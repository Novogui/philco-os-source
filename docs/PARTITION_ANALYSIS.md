# PhilcoOS — Partition Analysis Report

**Device:** Philco PH42F10DSGW (Smart TV)  
**Storage Medium:** Embedded MultiMediaCard (eMMC 8GB)  
**Platform:** Realtek RTD2984TD-GR (`rtd298x`)  
**Date of Analysis:** 2026-11-20  

---

## 1. Storage Topology & eMMC Hardware Partitioning

The storage subsystem is an 8 GB eMMC managed by the Realtek eMMC driver (`rtkemmc`). The eMMC exposes standard JEDEC hardware partitions along with user data partitions.

```
+----------------------------------------------------------------------------------------------------+
|                                    eMMC Physical Storage (~7.82 GB)                                |
+-----------------------+-----------------------+----------------------------------------------------+
| mmcblk0boot0 (4 MB)   | mmcblk0boot1 (4 MB)   | mmcblk0 (User Area: 7,634,944 KB / ~7.45 GiB)      |
| Primary Bootloader    | Secondary / Backup    +----------------------------------------------------+
| (bootcode.bin, aux)   | Bootloader Block      | Raw Microcode Sectors (Kernel, Audio, Video DSP)   |
|                       |                       +----------------------------------------------------+
|                       |                       | mmcblk0p1: /system (2,097,152 KB / 2 GB, ext4)    |
|                       |                       +----------------------------------------------------+
|                       |                       | mmcblk0p2: /data   (4,194,304 KB / 4 GB, ext4)    |
|                       |                       +----------------------------------------------------+
|                       |                       | mmcblk0p3: /cache  (  524,288 KB / 512 MB, ext4)  |
|                       |                       +----------------------------------------------------+
|                       |                       | mmcblk0p4: /res    (   16,384 KB / 16 MB, ext4)   |
+-----------------------+-----------------------+----------------------------------------------------+
```

---

## 2. Kernel Partition Table Verification

### 2.1 `/proc/partitions` Verification
```text
major minor  #blocks  name
 179        0    7634944 mmcblk0
 179        1    2097144 mmcblk0p1
 179        2    4194296 mmcblk0p2
 179        3     524280 mmcblk0p3
 179        4      16376 mmcblk0p4
 179       32       4096 mmcblk0boot1
 179       16       4096 mmcblk0boot0
```

### 2.2 Kernel Command Line Partition Arguments (`mmcparts`)
From `/proc/cmdline`:
```text
mmcparts=rtkemmc:3440640k,2097152k(/system),0k(/data),524288k(/cache),16384k(/res)
```
* **Raw Reserve (`3440640k`):** Reserved at the beginning of the eMMC disk for bootloader, factory parameters, recovery kernel, audio/video firmware microcode, and the main Linux kernel.
* **`/system` (`2097152k`):** Fixed 2,048 MB ext4 partition.
* **`/data` (`0k`):** Takes all remaining eMMC capacity up to total disk space (~4,096 MB).
* **`/cache` (`524288k`):** Fixed 512 MB ext4 partition.
* **`/res` (`16384k`):** Fixed 16 MB ext4 partition.

---

## 3. Installer (`config.txt`) Configuration

The installer specifies two potential storage targets. On this 8 GB TV model, `part_targetM=8192` is automatically selected:

```ini
# Partition Configuration for 8GB eMMC
part_targetM=8192
part = system /system ext4 package4/system.emmc.tar 2147483648
part = data /data ext4 package4/data.tar.bz2 4294967296
part = cache /cache ext4 null 536870912
part = res /res ext4 otares.tar.bz2 16777216
```

---

## 4. Raw Microcode & Firmware Blocks

These components are burned directly to pre-allocated raw flash sectors rather than residing inside filesystem partitions:

| Component | Source Image in Firmware | Size | Flash Target / Handling |
| :--- | :--- | :--- | :--- |
| **Linux Kernel** | `package4/vmlinux.develop.android.kk.rtd298x.tv001.emmc.bin` | 14,192,628 B | Burned to raw Linux kernel offset (`fw = linuxKernel`) |
| **Audio DSP Microcode** | `package4/bluecore.audio` | 1,916,960 B | Burned to Audio DSP offset (`fw = audioKernel`) |
| **Video VPU Microcode** | `package4/video_firmware.bin` | 3,545,576 B | Burned to Video VPU offset (`fw = videoKernel`) |
| **Audio Bootstrap** | `package4/bootfile.audio` | 40,750 B | Burned to Audio bootfile offset (`fw = audioFile`) |
| **Video Bootstrap** | `package4/bootfile.image` | 94,204 B | Burned to Video bootfile offset (`fw = videoFile`) |
| **Factory / Panel Data** | `package4/factory.tar` | 645,120 B | Unpacked to `/tmp/factory` during install; panel bins preserved |
| **Bootloader Block** | `package4/bootloader.tar` | 4,741,120 B | Burned to eMMC boot partitions (`bootcode=y`) |

---

## 5. Filesystem Partition Analysis & Space Utilization

### 5.1 System Partition (`/system`)
* **Device:** `/dev/block/mmcblk0p1`
* **Filesystem:** ext4 (mounted `rw,seclabel,relatime,data=ordered`)
* **Allocated Size:** 2,147,483,648 bytes (2,048 MB / 2.0 GB)
* **Payload Archive (`package4/system.emmc.tar`):** 565,647,360 bytes (~539.44 MB)
* **Current Utilization:** ~26.3%
* **Free / Headroom:** ~1,470 MB (Over 1.4 GB of free unallocated space inside `/system`!)
* **Opportunity for PhilcoOS:** The stock `/system` partition is largely empty. By trimming bloatware, we can further reduce installation size, decrease flash wear, and maintain plenty of space for essential TV services.

### 5.2 Data Partition (`/data`)
* **Device:** `/dev/block/mmcblk0p2`
* **Filesystem:** ext4 (mounted `rw,seclabel,nosuid,nodev,noatime,nomblk_io_submit,errors=panic,data=ordered`)
* **Allocated Size:** 4,294,967,296 bytes (4,096 MB / 4.0 GB)
* **Initial Archive (`package4/data.tar.bz2`):** 189,780 bytes (contains only native benchmark tests)
* **Role:** Stores user preferences, scanned DTV/ATV channels, Wi-Fi keys, and user-installed apps.

### 5.3 Cache Partition (`/cache`)
* **Device:** `/dev/block/mmcblk0p3`
* **Filesystem:** ext4 (mounted `rw,seclabel,nosuid,nodev,noatime,nomblk_io_submit,errors=panic,data=ordered`)
* **Allocated Size:** 536,870,912 bytes (512 MB)
* **Role:** Formatted clean during flashing (`null` source); used by `init.tvserver.sh` to store channel cache (`/cache/dvdplayer`) and temporary OTA logs.

### 5.4 Resource Partition (`/res`)
* **Device:** `/dev/block/mmcblk0p4`
* **Filesystem:** ext4
* **Allocated Size:** 16,777,216 bytes (16 MB)
* **Payload Archive (`otares.tar.bz2`):** 611,044 bytes
* **Role:** Houses recovery UI graphics and progress icons for OTA installation screens.
