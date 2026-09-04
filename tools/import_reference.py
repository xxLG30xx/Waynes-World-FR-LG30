#!/usr/bin/env python3
"""Import Image.png byte-for-byte. This preserves Wayne and Garth's original pixels."""
import shutil
from pathlib import Path

root = Path(__file__).resolve().parents[1]
destination = root / "src/main/resources/ui/reference.png"
destination.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(root / "Image.png", destination)
print(f"Visual reference imported: {destination.stat().st_size} bytes")
