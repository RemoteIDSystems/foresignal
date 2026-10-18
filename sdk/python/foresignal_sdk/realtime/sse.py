"""An incremental Server-Sent Events parser (WHATWG HTML "event stream interpretation")."""
from __future__ import annotations

import re
from dataclasses import dataclass

_DIGITS = re.compile(r"^\d+$")


@dataclass(frozen=True)
class SseMessage:
    """One dispatched event."""

    event: str
    """The ``event:`` name (``message`` when absent)."""
    data: str
    """The ``data:`` lines joined with ``\\n``."""
    id: str | None
    """The ``id:`` of this event, only when the event carried one."""


class SseParser:
    """Parses an event stream fed with decoded text chunks of any size."""

    def __init__(self) -> None:
        """Creates an empty parser."""
        self._buffer = ""
        self._started = False
        self._event = ""
        self._data: list[str] = []
        self._id: str | None = None
        self._has_id = False
        self.retry_ms: int | None = None
        """The latest ``retry:`` value in milliseconds, or None."""

    def feed(self, chunk: str) -> list[SseMessage]:
        """Feeds a chunk of text and returns the events it completed."""
        text = self._buffer + chunk
        if not self._started and text:
            self._started = True
            if text.startswith("﻿"):
                text = text[1:]
        out: list[SseMessage] = []
        pos = 0
        while True:
            end = -1
            for i in range(pos, len(text)):
                if text[i] in "\r\n":
                    end = i
                    break
            if end < 0:
                break
            if text[end] == "\r" and end == len(text) - 1:
                break  # possibly the first half of CRLF
            line = text[pos:end]
            pos = end + (2 if text[end] == "\r" and end + 1 < len(text) and text[end + 1] == "\n" else 1)
            self._line(line, out)
        self._buffer = text[pos:]
        return out

    def _line(self, line: str, out: list[SseMessage]) -> None:
        if line == "":
            self._dispatch(out)
            return
        if line.startswith(":"):
            return
        field, sep, value = line.partition(":")
        if sep and value.startswith(" "):
            value = value[1:]
        if field == "event":
            self._event = value
        elif field == "data":
            self._data.append(value)
        elif field == "id":
            if "\x00" not in value:
                self._id = value
                self._has_id = True
        elif field == "retry":
            if _DIGITS.match(value):
                self.retry_ms = int(value)

    def _dispatch(self, out: list[SseMessage]) -> None:
        if self._data:
            out.append(SseMessage(event=self._event or "message", data="\n".join(self._data),
                                  id=self._id if self._has_id and self._id != "" else None))
        self._event = ""
        self._data = []
        self._id = None
        self._has_id = False
