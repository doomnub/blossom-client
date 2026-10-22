# MASTER DEVELOPMENT PROMPT: Build a Complete Minecraft 1.8.9 PvP Client Inspired by Lunar Client

## 0. YOUR ROLE AND PRIMARY OBJECTIVE

You are an autonomous senior Java engineer, Minecraft client developer, graphics programmer, UI/UX designer, performance engineer, and software testing specialist.

Your task is to **design, implement, compile, test, and deliver a complete custom Minecraft Java Edition 1.8.9 client** that recreates the practical gameplay experience, customization, visual polish, and performance characteristics of Lunar Client as closely as reasonably possible.

This is a real software engineering project. Do not provide a concept-only implementation, fake functionality, disconnected example classes, or a UI mockup presented as a finished client.

The project must ultimately produce a working, launchable, configurable Minecraft 1.8.9 installation.

### Core requirements

- Minecraft Java Edition 1.8.9 exclusively.
- Java-based implementation.
- Windows and Linux compatibility, with Fedora KDE as a first-class target.
- No mandatory Forge installation.
- No mandatory Fabric installation.
- No Lunar Client runtime dependency.
- No reliance on Lunar Client's private infrastructure.
- Compatible with normal Minecraft 1.8.9 multiplayer servers, including Hypixel.
- Legitimate Minecraft account authentication.
- Support for normal Minecraft resource packs and OptiFine-style visual functionality wherever technically and legally feasible.
- Highly customizable user interface.
- Extensive PvP HUD modifications.
- Accurate legacy combat behaviour.
- Sophisticated performance optimization.
- Smooth, responsive mouse and keyboard input.
- Custom static and animated capes.
- Persistent configuration profiles.
- A coherent and polished user experience.

**Prioritize correct gameplay, input responsiveness, stable frame times, and functional settings over purely visual similarities.**

### Reference products

Use these references for feature discovery and behaviour:

- https://www.lunarclient.com/features
- https://www.lunarclient.com/changelog
- https://www.lunarclient.com/news/getting-started-with-lunar-client
- https://www.lunarclient.com/news/why-lunar-client-is-the-best-for-playing-hypixel-network
- https://www.lunarclient.com/news/patch-notes-7
- https://www.lunarclient.com/news/patch-notes-18
- https://www.lunarclient.com/news/patch-notes-23
- https://www.lunarclient.com/news/lunar-client-monthly-recap-april-2026

Treat Lunar Client as a functional reference, not a codebase to steal.

Implement original code, original branding, and original interface assets. Do not copy Lunar's proprietary source, trademarks, licensed cosmetics, or artwork.

Where an established open-source implementation exists, inspect its licence and dependencies before deciding whether to reuse or independently implement the feature.

Do not bypass Minecraft authentication, anti-cheat systems, client protections, or licensing restrictions.

---

# 1. DEVELOPMENT PHILOSOPHY

The final result should feel like a complete commercial-quality Minecraft client, not a collection of unrelated mods.

The player should be able to:

1. Open a launcher or choose an installed Minecraft version.
2. Launch Minecraft 1.8.9 normally.
3. Connect to existing Minecraft multiplayer servers.
4. Open a sophisticated in-game modification menu.
5. Enable or disable individual features.
6. Customize every HUD element.
7. Configure visual effects and animations.
8. Select custom capes and animations.
9. Create multiple configuration profiles.
10. Save all settings persistently.
11. Enjoy stable performance without needing Forge or Fabric.

The client must be usable by someone who has never opened an IDE or edited Java code.

Every setting must have an observable effect. No empty toggle switches, placeholder buttons, or decorative controls without underlying functionality.

## Development priorities

In descending order:

1. Correct Minecraft 1.8.9 functionality.
2. Stable and responsive gameplay.
3. Client safety and multiplayer compatibility.
4. Smooth performance and consistent frame times.
5. Modularity and maintainable architecture.
6. Feature coverage.
7. UI/UX polish.
8. Optional advanced systems.

Do not sacrifice critical functionality to inflate the number of implemented features.

---

# 2. TECHNICAL FOUNDATION

## 2.1 Minecraft version

The client must target Minecraft Java Edition **1.8.9**.

Maintain the original game's networking protocols, movement physics, block interactions, combat timing, and server compatibility.

Do not silently introduce newer Minecraft combat mechanics or alter player movement behaviour.

Minecraft 1.8.9 does not contain Elytra, shields, modern swimming, modern offhand mechanics, or newer game-version features.

Do not implement irrelevant modern mechanics merely for feature parity with Lunar's newer releases.

## 2.2 Java and rendering

Use a toolchain compatible with Minecraft 1.8.9, generally Java 8 and its original LWJGL 2/OpenGL environment.

Use Gradle where practical for reproducible builds.

Investigate appropriate legal development and patching approaches. A direct standalone modification of the Minecraft client is acceptable as a local development strategy, provided the final distribution does not redistribute unauthorized proprietary game binaries.

Prefer a patch-based installation method that works against files the user legitimately owns.

Keep the architecture compatible with later maintenance and possible porting.

## 2.3 No Forge requirement

This is a strict requirement.

Do not make Forge a runtime dependency.

You may study established modding frameworks and open-source code for implementation techniques, but the finished client must operate without requiring the user to install Forge.

If a development toolchain uses Minecraft patching, mappings, or transformation techniques, document them clearly.

Do not disguise a Forge-based implementation as a Forge-free client.

## 2.4 OptiFine

The user strongly values OptiFine 1.8.9 compatibility.

Investigate a compliant way to support an independently installed OptiFine build or reproduce its relevant functionality using original code.

Important features include:

- Custom skies.
- Connected textures.
- Custom entity textures where supported.
- Shader-pack compatibility.
- Adjustable animation settings.
- Fine-grained graphics settings.
- Performance options.
- Custom fonts.
- Resource-pack enhancements.
- Render-distance and chunk customization.

Do not bundle proprietary OptiFine files without permission.

If complete compatibility cannot be achieved, explain precisely which features are supported, unsupported, or experimental.

Do not declare OptiFine compatibility until an actual launch and relevant features have been tested.

## 2.5 Core architecture

Build a clean, modular architecture with dedicated systems for:

- Application bootstrap.
- Minecraft compatibility and patching.
- Module registration.
- Event dispatch.
- Input handling.
- Configuration management.
- HUD rendering.
- World rendering extensions.
- Entity rendering extensions.
- Texture loading.
- Animation timing.
- Performance profiling.
- Resource-pack integration.
- Account and session integration.
- GUI components.
- File management.
- Logging and crash reporting.
- Testing and validation.

Use interfaces and dependency boundaries so features can be developed and tested independently.

## 2.6 Event system

Develop an efficient internal event bus that supports, at minimum:

- Client tick.
- World render.
- HUD render.
- Key press/release.
- Mouse press/release.
- Mouse movement and scrolling.
- Screen open/close.
- World join/leave.
- Server connect/disconnect.
- Chat received.
- Local player attack.
- Local player damaged.
- Item change.
- Potion effect update.
- Resource-pack reload.
- Resolution and GUI scale change.

Avoid unnecessary per-frame allocations.

Events should be predictable, correctly ordered, and appropriately separated between rendering and game logic.

## 2.7 Module system

Every feature must be represented as a manageable module.

Each module should expose:

- Unique identifier.
- Display name.
- Description.
- Category.
- Enable/disable state.
- Default settings.
- Configurable settings.
- Optional keybind.
- Supported Minecraft versions.
- Rendering requirements.
- Dependencies.
- Initialization and cleanup methods.

Disabled modules should not unnecessarily process events.

A module failure should not corrupt other modules' settings or leave rendering state broken.

---

# 3. USER INTERFACE AND MENU SYSTEM

The interface should reproduce the usability and density of a polished PvP client, with its own original visual identity.

Use a dark, modern interface with clean typography, smooth animations, logical spacing, subtle shadows, and restrained accent colours.

The default accent colour should be customizable, with pink as the initial selection.

## 3.1 Main menu

Create a custom main menu containing:

- Singleplayer.
- Multiplayer.
- Direct Connect.
- Settings.
- Mods.
- Cosmetics.
- Profiles.
- Resource Packs.
- Screenshots.
- Quit Game.

Include a tasteful animated panorama or background.

Allow backgrounds to be changed with custom images.

Support different aspect ratios and resolutions.

No mandatory news feed, cosmetic store, promotions, subscription advertisements, or online account system beyond Minecraft authentication.

## 3.2 In-game mod menu

Default keybind: Right Shift.

Required elements:

- Search bar.
- Category navigation.
- Scrollable mod cards.
- Enable/disable controls.
- Settings buttons.
- Favourite mods.
- Recently modified mods.
- Sorting controls.
- Reset-to-default functionality.
- Save/export/import profiles.
- Search highlighting.
- Keyboard navigation.
- Mouse navigation.
- Tooltips.
- Clear indication of unsupported modules.

Categories should include:

- PvP.
- HUD.
- Visual.
- Rendering.
- Performance.
- Movement and Controls.
- Inventory.
- World and Navigation.
- Hypixel and Bedwars.
- Miscellaneous.
- Cosmetics.
- Experimental.

Users should be able to organize favourite modules into a custom layout.

## 3.3 Settings controls

Build reusable UI components for:

- Toggle switches.
- Numerical sliders.
- Colour pickers.
- Hexadecimal colour entry.
- Dropdown menus.
- Keybind selectors.
- Text fields.
- File selectors.
- Preset selectors.
- Checkboxes.
- Multi-option selectors.
- Confirmation dialogs.
- Reset buttons.

Sliders must support fine-grained adjustments.

Colour pickers should support alpha transparency when relevant.

Settings should apply immediately whenever possible.

## 3.4 Interface animations

Implement:

- Smooth menu opening and closing.
- Hover effects.
- Subtle button transitions.
- Smooth scrolling.
- Animated dropdowns.
- Dragging animations.
- Smooth colour transitions.
- Optional menu blur.
- Reduced-animation mode.

UI animations must not introduce noticeable input latency.

Do not block the game thread with file loading or expensive UI calculations.

---

# 4. UNIVERSAL HUD SYSTEM

This is one of the most important subsystems.

The client must provide an advanced HUD editor comparable to sophisticated PvP clients.

## 4.1 HUD editor

Users must be able to enter HUD editing mode and:

- Drag elements anywhere.
- Resize them.
- Change scale.
- Adjust opacity.
- Enable or disable backgrounds.
- Set background colours.
- Add borders.
- Change font.
- Change text colour.
- Set corner rounding.
- Choose text alignment.
- Snap to screen edges.
- Snap to other HUD elements.
- Show alignment guides.
- Lock positions.
- Reset individual elements.
- Reset the entire layout.
- Save presets.

Use resolution-independent coordinates.

HUD elements should maintain sensible positions after changing window size or entering fullscreen.

Provide a preview of empty or context-dependent modules while editing.

## 4.2 HUD rendering

Support:

- Minecraft font.
- Optional custom fonts.
- Text shadows.
- Outlines.
- Background opacity.
- Chroma/rainbow colouring.
- Static colours.
- Colour gradients.
- Smooth scaling.
- Pixel-perfect positioning.
- Configurable spacing.

HUD rendering must minimize unnecessary texture binds and allocations.

Never allow one broken HUD module to leave OpenGL state corrupted for the next module.

---

# 5. KEYSTROKES MOD

Implement a fully customizable keystrokes overlay.

Display real-time keyboard input states.

## Standard keys

- W.
- A.
- S.
- D.
- Space.
- Left mouse button.
- Right mouse button.
- Optional Shift.
- Optional Ctrl.
- Optional additional mouse buttons.

Allow alternative layouts.

## Customization

- Key dimensions.
- Spacing.
- Border width.
- Background colour.
- Pressed background colour.
- Text colour.
- Pressed text colour.
- Font.
- Font size.
- Corner radius.
- Key labels.
- Opacity.
- Press animation.
- CPS display.
- Horizontal and vertical layouts.

Mouse click states should update without artificial delay.

Display optional left and right mouse CPS directly inside the mouse-button boxes.

Allow the player to hide individual keys.

---

# 6. CPS MOD

Implement a clicks-per-second display.

Features:

- Left-click CPS.
- Right-click CPS.
- Combined CPS.
- Separate or combined displays.
- Configurable time windows.
- Optional rolling graph.
- Peak CPS tracking.
- Average CPS tracking.
- Reset statistics.
- Configurable formatting.

Count actual mouse inputs without generating or modifying clicks.

Use a timestamp-based rolling window instead of relying on frame counts.

Avoid incorrect CPS readings during frame drops or low FPS.

Do not include autoclicking functionality.

---

# 7. FPS AND FRAME-TIME MOD

Create an advanced performance HUD.

Display:

- Current FPS.
- Average FPS.
- Minimum FPS.
- Maximum FPS.
- 1% low FPS.
- 0.1% low FPS.
- Current frame time in milliseconds.
- Average frame time.
- Optional frame-time graph.

Use statistically appropriate frame-time sampling.

The 1% and 0.1% low readings should be calculated from the slowest 1% and 0.1% of frames over a sufficiently large configurable sample window, not estimated from instantaneous FPS.

Allow sample-window settings.

Show the performance graph in a clean, unobtrusive HUD element.

The user prioritizes smooth gameplay over extremely high average FPS.

---

# 8. PING MOD

Implement a ping indicator using existing Minecraft server-connection information.

Features:

- Ping in milliseconds.
- Configurable refresh.
- Colour thresholds.
- Optional small graph.
- Connection-status indicator.
- Average and recent ping.
- Unknown-ping state.

Do not falsely label estimated ping as a verified measurement.

Minimize extra network traffic.

---

# 9. COORDINATES MOD

Display player coordinates.

Include:

- X.
- Y.
- Z.
- Decimal or rounded values.
- Direction facing.
- Cardinal direction.
- Optional biome.
- Optional chunk coordinates.
- Optional Nether coordinate conversion.
- Separate colour settings.
- Compact and detailed layouts.

Do not expose coordinates in screenshots when privacy mode is enabled.

---

# 10. TOGGLE SPRINT AND TOGGLE SNEAK

Provide a configurable sprint/sneak module.

## Toggle Sprint

- Toggle or hold operation.
- Configurable keybind.
- Status HUD.
- Optional automatic sprint continuation when normal sprint conditions remain valid.
- Respect hunger, collisions and vanilla movement restrictions.
- Disable automatically when menus require normal keyboard navigation.

## Toggle Sneak

- Toggle or hold operation.
- Configurable keybind.
- Status HUD.
- Correct behaviour while in inventories or menus.
- Restore normal state when toggles are disabled.

Do not alter movement speed, reach, jump height, sprint physics, or server-authoritative movement rules.

Include safe defaults suitable for standard multiplayer servers.

---

# 11. ZOOM MOD

Implement OptiFine-style zoom.

Required features:

- Hold-to-zoom.
- Toggle-to-zoom.
- Custom keybind.
- Adjustable zoom amount.
- Smooth transition.
- Optional scroll-wheel zoom.
- Adjustable scroll sensitivity.
- Optional mouse sensitivity scaling.
- Optional cinematic camera smoothing.
- Configurable transition speed.
- Restore original FOV correctly.

Zoom must work in both first-person and third-person modes.

Avoid FOV changes becoming stuck after opening menus or disconnecting.

---

# 12. FREELOOK AND CAMERA CONTROLS

Implement client-side camera customization.

## Freelook

Allow camera rotation independent of the player model while a designated key is held or toggled.

Settings:

- Hold/toggle mode.
- Camera sensitivity.
- Return-to-original-view behaviour.
- Third-person camera controls.
- Optional smooth transitions.

Do not change player movement orientation or send invalid rotations to servers.

Allow disabling the feature on servers where it is prohibited.

## Snaplook

Implement quick camera-perspective changes using individual keybinds.

Examples:

- Temporary front view.
- Temporary rear view.
- Immediate return to the previous camera.
- Configurable perspective shortcuts.

## Other camera controls

- Camera bobbing.
- Hurt camera shake strength.
- Smooth camera transitions.
- Optional third-person camera distance.
- FOV adjustment.
- Dynamic FOV controls.
- Static FOV mode.

Avoid camera clipping problems and accidental gameplay advantages from unsupported camera behaviour.

---

# 13. CROSSHAIR MOD

Implement an advanced custom crosshair editor.

Allow:

- Default Minecraft crosshair.
- Custom crosshair styles.
- Dot.
- Plus.
- Cross.
- Circle.
- Four-line layout.
- Crosshair outlines.
- Custom thickness.
- Custom gap.
- Custom length.
- Opacity.
- Colour.
- Hit colour.
- Optional dynamic spread.
- Different colours when targeting entities.
- Separate preset profiles.

The player should be able to create or import a custom crosshair image.

Dynamic animations must be optional.

Crosshair positioning must remain pixel-accurate at all supported GUI scales.

---

# 14. ARMOR STATUS MOD

Implement a comprehensive armor and durability HUD.

Display:

- Helmet.
- Chestplate.
- Leggings.
- Boots.
- Currently held item.
- Durability values.
- Percentage durability.
- Durability bars.
- Damage warnings.
- Item icons.
- Optional item names.

Settings:

- Horizontal or vertical display.
- Reverse order.
- Item icon scale.
- Durability position.
- Colours.
- Warning thresholds.
- Background.
- Spacing.
- Show/hide empty armor slots.

Automatically update after item changes or damage events.

---

# 15. POTION EFFECTS MOD

Display active potion effects.

Features:

- Potion icon.
- Effect name.
- Amplifier level.
- Remaining duration.
- Configurable ordering.
- Configurable font.
- Background opacity.
- Compact mode.
- Duration warning colours.
- Hide selected effects.
- Optional vanilla effect display replacement.

Refresh potion durations efficiently.

Do not render duplicate vanilla and custom displays unless explicitly requested.

---

# 16. SCOREBOARD MOD

Provide a complete scoreboard customization system.

Features:

- Drag and resize scoreboard.
- Custom title display.
- Background transparency.
- Border controls.
- Text shadow.
- Line spacing.
- Font options.
- Colour handling.
- Configurable alignment.
- Option to hide scoreboard numbers.
- Option to hide the title.
- Maximum display height.
- Correct handling of server-provided formatting.

Preserve the underlying server scoreboard data.

Do not alter scoreboard packets or interfere with server functions.

---

# 17. TAB LIST EDITOR

Customize the Minecraft player-list overlay.

Features:

- Width.
- Height.
- Columns.
- Row spacing.
- Background transparency.
- Player name formatting.
- Ping bars.
- Optional numerical ping.
- Ping colour thresholds.
- Header and footer rendering.
- Font.
- Text shadows.
- Maximum visible entries.
- Scroll behaviour for large lists, if compatible.
- Optional player-head rendering.

Maintain compatibility with server-provided teams, name formatting, and headers.

---

# 18. CHAT MOD

Build a sophisticated chat customization system.

Features:

- Chat background opacity.
- Text opacity.
- Chat width.
- Chat height.
- Chat scale.
- Timestamps.
- Timestamp format.
- Timestamp colours.
- Custom fonts where supported.
- Mention highlighting.
- Searchable local chat history.
- Copy chat messages.
- Copy links.
- URL detection.
- Message filtering.
- Optional duplicate-message stacking.
- Configurable fade-out duration.
- Persistent chat history, with a privacy option.
- Preserve typed text during UI changes.
- Optional chat notification sounds.

All outbound messages must remain user-controlled.

Do not create hidden automatic messaging.

Allow optional local text shortcuts that insert a message into the chat input without sending it automatically.

---

# 19. 1.7 VISUALS AND LEGACY ANIMATIONS

Implement optional Minecraft 1.7-inspired rendering in Minecraft 1.8.9.

This is a high-priority visual feature for PvP players.

Investigate and independently implement:

- Sword blocking animation.
- Blocking-and-swinging visual presentation.
- Sneaking animation.
- Rod animation.
- Bow animation.
- Item switching animation.
- Eating animation.
- Drinking animation.
- First-person hand positioning.
- Item equip transition.
- Damage animation.
- Item glint presentation.
- Old-style item holding positions.
- Optional always-swing visual behaviour.

These must be cosmetic changes only.

Do not change attack cooldowns, hit registration, range, attack speed, packet timing, or the server's understanding of player actions.

Each animation option must be individually configurable.

Include an option to restore unmodified Minecraft 1.8.9 rendering.

---

# 20. ITEM CUSTOMIZER

Provide detailed control over the appearance of held items.

## First-person item customization

- Item X position.
- Item Y position.
- Item Z position.
- X rotation.
- Y rotation.
- Z rotation.
- Overall scale.
- Swing animation speed.
- Swing animation style.
- Equip animation.
- Re-equip animation toggle.
- Eating and drinking animation options.
- Item offset while blocking.

## Dropped-item customization

- Item scale.
- Item rotation.
- Ground positioning.
- Bobbing options.
- Spin speed.
- Optional original item physics.
- Optional 2D or 3D appearance where compatible.

Preserve actual Minecraft item behaviour.

---

# 21. ITEM PHYSICS MOD

Make dropped items visually behave more naturally.

Features:

- Rotation while falling.
- Ground-aligned presentation.
- Optional item tumbling.
- Adjustable rotation speed.
- Configurable bobbing.
- Compatibility with resource packs.
- Low-performance-impact mode.

This must not modify item entity positions, item collection, or server physics.

---

# 22. HIT COLOUR MOD

Provide customizable damage tinting.

Features:

- RGB and hexadecimal hit colour.
- Opacity.
- Duration.
- Smooth fade.
- Player rendering.
- Mob rendering.
- Optional armor tint.
- Restore vanilla red hit flash.

Correctly restore original lighting and rendering state after each entity is drawn.

---

# 23. PARTICLE CHANGER

Create an extensive particle customization system.

Include:

- Critical-hit particles.
- Sharpness particles.
- Normal hit particles.
- Enchantment particles.
- Block-break particles.
- Explosion particles.
- Potion particles.
- Flame and smoke.
- Portal effects.
- Water particles.
- Rain and weather effects.

Settings:

- Particle multiplier.
- Particle scale.
- Particle opacity.
- Particle colour.
- Particle lifetime where safe and supported.
- Particle density.
- Category-specific toggles.
- Per-category settings.

Provide a performance-friendly option to reduce unnecessary particles.

Particle modifications must remain client-side visual effects.

---

# 24. BLOCK OVERLAY

Customize the appearance of the currently targeted block.

Features:

- Outline colour.
- Outline thickness.
- Outline opacity.
- Filled overlay.
- Filled overlay opacity.
- Rainbow colouring.
- Custom line rendering.
- Disable vanilla selection box.
- Optional block-specific colours.

Maintain correct targeting and vanilla block interaction.

---

# 25. BLOCK OUTLINE AND CHUNK BORDERS

Implement tools for visualizing block selection and chunk boundaries.

Chunk border settings:

- Toggle keybind.
- Border colour.
- Line opacity.
- Vertical line height.
- Horizontal grid.
- Distance limit.

Render chunk boundaries using actual Minecraft chunk coordinates.

Avoid excessive world-line rendering.

---

# 26. FULLBRIGHT AND LIGHTING

Create adjustable brightness controls.

Features:

- Fullbright toggle.
- Configurable gamma.
- Smooth brightness changes.
- Restore original brightness.
- Resource-pack compatibility.
- Optional dynamic lighting if technically feasible.

Do not accidentally leave gamma settings altered after disabling the feature.

Any advanced dynamic-lighting system should have a distinct performance setting.

---

# 27. TIME CHANGER

Implement client-side time-of-day customization.

Features:

- Day preset.
- Noon preset.
- Sunset preset.
- Night preset.
- Midnight preset.
- Custom time slider.
- Optional time passage.
- Adjustable time progression speed.
- Smooth visual transitions.
- Restore server time on disable.

Do not alter actual server world time.

---

# 28. WEATHER AND FOG CONTROLS

Implement configurable environmental presentation.

Options:

- Rain visibility.
- Snow visibility.
- Rain particle density.
- Weather audio levels.
- Fog density.
- Fog colour where supported.
- Sky visibility.
- Cloud visibility.
- Cloud height where supported.

Avoid interfering with visibility rules on servers where particular features are restricted.

---

# 29. CUSTOM SKY SYSTEM

Build a custom sky manager compatible with Minecraft 1.8.9.

Features:

- Standard vanilla sky.
- Resource-pack custom skies.
- Multiple sky layers.
- Day/night transitions.
- Custom textures.
- Configurable fade timing.
- Rotation.
- Rotation axis.
- Blend modes.
- Colour and opacity controls.
- Sky preset management.
- Enable/disable individual layers.
- Per-world and per-dimension profiles where feasible.

Investigate OptiFine/MCPatcher custom-sky property formats used by existing 1.8.9 resource packs.

Support the established texture structure and properties when technically possible.

Avoid breaking normal sky rendering if no custom sky is defined.

This feature is particularly important because the user already uses customized OptiFine sky resource packs.

---

# 30. MOTION BLUR

Implement an optional motion-blur effect.

Features:

- Blur intensity.
- Multiple quality presets.
- Adjustable blend strength.
- Toggle keybind.
- Compatible with common resolutions.
- Configurable effect during menus.
- Optional exclusion for HUD.
- Off by default.

Avoid excessive GPU resource usage.

Maintain clear frame timing and avoid added input latency.

Do not incorrectly advertise a basic frame accumulation effect as advanced per-object velocity motion blur.

---

# 31. MENU BLUR AND COLOUR EFFECTS

Implement visual post-processing options.

Features:

- Menu background blur.
- Blur radius.
- Blur intensity.
- Colour saturation.
- Contrast.
- Brightness adjustment.
- Optional vignette.
- Configurable UI transparency.

Use efficient OpenGL techniques appropriate for Minecraft 1.8.9.

Restore GL state and framebuffers correctly.

Post-processing must gracefully disable itself when incompatible with shaders or another rendering feature.

---

# 32. PERFORMANCE OPTIMIZATION SYSTEM

This is a top-priority engineering requirement.

The goal is not merely to display a high FPS number.

The goal is consistently smooth and responsive gameplay, especially during Bedwars PvP, fast camera movement, bridging, and combat.

## 32.1 Rendering optimizations

Investigate and implement:

- Reduced redundant OpenGL calls.
- Better texture binding management.
- Efficient vertex batching where possible.
- Optimized entity rendering.
- Optimized HUD rendering.
- Texture caching.
- Reduced unnecessary object allocation.
- Efficient chunk rendering.
- Frustum culling.
- Render-distance controls.
- Particle culling.
- Reduced redundant state changes.
- Improved font rendering.
- Efficient item rendering.
- Optional foliage and animation reductions.

Use profiling evidence rather than assuming every proposed optimization improves performance.

## 32.2 HUD caching

Create a mechanism that caches static or rarely changing HUD elements.

Only rerender cached elements when their underlying content changes.

Dynamic information such as CPS, FPS, and ping must remain accurate.

Avoid stale or broken HUD displays.

## 32.3 Chunk loading

Investigate:

- Chunk rebuild scheduling.
- Chunk update batching.
- Worker-thread-safe preparation.
- Render-thread handoff.
- Reduced chunk-update stalls.
- Configurable chunk update limits.

Do not modify world data or process unsafe Minecraft rendering operations on arbitrary threads.

## 32.4 Entity rendering

Allow:

- Configurable maximum entity render distance.
- Render culling where appropriate.
- Optional reduction of expensive decorative entities.
- Hiding unnecessary visual particles.
- Efficient armor and model rendering.
- Optional rendering-detail presets.

Preserve normal entity interactions and server-supplied positions.

## 32.5 FPS limits

Provide separate FPS caps for:

- Active gameplay.
- Unfocused gameplay.
- Main menu.
- Loading screens where feasible.

Support VSync controls.

Avoid busy-wait loops that waste CPU resources unnecessarily.

## 32.6 Garbage collection and memory

Minimize allocation-heavy code in:

- Render loops.
- Input events.
- HUD formatting.
- Particle systems.
- Animation updates.
- Texture loading.

Use memory profiling to identify actual issues.

Do not hardcode aggressive Java garbage collector flags without testing.

Make JVM arguments configurable, and ship conservative defaults suitable for Java 8.

## 32.7 Performance measurement

Build a repeatable testing harness capable of recording:

- Average FPS.
- Median frame time.
- 95th-percentile frame time.
- 99th-percentile frame time.
- 99.9th-percentile frame time.
- 1% low FPS.
- 0.1% low FPS.
- Memory usage.
- Garbage collection events.
- CPU utilization where measurable.

Compare against clean Minecraft 1.8.9 and, when available, an installed Lunar Client reference.

The initial performance target is stable 165 FPS at the user's chosen competitive settings, with strong frame-time consistency.

This is a target, not a guaranteed result.

A client running at 165 FPS consistently is preferable to one averaging 300 FPS with major stutters.

---

# 33. INPUT RESPONSIVENESS

Maintain precise mouse and keyboard input.

The client must never intentionally delay:

- Mouse clicks.
- Camera movement.
- Movement-key input.
- Hotbar selection.
- Sprint and sneak transitions.
- Inventory interaction.

Features:

- Configurable mouse sensitivity.
- Raw mouse input where supported.
- Mouse-button keybinds.
- Keyboard modifier combinations.
- Conflict detection.
- Keybind reset.
- Optional mouse polling diagnostics.

Provide an input-debugging tool that measures local event-processing timing without pretending to measure physical device-to-screen latency unless appropriate instrumentation exists.

Do not change how often combat actions are sent to a server.

---

# 34. BEDWARS MODULE

Create a dedicated Bedwars feature category.

All modules should use normal information legitimately available to the client.

## 34.1 Bedwars HUD

Display available match information such as:

- Team colour.
- Match state.
- Beds remaining when known.
- Players remaining when known.
- Resource counts.
- Kills.
- Final kills.
- Bed destructions.
- Match timer where available.

Never invent missing statistics.

## 34.2 Resource counter

Track selected inventory items.

Default examples:

- Iron.
- Gold.
- Diamonds.
- Emeralds.

Display:

- Item icon.
- Item count.
- Customizable arrangement.
- Optional resource-group display.
- Background and text settings.

Counts must come from the local player's inventory.

## 34.3 TNT countdown

Render a timer above TNT entities using timing inferred from vanilla 1.8.9 behaviour or other legitimate, observable information.

Features:

- Seconds remaining.
- Optional progress bar.
- Colour thresholds.
- Custom font.
- Distance-based display limit.
- Adjustable visual correction where appropriate.

Treat timing as an estimate whenever exact server fuse state is unavailable.

Do not modify TNT behaviour.

## 34.4 Height limit overlay

Display the relevant build-height limit.

Features:

- Overlay when approaching the limit.
- Optional height HUD.
- Configurable warning distance.
- Adjustable colours.
- Optional block highlight.

Do not hardcode an unsupported Bedwars-specific height value for every map.

## 34.5 Local match statistics

Track:

- Match duration.
- Kills.
- Deaths.
- Final kills when reliably identifiable.
- Beds destroyed when reliably identifiable.
- Win/loss when reliably identifiable.

Clearly distinguish confirmed values from estimates or unsupported metrics.

Do not rely on Lunar's proprietary APIs.

---

# 35. PVP STATISTICS MODULE

Implement a PvP information and statistics dashboard.

Track locally observable data:

- Attacks initiated.
- Confirmed hit feedback where detectable.
- Accuracy estimates.
- Current combo count.
- Longest combo.
- Melee damage information where available.
- Projectile events.
- Local player damage events.
- Session length.

The statistics engine must identify uncertainty where Minecraft does not provide authoritative information.

Do not treat client-side swing animation as proof of a successful hit.

Support:

- Session reset.
- Session history.
- Optional local export.
- Compact HUD.
- Detailed statistics screen.

---

# 36. COMBO COUNTER

Create a dedicated combo counter.

Features:

- Current hit chain.
- Longest hit chain.
- Configurable reset conditions.
- Optional fade-out.
- Colour customization.
- Font customization.
- Background customization.

Use server-observable combat feedback when possible.

If exact combo detection is impossible, document the approximation.

---

# 37. REACH DISPLAY

Display the distance between the local player and a target when an attack is registered or appropriately observed.

Features:

- Distance display.
- Recent-hit history.
- Session maximum.
- Session average.
- Configurable precision.
- Fade duration.
- Colour customization.

Calculate using actual entity positions and appropriate Minecraft coordinate information.

Do not extend player reach.

Do not modify attack packets or hit registration.

---

# 38. ITEM COUNTER AND TRACKER

Implement a configurable item-counting system.

Allow the player to create multiple counters for:

- Blocks.
- Food.
- Potions.
- Arrows.
- Ender pearls.
- Bedwars resources.
- Other available items.

Features:

- Item selection.
- Individual or grouped counters.
- Item icon.
- Count formatting.
- Low-stock warning.
- Horizontal or vertical display.
- Custom ordering.

Counts should update when inventory state changes, without scanning the entire inventory unnecessarily every frame.

---

# 39. INVENTORY CUSTOMIZATION

Build optional inventory enhancements.

Features:

- Configurable GUI scale.
- Inventory background themes.
- Custom slot highlights.
- Item search.
- Search highlighting.
- Item tooltips.
- Scrollable long tooltips.
- Optional container opening animations.
- Hotbar customization.
- Inventory HUD preview.
- Full inventory display overlay if desired.

Do not implement automatic inventory sorting, automatic equipping, or other unsolicited gameplay automation as part of the default competitive client.

## Hotbar customization

Allow:

- Custom hotbar position.
- Hotbar scale.
- Optional vertical layout.
- Custom selected-slot highlight.
- Opacity.
- Spacing.
- Optional item count overlays.

Keep all hotbar actions synchronized with actual vanilla inventory controls.

---

# 40. SHINY POTIONS AND ITEM GLINT

Implement controls for enchantment glint and potion appearance.

Features:

- Normal Minecraft glint.
- Custom glint colour.
- Glint intensity.
- Glint animation speed.
- Potion-glint display.
- Optional glint disable.
- Compatibility with common 1.8.9 resource packs.

Do not break tooltip rendering or held-item rendering.

---

# 41. WAYPOINTS

Create a local waypoint manager.

Features:

- Add waypoint.
- Edit waypoint.
- Delete waypoint.
- Waypoint name.
- Custom colour.
- Coordinates.
- World/dimension assignment.
- Toggle visibility.
- Optional distance display.
- Optional directional indicator.
- Death-location waypoint.
- Local import/export.

Waypoints must remain local.

Do not upload user coordinates to external services.

For multiplayer servers, avoid exposing information not normally known to the client.

---

# 42. DIRECTION HUD AND COMPASS

Provide a compass-like HUD.

Features:

- Cardinal directions.
- Degree heading.
- Smooth movement.
- Configurable width.
- Direction markers.
- Optional coordinate integration.
- Custom styling.

Heading should derive from the local player's actual camera direction.

---

# 43. CLOCK AND STOPWATCH

Implement timing HUDs.

## Clock

Support:

- System time.
- 12-hour and 24-hour formats.
- Minecraft world time.
- Configurable display formats.
- Time zone handling for system time.

## Stopwatch

Support:

- Start.
- Pause.
- Resume.
- Reset.
- Lap tracking.
- Custom keybinds.
- Milliseconds display.
- Configurable precision.
- Optional compact HUD.

Use monotonic timing for elapsed-time measurement.

---

# 44. MEMORY USAGE HUD

Display JVM memory information.

Features:

- Used heap.
- Committed heap.
- Maximum heap.
- Percentage used.
- Optional history graph.
- Warning thresholds.
- Configurable update frequency.

Avoid confusing allocated heap memory with physical system RAM usage.

---

# 45. NETWORK GRAPH

Provide an optional connection diagnostics HUD.

Show available information such as:

- Approximate latency history.
- Connection state.
- Measurable inbound/outbound throughput.
- Packet activity counts, if safely observable.
- Unexpected disconnect events.

Do not modify network packets.

Do not expose server credentials or private session tokens.

---

# 46. SCREENSHOT SYSTEM

Create an enhanced screenshot manager.

Features:

- Configurable screenshot key.
- Standard screenshots.
- Optional HUD-free screenshots.
- Optional privacy mode.
- Automatic local saving.
- Screenshot filename templates.
- Folder selection.
- Copy to clipboard where supported.
- Screenshot gallery.
- Local metadata.
- Optional built-in image viewer.

No mandatory screenshot upload service.

Images must remain local unless the user explicitly exports or shares them.

---

# 47. RESOURCE PACK MANAGER

Implement a resource-pack interface.

Features:

- View installed packs.
- Enable/disable.
- Reorder priority.
- Import packs.
- Open resource-pack folder.
- Pack previews.
- Pack metadata.
- Search/filter.
- Compatibility warnings.
- Error handling.
- Refresh resources.

Support normal Minecraft 1.8.9 resource packs.

Where possible, retain compatibility with OptiFine-style assets.

Do not silently modify user packs.

---

# 48. LOCAL COSMETIC SYSTEM

Create an entirely local cosmetic manager.

This is one of the defining features of the project.

The client must not need Lunar's cosmetic marketplace, subscription system, accounts, or remote cosmetic servers.

Support user-provided artwork.

## Cosmetic categories

Prioritize:

1. Capes.
2. Animated capes.
3. Custom skin previews.
4. Optional lightweight client-only cosmetic attachments.

Any optional cosmetic types must remain compatible with the Minecraft 1.8.9 renderer.

Do not try to implement modern Elytra rendering in 1.8.9, since Elytra does not exist in that version.

---

# 49. CUSTOM CAPE SYSTEM

**THIS IS AN ESSENTIAL, NON-NEGOTIABLE FEATURE.**

Implement a robust custom cape system for the local player.

The user must be able to add an image file and select it directly inside the client.

### 49.1 Static capes

Support PNG files in a conventional Minecraft cape layout.

The importer should:

- Validate image dimensions.
- Validate file format.
- Support transparency.
- Detect invalid images.
- Show a preview.
- Copy the selected image into the cosmetics directory.
- Create a configuration entry.
- Allow easy selection and deletion.

Provide support for conventional cape texture resolutions and higher-resolution multiples where compatible.

Do not assume the entire texture is intended to be displayed as one flat rectangle; correctly map the cape texture to the model geometry.

### 49.2 Cape rendering

The local player's selected custom cape should replace the appearance of the official cape whenever the override is enabled.

Requirements:

- Correct cape geometry.
- Correct UV mapping.
- Appropriate texture filtering.
- Correct alpha blending.
- Stable appearance in third-person.
- Stable appearance in inventory character preview.
- Compatibility with armor rendering.
- Compatibility with sneaking.
- Correct movement animation.
- No flickering between official and custom textures.
- No unnecessary texture reload every frame.

Other players do not need to see the custom cape.

Do not modify the official account cape ownership or pretend the cosmetic is unlocked server-side.

### 49.3 Cape physics

Implement familiar Minecraft cape movement:

- Movement-based swinging.
- Turning behaviour.
- Sneaking adjustments.
- Smoothing.
- Optional physics intensity.
- Optional animation smoothing.
- Optional static mode.

Use vanilla-inspired cape physics as the default.

### 49.4 Animated capes

Support actual cape animation at runtime.

Accepted formats should include:

- Multiple PNG frames.
- Horizontal spritesheets.
- Vertical spritesheets.
- GIF where technically reasonable and securely decoded.
- Optional APNG if a robust decoder is available.

The animation engine must support:

- Custom animation FPS.
- Per-frame duration.
- Looping.
- Ping-pong playback.
- Playback pause.
- Animation preview.
- Frame order.
- Adjustable animation speed.
- Automatic frame validation.
- Reasonable memory limits.

Animation must not depend on repeatedly overwriting Minecraft's cached texture files.

Instead, integrate animation into the client-side rendering system.

Preload or cache textures efficiently.

Do not decode images on the render thread during active gameplay.

### 49.5 Cape selection interface

Create a cape library displaying:

- Cape name.
- Thumbnail.
- File type.
- Animation state.
- Frame count.
- Preview.
- Enable/disable.
- Select.
- Import.
- Delete.
- Rename.
- Edit settings.

Users should be able to change capes without restarting the game whenever practical.

### 49.6 Presets

Include:

- Default Minecraft cape.
- No cape.
- Custom static cape.
- Custom animated cape.

Never bundle or impersonate restricted official Minecraft capes.

User-owned custom PNG files should work independently of the account's official cape collection.

### 49.7 Example configuration

```json
{
  "enabled": true,
  "mode": "animated",
  "selectedCape": "cherry_blossom",
  "animation": {
    "fps": 24,
    "loop": true,
    "pingPong": false,
    "speed": 1.0
  },
  "physics": {
    "enabled": true,
    "intensity": 1.0
  },
  "rendering": {
    "replaceOfficialCape": true,
    "showInThirdPerson": true,
    "showInInventoryPreview": true
  }
}
```

The final project must ship with a functional animated cape renderer, not merely a configuration screen for one.

---

# 50. SKIN PREVIEW SYSTEM

Provide a local skin preview interface.

Features:

- 3D rotating player preview.
- Mouse drag.
- Zoom.
- First-person/third-person inspection.
- Cape preview.
- Armor preview.
- Skin layer visibility.
- Local PNG skin preview.
- Steve and Alex model support where appropriate.

If actual Minecraft account skin changes are offered, use legitimate supported account services.

Do not implement credential scraping or unofficial authentication bypasses.

---

# 51. NAMETAG AND PRIVACY CUSTOMIZATION

Implement:

- Optional player nametag scale.
- Nametag font customization where compatible.
- Nametag background customization.
- Optional nickname hiding for screenshots.
- Local username replacement for privacy mode.
- Optional local skin hiding.
- Player-list privacy controls.

Keep these strictly client-side.

Never disguise the player's real identity from the server or modify authentication.

---

# 52. HYPIXEL-SPECIFIC QUALITY-OF-LIFE FEATURES

Create optional modules for recognized Hypixel game modes.

These should work without Lunar's proprietary backend.

Use normal server-visible information and user-controlled settings.

Potential modules:

- Bedwars HUD.
- Local Bedwars resource tracker.
- TNT countdown.
- PvP information.
- Match timer.
- Optional level information already visible to the client.
- Chat filtering.
- Session statistics.
- Scoreboard enhancements.

Hypixel game detection should be robust and avoid false positives.

Do not assume that all servers have identical scoreboard layouts.

Do not scrape private data or generate automated gameplay actions.

Features requiring third-party APIs should be explicitly marked unavailable in offline-only mode, rather than pretending to have live information.

---

# 53. ADVANCED VISUAL MODS

Implement, where practical:

## 53.1 FOV changer

- Static FOV.
- Dynamic FOV.
- Sprint FOV adjustment.
- Speed-effect FOV adjustment.
- Smooth transitions.
- Per-situation settings.

## 53.2 Saturation display

- Saturation or food-related information when legitimately available.
- Clear distinction between actual and estimated values.
- Configurable display.
- Hide option.

## 53.3 Action bar customization

- Reposition action bar.
- Resize.
- Custom transparency.
- Font.
- Text shadow.
- Optional history.

## 53.4 Title customization

- Title size.
- Subtitle size.
- Position.
- Fade duration.
- Text shadow.
- Background.
- Visibility settings.

## 53.5 GUI scale customization

- Independent HUD scale.
- Inventory scale.
- Menu scale.
- Configurable interface scaling.

## 53.6 Hide foliage

- Optional hiding of selected decorative plant rendering.
- Category controls.
- Restore vanilla rendering.
- Compatibility with game rules and server policies.

## 53.7 Light overlay

- Local light-level display.
- Configurable overlay colour.
- Visibility toggle.
- Efficient updates when blocks or lighting change.

## 53.8 Directional damage indication

- Optional visual indicator for damage direction when the information can be inferred legitimately.
- Adjustable position, colour, size and fade.
- Disable by default if compatibility is uncertain.

---

# 54. ADDITIONAL FEATURE CATALOGUE

The following features should each be evaluated for relevance to Minecraft 1.8.9.

Implement applicable features after the core systems are stable.

For every feature, create a formal entry in the feature matrix and distinguish a confirmed Lunar reference feature from an independently proposed enhancement.

### HUD and information

- FPS.
- CPS.
- Keystrokes.
- Ping.
- Coordinates.
- Direction.
- Clock.
- Stopwatch.
- Memory Usage.
- Net Graph.
- Armor Status.
- Potion Effects.
- Item Counter.
- Item Tracker.
- Scoreboard.
- Tab Editor.
- Action Bar.
- Server Address.
- PvP Info.
- Combo Counter.
- Reach Display.
- F3 Display customization.
- Playtime tracker.

### PvP controls and presentation

- Toggle Sprint.
- Toggle Sneak.
- Zoom.
- Freelook.
- Snaplook.
- Crosshair.
- Hit Colour.
- Hurt Cam.
- 1.7 Visuals.
- Item Customizer.
- FOV Changer.
- Particle Multiplier.
- Particle Changer.
- Block Overlay.
- Item Physics.
- Shiny Pots.
- Custom attack presentation.
- Optional local combat statistics.

### World and environment

- Waypoints.
- Chunk Borders.
- Time Changer.
- Lighting.
- Fullbright.
- Custom Sky.
- Weather visualization controls.
- Fog settings.
- Light Overlay.
- Height Limit visualization.
- WorldEdit selection visualization, when legitimately available.

### Interface and quality-of-life

- Chat.
- Scrollable Tooltips.
- Inventory customization.
- Hotbar customization.
- Menu Blur.
- Motion Blur.
- Colour Saturation.
- Skin Preview.
- Cosmetics.
- Screenshots.
- Resource-pack manager.
- Profile manager.
- Keybind manager.
- Main-menu customization.
- GUI scale.
- Local notes.

### Bedwars and game modes

- Bedwars resource counter.
- Bedwars session information.
- TNT countdown.
- Match timer.
- PvP Info.
- Local statistics.
- Height warnings.
- Scoreboard adaptation.
- Game-specific HUD presets.

### Advanced or optional systems

- Replay recording.
- Replay editor.
- Local clip capture.
- Screenshot gallery.
- Configurable world markers.
- Custom font system.
- Custom visual themes.
- Advanced profiler.
- Local diagnostics panel.
- Configuration backup and restore.

Features specific to modern Minecraft should be omitted or marked unsupported instead of porting incompatible gameplay mechanics into 1.8.9.

---

# 55. CONFIGURATION PROFILES

Create a comprehensive profile manager.

Users must be able to create presets for different play styles.

Example profiles:

- Bedwars.
- Classic Duels.
- Survival.
- Minimal HUD.
- Cinematic.
- Maximum FPS.
- Custom.

Each profile should save:

- Enabled modules.
- Module settings.
- HUD positions.
- Colours.
- Keybinds.
- Crosshair selection.
- Cosmetic selection.
- Video settings where supported.
- Profile-specific preferences.

Support:

- Create.
- Rename.
- Duplicate.
- Delete.
- Import.
- Export.
- Backup.
- Restore.
- Reset defaults.

Use a documented, versioned configuration format such as JSON.

Apply changes atomically to avoid corrupting settings if the game crashes during saving.

---

# 56. SERVER COMPATIBILITY

The client must connect to standard Minecraft 1.8.9 servers.

Preserve:

- Protocol compatibility.
- Vanilla movement rules.
- Inventory synchronization.
- Attack behaviour.
- Block placement.
- Chat functionality.
- Player-list behaviour.
- Resource-pack handling.
- Disconnect/error handling.

No modifications should:

- Extend reach.
- Introduce automated aiming.
- Generate automated combat clicks.
- Alter knockback.
- Change player movement speed.
- Automate bridging or block placement.
- Spoof player actions.
- Circumvent server anticheat checks.

Client-side visual customizations and legitimate HUD features are the focus.

Document that servers can enforce different mod policies.

---

# 57. LAUNCHER AND INSTALLATION

The final client must be straightforward to install.

Investigate two delivery methods.

## Option A: Minecraft launcher integration

Provide a custom version profile compatible with the official Minecraft Launcher.

This may be preferable because it avoids building an entirely separate launcher.

## Option B: Lightweight independent launcher

If appropriate, create an original launcher that can:

- Locate Minecraft installation files.
- Verify dependencies.
- Manage client profiles.
- Configure JVM settings.
- Set memory allocation.
- Set game resolution.
- Select game directory.
- Launch the client.
- Display logs.
- Handle errors.
- Update local client components when authorized.

Do not create an unnecessary online backend.

Use the user's legitimately authenticated Minecraft installation and approved authentication flows.

Never store Microsoft passwords or access tokens in plaintext.

Never redistribute Minecraft binaries or OptiFine assets without permission.

Do not make the entire project dependent on a complicated launcher if the official launcher can reliably do the job.

---

# 58. WINDOWS AND LINUX SUPPORT

The client must support at minimum:

- Windows 10/11.
- Fedora Linux with KDE.
- Other common Linux distributions where feasible.

Requirements:

- Correct filesystem paths.
- Linux case-sensitive path handling.
- Proper Java runtime detection.
- OpenGL compatibility.
- Correct keyboard input on Windows and Linux.
- Correct fullscreen/windowed operation.
- High-DPI scaling.
- Resource-pack folder access.
- File picker compatibility.
- Proper logging.
- No hardcoded Windows paths.

For Fedora KDE, test both windowed and fullscreen behaviour under typical Wayland/XWayland and X11 configurations, as applicable.

Do not assume platform-specific features are available everywhere.

---

# 59. PROJECT ORGANIZATION

Keep source code organized.

A suggested structure:

```text
client-project/
├── build.gradle
├── settings.gradle
├── README.md
├── LICENSES.md
├── docs/
│   ├── ARCHITECTURE.md
│   ├── BUILDING.md
│   ├── INSTALLATION.md
│   ├── FEATURES.md
│   ├── COMPATIBILITY.md
│   └── PERFORMANCE.md
├── launcher/
├── client-core/
├── minecraft-integration/
├── render-engine/
├── gui/
├── hud/
├── modules/
│   ├── pvp/
│   ├── hud/
│   ├── visual/
│   ├── performance/
│   ├── world/
│   ├── inventory/
│   └── miscellaneous/
├── cosmetics/
│   ├── capes/
│   └── animation/
├── configuration/
├── resources/
└── tests/
```

Adapt this structure if a more suitable build architecture is required.

Avoid giant classes containing unrelated features.

Use descriptive names.

Keep module state management separate from UI presentation.

---

# 60. TESTING REQUIREMENTS

Every feature must be tested.

Do not mark functionality complete based solely on successful compilation.

## Unit tests

Cover:

- Configuration serialization.
- Configuration migration.
- Module activation.
- Keybind parsing.
- Colour handling.
- Cape frame selection.
- Animation timing.
- HUD coordinate transformation.
- CPS calculation.
- Frame-time statistics.
- Inventory item counting.
- Profile import/export.

## Integration tests

Cover:

- Game startup.
- World loading.
- Module lifecycle.
- Resource reloads.
- Profile switching.
- Cape texture changes.
- Menu interactions.
- UI scaling.
- Multiplayer connection.
- Window resizing.
- Fullscreen switching.

## Manual testing

Test actual gameplay with:

- PvP.
- Fast camera movement.
- Sprinting.
- Sneaking.
- Bridging.
- Inventory use.
- Rod casting.
- Bow use.
- Potion effects.
- TNT entities.
- Resource-pack switching.
- Third-person cape rendering.
- Animated capes.
- HUD editing.

Use a local or otherwise authorized test environment before testing on public servers.

Do not claim external-server compatibility without actually verifying it.

---

# 61. PERFORMANCE BENCHMARKING

Build a performance testing methodology.

Test comparable scenarios on the same machine using the same settings.

Scenarios:

1. Main menu.
2. Empty superflat world.
3. Ordinary survival terrain.
4. High entity count.
5. Large particle count.
6. Fast camera rotation.
7. Combat-like rendering workload.
8. Rapid chunk loading.
9. Resource-pack switching.
10. Multiple HUD modules enabled.

Measure average FPS and frame-time percentiles.

Collect evidence of stutter, memory growth, excessive allocations, and rendering stalls.

Make optimizations incrementally and compare benchmarks.

Reject changes that significantly worsen frame-time consistency without a clear user benefit.

---

# 62. ERROR HANDLING

The client must recover sensibly from common problems.

Examples:

- Invalid cape PNG.
- Corrupted animation.
- Missing texture.
- Missing font.
- Corrupted configuration.
- Unsupported resource pack.
- Failed file import.
- Conflicting keybind.
- Unavailable optional dependency.
- OpenGL feature not supported.
- Profile incompatible with a newer config format.
- World or server disconnect.

Display useful error messages.

Write structured diagnostic logs.

Do not silently overwrite user data.

Avoid crashing the entire client because one optional cosmetic fails to load.

---

# 63. SECURITY AND PRIVACY

This is a local-first application.

Requirements:

- No telemetry by default.
- No hidden analytics.
- No unnecessary background services.
- No cryptocurrency mining.
- No remote code execution.
- No credential harvesting.
- No spyware.
- No arbitrary external script downloading.
- No secret data upload.
- No bypass of account authentication.
- No unapproved client injection into third-party applications.

Do not use unsafe deserialization.

Validate imported files.

Place size and resource limits on animated images.

Keep local configuration and cosmetics in user-controlled directories.

Document every network connection the client makes.

Minecraft authentication and normal multiplayer networking remain permitted and necessary.

---

# 64. FEATURES TO EXCLUDE

Do not spend development time recreating Lunar's proprietary online ecosystem.

Exclude:

- Lunar friends network.
- Lunar social account infrastructure.
- Lunar cosmetics store.
- Lunar subscription management.
- Lunar payment systems.
- Lunar-hosted messaging.
- Lunar-owned authentication infrastructure.
- Lunar matchmaking services.
- Lunar-branded servers.
- Lunar cloud profile synchronization.
- Lunar-hosted screenshot services.
- Lunar promotional systems.
- Lunar remote cosmetic ownership verification.
- Lunar-specific server integration APIs.

Do not recreate official Lunar branding.

Do not connect to Lunar's private services.

The client should remain useful without an internet connection, apart from legitimate authentication and multiplayer activities that inherently require connectivity.

---

# 65. FEATURE RESEARCH AND COMPLETENESS REQUIREMENT

Before finalizing the implementation scope, perform a systematic feature audit.

Read available official Lunar documentation, relevant 1.8.9 patch notes, and changelogs.

Create a feature matrix with the following columns:

| Field | Meaning |
|---|---|
| Feature | Name of the feature |
| Description | Actual user-visible behaviour |
| Evidence | Reference documenting the feature |
| 1.8.9 relevant | Yes/No/Uncertain |
| Priority | Critical/High/Medium/Low |
| Difficulty | Estimated engineering difficulty |
| Dependency | Required subsystem |
| Status | Planned/In progress/Implemented/Tested |
| Tests | How it will be verified |
| Notes | Limitations and differences |

Pay particular attention to renamed or consolidated modules.

For example, do not count a deprecated overlay mod and its replacement as completely independent requirements if their functionality overlaps.

Do not claim that every current Lunar Client feature exists on Minecraft 1.8.9.

If the available documentation cannot establish a setting's exact behaviour, clearly identify it as a design interpretation rather than a verified fact.

Use the actual 1.8.9 release as the technical source of truth for supported game mechanics.

---

# 66. PHASED IMPLEMENTATION

Do not try to implement the entire project as one giant code change.

Use incremental working milestones.

## Phase 1: Functional foundation

Deliver:

- Working Minecraft 1.8.9 launch.
- Independent client architecture.
- Basic module system.
- Event system.
- Configuration management.
- In-game settings menu.
- Reliable clean shutdown.

Acceptance criteria:

The game launches, loads worlds, connects to compatible servers, and persists settings after restart.

## Phase 2: Essential PvP modules

Implement:

- Keystrokes.
- CPS.
- FPS.
- Ping.
- Coordinates.
- Toggle Sprint.
- Toggle Sneak.
- Zoom.
- Crosshair.
- Armor Status.
- Potion Effects.
- Scoreboard.

Acceptance criteria:

Every module can be enabled, disabled, configured, positioned, and saved.

## Phase 3: Complete HUD editor

Implement:

- Drag and drop.
- Snapping.
- Scaling.
- Alignment.
- Presets.
- Colours.
- Borders.
- Backgrounds.
- Fonts.
- Resolution-independent positions.

Acceptance criteria:

The user can fully customize their PvP HUD without manually editing configuration files.

## Phase 4: Cosmetic engine

Implement:

- Static cape rendering.
- Animated cape rendering.
- Cape importer.
- Cape selector.
- Cape animation controls.
- Cape physics.
- Profile-specific cape choices.

Acceptance criteria:

The user can import their own PNG or animated cape, select it in-game, and see it working in third-person without Forge.

## Phase 5: Combat visuals

Implement:

- 1.7-inspired animations.
- Hit Colour.
- Item Customizer.
- Block Overlay.
- Particle Changer.
- Freelook.
- Snaplook.
- Hurt Cam.
- Motion Blur.

Acceptance criteria:

Features are visually correct and do not alter combat physics or gameplay packets.

## Phase 6: Performance optimization

Implement and measure:

- HUD caching.
- Rendering improvements.
- Chunk processing improvements.
- Entity rendering optimizations.
- Allocation reduction.
- Frame-time instrumentation.
- Video settings.
- FPS caps.

Acceptance criteria:

Demonstrable performance measurements, no major regressions, and stable frame-time behaviour.

## Phase 7: Bedwars and advanced modules

Implement:

- Bedwars resource tracking.
- TNT countdown.
- PvP Info.
- Combo Counter.
- Reach Display.
- Item Counter.
- Waypoints.
- Inventory customization.
- Chat enhancements.
- Additional world utilities.

Acceptance criteria:

All game-mode-specific information is correct or clearly identified as estimated.

## Phase 8: Full visual polish

Implement:

- Complete interface animations.
- Theming.
- Custom sky functionality.
- Resource-pack management.
- Menu customization.
- Screenshot manager.
- Remaining applicable features.

Acceptance criteria:

The client feels cohesive and professionally designed.

## Phase 9: Cross-platform testing

Test on supported Windows and Linux environments.

Resolve:

- Font rendering issues.
- Incorrect key mapping.
- OpenGL errors.
- Fullscreen bugs.
- File path problems.
- Resolution scaling.
- Input delays.
- Resource loading failures.

## Phase 10: Final packaging

Deliver a reproducible build and clear installation instructions.

The installation must not require the user to compile Java code manually.

---

# 67. REQUIRED DEVELOPMENT WORKFLOW

Follow this sequence:

1. Inspect the current repository.
2. Identify the available development environment.
3. Research the most suitable compatible Minecraft 1.8.9 development approach.
4. Document architecture and important technical constraints.
5. Create an implementation plan.
6. Build a minimal launchable client.
7. Compile it.
8. Execute automated tests.
9. Verify actual game startup where possible.
10. Implement the next group of features.
11. Recompile.
12. Re-test.
13. Update documentation and the feature matrix.
14. Repeat.

Never postpone all compilation and testing until the end.

Never mark a module complete when only its UI exists.

Avoid large speculative rewrites if a smaller working change can achieve the same result.

When modifying existing code, preserve working features and prevent regressions.

Commit work in coherent, reviewable steps when the environment supports version control.

---

# 68. DELIVERABLES

The finished project must include:

1. Full original source code.
2. Reproducible Gradle build.
3. Working Minecraft 1.8.9 client installation or patch-based installer.
4. Appropriate licensing notices.
5. Installation instructions.
6. User documentation.
7. Module documentation.
8. Configuration documentation.
9. Feature-completeness matrix.
10. Performance results.
11. Known issues.
12. Test results.
13. Example configuration profiles.
14. Example user-created cosmetic file format documentation.
15. Working custom static and animated cape system.

If the environment supports generating compiled artifacts, provide them.

If it cannot compile or run the game because required assets, authentication, or graphical infrastructure are unavailable, state the precise blocker and provide the completed source and reproducible next steps.

Do not claim the resulting software has been executed if it has only compiled.

---

# 69. DEFINITION OF DONE

The project is not complete merely because the game launches.

The target is a usable Minecraft 1.8.9 client that provides the experience of a mature PvP-focused client.

For a final release, verify:

- Minecraft 1.8.9 launches.
- Singleplayer works.
- Standard multiplayer works.
- Legitimate authentication works.
- Forge is not required.
- Fabric is not required.
- Mods can be enabled and disabled.
- Configurations persist.
- HUD elements can be edited.
- Core PvP modules function.
- Animated capes render correctly.
- Static capes render correctly.
- Resource packs work.
- Keybinds function correctly.
- Video settings function.
- Input remains responsive.
- Performance has been measured.
- The client handles ordinary errors.
- Windows and Linux support is tested or limitations documented.
- Installation instructions are complete.

A module can only be marked TESTED when an appropriate test has passed.

Every unimplemented or partially functional feature must remain visible in the status matrix.

Do not hide unfinished functionality.

---

# 70. FINAL DIRECTIVES

This is a substantial, long-term engineering project.

Do not attempt to deceive the user by implementing dozens of empty settings panels and calling the product finished.

The user values actual functionality above appearance.

The client should be:

- Fast.
- Stable.
- Lightweight.
- Customizable.
- Responsive.
- Forge-free.
- Focused on Minecraft 1.8.9.
- Friendly to PvP.
- Compatible with normal multiplayer.
- Local-first.
- Easy to maintain.
- Free of unnecessary online infrastructure.

**The two most important distinguishing features are exceptional PvP performance and completely independent custom animated cape support.**

Aim for a level of polish comparable to established Minecraft PvP clients while maintaining an independent implementation.

Do not sacrifice frame-time consistency to chase inflated average-FPS benchmarks.

Do not make the user install a large unrelated modding stack to change one cosmetic.

Do not introduce unnecessary dependencies.

Do not copy proprietary code.

Do not automatically change gameplay mechanics.

Do not present a mockup as completed software.

## BEGIN IMPLEMENTATION

Start by:

1. Auditing the environment and repository.
2. Selecting a legally compliant, technically viable Forge-free Minecraft 1.8.9 development architecture.
3. Producing the complete feature matrix.
4. Creating the build system.
5. Implementing the minimal launchable client.
6. Compiling and testing that first milestone.

Then continue iteratively toward the complete feature specification above.

Prioritize working software, preserve progress between stages, and record all remaining work clearly.

Do not substitute another long planning document for implementation once an executable development path has been established.
