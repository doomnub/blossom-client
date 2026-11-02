# Linux world-entry freeze mitigation

A normal Blossom session freezes on the reported Fedora/Wayland system while
`BLOSSOM_SAFE_MODE=1` does not. This isolates the regression to patched execution,
but does not identify a particular patch or establish a memory leak.

Linux now defaults to vanilla renderer scheduling and buffers. The performance
and renderer-plus transformers are not installed: chunk queues, worker counts,
upload draining, model batching, translucent sorting, region reuse, lightmap
caching and extra display polling use the vanilla paths. Runtime-only performance
helpers are also gated. Saved profiles cannot bypass this policy. HUD, input
bindings, zoom, skins/capes, chat, item fixes and the remaining Blossom modules
stay active. Benchmark CPU timings remain available; GPU timestamps are disabled
because the custom render wrapper is absent.

This is a targeted regression rollback, not proof of the precise root cause.
Windows retains its existing renderer policy. Developers can explicitly select
`-Dblossom.experimentalRenderer=true` or `false` on the **game JVM** for isolation;
that override is not a recommendation for affected Linux users.

A low-priority daemon observes frame heartbeats once per second. After a stall
of at least five seconds it writes thread stacks, heap/direct-buffer usage and GC
counts/times to the instance's `blossom/diagnostics/stall-1.txt` (up to `stall-3.txt`).
One report per continuous stall, at most three per process; fixed filenames are
reused across sessions. No jcmd/JDK install, external process, network upload,
heap dump or forced garbage collection is involved. A JVM-wide pause can delay
the reporter too, so the absence of a report does not exclude a freeze.

Validation: real Mojang bytecode checks cover both renderer policies, verify that
compatibility preserves module hooks and omits experimental renderer calls; Java
8 API/bytecode build and linkage checks pass. Actual affected-hardware gameplay
validation is still required. Included in 0.2.9.3; older release assets are unchanged.
