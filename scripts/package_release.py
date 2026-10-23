#!/usr/bin/env python3
"""Package already-tested artifacts. Never includes Mojang binaries or credentials."""
import hashlib,pathlib,zipfile,shutil
root=pathlib.Path(__file__).resolve().parents[1]
version=(root/'VERSION').read_text().strip()
out=root/'build/release';shutil.rmtree(out,ignore_errors=True);out.mkdir(parents=True)
files={}
for kind in ['client','launcher']:
    name=f'blossom-{kind}-{version}.jar';data=(root/'build/libs'/name).read_bytes();files[name]=data;(out/name).write_bytes(data)
files['Start-Blossom.bat']=b'@echo off\r\ncd /d "%~dp0"\r\npowershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-Blossom.ps1"\r\nif errorlevel 1 pause\r\n'
files['start-blossom.sh']=b'#!/bin/sh\nset -eu\ncd -- "$(dirname -- "$0")"\nexec python3 ./start.py "$@"\n'
for name in ['start.py','Start-Blossom.ps1']:files[name]=(root/'scripts/bootstrap'/name).read_bytes()
files['runtimes.json']=(root/'launcher/src/main/resources/runtimes.json').read_bytes()
files['VERSION']=(root/'VERSION').read_bytes()
for source,target in [('docs/LAUNCHER.md','README.md'),('LICENSE','LICENSE'),('LICENSES.md','LICENSES.md'),('licenses/Apache-2.0.txt','licenses/Apache-2.0.txt')]:files[target]=(root/source).read_bytes()
files['SHA256SUMS.txt']=''.join(hashlib.sha256(data).hexdigest()+'  '+name+'\n' for name,data in sorted(files.items())).encode()
archive=out/f'blossom-{version}.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
    for name,data in sorted(files.items()):
        info=zipfile.ZipInfo(name,(2026,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=(0o100755 if name.endswith('.sh') else 0o100644)<<16;z.writestr(info,data)
(out/'SHA256SUMS.txt').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted(out.iterdir()) if p.is_file()))
print('Packaged',archive)
