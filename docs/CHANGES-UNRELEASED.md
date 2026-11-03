# Unreleased changes after 0.2.10

- Add opt-in Performance > Support-corner frustum (experimental). Keep the common first-corner
  early-out, then test the support corner instead of up to seven more corners.
  Preserve vanilla boundary decisions and fall back for nonfinite/malformed input.
  The switch defaults off; module-off and -Dblossom.fastFrustum=false also bypass it.
- No visibility cache, world references, background threads, scheduling changes,
  or GPU resources are added. Zoom/projection changes use current planes.
- Extend per-frame benchmarks with nearby rebuild, camera-section visibility scan,
  main-thread chunk rebuild, weather and particle timings. The existing culling
  section includes nearby rebuild work and should not be read as pure box testing.
- Differential checks compare against vanilla for randomized boxes, exact clipping
  boundaries, large coordinates, invalid inputs and live plane changes.

No version bump or release. Gameplay performance and hardware visual acceptance
remain unmeasured. User benchmark archives and derived measurements are private.

Synthetic timings were workload-dependent and did not establish a consistent
speedup. This experiment stays off by default pending gameplay comparisons.


## Bedwars-focused rendering work

- Batch fancy-cloud cells within each depth/color pass. Preserve vertex order,
  positions, UVs, colors, normals and pass masks. Flush at whole-cell boundaries
  after 4,096 vertices and before state changes; use the existing vanilla buffer.
  No retained cloud mesh, GPU allocation, worker or upload queue is introduced.
  The CPU-only fixture preserves identical bytes while reducing 128 draws to 4;
  that is a synthetic submission count, not a measured gameplay speedup.
- Cache chat component formatting within a game tick, invalidating on layout and
  message changes. Limit retention to 128 components and 65,536 characters; bypass
  oversized values. Text drawing, fade, clicks, hover and obfuscated glyphs remain
  live. This reduces repeated formatting, not font draw calls.
- Enable conservative billboard particle frustum culling independently of the
  legacy renderer group on Linux. Keep specialized renderers and simulation
  unchanged. Use current-frame camera and interpolated quad bounds, with an
  outward margin and conservative fallback for invalid coordinates.
- Expose independent Performance toggles for clouds, chat text and particles.
  The Performance module bypasses them; cloud batching also has startup override
  -Dblossom.cloudBatching=false.
- Add per-frame chat timings. The prior support-corner experiment remains off.
  The old Linux scheduling/buffer optimization group stays disabled.

Build/geometry checks do not replace on-device checks of cloud depth, anaglyph,
resource packs, fast camera turns, particle edges and world transitions. No release
is published and no private benchmark data is included.
