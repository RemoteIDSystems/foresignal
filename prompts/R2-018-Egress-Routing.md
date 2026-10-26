# R2-018 — Egress routing & IP rotation

**Context.** v2.0 (R2) backend, Part C (network sourcing). Permission `network.manage` (admin). Depends on
the identity/RBAC layer (R2-001…R2-013) for the permission and on the existing collector plumbing. This is
the **fallback for sources that cannot be self-hosted** — the first-party aggregators (R2-019…R2-023) are the
durable fix where a feed is self-mirrorable. Default **direct (off, opt-in)**.

**Goal.** Route the collector's outbound pulls through a configurable **egress pool** so the instance can
present multiple source IPs — for privacy (not exposing one operator IP/location), resilience (failover across
paths), geo-access (region-locked sources), and spreading collection so no single supplier is served
everything from one address. **Responsible by design:** each egress still obeys the per-host pacer and still
honors explicit `Retry-After` *per egress*; rotation spreads legitimate collection, it does not brute-force a
provider's hard limits, and each source's terms of service still govern use.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-018`)

| operationId | method + path | scope |
|---|---|---|
| `listEgressRoutes` | `GET /v1/egress` | admin · `network.manage` |
| `createEgressRoute` | `POST /v1/egress` | admin · `network.manage` |
| `getEgressRoute` | `GET /v1/egress/{id}` | admin · `network.manage` |
| `updateEgressRoute` | `PUT /v1/egress/{id}` | admin · `network.manage` |
| `deleteEgressRoute` | `DELETE /v1/egress/{id}` | admin · `network.manage` |
| `testEgressRoute` | `POST /v1/egress/{id}/test` | admin · `network.manage` |
| `getEgressRouteHealth` | `GET /v1/egress/{id}/health` | admin · `network.manage` |
| `getEgressSettings` | `GET /v1/settings/egress` | admin · `network.manage` |
| `updateEgressSettings` | `PUT /v1/settings/egress` | admin · `network.manage` |
| `assignSourceEgress` | `PUT /v1/sources/{sourceId}/egress` | admin · `network.manage` |

Schemas authored: `EgressRoute`, `EgressRouteInput`, `EgressRoutePage`, `EgressHealth`, `EgressTestResult`,
`EgressSettings`, `SourceEgressAssignment`.

## Read first

- `collect/FetchExecutor.java` (the single fetch-execution chokepoint — `execute(...)`, the `PullGovernor`
  permit, `HostPacer`, `BackoffRegistry`), `collect/BoundSourceHttp.java` (where the per-source `java.net.http.HttpClient`
  is built — the natural place to attach a proxy/bound interface), `collect/HostPacer.java`,
  `collect/BackoffRegistry.java` (honors `Retry-After`), `collect/BudgetGovernor.java`.
- `net/OutboundProxies.java`, `net/OutboundUrlPolicy.java`, `net/PinnedHttp.java`, `net/PinnedTls.java`,
  `net/HostResolver.java` (current single-proxy handling and the DNS-pinning the egress client must preserve).
- `security/CredentialCipher.java` + `security/SecretKeyStore.java` + `security/Encrypted.java` (encrypt
  proxy/VPN credentials at rest; never in the app), `source/SourceService.java` (per-source settings, for the
  per-source assignment), R2-014 `OperationalAlertService` (surface an unhealthy egress).

## Build

1. **Egress pool** (`ai.foresignal.net` or a new `ai.foresignal.egress`). An `egress_route` table + entity:
   `id` (uuid), `name`, `type` (`http`/`socks5`/`wireguard`/`interface`), `enabled`, `url`
   (`scheme://host:port`), `boundInterface` (for `interface`), encrypted `secrets` (proxy creds or a WireGuard
   config), `exitIp` (last observed), timestamps. CRUD via `listEgressRoutes`/`createEgressRoute`/
   `getEgressRoute`/`updateEgressRoute`/`deleteEgressRoute` (`EgressRoute`/`EgressRouteInput`/`EgressRoutePage`;
   secrets write-only). Primary mechanism: **HTTP/SOCKS5 proxies** (in-container, each a different exit IP).
   **WireGuard/VPN profiles** and **bound interfaces/IPs** are host-level and optional — support them where the
   host allows, otherwise record the route but mark it unavailable rather than failing.

2. **Rotation policy** (`EgressSettings`, settings key `egress`): `rotation` ∈ `direct` (default/off),
   `round-robin`, `random`, `per-source-sticky`, `rotate-on-rate-limit`; plus a `defaultRoute`. `getEgressSettings`/
   `updateEgressSettings`. A `RouteSelector` picks a route per pull from the policy; `rotate-on-rate-limit` moves
   a source off any egress that 429s / sends `Retry-After` on that egress onto another.

3. **Per-source assignment.** `assignSourceEgress` (`SourceEgressAssignment`) pins a source to a specific route
   (e.g. a geo-locked source → an in-region exit) or excludes it from rotation. Store on the source settings.

4. **Wire into the collector.** At `BoundSourceHttp`/`FetchExecutor`, build the outbound `HttpClient` with the
   selected route's proxy (`java.net.ProxySelector` for HTTP; a SOCKS5 proxy for socks5) or bound local address,
   while **preserving** `OutboundUrlPolicy`/`PinnedTls` DNS-pinning and the `HostPacer`/`BackoffRegistry`
   behavior **per egress** (track pacing and backoff keyed by host *and* route, so one egress hitting a limit
   does not stall the others and `Retry-After` is honored on the egress that received it). `direct` keeps
   today's behavior exactly.

5. **Egress health + test.** `testEgressRoute` makes a test call through the route and reports the observed
   public **exit IP** (`EgressTestResult`), latency, reachability. `getEgressRouteHealth` (`EgressHealth`)
   reports per-route reachability/latency/exit-IP and per-source backoff state; unhealthy routes are skipped by
   the selector and surfaced to the admin (raise `system.egress_unhealthy` via R2-014). The exit-IP probe uses
   an allow-listed, policy-checked URL — never one supplied by observed content.

**Migration.** Add the next free additive Flyway migration `V{n}__egress_route.sql` (`n` one past the highest
`V*` now present — R2-001…R2-013 claim V14 upward, read the directory). Settings live in `app_setting`;
per-source assignment on the source settings row. Additive only.

## Contract

The ten ops are authored `planned`; implement to their schemas, secrets write-only. Extend a schema / add an
error code if needed, regenerate Postman (`python3 scripts/generate-postman.py`), but **leave
`x-foresignal-status: planned` and do not regenerate the SDKs** (R2-027 flips + regenerates). `docs/OpenAPI.json`
is the only contract.

## Docs

Create `docs/Network-Sourcing.md` (egress pool, rotation policies, the responsible-by-design posture — per-host
pacer + `Retry-After` honored per egress) and note it in `docs/Operator-Guide.md`. R2-019…R2-023 extend this
doc with the first-party aggregators.

## Tests to write (do not run)

- `EgressRouteServiceTest` — CRUD; secrets write-only and encrypted; validation of type/url.
- `RouteSelectorTest` — each rotation policy; `rotate-on-rate-limit` moves a source off a 429'd egress;
  per-source pin and exclusion honored; `direct` selects no route.
- `EgressFetchIT` — a pull uses the selected proxy; `HostPacer`/`BackoffRegistry` are keyed per egress so one
  egress's `Retry-After` does not stall another; DNS-pinning/`OutboundUrlPolicy` preserved.
- `EgressHealthIT` — `test` reports an exit IP; an unhealthy route is skipped and alerts.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

An admin can build an egress pool (proxy URL + encrypted creds, or a WireGuard config, or a bound interface),
set a rotation policy + default route, pin/exclude sources, and `POST /v1/egress/{id}/test` shows the exit IP;
pulls route through the selected egress while still obeying the per-host pacer and `Retry-After` per egress;
default is `direct`; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-018: Egress routing & IP rotation`, then
`completion-reports/R2-018-CR-Egress-Routing.md` with the 40-character implementation hash.
