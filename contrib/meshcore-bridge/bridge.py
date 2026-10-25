#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
# Copyright (c) 2026 RemoteID Systems, LLC.
#
# Foresignal MeshCore bridge (prompt 083): reads the operator's own MeshCore companion radio over USB serial, BLE or
# TCP with the MIT "meshcore" package (meshcore_py, installed from PyPI, not vendored) and POSTs its events as JSON to
# Foresignal's meshcore-bridge source:
#
#     POST {FORESIGNAL_URL}/v1/ingest/meshcore-bridge/meshcore-json
#     X-Foresignal-Ingest-Key: {FORESIGNAL_INGEST_KEY}
#     {"events": [{"type": "contact" | "advert" | "self_info" | "channel_msg" | "contact_msg", "data": {...}}]}
#
# "data" is the meshcore_py event payload with its own keys. Channel secrets are never sent: only channel names.
#
# Receive-only towards the mesh: the bridge only identifies itself (app start), reads contacts, channel names and
# contacts by key, and fetches queued messages. It never changes a setting, never reboots or resets the radio, never
# imports or exports keys and never sends a request to another node.
"""Foresignal MeshCore bridge: companion radio events to Foresignal's meshcore-json ingest format."""

import argparse
import asyncio
import inspect
import json
import logging
import os
import sys
import urllib.error
import urllib.request

from meshcore import EventType, MeshCore

LOG = logging.getLogger("foresignal-meshcore-bridge")

#: Re-send the full contact list this often, so Foresignal sees contacts whose adverts it missed.
CONTACTS_REFRESH_SECONDS = 30 * 60

#: Reconnect back-off: 5 s doubling to 5 min.
RECONNECT_MIN, RECONNECT_MAX = 5, 300


async def maybe_await(value):
    """Awaits value when it is awaitable (meshcore_py versions differ on which calls are coroutines)."""
    if inspect.isawaitable(value):
        return await value
    return value


class Poster:
    """POSTs events to Foresignal, retrying a failed request a few times with a growing pause."""

    def __init__(self, url, key, source):
        self.endpoint = url.rstrip("/") + "/v1/ingest/" + source + "/meshcore-json"
        self.key = key

    def _post(self, body):
        request = urllib.request.Request(self.endpoint, data=body, method="POST", headers={
            "Content-Type": "application/json",
            "X-Foresignal-Ingest-Key": self.key,
            "User-Agent": "foresignal-meshcore-bridge/1",
        })
        with urllib.request.urlopen(request, timeout=30) as response:
            return response.status

    async def post(self, events):
        """Sends events (a list of {"type", "data"} dicts); logs and drops them after three failures."""
        if not events:
            return
        body = json.dumps({"events": events}, default=str).encode("utf-8")
        for attempt in range(3):
            try:
                status = await asyncio.to_thread(self._post, body)
                LOG.debug("posted %d event(s): HTTP %s", len(events), status)
                return
            except urllib.error.HTTPError as error:
                LOG.warning("Foresignal answered HTTP %s: %s", error.code, error.read()[:300])
                if 400 <= error.code < 500 and error.code != 429:
                    return
            except (urllib.error.URLError, OSError) as error:
                LOG.warning("could not reach Foresignal: %s", error)
            await asyncio.sleep(2 ** (attempt + 1))
        LOG.error("dropped %d event(s) after three failed attempts", len(events))


def without_secrets(payload):
    """A payload copy without channel secrets or hashes, which must never leave the bridge."""
    return {k: v for k, v in dict(payload or {}).items() if k not in ("channel_secret", "channel_hash")}


async def connect(args):
    """Opens the companion connection the arguments name."""
    if args.serial:
        return await MeshCore.create_serial(args.serial, args.baud)
    if args.ble:
        return await MeshCore.create_ble(args.ble, pin=args.pin)
    host, _, port = args.tcp.rpartition(":")
    return await MeshCore.create_tcp(host, int(port))


async def run_once(args, poster):
    """One connection: initial snapshot, then events until the companion disconnects."""
    mc = await connect(args)
    if mc is None:
        raise ConnectionError("the companion did not answer")
    channel_names = {}
    disconnected = asyncio.Event()

    started = await mc.commands.send_appstart()
    if started is not None and started.type != EventType.ERROR:
        await poster.post([{"type": "self_info", "data": started.payload}])

    for index in range(8):
        reply = await mc.commands.get_channel(index)
        if reply is not None and reply.type != EventType.ERROR:
            name = (reply.payload or {}).get("channel_name")
            if name:
                channel_names[index] = name

    async def post_contacts():
        reply = await mc.commands.get_contacts()
        if reply is not None and reply.type != EventType.ERROR:
            await poster.post([{"type": "contact", "data": c} for c in (reply.payload or {}).values()])

    await post_contacts()

    async def on_channel_message(event):
        data = without_secrets(event.payload)
        name = channel_names.get(data.get("channel_idx"))
        if name:
            data["channel_name"] = name
        await poster.post([{"type": "channel_msg", "data": data}])

    async def on_contact_message(event):
        await poster.post([{"type": "contact_msg", "data": without_secrets(event.payload)}])

    async def on_advert(event):
        key = (event.payload or {}).get("public_key")
        if not key:
            return
        reply = await mc.commands.get_contact_by_key(bytes.fromhex(key))
        if reply is not None and reply.type != EventType.ERROR and reply.payload:
            await poster.post([{"type": "advert", "data": reply.payload}])

    async def on_new_contact(event):
        await poster.post([{"type": "advert", "data": event.payload}])

    async def on_disconnected(event):
        disconnected.set()

    await maybe_await(mc.subscribe(EventType.CHANNEL_MSG_RECV, on_channel_message))
    await maybe_await(mc.subscribe(EventType.CONTACT_MSG_RECV, on_contact_message))
    await maybe_await(mc.subscribe(EventType.ADVERTISEMENT, on_advert))
    await maybe_await(mc.subscribe(EventType.NEW_CONTACT, on_new_contact))
    await maybe_await(mc.subscribe(EventType.DISCONNECTED, on_disconnected))
    await mc.start_auto_message_fetching()
    LOG.info("connected; streaming events to %s", poster.endpoint)

    try:
        while not disconnected.is_set():
            try:
                await asyncio.wait_for(disconnected.wait(), timeout=CONTACTS_REFRESH_SECONDS)
            except asyncio.TimeoutError:
                await post_contacts()
    finally:
        try:
            await mc.stop_auto_message_fetching()
            await mc.disconnect()
        except Exception:  # noqa: BLE001 - the connection is gone anyway
            pass


async def main_async(args):
    url = os.environ.get("FORESIGNAL_URL", "").strip()
    key = os.environ.get("FORESIGNAL_INGEST_KEY", "").strip()
    if not url or not key:
        sys.exit("Set FORESIGNAL_URL and FORESIGNAL_INGEST_KEY (the meshcore-bridge source's ingestKey credential).")
    poster = Poster(url, key, args.source)
    delay = RECONNECT_MIN
    while True:
        try:
            await run_once(args, poster)
            delay = RECONNECT_MIN
        except Exception as error:  # noqa: BLE001 - log and reconnect
            LOG.warning("companion connection failed: %s", error)
        LOG.info("reconnecting in %d s", delay)
        await asyncio.sleep(delay)
        delay = min(delay * 2, RECONNECT_MAX)


def parse_args(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    where = parser.add_mutually_exclusive_group(required=True)
    where.add_argument("--serial", help="serial port of a USB companion, for example /dev/ttyUSB0")
    where.add_argument("--ble", help="BLE address of a companion")
    where.add_argument("--tcp", help="host:port of a WiFi companion (no default port)")
    parser.add_argument("--baud", type=int, default=115200, help="serial speed (default 115200)")
    parser.add_argument("--pin", help="BLE pairing PIN, if the companion asks for one")
    parser.add_argument("--source", default="meshcore-bridge", help="the Foresignal source id (default meshcore-bridge)")
    parser.add_argument("--verbose", action="store_true", help="log every POST")
    return parser.parse_args(argv)


def main():
    args = parse_args()
    logging.basicConfig(level=logging.DEBUG if args.verbose else logging.INFO,
                        format="%(asctime)s %(levelname)s %(message)s")
    asyncio.run(main_async(args))


if __name__ == "__main__":
    main()
