# Foresignal Operator Guide

How to run and tune a Foresignal instance through its API. Later prompts add sections for their
features. Every write below needs the admin token: `Authorization: Bearer fsr_…`.

The [README](../README.md) has the five-minute quickstart. This guide covers everything after it:
the token, sources, policies and retention, triggers and channels, inbound webhooks and external
data providers, streaming for frontends, and
running the host (storage, clock, binds, remote access, mDNS, backups and troubleshooting).

## First start and the admin token

`docker compose up -d` starts PostGIS and the broker. On the first start the broker generates an
admin token and prints it **once**, in a delimited block:

```sh
docker compose logs foresignal | grep -A3 "FORESIGNAL ADMIN TOKEN"
```

The token starts with `fsr_`. Only its SHA-256 hash is stored, so it cannot be shown again: put it
in a password manager. Send it as `Authorization: Bearer fsr_…`.

- **Rotate** (you still have the token): `POST /v1/system/admin-token/rotate` returns a new token
  once; the old one stops working immediately.
- **Reset** (you lost it): restart once with `FORESIGNAL_RESET_ADMIN_TOKEN=true`
  (for example `FORESIGNAL_RESET_ADMIN_TOKEN=true docker compose up -d foresignal` after adding the
  variable to the service's `environment`), read the new token from the log, then remove the
  variable again, or every restart replaces the token.

Data reads (`GET` of records, changes, tiles and alerts, and the streams) need no token while the
`security` setting's `openReads` is on, which is the default on a trusted network. Every write, the
fetch log and `/v1/system`'s full document need a token. The admin token is the `api_token` row
named `bootstrap`; give agents, dashboards and phones their own scoped tokens instead (see
[Tokens, pairing and the audit log](#tokens-pairing-and-the-audit-log)). An install upgraded from
before prompt 023 keeps its admin token: it moves into that row on the first start.

## Sources

A source is one upstream feed, such as `usgs-earthquakes`. Every source is installed **disabled**.
Enabling it is the only thing that makes Foresignal contact that upstream; reading or streaming data
never triggers a fetch.

| Call | What it does |
|---|---|
| `GET /v1/sources` | Every source, with status, tier, license and today's budget use |
| `GET /v1/sources/{id}` | Descriptor, settings and health of one source |
| `PUT /v1/sources/{id}` | Partial settings change (admin) |
| `PUT` / `DELETE /v1/sources/{id}/credentials` | Store or remove upstream keys (admin; never returned) |
| `POST /v1/sources/{id}/fetch` | Fetch once now, ingesting normally (admin; uses budget) |
| `POST /v1/sources/{id}/probe` | Diagnostic fetch that never ingests (admin; uses budget) |
| `GET /v1/sources/health`, `GET /v1/sources/{id}/health` | Health, coverage and storage cost |
| `GET /v1/sources/{id}/usage` | Requests, bytes and ingest counters over time |
| `GET /v1/sources/{id}/fetches` | Per-fetch provenance log (admin) |

Enable a source, with a slower poll and a smaller budget:

```json
PUT /v1/sources/usgs-earthquakes
{"enabled": true, "interval": "5m", "budget": {"perMinute": 2, "perDay": 500}}
```

Omitted fields are left unchanged. A source that needs credentials returns `409 CREDENTIALS_REQUIRED`
until you store them with `PUT /v1/sources/{id}/credentials`.

### Credentials and budgets

- **Credentials:** `PUT /v1/sources/{id}/credentials` with the fields the descriptor lists under
  `credentialFields` (for example `{"apiKey": "…"}`). They are encrypted with AES-256-GCM under the key
  in `${FORESIGNAL_DATA_DIR}/secret.key` and are never returned; a source only shows
  `credentialsSet: true`. `DELETE` removes them.
- **Budgets:** every source has a request budget (`budget.perMinute`, `budget.perDay`) that the
  broker never exceeds. A refused request is logged as a `REFUSED` fetch row, a manual fetch over
  budget answers `429`, and the scheduler simply waits. Scheduled polls, manual fetches and probes
  all spend the same budget. `budgetUsedToday` is in `GET /v1/sources`; the day is the UTC day.
- **Interval:** `interval` (shorthand such as `60s`, `5m`, `1h`, or ISO-8601) cannot go below the
  descriptor's minimum. An enable or interval change applies within seconds; the first poll after
  an enable is immediate.

### Health, usage and the fetch log at a glance

| Question | Call |
|---|---|
| Is it working? | `GET /v1/sources/{id}/health`: `status` (`healthy`, `degraded`, `failing`, `idle`, `disabled`), `statusReason`, `lastSuccessAt`, `lastError`, `consecutiveFailures`, `coverage` and `storage` |
| Which sources need attention? | `GET /v1/sources/health?status=failing,degraded` |
| How much is it costing upstream? | `GET /v1/sources/{id}/usage?resolution=hour` (requests, bytes, errors, refused, records seen, versions, touches) |
| What did each fetch do? | `GET /v1/sources/{id}/fetches` (below) |
| What does the upstream send right now? | `POST /v1/sources/{id}/probe` (a diagnostic fetch that never ingests) |

### Aircraft sources

Prompt 062 adds the global aircraft sources, all disabled until you enable them:

| Source | What it covers | Default interval · budget |
|---|---|---|
| `adsblol-mil` | Aircraft flagged military in adsb.lol's database, worldwide | 30 s · 4/min, 3,000/day |
| `adsblol-area-1`, `-2`, `-3` | Every aircraft within `radiusNm` (≤ 250 nm, default 250) of a point you set | 60 s · 2/min, 1,500/day each |
| `opensky-states` | Worldwide civil aircraft from the OpenSky Network | 300 s · 2/min, 1,500/day |

There is no worldwide adsb.lol source: the adsb.lol API documents a 250 nm limit, so worldwide civil coverage comes from
OpenSky. Set an area slot's center before enabling it:

```json
PUT /v1/sources/adsblol-area-1
{"enabled": true, "params": {"lat": 41.98, "lon": -87.90, "radiusNm": 250}}
```

All five are TRACK sources: positions are sampled (a version when an aircraft deviates 1 km from its dead-reckoned
track, turns 20° or changes altitude by 300 m, and at least every 240 s), and a callsign, registration, type, squawk,
emergency, ground state or navigation-quality change is always kept. An aircraft that stops reporting expires after its
TTL (6 minutes for adsb.lol, 15 for OpenSky). With those TTLs the 240 s keyframe stays valid for intervals up to 90 s
(adsb.lol) and 630 s (OpenSky); if you poll more slowly, also shorten the keyframe in the source's `ingestPolicy`.

### adsb.lol access

adsb.lol is a community network; its API is free. Its terms (the description in `https://api.adsb.lol/api/openapi.json`
and `https://github.com/adsblol/api`) say: "In the future, you will require an API key which you can get by feeding to
adsb.lol." Feeding (running a receiver that sends data to adsb.lol) is the supported route to continued access, and it
also gives access to adsb.lol's re-api and raw aggregated data. The data is ODbL: share-alike, with attribution
("adsb.lol").

Foresignal identifies itself on every request (`Foresignal/<version> (+https://foresignal.ai; install=<id>)`), honors
`Retry-After` and waits 30 s after a 429 that has none. If adsb.lol answers **HTTP 451**, or **403**, Foresignal treats it as
a refusal of this installation:

- the source goes into a **one-hour backoff** and is retried hourly, never faster;
- `GET /v1/sources/{id}/health` shows `failing` with the reason "upstream refused this client (HTTP 451); see Operator
  Guide → adsb.lol access";
- Foresignal never changes its User-Agent to look like a browser, adds browser headers or switches to another address to
  get past the refusal.

What to do: feed adsb.lol, or ask its maintainer (contact details in the API repository). When a key scheme is published,
a later Foresignal release adds an optional `apiKey` credential; until then these sources take no credentials. An HTTP 200
whose body is HTML rather than JSON is also treated as throttling: the fetch fails with "non-JSON 200, treated as
throttling" and nothing is ingested.

**adsb.fi and airplanes.live (prompt 080)** follow the same rules, and the generic refusal reason above points here for
them too:

- `adsbfi-mil` and `adsbfi-region-1..4` share the host `opendata.adsb.fi`, which allows one request per second and
  temporarily restricts an address that keeps making invalid requests (400, 401, 403, 404, 429). Foresignal keeps
  requests to that host strictly serial, at least 1.1 s apart, across all five sources; when a request cannot start
  within 30 s the run is skipped with the reason "host pacing" and nothing is sent. adsb.fi data is for personal,
  non-commercial use, with attribution to adsb.fi; the sources are `LOCAL_ONLY` by default.
- `airplaneslive-mil` is refused today: airplanes.live answers every anonymous request with HTTP 403 and asks projects to
  email `contact@airplanes.live` with links and a description. Health then reads "upstream refused this client (HTTP
  403): access approval required (contact airplanes.live)", and the source is retried hourly. Enable `adsbfi-mil` for
  military aircraft without that step.

### OpenSky credentials and credits

`opensky-states` runs anonymously unless you store API client credentials. Create an API client on your OpenSky account
page and store both values together (one without the other is refused with `400`):

```json
PUT /v1/sources/opensky-states/credentials
{"clientId": "…", "clientSecret": "…"}
```

Foresignal exchanges them for an OAuth2 token (valid 30 minutes, reused and refreshed a minute early) and never sends
basic authentication, which OpenSky no longer accepts. A refused client id or secret shows as failing with "OpenSky refused
the client credentials".

OpenSky meters `/states/all` in **credits**: 400 a day anonymous, 4,000 for a standard account, 8,000 for an active feeder
and 14,400 for a licensed account (refilled hourly, per OpenSky's documentation). One call costs 1 to 4 credits by the area
of the `bbox` parameter (≤ 25 sq° 1, ≤ 100 2, ≤ 400 3, larger or the whole world 4). So:

- a standard account polling the world can go down to the 90 s floor (`86,400 / (4,000 / 4)` ≈ 86 s);
- **anonymous world polling needs an interval of at least 900 s** (400 / 4 = 100 calls a day); a small `bbox`, such as
  `"bbox": "5,45,10,48"` (1 credit), allows more.

After every response Foresignal stores OpenSky's `X-Rate-Limit-Remaining`. When today's balance (UTC day) would drop below
the call's cost plus `creditReserve` (a parameter, default 40), the fetch is not sent: the fetch log shows a `REFUSED` row
("upstream quota deferred", with the retry time, the next UTC midnight) and health shows `degraded`, not `failing`. A 429
with `X-Rate-Limit-Retry-After-Seconds` puts the source in backoff for that long.

OpenSky's data is for non-commercial research under its terms (`https://opensky-network.org/about/terms-of-use`; read
them yourself, the page does not answer automated requests). The source is `egress: LOCAL_ONLY`: its records are served to
your own API clients, tiles, streams and notifications, but never re-served by sinks, federation, share links or feeds.
Credit OpenSky as its descriptor's `attribution` asks (Schäfer et al., IPSN 2014).

### Vessels from aisstream.io

`aisstream` streams global AIS (vessel positions and static data) over a WebSocket and merges each vessel's messages
into one complete record per MMSI. It needs a key, and stays disabled until one is stored (enabling without it is a
`409 CREDENTIALS_REQUIRED`):

1. Create a free account at <https://aisstream.io>, then an API key on its Account page. The key is shown once.
2. Store it and enable the source:

   ```json
   PUT /v1/sources/aisstream/credentials
   {"apiKey": "…"}
   ```

   ```json
   PUT /v1/sources/aisstream
   {"enabled": true, "params": {"bbox": ["-1.8,50.6,-0.9,50.95"]}}
   ```

**Use one key per Foresignal instance, and nowhere else.** aisstream allows 3 subscribed connections per account and 3
open connections per IP, and Foresignal holds one connection per source. A key shared with another program or another
instance makes connections fail in ways that look like a bad key. Storing a new key restarts the stream at once.

**Size the `bbox` to what you need.** `bbox` takes 1 to 10 boxes, each `"minLon,minLat,maxLon,maxLat"`, as a JSON array
of strings (a single string would be split at its commas); the default is the whole world. The world is thousands of
messages a minute. Since September 2026 aisstream limits the bandwidth of connections that do not use compression and
silently drops the excess; Foresignal negotiates `permessage-deflate`, and `GET /v1/sources/aisstream/health` shows
`details.compressionEnabled` once aisstream confirms it. If it shows `false`, narrow the boxes. A box must not cross the
antimeridian: split it in two. `messageTypes` limits the AIS message types (default all five the merge uses) and `mmsi`
follows up to 50 vessels only. Changing `params` while the stream runs resends the subscription on the same connection.

**Health.** A connection that has delivered no AIS message for 120 s shows `degraded` ("connected but silent for N
s"); after 300 s Foresignal drops it and reconnects on the ladder (5 s, 15 s, 60 s, 300 s, then every 900 s). A rejected
key (an error naming the key, or two closes in a row before aisstream confirms the subscription; aisstream answers an
invalid key by closing the socket) is retried only hourly; fix the key and store it again. Each connection attempt counts
against the budget (2 a minute, 144 a day).

aisstream publishes no terms of use or redistribution clause, and asks you to proxy only what your clients need, so the
source is `egress: LOCAL_ONLY`, like `opensky-states`: served to your own clients, never re-served by sinks, federation,
share links or feeds. No key means no vessels from this source; keyless official AIS (`ais-kystverket`,
`ais-digitraffic`) and your own AIS-catcher (`local-ais-{n}`) need no key (next section).

### Keyless AIS and your own AIS-catcher

Prompt 085 adds three vessel sources that need no key, all disabled until you enable them. They emit the same
`vessel` records as `aisstream` (the same payload keys, the same SAMPLED policy and 30-minute TTL), decoded by
Foresignal's own AIVDM codec.

- **`ais-kystverket`** reads the Norwegian Coastal Administration's open AIS stream (`153.44.253.27:5631`, NMEA with
  TAG blocks) over raw TCP, receive-only. It covers the Norwegian economic zone and the Svalbard and Jan Mayen
  protection zones; fishing vessels under 15 m and leisure craft under 45 m are left out by Kystverket. The data are
  under the Norwegian Licence for Open Government Data (NLOD 2.0), so the source is `OPEN`; credit "Contains data
  under the Norwegian licence for Open Government data (NLOD) distributed by the Norwegian Coastal Administration"
  wherever you show it. **Best effort:** in October 2026 the endpoint was still listed as open, but connections from
  outside Norway (a US host, and the build host) timed out. While it is unreachable the source shows `failing`, and
  Foresignal backs off from 30 s to 15 minutes (at most 12 connects an hour); it never tries another route.
- **`ais-digitraffic`** reads Fintraffic's Digitraffic marine MQTT over WebSocket (Finland and the northern Baltic)
  and fetches the vessel metadata list every 12 hours. Location fixes older than 24 hours are dropped. If
  Digitraffic's own status topic has not been updated for 10 minutes, the source shows `degraded`. CC BY 4.0, `OPEN`;
  credit "Source: Fintraffic / digitraffic.fi, licence CC 4.0 BY".
- **`local-ais-{n}`** reads your own AIS-catcher over TCP on your network (next paragraphs).

```json
PUT /v1/sources/ais-digitraffic
{"enabled": true}
```

**Your AIS-catcher as a TCP server.** Start AIS-catcher with `-S <port>`, which serves its NMEA lines to TCP clients
on that port (for example `AIS-catcher -S 5012` plus your usual device options), then point a slot at it. The port is always explicit; there is
no default:

```json
PUT /v1/sources/local-ais-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.30:5012", "stationName": "rooftop"}}
```

Foresignal only reads: nothing is ever written to the socket. The host must be on your network (a public address is
refused). `stationName` (default `local`) names your station as the observer of every record, and
`GET /v1/sources/local-ais-1/health` shows `details.messagesPerMin`, `details.distinctMmsi10m`,
`details.lastMessageAt` and `details.vessels`. A connection silent for 120 s is closed and reopened (5 s to 5 minutes).
The slots are `egress: LOCAL_ONLY`; you may set them to `OPEN`.

**Your station's position stays private by default.** AIS-catcher's NMEA carries no station position, and Foresignal
stores none unless you ask: set `publishStationPosition` to `true` together with `stationLat` and `stationLon` to get
one `ais-receiver` record on the map. Without `publishStationPosition`, `stationLat` and `stationLon` are refused
(`400 VALIDATION_FAILED`), so the position is never stored or emitted.

For an AIS-catcher somewhere else (a remote site that can reach Foresignal but not the other way round), the
`aiscatcher` HTTP push preset ("Inbound webhooks") remains the alternative.

### Local RF decoders

Prompt 086 brings in your own SDR decoders: aviation datalink metadata, radiosondes, a GNSS integrity sentinel, drone
Remote ID, Raspberry Shake triggers and NOAA Weather Radio SAME headers. Every source is disabled until you enable it,
reads only (it never writes to your decoders, except gpsd's watch command), and accepts only addresses on your network
with an explicit port. The legal and privacy rules below are enforced in code; there is no setting to turn them off.

**Aviation datalink (`local-acars-{n}`).** Run [acars_router](https://github.com/sdr-enthusiasts/acars_router) in front
of acarsdec, dumpvdl2 and dumphfdl; its TCP servers default to 15550 (ACARS), 15555 (VDLM2) and 15556 (HFDL). Give a slot
one, two or all three:

```json
PUT /v1/sources/local-acars-1
{"enabled": true, "params": {"acarsEndpoint": "tcp://192.168.1.40:15550", "vdlm2Endpoint": "tcp://192.168.1.40:15555",
 "hfdlEndpoint": "tcp://192.168.1.40:15556", "stationId": "home"}}
```

**Legal note on datalink content.** Foresignal stores only message metadata (protocol, registration, flight, label,
mode, block and message numbers, frequency, signal level, ICAO address, ground station, and whether the message had
text). The message text is dropped before a record is built and is never stored, logged, embedded or forwarded:
interception-disclosure laws such as US 18 U.S.C. §2511 and the UK Wireless Telegraphy Act 2006 s.48 make passing on
the content of these messages unlawful. The slots are **private by definition**: `PUT … {"private": false}` is refused,
their records never reach anonymous readers, tiles, embeddings, a remote LLM, MCP or Ask tools for callers without the
private grant, or a sink, and their `LOCAL_ONLY` egress cannot be changed (`400 EGRESS_FIXED`). They are kept 7 days.
Geometry appears only when an HFDL position report was decoded.

**Radiosondes (`local-sonde`).** radiosonde_auto_rx broadcasts `PAYLOAD_SUMMARY` datagrams (its
`payload_summary_port` defaults to 55673). Listen for them and name your station:

```json
PUT /v1/sources/local-sonde
{"enabled": true, "params": {"endpoint": "udp://0.0.0.0:55673", "allowSenders": ["192.168.1.41"],
 "stationCallsign": "N0CALL-1"}}
```

`stationCallsign` becomes the observer of every record (otherwise the station auto_rx names), with the sonde's
frequency; the records are the same `radiosonde` records as `sondehub`.

**GNSS integrity sentinel (`local-gpsd-{n}`).** The gpsd slot from "Local receivers" also judges the integrity of
your GNSS every 30 seconds against a 15-minute baseline and writes one `gnss-integrity` record per receiver:
`LOST` (no fix for more than a minute), `JAMMED` (the receiver reports jamming, or signal strength fell 10 dB while
half the satellites were lost), `SUSPECT_SPOOF` (a fixed antenna whose position jumped more than 500 m or whose clock
stepped more than a second while signals grew stronger), `DEGRADED` (accuracy three times worse than usual, or fewer
than four satellites) or `OK`. Set `fixedStation` to `false` for a moving antenna (the spoofing rule then stays off),
`stationLabel` to name the station in the log, and `ubxMonitor` to `true` with a u-blox receiver to read its own
jamming indicator from gpsd's raw passthrough:

```json
PUT /v1/sources/local-gpsd-1
{"params": {"endpoint": "tcp://192.168.1.60:2947", "fixedStation": true, "ubxMonitor": true, "stationLabel": "roof"}}
```

Foresignal writes only gpsd's `?WATCH` command (with `"raw":2` for `ubxMonitor`); every write passes an allowlist of
`?WATCH`, `?VERSION`, `?DEVICES` and `?POLL`, so nothing can reconfigure your receiver. A state change is logged once at
INFO. The records stay private with the rest of the slot and are kept 30 days. Trigger on `integrityState` like any
significant field.

**Drone Remote ID (`local-remoteid`).** A WarDragon kit running [DragonSync](https://github.com/alphafox02/DragonSync)
publishes drones to an MQTT broker. Point the source at your broker (credentials go in
`PUT /v1/sources/local-remoteid/credentials` as `username` and `password`):

```json
PUT /v1/sources/local-remoteid
{"enabled": true, "params": {"brokerUrl": "mqtt://192.168.1.10:1883", "topics": ["wardragon/drones"]}}
```

**Remote ID privacy defaults.** Only ASTM F3411 / ASD-STAN Remote ID (Wi-Fi and Bluetooth) is ingested: DJI DroneID
detections are proprietary and are dropped (counted in `foresignal.remoteid.dropped{reason="dji"}`). Pilot and home
positions are removed before a record is built, so they are never stored. If you want the pilot position and your
deployment allows it, make the source private and set `keepOperatorPosition` in the same request:

```json
PUT /v1/sources/local-remoteid
{"private": true, "params": {"brokerUrl": "mqtt://192.168.1.10:1883", "keepOperatorPosition": true}}
```

It is then kept as payload `operatorPoint`, never as the record's geometry. On a source that is not private the
parameter is refused with `400 PRIVATE_REQUIRED`, and so is making the source public while it is set. Drones are TRACK
records that expire 3 minutes after their last report.

**Raspberry Shake (`rsudp-alarm` preset).** Create a custom source from the preset with your Shake's position, then
install `contrib/rsudp/foresignal_alarm.py` as rsudp's `custom` code file (instructions in `contrib/rsudp/README.md`).
Each STA/LTA alarm becomes a `seismic-trigger` record at the station point. rsudp runs custom code only on an alarm; a
reset needs your own wrapper. Unverified against a real Shake.

**NOAA Weather Radio SAME (`same` format).** Create a custom source with the kind `same-alert`, then run the recipe in
`contrib/same/` (`rtl_fm … | multimon-ng -a EAS -t raw - | foresignal-post`). Each header becomes one `same-alert`
record drawn on the named counties (the whole state for county `000`) when the reference data is loaded, valid until the
header's purge time; the three repetitions of a header collapse into one, and `NNNN` is ignored.

### Aviation safety

Prompt 087 adds the US aviation-safety picture. Every source is disabled until you enable it; all are public-domain US
Government data.

| Source | What it gives | Notes |
|---|---|---|
| `faa-tfr` | Temporary flight restrictions with their real polygons, floors, ceilings and validity | Times come from the XNOTAM in UTC. A TFR that starts later is stored with its `validFrom` and matches `forecastHorizon` rules only from then; one without an end stays until the FAA removes it from the list (then it is tombstoned). |
| `faa-nas-status` | Ground delay programs, ground stops, arrival and departure delays, closures, deicing | Placed at the airport from the bundled US airport table; an airport missing from it gives a record without a position. |
| `awc-sigmets`, `awc-gairmets`, `awc-cwas` | SIGMETs and AIRMETs, G-AIRMET snapshots, Center Weather Advisories | A canceled advisory is tombstoned at the next poll. A G-AIRMET snapshot becomes valid at its `validTime`. |
| `awc-pireps` | Pilot reports of the last two hours inside a box | See "Choosing a PIREP box" below. |
| `awc-tafs` | Terminal Aerodrome Forecasts, one per station | `scope` (`aoi`, `conus`, `global`) reads the AWC cache file; `stations` reads the TAF API for just those stations. METARs remain `awc-metars`. |
| `faa-uasfm` | The FAA UAS Facility Map grid: the altitude up to which the FAA may authorize drone flight near airports | `bbox` is required (the grid is national: about 380,000 cells). |
| `faa-uas-*` | National security UAS restrictions (permanent and part-time), recreational flyer sites, stadiums, prohibited areas, national defense TFR areas | Feature-kit sources, read daily. |

**AWC's limits.** The Aviation Weather Center limits every client to **100 requests per minute**, asks you to wait
between requests (about one per minute per endpoint) and blocks clients that exceed it. The defaults stay far inside:
all six AWC sources together budget 10 requests per minute, none polls faster than every 2 minutes, requests run one
after another, and a 429 puts the source in backoff for the time AWC names in `Retry-After`. If you raise an AWC
source's budget or shorten its interval, keep the sum of the six well under 100 per minute (other software on the same
public address counts too).

**Choosing a PIREP box.** AWC refuses a PIREP query without a box and returns at most 400 reports per query. The default
box is the conterminous US (`-130,20,-60,55`, west,south,east,north), which can exceed 400 reports in a busy afternoon,
so the oldest reports of the two-hour window are then missing. Set `bbox` to the region you care about (for example
your places plus a few hundred kilometers) to keep busy periods complete; Foresignal sends it to AWC latitude first.

**The UAS Facility Map** is read in full for your box on the first run and every 56 days (the FAA chart cycle); weekly
runs in between ask only for cells edited since the last run. A larger box costs more requests on full reads
(2,000 cells per request, at most 50,000 cells per run, `maxFeatures`).

**NOTAMs are not collected.** The FAA moved NOTAM distribution to the NOTAM Management Service (cutover reported for
18 April 2026); its API needs an approval requested from `notams@faa.gov` and its endpoints are not publicly documented.
TFRs, the NOTAMs that matter most on a map, come from `faa-tfr`.

### Cyber feeds and Certificate Transparency

Prompt 076 adds five sources, all disabled until you enable them:

- **`cisa-kev`** (the CISA Known Exploited Vulnerabilities catalog) and **`abusech-feodo`** (botnet C2 servers)
  need no credential.
- **`abusech-urlhaus`** needs a free abuse.ch Auth-Key from `auth.abuse.ch`:
  `PUT /v1/sources/abusech-urlhaus/credentials` with `{"authKey": "…"}`. Malware URLs are stored as text and as a
  defanged copy (`hxxp://`, `[.]`); Foresignal never fetches them, and `fieldSpecs` marks both `noLink`, so clients
  must not render them as links. Commercial or for-profit use may require a Spamhaus subscription.
- **`nvd-cves`** syncs NVD CVEs every two hours, filtered by `minCvss` (default 7.0) and optionally `vendors`
  (CPE vendor names such as `microsoft`); KEV CVEs are always kept. An optional free NVD key (`apiKey`) removes
  the 6-second pause between requests. Each kept CVE also gets a `cve-epss` record with its EPSS probability and
  percentile as bands only (`epssBand`, `percentileBand`), refreshed daily for `epssRefreshDays` (default 30).
  Notices: "This product uses the NVD API but is not endorsed or certified by the NVD." and "EPSS scores from
  FIRST.org (first.org/epss)".
- **`ct-domain-watch`** lists certificates issued for your own domains through the SSLMate Cert Spotter API:
  set `domains` (up to 25; subdomains and matching wildcards are included) and, optionally, `expectedIssuers`
  (issuer names or CAA domains such as `letsencrypt.org`). An issuance from any other issuer sets
  `unexpectedIssuer: true`, which a rule can alert on. Unauthenticated use is limited to a few queries an hour for
  personal or evaluation use; for more, set a Cert Spotter key (`apiKey`) and raise the source's budget. Domains
  are polled round-robin, so a throttled run resumes where it stopped.

**crt.sh is never polled, and Foresignal ships no CT log client.** If you prefer not to use the Cert Spotter API,
run SSLMate's open-source monitor [`certspotter`](https://github.com/SSLMate/certspotter) (MPL-2.0) yourself and let
its hook script post each discovery to a custom PUSH source (see "Inbound webhooks"):

```json
POST /v1/custom-sources
{"id": "custom-ct", "name": "Certificate Transparency (certspotter)", "kinds": ["certificate-issuance"],
 "observedAtMode": "RECEIVED", "titleField": "watchedDomain"}
```

Put your domains in `~/.certspotter/watchlist` (one per line; a leading dot includes subdomains) and this script in
`~/.certspotter/hooks.d/foresignal` (executable; it needs `jq` and `curl`). `certspotter` sets the environment
variables for each `discovered_cert` event:

```sh
#!/bin/sh
[ "$EVENT" = "discovered_cert" ] || exit 0
jq -n --arg id "$TBS_SHA256" --arg domain "$WATCH_ITEM" --arg cert "$CERT_SHA256" \
      --arg pubkey "$PUBKEY_SHA256" --arg issuer "$ISSUER_DN" --arg nb "$NOT_BEFORE_RFC3339" \
      --arg na "$NOT_AFTER_RFC3339" --arg log "$LOG_URI" \
  '{records: [{externalId: $id, kind: "certificate-issuance", eventTime: $nb, validUntil: $na,
               payload: {watchedDomain: $domain, issuerDn: $issuer, notBefore: $nb, notAfter: $na,
                         certSha256: $cert, pubkeySha256: $pubkey, logUri: $log}}]}' |
curl -fsS -X POST "http://foresignal.local:8080/v1/ingest/custom-ct" \
     -H "X-Foresignal-Ingest-Key: $FORESIGNAL_INGEST_KEY" -H "Content-Type: application/json" --data-binary @-
```

The external id is the TBS digest, which a certificate and its precertificate share, so the pair becomes one
entity. `ISSUER_DN` is the issuer's distinguished name as the CA wrote it; Cert Spotter's curated issuer names are
not available to the self-hosted monitor.

### Local receivers

Prompt 069 adds sources for hardware you run yourself on your own network. Each family has
`foresignal.local-receivers.slots` numbered slots (default 2, at most 16; environment
`FORESIGNAL_LOCAL_RECEIVERS_SLOTS`), all disabled until you set an address and enable them:

| Source | Reads | Default interval · budget |
|---|---|---|
| `local-adsb-{n}` | readsb, dump1090 or skyaware978 `aircraft.json` (`band` 1090 or 978) | 2 s · 60/min, 50,000/day |
| `local-sbs-{n}` | SBS-1 (BaseStation) lines over TCP | stream · 2 connects/min, 200/day |
| `local-gpsd-{n}` | your GNSS receiver through gpsd (private) | stream · 2 connects/min, 200/day |
| `station-readsb-{n}` | readsb `stats.json`: messages per second, signal, noise, `state` ok, low-rate or silent | 60 s · 2/min, 2,000/day |
| `wxstation-davis-{n}` | a Davis WeatherLink Live console | 60 s (never under 10 s) · 6/min, 10,000/day |
| `wxstation-ecowitt-{n}` | an Ecowitt gateway's live data (experimental) | 60 s · 6/min, 10,000/day |

**Addresses.** Every address must resolve to a loopback, private (RFC 1918) or unique-local address. It is resolved
once when you save it and pinned, so a name that later resolves elsewhere cannot redirect Foresignal; redirects are
never followed. Sockets need an explicit port: no default is assumed.

```json
PUT /v1/sources/local-adsb-1
{"enabled": true, "params": {"url": "http://192.168.1.50/tar1090/data/aircraft.json", "band": "1090"}}

PUT /v1/sources/local-sbs-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.50:30003"}}

PUT /v1/sources/local-gpsd-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.60:2947"}}

PUT /v1/sources/station-readsb-1
{"enabled": true, "params": {"url": "http://192.168.1.50/tar1090/data/stats.json", "minMessagesPerSec": 1.0}}

PUT /v1/sources/wxstation-davis-1
{"enabled": true, "params": {"url": "http://192.168.1.70", "lat": 40.01, "lon": -105.27}}
```

**Enabling the receiver's outputs.**

- **readsb JSON:** readsb writes `aircraft.json` and `stats.json` with `--write-json` and serves them through
  tar1090 or its own web server (`http://host/tar1090/data/aircraft.json`); with `--net-api-port` they are on that
  port too. The path must end in `aircraft.json` (or `stats.json`).
- **readsb SBS:** off unless you start readsb with `--net-sbs-port 30003` (any port you choose; set it in
  `endpoint`).
- **dump978 / skyaware978:** skyaware978 serves UAT aircraft at `http://host:8978/data/aircraft.json` (or
  `/skyaware978/data/aircraft.json` behind the PiAware web server); set `band` to `978`.
- **gpsd:** listens on 2947 by default, on loopback only unless started with `-G` (or `GPSD_OPTIONS="-G"`); allow your
  Foresignal host in its firewall. Foresignal sends `?WATCH={"enable":true,"json":true};` and needs protocol 3.
- **Davis WeatherLink Live:** the local API is on by default; Davis asks clients not to poll more often than every
  10 s, which is the minimum interval.
- **Ecowitt:** `/get_livedata_info` has no official specification and changes with firmware; the parser is tolerant
  and counts ids it does not know (`foresignal.ecowitt.unknown_ids`). After a firmware update, watch the source's
  health for a format change. For Ecowitt's own push ("Customized upload"), use the `ecowitt` mapping preset instead.

**Firewalling UDP.** No source in this prompt listens on UDP, but later ones (AIS, APRS, radiosondes) will: a UDP
endpoint must name `allowSenders` (private IPs or CIDRs), and datagrams from anyone else are dropped unread and counted
(`foresignal.socket.udp.rejected`). Bind to the interface that faces your receivers rather than `0.0.0.0` where you
can, and keep the port closed to the internet in your router and host firewall.

**Why the data stays local.** Every local source is `egress: LOCAL_ONLY`: your own clients, tiles, SSE, MCP and
notifications see it, but sinks, federation, share links and public feed keys do not. This is your data, so unlike a
licensed source you may change it: `PUT /v1/sources/local-adsb-1 {"egress": "OPEN"}` (and `null` to go back); other
sources answer `400 EGRESS_FIXED`. `local-gpsd-{n}` is also **private**: it is your location, so it is never served to
anonymous readers, tiled, embedded or sent to a remote LLM. Weather stations store `lat`/`lon` rounded to about 1 km.

**MLAT terms.** Multilateration positions (`posSource: mlat`) are computed by the aggregator networks your receiver
feeds, under terms that forbid passing them on. Foresignal never sends an MLAT record on a sink, federation, share link
or feed, even from a source you opened, and strips them from anonymous `openReads` responses unless you set
`security.exposeMlat` to true (`PUT /v1/settings/security`). Receiver health: a local `aircraft.json` whose `now` is
more than 10 s old marks `local-adsb-{n}` degraded ("receiver feed stale"), and a `station-readsb-{n}` rule on
`state == silent` tells you when the antenna or dongle has stopped.

### Local air-quality monitors

Prompt 078 adds `airq-local-{n}`: one slot per local receiver slot (`foresignal.local-receivers.slots`, default 2),
each polling one PurpleAir, airrohr (Sensor.Community firmware) or AirGradient monitor on your network every 60 s
(30 s minimum, 4 requests/min, 3,000/day). Like the other local receivers, every slot is disabled until you set it up,
and the address follows the same rules: it must resolve to a loopback, private or unique-local address, it is resolved
once and pinned, and redirects are never followed. Use the monitor's IP address (give it a fixed DHCP lease) rather
than an mDNS `.local` name, which the Foresignal host may not resolve.

| `deviceType` | Request | Location | Indoor default |
|---|---|---|---|
| `purpleair` | `GET {url}/json?live=true` | the sensor's own `lat`/`lon`, or your `lat`/`lon` | from its `place` (`inside`/`outside`) |
| `airrohr` | `GET {url}/data.json` | your `lat` and `lon` (required) | outdoor |
| `airgradient` | `GET {url}/measures/current` | your `lat` and `lon` (required) | from the model: `I-…` indoor, `O-…` outdoor |

```json
PUT /v1/sources/airq-local-1
{"enabled": true, "params": {"deviceType": "airgradient", "url": "http://192.168.1.61", "lat": 40.01,
 "lon": -105.27, "name": "Living room"}}
```

`url` is the device's address only (no path, query or credentials); `indoor` (`true`/`false`) overrides the default;
`name` labels the slot in log lines. An indoor monitor's records are `indoor-air-quality`, an outdoor one's
`air-quality`, with `pm1, pm25, pm10, tempC, humidityPct, pressureHpa` and, on AirGradient, `co2Ppm, tvocIndex,
noxIndex`.

**Enabling each device's local API.**

- **PurpleAir:** the sensor serves its readings at `http://<ip>/json` once it is on your Wi-Fi; nothing needs turning
  on. Foresignal asks for `?live=true` (the newest readings rather than the two-minute average). PurpleAir asks for at
  least 10 s between requests. Temperature and humidity are the sensor's raw values (it reads warm); with both laser
  channels, PM values are their mean and `channelDisagreement` is true when PM2.5 A and B differ by more than
  5 µg/m³ and more than 70%: clean or replace the sensor.
- **airrohr:** the firmware serves the current values at `http://<ip>/data.json` on your network; the device has no
  location, so set `lat` and `lon`. Pressure arrives in pascals and is stored in hPa. Its push to your own API is the
  `airrohr` mapping preset (below), a different path: use one or the other.
- **AirGradient:** firmware 3.0.10 and later (ONE and Open Air) serve `http://<ip>/measures/current`; the monitor is
  also reachable as `airgradient_<serial>.local`, but give Foresignal its IP address. Set `lat` and `lon`.

**Storage.** The time base is the poll time (device clocks disagree), and the records expire after 10 minutes. A
SAMPLED policy keeps the history small: a new version only when PM2.5 moves by 5 µg/m³, PM10 by 10, CO₂ by 100 ppm,
temperature by 1 °C or humidity by 5 points (at most once a minute), when the model or the channel-disagreement flag
changes, and otherwise every 8 minutes.

**Privacy.** These monitors are at your home, so every slot is **private** (never served to anonymous readers, tiled,
embedded or sent to a remote LLM) and `egress: LOCAL_ONLY`, which you may change to `OPEN` as for the other
local receivers.

### Amateur radio sources

Prompt 082 adds five amateur-radio sources, all disabled until you enable them. Whoever received a spot or a packet
(a PSK Reporter receiver, an APRS igate, your own TNC) is stored as the record's **observer**, never in its payload:
it comes back only with `include=observer` on an agent or admin token, or when `security.exposeObserver` is on.

| Source | What it is | Default interval · budget |
|---|---|---|
| `pskreporter-ft8` | PSK Reporter reception reports (FT8 by default; FT4, WSPR, JS8, CW) | 10 min (never under 5 min) · 2/min, 300/day |
| `kiwisdr` | the public KiwiSDR web receivers | 24 h · 1/min, 4/day |
| `openmhz-systems` | OpenMHz scanner systems: **blocked**, see below | 1 h · 1/min, 30/day |
| `aprs-is` | APRS from the APRS-IS network, narrow and receive-only | stream · 1 connect/min, 100/day |
| `local-aprs-{n}` | APRS your own TNC hears on RF (KISS over TCP) | stream · 1 connect/min, 1,440/day |

**PSK Reporter.** Set `appContact` to your email address: PSK Reporter asks every client for a contact and for no more
than one query every five minutes. `callsigns` limits the queries to stations you watch (one request each, so keep
the list short); a watched call matches whether it transmitted or received. Without call signs one unfiltered query
runs, capped at `rptlimit` reports.

```json
PUT /v1/sources/pskreporter-ft8
{"enabled": true, "params": {"appContact": "you@example.org", "callsigns": ["N0CALL"], "windowSeconds": 900}}
```

**KiwiSDR.** The list is fetched over plain HTTP from `rx.linkfanel.net` (its HTTPS timed out when checked); it is a
public list and nothing private is sent.

**OpenMHz.** OpenMHz answers Foresignal with a Cloudflare challenge page. The source reports `BLOCKED_CHALLENGE` and
stays `failing`; Foresignal never pretends to be a browser or solves the challenge. Leave it disabled until OpenMHz
offers API access.

**APRS-IS filter etiquette.** `aprs-is` cannot be enabled without a narrow `filter`: ask APRS-IS only for what you
need. Use a range around you (`r/lat/lon/km`, at most 250 km), a list of stations (`b/N0CALL/N0CALL-9`), call-sign
prefixes (`p/K6/W6`), a type filter with a range (`t/poimqstunw/N0CALL/50`) or a friend range (`f/N0CALL/25`); at
most five terms, and no bare `*`. A filter over a busy area still delivers several packets a second, so start small.

```json
PUT /v1/sources/aprs-is
{"enabled": true, "params": {"filter": "r/37.77/-122.42/50", "login": "N0CALL"}}
```

`login` is your call sign, or leave it empty for an anonymous login (`EL` and six characters of your install id).
`N0CALL` itself is refused by the servers. With `includeMessages` APRS messages are kept too (`aprs-message`); they
are never sent to an embedding provider. With `dedupLocal` (on by default) a packet your own TNC heard in the last
30 seconds is dropped from the APRS-IS copy, because the RF copy is first-hand.

**Receive-only guarantees.** Foresignal never transmits and never igates:
- `aprs-is` logs in with passcode `-1`, which APRS-IS treats as a receive-only client, and writes nothing after the
  login line. Its connection object has no write method at all.
- `local-aprs-{n}` opens the TNC socket receive-only: not a single byte is written, not even a KISS command, and the
  socket is never half-closed (Direwolf would read that as a disconnect).

**Direwolf.** Enable Direwolf's KISS TCP server in `direwolf.conf` with `KISSPORT 8001` (8001 is the conventional
port; set whichever you use), then point a slot at it:

```json
PUT /v1/sources/local-aprs-1
{"enabled": true, "params": {"endpoint": "tcp://192.168.1.20:8001", "stationCallsign": "N0CALL-10"}}
```

The address must be on your own network (loopback, RFC 1918 or unique-local), as for the other local receivers.
`stationCallsign` names your station as the observer of what it hears. Local APRS stays on this instance
(`LOCAL_ONLY`) unless you open it; you can also mark the source private, which rounds positions.

**MeshCore KISS is not AX.25.** A MeshCore KISS modem also speaks KISS over TCP, but its frames carry raw MeshCore
packets, not AX.25. Pointed at one, `local-aprs-{n}` connects, counts every frame in
`foresignal.aprs.local.non_ax25` and stores nothing.

### Mesh networks

Prompt 083 adds five mesh sources, all disabled until you enable them. Every node arrives as a `mesh-node` record:
its names, role or node type, hardware, firmware, battery and last known position. The gateway or your own node that
heard it is the record's observer. Foresignal never decodes Meshtastic protobufs (they are GPL-3.0); every Meshtastic
path uses the firmware's JSON output.

| Source | What it is | Default interval · budget |
|---|---|---|
| `meshtastic-mqtt` | Meshtastic nodes on the public broker, under fixed rules | stream · 4 units/min, 100/day |
| `meshtastic-map` | the community Meshtastic map's node list | 24 h (never less) · 1/min, 2/day |
| `meshtastic-local` | your own Meshtastic node through your own MQTT broker (private) | stream · 20/min, 2,000/day |
| `meshcore-local` | your own MeshCore companion radio over its WiFi TCP firmware (private) | stream · 1 connect/min, 1,440/day |
| `meshcore-bridge` | your own MeshCore companion over USB, BLE or TCP through the bridge script (private) | push · 120/min |

**The public broker's rules, and why they are fixed.** ShadowBroker was reported to the Meshtastic project for one to
two orders of magnitude more broker traffic than a typical client, so `meshtastic-mqtt` enforces these limits in code
and no setting loosens them:
- it subscribes to `<root>/2/json/#` only, one subscription per root, at QoS 0; `root` must look like `msh/US` or
  `msh/US/CA`, and `msh/#`, `+/…` or any other wildcard is refused;
- `maxSubscriptions` is 1 by default and at most 3, and `root` plus `extraRoots` must fit in it (a PUT that breaks
  this is refused with `400 VALIDATION_FAILED`);
- the client id is `foresignal-<installId>`, so the broker's operators can identify it; keepalive 120 s; a clean
  session; it never publishes anything;
- it reconnects from 15 s up to 5 minutes with jitter, at most 12 times an hour, and each connect and subscribe costs a
  budget unit;
- it reads at most 100 messages a minute and drops the rest before parsing (counted as
  `foresignal.mesh.dropped{reason=rate}`).

Expect little data: most public traffic is protobuf, which Foresignal ignores, and the broker reduces public positions
to 10–16 bits (`positionPrecisionBits`; the record's accuracy follows from it). `includeText` keeps public channel text
as records; it is never posted anywhere.

```json
PUT /v1/sources/meshtastic-mqtt
{"enabled": true, "params": {"root": "msh/US"}}
```

**The community map** (`meshtastic.liamcottle.net`) is a hobby service; it is read at most once a day (about 29 MB,
parsed as a stream). Its license is not stated, so its records are marked "unspecified (community service)".

**Your own Meshtastic node.** Run a broker on your network (for example Mosquitto), then on the node's MQTT module set
the broker address, **enable JSON** and **disable encryption on uplink** for the channels you want to see. nRF52 nodes
(RAK4631, T-Echo and the like) cannot emit JSON; if you need one, run a protobuf-to-JSON bridge outside Foresignal
that republishes JSON to your broker (the protobuf path is GPL-3.0 and stays out of this repository). Then:

```json
PUT /v1/sources/meshtastic-local
{"enabled": true, "params": {"brokerUrl": "mqtt://192.168.1.10:1883", "root": "msh/local", "channels": ["Ops"]}}
PUT /v1/sources/meshtastic-local/credentials
{"username": "foresignal", "password": "…"}
```

The broker must be on your own network and the port explicit. With `mqtts://` Foresignal connects by name so TLS can
verify the certificate (system trust store), after checking the name still resolves to a private address. Leave
`channels` empty to see every channel. The source is private: it shows where your neighbors' nodes are.

**MeshCore: bridge or TCP firmware.**
- A companion running the **WiFi companion firmware** listens on TCP; point `meshcore-local` at it. There is no
  default port: the firmware uses 5000 only when it was built without `TCP_PORT`.

  ```json
  PUT /v1/sources/meshcore-local
  {"enabled": true, "params": {"endpoint": "tcp://192.168.1.30:5000"}}
  ```

- A **USB or BLE companion** (or any other) uses the bridge script in `contrib/meshcore-bridge`: it runs next to the
  radio, uses the `meshcore` Python package and pushes to `meshcore-bridge`. Store a key you choose (starting with
  `eik_`) as that source's `ingestKey` credential and give the bridge the same key; its README has a systemd unit.

Both read the companion's contacts (chat nodes, repeaters, rooms, sensors) and its own node, and put every channel and
direct message on the internal mesh message bus that mesh field reports use (prompt 084); messages become
`mesh-message` records only with `includeText`. Foresignal sends the companion only read commands (identify, device
query, contacts, channel names, battery, fetch messages); it never changes a setting, never reboots or resets it, never
touches keys and never sends anything to another node.

**One client per companion.** A MeshCore companion accepts a single TCP client and drops the previous one when another
connects. Inside Foresignal, `meshcore-local` and the `meshcore` channel (prompt 084) share one connection per
companion; outside it, close the MeshCore app on that companion's TCP port (or use BLE for the app), and do not run
the bridge and `meshcore-local` against the same companion.

**Repeater health.** For each repeater among your contacts, `advertStatus` is `ok` while its last advert is under
`repeaterLateAfter` (13 h), `late` until `repeaterSilentAfter` (26 h), then `silent`. It is re-checked every 15 minutes
and changes are stored as they happen, so a rule on `advertStatus` tells you when a repeater goes quiet. Health comes
from adverts alone; Foresignal never logs in to a repeater or asks it for status.

### Flood watching

Prompt 079 adds river, snow, drought and coastal water sources (see the Source Catalog, "Hydrology and coast"). All
ship disabled. Two of them publish an official flood category you can trigger on: `nwps-gauges` (NOAA river gauges,
`floodCategory` `no_flooding|action|minor|moderate|major|…`) and `noaa-coops-tides` (NOAA tide gauges,
`floodCategory` `none|action|minor|moderate|major`, from the station's NOS flood thresholds converted to meters above
MLLW).

**1. Scope the sources to your place.** Take the box from your place (a box a little larger than the area you care
about; `nwps-gauges` accepts up to 10° × 10°, `usgs-water` 5° × 5°) and pick the tide stations near you from
`tidesandcurrents.noaa.gov` (seven-digit ids; Great Lakes stations are not supported):

```json
PUT /v1/sources/nwps-gauges
{"enabled": true, "params": {"bbox": "-98.2,29.9,-97.3,30.7"}}

PUT /v1/sources/noaa-coops-tides
{"enabled": true, "params": {"stations": ["8771450", "8771341"]}}
```

`nwps-gauges` reads each gauge's flood stages once a day (at most 10 gauges per run, so a large box has all its
thresholds after a few hours); `maxGauges` (at most 300) caps the cost. `noaa-coops-tides` polls every 6 minutes,
one request per station.

**2. A river flood rule.** One alert when a gauge near home enters minor flood or worse, resolved when it falls back:

```json
POST /v1/triggers
{"name": "River flooding near home", "sources": ["nwps-gauges"], "mode": "enter", "autoResolve": true,
 "near": {"placeIds": ["home"], "radiusKm": 30},
 "conditions": [{"path": "floodCategory", "op": "in", "value": ["minor", "moderate", "major"]}],
 "messageTemplate": "{{payload.name}}: {{payload.floodCategory}} flooding, stage {{payload.stage}} {{payload.stageUnit}}",
 "severity": "warning"}
```

For an earlier warning, watch the forecast (`forecastCategory`, present only while NWPS has a current forecast) or
the margin to the minor flood stage: `{"path": "stageToMinor", "op": "<", "value": 1}` (feet, negative once in flood).

**3. A coastal flood rule.** The same shape on the tide gauges:

```json
POST /v1/triggers
{"name": "Coastal flooding", "sources": ["noaa-coops-tides"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "floodCategory", "op": "in", "value": ["minor", "moderate", "major"]}],
 "messageTemplate": "{{payload.name}}: water {{payload.waterLevelM}} m above MLLW ({{payload.floodCategory}})",
 "severity": "warning"}
```

Each record also carries the next predicted high and low water (`nextHighTime`, `nextHighM`, `nextLowTime`,
`nextLowM`) and the station's thresholds (`minorFloodM`, `moderateFloodM`, `majorFloodM`) in meters above MLLW.

**Other sources.** `usgs-water` gives raw USGS discharge and gage height (no flood category: combine it with
`nwps-gauges`, whose `usgsId` names the co-located USGS site); `snotel` gives snow water equivalent for melt-driven
floods; `usdm` gives the weekly drought map; `ea-flood-warnings` (England) and `eccc-hydrometric` (Canada) are the
optional equivalents outside the US. The USDM map requires its attribution wherever it is shown; Foresignal carries it
in the source descriptor.

### Energy and markets series

The energy and markets sources store time series as records without geometry: one `series-point` per period
(`externalId = {seriesId}:{period}`) and one `series-latest` per series, so a rule fires on a single entity. Browse
them with `GET /v1/series` and read one with `GET /v1/series/{sourceId}/{seriesId}` (JSON, or `?format=csv`), or the
`get_series` MCP tool.

- **`eia-petroleum`** (EIA spot prices and weekly stocks) needs a free EIA key from
  <https://www.eia.gov/opendata/>. The key is sent as the `api_key` query parameter and is redacted in every trace.
  Series ids are `eia:RWTC` (WTI Cushing), `eia:RBRTE` (Brent) and `eia:WCESTUS1` (weekly crude stocks). The spot
  prices are sourced by EIA from Refinitiv (LSEG), so the source is `LOCAL_ONLY`: its raw values reach this
  instance's own clients and notifications, never sinks, federation, share links or exports.
- **`eia-930`** (hourly US grid demand, net generation, interchange and fuel mix) uses the same key. Series are
  `eia930:{respondent}:{D|DF|NG|TI}` and `eia930:{respondent}:fuel:{fuel}`; the demand forecast (`DF`) carries
  `status=forecast`.
- **`gie-agsi`** (daily EU gas storage) needs a free key from <https://agsi.gie.eu/> (the `x-key` header). A missing
  or invalid key answers HTTP 200 with an `error` body, which shows as a rejected credential. Series are `agsi:{area}`
  (for example `agsi:DE`), value = `full` (%).
- **`ember-electricity`** (monthly generation, carbon intensity and demand) needs a free key from
  <https://ember-energy.org/>. Series are `ember:{entity}:{series}`.
- **`gb-carbon-intensity`** needs no key. Series are `carbon:gb:national`, `carbon:gb:region:{regionid}` and
  `genmix:gb`.
- **`grid-frequency-gb`** needs no key and watches Great Britain's 15-second system frequency; the companion
  `grid-frequency-fingrid` needs a Fingrid key and is **disabled by default**.
- **`ercot-public`** is **disabled by default** and needs a registered ERCOT account. Register personally at
  <https://apiexplorer.ercot.com> (ERCOT's terms forbid bot registration), subscribe to the Public API and copy the
  subscription key. Set the `username`, `password` and `subscriptionKey` credentials; Foresignal exchanges the
  username and password for a one-hour B2C token (cached in memory, never persisted). The Public API is only
  reachable from inside the United States: a 403 shows `geo-blocked` in the source's health. The keyless `eia-930`
  respondent `ERCO` covers ERCOT system load without an account.

**WTI above a threshold.** The `series-latest` record carries the newest non-forecast point, so one rule watches one
series:

```json
POST /v1/triggers
{"name": "WTI above 90", "sources": ["eia-petroleum"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "value", "op": "gt", "value": 90}],
 "messageTemplate": "{{payload.seriesName}}: {{payload.value}} {{payload.unit}} ({{payload.period}})",
 "severity": "warning"}
```

**A statutory grid-frequency excursion** (`match` with `onlyNew`, so it fires once when the excursion opens and not
again on every revision):

```json
POST /v1/triggers
{"name": "GB frequency excursion", "sources": ["grid-frequency-gb"], "kinds": ["frequency-excursion"],
 "mode": "match", "onlyNew": true,
 "conditions": [{"path": "severity", "op": "in", "value": ["statutory", "severe"]}],
 "messageTemplate": "GB frequency {{payload.direction}} to {{payload.extremeHz}} Hz ({{payload.severity}})",
 "severity": "critical"}
```

The `grid-frequency` `{grid}:state` record (`normal`, `low`, `high`, `no-data`) is a convenient edge entity, but
remember that `no-data` means the feed is stale, never an all-clear.

### History tiers

Each source's descriptor fixes its `historyTier`:

- **STANDARD** (discrete records: earthquakes, alerts, filings). Retention is per source and per
  kind; see [Retention](#retention) for the full resolution order. Set or clear a source's own
  window with `"retention": "365d"` or `"retention": null`.
- **TRACK** (moving things: aircraft, vessels). Every TRACK source shares one window,
  `retention.trackWindow` (3 days by default). A per-source `retention` is refused with
  `400 RETENTION_TIER_FIXED`; change `retention.trackWindow` instead.

`settings.effectiveRetention` shows the window in force for either tier.

**Why TRACK refuses a per-source window.** TRACK history lives in partitions shared by every TRACK
source and is removed by dropping whole partitions, never row by row: that is what keeps millions of
positions a day cheap. A partition can only be dropped when every source in it agrees, so there is
exactly one window for all of them. STANDARD history is deleted row by row and can therefore follow
each source's and each kind's own window.

### Ingest policies

The ingest policy decides which observations become stored versions. Everything else updates live
state only. The descriptor has a default (`descriptor.defaultIngestPolicy`). Your override is
`settings.ingestPolicy`, and `settings.effectiveIngestPolicy` is the one in force.

| Mode | Stores |
|---|---|
| `ALL` | Every change, plus a keyframe |
| `SAMPLED` | Significant changes, trigger edges and keyframes. Also a sample every `minInterval` when the position deviates by `minDeviationMeters` or a threshold moves (`thresholds: true`) |
| `LATEST` | Significant changes, trigger edges and keyframes only |

Override a policy, for example to sample aircraft less often:

```json
PUT /v1/sources/adsb-area
{"ingestPolicy": {"mode": "SAMPLED", "minInterval": "60s", "keyframe": "240s",
                  "minDeviationMeters": 1000, "thresholds": true, "edgeMinInterval": "30s"}}
```

Send `"ingestPolicy": null` to go back to the descriptor default. The rules (Core-Data-Model §5.1):

- `2s ≤ minInterval ≤ keyframe`, and `edgeMinInterval ≤ minInterval` (default `minInterval / 2`).
- With a record TTL: `keyframe ≤ ttl − pollInterval − 30s`.
- `"keyframe": "none"` is allowed only when the source has no TTL.
- `SAMPLED` and `LATEST` need a source that declares `significantFields`.

A policy that breaks a rule returns `400 INGEST_POLICY_INVALID`. Every violation is listed in
`errors[]` with its field, for example `ingestPolicy.keyframe`.

**Keyframes.** A keyframe is a version stored even though nothing significant changed, so history
always has a recent full picture of every entity: `keyframe` sets how often (for a source with a
TTL the default is `ttl − pollInterval − 30s`; `none` without a TTL). Separately, the **retention
keyframe** stores a fresh version of any entity still being observed once a quarter of its retention
window has passed since its last stored version, so retention can never delete the only version of
something that is still current. It adds well under 1% on STANDARD sources.

**What is not stored still counts.** An observation that is not persisted still updates live state
(`record_live`) and the `live` and `touch` stream events. An identical re-observation is a *touch*:
it moves `observedAt` and `lastReceivedAt` without a version, at most once per `touchMin` (one
hour without a TTL, else a tenth of the TTL between 30 s and 1 h). The fetch row's `touches`,
`versionsWritten` and per-reason counts show exactly what each fetch did.

**Defaults** (Core-Data-Model §5.3): aircraft, vessels and transit are `SAMPLED` on the TRACK tier
with family-specific intervals, deviations and thresholds; everything else is `ALL` on STANDARD.

### The fetch log

`GET /v1/sources/{id}/fetches` lists one row per fetch, stream flush or push batch, newest first.
Each row has its trigger and status, HTTP status, redacted URL, ETag, body hash, license, per-reason
counts and the seq range it wrote. Request and response bodies are never kept.

- Page with `limit` (up to 1000) and pass each page's `nextAfter` as `after`.
- Filter with `status` (`OK`, `NOT_MODIFIED`, `FAILED`, `REFUSED`, `DISCARDED`) and `trigger`
  (`SCHEDULED`, `MANUAL`, `STREAM`, `PUSH`).
- A row's `id` is the `batchId` on the versions it wrote.

### Reading `storage` and `budgetHoursRemaining`

Source health includes `storage`, which shows what the source costs:

- `observationsPerMinute`, `versionsPerMinute`: what arrives and what is stored, over the last hour.
- `persistRatio`: versions ÷ observations. A low ratio on a busy feed means the policy is doing its
  job. If it is near 1 on a position feed, consider `SAMPLED`.
- `triggerEdgeShare`: share of versions forced by trigger edges. Above 0.2, a trigger rule is
  probably flapping at a threshold; raise `edgeMinInterval` or adjust the rule.
- `bytesPerDay` (7-day average) and `projectedBytes` (`bytesPerDay` × retention window): the disk
  this source will hold at steady state.
- `lateLastHour`, `clampedLastHour`: observations that arrived out of order, and ones with
  timestamps in the future.

`budgetHoursRemaining` is how many hours today's request budget lasts at the last hour's rate. It is
`null` when nothing was requested in the last hour. If it is low early in the UTC day, lengthen the
`interval` or raise `budget.perDay`.

## Triggers and alerts

A trigger rule watches the change stream and opens an alert when a stored version matches. Rules live
under `/v1/triggers`; `POST /v1/triggers/{id}/test` backtests a rule against stored history without
alerting.

**A magnitude threshold** that opens one alert when a quake reaches M 6 and closes it on its own when a
revision takes it below:

```json
POST /v1/triggers
{"name": "Big quake", "sources": ["usgs-earthquakes"], "mode": "enter", "autoResolve": true,
 "conditions": [{"path": "mag", "op": ">=", "value": 6}],
 "messageTemplate": "M{{payload.mag}} {{payload.place}}", "severity": "warning"}
```

**A geofence** (GeoJSON Polygon or MultiPolygon in WGS 84; a LineString with `bufferMeters` becomes
a corridor) that alerts when a vessel enters a harbor and again when it leaves:

```json
POST /v1/triggers
{"name": "Harbor entry", "sources": ["aisstream"], "mode": "enter", "autoResolve": true,
 "geofence": {"type": "Polygon", "coordinates": [[[-97.40, 27.80], [-97.30, 27.80],
              [-97.30, 27.86], [-97.40, 27.86], [-97.40, 27.80]]]},
 "hysteresisMeters": 200, "cooldown": "30m"}
```

- **Conditions** are ANDed; operators include `>`, `>=`, `<`, `<=`, `==`, `!=`, `in`, `notIn`,
  `exists`, `matches` (a safe regex subset) and length tests. Paths are payload fields; volatile
  fields (such as USGS `updated`) are refused, because they never cause a version.
- **Mode:** `match` alerts on every matching version; `enter` when a version matches and the
  previous one did not (a new entity counts as entering); `exit` the reverse. With
  `autoResolve: true` an `enter` alert resolves on the exit edge, on a tombstone, or when the
  entity's live row expires. `hysteresisMeters` keeps a position jittering on a fence edge from
  flapping.
- **Cooldown** (`cooldown`, per entity or per rule with `cooldownScope: "rule"`) is measured on the
  observation time, so a burst of late data cannot fire a rule twice. `maxAlertsPerHour` caps a
  storm (default 120); `activeWindow` limits a rule to certain hours.
- **Templates:** `{{payload.<field>}}`, `{{title}}`, `{{source}}`, `{{externalId}}`,
  `{{observedAt}}` and the other placeholders the spec lists, plus the region placeholders of
  [Reference data and region tags](#reference-data-and-region-tags) (`{{near.text}}` …) and the weather
  placeholders of [Alert weather context](#alert-weather-context) (`{{context.weather.summary}}` …).
- **Weather context:** `weatherContext` (`auto`, the default, or `off`) decides whether the rule's geolocated
  alerts get `context.weather` (prompt 056).
- **Region presets:** `"geofenceRef": {"country": ["UA"]}` (or `admin1: ["US-TX"]`,
  `county: ["48453"]`) instead of a polygon. It needs the reference data loaded (otherwise
  `409 REFERENCE_DATA_MISSING`) and cannot be combined with `geofence`, `near` or `hysteresisMeters`.

**The alert lifecycle.** An alert is `firing`, then optionally `acknowledged`, then `resolved`:

| Call | Effect |
|---|---|
| `GET /v1/alerts?state=firing` | List, newest first (filters for rule, source, kind, severity, tag) |
| `POST /v1/alerts/{id}/ack` | Acknowledge (optional `note` and `feedback`) |
| `POST /v1/alerts/{id}/resolve` | Resolve by hand |
| `POST /v1/alerts/{id}/snooze` `{"for": "8h"}` | Suppress repeats of this alert for a while |
| `PUT /v1/alerts/{id}/feedback` | `useful`, `noise`, `late` or `false_positive`, for tuning |
| `POST /v1/triggers/{id}/snooze` | Snooze the whole rule |

`GET /v1/alerts/stream` streams `alert` (fired), `alert.updated` and `alert.resolved` events, each
with the alert's `rev` as its id, so a client resumes with `Last-Event-ID`. Every lifecycle change
also goes to the notification channels below.

### Places

A place is a named area used everywhere a "where" is needed: rules, saved views, briefings, Ask, the
`place=` filter of the read endpoints, and the weather area of interest.

```json
POST /v1/places
{"id": "home", "name": "Home", "shape": "point", "geometry": {"type": "Point", "coordinates": [-97.74, 30.27]},
 "radiusKm": 25, "timezone": "America/Chicago"}
```

- **Shapes:** a `point` with `radiusKm` (0.1–500, default 20), a `polygon` (fixed automatically; a ring
  that crosses the antimeridian is split), or a `route` (a LineString) with `bufferM` (10–50,000). The
  place's `area` is derived on every save (at most 10,000 vertices) and its `version` goes up.
- **Rules:** `"near": {"placeIds": ["home"], "radiusKm": 10}` fires inside the places' areas, grown by
  `radiusKm`. Editing a place moves its rules at once; a record that does not move never fires because
  of the edit, and one that moves afterwards is judged against the new area on its next observation.
  Alerts record `context.near` (place ids and versions).
- **Deleting** a place that rules, views or briefings use answers `409 PLACE_IN_USE` with the list;
  `?force=true` deletes it and disables those rules (their `lastError` says why). Views and briefings
  that still name it match nothing there. Exit rules on place-weather records can still fire when
  those records are tombstoned.
- **Privacy:** a home location is sensitive. Reading places always needs a token, even with
  `openReads`; `place=` filters do too. Notifications and the LLM get place names and rounded
  distances (`{{place.name}}`, `{{place.distanceKm}}`), never coordinates, and MCP returns coordinates
  only to agent tokens that ask for `detail=long`.
- **Weather AOI:** `GET /v1/places/aoi` is the union of the places, each grown to at least 150 km
  (`foresignal.weather.aoi-buffer-km`). With no places, weather sources use
  `foresignal.weather.default-aoi` (a Texas box) and report a health warning until a place exists.

### Saved views

A saved view is a named filter that the records API, map tiles and the stream render identically:

```json
POST /v1/views
{"slug": "quakes-near-home", "name": "Quakes near home", "kinds": ["earthquake"], "placeIds": ["home"],
 "conditions": [{"path": "mag", "op": ">=", "value": 2.5}], "fields": ["title", "observedAt", "payload.mag"]}
```

`GET /v1/views/quakes-near-home/records`, `/tiles.json` (its tile URLs carry the view's version) and
`/stream`; or `view=quakes-near-home` on `/v1/records`, `/v1/changes`, the streams and `/v1/search`,
combined with any other filter. A view that uses a place or a watchlist needs a token to render.

### Watchlists

A watchlist is a list of identifiers that rules and views match with
`{"op": "inWatchlist", "value": "<watchlist id>"}`: every source that declares the identity takes part.

```text
POST /v1/watchlists                       {"name": "Tails", "identity": "icao24"}
POST /v1/watchlists/{id}/import?dryRun=true   (Content-Type: text/csv)
value,label,notes,tags
a1b2c3,N123AB,Survey aircraft,survey;local
GET  /v1/watchlists/{id}/export
```

- **Identities:** `icao24` (six hex digits), `registration`, `callsign`, `mmsi` (nine digits), `imo`
  (seven digits with a valid check digit) and `custom` (rules must then give a `path`). Values are
  normalized (upper case for aircraft identifiers, digits only for MMSI and IMO) and validated; an
  import reports invalid rows and changes nothing with `dryRun=true`. `mode=replace` also removes what
  the file does not list. At most 100,000 items per list and 5 MB per import.
- **Identifier quality:** prefer ICAO 24-bit addresses for aircraft and IMO numbers for ships.
  Registrations move between airframes; MMSIs are reused, mistyped and spoofed, so an MMSI match is a
  lead, not proof.
- **PIA and LADD:** aircraft in the FAA's Privacy ICAO Address program fly under temporary addresses,
  and LADD-listed aircraft are blocked from many feeds; a watchlist cannot and should not get around
  either. Respect the operators' choice.
- **Policy:** watchlists are for aircraft, vessels and assets of public or professional interest. Do
  not use Foresignal to track private individuals.
- **Effect:** list edits take effect on each entity's next observation; nothing fires for the past.
  Alerts of watchlist rules are sensitive: anonymous readers and feed keys never see them.

### The entity registry and privacy (prompt 098)

The entity registry (`/v1/entities`) resolves records to one canonical object per aircraft, vessel,
satellite or station, from deterministic keys only (`icao24`, `registration`, `airframe`, `mmsi`,
`imo`, `norad`, `cospar`, `callsign`); there is no fuzzy name matching and no automatic merge of two
anchored entities. It is about vehicles and objects, never people or organisations.

- **No owner PII:** the FAA registry adapter stores no owner name or address; the aircraft entity's
  attributes carry only manufacturer, model, type and the like.
- **Privacy ICAO Addresses:** PIA hexes are absent from the registry and are never resolved to a
  registration; a leading `~` (readsb TIS-B) is rejected as a key.
- **Private sources:** records from private sources create no entity or link unless
  `foresignal.entity.includePrivate` is on, and links from private sources are visible only to admin
  tokens.

### "Near me" rules

A rule can follow a moving record, for example a phone that reports its position through a custom
push source (keep that source private):

```json
POST /v1/triggers
{"name": "Quake near me", "kinds": ["earthquake"], "mode": "enter",
 "conditions": [{"path": "mag", "op": ">=", "value": 5}],
 "near": {"anchor": {"source": "my-phone", "externalId": "pixel"}, "radiusKm": 150, "maxAnchorAge": "PT1H"}}
```

It fires when a quake appears near the phone's current position and when the phone moves near a quake
that is still current (re-checked when the phone has moved `minMoveMeters`, default 200 m).
`radiusKm: 0` means "the phone is inside the record's polygon", for warning areas. While the phone has
not reported for `maxAnchorAge` (default 30 minutes), or its record is gone, the rule is paused, its
`lastError` starts with `ANCHOR_STALE`, and one system notification says so; it resumes on the next
position. Templates get `{{anchor.distanceKm}}`; the phone's coordinates never leave the server.

### Settings export with places and watchlists

`GET /v1/settings/export?includePlaces=true&includeWatchlists=true` adds places, saved views and
watchlists with their items (both default false); the bundle then carries a `warnings` list, because
places reveal locations. `PUT /v1/settings/import` restores them before the rules that use them.

## Windowed rules and anomalies (prompt 100)

Besides the single-version rules above, a trigger rule has a `type`: `record` (the default engine),
`count`, `absence` or `anomaly`. Windowed rules reason over a time window rather than a version edge.
They reuse every 017 filter (sources, kinds, conditions, geofence, `near`, active window, tags,
severity, `max_alerts_per_hour`) and reject `onlyNew`, `mode` and `reappearAsEnter` (400
`WINDOW_RULE_INVALID`). All four types alert and resolve through the same 017 lifecycle, and their
alerts reference the `rule-windows` record version.

```json
{"name": "Many military aircraft near home", "type": "count",
 "sources": ["adsblol-mil"], "near": {"placeIds": ["home"], "radiusKm": 100},
 "window": {"window": "30m", "op": ">=", "threshold": 5}, "severity": "warning"}
```

```json
{"name": "NWS quiet too long", "type": "absence", "sources": ["nws-alerts"],
 "window": {"window": "2h", "coverageMin": 0.8}, "severity": "warning"}
```

```json
{"name": "Unusual vessel count in the strait", "type": "anomaly", "sources": ["aisstream"],
 "window": {"window": "1h", "groupBy": "h3:5", "direction": "up", "sensitivity": "medium",
            "minWeeks": 8, "coverageMin": 0.8}, "severity": "warning"}
```

- **Window.** `window` is the length (1 minute to 7 days); `step` is how often it is evaluated
  (default `min(window/4, 5m)`, minimum 30 s). `distinctBy` is `entity` (the 098 canonical entity
  when the record is linked, else the descriptor `entityKeys`, else `source+externalId`),
  `externalId`, or a payload path. Versions are never counted. `groupBy` is null, `source`, `place`,
  `h3:<res 3..8>`, `admin1`, `country` or a payload path. `GET /v1/triggers/{id}/window-state`
  returns the per-group state.
- **Identity limits.** One aircraft seen by two sources counts once only when 098 links them (fuse
  with `derived-aircraft` if you want that). Cluster identity ("one storm cell") is out of scope,
  which is why storm rules stay version-based.
- **Absence and the coverage gate.** An absence rule alerts only when every involved source was
  enabled and covered for the whole window. When it was not, the state is `unknown` with a reason and
  a single `system.window_rule_unknown` notification is raised; it never alerts. Receiver-down
  detection belongs to health and station events, not to absence rules. Warm-up: nothing is evaluated
  until the rule and the source have existed for one full window.
- **Anomalies.** An anomaly rule learns a baseline per slot (hour-of-week by default, or hour-of-day)
  over 8–12 weeks of coverage-ok samples, excluding flagged samples and winsorized at p99. A bucket
  fires when its tail p-value is below the sensitivity α (low 1e-4, medium 1e-3, high 1e-2) **and**
  the difference from the expected count is at least `minAbsoluteDelta`. Until all slots have
  `minWeeks` coverage-ok samples the state is `learning`; hour-of-day mode needs 14 days and marks
  results `lowConfidence`. A count drop while the coverage ratio fell at least 30% against the slot
  median is `coverage-drop` and never alerts. `GET /v1/triggers/{id}/baseline` shows the learned
  values, and `POST /v1/triggers/{id}/baseline/rebuild` recomputes them (AGENT).
- **Reading anomalies honestly.** `GET /v1/anomalies` lists the evaluated buckets. The p-value
  assumes the stated Poisson or negative-binomial model; it is **not** the probability that something
  is happening. Every result carries the detector name, version and parameters, the baseline summary,
  the watermark and evidence references, so it is reproducible from stored inputs. The four
  principles: detection is deterministic, results are reproducible, a p-value is not a probability,
  and absence of evidence is not evidence of absence.
- **MCP.** `list_anomalies` and `get_window_state` (read), and `watch_for_unusual_activity` (agent),
  which creates an anomaly rule and states the warm-up period.

## Intelligence events (prompt 101)

An **intelligence event** is a typed, located, time-bounded finding: `maritime-disruption`,
`aviation-anomaly`, `infrastructure-disruption`, `weather-impact`, `seismic` or `custom`. It is built
by a deterministic **correlation recipe** from pinned record versions, or created by an operator or
agent, and carries supporting and contradicting evidence, a confidence score, an evidence quality and
the detector version and parameters needed to reproduce it.

- **Four principles.** Detection is deterministic (an LLM may explain evidence but never adds members,
  raises confidence or changes the type); every result is reproducible (members pin
  `source/externalId@seq/version`, and `detector {name, version, params}` is stored); confidence is a
  score, **not** a calibrated probability (it becomes calibrated only when 103 validation sets
  `calibrated=true`, and every response carries `confidenceMethod`); and results are coverage-aware
  (missing data lowers evidence quality, it never counts as contradiction).
- **Evidence, inference, speculation.** `GET /v1/intel/events/{id}` separates `evidence` (members:
  records, alerts, anomalies), `inference` (type, severity, confidence, geometry, times) and
  `speculation` (alternatives and any LLM narrative, always labelled).
- **Confidence.** `c = 1 − Π_f (1 − w_f · r_f)` over distinct signal families, where `r_f` is the best
  member's source reliability (0.6 until 109 grades exist), then `×(1 − 0.25)` per active
  contradiction, capped at 0.95. Labels: low < 0.4 ≤ moderate < 0.7 ≤ high. Evidence quality: strong
  (≥ 3 families from ≥ 2 independent sources), moderate (2 families), weak (1 family), insufficient
  (members only from unhealthy sources).
- **Recipes.** Built-ins load from `src/main/resources/intel/recipes/*.json` and refresh on start
  when the bundled version is newer; the operator's `enabled` flag and parameter overrides are kept.
  A recipe whose kinds are not installed loads disabled with a warning. `PUT /v1/intel/recipes/{id}`
  enables/disables and sets overrides; `POST /v1/intel/recipes/{id}/test` dry-runs it over a seq
  range.
- **Dedup.** A candidate merges into an open event of the same type when their geometries intersect
  after buffering by the recipe's radius and their times overlap, or when they share a member. Two
  open events merge via `POST /v1/intel/events/{id}/merge {intoId}`: the older id survives, members
  move, and the merged event is tombstoned. Never across types.
- **Lifecycle.** `emerging` → `active` when the activate threshold is met; `resolved` after the quiet
  period with no new evidence; `reopened` within the reopen window; `dismissed` only by an operator
  or agent.
- **Alerts from events.** Every event is published as a record of the DERIVED source
  `foresignal-intel`, kind `intelligence-event`, so an ordinary 017 rule fires on it:

  ```json
  {"name": "Critical intelligence event", "sources": ["foresignal-intel"],
   "kinds": ["intelligence-event"], "conditions": [{"path": "severity", "op": "==", "value": "critical"}],
   "mode": "enter", "severity": "critical"}
  ```

- **Investigations.** Case files (`INV-{yyyy}-{nnnn}`) with items that keep a snapshot taken when
  added, so the evidence survives retention; `GET /v1/investigations/{id}/export?format=json|md|geojson`
  exports it, each format carrying the "Automated analysis inputs; verify before acting" header and
  licence attributions.
- **Exposure.** `POST /v1/exposure {geometry | placeId | eventId}` counts reference facilities,
  places, live aircraft and vessels and, with a population pack, an estimate. Every section carries a
  `status`; the population section is `not-installed` until a pack is imported.
- **Population pack (optional).** Download a GHSL GHS-POP GeoTIFF (EPSG:4326, 30 arcsec; © European
  Union, GHSL; CC-BY-4.0) into `${FORESIGNAL_DATA_DIR}/packs`, then
  `POST /v1/packs/population/import {"path": "packs/ghsl/GHS_POP_E2025_..._4326_30ss", "epoch": 2025}`.
  Each pixel's population is aggregated to the H3 cell (res 5 and 7) containing its centre; a query
  polyfills the area at res 7 (res 5 over 50,000 km²) and sums. The estimate is a **modelled
  residential population, not a casualty or impact estimate**.

## Autonomous analyst (prompt 102)

The analyst is a **scheduled LLM reviewer**. On each run it looks at what is new — intelligence
events (101), anomalies (100) and alerts (017) — compares them with history through read-only tools,
and writes **analyst notes** with typed, cited claims. It may also **propose** watches, intelligence
events, investigation items or event annotations. It explains evidence that deterministic detectors
already produced; it never manufactures evidence.

- **With no LLM configured the feature is inert.** Enabling a profile without a chat provider is 409
  `LLM_NOT_CONFIGURED`, and every non-LLM feature is unaffected.
- **Cost and egress.** Each run spends the configured provider's budget (026). A cloud provider is an
  egress path; a note citing anything private is itself private and is excluded from non-private
  egress and from cloud providers unless the operator allows it. Use a **local (Ollama)** provider to
  keep everything on-host.
- **Profiles.** `POST /v1/analyst/profiles` takes a `schedule` (`cron` + `timezone`, or `every` ≥ 10
  minutes; default every 30 minutes), a `scope` (sources, kinds, places, bbox, event types, rule tags,
  `minSeverity`), a `lookback`, `instructions` (≤ 4,000 chars), a model `providerId` (null = the chat
  role) and `effort` (`low`/`medium`), budgets (`maxRunsPerDay`, `maxTokensPerRun`, `maxToolCalls`,
  `maxWallSeconds`) and delivery (`channelIds`, `notifyMinPriority`).
- **`allowActions` risks.** Proposals are only *proposed* by default. Executing one requires an
  operator confirm, or `allowActions=true` and the exact type in `allowedActions`. **Even then**
  auto-execution re-checks that the owner token is still unrevoked, unexpired and holds `agent`
  scope, and that the daily `maxAutoActionsPerDay` cap is not reached. A restricted token cannot own
  an action-enabled profile. The analyst can never delete or disable anything. Watches are created
  enabled and tagged `analyst`; events carry `origin=analyst`; investigation items go only into open
  investigations; `annotate_event` only appends labelled speculation and never touches members or
  confidence.
- **Reading claims.** Every note body has claims typed `evidence`, `inference` or `speculation`.
  Evidence claims cite a tool result from that run; an uncited evidence claim is downgraded to
  speculation with a warning. The model's own confidence numbers are discarded — a proposed event's
  confidence is the 101 `EventScorer` over its members. An absence claim needs a coverage-ok citation.
- **The four principles.** Evidence comes from detectors; claims are typed; no confidence inflation;
  coverage-aware (never assert something "stopped" unless the coverage gate was ok). Tool results are
  delimited and treated as data, never instructions.
- **Evaluation.** `GET /v1/analyst/evaluation?profileId&since` returns per-profile aggregates: runs by
  status, the noop share, tokens, proposal acceptance rate, notes dismissed share, useful/wrong
  ratios. `GET /v1/analyst/runs` lists every run's tokens, tool calls, duration and outcome.

## Backtesting and precursor tests (prompt 103)

Ask "what would this rule or recipe have done?" and "does X tend to precede Y within T?" over retained
history, with answers that state their own limits. A backtest replays what Foresignal **stored**, not
ground truth.

- **Jobs.** `POST /v1/backtests {type: rule|recipe|precursor|quality, targetId | definition, from|fromSeq,
  to|toSeq, variants?}` returns 202 with a job; poll `GET /v1/jobs/{jobId}` (one path for every job
  type) or `GET /v1/backtests`. One queued or running job per token (409 `BACKTEST_IN_PROGRESS`, shared
  by REST and MCP). A range longer than 90 days, empty, inverted or wholly below retention is 400
  `BACKTEST_RANGE_INVALID`; an open-ended `toSeq` is clamped to the published watermark, and a start
  below the retained floor is clamped with the `RANGE_CLAMPED_BY_RETENTION` caveat. Without a range a
  job covers the last day (precursor jobs: the last 30 days). Jobs run on
  `foresignal.backtest.concurrency` worker threads (default 1, at most 2), in 50k-seq chunks with a 60 s
  statement timeout each, reporting `progress` and checking for cancellation between chunks;
  `DELETE /v1/jobs/{jobId}` cancels, and a canceled job keeps the status `cancelled`. Finished jobs are deleted
  after 30 days.
- **Recipe jobs** report the intelligence events the recipe actually formed in the range (the
  correlation engine has no dry-run replay yet), so they show what was stored, not a re-run.
- **Caveats.** Every result carries `NOT_GROUND_TRUTH` plus each applicable caveat: `RANGE_CLAMPED_BY_RETENTION`,
  `DOWNSAMPLED_RANGE`, `SAMPLED_SOURCE`, `COVERAGE_GAPS`, `DEFINITION_DIFFERS`, `ENTITY_RESOLUTION_CHANGED`,
  `WINDOW_APPROXIMATE`, `SMALL_SAMPLE`. **Always quote them when reporting a result.**
- **No look-ahead.** Anomaly rules are replayed bucket by bucket from the stored counts, and a baseline
  trains only on buckets before the evaluated bucket. Forecast-horizon rules are matched as the live
  engine saw each version at its own ingest time, never at the replay's wall clock. Count and absence
  rules are replayed at record level only (`WINDOW_APPROXIMATE`).
- **Reading lift and p-values.** Hit rates, lifts and p-values are descriptive statistics with stated
  assumptions; they are never calibrated probabilities. Small samples are labelled `insufficient`.
- **Precursor tests.** `POST /v1/precursor-tests` defines X and Y (an alert rule, an intelligence event
  type or an anomaly rule), a lag window, a spatial rule and a dedup window; `POST
  /v1/precursor-tests/{id}/test` submits a job. The verdict is `insufficient`, `no-evidence-of-precedence`,
  `weak` or `notable`. The output reports the reverse test (flagging "co-occurrence, not precedence"), a
  deterministic circular-shift base rate and permutation p-value, and how many precursor tests this
  instance ran in the last 30 days with a Bonferroni-adjusted p-value. **Association in stored data; not
  causation.**
- **Alert quality.** `GET /v1/triggers/{ruleId}/quality?since` reports precision with a Wilson interval
  (null with fewer than five labelled alerts), median time-to-ack and time-to-resolve, flapping,
  duplicates and the severity distribution. The 017 feedback enum maps to classes as: `useful` →
  positive; `noise` and `false_positive` → negative; `late` → neutral.
- **Tuning suggestions** (`GET /v1/triggers/{ruleId}/suggestions?since&simulate`) are deterministic and
  **never applied automatically**. Each carries a rationale, a ready `TriggerRuleInput` partial patch, an
  expected effect and caveats; applying one is an ordinary rule update by an agent. With `simulate=true`
  (agent only) a backtest job measures the patched variant over the same range.

## Infrastructure graph (prompt 104)

A facility graph of power, telecom, transport, health, water and space infrastructure, built from the
094 reference datasets and, optionally, your own OpenStreetMap extract. It answers "what depends on
things inside this outage or event area?" with paths and explicit uncertainty. All of it is open data,
processed locally.

- **The OSM extract.** Download a Geofabrik extract for your region (`https://download.geofabrik.de/`,
  refresh monthly), then process it locally with `osmium` (an external tool; never linked or bundled):
  `osmium tags-filter region-latest.osm.pbf nwr/power=plant,substation,generator nw/power=line,cable
  nwr/amenity=hospital nwr/healthcare=hospital nwr/telecom=data_center,exchange nwr/aeroway=aerodrome
  nwr/landuse=port nwr/harbour=yes nwr/man_made=wastewater_plant,water_works -o infra.osm.pbf` then
  `osmium export infra.osm.pbf -f geojsonseq --add-unique-id=type_id -o infra.geojsonseq`. Put the
  result under `${FORESIGNAL_INBOX_DIR}/osm` and enable `osm-infrastructure`. **Do not use the public
  Overpass API in bulk** — Foresignal never calls it; use an extract. The file is streamed (never
  loaded whole); a file over 2,000,000 features is refused with the health reason "feature cap
  exceeded", and lines that are not valid GeoJSON features show as "parse errors: n".
- **ODbL obligations.** The OSM data is ODbL 1.0; attribution "© OpenStreetMap contributors" appears
  in every response that includes it, and the derived facility graph is a Derived Database — if you
  redistribute it, keep the ODbL and share-alike terms.
- **Edge labels.** Every edge states how it is known — `declared`, `topological`, `proximity` or
  `inferred` — and a label `confirmed | likely | possible | speculative`. **Proximity edges are never
  better than `possible`.** A path's label is its weakest edge. Results always say "derived from open
  data; not authoritative; heuristic links may be wrong." The cable data does not list each cable's
  landing points, so `cable-landing` is a geometry match (`topological`, `likely`). A power line
  reaching one substation is `likely`, reaching two is `confirmed`; impact traversal walks lines in
  both directions. An edge's licence is the most restrictive licence of its two facilities.
- **API.** `GET /v1/infra/nodes`, `GET /v1/infra/nodes/{id}` (with in/out edges), `GET /v1/infra/edges`,
  `POST /v1/infra/affected {geometry | eventId | record, depth, minLabel, types}`, `GET /v1/infra/status`,
  `POST /v1/infra/rebuild` (admin; 202 at once, the build runs in the background),
  `GET/PUT /v1/settings/infra-rules` (admin; `enabled` and `params` per rule apply to the next build).
  `GET /v1/infra/*` are reads, serve only the newest successful build, and page with `cursor`; a
  query area over 2,000,000 km² needs a `types` filter. Before the first build the graph endpoints
  answer 409 `INFRA_GRAPH_NOT_BUILT`. A record seed that is only a point uses its region polygon and
  is flagged `approximateArea`; a private event needs the private grant.
- **Rebuilds.** Besides `POST /v1/infra/rebuild`, the graph rebuilds 10 minutes after the last import
  of a mapped source and weekly (`foresignal.infra.rebuild-cron`, default Sunday 04:30 UTC;
  `foresignal.infra.auto-rebuild=false` turns both off). A rebuild replaces the previous graph in one
  transaction.

## Notification channels

A channel delivers alerts, briefings and system events to one destination, formatted natively for
it. Manage channels under `/v1/channels` (admin only). `GET /v1/channels/types` lists every type
with its config and secret fields. Secret fields (`secrets`) are write-only: they are encrypted at
rest, never returned (a channel lists only the names of its set secrets in `secretFields`), and a
`PUT` that omits `secrets` keeps the stored ones. `POST /v1/channels/{id}/test` sends a test message
at once and returns the destination's answer without storing anything.

Every message is stored as a notification before it is delivered, even when no channel takes it.
Deliveries are durable: a failure retries after 10 s, 1 min, 5 min, 30 min and 2 h, then fails, and
the failure is announced as a `system` event to your *other* channels. `429` answers honor
`Retry-After`. Each channel has a rate limit sized to its destination. Look at
`GET /v1/channels/{id}/deliveries` to see what happened (`kind` is `send`, `update`, `followup` or
`digest`; `deferredReason` says why one is waiting).

Common fields: `events` (`alert`, `briefing`, `system`; default `["alert"]`), `ruleIds` (null for
every rule), `minSeverity`, `titleTemplate` and `bodyTemplate` (the trigger placeholders plus
`{{severity}}`, `{{summary}}`, `{{url}}`, `{{ruleName}}` and `{{message}}`; `{{observedAt|local}}`
prints `2026-05-01 07:00 CDT` in the channel timezone and `{{observedAt|ago}}` prints `3 min ago`),
`waitForEnrichmentSeconds`, `timezone`, `quietHours`, `groupWindowSeconds`, `backlogCap` and
`editInPlace`. The weather placeholders `{{context.weather.summary}}` and `{{context.weather.<section>.<field>}}`
(prompt 056) read the alert's weather context; see [Alert weather context](#alert-weather-context). The region placeholders (`{{near.text}}`, `{{country.name}}` …, prompt 040) read the
alert's region snapshot; `config.units: "imperial"` prints `{{near.text}}` in miles. Links in messages use the `server` setting's `publicBaseUrl` (default
`http://localhost:8080`); set it to the address your phone can reach.

### Setting up each type

| Type | Config | Secrets | Notes |
|---|---|---|---|
| `webhook` | `url`, `method` (POST/PUT), `headers`, `bodyTemplate` | `hmacSecret`, `header.<Name>` | See "Verifying webhook signatures" |
| `slack` | `username`, `iconEmoji` | `webhookUrl` | An incoming webhook from a Slack app |
| `mattermost` | `channel`, `username`, `iconUrl` | `webhookUrl` | An incoming webhook |
| `discord` | `username` | `webhookUrl` | Channel settings → Integrations → Webhooks |
| `teams` | — | `workflowUrl` | **Workflows URLs only** (below) |
| `google-chat` | — | `webhookUrl` | Space → Apps & integrations → Webhooks |
| `telegram` | `chatId` | `botToken` | Create a bot with @BotFather and add it to the chat |
| `matrix` | `homeserverUrl`, `roomId` | `accessToken` | A bot account invited to the room (unencrypted room) |
| `ntfy` | `serverUrl`, `topic`, `call` | `accessToken` | `call` is below |
| `gotify` | `serverUrl` | `appToken` | An application token |
| `pushover` | `device`, `sound` | `appToken`, `userKey` | Critical is an emergency alert, canceled on resolve |
| `email` | `smtpHost`, `smtpPort`, `tls`, `from`, `to`, `plainTextOnly` | `username`, `password` | `plainTextOnly` for Winlink gateways |
| `homeassistant` | `baseUrl`, `mode`, `service`, `iosCritical`, `androidAlarmStream` | `webhookId` or `accessToken` | Below |
| `apprise` | `serverUrl`, `configKey`, `tag` | `urls` | Below |
| `zulip` | `site`, `stream`, `botEmail`, `topicTemplate` | `apiKey` | Below |
| `mqtt` | `brokerUrl`, `topicPrefix`, `qos`, `protocolVersion`, `messageExpiry`, `payloadTemplate`, `publishHealth`, `publishSummary`, `summaryInterval`, `haDiscovery`, `haPrefix`, `haMaxRules`, `haMaxSources`, `haRetainDiscovery` | `username`, `password`, `caCertPem` | [MQTT and Home Assistant](#mqtt-and-home-assistant) |

- **Microsoft Teams:** Office 365 connector webhooks were retired in May 2026, so a
  `*.webhook.office.com` or `outlook.office.com/webhook` URL is refused. In Teams, open the channel's
  *Workflows*, choose "Post to a channel when a webhook request is received", and use the URL it
  gives you.
- **Home Assistant:** `mode: webhook` (default) posts the full Foresignal JSON to
  `/api/webhook/{webhookId}` so an automation can react to it. `mode: notify` calls
  `notify.{service}` (for example `mobile_app_pixel`) with a long-lived access token; critical
  alerts then use the iOS critical sound and the Android alarm stream, which break through
  do-not-disturb. Turn either off with `iosCritical: false` or `androidAlarmStream: false`.
- **ntfy `call`:** set `call` to a phone number (or `yes` for the first verified number) to get a
  voice call for **critical** alerts only. It needs an ntfy account with a verified phone number
  and works only with a server that offers calls (ntfy.sh with a paid plan).
- **Apprise:** point `serverUrl` at an apprise-api server and either name a saved configuration
  (`configKey`) or pass Apprise URLs as the secret `urls`. When some of the configuration's targets
  fail and others succeed, apprise-api answers `424`; Foresignal records the delivery as delivered
  with a warning and **does not retry**, because a retry would notify the successful targets again.
- **Zulip:** create a generic bot (Settings → Bots), subscribe it to the stream, and use its email
  as `botEmail` and its API key as the secret `apiKey`. Each rule posts to its own topic
  (`topicTemplate`, default `{{ruleName}}`), and later updates of an alert land in the same topic.

### Edit in place

When an alert is acknowledged, snoozed or resolved, the original message is edited where the
destination allows it, and a follow-up message is sent everywhere else:

| Edited in place | Follow-up message |
|---|---|
| Discord, Telegram, Matrix | Webhook, Slack, Mattermost, Teams, Google Chat, ntfy, Gotify, Pushover, email, Home Assistant, Apprise, Zulip |

The edited text shows the new state, for example "✅ Resolved 14:02 CDT — …" with the old text struck
through. Set `editInPlace: false` on a channel to always get a follow-up instead. Pushover emergency
alerts are canceled on acknowledge or resolve, so the phone stops repeating them.

### Map attachments

A geolocated alert can carry a static map: the alert's geometry over radar (or other products) on a
Natural Earth basemap, with the frame's valid time, a legend, the attribution and the footer
"Automated — not an official warning" (prompt 055). It is **off by default on every channel**, because
the image leaves your network on a cloud chat service and shows where the alert is. Turn it on per channel
in `config`:

| Key | Default | Meaning |
|---|---|---|
| `attachMap` | `none` | `static` attaches a map to each geolocated alert |
| `mapProducts` | `["mrms-reflectivity"]` | 1–4 products drawn; an alert on a `raster-frame` record defaults to the product that matched |
| `mapRadiusKm` | `50` | radius around the alert geometry (the map always contains the whole geometry) |
| `maxAttachmentBytes` | per type, below | the largest image sent on this channel |

The map is rendered **once per alert** when it fires, at 800 px, after waiting up to 5 s for a frame newer
than the alert, so it shows the weather that caused it. It is stored as media for 7 days (`role: map`) and
reused by every channel. Rendering starts at once and runs alongside the enrichment wait: a send waits for
its map at most 8 s, and never longer than the channel's `waitForEnrichmentSeconds` when that is set; past
that the alert goes out as text. A `satellite-pass` alert attaches the pass composite's 256 px thumbnail
instead of a map.

| Type | How the image is sent | Default cap |
|---|---|---|
| `discord` | multipart `files[0]`, shown in the embed (`attachment://map.png`) | 8 MB |
| `telegram` | `sendPhoto` with the text as caption (≤ 1,024 characters; longer text follows as a message) | 10 MB |
| `ntfy` | `PUT` of the PNG with `Filename: map.png`, the text in the `Message` header | 15 MB |
| `pushover` | multipart `attachment` | 5,242,880 bytes |
| `email` | inline image (`cid:map`) in the HTML part; none with `plainTextOnly` | 10 MB |
| every other type | the text plus a `[Map](…)` link to `{publicBaseUrl}/v1/media/{id}`; `webhook` also lists `attachments[{mediaId, url, contentType, bytes}]` in its default body | — |

**Size control:** an image over the channel's `maxAttachmentBytes` is replaced by a 600 px map, then by
the 256 px thumbnail, then the alert is sent as text only. The level chosen is shown on the delivery
(`attachmentLevel`: `map-800`, `map-600`, `thumbnail` or `text-only`), with the media references in
`attachments`; a retry resends the same image from the media store.

**ntfy attachments** need attachments enabled on the server: set `attachment-cache-dir` (a directory or
an S3 URL) and `base-url` in `server.yml` (ntfy's own option names; `attachment-file-size-limit` caps a
file, 15M by default). ntfy.sh has them on. When the server answers `413` (too large, or the bandwidth
limit) or `400` with code `40014` (attachments not allowed), Foresignal sends the text message instead and
records `text-only`.

**Links on the other types** point at the in-app media URL, which needs a token unless reads are open,
and works only where `publicBaseUrl` is reachable (your LAN or a VPN). There are no signed URLs in v1.
Private-source alerts never carry a map to a channel without `privatePayload: true`, and products of
private sources are left off the map for such a channel.

### Quiet hours, pause, folding and grouping

- **Quiet hours** (per channel): `{"start": "22:00", "end": "07:00", "days": ["MON", …],
  "bypassSeverity": "critical"}` in the channel `timezone` (else the `server` setting's timezone,
  else UTC). `days` are the days a window starts on; leave it out for every day. Messages inside the
  window wait until it ends; if more than one alert waited, they arrive as one digest, newest first.
  Messages at or above `bypassSeverity` are sent anyway (`null`: nothing bypasses).
- **Global pause:** `PUT /v1/settings/notifications` with `{"pausedUntil": "<time>",
  "pauseBypassSeverity": "critical"}` defers everything else on every channel until then, and each
  channel gets one digest when the pause ends. Set `pausedUntil` to `null` to end it early.
- **Backlog folding:** when more than `backlogCap` (default 20) alert messages wait for one channel,
  the oldest are folded into one digest ("+N more alerts"), so a storm cannot flood a phone.
- **Grouping** (opt-in): with `groupWindowSeconds` above 0, the first alert of a rule waits for the
  window and everything the rule fires meanwhile is sent with it as one message.

Lifecycle updates and system events are never folded into a digest.

### Verifying webhook signatures

With `hmacSecret` set, every `webhook` request carries `X-Foresignal-Timestamp` (Unix seconds) and
`X-Foresignal-Signature: sha256=<hex>`, where the hex is HMAC-SHA256 with your secret over the
timestamp, a dot and the raw request body. To verify: recompute it over the bytes you received,
compare in constant time, and reject timestamps more than a few minutes from your clock.
`X-Foresignal-Delivery` is the delivery id (the same on every retry, so you can drop duplicates).

### The origin marker

Every outbound request carries this instance's id in `X-Foresignal-Origin`, and the JSON bodies
Foresignal defines (webhook, Home Assistant, Apprise) carry it as `foresignalOrigin`; ntfy messages
carry the tag `foresignal-origin-<id>` and Gotify messages the extra `foresignal::origin`. When you
loop a channel back into Foresignal (for example ntfy into an ingest endpoint), the ntfy- and Gotify-compatible ingest endpoints (prompt 024) drop
messages carrying its own marker, so a notification can never feed itself.

### MQTT and Home Assistant

An `mqtt` channel publishes alerts, briefings and system events to your broker, keeps retained state
topics (rule states, source health, a live summary) and, with `haDiscovery: true`, makes Foresignal
appear in Home Assistant as a device without any YAML. Foresignal holds one connection per channel and
reconnects on its own (1 s, doubling to 120 s).

```json
{"name": "MQTT broker", "type": "mqtt", "events": ["alert", "briefing", "system"],
 "config": {"brokerUrl": "mqtts://broker.lan:8883", "topicPrefix": "foresignal", "haDiscovery": true},
 "secrets": {"username": "foresignal", "password": "…"}}
```

- `brokerUrl` takes `mqtt://`, `mqtts://`, `ws://` and `wss://`. A LAN broker is fine. A broker on a
  public address without TLS or without a username is saved with the warning
  `MQTT_INSECURE_PUBLIC_BROKER` in the channel's `warnings`; it is not refused.
- For a private CA, put its certificate (PEM) in the secret `caCertPem`. Hostname verification always
  stays on.
- `protocolVersion: "3.1.1"` is for old brokers; message expiry and the content type need MQTT 5.
- `POST /v1/channels/{id}/test` publishes to `{prefix}/test` and reports the PUBACK latency in
  `durationMs`. `POST /v1/channels/{id}/mqtt/republish` sends every retained state and the discovery
  config again.

**Mosquitto.** Give Foresignal its own user, and let it write only its own topics and the discovery
prefix:

```
# mosquitto.conf
listener 8883
cafile /mosquitto/certs/ca.crt
certfile /mosquitto/certs/server.crt
keyfile /mosquitto/certs/server.key
allow_anonymous false
password_file /mosquitto/config/passwd
acl_file /mosquitto/config/acl
```

```
# /mosquitto/config/acl
user foresignal
topic write foresignal/#
topic write homeassistant/#
topic read homeassistant/status

user homeassistant
topic read foresignal/#
topic readwrite homeassistant/#
```

Create the users with `mosquitto_passwd -c /mosquitto/config/passwd foresignal` (and without `-c`
for the next one).

**Topics** (`{prefix}` is `topicPrefix`, default `foresignal`):

| Topic | Retained | Content |
|---|---|---|
| `{prefix}/status` | yes | `online`, or `offline` (Last Will, and on shutdown) |
| `{prefix}/alerts/{severity}` | no, expires after `messageExpiry` | a fired alert: `event_type`, `id`, `ruleId`, `ruleName`, `severity`, `state`, `title`, `shortTitle`, `body`, `url`, `sourceId`, `externalId`, `kind`, `firedAt`, `point` (`[lon, lat]`), `tags`, `near` |
| `{prefix}/alerts/updates` | no | `{id, state, resolvedAt, acknowledgedBy}` on acknowledge, snooze, resolve |
| `{prefix}/briefings/{briefingId}` | yes | the latest run: `runId`, `title`, `at`, `url`, `status`, `markdown` (at most 16 KB) |
| `{prefix}/system/events` | no | system events (a source failing, a delivery that gave up, …) |
| `{prefix}/rules/{ruleId}/state` | yes | `{state: firing\|resolved\|idle, alertId, severity, lastFiredAt}`; cleared when the rule is deleted |
| `{prefix}/sources/{sourceId}/health` | yes | `{status, reason, lastSuccessAt, lastDataAt}`; never for private sources |
| `{prefix}/summary` | yes | the glance (`GET /v1/glance`), when it changes |

Alerts of private sources carry only the rule name, severity and link unless the channel has
`privatePayload: true`. `payloadTemplate` replaces the event payloads with your own JSON (the webhook
`bodyTemplate` placeholders); keep an `event_type` member if Home Assistant should still see alerts.

**Home Assistant.** With the MQTT integration set up and `haDiscovery: true`, a *Foresignal* device
appears with sensors for open alerts per severity, failing and degraded sources and the last briefing,
an event entity per severity (*Critical alert*, *Warning alert*, *Info alert*), and a problem sensor
per enabled rule (at most `haMaxRules`, critical first) and per non-private source (at most
`haMaxSources`). Entities show unavailable while Foresignal is down. Foresignal re-sends discovery when
Home Assistant restarts (its `online` birth message) and 5 s after rules or sources change; the config
is not retained unless you set `haRetainDiscovery: true`. Turning discovery off, or disabling or
deleting the channel, removes the device. Watched aircraft and vessels are not exposed as
`device_tracker` entities: Home Assistant uses GPS trackers only to place them in its zones.

An automation on the critical alert event (the payload's members are the event's attributes):

```yaml
automation:
  - alias: "Foresignal critical alert"
    triggers:
      - trigger: state
        entity_id: event.foresignal_critical_alert
    conditions:
      - condition: template
        value_template: "{{ trigger.from_state.state not in ['unavailable', 'unknown'] }}"
    actions:
      - action: notify.mobile_app_pixel
        data:
          title: "{{ trigger.to_state.attributes.shortTitle }}"
          message: "{{ trigger.to_state.attributes.title }}"
```

Check the entity id under *Settings → Devices → Foresignal*; it follows the device and entity names.

**Node-RED.** An *mqtt in* node on `foresignal/alerts/+` (QoS 1, output "a parsed JSON object") gives
`msg.payload.severity`, `msg.payload.title` and `msg.payload.url`; `foresignal/alerts/updates` tells
you when an alert is acknowledged or resolved, and `foresignal/status` whether Foresignal is up.

**Displays.** Devices that subscribe to MQTT, such as an AWTRIX clock, can show
`{prefix}/summary` (`alerts.open.critical`, `status`) or react to `{prefix}/alerts/critical`
directly; give them a broker user that may only read `foresignal/#`.

### Mesh alert channels

Two channel types put short alert lines on **your own** LoRa mesh: `meshtastic` (through your node's MQTT JSON
downlink) and `meshcore` (through the MeshCore companion protocol over TCP). Both send only on a private, keyed
channel, pass every transmission through an airtime governor, and can take short field reports back
(prompt 084).

```json
{"name": "Mesh alerts", "type": "meshtastic", "events": ["alert"],
 "config": {"brokerUrl": "mqtt://192.168.1.20:1883", "root": "msh/local", "gatewayNodeNum": "2864434397",
            "channelIndex": 1, "channelName": "Family", "privateChannelConfirmed": true,
            "region": "EU_868", "airtimePreset": "meshtastic-longfast", "minSeverity": "warning"},
 "secrets": {"username": "foresignal", "password": "…"}}
```

```json
{"name": "MeshCore alerts", "type": "meshcore", "events": ["alert"],
 "config": {"companionUrl": "tcp://192.168.1.30:5000", "channelIndex": 1, "region": "EU_868",
            "airtimePreset": "custom",
            "airtimeCustom": {"spreadingFactor": 10, "bandwidthKhz": 250, "codingRate": 5, "preambleSymbols": 8}}}
```

**What goes out.** One line per alert: `{sev} {title} {where} {hhmm}Z #{code}`, for example
`!! Quakes Austin 1205Z #0007`. `sev` is ASCII (`!!!` critical, `!!` warning, `i` info; an emoji would cost four
bytes), `where` is the nearest place or a 2-decimal `lat,lon`, the time is UTC, and `#code` is the alert's
4-character short id, which a reply can acknowledge. A resolution is `OK {title} #{code}`; acknowledgements and
snoozes are never sent. Meshtastic lines are at most 200 bytes; MeshCore channel lines at most 133 characters and
127 bytes (the firmware's 160-byte text limit less the `name: ` prefix it adds). `profile: "cbor"` sends the
machine-readable CBOR profile instead (Meshtastic: `~E1 ` and base64url; MeshCore: a channel datagram with
`dataType` 0xFF45, in the 0xFF00–0xFFFE development range; a production deployment should register its own value
through MeshCore's `number_allocations.md`). The channel's own `config.minSeverity` (default `warning`) applies on
top of its top-level `minSeverity`.

**Meshtastic node prerequisites.**

- In the node's MQTT settings: your broker on the LAN, **JSON enabled** (not available on nRF52 nodes),
  **encryption disabled** toward that private broker, and the root topic you set as `root`.
- A channel named `mqtt` with **downlink enabled** (the firmware takes JSON downlinks on `{root}/2/json/mqtt/`).
  Some firmware wants the gateway id on the topic: set `topicSuffixNodeId: true`.
- `gatewayNodeNum` is the node's number in decimal (the `from` of the downlink); `channelIndex` and
  `channelName` name the private channel the text goes to. `toNodeNum` sends a direct message instead.
- Foresignal publishes at QoS 1 on one client per broker (`foresignal-<installId>-mesh`, no Last Will) and marks
  the delivery sent on the broker's PUBACK: JSON gives no delivery confirmation.

**MeshCore prerequisites.** A WiFi companion firmware reachable as `tcp://host:port` (explicit port, a private
address). Foresignal shares the one TCP client the companion accepts with the `meshcore-local` source and only
ever sends the three registered send commands (channel text 0x03, channel datagram 0x3E, direct text 0x02);
`dmContactPrefix` (a contact's 12-hex key prefix) sends direct messages, and `requireAck: true` waits for the
contact's acknowledgement, retries once and ends the delivery `unacked` (not `failed`, so no failure event) when
none comes. MeshCore radio settings vary by region, so enter them in `airtimeCustom`.

**Why public channels are refused.** A public or default channel is shared by everyone in radio range, and
automated traffic there drowns human conversation and breaks the mesh's etiquette. Create and update answer
400 `MESH_PUBLIC_CHANNEL` when:

- `meshtastic`: the broker is under `meshtastic.org` or not on the LAN; `channelIndex` is 0; `channelName` is
  empty or a modem-preset name (LongFast, LongSlow, LongModerate, MediumFast, MediumSlow, ShortFast, ShortSlow,
  ShortTurbo, VLongSlow); `licensedMode` is true (ham mode disables encryption); or `privateChannelConfirmed` is
  not true.
- `meshcore`: `channelIndex` is 0, or the slot read from the companion (`CMD_GET_CHANNEL`) is empty, named
  `Public`, a `#hashtag` channel (its key derives from the name) or has an all-zero secret. The slot is read again
  on every reconnect, and nothing is sent until it is verified private; a companion that cannot be reached at save
  time does not block the save.

**Duty cycle.** Every transmission's time on air is estimated with Semtech's formula from the preset
(`meshtastic-longfast`: SF11, 250 kHz, 4/5, 16-symbol preamble; `meshtastic-mediumfast`: SF9; or `custom`) and the
message bytes plus a conservative overhead (32 bytes for Meshtastic, 24 for MeshCore). A one-hour sliding window
admits a message only when:

- the airtime stays within `dutyCyclePercent` (default **1 %** of the hour, a polite share; at most the region's
  ceiling and at most 10 %);
- at least `minSpacing` (default 30 s) has passed since the last transmission;
- fewer than `maxMessagesPerHour` (default 20) went out.

`region` sets the ceiling: 10 % for EU_868 and UK_868 (the 869.40–869.65 MHz sub-band Meshtastic's EU_868 uses)
and EU_433; US and ANZ have no duty cycle (their dwell-time rules are the radio's job), where the 10 % maximum caps
critical alerts instead. **Worked example, LongFast:** a 60-byte line plus 32 bytes of overhead is 92 bytes on the
air, 93 payload symbols of 8.192 ms plus a 165.9 ms preamble: **0.93 s**. A 1 % share is 36 s an hour, room for
about 38 such lines, so the 20-per-hour cap binds first; a full 200-byte line takes 1.99 s (18 an hour). On
MediumFast the same 60-byte line takes 0.27 s.

A message that does not fit waits (`deferredReason: airtime`); alerts released together go out as one line,
`{n} alerts, top: {title} #{code}`. Critical alerts jump the queue but never exceed the region's ceiling.
`GET /v1/channels/{channelId}/airtime` shows the window (`usedSeconds`, `limitSeconds`, `queued`,
`foldedLastHour`, `droppedLastHour`), the counters `foresignal.channel.mesh.airtime_seconds`, `…deferred`,
`…folded` and `…dropped` are tagged with the channel id, and a backlog older than an hour raises the system event
`system.mesh_airtime_backlog`.

**Field reports.** With `reportsEnabled: true`, Foresignal reads messages heard by your own node (the
`meshtastic-local` or `meshcore-local` source must be running) on the channel's **own private index** from the
senders in `allowedSenders` (sender → `report` or `ack`). Senders are Meshtastic node ids (`!a1b2c3d4`); MeshCore
channel messages do not carry the sender's key, only its advertised name, so a MeshCore sender is `name:Alice`
(anyone holding the channel key can use that name). Anything else is ignored without a reply; at most 10 commands
a minute per sender are read.

| Send | Reply | Does |
|---|---|---|
| `#E R FIRE 51.48,-0.12 smoke over the ridge` | `OK R7K2Q FIRE` | records a `field-report` in the private `mesh-reports` source |
| `#E R FLOOD @home water over the road` | `OK R…` | the same, located at place `home` |
| `#E ACK 0007` | `OK ACK #0007` | acknowledges the alert (`acknowledged_by` `mesh:!a1b2c3d4`; role `ack` only), audited |
| `#E STATUS` | `ok: open 0 crit, 2 warn, 5 info` | a summary of at most 133 characters |
| `#E HELP` | the grammar card | |

Types are FIRE, SMOKE, FLOOD, POWER, ROAD, MED, WX, SEC and OTHER; keywords are case-insensitive. A malformed
command answers `ERR {reason}`. Replies go out on the same channel through the airtime governor (a reply it
cannot fit is dropped). Report text is untrusted: it is never embedded, and Ask and briefings quote it only as
data.

**License-free operation only.** Unattended, automated transmission on amateur allocations is restricted by local
rules (for example FCC Part 97 in the US), which is one reason licensed mode is refused. These channels are for
license-free ISM operation under your region's rules.

### Chat commands

A Telegram or Matrix channel can also take commands: list and acknowledge alerts, ask questions, read or run
briefings and check health from the chat app you already get alerts in. Foresignal answers only chats you
allowlist on the channel, and each chat gets `read` or `agent` scope (never `admin`), checked exactly like an
API token of that scope.

**Privacy.** Telegram bot chats are not end-to-end encrypted, and the Matrix rooms used here must be
unencrypted. Telegram or your homeserver can read the alert titles, Ask answers and briefing excerpts
Foresignal sends. Replies carry summaries and links, never raw payloads or secrets. Leave `privateAccess`
off unless the chat may see your private sources.

**Telegram.**
1. Create a **dedicated** bot with @BotFather (`/newbot`). Interactive mode long-polls `getUpdates`, which works
   for only one consumer per bot and not while a webhook is set: do not share the token with another program.
   If the bot has a webhook, Foresignal reports `degraded` ("bot has a webhook; set takeOverWebhook or use a
   dedicated bot"); set `takeOverWebhook: true` only if you want Foresignal to delete it.
2. In groups, Telegram's **privacy mode** (on by default, `/setprivacy` in @BotFather) lets the bot see only
   messages that start with `/` and replies to it. That is all Foresignal needs; keep it on.
3. Create the channel with `interactive: true` and `discoveryMode: true`, then send `/whoami` to the bot from
   each chat. It answers with that chat's id and your user id. Discovery turns itself off after 10 minutes
   (the stored `discoveryMode` is set back to false); save the channel with `discoveryMode: true` to reopen it.
4. Put the ids into `allowedChats`. A group (negative id) must list `userIds`, otherwise any member could
   command.

```json
{"name": "Telegram ops", "type": "telegram",
 "config": {"chatId": "123456789", "interactive": true,
            "allowedChats": [{"chatId": "123456789", "name": "My phone", "scope": "agent"},
                             {"chatId": "-1001234567890", "name": "Family", "scope": "read", "userIds": ["123456789"]}]},
 "secrets": {"botToken": "…"}, "events": ["alert", "system"]}
```

**Matrix.** Create a separate bot account on your homeserver, invite it to an **unencrypted** room (an
encrypted room is ignored and the channel reports "encrypted rooms are not supported"), and use its access
token as the channel's `accessToken`. Allowlist the room id (`!abc:example.org`); a room with more than two
members must list the allowed `userIds` (`@ana:example.org`). Matrix has no buttons, so proposed Ask actions
come with a 6-character code: reply `/confirm CODE` or `/reject CODE`.

**Scopes and commands.** Messages from chats or users that are not allowlisted are ignored without a reply
(counted in `foresignal.chatops.rejected`). Every command is written to the audit log as
`chat:{channelId}:{chatId}[:{userId}]`.

| Command | Scope | What it does |
|---|---|---|
| `/help` | read | The commands this chat may use |
| `/status` | read | The glance as text |
| `/alerts [n] [critical\|warning\|info]` | read | Open alerts, newest first (at most 10); agent chats get Ack and Resolve buttons on Telegram |
| `/alert <id>` | read | Rule, record title, kind, source, observed time, state and a link |
| `/ack <id>`, `/resolve <id>` | agent | Acknowledge or resolve; recorded as the chat principal |
| `/snooze <id> <duration>` | agent | Snooze for `30m`, `2h` … (at most 7 days) |
| `/ask <question>` | read | Ask Foresignal; one conversation per chat, `/ask new` starts another. Proposed actions get Confirm/Reject buttons (Telegram) or a code (Matrix) in agent chats |
| `/brief [id\|name]` | read | The latest briefing run (first 1,500 characters and a link) |
| `/brief run <id\|name>` | agent | Run a briefing now; replies when it finishes |
| `/sources` | read | Degraded and failing sources with reasons |
| `/confirm <code>`, `/reject <code>` | agent | Decide a proposed Ask action (Matrix) |

Limits: `commandRateLimit` commands per chat per 10 minutes (30) and `askRateLimitPerHour` questions (10);
Ask also spends the LLM budget. `GET /v1/channels/{id}/interactive` shows whether the connector is connected,
its last poll and error, the discovery window and which chats are served. If the platform refuses the token,
the connector stops, a `system` event is sent to your other channels, and it starts again when you save the
channel. `PUT /v1/settings/notifications` with `{"chatops": {"enabled": false}}` stops every interactive channel
at once.

## Inbound webhooks

Some data cannot be polled: an SDR decoder, AIS-catcher, Home Assistant, a script, an n8n flow,
Alertmanager, a CAP publisher, or any homelab tool that already speaks ntfy or Gotify. These push
into a **custom source**. Pushed records go through the same writer as polled data, so versioning,
policies, touches, the change stream, triggers, tiles, search and retention all apply.

### Creating a custom source

```json
POST /v1/custom-sources
{"id": "custom-garage", "name": "Garage sensors", "kinds": ["sensor-reading"],
 "observedAtMode": "UPSTREAM", "recordTtl": "1h", "ttlAnchor": "RECEIVED",
 "volatileFields": ["rssi"], "significantFields": [], "titleField": "name",
 "tileProperties": ["value"], "rateLimitPerMinute": 60, "recordsPerMinute": 6000}
```

The response is `201` with `ingestKey` (`eik_…`), **shown once**; only its SHA-256 and first eight
characters (`ingestKeyPrefix`) are stored. Rotate it with
`POST /v1/custom-sources/{id}/ingest-key/rotate`; the old key stops working at once. The id must
start with `custom-` and be unique across all sources. The source is created enabled, appears in
`GET /v1/sources` with `custom: true`, and is always STANDARD tier.

- **Time base (`observedAtMode`).** `UPSTREAM` (the default) means every record carries its own
  `observedAt`; a record without one is rejected `MISSING_OBSERVED_AT`, never silently stamped.
  `RECEIVED` stamps the receipt time on every record (flagged `TIME_IMPUTED`). A source never
  mixes the two, and the mode can change only while the source has no records
  (`409 OBSERVED_AT_MODE_LOCKED`).
- **TTL.** `recordTtl` makes records expire from the current view; `ttlAnchor` counts it from receipt
  (`RECEIVED`, the default) or from `observedAt` (`OBSERVED`).
- **Volatile and significant fields.** Volatile fields (an RSSI, a battery level) are left out of the
  hash, so a change to them alone is a touch, not a version. Significant fields are the ones whose
  change is always worth a version; a SAMPLED or LATEST ingest policy needs them
  (`400 INGEST_POLICY_INVALID` if they are emptied). Editing either list changes the hash recipe: the
  writer rehashes the source's records before its next batch, so the edit never produces a burst of
  spurious versions.
- **Editing.** `PUT /v1/custom-sources/{id}` replaces the definition; the id cannot change, and
  removing a kind that still has current records is `409 KIND_IN_USE`.
- **Caps.** `rateLimitPerMinute` limits requests (the source's budget); `recordsPerMinute` (default
  6,000, at most 60,000) limits records. Over either is `429` with `Retry-After`, and a `REFUSED` row
  appears in `GET /v1/sources/{id}/fetches`. A body is at most 5 MB (decompressed; `gzip` is accepted)
  and 10,000 records.
- **Deleting** a custom source stops ingest; its records age out through retention.
- **`mapping`, `preset`, `presetParams` and `captureSamples`** are described under "Devices and
  mappings"; `profile`, `allowPathKey` and `ingestAllowCidrs` under "Personal location and privacy".
- **Members later prompts own** (`licenceTerms`, prompt 109) are refused with `400` until then.

Every push answers `200` with an `IngestOutcome` (`seen`, `persisted`, `touched`, `rejected` with
each item's index and reason, …) whenever the request itself was valid, even if some records were
rejected. `GET /v1/sources/{id}/fetches` shows each push with `trigger: PUSH`, the principal
(`ingest-key:<prefix>`, `compat-ntfy:<prefix>`, `compat-gotify:<prefix>` or `token:<name>`) and the
body's SHA-256.

### Credentials

Send the key as `X-Foresignal-Ingest-Key: eik_…`, as `Authorization: Bearer eik_…`, or as the
Basic-auth password (any user name), for devices that only do Basic. An admin token also works. A
key belongs to one source; pushing it to another is `401`.

### Formats

`POST /v1/ingest/{sourceId}` reads the `native` format; `POST /v1/ingest/{sourceId}/{format}` reads a
named one. `GET /v1/ingest/formats` lists them with their options. Options live in the source's
`formats` object, keyed by format id.

| Format | Body | Notes |
|---|---|---|
| `native` | GeoJSON FeatureCollection, or `{"records": [...]}` | Feature `id` is the external id; properties `kind` (optional with one kind), `observedAt`, `eventTime`, `validUntil`, `tombstone`, `quality`; the rest is the payload |
| `alertmanager` | Alertmanager webhook payload v4 | One record per alert (`fingerprint`); `kindLabel` picks the kind from a label; `resolved: tombstone` (default) or `record` |
| `cloudevents` | CloudEvents 1.0 structured, batch or binary mode | External id `subject`, else `source/id`; `typeKinds` maps CloudEvents types to kinds; answers the webhook validation handshake (`OPTIONS` with `WebHook-Request-Origin`) |
| `cap` | CAP 1.2 XML | `Alert` creates a record, `Update` also tombstones the referenced alerts, `Cancel` only tombstones; polygons and circles become geometry; non-`Actual` alerts are rejected unless `acceptNonActual` |
| `mapping` | Any JSON, or a form body | Uses the source's `mapping` member, a RecordMapping (see "Devices and mappings" below). The 024 `formats.mapping` JSON-Pointer options are no longer accepted |
| `ecowitt`, `wu-pws`, `aiscatcher`, `chirpstack-v4`, `tts-v3`, `airrohr`, `gmc` | The device's own payload | The device presets (see "Devices and mappings"); each replies the way its firmware expects |

`media` and `rasters` are the remote SatDump endpoints and `mapping-preview` is reserved
(`404 FORMAT_UNKNOWN`). A source created from a preset uses the preset's format on
`POST /v1/ingest/{sourceId}` too.

```sh
# native GeoJSON
curl -X POST http://foresignal.local:8080/v1/ingest/custom-garage \
  -H "X-Foresignal-Ingest-Key: $KEY" -H "Content-Type: application/geo+json" \
  -d '{"type":"FeatureCollection","features":[{"type":"Feature","id":"garage-temp",
       "geometry":{"type":"Point","coordinates":[-97.74,30.27]},
       "properties":{"observedAt":"2026-10-12T08:00:00Z","name":"Garage","value":31.5}}]}'

# a CloudEvent
curl -X POST http://foresignal.local:8080/v1/ingest/custom-garage/cloudevents \
  -H "Authorization: Bearer $KEY" -H "Content-Type: application/cloudevents+json" \
  -d '{"specversion":"1.0","id":"e1","source":"/ha/garage","type":"door","subject":"garage-door",
       "time":"2026-10-12T08:00:00Z","data":{"state":"open"}}'

# a CAP alert
curl -X POST http://foresignal.local:8080/v1/ingest/custom-alerts/cap \
  -H "X-Foresignal-Ingest-Key: $KEY" -H "Content-Type: application/cap+xml" --data-binary @alert.xml
```

**AIS-catcher.** Use the `aiscatcher` preset (see "Devices and mappings"); `gzip` is accepted.

**Home Assistant** (`configuration.yaml`):

```yaml
rest_command:
  foresignal_push:
    url: "http://foresignal.local:8080/v1/ingest/custom-home"
    method: POST
    headers:
      X-Foresignal-Ingest-Key: !secret foresignal_ingest_key
    content_type: "application/json"
    payload: >-
      {"records": [{"externalId": "{{ entity_id }}", "kind": "state",
        "observedAt": "{{ now().isoformat() }}", "payload": {"state": "{{ state }}"}}]}
```

**Python:**

```python
import requests
requests.post("http://foresignal.local:8080/v1/ingest/custom-garage",
              headers={"X-Foresignal-Ingest-Key": KEY},
              json={"records": [{"externalId": "probe-1", "kind": "sensor-reading",
                                 "observedAt": "2026-10-12T08:00:00Z", "payload": {"value": 21.4}}]},
              timeout=10).raise_for_status()
```

**Alertmanager** (`alertmanager.yml`), which cannot sign, authenticates with the key as a bearer
token:

```yaml
receivers:
  - name: foresignal
    webhook_configs:
      - url: "http://foresignal.local:8080/v1/ingest/custom-alerts/alertmanager"
        send_resolved: true
        http_config:
          authorization:
            type: Bearer
            credentials_file: /etc/alertmanager/foresignal-key
```

### ntfy and Gotify compatibility

Tools that publish to ntfy or Gotify can publish to Foresignal unchanged. Give such sources
`observedAtMode: RECEIVED` (the messages carry no observation time) and a TTL. The kind is the
source's single kind, or `notification` (which must then be one of its kinds); the payload is
`{title, message, priority, tags, click, topic}`. The external id comes from
`X-Foresignal-External-Id`, an ntfy tag `id=<x>`, or Gotify `extras["foresignal::record"].externalId`,
otherwise each message gets a new ULID.

- **ntfy:** the topic is the custom source id. `POST` or `PUT /compat/ntfy/{topic}` (body = message;
  `X-Title`, `X-Priority` (1–5 or `min`…`urgent`), `X-Tags`, `X-Click`),
  `GET` or `POST /compat/ntfy/{topic}/publish` (also `send`, `trigger`) with `?message=&title=…`, and
  the JSON form `POST /compat/ntfy` with `{"topic", "message", "title", "priority", "tags", "click"}`.
  Credentials: Bearer or Basic with the key, or ntfy's `?auth=` (base64 of `Bearer eik_…`).
- **Uptime Kuma:** add an ntfy notification with server URL `http://foresignal.local:8080/compat/ntfy`
  (Kuma posts the JSON form there), topic `custom-uptime`, and the ingest key as the access token.
- **Gotify:** `POST /compat/gotify/message` (JSON or form `title`, `message`, `priority`, `extras`);
  the key in `X-Gotify-Key` or `?token=` picks the source.
- **Proxmox:** add a Gotify notification target with server URL
  `http://foresignal.local:8080/compat/gotify` (Gotify clients post to `{server}/message`) and the
  ingest key as the token.

`?auth=` and `?token=` values are masked as `***` in logs and in the fetch log. A message carrying
this instance's own origin marker (the ntfy tag `foresignal-origin-<id>` or the Gotify extra
`foresignal::origin`) is acknowledged and dropped, so a notification channel pointed back at
Foresignal cannot feed itself.

### Signatures

A source can require a webhook signature per format under `verifiers.{format}`:

| Scheme | What is checked |
|---|---|
| `hmac-sha256` | `X-Signature-256: sha256=<hex>` (header configurable) over the raw body |
| `standard-webhooks` | `webhook-id`, `webhook-timestamp` and `webhook-signature: v1,<base64>` over `id.timestamp.body`, ±5 minutes, a replayed id refused for 10 minutes |
| `foresignal` | `X-Foresignal-Timestamp` and `X-Foresignal-Signature: sha256=HMAC("<ts>.<body>")`, the scheme of Foresignal's own webhook channel (instance to instance) |

```json
"verifiers": {"native": {"scheme": "hmac-sha256", "secret": "…", "keyOptional": false}}
```

With a verifier set, a request must carry a valid signature **and** the key
(`401 SIGNATURE_INVALID` otherwise); with `keyOptional: true` a valid signature alone authenticates.
Secrets are write-only: reads show `secretSet: true`, and omitting `secret` on an update keeps the
stored one. An admin token is not asked for a signature.

### A geofence rule on a custom kind

Custom kinds work in rules like any other:

```json
POST /v1/triggers
{"name": "Drone near the house", "sources": ["custom-adsb"], "kinds": ["drone"], "mode": "enter",
 "geofence": {"type": "Polygon", "coordinates": [[[-97.75,30.26],[-97.73,30.26],[-97.73,30.28],
              [-97.75,30.28],[-97.75,30.26]]]}, "severity": "warning"}
```

## Devices and mappings

Homelab devices that can only send their own payload (weather stations, AIS-catcher, LoRaWAN
servers, Alertmanager, air-quality sensors, Geiger counters) are onboarded with a **preset** or a
**RecordMapping**, with no code.

### Creating a source from a preset

```sh
curl -X POST http://foresignal.local:8080/v1/custom-sources -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"id":"custom-backyard-weather","preset":"ecowitt","presetParams":{"lon":-97.7431,"lat":30.2672}}'
```

`GET /v1/mappings/presets` lists the presets with their mapping, defaults, parameters and setup
text. The preset fills every member you leave out (kinds, field specs, time base, TTL, significant
and volatile fields, privacy, `allowPathKey`, the mapping and format options); anything you set
wins. `lon`/`lat` are rounded to 0.01° (about 1 km) unless `exactLocation: true`. The response
carries the ingest key and `setup`: the device instructions with the concrete URL, built from
`publicBaseUrl`. **Both are shown once**: the URL can contain the key.

**Cleartext and the LAN.** Most of this firmware speaks plain HTTP and can only put the key in the
URL path or the query string. Keep such devices on the same LAN as Foresignal: pushes with a key in
the path, a key in the query (`PASSWORD`) or by GET are accepted only from private addresses
(loopback, RFC 1918, link-local, ULA) unless the source's `ingestAllowCidrs` lists the sender
(`403 INGEST_SOURCE_DENIED` otherwise). Never forward these URLs over the internet without TLS.

### Ecowitt (`ecowitt`)

Consoles and gateways (GW1000/GW1100/GW2000, HP2551, WS2910 …) in the WS View Plus or Ecowitt app:
Weather Services → **Customized**.

| Setting | Value |
|---|---|
| Customized | Enable |
| Protocol Type | Ecowitt |
| Server IP / Hostname | the host of `publicBaseUrl` (for example `192.168.1.10`) |
| Path | `/v1/ingest/<sourceId>/k/<key>/ecowitt` |
| Port | the port of `publicBaseUrl` (for example `8080`) |
| Upload Interval | 16 seconds or more |

Fields: `tempC`, `rh`, `pressureHpa` (relative), `windDirDeg`, `windSpeedMs`, `windGustMs`,
`rainRateMmH`, `rainDailyMm`, `solarWm2`, `uvIndex`, `pm25` (channel 1), `lightningKm`,
`indoorTempC`, `indoorRh` and `model` (from `stationtype`); `dateutc` is the observation time.
`PASSKEY` (a hash of the console's MAC address) is never stored. Policy LATEST with a 5-minute
keyframe: the live value follows every upload, history keeps one version per 5 minutes plus model
changes. One source per station (the external id is `station`).

### Weather Underground protocol (`wu-pws`)

Any station software that uploads with the WU PWS protocol, or an Ecowitt console with Customized →
Protocol Type **Wunderground**: Server `<host>`, Path `/v1/ingest/<sourceId>/wu-pws?`, Port
`<port>`, Station ID anything, Station Key `<key>`. Other software sends
`GET /v1/ingest/<sourceId>/wu-pws?ID=any&PASSWORD=<key>&dateutc=now&tempf=…`. The key is the
`PASSWORD` parameter; `ID` is ignored. The reply is `success`. Every observation is stamped with its
receive time (`observedAtMode: RECEIVED`), because most stations send `dateutc=now`.

### AIS-catcher (`aiscatcher`)

```
AIS-catcher … -H http://foresignal.local:8080/v1/ingest/custom-ais/aiscatcher PROTOCOL AISCATCHER USERPWD x:<key> GZIP on INTERVAL 30
```

The key is the Basic password. One vessel record per MMSI; position reports and static messages are
merged into one snapshot (`sog`, `cog`, `heading`, `navStatus`, `name`, `callsign`, `destination`,
`shipType`). The AIS "not available" values (lat 91, lon 181, speed 102.3, course 360, heading 511)
are dropped. SAMPLED vessel policy, 30-minute TTL, 3 days of history. The station position AIS-catcher
sends is never stored. Where AIS-catcher runs next to Foresignal, the built-in `local-ais` source
(prompt 085) is preferred.

### ChirpStack v4 (`chirpstack-v4`)

Applications → your application → Integrations → **HTTP**: payload encoding **JSON**, event
endpoint `http://foresignal.local:8080/v1/ingest/<sourceId>/chirpstack-v4`, header
`Authorization: Bearer <key>`. Uplinks (`?event=up`) become one record per DevEUI: `name`, `fPort`,
`fCnt` and up to 32 numeric keys of the decoded `object`; `rxInfo[0]` RSSI and SNR go to
`quality.extra` (`lora.rssi`, `lora.snr`). `location` events move the device; other events are
acknowledged and ignored; the Protobuf encoding is refused (415). Position: `object.latitude` and
`object.longitude`, else the last `location` event, else the preset's `lon`/`lat`. **Private by
default**: trackers carry personal data.

### The Things Stack v3 (`tts-v3`)

Applications → your application → Integrations → **Webhooks** → Custom webhook: format JSON, base URL
`http://foresignal.local:8080/v1/ingest/<sourceId>/tts-v3`, additional header
`Authorization: Bearer <key>`, event type **Uplink message**. Same record shape as ChirpStack, from
`uplink_message.decoded_payload`. Position: the `frm-payload` location, else the device's configured
location (`locations.user`, flagged `lora.locationSource=static`), else the preset's `lon`/`lat`.
Private by default.

### Alertmanager (`alertmanager`)

```yaml
receivers:
  - name: foresignal
    webhook_configs:
      - url: http://foresignal.local:8080/v1/ingest/<sourceId>/alertmanager
        send_resolved: true
        http_config:
          authorization:
            type: Bearer
            credentials_file: /etc/alertmanager/foresignal-key
```

The preset uses the built-in `alertmanager` format with: external id `{receiver}:{fingerprint}`,
`severity` and `alertname` as significant fields, and a resolved alert kept as a new version with
`status=resolved` and `validUntil = endsAt + 1 h`. `presetParams.tombstoneOnResolve: true` ends the
record instead; `lon`/`lat` place alerts without location labels.

### Sensor.Community airrohr (`airrohr`)

On the sensor's configuration page (`http://<sensor>/config`) → APIs → **Send data to own API**:
Server `<host>`, Path `/v1/ingest/<sourceId>/airrohr`, Port `<port>`, User anything, Password `<key>`.
The `sensordatavalues` pairs become `pm1`, `pm25`, `pm4`, `pm10` (SDS011, PMS or SPS30, the first
present wins), `tempC`, `rh`, `pressureHpa` (BME280, converted from Pa) and `firmware`; the Wi-Fi
signal goes to `quality.extra wifi.rssi`. The names match the `openaq` adapter. LATEST with a
10-minute keyframe.

### GQ GMC Geiger counter (`gmc`) — unverified

Server → Website `<host>`, URL `/v1/ingest/<sourceId>/k/<key>/gmc`. The counter sends
`GET …?AID=…&GID=…&CPM=…&ACPM=…&uSV=…`; the reply is `OK.`. Fields `cpm`, `acpm`, `doseRateUSvH`
and `geigerId`; `AID` is never stored. **This preset was written from the protocol description, not a
capture (2026-10).** Turn on `captureSamples`, check the first upload under `/samples`, and adjust the
mapping if the counter differs.

### Raspberry Shake rsudp alarm (`rsudp-alarm`) — unverified

`contrib/rsudp/foresignal_alarm.py`, run by rsudp's `custom` module on every alarm, POSTs
`{"event": "ALARM", "time": …, "station": …, "channel": …}` to `/v1/ingest/<sourceId>/k/<key>/mapping`. Each post
becomes a `seismic-trigger` record (externalId `station.channel`, `eventTime` = `time`) at the station point given as
`presetParams` `lon`/`lat`. **Written from rsudp's documentation, not a capture (2026-10).** See "Local RF decoders".

### Writing a mapping

A `RecordMapping` is data, not a script. It goes in the source's `mapping` member:

```json
{"version": 1,
 "select": "$.items[*]",
 "externalId": {"template": "probe-{{$.serial}}"},
 "observedAt": {"path": "$.ts", "format": "epochS"},
 "geometry": {"lon": {"path": "$.pos.lon"}, "lat": {"path": "$.pos.lat"}},
 "fields": [
   {"name": "tempC", "path": "$.t_f", "type": "number", "unit": {"from": "[degF]", "to": "Cel"}},
   {"name": "battery", "path": "$.bat", "type": "integer", "sentinels": [-1]}
 ],
 "drop": ["apikey"],
 "filter": [{"path": "$.type", "op": "==", "value": "reading"}]}
```

- Values are `{path}` (Jayway JSONPath; `$` is the document, or each item under `select`;
  `$['frm-payload']` quotes a name), `{template}` (substitution only) or `{const}`. Deep scan
  (`..`), function calls and regex filters are refused.
- Form and query inputs are flat objects of strings: `$.tempf`.
- `observedAt` is `{path, format: iso | epochS | epochMs | pattern:<Java DateTimeFormatter>, zone}`
  under `observedAtMode: UPSTREAM`, and `{received: true}` under RECEIVED; mixing them is refused.
- Units come only from the built-in table: `[degF]`→`Cel`; `[mi_i]/h`, `km/h`, `[kn_i]`→`m/s`;
  `[in_i]`→`mm`; `[in_i]/h`→`mm/h`; `[in_i'Hg]`, `Pa`→`hPa`; `[mi_i]`→`km`; `[ft_i]`→`m`.
- `pairs` flattens name/value arrays (`$pairs.<name>`); `spread` copies an object's scalar members;
  `tombstone`, `quality` (`accuracyMeters`, `extra`) and `eventTime`/`validUntil` are optional.
- A value that cannot be converted (or NaN) leaves that field out; an out-of-range coordinate leaves
  the geometry out.

Errors come back as `400 MAPPING_INVALID` with a path per problem, such as
`mapping.fields[3].unit.from`.

### Preview, samples and test

- `POST /v1/mappings/preview` with `{mapping, sample: {contentType, body | query}, sourceId?}`
  shows exactly what ingest would write, normalized, and writes nothing. Warnings name unmapped
  input keys, fields missing from the source's `fieldSpecs`, significant fields a SAMPLED or LATEST
  policy needs, and a time-base mismatch.
- `captureSamples: true` on the source keeps the last five raw requests in memory for one hour,
  redacted, without headers except `Content-Type`: `GET /v1/custom-sources/<id>/samples`.
- `POST /v1/custom-sources/<id>/mapping/test` runs the stored mapping, or `{"mapping": …}`, over
  those samples.

To write a mapping for a new device: create a plain custom source with `captureSamples: true`, point
the device at it, read `/samples`, preview a mapping against one of them, then save it with
`PUT /v1/custom-sources/<id>`.

### Asking the LLM for a draft

`POST /v1/mappings/suggest` with `{sample, hints: {kind, description, sourceId}, confirmEgress}`
asks the chat provider for a draft. The sample is sent as untrusted data and the draft is validated
(two repair rounds at most), then previewed. **It is never saved**: review it and save it yourself.
A remote provider needs `confirmEgress: true` (`409 EGRESS_CONFIRMATION_REQUIRED`), because the
sample leaves your network; a private source's sample is never sent to one. It spends the
provider's budget like any other call.

## External data providers

The External Data Protocol (EDP, [docs/External-Data-Protocol.md](External-Data-Protocol.md)) lets
anyone add a data source without changing Foresignal: a provider describes itself with a
**manifest** and pushes or serves **items**. `GET /v1/external/protocol` shows the protocol version
and item types; `GET /v1/external/protocol/schemas/{name}` serves the JSON Schemas.

### Registering a provider

- **By manifest or URL (admin):** `POST /v1/external/sources` with `{"manifest": {...}}` or
  `{"manifestUrl": "https://provider.example/.well-known/foresignal-edp.json"}` (fetched once), plus
  `"approve": true` to activate it at once. The response carries `ingestKey` once. The provider
  becomes the source `ext-{source.id}`.
- **By registration token:** create one with `POST /v1/external/registration-tokens`
  (`{"name", "expiresAt", "maxUses" (default 1), "sourceIdPrefix"}`); the `fsx_…` token is shown
  once. The provider calls `POST /v1/external/register` with `Authorization: Bearer fsx_…` and its
  manifest; the source is created **pending**. A provider id outside the token's prefix is
  `409 SOURCE_ID_NOT_ALLOWED`; a used-up or expired token is `401 REGISTRATION_TOKEN_INVALID`.
  `GET /v1/external/registration-tokens` lists unexpired tokens (never the secret);
  `DELETE /v1/external/registration-tokens/{id}` revokes one.

A manifest is validated first: `400 MANIFEST_INVALID` lists every error with its JSON Pointer, and
a protocol minor version above what this instance supports is `400 PROTOCOL_UNSUPPORTED`.

### Approval and the access block

Nothing from a **pending** or **suspended** provider is stored (pushes answer
`409 SOURCE_PENDING_APPROVAL` / `SOURCE_SUSPENDED`, and it is not pulled). Before approving, read
`GET /v1/external/sources/{id}`: the **access block** says how the provider gets its data
(`official`, `unofficial-api`, `scrape`, `browser`, `other`), how likely it is to break
(`fragility`) and a summary of the upstream's terms; the licence's `redistribution` sets the egress
(`allowed` → `OPEN`, otherwise `LOCAL_ONLY`); `private` marks personal data. Then
`POST /v1/external/sources/{id}/approve`. `POST …/suspend` stops it (records stay);
`DELETE /v1/external/sources/{id}` removes it (records age out through retention).

Provider URLs (manifest, pull, search) must be `https` and may not reach private, loopback,
link-local, carrier-grade NAT or cloud-metadata addresses, including by DNS rebinding or redirects
(only same-host redirects are followed). For a provider on your own network, register it with
`"allowPrivateNetwork": true`; metadata addresses stay refused even then.

A provider (with its ingest key) or you can send `PUT /v1/external/sources/{id}/manifest`. Additive
changes (descriptions, presentation, new optional fields) apply at once; a change to streams, kinds,
item types, modes, payload schemas or provider URLs returns the source to **pending** until you
approve it again, and the response lists those changes in `breakingChanges`.

### Push and pull

- **Push:** the provider sends batches to `POST /v1/external/sources/{id}/items` (JSON, or NDJSON
  with an optional header line) with its ingest key, the same credentials as custom sources.
- **Pull:** a manifest with `transport.pull` makes Foresignal poll `itemsUrl?cursor=&limit=500` every
  `intervalSeconds`, following `nextCursor` and storing the cursor. Enter the credential with
  `PUT /v1/sources/{id}/credentials`: `token` for `bearer`, `value` for `header`, `username` and
  `password` for `basic`. Requests count against the source's budget; provider failures show in its
  health and in `pull.lastError`.

Layer streams become ordinary map layers (their `presentation` hints appear on the provider and in
the kind's TileJSON); atomic streams are stored, searchable and usable in rules, but kept out of the
default tile layer list until prompt 138 binds them. `status` items change the provider's health
without creating records. Snapshot streams remove entities only after a complete, validated snapshot
of the same scope; a snapshot that would remove more than half of a scope is held and the provider
turns degraded.

### Reading validation errors

A push answers `200` with `accepted`, `rejected` (each item's index, stream, id, reason such as
`SCHEMA_INVALID`, `PAYLOAD_INVALID`, `GEOMETRY_INVALID`, `PERIOD_INVALID` or `FORECAST_INVALID`, and
`schemaErrors` with JSON Pointers) and the writer's `outcome`. Valid items in the same batch are kept.
`GET /v1/external/sources/{id}` shows per-stream counts and the last validation errors.
`POST /v1/external/validate` (agent) checks a manifest and sample items without storing anything,
for provider developers and CI.

## Streaming for frontends, tiles and asOf

**Snapshot plus stream.** A frontend that mirrors current state:

1. `GET /v1/records?source=…&order=id` returns the first page and `watermarkSeq`. Page on with
   `cursor`; the cursor pins the snapshot at that seq, so later changes are left to the stream.
2. Open `GET /v1/changes/stream?source=…&after=<watermarkSeq>` (SSE). Every persisted `change` has
   its seq as the event id, so a reconnect with `Last-Event-ID` resumes exactly where it stopped.
3. Apply an event only if its `observedAt` is not older than what you hold.

Stream parameters: `source`, `kind`, `bbox`, `live` (default true: non-persisted position updates as
`live` events, conflated to one per entity per second), `touches` (default false: `touch` events for
re-observations) and `payload` (include the payload in `change`). `/v1/events/stream` multiplexes
changes, alerts and other topics on one connection, and its filter can be changed in place with
`PATCH /v1/events/stream/{streamId}/filter`.

**When to start over.**

- `reset` (`buffer_overflow`, `replay_cap`, or a resume below the retained floor) ends the stream:
  reconnect with the `resumeAfter` it carries, or re-snapshot when it says so.
- `resync` (`gap`, `live_cap`, `live_dropped`) keeps the stream open but means live positions were
  lost: re-read current state for the affected sources.
- `GET /v1/changes?after=<seq>` answers `410 RESYNC_REQUIRED` when retention purged records after
  that seq; take a new snapshot. Routine version deletes never cause it.
- A `heartbeat` arrives every 15 s; a proxy must not buffer the stream (see the Caddy example).

**Browsers and map loaders without an `Authorization` header** use a ticket instead of putting a
token in the URL: `POST /v1/stream-tickets` (any token, `read` or higher) and append
`?ticket=fst_…` to the stream or tile URL. See [Tickets for browser clients](#tickets-for-browser-clients).

**Tiles.** `GET /v1/tiles/{kind}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current state for one
kind (for example `/v1/tiles/earthquake/3/1/3.mvt`), with `agg=grid` or `agg=hex` for aggregates.
`GET /v1/tiles.json` and `/v1/tiles/{kind}.json` are TileJSON for MapLibre.

**asOf.** `GET /v1/records?…&asOf=2026-10-11T12:00:00Z` returns what Foresignal *knew* at that time
(transaction time): each entity at the version stored then, even if it was revised later. The
response echoes `asOf` and reports `horizon`, the earliest time the involved sources' retention still
supports; earlier times are `400 ASOF_BEYOND_RETENTION`. `asOfSeq` pins the same thing by seq, and
`GET /v1/records/{source}/{id}/versions` lists every stored version.

**Time travel.** The same transaction-axis replay serves paused maps (`asOf` on tiles and TileJSON),
trails (`GET /v1/records/{source}/{id}/track`), playback (`GET /v1/history/frames`) and scrubber
histograms (`GET /v1/timeline`). Past asOf tiles and tracks are sent with
`Cache-Control: …, max-age=31536000, immutable` once their instant is at least 2 minutes old, at least
1 hour after the retention horizon and not pending downsampling, so a caching proxy (Caddy, a CDN)
may keep them; `public` is used only while `openReads` covers the path and no private source is
involved. Chrome ignores `immutable` but honors the year-long `max-age`. The client recipe is in the
Client Guide ("Time travel").

## Tokens, pairing and the audit log

### Tokens and scopes

Every caller authenticates with `Authorization: Bearer fsr_…`. Scopes, least to most privileged:

| Scope | Can |
|---|---|
| `read` | Every data read (needed only when `openReads` is off), stream and tile tickets, its own device report; later Ask, search and Web Push |
| `agent` | `read` plus managing triggers and briefings and the alert lifecycle; sees private sources |
| `admin` | Everything: sources, credentials, channels, LLM providers, tokens, pairing codes, settings, the audit log |

- **Create:** `POST /v1/tokens {"name": "openclaw", "scope": "agent", "expiresAt": null}` answers
  `201` with `token` once; only its SHA-256 and its first eight characters (`tokenPrefix`) are
  stored. Every token belongs to a principal: name an existing one with `principalId` (id or
  handle), or a new one named after the token is created.
- **List:** `GET /v1/tokens` shows names, scopes, prefixes, `lastUsedAt` (updated at most once a
  minute), expiry, revocation, device and restrictions, never values.
- **Revoke:** `DELETE /v1/tokens/{id}`. The token stops working at once, its tickets are dropped and
  its open streams close. The `bootstrap` token cannot be revoked (`409 BOOTSTRAP_TOKEN`); rotate it
  with `POST /v1/system/admin-token/rotate`.
- **Errors:** a missing or invalid token is `401`; a token whose scope is too low is
  `403 INSUFFICIENT_SCOPE` with `requiredScope`.
- **Always authenticated:** ticket minting, the device report and, from their prompts, Ask, search
  and Web Push need a token even while `openReads` is on, because they cost money or send messages.

### Restricted tokens

`restrictions` narrows a token further:

```json
POST /v1/tokens
{"name": "kitchen-dashboard", "scope": "read",
 "restrictions": {"endpoints": ["GET /v1/records/**", "GET /v1/tiles/**", "GET /v1/tiles.json"]}}
```

- `endpoints` (`METHOD /v1/path/pattern`, Ant style, method may be `*`): any other request is
  `403 ENDPOINT_NOT_ALLOWED`. Each pattern must match an operation the scope can reach.
- `sources`, `kinds`, `bbox` and `area` (a GeoJSON Polygon or MultiPolygon, at most 2,000 vertices)
  narrow every read: records, history, changes, tiles, streams, search, alerts, the inbox, the digest,
  briefings, MCP tools and Ask. A request filter is intersected with the restriction, and a record
  outside it answers `404`. `excludePrivate: true` hides private sources even from an agent token,
  and `maxRequestsPerMinute` sets the token's own read rate. `placeIds` (036) limits the token to
  the union of those places' areas (an unknown or deleted place lets nothing through); it is refused
  for the anonymous profile and feed keys, because places are never anonymous. The federation
  members (113) are refused until their prompt. Admin tokens cannot carry restrictions
  (`400 RESTRICTION_NOT_ALLOWED`); an admin can mint any token anyway.

See "Exposing Foresignal" below for kiosks, family phones, feed keys and limits.

### Exposing Foresignal

Before Foresignal is reachable beyond your LAN (a tailnet shared with others, a public name behind
Caddy), close the perimeter:

- **Restricted tokens for kiosks and family phones.** Give each device a `read` token restricted to
  what it shows, for example a wall map:
  `{"name":"hall-map","scope":"read","restrictions":{"kinds":["earthquake","weather-alert"],"bbox":[-80.5,43.0,-78.5,44.5]}}`.
  A lost phone then exposes only that slice, and revoking the token cuts it off.
- **The anonymous profile.** While `openReads` is on, tokenless reads run under `security.anonymous`
  (`PUT /v1/settings/security`), for example
  `{"openReads":true,"anonymous":{"kinds":["earthquake"],"maxRequestsPerMinute":60}}`. Private
  sources are never served anonymously, and the inbox, places, watchlists and every
  always-authenticated operation need a token. `GET /v1/system` warns
  (`security.warnings: ANONYMOUS_UNRESTRICTED`, also logged) when `openReads` is on, `publicBaseUrl` is
  a public name and the profile restricts nothing; turn `openReads` off or restrict the profile.
- **Feed keys for clients that cannot send headers** (KML NetworkLinks, iCal subscriptions, feed
  readers, Home Assistant `geo_json_events`, a browser `EventSource`):
  `POST /v1/feed-keys {"name":"ha-map","prefixes":["/v1/records"],"restrictions":{"kinds":["earthquake"]},"expiresAt":"2027-06-01T00:00:00Z"}`
  returns the `fsf_` key and a ready-made URL per prefix, once. The URL `/v1/k/{key}/records?…`
  serves `GET`/`HEAD` of paths under the prefixes only, inside the key's restriction, and never
  private sources. A restriction (sources, kinds or area) is mandatory and the key expires within
  365 days. `GET /v1/feed-keys` lists keys without values; `DELETE /v1/feed-keys/{id}` revokes one,
  after which its URLs answer `410`. Treat the URL as a password: responses carry
  `Referrer-Policy: no-referrer` and `X-Robots-Tag: noindex`, and are never cached publicly.
- **Tile keys for map clients** (MapLibre, maplibre_gl, Cesium, a web map's `EventSource`): an `agent`
  token mints `POST /v1/tile-keys {"name":"kitchen-tablet","prefixes":["/v1/products/","/v1/styles","/v1/changes/stream"],"ttl":"7d"}`
  and gets the `fsk_` key and its `urlBase` once. They are the same path carrier as feed keys, with map
  prefixes only (`/v1/products/`, `/v1/styles`, `/v1/media/`, `/v1/tiles/`, `/v1/maps/static`,
  `/v1/sample`, `/v1/ground-stations/`, `/v1/changes/stream`, `/v1/events/stream`, `/v1/map/`), a lifetime of at
  most 30 days, an optional restriction, and never private sources. An unknown, expired or revoked key
  answers `401 INVALID_TILE_KEY`. Mint one per device and rotate weekly; `docs/Frontend-Guide.md` has
  the client recipes. Responses through a tile key follow the normal cache rule, so content-addressed
  tiles stay cacheable (`public` only while `openReads` is on and the key has no restriction).
- **Limits.** Every client is rate-limited per minute. Defaults: read 600, tiles 1,200, streams 30
  opens and 8 open at once, expensive (search, Ask, LLM tools, sampling) 20, write 120, and
  anonymous 120 per IP. Change them with `PUT /v1/settings/ratelimits`; they apply immediately. To
  give a kiosk or dashboard token a different read limit, set `maxRequestsPerMinute` in its
  restrictions. Admin tokens are not limited on reads and tiles. Set `metricsPerMinute` to limit
  Prometheus scrapes per IP, and `metricsOpen: false` (`PUT /v1/settings/security`) to require an
  admin token for `/actuator/prometheus`. Behind a reverse proxy, list the proxy in
  `FORESIGNAL_TRUSTED_PROXIES`; otherwise every client shares the proxy's address and its limits.
  Repeated wrong credentials from one address get `429` with a growing `Retry-After`, up to 5
  minutes, while a correct token from the same address always works. Clients should honor
  `Retry-After`.
- **Log redaction.** Feed and tile keys are credentials in the path. Foresignal's access log (off
  unless `FORESIGNAL_ACCESS_LOG=true`) logs the URI with the key cut to its first eight characters
  (`/v1/k/fsk_AbCd…/…`, the same prefix the key listings show) and without the query string. `docker/caddy/Caddyfile.example` redacts both the key and `?ticket=` values in Caddy's
  log; do the same in any other proxy that logs request lines.
- **Outbound URLs.** Channel URLs, LLM `baseUrl`s, push endpoints and other operator-supplied URLs
  are checked against the outbound URL policy on save and on every send: link-local and cloud
  metadata addresses are always refused, and LAN addresses only where the field allows them.
  `PUT /v1/settings/outbound` adds `allowHosts`/`allowCidrs` and `denyHosts`/`denyCidrs`
  overrides (deny wins).

### Pairing a phone

A phone gets its own `read` or `agent` token without ever seeing the admin token:

1. `POST /v1/pairing-codes {"name": "Pixel", "scope": "read", "ttl": "10m"}` returns `code`
   (`XXXX-XXXX`, single use, at most 1 h) and `qrPayload`
   (`foresignal://pair?u=<publicBaseUrl>&c=<code>`).
2. Show `GET /v1/pairing-codes/{id}/qr.svg` and scan it with the app, or type the code.
3. The app calls `POST /v1/pairing/claim {"code": "…", "device": {"deviceName": "Pixel", "platform": "android"}}`
   and receives its token once.

`admin` is never pairable. Codes are stored hashed and claims are limited to 5 per minute per client
IP and 50 per hour overall (`429 RATE_LIMITED`); every claim is audited. `GET /v1/pairing-codes`
lists open codes (never their values) and `DELETE` withdraws one. The QR image is held in memory, so
after a restart create a new code. Apps report their device later with
`PUT /v1/tokens/current/device`.

### Tickets for browser clients

`EventSource` and map tile loaders cannot send headers, and tokens never belong in URLs. Mint a
ticket with the token instead:

- `POST /v1/stream-tickets {"purpose": "stream"}` → `{"ticket": "fst_…", "expiresAt": …}`. Use it as
  `?ticket=` on `/v1/changes/stream`, `/v1/alerts/stream` or `/v1/events/stream`. The first connect
  must come within 60 s; `EventSource` reconnects with the same URL keep working up to 5 minutes
  after a disconnect and 24 h in total, one stream at a time (`429 STREAM_LIMIT` for a second).
- `{"purpose": "tiles", "ttl": "10m"}` gives a ticket for `/v1/tiles/**` and TileJSON, reusable for
  any number of requests until it expires (at most 1 h).

A ticket carries its token's scope and restrictions, never more; it is refused in the
`Authorization` header and on other paths, is stripped before logging, and is held in memory: after
a restart or a `401`, mint a new one.

### Trusted proxies

Behind Tailscale Serve or Caddy, Foresignal sees the proxy's address. Tomcat honours
`X-Forwarded-For` and `X-Forwarded-Proto` only from addresses matching `FORESIGNAL_TRUSTED_PROXIES`
(a regular expression, or a comma-separated CIDR list). The default trusts loopback only
(`127\.0\.0\.1|0:0:0:0:0:0:0:1|::1`), which is right for a proxy on the host
(`tailscale serve --bg 8080`, Caddy on the host). A proxy in another container reaches Foresignal
from the Docker network, so trust that network, for example
`FORESIGNAL_TRUSTED_PROXIES='172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3}'` for the compose
`tailscale` and `web` profiles. Never trust addresses a client can reach directly: a client could then forge its
IP. The resolved client IP is what the audit log and the pairing limits use.

### Deep links (`clientLinks`)

`PUT /v1/settings/server` also sets `timezone` and `clientLinks`, the templates apps and
notifications use: `alert` (`foresignal://alerts/{id}`), `record`, `briefingRun` and `pair`. Templates
must use `foresignal://` or an `https://` link you have verified for the app. Once you set
`clientLinks`, alert notifications carry an "Open in app" link next to "Open alert".

### The audit log

`GET /v1/audit` (admin) lists, newest first: every authenticated write with its token, scope,
operation, status, target and client IP; every `401`/`403` on a write path; pairing claims; token
revocations. Filter with `since`, `until`, `tokenId`, `operationId` and `status`, and page with
`cursor` and `limit` (up to 500). Entries never contain secrets, token values or request bodies.
They are kept for `retention.audit` (default 90 days). Writing the log never slows a request: under
extreme load entries are dropped and counted (`foresignal.audit.dropped`).

## LLM

An LLM is optional. Without one, every other feature works exactly as before: briefings use
digest mode, search matches text, enrichment is skipped and Ask answers `409 LLM_NOT_CONFIGURED`.
With one, Foresignal can answer questions about its data (Ask), write briefings, enrich alerts and
search by meaning. Every LLM endpoint needs the `admin` scope.

### Providers and roles

A provider is one model API. Three types exist:

| Type | What it talks to | Defaults |
|---|---|---|
| `anthropic` | Claude, through the official Anthropic Java SDK | `baseUrl` `https://api.anthropic.com`, `chatModel` `claude-opus-5-5`, effort `medium`, `maxTokens` 16000. An `apiKey` is required. No embeddings. |
| `openai-compatible` | OpenAI, OpenRouter, Groq, Mistral, vLLM, LM Studio, llama.cpp server, LocalAI | `baseUrl` is required (for example `https://api.openai.com/v1`) |
| `ollama` | A local Ollama server | `baseUrl` `http://ollama:11434` |

Two roles use providers: `chat` (Ask, briefings, enrichment) and `embedding` (search). Assign them
with `PUT /v1/llm/settings`. A provider needs a `chatModel` for the chat role, and an
`embeddingModel` plus `embeddingDimensions` for the embedding role. Anthropic has no embeddings
endpoint, so pair a Claude chat provider with an Ollama or OpenAI-compatible embedding provider.
A provider that holds a role cannot be deleted (`409 LLM_PROVIDER_IN_USE`) or changed so that it
can no longer serve it; reassign the role first.

`baseUrl` must be `http` or `https` with no user name, password or fragment. Addresses on your own
network are allowed, because a local model is the point.

### A local model (nothing leaves the box)

```sh
docker compose --profile local-llm up -d
docker compose exec ollama ollama pull qwen3:14b
docker compose exec ollama ollama pull nomic-embed-text
```

Then add the provider and assign both roles:

```sh
curl -X POST http://localhost:8080/v1/llm/providers -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"id":"ollama","type":"ollama","chatModel":"qwen3:14b",
  "embeddingModel":"nomic-embed-text","embeddingDimensions":768}'
curl -X PUT http://localhost:8080/v1/llm/settings -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"chatProvider":"ollama","embeddingProvider":"ollama"}'
curl -X POST http://localhost:8080/v1/llm/providers/ollama/test -H "Authorization: Bearer $TOKEN"
```

The Ollama port is bound to `FORESIGNAL_BIND` (localhost by default), like the other services.
If the chat model does not support tools, the provider shows `supportsTools: false` and Ask
answers from retrieved records instead of calling tools.

### Claude

Create an API key in the Anthropic Console (<https://console.anthropic.com>), then:

```sh
curl -X POST http://localhost:8080/v1/llm/providers -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"id":"claude","type":"anthropic","apiKey":"sk-ant-..."}'
```

The key is encrypted with the install key and never returned (`apiKeySet: true`). To change
anything else later, send the provider without `apiKey` and the stored key is kept.

Foresignal sets the effort explicitly for each use: Ask and briefings `medium`, enrichment `low`.
`options.effort` on the provider overrides that for every call (`low`, `medium`, `high`, `xhigh`,
`max`); higher effort spends more tokens. Calls enable Anthropic's server-side refusal fallback, and
a refusal is reported as such (the test endpoint shows `refusalCategory`). The stable system prompt
and tool definitions are cached by Anthropic, so repeated Ask and briefing calls are cheaper; the
`cachedTokens` figures show how much was served from the cache.

### Budgets and costs

Every provider has `requestsPerMinute` (default 30) and `tokensPerDay` (default 2,000,000, counted
as input plus output tokens and reset at 00:00 UTC). Before each call Foresignal estimates its
tokens (prompt characters / 4, plus the output cap) and refuses the call with
`429 LLM_BUDGET_EXCEEDED` when the estimate does not fit in what is left today. A refused call is
never sent. The first daily-budget refusal of a day, and a provider that fails three calls in a row, raise a
`system` notification. Restarting does not reset the day: it is seeded from the recorded usage.

`GET /v1/llm/usage?provider=&from=&to=&resolution=` shows requests, input, output and cached tokens,
errors and refusals per minute, hour or day; `GET /v1/llm/providers` shows each provider's
`tokensToday`, health (`status`, `lastError`, `consecutiveFailures`) and roles; `/v1/system` shows
the `llm` block. To estimate money, multiply the tokens by your provider's prices; the test
endpoint itself spends a few tokens.

Every call is logged with its purpose, provider, model, token counts (cached included), duration
and stop reason. Prompts and responses are logged only when `logContent` is `true` in
`PUT /v1/llm/settings`, at DEBUG level. API keys are never logged.

### What leaves the box

With a frontier provider (Claude, OpenAI, or any remote OpenAI-compatible API), the records the
model reads to answer are sent to that provider. That is your choice to make. Records of private
sources (personal data such as family locations) are withheld from remote providers and sent only
to a local one: an `ollama` provider, or one you mark with `options.local: true` because it runs on
your own network. The `local-llm` profile keeps everything on this machine.

## Search

`GET /v1/search?q=…` finds records (and, once those prompts land, briefing runs and enriched
alerts) by words, by meaning, or both. It needs a token even when open reads are on, because a
semantic query spends the embedding budget.

| `mode` | How it ranks | Needs |
|---|---|---|
| `text` | Full-text match on the embedded text (`websearch_to_tsquery`), plus a substring match on each record's search fields for records that have no document | Nothing; always works |
| `semantic` | Cosine distance between the embedded query and each document | pgvector, an embedding provider and a built index; otherwise `409 SEMANTIC_UNAVAILABLE` |
| `hybrid` (default) | Reciprocal rank fusion of both | Falls back to text with `degraded: true` and a `reason` when semantic search is off |

Filters: `source`, `kind`, `docType`, `bbox`, and `since`/`until` on the effective time (event time
when the record has one, else observation time; `timeField=observed` for the observation time).
Record hits carry the persisted `seq` to cite and the current record. Private sources are never
embedded, and their records only appear in text results for callers allowed to see them. MCP
clients get the same search as the cost-bearing `search` tool.

### The database image

Semantic search stores vectors with the pgvector extension in the same PostgreSQL. The compose
`db` service builds `docker/db` (image `foresignal-db:17-3.5-pgvector`): the pinned
`postgis/postgis:17-3.5` plus `postgresql-17-pgvector` from the PostgreSQL apt repository. Existing
data volumes keep working; the first start after the switch runs `CREATE EXTENSION vector`.

```sh
docker compose build db && docker compose up -d
```

Without pgvector (an external PostgreSQL, or the plain PostGIS image) Foresignal starts normally,
text search works, and `GET /v1/search/status` says why semantic search is off.

### Choosing an embedding model

Assign an embedding provider in `PUT /v1/llm/settings` (see "LLM"). A local Ollama with
`nomic-embed-text` (768 dimensions) keeps everything on this machine; an OpenAI-compatible
provider works the same way. The first assignment builds the index from the current records;
until it finishes, `hybrid` answers from text and `semantic` is `409`.

Only sources that declare `embedFields` (text-heavy feeds such as alerts, warnings and notices) are
embedded. Each record is embedded once per text change: unchanged text is skipped without calling
the model, and new versions are batched 64 texts per call within the provider's budget. When the
budget runs out the worker pauses until the next minute and resumes where it stopped. Retention
waits for the worker (`embedding.cursor`), so a long pause holds old versions back until the clamp
is released at twice the window.

### Changing the model

Changing the embedding model (or `POST /v1/search/reindex`, which also rebuilds with the same
model) never takes search offline. The new index is built in the second vector slot while searches
keep using the old one, with queries embedded by the old model. New records are written to both.
When every document has the new vector, the new index is built concurrently and swapped in; the
old slot is dropped. `GET /v1/search/status` shows the progress:

```json
"reindex": {"state": "backfilling", "fromModel": "ollama:nomic-embed-text:768",
            "toModel": "openai:text-embedding-3-small:1536", "total": 120000, "done": 45000,
            "percent": 37.5, "ratePerMinute": 1900.0, "etaSeconds": 2368, "startedAt": "…"}
```

The backfill embeds every embeddable record again, so it costs one embedding per record (about
`total × average text tokens`; texts are capped at 2,000 characters). A restart resumes from where
it stopped. `DELETE /v1/search/reindex` cancels and discards the new slot. If the old provider is
deleted or loses the old model during the rebuild, searches answer from text (`degraded: true`)
until the swap.

### The `embed` flag

Each source has an `embed` setting (`PUT /v1/sources/{id}` with `{"embed": false}`), on by default
for sources that declare `embedFields`. Turning it off stops embedding the source and deletes its
documents. Turning it back on embeds new versions; run `POST /v1/search/reindex` to embed the
records it already has.

## Briefings

A briefing is a scheduled summary ("morning brief", "weekly maritime summary") delivered to your
channels. It needs no LLM: the default `digest` mode is deterministic markdown. With a chat provider
assigned, `llm` mode writes a cited narrative from the same data instead.

```json
POST /v1/briefings
{"name": "Morning brief", "schedule": "0 7 * * *", "timezone": "America/Chicago",
 "scope": {"kinds": ["earthquake"], "bbox": "-125,24,-66,50", "ruleIds": [], "includeHealth": true},
 "lookback": "24h", "mode": "digest", "channelIds": ["…"], "maxItemsPerKind": 5}
```

- **Schedule.** A standard 5-field cron (`minute hour day-of-month month day-of-week`) in the
  briefing's IANA `timezone` (default `UTC`), so `0 7 * * *` stays at 07:00 local time across DST
  changes. Briefings may run at most every 15 minutes. The scheduler checks every 30 s; runs missed
  while Foresignal was down run once when it is back, never once per missed slot.
- **Period.** `lookback` (1h to 7d) back from the run; without one, since the previous run (capped at
  7 days), or 24 hours for the first run.
- **Scope.** `sources`, `kinds` and `bbox` limit the records; `ruleIds` limits the alert section;
  `includeHealth` adds the source health section. Empty lists mean everything. `places` (prompt 056) adds
  "Weather at places" for those places and `weatherProducts` picks its products; see
  [Alert weather context](#alert-weather-context).
- **Channels.** The run is published as a `briefing` notification to `channelIds`, or to every
  channel subscribed to `briefing` when the list is empty. A channel that does not exist, is disabled
  or is not subscribed to `briefing` is reported in `warnings` when you save the briefing; it is not
  an error. The message links to the HTML rendition ("Read more").

### What a digest contains

- **Alerts** in the period, critical first, with counts per severity and state.
- **Activity**, per kind: versions that are `new` (first seen or reappeared), `changed` and
  `removed`, entities `active` in the period (including ones only re-observed, never re-versioned),
  and the delta against the previous period of the same length. Routine versions (samples,
  keyframes, heartbeats) are listed separately and never counted as change.
- **Notable** records per kind, ranked by the source's `rankField` (for example magnitude), else by
  time, each linked to `publicBaseUrl/v1/records/{source}/{externalId}`. Set `publicBaseUrl` in
  `PUT /v1/settings/server` so links work outside the box.
- **Source health**: sources currently degraded or failing (Foresignal does not keep a history of
  health changes yet, so this is the state at run time).
- A **"Counts incomplete (retention)"** note when the period reaches back past what retention kept.

Private sources are counted but their records are not listed, unless the briefing was created or
last saved with the admin token.

### LLM mode

`mode: "llm"` with `instructions` (focus, length, tone; up to 4,000 characters). The model gets the
digest statistics, the notable records and, when semantic search is available, a search on your
instructions within the scope, and must cite `[source:externalId@seq]`. Citations are verified as in
Ask; problems go to the run's `warnings`. If the model fails, refuses, or the LLM budget is spent,
the run falls back to the digest with status `fallback` and the reason in `error`. Records of private
sources never go to a non-local provider. Usage is recorded per run.

### Runs

- `POST /v1/briefings/{id}/run` runs now (agent token) and returns the run, waiting up to 120 s; a
  second run while one is going is `409 BRIEFING_RUNNING`. `POST /v1/briefings/preview` renders a
  definition without storing, delivering or scheduling anything.
- `GET /v1/briefings/{id}/runs` lists runs, newest first; `GET /v1/briefings/{id}/runs/latest` and
  `/runs/{runId}` return one as JSON, or as a self-contained HTML page with `Accept: text/html` or
  `?format=html`. The page is sanitized (no scripts, styles attributes or frames survive, even from
  record titles), follows the browser's light or dark theme, and is served with a strict
  Content-Security-Policy.
- Runs are kept for the `briefings` retention (default 180 days), even after the briefing is
  deleted, and are searchable with `docType=briefing` when semantic search is on. MCP agents use
  `list_briefings`, `get_briefing_run`, `create_briefing` and `run_briefing`.

## Alert enrichment

With a chat provider assigned, a rule can ask for a short summary of what Foresignal knows around each
of its alerts. Set `enrich: true` on the rule, optionally with `enrichRadiusKm` (1 to 1,000, default
100) and `enrichWindow` (10m to 7d, default 24h):

```json
PUT /v1/triggers/{ruleId}
{"name": "Big quakes", "sources": ["usgs-earthquakes"], "conditions": [{"path": "mag", "op": ">=", "value": 5}],
 "enrich": true, "enrichRadiusKm": 150, "enrichWindow": "12h"}
```

`enrich: true` is accepted without a provider: it is stored and enrichment is skipped until a chat
provider is assigned. `PUT /v1/llm/settings` with `"enrichmentEnabled": false` switches enrichment off
everywhere. `testTrigger` reports `"enrich": true` without calling the model.

**What the model reads.** Around the alert's observation time `T`, within the radius and `T ±
enrichWindow`: the record and up to 10 of its earlier versions, records of any kind nearby (also ones
that only moved, without a new version), up to 10 nearby alerts and, when semantic search is on, the
5 best search matches for the alert message. A record without a location gets its history and the
search matches only. The context is capped at 24 KB; the least relevant items are left out first.

**What it writes.** At most about 120 words for the operator: what happened, what is nearby or
related, and why it might matter, citing records as `[source:externalId@seq]` and alerts as
`[alert:id]`. Citations are checked against what the model was given and the ones it invented are
removed. "No notable related activity" means it found nothing relevant. The summary appears as
`summary`, `enrichedAt` and `summaryCitations` on `GET /v1/alerts/{id}` and in lists, in MCP
`get_alert`, and as an `alert.enriched` event on `/v1/alerts/stream` and the `alerts` topic of
`/v1/events/stream` (the full alert; treat it as an update of the alert with the same id).

**Waiting or editing, per channel.** Enrichment never holds a message back longer than the channel
allows:

| Channel | What happens |
|---|---|
| Any type with `waitForEnrichmentSeconds` > 0 (up to 60) | The alert waits up to that long. When the summary arrives the message is sent at once, with it; if it does not arrive in time, the message goes out without it. |
| `discord`, `telegram`, `matrix` with `editInPlace` on (the default) | A message already sent without the summary is edited to add it, if it is less than an hour old. |
| Every other type, wait 0 | The message goes out at once without the summary, and nothing more is sent: no follow-up message. |

`{{summary}}` is in the default body; a custom `bodyTemplate` must include it to show the summary.
Slack and Mattermost incoming webhooks cannot edit messages, so give those channels a wait (10 to 20
seconds is usually enough) if you want the summary in them.

**Cost per alert.** One call per enriched alert, never retried: up to about 6,000 input tokens (the
24 KB context plus a fixed system prompt that providers can cache) and at most 600 output tokens, at
effort `low`. A rule that fires often multiplies that; the LLM budget refuses calls once the day's
tokens are spent, and those alerts go out without a summary. Alerts older than 10 minutes (catch-up
after downtime) are not enriched. Usage shows under purpose `enrichment` in `GET /v1/llm/usage`, and
`foresignal.enrichment.succeeded`, `.failed{reason}`, `.skipped{reason}`, `.dropped` and `.duration`
are exported as metrics.

**What leaves the box.** The context goes to the chat provider. Records of private sources are left
out for a remote provider and sent only to a local one, and an alert on a private source is not
enriched at all by a remote provider. Record text is treated as data: instructions hidden in a record
are not followed, and the model is offered no tools, so it cannot change anything.

## Web Push and UnifiedPush

Foresignal pushes alerts, briefings and system events to browsers (a PWA, the operator console) and
to native Android and Linux apps through [UnifiedPush](https://unifiedpush.org), which speaks the same
Web Push protocol (RFC 8030 with RFC 8291 encryption). Foresignal holds its own VAPID key, generated on
first start, so no push account is needed.

**The Push channel.** One built-in channel named "Push" (type `webpush`) is created at startup and
fans out to every verified subscription. Its events (alert, briefing, system), rule filter, minimum
severity, quiet hours and the global pause apply as for any channel (`PUT /v1/channels/{id}`); it
cannot be created or tested like the others. Its `config.quietHoursMode` decides what quiet hours do:
`defer` (default, as for every channel), `downgrade` (sent at once with `Urgency: very-low` and
`silent: true`) or `drop`. Messages at or above the quiet hours' `bypassSeverity` (critical by
default) always go out. Each subscription adds its own filters (`events`, `ruleIds`, `minSeverity`,
`tags`).

**Subscribing.** A client needs a token (read scope is enough; a token is required even with open
reads). It fetches `GET /v1/push/vapid-public-key`, subscribes, and POSTs the subscription to
`/v1/push/subscriptions`. The new subscription is `pending`: Foresignal immediately sends it a
verification push `{"v":1,"event":"verify","subscriptionId":"…","code":"…"}`, and the client echoes the
code to `POST /v1/push/subscriptions/{id}/verify` within 10 minutes (five wrong codes delete it). Only
verified subscriptions receive notifications. Limits: 5 pending per token, 200 in total. Revoking the
token deletes its subscriptions. `GET /v1/push/subscriptions` (admin) lists them with their host,
status and failure count, never the endpoint.

A service worker for a browser client:

```js
// page: register the worker and subscribe
const reg = await navigator.serviceWorker.register('/sw.js');
const { publicKey } = await (await fetch(`${FS}/v1/push/vapid-public-key`)).json();
const sub = await reg.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: publicKey });
await fetch(`${FS}/v1/push/subscriptions`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
  body: JSON.stringify({ name: 'Laptop', subscription: sub.toJSON(), filters: { minSeverity: 'warning' } }),
});

// sw.js
self.addEventListener('push', (event) => {
  const msg = event.data.json();
  if (msg.event === 'verify') {
    event.waitUntil(fetch(`${FS}/v1/push/subscriptions/${msg.subscriptionId}/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${TOKEN}` },
      body: JSON.stringify({ code: msg.code }),
    }));
    return;
  }
  event.waitUntil(self.registration.showNotification(msg.title, {
    body: msg.body, tag: msg.tag, lang: msg.lang, silent: msg.silent, data: msg,
  }));
});
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const links = event.notification.data.links || {};
  event.waitUntil(clients.openWindow(links.web || links.api));
});
```

The payload is `{v, event, id, alertId, ruleId, severity, title, shortTitle, body, url, links {api,
app, web}, tags, lang, tag, silent, ts}` (absent members omitted), at most 3,993 bytes; long bodies
are cut first. `tag` is `foresignal-alert-<alertId>`, so a resolve replaces the fired notification.
`links.app` and `links.web` come from the server setting `clientLinks`. Safari 18.4 and later can use
`payloadFormat: "declarative"` (WebKit Declarative Web Push, no service worker needed); the
notification then opens `links.web`, or `links.api` when there is none.

A Flutter app with the [`unifiedpush`](https://pub.dev/packages/unifiedpush) package passes the
VAPID key to `registerApp` and gets an already decrypted message:

```dart
await UnifiedPush.initialize(
  onNewEndpoint: (PushEndpoint endpoint, String instance) async {
    final keys = endpoint.pubKeySet!;
    await api.post('/v1/push/subscriptions', {
      'name': 'Pixel', 'transportLabel': 'unifiedpush',
      'subscription': {'endpoint': endpoint.url, 'keys': {'p256dh': keys.pubKey, 'auth': keys.auth}},
    });
  },
  onMessage: (PushMessage message, String instance) async {
    final msg = jsonDecode(utf8.decode(message.content));
    if (msg['event'] == 'verify') {
      await api.post('/v1/push/subscriptions/${msg['subscriptionId']}/verify', {'code': msg['code']});
    } else {
      showLocalNotification(msg['title'], msg['body'], tag: msg['tag']);
    }
  },
);
final vapid = (await api.get('/v1/push/vapid-public-key'))['publicKey'];
if (await UnifiedPush.tryUseCurrentOrDefaultDistributor()) {
  await UnifiedPush.registerApp('default', vapid: vapid);
}
```

**A distributor on the LAN.** Endpoints must be `https` and every address the host resolves to must
be globally routable; loopback, private, CGNAT, link-local, unique-local, NAT64, 6to4 and metadata
addresses are refused. A self-hosted UnifiedPush distributor (for example ntfy) on the LAN is allowed
by listing it in the server setting `push.allowedPrivateHosts`, as an exact host name or a CIDR (IPv4
/8 or longer, IPv6 /32 or longer; no wildcards). It still needs https. The address is checked again
before every send and the connection goes to the checked address; three failed rechecks in a row
disable the subscription. `push.contact` (default `mailto:admin@localhost`) is the VAPID contact the
push services see, and `push.defaultLang` the payload language when a subscription sets none.

**Reach.** Web Push reaches Chrome, Edge and Firefox on desktop and Android while the browser runs,
and Safari when a site is added to the Home Screen. It does not reach the native iOS app, and it does
not wake a closed desktop browser on macOS or Windows. Use native push (APNs and FCM, below) or an
ntfy channel for those.

**Delivery.** Each subscription gets its own send and retries (10 s, 1 min, 5 min, 30 min, 2 h), so a
retry never repeats a push that already arrived. A push service answering 404 or 410 removes the
subscription; 429 and 503 are retried after `Retry-After`. At most 20 pushes per second go to one push
service host. The Push channel's delivery is delivered when at least one subscription received it.

## Native push (APNs and FCM)

The Flutter apps receive alerts on iOS, iPadOS and macOS through Apple Push Notification service
(APNs) and on Android through Firebase Cloud Messaging (FCM). Both are further targets of the
built-in Push channel, so its events, quiet-hours mode and retries apply unchanged.

**The self-hosting constraint.** An APNs key belongs to the Apple developer team that signs the app,
and a Firebase service account to the Firebase project compiled into the app. Foresignal can only
push to an app built and signed with your own team and Firebase project (or through a publisher's
relay, which Foresignal does not provide). Without that, use UnifiedPush (above) or an ntfy channel:
neither needs an account.

**Privacy.** A native push is a visible alert that carries only ids, the severity and a minimal
text; full titles and bodies never leave the server through Apple or Google. Per device,
`contentLevel` chooses the text:

| `contentLevel` | Title | Body |
|---|---|---|
| `minimal` (default) | Foresignal | "Critical alert", "Briefing ready" or "System notice" |
| `short` | Foresignal · critical | the alert's short title (at most 40 characters) |

`short` lets Apple or Google see the rule name; choose it only where that is acceptable. The app's
Notification Service Extension (iOS) or message handler (Android) replaces the text with the real
title and body, which it fetches from Foresignal itself (see `docs/Client-Guide.md`).

**Reachability for the rich text.** The extension fetches from your Foresignal over the phone's own
network: on the LAN, over a VPN, or through Tailscale. It has about five seconds; when Foresignal is
out of reach the minimal text is shown, and opening the notification fetches the details later.

### Create an APNs key

1. In your Apple Developer account, under Certificates, Identifiers & Profiles → Keys, create a key
   with Apple Push Notifications service enabled. Apple's keys are environment-specific: choose
   Sandbox (development builds) or Production (TestFlight and App Store); a team may hold two per
   environment. Older keys that serve both environments still work. Topic-specific keys are tied to
   one environment and a list of bundle ids.
2. Download the `.p8` file (you can download it once), and note the Key ID and your Team ID.
3. Configure the provider (the `.p8` text is the PEM; it is validated as an EC P-256 key, encrypted
   and never shown again):

```http
PUT /v1/push/providers/ios-apns
Authorization: Bearer <admin token>

{"type": "apns", "appIds": ["com.example.foresignal"],
 "config": {"teamId": "ABCDE12345", "keyId": "KEY1234567", "keyEnvironment": "production",
            "timeSensitive": false},
 "secrets": {"p8": "-----BEGIN PRIVATE KEY-----\n…\n-----END PRIVATE KEY-----\n"}}
```

`keyEnvironment` is `sandbox`, `production` or `both` (legacy keys). A device registers with its
own environment, and registration is refused (409 `PUSH_PROVIDER_NOT_CONFIGURED`) when no key
covers it: debug builds use `sandbox`, TestFlight and App Store builds `production`. Set
`timeSensitive` only when the app has the Time Sensitive Notifications capability; critical alerts
are then delivered as time-sensitive. Foresignal never sends Apple's `critical` level, which needs a
special entitlement.

### Create a Firebase service account

1. In the Firebase console of the project the Android app uses, open Project settings → Service
   accounts and generate a new private key. It downloads a JSON file.
2. Configure the provider with the whole JSON (only its `private_key` is stored, encrypted;
   `project_id` and `client_email` fill the config):

```http
PUT /v1/push/providers/android-fcm
Authorization: Bearer <admin token>

{"type": "fcm", "appIds": ["com.example.foresignal"], "config": {},
 "secrets": {"serviceAccountJson": "<the downloaded JSON, as a string>"}}
```

Foresignal uses the FCM HTTP v1 API with an OAuth 2.0 token it mints from the service account
(the legacy FCM APIs no longer exist). An iOS build that registers through FCM instead of APNs is
also served: the message then carries an APNs block.

### Test and monitor

`POST /v1/push/providers/{providerId}/test` mints an APNs provider token or an FCM access token and
reports whether it worked; with `{"deviceId": "…"}` it also sends that device a test notice. A
device's owner can send itself a test with `POST /v1/push/devices/{deviceId}/test` (five per hour).

When Apple or Google rejects the key (an invalid or revoked key, a key of the wrong environment, a
Firebase permission problem), the provider's `lastError` shows the reason code, one
`system.push_provider_failed` notification goes to your other channels (never to Push), and the
pushes are retried after 15 minutes. `lastError` clears on the next success. Responses, logs and
`lastError` never contain a key, a device token or alert text.

### Devices

The app registers each installation with `POST /v1/push/devices` using its own read token (a
paired token from `foresignal://pair`). `GET /v1/push/devices` lists every device for an admin and
only its own for a read token; device tokens are never shown, only their last six characters.
A device whose token Apple or Google reports as gone (APNs 410, `BadDeviceToken`; FCM
`UNREGISTERED`) is removed automatically, and revoking or expiring the API token that registered a
device removes it too. Deleting or disabling a provider stops pushes to its apps' devices until a
provider serves them again; the devices stay registered.

## Acting from notifications

A firing alert can be acknowledged, snoozed (1 h) or its watch muted (4 h) straight from the
notification. Each button or link is a **single-use capability** (`/v1/actions/eac_…`), valid for 12
hours and only for that alert; no API token is ever put on the phone. Opening a link (`GET`) only
shows a confirm page with one button, so link previews, mail scanners and a stray tap never act; the
action happens on `POST`. Acknowledging or resolving the alert any other way revokes the remaining
buttons, and every execution is in the audit log (actor kind `action`, the token shown as `eac_…`).
Acting on an alert that is already acknowledged or resolved answers "already acknowledged".

Per channel, under `config.actions` (`{"enabled": true, "list": ["ack", "snooze", "mute"], "links": true}`):

| Channel | How it acts | Default |
|---|---|---|
| ntfy | Two one-tap buttons (`http` POST, `clear=true`) and **Open** | on |
| Push (Web Push) | Up to two notification buttons; browsers without buttons (Safari) open the confirm page | on |
| Push (native apps) | Category `FORESIGNAL_ALERT_ACTIONABLE`; the app fetches its own buttons from `GET /v1/notifications/{id}`, so no capability goes through Apple or Google | on |
| Telegram | Inline buttons; the message is then edited ("Acknowledged by Ana") and the buttons removed | on |
| Matrix | Confirm-page links; with `"reactions": true`, a ✅ reaction from someone in `allowedSenders` acknowledges | on (links) |
| Pushover | Critical alerts are emergency priority; acknowledging in the Pushover app acknowledges in Foresignal (`pushover:<device>`); the URL opens the acknowledge confirm page | on |
| Webhook | An `actions` array (`action`, `label`, `url`, `expiresAt`) in the default JSON body | opt-in |
| Email, Slack, Discord, Mattermost, Teams, Google Chat, Gotify, … | Confirm-page links at the end of the body (`"links": true`) | opt-in |

The links use the `server` setting's `publicBaseUrl`, which must be reachable from the phone. Set
`"enabled": false` to send a channel no buttons, or narrow `list` (for example `["ack"]`).

**Telegram needs a dedicated bot.** Foresignal long-polls `getUpdates` for button presses, which
Telegram refuses while a webhook is set for the bot, and two programs polling one bot steal each
other's updates. Create a bot for Foresignal alone with @BotFather. `allowedUserIds` (Telegram user
ids) limits who may press; left empty, any member of the channel's chat may.

**Pushover receipts.** Foresignal polls each emergency receipt every 60 seconds (Pushover asks for no
more than every 5 seconds) for up to three hours, until it is acknowledged or expires.

**Why links only confirm.** A capability URL is a credential. Preview bots, antivirus scanners and
browsers prefetch links; if `GET` acted, they would acknowledge your alerts. The confirm page is
plain HTML without scripts or external assets, is never cached and is not indexed; posts from
another site are refused, and the endpoint takes at most 30 requests per minute per client address.

### Presence deferral

When you are at your desk, your phone need not buzz for what you are already looking at. Give the
phone-type channels (Push, ntfy, Pushover, Telegram, Gotify) `"deferWhileActiveSeconds": 120`. Your
desktop client reports `POST /v1/clients/presence {"kind":"desktop","state":"active","ttlSeconds":120}`
every minute or so. While a desktop is active, a non-critical alert waits 120 seconds on those
channels; if you acknowledge, snooze or resolve it, or read it on the desktop, in that time, the phone
copy is marked `suppressed` (visible in `/v1/channels/{id}/deliveries`) instead of sent. Critical
alerts and system events are never held, and presence that stops arriving expires, so everything goes
to the phone again. `GET /v1/clients` (admin) lists the current presence of every device.

### Escalation

An escalation policy reaches further people when nobody acknowledges:

```json
POST /v1/escalation-policies
{"name": "Wake me up",
 "steps": [{"afterMinutes": 10, "channelIds": ["<pager channel id>"], "includePush": true},
           {"afterMinutes": 30, "channelIds": ["<second person's channel id>"]}],
 "repeat": {"everyMinutes": 30, "maxTimes": 3},
 "stopOn": {"resolve": true, "snooze": true}}
```

Put its id in a watch's `escalationPolicyId`. Ten minutes after an alert of that watch fires, unless
it was acknowledged (always), resolved or snoozed (per `stopOn`), the pager channel and Push get
"[Escalation 1] …" with fresh buttons; at 30 minutes the second channel gets "[Escalation 2] …", and
then every 30 minutes up to three more times. Quiet hours hold escalations back except for critical
alerts. Acknowledging stops it at once, cancels escalation messages still waiting and cancels Pushover
emergency retries; muting the watch (`POST /v1/triggers/{id}/mute {"for":"4h"}`) stops escalation of
its open alerts too. `GET /v1/alerts/{id}/escalation` shows where an alert stands. A policy used by a
watch cannot be deleted (`409 POLICY_IN_USE`). Muted watches still record alerts and keep them in the
inbox, but notify nobody until the mute ends (1 hour to 7 days).

## Reference data and region tags

Foresignal gives every record a human place context without any online geocoder (prompt 040):
country, first-level region, US county, sea area, and the nearest town ("12 km NNE of Austin, TX").
Tags are stored beside the record (`record_region`), never in its payload, so they never create
versions or change hashes.

**Datasets.** On the first start, when `foresignal.reference.auto-load` is on (the default), the
default datasets load in the background about 30 s after startup. Loading never blocks startup or
readiness; the `reference` readiness detail and `GET /v1/reference` show progress and failures, and a
failed load raises a `system.reference_load_failed` notification.

| Dataset | Source | Licence | Download | Table |
|---|---|---|---|---|
| `ne-admin0` | Natural Earth 10m admin-0 countries v5.1.2 | Public domain | 13 MB | `ref_country` |
| `ne-admin1` | Natural Earth 10m admin-1 states and provinces | Public domain | 41 MB | `ref_admin1` |
| `ne-admin2-us` | Natural Earth 10m admin-2 (US counties) | Public domain | 8 MB | `ref_admin2` |
| `ne-marine` | Natural Earth 10m marine polygons (sea areas) | Public domain | 1.7 MB | `ref_sea` |
| `geonames-cities15000` | GeoNames, places over 15,000 people (34,160 rows on 2026-10-12) | CC BY 4.0 | 3.4 MB | `gazetteer_place` |
| `geonames-admin1` | GeoNames first-level region names | CC BY 4.0 | 0.2 MB | `gazetteer_admin1` |
| `geonames-countryinfo` | GeoNames country names | CC BY 4.0 | 32 KB | `gazetteer_country` |
| `geonames-cities1000`, `geonames-cities500` (optional) | GeoNames, places over 1,000 / 500 people (171,192 / 236,046 rows) | CC BY 4.0 | 11 / 14 MB | `gazetteer_place` (replaces) |

Reload or add datasets with `POST /v1/reference/load {"datasets": ["ne-admin1", "geonames-cities1000"]}`
(admin, 202). Only names from the bundled manifest are accepted, never URLs. Downloads go through the
same budgeted client as adapters (id `foresignal-reference`, 10 requests a minute, 100 a day,
Foresignal's User-Agent) into `${FORESIGNAL_DATA_DIR}/reference/`. Each file is checked against the
SHA-256 pinned in the manifest; a mismatch is `REFERENCE_CHECKSUM_MISMATCH` and the dataset is not
loaded. GeoNames regenerates its dumps every day, so its entries are verified only when you pin a
checksum (`foresignal.reference.checksums.geonames-cities15000: <sha256>`); otherwise the observed
SHA-256 is recorded in `GET /v1/reference`. Every load goes into a staging table and is swapped in
inside one transaction, so readers never see a half-loaded table.

**Disk use.** The default downloads take 65 MB in `${FORESIGNAL_DATA_DIR}/reference/` (measured
2026-10-12; you may delete them after a load). In PostgreSQL the boundaries are stored at full Natural
Earth resolution plus a simplified copy, with GiST indexes; check the real size with
`SELECT pg_size_pretty(sum(pg_total_relation_size(c.oid))) FROM pg_class c WHERE c.relname LIKE 'ref\_%'
OR c.relname LIKE 'gazetteer\_%'`.

**Offline installs.** Copy the files named in `src/main/resources/reference/manifest.json` into a
directory and set `foresignal.reference.offline-dir` (or `FORESIGNAL_REFERENCE_OFFLINEDIR`) to it;
the loader reads them instead of downloading and still verifies the checksums. Without any reference
data, tags still carry the country, from a simplified boundary set bundled with the application, and
`GET /v1/reference/countries` serves the bundled country table.

**Boundaries.** Natural Earth draws de facto boundaries (who controls the ground), which are not an
endorsement of any claim. `foresignal.reference.ne-admin0-variant: ind` (or `arg`, `chn`, `deu`,
`fra`, `gbr`, `rus`, `tur`, `twn`, `ukr`, `usa` … every Natural Earth point-of-view file is in the
manifest with its hash) loads a point-of-view file instead.

**Licences.** Natural Earth is public domain. GeoNames is CC BY 4.0: the credit is in
`THIRD_PARTY_NOTICES.md`, in `GET /v1/reference`, and in every response that carries a `near`
(`RegionTags.attribution`, GeoJSON `properties.nearAttribution`). Keep it when you republish those
fields.

**How records are tagged.** Per source, `regionTagging` (`PUT /v1/sources/{id}`) is `eager` (default
for STANDARD sources: tagged as versions persist), `lazy` (default for TRACK sources: tagged when a
template renders, an alert fires, or a record is read with `include=region`) or `off`. A moving entity
is tagged again once it is more than 2 km (10 km for TRACK sources) from where it was last tagged. A
point outside every country but within 5 km of one gets that country; at sea the country is empty and
`sea` is set. After a reference load every current record of an eager source is tagged again.

**Using the tags.**
- `include=region` on `GET /v1/records` and `GET /v1/records/{source}/{id}` adds `region`; GeoJSON
  features gain `properties.near`.
- Filters `country=UA,PL`, `admin1=US-TX` and `county=48453` on records (JSON and GeoJSON), `/v1/search`,
  `/v1/alerts` (the alert's region at fire time), the stats reads (answered from raw rows) and briefing
  scopes. Unknown codes are `400 UNKNOWN_REGION`. Record filters see tagged records only, so a mover may
  lag by up to one retag threshold, and lazy sources match only once their records were tagged.
- `GET /v1/reference/reverse?lon=-97.7&lat=30.37` and the MCP tool `reverse_geocode` tag any point.
- Template examples:

```text
{{name}}: M{{payload.mag}} {{near.text}}            → Quake: M4.6 12 km NNE of Austin, TX
{{country.name}} / {{admin1.name}} / {{county}}     → United States of America / Texas / Travis
{{near.km}} km ({{near.mi}} mi) {{near.bearing}} of {{near.name}}
{{near.text|imperial}}                              → 8 mi NNE of Austin, TX
```

## Stats, glance and metrics

### Activity rollups

Foresignal keeps an `activity_rollup` table so clients can draw activity charts and maps without
paging raw history, and so the counts outlive retention. A job runs every 5 minutes and rolls up each
closed hour (and, once its 24 hours are done, each UTC day) from `record_version`. It works from a
durable cursor (`app_setting['stats.rollup.cursor']`) written in the same transaction as the rows, so a
crash never double-counts or skips an hour.

- **Buckets are on the transaction axis:** hour `H` holds the versions *ingested* in `[H, H + 1 h)`.
  Stats on the observed, effective or event time are served from raw history, inside retention.
- **Measures:** `versions` (HEARTBEAT and KEYFRAME excluded), `entities` (distinct entities),
  `firsts` (FIRST and REAPPEAR), `tombstones`, `edges` (TRIGGER_EDGE). Rows hold no entity ids.
- **Cells:** each version's centroid is mapped to an H3 cell at `foresignal.stats.cell-res` (default 4,
  about 1,770 km² per cell). Versions without a geometry go to cell 0.
- **First start:** the job backfills from the oldest retained version, at most
  `foresignal.stats.backfill` (default `30d`) back, 6 hours per run.
- **Retention:** the rollup cursor joins the consumer clamp (`min(trigger, embedding, rollup)`), so raw
  versions are never deleted before they are rolled up. Rollup rows have their own windows:
  `RetentionSettings.stats = {"hourly": "35d", "daily": "1825d"}`.
- **Which sources:** private sources are skipped unless `PUT /v1/sources/{id}` sets
  `statsIncludePrivate: true`; set `statsEnabled: false` to leave a noisy source out.
- **Rebuild:** `POST /v1/stats/rollup/rebuild {"from": "…", "to": "…"}` (admin) recomputes the closed
  buckets in the range, per source, only where that source's raw versions are all still retained.

**Storage estimate.** Rows ≈ hours × sources × kinds × occupied cells, plus one totals row per
source and kind per hour. Point sources in a few hundred cells cost little. **Global aircraft at
resolution 4 occupy about 20,000 cells an hour, about 0.5 million rows a day** (about 17 million rows in
the 35-day hourly window). If that is too much, lower `foresignal.stats.cell-res` to 3 (about 7 times
fewer cells) or set `statsEnabled: false` on the aircraft sources.

### Stats endpoints

| Endpoint | What |
|---|---|
| `GET /v1/stats/timeseries` | `bucket=1h\|1d\|auto` (≤ 2,000 buckets), `measure`, `groupBy=none\|source\|kind`, `timeField`. `backend` says `rollup`, `raw` or `mixed` |
| `GET /v1/stats/grid` | H3 cells as GeoJSON (`res` ≤ `cellRes`), or `mode=raw&sizeKm=` hexagons in EPSG:3857 (≤ 7 days); ≤ 10,000 cells |
| `GET /v1/stats/top` | Top sources, kinds, cells or entities (`by=entity` reads raw history inside retention) |
| `count=true` | On `/v1/records`, `/v1/history` and `/v1/search`: `{count, exact, watermarkSeq}`, exact up to 1,000,000 |

Every stats query has a 10-second statement timeout (`503 QUERY_TIMEOUT`). With a bbox, a restricted
area or a place, rollup rows are chosen by cell centre, so edges are accurate to a cell, and
`entities` counts an entity once per cell it visited.

### Glance

`GET /v1/glance` is a status document of at most 2 KB for widgets, watches and e-ink displays:
`status`, open alerts per severity with the latest three, source tallies, the latest briefing,
versions in the last hour, `lastSeq` and deep links. `?format=text` (or `Accept: text/plain`) gives at
most 8 lines of 40 ASCII characters. `?place=<id>` narrows the alerts to a place (token required).
Answers are cached for 15 seconds and carry a strong ETag, so polling with `If-None-Match` is cheap.

Without a token (under `openReads`) only `status`, `serverTime`, the latest alerts and `lastSeq` are
shown. To show the counts too, set the app setting `glance.exposeCountsAnonymously` to `true`.

- **TRMNL:** the TRMNL cloud accepts at most 12 webhook updates an hour, so whatever forwards the
  glance (a small script or an automation fetching `/v1/glance` with a read token) should coalesce to
  one update every 10 minutes or more. Bring-your-own-server (BYOS) setups need a renderer such as
  Terminus that turns the JSON into the panel image.
- **MagicMirror:** any JSON or text module can poll `/v1/glance?format=text` every minute.
- **Home Assistant:** a REST sensor:

  ```yaml
  sensor:
    - platform: rest
      name: Foresignal
      resource: http://foresignal.local:8080/v1/glance
      headers:
        Authorization: !secret foresignal_read_token
      value_template: "{{ value_json.status }}"
      json_attributes: [alerts, sources, activity, lastSeq]
      scan_interval: 60
  ```

- **Garmin and Wear OS:** the watch platforms need native code (a Connect IQ widget, a Wear OS tile)
  that fetches the JSON glance through the phone; the 2 KB limit keeps it within their request
  budgets.

### Metric projections

`/v1/metrics` exposes payload fields of current records as Prometheus gauges, from a registry of its
own: it never appears on `/actuator/prometheus`, and it needs the `read` scope (Prometheus sends the
token as a bearer). Define projections with `PUT /v1/settings/metrics` (admin):

```json
{"projections": [{"name": "usgs_magnitude", "source": "usgs-earthquakes", "kind": "earthquake",
  "path": "mag", "unit": "magnitude", "help": "Magnitude of current USGS earthquakes", "maxSeries": 500}]}
```

Each projection becomes `foresignal_data_<name>{source, external_id}` (or the projection's own
`labels`, at most 4) plus `foresignal_data_<name>_observed_timestamp_seconds`. Values follow live state
(SAMPLED sources included); a series disappears when its record is tombstoned, expires or is purged.
Non-numeric values are skipped and counted in `foresignal.projection.coercion_failures{projection}`.
Each projection keeps at most `maxSeries` series and the whole endpoint at most 10,000; reaching that
raises a `system` notification once. A private source needs `allowPrivate: true`, and its series are
shown only to callers that pass the private gate (an agent or admin token).

A Prometheus scrape job:

```yaml
scrape_configs:
  - job_name: foresignal-data
    metrics_path: /v1/metrics
    scrape_interval: 60s
    authorization:
      type: Bearer
      credentials_file: /etc/prometheus/foresignal.token
    static_configs:
      - targets: ["foresignal.local:8080"]
```

**Grafana without Prometheus:** the Infinity data source can chart the stats API directly. Add a
query of type JSON, URL `http://foresignal.local:8080/v1/stats/timeseries?bucket=1h&measure=versions&groupBy=source&from=${__from:date:iso}&to=${__to:date:iso}`,
an `Authorization: Bearer …` header, root selector `series`, and columns `key` (string) and
`points` expanded to `t` (time) and `value` (number).

## Personal location and privacy

Prompt 039 makes it safe to feed family phones and personal trackers into Foresignal. A **private**
source is invisible on every read and egress path unless the caller is explicitly allowed; the four
common tracker apps push without glue; each phone gets a key that can write only its own entity; and
any single entity can be exported or erased.

### Create a personal-location source

```http
POST /v1/custom-sources
{"id": "custom-family", "name": "Family phones", "profile": "personal-location"}
```

The `personal-location` profile fills in what the request leaves out:

- `private: true`. It cannot be unset: `"private": false` with this profile is a 400.
- STANDARD history tier, kinds `tracker` (fixes) and `tracker-transition` (OwnTracks region events).
- `observedAtMode: UPSTREAM`, `ttlAnchor: OBSERVED`, `recordTtl: 24h`, default retention 7 days.
- Ingest policy SAMPLED: `minInterval` 30 s, keyframe 10 min, `minDeviation` 50 m,
  `significantFields: [motion, charging]`.
- No embedding (`embedFields` are refused) and no enrichment (the private gate below).
- No precision cap: the stored coordinates keep 7 decimals. It is your own data; set
  `privatePrecisionDecimals` yourself if you want the 008 cap.
- Field specs `speedMs` (m/s), `courseDeg` (deg), `altitudeM` (m), `battery` (%), `charging`, `motion`.

Explicit members win over the profile, except `private`.

### One key per phone

```http
POST /v1/custom-sources/custom-family/devices
{"externalId": "alex-phone", "name": "Alex's phone"}
```

The answer carries the device key (`eid_…`) **once**, plus ready-made settings for each app under
`setup`, all built from the `server` setting's `publicBaseUrl` (set it to the address the phones
reach). A device key:

- authenticates only for its source, and forces every record it pushes to its bound `externalId`. A
  payload that names another device (`id`, `deviceid`, `device_id`, `dev`) is refused with
  `403 DEVICE_ENTITY_MISMATCH` and nothing is written;
- is accepted as `X-Foresignal-Ingest-Key`, `Authorization: Bearer`, the Basic-auth password (any
  username), or in the path: `/v1/ingest/{sourceId}/k/{key}/{format}`;
- stops working the moment it is revoked (`DELETE …/devices/{deviceId}`) or rotated
  (`POST …/devices/{deviceId}/key/rotate`, which returns the new key and setup once). The device's
  records stay until retention removes them or you purge them.

`GET /v1/custom-sources/{id}/devices` lists the devices with their key prefix and `lastSeenAt`; key
values are never shown again.

### App setup

| App | Setting | Value |
|---|---|---|
| OwnTracks (HTTP mode) | Import `setup.owntracks` as a `.otrc` file, or set Mode HTTP, URL `<base>/v1/ingest/<source>/owntracks`, Authentication on, any username, password = the device key | Foresignal replies `200 []` |
| Overland (iOS) | Receiver endpoint `setup.overland.endpoint`, access token `setup.overland.token` (sent as `Authorization: Bearer`) | Foresignal replies `{"result":"ok"}` even when some points are rejected (rejections are counted, not retried) |
| Traccar Client, OsmAnd | Server URL `setup.traccar.serverUrl` (`<base>/v1/ingest/<source>/k/<key>/osmand`); query, form or JSON bodies are all read | `200` with an empty body |
| GPSLogger (Android) | Log to custom URL: `setup.gpslogger.urlTemplate`, method GET (POST with a form or JSON body works too; Basic or Bearer auth instead of the path key is accepted) | `200` |

The GPSLogger template is
`lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER`; parameter names
are read case-insensitively. Check the placeholders against the app's own Parameters screen (in the
custom URL settings): its website names only `%LAT`, `%LON`, `%TIME`, `%SAT`, `%ACC` and `%ALL`, and
the full list lives in the app. Foresignal reads `spd` as m/s.

OwnTracks region events (`_type=transition`) become a separate `tracker-transition` record whose
externalId is the phone's plus `:transition`, so an event never replaces the position.

Every fix maps to kind `tracker` with `speedMs`, `courseDeg`, `altitudeM`, `battery`, `charging`,
`motion` and `quality.accuracyMeters`. Units are normalized: OwnTracks `vel` is km/h, OsmAnd query
`speed` is knots, everything else is m/s.

- **Accuracy filter:** a fix whose accuracy is worse than `maxAccuracyMeters` (a format option under
  `formats.<format>`, default 200 m) is rejected with reason `LOW_ACCURACY`. Fixes with `valid=false`
  (`INVALID_FIX`) and exact (0, 0) (`NULL_ISLAND`) are rejected too. Overland's `wifi` name is dropped
  unless `formats.overland.keepWifi` is true.
- **OwnTracks encryption** is not supported: encrypted payloads are acknowledged with `[]`, dropped
  and counted, and the source's health shows "OwnTracks payload encryption is not supported; disable
  it".
- **Path keys and GET.** A key in the path, a key in the query or a GET push is accepted only from
  the addresses in the source's `ingestAllowCidrs`, or, when that is unset, from private addresses
  (loopback, RFC 1918, link-local, ULA); anything else is `403 INGEST_SOURCE_DENIED`. A phone on
  mobile data reaching you through a VPN (WireGuard, Tailscale) arrives from a private address; for a
  public reverse proxy, list its address range. The source ingest key (`eik_`) is accepted in the
  path only with `allowPathKey: true` (otherwise `404`); device keys always are.

### Logs and reverse proxies

Foresignal never logs a query string or a path that carries a key: `/k/{key}/` is rewritten to
`/k/redacted/` before anything (the token filter, problem documents, the fetch log, the access log)
sees it, and `key=`, `auth=`, `token=`, `PASSWORD=` and `PASSKEY=` query values are redacted. Do the
same in front of it:

- never log `$request_uri` or the query string for `/v1/ingest/*/k/*` or anything carrying `key=`,
  `auth=`, `token=`, `PASSWORD=` or `PASSKEY=`;
- log the path with the `/k/[^/]+/` segment rewritten (nginx: a `map $uri` to `/k/redacted/`), or
  turn access logging off for `/v1/ingest/`;
- never log the `Authorization` or `X-Foresignal-Ingest-Key` headers.

Caddy's `log` directive can rewrite both the path key and the query values (the `filter` format's
`regexp` filter, RE2; checked against the Caddy documentation on 2026-10-13):

```caddy
foresignal.example.org {
	reverse_proxy foresignal:8080
	log {
		format filter {
			request>uri regexp "(/k/)[^/?]+|((?i:password|passkey|key|auth|token)=)[^&]*" "${1}${2}redacted"
		}
	}
}
```

### What `private` hides, and from whom

A source is private when its descriptor says so (built-in TAK, Home Assistant and mail sources,
`CustomSourceInput.private`, an EDP manifest's `private`) or when you tighten it with
`PUT /v1/sources/{id} {"private": true}`. You can make any source private, never a descriptor-private
source public (`"private": false` is a 400).

Private sources are visible only to:

- `admin` tokens;
- `read` and `agent` tokens, feed keys and pairing codes created with `privateAccess: true` (tickets
  inherit their token's grant). An `agent` token **without** the grant no longer sees them;
- never to anonymous callers, whatever `openReads` says.

For everyone else a private source does not exist:

| Path | Behavior for a caller without access |
|---|---|
| `/v1/records` (list, get, versions, track, asOf, `count=true`), `/v1/history`, `/v1/history/frames`, `/v1/timeline`, `/v1/changes`, `/v1/kinds` counts | absent from lists and counts; a direct read is `404`, never `403` |
| `/v1/sources` | absent; `GET /v1/sources/{id}` is `404` |
| Tiles | kind layers leave private sources out; a private source layer is `404`; TileJSON and the layer index omit them. A tile or TileJSON that can contain private data is sent `Cache-Control: private` only |
| SSE (`/v1/changes/stream`, `/v1/events/stream`, alerts, inbox) | filtered per client at the hub; revoking the token or feed key sends `reset` (reason `revoked`) and ends its streams |
| Search | text search leaves them out; they are never embedded (`embed` is forced off and existing embedding rows are deleted when a source becomes private) |
| MCP | every tool filters per token with the same rule |
| Alerts | alerts on private records (snapshot included) are hidden; see notifications below |
| Stats, glance, metrics | rollups skip private sources unless `statsIncludePrivate`; glance hides their alerts; metric projections need `allowPrivate` and a caller with access |

Every private-data read by a non-admin principal (a token or feed key holding the grant) is written to
the audit log (`targetType: private_read`), as is every export and purge.

### Notifications

A notification about a private-source alert carries only the rule name, the severity and a link,
unless the channel has `config.privatePayload: true` (an admin opt-in per channel). Web Push, APNs and
FCM payloads never carry it, whatever the channel says, because they cross third-party push services;
the app fetches the details over its own authenticated connection.

### LLM egress

Private records are never placed in a prompt for a provider classified as remote. A provider counts
as local only when it is Ollama or flagged `options.local` **and** its base URL resolves only to
loopback or private addresses. Even then, private records are included only when
`PUT /v1/settings/llm {"allowPrivateLocal": true}` (default false). This covers Ask, briefings and
alert enrichment; enrichment of an alert on a private record is skipped unless both conditions hold.

### Export and erase one entity

```http
GET /v1/records/custom-family/alex-phone/export?format=gpx       # or geojson, ndjson (admin)
POST /v1/records/custom-family/alex-phone/purge {"purgeAlerts": false}   # admin
POST /v1/custom-sources/custom-family/devices/{deviceId}/purge             # purge + revoke the key
```

- The export streams the current state, every retained version (with observers), the alerts that
  reference the entity and a manifest with counts and a SHA-256 of everything before it. GPX writes a
  `trk` for kind `tracker`.
- A purge writes a tombstone (qa flag `PURGED`, its change carries `purged: true` so SSE clients,
  `/v1/changes` readers and offline caches delete the entity), deletes every older version on both
  tiers, then deletes the entity's embeddings, redacts referencing alerts (`{"redacted": true}`; or
  deletes them with `purgeAlerts: true`), redacts stored notification bodies and drops its metric
  series. Ask messages and briefing runs that cite the entity are counted in the report, not edited.
  The head disappears with a later retention pass.
- **Messages already delivered to external channels (email, chat, push) cannot be recalled.**

### A note on data-protection law

Tracking your own and your household's devices on hardware you control is the kind of purely personal
or household activity that the EU GDPR (Article 2(2)(c)) and similar laws place outside their scope.
That exemption may not cover tracking people outside your household, or sharing the data further. This
is not legal advice: check the rules that apply to you, and get consent from the people you track.

## Retention

`GET /v1/settings/retention` shows every window; `PUT` replaces the whole object (every window at
least `1h`); `POST /v1/settings/retention/run` runs the job now and returns what it deleted. The job
also runs hourly, starting 5 minutes after boot.

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "provenance": "30d", "alerts": "90d", "usage": "30d",
 "notifications": "90d", "audit": "90d", "kinds": {"earthquake": "3650d"}, "sources": {"usgs-earthquakes": "1825d"},
 "stats": {"hourly": "35d", "daily": "1825d"}}
```

**Which window applies.** A TRACK source always uses `trackWindow`. A STANDARD record's window is the
first of:

1. `kinds.{kind}` (yours);
2. `sources.{id}` (yours; the same value as `PUT /v1/sources/{id}` `retention`, stored once);
3. the descriptor's per-kind default (`kindRetention`);
4. the descriptor's `defaultRetention` (adapters set it by analytic class: events 1825 days, series
   3650 days, bulk events 90 days);
5. `default` (7 days).

A `sources` entry for a TRACK source, or a `kinds` entry for a kind only TRACK sources emit, is
`400 RETENTION_TIER_FIXED`; unknown sources and kinds are listed in one `400`.

**What the job removes, in order.**

| Step | What |
|---|---|
| TRACK | Whole track partitions (`rv_t_*`) once they are older than `trackWindow`; never row by row |
| STANDARD | Versions older than their window, in transactions of 10,000 rows |
| Partitions | Standard partitions that are older than the largest window and already empty |
| Heads | Records whose every version is gone and that nobody has observed for a window, purged by the writer |
| Provenance | `fetch_log` rows that wrote nothing after 7 days, the others after `provenance` |
| `seq_clock` | Minutes older than the largest window plus a day |
| Other | Resolved or suppressed alerts after `alerts` (open alerts stay), usage minutes after `usage`, delivered or failed deliveries after 7 days, notifications after `notifications` |
| Audit | Audit log entries after `audit` (default 90 days) |
| Inbox | Notifications after `inbox` (default 30 days) once no delivery refers to them, read cursors of devices unseen for 90 days, expired presence and action capabilities expired for a week |
| Stats | Activity rollup rows: hourly after `stats.hourly` (default 35 days), daily after `stats.daily` (default 1825 days) |

**Current state is kept while it is current** (FR-17 as amended). Versions follow the window, but a
record that is still being observed keeps a version newer than a quarter of its window, so it never
disappears. A record purged after going quiet that comes back starts again at version 1.

**Durable consumers hold retention back.** The trigger engine's, the embedding worker's and the
activity rollup's cursors (and later the intelligence cursors) clamp every cutoff, so nothing is deleted before it was evaluated. If a cursor
falls more than two windows behind, retention stops waiting for it, counts
`foresignal.retention.consumer_lag` and sends a `system` notification; `/v1/system` then shows
`RETENTION_CONSUMER_LAG`.

**Clients.** A change-feed cursor below a purge gets `410 RESYNC_REQUIRED` (re-snapshot, then resume);
an SSE stream resumed below the retained floor gets `reset`. Routine version deletes never force a
resync.

### History downsampling

TRACK history (aircraft, vessels, other moving things) is kept at full detail for
`trackRawWindow` (24 hours by default). After that, the retention job thins one track partition per
hourly run, oldest first, until the partition reaches the end of `trackWindow` and is dropped.

**What is kept** in a thinned partition:

- every version that matters on its own: the first sighting, a reappearance, a significant change
  (callsign, squawk and the like), a tombstone and every trigger edge;
- the newest version of each entity in the partition;
- every version a current record or an alert points at;
- of the remaining positions, one per entity per `trackDownsampleInterval` (5 minutes by default).

So alerts, trigger edges and current records always resolve. What goes is the dense run of ordinary
position samples in between.

**Storage effect.** A thinned partition keeps at most one ordinary sample per entity per interval.
For an entity sampled once a minute at the default 5-minute interval, that is at most one ordinary
sample in five, plus the versions above. How much TRACK storage that saves depends on how many of
your versions are edges and keyframes. `/v1/system` `history.tiers[]` shows `downsampledLeaves` and
`downsampledRows`, and the `foresignal.history.downsample.*` metrics count the rows before and after.

**Reading thinned history.**

- `/versions` items inside a thinned partition say `downsampled: true`. Their `prevSeq` may name a
  version that downsampling removed.
- An asOf read over thinned history treats each surviving position as valid for
  `max(ttl, interval)`, so a stationary aircraft stays on the map between survivors. The answer says
  `approximate: true`.
- A trigger backtest (`POST /v1/triggers/{id}/test`) over thinned history warns `DOWNSAMPLED_RANGE`.
  Match-mode counts there are lower than they were live; edge rules are unaffected. Live triggers
  never see thinned data.

**Settings.**

```json
PUT /v1/settings/retention
{"default": "7d", "trackWindow": "3d", "trackRawWindow": "24h", "trackDownsampleInterval": "5m"}
```

- `trackRawWindow` must be shorter than `trackWindow`.
- `trackDownsampleInterval` must be between `30s` and `trackRawWindow`.
- A TRACK source can use its own interval: `PUT /v1/sources/{id}` with
  `{"downsampleInterval": "1m"}`, and `{"downsampleInterval": null}` to go back to the global one.
  A STANDARD source is refused with `400 VALIDATION_FAILED`, because its history is never thinned.

**How to disable it.** Set `"trackRawWindow": null`. Partitions that are already thinned stay as
they are until retention drops them; nothing new is thinned.

## Radar and satellite products

Prompt 046 adds the first two product sources. Like every source they start disabled; enable them
with `PUT /v1/sources/{id}` and `{"enabled": true}`.

| Source | Products | Upstream | Poll | Default budget |
|---|---|---|---|---|
| `mrms` | `mrms-reflectivity`, `mrms-precip-rate` (band 2 is the PrecipFlag type code), `mrms-mesh-60m` | NOAA MRMS, bucket `noaa-mrms-pds`, anonymous | 2 min, every frame | 30/min, 12,000/day, 4,000 MB/day |
| `goes-east-abi` | `goes-east-ir` (ABI band 13, clean IR) | GOES-19 CMIPC, bucket `noaa-goes19`, anonymous | 5 min | 10/min, 2,000/day, 2,500 MB/day |

Expect about 1.7–3.4 GB a day of downloads for `mrms` and 1.0–1.1 GB for `goes-east-abi`, and a
rolling disk of about 2 GB and 1.2 GB under the default retention ladders (MRMS: every frame for 6 h,
then 10-minute frames to 48 h; GOES: 5-minute frames for 24 h, then 30-minute frames to 72 h). The
`mbPerDay` budget is charged with each object's listed size **before** it is downloaded, so a day
whose byte budget is spent stops fetching rather than overspending.

**Health.** A source degrades when its newest frame is older than the product's `maxGap` (6 min for
MRMS, 15 min for GOES), and when an upstream file arrives without the field or variable Foresignal
expects ("field MergedReflectivityQCComposite missing at level 500 m", "variable CMI missing"): no
frame is written for such a file, because an empty frame would read as clear weather.

**The decoder self-test.** At startup Foresignal decodes four small bundled files, one per decoding
path: GRIB2 complex packing (GFS), GRIB2 PNG (MRMS), GRIB2 CCSDS/AEC (ECMWF, through the native
libaec library) and NetCDF4 (GOES). If a path fails, only the products that need it are disabled:
their tile, image and frame endpoints answer `503 PRODUCT_UNAVAILABLE`, the adapter skips them, and
the source's health shows `disabled: decoder self-test failed (…)`. The log line
`Decoder self-test failed for …` names the cause. The usual one is the native library: JNA extracts
libaec into `jna.tmpdir`, which the container sets to `/data/tmp` on the data volume. That directory
must allow executables. A named Docker volume does; a bind mount from a `noexec` filesystem does
not. Restart after fixing it. `foresignal.raster.decoder-self-test.enabled=false` turns the test off
(products are then never disabled by it).

**Files.** Downloads are staged in `${FORESIGNAL_DATA_DIR}/rasters/tmp/` and deleted after decoding.
NetCDF-Java's caches live in `${FORESIGNAL_DATA_DIR}/cache/netcdf/` (normally empty). Foresignal
never opens a NetCDF or GRIB file outside `rasters/tmp` and `rasters/selftest`.

### Building when the Unidata repository is down

NetCDF-Java is the one dependency not on Maven Central: Gradle fetches `edu.ucar` and
`edu.ucar.unidata` only from `https://artifacts.unidata.ucar.edu/all/` (Architecture §2). A build
host that has built Foresignal before already has the artifacts in its Gradle cache. To build
without the Unidata host:

1. Create `gradle/vendor-repo/` as a Maven repository layout and copy into it, from a machine that
   has them, every `edu.ucar` and `edu.ucar.unidata` jar and pom listed in
   `gradle/verification-metadata.xml` (for example from
   `~/.gradle/caches/modules-2/files-2.1/edu.ucar/`, renamed to
   `edu/ucar/<name>/<version>/<name>-<version>.{jar,pom}`).
2. In `build.gradle.kts`, replace `maven("https://artifacts.unidata.ucar.edu/all/") { name = "unidata" }`
   with `maven(uri("gradle/vendor-repo")) { name = "unidata" }`. Keep the name and the
   `exclusiveContent` filter: `checkUnidataScope` and the checksums in
   `gradle/verification-metadata.xml` keep applying, so a tampered or wrong file fails the build.
3. Build. Revert the line once the host is back; the vendored files need not be committed.

After a NetCDF-Java upgrade, regenerate the checksums with
`./gradlew --write-verification-metadata sha256 help`, then trim the file back to the `edu.ucar`
components and the `<trusted-artifacts>` rule.

## Static maps

`GET /v1/maps/static` (read scope; prompt 055) draws a PNG from the bundled Natural Earth 1:50m basemap and
Foresignal's own stored frames, so it works offline and **never fetches third-party tiles**. Give a box
(`bbox`), a `center` with `radiusKm`, an `alert` id (the map centers on its record geometry, radius default
50 km) or a `place` (a token is required; adds a marker and range rings), plus `products` (at most 4) with
optional `style`s, `time` (`latest`, `now`, an ISO time or an offset), `width` (≤ 1280, default 800),
`height` (default width × 0.75), `theme=light|dark` and `units=metric|imperial`. Every map shows each
product's frame valid time (UTC, plus the place's local time), a legend, the attribution, a scale bar and
the footer "Automated — not an official warning". A product without a usable frame is drawn as a note
("MRMS reflectivity unavailable — not evidence of clear weather"), never left out silently. The MCP tool
`get_map_image` returns the same map (≤ 512 px, ≤ 200 KB) with a text caption, and map attachments of
alerts use it too.

The response is immutable only when every frame is pinned (an ISO `time`, a versioned `style` per product,
a pinned `run` for forecasts, no `place`); otherwise it gets a short max-age and an ETag. Renders run on
the render pool within 2 s; under load the answer is `503 RENDER_BUSY` with `Retry-After: 1`.

**Your own tile server (optional).** `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, an XYZ template such as
`http://tiles.lan:8080/{z}/{x}/{y}.png`, replaces the Natural Earth fills with tiles from a server **on your
own network**: a host that does not resolve to a loopback or private address is refused (logged once per
map) and Natural Earth is used. Tiles are fetched only while a map is drawn (at most 16 per map, 0.8 s
each) and are never cached or passed on. Do **not** point it at `tile.openstreetmap.org`: the OpenStreetMap
tile usage policy forbids this kind of use, and Foresignal refuses public hosts anyway.

## Feeds, Google Earth and Cesium

Prompt 058 lets ordinary feed readers, Google Earth and Cesium read Foresignal with no custom code. All of
these are `read` operations: open while `openReads` is on, otherwise reached with a token or, for clients
that cannot send headers, a feed key in the path (`/v1/k/{key}/…`). Private sources appear only for a
token or feed key holding the private grant, and a URL that carries a key is never cached publicly.

**Feed readers (Atom and JSON Feed).**
- Alerts: `GET /v1/alerts/feed.atom` or `/v1/alerts/feed.json`, filtered by `rule`, `minSeverity`
  (`info|warning|critical`), `source`, `kind` and `state` (`firing|acknowledged|resolved`), `limit` 1–200
  (default 50), newest first by last change (fired, acknowledged or resolved).
- Briefing runs: `GET /v1/briefings/{id}/runs/feed.atom` or `.json`, with the whole run text.
- Entry links open your app when you set `clientLinks` in the server settings, else the API URL under
  `publicBaseUrl`, so set `publicBaseUrl` to a name the reader can reach. Content is the alert message
  (and its enrichment summary) as sanitized HTML: no scripts, images or styles, only `http`/`https`
  links. Atom entries carry GeoRSS (`georss:point`, or `georss:polygon` for a simple polygon). Readers that
  send `If-None-Match` or `If-Modified-Since` get `304` until something changes.
- With `openReads` off, mint a feed key for the reader, for example
  `POST /v1/feed-keys {"name":"reader","prefixes":["/v1/alerts/feed.atom"],"restrictions":{"kinds":["earthquake"]},"expiresAt":"2027-06-01T00:00:00Z"}`,
  and subscribe to `{publicBaseUrl}/v1/k/{key}/alerts/feed.atom`. A feed key never sees alerts of watchlist
  rules.
- RSS 2.0 and WebSub are not offered: every current reader understands Atom or JSON Feed, and readers poll.

**Google Earth.** Add a network link to `{publicBaseUrl}/v1/kml/networklink.kml?layers=usgs-earthquakes,nws-alerts&refresh=60`
(Add → Network Link in Google Earth Pro; or open the file). It holds one NetworkLink per layer to
`/v1/kml/records.kml?layer=…`, refreshed every `refresh` seconds (at least 30) and two seconds after the
view stops, with the view's bounding box. Each refresh returns at most 5,000 Placemarks per layer, newest
first; when more match, the document's snippet says `truncated: showing 5000 of N`, so zoom in. Names,
descriptions and values are escaped for HTML, so upstream text can never inject markup into a balloon.
Other exports:
- `GET /v1/kml/records.kml?source=…|kind=…&bbox=…&asOf=…&format=kmz` for a one-off file (KMZ bundles the
  icons);
- `GET /v1/kml/tracks.kml?sourceId=…&externalId=…&from=…&to=…` for one entity's `gx:Track` history
  (unsimplified, at most 5,000 points), or `?kind=…&bbox=…` for up to 100 entities;
- `GET /v1/triggers/{ruleId}.kml` for a trigger's geofence.

With `openReads` off, use a feed key whose prefixes include `/v1/kml/` and `/v1/map/` (the icons), and add
`{publicBaseUrl}/v1/k/{key}/kml/networklink.kml`: the generated links keep the same key, and the access
log shows only its first eight characters.

**Cesium.** Load the same NetworkLink document as a data source; Cesium needs the camera and canvas to fill
the view bounding box:

```js
viewer.dataSources.add(Cesium.KmlDataSource.load(
  `${base}/v1/kml/networklink.kml?layers=usgs-earthquakes`,
  { camera: viewer.scene.camera, canvas: viewer.scene.canvas }));
```

**MapLibre style.** `GET /v1/map/style.json?layers=…&theme=light|dark` is an overlay style built from each
kind's display hints, with an SDF sprite (Maki and Temaki icons) and no glyphs; `docs/Frontend-Guide.md`
("Overlay style and sprite") shows how to add it over a basemap. Tile keys reach it when their prefixes
include `/v1/map/` and `/v1/tiles/`.

## Forecast models (GFS and HRRR)

Prompt 051 adds two forecast model sources. Both start disabled; enable them with
`PUT /v1/sources/{id}` and `{"enabled": true}`.

| Source | Products | Runs and steps | Poll | Default budget |
|---|---|---|---|---|
| `gfs` | `gfs` (10 m wind, u/v), `gfs-gust`, `gfs-t2m`, `gfs-apcp` (precipitation since the run started) | 4 runs a day; f000–f048 every 3 h, f054–f120 every 6 h | 15 min | 60/min, 3,000/day, 1,000 MB/day |
| `hrrr` | `hrrr` (10 m wind, u/v), `hrrr-refc`, `hrrr-gust`, `hrrr-smoke` | every 3 h, or every 6 h with `FORESIGNAL_PROFILE=small`; f00–f18 | 10 min | 60/min, 4,000/day, 2,000 MB/day |

Neither source downloads whole files (a GFS step is about 0.5 GB, an HRRR step about 150 MB). Each
step's `.idx` inventory names every message and its byte offset; Foresignal reads only the messages
it needs, with ranged requests, each charged to `mbPerDay` before it is sent. Expect about 0.4–0.5 GB
a day for `gfs`, and about 0.6 GB (small profile) or 1.1 GB (large) for `hrrr`, whose 10 m winds are
stored uncompressed upstream. At most 10 steps are read per poll, so a run completes over a few polls.
Disk: the current and the previous complete run are kept per product (about 29 steps × 4 products
for GFS), plus the GFS wind fields at 1° and 0.25° (about 6 MB compressed per step).

**What clients see.** A forecast product gets one `raster-frame` record version per **completed** run
(every configured step has a frame), not one per step; `{productId}:partial` reports a run in progress.
Tiles of a forecast frame need a run: a concrete time without `run=` answers `400 RUN_REQUIRED`
(`run=latest`, `previous` or the run time). The `gfs` product also serves God's Eye View wind fields
(`/v1/products/gfs/fields/…`) for web particle layers, and both wind products serve barb vector tiles
(`/v1/products/{id}/vectors/…`) and the `wind-speed@1` raster style. See the Frontend Guide.

**HRRR cadence by profile.** The small profile ingests every other HRRR run (00, 06, 12, 18z), which
halves the downloads; set the source's `runEvery` param to `3h` to ingest every run anyway, or `6h` on
a large host to save bandwidth. `runEvery` must be whole hours dividing 24.

**GFSv17.** NCEP plans GFS v17 (PNS 26-29/26-30). If its file layout changes, set the `gfs` source's
`pathTemplate` param instead of waiting for a release, for example
`{"params": {"pathTemplate": "gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}"}}` (the
default; tokens `{yyyyMMdd}`, `{HH}`, `{FFF}` three-digit step, `{FF}` two-digit step). Fields are
found by variable and level in the `.idx`, never by position, so new or reordered fields do not
matter. The `steps` param (for example `["0-48/3", "54-120/6"]`) and `availabilityLag` can be changed
the same way.

**Health.** A model source degrades when:
- its newest frame was ingested more than two runs ago (12 h for GFS, 6 h or 12 h for HRRR);
- the first `.idx` of the newest run is still missing 2 h after `availabilityLag` (3.5 h for GFS,
  50 min for HRRR): the reason names the missing object;
- an `.idx` lacks a field: "possible format change: GFS idx missing GUST:surface". No frame is written
  for that product (a missing wind field stops the whole step), so a format change never produces
  empty frames.

## Severe weather records

Prompt 047 adds four record sources (installed disabled, like every source):

| Source | What it brings | Notes |
|---|---|---|
| `nws-alerts` | NWS watches, warnings and advisories | Every 60 s. Param `area` (state or marine codes, empty = national) and `minSeverity`. The NWS asks for a contact in the User-Agent: set `contactEmail` in the server settings (`PUT /v1/settings/server`), which Foresignal appends as `contact=…`. |
| `mrms-probsevere` | MRMS ProbSevere v3 storm objects | Every 2 min, newest file only, inside the places area of interest (no places: the Texas box, with a health warning). Automated guidance, never a warning. |
| `spc-outlooks` | SPC day 1–3 convective outlooks | Every 15 min, 17 conditional requests, mostly 304. A missing layer shows the source `degraded` with "SPC layer {file} missing". |
| `awc-metars` | Airport METARs | Every 5 min (AWC allows 1 request per minute per thread). Param `scope` (`aoi`, `conus`, `global`) or a `stations` allowlist. A report stays current for 3 hours. |

```json
PUT /v1/settings/server
{"publicBaseUrl": null, "contactEmail": "ops@example.org"}
```

None of these sources ever turns a failed fetch into an all-clear: a record is tombstoned only when a
complete, successful response no longer contains it. When a feed is down its records stay, expire on
their own, and the source shows `degraded`. Rule recipes are in the Agent Guide, "Weather alerts", and for the
derived place weather below, "Weather alerts".

## ECMWF IFS, forecast areas and storm tracking (prompt 105)

**`ecmwf-ifs`** adds the ECMWF IFS 0.25° open-data model as a third forecast alongside GFS and HRRR. It reads the
`ecmwf-forecasts` S3 mirror (falling back to `data.ecmwf.int/forecasts` by editing the `pathTemplate`), plans byte
ranges from each step's JSON `.index`, and costs about **0.5–1 GB/day** (20/min, 3,000/day, 1,200 MB/day; one
20-minute check, 00z and 12z, steps 0–144 by 3). Products: `ifs` (10 m wind), `ifs-gust`, `ifs-t2m`, `ifs-mslp`,
`ifs-precip-3h` and `ifs-precip-24h`. The whole real-time catalogue is CC BY 4.0: **every derived output carries the
"Contains modified Copernicus/ECMWF data … changes made" attribution** (the `forecast-area` payload and the
place-weather `forecast.ifs` section carry it as `attribution`). The 00z/12z run appears about 7 h after its
reference time. A step is marked fetched only when every product that has it was committed; a step whose products
did not all commit (a missing reference message, a missing field) is retried on the next checks, at most 3 times.

**Run staleness is per model.** A derived weather section (place weather, forecast areas) calls a model's newest run
`stale` only once its successor should already be ingested: run cadence + availability lag + an ingest allowance.
Defaults: GFS 11.5 h, GFS-Wave 12 h, HRRR 7 h 50 min (the 6-hourly small-profile cadence), NBM 9 h, AIFS 16 h,
IFS 22 h. A slower cadence set by the operator makes sections go stale early, never late.

**`wx-forecast-areas`** (`forecast-area` records) thresholds a model field over the places AOI on each completed run:
the polygon where it is forecast to cross the threshold within the `thresholds` horizon (default 120 h, at most 120).
Defaults cover gust ≥ 20 and ≥ 25 m/s, 24-hour precipitation ≥ 50 mm and 2 m temperature ≥ 313.15 K (40 °C) and
≤ 263.15 K (−10 °C) for GFS, HRRR and IFS where the product exists. `validFrom` is the first exceeding valid time and
`validUntil` the last exceeding valid time plus the step, so a `forecast-area` rule can use `forecastHorizon`. A
typical recipe: "IFS gust ≥ 25 m/s within 48 h at home" is `kind=forecast-area`, `near{home}`, `forecastHorizon 48h`,
`enter`. A version is written only when the quantized payload changes or the polygon changes by more than 10%
(symmetric difference over union); `run` alone is volatile, so a newer run forecasting the same area writes nothing.
When a newer run drops the exceedance the area is **tombstoned** (an `exit`). The peak of a `<=` threshold is the
minimum.

**`wx-storm-tracks`** (`storm-track`, `storm-swath`) tracks MRMS storm cells with TITAN-lite: advected-polygon IoU
with a Hungarian assignment, merges and splits. Limits: it runs on the 2-minute MRMS frames inside the places AOI
(`scope=conus` on the large profile only), the nowcast is a straight **linear advection** of +15/+30/+60 min, and the
GLM lightning-jump flag (`2sigma-glm-v1`) is **advisory** — GLM's footprint over Texas is about 8–10 km and its
detection efficiency is below 100%. `storm-swath` is the union of the cells along the track, written every 10 min and
when the track ends (by a merge or by two missed frames); a restart extends the stored swath. A track keeps a
12-position `history` (volatile: a frame that only moves the history writes no version); its motion is the least
squares over the newest five positions. In a merge the largest track continues. Automated, not a warning.

**Annotators.** `outage-weather` adds `context.weather` (NWS warnings, radar ≥ 50 dBZ areas and storm tracks, and
gusts ≥ 25 m/s from `forecast-area` gust areas or METAR observations, within ±3 h and 25 km; read from the stored
versions in that window, so weather that has since ended still counts) to `infrastructure-disruption` and
`weather-impact` events as **context members** — background, never
supporting evidence, so they never raise confidence — and adds the alternative "weather-related damage". The
weather-impact recipes `dry-lightning-ignition`, `airport-weather` and `smoke-aq` create events from weather
co-occurrences.

## Extended weather products (prompt 106)

**Per-product enablement.** Every extended source has a `products` string-list parameter; a product not
listed is not fetched or stored. Left unset (it has no descriptor default), the memory profile
(`FORESIGNAL_PROFILE`) decides: the **small** profile turns on GOES-East C13/C08/C09, `rtma-ru`, `nbm`,
the OVATION aurora raster, TAF, HRRR smoke (surface and column), `wpc-heatrisk` and `epa-uv`; the **large**
profile also turns on C02, `goes-west-abi`, `gfs-wave`, the MRMS extras (lightning probability, VIL, echo
top), `sst-geopolar` and `aifs`. The mesoscale sectors are opt-in on both (list them in `goes-east-abi`'s
`products`), and `nexrad-l3` is opt-in like every source. Set the parameter with
`PUT /v1/sources/{id}/params` (or the source settings API).

**New sources.** `goes-west-abi` mirrors the East clean IR on GOES-18; `goes-east-abi` adds the C08/C09
water-vapour channels, the C02 visible (daytime only, cropped to the AOI + 300 km) and the 1-minute
mesoscale sectors (AOI-gated, every second frame). `rtma-ru` is the 15-minute RTMA Rapid Update 2.5 km
CONUS analysis (observation; about 3.4 GB/day for all five products — set `analysisEvery` to `30m` or `60m`
to thin it); `nbm` is the National Blend of Models core; `gfs-wave` (HTSGW/PERPW/DIRPW) feeds the
place-weather `marine` section for places tagged `coastal`; `aifs` is the ECMWF AIFS machine-learning
forecast (CC BY 4.0). The `mrms` source gains lightning probability, VIL and echo top; the `hrrr` source
gains `hrrr-smoke-column` (COLMD); `swpc-aurora` gains the global `aurora-probability` raster; `sst-geopolar`
adds the daily Geo-Polar SST over an ocean bbox; `wpc-heatrisk` adds the **experimental** NWS HeatRisk
days 1–7 (place-weather `heat`); `epa-uv` adds the hourly UV index per place — set a place's `zip`
attribute (`PUT /v1/places/{id}` `{"attributes":{"zip":"78701"}}`) for it to be fetched; places without
one are named in the source's health note. **`nexrad-l3`** (opt-in) ingests N0B reflectivity and N0G
velocity for the nearest WSR-88D sites (`sites`, or `siteCount` nearest the home), decoded with
NetCDF-Java's NIDS reader and resampled to 0.005° within 230 km.

**Products with a byte budget** set `mbPerDay`; when the projected day's download, at the rate seen so
far, exceeds **120%** of it, the source degrades with a `projected download … exceeds …` reason
(`BudgetOutlookHealthRule`, prompt 106), before the budget is actually refused.

**Field encodings.** Besides `.f32`, each global wind field (`gfs`, `ifs`, `aifs`) serves
`.velocity-json` (leaflet-velocity / wind-js, gzipped, ≤ 300,000 points per component) and `.png-uv`
(R = u, G = v, an 8-bit RGB PNG for WebGL particles); both are listed in the field manifest's
`encodings` block. **Position sampling:** `/v1/sample?record={sourceId}/{externalId}` samples at an
entity's live position (honouring `private`); a position older than 15 minutes marks the values
`stale-position`. `POST /v1/sample/track` samples each point of a track (given, or a record's 037
track) at its own time.

## Lightning and fire

Prompt 048 adds four more sources (installed disabled):

| Source | What it brings | Notes |
|---|---|---|
| `goes-glm` | GOES-19 GLM lightning | Every 30 s, every 20-second granule (about 1.1–1.9 GB a day). Flashes go to the flash store, not to records: a daily archive under `${FORESIGNAL_DATA_DIR}/rasters/glm/G19/{yyyymmdd}.bin` (16 bytes per flash, about 28 MB a day, kept 14 days, counted toward `FORESIGNAL_RASTER_DISK_QUOTA_GB`) and a 60-minute in-memory ring of the flashes inside the places area. Records are `lightning-cluster`. |
| `goes-fdc` | GOES-19 fire detections | Every 5 min, newest scan only. Param `scope` (`aoi` default, or `conus`). Records `fire-detection` (one per pixel) and `fire-cluster`. |
| `hms-smoke` | NOAA HMS analyst smoke polygons | Every 30 min, conditional requests; today's file, and yesterday's until 06:00 UTC. A 404 before the first analysis of the day is normal. |
| `hms-fire` | NOAA HMS analyst fire points | Same schedule and files as `hms-smoke`; records share `fire-detection` with `goes-fdc`. |

**The GLM ring scope.** `foresignal.raster.glm-ring-scope` (`FORESIGNAL_RASTER_GLM_RING_SCOPE`) is `aoi`
by default: the ring keeps the flashes inside the union of your places' buffers (no places: the
`weather.defaultAoi` box). The large profile (`FORESIGNAL_PROFILE=large`) may set `conus`; the small
profile logs a warning and stays on `aoi`. The archive always keeps the full field of view, and the ring
is rebuilt from it at startup and whenever a place changes. `GET /v1/system` reports
`rasters.glm{ringFlashes, archiveBytes, newestFileAt}`.

**Stale is not clear.** When the GLM or FDC feed stops, clusters and detections freeze: nothing is
tombstoned and no `exit` rule fires. The source shows `degraded` with "newest input file is …s old" once
the newest processed file is older than 2 minutes (GLM) or 15 minutes (FDC). An unreadable file or a
missing NetCDF variable also shows `degraded` with the reason, and the file is skipped. Rule recipes are
in the Agent Guide, "Lightning and fire".

### NASA FIRMS (`nasa-firms`, prompt 065)

`nasa-firms` brings NASA FIRMS near-real-time active-fire detections worldwide: VIIRS on NOAA-20, NOAA-21 and
Suomi-NPP, and MODIS, by default; Landsat (`LANDSAT_NRT`, US and Canada only) can be added. Records are
`fire-detection`, the kind `goes-fdc` and `hms-fire` write, so one "fire near home" rule covers all of them; fire
radiative power is `frpMw` in all three. Each detection stays current for 24 hours after its acquisition time and
is kept 7 days.

**Obtaining a MAP_KEY.** FIRMS needs your own free MAP_KEY, and the source stays disabled until one is stored
(enabling without it is a `409 CREDENTIALS_REQUIRED`, and no request is ever made without it):

1. Open <https://firms.modaps.eosdis.nasa.gov/api/area/>, choose **Get MAP_KEY** and enter your e-mail address.
   FIRMS e-mails the key.
2. Store it and enable the source:

   ```json
   PUT /v1/sources/nasa-firms/credentials
   {"mapKey": "…"}
   ```

   then `PUT /v1/sources/nasa-firms` with `{"enabled": true}`. A key FIRMS refuses makes every fetch fail with
   "FIRMS refused the MAP_KEY", which the source's health shows. The key is part of the request URL; Foresignal masks it as `***` in fetch
   traces, probes, the fetch log and errors.

A key allows 5,000 transactions per 10 minutes, and a large request may count as several. The default schedule
(every 30 minutes, four products one after another, plus yesterday's file for each product between 00:00 and
06:00 UTC) stays far below that.

**Setting `area` to reduce bytes.** By default each request asks for the whole world, and a busy day's VIIRS file
for the world runs to many megabytes. If you only care about one region, set `area` to
`west,south,east,north`, for example `PUT /v1/sources/nasa-firms` with `{"params": {"area": "-125,24,-66,50"}}` for
the contiguous United States.
The box must not cross the antimeridian. A small box can legitimately have no fires; this source does not treat
zero results as normal, so a quiet small area can show a warning. Other parameters: `sources` (the products, in
fetch order) and `window` (how old a detection may be when first emitted, 1h to 48h, default 24h).

**Incremental fetching.** FIRMS always returns the whole day, so each run emits only detections from three hours
before the newest one already ingested, per product; re-sent detections are touches, not new versions.

## Weather alerts

Prompt 052 adds three **derived** sources (installed disabled). They make no upstream request: they read
the frames and records the weather sources already stored, and write a record only when its rounded
content changes.

| Source | Kind | What a record is |
|---|---|---|
| `wx-places` | `place-weather` | one per place (`place:{id}`): radar, hail, rain rate, lightning, storm-cell ETA, smoke and GFS forecast sections, re-evaluated on every MRMS frame and every 60 s |
| `wx-areas` | `wx-area` | one per configured area (`refl:50:aoi`, `mesh:25:home`): the area above a threshold inside the places AOI or a place, every 6–10 min |
| `wx-cells` | `radar-cell` | one per tracked storm cell (`c-{ulid}`): maximum reflectivity, area, hail size, GLM flashes and centroid motion |

Enable the inputs you want first (`mrms`, `goes-glm`, `gfs`, `hrrr`), then the derived sources:
`PUT /v1/sources/wx-places {"enabled": true}` and the same for `wx-cells` (the `cell` section needs it)
and `wx-areas`. `wx-areas` takes `areas` (a list of `field:threshold:aoi`, field `refl`, `mesh`, `precip`,
`rotation` (s⁻¹), `qpe1h` (mm) or `ffg` (% of flash-flood guidance))
and `interval` (6–10 min); `wx-cells` takes `scope` (`aoi`, or `conus` on the large profile). Each derived
source shows in `GET /v1/sources/{id}/health` like any other: every invocation (a frame, a change batch, a
tick) writes one `fetch_log` row with `principal: derived`.

**Unknown is not clear.** Every section has a `status`: `ok`, `stale` (MRMS older than 6 min, GLM older
than 2 min, model runs older than 12 h), `no-coverage` or `unavailable` (input disabled, failing or never
seen). A section that is not `ok` keeps its last values and adds `asOf`, the time of the last good data;
before any good data it carries only its status. "None within 100 km" is `999`, never null. So a stopped
feed can never look like clear weather, and a derived record is never tombstoned because its input is
stale; it is tombstoned only when its object disappears (a storm cell no longer present in a processed
frame) or its place is deleted. **Deleting a place tombstones its `place-weather` record, which fires
`exit` rules whose condition held** (a tombstone after a matching version is an exit).

**GLM limits.** GLM's footprint over Texas is about 8–10 km and it detects less than every flash. GLM-only
rules should use 15 km or more, and a lightning all-clear is advisory.

**Write derived rules with `enter` and `exit`** (or an explicit cooldown), never a bare `match`: a
place-weather record versions whenever any section changes, and `match` would fire on each version.
Conditions are AND-only, so all-clear rules use the **verified-clear helpers**:
`radar.verifiedNearest50Km10m` and `lightning.verifiedNearestKm30m` equal the metric while the section is
`ok` and `0` otherwise. A condition `<= X` on them is therefore true when a storm is near **or** the data is
unknown, and its `exit` fires only when the data is known **and** far.

**≥ 50 dBZ within 20 km:**

```json
{"name": "Heavy echoes near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "radar.nearest50Km10m", "op": "<=", "value": 20}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "≥ 50 dBZ within {{payload.radar.nearest50Km10m}} km of {{payload.place}} (automated, not a warning)"}
```

**Echoes cleared (verified only).** P is "near or unknown"; `exit` fires when the previous version was near
or unknown and the current one is `ok` and far:

```json
{"name": "Echoes cleared at home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "radar.verifiedNearest50Km10m", "op": "<=", "value": 20}],
 "mode": "exit", "severity": "info",
 "messageTemplate": "No ≥ 50 dBZ within 20 km of {{payload.place}} for 10 min (verified radar)"}
```

**Lightning within 15 km, with an advisory all-clear:**

```json
{"name": "Lightning near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "lightning.nearestKm15m", "op": "<=", "value": 15}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "Lightning {{payload.lightning.nearestKm15m}} km from {{payload.place}} (GLM, automated)"}
```

```json
{"name": "Lightning all-clear at home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "lightning.verifiedNearestKm30m", "op": "<=", "value": 15}],
 "mode": "exit", "severity": "info",
 "messageTemplate": "No GLM lightning within 15 km of {{payload.place}} for 30 min (advisory)"}
```

**Storm core within 30 minutes:**

```json
{"name": "Storm core approaching home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "cell.eta50Min", "op": "<=", "value": 30}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "A ≥ 50 dBZ core may reach {{payload.place}} in about {{payload.cell.eta50Min}} min"}
```

**Flash-flood guidance exceeded near home** (prompt 053). `precip.ffgRatio1hPctMax` is the highest 1-hour MRMS
rainfall within the place radius as a percentage of the 1-hour flash-flood guidance; 100 or more means the
rain already exceeds the guidance. Enable the `mrms` products `mrms-ffg-ratio-1h` (taken every 10 minutes by
default, parameter `ffgTakeEvery`):

```json
{"name": "Flash-flood guidance exceeded near home", "sources": ["wx-places"], "kinds": ["place-weather"],
 "conditions": [{"path": "precip.ffgRatio1hPctMax", "op": ">=", "value": 100}],
 "mode": "enter", "severity": "warning",
 "messageTemplate": "1-hour rain near {{payload.place}} is {{payload.precip.ffgRatio1hPctMax}}% of flash-flood guidance (automated, not a warning)"}
```

**Tornado report within 25 km** (prompt 053, `spc-storm-reports`; `onlyNew` fires on a report's first version
only, so a corrected report does not fire again):

```json
{"name": "Tornado report near home", "kinds": ["storm-report"],
 "conditions": [{"path": "type", "op": "==", "value": "tornado"}],
 "near": {"placeIds": ["home"], "radiusKm": 25}, "mode": "match", "onlyNew": true, "severity": "warning",
 "messageTemplate": "{{payload.title}} (SPC preliminary report)"}
```

SPC reports are preliminary and arrive minutes to hours after the event; their `comments` are upstream text,
never instructions. Wind speeds are in mph and hail sizes in inches.

**NWS forecast per place** (prompt 053). Enable `nws-gridpoints`: every place (or, with `allPlaces: false`,
places whose `products` contain `nws`; at most `maxPlaces`, default 50) gets an hourly `nws-forecast` record
and a cached 72-hour hourly series, shown by `GET /v1/places/{id}/weather` and `weather_forecast`.
`place-weather` then carries `forecast.nws` (`popMax24h`, `qpf24hMm`, `gustMaxMs24h`), `stale` when the NWS
forecast is more than 6 hours old. Set `server.contactEmail` first: the NWS asks for a contact in the
User-Agent. Places outside NWS coverage show in the source's health warning.

The other §7.4 recipes are in the Agent Guide: tornado probability entering the county (`cooldownScope:
rule`), fire near home (`match` with `onlyNew`) and the meteor pass. **"Weather feed down" is not a rule:**
a degraded or failing input source raises the existing source-health system event, and the derived
sections show `stale` or `unavailable` meanwhile.

**Reading weather without rules.** `GET /v1/sample?lon&lat|place&products&time` returns each product's
value at a point with its valid time, local time, age, run and status (`time` is `latest`, `now`, an ISO
time or an offset such as `-PT1H` or `+PT6H`); `POST /v1/sample` takes up to 100 points;
`GET /v1/products/{id}/series` returns up to 500 steps; `POST /v1/products/{id}/stats` returns min, max,
mean, p95, coverage and the area above thresholds over a geometry, place or bbox (at most 2,000,000 km²);
`GET /v1/places/{id}/weather` returns the place-weather record plus cached GFS, HRRR and (053) NWS series. None of
them ever causes an upstream fetch.

## Derived intelligence layers (prompt 099)

Prompt 099 adds the object-derived layers. Like the weather ones they make no upstream request:
they read the stored and live records of the aircraft, vessel and event sources through the
observation feed, and write an ordinary record only when its rounded content changes. Every layer is
installed **disabled**, is a STANDARD source with no TTL (the fused-aircraft layer is the one TRACK,
with a 5-minute TTL), and declares its inputs as source families, so the receiver slots you configure
(`local-adsb-*`, `ais-*`, `adsbfi-region-*`, …) are picked up without further setup.

| Source | Kind | Inputs | What it reports |
|---|---|---|---|
| `derived-gps-jamming` | `jamming-cell` | readsb-family aircraft (NACp/NIC; not OpenSky) | cells where ≥ 30% (medium) or ≥ 50% (high) of ≥ 3 airborne aircraft report a degraded solution |
| `derived-ais-gap` | `ais-gap` | AIS vessel sources | a vessel under way with no fix for 30 min, when ≥ 3 neighbours kept reporting nearby |
| `derived-loiter` | `loiter` | AIS vessel sources | a vessel holding within 0.5 nm of its centroid for ≥ 30 min |
| `derived-rendezvous` | `rendezvous` | AIS vessel sources | two slow vessels within 0.5 nm for ≥ 15 min, away from ports |
| `derived-deception` | `deception` | aircraft + vessel | duplicate MMSI, impossible vessel or aircraft speed |
| `derived-holding` | `holding-pattern` | aircraft | a slow aircraft turning through > 300° in 10 min in a level band |
| `derived-emergency-squawk` | `emergency-squawk` | aircraft | a 7500/7600/7700 squawk or a readsb emergency, confirmed twice |
| `derived-chokepoint` | `chokepoint-activity`, `chokepoint-uturn` | AIS vessel + PortWatch chokepoints | hourly banded transits, U-turns and stops per chokepoint |
| `derived-mil-naval` | `mil-naval-cooccurrence` | aircraft + vessel | military aircraft and naval vessels in one res-3 cell within 30 min |
| `derived-aircraft` | `aircraft` | every aircraft source | the freshest observation per hex, fused |
| `derived-corroboration` | `corroboration` | earthquakes, fires, conflict, outages | one record per cross-source event cluster with an Admiralty credibility |
| `derived-port-calls` | `port-call` | AIS vessel + World Port Index | arrivals and departures at port buffers |
| `derived-cpa` | `cpa` | AIS vessel (area) | vessel pairs with a close-quarters approach inside an area |
| `derived-felt-locally` | `felt-report-estimate` | earthquakes + places | estimated intensity at each place, by ShakeMap or IPE |
| `derived-cable-risk` | `cable-proximity-risk` | AIS vessel + submarine cables | slow or anchored vessels near cables outside ports |
| `derived-dark-vessel-match` | `dark-vessel-candidate` | AIS gaps + SAR detections | gaps joined to unmatched SAR detections (candidates only) |
| `derived-lightning-holdover` | `holdover-fire-risk` | GLM + FIRMS/FDC + QPE | flash clusters over dry cells, watched for a later fire |
| `derived-hf-blackout` | `hf-propagation-anomaly` | PSKReporter + SWPC X-ray | spot/SNR drops on sunlit paths against a 14-day baseline |

Enable the inputs you want first, then the layers, for example
`PUT /v1/sources/derived-gps-jamming {"enabled": true}`. Each layer shows in
`GET /v1/sources/{id}/health` like any other and writes one `fetch_log` row with `principal: derived`
per invocation. Its tick is one minute by default (`derived-gps-jamming` five, others as noted in the
catalog).

**Absence of evidence is not evidence of absence.** Every layer is coverage-aware. An input that is
not `healthy`, or a window touched by a change-bus gap or a live drop, marks the record
`status: degraded-input` and suppresses absence-based events (an AIS gap, a cleared squawk, a port
departure). `derived-ais-gap` additionally requires three other vessels reported by the same source
within 50 km during the gap. A record is tombstoned only when its condition is genuinely gone, never
because input is stale; an uncertain record keeps its last values with `insufficient-data`.

**Rule recipes** (all `enter`/`exit` or an explicit cooldown, never a bare `match`):

```json
{"name": "GPS jamming cell goes high", "sources": ["derived-gps-jamming"], "kinds": ["jamming-cell"],
 "conditions": [{"path": "level", "op": "==", "value": "high"}], "mode": "enter", "severity": "warning",
 "messageTemplate": "High-rate GPS jamming in {{payload.cell}} (automated detection, not a warning)"}
```

```json
{"name": "Emergency squawk", "sources": ["derived-emergency-squawk"], "kinds": ["emergency-squawk"],
 "conditions": [{"path": "squawk", "op": "==", "value": "7700"}], "mode": "enter", "severity": "critical",
 "cooldown": "PT30M", "messageTemplate": "Aircraft {{title}} squawking 7700 (unverified)"}
```

```json
{"name": "AIS gap near me", "sources": ["derived-ais-gap"], "kinds": ["ais-gap"],
 "near": {"placeId": "home", "radiusKm": 100},
 "conditions": [{"path": "open", "op": "==", "value": true}], "mode": "enter", "severity": "warning",
 "cooldown": "PT1H", "messageTemplate": "Vessel {{title}} lost AIS inside 100 km of home"}
```

## Alert weather context

Prompt 056 gives every geolocated alert a deterministic weather block, `context.weather`, computed when the
alert fires and stored with it **before** it is dispatched, so the `alert.fired` event, every channel and the
enrichment see the same facts. It is computed from stored frames, the GLM flash ring and current records only
(never an upstream fetch) within a 500 ms budget; a part that misses the budget is `unavailable`, and a failure
stores `{"status": "unavailable"}` without delaying or blocking delivery.

| Section | What it holds |
|---|---|
| `radar` | MRMS reflectivity maximum within 10 km of the record's geometry (`maxDbz`), and the nearest `radar-cell` within 100 km (`nearestCell`: `id`, `distanceKm`, `motion {speedKmh, bearingDeg, method}`, `meshMaxMm`) |
| `lightning` | GOES GLM flashes within 20 km over the 15 minutes before the alert (`flashes20km15m`); `stale` when no GLM file ended within 2 minutes |
| `precip` | MRMS rain rate at the geometry (`rateMmH`), plus the 1-hour QPE (`qpe1hMm`) when `mrms-qpe-1h` is current |
| `warnings` | up to five active NWS alerts intersecting the geometry (`id`, `event`, `severity`, `expires`), each `untrusted: true` because its text is upstream data |
| `wind` | 10-m wind and gust of the newest HRRR run (else GFS) at the step nearest the alert (`productId`, `speedMs`, `gustMs`, `fromDeg`, `run`, `validTime`, `step`) |
| `smoke` | HRRR near-surface smoke at the nearest step (`pm25UgM3`, `run`, `validTime`, `step`) |
| `summary` | one sentence built from the sections, for example "Radar max 52 dBZ within 10 km; cell 8 km away moving ENE at 45 km/h; 14 GLM flashes within 20 km (15 min); NWS Severe Thunderstorm Warning in effect. Automated — not an official warning." |

Every section has `status` `ok`, `stale`, `no-coverage` or `unavailable`. **Unknown is not clear:** a non-ok
section carries `statusText` (and `asOf`, the time of its last good data, when known) and no values, and the
summary words it as unknown ("lightning data stale since 14:30 UTC", "radar data has no coverage here (not
evidence of clear weather)", "smoke data unavailable"), never as absence. A product whose source is disabled
gives an `unavailable` section, not a missing one. GLM's footprint over Texas is about 8–10 km and its detection
efficiency is below 100%, so a zero flash count is advisory.

- **Switching it off.** `PUT /v1/settings/weather` (admin) `{"alertContext": "off"}` turns it off everywhere;
  a rule's `"weatherContext": "off"` turns it off for that rule (default `auto`). It also stays off while no
  weather input (radar, rain, model or smoke product, GLM or NWS alerts) is enabled.
- **The same settings** hold `defaultAoi` (the area used while no places exist, 047) and `aoiBufferKm` (the
  minimum buffer around each place, 036); changing either recomputes the places area of interest. Until you
  store a value they come from the configuration properties `foresignal.weather.default-aoi` and
  `foresignal.weather.aoi-buffer-km`.
- **Reading it.** `GET /v1/alerts/{id}` returns the whole `context`; `GET /v1/alerts` lists carry only
  `context.weather.summary` unless you add `include=context`. Webhook bodies carry it in `data.context`.
- **Templates.** Rule templates and channel `titleTemplate`/`bodyTemplate` can use
  `{{context.weather.summary}}` and every field by its path: `{{context.weather.radar.maxDbz}}`,
  `{{context.weather.radar.nearestCell.motion.speedKmh}}`, `{{context.weather.lightning.flashes20km15m}}`,
  `{{context.weather.lightning.status}}`, `{{context.weather.wind.gustMs}}`,
  `{{context.weather.warnings.items.0.event}}` … A missing context or field prints nothing. The default body
  of `discord`, `telegram`, `email`, `ntfy`, `pushover` and `slack` appends the summary line when there is one,
  and the stored notification keeps it with the message.
- **Enrichment** receives the block as the labeled data section `alert.context.weather`, inside the untrusted
  data envelope, and is told that a section other than `ok` is unknown, not clear.

```json
POST /v1/triggers
{"name": "Storm cell near the farm", "sources": ["wx-cells"], "near": {"placeIds": ["farm"], "radiusKm": 30},
 "mode": "enter", "conditions": [{"path": "maxDbz", "op": ">=", "value": 55}],
 "messageTemplate": "{{payload.maxDbz}} dBZ cell near the farm. {{context.weather.summary}}"}
```

**Weather at places in briefings.** A briefing scope with `places` (and optionally `weatherProducts`, by default
the products behind place weather) adds a "Weather at places" table per place, in the place's timezone: **now**
(radar and lightning from the place's `place-weather` record, active NWS alerts), **the period** (maximum dBZ and
MESH, the GLM flash total within the place radius from the 14-day flash archive, and hazards that entered the
area; "partial period" when the window reaches past retention) and **the next 24–72 h** (the NWS gridpoint summary
and the GFS and HRRR maxima, each with its run and valid time). Stale or unavailable parts are labeled
("Lightning: data stale since 14:30"), never left out or shown as calm. `GET /v1/digest?places=home,farm` adds the
same section as `weatherAtPlaces` (it needs a token). The place-weather rule recipes are in
[Weather alerts](#weather-alerts).

## SatDump ground station

Prompt 049 makes your own [SatDump](https://github.com/SatDump/SatDump) station a source. Set
`FORESIGNAL_SATDUMP_STATIONS` (property `foresignal.satdump.stations`) to a comma list of station names,
for example `home`, and restart: each name becomes a `satdump-{station}` source, installed disabled like
every source. A name is kebab-case and at most 56 characters.

SatDump is GPL-3.0 and stays a separate program that you install and run. Foresignal talks to it only
through HTTP (`/api`, `/polarplot.jpeg`) and the files it writes; no SatDump code is in Foresignal, and the
Foresignal image contains no SatDump binaries or SatDump-produced images.

### Station requirements (§4.0)

- **`/api` needs the headless legacy CLI.** Only `satdump legacy live …` and `satdump legacy autotrack …`
  serve it. In `live` mode pass `--http_server 0.0.0.0:8081` on the command line; in `autotrack` mode the
  web server and the other options come from the config file:
  `satdump legacy autotrack autotrack.json` with `"http_server": "0.0.0.0:8081"` in it. A station run from
  the GUI works in **products-only mode** (inbox only, no RF statistics).
- **`finish_processing: true`, and no server mode.** SatDump runs the offline pipeline (and writes
  `dataset.json`, the products and the composites) only when `finish_processing` is true and no
  `server_address`/`server_port` is set. Without it a pass is recorded but never decoded, and the source
  shows "Pass ended {t} but no dataset.json after 20 min. Is `finish_processing: true` set, and server_mode
  off?".
- **Pin a SatDump version.** The `/api` shape and the `product.cbor` keys differ between versions (1.2.2
  and 2.0.0-alpha differ). Foresignal keeps one fixture set per pinned version; a response that no longer
  matches shows "SatDump /api shape changed (version drift?)".
- Foresignal never requests `/fft.jpeg` (requesting it turns SatDump's FFT on) or `/schedule.jpeg`.

### Parameters

| Param | Type | Default | Meaning |
|---|---|---|---|
| `apiUrl` | LAN URL | — | SatDump's web server, for example `http://192.168.1.60:8081`. Local addresses only; resolved once and pinned. |
| `inboxPath` | inbox path | — | SatDump's output directory under `FORESIGNAL_INBOX_DIR`, for example `satdump/home`. |
| `location` | string | required | `lat,lon[,altM]` or a place id. |
| `satNames` | JSON object | — | Name or NORAD number to `satKey`, overriding the built-in table (`src/main/resources/satdump/sat-names.json`). |
| `apiInterval` | duration | `5s` | 2 s to 30 s. |
| `scanInterval` | duration | `30s` | 5 s to 10 min. Each full scan costs one budget unit. |
| `polarGlob` | glob | `**/*.tif` | Which files of a ready polar pass folder are read as GeoTIFF overlays, relative to the pass folder (`**` crosses directories, `*` and `?` do not; case is ignored). |

Credential (optional, `PUT /v1/sources/satdump-home/credentials`): `ingestKey`, a key you choose
(`eik_` followed by at least 32 random characters) that a remote station presents on the push endpoints
below. Without it only an admin token may push.

At least one of `apiUrl` and `inboxPath` is required (`400 VALIDATION_FAILED` otherwise). The budget is
60 a minute and 50,000 a day; every request stays on your LAN.

```json
PUT /v1/sources/satdump-home
{"enabled": true, "params": {"apiUrl": "http://192.168.1.60:8081", "inboxPath": "satdump/home", "location": "home"}}
```

### What it produces

- **`ground-station`** (`externalId` = the station name, kept 30 days): `state`
  (`idle | tracking | receiving | locked | unknown`), `currentTarget`, `frequencyHz`, `az`, `el`, `snrDb`,
  `nextAos`, `nextLos`, `rotatorEngaged`, `satdumpMode`. Azimuth and elevation are rounded to 1° and SNR to
  1 dB, so a version is written only when something changes. Without `apiUrl` there is one record in state
  `unknown`.
- **`satellite-pass`** (kept 365 days): `receiving` at AOS, `recorded` at LOS with the RF statistics
  (`rf.snrMaxDb`, `snrMeanDb`, `berMin`, `berMean`, `syncedFraction`, lock times and elevations, and an
  SNR-against-elevation series), then `decoded` when the pass folder's products arrive, or `failed` when
  none arrived 20 minutes after LOS although the station had lock. Each composite is stored as media with
  256 and 1024 px thumbnails (`products[].thumbMediaId`). The id is `{station}:{satKey}:{aosEpochMinute}`.
- **Geostationary frames**: GOES-19/18 HRIT and Elektro-L xRIT folders become frames of dynamic products
  `satdump-{station}-{satKey}-{region}-{channel}` (region `fd` when SatDump writes none), kept 72 hours and
  rendered through the normal tile path. A frame is `partial` as soon as one channel arrives and becomes
  complete after 120 s without changes or when the next timestamp folder appears. GK-2A needs KMA keys
  obtained legitimately. FY-4 and MSG HRV are refused ("FY-4/MSG HRV has no usable projection.").
- **Live polar plot** during an active autotrack pass: `GET /v1/ground-stations/satdump-home/polarplot.jpeg`
  (read scope; `404 NO_ACTIVE_PASS` otherwise). Fetched every 15 s by the source, held in memory only,
  never fetched because a client asked.

- **Polar overlays** (prompt 054): every file matching `polarGlob` in a ready pass folder is read as a
  GeoTIFF map overlay; see the next section.

The original images and the provenance files (`dataset.json`, `product.cbor`, any `.tif`) are copied to
`${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/{passId}/` and deleted after 30 days. `.cadu` and baseband
files are never copied; SatDump's own retention keeps them.

**Health.** The source is `failing` when `apiUrl` is set and `/api` has been unreachable for 3 polls in a
row, and `degraded` for the reasons above and the overlay reasons below. With no `apiUrl` it stays healthy
on the inbox loop alone.

### Polar overlays and footprints (prompt 054)

Foresignal only **reads** GeoTIFFs that SatDump wrote into a pass folder; it never reimplements SatDump's
projection, and no SatDump code is involved (SatDump's source was read for file-format facts only).

**The one producer configuration.** No SatDump version has been confirmed for your station yet
(Weather-Imagery-Design §1.5, question 1), so the documented producer is the default the design names:
**SatDump 1.2.2**, with a `project` block on each composite you want as an overlay, in the pipeline's
composite settings:

```json
"project": {"config": {"type": "equirec", "auto": true}, "img_format": ".tif", "draw_map_overlay": false}
```

SatDump 1.2.2 then writes `rgb_{composite}_projected.tif` next to the composite: an EPSG:4326 GeoTIFF
(one tie point, a pixel scale of about 0.016°, `GeographicTypeGeoKey` 4326). `"auto": true` is needed:
without it, or an explicit `width` and `height`, 1.2.2 logs "No width or height defined for projection!"
and writes nothing. **SatDump 2.0-alpha ignores a preset's `project` keys**, so a 2.0 station gets
thumbnails and approximate swaths only, until the 2.0 hook of prompt 107.

**What happens to each GeoTIFF:**

1. The header is checked first: a BigTIFF, more than 150 megapixels or a side over 12,000 px is refused
   before any pixel is decoded.
2. Only EPSG:4326 and EPSG:3857 are read (PixelIsArea and PixelIsPoint both; a rotated
   ModelTransformation is refused); 8-bit RGB/RGBA and 16-bit grayscale. A file without alpha is
   transparent where every band is zero (or equals `GDAL_NODATA`).
3. It becomes one frame of the dynamic product `satdump-{station}-{composite}` (the file stem without
   `_projected`): `rgba8`, `temporal.type` `pass`, coverage `swath`, style `rgb@1`, kept 30 days, tiled
   like every other product. Processing the same pass again replaces its frame.
4. Its footprint is the outline of the non-transparent pixels, simplified to about 0.02° and split at
   ±180°. The `satellite-pass` record's geometry becomes that footprint (`geometrySource: footprint`),
   and its `products[]` entry gains `productId`, `frameKey` and `tilesTemplate`.

**Approximate swath.** A pass without a usable GeoTIFF (a 2.0 station, a refused file, a failed decode) gets
an approximate swath: the SGP4 ground track (Orekit) from the TLE in `product.cbor`, between the first and
last product timestamps, widened by half the swath on each side (`corr_swath` from the product, else
MSU-MR 2,800 km, AVHRR 2,900 km, MHS 2,200 km). It is a geometry only (`geometrySource: swath`), never
tiles, and never replaces a footprint. Without a TLE the pass keeps the station point.

**Health reasons** (the source shows `degraded` until a later pass is clean):

| Reason | Fix |
|---|---|
| `GeoTIFF CRS EPSG:{n} not supported (build with WITH_GDAL=true)` | Use `"type": "equirec"` (or WebMercator) in the composite's `project`, or build with GDAL |
| `GeoTIFF ModelTransformation is rotated or sheared (not supported; build with WITH_GDAL=true)` | As above |
| `BigTIFF not supported.` | Keep overlays under 4 GB, or build with GDAL |
| `GeoTIFF … px has a side over the 12000 px limit` / `… over the 150 megapixel limit` | Raise `scale_x`/`scale_y` in the projection |

**Optional GDAL.** Build the image with `docker compose build --build-arg WITH_GDAL=true` (or
`WITH_GDAL=true docker compose build`): it installs `gdal-bin` only (no JNI, no AGPL drivers). Foresignal
detects `gdalwarp --version` at startup (`FORESIGNAL_RASTER_GDAL_ENABLED`, default `auto`; `false` turns it
off) and warps refused GeoTIFFs (another CRS, a rotation, BigTIFF) with
`gdalwarp -t_srs EPSG:4326 -of GTiff -co COMPRESS=DEFLATE`, a 120-second timeout and `GDAL_CACHEMAX=256`,
on the raster decode pool, with its files only under `rasters/tmp/`.

### Same host or remote Pi (§4.7)

- **Same host:** `docker compose --profile satdump up -d` runs a `satdump` service with
  `network_mode: host`, `/dev/bus/usb` passed through and the output volume `satdump-output`, which the
  broker mounts read-only at `/data/inbox/satdump/${SATDUMP_STATION:-home}`. Build the image yourself from
  SatDump's own Dockerfile and pin the commit:
  `git clone https://github.com/SatDump/SatDump && git -C SatDump checkout <commit> && docker build -t satdump:local SatDump`
  (or set `SATDUMP_IMAGE`). Write `docker/satdump/autotrack.json` with `"http_server": "0.0.0.0:8081"`,
  `"finish_processing": true` and `"output_folder": "/srv/output"`.
- **Remote Pi at the antenna (recommended in v1):** run SatDump on the Pi and copy its output directory into
  `${FORESIGNAL_INBOX_DIR}/satdump/{station}` with rsync or Syncthing; the 30-second rescan picks it up
  (inotify is not trusted, so a sync tool that renames into place works). Point `apiUrl` at the Pi if it
  is on your LAN.
- **Remote Pi that pushes (prompt 054):** when a sync tool is not an option, the Pi can push with the
  source's `ingestKey` credential (`X-Foresignal-Ingest-Key`, `Authorization: Bearer` or Basic) or an
  admin token:
  - `POST /v1/ingest/satdump-home/rasters`: a whole pass folder as `multipart/form-data`, a `meta` part
    `{"passFolder": "…", "files": ["dataset.json", "product.cbor", "…png", "…tif"], "product": "MSU-MR"}`
    and one `files` part per file. Each file at most 25 MB, the bundle at most 200 MB (`413
    MEDIA_TOO_LARGE`); file and folder names are single path segments. `dataset.json` goes to the pass
    folder and the rest to the `product` directory (default: the single product `dataset.json` lists).
    The bundle is written atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/home/incoming/{uploadId}/`
    and processed by the next inbox scan exactly like a pass folder; it is deleted after 2 days. Send one
    bundle per product for a multi-instrument pass. Answers `202 {uploadId, passFolder}`.
  - `POST /v1/ingest/satdump-home/media`: one image (`file`, at most 25 MB) and
    `meta {"composite", "timestamp", "satellite", "passId"?}`; it is checked by the image guard, stored
    with thumbnails and attached to the matching pass (or a new one). Answers
    `202 {mediaId, sha256, passId}`; `404` while the source's stream is not running.

  Each push costs one unit of the source's budget (`429 RATE_LIMITED` with `Retry-After` when spent).
  Other sources answer both paths with `404`.

  ```sh
  curl -H "X-Foresignal-Ingest-Key: $KEY" -F 'meta={"passFolder":"2026-10-12_18-02_meteor","files":["dataset.json","product.cbor","rgb_MSU-MR_221_Composite_projected.tif"],"product":"MSU-MR"};type=application/json' \
       -F files=@dataset.json -F files=@MSU-MR/product.cbor -F files=@MSU-MR/rgb_MSU-MR_221_Composite_projected.tif \
       https://foresignal.example/v1/ingest/satdump-home/rasters
  ```

**Do not use SatDump's webhook plugin** as an ingest path: it posts plain text only.

### Texas targets

| Target | Frequency | Hardware |
|---|---|---|
| GOES-19 HRIT/EMWIN | 1694.1 MHz | about 1 m dish, LNA, LTE filter |
| Meteor-M2-3 / M2-4 LRPT | 137.9 / 137.1 MHz | QFH or V-dipole |
| Metop-B/C AHRPT (optional) | 1701.3 MHz | tracking dish |

Drop `noaa_apt`, `noaa_dsb` and `noaa_hrpt` from SatDump's defaults: NOAA-15, -18 and -19 were
decommissioned in 2025.

### Open owner questions (Weather-Imagery-Design §1.5) and the defaults used

| Question | Default until answered |
|---|---|
| SatDump version and mode at the station | 2.0.0-alpha headless `legacy autotrack` for `/api` and the inbox; fixtures are hand-written from that version's observed keys until a capture replaces them. The polar overlay producer is documented for 1.2.2 (054, the design's default when unanswered); a 2.0 station gets approximate swaths only |
| Bench server or remote Pi | Both work; the remote Pi with rsync/Syncthing is the documented default |
| L-band dish for GOES HRIT, 137 MHz antenna | Both decoders are built; whatever arrives is used |
| 32 GB or 64 GB host | `small` profile |
| Clients outside the LAN | Tile keys and the `web` profile as documented; nothing SatDump-specific |
| Home location and places | `location` names a place when one exists, otherwise coordinates |

### SatDump extras (prompt 107)

**DCS.** Enable `parse_dcs` in SatDump (GOES-R HRIT pipeline) and, at the source, set `tracked_addresses` to
cut volume. Then set `dcsEnabled` on the `satdump-{station}` source and a `dcsBbox` (or configure places, whose
AOI is the default; the built-in default box in force without places does not count): **DCS cannot be enabled
without an effective bbox**, and if places are later removed, no DCS message is processed and health says
so. Put a `dcp_locations.csv`
(`address,lat,lon,name,agency`) at `${FORESIGNAL_INBOX_DIR}/dcs/dcp_locations.csv`; it is re-read on change.
Messages with no location, outside the bbox or not allowlisted are dropped and counted. Over `dcsMaxPerScan`
(500) the newest per address are kept and health degrades with "DCS volume over cap" — tighten `dcsBbox` or
`dcsAddresses`. DCS is hemisphere-wide from a GOES antenna; expect high volume without a filter.

DCS and EMWIN files are read **as they settle**, on every inbox scan, wherever `dcsGlob`/`emwinGlob` match
below `inboxPath` (not only in finished pass folders), so continuous HRIT reception delivers warnings within
about two scan intervals. A file is read once it is unchanged since the previous scan, and once per size and
modification time; `IMAGES` trees are not searched.

**EMWIN.** Enable `emwinEnabled`; products in `emwinProducts` (AWIPS categories) become `emwin-product`
records and P-VTEC warnings with a polygon become `emwin-warning`, filtered to the places AOI. While
`nws-alerts` is healthy the warnings carry `duplicateOf`; when it is failing they do not, and this rule fires:

```json
{"name": "Warnings via satellite (internet down)", "source": "satdump-home", "kind": "emwin-warning",
 "conditions": [{"path": "duplicateOf", "op": "exists", "value": false}],
 "near": {"placeId": "home", "radiusKm": 100}, "mode": "enter", "severity": "warning"}
```

EMWIN is never more authoritative than NWS; messages render "Received via GOES HRIT EMWIN".

Products are applied in issue-time order (the WMO heading's time), and an older product never overrides a
newer one of the same warning, so a late copy of a `NEW` cannot revive a canceled warning. An
`emwin-product` id is `{wmoHeading}:{awipsId}:{issue time}` and its `observedAt` is the issue time.

**Local GLM (GRB).** A GOES GRB station (1686.6 MHz; a larger dish than HRIT) writes L2 LCFA NetCDF files;
set `FORESIGNAL_GLM_LOCAL_STATIONS` to the station names (use the same names as `FORESIGNAL_SATDUMP_STATIONS`)
and point `inboxPath` at the GLM output. Local flashes join the shared AOI ring and de-duplicate against the
internet `goes-glm` in both directions (whichever copy of a flash arrives second is dropped, within 1 s and
0.05°, inside the ring's scope); `lightning-cluster` records keep flowing from local flashes while the
internet feed is down, and the local clusters end as soon as it recovers. A GLM file is read once it has
been unmodified for 10 s; a young file that does not parse is retried. Health degrades if no file arrives for
5 minutes during a pass.

**Elements feed (TLE).** For SatDump, Gpredict and Look4Sat, point the TLE update at Foresignal:
`/compat/celestrak/NORAD/elements/{group}.txt` (groups `weather`, `noaa`, `goes`, `resource`, `amateur`,
`cubesat`, `active`, `stations`, …) or `/compat/celestrak/gp.php?GROUP=…&FORMAT=tle`. It is **served from the
cache only** (never fetched on request); `X-Foresignal-Elements-Age` gives the age and a `Warning` appears
past 3 days. Clients that cannot send headers use a feed key as `?key=fsf_…` (`POST /v1/tile-keys` with the
prefix `/compat/celestrak/`), or enable `openReads`.

**SatDump 2.0 hook.** If the station runs SatDump 2.0 (alpha only as of 2026-10; 1.2.2 is stable), set
`satdumpVersion` to `2.0` and install `contrib/satdump/foresignal_hook.as` (MIT, separate, experimental). It
writes equirectangular composites and a `foresignal-manifest.json` that Foresignal recognises
version-agnostically: listed GeoTIFFs go through the overlay pipeline, and listed PNGs through the image
guard, georeferenced from the product's `bounds [w,s,e,n]` (`projection: equirec`). An unknown manifest
schema, or a PNG without valid bounds, is rejected with a health reason.

## Space sources (prompt 064)

All five start disabled. Enable them with `PUT /v1/sources/{id}`.

**`celestrak-gp` (element sets).** CelesTrak's usage policy of 2026-05-22 is strict, and Foresignal
follows it:
- It fetches `GROUP=active` as CSV every 2 h (the policy's own check interval), one request at a time.
  It then fetches at most two `memberGroups` whose membership is a day old, so each group about once a day.
- A repeat inside one CelesTrak update gets a 403 "GP data has not updated since your last successful
  download". That counts as not modified.
- **Any other status, a redirect, or a body that is not CSV/JSON stops the source for 24 hours.** Its
  health turns `failing` with "CelesTrak returned {status}; automatic retries stopped per CelesTrak usage
  policy — investigate, then use fetch-now", and a `system.source_halted` notification goes to every
  channel subscribed to `system`. More than 50 such errors in 2 h get an address firewalled.
- Find out why (the fetch log and `GET /v1/sources/celestrak-gp/health` show the status), then
  `POST /v1/sources/celestrak-gp/fetch`. A manual fetch releases the hold; an ordinary backoff still
  answers `409 IN_BACKOFF`.
- More than 60 MB in a day turns health `degraded` (CelesTrak may block above 100 MB a day).
- `celestrak-socrates` (about 23 MB, refreshed every 10–11 h) follows the same rules and the same
  CelesTrak error budget. Leave it disabled if you do not need conjunctions.

**Feeding Gpredict, Look4Sat and SatDump.** Point clients at Foresignal instead of CelesTrak:
`/compat/celestrak/gp.php` (or `/compat/celestrak/NORAD/elements/gp.php`) serves the stored element
sets in CelesTrak's formats. It never calls CelesTrak. It needs `read` (open while `security.openReads`
is on). For a client that cannot send headers, create a feed key whose prefixes include
`/compat/celestrak/` and whose restriction names the source, and append it as `&key=`:

```http
POST /v1/feed-keys
{"name": "Gpredict", "prefixes": ["/compat/celestrak/"], "restrictions": {"sources": ["celestrak-gp"]}}
```

| Client | URL |
|---|---|
| Gpredict | `https://foresignal.example/compat/celestrak/gp.php?GROUP=stations&FORMAT=tle&key=fsf_…` (one URL per group) |
| Look4Sat | `https://foresignal.example/compat/celestrak/gp.php?GROUP=active&FORMAT=csv&key=fsf_…` |
| SatDump | `kepler_settings.urls_to_fetch`: `http://foresignal.lan:8080/compat/celestrak/gp.php?GROUP=active&FORMAT=omm` |

- Unknown `FORMAT` values (SatDump's `omm`) get CSV, as at CelesTrak. That is what SatDump needs: its
  OMM reader (`ccsdsOmmToKepler`, checked in the SatDump tree of 2026-10-12) splits each line into the
  17 CSV columns. `FORMAT=json` does **not** load in SatDump. SatDump's default source is orbicentral
  (`http://orbicentral.net/gp.php?GROUP=active&FORMAT=omm`).
- TLE formats leave out objects numbered 100000 and above (CelesTrak does not use alpha-5); use CSV or
  JSON to get them.
- `X-Foresignal-Elements-Age` gives the age of the newest element set in seconds. A `Warning` header
  appears when it is over 3 days old, meaning `celestrak-gp` has stopped.

**`satellite-passes`.** This source predicts passes of `norad` (default ISS 25544, METEOR-M2 3 57166 and
METEOR-M2 4 59051) over every non-sensitive point place for 48 h, with Orekit, from the element sets.
It needs `celestrak-gp` enabled.
- The observer height is the place's `altitudeM` attribute, else 0 m.
- A sensitive place is used only when you name it in `places`. Mark the source private if its passes
  should not reach read tokens.
- A SatDump station predicts its own `predictNorad` satellites (default 57166 and 59051, at
  `predictMinElevationDeg` 10). Its `predicted` versions become `receiving`/`recorded`/`decoded` under the
  same id when a session starts within 5 minutes of the predicted AOS. A predicted pass with no session
  by LOS + 20 minutes becomes `missed` while `/api` answered throughout the pass, and is removed otherwise.
- At startup a self-test predicts a bundled ISS pass. If Orekit's output drifts, the log says "Orekit
  self-test FAILED".

**`launch-library`.** It works anonymously: 15 requests an hour, polled every 15 minutes. With a Launch
Library 2 token (`PUT /v1/sources/launch-library/credentials {"values": {"token": "…"}}`) you may lower
the interval to 5 minutes. Do not lower it without a token: the upstream answers 429, and Foresignal
backs off 30 minutes.

**`spacetrack-tip`.** It needs your own Space-Track account (`identity` and `password`). The User
Agreement allows one account per person or entity, and the credentials must never be shared. The data
may not be passed to others without Space-Track's approval, so the source is `LOCAL_ONLY`: it is never
served to feeds, sinks, federation or share links. It queries the last hour's TIP messages hourly. Space-Track
limits TIP to once an hour, or every 10 minutes during a re-entry within 12 hours.

## Transit (prompt 070)

Foresignal ships seven reviewed GTFS-Realtime feeds (`docs/Source-Catalog.md`, "Reviewed transit feeds"): each has a
`transit-{id}` vehicle source and, where the operator publishes alerts, a `transit-alerts-{id}` source. All are
disabled until you enable them with `PUT /v1/sources/{id}`. Vehicles are TRACK records with a 5-minute TTL; keep the
interval at or below 30 s (the default), or shorten the keyframe of the ingest policy with it (225 s at 45 s), because
`keyframe ≤ ttl − interval − 30 s` must hold. The policy validator checks that only against the default interval, so it
will not stop a slower interval on its own. The alerts source takes a `language` parameter (default `en`)
that picks the translation of the alert texts. Entur requires an identifying header; Foresignal sends
`ET-Client-Name: foresignal-{installId}` automatically.

### Adding a transit feed from the Mobility Database

More keyless feeds can be enabled by id from the vendored Mobility Database snapshot
(`src/main/resources/transit/mobility-gtfs-rt.csv`; the snapshot date is in the README beside it). Foresignal never
accepts a transit URL at runtime, so the steps are:

1. **Search.** `GET /v1/transit/catalog?country=FR&q=bus&entityType=vp` (admin token) lists matching rows with their
   `id`, provider, URL, license URL and whether each is already `enabled`. `entityType` is `vp` (vehicle positions),
   `sa` (service alerts) or `tu` (trip updates, which Foresignal does not ingest).
2. **Check the license.** Open the row's `licenseUrl`. When it is empty the catalog does not know the license, and the
   source's license reads "see provider terms": read the provider's own terms before you enable the feed. Catalog feeds
   are egress OPEN (their records may be re-served on every path); the per-source license override arrives with
   prompt 109, so until then do not enable a feed whose terms forbid redistribution.
3. **Set the property.** Add the ids, comma-separated, to `foresignal.transit.catalog-feeds` (environment variable
   `FORESIGNAL_TRANSIT_CATALOG_FEEDS`), for example `FORESIGNAL_TRANSIT_CATALOG_FEEDS=mdb-1602,mdb-1603`.
4. **Restart.** The family reads the property only at startup: a `vp` row becomes `transit-mdb-{id}` and an `sa` row
   `transit-alerts-mdb-{id}` (a row serving both registers both). The search result's `sourceIds` names them. Each URL
   must pass the outbound URL policy (public addresses only); a refused URL, an id missing from the snapshot and a
   trip-updates-only row are skipped with a warning in the log.
5. **Enable** the new sources with `PUT /v1/sources/{id}` like any other.

Why a restart: the set of transit URLs is fixed when Foresignal starts, and each one came from code or from the
reviewed snapshot. An API that accepted a feed URL would let any admin-token holder point Foresignal at an arbitrary
host, which is the open-proxy hole the static feed lists exist to close. Catalog feeds use the header timestamp as
their time base (their vehicle timestamps are unknown), send no extra headers (an Entur sub-feed listed in the
catalog may therefore be refused; use `entur-norway` instead), and the snapshot is refreshed only when a developer
reruns `scripts/update-mobility-catalog.py`.

## Bikeshare (prompt 071)

Foresignal ships 30 reviewed docked bikeshare systems, each a `gbfs-{id}` source of `bikeshare-station` records. All
are disabled until you enable them with `PUT /v1/sources/{id}`:

| Operator family | Sources |
|---|---|
| Lyft-operated | `gbfs-nyc-citibike`, `gbfs-chicago-divvy`, `gbfs-dc-capital-bikeshare`, `gbfs-sf-bay-wheels` |
| Own hosts | `gbfs-boston-bluebikes`, `gbfs-portland-biketown`, `gbfs-buffalo-reddy` |
| publicbikesystem.net | `gbfs-austin-capmetro`, `gbfs-honolulu-biki`, `gbfs-chattanooga-bikechatt`, `gbfs-tucson-tugo`, `gbfs-fort-worth-trinity` |
| BCycle | `gbfs-philadelphia-indego`, `gbfs-la-metro-bike`, `gbfs-boulder-bcycle`, `gbfs-milwaukee-bublr`, `gbfs-madison-bcycle`, `gbfs-nashville-bcycle`, `gbfs-salt-lake-greenbike`, `gbfs-san-antonio-bcycle`, `gbfs-cincinnati-red-bike`, `gbfs-el-paso-bcycle`, `gbfs-indianapolis-pacers`, `gbfs-memphis-bcycle`, `gbfs-des-moines-bcycle`, `gbfs-omaha-heartland`, `gbfs-lincoln-bikelnk`, `gbfs-greenville-sc-bcycle`, `gbfs-las-vegas-rtc-bike-share`, `gbfs-santa-barbara-bcycle` |

Each station is one record: a new version is written when its service state changes (name, capacity, installed,
renting, returning, or `availability`: `out-of-service`, `empty`, `low` at 2 bikes or fewer, `full`, `ok`), while bike
and dock counts arrive as live updates. Rules on `availability` therefore fire on the transitions that matter without
storing a version for every bike taken. The default poll is 60 s; the source honors the feed's own `ttl` and does not
ask again while the last status is still valid. The `language` parameter (default `en`) picks the name of a GBFS 3.0
station where the operator publishes several. No reviewed feed declares a data license, so each source's license reads
"operator terms" and links to the operator's data page: read it before re-serving the data.

**Redirects are refused.** A system that starts answering with a redirect fails its runs with "redirect refused"
until the reviewed URL is updated in code; the redirect target is never requested.

### Proposing another system

Systems are added only by a code change to `GbfsSystems` (`src/main/java/ai/foresignal/source/adapters/gbfs/`),
reviewed like any other change; there is no property or API that adds a URL, and Foresignal never follows a feed's
`gbfs.json` to discover one. A proposal names the system's `station_information.json` and `station_status.json` URLs
(https, docked stations), the GBFS version, and the license from its `system_information.json` (`license_id` or
`license_url`, GBFS 2.3 and later) or, when it declares none, the operator's data terms page. The host allowlist is
derived from the list, so a new host needs nothing else. MobilityData's `systems.csv` is a good place to find
candidates.

## Operating the instance

### Probes and the compose healthcheck

| Endpoint | Includes | Use |
|---|---|---|
| `/actuator/health/liveness` | the application state only, never the database | restart decisions |
| `/actuator/health/readiness` | readiness, database, ingest writer (lock held, state loaded, clock in step), partition headroom | traffic and the compose healthcheck |
| `/actuator/health/catchup` | the catch-up signal (trigger lag over 10,000 seq, or the ingest queue above 80% for a minute) | dashboards; deliberately not part of readiness |

Details are never shown; `GET /v1/system` has them. The compose file checks readiness with
`curl -fsS http://localhost:8080/actuator/health/readiness` every 15 s after a 120 s start period.

### Watchdog and canary

A watchdog proves Foresignal is alive from somewhere else. Create a push check in Healthchecks.io
(or a push monitor in Uptime Kuma), then:

```json
PUT /v1/settings/watchdog
{"heartbeatUrl": "https://hc-ping.com/<uuid>", "failUrl": "https://hc-ping.com/<uuid>/fail",
 "interval": "60s", "method": "GET", "canary": {"enabled": true, "interval": "1h"}}
```

Every `interval` (at least 30 s) Foresignal pings `heartbeatUrl` while readiness is UP, and
`failUrl` (when set) while it is not. If the host dies the pings stop and the monitor alerts. Pings
time out after 5 s, never follow redirects, and their URLs are redacted in logs.

The opt-in **canary** submits one synthetic record to the built-in `system-canary` source every
`canary.interval` and requires its change on the change stream within 30 s
(`foresignal.canary.latency`). A failure sends a `system` notification and switches the watchdog to
`failUrl` until the canary passes again. `system-canary` keeps one day of history and never appears
in tiles.

### `/v1/system`

Anonymous callers get `version`, `apiVersion`, `startedAt`, `uptimeSeconds`, `status`
(`ok`, `degraded` or `down`), `features` and `limits`. A token of at least the `read` scope adds the
database, records, sources, scheduler, writer, streams, notifications, history per tier, catch-up
and `warnings`:

| Warning | Meaning |
|---|---|
| `OPEN_READS_EXPOSED` | Data reads need no token and `publicBaseUrl` or the published bind is not local (loopback, RFC 1918, 100.64/10 tailnets, link-local, unique-local or `.local`) |
| `OPEN_READS_ALL_INTERFACES` (info) | Data reads need no token and the port is published on `0.0.0.0` |
| `HISTORY_RATE_HIGH` | More than 20,000 versions per minute |
| `PARTITION_HEADROOM_LOW` | A tier has less than one partition block attached ahead |
| `RETENTION_CONSUMER_LAG` | The last retention run stopped waiting for a lagging consumer |
| `PUBLIC_BASE_URL_INSECURE` | `publicBaseUrl` is `http://` on a host that is not loopback, private, 100.64/10 or `.local` |
| `PUBLIC_BASE_URL_MISMATCH` | Requests arrive over HTTPS but `publicBaseUrl` is still `http://` |

Compose passes `FORESIGNAL_BIND` into the container so the warning knows the published bind.

### Logs

`FORESIGNAL_LOG_FORMAT` selects the console format: `ecs` (the default in the container), `gelf`,
`logstash`, or `text` (the default elsewhere). Lines logged while a request is served carry its
`traceId` and `spanId` from Micrometer Tracing (traces are never exported); an unexpected error's
`correlationId`, which the caller sees in the `500` body, is logged with its stack trace.

### Settings export and import

`GET /v1/settings/export` returns every setting (sources without credentials, retention, CORS,
notifications, the watchdog, `security`, `server`, trigger rules with their ids, channels without
secrets, LLM providers without their API keys, the LLM roles, and native push providers without their
keys). Tokens, tickets, pairing codes and push devices are never exported. An imported LLM provider
keeps a key it already has here; set the key of a new one with `PUT /v1/llm/providers/{id}`. An
imported push provider likewise keeps its key; a new one is imported disabled until you set its key
with `PUT /v1/push/providers/{providerId}` and enable it.
`PUT /v1/settings/import` applies such a bundle atomically: unknown sources are listed, a source
that needs credentials you have not stored is imported disabled with a warning, rules and channels
are upserted by id (channels keep their stored secrets), and any error rejects the whole bundle.
`POST /v1/system/admin-token/rotate` returns a new admin token once; the old one stops working at
once.

## Running the host

### Binds

Compose publishes the broker on `127.0.0.1:8080` and PostgreSQL on `127.0.0.1:5433`, so a fresh
install is reachable only from the host itself.

- **LAN access:** `FORESIGNAL_BIND=0.0.0.0 docker compose up -d`. Compose passes the bind into the
  container, and `GET /v1/system` then warns `OPEN_READS_ALL_INTERFACES` (and `OPEN_READS_EXPOSED`
  when the address is not private) while `openReads` is on. Read those warnings: open reads mean
  anyone who can reach the port can read every collected record.
- **The database** has its own `FORESIGNAL_DB_BIND`. Leave it on `127.0.0.1`; nothing outside the
  host needs PostgreSQL, and the compose password is a default.

### Remote access and TLS

**Recommended: Tailscale Serve.** It gives the broker a real HTTPS certificate on your tailnet name
(`https://foresignal.<tailnet>.ts.net`) without opening any port on your router.

- **Compose profile:** create an auth key in the Tailscale admin console, then
  `TS_AUTHKEY=tskey-auth-… docker compose --profile tailscale up -d`. The `tailscale` service (pinned
  `tailscale/tailscale:v1.102.5`) joins the tailnet as `foresignal`, keeps its identity in the
  `tailscale-state` volume, and loads `docker/tailscale/serve.json`, which proxies HTTPS 443 to
  `http://foresignal:8080` over the compose network. Funnel (public internet) is off. The broker's
  own port stays on `127.0.0.1`. Enable HTTPS certificates for your tailnet in the admin console
  first.
- **Or on the host:** with Tailscale installed on the host, `tailscale serve --bg 8080` does the same
  for the localhost port.

**Alternative: Caddy.** `docker/caddy/Caddyfile.example` reverse-proxies with automatic HTTPS (a
public name pointing at the host, ports 80 and 443 open) or `tls internal` for a LAN-only name. It
sets `flush_interval -1`, which SSE needs. Foresignal trusts its forwarded headers only when
`FORESIGNAL_TRUSTED_PROXIES` matches the proxy's address (see [Trusted proxies](#trusted-proxies)).

**For map clients: the `web` profile.** Map clients fetch many tiles at once and hold SSE streams open.
Over plain HTTP/1.1 a browser allows about six connections per host, so tiles queue behind each other
and every open stream costs a connection. The `web` profile puts Caddy in front of the broker with
HTTP/2 and HTTP/3 over TLS:

```sh
FORESIGNAL_HOSTNAME=foresignal.local \
FORESIGNAL_TRUSTED_PROXIES='172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3}' \
  docker compose --profile web up -d
```

- The `caddy` service (pinned `caddy:2.11.7-alpine`) publishes `80`, `443/tcp` and `443/udp` (HTTP/3)
  on every interface, and loads `deploy/caddy/Caddyfile`. Its state lives in the `caddy-data` and
  `caddy-config` volumes.
- `FORESIGNAL_HOSTNAME` is the site name (default `foresignal.local`); it must resolve to the host
  through mDNS, your DNS or the clients' hosts files.
- **Certificates:** `tls internal` by default: Caddy runs its own local certificate authority and
  issues the site certificate from it. Clients must trust that authority's root (below). For a
  publicly trusted certificate without opening port 80, use DNS-01: build a Caddy binary with your DNS
  provider's module (`xcaddy build --with github.com/caddy-dns/cloudflare`, or a Dockerfile `FROM
  caddy:2.11.7-builder-alpine` running that command), use that image, and replace `tls internal`
  with the commented `tls { dns … }` block in the Caddyfile. The stock image has no DNS modules.
- **Routes:** `/v1/*`, `/mcp*` and `/actuator/health*` go to the broker (with `flush_interval -1` for
  SSE); everything else is served from `./deploy/web` with a single-page-app fallback. Copy a separately
  built web client there: it is then same-origin with the API, so it makes no CORS preflights.
- **Compression:** zstd or gzip for JSON, vector tiles and text; PNG tiles and SSE are never
  re-encoded.
- **Trusted proxies:** Caddy reaches the broker from the compose network, so set
  `FORESIGNAL_TRUSTED_PROXIES` to that network as above (check it with
  `docker network inspect`; the compose network is named after the project, for example
  `foresignal_default`). Otherwise every client appears as Caddy's address in
  the audit log, the rate limits and the auth-failure backoff.
- Set `publicBaseUrl` to `https://<FORESIGNAL_HOSTNAME>` so feed-key and tile-key URLs, notification
  links and pairing codes use the HTTPS name.
- Plain `http://<host>:8080` keeps working for agents, scripts and tools.
- Caddy's access log (container stdout) keeps only the first eight characters of a path key and
  removes `?ticket=` values.

**Trusting Caddy's local CA.** Export the root certificate once:

```sh
docker compose cp caddy:/data/caddy/pki/authorities/local/root.crt ./foresignal-root.crt
```

Then install `foresignal-root.crt` on each client:

- **macOS:** open it in Keychain Access, add it to the System keychain, open the certificate, and set
  "When using this certificate" to **Always Trust** (or
  `sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain foresignal-root.crt`).
- **Windows:** `certutil -addstore -f Root foresignal-root.crt` from an administrator prompt, or
  double-click it and import it into **Trusted Root Certification Authorities** for the local machine.
- **Linux:** Debian and Ubuntu: copy it to `/usr/local/share/ca-certificates/foresignal-root.crt` and
  run `sudo update-ca-certificates`; Fedora: copy it to `/etc/pki/ca-trust/source/anchors/` and run
  `sudo update-ca-trust`. Firefox keeps its own store: import it under Settings → Privacy & Security →
  Certificates, or enable `security.enterprise_roots.enabled`.
- **Android:** Settings → Security → Encryption & credentials → Install a certificate → CA certificate.
  Browsers then trust it, but **apps targeting API level 24 or later do not trust user-installed CAs**
  unless their `network_security_config` allows them. A Flutter client that talks to the `web` profile
  must ship that configuration, for example a `res/xml/network_security_config.xml` with
  `<trust-anchors><certificates src="system"/><certificates src="user"/></trust-anchors>`, referenced
  from `AndroidManifest.xml` with `android:networkSecurityConfig`.
- **iOS and iPadOS:** send the file to the device (AirDrop, Mail or a download), install the profile under
  Settings → General → VPN & Device Management, then enable full trust under Settings → General →
  About → Certificate Trust Settings. Apps then trust it too.

Whichever you use:

- set the `server` setting's `publicBaseUrl` to the HTTPS name with `PUT /v1/settings/server`, so
  notification links and pairing QR codes work from a phone (an `http://` URL on a public host
  answers with `PUBLIC_BASE_URL_INSECURE`);
- **never expose port 5433**;
- plain HTTP port forwarding from the internet is **unsupported**: the token would cross the network
  in clear text.

### mDNS

`docker/avahi/foresignal.service` lets LAN clients discover the broker. On a host running Avahi:

```sh
sudo cp docker/avahi/foresignal.service /etc/avahi/services/
```

It advertises `_http._tcp` and `_foresignal._tcp` on port 8080 with exactly two TXT records,
`path=/v1` and `api=v1`. It deliberately carries **no install id or other stable identifier**:
anyone on the network can read mDNS, and a stable value would let the device be recognized and
tracked wherever it goes. Clients ask `GET /v1/system` once connected. mDNS only helps when the broker
is bound to the LAN (`FORESIGNAL_BIND=0.0.0.0`).

### Clock and NTP

Every version's `ingested_at` comes from the host clock, and asOf, retention and the change cursor
depend on it never going backwards. **Sync time before Foresignal starts:** use chrony or
systemd-timesyncd and check with `timedatectl` (`System clock synchronized: yes`).

- If the clock steps **backwards** past the tolerance, the writer **holds ingest** (readiness goes down and
  `GET /v1/system` `ingestWriter` says why) until the clock catches up with the last stored time; small steps are absorbed.
- A **forward** jump is only counted and logged; it never blocks, but versions written during it carry
  the wrong time.

### Storage projection

At full load (every planned source, Core-Data-Model §5.3 budgets), expect about **10–15 GB/day of
history writes plus 15–20 GB/day of WAL**. The TRACK window (3 days) holds about **40 million rows,
roughly 20 GB including indexes**. Size the disk for the STANDARD windows on top (1825 days for events).

- `GET /v1/system` (with the token) reports `history` per tier: partitions, rows, bytes, headroom,
  window, plus `versionsPerMinute` and `projectedBytes`; above 20,000 versions per minute it warns
  `HISTORY_RATE_HIGH`.
- Each source's health has `storage.projectedBytes` (its 7-day average bytes per day times its
  window) and `persistRatio`.

The compose database runs with the flags Core-Data-Model §16 sets for sustained writes:
`shared_buffers=512MB`, `max_wal_size=8GB`, `checkpoint_timeout=15min`, `wal_compression=lz4`,
`max_locks_per_transaction=256` (many partitions) and `autovacuum_vacuum_cost_limit=2000`. A
standalone PostgreSQL should use the same. The broker also sets `reWriteBatchedInserts=true` on its
JDBC connection.

### Backups

A complete backup is **two things**: a database dump and the secret key file.

```sh
docker compose exec -T db pg_dump -U foresignal -Fc foresignal > foresignal-$(date +%F).dump
docker compose cp foresignal:/data/secret.key ./secret.key   # FORESIGNAL_DATA_DIR in the container
```

- `pg_dump -Fc` dumps and restores the partitioned history like any other table.
- **Source credentials and channel secrets cannot be decrypted without `secret.key`.** A dump without
  it restores everything except those secrets, which you would have to enter again. Keep the key with
  the dump, but not next to it on the same disk, and treat it like a password.
- The admin token's hash is in the database, so a restored instance accepts the token of the
  instance it came from.

**Restore** (stop Foresignal first, so the writer is not running):

```sh
docker compose stop foresignal
docker compose exec -T db dropdb -U foresignal foresignal
docker compose exec -T db createdb -U foresignal foresignal
docker compose exec -T db pg_restore -U foresignal -d foresignal --no-owner < foresignal-2026-10-11.dump
docker compose cp ./secret.key foresignal:/data/secret.key
docker compose start foresignal
```

**Drill it.** Restore into a scratch database now and then and check it, without touching the live
one:

```sh
docker compose exec -T db createdb -U foresignal restore_drill
docker compose exec -T db pg_restore -U foresignal -d restore_drill --no-owner < foresignal-2026-10-11.dump
docker compose exec -T db psql -U foresignal -d restore_drill -c "select count(*) from record_head"
docker compose exec -T db dropdb -U foresignal restore_drill
```

### Troubleshooting

| Symptom | Look at |
|---|---|
| A source shows no data | `GET /v1/sources/{id}/health` (`status`, `statusReason`, `lastError`), then `GET /v1/sources/{id}/fetches?limit=5` (HTTP status, error, `versionsWritten`), then `POST /v1/sources/{id}/probe` to see the raw upstream answer |
| Many sources unhappy at once | `GET /v1/sources/health?status=failing,degraded` |
| Fetches are `REFUSED` | The budget is spent: raise `budget` or lengthen `interval` |
| The container restarts or is unhealthy | `/actuator/health/readiness` (database, ingest writer, partitions; details are in `GET /v1/system`); `docker compose logs foresignal` |
| `GET /v1/system` `ingestWriter` says the clock is behind | The host clock went backwards: fix NTP (above) |
| Alerts or streams lag | `/actuator/health/catchup` and `GET /v1/system` `catchUp` (trigger lag, ingest queue) |
| Disk filling | `GET /v1/system` `history` and `warnings`, each source's `storage.projectedBytes`, then the retention windows |
| A smoke test against the instance | `FORESIGNAL_TOKEN=fsr_… scripts/smoke.sh` (it enables `usgs-earthquakes`) |

## Humanitarian source credentials (prompt 090)

- **ReliefWeb (`reliefweb-disasters`)** needs an **approved appname**, not just any string. Request one from the
  form at <https://apidoc.reliefweb.int/parameters#appname>, then set it as the source's `appname` credential.
  ReliefWeb answers HTTP 403 "You are not using an approved appname" without one; Foresignal reports that as
  `APPNAME_NOT_APPROVED` and does not retry until the credential changes.
- **HDX HAPI (the optional `hdx-hapi-*` family)** is not built in prompt 090; it would need an `appName` and
  `email` sent as `app_identifier = base64("<appName>:<email>")` (requests without it get HTTP 403 "Invalid app
  identifier"). If the family is added later, set both credentials on each HAPI source.
- The other prompt 090 sources (`nasa-eonet`, `unhcr-population`, `who-don`, `copernicus-ems`,
  `usgs-hans-volcanoes`, `geonet-volcanoes`, `openfema-declarations`, `travel-advisory-us`) are keyless.

## Sanctions lists and the shrink guard (prompt 091)

- **SEC 8-K:** the SEC requires a declared User-Agent with contact details. Set the `contactEmail` credential on
  `sec-8k` (`PUT /v1/sources/sec-8k/credentials`); the shared client appends it to Foresignal's own User-Agent as
  `; contact=<email>`. Until it is set the fetch is refused (`credentials rejected`), unless the optional
  `foresignal.adapters.sec-8k.contact-email` property supplies a fallback. Never replace the User-Agent with
  another product's.
- **OFAC and UN redirects:** both export URLs answer 302 to a pre-signed object (S3 in `us-gov-west-1`, Azure Blob
  `unsolprodfiles.blob.core.windows.net`). Only those hosts are allowed. The whole query string of a pre-signed
  URL (AWS `X-Amz-Signature`/`X-Amz-Credential`, Azure `sig`) is replaced by `***` wherever a URL is stored or
  logged (fetch log, traces, error messages). The UN blob is change-checked by its ETag (a conditional GET); the
  OFAC export by its publication path and the SHA-256 of the file. Nothing to configure.
- **The delisting shrink guard (`LIST_SHRINK_GUARD`):** a sanctions list normally only grows a little between
  runs. If a run's entry count is below 90 % of the previous run's, Foresignal writes **no** tombstones, keeps the
  previous run's id set as the baseline, marks the source degraded with a reason starting `LIST_SHRINK_GUARD`
  (naming the list and both counts), and raises a `system.sanctions_list_shrink` notification, because a sudden
  large shrink is almost always an upstream fault (a bad export, a truncated download). Running the fetch again
  does not accept the smaller list: the guard keeps tripping against the kept baseline.
- **Accepting a genuinely large delisting:** verify the upstream list first, then run one manual fetch with the
  override: `POST /v1/sources/{sourceId}/fetch?allowShrink=true` (the same one-off override as the complete-set
  reconciliation below). That run writes the tombstones, stores the smaller list as the new baseline and clears
  the degraded reason; the next ordinary run re-applies the guard. **Note:** the override reaches the sanctions
  adapters through the fetch context, which the fetch executor must populate from the request; until it does, the
  override has no effect on these three sources (see the 091 completion report).
- **The UK source changed:** `uk-sanctions-list` (the FCDO UK Sanctions List) replaced `uk-ofsi-sanctions`,
  whose list closed on 28 January 2026.

## Internet-measurement sources (prompt 092)

- **Cloudflare Radar (`cloudflare-radar-outages`, `cloudflare-radar-bgp`):** create an API token at
  <https://dash.cloudflare.com/profile/api-tokens> with **Radar read** access and set it as the `apiToken`
  credential. Cloudflare's global limit is 1,200 requests per 5 minutes per user; Foresignal's budget is far
  below that and honours `Retry-After` on 429.
- **`gpsjam-daily` is disabled by default:** gpsjam.org publishes no licence, so the source is local-only and
  must be enabled deliberately.
- **RIPE Atlas:** `ripe-atlas-probes` never stores a probe's address or description; only status, prefixes,
  ASNs, country and tags. Licence: RIPE Atlas Service Terms v3.5.
- **Rule recipes:** BGP hijacks — enter on `bgp-hijack` when `confidence_score >= 8`; service status — enter on
  `service-status` when `indicator in [major, critical]`. Cloudflare outages pair with IODA (`ioda-outages`) in
  the 099 corroboration layer to distinguish a national outage from a cable cut or a hijack.
- **RIS Live (`ris-live`) is optional and disabled by default.** Set `prefixes` (at most 50) to the prefixes you
  watch; Foresignal subscribes to those (with their more-specifics) and never to the full firehose. Optional:
  `expectedOrigins` (`prefix=ASN,ASN;prefix2=ASN`) to flag an unexpected origin, and `hosts` (RRC names such as
  `rrc00`) to narrow the subscription to those collectors. While the stream is down, records say
  `status: no-data` rather than reporting zero visibility.
- **RIPE Atlas intraday:** to refresh disconnected probes between daily dumps, set `intradayDisconnected: true`
  and the source's interval to 30 minutes; the dump is still checked only every 6 hours.

## Reference datasets (prompt 094)

- **They poll weekly on their own.** `wri-power-plants`, `ourairports`, `submarine-cables`, `gvp-volcanoes`,
  `world-port-index`, `nps-boundaries`, `spaceports`, `nuclear-reactors-us` and `nuclear-plants-wikidata` are
  disabled by default like every built-in source; enable the ones you want. Each keeps 730 days of history and
  emits one `dataset-release` record per run.
- **`allowShrink`.** A reference dataset normally changes slowly, so if a complete run would remove more than
  `reconcile.maxAbsentRatio` of it (20 % by default), Foresignal writes no tombstones and shows the source
  degraded ("dataset shrank by N% — reconciliation skipped"): a truncated download must never read as a mass
  deletion. The skipped reconciliation also leaves a `DISCARDED` row in the fetch log, carrying the run's
  `runId` and the reason. After you have verified the smaller upstream dataset is genuine, release the guard
  **once** with `POST /v1/sources/{sourceId}/fetch?allowShrink=true`: that run re-reads every file in full
  (it sends no stored `ETag`/`Last-Modified`, so it cannot be answered with a 304) and reconciles. The
  degradation clears as soon as a reconciliation runs, after the override or after an ordinary complete run
  whose dataset is back within the ratio. The next ordinary run re-applies the guard.
- **Licence flags you should read before enabling:**
  - `submarine-cables` is **non-commercial**: the public Submarine Cable Map is CC BY-SA 4.0, the geometry is
    stylised and TeleGeography's underlying database is subscriber-only. Egress is local-only for prompts
    109/110 and the attribution carries `nonCommercial: true`. Display it, attribute TeleGeography, do not
    redistribute.
  - `gvp-volcanoes` uses the Smithsonian Terms of Use (**non-commercial; cite the VOTW DOI**). The attribution
    names Volcanoes of the World v5.4.0 (2026) and `doi:10.5479/si.GVP.VOTW5-2026.5.4`.
- **Keys:** none. `nuclear-plants-wikidata` sends Foresignal's own User-Agent with contact; set `contactEmail`
  so Wikidata can reach you, and it honours `429`/`Retry-After`.
- **Excluded upstreams:** PeeringDB facilities (AUP forbids storage/reproduction — ask
  support@peeringdb.com first), IAEA PRIS (no open licence), GEM trackers (request form, no stable file URL)
  and the NRC dataset pages (blocked for scripted clients) are noted, not built.

## Citizen science and signals (prompt 095)

- **`inat-observations` needs an area.** iNaturalist asks that its API be used for application development, not
  scraping, so the `aoi` parameter (a `west,south,east,north` box or a place id) is required and the source
  cannot be enabled without it. A box must not cross the antimeridian (split it into two sources) and may be at
  most 15° on a side. Each run reads one round of revisions (quality-grade changes, re-identifications) and new
  observations in the area, sending no more requests than the per-minute budget (2 by default: one page of each);
  a busy area catches up over the following runs from the stored cursor, and a run inside the upstream's
  `Cache-Control: max-age` sends nothing.
- **The privacy choices are deliberate.** No contributor usernames, descriptions or comments are ever stored.
  Obscured observations keep their randomised upstream coordinates with the upstream's own positional accuracy;
  `private` observations and observations without coordinates are skipped. iNaturalist photo links are kept only
  when the photo's license is open and its host is exactly the open-data bucket
  (`inaturalist-open-data.s3.amazonaws.com`); licenses are per record (see `licenseCode`).
- **`ebird-notable` is off by default and non-commercial.** It needs your own eBird key (`apiKey`, one per eBird
  account). Private locations are rounded to 0.05 degrees with a 5000 m accuracy, and neither their place name
  nor their eBird location id is stored. eBird's terms are non-commercial with attribution and the source is
  local-only for 109/110.
- **CDC wastewater.** `cdc-nwss-wval` reads site viral-activity levels and `cdc-nwss-detections` reads pathogen
  detections, both from the current datasets (the pre-September-2025 datasets are archived). Add an optional
  Socrata `appToken` to avoid the shared anonymous IP pool and its 429s. Each run sends at most its per-minute
  budget of requests (4), shared among the configured detection datasets in turn, and a weekly update larger
  than that is read over the following runs; only the latest week (or sample) of each site is kept current, so a
  re-dated older row never replaces a newer one. **A detection flip (`detected == true`)
  is the signal**; a rule with `detected == true` in `enter` catches it. All rows are public-domain US
  government data **except WastewaterSCAN rows, which are CC BY-NC 4.0**; those are dropped unless
  `includeWastewaterScan` is set, and then each carries `licenceOverride: "CC BY-NC 4.0 (WastewaterSCAN)"`, which
  109 honours.
- **`wiki-edit-surge` is off by default and bandwidth-heavy.** It holds one connection to the Wikimedia
  EventStreams `recentchange` firehose and reports **the bytes it reads** against a 3 GB/day budget; the stream is
  a continuous firehose, so measure your own volume before enabling it. It never stores usernames, edit comments
  or parsed comments; the page title is untrusted text and is the only embedded field. A useful rule:
  `phase == rising` in `enter`, with a **6 h cooldown** so a long surge does not repeat.

## Satellite scenes (prompt 096)

- **You must set an AOI.** `stac-sentinel2` and `stac-landsat` require the `aoi` parameter — a box
  `west,south,east,north` or a place id — so enabling either fails with `400 VALIDATION_FAILED` until you
  supply one. A box that crosses the antimeridian is split into two searches automatically. A place id searches
  the place's own area (its shape, radius or buffer), and `aoiCoverPct` is measured against that area.
- **Links only; no imagery is ever downloaded.** A `satellite-scene` record carries the footprint, the
  acquisition time, the cloud cover, the fraction of your AOI the footprint covers (`aoiCoverPct`), the STAC
  item link (`stacItemUrl`) and, only when the collection publishes a public one, an `https://` thumbnail.
  Landsat and Sentinel-1 thumbnails live in requester-pays S3 buckets (`s3://…`), so Foresignal drops them
  rather than rewriting or signing them; open `stacItemUrl` to get the full set of assets yourself.
- **Rules.** A rule is just `kind=satellite-scene` plus payload conditions, for example
  `cloudCover < 20 AND aoiCoverPct >= 80` to catch a clear scene over your area:
  ```json
  {"name": "Clear Sentinel-2 scene over home", "kinds": ["satellite-scene"],
   "sources": ["stac-sentinel2"], "match": "match", "onlyNew": true,
   "enter": "cloudCover < 20 AND aoiCoverPct >= 80"}
  ```
  Use `only_new` (`onlyNew`) so a reprocessed item that only moves `properties.updated` is a touch, not a new
  alert. `cloudCover` is percent (one decimal); `aoiCoverPct` is an integer 0–100.
- **Cloudy-season flood watching with Sentinel-1.** SAR sees through cloud, so enable `stac-sentinel1`
  (off by default) and watch `kind=satellite-scene`, `sources=["stac-sentinel1"]` with a
  `aoiCoverPct >= 90` rule: a new Sentinel-1 scene over your flood AOI is a fresh, cloud-independent look.
  Sentinel-1 has no `cloudCover`, so leave it out of the conditions.
- **Cadence and quota.** Each run searches your AOI hourly (min 15 minutes) at 4 requests/minute and 300/day;
  the first run looks `lookbackDays` (default 3) behind, later runs start two days behind the newest scene
  already seen. `maxPages` (default 5, at most 10) caps the pages of 100 read per run, and a run never sends more
  searches than the per-minute budget (4), shared by the parts of a split AOI; when that ends a search early the
  log says so. A cloudy week returns nothing, which is normal (no silent-break warning).

## Raster exports and offline packs (prompt 108)

Async raster exports and place offline packs write files under
`${FORESIGNAL_DATA_DIR}/exports/{exportId}/` and keep them for 48 h; they count against the raster disk
quota. Submit an export with `POST /v1/products/{id}/exports` (`format`: `mbtiles`, `pmtiles` or `cog`,
plus `frameKey`/`frameKeys`, `bbox` or `placeId`+`radiusKm`, `minZoom`, `maxZoom`, `style`, `tms`), poll
`GET /v1/exports/{id}`, and download via `GET /v1/exports/{id}/files/{name}`. Caps: 50,000 tiles per
tileset and 2 GB per file. `POST /v1/places/{id}/pack` builds a ZIP (≤ 50 MB) of place weather, forecast
series, nearby alerts, legends, a loop GIF and one PMTiles per product.

A sweep (every 15 minutes, `foresignal.raster.export.sweep-interval`) deletes jobs and their files once
`expiresAt` passes, finished or not, and fails jobs that can no longer finish: those still queued or
running when the process restarted (submit them again) and any stuck for more than 6 hours. MBTiles and
PMTiles hold WebMercatorQuad tiles only (`tms=WorldCRS84Quad` is 400); a COG follows `tms`
(WebMercatorQuad → EPSG:3857, WorldCRS84Quad → EPSG:4326). Every file embeds the product's attribution and
license (MBTiles/PMTiles `attribution` and `description`, the TIFF `Copyright` tag and `GDAL_METADATA`,
the pack's `manifest.json`), with a "changes made" note for CC BY and ECMWF products. A place pack only
holds products the requesting token may read, and the same request from the same token within 10 minutes
returns the pack already built.

WebP tiles need the bundled `webp-imageio` native library. A missing native (an unsupported platform)
disables WebP: `/v1/system` reports a `WEBP_UNAVAILABLE` warning and `.webp` answers `406
FORMAT_UNAVAILABLE`. Set `notifications.imageLinks` only on an instance with an https `publicBaseUrl`;
enabling it on a LAN-only instance exposes one media path per feed key and is refused without https.

## Licences and egress (prompt 109)

Every source resolves to structured licence terms (`ai.foresignal.provenance.LicenceTerms`, from
`provenance/licences.json`): `commercialUse`, `shareAlike`, `attributionRequired` and `redistribution`.
`UNKNOWN` is treated as `FORBIDDEN` (commercial) and `LOCAL_ONLY` (redistribution). Free-text descriptor
licenses resolve through catalog aliases; provider terms that state no license resolve to `provider-terms`,
`provider-terms-nc` or `unspecified` (redistribution `UNKNOWN`, so local use only). A DERIVED source takes
the most restrictive terms of its inputs (`derivedFrom`, else the sources its generator subscribes to). A
custom PUSH source may declare its own `licenceTerms`; each PUSH batch snapshots their id.

A version's license is the one snapshotted on its batch's `fetch_log` row, else the source's current terms
(`licenceBasis: descriptor`). A peer record's `_foresignal.originLicence` overrides the batch license, and an
MLAT position is downgraded to `LOCAL_ONLY` while keeping its license id and attribution. Fetch rows that wrote
versions are kept for `max(retention.provenance, the largest version window)`. `include=provenance` returns
`fetchId`, `adapterVersion`, the ingest `principal` (admins only), the Admiralty `reliability` and the
`credibility` (1–6, computed at read time), composed as a `grade` (e.g. `B2`).

**Reliability.** Unless a descriptor sets its own grade, official agencies (US federal public domain and the
open government licenses) get `B`, your own receivers and other built-in providers `C`, community and
news-derived feeds (GDELT, Wikipedia surge) `D`, and custom, peer and DERIVED sources `F`. Override it per
source with `PUT /v1/sources/{id}` `{"reliability": "A"}`; `null` reverts to the default.

**Licence-filtered reads.** Clients send `Foresignal-Licence-Policy: no-nc, no-sa, redistributable` (or
`licencePolicy=`). A token's `licencePolicy` is a floor a request may add to but never loosen; set it at
creation or replace it with `PATCH /v1/tokens/{tokenId}` `{"licencePolicy": [...]}` (admin; `[]` clears it).
JSON pages and GeoJSON collections carry `licenceFiltered` (records dropped) and aggregated `attributions`,
plus a `Foresignal-Attribution` header (ISO-8859-1, at most 1 KB, cut with `...`) and
`Link: <termsUrl>; rel="license"`. Record reads send `Vary: …, Foresignal-Licence-Policy`. Tiles, the SSE
change stream and `/v1/changes` prune excluded sources in SQL and send `Foresignal-Licence-Filtered: <n>`, the
number of *sources* the policy pruned (their records are never read, so they cannot be counted). A layer's
TileJSON `attribution` aggregates its sources.

**Egress.** `EgressPolicy` is the one component that decides whether a record may leave, on every path; its
license step (`LicenceEgressGuard`) is internal and is applied on every path, `EXPORT` and `ARCHIVE` included.
In order:

1. An unregistered source never leaves.
2. A private source is excluded on every path except `API_READ`, `TILE`, `SSE` and `MCP` for callers allowed
   to see it, and `ARCHIVE` (your own storage), whatever its license. Two explicit exceptions: an `EXPORT` by
   an admin with `includePrivate=true` (111), and a sink with `filter.includePrivate=true` that is a SERVE sink
   bound to loopback/private or a PUSH sink whose target resolves to a private address (110). Feeds, share
   links and federation never carry private records.
3. A source whose effective `egress` is `LOCAL_ONLY` is served to this instance's own clients, exports and the
   archive, and to a LAN-serving sink bound to loopback/private only; never pushed, shared, federated or put
   on a feed.
4. An MLAT position never leaves on a sink push, a share, a feed or federation, nor on a LAN sink that is not
   local.
5. The record's license goes through the table below. Turning a local receiver's egress to `OPEN` (only the
   `egressOverridable` families allow it) also lifts its `LOCAL_ONLY` license default; `FORBIDDEN` terms are
   never lifted.

Denials are counted as `foresignal.licence.egress.denied{path,licence}`.

| Redistribution | API_READ / TILE / SSE / MCP / LLM_* / NOTIFICATION / FEED / ARCHIVE | SINK_SERVE_LAN | SINK_PUSH, EXPORT | PUBLIC_SHARE, FEDERATION, SHARE |
|---|---|---|---|---|
| ALLOWED | yes | yes | yes | yes (NC terms: yes; attribution always attached) |
| LOCAL_ONLY | yes | yes, only if the sink binds loopback/private | no (EXPORT: only with `personalUse=true`) | no |
| FORBIDDEN | yes (own use) | no | no (EXPORT: only with `personalUse=true`, sidecar marks "do not redistribute") | no |

So `opensky-states`, `acled` and `aisstream` (`FORBIDDEN`) are exported only with `personalUse=true` and never
reach a sink, a share link or a peer.

**Signing.** The instance Ed25519 signing key is published at `GET /.well-known/jwks.json` (and
`GET /v1/system/jwks`), `Cache-Control: public, max-age=3600`; `POST /v1/system/signing-key/rotate` retires the
active key, activates a new one and writes a `signing_key` audit event. Retired keys stay published for
`signing.retainRetired` (365 days). A stored briefing run carries `signature`, a detached JWS (`b64:false`)
over its markdown; `GET /v1/briefings/{id}/runs/{runId}` sends it as `Foresignal-Signature`, and the HTML
rendition sends a signature over its own bytes. Verify it with any JOSE library, for example Python
`jwcrypto`:

```python
import json, urllib.request
from jwcrypto import jwk, jws
jwks = json.load(urllib.request.urlopen("https://host/.well-known/jwks.json"))
key = jwk.JWK(**jwks["keys"][0])
signed = jws.JWS(payload)          # payload = the exact response bytes
signed.deserialize(signature, key)  # signature = the Foresignal-Signature header
```

or without one, from the definition (RFC 7797: the signing input is the protected header, a dot and the
unencoded payload), with the `cryptography` package:

```python
import base64, json
from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PublicKey

def b64url(s): return base64.urlsafe_b64decode(s + "=" * (-len(s) % 4))

header_b64, _, signature_b64 = signature.split(".")   # "header..signature"
header = json.loads(b64url(header_b64))
assert header["alg"] in ("Ed25519", "EdDSA") and header["b64"] is False and header["crit"] == ["b64"]
jwk_ = next(k for k in jwks["keys"] if k["kid"] == header["kid"])
Ed25519PublicKey.from_public_bytes(b64url(jwk_["x"])).verify(
    b64url(signature_b64), header_b64.encode("ascii") + b"." + payload)   # raises if invalid
```

## Outbound sinks (prompt 110)

Outbound sinks let existing ecosystems consume Foresignal with no new client code. Each sink is created
disabled (`POST /v1/sinks` with `enabled=false` unless set true); enabling starts it on its own virtual
thread with a durable cursor under `sink`/`sink_cursor`. Sinks are an egress path like any other: every
record goes through `EgressPolicy` (the one egress decision, 109) on `SINK_SERVE_LAN` (SERVE sinks) or
`SINK_PUSH` (PUSH sinks), so the private flag, the descriptor's `egress` (with your overrides), the MLAT
rule and the license table all apply.

| Type | Kind | Mode | Client setup |
|---|---|---|---|
| `mqtt-state` | STATE_MIRROR | PUSH | Retained `{prefix}/state/{source}/{externalId}` (QoS 1); LWT `{prefix}/status` |
| `cot` | STATE_MIRROR | PUSH/SERVE | ATAK/iTAK/WinTAK: UDP multicast SA (default `239.2.3.1:6969`, LAN) or a TCP network input |
| `readsb-json` | STATE_MIRROR | SERVE | tar1090 / Virtual Radar Server: `http://host:port/data/aircraft.json` |
| `sbs` | STATE_MIRROR | SERVE | BaseStation client: TCP `host:port` |
| `nmea-aivdm` | STATE_MIRROR | SERVE | OpenCPN / Signal K: NMEA 0183 TCP provider `host:port` |
| `aprs-is-serve` | STATE_MIRROR | SERVE | Xastir / YAAC / APRSdroid: APRS-IS server `host:port` |
| `cloudevents` | CHANGE_FEED | PUSH | HTTP receiver; verify `X-Foresignal-Signature` |

**What never leaves.** OpenSky, ACLED and aisstream records are never re-served on any sink (their terms
forbid redistribution). `LOCAL_ONLY` records — sources whose descriptor `egress` is `LOCAL_ONLY` (for
example `eia-petroleum`, even though its license is public domain), the operator's own receivers, records
whose license is unknown, and every MLAT position — reach only SERVE sinks bound to loopback or a private
address, never a PUSH sink. Setting an overridable local receiver's egress to `OPEN`
(`PUT /v1/sources/{id}`) lets its non-MLAT records reach PUSH sinks too. A private source needs
`filter.includePrivate=true` **and** a local target (a SERVE sink, a PUSH target that resolves to a private
address, or the CoT LAN multicast group).

**Denied records.** A record the policy refuses is skipped and counted, never retried: a CHANGE_FEED cursor
moves past it, so one denied record cannot stall the sink or hold back retention. Denials are counted as
`foresignal.sink.denied{sink,reason}` (reasons `private`, `local-only`, `mlat`, `licence-forbidden`,
`licence-local-only`, `unregistered`) and shown, per reason and per source, under
`GET /v1/sinks/{id}/status` → `denied` (written to the cursor every few seconds).

**Filters.** `filter.sources`, `filter.kinds` and `filter.bbox` select records; `filter.placeId` keeps
records whose geometry intersects the place; `filter.watchlistId` keeps records whose watched identity
(icao24, registration, callsign, mmsi, imo, read from the payload field the source declares) is on the list,
or whose external id is on a `custom` list. A filter that names a deleted place or list matches nothing.

**Delivery and resync.**

- *CHANGE_FEED* (`cloudevents`): ordered by seq, at least once. The cursor is persisted after every
  acknowledged batch (`config.batchSize` 1–100; above 1 the body is `application/cloudevents-batch+json`). A
  new sink, a sink whose cursor fell below the purge horizon, and `POST /v1/sinks/{id}/resync` first replay
  current state as `ai.foresignal.record.snapshot` events, then continue with changes after the snapshot.
  A 2xx acknowledges; 429 and 5xx back off (honoring `Retry-After`), other 4xx mark the sink `FAILING`;
  neither advances the cursor. The URL is re-checked against the outbound URL policy on every delivery,
  and the request goes to the address that check resolved.
- *STATE_MIRROR*: a snapshot of current state, then persisted changes and live updates. An update older
  than the mirrored `observedAt` is ignored (counted as `dropped`); tombstones and records past
  `validUntil` are removed; a record that leaves the filter is removed. A live-bus overflow, a change-feed
  gap or `POST /v1/sinks/{id}/resync` drops the mirror and snapshots again. `mqtt-state` republishes its
  retained topics after a broker reconnect.
- A failing sink restarts with backoff (5 s doubling to 5 min) and raises one `system.sink_failing`
  notification after 10 minutes of failure.

**SERVE binding.** A SERVE sink must set `config.port` (there is no default). `config.bind` defaults to
`127.0.0.1`; only loopback, RFC 1918, link-local or ULA are accepted, `100.64.0.0/10` only with
`allowTailnet=true`, and `0.0.0.0` only with `allowAllInterfaces=true` (then every connecting peer must
pass the same check, for the TCP types and for `readsb-json` alike). `maxClients` defaults to 8; further
connections are refused (HTTP: `503`) and counted under `clients.rejected`. TCP clients that do not read
fast enough are disconnected rather than allowed to stall the sink, and a client line longer than 1 KiB
closes the connection. `aprs-is-serve` sends a `#` keepalive comment every 20 s and discards everything
clients send. CoT PUSH sends only to a LAN-scoped multicast group (`239.0.0.0/8`, `224.0.0.0/24`, or IPv6
link/site/organization scope) with TTL 1.

In Docker, publish the port, e.g.:

```yaml
services:
  foresignal:
    ports:
      - "${FORESIGNAL_SINK_BIND:-127.0.0.1}:8081:8081"
```

**Docker NAT and the peer check.** With Docker's default bridge network and port publishing, connections
reach the container from the bridge gateway (for example `172.17.0.1`), not from the real client, so the
per-peer check sees a private address for every client: `allowAllInterfaces` then admits anyone who can
reach the published port. Restrict exposure with the host side of the `ports:` entry
(`FORESIGNAL_SINK_BIND`) or a host firewall, or run with `network_mode: host` so the real peer address is
visible.

**Secrets.** Secrets (`secrets` on create and update) are write-only and encrypted at rest; responses list
only their names (`secretsSet`). An update merges them: a field with a value sets it, `null` removes it, and
an omitted field keeps its stored value.

## Exports, snapshots and holds (prompt 111)

**Bulk exports.** `POST /v1/exports` (agent, always authenticated) queues an export of records and answers
`202` with the job; poll `GET /v1/exports/{id}`, list your jobs with `GET /v1/exports` (admin sees every
job; `kind=`, `status=`, `cursor=`, `limit=`), download with `GET /v1/exports/{id}/download`, and cancel or
delete with `DELETE /v1/exports/{id}`. A job is visible only to the token that created it and to admin.

| `format` | File | Notes |
|---|---|---|
| `geoparquet` (default) | `{id}.parquet` | GeoParquet 1.1: WKB `geometry`, the `bbox` covering struct, `geo` metadata; ZSTD pages; row groups of 65,536 rows |
| `geopackage` | `{id}.gpkg` | OGC GeoPackage 1.4: one feature table per kind, RTree spatial index, the license summary in `gpkg_metadata` |
| `flatgeobuf` | `{id}.flatgeobuf` | Packed Hilbert R-tree (node size 16); written without an index when some record has no geometry |
| `gpx` | `{id}.gpx` | `mode=history` only: one `<trk>` per entity of a TRACK source, `<wpt>` for other point records |

The spec chooses `sources[]`, `kinds[]`, `bbox` or `placeId`, a `from`/`to` window on `timeField`
(`effective`, `observed`, `event`, `ingested`), and the `mode`: `current` (live state), `history` (every
version), `asOf` (the state known at an instant; the same horizon rules as `/v1/records?asOf=`) or `atSeq`
(the state at a seq; see snapshots). `fields[]` keeps only those payload keys; `flatten=true` adds one text
column per payload field (the `fields[]`, else every `fieldSpecs` key of the sources). Rows are read in
keyset pages of 10,000, so a cancel takes effect within a page.

Every finished job carries, next to its data file, `manifest.json` (each file's SHA-256, the spec, the
watermark and the license summary), `manifest.json.jws` (a detached Ed25519 JWS over `manifest.json`,
verifiable against `/.well-known/jwks.json` exactly as in "Licences and egress" above), `licence.json`
(the license summary: per license the terms, sources and rows; the attributions; the exclusions) and
`ATTRIBUTION.txt`. `?bundle=zip` downloads all of them in one zip; `?file=` names one file. Downloads support
`Range` and `If-None-Match` (the `ETag` is the file's SHA-256), so `curl -C -` resumes:

```sh
curl -sS -H "Authorization: Bearer $AGENT" -H 'Content-Type: application/json' \
  -d '{"format":"geoparquet","sources":["usgs-earthquakes"],"mode":"history","from":"2026-10-01T00:00:00Z"}' \
  https://host/v1/exports
curl -C - -o quakes.zip -H "Authorization: Bearer $AGENT" "https://host/v1/exports/$ID/download?bundle=zip"
```

**License rules.** Sources whose terms forbid redistribution (`FORBIDDEN`, for example OpenSky and ACLED)
or restrict it to this instance (`LOCAL_ONLY`, including unknown terms) are left out and listed under
`excluded` in `licence.json`. `personalUse=true` includes them; the sidecar and `ATTRIBUTION.txt` then say
"Personal use only — do not redistribute" and list them. Private sources need an admin token and
`includePrivate=true` (`403` otherwise).

**Limits** (`foresignal.exports.*` in `application.yml`): `max-concurrent` (1: exports, snapshots and packs
share one worker pool), `max-rows` (5,000,000; a larger export fails), `disk-quota-gb` (20 GiB across
`${FORESIGNAL_DATA_DIR}/exports` and `/snapshots`; a new job is refused with `409 EXPORT_QUOTA`), `ttl`
(7 days; then the hourly retention run deletes the files and the job becomes `EXPIRED`, and downloads answer
`410 EXPORT_EXPIRED`), `page-size` (10,000) and `query-timeout` (5 min per page). A job a restart
interrupted is marked `FAILED`; submit it again. `/v1/system` shows `exports.queued`, `running` and `bytes`;
metrics `foresignal.export.rows` and `foresignal.export.duration` are tagged by format.

**Opening the files.** DuckDB reads GeoParquet with its spatial extension
(`INSTALL spatial; LOAD spatial; SELECT kind, count(*) FROM 'export.parquet' GROUP BY kind;`), and GDAL 3.8
or later with `ogrinfo export.parquet`. QGIS opens the `.gpkg` directly (Layer → Add Layer → Add Vector
Layer); the manual check is that each kind shows as a layer with its features in place and that
`ogrinfo -al -so export.gpkg` reports the spatial index. `ogrinfo export.flatgeobuf` reads FlatGeobuf, and
the GPX file loads in any GPX viewer.

**Snapshots.** `POST /v1/snapshots` (admin) writes the state at a seq S as GeoParquet with the same
sidecars, under `${FORESIGNAL_DATA_DIR}/snapshots/{id}/`. S is `atSeq`, else `S(asOf)`, else the current
watermark. The state is rebuilt from `record_version` per tier (the newest version of each entity at or
below S, tombstones dropped, validity judged at the ingestion time of S). A snapshot is an export job of kind
`SNAPSHOT`: poll and download it through `/v1/exports/{id}`. It never expires unless `expiresAt` is set.
When S lies before a source's retention horizon, the versions it needs are read from a hold's copies; with
no hold that covers the source, its kinds and the needed seq range since before those versions were
deleted, the request fails `400 SNAPSHOT_BEYOND_RETENTION` (with `sourceId` and `horizon`).

**Holds.** `POST /v1/holds` (admin) keeps the records of some sources and kinds, within an ingestion-time
range (`from`/`to`) or a seq range (`fromSeq`/`toSeq`), through retention: before each retention run
deletes STANDARD versions or drops TRACK partitions, it copies the rows about to go that fall inside an
active hold into `record_version_held`. Retention still drops partitions and `floorSeq` is unchanged; a hold
never blocks a drop. When a copy fails, retention keeps those rows for that run (it skips the STANDARD
source, or keeps the TRACK partition and every later one). At most 20 holds (`holds-max`); a hold lapses at
its `expiresAt`. `GET /v1/holds/{id}/versions` pages through the copies; `DELETE /v1/holds/{id}` deletes the
hold and its copies. `/v1/system.holds` reports the count, the copied rows and the table's size. Copies are
made only from the moment a hold exists: create the hold before the records age out.

## Cold archive (prompt 111)

The cold archive writes records out as GeoParquet before retention removes them, so history stays
queryable after the database lets it go. It is off by default; configure it with
`PUT /v1/settings/archive` (admin):

```json
{"enabled": true, "sources": [], "root": "", "target": "local", "keepLocal": true}
```

`sources` is an allowlist (empty: every non-private source); `root` defaults to
`${FORESIGNAL_DATA_DIR}/archive` and may be an absolute path (another disk) or a path relative to the data
directory. Each hourly retention run then writes, before its deletes and drops, exactly the rows it is about
to remove: for every TRACK partition about to be dropped, the rows of each archived TRACK source in it, and
for every STANDARD source, its rows below the run's cutoff. Files land in a hive layout:

```
archive/record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd}/part-{fromSeq}-{toSeq}.parquet
archive/record_version/tier={std|trk}/source_id={id}/date={yyyy-mm-dd}/_manifest.json
```

`date` is the UTC date of `ingested_at`; each part is named after the first and last seq it holds, and the
partition's `_manifest.json` lists every part with its rows, SHA-256, upload state and the source's
license. A part is written under a temporary name, fsynced, and its row count is read back from the Parquet
footer before retention deletes anything; only then does the archive's watermark for that scope advance, so
a row is never archived twice, even when a delete fails after the write. When a write fails, retention keeps
the rows for that run (it skips the STANDARD source, or keeps the TRACK partition) and counts
`foresignal.archive.failed`. `foresignal.archive.bytes` counts the bytes written; `/v1/system.archive` shows
the last run, its bytes and failures. `GET /v1/archive/catalog?source=&from=&to=` lists the partitions and
their files. The archive is the `ARCHIVE` egress path (operator-owned storage), allowed for every license;
the manifests still record each license.

**S3 and MinIO.** Set `target` to `s3` and give the bucket and the credentials (write-only; the API only
lists their names under `secretsSet`, and they are stored encrypted):

```json
{"enabled": true, "target": "s3", "keepLocal": false,
 "s3": {"endpoint": "http://192.168.1.20:9000", "bucket": "foresignal-archive", "prefix": "prod",
        "region": "us-east-1", "pathStyle": true},
 "secrets": {"accessKey": "REPLACE", "secretKey": "REPLACE"}}
```

The endpoint must pass the outbound URL policy's LAN-allowed profile: a public endpoint, or one on
loopback, RFC 1918, unique-local or 100.64/10 addresses, over https or http (a refused endpoint is
`400 URL_NOT_ALLOWED`). MinIO needs `pathStyle: true`; AWS S3 works with either style. Each verified part
is uploaded after it is written, by one background thread: files up to 64 MiB with one `PutObject`, larger
ones as a multipart upload in 64 MiB parts, every request signed with AWS Signature Version 4 over the
body's SHA-256. The local copy is deleted only when `keepLocal` is false **and** the returned ETag matches
the file's MD5 (or the multipart MD5-of-MD5s). A MinIO bucket for the archive:

```sh
mc alias set lan http://192.168.1.20:9000 ADMIN_USER ADMIN_PASSWORD
mc mb lan/foresignal-archive
mc admin user add lan foresignal REPLACE_SECRET
mc admin policy attach lan readwrite --user foresignal
```

**Reading the archive.** DuckDB reads the whole tree with its partition columns:

```sql
INSTALL spatial; LOAD spatial;
SELECT source_id, date, count(*) AS rows
  FROM read_parquet('archive/record_version/**/*.parquet', hive_partitioning=true)
 GROUP BY ALL ORDER BY ALL;
```

From S3, `CREATE SECRET (TYPE s3, KEY_ID '…', SECRET '…', ENDPOINT '192.168.1.20:9000', URL_STYLE 'path', USE_SSL false);`
then `read_parquet('s3://foresignal-archive/prod/record_version/**/*.parquet', hive_partitioning=true)`.
GDAL reads a part with `ogrinfo part-….parquet` (GDAL 3.8 or later) and converts a partition with
`ogr2ogr -f GPKG partition.gpkg part-….parquet`.

## Offline packs (prompt 111)

`POST /v1/packs` (agent, always authenticated) builds PMTiles v3 files for phones and field laptops:

```json
{"name": "Austin field pack", "bbox": [-97.95, 30.1, -97.55, 30.5], "minZoom": 6, "maxZoom": 12,
 "vector": {"sources": ["usgs-earthquakes"], "kinds": [], "asOf": null},
 "raster": {"productId": "mrms-reflectivity", "frame": "latest"}}
```

`vector` produces `vector.pmtiles` (Mapbox Vector Tiles, gzip, one layer named `foresignal`, the same
features as `/v1/tiles`; `asOf` renders the state at that instant); `raster` produces `raster.pmtiles`
(PNG tiles of one product frame; `latest` is resolved when the pack is queued). A pack may cover at most
`foresignal.exports.pack-max-tiles` (200,000) tiles over its box and zooms, else
`413 PACK_TOO_LARGE` with the computed `tileCount`. Empty tiles are left out; identical tiles are stored
once. The metadata carries `name`, `vector_layers`, the aggregated `attribution`, `generated` and
`watermarkSeq`. A pack is an export job of kind `PACK` with the same license and private rules and sidecars:
download each file with `GET /v1/exports/{id}/download?file=vector.pmtiles` (resumable with `Range`). Packs
are files to copy to the device; Foresignal never proxies `pmtiles://` from an upstream.

**MapLibre GL JS** (the `pmtiles` npm package) opens a local file the user picks:

```js
import maplibregl from "maplibre-gl";
import { PMTiles, Protocol, FileSource } from "pmtiles";

const protocol = new Protocol();
maplibregl.addProtocol("pmtiles", protocol.tile);
const pack = new PMTiles(new FileSource(file)); // file: a File from <input type="file">
protocol.add(pack);
map.addSource("foresignal", { type: "vector", url: "pmtiles://" + pack.source.getKey() });
map.addLayer({ id: "records", type: "circle", source: "foresignal", "source-layer": "foresignal" });
```

**Flutter** (`flutter_map`): the raster pack with the `flutter_map_pmtiles` package and the vector pack with
`vector_map_tiles_pmtiles`, both reading a file on the device:

```dart
final rasterProvider = await PmTilesTileProvider.fromSource('/path/to/raster.pmtiles');
final vectorProvider = await PmTilesVectorTileProvider.fromSource('/path/to/vector.pmtiles');
```

**Checking a pack.** The manual check is the `pmtiles` command-line tool from protomaps/go-pmtiles:
`pmtiles verify vector.pmtiles` checks the header counts, the directories and the clustered order, and
`pmtiles show vector.pmtiles` prints the header and metadata. The header's zoom range is that of the tiles
actually present.

## Standards outputs (prompt 112)

Foresignal also speaks four standards that existing tools understand, for clients that can only fetch a URL.
Every one of them honors the caller's token scope, restricted tokens and feed keys, hides private sources and
applies the license rules (109).

### OGC API – Features (QGIS, GDAL, GeoPandas)

`/v1/ogc` is an OGC API – Features server (Part 1 core and GeoJSON, Part 3 filtering and queryables, basic CQL2 in
`cql2-text` and `cql2-json` with `S_INTERSECTS`). Each record kind is two collections: `{kind}` (current state)
and `{kind}-history` (every stored version; feature ids `{sourceId}:{externalId}:{version}`). The raster products
of 108 are listed next to them as coverage collections. JSON only; unknown query parameters are refused with
`400` so a typo never returns unfiltered data (`f=json` is tolerated).

- **QGIS:** Layer → Add Layer → Add WFS / OGC API – Features Layer → New, URL `https://foresignal.lan/v1/ogc`
  (with `openReads` off use a feed key whose prefix is `/v1/ogc`: `https://foresignal.lan/v1/k/fsf_…/ogc`).
  Connect, pick a collection, and use the filter button to write a CQL2 expression such as
  `mag > 5 AND S_INTERSECTS(geometry, BBOX(-98, 29, -96, 31))`. Manual check (not automated): the collections
  list, a layer loads, paging continues past 100 features and a filter narrows the layer.
- **GDAL/OGR:** `ogrinfo "OAPIF:https://foresignal.lan/v1/ogc" earthquake -so` and
  `ogr2ogr -f GPKG quakes.gpkg "OAPIF:https://foresignal.lan/v1/ogc" earthquake -where "mag > 5"`.
- **GeoPandas:**

  ```python
  import geopandas as gpd
  url = ("https://foresignal.lan/v1/ogc/collections/earthquake/items"
         "?limit=1000&filter=mag%20%3E%205")
  quakes = gpd.read_file(url)
  ```

`limit` is at most 10,000; `next` links carry an opaque cursor; `numberMatched` is returned with `count=true` up
to 100,000. `datetime` matches the effective time of current collections and the observed time of history.
`/v1/ogc/collections/{id}/queryables` lists the filterable properties (the kind's declared payload fields plus
`title`, `sourceId`, `observedAt`, `eventTime`, `version` and `geometry`). A filter error is
`400 FILTER_INVALID` with the `position` of the problem.

### CAP 1.2 for alerting systems

Every alert state change issues one CAP 1.2 message (`Alert` when it fires; `Update` on acknowledgement, snooze,
note or enrichment; `Update` with `responseType AllClear` when it resolves; never `Cancel`, which CAP reserves for
messages issued in error). Each message lists the earlier ones in `references`, is `scope Public`, and carries the
note "Generated automatically by a Foresignal instance from third-party data. Not an official warning."
Consumers must treat it as such: it is a relay of third-party data, not an authority's warning. For alerts on
official CAP sources (the `cap-*` and `meteoalarm-*` feeds, `tsunami-warnings`, `nws-alerts`) the message links
the upstream message in `web` and names the upstream sender in the description.

- `GET /v1/cap/alerts.atom` — the Atom index (filters `ruleId`, `minSeverity`, `tags`, `placeId`, `since`).
- `GET /v1/cap/alerts/{alertId}.cap` — an alert's latest message; `GET /v1/cap/messages/{identifier}.cap` — any
  stored message. Identifiers are `{instanceId}-{alertId}-{n}`.

The sender is the setting `cap.sender` (default `foresignal@{installId}`) and `senderName` is
"Foresignal ({instance.name})" (default the install id). Messages are deleted with their alert by retention.
Messages of alerts that fired before the upgrade start with the next state change.

### Calendars

`GET /v1/calendars/{feed}.ics` with `feed` one of `launches` (Launch Library NET and window), `tfr` (FAA TFR
effective to expiry; open-ended ones carry `X-FORESIGNAL-OPEN-ENDED:TRUE`), `nws` (NWS onset to end),
`briefings` (the next 30 days of each enabled briefing, one event per occurrence in the briefing's time zone) or
`custom?source=…&kind=…` (any kind whose descriptor declares `startField`/`endField`; otherwise
`400 ICAL_FIELDS_MISSING`). Filters `bbox`, `placeId`, `source`, `kind`. UIDs are stable
(`{sourceId}:{externalId}@{instanceId}`) and `SEQUENCE` is the record version, so a calendar app updates an event
in place; a canceled or expired record stays for 7 days as `STATUS:CANCELLED`. Calendars ask to be refreshed
every 15 minutes.

To subscribe from a phone or desktop calendar with `openReads` off, create a feed key whose prefix is
`/v1/calendars` and subscribe to `https://foresignal.lan/v1/k/fsf_…/calendars/launches.ics` (in Apple Calendar:
File → New Calendar Subscription; in Google Calendar: Other calendars → From URL, which needs a public URL).

### Share links

`POST /v1/share-links` (agent token) creates an expiring link (default 7 days, at most 30) to one `record`,
`alert`, `view`, `track`, `briefing-run` or `export`; the `fss_` key is returned once and only its hash is
stored. The link works without a token, only at `GET /v1/k/{key}` (JSON, or GeoJSON with
`Accept: application/geo+json`), `/v1/k/{key}/geojson`, `/v1/k/{key}/tiles/{z}/{x}/{y}.mvt` (views) and
`/v1/k/{key}/download` (exports); it is never accepted as a bearer token. Uses are counted (`maxUses`), limited to
60 a minute per link, and audited (the first use and then at most one an hour); a revoked, expired or used-up link
answers `410 SHARE_EXPIRED`. With `clientLinks.share` set in the server settings (placeholder `{key}`),
`?open=1` redirects to your client.

Privacy defaults: a link renders within the creating token's restriction and never shows a private source.
Records whose license does not allow redistribution are removed, and a target that is such a record (or an
export or briefing run built from such sources) is refused with `409 SHARE_LICENCE_FORBIDDEN`; attribution is
always included. `fieldMask` keeps only the listed payload fields and `precisionM` coarsens coordinates. The
privacy mask for personal sources (precision capped to 1,000 m, observer, device and address-like fields removed,
the payload reduced to `title`, `kind` and `observedAt`) is built in, but a private target is refused even for an
admin token, because Foresignal's egress policy never lets a private record leave on a share link. Views over
places or watchlists cannot be shared (they reveal your places).

## Federation and edge relay (prompt 113)

Foresignal instances can share data with each other. Every replicated record carries a provenance chain (the
origin instance, source, external id and seq, then one hop per instance that re-served it) and a globally unique
reference, `urn:foresignal:{instanceId}:{sourceId}:{externalId}` (`globalRef` on records, versions and changes;
the instance id is the install id).

### Topologies

- **Home hub ⇄ cabin edge.** A Raspberry Pi at the cabin runs `FORESIGNAL_MODE=edge` with its local receivers
  (ADS-B, AIS, mesh, decoders). It pushes to the hub whenever the link is up. On the hub, create a `RECEIVE` peer
  for the cabin and rotate its ingest key; on the edge, create a `federation-push` sink to the hub with that key.
  Set `sameOperator: true` on both when the hub is yours, so the cabin's `LOCAL_ONLY` receiver data may reach it.
- **Two friends' instances.** Each creates a read token for the other with `restrictions.federation: true`
  (optionally narrowed to sources, kinds or a bbox) and a `PULL` peer pointing at the other's base URL with that
  token. Each replicates the subset the other's token and its own `selection` allow.
- **A ring** (A → B → C → A) is safe: an item whose chain already names the receiving instance is dropped
  (`foresignal.federation.dropped{reason="loop"}`), and so is one whose chain has `maxHops` (default 3) or more
  entries. The same origin arriving over two paths becomes one local record: the first peer source to claim an
  origin owns it, later updates from either path land in that record, and an update older than the newest one
  applied is dropped as `dedup`.

### Serving peers

The feed is `GET /v1/federation/changes`, `/stream` (SSE with `change`, optional `live`, `heartbeat` and `reset`)
and `/snapshot`. Each needs a token with the read scope **and** `restrictions.federation: true`; the token's
source, kind, bbox and area restrictions still apply, and `includeExercise: true` adds exercise records. Below
the purge horizon the feed answers `410 RESYNC_REQUIRED` and the stream sends `reset`, and the peer re-snapshots.
`GET /v1/federation/info` (signed with the instance key, `Foresignal-Signature`) tells a peer who you are.

### What never leaves

Every record passes the one egress policy on the federation path before it is serialized:

- licenses that forbid redistribution: `opensky-states`, `acled`, `aisstream`;
- `LOCAL_ONLY` sources and licenses: the local receivers (`local-*`), and anything you have not opened;
- MLAT positions, from any source;
- private sources (personal location, mesh, ACARS, home sensors), always;
- exercise records, unless the token asks for them.

Exclusions count in `foresignal.federation.denied{reason}`. The one exception is an edge pushing to its
operator's own hub (`sameOperator: true` on the `federation-push` sink and on the hub's peer): `LOCAL_ONLY`
records may then travel, labelled `local-only-operator`, and the hub keeps them `LOCAL_ONLY`, so it never
re-federates them. Private records and MLAT positions never travel, even then.

### Consuming a peer

`POST /v1/peers` (admin) with `id`, `name`, `baseUrl` (checked by the outbound URL policy: LAN, Tailnet and public
addresses are allowed), `mode: PULL`, the remote `token` (stored encrypted, never returned) and a `selection`:
`sources`, `kinds`, `bbox` or `placeId`, `maxAge` (drop older items), `backfill` (`none`, the default: the current
snapshot only; or a duration of history to page from the remote change feed), `live`, `maxHops`, `trackTtl`.
The peer registers `peer-{id}` (STANDARD), `peer-{id}-trk` (TRACK, TTL `trackTtl`, sampled like other tracks) and
`peer-{id}-events` (compact messages). Each record keeps the origin's license in `_foresignal.originLicence`, a
volatile payload member, so an identical re-send makes no new version and the license follows the record. Peer
data has reliability `F` (cannot be judged) unless you set `reliability`.

**Key pinning.** On the first successful contact the remote signing key is pinned (trust on first use). If the
peer later signs with another key, replication stops with the health reason `PEER_KEY_MISMATCH`. Check with the
peer's operator, then `PUT /v1/peers/{id}` with `pinnedKid: null` to re-pin on the next contact (or the new kid).
`POST /v1/peers/{id}/test` fetches and verifies `/info` without pinning anything. Other health reasons:
`PEER_UNREACHABLE` (the stream reconnects from 5 s up to 5 min, and honors `Retry-After`) and `PEER_RESYNC`.
`POST /v1/peers/{id}/resync` re-snapshots and then tombstones the local records the peer no longer lists. Metrics:
`foresignal.federation.items{peer,result}` and `foresignal.federation.lag_seq{peer}`; MCP `list_peers` and
`peer_status` (admin).

### Edge mode

`FORESIGNAL_MODE=edge` keeps collection, the writer, triggers and notification channels, and leaves out
embeddings, LLM features (ask, briefings, the analyst), raster products and rendering, OGC, exports and the
federation feed; `GET /v1/system` reports `mode: edge` and those `features` off. Retention defaults to a 6 h track
window and a 1 d default window (only when you have stored no retention settings). Plan for a JVM heap of about
256 MB (`JAVA_TOOL_OPTIONS=-Xmx256m`) on a Raspberry Pi 4 or 5 with 2 GB or more.

The edge's `federation-push` sink (`config.url` = the hub's base URL, `config.peerId` = the edge's peer id on the
hub, `secrets.ingestKey` = the `fsp_` key the hub issued) sends signed, gzip-compressed batches of up to 1,000
items from a durable cursor. While the link is down the cursor holds and nothing is lost; when it comes back the
edge catches up in order (the usual sink rules on cursor clamps and resync apply). The hub pins the edge's key on
the first batch and refuses later batches signed by another key (`403 PEER_KEY_MISMATCH`).

### Compact messages over mesh and DTN

Fired and resolved alerts and intelligence events (never tracks or bulk records) are also queued as compact
CBOR messages of at most 160 bytes: the 084 mesh profile plus key 10 (this instance's 4-byte short id), key 11
(a message id) and key 12 (expiry in minutes). The queue sends critical messages first, newest first within a
severity, drops expired ones, and sends a message again only when its severity changes or it is resolved.

- **Mesh:** set `compactEvents: true` on a Meshtastic or MeshCore channel. Messages go out through the channel's
  `profile` (`cbor` or `text`) and its airtime governor, within `maxMessagesPerHour × 160` bytes an hour.
- **Store and forward:** `GET /v1/federation/bundle?since=` (a token with `federation: true`) returns the pending
  messages as one CBOR array, for sneakernet or DTN tooling such as a BPv7 bundle agent; available on an edge too.
  `POST /v1/federation/bundle` with a peer's ingest key accepts the same, as `remote-event` records on
  `peer-{id}-events`. Messages from this instance or already seen are dropped, and received messages are never
  sent on.

## Pattern of life (prompt 114)

Pattern of life learns where a tracked entity dwells and when, and reports deviations as records that rules can
match. **It is opt-in only**: nothing is analyzed until you create a subject.

```http
POST /v1/pol/subjects
{"subjectType": "record", "ref": "owntracks-phone/phone",
 "params": {"stayRadiusM": 100, "minDwell": "10m", "maxGap": "30m", "eps": 150, "minPts": 3, "lookback": "28d"}}
```

- **Subjects:** a record (`{sourceId}/{externalId}`), a 098 entity (every record it links) or a 036 watchlist
  (every record whose identity field holds a value on the list; at most 200 records per subject).
- **Stays:** consecutive positions within `stayRadiusM` of the stay's first fix, spanning at least `minDwell`; a gap
  longer than `maxGap` ends a stay. Positions are read incrementally from the stored versions, so a subject needs
  history in the TRACK window (downsampled ranges are fine: dwell uses observed-time gaps).
- **Places:** nightly at 03:00 in the server timezone (`PUT /v1/settings/server {"timezone": …}`), or on
  `POST /v1/pol/subjects/{id}/recompute`, the stays of the trailing `lookback` are clustered with DBSCAN (`eps`
  meters, `minPts` stays). Between runs a new stay joins the nearest place within its radius. Places keep their
  id and your label across runs; label one with `PUT /v1/pol/places/{id} {"label": "home"}`. Stays and places
  outlive the TRACK window; the `pol.retention` setting (default `180d`) removes them.
- **Records:** `pattern-of-life` publishes `pol-place` (one record per place: label, visits, dwell, typical hours)
  and `pol-deviation` with `deviationType`:
  - `NEW_PLACE`: a stay outside every place, once the subject has 14 days of history;
  - `UNUSUAL_TIME`: a stay at a known place arriving in an hour-of-week bucket that holds under 2% of the place's
    dwell;
  - `EXPECTED_ABSENT`: checked every 10 minutes; in a bucket where one place normally holds at least 60% of the
    subject's dwell, the subject has been elsewhere, or unseen, for more than an hour. Suppressed unless every
    member source is healthy, so an outage never reads as an absence.
- **Rule recipes:** `{"name": "Not home at night", "sources": ["pattern-of-life-private"], "kinds":
  ["pol-deviation"], "conditions": [{"path": "deviationType", "op": "==", "value": "EXPECTED_ABSENT"},
  {"path": "placeLabel", "op": "==", "value": "home"}], "mode": "match"}`; or a new place for a watched aircraft:
  `kinds: ["pol-deviation"]`, `deviationType == NEW_PLACE`.

**Privacy.** A subject with a member on a private source (039, such as an OwnTracks device) needs an admin token to
create, and is visible only to admin tokens. Its stays and places are stored at that source's precision cap, and
its records go to `pattern-of-life-private`, a private source: never served anonymously, never embedded, exported,
shared or federated. Deleting a subject deletes its stays and places and tombstones its records; purging an entity
(`POST /v1/records/{sourceId}/{externalId}/purge`) also deletes the subjects of that record and the stays other
subjects formed from its positions.

## Trips and commutes (prompt 114)

A trip watches the corridor along a route you supply, around a scheduled departure.

**Routing stays in your client.** Compute the route with your own router and send the line; Foresignal never calls a
routing service. To route around known hazards, ask a router that can exclude polygons: Valhalla accepts
`exclude_polygons` (for example the corridor hazards a dry-run check returned), OSRM cannot exclude areas. Then save
the resulting LineString (at most 5,000 vertices), or a 036 route place:

```http
POST /v1/trips
{"name": "Morning commute",
 "route": {"type": "LineString", "coordinates": [[-97.7431, 30.2672], [-97.6990, 30.3600]]},
 "bufferMeters": 2000,
 "schedule": {"type": "recurring", "days": ["MO","TU","WE","TH","FR"], "departTime": "07:45",
              "timezone": "America/Chicago"},
 "expectedDuration": "40m", "leadTime": "60m", "channelIds": ["<channel id>"],
 "anchor": "owntracks-phone/phone"}
```

- **Corridor:** the route buffered by `bufferMeters` on the ellipsoid (split at the antimeridian), stored on save.
- **Pre-trip briefing:** at `departAt − leadTime`, the hazards of `hazardKinds` (default weather alerts, road events,
  work zones, fire perimeters, cyclone cones, M4+ earthquakes, incidents) on the route, near it (within twice the
  buffer), and forecast hazards whose `validFrom` falls inside the trip, ordered along the route, are sent to the
  trip's channels (every channel subscribed to briefings when it names none). A clear route is sent too unless
  `notifyWhenClear` is false. `minSeverity` drops lower-rated hazards. `POST /v1/trips/{id}/check {"dryRun": true}`
  runs it now without sending; `GET /v1/trips/{id}/runs` lists past checks.
- **During the trip:** an ephemeral trigger rule tagged `trip-<trip id in lower case>` watches the corridor for new
  hazards (mode `enter`) from the check until `departAt + expectedDuration`, extended in 15-minute steps (up to twice
  the duration) while the anchor is still moving, then deleted. Its alerts follow your normal alert routing.
- **Anchor:** with a moving-anchor record, the briefing lists only hazards ahead of your position on the route,
  each with an ETA at your average speed over the last 10 minutes.
- Recurring departures keep their local time across daylight-saving changes.

## CPA/ETA and approach rules (prompt 114)

- `GET /v1/kinematics/cpa?a=aisstream/244660000&b=aisstream/244123000&horizon=1h`: the closest point of approach of
  two records under constant velocity (`dcpaMeters`, `tcpaSeconds` clamped to the horizon, vertical separation for
  aircraft). Speed and course come from the payload (`gs`/`track`, `sog`/`cog`, `speed`, `speedMs`/`courseDeg`),
  or from the last two stored positions. Pairs more than 500 km apart answer `400 CPA_RANGE`; states older than an
  hour `422 STATE_TOO_OLD`; `stale: true` marks a state older than twice its source TTL.
- `GET /v1/kinematics/eta?record=…&to=home` (or `to=-97.74,30.27`): approaching when the course is within 45° of the
  bearing at 0.5 m/s or more; then `etaSeconds = distance / (speed · cos Δ)`.
- `POST /v1/kinematics/cpa/batch`: one subject against up to 500 records of a filter (kinds, sources, bbox), the
  closest approaches first.
- **Approach rules:** a trigger condition `{"type": "approach", "target": {"placeId": "home"} | {"point": {...}} |
  {"record": "src/id"}, "withinMeters": 1852, "withinMinutes": 20}` fires `enter` when a watched record's CPA to the
  target is within the distance and the time. It is evaluated on each stored version of the rule's sources, so edge
  resolution is the source's poll or flush resolution; unknown speed, course or a stale target counts as not
  approaching. Approach conditions apply to record rules only and cannot be combined with `hysteresisMeters` or a
  moving anchor.

## Exercises and replay (prompt 115)

An exercise replays a past window of retained history through selected rules and delivers the matches as
clearly marked exercise alerts, so you can rehearse the whole alert chain (rule → notification → phone → action)
without touching real alert state.

**Rehearsal checklist**

1. Opt in the channels that should take part: set `acceptExercise: true` on them (`PUT /v1/channels/{id}`), or name
   them in the run's `channelIds`. Every other channel never receives an exercise alert. Good candidates are a test
   ntfy topic, your own phone's Web Push or native push, and a webhook pointed at the automation you want to
   rehearse.
2. Pick a window you remember (an evening with a known storm, a busy shipping morning) and the rules to replay
   (`ruleIds`, or a `tag`).
3. Project it first: `POST /v1/exercises` with `"dryRun": true` returns `stats.alertsByRule` and
   `stats.notificationsByChannel` without delivering anything.
4. Start it with a pace: `realtime`, `x10`, `x60` (the default) or `max`. The replay sleeps the scaled gap between
   the versions' ingest times, never more than 60 s ÷ the factor at once.
5. Watch `GET /v1/exercises/{id}`; stop it with `POST /v1/exercises/{id}/cancel` (it stops between matches).
6. Afterwards remove its alerts and notifications with `DELETE /v1/exercises/{id}/alerts`. They also expire on
   their own after `exercise.alertRetention` (7 days).

One exercise runs at a time (`409 EXERCISE_RUNNING`). Start, cancel and purge are audited, and channels subscribed
to system events get an `[EXERCISE]` start and finish notification.

**How an exercise is marked**

| Where | Marking |
|---|---|
| Every notification | title prefixed `[EXERCISE] `; body footer "This is an exercise replay of data from {from}–{to}. No action required." |
| Webhook | header `X-Foresignal-Exercise: true`, payload field `exercise: true` |
| Web Push, APNs, FCM | `exercise: true`; urgency `normal`, APNs `active`, FCM `NORMAL`, never time-sensitive or critical |
| CAP output | `status` `Exercise` |
| API, SSE and MCP | `exercise: true` and `exerciseRunId` on the alert; hidden unless `includeExercise=true` |

**What an exercise never touches**: real cooldowns, `maxAlertsPerHour` counters and storm state, real alerts'
lifecycle, escalation policies, proposed actions, federation, digests and briefings, rollups and statistics,
glance counts, and alert-quality statistics. Enrichment runs only when the run sets `enrich: true`.

**Replay limits**

- The window is at most `exercise.maxWindow` (7 days) and must lie inside retained history: a window that starts
  below any involved source's floor is refused with `400 REPLAY_BEYOND_RETENTION`, naming the source and its
  earliest replayable time. TRACK sources keep only `retention.trackWindow` (3 days) of raw history.
- Cooldowns, the hourly cap and active windows are simulated in run-local state, from the start of the window.
- Enter and exit come from each version's predecessor. A version whose predecessor lies before the window and was
  already removed by retention fires nothing; `stats.caveats` says so (`PREDECESSOR_OUTSIDE_WINDOW`).
- Count and absence rules replay at record level (`WINDOW_APPROXIMATE`); anomaly rules are not replayed and are
  listed in `stats.skippedRules`.
- Replays create no records, so trip briefings and pattern-of-life analyses are not triggered by an exercise.

## Tuning alert quality (prompt 115)

**Giving feedback.** Every alert takes one of `useful`, `noise`, `late` or `false_positive`:

- REST: `PUT /v1/alerts/{id}/feedback`;
- notification buttons: `feedback:useful` and `feedback:noise` (Telegram and Matrix show them by default; on ntfy
  and Web Push, which show two buttons, list them in `config.actions.list`, for example
  `["ack", "feedback:noise"]`);
- chat: `/feedback <alertId> <value>` (agent chats);
- MCP: `set_alert_feedback`; Ask: the model proposes it (`propose_feedback`) and you confirm.

**Implicit signals.** Without any feedback, Foresignal also records `ACK_FAST` (acknowledged within 15 minutes),
`SNOOZED` (the alert, or its rule while the alert was open, was snoozed), `FLAP` (resolved by the inverse edge within
10 minutes) and `UNOPENED` (its notification was not read within 24 hours; only when a device uses the inbox read
cursors). They are never stored as feedback. `signalScore` weighs them (+0.5, −0.5, −0.5, −0.25) per alert.

**Reading the quality view.** `GET /v1/triggers/{id}/quality?since=` shows feedback precision (with a Wilson
interval once five alerts are labeled), the signal counts and `signalScore` next to it (never mixed in), and
`daily[]`: per-day alerts, feedback counts, signals, median time to acknowledge, flaps and matches suppressed by
the cooldown or the rate cap. A nightly job (after 02:00 UTC) writes these rows and keeps them 400 days. Exercise
alerts never count. A briefing can include a weekly quality section with `scope.includeAlertQuality: true`.

**Suggestions.** `GET /v1/triggers/{id}/suggestions` generates and stores deterministic suggestions (tuning 1.1.0):
thresholds, cooldowns, enter mode, a smaller geofence, anomaly sensitivity, an `ACTIVE_WINDOW` when most negative
alerts fall in hours with few positives, `SEVERITY_UP` when many labels say `late`, and `SEVERITY_DOWN` for a
critical rule labeled mostly noise. `status=OPEN` lists the stored ones; `explain=true` adds a short plain-language
summary from the chat provider (budgeted; needs a token). Nothing changes a rule on its own.

- **Apply:** `POST /v1/triggers/{id}/suggestions/{suggestionId}/apply` with an optional `note` applies the patch as
  an ordinary rule update (the same validation and events as `PUT /v1/triggers/{id}`) and writes a tuning log
  entry with the rule before and after. A suggestion generated before the rule last changed is `STALE` and refused
  with `409 SUGGESTION_STALE`; generate suggestions again.
- **Dismiss:** `POST …/dismiss` with an optional `reason`.
- **Undo:** `POST /v1/triggers/{id}/tuning-log/{logId}/undo` restores the earlier definition while the rule is
  unchanged since the change (`409` otherwise). `GET /v1/triggers/{id}/tuning-log` lists the changes.


## Evidence and observability (prompt 116)

**Why independence matters.** One earthquake can arrive four times: from USGS, from EMSC, from GDACS and from a
news report. Four records are not four confirmations. GDACS republishes USGS for earthquakes, and the news report
cites one of them. Foresignal therefore separates the real-world event from the observations that describe it:

- every source maps to an **originator** (`intel/originators.json`, extended by the `intel.originators` setting or a
  descriptor's own `originator`): an agency, sensor network, aggregator, outlet, crowd, your own receiver, or
  Foresignal itself. Originators in one **independence group** (for example the community ADS-B networks, which share
  feeders) are never independent of each other, and an aggregator lists the originators it **republishes**;
- the evidence resolver links event-class observations (earthquakes, fires, conflict events, outages, disasters,
  weather alerts; `intel/canonical-rules.json`) to **canonical events**, using the corroboration tolerances
  (earthquakes 60 s, 100 km, 0.8 magnitude; fires 1 km, 1 h; conflict events the same 0.1° cell and UTC day; outages
  the same country or network with overlapping times) and, ahead of them, the upstream's own event id (a GLIDE
  number, the CAP identifier). Each link shows its confidence and why it was made; links below
  `evidence.minLinkConfidence` (0.5) are not made;
- observations that trace back to one originator form one **evidence group**. A news item that cites its source is
  judged by what it cites. Only evidence groups count as independent: in the example above, USGS and GDACS are one
  group and EMSC another, so the event has two independent confirmations, not four.

`GET /v1/evidence/events` lists canonical events (filter by `family`, `region`, `since` and `flags`);
`GET /v1/evidence/events/{id}` shows every observation, its originator, its link reason and the groups;
`GET /v1/evidence/of?source=&externalId=` shows where one record went. The `evidence` stream topic announces
`canonical.created`, `canonical.merged` and `canonical.flagged`. The corroboration layer (`derived-corroboration`)
is now one record per canonical event, and the intelligence-event scorer (1.1.0) counts signal families from
distinct evidence groups, so repeated reports never raise confidence or evidence quality.

**Quality flags** describe the shape of the evidence; they never say a report is false:

- `ECHO`: five or more observations of one evidence group within two hours: many re-reports of one origin;
- `SINGLE_ORIGINATOR`: only one evidence group;
- `TIMESTAMP_CONFLICT`: the observations disagree about when it happened by more than three times the family's
  tolerance;
- `LATE_REVISION`: a source moved the event's time or place after first publishing it.

**Detection probabilities.** A healthy source is not the same as a source that could have seen something. For each
source, family, place and threshold band Foresignal keeps time-versioned estimates of the probability of
detection (`p`, with an interval):

- **capture–recapture** for families with independent sources: of the events independent sources reported in the
  last year, the share this source also reported (at least 20 reference events; per region and, for earthquakes,
  per magnitude band such as `M4.5-5`);
- **presence proxy** for aircraft and vessels: how often a map cell had traffic in the source's active hours over
  the last 28 days, relative to its busiest cells (always low confidence); your own receivers get their
  **measured range** instead;
- **declared** footprints otherwise (`intel/observability.json` or the descriptor's `observability`): inside the
  declared area and above the declared threshold, for example USGS reports magnitude 2.5 in the United States but
  4.5 elsewhere. A source that declares nothing is assumed to observe everywhere; declare a footprint to restrict it.

The estimator runs weekly (Mondays from 04:30 UTC). Combined detection is `1 − Π(1 − p)` across independence
groups, never across sources of one group. A question about the past always uses the estimate that was valid then.
`GET /v1/observability?family=seismic&region=TR` (or `&bbox=` for map cells, `&at=` for a past time,
`&include=estimates` for the per-source numbers) answers how well instrumented a place is, and
`GET /v1/observability/tiles/{z}/{x}/{y}.mvt?family=` draws it as a map layer.

**Why some absences say `unknown`.** "Nothing happened" needs two things: every involved source healthy over the
window, and a combined detection probability of at least `observability.absenceMinDetect` (0.8). Otherwise an
absence rule, or a downward anomaly, reports `unknown` with reason `LOW_DETECTABILITY`, and never alerts: the
sources could not have seen it, so silence says nothing.

**Manifests and replay.** Every analytic output (anomaly evaluations, intelligence-event scores, recipe runs,
analyst runs, backtest jobs, observability estimates) points to an analysis manifest: the component and version,
the build, the configuration and its hash, the hashes of the registry files used, `asOfSeq`, the inputs and, for
LLM steps, the provider, model, prompt hash and prompt template. Identical manifests are stored once.
`GET /v1/manifests/{id}` shows one (agent), and `POST /v1/manifests/{id}/replay` queues a job (poll
`GET /v1/jobs/{jobId}`) that re-runs a deterministic component with exactly those inputs and reports `identical`,
`differs` (with what changed) or `not-replayable` (inputs purged by retention, a component that no longer exists,
or LLM output: "inputs recorded; model output not reproducible"). Every LLM role's system prompt is a versioned
template; `GET /v1/prompt-templates/{id}` shows exactly what the model was asked.

## Markets and attention (prompt 117)

Five sources add the market and attention signals the intelligence layer correlates with events. All of them write
`series-point` records (one per period) and one `series-latest` per series, so `/v1/series`, `get_series`, rules
and briefings work on them like on the energy series of 093. Every catalog entry now says what kind of number it is:

- `family`: `price`, `volume`, `open-interest`, `position`, `rate`, `attention`, `tone`, `count`, `index` or
  `structural`;
- `transform`: the hint for analysis (`log-return` for prices, `log-level` for counts and attention, `difference`
  for positions and storage, `level` otherwise);
- `releaseLagDays`: how long after its period a value is normally published. The point's `knownAt` is when
  Foresignal received it; its period is never back-dated, so analyses only use what was known at the time;
- `egress`: `OPEN` or `LOCAL_ONLY`; `regionCode` and `topicId` where they apply; `attribution`, the credit line.

**What each source gives.**

| Source | What | Lag | Needs |
|---|---|---|---|
| `cftc-cot` | Weekly futures positioning from the CFTC Commitments of Traders: open interest and the net position of each trader category (managed money, producers, swap dealers, asset managers, leveraged funds…) for twelve default contracts (`contracts`), from the disaggregated, legacy and TFF reports (`reports`; `futuresOnly=false` reads the futures-and-options reports). Series `cot.{code}.{report}.{measure}`, for example `cot.067651.disaggregated.mm_net` (WTI managed money net). | Positions as of Tuesday, published Friday 15:30 ET: 3 days | nothing; an optional Socrata `appToken` raises the rate limit |
| `fred-series` | Rates and prices from FRED: 10- and 2-year Treasury yields, the 10y−2y spread, the effective federal funds rate, the broad dollar index, WTI and Henry Hub spot prices (`series`). Series `fred.{id}`. | about 1 day | a free FRED API key (`PUT /v1/sources/fred-series/credentials` with `apiKey`) |
| `wikimedia-pageviews` | Daily Wikipedia pageviews of your attention topics' articles (human readers), and each project's daily top 100 articles (`attention-top`). Series `wiki.{project}.{article}`. | 1 day (read once a day after 02:30 UTC) | attention topics with articles |
| `gdelt-timelines` | News volume (matching articles per 15 minutes, with `extra.share` of everything GDELT monitored) and average tone per attention topic. Series `gdelt.{topicId}.vol` and `gdelt.{topicId}.tone`. | none | attention topics with a GDELT query |
| `worldbank-indicators` | Slow structural covariates per country and year: population, GDP per capita, infant mortality, urbanization, trade share, electricity access (`indicators`, from `fromYear`). Series `wb.{indicator}.{iso2}`. | about 180 days | nothing |

**Daily and weekly data cannot show intraday trading.** COT positions are a weekly snapshot and FRED prices are
daily closes: they can show that positioning moved before an event, never when within a day it moved. Market
findings say "unusual positioning before the event", never more.

**The FRED key and notice.** FRED is free with a key from <https://fred.stlouisfed.org/docs/api/api_key.html>.
FRED's terms require this notice wherever its data appears: "This product uses the FRED® API but is not endorsed or
certified by the Federal Reserve Bank of St. Louis." Foresignal carries it, with the series' own source line, in the
`attribution` of every FRED series and in the source's attribution.

**Copyrighted series and `LOCAL_ONLY`.** FRED republishes series owned by others (Cboe's VIX and OVX, ICE's Brent
price…). The whole `fred-series` source is `LOCAL_ONLY`: its raw values are served to this instance's own callers
allowed local egress and never leave through sinks, federation, share links or feeds; derived, standardized
statistics may. A series whose FRED notes mark it as copyrighted is skipped (the source shows `degraded` with the
skipped ids) until you list it in `allowCopyrighted`, which records that you accept it for personal use:

```
PUT /v1/sources/fred-series
{"params": {"series": ["DGS10", "VIXCLS"], "allowCopyrighted": ["VIXCLS"]}}
```

**Attention topics.** A topic names what you want to watch: `GET /v1/attention/topics` lists them, `POST` creates
one, `PUT /v1/attention/topics/{id}` replaces it and `DELETE` removes it (stored series stay). Enabled topics are
written into the `topics` parameter of `wikimedia-pageviews` and `gdelt-timelines` on every change, so enable those
two sources as well. Five examples ship disabled: `red-sea-shipping`, `strait-of-hormuz`,
`gulf-energy-infrastructure`, `taiwan-strait` and `ukraine-front`; enable one with a `PUT` that sets
`"enabled": true`. A topic has an id (`a-z`, `0-9` and `-`, 3 to 48 characters), a name, up to 20 Wikipedia articles
(`{"project": "en.wikipedia", "article": "Strait_of_Hormuz"}`), up to 30 keywords (the situation linker uses them),
040 region codes and themes.

**Writing a GDELT query** (at most 250 characters): quote phrases (`"Strait of Hormuz"`), and put alternatives joined
with `OR` in parentheses (`("Red Sea" OR "Bab el-Mandeb") (tanker OR Houthi)`); terms side by side must all match.
Foresignal checks the characters (balanced quotes and parentheses, no control characters) and, when you create a
topic while `gdelt-timelines` is enabled, makes one budgeted test call; a query GDELT does not answer with a
timeline is refused with `400 TOPIC_INVALID`. GDELT allows one request every 5 seconds, so a run covers at most five
topics (two requests each, 5 seconds apart) and takes the rest in turn on the next runs.

**Wikipedia's User-Agent policy** asks for contact details. Foresignal's User-Agent always carries the project URL;
set `contactEmail` with `PUT /v1/settings/server` and it is appended as `; contact=…` for every upstream. If
Wikimedia answers 403, the source shows `failing` with that reason.

**Backfilling COT history.** Live polling reads the last year. To load older reports:

```
POST /v1/sources/cftc-cot/backfill
{"from": "2010-01-01"}
```

The source must be enabled. The answer (202) is the job; the backfill reads older pages during the source's runs
and a tick every two minutes, at most five pages a run and never past the source's budget, so a history back to 2010 takes
some minutes. An interrupted backfill continues where its last successful run stopped, and running it again changes
nothing that is already stored (`409 BACKFILL_RUNNING` while one is open).

**Not built:** ICE's own weekly COT for Brent and gasoil (its website terms forbid automated collection), CME and ICE
end-of-day prices, UKMTO advisories and Metaculus. An external provider (EDP, prompt 024) can supply any of them
from your own subscription.
