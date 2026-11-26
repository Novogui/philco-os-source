#!/usr/bin/env python3
"""
PhilcoOS — Development Copy Creation Engine
Creates an isolated development copy at C:\\PhilcoOS\\build\\system_stage
strictly preserving exact filename case, metadata, and all case-colliding pairs.
"""

import os
import sys
import shutil
import hashlib
import json
import subprocess
import time

PROJECT_ROOT = r"C:\PhilcoOS"
SRC_SYSTEM = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline", "system")
DST_STAGE = os.path.join(PROJECT_ROOT, "build", "system_stage")
SRC_META = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline", "system_metadata.json")
DST_META = os.path.join(PROJECT_ROOT, "build", "system_stage_metadata.json")

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def check_case_sensitive(path):
    try:
        res = subprocess.run(["fsutil.exe", "file", "queryCaseSensitiveInfo", path],
                             capture_output=True, text=True, check=True)
        return "habilitado" in res.stdout.lower() or "enabled" in res.stdout.lower()
    except Exception:
        return False

def main():
    t0 = time.time()
    print("=== PhilcoOS Development Copy Creation ===")
    
    if not os.path.isdir(SRC_SYSTEM):
        print(f"[!] Source system directory missing: {SRC_SYSTEM}")
        sys.exit(1)
        
    print(f"[*] Source:      {SRC_SYSTEM}")
    print(f"[*] Destination: {DST_STAGE}")
    
    # 1. Ensure DST_STAGE directory exists
    os.makedirs(DST_STAGE, exist_ok=True)
    
    # Verify case sensitivity of DST_STAGE
    is_cs = check_case_sensitive(DST_STAGE)
    print(f"[*] Destination directory case-sensitivity: {'ENABLED' if is_cs else 'DISABLED'}")
    if not is_cs:
        print("[!] Warning: Destination is not case-sensitive. Please check fsutil.")
        
    # 2. Replicate directory hierarchy first
    print("[*] Creating directory structure...")
    dir_count = 0
    for root, dirs, _ in os.walk(SRC_SYSTEM):
        rel = os.path.relpath(root, SRC_SYSTEM)
        if rel != ".":
            target_dir = os.path.join(DST_STAGE, rel)
            os.makedirs(target_dir, exist_ok=True)
            dir_count += 1
            
    print(f"[+] Created {dir_count} subdirectories.")
    
    # 3. Copy files with exact filename casing
    print("[*] Copying files (preserving exact case, timestamps, and .symlink descriptors)...")
    file_count = 0
    symlink_count = 0
    
    for root, _, files in os.walk(SRC_SYSTEM):
        rel = os.path.relpath(root, SRC_SYSTEM)
        target_dir = DST_STAGE if rel == "." else os.path.join(DST_STAGE, rel)
        
        for f in files:
            src_file = os.path.join(root, f)
            dst_file = os.path.join(target_dir, f)
            
            # Binary copy preserving file attributes and timestamps
            shutil.copy2(src_file, dst_file)
            
            if f.endswith(".symlink"):
                symlink_count += 1
            else:
                file_count += 1
                
            total = file_count + symlink_count
            if total % 500 == 0:
                print(f"  Copied {total} items ({file_count} files, {symlink_count} symlinks)...")
                
    print(f"[+] Total copied: {file_count} regular files, {symlink_count} symlink descriptors.")
    
    # 4. Copy reference metadata
    if os.path.isfile(SRC_META):
        shutil.copy2(SRC_META, DST_META)
        print(f"[+] Saved reference metadata to: {DST_META}")
        
    # 5. Immediate verification of critical case collisions
    print("\n[*] Immediate verification of case-collision pairs in destination:")
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
    
    all_collisions_ok = True
    for f1, f2 in case_collision_pairs:
        p1 = os.path.join(DST_STAGE, f1.replace("/", os.sep))
        p2 = os.path.join(DST_STAGE, f2.replace("/", os.sep))
        e1 = os.path.isfile(p1)
        e2 = os.path.isfile(p2)
        
        s1 = os.path.getsize(p1) if e1 else None
        s2 = os.path.getsize(p2) if e2 else None
        h1 = sha256_file(p1) if e1 else None
        h2 = sha256_file(p2) if e2 else None
        
        pair_ok = (e1 and e2 and h1 != h2 and s1 is not None and s2 is not None)
        status = "CONFIRMED" if pair_ok else "FAILED"
        if not pair_ok:
            all_collisions_ok = False
            
        print(f"  [{status}] {f1} ({s1:,} B) vs {f2} ({s2:,} B)")
        
    print(f"\n[+] Development copy creation completed in {time.time() - t0:.2f} seconds.")
    print(f"[*] Collision Preservation Status: {'CONFIRMED (100% Intact)' if all_collisions_ok else 'FAILED'}")
    return all_collisions_ok

if __name__ == "__main__":
    main()
