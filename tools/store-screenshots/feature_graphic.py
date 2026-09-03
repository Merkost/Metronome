import sys
from PIL import Image, ImageDraw, ImageFont

W, H = 1024, 500
BG = (0xF2, 0xEF, 0xEA)
FONT = "/System/Library/Fonts/SFNS.ttf"


def font(size, weight="Bold"):
    f = ImageFont.truetype(FONT, size)
    try:
        f.set_variation_by_name(weight)
    except Exception:
        pass
    return f


def build(hero_path, out_path):
    canvas = Image.new("RGB", (W, H), BG)

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
    x = 64
    f1, f2 = font(74), font(30, "Medium")
    draw.text((x, 178), "Set it. Trust it.", font=f1, fill=(0x14, 0x14, 0x14))
    draw.text((x, 272), "A metronome that never drifts.", font=f2, fill=(0x5A, 0x56, 0x52))
    draw.text((x, 316), "No ads. No account. Free.", font=f2, fill=(0x5A, 0x56, 0x52))

    canvas.save(out_path, "PNG")
    print(f"✓ {out_path} ({canvas.width}x{canvas.height})")


if __name__ == "__main__":
    build(sys.argv[1], sys.argv[2])
