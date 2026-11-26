#!/usr/bin/env python3
"""
PhilcoOS — Firmware Inventory and Manifest Engine
Parses all system applications, ELF libraries, binaries, services, permissions, and dependencies.
Generates comprehensive JSON and Markdown manifests.
"""

import os
import sys
import struct
import zipfile
import hashlib
import json
import time

PROJECT_ROOT = r"C:\PhilcoOS"
BASELINE_DIR = os.path.join(PROJECT_ROOT, "extracted", "factory_baseline")
SYSTEM_DIR = os.path.join(BASELINE_DIR, "system")
SYSTEM_META_PATH = os.path.join(BASELINE_DIR, "system_metadata.json")
OUTPUT_MD = os.path.join(PROJECT_ROOT, "docs", "SYSTEM_MANIFEST.md")
OUTPUT_JSON = os.path.join(PROJECT_ROOT, "analysis", "system_manifest.json")

def sha256_file(filepath):
    h = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(1024 * 1024):
            h.update(chunk)
    return h.hexdigest()

# ==============================================================================
# Android Binary XML (AXML) Parser
# ==============================================================================
class AXMLParser:
    def __init__(self, data):
        self.data = data
        self.strings = []
        self.package_name = None
        self.shared_user_id = None
        self.version_code = None
        self.version_name = None
        self.permissions = []
        self.services = []
        self.receivers = []
        self.providers = []
        self.activities = []
        self._parse()

    def _get_string(self, idx):
        if 0 <= idx < len(self.strings):
            return self.strings[idx]
        return ""

    def _parse(self):
        if len(self.data) < 8:
            return
        magic = struct.unpack_from("<I", self.data, 0)[0]
        if magic != 0x00080003: # RES_XML_TYPE
            return
            
        cur = 8
        while cur < len(self.data):
            if cur + 8 > len(self.data):
                break
            chunk_type, header_size, chunk_size = struct.unpack_from("<HHI", self.data, cur)
            
            if chunk_type == 0x0001: # RES_STRING_POOL_TYPE
                sc = struct.unpack_from("<I", self.data, cur + 8)[0]
                flags = struct.unpack_from("<I", self.data, cur + 16)[0]
                is_utf8 = (flags & (1 << 8)) != 0
                s_start = cur + struct.unpack_from("<I", self.data, cur + 20)[0]
                offsets = [struct.unpack_from("<I", self.data, cur + 28 + (i * 4))[0] for i in range(sc)]
                for off in offsets:
                    p = s_start + off
                    if p >= len(self.data):
                        self.strings.append("")
                        continue
                    if is_utf8:
                        u16len = self.data[p]
                        p += 1
                        if u16len & 0x80:
                            p += 1
                        u8len = self.data[p]
                        p += 1
                        if u8len & 0x80:
                            p += 1
                        s = self.data[p:p + u8len].decode("utf-8", errors="replace")
                    else:
                        slen = struct.unpack_from("<H", self.data, p)[0]
                        s = self.data[p + 2:p + 2 + (slen * 2)].decode("utf-16le", errors="replace")
                    self.strings.append(s)
                    
            elif chunk_type == 0x0102: # RES_XML_START_ELEMENT_TYPE
                tag_ns_idx, tag_name_idx = struct.unpack_from("<II", self.data, cur + 16)
                attr_start, attr_size, attr_count = struct.unpack_from("<HHH", self.data, cur + 24)
                tag_name = self._get_string(tag_name_idx)
                
                attrs = {}
                for i in range(attr_count):
                    attr_off = cur + 16 + attr_start + (i * attr_size)
                    if attr_off + 20 > len(self.data):
                        break
                    ans, aname, val_str, val_type, val_data = struct.unpack_from("<IIIII", self.data, attr_off)
                    key = self._get_string(aname)
                    val = self._get_string(val_str) if val_str != 0xFFFFFFFF and val_str < len(self.strings) else str(val_data)
                    attrs[key] = val
                    
                if tag_name == "manifest":
                    self.package_name = attrs.get("package")
                    self.shared_user_id = attrs.get("sharedUserId")
                    self.version_code = attrs.get("versionCode")
                    self.version_name = attrs.get("versionName")
                elif tag_name == "uses-permission":
                    name = attrs.get("name")
                    if name:
                        self.permissions.append(name)
                elif tag_name == "service":
                    name = attrs.get("name")
                    if name:
                        self.services.append(name)
                elif tag_name == "receiver":
                    name = attrs.get("name")
                    if name:
                        self.receivers.append(name)
                elif tag_name == "provider":
                    name = attrs.get("name")
                    if name:
                        self.providers.append({
                            "name": name,
                            "authorities": attrs.get("authorities")
                        })
                elif tag_name == "activity":
                    name = attrs.get("name")
                    if name:
                        self.activities.append(name)
                        
            cur += chunk_size

# ==============================================================================
# ELF Binary / Shared Library Parser (32-bit ARM)
# ==============================================================================
class ELFParser:
    def __init__(self, filepath):
        self.filepath = filepath
        self.is_valid = False
        self.is_arm32 = False
        self.soname = None
        self.needed = []
        self._parse()

    def _parse(self):
        try:
            with open(self.filepath, "rb") as f:
                header = f.read(52)
                if len(header) < 52 or header[:4] != b"\x7fELF":
                    return
                self.is_valid = True
                ei_class = header[4] # 1 = 32-bit
                ei_data = header[5]  # 1 = little endian
                e_machine = struct.unpack_from("<H", header, 18)[0]
                if ei_class == 1 and ei_data == 1 and e_machine == 0x28: # EM_ARM
                    self.is_arm32 = True
                
                e_phoff = struct.unpack_from("<I", header, 28)[0]
                e_shoff = struct.unpack_from("<I", header, 32)[0]
                e_phentsize = struct.unpack_from("<H", header, 42)[0]
                e_phnum = struct.unpack_from("<H", header, 44)[0]
                e_shentsize = struct.unpack_from("<H", header, 46)[0]
                e_shnum = struct.unpack_from("<H", header, 48)[0]
                e_shstrndx = struct.unpack_from("<H", header, 50)[0]
                
                f.seek(e_shoff)
                sections = []
                for i in range(e_shnum):
                    s_data = f.read(e_shentsize)
                    sh_name, sh_type, sh_flags, sh_addr, sh_offset, sh_size = struct.unpack_from("<IIIIII", s_data, 0)
                    sections.append({
                        "name_idx": sh_name,
                        "type": sh_type,
                        "offset": sh_offset,
                        "size": sh_size
                    })
                    
                if e_shstrndx < len(sections):
                    shstr_sec = sections[e_shstrndx]
                    f.seek(shstr_sec["offset"])
                    shstrtab = f.read(shstr_sec["size"])
                else:
                    shstrtab = b""
                    
                dyn_sec = None
                dynstr_sec = None
                for sec in sections:
                    name = ""
                    idx = sec["name_idx"]
                    if idx < len(shstrtab):
                        end = shstrtab.find(b"\x00", idx)
                        name = shstrtab[idx:end].decode("ascii", errors="replace")
                    if name == ".dynamic" or sec["type"] == 6: # SHT_DYNAMIC
                        dyn_sec = sec
                    elif name == ".dynstr" or (sec["type"] == 3 and not dynstr_sec): # SHT_STRTAB
                        dynstr_sec = sec
                        
                if dyn_sec and dynstr_sec:
                    f.seek(dynstr_sec["offset"])
                    dynstr = f.read(dynstr_sec["size"])
                    
                    f.seek(dyn_sec["offset"])
                    dyn_data = f.read(dyn_sec["size"])
                    for i in range(0, len(dyn_data), 8):
                        d_tag, d_val = struct.unpack_from("<II", dyn_data, i)
                        if d_tag == 0: # DT_NULL
                            break
                        elif d_tag == 1: # DT_NEEDED
                            str_end = dynstr.find(b"\x00", d_val)
                            s = dynstr[d_val:str_end].decode("utf-8", errors="replace")
                            self.needed.append(s)
                        elif d_tag == 14: # DT_SONAME
                            str_end = dynstr.find(b"\x00", d_val)
                            self.soname = dynstr[d_val:str_end].decode("utf-8", errors="replace")
        except Exception:
            pass

# ==============================================================================
# Inventory Generation
# ==============================================================================
def inspect_applications():
    apps = []
    for sub in ["app", "priv-app"]:
        app_dir = os.path.join(SYSTEM_DIR, sub)
        if not os.path.isdir(app_dir):
            continue
        for fname in sorted(os.listdir(app_dir)):
            if not fname.endswith(".apk"):
                continue
            fpath = os.path.join(app_dir, fname)
            size = os.path.getsize(fpath)
            fhash = sha256_file(fpath)
            
            pkg_info = {
                "file": fname,
                "tier_location": sub,
                "path": f"system/{sub}/{fname}",
                "size": size,
                "size_mb": round(size / (1024 * 1024), 2),
                "sha256": fhash,
                "package_name": None,
                "shared_user_id": None,
                "version_code": None,
                "version_name": None,
                "permissions": [],
                "services": [],
                "receivers": [],
                "providers": [],
                "activities": [],
                "bundled_libs": []
            }
            
            try:
                with zipfile.ZipFile(fpath, "r") as z:
                    for zname in z.namelist():
                        if zname.startswith("lib/") and zname.endswith(".so"):
                            pkg_info["bundled_libs"].append(zname)
                            
                    if "AndroidManifest.xml" in z.namelist():
                        axml_data = z.read("AndroidManifest.xml")
                        axml = AXMLParser(axml_data)
                        pkg_info["package_name"] = axml.package_name
                        pkg_info["shared_user_id"] = axml.shared_user_id
                        pkg_info["version_code"] = axml.version_code
                        pkg_info["version_name"] = axml.version_name
                        pkg_info["permissions"] = axml.permissions
                        pkg_info["services"] = axml.services
                        pkg_info["receivers"] = axml.receivers
                        pkg_info["providers"] = axml.providers
                        pkg_info["activities"] = axml.activities
            except Exception as e:
                pkg_info["error"] = str(e)
                
            apps.append(pkg_info)
    return apps

def inspect_libraries():
    libs = []
    search_dirs = [
        ("system/lib", os.path.join(SYSTEM_DIR, "lib")),
        ("system/vendor/lib", os.path.join(SYSTEM_DIR, "vendor", "lib")),
        ("system/rtk_rootfs/usr/local/bin/solib", os.path.join(SYSTEM_DIR, "rtk_rootfs", "usr", "local", "bin", "solib"))
    ]
    
    for prefix, sdir in search_dirs:
        if not os.path.isdir(sdir):
            continue
        for root, _, files in os.walk(sdir):
            for f in sorted(files):
                if f.endswith(".so") or ".so." in f:
                    fpath = os.path.join(root, f)
                    rel = os.path.relpath(fpath, SYSTEM_DIR).replace("\\", "/")
                    size = os.path.getsize(fpath)
                    elf = ELFParser(fpath)
                    libs.append({
                        "name": f,
                        "relpath": f"system/{rel}",
                        "size": size,
                        "is_arm32": elf.is_arm32,
                        "soname": elf.soname,
                        "needed": elf.needed
                    })
    return libs

def inspect_rtk_subsystem():
    rtk_root = os.path.join(SYSTEM_DIR, "rtk_rootfs")
    if not os.path.isdir(rtk_root):
        return {}
        
    modules = []
    daemons = []
    ginga_files = []
    
    for root, _, files in os.walk(rtk_root):
        for f in files:
            fpath = os.path.join(root, f)
            rel = os.path.relpath(fpath, rtk_root).replace("\\", "/")
            size = os.path.getsize(fpath)
            if f.endswith(".ko"):
                modules.append({"module": f, "relpath": rel, "size": size})
            elif rel.startswith("usr/local/bin") and not f.endswith(".so") and not f.endswith(".isdb"):
                daemons.append({"name": f, "relpath": rel, "size": size})
            elif "EITV" in rel or "ginga" in rel.lower():
                ginga_files.append({"file": f, "relpath": rel, "size": size})
                
    return {
        "kernel_modules": modules,
        "daemons": daemons,
        "ginga_components": ginga_files
    }

def inspect_binaries():
    bins = []
    for sub in ["bin", "xbin"]:
        bdir = os.path.join(SYSTEM_DIR, sub)
        if not os.path.isdir(bdir):
            continue
        for f in sorted(os.listdir(bdir)):
            fpath = os.path.join(bdir, f)
            if f.endswith(".symlink"):
                with open(fpath, "r", encoding="utf-8") as sfile:
                    target = sfile.read().strip()
                bins.append({
                    "name": f[:-8],
                    "path": f"system/{sub}/{f[:-8]}",
                    "type": "symlink",
                    "target": target
                })
            elif os.path.isfile(fpath):
                size = os.path.getsize(fpath)
                with open(fpath, "rb") as bf:
                    magic = bf.read(4)
                btype = "elf" if magic == b"\x7fELF" else "script" if magic[:2] == b"#!" else "binary"
                bins.append({
                    "name": f,
                    "path": f"system/{sub}/{f}",
                    "type": btype,
                    "size": size
                })
    return bins

def inspect_permissions():
    perms_dir = os.path.join(SYSTEM_DIR, "etc", "permissions")
    perms = []
    if os.path.isdir(perms_dir):
        for f in sorted(os.listdir(perms_dir)):
            if f.endswith(".xml"):
                perms.append(f)
    return perms

def main():
    t0 = time.time()
    print("=== PhilcoOS System Inventory Engine ===")
    if not os.path.isdir(SYSTEM_DIR):
        print(f"[!] Error: Baseline system directory not found at {SYSTEM_DIR}")
        sys.exit(1)
        
    print("[*] Inspecting system applications...")
    apps = inspect_applications()
    print(f"  Found {len(apps)} total APKs.")
    
    print("[*] Inspecting shared native libraries...")
    libs = inspect_libraries()
    print(f"  Found {len(libs)} shared libraries.")
    
    print("[*] Inspecting Realtek subsystem...")
    rtk = inspect_rtk_subsystem()
    print(f"  Found {len(rtk.get('kernel_modules', []))} kernel modules, {len(rtk.get('daemons', []))} daemons.")
    
    print("[*] Inspecting system binaries and scripts...")
    bins = inspect_binaries()
    print(f"  Found {len(bins)} binaries and links.")
    
    perms = inspect_permissions()
    
    inventory = {
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "total_apks": len(apps),
        "total_libs": len(libs),
        "total_binaries": len(bins),
        "applications": apps,
        "libraries": libs,
        "rtk_subsystem": rtk,
        "binaries": bins,
        "permission_configs": perms
    }
    
    os.makedirs(os.path.dirname(OUTPUT_JSON), exist_ok=True)
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(inventory, f, indent=2)
    print(f"[+] Detailed JSON inventory saved to: {OUTPUT_JSON}")
    
    # Generate Markdown Report
    print("[*] Generating Markdown manifest...")
    with open(OUTPUT_MD, "w", encoding="utf-8") as f:
        f.write("# PhilcoOS — Comprehensive System Manifest\n\n")
        f.write(f"**Generated:** {inventory['timestamp']}  \n")
        f.write(f"**Baseline Location:** `C:\\PhilcoOS\\extracted\\factory_baseline\\system`  \n")
        f.write(f"**Total Applications:** {len(apps)} ({sum(a['size'] for a in apps) / (1024*1024):.2f} MB total)  \n")
        f.write(f"**Total Shared Libraries:** {len(libs)}  \n")
        f.write(f"**Total Binaries / Scripts:** {len(bins)}  \n\n")
        f.write("---\n\n")
        
        f.write("## 1. System Applications Inventory\n\n")
        f.write("| File Name | Location | Package Name | Shared UID | Size (MB) | Services | Receivers | Providers | Bundled Libs |\n")
        f.write("| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n")
        for a in apps:
            f.write(f"| `{a['file']}` | `{a['tier_location']}` | `{a['package_name'] or 'Unknown'}` | `{a['shared_user_id'] or '-'}` | {a['size_mb']} | {len(a['services'])} | {len(a['receivers'])} | {len(a['providers'])} | {len(a['bundled_libs'])} |\n")
            
        f.write("\n---\n\n")
        f.write("## 2. Proprietary TV Subsystem & Drivers\n\n")
        f.write("### 2.1 Realtek Daemons & Middleware\n")
        for d in rtk.get("daemons", []):
            f.write(f"- `{d['relpath']}` ({d['size']:,} bytes)\n")
            
        f.write("\n### 2.2 Wi-Fi & Filesystem Kernel Modules\n")
        for m in rtk.get("kernel_modules", []):
            f.write(f"- `{m['relpath']}` ({m['size']:,} bytes)\n")
            
        f.write("\n---\n\n")
        f.write("## 3. Core Hardware Abstraction Layers (HAL) & Key Native Libraries\n\n")
        f.write("| Library | Location | SONAME | Needed Dependencies |\n")
        f.write("| :--- | :--- | :--- | :--- |\n")
        key_libs = [lib for lib in libs if "hw" in lib["relpath"] or "directvo" in lib["name"] or "rpc" in lib["name"] or "egl" in lib["relpath"].lower() or "omx" in lib["name"].lower() or "rtk" in lib["name"].lower()]
        for kl in key_libs[:40]:
            f.write(f"| `{kl['name']}` | `{kl['relpath']}` | `{kl['soname'] or '-'}` | `{', '.join(kl['needed']) or '-'}` |\n")
            
    print(f"[+] Markdown manifest saved to: {OUTPUT_MD}")
    print(f"[+] Inventory completed in {time.time() - t0:.2f} seconds.")

if __name__ == "__main__":
    main()
