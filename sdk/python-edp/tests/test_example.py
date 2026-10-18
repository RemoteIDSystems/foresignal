"""The example provider (``examples/river_gauges_provider.py``) builds a valid manifest and serves and
pushes valid batches."""

from __future__ import annotations

import importlib.util
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from types import ModuleType
from typing import Any

import pytest

from foresignal_edp import PushClient, validate_batch, validate_manifest

from .conftest import LIBRARY_ROOT, FakeTransport, ok_response

EXAMPLES = LIBRARY_ROOT / "examples"
NOW = datetime(2026, 10, 17, 12, 0, tzinfo=timezone.utc)


@pytest.fixture(scope="module")
def example() -> ModuleType:
    spec = importlib.util.spec_from_file_location("river_gauges_provider", EXAMPLES / "river_gauges_provider.py")
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    sys.modules["river_gauges_provider"] = module
    spec.loader.exec_module(module)
    return module


def test_manifest_is_valid_with_and_without_an_api_key(example: ModuleType) -> None:
    open_manifest = example.build_manifest("https://hydro.example.org")
    assert validate_manifest(open_manifest).valid
    assert open_manifest["transport"]["pull"] == {"itemsUrl": "https://hydro.example.org/edp/items",
                                                  "intervalSeconds": 300, "auth": "none"}
    keyed = example.build_manifest(api_key_required=True)
    assert keyed["transport"]["pull"]["auth"] == "header" and keyed["transport"]["pull"]["header"] == "X-Api-Key"
    assert validate_manifest(keyed).valid


def test_rows_become_observations(example: ModuleType) -> None:
    items = example.read_items(EXAMPLES / "river_levels.csv", NOW)
    assert [i["id"] for i in items] == ["kaub:2026-10-15", "kaub:2026-10-16", "maxau:2026-10-15", "maxau:2026-10-16"]
    assert items[0]["knownAt"] == "2026-10-15T06:00:00Z" and "knownAt" not in items[2]
    assert items[3]["value"] is None


def test_served_page_is_a_valid_batch_with_status(example: ModuleType) -> None:
    handler = example.make_handler(EXAMPLES / "river_levels.csv", api_key="k")
    assert handler.handle("GET", "/edp/items").status == 401
    response = handler.handle("GET", "/edp/items", headers={"X-Api-Key": "k"})
    body = response.json()
    assert body["items"][0] == {"stream": "levels", "type": "status", "health": "ok"}
    assert len(body["items"]) == 5 and body["nextCursor"] is None
    manifest = handler.handle("GET", "/.well-known/foresignal-edp.json", headers={}).json()
    assert validate_batch(body, manifest).ok


def test_missing_csv_reports_blocked(example: ModuleType, tmp_path: Path) -> None:
    handler = example.make_handler(tmp_path / "missing.csv")
    body = handler.handle("GET", "/edp/items").json()
    assert body["items"] == [{"stream": "levels", "type": "status", "health": "blocked",
                              "message": "missing.csv is missing"}]


def test_push_command(example: ModuleType, monkeypatch: pytest.MonkeyPatch, capsys: pytest.CaptureFixture[str]) -> None:
    transport = FakeTransport([ok_response(4)])

    def client(base_url: str, source_id: str, key: str, **kwargs: Any) -> PushClient:
        return PushClient(base_url, source_id, key, transport=transport, sleep=lambda s: None)

    monkeypatch.setattr(example, "PushClient", client)
    assert example.main(["x", "push", str(EXAMPLES / "river_levels.csv"), "https://fs.example", "eik_k"]) == 0
    sent = json.loads(transport.requests[0].body)
    assert transport.requests[0].url == "https://fs.example/v1/external/sources/ext-river-gauges/items"
    assert len(sent["items"]) == 4
    assert "accepted 4 of 4" in capsys.readouterr().out


def test_usage(example: ModuleType) -> None:
    assert example.main(["x"]) == 64
