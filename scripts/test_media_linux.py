"""Test GLib variant decoding without a desktop session or media application."""
import importlib.util
from pathlib import Path
spec=importlib.util.spec_from_file_location('media',Path(__file__).resolve().parents[1]/'src/main/resources/media/linux.py')
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
for _ in range(100):
    value=m.parse(None,b"({'Metadata': <{'xesam:title': <'Test song'>, 'xesam:artist': <['Artist']>, 'mpris:length': <int64 120000000>}>, 'PlaybackStatus': <'Playing'>, 'Position': <int64 3000000>},)",None,None,None)
    assert value
    try:
        data=m.unpack(value)[0]
        assert data['Metadata']['xesam:title']=='Test song'
        assert data['Metadata']['xesam:artist']==['Artist']
        assert data['Position']==3000000
    finally:m.unref(value)
print('MPRIS variant metadata, arrays and int64 decoding passed')
