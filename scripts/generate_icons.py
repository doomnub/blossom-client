#!/usr/bin/env python3
"""Rasterize the original SVG for platforms requiring PNG/ICO. pip install cairosvg pillow."""
from pathlib import Path
import shutil
import cairosvg
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
source=ROOT/'assets/blossom.svg'
shutil.copyfile(source,ROOT/'launcher-tauri/public/blossom.svg')
icons=ROOT/'launcher-tauri/src-tauri/icons';icons.mkdir(exist_ok=True)
for size,name in [(512,'icon.png'),(32,'32x32.png'),(128,'128x128.png'),(256,'128x128@2x.png')]:
    cairosvg.svg2png(url=str(source),write_to=str(icons/name),output_width=size,output_height=size)
Image.open(icons/'icon.png').save(icons/'icon.ico',sizes=[(16,16),(24,24),(32,32),(48,48),(64,64),(128,128),(256,256)])
shutil.copyfile(icons/'icon.png',ROOT/'assets/blossom.png')

cairosvg.svg2png(url=str(ROOT/'assets/blossom-client.svg'),write_to=str(ROOT/'assets/blossom-client.png'),output_width=512,output_height=512)
