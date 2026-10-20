#!/bin/bash
# Parsisiunčia GCam port'ą į prebuilt/app/GoogleCamera/GoogleCamera.apk.
# APK ne saugomas gite (per didelis), bet fiksuojamas SHA-256.
set -euo pipefail
cd "$(dirname "$0")/prebuilt/app/GoogleCamera"
URL="https://1-dontsharethislink.celsoazevedo.com/file/filesc/MGC_9.7.047_V33_snap.apk"
SHA256="$(cat GoogleCamera.apk.sha256 2>/dev/null || true)"
if [ -f GoogleCamera.apk ] && [ -n "$SHA256" ] && echo "$SHA256  GoogleCamera.apk" | sha256sum -c --status -; then
    echo "GoogleCamera.apk jau yra ir sutampa su SHA-256"
    exit 0
fi
echo "==> siunčiu $URL"
curl -L --retry 3 -o GoogleCamera.apk "$URL"
echo "==> sha256: $(sha256sum GoogleCamera.apk | cut -d' ' -f1)"
