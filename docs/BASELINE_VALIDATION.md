# PhilcoOS — Factory Baseline Read-Only Validation Report

**Target Device:** Philco PH42F10DSGW (Smart TV 42" LED)  
**Mainboard:** CVTE TP.RT2984T.PB801 / Realtek RTD2984TD-GR (`rtd298x`)  
**Base Image:** `PH42F10DSGWAVA8GB.img` (SHA-256: `9BCEF48B439998947C2AC64E2C127D7A83D7FBF2FC9F2EB0579ED4C8D7EAFFC0`)  
**Document:** `C:\PhilcoOS\docs\BASELINE_VALIDATION.md`  
**Date:** 2026-11-20  
**Validation Methodology:** 100% Read-Only Comparison of On-Disk Files vs. Original Uncompressed Archive Streams  

---

## 1. Executive Summary & Suitability Assessment

A comprehensive, read-only integrity audit was executed across the entire factory baseline located at:
* Outer Firmware: `C:\PhilcoOS\extracted\factory_baseline\firmware`
* System Filesystem: `C:\PhilcoOS\extracted\factory_baseline\system`

Every single regular file was verified by streaming the raw bytes directly from the original nested TAR archive (`package4/system.emmc.tar` inside `PH42F10DSGWAVA8GB.img`), calculating SHA-256 cryptographic hashes on the fly, and comparing against the on-disk extracted file. Every symbolic link was validated against original link targets, and all case-sensitive collision pairs were verified on disk.

### Baseline Suitability Determination
> **Verdict:** `CONFIRMED — SUITABLE FOR DEVELOPMENT COPY`  
> The factory baseline is 100% bit-exact, exhibits zero missing or corrupted files, preserves all 317 symbolic link targets, and maintains full case-separation for all 8 colliding pairs. It provides a flawless, uncorrupted master reference for cloning into an isolated development workspace (`C:\PhilcoOS\build\` or `C:\PhilcoOS\source\`).

---

## 2. Item-by-Item Baseline Validation Results

| # | Validation Item | Status | Verification Summary & Evidence |
| :--- | :--- | :--- | :--- |
| **1** | All 2,117 entries verified against original TAR | `CONFIRMED` | 172 dirs, 1,628 regular files, 317 symlinks. Exactly 2,117 entries accounted for. |
| **2** | SHA-256 hashes of regular files vs. TAR members | `CONFIRMED` | 1,628 / 1,628 files matched bit-for-bit (0 mismatches, 0 missing). |
| **3** | All 317 symlink descriptors vs. TAR targets | `CONFIRMED` | 317 / 317 symlink targets matched exactly (0 target mismatches). |
| **4** | No symlink silently converted to regular file | `CONFIRMED` | 0 symlinks materialized as regular files on disk; all 317 exist as `.symlink` descriptors with target strings. |
| **5** | Case-colliding filename survival (`libSync.so` vs `libsync.so`) | `CONFIRMED` | All 8 collision pairs exist simultaneously on disk with unique sizes and distinct SHA-256 hashes. |
| **6** | Case-sensitive attributes on colliding directories | `CONFIRMED` | Queried via `fsutil`: case sensitivity is enabled (`habilitado`) on `system`, `lib`, and `media` directories. |
| **7** | Review of scripts (`extract`, `inventory`, `compare`, `repack`) | `CONFIRMED` | All 4 scripts reviewed: path safety, USTAR format compliance, and AXML/ELF parsers confirmed correct. |
| **8** | Factory baseline untouched & pristine | `CONFIRMED` | All 17 outer firmware files and 1,628 system files match golden hashes; delta test yields 0 added/removed/modified. |
| **9** | Complete APK manifest metadata capture | `CONFIRMED` | 53 / 53 APKs captured (0 unknown package names); 526 perms, 70 services, 52 receivers, 25 providers, 355 activities. |
| **10**| Reporting of proposed manual permission restoration | `CONFIRMED` | Exact 3-step PowerShell procedure documented from `EXTRACTION_BLOCKER.md` Section 7. |

---

## 3. Detailed Audit Evidence

### 3.1 Overall Member Accounting & Cryptographic Integrity

```text
================================================================================
Baseline Validation Summary Statistics
================================================================================
Total Original TAR Members : 2,117
Directories Verified       : 172       (Missing: 0)
Regular Files Verified     : 1,628     (Hash Mismatches: 0, Missing: 0)
Symbolic Links Verified    : 317       (Target Mismatches: 0, Converted: 0)
Outer Container Files      : 17        (Hash Mismatches: 0, Missing: 0)
Overall Archive Integrity  : 100% BIT-EXACT MATCH [CONFIRMED]
================================================================================
```

### 3.2 Case-Sensitive Filename Collisions Audit `[CONFIRMED]`

Because Windows NTFS default settings are case-insensitive, Linux firmware archives containing files differing only by case can cause silent data loss. The table below proves that **both distinct files survive on disk with their correct individual sizes and unique SHA-256 hashes**:

| File 1 | Size 1 | SHA-256 (File 1) | File 2 | Size 2 | SHA-256 (File 2) | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `lib/libSync.so` | 112,068 B | `115ad4601e76c626...` | `lib/libsync.so` | 5,252 B | `f8be66ff3becda9a...` | `CONFIRMED` |
| `notifications/vega.ogg` | 29,476 B | `eb098eac0e8d4c73...` | `notifications/Vega.ogg` | 29,310 B | `91c041ca4c138a73...` | `CONFIRMED` |
| `notifications/arcturus.ogg` | 7,908 B | `6487240c9d6dffe7...` | `notifications/Arcturus.ogg` | 7,933 B | `2492fa4d4e84afff...` | `CONFIRMED` |
| `ringtones/hydra.ogg` | 22,962 B | `5c4fc32cc39683fb...` | `ringtones/Hydra.ogg` | 22,924 B | `c979c78cc857715e...` | `CONFIRMED` |
| `ringtones/Perseus.ogg` | 236,343 B | `c1e82baa61a6f33a...` | `ringtones/PERSEUS.ogg` | 112,981 B | `cde22a2122705063...` | `CONFIRMED` |
| `ringtones/ANDROMEDA.ogg` | 25,014 B | `893fcf16d0346541...` | `ringtones/Andromeda.ogg` | 24,973 B | `c8461a9f82bb528f...` | `CONFIRMED` |
| `ringtones/CANISMAJOR.ogg` | 31,170 B | `bdbd5c9f2d23bdb3...` | `ringtones/CanisMajor.ogg` | 31,133 B | `c3fb7680f2c23cd2...` | `CONFIRMED` |
| `ringtones/URSAMINOR.ogg` | 144,112 B | `81c37cc3851508e0...` | `ringtones/UrsaMinor.ogg` | 185,005 B | `6f2a74c5124578f3...` | `CONFIRMED` |

### 3.3 Directory Case-Sensitivity Attribute Verification `[CONFIRMED]`

Queried using read-only `fsutil.exe file queryCaseSensitiveInfo`:
* `C:\PhilcoOS\extracted\factory_baseline`: **Habilitado (Enabled)**
* `C:\PhilcoOS\extracted\factory_baseline\system`: **Habilitado (Enabled)**
* `C:\PhilcoOS\extracted\factory_baseline\system\lib`: **Habilitado (Enabled)**
* `C:\PhilcoOS\extracted\factory_baseline\system\media\audio\notifications`: **Habilitado (Enabled)**
* `C:\PhilcoOS\extracted\factory_baseline\system\media\audio\ringtones`: **Habilitado (Enabled)**

### 3.4 Symbolic Link Descriptor Verification `[CONFIRMED]`

* **Total Symlinks in Stock Image:** 317 entries (primarily standard Android `/system/bin` links to `toolbox` and `/system/xbin` links to `busybox`).
* **Conversion Check:** Verified that zero symlink paths materialized as regular files or copied executables.
* **Integrity:** Every single symlink has an associated `.symlink` descriptor file on disk containing the exact target string (e.g., `bin/sh.symlink` contains `mksh`, `bin/ls.symlink` contains `toolbox`). All 317 targets match `m.linkname` from the original TAR with zero mismatches.

---

## 4. Script Quality & Correctness Review

A read-only structural audit of the four project scripts in `C:\PhilcoOS\scripts\` confirms their operational correctness:

1. **[`extract_firmware.py`](file:///C:/PhilcoOS/scripts/extract_firmware.py): `CONFIRMED`**
   - Correctly opens original archives in read-only mode (`r:`).
   - Generates exact JSON metadata manifests recording POSIX mode, UID, GID, mtime, and SHA-256.
   - Accurately branches between directory, regular file, and symbolic link entries.
2. **[`inventory_firmware.py`](file:///C:/PhilcoOS/scripts/inventory_firmware.py): `CONFIRMED`**
   - Self-contained Python binary AXML parser correctly handles UTF-8 and UTF-16 string pools, namespace offsets, and attribute array indexing.
   - ELF parser correctly verifies 32-bit little-endian ARM architecture (`EM_ARM` / `0x28`), reads `.dynamic` tags, and resolves `DT_NEEDED` dependencies against `.dynstr`.
3. **[`compare_firmware.py`](file:///C:/PhilcoOS/scripts/compare_firmware.py): `CONFIRMED`**
   - Correctly compares target filesystem trees against golden metadata.
   - Validated against `factory_baseline/system`: correctly reports **0 added, 0 removed, 0 modified files**.
4. **[`repack_firmware.py`](file:///C:/PhilcoOS/scripts/repack_firmware.py): `CONFIRMED`**
   - Employs standard POSIX USTAR format (`tarfile.USTAR_FORMAT`).
   - Reconstructs true `SYMTYPE` TAR entries with original `linkname`s, preserving POSIX permission modes, builder UID/GID (`1006:1006`), and timestamps from metadata.

---

## 5. APK Manifest & Component Parsing Verification `[CONFIRMED]`

The inventory parser was executed against all applications in `/system/app` and `/system/priv-app`. The database saved at [`C:/PhilcoOS/analysis/system_manifest.json`](file:///C:/PhilcoOS/analysis/system_manifest.json) confirms:

* **Total Applications Parsed:** 53
* **Package Names Captured:** 53 / 53 (0 "Unknown" entries, 100% resolution rate)
* **Total Components & Metadata Parsed:**
  - **Permissions Requested:** 526 permissions across all applications
  - **Services Declared:** 70 background and system services
  - **Broadcast Receivers:** 52 receivers (including `BOOT_COMPLETED`, hardware key receivers)
  - **Content Providers:** 25 providers (Settings, Telephony, Media, Downloads)
  - **Activities Declared:** 355 user-facing and sub-activity screens
  - **Bundled Native Libraries:** 14 bundled `.so` files cataloged (e.g. inside `LatinIME.apk`, `aptoide.apk`, `QjyBrowser.apk`)

---

## 6. Proposed Manual Security Permission Restoration Procedure `[CONFIRMED]`

As documented in Section 7 of [`C:/PhilcoOS/docs/EXTRACTION_BLOCKER.md`](file:///C:/PhilcoOS/docs/EXTRACTION_BLOCKER.md), the exact manual procedure proposed for the user to restore standard Windows inherited security permissions on `C:\PhilcoOS` from an **Elevated (Administrator) PowerShell prompt** is:

```powershell
# 1. Restore standard Medium Mandatory Integrity Level (resets Low IL)
icacls "C:\PhilcoOS" /setintegritylevel "(OI)(CI)M" /T

# 2. Reset DACL inheritance to clean Windows system defaults
icacls "C:\PhilcoOS" /reset /T

# 3. Ensure the current user has standard administrative modify rights
icacls "C:\PhilcoOS" /grant "${env:USERNAME}:(OI)(CI)M" /T
```

*Strict Compliance Note: As instructed by project directives, Antigravity has not executed any of these commands automatically.*

---

## 7. Final Recommendation & Readiness for Development

* **Factory Baseline Status:** 100% Bit-Exact and Fully Verified.
* **Workspace Isolation:** Read-only baseline remains permanently quarantined at `C:\PhilcoOS\extracted\factory_baseline`.
* **Development Staging:** The project is now ready to safely clone `factory_baseline/system` into a designated development directory (such as `C:\PhilcoOS\build\system_stage`) where selective debloating, launcher replacement, and `build.prop` low-RAM optimizations can take place without touching the original factory reference.
