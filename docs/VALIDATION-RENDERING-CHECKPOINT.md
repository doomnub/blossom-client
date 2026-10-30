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
