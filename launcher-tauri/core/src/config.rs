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
    // Match the existing Swing UTF-8 Reader/Writer contract.
    let raw = std::fs::read(path)?;
    let text = match String::from_utf8(raw.clone()) {
        Ok(text) => text,
        Err(_) => encoding_rs::WINDOWS_1252.decode(&raw).0.into_owned(),
    };
    let mut values = HashMap::new();
    java_properties::PropertiesIter::new_with_encoding(text.as_bytes(), encoding_rs::UTF_8)
        .read_into(|key, value| { values.insert(key, value); })?;
    Ok(values)
}
pub fn write_properties(path: &Path, p: &HashMap<String, String>, private: bool) -> Result<()> {
    std::fs::create_dir_all(path.parent().ok_or("No settings folder")?)?;
    let mut file = tempfile::NamedTempFile::new_in(path.parent().unwrap())?;
    if private { private_file(file.path())?; }
    {
        let mut writer = java_properties::PropertiesWriter::new_with_encoding(&mut file, encoding_rs::UTF_8);
        for (key, value) in p { writer.write(key, value)?; }
        writer.finish()?;
    }
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
        let out = crate::command("powershell.exe")
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
        if !crate::command("icacls.exe")
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
    fn unicode_paths_survive_native_and_java_properties() {
        let dir = tempfile::tempdir().unwrap();
        let path = dir.path().join("launcher.properties");
        std::fs::write(&path, "game=Jadé-花-🌸\n").unwrap();
        let config = Config::load(dir.path()).unwrap();
        assert_eq!(config.game, "Jadé-花-🌸");
        config.save(dir.path()).unwrap();
        assert_eq!(Config::load(dir.path()).unwrap().game, config.game);
        let bytes = std::fs::read(path).unwrap();
        assert!(String::from_utf8(bytes).unwrap().contains("Jadé-花-🌸"));
    }
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
