# PHILCOOS ULTRA 777 — ARCHITECTURAL & DESIGN DECISIONS LOG

This document records architectural, performance, and design decisions agreed upon by the engineering team (Antigravity & Codex).

---

## DEC-001: Visual Design Framework — Glass & Aurora 2.0 on Android KitKat
- **Date:** 2026-11-21
- **Status:** APPROVED
- **Context:** The target TV runs Android 4.4.2 (API 19) with a PowerVR SGX544MP GPU and 512 MB physical RAM. Modern real-time blur shaders (`RenderScript`, multi-pass Gaussian blur) and VectorDrawable XML resources cause severe frame drops and Dalvik memory allocation overhead.
- **Decision:**
  1. Achieve the "Glassmorphic" look strictly through zero-cost static techniques: semi-transparent XML shape layers (`<layer-list>`), linear alpha gradient ramps, and 1.5dp frosted rim borders.
  2. Base card surfaces use `#14FFFFFF` (8% translucent white), borders use `#29FFFFFF` (16% translucent white), and focused surfaces use `#38FFFFFF` (22% white).
  3. High-contrast selection indicator uses an Electric Cyan border (`#00E5FF`) at 1.045x scale factor, respecting TV overscan safety zones (64dp horizontal margins).
  4. Bitmaps for photo wallpapers are strictly decoded as `RGB_565` (1.84 MB per 1280×720 image instead of 3.68 MB in ARGB_8888) with immediate recycling of unneeded bitmaps.
  5. UI canvas resolution is locked at 1280×720 progressive.
- **Consequences:** Near-zero GPU composition cost, zero GC thrashing during D-pad navigation, high legibility from 3 meters viewing distance.

---

## DEC-002: Iconography Standard — Material Design Symbols Extended
- **Date:** 2026-11-21
- **Status:** APPROVED
- **Context:** PhilcoOS requires clean, recognizable iconography for TV navigation (Live TV, HDMI, AV, Apps, Settings, Quick Controls, Weather).
- **Decision:**
  1. Standardize on Google Material Design Symbols Extended (Apache License 2.0).
  2. Implement icons via procedural path reuse (cached `Path` objects in `TvGlyphView`), optimized 9-patch assets, and vector-compatible color palettes without adding third-party heavy dependencies.
  3. Map symbols:
     - Live TV: `live_tv` / `tv`
     - Inputs: `settings_input_hdmi` (HDMI 1 & 2), `cable` / `settings_input_component` (AV RCA)
     - Apps Drawer: `apps` / `grid_view`
     - Quick Controls: `volume_up`, `volume_off` (mute), `brightness_medium` (picture brightness), `light_mode` (backlight), `tune` (system settings)
     - Weather: `wb_sunny` (clear), `partly_cloudy_day`, `cloud` (neutral fallback), `rainy` / `water_drop`.
- **Consequences:** Consistent, premium visual identity with zero APK bloat and full licensing compliance.

---

## DEC-003: Moonlight Android Client Selection for Game Streaming
- **Date:** 2026-11-21
- **Status:** APPROVED
- **Context:** Moonlight Android dropped support for API 19 (KitKat) in commit `17df152` (May 2023) by setting `minSdk 21`. Newer releases (v11.x, v12.x) fail to parse or install on Android 4.4.2.
- **Decision:**
  1. Target **Moonlight Android v10.11** (or v10.9) as the official compatible candidate (`armeabi-v7a`, `minSdkVersion 19`).
  2. Stream from Sunshine host using the GameStream protocol (RTSP + ENet) with conservative parameters: 1280×720 @ 30fps, H.264 Baseline/Main profile, 5–10 Mbps bitrate.
  3. Avoid HEVC streaming on the TV to minimize VPU decoding latency and buffer underruns.
- **Consequences:** Enables low-latency PC game streaming to the Philco TV without requiring operating system upgrades.

---

## DEC-004: SmartTube 32.80 First-Frame Latency Mitigation Strategy
- **Date:** 2026-11-21
- **Status:** APPROVED
- **Context:** SmartTube 32.80 experiences a 1.5–2 minute delay before visible playback on cold start due to BotGuard JavaScript execution in the legacy KitKat WebView (`PoTokenWebView4`) and subsequent ExoPlayer `dropOutputBuffer` cascading discards.
- **Decision:**
  1. Maintain SmartTube 32.80 Stable (`armeabi-v7a`) as the verified installed baseline.
  2. Do not attempt blind updates to newer experimental builds which may break KitKat WebView compatibility.
  3. Document the root cause: CPU saturation on the Cortex-A7 during BotGuard challenge execution causes video PTS to lag behind the audio clock, triggering `shouldDropOutputBuffer`.
  4. Ensure warm session reuse where possible to prevent re-triggering the 60-second BotGuard attestation penalty.
- **Consequences:** Realistic user expectations set; avoids bricking video playback with incompatible APK updates.

---

## DEC-005: Video Validation Protocol (AVC 720p30 Baseline)
- **Date:** 2026-11-21
- **Status:** APPROVED
- **Context:** Claims of 720p30 video capability must be grounded in physical measurement, not theoretical chipset specs.
- **Decision:**
  1. Stage 1: Local AVC 720p30 MP4 playback benchmark via MediaCodec / Stagefright.
  2. Stage 2: Measure actual rendered FPS and ensure `droppedFrames` rate is below 1%.
  3. Stage 3: Only after local playback passes, test and validate 720p stream profiles in SmartTube (itag 136).
- **Consequences:** Prevents phantom regressions and guarantees reliable multimedia playback.
