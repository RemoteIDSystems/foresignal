"""``ChangeStream``: a resuming reader of ``GET /v1/changes/stream`` and ``GET /v1/events/stream``.

It implements the client contract of docs/Core-Data-Model.md section 10.2 (prompts 015 and 023) with the same
semantics as the Dart and TypeScript SDKs:

- resumes with ``Last-Event-ID`` (or ``after=`` / ``lastEventId=`` when a stream ticket replaces the header)
  and tracks the last ``change`` id;
- on ``reset``: yields the reset; when the reason is ``resync_required`` it pages the snapshot
  (``/v1/records?order=id``, keeping ``watermarkSeq``) and reopens at the watermark, otherwise it reopens at
  ``resumeAfter`` (or the last id);
- on ``resync``: pages the current state again and keeps the stream open;
- applies ``live`` and ``touch`` events only when their ``observedAt`` is not older than the stored one;
- reconnects when no event or heartbeat arrives within 2.5 x the heartbeat interval (the httpx read timeout),
  with jittered exponential backoff (1 s to 60 s) that honors ``retry:`` and ``Retry-After``.

Iterate it (a generator of :class:`StreamSignal`); call :meth:`ChangeStream.close` to stop, from any thread.
"""
from __future__ import annotations

import codecs
import json
import threading
from collections.abc import Callable, Iterator, Mapping
from dataclasses import dataclass
from datetime import datetime
from typing import Any

import httpx

from ..errors import ApiError, HeartbeatTimeout, api_error_from
from ..http import HttpTransport, Query, clean_query
from .backoff import Backoff
from .sse import SseParser

#: Filter names a records snapshot understands.
SNAPSHOT_KEYS = frozenset({
    "source", "kind", "bbox", "externalId", "near", "radiusKm", "place", "view", "inWatchlist", "licencePolicy",
})

#: Event names handled by the stream itself; everything else is yielded as type ``event``.
CORE_EVENTS = frozenset({"hello", "change", "live", "touch", "resync", "reset", "heartbeat"})


@dataclass(frozen=True)
class StreamSignal:
    """What the stream yields; ``type`` is one of open, hello, change, live, touch, resync, reset, snapshot,
    heartbeat, event or reconnecting, and the other members are set as that type needs."""

    type: str
    data: Any = None
    id: str | None = None
    event: str | None = None
    url: str | None = None
    cause: str | None = None
    items: list[Any] | None = None
    watermark_seq: int | None = None
    done: bool | None = None
    attempt: int | None = None
    delay: float | None = None
    reason: str | None = None


def composite_cursor_value(cursor: str | None, key: str) -> int | None:
    """The value of ``key`` in a composite cursor (``c=1234;a=56``), or None."""
    if not cursor:
        return None
    for part in cursor.split(";"):
        name, sep, value = part.partition("=")
        if sep and name == key:
            try:
                return int(value)
            except ValueError:
                return None
    return None


def with_composite_cursor_value(cursor: str | None, key: str, value: int) -> str:
    """``cursor`` with ``key`` set to ``value`` (added first when absent)."""
    parts = [p for p in (cursor.split(";") if cursor else []) if p and not p.startswith(key + "=")]
    return ";".join([f"{key}={value}"] + parts)


def _parse_time(value: Any) -> float | None:
    if not isinstance(value, str):
        return None
    try:
        return datetime.fromisoformat(value.replace("Z", "+00:00")).timestamp()
    except ValueError:
        return None


class ObservedAtGuard:
    """Remembers the newest ``observedAt`` per entity and refuses older updates."""

    def __init__(self) -> None:
        """Creates an empty guard."""
        self._latest: dict[tuple[str, str], float] = {}

    @staticmethod
    def key(source_id: str, external_id: str) -> tuple[str, str]:
        """The key of an entity."""
        return (source_id, external_id)

    def accept(self, key: tuple[str, str], observed_at: Any) -> bool:
        """True (and remembered) when ``observed_at`` is not older than the stored value; unparseable times pass."""
        t = _parse_time(observed_at)
        if t is None:
            return True
        stored = self._latest.get(key)
        if stored is not None and t < stored:
            return False
        self._latest[key] = t
        return True

    def observe(self, key: tuple[str, str], observed_at: Any) -> None:
        """Remembers ``observed_at`` when it is newer than the stored value (changes and snapshot rows)."""
        self.accept(key, observed_at)

    def get(self, key: tuple[str, str]) -> float | None:
        """The stored ``observedAt`` as a POSIX timestamp, or None."""
        return self._latest.get(key)

    def __len__(self) -> int:
        """Number of entities tracked."""
        return len(self._latest)


def _parse_data(raw: str) -> Any:
    try:
        return json.loads(raw)
    except ValueError:
        return raw


def _describe(error: BaseException) -> str:
    return f"{type(error).__name__}: {error}"


class ChangeStream:
    """A resuming change stream."""

    def __init__(self, base_url: str | None = None, token: str | None = None, *, endpoint: str = "changes",
                 filters: Query | None = None, after: int | None = None, last_event_id: str | None = None,
                 heartbeat_interval: float = 15.0, backoff: Backoff | None = None, use_ticket: bool = False,
                 snapshot: bool = True, snapshot_page_size: int = 1000, snapshot_query: Query | None = None,
                 transport: HttpTransport | None = None, headers: Mapping[str, str] | None = None,
                 sleep: Callable[[float], None] | None = None) -> None:
        """Creates a stream; nothing is sent until it is iterated.

        :param base_url: the Foresignal base URL (or pass ``transport``)
        :param token: a Foresignal API token
        :param endpoint: ``changes`` (``/v1/changes/stream``) or ``events`` (``/v1/events/stream``)
        :param filters: stream query parameters (``source``, ``kind``, ``bbox``, … or ``topics`` and the prefixed
            topic filters for ``events``)
        :param after: start after this change seq
        :param last_event_id: start from this event id (``events`` composite cursor)
        :param heartbeat_interval: the server's heartbeat interval in seconds (default 15)
        :param backoff: reconnect backoff (default 1 s to 60 s)
        :param use_ticket: authenticate with a stream ticket instead of the header and send the cursor in the query
        :param snapshot: page ``/v1/records?order=id`` on ``reset {resync_required}`` and on ``resync``
        :param snapshot_page_size: snapshot page size (1 to 5000)
        :param snapshot_query: snapshot query (defaults to the stream's record filters)
        :param sleep: sleep between reconnects (tests replace it); the default wakes up on :meth:`close`
        """
        if endpoint not in ("changes", "events"):
            raise ValueError("endpoint must be 'changes' or 'events'")
        if transport is None:
            if base_url is None:
                raise ValueError("pass base_url or transport")
            transport = HttpTransport(base_url, token, headers=headers)
        self.endpoint = endpoint
        self.transport = transport
        self.filters: dict[str, Any] = dict(filters or {})
        self.watchdog_timeout = 2.5 * heartbeat_interval
        self.backoff = backoff or Backoff()
        self.use_ticket = use_ticket
        self.snapshot_enabled = snapshot
        self.snapshot_page_size = max(1, min(5000, snapshot_page_size))
        self.snapshot_query = dict(snapshot_query) if snapshot_query is not None else None
        self.guard = ObservedAtGuard()
        self.stale_dropped = 0
        self.stream_id: str | None = None
        self._stop = threading.Event()
        self._sleep = sleep or (lambda seconds: self._stop.wait(seconds) and None)
        self._response: httpx.Response | None = None
        self._ticket: str | None = None
        self._server_retry: float | None = None
        self._started = False
        self._seq: int | None = after if after is not None else composite_cursor_value(last_event_id, "c")
        if last_event_id is not None:
            self._event_id: str | None = last_event_id
        elif after is not None and endpoint == "events":
            self._event_id = f"c={after}"
        else:
            self._event_id = None

    @property
    def last_seq(self) -> int | None:
        """The last change seq received (or the watermark the stream reopened at)."""
        return self._seq

    @property
    def last_event_id(self) -> str | None:
        """The last event id (``events`` composite cursor; the seq on ``changes``)."""
        if self.endpoint == "events":
            return self._event_id
        return None if self._seq is None else str(self._seq)

    @property
    def closed(self) -> bool:
        """Whether :meth:`close` was called."""
        return self._stop.is_set()

    def close(self) -> None:
        """Stops the stream; safe to call from another thread."""
        self._stop.set()
        response = self._response
        if response is not None:
            try:
                response.close()
            except Exception:  # noqa: BLE001 - closing a response that is being read may fail; it is closed anyway
                pass

    def __iter__(self) -> Iterator[StreamSignal]:
        """Iterates the stream (once)."""
        if self._started:
            raise RuntimeError("A ChangeStream can be iterated only once")
        self._started = True
        return self._run()

    # -- the loop --------------------------------------------------------------------------------------

    def _run(self) -> Iterator[StreamSignal]:
        while not self.closed:
            try:
                response, url = self._connect()
            except Exception as error:  # noqa: BLE001 - network errors are retried below
                if self.closed:
                    return
                if not self._retryable(error):
                    raise
                floor = max(self._server_retry or 0.0,
                            (error.retry_after or 0.0) if isinstance(error, ApiError) else 0.0)
                yield from self._pause(self.backoff.next(floor), _describe(error))
                continue
            self.backoff.reset()
            self._response = response
            yield StreamSignal("open", url=url)

            reset: dict[str, Any] | None = None
            failure: BaseException | None = None
            parser = SseParser()
            decoder = codecs.getincrementaldecoder("utf-8")(errors="replace")
            try:
                for chunk in response.iter_bytes():
                    messages = parser.feed(decoder.decode(chunk))
                    if parser.retry_ms is not None:
                        self._server_retry = parser.retry_ms / 1000.0
                    for message in messages:
                        if message.event == "reset":
                            reset = _parse_data(message.data)
                            if not isinstance(reset, dict):
                                reset = {"reason": str(reset)}
                            yield StreamSignal("reset", data=reset)
                            break
                        yield from self._handle(message.event, message.data, message.id)
                    if reset is not None:
                        break
            except httpx.ReadTimeout:
                failure = HeartbeatTimeout(self.watchdog_timeout)
            except (httpx.HTTPError, httpx.StreamError, OSError) as error:
                failure = error
            finally:
                self._response = None
                response.close()
            if self.closed:
                return

            if reset is not None:
                try:
                    yield from self._after_reset(reset)
                except Exception as error:  # noqa: BLE001 - retried unless fatal
                    if self.closed:
                        return
                    if not self._retryable(error):
                        raise
                    yield from self._pause(self.backoff.next(self._server_retry or 0.0), _describe(error))
                    continue
                reason = f"reset: {reset.get('reason')}"
                if self._server_retry:
                    yield from self._pause(self._server_retry, reason)
                else:
                    yield StreamSignal("reconnecting", attempt=0, delay=0.0, reason=reason)
            else:
                reason = _describe(failure) if failure else "stream closed by the server"
                yield from self._pause(self.backoff.next(self._server_retry or 0.0), reason)

    def _handle(self, event: str, raw: str, event_id: str | None) -> Iterator[StreamSignal]:
        if event_id is not None:
            if self.endpoint == "events":
                self._event_id = event_id
                c = composite_cursor_value(event_id, "c")
                if c is not None:
                    self._seq = c
            elif event == "change":
                try:
                    self._seq = int(event_id)
                except ValueError:
                    pass
        data = _parse_data(raw)
        if event == "hello":
            if isinstance(data, dict):
                self.stream_id = data.get("streamId")
            yield StreamSignal("hello", data=data)
        elif event == "change":
            if isinstance(data, dict):
                self.guard.observe(ObservedAtGuard.key(data.get("source", ""), data.get("externalId", "")),
                                   data.get("observedAt"))
            yield StreamSignal("change", data=data, id=event_id)
        elif event in ("live", "touch"):
            if isinstance(data, dict) and not self.guard.accept(
                    ObservedAtGuard.key(data.get("sourceId", ""), data.get("externalId", "")), data.get("observedAt")):
                self.stale_dropped += 1
                return
            yield StreamSignal(event, data=data)
        elif event == "resync":
            yield StreamSignal("resync", data=data)
            if self.snapshot_enabled:
                yield from self._snapshot("resync")
        elif event == "heartbeat":
            yield StreamSignal("heartbeat")
        elif event not in CORE_EVENTS:
            yield StreamSignal("event", event=event, data=data, id=event_id)

    def _after_reset(self, reset: dict[str, Any]) -> Iterator[StreamSignal]:
        if reset.get("reason") == "resync_required":
            watermark = reset.get("watermarkSeq")
            if self.snapshot_enabled:
                watermark = yield from self._snapshot("reset")
            self._move_cursor(watermark if isinstance(watermark, int) else None)
            return
        resume = reset.get("resumeAfter")
        if isinstance(resume, int):
            self._move_cursor(resume)

    def _move_cursor(self, seq: int | None) -> None:
        self._seq = seq
        if self.endpoint == "events":
            self._event_id = None if seq is None else with_composite_cursor_value(self._event_id, "c", seq)

    def _snapshot(self, cause: str) -> Iterator[StreamSignal]:
        """Pages ``/v1/records?order=id`` with the record filters; returns (via StopIteration) the watermark."""
        query: dict[str, Any] = {**self._snapshot_query(), "order": "id", "limit": self.snapshot_page_size}
        cursor: str | None = None
        watermark: int | None = None
        while True:
            page, response = self.transport.request("GET", "/v1/records",
                                                    query={**query, "cursor": cursor} if cursor else query)
            page = page or {}
            if watermark is None:
                header = response.headers.get("Foresignal-Watermark-Seq")
                value = page.get("watermarkSeq", page.get("asOfSeq"))
                watermark = value if isinstance(value, int) else (int(header) if header and header.isdigit() else None)
            items = page.get("items") or []
            for item in items:
                if isinstance(item, dict):
                    self.guard.observe(ObservedAtGuard.key(item.get("source", ""), item.get("externalId", "")),
                                       item.get("observedAt"))
            cursor = page.get("nextCursor") or None
            yield StreamSignal("snapshot", cause=cause, items=items, watermark_seq=watermark, done=cursor is None)
            if cursor is None or self.closed:
                return watermark

    def _snapshot_query(self) -> dict[str, Any]:
        if self.snapshot_query is not None:
            return dict(self.snapshot_query)
        out: dict[str, Any] = {}
        for key, value in self.filters.items():
            name = key[len("changes."):] if self.endpoint == "events" and key.startswith("changes.") else (
                key if self.endpoint == "changes" else None)
            if name is not None and name in SNAPSHOT_KEYS:
                out[name] = value
        return out

    def _connect(self) -> tuple[httpx.Response, str]:
        path = "/v1/events/stream" if self.endpoint == "events" else "/v1/changes/stream"
        ticket_attempt = 0
        while True:
            query: dict[str, Any] = dict(self.filters)
            headers = {"Accept": "text/event-stream", "Cache-Control": "no-cache"}
            if self.endpoint == "events":
                if self._event_id:
                    if self.use_ticket:
                        query["lastEventId"] = self._event_id
                    else:
                        headers["Last-Event-ID"] = self._event_id
            elif self._seq is not None:
                if self.use_ticket:
                    query["after"] = self._seq
                else:
                    headers["Last-Event-ID"] = str(self._seq)
            if self.use_ticket:
                query["ticket"] = self._ensure_ticket()
            response = self.transport.open_stream(path, query=query, headers=headers, auth=not self.use_ticket,
                                                  read_timeout=self.watchdog_timeout)
            if response.status_code == 200:
                shown = {k: v for k, v in query.items() if k != "ticket"}
                url = str(httpx.URL(self.transport.url(path), params=clean_query(shown)))
                return response, url
            response.read()
            response.close()
            if response.status_code == 401 and self.use_ticket and ticket_attempt == 0:
                # The ticket expired (5 min after the last disconnect, 24 h in total): mint a new one once.
                ticket_attempt += 1
                self._ticket = None
                continue
            raise api_error_from(response)

    def _ensure_ticket(self) -> str:
        if self._ticket is None:
            data, _ = self.transport.post_json("/v1/stream-tickets", {"purpose": "stream"})
            self._ticket = data["ticket"]
        return self._ticket

    @staticmethod
    def _retryable(error: BaseException) -> bool:
        if isinstance(error, ApiError):
            return error.status in (408, 425, 429) or error.status >= 500
        return isinstance(error, (httpx.HTTPError, httpx.StreamError, OSError, HeartbeatTimeout))

    def _pause(self, delay: float, reason: str) -> Iterator[StreamSignal]:
        yield StreamSignal("reconnecting", attempt=self.backoff.attempt, delay=delay, reason=reason)
        self._sleep(delay)

