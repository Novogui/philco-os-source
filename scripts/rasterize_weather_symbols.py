"""Rasterize preserved Google Material Symbols Rounded weather SVGs to PNG."""
from pathlib import Path
import re
import subprocess
import time
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SVG_DIR = ROOT / "docs/assets/material-weather-reference"
OUT_DIR = ROOT / "source/philco-home/res/drawable-nodpi"
TMP_DIR = ROOT / ".toolchain/tmp/philco-weather-raster"
EDGE = Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe")
if not EDGE.exists():
    EDGE = Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe")
NAMES = ("sunny", "partly_cloudy_day", "cloud", "rainy",
         "thunderstorm", "weather_snowy", "foggy")
OUTPUT_NAMES = {"weather_snowy": "weather_snowy", "cloud": "weather_cloud"}
SIZE = 96

if not EDGE.exists():
    raise SystemExit("Microsoft Edge headless is required; no files were changed")
TMP_DIR.mkdir(parents=True, exist_ok=True)
for name in NAMES:
    source = SVG_DIR / f"{name}.svg"
    output = OUT_DIR / f"{OUTPUT_NAMES.get(name, 'weather_' + name)}.png"
    if not source.is_file():
        raise SystemExit(f"Missing preserved source SVG: {source}")
    if output.exists():
        print(f"Preserving existing PNG (not overwritten): {output.relative_to(ROOT)}")
        continue
    svg = source.read_text(encoding="utf-8-sig")
    svg = re.sub(r"<path(?![^>]*\bfill=)([^>]*)>",
                 r'<path fill="#FFFFFF"\1>', svg)
    svg = svg.replace('width="48"', f'width="{SIZE}"')
    svg = svg.replace('height="48"', f'height="{SIZE}"')
    html = (f'<html><body style="margin:0;width:{SIZE}px;height:{SIZE}px;'
            f'background:#00ff00">{svg}</body></html>')
    token = str(time.time_ns())
    html_path = TMP_DIR / f"{name}-{token}.html"
    raw_path = TMP_DIR / f"{name}-{token}.raw.png"
    html_path.write_text(html, encoding="utf-8")
    subprocess.run([str(EDGE), "--headless", "--disable-gpu", "--hide-scrollbars",
                    "--no-first-run", f"--window-size={SIZE},{SIZE}",
                    f"--screenshot={raw_path}", html_path.resolve().as_uri()],
                   check=True, stdout=subprocess.DEVNULL,
                   stderr=subprocess.DEVNULL, timeout=20)
    for _ in range(200):
        if raw_path.is_file() and raw_path.stat().st_size > 128:
            break
        time.sleep(.1)
    if not raw_path.is_file():
        raise SystemExit(f"Rasterization failed: {name}")
    src = Image.open(raw_path).convert("RGB")
    if src.size != (SIZE, SIZE):
        raise SystemExit(f"Unexpected raster size for {name}: {src.size}")
    dst = Image.new("RGBA", src.size, (255, 255, 255, 0))
    sp, dp = src.load(), dst.load()
    for y in range(SIZE):
        for x in range(SIZE):
            dp[x, y] = (255, 255, 255, sp[x, y][2])
    dst.save(output, format="PNG", optimize=True)
    print(f"{output.relative_to(ROOT)} {output.stat().st_size} bytes {SIZE}x{SIZE}")
