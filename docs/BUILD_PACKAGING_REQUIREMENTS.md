# PhilcoOS — Build & Packaging Requirements Report

**Device:** Philco PH42F10DSGW (Smart TV 42" LED)  
**Mainboard:** CVTE TP.RT2984T.PB801  
**Target Platform:** Realtek RTD2984TD-GR (`rtd298x`)  
**Audit Date:** 2026-11-20  
**Status:** 100% Confirmed Against `install_a`, `config.txt`, and `postprocess.sh`  

---

## 1. System Partition Image Specifications (`package4/system.emmc.tar`)

The Android `/system` partition is packaged as a standard POSIX TAR archive rather than a sparse ext4 disk image.

```
+----------------------------------------------------------------------------------------------------+
|                                package4/system.emmc.tar Specifications                             |
+-------------------+--------------------------------------------------------------------------------+
| Archive Format    | POSIX TAR (Header format: ustar\000)                                           |
| Compression       | None (Uncompressed TAR stream)                                                 |
| File Entry Count  | 2,117 entries in stock release                                                  |
| Target Partition  | /dev/block/mmcblk0p1 (ext4, 2,147,483,648 bytes / 2.0 GB allocated)            |
| Extraction Method | tar xOf <image> package4/system.emmc.tar | tar xf - -C /system                 |
+-------------------+--------------------------------------------------------------------------------+
```

### 1.1 Metadata & Attribute Requirements `[CONFIRMED]`

1. **Ownership (UID / GID):**
   * In the factory archive, all files are stored with builder UID/GID `1006:1006` (`wubf:wubf`).
   * When extracted on the TV by `install_a`, the files initially inherit ownership based on the extracting user (root in rescue environment).
   * **Crucial Mechanism:** Final ownership is **not** dictated by the tar header; it is enforced post-extraction by `postprocess.sh` via explicit `chown` commands (e.g., `chown 0:2000 /system/bin/*`, `chown 0:0 /system/etc/*`, `chown 2000:2000 /system/rtk_rootfs/*`).
   * *Packaging Requirement:* Standard `0:0` (root) or original `1006:1006` are both fully acceptable.

2. **File Permissions (Mode):**
   * Standard POSIX octal modes must be set:
     * Regular files (configs, assets, frameworks): `0644` (`-rw-r--r--`).
     * Executable binaries, scripts, and shared libraries: `0755` (`-rwxr-xr-x`).
     * Special setuid binaries (e.g., `ping`, `netcfg`): handled by `postprocess.sh` (`chmod g+s`).
   * `postprocess.sh` re-applies `chmod 755` to `/system/bin`, `/system/xbin`, `/system/vendor/bin`, and `/system/rtk_rootfs/usr/local/bin`.

3. **Symbolic Links (Symlinks):**
   * Symlinks must be stored as true TAR symlink entries (`typeflag = b'2'`).
   * Example: `bin/sh` -> `mksh` (link target: `mksh`).
   * *Packaging Requirement:* Windows file systems do not always handle POSIX symlinks correctly. Packaging must be performed via a tool or script that preserves TAR symlink headers.

4. **Extended Attributes (xattrs) & SELinux Contexts:**
   * **Are PAX extended attributes required in the TAR?** **NO.** `[CONFIRMED]`.
   * Inspection of stock `system.emmc.tar` confirms `Pax headers: {}` (zero extended attributes).
   * **How SELinux labeling is achieved:** `postprocess.sh` executes after extraction:
     ```sh
     mount -o remount,acl,user_xattr /system
     find /system/* -type f -o -type d | xargs setfilecon u:object_r:system_file:s0
     setfilecon u:object_r:shell_exec:s0 /system/bin/ash
     setfilecon u:object_r:shell_exec:s0 /system/bin/mksh
     setfilecon u:object_r:shell_exec:s0 /system/bin/sh
     setfilecon u:object_r:runas_exec:s0 /system/bin/run-as
     setfilecon u:object_r:zygote_exec:s0 /system/bin/app_process
     setfilecon u:object_r:surfaceflinger_exec:s0 /system/bin/surfaceflinger
     ...
     ```
   * SELinux labels are dynamically stamped onto the ext4 filesystem by `setfilecon` during installation.

---

## 2. Master Firmware Image Specifications (`PH42F10DSGWAVA8GB.img`)

The master firmware file is a POSIX TAR archive containing 18 distinct components.

### 2.1 Exact Member Order & Layout `[CONFIRMED]`

To match factory behavior and ensure sequential streaming by `install_a`, the TAR entries should follow the stock hierarchy:

```
PH42F10DSGWAVA8GB.img
├── 0.vmlinux.rescue.bin                                    (7,229,664 bytes)
├── config.txt                                              (    1,457 bytes)
├── customer                                                (  446,600 bytes)
├── font.ttf                                                (  143,300 bytes)
├── install_a                                               (  287,992 bytes)
├── mm                                                      (  639,125 bytes)
├── otares.tar.bz2                                          (  611,044 bytes)
├── package4/
│   ├── bootfile.image                                      (   94,204 bytes)
│   ├── factory.tar                                         (  645,120 bytes)
│   ├── bootfile.audio                                      (   40,750 bytes)
│   ├── vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin   (14,192,628 bytes)
│   ├── bluecore.audio                                      ( 1,916,960 bytes)
│   ├── video_firmware.bin                                  ( 3,545,576 bytes)
│   ├── data.tar.bz2                                        (  189,780 bytes)
│   ├── bootloader.tar                                      ( 4,741,120 bytes)
│   └── system.emmc.tar                                     (MODIFIED PAYLOAD)
└── postprocess.sh                                          (    9,093 bytes)
```

---

## 3. Installer Behavior & Validation Audit (`install_a`)

### 3.1 Verification & Signature Checks `[CONFIRMED]`

1. **Digital Signatures:**
   * `config.txt` explicitly specifies:
     ```ini
     signature=""
     secure_boot=0
     ```
   * `[CONFIRMED]`: The firmware is **unsigned**. `install_a` does not enforce RSA/cryptographic public key validation when `secure_boot=0`.
2. **Checksum Verification:**
   * Inspection of `install_a` strings shows optional routines for `checksum.exe` or `layout.txt_md5`.
   * Neither `checksum` nor `checksum.exe` exists inside the original firmware image.
   * `install_a` proceeds directly with partition formatting and tar stream extraction without failing on checksum checks.

### 3.2 Partition Formatting & Flashing Workflow `[CONFIRMED]`

When triggered in USB recovery mode:
1. `install_a` parses `config.txt`.
2. Targets `part_targetM=8192` (8GB eMMC layout).
3. Burns raw microcodes:
   * Kernel -> Linux Kernel partition (`fw = linuxKernel`)
   * Audio DSP -> Audio DSP partition (`fw = audioKernel`)
   * Video VPU -> Video VPU partition (`fw = videoKernel`)
   * Bootfiles -> Bootfile partitions (`fw = audioFile`, `fw = videoFile`)
4. Formats eMMC partition 1 (`mmcblk0p1`) as ext4.
5. Mounts `/system` and executes:
   ```sh
   tar xOf <firmware_path> package4/system.emmc.tar | tar xf - -C /system
   ```
6. Runs `ifcmd0` (copies panel files from `tmp/install_factory` to `tmp/factory`).
7. Executes `postprocess.sh` to enforce ownerships, permissions, capabilities, and SELinux contexts.
8. Reboots cleanly.
