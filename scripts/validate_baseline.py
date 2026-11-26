#!/usr/bin/env python3
"""
PhilcoOS — Baseline Validation Engine (Read-Only)
Validates all 2,117 entries, file hashes, symlink targets, and case-sensitive collisions
against the original master firmware archive.
"""

import os
import sys
import tarfile
import hashlib
import json
import time

PROJECT_ROOT = r"C:\PhilcoOS"
ORIGINAL_IMAGE = os.path.join(PROJECT_ROOT, "original", "PH42F10DSGWAVA8GB.img")
BASELINE_SYSTEM = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline", "system")

def sha256_bytes(data):
    return hashlib.sha256(data).hexdigest()

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def validate_baseline():
    t0 = time.time()
    print("=== PhilcoOS Baseline Validation ===")
    
    if not os.path.isfile(ORIGINAL_IMAGE):
        print(f"[!] Original image not found at {ORIGINAL_IMAGE}")
        sys.exit(1)
        
    print(f"[*] Opening original firmware archive: {ORIGINAL_IMAGE}")
    
    stats = {
        "total_tar_members": 0,
        "dirs_verified": 0,
        "dirs_missing": 0,
        "files_verified": 0,
        "files_mismatched_hash": 0,
        "files_missing": 0,
        "symlinks_verified": 0,
        "symlinks_target_mismatch": 0,
        "symlinks_missing": 0,
        "symlinks_converted_to_regular": 0,
        "case_collisions_verified": 0,
        "case_collisions_failed": 0,
        "case_collision_details": []
    }
    
    case_collision_pairs = [
        ("lib/libSync.so", "lib/libsync.so"),
        ("media/audio/notifications/vega.ogg", "media/audio/notifications/Vega.ogg"),
        ("media/audio/notifications/arcturus.ogg", "media/audio/notifications/Arcturus.ogg"),
        ("media/audio/ringtones/hydra.ogg", "media/audio/ringtones/Hydra.ogg"),
        ("media/audio/ringtones/Perseus.ogg", "media/audio/ringtones/PERSEUS.ogg"),
        ("media/audio/ringtones/ANDROMEDA.ogg", "media/audio/ringtones/Andromeda.ogg"),
        ("media/audio/ringtones/CANISMAJOR.ogg", "media/audio/ringtones/CanisMajor.ogg"),
        ("media/audio/ringtones/URSAMINOR.ogg", "media/audio/ringtones/UrsaMinor.ogg")
    ]
    
    with tarfile.open(ORIGINAL_IMAGE, "r:") as outer_tar:
        sys_fileobj = outer_tar.extractfile("package4/system.emmc.tar")
        with tarfile.open(fileobj=sys_fileobj, mode="r:") as sys_tar:
            members = sys_tar.getmembers()
            stats["total_tar_members"] = len(members)
            print(f"[*] Total entries in system.emmc.tar: {len(members)}")
            
            for idx, m in enumerate(members, 1):
                clean_name = m.name.lstrip("./").lstrip("/")
                disk_path = os.path.join(BASELINE_SYSTEM, clean_name.replace("/", os.sep))
                
                if m.isdir():
                    if os.path.isdir(disk_path):
                        stats["dirs_verified"] += 1
                    else:
                        stats["dirs_missing"] += 1
                        print(f"  [FAIL] Missing dir: {clean_name}")
                        
                elif m.issym() or m.islnk():
                    sym_file = disk_path + ".symlink"
                    # Check that no ordinary file was created where symlink should be
                    if os.path.isfile(disk_path):
                        stats["symlinks_converted_to_regular"] += 1
                        print(f"  [FAIL] Symlink converted to regular file on disk: {clean_name}")
                    
                    if os.path.isfile(sym_file):
                        with open(sym_file, "r", encoding="utf-8") as sf:
                            recorded_target = sf.read().strip()
                        if recorded_target == m.linkname:
                            stats["symlinks_verified"] += 1
                        else:
                            stats["symlinks_target_mismatch"] += 1
                            print(f"  [FAIL] Target mismatch for {clean_name}: expected {m.linkname}, got {recorded_target}")
                    else:
                        stats["symlinks_missing"] += 1
                        print(f"  [FAIL] Missing .symlink descriptor: {clean_name}")
                        
                else: # Regular file
                    if os.path.isfile(disk_path):
                        disk_hash = sha256_file(disk_path)
                        tar_data = sys_tar.extractfile(m).read()
                        tar_hash = sha256_bytes(tar_data)
                        
                        if disk_hash == tar_hash:
                            stats["files_verified"] += 1
                        else:
                            stats["files_mismatched_hash"] += 1
                            print(f"  [FAIL] Hash mismatch for {clean_name}: disk={disk_hash} != tar={tar_hash}")
                    else:
                        stats["files_missing"] += 1
                        print(f"  [FAIL] Missing file on disk: {clean_name}")
                        
                if idx % 500 == 0 or idx == len(members):
                    print(f"  Progress: {idx}/{len(members)} entries checked...")
                    
    print("\n[*] Validating case-colliding filenames...")
    for f1, f2 in case_collision_pairs:
        p1 = os.path.join(BASELINE_SYSTEM, f1.replace("/", os.sep))
        p2 = os.path.join(BASELINE_SYSTEM, f2.replace("/", os.sep))
        e1 = os.path.isfile(p1)
        e2 = os.path.isfile(p2)
        
        detail = {
            "pair": (f1, f2),
            "file1_exists": e1,
            "file2_exists": e2,
            "file1_size": os.path.getsize(p1) if e1 else None,
            "file2_size": os.path.getsize(p2) if e2 else None,
            "file1_hash": sha256_file(p1) if e1 else None,
            "file2_hash": sha256_file(p2) if e2 else None,
        }
        
        if e1 and e2 and detail["file1_hash"] != detail["file2_hash"]:
            stats["case_collisions_verified"] += 1
            detail["status"] = "CONFIRMED"
        else:
            stats["case_collisions_failed"] += 1
            detail["status"] = "FAILED"
            
        stats["case_collision_details"].append(detail)
        print(f"  Collision check: {f1} vs {f2} -> {detail['status']}")
        if e1 and e2:
            print(f"    {f1}: {detail['file1_size']:,} B, sha256={detail['file1_hash'][:16]}...")
            print(f"    {f2}: {detail['file2_size']:,} B, sha256={detail['file2_hash'][:16]}...")
            
    print(f"\n[+] Validation completed in {time.time() - t0:.2f} seconds.")
    print("=== SUMMARY STATS ===")
    for k, v in stats.items():
        if k != "case_collision_details":
            print(f"{k}: {v}")
            
    return stats

if __name__ == "__main__":
    validate_baseline()
