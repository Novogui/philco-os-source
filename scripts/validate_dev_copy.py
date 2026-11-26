#!/usr/bin/env python3
"""
PhilcoOS — Development Copy Validation Engine
Exhaustively compares factory_baseline/system against build/system_stage
verifying exact paths, casing, sizes, SHA-256 hashes, and symlinks.
"""

import os
import sys
import hashlib
import json
import time

PROJECT_ROOT = r"C:\PhilcoOS"
SRC_SYSTEM = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline", "system")
DST_STAGE = os.path.join(PROJECT_ROOT, "build", "system_stage")
OUTPUT_JSON = os.path.join(PROJECT_ROOT, "analysis", "dev_copy_validation.json")

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def scan_tree(root_dir):
    dirs = []
    files = {}
    symlinks = {}
    
    for r, d_list, f_list in os.walk(root_dir):
        rel_root = os.path.relpath(r, root_dir).replace("\\", "/")
        if rel_root != ".":
            dirs.append(rel_root)
            
        for f in f_list:
            fpath = os.path.join(r, f)
            rel_file = f if rel_root == "." else f"{rel_root}/{f}"
            
            if f.endswith(".symlink"):
                with open(fpath, "r", encoding="utf-8") as sf:
                    target = sf.read().strip()
                symlinks[rel_file] = target
            else:
                files[rel_file] = {
                    "size": os.path.getsize(fpath),
                    "sha256": sha256_file(fpath)
                }
                
    return dirs, files, symlinks

def main():
    t0 = time.time()
    print("=== PhilcoOS Development Copy Validation ===")
    
    print("[*] Scanning Source (factory_baseline/system)...")
    src_dirs, src_files, src_symlinks = scan_tree(SRC_SYSTEM)
    print(f"  Source: {len(src_dirs)} dirs, {len(src_files)} files, {len(src_symlinks)} symlinks.")
    
    print("[*] Scanning Destination (build/system_stage)...")
    dst_dirs, dst_files, dst_symlinks = scan_tree(DST_STAGE)
    print(f"  Destination: {len(dst_dirs)} dirs, {len(dst_files)} files, {len(dst_symlinks)} symlinks.")
    
    # 1. Compare Directories
    src_dir_set = set(src_dirs)
    dst_dir_set = set(dst_dirs)
    dirs_match = (src_dir_set == dst_dir_set)
    missing_dirs = list(src_dir_set - dst_dir_set)
    extra_dirs = list(dst_dir_set - src_dir_set)
    
    # 2. Compare Files
    src_file_keys = set(src_files.keys())
    dst_file_keys = set(dst_files.keys())
    missing_files = list(src_file_keys - dst_file_keys)
    extra_files = list(dst_file_keys - src_file_keys)
    
    size_mismatches = []
    hash_mismatches = []
    
    for fname in src_file_keys.intersection(dst_file_keys):
        s_info = src_files[fname]
        d_info = dst_files[fname]
        if s_info["size"] != d_info["size"]:
            size_mismatches.append(fname)
        if s_info["sha256"] != d_info["sha256"]:
            hash_mismatches.append(fname)
            
    # 3. Compare Symlinks
    src_sym_keys = set(src_symlinks.keys())
    dst_sym_keys = set(dst_symlinks.keys())
    missing_symlinks = list(src_sym_keys - dst_sym_keys)
    extra_symlinks = list(dst_sym_keys - src_sym_keys)
    symlink_target_mismatches = []
    
    for sname in src_sym_keys.intersection(dst_sym_keys):
        if src_symlinks[sname] != dst_symlinks[sname]:
            symlink_target_mismatches.append({
                "name": sname,
                "src_target": src_symlinks[sname],
                "dst_target": dst_symlinks[sname]
            })
            
    # 4. Collision check on destination
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
    
    collisions_verified = 0
    for f1, f2 in case_collision_pairs:
        if f1 in dst_files and f2 in dst_files:
            if dst_files[f1]["sha256"] != dst_files[f2]["sha256"]:
                collisions_verified += 1
                
    all_ok = (
        len(missing_dirs) == 0 and len(extra_dirs) == 0 and
        len(missing_files) == 0 and len(extra_files) == 0 and
        len(size_mismatches) == 0 and len(hash_mismatches) == 0 and
        len(missing_symlinks) == 0 and len(extra_symlinks) == 0 and
        len(symlink_target_mismatches) == 0 and
        collisions_verified == 8
    )
    
    report = {
        "status": "CONFIRMED" if all_ok else "FAILED",
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "source": SRC_SYSTEM,
        "destination": DST_STAGE,
        "directory_count": {"source": len(src_dirs), "destination": len(dst_dirs), "match": dirs_match},
        "regular_file_count": {"source": len(src_files), "destination": len(dst_files)},
        "symlink_count": {"source": len(src_symlinks), "destination": len(dst_symlinks)},
        "missing_files": missing_files,
        "extra_files": extra_files,
        "size_mismatches": size_mismatches,
        "hash_mismatches": hash_mismatches,
        "missing_symlinks": missing_symlinks,
        "extra_symlinks": extra_symlinks,
        "symlink_target_mismatches": symlink_target_mismatches,
        "case_collisions_verified": f"{collisions_verified}/8"
    }
    
    os.makedirs(os.path.dirname(OUTPUT_JSON), exist_ok=True)
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)
        
    print("\n=== VALIDATION SUMMARY ===")
    print(f"Status:                      {report['status']}")
    print(f"Directories Matched:         {len(src_dirs)}/{len(dst_dirs)} (Missing: {len(missing_dirs)}, Extra: {len(extra_dirs)})")
    print(f"Regular Files Matched:       {len(src_files)}/{len(dst_files)} (Missing: {len(missing_files)}, Extra: {len(extra_files)})")
    print(f"File Size Mismatches:        {len(size_mismatches)}")
    print(f"SHA-256 Hash Mismatches:     {len(hash_mismatches)}")
    print(f"Symlinks Matched:            {len(src_symlinks)}/{len(dst_symlinks)} (Missing: {len(missing_symlinks)}, Extra: {len(extra_symlinks)})")
    print(f"Symlink Target Mismatches:   {len(symlink_target_mismatches)}")
    print(f"Case Collisions Preserved:   {collisions_verified}/8")
    print(f"Validation time:             {time.time() - t0:.2f} seconds.")
    
    return all_ok

if __name__ == "__main__":
    main()
