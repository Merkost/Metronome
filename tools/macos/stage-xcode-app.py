import hashlib
import json
import os
from pathlib import Path
import plistlib
import shutil
import subprocess


def main():
    source = Path(os.environ["METRONOME_PREBUILT_APP"]).resolve()
    destination = (Path(os.environ["TARGET_BUILD_DIR"]) / os.environ["FULL_PRODUCT_NAME"]).resolve()
    if source == destination or source in destination.parents or destination in source.parents:
        raise ValueError("The archive staging location must be separate from the prepared app")
    if source.name != "Metronome.app" or destination.name != "Metronome.app":
        raise ValueError("Expected the Metronome application bundle")
    info = plistlib.loads((source / "Contents/Info.plist").read_bytes())
    expected = {
        "CFBundleIdentifier": os.environ["PRODUCT_BUNDLE_IDENTIFIER"],
        "CFBundleShortVersionString": os.environ["MARKETING_VERSION"],
        "CFBundleVersion": os.environ["CURRENT_PROJECT_VERSION"],
        "CFBundleExecutable": "Metronome",
    }
    for key, value in expected.items():
        if info.get(key) != value:
            raise ValueError(f"Prepared application {key} does not match the Xcode target")
    subprocess.run(["/usr/bin/codesign", "--verify", "--deep", "--strict", str(source)], check=True)
    contents = destination / "Contents"
    contents.mkdir(parents=True, exist_ok=True)
    hashes = {}
    for name in ("MacOS", "Resources", "app", "runtime"):
        origin = source / "Contents" / name
        target = contents / name
        if not origin.is_dir() or origin.is_symlink() or target.is_symlink():
            raise ValueError(f"Unexpected bundle directory: {name}")
        if target.exists():
            shutil.rmtree(target)
        shutil.copytree(origin, target, symlinks=True)
        for item in origin.rglob("*"):
            if item.is_file() and not item.is_symlink():
                relative = item.relative_to(source)
                digest = hashlib.sha256(item.read_bytes()).hexdigest()
                if hashlib.sha256((destination / relative).read_bytes()).hexdigest() != digest:
                    raise ValueError(f"Archive staging changed {relative}")
                hashes[str(relative)] = digest
    receipt = Path(os.environ["DERIVED_FILE_DIR"]) / "metronome-staging.json"
    receipt.parent.mkdir(parents=True, exist_ok=True)
    receipt.write_text(json.dumps({"source": str(source), "destination": str(destination), "files": hashes}, indent=2) + "\n")
    print(f"Staged {len(hashes)} verified files for Xcode archiving")


if __name__ == "__main__":
    main()
