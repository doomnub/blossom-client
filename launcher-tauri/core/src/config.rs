use crate::*;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Config {
    pub java_auto: bool,
    pub java: String,
    pub game: String,
    pub client_id: String,
    pub offline_name: String,
    pub memory: u32,
    pub width: u32,
    pub height: u32,
}
pub fn properties(path: &Path) -> Result<HashMap<String, String>> {
    if !path.exists() {
        return Ok(HashMap::new());
    }
    if path.metadata()?.len() > 65536 {
        return Err("Settings file exceeds size limit".into());
    }
    // Swing writes Properties through a UTF-8 Writer; java-properties supports Java escaping.
    let text = std::fs::read_to_string(path)?;
    let escaped: String = text
        .chars()
        .flat_map(|c| {
            if c as u32 > 127 {
                c.encode_utf16(&mut [0; 2])
                    .iter()
                    .map(|v| format!("\\u{v:04x}"))
                    .collect::<String>()
                    .chars()
                    .collect::<Vec<_>>()
            } else {
                vec![c]
            }
        })
        .collect();
    Ok(java_properties::read(escaped.as_bytes())?)
}
pub fn write_properties(path: &Path, p: &HashMap<String, String>, private: bool) -> Result<()> {
    std::fs::create_dir_all(path.parent().ok_or("No settings folder")?)?;
    let mut file = tempfile::NamedTempFile::new_in(path.parent().unwrap())?;
    if private {
        private_file(file.path())?;
    }
    java_properties::write(&mut file, p)?;
    file.as_file().sync_all()?;
    file.persist(path)?;
    Ok(())
}
fn private_file(path: &Path) -> Result<()> {
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(path, std::fs::Permissions::from_mode(0o600))?;
    }
    #[cfg(windows)]
    {
        use std::process::Command;
        let out = Command::new("powershell.exe")
            .args([
                "-NoProfile",
                "-NonInteractive",
                "-Command",
                "[System.Security.Principal.WindowsIdentity]::GetCurrent().User.Value",
            ])
            .output()?;
        let sid = String::from_utf8(out.stdout)?.trim().to_owned();
        if !out.status.success() || !regex::Regex::new(r"^S-1-[0-9-]+$")?.is_match(&sid) {
            return Err("Cannot identify credential owner".into());
        }
        if !Command::new("icacls.exe")
            .arg(path)
            .args(["/inheritance:r", "/grant:r", &format!("*{sid}:(F)")])
            .output()?
            .status
            .success()
        {
            return Err("Cannot secure account file".into());
        }
    }
    Ok(())
}
impl Config {
    pub fn load(root: &Path) -> Result<Self> {
        let p = properties(&root.join("launcher.properties"))?;
        let get = |k: &str, d: String| p.get(k).cloned().unwrap_or(d);
        let number = |k: &str, d: u32, min: u32, max: u32| {
            p.get(k)
                .and_then(|x| x.parse::<u32>().ok())
                .unwrap_or(d)
                .clamp(min, max)
        };
        Ok(Self {
            java_auto: get("java.auto", "true".into()) == "true",
            java: get("java", String::new()),
            game: get(
                "game",
                root.join("instances/default").to_string_lossy().into(),
            ),
            client_id: get(
                "clientId",
                std::env::var("BLOSSOM_MICROSOFT_CLIENT_ID").unwrap_or_default(),
            ),
            offline_name: get("offlineName", "Player".into()),
            memory: number("memory", 4096, 1024, 16384),
            width: number("width", 1280, 640, 7680),
            height: number("height", 720, 360, 4320),
        })
    }
    pub fn validate(&self) -> Result<()> {
        if !(1024..=16384).contains(&self.memory)
            || !(640..=7680).contains(&self.width)
            || !(360..=4320).contains(&self.height)
        {
            return Err("Memory or resolution is outside supported bounds".into());
        }
        if self.game.trim().is_empty() {
            return Err("Choose a Minecraft directory".into());
        }
        if !self.java_auto && self.java.trim().is_empty() {
            return Err("Choose a Java executable or enable automatic Java".into());
        }
        Ok(())
    }
    pub fn save(&self, root: &Path) -> Result<()> {
        self.validate()?;
        let path = root.join("launcher.properties");
        let mut p = properties(&path)?;
        for (k, v) in [
            ("java.auto", self.java_auto.to_string()),
            ("java", self.java.clone()),
            ("game", self.game.clone()),
            ("clientId", self.client_id.clone()),
            ("offlineName", self.offline_name.clone()),
            ("memory", self.memory.to_string()),
            ("width", self.width.to_string()),
            ("height", self.height.to_string()),
        ] {
            p.insert(k.into(), v);
        }
        write_properties(&path, &p, false)
    }
}
#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn migration_roundtrip() {
        let dir = tempfile::tempdir().unwrap();
        std::fs::write(dir.path().join("launcher.properties"),"java.auto=false\njava=C\\:\\\\Java\\\\bin\\\\java.exe\ngame=/home/jade/Minecraft\nmemory=8192\nofflineName=Jade\nunknown=preserved\n").unwrap();
        let c = Config::load(dir.path()).unwrap();
        assert_eq!(c.memory, 8192);
        assert_eq!(c.java, r"C:\Java\bin\java.exe");
        c.save(dir.path()).unwrap();
        assert_eq!(
            properties(&dir.path().join("launcher.properties")).unwrap()["unknown"],
            "preserved"
        );
        assert_eq!(Config::load(dir.path()).unwrap().offline_name, "Jade");
    }
}
