# Generated item gap correction

Design reference: MehVahdJukaar's Model Fix, inspected at commit
bcf9b71e4f619d53622a55acaeaa77d3310c1331:
https://github.com/MehVahdJukaar/modelfix-multi

The reference identifies side-face recess, expansion, contiguous edge spans and
UV shrink as relevant to gaps. Blossom implements those ideas independently for
Minecraft 1.8.9's generated layers; no upstream implementation is vendored.

The previous per-row front geometry was removed. A single front/back surface uses
texture alpha for holes; exposed side runs merge only across adjacent edge pixels.
Side walls move inside the silhouette by 0.007 model units and overlap by 0.008,
capped to a fraction of one pixel for high-resolution textures. Generated UVs
compensate for 1.8.9's 0.01-pixel atlas inset; atlas state and block models are not
modified. Animation edge coverage is unioned across frames, as in vanilla.

Texture area and emitted-part limits preserve the vanilla fallback for pathological
assets. Tests cover recess in each direction, overlap through the front/back planes,
disconnected edges, atlas interpolation, animation and complexity bounds. Actual
appearance with resource-pack textures and mipmaps still needs in-game validation.
