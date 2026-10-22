# Local capes

Open Right Shift > Cape folder. Place original/user-provided PNG artwork here.
Open Capes to browse entries, preview playback, or import a PNG/frame directory.
Default cape restores your Minecraft cape; No cape hides it locally. Enable the Local cape module and Minecraft's own cape
visibility under Skin Customization. Only your local player is overridden.

Supported input:

- Static PNG: 64x32, 128x64, or integer multiples up to 1024x512. Standard Minecraft
  cape UV layout, including the thin edges; not a stretched single face.
- Horizontal spritesheet: 2:1 frames placed side by side, inferred from aspect ratio.
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

The browser provides pause/resume, file/default FPS or fixed FPS, speed, looping,
and file/on/off ping-pong overrides. The preview displays the current texture atlas,
not a 3D model. Settings are saved with profiles. Import never overwrites existing
artwork; Rename checks names and Remove asks before moving to `.trash/`.

GPU uploads are staged at most two frames per client tick. The old cape remains
visible until the new sequence is fully uploaded. Cancelled staging textures are freed.

Not yet implemented: GIF/APNG, 3D player preview, custom physics, arbitrary frame
reordering, or resource-reload lifecycle acceptance tests.
