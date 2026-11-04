import datetime
import hashlib
from pathlib import Path
import plistlib
import re
import subprocess


TEAM_ID = "P47X2292CM"
APP_ID = "com.merkost.metronome"
RUNTIME_ID = "com.oracle.java.com.merkost.metronome"
APPLICATION_AUTHORITY = "3rd Party Mac Developer Application: Konstantin Merenkov (P47X2292CM)"
INSTALLER_AUTHORITY = "3rd Party Mac Developer Installer: Konstantin Merenkov (P47X2292CM)"
FORBIDDEN_ENTITLEMENTS = {
    "com.apple.security.cs.debugger",
    "com.apple.security.cs.allow-dyld-environment-variables",
    "com.apple.security.cs.allow-unsigned-executable-memory",
    "com.apple.security.cs.disable-library-validation",
    "com.apple.security.device.audio-input",
    "com.apple.security.device.camera",
    "com.apple.security.network.client",
    "com.apple.security.network.server",
    "com.apple.security.get-task-allow",
    "get-task-allow",
}


class ValidationError(Exception):
    pass


def require(condition, message):
    if not condition:
        raise ValidationError(message)


def run(arguments):
    result = subprocess.run([str(value) for value in arguments], capture_output=True)
    require(
        result.returncode == 0,
        f"{Path(arguments[0]).name} failed: {result.stderr.decode(errors='replace').strip()}",
    )
    return result.stdout


def sha256(path):
    digest = hashlib.sha256()
    with Path(path).open("rb") as source:
        for block in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def read_properties(path):
    properties = {}
    for line in Path(path).read_text().splitlines():
        if "=" in line:
            key, value = line.split("=", 1)
            properties[key] = value.strip().strip('"')
    return properties


def check_runtime_properties(properties):
    require(properties.get("JAVA_VERSION", "").split(".")[0] == "21", "Packaging requires JetBrains Runtime 21")
    require("JetBrains" in properties.get("IMPLEMENTOR", ""), "Packaging requires the JetBrains Runtime, selected with macos.jdkHome or METRONOME_MACOS_JDK_HOME")
    require(properties.get("OS_ARCH") in {"aarch64", "arm64"}, "This Mac release requires the arm64 JetBrains Runtime")
    return {key: properties.get(key) for key in ["JAVA_VERSION", "IMPLEMENTOR", "OS_ARCH", "OS_NAME"]}


def check_entitlements(entitlements, identifier, team_id=TEAM_ID, user_selected_files=False, provisioned=False):
    expected = {
        "com.apple.security.app-sandbox": True,
        "com.apple.security.cs.allow-jit": True,
    }
    if provisioned:
        expected["com.apple.application-identifier"] = f"{team_id}.{identifier}"
        expected["com.apple.developer.team-identifier"] = team_id
    else:
        for key in ["com.apple.application-identifier", "com.apple.developer.team-identifier"]:
            require(key not in entitlements, f"Production-only {identifier} must omit the provisioned {key} claim")
    if user_selected_files:
        expected["com.apple.security.files.user-selected.read-write"] = True
    for key, value in expected.items():
        require(entitlements.get(key) == value, f"{identifier} has an unexpected or missing {key} entitlement")
    for key in FORBIDDEN_ENTITLEMENTS:
        require(not entitlements.get(key), f"{identifier} must not request {key}")
    unexpected = {key for key, value in entitlements.items() if value and key not in expected}
    require(not unexpected, f"{identifier} requests unreviewed entitlements: {', '.join(sorted(unexpected))}")
    return expected


def read_profile(path):
    path = Path(path)
    require(path.is_file(), f"Provisioning profile does not exist: {path}")
    return plistlib.loads(run(["/usr/bin/security", "cms", "-D", "-i", path]))


def check_profile(profile, identifier, team_id=TEAM_ID, certificate_sha1=None):
    require("OSX" in profile.get("Platform", []), f"{identifier} needs a macOS profile, not an iOS profile")
    require(profile.get("TeamIdentifier") == [team_id], f"{identifier} profile belongs to a different developer team")
    entitlements = profile.get("Entitlements", {})
    require(entitlements.get("com.apple.application-identifier") == f"{team_id}.{identifier}", f"{identifier} profile must use its explicit App ID")
    require(not profile.get("ProvisionedDevices") and not profile.get("ProvisionsAllDevices"), f"{identifier} needs a Mac App Store distribution profile")
    require(not entitlements.get("com.apple.security.get-task-allow") and not entitlements.get("get-task-allow"), f"{identifier} profile enables debugging")
    expires = profile.get("ExpirationDate")
    require(isinstance(expires, datetime.datetime), f"{identifier} profile has no valid expiry")
    now = datetime.datetime.now(datetime.timezone.utc)
    require(expires.replace(tzinfo=datetime.timezone.utc) > now, f"{identifier} provisioning profile has expired")
    certificates = [hashlib.sha1(certificate).hexdigest().upper() for certificate in profile.get("DeveloperCertificates", [])]
    require(certificates, f"{identifier} profile has no signing certificate")
    if certificate_sha1:
        require(certificate_sha1.upper() in certificates, f"{identifier} profile does not include the selected Mac App Distribution certificate")
    return {
        "identifier": identifier,
        "teamIdentifier": team_id,
        "name": profile.get("Name"),
        "uuid": profile.get("UUID"),
        "expiresAtUtc": expires.replace(tzinfo=datetime.timezone.utc).isoformat(),
        "distribution": "Mac App Store",
        "certificateSha1": certificates,
    }


def signing_identity_hashes(identity):
    output = run(["/usr/bin/security", "find-identity", "-v"]).decode()
    names = {
        "application": f"3rd Party Mac Developer Application: {identity}",
        "installer": f"3rd Party Mac Developer Installer: {identity}",
    }
    found = {}
    for line in output.splitlines():
        if "CSSMERR" in line:
            continue
        match = re.search(r'([0-9A-F]{40}) "([^"]+)"', line)
        if match:
            for role, name in names.items():
                if match.group(2) == name:
                    found[role] = match.group(1)
    for role, name in names.items():
        require(role in found, f"Missing valid signing identity with private key: {name}")
    return found


def read_code_entitlements(path):
    output = run(["/usr/bin/codesign", "-d", "--entitlements", ":-", path])
    return plistlib.loads(output) if output.strip() else {}


def check_code_signature(path, team_id=TEAM_ID):
    run(["/usr/bin/codesign", "--verify", "--strict", path])
    result = subprocess.run(["/usr/bin/codesign", "-dv", "--verbose=4", str(path)], capture_output=True)
    require(result.returncode == 0, f"Cannot inspect code signature: {path}")
    details = result.stderr.decode()
    require(f"TeamIdentifier={team_id}" in details, f"Code has a different signing team: {path}")
    require(f"Authority=3rd Party Mac Developer Application: " in details, f"Code is not signed for the Mac App Store: {path}")
    return {
        "teamIdentifier": team_id,
        "authorities": re.findall(r"^Authority=(.+)$", details, re.MULTILINE),
        "strictSignatureVerified": True,
    }
