#!/usr/bin/env python3
"""Read-only MPRIS via the desktop's GLib/GIO D-Bus API. No pip dependencies."""
import ctypes as C
import ctypes.util
import json
import time

g = C.CDLL(ctypes.util.find_library('glib-2.0'))
io = C.CDLL(ctypes.util.find_library('gio-2.0'))
def bind(lib, name, restype, *args):
    f = getattr(lib, name); f.restype = restype; f.argtypes = args; return f
P = C.c_void_p
unref = bind(g, 'g_variant_unref', None, P)
child = bind(g, 'g_variant_get_child_value', P, P, C.c_size_t)
children = bind(g, 'g_variant_n_children', C.c_size_t, P)
kind = bind(g, 'g_variant_classify', C.c_int, P)
string = bind(g, 'g_variant_get_string', C.c_char_p, P, P)
variant = bind(g, 'g_variant_get_variant', P, P)
parse = bind(g, 'g_variant_parse', P, P, C.c_char_p, P, P, P)
free_error = bind(g, 'g_error_free', None, P)
call_sync = bind(io, 'g_dbus_connection_call_sync', P, P, C.c_char_p, C.c_char_p, C.c_char_p, C.c_char_p, P, P, C.c_int, C.c_int, P, P)
bus_get = bind(io, 'g_bus_get_sync', P, C.c_int, P, P)
number = {c: bind(g, 'g_variant_get_'+name, typ, P) for c,name,typ in [('x','int64',C.c_int64),('t','uint64',C.c_uint64),('i','int32',C.c_int32),('u','uint32',C.c_uint32),('d','double',C.c_double),('b','boolean',C.c_int)]}
def unpack(v):
    k = chr(kind(v))
    if k in 'sog': return string(v, None).decode('utf-8', 'replace')
    if k in number: return number[k](v)
    if k == 'v':
        nested = variant(v)
        try: return unpack(nested)
        finally: unref(nested)
    if k in 'a({':
        result=[]
        for i in range(min(children(v), 4096)):
            c=child(v,i)
            try: result.append(unpack(c))
            finally: unref(c)
        if k=='a' and result and isinstance(result[0],list) and len(result[0])==2 and isinstance(result[0][0],str): return dict(result)
        return result
    return None
bus=None
def call(dest, path, interface, method, args=None):
    p=parse(None,args.encode(),None,None,None) if args else None
    error=P()
    try: result=call_sync(bus,dest.encode(),path.encode(),interface.encode(),method.encode(),p,None,0,1000,None,C.byref(error))
    finally:
        if p: unref(p)
    if not result:
        if error.value: free_error(error)
        raise RuntimeError('D-Bus call unavailable')
    try: return unpack(result)[0]
    finally: unref(result)
def main():
    global bus
    bus=bus_get(2,None,None)
    if not bus: raise RuntimeError('Session D-Bus unavailable')
    while True:
        out={}
        try:
            names=call('org.freedesktop.DBus','/org/freedesktop/DBus','org.freedesktop.DBus','ListNames')
            candidates=[]
            for name in sorted(n for n in names if n.startswith('org.mpris.MediaPlayer2.'))[:16]:
                try:
                    p=call(name,'/org/mpris/MediaPlayer2','org.freedesktop.DBus.Properties','GetAll',"('org.mpris.MediaPlayer2.Player',)")
                    if isinstance(p,dict) and p.get('PlaybackStatus') in ('Playing','Paused'): candidates.append((name,p))
                except Exception: pass
            candidates.sort(key=lambda c:c[1].get('PlaybackStatus')!='Playing')
            if candidates:
                name,p=candidates[0]; m=p.get('Metadata',{})
                if not isinstance(m,dict): m={}
                pos=call(name,'/org/mpris/MediaPlayer2','org.freedesktop.DBus.Properties','Get',"('org.mpris.MediaPlayer2.Player', 'Position')")
                out=dict(title=str(m.get('xesam:title',''))[:512],artist=', '.join(m.get('xesam:artist',[]))[:512],art=str(m.get('mpris:artUrl',''))[:4096],position=max(0,pos/1e6),duration=max(0,m.get('mpris:length',0)/1e6),playing=p.get('PlaybackStatus')=='Playing',rate=p.get('Rate',1))
        except Exception: pass
        print(json.dumps(out,ensure_ascii=True),flush=True)
        time.sleep(1)

if __name__ == '__main__':
    main()
