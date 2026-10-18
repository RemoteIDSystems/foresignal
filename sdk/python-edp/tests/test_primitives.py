"""Times, periods, geometry, payload-schema vetting, NDJSON and schema loading."""

from __future__ import annotations

from datetime import date, datetime, timedelta, timezone

import pytest

from foresignal_edp import (
    BatchFormatError,
    PayloadSchemaError,
    compile_payload_schema,
    decode_batch,
    decode_json,
    decode_ndjson,
    encode_json,
    encode_ndjson,
    format_time,
    iso_week,
    load_schema,
    month,
    parse_period,
    parse_rfc3339,
    protocol_minor,
    quarter,
    registered_source_id,
    validate_geometry,
    vet_payload_schema,
)

UTC = timezone.utc


class TestTimes:
    def test_parse_rfc3339(self) -> None:
        assert parse_rfc3339("2026-10-11T18:00:00Z") == datetime(2026, 10, 11, 18, tzinfo=UTC)
        assert parse_rfc3339("2026-10-11T20:00+02:00") == datetime(2026, 10, 11, 18, tzinfo=UTC)
        assert parse_rfc3339("2026-10-11t18:00:00.123456789z") == datetime(2026, 10, 11, 18, 0, 0, 123456, tzinfo=UTC)
        for bad in ("2026-10-11", "2026-02-30T00:00:00Z", "2026-10-11T24:00:00Z", "2026-10-11T18:00:60Z",
                    "2026-10-11T18:00:00", "2026-10-11T18:00:00+19:00", None, 5):
            assert parse_rfc3339(bad) is None, bad

    def test_format_time(self) -> None:
        assert format_time(datetime(2026, 10, 11, 20, tzinfo=timezone(timedelta(hours=2)))) == "2026-10-11T18:00:00Z"
        assert format_time(datetime(2026, 10, 11, 18, 0, 0, 250000, tzinfo=UTC)) == "2026-10-11T18:00:00.250Z"
        assert format_time(date(2026, 10, 11)) == "2026-10-11T00:00:00Z"
        assert format_time("as-is") == "as-is" and format_time(None) is None
        with pytest.raises(ValueError):
            format_time(datetime(2026, 10, 11))
        with pytest.raises(TypeError):
            format_time(3)  # type: ignore[arg-type]


class TestPeriods:
    @pytest.mark.parametrize("t, granularity, start, end", [
        ("2026", "year", "2026-01-01", "2027-01-01"),
        ("2026-Q4", "quarter", "2026-10-01", "2027-01-01"),
        ("2026-12", "month", "2026-12-01", "2027-01-01"),
        ("2026-W01", "week", "2025-12-29", "2026-01-05"),
        ("2026-10-11", "day", "2026-10-11", "2026-10-12"),
    ])
    def test_periods(self, t: str, granularity: str, start: str, end: str) -> None:
        period = parse_period(t)
        assert period is not None and period.granularity == granularity
        assert period.start == datetime.fromisoformat(start).replace(tzinfo=UTC)
        assert period.end == datetime.fromisoformat(end).replace(tzinfo=UTC)

    def test_instant_and_invalid(self) -> None:
        period = parse_period("2026-10-11T18:00:00+00:00")
        assert period is not None and period.granularity == "instant" and period.start == period.end
        for bad in ("2026-13", "2026-W00", "2025-W53", "2026-02-29", "26", "2026-10-11T18:00", None):
            assert parse_period(bad) is None, bad

    def test_helpers(self) -> None:
        assert quarter(2026, 4) == "2026-Q4" and month(2026, 3) == "2026-03" and iso_week(2026, 53) == "2026-W53"
        with pytest.raises(ValueError):
            quarter(2026, 5)
        with pytest.raises(ValueError):
            month(2026, 0)
        with pytest.raises(ValueError):
            iso_week(2025, 53)


class TestGeometry:
    def test_valid_types(self) -> None:
        ring = [[0, 0], [1, 0], [1, 1], [0, 0]]
        for geometry in ({"type": "Point", "coordinates": [10, 20, 5]},
                         {"type": "MultiPoint", "coordinates": [[0, 0], [1, 1]]},
                         {"type": "LineString", "coordinates": [[0, 0], [1, 1]]},
                         {"type": "MultiLineString", "coordinates": [[[0, 0], [1, 1]]]},
                         {"type": "Polygon", "coordinates": [ring]},
                         {"type": "MultiPolygon", "coordinates": [[ring]]}):
            assert validate_geometry(geometry) == [], geometry
        assert validate_geometry(None, "none") == []

    @pytest.mark.parametrize("geometry, declared, path, text", [
        ("x", None, "/geometry", "GeoJSON geometry object"),
        ({"type": "GeometryCollection", "geometries": []}, None, "/geometry/type", "must be Point"),
        ({"type": "Polygon", "coordinates": [[[0, 0], [1, 0], [1, 1], [0, 0]]]}, "Point", "/geometry/type",
         "does not match"),
        ({"type": "Point", "coordinates": [0, 0]}, "none", "/geometry/type", "does not match"),
        ({"type": "Point", "coordinates": "x"}, None, "/geometry/coordinates", "must be an array"),
        ({"type": "Point", "coordinates": [0]}, None, "/geometry/coordinates", "two or three numbers"),
        ({"type": "Point", "coordinates": [0, "a"]}, None, "/geometry/coordinates", "finite numbers"),
        ({"type": "Point", "coordinates": [0, 91]}, None, "/geometry/coordinates/1", "latitude"),
        ({"type": "Point", "coordinates": [-181, 0]}, None, "/geometry/coordinates/0", "longitude"),
        ({"type": "MultiPoint", "coordinates": []}, None, "/geometry/coordinates", "must not be empty"),
        ({"type": "MultiPoint", "coordinates": [5]}, None, "/geometry/coordinates/0", "must be an array"),
        ({"type": "Polygon", "coordinates": []}, None, "/geometry/coordinates", "at least one ring"),
        ({"type": "Polygon", "coordinates": [[[0, 0], [1, 0], [0, 0]]]}, None, "/geometry/coordinates/0",
         "at least four"),
        ({"type": "Polygon", "coordinates": [5]}, None, "/geometry/coordinates/0", "must be an array"),
    ])
    def test_problems(self, geometry: object, declared: str | None, path: str, text: str) -> None:
        errors = validate_geometry(geometry, declared)
        assert any(e.path == path and text in e.message for e in errors), errors

    def test_vertex_cap(self) -> None:
        line = [[0, i * 0.0001] for i in range(10_001)]
        errors = validate_geometry({"type": "LineString", "coordinates": line})
        assert any("maximum is 10000" in e.message for e in errors)
        assert validate_geometry({"type": "LineString", "coordinates": line[:10_000]}) == []

    def test_multi_polygon_declared_polygon_is_compatible(self) -> None:
        ring = [[0, 0], [1, 0], [1, 1], [0, 0]]
        assert validate_geometry({"type": "MultiPolygon", "coordinates": [[ring]]}, "Polygon") == []
        assert validate_geometry({"type": "Polygon", "coordinates": [ring]}, "MultiPolygon") == []
        assert validate_geometry({"type": "MultiLineString", "coordinates": [[[0, 0], [1, 1]]]}, "LineString") == []


class TestPayloadSchemas:
    def test_acceptable_schema_compiles_and_validates(self) -> None:
        validator = compile_payload_schema({
            "$schema": "https://json-schema.org/draft/2020-12/schema", "type": "object",
            "$defs": {"name": {"type": "string", "pattern": "^[A-Z]"}},
            "properties": {"name": {"$ref": "#/$defs/name"}}, "required": ["name"],
        })
        assert validator.validate({"name": "Kaub"}) == []
        errors = validator.validate({"name": "kaub"})
        assert errors and errors[0].path == "/name"
        assert validator.validate(None)[0].message.endswith("is a required property")

    @pytest.mark.parametrize("schema, path", [
        ([], ""),
        ({"$ref": "other.json#/x"}, "/$ref"),
        ({"properties": {"a": {"$recursiveRef": "#"}}}, "/properties/a/$recursiveRef"),
        ({"properties": {"a": {"pattern": "(a)\\1"}}}, "/properties/a/pattern"),
        ({"properties": {"a": {"pattern": "(?<!x)y"}}}, "/properties/a/pattern"),
        ({"properties": {"a": {"pattern": "a++"}}}, "/properties/a/pattern"),
        ({"properties": {"a": {"pattern": "("}}}, "/properties/a/pattern"),
        ({"patternProperties": {"x/y": {}, "(?!z)": {}}}, "/patternProperties/(?!z)"),
    ])
    def test_vetting(self, schema: object, path: str) -> None:
        problems = vet_payload_schema(schema)
        assert any(p.path == path for p in problems), problems
        with pytest.raises(PayloadSchemaError):
            compile_payload_schema(schema)

    def test_metaschema_errors(self) -> None:
        with pytest.raises(PayloadSchemaError) as raised:
            compile_payload_schema({"type": 5})
        assert raised.value.errors


class TestNdjson:
    def test_round_trip(self) -> None:
        header = {"protocol": "foresignal-edp/1.0", "sourceId": "a", "items": ["dropped"]}
        items = [{"stream": "s", "type": "delete", "id": "1"}, {"type": "status", "health": "ok"}]
        body = encode_ndjson(header, items)
        lines = body.decode().splitlines()
        assert len(lines) == 3 and '"items"' not in lines[0] and body.endswith(b"\n")
        decoded = decode_ndjson(body)
        assert decoded == {"protocol": "foresignal-edp/1.0", "sourceId": "a", "items": items}
        assert decode_batch(body, "application/x-ndjson; charset=utf-8") == decoded

    def test_headerless_and_blank_lines(self) -> None:
        body = b'{"stream":"s","type":"delete","id":"1"}\n\n{"stream":"s","type":"delete","id":"2"}'
        assert decode_ndjson(body) == {"items": [{"stream": "s", "type": "delete", "id": "1"},
                                                 {"stream": "s", "type": "delete", "id": "2"}]}
        assert encode_ndjson(None, [{"a": 1}]) == b'{"a":1}\n'

    def test_errors(self) -> None:
        with pytest.raises(BatchFormatError, match="Line 2"):
            decode_ndjson(b'{"protocol":"foresignal-edp/1.0"}\n[1]')
        with pytest.raises(BatchFormatError):
            decode_ndjson(b"{nope")
        deep = "[" * 40 + "]" * 40
        with pytest.raises(BatchFormatError, match="deeper"):
            decode_json('{"items": ' + deep + "}")
        with pytest.raises(BatchFormatError):
            decode_json(b"[]")
        with pytest.raises(BatchFormatError):
            decode_json(b'{"items": {}}')
        assert decode_json(b'{"protocol":"foresignal-edp/1.0"}')["items"] == []
        assert decode_batch(encode_json({"protocol": "p", "items": []})) == {"protocol": "p", "items": []}
        assert encode_json({"t": "é"}) == '{"t":"é"}'.encode()


def test_schemas_and_protocol_helpers() -> None:
    assert load_schema("manifest")["$id"].endswith("manifest.schema.json")
    with pytest.raises(ValueError):
        load_schema("nope")
    assert protocol_minor("foresignal-edp/1.12") == 12 and protocol_minor("foresignal-edp/2.0") == -1
    assert protocol_minor("foresignal-edp/1.01") == -1 and protocol_minor(None) == -1
    assert registered_source_id("abc") == "ext-abc"
