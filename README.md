# Blossom Client

An original, local-first, Forge-free **Minecraft 1.8.9** PvP client in development.
Pink by default. No Lunar services, store, account system or bundled game files.

**This is an early compiled checkpoint, not a finished Lunar-equivalent client.**
The full requested scope is preserved in [SPECIFICATION](docs/SPECIFICATION.md).
Most advanced features are still planned. No graphical game launch, multiplayer,
OptiFine compatibility or performance improvement is claimed yet.

## Try 0.2.4

The custom launcher installs vanilla 1.8.9, embeds the Blossom agent, and offers
Microsoft device-code sign-in or offline play with a local username. Download the ZIP from [Releases](https://github.com/doomnub/blossom-client/releases)
and follow [launcher setup](docs/LAUNCHER.md). Offline play needs no account or
application ID. Microsoft sign-in requires a configured application ID. The game
uses Java 8, installed and selected automatically by the supplied start scripts.
Live sign-in and gameplay are not yet verified. An existing authenticated launcher can also load the agent.

Implemented paths, pending graphical acceptance:

- Right Shift module menu with search, toggles and two profile slots.
- Draggable normalized HUD positions with optional grid snapping/backgrounds.
- FPS slow-tail statistics, left/right CPS, keystrokes, server ping, coordinates,
  heading, clock and heap-memory display.
- Smooth hold-C zoom with temporary scroll adjustment, configurable FOV, and gamma control.
- Shared HUD styling, compact labels, optional potion levels, and persistent sprint toggle.
- Local static PNG, horizontal/vertical spritesheet and PNG-sequence cape selection; worker
  decoding, cached textures, per-frame durations and ping-pong animation.

Validated here: compilation, 74 core assertions and 49 launcher checks, transformation of real 1.8.9
classes, and full JVM verification/linking. See [test record](docs/TESTING.md).
Unsupported class fingerprints disable Blossom cleanly. OptiFine is not supported
by this initial adapter; it needs a separately tested integration.

## Develop

`python3 scripts/build.py` (Linux) / `py scripts/build.py` (Windows), with Java 17+
on PATH. `gradle build` delegates to the same pinned pipeline. Build details are
in [BUILDING](docs/BUILDING.md). Output targets Java 8 and excludes game binaries.

[Feature matrix](docs/FEATURES.md) · [Architecture](docs/ARCHITECTURE.md) ·
[Capes](docs/CAPES.md) · [Configuration](docs/CONFIGURATION.md) ·
[Performance](docs/PERFORMANCE.md) · [Reference audit](docs/RESEARCH.md) ·
[Licenses](LICENSES.md)

Commit and push coherent checkpoints. Never mark a feature tested on the basis
of compilation alone. Next gate: authenticated Java 8 graphical startup, then
world/menu/cape acceptance testing before expanding the remaining modules.
