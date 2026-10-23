# Architecture and boundaries

Blossom is a startup Java instrumentation agent, targeting bytecode level Java 8.
The original Minecraft JAR remains intact and signed. The launcher owns normal
Microsoft authentication and launches the game with one additional JVM argument.
Blossom does not implement login, alter access tokens, or attach to running clients.

Before enabling hooks, premain verifies SHA-256 fingerprints of the four expected
vanilla classes. Modified/unsupported layouts are refused. OptiFine is therefore
not currently supported: its own class loader/transformers require a separate
compatibility adapter and actual testing. No Forge or Fabric runtime is involved.

Integration points (MCP 1.8.9 SRG consulted for symbol identification):

| Notch signature | Meaning | Hook |
| --- | --- | --- |
| ave.s()V | Minecraft runTick | Post-tick state, settings, menu, texture upload |
| Mouse.next()Z inside runTick | Consume vanilla mouse events | Observe successful events without consuming another |
| avo.a(F)V | GuiIngame renderGameOverlay | Render enabled HUD widgets at return |
| bet.k()Ljy; | AbstractClientPlayer cape resource | Replace local player's returned texture |

The agent transforms return sites without changing game control flow or networking.
COMPUTE_MAXS preserves existing stack-map frames. Tests transform the real client
classes and run Java's full verifier against the resulting classes.

## Signed-package issue and build relocation

Minecraft's obfuscated classes live in Java's unnamed package and are signed.
Java source in a named package cannot import these classes. The integration
source files compile in the unnamed package, then ASM remaps **our own classes**
into dev.blossom.integration. This avoids signer collisions without removing
Minecraft signatures or redistributing a repackaged game. Do not skip relocation.
ASM Commons is a build dependency only; the runtime bundles ASM core.

Core algorithms, configuration, image validation and modules are independent of
game classes. Minecraft-specific bridge classes are deliberately small and direct.
The event bus currently exposes tick subscriptions only; the full event catalogue
in the specification is not implemented.

## Thread ownership

Game thread: module state, input observation, HUD, texture creation and deletion.
Cape worker: bounded PNG decode and folder opening. Profile worker: serialized
atomic writes. Shutdown waits at most three seconds for queued profile writes.
Changing selections cancels pending work; only the selected future is uploaded.
GL uploads still occur as one batch and need profiling for large animations.

CPS uses monotonic event-processing timestamps, not assumed compatibility between
LWJGL's platform event clock and System.nanoTime. Long blocked input queues can
therefore bunch historical events; this is not hardware timing instrumentation.

## Privacy and network

Blossom runtime makes no network requests. Minecraft/launcher traffic remains
unchanged. Build-time downloads use Mojang metadata and library servers and Maven
Central; all downloaded artifacts are checksum checked. No accounts, tokens,
private profiles or imported cosmetics are committed or uploaded by the client.

Additional hooks: ave.av()V main-loop entry samples every frame; avo.b()Z retains
vanilla crosshair eligibility while suppressing its texture when custom is enabled;
avb.d()Z adapts only the configured sprint/sneak key query. Keyboard.next() events
inside runTick observe press edges. No player movement method is transformed.

0.2.2 adds fingerprint-checked `aya` (GuiMainMenu) init/action hooks. The vanilla
screen passes its button list and dimensions into Blossom; no reflective access
to protected screen fields is needed. Sprint key reads use `avh.ae`, verified
against the vanilla constructor's `key.sprint` declaration. No player movement
or sprint-state method is patched.


0.2.3 adds fingerprint-checked `bfk` EntityRenderer FOV return hooks and wraps
Minecraft's wheel read. Projection changes never write game FOV settings or
player physics. Monotonic 250 ms interpolation composes after optional FOV
normalization; consumed zoom wheel input never reaches vanilla hotbar selection.
Sprint keeps a separate latch from the menu-suspended held-key output.
