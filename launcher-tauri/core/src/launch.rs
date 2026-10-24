use crate::*;
use crate::{auth::Session,config::Config,install::Installed};
use std::process::{Command,Stdio};
use std::io::{BufRead,BufReader};
pub fn arguments(java:&Path,agent:&Path,game:&Path,installed:&Installed,session:&Session,c:&Config)->Result<Vec<String>>{
    c.validate()?;let mut cp=installed.classpath.clone();cp.push(agent.to_owned());let cp=std::env::join_paths(cp)?.into_string().map_err(|_|"Classpath is not valid Unicode")?;
    let mut result=vec![java.to_string_lossy().into(),"-Xms512M".into(),format!("-Xmx{}M",c.memory),format!("-Djava.library.path={}",installed.natives.display()),format!("-Dlog4j.configurationFile={}",installed.logging.display()),format!("-javaagent:{}",agent.display()),"-cp".into(),cp,"dev.blossom.agent.GameBootstrap".into()];
    for (key,value)in [("--username",session.name.clone()),("--version","1.8.9".into()),("--gameDir",game.to_string_lossy().into()),("--assetsDir",installed.assets.to_string_lossy().into()),("--assetIndex",installed.index.clone()),("--uuid",session.uuid.clone()),("--accessToken","@BLOSSOM_SESSION@".into()),("--userProperties","{}".into()),("--userType",if session.offline{"legacy"}else{"msa"}.into()),("--width",c.width.to_string()),("--height",c.height.to_string())]{result.push(key.into());result.push(value);}Ok(result)
}
pub fn run(t:&Task,args:&[String],game:&Path,session:Session)->Result<i32>{
    t.check()?;let mut child=Command::new(&args[0]).args(&args[1..]).current_dir(game).stdin(Stdio::piped()).stdout(Stdio::piped()).stderr(Stdio::piped()).spawn()?;
    // Java DataInputStream.readUTF uses modified UTF-8; OAuth tokens and offline "0" are ASCII.
    if !session.token.is_ascii()||session.token.is_empty()||session.token.len()>16000{let _=child.kill();let _=child.wait();return Err("Invalid game session transport".into());}
    let mut input=child.stdin.take().unwrap();let send=input.write_all(&(session.token.len() as u16).to_be_bytes()).and_then(|_|input.write_all(session.token.as_bytes()));drop(input);if let Err(e)=send{let _=child.kill();let _=child.wait();return Err(e.into());}
    (t.notify)("running","Minecraft is running");
    let stdout=child.stdout.take().unwrap();let stderr=child.stderr.take().unwrap();
    let reader=|stream:Box<dyn Read+Send>,task:Task,secret:String|std::thread::spawn(move||{
        let mut input=BufReader::new(stream);let mut bytes=Vec::new();loop{bytes.clear();let mut limited=(&mut input).take(16384);match limited.read_until(b'\n',&mut bytes){Ok(0)|Err(_)=>break,Ok(_)=>{let line=String::from_utf8_lossy(&bytes);let text=if secret=="0"{line.into_owned()}else{line.replace(&secret,"[session redacted]")};(task.notify)("log",&text);}}}
    });
    let a=reader(Box::new(stdout),t.clone(),session.token.clone());let b=reader(Box::new(stderr),t.clone(),session.token);let result=child.wait()?;let _=a.join();let _=b.join();let code=result.code().unwrap_or(-1);t.status(&format!("Minecraft exited with code {code}"));Ok(code)
}
#[cfg(test)]mod tests{use super::*;#[test]fn launch_tokens_not_in_arguments(){let d=tempfile::tempdir().unwrap();let c=Config::load(d.path()).unwrap();let i=Installed{classpath:vec![d.path().join("client.jar")],assets:d.path().join("assets"),natives:d.path().join("native"),logging:d.path().join("log.xml"),index:"1.8".into()};let s=Session{name:"Jade".into(),uuid:"123".into(),token:"super-secret".into(),offline:false};let a=arguments(Path::new("java"),Path::new("agent.jar"),d.path(),&i,&s,&c).unwrap();assert!(!a.iter().any(|a|a.contains("super-secret")));assert!(a.contains(&"@BLOSSOM_SESSION@".into()));assert!(a.iter().any(|a|a.starts_with("-javaagent:")));}}
