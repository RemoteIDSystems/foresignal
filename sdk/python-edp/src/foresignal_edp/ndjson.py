"""Batch encoding: one JSON object, or NDJSON (one item per line, optionally after a header line).

The NDJSON header is the batch without ``items``: an object with ``protocol`` and no ``type``
(EDP §4.1). Encoding is compact UTF-8, so the byte counts the push client computes are exact.
"""

from __future__ import annotations

import json
from typing import Any, Iterable, Mapping

from .errors import EdpError
from .protocol import MAX_DEPTH, MAX_ITEMS


class BatchFormatError(EdpError):
    """A body is not a readable batch (malformed JSON, too deep, not an object, too many items)."""


def dumps(value: Any) -> bytes:
    """Compact UTF-8 JSON."""
    return json.dumps(value, separators=(",", ":"), ensure_ascii=False, allow_nan=False).encode("utf-8")


def encode_json(batch: Mapping[str, Any]) -> bytes:
    """Encodes a batch as one JSON object."""
    return dumps(batch)


def encode_ndjson(header: Mapping[str, Any] | None, items: Iterable[Mapping[str, Any]]) -> bytes:
    """Encodes a header line (without ``items``; omitted when ``header`` is ``None``) and one line per item."""
    lines = []
    if header is not None:
        lines.append(dumps({k: v for k, v in header.items() if k != "items"}))
    lines.extend(dumps(item) for item in items)
    return b"\n".join(lines) + b"\n"


def _depth(value: Any, depth: int = 1) -> int:
    if isinstance(value, dict):
        return max((_depth(v, depth + 1) for v in value.values()), default=depth)
    if isinstance(value, list):
        return max((_depth(v, depth + 1) for v in value), default=depth)
    return depth - 1


def _parse(text: str, what: str) -> Any:
    try:
        value = json.loads(text)
    except (ValueError, RecursionError) as e:
        raise BatchFormatError(f"{what} is not valid JSON: {e}") from e
    if _depth(value) > MAX_DEPTH:
        raise BatchFormatError(f"{what} nests deeper than {MAX_DEPTH} levels")
    return value


def decode_json(body: bytes | str) -> dict[str, Any]:
    """Decodes a JSON batch; ``items`` defaults to an empty list.

    :raises BatchFormatError: for malformed JSON, nesting deeper than 32, a non-object, a non-array
        ``items`` or more than 10,000 items
    """
    text = body.decode("utf-8") if isinstance(body, bytes) else body
    root = _parse(text, "The batch")
    if not isinstance(root, dict):
        raise BatchFormatError("The batch must be a JSON object")
    items = root.get("items")
    if items is not None and not isinstance(items, list):
        raise BatchFormatError("The batch's items must be an array")
    root["items"] = items or []
    if len(root["items"]) > MAX_ITEMS:
        raise BatchFormatError(f"The batch has {len(root['items'])} items; the maximum is {MAX_ITEMS}")
    return root


def decode_ndjson(body: bytes | str) -> dict[str, Any]:
    """Decodes NDJSON into a batch: the first line is the header when it has ``protocol`` and no ``type``.

    :raises BatchFormatError: as :func:`decode_json`, per line
    """
    text = body.decode("utf-8") if isinstance(body, bytes) else body
    header: dict[str, Any] = {}
    items: list[Any] = []
    first = True
    for number, line in enumerate(text.splitlines(), start=1):
        if not line.strip():
            continue
        node = _parse(line, f"Line {number}")
        if not isinstance(node, dict):
            raise BatchFormatError(f"Line {number} must be a JSON object")
        if first and "protocol" in node and "type" not in node:
            header = {k: v for k, v in node.items() if k != "items"}
        else:
            items.append(node)
            if len(items) > MAX_ITEMS:
                raise BatchFormatError(f"The batch has more than {MAX_ITEMS} items")
        first = False
    header["items"] = items
    return header


def decode_batch(body: bytes | str, content_type: str | None = None) -> dict[str, Any]:
    """Decodes a batch, as NDJSON when ``content_type`` is ``application/x-ndjson``, else as JSON."""
    if content_type and content_type.split(";")[0].strip().lower() == "application/x-ndjson":
        return decode_ndjson(body)
    return decode_json(body)
