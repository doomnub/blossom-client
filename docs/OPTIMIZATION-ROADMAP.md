# Optimization implementation status

This is a staged implementation of the requested optimization list, not a claim
that every named technique is complete. Current source is an unreleased checkpoint
on top of 0.2.9.1. No gameplay speedup has been measured for this checkpoint.

## Added in this checkpoint

| Area | Implementation and limits |
| --- | --- |
| Region cache storage | Reuse the vanilla 8,000-entry light and block-state arrays per rebuild worker. Existing light sentinel reset is retained; block references are cleared on normal/exceptional exit. Nested/additional caches and disabled scopes allocate independently. No cached world values cross rebuilds. |
| Translucent sorting | Stable primitive merge sort replaces boxed indices/comparator/BitSet and per-cycle buffer slices. Thread-local scratch is reused up to 65,536 quads; larger sorts use temporary storage. Complete vertex records move together and ties retain their order. Configurable vanilla fallback. |
| Chunk priorities | Bounded 100-entry primitive-backed priority queue scores distance and camera direction at submission. Stable ties, cancellation removal, duplicate task suppression and aging after 64 newer submissions. Queued scores are not recomputed when turning. Vanilla section task cancellation/deduplication remains authoritative. |
| Adaptive scheduling | Smoothed main-frame work, excluding Display update and limiter wait, reduces the configured upload budget under load. Minimum 250 microseconds; one indivisible upload may exceed it. This does not preempt a mesh build/upload. Target FPS is configurable. |
| Particle visibility | Conservative interpolated billboard bounds reuse the current vanilla frustum without allocating bounding boxes. Specialized particle renderers remain untouched; simulation/spawning is unchanged. Frustum reference resets each frame. |
| Lightmap uploads | Exact 256-pixel comparison suppresses identical uploads; retains vanilla binding/sampler state. Texture deletion, world changes, pixel changes and anaglyph changes invalidate reuse. Lighting calculation itself remains vanilla. |
| GUI batches | Rounded-panel rectangles use one draw per panel and cached row insets. Actual graphical acceptance is still required. |
| Hot-path allocations | Event listeners use immutable arrays rebuilt on subscription changes; idle tick bus avoids timestamp/boxing. FPS statistics reuse sort scratch storage. |
| Screenshots | Reused readback buffers; bounded single in-flight image; conversion, PNG encoding and filesystem work move to a lazy background worker. GPU readback remains synchronous. Quitting during a save can interrupt the daemon writer. |
| GPU profiling | Optional 16-pair timestamp pool, capability detection, availability checks before reads, dropped samples rather than waiting. No glFinish. Captures renderer intervals including dependencies/idle gaps, not isolated per-stage GPU work. Query objects are deleted on capture finish. |
| Benchmark analysis | Python archive analyzer reports FPS/lows/percentiles, memory/queue peaks, slow frames with correlated phases/GPU samples, and optional baseline percentage differences. Compatible with old five-file captures. Synthetic tests validate analysis, not real gameplay speed. |

## Existing foundations retained

Vanilla 1.8.9 already has terrain/entity frustum checks, block face rejection,
OpenGL backface culling, section visibility connectivity, distance checks,
chunk meshes, optional VBO terrain, separate opaque/translucent layers,
translucent sorting, texture/font atlases, GL state and texture binding caches,
block model baking, particle texture-layer batches, asynchronous chunk workers,
section dirty flags, pending-task cancellation and network decoding threads.
These are foundations, not equivalent to a modern GPU occlusion/indirect renderer.

Earlier Blossom work adds limited immediate chunk builds, configurable workers
and upload budgets, reduced upload-queue lock scope, model-face/cape batches,
zoom frustum invalidation, conditional HUD string/width refresh, input polling
before the tick, disabled-module guards, and bounded opt-in CPU/resource/chunk
profiling. Existing functionality should not be renamed as a new optimization.

## Remaining dependency order

1. **Obtain representative captures.** Warmed stationary rendering, rapid travel,
   chunk edits, translucent scenes, entities/particles and world transitions on
   both Radeon/Linux and NVIDIA/Windows. Compare repeated routes and visible chunk
   completion, not just FPS. Add automated route playback and regression thresholds
   only once stable workloads and run-to-run noise are known.
2. **Chunk data/meshing architecture.** Immutable preparation snapshots, neighbor
   and lighting caches, reduced allocations/copies, pooled/preallocated mesh
   buffers, unchanged-section detection, finer dirty regions, reusable geometry,
   incremental/partial work, improved translucent resorting and unload cleanup.
   Preserve block/tint/light invalidation and resource-pack behavior. Tune worker
   contention, cancellation, task batching and background throttling from captures.
3. **Modern terrain backend.** Capability-gated VAOs, compact formats, indexed or
   packed geometry, buffer arenas, staged uploads, fences and double/triple buffering.
   Then persistent mapping, multi-draw indirect, render command batching and
   GPU-driven submission. Persistent mapping requires GL 4.4 or ARB_buffer_storage;
   indirect multi-draw requires GL 4.3 or ARB_multi_draw_indirect. Keep the existing
   backend available. These capabilities being reported in a benchmark does not
   mean Blossom currently uses them.
4. **Visibility backend.** Conservative occlusion queries/Hi-Z with temporal
   validity, camera/FOV/teleport invalidation and fail-open behavior. Extend to
   entities/block entities only with transparent geometry and dynamic bounds
   accounted for. Fog culling requires matching the actual fog shader/alpha behavior.
5. **Entity and effects pipeline.** Instance/batch compatible models, armor/items,
   nameplates/text, shadows and particles; cache transforms/interpolation only
   across unchanged inputs. Parallel animation/particle preparation requires owned
   snapshots and deterministic integration. Do not move mutable world tick methods
   blindly to worker threads. Spatial indexing/collision/ray-trace caches need
   correctness and invalidation coverage before replacing vanilla queries.
6. **Assets, IO and transitions.** Profile NBT/decompression/packet initialization,
   chunk caches, disk reads, resource-pack deduplication, background image/texture
   decoding, atlas/mipmap/format/compression changes, texture staging/reuse, block
   baking/tint caches, sound decode/buffer/source reuse and reload/world-switch
   cleanup. Texture compression must preserve pack alpha and visual quality.
7. **Input/network/runtime.** Measure actual input latency separately from FPS;
   native raw input and late sampling are platform-specific. Preserve packet order
   and thread ownership when reducing allocation/copies or batching dispatch.
   Benchmark Java versions, heaps/collectors/JIT warmup before adopting flags.
   SIMD, lock-free queues, false-sharing padding and off-heap storage need measured
   hotspots; they are not universally faster replacements.
8. **Close measurement gaps.** Allocation/RSS/VRAM, per-stage GPU timers, physical
   input latency, network/decompression/disk attribution, automated gameplay
   benchmarking and performance regression gates remain outstanding. Current
   section correlation does not prove a frame spike's cause.

The remaining groups include the requested cloud/sky/weather/water/foliage,
lighting interpolation, GUI/glyph batching, audio, collision and runtime topics.
No entry in those groups should be marked implemented without an actual code path,
fallback policy, invalidation behavior and relevant validation.

## Validation required before release

Headless tests cover queue ordering/cancellation/aging, adaptive budgets, timer
availability/lifecycle, benchmark serialization, analysis math and transformed JVM
linkage. They cannot accept graphics, input feel or performance. Test zoom/turns,
teleports/dimensions, chunk edits, particles, anaglyph/fullbright, resource reloads,
screenshots including non-native framebuffer sizes, unsupported timer hardware,
and Performance off/on in real gameplay before publishing this checkpoint.
