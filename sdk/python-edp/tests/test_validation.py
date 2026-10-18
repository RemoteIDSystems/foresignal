"""Item and batch validation beyond the conformance fixtures: every rejection code and batch-level refusal."""

from __future__ import annotations

from datetime import datetime, timezone
from typing import Any

import pytest

from foresignal_edp import ManifestInvalidError, observation, validate_batch, validate_items
from foresignal_edp.validation import forecast_problem, rejected_reasons, validate_snapshot_header

from .conftest import minimal_manifest

NOW = datetime(2026, 10, 11, 18, 0, tzinfo=timezone.utc)


def obs(**overrides: Any) -> dict[str, Any]:
    item = {"stream": "s", "type": "observation", "id": "a", "observedAt": "2026-10-11T17:00:00Z",
            "seriesKey": "k", "t": "2026-10-10", "value": 1}
    item.update(overrides)
    return {k: v for k, v in item.items() if v is not ...}


def reasons(items: list[Any], manifest: dict[str, Any] | None = None) -> list[str]:
    return rejected_reasons(validate_items(items, manifest or minimal_manifest(), received_at=NOW))


def test_non_object_and_unknown_type() -> None:
    assert reasons(["text", obs(type="widget")]) == ["SCHEMA_INVALID", "SCHEMA_INVALID"]


def test_item_type_mismatch() -> None:
    item = {"stream": "s", "type": "event", "id": "e", "observedAt": "2026-10-11T17:00:00Z",
            "eventTime": "2026-10-11T17:00:00Z"}
    assert reasons([item]) == ["ITEM_TYPE_MISMATCH"]


def test_status_without_stream_is_source_level_and_with_unknown_stream_is_refused() -> None:
    check = validate_items([{"type": "status", "health": "ok"}, {"type": "status", "health": "ok", "stream": "x"}],
                           minimal_manifest(), received_at=NOW)
    assert len(check.valid) == 1 and check.valid[0].stream is None
    assert rejected_reasons(check) == ["UNKNOWN_STREAM"]


def test_limits() -> None:
    long_string = obs(properties={"note": "x" * 2049})
    long_key = obs(properties={"k" * 2049: 1})
    manifest = minimal_manifest(itemType="document")
    long_text = {"stream": "s", "type": "document", "id": "d", "observedAt": "2026-10-11T17:00:00Z",
                 "documentType": "note", "text": "x" * 20001}
    many_media = {"stream": "s", "type": "document", "id": "m", "observedAt": "2026-10-11T17:00:00Z",
                  "documentType": "note", "text": "ok", "media": [{"url": f"https://e.org/{i}"} for i in range(21)]}
    text_at_limit = dict(long_text, id="ok", text="x" * 20000)
    assert reasons([long_string, long_key]) == ["LIMIT_EXCEEDED", "LIMIT_EXCEEDED"]
    # A text over 20,000 characters breaks the schema's maxLength first.
    assert reasons([long_text, many_media, text_at_limit], manifest) == ["SCHEMA_INVALID", "LIMIT_EXCEEDED"]


def test_times() -> None:
    assert reasons([obs(observedAt="2026-10-11 17:00")]) == ["SCHEMA_INVALID"]
    assert reasons([obs(knownAt="yesterday")]) == ["SCHEMA_INVALID"]
    assert reasons([obs(observedAt="2026-10-11T18:05:00Z")]) == []
    assert reasons([obs(observedAt="2026-10-11T18:05:01Z")]) == ["OBSERVED_AT_IN_FUTURE"]
    assert reasons([obs(observedAt="2026-10-11T20:00:00+02:00")]) == []  # 18:00 UTC
    assert reasons([obs(observedAt=...)]) == ["MISSING_OBSERVED_AT"]
    received = minimal_manifest(fields={"observedAtMode": "received"})
    assert reasons([obs(observedAt=...)], received) == []


def test_granularity_must_not_mix_within_a_series() -> None:
    assert reasons([obs(id="a", t="2026-10-10"), obs(id="b", t="2026-10"), obs(id="c", seriesKey="other",
                                                                                   t="2026")]) == ["GRANULARITY_MIXED"]


def test_periods() -> None:
    assert reasons([obs(t="2025-W53")]) == ["PERIOD_INVALID"]  # 2025 has 52 ISO weeks
    assert reasons([obs(t="2026-W53")]) == []  # 2026 starts on a Thursday: 53 weeks
    assert reasons([obs(t="2024-02-29")]) == []
    assert reasons([obs(t="2023-02-29")]) == ["PERIOD_INVALID"]
    assert reasons([obs(t="2026-Q5")]) == ["SCHEMA_INVALID"]


def test_payload_and_geometry() -> None:
    manifest = minimal_manifest(mode="layer", itemType="feature", geometry="LineString",
                                payloadSchema={"type": "object", "required": ["name"],
                                               "properties": {"name": {"type": "string"}}})
    base = {"stream": "s", "type": "feature", "id": "f", "observedAt": "2026-10-11T17:00:00Z",
            "properties": {"name": "a"}}
    line = {"type": "LineString", "coordinates": [[0, 0], [1, 1]]}
    check = validate_items([
        dict(base, geometry=line),
        dict(base, properties={}),
        dict(base, geometry={"type": "LineString", "coordinates": [[0, 0]]}),
        dict(base, geometry={"type": "Point", "coordinates": [0, 0]}),
        dict(base),
    ], manifest, received_at=NOW)
    assert rejected_reasons(check) == ["PAYLOAD_INVALID", "GEOMETRY_INVALID", "GEOMETRY_INVALID"]
    assert check.rejected[0].schema_errors[0].path == "/properties"
    assert [v.index for v in check.valid] == [0, 4]


def test_forecast_problem() -> None:
    assert forecast_problem({"questionRef": " "}) == "a forecast needs a question or a questionRef"
    assert forecast_problem({"questionRef": "q"}) == "a forecast needs a probability or quantiles"
    assert forecast_problem({"questionRef": "q", "probability": 2}) == "probability must be between 0 and 1"
    assert forecast_problem({"questionRef": "q", "quantiles": {"p05": 1}}) == "quantiles need p05, p25, p50, p75 and p95"
    assert "p10 < p05" in (forecast_problem({"questionRef": "q", "quantiles": {
        "p05": 2, "p10": 1, "p25": 3, "p50": 4, "p75": 5, "p95": 6}}) or "")
    assert forecast_problem({"question": {"externalId": "a", "title": "b"}, "probability": 0.5}) is None


def test_invalid_manifest_raises() -> None:
    manifest = minimal_manifest()
    manifest["transport"] = {}
    with pytest.raises(ManifestInvalidError):
        validate_items([], manifest)


def batch(**overrides: Any) -> dict[str, Any]:
    out = {"protocol": "foresignal-edp/1.0", "sentAt": "2026-10-11T18:00:00Z", "items": [obs()]}
    out.update(overrides)
    return out


@pytest.mark.parametrize(
    "envelope, code",
    [
        (batch(protocol="foresignal-edp/1.1"), "PROTOCOL_UNSUPPORTED"),
        (batch(protocol="nope"), "VALIDATION_FAILED"),
        (batch(sourceId="someone-else"), "SOURCE_MISMATCH"),
        (batch(items={"a": 1}), "VALIDATION_FAILED"),
        (batch(items=[obs(id=str(i)) for i in range(10_001)]), "VALIDATION_FAILED"),
        (batch(snapshot={"id": "x", "stream": "s", "page": 0, "final": True}), "VALIDATION_FAILED"),
        (batch(items=[obs(properties={"blob": "x" * 2000, "i": i}) for i in range(2700)]), "PAYLOAD_TOO_LARGE"),
        ([1], "VALIDATION_FAILED"),
    ],
)
def test_batch_level_refusals(envelope: Any, code: str) -> None:
    check = validate_batch(envelope, minimal_manifest(), received_at=NOW)
    assert check.code == code
    assert check.errors
    assert not check.ok


def test_batch_source_id_may_be_the_provider_or_registered_id() -> None:
    for source_id in ("test-source", "ext-test-source"):
        assert validate_batch(batch(sourceId=source_id), minimal_manifest(), received_at=NOW).ok
    assert validate_batch(batch(sourceId="custom"), minimal_manifest(), received_at=NOW, registered_id="custom").ok


def test_batch_with_snapshot_and_rejections() -> None:
    envelope = batch(snapshot={"id": "x", "stream": "s", "scope": None, "page": 1, "final": True},
                     items=[obs(), obs(id="b", t="bad")])
    check = validate_batch(envelope, minimal_manifest(), received_at=NOW)
    assert check.code is None and check.items is not None
    assert rejected_reasons(check.items) == ["SCHEMA_INVALID"]
    assert not check.ok


def test_snapshot_header() -> None:
    assert validate_snapshot_header(None) is None
    assert validate_snapshot_header("x") == "snapshot must be an object"
    assert validate_snapshot_header({"id": "x" * 129, "stream": "s", "page": 1, "final": True}) is not None
    assert validate_snapshot_header({"id": "x", "stream": "s", "page": True, "final": True}) is not None
    assert validate_snapshot_header({"id": "x", "stream": "s", "page": 2, "final": False}) is None


def test_builder_output_round_trips() -> None:
    assert reasons([observation("s", series_key="k", t="2026-10", value=3, observed_at=NOW)]) == []
