"""Batched envelope resolution (``POST /v1/objects/resolve``, at most 500 refs per call).

Missing and invisible references come back in ``missing``, not as errors (docs/Client-Contract.md section 8).
"""
from __future__ import annotations

from collections.abc import Iterable, Sequence
from typing import Any

from ..http import HttpTransport
from .object_ref import parse_object_ref

#: Most references per resolve call.
MAX_RESOLVE_BATCH = 500


def batch_refs(refs: Sequence[str], size: int = MAX_RESOLVE_BATCH) -> list[list[str]]:
    """Splits references into batches of at most ``size``."""
    return [list(refs[i:i + size]) for i in range(0, len(refs), size)]


def resolve_objects(transport: HttpTransport, refs: Iterable[str], *, as_of: str | None = None,
                    batch_size: int = MAX_RESOLVE_BATCH, validate: bool = True) -> dict[str, Any]:
    """Resolves any number of references in batches of at most 500, keeping the first occurrence of duplicates
    and the input order. Returns ``{"items": [...], "missing": [...], "asOf": …}``.

    :raises ObjectRefError: for a malformed reference (when ``validate`` is on)
    :raises ApiError: when a call fails
    """
    size = max(1, min(MAX_RESOLVE_BATCH, batch_size))
    unique: list[str] = []
    seen: set[str] = set()
    for ref in refs:
        if validate:
            parse_object_ref(ref)
        if ref not in seen:
            seen.add(ref)
            unique.append(ref)
    items: list[Any] = []
    missing: list[str] = []
    result_as_of = as_of
    for batch in batch_refs(unique, size):
        body: dict[str, Any] = {"refs": batch}
        if as_of is not None:
            body["asOf"] = as_of
        data, _ = transport.post_json("/v1/objects/resolve", body)
        data = data or {}
        items.extend(data.get("items") or [])
        missing.extend(data.get("missing") or [])
        result_as_of = data.get("asOf", result_as_of)
    out: dict[str, Any] = {"items": items, "missing": missing}
    if result_as_of is not None:
        out["asOf"] = result_as_of
    return out
