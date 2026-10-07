from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "artifacts/aso-screenshots/final/feature-graphic-v2.png"
SCALE = 3
FONT = "/System/Library/Fonts/SFNS.ttf"


def font(size, weight):
    face = ImageFont.truetype(FONT, size * SCALE)
    face.set_variation_by_name(weight)
    return face


def point(x, y):
    return (round(x * SCALE), round(y * SCALE))


def rect(box):
    return tuple(round(value * SCALE) for value in box)


def main():
    canvas = Image.new("RGB", point(1024, 500), "#C9D5FE")
    draw = ImageDraw.Draw(canvas)

    draw.ellipse(rect((-168, 289, 228, 685)), fill="#B89FFF")
    draw.ellipse(rect((852, -177, 1210, 181)), fill="#FFCAEA")
    draw.rounded_rectangle(rect((586, 71, 944, 430)), radius=48 * SCALE, fill="#F8F6FF")

    draw.text(point(112, 95), "METRONOME", font=font(20, "Semibold"), fill="#24222B", spacing=0)
    draw.text(point(108, 150), "Precise timing.", font=font(58, "Bold"), fill="#17151C")
    draw.text(point(108, 216), "Better practice.", font=font(58, "Bold"), fill="#17151C")
    draw.rounded_rectangle(rect((112, 320, 448, 377)), radius=28 * SCALE, fill="#9EFFAE")
    draw.text(point(135, 338), "TEMPO  ·  RHYTHM  ·  PRACTICE", font=font(15, "Bold"), fill="#17151C")

    draw.text(point(630, 104), "4 / 4", font=font(21, "Semibold"), fill="#292631")
    for x, color, size in [(651, "#17151C", 48), (732, "#D8D8D8", 34), (809, "#D8D8D8", 34), (886, "#D8D8D8", 34)]:
        y = 180
        draw.ellipse(rect((x - size / 2, y - size / 2, x + size / 2, y + size / 2)), fill=color)
    draw.ellipse(rect((620, 149, 682, 211)), outline="#17151C", width=5 * SCALE)
    draw.text(point(650, 249), "100", font=font(76, "Bold"), fill="#17151C")
    draw.text(point(821, 291), "BPM", font=font(20, "Semibold"), fill="#5D5864")
    draw.rounded_rectangle(rect((628, 348, 900, 360)), radius=6 * SCALE, fill="#D8D8D8")
    draw.rounded_rectangle(rect((628, 348, 765, 360)), radius=6 * SCALE, fill="#17151C")
    draw.ellipse(rect((752, 338, 774, 370)), fill="#17151C")

    canvas.resize((1024, 500), Image.Resampling.LANCZOS).save(OUTPUT, "PNG", optimize=True)
    print(OUTPUT)


if __name__ == "__main__":
    main()
