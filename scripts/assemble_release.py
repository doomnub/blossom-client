#!/usr/bin/env python3
"""Collect verified native packages and label the retained Swing fallback explicitly."""
import hashlib
import pathlib
import shutil
import zipfile
root = pathlib.Path(__file__).resolve().parents[1]
version = (root / 'VERSION').read_text().strip()
out = root / 'build/release'
legacy = out / f'blossom-{version}.zip'
legacy.rename(out / f'blossom-{version}-legacy.zip')
for platform in ('linux', 'windows'):
    name = f'blossom-{version}-{platform}-x64.zip'
    matches = list((root / 'build/native-artifacts').rglob(name))
    if len(matches) != 1:
        raise SystemExit(f'Expected exactly one {name}, found {len(matches)}')
    archive = matches[0]
    expected = archive.with_name(name + '.sha256').read_text().split()[0]
    if hashlib.sha256(archive.read_bytes()).hexdigest() != expected:
        raise SystemExit(f'Checksum mismatch: {name}')
    with zipfile.ZipFile(archive) as z:
        if z.testzip() is not None:
            raise SystemExit(f'Corrupt ZIP: {name}')
        for line in z.read('checksums/SHA256SUMS.txt').decode().splitlines():
            digest, member = line.split('  ', 1)
            if hashlib.sha256(z.read(member)).hexdigest() != digest:
                raise SystemExit(f'Internal checksum mismatch: {member}')
    shutil.copy2(archive, out / name)
(out / 'SHA256SUMS.txt').write_text(''.join(
    hashlib.sha256(p.read_bytes()).hexdigest() + '  ' + p.name + '\n'
    for p in sorted(out.iterdir()) if p.is_file() and p.name != 'SHA256SUMS.txt'
))
print('Verified and assembled Windows, Linux and legacy packages.')
