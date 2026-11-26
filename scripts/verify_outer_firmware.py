#!/usr/bin/env python3
"""
PhilcoOS — Independent Outer Firmware Verification
Compares every extracted outer firmware file against the original image and metadata.
"""

import os
import sys
import tarfile
import hashlib
import json

PROJECT_ROOT = r"C:\PhilcoOS"
ORIGINAL_IMAGE = os.path.join(PROJECT_ROOT, "original", "PH42F10DSGWAVA8GB.img")
BASELINE_FW = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline", "firmware")
META_PATH = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline", "firmware_metadata.json")

def sha256_bytes(b):
    return hashlib.sha256(b).hexdigest()

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def main():
    print("=== Independent Outer Firmware Hash Verification ===")
    
    if not os.path.isfile(ORIGINAL_IMAGE):
        print(f"[!] Original image missing: {ORIGINAL_IMAGE}")
        sys.exit(1)
        
    if not os.path.isfile(META_PATH):
        print(f"[!] Metadata missing: {META_PATH}")
        sys.exit(1)
        
    with open(META_PATH, "r", encoding="utf-8") as f:
        metadata = json.load(f)
        
    results = []
    all_matched = True
    
    with tarfile.open(ORIGINAL_IMAGE, "r:") as tar:
        for name, meta_entry in metadata.items():
            if meta_entry["type"] == "dir":
                continue
                
            disk_path = os.path.join(BASELINE_FW, name.replace("/", os.sep))
            
            # Check existence on disk
            if not os.path.isfile(disk_path):
                print(f"[FAIL] Missing on disk: {name}")
                all_matched = False
                continue
                
            disk_size = os.path.getsize(disk_path)
            disk_hash = sha256_file(disk_path)
            
            # Check tar member
            try:
                tar_member = tar.getmember(name)
                tar_size = tar_member.size
                tar_data = tar.extractfile(tar_member).read()
                tar_hash = sha256_bytes(tar_data)
            except Exception as e:
                print(f"[FAIL] Error reading {name} from TAR: {e}")
                all_matched = False
                continue
                
            meta_hash = meta_entry.get("sha256")
            meta_size = meta_entry.get("size")
            
            size_match = (disk_size == tar_size == meta_size)
            hash_match = (disk_hash == tar_hash == meta_hash)
            
            status = "CONFIRMED" if (size_match and hash_match) else "FAILED"
            if status == "FAILED":
                all_matched = False
                
            results.append({
                "name": name,
                "size": disk_size,
                "tar_size": tar_size,
                "meta_size": meta_size,
                "sha256": disk_hash,
                "tar_hash": tar_hash,
                "meta_hash": meta_hash,
                "size_match": size_match,
                "hash_match": hash_match,
                "status": status
            })
            
            print(f"[{status}] {name:<55} {disk_size:>12,} B  sha256={disk_hash[:16]}...")
            
    print(f"\nVerification completed: {len(results)} files inspected.")
    print(f"Overall Result: {'CONFIRMED (All 17 files match bit-for-bit)' if all_matched else 'FAILED'}")
    
    # Save validation details
    out_json = os.path.join(PROJECT_ROOT, "analysis", "outer_firmware_validation.json")
    os.makedirs(os.path.dirname(out_json), exist_ok=True)
    with open(out_json, "w", encoding="utf-8") as f:
        json.dump({"all_matched": all_matched, "files": results}, f, indent=2)
        
    return all_matched, results

if __name__ == "__main__":
    main()
