#!/usr/bin/env python3
"""Foresignal change stream to stdout, for Vector's exec source (docs/cookbooks/event-bus-bridges.md).

Connects to /v1/changes/stream with the token from FORESIGNAL_TOKEN, resumes with Last-Event-ID
from a cursor file, and prints every persisted change as one JSON line on stdout. The cursor (the
last printed seq) is written after each line, so a restart replays at most one change: consumers
dedupe on seq.

Environment:
  FORESIGNAL_URL          base URL, for example http://foresignal.local:8080
  FORESIGNAL_TOKEN        a read token (fsr_...)
  FORESIGNAL_CURSOR_FILE  where the last seq is kept (default ./foresignal-sse.cursor)
  FORESIGNAL_QUERY        extra stream filters, for example "kind=earthquake&payload=true"

Only the Python standard library is used. Exit codes: 0 never (it runs until stopped), 2 for a
configuration error, 3 when the server says the history needed for the resume point is gone
(reset with reason resync_required): take a new snapshot and reset the cursor file.
"""

import json
import os
import sys
import time
import urllib.error
import urllib.request

BASE = os.environ.get("FORESIGNAL_URL", "").rstrip("/")
TOKEN = os.environ.get("FORESIGNAL_TOKEN", "")
CURSOR_FILE = os.environ.get("FORESIGNAL_CURSOR_FILE", "./foresignal-sse.cursor")
QUERY = os.environ.get("FORESIGNAL_QUERY", "")


def log(message):
    """Writes a diagnostic line to stderr (Vector can include or drop it)."""
    print("foresignal_sse: " + message, file=sys.stderr, flush=True)


def read_cursor():
    """The stored seq, or None for a first start (the stream then starts live)."""
    try:
        with open(CURSOR_FILE, encoding="utf-8") as f:
            value = f.read().strip()
            return value if value.isdigit() else None
    except FileNotFoundError:
        return None


def write_cursor(seq):
    """Stores seq atomically."""
    tmp = CURSOR_FILE + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        f.write(str(seq))
    os.replace(tmp, CURSOR_FILE)


def events(response):
    """Yields (event, id, data) for each server-sent event of an open response."""
    event, event_id, data = "message", None, []
    for raw in response:
        line = raw.decode("utf-8").rstrip("\r\n")
        if line == "":
            if data:
                yield event, event_id, "\n".join(data)
            event, event_id, data = "message", None, []
        elif line.startswith(":"):
            continue
        else:
            field, _, value = line.partition(":")
            value = value[1:] if value.startswith(" ") else value
            if field == "event":
                event = value
            elif field == "id":
                event_id = value
            elif field == "data":
                data.append(value)


def stream_once(resume):
    """Reads the stream until it ends; returns the seq to resume from next."""
    url = BASE + "/v1/changes/stream?live=false" + ("&" + QUERY if QUERY else "")
    headers = {"Authorization": "Bearer " + TOKEN, "Accept": "text/event-stream"}
    if resume is not None:
        headers["Last-Event-ID"] = str(resume)
    request = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(request, timeout=60) as response:
        for event, event_id, data in events(response):
            if event == "change":
                sys.stdout.write(json.dumps(json.loads(data), separators=(",", ":")) + "\n")
                sys.stdout.flush()
                if event_id:
                    resume = event_id
                    write_cursor(event_id)
            elif event == "reset":
                info = json.loads(data or "{}")
                reason = info.get("reason")
                if reason == "resync_required":
                    log("history for the resume point is gone; take a snapshot and reset " + CURSOR_FILE)
                    sys.exit(3)
                if info.get("resumeAfter") is not None:
                    resume = str(info["resumeAfter"])
                    write_cursor(resume)
                log("stream reset (" + str(reason) + "); reconnecting")
    return resume


def main():
    """Reconnects forever with a capped backoff; heartbeats every 15 s keep the 60 s read timeout alive."""
    if not BASE or not TOKEN:
        log("set FORESIGNAL_URL and FORESIGNAL_TOKEN")
        sys.exit(2)
    resume = read_cursor()
    delay = 1
    while True:
        try:
            resume = stream_once(resume)
            delay = 1
        except urllib.error.HTTPError as e:
            log("HTTP " + str(e.code) + "; retrying in " + str(delay) + " s")
            if e.code in (401, 403):
                delay = 60
        except (urllib.error.URLError, OSError, ValueError) as e:
            log("connection failed: " + str(e) + "; retrying in " + str(delay) + " s")
        time.sleep(delay)
        delay = min(delay * 2, 60)


if __name__ == "__main__":
    main()
