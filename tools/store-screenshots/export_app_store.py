from pathlib import Path

from PIL import Image, ImageOps


ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "screenshots/source/figma-1320x2868"
FINAL = ROOT / "screenshots/final"
SIZES = {
    "6.5-1242x2688": (1242, 2688),
    "6.7-1284x2778": (1284, 2778),
}


def main():
    sources = sorted(SOURCE.glob("*.png"))
    if len(sources) != 8:
        raise ValueError(f"Expected 8 Figma source PNGs, found {len(sources)}")

    for source in sources:
        with Image.open(source) as image:
            if image.size != (1320, 2868):
                raise ValueError(f"Unexpected source size for {source.name}: {image.size}")
            image = image.convert("RGB")
            for folder, size in SIZES.items():
                destination = FINAL / folder / source.name
                destination.parent.mkdir(parents=True, exist_ok=True)
                ImageOps.fit(image, size, method=Image.Resampling.LANCZOS).save(
                    destination, "PNG", optimize=True
                )
                print(f"{destination.relative_to(ROOT)}: {size[0]} × {size[1]}")


if __name__ == "__main__":
    main()
