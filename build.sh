#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
rm -rf build WaynesWorldPatcher.jar
mkdir -p build/classes
find src/main/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -d build/classes
cp -R src/main/resources/. build/classes/
printf 'Manifest-Version: 1.0\nMain-Class: fr.lg30.waynesworld.WaynesWorldPatcher\n\n' > build/MANIFEST.MF
jar --create --file WaynesWorldPatcher.jar --manifest build/MANIFEST.MF -C build/classes .
echo "Built WaynesWorldPatcher.jar ($(wc -c < WaynesWorldPatcher.jar) bytes)"
