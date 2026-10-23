# Blossom Launcher — 0.2.2

Standalone dark/pink launcher for the Forge-free Blossom Minecraft 1.8.9 client.
Supported targets: Windows and Linux, x86_64. Gameplay remains experimental.

## Start — Java installs automatically

1. Extract the release ZIP into a folder you can write to.
2. On Fedora/openSUSE/Linux, run `bash start-blossom.sh` from a desktop terminal.
   Python 3 is required; Java does not need to be preinstalled.
   On Windows, run `Start-Blossom.bat` (uses built-in Windows PowerShell).
3. The start script downloads Azul Zulu **Java 8u504**, verifies its SHA-256,
   installs the matching 64-bit runtime, and opens Blossom with it. The first run
   requires internet. Subsequent runs reuse the installed runtime offline.
4. To play offline, enter an **Offline username** and press **Play offline**.
   No Microsoft account or application ID is needed. Game files download on the
   first launch. Keep the username unchanged to keep your local player identity.
5. To sign in with Microsoft, configure your registered public-client application
   ID in Settings. A project-owned registration is not supplied. It must support
   personal Microsoft accounts and device-code sign-in; Minecraft service access
   may require approval. Sign in, then press Play. Authenticated servers such as
   Hypixel require this account sign-in.

## Where Java goes

Blossom installs an app-private runtime. It does not change system Java, PATH,
registry settings or package-manager installations, and needs no administrator.

| System | Managed executable |
| --- | --- |
| Linux | `~/.blossom/runtimes/zulu8.96.0.205-ca-jre8.0.504-linux_x64/bin/java` |
| Windows | `%USERPROFILE%\.blossom\runtimes\zulu8.96.0.205-ca-jre8.0.504-win_x64\bin\java.exe` |

For example, with Linux user `Qaun`, the root is `/home/Qaun/.blossom/runtimes/`.
The launcher checks that the executable reports **Java 8 and 64-bit**, displays
its selected path in Settings/logs, and saves that path in
`~/.blossom/launcher.properties`. Old launcher settings migrate to automatic Java
management by default. **Install / check Java 8** checks or repairs the runtime.
**Install / repair** also installs Java before checking game files.

To use your own runtime for Minecraft, uncheck **Install and select Java 8
automatically**, browse to its `bin/java` or `bin/java.exe`, and Save settings.
It must be 64-bit Java 8. Running the launcher JAR directly still requires an
existing Java runtime; the supplied start scripts handle installation beforehand.

Linux needs a graphical desktop/XWayland and the native libraries used by Java
AWT/LWJGL. If the launcher reports missing graphical support, run from a desktop
terminal and use the supplied script rather than a headless system Java.
macOS, ARM and 32-bit systems are not supported by this adapter.

## Files and sessions

- Downloaded game files and runtime archives are checksum-verified. Java installs
  in a staging folder and is validated before becoming the active runtime.
- Game data defaults to `~/.blossom/instances/default`; settings/cache live in
  `~/.blossom`. Missing or damaged game files require internet to repair.
- Modern Log4j 2.25.5 replaces the historical logging libraries.
- Microsoft tokens remain in memory. The game receives the token through stdin,
  not its command line. Sign in again after restarting Blossom.
- Offline and authenticated profiles have different UUIDs and can have different
  player data. Offline mode cannot join servers requiring account authentication.
- Close Minecraft before closing Blossom. Cancel stops installation/sign-in.
- The runtime archive includes its original vendor licenses. Game and Java
  binaries are downloaded separately, not bundled in the release ZIP.
- Right Shift opens the client menu in-game. OptiFine/mod loaders are unsupported.

Existing authenticated launchers may use the separate agent JAR with
`-javaagent:/absolute/path/blossom-client-0.2.2.jar` and vanilla 1.8.9 on Java 8.

## Verification limits

Compilation, core/launcher tests, runtime installation, offline reuse and vanilla
bytecode checks are automated. A headless render checks Swing layout. Graphical
Minecraft gameplay and live Microsoft sign-in remain unverified. See the
repository's docs/TESTING.md for the detailed record.
