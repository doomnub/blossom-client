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
