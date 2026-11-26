# PhilcoOS Ultra 777 — Product, UX/UI & Art Direction Specification
## Glass & Aurora 2.0 Design Baseline

**Operating Environment:** PhilcoOS Ultra 777  
**Platform Target:** Philco PH42F10DSGW (CVTE TP.RT2984T.PB801 / Realtek RTD2984TD-GR / Android 4.4.2 KitKat API 19)  
**Primary UI Canvas:** 1280 × 720 (Progressive Full HD Scaled)  
**Document:** `C:\PhilcoOS\docs\PHILCOOS_ULTRA_777_DESIGN.md`  
**Date:** 2026-11-21  
**Status:** Approved Glass & Aurora 2.0 Design Baseline  
**Editable SVG Frames:** Located at `docs/design/frames_1280x720/`

---

## 1. Project Vision & Experience Principles

The goal of **PhilcoOS Ultra 777** is to transform a resource-constrained legacy TV into a bespoke, commercial-grade Smart TV platform. The core design thesis is:

> **"Premium visual quality with uncompromising efficiency."**

Rather than treating the TV as an oversized mobile phone or deploying heavy third-party web frameworks, PhilcoOS Ultra 777 delivers an original, cohesive aesthetic inspired by the finest ergonomic paradigms of Apple tvOS, Google TV, and LG webOS, engineered strictly within the constraints of Android 4.4 KitKat and a 512 MB physical RAM envelope.

---

## 2. Art Direction & Visual Identity (Glass & Aurora 2.0)

### 2.1 Color System & Mood
The color palette evokes a calm, cinematic evening atmosphere with layered visual depth:
* **Midnight Navy Canvas (`#060B14` to `#0B1220`):** Provides deep contrast and reduces backlight bleed on the 42" LED panel.
* **Atmospheric Electric Blue (`#1A73E8` / `#00E5FF`):** Used for active focus rings, selection halos, and live status beacons.
* **Warm Sunset / Twilight Violet Accents (`#7B1FA2` to `#FF5722`):** Soft, non-distracting gradient highlights in the ambient background.
* **Glassmorphic Translucency:**
  - Base Card: `rgba(255, 255, 255, 0.08)` (`#14FFFFFF`)
  - Card Stroke / Border: `rgba(255, 255, 255, 0.16)` (`#29FFFFFF`)
  - Focused Card Surface: `rgba(255, 255, 255, 0.22)` (`#38FFFFFF`)
  - Focused Card Glow / Rim: `#00E5FF` (Electric Cyan, 2.5dp stroke)
  - Weather Card Surface (Dark Glass): `#CC0B1220` (80% opacity dark glass to guarantee contrast over light photo wallpapers).

### 2.2 Zero-Cost Glassmorphic Materials on Android 4.4
The PowerVR SGX544MP GPU and 512 MB DDR3 RAM cannot afford real-time multi-pass Gaussian blur shaders (`RenderScript` / `glBlitFramebuffer`). 
PhilcoOS Ultra 777 achieves the impression of frosted glass through **zero-cost static Android drawables**:
1. Layered semi-transparent XML shapes (`<layer-list>`).
2. Soft inner and outer gradient highlights (`<gradient>` with subtle alpha ramps).
3. 1.5dp frosted rim borders that simulate light refraction on glass edges.
4. Corner radii (`18dp` to `20dp`) creating smooth, modern pebble shapes.
5. Bitmaps decoded exclusively as `RGB_565` (1.84 MB per 1280×720 image instead of 3.68 MB in ARGB_8888).

### 2.3 Typography & 10-Foot UI Legibility
Designed for viewing from a 2.5 to 4.0-meter distance:
* **Digital Clock:** 90sp Roboto Medium (`sans-serif-medium`), high-contrast white `#FFFFFF` (optimized from 78sp in EB256).
* **Date Header:** 18sp Roboto Regular (`sans-serif`), subtle twilight grey `#B0BEC5`.
* **Dock Item Labels:** 14sp Roboto Medium (`sans-serif-medium`), crisp `#E0E0E0` on dark glass.
* **System Indicators:** 12sp Roboto Regular, muted cyan `#80DEEA`.

---

## 3. Composition & Spatial Hierarchy (1280 × 720)

```
+----------------------------------------------------------------------------------------------------+
|  [05:45]                                                          [Wi-Fi: Conectado] [Philco 777]  |
|  Sexta-feira, 21 de Novembro                                                  HDMI 2 • 1080p @ 60Hz|
|                                                                                                    |
|  +---------------------------+                                                                     |
|  | [Sun/Cloud] 21°           |                 (CINEMATIC BREATHING ROOM)                          |
|  | Parcialmente nublado      |                                                                     |
|  | Joinville, SC             |                                                                     |
|  +---------------------------+                                                                     |
|                                                                                                    |
|        +----+  +----+  +----+  +----+  +---------+  +----+  +----+  +----+                         |
|        | TV |  |CABO|  | AV |  |HDMI|  | HDMI 2  |  | YT |  |APPS|  |SET |                         |
|        | 27 |  | 1  |  | 5  |  | 18 |  | 17(FOC) |  |Tube|  |    |  |    |                         |
|        +----+  +----+  +----+  +----+  +---------+  +----+  +----+  +----+                         |
|                                        (FOCUSED: SCALE 1.045x + CYAN GLOW)                         |
+----------------------------------------------------------------------------------------------------+
```

### 3.1 Spatial Breakdown
1. **Upper Information Area (Top 140dp):**
   - **Left:** Large digital clock (`90sp`) and Brazilian Portuguese date display (`Sexta-feira, 21 de Novembro`). Updates via `TIME_TICK` broadcasts with **zero polling overhead**.
   - **Right:** System telemetry indicators: Wi-Fi signal, Ethernet status, and PhilcoOS 777 emblem.
   - **Weather Card:** 104dp height, X=64dp, Y=180dp. Dark Glass background (`#CC0B1220`) preventing contrast issues over light wallpapers. Weather glyph: 54dp.
2. **Center Cinematic Breathing Room (Middle 240dp):**
   - Negative space showcasing the atmospheric background / user photo wallpaper without clutter.
3. **Primary Application Dock (Bottom 230dp):**
   - Horizontally centered row housing 8 primary shortcut tiles (124dp × 134dp, 20dp gap, 64dp lateral safe margins):
     1. **Live TV (`AR=27`):** Direct launch into ISDB-Tb tuner (`com.tsb.tv/.Tv_strategy`).
     2. **Cabo (`Cable=1`):** Direct hardware cable input selection (`TvManager.setSource(1)`).
     3. **AV Composto (`AV=5`):** Composite RCA input (`TvManager.setSource(5)`).
     4. **HDMI 1 (`HDMI1=18`):** Direct hardware input switch (`TvManager.setSource(18)`).
     5. **HDMI 2 (`HDMI2=17`):** Active default input (`TvManager.setSource(17)`).
     6. **SmartTube (`org.smarttube.stable`):** YouTube TV client launch.
     7. **Application Library:** Opens full D-pad navigable app drawer.
     8. **TV Settings (`com.android.settings`):** Native Android/Realtek settings.

---

## 4. Remote Control Interaction & Focus Engine

The physical IR remote control is the sole input mechanism.

### 4.1 Directional Logic Matrix & Anti-Trap Guarantees
* **Horizontal D-Pad (LEFT / RIGHT):**
  - Smoothly navigates between dock items with explicit XML bindings:
    `tile_tv` ◄► `tile_cable` ◄► `tile_av` ◄► `tile_hdmi1` ◄► `tile_hdmi2` ◄► `tile_smarttube` ◄► `tile_apps` ◄► `tile_settings`.
  - Boundary stops: `nextFocusLeft` on Tile 1 points to `tile_tv`; `nextFocusRight` on Tile 8 points to `tile_settings` (prevents focus loss).
* **Focus Scaling Factor:**
  - Scaled strictly at **1.045x** (optimized from the earlier 1.12x to prevent clipping on TVs with active overscan).
  - Anel de seleção em Ciano Elétrico `#00E5FF` (2.5dp) com halo translúcido.
* **Action (ENTER / OK):**
  - Tactile compression animation (`1.045x -> 1.02x` for 90ms) before intent execution.
* **Menu (MENU):**
  - Toggles the **Quick Control Center** modal overlay from anywhere.

---

## 5. Subsystems & Overlays

### 5.1 Quick Control Center Overlay
* Lightweight modal sheet sliding in from the right edge (width: 420px, height: 720px).
* Surface: `#E60B1220` with 1.5dp frosted border on left edge.
* Direct Realtek hardware controls:
  1. Master TV Volume slider (0–100 via `AudioManager.setStreamVolumeForL4300`).
  2. Mute toggle button (`TvManager.getMute()` / `TvManager.setMute()`).
  3. Image Brightness slider (0–100 via `TvManager.getBrightness()` / `setBrightness()`).
  4. Panel Backlight slider (0–100 via `TvManager.getBacklight()` / `setBacklight()`).
  5. Native Android TV Settings button (`com.android.settings/.Settings`).

### 5.2 Iconography (Material Design Symbols Extended)
All icons are sourced from the Google Material Design Symbols Extended library under Apache License 2.0, rendered via procedural Path drawing in `TvGlyphView.java` to guarantee zero allocations in `onDraw()`:
- `live_tv` / `tv`
- `settings_input_hdmi`
- `cable` / `settings_input_component`
- `apps` / `grid_view`
- `settings` / `tune`
- `volume_up`, `volume_mute`, `volume_off`
- `brightness_medium`, `light_mode`
- `wb_sunny`, `partly_cloudy_day`, `cloud`, `rainy`

---

## 6. Deliverable Vector Assets (1280 × 720)

Editable SVG vector specifications are stored in:
- `docs/design/frames_1280x720/home_1280x720.svg`
- `docs/design/frames_1280x720/quick_control_center_1280x720.svg`
- `docs/design/frames_1280x720/clima_widget_1280x720.svg`
- `docs/design/frames_1280x720/dock_1280x720.svg`
- `docs/design/frames_1280x720/focus_states_dpad_1280x720.svg`
