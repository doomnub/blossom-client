# Blossom 0.2.6 validation

## Fixes and automated coverage

- Native release builds enable `tauri/custom-protocol`, embedding the frontend instead
  of loading the Vite localhost URL. A compile-time guard rejects release builds
  missing that feature. Linux CI starts the real release binary under WebKitWebDriver
  and checks the embedded Play/Settings pages, native Rust IPC and migrated settings.
- Cape faces submit interpolated vertex normals with GL smooth shading. A separate
  render-only shell gives 1/32-block thickness, with independent perimeter normals
  preserving sharp edges. The solver remains one surface.
- Crouching applies the player model's 0.2-block body translation to anchors and
  colliders. An oriented padded torso box covers jacket corners; padded limb capsules
  account for sleeve/leg layers and the rendered shell.
- Long-range top-row tethers cap attachment stretch at 0.5%, independent of mesh
  resolution. Headless fast-movement tests cover 4, 16 and 64 subdivisions.
- Ping resolves the current connection's login profile each tick while enabled,
  clears state on connection changes, and treats an unmeasured remote zero as
  awaiting server data. No probes or fabricated RTT measurements are added.
- Left Hand uses the actual left-arm/sleeve skin, local third-person arm pose/item
  attachment, reversed item winding, and a vanilla two-handed map exception.
- Hit Tilt scales the vanilla 14-degree hurt amplitude from 0–200%; death rotation,
  camera bob, damage and knockback are untouched. Settings persist through restart.

Local Java checks: 86 core checks, cloth tests, 14 real vanilla transformer targets,
full JVM verification/linkage and 49 legacy launcher checks pass. Native build and
real desktop smoke results are checked in CI before publication.

## Disabled-module work

FPS buffers are reset and sampling/sorting stops unless FPS diagnostics are enabled.
CPS recording stops when disabled. Disabled cape decoding/uploads are canceled,
textures/meshes are released, and the decoder worker expires when idle. Network
observers detach; hit history and disabled HUD caches clear. FOV/zoom and key queries
return early when their modules are disabled. The shared hook dispatch and small
settings objects remain; this does not promise literally zero memory or zero CPU.
The Windows JInput library owns a process-wide raw-input event thread after first use;
Blossom stops polling when disabled but does not tear down shared library internals.

## Still requires graphical gameplay acceptance

Test standing/crouching, sprinting/flying/stopping, turns, jackets/sleeves, custom cape
animation and low/high resolutions in Minecraft. Verify left-hand empty/sword/bow,
eating/blocking/map and slim/regular skins in first and third person, and toggle all
new modules repeatedly. Check singleplayer → multiplayer → another server → disconnect,
including proxy/offline UUID differences and delayed player-list latency packets.
Actual Minecraft rendering and the reported live-server sequence are not reproducible
in this headless workspace. Cloth self-collision remains bounded vertex/triangle
projection, not full continuous edge/edge collision. Fast folds can still intersect.
