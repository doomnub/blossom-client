import { invoke } from "@tauri-apps/api/core";
import { SkinViewer } from "skinview3d";
export type Skin = {
  id: string;
  name: string;
  variant: "classic" | "slim";
  image: string;
};
type Cape = { id: string; name: string; active: boolean; image: string };
type Wardrobe = {
  name: string;
  uuid: string;
  currentSkin: string | null;
  skins: Skin[];
  capes: Cape[];
};
const el = <T extends HTMLElement>(id: string) =>
  document.getElementById(id) as T;
let skins: Skin[] = [],
  account: Wardrobe | null = null,
  selectedSkin = "",
  selectedCape = "";
let viewer: SkinViewer | null = null,
  visible = false,
  working = false,
  outerBusy = false;
let notify: (message: string, error?: boolean) => void;
let setBusy: (value: boolean) => void;
let generation = 0;
const image = (url: string) =>
  new Promise<HTMLImageElement>((resolve, reject) => {
    const i = new Image();
    i.onload = () => resolve(i);
    i.onerror = () => reject(new Error("Could not decode skin preview"));
    i.src = url;
  });
// Small cards are static 2D canvases; only the selected skin gets a WebGL viewer.
export async function thumbnail(
  canvas: HTMLCanvasElement,
  skin: Skin,
  head = false,
) {
  const texture = await image(skin.image);
  const ctx = canvas.getContext("2d")!;
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.imageSmoothingEnabled = false;
  if (head) {
    ctx.drawImage(texture, 8, 8, 8, 8, 0, 0, canvas.width, canvas.height);
    ctx.drawImage(texture, 40, 8, 8, 8, 0, 0, canvas.width, canvas.height);
    return;
  }
  const s = canvas.height / 34,
    x = (canvas.width - 16 * s) / 2,
    y = s;
  const draw = (
    u: number,
    v: number,
    w: number,
    h: number,
    dx: number,
    dy: number,
  ) => ctx.drawImage(texture, u, v, w, h, x + dx * s, y + dy * s, w * s, h * s);
  const arm = skin.variant === "slim" ? 3 : 4;
  draw(8, 8, 8, 8, 4, 0);
  draw(20, 20, 8, 12, 4, 8);
  draw(44, 20, arm, 12, 4 - arm, 8);
  draw(4, 20, 4, 12, 4, 20);
  if (texture.height === 64) {
    draw(36, 52, arm, 12, 12, 8);
    draw(20, 52, 4, 12, 8, 20);
  } else {
    draw(44, 20, 4, 12, 12, 8);
    draw(4, 20, 4, 12, 8, 20);
  }
  draw(40, 8, 8, 8, 4, 0);
  if (texture.height === 64) {
    draw(20, 36, 8, 12, 4, 8);
    draw(44, 36, arm, 12, 4 - arm, 8);
    draw(52, 52, arm, 12, 12, 8);
    draw(4, 36, 4, 12, 4, 20);
    draw(4, 52, 4, 12, 8, 20);
  }
}
function controls() {
  el<HTMLButtonElement>("use-skin").disabled =
    outerBusy ||
    working ||
    !account ||
    !selectedSkin ||
    selectedSkin === account.currentSkin;
  el<HTMLButtonElement>("use-cape").disabled =
    outerBusy ||
    working ||
    !account ||
    selectedCape === (account.capes.find((c) => c.active)?.id ?? "");
  for (const id of ["add-skin", "refresh-wardrobe", "skin-model"])
    (el(id) as HTMLButtonElement).disabled = outerBusy || working;
  document
    .querySelectorAll<HTMLButtonElement>(".texture-card")
    .forEach((b) => (b.disabled = outerBusy || working));
}
function preview() {
  viewer?.dispose();
  viewer = null;
  const skin = skins.find((s) => s.id === selectedSkin),
    cape = account?.capes.find((c) => c.id === selectedCape);
  el("preview-name").textContent = skin?.name ?? "Choose a skin";
  el("preview-model").textContent = skin
    ? `${skin.variant === "slim" ? "Slim" : "Classic"} arms`
    : "Import a PNG to start your collection";
  if (!visible || !skin) return;
  const canvas = el<HTMLCanvasElement>("skin-preview");
  try {
    viewer = new SkinViewer({
      canvas,
      width: 280,
      height: 340,
      skin: skin.image,
      model: skin.variant === "slim" ? "slim" : "default",
      cape: cape?.image,
      pixelRatio: Math.min(window.devicePixelRatio, 2),
    });
    viewer.controls.enableZoom = false;
    viewer.controls.enablePan = false;
    viewer.playerObject.rotation.y = 0.35;
    viewer.autoRotate = false;
    el("preview-help").textContent =
      "Drag to rotate. Previewing does not change your account.";
  } catch {
    // Static front/back selection still works on machines without WebGL.
    canvas.width = 160;
    canvas.height = 272;
    void thumbnail(canvas, skin);
    el("preview-help").textContent =
      "3D preview unavailable. Showing the skin from the front.";
  }
}
function card(
  title: string,
  selected: boolean,
  current: boolean,
): HTMLButtonElement {
  const button = document.createElement("button");
  button.className = "texture-card";
  button.classList.toggle("selected", selected);
  button.setAttribute("aria-pressed", String(selected));
  const label = document.createElement("span");
  label.textContent = title;
  button.append(label);
  if (current) {
    const tag = document.createElement("small");
    tag.textContent = "CURRENT";
    button.append(tag);
  }
  return button;
}
function paint() {
  const list = el("skin-list");
  list.replaceChildren();
  for (const skin of skins) {
    const button = card(
      skin.name,
      skin.id === selectedSkin,
      skin.id === account?.currentSkin,
    );
    const canvas = document.createElement("canvas");
    canvas.width = 80;
    canvas.height = 136;
    canvas.setAttribute("aria-label", `${skin.name}, ${skin.variant} arms`);
    button.prepend(canvas);
    void thumbnail(canvas, skin).catch(() => {
      canvas.setAttribute("aria-label", "Preview unavailable");
    });
    button.onclick = () => {
      selectedSkin = skin.id;
      paint();
    };
    list.append(button);
  }
  el("skin-empty").hidden = skins.length > 0;
  const capes = el("cape-list");
  capes.replaceChildren();
  if (account) {
    const none = card(
      "No cape",
      selectedCape === "",
      !account.capes.some((c) => c.active),
    );
    none.onclick = () => {
      selectedCape = "";
      paint();
    };
    capes.append(none);
    for (const cape of account.capes) {
      const button = card(cape.name, cape.id === selectedCape, cape.active);
      const canvas = document.createElement("canvas");
      canvas.width = 60;
      canvas.height = 96;
      button.prepend(canvas);
      void image(cape.image).then((i) => {
        const ctx = canvas.getContext("2d")!;
        ctx.imageSmoothingEnabled = false;
        ctx.drawImage(i, 1, 1, 10, 16, 0, 0, 60, 96);
      });
      button.onclick = () => {
        selectedCape = cape.id;
        paint();
      };
      capes.append(button);
    }
  }
  el("cape-empty").hidden = !!account?.capes.length;
  el("cape-empty").textContent = account
    ? "This Minecraft account does not own any capes."
    : "Sign in to see the capes owned by your Minecraft account.";
  el("wardrobe-account").textContent = account
    ? `Account: ${account.name} · Changes appear after restarting Minecraft.`
    : "Your collection stays here. Sign in to apply skins or owned capes.";
  preview();
  controls();
}
async function request(action: string, id?: string) {
  if (working || outerBusy) return;
  working = true;
  setBusy(true);
  controls();
  const mine = ++generation;
  try {
    const value = await invoke<Wardrobe | Skin[]>("wardrobe", {
      action,
      id: id ?? null,
      variant: el<HTMLSelectElement>("skin-model").value,
    });
    if (mine !== generation) return;
    if (Array.isArray(value)) {
      const previous = new Set(skins.map((s) => s.id));
      skins = value;
      selectedSkin = skins.find((s) => !previous.has(s.id))?.id ?? selectedSkin;
    } else {
      account = value;
      skins = value.skins;
      selectedSkin = value.currentSkin ?? skins[0]?.id ?? "";
      selectedCape = value.capes.find((c) => c.active)?.id ?? "";
      const current = skins.find((s) => s.id === value.currentSkin);
      const head = el<HTMLCanvasElement>("account-head");
      head.hidden = !current;
      el("account-flower").hidden = !!current;
      if (current) await thumbnail(head, current, true);
    }
    if (!skins.some((s) => s.id === selectedSkin))
      selectedSkin = skins[0]?.id ?? "";
    paint();
    if (action === "skin" || action === "cape")
      notify(
        "Account appearance updated. Restart Minecraft to see it in game.",
      );
    else if (action === "import")
      notify(
        "Skin collection saved. Select a skin and choose Use skin to apply it.",
      );
  } catch (e) {
    notify(String(e), true);
  } finally {
    working = false;
    setBusy(false);
    controls();
  }
}
export function wardrobeBusy(value: boolean) {
  outerBusy = value;
  controls();
}
export function wardrobeTab(show: boolean) {
  visible = show;
  if (show) paint();
  else {
    viewer?.dispose();
    viewer = null;
  }
}
export function clearAccount() {
  generation++;
  account = null;
  selectedCape = "";
  el("account-head").hidden = true;
  el("account-flower").hidden = false;
  el<HTMLCanvasElement>("account-head")
    .getContext("2d")
    ?.clearRect(0, 0, 32, 32);
  paint();
}
export async function refreshWardrobe(signedIn: boolean) {
  if (signedIn) await request("refresh");
  else {
    clearAccount();
    await request("collection");
  }
}
export function initWardrobe(message: typeof notify, busy: typeof setBusy) {
  notify = message;
  setBusy = busy;
  el("add-skin").onclick = () => void request("import");
  el("refresh-wardrobe").onclick = () =>
    void request(account ? "refresh" : "collection");
  el("use-skin").onclick = () => void request("skin", selectedSkin);
  el("use-cape").onclick = () => void request("cape", selectedCape);
  document.addEventListener("visibilitychange", () => {
    if (document.hidden) {
      viewer?.dispose();
      viewer = null;
    } else if (visible) preview();
  });
}
