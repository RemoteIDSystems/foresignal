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
| readsb/tar1090 parser | `source/formats/readsb/ReadsbAircraftMapper.java` | already parses the native `aircraft.json` format |
| Base-URL override | `source/adapters/adsblol/AdsbLolMilAdapter.java` (`BASE_URL_PROPERTY`) | adsb adapters already accept a base-URL override; blank = `https://api.adsb.lol` |
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

## C2. Local ADS-B aggregator (the durable fix for ADS-B)

adsb.lol / adsb.fi / airplanes.live are **public mirrors of the same readsb/tar1090 data Foresignal
already parses** (`ReadsbAircraftMapper`), and the adsb adapters already take a base-URL override
(`AdsbLolMilAdapter` `BASE_URL_PROPERTY`). So stand up **our own readsb** — fed by a local SDR receiver
and/or aggregating the global feed — and point the world/mil/region adapters at `http://localhost`.

Why this is strictly better than egress rotation for ADS-B, on every axis:

- the external rate limit **disappears** (no public endpoint is hit);
- true **1-second refresh** and **global coverage** — a local readsb serves the full `aircraft.json`
  at once, so the radius ≤ 250 world-tiling used against a public host (`RegionPoller`) is not needed;
- the **"one system pulling" exposure is gone entirely** — privacy included.

**Scope:** bundle/stand up readsb (a compose profile or documented setup); a first-class **"local
ADS-B aggregator"** source option (full global read from `localhost` through the existing mapper +
base-URL override); feeding docs (own receiver vs. aggregating the global feed); and an admin toggle
to make the local aggregator the ADS-B source. Once it runs, the public-mirror adapters (adsb.lol /
adsb.fi / airplanes.live) stay in the catalog but are **disabled by default** — a zero-setup fallback
only. Because GPS-jamming is **derived** from ADS-B (`derived-gps-jamming`), a local ADS-B feed makes
it first-party too, and `gpsjam` goes default-off.

## C3. First-party aggregators across the RF / sensor domains (the mission)

The same move applies to every domain where the data rides a distributed sensor/RF network and the
public sites are only aggregators. Build first-party ingestion for each; **keep the original
public/mirror sources in the catalog but default them OFF** once a first-party aggregator is live
(zero-setup, opt-in fallback — nothing is removed). Several already have reception plumbing to promote
rather than build:

| Domain | First-party source | Public mirror → default-off | Existing plumbing |
|---|---|---|---|
| **AIS (ships)** — the ADS-B twin | own RF receiver (RTL-SDR + AIS) and/or aggregate the AIS feed | aisstream, maritime mirrors | `adapters/ais`, `aisstream`, `maritime` *(new build)* |
| **Aircraft datalink** — ACARS/VDL/HFDL | `adapters/acars/LocalAcarsAdapter` + acarsdec/dumpvdl2/dumphfdl | airframes.io | *(promote)* |
| **APRS** | own igate via APRS-IS | APRS-IS public | `formats/aprs/`, `adapters/radio/` *(promote)* |
| **Direct satellite reception** | GOES / weather sats off a dish | NOAA mirrors | `adapters/satdump/`, `contrib/satdump/` *(promote)* |
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

Registered in `docs/Invariants.md` with their tests:

| Id | Invariant |
|---|---|
| EGR-4 | Every outbound source request still passes through `UpstreamClient` with its budget, backoff and per-host pacer, whichever egress route carries it; `direct` routes nothing; `Retry-After` and pacing are per route; a pinned LAN host is never routed; with no usable route a pull fails rather than going direct (R2-018) |
| EGR-5 | Egress route secrets are write-only: sealed with the instance key, never returned by the API and never logged (R2-018) |

Still to be registered by the prompts that build them:

| Id | Invariant |
|---|---|
| EGR-6 | A first-party local aggregator is `LOCAL_ONLY` by descriptor like the receiver it reads; promoting a domain to first-party never relaxes its egress class (keeps EGR-2) |
| EGR-7 | Superseded public sources are disabled, never removed; a default-off source still fetches nothing until an operator or a user enables it (keeps SRC-6) |
