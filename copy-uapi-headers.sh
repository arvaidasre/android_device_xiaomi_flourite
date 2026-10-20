#!/bin/bash
# Kopijuoja QCOM modulių UAPI header'ius į generated_kernel_includes išvestį.
# Šaltinis: kernel/xiaomi/flourite-source-modules (atskiras modulių repo).
set -e
dest="${1:?destination usr/include dir}"
src_root="$(cd "$(dirname "$0")/../../.." && pwd)"
modules="$src_root/kernel/xiaomi/flourite-source-modules"
[ -d "$modules" ] || exit 0
mkdir -p "$dest"
find "$modules" -type d -path '*include/uapi' -print0 | while IFS= read -r -d '' d; do
  cp -a "$d"/. "$dest"/
done
