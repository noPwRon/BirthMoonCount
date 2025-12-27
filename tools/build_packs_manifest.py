#!/usr/bin/env python3
"""
build_packs_manifest.py

Generate app/src/main/assets/packs.json with SHA256 hashes for each quote pack.

Usage:
  python tools/build_packs_manifest.py
  python tools/build_packs_manifest.py --assets-dir app/src/main/assets --output app/src/main/assets/packs.json
"""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from typing import Iterable, List, Set


def sha256_hex(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            h.update(chunk)
    return h.hexdigest()


def iter_json_files(assets_dir: Path, excludes: Set[str]) -> Iterable[Path]:
    for path in sorted(assets_dir.iterdir()):
        if not path.is_file():
            continue
        if path.suffix.lower() != ".json":
            continue
        if path.name in excludes:
            continue
        yield path


def build_manifest(assets_dir: Path, excludes: Set[str]) -> List[dict]:
    packs: List[dict] = []
    for path in iter_json_files(assets_dir, excludes):
        packs.append({
            "fileName": path.name,
            "sha256": sha256_hex(path),
        })
    return packs


def main() -> int:
    repo_root = Path(__file__).resolve().parents[1]
    default_assets_dir = repo_root / "app" / "src" / "main" / "assets"
    default_output = default_assets_dir / "packs.json"

    ap = argparse.ArgumentParser()
    ap.add_argument("--assets-dir", default=str(default_assets_dir), help="Assets directory containing pack json files")
    ap.add_argument("--output", default=str(default_output), help="Output packs.json path")
    ap.add_argument("--exclude", action="append", default=[], help="File name to exclude (repeatable)")
    args = ap.parse_args()

    assets_dir = Path(args.assets_dir).expanduser().resolve()
    output_path = Path(args.output).expanduser().resolve()
    # Exclude the manifest itself to avoid self-referential hashes.
    excludes = {"packs.json"} | set(args.exclude)

    if not assets_dir.exists() or not assets_dir.is_dir():
        raise SystemExit(f"Assets dir does not exist or is not a directory: {assets_dir}")

    packs = build_manifest(assets_dir, excludes)
    if not packs:
        raise SystemExit("No json packs found to include in the manifest.")

    manifest = {"packs": packs}
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", encoding="utf-8", newline="\n") as f:
        json.dump(manifest, f, indent=2, ensure_ascii=True)
        f.write("\n")

    print(f"Wrote {len(packs)} pack(s) to {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
