import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import tempfile


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    args = parser.parse_args()
    source = args.root / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app_icon_1024.png"
    output = args.root / "desktopApp/packaging/Metronome.icns"
    if not source.is_file():
        parser.error("The existing 1024px Metronome app icon is missing")
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="metronome-icon-") as temporary:
        iconset = Path(temporary) / "Metronome.iconset"
        iconset.mkdir()
        for size in [16, 32, 128, 256, 512]:
            for scale in [1, 2]:
                suffix = "@2x" if scale == 2 else ""
                destination = iconset / f"icon_{size}x{size}{suffix}.png"
                subprocess.run(
                    ["/usr/bin/sips", "-z", str(size * scale), str(size * scale), str(source), "--out", str(destination)],
                    check=True,
                    stdout=subprocess.DEVNULL,
                )
        subprocess.run(["/usr/bin/iconutil", "-c", "icns", str(iconset), "-o", str(output)], check=True)
    manifest = {
        "source": str(source.relative_to(args.root)),
        "sourceSha256": hashlib.sha256(source.read_bytes()).hexdigest(),
        "output": str(output.relative_to(args.root)),
        "sha256": hashlib.sha256(output.read_bytes()).hexdigest(),
        "method": "Existing app icon resized with Apple sips and assembled with Apple iconutil",
        "artworkChanged": False,
    }
    (output.parent / "icon-provenance.json").write_text(json.dumps(manifest, indent=2) + "\n")
    print(f"Created {output}")


if __name__ == "__main__":
    main()
