# Rendering checkpoint validation

Unreleased changes based on 0.2.9.1; existing public version is intentionally
unchanged until a release is prepared.

Executed successfully:

- `python scripts/build.py`: Java 8 API/bytecode compilation, core and cloth tests,
  benchmark serialization checks, disabled-hook checks, transformer hook checks,
  JVM `-Xverify:all` linking, client/legacy launcher packaging, 49 launcher tests.
- New queue/budget/query tests cover bounds, priority order, cancellation,
  duplicate suppression, aging, worker wakeup, adaptive recovery, unavailable GPU
  results and query object deletion. GPU tests use a fake driver, not a real GPU.
- `python scripts/test_analyze_benchmark.py`: synthetic old-format compatibility,
  frame statistics, GPU/frame correlation and empty-capture rejection.
- `git diff --check`: no whitespace errors.

The compiler targets Java 8; the JVM verification run used the installed build
JVM, not a graphical Java 8 Minecraft session. No real gameplay benchmark,
Linux/Windows graphical acceptance or measured FPS/latency improvement is claimed.
Native launcher code was not changed in this checkpoint. Graphical checks listed
in OPTIMIZATION-ROADMAP.md remain required before release.

The subsequent mesh-sort pass adds randomized reference comparisons across vertex
strides, empty/small meshes, tied distances, NaN and repeated scratch reuse. It
compares every vertex attribute against stable boxed reference ordering. The
transformed buffer builder and integration helper are also JVM-verified. No real
scene speedup is inferred from those correctness checks.

Region scratch tests cover sequential reuse, non-aliasing nested/additional caches,
disabled/out-of-scope fallback and reference cleanup on normal/exceptional exits.
Transform checks require both allocation hooks and rebuild scope hooks, and the
JVM verifier links the changed region cache and wrapper.
