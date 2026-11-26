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

---

## DEC-006: Realtek Physical Source Switching and OEM TV Activity Handoff
- **Date:** 2026-11-21
- **Status:** DEVICE-VALIDATED FOR ALL FIVE SOURCE ROUTES; EXTERNAL VIDEO SIGNAL NOT VERIFIED
- **Context:** OEM bytecode has more than one source-switch path. `SourceMenu.checkSource` persists `PlayingSource` then launches explicit `com.tsb.tv/.Tv_strategy` with `FLAG_ACTIVITY_NEW_TASK`. `KeyPadService.setSource` calls `TvManager.setSource(id)`, persists `PlayingSource`, then broadcasts `com.realtek.TVSignalAlarm.SourceChange`; the OEM TV app also provides `TvManagerHelper.setInputSource` on a worker thread. There is no single shared sequence in all OEM paths. The launcher combines the observed source call, persistence and source-change broadcast, then launches the explicit OEM activity and verifies `getCurLiveSource()` asynchronously. The reason the OEM activity is needed for visible video remains a hypothesis.
- **Decision:**
  1. For AV=5, HDMI1=18, HDMI2=17, Cable=1 and TV/AIR=27: call `TvManager.setSource(sourceId)`, persist `PlayingSource`, send the OEM source-change broadcast, then launch the resolvable explicit component `com.tsb.tv/.Tv_strategy` with `FLAG_ACTIVITY_NEW_TASK` only. Verify live source after launch without blocking the UI.
  2. Preserve PhilcoOS as persistent HOME and confirm the hardware HOME key returns from `Tv_strategy`.
- **Observed on device (QC05-r3):** All five source IDs were selected separately. `getCurLiveSource()` asynchronous logs and `PlayingSource` matched the requested ID; `Tv_strategy` was resumed. HOME returned to PhilcoOS with `mAlways=true`; final source was HDMI2=17. `TvManager.setSource` returns `false` in this firmware even when the subsequent live-source readback confirms the requested ID; do not interpret that boolean alone as failure.
- **Consequence:** Source routing and activity handoff are confirmed. Actual external picture/signal was not verified; do not claim video output from routing/readback alone.

---

## DEC-007: Realtek Physical Hardware Audio Mute via ApMute and Master Mute (superseded)
- **Date:** 2026-11-21
- **Status:** REJECTED AS A PHYSICAL-MUTE IMPLEMENTATION — API/UI READBACK WAS A FALSE POSITIVE
- **Context:** The original Quick Control Center used `TvManager.setMute(boolean)`. Reverse engineering identified `setApMute(boolean)` as the amplifier mute call. Runtime inspection found that this installed firmware exposes `setApMute` but does not expose `getApMute`; it does expose `AudioManager.setMasterMute(boolean,int)` and `isMasterMute()`.
- **Decision:**
  1. Call `TvManager.setApMute(muted)` and `AudioManager.setMasterMute(muted, 1)` when available; use `setStreamMute(STREAM_MUSIC, muted)` only as fallback. Read actual state back with `AudioManager.isMasterMute()` before updating the UI.
  2. Call `TvManager.setMute(muted)` only for tuner sources (source type 1/27), never as a substitute for the amplifier/system mute. This installed firmware lacks `TvManager.getApMute()`.
  3. Record source volume before/after. If mute changes it, restore with the OEM `AudioManager.setStreamVolumeForL4300` bridge and verify the restored value.
- **Observed on device (QC05-r3):** The quick-control button changed from “Silenciar” to “Ativar som” after `setApMute(true)` and master-mute readback `true`, then returned to “Silenciar” after `setApMute(false)` and readback `false`. Volume read 0 before/after. No audible program was playing, so speaker silence/restoration and nonzero-volume preservation remain acoustically unverified. The volume-restore-on-change path is implemented but was not triggered because volume did not change.
- **Consequence:** Device APIs and UI state are confirmed, but do not claim audible muting until checked with known content at nonzero volume.

### QC05-r4 correction (2026-11-21)
- User confirmed that the physical remote's mute indicator also changes while SmartTube audio remains audible. Therefore `setApMute`/`setMasterMute` state is not evidence of acoustic mute on this route. DEC-007's proposed implementation is superseded; neither method is used by the QCC media mute.

## DEC-009: QCC media-stream mute with saved-index recovery
- **Date:** 2026-11-21
- **Status:** IMPLEMENTED, SOFTWARE-READBACK VALIDATED, AND USER-REPORTED PHYSICALLY WORKING
- **Decision:** Treat mute as a `STREAM_MUSIC` volume-zero alternative, not an amplifier/HAL mute. Persist the exact positive media-stream index synchronously in SharedPreferences before setting zero with the OEM `AudioManager.setStreamVolumeForL4300(streamType=3, ...)` bridge. Do not overwrite the saved index while mute is active. Require exact zero readback before setting the active marker, and require exact saved-index readback before clearing it.
- **Recovery/input:** On Home resume, reapply zero when the active marker is persisted; retain the registered volume-change receiver while the launcher is paused/muted and reapply zero after volume-key changes. Consume volume up/down while the launcher is foreground and mute active so they cannot overwrite the saved value. The saved value survives process death/reboot through SharedPreferences; reboot recovery is implemented but only process termination/relaunch was exercised.
- **Evidence:** `TSB_Tv.apk` calls `setStreamVolumeForL4300` with literal stream type `3`; framework disassembly shows this method resolves the device for the supplied stream and updates its `VolumeStreamState`. Runtime QCC readback went 26→0→26; a volume-up event while muted was forced back to 0 without changing saved 26; after force-stop/relaunch the saved 26 and mute-active state recovered at stream index 0, then unmute restored 26. Final stream index restored to the pretest baseline 25.
- **Limit:** The direct listening condition during the agent-run QCC cycle was interrupted when Home took focus. The user subsequently reported that the volume-zero workaround physically worked. This is user-confirmed behavior, not an instrumented audio measurement; it remains a media-volume mute alternative, not native amplifier mute.

---

## DEC-008: Realtek Hardware Picture & Sound Parameter Integration
- **Date:** 2026-11-21
- **Status:** RESEARCH ONLY — NOT IMPLEMENTED OR DEVICE-VALIDATED
- **Context:** The UI lacked hardware picture (contrast, sharpness, picture modes) and sound controls (bass, treble, sound modes, 7-band EQ).
- **Decision:**
  1. Expose verified Realtek TvManager methods in the Quick Control Center:
     - Picture: `getBrightness()`/`setBrightness(int)`, `getContrast()`/`setContrast(int)`, `getSaturation()`/`setSaturation(int)`, `getSharpness()`/`setSharpness(true, int)`, `getBacklight()`/`setBacklight(int)`, `getPictureMode()`/`setPictureMode(int)`.
     - Sound: `getBass()`/`setBass(int)`, `getTreble()`/`setTreble(int)`, `getBalance()`/`setBalance(int)`, `getSoundMode()`/`setSoundMode(int)`, `getEqualizer(int freqHz)`/`setEqualizer(int freqHz, int val)`.
  2. Implement safe reflection with bounds clamping [0, 100] and hardware read-back validation.
- **Consequences:** Gives the user direct, low-level control of the TV's display and audio processors from the PhilcoOS launcher.

## 2026-11-21 — Compact weather and clock treatment

- Set the Home weather card to 420 × 86dp, right-aligned below system indicators. Keep live temperature, condition, city, and attribution; use a darker glass surface for photo-wallpaper contrast.
- Draw weather states with the existing native Canvas view, including sun, cloud, rain, fog, snow, and storm. Avoid SVG/PNG runtime decoding and new dependencies on API 19.
- Use white 38sp Roboto Light for temperature, 94sp Roboto Light for time, and 20sp Roboto Medium for the date beneath it.


## 2026-11-21 — Home clock and weather typography

Use a bundled static Lato Medium TTF for the Home/saver clock instead of a thin system face. It is a modest-weight display face, readable at TV distance, with no runtime dependency. Keep Android 4.4/API 19 compatibility; avoid newer `TextView` typography APIs. Recompose the weather card around only verified live fields (icon, city, condition, temperature), preserving the provider credit and current focus/click behavior. The style is iOS-inspired but remains integrated with PhilcoOS dark glass and Aurora accents.

## 2026-11-21 — Clock zone and Home alignment

- Use IANA `Etc/GMT+3` for a fixed UTC−3 display on this Android 4.4 TV. The `Etc/GMT` sign is intentionally reversed by IANA. On the actual device, setting `persist.sys.timezone=GMT-03:00` produced UTC+3 and a six-hour error; it was immediately corrected to `Etc/GMT+3`. Keep automatic clock (`auto_time=1`) enabled and automatic timezone (`auto_time_zone=0`) disabled to avoid restoration of the wrong region ID.
- Keep the Home date numeric but spell the weekday fully (`EEEE • dd/MM/yyyy`). Use the same Lato Medium face as the Home time, and optically inset the date by 8dp to align visible glyph edges. Right-align the source/OS label to the same right edge used by actions, status and weather card.
- Use the official Google Material Symbols Rounded SVG artwork rasterized as 96×96 transparent PNG for the seven WMO icon families used by this client. Preserve the original SVGs and Apache 2.0 license in `docs/assets/material-weather-reference/`. Source: https://github.com/google/material-design-icons.

## 2026-11-21 — Screensaver optical alignment

When a user reports the screensaver date/weather as visually right-shifted, apply an 8dp left optical correction to both rows while keeping the large time fixed. Permit child drawing beyond the centered content bounds to prevent clipping. The r3.10 attempt lacked that clip allowance and clipped the first date glyph; r3.11 corrects it. Keep both APKs preserved and treat r3.11 as current installed candidate.

## Idle policy decision — 2026-11-23
- Keep screensaver at 5 min. At idle 15/30 min set Realtek backlight to absolute 40/20, saving the original value once and restoring that same value on wake. Begin the 30-second warning at 59:30 so, without music, Realtek standby is requested at 60 minutes total.
- Do not request standby while the in-process music repository reports `PLAYING`; defer and show a fresh 30-second warning after playback ends if the one-hour idle deadline has passed. This uses the existing service/repository and adds no dependency.
- Physical timing/brightness and standby behavior remain unproven until observed on hardware. Never copy this Conscrypt-dependent APK into `/system`; package persistence after a prior power loss/reboot remains an open device-level issue.

## DEC-2026-11-23 — Restore the last TV input after boot

- Keep the feature opt-out from Quick Control Center, enabled by default. Save only a Realtek source ID confirmed by `getCurLiveSource()`; current valid IDs are 1, 5, 17, 18, and 27.
- At `BOOT_COMPLETED`, use the app's existing Realtek `TvManager.setSource()` bridge with delayed, bounded read-back retries. Do not launch `com.tsb.tv/.Tv_strategy` from the receiver, because the PhilcoOS Home must remain visible.
- This is a reversible app-level change; it does not modify firmware or partitions. Actual input restoration across a real reboot is still an open validation item, especially due to historical `/data/app` update fallback after reboot.

## DEC-2026-11-23 — Power controls require hardware-granted privilege

- Do not expose app-level shutdown/reboot actions unless the installed firmware grants the required protected permission or a documented OEM service accepts the request.
- Evidence: `DEVICE_POWER` is `signature|system`; the signed PhilcoOS APK requests it, but the PackageManager did not grant it on the updated system package; `pm grant` is explicitly disallowed. Prior `TvManager.standBy()` returned false. Therefore PowerManager reflection and UI actions remain unavailable; never present API invocation as physical success.
- The custom release was promoted to the existing `/system/app/PhilcoOS.apk` slot using the same signing certificate, with the previous APK backed up locally and on-device. No other firmware component was changed.


## TV_INPUT hold menu — offline candidate — 2026-11-23

- **Goal:** on the targeted `Smart_TV_Keypad` scan code `549` / keycode `TV_INPUT`, consume the OEM event before SourceMenu; on release inject one `DPAD_CENTER`, or after a held 5,000 ms open the PhilcoOS control menu once. Other keycodes/devices pass through unchanged.
- **Implemented:** policy copy adds the early `PhoneWindowManager.interceptKeyBeforeDispatching()` hook, exact device/scan-code checks, repeat/cancel handling, pending callback cancellation, short-click OK injection through the API 19 `InputManager`, and long-press launch of `MainActivity` with the menu extra. The app menu exposes Home; shutdown/restart rows are visibly unavailable because `DEVICE_POWER` was not granted and no OEM power mechanism is validated.
- **Artifacts:** `output/candidates/android.policy-tvinput-control-menu-20261123.jar` SHA-256 `B85A2771616C621AA321D39EA4D2D4BEDC355E4ACA3465B2095664A646F6C6AC`; `output/candidates/philco-home-tvinput-control-menu-20261123.apk` SHA-256 `BC5D6EFF14B059B0076C841D9DFF2179811227796D642DFA61540AA8A1E1FD94`. Original policy backup remains `output/backups/joystick-policy-baseline-20261123/android.policy.jar` SHA-256 `58CC4324C7F7AD44C5198747A3423D742B6517B45A8A6CAFCE709E9D1D44EC2E`.
- **Validation labels:** ANALYZED, IMPLEMENTED, COMPILED, OFFLINE_VALIDATED. Policy DEX assembled to version 035 and round-tripped through disassembly; required API signatures were found in the TV's Android 4.4 framework. APK `assembleRelease` succeeded, API 19 metadata, v1 signature and zip alignment verified. No device test was run.
- **Device/SCM:** no TV writes, installs, reboots, shutdown calls, or source changes in this task. Last documented TV state is PhilcoOS Home/system package, `PlayingSource=27`; live state not queried this task. Git branch/commit unavailable (workspace has no `.git`).
- **Risk/next action:** this changes boot-critical `android.policy.jar` behavior and is only a review candidate. Before installation, independently review the DEX diff, verify framework build correspondence and recovery procedure, then arrange a controlled device test. Do not install until recovery is proven. Physical validation is still required for immediate OK, five-second hold, menu, OEM selector suppression, and unaffected directions. Power-off/restart remain disabled.


## TV_INPUT candidate — final static review — 2026-11-23

- **Original vs candidate:** DEX dump of the preserved original and candidate shows `interceptKeyBeforeDispatching()` grew from 5,026 to 5,046 code units. The new guard is at method entry, before the existing OEM branch. `com.hikeen.xymenu.MyService.Start` occurs once in each DEX; the candidate consumes the exact targeted key event before reaching that existing OEM call. This rules out a second explicit SourceMenu start path in this policy DEX, not every possible external listener in the firmware.
- **No duplicate OK:** the handler has one `injectPhilcoOk()` call, reachable only on ACTION_UP after a held DOWN. It resets the held flag before injection; repeat DOWN events are consumed; cancelled UP and UP after long-press suppress OK. UP removes the pending callback. The 5,000 ms callback opens the menu and has no OK injection path. Static checks passed; simultaneous input/handler timing and physical behavior are not device-tested.
- **APK/JAR compatibility:** APK package/version is `com.philco.ultra777.home`, versionCode 777, target/min API 19. MainActivity is exported and `singleTask`. The APK DEX contains the exact menu extra used by the policy DEX. APK v1 signature verifies; certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`. Candidate policy JAR ZIP CRC and DEX 035 passed. Build already completed successfully; no source/build rerun was needed.
- **Recovery assessment:** original `android.policy.jar` is backed up locally at `output/backups/joystick-policy-baseline-20261123/android.policy.jar` (SHA-256 `58CC4324C7F7AD44C5198747A3423D742B6517B45A8A6CAFCE709E9D1D44EC2E`). A recovery path for a failure before Android/ADB becomes available has not been proven; correct on-device file ownership, mode and SELinux context have not been collected for a restore. Therefore the candidate is **not safe to install yet**. First validate a recovery route (for example, the device's supported USB recovery) and preserve the exact restore commands/metadata. No write/install/reboot was done.


## Recovery readiness for TV_INPUT policy candidate — 2026-11-23

- **Device evidence (read-only):** device `smart_tv001`, Android 4.4.2 fingerprint `realtek/rtd298x_tv001/...20170531.092306`; current `/system/framework/android.policy.jar` SHA-256 exactly matches local original backup `58CC4324C7F7AD44C5198747A3423D742B6517B45A8A6CAFCE709E9D1D44EC2E`. Live attributes from `ls -lZ`: mode 0644, owner/group shown as `bluetooth:bluetooth`, SELinux `u:object_r:unlabeled:s0`. Do not substitute staging metadata: factory archive metadata describes a different JAR (115,643 bytes, mode 0664, uid/gid 1006 `wubf`, different SHA-256).
- **Official USB availability:** Philco's official driver index has a software package under the exact `PH42F10DSGWA` model directory: https://drivers.philco.com.br/TV/CHANGHONG/PH42F10DSGWA/. The local firmware bundle includes `0.vmlinux.rescue.bin` and `install_a`; the inspected installer formats/recreates system/data and writes kernel/boot/microcode. This is broad firmware recovery/update, not a validated single-file rollback. A rehosted copy of the described original manual reports a Settings > Software Update (USB) route, but no update/recovery procedure was run.
- **Recovery mode:** `/system/bin/recovery` exists in the factory baseline; `/system/recovery-from-boot.p` and `/system/bin/fastboot` were absent on the live device. These facts do not prove how to enter recovery, whether it exposes ADB, or whether it can mount and restore one JAR. No recovery reboot was attempted.
- **ADB resilience:** live `adbd` is `/sbin/adbd`, started as an init service; `init.svc.adbd=running`, `sys.usb.config=adb`, and ADB over TCP is currently connected. Because adbd is an init process separate from SystemUI/system_server, it is likely to remain available for a SystemUI-only failure, and may remain during a framework crash while init/network/adbd survive. This was not fault-tested; loss of boot, Wi-Fi/network, init or adbd means ADB cannot be relied on.
- **Restore requirements:** the only empirically grounded attributes for this build are the current live values above; numeric UID/GID were not resolved and `/file_contexts` has no explicit `framework` mapping. A future restore must preserve the exact installed JAR bytes and its live metadata/context, not use values from the August staging archive. Those values alone do not establish a recovery command or safe offline restore mechanism.
- **Conclusion:** official USB firmware exists, but no official or tested file-level recovery path has been established. Do not install the policy candidate until a separate recovery method is verified (including entry procedure and ability to restore the original JAR when normal Android is unavailable). No writes, install, reboot or standby action occurred; TV remains on its existing PhilcoOS/system baseline.


## Rear physical D-pad event audit — STOPPED before patch — 2026-11-23

- **Identified nodes:** live `/dev/input/event2` is `Smart_TV_Keypad` (virtual input device, bus 0006, sysfs `/devices/virtual/input/input2`) with EV_KEY directions, POWER, MENU and code `0x225`. Separate `/dev/input/event0` is `venus_IR_input` (IR receiver, `/devices/virtual/input/input0`). The user has no functional remote, so event0 was not physically pressed. These are different devices; both keylayouts map Linux key code 549 to TV_INPUT, but event2 uses Generic.kl fallback (no `Smart_TV_Keypad.kl` found) while event0 has `venus_IR_input.kl`. `Realtek_Keypad_Device.kl` is a third named mapping and is not the live event2 name.
- **Code terminology:** live event2 reports EV=3 (EV_SYN + EV_KEY), not EV_MSC. The trace showed `EV_KEY 0225` DOWN/UP; 549/0x225 is the Linux EV_KEY code mapped by the keylayout to `KEYCODE_TV_INPUT`, not a separately observed hardware `MSC_SCAN` value.
- **Partial raw evidence:** `output/captures/rear-dpad-event-capture-20261123.txt` records event2 KEY_LEFT, KEY_DOWN, `0225`, and KEY_POWER events. The capture did not reliably mark which physical action produced every line; KEY_UP/RIGHT were not captured. During the user's requested central long press the TV powered off; `KEY_POWER` DOWN/UP appeared in the same trace. Do not repeat a long press to reproduce this.
- **OEM behavior:** exact current installed policy JAR is backed up and its SHA matches the TV. Its policy DEX has an OEM `com.hikeen.xymenu.MyService.Start` branch for TV_INPUT. The earlier candidate checks `Smart_TV_Keypad` and keycode 549 before that branch, so it targets event2 rather than the separate IR node. However, it does not intercept a `KEY_POWER` event. It therefore cannot yet be considered safe or sufficient for the rear joystick's hold behavior; the observed power-off path may bypass its TV_INPUT handler.
- **Status after user powered TV back on:** read-only ADB confirms boot complete; PhilcoOS MainActivity resumed, PackageManager path `/system/app/PhilcoOS.apk`, `PlayingSource=17` (HDMI 2). No source, system file, APK/JAR, or firmware was written; no build or install occurred. Previous candidates/backups remain unchanged.
- **Decision/next exact action:** no new candidate is generated. Before any implementation, capture a phase-marked **short central click only** on event2 to identify its event sequence without another long hold; then review policy handling of the observed KEY_POWER path. Up/right mappings also remain to be captured individually if needed. Until that evidence exists, preserve the current firmware behavior and do not install the earlier TV_INPUT candidate for this use case.


## Rear D-pad short-click / KEY_POWER analysis — 2026-11-23

- **Existing capture:** `output/captures/rear-dpad-event-capture-20261123.txt` is partial and has no action markers. It records event2 pulses for `KEY_LEFT`, `KEY_DOWN`, `EV_KEY 0x225`, and `KEY_POWER`; timestamps alone do not prove which events belong to one gesture. No `EV_MSC/MSC_SCAN` appeared. The user reports the TV turned off during that physical sequence; do not repeat a hold.
- **Live system confirmed read-only:** Android 4.4.2 KOT49H build 20170531; `/system/framework/android.policy.jar` hash matches the preserved original. `Generic.kl` maps EV_KEY 549→TV_INPUT and 116→POWER. event2 `Smart_TV_Keypad` has no matching named keylayout, so Generic.kl applies; `venus_IR_input` event0 is a separate IR device with its own layout, also mapping 549→TV_INPUT. The active input process is `/system/bin/keypad`; `com.cvte.keyservice` is separate.
- **OEM keypad evidence:** a read-only pull of the live 9,728-byte `/system/bin/keypad` (SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`) contains strings `Smart_TV_Keypad`, `PIN_KEYPAD_SOURCE`, `PIN_KEYPAD_PWR`, and directional/menu pins. This supports native keypad handling but strings alone do not establish how a center hold maps to SOURCE or PWR. Live keyservice APK SHA-256 `157C87737441719CDF593831F10711D577329567E0862A3037045F5E75EFA236`; its DEX has no matching printable mapping strings.
- **Ordering / power route:** exact installed policy is the preserved DEX (`android.policy.jar` SHA noted above). Input policy calls `interceptKeyBeforeQueueing()` before `interceptKeyBeforeDispatching()`. Queueing handles Android keycode 26 (`KEY_POWER`) via the standard power-key state and `interceptPowerKeyDown/Up`; it schedules the resource-defined power-long-press callback. For `KEY_POWER` it also checks Realtek `GetBurnMode` and may send `com.hikeen.powerkey`, then checks TV scan/record/timeshift and CEC power options. This handling is earlier than the old TV_INPUT hook in `interceptKeyBeforeDispatching`, so that hook cannot prevent an earlier POWER action. The keylayout has no 5-second gesture mapping.
- **OEM shutdown association:** observed KEY_POWER pulses plus the user's report make the power path a concrete risk. Static evidence does not yet prove which center gesture generated those pulses or whether the native keypad service itself emits them after a hold. No global KEY_POWER suppression is acceptable.
- **Implementation decision:** no new candidate and no source changes. If the short-click trace confirms distinct event2 code(s), the possible safe design is a narrowly device-scoped early queueing filter for only the proven center gesture, while leaving IR and all directions alone; exact ability to infer a five-second hold depends on the raw event sequence and must be proven first.
- **Current state:** after the user powered the TV back on, read-only ADB confirmed boot complete, PhilcoOS MainActivity resumed, `/system/app/PhilcoOS.apk`, `PlayingSource=17` (HDMI 2). No device writes, install or reboot occurred.
- **Next test, pending explicit user confirmation:** start a timestamped `getevent -lt /dev/input/event2` capture, record the initial activity/source, ask for exactly one center tap with immediate release (no hold), stop capture immediately, then read final activity/source. No artificial input or IR event will be sent. Do not run until the user confirms.

### Captura autorizada: um toque curto no centro — 2026-11-23
- Arquivo: `output/captures/rear-dpad-short-center-20261123.txt`.
- Somente leitura em `/dev/input/event2` (`Smart_TV_Keypad`); captura limitada e encerrada após inatividade. Um toque breve produziu `EV_KEY 0225 DOWN` em `1795457876.153197` e `EV_KEY 0225 UP` em `1795457876.153253`, com `SYN_REPORT` após cada evento. Não houve `KEY_POWER` nem `EV_MSC` nesta captura.
- Leitura final da janela: `com.philco.ultra777.home/.MainActivity`; TV permaneceu online e `PlayingSource=17` (HDMI 2). Nenhum evento artificial, escrita na TV, instalação ou reboot.
- `Generic.kl` mapeia código Linux 549/`0x225` para `KEYCODE_TV_INPUT`. Assim, o toque central curto foi identificado no nível de entrada, mas **não foi convertido nem validado como OK/ENTER**. A janela Home em foco não comprova comportamento de OK nem exclui overlay OEM não refletido por esse campo.
- A associação de `KEY_POWER` vista na captura parcial anterior continua indeterminada. Não houve pressão longa e não se deve repeti-la nesta investigação.
- Próximo passo: revisar a implementação OEM a partir dessas evidências; qualquer captura adicional de interação física exige instrução específica. Nenhuma alteração/candidato de framework foi feito.

## Revisão estática do desenho de pressão longa — 2026-11-23
- **Artefato revisado, não instalado:** `output/work/tv-input-policy/smali/com/android/internal/policy/impl/PhoneWindowManager.smali` e `PhoneWindowManager$TvInputLongPress.smali`; candidato JAR `output/candidates/android.policy-tvinput-control-menu-20261123.jar`. O código local detecta `keyCode=TV_INPUT (0xB2)`, `isKeyPad()` pelo nome `Smart_TV_Keypad` e `scanCode=0x225`; intercepta em `interceptKeyBeforeDispatching()` antes do ramo OEM que inicia `com.hikeen.xymenu.MyService.Start`.
- **Fluxo proposto:** DOWN inicial agenda callback de 5000 ms; repetições são consumidas; UP cancela o callback e injeta um par `KEYCODE_DPAD_CENTER` (0x17) somente se não cancelado e o callback não marcou long press; callback verifica `held`, marca `longPressed` e abre uma vez a MainActivity com extra `JOYSTICK_CONTROL_MENU`. O evento original é consumido. Não há alteração explícita das quatro direções nem do ramo `KEY_POWER`.
- **Limitações estáticas:** não se demonstrou que uma pressão longa do hardware gere apenas `TV_INPUT`. `KEY_POWER` é processado em `interceptKeyBeforeQueueing()`, antes do gancho em dispatch; se o keypad emitir um evento POWER independente, esse desenho não o suprime por projeto. A captura autorizada de toque curto mostrou somente `0x225` DOWN/UP, portanto não resolve o comportamento de hold. Também não há validação de execução do timer, injeção de OK ou abertura do menu no aparelho. O ramo que consome DOWN sem agendar caso `mHandler` seja nulo pode perder um toque; deve ser revisto no diff final. A fronteira exata de 5 s depende da disputa entre callback e UP e não foi testada.
- **Conclusão:** a arquitetura é coerente para distinguir DOWN/UP de `TV_INPUT` sem atrasar o clique depois da soltura e bloquear o seletor OEM nesse ramo. Ainda não é seguro afirmar que cumpre o requisito de não desligar: isso depende de evidência de que o hold não gera `KEY_POWER`. Sem executar hold, essa evidência precisa vir de análise estática conclusiva do produtor OEM do evento; caso não seja possível, manter a instalação bloqueada.
- Nenhum arquivo Java, Smali, JAR, APK ou TV foi modificado nesta revisão; sem build, instalação, reboot ou teste físico. `git status` não está disponível porque `C:\PhilcoOS` não contém repositório Git (`fatal: not a git repository`).


## Análise estática do produtor OEM de teclas — 2026-11-23
- Artefato: cópia local somente leitura de `/system/bin/keypad`, ELF32 ARM little-endian, 9.728 bytes, SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`. Tem imports dinâmicos, mas não símbolos completos nem fonte; não há driver `.ko`, fonte GPIO ou imagem de kernel/firmware correspondente entre os artefatos locais examinados.
- O binário contém strings `/dev/uinput`, `Smart_TV_Keypad`, `/dev/mem`, `/sys/realtek_boards/pcb_enum`, `PIN_KEYPAD_SOURCE`, `PIN_KEYPAD_PWR` e logs de repetição. Imports incluem `ioctl`, `write`, `mmap` e `usleep`. O código cria eventos uinput (`UI_DEV_CREATE`) e possui rotinas que escrevem pares EV_KEY DOWN/UP; uma usa Linux key code `0x74` (KEY_POWER), outra `0x225` (Generic.kl→Android TV_INPUT). Isso aponta fortemente para `/system/bin/keypad` como produtor do dispositivo virtual e eventos. Não prova qual configuração de pino chama cada rotina nem que segurar SOURCE vira POWER.
- O binário contém `Repeat_Count`, comparações/counters e `usleep` (incluindo 100 µs, 50.000 µs e 13.000 µs nos trechos examinados). Não foi possível associar limiar temporal a `PIN_KEYPAD_SOURCE`→`KEY_POWER`; não há prova de threshold de 5 s. O fluxo de configuração de pinos não ficou demonstrado de ponta a ponta. Esses sleeps/counters não provam duração de hold.
- A captura curta `output/captures/rear-dpad-short-center-20261123.txt` mostra `0x225` DOWN/UP separados por 56 µs. A captura anterior contém pulsos `KEY_POWER` e `0x225` em event2, mas sem marcadores de gesto. A duração do KeyEvent não representa comprovadamente o tempo físico pressionado. Assim, o timer candidato de 5 s iniciado no DOWN e cancelado no UP não está validado e provavelmente perderia o hold se o daemon continuar emitindo pulsos.
- Caminho Android: D-pad/pinos da placa → acesso OEM a GPIO/registradores (o daemon referencia `/dev/mem` e enumeração Realtek; driver/pino exato indisponível) → `/system/bin/keypad` escreve EV_KEY em `/dev/uinput` no `Smart_TV_Keypad` → kernel expõe evdev (`event2`) → EventHub → InputReader aplica Generic.kl (549→TV_INPUT/178; 116→POWER/26) → InputDispatcher chama `interceptKeyBeforeQueueing()` e depois `interceptKeyBeforeDispatching()`. TV_INPUT é interceptável antes do ramo SourceMenu em dispatch; KEY_POWER entra no tratamento de energia na fila.
- Em princípio, interceptar sem afetar outras fontes é possível em `interceptKeyBeforeQueueing()`, condicionado ao device ID/nome `Smart_TV_Keypad`, código de POWER e uma sequência central reconhecida; IR/outros dispositivos ficam fora. Hoje não há correlação segura demonstrada entre sequência do centro e POWER. Um filtro prematuro poderia suprimir um desligamento legítimo do keypad ou deixar passar o POWER do hold. Não alterar KEY_POWER até esclarecer o produtor.
- Decisão: causa temporal exata permanece não determinada; instalação de candidato segue bloqueada. Nenhum teste físico, escrita na TV, alteração de candidato, build ou reboot nesta etapa.


### Complemento: cópias de firmware e validade da análise — 2026-11-23
- A cópia lida da TV (`output/work/rear-dpad-audit-20261123/keypad`) tem SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`. A baseline extraída e os dois estágios locais (`extracted/factory_baseline/system/bin/keypad`, `build/system_stage/bin/keypad`, `build/system_stage_slim/bin/keypad`) têm 9.728 bytes, mas SHA diferente e idêntico entre si: `A49D4456DDDD073714292596A0BC9691AB2B081C37BB24A9FA050C9EB302BB61`. As strings são semelhantes, mas não se deve transferir conclusões de temporização entre essas imagens e o executável instalado.
- Há também `CVTEkeypad` (9.752 bytes), com mensagens `press pwr`, `press Source` e `PIN_KEYPAD_PWR2`; seu hash é igual entre baseline e staging. Não foi encontrado script de inicialização textual que prove que esse binário é executado. `top` arquivado no diagnóstico anterior mostrou `/system/bin/keypad` ativo (PID 1801); não foi feita nova consulta ao dispositivo.
- O `system.emmc.tar` local contém ambos os executáveis. A busca por definições legíveis de `PIN_KEYPAD_SOURCE/PWR` encontrou referências nos binários `keypad`, `CVTEkeypad` e `TvServer`, sem arquivo textual de configuração/pinos identificado. `TvServer` contém strings de pins e `POWER_EN`, mas isso não demonstra que ele gere os eventos uinput do event2.
- Consequência: o caminho provável do produtor instalado é `keypad` → uinput → event2; criação do dispositivo e writes EV_KEY são sustentados pelo ELF instalado. O vínculo concreto entre registro/pino SOURCE ou PWR, gesto físico e código emitido segue não comprovado. Também não foi localizado o driver GPIO ou fonte correspondente ao build instalado; cópias de módulo/kernel do staging não substituem esses artefatos.
- Alternativas: (A) modificar `keypad` seria a camada que ainda pode ler pinos/estado, mas requer binário/fonte/configuração exatos, fluxo e recuperação; não recomendada agora. (B) framework só recebe os pulsos já sintetizados; filtro condicional no início de `interceptKeyBeforeQueueing()` poderia capturar POWER de event2, mas falta identificar uma sequência central estável para não suprimir energia legítima. (C) serviço Android comum recebe eventos já roteados e não tem um canal exclusivo do GPIO; não distingue a duração física sem interface OEM específica. **Recomendação:** nenhuma implementação até conseguir a correspondência da imagem instalada com a fonte/configuração do keypad ou um protocolo OEM documentado que preserve estado do botão.
- Sem teste físico, escrita na TV, edição/geração de candidatos, build ou instalação. Artefatos existentes preservados.


### Consulta read-only da configuração efetiva da placa — 2026-11-23
- ADB `192.168.0.71:5555` estava `device`; não houve interação física. `/system/bin/keypad` estava ativo (PID 1797) e `TvServer` ativo (PID 2037).
- `/sys/realtek_boards/pcb_enum` é uma **pasta** de parâmetros da placa, não um arquivo. Leituras efetivas: `PIN_KEYPAD_PWR=0x400000001`, `PIN_KEYPAD_UP=0x41f000001`, `PIN_KEYPAD_DOWN=0x427000001`, `PIN_KEYPAD_LEFT=0x408000001`, `PIN_KEYPAD_RIGHT=0x530000001`. Não existe entrada `PIN_KEYPAD_SOURCE` nem `PIN_KEYPAD_MENU` nesse diretório. Os valores são códigos opacos do firmware; não os decodificar como GPIO/registrador sem formato OEM.
- `/sys/class/gpio` não existe. `/sys/devices/platform/Venus GPIO` existe, mas a listagem mostrou apenas `modalias`, `uevent`, `driver`, `subsystem` e `power`, sem atributo de estado de pino. `/sys/devices/virtual/input/input2/uevent` identifica `Smart_TV_Keypad`; `MODALIAS` inclui key bits `0x74` e `0x225`. Isso expõe capacidades do dispositivo virtual, não estado elétrico atual. A leitura anterior de `/sys/realtek_boards/pcb_enum` como arquivo falhou porque era diretório; esta consulta corrigiu isso.
- Evidência combinada: a configuração da placa define um pin `PWR` e os quatro direcionais, enquanto o event2 virtual oferece `KEY_POWER` e `TV_INPUT`; o clique central curto observado ainda emite `0x225`. É plausível que o daemon interprete o pin PWR/centro e sintetize código conforme estado/repetição, mas a associação efetiva PWR→SOURCE/POWER e a duração do hold **continuam não comprovadas**. Não há interface sysfs encontrada para ler continuamente o nível físico do pino.
- O daemon tem thread de keypad, `usleep` e contadores `Repeat_Count`, compatíveis com leitura periódica/repetição; a implementação exata e o limiar não foram estabelecidos por causa do binário parcialmente stripped e falta de documentação da codificação `pcb_enum`.
- Decisão mantém bloqueio: não interceptar `KEY_POWER` até correlacionar a sequência física/origem; não modificar o daemon. Próximo passo estático necessário: documentação/formato Realtek de `pcb_enum` para este build ou fonte/mapa de placa correspondente ao firmware instalado, além do fluxo de estado/repeat do ELF live. Nenhum teste físico, escrita, instalação, build ou alteração de candidato.


## Comparação do keypad instalado com a baseline de firmware — 2026-11-23
- **TV (ADB, leitura):** fingerprint `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`; build date 31/05/2017; `ro.cvte.boardstr=TP.RT2984T.PB801`.
- **Baseline local mais próxima:** `extracted/factory_baseline/system/build.prop` e `firmware/package4/system.emmc.tar`, target `rtd298x_tv001`; build 4.4.2/KOT49H de 16/08/2017; `ro.cvte.boardstr=TP.RT2984T.PB801`. `firmware/config.txt` deixa `modelname`, `version` e `releaseDate` vazios. Mesmo board string/plataforma, porém baseline é posterior em ~11 semanas e não é o firmware instalado exato.
- **Comparação binária:** `/system/bin/keypad` lido da TV = 9.728 bytes, SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`; daemon na baseline `A49D4456DDDD073714292596A0BC9691AB2B081C37BB24A9FA050C9EB302BB61`, também 9.728 bytes. Só 5 bytes diferem: um byte em offset `0x20a8` (na `.data`, palavra inicializada: instalado `5`, baseline `0`) e quatro bytes `0x2122–0x2125` no CRC de `.gnu_debuglink`. As seções `.text` e `.rodata` são idênticas byte a byte; portanto código/instruções e strings são iguais, mas o estado global `.data` diferente impede afirmar equivalência funcional até descobrir seu uso.
- O binário `build/system_stage/bin/keypad` e `build/system_stage_slim/bin/keypad` são idênticos à baseline A49D, não ao daemon instalado FE90. `CVTEkeypad` é outro executável; não há prova de que esteja ativo.
- **Configuração de placa instalada:** consulta anterior encontrou `PIN_KEYPAD_PWR=0x400000001`, UP `0x41f000001`, DOWN `0x427000001`, LEFT `0x408000001`, RIGHT `0x530000001`; `PIN_KEYPAD_SOURCE` não está presente. O arquivo de firmware local não fornece uma tabela legível que decodifique esses valores ou prove a mesma configuração para maio/2017. `board_id` exposto pela TV é `0`, insuficiente para derivar o mapa.
- **Conclusão:** baseline é um comparador forte para a implementação do daemon porque o código executável é igual, mas não substitui o build de maio/2017. O produtor parece ter a mesma lógica de código; o efeito da palavra `.data` diferente e a configuração GPIO efetiva do build instalado permanecem desconhecidos. Próximo trabalho estático: encontrar xref/uso da global inicializada em `0x20a8` e uma tabela de placa correspondente ao build de maio, ou outra imagem oficial exata. Sem alterações/candidato/build/instalação.


## ELF .data xref + firmware de maio — continuação estática — 2026-11-23
- **Endereçamento corrigido:** no ELF32, `.data` tem VA `0x3000`, file offset `0x2000`, tamanho `0xb0`. A palavra distinta começa em VA `0x30a8`, offset de arquivo `0x20a8`: `5` no daemon lido da TV e `0` na cópia 16/08. `.gnu_debuglink` CRC difere em `0x2122–0x2125`. `.text` (`VA/file 0x7d8`, `0xc48` bytes) e `.rodata` são idênticas.
- **Referências:** o ELF não tem `.symtab` completo. A análise Capstone das funções delimitadas pelo `.ARM.exidx` não encontrou referência PC-relative direta à VA `0x30a8`; o fluxo usa PIC/GOT e globals acessadas indiretamente (ex.: em `0xa28–0xa8a` há `SB` inicializado PC-relativamente e stores a globals `.bss`). Não foi possível provar que o inteiro `.data` em `0x30a8` seja variável lida/escrita, estrutura ou configuração, nem localizar todas referências indiretas com o ELF stripped. **Efeito sobre repetição, tempo, pinos, POWER/TV_INPUT e inicialização: não comprovado.** Não atribuir sem mais símbolos/mapa de relocação ou código-fonte.
- **Firmware exato:** arquivos locais de firmware identificáveis são da baseline 16/08/2017 (`rtd298x_tv001`, `TP.RT2984T.PB801`); nenhum pacote/imagem de 31/05/2017 correspondente foi encontrado. O metadata `firmware/config.txt` da baseline deixa modelo/versão/data de release vazios. O binário original May permanece sendo a cópia read-only coletada da TV, não uma imagem oficial May completa.
- **TvServer:** há uma cópia local coletada da TV (`.toolchain/tmp/TvServer-installed-20170531.bin`) e a variante de agosto no firmware. Strings de `TvServer` citam `PIN_KEYPAD_PWR` e direções; isso não prova que inicie/controla `/system/bin/keypad` nem que forneça os valores. Nenhum script de boot ou protocolo/socket/Binder compartilhado com o daemon foi confirmado nesta rodada. Os valores live `pcb_enum` continuam: PWR `0x400000001`, UP `0x41f000001`, DOWN `0x427000001`, LEFT `0x408000001`, RIGHT `0x530000001`; SOURCE/MENU ausentes. Significado/codificação permanecem desconhecidos.
- **Respostas:** byte `.data` influência D-pad — **não comprovada**; significado do PWR codificado — **não comprovado**; componente que associa pino a keycode — `/system/bin/keypad` é **provável** produtor uinput, associação pino→código exata não comprovada; duração física observável pelo daemon — **provável** no ponto de polling/memória mapeada, mas duração capturada no Android é apenas pulso; interface contínua de estado — não encontrada em sysfs padrão nem no nó `Venus GPIO`; timer OEM cinco segundos — não localizado/provado. Modificar framework ou daemon não é recomendado ainda.
- **Falta concreta:** fonte/símbolos do `/system/bin/keypad` instalado, mapa de hardware/decodificação `pcb_enum` para boardstr `TP.RT2984T.PB801` no build 20170531 e configuração de inicialização que vincule `TvServer` ao daemon. Sem isso, as relações solicitadas permanecem indeterminadas. Sem interação física, escrita, alteração de candidato, build ou instalação.


## Achado novo: TvServer instalado e decisão de camada — 2026-11-23
- **Artefato exato do build da TV já local:** `.toolchain/tmp/TvServer-installed-20170531.bin` (33.318.520 bytes, SHA-256 `9C6D30EE1E3B79BCC590FC374355EDA0B1CDFD098C4A10E6D7C6B5FD1EAB5F50`). É diferente da baseline de agosto (`77FAAB5D4B731AD147E6AB395E37427B87B5C033A4071DAABCBD7F2ED91223B2`).
- Strings internas do TvServer instalado incluem `KEYPAD_PARM%u=0x%8x`, `Valid keypad paramteter`, `powerMgr_setup_keypad_multi(unsigned int, long long unsigned int*)`, `PIN_KEYPAD_PWR` e direções, além de `/dev/venus_gpio`, `int getGPIO(int)`, `int setGPIO(int)`, chamadas/logs de debounce e interrupção GPIO e `TV_INPUT`. Isso confirma que o TvServer contém subsistema/configuração OEM de keypad/GPIO. **Não comprova** que ele inicia `/system/bin/keypad`, passa a configuração a esse processo, lê o centro continuamente ou oferece essa API via Binder/RPC ao app; não foi localizado um canal/export público.
- **Byte `.data`:** VA `.data=0x3000`, byte/palavra em VA `0x30a8` (offset `0x20a8`), valor 5 instalado/0 baseline. `.text`/`.rodata` idênticas. O ELF não tem `.symtab`; análise direta das referências PC-relative não achou xref para `0x30a8`. O código tem PIC/GOT e acesso indireto a globais/BSS; portanto isso **não é prova de que o campo não seja lido**. Significado e influência permanecem não comprovados. Evidência reproduzível: seções ELF (`readelf` equivalente), relocations `.rel.dyn` cobrem GOT em `0x2f80` e entradas `0x3008...`; nenhuma relocação aponta a `0x30a8`.
- **Hipóteses atualizadas:** (1) daemon lê o parâmetro PWR/GPIO e transforma nível/duração em pulsos TV_INPUT/POWER — provável, ainda sem fluxo de dados que ligue parâmetro a code emitter; (2) TvServer configura/consome parte do keypad via `KEYPAD_PARM` e `venus_gpio` — provável subsistema, integração com event2 não comprovada; (3) outro driver da placa produz eventos — possível, mas o event2 virtual e o processo `keypad` ativo favorecem daemon. Nenhuma hipótese foi eliminada por completo.
- **Decisão de arquitetura:** framework é inadequado para medir duração física no padrão já observado (DOWN/UP TV_INPUT de 56 µs); só poderia classificar uma sequência de pulsos depois de conhecer a cadência e POWER gerado. Serviço Android ordinário não tem acesso contínuo ao pino; TvServer pode ter acesso nativo GPIO, mas não foi identificada API autorizada/exportada. A camada mais apropriada para um timer confiável é a que lê o nível GPIO (provavelmente keypad/TvServer), mas ainda não se pode escolher entre patch do daemon ou interface do TvServer sem mapear `KEYPAD_PARM` e fluxo de inicialização/configuração.
- **Futuro menor conjunto de mudanças, condicional:** depois de comprovar o produtor e a configuração exatos, interceptar somente a tecla central na camada de origem, atrasar o evento curto até UP, ao atingir 5 s emitir uma ação/menu apenas uma vez e suprimir o POWER derivado do mesmo gesto; deixar os 4 direções e POWER de outras origens inalterados. Nenhum patch foi feito nesta rodada.
- **Risco restante / artefato que bloqueia decisão:** falta o mapa/schema OEM de `KEYPAD_PARM`/`pcb_enum` para `TP.RT2984T.PB801` build 20170531 e uma referência de fluxo que conecte esse parâmetro e estado do pino aos eventos `0x225`/`0x74` (fonte, mapa de símbolos ou descompilação conclusiva do produtor). Sem isso, a arquitetura de origem é preferível em princípio, mas o componente exato não pode ser selecionado com segurança.
- Sem interação física, escrita na TV, modificação de código/candidato, build ou instalação.

## Evidência nova: KeypadService e xrefs Thumb — 2026-11-23
- Init instalado confirma KeypadService /system/bin/keypad em class core, root/shell e ativo. TvServer é processo separado iniciado pelo oneshot tvserver-setup; comunicação entre ambos não foi demonstrada.
- Thumb do daemon confirma parser de /sys/realtek_boards/pcb_enum (0x118c), mmap de /dev/mem (0x132c), wrappers de leitura/escrita (0x140c/0x13f8) e loop de sete registros (0xa28, stride 0x18) antes da emissão em uinput. A duração física pode ser medida na camada daemon.
- .data VA 0x30a8 é lida: campo cmd (+0x10) da linha PWR em 0x3098; instruções 0xac8, 0xad2, 0xb26. No dispatcher 0xc3a, cmd 5→TV_INPUT 0x225, cmd 1→POWER 0x74; SOURCE e PWR contêm 5. A transição de hold para cmd 1 não foi provada.
- **Decisão:** reconhecer hold no produtor que amostra o estado, não no framework que recebe o pulso de 56 µs. **Bloqueio:** falta schema/código OEM que mapeie pcb_enum/KEYPAD_PARM ao pino central da placa/build e explique eventual transição para cmd 1. Não bloquear POWER globalmente.

## Decisão: origem do estado contínuo do PWR — 2026-11-23
A análise do ELF instalado identificou o caminho `pcb_enum` → descritor da linha PWR → amostra periódica em `/dev/mem` → campo cmd da tabela → dispatcher uinput. Para PWR, o cmd é 5/TV_INPUT e o modo é 0. O ramo `counter > 119` apenas grava o marcador global `0x3514`; não altera o cmd para 1 nem chama o dispatcher. A taxa nominal de retorno do loop é 13 ms, então esse marcador não corresponde a 5 s. O caso genérico cmd1→KEY_POWER não é alcançável a partir dos sete cmd estáticos instalados no fluxo analisado.

**Decisão:** não produzir candidato nem interceptar/bloquear KEY_POWER. A amostragem do daemon é o ponto tecnicamente mais adequado para uma futura medição monotônica, mas só após obter o kernel/mapa de hardware exato e identificar a origem do desligamento anterior. A hipótese de ADC ladder é apenas provável. Relatório e endereços: `docs/REAR_DPAD_KEYPAD_REVERSE_ENGINEERING_20261123.md`.

## KEY_POWER pelo hold central: status após análise dos produtores — 2026-11-23
A rota PWR→cmd1 existe comprovadamente no ELF legado `CVTEkeypad`, mas sua execução na TV não está comprovada e não há serviço local apontando para ele. No `/system/bin/keypad` ativo, PWR→cmd5 e o marcador após 119 amostras não chega a um consumidor nem emite cmd1. A captura parcial event2 registra KEY_POWER e a TV desligou, portanto o risco é real, porém a origem/gesto permanecem desconhecidos. **Manter qualquer bloqueio/instalação suspenso** até obter o kernel de maio com símbolos/fonte do driver e rastrear o produtor ativo.

## DEC-2026-11-23 — Home edge navigation to Music
- **Status:** Implemented in a local candidate; device validation pending.
- **Decision:** Keep the eight-card Home dock unchanged. When Ajustes has focus, RIGHT opens the existing full-screen MusicActivity; repeated RIGHT events are consumed, the center button still opens Quick Controls, and returning from Music restores focus to Ajustes. The Ajustes subtitle shows → Música as a compact cue.
- **Reason:** This gives Music a second-page feel without adding a ninth card, shrinking the established tiles, or changing the Home layout. It stays in the app layer and uses the existing API 19-compatible Activity.
- **Evidence/limit:** candidate output/releases/philco-home-home-music-right-page-20261123.apk; SHA-256 7D28138D73CEEA1CF6F36CE0D9EC7F27AEDAFD86F58B49F0DDC14FE685F68513. Build, v1 signature and zip alignment passed. Navigation has not yet been tested on the TV.
### DEC-2026-11-23 refinement — transition motion
The user clarified that RIGHT from focused Ajustes must feel like paging horizontally into Music. The existing Activity route is retained, with paired 260 ms decelerating translations (Home left / Music from right; reverse on Back). This is a brief transition only; no always-running animation or blur is introduced. Device appearance remains unverified.
## DEC-2026-11-23 — Lightweight Home Dynamic Bar
- **Status:** Implemented and compiled locally; TV installation pending because ADB went offline.
- **Decision:** Show a small graphite/gold now-playing row below the Home weather card only while the playback repository reports PLAYING or PAUSED. Include a 2D music glyph, truncated track title and state. Hide it when inactive. Reuse PlayerStateRepository.Observer, subscribe only while MainActivity is resumed, and perform no periodic UI polling or album-art decoding.
- **Reason:** Adds the useful “now playing” affordance of newer TV interfaces while keeping the resource cost small for this Android 4.4.2 device. Existing Wi-Fi, volume and weather indicators remain in place.
- **Evidence/limit:** candidate output/releases/philco-home-dynamic-bar-20261123.apk, SHA-256 77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B; compile and package checks passed. ADB was offline before install, so visual appearance and observer behavior are not device-validated.
## DEC-2026-11-23 — instalar Music slide + Dynamic Bar
- **Decisão:** atualizar o app do sistema usando o mecanismo normal de instalação de APK, mantendo `/system/app/PhilcoOS.apk` intocado. A assinatura do candidato corresponde à instalação prévia.
- **Resultado:** instalação `adb install -r` concluída. APK ativo em `/data/app/com.philco.ultra777.home-1.apk`, SHA-256 `77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B`, igual ao candidato local.
- **Recuperação preservada:** APK anterior em `output/backups/PHILCOOS_DYNAMIC_BAR_DEPLOY_20261123/PhilcoOS-installed-before-update.apk` (SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`).
- **Limite:** confirmar instalação não confirma visualmente o comportamento. Navegação, barra em PLAYING/PAUSED e composição na tela aguardam validação no aparelho.

## DEC-2026-11-23 — Music UI r2, foco TV
- **Decisão:** reorganizar Música em faixa atual compacta à esquerda, biblioteca à direita e player unificado no rodapé; manter a lista local e os comandos já existentes.
- **Referência de hierarquia:** Apple Music na TV prioriza “Now Playing” e acesso às faixas; Spotify na TV organiza conteúdo para navegação à distância. A adaptação evita dependências visuais ou APIs dessas plataformas.
- **Restrições aplicadas:** Java/Views API 19, capa fallback 2D em gradiente, sem blur, PNG novo, animação contínua ou biblioteca extra. A navegação com D-pad foi preservada e visualmente verificada.
- **Resultado:** candidato `output/releases/philco-home-music-ui-redesign-r2-20261123.apk`, SHA-256 `6F2B844069BA318B3C231346722D55BF55709339637A018DDB4B4D4259F403FD`; instalado e confirmado por hash. Evidências visuais em `output/captures/philcoos-music-ui-redesign-r2-20261123.png` e `output/captures/philcoos-music-back-home-20261123.png`.
- **Limite registrado:** capas reais de álbum ainda não são carregadas; integrar uma única miniatura RGB_565 redimensionada será tarefa separada, caso necessária.

## PHILCO-MUSIC-GLASS-003 — adaptação da referência Dribbble — 2026-11-23
- **IMPLEMENTED / COMPILED / INSTALLED:** adaptei a linguagem de vidro translúcido à página Música: wallpaper visível através de scrim mais leve, cartões fumê com borda fina e controles transparentes. Mantive somente a biblioteca local real e a faixa atual; não adicionei recomendações, artistas seguidos ou catálogo fictício. API 19/Views nativas; sem blur.
- APK `output/releases/philco-home-music-glass-r3-20261123.apk`, SHA-256 `83EEF63CE2C23100B1BA855057F04E649C120B54EFD984E19F1ADA5A80BBAFF6`; `adb install -r` retornou Success. Captura na TV: `output/captures/philcoos-music-glass-r3-20261123.png` (1280×720), TV acordada.
- **VISUALLY_CONFIRMED:** o fundo da paisagem agora aparece através dos painéis e o texto permanece legível. Backup prévio do r2 permanece em `output/backups/PHILCOOS_MUSIC_UI_REDESIGN_20261123/PhilcoOS-installed-music-redesign-r2.apk`.


## DEC-2026-11-23 — cores e atualização da tela Música
- Manter identidade dourada com dourado champanhe (`#E5C36B`) e dourado claro de foco (`#F3DA88`), sobre superfícies navy/grafite translúcidas. O dourado fica em acentos, foco e ícone; evitar painéis dourados extensos.
- Aumentar rótulos pequenos dos controles para leitura a distância. O símbolo musical usa a fonte padrão do Android para evitar falta do glifo da fonte de título.
- Atualizar seekbar/estado a cada 1 s enquanto Música está ativa, em vez de 500 ms. O callback é cancelado no `onStop`; manter playback e biblioteca sem alterações.
- Resultado r7 instalado e visualmente confirmado; sem medição comparativa de CPU ou RAM, portanto não alegar ganho quantificado. APK SHA-256 `E3127E2ED293A007163A22134EC97A8F54F9C5A845499458CD8A7521B9C65EF0`.

## PHILCO-MUSIC-REFERENCE-UI-005 — reprodução da referência — 2026-11-24
- **IMPLEMENTED / COMPILED:** adaptei a tela existente com cabeçalho PHILCOOS Música, abas, cartões translúcidos sobre o wallpaper já usado, faixa atual, lista real da biblioteca e faixa inferior de reprodução. Busca filtra MediaStore; recomendações fictícias foram substituídas por atalhos de faixas locais reais. D-pad tem ligações explícitas entre abas, lista, cartões e player.
- Reprodução, serviço e controlador foram preservados. `PlayerController.java` SHA-256 `79FCE90283DCCF7EBAD0F8160130150A62D00C70665ABECF4ED26459357DE8E1`; `MusicPlaybackService.java` SHA-256 `55741EE121725E55EC0B46188ABAC7DE9BFBE11DEA025133C0158361DE67881F`.
- Controle de volume usa AudioManager e integração Realtek já existente; ícone vetorial leve muda de um arco para dois em 50% e mostra mudo. Relógio real atualiza por eventos do sistema. Sem assets de fundo novos, blur ou dependências novas.
- **COMPILED:** APK `output/releases/philco-home-music-reference-20261124.apk`, 1.894.589 bytes, SHA-256 `3C216E85EB6FCA75807375211429D7FCD48DDA7AFBDA56D264ED454CECBDB092`; pacote/versionCode existentes, minSdk/targetSdk 19. `assembleRelease --offline`, zipalign e verificação da assinatura v1 passaram; certificado PhilcoOS existente. `lintVitalRelease` passou durante o build.
- Backup do fonte anterior: `output/backups/PHILCO_MUSIC_REFERENCE_UI_20261124/MusicActivity.java.before-final-ui`, SHA-256 `944A090453F8588A7610855A3745489599546DF3CE33C95D649E1EF17E2BFFCE`.
- **Não instalado:** sem validação visual ou funcional na TV. Reprodução, foco remoto, busca e volume precisam de confirmação no aparelho; a compilação não prova comportamento físico.

## PHILCO-MUSIC-REFINE-006 — geometria e estados da biblioteca — 2026-11-24
- Abas calculadas para x=356–924 na tela 1280×720/160 dpi; início dos painéis y=141; cards y=567–609; player y=609–687. Painéis mantêm proporção 43/57. `recTitle` agora mede 88 dp, com 78 dp de conteúdo interno e folga de 10 dp. Cards ficaram compactos (42 dp) para cumprir a restrição de não sobrepor o player; são menores que na referência.
- Biblioteca agora distingue carregamento, vazio, busca sem resultados e falha do MediaStore. Cursor é fechado em `finally`; consulta nula/erros são exibidos como falha e dados parciais descartados.
- Nenhuma mudança no backend, serviço, manifesto, build.gradle ou launcher. Backup do fonte anterior: `output/backups/PHILCO_MUSIC_SURGICAL_REFINEMENT_20261124/MusicActivity.java.before-refinement`, SHA-256 `6447FC287EB19F3D5F376FDC95CA79323DCFDC282684F406063AF0FFB91975A6`.
- APK `output/releases/philco-home-music-refined-20261124.apk`, 1.894.941 bytes, SHA-256 `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`. Build offline, `lintVitalRelease`, zipalign, assinatura v1, package/minSdk/targetSdk e equivalência do `classes.dex` ao intermediário atual verificados.
- Não instalado nem capturado na TV. A versão visual ainda precisa de comparação física; cards compactos são a diferença intencional principal.

## 2026-11-24 — refinamento final do player e recuperação Home
- Decisão: manter MediaStore/MediaPlayer e os serviços atuais; corrigir somente a camada de apresentação e interação em `MusicActivity`.
- D-pad/setas no seek controlam imediatamente a posição; volume permanece na tela Música e não exibe sobreposição cobrindo a interface. Nível final solicitado: 7.
- A recuperação de substituição do pacote é tratada dentro do próprio app; não altera HOME preferido, Haier, system_server nem partições. A retomada OEM do Haier durante indisponibilidade temporária do HOME é hipótese apoiada pelo histórico, não causa definitiva comprovada.
- Sem integração de catálogo/contas Spotify, Apple Music ou Amazon: escopo validado é reprodução da biblioteca local.

## DEC-2026-11-24 — integração do Philco Music Pro
- Reutilizar `MusicPlaybackService`/`PlayerStateRepository` para a barra inferior e o mini-player da Home; manter uma única fonte de estado de reprodução.
- Adotar as melhorias compatíveis do código revisado: filtro sem diacríticos, atualização da biblioteca com observador debounced, snapshots imutáveis para a UI, favoritos e histórico simples. Não copiar estrutura de streaming online, gerar recomendações fictícias, adicionar dependências ou redesenhar o backend.
- O visual premium é voltado ao controle remoto em 1280×720/API 19; animações e decorações contínuas ficam limitadas. A lista real continua baseada em MediaStore.
- Build e instalação do candidato passaram. A validação visual e funcional na TV está pendente após ADB encerrar a conexão; não alegar aprovação de UI/controles.

## DEC-2026-11-24 — integração seletiva do diff Qwen
- Aplicar as correções de lifecycle/estado do player e do foco Home que foram confirmadas contra os fontes locais. Preservar o backend e `MediaStore`.
- Manter o timer visual em 1 s para reduzir wakeups; não adotar a versão 500 ms sem medição que demonstre necessidade.
- Não usar `unbindService()` quando `bindService()` informa falha: sem vínculo válido, a chamada apenas lança `IllegalArgumentException`, que seria ignorada; não corrige vazamento.
- Não alterar configuração de orientação/`configChanges` no manifesto, pois não foi demonstrada necessidade e o layout TV já está em paisagem.
- Código compilado e APK assinado/alinhado. Sem validação de dispositivo; preparar testes antes de instalação.

## DEC-2026-11-24 — Volume + deve sair do mute persistido
- A Home consumia Volume + e Volume − durante mute persistido e o receiver forçava `STREAM_MUSIC=0` após mudanças de volume. Esse ciclo podia impedir o áudio de voltar.
- Decisão: o primeiro Volume + restaura o nível pré-mute; após confirmação, limpa o mute persistido. Repetições e soltura dessa tecla são consumidas. Volume − não altera o valor salvo.
- Um volume positivo recebido durante a tela do player também é tratado como saída explícita do mute; a Home restaura o nível salvo e remove o estado para não silenciar novamente.
- A recuperação do mute ao iniciar/reabrir o app permanece. A instalação e validação física desta correção ficam para etapa autorizada.

## DEC-2026-11-24 — integração seletiva de PHILCO_MUSIC_refeito
- Integrar album/genre/tags e fallback de metadados na biblioteca existente, sem substituir o serviço de reprodução nem introduzir dependências.
- Evitar o custo do ZIP recebido que tentava abrir um `MediaMetadataRetriever` por faixa: ler tags incorporadas apenas quando título/artista/álbum faltarem; construir o índice de gênero em lote pelo MediaStore.
- Corrigir o fallback do artista para nome de arquivo independentemente de o título já estar preenchido.
- Preservar funcionamento se tabela de gênero ou metadados incorporados não estiverem disponíveis. A busca continua usando os campos presentes.
- API 19 checada na tabela local de versões de API; compilado e validado offline, sem instalação na TV.

## DEC-2026-11-24 — restauração segura do volume no player r5
- Guardar o último volume audível em preferência própria do player, além da preferência temporária de mute; isso permite recuperar um nível conhecido mesmo se o estado de mute for perdido entre telas ou reinicialização do processo.
- Quando o volume atual for zero, Volume+ restaura o valor conhecido; o ajuste positivo limpa o mute persistido. Valor máximo continua limitado pelo AudioManager.
- O player/service original permanece como backend. A implementação foi compilada, atualizada com `adb install -r` e validada de 0 para volume 7. O mute acionado pelo botão gráfico não foi confirmado com o teste disponível.

## DEC-2026-11-24 — Music UI r6: separar volume, busca e transporte
- Não misturar teclas de volume com navegação do foco. Consumir o primeiro DOWN de Volume+/− no dispatcher; não solicitar foco no slider a cada mudança.
- Abrir o slider por um controle explícito e deixar BACK fechar esse painel. No slider, esquerda/direita ajustam volume; no seekbar, esquerda/direita continuam fazendo seek.
- Botões de reprodução usam ícone e rótulo lado a lado em tamanho de leitura à distância. Fila, aleatório, repetição, detalhes e favorito ficam agrupados no menu “Mais”, sem ícones pouco claros na barra principal.
- Capas consultadas por albumId e metadados embutidos apenas na arte principal; execução assíncrona, RGB_565 e LRU de até 3 MiB. Serviço/backend preservados.
- r6 compilado e assinado, porém não instalado: ADB da TV caiu. Validar layout, volume e custo das capas na TV antes de declarar conclusão visual/funcional.

## DEC-2026-11-24 — manutenção direcionada do Music r6.1
- Corrigir o loader para sempre tentar arte do MediaStore; `allowEmbeddedMetadata` controla apenas o fallback de imagem embutida.
- Incluir tamanho solicitado na identidade da entrada de cache para que arte de 120/160 px não seja reutilizada em 480 px.
- Limitar pedidos pendentes de capa a 16 numa única thread; descartar o pedido mais antigo em sobrecarga para proteger a responsividade em hardware de pouca memória.
- Proteger callbacks completion/error com identidade de instância, como o callback prepared já fazia.
- Manter `MusicPlaybackService` e a lógica de reprodução existentes; r6.1 deve ser atualização do app e precisa de validação física antes de considerar resolvido.

## DEC-2026-11-24 — dispensar Central de Controle no standby
- Manter uma referência ao diálogo da Central de Controle e fechá-lo em `ACTION_SCREEN_OFF`, `Activity.onPause()`, antes do `TvManager.standBy()` automático e antes das ações de energia PowerManager da Home.
- `onPause` é fallback para mudanças de atividade; `ACTION_SCREEN_OFF` cobre tela apagada sem mudança de Activity. As rotas próprias fecham antes da chamada do middleware.
- Não chamar `goToSleep()` adicionalmente, não mudar a tecla de energia e não alterar HOME.
- Atualização instalada com backup/hash preservados. O evento de standby físico não foi provocado; validar no próximo ciclo natural antes de chamar `DEVICE_VALIDATED`.

## 2026-11-25 — Philco Music: decisões básicas de usabilidade
- **Fila única no serviço:** escolhida para Home e MusicActivity controlarem a mesma sequência sem recriar players nem depender de a Activity estar visível. O serviço mantém tracks atuais, shuffle/repeat e histórico curto em memória.
- **Faixa anterior:** posição real `MediaPlayer` é a fonte. `<45.000 ms` volta à faixa anterior; `>=45.000 ms` reinicia; pressionar novamente após reinício vai à anterior. A flag pendente cobre a latência assíncrona do seek.
- **Seek:** foco navega com esquerda/direita; OK entra em modo de ajuste, cima/baixo alteram ±10 s, BACK sai e retorna ao controle de origem. Isso evita scrubbing acidental.
- **Áudio:** não aplicar pré-amplificação. Reassumir Audio Focus e limpar atenuação temporária na retomada; a diferença em relação a HDMI requer teste acústico controlado.
- **Reversão local:** fontes anteriores copiadas para `output/backups/PHILCO_MUSIC_BASIC_UX_20261125/source-before/`; hashes constam nos resultados. Sem edição de sistema e sem instalação.

## 2026-11-25 — revisão da fila do Philco Music
- A lógica pura `PlaybackQueue` foi separada do serviço para permitir testes determinísticos da fila e do histórico sem Android.
- A integração mantém a API pública do serviço/controlador e o limiar de 45 s para “Anterior”. A proposta externa de 3 s foi rejeitada por contrariar a regra funcional vigente.
- Não integrar cache de metadados nem auto-skip nesta mudança: são alterações de comportamento/armazenamento fora do mínimo necessário.
- TV não foi consultada nem alterada; o APK novo é apenas candidato local.

## 2026-11-25 — revisão dos patches Qwen
- Aplicar somente correções confirmadas na fonte atual; não trocar arquivos completos por versões que precedem a fila atual.
- A fila é copiada pelo serviço para armazenamento próprio; uma cópia extra na Activity não corrige alias no código atual.
- Mantido o limiar anterior de 45 s. O serviço Qwen com 3 s foi rejeitado.
- Corrigidos volume repetido ao segurar, persistência da faixa após troca de estado, saída do modo seek, seleção da primeira faixa e foco acima da lista. Relógio da Music sincronizado com UTC−03:00 da Home; volume exibido em porcentagem.
- Métodos auxiliares não removidos nesta etapa: não havia evidência de defeito ativo que justificasse refatoração.

## 2026-11-25 — redesenho visual da tela Música
- Direção visual: usar o wallpaper como atmosfera com escurecimento suficiente para leitura; cartões em carvão translúcido com contorno dourado discreto; ouro restrito a foco/ação primária; a biblioteca e a faixa atual recebem prioridade sobre decoração.
- Navegação mostra apenas destinos implementados: Ouvir agora, Biblioteca e Tocando. Não exibir abas de conteúdo que não existe.
- Barra inferior usa transporte visual sem texto minúsculo abaixo dos botões, seek amplo e volume/opções à direita.
- Mudanças limitadas à `MusicActivity`; backend, permissões, sistema, firmware e dependências não alterados.
- O candidato ainda requer revisão visual real em 1280×720 antes de ser tratado como concluído.

## 2026-11-25 — patches visuais Qwen para Philco Music
- Os onze arquivos recebidos incluem snippets independentes; não substituir a Activity inteira por versões de outra paleta.
- Adaptar o gradiente/capa para uma amostra pequena da arte hero, escurecida e aquecida com dourado PhilcoOS. Atualizar também quando a arte vier do cache ou thread de trabalho, e resetar quando faltar capa para evitar cor residual.
- Exibir o thumb do seek somente quando focado ou em modo de ajuste. Separador reduzido para não competir com conteúdo.
- Lógica de biblioteca/player/volume da versão Qwen não trouxe correção confirmada acima do código atual; backend mantido.

## 2026-11-25 — validação de instalação Qwen UI fixes
- Atualização por `adb install -r` autorizada e concluída; hash verificado antes. Sem limpar dados ou alterar preferência HOME.
- Após instalar, confirmar `mAlways=true` para PhilcoOS e deixar MusicActivity aberta. Captura real 1280×720 salva em `output/captures/philco-music-qwen-visual-fixes-device-20261125.png`.
- Não iniciar faixa nem mudar volume durante o teste visual. Se não houver faixa ativa, seek fica desabilitado e seu thumb não pode ser validado ainda.
- Nenhuma mudança de backend é justificada pelo teste visual isolado.

## 2026-11-25 — música, wallpapers, transferência e ajuste Realtek
- Buscar recebe foco explícito à direita de Tocando e mostra preenchimento dourado claro. A linha da biblioteca selecionada fica sobre carvão aquecido com contorno dourado e texto claro; evita texto preto sobre fundo escuro e não usa animação de cor.
- O menu de três pontos contém “Receber do celular”. Foi preferido um receptor HTTP pequeno, com código temporário, limite de tamanho, tipos e extensões permitidos, gravação apenas em pastas públicas de música/foto e servidor encerrado ao fechar o diálogo. Nenhum APK de terceiros foi instalado. A versão atual de LocalSend não suporta API 19; não depender de builds espelhadas sem assinatura/origem verificável.
- Dez fotos fornecidas foram otimizadas para 1280×720/JPEG qualidade 87 e adicionadas às cinco já existentes. Manter uma imagem RGB_565 ativa; Home ambiente roda a cada 7 min e Música a cada 10 min.
- A barra ambiente mostra faixa, artista, estado e progresso durante playback/pausa. É parte do screensaver luminoso, não do standby elétrico com tela apagada.
- EQ User moderado: +8 a +11 pontos em presença/agudos (3–10 kHz) e treble 58, graves 52, balance intocado. Picture mode User 0 com brilho 50, contraste 55, saturação 52, nitidez 12. Não alterar backlight. Reverter apenas pelo snapshot salvo via ação RESTORE; retorno ainda não foi executado nesta etapa.
- Não alterar `PlayerController` nem `MusicPlaybackService`; não adicionar biblioteca/dependência.
