# Configuration

Game-directory relative paths:

- blossom/profiles/default.properties — automatically saved settings.
- blossom/profiles/preset.properties — explicit Save/Load preset slot.
- blossom/profiles/*.backup — previous successfully replaced file.
- blossom/capes/ — user-owned PNG files or frame directories.

Version 1 uses UTF-8 Java properties (not JSON). Keys:
`schema=1`, `<module>.enabled=true|false`, `<module>.x=0..1`,
`<module>.y=0..1`, `hud.background=true|false`, `cape.selected=filename`.
Names must match `[a-zA-Z0-9_-]{1,48}`. Oversized/unknown-schema files are refused.
Coordinates are clamped and nonfinite values revert to defaults.
Writes serialize on a worker, write a sibling temporary file, retain a backup,
and atomically replace when supported by the filesystem. Shutdown waits up to
three seconds for pending writes. Corrupt default files are not overwritten;
rename the corrupt file before restarting to create a new one.

Not implemented: migrations beyond schema 1, unlimited named profile UI,
import/export dialogs, profile-specific keybinds/colors/video settings, recovery UI.

## Module settings (0.2.3)

Every module Settings screen has Visuals and Functionality tabs. Visuals contains
HUD text size, opacity, text/background colors, shadow, position lock and reset;
crosshair shape/color options also live here. Functionality contains enablement,
FPS content, zoom behavior/keybind and coordinate privacy where applicable.
Small GUI sizes paginate the controls. Cape settings link to the existing library
and animation controls. Modules without visual options explicitly say so.

FPS defaults to Current: Minecraft's built-in recent FPS counter (updated roughly
once per second). Average uses the collected frame-time samples, up to 10,000
frames. Slow-tail 1%/0.1% values can be enabled independently; they default off.
`fps.average` and `fps.lows` are boolean profile settings. Existing profiles that
lack these keys get the new Current default.

Toggle sprint listens to the vanilla sprint binding (normally Left Ctrl), not the
attack binding. While latched on, it supplies a held sprint-key input; stopping
and restarting forward movement does not clear it. Vanilla decides when sprinting
is allowed. Menus suspend sprint input without clearing the latch. Closing a menu resumes the held input. Player/world changes and module disable clear it.

The Minecraft title screen now has a Blossom settings button in the upper-right.
Escape from the Blossom module menu returns to the title screen.


## Camera and HUD controls (0.2.3)

Zoom interpolates the camera FOV over 250 ms on press, release and scroll changes.
Scroll while holding the zoom key to adjust magnification between 2x and 20x;
these events do not scroll the hotbar. Each hold starts from the configured zoom
factor, and scroll adjustments are never saved to the profile.

The optional FOV module defaults to Constant FOV. Turn Constant off for separate
base, sprint, Speed I, Speed II+, slowness, flying and bow values (30–110 degrees).
Slowness takes precedence over speed; sprint and flight add their difference from
the base value; bow draw interpolates toward its configured value. Dynamic sprint
can be disabled independently. This changes camera projection only. Constant mode
also normalizes vanilla underwater/death FOV changes. Zoom applies afterward.

Extra text can be disabled per applicable HUD in Functionality: FPS, CPS, ping,
coordinates, direction, memory, resources, movement and armor. For example,
`0 ms (server)` becomes `0`, and `0 | 0 CPS` becomes `0 | 0`.
Potion effects have a Hide level 1 option, enabled by default; higher levels remain.

All HUD visuals on the module page edits text size, opacity, text/background
colors and shadow across every HUD, including disabled HUDs. Only the property
you change is copied; positions, locks and enabled states remain intact.
