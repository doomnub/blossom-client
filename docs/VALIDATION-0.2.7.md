# Blossom 0.2.7 validation

## Automated coverage

- Java: 100 core checks; cloth stability, rotated head/hat separation, render-shell clearance, movement/stretch and crouch cases; sprint latch persistence; independent legacy settings round-trips.
- Fifteen actual vanilla 1.8.9 classes transformed and JVM-verified. All fifteen are fingerprinted before registering the agent. Hook checks cover item rendering, bow draw, equip, camera height, damage and glint. Regression checks reject game-state writes and network/attack mutations in the classic animation helper.
- Disabled render/input/network helpers run without a Minecraft instance or GL context. Legacy simulation stops when disabled; hooks return original values. Shared dispatch and settings still consume a small amount of memory/work.
- Legacy launcher: 49 checks, plus CI managed Java 8 installation and offline reuse.
- Native backend: configuration migration/defaults, game verification policy, append-only skin collection/deduplication/variants, malformed PNG rejection and desktop-entry escaping.
- Browser fixtures: skin import/use, retained collection, account head, owned cape/hide-cape selection, sign-out clearing, both verification switches, narrow layout, navigation and safe text/log rendering.
- Windows and Ubuntu native builds and Rust/installer checks passed at checkpoint `ea60ba99`; Ubuntu also passed the actual release WebView/IPC check. Release CI repeats those gates on the final source.

## Implementation boundaries

Wardrobe operations use Minecraft Services and a fresh ownership list before applying a cape. Tokens stay in Rust. Existing/current skins are archived before replacement. The local collection has no delete operation. Sign-out clears account previews/capes, not saved skins. The preview uses skinview3d; its animation loop is disposed off-tab or while the page is hidden.

The cape solver remains a single surface; thickness is render-only. The rotating padded head box includes hat clearance. Cape faces do not use OpenGL directional lighting, but retain Minecraft's lightmap and gamma, including Fullbright. This does not alter the world's lighting.

1.7 visuals use independently implemented transformations compared against the official 1.7.10 and 1.8.9 render paths. Sprite-based held items get classic positioning; maps and 3D models retain vanilla positioning. Bow draw scales about the old sprite axes. Classic glint normalizes sprite UVs out of the atlas; 3D models retain vanilla glint geometry with classic color. Bare-arm transforms already share the original equations. The camera-only sneak interpolation does not change eye position for targeting, player bounds or movement. Additional swings are local renderer state, never player swing/attack packets. The Hit Color module takes priority over classic armor tint.

## Manual acceptance still needed

- Authenticate a real account, refresh its current skin, switch classic/slim skins, restart Minecraft, select each owned cape, hide the cape, sign out/in, and confirm every archived skin remains.
- Test the installed launcher icon in Fedora GNOME/KDE Wayland and the Windows taskbar. Automated Linux desktop testing uses Xvfb, not a Wayland compositor.
- Test head/hat/cape collision while crouching, turning, jumping and moving quickly, with skin layers, Fullbright and dark/light environments. Bounded cloth collision can still allow intersections in extreme folds.
- Compare first-person blocking, swing+block, bow, rod, eat/drink, item switches, bare arm, map and 3D items with each legacy setting disabled/enabled; repeat with Left Hand and resource packs. Inspect glint, armor damage tint and sneak transitions. No pixel-perfect graphical gameplay claim follows from compilation or bytecode tests.
- Test sprint persistence across death, menus, reconnect and process restart; hotbar scroll with zoom enabled/disabled; five preset save/load/overwrite paths.

Live account mutation and graphical Minecraft gameplay were not run in this workspace. CI packaging and a native-window smoke test do not substitute for those checks.
