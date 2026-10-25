#!/usr/bin/env python3
"""Pinned Java 8 bytecode build; JRE 17+ and Python 3.9+ suffice (no Gradle required)."""
import concurrent.futures, hashlib, json, os, pathlib, subprocess, sys, urllib.request, zipfile
ROOT = pathlib.Path(__file__).resolve().parents[1]
DEPS = ROOT / '.deps'
def fetch(url, target, digest, algorithm='sha256'):
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists() and hashlib.new(algorithm, target.read_bytes()).hexdigest() == digest:
        return
    with urllib.request.urlopen(url, timeout=60) as response:
        data = response.read()
    if hashlib.new(algorithm, data).hexdigest() != digest:
        raise RuntimeError('Checksum mismatch: ' + url)
    temp = target.with_suffix(target.suffix + '.tmp')
    temp.write_bytes(data); temp.replace(target)
def prepare():
    fetch('https://piston-meta.mojang.com/v1/packages/d546f1707a3f2b7d034eece5ea2e311eda875787/1.8.9.json', DEPS/'version.json', 'd546f1707a3f2b7d034eece5ea2e311eda875787', 'sha1')
    meta = json.loads((DEPS/'version.json').read_text())
    client=meta['downloads']['client']; fetch(client['url'], DEPS/'client.jar', client['sha1'], 'sha1')
    def library(lib):
        a=lib.get('downloads',{}).get('artifact')
        if a: fetch(a['url'], DEPS/'libraries'/a['path'], a['sha1'], 'sha1')
    with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:
        list(pool.map(library,meta['libraries']))
    fetch('https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar',DEPS/'asm.jar','8cadd43ac5eb6d09de05faecca38b917a040bb9139c7edeb4cc81c740b713281')
    fetch('https://repo.maven.apache.org/maven2/org/eclipse/jdt/ecj/3.33.0/ecj-3.33.0.jar',DEPS/'ecj.jar','f7686c4960cf70c2ebc5c500a73a8cfc04541b730c18f1c5c21329889b137f45')
    fetch('https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar',DEPS/'asm-commons.jar','9a579b54d292ad9be171d4313fd4739c635592c2b5ac3a459bbd1049cddec6a0')
def main():
    prepare()
    if '--prepare-only' in sys.argv: return
    import shutil
    classes=ROOT/'build/classes'; shutil.rmtree(classes,ignore_errors=True); classes.mkdir(parents=True)
    jars=[DEPS/'client.jar',DEPS/'asm.jar',DEPS/'asm-commons.jar']+sorted((DEPS/'libraries').rglob('*.jar'))
    cp=os.pathsep.join(map(str,jars))
    sources=sorted((ROOT/'src/main/java').rglob('*.java'))+sorted((ROOT/'src/test/java').rglob('*.java'))
    subprocess.run(['java','-jar',str(DEPS/'ecj.jar'),'-8','-encoding','UTF-8','-warn:none','-cp',cp,'-d',str(classes)]+list(map(str,sources)),check=True)
    subprocess.run(['java','-cp',str(classes)+os.pathsep+cp,'dev.blossom.RelocateIntegration',str(classes)],check=True)
    subprocess.run(['java','-ea','-cp',str(classes)+os.pathsep+cp,'dev.blossom.CoreTest'],check=True)
    subprocess.run(['java','-ea','-cp',str(classes)+os.pathsep+cp,'dev.blossom.TransformTest',str(DEPS/'client.jar')],check=True)
    version=(ROOT/'VERSION').read_text().strip()
    out=ROOT/('build/libs/blossom-client-'+version+'.jar'); out.parent.mkdir(parents=True,exist_ok=True)
    entries={}
    with zipfile.ZipFile(DEPS/'asm.jar') as asm:
        for name in asm.namelist():
            if name.startswith('org/objectweb/asm/') and not name.endswith('/'): entries[name]=asm.read(name)
    for file in classes.rglob('*.class'):
        name=file.relative_to(classes).as_posix()
        if name.startswith('dev/blossom/') and ('Test' in name or 'RelocateIntegration' in name): continue
        entries[name]=file.read_bytes()
    entries['blossom.png']=(ROOT/'assets/blossom.png').read_bytes()
    entries['META-INF/MANIFEST.MF']=b'Manifest-Version: 1.0\r\nPremain-Class: dev.blossom.agent.BlossomAgent\r\nMain-Class: dev.blossom.agent.InstallHelp\r\n\r\n'
    for name in ['LICENSE','LICENSES.md']: entries[name]=(ROOT/name).read_bytes()
    with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as jar:
        for name,data in sorted(entries.items()):
            info=zipfile.ZipInfo(name,(2026,1,1,0,0,0)); info.compress_type=zipfile.ZIP_DEFLATED
            jar.writestr(info,data)
    subprocess.run(['java','-Xverify:all','-javaagent:'+str(out),'-cp',str(classes)+os.pathsep+cp,'dev.blossom.LinkageTest'],check=True)
    build_launcher(out,version)
    print('Built',out,'SHA-256',hashlib.sha256(out.read_bytes()).hexdigest())
def build_launcher(agent,version):
    import shutil
    fetch('https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar',DEPS/'gson.jar','57928d6e5a6edeb2abd3770a8f95ba44dce45f3b23b7a9dc2b309c581552a78b')
    classes=ROOT/'build/launcher-classes';shutil.rmtree(classes,ignore_errors=True);classes.mkdir(parents=True)
    sources=sorted((ROOT/'launcher/src').rglob('*.java'))
    subprocess.run(['java','-jar',str(DEPS/'ecj.jar'),'-8','-encoding','UTF-8','-warn:none','-cp',str(DEPS/'gson.jar'),'-d',str(classes)]+list(map(str,sources)),check=True)
    shutil.copyfile(ROOT/'launcher/src/main/resources/runtimes.json',classes/'runtimes.json')
    if (classes/'dev/blossom/launcher/LauncherTest.class').exists():
        subprocess.run(['java','-ea','-cp',str(classes)+os.pathsep+str(DEPS/'gson.jar'),'dev.blossom.launcher.LauncherTest'],check=True)
    entries={}
    with zipfile.ZipFile(DEPS/'gson.jar') as gson:
        for name in gson.namelist():
            if not name.endswith('/') and (name.startswith('com/google/gson/') or name.startswith('META-INF/LICENSE')):entries[name]=gson.read(name)
    for file in classes.rglob('*.class'):
        if 'Test' not in file.name:entries[file.relative_to(classes).as_posix()]=file.read_bytes()
    entries['runtimes.json']=(classes/'runtimes.json').read_bytes()
    entries['licenses/Apache-2.0.txt']=(ROOT/'licenses/Apache-2.0.txt').read_bytes()
    entries['client/blossom-agent.jar']=agent.read_bytes()
    entries['build.properties']=('version='+version+'\nagent.sha256='+hashlib.sha256(agent.read_bytes()).hexdigest()+'\n').encode()
    entries['blossom.png']=(ROOT/'assets/blossom.png').read_bytes()
    entries['META-INF/MANIFEST.MF']=b'Manifest-Version: 1.0\r\nMain-Class: dev.blossom.launcher.Launcher\r\n\r\n'
    for name in ['LICENSE','LICENSES.md']:entries[name]=(ROOT/name).read_bytes()
    output=ROOT/('build/libs/blossom-launcher-'+version+'.jar')
    with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as jar:
        for name,data in sorted(entries.items()):
            info=zipfile.ZipInfo(name,(2026,1,1,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;jar.writestr(info,data)
    print('Built',output,'SHA-256',hashlib.sha256(output.read_bytes()).hexdigest())
if __name__=='__main__': main()
