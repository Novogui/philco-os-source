# PhilcoOS Ultra 777 â€” Glass & Aurora

## Build target

- Existing module: `source/philco-home` (`com.philco.ultra777.home`).
- Android minimum/target: API 19 (KitKat).
- Release signing: local v1 JAR signing, v2 disabled; key and passwords are stored under `.toolchain/` and excluded from Git.
- No third-party Android libraries or native `.so` files are bundled.
- `build/system_stage` and the extracted factory baseline were not modified by this redesign.

## Implemented

- Five selectable, persistent photo wallpapers supplied by the user. One JPEG is decoded at a time as RGB_565, center-cropped to the screen, then recycled when the view detaches or another scene is selected.
- Full-screen clock and Brazilian Portuguese date, with minute/time/time-zone updates.
- Eight horizontally scrollable D-pad cards, colored vector-style icons, explicit focus links, focus scaling/highlight and automatic scrolling.
- Real Wi-Fi state/signal reading through `WifiManager` and real media volume/current maximum through `AudioManager`; status refreshes on Wi-Fi and volume broadcasts.
- Open-Meteo geocoding/current conditions over HTTPS, background requests, 5-second connect/read timeouts, 128 KiB response cap, cached last successful result and offline fallback. The initial city is Joinville, SC and the user can change it.
- A TLS socket wrapper enables TLS 1.2 only when the running Android build reports it as supported. It keeps the platform trust manager and hostname verification; there is no HTTP or trust-all fallback.
- App list is populated from installed packages at runtime. Settings, TV, SmartTube and source-selector intents are resolved before launch and failures are caught at the external integration boundary.

## Firmware evidence and limits

The following evidence was checked from APKs present in `build/system_stage` with Android `aapt`:

- `app/TSB_Tv.apk`: package `com.tsb.tv`; launchable activity `com.tsb.tv.Tv_strategy`.
- `app/SourceMenu.apk`: package `com.hikeen.xymenu`; manifest contains a `SourceMenu` activity with an `ACTION_MAIN` filter.
- `priv-app/RTKSettings.apk`: package `com.android.settings`; launchable activity `com.android.settings.Settings`.
- `app/com.cvte.keyservice.apk`: package `com.cvte.keyservice`; contains the IR/key service and boot receiver.
- The firmware inventory and extracted Realtek resources include TV middleware, tuner tables and the SourceMenu/TV apps; the hardware HALs remain untouched.

Historical note from the initial static audit (superseded by the Realtek runtime validation below): AR opened the TV application and direct cable selection had not yet been confirmed. Subsequent tests confirmed source selection and `getCurLiveSource()` read-back for AR=27, Cable=1, AV=5, HDMI 1=18 and HDMI 2=17. Live input preview is still not implemented in the new home.

Historical TLS failure from the initial validation (superseded by the successful refresh recorded below): an earlier Open-Meteo request failed its handshake and the card used cache/offline fallback. The implementation continues to require HTTPS and does not fall back to plaintext HTTP. The later successful cache refresh on this same TV demonstrates a subsequent successful HTTPS/TLS request. See [Android TLS protocol defaults](https://developer.android.com/reference/javax/net/ssl/SSLEngine) and the [Open-Meteo forecast API](https://open-meteo.com/en/docs).

## Validation state

- `assembleRelease`: passed.
- `lintRelease`: passed with 0 errors and 75 warnings. Most warnings are unused inherited resources; others flag fixed landscape orientation, procedural draw allocations, and the cancellable weather `AsyncTask`'s Activity reference.
- The first Release APK (38,179 bytes, SHA-256 `4BBF03FE37CA7AB3E21D2C37D13AFAD92C2569627F12C7FB8488A4071D935811`) was followed by UI and source-switch revisions; see the current APK identity below.
- The initial 38,179-byte APK (`4BBF03FE...935811`) had no native libraries. The later on-disk A56 artifact includes Conscrypt's `armeabi-v7a` native library; its identity and signature details are recorded in the audit section below.
- Runtime tests used ADB target `192.168.0.71:5555` (`smart_tv001`, `rtd298x_tv001`). The release replaced the already-installed same-package/same-signature app in `/data/app`; app data was retained. It was started explicitly, and the default Home selection and `/system` were not changed.
- On the first release, D-pad Left/Right/Up/Down/Center/Enter/Back were exercised. Enter opened SmartTube; AR/Cabo opened `com.tsb.tv/.Tv_strategy`; AV/HDMI 1/2 opened `com.hikeen.xymenu/.SourceMenu`; Apps opened its in-activity chooser; Settings and the Menu key opened `com.android.settings/.Settings`; weather and landscape dialogs opened and accepted D-pad input. The launcher remained startable after these transitions.
- Filtered logcat contained 0 `FATAL EXCEPTION` / `E/AndroidRuntime` records. It did contain the weather TLS failure described above. Captured filtered log: `output/philco_ultra777_device_validation.log`.
- ADB also listed `emulator-5562` as offline; no API 19 emulator image was installed. The physical TV provided the runtime validation instead.
- Firmware packaging, recovery and installation remain separate unvalidated steps; this document does not authorize or perform them.

## Follow-up after the user's screen review (2026-11-20)

The screenshot exposed the last dock tile clipped at the right edge, an oversized focus scale, and the dawn sun behind the weather card. The code now uses 64 dp horizontal safe margins, a centered row sized to fit the 1280Ã—720 layout, 124Ã—132 dp tiles, and a 1.045 focus scale. Dawn/sunset highlights were moved left of the weather card. If weather has no cached result, its glyph now shows a neutral cloud instead of incorrectly implying clear/sunny conditions.

The earlier `output/installed_check_20261120.apk` checkpoint is 399,733 bytes with SHA-256 `88F97F42FD665B256DD005B7A9D7781A3EBD0F26FF86E6B36636B234A031882A`; it is not the current release artifact.

That earlier wallpaper status was superseded by the user-provided five Pinterest image URLs. The photos now live at `source/philco-home/res/drawable-nodpi/wallpaper_01.jpg` through `wallpaper_05.jpg` (about 353 KiB total); URL 2 was duplicated in the request and is included once. Redistribution/licensing rights were not independently checked. Only one RGB_565 image is decoded at a time. This build has not been visually checked on the TV.

The screen review found the RCA rings and small Wi-Fi/weather/volume glyphs clipped or visually malformed. Their drawing geometry has been brought inside the view bounds; offline weather uses one neutral cloud silhouette, and the weather card now uses a darker glass surface to preserve text contrast over bright photos.

The OEM launcher DEX maps AV1 to `TvManager.setSource(5)`, HDMI 1 to `setSource(18)`, and HDMI 2 to `setSource(17)`. On the TV, the Realtek service logged successful calls for all three inputs. The first test exposed that `setSource()` returns `false` while applying a valid source; the launcher now follows the factory launcher's behavior and treats a non-throwing call as dispatched instead of opening the OEM selector after switching. The repeat test confirmed that the current build stays in `MainActivity`; HDMI 2 was restored and remains the selected source (`PlayingSource=17`). `dumpsys meminfo` showed 11,471 kB total PSS for the active launcher process at the final check; this is not a peak measurement. The filtered runtime log contained no fatal launcher exception. No post-fix screenshot was captured, so the overscan correction still needs visual confirmation. The OEM live preview is not yet recreated in the new home.

## Release A56 identity audit (offline, 2026-11-20)

Current file: `source/philco-home/build/outputs/apk/release/philco-home-release.apk`; 1,199,404 bytes; file modification time 2026-11-20 23:16:25 -03:00; SHA-256 `A56C3D7D370D83FC3AEC652F9AC14E024CBD9341466737291AB8983B59AA7374`. A copy in `build/system_stage_slim/app/PhilcoUltra777/PhilcoUltra777.apk` was recorded with the same SHA-256. This audit did not run Gradle or ADB.

`aapt dump badging` identifies package `com.philco.ultra777.home`, versionCode `777`, versionName `1.0.0-ultra777`, minSdk 19, targetSdk 19 and compileSdk 35. The APK contains `lib/armeabi-v7a/libconscrypt_jni.so`. The DEX contains both `org.smarttube.stable` and `getLaunchIntentForPackage`; this confirms that the SmartTube launch route is present, not that the whole APK is identical to a prior build.

`zipalign -c -v 4` passed. `apksigner verify --min-sdk-version 19 --verbose --print-certs` reports v1 true, v2/v3/v3.1/v4 false, one RSA-2048 signer, certificate DN `CN=PhilcoOS Ultra 777, OU=Personal, O=PhilcoOS, C=BR`, certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`. The verifier warns that the two Gradle metadata entries under `META-INF` are not protected by the v1 signature; the APK still verifies. Existing `lint-results-release.txt` has no `NewApi` findings; this static result does not replace runtime testing.

The current `MainActivity.java` SHA-256 is `E9C4FE5D5AA5DE9A78B4FC75A220C6120E8D2B2E66EE4D160D365260956ABB07`. The APK's `META-INF/version-control-info.textproto` says `generate_error_reason: NO_SUPPORTED_VCS_FOUND`; the project and source module have no `.git` directory. `output-metadata.json` records the release variant and package metadata but no source revision. The compiled `MainActivity.class` intermediate is timestamped after the source file, and the APK DEX contains the SmartTube route, but there is no reproducible-build record linking every APK byte to the complete source tree.

The expected known-good hash `8D555DB8AA3F2BDEC8CF549DD2626A66F1E515135867ABDD5213E12B47A78CC4` was not found among APKs in `source`, `output`, `build` or `.toolchain/tmp`. Other local launcher APKs were found, but none has that hash; they are not treated as the known-good comparator. Therefore no internal ZIP, manifest, DEX, resource, certificate or compression comparison against that known-good artifact is possible. A56's ZIP entries use normalized 1981 timestamps, which do not establish its build date.

**Identity divergence status: NOT RESOLVED.** The local A56's package metadata, signature, alignment, SmartTube route and API declarations are confirmed. The reason its whole-file SHA-256 differs from the stated known-good hash cannot be determined without that APK or an authenticated build record. No equivalence is claimed.

## Render-allocation optimization checkpoint A57 (2026-11-21)

Changes are limited to `TvGlyphView.java` and `AuroraBackdropView.java`. `TvGlyphView` now reuses one `Path` per view and static color palettes instead of creating paths and arrays during each draw. `AuroraBackdropView` now caches its fallback and vignette gradients, rebuilding them only when the view dimensions change. Glyph geometry, colors, wallpaper selection and crop behavior were left unchanged. Source SHA-256: `TvGlyphView.java` `4E608EB07F40D37DB0928C064352BD6B074367D154835A88B980998E333F9E3A`; `AuroraBackdropView.java` `767BDCB8106D870C8822AD6E8AF8597E674A1E4515E5AE3AF38DE815D5A8CD76`.

The offline `assembleRelease`/`lintRelease` run generated the current release at `source/philco-home/build/outputs/apk/release/philco-home-release.apk`: 1,199,801 bytes, SHA-256 `13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`, modified 2026-11-21 00:05:19 -03:00. `aapt` reports package `com.philco.ultra777.home`, versionCode 777, minSdk/targetSdk 19 and compileSdk 35. `classes.dex` still contains the SmartTube launch route. `zipalign -c -v 4` passed; `apksigner` verifies v1, with v2/v3 disabled. Lint reports 0 errors and 75 warnings, with no `DrawAllocation` or `NewApi` findings.

This is a new artifact; it is not the A56 hash and is not the stated known-good APK. The known-good comparator remains unavailable. A57 was later promoted into `build/system_stage_slim`; see the promotion and runtime sections below.

## A57 promotion and physical-TV validation (2026-11-21)

### Artifact identity and promotion

- A57 release: `source/philco-home/build/outputs/apk/release/philco-home-release.apk`, 1,199,801 bytes, SHA-256 `13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`.
- A56 stage artifact and preserved archive: `output/releases/philco-home-A56-A56C3D7D.apk`, 1,199,404 bytes, SHA-256 `A56C3D7D370D83FC3AEC652F9AC14E024CBD9341466737291AB8983B59AA7374`.
- A57 release archive: `output/releases/philco-home-A57-13BADB2D.apk`; the same A57 hash was promoted to `build/system_stage_slim/app/PhilcoUltra777/PhilcoUltra777.apk`.
- The APK installed immediately before the A57 update was pulled to `output/releases/philco-home-installed-before-A57-20261121.apk`; its SHA-256 is A56's `A56C3D7D...AA7374`.
- Offline metadata comparison: both artifacts have package `com.philco.ultra777.home`, versionCode 777, versionName `1.0.0-ultra777`, min/target API 19 and the same v1 signing certificate (SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`). Both have v1 signing and no v2/v3 signature. `AndroidManifest.xml`, `resources.arsc`, resources and bundled Conscrypt library are byte-identical; `classes.dex` differs, as do the expected signature/manifest files. This confirms A57 is not byte-identical to A56, while the resource/manifest package surface is unchanged.
- The previously stated known-good hash `8D555DB8AA3F2BDEC8CF549DD2626A66F1E515135867ABDD5213E12B47A78CC4` is still absent from available artifacts; no equivalence with that release is claimed.

### TV installation and observed behavior

- ADB identified the physical TV as `smart_tv001` / `rtd298x_tv001`, Android 4.4.2, build fingerprint `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`.
- A57 installed successfully with `adb install -r` over the same package and signing certificate, updating only `/data/app`; package data was retained. `/system`, the firmware stage on TV, partitions and bootloader were not written.
- The package manager temporarily removed the preferred HOME record during update. The PhilcoOS launcher was explicitly reselected as the persistent HOME afterward; subsequent checks showed PhilcoOS `MainActivity` resumed. The OEM launcher was not disabled.
- The launcher opened after installation. D-pad navigation reached the landscape picker; changing wallpaper to “Lago alpino” and restoring “Luz dourada” both worked. The home screenshot shows the AV composite icon fully inside its card and the eight-card dock within the visible screen. SmartTube's browse Activity opened from its card (`org.smarttube.stable`); video playback/resolution was not tested.
- The screenshot also shows a floating mute/speaker overlay at the lower-right edge while the launcher reports volume `0 / 100`. This is a visible issue to investigate; available evidence does not establish whether the overlay belongs to the launcher or the TV's system audio UI. A volume-key test was attempted, but its command result was truncated and is not treated as verified.
- Wi-Fi status was connected/strong, and weather displayed Joinville, 21°C, drizzle during the session. This confirms the card rendered live data during that session, not long-term refresh/reliability. No TV reboot/cold-start test, AV source transition test, or HDMI/TV input preview test was run in this A57 round.
- Captures: `output/philcoos_a57_home_20261121.png`, `output/philcoos_a57_home_restored_20261121.png`, `output/philcoos_a57_scene_picker_20261121.png`, `output/philcoos_a57_wallpaper_test_20261121.png`, and `output/a57_home_resolver_20261121.png`.
- Logs during installation and UI navigation showed no fatal exception from `com.philco.ultra777.home`. SmartTube launch was recorded. This is a session-specific observation, not proof of crash-free operation under all inputs.

### Memory and current ADB status

- A57 launcher PSS after a fresh launch was 12,272 kB, versus the previously recorded 11,471 kB on an earlier build: +801 kB (about 7%). The measurements were taken in separate sessions and do not prove the optimization increased memory use; PSS is not peak RAM.
- A later launcher sample after opening SmartTube, wallpaper UI and the Android HOME resolver was 16,396 kB and had two activities/four app contexts. It is a loaded/interacted state and is not directly comparable to the earlier idle sample. SmartTube itself measured 52,071 kB in that session; no video playback load was measured.
- A later local ADB check again failed at daemon startup (`could not read ok from ADB Server`, `failed to start daemon`). No repeated restart attempts were made after this bounded failure. Therefore additional post-session state (including the attempted volume-key result) could not be confirmed in that check.
- A57 has now been visually exercised on the TV, but performance improvement is not demonstrated. No peak memory measurement, sustained playback test, reboot/boot-loop validation, or recovery procedure validation is available.

## Clock and weather-card visual polish (2026-11-21)

Only the two requested home-screen elements were adjusted in `MainActivity.java`:

- Clock text increased from 78sp to 90sp (about 15.4%) and changed from `sans-serif-thin` to Android's built-in `sans-serif-medium` for stronger readability. No font dependency was added.
- Weather card height reduced from 116dp to 104dp; vertical padding changed from 12dp to 8dp, and its glyph from 58dp to 54dp with a slightly tighter right margin. City, temperature, condition, attribution, Glass & Aurora background and overall layout remain in place.

The offline release build and lint completed successfully. Lint reports 0 errors and 75 warnings (the same count recorded for A57). `zipalign -c -v 4` passed; `apksigner` confirms v1 signing and no v2/v3 signature; `aapt` confirms package `com.philco.ultra777.home`, versionCode 777, versionName `1.0.0-ultra777`, minSdk 19 and targetSdk 19. Candidate APK: `output/releases/philco-home-visual-polish-EB256CA3.apk`, 1,199,801 bytes, SHA-256 `EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2`.

The archived A57 remains unchanged at `output/releases/philco-home-A57-13BADB2D.apk` (SHA-256 `13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`). `build/system_stage_slim` and the TV installation were not updated. No new TV screenshot or runtime visual check was possible in this round; install/update waits for a functioning ADB session and visual review.

## EB256 visual validation on TV (2026-11-21)

- Offline comparison passed before installation. A57 and EB256 both declare package `com.philco.ultra777.home`, versionCode 777, versionName `1.0.0-ultra777`, minSdk 19 and targetSdk 19. Both verify with v1 signing only and the same signer certificate SHA-256 `b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa`; their whole-file SHA-256 values differ as expected. `apksigner verify` passed and `jar tf` read both APK archives.
- ADB 1.0.41 / Platform Tools 37.0.3 identified `smart_tv001` / `rtd298x_tv001`, Android 4.4.2, build fingerprint `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`.
- Before updating, the installed package was A57 exactly (pulled from `/data/app`; SHA-256 `13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`). It was preserved as `output/releases/philco-home-installed-before-EB256-20261121.apk`. EB256 was installed with `adb install -r`; the installed APK was then pulled to `output/releases/philco-home-installed-EB256-20261121.apk` and matched candidate SHA-256 `EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2`. App data was retained; no firmware, `/system`, partition, bootloader or slim-stage file was changed.
- The TV screenshot `output/philcoos_eb256_visual_validation_20261121.png` is 1280×720. It shows the increased, heavier clock; the shorter weather card; the Joinville condition and current wallpaper; and no visible overlap or clipping in the tested home layout. The visual size and balance are legible in the capture. A physical viewing-distance assessment was not possible remotely.
- Immediately after package update, Android had removed the persistent HOME choice (same package-manager side effect seen on the prior update). The existing PhilcoOS Ultra 777 option was selected with “Sempre” again; `dumpsys package` then showed its HOME record `mAlways=true`, and `dumpsys activity activities` showed `MainActivity` resumed. No different HOME launcher was chosen.
- Captured logcat after launch contained no `FATAL EXCEPTION` or `E/AndroidRuntime` entries. This is limited to the captured session.
- A57 remains preserved as the reference archive with its original hash. `build/system_stage_slim` was later promoted to EB256; the state is superseded by the quick-control checkpoint below.

## Quick Control Center QC02–QC04 (2026-11-21)

### Implementation and confirmed Realtek interfaces

- The compact translucent D-pad panel is integrated with the existing Settings buttons. It preserves the wallpaper, home clock, weather card and dock. `TvGlyphView` adds small rounded brightness/backlight symbols; focused slider thumbs enlarge and gain a high-contrast outline. Realtek reads/writes run on a serial `HandlerThread`, not the UI thread.
- Factory framework DEX confirms `TvManager.getCurSourceType()`, `getSourceVolume(int)`, `getMute()` and `setMute(boolean)`. The factory SourceMenu adjusts volume with `AudioManager.setStreamVolumeForL4300(STREAM_MUSIC, value, 1, true)`. The panel uses that same vendor bridge and reads the current-source volume and mute state back from `TvManager`.
- The native TV app's `PictureSettingFragment` configures both image brightness and panel backlight with explicit 0–100 slider bounds. It reads via `getBrightness()` / `getBacklight()` and writes via `setBrightness(int)` / `setBacklight(int)`. The quick panel therefore exposes two distinct, hardware-backed sliders. Android `Settings.System.SCREEN_BRIGHTNESS` (observed value 102) was not changed.
- `TvManager` also exposes LED setters, but this audit did not find a verified state query or safe mapping for their arguments; no LED control is shown. Physical luminance and sound-pressure measurements were not available, so hardware validation is the Realtek service read-back rather than an external light/audio meter.

### Television validation

- QC02 was installed in `/data/app` after preserving the prior installed APK. With D-pad input, Realtek volume read-back changed 2→3→2; Mute switched to “Ativar som” and back to “Silenciar”; image brightness changed 50→51→50; backlight changed 100→99→100. Final values are restored to volume 2/100, Mute off, image brightness 50/100 and backlight 100/100.
- The native Settings button resumed `com.android.settings/.Settings` from the OEM `RTKSettings.apk`. Its visible sections include wireless, wired network, T-Link, device/application, date/time and About. Image/sound/channel controls are in the original TV app: `com.tsb.tv/.Tv_strategy` opened the native `IMAGEM` menu when the TV's MENU key was pressed; it displayed brightness 50 and backlight 100. The quick-panel button opens the native Settings activity, not that in-app picture menu.
- QC03 adds the clearer D-pad focus marker. The TV hierarchy confirmed focus traverses the volume slider and the panel values load. Capture: `output/quick_control_center_qc03.png`. QC03 is the installed physical build (SHA-256 `F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8`); the pulled installed APK matched it. Home preference was restored to PhilcoOS (`mAlways=true`).
- QC04 is the current local build after suppressing lint's false `WrongConstant` warning for Realtek's proprietary service name `"tv"`. `assembleRelease` and `lintRelease` succeeded; lint reports 0 errors / 87 warnings. Package is `com.philco.ultra777.home`, versionCode 777, min/target API 19; zipalign and v1 signature validation passed. QC04 SHA-256: `C9870E798A380BDCBC101580F10461D86FDB1258D0628E6F520EED1D727631BD`. QC04 changes only lint annotations from the physically exercised QC03; it was not reinstalled.
- `output/releases/philco-home-A57-13BADB2D.apk` remains preserved as rollback. EB256 remains preserved at `output/releases/philco-home-visual-polish-EB256CA3.apk` (SHA-256 `EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2`). `build/system_stage_slim` still contains EB256 with that hash; it was not updated to QC04. Firmware, TV `/system`, partitions and bootloader were not modified.


## Low-quota continuation checkpoint (historical snapshot at 02:05, superseded below)

- QC02–QC04 work is complete as recorded above: QC03 is the physically tested build, QC04 is the local lint-clean build with only lint annotations changed. The confirmed D-pad hardware checks restored volume 2/100, Mute off, brightness 50/100 and backlight 100/100. Do not repeat these checks unless source behavior changes.
- Read-only TV check at 01:56 -03:00 found `weather_time=2026-11-21 01:50:02`, `weather_attempt_time=01:50:00`, and cached live result `21° · Nublado`. This confirms a successful recent network refresh. It does not, by itself, prove whether that refresh was caused by the periodic timer, resume, connectivity, or a manual request. Code schedules refresh every 30 minutes while Home is active and also refreshes on connectivity changes.
- Realtek source IDs in current source are TV aberta 27, AV 5, HDMI 1 18, HDMI 2 17, and cable 1. The existing runtime evidence confirms AV and both HDMI routes; TV aberta and cable have not been individually verified by source read-back in the current checkpoint.
- Wi-Fi glyph remains procedurally drawn in `TvGlyphView.drawWifi()` and reports real `WifiManager` signal state. No icon edit or rebuild was made in this continuation because the available screenshot is the quick-control overlay, not a clean home-screen close-up suitable to validate a glyph change.
- Exact next action: capture a clean 1280x720 home screen and verify TV aberta/cable by Realtek source read-back, restoring HDMI 2 afterward; use that capture to make and validate one bounded Wi-Fi glyph adjustment if the visual defect is still present. No firmware or `/system` changes.


## Input and home-screen validation checkpoint (2026-11-21; weather follow-up superseded below)

- On the physical `smart_tv001` / `rtd298x_tv001` (Android 4.4.2, 1280×720), the AR tile requested Realtek source 27 and `getCurLiveSource()` read back `current=27`. The Cabo tile requested source 1 and read back `current=1`. Both checks also logged successful matching source IDs; this is stronger than the `setSource()` return value, which is `false` even on valid transitions.
- Restored HDMI 2: the TV app log records expected/current 17. Android `PlayingSource` reads 17. The resumed activity is PhilcoOS `MainActivity`, and the package HOME record has `mAlways=true`. No `/system`, firmware, stage, partition or bootloader changes were made.
- Clean home capture: `output/philcoos_home_checkpoint_20261121.png`, 1280×720. The Wi-Fi glyph is fully in bounds and recognizable at the current size; no concrete clipping/rendering fault justified a code change. No APK was rebuilt or reinstalled.
- Release identity remains unchanged: installed physical candidate QC03 SHA-256 `F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8`; local-only QC04 SHA-256 `C9870E798A380BDCBC101580F10461D86FDB1258D0628E6F520EED1D727631BD`. EB256 remains in `build/system_stage_slim`; A57 rollback archive remains preserved.
- Weather code schedules a 30-minute refresh while Home is foregrounded and has connectivity-triggered refresh. At 02:05 -03:00, the last success was still 01:50:02; the next periodic window had not yet elapsed. Thus live weather retrieval is confirmed, but an autonomous timer firing is **not proven in this checkpoint**. Logs/timestamps did not distinguish a timer refresh from resume, connectivity or manual refresh.
- Historical next action from the 02:05 snapshot: check one timestamp/cache sample at or after 02:20. This was completed and its result is recorded in the final checkpoint below; no further source-input or quick-control retest is needed.


## Final weather and release checkpoint (2026-11-21)

### Automatic weather result

One read-only collection on the TV at 02:23:55 -03:00 showed `weather_time=1795238403411` (02:20:03.411), `weather_attempt_time=1795238402344` (02:20:02.344), and cache text `21° · Parcialmente nublado`. The prior recorded attempt/success was 01:50:00.793 / 01:50:02.339 with `21° · Nublado`; the new attempt began about 30 minutes later and success/cache timestamp followed about one second later. PhilcoOS `MainActivity` was still resumed. No manual weather refresh or app relaunch was issued after the 02:05 checkpoint; the agent restored Home at 02:05, and `MainActivity` remained resumed at the sample. Its `onResume()` refresh would have skipped the request because the cache was younger than 30 minutes, then scheduled only the remaining interval. The new attempt exactly 30 minutes after the previous attempt therefore strongly identifies the periodic callback, not an explicit weather interaction.

- **Periodic timer:** strongly corroborated as having fired without manual interaction: the attempt advanced at the configured 30-minute cadence while Home remained resumed. The code does not emit a dedicated “timer fired” log, so callback identity is inferred from cadence/state rather than directly logged.
- **HTTPS request:** executed; the attempt timestamp was written and a successful result followed. The existing fetch path permits only `https://` endpoints.
- **TLS:** latest request succeeded. The valid parsed response and success timestamp are saved only on the success path; no matching TLS failure appeared in the retained filtered logcat sample. This supersedes the earlier TLS failure, without weakening certificate validation.
- **Cache:** updated successfully at 02:20:03.411; both timestamp and condition text changed from the prior sample.

### Current artifact and test status

- **Installed and physically tested:** QC03, package `com.philco.ultra777.home`, versionCode 777 / versionName `1.0.0-ultra777`, API 19; SHA-256 `F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8`.
- **Latest compiled, offline validated only:** QC04, same package/version/API; SHA-256 `C9870E798A380BDCBC101580F10461D86FDB1258D0628E6F520EED1D727631BD`. It contains lint-only annotations versus QC03 and was not installed.
- **Rollback:** A57 archive `output/releases/philco-home-A57-13BADB2D.apk`; SHA-256 `13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`.
- **Slim stage:** remains EB256 at `build/system_stage_slim/app/PhilcoUltra777/PhilcoUltra777.apk`; SHA-256 `EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2`.
- **Confirmed:** all five Realtek inputs by source read-back (AR 27, Cable 1, AV 5, HDMI 1 18, HDMI 2 17); HDMI 2 restored; PhilcoOS remains persistent Home; QC03 Quick Control hardware (volume, Mute, image brightness, backlight) works and original values were restored; native Android/Realtek Settings opens; home capture is 1280×720 and Wi-Fi glyph is in bounds. Automatic weather timer and HTTPS/TLS/cache refresh are now evidenced as above.
- **Still unverified / outside this round:** sustained SmartTube video playback/performance, cold boot/recovery, and OEM live input preview in the launcher. No firmware, `/system`, partition or bootloader change was made. No APK was installed or rebuilt in this round.

**Checkpoint complete.** No code or artifact changes are pending from this verification; continue from QC03 on-TV / QC04 local-only, with A57 rollback preserved and EB256 still in slim stage.


## Final stability pass (2026-11-21)

### Reboot and persistence

- Before reboot the physical TV was online over ADB, Android 4.4.2, PhilcoOS `MainActivity` resumed, HOME preference `mAlways=true`, and `PlayingSource=17` (HDMI 2). One controlled `adb shell reboot` was issued. The TV briefly went offline, returned with `sys.boot_completed=1`, and launched PhilcoOS directly as persistent Home.
- Persistent app state survived: wallpaper scene 1 (same photo visible after reboot), weather city Joinville and cached condition, and quick-control preference data. `PlayingSource` remained 17 throughout; HDMI 2 was not changed by this session. No system/firmware partitions were touched.
- Read-only quick-control panel check after reboot showed TV volume 2%, Mute off (`Silenciar` action), image brightness 50%, and panel backlight 100%. Panel opened and populated hardware readbacks; sliders were not moved, avoiding a repeat of already-approved adjustment tests.
- Launcher PSS was 21,518 kB before reboot and 13,560 kB shortly after boot. After SmartTube exit it was 17,191 kB; with the quick panel open it was 19,228 kB. These are point-in-time PSS samples, not peaks or directly equivalent conditions.

### SmartTube session

- SmartTube opened to Browse and loaded search results for `nature`. Selecting “Raja Ampat: Life in the Coral Kingdom | Free Documentary Nature” entered `PlaybackActivity`, but the screen still showed a loading spinner after 12 seconds and again after an additional 20-second bounded wait. No video frame/audio playback was confirmed; sustained playback test therefore **failed to start**.
- SmartTube PSS samples were 43,945 kB after opening Browse, 67,696 kB after search results, 105,543 kB about 12 seconds after selecting the video, and 191,805 kB after about 32 seconds total while the spinner remained. After returning to Home it measured 67,798 kB. This is a sharp measured increase during the stalled attempt, not proof of a leak; it is a significant performance concern on the TV's limited RAM.
- Filtered post-boot logs contained no `FATAL EXCEPTION` or `E/AndroidRuntime`. They did contain nonfatal ExoPlayer/Dalvik verification warnings for guarded API-26 `AudioFocusRequest` symbols and Chromium favicon callback errors; playback remained stuck despite no fatal crash. The specific stream failure cause is unresolved.
- Returned to PhilcoOS `MainActivity`; `mAlways=true` and `PlayingSource=17` remained. Captures: `output/smarttube_stability_checkpoint.png`, `output/smarttube_results_checkpoint.png`, `output/smarttube_playback_checkpoint.png`, `output/smarttube_playback_end_checkpoint.png`, `output/home_after_smarttube_stability.png`, and `output/quick_controls_after_reboot.png`.

### Artifact state and next task

No APK was installed or rebuilt. QC03 remains the physically tested installation (SHA-256 `F837A27AD5BB8CFB296C5562A875B38FA44DABFB38CF51017447ED411646A6A8`); QC04 remains the latest local-only build (SHA-256 `C9870E798A380BDCBC101580F10461D86FDB1258D0628E6F520EED1D727631BD`); A57 rollback remains preserved (`13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676`); EB256 remains in `build/system_stage_slim` (`EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2`).

**Historical next task from the stability pass:** diagnose SmartTube with bounded playback/log/PSS sampling. This was followed up in the SmartTube final diagnostic checkpoint below; visible playback remains unresolved. Boot recovery remains unvalidated.


## SmartTube final diagnostic checkpoint (2026-11-21)

- Existing artifact review: `output/philcoos_smarttube_playback_test2_20261121.png` shows video frames from the earlier playback; per the user's prior observation that run was AVC 360p with advancing timestamps. This proves the TV can decode at least that earlier AVC stream. The archived `output/philco_ultra777_device_validation.log` contains repeated PlaybackActivity launches and old launcher weather TLS failures, but no `SabrPoTokenError`, media-decoder failure, or playback-network diagnosis.
- Current post-boot logs for the restored SmartTube task contain SABR stream segments (itag 135; sequences 8 and 9), then `The UMP stream is ended` at 02:46:43, with `MediaCodecVideoRenderer` reporting `dropOutputBuffer` at PTS 10,760,000. This suggests the stream/renderer stopped after only an initial part of the content, but the logs do not give a reason. On Home, ExoPlayer released MediaCodec at 02:46:48. No `SabrPoTokenError`, HTTP 4xx/5xx, DNS/UnknownHost, TLS exception, decoder initialization failure, or fatal exception appeared in the available buffer. This narrows the issue: content reached the SABR/media pipeline and decoder; the logs do not identify a token/network/AVC incompatibility as the cause. The `dropOutputBuffer` records dropped renderer output, so it does not prove a visible frame.
- On reopening SmartTube it restored the prior PlaybackActivity and spinner (no new video selection was started). PSS was 105,114 kB while this restored player task was active and 94,035 kB after returning Home. The prior observed 191,805 kB remains the maximum sample from a stalled session; these samples vary by task/cache state and do not establish a memory leak.
- No second, deliberately forced 360p selection was made: the existing 360p AVC success and the current SABR/decoder progress provide evidence that AVC can work, while the current log already narrows the failure to early stream/renderer termination. A further video attempt would add little without the missing stream-specific error record. Visible playback and the exact reason the spinner persisted remain unconfirmed. The prior SabrPoToken report remains unverified because its raw log was not present locally.
- Final state: Home key returned to PhilcoOS `MainActivity`; `mAlways=true`; `PlayingSource=17` (HDMI 2). No APK, launcher, SmartTube package, firmware or settings were changed.

**Disposition:** SmartTube diagnosis is narrowed but not conclusive. Keep installed/tested QC03 and all archived releases. A future playback test should only follow a fresh, user-confirmed choice of a specific stream/quality; the next current-project task is to identify why renderer output is dropped while SABR data advances, with bounded logs and no package or firmware changes.


## SmartTube recovery engineering — fresh playback capture (2026-11-21)

### Official source and compatibility

- Inspected the official SmartTube source at stable tag `32.80s` (commit `41fbc7c60ee28e6f13cee74ff630dac6df2724bf`). The installed app remains versionName `32.80`, versionCode `2470`, minSdk `17`, targetSdk `34`, ABI `armeabi-v7a`. The installed APK was previously pulled and matched byte-for-byte to the official 32.80 armeabi-v7a stable asset (25,569,109 bytes; SHA-256 `F90EEA37B474F848BA37E8BFC37DF5AB31517AA41E12D0D38E185E62378488B8`). No APK was installed or updated in this investigation. The official README declares Android 4.3+; the TV is Android 4.4.2/API 19. This confirms declared API/ABI compatibility, not a guarantee of every video stream. At the source/release check, 32.80 Stable was marked latest and its notes include minor playback fixes. [Official README](https://github.com/yuliskov/SmartTube/blob/master/README.md) · [32.80 Stable](https://github.com/yuliskov/SmartTube/releases/tag/32.80s).
- Android 4.4 does not expose a separately registered WebView provider package on this TV; the framework WebView Chromium libraries are present. More importantly, this session logged `PoTokenWebView4` loading the Botguard challenge and successful initialization. The exact WebView build/version was not established. No WebView initialization exception was observed.

### What `PENDING` means and likely origin

- In the 32.80 source, `StreamProtectionStatus.ATTESTATION_PENDING` maps to `PENDING` when a PoToken is present, and to `PENDING_MISSING` when it is absent. The enum comment describes `PENDING` as a likely cold-start token for which a full token should follow. `SabrProcessor` nevertheless constructs `SabrPoTokenError` for every status except `OK`; the SABR load policy propagates it without retry to `ErrorFixerController`, which switches to another YouTube client. This status is an attestation state, not itself a DNS, TLS, HTTP, or decoder diagnosis. [PoToken status parser](https://github.com/yuliskov/SmartTube/blob/32.80s/exoplayer-amzn-2.10.6/library/sabr/src/main/java/com/google/android/exoplayer2/source/sabr/parser/parts/PoTokenStatusSabrPart.java) · [SABR processor](https://github.com/yuliskov/SmartTube/blob/32.80s/exoplayer-amzn-2.10.6/library/sabr/src/main/java/com/google/android/exoplayer2/source/sabr/parser/SabrProcessor.java) · [load error policy](https://github.com/yuliskov/SmartTube/blob/32.80s/common/src/main/java/com/liskovsoft/smartyoutubetv2/common/exoplayer/errors/SabrDefaultLoadErrorHandlingPolicy.java).
- The earlier 04:15 session directly recorded `PoToken error: PENDING` while Android had restored a prior `PlaybackActivity`; `ErrorFixerController` then attempted recovery. The official controller source explicitly warns that a resumed video may fail immediately after the 60-second PoToken attestation window. This makes an expired/stale attestation on the restored task the strongest explanation for that session, but neither the token age nor the server's complete attestation exchange was recorded, so it is not proven as the sole cause. [ErrorFixerController](https://github.com/yuliskov/SmartTube/blob/32.80s/common/src/main/java/com/liskovsoft/smartyoutubetv2/common/app/models/playback/controllers/ErrorFixerController.java).

### One fresh-video session and result

- Started the log capture before opening SmartTube. The app first restored its old playback task; after returning to results, one different video was selected. No app setting, client, network, firmware, decoder, or installed package was changed. The player loaded SABR, selected AVC itag 135 (`854×480`, 25 fps, `avc1.4D401E`) plus AAC audio, and initialized the Realtek OMX/MediaCodec path.
- The fresh session logged Botguard/PoToken WebView initialization, but this report and the shareable focus log intentionally omit all token values. No `SabrPoTokenError`, HTTP 403/429/5xx, DNS failure, TLS exception, decoder initialization failure, or fatal exception occurred in the fresh-session capture. The current log does not expose the selected YouTube client identifier, so the exact client used cannot be stated.
- The screen was still on a spinner at the 04:29:10 capture; a video image was visible by the 04:30:09 capture and remained visible at 04:31:28. Renderer PTS records advanced to `110,440,000` µs at 04:31:23; the SABR/UMP stream ended at 04:31:59 without a fatal error. This proves the selected AVC stream reached visible playback and advanced for over a minute after first observed image. It does not establish smooth frame delivery throughout, audible continuity, or reliable playback across multiple videos. The spinner-to-picture delay was roughly two minutes from player initialization and remains a concrete startup problem. The logs contain many `dropOutputBuffer` records, so PTS progress alone must not be interpreted as proof every frame was displayed.
- PSS samples: SmartTube 88,107 kB before opening (already a resident background process), 102,785 kB during loading, 113,558 kB during the active attempt, and 102,437 kB after returning Home. Launcher PSS after return was 21,155 kB. These single-session samples show an active-playback increase and a residual process/cache footprint, not a demonstrated leak. At the final idle sample `/proc/meminfo` reported 456,492 kB total, 13,284 kB free and 117,440 kB cached; `top` showed SmartTube idle at about 101,636 kB RSS. RSS is not PSS, cached pages are reclaimable, and no OOM/kill was observed; memory headroom under sustained playback is not fully characterized.

### Artifacts and final state

- Full log capture (unaltered): `output/smarttube_fresh_video_20261121.log`, SHA-256 `7E3F0B085AD37BC41399FF1B4448B4DA89AE859669D9877EC57DF5DC07B717D6`. It contains transient Botguard/PoToken debug output; do not publish or share this raw file. A filtered, token-value-free derivative is `output/smarttube_fresh_video_20261121_focus_redacted.log`, SHA-256 `B3B0F9AB6F31C00D303A51D6DB8FA82BDF7103A3181D65817010A191C9B769E7`.
- Video still at 04:31:28: `output/smarttube_fresh_end.png`, SHA-256 `752DC98C004E8AA7D8524F1BCDA6533959B75661F91F13234821E528691BD411`. Home after exit: `output/philcoos_home_after_smarttube.png`, SHA-256 `26DA0909393361512FE04416AA40D698CFB478EE6808DE22616D1C22E1F19ED2`.
- Final device check at 04:33:56: PhilcoOS `MainActivity` resumed, HOME preference `mAlways=true`, `settings system PlayingSource=17` (HDMI 2). SmartTube was left in the background at its existing version. No app update/reinstall, launcher change, firmware/system write, decoder change, or additional playback attempt occurred.

**Conclusion:** The previous spinner has a plausible app-level cause: resuming a stale playback task after the PoToken attestation window, followed by the app's client-recovery path. A single fresh selection then produced real visible AVC playback without repeating the PoToken error, so the device decoder and declared Android/ABI floor are not the blocker for this stream. The investigation does not prove why startup took about two minutes or that playback is generally reliable. Keep QC03 as installed reference and preserve QC04/A57/EB256 unchanged. The concrete remaining SmartTube issue is long time-to-first-frame and incomplete evidence of sustained smooth playback; do not change firmware or decoder based on this session.


## Project resumption checkpoint — SmartTube (2026-11-21)

- Read the prior SmartTube diagnosis and inspected the available playback screenshots/logs before any new playback test. The earlier AVC 360p capture shows a decoded picture; the more recent captured session remained on a spinner. The SABR log described above shows received itag 135 segments, PTS 10.76 s and then UMP end/renderer output drops; it does not contain an explanatory playback exception. No raw `SabrPoTokenError` record is available locally. The diagnosis remains stream/renderer stall of unknown cause; network/TLS/token/AVC incompatibility is not proven.
- Current read-only TV check at 04:12 -03:00: device online (`smart_tv001` / `rtd298x_tv001`), Android 4.4.2, PhilcoOS `MainActivity` resumed, HOME preference `mAlways=true`, `PlayingSource=17`, launcher PSS 23,360 kB. SmartTube installed metadata is versionName 32.80 / versionCode 2470 / targetSdk 34. Device log buffer currently contains no retained SmartTube playback trace from this session; no new playback was started because the previous controlled attempt already demonstrated the spinner and did not expose a cause.
- No package was updated/reinstalled; no app settings, launcher, firmware or decoder configuration was changed. HDMI 2 and PhilcoOS Home remain selected/default. All release artifacts remain preserved.

**Next task:** obtain one fresh, focused log capture around a deliberate selection of a known AVC 360p video only when ready to run another bounded playback test; preserve the log before leaving the player. Capture `SabrPoToken`, HTTP/Cronet/SABR, ExoPlayer renderer/decoder, and PSS before/during/after. Current retained evidence does not justify changing SmartTube or firmware.


## SmartTube controlled capture — PoToken cause confirmed (2026-11-21)

### Capture conditions and outcome

- Before launch at 04:14:20 WGT (-03:00), TV was online; `MainActivity` was foreground, HOME preference `mAlways=true`, `PlayingSource=17`; launcher PSS 23,408 kB. SmartTube process already existed in background/cache at PSS 85,739 kB, so this is not a cold-start baseline. A continuous `adb logcat -v time` capture was started before launch; previous logs were not cleared.
- One SmartTube session was opened from the Home tile. Android restored its existing `PlaybackActivity` rather than a fresh browse/video selection; no second video was selected and no app settings were altered. The player selected AVC 480p (itag 135, 854×480 at 25 fps), despite 360p being the preferred diagnostic quality; the quality UI was unavailable while the restored task was loading.
- The capture shows `SabrPoTokenError: PoToken error: PENDING` at 04:15:19 while `SabrProcessor.processStreamProtectionStatus()` parsed stream protection status. `ExoPlayerController.onPlayerError` reported the same error and `ErrorFixerController` began automatic recovery. This directly confirms the token-pending condition as the cause of that playback failure/retry.
- Network/decoder evidence: SABR/UMP data blocks continued arriving after the error; ExoPlayer configured `video/avc` successfully through `MediaCodec` at 854×480 and reported output format. Video renderer PTS reached 29.08 seconds with repeated `skipOutputBuffer` / `dropOutputBuffer`; the UMP stream ended and recovery was retried. No HTTP 403/429/5xx, UnknownHost/DNS, TLS exception, decoder initialization failure or fatal app exception was recorded. Thus this captured failure is not explained by loss of network connectivity or inability to initialize AVC; the token error is the confirmed cause, while why the playback surface remained on the spinner despite renderer timestamps is not determined. Screenshot at 04:15:49 still showed spinner.
- PSS: SmartTube 85,739 kB before launch (already-running background process), 117,696 kB during loading/recovery at 04:15:34, and 103,564 kB after returning Home. Launcher PSS after return was 22,084 kB. These conditional samples show a temporary increase during active playback/recovery, not a proven leak.

### Preserved diagnostic artifacts

- Full capture: `output/smarttube_controlled_20261121.log` (1,195,279 bytes; SHA-256 `E563502DEB4207E0CC62D769510DFF1E249F86E96A3FC7AA93BB36BA9D2E6A64`). It began before the session and contains the available logcat buffer; prior logcat was not cleared.
- Focused error context: `output/smarttube_controlled_20261121_focus.log` (SHA-256 `DE284C35E65A149EE2E4F16CE8C1E65F194FB8EB789A3D31433DE2B1BDBFEAF0`).
- Spinner capture: `output/smarttube_controlled_waiting.png` (SHA-256 `0DAEFBADE2EDAA1F25057CABEE940919C5E1C13017360C050821CB5E539C7002`).

### Final device state

SmartTube was exited after the log and spinner were captured. PhilcoOS `MainActivity` is foreground and remains persistent Home (`mAlways=true`); `PlayingSource=17` (HDMI 2). No app was updated/reinstalled, no decoder/network setting was changed, and no launcher or firmware file was modified. The one-session test is complete; do not repeat playback without new evidence or a separately identified stream-specific hypothesis.


## MediaBrowser and Aptoide device diagnostic (2026-11-21)

- **CONFIRMADO — device:** ADB target `192.168.0.71:5555` reported `smart_tv001` / `rtd298x_tv001`, Android 4.4.2 and fingerprint `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170531.092306:eng/test-keys`.
- **CONFIRMADO — Aptoide:** `cm.aptoidetv.pt.changhong_philco_44` already appeared disabled (`enabled=2`, last disable caller shell), while package path remains `/system/priv-app/aptoide.apk` (v2.2.6/code23). No state change was needed. Re-enable with `pm enable cm.aptoidetv.pt.changhong_philco_44` if required.
- **CONFIRMADO — MediaBrowser launch only:** Package v1.0/code1, targetSdk17, `/system/app/MediaBrowser.apk`; manifest declares `VideoBrowser`, `VideoPlayerActivity`, `GridViewActivity`, `MusicActivity`, `PhotoPlayerActivity`, plus external-storage read/write. Starting `VideoBrowser` brought its Activity/process to foreground. This does not prove successful enumeration or playback.
- During the idle inspection, `dumpsys media.player` showed no active media client mapping. `media.rtkplayer` service exists, but `dumpsys media.rtkplayer` returned no useful state. Filtered logs showed repeated `MediaPlayer stop called in state 0`; no file playback, OMX initialization, or hardware decode was observed. Firmware decoder library presence is not playback evidence.
- Storage: `/data` had 3.1 GB free; `/storage` and `/mnt/media_rw` were reported as 222.9 MB tmpfs. `sdcard0` maps to emulated storage; `udisk` existed but no mounted USB media/files were visible. No file transfer or playback test occurred. `/system` was observed mounted `rw`; no writes/remounts were performed and the mount's cause is unknown.
- Read-only APK evidence: `output/MediaBrowser-system-baseline.apk`, 8,041,862 bytes, SHA-256 `CF073617F5143370E449EA00E495DBB6EFEEBE7DEB247CCC26178AD80C760AB3`. `aapt dump badging` hit an unresolved icon-resource reference; package/version and `xmltree` manifest were readable.
- Final state restored: PhilcoOS `MainActivity`, persistent Home `mAlways=true`, `PlayingSource=17` (HDMI 2). Aptoide remained disabled. No install/build/firmware/partition changes; no prolonged playback test.

**Status:** Activity/process launch is `DEVICE_VALIDATED`; MediaBrowser playback and Realtek decoder use remain `PENDENTE`. See `docs/AI_TEST_RESULTS.md` and `docs/AI_HANDOFF.md` for command/evidence detail.


## Local AVC 720p30 benchmark preparation — blocked before transfer (2026-11-21)

- Revalidated TV fingerprint/model/API, PhilcoOS persistent Home, and `PlayingSource=17`; `/sdcard/Movies` is empty and `/data` reports 3.1 GB free.
- No project video was available. No FFmpeg/FFprobe or common local Python encoder modules were found; no software was installed. Unrelated personal MP4s were not repurposed without verified format/content.
- Saved the read-only device baseline under `output/media_benchmark_20261121_063337/`. `adb exec-out screencap -p` failed with status `4294967295`, so there is no screenshot artifact. No test media was generated/transferred and no Media Scanner/playback was started.
- **Blocked:** no verified test clip can be produced within the installed-tool constraint. MediaBrowser playback, actual OMX decoder selection, FPS, dropped frames, CPU, and during-playback memory remain untested. Existing decoder library presence is not evidence of runtime use.
- TV was not written to; Home remains default and HDMI 2 selected. Aptoide remains disabled. No app, firmware, system partition or bootloader modification.


## Pós-teste: diagnóstico de logs e estabilidade (2026-11-21)

- Coletados sem limpar buffers: logcat main/system/events/crash/radio/completo e snapshots de Activity/window/memória/input/power/media/packages/storage/top. Evidências em `output/post_test_diagnostics_20261121_064151/`; diagnóstico redigido em `DIAGNOSTICO_TECNICO.md`.
- **CONFIRMADO:** sem FATAL EXCEPTION, ANR, OOM, Permission Denial ou Fatal signal nos buffers disponíveis. Buffer `crash` não suportado (`/dev/log/crash` ausente).
- **CONFIRMADO:** 18 eventos `NotificationService` informam que PhilcoOS já havia atingido o limite de 50 Toasts e mensagens novas foram suprimidas, 06:39:35–06:39:39. Chamadas Realtek de volume/source/mute ocorrem na mesma janela; não há valores numéricos correlacionados nem os textos dos Toasts, então a operação incorreta referida pelo usuário não pode ser identificada.
- **OBSERVADO:** alternâncias `com.tsb.tv/.Tv_strategy` ↔ Home (06:37:43–06:38:37) e abertura de Configurações às 06:40:13, retorno à Home às 06:40:15. Dalvik abortou compilação JIT de traces duas vezes e voltou ao interpretador. HWC registrou `FrameBufferHz` em linhas E sem evidência de falha funcional. Sem impacto medido.
- Snapshot ActivityManager: RAM total 456,492 kB, Free RAM 219,019 kB; PhilcoOS PSS 20,712 kB, MediaBrowser 9,456 kB, mediaserver 2,040 kB. `/proc/meminfo` MemFree 14,964 kB e Cached 77,264 kB são categorias distintas. Sem evidência de pressão fatal.
- Estado final: PhilcoOS MainActivity, Home padrão `mAlways=true`, HDMI 2 `PlayingSource=17`; nenhum ajuste foi feito. Os logs originais permanecem locais e não devem ser compartilhados integralmente.

**Conclusão:** throttling de Toast é problema de UX/diagnóstico comprovado; a causa das falhas funcionais relatadas permanece desconhecida sem o sintoma específico. Próximo passo é correlacionar uma ação indicada pelo usuário a uma captura curta e estruturada antes de corrigir código.

## QC06-r2 — Clima compacto e relógio (2026-11-21)

- Cartão do clima em 420 × 86dp, com vidro escuro mais contrastante, glifo Canvas de 48dp e temperatura branca Roboto Light 38sp com unidade °C. Cidade, condição e crédito do provedor preservados. Ícones variam pelos códigos meteorológicos; não foram criadas métricas sem fonte nem adicionadas dependências.
- Relógio em Roboto Light 94sp; data em Roboto Medium 20sp branca logo abaixo. Wallpaper, dock e funções existentes mantidos.
- QC06-r2 compilado e instalado. SHA-256 `7745F02D77F64F676B515DE30718B48D429A44B7BDBDE9700D01A9B5B137A01C`; API 19, assinatura v1 e alinhamento verificados; hash do APK extraído da TV confere.
- Captura real 1280 × 720: `output/qc06_weather_clock_r2.png`. A TV mostrava 26°C / Nublado e o ícone correspondente. MainActivity retomada; sem fatal exception correspondente no trecho de log consultado. Estado parcialmente ensolarado não foi capturado porque não era a condição atual.
- QC05-r4.5 e a cópia exata do APK instalado antes da atualização estão preservados em `output/releases/` e `output/backups/`. Próxima ação: revisão visual do usuário; firmware intacto.

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

## QC07-r3.12 — screensaver weekday clipping fix (2026-11-21)

- Removed the screensaver date's leftward 8dp translation, which clipped the first letter of the full Portuguese weekday. Added 8dp horizontal internal padding so the full date has glyph clearance while remaining centered. No other visual or functional changes.
- Build passed; API/package identity retained; `zipalign` and v1 signature checks passed. Release SHA-256: `235FB14DE996BCD5B78DBF0E83D452B89A76402030B1E92FA9697A3DA88DF688`. Installed pull hash matches exactly.
- On the TV, the 1280×720 screensaver capture `output/qc07-r3.12-screensaver-final-1280x720.png` shows “Sábado, 21 de novembro de 2026” without clipping. Returning to Home succeeded; HDMI 2 remained active in `output/qc07-r3.12-home-after-saver-1280x720.png`. QC07-r3.11 remains preserved as rollback.
- Open issue: user reports the Quick Control Center “brilho da imagem” setting changes nothing visibly. Antigravity is assigned a bounded source/OEM-evidence diagnosis; no brightness values were changed in this round.


## Home visual correction — 3D weather PNGs, dock edges and screensaver date (2026-11-22)

- Added three small transparent 3D weather icons derived from the supplied reference artwork and mapped rain/partly-cloudy/cloud conditions for both Home and Screensaver. Existing icons remain for sun, snow, fog and thunder.
- Expanded the dock viewport to screen width while retaining 64dp left/right safe inset and unchanged tile dimensions/spacing. Focused SmartTube and Settings are both fully visible in 1280×720 captures.
- Removed Screensaver date's 8dp horizontal padding and aligned it left with the clock; full `Domingo, 22 de novembro de 2026` visually confirmed.
- Final candidate is installed, SHA `5D12F95C21B974F3910FE4687CB01944052FB9FAC9D370908B5846461C5CAF2B`; details and evidence in `docs/AI_TEST_RESULTS.md` under the matching 2026-11-22 entry. No firmware/source-input change.


## Dock endpoint focus — final fixed-row correction (2026-11-22)

- Final r5 keeps all eight tiles fixed and visible; endpoint cards use the existing clear focus border without enlarging beyond their bounds. The temporary r4 auto-scroll was rejected after device screenshots showed the opposite tile partly off-screen.
- SmartTube and Settings focus were visually confirmed on the TV; APK SHA `53E294BBB988F4C1D04B75A33A046525505842CB28A80A79E7CB6939958430DC`. Full evidence and rollback references: `docs/AI_TEST_RESULTS.md`, “Dock endpoint focus correction — final r5”.


## Home date separator — 2026-11-22

Replaced only the Home date's large bullet with a smaller middle dot (`Domingo · 22/11/2026`). Built and installed; screenshot `output/captures/home-date-dot-small-20261122.png`. Release SHA-256: `E6D737E594F3831D85EE5E7C754C55095CE469F0D9A73D9C346AF76E801A1F8A`.

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

## Idle brightness schedule update — 2026-11-23
The idle policy now keeps the existing 5-minute screensaver, sets Realtek backlight to 40 at 15 minutes and 20 at 30 minutes, and preserves/restores the original value. The 30-second standby warning begins at 59:30; absent music, `TvManager.standBy()` is requested at 60 minutes total. If the shared Music Engine state is `PLAYING`, shutdown is deferred; after it stops past the deadline, a fresh 30-second warning is given.

Build `assembleRelease` succeeded; APK `output/releases/philco-home-idle-backlight-15-30-60m-20261123.apk`, SHA-256 `1C63DCBF371287A1AD7089B91E6E2F5C1D21B71424974EA1EB6BFECE10F40904`. Installed to `/data/app` and MainActivity resumed. No `/system` changes. Actual 15/30-minute brightness, wake restoration, one-hour standby and music deferral have not yet been observed on the TV. After reboot, the previous `/data/app` update was absent and only base `/system/app/PhilcoOS.apk` was listed; current release was reinstalled as a data-app update.
## Boot animation override — 2026-11-23

Validated and installed the existing 15 fps archive at `/data/local/bootanimation.zip` (requested exact archive name was absent). SHA-256: `EB9D2105AEBDB4BE64B09336337591D04D19C788EAC27A0D6BDC57C1280382BAC`. The original `/system/media/bootanimation.zip` was backed up locally to `output/backups/bootanimation-original-20170530.zip` (SHA-256 `622F473EEABDD95BD2715891F783EFCC6B8CCA86A348E2B1A252971D1EEFE0EB`) and remains unchanged. `desc.txt`, ZIP integrity, frame dimensions and stored-PNG compression match Android 4.4 expectations. Reboot completed and PhilcoOS Home resumed; animation playback was not visually captured. No firmware or `/system` writes. Rollback: delete `/data/local/bootanimation.zip` and reboot to use the original.

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

## Last-input restore preference — 2026-11-23

PhilcoOS now offers an enabled-by-default Quick Control Center preference to reapply the last Realtek input confirmed by `getCurLiveSource()` when Android broadcasts `BOOT_COMPLETED`. It stores the last confirmed source ID in app preferences and makes bounded `TvManager.setSource()` retries without launching the OEM TV Activity. Release APK `output/releases/philco-home-last-input-restore-20261123.apk` SHA-256 `437B504797B985CB466647CA7B2A9F30852E96BA628A05EC7616B72431CC566D`; build, installation, zipalign, and D-pad toggle were validated. Current TV state: Android 4.4.2, `PlayingSource=27`, PhilcoOS MainActivity resumed. No reboot was run, so actual boot restoration and visible AV/HDMI output remain unverified. Power/restart shortcut is deferred. Git metadata is absent.



## Weather range labels and alignment — 2026-11-23

Labels now read Máx. and Min. Their labels and temperatures use fixed columns; both numeric values are right-aligned to the same edge. Built and installed successfully, then visually confirmed at 1280×720. APK SHA-256: 7A2284D50CF98D8C46D516734BD0EB2A0ED848CB1194614DD36AABBD695E1C6A. Capture: output/captures/weather-max-min-aligned-20261123.png.

## Energy-control feasibility — 2026-11-23

The PhilcoOS APK requests `android.permission.DEVICE_POWER`, but Android 4.4.2 did not grant it, even after installing the same-cert release in the existing `/system/app/PhilcoOS.apk` slot and rebooting. `pm grant` reports this protected permission is not changeable; the earlier Realtek `TvManager.standBy()` call returned false. Consequently, no app-level power controls are shown or claimed functional, and no shutdown/sleep call was made. A launcher cannot power on a fully powered-off TV. The previous system APK is backed up locally and at `/system/app/PhilcoOS.apk.bak-power-20261123`. The controlled reboot returned to PhilcoOS Home with source 27. Rear joystick candidate is `/dev/input/event2` (`Smart_TV_Keypad`); key mapping remains pending.


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

## Home-to-Music navigation — 2026-11-23

The eight primary Home cards retain their current size and layout. With focus on Ajustes, the RIGHT direction opens the existing full-screen Music page; the tile subtitle displays → Música. Center/OK continues to open Quick Controls, and Android Back returns focus to Ajustes. This route is compiled in the local candidate but has not been installed or visually checked on the TV.
### Music page motion — 2026-11-23
From Ajustes focus, one press of RIGHT enters Music directly with a short horizontal slide from right to left. Android Back returns with the reverse slide and restores Ajustes focus. The transition uses a single translate animation in each direction (260 ms); no blur or continuous animation is used. Compiled locally; physical appearance is pending confirmation.
## Home Dynamic Bar — 2026-11-23

A compact graphite/gold row below the weather card displays a 2D music glyph, track title and TOCANDO AGORA / PAUSADA state while local music is active. It hides while idle/stopped, listens to the existing playback observer only while Home is resumed and does not decode album art or animate continuously. Compiled locally; the TV was offline before installation, so visual validation is pending.
### Instalação combinada Home → Música + Dynamic Bar — 2026-11-23
A versão local mais recente foi instalada como atualização do app do sistema; o APK original em `/system/app` ficou intacto. O arquivo ativo em `/data/app` foi comparado com o candidato e os SHA-256 coincidem (`77BA251A817EA92EFE5F452D215B155EBA69DA31557BAE9949E20ECF65B9CC0B`). A cópia anterior foi arquivada em `output/backups/PHILCOOS_DYNAMIC_BAR_DEPLOY_20261123/`. A TV estava acordada após a instalação. A aparência da Dynamic Bar e a navegação lateral ainda precisam de confirmação na tela real.

## Music UI r2 — referências de TV e música — 2026-11-23
Reorganizada a tela Música com foco na leitura a distância e navegação pelo D-pad: identidade PhilcoOS no topo, estado compacto, faixa atual com símbolo musical 2D, lista local organizada e controles de reprodução agrupados no rodapé. A composição usa referências de hierarquia do Apple Music para tvOS e Spotify para Smart TV, sem reproduzir suas marcas nem exigir serviços externos. Permanece um fallback de capa abstrato até integrar capas de álbum locais redimensionadas. Candidato r2 instalado e visualmente conferido em 1280×720; screenshots em `output/captures/philcoos-music-ui-redesign-r2-20261123.png` e `output/captures/philcoos-music-back-home-20261123.png`.

## Music reference UI implementation — 2026-11-24
`MusicActivity` now implements the supplied PHILCOOS Música composition using native API 19 views, existing wallpaper and translucent drawable panels. The actual MediaStore library, search filtering, `PlayerController`/`MusicPlaybackService`, seek/progress and AudioManager volume remain wired; no fake catalogue or new dependency was added. A small local Canvas glyph replaces the package-private shared volume view. Release APK: `output/releases/philco-home-music-reference-20261124.apk`, SHA-256 `3C216E85EB6FCA75807375211429D7FCD48DDA7AFBDA56D264ED454CECBDB092`; build, zip alignment and v1 signature checks passed. Not installed or visually confirmed on the TV; see `AI_TEST_RESULTS.md` for the outstanding device checks.

## Music surgical refinement — 2026-11-24
The MusicActivity geometry now follows the 1280×720, 160 dpi coordinates: top tabs x=356–924, panels y=141/h=338, cards y=567–609, player y=609. The panels retain their 43/57 split. Cards are compact 42 dp high to avoid overlap with the player. MediaStore failures, an empty library and a no-result search now have distinct messages, and cursor close is protected by `finally`. No player/service/backend, dependency or manifest changes. APK `output/releases/philco-home-music-refined-20261124.apk`, SHA-256 `522EC416A1DE12F12CB8445F7728ADD9962C4E95B242634C9F78243F864C1D77`; compiled and statically verified, not installed or visually confirmed. See `AI_TEST_RESULTS.md`.

## Atualização 2026-11-24 — Música
A tela Música foi refinada sobre o backend existente para leitura em TV a 1280×720: títulos/artistas ampliados, busca mais clara, cards alinhados sem sobreposição com o player, foco do D-pad previsível, seek direto pelas setas e controle de volume sem OSD cobrindo o conteúdo. Captura real e resultados estão em `output/captures/philcoos-music-final-overview-20261124.png` e `docs/AI_TEST_RESULTS.md`. Reprodução permanece baseada na biblioteca local; nenhuma integração de streaming foi adicionada.

## PHILCO MUSIC Pro — 2026-11-24
Player inferior e mini-player da Home agora compartilham a sessão existente do serviço de música. Interface de TV feita com Views nativas/API 19, biblioteca MediaStore, busca tolerante a acentos, favoritos e fila/repetição/aleatório. APK compilado e instalado como atualização; HOME preferido permaneceu PhilcoOS. A validação visual não foi concluída: ADB fechou ao abrir a tela Música para captura. Veja `AI_HANDOFF.md` e `AI_TEST_RESULTS.md`; não considerar estes controles visualmente ou funcionalmente validados na TV.

## Integração Qwen do player — 2026-11-24
Foram integradas correções selecionadas para conexão do serviço, histórico por URI, atualizações de biblioteca que não deslocam foco e o ID de navegação do mini-player da Home. O código foi compilado e conferido offline para API 19; a nova versão não foi instalada nem validada visualmente na TV. Detalhes em `AI_HANDOFF.md` e `AI_TEST_RESULTS.md`.

## Metadados locais de música — 2026-11-24
O modelo de faixa agora pode guardar álbum, gênero e tags; a pesquisa local considera esses campos. O MediaStore fornece o índice de gêneros em lote. Tags incorporadas são consultadas como fallback apenas quando faltam título, artista ou álbum, para limitar custo de leitura no hardware de 512 MB. APK compilado e conferido offline para API 19; sem instalação ou validação física. Acesso a gêneros depende dos dados disponíveis no MediaStore da TV.

## Philco Music Refeito r5 — validação no dispositivo — 2026-11-24
O r5 foi compilado e instalado como atualização do pacote existente, sem limpar dados nem mudar o HOME padrão. Biblioteca e capa exibiram nomes/artistas Genesis na TV 1280×720. Volume+ restaurou `STREAM_MUSIC` de 0 para 7 e o player permaneceu aberto. APK: `output/releases/philco-music-refeito-metadata-r5-20261124.apk`, SHA-256 `6E42987B880B27B6A456354FC64401DF98F786A8F20FDA20A92E20083BBFB5B1`. Captura real: `output/captures/philco-music-refeito-r5-final-20261124.png`. O acionamento do mute gráfico não foi validado por toque ADB; consulte `AI_TEST_RESULTS.md` para limites e backup/rollback.

## Philco Music UI r6 — candidato recompilado — 2026-11-24
A interface foi reorganizada para leitura a distância: transporte com texto junto dos ícones, volume num painel próprio e funções secundárias agrupadas. Miniaturas consultam arte local disponível de forma assíncrona e limitada em memória. O volume não requisita foco no slider ao mudar, separando as teclas de volume do seek e da seleção de faixas. APK r6 compilado e verificado para API 19: `output/releases/philco-music-ui-refined-r6-20261124.apk`, SHA-256 `2E6A8AB6EE0C3291E0E30302DD3E8C19C275B4C8948E395AC8920A5893715B29`. A TV ficou indisponível no ADB antes da instalação; não há captura nem validação do r6 no hardware. Fontes anteriores e APK r5 foram preservados. Consulte `AI_HANDOFF.md` e `AI_TEST_RESULTS.md`.

## Philco Music UI r6.1 — correções pontuais — 2026-11-24
A revisão corrigiu o acesso às capas MediaStore nas linhas da biblioteca, separou as entradas de cache por tamanho, limitou pedidos pendentes de decodificação e protegeu callbacks atrasados de faixas anteriores. Backend e navegação da r6 foram preservados. APK `output/releases/philco-music-ui-corrected-r6-1-20261124.apk`, SHA-256 `5B59FAC82E63FDFCCD475698444CB83FCFB5A28C0A3743EF54ED227897E24231`, build/API 19/assinatura validados offline. A TV estava ausente do ADB, então não foi instalado nem validado visualmente. Ver handoff e resultados de teste para limitações e próximo passo.

## Fechar Central de Controle ao suspender — 2026-11-24
O diálogo da Central de Controle passa a fechar quando a tela apaga, quando a Home pausa e antes dos pedidos de standby do middleware/PowerManager. A atualização foi instalada preservando HOME; o print mostra o PhilcoOS restaurado, HDMI 2 ativa e volume 7. O ciclo de standby real não foi disparado, então essa resposta ainda aguarda validação física. APK `output/releases/philco-home-r6-2-standby-menu-close-20261124.apk`, SHA-256 `D28A9E9B312F670F4883CAD2C7DA907DD0798E7FE8F9DA759C1629D7691967E0`.

## 2026-11-25 — Philco Music UX básico
Foi preparado um candidato API 19 com navegação de faixas compartilhada entre Home e MusicActivity, regra de faixa anterior aos 45 segundos, seek com modo explícito de ajuste pelo OK, cards de coleção com títulos em duas linhas e retomada que limpa duck de áudio temporário sem amplificar o player. Build e validação offline passaram. APK: `output/releases/philco-music-basic-ux-20261125.apk` (SHA-256 `46028E814FC83DE63C6FD4D35FF8AEDC899061D0CB697197384FFD68DA249336`). Não instalado; operação, aparência e volume comparado ao HDMI aguardam validação na TV.

## Philco Music — revisão visual local (2026-11-25)

A tela Música recebeu um redesenho de apresentação limitado a `MusicActivity.java`. A direção usa o wallpaper escurecido como atmosfera, painéis de carvão com contorno dourado discreto, abas somente para destinos reais, capa e faixa atual em composição horizontal, biblioteca local em painel próprio, cartões separados para continuar a coleção e uma barra inferior de reprodução com controles claros e sem legendas pequenas sob os ícones.

O serviço, a fila, a busca local, favoritos, volume, mute, seek e integração Realtek permaneceram no código. Nenhuma dependência nem arquivo de sistema foi adicionado ou alterado. O APK local passou na compilação, assinatura v1 e verificação de alinhamento. **Aparência em 1280×720 e navegação real ainda não foram validadas na TV.** O candidato está em `output/releases/philco-music-visual-refresh-20261125.apk` (SHA-256 `617CE875059EE451C00D490B850E36FFCE9EAB740A048DC9901FCA148B27420C`).

## Philco Music — patches visuais Qwen (2026-11-25)
Foi integrado à `MusicActivity` um gradiente superior escurecido com tonalidade amostrada da capa atual e aquecida em dourado PhilcoOS; o fallback/capa ausente é tratado para evitar vazamento de cor da faixa anterior. O marcador do seek aparece com foco ou ajuste ativo e a linha divisória da biblioteca ficou mais leve. Backend preservado. APK compilado e validado offline: `output/releases/philco-music-qwen-visual-fixes-20261125.apk`, SHA-256 `04F9FFBA106B4F166FADAADE1E4DE10EEF10AD490F718E79A885F56632E83957`. A captura e a navegação na TV ainda não foram validadas.

## Philco Music — instalação dos ajustes Qwen (2026-11-25)
O APK de ajustes visuais foi instalado como atualização via ADB sem limpar dados. HOME permaneceu associado ao PhilcoOS (`mAlways=true`). Captura real em 1280×720: `output/captures/philco-music-qwen-visual-fixes-device-20261125.png`. A versão abriu e exibiu a interface; o foco do seek com faixa ativa, reprodução, pausa e volume ainda não foram testados. Backend permaneceu inalterado; nenhuma reprodução ou mudança de volume foi feita nesta validação.

## Philco Music R6 — 2026-11-25
R6 acrescenta foco visível em Buscar e na biblioteca, receptor temporário de músicas/fotos pela LAN, dez wallpapers otimizados e rotação Home/Música em 7/10 minutos. A tela ambiente pode mostrar a faixa e progresso; standby elétrico permanece com tela apagada. O perfil Realtek foi ajustado para maior presença nos médios/agudos sem mudar balance/backlight. APK: `output/releases/philco-music-wallpapers-transfer-eq-r6-20261125.apk`; SHA-256 `1C2F2CC7D11CB3C0D663BEEA2E69FA8F585E9596FCEBBE884C7442189F917F19`. A transferência foi validada por PC na rede local; controle físico, reprodução real, percepção auditiva, rotação cronometrada e barra durante música continuam pendentes.
