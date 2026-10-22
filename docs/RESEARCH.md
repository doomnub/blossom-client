# Reference audit

Reviewed official references on 2026-10-22. These sources inform feature discovery,
not implementation code. The supplied specification is a product wish list; it is
not evidence that every setting exists in Lunar 1.8.9.

| Source | Scope established | Limits |
| --- | --- | --- |
| https://www.lunarclient.com/features | Modules such as keystrokes/coordinates and multi-platform product | Current product page does not prove every feature on 1.8.9 |
| https://www.lunarclient.com/changelog | Ongoing changes across launcher/game versions | Includes modern-only changes; no blanket 1.8.9 parity claim |
| https://www.lunarclient.com/news/patch-notes-7 | Explicit discussion of 1.7.10/1.8.9 work | Historical snapshot |
| https://www.lunarclient.com/news/patch-notes-18 | 1.7 visuals work and chunk-border module | Per-setting/version verification remains required |
| https://www.lunarclient.com/news/patch-notes-23 | Hurt-camera module and crosshair outline work | Different integrations must not be conflated |
| https://www.lunarclient.com/news/lunar-client-monthly-recap-april-2026 | Contemporary product changes | Modern-version content is not a 1.8.9 requirement |
| https://www.lunarclient.com/news/getting-started-with-lunar-client | User-facing setup/mod configuration workflow | Original Blossom UI implementation |
| https://www.lunarclient.com/news/why-lunar-client-is-the-best-for-playing-hypixel-network | PvP/Hypixel-oriented feature discovery | Marketing claims do not certify Blossom compatibility |
| https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html | Standard premain Java agent and class transformation mechanism | Does not establish any game-specific correctness |

Technical source of truth is the downloaded official 1.8.9 bytecode. No proprietary
Lunar source, assets, cosmetics or services are used. MCP SRG signatures are
consulted rather than copying a third-party client's implementation.
