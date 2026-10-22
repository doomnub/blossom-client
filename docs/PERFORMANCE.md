# Performance — no gameplay benchmark yet

FrameStats records a bounded 10,000-frame ring. Snapshot computation sorts only
once per second. Average FPS is inverse mean frame duration; 1%/0.1% lows are
inverse means of the slowest ceil(N*0.01) / ceil(N*0.001) samples. The HUD labels
fewer than 1,000 samples as warming up. Frame boundaries are now sampled at the
start of Minecraft's main game-loop method, including menus, hidden-HUD periods,
and intervals longer than one second. These stalls are retained in the slow tail.
World loading and startup transitions can influence the rolling sample window;
controlled benchmark scenarios still require separate capture/reset support.

No FPS improvement, 165 FPS guarantee, input latency improvement, or memory
reduction has been measured. No optimization is marked as validated.

Next benchmark gate: use identical Java 8, hardware, resolution, render distance,
VSync/cap, world, and route for vanilla and Blossom. Record ten scenarios from
SPECIFICATION section 61. Collect full render-loop intervals including stalls,
p50/p95/p99/p99.9, mean slow-tail FPS, allocation/GC, and RSS/heap. Warm up before
recording, repeat runs, retain raw data, and report variance. Additive HUD overhead
and texture upload stalls must be measured before enabling aggressive features.
