use crate::*;
use serde_json::{json, Value};
use std::collections::HashMap;
const OAUTH: &str = "https://login.microsoftonline.com/consumers/oauth2/v2.0/";
#[derive(Clone)]
pub struct Session {
    pub name: String,
    pub uuid: String,
    pub token: String,
    pub offline: bool,
}
impl Session {
    pub fn offline(name: &str) -> Result<Self> {
        use md5::Md5;
        if !regex::Regex::new(r"^[A-Za-z0-9_]{1,16}$")?.is_match(name) {
            return Err("Offline username needs 1–16 letters, numbers or underscores".into());
        }
        let mut bytes = Md5::digest(format!("OfflinePlayer:{name}").as_bytes());
        bytes[6] = (bytes[6] & 15) | 0x30;
        bytes[8] = (bytes[8] & 63) | 0x80;
        Ok(Self {
            name: name.into(),
            uuid: hex::encode(bytes),
            token: "0".into(),
            offline: true,
        })
    }
}
fn response(t: &Task, request: reqwest::blocking::RequestBuilder) -> Result<Value> {
    t.check()?;
    let r = request.send()?;
    let status = r.status();
    let v: Value = serde_json::from_reader(r.take(2 * 1024 * 1024))?;
    if !status.is_success() {
        let code = v["error"]
            .as_str()
            .filter(|s| s.len() < 80 && s.bytes().all(|c| c.is_ascii_alphanumeric() || c == b'_'))
            .unwrap_or("service_error");
        return Err(format!("Authentication failed ({status}): {code}").into());
    }
    t.check()?;
    Ok(v)
}
fn form(t: &Task, path: &str, args: &[(&str, &str)]) -> Result<Value> {
    response(t, t.http.post(format!("{OAUTH}{path}")).form(args))
}
fn field<'a>(v: &'a Value, k: &str) -> Result<&'a str> {
    v[k].as_str()
        .ok_or_else(|| format!("Authentication response missing {k}").into())
}
pub fn saved(root: &Path, id: &str) -> Result<Option<String>> {
    let p = config::properties(&root.join("account.properties"))?;
    Ok(if p.get("clientId").map(String::as_str) == Some(id) {
        p.get("refreshToken").cloned()
    } else {
        None
    })
}
fn remember(root: &Path, id: &str, reply: &Value) -> Result<()> {
    if let Some(token) = reply["refresh_token"].as_str() {
        if token.is_empty() || token.len() > 32000 {
            return Err("Invalid refresh credential".into());
        }
        let p = HashMap::from([
            ("clientId".into(), id.into()),
            ("refreshToken".into(), token.into()),
        ]);
        config::write_properties(&root.join("account.properties"), &p, true)?;
    }
    Ok(())
}
pub fn sign_out(root: &Path) -> Result<()> {
    match std::fs::remove_file(root.join("account.properties")) {
        Ok(()) => Ok(()),
        Err(e) if e.kind() == std::io::ErrorKind::NotFound => Ok(()),
        Err(e) => Err(e.into()),
    }
}
pub fn sign_in(t: &Task, root: &Path, id: &str) -> Result<Session> {
    if !regex::Regex::new(r"^[a-fA-F0-9]{8}(-[a-fA-F0-9]{4}){3}-[a-fA-F0-9]{12}$")?.is_match(id) {
        return Err("Set your Microsoft public-client application ID in Settings".into());
    }
    let d = form(
        t,
        "devicecode",
        &[
            ("client_id", id),
            ("scope", "XboxLive.signin offline_access"),
        ],
    )?;
    let uri = field(&d, "verification_uri")?;
    let url = reqwest::Url::parse(uri)?;
    if url.scheme() != "https"
        || !matches!(
            url.host_str(),
            Some("microsoft.com" | "www.microsoft.com" | "login.microsoftonline.com")
        )
    {
        return Err("Unexpected sign-in URL".into());
    }
    (t.notify)(
        "device",
        &json!({"code":field(&d,"user_code")?,"url":uri}).to_string(),
    );
    let deadline = std::time::Instant::now()
        + std::time::Duration::from_secs(d["expires_in"].as_u64().unwrap_or(900).min(1800));
    let mut interval = d["interval"].as_u64().unwrap_or(5).clamp(5, 60);
    while std::time::Instant::now() < deadline {
        t.wait(interval)?;
        match form(
            t,
            "token",
            &[
                ("client_id", id),
                ("grant_type", "urn:ietf:params:oauth:grant-type:device_code"),
                ("device_code", field(&d, "device_code")?),
            ],
        ) {
            Ok(reply) => {
                remember(root, id, &reply)?;
                return exchange(t, field(&reply, "access_token")?);
            }
            Err(e) if e.to_string().ends_with("authorization_pending") => continue,
            Err(e) if e.to_string().ends_with("slow_down") => {
                interval = (interval + 5).min(60);
                continue;
            }
            Err(e) => return Err(e),
        }
    }
    Err("Sign-in code expired; try again".into())
}
pub fn refresh(t: &Task, root: &Path, id: &str) -> Result<Session> {
    let token = saved(root, id)?.ok_or("Sign in with Microsoft first")?;
    t.status("Restoring remembered sign-in");
    let reply = match form(
        t,
        "token",
        &[
            ("client_id", id),
            ("grant_type", "refresh_token"),
            ("refresh_token", &token),
            ("scope", "XboxLive.signin offline_access"),
        ],
    ) {
        Ok(r) => r,
        Err(e) => {
            if e.to_string().ends_with("invalid_grant")
                || e.to_string().ends_with("interaction_required")
            {
                sign_out(root)?;
            }
            return Err(e);
        }
    };
    remember(root, id, &reply)?;
    exchange(t, field(&reply, "access_token")?)
}
fn exchange(t: &Task, microsoft: &str) -> Result<Session> {
    t.status("Verifying Xbox account");
    let user=response(t,t.http.post("https://user.auth.xboxlive.com/user/authenticate").json(&json!({"Properties":{"AuthMethod":"RPS","SiteName":"user.auth.xboxlive.com","RpsTicket":format!("d={microsoft}")},"RelyingParty":"http://auth.xboxlive.com","TokenType":"JWT"})))?;
    let xsts=response(t,t.http.post("https://xsts.auth.xboxlive.com/xsts/authorize").json(&json!({"Properties":{"SandboxId":"RETAIL","UserTokens":[field(&user,"Token")?]},"RelyingParty":"rp://api.minecraftservices.com/","TokenType":"JWT"})))?;
    let hash = field(&xsts["DisplayClaims"]["xui"][0], "uhs")?;
    let xbox = field(&xsts, "Token")?;
    t.status("Checking Minecraft Java ownership");
    let game = response(
        t,
        t.http
            .post("https://api.minecraftservices.com/authentication/login_with_xbox")
            .json(&json!({"identityToken":format!("XBL3.0 x={hash};{xbox}")})),
    )?;
    let token = field(&game, "access_token")?;
    let ownership = response(
        t,
        t.http
            .get("https://api.minecraftservices.com/entitlements/mcstore")
            .bearer_auth(token),
    )?;
    if !ownership["items"]
        .as_array()
        .ok_or("Missing entitlements")?
        .iter()
        .any(|i| {
            matches!(
                i["name"].as_str(),
                Some("game_minecraft" | "product_minecraft")
            )
        })
    {
        return Err("This account has no recognized Minecraft Java entitlement".into());
    }
    let p = response(
        t,
        t.http
            .get("https://api.minecraftservices.com/minecraft/profile")
            .bearer_auth(token),
    )?;
    let name = field(&p, "name")?;
    let uuid = field(&p, "id")?;
    if !regex::Regex::new(r"^[A-Za-z0-9_]{1,16}$")?.is_match(name)
        || !regex::Regex::new(r"^[a-fA-F0-9]{32}$")?.is_match(uuid)
    {
        return Err("Invalid Minecraft profile".into());
    }
    Ok(Session {
        name: name.into(),
        uuid: uuid.into(),
        token: token.into(),
        offline: false,
    })
}
#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn offline_uuid() {
        let s = Session::offline("Notch").unwrap();
        assert_eq!(s.uuid, "b50ad385829d3141a2167e7d7539ba7f");
        assert!(Session::offline("bad name").is_err());
    }
    #[test]
    fn account_roundtrip() {
        let d = tempfile::tempdir().unwrap();
        remember(
            d.path(),
            "client-a",
            &json!({"refresh_token":"test-secret"}),
        )
        .unwrap();
        assert!(saved(d.path(), "client-b").unwrap().is_none());
        assert_eq!(saved(d.path(), "client-a").unwrap().unwrap(), "test-secret");
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            assert_eq!(
                std::fs::metadata(d.path().join("account.properties"))
                    .unwrap()
                    .permissions()
                    .mode()
                    & 0o777,
                0o600
            );
        }
        sign_out(d.path()).unwrap();
        assert!(saved(d.path(), "client-a").unwrap().is_none());
    }
}
