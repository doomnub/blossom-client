# Blossom Launcher — 0.2.0-alpha.1

Standalone dark/pink launcher for the Forge-free Blossom Minecraft 1.8.9 client.
Supported targets: Windows and Linux, x86_64. This is an early prerelease.

## Start

1. Install Java 8 (64-bit) for Minecraft. The launcher itself runs on Java 8 or newer.
2. Extract the release ZIP. On Windows run `Start-Blossom.bat`; on Linux run
   `sh start-blossom.sh`. Alternatively: `java -jar blossom-launcher-0.2.0-alpha.1.jar`.
3. In Settings select your Java 8 `bin/java` (`java.exe` on Windows), game folder,
   memory allocation and window dimensions. Save settings.
4. Configure Blossom's registered Microsoft public-client application ID. A
   project-owned registration is **not supplied in this alpha**. The application
   must allow personal Microsoft accounts and device-code/public-client sign-in;
   Minecraft API access may require approval. `BLOSSOM_MICROSOFT_CLIENT_ID` can
   provide the initial value. Do not reuse another launcher's client ID.
5. Sign in with Microsoft. Open the displayed Microsoft URL and enter the code.
   Passwords stay on Microsoft's site. A Minecraft Java entitlement is required.
6. Press Play. The launcher checks/downloads vanilla 1.8.9, libraries, assets and
   natives, extracts its bundled Blossom agent, and starts the game directly.

Install / repair works without signing in. Right Shift opens Blossom in-game.
For existing authenticated launchers, use the separate agent JAR and the
`-javaagent:/absolute/path/blossom-client-0.2.0-alpha.1.jar` JVM argument with
vanilla 1.8.9 and Java 8. OptiFine and other mod loaders are unsupported.

## Behavior

- SHA checks on downloaded files and bundled agent; temporary download files.
- Modern Log4j 2.25.5 replaces the historical logging libraries.
- Microsoft/Xbox/Minecraft session tokens remain in memory. Sign in again after
  restarting. The game receives its token via stdin, not its command line.
- Launcher settings/cache live in `~/.blossom`; game defaults to
  `~/.blossom/instances/default`. No Minecraft binaries ship in this release.
- Cancel stops installation/sign-in. Close Minecraft before closing the launcher.
- HTTPS_PROXY is supported for HTTP CONNECT proxies without credentials.
- No automatic update installer, bundled Java, native executable, code signing,
  persisted account vault, macOS/ARM support or claimed performance gain yet.

## Verification limits

Compilation, core tests, launcher tests and real vanilla bytecode verification
are automated. A headless image checks launcher layout. Live Microsoft sign-in,
Java 8 graphical gameplay, multiplayer and Windows desktop operation remain
unverified. A configured application ID is necessary to complete sign-in.
See the repository's docs/TESTING.md for the installer smoke-test result.
