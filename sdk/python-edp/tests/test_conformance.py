"""The 024 EDP 1.0 conformance fixtures (``src/test/resources/edp/conformance``, by relative path).

- every ``manifests/valid`` manifest validates;
- every ``manifests/invalid`` manifest is refused for the reason its name gives;
- every item of every ``batches/valid`` batch (JSON and NDJSON) is accepted;
- every ``batches/invalid`` batch has exactly one item, rejected with the reason in ``x-expect``.
"""

from __future__ import annotations

from pathlib import Path

import pytest

from foresignal_edp import decode_ndjson, validate_batch, validate_items, validate_manifest

from .conftest import CONFORMANCE_DIR, CONFORMANCE_RECEIVED_AT, load_json


def _files(sub: str, pattern: str = "*.json") -> list[Path]:
    directory = CONFORMANCE_DIR / sub
    return sorted(directory.glob(pattern)) if directory.is_dir() else []


INVALID_MANIFEST_EXPECTATIONS = {
    "protocol-too-new.json": ("/protocol", "newer"),
    "empty-transport.json": ("/transport", "delivery mechanism"),
    "duplicate-stream-ids.json": ("/streams/1/id", "duplicate stream id"),
    "layer-observation.json": ("/streams/0/itemType", "layer stream carries feature or event"),
    "remote-ref.json": ("/streams/0/payloadSchema/$ref", "only local references"),
    "long-pattern.json": ("/streams/0/payloadSchema/properties/code/pattern", "maximum is 256"),
}


def test_fixture_directory_has_every_expected_file(conformance_dir: Path) -> None:
    assert {p.name for p in _files("manifests/invalid")} == set(INVALID_MANIFEST_EXPECTATIONS)
    assert {p.name for p in _files("manifests/valid")} == {"hormuz-example.json", "pull-atomic.json",
                                                         "push-layer.json"}
    valid_types = {p.stem for p in _files("batches/valid")}
    assert {"feature", "event", "observation", "document", "forecast", "delete", "status"} <= valid_types
    assert (conformance_dir / "batches" / "valid" / "event-and-delete.ndjson").is_file()
    assert len(_files("batches/invalid")) == 9


@pytest.mark.parametrize("path", _files("manifests/valid"), ids=lambda p: p.name)
def test_valid_manifests_validate(path: Path) -> None:
    check = validate_manifest(load_json(path))
    assert check.errors == []
    assert check.valid
    assert not check.protocol_unsupported
    assert check.streams


@pytest.mark.parametrize("path", _files("manifests/invalid"), ids=lambda p: p.name)
def test_invalid_manifests_are_refused_for_their_reason(path: Path) -> None:
    check = validate_manifest(load_json(path))
    assert not check.valid
    expected_path, expected_text = INVALID_MANIFEST_EXPECTATIONS[path.name]
    assert any(e.path == expected_path and expected_text in e.message for e in check.errors), check.errors
    assert check.protocol_unsupported == (path.name == "protocol-too-new.json")
    assert check.streams == {}


def test_protocol_too_new_is_accepted_when_the_validator_knows_the_minor() -> None:
    manifest = load_json(CONFORMANCE_DIR / "manifests" / "invalid" / "protocol-too-new.json")
    assert validate_manifest(manifest, max_minor=9).valid


@pytest.mark.parametrize("path", _files("batches/valid"), ids=lambda p: p.name)
def test_valid_batches_accept_every_item(path: Path) -> None:
    batch = load_json(path)
    manifest = load_json(CONFORMANCE_DIR / "manifests" / "valid" / batch["x-manifest"])
    check = validate_batch(batch, manifest, received_at=CONFORMANCE_RECEIVED_AT)
    assert check.code is None, check.errors
    assert check.items is not None
    assert check.items.rejected == []
    assert len(check.items.valid) == len(batch["items"])
    assert check.ok


def test_valid_ndjson_batch_accepts_every_item() -> None:
    path = CONFORMANCE_DIR / "batches" / "valid" / "event-and-delete.ndjson"
    if not path.is_file():
        pytest.skip("conformance fixtures not available")
    batch = decode_ndjson(path.read_bytes())
    assert batch["sourceId"] == "hormuz-transits"
    assert [i["type"] for i in batch["items"]] == ["event", "delete"]
    manifest = load_json(CONFORMANCE_DIR / "manifests" / "valid" / "hormuz-example.json")
    check = validate_batch(batch, manifest, received_at=CONFORMANCE_RECEIVED_AT)
    assert check.ok, check.items.rejected if check.items else check.errors


@pytest.mark.parametrize("path", _files("batches/invalid"), ids=lambda p: p.name)
def test_invalid_batches_reject_their_one_item_with_the_expected_reason(path: Path) -> None:
    batch = load_json(path)
    manifest = load_json(CONFORMANCE_DIR / "manifests" / "valid" / batch["x-manifest"])
    assert len(batch["items"]) == 1
    check = validate_items(batch["items"], manifest, received_at=CONFORMANCE_RECEIVED_AT)
    assert check.valid == []
    assert len(check.rejected) == 1
    rejection = check.rejected[0]
    assert rejection.reason == batch["x-expect"], rejection
    assert rejection.index == 0
    assert rejection.schema_errors
