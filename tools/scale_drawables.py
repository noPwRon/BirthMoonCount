#!/usr/bin/env python3
"""
scale_drawables.py

Batch-scale PNG drawables (keeps alpha) and write to an output folder.

Pixel 7 density reference:
- dpi ≈ 420
- px_per_dp = 420/160 = 2.625

Typical use cases:
1) Multiply pixel dimensions by a scale factor (factor mode)
2) Convert "dp-sized art exported as px" into Pixel 7 pixels (dp_to_px mode)
3) Convert Pixel 7 px back to dp-equivalent px (px_to_dp mode)

Examples:
# Scale everything up by 2.625x (common if your PNGs were authored as "dp pixels")
python scale_drawables.py --in ./app/src/main/res/drawable --out ./scaled --mode factor --factor 2.625

# Convert dp->px using Pixel 7 density (same as factor 2.625)
python scale_drawables.py --in ./app/src/main/res/drawable --out ./scaled --mode dp_to_px

# Convert px->dp-equivalent px (divide by 2.625)
python scale_drawables.py --in ./app/src/main/res/drawable --out ./scaled --mode px_to_dp
"""

from __future__ import annotations

import argparse
import os
from pathlib import Path
from typing import Iterable, Tuple

from PIL import Image


DEFAULT_PX_PER_DP = 2.625  # Pixel 7: 420/160


def iter_pngs(folder: Path) -> Iterable[Path]:
    for p in folder.iterdir():
        if p.is_file() and p.suffix.lower() == ".png":
            yield p


def compute_scale(mode: str, px_per_dp: float, factor: float) -> float:
    if mode == "factor":
        return factor
    if mode == "dp_to_px":
        return px_per_dp
    if mode == "px_to_dp":
        return 1.0 / px_per_dp
    raise ValueError(f"Unknown mode: {mode}")


def scale_image(in_path: Path, out_path: Path, scale: float, resample: int) -> Tuple[int, int, int, int]:
    with Image.open(in_path) as im:
        im = im.convert("RGBA")  # ensure alpha preserved
        w, h = im.size
        new_w = max(1, int(round(w * scale)))
        new_h = max(1, int(round(h * scale)))
        out = im.resize((new_w, new_h), resample=resample)

        out_path.parent.mkdir(parents=True, exist_ok=True)
        out.save(out_path, format="PNG", optimize=True)

        return w, h, new_w, new_h


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--in", dest="in_dir", required=True, help="Input folder containing PNGs (e.g., app/src/main/res/drawable)")
    ap.add_argument("--out", dest="out_dir", required=True, help="Output folder for scaled PNGs")
    ap.add_argument("--mode", choices=["factor", "dp_to_px", "px_to_dp"], default="factor",
                    help="Scaling mode: factor, dp_to_px (Pixel7), px_to_dp (Pixel7)")
    ap.add_argument("--factor", type=float, default=1.0, help="Scale factor used only when mode=factor")
    ap.add_argument("--px-per-dp", type=float, default=DEFAULT_PX_PER_DP, help="Pixels per dp, default is Pixel 7 (2.625)")
    ap.add_argument("--resample", choices=["lanczos", "bicubic", "bilinear", "nearest"], default="lanczos")
    ap.add_argument("--overwrite", action="store_true", help="Allow overwriting files in output folder")
    args = ap.parse_args()

    in_dir = Path(args.in_dir).expanduser().resolve()
    out_dir = Path(args.out_dir).expanduser().resolve()

    if not in_dir.exists() or not in_dir.is_dir():
        raise SystemExit(f"Input dir does not exist or is not a directory: {in_dir}")

    resample_map = {
        "lanczos": Image.Resampling.LANCZOS,
        "bicubic": Image.Resampling.BICUBIC,
        "bilinear": Image.Resampling.BILINEAR,
        "nearest": Image.Resampling.NEAREST,
    }
    resample = resample_map[args.resample]

    scale = compute_scale(args.mode, args.px_per_dp, args.factor)

    print(f"Input:  {in_dir}")
    print(f"Output: {out_dir}")
    print(f"Mode:   {args.mode}")
    print(f"Scale:  {scale:.6f}x")
    print(f"Filter: {args.resample}")

    count = 0
    for src in iter_pngs(in_dir):
        dst = out_dir / src.name
        if dst.exists() and not args.overwrite:
            print(f"SKIP (exists): {dst.name}")
            continue

        w, h, nw, nh = scale_image(src, dst, scale, resample)
        print(f"{src.name}: {w}x{h} -> {nw}x{nh}")
        count += 1

    print(f"Done. Scaled {count} file(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
