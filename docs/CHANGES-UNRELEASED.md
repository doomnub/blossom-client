# Unreleased changes after 0.2.9.3

- Rename Now Playing to Media Player, preserving saved settings under the media ID.
- Media Player obeys the shared HUD background toggle. With it off, neither the
  panel nor album-art placeholder is drawn. Add the shared toggle to HUD Visuals.
- Replace stepped HUD text-size buttons with drag sliders, including All HUD
  visuals. Range remains 0.5x–3x and global changes propagate to every HUD module.
- Replace item-gap geometry with recessed, overlapping contiguous side spans and
  a single alpha-textured front/back surface. Correct 1.8.9 atlas UV inset only
  for generated item faces; retain bounded complexity and transparent gaps.
- Set Linux GTK program identity before creating windows so shell launches match
  the installed desktop entry and icon.
- Preserve the confirmed-stable Linux renderer baseline. Reintroduce optimizations
  individually only after comparative gameplay, memory and frame-time validation.

Item rendering and KDE Wayland icon appearance require on-device visual checks.
