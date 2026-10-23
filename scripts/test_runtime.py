#!/usr/bin/env python3
"""Integration check on the host OS: actual managed Java, offline reuse and tests."""
import json,os,pathlib,subprocess,sys
root=pathlib.Path(__file__).resolve().parents[1]
version=(root/'VERSION').read_text().strip()
jar=root/f'build/libs/blossom-launcher-{version}.jar'
install=root/'build/runtime-smoke'
subprocess.run(['java','-jar',str(jar),'--install-java',str(install)],check=True)
spec=json.loads((root/'launcher/src/main/resources/runtimes.json').read_text())['windows' if os.name=='nt' else 'linux']
java=install/'runtimes'/spec['folder']/spec['executable']
env=os.environ.copy();env['HTTPS_PROXY']='http://127.0.0.1:1'
subprocess.run([str(java),'-jar',str(jar),'--install-java',str(install)],env=env,check=True)
cp=os.pathsep.join([str(root/'build/launcher-classes'),str(root/'.deps/gson.jar')])
subprocess.run([str(java),'-ea','-cp',cp,'dev.blossom.launcher.LauncherTest'],check=True)
print('Managed Java 8 host installation and offline reuse passed.')
