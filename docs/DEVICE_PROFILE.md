# PhilcoOS — Device Profile Report

**Device:** Philco PH42F10DSGW (Smart TV 42" LED)  
**Mainboard:** CVTE TP.RT2984T.PB801  
**Target SoC:** Realtek RTD2984TD-GR (RTD298x Platform)  
**Date of Analysis:** 2026-11-20  
**Status:** Firmware Investigation Complete  

---

## 1. System Identification & Build Metadata

| Property | Value | Origin / Reference |
| :--- | :--- | :--- |
| **Brand / Model** | Philco PH42F10DSGW | TV Chassis / Firmware Label |
| **Platform / SoC** | Realtek RTD2984TD-GR (`rtd298x`) | `ro.cvte.chip.version`, `ro.board.platform` |
| **Mainboard Model** | CVTE `TP.RT2984T.PB801` | `ro.cvte.boardstr` |
| **Android Version** | 4.4.2 KitKat (API Level 19) | `ro.build.version.release` |
| **Build ID** | `KOT49H` | `ro.build.id` |
| **Display ID** | `rtd298x_tv001-eng 4.4.2 KOT49H eng.wubf.20170816.163001 test-keys` | `ro.build.display.id` |
| **Build Fingerprint** | `realtek/rtd298x_tv001/rtd298x_tv001:4.4.2/KOT49H/eng.wubf.20170816.163001:eng/test-keys` | `ro.build.fingerprint` |
| **Build Date** | Wed Aug 16 16:32:00 CST 2017 (UTC: 1502872320) | `ro.build.date` |
| **Default Locale** | `pt_US` / Timezone: `America/Sao_Paulo` | `ro.product.locale.language`, `persist.sys.timezone` |
| **Build Target** | `smart_tv001` (product: `rtd298x_tv001`) | `ro.product.model`, `ro.product.device` |

---

## 2. Processor & Architecture

* **CPU Core:** Quad-core ARM Cortex-A7 (`ARMv7 Processor rev 5 (v7l)`)
  * **Cores:** 4 execution cores (CPU 0 to 3)
  * **BogoMIPS:** 54.00 per core
  * **Features:** `swp half thumb fastmult vfp edsp thumbee neon vfpv3 tls vfpv4 idiva idivt`
  * **CPU Architecture:** ARMv7-A 32-bit (`armeabi-v7a`, `armeabi`)
  * **Hardware Identification:** `rtd298x`, Revision: `0002`
* **GPU (Graphics Processing Unit):**
  * **Model:** Imagination Technologies **PowerVR SGX544MP**
  * **API Support:** OpenGL ES 2.0 / 1.1, EGL 1.4
  * **DDK / Driver Release:** 1.1.5 (`libEGL_POWERVR_SGX544_115.so`)
  * **Compositor Target:** 1280x720 UI Plane (Hardware scaled to 1920x1080 panel output)
  * **UI LCD Density:** 160 dpi (TVDPI)

---

## 3. Memory & Storage Topology

### 3.1 Random Access Memory (RAM)
* **Physical Hardware:** 512 MB DDR3 SDRAM
* **Available to Kernel (`MemTotal`):** 456,492 kB (~446 MB)
* **Hardware Carveouts (~56 MB):**
  * Realtek Video Processing Unit (VPU) zero-copy direct video overlay buffer (`media.sf.enable_vo_zero_copy=1`)
  * Realtek Audio DSP (`bluecore.audio`) firmware memory
  * Kernel code (`0x00108000-0x0093c987`) & static data (`0x00da0000-0x00e6c977`)
  * GPU framebuffers & SurfaceFlinger graphics memory
* **Memory Management Profile:**
  * Low-RAM device mode: `ro.config.low_ram=true`
  * ZRAM / Swap: None configured in original firmware (`SwapTotal: 0 kB`)
  * Dalvik VM Heap Config:
    * `dalvik.vm.heapstartsize`: 5 MB
    * `dalvik.vm.heapgrowthlimit`: 48 MB
    * `dalvik.vm.heapsize`: 128 MB
    * `dalvik.vm.heaptargetutilization`: 0.75

### 3.2 Internal Storage (eMMC)
* **Storage Type:** Embedded MultiMediaCard (eMMC) v4.41/4.5
* **Block Device:** `/dev/block/mmcblk0`
* **Total Capacity:** 7,634,944 blocks (~7.82 GB = 8 GB Class)
* **Hardware Boot Partitions:**
  * `mmcblk0boot0`: 4,096 KB (4 MB)
  * `mmcblk0boot1`: 4,096 KB (4 MB)
* **Logical Mounts:**
  * Partition 1 (`mmcblk0p1`): `/system` — 2,097,144 blocks (2,048 MB / 2 GB), ext4
  * Partition 2 (`mmcblk0p2`): `/data` — 4,194,296 blocks (4,096 MB / 4 GB), ext4
  * Partition 3 (`mmcblk0p3`): `/cache` — 524,280 blocks (512 MB), ext4
  * Partition 4 (`mmcblk0p4`): `/res` — 16,376 blocks (16 MB), ext4

---

## 4. Kernel & Boot Parameters

* **Kernel Release:** `Linux 3.7.2-298x`
* **Compiler:** `gcc version 4.8.3 (Realtek ASDK-4.8.3p1 Build 2003)`
* **Kernel Build:** `#192 SMP PREEMPT Wed May 31 10:03:26 CST 2017`
* **Boot Arguments (`/proc/cmdline`):**
  ```text
  androidboot.console=ttyS0 console=ttyS0,115200 coherent_pool=768K envp=100700 flashtype=emmc nr_cpus=4 maxcpus=4 mmcparts=rtkemmc:3440640k,2097152k(/system),0k(/data),524288k(/cache),16384k(/res) reclaim=72M@32M last_image=8M@160M earlyprintk
  ```

---

## 5. Peripheral & Connectivity Hardware

* **Wi-Fi Subsystem:** MediaTek **MT7601U** 802.11b/g/n (150 Mbps) USB Dongle / On-board module
  * Active module: `mt7601Usta.ko`
  * Interface: `wlan0`
* **Ethernet:** Realtek RTD298x integrated Fast Ethernet MAC (`fe010000`, `eth0`)
* **USB Host Controllers:**
  * EHCI 0/1 (`fe013000`, `fe012000`)
  * OHCI 0/1 (`fe013400`, `fe012400`)
* **Remote Control (IR):** CVTE IR Receiver routed via hardware GPIO/IR controller, configured via `/bin/setir -p 1` and bound to `com.cvte.keyservice.apk`.
* **Hardware Real-Time Clock (RTC):** `rtc-rtk_dmp_venus.0` mapped at `b6061900-b6061933`.
