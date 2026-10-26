# Network sourcing — local-first, with egress rotation as fallback

How Foresignal collects without depending on one host IP and one public aggregator: **host the feed
locally where it can be mirrored, and route around a single egress where it cannot.** This is the
design the Release 2 prompts (backend 181–186) build to; `docs/OpenAPI.json` (API **2.0.0**) is the
contract, and the running collector is what is true.

## The principle

Where a feed is **self-mirrorable**, host it locally and read from `localhost`. That removes the
external rate limit *and* the "one system pulling" exposure at the source — strictly better than
masking it. Where a source **cannot** be self-hosted (most news, social and API feeds), **egress
rotation** spreads and anonymizes the pulls.

So the two parts are ordered by preference: **first-party aggregation (C2, C3) is the durable fix;
egress rotation (C1) is the fallback** for what is left.

## What exists today (verified 2026-10-26)

| Capability | Where | Note |
|---|---|---|
| Polite pacing & backoff | `collect/HostPacer.java`, `collect/BackoffRegistry.java`, `collect/BudgetGovernor.java` | honors Retry-After; per-host gates; global pull governor |
| Outbound client | `collect/FetchExecutor.java`, `collect/UpstreamClient.java` | the one HTTP path (SRC-1); **every request leaves via the single host IP today** |
| readsb/tar1090 parser | `source/formats/readsb/ReadsbAircraftMapper.java` | already parses the native `aircraft.json` format |
| Base-URL override | `source/adapters/adsblol/AdsbLolMilAdapter.java:36,50` (`BASE_URL_PROPERTY`) | adsb adapters already accept a base-URL override; blank = `https://api.adsb.lol` |
| Local receivers | prompt 069 | socket transport + codec SPI (readsb v2, Davis/Ecowitt, AIS-catcher, …) |

**No egress routing exists today** — that is the gap C1 fills.

## C1. Egress routing & IP rotation (the fallback)

Route the collector's outbound requests through a configurable **egress pool** so the instance can
present multiple source IPs — for **privacy** (not exposing one operator IP/location), **resilience**
(failover across paths), **geo-access** (region-locked sources), and **distribution** (no single
supplier served everything from one address). Operator/global, gated `network.manage`.

- **Egress pool.** `EgressRoute` of `type` `http` / `socks5` / `wireguard` / `interface`: HTTP/SOCKS5
  **proxies** (the practical in-container mechanism — each a different exit IP: VPN exit nodes,
  self-hosted, or a provider), optional **WireGuard/VPN** profiles (host-level, where supported), and
  bound source **interfaces/IPs** (`boundInterface`). Proxy/VPN credentials are **write-only**
  (`EgressRouteInput.secrets`, never returned; `secretsSet` only names which are set) and stored
  encrypted with the install key, like every other secret — never in the app.
- **Rotation policy** (`EgressSettings.rotation`, operator-set): `round-robin`, `random`,
  `per-source-sticky`, or `rotate-on-rate-limit` (a source that 429s / Retry-After on one route moves
  to another). **Default `direct`** — egress off, opt-in.
- **Per-source assignment** (`PUT /v1/sources/{sourceId}/egress`, `SourceEgressAssignment`): pin a
  source to a route (e.g. a geo-locked source to an in-region exit) or `excludeFromRotation`.
- **Egress health** (`GET /v1/egress/{id}/health` → `EgressHealth`): per-route `reachable`, `latencyMs`,
  the public **`exitIp`** it presents, `consecutiveFailures`, and `backoffUntil`. Unhealthy routes are
  skipped and surfaced. `POST /v1/egress/{id}/test` (`EgressTestResult`) reports the observed exit IP
  from a live test call.
- **Responsible by design.** Each route still obeys the per-host pacer and still honors explicit
  Retry-After **per route** — rotation spreads legitimate collection and preserves privacy/resilience,
  it does **not** brute-force a provider's hard limits. Each source's terms of service still govern use.
- **Wiring:** `FetchExecutor` / `UpstreamClient` select the route per pull, so this is one integration
  point on the existing single HTTP path, not a second client (keeps SRC-1).

### API surface (C1)

| Operation | Method / path |
|---|---|
| `listEgressRoutes` / `createEgressRoute` | `GET` / `POST /v1/egress` |
| `getEgressRoute` / `updateEgressRoute` / `deleteEgressRoute` | `GET` / `PUT` / `DELETE /v1/egress/{id}` |
| `testEgressRoute` | `POST /v1/egress/{id}/test` (reports exit IP) |
| `getEgressRouteHealth` | `GET /v1/egress/{id}/health` |
| `getEgressSettings` / `updateEgressSettings` | `GET` / `PUT /v1/settings/egress` (policy + default route) |
| `assignSourceEgress` | `PUT /v1/sources/{sourceId}/egress` |

All `admin` scope, permission `network.manage`.

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

A new **System → Network / Sourcing** screen (admin, `network.manage`): manage the egress pool
(add a proxy URL + credentials, or import a WireGuard config; **Test** shows the exit IP), set the
rotation policy + default route, assign per-source egress, watch live egress health — and, in the same
place, the **first-party aggregators**: setup, endpoints, live status, and the **default-off toggle**
for each superseded public source (ADS-B, AIS, datalink, satellite, ground sensors).

## Invariants this design must keep

Extend `docs/Invariants.md` (the implementing prompts register them with test names):

| Id | Invariant |
|---|---|
| EGR-4 | Every outbound request still passes through `UpstreamClient` and the per-host pacer and backoff, whichever egress route carries it (extends SRC-1, SRC-3); rotation never bypasses Retry-After |
| EGR-5 | Egress route credentials are write-only: stored encrypted with the install key, never returned by the API and never logged (extends SEC-3, SEC-5) |
| EGR-6 | A first-party local aggregator is `LOCAL_ONLY` by descriptor like the receiver it reads; promoting a domain to first-party never relaxes its egress class (keeps EGR-2) |
| EGR-7 | Superseded public sources are disabled, never removed; a default-off source still fetches nothing until an operator or a user enables it (keeps SRC-6) |
