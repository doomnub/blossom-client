//! Real installer verification; accepts an isolated test root, never signs in.
use blossom_launcher_core::{Task,install};
fn main()->blossom_launcher_core::Result<()>{let root=std::path::absolute(std::env::args().nth(1).ok_or("Pass test root")?)?;let task=Task::new(|_,s|println!("{s}"))?;let java=install::java(&task,&root)?;install::verify_java(&java)?;let installed=install::install(&task,&root,&root.join("game"))?;println!("Verified {} classpath entries; Java {}",installed.classpath.len(),java.display());Ok(())}
