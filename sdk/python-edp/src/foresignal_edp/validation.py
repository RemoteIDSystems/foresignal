"""Item and batch validation in the server's order (EDP §3.1), with the server's rejection codes.

Per item: a known ``type``; the stream exists (a source-level ``status`` may omit it); the type
matches the stream's ``itemType`` (``delete`` and ``status`` fit any stream); the item schema;
string and media limits; times (RFC 3339 with an offset, ``observedAt`` at most 5 minutes after
receipt, ``observedAt`` present unless the stream declares ``observedAtMode: received``);
``properties`` against the stream's ``payloadSchema``; geometry (§3.3); the observation period and
one granularity per series; forecast values. Invalid items are rejected individually.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from typing import Any, Iterable, Mapping, Sequence

from .errors import ItemRejection, SchemaError
from .geometry import validate_geometry
from .manifest import ManifestCheck, StreamInfo, as_manifest_check
from .ndjson import dumps
from .periods import parse_period
from .protocol import (
    ITEM_TYPES,
    MAX_BATCH_BYTES,
    MAX_FUTURE_SECONDS,
    MAX_ITEMS,
    MAX_MEDIA,
    MAX_MINOR,
    MAX_STRING_LENGTH,
    MAX_TEXT_LENGTH,
    QUANTILES,
    REQUIRED_QUANTILES,
    protocol_minor,
    registered_source_id,
)
from .schema_store import shape_errors
from .times import parse_rfc3339, utc_now

_TIMES = ("observedAt", "eventTime", "validUntil", "knownAt", "publishedAt", "madeAt")
_NEEDS_OBSERVED_AT = ("feature", "event", "observation", "document")


@dataclass(frozen=True)
class ValidItem:
    """An accepted item: its batch index, the item, its type and its stream (``None`` for a
    source-level ``status``)."""

    index: int
    item: dict[str, Any]
    type: str
    stream: StreamInfo | None


@dataclass
class ItemCheck:
    """The result of validating items: accepted and refused, each in batch order."""

    valid: list[ValidItem] = field(default_factory=list)
    rejected: list[ItemRejection] = field(default_factory=list)

    @property
    def ok(self) -> bool:
        """True when no item was refused."""
        return not self.rejected


@dataclass
class BatchCheck:
    """The result of validating a batch.

    ``code`` is the server's batch-level refusal (``VALIDATION_FAILED``, ``PROTOCOL_UNSUPPORTED``,
    ``SOURCE_MISMATCH`` or ``PAYLOAD_TOO_LARGE``) with its ``errors``; when the header is acceptable
    ``items`` holds the per-item result.
    """

    code: str | None = None
    errors: list[SchemaError] = field(default_factory=list)
    items: ItemCheck | None = None

    @property
    def ok(self) -> bool:
        """True when the batch is accepted and every item is valid."""
        return self.code is None and self.items is not None and self.items.ok


def _text(node: Any, name: str) -> str | None:
    if not isinstance(node, Mapping):
        return None
    value = node.get(name)
    if value is None or isinstance(value, (dict, list)):
        return None
    return value if isinstance(value, str) else str(value)


def _is_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def check_strings(node: Any, path: str, errors: list[SchemaError]) -> None:
    """Collects strings over the limits: ``/text`` 20,000 characters, everything else 2,048."""
    if isinstance(node, str):
        limit = MAX_TEXT_LENGTH if path == "/text" else MAX_STRING_LENGTH
        if len(node) > limit:
            errors.append(SchemaError(path, f"is {len(node)} characters; the maximum is {limit}"))
    elif isinstance(node, list):
        for i, child in enumerate(node):
            check_strings(child, f"{path}/{i}", errors)
    elif isinstance(node, dict):
        for key, value in node.items():
            if len(key) > MAX_STRING_LENGTH:
                errors.append(SchemaError(path, f"has a member name longer than {MAX_STRING_LENGTH} characters"))
            check_strings(value, f"{path}/{key.replace('~', '~0').replace('/', '~1')}", errors)


def forecast_problem(item: Mapping[str, Any]) -> str | None:
    """The problem with a forecast's question and values, or ``None``."""
    question = item.get("question")
    question_ref = item.get("questionRef")
    if not isinstance(question, Mapping) and not (isinstance(question_ref, str) and question_ref.strip()):
        return "a forecast needs a question or a questionRef"
    probability = item.get("probability")
    quantiles = item.get("quantiles")
    has_probability = _is_number(probability)
    has_quantiles = isinstance(quantiles, Mapping)
    if not has_probability and not has_quantiles:
        return "a forecast needs a probability or quantiles"
    if has_probability and not 0 <= float(probability) <= 1:  # type: ignore[arg-type]
        return "probability must be between 0 and 1"
    if has_quantiles:
        assert isinstance(quantiles, Mapping)
        if any(not _is_number(quantiles.get(name)) for name in REQUIRED_QUANTILES):
            return "quantiles need p05, p25, p50, p75 and p95"
        last: float | None = None
        last_name = ""
        for name in QUANTILES:
            value = quantiles.get(name)
            if value is None:
                continue
            if not _is_number(value) or not math.isfinite(value):
                return f"quantile {name} must be a finite number"
            if last is not None and value < last:
                return f"quantiles must be non-decreasing, but {name} < {last_name}"
            last, last_name = value, name
    return None


def _check(index: int, item: Any, manifest: ManifestCheck, received_at: datetime,
           granularity: dict[tuple[str | None, str | None], str]) -> ValidItem | ItemRejection:
    stream_id, item_id = _text(item, "stream"), _text(item, "id")
    if not isinstance(item, dict):
        return ItemRejection.of(index, None, None, "SCHEMA_INVALID", "an item must be a JSON object")
    item_type = _text(item, "type")
    if item_type is None or item_type not in ITEM_TYPES:
        return ItemRejection.of(index, stream_id, item_id, "SCHEMA_INVALID",
                                f"type must be one of [{', '.join(ITEM_TYPES)}]")
    stream: StreamInfo | None = None
    if stream_id is not None or item_type != "status":
        stream = manifest.stream(stream_id)
        if stream is None:
            return ItemRejection.of(index, stream_id, item_id, "UNKNOWN_STREAM",
                                    f"the manifest declares no stream '{stream_id}'")
        if item_type not in ("delete", "status") and item_type != stream.item_type:
            return ItemRejection.of(index, stream_id, item_id, "ITEM_TYPE_MISMATCH",
                                    f"stream '{stream_id}' carries {stream.item_type} items, not {item_type}")
    shape = shape_errors(item_type, item)
    if shape:
        return ItemRejection(index, stream_id, item_id, "SCHEMA_INVALID", tuple(shape))
    limits: list[SchemaError] = []
    check_strings(item, "", limits)
    media = item.get("media")
    if isinstance(media, list) and len(media) > MAX_MEDIA:
        limits.append(SchemaError("/media", f"has {len(media)} entries; the maximum is {MAX_MEDIA}"))
    if limits:
        return ItemRejection(index, stream_id, item_id, "LIMIT_EXCEEDED", tuple(limits))
    for name in _TIMES:
        value = item.get(name)
        if value is not None and parse_rfc3339(value) is None:
            return ItemRejection(index, stream_id, item_id, "SCHEMA_INVALID",
                                 (SchemaError(f"/{name}", "must be an RFC 3339 date-time with an offset"),))
    observed_at = parse_rfc3339(item.get("observedAt"))
    if observed_at is not None and observed_at > received_at + timedelta(seconds=MAX_FUTURE_SECONDS):
        return ItemRejection.of(index, stream_id, item_id, "OBSERVED_AT_IN_FUTURE",
                                f"observedAt {item.get('observedAt')} is more than 5 minutes after receipt")
    if item_type in _NEEDS_OBSERVED_AT and observed_at is None and (stream is None or not stream.observed_at_received):
        return ItemRejection.of(index, stream_id, item_id, "MISSING_OBSERVED_AT",
                                "observedAt is required unless the stream declares observedAtMode: received")
    if stream is not None and item_type not in ("status", "delete"):
        validator = manifest.payload_validators.get(stream.id)
        if validator is not None:
            payload = validator.validate(item.get("properties"))
            if payload:
                return ItemRejection(index, stream_id, item_id, "PAYLOAD_INVALID",
                                     tuple(SchemaError("/properties" + e.path, e.message) for e in payload))
        geometry = validate_geometry(item.get("geometry"), stream.geometry, "/geometry")
        if geometry:
            return ItemRejection(index, stream_id, item_id, "GEOMETRY_INVALID", tuple(geometry))
    if item_type == "observation":
        t = _text(item, "t")
        period = parse_period(t)
        if period is None:
            return ItemRejection.of(index, stream_id, item_id, "PERIOD_INVALID",
                                    f"t '{t}' is not YYYY, YYYY-Qn, YYYY-MM, YYYY-Www, YYYY-MM-DD or an RFC 3339 "
                                    f"timestamp")
        key = (stream_id, _text(item, "seriesKey"))
        previous = granularity.setdefault(key, period.granularity)
        if previous != period.granularity:
            return ItemRejection.of(index, stream_id, item_id, "GRANULARITY_MIXED",
                                    f"series '{key[1]}' already uses {previous} periods, not {period.granularity}")
    if item_type == "forecast":
        problem = forecast_problem(item)
        if problem is not None:
            return ItemRejection.of(index, stream_id, item_id, "FORECAST_INVALID", problem)
    return ValidItem(index, item, item_type, stream)


def validate_items(items: Sequence[Any], manifest: ManifestCheck | Mapping[str, Any], *,
                   received_at: datetime | None = None, max_minor: int = MAX_MINOR) -> ItemCheck:
    """Validates ``items`` against a manifest (a ``dict`` or a :class:`ManifestCheck`).

    ``received_at`` (default now) is the receipt time the 5-minute ``observedAt`` rule compares to.

    :raises ManifestInvalidError: when the manifest itself is invalid
    """
    check = as_manifest_check(manifest, max_minor=max_minor)
    now = received_at or utc_now()
    result = ItemCheck()
    granularity: dict[tuple[str | None, str | None], str] = {}
    for index, item in enumerate(items):
        outcome = _check(index, item, check, now, granularity)
        if isinstance(outcome, ValidItem):
            result.valid.append(outcome)
        else:
            result.rejected.append(outcome)
    return result


def validate_snapshot_header(snapshot: Any) -> str | None:
    """The problem with a batch's ``snapshot`` member, or ``None`` (absent or ``null`` is fine)."""
    if snapshot is None:
        return None
    if not isinstance(snapshot, Mapping):
        return "snapshot must be an object"
    snapshot_id, stream = _text(snapshot, "id"), _text(snapshot, "stream")
    page, final = snapshot.get("page"), snapshot.get("final")
    if (snapshot_id is None or len(snapshot_id) > 128 or stream is None or not isinstance(page, int)
            or isinstance(page, bool) or page < 1 or not isinstance(final, bool)):
        return "snapshot needs id (at most 128 characters), stream, page >= 1 and final"
    return None


def validate_batch(batch: Any, manifest: ManifestCheck | Mapping[str, Any], *, received_at: datetime | None = None,
                   registered_id: str | None = None, max_minor: int = MAX_MINOR) -> BatchCheck:
    """Validates a whole batch (a ``dict``, e.g. from :func:`foresignal_edp.ndjson.decode_batch`).

    Batch-level checks first (size, ``protocol`` not above the manifest's minor, ``sourceId`` equal to
    the provider id or its registered ``ext-`` id, ``items`` an array of at most 10,000, a well-formed
    ``snapshot``), then every item.

    :raises ManifestInvalidError: when the manifest itself is invalid
    """
    check = as_manifest_check(manifest, max_minor=max_minor)
    if not isinstance(batch, Mapping):
        return BatchCheck("VALIDATION_FAILED", [SchemaError("", "the batch must be a JSON object")])
    try:
        size = len(dumps(batch))
    except (TypeError, ValueError) as e:
        return BatchCheck("VALIDATION_FAILED", [SchemaError("", f"the batch is not JSON: {e}")])
    if size > MAX_BATCH_BYTES:
        return BatchCheck("PAYLOAD_TOO_LARGE", [SchemaError("", f"the batch is {size} bytes; the maximum is "
                                                                f"{MAX_BATCH_BYTES}")])
    minor = protocol_minor(batch.get("protocol"))
    if minor < 0:
        return BatchCheck("VALIDATION_FAILED", [SchemaError("/protocol", "must be foresignal-edp/1.N")])
    if minor > check.minor:
        return BatchCheck("PROTOCOL_UNSUPPORTED", [SchemaError(
            "/protocol", f"foresignal-edp/1.{minor} is newer than the manifest's 1.{check.minor}")])
    source_id = batch.get("sourceId")
    if source_id is not None:
        value = source_id if isinstance(source_id, str) else str(source_id)
        provider = check.provider_id or ""
        allowed = {provider, registered_source_id(provider)}
        if registered_id:
            allowed.add(registered_id)
        if value not in allowed:
            return BatchCheck("SOURCE_MISMATCH", [SchemaError("/sourceId", f"names source '{value}', not "
                                                                           f"'{registered_source_id(provider)}'")])
    items = batch.get("items")
    if items is not None and not isinstance(items, list):
        return BatchCheck("VALIDATION_FAILED", [SchemaError("/items", "must be an array")])
    item_list: list[Any] = items or []
    if len(item_list) > MAX_ITEMS:
        return BatchCheck("VALIDATION_FAILED", [SchemaError("/items", f"has {len(item_list)} items; the maximum is "
                                                                      f"{MAX_ITEMS}")])
    problem = validate_snapshot_header(batch.get("snapshot"))
    if problem is not None:
        return BatchCheck("VALIDATION_FAILED", [SchemaError("/snapshot", problem)])
    return BatchCheck(items=validate_items(item_list, check, received_at=received_at, max_minor=max_minor))


def rejected_reasons(check: ItemCheck) -> list[str]:
    """The rejection codes of ``check``, in order (convenient in tests and logs)."""
    return [r.reason for r in check.rejected]


def iter_valid_items(check: ItemCheck) -> Iterable[dict[str, Any]]:
    """The accepted items of ``check``."""
    return (v.item for v in check.valid)
