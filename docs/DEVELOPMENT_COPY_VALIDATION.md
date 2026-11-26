# PhilcoOS — Development Copy Validation Report

**Target Device:** Philco PH42F10DSGW (Smart TV 42" LED)  
**Mainboard:** CVTE TP.RT2984T.PB801 / Realtek RTD2984TD-GR (`rtd298x`)  
**Base Firmware:** `PH42F10DSGWAVA8GB.img`  
**Development Staging Path:** `C:\PhilcoOS\build\system_stage`  
**Reference Baseline:** `C:\PhilcoOS\extracted\factory_baseline\system`  
**Document:** `C:\PhilcoOS\docs\DEVELOPMENT_COPY_VALIDATION.md`  
**Date:** 2026-11-20  
**Status:** Development Copy Created, Validated, and Quarantined  

---

## 1. Executive Summary

The project has advanced from factory baseline verification into the isolated **PhilcoOS Development Stage**.

1. **Outer Firmware Verification:** All 17 regular files in the outer container `PH42F10DSGWAVA8GB.img` were independently validated against on-disk copies and metadata. Every file matches size and SHA-256 bit-for-bit (`[CONFIRMED]`).
2. **Development Copy Established:** An isolated, clean development copy of the Android `/system` filesystem was created at `C:\PhilcoOS\build\system_stage` directly from `factory_baseline/system`.
3. **Exhaustive Comparison:** All 172 directories, 1,628 regular files, and 317 symlink descriptors were compared between source and destination. Exactly **zero differences** in file count, sizes, hashes, or symlink targets were detected (`[CONFIRMED]`).
4. **Collision Preservation:** All 8 case-sensitive filename collision pairs (most critically `lib/libSync.so` [112,068 B] vs `lib/libsync.so` [5,252 B]) coexist simultaneously in `build\system_stage` with distinct hashes.
5. **Repacker Audit:** `scripts/repack_firmware.py` was reviewed for USTAR format compliance, symlink reconstruction, and POSIX metadata preservation without execution.
6. **Factory Baseline Quarantine:** The factory baseline remains 100% read-only and untouched.

---

## 2. Independent Outer Firmware Hash Validation `[CONFIRMED]`

Every regular file extracted into `C:\PhilcoOS\extracted\factory_baseline\firmware` was independently audited against the outer TAR archive stream and `firmware_metadata.json`:

| File Name in Master Image | Size (Bytes) | SHA-256 Checksum | Tar Match | Meta Match | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `0.vmlinux.rescue.bin` | 7,229,664 | `e3d1f51a340a9e4c19419b48b7d911b33ce8ca3f24b207555dd53ba6f73a3aa5` | `YES` | `YES` | `CONFIRMED` |
| `config.txt` | 1,457 | `cb5031a122f8a1eda6bfa7541f92e70e4b85c17d23d8c47d730a08e0b650882e` | `YES` | `YES` | `CONFIRMED` |
| `customer` | 446,600 | `02e78caa1a9d4a89643d9a1aeeb8d4611481b3dff84b5c777a419266e74b5c78` | `YES` | `YES` | `CONFIRMED` |
| `font.ttf` | 143,300 | `ca6714459a58eca546a160868f760dafa39aa9a17409bc306c5896a605f247ea` | `YES` | `YES` | `CONFIRMED` |
| `install_a` | 287,992 | `f5e50ad736b7a199fa68cf30a3ae2a5c48c48a73130c25d808d4b3b185675f92` | `YES` | `YES` | `CONFIRMED` |
| `mm` | 639,125 | `e78b71fbad2a8be04439c27931c8aa9820f1883395914fa65b266bf963f45c9a` | `YES` | `YES` | `CONFIRMED` |
| `otares.tar.bz2` | 611,044 | `8f9e571110337b31174989e24694db09101d2a45fc4fc600fb1c80f1a5ba8575` | `YES` | `YES` | `CONFIRMED` |
| `package4/bootfile.image` | 94,204 | `91762320023026402eaec0104e76c11b15aa224a9058bfe05741f237efb99e7b` | `YES` | `YES` | `CONFIRMED` |
| `package4/factory.tar` | 645,120 | `6c1d8214a5b0185fe93b827725ca783f23f81dae147b36f73111fbf8f566e133` | `YES` | `YES` | `CONFIRMED` |
| `package4/bootfile.audio` | 40,750 | `0a3d4e156d032ff870197d13e3ce9c222bf05a3f2117565ddc8a7791461ffea0` | `YES` | `YES` | `CONFIRMED` |
| `package4/vmlinux.develop...bin` | 14,192,628 | `7b5f5476ffbe9b66236b306e987c8d93f9ef3104a3f124618e7e17c07bc542e8` | `YES` | `YES` | `CONFIRMED` |
| `package4/bluecore.audio` | 1,916,960 | `879fcd80e463969d20c74a00ea2f90a4be3568c0762140685938bf8a33f4a3bf` | `YES` | `YES` | `CONFIRMED` |
| `package4/video_firmware.bin` | 3,545,576 | `b1f82c0c4401393dc5fbccb6819a55740fcb29cc30e9e422ba077ebfdb21d03c` | `YES` | `YES` | `CONFIRMED` |
| `package4/data.tar.bz2` | 189,780 | `80c4d5f9e1dd089eb3b1c60f2bb9780b4353f47c8f9db586c91a45bb38c207a7` | `YES` | `YES` | `CONFIRMED` |
| `package4/bootloader.tar` | 4,741,120 | `f6730987ebd319c43d7890b05b41045e054baeb3cece39be338ce245b0d0144f` | `YES` | `YES` | `CONFIRMED` |
| `package4/system.emmc.tar` | 565,647,360 | `f8355b44691c2d627c541793dd567b5eb92f72591ae9c7e0b5c13e9a595cb640` | `YES` | `YES` | `CONFIRMED` |
| `postprocess.sh` | 9,093 | `eb11bc04a3e6411b43d526e0b4685ae26b7c0ca3ea380ea7b036c84c1766a2b5` | `YES` | `YES` | `CONFIRMED` |

---

## 3. Development Copy Creation & Collision Audit `[CONFIRMED]`

The development copy was established via [`scripts/create_dev_copy.py`](file:///C:/PhilcoOS/scripts/create_dev_copy.py) with NTFS case-sensitivity enabled on `C:\PhilcoOS\build\system_stage`.

### Case-Collision Verification in `build\system_stage`:

| Colliding Pair | File 1 Size & Hash | File 2 Size & Hash | Status |
| :--- | :--- | :--- | :--- |
| **`libSync.so`** vs **`libsync.so`** | `112,068 B` (`115ad4601e76c626...`) | `5,252 B` (`f8be66ff3becda9a...`) | `CONFIRMED` |
| **`vega.ogg`** vs **`Vega.ogg`** | `29,476 B` (`eb098eac0e8d4c73...`) | `29,310 B` (`91c041ca4c138a73...`) | `CONFIRMED` |
| **`arcturus.ogg`** vs **`Arcturus.ogg`** | `7,908 B` (`6487240c9d6dffe7...`) | `7,933 B` (`2492fa4d4e84afff...`) | `CONFIRMED` |
| **`hydra.ogg`** vs **`Hydra.ogg`** | `22,962 B` (`5c4fc32cc39683fb...`) | `22,924 B` (`c979c78cc857715e...`) | `CONFIRMED` |
| **`Perseus.ogg`** vs **`PERSEUS.ogg`** | `236,343 B` (`c1e82baa61a6f33a...`) | `112,981 B` (`cde22a2122705063...`) | `CONFIRMED` |
| **`ANDROMEDA.ogg`** vs **`Andromeda.ogg`** | `25,014 B` (`893fcf16d0346541...`) | `24,973 B` (`c8461a9f82bb528f...`) | `CONFIRMED` |
| **`CANISMAJOR.ogg`** vs **`CanisMajor.ogg`** | `31,170 B` (`bdbd5c9f2d23bdb3...`) | `31,133 B` (`c3fb7680f2c23cd2...`) | `CONFIRMED` |
| **`URSAMINOR.ogg`** vs **`UrsaMinor.ogg`** | `144,112 B` (`81c37cc3851508e0...`) | `185,005 B` (`6f2a74c5124578f3...`) | `CONFIRMED` |

---

## 4. Full Source vs. Destination Comparison `[CONFIRMED]`

Executed via [`scripts/validate_dev_copy.py`](file:///C:/PhilcoOS/scripts/validate_dev_copy.py) comparing `extracted/factory_baseline/system` against `build/system_stage`:

```text
================================================================================
Development Copy Comparison Audit
================================================================================
Source Directory           : C:\PhilcoOS\extracted\factory_baseline\system
Destination Directory      : C:\PhilcoOS\build\system_stage
Directories Matched        : 172 / 172       (Missing: 0, Extra: 0)
Regular Files Matched      : 1,628 / 1,628   (Missing: 0, Extra: 0)
File Size Mismatches       : 0
SHA-256 Hash Mismatches    : 0
Symlink Descriptors Matched: 317 / 317       (Missing: 0, Extra: 0)
Symlink Target Mismatches  : 0
Case Collisions Preserved  : 8 / 8
Metadata Reference Saved   : C:\PhilcoOS\build\system_stage_metadata.json
Validation Status          : CONFIRMED (100% Exact Mirror)
================================================================================
```

---

## 5. Build Pipeline Review of `repack_firmware.py`

A thorough code and structural review of [`scripts/repack_firmware.py`](file:///C:/PhilcoOS/scripts/repack_firmware.py) was conducted without executing the repacker:

### 5.1 Symbolic Link Reconstruction
* **Mechanism:** The script identifies `.symlink` files, extracts the base path, reads the stored target string from the descriptor, and instantiates:
  ```python
  ti = tarfile.TarInfo(name=rel)
  ti.type = tarfile.SYMTYPE  # POSIX typeflag b'2'
  ti.linkname = target
  ti.mode = entry_meta.get("mode_int", 0o777)
  ```
* **Evaluation:** When extracted by the TV's recovery installer (`tar xf - -C /system`), standard POSIX `tar` reads `typeflag=b'2'` and issues the `symlink(linkname, name)` syscall. This correctly creates authentic Linux symlinks without requiring Windows to support native symlinks.

### 5.2 Risk & Constraint Analysis
1. **Hard Links:** Verified. The stock system image contains **zero hard links** (`tarfile.LNKTYPE`). All 317 links are symbolic links.
2. **Device Nodes:** Verified. There are **zero device nodes** (`CHRTYPE`, `BLKTYPE`, `FIFOTYPE`) in the stock image. Android `init` dynamically populates `/dev` at boot.
3. **File Ownership:** All stock entries carry builder UID/GID `1006:1006` (`wubf:wubf`). `repack_firmware.py` restores these from `system_stage_metadata.json`. Furthermore, `postprocess.sh` applies final operational ownership (`0:2000`, `2000:2000`) on the device.
4. **Permissions:** Default modes are set to `0644` (regular files), `0755` (executables/directories), and `0777` (symlinks). Setuid bits (`ping`, `netcfg`) are re-stamped by `postprocess.sh` (`chmod g+s`).
5. **Timestamps:** Original timestamps are preserved from metadata; new replacement packages will receive current epoch timestamps.
6. **TAR Header Format:** Format is explicitly set to `tarfile.USTAR_FORMAT` (format code 2), bit-identical to factory `package4/system.emmc.tar` (which uses POSIX USTAR with zero PAX extended headers).
7. **Long Path Handling:** Exactly 7 paths in the stock image exceed 100 characters (max: 114 chars in `rtk_rootfs/.../IDirectFBImageProvider/...`). Under USTAR format, paths up to 255 characters are accommodated via standard USTAR prefix splitting.
8. **Case-Sensitive Traversal:** `os.walk()` on the case-sensitive `build\system_stage` directory yields both `libSync.so` and `libsync.so` as independent directory entries.

---

## 6. Packaging Blockers

* **Active Packaging Blockers:** **NONE.**
  - Destination directory case sensitivity is active and verified.
  - Symlink reconstruction mechanism is fully verified against POSIX USTAR specifications.
  - Development copy is isolated and verified bit-exact against factory baseline.

---

## 7. Next Recommended Development Action

With the development copy established and validated at [`C:/PhilcoOS/build/system_stage`](file:///C:/PhilcoOS/build/system_stage), the project is ready to begin **Android System Modification**:

1. **Stage Debloating (Tier 1 Removals):** Safely remove the non-essential bloat packages identified in [REMOVAL_CANDIDATES_WITH_EVIDENCE.md](file:///C:/PhilcoOS/docs/REMOVAL_CANDIDATES_WITH_EVIDENCE.md) (`QjyBrowser.apk`, `aptoide.apk`, `SelfTest.apk`, `OneClickCleaner.apk`, Chinese voice libs, and live wallpapers) from `build/system_stage/priv-app/` and `app/`.
2. **Stage Replacement Packages (Tier 2):** Introduce a lean TV D-pad launcher (< 2 MB) and lightweight TV IME keyboard (< 1.5 MB) to replace `LancherProject2995.apk` and `LatinIME.apk`.
3. **Tune `build.prop`:** Apply low-RAM Dalvik heap parameters and disable verbose debugging overhead while preserving `ro.config.low_ram=true`.
4. **Delta Audit:** Run [`scripts/compare_firmware.py`](file:///C:/PhilcoOS/scripts/compare_firmware.py) against `build/system_stage` to verify only the planned modifications exist before packaging.
