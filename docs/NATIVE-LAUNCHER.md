# Blossom native launcher 0.2.6

Use the ZIP matching your platform: Windows x64 or Linux x64. Extract the whole ZIP.
On Windows, run `Start-Blossom.bat`; on Linux, run `bash start-blossom.sh`.
Only the README and start scripts are at the top level. The launcher is in `app/`.

The native launcher uses Tauri 2, Rust and TypeScript. Minecraft stays Java 8.
It uses the same `~/.blossom/launcher.properties`, `account.properties`, runtime
and cache folders as 0.2.4. The existing game directory, worlds, capes and client
profiles are not relocated. Close the old launcher before opening the new one.

## System dependencies

- Fedora: install the WebKitGTK 4.1 runtime, e.g. `sudo dnf install webkit2gtk4.1`.
- Windows 11: Microsoft Edge WebView2 Runtime must be available (normally installed).
- Minecraft's own graphics/audio drivers and Java runtime prerequisites still apply.
- Minecraft Java 8 is installed automatically on first use unless you choose a custom executable.
- First installation and Microsoft sign-in require internet. A verified cached
  installation can be used offline with a local username. Authenticated servers need sign-in.

## Settings and accounts

Configure your Microsoft public-client application ID in Settings. Its registration
must permit personal accounts and the device-code flow, and may require Minecraft
API approval. No other launcher's ID is bundled. Existing refresh credentials are
matched to their original ID. Changing the ID requires signing in for the new ID.

Credentials stay in the same owner-only local account file as 0.2.4. Sign out removes
that file. Logs are session-only. Neither credentials nor access tokens are sent to
the frontend; the game token is delivered through stdin, not process arguments.

## Validation

See `docs/VALIDATION-0.2.6.md` and `docs/LAUNCHER-MIGRATION.md` in the source repository.
The legacy Swing source and build remain available until desktop acceptance is complete.
The native ZIP does not launch Swing in the background.

## Build from source

Install a JRE 17+, Python 3, Node.js 22, stable Rust, and Tauri 2 platform development
prerequisites. Then from the repository root:

```
python scripts/build.py
cargo test --locked --manifest-path launcher-tauri/core/Cargo.toml
cd launcher-tauri
npm ci
npm run build
cd ..
cargo build --locked --release --features custom-protocol --manifest-path launcher-tauri/src-tauri/Cargo.toml
python scripts/package_native.py
```
