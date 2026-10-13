# SPDX-License-Identifier: Apache-2.0
"""Align stock camera perflocks with Flourite's clusters and bus limits."""

import hashlib
import json
import re
from pathlib import Path


TARGET = 'vendor/etc/camera/cameraopt_perf.json'
STOCK_SHA256 = '00d65ee84fe0c566a6e5fa5073b3018487a7769b4e507b02a7d144ec8d435177'
PATCHED_SHA256 = 'b7ad70a3a7491f94ea04dc7ac015f36319236f46ac14da3328cb9717eb835225'

# Volcano has three CPU clusters and no LLCC boost_freq node.
UNSUPPORTED_OPCODES = frozenset({
    0x40800300,  # CPU minimum frequency, fourth cluster
    0x40804300,  # CPU maximum frequency, fourth cluster
    0x43C1C300,  # Scheduler frequency cap, fourth cluster
    0x41450300,  # Governor target load, fourth cluster
    0x40CE0300,  # Scheduler migration threshold, fourth cluster
    0x41010300,  # Core control partial halt, fourth cluster
    0x41014300,  # Core control offline timeout, fourth cluster
    0x4144C300,  # RTG boost frequency, fourth cluster
    0x434A0000,  # LLCC boost frequency
})

# Measured bus_dcvs hw_max_freq values, in kHz. Keep votes within hardware limits.
BUS_BOOST_LIMITS = {
    0x43498000: (4224000, 2092000),  # DDR
    0x4349C000: (2035200, 1804800),  # L3
}


def patched_camera_performance(data):
    digest = hashlib.sha256(data).hexdigest()
    if digest == PATCHED_SHA256:
        return data
    if digest != STOCK_SHA256:
        raise ValueError('Unknown camera performance config; re-audit before patching')

    lines = []
    for line in data.decode('utf-8').splitlines(keepends=True):
        match = re.match(r'\s*"(0[xX][0-9a-fA-F]+),', line)
        if match and int(match[1], 16) in UNSUPPORTED_OPCODES:
            continue
        if match and int(match[1], 16) in BUS_BOOST_LIMITS:
            old, new = BUS_BOOST_LIMITS[int(match[1], 16)]
            line = line.replace(str(old), str(new))
        lines.append(line)
    result = re.sub(r',(\s*\])', r'\1', ''.join(lines)).encode('utf-8')
    json.loads(result)
    if hashlib.sha256(result).hexdigest() != PATCHED_SHA256:
        raise ValueError('Unexpected camera performance fixup output')
    return result


def fixup_camera_performance(ctx, file, file_path, *args, **kwargs):
    if file.dst != TARGET:
        raise ValueError('Camera performance fixup is restricted to the vendor defaults')
    path = Path(file_path)
    original = path.read_bytes()
    result = patched_camera_performance(original)
    if result != original:
        path.write_bytes(result)
