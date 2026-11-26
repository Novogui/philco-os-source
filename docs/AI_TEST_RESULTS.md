# PhilcoOS AI Test Results and Baseline

Updated: 2026-11-21. This records observed evidence, not intended behavior. Historical results remain in `PHILCOOS_ULTRA_777_GLASS_AURORA.md`.

## Git baseline
- `C:\PhilcoOS` is not a Git repository. `git status --short --branch` and `git rev-parse HEAD` fail with “not a git repository”.
- Branch: unavailable. Last/base commit: unavailable. Existing changes/diffs cannot be enumerated through Git.
- Do not initialize Git until the current tree is backed up and a migration proposal is recorded.

## Device and installed launcher — read-only baseline
- ADB endpoint: `192.168.0.71:5555`; device reports product `rtd298x_tv001`, model `smart_tv001`.
- Android: 4.4.2, API 19.
- Installed app: `com.philco.ultra777.home`, versionCode 777, versionName `1.0.0-ultra777`, path `/data/app/com.philco.ultra777.home-2.apk`.
- Foreground activity: `com.philco.ultra777.home/.MainActivity`.
- Persistent Home record: `mAlways=true`.
- Current source: `settings system PlayingSource=17` (HDMI 2).
- **Evidence state:** `DEVICE_VALIDATED` for current ADB identity/Home/source read-back. This does not retest UI or individual features.
- No APK pulled, installed or modified in this baseline.

## Candidate and recovery APKs

| Artifact | State | SHA-256 | Size (bytes) | Evidence |
|---|---|---|---:|---|
| QC03 `output/releases/philco-home-quick-control-QC03.apk` | Installed reference; prior physical validation | `F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8` | 1,208,386 | File hash locally verified; prior pulled-install match recorded in main report |
| QC04 `output/releases/philco-home-quick-control-QC04.apk` | Latest local build; not installed | `C9870E798A380BDCBC101580F10461D86FDB1258D0628E6F520EED1D727631BD` | 1,208,363 | File hash locally verified; prior offline build/lint/signing record in main report |
| A57 `output/releases/philco-home-A57-13BADB2D.apk` | Rollback preserved | `13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676` | 1,199,801 | File hash locally verified; physical validation is historical |
| EB256 `output/releases/philco-home-visual-polish-EB256CA3.apk` | Preserved; previously staged in `build/system_stage_slim` | `EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2` | 1,199,818 | File hash locally verified; staging state recorded in main report |

The SHA checks above identify the local archived files; the current installed package was identified by package manager/path and is QC03 according to prior pulled APK/hash evidence in the main report. This task did not pull the installed APK for a new byte-for-byte comparison.

## Other existing releases
- `output/releases` also contains A56, earlier pre-install backups, functional candidates and QC01–QC02. They were preserved and not exhaustively hashed in this baseline because the requested checkpoint set was QC03/QC04/A57/EB256.
- Main report lists the SmartTube installed APK as 32.80 Stable, vCode 2470, minSdk 17, targetSdk 34, ARMv7. Its installed APK previously matched the official asset hash `F90EEA37B474F848BA37E8BFC37DF5AB31517AA41E12D0D38E185E62378488B8`.

## Open problems
- Git repository/branch/commit absent; cannot provide current commit or safely isolate Antigravity work.
- No current Antigravity Figma spec/handoff was in the workspace when this file was created.
- SmartTube: actual AVC 480p output is proven; first frame took roughly two minutes in the last captured session. Exact client and individual player-response/SABR/surface timestamps remain unknown. Do not expose raw token-bearing logs; use reviewed redacted extracts.
- Quick Control brightness/backlight behavior was historically tested and restored, but the requested new controlled comparison of QC panel method vs OEM visual brightness has not been performed in this task.
- Local AVC 1280×720@30 test and dropped-frame/CPU baseline remain pending.
- Moonlight candidate/version/signature/ABI review and Sunshine test remain pending.
- Firmware slim-stage RAM/process comparison remains pending; no firmware was written.
- TV recovery/flash method remains unvalidated; do not flash.

## Commands/results in this task
- Git status/commit query: failed because this workspace has no `.git` directory.
- ADB device/property/package/Home/source checks: successful; values above.
- `Get-FileHash -Algorithm SHA256` on the four requested archived artifacts: hashes above.
- No build, lint, installation, control adjustment, or firmware command was executed.

## TV media and Aptoide diagnostics — 2026-11-21

### Device identity and Aptoide
- ADB target `192.168.0.71:5555` was `device`, product/device `rtd298x_tv001`, model `smart_tv001`, Android 4.4.2, API 19, fingerprint `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`.
- Aptoide `cm.aptoidetv.pt.changhong_philco_44` was already disabled: it appears in `pm list packages -d`; `dumpsys package` reports `enabled=2` and `lastDisabledCaller: shell:0`. Its APK remains installed at `/system/priv-app/aptoide.apk`, version 2.2.6 / code 23. No disable command was run. Re-enable if needed with `pm enable cm.aptoidetv.pt.changhong_philco_44`.

### MediaBrowser
- Package `com.rtk.mediabrowser` is installed at `/system/app/MediaBrowser.apk`, version 1.0 / code 1, target SDK 17. Manifest and PackageManager list `VideoBrowser`, `VideoPlayerActivity`, `GridViewActivity`, `MusicActivity` and `PhotoPlayerActivity`; storage read/write permissions are declared.
- `am start -n com.rtk.mediabrowser/.VideoBrowser` brought the existing task forward; `dumpsys activity` showed `VideoBrowser` resumed and `ps` showed process `com.rtk.mediabrowser` (PID 5551). This confirms Activity/process launch, not successful media browsing or decoding.
- `dumpsys media.player` showed “No media recorder client” and no active opened/mapped file. `service list` / `service check` found `media.rtkplayer`; `dumpsys media.rtkplayer` returned no useful state. Filtered logcat showed repeated `MediaPlayer: stop called in state 0` while idle and no decode-start/OMX playback evidence. These messages are not proof that a file played.
- Pulled the system APK read-only to `output/MediaBrowser-system-baseline.apk`, 8,041,862 bytes, SHA-256 `CF073617F5143370E449EA00E495DBB6EFEEBE7DEB247CCC26178AD80C760AB3`. `aapt dump badging` resolved package/version but emitted an icon-resource reference error; `aapt dump xmltree` successfully exposed activities, intent filters and storage permissions. No APK on TV was changed.

### Storage and safety
- `df`: `/data` 3.9 GB total / 3.1 GB free; `/storage` and `/mnt/media_rw` are reported as 222.9 MB tmpfs mounts. `/storage/sdcard0` is a symlink to `/storage/emulated/legacy` → `/mnt/shell/emulated/0`; `/storage/udisk` exists but no mounted USB volume/files were visible in this check. No volume was mounted, formatted, or altered.
- `/system` appeared `rw` in the pre-existing mount table. No write/remount command was issued; this observation does not show who mounted it or why.
- Did not create or transfer a benchmark file and did not start playback. Confirm the user can observe the TV and identify a readable media path before any future controlled playback.
- After the browser inspection, `com.philco.ultra777.home/.MainActivity` was explicitly brought to foreground; HOME preference remains `mAlways=true`; `PlayingSource=17` (HDMI 2).

### Evidence limits / status
- **CONFIRMADO:** Aptoide already disabled but still present; MediaBrowser package/manifest/Activity/process exist; media.rtkplayer service exists; current storage mount layout; Home and HDMI 2 restored.
- **PENDENTE:** MediaBrowser file discovery, supported path, successful local playback, media.rtkplayer client state, actual OMX decoder use, FPS/drops/CPU/PSS during a clip, USB availability. No decoder claim is inferred from firmware libraries or service presence.
- No APK install, firmware/system write, partition operation, or launcher change occurred.

## Local AVC 720p30 benchmark preparation — blocked before transfer (2026-11-21)

- Re-read the autonomous test procedure and validated the device again: expected fingerprint/model/API; `PhilcoOS MainActivity` foreground, `mAlways=true`, source 17. `/sdcard/Movies` exists and lists no files; `/data` free space 3.1 GB.
- No existing video was found in project output. Two personal MP4 files were present elsewhere on the computer but were not selected because their format/content was not verified as synthetic/safe benchmark material. `ffmpeg`, `ffprobe`, MediaInfo/exiftool, and Python modules `cv2`, `imageio_ffmpeg`, `av`, `moviepy` are unavailable in the checked environment. No software was installed. Thus no qualified 720p30 test clip could be generated or validated.
- Created local evidence directory `output/media_benchmark_20261121_063337/` with device date, `/proc/meminfo`, `top`, launcher/mediaserver meminfo, resumed Activity and PlayingSource baseline. Home screenshot attempt via `adb exec-out screencap -p` failed (ADB exit status `4294967295`); no remote screenshot/file was created.
- Did not create a directory on TV, transfer a video, invoke Media Scanner, start MediaBrowser, or play anything. No trial video exists to pass integrity/access checks; subsequent phases C/D therefore did not run.
- Final queried state remains PhilcoOS Home / persistent default / HDMI 2. Aptoide remains disabled; no TV writes, install, firmware/system/partition changes occurred.
- **Status:** benchmark `BLOCKED` before generation by missing local encoder and verified safe existing test asset. Decoder, FPS, drops, playback CPU/PSS, and first frame remain `PENDENTE`; prior evidence that firmware contains an OMX component is not used to claim decode capability.

## Post-test diagnostic collection (2026-11-21 06:41 WGT)
- Collected logcat `main`, `system`, `events`, `crash`, `radio`, and combined output without clearing buffers. Saved Android state dumps, package records, `/proc/meminfo`, `df`, and `top` in `output/post_test_diagnostics_20261121_064151/`.
- No `FATAL EXCEPTION`, ANR, OOM, permission denial, or fatal signal appears in retained logs. The `crash` buffer is unsupported (`/dev/log/crash` absent); this is documented.
- 18 Toast suppressions for PhilcoOS occurred at 06:39:35.493–06:39:39.597 after Android's 50-toast cap. Nearby logs show Realtek volume/source/mute calls; event logs show TV/Home transitions and Settings opening/return. The triggering Toast text/action and the user's specific failed behavior are not recoverable from logs.
- Dalvik aborted JIT trace compilation twice with `No free temp registers`, reverting to interpreter. No crash/OOM; impact was not measured. HWC `FrameBufferHz` E-level lines are not independently confirmed faults.
- Memory snapshot: ActivityManager total RAM 456,492 kB, Free RAM 219,019 kB; PhilcoOS PSS 20,712 kB, MediaBrowser 9,456 kB, mediaserver 2,040 kB. `/proc/meminfo` MemFree 14,964 kB and Cached 77,264 kB; these are different accounting values. No memory failure observed.
- Final state: PhilcoOS MainActivity, `mAlways=true`, HDMI 2 (`PlayingSource=17`); QC03 package vCode 777. No configuration changed.
- Raw captures stay local. Reviewed excerpt: `output/post_test_diagnostics_20261121_064151/logcat_focus_redacted.txt`. Full assessment: `output/post_test_diagnostics_20261121_064151/DIAGNOSTICO_TECNICO.md`.
- **Conclusion:** Toast throttling is a confirmed UX/diagnostic issue; cause of unspecified functional errors remains unknown. No fix implemented.

## QC05-r2 — AV/HDMI and Mute device validation (2026-11-21)

- **Build/install:** `:source:philco-home:assembleRelease` succeeded. Installed as an app update on `smart_tv001` (Android 4.4.2/API 19); the pulled `/data/app` APK exactly matches `output/releases/philco-home-av-hdmi-mute-QC05-r2-20261121.apk`, SHA-256 `0EC29239C0205BDFDDC4A63B7A980C6D2012339FEDEFF53593FC9BE85E9AB3C0`. Package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`; `minSdkVersion=19`; v1 signing valid, v2/v3 absent, zip alignment verified. Certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa` matches QC03.
- **Source switching — DEVICE-VALIDATED (routing):** D-pad selected AV=5, HDMI 1=18, HDMI 2=17 individually. For each, `TvManager.getCurLiveSource()` and `Settings.System.PlayingSource` matched; `com.tsb.tv/.Tv_strategy` became resumed and its on-screen label showed the requested input. HOME returned to PhilcoOS. Final state: MainActivity foreground and HDMI 2 (`PlayingSource=17`). Screenshots: `output/qc05_r2_av.png`, `output/qc05_r2_hdmi1.png`, `output/qc05_r2_hdmi2.png`, `output/qc05_r2_final_home.png`.
- **Signal limit:** All three captures show the correct OEM source overlay but a black picture area. This confirms source routing and the TV Activity handoff, not usable picture from a connected external device; no active video source was visible during the test.
- **Mute — API/readback validated; acoustics pending:** On HDMI 2, `TvManager.setApMute(true)` and `setApMute(false)` returned normally. `AudioManager.isMasterMute()` read back `true` then `false`; the source volume was 0 before and after. The Quick Control Center displayed Mute then restored its unmuted label. No volume adjustment occurred. Speaker output was not acoustically observable with volume 0 and no active content, so physical silence is not claimed.
- **Runtime API finding:** This installed firmware does not expose `TvManager.getApMute()` (`NoSuchMethodException` on QC05-r1). It exposes `setApMute`, `AudioManager.setMasterMute(boolean,int)` and `isMasterMute()`. The r1 artifact was preserved and superseded; r1's attempted QCC session disabled its sliders due the absent getter. QC05-r2 uses the available APIs and the QCC loads normally.
- **Preserved artifacts:** QC03, QC04, A57 and EB256 were not modified. Both QC05 archives and the installed APK pull are retained. No firmware, `/system`, partition or bootloader changes.
- **Evidence logs:** `output/qc05_r2_mute_console_observation.txt` is the preserved terminal excerpt with both mute/unmute readbacks (not a raw logcat dump); `output/qc05_device_validation_focus.log` contains the retained source-switch traces. Captures remain local.
- **Remaining validation:** Have a known-good audible source at nonzero volume and confirm speaker silence/restoration by listening while retaining API-state monitoring. Do not infer acoustic success from `setApMute()` returning normally or from master-mute readback alone.

## QC05-r3 — OEM source handoff and Mute volume preservation (2026-11-21)

- **Code delta:** `MainActivity.java` now dispatches source change and OEM activity in the sequence supported by the two observed OEM paths: `TvManager.setSource(id)` → persist/read back `PlayingSource` → broadcast `com.realtek.TVSignalAlarm.SourceChange` → launch explicit `com.tsb.tv/.Tv_strategy` with `FLAG_ACTIVITY_NEW_TASK` → asynchronous `getCurLiveSource()` readback. It does not assume that `Tv_strategy` itself selects the input. If Mute changes the source-volume readback, the previous volume is restored using the Realtek `setStreamVolumeForL4300` bridge and verified before considering the operation successful.
- **Build/package:** `assembleRelease` succeeded. APK `output/releases/philco-home-av-hdmi-mute-QC05-r3-20261121.apk`; package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`, minSdk/targetSdk 19; 1,210,280 bytes; SHA-256 `A081CC53BC9695657AD39A12032B05AD4BDA0F4F50EBBD098790B1272A370A30`. `zipalign -c 4` passed; `apksigner` verifies v1=true and v2/v3=false. Certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`.
- **Install:** `adb install -r` returned `Success` on `192.168.0.71:5555` (Android 4.4.2/API19). Pulled installed APK `output/philco-home-installed-QC05-r3.apk` hash exactly matches release hash.
- **Source test (D-pad, individually):** TV aberta=27, Cabo=1, AV=5, HDMI1=18, HDMI2=17. For each, `PlayingSource` and the app’s delayed `TvManager.getCurLiveSource()` readback matched the requested ID; `com.tsb.tv/.Tv_strategy` became resumed. `TvManager.setSource` returned boolean `false` on this firmware despite matched independent readback, so that boolean was not used as a failure criterion. Hardware HOME returned to PhilcoOS each time. Final source HDMI2=17, `MainActivity` resumed, persistent HOME preference `mAlways=true`.
- **Mute test:** On HDMI2 with reported source volume 0, QCC showed the corresponding label after each operation. `TvManager.setApMute(true)` and `setApMute(false)` returned normally; `AudioManager.isMasterMute()` read true then false; source volume was 0 before/after. QCC ended unmuted. API/state transition is confirmed; physical acoustic silence/restoration is not confirmed because there was no audible content and volume was zero. The new restore branch was not exercised because no volume change occurred.
- **Crash/signal limit:** No PhilcoOS fatal exception appeared in the retained filtered log. No active AV/HDMI picture source was connected/verified; source routing is not proof of visible external video. No audible test signal was used.
- **Final state/evidence:** PhilcoOS Home, HDMI2=17, unmuted, `mAlways=true`. Focused log and D-pad observations: `output/qc05_r3_device_validation.log`. No firmware, `/system`, bootloader, HAL, or original OEM APK changed.

## QC05-r4 — STREAM_MUSIC mute alternative (2026-11-21)

- **Implementation:** QCC now uses `AudioManager.setStreamVolumeForL4300(3, index, 1, true)` when available; stream type 3 is `STREAM_MUSIC`. `TSB_Tv.apk` calls the same OEM method with literal 3, and framework disassembly shows the implementation chooses the output device for that stream and updates its `VolumeStreamState`. This is a per-media-stream volume-zero alternative, not native amplifier mute.
- **Persistence and safety:** Prior positive media index is synchronously saved to SharedPreferences before setting index 0. Repeated mute/re-enforcement never overwrites it. UI state updates only after index readback. Unmute requires and restores the exact saved index before clearing the active flag. Home resume recovers persisted mute; the volume-change receiver is retained while muted and reasserts zero on volume changes. Volume up/down is consumed while Home has focus and mute is active.
- **Build:** `assembleRelease` successful. Final artifact `output/releases/philco-home-av-hdmi-mute-QC05-r4-20261121.apk`, 1,210,894 bytes, SHA-256 `3D43327516091ED0AF5A0580F0BF5B4F6FE62F1ECB0A266A9DF4DFA5934C4A57`; package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`, min/target API 19. v1 signature valid; v2/v3 absent; zipalign valid; certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`.
- **Install:** `adb install -r` returned `Success` on `192.168.0.71:5555`. QC05-r3 release and installed pull remain intact. An earlier r4 candidate was retained as `output/releases/philco-home-av-hdmi-mute-QC05-r4-initial-20261121.apk` (SHA-256 `867EB4DE8B6A330E99B5F0D68A2CC18962F9D6DAE8EE7ABE237E43F4A26CF705`).
- **Runtime readback:** Baseline before the controlled sequence was index 25. Test cycle used current index 26: QCC set 26→0 and the OEM readback returned 0; Volume+ during active mute was re-enforced to 0 without replacing saved 26; unmute restored exact 26. With mute persisted at 0, `am force-stop` + relaunch logged recovery `saved=26, before=0, readBack=0`; QCC then restored 26. Final test volume was returned to pretest 25. QCC/Home stayed responsive; no filtered fatal exception found. TV reboot recovery is implemented but was not physically exercised.
- **Acoustic result:** The agent-run session lost SmartTube audio focus when Home/QCC opened, so its own cycle did not directly measure continuing playback. The user subsequently reported that the media-volume mute workaround physically worked. Classify this as **user-confirmed operation**, not instrumented acoustic measurement; it is not native amplifier mute.
- **Final device state:** PhilcoOS MainActivity resumed/default, HDMI 2 active, `STREAM_MUSIC` index 25 and unmuted. Logs/captures: `output/qc05_r4_acoustic_test.log`, `output/qc05_r4_check.png`, `output/qc05_r4_panel.png`. Firmware, `/system`, HAL and OEM APKs were not modified.

## QC05-r4.3 — Large mute OSD (2026-11-21)

- **UI change:** Added a custom static mute badge at the lower-left corner of the Home: large speaker-with-x glyph in a compact glass tile. It is visible when `STREAM_MUSIC` is muted/at zero and hidden when audio returns. The top-right volume indicator also keeps its small mute symbol. No animation or dependency was added.
- **Build/install:** `output/releases/philco-home-QC05-r4.3-mute-icon-20261121.apk`, 1,211,731 bytes, SHA-256 `9229049E6BF86FE123E99E82F4476051BFF7EBB993174C590D68B8C91497F160`. v1 signature verified, v2/v3 absent; zipalign valid; package/version/API 19 unchanged; signing certificate remains `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`. Installed with `adb install -r`; pulled installed APK SHA matches release.
- **Device visual check:** 1280×720 capture `output/qc05_r43_muted.png` shows the large lower-left OSD and small top indicator while the persisted mute is active. `STREAM_MUSIC` readback is zero; HDMI 2 remains selected; PhilcoOS MainActivity is foreground. The user's later feedback reports the mute workaround physically works. Existing r4 and r4.1/r4.2 candidate artifacts were preserved.

## QC05-r4.4 — Mute OSD optical centering (2026-11-21)

- Shifted the speaker/x silhouette 2dp left inside the badge to center its combined visual weight; badge size and location are unchanged.
- `assembleRelease` passed. Artifact `output/releases/philco-home-QC05-r4.4-mute-osd-centered-20261121.apk`, 1,211,716 bytes, SHA-256 `2F37F702230BA3A2B8BA0F6DEBBCB1C693FD5FD73B85454DE28CB5B0990DB20C`; v1 signing valid, v2/v3 absent, zipalign valid, API 19. Installed with `adb install -r`; pulled `/data/app` APK hash matches exactly.
- Capture `output/qc05_r44_muted.png` shows the centered badge at 1280×720 while mute is active. Final state remains muted at index 0, HDMI2=17, PhilcoOS Home resumed. This was a visual-only iteration; no audio state was changed.

## QC05-r4.5 — Darker Home cards and header actions (2026-11-21)

- Raised opacity of the shared glass tile/button fills to dark navy: dock cards (SmartTube, AR, Cabo, AV, HDMI 1/2, Apps, Ajustes) and the top `Paisagens` / `Ajustes` actions now read clearly over the bright wallpaper. Accent colors, wallpaper, spacing and focus ring are unchanged.
- `assembleRelease` passed. Artifact `output/releases/philco-home-QC05-r4.5-opaque-cards-20261121.apk`, 1,211,715 bytes, SHA-256 `CCED15E1245A384DDC8EA427726DCE2202A68CCE4F18DC605F8C211F346B389E`; v1 valid, v2/v3 absent, API19. `adb install -r` succeeded. Visual capture: `output/qc05_r45_muted.png`.
- Final TV state after screenshot: PhilcoOS Home, HDMI2=17, mute retained at `STREAM_MUSIC=0`. No audio setting or source was modified by this visual change.

## QC06-r2 — Weather / clock visual update (2026-11-21)

- `assembleRelease`: **PASS** (`BUILD SUCCESSFUL`, 45 tasks; 8 executed, 37 up-to-date). Gradle emitted environment warnings about Android SDK XML/metrics and Java deprecation only.
- APK: package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`, min/target API 19; v1 valid, v2/v3 absent, `zipalign -c 4` passed. SHA-256 `7745F02D77F64F676B515DE30718B48D429A44B7BDBDE9700D01A9B5B137A01C`.
- TV `192.168.0.71:5555`: `adb install -r` returned **Success**. Pulled installed APK hash equals the release; MainActivity resumed. Capture `output/qc06_weather_clock_r2.png` (1280 × 720) showed real 26°C / Nublado and the matching cloud glyph. The partly-sunny state was not captured because the actual weather was overcast.
- No matching fatal exception appeared in the last 600 log lines queried. This was a visual smoke check, not a prolonged stability or D-pad regression test. QC05-r4.5 rollback: `output/releases/philco-home-QC05-r4.5-opaque-cards-20261121.apk`; exact pre-update installed APK: `output/backups/philco-home-installed-before-weather-clock-QC06.apk`.

## QC07-r3.7 — Home clock and iOS-inspired weather card (2026-11-21)

- Home clock now uses the bundled static Lato Medium font (94sp); the screensaver clock uses the same face for consistency. Weather was restructured within a 350×96dp dark glass card: live weather glyph, city and condition grouped on the left, prominent live temperature on the right, provider attribution retained. No forecast values were invented and no library was added. The implementation uses API 19-compatible Android views.
- Build: `assembleRelease` **PASS**. APK `output/releases/philco-home-QC07-r3.7-ios-weather-clock-lato-20261121.apk`, 1,780,835 bytes, SHA-256 `3C5DF979FE51D327886A68AA989BCC7A47535823407B8E0C2201F52923382088`. Package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`, min/target API 19. `zipalign` passed; apksigner verified v1 true, v2/v3 false; certificate fingerprint unchanged (`b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`).
- Installed with `adb install -r` on `192.168.0.71:5555`; installed APK pulled to `output/qc07-r3.7-installed.apk` and SHA matches release exactly. `MainActivity` resumed. Visual capture `output/qc07-r3.7-home-1280x720.png` (1280×720, SHA-256 `325ABE4E7437372C1ACAB6BF73DCDA3FE0801186E3DFFFD67C535B8475F6B6C7`) shows 28°C, Joinville, Nublado, Wi-Fi Forte and HDMI 2 active. No fatal exception found in the queried recent log tail. This is a visual/device smoke check, not a prolonged stability test.
- Prior QC07 reference/rollback APKs were preserved. No source input, audio setting, firmware, `/system`, HAL, partition or bootloader was changed. Existing device media volume readback remained 25.
- **Next action:** user visual review. Then, if still desired, perform one evidence-based 720p30 SmartTube stutter capture; do not modify decoder/firmware absent measured cause.

## QC07-r3.9 — GMT−3 clock, full date, aligned Home, compact weather (2026-11-21)

- **Changes:** Weather card is 312×88dp (physical 312×88px at the TV's 160dpi), right-aligned with the Home action/status edge. Weather uses seven pre-rasterized 96×96 transparent Material Symbols Rounded PNGs (total 12,325 bytes): sunny, partly cloudy, cloud, rain, thunderstorm, snow, fog. The same live weather icon is used on Home and the screensaver. Date under the clock is `sábado • 21/11/2026`, weekday spelled out; date uses Lato Medium and an 8dp optical inset to align its visible left edge with the clock digits. Dynamic OS/source label is right-aligned to the same edge as actions, Wi-Fi/volume status and weather.
- **Time correction:** On reconnect, the TV reported `America/Godthab`; its epoch was accurate (`22:51 UTC`, `19:51` local UTC−3). `auto_time=1`, `auto_time_zone=1`. The literal `GMT-03:00` property was interpreted with the opposite sign by this KitKat build (observed `01:51`), so it was immediately corrected to IANA `Etc/GMT+3` (fixed UTC−3); `auto_time_zone` was set to `0`, while `auto_time` stayed `1`. Subsequent device `date` was 19:55–19:57 local against 22:55–22:57 UTC, and Home/saver screenshots showed the matching local time. No manual epoch adjustment was made.
- **Build / install:** `assembleRelease` PASS. APK `output/releases/philco-home-QC07-r3.9-gmt3-weather-icons-alignment-20261121.apk`, 1,792,606 bytes, SHA-256 `860F128325E17A9A5D5CD301E2D3FC32714C48AABA9CC4A586D3C90D1EA85EE8`. Package/version unchanged (`com.philco.ultra777.home`, code 777, `1.0.0-ultra777`), min/target API 19. Zipalign passed; v1 signature true, v2/v3 false; original signing certificate unchanged. `adb install -r` succeeded. Installed pull `output/qc07-r3.9-installed.apk` has the exact same SHA-256.
- **Device validation:** Physical display 1280×720, density 160dpi. Captures: Home `output/qc07-r3.9-home-1280x720.png` (SHA-256 `600B38ABE2E059A12A7E138EDEAF94661F518BB98BA475A6BE04F6BA5547F71F`), screensaver `output/qc07-r3.9-screensaver-1280x720.png` (SHA-256 `1B4D0A1E666E2182779C04CC3C7B4E591EE1275F30F4BE34D6FEFB81BFC8F284`), Home after Back `output/qc07-r3.9-home-after-screensaver-1280x720.png` (SHA-256 `31771DBB99D91D7BFED4874263A6B82086DC13FCF9FDFED26498EE012F08CF49`). The Home showed 19:54/19:57, `Sábado • 21/11/2026`, Joinville, 24°C/Nublado, and `HDMI 2 ATIVA`; manual screensaver entry and Back exit worked. MainActivity was resumed afterward, music volume remained 25, and the queried 500-line logcat tail had no `FATAL EXCEPTION` for PhilcoOS. This is a focused UI smoke test, not prolonged stability or cold-boot persistence validation.
- **Power state:** The TV was absent from ADB at the start, then user powered it back on and it reconnected. The prior five-hour idle shutdown remains a plausible explanation because code requests Realtek standby after five hours plus a 30-second countdown, but the cause of the earlier off state is unconfirmed. This test did not trigger actual panel/TV shutdown.
- **Workspace:** `git status` reports `C:\PhilcoOS` is not a Git repository; no commit/branch is available. QC07-r3.8, r3.7, earlier QC07 references, A57 and EB256 remain preserved. No firmware, `/system`, HAL, partition, or bootloader changes were made. Next action: user reviews the Home/saver captures; cold-boot timezone persistence remains untested.

## QC07-r3.11 — Screensaver date/weather optical centering (2026-11-21)

- User observed the spelled-out screensaver date and weather row appearing slightly right of center. Applied a shared 8dp left optical offset to those two rows only; the clock and Home layout are unchanged. Disabled child clipping on the centered screensaver group so the first letter of “Sábado” remains visible after the offset. The first candidate r3.10 exposed clipping; r3.11 fixes it and supersedes r3.10.
- Build `assembleRelease`: PASS. Release `output/releases/philco-home-QC07-r3.11-screensaver-centering-clipfix-20261121.apk`, 1,792,620 bytes, SHA-256 `5943710DF151F44EAAAA8BBE44225699BC848774F3960EDDBBCCDE48EA0DC946`; min/target API 19, v1 signature valid, v2/v3 absent, zipalign passed. Installed on `192.168.0.71:5555`; pulled APK hash matches release.
- Device capture `output/qc07-r3.11-screensaver-1280x720.png` (SHA-256 `7F1D4558CBAD3D096CB00898D505573338656F346B93FD478363109B86F9E61E`) shows the complete date and centered-looking weather row. Back returned to Home, HDMI 2 remained displayed active, MainActivity resumed, `STREAM_MUSIC=25`; queried device time is 20:09 in fixed UTC−3 and `auto_time=1` / `auto_time_zone=0`. Home-after-saver capture: `output/qc07-r3.11-home-after-saver-1280x720.png` (SHA-256 `AC61EAAFDDC580D33742E89ABFBAADDB635DFB7D6E947B5DE4B67B15CCBCCE8E`).
- QC07-r3.9 remains the prior known-good rollback; r3.10 is preserved as a superseded candidate. No firmware/system partition changes.

## QC07-r3.12 — screensaver date clipping (2026-11-21)
- **IMPLEMENTED:** removed the saver date's -8dp view translation and added 8dp horizontal padding, leaving clock, wallpaper, weather, and other Home layout unchanged.
- **COMPILED / OFFLINE_VALIDATED:** `assembleRelease` passed. Release: `output/releases/philco-home-QC07-r3.12-saver-weekday-clipfix-20261121.apk`, SHA-256 `235FB14DE996BCD5B78DBF0E83D452B89A76402030B1E92FA9697A3DA88DF688`; API 19-compatible package, v1 signature verified, zipaligned, existing signer retained.
- **INSTALLED / DEVICE_VALIDATED / VISUALLY_CONFIRMED:** installed on `192.168.0.71:5555`; installed pull hash matches. `output/qc07-r3.12-screensaver-final-1280x720.png` visibly shows the complete long Portuguese weekday/date. Back returned to PhilcoOS Home; `output/qc07-r3.12-home-after-saver-1280x720.png` shows HDMI 2 active. No matching PhilcoOS fatal exception/ANR in the queried recent log tail.
- Brightness slider's lack of visible image change is still user-reported and unverified as a specific code-path failure; delegated to Antigravity for focused offline diagnosis before a Codex hardware test.

## Installed baseline capture — brightness investigation (2026-11-21)
- Read-only ADB identification and collection from the real TV; exact May 2017 fingerprint and per-file hashes are recorded in `analysis/installed_tv_20170531/README.md`.
- Registered Binder 	v service confirmed. Generic /sys/class/backlight missing; this alone does not establish absence of proprietary brightness control.
- No settings changed, no log buffers cleared, no APK installed, no firmware changed.


## Music Library device smoke test — 2026-11-22

- `assembleRelease`: BUILD SUCCESSFUL. APK `source/philco-home/build/outputs/apk/release/philco-home-release.apk`, SHA-256 `5A32AB299081F87B344F3293B0FDA44CCF2725EA2DB7462114C4AA70C906B604`.
- Device `192.168.0.71:5555` identified as `smart_tv001`, Android 4.4.2. Existing launcher APK backed up to `output/backups/philco-home-before-music-library-20261122.apk` (SHA-256 `6BDB9BE8D4A188333CDB52AA0C0DC06A8E40A6844109C271E40EFED377822A5F`). `adb install -r` returned `Success`.
- Explicit `MusicActivity` launch succeeded. Screen capture: `output/captures/music-library-20261122.png`. MediaStore query returned `No result found`; app displayed “Nenhuma música local encontrada”.
- D-pad navigation and button key events were exercised without a fatal exception. Back/Home returned to `MainActivity`. Recent logcat query found no PhilcoOS fatal/ANR.
- Playback, pause/resume with audio, next/previous on actual tracks, seek, and continuation after leaving MusicActivity remain unvalidated because the TV has no indexed music. The current MusicActivity also has no seek control; seek can only be invoked through the service/controller API. No firmware or OEM launcher changes.

## Music Engine follow-up — MediaStore/WAV verification (2026-11-22)

- **MediaStore cause:** `/sdcard/Music` was empty and the initial index had zero music rows. `/sdcard/Download` contained only screenshots; no audio was found on `/storage/udisk`. Manifest has `READ_EXTERNAL_STORAGE`; install-time grant on API 19. Query filters `IS_MUSIC != 0` and returns content URIs.
- Generated three 12-second, 22.05 kHz mono PCM WAV test tones (440/660/880 Hz) in `output/music-test-wavs/`, copied to `/sdcard/Music/`, then broadcast `android.intent.action.MEDIA_SCANNER_SCAN_FILE`. MediaStore indexed IDs 192–194, all duration 12000ms and `audio/x-wav`.
- MusicActivity listed all three tracks with names, unknown artist and duration. Selecting 440Hz entered playback path: MediaPlayer opened `/data/media/0/Music/Philco_Test_440Hz.wav` and requested `STREAM_MUSIC` audio focus. Capture: `output/captures/music-playing-test-20261122.png`.
- D-pad and play/pause/next/previous key/tap attempts were sent; however the Activity has no seek bar or playback-state/progress controls, so pause/resume/track transitions were not independently proven from UI state. User physically confirmed audible output from the 440Hz test tone on the TV. Seeking remains untestable from UI. Continuation after leaving Activity was not confirmed by a reliable playing-state readback.
- No code changed or rebuilt. Return-to-Home was requested at end; verify current foreground before further test. WAVs remain on TV for repeatable testing.


## Music Engine control/continuity follow-up — 2026-11-22

- Three indexed WAV tracks remained available. Selecting 880Hz updated the MusicActivity title to `Philco_Test_880Hz`; Next from 660 advanced to 880 and Previous returned to 660. Captures: `output/captures/music-next-880-20261122.png`, `output/captures/music-prev-660-20261122.png`.
- Pause/Resume taps were issued. AudioFlinger showed the track with changed state/flags across the two snapshots, but the legacy dump exposes no unambiguous play/pause label and the Activity has no state indicator; mark pause/resume **not conclusively verified**. The user previously confirmed audible 440Hz output; no new human acoustic confirmation for 660/880 in this run.
- Exit test: after HOME, `dumpsys activity services` showed `MusicPlaybackService` still `startRequested=true`; AudioFlinger still showed its 22050Hz output track active. Reopening MusicActivity showed `Philco_Test_880Hz`, confirming service state/source continuity across Activity exit.
- Seek remains unavailable from the MusicActivity UI (no seek control); no source/build change or reinstall was made. Current foreground left as MusicActivity.

## Music Engine — controles de reprodução e seek (2026-11-22)

- **IMPLEMENTED:** `MusicActivity` agora exibe estado (Preparando/Tocando/Pausado/Parado/erro), tempo decorrido/duração e SeekBar. Atualiza a posição a cada 500 ms; sincroniza ao vincular/reabrir a Activity. Toque e D-pad enviam seek ao `MediaPlayer`. `MusicPlaybackService.getState()` publica uma leitura atual de posição/duração no `PlayerStateRepository`.
- **COMPILED:** `assembleRelease` passou. APK `output/releases/philco-home-music-controls-dpad-20261122.apk`, SHA-256 `67D87D0A480F7A5A906A054D7F59D3B5BDF5712BDF28B9A2661CD0D47D97EE67`. Package `com.philco.ultra777.home`, versionCode 777, versionName `1.0.0-ultra777`, target API 19.
- **INSTALLED / DEVICE_VALIDATED:** ADB `192.168.0.71:5555`; `adb install -r` retornou `Success`. APK instalado foi puxado e seu SHA-256 coincide com o build. Certificado é igual ao baseline instalado. Backup baseline: `output/backups/philco-home-before-music-controls-20261122.apk`; APK instalado após atualização: `output/backups/philco-home-music-controls-installed-20261122.apk`.
- **DEVICE_VALIDATED:** 3 WAVs MediaStore disponíveis. Estado `Tocando` e contador avançando; pausa exibiu `Pausado` e a posição ficou congelada por 2 s; retomada exibiu `Tocando` e a posição avançou. Seek por toque saltou aproximadamente a 7 s. Seek via D-pad moveu a posição para trás (capturas mostram ~0:11 para ~0:10). Ao sair para Home e reabrir, faixa 880 Hz, estado `Tocando` e posição ~0:05 foram restaurados pelo serviço. Serviço continuou vivo após sair da Activity.
- Capturas em `output/captures/`: `music-controls-playing-20261122.png`, `music-controls-paused-verified-20261122.png`, `music-controls-pause-stable-20261122.png`, `music-controls-resume-verified-20261122.png`, `music-controls-seek-verified-20261122.png`, `music-controls-dpad-seek-verified-20261122.png`, `music-controls-reopen-sync-20261122.png` e `home-after-music-controls-20261122.png`.
- Logcat consultado após os testes: nenhuma ocorrência correspondente de `FATAL EXCEPTION` ou ANR do launcher no resultado da busca. O usuário pediu volume moderado; não aumentei o volume. A confirmação acústica deste ciclo não foi solicitada; a saída de 440 Hz havia sido ouvida pelo usuário no teste anterior.
- Home voltou para `MainActivity` e continua como HOME padrão. A captura final mostra HDMI 1 ativo; este trabalho não alterou a fonte e não a restaurou.
- **Limitação/Próxima ação:** reprodução longa com arquivos musicais reais e confirmação auditiva de pausa/retomada continuam pendentes. Testar usando volume baixo; não é necessário mexer em firmware ou no decoder.

## MusicActivity — visual premium dourado (2026-11-22)

- **IMPLEMENTED:** redesenho isolado em Java nativo. Reutiliza `res/drawable-nodpi/wallpaper_02.jpg` (Jesus e ovelha), decodificado em RGB_565, com scrim estático escuro. Layout 16:9 com música atual em destaque, biblioteca em painel grafite, controles grandes e foco/seek dourados (#D4AF37). Metadado MediaStore `<unknown>` é apresentado como “Artista desconhecido”.
- **Desempenho/navegação:** sem bibliotecas, assets novos, blur ou animação. A posição continua atualizando a cada 500 ms; atualização da lista limitada a mudanças de faixa para não interromper a navegação nem redesenhar toda a biblioteca continuamente. D-pad focou faixas e controles.
- **COMPILED / INSTALLED / VISUALLY_CONFIRMED:** `assembleRelease` BUILD SUCCESSFUL; instalado com `adb install -r` no dispositivo `192.168.0.71:5555`. Screenshot real: `output/captures/music-premium-ui-final-1280x720-20261122.png`, confirmado 1280×720. Capturas de foco: `output/captures/music-premium-ui-focus-20261122.png`, `music-premium-ui-controls-focus-20261122.png`.
- APK `output/releases/philco-home-music-premium-ui-r2-20261122.apk`, SHA-256 `219EE9A4E92B4E546457539E33CB2B75883630F4E0F9253B075C69CDDD6FAFCC`; cópia puxada da instalação tem hash idêntico em `output/backups/philco-home-music-premium-ui-installed-20261122.apk`. API mínima/alvo 19; versionCode 777 / `1.0.0-ultra777`. Rollback pré-redesign: `output/backups/philco-home-before-music-premium-ui-r2-20261122.apk`.
- **Limitação:** por pedido do usuário para evitar ruído, não iniciei reprodução nesta validação visual; título/estado dinâmicos foram validados no trabalho anterior, não nesta build visual. Não alterei Home, dock ou fonte de entrada. Sem exceções fatais/ANR correspondentes na consulta de logcat feita após abrir a tela.

## Home weather card — referência de gradiente e hierarquia (2026-11-22)

- **IMPLEMENTED:** cartão da Home passou de 312×88dp para 432×132dp e usa composição em duas zonas inspirada na referência: condição/ícone à esquerda; cidade, rótulo "Agora" e temperatura real à direita, com divisor e atribuição Open-Meteo. Gradiente fixo por código WMO (âmbar/sol, teal/parcial, azul/chuva, grafite/nublado); foco D-pad usa contorno dourado. Reutiliza PNGs de clima existentes, sem dependências nem novas imagens.
- **COMPILED / INSTALLED / DEVICE_VALIDATED / VISUALLY_CONFIRMED:** `assembleRelease` passou e `adb install -r` retornou Success na TV `192.168.0.71:5555`. Captura física `output/captures/home-weather-card-1280x720-20261122.png` (1280×720) mostra Garoa, Joinville, 21°C e atribuição Open-Meteo. APK instalado puxado da TV confere com o release.
- APK `output/releases/philco-home-weather-home-card-20261122.apk`, SHA-256 `925EFC9E8FB3BE56127009300065705FDFA737DD923A1E7001D9F4698E130D50`; instalado: `output/backups/philco-home-weather-home-card-installed-20261122.apk`. Rollback anterior: `output/backups/philco-home-before-weather-home-card-20261122.apk`.
- O arquivo Kimi recebido foi somente inspecionado, sem extrair/importar código executável. O `WeatherProviders` do pacote escolhe `DemoWeatherProvider` por padrão e as capturas exibem `DADOS DEMO`; portanto o módulo de tela cheia não foi integrado. O ZIP também contém APK, keystore de assinatura, `signing.properties` e `local.properties`; nenhum foi usado.
- **Próxima ação, ainda não iniciada:** descrever auditoria de compatibilidade do módulo Kimi, trocar o provider demonstrativo por Open-Meteo real como padrão apenas após revisão, validar campos com dados disponíveis e só então integrar/testar. A MusicActivity permanece como estava; Home/dock não foram redesignados além do cartão de clima.


## Home climate 3D icons, dock safe viewport and screensaver alignment — 2026-11-22

- **IMPLEMENTED:** Reused three 3D weather glyphs (rain, partly cloudy, cloud) cropped from the supplied design reference `docs/design/reference/weather-card-reference-20261121.png`, saved as 160×160 transparent PNGs in `source/philco-home/res/drawable-nodpi/`. Combined size: 72,195 bytes. Weather code mapping is shared by Home and Screensaver; other conditions retain existing assets. No network-loaded images or dependencies.
- **IMPLEMENTED:** Expanded only the dock viewport to full screen width with negative side margins and retained 64dp safe insets inside the row. Kept all eight 130×134dp tiles, 16dp spacing, ordering, focus animation and D-pad behavior. This corrected the first candidate, whose 24dp row inset narrowed the viewport and clipped the rightmost tile.
- **IMPLEMENTED:** Screensaver date now has zero horizontal padding and left gravity, aligning the start of the full date with the clock across weekday/date lengths.
- **COMPILED:** `assembleRelease` succeeded. APK `output/releases/philco-home-weather3d-dock-saver-align-r2-20261122.apk`, SHA-256 `5D12F95C21B974F3910FE4687CB01944052FB9FAC9D370908B5846461C5CAF2B`; API target 19; v1 signature verified, v2/v3 absent; zipalign check passed.
- **INSTALLED / DEVICE_VALIDATED / VISUALLY_CONFIRMED:** Installed on `192.168.0.71:5555` (`smart_tv001`). APK pulled from `/data/app` matches the release SHA. Home captures show both focused edge tiles fully visible, 64px outer safe margin, and 3D drizzle glyph; Screensaver capture shows the full Sunday date starting at the same x-coordinate as the clock and the same weather glyph. Back returned to PhilcoOS Home; preferred Home remains `com.philco.ultra777.home/.MainActivity` (`mAlways=true`). Source remained HDMI 1.
- Captures: `output/captures/home-dock-settings-safe-r2-20261122.png`, `home-dock-smarttube-safe-r2-20261122.png`, `screensaver-date-align-r2-20261122.png`.
- Rollback before final update: `output/backups/philco-home-before-dock-safe-r2-20261122.apk` (the prior interim candidate, SHA `F66D9F31CD606C4D588D2A2CD4F961B667E4FC699498C03BA7A0259B48FBAA07`). Prior known Home baseline is preserved separately at `output/backups/philco-home-before-weather3d-dock-saver-align-20261122.apk` (SHA `925EFC9E8FB3BE56127009300065705FDFA737DD923A1E7001D9F4698E130D50`).
- **Limitation:** No camera-based panel overscan measurement was available; the 1280×720 ADB captures confirm the Android viewport and focus outline are not clipped. A preliminary OK key opened SmartTube's recommendations because SmartTube held initial focus; no video playback was started, and Home was restored.


## Dock endpoint focus correction — final r5 (2026-11-22)

- User feedback caught a regression in experimental r4: aligning the row while scrolling caused the opposite endpoint tile to be partly off-screen. r4 remains preserved as a diagnostic artifact; it is not the final release.
- Final correction keeps the dock fixed and all eight cards present. SmartTube and Settings retain their clear white focus outline but no longer scale beyond their 130×134dp bounds; the other six cards keep their existing 1.045 focus scale. Layout, order and 16dp spacing are unchanged.
- **COMPILED / INSTALLED / DEVICE_VALIDATED / VISUALLY_CONFIRMED:** `assembleRelease` passed, v1 signature verified, v2/v3 absent, zipalign passed. Installed on TV `192.168.0.71:5555`; pulled installed APK SHA matches. Focused SmartTube and focused Settings screenshots both show the entire card, all eight cards remain in a fixed row, and Home remains the preferred launcher (`mAlways=true`). Source remains HDMI 1.
- Final APK: `output/releases/philco-home-weather3d-edge-focus-fixed-20261122.apk`; SHA-256 `53E294BBB988F4C1D04B75A33A046525505842CB28A80A79E7CB6939958430DC`. Installed pull: `output/backups/philco-home-dock-edge-focus-fixed-installed-20261122.apk`. Rollback immediately before final update: `output/backups/philco-home-before-edge-focus-fixed-20261122.apk` (r4, SHA `71D6A4ADE4011BC6A1EF63191712224B921C5FB29B042AD99D12135577710226`). Earlier stable r2 and known weather baseline remain preserved too.
- Captures: `output/captures/home-smarttube-edge-focus-fixed-20261122.png`, `home-settings-edge-focus-fixed-20261122.png`, `home-final-weather-dock-r5-20261122.png`. The earlier Screensaver date alignment capture remains `output/captures/screensaver-date-align-r2-20261122.png`; r5 did not change that code.
- **Limitation:** ADB screenshots verify the 1280×720 Android composition; no camera measurement of optical panel overscan was made. No firmware or `/system` writes were made.


## Home date separator refinement — 2026-11-22

- Changed only the Home date separator from the large bullet `•` to the smaller middle dot `·`; format remains full weekday and `dd/MM/yyyy`, with position and font unchanged.
- `assembleRelease`: BUILD SUCCESSFUL. v1 signature verified; v2/v3 absent; zipalign passed. Installed with `adb install -r` on `192.168.0.71:5555`; pulled APK hash matches release. Home remains preferred (`mAlways=true`).
- Final APK: `output/releases/philco-home-home-date-dot-small-20261122.apk`; SHA-256 `E6D737E594F3831D85EE5E7C754C55095CE469F0D9A73D9C346AF76E801A1F8A`. Installed copy: `output/backups/philco-home-home-date-dot-small-installed-20261122.apk`. Rollback: `output/backups/philco-home-dock-edge-focus-fixed-installed-20261122.apk`.
- Screenshot: `output/captures/home-date-dot-small-20261122.png`; visually confirms `Domingo · 22/11/2026` and unchanged alignment.

## Screensaver weather icon simplification — 2026-11-22

- **State:** IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED.
- **Change:** Screensaver now uses the existing lightweight 2D white weather PNG mapping; the Home card keeps its existing 3D assets. For the TV's live WMO drizzle condition (Garoa), the Screensaver displayed the minimalist 2D rain-cloud glyph. No other UI, timer, Music Engine, or firmware behavior was changed.
- **Source:** `source/philco-home/src/com/philco/ultra777/home/MainActivity.java` only. Existing icons under `source/philco-home/res/drawable-nodpi/` were reused; no new dependency/assets.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; package `com.philco.ultra777.home`, versionCode 777 / versionName `1.0.0-ultra777`, min/target API 19. v1 signature verified; v2/v3 absent; zipalign passed.
- **Release:** `output/releases/philco-home-screensaver-weather-2d-20261122-standby-weather-2d.apk`, 1,877,751 bytes, SHA-256 `86D9E2B5A85F7E39C7DA551B539511EEE5CED5C7E6D4ED6833A3412E55F4758E`.
- **Rollback:** pre-install APK preserved at `output/backups/philco-home-installed-before-weather2d-20261122.apk`, SHA-256 `E6D737E594F3831D85EE5E7C754C55095CE469F0D9A73D9C346AF76E801A1F8A`.
- **Device:** `smart_tv001` (`192.168.0.71:5555`), Android 4.4.2. Installed APK pull matched release SHA. PhilcoOS remains preferred Home (`mAlways=true`), Home foreground after returning with BACK; source HDMI 1.
- **Evidence:** `output/captures/screensaver-weather-2d-20261122.png` (1280×720, Screensaver, Garoa); `output/captures/home-weather-3d-preserved-20261122.png` (1280×720, Home retained 3D weather card). D-pad reached Standby; BACK returned to Home. No `FATAL EXCEPTION` or launcher ANR match in the inspected last 600 logcat entries.
- **Build note:** Gradle reported a local analytics-settings access warning and SDK XML version warning; build completed successfully.
- **Not tested/changed:** no weather conditions other than the live garoa state were physically displayed in the Screensaver. No changes to 777 Music, idle dimming/shutdown timers, source selection, or firmware.
- **Workspace:** no Git repository, branch, or commit is available.

## Home weather condition alignment — 2026-11-22

- **State:** IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED.
- **Change:** centered the condition label above the weather illustration in the left panel and enlarged the existing Home icon from 70dp to 84dp. The city, current temperature, attribution, card bounds, gradients, dock, and Screensaver mapping remain unchanged.
- **Source modified:** `source/philco-home/src/com/philco/ultra777/home/MainActivity.java`. Source backup: `output/backups/MainActivity-before-home-weather-align-20261122.java`.
- **Assets:** reused existing 3D PNGs; no new generated assets or dependencies. The 3D PNG shown in the physical capture is the current drizzle/rain icon. Other weather-code visuals retain their previous mappings.
- **Build/device:** `assembleRelease` succeeded; installed on `smart_tv001` (`192.168.0.71:5555`, Android 4.4.2). APK pull from `/data/app/com.philco.ultra777.home-2.apk` matches the release hash. Home returned to foreground and remains the established preferred launcher; HDMI 1 remains the displayed source.
- **Release:** `output/releases/philco-home-weather-card-centered-20261122.apk`, versionCode 777 / versionName `1.0.0-ultra777`, 1,877,681 bytes, SHA-256 `83709B89CA06C12DBC1AC5CF753ABB2A7B3B391732A5938D6C7F7CA69F4B0439`. v1 signature verified; v2/v3 absent; zipalign passed.
- **Rollback:** pre-install APK at `output/backups/philco-home-screensaver-weather-2d-before-home-weather-layout-20261122.apk`, SHA-256 `86D9E2B5A85F7E39C7DA551B539511EEE5CED5C7E6D4ED6833A3412E55F4758E`.
- **Evidence:** `output/captures/home-weather-card-centered-20261122.png` (1280×720). The condition and 3D rain art are centered together; city and 21°C remain in the right column. Inspected last 500 logcat lines: no matching launcher fatal exception or ANR.
- **Not tested:** other live weather conditions and their visual mappings; no camera-based panel overscan test. No changes to Music, idle timers, sources, or firmware. No Git repository/branch/commit is present.
- **Next exact action:** prepare/inspect a consistent lightweight 3D PNG set for the remaining Home weather states before changing their mappings; keep the Screensaver's 2D glyphs.

## Compact Home weather card and system launcher install — 2026-11-22

### Home weather card
- Reduced the card from 432×132dp to 360×112dp (about 29% less surface area). Kept right alignment, 3D weather PNG, centered condition, city, current temperature, attribution and gradient. Internal icon/divider/padding were resized only to prevent clipping.
- `assembleRelease`: BUILD SUCCESSFUL. APK installed and visually checked on the 1280×720 TV screen; hierarchy reports card bounds `[856,259][1216,371]`, exactly 360×112 pixels at 160dpi. Capture: `output/captures/home-weather-card-compact-20261122.png`.
- Release `output/releases/philco-home-weather-card-compact-20261122.apk`, versionCode 777 / `1.0.0-ultra777`, SHA-256 `CD4B2FC8BAB74300D6AE323EE544BF0BFEDF7BE5F28235E6B3613A9EFFD298F7`. Pulled installed APK hash matches.
- Previous APK backup: `output/backups/philco-home-weather-card-centered-before-compact-20261122.apk`, SHA-256 `83709B89CA06C12DBC1AC5CF753ABB2A7B3B391732A5938D6C7F7CA69F4B0439`.

### PhilcoOS installed as a system app
- Device: `smart_tv001`, Android 4.4.2 build `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`.
- ADB shell is root; `/system` is writable, SELinux is Enforcing, and the APK was assigned `root:root`, mode `0644`, label `u:object_r:system_file:s0`.
- Android 4.4 did not register the initial nested path under `/system/app/PhilcoOS/`; after placing the APK directly at `/system/app/PhilcoOS.apk` and rebooting once, PackageManager reports `codePath=/system/app/PhilcoOS.apk`, `pkgFlags=[ SYSTEM HAS_CODE ALLOW_CLEAR_USER_DATA ]`, and `pm list packages -s` includes `com.philco.ultra777.home`.
- The system APK SHA-256 equals the release: `CD4B2FC8BAB74300D6AE323EE544BF0BFEDF7BE5F28235E6B3613A9EFFD298F7`. Free `/system` space remains about 1.4GB.
- After reboot, PhilcoOS MainActivity resumed, `mAlways=true` remains set, Home screenshot captured at `output/captures/home-philcoos-system-app-20261122.png`. No matching launcher fatal exception or ANR in the last 800 logcat lines.
- Original Philco/Changhong launcher was not disabled or replaced; `com.haiersoft.launcherproject2995hk` remains installed and enabled. Exact APK pulled from TV and preserved at `output/backups/LancherProject2995-installed-tv-20261122.apk`, SHA-256 `98B88B8843AFE0D3910C9A334C532450F13ACEF37D78A93936F6C1CF38B91DD1`.
- **Limit:** system placement makes PhilcoOS preinstalled and resilient to ordinary app removal. It does not prevent someone from manually changing the preferred HOME app in Android settings; the OEM launcher stays as recovery. The initial unscanned duplicate at `/system/app/PhilcoOS/PhilcoOS.apk` remains; package manager uses the direct flat APK above.

### Deferred weather PNG reference
- Preserved the user's new 3D icon sheet at `docs/design/reference/weather-3d-icon-sheet-user-20261122.png` (SHA-256 `1378367B2BB004365E3AFF7F62640B1ABE056D3D19F178BDB8465C1A8111F28B`). It is a design reference only and was not added to the APK. Next UI task: inspect/crop suitable icons and map them to actual WMO conditions while keeping the Screensaver's 2D glyphs.
- No changes to Music Engine, idle timers, TV firmware, bootloader, or OEM launcher behavior.

## Home weather card — user reference match with live daily range — 2026-11-22

- **Design:** kept the compact 360×112dp card and matched the supplied reference hierarchy: centered condition above 3D weather PNG at left; stronger city label, small `AGORA`, prominent current temperature, and `Máx / Mín` on the right. Preserved the dark blue gradient, existing 3D rain PNG, Open-Meteo attribution and the Screensaver's separate 2D icon.
- **Data:** code now requests Open-Meteo `daily=temperature_2m_max,temperature_2m_min` with `forecast_days=1`, alongside current temperature/condition. Daily range is rendered only when both values parse as finite numbers; otherwise that line remains empty. Values are cached by city and refreshed with the existing weather cadence. City changes clear the daily cache.
- **Physical result:** at the capture, live data displayed `Joinville`, `21°C`, `Máx 27° · Mín 21°`, and `Garoa`. These are the device/API values at test time, not the example numbers in the reference image. Screenshot `output/captures/home-weather-reference-build-20261122.png` (1280×720); UI tree `output/captures/home-weather-reference-ui-20261122.xml` confirms all fields fit inside card bounds `[856,259][1216,371]`.
- **Source:** only `source/philco-home/src/com/philco/ultra777/home/MainActivity.java` changed. Pre-edit backup: `output/backups/MainActivity-before-weather-reference-range-20261122.java`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; API 19, v1 signature verified, v2/v3 absent, zipalign passed. Existing Open-Meteo HTTPS/TLS path retained; no dependencies added.
- **Release/current system APK:** `output/releases/philco-home-weather-reference-20261122.apk`, 1,878,367 bytes, SHA-256 `65313B6085B7B02A71A5632F347FB59EDA2D2678111991E8C9BB53D90069CD21`. The pulled installed APK and `/system/app/PhilcoOS.apk` both match this hash. PackageManager reports `SYSTEM`, versionCode 777, and PhilcoOS remains preferred Home (`mAlways=true`). HOME key returned to PhilcoOS; OEM launcher remains enabled as fallback.
- **Rollback:** previous system APK preserved at `output/backups/PhilcoOS-system-before-weather-reference-20261122.apk`, SHA-256 `CD4B2FC8BAB74300D6AE323EE544BF0BFEDF7BE5F28235E6B3613A9EFFD298F7`.
- **Reference preserved:** `docs/design/reference/weather-card-compact-style-user-20261122.png`, SHA-256 `90921F9D56BB6A8D342C2EC96B0857BA682B9E5558DC235C758A29B64D2BA8B8`. No icon sheet was integrated in this pass.
- **Limits:** only the live drizzle case was visually checked; other WMO icon states and extreme city-name truncation were not exercised. No Music, dock, Screensaver layout, firmware, or OEM app changes.
- **Next exact action:** separately evaluate the user's preserved 3D icon sheet for clean transparent crops and matching WMO states; do not alter the weather card geometry in that step.

## Weather label correction — 2026-11-23

- **Change:** Home weather range label changed from `Mín` to `Min`; no other UI or behavior changed.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK: `output/releases/philco-home-weather-reference-min-no-accent-20261122.apk`; SHA-256 `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D`.
- **Device:** installed and copied to `/system/app/PhilcoOS.apk`; on-device system APK SHA-256 matches. PackageManager reports `SYSTEM`, versionCode 777. Home launched normally.
- **Visual confirmation:** `output/captures/weather-min-label.png` (1280×720) visibly shows `Máx 27° · Min 21°`; UI capture `output/captures/weather-min-label-window.xml`. This verifies the requested spelling on the real TV.
- **Backup:** prior system APK saved as `output/backups/PhilcoOS-system-before-min-label-20261122.apk`.

## Idle backlight recovery — 2026-11-23

- **Cause found in source:** original backlight was held only in Activity memory; `onDestroy()` cleared the worker queue immediately after queuing restore. A process/activity recreation could therefore lose the saved value or cancel its restore.
- **Fix:** persist original backlight before dimming, recover and retry restoration in `onResume()`, clear saved value only after setter/readback succeeds, and allow queued worker operations to drain on destroy. No brightness/image slider or firmware/OEM code changed.
- **Backup:** `output/backups/MainActivity-before-backlight-restore-fix-20261123.java`, SHA-256 `1DB691C5D21F425B90C913136F6E4AE6A4F3CD104813F09A1119B2865B154BDC`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK `output/releases/philco-home-backlight-recovery-20261123.apk`, SHA-256 `8C9495927AEE57CBC8F6FD3223A9792F0DEF47A2F25516F99F04380480DB2B90`; v1 signature verified, v2/v3 absent, zipalign passed.
- **Device:** installed with `adb install -r` as an updated system app at `/data/app/com.philco.ultra777.home-1.apk`; base `/system/app/PhilcoOS.apk` and OEM launcher were not overwritten. PhilcoOS MainActivity resumed; preferred HOME remains `mAlways=true`.
- **Not yet physically verified:** no controlled idle-dim/return cycle was run after this build. TvManager `getBacklight()` readback is software evidence only and cannot prove panel luminance. If the panel remains dim despite the recovery attempt, investigate actual Realtek panel output path before further changes.

## Backlight re-dim after waking — root cause and fix — 2026-11-23

- **Reproduced cause:** old device logs show `Idle screensaver restored backlight to 77` at 00:44:44.076 followed by `Idle screensaver reduced backlight from 77 to 14` at 00:44:46.980. `dispatchKeyEvent()` consumes the wake key before `onUserInteraction()` can reliably reset the idle clock; the stale clock immediately re-triggered the dim action. The API restore itself did run.
- **Source fix:** `finishDismissIdleScreensaver()` now resets `lastHomeInteraction` and reschedules `homeIdleMonitor` before restoring. Combined with the prior persistent-original-value/worker-drain fix, this prevents immediate re-dim and retains recovery across Activity recreation.
- **Backup:** `output/backups/MainActivity-before-idle-reset-fix-20261123.java`, SHA-256 `4349371F020133490F57A2DF6B76D6045C3CB2258A4FC280853BC7EFB10D13C3`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK `output/releases/philco-home-backlight-idle-fix-20261123.apk`, 1,878,544 bytes, SHA-256 `9EBF709350EF7EF980017C947A4AFE05A96AC39E413FC8CCD3F34A4CDB63195F`. v1 signature verified; v2/v3 absent; zipalign passed.
- **Device:** `adb install -r` succeeded on `192.168.0.71:5555` (`rtd298x_tv001`, `smart_tv001`). Updated system app installed at `/data/app` over base `/system/app/PhilcoOS.apk`; original system APK preserved. `MainActivity` resumed and HOME preference remains `mAlways=true`.
- **Validation boundary:** fixed code and installed build are verified. The complete 15-minute auto-dim → wake → stay bright cycle has not yet been observed after this build. Previous logs prove the old immediate re-dim mechanism; software `getBacklight()` readback alone is not physical luminance proof. Current screenshot: `output/captures/idle-fix-current.png`.

## Weather fetch after TV power loss — 2026-11-23

- After reconnection, ADB initially showed only the system base APK (`/system/app/PhilcoOS.apk`, SHA-256 `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D`). The previous `/data/app` update was absent. Logs from that old process show `NoClassDefFoundError: ...Tls12SocketFactory`; the system app's `nativeLibraryPath` pointed to a missing `/data/app-lib/...` directory and `/system/lib/libconscrypt_jni.so` was absent.
- Preserved the system base APK at `output/backups/PhilcoOS-system-before-conscrypt-repair-20261123.apk` (same SHA-256 as above), then reinstalled existing signed release `output/releases/philco-home-backlight-idle-fix-20261123.apk` with `adb install -r`; no rebuild or `/system` write. PackageManager now loads the update from `/data/app/com.philco.ultra777.home-1.apk` and `/data/app-lib/com.philco.ultra777.home-1/libconscrypt_jni.so` exists.
- PhilcoOS MainActivity resumed and remains preferred Home (`mAlways=true`). Wi-Fi reports CONNECTED. The weather card now shows max `28°` vs cached `27°` in the earlier screenshot; no new `Tls12SocketFactory`/weather failure appeared after reinstall. This is consistent with a refreshed response, but the app does not log a success timestamp, so exact HTTPS success is not directly logged. Captured `output/captures/weather-verified-current.png`.
- **Not verified:** 30-minute periodic timer; the visible update followed app resume/card interaction. An OEM OTA service also logged an unrelated `UnknownHostException` for its Changhong update host; do not attribute that to Open-Meteo.
- **Next:** retain the `/data/app` update (do not copy this Conscrypt-dependent build into `/system` without packaging its JNI library correctly). Separately run the controlled 15-minute backlight dim/wake test; verify physical brightness visually.

## Idle/backlight schedule — 2026-11-23
- **IMPLEMENTED / COMPILED / INSTALLED:** 5-min screensaver retained; `TvManager.setBacklight(40)` at 15 min idle; `setBacklight(20)` at 30 min; preserve the first original value and restore on wake/resume. Start the 30-s warning at 59:30 and request Realtek standby at 60:00 total. While `PlayerStateRepository` reports `PLAYING`, hide/defer shutdown; once playback stops after deadline, provide a fresh 30-s warning.
- Build `assembleRelease`: BUILD SUCCESSFUL. APK `output/releases/philco-home-idle-backlight-15-30-60m-20261123.apk`; SHA-256 `1C63DCBF371287A1AD7089B91E6E2F5C1D21B71424974EA1EB6BFECE10F40904`. v1 verified; v2/v3 absent; zipalign 4 passed.
- Device `192.168.0.71:5555`: install returned `Success`; Android 4.4.2; PhilcoOS MainActivity resumed and preferred HOME remained true. `libconscrypt_jni.so` present in the data-app native library directory. No new fatal exception appeared in the recent filtered log window.
- **Not DEVICE_VALIDATED:** actual panel brightness at 15/30 min, restoration after waking, 1-hour standby, and deferral while playing music. These need real elapsed-time observation; API readback is not physical luminance proof.
- Pre-edit backup: `output/backups/MainActivity-before-20261123-idle-schedule.java`, SHA-256 `FC439E1B081447D0CBD2AEB7EE02AFB4D42AB18DCB726EDF353B792A05A5F740`.
- Reboot note: after the controlled reboot in this session, PackageManager exposed only the base `/system/app/PhilcoOS.apk`; the release was reinstalled to `/data/app`. No firmware/system partition was changed.
## Boot animation override — 2026-11-23

- Candidate archive: `output/releases/PhilcoOS_Ultra777_bootanimation_15fps.zip` (the exact requested filename without `_15fps` was absent). SHA-256 `EB9D2105AEBDB4BE64B09336337591D04D19C788EAC27A0D6BDC57C1280382BAC`; ZIP CRC test passed; `desc.txt` is `1280 720 15 / p 1 0 part0 / p 0 0 part1`; 90 one-shot PNG frames plus one looping frame, all 1280x720 and ZIP-stored for Android 4.4.
- Original: `/system/media/bootanimation.zip`; preserved locally at `output/backups/bootanimation-original-20170530.zip`, 2,001,514 bytes, SHA-256 `622F473EEABDD95BD2715891F783EFCC6B8CCA86A348E2B1A252971D1EEFE0EB`.
- Installed override: `/data/local/bootanimation.zip`; verified SHA-256 matches candidate. Owner/mode/context: root:root, 0644, `u:object_r:system_data_file:s0`. No `/system`, firmware or partition writes.
- Reboot: `sys.boot_completed=1`; bootanim stopped; PhilcoOS MainActivity resumed as HOME. Screenshot `output/captures/bootanimation-post-reboot-20261123.png` confirms the post-boot Home only. No matching BootAnimation log lines were retained, so the animation itself was not visually confirmed by capture.
- Rollback: remove `/data/local/bootanimation.zip`; system falls back to the preserved original. Temporary staging/capture files under `/data/local/tmp` were removed.

## Weather manual refresh and graphite card — 2026-11-23

- **Cause confirmed on device:** after reboot PackageManager was loading only `/system/app/PhilcoOS.apk`, whose expected `/data/app-lib/com.philco.ultra777.home-1/libconscrypt_jni.so` was missing. Logs from 04:17 show `NoClassDefFoundError: Tls12SocketFactory` / `UnsatisfiedLinkError`, so HTTPS failed before Open-Meteo could respond. This was an installed-package fallback, not a demonstrated Open-Meteo/TLS certificate rejection.
- **Implemented:** fixed the weather-card background to a restrained graphite gradient for all WMO conditions; manual refresh now shows progress, reports an already-running request, and logs sanitized start/success/failure markers. TLS certificate/hostname validation remains enabled.
- **Build:** `assembleRelease` BUILD SUCCESSFUL. APK `output/releases/philco-home-weather-gray-refresh-20261123.apk`, SHA-256 `B2D0854662C9D0D9E766C2141C80CBDAB0BA00538F5377B21B65E08D9DC1D35D`; signature verification and zipalign passed. Backup: `output/backups/MainActivity-before-weather-gray-refresh-20261123.java`, SHA-256 `D09E5D8FFB945BDD0EB7320D96784F7F8CE1EF9884D853C69B13F2CA6CEA3534`.
- **Device:** installed with `adb install -r`; PackageManager now resolves `/data/app/com.philco.ultra777.home-1.apk`; `libconscrypt_jni.so` exists in `/data/app-lib`. One manual D-pad activation produced `Weather refresh started (manual=true)` at 04:26:22 and `Weather refresh succeeded` at 04:26:23. MainActivity resumed as Home. Screenshot `output/captures/weather-refresh-validated.png`; filtered log `output/captures/weather-refresh-validation-20261123.txt`. Gray gradient is visually confirmed. Response success is evidenced; the displayed values matched the prior cached values, so a value change itself was not observed.
- **Boot animation fluency:** current installed candidate still has `desc.txt` at 15 fps and 90 unique part0 frames (6 seconds). No 30-fps source/intermediate frames are present. Changing only the declared rate would accelerate the same 90 frames to 3 seconds; it would not add motion samples. Left the installed animation unchanged rather than claiming that metadata-only edit improves smoothness. A true 30-fps master/interpolation is still needed for a meaningful fluency upgrade.
- **Scope:** no firmware or `/system` writes; bootanimation override unchanged.

## Weather card composition — 2026-11-23

- Removed the thin vertical divider between forecast icon/condition and current city/temperature. Increased the current-conditions panel from 136dp to 148dp and gave it a 4dp inset so the two groups sit closer while retaining breathing room. Kept the outer focus outline, wallpaper, card dimensions, and data.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-weather-clean-card-20261123.apk`, SHA-256 `9B70CE3CD0ABF6E5CB9FC73937EE4DF4E22CE4126FCD7AFA7820BC9D21C35A71`; zipalign passed.
- **Device:** installed using `adb install -r`; PM path `/data/app/com.philco.ultra777.home-2.apk`; MainActivity resumed. Screenshot `output/captures/weather-clean-card-20261123.png` visually confirms divider removal and preserved layout.
- Backup: `output/backups/MainActivity-before-weather-composition-20261123.java`, SHA-256 `361BC822E779AD9217976BD35E889D95468CE10591B00BEA14826FA55A2745CB`.
- Labels: IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED. No firmware changes.

## Weather card hierarchy pass — 2026-11-23

- Antigravity design input: `docs/design/weather-card-apple-design-plan-20261123.md` (design-only, no app edits). Implemented a compact 116dp condition/icon column, 64dp weather asset, 13sp medium condition label, expanded remaining data column, city + `AGORA` row, 42sp temperature, and readable stacked real `Máx`/`Min` values. Kept the 360×112dp footprint, gray gradient, external focus outline and removed central divider.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-weather-premium-layout-20261123.apk`, SHA-256 `DB1AECE11186951BFB93BA9C46EA669A3AF9A2EA2C09BD4CCBDD7E37FDCD3EA5`; zipalign passed.
- **Device:** `adb install -r` succeeded; PackageManager path `/data/app/com.philco.ultra777.home-1.apk`; MainActivity resumed. D-pad Up focused the weather card and Down restored SmartTube focus; screenshot confirms no clipping. Full capture `output/captures/weather-premium-layout-20261123.png`; focused capture `output/captures/weather-premium-focus-20261123.png`; close-up `output/captures/weather-premium-card-closeup-20261123.png`.
- **Backup:** `output/backups/MainActivity-before-weather-apple-layout-20261123.java`, SHA-256 `F04AF900BB598DB8B026DF263EF47193E1BCC0905A389A6EA6261DCD4DA828DB`.
- Labels: ANALYZED, IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED. No Home dock, wallpaper, climate-fetch logic, firmware, or `/system` changes.

## Weather card hierarchy refinement r2 — 2026-11-23

- Visual QA showed the condition label still needed stronger distant readability. Raised it to 14sp bold with a subtle shadow; no other UI change. The Agy proposal is preserved at `docs/design/weather-card-apple-design-plan-20261123.md`.
- **Build:** `assembleRelease` BUILD SUCCESSFUL; APK `output/releases/philco-home-weather-premium-layout-r2-20261123.apk`, SHA-256 `02914C0D9BC89A5E3233F0FF7702DD884417549474A6908BFA9CBCFBF871CDB9`; zipalign passed.
- **Device:** `adb install -r` succeeded; PM path `/data/app/com.philco.ultra777.home-2.apk`. MainActivity resumed. D-pad Up focused the weather card without clipping, Down returned focus to SmartTube. Current input visible in Home: TV aberta. Full/focus/close-up captures: `output/captures/weather-premium-r2-20261123.png`, `output/captures/weather-premium-r2-focus-20261123.png`, `output/captures/weather-premium-r2-closeup-20261123.png`.
- Backup before r2 text-weight edit: `output/backups/MainActivity-before-weather-condition-weight-20261123.java`, SHA-256 `D5F6FD3B18D4A6E9ED66246FB6CA2A82280B26BC13F6DC4778D848230E751B71`.
- Final labels: IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED. No firmware or `/system` writes.

## Restore last Realtek input at boot — 2026-11-23

- **IMPLEMENTED:** Added an enabled-by-default Quick Control Center toggle, `Ao ligar: restaurar última entrada`, and save the last input only after `getCurLiveSource()` confirms it (IDs 1, 5, 17, 18, 27).
- Added `RECEIVE_BOOT_COMPLETED` and `InputRestoreReceiver`. After boot it waits 2.5 s, reapplies the stored source through `TvManager.setSource()`, persists `PlayingSource`, and verifies with bounded retries. It does not launch `Tv_strategy`, so it will not intentionally cover the PhilcoOS Home.
- **COMPILED:** `assembleRelease` BUILD SUCCESSFUL. APK `output/releases/philco-home-last-input-restore-20261123.apk`, SHA-256 `437B504797B985CB466647CA7B2A9F30852E96BA628A05EC7616B72431CC566D`; zipalign passed.
- **INSTALLED:** `adb install -r` succeeded on `192.168.0.71:5555`; pulled installed APK hash matches. MainActivity is resumed; `PlayingSource=27` (TV aberta). Preferred Activities User 0 confirms `com.philco.ultra777.home/.MainActivity` is HOME `mAlways=true`.
- **DEVICE_VALIDATED / VISUALLY_CONFIRMED:** Toggle was navigated and switched off/on with D-pad. Captures: `output/captures/qcc-last-input-restore-20261123.png`, `qcc-last-input-off-20261123.png`, `qcc-last-input-on-20261123.png`. The final setting is enabled. Boot receiver is present in the installed package manifest.
- **NOT DEVICE-VALIDATED:** No real TV reboot was performed; startup source restoration and interaction with OEM boot ordering remain unverified. An explicit test broadcast produced no receiver log entry, so it is not treated as proof. Existing reports note that a reboot previously caused fallback from `/data/app` to the system APK; preserve this release and validate during a planned reboot only.
- The requested restart/power button in Settings remains deferred (“depois”).
- Backup: `output/backups/MainActivity-before-last-input-restore-20261123-050100.java` (SHA-256 `7F5D6B978D34F8F18C3DB9A538461558C9D043266348901B40503AB21067A292`); previous installed APK backup: `output/backups/philco-home-installed-before-last-input-restore-20261123.apk` (SHA-256 `709645F302AC216DBB8CC0355D3AAF51823560B83C9805899F4B7FBA48512FA0`).


## Weather Max/Min punctuation and alignment — 2026-11-23

- Changed labels to `Máx.` and `Min.` and split labels/values into fixed columns. Both temperature values are right-aligned to the same edge; labels share a consistent left column. No climate data or other layout changed.
- `assembleRelease`: BUILD SUCCESSFUL; zipalign passed. APK `output/releases/philco-home-weather-max-min-aligned-20261123.apk`, SHA-256 `7A2284D50CF98D8C46D516734BD0EB2A0ED848CB1194614DD36AABBD695E1C6A`.
- Installed with `adb install -r` on `192.168.0.71:5555`; pulled installed APK hash matches. MainActivity resumed, `PlayingSource=27` (TV aberta).
- Visually confirmed at 1280×720: `output/captures/weather-max-min-aligned-20261123.png`. Backup: `output/backups/MainActivity-before-weather-label-alignment-20261123.java`, SHA-256 `160DEADD4C2339586842AB15F89BA16129D10920517BA9E3A383FDD11A6B44F3`.
- Labels: IMPLEMENTED, COMPILED, INSTALLED, DEVICE_VALIDATED, VISUALLY_CONFIRMED.

## Power-action feasibility — 2026-11-23

- **ANALYZED:** Android reports `android.permission.DEVICE_POWER` protection `signature|system`. The release requests it, but PackageManager did not grant it while installed as `/data/app`, nor after promotion to `/system/app` and one controlled reboot. `pm grant` returned `SecurityException: Permission ... is not a changeable permission type`. The app's custom signing certificate is not a platform certificate.
- Existing `TvManager.standBy()` had previously returned `false`; no additional power action was invoked in this round. PowerManager reflection therefore remains unavailable and was not called.
- Agy static review could not complete: CLI returned `RESOURCE_EXHAUSTED` (individual quota).
- System package update: release SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F` was copied to `/system/app/PhilcoOS.apk`; it is v1-signed with the same cert as the prior system base (`b52e3604…14f2f9aa`). Old base is preserved locally at `output/backups/PhilcoOS-system-base-before-power-controls-20261123.apk` (SHA-256 `5C7DBA8B693B10FD7EDD6606A54E106954D2DE6D491B90E0EE77AC7163BA6C3D`) and on the TV as `/system/app/PhilcoOS.apk.bak-power-20261123`.
- One controlled ADB reboot completed. PackageManager now resolves `/system/app/PhilcoOS.apk`; `MainActivity` resumed, source remains `PlayingSource=27`, and PhilcoOS is the system Home package. `DEVICE_POWER` remains absent from granted permissions.
- **RESULT:** no power menu/button is exposed because the firmware denied the needed permission. “Power on” cannot be initiated by a launcher after the TV is fully off. Do not use root/shell commands from the app or weaken permission enforcement as a workaround.
- Input-device inventory: `/dev/input/event2` is `Smart_TV_Keypad`; event mapping for the rear joystick has not yet been captured. Next step is a short, user-triggered `getevent` observation.


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
- **ANALYZED — init instalado, ADB somente leitura:** /init.rtd298x.rc SHA-256 1ABED2539C22CED777A31745F794EC80E8F8207CB5CAEDF4CF83BD90B0C5D105, linhas 372–375 declara /system/bin/keypad; serviço ativo, PID 1797. TvServer PID 2037 e tvserver-setup são separados.
- **ANALYZED — ELF instalado:** keypad SHA-256 FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429. Thumb: 0x118c percorre pcb_enum; 0x132c abre/mapeia /dev/mem; 0x13f8/0x140c gravam/leem a região; 0xa28 amostra sete entradas (0x18 bytes cada), mantém estado/contadores e emite por uinput.
- **ANALYZED — xref .data:** PWR row 0x3098, campo cmd +0x10 em VA 0x30a8/offset 0x20a8, instalado=5, baseline=0. Lido em 0xac8, 0xad2, 0xb26. Dispatcher tbh 0xc3a: cmd 1→EV_KEY 0x74; cmd 5→EV_KEY 0x225. PWR/SOURCE têm cmd 5; a associação entre hold e cmd 1 segue desconhecida.
- **Conclusão:** o daemon tem amostras repetidas antes de sintetizar eventos, então é o lugar adequado para reconhecer duração. Não está provado que pressão longa central gere cmd 1/POWER. Falta documentação/fonte/símbolos OEM do mapa pcb_enum/KEYPAD_PARM para TP.RT2984T.PB801 build 20170531 e o ramo do hold.
- Nenhum teste físico, escrita na TV, candidato, build, instalação ou reboot nesta rodada.

## Análise estática do produtor PWR — 2026-11-23
- **ANALYZED:** ELF instalado do `/system/bin/keypad`, SHA-256 `FE908E30623A8B7CD2A9176CBDFD0219E6897AA41594FC15688C1963B81EF429`; layout de sete registros, parser, leitura `/dev/mem` e dispatcher analisados por disassembly Thumb/ARM.
- **ANALYZED:** descritor PWR (`0x400000001`) é consumido como índice 0/valor esperado 0/tolerância 4. O mapeamento calculado lê `0x18061500`. Nome de hardware do registrador não comprovado.
- **ANALYZED:** PWR cmd5 produz `TV_INPUT`; limiar `>119` registra flag, sem dispatch de `KEY_POWER`. Ciclo nominal sem evento 13 ms, estimativa do limiar ~1,6 s. Não houve medição em runtime.
- **Não realizado:** teste físico, ADB, instalação, build ou alteração de candidatos. Causa do desligamento permanece DESCONHECIDA. Fonte/símbolos do kernel exato de maio e mapa da placa continuam necessários.
- Relatório: `docs/REAR_DPAD_KEYPAD_REVERSE_ENGINEERING_20261123.md`.

## Rastreio adicional de KEY_POWER — 2026-11-23
- **ANALYZED:** CVTEkeypad legado: PWR/PWR2 `cmd=1`; ramo dispatcher `0x0a4c` prepara `EV_KEY 0x74` e escreve via uinput. Não foi encontrada execução ativa; a evidência live anterior aponta `/system/bin/keypad`.
- **ANALYZED:** no keypad instalado, marcador BSS `0x3514` após `>119` não tem xref de leitura e não altera cmd/dispatcher. Captura event2 tem dois pares KEY_POWER, mas não associa evento a gesto/processo.
- **ANALYZED:** `0x18061500` não foi identificado: ocorrência bruta desalinhada no kernel de agosto, sem símbolo ou xref. Kernel de maio/fonte `venus_gpio` ainda faltam.
- Sem interação TV, build, instalação ou escrita. Detalhes acrescidos ao relatório técnico existente.

## Deploy clima / inicialização — 2026-11-23
- **INSTALLED:** `adb install -r` do release SHA-256 `F0DE6C261F6BB3C3744414DF37F4486967630E876E0E5CAE5A9136928AA419F1` em `192.168.0.71:5555`; sucesso. APK-base anterior preservado em `output/backups/WEATHER_REFRESH_DEPLOY_20261123/PhilcoOS-system-base-before-install.apk` (SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`).
- **DEVICE_VALIDATED:** MainActivity abriu via `am start -W` (957 ms); boot concluído, tela ligada e atividade em foco.
- **VISUALLY_CONFIRMED:** captura `output/captures/weather-deploy-20261123.png` mostra cartão de Joinville com `ATUALIZADO 20:08` e valores meteorológicos.
- **ANALYZED:** logcat mostrou `Weather refresh succeeded; updating cached conditions`.
- TV estava ligada após o deploy; não houve teste do timer de standby/inatividade.

## HOME-MUSIC-EDGE-001 — Home to Music route — 2026-11-23
- **ANALYZED:** Home currently has eight fixed D-pad tiles in a horizontal scroller. The existing MusicActivity and MusicPlaybackService are registered in the API 19 app. No Home route to Music was present.
- **IMPLEMENTED:** RIGHT from focused Ajustes opens MusicActivity; repeat events do not launch it repeatedly; Back returns and restores focus to Ajustes. The Ajustes subtitle provides a right-arrow Music cue. Existing tile order, size and OK action remain.
- **COMPILED:** ssembleRelease succeeded. Candidate output/releases/philco-home-home-music-right-page-20261123.apk, 1,885,327 bytes, SHA-256 7D28138D73CEEA1CF6F36CE0D9EC7F27AEDAFD86F58B49F0DDC14FE685F68513.
- **Artifact checks:** zipalign -c -v 4 passed. pksigner verify --min-sdk-version 19 passed using v1; certificate matches the documented PhilcoOS signer. Gradle emitted only the existing Java deprecation note for MainActivity.java.
- **Not performed:** no automated/UI test, ADB query, install, screenshot or physical navigation. Do not label this feature DEVICE_VALIDATED or VISUALLY_CONFIRMED yet. TV state was not checked in this task.
- **Next check:** on-device navigate to Ajustes, press RIGHT once, confirm Music opens; press Back and confirm Home focus returns to Ajustes. Also confirm OK on Ajustes still opens Quick Controls.
### HOME-MUSIC-EDGE-001 refinement — page motion — 2026-11-23
- **IMPLEMENTED:** clarified transition: a single RIGHT from focused Ajustes opens the Music page with a horizontal slide from the right; Back returns with the reverse slide. Duration is 260 ms using simple translate resources.
- **COMPILED:** ssembleRelease succeeded. Candidate output/releases/philco-home-home-music-slide-20261123.apk, 1,887,428 bytes, SHA-256 8C4E9B896F2F77E999A157CCE797617888BDEFD9EAE2990B9736203C5A26857C.
- **Artifact checks:** zipalign check passed; pksigner verify --min-sdk-version 19 passed with v1 signature and the established PhilcoOS certificate.
- No test, ADB/device access, installation, screenshot or visual confirmation was performed. Mark this COMPILED only; physical transition quality and focus return still require device validation.
## HOME-MUSIC-EDGE-001 — deployment — 2026-11-23
- **INSTALLED:** db install -r returned Success on 192.168.0.71:5555. PackageManager reports com.philco.ultra777.home, versionCode 777 / 1.0.0-ultra777, update path /data/app/com.philco.ultra777.home-2.apk.
- **Preserved previous package:** output/backups/PHILCOOS_HOME_MUSIC_SLIDE_20261123/PhilcoOS-installed-before-update.apk, SHA-256 56D3E294D7195328A6A0133947BC2BB9B7F622BF2E607DA51B7994E58358B676.
- **Installed artifact identity:** pulled APK hash 8C4E9B896F2F77E999A157CCE797617888BDEFD9EAE2990B9736203C5A26857C, equal to the release candidate. /system/app/PhilcoOS.apk was not modified.
- **Device state:** boot complete, awake, screen on. No selected-source change or HOME-preference change was performed. Source last recorded as HDMI 2, not re-read during this deploy.
- **Not DEVICE_VALIDATED / not VISUALLY_CONFIRMED:** no key event, navigation test, screenshot or physical observation was performed. The transition behavior still needs a separate device check.
- **Next step:** inspect the existing Home header and playback state, then add the Dynamic Bar in a scoped change.
## PHILCO-HOME-DYNAMIC-BAR-001 — local implementation — 2026-11-23
- **ANALYZED:** PlayerStateRepository already exposes observer add/remove and publishes playback state; Home previously read it only for the idle-to-standby music guard.
- **IMPLEMENTED:** added a 360×54dp now-playing row below weather, visible only for PLAYING/PAUSED, showing the track title and playback state. MainActivity observes only during its resumed lifetime. No timer, album art or continuous animation was introduced.
- **COMPILED:** ssembleRelease succeeded. Candidate output/releases/philco-home-dynamic-bar-20261123.apk, 1,888,341 bytes, SHA-256 77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B. Zip alignment and v1 signature verification passed; signer matches the established PhilcoOS certificate.
- **Not INSTALLED:** before taking the current-build backup, ADB at 192.168.0.71:5555 was offline. One reconnect attempt and ping did not restore access. No APK was pushed and no TV writes occurred.
- **Not DEVICE_VALIDATED / not VISUALLY_CONFIRMED:** player states, card layout, transition and screen fit remain untested on the television.
- **Next:** after ADB is online, back up/hash the currently installed package, install this candidate, confirm its hash, then inspect Home with music playing and paused.
## HOME-MUSIC-EDGE-001 + PHILCO-HOME-DYNAMIC-BAR-001 — deployment — 2026-11-23
- **INSTALLED:** `adb install -r` retornou `Success` em `192.168.0.71:5555`.
- Package Manager informa `/data/app/com.philco.ultra777.home-1.apk`, versionCode 777, versionName `1.0.0-ultra777`.
- Candidato `output/releases/philco-home-dynamic-bar-20261123.apk`: 1.888.341 bytes, SHA-256 `77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B`.
- Cópia puxada da TV em `output/backups/PHILCOOS_DYNAMIC_BAR_DEPLOY_20261123/PhilcoOS-installed-after-update.apk` tem hash idêntico ao candidato.
- APK encontrado antes da atualização preservado em `output/backups/PHILCOOS_DYNAMIC_BAR_DEPLOY_20261123/PhilcoOS-installed-before-update.apk`: 1.882.620 bytes, SHA-256 `8958C7A896E191ADB63D55D3E74B31B7C64F27ED96A86229BF53D53E38A4934F`.
- **Estado da TV:** `sys.boot_completed=1`, acordada e tela ligada. Nenhum teste de navegação, reprodução ou inspeção visual foi feito. A seleção de entrada e o launcher em primeiro plano não foram reconsultados.
- **Não alegar DEVICE_VALIDATED nem VISUALLY_CONFIRMED.**

## Conferência pós-instalação — 2026-11-23
- **ANALYZED:** tentou-se consultar Activity em primeiro plano, estado de mídia e energia via ADB.
- **Resultado:** dispositivo `192.168.0.71:5555` ausente; ping falhou; reconexão ADB não se restabeleceu. Nenhuma entrada foi simulada e nenhum teste visual ocorreu.
- **Estado:** instalação anteriormente confirmada; estado atual da TV desconhecido até ela voltar à rede. A validação da navegação e da barra continua pendente.

## PHILCO-MUSIC-UI-002 — TV — 2026-11-23
- **ANALYZED:** tela antiga inspecionada no dispositivo; composição mostrava faixa isolada à esquerda, painel grande com poucas faixas e controles separados.
- **IMPLEMENTED:** MusicActivity reorganizada em cabeçalho, faixa/artwork à esquerda, biblioteca local à direita e rodapé único de reprodução. O artwork atual é um fallback 2D em gradiente; nenhum asset pesado novo foi adicionado.
- **COMPILED:** `assembleRelease` concluído. APK r2: 1.888.782 bytes, SHA-256 `6F2B844069BA318B3C231346722D55BF55709339637A018DDB4B4D4259F403FD`; zipalign e assinatura v1 verificados.
- **INSTALLED:** ADB `Success`; PM path `/data/app/com.philco.ultra777.home-1.apk`; hash da cópia extraída da TV corresponde ao candidato. Backup da versão anterior e r1 preservados em `output/backups/PHILCOOS_MUSIC_UI_REDESIGN_20261123/`.
- **DEVICE_VALIDATED:** Home → Música por DPAD_RIGHT abriu a Activity; Android Back retornou à Home e deixou Ajustes focado.
- **VISUALLY_CONFIRMED:** captura da Música `output/captures/philcoos-music-ui-redesign-r2-20261123.png`; retorno/Home `output/captures/philcoos-music-back-home-20261123.png`. Resolução visual 1280×720; lista com três arquivos de teste, sem corte.
- Não foi refeito teste de áudio no r2, pois playback permaneceu igual ao r1; não validar capas reais nem grandes bibliotecas USB.

## PHILCO-MUSIC-GLASS-003 — adaptação da referência Dribbble — 2026-11-23
- **IMPLEMENTED / COMPILED / INSTALLED:** adaptei a linguagem de vidro translúcido à página Música: wallpaper visível através de scrim mais leve, cartões fumê com borda fina e controles transparentes. Mantive somente a biblioteca local real e a faixa atual; não adicionei recomendações, artistas seguidos ou catálogo fictício. API 19/Views nativas; sem blur.
- APK `output/releases/philco-home-music-glass-r3-20261123.apk`, SHA-256 `83EEF63CE2C23100B1BA855057F04E649C120B54EFD984E19F1ADA5A80BBAFF6`; `adb install -r` retornou Success. Captura na TV: `output/captures/philcoos-music-glass-r3-20261123.png` (1280×720), TV acordada.
- **VISUALLY_CONFIRMED:** o fundo da paisagem agora aparece através dos painéis e o texto permanece legível. Backup prévio do r2 permanece em `output/backups/PHILCOOS_MUSIC_UI_REDESIGN_20261123/PhilcoOS-installed-music-redesign-r2.apk`.


## PHILCO-MUSIC-GLASS-GOLD-004 — validação r7 — 2026-11-23
- **COMPILED:** `assembleRelease --offline` passou no módulo `source/philco-home`. APK `output/releases/philco-home-music-glass-gold-r7-20261123.apk`, 1.888.762 bytes, SHA-256 `E3127E2ED293A007163A22134EC97A8F54F9C5A845499458CD8A7521B9C65EF0`. `zipalign -c 4` passou; `apksigner verify --min-sdk-version 19` confirmou esquema v1 e o certificado PhilcoOS existente.
- **INSTALLED:** atualização do package `com.philco.ultra777.home` em `192.168.0.71:5555` retornou `Success`; PackageManager indicou `/data/app/com.philco.ultra777.home-2.apk`. APK extraído do dispositivo corresponde ao hash do candidato. Backup prévio: `output/backups/PHILCO-MUSIC-GLASS-GOLD-R7-20261123/PhilcoOS-installed-before-r7.apk`, SHA-256 `F65F54A97B3D850D0149D824342CBED3964070F24FFA3963331E30761CFE5A99`.
- **DEVICE_VALIDATED:** Activity `com.philco.ultra777.home/.media.MusicActivity` iniciou e permaneceu em primeiro plano. `mWakefulness=Awake`, `mScreenOn=true`; filtro do trecho recente do logcat sem fatal de Android.
- **VISUALLY_CONFIRMED:** captura 1280×720 em `output/captures/philcoos-music-glass-gold-r7-20261123.png`, SHA-256 `BD80C7984D1CBBBBE9D193247C8211872BD3332B76822127B6EAD9B80B1EFAC6`. Mostra painéis grafite/navy, acentos dourados e os controles sem corte.
- A biblioteca exibida contém três arquivos Philco_Test; não havia faixa em reprodução nesta captura. Não retestados áudio, seek durante playback, pausa/retomada, D-pad funcional completo ou Dynamic Bar. Nenhuma medição comparativa de CPU/RAM foi feita.

## PHILCO-MUSIC-REFERENCE-UI-005 — 2026-11-24
- **ANALYZED:** MusicActivity, PlayerController, MusicPlaybackService, manifest e configuração Gradle. O projeto usa Views nativas, minSdk/targetSdk 19, MediaStore e MediaPlayer existente.
- **IMPLEMENTED:** atualização isolada em `source/philco-home/src/com/philco/ultra777/home/media/MusicActivity.java`. Layout de vidro/dourado inspirado na imagem; lista e cartões vêm da biblioteca local; busca filtra títulos/artistas; controles de reprodução continuam no controller/service. Volume real usa AudioManager/ponte Realtek já existente. D-pad/Back e foco foram ligados entre regiões. Não houve mudança no serviço, Home, firmware ou outros módulos.
- **COMPILED:** `assembleRelease --offline` passou. APK `output/releases/philco-home-music-reference-20261124.apk`, 1.894.589 bytes, SHA-256 `3C216E85EB6FCA75807375211429D7FCD48DDA7AFBDA56D264ED454CECBDB092`; `aapt` confirmou pacote existente, minSdk 19 e targetSdk 19; zipalign e `apksigner verify --min-sdk-version 19` passaram com assinatura v1 existente. `lintVitalRelease` passou como parte do build.
- **Não instalado / sem DEVICE_VALIDATED / sem VISUALLY_CONFIRMED.** Não foram feitos testes de interação ou áudio na TV. O APK está pronto para revisão/instalação manual futura.

## PHILCO-MUSIC-REFINE-006 — 2026-11-24
- **ANALYZED:** diff limitado a `MusicActivity.java`; dimensões calculadas para 1280×720/160 dpi. PlayerController e MusicPlaybackService conferidos como não modificados.
- **IMPLEMENTED:** alinhamento das abas/painéis; painel y=141, h=338; seção de cards y=567, h=42, até o player em y=609. `recTitle` 88 dp. Adicionado estado vazio da ListView e distinção entre biblioteca vazia, busca sem resultado e erro de consulta; cursor fechado em `finally`.
- **COMPILED / OFFLINE_VALIDATED:** `assembleRelease --offline` BUILD SUCCESSFUL; `lintVitalRelease` passou. `zipalign -c 4` e `apksigner verify --min-sdk-version 19` passaram. AAPT confirmou pacote existente, minSdk/targetSdk 19; classes.dex coincide com intermediário atual.
- APK: `output/releases/philco-home-music-refined-20261124.apk`, 1.894.941 bytes, SHA-256 `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`.
- **Não INSTALLED, sem DEVICE_VALIDATED e sem VISUALLY_CONFIRMED.** Sem captura da nova build, pois ela não foi instalada. O screenshot r7 existente é de outra build e não é evidência visual deste APK.

## PHILCO-MUSIC-REFINE-006 pre-install audit — 2026-11-24
- **ANALYZED / OFFLINE_VALIDATED:** candidate `output/releases/philco-home-music-refined-20261124.apk` SHA-256 matches expected `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`. Zip alignment and `apksigner verify --min-sdk-version 19` passed. `aapt` reports package `com.philco.ultra777.home`, versionCode 777/versionName `1.0.0-ultra777`, minSdk/targetSdk 19.
- Candidate certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa` matches the current r7 APK; both use v1 JAR signing. apksigner emits notices for two Gradle metadata entries in `META-INF`, but signature verification passes.
- **DEVICE READ-ONLY:** `192.168.0.71:5555` is connected; model property `smart_tv001`, Android 4.4.2, boot complete, awake/screen on, `MainActivity` foreground. Installed update is version 777 at `/data/app/com.philco.ultra777.home-2.apk`; its SHA-256 is the known r7 hash `E3127E2ED293A007163A22134EC97A8F54F9C5A845499458CD8A7521B9C65EF0`. Factory base `/system/app/PhilcoOS.apk` remains present.
- Rollback copy `output/backups/PHILCO-MUSIC-GLASS-GOLD-R7-20261123/PhilcoOS-installed-r7.apk` SHA-256 matches installed r7 exactly. Pre-r7 backup remains separate at `PhilcoOS-installed-before-r7.apk` (`F65F54A9…`).
- No installation, reboot, or settings change was performed. App data was not separately backed up; exact device validation and a 1280×720 capture require approval to install the candidate. TV selected source was not queried.

## HOME-DIAG-007 — diagnóstico da atividade HOME — 2026-11-24
- **ANALYZED (ADB somente leitura):** TV `smart_tv001`, Android 4.4.2, boot complete. Foreground/activity window: `com.haiersoft.launcherproject2995hk/com.haiersoft.lancherproject.activity.MainActivity`; processo Haier PID 2591. Philco Activity process was not present in `ps` at capture.
- **PackageManager:** Philco `MainActivity` continua habilitada (`User 0 enabled=0`, default enabled), e registrada nos filtros MAIN/HOME/DEFAULT, MAIN/LAUNCHER e MAIN/LEANBACK_LAUNCHER. Haier `MainActivity` também está habilitada e tem MAIN/HOME/DEFAULT. Ambas aparecem em `pm list packages -e`; nenhuma em `-d`.
- Preferência HOME atual: record `mAlways=true` seleciona `com.philco.ultra777.home/.MainActivity`; `Selected from` lista Haier e Philco. A leitura anterior imediatamente após instalação mostrou a mesma seleção.
- Resolução direta não está disponível nesta build: `pm resolve-activity` e `am resolve-activity` respondem `unknown command`; `/system/bin/cmd` não existe. Assim, a preferência persistida indica Philco como destino resolvido esperado, mas o comando direto não pôde confirmar o resultado do resolver.
- APK ativo `/data/app/com.philco.ultra777.home-1.apk`, SHA-256 igual ao candidato refinado. Refined e r7 têm manifest idêntico, SHA-256 `22E74D205A46FC6D0352054594F09A58E728453E035D757EE35081F2AA1BAC6F`; o system-stage APK também declara os mesmos filtros HOME. Não há evidência de alteração de componentes/filtros pelo update.
- Logcat recente filtrado não mostrou falha do Philco, `FATAL EXCEPTION`, nem linha útil de troca/lançamento por ActivityManager/PackageManager; hits foram inicializações do runtime usadas pelos próprios comandos de consulta. O motivo exato do Haier estar em primeiro plano continua **DESCONHECIDO**; lançamento explícito OEM ou restauração da tarefa Haier são hipóteses, não fatos.
- **Não executados:** abrir atividade, HOME/BACK, reiniciar, mexer na preferência ou alterar componentes. Próximo passo seguro: investigação estática offline dos caminhos de inicialização OEM; qualquer teste de abrir uma Activity requer autorização separada.

## 2026-11-24 — PhilcoOS Música: usabilidade e verificação no aparelho
- `COMPILED`: `assembleRelease --offline` passou; APK alinhado e assinatura verificada. APK `output/releases/philco-home-music-player-usability-final-20261124.apk`, SHA-256 `D2CDE8EA50526D6C81F23A787FD328AC0BFA18D198130F607E85C6C7A698D10A`.
- `INSTALLED`: atualização com `adb install -r`; hash do APK no dispositivo coincide com o local.
- `DEVICE_VALIDATED`: busca local e retorno à lista, reprodução de faixa, pausa/retomada, faixa seguinte, seek com setas, navegação D-pad/BACK e volume sem abandonar a tela. Volume final: 7.
- `VISUALLY_CONFIRMED`: captura real 1280×720 `output/captures/philcoos-music-final-overview-20261124.png`; texto e controles legíveis, sem corte/colisão. Captura do volume 7 sem OSD: `output/captures/philcoos-music-final-no-osd-volume7-20261124.png`.
- HOME preferido permanece PhilcoOS (`mAlways=true`); Haier permanece habilitado. Não houve reboot, limpeza de dados, alteração de firmware ou sistema.
- Pendências: validar pós-reboot em etapa autorizada; serviços/catálogos de música online não fazem parte deste player local.

## 2026-11-24 — Philco Music Pro + Home mini-player
- `COMPILED`: `assembleRelease --offline` concluído com sucesso; APK `output/releases/philco-music-premium-final-20261124.apk`, SHA-256 `2E2EC8623470BB8FBD4BCC3EEE414E1672107E16B7B211853835668618FA93E3`. API min/target 19; apksigner e zipalign passaram.
- `INSTALLED`: atualização por `adb install -r` retornou Success. Backup do APK anterior foi salvo e conferido em `output/backups/PHILCO_MUSIC_PRO_REDESIGN_20261124/installed-before-update.apk`, SHA `D2CDE8EA50526D6C81F23A787FD328AC0BFA18D198130F607E85C6C7A698D10A`.
- Pós-instalação: PackageManager mostrou versão 777; PhilcoOS continuou como HOME (`mAlways=true`), e `MainActivity` apareceu em primeiro plano logo após o update. Nenhum comando de alteração de HOME, volume ou firmware foi enviado.
- Captura e teste funcional pós-instalação **PENDENTES**: a abertura direta de MusicActivity retornou `error: closed` e `adb exec-out screencap` falhou. A sessão de TV foi interrompida sem tentar reconectar ou corrigir.
- `DEVICE_VALIDATED` e `VISUALLY_CONFIRMED`: não concedidos para esta versão. Não há medições de RAM/CPU.
- Arquivo leve para revisão por outra IA: `output/shares/philco-music-ai-review-20261124.zip`, 622C66F78E7DF0BAA29A3A3C0E9C3DCC50B05DAE0BDFC672477EDF55927FF30F.


## 2026-11-24 — integração revisada do Qwen
- `IMPLEMENTED`: `MusicActivity.java` atualizada seletivamente; `MusicLibraryRepository.java` evita título nulo; mini-player da Home recebe ID para o destino de foco D-pad. Backend/serviço não alterados.
- `COMPILED`: `assembleRelease --offline` passou, incluindo lint vital. APK `output/releases/philco-music-qwen-integrated-20261124.apk`, SHA-256 `EA99248513847A9080A8D405E7DF5FE1E0B6C0C19FE4CB043BDC1A108F7D5609`. API min/target 19, assinatura e alinhamento verificados.
- `OFFLINE_VALIDATED`: pacote, minSdk, certificado e alinhamento verificados. Não há testes automatizados relatados nesta etapa.
- Não instalado. Nenhum comando ADB executado nesta tarefa; sem captura. `DEVICE_VALIDATED` e `VISUALLY_CONFIRMED` pendentes.
- Pacote para revisão: `output/shares/philco-music-qwen-merged-20261124.zip` (61 KB, SHA-256 `8FE3BC96B4822A379DD1282C1BD217978A629E11865095ECC4B57939BE810B44`).

## 2026-11-24 — correção de saída do mute pelo Volume +
- `ANALYZED`: fonte anterior em `MainActivity.java` consumia Volume +/− com mute persistido e reaplicava zero após mudança de `STREAM_MUSIC`; isso pode explicar o relato após a atualização.
- `IMPLEMENTED`: Volume + restaura o volume salvo e limpa o mute persistido. Repetições e key-up não causam passos adicionais. O receiver também trata aumento positivo do volume durante o player como saída explícita do mute.
- Backup do fonte preservado: `output/backups/PHILCO_MUTE_VOLUME_UP_FIX_20261124/MainActivity.java.before-mute-fix`, SHA-256 `7D2F84020BE26FF6BDB52AD1B48A3F3FFBD83E75CA4531EC103B5AE2E71940B3`.
- `COMPILED`: `:assembleRelease --offline` BUILD SUCCESSFUL; lint vital passou. APK `output/releases/philco-music-audio-unmute-fix-20261124.apk`, 1.905.937 bytes, SHA-256 `FB82F3CDB1ED8E843D20206232B97E652A70FF8B800B0BF32DACFDD8EBF72DC7`; package/version 777, min/target API 19, assinatura v1 válida, certificado existente e zipalign válido.
- `OFFLINE_VALIDATED`: metadados, assinatura e alinhamento verificados. Não instalado nem testado na TV; volume/estado da TV não foram alterados. `DEVICE_VALIDATED` pendente.
- Confiança da causa: **provável** para o app manter/reaplicar o silêncio, baseada no código e na sequência temporal relatada; o ícone OEM e a saída acústica ainda precisam de confirmação no aparelho.

## 2026-11-24 — integração seletiva do ZIP PHILCO_MUSIC_refeito
- Entrada examinada: `PHILCO_MUSIC_v2_download (29).zip` SHA-256 `AA45A627DBCBEBE0086F098A69195B80F326A923E94C511EA48993099C27ED98`; `PHILCO_MUSIC_refeito (7).zip` SHA-256 `5EBA0BBF682405DB1A704672D70F96F937209FD87F95B4039A4AA387342C5DC1`.
- `IMPLEMENTED`: `Track` guarda album/genre/tags; `MusicActivity` os inclui na pesquisa. Tags incorporadas só são lidas quando faltam campos principais, e o fallback de artista do nome do arquivo não depende de título ausente. Índice de gêneros em lote. Player/backend mantidos.
- `COMPILED`: `:assembleRelease --offline` BUILD SUCCESSFUL; lint vital passou. APK `output/releases/philco-music-refeito-metadata-r2-20261124.apk`, 1.907.983 bytes, SHA-256 `1C6BA1C5CFC9922747EBC64B3F8FB4240F778A991183423E9707A78B4AFF7834`; package/version 777, min/target 19, assinatura v1 válida, certificado PhilcoOS, zipalign válido.
- `OFFLINE_VALIDATED`: compatibilidade estática das APIs adicionadas conferida pela tabela de API do SDK; metadados, assinatura e alinhamento conferidos.
- Não instalado; sem consulta ou escrita ADB. `DEVICE_VALIDATED` e `VISUALLY_CONFIRMED` pendentes.
- Limitação: gênero e tags podem ficar vazios se a tabela de gêneros não existir e os campos principais já estiverem preenchidos; evita ler todos os arquivos de áudio no primeiro carregamento. Compatibilidade e velocidade reais precisam ser medidas na TV.

## 2026-11-24 — Philco Music Refeito r5
- `IMPLEMENTED`: `MusicActivity` persiste separadamente o último volume audível e o usa como fallback de restauração quando o player inicia em zero.
- `COMPILED`: `assembleRelease --offline` passou, lint vital incluído. APK `output/releases/philco-music-refeito-metadata-r5-20261124.apk`, 1.908.371 bytes, SHA-256 `6E42987B880B27B6A456354FC64401DF98F786A8F20FDA20A92E20083BBFB5B1`; pacote `com.philco.ultra777.home`, versão 777, min/target API 19, assinatura v1 válida, certificado PhilcoOS, zipalign aprovado.
- `INSTALLED`: atualização `adb install -r` concluída. APK puxado da TV tem o mesmo SHA-256. APK anterior r4 preservado em `output/backups/PHILCO_META_INSTALL_20261124/installed-before-r5.apk`.
- `DEVICE_VALIDATED`: tela Música iniciou; um Volume+ com `STREAM_MUSIC=0` restaurou o nível diretamente a 7 e manteve MusicActivity em primeiro plano. A lista real apresentou faixas com artista Genesis. A resolução lida foi 1280×720; preferência HOME ainda tem `mAlways=true` para PhilcoOS.
- `VISUALLY_CONFIRMED`: captura real `output/captures/philco-music-refeito-r5-final-20261124.png`, 1280×720, SHA-256 `FC4B084AF0970D453C7E8DEA4966FF32BA179555B45DC7DC1DE343EC3CE658B5`.
- O toque sintético ADB no controle mute não acionou o botão; restauração após mute gráfico e reprodução/pause/next no r5 continuam pendentes. Não declarar esses testes como aprovados. Nenhuma configuração HOME, firmware, partição ou dados foi alterada.
- Pacote de código enxuto para revisão: `output/shares/philco-music-refeito-r5-review-20261124.zip` (25.100 bytes).

## 2026-11-24 — Philco Music UI r6 (candidato local)
- `IMPLEMENTED`: layout refeito com botões de transporte rotulados em linha, controle de volume separado e acessado por OK, painel “Mais”, coleção horizontal e miniaturas/capas amostradas com cache LRU 3 MiB. Volume físico não rouba o foco e não é encaminhado ao seek ou à lista. Backend de reprodução preservado.
- `COMPILED`: `assembleRelease --offline` passou; lint vital passou. APK `output/releases/philco-music-ui-refined-r6-20261124.apk`, 1.911.606 bytes, SHA-256 `2E6A8AB6EE0C3291E0E30302DD3E8C19C275B4C8948E395AC8920A5893715B29`; pacote `com.philco.ultra777.home`, version 777, min/target API 19.
- `OFFLINE_VALIDATED`: apksigner v1 válido, certificado PhilcoOS SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`; zipalign aprovado.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. A TV não estava listada no ADB quando tentamos atualizar; nenhum install foi executado. A captura real existente é da r5 e não valida r6.
- Backups de fontes r5 usados para retorno em `output/backups/PHILCO_MUSIC_UI_R6_20261124/source-before-r6/`. Backup APK r5: `output/backups/PHILCO_META_INSTALL_20261124/installed-after-r5.apk`.
- Próxima validação necessária: TV visível no ADB; instalar como atualização sem limpar dados; conferir 1280×720, volume que não muda faixa, abrir/fechar painel de volume com OK/BACK, foco D-pad, capas reais disponíveis, play/pause/next e estado final do volume.
- Pacote leve de revisão com APK e fontes-chave: `output/shares/philco-music-ui-r6-review-20261124.zip`, 1.879.111 bytes, SHA-256 `0F17C02EF60E1660F0FD697B1EB153EFE7FC825F4DF18DD32FD29824ADF0F40E`.

## 2026-11-24 — Philco Music UI r6.1 (candidato local)
- `IMPLEMENTED`: capas MediaStore habilitadas para as linhas da biblioteca, cache separado por tamanho de imagem, fila de decode limitada a 16 pedidos e callbacks completion/error protegidos contra instância antiga do MediaPlayer.
- `COMPILED`: `assembleRelease --offline` passou com Gradle 8.9/JDK17.
- `OFFLINE_VALIDATED`: APK `output/releases/philco-music-ui-corrected-r6-1-20261124.apk`, 1.911.806 bytes, SHA-256 `5B59FAC82E63FDFCCD475698444CB83FCFB5A28C0A3743EF54ED227897E24231`; package `com.philco.ultra777.home`, versionCode 777, min/target API19; assinatura v1/certificado PhilcoOS verificados e zipalign aprovado.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. TV não aparece no ADB; só há `emulator-5562 offline`. Nenhum comando de instalação foi executado.
- r6 preservado e fontes anteriores à correção em `output/backups/PHILCO_MUSIC_UI_R6_1_20261124/source-before-r6.1/`.
- ZIP de revisão: `output/shares/philco-music-ui-r6-1-review-20261124.zip`, 1.879.150 bytes, SHA-256 `1E4AA368F2471C70642D412E882BDD32BBE152B7F6F8824ACA69ED6C25099085`.
- Pendências: confirmar capas em arquivo real, rolagem com fila cheia, callbacks na troca rápida, volume sem pular faixa, navegação e aparência na resolução 1280×720, além de medir memória no hardware.

## 2026-11-24 — Central de Controle fecha ao entrar em standby
- `IMPLEMENTED`: referência rastreada para o diálogo e fechamento por `ACTION_SCREEN_OFF`, `onPause`, standby automático Realtek e ações PowerManager da Home.
- `COMPILED`: `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: APK assinado pelo mesmo certificado PhilcoOS, pacote/versionCode 777, min/target API19 e zipalign conferidos.
- `INSTALLED`: `adb install -r` retornou sucesso. APK puxado da TV tem o mesmo SHA-256 do candidato.
- HOME permanece `mAlways=true` para PhilcoOS. Captura após restauração explícita da Home: `output/captures/tv-after-philcoos-restore-20261124.png`, 1280×720, mostra HDMI 2 ativa e volume 7/100.
- `DEVICE_VALIDATED` da condição de standby está pendente: não houve ciclo físico de standby para evitar apagar a TV durante o trabalho.
- APK: `output/releases/philco-home-r6-2-standby-menu-close-20261124.apk`, 1.912.304 bytes, SHA-256 `D28A9E9B312F670F4883CAD2C7DA907DD0798E7FE8F9DA759C1629D7691967E0`.
- Rollback do APK instalado e da fonte preservados em `output/backups/PHILCO_STANDBY_MENU_CLOSE_20261124/`.

## 2026-11-25 — Philco Music: correções básicas de UX (MUSIC-UX-20261125)
- **ANALYZED:** `MusicActivity` usava limiar de 3 s para reiniciar, e o seek respondia imediatamente a esquerda/direita; BACK retornava fixamente ao play. Home mostrava apenas play/pause. Os cards da coleção truncavam o título em uma linha. Serviço não mantinha fila para controles da Home.
- **IMPLEMENTED:** fila/histórico/shuffle/repeat e skipNext/skipPrevious compartilhados no `MusicPlaybackService`; avanço automático ao concluir a faixa. Mini-player Home com anterior/próxima. Regra anterior 45 s implementada. Seek explícito com OK, cima/baixo ±10 s e BACK restaurando foco. Cards ampliados para duas linhas; painel reposicionado para espaço vertical. `ActionButton` ajustado para caber “Reproduzir”. Retomada solicita Audio Focus e restaura duck temporário para 1.0, sem boost.
- Backups de fonte preservados em `output/backups/PHILCO_MUSIC_BASIC_UX_20261125/source-before/`:
  - `MusicActivity.java`: `4A2E19B64B44EDA81B6BEA7DEF59F491F5B759FAFA6560C4237A1AA8269D5A2E`
  - `MusicPlaybackService.java`: `105FF4AD7332280D83011BA4B1FC45D78B9697CAFC348D6A028F7D50DC0B6931`
  - `PlayerController.java`: `79FCE90283DCCF7EBAD0F8160130150A62D00C70665ABECF4ED26459357DE8E1`
  - `MainActivity.java`: `2CAA988D6D608A862C8379C5F890670294C70324D8FF9BDC098993C644A318DD`
- `COMPILED`: `assembleRelease --offline` passou; lint vital passou.
- `OFFLINE_VALIDATED`: APK `output/releases/philco-music-basic-ux-20261125.apk`, 1,914,108 bytes, SHA-256 `46028E814FC83DE63C6FD4D35FF8AEDC899061D0CB697197384FFD68DA249336`; package `com.philco.ultra777.home`, versionCode 777, min/target API 19; assinatura v1 válida, certificado PhilcoOS; zipalign aprovado.
- Não instalado. Sem ADB, testes funcionais ou captura nesta tarefa. `DEVICE_VALIDATED` e `VISUALLY_CONFIRMED` pendentes.
- Áudio mais baixo que HDMI continua **não diagnosticado**: sem medição comparável. Sem pré-amplificação para evitar distorção/clipping. Estado de instalação/Home/fonte/volume da TV não foi reconfirmado nesta execução; último registro disponível 2026-11-24 foi Home PhilcoOS, HDMI 2, volume 7.

## 2026-11-25 — revisão seletiva da fila do player
- `IMPLEMENTED`: adicionado `PlaybackQueue.java`; serviço delega navegação, repetição e histórico à nova classe. API externa preservada. Regra anterior continua em 45.000 ms.
- Backup pré-edição: `output/backups/PHILCO_MUSIC_CLAUDE_REVIEW_20261125/source-before/MusicPlaybackService.java`, SHA-256 `9B86A2CB455A7B86C2F17625C59EE924233C284993AE7424E31A19E3BD3188F1`.
- Harness local: **14 verificações passaram**. Escopo cobre índice inicial/final, repetição, shuffle, histórico, URIs removidas, histórico sem ping-pong e repetição de faixa única. Não substitui teste Android/MediaPlayer.
- `COMPILED`: Gradle `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: APK `output/releases/philco-music-queue-review-20261125.apk`, 1.914.676 bytes, SHA-256 `2E44E86D409EA8E8641AF7E1A114D4AE1EF7D499E59D7072973D8920A2CA26D2`; package `com.philco.ultra777.home`, versionCode 777, min/target API 19; assinatura v1 válida com certificado PhilcoOS existente; zipalign aprovado.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. TV não consultada ou alterada. Última informação documentada ainda é HOME PhilcoOS, HDMI 2, volume 7 em 2026-11-24, sem confirmação do estado atual.
- A revisão externa não é tratada como teste executado aqui. O APK não foi instalado.

## 2026-11-25 — auditoria dos patches Qwen
- `IMPLEMENTED`: patches pontuais auditados em `MusicActivity.java`; backend existente preservado. Fila `PlaybackQueue` permanece integrada; regra de anterior segue 45 s.
- Backup pré-patch: `output/backups/PHILCO_QWEN_PATCH_AUDIT_20261125/source-before/MusicActivity.java`, SHA-256 `E18949CC64B480B61835A149A36D0280F72227EA13D48AB41B15F2BF286DC7BE`.
- Harness de fila: 14 verificações aprovadas.
- `COMPILED`: `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: candidato `output/releases/philco-music-qwen-audited-20261125.apk`, 1.914.772 bytes, SHA-256 `FD0A1AECB5437844E19241ADF75F0E8CA478CF84A24DC49265251E30A032105C`; package `com.philco.ultra777.home`, versionCode 777, min/target API19, assinatura v1 e certificado PhilcoOS existente, alinhamento aprovado.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. TV não foi consultada nem alterada.
- Não testado no dispositivo: rampa por tecla repetida, novo foco do seek, sincronismo do relógio e apresentação percentual.

## 2026-11-25 — redesign visual Philco Music
- `IMPLEMENTED`: nova hierarquia visual da tela: navegação superior real, destaque horizontal de capa/faixa, biblioteca local, cartões de coleção e barra inferior simplificada. Busca, biblioteca local, favoritos, reprodução, fila, volume, mute e seek mantidos no código.
- Nenhum teste foi alegado para controle remoto ou imagem física.
- `COMPILED`: `assembleRelease --offline` passou.
- `OFFLINE_VALIDATED`: `output/releases/philco-music-visual-refresh-20261125.apk`, 1.914.849 bytes, SHA-256 `617CE875059EE451C00D490B850E36FFCE9EAB740A048DC9901FCA148B27420C`; package `com.philco.ultra777.home`, versionCode 777, min/target API19, assinatura v1/certificado PhilcoOS e zipalign verificados.
- `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED`: pendentes. A captura existente é da versão anterior e não representa este candidato.

## 2026-11-25 — patches visuais Qwen
- `IMPLEMENTED`: cor ambiente superior derivada da capa (amostragem limitada), seek thumb condicionado ao foco/ajuste e divisor mais discreto em `MusicActivity.java`.
- Backup: `output/backups/PHILCO_QWEN_UI_PATCHES_20261125/source-before/MusicActivity.java`, SHA-256 `0B0EC8DD11F12F42236E3E9DAB2BCCF93F740258D7924B4026247825D2F72EEC`.
- `COMPILED`: Gradle offline com JDK local passou; lint vital passou.
- `OFFLINE_VALIDATED`: package `com.philco.ultra777.home`, version 777, min/target API 19; assinatura v1/certificado PhilcoOS e alinhamento ZIP verificados. APK `output/releases/philco-music-qwen-visual-fixes-20261125.apk`, 1.915.302 bytes, SHA-256 `04F9FFBA106B4F166FADAADE1E4DE10EEF10AD490F718E79A885F56632E83957`.
- TV não consultada nem alterada. `INSTALLED`, `DEVICE_VALIDATED`, `VISUALLY_CONFIRMED` pendentes; última captura é anterior à alteração.

## 2026-11-25 — instalação e captura real dos patches visuais Qwen
- `INSTALLED`: `adb install -r output/releases/philco-music-qwen-visual-fixes-20261125.apk` retornou `Success`. Hash conferido antes da instalação: `04F9FFBA106B4F166FADAADE1E4DE10EEF10AD490F718E79A885F56632E83957`. Sem desinstalação ou limpeza de dados.
- `DEVICE_VALIDATED`: MusicActivity abriu na smart_tv001 (Android 4.4.2/API 19). D-pad emitido via ADB moveu o foco da biblioteca para cartões de recomendação. Não é teste com joystick/controle físico.
- `VISUALLY_CONFIRMED`: captura real 1280×720/160 dpi em `output/captures/philco-music-qwen-visual-fixes-device-20261125.png` (SHA-256 `98BE0BCBE661B2594F8E2C14C2EAD2E0145B926AAE9C5BE1A82BFA71B9BDCC91`). Gradiente/fallback dourado e composição foram vistos; a faixa Genesis mostrada não tinha arte de capa renderizada, logo mudança dinâmica de cor por capa real permanece sem confirmação.
- Estado HOME permaneceu `com.philco.ultra777.home/.MainActivity`, `mAlways=true`; Haier continua instalado/habilitado; Activity deixada em MusicActivity. Resolução e densidade consultadas: 1280×720 e 160 dpi.
- Seek thumb, playback, pause, next e volume não foram testados. Sem faixa ativa, seek estava desabilitado. Volume não foi ajustado e nenhum áudio foi iniciado. Logcat AndroidRuntime não mostrou FATAL EXCEPTION.
- Backend permaneceu inalterado. Testes funcionais pendentes; não classificar Music player como completamente validado.

## PHILCO-MUSIC-WALLPAPER-TRANSFER-EQ-R6 — 2026-11-25
- `ANALYZED`: APIs reais do Realtek TvManager e fluxo atual da Home/Música revisados; conjunto de cinco fundos existentes e os dez arquivos de imagem do usuário localizados.
- `IMPLEMENTED`: foco Buscar/biblioteca, wallpaper 15 imagens, barra de reprodução na tela ambiente, receptor LAN restrito a música/foto e perfil de equalização de presença.
- `COMPILED`: `assembleRelease --offline` passou após corrigir um import ausente.
- `OFFLINE_VALIDATED`: APK alinhado; assinatura v1 válida e certificado PhilcoOS SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`; package/API19 conferidos.
- `INSTALLED`: `output/releases/philco-music-wallpapers-transfer-eq-r6-20261125.apk`, SHA-256 `1C2F2CC7D11CB3C0D663BEEA2E69FA8F585E9596FCEBBE884C7442189F917F19`; `adb install -r` aceitou sem limpeza de dados. HOME PhilcoOS, `mAlways=true`.
- `DEVICE_VALIDATED`: receiver Realtek leu de volta o perfil com treble 58 e EQ 3k=58/5k=61/10k=59; balance=0 e backlight preservados. HTTP GET `/`=200, upload JPG válido=201, arquivo na TV igual ao original por SHA-256; chave inválida=403; diálogo fechado e servidor deixou de responder. Arquivo de teste apagado.
- `VISUALLY_CONFIRMED`: capturas verdadeiras 1280×720: `output/captures/philco-music-final-r6-device-20261125.png` (SHA-256 `C39E8227862D0CFAB3CCE01533E0819CBDBC1D3FBC7D2EAE9430B769B1BA1263`), `output/captures/philco-music-r6-menu-device-20261125.png`, `output/captures/philco-transfer-r6-device-20261125.png`.
- Ainda não comprovado em uso real: sensação sonora; D-pad físico/esquerda-direita e volume segurado; reprodução/pausa/próxima faixa; ciclo visual após 7/10 min; dynamic bar com faixa tocando; celular físico na LAN. Não declarar estes itens como DEVICE_VALIDATED.
- TV manteve `com.philco.ultra777.home/.MainActivity` como HOME persistente; MusicActivity ficou aberta no fim. Fonte de entrada atual não foi consultada.

### Aditivo — estado final de volume
- `DEVICE_VALIDATED`: volume de mídia alterado para 7 pela UI e conferido em `dumpsys audio` (índice atual 7 no dispositivo ativo).
- `VISUALLY_CONFIRMED`: captura `output/captures/philco-music-final-r6-volume7-device-20261125.png`, 1280×720, SHA-256 `6678D9CE588BE8C2D2598AE8C3E3AB90D89949D4B8D8DBD556991166D330782E`.
- Nenhuma faixa foi iniciada durante o ajuste; validação de reprodução continua pendente.
