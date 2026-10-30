# Performance work in 0.2.9.1

Implemented mechanisms are verified by bytecode checks and JVM linkage, but no
hardware gameplay FPS, frame-time, loading-time or input-latency improvement has
been measured yet. This is an initial performance pass, not a replacement terrain
renderer or a promise of a particular percentage gain.

## Implemented

- **Model batching:** submit all box faces in a ModelRenderer display list as one
  vertex batch instead of a draw per face. Per-vertex UVs and normals remain intact.
  Changing the setting invalidates compiled model lists and frees the old lists.
- **Cape geometry:** stream the front/back surface and thickness edges through
  two reusable vertex-buffer batches, avoiding per-vertex OpenGL calls. Shares
  the model-batching switch; simulation, UVs, thickness and lightmap stay intact.
- **Chunk scheduling:** keep one nearby section rebuild immediate per frame and
  queue the others through vanilla's workers. A configurable 0.5–8 ms budget
  (default 2 ms) covers that immediate work and the following upload loop. A
  single rebuild/upload cannot be interrupted, so this is not a hard frame limit.
- **Chunk workers:** auto-select 1–4 threads based on CPU count, with an explicit
  override. Keep vanilla's five-buffer pool and shutdown accounting intact.
  Worker changes, including restoring vanilla's two workers by disabling the
  module, require a restart. More workers can contend with the integrated server;
  fewer workers may be better on small CPUs.
- **World transitions:** skip the two explicit garbage collections in world
  loading/unloading. Automatic GC and emergency out-of-memory recovery remain.
- **Input:** poll LWJGL messages at frame start, after the previous frame's limiter
  wait and before tick processing. Keep vanilla input events and simulation rate.
- **HUD/allocation:** build each HUD's lines and text measurements once per draw;
  reuse raw-mouse delta arrays instead of allocating them on each poll.

These are in the Performance module with individual switches. A smaller chunk
budget favors smoother frames; a larger budget favors visible chunk completion.
Client chunk meshing is distinct from world generation, disk loading and server
chunk delivery: this pass does not accelerate all those operations. VSync, GPU
queueing and refresh rate can still dominate input latency.

## Research and further work

Primary sources consulted:

- [Patcher](https://github.com/Sk1erLLC/Patcher): model batching, font rendering,
  removal of forced world-swap GC and configurable entity occlusion.
- [EntityCulling](https://github.com/tr7zw/EntityCulling): asynchronous visibility
  decisions with exclusions. This needs careful handling of nametags, transparent
  blocks and camera changes before integrating into Blossom; not implemented.
- [Sodium](https://github.com/CaffeineMC/sodium) and
  [Nothirium](https://github.com/Meldexun/Nothirium): larger terrain-renderer
  redesigns. These are research references, not drop-in vanilla 1.8.9 dependencies.
- [LWJGL 2 Display documentation](https://legacy.lwjgl.org/javadoc/org/lwjgl/opengl/Display.html):
  message processing/input updates and frame presentation.

Next candidates are terrain batching, font batching and conservative entity
occlusion, guided by CPU/GPU profiling. No source code from these mods was copied.

## Detailed benchmark in 0.2.9.1

Open Performance settings and choose **Capture detailed benchmark (60 seconds)**.
Return to the world to start. The same button finishes early. Each capture writes
one `.benchmark.zip` to the game directory's `blossom/benchmarks` folder, and logs
its exact path. Send that entire file when investigating a performance problem.
It is first written as `.partial`, then renamed after the ZIP closes successfully.

The archive contains:

| File | Measurements |
| --- | --- |
| `summary.txt` | Configuration/hardware, average FPS, 1% and 0.1% lows, median/p90/p95/p99/p99.9 frame times, worst frame, standard deviation, counts over 16.667/33.333/50/100/250 ms, sampled peak memory, GC totals, ranked self-time sections |
| `frames.csv` | Every frame interval, render-thread CPU time, and selected inclusive phase times: ticks, game renderer, display, cap wait, upload drain, cape simulation/drawing, HUD |
| `sections.csv` | Hierarchical vanilla profiler sections, call counts, inclusive time, self time, maximum call; plus Blossom HUD/cape/chunk work |
| `resources.csv` | Heap used/committed/max, nonheap, direct/mapped buffers, JVM GC count/time deltas, threads, process CPU counter, pending chunk jobs/uploads, free chunk buffers |
| `chunk-jobs.csv` | Chunk rebuild, translucent sort, upload task and upload-drain counts, total/max wall times and exceptions, separated into main and worker threads |

Durations/counters use nanoseconds unless named `_ms`; all memory values are bytes.
`-1` means unavailable. Resources are sampled about four times per second. There
is no polling thread when idle. The bounded 120,000-frame store is allocated in
the menu before gameplay capture; disk output, compression and sorting happen
on a background thread after the session detaches. Section tracking uses bounded
reusable stacks and at most 1,024 distinct paths. Diagnostic counts flag forced
closes, unmatched ends and overflow. Worker completions after capture ends are
excluded. Capture retains stalls and menu/world transitions after it starts.

Self time subtracts nested children; inclusive timings overlap and must not be
summed. Chunk worker times overlap the main thread and each other. Counts refer
to completed method invocations, including early-outs/cancelled work, not unique
fully loaded world chunks. Queue depths are snapshots, not throughput counters.
Display timings include driver/VSync blocking; they are not GPU execution times.
GC time is JVM-reported collector time, not total stop-the-world pause duration.
Physical input latency, VRAM, process RSS, disk throughput and server world
generation are not measured. Profiling itself has overhead; compare identical
capture settings. Main-thread CPU timing is unavailable if the JVM disables it.

## Additional optimizations in 0.2.9.1

- Run upload callbacks outside the shared producer-queue lock, so chunk workers
  can enqueue while the main thread submits geometry. Preserve vanilla's one-task
  behavior for a zero deadline and its deadline check after each callback.
- Reuse HUD text widths until text changes; refresh telemetry strings at 20 Hz.
  Keystrokes, clicks and toggle-state text remain live each frame. Refresh width
  caches each second to pick up resource-pack font changes. These HUD/queue
  improvements apply independently of the Performance module switches.

## Comparing runs

Keep Java, hardware, resolution, cap/VSync, resource pack, modules, world and route
identical. Warm up first; repeat at least three times in alternating order. Keep
raw captures and compare run-to-run variation, not just a best run. Separately
measure cached-world entry and travel through new chunks; compare visible chunk
completion alongside frame-time lows. Use external input-to-display measurement
for latency. No measured gameplay speedup is claimed by this change.

## Unreleased rendering checkpoint

New settings provide adaptive upload budgets (target FPS), distance/direction chunk
priority, particle frustum checks and unchanged lightmap upload suppression. Other
additions batch rounded panels and encode screenshots in the background. See
[implementation status and remaining work](OPTIMIZATION-ROADMAP.md) for limits.

Detailed captures now include `gpu.csv` with `frame_index` and
`game_renderer_gpu_interval_ns`; `frames.csv` also includes `frame_index`. Unsupported
hardware produces an empty GPU data file with its status in `summary.txt`. Available
queries are collected later without waiting. GPU timestamp intervals include idle
and dependency time within the game renderer, and are not physical input latency.
Pending samples at capture end are discarded and counted. All six files remain in
one ZIP; metadata distinguishes available hardware features from active features.

Analyze a capture or compare it with a matching baseline (Python 3.9+):

```sh
python scripts/analyze_benchmark.py after.benchmark.zip --baseline before.benchmark.zip --json comparison.json
```

The JSON contains slow frames, overlapping phase timings, GPU correlations and
ranked section self times. Higher FPS/lows and lower frame-time percentiles are
better. A single percentage difference is not proof of an improvement; repeat the
same warmed workload and retain both original archives.

The **Reuse translucent sort buffers** setting replaces vanilla's boxed quad sorting
with a stable primitive sort. Scratch storage belongs to each worker/render thread,
so concurrent builds do not share mutable arrays. Retention is capped at 65,536
quads per thread; oversized meshes use temporary storage. This changes allocation
behavior, not visibility, quad geometry or the transparency ordering policy.
