pub mod auth;
pub mod wardrobe;
pub mod desktop;
pub mod config;
pub mod install;
pub mod launch;
use sha2::{Digest, Sha256};
use std::{
    io::{Read, Write},
    path::{Path, PathBuf},
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc,
    },
};
pub type Result<T> = std::result::Result<T, Box<dyn std::error::Error + Send + Sync>>;
#[derive(Clone)]
pub struct Task {
    pub cancel: Arc<AtomicBool>,
    pub verify_existing: bool,
    pub notify: Arc<dyn Fn(&str, &str) + Send + Sync>,
    pub http: reqwest::blocking::Client,
}
impl Task {
    pub fn new(notify: impl Fn(&str, &str) + Send + Sync + 'static) -> Result<Self> {
        Ok(Self {
            verify_existing: true,
            cancel: Arc::new(AtomicBool::new(false)),
            notify: Arc::new(notify),
            http: reqwest::blocking::Client::builder()
                .connect_timeout(std::time::Duration::from_secs(15))
                .timeout(std::time::Duration::from_secs(120))
                .user_agent("Blossom/0.2.9.1")
                .build()?,
        })
    }
    pub fn check(&self) -> Result<()> {
        if self.cancel.load(Ordering::Relaxed) {
            Err("Cancelled".into())
        } else {
            Ok(())
        }
    }
    pub fn status(&self, s: &str) {
        (self.notify)("status", s)
    }
    pub fn wait(&self, seconds: u64) -> Result<()> {
        for _ in 0..seconds * 10 {
            self.check()?;
            std::thread::sleep(std::time::Duration::from_millis(100));
        }
        Ok(())
    }
    pub fn download(&self, url: &str, path: &Path, hash: &str, size: u64) -> Result<()> {
        self.check()?;
        if path.is_file()
            && (size == 0 || path.metadata()?.len() == size)
            && (!self.verify_existing || digest(path, hash.len())? == hash)
        {
            return Ok(());
        }
        if !url.starts_with("https://") {
            return Err("Download must use HTTPS".into());
        }
        std::fs::create_dir_all(path.parent().ok_or("Missing parent directory")?)?;
        let mut out = tempfile::NamedTempFile::new_in(path.parent().unwrap())?;
        let mut response = self.http.get(url).send()?.error_for_status()?;
        let mut buffer = [0; 65536];
        let mut count = 0u64;
        loop {
            self.check()?;
            let n = response.read(&mut buffer)?;
            if n == 0 {
                break;
            }
            count += n as u64;
            if count > 1024 * 1024 * 1024 || size > 0 && count > size {
                return Err("Download exceeds expected size".into());
            }
            out.write_all(&buffer[..n])?;
        }
        out.flush()?;
        if size > 0 && count != size || digest(out.path(), hash.len())? != hash {
            return Err("Download checksum or size mismatch".into());
        }
        self.check()?;
        out.as_file().sync_all()?;
        out.persist(path)?;
        Ok(())
    }
}
pub fn digest(path: &Path, len: usize) -> Result<String> {
    let mut f = std::fs::File::open(path)?;
    let mut b = [0; 65536];
    let mut sha1 = sha1::Sha1::new();
    let mut sha256 = Sha256::new();
    loop {
        let n = f.read(&mut b)?;
        if n == 0 {
            break;
        }
        if len == 40 {
            sha1.update(&b[..n]);
        } else if len == 64 {
            sha256.update(&b[..n]);
        } else {
            return Err("Unsupported checksum".into());
        }
    }
    Ok(if len == 40 {
        hex::encode(sha1.finalize())
    } else {
        hex::encode(sha256.finalize())
    })
}
pub fn safe(root: &Path, name: &str) -> Result<PathBuf> {
    if name.is_empty()
        || name.contains('\\')
        || name.contains(':')
        || name.starts_with('/')
        || name.split('/').any(|c| c == ".." || c == ".")
    {
        return Err("Unsafe archive or download path".into());
    }
    let path = root.join(name);
    let mut parent = Some(path.as_path());
    while let Some(p) = parent {
        if p.symlink_metadata()
            .map(|m| m.file_type().is_symlink())
            .unwrap_or(false)
        {
            return Err("Symbolic links are not valid installation targets".into());
        }
        if p == root {
            break;
        }
        parent = p.parent();
    }
    Ok(path)
}
pub fn read_json(path: &Path) -> Result<serde_json::Value> {
    if path.metadata()?.len() > 16 * 1024 * 1024 {
        return Err("Metadata too large".into());
    }
    Ok(serde_json::from_reader(std::fs::File::open(path)?)?)
}
pub fn os() -> Result<&'static str> {
    if !cfg!(target_arch = "x86_64") {
        return Err("Blossom requires x86_64".into());
    }
    if cfg!(windows) {
        Ok("windows")
    } else if cfg!(target_os = "linux") {
        Ok("linux")
    } else {
        Err("Supported platforms: Windows and Linux".into())
    }
}
#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn paths() {
        let d = tempfile::tempdir().unwrap();
        for s in ["../x", "/x", "C:/x", "a\\x", "a/./b"] {
            assert!(safe(d.path(), s).is_err());
        }
        assert!(safe(d.path(), "a/b.jar").is_ok());
    }
    #[test]
    fn optional_existing_checks_never_skip_new_downloads() {
        let d=tempfile::tempdir().unwrap();let file=d.path().join("cached");std::fs::write(&file,b"old").unwrap();
        let mut t=Task::new(|_,_|{}).unwrap();t.verify_existing=false;
        assert!(t.download("invalid",&file,&"0".repeat(64),3).is_ok());
        assert!(t.download("invalid",&d.path().join("missing"),&"0".repeat(64),3).is_err());
        t.verify_existing=true;assert!(t.download("invalid",&file,&"0".repeat(64),3).is_err());
    }
    #[test]
    fn cancelled() {
        let t = Task::new(|_, _| {}).unwrap();
        t.cancel.store(true, Ordering::Relaxed);
        assert!(t.check().is_err());
    }
}

/// Suppress helper console windows while preserving piped game output.
pub(crate) fn command(program: impl AsRef<std::ffi::OsStr>) -> std::process::Command {
    let mut command = std::process::Command::new(program);
    #[cfg(windows)] {
        use std::os::windows::process::CommandExt;
        command.creation_flags(0x08000000);
    }
    #[cfg(not(windows))]
    let _ = &mut command;
    command
}
