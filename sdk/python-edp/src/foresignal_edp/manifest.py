"""Manifest builder and validator (EDP §2, §3.1, §7).

:class:`ManifestBuilder` and :class:`StreamBuilder` write a manifest with Python names; the wire
keys stay as the protocol spells them (``licence`` included). :func:`validate_manifest` performs the
checks Foresignal performs at registration: the exported manifest schema, the protocol minor, at
least one delivery mechanism, unique stream ids, layer streams carrying ``feature`` or ``event``
items with a geometry, slugs, pull and search endpoints, field roles, presentation hints, TTLs, and
every ``payloadSchema`` (vetted and compiled). Every problem is reported, not just the first.
"""

from __future__ import annotations

import copy
import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Iterable, Mapping
from urllib.parse import urlsplit

from .errors import ManifestInvalidError, PayloadSchemaError, SchemaError
from .payload_schema import PayloadValidator, compile_payload_schema
from .protocol import (
    COLOR,
    FIELD_NAME,
    MAX_MINOR,
    MAX_ZOOM,
    MIN_PULL_INTERVAL_SECONDS,
    PROTOCOL_VERSION,
    SLUG,
    protocol_minor,
    registered_source_id,
)
from .schema_store import shape_errors

_ISO_DURATION = re.compile(
    r"^P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)(?:[.,]\d{0,9})?S)?)?$", re.IGNORECASE
)
_SHORTHAND_DURATION = re.compile(r"^(\d+)\s*([smhdw])$", re.IGNORECASE)


def _drop_none(data: Mapping[str, Any]) -> dict[str, Any]:
    return {k: v for k, v in data.items() if v is not None}


def is_duration(text: str) -> bool:
    """True for a non-negative ISO-8601 duration (``PT1H``, ``P7D``; days, hours, minutes and seconds,
    as ``java.time.Duration`` reads it) or the shorthand ``30s``, ``15m``, ``12h``, ``7d``, ``1w``."""
    trimmed = text.strip()
    if _SHORTHAND_DURATION.match(trimmed):
        return True
    match = _ISO_DURATION.match(trimmed)
    if not match or not any(match.group(i) is not None for i in range(1, 5)):
        return False
    has_time_part = "T" in trimmed.upper()
    return not has_time_part or any(match.group(i) is not None for i in range(2, 5))


def is_http_url(url: object) -> bool:
    """True for an absolute ``http`` or ``https`` URL with a host."""
    if not isinstance(url, str):
        return False
    try:
        parts = urlsplit(url)
    except ValueError:
        return False
    return parts.scheme.lower() in ("http", "https") and bool(parts.hostname)


@dataclass(frozen=True)
class StreamInfo:
    """The parts of a declared stream the item validator needs."""

    id: str
    mode: str
    item_type: str
    kind: str
    geometry: str
    update: str
    snapshot_scope: str | None
    observed_at_received: bool

    @property
    def snapshot(self) -> bool:
        """True for a ``snapshot`` stream."""
        return self.update == "snapshot"

    @staticmethod
    def from_dict(stream: Mapping[str, Any]) -> "StreamInfo":
        """Reads a manifest stream object (assumed valid)."""
        fields = stream.get("fields") or {}
        return StreamInfo(
            id=stream["id"],
            mode=stream["mode"],
            item_type=stream["itemType"],
            kind=stream["kind"],
            geometry=stream.get("geometry") or "any",
            update=stream.get("update") or "incremental",
            snapshot_scope=stream.get("snapshotScope"),
            observed_at_received=isinstance(fields, Mapping) and fields.get("observedAtMode") == "received",
        )


@dataclass
class ManifestCheck:
    """The result of validating a manifest.

    ``errors`` lists every problem (empty when valid); ``protocol_unsupported`` is true when the
    protocol minor is above ``max_minor`` (the server's ``PROTOCOL_UNSUPPORTED``); ``streams`` and
    ``payload_validators`` are filled only for a valid manifest.
    """

    manifest: Any
    errors: list[SchemaError] = field(default_factory=list)
    protocol_unsupported: bool = False
    streams: dict[str, StreamInfo] = field(default_factory=dict)
    payload_validators: dict[str, PayloadValidator] = field(default_factory=dict)

    @property
    def valid(self) -> bool:
        """True when the manifest may be registered."""
        return not self.errors

    @property
    def minor(self) -> int:
        """The manifest's protocol minor (-1 when invalid)."""
        return protocol_minor(self.manifest.get("protocol")) if isinstance(self.manifest, Mapping) else -1

    @property
    def provider_id(self) -> str | None:
        """``source.id``, when present."""
        if isinstance(self.manifest, Mapping) and isinstance(self.manifest.get("source"), Mapping):
            value = self.manifest["source"].get("id")
            return value if isinstance(value, str) else None
        return None

    def stream(self, stream_id: str | None) -> StreamInfo | None:
        """The declared stream ``stream_id``, or ``None``."""
        return None if stream_id is None else self.streams.get(stream_id)

    def raise_if_invalid(self) -> "ManifestCheck":
        """Returns ``self`` when valid; raises :class:`ManifestInvalidError` otherwise."""
        if not self.valid:
            raise ManifestInvalidError(self)
        return self

    def to_dict(self) -> dict[str, Any]:
        """A report in the shape of the server's ``EdpValidationReport`` manifest part."""
        return {"manifestValid": self.valid, "manifestErrors": [e.to_dict() for e in self.errors],
                "protocolUnsupported": self.protocol_unsupported}


def _text(node: Any, name: str) -> str | None:
    if not isinstance(node, Mapping):
        return None
    value = node.get(name)
    if value is None or isinstance(value, (dict, list)):
        return None
    return value if isinstance(value, str) else json.dumps(value)


def _strings(node: Any) -> list[str]:
    return [v for v in node if isinstance(v, str)] if isinstance(node, list) else []


def _check_source(source: Any, errors: list[SchemaError]) -> None:
    if not isinstance(source, Mapping):
        return
    source_id = _text(source, "id")
    if source_id is not None and (not SLUG.match(source_id) or len(source_id) > 48):
        errors.append(SchemaError("/source/id", "must be a slug of at most 48 characters"))
    originator = _text(source.get("originator"), "id")
    if originator is not None and not SLUG.match(originator):
        errors.append(SchemaError("/source/originator/id", "must be a slug"))


def _check_endpoint(endpoint: Mapping[str, Any], url_field: str, path: str, errors: list[SchemaError]) -> None:
    if not is_http_url(_text(endpoint, url_field)):
        errors.append(SchemaError(f"{path}/{url_field}", "must be an absolute http or https URL"))
    auth = _text(endpoint, "auth")
    if auth is not None and auth not in ("none", "bearer", "header", "basic"):
        errors.append(SchemaError(f"{path}/auth", "must be none, bearer, header or basic"))
    if auth == "header" and _text(endpoint, "header") is None:
        errors.append(SchemaError(f"{path}/header", "is required when auth is header"))


def _check_transport(transport: Any, errors: list[SchemaError]) -> None:
    if not isinstance(transport, Mapping):
        errors.append(SchemaError("/transport", "is required"))
        return
    push = transport.get("push") is True
    pull = transport.get("pull")
    if not push and not isinstance(pull, Mapping):
        errors.append(SchemaError("/transport",
                                  "must declare at least one delivery mechanism: push: true or a pull endpoint"))
    if isinstance(pull, Mapping):
        _check_endpoint(pull, "itemsUrl", "/transport/pull", errors)
        interval = pull.get("intervalSeconds")
        if interval is not None and (
            not isinstance(interval, int) or isinstance(interval, bool) or interval < MIN_PULL_INTERVAL_SECONDS
        ):
            errors.append(SchemaError("/transport/pull/intervalSeconds", "must be an integer of at least 60"))
    search = transport.get("search")
    if isinstance(search, Mapping):
        _check_endpoint(search, "url", "/transport/search", errors)


def _check_names(names: list[str], path: str, errors: list[SchemaError]) -> None:
    for i, name in enumerate(names):
        if not FIELD_NAME.match(name):
            errors.append(SchemaError(f"{path}/{i}", "must be a top-level payload name"))


def _check_fields(fields: Any, path: str, errors: list[SchemaError]) -> None:
    if not isinstance(fields, Mapping):
        return
    title = _text(fields, "title")
    if title is not None and not FIELD_NAME.match(title):
        errors.append(SchemaError(path + "/title", "must be a top-level payload name"))
    significant = _strings(fields.get("significant"))
    volatile = _strings(fields.get("volatile"))
    _check_names(significant, path + "/significant", errors)
    _check_names(volatile, path + "/volatile", errors)
    for name in significant:
        if name in volatile:
            errors.append(SchemaError(path, f"'{name}' is both significant and volatile"))


def _is_int(value: Any) -> bool:
    return isinstance(value, int) and not isinstance(value, bool)


def _check_presentation(presentation: Any, path: str, errors: list[SchemaError]) -> None:
    if not isinstance(presentation, Mapping):
        return
    color = _text(presentation, "color")
    if color is not None and not COLOR.match(color):
        errors.append(SchemaError(path + "/color", "must be #RRGGBB"))
    for zoom in ("minZoom", "maxZoom"):
        value = presentation.get(zoom)
        if value is not None and (not _is_int(value) or value < 0 or value > MAX_ZOOM):
            errors.append(SchemaError(f"{path}/{zoom}", f"must be an integer from 0 to {MAX_ZOOM}"))
    label = _text(presentation, "labelField")
    if label is not None and not FIELD_NAME.match(label):
        errors.append(SchemaError(path + "/labelField", "must be a top-level payload name"))
    _check_names(_strings(presentation.get("tileProperties")), path + "/tileProperties", errors)


def _check_stream(stream: Any, path: str, ids: set[str], validators: dict[str, PayloadValidator],
                  errors: list[SchemaError]) -> None:
    if not isinstance(stream, Mapping):
        return
    stream_id = _text(stream, "id")
    if stream_id is not None:
        if not SLUG.match(stream_id):
            errors.append(SchemaError(path + "/id", "must be a slug"))
        if stream_id in ids:
            errors.append(SchemaError(path + "/id", f"duplicate stream id '{stream_id}'"))
        ids.add(stream_id)
    kind = _text(stream, "kind")
    if kind is not None and not SLUG.match(kind):
        errors.append(SchemaError(path + "/kind", "must be a slug"))
    mode, item_type, geometry = _text(stream, "mode"), _text(stream, "itemType"), _text(stream, "geometry")
    if mode == "layer":
        if item_type is not None and item_type not in ("feature", "event"):
            errors.append(SchemaError(path + "/itemType", "a layer stream carries feature or event items"))
        if geometry == "none":
            errors.append(SchemaError(path + "/geometry", "a layer stream must have a geometry"))
    ttl = _text(stream, "ttl")
    if ttl is not None and not is_duration(ttl):
        errors.append(SchemaError(path + "/ttl", "must be an ISO-8601 duration"))
    if _text(stream, "snapshotScope") is not None and _text(stream, "update") != "snapshot":
        errors.append(SchemaError(path + "/snapshotScope", "only snapshot streams have a snapshot scope"))
    _check_fields(stream.get("fields"), path + "/fields", errors)
    _check_presentation(stream.get("presentation"), path + "/presentation", errors)
    schema = stream.get("payloadSchema")
    if schema is not None:
        try:
            compiled = compile_payload_schema(schema)
            if stream_id is not None:
                validators[stream_id] = compiled
        except PayloadSchemaError as e:
            for error in e.errors:
                errors.append(SchemaError(path + "/payloadSchema" + error.path, error.message))


def validate_manifest(manifest: Any, *, max_minor: int = MAX_MINOR) -> ManifestCheck:
    """Validates ``manifest`` (a ``dict``). Never raises for invalid input; the problems are in the result.

    ``max_minor`` is the highest ``foresignal-edp/1.N`` minor to accept (``GET /v1/external/protocol``
    reports an instance's ``maxMinor``; 0 for EDP 1.0).
    """
    if not isinstance(manifest, Mapping):
        return ManifestCheck(manifest, [SchemaError("", "the manifest must be a JSON object")])
    errors: list[SchemaError] = []
    unsupported = False
    minor = protocol_minor(manifest.get("protocol"))
    if minor < 0:
        errors.append(SchemaError("/protocol", "must be foresignal-edp/1.N"))
    elif minor > max_minor:
        unsupported = True
        errors.append(SchemaError("/protocol", f"foresignal-edp/1.{minor} is newer than this validator supports "
                                               f"(highest minor {max_minor})"))
    errors.extend(shape_errors("manifest", manifest))
    _check_transport(manifest.get("transport"), errors)
    _check_source(manifest.get("source"), errors)
    validators: dict[str, PayloadValidator] = {}
    ids: set[str] = set()
    streams = manifest.get("streams")
    if isinstance(streams, list):
        for i, stream in enumerate(streams):
            _check_stream(stream, f"/streams/{i}", ids, validators, errors)
    unique = list(dict.fromkeys(errors))
    check = ManifestCheck(manifest, unique, unsupported)
    if not unique:
        check.streams = {s["id"]: StreamInfo.from_dict(s) for s in manifest["streams"]}
        check.payload_validators = validators
    return check


def load_manifest(path: str | Path) -> dict[str, Any]:
    """Reads a manifest JSON file."""
    with open(path, encoding="utf-8") as f:
        data: dict[str, Any] = json.load(f)
    return data


def as_manifest_check(manifest: "ManifestCheck | Mapping[str, Any]", *, max_minor: int = MAX_MINOR) -> ManifestCheck:
    """Returns ``manifest`` when it is already a :class:`ManifestCheck`, else validates it.

    :raises ManifestInvalidError: when the manifest is invalid
    """
    check = manifest if isinstance(manifest, ManifestCheck) else validate_manifest(manifest, max_minor=max_minor)
    return check.raise_if_invalid()


class StreamBuilder:
    """Builds one ``streams[]`` entry. Every setter returns ``self``."""

    def __init__(self, stream_id: str, *, mode: str, item_type: str, kind: str) -> None:
        """Starts a stream with its required members: ``id``, ``mode`` (``layer`` or ``atomic``),
        ``itemType`` and ``kind``."""
        self._data: dict[str, Any] = {"id": stream_id, "mode": mode, "itemType": item_type, "kind": kind}

    def title(self, title: str | None) -> "StreamBuilder":
        """Sets ``title``."""
        self._data["title"] = title
        return self

    def description(self, description: str | None) -> "StreamBuilder":
        """Sets ``description``."""
        self._data["description"] = description
        return self

    def geometry(self, geometry: str) -> "StreamBuilder":
        """Sets ``geometry``: ``Point``, ``LineString``, ``Polygon``, ``MultiPolygon``, ``any`` or ``none``."""
        self._data["geometry"] = geometry
        return self

    def incremental(self) -> "StreamBuilder":
        """Sets ``update: incremental`` (items are changes; only ``delete`` items remove anything)."""
        self._data["update"] = "incremental"
        self._data.pop("snapshotScope", None)
        return self

    def snapshot(self, scope_property: str | None = None) -> "StreamBuilder":
        """Sets ``update: snapshot`` and ``snapshotScope`` (``None``: the whole stream is one scope;
        otherwise the item property whose value names a scope)."""
        self._data["update"] = "snapshot"
        self._data["snapshotScope"] = scope_property
        return self

    def payload_schema(self, schema: Mapping[str, Any] | None) -> "StreamBuilder":
        """Sets ``payloadSchema`` (JSON Schema 2020-12 for the items' ``properties``)."""
        self._data["payloadSchema"] = copy.deepcopy(dict(schema)) if schema is not None else None
        return self

    def fields(self, *, title: str | None = None, significant: Iterable[str] = (), volatile: Iterable[str] = (),
               observed_at_mode: str | None = None) -> "StreamBuilder":
        """Sets ``fields``: the title field, significant and volatile fields, and ``observedAtMode``
        (``upstream`` or ``received``)."""
        self._data["fields"] = _drop_none({
            "title": title, "significant": list(significant), "volatile": list(volatile),
            "observedAtMode": observed_at_mode,
        })
        return self

    def ttl(self, ttl: str | None) -> "StreamBuilder":
        """Sets the record ``ttl`` (ISO-8601 duration such as ``PT1H``)."""
        self._data["ttl"] = ttl
        return self

    def retention_class(self, retention_class: str) -> "StreamBuilder":
        """Sets ``retentionClass``."""
        self._data["retentionClass"] = retention_class
        return self

    def unit(self, unit: str | None) -> "StreamBuilder":
        """Sets ``unit`` (observation streams)."""
        self._data["unit"] = unit
        return self

    def document_type(self, document_type: str | None) -> "StreamBuilder":
        """Sets ``documentType`` (document streams)."""
        self._data["documentType"] = document_type
        return self

    def presentation(self, *, color: str | None = None, icon: str | None = None, label_field: str | None = None,
                     min_zoom: int | None = None, max_zoom: int | None = None,
                     tile_properties: Iterable[str] | None = None) -> "StreamBuilder":
        """Sets the layer ``presentation`` hints."""
        self._data["presentation"] = _drop_none({
            "color": color, "icon": icon, "labelField": label_field, "minZoom": min_zoom, "maxZoom": max_zoom,
            "tileProperties": list(tile_properties) if tile_properties is not None else None,
        })
        return self

    def coverage(self, *, area: Mapping[str, Any] | None = None, family: str | None = None,
                 p_detect: float | None = None) -> "StreamBuilder":
        """Sets the declared ``coverage``: footprint geometry, canonical family and detection probability."""
        self._data["coverage"] = {"area": dict(area) if area is not None else None, "family": family,
                                  "pDetect": p_detect}
        return self

    def complete(self, complete: bool = True) -> "StreamBuilder":
        """Sets ``complete``: the stream is a complete record of its scope."""
        self._data["complete"] = complete
        return self

    def suggest_binding(self, target: str, config: Mapping[str, Any] | None = None) -> "StreamBuilder":
        """Appends a ``suggestedBindings`` entry (``series``, ``canonical-family``, ``social``, ...)."""
        entry: dict[str, Any] = {"target": target}
        if config is not None:
            entry["config"] = dict(config)
        self._data.setdefault("suggestedBindings", []).append(entry)
        return self

    def set(self, name: str, value: Any) -> "StreamBuilder":
        """Sets any other member by its wire name (for later 1.x additions)."""
        self._data[name] = value
        return self

    def build(self) -> dict[str, Any]:
        """Returns the stream object, without members set to ``None`` except ``snapshotScope``."""
        out = {k: v for k, v in self._data.items() if v is not None or k == "snapshotScope"}
        if "snapshotScope" in out and out.get("update") != "snapshot":
            out.pop("snapshotScope")
        return copy.deepcopy(out)


class ManifestBuilder:
    """Builds a manifest. Every setter returns ``self``; :meth:`build` validates by default.

    Example::

        manifest = (ManifestBuilder("river-gauges", "River gauges")
                    .publisher("Example Hydro")
                    .originator("example-hydro", "sensor-network")
                    .access("unofficial-api", "medium")
                    .pull("https://hydro.example.org/edp/items", interval_seconds=300)
                    .stream(StreamBuilder("levels", mode="atomic", item_type="observation", kind="river-level")
                            .unit("m").geometry("none").suggest_binding("series"))
                    .build())
    """

    def __init__(self, source_id: str, name: str, *, protocol: str = PROTOCOL_VERSION) -> None:
        """Starts a manifest for provider ``source_id`` (a slug; registered as ``ext-{source_id}``)."""
        self._protocol = protocol
        self._source: dict[str, Any] = {"id": source_id, "name": name}
        self._transport: dict[str, Any] = {}
        self._streams: list[dict[str, Any]] = []
        self._extra: dict[str, Any] = {}

    @property
    def registered_id(self) -> str:
        """The id an instance registers this provider under (``ext-{source.id}``)."""
        return registered_source_id(self._source["id"])

    def description(self, description: str | None) -> "ManifestBuilder":
        """Sets ``source.description``."""
        self._source["description"] = description
        return self

    def version(self, version: str | None) -> "ManifestBuilder":
        """Sets ``source.version`` (the provider's own version)."""
        self._source["version"] = version
        return self

    def publisher(self, name: str, *, url: str | None = None, contact: str | None = None) -> "ManifestBuilder":
        """Sets ``source.publisher``."""
        self._source["publisher"] = _drop_none({"name": name, "url": url, "contact": contact})
        return self

    def homepage(self, homepage: str | None) -> "ManifestBuilder":
        """Sets ``source.homepage``."""
        self._source["homepage"] = homepage
        return self

    def license(self, *, id: str | None = None, url: str | None = None, attribution: str | None = None,
                redistribution: str = "unknown") -> "ManifestBuilder":
        """Sets the source license (wire key ``source.licence``); ``redistribution`` is ``allowed``,
        ``local-only`` or ``unknown`` and sets the default egress."""
        self._source["licence"] = _drop_none({"id": id, "url": url, "attribution": attribution,
                                              "redistribution": redistribution})
        return self

    def terms(self, summary: str | None, url: str | None = None) -> "ManifestBuilder":
        """Sets ``source.terms``."""
        self._source["terms"] = {"summary": summary, "url": url}
        return self

    def access(self, route: str, fragility: str | None = None) -> "ManifestBuilder":
        """Sets ``source.access``: ``route`` (``official``, ``unofficial-api``, ``scrape``, ``browser``,
        ``other``) and ``fragility`` (``low``, ``medium``, ``high``)."""
        self._source["access"] = _drop_none({"route": route, "fragility": fragility})
        return self

    def originator(self, originator_id: str, originator_class: str,
                   aggregates: Iterable[str] | None = None) -> "ManifestBuilder":
        """Sets ``source.originator`` (who originally observed the data, for evidence independence)."""
        entry: dict[str, Any] = {"id": originator_id, "class": originator_class}
        if aggregates is not None:
            entry["aggregates"] = list(aggregates)
        self._source["originator"] = entry
        return self

    def private(self, private: bool = True) -> "ManifestBuilder":
        """Sets ``source.private`` (the data is personal)."""
        self._source["private"] = private
        return self

    def push(self, enabled: bool = True) -> "ManifestBuilder":
        """Declares ``transport.push``."""
        self._transport["push"] = enabled
        return self

    def pull(self, items_url: str, *, interval_seconds: int | None = None, auth: str | None = None,
             header: str | None = None) -> "ManifestBuilder":
        """Declares ``transport.pull``; ``auth`` is ``none``, ``bearer``, ``header`` (with ``header``) or
        ``basic``. The credential itself is entered by the operator, never put in the manifest."""
        self._transport["pull"] = _drop_none({"itemsUrl": items_url, "intervalSeconds": interval_seconds,
                                              "auth": auth, "header": header})
        return self

    def search(self, url: str, *, auth: str | None = None, header: str | None = None) -> "ManifestBuilder":
        """Declares the optional ``transport.search`` endpoint."""
        self._transport["search"] = _drop_none({"url": url, "auth": auth, "header": header})
        return self

    def stream(self, stream: StreamBuilder | Mapping[str, Any]) -> "ManifestBuilder":
        """Appends a stream (a :class:`StreamBuilder` or a ready stream object)."""
        self._streams.append(stream.build() if isinstance(stream, StreamBuilder) else copy.deepcopy(dict(stream)))
        return self

    def set(self, name: str, value: Any) -> "ManifestBuilder":
        """Sets any other top-level member by its wire name."""
        self._extra[name] = value
        return self

    def to_dict(self) -> dict[str, Any]:
        """Returns the manifest without validating it."""
        source = {k: v for k, v in self._source.items() if v is not None}
        manifest: dict[str, Any] = {"protocol": self._protocol, "source": source,
                                    "transport": copy.deepcopy(self._transport),
                                    "streams": copy.deepcopy(self._streams)}
        manifest.update(copy.deepcopy(self._extra))
        return manifest

    def validate(self, *, max_minor: int = MAX_MINOR) -> ManifestCheck:
        """Validates the manifest built so far."""
        return validate_manifest(self.to_dict(), max_minor=max_minor)

    def build(self, *, validate: bool = True, max_minor: int = MAX_MINOR) -> dict[str, Any]:
        """Returns the manifest.

        :raises ManifestInvalidError: when ``validate`` is true and the manifest is invalid
        """
        manifest = self.to_dict()
        if validate:
            validate_manifest(manifest, max_minor=max_minor).raise_if_invalid()
        return manifest
