#!/usr/bin/env python3
"""
PhilcoOS — Firmware Extraction and Metadata Preservation Engine
Extracts outer container and Android system partition while preserving bit-exact POSIX metadata.
"""

import os
import sys
import tarfile
import hashlib
import json
import time

PROJECT_ROOT = r"C:\PhilcoOS"
ORIGINAL_IMAGE = os.path.join(PROJECT_ROOT, "original", "PH42F10DSGWAVA8GB.img")
BASELINE_DIR = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline")
FIRMWARE_DIR = os.path.join(BASELINE_DIR, "firmware")
SYSTEM_DIR = os.path.join(BASELINE_DIR, "system")
FIRMWARE_META_PATH = os.path.join(BASELINE_DIR, "firmware_metadata.json")
SYSTEM_META_PATH = os.path.join(BASELINE_DIR, "system_metadata.json")

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def sha256_bytes(data):
    return hashlib.sha256(data).hexdigest()

def extract_outer_firmware():
    print(f"[*] Extracting outer firmware container from: {ORIGINAL_IMAGE}")
    os.makedirs(FIRMWARE_DIR, exist_ok=True)
    
    metadata = {}
    with tarfile.open(ORIGINAL_IMAGE, "r:") as tar:
        members = tar.getmembers()
        print(f"[*] Found {len(members)} entries in outer container.")
        for idx, m in enumerate(members, 1):
            target_path = os.path.join(FIRMWARE_DIR, m.name.replace("/", os.sep))
            print(f"  [{idx:02d}/{len(members):02d}] Extracting {m.name} ({m.size:,} bytes)...")
            
            meta_entry = {
                "name": m.name,
                "size": m.size,
                "mode": oct(m.mode),
                "mode_int": m.mode,
                "uid": m.uid,
                "gid": m.gid,
                "uname": m.uname,
                "gname": m.gname,
                "mtime": m.mtime,
                "type": "dir" if m.isdir() else "file",
                "sha256": None
            }
            
            if m.isdir():
                os.makedirs(target_path, exist_ok=True)
            else:
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                with tar.extractfile(m) as src, open(target_path, "wb") as dst:
                    data = src.read()
                    dst.write(data)
                    meta_entry["sha256"] = sha256_bytes(data)
            
            metadata[m.name] = meta_entry
            
    with open(FIRMWARE_META_PATH, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    print(f"[+] Outer firmware metadata saved to: {FIRMWARE_META_PATH}")
    return metadata

def extract_system_partition():
    sys_tar_path = os.path.join(FIRMWARE_DIR, "package4", "system.emmc.tar")
    print(f"\n[*] Extracting system partition from: {sys_tar_path}")
    os.makedirs(SYSTEM_DIR, exist_ok=True)
    
    metadata = {}
    symlink_count = 0
    file_count = 0
    dir_count = 0
    
    with tarfile.open(sys_tar_path, "r:") as tar:
        members = tar.getmembers()
        total = len(members)
        print(f"[*] Total entries in system.emmc.tar: {total}")
        
        for idx, m in enumerate(members, 1):
            clean_name = m.name.lstrip("./").lstrip("/")
            target_path = os.path.join(SYSTEM_DIR, clean_name.replace("/", os.sep))
            
            entry_type = "file"
            linkname = None
            sha256_val = None
            
            if m.isdir():
                entry_type = "dir"
                dir_count += 1
                os.makedirs(target_path, exist_ok=True)
            elif m.issym() or m.islnk():
                entry_type = "symlink" if m.issym() else "hardlink"
                linkname = m.linkname
                symlink_count += 1
                # On Windows, record in metadata and create placeholder file with .symlink marker
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                with open(target_path + ".symlink", "w", encoding="utf-8") as f:
                    f.write(linkname)
            else:
                entry_type = "file"
                file_count += 1
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                with tar.extractfile(m) as src, open(target_path, "wb") as dst:
                    data = src.read()
                    dst.write(data)
                    sha256_val = sha256_bytes(data)
                    
            metadata[clean_name] = {
                "name": clean_name,
                "type": entry_type,
                "size": m.size,
                "mode": oct(m.mode),
                "mode_int": m.mode,
                "uid": m.uid,
                "gid": m.gid,
                "uname": m.uname,
                "gname": m.gname,
                "mtime": m.mtime,
                "linkname": linkname,
                "sha256": sha256_val
            }
            
            if idx % 200 == 0 or idx == total:
                print(f"  Processed {idx}/{total} entries ({dir_count} dirs, {file_count} files, {symlink_count} symlinks)...")
                
    with open(SYSTEM_META_PATH, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    print(f"[+] System metadata saved to: {SYSTEM_META_PATH}")
    print(f"[+] Summary: {total} total entries ({dir_count} dirs, {file_count} files, {symlink_count} symlinks)")
    return metadata

def main():
    t0 = time.time()
    print("=== PhilcoOS Firmware Extraction Engine ===")
    if not os.path.isfile(ORIGINAL_IMAGE):
        print(f"[!] Error: Original firmware not found at {ORIGINAL_IMAGE}")
        sys.exit(1)
        
    extract_outer_firmware()
    extract_system_partition()
    print(f"\n[+] Extraction completed successfully in {time.time() - t0:.2f} seconds.")

if __name__ == "__main__":
    main()
