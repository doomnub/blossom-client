# Blossom development conventions

- Commit and push coherent checkpoints, as requested by the owner.
- Increment release versions by one patch by default: 0.2.1, 0.2.2, 0.2.3.
  Do not increment the minor version unless explicitly requested.
- After 0.2.9, performance-only releases use a fourth component: 0.2.9.1,
  0.2.9.2, and so on. Keep this public version in VERSION, release tags, package
  names and UI. For tools requiring three-component SemVer (Cargo/npm/Tauri),
  use a compatible internal version such as 0.2.9+perf.1; do not insert a fourth
  numeric component into their manifests.
- Verify published GitHub release assets before reporting publication complete.
- Graphical gameplay requires actual acceptance testing; compilation is not proof.
