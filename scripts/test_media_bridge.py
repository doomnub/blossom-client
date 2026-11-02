"""Start the real OS adapter and require a JSON snapshot, even with no player open."""
import json
import os
import pathlib
import queue
import subprocess
import threading
root=pathlib.Path(__file__).resolve().parents[1]
command=['powershell.exe','-NoLogo','-NoProfile','-NonInteractive','-ExecutionPolicy','Bypass','-File',str(root/'src/main/resources/media/windows.ps1')] if os.name=='nt' else ['python3','-u',str(root/'src/main/resources/media/linux.py')]
process=subprocess.Popen(command,stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True,encoding='utf-8')
lines=queue.Queue()
threading.Thread(target=lambda:lines.put(process.stdout.readline()),daemon=True).start()
try:
    line=lines.get(timeout=25)
    if not line: raise AssertionError('Media adapter exited: '+process.stderr.read(4096))
    assert isinstance(json.loads(line.lstrip('\ufeff')),dict)
    print('OS media adapter emitted a JSON snapshot')
finally:
    process.kill();process.wait(timeout=5)
