import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import "./style.css";
import {
  initWardrobe,
  refreshWardrobe,
  wardrobeTab,
  wardrobeBusy,
  clearAccount,
} from "./wardrobe";
type Config = {
  verifyGame: boolean;
  verifyJava: boolean;
  javaAuto: boolean;
  java: string;
  game: string;
  clientId: string;
  offlineName: string;
  memory: number;
  width: number;
  height: number;
};
type Event = { kind: string; text: string };
const $ = <T extends HTMLElement>(id: string) =>
  document.getElementById(id) as T;
const input = (id: string) => $<HTMLInputElement>(id);
let config: Config,
  busy = false,
  running = false,
  remembered = false;
const log: string[] = [];
function append(text: string) {
  log.push(text.trimEnd());
  while (log.join("\n").length > 150000) log.shift();
  $("log").textContent = log.join("\n");
  $("log").scrollTop = $("log").scrollHeight;
}
function status(text: string, error = false) {
  $("status").textContent = text;
  $("status-light").classList.toggle("error", error);
  if (error) $("error-dot").hidden = false;
}
function update() {
  wardrobeBusy(busy);
  document
    .querySelectorAll<HTMLButtonElement>(
      "[data-action], [data-browse], #settings-form button[type=submit]",
    )
    .forEach((b) => (b.disabled = busy));
  document
    .querySelectorAll<HTMLInputElement>("input")
    .forEach((i) => (i.disabled = busy));
  input("java-path").disabled = busy || input("java-auto").checked;
  $<HTMLButtonElement>("play-online").disabled = busy || !remembered;
  $("cancel").hidden = !busy || running;
  $("progress").hidden = !busy || running;
  $("signout").hidden = !remembered;
}
function paint(c: Config) {
  config = c;
  input("verify-game").checked = c.verifyGame ?? true;
  input("verify-java").checked = c.verifyJava ?? true;
  input("java-auto").checked = c.javaAuto;
  input("java-path").value = c.java;
  input("game-path").value = c.game;
  input("client-id").value = c.clientId;
  input("offline-name").value = c.offlineName;
  input("memory").value = String(c.memory);
  input("width").value = String(c.width);
  input("height").value = String(c.height);
  memory();
  $("runtime-label").textContent = c.javaAuto
    ? "Managed Java 8"
    : "Custom Java runtime";
  update();
}
function read(): Config {
  return {
    ...config,
    verifyGame: input("verify-game").checked,
    verifyJava: input("verify-java").checked,
    javaAuto: input("java-auto").checked,
    java: input("java-path").value.trim(),
    game: input("game-path").value.trim(),
    clientId: input("client-id").value.trim(),
    offlineName: input("offline-name").value.trim(),
    memory: Number(input("memory").value),
    width: Number(input("width").value),
    height: Number(input("height").value),
  };
}
function memory() {
  $("memory-value").textContent = `${input("memory").value} MiB`;
}
async function refresh() {
  paint(await invoke<Config>("settings"));
  remembered = await invoke<boolean>("account");
  update();
}
async function save() {
  const oldClientId = config.clientId;
  config = read();
  await invoke("save_settings", { config });
  if (oldClientId !== config.clientId) clearAccount();
  remembered = await invoke<boolean>("account");
  update();
}
async function task(action: string) {
  if (busy) return;
  try {
    if (action !== "restore") await save();
    busy = true;
    running = false;
    update();
    status(action === "restore" ? "Restoring your account…" : "Working…");
    await invoke("task", { action });
  } catch (error) {
    status(String(error), true);
    append(String(error));
  } finally {
    busy = false;
    running = false;
    $<HTMLDialogElement>("device").close();
    await refresh().catch((e) => status(String(e), true));
    update();
  }
  if (["restore", "signin", "signout"].includes(action))
    await refreshWardrobe(remembered);
}
const titles: Record<string, [string, string]> = {
  play: ["LET'S PLAY", "Welcome home."],
  settings: ["MAKE IT YOURS", "Your setup."],
  wardrobe: ["YOUR SIGNATURE", "Skins & capes."],
  logs: ["A CLOSER LOOK", "Session activity."],
};
function tab(id: string) {
  wardrobeTab(id === "wardrobe");
  document
    .querySelectorAll<HTMLElement>(".page")
    .forEach((p) => (p.hidden = p.id !== id));
  document
    .querySelectorAll<HTMLElement>("[data-tab]")
    .forEach((b) => b.classList.toggle("active", b.dataset.tab === id));
  $("section-label").textContent = titles[id][0];
  $("section-title").textContent = titles[id][1];
  if (id === "logs") $("error-dot").hidden = true;
}
document
  .querySelectorAll<HTMLElement>("[data-tab]")
  .forEach((b) => (b.onclick = () => tab(b.dataset.tab!)));
document.querySelector<HTMLAnchorElement>(".brand")!.onclick = (e) => {
  e.preventDefault();
  tab("play");
};
document
  .querySelectorAll<HTMLElement>("[data-action]")
  .forEach((b) => (b.onclick = () => void task(b.dataset.action!)));
document.querySelectorAll<HTMLElement>("[data-browse]").forEach(
  (b) =>
    (b.onclick = () =>
      void invoke<string | null>("browse", {
        directory: b.dataset.browse === "game-path",
      })
        .then((path) => {
          if (path) input(b.dataset.browse!).value = path;
        })
        .catch((e) => status(String(e), true))),
);
$("settings-form").onsubmit = (e) => {
  e.preventDefault();
  void save()
    .then(() => status("Settings saved."))
    .catch((e) => status(String(e), true));
};
input("java-auto").onchange = update;
input("memory").oninput = memory;
$("cancel").onclick = () =>
  void invoke("cancel").then(() => status("Cancelling…"));
$("cancel-login").onclick = () => void invoke("cancel");
$<HTMLDialogElement>("device").oncancel = (e) => {
  e.preventDefault();
  void invoke("cancel");
};
$("open-login").onclick = () =>
  void invoke("open_login").catch((e) => status(String(e), true));
$("open-folder").onclick = () =>
  void invoke("open_folder").catch((e) => status(String(e), true));
$("clear-log").onclick = () => {
  log.length = 0;
  $("log").textContent = "";
};
async function start() {
  await listen<Event>("blossom", ({ payload: e }) => {
    switch (e.kind) {
      case "status":
        status(e.text);
        append(e.text);
        break;
      case "error":
        status(e.text, true);
        append(e.text);
        break;
      case "log":
        append(e.text);
        break;
      case "running":
        running = true;
        status(e.text);
        update();
        break;
      case "device": {
        const d = JSON.parse(e.text) as { code: string; url: string };
        $("device-code").textContent = d.code;
        $("device-url").textContent = d.url;
        $<HTMLDialogElement>("device").showModal();
        break;
      }
      case "account":
        clearAccount();
        remembered = e.text !== "Signed out";
        $("account-label").textContent = remembered ? e.text : "Not signed in";
        $("account-title").textContent = remembered
          ? `Good to see you, ${e.text}.`
          : "Pick up where you left off.";
        $("account-description").textContent = remembered
          ? "Your account is ready. Let’s get you into Minecraft."
          : "Sign in to play on authenticated servers.";
        $<HTMLDialogElement>("device").close();
        update();
        break;
    }
  });
  await refresh();
  if (remembered) await task("restore");
  else await refreshWardrobe(false);
}
initWardrobe(status, (value) => {
  busy = value;
  update();
});
void start().catch((e) => status(`Could not load Blossom: ${String(e)}`, true));
