#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]
// A release binary must serve embedded assets, never the development server.
#[cfg(all(not(debug_assertions), not(feature = "custom-protocol")))]
compile_error!("Release builds require --features custom-protocol (embedded frontend)");
use blossom_launcher_core::{self as core, config::Config, Task};
use std::{
    path::PathBuf,
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc, Mutex,
    },
};
use tauri::{Emitter, Manager};
use tauri_plugin_dialog::DialogExt;
use tauri_plugin_opener::OpenerExt;
#[derive(serde::Serialize, Clone)]
struct Event {
    kind: String,
    text: String,
}
struct State {
    root: PathBuf,
    busy: AtomicBool,
    running: AtomicBool,
    cancel: Mutex<Option<Arc<AtomicBool>>>,
}
fn emit(app: &tauri::AppHandle, kind: &str, text: &str) {
    let _ = app.emit(
        "blossom",
        Event {
            kind: kind.into(),
            text: text.into(),
        },
    );
}
#[tauri::command]
fn settings(state: tauri::State<State>) -> Result<Config, String> {
    Config::load(&state.root).map_err(|e| e.to_string())
}
#[tauri::command]
fn save_settings(config: Config, state: tauri::State<State>) -> Result<(), String> {
    if state.busy.load(Ordering::SeqCst) {
        return Err("Wait for the current task to finish".into());
    }
    config.save(&state.root).map_err(|e| e.to_string())
}
#[tauri::command]
fn account(state: tauri::State<State>) -> Result<bool, String> {
    let c = Config::load(&state.root).map_err(|e| e.to_string())?;
    core::auth::saved(&state.root, &c.client_id)
        .map(|s| s.is_some())
        .map_err(|e| e.to_string())
}
#[tauri::command]
fn cancel(state: tauri::State<State>) {
    if !state.running.load(Ordering::SeqCst) {
        if let Ok(guard) = state.cancel.lock() {
            if let Some(c) = guard.as_ref() {
                c.store(true, Ordering::SeqCst);
            }
        }
    }
}
#[tauri::command]
async fn browse(app: tauri::AppHandle, directory: bool) -> Result<Option<String>, String> {
    tauri::async_runtime::spawn_blocking(move || {
        let dialog = app.dialog().file();
        let selected = if directory {
            dialog.blocking_pick_folder()
        } else {
            dialog.blocking_pick_file()
        };
        selected
            .map(|p| {
                p.into_path()
                    .map(|p| p.to_string_lossy().into_owned())
                    .map_err(|e| e.to_string())
            })
            .transpose()
    })
    .await
    .map_err(|e| e.to_string())?
}
#[tauri::command]
fn open_folder(app: tauri::AppHandle, state: tauri::State<State>) -> Result<(), String> {
    app.opener()
        .open_path(state.root.to_string_lossy(), None::<&str>)
        .map_err(|e| e.to_string())
}
#[tauri::command]
fn open_login(app: tauri::AppHandle) -> Result<(), String> {
    app.opener()
        .open_url("https://www.microsoft.com/link", None::<&str>)
        .map_err(|e| e.to_string())
}
fn agent(root: &std::path::Path) -> core::Result<PathBuf> {
    use sha2::{Digest, Sha256};
    let bytes = include_bytes!(concat!(env!("OUT_DIR"), "/blossom-agent.jar"));
    if hex::encode(Sha256::digest(bytes)) != env!("BLOSSOM_AGENT_SHA") {
        return Err("Embedded agent checksum mismatch".into());
    }
    let dir = root.join("client");
    std::fs::create_dir_all(&dir)?;
    let file = dir.join(format!("blossom-client-{}.jar", env!("BLOSSOM_VERSION")));
    if !file.exists() || core::digest(&file, 64)? != env!("BLOSSOM_AGENT_SHA") {
        let temp = dir.join("blossom-agent.tmp");
        std::fs::write(&temp, bytes)?;
        if file.exists() {
            std::fs::remove_file(&file)?;
        }
        std::fs::rename(temp, &file)?;
    }
    Ok(file)
}
fn work(app: &tauri::AppHandle, action: &str) -> core::Result<()> {
    let state = app.state::<State>();
    let root = &state.root;
    let mut c = Config::load(root)?;
    c.validate()?;
    let app2 = app.clone();
    let task = Task::new(move |kind, text| {
        if kind == "running" {
            app2.state::<State>().running.store(true, Ordering::SeqCst);
        }
        emit(&app2, kind, text);
    })?;
    *state.cancel.lock().map_err(|_| "Task lock failed")? = Some(task.cancel.clone());
    match action {
        "signout" => {
            core::auth::sign_out(root)?;
            emit(app, "account", "Signed out");
            return Ok(());
        }
        "signin" => {
            let s = core::auth::sign_in(&task, root, &c.client_id)?;
            emit(app, "account", &s.name);
            return Ok(());
        }
        "restore" => {
            let s = core::auth::refresh(&task, root, &c.client_id)?;
            emit(app, "account", &s.name);
            return Ok(());
        }
        "java" | "repair" | "online" | "offline" => {}
        _ => return Err("Unknown task".into()),
    }
    let session = match action {
        "online" => Some(core::auth::refresh(&task, root, &c.client_id)?),
        "offline" => Some(core::auth::Session::offline(&c.offline_name)?),
        _ => None,
    };
    let java = if c.java_auto || action == "java" {
        let p = core::install::java(&task, root)?;
        c.java = p.to_string_lossy().into();
        c.save(root)?;
        p
    } else {
        PathBuf::from(&c.java)
    };
    if action == "java" {
        task.status("Java 8 is ready");
        return Ok(());
    }
    let game = std::path::absolute(&c.game)?;
    let installed = core::install::install(&task, root, &game)?;
    let agent = agent(root)?;
    if let Some(session) = session {
        core::install::verify_java(&java)?;
        let args = core::launch::arguments(&java, &agent, &game, &installed, &session, &c)?;
        let exit = core::launch::run(&task, &args, &game, session)?;
        if exit != 0 {
            return Err(format!("Minecraft exited with code {exit}; check Logs").into());
        }
    } else {
        task.status("Installation verified. Ready to play.");
    }
    Ok(())
}
#[tauri::command]
async fn task(app: tauri::AppHandle, action: String) -> Result<(), String> {
    if app.state::<State>().busy.swap(true, Ordering::SeqCst) {
        return Err("Another task is already running".into());
    }
    let cloned = app.clone();
    let result = tauri::async_runtime::spawn_blocking(move || {
        work(&cloned, &action).map_err(|e| e.to_string())
    })
    .await
    .map_err(|e| e.to_string())
    .and_then(|r| r);
    let state = app.state::<State>();
    state.running.store(false, Ordering::SeqCst);
    state.busy.store(false, Ordering::SeqCst);
    if let Ok(mut c) = state.cancel.lock() {
        *c = None;
    }
    if let Err(e) = &result {
        emit(&app, "error", e);
    }
    emit(&app, "done", "");
    result
}
fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_single_instance::init(|app, _, _| {
            if let Some(w) = app.get_webview_window("main") {
                let _ = w.set_focus();
            }
        }))
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_opener::init())
        .setup(|app| {
            let root = app.path().home_dir()?.join(".blossom");
            std::fs::create_dir_all(&root)?;
            app.manage(State {
                root,
                busy: AtomicBool::new(false),
                running: AtomicBool::new(false),
                cancel: Mutex::new(None),
            });
            Ok(())
        })
        .on_window_event(|window, event| {
            if let tauri::WindowEvent::CloseRequested { api, .. } = event {
                let state = window.state::<State>();
                if state.busy.load(Ordering::SeqCst) {
                    api.prevent_close();
                    emit(
                        window.app_handle(),
                        "status",
                        if state.running.load(Ordering::SeqCst) {
                            "Close Minecraft before closing Blossom"
                        } else {
                            "Cancel the active task before closing Blossom"
                        },
                    );
                }
            }
        })
        .invoke_handler(tauri::generate_handler![
            settings,
            save_settings,
            account,
            cancel,
            browse,
            open_folder,
            open_login,
            task
        ])
        .run(tauri::generate_context!())
        .expect("Blossom desktop runtime failed");
}
