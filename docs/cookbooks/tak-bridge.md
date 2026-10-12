# Cookbook: TAK bridge (Cursor on Target into Foresignal)

A receive-only bridge that reads Cursor on Target (CoT) events from your TAK network and stores
them as Foresignal records, so watches, the map, history and briefings work on TAK positions. It
never transmits on the TAK network.

Artifacts: [`integrations/pytak/foresignal_tak_bridge.py`](../../integrations/pytak/foresignal_tak_bridge.py)
and its pinned [`requirements.txt`](../../integrations/pytak/requirements.txt). The script lives
outside the Java application and is not part of the Foresignal build.

**Token scope:** an **ingest key** (`eik_…`) bound to the `custom-tak` source; no API token. The
admin token is used once to create the source.

**URLs used:** `/v1/ingest/{sourceId}` (the bridge), and for setup `/v1/custom-sources` and
`/v1/sources/{sourceId}`.

## Dependencies (verified on PyPI, 2026-10-12)

| Package | Version | License | Role |
|---|---|---|---|
| [PyTAK](https://pypi.org/project/pytak/) | 7.6.1 | Apache-2.0 | TAK Server and multicast connections, the receive queue |
| [takproto](https://pypi.org/project/takproto/) | 3.0.1 | MIT | TAK Protocol v1 (protobuf) decoding, only with `TAK_PROTO` |
| [defusedxml](https://pypi.org/project/defusedxml/) | 0.7.1 | PSF License | XML parsing that refuses entity expansion and external references |
| [aiohttp](https://pypi.org/project/aiohttp/) | 3.14.5 | Apache-2.0 and MIT | POSTs to Foresignal |

Python 3.10 or later. `pip install -r integrations/pytak/requirements.txt` in a virtual
environment.

## 1. Create the source

```sh
curl -fsS -X POST http://foresignal.local:8080/v1/custom-sources \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{
    "id": "custom-tak", "name": "TAK", "kinds": ["cot-event"], "private": true,
    "recordTtl": "10m", "observedAtMode": "UPSTREAM", "ttlAnchor": "OBSERVED",
    "titleField": "callsign", "recordsPerMinute": 6000}'
curl -fsS -X PUT http://foresignal.local:8080/v1/sources/custom-tak \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"retention": "3d"}'
```

- `private: true`: TAK positions are hidden from anonymous callers and from tokens without private
  access.
- A short `recordTtl` (10 minutes after the observation) drops units that stop reporting; a CoT
  `stale` time, when present, sets `validUntil` directly.
- No `embedFields`: nothing from this source is embedded for semantic search (a private source
  cannot declare them anyway).
- Keep the `ingestKey` from the response; it is shown once.

## 2. Run the bridge

```sh
export FORESIGNAL_URL=http://foresignal.local:8080 FORESIGNAL_SOURCE=custom-tak FORESIGNAL_INGEST_KEY=eik_…
# Preferred: your TAK Server's TLS streaming port, with a client certificate it issued.
export COT_URL=tls://takserver.lan:8089 PYTAK_TLS_CLIENT_CERT=/certs/bridge.pem \
       PYTAK_TLS_CLIENT_KEY=/certs/bridge.key PYTAK_TLS_CLIENT_CAFILE=/certs/ca.pem
python3 integrations/pytak/foresignal_tak_bridge.py
```

Or listen to the Situational Awareness multicast group directly:

```sh
export COT_URL=udp://239.2.3.1:6969
```

(PyTAK's own default, `udp+wo://239.2.3.1:6969`, is write-only and receives nothing.) In a
container, multicast needs `network_mode: host`.

## Mapping

| CoT | Foresignal |
|---|---|
| `event@uid` | `externalId` |
| `event@time` | `observedAt` |
| `event@stale` | `validUntil` |
| `point@lat`, `point@lon` | `geometry` (Point); `9999999` and out-of-range values mean none |
| `event@type`, `event@how`, `point@hae`, `point@ce`, `point@le` | `payload.type`, `.how`, `.hae`, `.ce`, `.le` |
| `detail/contact@callsign` | `payload.callsign` (the title) |
| — | `kind: cot-event` |

Records are posted in batches (`BATCH_SIZE`, default 200, at least every `BATCH_SECONDS`, default
2) as `{"records": [...]}` to `/v1/ingest/custom-tak`. Unparseable events are skipped. A failed
POST drops that batch: the feed is live and is not replayed.

A watch then works like any other, for example `{"sources": ["custom-tak"], "mode": "enter",
"geofence": {...}}` for a unit entering an area, or a condition on `type` (`startsWith a-h` for
hostile tracks).

## Limits

- Ingest: 5 MB and 10,000 records per request; 429 `INGEST_RECORD_RATE` above `recordsPerMinute`.
  A busy SA network can exceed the default; raise it or filter on your TAK Server.
- One bridge per TAK connection; it holds no state and can be restarted at any time.
- TAK Protocol v1 (protobuf) needs `TAK_PROTO` set as PyTAK documents and the `takproto` package.

## Privacy and security

- **Plain UDP CoT (multicast or unicast) is unauthenticated and spoofable**: anyone on the network
  segment can inject positions under any `uid` and callsign. Treat multicast-sourced records
  accordingly; prefer the TLS connection to a TAK Server, where clients authenticate with
  certificates.
- XML is parsed with `defusedxml` only; the script never uses `xml.etree` directly.
- Positions of people are personal data: keep the source private, keep retention short, and never
  bridge a TAK network you are not authorized to read.
