"""Rasterize the checked-in Google Material Icons SVGs to transparent PNGs."""
from pathlib import Path
import subprocess
import time
import re
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SVG_DIR = ROOT / "docs/assets/material-icons-reference"
OUT_DIR = ROOT / "source/philco-home/res/drawable-nodpi"
TMP_DIR = ROOT / ".toolchain/tmp/philco-icon-raster"
EDGE = Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe")
if not EDGE.exists():
    EDGE = Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe")
NAMES = {
    "signal_wifi_1_bar": "philco_wifi_1",
    "signal_wifi_2_bar": "philco_wifi_2",
    "signal_wifi_3_bar": "philco_wifi_3",
    "signal_wifi_4_bar": "philco_wifi_4",
    "signal_wifi_off": "philco_wifi_off",
    "settings_input_hdmi": "philco_hdmi",
}

if not EDGE.exists():
    raise SystemExit("Microsoft Edge headless was not found; no files were changed")
OUT_DIR.mkdir(parents=True, exist_ok=True)
TMP_DIR.mkdir(parents=True, exist_ok=True)
for name, output_name in NAMES.items():
    source = SVG_DIR / f"{name}.svg"
    output = OUT_DIR / f"{output_name}.png"
    if not source.is_file():
        raise SystemExit(f"Missing preserved source SVG: {source}")
    if output.exists():
        print(f"preserved existing PNG (not overwritten): {output.relative_to(ROOT)}")
        continue
    svg = source.read_text(encoding="utf-8-sig")
    svg = svg.replace('width="24"', 'width="96"').replace('height="24"', 'height="96"')
    # Keep explicit transparent paths and original fill-opacity=.3; set only
    # default-filled Material paths to white as requested.
    svg = re.sub(r'<path(?![^>]*\bfill=)([^>]*)>',
                 r'<path fill="#FFFFFF"\1>', svg)
    html = ('<html><body style="margin:0;width:96px;height:96px;background:#00ff00">'
            + svg + '</body></html>')
    token = f"{time.time_ns()}"
    html_path = TMP_DIR / f"{name}-{token}.html"
    raw_path = TMP_DIR / f"{name}-{token}.raw.png"
    html_path.write_text(html, encoding="utf-8")
    subprocess.run([
        str(EDGE), "--headless", "--disable-gpu", "--hide-scrollbars",
        "--no-first-run", "--window-size=96,96", f"--screenshot={raw_path}",
        html_path.resolve().as_uri(),
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=20)
    for _ in range(200):
        if raw_path.is_file() and raw_path.stat().st_size > 128:
            break
        time.sleep(0.1)
    if not raw_path.is_file():
        raise SystemExit(f"Rasterization failed: {name}")
    src = Image.open(raw_path).convert("RGB")
    dst = Image.new("RGBA", src.size, (255, 255, 255, 0))
    src_px, dst_px = src.load(), dst.load()
    for y in range(src.height):
        for x in range(src.width):
            # The temporary green backdrop is removed. Blue encodes coverage
            # for white antialiased SVG pixels, preserving .3-opacity shapes.
            dst_px[x, y] = (255, 255, 255, src_px[x, y][2])
    dst.save(output, format="PNG", optimize=True)
    print(f"{output.relative_to(ROOT)} {output.stat().st_size} bytes {dst.size[0]}x{dst.size[1]}")
