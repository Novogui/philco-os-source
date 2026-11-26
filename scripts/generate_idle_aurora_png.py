"""Create the static transparent aurora overlay for the KitKat idle saver."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "source/philco-home/res/drawable-nodpi/idle_aurora_glow.png"
OUTPUT_SIZE = (1280, 720)
SCALE = 2
SIZE = (OUTPUT_SIZE[0] * SCALE, OUTPUT_SIZE[1] * SCALE)

if OUT.exists():
    raise SystemExit(f"Refusing to overwrite existing image: {OUT}")

def curve(a, b, c, d, steps=3500):
    pts = []
    for i in range(steps + 1):
        t = i / steps
        q = 1.0 - t
        x = q**3*a[0] + 3*q*q*t*b[0] + 3*q*t*t*c[0] + t**3*d[0]
        y = q**3*a[1] + 3*q*q*t*b[1] + 3*q*t*t*c[1] + t**3*d[1]
        pts.append((round(x * SIZE[0]), round(y * SIZE[1])))
    return pts

def join_curves(*segments):
    points = []
    for segment in segments:
        points.extend(curve(*segment)[1:] if points else curve(*segment))
    return points

upper = join_curves(
    ((0,.56),(.20,.76),(.37,.91),(.56,.82)),
    ((.56,.82),(.73,.73),(.84,.63),(1,.72)),
)
lower = join_curves(
    ((0,.76),(.23,.84),(.39,.99),(.58,.93)),
    ((.58,.93),(.76,.87),(.86,.80),(1,.88)),
)

def stroke(points, rgba, width):
    layer = Image.new("RGBA", SIZE, (0, 0, 0, 0))
    ImageDraw.Draw(layer).line(points, fill=rgba, width=width, joint="curve")
    return layer

base = Image.new("RGBA", SIZE, (0, 0, 0, 0))
# Broad amber aura, tighter gold bloom, then a crisp warm-white filament.
for layer in (
    stroke(upper, (255, 166, 24, 110), 32*SCALE).filter(ImageFilter.GaussianBlur(20*SCALE)),
    stroke(upper, (255, 188, 44, 170), 16*SCALE).filter(ImageFilter.GaussianBlur(8*SCALE)),
    stroke(upper, (255, 224, 112, 220), 5*SCALE).filter(ImageFilter.GaussianBlur(2*SCALE)),
    stroke(lower, (255, 194, 64, 68), 22*SCALE).filter(ImageFilter.GaussianBlur(15*SCALE)),
    stroke(lower, (255, 222, 126, 145), 4*SCALE).filter(ImageFilter.GaussianBlur(3*SCALE)),
    stroke(upper, (255, 232, 158, 240), 2*SCALE),
    stroke(lower, (255, 230, 160, 205), 1*SCALE),
):
    base = Image.alpha_composite(base, layer)

base = base.resize(OUTPUT_SIZE, Image.Resampling.LANCZOS)
OUT.parent.mkdir(parents=True, exist_ok=True)
base.save(OUT, format="PNG", optimize=True)
print(f"{OUT.relative_to(ROOT)} {OUT.stat().st_size} bytes {OUTPUT_SIZE[0]}x{OUTPUT_SIZE[1]} RGBA")
