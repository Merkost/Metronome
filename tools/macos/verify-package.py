import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import plistlib
import re
import stat
import subprocess
import tempfile
import zipfile

from release_support import (
    APP_ID,
    INSTALLER_AUTHORITY,
    RUNTIME_ID,
    TEAM_ID,
    ValidationError,
    check_code_signature,
    check_entitlements,
    check_profile,
    check_runtime_properties,
    read_code_entitlements,
    read_profile,
    read_properties,
    require,
    run,
    sha256,
)


MACH_O_MAGIC = {
    b"\xfe\xed\xfa\xce",
    b"\xce\xfa\xed\xfe",
    b"\xfe\xed\xfa\xcf",
    b"\xcf\xfa\xed\xfe",
    b"\xca\xfe\xba\xbe",
    b"\xbe\xba\xfe\xca",
    b"\xca\xfe\xba\xbf",
    b"\xbf\xba\xfe\xca",
}

MINIMUM_MACOS = "13.0"


def check_compiled_runtime_identity(properties, inspection_properties, compiled_metadata):
    check_runtime_properties(inspection_properties)
    require(inspection_properties.get("OS_NAME") == "Darwin", "Runtime inspection requires a macOS JBR installation")
    require(inspection_properties.get("JAVA_RUNTIME_VERSION"), "Runtime inspection JBR has no full runtime version")
    constants = {}
    for name in ["java_version", "java_runtime_version", "VENDOR"]:
        values = re.findall(r'^\s*private static final java\.lang\.String ' + name + r' = "([^"\\]*)";\s*$', compiled_metadata, re.MULTILINE)
        require(len(values) == 1, f"Missing or ambiguous compiled runtime identity: {name}")
        constants[name] = values[0]
    require(constants["VENDOR"] == inspection_properties["IMPLEMENTOR"], "Bundled runtime vendor does not match the verified JetBrains Runtime")
    require(constants["java_version"] == inspection_properties["JAVA_VERSION"], "Bundled Java version does not match the selected JetBrains Runtime")
    require(constants["java_runtime_version"] == inspection_properties["JAVA_RUNTIME_VERSION"], "Bundled runtime build does not match the selected JetBrains Runtime")
    require(properties.get("JAVA_VERSION") == constants["java_version"], "Bundled release metadata conflicts with its compiled Java version")
    for key, expected in [("IMPLEMENTOR", constants["VENDOR"]), ("JAVA_RUNTIME_VERSION", constants["java_runtime_version"]), ("OS_NAME", "Darwin")]:
        require(key not in properties or properties[key] == expected, f"Bundled release metadata has a conflicting {key}")
    require("OS_ARCH" not in properties or properties["OS_ARCH"] in {"aarch64", "arm64"}, "Bundled release metadata has a conflicting architecture")
    return {
        "JAVA_VERSION": constants["java_version"],
        "JAVA_RUNTIME_VERSION": constants["java_runtime_version"],
        "IMPLEMENTOR": constants["VENDOR"],
        "identitySource": "java.base/java/lang/VersionProps.class",
    }


def check_bundled_runtime(runtime_home, jdk_home):
    require(jdk_home is not None, "Pass --jdk-home, METRONOME_MACOS_JDK_HOME or JAVA_HOME for the full JBR used to build this app")
    jdk_home = Path(jdk_home).expanduser().resolve()
    inspection_properties = read_properties(jdk_home / "release")
    check_runtime_properties(inspection_properties)
    require(inspection_properties.get("OS_NAME") == "Darwin", "Runtime inspection requires a macOS JBR installation")
    jimage = jdk_home / "bin/jimage"
    javap = jdk_home / "bin/javap"
    require(jimage.is_file() and javap.is_file(), "The full JBR must contain jimage and javap for static runtime inspection")
    modules = runtime_home / "lib/modules"
    require(modules.is_file(), "The bundled Java module image is missing")
    properties = read_properties(runtime_home / "release")
    with tempfile.TemporaryDirectory(prefix="metronome-runtime-check-") as temporary:
        run([jimage, "extract", "--include", "glob:/java.base/java/lang/VersionProps.class", "--dir", temporary, modules])
        identity_class = Path(temporary) / "java.base/java/lang/VersionProps.class"
        require(identity_class.is_file(), "The bundled runtime has no compiled version identity")
        metadata = run([javap, "-private", "-constants", identity_class]).decode()
        summary = check_compiled_runtime_identity(properties, inspection_properties, metadata)
        summary["compiledIdentitySha256"] = sha256(identity_class)
    summary["moduleImageSha256"] = sha256(modules)
    return summary


def check_runtime_library_entitlements(runtime, runtime_info):
    executable = runtime / "Contents/MacOS" / runtime_info.get("CFBundleExecutable", "")
    require(executable.is_file(), "The runtime bundle library is missing")
    description = run(["/usr/bin/file", "-b", executable]).decode()
    require("Mach-O" in description and "dynamically linked shared library" in description, "The unentitled runtime bundle must contain library code, not a main executable")
    entitlements = read_code_entitlements(runtime)
    require(not entitlements, "The runtime library must not claim process entitlements")
    return entitlements


def regular_files(directory):
    return sorted(path for path in directory.rglob("*") if path.is_file() and not path.is_symlink())


def macos_version_components(version):
    require(isinstance(version, str) and re.fullmatch(r"[1-9][0-9]*(?:\.[0-9]+){1,2}", version), f"Unparseable macOS deployment version: {version!r}")
    parts = [int(part) for part in version.split(".")]
    return tuple(parts + [0] * (3 - len(parts)))


def load_command_field(block, name):
    values = []
    for line in block.splitlines():
        parts = line.strip().split(maxsplit=1)
        if parts and parts[0] == name:
            values.append(parts[1] if len(parts) > 1 else "")
    require(len(values) == 1, f"Missing or ambiguous native deployment field: {name}")
    return values[0]


def native_minimum_macos(load_commands, supported_macos=MINIMUM_MACOS):
    deployment_commands = {
        "LC_BUILD_VERSION",
        "LC_VERSION_MIN_MACOSX",
        "LC_VERSION_MIN_IPHONEOS",
        "LC_VERSION_MIN_TVOS",
        "LC_VERSION_MIN_WATCHOS",
    }
    deployments = []
    for block in re.split(r"(?m)^[ \t]*Load command[ \t]+[0-9]+[ \t]*\r?$", load_commands)[1:]:
        commands = re.findall(r"(?m)^[ \t]*cmd[ \t]+([^\r\n]+)", block)
        if any(command.strip().split()[0] in deployment_commands for command in commands if command.strip()):
            require(len(commands) == 1 and commands[0].strip() in deployment_commands, "Ambiguous or unparseable native deployment command")
            deployments.append((commands[0].strip(), block))
    require(len(deployments) == 1, "Native payload must contain exactly one macOS deployment command")
    command, block = deployments[0]
    if command == "LC_BUILD_VERSION":
        require(load_command_field(block, "platform").lower() in {"1", "macos"}, "Native payload targets a different Apple platform")
        version = load_command_field(block, "minos")
    else:
        require(command == "LC_VERSION_MIN_MACOSX", "Native payload targets a different Apple platform")
        version = load_command_field(block, "version")
    require(macos_version_components(version) <= macos_version_components(supported_macos), f"Native payload requires macOS {version}, later than the claimed {supported_macos} minimum")
    return version


def file_manifest(directory):
    return {
        str(path.relative_to(directory)): {"bytes": path.stat().st_size, "sha256": sha256(path)}
        for path in regular_files(directory)
    }


def check_bundle_paths(app):
    base = app.resolve()
    for path in app.rglob("*"):
        require(not path.stat().st_mode & stat.S_IWOTH, f"World-writable bundle content is forbidden: {path}")
        if path.is_symlink():
            try:
                path.resolve(strict=True).relative_to(base)
            except (ValueError, OSError):
                raise ValidationError(f"Bundle symlink points outside the app or is broken: {path}")
    plugins = app / "Contents/PlugIns"
    require(not plugins.exists() or not any(plugins.iterdir()), "This release must not include an additional plug-in directory")


def check_mach_o(app, signed):
    binaries = []
    for path in regular_files(app):
        if path.suffix in {".class", ".jar", ".zip"}:
            continue
        with path.open("rb") as source:
            if source.read(4) not in MACH_O_MAGIC:
                continue
        description = run(["/usr/bin/file", "-b", path]).decode()
        require("Mach-O" in description, f"Unrecognized native payload: {path}")
        architectures = run(["/usr/bin/lipo", "-archs", path]).decode().strip().split()
        require(architectures == ["arm64"], f"Native payload is not arm64-only: {path}")
        load_commands = run(["/usr/bin/otool", "-l", path]).decode()
        try:
            minimum_macos = native_minimum_macos(load_commands)
        except ValidationError as error:
            raise ValidationError(f"{path}: {error}") from error
        entry = {"path": str(path.relative_to(app)), "architectures": architectures, "minimumMacOS": minimum_macos, "sha256": sha256(path)}
        if signed:
            entry["signature"] = check_code_signature(path)
        binaries.append(entry)
    require(binaries, "No native launcher or runtime libraries were found")
    names = {Path(entry["path"]).name for entry in binaries}
    require("libjvm.dylib" in names, "The app must embed its Java VM")
    require(any("skiko" in name.lower() for name in names), "The app must embed its signed Skiko renderer instead of extracting it at runtime")
    require(any("awt" in name.lower() for name in names), "The app must embed the AWT desktop runtime")
    return binaries


def packaged_payloads(app):
    payloads = {}
    for path in regular_files(app):
        if path.suffix.lower() in {".wav", ".mp3"}:
            payloads.setdefault(sha256(path), []).append(str(path.relative_to(app)))
        if path.suffix == ".jar":
            with zipfile.ZipFile(path) as archive:
                require(archive.testzip() is None, f"Invalid packaged JAR: {path}")
                for info in archive.infolist():
                    if info.is_dir():
                        continue
                    require(not info.filename.lower().endswith((".dylib", ".jnilib", ".so", ".dll")), f"Native library must be bundled and signed outside its JAR: {path.name}!/{info.filename}")
                    if info.filename.lower().endswith((".wav", ".mp3")):
                        digest = hashlib.sha256(archive.read(info)).hexdigest()
                        payloads.setdefault(digest, []).append(f"{path.relative_to(app)}!/{info.filename}")
    return payloads


def check_audio(app, root):
    source_directory = root / "shared/src/desktopMain/resources/audio"
    manifest = source_directory / "origin-manifest.json"
    require(manifest.is_file(), "The desktop audio origin manifest is missing")
    data = json.loads(manifest.read_text())
    entries = data.get("sounds", data.get("files", data.get("entries", [])))
    require(isinstance(entries, list) and len(entries) == 11, "The desktop audio manifest must describe all eleven canonical audio payloads")
    payloads = packaged_payloads(app)
    verified = []
    for entry in entries:
        source_name = entry.get("source", entry.get("sourcePath"))
        desktop_name = entry.get("desktop", entry.get("desktopPath", entry.get("resourcePath", entry.get("output"))))
        source_digest = entry.get("sourceSha256")
        desktop_digest = entry.get("desktopSha256", entry.get("packagedSha256", entry.get("outputSha256", entry.get("sha256"))))
        require(source_name and desktop_name and source_digest and desktop_digest, "Audio origin entries need source and resource paths with their SHA256 hashes")
        source = root / source_name
        if not source.is_file():
            source = root / "shared/src/androidMain/res/raw" / Path(source_name).name
        desktop = root / desktop_name
        if not desktop.is_file():
            desktop = source_directory / Path(desktop_name).name
        require(source.is_file() and sha256(source) == source_digest, f"Canonical audio source does not match its origin manifest: {source_name}")
        require(desktop.is_file() and sha256(desktop) == desktop_digest, f"Desktop audio does not match its origin manifest: {desktop_name}")
        require(desktop_digest in payloads, f"Desktop audio is missing or changed in the package: {desktop_name}")
        verified.append({"source": str(source.relative_to(root)), "desktop": str(desktop.relative_to(root)), "sha256": desktop_digest, "packageEntries": payloads[desktop_digest]})
    require(len({entry["source"] for entry in verified}) == 11, "The audio origin manifest repeats a canonical source")
    return verified


def check_app(app, root, expected_version, expected_build, signed, testflight=False, jdk_home=None):
    require(app.is_dir() and app.suffix == ".app", "Pass the packaged Metronome.app directory")
    info_path = app / "Contents/Info.plist"
    require(info_path.is_file(), "Packaged app Info.plist is missing")
    info = plistlib.loads(info_path.read_bytes())
    require(info.get("CFBundleIdentifier") == APP_ID, "The app bundle identifier is incorrect")
    require(info.get("CFBundleShortVersionString") == expected_version, "The app version is incorrect")
    require(info.get("CFBundleVersion") == expected_build, "The app build number is incorrect")
    require(info.get("CFBundlePackageType") == "APPL", "The bundle is not a macOS application")
    require(info.get("LSApplicationCategoryType") == "public.app-category.music", "The app category must be Music")
    require(info.get("LSMinimumSystemVersion") == MINIMUM_MACOS, f"The minimum supported macOS version must be {MINIMUM_MACOS}")
    require(not info.get("NSMicrophoneUsageDescription"), "Metronome playback does not need microphone access")
    require((app / "Contents/MacOS" / info.get("CFBundleExecutable", "")).is_file(), "Native app launcher is missing")
    require((app / "Contents/Resources" / info.get("CFBundleIconFile", "")).is_file(), "The app icon is missing")
    runtime = app / "Contents/runtime"
    runtime_home = runtime / "Contents/Home"
    runtime_properties = read_properties(runtime_home / "release")
    runtime_summary = check_bundled_runtime(runtime_home, jdk_home)
    modules = set(runtime_properties.get("MODULES", "").split())
    require({"java.base", "java.desktop", "java.logging", "java.prefs", "jdk.unsupported", "jdk.charsets"}.issubset(modules), "The bundled runtime is missing a required Java module")
    runtime_info = plistlib.loads((runtime / "Contents/Info.plist").read_bytes())
    require(runtime_info.get("CFBundleIdentifier") == RUNTIME_ID, "The Java runtime bundle ID does not match its explicit store profile")
    check_bundle_paths(app)
    signatures = {}
    if signed:
        run(["/usr/bin/codesign", "--verify", "--deep", "--strict", app])
        for role, bundle, identifier, user_files in [("app", app, APP_ID, True), ("runtime", runtime, RUNTIME_ID, False)]:
            signature = check_code_signature(bundle)
            if role == "runtime" and not testflight:
                signature["entitlements"] = check_runtime_library_entitlements(runtime, runtime_info)
                signature["codeKind"] = "Library loaded inside the sandboxed main process"
            else:
                signature["entitlements"] = check_entitlements(read_code_entitlements(bundle), identifier, user_selected_files=user_files, provisioned=testflight)
            profile_path = bundle / "Contents/embedded.provisionprofile"
            if testflight:
                signature["profile"] = check_profile(read_profile(profile_path), identifier)
            else:
                require(not profile_path.exists(), f"Production-only {identifier} must not embed an unused provisioning profile")
                signature["restrictedEntitlementsAbsent"] = True
            signatures[role] = signature
    launcher_configs = sorted((app / "Contents/app").glob("*.cfg"))
    require(len(launcher_configs) == 1, "The self-contained Java launcher configuration is missing or ambiguous")
    configuration = launcher_configs[0].read_text()
    require("com.merkost.metronome.desktop.MainKt" in configuration, "The desktop entry point is incorrect")
    require("skiko.library.path=$APPDIR" in configuration, "Skiko must load from the bundled, signed application directory")
    for line in configuration.splitlines():
        if line.startswith("app.classpath="):
            classpath = line.split("=", 1)[1]
            for item in classpath.split(":"):
                require(item.startswith("$APPDIR/"), "The launcher must not load code from an external classpath")
                require((app / "Contents/app" / item.removeprefix("$APPDIR/")).is_file(), "A bundled classpath entry is missing")
    return {
        "identifier": APP_ID,
        "version": expected_version,
        "build": expected_build,
        "minimumMacOS": MINIMUM_MACOS,
        "category": "public.app-category.music",
        "runtime": runtime_summary,
        "signatures": signatures,
        "nativeBinaries": check_mach_o(app, signed),
        "audioPayloads": check_audio(app, root),
        "fileManifest": file_manifest(app),
    }


def check_pkg(pkg, app):
    require(pkg.is_file() and pkg.suffix == ".pkg", "Pass the signed Mac App Store .pkg file")
    result = run(["/usr/sbin/pkgutil", "--check-signature", pkg]).decode()
    require(INSTALLER_AUTHORITY in result, "The installer is not signed with the expected Mac Installer Distribution identity")
    require("not trusted" not in result.lower(), "The installer signature is not trusted")
    with tempfile.TemporaryDirectory(prefix="metronome-pkg-check-") as temporary:
        expanded = Path(temporary) / "expanded"
        run(["/usr/sbin/pkgutil", "--expand-full", pkg, expanded])
        applications = [path for path in expanded.rglob("Metronome.app") if path.is_dir()]
        require(len(applications) == 1, "The installer must contain exactly one Metronome app")
        require(file_manifest(applications[0]) == file_manifest(app), "Installer app bytes differ from the verified application image")
        for scripts in expanded.rglob("Scripts"):
            require(not scripts.is_dir() or not any(path.is_file() for path in scripts.rglob("*")), "The App Store installer must not contain custom installer scripts")
    return {"path": str(pkg.resolve()), "bytes": pkg.stat().st_size, "sha256": sha256(pkg), "installerSignatureVerified": True, "appBytesMatchVerifiedImage": True}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--app", type=Path, required=True)
    parser.add_argument("--pkg", type=Path)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--expected-version", default="1.6.0")
    parser.add_argument("--expected-build", default="1")
    parser.add_argument("--unsigned", action="store_true")
    parser.add_argument("--testflight", action="store_true")
    parser.add_argument("--jdk-home", type=Path, default=os.environ.get("METRONOME_MACOS_JDK_HOME") or os.environ.get("JAVA_HOME"))
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    try:
        require(not args.unsigned or args.pkg is None, "Unsigned checks cannot certify an App Store installer")
        require(not args.unsigned or not args.testflight, "TestFlight checks require distribution signatures")
        app = args.app.expanduser().resolve()
        result = {
            "verifiedAtUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            "classification": "Local development app image" if args.unsigned else "Mac App Store signed application",
            "appPath": str(app),
            "checksPassed": True,
            "appStoreUploadPerformed": False,
            "submittedForReview": False,
            "testFlightProvisioningChecked": args.testflight,
            "scope": "Static packaged artifact checks; runtime UI, audio, file-access and sandbox acceptance are separate gates",
            "app": check_app(app, args.root.resolve(), args.expected_version, args.expected_build, not args.unsigned, args.testflight, args.jdk_home),
        }
        if args.pkg:
            result["package"] = check_pkg(args.pkg.expanduser().resolve(), app)
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(json.dumps(result, indent=2) + "\n")
        summary = {key: value for key, value in result.items() if key != "app"}
        summary["version"] = args.expected_version
        summary["build"] = args.expected_build
        summary["nativeBinariesVerified"] = len(result["app"]["nativeBinaries"])
        summary["audioPayloadsVerified"] = len(result["app"]["audioPayloads"])
        print(json.dumps(summary, indent=2))
    except (ValidationError, OSError, ValueError, plistlib.InvalidFileException, zipfile.BadZipFile) as error:
        parser.exit(2, f"Mac package verification failed: {error}\n")


if __name__ == "__main__":
    main()
