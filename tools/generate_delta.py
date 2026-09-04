#!/usr/bin/env python3
"""Reproducibly build the distributable sparse patch from the two authority ROMs."""
import gzip
import struct
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / "Wayne's World (USA).md").read_bytes()
target = next(root.glob("Waynes_World_FR_complete_183_bulles*.bin")).read_bytes()
runs = []
i = 0
while i < len(target):
    if i < len(source) and source[i] == target[i]:
        i += 1
        continue
    start = i
    while i < len(target) and not (i < len(source) and source[i] == target[i]):
        i += 1
    runs.append((start, target[start:i]))
raw = b"WWD1" + struct.pack(">II", len(target), len(runs))
raw += b"".join(struct.pack(">II", offset, len(data)) + data for offset, data in runs)
output = root / "src/main/resources/patch/french-lg30.dlt.gz"
output.parent.mkdir(parents=True, exist_ok=True)
output.write_bytes(gzip.compress(raw, compresslevel=9, mtime=0))
print(f"Patch: {len(runs)} blocs, {sum(map(lambda item: len(item[1]), runs))} octets modifiés, {output.stat().st_size} octets compressés")
