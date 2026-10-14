#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
# Copyright (c) 2026 Adam Allard
#
# Foresignal rsudp alarm poster (prompt 086). Posts one Raspberry Shake trigger to a Foresignal custom source created
# from the "rsudp-alarm" preset:
#
#     POST {FORESIGNAL_URL}          (the preset's setup URL: .../v1/ingest/{sourceId}/k/{key}/mapping)
#     Content-Type: application/json
#     {"event": "ALARM" | "RESET", "time": "2026-10-13T15:04:05.123Z", "station": "R3BCF", "channel": "EHZ"}
#
# Two ways to run it:
#   1. As rsudp's custom code file ("custom": {"enabled": true, "codefile": "/path/to/foresignal_alarm.py"}). rsudp
#      exec()s the file on every ALARM; the post runs on a background thread with a short timeout so rsudp's queue is
#      not held up.
#   2. From the command line, for example from your own wrapper on RESET:  python3 foresignal_alarm.py RESET
#
# Standard library only. Written from rsudp's documentation (rsudp is GPL-3.0; no rsudp code is copied or imported).
# "time" is the moment this script runs, in UTC: rsudp's custom module does not pass the trigger time to the code file.
"""Post a Raspberry Shake ALARM or RESET to Foresignal's rsudp-alarm preset."""

import datetime
import json
import os
import sys
import threading
import urllib.request

# The preset's setup URL (GET /v1/custom-sources/{id} shows it), or the FORESIGNAL_URL environment variable.
FORESIGNAL_URL = os.environ.get("FORESIGNAL_URL", "https://foresignal.example/v1/ingest/custom-shake/k/REPLACE/mapping")

# rsudp's settings file, read for the station name and the alert channel; the fallbacks are used when it is missing.
RSUDP_SETTINGS = os.environ.get("RSUDP_SETTINGS", os.path.expanduser("~/.config/rsudp/rsudp_settings.json"))
FALLBACK_STATION = "Z0000"
FALLBACK_CHANNEL = "HZ"
TIMEOUT_SECONDS = 5


def _station_and_channel():
    """The station from settings.station and the channel from alert.channel, or the fallbacks."""
    try:
        with open(RSUDP_SETTINGS, encoding="utf-8") as handle:
            settings = json.load(handle)
        station = str(settings.get("settings", {}).get("station") or FALLBACK_STATION)
        channel = str(settings.get("alert", {}).get("channel") or FALLBACK_CHANNEL)
        return station, channel
    except (OSError, ValueError):
        return FALLBACK_STATION, FALLBACK_CHANNEL


def build_event(event):
    """The JSON body for one ALARM or RESET."""
    if event not in ("ALARM", "RESET"):
        raise ValueError("event must be ALARM or RESET")
    station, channel = _station_and_channel()
    now = datetime.datetime.now(datetime.timezone.utc).isoformat(timespec="milliseconds").replace("+00:00", "Z")
    return {"event": event, "time": now, "station": station, "channel": channel}


def post(event):
    """POSTs one event; returns the HTTP status, or raises on a network error."""
    body = json.dumps(build_event(event)).encode("utf-8")
    request = urllib.request.Request(FORESIGNAL_URL, data=body, method="POST",
                                     headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(request, timeout=TIMEOUT_SECONDS) as response:
        return response.status


def _post_quietly(event):
    try:
        post(event)
    except Exception as error:  # noqa: BLE001 - never raise into rsudp
        print("foresignal_alarm: post failed: %s" % error, file=sys.stderr)


if __name__ == "__main__":
    print(post(sys.argv[1] if len(sys.argv) > 1 else "ALARM"))
else:
    # exec()d by rsudp's custom module on an ALARM.
    threading.Thread(target=_post_quietly, args=("ALARM",), daemon=True).start()
