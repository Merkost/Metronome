import argparse
import hashlib
import json
import os
import re
import shutil
import struct
import subprocess
import sys
import zipfile
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from xml.etree import ElementTree


ROOT = Path(__file__).resolve().parents[1]
ANDROID = "http://schemas.android.com/apk/res/android"
CERTIFICATE = "e73661c9046d6755c908f745cbddcadaaa8323f0102bfadc29add13dba6f2036"
BUNDLETOOL_SHA256 = "a099cfa1543f55593bc2ed16a70a7c67fe54b1747bb7301f37fdfd6d91028e29"
PAGE_SIZE = 16384


class VerificationError(ValueError):
    pass


def require(condition, message):
    if not condition:
        raise VerificationError(message)


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def run_command(command, output, name, allowed=(0,)):
    result = subprocess.run(command, capture_output=True, text=True, env={**os.environ, "LC_ALL": "C"})
    text = result.stdout + result.stderr
    (output / name).write_text(text)
    if allowed is not None:
        require(result.returncode in allowed, f"{name}: command exited {result.returncode}: {text[-700:].strip()}")
    return result.returncode, text


def manifest_identity(manifest):
    sdk = manifest.find("uses-sdk")
    application = manifest.find("application")
    require(sdk is not None and application is not None, "Manifest is missing uses-sdk or application")

    def attribute(element, name, default=None):
        return element.get(f"{{{ANDROID}}}{name}", default)

    def boolean(name):
        value = attribute(application, name, "false")
        require(value in ("true", "false", "1", "0", "-1"), f"Unresolved application {name}: {value}")
        return value in ("true", "1", "-1")

    return {
        "package": manifest.get("package"),
        "versionName": attribute(manifest, "versionName"),
        "versionCode": int(attribute(manifest, "versionCode")),
        "minSdk": int(attribute(sdk, "minSdkVersion")),
        "targetSdk": int(attribute(sdk, "targetSdkVersion")),
        "debuggable": boolean("debuggable"),
        "testOnly": boolean("testOnly"),
    }


def parse_aapt_manifest(text):
    stack = []
    root = None
    for line in text.splitlines():
        element = re.match(r"^(\s*)E: ([^\s(]+)", line)
        if element:
            indentation = len(element.group(1))
            while stack and stack[-1][0] >= indentation:
                stack.pop()
            node = ElementTree.Element(element.group(2))
            if stack:
                stack[-1][1].append(node)
            else:
                require(root is None, "APK manifest contains multiple roots")
                root = node
            stack.append((indentation, node))
            continue
        attribute = re.match(r"^\s*A: ([^(=]+)(?:\(0x[0-9a-fA-F]+\))?=(.*)$", line)
        if attribute:
            require(stack, "APK attribute appears outside an element")
            name, value = attribute.groups()
            if name.startswith(ANDROID + ":"):
                name = f"{{{ANDROID}}}" + name[len(ANDROID) + 1:]
            quoted = re.match(r'"(?:\\.|[^"\\])*"', value)
            if quoted:
                value = json.loads(quoted.group())
            else:
                value = value.split(" ", 1)[0]
                if value.startswith("0x"):
                    value = str(int(value, 16))
            stack[-1][1].set(name, value)
    require(root is not None and root.tag == "manifest", "Could not parse the APK manifest")
    return root


def check_identity(identity, expected, package):
    for key, value in expected.items():
        require(identity.get(key) == value, f"{package} {key}: expected {value!r}, found {identity.get(key)!r}")


def check_aab_signatures(path, output, java_home, expected_certificate):
    flags = ["-J-Duser.language=en", "-J-Duser.country=US", "-J-Duser.timezone=UTC"]
    code, text = run_command(
        [str(java_home / "jarsigner"), *flags, "-verify", "-strict", "-verbose", "-certs", str(path)],
        output,
        "aab-jarsigner.log",
        allowed=None,
    )
    rows = {}
    pattern = re.compile(r"^([smki? ]{6})\s*\d+\s+\w{3}\s+\w{3}\s+\d+\s+\d{2}:\d{2}:\d{2}\s+\S+\s+\d{4}\s+(.*)$")
    for line in text.splitlines():
        row = pattern.match(line)
        if row:
            rows[row.group(2)] = row.group(1)
    with zipfile.ZipFile(path) as archive:
        entries = [item.filename for item in archive.infolist() if not item.is_dir()]
        require(len(entries) == len(set(entries)), "AAB contains duplicate ZIP entries")
        signature_file = re.compile(r"^META-INF/(?:[^/]+\.(?:SF|RSA|DSA|EC)|SIG-[^/]+)$", re.IGNORECASE)
        payloads = [name for name in entries if not signature_file.match(name)]
    unsigned = [name for name in payloads if "s" not in rows.get(name, "")]
    require(not unsigned, f"AAB has unsigned or unverified payload entries: {', '.join(unsigned[:8])}")
    require("s" in rows.get("META-INF/MANIFEST.MF", ""), "AAB manifest signature did not verify")
    require("jar verified" in text, "AAB signature integrity verification did not pass")
    require(code in (0, 4), f"AAB strict signature check exited {code}")
    if code == 4:
        errors = text.split("\nError:", 1)[-1].split("\nWarning:", 1)[0].strip()
        allowed_errors = (
            "This jar contains entries whose certificate chain is invalid. Reason: PKIX path building failed:",
            "This jar contains entries whose signer certificate is self-signed.",
        )
        require(errors and all(line.startswith(allowed_errors) for line in errors.splitlines()), "AAB strict verification contains certificate errors beyond the pinned self-signed chain")
    _, certificate_text = run_command(
        [str(java_home / "keytool"), *flags, "-printcert", "-jarfile", str(path)], output, "aab-certificate.log"
    )
    certificates = {digest.replace(":", "").lower() for digest in re.findall(r"SHA256:\s*([0-9A-Fa-f:]+)", certificate_text)}
    require(certificates == {expected_certificate}, "AAB certificate does not match the expected production certificate")
    return {
        "signedPayloadEntries": len([name for name in payloads if name != "META-INF/MANIFEST.MF"]),
        "unsignedPayloadEntries": len(unsigned),
        "signedManifestVerified": True,
        "certificateSha256": expected_certificate,
        "strictExitCode": code,
        "selfSignedCertificatePinned": code == 4,
        "jarInputStreamOrderWarning": "internal inconsistencies" in text,
        "timestampPresent": "do not include a timestamp" not in text,
        "log": "aab-jarsigner.log",
    }


def check_apk_signatures(path, output, build_tools, expected_certificate):
    code, text = run_command(
        [str(build_tools / "apksigner"), "verify", "--verbose", "--print-certs", str(path)],
        output,
        "apk-signature.log",
        allowed=None,
    )
    require(code == 0 and re.search(r"^Verifies$", text, re.MULTILINE), "APK signature verification failed; see apk-signature.log")
    certificates = set(re.findall(r"certificate SHA-256 digest:\s*([0-9a-fA-F]{64})", text))
    require({value.lower() for value in certificates} == {expected_certificate}, "APK certificate does not match the expected production certificate")
    require(re.search(r"^Number of signers: 1$", text, re.MULTILINE), "APK must have exactly one production signer")
    return {
        "certificateSha256": expected_certificate,
        "signatureSchemes": {name: value == "true" for name, value in re.findall(r"Verified using ([^(\n]+).*?: (true|false)", text)},
        "log": "apk-signature.log",
    }


def inspect_elf_alignment(data, name):
    require(len(data) >= 64 and data[:4] == b"\x7fELF", f"Invalid ELF library: {name}")
    bits, byte_order = data[4:6]
    require(bits in (1, 2) and byte_order in (1, 2), f"Unsupported ELF encoding: {name}")
    endian = "<" if byte_order == 1 else ">"
    width = "Q" if bits == 2 else "I"
    program_offset = struct.unpack_from(endian + width, data, 32 if bits == 2 else 28)[0]
    entry_size, count = struct.unpack_from(endian + "HH", data, 54 if bits == 2 else 42)
    require(entry_size >= (56 if bits == 2 else 32), f"Invalid ELF program-header size: {name}")
    require(program_offset + entry_size * count <= len(data), f"Truncated ELF program headers: {name}")
    segments = []
    for index in range(count):
        header = program_offset + index * entry_size
        if struct.unpack_from(endian + "I", data, header)[0] != 1:
            continue
        offset, address = struct.unpack_from(endian + width * 2, data, header + (8 if bits == 2 else 4))
        alignment = struct.unpack_from(endian + width, data, header + (48 if bits == 2 else 28))[0]
        require(alignment >= PAGE_SIZE and alignment & (alignment - 1) == 0, f"ELF LOAD segment alignment is below 16KiB: {name}")
        require((address - offset) % PAGE_SIZE == 0, f"ELF LOAD segment offset is not aligned to its virtual address: {name}")
        segments.append({"offset": offset, "virtualAddress": address, "alignment": alignment})
    require(segments, f"ELF library contains no LOAD segments: {name}")
    return {"entry": name, "bits": 64 if bits == 2 else 32, "elf16KiBAligned": True, "loadSegments": segments, "sha256": sha256(data)}


def inspect_package(path, kind, sound_directory, sound_count, expected_abis):
    canonical = {file.name: sha256(file.read_bytes()) for file in sorted(sound_directory.iterdir()) if file.suffix.lower() in (".wav", ".mp3")}
    require(len(canonical) == sound_count, f"Expected {sound_count} canonical sounds, found {len(canonical)}")
    profiles = (
        ["BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof", "BUNDLE-METADATA/com.android.tools.build.profiles/baseline.profm"]
        if kind == "aab" else ["assets/dexopt/baseline.prof", "assets/dexopt/baseline.profm"]
    )
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        require(len(names) == len(set(names)), f"{kind} contains duplicate ZIP entries")
        sounds = {name: sha256(archive.read(name)) for name in names if Path(name).suffix.lower() in (".wav", ".mp3")}
        require(Counter(sounds.values()) == Counter(canonical.values()), f"{kind} packaged audio hashes do not match the {sound_count} canonical resources")
        audio = [
            {"resource": resource, "sha256": digest, "packagedEntries": [name for name, value in sounds.items() if value == digest]}
            for resource, digest in canonical.items()
        ]
        profile_records = []
        for name in profiles:
            require(name in names, f"{kind} is missing the embedded baseline profile: {name}")
            data = archive.read(name)
            require(len(data) > 8 and data[:4] == (b"pro\x00" if name.endswith(".prof") else b"prm\x00"), f"{kind} has an invalid baseline profile: {name}")
            profile_records.append({"entry": name, "bytes": len(data), "sha256": sha256(data)})
        native = [inspect_elf_alignment(archive.read(name), name) for name in names if name.endswith(".so")]
        actual_abis = set()
        for library in native:
            match = re.fullmatch(r"(?:base/)?lib/([^/]+)/[^/]+\.so", library["entry"])
            require(match, f"Unexpected native-library path: {library['entry']}")
            actual_abis.add(match.group(1))
        require(actual_abis == expected_abis, f"{kind} native ABIs: expected {sorted(expected_abis)}, found {sorted(actual_abis)}")
    return {
        "soundResources": len(sounds),
        "soundResourcePayloads": audio,
        "embeddedProfiles": profiles,
        "profilePayloads": profile_records,
        "nativeLibraries": native,
        "nativeAbis": sorted(actual_abis),
    }


def verify(arguments, report):
    output = arguments.outputdir
    expected = {
        "package": arguments.expected_package,
        "versionName": arguments.expected_version,
        "versionCode": arguments.expected_code,
        "minSdk": arguments.expected_min_sdk,
        "targetSdk": arguments.expected_target_sdk,
        "debuggable": False,
        "testOnly": False,
    }
    report["expectedIdentity"] = expected
    for path in (arguments.aab, arguments.apk, arguments.bundletool_all):
        require(path.is_file(), f"Required file does not exist: {path}")
    require(sha256(arguments.bundletool_all.read_bytes()) == arguments.bundletool_sha256.lower(), "Bundletool SHA256 does not match its pinned release")
    report["bundletool"] = {"path": str(arguments.bundletool_all), "sha256": arguments.bundletool_sha256.lower()}
    java_home = arguments.java_home / "bin" if arguments.java_home else Path(shutil.which("java") or "").parent
    for tool in (java_home / "java", java_home / "jarsigner", java_home / "keytool", arguments.build_tools / "apksigner", arguments.build_tools / "aapt2", arguments.build_tools / "zipalign"):
        require(tool.is_file(), f"Required verification tool does not exist: {tool}")
    run_command([str(java_home / "java"), "-version"], output, "java-version.log")
    bundletool = [str(java_home / "java"), "-jar", str(arguments.bundletool_all)]
    _, version = run_command([*bundletool, "version"], output, "bundletool-version.log")
    report["bundletool"]["version"] = version.strip()
    run_command([*bundletool, "validate", f"--bundle={arguments.aab}"], output, "bundle-validation.log")
    report["aabValidationPassed"] = True
    _, manifest = run_command([*bundletool, "dump", "manifest", f"--bundle={arguments.aab}", "--module=base"], output, "aab-manifest.xml")
    aab_identity = manifest_identity(ElementTree.fromstring(manifest))
    check_identity(aab_identity, expected, "AAB")
    _, apk_tree = run_command([str(arguments.build_tools / "aapt2"), "dump", "xmltree", "--file", "AndroidManifest.xml", str(arguments.apk)], output, "apk-manifest-tree.log")
    apk_manifest = parse_aapt_manifest(apk_tree)
    ElementTree.indent(apk_manifest)
    (output / "apk-manifest.xml").write_text(ElementTree.tostring(apk_manifest, encoding="unicode") + "\n")
    apk_identity = manifest_identity(apk_manifest)
    check_identity(apk_identity, expected, "APK")
    report["identities"] = {"aab": aab_identity, "apk": apk_identity}
    certificate = arguments.expected_certificate.replace(":", "").lower()
    require(re.fullmatch(r"[0-9a-f]{64}", certificate), "Expected certificate SHA256 must contain 64 hexadecimal characters")
    report["signatures"] = {
        "aab": check_aab_signatures(arguments.aab, output, java_home, certificate),
        "apk": check_apk_signatures(arguments.apk, output, arguments.build_tools, certificate),
    }
    report["productionSignaturesVerified"] = True
    report["productionCertificateSha256"] = certificate
    run_command([str(arguments.build_tools / "zipalign"), "-c", "-P", "16", "-v", "4", str(arguments.apk)], output, "apk-zipalign.log")
    report["apk16KiBAlignmentVerified"] = True
    abis = set(arguments.expected_abis.split(","))
    report["packages"] = {
        kind: inspect_package(path, kind, arguments.sound_dir, arguments.expected_sound_count, abis)
        for kind, path in (("aab", arguments.aab), ("apk", arguments.apk))
    }
    report["packages"]["aab"].update({key: report["signatures"]["aab"][key] for key in ("signedPayloadEntries", "unsignedPayloadEntries")})
    native = {
        kind: {item["entry"].removeprefix("base/"): item["sha256"] for item in package["nativeLibraries"]}
        for kind, package in report["packages"].items()
    }
    require(native["aab"] == native["apk"], "AAB and APK native-library payloads differ")
    report.update({"versionName": arguments.expected_version, "versionCode": arguments.expected_code, "checksPassed": True})


def arguments():
    sdk = Path(os.environ.get("ANDROID_SDK_ROOT", os.environ.get("ANDROID_HOME", str(Path.home() / "Library/Android/sdk"))))
    java_home = os.environ.get("JAVA_HOME")
    known_jdk = Path.home() / "Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home"
    parser = argparse.ArgumentParser(description="Verify signed Metronome Android release packages without signing or uploading them")
    parser.add_argument("--aab", type=Path, required=True)
    parser.add_argument("--apk", type=Path, required=True)
    parser.add_argument("--bundletool-all", type=Path, required=True)
    parser.add_argument("--bundletool-sha256", default=BUNDLETOOL_SHA256)
    parser.add_argument("--outputdir", type=Path, required=True)
    parser.add_argument("--expected-package", default="com.merkost.metronome")
    parser.add_argument("--expected-version", required=True)
    parser.add_argument("--expected-code", required=True, type=int)
    parser.add_argument("--expected-certificate", default=CERTIFICATE)
    parser.add_argument("--expected-min-sdk", default=26, type=int)
    parser.add_argument("--expected-target-sdk", default=37, type=int)
    parser.add_argument("--expected-sound-count", default=11, type=int)
    parser.add_argument("--expected-abis", default="arm64-v8a,armeabi-v7a,x86,x86_64")
    parser.add_argument("--sound-dir", type=Path, default=ROOT / "shared/src/androidMain/res/raw")
    parser.add_argument("--build-tools", type=Path, default=sdk / "build-tools/37.0.0")
    parser.add_argument("--java-home", type=Path, default=Path(java_home) if java_home else known_jdk if known_jdk.is_dir() else None)
    return parser.parse_args()


def main():
    options = arguments()
    options.outputdir.mkdir(parents=True, exist_ok=True)
    report = {
        "schemaVersion": 1,
        "verifiedAtUtc": datetime.now(timezone.utc).isoformat(),
        "checksPassed": False,
        "files": {},
        "actionsPerformed": ["local package verification"],
    }
    checksums = []
    for path in (options.aab, options.apk):
        if path.is_file():
            digest = sha256(path.read_bytes())
            report["files"][path.name] = {"path": str(path.resolve()), "bytes": path.stat().st_size, "sha256": digest}
            checksums.append(f"{digest}  {path.name}")
    try:
        verify(options, report)
    except (ValueError, OSError, zipfile.BadZipFile, ElementTree.ParseError, struct.error) as error:
        report["error"] = str(error)
    report_file = options.outputdir / "release-verification.json"
    temporary = report_file.with_suffix(".json.tmp")
    temporary.write_text(json.dumps(report, indent=2) + "\n")
    temporary.replace(report_file)
    (options.outputdir / "SHA256SUMS").write_text("\n".join(checksums) + "\n")
    if not report["checksPassed"]:
        print(f"Release verification failed: {report['error']}", file=sys.stderr)
        return 1
    print(f"Verified {options.expected_version} ({options.expected_code}): signed AAB/APK, identities, sound payloads, profiles and 16KiB alignment")
    print(str(report_file.resolve()))
    return 0


if __name__ == "__main__":
    sys.exit(main())
