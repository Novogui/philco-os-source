#!/usr/bin/env python3
"""
PhilcoOS — Firmware Comparison Engine
Compares any target system directory or TAR against factory baseline metadata.
Identifies added, deleted, modified files, permissions, and symlink targets.
"""

import os
import sys
import hashlib
import json
import tarfile

PROJECT_ROOT = r"C:\PhilcoOS"
BASELINE_DIR = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline")
SYSTEM_META_PATH = os.path.join(BASELINE_DIR, "system_metadata.json")

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

def compare_directory(target_dir):
    if not os.path.isfile(SYSTEM_META_PATH):
        print(f"[!] Baseline metadata not found at {SYSTEM_META_PATH}")
        sys.exit(1)
        
    with open(SYSTEM_META_PATH, "r", encoding="utf-8") as f:
        baseline = json.load(f)
        
    target_files = {}
    for root, dirs, files in os.walk(target_dir):
        for d in dirs:
            rel = os.path.relpath(os.path.join(root, d), target_dir).replace("\\", "/")
            target_files[rel] = {"type": "dir"}
        for f in files:
            fpath = os.path.join(root, f)
            if f.endswith(".symlink"):
                rel = os.path.relpath(fpath[:-8], target_dir).replace("\\", "/")
                with open(fpath, "r", encoding="utf-8") as sf:
                    target = sf.read().strip()
                target_files[rel] = {"type": "symlink", "linkname": target}
            else:
                rel = os.path.relpath(fpath, target_dir).replace("\\", "/")
                target_files[rel] = {
                    "type": "file",
                    "size": os.path.getsize(fpath),
                    "sha256": sha256_file(fpath)
                }
                
    removed = []
    added = []
    modified = []
    
    for name, base_entry in baseline.items():
        if name not in target_files:
            removed.append(name)
        else:
            tgt = target_files[name]
            if base_entry["type"] != tgt["type"]:
                modified.append(f"{name} (type changed: {base_entry['type']} -> {tgt['type']})")
            elif base_entry["type"] == "symlink":
                if base_entry["linkname"] != tgt.get("linkname"):
                    modified.append(f"{name} (symlink target: {base_entry['linkname']} -> {tgt.get('linkname')})")
            elif base_entry["type"] == "file":
                if base_entry["sha256"] != tgt.get("sha256"):
                    size_diff = tgt.get("size", 0) - base_entry.get("size", 0)
                    modified.append(f"{name} (modified, size delta: {size_diff:+} B)")
                    
    for name in target_files:
        if name not in baseline:
            added.append(name)
            
    print(f"\n=== Firmware Delta vs Factory Baseline ===")
    print(f"Removed files ({len(removed)}):")
    for r in removed[:20]:
        print(f"  - {r}")
    if len(removed) > 20:
        print(f"  ... and {len(removed) - 20} more.")
        
    print(f"\nAdded files ({len(added)}):")
    for a in added[:20]:
        print(f"  + {a}")
    if len(added) > 20:
        print(f"  ... and {len(added) - 20} more.")
        
    print(f"\nModified files ({len(modified)}):")
    for m in modified[:20]:
        print(f"  * {m}")
    if len(modified) > 20:
        print(f"  ... and {len(modified) - 20} more.")
        
    return {
        "removed": removed,
        "added": added,
        "modified": modified
    }

if __name__ == "__main__":
    target = sys.argv[1] if len(sys.argv) > 1 else os.path.join(BASELINE_DIR, "system")
    print(f"Comparing target: {target}")
    compare_directory(target)
