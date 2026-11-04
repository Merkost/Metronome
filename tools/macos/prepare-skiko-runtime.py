import argparse
import hashlib
import json
from pathlib import Path
import tempfile
import zipfile

from release_support import ValidationError, require, sha256


ARM_LIBRARY = "libskiko-macos-arm64.dylib"
INTEL_LIBRARY = "libskiko-macos-x64.dylib"


def prepare_runtime(source, destination, receipt):
    require(source.resolve() != destination.resolve(), "Skiko preparation must not modify the dependency cache")
    require(source.name.startswith("skiko-awt-runtime-macos-arm64-") and source.suffix == ".jar", "Pass the resolved macOS arm64 Skiko runtime JAR")
    require(destination.name == source.name, "Preserve the Skiko artifact name so Compose extracts its renderer")
    with zipfile.ZipFile(source) as archive:
        require(archive.testzip() is None, "The source Skiko archive is corrupt")
        names = archive.namelist()
        require(len(names) == len(set(names)), "The source Skiko archive has duplicate entries")
        require(ARM_LIBRARY in names and ARM_LIBRARY + ".sha256" in names, "The source Skiko archive is missing its arm64 renderer or checksum")
        libraries = {name for name in names if name.lower().endswith((".dylib", ".jnilib", ".so", ".dll"))}
        require(libraries.issubset({ARM_LIBRARY, INTEL_LIBRARY}), "The source Skiko archive contains an unreviewed native library")
        require(not any(name.upper().startswith("META-INF/") and name.upper().endswith((".SF", ".RSA", ".DSA", ".EC")) for name in names), "A signed Skiko JAR needs explicit signature handling before modification")
        arm_digest = hashlib.sha256(archive.read(ARM_LIBRARY)).hexdigest()
        require(archive.read(ARM_LIBRARY + ".sha256").decode("ascii").strip().lower() == arm_digest, "The Skiko arm64 renderer does not match its published checksum")
        removed = sorted(set(names).intersection({INTEL_LIBRARY, INTEL_LIBRARY + ".sha256"}))
        destination.parent.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(prefix="skiko-arm64-", dir=destination.parent) as temporary:
            staged = Path(temporary) / source.name
            with zipfile.ZipFile(staged, "w") as output:
                for entry in archive.infolist():
                    if entry.filename not in removed:
                        output.writestr(entry, archive.read(entry))
            staged.replace(destination)
    result = {
        "source": str(source.resolve()),
        "sourceSha256": sha256(source),
        "prepared": str(destination.resolve()),
        "preparedSha256": sha256(destination),
        "retainedRenderer": ARM_LIBRARY,
        "retainedRendererSha256": arm_digest,
        "removedEntries": removed,
        "stage": "Dependency preparation before Compose renderer extraction and code signing",
    }
    receipt.parent.mkdir(parents=True, exist_ok=True)
    receipt.write_text(json.dumps(result, indent=2) + "\n")
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--destination", type=Path, required=True)
    parser.add_argument("--receipt", type=Path, required=True)
    args = parser.parse_args()
    try:
        result = prepare_runtime(args.source, args.destination, args.receipt)
        print(json.dumps(result, indent=2))
    except (ValidationError, OSError, ValueError, UnicodeError, zipfile.BadZipFile) as error:
        parser.exit(2, f"Skiko runtime preparation failed: {error}\n")


if __name__ == "__main__":
    main()
