"""Workspace contexts (docs/Client-Contract.md section 5, schema version 1): JSON merge patches (RFC 7396) and
the client-side checks that mirror the server's validation."""
from __future__ import annotations

import copy
import json
import re
from datetime import datetime
from typing import Any

from ..errors import ContextValidationError
from .object_ref import is_valid_object_ref

#: The context members of schema version 1.
CONTEXT_MEMBERS = ("selectedRefs", "focusRef", "timeRange", "asOf", "playback", "viewport", "activeSituationId",
                   "activeHypothesisSetId", "filters", "layers")
#: Most selected references.
MAX_SELECTED_REFS = 200
#: Most layers.
MAX_LAYERS = 200
#: Largest serialized context, in bytes.
MAX_CONTEXT_BYTES = 16_384
#: Largest serialized filters object, in bytes.
MAX_FILTERS_BYTES = 4096

_ULID = re.compile(r"^[0-9A-HJKMNP-TV-Z]{26}$")
_TIME = re.compile(r"^\d{4}-\d{2}-\d{2}T")


def apply_merge_patch(target: Any, patch: Any) -> Any:
    """Applies a JSON merge patch (RFC 7396) and returns the result; neither argument is modified.

    Objects merge member by member, ``None`` removes a member, anything else (lists included) replaces.
    """
    if not isinstance(patch, dict):
        return copy.deepcopy(patch)
    out = copy.deepcopy(target) if isinstance(target, dict) else {}
    for key, value in patch.items():
        if value is None:
            out.pop(key, None)
        else:
            out[key] = apply_merge_patch(out.get(key), value)
    return out


def strip_null_members(context: dict[str, Any]) -> dict[str, Any]:
    """A copy of ``context`` without its top-level ``None`` members (what the server stores)."""
    return {k: v for k, v in context.items() if v is not None}


def _compact(value: Any) -> str:
    return json.dumps(value, separators=(",", ":"), ensure_ascii=False)


def _parse_time(value: Any) -> float | None:
    if not isinstance(value, str) or not _TIME.match(value):
        return None
    try:
        return datetime.fromisoformat(value.replace("Z", "+00:00")).timestamp()
    except ValueError:
        return None


def _number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool)


def _simple(value: Any) -> bool:
    def scalar(v: Any) -> bool:
        return v is None or isinstance(v, (str, int, float, bool))

    return scalar(value) or (isinstance(value, list) and all(scalar(v) for v in value))


def context_problems(context: Any, prefix: str = "context") -> list[dict[str, str]]:
    """The problems of a context under schema version 1 (empty when valid), as ``{"field", "message"}``."""
    errors: list[dict[str, str]] = []

    def add(field: str, message: str) -> None:
        errors.append({"field": field, "message": message})

    if context is None:
        return errors
    if not isinstance(context, dict):
        return [{"field": prefix, "message": "must be an object"}]
    p = prefix + "."
    for name in context:
        if name not in CONTEXT_MEMBERS:
            add(p + name, "is not a workspace context member")
    selected = context.get("selectedRefs")
    if selected is not None:
        if not isinstance(selected, list):
            add(p + "selectedRefs", "must be an array of references")
        else:
            if len(selected) > MAX_SELECTED_REFS:
                add(p + "selectedRefs", f"holds more than {MAX_SELECTED_REFS} references")
            for i, ref in enumerate(selected):
                if not isinstance(ref, str) or not is_valid_object_ref(ref):
                    add(f"{p}selectedRefs[{i}]", "is not a valid object reference")
    focus = context.get("focusRef")
    if focus is not None and (not isinstance(focus, str) or not is_valid_object_ref(focus)):
        add(p + "focusRef", "is not a valid object reference")
    time_range = context.get("timeRange")
    if time_range is not None:
        if not isinstance(time_range, dict):
            add(p + "timeRange", "must be an object {start, end}")
        else:
            start = _parse_time(time_range.get("start"))
            if start is None:
                add(p + "timeRange.start", "is required (an RFC 3339 date-time)")
            end_value = time_range.get("end")
            if end_value is not None:
                end = _parse_time(end_value)
                if end is None:
                    add(p + "timeRange.end", "must be an RFC 3339 date-time or null")
                elif start is not None and end < start:
                    add(p + "timeRange.end", "must not be before start")
            for name in time_range:
                if name not in ("start", "end"):
                    add(f"{p}timeRange.{name}", "is not a time range member")
    as_of = context.get("asOf")
    if as_of is not None and _parse_time(as_of) is None:
        add(p + "asOf", "must be an RFC 3339 date-time")
    playback = context.get("playback")
    if playback is not None:
        if not isinstance(playback, dict):
            add(p + "playback", "must be an object {state, rate}")
        else:
            if playback.get("state") not in ("playing", "paused"):
                add(p + "playback.state", "must be playing or paused")
            rate = playback.get("rate")
            if rate is not None and (not _number(rate) or rate <= 0):
                add(p + "playback.rate", "must be a number above 0")
    viewport = context.get("viewport")
    if viewport is not None:
        if not isinstance(viewport, dict):
            add(p + "viewport", "must be an object")
        else:
            has_bbox = viewport.get("bbox") is not None
            has_center = viewport.get("center") is not None or viewport.get("radiusKm") is not None
            if has_bbox == has_center:
                add(p + "viewport", "must be either bbox or center with radiusKm")
            elif has_bbox:
                b = viewport["bbox"]
                if (not isinstance(b, list) or len(b) != 4 or not all(_number(n) for n in b) or b[1] >= b[3]
                        or abs(b[0]) > 180 or abs(b[2]) > 180 or abs(b[1]) > 90 or abs(b[3]) > 90):
                    add(p + "viewport.bbox", "must be [minLon, minLat, maxLon, maxLat]")
            else:
                c = viewport.get("center")
                if (not isinstance(c, list) or len(c) != 2 or not all(_number(n) for n in c)
                        or abs(c[0]) > 180 or abs(c[1]) > 90):
                    add(p + "viewport.center", "must be [lon, lat]")
                r = viewport.get("radiusKm")
                if not _number(r) or r <= 0 or r > 20_000:
                    add(p + "viewport.radiusKm", "must be above 0 and at most 20000")
    for ident in ("activeSituationId", "activeHypothesisSetId"):
        v = context.get(ident)
        if v is not None and (not isinstance(v, str) or not _ULID.match(v)):
            add(p + ident, "must be a ULID or null")
    filters = context.get("filters")
    if filters is not None:
        if not isinstance(filters, dict):
            add(p + "filters", "must be an object")
        else:
            for name, value in filters.items():
                if not _simple(value):
                    add(f"{p}filters.{name}", "must be a string, number, boolean, null or an array of those")
            if len(_compact(filters).encode("utf-8")) > MAX_FILTERS_BYTES:
                add(p + "filters", f"is larger than {MAX_FILTERS_BYTES} bytes")
    layers = context.get("layers")
    if layers is not None:
        if not isinstance(layers, list) or len(layers) > MAX_LAYERS:
            add(p + "layers", f"must be an array of at most {MAX_LAYERS} strings")
        elif any(not isinstance(x, str) or not x.strip() or len(x) > 128 for x in layers):
            add(p + "layers", "each layer is a string of 1 to 128 characters")
    if len(_compact(context).encode("utf-8")) > MAX_CONTEXT_BYTES:
        add(prefix, f"is larger than {MAX_CONTEXT_BYTES} bytes")
    return errors


def validate_context(context: Any, prefix: str = "context") -> None:
    """Validates a context under schema version 1.

    :raises ContextValidationError: listing every problem
    """
    errors = context_problems(context, prefix)
    if errors:
        raise ContextValidationError(errors)
