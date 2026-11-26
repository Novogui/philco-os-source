#!/usr/bin/env python3
"""
PhilcoOS — Firmware Repackaging Engine (Build Pipeline Preparation)
Builds system.emmc.tar using exact POSIX TAR headers and symlinks reconstructed from metadata.
"""

import os
import sys
import tarfile
import json
import time

PROJECT_ROOT = r"C:\PhilcoOS"
BASELINE_DIR = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline")
SYSTEM_META_PATH = os.path.join(BASELINE_DIR, "system_metadata.json")

def repack_system_tar(source_dir, output_tar_path, metadata_path=SYSTEM_META_PATH):
    print(f"[*] Repackaging system partition from: {source_dir}")
    print(f"[*] Output destination: {output_tar_path}")
    
    with open(metadata_path, "r", encoding="utf-8") as f:
        meta = json.load(f)
        
    os.makedirs(os.path.dirname(output_tar_path), exist_ok=True)
    
    t0 = time.time()
    count = 0
    with tarfile.open(output_tar_path, "w:", format=tarfile.USTAR_FORMAT) as tar:
        # Collect all items in source_dir
        for root, dirs, files in os.walk(source_dir):
            for d in dirs:
                rel = os.path.relpath(os.path.join(root, d), source_dir).replace("\\", "/")
                entry_meta = meta.get(rel, {})
                ti = tarfile.TarInfo(name=rel)
                ti.type = tarfile.DIRTYPE
                ti.mode = entry_meta.get("mode_int", 0o755)
                ti.uid = entry_meta.get("uid", 1006)
                ti.gid = entry_meta.get("gid", 1006)
                ti.uname = entry_meta.get("uname", "wubf")
                ti.gname = entry_meta.get("gname", "wubf")
                ti.mtime = int(entry_meta.get("mtime", time.time()))
                tar.addfile(ti)
                count += 1
                
            for f in files:
                fpath = os.path.join(root, f)
                if f.endswith(".symlink"):
                    rel = os.path.relpath(fpath[:-8], source_dir).replace("\\", "/")
                    with open(fpath, "r", encoding="utf-8") as sf:
                        target = sf.read().strip()
                    entry_meta = meta.get(rel, {})
                    ti = tarfile.TarInfo(name=rel)
                    ti.type = tarfile.SYMTYPE
                    ti.linkname = target
                    ti.mode = entry_meta.get("mode_int", 0o777)
                    ti.uid = entry_meta.get("uid", 1006)
                    ti.gid = entry_meta.get("gid", 1006)
                    ti.uname = entry_meta.get("uname", "wubf")
                    ti.gname = entry_meta.get("gname", "wubf")
                    ti.mtime = int(entry_meta.get("mtime", time.time()))
                    tar.addfile(ti)
                    count += 1
                else:
                    rel = os.path.relpath(fpath, source_dir).replace("\\", "/")
                    entry_meta = meta.get(rel, {})
                    ti = tarfile.TarInfo(name=rel)
                    ti.type = tarfile.REGTYPE
                    ti.size = os.path.getsize(fpath)
                    ti.mode = entry_meta.get("mode_int", 0o644)
                    ti.uid = entry_meta.get("uid", 1006)
                    ti.gid = entry_meta.get("gid", 1006)
                    ti.uname = entry_meta.get("uname", "wubf")
                    ti.gname = entry_meta.get("gname", "wubf")
                    ti.mtime = int(entry_meta.get("mtime", time.time()))
                    with open(fpath, "rb") as content:
                        tar.addfile(ti, content)
                    count += 1
                    
    tar_size = os.path.getsize(output_tar_path)
    print(f"[+] Successfully packed {count} entries into {output_tar_path} ({tar_size:,} bytes) in {time.time()-t0:.2f}s")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python repack_firmware.py <source_system_dir> <output_system_tar>")
        sys.exit(1)
    repack_system_tar(sys.argv[1], sys.argv[2])
