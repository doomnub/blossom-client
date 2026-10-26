use crate::*;
use serde_json::Value;
pub struct Installed {
    pub classpath: Vec<PathBuf>,
    pub assets: PathBuf,
    pub natives: PathBuf,
    pub logging: PathBuf,
    pub index: String,
}
fn string<'a>(v: &'a Value, key: &str) -> Result<&'a str> {
    v[key]
        .as_str()
        .ok_or_else(|| format!("Missing metadata field: {key}").into())
}
fn artifact(t: &Task, a: &Value, target: &Path) -> Result<()> {
    t.download(
        string(a, "url")?,
        target,
        string(a, "sha1")?,
        a["size"].as_u64().unwrap_or(0),
    )
}
pub fn allowed(v: &Value, os: &str) -> Result<bool> {
    let Some(rules) = v["rules"].as_array() else {
        return Ok(true);
    };
    let mut allow = false;
    for rule in rules {
        let o = &rule["os"];
        let mut matches = rule.get("features").is_none();
        if let Some(name) = o["name"].as_str() {
            matches &= name == os;
        }
        if let Some(arch) = o["arch"].as_str() {
            matches &= regex::Regex::new(arch)?.is_match("amd64");
        }
        if o.get("version").is_some() {
            matches = false;
        }
        if matches {
            allow = rule["action"] == "allow";
        }
    }
    Ok(allow)
}
pub fn install(t: &Task, root: &Path, game: &Path) -> Result<Installed> {
    let platform = os()?;
    let cache = root.join("cache");
    std::fs::create_dir_all(game)?;
    t.status("Checking Minecraft 1.8.9");
    let manifest = cache.join("versions/1.8.9.json");
    t.download("https://piston-meta.mojang.com/v1/packages/d546f1707a3f2b7d034eece5ea2e311eda875787/1.8.9.json",&manifest,"d546f1707a3f2b7d034eece5ea2e311eda875787",0)?;
    let meta = read_json(&manifest)?;
    let client = cache.join("versions/1.8.9.jar");
    artifact(t, &meta["downloads"]["client"], &client)?;
    let libraries = cache.join("libraries");
    let natives = game.join(".blossom-natives");
    std::fs::create_dir_all(&natives)?;
    let mut cp = vec![];
    for lib in meta["libraries"].as_array().ok_or("Missing libraries")? {
        t.check()?;
        if !allowed(lib, platform)? || string(lib, "name")?.starts_with("org.apache.logging.log4j:")
        {
            continue;
        }
        let downloads = &lib["downloads"];
        if let Some(a) = downloads.get("artifact") {
            let file = safe(&libraries, string(a, "path")?)?;
            artifact(t, a, &file)?;
            cp.push(file);
        }
        if let Some(native) = lib["natives"][platform].as_str() {
            let classifier = native.replace("${arch}", "64");
            let a = &downloads["classifiers"][classifier];
            let file = safe(&libraries, string(a, "path")?)?;
            artifact(t, a, &file)?;
            extract(t, &file, &natives, true)?;
        }
    }
    for (name, hash) in [
        ("log4j-api", "6c80ea65e41f7858f7e15cb390c98c36167c7b3a"),
        ("log4j-core", "c194a13e18b0a36014e867d2d323b989bf212151"),
    ] {
        let relative = format!("org/apache/logging/log4j/{name}/2.25.5/{name}-2.25.5.jar");
        let path = safe(&libraries, &relative)?;
        t.download(
            &format!("https://repo.maven.apache.org/maven2/{relative}"),
            &path,
            hash,
            0,
        )?;
        cp.push(path);
    }
    cp.push(client);
    let assets = cache.join("assets");
    let index = string(&meta["assetIndex"], "id")?.to_owned();
    let index_file = safe(&assets, &format!("indexes/{index}.json"))?;
    artifact(t, &meta["assetIndex"], &index_file)?;
    let data = read_json(&index_file)?;
    let objects = data["objects"].as_object().ok_or("Missing asset objects")?;
    for (i, (name, a)) in objects.iter().enumerate() {
        let hash = string(a, "hash")?;
        if hash.len() != 40 || !hash.bytes().all(|b| b.is_ascii_hexdigit()) {
            return Err("Invalid asset hash".into());
        }
        let relative = format!("{}/{}", &hash[..2], hash);
        let path = safe(&assets.join("objects"), &relative)?;
        t.download(
            &format!("https://resources.download.minecraft.net/{relative}"),
            &path,
            hash,
            a["size"].as_u64().unwrap_or(0),
        )?;
        for destination in [
            if data["virtual"] == true {
                Some(assets.join("virtual").join(&index))
            } else {
                None
            },
            if data["map_to_resources"] == true {
                Some(game.join("resources"))
            } else {
                None
            },
        ]
        .into_iter()
        .flatten()
        {
            let target = safe(&destination, name)?;
            std::fs::create_dir_all(target.parent().unwrap())?;
            if t.verify_existing || !target.is_file() || target.metadata()?.len()!=path.metadata()?.len() {std::fs::copy(&path, target)?;}
        }
        if i % 25 == 0 || i + 1 == objects.len() {
            t.status(&format!("Checking assets: {} / {}", i + 1, objects.len()));
        }
    }
    let logging = write_logging(&cache)?;
    Ok(Installed {
        classpath: cp,
        assets,
        natives,
        logging,
        index,
    })
}
/// Use the same dependency-free layout shipped in the legacy launcher.
pub fn write_logging(cache: &Path) -> Result<PathBuf> {
    let path=cache.join("logging/blossom-log4j2.xml");
    std::fs::create_dir_all(path.parent().unwrap())?;
    std::fs::write(&path,include_bytes!("../../../launcher/src/main/resources/log4j2-blossom.xml"))?;
    Ok(path)
}
pub fn extract(t: &Task, archive: &Path, root: &Path, natives: bool) -> Result<()> {
    let mut zip = zip::ZipArchive::new(std::fs::File::open(archive)?)?;
    let mut total = 0;
    let mut links = Vec::new();
    for i in 0..zip.len() {
        t.check()?;
        let mut entry = zip.by_index(i)?;
        let target = safe(root, entry.name())?;
        let mode = entry.unix_mode().unwrap_or(0);
        if mode & 0o170000 == 0o120000 {
            if natives || entry.size() > 4096 {
                return Err("Invalid archive link".into());
            }
            let mut link = String::new();
            entry.read_to_string(&mut link)?;
            let source = link_target(root, &target, &link)?;
            links.push((target, source));
            continue;
        }
        if entry.is_dir() {
            continue;
        }
        if natives
            && (![".so", ".dll", ".dylib"]
                .iter()
                .any(|s| entry.name().ends_with(s))
                || entry.name().starts_with("META-INF/"))
        {
            continue;
        }
        total += entry.size();
        if total
            > if natives {
                128 * 1024 * 1024
            } else {
                512 * 1024 * 1024
            }
        {
            return Err("Archive exceeds extraction limit".into());
        }
        if natives && !t.verify_existing && target.is_file() && target.metadata()?.len()==entry.size() { continue; }
        std::fs::create_dir_all(target.parent().unwrap())?;
        let mut file = tempfile::NamedTempFile::new_in(target.parent().unwrap())?;
        let mut buf = [0; 65536];
        loop {
            t.check()?;
            let n = entry.read(&mut buf)?;
            if n == 0 {
                break;
            }
            file.write_all(&buf[..n])?;
        }
        file.flush()?;
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            std::fs::set_permissions(
                file.path(),
                std::fs::Permissions::from_mode(if mode & 0o111 != 0 { 0o755 } else { 0o644 }),
            )?;
        }
        file.persist(target)?;
    }
    // Materialize internal file aliases after extraction; no symlinks remain in the runtime.
    for (target, source) in links {
        t.check()?;
        if source.is_dir() {
            continue;
        }
        if !source.is_file() {
            return Err("Archive link target is missing".into());
        }
        std::fs::create_dir_all(target.parent().unwrap())?;
        std::fs::copy(source, target)?;
    }
    Ok(())
}
fn link_target(root: &Path, target: &Path, link: &str) -> Result<PathBuf> {
    if link.is_empty() || link.starts_with('/') || link.contains(':') || link.contains('\\') {
        return Err("Unsafe archive link".into());
    }
    let mut relative = target
        .parent()
        .ok_or("Invalid link")?
        .strip_prefix(root)?
        .to_path_buf();
    for component in link.split('/') {
        match component {
            "" | "." => {}
            ".." => {
                if !relative.pop() {
                    return Err("Archive link escapes extraction folder".into());
                }
            }
            name => relative.push(name),
        }
    }
    safe(root, &relative.to_string_lossy().replace('\\', "/"))
}
pub fn java(t: &Task, root: &Path) -> Result<PathBuf> { java_with_verification(t, root, true) }
pub fn java_with_verification(t: &Task, root: &Path, verify: bool) -> Result<PathBuf> {
    let meta: Value = serde_json::from_str(include_str!(
        "../../../launcher/src/main/resources/runtimes.json"
    ))?;
    let runtime = &meta[os()?];
    let folder = string(runtime, "folder")?;
    let target = root.join("runtimes").join(folder);
    let exe = safe(&target, string(runtime, "executable")?)?;
    if exe.is_file() && (!verify || verify_java(&exe).is_ok()) {
        t.status("Managed Java 8 is ready");
        return Ok(exe);
    }
    t.status("Downloading verified Java 8");
    let archive = root.join("cache/runtimes").join(format!("{folder}.zip"));
    let mut verified=t.clone();verified.verify_existing=true;
    verified.download(
        string(runtime, "url")?,
        &archive,
        string(runtime, "sha256")?,
        0,
    )?;
    let runtimes = root.join("runtimes");
    std::fs::create_dir_all(&runtimes)?;
    let staging = tempfile::tempdir_in(&runtimes)?;
    extract(t, &archive, staging.path(), false)?;
    let staged = safe(&staging.path().join(folder), string(runtime, "executable")?)?;
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(&staged, std::fs::Permissions::from_mode(0o755))?;
    }
    verify_java(&staged)?;
    t.check()?;
    // Never delete an existing runtime until the replacement verifies. Keep a recoverable backup.
    let backup = runtimes.join(format!("{folder}.backup"));
    if target.exists() {
        if backup.exists() {
            return Err("Runtime backup already exists; inspect it before repair".into());
        }
        std::fs::rename(&target, &backup)?;
    }
    if let Err(e) = std::fs::rename(staging.path().join(folder), &target) {
        if backup.exists() {
            let _ = std::fs::rename(&backup, &target);
        }
        return Err(e.into());
    }
    Ok(exe)
}
pub fn verify_java(exe: &Path) -> Result<()> {
    use std::process::Stdio;
    if !exe.is_file() {
        return Err("Java executable does not exist".into());
    }
    let mut child = crate::command(exe)
        .args(["-XshowSettings:properties", "-version"])
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()?;
    let out = child.stdout.take().unwrap();
    let err = child.stderr.take().unwrap();
    let reader = |r: Box<dyn Read + Send>| {
        std::thread::spawn(move || {
            let mut text = String::new();
            let _ = r.take(65536).read_to_string(&mut text);
            text
        })
    };
    let a = reader(Box::new(out));
    let b = reader(Box::new(err));
    let deadline = std::time::Instant::now() + std::time::Duration::from_secs(30);
    let status = loop {
        if let Some(status) = child.try_wait()? {
            break status;
        }
        if std::time::Instant::now() > deadline {
            let _ = child.kill();
            let _ = child.wait();
            return Err("Java version check timed out".into());
        }
        std::thread::sleep(std::time::Duration::from_millis(50));
    };
    let text = a.join().unwrap_or_default() + &b.join().unwrap_or_default();
    if !status.success()
        || !text.contains("\"1.8.")
        || !regex::Regex::new(r"sun\.arch\.data\.model\s*=\s*64")?.is_match(&text)
    {
        return Err("Select a 64-bit Java 8 executable".into());
    }
    Ok(())
}
#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn internal_runtime_links() {
        let d = tempfile::tempdir().unwrap();
        let archive = d.path().join("runtime.zip");
        let mut zip = zip::ZipWriter::new(std::fs::File::create(&archive).unwrap());
        let options = zip::write::SimpleFileOptions::default();
        zip.start_file("lib/libjsig.so", options).unwrap();
        zip.write_all(b"verified-library").unwrap();
        zip.add_symlink("lib/server/libjsig.so", "../libjsig.so", options)
            .unwrap();
        zip.finish().unwrap();
        let out = d.path().join("out");
        extract(&Task::new(|_, _| {}).unwrap(), &archive, &out, false).unwrap();
        assert_eq!(
            std::fs::read(out.join("lib/server/libjsig.so")).unwrap(),
            b"verified-library"
        );
        assert!(
            !std::fs::symlink_metadata(out.join("lib/server/libjsig.so"))
                .unwrap()
                .file_type()
                .is_symlink()
        );
        assert!(link_target(&out, &out.join("alias"), "../../outside").is_err());
    }
    #[test]
    fn rules() {
        assert!(allowed(&serde_json::json!({}), "linux").unwrap());
        assert!(!allowed(
            &serde_json::json!({"rules":[{"action":"allow","os":{"name":"windows"}}]}),
            "linux"
        )
        .unwrap());
    }
    #[test]
    fn extraction_rejects_escape() {
        let d = tempfile::tempdir().unwrap();
        let p = d.path().join("bad.zip");
        let mut zip = zip::ZipWriter::new(std::fs::File::create(&p).unwrap());
        zip.start_file("../escape", zip::write::SimpleFileOptions::default())
            .unwrap();
        zip.write_all(b"bad").unwrap();
        zip.finish().unwrap();
        assert!(extract(
            &Task::new(|_, _| {}).unwrap(),
            &p,
            &d.path().join("out"),
            false
        )
        .is_err());
    }
}
