# PhilcoOS — Original Firmware Analysis Report

**Firmware Archive:** `PH42F10DSGWAVA8GB.img`  
**File Location:** `C:\PhilcoOS\original\PH42F10DSGWAVA8GB.img`  
**Date of Analysis:** 2026-11-20  
**Status:** Firmware Archive Fully Decoded & Verified  

---

## 1. File Verification & Cryptographic Integrity

* **Filename:** `PH42F10DSGWAVA8GB.img`
* **File Size:** `600,401,920` bytes (572.58 MB / 586,330 KB)
* **SHA-256 Checksum:**  
  `9BCEF48B439998947C2AC64E2C127D7A83D7FBF2FC9F2EB0579ED4C8D7EAFFC0`
* **File Signature / Format:** Standard POSIX TAR (`ustar\000` header layout)
* **Accessibility:** 100% Readable and extractable using standard archival utilities (`tar`, Python `tarfile`, 7-Zip). No proprietary encryption, wrapping, or obfuscation detected on the outer container.

---

## 2. Archive Container Structure (Outer Layer)

The `.img` file is not a raw disk block dump, but an installation bundle formatted as a POSIX TAR archive containing 18 distinct members:

| Member Name | Size (Bytes) | Type | Role & Description |
| :--- | :--- | :--- | :--- |
| `0.vmlinux.rescue.bin` | 7,229,664 | File | Bootcode emergency/rescue Linux kernel loaded into RAM for USB recovery. |
| `config.txt` | 1,457 | File | Master installer configuration script defining partition targets, sizes, microcode images, and install hooks. |
| `customer` | 446,600 | File | ELF executable / customer payload hook executed during installation. |
| `font.ttf` | 143,300 | File | TrueType font embedded for rendering UI installation status and progress bars on-screen. |
| `install_a` | 287,992 | File | Realtek RTD298x installer binary (`src/rtk_burn_emmc.cpp`). Formats eMMC, flashes partitions, burns bootcode/firmware. |
| `mm` | 639,125 | File | Binary splash / visual boot asset displayed during update execution. |
| `otares.tar.bz2` | 611,044 | Archive | Compressed resource archive unpacked into `/res` (progress bar overlays, icons, error screens). |
| `package4/` | 0 | Directory | Primary OS payload container. |
| `package4/bootfile.image` | 94,204 | File | Video subsystem bootstrap microcode. |
| `package4/factory.tar` | 645,120 | Archive | Factory calibration files, panel parameters (`vip_default.bin`, `panel.bin`), ATV/DTV initial channel tables. |
| `package4/bootfile.audio` | 40,750 | File | Audio subsystem bootstrap microcode. |
| `package4/vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin` | 14,192,628 | File | Operational Android Linux 3.7.2-298x kernel burned directly to the eMMC kernel partition. |
| `package4/bluecore.audio` | 1,916,960 | File | Realtek Audio DSP microcode coprocessor firmware (handles AC3, DTS, AAC, Equalizer, DAC routing). |
| `package4/video_firmware.bin` | 3,545,576 | File | Realtek Video Processing Unit (VPU) microcode coprocessor firmware (H.264, MPEG-2/4, hardware scaling). |
| `package4/data.tar.bz2` | 189,780 | Archive | Pre-seeded `/data` partition contents (contains initial native test binaries and directory skeletons). |
| `package4/bootloader.tar` | 4,741,120 | Archive | Bootcode binaries (`bootcode.bin`, `aux.bin`, `hwsetting.bin`, `params.bin`, `vmlinux.rescue.rtd298x.bin.lzma`). |
| `package4/system.emmc.tar` | 565,647,360 | Archive | Android `/system` partition filesystem packaged as an uncompressed TAR archive (2,117 files). |
| `postprocess.sh` | 9,093 | File | Shell script executed by `install_a` at the end of installation to enforce file permissions and ownerships. |

---

## 3. Analysis of `config.txt` and Installer Logic

The `config.txt` file controls the Realtek flashing sequence executed by `install_a`:

```ini
start_customer=y
kernel_version="9952"
audio_version="0"
video_version="0"
bootcode=y
factory=y
customer_delay=0
kill_000=y
update_etc=y
install_avfile_count=1
install_avfile_audio_size=1536
install_avfile_video_size=2560
reboot_delay=5
ifcmd0 = "cd ../../;rm -rf /tmp/factory/bin_panel/PanelParam/*.bin; rm -rf /tmp/factory/bin_panel/panel/*; cp -rpf tmp/install_factory/* tmp/factory/;chmod 755 tmp/factory;"
ifcmd1 = "rm -rf /tmp/factory/recovery;"
secure_boot=0
fw = linuxKernel package4/vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin 0xdeaddead
fw = audioKernel package4/bluecore.audio 0xdeaddead
fw = videoKernel package4/video_firmware.bin 0xdeaddead
fw = audioFile package4/bootfile.audio 0xdeaddead
fw = videoFile package4/bootfile.image 0xdeaddead

# Partition Configuration for 8GB eMMC (Target Selected)
part_targetM=8192
part = system /system ext4 package4/system.emmc.tar 2147483648
part = data /data ext4 package4/data.tar.bz2 4294967296
part = cache /cache ext4 null 536870912
part = res /res ext4 otares.tar.bz2 16777216
```

### Key Installer Mechanics:
1. **Flashing Method:** The installer does **not** flash raw filesystem images using `dd`. Instead, it creates an ext4 filesystem on the target eMMC partition and unpacks the TAR archive (`system.emmc.tar`) directly into the mounted filesystem!
2. **Dual Target Profiles:** `config.txt` contains definitions for both 2048 MB eMMC (legacy/low-end) and 8192 MB eMMC. For this Philco TV (`part_targetM=8192`), `/system` is allocated 2,147,483,648 bytes (2 GB), `/data` gets 4,294,967,296 bytes (4 GB), `/cache` gets 512 MB, and `/res` gets 16 MB.
3. **Microcode Offsets:** Kernel, Audio DSP, Video VPU, and Bootfiles are burned to dedicated raw eMMC sectors indexed by `boottable`.
4. **Hook Execution:** `ifcmd0` copies panel parameters into `/tmp/factory` and sets permissions. After archive unpacking, `postprocess.sh` is executed to set Android permissions (`chown`, `chmod`).

---

## 4. Inspection of Android System Image (`package4/system.emmc.tar`)

* **Format:** Uncompressed POSIX TAR archive
* **Size:** 565,647,360 bytes (~539.44 MB)
* **Total Entries:** 2,117 files, symlinks, and directories
* **Extractability:** Can be completely unpacked, modified, and repacked using standard `tar -cf` without requiring specialized sparse-image tools (`simg2img`/`make_ext4fs`).

### Top-Level Hierarchy:
```
/system/
├── app/            # 28 Pre-installed user applications (65.94 MB)
├── priv-app/       # 25 Privileged system applications (41.15 MB)
├── bin/            # 180 Android native binaries and toolbox commands
├── xbin/           # Extended native binaries (busybox, procmem, procrank)
├── lib/            # 298 Shared dynamic libraries (.so)
├── vendor/         # Vendor graphics driver & HAL components (18 entries)
├── rtk_rootfs/     # Realtek TV proprietary backend subsystem & modules
├── etc/            # System configuration, permissions, audio/media XMLs
├── fonts/          # TrueType system fonts
├── framework/      # Android framework JARs and framework-res.apk
├── media/          # Audio ringtones, notifications, and UI sounds
├── usr/            # Keylayouts, keychars, and ICU data
└── build.prop      # Android system properties and build configuration
```

---

## 5. Vendor Components & Subsystems

1. **Vendor Directory (`/system/vendor`):**
   * **GPU Drivers:** Complete Imagination Technologies PowerVR SGX544MP userspace driver suite (`libEGL_POWERVR_SGX544_115.so`, `libGLESv1_CM_POWERVR_SGX544_115.so`, `libGLESv2_POWERVR_SGX544_115.so`, `libIMGegl.so`, `libpvr2d.so`, `libpvrANDROID_WSEGL.so`, `libsrv_init.so`, `libsrv_um.so`, `libusc.so`).
   * **Vendor Hardware HALs:** `vendor/lib/hw/gralloc.rtd298x.so`, `vendor/lib/hw/hwcomposer.rtd298x.so`, `vendor/lib/hw/keystore.rtd298x.so`, `vendor/lib/hw/memtrack.rtd298x.so`.
2. **Realtek Subsystem (`/system/rtk_rootfs`):**
   * Acts as a standalone Linux chroot/daemon environment housing the core TV server (`TvServer`, 33.3 MB), Root Supervisor (`RootApp`), DTV/ATV frequency tables, Ginga JavaTV middleware (`javatv.jar`), and out-of-tree kernel modules (`.ko`).
