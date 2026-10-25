//! Account-owned capes and an append-only local skin collection. Tokens never cross IPC.
use crate::*;
use base64::{engine::general_purpose::STANDARD, Engine};
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
const PROFILE: &str = "https://api.minecraftservices.com/minecraft/profile";
const MAX_PNG: u64 = 1024 * 1024;
#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Skin {
    pub id: String,
    pub name: String,
    pub variant: String,
    #[serde(default, skip_serializing_if = "String::is_empty")]
    pub image: String,
}
#[derive(Serialize)]
pub struct Cape { pub id: String, pub name: String, pub active: bool, pub image: String }
#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct Wardrobe {
    pub name: String, pub uuid: String, pub current_skin: Option<String>,
    pub skins: Vec<Skin>, pub capes: Vec<Cape>,
}
fn variant(v: &str) -> Result<&str> {
    if matches!(v, "classic" | "slim") { Ok(v) } else { Err("Choose Classic or Slim arms".into()) }
}
pub fn validate_png(bytes: &[u8], skin: bool) -> Result<()> {
    if bytes.len() as u64 > MAX_PNG { return Err("PNG exceeds 1 MiB".into()); }
    let mut decoder = png::Decoder::new(std::io::Cursor::new(bytes));
    decoder.set_limits(png::Limits { bytes: 1024 * 1024 });
    let mut reader = decoder.read_info()?;
    let (w, h) = (reader.info().width, reader.info().height);
    if w != 64 || !(h == 32 || skin && h == 64) {
        return Err(if skin { "Skin must be a 64×64 or 64×32 PNG" } else { "Invalid cape texture dimensions" }.into());
    }
    let mut frame = vec![0; reader.output_buffer_size()];
    reader.next_frame(&mut frame)?;
    Ok(())
}
fn data_url(bytes: &[u8]) -> String { format!("data:image/png;base64,{}", STANDARD.encode(bytes)) }
fn store_path(root: &Path) -> PathBuf { root.join("skins") }
fn skin_path(root: &Path, id: &str, extension: &str) -> Result<PathBuf> {
    if id.len() != 72 && id.len() != 69 { return Err("Invalid saved skin".into()); }
    let (hash, model) = id.split_once('-').ok_or("Invalid skin ID")?;
    if hash.len() != 64 || !hash.bytes().all(|b| b.is_ascii_hexdigit()) { return Err("Invalid skin ID".into()); }
    variant(model)?;
    safe(&store_path(root), &format!("{id}.{extension}"))
}
fn append(root: &Path, bytes: &[u8], name: &str, model: &str) -> Result<String> {
    variant(model)?; validate_png(bytes, true)?;
    let id = format!("{}-{model}", hex::encode(Sha256::digest(bytes)));
    let png = skin_path(root, &id, "png")?;
    std::fs::create_dir_all(store_path(root))?;
    // Each content/model pair has its own immutable file. Never replace or prune old skins.
    if !png.exists() {
        let mut file = tempfile::NamedTempFile::new_in(store_path(root))?;
        file.write_all(bytes)?; file.as_file().sync_all()?;
        file.persist_noclobber(&png)?;
    }
    let metadata = skin_path(root, &id, "json")?;
    if !metadata.exists() {
        let skin = Skin { id: id.clone(), name: name.chars().take(80).collect(), variant: model.into(), image: String::new() };
        let mut file = tempfile::NamedTempFile::new_in(store_path(root))?;
        serde_json::to_writer(&mut file, &skin)?; file.as_file().sync_all()?;
        file.persist_noclobber(metadata)?;
    }
    Ok(id)
}
pub fn import(root: &Path, file: &Path, model: &str) -> Result<String> {
    if std::fs::metadata(file)?.len() > MAX_PNG { return Err("Skin exceeds 1 MiB".into()); }
    let bytes = std::fs::read(file)?;
    append(root, &bytes, file.file_stem().and_then(|s| s.to_str()).unwrap_or("Skin"), model)
}
pub fn collection(root: &Path) -> Result<Vec<Skin>> {
    let dir = store_path(root); if !dir.exists() { return Ok(vec![]); }
    let mut skins = vec![];
    for entry in std::fs::read_dir(dir)? {
        let path = entry?.path();
        if path.extension().and_then(|s| s.to_str()) != Some("json") { continue; }
        let mut skin: Skin = serde_json::from_value(read_json(&path)?)?;
        let bytes = std::fs::read(skin_path(root, &skin.id, "png")?)?;
        validate_png(&bytes, true)?; skin.image = data_url(&bytes); skins.push(skin);
    }
    skins.sort_by(|a,b| a.name.to_lowercase().cmp(&b.name.to_lowercase()).then(a.id.cmp(&b.id)));
    Ok(skins)
}
fn checked(request: reqwest::blocking::RequestBuilder) -> Result<reqwest::blocking::Response> {
    let r = request.send()?; let status = r.status();
    if !status.is_success() {
        return Err(match status.as_u16() {
            401 => "Your session expired. Restore sign-in and try again".into(),
            429 => "Minecraft is rate limiting changes. Please wait before trying again".into(),
            _ => format!("Minecraft profile request failed ({status}). Your saved skins are unchanged"),
        }.into());
    }
    Ok(r)
}
fn profile(t: &Task, s: &auth::Session) -> Result<Value> {
    if s.offline { return Err("Sign in with Microsoft to manage account skins and capes".into()); }
    t.check()?;
    let p: Value = serde_json::from_reader(checked(t.http.get(PROFILE).bearer_auth(&s.token))?.take(2*1024*1024))?;
    if p["id"].as_str() != Some(&s.uuid) { return Err("Account profile changed; sign in again".into()); }
    Ok(p)
}
fn texture(t: &Task, root: &Path, url: &str, skin: bool) -> Result<Vec<u8>> {
    let mut url = reqwest::Url::parse(url)?;
    if url.host_str() != Some("textures.minecraft.net") || !matches!(url.scheme(), "http" | "https") || !url.username().is_empty() || url.password().is_some() || url.port().is_some() {
        return Err("Unexpected Minecraft texture URL".into());
    }
    url.set_scheme("https").map_err(|_| "Invalid texture URL")?;
    let hash = url.path().strip_prefix("/texture/").ok_or("Invalid texture path")?;
    if hash.len() > 64 || hash.len() < 32 || !hash.bytes().all(|b| b.is_ascii_hexdigit()) { return Err("Invalid texture hash".into()); }
    let path = safe(&root.join("cache/wardrobe"), &format!("{hash}.png"))?;
    if path.is_file() { let bytes=std::fs::read(&path)?; validate_png(&bytes,skin)?; return Ok(bytes); }
    // No bearer credentials on the public texture host; no redirects to arbitrary hosts.
    let client = reqwest::blocking::Client::builder().redirect(reqwest::redirect::Policy::none()).timeout(std::time::Duration::from_secs(25)).build()?;
    t.check()?;
    let mut bytes=Vec::new(); checked(client.get(url))?.take(MAX_PNG+1).read_to_end(&mut bytes)?;
    validate_png(&bytes, skin)?;
    std::fs::create_dir_all(path.parent().unwrap())?;
    let mut temp=tempfile::NamedTempFile::new_in(path.parent().unwrap())?; temp.write_all(&bytes)?; temp.persist(path)?;
    Ok(bytes)
}
fn view(t: &Task, root: &Path, p: &Value) -> Result<Wardrobe> {
    let name=p["name"].as_str().ok_or("Missing profile name")?.to_owned();
    let uuid=p["id"].as_str().ok_or("Missing profile ID")?.to_owned();
    let mut current_skin=None;
    for skin in p["skins"].as_array().ok_or("Missing account skins")? {
        if skin["state"] != "ACTIVE" { continue; }
        let bytes=texture(t,root,skin["url"].as_str().ok_or("Missing skin texture")?,true)?;
        let model=if skin["variant"] == "SLIM" { "slim" } else { "classic" };
        current_skin=Some(append(root,&bytes,&format!("{name}'s skin"),model)?);
    }
    let mut capes=vec![];
    for cape in p["capes"].as_array().ok_or("Missing owned capes")? {
        capes.push(Cape { id: cape["id"].as_str().ok_or("Missing cape ID")?.into(), name: cape["alias"].as_str().unwrap_or("Owned cape").into(), active: cape["state"] == "ACTIVE", image: data_url(&texture(t,root,cape["url"].as_str().ok_or("Missing cape texture")?,false)?) });
    }
    Ok(Wardrobe { name, uuid, current_skin, skins: collection(root)?, capes })
}
pub fn refresh(t: &Task, root: &Path, s: &auth::Session) -> Result<Wardrobe> { view(t,root,&profile(t,s)?) }
pub fn use_skin(t: &Task, root: &Path, s: &auth::Session, id: &str) -> Result<Wardrobe> {
    // Archive the account's current skin BEFORE replacing it, including externally changed skins.
    refresh(t,root,s)?;
    let path=skin_path(root,id,"png")?; let bytes=std::fs::read(path)?; validate_png(&bytes,true)?;
    let model=variant(id.split_once('-').ok_or("Invalid skin")?.1)?;
    let form=reqwest::blocking::multipart::Form::new().text("variant",model.to_owned()).part("file",reqwest::blocking::multipart::Part::bytes(bytes).file_name("skin.png").mime_str("image/png")?);
    checked(t.http.post(format!("{PROFILE}/skins")).bearer_auth(&s.token).multipart(form))?;
    refresh(t,root,s)
}
pub fn use_cape(t: &Task, root: &Path, s: &auth::Session, id: &str) -> Result<Wardrobe> {
    let p=profile(t,s)?;
    let endpoint=format!("{PROFILE}/capes/active");
    if id.is_empty() { checked(t.http.delete(endpoint).bearer_auth(&s.token))?; }
    else {
        if !p["capes"].as_array().ok_or("Missing capes")?.iter().any(|c| c["id"].as_str()==Some(id)) { return Err("This cape is not owned by the signed-in account".into()); }
        checked(t.http.put(endpoint).bearer_auth(&s.token).json(&json!({"capeId":id})))?;
    }
    refresh(t,root,s)
}
#[cfg(test)]
mod tests {
    use super::*;
    fn png() -> Vec<u8> { let mut b=vec![]; {let mut encoder=png::Encoder::new(&mut b,64,64);encoder.set_color(png::ColorType::Rgba);let mut w=encoder.write_header().unwrap();w.write_image_data(&vec![255;64*64*4]).unwrap();} b }
    #[test] fn append_only_skin_collection() {
        let d=tempfile::tempdir().unwrap();let bytes=png();
        let a=append(d.path(),&bytes,"First","classic").unwrap();
        assert_eq!(a,append(d.path(),&bytes,"Renamed","classic").unwrap());
        let b=append(d.path(),&bytes,"Slim","slim").unwrap();assert_ne!(a,b);
        let saved=collection(d.path()).unwrap();assert_eq!(saved.len(),2);assert_eq!(saved[0].name,"First");
        assert!(skin_path(d.path(),"../escape","png").is_err());
        assert!(append(d.path(),b"not png","Bad","classic").is_err());
        assert!(append(d.path(),&bytes,"Bad","unknown").is_err());
        assert_eq!(collection(d.path()).unwrap().len(),2);
    }
}
