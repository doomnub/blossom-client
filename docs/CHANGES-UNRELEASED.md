# Unreleased changes after 0.2.11

- XP Changer: a saved 0–100% slider changes only the hotbar XP fill. XP levels,
  earned experience, enchanting, and packets keep their original values.
- Time Changer: a saved midnight-to-midnight slider overrides the client celestial
  angle, affecting sunlight/lightmap brightness, fog, clouds, sun, moon, and sky.
  No world time is written and no commands or time packets are sent.
- Custom Skies: enabled by default; automatically discovers selected-pack OptiFine
  and MCPatcher sky layers. Includes fade times, nine blend modes, rotation,
  weather, days, biome/height filters, transitions, and sun/moon texture overrides.
  Uses bounded background image decoding and explicitly releases owned GPU textures.
- Free Look: hold Left Alt (rebindable in module settings or Minecraft Controls)
  to orbit in rear third person without changing player aim. Restores the previous
  perspective on release, menu/focus changes, disable, and world changes. Camera
  angles also drive terrain visibility, particles, and nameplate billboards.
- The game window uses a large pink block icon; the launcher keeps its flower.

Validation: Java 8 core/bytecode checks, selected-pack loader fixtures, invalid-image
bounds, property parsing, and transformed-class verification. Graphical acceptance
is still required: this environment has no X display, and installing a virtual
one was blocked by container package-install permissions. These changes have not
been published as a release.
