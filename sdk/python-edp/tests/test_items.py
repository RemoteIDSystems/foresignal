"""Typed item builders: every item type validates against its exported schema and a manifest."""

from __future__ import annotations

from datetime import date, datetime, timedelta, timezone

import pytest

from foresignal_edp import (
    author,
    batch,
    delete,
    document,
    event,
    feature,
    forecast,
    line_string,
    media,
    multi_line_string,
    multi_point,
    multi_polygon,
    observation,
    origin,
    point,
    polygon,
    quantiles,
    question,
    shape_errors,
    snapshot,
    status,
    validate_items,
)

from .conftest import CONFORMANCE_RECEIVED_AT

T = datetime(2026, 10, 11, 17, 0, tzinfo=timezone.utc)


def test_feature() -> None:
    item = feature("cameras", "cam-1", geometry=point(-71.05, 42.36), observed_at=T,
                   properties={"name": "Pier 4", "region": "north"}, valid_until=T + timedelta(hours=1),
                   origin=origin("example-port-watch", "operator", upstream_id="u-1"))
    assert item == {
        "stream": "cameras", "type": "feature", "id": "cam-1", "observedAt": "2026-10-11T17:00:00Z",
        "properties": {"name": "Pier 4", "region": "north"},
        "origin": {"originator": "example-port-watch", "class": "operator", "upstreamId": "u-1"},
        "geometry": {"type": "Point", "coordinates": [-71.05, 42.36]}, "validUntil": "2026-10-11T18:00:00Z",
    }
    assert shape_errors("feature", item) == []


def test_event() -> None:
    item = event("incidents", "inc-1", event_time="2026-10-11T16:55:00Z", observed_at=T,
                 geometry=point(56.4, 26.5), properties={"category": "x"}, upstream_event_id="up-9")
    assert item["eventTime"] == "2026-10-11T16:55:00Z"
    assert item["upstreamEventId"] == "up-9"
    assert shape_errors("event", item) == []


def test_observation_defaults_its_id_and_formats_periods() -> None:
    item = observation("transits", series_key="tanker", t=date(2026, 10, 10), value=31, observed_at=T,
                       known_at=T, unit="vessels/day")
    assert item["id"] == "tanker:2026-10-10"
    assert item["t"] == "2026-10-10"
    assert item["value"] == 31 and item["unit"] == "vessels/day"
    null_value = observation("transits", "cargo:w41", series_key="cargo", t="2026-W41", value=None, observed_at=T)
    assert null_value["value"] is None and null_value["id"] == "cargo:w41"
    instant = observation("transits", series_key="k", t=T, value=1.5, observed_at=T)
    assert instant["t"] == "2026-10-11T17:00:00Z"
    for i in (item, null_value, instant):
        assert shape_errors("observation", i) == []
    with pytest.raises(TypeError):
        observation("transits", series_key="k", t="2026", value=True)  # type: ignore[arg-type]


def test_document() -> None:
    item = document("notes", "n-1", document_type="note", text="Crane 3 out of service", observed_at=T,
                    title="Crane", url="https://example.org/n-1", author=author("@port", "Port", "42"),
                    language="en", published_at=T, about=["place:01J"], media=[media("https://e.org/c.jpg", "image")],
                    geometry=polygon([[(0, 0), (1, 0), (1, 1)]]), engagement={"likes": 3, "shares": None})
    assert item["author"] == {"handle": "@port", "name": "Port", "accountId": "42"}
    assert item["media"] == [{"url": "https://e.org/c.jpg", "type": "image"}]
    assert item["geometry"]["coordinates"][0][-1] == [0, 0]  # closed automatically
    assert shape_errors("document", item) == []


def test_forecast_variants_and_errors() -> None:
    by_probability = forecast("odds", "f-1", made_at=T, question=question("q-1", "Will it close?", close_time=T),
                              probability=0.2)
    by_quantiles = forecast("odds", "f-2", made_at=T, question_ref="01J9", quantiles=quantiles(1, 2, 3, 4, 5, p10=1.5))
    assert by_probability["question"]["closeTime"] == "2026-10-11T17:00:00Z"
    assert by_quantiles["quantiles"] == {"p05": 1, "p10": 1.5, "p25": 2, "p50": 3, "p75": 4, "p95": 5}
    assert shape_errors("forecast", by_probability) == []
    assert shape_errors("forecast", by_quantiles) == []
    with pytest.raises(ValueError, match="question"):
        forecast("odds", "f", made_at=T, probability=0.5)
    with pytest.raises(ValueError, match="probability or quantiles"):
        forecast("odds", "f", made_at=T, question_ref="q")
    with pytest.raises(ValueError, match="between 0 and 1"):
        forecast("odds", "f", made_at=T, question_ref="q", probability=1.5)
    with pytest.raises(ValueError, match="p05, p25"):
        forecast("odds", "f", made_at=T, question_ref="q", quantiles={"p05": 1})
    with pytest.raises(ValueError, match="non-decreasing"):
        quantiles(5, 4, 3, 2, 1)


def test_delete_and_status() -> None:
    assert delete("incidents", "inc-8790") == {"stream": "incidents", "type": "delete", "id": "inc-8790"}
    assert status("degraded", "layout changed") == {"type": "status", "health": "degraded",
                                                    "message": "layout changed"}
    assert status("ok", stream="transits") == {"stream": "transits", "type": "status", "health": "ok"}
    assert shape_errors("delete", delete("s", "x", observed_at=T, properties={"a": 1})) == []
    assert shape_errors("status", status("blocked")) == []
    with pytest.raises(ValueError):
        status("on-fire")


def test_extra_members_are_merged() -> None:
    item = feature("s", "1", extra={"x-new": 1})
    assert item["x-new"] == 1


def test_geometry_builders() -> None:
    assert point(1, 2, 3) == {"type": "Point", "coordinates": [1, 2, 3]}
    assert multi_point([(1, 2), (3, 4)])["coordinates"] == [[1, 2], [3, 4]]
    assert line_string([(0, 0), (1, 1)])["type"] == "LineString"
    assert multi_line_string([[(0, 0), (1, 1)]])["coordinates"] == [[[0, 0], [1, 1]]]
    assert polygon([[(0, 0), (1, 0), (1, 1), (0, 0)]])["coordinates"][0][-1] == [0, 0]
    assert len(polygon([[(0, 0), (1, 0), (1, 1)]], close=False)["coordinates"][0]) == 3
    assert multi_polygon([[[(0, 0), (1, 0), (1, 1)]]])["coordinates"][0][0][-1] == [0, 0]


def test_times_must_be_aware() -> None:
    with pytest.raises(ValueError, match="naive"):
        feature("s", "1", observed_at=datetime(2026, 1, 1))


def test_batch_and_snapshot_envelopes() -> None:
    page = snapshot("snap-1", "cameras", page=1, final=True, scope="north")
    envelope = batch([delete("s", "1")], source_id="harbor-cams", sent_at=T, snapshot=page)
    assert envelope == {"protocol": "foresignal-edp/1.0", "sourceId": "harbor-cams", "sentAt": "2026-10-11T17:00:00Z",
                        "snapshot": {"id": "snap-1", "stream": "cameras", "scope": "north", "page": 1, "final": True},
                        "items": [{"stream": "s", "type": "delete", "id": "1"}]}
    pulled = batch([], include_next_cursor=True)
    assert pulled["nextCursor"] is None and pulled["sentAt"].endswith("Z")
    with pytest.raises(ValueError):
        snapshot("x", "s", page=0, final=False)


def test_built_items_pass_the_full_validator(push_layer_manifest: dict) -> None:
    items = [
        feature("cameras", "cam-9", geometry=point(-71.0, 42.3), observed_at=T, properties={"name": "Dock 9"}),
        document("notes", "n-9", document_type="note", text="ok", observed_at=T),
        forecast("odds", "f-9", made_at=T, question_ref="q", probability=0.4),
        delete("cameras", "cam-1"),
        status("ok", stream="cameras"),
        status("ok"),
    ]
    check = validate_items(items, push_layer_manifest, received_at=CONFORMANCE_RECEIVED_AT)
    assert check.rejected == []
    assert [v.type for v in check.valid] == ["feature", "document", "forecast", "delete", "status", "status"]
    assert check.valid[-1].stream is None
