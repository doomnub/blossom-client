#!/usr/bin/env python3
"""Linux bootstrap: install a verified app-private runtime before Java is available."""
import hashlib,json,os,pathlib,platform,shutil,subprocess,sys,tempfile,urllib.request,uuid,zipfile
HERE=pathlib.Path(__file__).resolve().parent

def verify(executable):
    try:
        p=subprocess.run([str(executable),'-XshowSettings:properties','-version'],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,timeout=10)
        text=p.stdout.decode(errors='replace')
        return p.returncode==0 and '"1.8.' in text and 'sun.arch.data.model = 64' in text
    except (OSError,subprocess.TimeoutExpired):return False

def install(root,spec):
    runtimes=root/'runtimes';target=runtimes/spec['folder'];java=target/spec['executable'];marker=target/'.blossom-ready'
    if marker.is_file() and marker.read_text().strip()==spec['sha256'] and verify(java):return java
    print('Installing '+spec['name']+' to '+str(target),flush=True)
    runtimes.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='bootstrap-',dir=runtimes) as temporary:
        stage=pathlib.Path(temporary);archive=stage/'runtime.zip';digest=hashlib.sha256();count=0
        with urllib.request.urlopen(spec['url'],timeout=30) as response,archive.open('wb') as out:
            if not response.url.startswith('https://cdn.azul.com/zulu/bin/'):raise RuntimeError('Unexpected download redirect')
            while True:
                data=response.read(65536)
                if not data:break
                count+=len(data)
                if count>256*1024*1024:raise RuntimeError('Runtime download exceeds size limit')
                digest.update(data);out.write(data)
        if digest.hexdigest()!=spec['sha256']:raise RuntimeError('Java download checksum mismatch')
        with zipfile.ZipFile(archive) as z:
            entries=z.infolist();total=0;seen=set()
            if len(entries)>10000:raise RuntimeError('Too many runtime files')
            for entry in entries:
                name=entry.filename;parts=pathlib.PurePosixPath(name).parts
                if not name.startswith(spec['folder']+'/') or '..' in parts or '\\' in name or ':' in name:raise RuntimeError('Invalid runtime archive path')
                normalized=pathlib.PurePosixPath(name)
                if normalized in seen:raise RuntimeError('Duplicate archive path')
                seen.add(normalized);total+=entry.file_size
                if total>1024*1024*1024:raise RuntimeError('Runtime archive too large')
            z.extractall(stage)
        installed=stage/spec['folder']
        for file in (installed/'bin').iterdir():file.chmod(file.stat().st_mode|0o100)
        if not verify(installed/spec['executable']):raise RuntimeError('Downloaded Java failed 64-bit Java 8 verification')
        (installed/'.blossom-ready').write_text(spec['sha256']+'\n')
        backup=None
        if target.exists():
            backup=runtimes/(spec['folder']+'-previous-'+str(uuid.uuid4()));target.rename(backup)
        try:installed.rename(target)
        except Exception:
            if backup:backup.rename(target)
            raise
        if backup:shutil.rmtree(backup,ignore_errors=True)
    return java

def main():
    if platform.system()!='Linux' or platform.machine().lower() not in ('x86_64','amd64'):raise RuntimeError('This launcher supports Linux x86_64; Windows users should run Start-Blossom.bat.')
    spec=json.loads((HERE/'runtimes.json').read_text())['linux']
    root=pathlib.Path.home()/'.blossom'
    java=install(root,spec)
    print('Using '+spec['name']+': '+str(java),flush=True)
    if len(sys.argv)==2 and sys.argv[1]=='--install-only':return
    version=(HERE/'VERSION').read_text().strip()
    os.execv(str(java),[str(java),'-jar',str(HERE/('blossom-launcher-'+version+'.jar'))])

if __name__=='__main__':
    try:main()
    except Exception as e:sys.exit('Blossom startup failed: '+str(e))
