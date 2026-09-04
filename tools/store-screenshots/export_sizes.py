import os
import sys
from PIL import Image

# App Store Connect portrait slots. ASC rejects anything off by a pixel.
SIZES = {
    "6.5-1242x2688": (1242, 2688),
    "6.7-1284x2778": (1284, 2778),
    "6.9-1290x2796": (1290, 2796),
}

# 4K master -> output basename
FRAMES = [
    ("01-trust-every-beat/clean2.jpg", "01-set-it-trust-it"),
    ("02-hold-silent-bars/clean1.jpg", "02-keep-time"),
    ("03-climb-80-to-120/clean1.jpg", "03-climb-80-120"),
    ("04-play-your-plan/clean1.jpg", "04-follow-your-plan"),
    ("05-start-no-ads/clean2.jpg", "05-no-ads"),
]

ROOT = "artifacts/aso-screenshots"


def export(master, out_path, tw, th):
    im = Image.open(master).convert("RGB")
    w, h = im.size
    # Crop to the exact target aspect first, anchored top-centre so the
    # headline never shifts, then resize. Never resize straight to target:
    # the ratios differ enough to stretch the type.
    crop_w = round(h * tw / th)
    if crop_w <= w:
        left = (w - crop_w) // 2
        im = im.crop((left, 0, left + crop_w, h))
    else:
        crop_h = round(w * th / tw)
        im = im.crop((0, 0, w, crop_h))
    im = im.resize((tw, th), Image.LANCZOS)
    im.save(out_path, "JPEG", quality=95, subsampling=0)


if __name__ == "__main__":
    for slot, (tw, th) in SIZES.items():
        outdir = os.path.join(ROOT, "final", slot)
        os.makedirs(outdir, exist_ok=True)
        for master, name in FRAMES:
            src = os.path.join(ROOT, master)
            dst = os.path.join(outdir, f"{name}.jpg")
            export(src, dst, tw, th)
        print(f"✓ {slot}: {len(FRAMES)} frames")
