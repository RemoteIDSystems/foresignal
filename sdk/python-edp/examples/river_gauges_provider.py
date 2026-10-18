#!/usr/bin/env python3
"""Example EDP provider built on ``foresignal_edp``: river levels from a CSV, by push or pull.

The CSV has the columns ``gauge,period,level_m[,known_at]`` (``river_levels.csv``); each row becomes
an ``observation`` of the atomic stream ``levels`` (kind ``river-level``), series ``{gauge}``.

push:   river_gauges_provider.py push CSV BASE_URL INGEST_KEY
        Pushes the rows to ``ext-river-gauges`` (register and approve the manifest first; print it
        with ``manifest``). Rejected rows are listed; a missing CSV is reported as a ``blocked``
        status instead of sending nothing.
serve:  river_gauges_provider.py serve CSV PORT [PUBLIC_URL]
        Serves ``GET /.well-known/foresignal-edp.json`` and ``GET /edp/items?cursor=&limit=`` for
        Foresignal to pull. Set ``RIVER_GAUGES_API_KEY`` to require the ``X-Api-Key`` header (the
        manifest then declares ``auth: header``; the operator enters the key in Foresignal).
manifest: river_gauges_provider.py manifest [PUBLIC_URL]
        Prints the validated manifest.
"""

from __future__ import annotations

import csv
import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from foresignal_edp import (
    ManifestBuilder,
    PullHandler,
    PushClient,
    SequenceSource,
    StatusBoard,
    StreamBuilder,
    header_auth,
    observation,
    registered_source_id,
    validate_items,
)
from foresignal_edp.pull import serve as serve_wsgi

SOURCE_ID = "river-gauges"
STREAM = "levels"
API_KEY_HEADER = "X-Api-Key"


def build_manifest(public_url: str = "http://localhost:8099", *, api_key_required: bool = False) -> dict[str, Any]:
    """The provider's manifest: push and pull, one atomic observation stream suggesting a series binding."""
    return (
        ManifestBuilder(SOURCE_ID, "River gauges")
        .description("Daily river levels from a CSV export")
        .version("1.0.0")
        .publisher("Example Hydro")
        .license(redistribution="local-only")
        .access("other", "low")
        .originator("example-hydro", "sensor-network")
        .push()
        .pull(f"{public_url.rstrip('/')}/edp/items", interval_seconds=300,
              auth="header" if api_key_required else "none", header=API_KEY_HEADER if api_key_required else None)
        .stream(StreamBuilder(STREAM, mode="atomic", item_type="observation", kind="river-level")
                .title("Daily river level by gauge").unit("m").geometry("none").retention_class("series")
                .payload_schema({"type": "object", "properties": {"gauge": {"type": "string"}}})
                .suggest_binding("series"))
        .build()
    )


def read_items(path: str | Path, observed_at: datetime | None = None) -> list[dict[str, Any]]:
    """The CSV rows as observations; an empty ``level_m`` is a null value (the gauge reported nothing)."""
    stamp = observed_at or datetime.now(timezone.utc)
    out = []
    with open(path, newline="", encoding="utf-8") as f:
        for row in csv.DictReader(f):
            out.append(observation(
                STREAM, series_key=row["gauge"], t=row["period"],
                value=float(row["level_m"]) if row.get("level_m") else None,
                observed_at=stamp, known_at=row.get("known_at") or None, properties={"gauge": row["gauge"]},
            ))
    return out


def make_handler(path: str | Path, public_url: str = "http://localhost:8099", api_key: str | None = None,
                 status: StatusBoard | None = None) -> PullHandler:
    """A pull handler over the CSV; a missing file is reported as a ``blocked`` status."""
    board = status or StatusBoard()

    def load() -> list[dict[str, Any]]:
        try:
            rows = read_items(path)
        except FileNotFoundError:
            board.report("blocked", f"{Path(path).name} is missing", stream=STREAM)
            return []
        board.ok(stream=STREAM)
        return rows

    return PullHandler(
        SequenceSource(load),
        source_id=SOURCE_ID,
        manifest=build_manifest(public_url, api_key_required=api_key is not None),
        authorize=header_auth(API_KEY_HEADER, api_key) if api_key else None,
        status=board,
    )


def push(path: str, base_url: str, ingest_key: str) -> int:
    """Validates the rows locally, pushes them, and prints the outcome. Returns a process exit code."""
    client = PushClient(base_url, registered_source_id(SOURCE_ID), ingest_key)
    try:
        rows = read_items(path)
    except FileNotFoundError:
        client.report_status("blocked", f"{Path(path).name} is missing", stream=STREAM)
        print("CSV missing; reported status blocked", file=sys.stderr)
        return 1
    local = validate_items(rows, build_manifest())
    for rejection in local.rejected:
        print(f"row {rejection.index} invalid locally: {rejection.reason}", file=sys.stderr)
    result = client.push([v.item for v in local.valid])
    print(f"accepted {result.accepted} of {len(rows)} in {result.batches} batch(es)")
    for rejection in result.rejected:
        print(f"rejected item {rejection.index}: {rejection.reason}", file=sys.stderr)
    return 0 if result.ok and local.ok else 2


def main(argv: list[str]) -> int:
    """Command-line entry point."""
    if len(argv) >= 5 and argv[1] == "push":
        return push(argv[2], argv[3], argv[4])
    if len(argv) >= 4 and argv[1] == "serve":
        public_url = argv[4] if len(argv) > 4 else f"http://localhost:{argv[3]}"
        serve_wsgi(make_handler(argv[2], public_url, os.environ.get("RIVER_GAUGES_API_KEY")), port=int(argv[3]))
        return 0
    if len(argv) >= 2 and argv[1] == "manifest":
        print(json.dumps(build_manifest(argv[2] if len(argv) > 2 else "http://localhost:8099"), indent=2))
        return 0
    print(__doc__, file=sys.stderr)
    return 64


if __name__ == "__main__":
    sys.exit(main(sys.argv))
