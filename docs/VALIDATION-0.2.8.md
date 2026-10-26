# Blossom 0.2.8 implementation notes

## Changes

- Item Model Fix: optional replacement of generated item side geometry. Exposed texel edges are collected across animation frames, UVs sample opaque texel centers, and small overlaps seal adjacent edges. Custom volumetric models are unchanged. Toggling reloads resource models once.
- Zoom/FOV changes invalidate the vanilla visible-chunk list, including the final return to normal FOV, without rebuilding all chunk meshes.
- Static FOV hides condition-specific settings. Coordinates support zero through six decimal places.
- CPS and keystrokes use current attack/use bindings, including keyboard bindings; key repeats do not inflate CPS.
- Resource names use Minecraft's current language and resource-pack translations. Held-item totals sum matching item, metadata and NBT across inventory stacks. Compact ping handles local/server labels; an unmeasured server value displays zero.
- Launcher HTML is formatted as readable nested elements. The skin-arm selector uses the launcher palette, including its menu and focus state.
- Launcher exit is never blocked by game/task state. Process exit releases running state without waiting indefinitely for inherited log pipes.
- Explicit native window icons, early Linux desktop registration and PNG fallback. The game uses a flower with a block badge; the launcher retains the flower.
- Chat implements specification section 18: independent opacities, dimensions/scale, fade duration, timestamp format/color, resource-pack/Unicode font, mentions/sound, phrase filtering, duplicate stacking, local searchable history, message/link copying, URL detection, opt-in disk history/erase, draft preservation during resize/history navigation, and an insert-only shortcut.

Chat history: Ctrl+F in chat or the module settings opens search. Select a message, then Copy message or Copy links. Shift-clicking a visible component copies that component. Ctrl+Space inserts the configured shortcut; Enter remains the user's send action. Disk history defaults off and is limited to 2,000 entries. Font support uses Minecraft's resource-pack font system, not arbitrary TTF loading.

## Research

The generated-item seam is the MC-73186 family of bugs. Primary project explanations:
- https://github.com/PepperCode1/Item-Model-Fix
- https://github.com/MehVahdJukaar/modelfix-multi

The side geometry implementation here is original, based on inspecting vanilla 1.8.9's generator and these descriptions. No third-party implementation was copied.

## Verification scope

Java 8 API-targeted compilation and the TypeScript/Vite production build pass. Full test suites were not run for this request. Rust/Tauri compilation and graphical gameplay acceptance have not been performed locally; compilation alone is not graphical acceptance. In particular, item seams across animated/high-resolution textures, zoom-out with a stationary camera, chat layout at GUI scales, and Linux compositor window icons still need gameplay/desktop acceptance.
