"""Push client: ``POST /v1/external/sources/{sourceId}/items`` with the source's ingest key (EDP §4.1).

- **Batching:** items are split into batches of at most 10,000 items and 5 MB (both configurable
  downward), counted on the exact bytes sent.
- **NDJSON:** with ``ndjson=True`` each batch is a header line plus one line per item
  (``application/x-ndjson``).
- **Retries:** ``429`` and ``503`` (and network failures) are retried with exponential backoff and
  jitter (1 s doubling to 60 s by default), honoring ``Retry-After`` (seconds or an HTTP date).
- **Idempotent re-sends:** every batch is serialized once, with a fixed ``sentAt``, and every retry
  sends exactly the same bytes. Foresignal hashes item content, so a repeated item becomes a touch,
  never a duplicate. A failed push raises :class:`PushError` carrying the batches not yet accepted;
  :meth:`PushClient.send_all` re-sends them unchanged later.
- **Snapshots:** :meth:`PushClient.push_snapshot` pages a full snapshot of one scope with a shared
  snapshot id, ``page`` from 1 and ``final: true`` on the last page (EDP §3.2).
- **Status:** :meth:`PushClient.report_status` sends source or stream health.
"""

from __future__ import annotations

import email.utils
import json
import random as _random
import time
import urllib.error
import urllib.request
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any, Callable, Iterable, Mapping, Protocol, Sequence
from urllib.parse import quote

from ._version import __version__
from .errors import ItemRejection, ItemTooLargeError, PushError
from .items import new_snapshot_id
from .items import status as status_item
from .ndjson import dumps
from .protocol import (
    INGEST_KEY_HEADER,
    JSON_MEDIA_TYPE,
    MAX_BATCH_BYTES,
    MAX_ITEMS,
    NDJSON_MEDIA_TYPE,
    PROTOCOL_VERSION,
)
from .times import format_time, utc_now

USER_AGENT = f"foresignal-edp-python/{__version__}"


@dataclass(frozen=True)
class TransportResponse:
    """An HTTP response: status, headers (keys lowercased) and body."""

    status: int
    headers: Mapping[str, str]
    body: bytes


class TransportError(Exception):
    """The request did not produce an HTTP response (connection refused, reset, timeout, DNS)."""


class Transport(Protocol):
    """Sends one HTTP request. Implement it to use another HTTP library or to test."""

    def request(self, method: str, url: str, headers: Mapping[str, str], body: bytes | None,
                timeout: float) -> TransportResponse:
        """Sends the request; returns any HTTP response (including 4xx and 5xx) or raises
        :class:`TransportError` when no response arrived."""
        ...


class UrllibTransport:
    """The default transport, on the standard library's ``urllib``."""

    def request(self, method: str, url: str, headers: Mapping[str, str], body: bytes | None,
                timeout: float) -> TransportResponse:
        """Sends the request with ``urllib.request``."""
        req = urllib.request.Request(url, data=body, method=method, headers=dict(headers))
        try:
            with urllib.request.urlopen(req, timeout=timeout) as resp:  # noqa: S310 (caller chooses the URL)
                return TransportResponse(resp.status, {k.lower(): v for k, v in resp.headers.items()}, resp.read())
        except urllib.error.HTTPError as e:
            return TransportResponse(e.code, {k.lower(): v for k, v in (e.headers or {}).items()}, e.read() or b"")
        except (urllib.error.URLError, OSError, TimeoutError) as e:
            raise TransportError(str(e)) from e


@dataclass(frozen=True)
class PreparedBatch:
    """One serialized batch, ready to send and re-send byte for byte.

    ``first_index`` is the position of its first item in the ``push`` call, used to map the server's
    per-batch rejection indexes back to the caller's item indexes.
    """

    body: bytes
    content_type: str
    item_count: int
    first_index: int = 0
    snapshot: Mapping[str, Any] | None = None

    @property
    def size(self) -> int:
        """The body size in bytes."""
        return len(self.body)


@dataclass
class PushResult:
    """The merged outcome of one or more batches.

    ``accepted`` counts accepted items, ``rejected`` lists refused items with indexes into the
    pushed sequence, and ``responses`` holds each batch's parsed response body (``EdpIngestResult``).
    """

    accepted: int = 0
    rejected: list[ItemRejection] = field(default_factory=list)
    batches: int = 0
    responses: list[dict[str, Any]] = field(default_factory=list)

    @property
    def ok(self) -> bool:
        """True when no item was refused."""
        return not self.rejected

    def add(self, response: Mapping[str, Any], first_index: int) -> None:
        """Merges one batch response."""
        self.batches += 1
        self.responses.append(dict(response))
        accepted = response.get("accepted")
        if isinstance(accepted, int) and not isinstance(accepted, bool):
            self.accepted += accepted
        for rejection in response.get("rejected") or []:
            if isinstance(rejection, Mapping):
                self.rejected.append(ItemRejection.from_dict(rejection, first_index))


def parse_retry_after(value: str | None, now: datetime | None = None) -> float | None:
    """Reads ``Retry-After``: delta seconds or an HTTP date. Returns seconds (never negative) or ``None``."""
    if value is None or not value.strip():
        return None
    text = value.strip()
    try:
        seconds = float(text)
        return max(0.0, seconds) if seconds == seconds else None  # NaN check
    except ValueError:
        pass
    try:
        when = email.utils.parsedate_to_datetime(text)
    except (TypeError, ValueError, IndexError):
        return None
    if when is None:
        return None
    if when.tzinfo is None:
        when = when.replace(tzinfo=timezone.utc)
    current = now or datetime.now(timezone.utc)
    return max(0.0, (when - current).total_seconds())


def _problem(body: bytes) -> dict[str, Any] | None:
    if not body:
        return None
    try:
        value = json.loads(body.decode("utf-8"))
    except (ValueError, UnicodeDecodeError):
        return None
    return value if isinstance(value, dict) else None


class PushClient:
    """Pushes EDP items to one registered source.

    :param base_url: the Foresignal instance, e.g. ``https://foresignal.example``
    :param source_id: the registered source id, ``ext-{source.id}``
    :param ingest_key: the source's ingest key (``eik_…``), sent as ``X-Foresignal-Ingest-Key``
    :param ndjson: send ``application/x-ndjson`` instead of one JSON object
    :param max_items: items per batch (at most 10,000)
    :param max_bytes: bytes per batch (at most 5 MB)
    :param max_retries: retries of one batch after a 429, a 503 or a network failure
    :param initial_backoff: the first backoff in seconds (doubles per retry)
    :param max_backoff: the backoff ceiling in seconds
    :param max_retry_after: the longest ``Retry-After`` honored, in seconds; a longer one fails the push
        as retryable instead of blocking
    :param timeout: the per-request timeout in seconds
    :param include_source_id: write ``sourceId`` into each batch (the server checks it matches)
    :param transport: the HTTP transport (default :class:`UrllibTransport`)
    :param sleep: the sleep function (injectable for tests)
    :param random: a ``[0, 1)`` random source for jitter (injectable for tests)
    """

    def __init__(self, base_url: str, source_id: str, ingest_key: str, *, ndjson: bool = False,
                 max_items: int = MAX_ITEMS, max_bytes: int = MAX_BATCH_BYTES, max_retries: int = 5,
                 initial_backoff: float = 1.0, max_backoff: float = 60.0, max_retry_after: float = 900.0,
                 timeout: float = 30.0, include_source_id: bool = True, protocol: str = PROTOCOL_VERSION,
                 retry_statuses: Iterable[int] = (429, 503), transport: Transport | None = None,
                 sleep: Callable[[float], None] = time.sleep, random: Callable[[], float] = _random.random,
                 clock: Callable[[], datetime] = utc_now) -> None:
        if not 1 <= max_items <= MAX_ITEMS:
            raise ValueError(f"max_items must be 1 to {MAX_ITEMS}")
        if not 1 <= max_bytes <= MAX_BATCH_BYTES:
            raise ValueError(f"max_bytes must be 1 to {MAX_BATCH_BYTES}")
        if max_retries < 0:
            raise ValueError("max_retries must not be negative")
        if not source_id:
            raise ValueError("source_id is required (the registered id, ext-{source.id})")
        self.base_url = base_url.rstrip("/")
        self.source_id = source_id
        self._ingest_key = ingest_key
        self.ndjson = ndjson
        self.max_items = max_items
        self.max_bytes = max_bytes
        self.max_retries = max_retries
        self.initial_backoff = initial_backoff
        self.max_backoff = max_backoff
        self.max_retry_after = max_retry_after
        self.timeout = timeout
        self.include_source_id = include_source_id
        self.protocol = protocol
        self.retry_statuses = frozenset(retry_statuses)
        self.transport: Transport = transport or UrllibTransport()
        self._sleep = sleep
        self._random = random
        self._clock = clock

    def __repr__(self) -> str:
        return f"PushClient(base_url={self.base_url!r}, source_id={self.source_id!r}, ingest_key=***)"

    @property
    def items_url(self) -> str:
        """``{base_url}/v1/external/sources/{source_id}/items``."""
        return f"{self.base_url}/v1/external/sources/{quote(self.source_id, safe='')}/items"

    @property
    def manifest_url(self) -> str:
        """``{base_url}/v1/external/sources/{source_id}/manifest``."""
        return f"{self.base_url}/v1/external/sources/{quote(self.source_id, safe='')}/manifest"

    # ---------------------------------------------------------------- preparing

    def _header(self, sent_at: str, snapshot: Mapping[str, Any] | None) -> dict[str, Any]:
        header: dict[str, Any] = {"protocol": self.protocol}
        if self.include_source_id:
            header["sourceId"] = self.source_id
        header["sentAt"] = sent_at
        if snapshot is not None:
            header["snapshot"] = dict(snapshot)
        return header

    def _encode(self, header: Mapping[str, Any], encoded_items: Sequence[bytes]) -> bytes:
        if self.ndjson:
            return b"\n".join([dumps(header), *encoded_items]) + b"\n"
        head = dumps({**header, "items": []})
        return head[:-2] + b",".join(encoded_items) + b"]}"

    def _overhead(self, header: Mapping[str, Any]) -> int:
        # Bytes of a batch with no items; each item then adds its length plus one separator.
        return len(self._encode(header, [])) + (0 if self.ndjson else -1)

    def prepare(self, items: Sequence[Mapping[str, Any]], *, snapshot: Mapping[str, Any] | None = None,
                sent_at: datetime | str | None = None) -> list[PreparedBatch]:
        """Splits ``items`` into serialized batches within the item and byte limits.

        ``snapshot`` (``{"id", "stream", "scope"}``) makes the batches the pages of one snapshot: each
        gets ``page`` from 1 and the last ``final: true``; an empty snapshot is one empty final page.

        :raises ItemTooLargeError: when one item alone does not fit in ``max_bytes``
        """
        stamp = format_time(sent_at) if sent_at is not None else format_time(self._clock())
        assert stamp is not None
        placeholder = None
        if snapshot is not None:
            placeholder = {"id": snapshot["id"], "stream": snapshot["stream"], "scope": snapshot.get("scope"),
                           "page": 10 ** 9, "final": False}
        overhead = self._overhead(self._header(stamp, placeholder))
        groups: list[tuple[int, list[bytes]]] = []
        current: list[bytes] = []
        current_size = overhead
        start = 0
        for index, item in enumerate(items):
            encoded = dumps(item)
            needed = len(encoded) + 1
            if overhead + needed > self.max_bytes:
                raise ItemTooLargeError(index, overhead + needed, self.max_bytes)
            if current and (len(current) >= self.max_items or current_size + needed > self.max_bytes):
                groups.append((start, current))
                start, current, current_size = index, [], overhead
            current.append(encoded)
            current_size += needed
        if current or not groups:
            groups.append((start, current))
        if snapshot is None and len(groups) == 1 and not groups[0][1]:
            return []
        content_type = NDJSON_MEDIA_TYPE if self.ndjson else JSON_MEDIA_TYPE
        prepared = []
        for page, (first, encoded_items) in enumerate(groups, start=1):
            page_snapshot = None
            if snapshot is not None:
                page_snapshot = {"id": snapshot["id"], "stream": snapshot["stream"], "scope": snapshot.get("scope"),
                                 "page": page, "final": page == len(groups)}
            body = self._encode(self._header(stamp, page_snapshot), encoded_items)
            prepared.append(PreparedBatch(body, content_type, len(encoded_items), first, page_snapshot))
        return prepared

    # ---------------------------------------------------------------- sending

    def _headers(self, content_type: str) -> dict[str, str]:
        return {"Content-Type": content_type, "Accept": "application/json, application/problem+json",
                INGEST_KEY_HEADER: self._ingest_key, "User-Agent": USER_AGENT}

    def backoff(self, attempt: int) -> float:
        """The delay before retry ``attempt`` (0-based) without ``Retry-After``: exponential with jitter,
        between half and all of ``min(max_backoff, initial_backoff * 2**attempt)``."""
        ceiling = min(self.max_backoff, self.initial_backoff * (2.0 ** attempt))
        return ceiling / 2 + self._random() * ceiling / 2

    def _request(self, method: str, url: str, body: bytes, content_type: str) -> dict[str, Any]:
        attempt = 0
        while True:
            try:
                response = self.transport.request(method, url, self._headers(content_type), body, self.timeout)
            except TransportError as e:
                if attempt >= self.max_retries:
                    raise PushError(f"Network failure after {attempt + 1} attempts: {e}", retryable=True) from e
                delay = self.backoff(attempt)
            else:
                if 200 <= response.status < 300:
                    parsed = _problem(response.body)
                    return parsed if parsed is not None else {}
                problem = _problem(response.body)
                code = problem.get("code") if problem else None
                retryable = response.status in self.retry_statuses
                if not retryable or attempt >= self.max_retries:
                    detail = (problem or {}).get("detail") or (problem or {}).get("title") or ""
                    raise PushError(
                        f"{method} {url} answered {response.status}{' ' + code if code else ''}"
                        f"{': ' + detail if detail else ''}",
                        status=response.status, code=code, problem=problem, body=response.body, retryable=retryable)
                retry_after = parse_retry_after(response.headers.get("retry-after"))
                if retry_after is not None and retry_after > self.max_retry_after:
                    raise PushError(f"{method} {url} answered {response.status} with Retry-After {retry_after:.0f} s, "
                                    f"longer than max_retry_after", status=response.status, code=code,
                                    problem=problem, body=response.body, retryable=True)
                delay = retry_after if retry_after is not None else self.backoff(attempt)
            self._sleep(delay)
            attempt += 1

    def send(self, prepared: PreparedBatch) -> dict[str, Any]:
        """Sends one prepared batch (retrying as configured) and returns the parsed response.

        :raises PushError: when the batch is refused or the retries are spent
        """
        return self._request("POST", self.items_url, prepared.body, prepared.content_type)

    def send_all(self, prepared: Sequence[PreparedBatch]) -> PushResult:
        """Sends prepared batches in order; use it to re-send ``PushError.remaining`` unchanged.

        :raises PushError: with ``result`` (what was accepted) and ``remaining`` (the failed batch first)
        """
        result = PushResult()
        for i, batch in enumerate(prepared):
            try:
                response = self.send(batch)
            except PushError as e:
                e.result = result
                e.remaining = list(prepared[i:])
                raise
            result.add(response, batch.first_index)
        return result

    def push(self, items: Sequence[Mapping[str, Any]]) -> PushResult:
        """Batches and pushes ``items``; rejection indexes refer to positions in ``items``.

        :raises PushError: see :meth:`send_all`
        :raises ItemTooLargeError: when one item alone exceeds ``max_bytes``
        """
        return self.send_all(self.prepare(list(items)))

    def push_snapshot(self, stream: str, items: Sequence[Mapping[str, Any]], *, scope: str | None = None,
                      snapshot_id: str | None = None) -> PushResult:
        """Pushes the full current set of one scope of a snapshot stream as the pages of one snapshot.

        Every page must arrive for anything to be removed; if a page fails, the snapshot is discarded
        by Foresignal and nothing is deleted, so a failed snapshot can simply be pushed again (with a
        new id).
        """
        sid = snapshot_id or new_snapshot_id()
        return self.send_all(self.prepare(list(items), snapshot={"id": sid, "stream": stream, "scope": scope}))

    def report_status(self, health: str, message: str | None = None, *, stream: str | None = None) -> PushResult:
        """Reports the source's health (or one stream's) with a ``status`` item. Report breakage this
        way rather than sending empty data."""
        return self.push([status_item(health, message, stream=stream)])

    def update_manifest(self, manifest: Mapping[str, Any]) -> dict[str, Any]:
        """``PUT /v1/external/sources/{id}/manifest`` with the ingest key. Additive changes apply at once;
        changes to streams, kinds or schemas return the source to ``pending`` until approved."""
        return self._request("PUT", self.manifest_url, dumps(manifest), JSON_MEDIA_TYPE)
