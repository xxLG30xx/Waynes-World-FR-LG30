#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
command -v javac >/dev/null || { echo "Java 17 JDK requis" >&2; exit 1; }
major=$(javac -version 2>&1 | sed -E 's/[^0-9]*([0-9]+).*/\1/')
(( major >= 17 )) || { echo "Java 17 ou supérieur requis" >&2; exit 1; }
rm -rf build WaynesWorldPatcher.jar
mkdir -p build/classes
find src/main/java -type f -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -d build/classes
cp -R src/main/resources/. build/classes/
printf 'Manifest-Version: 1.0\nMain-Class: fr.lg30.waynesworld.WaynesWorldPatcher\nImplementation-Version: 1.0.0\n\n' > build/MANIFEST.MF
jar --create --file WaynesWorldPatcher.jar --manifest build/MANIFEST.MF -C build/classes .
printf 'WaynesWorldPatcher.jar créé (%s octets)\n' "$(wc -c < WaynesWorldPatcher.jar)"
