"""Pull-server helper: a framework-neutral handler for ``GET {itemsUrl}?cursor=&limit=`` and the
optional ``GET {searchUrl}?q=&since=&until=&bbox=&lang=&limit=`` (EDP §4.2, §4.3).

The handler takes a query mapping and request headers and returns an :class:`HttpResponse`, so it
fits any framework; :func:`wsgi_app` adapts it to WSGI (Flask, Django, Bottle, ``wsgiref``).

**Cursor semantics.** Foresignal follows ``nextCursor`` until it is ``null``, storing the cursor
after each page, and the *next* run starts again from the last cursor it sent. So a cursor must stay
valid and replayable: asking for it again returns the items from that position onward, including
items added since. Positions are whatever your :class:`ItemSource` understands (an offset, a
timestamp, a key); a :class:`CursorStore` turns them into opaque strings and back.
:class:`OpaqueCursorStore` (the default) is stateless and survives restarts; :class:`MemoryCursorStore`
keeps short random tokens in memory, so use it only for providers that may restart from scratch.

**Pages** never exceed 5 MB: when a page would, the handler asks the source again with half the
limit. Changed health on a :class:`~foresignal_edp.health.StatusBoard` is delivered at the front of
the next page.
"""

from __future__ import annotations

import base64
import binascii
import hashlib
import hmac
import json
import logging
import threading
import time
from collections import OrderedDict
from dataclasses import dataclass, field
from datetime import datetime
from http import HTTPStatus
from typing import Any, Callable, Iterable, Mapping, Protocol, Sequence
from urllib.parse import parse_qs, urlsplit

from .errors import CursorError
from .health import StatusBoard
from .items import batch as make_batch
from .items import new_snapshot_id
from .ndjson import dumps
from .protocol import (
    DEFAULT_PULL_LIMIT,
    DEFAULT_SEARCH_LIMIT,
    JSON_MEDIA_TYPE,
    MAX_BATCH_BYTES,
    MAX_ITEMS,
    PROTOCOL_VERSION,
    WELL_KNOWN_MANIFEST_PATH,
)
from .times import parse_rfc3339, utc_now

log = logging.getLogger(__name__)

Query = Mapping[str, "str | Sequence[str]"]
Headers = Mapping[str, str]
Authorizer = Callable[[Headers], bool]


@dataclass(frozen=True)
class HttpResponse:
    """A framework-neutral HTTP response."""

    status: int
    headers: dict[str, str]
    body: bytes

    def json(self) -> Any:
        """The body parsed as JSON."""
        return json.loads(self.body.decode("utf-8")) if self.body else None


@dataclass
class Page:
    """What an :class:`ItemSource` returns: the items, the position of the next page (``None`` when
    caught up, which makes ``nextCursor`` null) and an optional batch ``snapshot`` object."""

    items: list[dict[str, Any]]
    next_position: Any = None
    snapshot: Mapping[str, Any] | None = None


class ItemSource(Protocol):
    """Produces pages of items from a position (``None`` for the beginning)."""

    def fetch(self, position: Any, limit: int) -> Page:
        """Returns at most ``limit`` items starting at ``position``. Raise :class:`CursorError` for a
        position the source cannot serve. Must be repeatable: the same position may be asked for again."""
        ...


class CursorStore(Protocol):
    """Turns source positions into opaque cursor strings and back."""

    def encode(self, position: Any) -> str:
        """Returns the cursor of ``position`` (JSON-serializable)."""
        ...

    def decode(self, cursor: str) -> Any:
        """Returns the position of ``cursor``; raises :class:`CursorError` when it is unknown or invalid."""
        ...


def _b64encode(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).decode("ascii").rstrip("=")


def _b64decode(text: str) -> bytes:
    return base64.urlsafe_b64decode(text + "=" * (-len(text) % 4))


class OpaqueCursorStore:
    """A stateless store: the cursor is the position as base64url JSON, signed with HMAC-SHA256 when a
    ``secret`` is given (so a caller cannot forge positions). Survives restarts."""

    def __init__(self, secret: bytes | str | None = None) -> None:
        """Creates the store; ``secret`` enables signing."""
        self._secret = secret.encode("utf-8") if isinstance(secret, str) else secret

    def _sign(self, payload: str) -> str:
        assert self._secret is not None
        return _b64encode(hmac.new(self._secret, payload.encode("ascii"), hashlib.sha256).digest())

    def encode(self, position: Any) -> str:
        """Encodes ``position``."""
        payload = _b64encode(json.dumps(position, separators=(",", ":"), sort_keys=True).encode("utf-8"))
        return f"{payload}.{self._sign(payload)}" if self._secret else payload

    def decode(self, cursor: str) -> Any:
        """Decodes ``cursor``; raises :class:`CursorError` when malformed or wrongly signed."""
        payload = cursor
        if self._secret:
            payload, _, signature = cursor.partition(".")
            if not signature or not hmac.compare_digest(signature, self._sign(payload)):
                raise CursorError("The cursor's signature does not match")
        try:
            return json.loads(_b64decode(payload).decode("utf-8"))
        except (ValueError, binascii.Error, UnicodeDecodeError) as e:
            raise CursorError("The cursor is not valid") from e


class MemoryCursorStore:
    """Random tokens kept in memory; the same position always gets the same token. Holds at most
    ``max_entries`` positions, forgetting the least recently used. Tokens do not survive a restart."""

    def __init__(self, max_entries: int = 100_000) -> None:
        """Creates the store."""
        self._max = max_entries
        self._lock = threading.Lock()
        self._by_token: OrderedDict[str, str] = OrderedDict()
        self._by_position: dict[str, str] = {}

    def encode(self, position: Any) -> str:
        """Returns the token of ``position``, creating one when needed."""
        key = json.dumps(position, separators=(",", ":"), sort_keys=True)
        with self._lock:
            token = self._by_position.get(key)
            if token is None:
                token = _b64encode(hashlib.sha256(key.encode("utf-8") + time.time_ns().to_bytes(8, "big")).digest()[:15])
                self._by_position[key] = token
            self._by_token[token] = key
            self._by_token.move_to_end(token)
            while len(self._by_token) > self._max:
                old_token, old_key = self._by_token.popitem(last=False)
                self._by_position.pop(old_key, None)
            return token

    def decode(self, cursor: str) -> Any:
        """Returns the position of ``cursor``; raises :class:`CursorError` when unknown."""
        with self._lock:
            key = self._by_token.get(cursor)
            if key is None:
                raise CursorError("The cursor is unknown or has expired")
            self._by_token.move_to_end(cursor)
        return json.loads(key)


class SequenceSource:
    """Serves a sequence by offset, like the reference ``csv_provider.py``: a full page gets the next
    offset as its cursor, a short page ``null``; asking again for the last cursor returns that page's
    items plus anything appended since (repeats are idempotent on Foresignal's side)."""

    def __init__(self, load: Callable[[], Sequence[dict[str, Any]]]) -> None:
        """``load`` returns the current items, in a stable order where new items are appended."""
        self._load = load

    def fetch(self, position: Any, limit: int) -> Page:
        """Returns the items at offset ``position`` (0 when ``None``)."""
        offset = 0 if position is None else position
        if not isinstance(offset, int) or isinstance(offset, bool) or offset < 0:
            raise CursorError("The cursor does not name an offset")
        rows = self._load()
        page = [dict(r) for r in rows[offset:offset + limit]]
        return Page(page, offset + len(page) if len(page) == limit else None)


class SnapshotSource:
    """Pages a full snapshot of one scope of a snapshot stream (EDP §3.2).

    Each run without a cursor (or with the cursor of a finished snapshot) starts a new snapshot with
    a fresh id; pages are ordered by item id with keyset paging (an id added or removed mid-snapshot
    never shifts another one out), share the snapshot id, count ``page`` from 1, and the last one has
    ``final: true`` and a null ``nextCursor``. A cursor of a snapshot older than ``window_seconds``
    (default 25 minutes, under Foresignal's 30-minute ``snapshotWindow``) starts a new one, since the
    old one could no longer complete.
    """

    def __init__(self, stream: str, list_items: Callable[[], Iterable[dict[str, Any]]], *,
                 scope: str | None = None, window_seconds: float = 1500.0,
                 clock: Callable[[], float] = time.time, id_factory: Callable[[], str] = new_snapshot_id,
                 remember: int = 1024) -> None:
        """``list_items`` returns the scope's complete current set of items of ``stream``."""
        self.stream = stream
        self.scope = scope
        self._list = list_items
        self._window = window_seconds
        self._clock = clock
        self._new_id = id_factory
        self._lock = threading.Lock()
        self._finished: OrderedDict[str, None] = OrderedDict()
        self._remember = remember

    def _finish(self, snapshot_id: str) -> None:
        with self._lock:
            self._finished[snapshot_id] = None
            while len(self._finished) > self._remember:
                self._finished.popitem(last=False)

    def _is_finished(self, snapshot_id: str) -> bool:
        with self._lock:
            return snapshot_id in self._finished

    def fetch(self, position: Any, limit: int) -> Page:
        """Returns the next page of the current snapshot, or the first page of a new one."""
        now = self._clock()
        fresh = (
            not isinstance(position, Mapping)
            or not isinstance(position.get("s"), str)
            or self._is_finished(position["s"])
            or not isinstance(position.get("t"), (int, float))
            or now - position["t"] > self._window
        )
        if fresh:
            snapshot_id, page, after, started = self._new_id(), 1, None, now
        else:
            assert isinstance(position, Mapping)
            snapshot_id, page, after, started = position["s"], int(position.get("p", 1)), position.get("a"), position["t"]
        items = []
        for item in self._list():
            if item.get("stream") != self.stream:
                raise ValueError(f"SnapshotSource({self.stream!r}) got an item of stream {item.get('stream')!r}")
            items.append(dict(item))
        items.sort(key=lambda i: str(i.get("id")))
        remaining = [i for i in items if after is None or str(i.get("id")) > after]
        chunk = remaining[:limit]
        final = len(remaining) <= limit
        snapshot = {"id": snapshot_id, "stream": self.stream, "scope": self.scope, "page": page, "final": final}
        if final:
            self._finish(snapshot_id)
            return Page(chunk, None, snapshot)
        return Page(chunk, {"s": snapshot_id, "p": page + 1, "a": str(chunk[-1].get("id")), "t": started}, snapshot)


@dataclass(frozen=True)
class SearchQuery:
    """A parsed search request: ``q``, optional ``since``/``until`` (aware UTC datetimes), ``bbox``
    (min lon, min lat, max lon, max lat), ``lang`` (BCP 47 tags) and ``limit``."""

    q: str
    since: datetime | None = None
    until: datetime | None = None
    bbox: tuple[float, float, float, float] | None = None
    lang: list[str] = field(default_factory=list)
    limit: int = DEFAULT_SEARCH_LIMIT


SearchFunction = Callable[[SearchQuery], "Sequence[dict[str, Any]] | Page"]


def _lower(headers: Headers | None) -> dict[str, str]:
    return {k.lower(): v for k, v in (headers or {}).items()}


def bearer_auth(token: str) -> Authorizer:
    """An authorizer for ``auth: bearer``: ``Authorization: Bearer {token}``."""
    expected = token.encode("utf-8")

    def check(headers: Headers) -> bool:
        value = _lower(headers).get("authorization", "")
        scheme, _, credential = value.partition(" ")
        return scheme.lower() == "bearer" and hmac.compare_digest(credential.strip().encode("utf-8"), expected)

    return check


def header_auth(name: str, value: str) -> Authorizer:
    """An authorizer for ``auth: header``: header ``name`` equal to ``value``."""
    key, expected = name.lower(), value.encode("utf-8")

    def check(headers: Headers) -> bool:
        return hmac.compare_digest(_lower(headers).get(key, "").encode("utf-8"), expected)

    return check


def basic_auth(username: str, password: str) -> Authorizer:
    """An authorizer for ``auth: basic``."""
    expected = f"{username}:{password}".encode("utf-8")

    def check(headers: Headers) -> bool:
        value = _lower(headers).get("authorization", "")
        scheme, _, credential = value.partition(" ")
        if scheme.lower() != "basic":
            return False
        try:
            decoded = base64.b64decode(credential.strip(), validate=True)
        except (binascii.Error, ValueError):
            return False
        return hmac.compare_digest(decoded, expected)

    return check


def _first(query: Query | None, name: str) -> str | None:
    if not query or name not in query:
        return None
    value = query[name]
    if isinstance(value, str):
        return value
    return value[0] if value else None


def _problem(status: int, code: str, detail: str, headers: dict[str, str] | None = None) -> HttpResponse:
    body = dumps({"type": "about:blank", "title": HTTPStatus(status).phrase, "status": status, "code": code,
                  "detail": detail})
    return HttpResponse(status, {"Content-Type": "application/problem+json", "Cache-Control": "no-store",
                                 **(headers or {})}, body)


def _json(status: int, value: Any) -> HttpResponse:
    return HttpResponse(status, {"Content-Type": JSON_MEDIA_TYPE, "Cache-Control": "no-store"}, dumps(value))


class PullHandler:
    """Serves an EDP items endpoint (and optionally search and the manifest).

    :param source: the :class:`ItemSource` of the items endpoint
    :param cursor_store: turns positions into cursors (default: an unsigned :class:`OpaqueCursorStore`)
    :param source_id: written as the batches' ``sourceId`` when given (the provider's ``source.id``)
    :param default_limit: page size when the request has no ``limit`` (Foresignal sends 500)
    :param max_limit: the largest page size honored (at most 10,000)
    :param max_bytes: the largest response body (at most 5 MB)
    :param search: a function answering search requests, or ``None`` (search answers 404)
    :param authorize: checks the request headers (see :func:`bearer_auth`, :func:`header_auth`,
        :func:`basic_auth`); ``None`` admits everyone
    :param status: a :class:`~foresignal_edp.health.StatusBoard` whose changes ride on the next page
    :param manifest: the manifest served at ``manifest_path`` (``/.well-known/foresignal-edp.json``)
    """

    def __init__(self, source: ItemSource, *, cursor_store: CursorStore | None = None,
                 source_id: str | None = None, protocol: str = PROTOCOL_VERSION,
                 default_limit: int = DEFAULT_PULL_LIMIT, max_limit: int = MAX_ITEMS,
                 max_bytes: int = MAX_BATCH_BYTES, search: SearchFunction | None = None,
                 authorize: Authorizer | None = None, status: StatusBoard | None = None,
                 manifest: Mapping[str, Any] | None = None, items_path: str = "/edp/items",
                 search_path: str = "/edp/search", manifest_path: str = WELL_KNOWN_MANIFEST_PATH,
                 clock: Callable[[], datetime] = utc_now) -> None:
        if not 1 <= max_limit <= MAX_ITEMS:
            raise ValueError(f"max_limit must be 1 to {MAX_ITEMS}")
        if not 1 <= max_bytes <= MAX_BATCH_BYTES:
            raise ValueError(f"max_bytes must be 1 to {MAX_BATCH_BYTES}")
        self.source = source
        self.cursor_store: CursorStore = cursor_store or OpaqueCursorStore()
        self.source_id = source_id
        self.protocol = protocol
        self.default_limit = min(default_limit, max_limit)
        self.max_limit = max_limit
        self.max_bytes = max_bytes
        self.search = search
        self.authorize = authorize
        self.status = status
        self.manifest = dict(manifest) if manifest is not None else None
        self.items_path = items_path
        self.search_path = search_path
        self.manifest_path = manifest_path
        self._clock = clock

    def _limit(self, query: Query | None, default: int) -> int | HttpResponse:
        raw = _first(query, "limit")
        if raw is None or raw == "":
            return default
        try:
            value = int(raw)
        except ValueError:
            return _problem(400, "LIMIT_INVALID", "limit must be an integer")
        return max(1, min(value, self.max_limit))

    def _unauthorized(self, headers: Headers | None) -> HttpResponse | None:
        if self.authorize is not None and not self.authorize(headers or {}):
            return _problem(401, "UNAUTHORIZED", "missing or invalid credentials")
        return None

    def _batch(self, items: list[dict[str, Any]], next_cursor: str | None,
               snapshot: Mapping[str, Any] | None) -> bytes:
        return dumps(make_batch(items, protocol=self.protocol, source_id=self.source_id, sent_at=self._clock(),
                                snapshot=snapshot, next_cursor=next_cursor, include_next_cursor=True))

    def handle_items(self, query: Query | None = None, headers: Headers | None = None) -> HttpResponse:
        """Answers ``GET {itemsUrl}?cursor=&limit=`` with a batch carrying ``nextCursor``."""
        denied = self._unauthorized(headers)
        if denied is not None:
            return denied
        limit = self._limit(query, self.default_limit)
        if isinstance(limit, HttpResponse):
            return limit
        cursor = _first(query, "cursor") or None
        try:
            position = None if cursor is None else self.cursor_store.decode(cursor)
        except CursorError as e:
            return _problem(400, "CURSOR_INVALID", str(e))
        statuses: list[dict[str, Any]] | None = None
        try:
            while True:
                page = self.source.fetch(position, limit)
                if statuses is None:
                    # Drained after the first fetch, so health the source reports while loading rides along.
                    statuses = self.status.drain() if self.status is not None else []
                next_cursor = None if page.next_position is None else self.cursor_store.encode(page.next_position)
                body = self._batch(statuses + list(page.items), next_cursor, page.snapshot)
                if len(body) <= self.max_bytes:
                    return HttpResponse(200, {"Content-Type": JSON_MEDIA_TYPE, "Cache-Control": "no-store"}, body)
                if limit == 1:
                    break
                limit = max(1, limit // 2)
        except CursorError as e:
            self._requeue(statuses)
            return _problem(400, "CURSOR_INVALID", str(e))
        except Exception as e:  # noqa: BLE001 (the provider's own failure becomes a 500, reported as health)
            log.exception("The EDP item source failed")
            self._requeue(statuses)
            return _problem(500, "PROVIDER_ERROR", f"the item source failed: {type(e).__name__}")
        self._requeue(statuses)
        return _problem(500, "PAGE_TOO_LARGE", f"a single item does not fit in {self.max_bytes} bytes")

    def _requeue(self, statuses: list[dict[str, Any]] | None) -> None:
        if self.status is not None and statuses:
            self.status.requeue(statuses)

    def handle_search(self, query: Query | None = None, headers: Headers | None = None) -> HttpResponse:
        """Answers ``GET {searchUrl}?q=&since=&until=&bbox=&lang=&limit=`` with a batch (``nextCursor`` null)."""
        if self.search is None:
            return _problem(404, "NOT_FOUND", "this provider has no search endpoint")
        denied = self._unauthorized(headers)
        if denied is not None:
            return denied
        q = (_first(query, "q") or "").strip()
        if not q:
            return _problem(400, "QUERY_REQUIRED", "q is required")
        times: dict[str, datetime | None] = {}
        for name in ("since", "until"):
            raw = _first(query, name)
            times[name] = parse_rfc3339(raw) if raw else None
            if raw and times[name] is None:
                return _problem(400, "TIME_INVALID", f"{name} must be an RFC 3339 date-time with an offset")
        bbox: tuple[float, float, float, float] | None = None
        raw_bbox = _first(query, "bbox")
        if raw_bbox:
            try:
                values = tuple(float(v) for v in raw_bbox.split(","))
            except ValueError:
                values = ()
            if (len(values) != 4 or not -180 <= values[0] <= values[2] <= 180
                    or not -90 <= values[1] <= values[3] <= 90):
                return _problem(400, "BBOX_INVALID", "bbox must be minLon,minLat,maxLon,maxLat in WGS84")
            bbox = (values[0], values[1], values[2], values[3])
        lang = [v.strip() for v in (_first(query, "lang") or "").split(",") if v.strip()]
        limit = self._limit(query, min(DEFAULT_SEARCH_LIMIT, self.max_limit))
        if isinstance(limit, HttpResponse):
            return limit
        request = SearchQuery(q, times["since"], times["until"], bbox, lang, limit)
        try:
            found = self.search(request)
        except Exception as e:  # noqa: BLE001
            log.exception("The EDP search function failed")
            return _problem(500, "PROVIDER_ERROR", f"the search failed: {type(e).__name__}")
        items = list(found.items if isinstance(found, Page) else found)[:limit]
        while True:
            body = self._batch(items, None, None)
            if len(body) <= self.max_bytes or not items:
                return HttpResponse(200, {"Content-Type": JSON_MEDIA_TYPE, "Cache-Control": "no-store"}, body)
            items = items[: len(items) // 2]

    def handle_manifest(self, headers: Headers | None = None) -> HttpResponse:
        """Answers the manifest URL (404 when no manifest was given)."""
        if self.manifest is None:
            return _problem(404, "NOT_FOUND", "no manifest is published here")
        return _json(200, self.manifest)

    def handle(self, method: str, path: str, query: Query | None = None,
               headers: Headers | None = None) -> HttpResponse:
        """Routes a request by path (a query string in ``path`` is merged into ``query``)."""
        parts = urlsplit(path)
        merged: dict[str, Any] = {k: v for k, v in parse_qs(parts.query, keep_blank_values=True).items()}
        merged.update(query or {})
        route = parts.path
        if route not in (self.items_path, self.search_path, self.manifest_path):
            return _problem(404, "NOT_FOUND", f"no route {route}")
        if method.upper() not in ("GET", "HEAD"):
            return _problem(405, "METHOD_NOT_ALLOWED", "only GET is supported", {"Allow": "GET, HEAD"})
        if route == self.items_path:
            response = self.handle_items(merged, headers)
        elif route == self.search_path:
            response = self.handle_search(merged, headers)
        else:
            response = self.handle_manifest(headers)
        if method.upper() == "HEAD":
            return HttpResponse(response.status, response.headers, b"")
        return response


def wsgi_app(handler: PullHandler) -> Callable[..., Iterable[bytes]]:
    """Adapts a :class:`PullHandler` to a WSGI application."""

    def app(environ: dict[str, Any], start_response: Callable[..., Any]) -> Iterable[bytes]:
        headers = {key[5:].replace("_", "-").lower(): value for key, value in environ.items()
                   if key.startswith("HTTP_")}
        query = parse_qs(environ.get("QUERY_STRING", ""), keep_blank_values=True)
        response = handler.handle(environ.get("REQUEST_METHOD", "GET"), environ.get("PATH_INFO", "/"), query,
                                  headers)
        phrase = HTTPStatus(response.status).phrase
        out_headers = list(response.headers.items()) + [("Content-Length", str(len(response.body)))]
        start_response(f"{response.status} {phrase}", out_headers)
        return [response.body]

    return app


def serve(handler: PullHandler, host: str = "", port: int = 8099) -> None:  # pragma: no cover (blocks)
    """Serves ``handler`` with the standard library's ``wsgiref`` (for development and examples; put a
    production WSGI server and TLS in front for real deployments)."""
    from wsgiref.simple_server import make_server

    with make_server(host, port, wsgi_app(handler)) as server:
        log.info("Serving EDP provider on %s:%s", host or "0.0.0.0", port)
        server.serve_forever()
