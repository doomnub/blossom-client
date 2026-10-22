# Local capes

Open Right Shift > Cape folder. Place original/user-provided PNG artwork here.
Click Next cape to cycle the sorted list; the empty selection restores your
normal Minecraft cape. Enable the Local cape module and Minecraft's own cape
visibility under Skin Customization. Only your local player is overridden.

Supported input:

- Static PNG: 64x32, 128x64, or integer multiples up to 1024x512. Standard Minecraft
  cape UV layout, including the thin edges; not a stretched single face.
- Vertical spritesheet: each frame is width x width/2, stacked downwards.
  Default playback 20 FPS, looping.
- Directory of equally sized PNG frames, sorted by filename (use 000.png, 001.png).
  Optional `animation.properties` sets `fps=24`, `pingPong=true`, and per-frame
  `duration.0=100` in milliseconds. Durations must be 10–60000 ms; FPS 1–100.

Bounds: 256 frames, 16 MiB per encoded PNG, 4096px maximum image side, 4,194,304
pixels per decoded source image, 8,388,608 total sequence pixels. Frame width is
limited to 1024. Frames are decoded on a background worker; cached GL textures
are registered on the render thread. No PNG is decoded each render frame and no
Minecraft texture cache files are overwritten. Replacing a cape frees old GL
textures. An invalid new selection leaves the previously loaded cape visible.

The game's original cape geometry, UVs, sneaking and swing physics are retained.
The texture return hook is shared by player rendering, but actual third-person
and inventory preview correctness still needs graphical verification.

Not yet implemented: horizontal sheets, GIF/APNG, graphical import/preview,
renaming/deletion UI, per-selection FPS UI, pause, speed control, no-cape mode,
custom physics, or resource-reload lifecycle acceptance tests.
