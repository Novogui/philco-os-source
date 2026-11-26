# PhilcoOS — Verified Keep Components Report

**Device:** Philco PH42F10DSGW (Smart TV 42" LED)  
**Mainboard:** CVTE TP.RT2984T.PB801  
**Target Platform:** Realtek RTD2984TD-GR (`rtd298x`)  
**Audit Date:** 2026-11-20  
**Status:** 100% Verified with Exact Paths, Byte Sizes, and Dependencies  

---

## 1. Bootcode & Kernel Components `[CONFIRMED]`

These components initialize the hardware, configure CPU clocks, establish DRAM timing, and bootstrap the operating system. They must remain bit-for-bit identical to the factory release.

| File Path in Firmware | Size (Bytes) | Role & Supporting Evidence |
| :--- | :--- | :--- |
| `0.vmlinux.rescue.bin` | 7,229,664 | Emergency recovery kernel loaded into RAM during USB update sequence. |
| `package4/bootloader.tar` | 4,741,120 | Contains `bootcode.bin` (322 KB), `aux.bin` (25 KB), `hwsetting.bin` (3 KB), `params.bin` (340 B), and `vmlinux.rescue.rtd298x.bin.lzma` (4.35 MB). Burned to eMMC boot partitions `mmcblk0boot0`/`boot1`. |
| `package4/vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin` | 14,192,628 | Operational Linux kernel (`Linux version 3.7.2-298x #192 SMP PREEMPT`). Contains built-in drivers for RTD2984 I2C, eMMC, USB EHCI/OHCI, and display controllers. |

---

## 2. Hardware Microcode & Coprocessor Firmwares `[CONFIRMED]`

The RTD2984 SoC offloads real-time audio decoding and video scaling/decoding to dedicated on-chip DSP cores. The Android OS cannot play sound or decode video without these microcode images.

| File Path in Firmware | Size (Bytes) | Role & Supporting Evidence |
| :--- | :--- | :--- |
| `package4/bluecore.audio` | 1,916,960 | Realtek Audio DSP microcode. Handles AC3, DTS, AAC, PCM mixing, multi-band equalizer, and SPDIF/I2S output. Loaded into hardware at boot (`fw = audioKernel`). |
| `package4/video_firmware.bin` | 3,545,576 | Realtek Video Processing Unit (VPU) microcode. Handles hardware MPEG-2/4, H.264 video decoding, de-interlacing, motion estimation, and Direct Video Overlay (DVO). Loaded at boot (`fw = videoKernel`). |
| `package4/bootfile.audio` | 40,750 | Audio coprocessor initialization bootstrap header. |
| `package4/bootfile.image` | 94,204 | Video coprocessor initialization bootstrap header. |

---

## 3. Factory Calibration & Panel Parameters `[CONFIRMED]`

These files define LCD panel timings, LVDS channel mapping, gamma curves, and factory white balance. Altering them can damage the panel or cause severe display distortion.

| File Path in Firmware | Size (Bytes) | Role & Supporting Evidence |
| :--- | :--- | :--- |
| `package4/factory.tar` | 645,120 | Contains `bin_panel/panel.bin` (128 B), `bin_panel/PanelParam/vip_default.bin` (613 KB), `hikeen_channel/atvChannel.bin` (5.9 KB), and `hikeen_channel/idtvChannel.bin` (3.0 KB). Unpacked to `/tmp/factory` by `install_a`. |

---

## 4. Realtek Rootfs Subsystem (`/system/rtk_rootfs`) `[CONFIRMED]`

The TV stack runs as an autonomous subsystem housed in `/system/rtk_rootfs/`. It must be preserved in its entirety.

### 4.1 Key Binaries & Daemons
* `/system/rtk_rootfs/usr/local/bin/TvServer` (33,322,616 bytes):
  * **Role:** Master TV backend daemon. Controls the silicon tuner, demodulator, HDMI switch IC, transport stream demuxers, and channel database.
  * **ELF `DT_NEEDED` Dependencies (16):**
    `libdl.so`, `libstlport.so`, `liblog.so`, `libutils.so`, `libgabi++.so`, `libc.so`, `libstdc++.so`, `libm.so`, `libfreetype.so.6`, `libEitvUtils-2.0.so`, `libEitvLogger-2.0.so`, `libEitvISDBTUtils-2.0.so`, `libEitvPal-2.0.so`, `libEitvGingaCommands-2.0.so`, `libEitvSockets-2.0.so`, `libEitvAppManager-1.0.so`.
* `/system/rtk_rootfs/usr/local/bin/RootApp` (423,392 bytes):
  * **Role:** Process supervisor for `TvServer`. Restarts `TvServer` on failure and handles watchdog events.
* `/system/rtk_rootfs/usr/local/bin/EITV/jar/javatv.jar` (499,175 bytes):
  * **Role:** Brazilian Ginga JavaTV interactive middleware runtime.
* `/system/rtk_rootfs/usr/local/bin/Resource/dtv_table/freqTable.isdb` (1,665 bytes):
  * **Role:** Brazilian ISDB-Tb channel frequency lookup table.

### 4.2 Kernel Driver Modules (`/system/rtk_rootfs/lib/modules/...`)
* `mt7601Usta.ko` (1,954,788 bytes):
  * **Role:** MediaTek MT7601U USB Wi-Fi driver. Active Wi-Fi driver confirmed in `dmesg`.
* Realtek fallback Wi-Fi modules: `8188eu.ko`, `8188fu.ko`, `8192cu.ko`, `8192du.ko`, `8192eu.ko`, `8812au.ko`.
* Filesystem drivers: `ufsd.ko` (893 KB), `jnl.ko` (48 KB) (NTFS/HFS external drive mounting).

---

## 5. Proprietary Hardware HALs & Dynamic Libraries `[CONFIRMED]`

These libraries link Android userspace frameworks to Realtek hardware drivers.

| Library Path in `/system` | Size (Bytes) | ELF `DT_NEEDED` Dependencies | Functional Role |
| :--- | :--- | :--- | :--- |
| `vendor/lib/hw/hwcomposer.rtd298x.so` | 30,204 | `libstdc++.so`, `libc.so`, `libdl.so`, `libcutils.so`, `libsrv_um.so`, `libhardware.so`, `libsync.so`, `libm.so`, `libui.so`, `libsysutils.so`, **`libdirectvo.so`**, `libutils.so` | SurfaceFlinger hardware composer (HWC 1.3). Bridges SurfaceFlinger with DirectVO overlay. |
| `vendor/lib/hw/gralloc.rtd298x.so` | 39,408 | `libc.so`, `libdl.so`, `libcutils.so`, `libsrv_um.so`, `libpvr2d.so`, `libsync.so`, `libcorkscrew.so` | Graphic buffer allocator. Directly binds PowerVR 2D/3D drivers. |
| `vendor/lib/egl/libEGL_POWERVR_SGX544_115.so` | 9,364 | PowerVR userspace core | EGL 1.4 driver for PowerVR SGX544MP. |
| `vendor/lib/egl/libGLESv1_CM_POWERVR_SGX544_115.so` | 423,280 | PowerVR userspace core | OpenGL ES 1.1 CM driver. |
| `vendor/lib/egl/libGLESv2_POWERVR_SGX544_115.so` | 443,764 | PowerVR userspace core | OpenGL ES 2.0 driver. |
| `lib/hw/audio.primary.rtd298x.so` | 27,280 | `liblog.so`, `libcutils.so`, `libtinyalsa.so`, `libaudioutils.so`, `libdl.so`, `libc.so`, `libstdc++.so`, `libm.so` | Primary Android Audio HAL. Connects AudioFlinger to ALSA and Audio DSP. |
| `lib/libdirectvo.so` | 13,544 | `libutils.so`, `libcutils.so`, `libbinder.so`, `libFileDescService.so`, `libc.so`, `libstdc++.so`, `libm.so` | Direct Video Overlay library. Enables zero-copy video playback to hardware planes. |
| `lib/librpccore.so` | 193,884 | `libutils.so`, `libcutils.so`, `libbinder.so`, `libFileDescService.so`, `libc.so`, `libstdc++.so`, `libm.so` | Realtek RPC client. Used by Android TV apps to send commands to `TvServer`. |
| `lib/libRtkFramework.so` | 116,272 | `libstlport.so`, `liblog.so`, `libc.so`, `libstdc++.so`, `libm.so` | Realtek TV framework JNI bridge. |
| `lib/libOMX.RTK.video.decoder.so` | 30,284 | Realtek VPU core | OpenMAX hardware video decoder plugin. |
| `lib/libOMX.RTK.audio.decoder.so` | 42,092 | Realtek Audio DSP core | OpenMAX hardware audio decoder plugin. |
| `lib/libcvtedatacontrol.so` | 30,104 | CVTE vendor core | CVTE picture mode and channel data manager. |

---

## 6. Core TV Applications & Userland Hardware Bridges `[CONFIRMED]`

These APKs provide the primary user interface and hardware event listeners for TV functionality.

| Package Path | Package Name | Size (MB) | Essential Components & Roles |
| :--- | :--- | :--- | :--- |
| `app/TSB_Tv.apk` | `com.tsb.tv` | 3.38 | **TV Broadcast Application.** Implements ISDB-T digital channel viewer, analog TV tuner, channel auto-scan, EPG, and CEC listener (`CecMessageReceiver`). |
| `app/SourceMenu.apk` | `com.hikeen.xymenu` | 0.30 | **Input Source Switcher.** Manages on-screen selection of HDMI 1, HDMI 2, HDMI 3, AV, and TV tuner inputs. Contains `KeyPadService`. |
| `app/com.cvte.keyservice.apk` | `com.cvte.keyservice` | 0.02 | **Hardware Remote & Key Service.** Listens to physical TV chassis buttons and IR remote control scancodes; broadcasts Android `KeyEvent`s. Contains `KeyService` and `KeyReceiver` (`BOOT_COMPLETED`). |
| `priv-app/RTKSettings.apk` | `com.android.settings` | 3.66 | **TV Settings Provider.** Tailored settings app for display resolution, sound equalization, network setup, and channel parameters. |
| `priv-app/RTK_engineer_menu.apk` | `com.rtk.engmenu` | 0.40 | **Factory & Panel Calibration Menu.** Used for servicing panel white balance, LVDS mapping, and hardware diagnostics (`OnReceiver`). |
| `app/MediaBrowser.apk` | `com.android.mediabrowser` | 7.67 | **USB Media Player.** Plays videos, photos, and music from USB drives utilizing hardware decoders. |
| `app/RTKMiracast.apk` | `com.realtek.miracast` | 1.32 | **Miracast Screen Mirroring.** Wi-Fi Display sink daemon for wireless screen projection. |

---

## 7. Android Core AOSP Runtime Framework `[CONFIRMED]`

The following standard Android 4.4.2 packages form the operating system backbone and cannot be removed without causing system crashes or boot loops:
* `framework/framework-res.apk` (7.25 MB): Core Android framework resources.
* `priv-app/SystemUI.apk` (1.09 MB): Status bar, navigation bar, and volume dialogs.
* `priv-app/Keyguard.apk` (1.15 MB): Screen lock and power management handlers.
* `priv-app/SettingsProvider.apk` (0.11 MB): Database for system settings and global properties.
* `priv-app/PackageInstaller.apk` (0.09 MB): Package manager installation UI.
* `priv-app/DefaultContainerService.apk` (0.01 MB): Package extraction and asset inspection service.
* `priv-app/InputDevices.apk` (0.05 MB): Key character map provider for USB keyboards and remotes.
