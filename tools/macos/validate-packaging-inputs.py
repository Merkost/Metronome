import argparse
import json
from pathlib import Path
import platform
import plistlib
import shutil
import sys

from release_support import (
    APP_ID,
    RUNTIME_ID,
    TEAM_ID,
    ValidationError,
    check_entitlements,
    check_profile,
    check_runtime_properties,
    read_profile,
    read_properties,
    require,
    signing_identity_hashes,
)


def validate(args):
    require(sys.platform == "darwin", "Mac packaging must run on macOS")
    require(platform.machine() == "arm64", "Build this Apple silicon release from an arm64 shell and JVM")
    root = args.root.resolve()
    require(not args.testflight or args.app_store, "TestFlight provisioning requires --app-store")
    home = args.jdk_home.expanduser().resolve()
    require((home / "release").is_file(), f"JDK home is invalid: {home}")
    runtime = check_runtime_properties(read_properties(home / "release"))
    for executable in ["java", "jpackage", "jlink"]:
        require((home / "bin" / executable).is_file(), f"Selected JetBrains Runtime has no {executable} tool")
    packaging = root / "desktopApp/packaging"
    require((packaging / "Metronome.icns").is_file(), "Run python3 tools/macos/generate-icon.py before packaging")
    suffix = "-testflight" if args.testflight else ""
    for filename, identifier, user_files in [
        (f"app{suffix}.entitlements.plist", APP_ID, True),
        (f"runtime{suffix}.entitlements.plist", RUNTIME_ID, False),
    ]:
        check_entitlements(plistlib.loads((packaging / filename).read_bytes()), identifier, user_selected_files=user_files, provisioned=args.testflight)
    result = {"runtime": runtime, "appStore": args.app_store, "testFlight": args.testflight, "teamIdentifier": TEAM_ID if args.app_store else None}
    if args.app_store:
        identities = signing_identity_hashes(args.identity)
        result["signingIdentityHashes"] = identities
    if args.testflight:
        require(args.app_profile, "Set macos.appProvisioningProfile or METRONOME_MAC_APP_PROFILE to the Mac App Store profile for com.merkost.metronome")
        require(args.runtime_profile, "Set macos.runtimeProvisioningProfile or METRONOME_MAC_RUNTIME_PROFILE to the Mac App Store profile for com.oracle.java.com.merkost.metronome")
        profiles = [
            check_profile(read_profile(args.app_profile), APP_ID, certificate_sha1=identities["application"]),
            check_profile(read_profile(args.runtime_profile), RUNTIME_ID, certificate_sha1=identities["application"]),
        ]
        result["profiles"] = profiles
        require(args.stage_directory is not None, "A build staging directory is required for signing profiles")
        stage = args.stage_directory.resolve()
        try:
            stage.relative_to(root / "desktopApp/build")
        except ValueError:
            raise ValidationError("Signing profiles may only be staged inside desktopApp/build")
        stage.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(Path(args.app_profile).expanduser(), stage / "embedded.provisionprofile")
        shutil.copyfile(Path(args.runtime_profile).expanduser(), stage / "runtime.provisionprofile")
        (stage / "validated-inputs.json").write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result, indent=2))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--jdk-home", type=Path, required=True)
    parser.add_argument("--app-store", action="store_true")
    parser.add_argument("--testflight", action="store_true")
    parser.add_argument("--identity", default="Konstantin Merenkov (P47X2292CM)")
    parser.add_argument("--app-profile")
    parser.add_argument("--runtime-profile")
    parser.add_argument("--stage-directory", type=Path)
    args = parser.parse_args()
    try:
        validate(args)
    except (ValidationError, OSError, plistlib.InvalidFileException) as error:
        parser.exit(2, f"Mac packaging input check failed: {error}\n")


if __name__ == "__main__":
    main()
