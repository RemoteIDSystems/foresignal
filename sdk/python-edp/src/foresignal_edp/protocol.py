"""Constants of the Foresignal External Data Protocol 1.x (``docs/External-Data-Protocol.md``)."""

from __future__ import annotations

import re
from typing import Final

#: The protocol version this library writes.
PROTOCOL_VERSION: Final = "foresignal-edp/1.0"

#: The highest ``1.N`` minor this library understands. A manifest with a higher minor is refused
#: with ``protocol_unsupported`` (the server answers ``PROTOCOL_UNSUPPORTED``).
MAX_MINOR: Final = 0

#: Every item type, in the order the specification lists them.
ITEM_TYPES: Final = ("feature", "event", "observation", "document", "forecast", "delete", "status")

#: The item types a stream may declare as its ``itemType``.
STREAM_ITEM_TYPES: Final = ("feature", "event", "observation", "document", "forecast")

#: Stream modes.
MODES: Final = ("layer", "atomic")

#: Declared stream geometries.
STREAM_GEOMETRIES: Final = ("Point", "LineString", "Polygon", "MultiPolygon", "any", "none")

#: GeoJSON geometry types accepted on items (no ``GeometryCollection``).
GEOMETRY_TYPES: Final = ("Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon")

#: Stream update styles.
UPDATE_STYLES: Final = ("incremental", "snapshot")

#: Analytic retention classes.
RETENTION_CLASSES: Final = ("event", "bulk-event", "series", "state", "track", "reference", "social")

#: Document types.
DOCUMENT_TYPES: Final = ("social-post", "article", "report", "transcript", "note")

#: Health values of ``status`` items.
HEALTH_VALUES: Final = ("ok", "degraded", "blocked", "layout-changed", "geo-restricted")

#: Targets of suggested bindings (prompt 138).
BINDING_TARGETS: Final = (
    "series", "canonical-family", "social", "news", "evidence", "indicator", "forecast", "place",
    "situation", "chronicle",
)

#: Originator classes (evidence independence).
ORIGINATOR_CLASSES: Final = (
    "sensor-network", "agency", "aggregator", "media", "crowd", "social", "operator", "derived",
)

#: How a provider gets its data.
ACCESS_ROUTES: Final = ("official", "unofficial-api", "scrape", "browser", "other")

#: How likely a provider is to break.
FRAGILITY_LEVELS: Final = ("low", "medium", "high")

#: Values of ``source.licence.redistribution`` (the wire key keeps its spelling).
REDISTRIBUTION_VALUES: Final = ("allowed", "local-only", "unknown")

#: Authentication styles of the pull and search endpoints.
AUTH_STYLES: Final = ("none", "bearer", "header", "basic")

#: Forecast quantile names in order, and the required ones.
QUANTILES: Final = ("p05", "p10", "p25", "p50", "p75", "p90", "p95")
REQUIRED_QUANTILES: Final = ("p05", "p25", "p50", "p75", "p95")

#: The names of the exported JSON Schemas.
SCHEMA_NAMES: Final = (
    "manifest", "batch", "item", "feature", "event", "observation", "document", "forecast", "delete", "status",
)

#: Per batch: 5 MB.
MAX_BATCH_BYTES: Final = 5 * 1024 * 1024
#: Per batch: 10,000 items.
MAX_ITEMS: Final = 10_000
#: Per batch: JSON nesting depth 32.
MAX_DEPTH: Final = 32
#: A document's ``text``: 20,000 characters.
MAX_TEXT_LENGTH: Final = 20_000
#: Any other string: 2,048 characters.
MAX_STRING_LENGTH: Final = 2_048
#: Per item: 20 media entries.
MAX_MEDIA: Final = 20
#: Per item: 10,000 geometry vertices.
MAX_VERTICES: Final = 10_000
#: A payload schema: 64 KB.
MAX_SCHEMA_BYTES: Final = 64 * 1024
#: A ``pattern`` or ``patternProperties`` key: 256 characters.
MAX_PATTERN_LENGTH: Final = 256
#: A pull ``intervalSeconds`` is at least 60.
MIN_PULL_INTERVAL_SECONDS: Final = 60
#: How far ahead of receipt an ``observedAt`` may be, in seconds.
MAX_FUTURE_SECONDS: Final = 300
#: The highest map zoom a presentation hint may name.
MAX_ZOOM: Final = 22

#: Registered external sources are ``ext-{source.id}``.
SOURCE_PREFIX: Final = "ext-"
#: The ingest-key header of pushes.
INGEST_KEY_HEADER: Final = "X-Foresignal-Ingest-Key"
#: The NDJSON media type.
NDJSON_MEDIA_TYPE: Final = "application/x-ndjson"
#: The JSON media type.
JSON_MEDIA_TYPE: Final = "application/json"
#: Where a provider conventionally publishes its manifest.
WELL_KNOWN_MANIFEST_PATH: Final = "/.well-known/foresignal-edp.json"
#: The page size Foresignal asks a pull provider for.
DEFAULT_PULL_LIMIT: Final = 500
#: The default page size of a search.
DEFAULT_SEARCH_LIMIT: Final = 100

SLUG: Final = re.compile(r"^[a-z0-9]+(-[a-z0-9]+)*$")
FIELD_NAME: Final = re.compile(r"^[A-Za-z0-9_]{1,64}$")
COLOR: Final = re.compile(r"^#[0-9A-Fa-f]{6}$")
_PROTOCOL: Final = re.compile(r"^foresignal-edp/1\.(0|[1-9][0-9]{0,8})$")


def protocol_minor(protocol: object) -> int:
    """Returns N of a ``foresignal-edp/1.N`` string, or -1 when it is not a valid 1.x protocol string."""
    if not isinstance(protocol, str):
        return -1
    match = _PROTOCOL.match(protocol)
    return int(match.group(1)) if match else -1


def registered_source_id(provider_id: str) -> str:
    """Returns the id a Foresignal instance registers a provider under: ``ext-{source.id}``."""
    return SOURCE_PREFIX + provider_id
