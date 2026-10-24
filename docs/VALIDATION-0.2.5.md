# 0.2.5 validation

Implementation checkpoints; desktop acceptance remains outstanding. Do not treat this document as a completed graphical acceptance test.

## Automated checks

- Java 8 bytecode compilation, actual vanilla transformer matching, full JVM verification.
- Settings pagination boundaries and empty-potion preference persistence.
- Cloth anchoring, fixed timestep consistency, finite positions, lag and teleport resets.
- Rust settings migration, private account storage, offline UUID, cancellation,
  archive traversal rejection, library rule selection and secret-free launch arguments.
- Strict TypeScript compilation and production frontend build.

Rust installer reused the prior launcher cache and managed Java with unreachable
HTTP/HTTPS proxies: all 734 assets and 32 classpath entries verified successfully.

Other-player cloth uses at most 16 subdivisions (8 beyond eight blocks), at most
32 cached simulations, and no strong world references in the simulation cache.

## Desktop acceptance procedure (Windows 11 and Fedora)

1. Copy an existing 0.2.4 configuration into a test user profile. Verify the same
   game path, custom Java, RAM, resolution, offline name and application ID appear.
2. Restore a saved sign-in, sign out, sign in through Microsoft's device flow,
   restart, then launch on an authenticated server. Test invalid/revoked credentials.
3. Install/repair an empty directory; cancel during assets/Java; restart; corrupt a
   cached file and repair. Disconnect networking and launch the complete cached install.
4. Check live stdout/stderr, error display, running status and nonzero game exit.
5. Test every settings page in both directions, at small and large GUI scales.
6. Potion HUD: no effects, active effects, empty toggle, editor preview and restart.
7. Hit tint: player, mob, armor, repeated damage, duration, fade, disable restores red.
8. Particles: each category, multiplier/density, scale, opacity, lifetime, original color,
   changed color and disabled module. Compare world and block effects explicitly.
9. Network HUD: singleplayer, multiplayer, reconnect, disconnect, disable and re-enable.
   Throughput measures observed bytes; latency is server player-list data, not a new probe.
10. Capes: stand, walk, sprint, jump, fall, strafe, stop, turn rapidly, sneak, swim using
    1.8.9 movement, teleport, respawn and change dimension. Test first/third person.
11. Test vanilla, custom static, animated and absent cape textures. Check white fallback
    only with physics enabled; custom hidden cape remains hidden. Toggle both modules.
12. Change subdivisions 4/16/64, gravity, damping, stiffness, iterations, both collision
    toggles and wind. Observe stretched cloth, jitter, clipping, fold intersections,
    texture edges, FPS and allocation behavior with multiple visible players.
13. Repeat cloth tests at 30/60/165 FPS and after a pause/stall. Invisible/distant capes
    should not consume active solver work. OptiFine compatibility is not established.

## Collision/performance notes

The solver is a 120 Hz Verlet/PBD grid, with structural, shear and two-hop bending
constraints. Top vertices are pinned; a triangle AABB spatial hash limits self-collision
candidates. Vertex/triangle plane projection includes a short swept crossing check.
It is not full continuous edge/edge collision: very fast or extreme folds can still
intersect. Seven simplified animated-body capsules replace mesh/mesh collision.
Self-collision has a 250,000-candidate budget and bounded hash storage; exhausting a
budget trades collision coverage for a bounded frame cost. This must be evaluated
in gameplay, particularly at 64 subdivisions. Eight catch-up steps maximum; large
stalls, world changes and teleports reset the cloth.

Initial headless CPU timings (120 stationary/moving steps, not gameplay FPS):
4 subdivisions / 20 vertices ~41 ms; 16 / 187 ~43 ms; 64 / 2665 ~544 ms.
These are single-run environment measurements, not performance guarantees.
