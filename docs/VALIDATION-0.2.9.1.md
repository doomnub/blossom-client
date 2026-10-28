# 0.2.9.1 validation

Java 8 API compilation and core/cloth/launcher regression checks pass. New tests
exercise nested inclusive/self timing, forced-close and depth limits, slow-frame
retention, main/worker job separation, completion after capture closes, ZIP
contents, queue samples, and upload callbacks running outside the producer lock.
Actual 1.8.9 bytecode is checked for profiler, chunk timing and upload-drain hooks;
transformed classes are linked with JVM verification enabled. Core tests and
transformed-class linkage also pass on Azul Java 8.0.504 with the agent active.
The launcher TypeScript/Vite production build passes.

Graphical gameplay acceptance, measured profiling overhead, CPU/GPU performance
comparisons and physical input-latency measurements have not been performed.
The benchmark report explicitly identifies unsupported metrics and measurement
limits. Four-component public version 0.2.9.1 maps to internal tooling SemVer
0.2.9+perf.1 for Cargo, npm and Tauri.
