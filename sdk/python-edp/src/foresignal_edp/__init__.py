"""Foresignal EDP provider library: build, validate, push and serve External Data Protocol 1.0 data.

Independent of the Foresignal API client, so a provider project needs only this package (and
``jsonschema``). See ``docs/External-Data-Protocol.md`` for the protocol.

Typical use::

    from foresignal_edp import ManifestBuilder, StreamBuilder, PushClient, observation

    manifest = (ManifestBuilder("river-gauges", "River gauges")
                .publisher("Example Hydro").originator("example-hydro", "sensor-network")
                .access("unofficial-api").push()
                .stream(StreamBuilder("levels", mode="atomic", item_type="observation", kind="river-level")
                        .geometry("none").unit("m").suggest_binding("series"))
                .build())
    client = PushClient("https://foresignal.example", "ext-river-gauges", "eik_...")
    client.push([observation("levels", series_key="kaub", t="2026-10-11", value=1.42,
                             observed_at="2026-10-11T06:00:00Z")])
"""

from ._version import __version__
from .errors import (
    CursorError,
    EdpError,
    ItemRejection,
    ItemTooLargeError,
    ManifestInvalidError,
    PayloadSchemaError,
    PushError,
    SchemaError,
)
from .geometry import (
    bbox_polygon,
    line_string,
    multi_line_string,
    multi_point,
    multi_polygon,
    point,
    polygon,
    validate_geometry,
)
from .health import StatusBoard
from .items import (
    author,
    batch,
    delete,
    document,
    event,
    feature,
    forecast,
    media,
    new_snapshot_id,
    observation,
    origin,
    quantiles,
    question,
    snapshot,
    status,
)
from .manifest import (
    ManifestBuilder,
    ManifestCheck,
    StreamBuilder,
    StreamInfo,
    load_manifest,
    validate_manifest,
)
from .ndjson import BatchFormatError, decode_batch, decode_json, decode_ndjson, encode_json, encode_ndjson
from .payload_schema import compile_payload_schema, vet_payload_schema
from .periods import Period, iso_week, month, parse_period, quarter
from .protocol import (
    INGEST_KEY_HEADER,
    MAX_BATCH_BYTES,
    MAX_ITEMS,
    MAX_MINOR,
    PROTOCOL_VERSION,
    protocol_minor,
    registered_source_id,
)
from .pull import (
    HttpResponse,
    MemoryCursorStore,
    OpaqueCursorStore,
    Page,
    PullHandler,
    SearchQuery,
    SequenceSource,
    SnapshotSource,
    basic_auth,
    bearer_auth,
    header_auth,
    wsgi_app,
)
from .push import PreparedBatch, PushClient, PushResult, Transport, TransportError, TransportResponse, UrllibTransport
from .schema_store import load_schema, shape_errors
from .times import format_time, parse_rfc3339
from .validation import BatchCheck, ItemCheck, ValidItem, validate_batch, validate_items

__all__ = [
    "__version__",
    "BatchCheck", "BatchFormatError", "CursorError", "EdpError", "HttpResponse", "ItemCheck", "ItemRejection",
    "ItemTooLargeError", "ManifestBuilder", "ManifestCheck", "ManifestInvalidError", "MemoryCursorStore",
    "OpaqueCursorStore", "Page", "PayloadSchemaError", "Period", "PreparedBatch", "PullHandler", "PushClient",
    "PushError", "PushResult", "SchemaError", "SearchQuery", "SequenceSource", "SnapshotSource", "StatusBoard",
    "StreamBuilder", "StreamInfo", "Transport", "TransportError", "TransportResponse", "UrllibTransport",
    "ValidItem",
    "INGEST_KEY_HEADER", "MAX_BATCH_BYTES", "MAX_ITEMS", "MAX_MINOR", "PROTOCOL_VERSION",
    "author", "basic_auth", "batch", "bbox_polygon", "bearer_auth", "compile_payload_schema", "decode_batch",
    "decode_json", "decode_ndjson", "delete", "document", "encode_json", "encode_ndjson", "event", "feature",
    "forecast", "format_time", "header_auth", "iso_week", "line_string", "load_manifest", "load_schema", "media",
    "month", "multi_line_string", "multi_point", "multi_polygon", "new_snapshot_id", "observation", "origin",
    "parse_period", "parse_rfc3339", "point", "polygon", "protocol_minor", "quantiles", "quarter", "question",
    "registered_source_id", "shape_errors", "snapshot", "status", "validate_batch", "validate_geometry",
    "validate_items", "validate_manifest", "vet_payload_schema", "wsgi_app",
]
