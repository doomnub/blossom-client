# Blossom Client

A local-first, Forge-free **Minecraft 1.8.9** PvP client with a pink identity,
customizable HUD, cosmetics, and a launcher for Linux and Windows.
Minecraft runs on Java 8; the native launcher uses Tauri 2, Rust, and TypeScript.

## Blossom 0.3.1 — a new look

- Charcoal launcher surfaces, vivid pink accents, consistent navigation icons,
  original block artwork, and layouts that adapt to smaller windows.
- An in-game module library with category filters, search, module cards, explicit
  toggle states, and separate settings buttons.
- Refreshed panels, sliders, and ordinary main/pause-menu buttons, plus a matching
  palette for the legacy launcher.

[Download 0.3.1](https://github.com/doomnub/blossom-client/releases/tag/v0.3.1) ·
[Release notes](docs/releases/v0.3.1.md) · [Changelog](CHANGELOG.md)

## Install and play

Download the ZIP for your platform from [Releases](https://github.com/doomnub/blossom-client/releases).
Extract the entire archive and use its included startup script. Native Linux and
Windows packages include only their own platform's startup script. A separate
Java/Swing legacy package is also available.

The launcher installs Minecraft 1.8.9 and a managed Java 8 runtime. Choose Microsoft
sign-in for authenticated servers or an offline username for singleplayer and
compatible offline servers. Microsoft sign-in requires a configured application
ID. Verification options, memory allocation, and game paths are available in Settings.

The **Skins & capes** page keeps imported skins in a permanent collection, provides
skin and arm-model previews, and offers capes owned by the signed-in Minecraft
account. **Logs** and **Changelog** are available from the sidebar.

See [native launcher setup](docs/NATIVE-LAUNCHER.md) and
[legacy launcher setup](docs/LAUNCHER.md).

## In-game features

Open Blossom with **Right Shift**. Use categories or type to search, toggle a
module, open its settings with the adjacent button, or edit your HUD layout.
Five preset slots save module settings and positions.

- FPS/frame statistics, CPS from attack/use bindings, keystrokes, ping,
  coordinates, armor/held items, resources, and other HUD displays.
- Zoom, FOV controls, sprint/sneak toggles, hotbar scroll lock, left-hand view,
  hit tilt, chat customization, and a local nickname hider.
- Free Look: tap Left Alt to toggle or hold it for a momentary third-person view.
- Visual-only XP bar and time controls; OptiFine-format custom sky resources
  from selected packs. See [custom sky support and limits](docs/CUSTOM-SKIES.md).
- Local capes, cape physics, and an OS-integrated Media Player display.
- Detailed benchmark capture and conservative, bounded rendering optimizations.
  See [performance documentation](docs/PERFORMANCE.md).

Blossom is still an alpha. Automated builds, Java 8 compatibility checks, real
1.8.9 class transformation/linkage checks, launcher browser tests, and native
launcher checks cover specific paths; they do not establish full graphical
in-game acceptance, multiplayer compatibility, or a measured FPS improvement.
The OptiFine mod itself is not supported by the vanilla adapter. Reading its sky
resource format does not require installing OptiFine. Experimental Linux rendering
paths remain restricted following earlier stability issues.

## Develop

Use a **JDK 17+** and Python 3.9+:

```sh
python3 scripts/build.py
```

On Windows, use `py scripts/build.py`. The pinned pipeline targets Java 8 and
excludes Minecraft binaries from the distributed client. `gradle build` delegates
to the same pipeline. Native launcher builds also require Node.js, Rust, and the
platform dependencies described in [BUILDING](docs/BUILDING.md) and
[NATIVE-LAUNCHER](docs/NATIVE-LAUNCHER.md).

[Feature matrix](docs/FEATURES.md) · [Architecture](docs/ARCHITECTURE.md) ·
[Configuration](docs/CONFIGURATION.md) · [Testing](docs/TESTING.md) ·
[Full specification](docs/SPECIFICATION.md) · [Licenses](LICENSES.md)
