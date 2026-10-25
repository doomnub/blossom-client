#!/usr/bin/env python3
"""Package the tested native binary; legacy Swing packaging remains separately available."""
import hashlib,os,pathlib,zipfile
root=pathlib.Path(__file__).resolve().parents[1]
version=(root/'VERSION').read_text().strip()
platform='windows' if os.name=='nt' else 'linux'
exe='blossom-launcher'+('.exe' if os.name=='nt' else '')
binary=root/'launcher-tauri/src-tauri/target/release'/exe
if not binary.is_file():raise SystemExit('Build the native launcher first')
files={f'app/{exe}':binary.read_bytes(),'README.md':(root/'docs/NATIVE-LAUNCHER.md').read_bytes(),'licenses/LICENSE':(root/'LICENSE').read_bytes(),'licenses/LICENSES.md':(root/'LICENSES.md').read_bytes()}
files['licenses/skin-preview.txt']=(root/'licenses/skin-preview.txt').read_bytes()
files['blossom.svg']=(root/'assets/blossom.svg').read_bytes()
files['start-blossom.sh']=b'#!/bin/sh\nset -eu\ncd -- "$(dirname -- "$0")"\nexec ./app/blossom-launcher "$@"\n'
files['Start-Blossom.bat']=b'@echo off\r\ncd /d "%~dp0"\r\nstart "Blossom" "%~dp0app\\blossom-launcher.exe" %*\r\n'
files['checksums/SHA256SUMS.txt']=''.join(hashlib.sha256(data).hexdigest()+'  '+name+'\n' for name,data in sorted(files.items())).encode()
out=root/'build/native-release';out.mkdir(parents=True,exist_ok=True)
archive=out/f'blossom-{version}-{platform}-x64.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
    for name,data in files.items():
        info=zipfile.ZipInfo(name,(2026,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o100755 if name.endswith('.sh') or name==f'app/{exe}' else 0o100644)<<16;z.writestr(info,data)
(out/f'{archive.name}.sha256').write_text(hashlib.sha256(archive.read_bytes()).hexdigest()+'  '+archive.name+'\n')
print('Packaged',archive)
