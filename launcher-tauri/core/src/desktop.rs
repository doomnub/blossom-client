//! Portable Linux desktop integration, matching the GTK/Wayland application ID.
use crate::*;
#[cfg(target_os = "linux")]
pub fn install() -> Result<()> {
    let data = std::env::var_os("XDG_DATA_HOME").map(PathBuf::from)
        .filter(|p| p.is_absolute())
        .unwrap_or_else(|| PathBuf::from(std::env::var_os("HOME").unwrap_or_default()).join(".local/share"));
    let applications=data.join("applications");
    let icons=data.join("icons/hicolor/scalable/apps");
    std::fs::create_dir_all(&applications)?;std::fs::create_dir_all(&icons)?;
    std::fs::write(icons.join("dev.blossom.launcher.svg"),include_bytes!("../../../assets/blossom.svg"))?;
    let exe=std::env::current_exe()?;
    let desktop=format!("[Desktop Entry]\nType=Application\nName=Blossom\nComment=Your Minecraft. In full bloom.\nExec={}\nIcon=dev.blossom.launcher\nTerminal=false\nCategories=Game;\nStartupWMClass=dev.blossom.launcher\n",quote_exec(&exe.to_string_lossy())?);
    let mut file=tempfile::NamedTempFile::new_in(&applications)?;file.write_all(desktop.as_bytes())?;
    file.persist(applications.join("dev.blossom.launcher.desktop"))?;
    Ok(())
}
#[cfg(not(target_os = "linux"))]
pub fn install() -> Result<()> { Ok(()) }
fn quote_exec(path:&str)->Result<String> {
    if path.contains(['\n','\r']) {return Err("Unsupported newline in launcher location".into());}
    // Exec field has both desktop-entry escaping and shell-like argument quoting.
    Ok(format!("\"{}\"",path.replace('\\',"\\\\\\\\").replace('"',"\\\\\"").replace('`',"\\\\`").replace('$',"\\\\$").replace('%',"%%")))
}
#[cfg(test)] mod tests {use super::*;#[test]fn paths(){assert_eq!(quote_exec("/home/jade/My Games/Blossom").unwrap(),"\"/home/jade/My Games/Blossom\"");assert_eq!(quote_exec("/tmp/100%/launcher").unwrap(),"\"/tmp/100%%/launcher\"");assert!(quote_exec("/tmp/a\nb").is_err());}}
