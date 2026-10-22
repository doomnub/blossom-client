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
