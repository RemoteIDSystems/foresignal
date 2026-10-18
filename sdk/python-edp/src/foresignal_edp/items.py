"""Typed builders for every EDP item type, the batch envelope and snapshot pages (EDP §3).

Each builder returns a plain ``dict`` in wire form (camelCase keys), omitting optional members left
as ``None``. Times accept an aware ``datetime``, a ``date`` or a ready RFC 3339 string. ``extra``
merges further members (for example a later 1.x field) into the item.
"""

from __future__ import annotations

import uuid
from datetime import date, datetime
from typing import Any, Iterable, Literal, Mapping, Sequence, TypedDict, Union

from .periods import format_period
from .protocol import HEALTH_VALUES, PROTOCOL_VERSION, REQUIRED_QUANTILES
from .times import format_time, utc_now

TimeLike = Union[datetime, date, str]
Item = dict[str, Any]


class Snapshot(TypedDict):
    """A batch's ``snapshot`` member (EDP §3.2)."""

    id: str
    stream: str
    scope: str | None
    page: int
    final: bool


def _time(value: TimeLike | None) -> str | None:
    return format_time(value)


def _common(stream: str | None, item_type: str, item_id: str | None, observed_at: TimeLike | None,
            properties: Mapping[str, Any] | None, origin_value: Mapping[str, Any] | None) -> Item:
    item: Item = {}
    if stream is not None:
        item["stream"] = stream
    item["type"] = item_type
    if item_id is not None:
        item["id"] = item_id
    if observed_at is not None:
        item["observedAt"] = _time(observed_at)
    if properties is not None:
        item["properties"] = dict(properties)
    if origin_value is not None:
        item["origin"] = dict(origin_value)
    return item


def _finish(item: Item, optional: Mapping[str, Any], extra: Mapping[str, Any] | None) -> Item:
    for key, value in optional.items():
        if value is not None:
            item[key] = value
    if extra:
        item.update(extra)
    return item


def origin(originator: str, originator_class: str | None = None, *, upstream_id: str | None = None,
           derived_from: Iterable[str] | None = None) -> dict[str, Any]:
    """Builds an ``origin`` object: the original observer's slug, its class, the upstream id, and the
    items or upstream ids this one was derived from."""
    out: dict[str, Any] = {"originator": originator}
    if originator_class is not None:
        out["class"] = originator_class
    if upstream_id is not None:
        out["upstreamId"] = upstream_id
    if derived_from is not None:
        out["derivedFrom"] = list(derived_from)
    return out


def feature(stream: str, item_id: str, *, geometry: Mapping[str, Any] | None = None,
            observed_at: TimeLike | None = None, properties: Mapping[str, Any] | None = None,
            event_time: TimeLike | None = None, valid_until: TimeLike | None = None,
            origin: Mapping[str, Any] | None = None, extra: Mapping[str, Any] | None = None) -> Item:
    """A ``feature`` item: the current state of an entity (layer streams)."""
    item = _common(stream, "feature", item_id, observed_at, properties, origin)
    return _finish(item, {"geometry": dict(geometry) if geometry is not None else None,
                          "eventTime": _time(event_time), "validUntil": _time(valid_until)}, extra)


def event(stream: str, item_id: str, *, event_time: TimeLike, observed_at: TimeLike | None = None,
          geometry: Mapping[str, Any] | None = None, properties: Mapping[str, Any] | None = None,
          upstream_event_id: str | None = None, origin: Mapping[str, Any] | None = None,
          extra: Mapping[str, Any] | None = None) -> Item:
    """An ``event`` item: a discrete occurrence; ``upstream_event_id`` lets evidence matching
    recognize the same event from other sources."""
    item = _common(stream, "event", item_id, observed_at, properties, origin)
    return _finish(item, {"eventTime": _time(event_time),
                          "geometry": dict(geometry) if geometry is not None else None,
                          "upstreamEventId": upstream_event_id}, extra)


def observation(stream: str, item_id: str | None = None, *, series_key: str, t: TimeLike,
                value: float | int | None, observed_at: TimeLike | None = None, known_at: TimeLike | None = None,
                unit: str | None = None, properties: Mapping[str, Any] | None = None,
                origin: Mapping[str, Any] | None = None, extra: Mapping[str, Any] | None = None) -> Item:
    """An ``observation`` item: a value (or ``None``) of series ``{kind}/{series_key}`` at period ``t``.

    ``item_id`` defaults to ``{series_key}:{t}``, which keeps re-sends of the same point idempotent.
    ``t`` is a period string, a ``date`` (``YYYY-MM-DD``) or an aware ``datetime`` (an instant).
    """
    if isinstance(value, bool):
        raise TypeError("An observation value is a number or None, not a bool")
    period = format_period(t)
    item = _common(stream, "observation", item_id or f"{series_key}:{period}", observed_at, properties, origin)
    item["seriesKey"] = series_key
    item["t"] = period
    item["value"] = value
    return _finish(item, {"knownAt": _time(known_at), "unit": unit}, extra)


def author(handle: str | None = None, name: str | None = None, account_id: str | None = None) -> dict[str, Any]:
    """A document ``author`` object."""
    return {k: v for k, v in {"handle": handle, "name": name, "accountId": account_id}.items() if v is not None}


def media(url: str, media_type: str | None = None, thumb_url: str | None = None) -> dict[str, Any]:
    """A document ``media`` entry (links only)."""
    out: dict[str, Any] = {"url": url}
    if media_type is not None:
        out["type"] = media_type
    if thumb_url is not None:
        out["thumbUrl"] = thumb_url
    return out


def document(stream: str, item_id: str, *, document_type: str, text: str, observed_at: TimeLike | None = None,
             title: str | None = None, url: str | None = None, author: Mapping[str, Any] | None = None,
             language: str | None = None, published_at: TimeLike | None = None, about: Iterable[str] | None = None,
             media: Iterable[Mapping[str, Any]] | None = None, geometry: Mapping[str, Any] | None = None,
             engagement: Mapping[str, float | None] | None = None, properties: Mapping[str, Any] | None = None,
             origin: Mapping[str, Any] | None = None, extra: Mapping[str, Any] | None = None) -> Item:
    """A ``document`` item: ``social-post``, ``article``, ``report``, ``transcript`` or ``note``
    (``text`` at most 20,000 characters)."""
    item = _common(stream, "document", item_id, observed_at, properties, origin)
    item["documentType"] = document_type
    item["text"] = text
    return _finish(item, {
        "title": title, "url": url, "author": dict(author) if author is not None else None,
        "language": language, "publishedAt": _time(published_at),
        "about": list(about) if about is not None else None,
        "media": [dict(m) for m in media] if media is not None else None,
        "geometry": dict(geometry) if geometry is not None else None,
        "engagement": dict(engagement) if engagement is not None else None,
    }, extra)


def question(external_id: str, title: str, *, close_time: TimeLike | None = None,
             resolution_criteria: str | None = None) -> dict[str, Any]:
    """A forecast ``question`` object (the provider's own question)."""
    out: dict[str, Any] = {"externalId": external_id, "title": title}
    if close_time is not None:
        out["closeTime"] = _time(close_time)
    if resolution_criteria is not None:
        out["resolutionCriteria"] = resolution_criteria
    return out


def quantiles(p05: float, p25: float, p50: float, p75: float, p95: float, *, p10: float | None = None,
              p90: float | None = None) -> dict[str, float]:
    """A ``quantiles`` object; values must be non-decreasing in quantile order.

    :raises ValueError: when they decrease
    """
    values = {"p05": p05, "p10": p10, "p25": p25, "p50": p50, "p75": p75, "p90": p90, "p95": p95}
    present = {k: v for k, v in values.items() if v is not None}
    ordered = list(present.values())
    if any(b < a for a, b in zip(ordered, ordered[1:])):
        raise ValueError("Quantiles must be non-decreasing (p05 <= p10 <= p25 <= p50 <= p75 <= p90 <= p95)")
    return present


def forecast(stream: str, item_id: str, *, made_at: TimeLike, question: Mapping[str, Any] | None = None,
             question_ref: str | None = None, probability: float | None = None,
             quantiles: Mapping[str, float] | None = None, observed_at: TimeLike | None = None,
             properties: Mapping[str, Any] | None = None, origin: Mapping[str, Any] | None = None,
             extra: Mapping[str, Any] | None = None) -> Item:
    """A ``forecast`` item: a ``probability`` (0-1) and/or ``quantiles`` for a ``question`` (the
    provider's) or a ``question_ref`` (a Foresignal question id).

    :raises ValueError: without a question or a value, with a probability outside 0-1, or with
        quantiles missing a required name
    """
    if question is None and not question_ref:
        raise ValueError("A forecast needs a question or a question_ref")
    if probability is None and quantiles is None:
        raise ValueError("A forecast needs a probability or quantiles")
    if probability is not None and not 0 <= probability <= 1:
        raise ValueError("probability must be between 0 and 1")
    if quantiles is not None and any(name not in quantiles for name in REQUIRED_QUANTILES):
        raise ValueError("quantiles need p05, p25, p50, p75 and p95")
    item = _common(stream, "forecast", item_id, observed_at, properties, origin)
    item["madeAt"] = _time(made_at)
    return _finish(item, {
        "question": dict(question) if question is not None else None, "questionRef": question_ref,
        "probability": probability, "quantiles": dict(quantiles) if quantiles is not None else None,
    }, extra)


def delete(stream: str, item_id: str, *, observed_at: TimeLike | None = None,
           properties: Mapping[str, Any] | None = None, extra: Mapping[str, Any] | None = None) -> Item:
    """A ``delete`` item: a tombstone for ``{stream}:{id}``."""
    return _finish(_common(stream, "delete", item_id, observed_at, properties, None), {}, extra)


def status(health: Literal["ok", "degraded", "blocked", "layout-changed", "geo-restricted"] | str,
           message: str | None = None, *, stream: str | None = None,
           extra: Mapping[str, Any] | None = None) -> Item:
    """A ``status`` item: the source's health, or a stream's when ``stream`` is given (no record).

    :raises ValueError: for an unknown health value
    """
    if health not in HEALTH_VALUES:
        raise ValueError(f"health must be one of {', '.join(HEALTH_VALUES)}")
    item = _common(stream, "status", None, None, None, None)
    item["health"] = health
    return _finish(item, {"message": message}, extra)


def new_snapshot_id() -> str:
    """A fresh snapshot id."""
    return uuid.uuid4().hex


def snapshot(snapshot_id: str, stream: str, *, page: int, final: bool, scope: str | None = None) -> Snapshot:
    """A batch ``snapshot`` object; pages share ``snapshot_id``, ``page`` counts from 1, and the last
    page has ``final: True``."""
    if page < 1:
        raise ValueError("Snapshot pages count from 1")
    return {"id": snapshot_id, "stream": stream, "scope": scope, "page": page, "final": final}


def batch(items: Sequence[Item], *, protocol: str = PROTOCOL_VERSION, source_id: str | None = None,
          sent_at: TimeLike | None = None, snapshot: Mapping[str, Any] | None = None,
          next_cursor: str | None = None, include_next_cursor: bool = False) -> dict[str, Any]:
    """A batch envelope. ``sent_at`` defaults to now; ``nextCursor`` is written when
    ``include_next_cursor`` is true (pull responses always carry it, ``None`` when caught up)."""
    out: dict[str, Any] = {"protocol": protocol}
    if source_id is not None:
        out["sourceId"] = source_id
    out["sentAt"] = _time(sent_at if sent_at is not None else utc_now())
    if snapshot is not None:
        out["snapshot"] = dict(snapshot)
    out["items"] = list(items)
    if include_next_cursor or next_cursor is not None:
        out["nextCursor"] = next_cursor
    return out
