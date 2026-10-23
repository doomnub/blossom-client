# Native launcher parity audit

The Java Minecraft agent remains independent. `launcher-tauri/core` implements the
launcher backend in Rust; `src-tauri` provides desktop commands and the TypeScript
frontend consumes events. No Swing process is spawned by the native launcher.

| Existing behavior | Native implementation |
| --- | --- |
| Microsoft device authorization | `auth::sign_in`, including pending/slow-down/expiry |
| Xbox/XSTS/Minecraft ownership/profile | `auth::exchange` |
| Persistent login and refresh rotation | Existing `~/.blossom/account.properties`; owner-only writes |
| Offline username and name-based UUID | `Session::offline` |
| Application ID change invalidates remembered account | Saved credentials matched to ID |
| Sign out | Removes saved account file |
| Install/repair and cached offline reuse | `install::install`, checksum verification on each run |
| Secure Log4j replacement | Same pinned 2.25.5 artifacts as Swing |
| Managed Java 8 | Same pinned platform manifests and runtime folders |
| Custom Java path | Preserved; 64-bit Java 8 check before launch |
| Game directory, RAM, width/height | Existing `launcher.properties` keys, bounded values |
| Agent injection and game session transport | Embedded verified agent; stdin token transport |
| Task cancellation | Cooperative cancellation, partial files never promoted |
| Logs/progress/running status | Bounded session-only log; process stdout and stderr drained |
| Browse and open folder | Native Tauri dialogs/opener, backend-restricted commands |
| Single instance and close behavior | Native single-instance plugin; active task/game blocks close |
| Existing Minecraft options, worlds and capes | Game directory reused without rewriting these files |

Settings read Java Properties escapes (including Windows paths and Unicode). Saving
retains unknown properties. Refresh tokens use the same protected on-disk format
as 0.2.4: POSIX 0600 or Windows owner ACL. They are not encrypted at rest. No login
credentials are exposed through frontend commands or stored in frontend state.

The old Swing launcher remains in `launcher/` for migration validation. Do not run
both launchers simultaneously against the same profile. Native builds require the
platform's WebView runtime (WebKitGTK 4.1 on Linux, WebView2 on Windows).

Validation status is tracked in `VALIDATION-0.2.5.md`; compiling the frontend does
not establish native WebView or authenticated game-launch correctness.
