#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if command -v sha1sum >/dev/null 2>&1; then
    sha1_file() { sha1sum "$1"; }
else
    sha1_file() { shasum -a 1 "$1"; }
fi

source_before=$(sha1_file "Wayne's World (USA).md")
./build.sh
mkdir -p build/test-classes
find src/test/java -type f -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes
java -ea -cp build/classes:build/test-classes fr.lg30.waynesworld.AllTests
test "$source_before" = "$(sha1_file "Wayne's World (USA).md")"
java -Djava.awt.headless=true -jar WaynesWorldPatcher.jar --render-preview
java -Djava.awt.headless=true -cp build/classes:build/test-classes fr.lg30.waynesworld.PreviewVerifier
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('WaynesWorldPatcher.jar') as jar:
    names = jar.namelist()
    assert not any(name.endswith(('.md', '.bin')) for name in names)
    assert 'patch/french-lg30.dlt.gz' in names and 'ui/reference.png' in names
print('PASS aucune ROM complète dans le JAR')
PY
