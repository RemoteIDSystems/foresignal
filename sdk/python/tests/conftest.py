"""An in-process HTTP server for the SDK tests: records every request and lets each test script the answers,
including Server-Sent Event streams that stay open, end, or go silent."""
from __future__ import annotations

import json
import pathlib
import threading
import time
from collections.abc import Callable, Iterator
from dataclasses import dataclass, field
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any
from urllib.parse import parse_qs, urlsplit

import pytest

FIXTURES = pathlib.Path(__file__).resolve().parents[2] / "contract-fixtures"


@dataclass
class Recorded:
    """A request the server received."""

    method: str
    path: str
    query: dict[str, list[str]]
    headers: dict[str, str]
    body: str

    def q(self, name: str) -> str | None:
        """The first value of a query parameter."""
        values = self.query.get(name)
        return values[0] if values else None

    def header(self, name: str) -> str | None:
        """A header, case-insensitively."""
        for key, value in self.headers.items():
            if key.lower() == name.lower():
                return value
        return None


class Reply:
    """Writes the answer to one request."""

    def __init__(self, handler: BaseHTTPRequestHandler, stop: threading.Event) -> None:
        self._h = handler
        self._stop = stop

    def json(self, status: int, body: Any, headers: dict[str, str] | None = None) -> None:
        """Answers with JSON (``application/problem+json`` for errors)."""
        data = json.dumps(body).encode("utf-8")
        self._h.send_response(status)
        self._h.send_header("Content-Type", "application/problem+json" if status >= 400 else "application/json")
        self._h.send_header("Content-Length", str(len(data)))
        for key, value in (headers or {}).items():
            self._h.send_header(key, value)
        self._h.end_headers()
        self._h.wfile.write(data)

    def sse(self, frames: list[dict[str, Any]], keep_open: bool = False) -> None:
        """Answers with an SSE stream of ``frames``; ``keep_open`` holds the connection until the server stops."""
        self._h.send_response(200)
        self._h.send_header("Content-Type", "text/event-stream")
        self._h.send_header("Cache-Control", "no-cache")
        self._h.end_headers()
        self.frames(frames)
        if keep_open:
            while not self._stop.wait(0.02):
                pass

    def frames(self, frames: list[dict[str, Any]]) -> None:
        """Writes frames to an open SSE response."""
        for f in frames:
            text = ""
            if "comment" in f:
                text += f":{f['comment']}\n"
            if "retry" in f:
                text += f"retry: {f['retry']}\n"
            if "id" in f:
                text += f"id: {f['id']}\n"
            if "event" in f:
                text += f"event: {f['event']}\n"
            if "data" in f:
                data = f["data"] if isinstance(f["data"], str) else json.dumps(f["data"])
                text += f"data: {data}\n"
            self._h.wfile.write((text + "\n").encode("utf-8"))
            self._h.wfile.flush()


Handler = Callable[[Recorded, Reply], None]


@dataclass
class MockServer:
    """A running mock server."""

    url: str
    requests: list[Recorded] = field(default_factory=list)
    handler: Handler | None = None

    def to(self, path: str, method: str | None = None) -> list[Recorded]:
        """Requests to one path."""
        return [r for r in self.requests if r.path == path and (method is None or r.method == method)]


@pytest.fixture
def server() -> Iterator[MockServer]:
    """A mock server on a free loopback port; set ``server.handler`` in the test."""
    stop = threading.Event()
    state: dict[str, MockServer] = {}

    class _Handler(BaseHTTPRequestHandler):
        protocol_version = "HTTP/1.0"

        def log_message(self, format: str, *args: Any) -> None:  # noqa: A002 - BaseHTTPRequestHandler's name
            return

        def _serve(self) -> None:
            length = int(self.headers.get("Content-Length") or 0)
            body = self.rfile.read(length).decode("utf-8") if length else ""
            parts = urlsplit(self.path)
            recorded = Recorded(self.command, parts.path, parse_qs(parts.query, keep_blank_values=True),
                                {k: v for k, v in self.headers.items()}, body)
            mock = state["server"]
            mock.requests.append(recorded)
            reply = Reply(self, stop)
            try:
                if mock.handler is None:
                    reply.json(404, {"status": 404})
                else:
                    mock.handler(recorded, reply)
            except (BrokenPipeError, ConnectionResetError):
                pass

        do_GET = _serve
        do_POST = _serve
        do_PATCH = _serve
        do_PUT = _serve
        do_DELETE = _serve

    httpd = ThreadingHTTPServer(("127.0.0.1", 0), _Handler)
    httpd.daemon_threads = True
    mock = MockServer(url=f"http://127.0.0.1:{httpd.server_address[1]}")
    state["server"] = mock
    thread = threading.Thread(target=httpd.serve_forever, daemon=True)
    thread.start()
    try:
        yield mock
    finally:
        stop.set()
        httpd.shutdown()
        httpd.server_close()


@pytest.fixture
def recording_sleep() -> tuple[list[float], Callable[[float], None]]:
    """An instant sleep that records the requested delays."""
    delays: list[float] = []
    return delays, delays.append


def collect(stream: Any, until: Callable[[Any, list[Any]], bool], limit: int = 200) -> list[Any]:
    """Collects stream signals until ``until`` is true (or ``limit`` signals), then closes the stream."""
    out: list[Any] = []
    iterator = iter(stream)
    try:
        for signal in iterator:
            out.append(signal)
            if until(signal, out) or len(out) >= limit:
                break
    finally:
        stream.close()
        close = getattr(iterator, "close", None)
        if close:
            close()
    return out


def wait_for(predicate: Callable[[], bool], timeout: float = 5.0) -> None:
    """Waits until ``predicate`` is true."""
    deadline = time.monotonic() + timeout
    while not predicate():
        if time.monotonic() > deadline:
            raise AssertionError("timed out waiting for the condition")
        time.sleep(0.01)


def load_fixture(name: str) -> Any:
    """Loads a shared contract fixture from sdk/contract-fixtures."""
    return json.loads((FIXTURES / name).read_text(encoding="utf-8"))
