# Linux world-entry freeze: recovery and evidence

Root cause is not confirmed. Headless compilation/verification does not rule out
GPU driver stalls, memory pressure or gameplay deadlocks. Do not repeatedly enter
a world if doing so makes the desktop unresponsive.

## Startup bypass (source builds after this change)

Close the launcher first. Launch it from a terminal with the environment variable
set, using the normal executable or launcher JAR:

```sh
BLOSSOM_SAFE_MODE=1 /path/to/Blossom.AppImage
# Or the legacy Java launcher:
BLOSSOM_SAFE_MODE=1 java -jar /path/to/blossom-launcher.jar
```

Use your actual launcher path. The game inherits the variable. Alternatively,
add `-Dblossom.safeMode=true` to the **game JVM** arguments. This is a new startup
option; earlier released agents do not recognize it. Confirm the game log says:

```
[Blossom] SAFE MODE: all Blossom game patches disabled for this launch
```

This prevents transformer registration, so every Blossom game patch/module is
absent for that launch. It does not change profiles, worlds, resource packs or
launcher settings, and does not change the graphics driver or vanilla Minecraft.
Remove the variable/argument to restore normal behavior. A successful safe-mode
run narrows the investigation; it does not identify which patch caused a freeze.

## Information needed before calling this fixed

- Exact release, or `git rev-parse HEAD` for a locally built client.
- Whether menus remain responsive and whether the stall happens in singleplayer,
  multiplayer or both; any existing safe-mode/vanilla comparison.
- Game `logs/latest.log` and launcher logs from the affected run. Redact tokens,
  session IDs and server addresses before sharing.
- Kernel messages around the freeze, after recovery. On Fedora:

```sh
journalctl -k -b --since '15 minutes ago' --no-pager
# If a reboot was necessary, inspect the preceding boot instead:
journalctl -k -b -1 --no-pager
```

Look for GPU timeout/reset messages (amdgpu/i915/nouveau/nvidia), out-of-memory
kills, or allocation failures. Missing previous-boot logs are possible. These are
read-only diagnostics; do not change kernel/driver/Java flags based only on guesses.
