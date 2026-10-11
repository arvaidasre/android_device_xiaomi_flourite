# SPDX-License-Identifier: Apache-2.0
"""Add only pinned CN hardware files to the Global extraction source."""

import hashlib
import json
from pathlib import Path

from extract_utils.main import ExtractUtils
from extract_utils.source import DiskSource, Source


class VariantSource(Source):
    def __init__(self, base, china_dir, hashes):
        self.base = base
        self.china = DiskSource(str(china_dir))
        self.hashes = hashes
        # Validate everything before extract-utils cleans the destination.
        for name, expected in hashes.items():
            blob = Path(china_dir) / name
            if not blob.is_file():
                raise ValueError(f'Missing CN stock file: {blob}')
            if hashlib.sha256(blob.read_bytes()).hexdigest() != expected:
                raise ValueError(f'CN stock hash mismatch: {blob}')

    def _copy_file_path(self, file_path, target_file_path):
        source = self.china if file_path in self.hashes else self.base
        return source._copy_file_path(file_path, target_file_path)

    def _copy_firmware(self, file, target_file_path):
        # Never replace Global modem, bootloader or trusted firmware with CN.
        return self.base._copy_firmware(file, target_file_path)

    def _list_sub_path_file_rel_paths(self, sub_path):
        paths = set(self.base._list_sub_path_file_rel_paths(sub_path))
        prefix = sub_path.rstrip('/') + '/'
        paths.update(name[len(prefix):] for name in self.hashes if name.startswith(prefix))
        return sorted(paths)


class FlouriteExtractUtils(ExtractUtils):
    china_source = None

    def process_modules(self, source):
        if self.china_source is not None:
            manifest = json.loads(Path(__file__).with_name('firmware-sources.json').read_text())
            source = VariantSource(source, self.china_source, manifest['china_files'])
        copied = super().process_modules(source)
        if not copied:
            raise RuntimeError('Incomplete flourite extraction; vendor was not regenerated')
        return copied
