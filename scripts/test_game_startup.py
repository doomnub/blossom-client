#!/usr/bin/env python3
"""Execute icon generation and the installed logging configuration on real Java 8."""
import argparse,json,os,pathlib,subprocess,sys
import build
ROOT=build.ROOT

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--manifest',type=pathlib.Path);parser.add_argument('--java',type=pathlib.Path);args=parser.parse_args()
    version=(ROOT/'VERSION').read_text().strip();agent=ROOT/f'build/libs/blossom-client-{version}.jar'
    legacy=ROOT/f'build/libs/blossom-launcher-{version}.jar'
    if args.manifest:
        manifest=json.loads(args.manifest.read_text());java=manifest['java'];libraries=list(map(pathlib.Path,manifest['classpath']));config=manifest['logging'];label='native'
    else:
        if not args.java:parser.error('Pass --java or --manifest')
        java=str(args.java.resolve());config=None;label='legacy';libraries=[]
        for name,digest in [('log4j-api','6c80ea65e41f7858f7e15cb390c98c36167c7b3a'),('log4j-core','c194a13e18b0a36014e867d2d323b989bf212151')]:
            path=ROOT/f'.deps/launch-libraries/{name}-2.25.5.jar';build.fetch(f'https://repo.maven.apache.org/maven2/org/apache/logging/log4j/{name}/2.25.5/{name}-2.25.5.jar',path,digest,'sha1');libraries.append(path)
        libraries+=sorted((ROOT/'.deps/libraries/org/lwjgl/lwjgl').rglob('lwjgl-*.jar'))
    classes=ROOT/'build/startup-test-classes';classes.mkdir(parents=True,exist_ok=True)
    subprocess.run(['java','-jar',str(ROOT/'.deps/ecj.jar'),'--release','8','-warn:none','-cp',os.pathsep.join(map(str,libraries)),'-d',str(classes),str(ROOT/'src/test/runtime/StartupSmoke.java')],check=True)
    work=ROOT/'build/startup-smoke'/label;work.mkdir(parents=True,exist_ok=True)
    latest=work/'logs/latest.log'
    if latest.exists():latest.unlink()
    command=[java,'-Xverify:all','-Djava.awt.headless=true']
    if config:command+=['-Dlog4j.configurationFile='+config]
    command+=['-cp',os.pathsep.join(map(str,[classes,agent,legacy]+libraries)),'StartupSmoke']
    if not config:command+=[str(work/'cache')]
    result=subprocess.run(command,cwd=work,text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    print(result.stdout);print(result.stderr,file=sys.stderr)
    result.check_returncode()
    content=latest.read_text()
    for marker in ['blossom-startup-smoke','blossom-stack-smoke','expected probe exception']:
        assert marker in content and marker in result.stdout,marker
    assert 'NoClassDefFoundError' not in result.stderr and 'ERROR' not in result.stderr,result.stderr
    print(label+': actual Java 8 window-icon buffers, logging plugins, console, file and stack traces passed.')
if __name__=='__main__':main()
