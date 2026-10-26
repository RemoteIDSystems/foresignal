# Network sourcing — local-first, with egress rotation as fallback

How Foresignal collects without depending on one host IP and one public aggregator: **host the feed
locally where it can be mirrored, and route around a single egress where it cannot.** The Release 2
prompts R2-018…R2-023 build this; `docs/OpenAPI.json` (API **2.0.0**) is the contract, and the running
collector is what is true.

## The principle

Where a feed is **self-mirrorable**, host it locally and read from `localhost`. That removes the
external rate limit *and* the "one system pulling" exposure at the source — strictly better than
masking it. Where a source **cannot** be self-hosted (most news, social and API feeds), **egress
rotation** spreads and anonymizes the pulls.

So the two parts are ordered by preference: **first-party aggregation (C2, C3) is the durable fix;
egress rotation (C1) is the fallback** for what is left.

## What exists

| Capability | Where | Note |
|---|---|---|
| Polite pacing & backoff | `collect/HostPacer.java`, `collect/BackoffRegistry.java`, `collect/BudgetGovernor.java` | honors Retry-After; per-host gates; global pull governor |
| Outbound client | `collect/UpstreamClient.java`, `collect/BoundSourceHttp.java` | the one HTTP path for source requests (SRC-1) |
| Egress routing (C1, R2-018) | `egress/` (`EgressRouter`, `RouteSelector`, `EgressBackoff`, `EgressHealthRegistry`, `EgressTunnel`, `EgressProbe`), `api/EgressController.java` | default `direct`: every request leaves from the host's own address until an operator turns rotation on |
| readsb/tar1090 parser | `source/formats/readsb/ReadsbAircraftMapper.java` | parses the native `aircraft.json` format |
| Aggregator surface (R2-019) | `source/aggregator/` (`AggregatorService`, `DomainAggregator`), `api/AggregatorsController.java` | one surface for every domain; `adsb`, `ais`, `datalink` and `satellite` implemented |
| Local ADS-B aggregator (C2, R2-019) | `source/aggregator/AdsbAggregator.java`, `source/adapters/local/LocalAdsbAggregatorAdapter.java`, the `readsb` compose profile | reads the operator's own readsb whole |
| Local AIS aggregator (C2b, R2-020) | `source/aggregator/AisAggregator.java`, `source/adapters/ais/LocalAisAggregatorAdapter.java` | reads the operator's own AIS decoder's NMEA feed whole, over TCP or UDP |
| Direct satellite reception (C2d, R2-022) | `source/aggregator/SatelliteAggregator.java`, `source/adapters/satdump/SatDumpAggregatorAdapter.java` | a SatDump station of the operator's own, always registered |
| Local datalink aggregator (C2c, R2-021) | `source/aggregator/DatalinkAggregator.java`, `source/adapters/acars/LocalAcarsAggregatorAdapter.java`, `source/adapters/radio/LocalAprsAggregatorAdapter.java` | reads the operator's own acars_router and igate TNC |
| Local receivers | prompt 069 | socket transport + codec SPI (readsb v2, Davis/Ecowitt, AIS-catcher, …) |

## C1. Egress routing & IP rotation (the fallback, R2-018)

Route the collector's outbound requests through a configurable **egress pool** so the instance can
present several source addresses — for **privacy** (not exposing one operator IP or location),
**resilience** (failover across paths), **geo-access** (region-locked sources), and **distribution**
(no single supplier served everything from one address). Operator-global, permission `network.manage`.

### The pool

An egress route (`egress_route`, migration `V23__egress_route.sql`) has a `name` (unique ignoring
case), a `type`, `enabled`, and per type:

| Type | Configuration | How a request leaves |
|---|---|---|
| `http` | `url` = `http://host:port` | without credentials, the JDK's own HTTP proxy support; with `secrets.username`/`password`, the route's loopback bridge sends `CONNECT` with `Proxy-Authorization: Basic` |
| `socks5` | `url` = `socks5://host:port` | the route's loopback bridge: RFC 1928 with RFC 1929 username/password when set, connecting **by host name** so the proxy resolves the target |
| `interface` | `boundInterface` = an interface name (`eth1`) or a local address literal | the client binds that local address (`HttpClient.Builder.localAddress`); a name takes its first address, IPv4 first, and must be up |
| `wireguard` | `secrets.wireguardConfig` (a profile with an `[Interface]` section) | **none**: the route is recorded with its profile sealed, and always reported unavailable, because the server cannot bring a tunnel up. Bring the tunnel up on the host and add its interface as an `interface` route |

A proxy URL holds only scheme, host and port; credentials go in `secrets`. Its host must pass the
outbound URL policy's `LAN_ALLOWED` profile (`400 URL_NOT_ALLOWED` otherwise), so a proxy may be on the
LAN or the internet but not on a link-local or metadata address.

**Secrets are write-only.** Username, password and WireGuard profile are sealed together with the
instance key (`CredentialCipher`), never returned, and only named in `secretsSet`. On a replace an
omitted secret keeps its value, an empty string or `null` clears it, and a secret the new type cannot
use is dropped. The route view also says whether this host can use it (`available`,
`unavailableReason`).

**Why a bridge.** `java.net.http.HttpClient` sends through an HTTP proxy only: it ignores a SOCKS proxy
(and would then connect direct), and does not apply proxy authentication to an https `CONNECT` by
default. So a SOCKS5 route, and an HTTP route with credentials, get a `CONNECT` listener bound to the
loopback address; the route's client uses it as a plain HTTP proxy and the bridge opens each tunnel
upstream. The bridge carries **https only**: a plain-http request through such a route is answered 502
and fails — it is never sent direct instead. The listener is closed when the route changes or is
deleted.

### Rotation policy

`EgressSettings` (settings key `egress`): `rotation` and an optional `defaultRoute`, which must name an
existing route.

| `rotation` | Behavior |
|---|---|
| `direct` (default) | **Off.** No request is routed — pins and the default route included. This is exactly the collector's behavior before R2-018 |
| `round-robin` | each pull of a source takes the next open route, per source |
| `random` | each pull takes a random open route |
| `per-source-sticky` | a source always uses the same route (by a hash of its id); while that route has throttled it, it waits rather than moving |
| `rotate-on-rate-limit` | a source stays on one route until that route throttles it, then moves to the next open route |

"Open" means usable (enabled, available on this host, not held off as unhealthy) and not throttled for
that source. **One pull keeps one route** while it stays open, so a multi-request pull leaves from one
address.

**Per-source assignment** (`PUT /v1/sources/{sourceId}/egress`, stored on the source's settings row):

- **`routeId`** pins the source to a route (a geo-locked source to an in-region exit). While that route
  has throttled the source it waits; while the route is unusable the pull **fails** — a pin is never
  moved to another exit.
- **`excludeFromRotation`** keeps an unpinned source on the default route, or **direct** when there is no
  default route.

**Fail closed.** With rotation on and no usable route for a source, its pull fails with an explanation
in the fetch log; it is never sent direct. Turning rotation on never silently exposes the host's own
address. When every route a source may use has throttled it, the source is in backoff until the
earliest of those holds ends.

### Responsible by design

Rotation spreads legitimate collection; it does not brute-force a provider's limits. Each source's terms
of service still govern its use.

- **Same path, same rules.** A routed request is still sent by `UpstreamClient` after the source's
  backoff and budget checks, with the same User-Agent, byte cap, metering and redaction.
- **Pacing per egress.** The per-host pacer is keyed by host *and* route (`host@egress:{routeId}`), so
  each address still paces each host, and one route's gate does not hold back another's.
- **`Retry-After` per egress.** Through a route, a 429 or any `Retry-After` holds **that source off that
  route** (`egress_backoff`, persisted across restarts) with the same arithmetic as the source-level
  backoff; the route stays open to other sources and the source may use its other routes. A refusal
  (451, or 403 where the descriptor says access denied is terminal) still holds the source everywhere —
  no other egress is tried for a refused client — and a 503 without `Retry-After`, an upstream outage
  rather than a limit on one address, still uses the source's own backoff.
- **DNS pinning holds.** A request to a pinned LAN host (a `LAN_URL` parameter) is never routed:
  re-resolving it through a proxy would undo the pinning.
- **Streams.** A WebSocket opened through a route uses the route's client. A permessage-deflate WebSocket
  opens its own socket and cannot leave through a route, so it is refused while the source would be
  routed; exclude such a source from rotation with no default route to open it direct.

### Health and test

- **Health** is held in memory per route (it starts empty at restart): `reachable`, `latencyMs`,
  `exitIp`, `lastCheckedAt`, `consecutiveFailures`, `backoffUntil`, `lastError`. A request whose
  connection through the route fails counts as a failure; any answer from the upstream counts as a
  success. **Three consecutive failures** mark the route unreachable: the selector skips it, with a
  growing hold, and `system.egress_unhealthy` is raised once. The first success after that raises
  `system.egress_recovered`. `GET /v1/egress/{id}/health` adds `sources`: the sources held off the
  route and until when.
- **Test** (`POST /v1/egress/{id}/test`) makes one `GET` of the exit-IP service through the route and
  reports the public address it saw (`exitIp`), the latency, or why it failed. The service is the
  operator's configuration, `foresignal.egress.probe-url`, default `https://checkip.amazonaws.com/`
  (run by Amazon Web Services), checked against the `PUBLIC_ONLY` outbound policy — never a URL from a
  request body or from observed content. The answer must be one address literal. A passing test
  records the exit IP on the route.

### API surface (C1)

| Operation | Method / path |
|---|---|
| `listEgressRoutes` / `createEgressRoute` | `GET` / `POST /v1/egress` |
| `getEgressRoute` / `updateEgressRoute` / `deleteEgressRoute` | `GET` / `PUT` / `DELETE /v1/egress/{id}` |
| `testEgressRoute` | `POST /v1/egress/{id}/test` (reports the exit IP) |
| `getEgressRouteHealth` | `GET /v1/egress/{id}/health` |
| `getEgressSettings` / `updateEgressSettings` | `GET` / `PUT /v1/settings/egress` (policy + default route) |
| `assignSourceEgress` | `PUT /v1/sources/{sourceId}/egress` |

All `admin` scope, permission `network.manage`. Deleting a route clears the pins to it and the default
route when it was that one.

## The aggregator surface (R2-019)

Every RF/sensor domain gets one **first-party aggregator**, configured through one surface
(`network.manage`):

| Operation | Method / path |
|---|---|
| `listAggregators` | `GET /v1/aggregators` |
| `getAggregator` / `updateAggregator` | `GET` / `PUT /v1/aggregators/{domain}` |
| `testAggregator` | `POST /v1/aggregators/{domain}/test` |

`{domain}` is `adsb`, `ais`, `datalink`, `satellite` or `ground-sensors`; anything else is
`404 UNKNOWN_AGGREGATOR`. Each domain's configuration is one setting, `aggregator.<domain>`:
`enabled` (default false), `mode` (`receiver`, `feed` or `hybrid` — how the local service is fed),
`endpoint` (the local URL its source reads), a domain-specific `config`, and
`disableSupersededSources` (default true). A PUT replaces it whole.

Behind the surface is one `DomainAggregator` per domain (`ai.foresignal.source.aggregator`). It names
the first-party sources it drives (the health of the first of them that is enabled is the aggregator's
`status`: `ok`, `degraded`, `down`, or `unconfigured` when it is not enabled with an endpoint), the public sources it
supersedes, how a configuration is validated and applied, and how its endpoint is tested. A domain
whose backend this server does not have yet reads `implemented: false`; its configuration can be
stored, but only disabled.

**Supersede, never remove.** When an update leaves an aggregator **live** (enabled with an endpoint)
with `disableSupersededSources`, every superseded source that is on is turned off and remembered.
While it stays live, seeding never enables them and `enable-core`/`enable-free` skip them. They stay
registered and in the catalog, and an operator may still turn one on by hand. When the aggregator
stops being live, or `disableSupersededSources` is set to false, exactly the sources it turned off
are turned back on. A live aggregator whose source starts failing raises `system.aggregator_down`
once, and `system.aggregator_recovered` when it is healthy again and none of its other enabled sources
is failing (since R2-021, any of a domain's sources counts, not only the first).

## C2. Local ADS-B aggregator (the durable fix for ADS-B, R2-019)

adsb.lol, adsb.fi and airplanes.live are public mirrors of readsb data, the format Foresignal already
parses (`ReadsbAircraftMapper`). A readsb of the operator's own serves the same `aircraft.json` for
everything it holds, so Foresignal reads it whole from this host or the LAN:

- the external rate limit **disappears** (no public endpoint is hit);
- **one request** brings every aircraft the readsb holds, so the world tiling used against a public
  host (`RegionPoller`, six circles a poll) is not needed;
- refresh is as fast as the readsb writes (1 s by default upstream; the read runs every 5 s by default,
  1 s at the fastest);
- the **"one system pulling" exposure is gone** — privacy included.

**The read.** `local-adsb-aggregator` is a `local-adsb-{n}` adapter with its own descriptor: the same
mapper, staleness rule and policy, sized for a large feed (32 MB, 10 s timeout). The aggregator owns
its configuration: `PUT /v1/aggregators/adsb` writes the endpoint into its `url` parameter
(`config.band`, `1090` or `978`, into `band`) and turns it on while the aggregator is live, off
otherwise. The endpoint is therefore checked like every `LAN_URL`: an http(s) URL naming
`aircraft.json`, with no credentials, query or fragment, that resolves to a loopback, private or
unique-local address; it is pinned, and never sent through an egress route. Its id carries the
`local-adsb-` prefix, so `derived-aircraft`, `derived-gps-jamming` and the other derived aircraft
layers read it like any local receiver. Like every local receiver it is `LOCAL_ONLY`, overridable.

The public-mirror adapters are not retargeted at the local readsb: they request the mirrors' own API
paths (`/v2/lat/…/lon/…/dist/…`, `/v2/mil`), which a readsb does not serve, and they are exactly
the sources the aggregator supersedes.

**Superseded:** every registered `adsblol-*`, `adsbfi-*` and `airplaneslive-*` source, and
`gpsjam-daily` — GPS jamming is first-party once `derived-gps-jamming` reads the local feed.

**Standing up readsb.** Foresignal bundles no readsb code (readsb is GPL-3.0). The opt-in `readsb`
compose profile pulls the sdr-enthusiasts ultrafeeder image (readsb with tar1090), whose web server
answers on container port 80 with `/data/aircraft.json`:

```sh
READSB_LAT=51.47 READSB_LON=-0.45 READSB_DEVICE_TYPE=rtlsdr docker compose --profile readsb up -d
```

then

```
PUT /v1/aggregators/adsb
{"enabled": true, "mode": "receiver", "endpoint": "http://readsb/data/aircraft.json"}
POST /v1/aggregators/adsb/test        → {"ok": true, "reachable": true, "sampleCount": 214, …}
```

- **`receiver`** — the readsb decodes this host's own SDR (`READSB_DEVICE_TYPE=rtlsdr`; the profile
  maps `/dev/bus/usb`).
- **`feed`** — the readsb aggregates feeds it is given rather than a radio: `ULTRAFEEDER_CONFIG` takes
  `adsb,<host>,<port>,beast_in` connectors (separated by `;`), for example other receivers on the LAN
  or a feed partner that sends you Beast data. Which global feeds may be aggregated, and on what
  terms, is up to their operators; Foresignal reads whatever the readsb holds.
- **`hybrid`** — both.

The server reads the same file in every mode; `mode` records how the readsb is fed. The image tag
defaults to the one its README recommends (`latest`); pin a digest with `READSB_IMAGE`. Its tar1090
map is published on `127.0.0.1:8078`.

## C2b. Local AIS aggregator (R2-020)

AIS is the ADS-B twin: a distributed RF network the public sites merely aggregate. aisstream.io is a
public mirror of the same AIVDM messages an AIS decoder of the operator's own produces, so Foresignal
reads the decoder's NMEA feed whole from this host or the LAN, with the `aivdm` codec the
`local-ais-{n}` slots already use (prompt 085).

**The read.** `local-ais-aggregator` is a `LocalAisAdapter` with its own descriptor: the same decoding,
station parameters, statistics (`messagesPerMin`, `distinctMmsi10m`, `lastMessageAt`, `vessels`) and
`LOCAL_ONLY`, overridable egress. Its `endpoint` is either

- **`tcp://host:port`** — the decoder's TCP server (AIS-catcher `-S <port>`, rtl-ais `-T`); a client
  connection, receive-only, pinned to a loopback, private or unique-local address; or
- **`udp://bindHost:port`** — where this server listens for the decoder's datagrams (AIS-catcher
  `-u <host> <port>`; rtl-ais sends to `127.0.0.1:10110` by default). `config.allowSenders` is then
  required: the IPs or CIDRs of the decoder hosts, private space only; any other sender's datagram is
  dropped unread. A datagram may carry several NMEA lines.

`PUT /v1/aggregators/ais` writes the endpoint, `config.allowSenders` and `config.stationName` (the
observer of every record, default `local`) into the read, and turns it on while the aggregator is live
and off otherwise. Its id carries the `local-ais-` prefix, so every derived vessel layer that reads
`DerivedSources.VESSEL_PREFIXES` (`derived-ais-gap`, loitering, rendezvous, port calls, chokepoints, …)
reads it like any local receiver.

```
PUT /v1/aggregators/ais
{"enabled": true, "mode": "receiver", "endpoint": "tcp://ais-catcher:5012"}
POST /v1/aggregators/ais/test         → {"ok": true, "reachable": true, "sampleCount": 37, …}
```

**Test.** `testAggregator` listens to the stored endpoint for 10 s through the probe service — the
read's own socket path, budget and pinning — and reports the vessel records decoded (at most 100). A
UDP port the live read already holds cannot be bound a second time, so a live UDP aggregator is
reported from the read's statistics instead (`sampleCount` = distinct MMSIs in the last 10 minutes).

- **`receiver`** — the decoder reads this host's or a LAN host's own SDR.
- **`feed`** — the decoder aggregates NMEA feeds it is given (other stations, a feed partner).
- **`hybrid`** — both. The server reads the same stream in every mode.

**Superseded:** `aisstream`, the only public global-AIS mirror registered. **Not superseded:** the
official regional feeds `ais-digitraffic` (Fintraffic) and `ais-kystverket` (Norwegian Coastal
Administration) — authorities, not mirrors — the `local-ais-{n}` slots, and the maritime event sources
(`gfw-events`, `nga-msi-warnings`, `imf-portwatch-*`), which are not AIS mirrors. Foresignal bundles no
decoder (AIS-catcher is GPL-3.0); run it on the host or the LAN.

## C2c. Local datalink aggregator (R2-021)

Aircraft datalink (ACARS, VDL Mode 2, HFDL) and APRS both ride distributed RF networks whose public
sites aggregate what stations like the operator's hear. The `datalink` aggregator reads the operator's
own decoders through two reads, each a slot adapter with its own descriptor:

| Read | Decoder | Like |
|---|---|---|
| `local-acars-aggregator` | acars_router's TCP servers, fed by acarsdec, dumpvdl2 and dumphfdl (acars_router's defaults 15550, 15555, 15556) | `local-acars-{n}`: metadata only, message text never stored; private by descriptor; `LOCAL_ONLY`, **not** overridable |
| `local-aprs-aggregator` | the igate's TNC, KISS over TCP (Direwolf's `KISSPORT`, conventionally 8001) | `local-aprs-{n}`: provably receive-only; registers packets so `aprs-is` drops its igated copy; `LOCAL_ONLY`, overridable |

`endpoint` is the decoder host, `tcp://host` with no port. `config` selects the decoders: `acars`,
`vdlm2`, `hfdl` and `aprs`, each a port on that host or a whole `tcp://host:port` on another, plus
`stationId` (the ACARS observer), `stationCallsign` (the APRS observer) and `includeMessages`. No port
is assumed. Enabling needs at least one decoder. Each read is turned on while the aggregator is live
with one of its decoders selected, and off otherwise; its endpoints are checked and pinned like every
`LAN_URL`.

```
PUT /v1/aggregators/datalink
{"enabled": true, "mode": "receiver", "endpoint": "tcp://acars-router",
 "config": {"acars": 15550, "vdlm2": 15555, "aprs": "tcp://192.168.1.20:8001", "stationCallsign": "N0CALL-10"}}
POST /v1/aggregators/datalink/test    → {"ok": true, "reachable": true, "sampleCount": 12, …}
```

**Test.** `testAggregator` listens to every selected read at once for 10 s through the probe service
and sums the messages decoded. Datalink traffic is sparse: a quiet sky reads `reachable: true,
ok: false`.

**Superseded:** `aprs-is`, the public APRS-IS network. **airframes.io** is the public ACARS/VDL/HFDL
aggregator, but no airframes.io source is registered, so nothing is superseded for ACARS, VDL Mode 2
and HFDL.

## C2d. Direct satellite reception (R2-022)

GOES and the polar weather satellites broadcast their imagery; the NOAA buckets Foresignal otherwise
pulls (`goes-east-abi`, `goes-west-abi`) mirror what a dish and SatDump receive directly. The SatDump
ground station plumbing exists since prompt 049; the `satellite` aggregator promotes it.

**The read.** `satdump-aggregator` is a `satdump-{station}` source for the reserved station name
`aggregator` (`FORESIGNAL_SATDUMP_STATIONS` refuses it), registered whether or not any station is
configured at startup, with exactly a station's loops: SatDump's `/api` (station state, RF sessions,
the live polar plot) and its output directory (passes, products, xRIT frames, DCS and EMWIN).
`PUT /v1/aggregators/satellite` writes it:

- `endpoint` → `apiUrl`, SatDump's web server (`http://host:port`, any base path kept; checked and
  pinned as a `LAN_URL`);
- `config.location` → `location` (required with an endpoint: `lat,lon[,altM]` or a place id);
- `config.inboxPath` → `inboxPath` (SatDump's output directory under `FORESIGNAL_INBOX_DIR`);
- `config.satdumpVersion` (`1.2` or `2.0`) and `config.predictNorad` (the polar satellites whose
  passes are predicted);
- `config.goes`: `east` or `west`, the GOES the dish receives.

It is turned on while the aggregator is live, off otherwise. Its redistribution is `LOCAL_ONLY`
through its license (`operator-owned`), as for every SatDump station.

```
PUT /v1/aggregators/satellite
{"enabled": true, "mode": "receiver", "endpoint": "http://satdump:8081",
 "config": {"location": "51.47,-0.45,30", "inboxPath": "satdump/home", "goes": "east"}}
POST /v1/aggregators/satellite/test   → {"ok": true, "reachable": true, "sampleCount": 3, …}
```

**Test.** `testAggregator` runs the station for 10 s through the probe service and counts the records
it produced: station state, passes and products.

**Superseded:** only the ABI imagery pull of the GOES named by `config.goes` (`goes-east-abi` or
`goes-west-abi`); a polar-only station supersedes nothing. **Not superseded:** `goes-glm` and
`goes-fdc` — GLM lightning and ABI fire detection are not in the HRIT broadcast a dish receives, and
the local GLM from a GRB station (`glm-local-{station}`) is built to run alongside `goes-glm`. Since
this domain's superseded set depends on its configuration, `DomainAggregator` gained
`supersededSourceIds(settings)`; `supersededSources` in the view is the set the current configuration
supersedes.

## C3. First-party aggregators across the RF / sensor domains (the mission)

Each domain plugs its backend into the aggregator surface above (R2-020…R2-023 add no operations). The same move applies to every domain where the data rides a distributed sensor/RF network and the
public sites are only aggregators. Build first-party ingestion for each; **keep the original
public/mirror sources in the catalog but default them OFF** once a first-party aggregator is live
(zero-setup, opt-in fallback — nothing is removed). Several already have reception plumbing to promote
rather than build:

| Domain | First-party source | Public mirror → default-off | Existing plumbing |
|---|---|---|---|
| **AIS (ships)** — the ADS-B twin | own RF receiver (RTL-SDR + AIS) and/or aggregate the AIS feed | aisstream | **built in R2-020 (C2b)** |
| **Aircraft datalink** — ACARS/VDL/HFDL | `adapters/acars/LocalAcarsAdapter` + acarsdec/dumpvdl2/dumphfdl | airframes.io (no source registered) | **built in R2-021 (C2c)** |
| **APRS** | own igate's TNC | APRS-IS public (`aprs-is`) | **built in R2-021 (C2c)** |
| **Direct satellite reception** | GOES / weather sats off a dish | NOAA ABI imagery pulls (`goes-east-abi`, `goes-west-abi`) | **built in R2-022 (C2d)** |
| **Seismic** | Raspberry Shake | — | `contrib/rsudp/` *(promote)* |
| **Weather-radio alerts** | SAME/EAS | — | `contrib/same/` *(promote)* |
| **Radiosondes** | `radiosonde_auto_rx` | SondeHub | *(new)* |
| **Air quality** | local sensor | public AQ mirrors | `env/airq` *(promote)* |
| **Personal weather stations** | Davis/Ecowitt (prompt 069) | WU/Ecowitt cloud | *(promote)* |
| **Mesh** | Meshtastic/MeshCore local | public map | `contrib/meshcore-bridge/` *(promote)* |

The **default-off rule** is the same throughout: a superseded public source is never deleted, only
disabled by default, so a fresh instance with no receiver still works and an operator who stands up a
receiver flips to first-party with one toggle. Local receivers remain `LOCAL_ONLY` by descriptor
(EGR-2) — standing up a first-party feed does not change a source's egress class.

## Admin surface

A **System → Network / Sourcing** screen (admin, `network.manage`): manage the egress pool (add a proxy
URL + credentials, a bound interface, or record a WireGuard profile; **Test** shows the exit IP), set
the rotation policy + default route, assign per-source egress, watch live egress health — and, in the
same place, the **first-party aggregators**: setup, endpoints, live status, and the **default-off
toggle** for each superseded public source (ADS-B, AIS, datalink, satellite, ground sensors).

## Invariants

All four are registered in `docs/Invariants.md` with their tests:

| Id | Invariant |
|---|---|
| EGR-4 | Every outbound source request still passes through `UpstreamClient` with its budget, backoff and per-host pacer, whichever egress route carries it; `direct` routes nothing; `Retry-After` and pacing are per route; a pinned LAN host is never routed; with no usable route a pull fails rather than going direct (R2-018) |
| EGR-5 | Egress route secrets are write-only: sealed with the instance key, never returned by the API and never logged (R2-018) |
| EGR-6 | A first-party local aggregator's read is `LOCAL_ONLY` by descriptor like the receiver it reads; promoting a domain to first-party never relaxes its egress class (keeps EGR-2) (R2-019; `local-ais-aggregator` in R2-020; `local-acars-aggregator`, `local-aprs-aggregator` in R2-021) |
| EGR-7 | Superseded public sources are turned off, never removed; while their aggregator is live, seeding and the bulk enables leave them off, and turning it off turns back on exactly those it turned off (keeps SRC-6) (R2-019) |
