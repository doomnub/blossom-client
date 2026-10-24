#!/usr/bin/env python3
"""Run the real release WebView and Rust IPC with no Vite server (Linux/Xvfb)."""
import base64,json,os,pathlib,subprocess,tempfile,time,urllib.request
root=pathlib.Path(__file__).resolve().parents[1]
binary=root/'launcher-tauri/src-tauri/target/release/blossom-launcher'
out=root/'build/ui-check';out.mkdir(parents=True,exist_ok=True)
def request(method,path,data=None):
    body=None if data is None else json.dumps(data).encode()
    req=urllib.request.Request('http://127.0.0.1:4444'+path,data=body,method=method,headers={'Content-Type':'application/json'})
    with urllib.request.urlopen(req,timeout=40) as response:result=json.load(response)
    value=result.get('value')
    if isinstance(value,dict) and 'error' in value:raise RuntimeError(value)
    return value
with tempfile.TemporaryDirectory(prefix='blossom-native-') as home:
    env=os.environ.copy();env['HOME']=home
    config=pathlib.Path(home)/'.blossom';config.mkdir()
    (config/'launcher.properties').write_text('offlineName=NativeCheck\ngame='+home+'/Minecraft\nmemory=8192\n',encoding='utf-8')
    driver=subprocess.Popen(['tauri-driver'],env=env)
    session=None
    try:
        for _ in range(100):
            try:request('GET','/status');break
            except Exception:time.sleep(.1)
        session=request('POST','/session',{'capabilities':{'alwaysMatch':{'browserName':'wry','tauri:options':{'application':str(binary)}}}})['sessionId']
        def js(script):return request('POST','/session/'+session+'/execute/sync',{'script':script,'args':[]})
        for _ in range(100):
            if js("return document.getElementById('offline-name')?.value")=='NativeCheck':break
            time.sleep(.1)
        assert js("return document.getElementById('offline-name')?.value")=='NativeCheck','Native frontend or Rust IPC did not load'
        assert ':1420' not in js('return location.href'),'Release still points to development server'
        assert js("return document.querySelector('h1').textContent")=='Welcome home.'
        (out/'native-play.png').write_bytes(base64.b64decode(request('GET','/session/'+session+'/screenshot')))
        js("document.querySelector('[data-tab=settings]').click()")
        assert js("return !document.getElementById('settings').hidden")
        result=request('POST','/session/'+session+'/execute/async',{'script':"const done=arguments[arguments.length-1];window.__TAURI_INTERNALS__.invoke('settings').then(done,e=>done({error:String(e)}));",'args':[]})
        assert result['memory']==8192 and result['offlineName']=='NativeCheck',result
        (out/'native-settings.png').write_bytes(base64.b64decode(request('GET','/session/'+session+'/screenshot')))
        print('Release native WebView loaded embedded frontend and real Rust IPC; migrated settings verified.')
    finally:
        if session:
            try:request('DELETE','/session/'+session)
            except Exception:pass
        driver.terminate()
        try:driver.wait(timeout=10)
        except subprocess.TimeoutExpired:driver.kill()
