# Unreleased changes after 0.2.9.2

- Linux native ZIP includes only `start-blossom.sh`; Windows native ZIP includes
  only `Start-Blossom.bat`. The separately labelled universal legacy ZIP remains
  cross-platform.
- Native and legacy launchers have a Changelog tab after Logs. Native UI selects
  the newest bundled release notes; legacy packaging includes the current release.
- Add opt-in Now Playing HUD with OS media-session detection, album art, track,
  artist and progress, rounded panel and translucent background. See MEDIA.md.
- Item Model Fix replaces generated-layer front/back geometry as well as sides,
  uses UVs inset within the relevant texels, preserves transparent holes and unions
  animation frames. Front faces merge contiguous row runs. Complexity is bounded;
  pathological silhouettes return to vanilla rather than allocating unbounded
  geometry. Custom hand-authored 3D models are unchanged. Toggle reloads models.

Headless item geometry tests cover holes, empty textures, animated silhouettes,
complexity fallback and injected hooks. Actual in-game gap elimination and visual
appearance still need graphical acceptance, including resource-pack mipmaps.
