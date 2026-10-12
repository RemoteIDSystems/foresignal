#!/usr/bin/env python3
"""Receive-only TAK bridge: Cursor on Target from a TAK Server or the SA multicast group into Foresignal.

Cookbook: docs/cookbooks/tak-bridge.md

Reads CoT events with PyTAK (Apache-2.0), parses XML with defusedxml (TAK Protocol v1 protobuf
through takproto when TAK_PROTO is set), maps each event to a Foresignal record and posts batches to
a custom source with its ingest key. It never sends anything to the TAK network.

Mapping: uid -> externalId, time -> observedAt, stale -> validUntil, point lat/lon -> geometry,
type, how, callsign, hae, ce and le -> payload. Kind: cot-event.

Environment (PyTAK reads its own keys from the same config section):
  COT_URL                   tls://takserver.lan:8089 (preferred) or udp://239.2.3.1:6969 (SA multicast)
  PYTAK_TLS_CLIENT_CERT     client certificate (PEM) for tls://
  PYTAK_TLS_CLIENT_KEY      its key
  PYTAK_TLS_CLIENT_CAFILE   the TAK Server CA
  TAK_PROTO                 0 for XML (default); see PyTAK for protobuf
  FORESIGNAL_URL            http://foresignal.local:8080
  FORESIGNAL_SOURCE         custom-tak
  FORESIGNAL_INGEST_KEY     eik_... (bound to that source)
  BATCH_SIZE                records per POST (default 200)
  BATCH_SECONDS             longest wait before a POST (default 2)
"""

import asyncio
import logging
import os
from configparser import ConfigParser
from datetime import datetime, timezone

import aiohttp
import defusedxml.ElementTree as SafeET
import pytak

KIND = "cot-event"
LOG = logging.getLogger("foresignal_tak_bridge")


def iso(value):
    """A CoT time attribute (ISO 8601) normalized to RFC 3339 UTC, or None."""
    if not value:
        return None
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        return None
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=timezone.utc)
    return parsed.astimezone(timezone.utc).isoformat().replace("+00:00", "Z")


def millis(value):
    """Milliseconds since the epoch (TAK protobuf times) as RFC 3339 UTC, or None."""
    if not value:
        return None
    return datetime.fromtimestamp(value / 1000, tz=timezone.utc).isoformat().replace("+00:00", "Z")


def number(value):
    """A float, or None for a missing, unparseable or CoT 'unknown' (9999999) value."""
    try:
        parsed = float(value)
    except (TypeError, ValueError):
        return None
    return None if parsed >= 9999999.0 else parsed


def record(uid, cot_type, how, time, stale, lat, lon, hae, ce, le, callsign):
    """One Foresignal record in the native ingest format, or None when unusable."""
    if not uid or time is None:
        return None
    lat, lon = number(lat), number(lon)
    geometry = None
    if lat is not None and lon is not None and -90 <= lat <= 90 and -180 <= lon <= 180:
        geometry = {"type": "Point", "coordinates": [lon, lat]}
    payload = {"type": cot_type, "how": how, "hae": number(hae), "ce": number(ce), "le": number(le)}
    if callsign:
        payload["callsign"] = callsign
    out = {"externalId": uid[:200], "kind": KIND, "observedAt": time, "geometry": geometry,
           "payload": {k: v for k, v in payload.items() if v is not None}}
    if stale:
        out["validUntil"] = stale
    return out


def from_xml(data):
    """Maps one CoT XML event; defusedxml refuses entity expansion and external references."""
    event = SafeET.fromstring(data)
    if event.tag != "event":
        return None
    point = event.find("point")
    contact = event.find("detail/contact")
    return record(
        event.get("uid"), event.get("type"), event.get("how"), iso(event.get("time")), iso(event.get("stale")),
        point.get("lat") if point is not None else None, point.get("lon") if point is not None else None,
        point.get("hae") if point is not None else None, point.get("ce") if point is not None else None,
        point.get("le") if point is not None else None,
        contact.get("callsign") if contact is not None else None)


def from_proto(data):
    """Maps one TAK Protocol v1 message (needs pytak[with_takproto])."""
    import takproto  # optional, only for TAK_PROTO

    message = takproto.parse_proto(bytearray(data))
    if message is None:
        return None
    cot = message.cotEvent
    callsign = cot.detail.contact.callsign if cot.HasField("detail") else None
    return record(cot.uid, cot.type, cot.how, millis(cot.sendTime), millis(cot.staleTime),
                  cot.lat, cot.lon, cot.hae, cot.ce, cot.le, callsign)


class IngestWorker(pytak.QueueWorker):
    """Takes received CoT from PyTAK's receive queue and posts batches to Foresignal."""

    def __init__(self, queue, config, session):
        super().__init__(queue, config)
        self.inbox = queue
        self.session = session
        self.batch = []
        self.url = config.get("FORESIGNAL_URL").rstrip("/") + "/v1/ingest/" + config.get("FORESIGNAL_SOURCE",
                                                                                          "custom-tak")
        self.headers = {"Authorization": "Bearer " + config.get("FORESIGNAL_INGEST_KEY"),
                        "Content-Type": "application/json"}
        self.batch_size = int(config.get("BATCH_SIZE", "200"))
        self.batch_seconds = float(config.get("BATCH_SECONDS", "2"))

    async def handle_data(self, data):
        """Parses one received event and queues its record; malformed input is skipped."""
        try:
            text = data.lstrip()
            item = from_xml(text) if text.startswith(b"<") else from_proto(data)
        except Exception as e:  # noqa: BLE001 - untrusted network input
            LOG.debug("Skipping an unparseable CoT event: %s", e)
            return
        if item is not None:
            self.batch.append(item)
        if len(self.batch) >= self.batch_size:
            await self.flush()

    async def flush(self):
        """Posts the queued records; on failure they are dropped (the TAK feed is live, not replayable)."""
        if not self.batch:
            return
        records, self.batch = self.batch, []
        try:
            async with self.session.post(self.url, json={"records": records}, headers=self.headers,
                                         timeout=aiohttp.ClientTimeout(total=30)) as response:
                if response.status >= 300:
                    LOG.warning("Foresignal answered %s: %s", response.status, await response.text())
        except aiohttp.ClientError as e:
            LOG.warning("Posting %d records failed: %s", len(records), e)

    async def run(self, number_of_iterations=-1):
        """Drains the queue, posting at least every BATCH_SECONDS."""
        while True:
            try:
                data = await asyncio.wait_for(self.inbox.get(), timeout=self.batch_seconds)
                await self.handle_data(data)
            except asyncio.TimeoutError:
                await self.flush()


async def main():
    """Configures PyTAK from the environment, adds the ingest worker and runs until stopped."""
    parser = ConfigParser(interpolation=None)
    parser["bridge"] = {k: v for k, v in os.environ.items() if k.isupper()}
    config = parser["bridge"]
    for required in ("COT_URL", "FORESIGNAL_URL", "FORESIGNAL_INGEST_KEY"):
        if not config.get(required):
            raise SystemExit("Set " + required)
    async with aiohttp.ClientSession() as session:
        clitool = pytak.CLITool(config)
        await clitool.setup()
        clitool.add_tasks({IngestWorker(clitool.rx_queue, config, session)})
        await clitool.run()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    asyncio.run(main())
