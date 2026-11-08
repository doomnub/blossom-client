# Custom Skies

Select an ordinary 1.8.9-compatible resource pack in Minecraft. Custom Skies is
on by default and reads the pack through Minecraft's resource manager; ZIP and
folder packs do not need extraction or a separate Blossom configuration.

Supported locations are `assets/minecraft/optifine/sky/world<dimension>/` and the
older `assets/minecraft/mcpatcher/sky/world<dimension>/`. Numbered `sky0` through
`sky127` properties and the legacy unnumbered `sky.properties` are supported.
The modern path takes precedence if both forms define the same layer. Individual
resources use the normal selected-pack precedence supplied by Minecraft.

The independent implementation follows the public
[OptiFine sky format](https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/sky.properties).
The [MCPatcher reference](https://github.com/pclewis/mcpatcher/blob/master/newcode/src/com/pclewis/mcpatcher/mod/SkyRenderer.java)
was consulted for the atlas orientation, rotation coordinate convention, and
legacy blend factors. No dependency on OptiFine is installed.

Features:

- Standard 3-by-2 skybox atlas, with bottom/top/south/east/north/west tiles.
- Relative, pack-root, `~/`, and namespaced source paths; omitted sources use the
  layer's PNG filename.
- Midnight-wrapping fade intervals, inferred `startFadeOut`, and always-on layers.
- `add`, `alpha`, `subtract`, `multiply`, `dodge`, `burn`, `screen`, `overlay`,
  and `replace` blending.
- Rotation axis/speed, day loops, weather, 1.8.9 biome names/IDs, heights, and
  smooth location transitions.
- `sun.properties` and `moon_phases.properties` source/blend overrides.
- Complete six-face coverage with lower-horizon suppression and smaller sky geometry to avoid low-render-distance clipping.
- Overworld and End skies; the Nether retains its vanilla environment.
- Time Changer drives layer fades and sky rotation along with vanilla sunlight.

One decoder thread handles at most one queued replacement request and expires
when idle. No world/entity objects are captured by decoding tasks. Images are
shared between layers, decoded off the render thread, and uploaded at most once
per client tick. Reload, disconnect, dimension change, and disabling the module
cancel obsolete work and delete owned textures. No GPU queries or worker GL
contexts are used.

Limits: 128 numbered layers, 32 million decoded pixels per active dimension,
32 MiB compressed bytes per image, 64 KiB per properties file, and 8192 pixels
per image axis (also subject to the GPU limit). Oversized/malformed resources
are skipped with a generic log message, without retaining personal filesystem
paths. Animated sky PNG metadata, shader packs, custom lightmaps, and other
OptiFine features are outside this module.

Before release, visually check a normal PvP sky pack and a six-face orientation
fixture, sunrise/midnight fades, End skies, pack reload, disabled restoration,
and Free Look interactions with zoom and particles. Headless checks do not
establish visual equivalence with OptiFine.
