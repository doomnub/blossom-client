# Validation record — 2026-10-22

Environment: Linux x86_64, OpenJDK 17.0.20, Eclipse ECJ 3.33.0, Python 3.12.
Minecraft compile/transform fixture: official 1.8.9 client SHA-1
`3870888a6c3d349d3771a3e9d16c9bf5e076b908`.

Passed locally:

- Java compilation to classfile version 52 (Java 8).
- 43 core assertions: timestamp expiration, bounded CPS storage, actual slow-tail
  FPS means, frame-duration boundaries, ping-pong playback, invalid durations,
  profile round-trip, previous-version backup, NaN normalization, traversal
  rejection, listener isolation/unsubscribe, module state, PNG sheet splitting,
  invalid image dimensions.
- Transformation of real Minecraft/GuiIngame/AbstractClientPlayer classes.
- Hook instruction presence including mouse event observation.
- Packaged startup agent under JVM `-Xverify:all`: all four transformed game
  classes and all ten relocated integration classes loaded and linked without
  class initialization. This caught and resolved the signed-package collision.

These tests do NOT initialize a window, authenticate, load a world, render a cape,
or connect to a server. No gameplay/performance claim follows from them.

## Concrete outstanding environment blockers

No Xvfb/X11 display server or graphical Minecraft environment is installed. No
user-authenticated launcher/account session is available in the build container.
The container has Java 17, not the target Java 8 gameplay runtime. Windows and
Fedora KDE/Wayland systems are not available for interactive tests.

## Manual acceptance gate (all currently NOT RUN)

- Start vanilla 1.8.9 with Java 8 and Blossom. Verify hook messages and clean quit.
- Singleplayer: create/load a world, move, sprint, sneak, bridge, mine and attack.
- Menu: Right Shift, search, toggles, Escape, reopening, resize and GUI scaling.
- HUD: drag/grid, restart persistence, FPS sampling, both CPS buttons, ping unknown.
- Zoom/gamma: enable/disable, open inventory while held, disconnect/reconnect.
- Capes: static/sequence/strip, all frame boundaries, transparency, third-person,
  inventory, sneak, armor, sprint, resource reload, repeated switching, invalid PNG.
- Profiles: corrupt default is preserved; preset round-trip; filesystem write error.
- Multiplayer: first an authorized test server, then check relevant public-server
  rules before use. No public server has been tested or approved.
- Cross-platform: Fedora KDE XWayland/window/fullscreen, openSUSE, Windows, high DPI.

Do not mark the foundation milestone complete until these relevant gates pass.

Continuation checkpoint: key latches, bounds/color/style persistence and HUD geometry
checks pass. Crosshair/keybinding/main-loop hooks pass bytecode and JVM checks.
Attempting to install a virtual display failed because the container denies setgroups/setuid.
Graphical acceptance remains NOT RUN.

Cape continuation: monotonic pause/speed continuity, horizontal sheets, staged
validated import, duplicate preservation, rename, recoverable removal and traversal
checks pass. Full graphical cape acceptance remains NOT RUN.

## Standalone launcher alpha

21 checks cover path traversal, native archive extraction, bounded responses,
OS rules, launch argument boundaries, session expiry, token redaction, invalid
application IDs and download host/checksum validation. A headless Swing render
was inspected. Live auth, graphical gameplay and Windows desktop testing remain
outstanding.

Installer smoke test completed on Linux x86_64 using the real pinned Mojang
manifest: client, selected libraries, native archives, all 734 assets and logging
configuration downloaded and verified. The workspace HTTP proxy was required.
This verifies installation, not game startup.

## 0.2.0-alpha.2 offline launch

32 launcher checks now include offline username validation, the vanilla-compatible
name-based UUID, case sensitivity, legacy session arguments, expiry behavior,
unchanged offline logs and verified cached downloads. Online-session checks remain.
A full installed cache was checked with HTTPS_PROXY pointing to an unreachable
local port: all 734 assets and required game files verified without network.
The updated Swing layout was rendered headlessly. Graphical game startup remains
unverified in this environment.

## 0.2.1 automatic Java installation

43 core assertions and 43 launcher checks pass. Added runtime platform selection,
archive traversal/root validation, extraction, cancellation and metadata checks.
On Linux x86_64, both Java and Python installers downloaded and checksum-verified
Azul Zulu 8u504; the installed executable reported Java 8 and 64-bit. Reuse with
an unreachable HTTPS proxy succeeded. Launcher tests and a headless Swing render
also ran using the downloaded Java 8. Windows CI checks the Java installer and
the packaged PowerShell bootstrap. This does not establish graphical gameplay.
