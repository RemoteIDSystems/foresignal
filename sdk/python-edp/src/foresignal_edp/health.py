"""Status reporting for pull providers (EDP §3, ``status`` items).

A push provider calls :meth:`foresignal_edp.push.PushClient.report_status`. A pull provider cannot
send on its own, so it records health on a :class:`StatusBoard`; the pull handler puts every changed
status at the front of the next page it serves. Health is kept per scope (the source, or one
stream), and only the latest value of each scope is delivered.
"""

from __future__ import annotations

import threading
from typing import Any

from .items import status as status_item


class StatusBoard:
    """Thread-safe latest health per scope, delivered once per change."""

    def __init__(self) -> None:
        """Creates an empty board (no health reported yet)."""
        self._lock = threading.Lock()
        self._latest: dict[str | None, dict[str, Any]] = {}
        self._pending: dict[str | None, dict[str, Any]] = {}

    def report(self, health: str, message: str | None = None, *, stream: str | None = None) -> None:
        """Records the health of the source (``stream`` ``None``) or of one stream. A value equal to the
        current one is not delivered again.

        :raises ValueError: for an unknown health value
        """
        item = status_item(health, message, stream=stream)
        with self._lock:
            if self._latest.get(stream) == item:
                return
            self._latest[stream] = item
            self._pending[stream] = item

    def ok(self, *, stream: str | None = None, message: str | None = None) -> None:
        """Records ``ok`` (shorthand for :meth:`report`)."""
        self.report("ok", message, stream=stream)

    def current(self) -> list[dict[str, Any]]:
        """The latest status item of every scope reported so far."""
        with self._lock:
            return [dict(v) for v in self._latest.values()]

    def drain(self) -> list[dict[str, Any]]:
        """Returns the status items changed since the last drain, and forgets them."""
        with self._lock:
            out = [dict(v) for v in self._pending.values()]
            self._pending.clear()
            return out

    def requeue(self, items: list[dict[str, Any]]) -> None:
        """Puts drained items back (when the page that carried them could not be served), unless a newer
        value for the same scope was reported meanwhile."""
        with self._lock:
            for item in items:
                scope = item.get("stream")
                if scope not in self._pending and self._latest.get(scope) == item:
                    self._pending[scope] = item
