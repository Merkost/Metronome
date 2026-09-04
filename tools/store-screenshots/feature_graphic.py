import sys
from PIL import Image, ImageDraw, ImageFont


BG = (0xF2, 0xEF, 0xEA)
FONT = "/System/Library/Fonts/SFNS.ttf"


def font(size, weight="Bold"):
    f = ImageFont.truetype(FONT, size)
    try:
        f.set_variation_by_name(weight)
    except Exception:
        pass
    return f


def build(hero_path, out_path, W=1024, H=500):
    canvas = Image.new("RGB", (W, H), BG)
    scale = H / 500.0

    hero = Image.open(hero_path).convert("RGB")
    hw, hh = hero.size
    # Crop the device out of the hero frame, below the headline.
    box = (int(hw * 0.17), int(hh * 0.26), int(hw * 0.83), hh)
    phone = hero.crop(box)
    # Scale so the device is taller than the canvas and bleeds off the bottom.
    target_h = int(H * 1.18)
    phone = phone.resize(
        (int(phone.width * target_h / phone.height), target_h), Image.LANCZOS
    )
    px = int(W * 0.66)
    canvas.paste(phone, (px, int(H * 0.16)))

    draw = ImageDraw.Draw(canvas)
    x = int(64 * scale)
    f1, f2 = font(int(74 * scale)), font(int(30 * scale), "Medium")
    draw.text((x, int(178 * scale)), "Set it. Trust it.", font=f1, fill=(0x14, 0x14, 0x14))
    draw.text((x, int(272 * scale)), "A metronome that never drifts.", font=f2, fill=(0x5A, 0x56, 0x52))
    draw.text((x, int(316 * scale)), "No ads. No account. Free.", font=f2, fill=(0x5A, 0x56, 0x52))

    canvas.save(out_path, "PNG")
    print(f"✓ {out_path} ({canvas.width}x{canvas.height})")


if __name__ == "__main__":
    w = int(sys.argv[3]) if len(sys.argv) > 3 else 1024
    h = int(sys.argv[4]) if len(sys.argv) > 4 else 500
    build(sys.argv[1], sys.argv[2], w, h)
