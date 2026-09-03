#!/usr/bin/env python3
"""Generate the deterministic WWD1 sparse translation delta."""
import base64, gzip, struct, sys
from pathlib import Path

def make(source: bytes, target: bytes):
    runs=[]; i=0
    while i < len(target):
        if i < len(source) and source[i] == target[i]: i += 1; continue
        start=i; i += 1
        while i < len(target) and not (i < len(source) and source[i] == target[i]): i += 1
        runs.append((start, target[start:i]))
    raw=b'WWD1'+struct.pack('>II',len(target),len(runs))
    raw+=b''.join(struct.pack('>II',o,len(d))+d for o,d in runs)
    return runs,raw,gzip.compress(raw,compresslevel=9,mtime=0)

if __name__ == '__main__':
    root=Path(__file__).resolve().parents[1]
    usa=root/"Wayne's World (USA).md"
    fr=next(root.glob('Waynes_World_FR_complete_183_bulles*.bin'))
    out=root/'src/main/resources/patch/french-lg30.dlt.b64'; out.parent.mkdir(parents=True,exist_ok=True)
    runs,raw,gz=make(usa.read_bytes(),fr.read_bytes())
    encoded=base64.b64encode(gz)+b'\n'; out.write_bytes(encoded)
    print(f'blocks={len(runs)} different={sum(len(d) for _,d in runs)} raw={len(raw)} gzip={len(gz)} base64={len(encoded)-1}')
