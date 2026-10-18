"""Manifest builder and validator."""

from __future__ import annotations

import copy
from typing import Any

import pytest

from foresignal_edp import (
    ManifestBuilder,
    ManifestInvalidError,
    StreamBuilder,
    bbox_polygon,
    validate_manifest,
)
from foresignal_edp.manifest import as_manifest_check, is_duration, is_http_url, load_manifest

from .conftest import CONFORMANCE_DIR, minimal_manifest


def hormuz_builder() -> ManifestBuilder:
    """Rebuilds the §2 example with the builder."""
    return (
        ManifestBuilder("hormuz-transits", "Strait of Hormuz transits")
        .description("Daily tanker and cargo transits counted from public dashboards")
        .version("0.3.1")
        .publisher("Example OSINT Lab", url="https://example.org", contact="ops@example.org")
        .homepage("https://github.com/example/hormuz-edp")
        .license(id="CC-BY-4.0", url="https://creativecommons.org/licenses/by/4.0/",
                 attribution="Example OSINT Lab", redistribution="allowed")
        .terms("Derived from a public dashboard; upstream terms unclear", None)
        .access("scrape", "high")
        .originator("example-osint-lab", "aggregator", aggregates=[])
        .private(False)
        .push()
        .pull("https://hormuz.example.org/edp/items", interval_seconds=3600, auth="bearer")
        .stream(StreamBuilder("transits", mode="atomic", item_type="observation", kind="transit-count")
                .title("Daily transits by vessel class").unit("vessels/day").incremental()
                .payload_schema({"type": "object", "properties": {"vesselClass": {"type": "string"}}})
                .suggest_binding("series"))
        .stream(StreamBuilder("incidents", mode="layer", item_type="event", kind="maritime-incident")
                .title("Reported incidents").geometry("Point").incremental()
                .payload_schema({"type": "object", "required": ["category"],
                                 "properties": {"category": {"type": "string"},
                                                "vessel": {"type": ["string", "null"]}}})
                .fields(title="category", significant=["category"], volatile=[])
                .retention_class("event")
                .presentation(color="#d9480f", icon="ship", label_field="vessel", min_zoom=3,
                              tile_properties=["category"])
                .coverage(area=bbox_polygon(54.5, 25.0, 58.0, 27.5), family="maritime-incident", p_detect=0.6)
                .suggest_binding("canonical-family", {"family": "maritime-incident"}))
    )


def test_builder_reproduces_the_specification_example() -> None:
    built = hormuz_builder().build()
    assert built["protocol"] == "foresignal-edp/1.0"
    assert built["source"]["licence"]["redistribution"] == "allowed"  # wire key keeps its spelling
    assert built["transport"] == {"push": True, "pull": {"itemsUrl": "https://hormuz.example.org/edp/items",
                                                         "intervalSeconds": 3600, "auth": "bearer"}}
    incidents = built["streams"][1]
    assert incidents["coverage"]["area"]["coordinates"][0][0] == incidents["coverage"]["area"]["coordinates"][0][-1]
    assert incidents["coverage"]["pDetect"] == 0.6
    assert incidents["suggestedBindings"] == [{"target": "canonical-family", "config": {"family": "maritime-incident"}}]
    if CONFORMANCE_DIR.is_dir():
        expected = load_manifest(CONFORMANCE_DIR / "manifests" / "valid" / "hormuz-example.json")
        assert built["source"] == {k: v for k, v in expected["source"].items()}
        assert built["streams"][0] == expected["streams"][0]
        assert built["streams"][1]["presentation"] == expected["streams"][1]["presentation"]


def test_builder_writes_snapshot_complete_and_document_members() -> None:
    manifest = (
        ManifestBuilder("harbor-cams", "Harbor cameras")
        .publisher("Example Port Watch").originator("example-port-watch", "operator").access("official", "low")
        .push()
        .stream(StreamBuilder("cameras", mode="layer", item_type="feature", kind="harbor-camera")
                .geometry("Point").snapshot("region").ttl("PT1H"))
        .stream(StreamBuilder("notes", mode="atomic", item_type="document", kind="harbor-note")
                .document_type("note").complete().fields(observed_at_mode="received"))
        .build()
    )
    cameras, notes = manifest["streams"]
    assert cameras["update"] == "snapshot" and cameras["snapshotScope"] == "region"
    assert notes["complete"] is True
    assert notes["documentType"] == "note"
    assert notes["fields"]["observedAtMode"] == "received"
    check = validate_manifest(manifest)
    assert check.valid
    assert check.streams["cameras"].snapshot
    assert check.streams["notes"].observed_at_received


def test_whole_stream_snapshot_keeps_a_null_scope_and_incremental_drops_it() -> None:
    whole = StreamBuilder("a", mode="layer", item_type="feature", kind="k").snapshot().build()
    assert "snapshotScope" in whole and whole["snapshotScope"] is None
    back = StreamBuilder("a", mode="layer", item_type="feature", kind="k").snapshot("r").incremental().build()
    assert "snapshotScope" not in back


def test_build_validates_and_raises_with_every_problem() -> None:
    builder = ManifestBuilder("Bad Id", "Bad").publisher("X").originator("x", "operator")
    with pytest.raises(ManifestInvalidError) as raised:
        builder.build()
    paths = {e.path for e in raised.value.check.errors}
    assert "/source/id" in paths
    assert "/transport" in paths
    assert "/streams" in paths  # minItems 1
    assert builder.build(validate=False)["source"]["id"] == "Bad Id"


def test_set_adds_forward_compatible_members() -> None:
    manifest = (ManifestBuilder("a", "A").publisher("X").originator("x", "operator").push()
                .stream(StreamBuilder("s", mode="atomic", item_type="observation", kind="k").set("x-future", 1))
                .set("x-top", True).build())
    assert manifest["x-top"] is True
    assert manifest["streams"][0]["x-future"] == 1


def test_registered_id() -> None:
    assert ManifestBuilder("abc", "A").registered_id == "ext-abc"


@pytest.mark.parametrize(
    "mutate, path, text",
    [
        (lambda m: m.__setitem__("protocol", "edp/1.0"), "/protocol", "foresignal-edp/1.N"),
        (lambda m: m["transport"].__setitem__("pull", {"itemsUrl": "ftp://x/items"}), "/transport/pull/itemsUrl",
         "absolute http or https URL"),
        (lambda m: m["transport"].__setitem__("pull", {"itemsUrl": "https://x/items", "intervalSeconds": 30}),
         "/transport/pull/intervalSeconds", "at least 60"),
        (lambda m: m["transport"].__setitem__("pull", {"itemsUrl": "https://x/items", "auth": "header"}),
         "/transport/pull/header", "required when auth is header"),
        (lambda m: m["transport"].__setitem__("search", {"url": "not a url"}), "/transport/search/url",
         "absolute http or https URL"),
        (lambda m: m["source"]["originator"].__setitem__("id", "Not Slug"), "/source/originator/id", "slug"),
        (lambda m: m["streams"][0].__setitem__("kind", "Bad Kind"), "/streams/0/kind", "slug"),
        (lambda m: m["streams"][0].__setitem__("ttl", "one hour"), "/streams/0/ttl", "ISO-8601"),
        (lambda m: m["streams"][0].__setitem__("snapshotScope", "region"), "/streams/0/snapshotScope",
         "only snapshot streams"),
        (lambda m: m["streams"][0].__setitem__("fields", {"title": "a.b"}), "/streams/0/fields/title",
         "top-level payload name"),
        (lambda m: m["streams"][0].__setitem__("fields", {"significant": ["x"], "volatile": ["x"]}),
         "/streams/0/fields", "both significant and volatile"),
        (lambda m: m["streams"][0].__setitem__("presentation", {"color": "red"}), "/streams/0/presentation/color",
         "#RRGGBB"),
        (lambda m: m["streams"][0].__setitem__("presentation", {"minZoom": 23}),
         "/streams/0/presentation/minZoom", "0 to 22"),
        (lambda m: m["streams"][0].__setitem__("presentation", {"labelField": "a b"}),
         "/streams/0/presentation/labelField", "top-level payload name"),
        (lambda m: m["streams"][0].__setitem__("presentation", {"tileProperties": ["ok", "not ok"]}),
         "/streams/0/presentation/tileProperties/1", "top-level payload name"),
        (lambda m: m["streams"][0].__setitem__("payloadSchema", {"$dynamicRef": "#x"}),
         "/streams/0/payloadSchema/$dynamicRef", "not allowed"),
        (lambda m: m["streams"][0].__setitem__("payloadSchema", {"$schema": "http://json-schema.org/draft-07/schema#"}),
         "/streams/0/payloadSchema/$schema", "must be absent"),
        (lambda m: m["streams"][0].__setitem__("payloadSchema", {"patternProperties": {"(?=x)": {}}}),
         "/streams/0/payloadSchema/patternProperties/(?=x)", "linear-time"),
        (lambda m: m["streams"][0].__setitem__("payloadSchema", {"type": "no-such-type"}),
         "/streams/0/payloadSchema/type", ""),
        (lambda m: m["streams"][0].update({"mode": "layer", "itemType": "feature", "geometry": "none"}),
         "/streams/0/geometry", "must have a geometry"),
    ],
)
def test_semantic_checks(mutate: Any, path: str, text: str) -> None:
    manifest = minimal_manifest()
    mutate(manifest)
    check = validate_manifest(manifest)
    assert not check.valid
    assert any(e.path == path and text in e.message for e in check.errors), check.errors


def test_oversized_payload_schema_is_refused() -> None:
    schema = {"type": "object", "description": "x" * (64 * 1024)}
    check = validate_manifest(minimal_manifest(payloadSchema=schema))
    assert any(e.path == "/streams/0/payloadSchema" and "maximum is 65536" in e.message for e in check.errors)


def test_non_object_manifest() -> None:
    check = validate_manifest([1, 2])
    assert not check.valid and check.errors[0].message == "the manifest must be a JSON object"
    assert check.minor == -1 and check.provider_id is None


def test_errors_are_deduplicated_and_check_exposes_helpers() -> None:
    manifest = minimal_manifest()
    check = validate_manifest(manifest)
    assert check.valid and check.minor == 0 and check.provider_id == "test-source"
    assert check.stream("s") is not None and check.stream(None) is None and check.stream("nope") is None
    assert check.to_dict() == {"manifestValid": True, "manifestErrors": [], "protocolUnsupported": False}
    assert as_manifest_check(check) is check
    bad = copy.deepcopy(manifest)
    bad["streams"].append(dict(bad["streams"][0]))
    with pytest.raises(ManifestInvalidError, match="duplicate stream id"):
        as_manifest_check(bad)


def test_is_duration_and_is_http_url() -> None:
    assert is_duration("PT1H") and is_duration("P7D") and is_duration("15m") and is_duration("pt1.5s")
    assert not is_duration("P") and not is_duration("PT") and not is_duration("-PT1H") and not is_duration("P1Y")
    assert is_http_url("https://a.example/x") and is_http_url("http://localhost:8099/edp/items")
    assert not is_http_url("https:///x") and not is_http_url("mailto:a@b") and not is_http_url(None)
