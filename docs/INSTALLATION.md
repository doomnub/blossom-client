# Install this development build

This is an experimental checkpoint, not the complete client described in the spec.
Full graphical startup and gameplay are not verified yet. Use a separate test
installation and keep normal Minecraft available.

1. Download the JAR from GitHub Actions artifacts, or `dist/` when present.
2. Launch **vanilla Minecraft 1.8.9** once using your legitimate account.
3. Make a separate 1.8.9 launcher installation and choose a separate game directory.
4. Select a Java 8 runtime for the game (build tools use Java 17).
5. Add `"-javaagent:/absolute/path/to/blossom-client-0.2.0-alpha.1.jar"` to the
   installation's JVM arguments. Preserve its existing memory and security options.
   On Windows, a valid example is `"-javaagent:C:\Games\Blossom\blossom-client-0.2.0-alpha.1.jar"`.
6. Launch through the launcher. Right Shift opens the module menu, including at
   the main menu. Type to filter modules. Escape returns to the preceding screen.

Running `java -jar blossom-client-0.2.0-alpha.1.jar` opens copyable setup instructions;
it is **not** an automatic installer or an independent Minecraft launcher.

Use Edit HUD to drag enabled widgets. Hold Left Shift for an 8-pixel grid. Positions
are stored as fractions of the available screen area. Background toggles all HUD
backgrounds. Save/Load preset uses a second named preset alongside default autosave.
This is not yet a full profile management UI.

Zoom: hold C, released on entering a menu or disabling it. Fullbright: disabled by
default, restores the previous gamma when disabled. Neither changes movement or
combat. Keybind remapping and advanced settings are pending.

See CAPES.md for artwork. If a log says `Blossom NOT ENABLED`, check that the game
is unmodified vanilla 1.8.9. Forge, OptiFine and Lunar are not supported by this
checkpoint. The fingerprint check intentionally rejects unverified layouts.

To uninstall, remove the javaagent argument. User files are in the game directory's
`blossom/` folder. No game JAR or launcher account settings are edited.

Fedora KDE/Wayland, openSUSE, Windows fullscreen, high DPI and server connections
still require manual acceptance tests. This repository does not claim Hypixel
approval or verified compatibility.
