use sha2::{Digest, Sha256};
fn main() {
    let root = std::path::PathBuf::from(std::env::var("CARGO_MANIFEST_DIR").unwrap()).join("../..");
    let version = std::fs::read_to_string(root.join("VERSION")).unwrap();
    let jar = root.join(format!("build/libs/blossom-client-{}.jar", version.trim()));
    println!("cargo:rerun-if-changed={}", jar.display());
    let bytes = std::fs::read(&jar)
        .expect("Run python scripts/build.py before building the Tauri launcher");
    std::fs::write(
        std::path::PathBuf::from(std::env::var("OUT_DIR").unwrap()).join("blossom-agent.jar"),
        &bytes,
    )
    .unwrap();
    println!(
        "cargo:rustc-env=BLOSSOM_AGENT_SHA={:x}",
        Sha256::digest(&bytes)
    );
    println!("cargo:rustc-env=BLOSSOM_VERSION={}", version.trim());
    tauri_build::build();
}
