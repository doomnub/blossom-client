"""Exercise native packager for both platforms without requiring native compiler outputs."""
import hashlib
import pathlib
import runpy
import sys
import tempfile
import types
import zipfile
source=pathlib.Path(__file__).with_name('package_native.py').read_text()
for platform in ('linux','windows'):
    with tempfile.TemporaryDirectory() as folder:
        root=pathlib.Path(folder)
        for p,data in {'VERSION':'test','docs/NATIVE-LAUNCHER.md':'readme','LICENSE':'license','LICENSES.md':'licenses','licenses/skin-preview.txt':'skin','assets/blossom.svg':'<svg/>'}.items():
            path=root/p;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(data)
        exe='blossom-launcher'+('.exe' if platform=='windows' else '')
        binary=root/'launcher-tauri/src-tauri/target/release'/exe;binary.parent.mkdir(parents=True);binary.write_bytes(b'test executable')
        # Inject only the packager's platform name; do not mutate pathlib's host OS.
        patched=source.replace("platform='windows' if os.name=='nt' else 'linux'",f"platform='{platform}'").replace("os.name=='nt'","platform=='windows'")
        exec(compile(patched,'package_native.py','exec'),{'__file__':str(root/'scripts/package_native.py')})
        with zipfile.ZipFile(root/f'build/native-release/blossom-test-{platform}-x64.zip') as archive:
            names=archive.namelist()
            assert ('Start-Blossom.bat' in names)==(platform=='windows')
            assert ('start-blossom.sh' in names)==(platform=='linux')
            for line in archive.read('checksums/SHA256SUMS.txt').decode().splitlines():
                digest,name=line.split('  ',1);assert hashlib.sha256(archive.read(name)).hexdigest()==digest
print('Linux/Windows package launchers and checksums passed')
