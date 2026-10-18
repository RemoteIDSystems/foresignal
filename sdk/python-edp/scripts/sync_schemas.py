#!/usr/bin/env python3
"""Copies the exported EDP JSON Schemas into the package.

The schemas are generated from ``docs/OpenAPI.json`` into ``docs/protocol/edp-1.0/`` by
``scripts/export-edp-schemas.py``; this library ships a byte-for-byte copy under
``src/foresignal_edp/schemas/`` so it works when installed on its own. Run this script after the
schemas change; ``tests/test_schemas_sync.py`` fails while the copies differ.

Usage: ``python3 scripts/sync_schemas.py [SOURCE_DIR]`` (default: the repository's
``docs/protocol/edp-1.0``).
"""

from __future__ import annotations

import shutil
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
DEFAULT_SOURCE = HERE.parents[2] / "docs" / "protocol" / "edp-1.0"
TARGET = HERE.parent / "src" / "foresignal_edp" / "schemas"


def main(argv: list[str]) -> int:
    """Copies every ``*.schema.json`` from the source directory into the package."""
    source = Path(argv[1]) if len(argv) > 1 else DEFAULT_SOURCE
    files = sorted(source.glob("*.schema.json"))
    if not files:
        print(f"No *.schema.json files in {source}", file=sys.stderr)
        return 1
    TARGET.mkdir(parents=True, exist_ok=True)
    for path in files:
        shutil.copyfile(path, TARGET / path.name)
        print(f"copied {path.name}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
