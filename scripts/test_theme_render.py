#!/usr/bin/env python3
"""Real LWJGL 2 theme rendering using the CI installer's isolated native libraries."""
import json,os,pathlib,subprocess
root=pathlib.Path(__file__).resolve().parents[1]
manifest=json.loads((root/'build/native-smoke/startup.json').read_text())
classes=root/'build/theme-test-classes';classes.mkdir(exist_ok=True)
out=root/'build/ui-check';out.mkdir(exist_ok=True)
cp=os.pathsep.join([str(root/'build/classes')]+manifest['classpath'])
subprocess.run(['java','-jar',str(root/'.deps/ecj.jar'),'--release','8','-warn:none','-cp',cp,'-d',str(classes),str(root/'src/test/runtime/ThemeRenderSmoke.java')],check=True)
subprocess.run([manifest['java'],'-Djava.library.path='+str(root/'build/native-smoke/game/.blossom-natives'),'-cp',str(classes)+os.pathsep+cp,'ThemeRenderSmoke',str(out)],check=True)
