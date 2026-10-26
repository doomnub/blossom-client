//! Real installer verification; accepts an isolated test root, never signs in.
use blossom_launcher_core::{install, Task};
fn main() -> blossom_launcher_core::Result<()> {
    let root = std::path::absolute(std::env::args().nth(1).ok_or("Pass test root")?)?;
    let task = Task::new(|_, s| println!("{s}"))?;
    let java = install::java(&task, &root)?;
    install::verify_java(&java)?;
    let installed = install::install(&task, &root, &root.join("game"))?;
    std::fs::write(root.join("startup.json"),serde_json::to_vec_pretty(&serde_json::json!({
        "java":java,"classpath":installed.classpath,"logging":installed.logging
    }))?)?;
    println!(
        "Verified {} classpath entries; Java {}",
        installed.classpath.len(),
        java.display()
    );
    Ok(())
}
