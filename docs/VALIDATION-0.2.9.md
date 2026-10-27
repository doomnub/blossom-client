# Blossom 0.2.9 validation

- Java 8 API/bytecode build succeeded with ECJ `--release 8`.
- Core checks, cloth checks, disabled-module checks, actual Minecraft 1.8.9
  transformer checks and 49 legacy-launcher checks passed.
- New checks cover worker bounds/settings persistence, retained slow frames and
  mean/slow-tail statistics, plus expected model/chunk/GC hook instructions.
- Transformed model, quad, render-global and chunk-dispatcher classes and the new
  integration helpers passed JVM verification/linkage on Java 17 and Azul Java
  8.0.504 without initializing a graphical game.
- `git diff --check` passed.

Not performed: graphical gameplay, GPU output comparison, FPS/low-percentile A/B
measurements, chunk-completion timing, physical input-latency measurement, or a
native launcher rebuild locally. No measured speedup is claimed. See
PERFORMANCE.md for the measurement procedure and implementation limits.
