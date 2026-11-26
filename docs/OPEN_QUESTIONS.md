# PhilcoOS — Open Questions & Technical Unknowns Report

**Device:** Philco PH42F10DSGW (Smart TV 42" LED)  
**Mainboard:** CVTE TP.RT2984T.PB801  
**Audit Date:** 2026-11-20  
**Status:** Explicit Inventory of Unverified Assumptions & Unknowns  

---

## 1. Physical Die Marking vs. Schematic Discrepancy

* **Status:** `[UNKNOWN]` (Physically unconfirmed) / `[CONFIRMED]` (Software-derived).
* **Investigation:**
  * The factory electrical schematic lists the SoC as **RTD2968-GR**.
  * The software build properties list **RTD2984TD-GR** (`ro.cvte.chip.version`) on mainboard **TP.RT2984T.PB801** (`ro.cvte.boardstr`).
  * `/proc/cpuinfo` confirms **4 execution cores** (`ARMv7 rev 5 Cortex-A7`), consistent with RTD2984 (RTD2968 is historically dual-core).
* **Open Question:** Is the physical chip soldered to the mainboard marked RTD2984TD-GR or RTD2968-GR? In CVTE OEM manufacturing, pin-compatible SoCs share identical board designs. Without physical inspection of the silicon package beneath the heatsink, the physical laser marking remains unverified, although the kernel operates in quad-core mode.

---

## 2. Live Runtime Process Memory Footprint (PSS)

* **Status:** `[UNKNOWN]`.
* **Investigation:**
  * `tv_info.txt` provides `/proc/meminfo` (456,492 KB MemTotal, 47,532 KB MemFree, 171,228 KB Active anon) and `dumpsys SurfaceFlinger`.
  * `tv_info.txt` does **not** include `dumpsys meminfo`, `procrank`, `librank`, or `top`.
* **Open Question:** What are the exact Proportional Set Size (PSS) values of individual system applications (e.g., `LancherProject2995.apk`, `OTAupdate.apk`, `com.cvte.keyservice.apk`) during typical TV operation? While storage savings from deleting APKs are 100% confirmed, exact runtime RAM savings cannot be quantitatively proven without a live memory dump.

---

## 3. Early Boot Hardware Initialization Log (Lost dmesg)

* **Status:** `[UNKNOWN]` (Silicon tuner part number) / `[CONFIRMED]` (Wi-Fi and Display drivers).
* **Investigation:**
  * The dmesg ring buffer captured in `tv_info.txt` spans only timestamps `7582.92s` through `7600.74s` (~18 seconds of uptime).
  * The circular buffer was completely overwritten by verbose beacon frames and RSSI polling logs from the MediaTek MT7601U wireless driver (`mt7601Usta.ko`).
* **Open Question:** What is the exact silicon tuner chip model (e.g. MaxLinear, Rafael Micro, or Silicon Labs) and demodulator I2C address? Although the binary drivers and frequency tables are fully preserved inside `TvServer` and `/system/rtk_rootfs/`, the early kernel probe log was lost due to buffer wrap-around.

---

## 4. Framework Stability if `TeleService.apk` is Purged

* **Status:** `[INFERRED]`.
* **Investigation:**
  * `build.prop` contains `ro.radio.noril=1`, disabling the cellular radio interface layer.
  * In standard Android KitKat (API 19), certain vendor builds of `services.jar` / `SystemServer` instantiate `TelephonyRegistry` unconditionally during boot.
* **Open Question:** Will `SystemServer` boot cleanly without `com.android.phone` (`TeleService.apk`) and `TelephonyProvider.apk`, or will it throw an uncaught exception causing a boot loop? Until tested on live hardware or validated against decompiled `services.jar`, removing `TeleService.apk` carries an unnecessary risk compared to its minimal 791 KB storage footprint.

---

## 5. Compatibility of Other Firmware Revisions (e.g., 2018 Release)

* **Status:** `[UNKNOWN]`.
* **Investigation:**
  * The current investigation is strictly grounded in `PH42F10DSGWAVA8GB.img` (build date: August 16, 2017).
  * A potential 2018 firmware release has not been analyzed or inventoried in this workspace.
* **Open Question:** Does a 2018 firmware release use identical partition layouts, kernel versions (3.7.2-298x), and panel timing binaries (`factory.tar`), or did CVTE revise the mainboard revision (e.g., changing from MediaTek MT7601U to Realtek 8188FTV)? **Compatibility cannot be assumed.** Any other firmware must undergo a separate, full technical audit before incorporation.

---

## 6. Panel Timing Header Decompilation

* **Status:** `[INFERRED]`.
* **Investigation:**
  * Panel binary configuration files (`bin_panel/panel.bin`, `bin_panel/PanelParam/vip_default.bin`) exist in `package4/factory.tar`.
  * The factory unpack script (`ifcmd0` in `config.txt`) copies them directly to `/tmp/factory/`.
* **Open Question:** While the binary blobs are fully preserved and will be flashed as-is, the exact human-readable LVDS clock frequencies, horizontal/vertical front porch, and sync timings are not documented in plain text. Retaining `package4/factory.tar` untouched is mandatory to avoid display signal mismatch.
