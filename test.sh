#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
before=$(sha1sum "Wayne's World (USA).md")
python3 tools/generate_delta.py
./build.sh
mkdir -p build/test-classes
find src/test/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes
java -ea -cp build/classes:build/test-classes fr.lg30.waynesworld.AllTests
test "$before" = "$(sha1sum "Wayne's World (USA).md")"
