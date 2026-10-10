# Elaro — Product Charter

| | |
|---|---|
| Product | Elaro — headless intelligence broker |
| Domain | elaro.ai |
| Owner | Adam Allard |
| Licence | MIT |
| Repository | gitlab.com/adam.allard/elaro |
| Status | Charter v1.0 — 2026-10-10 |

---

## 1. Purpose

Elaro is the data foundation for a family of situational-awareness applications. It collects public telemetry (aircraft, vessels, satellites, seismicity, fires, conflict events, weather, outages, infrastructure, cameras, transit, and more), normalizes it into versioned records, evaluates alert rules against the change stream, and serves everything through one stable, documented API.

Elaro has **no user interface**. Every capability — including all configuration — is reachable through the API. Frontends are separate projects.

## 2. Vision

> Own your view of the world. Subscribe once, store everything, render anywhere, and be told when something matters.

## 3. Problem statement

Open-source situational-awareness projects (ShadowBroker, God's Eye View, Velocity, World Monitor) each bundle collection, display and alerting into one application. The consequences:

1. **Collection is coupled to display.** Hiding a layer stops collecting it; you cannot monitor something without looking at it.
2. **No durable history.** Data lives in the browser or in short position caches; playback and analysis are limited or absent.
3. **Upstream abuse by accident.** Without per-source accounting, projects over-poll; ShadowBroker was called out by Meshtastic for 1–2 orders of magnitude excess traffic.
4. **Duplicated integration effort.** Every project re-implements the same upstreams, inside a UI codebase, and each breaks independently when upstreams change.
5. **One client per backend.** A globe, a phone, a CLI and an agent cannot share one collector.

## 4. Goals

| # | Goal | Measure of success |
|---|---|---|
| G1 | Decouple subscription, display and alerting | Disabling display in every client has zero effect on collection; enabling/disabling a source through the API starts/stops fetching within one scheduler tick (≤ 5 s) |
| G2 | Be a good upstream citizen | No source ever exceeds its configured per-minute or per-day request budget; spend is visible per source |
| G3 | Own the history | Every distinct revision of every record is retained per the configured retention; any record's version history is queryable |
| G4 | One API, many clients | A frontend can be built using only the OpenAPI spec; GeoJSON, MVT tiles and filtered SSE are available |
| G5 | Alert on what matters | Threshold and geofence rules fire within one ingest cycle of the triggering record and are delivered via SSE and webhook |
| G6 | Self-diagnosing adapters | A broken adapter (errors or silent zero results) is visible through the health API without reading logs |
| G7 | Trivial to run | `docker compose up` on Linux or macOS yields a working broker with no file edits beyond optional DB/port settings |
| G8 | Maintainable by an AI agent | Every adapter is self-contained with a recorded fixture and contract test; raw upstream fetches are available as a diagnostic |

## 5. Non-goals (v1)

- A user interface of any kind inside the broker.
- Licence/provenance enforcement or filtering (deferred; a plain attribution string per source is kept).
- Multi-user accounts, roles or per-client keys (single admin token only).
- Horizontal scale-out or clustering (single instance by design).
- Hosting or proxying basemaps and imagery (Esri, GIBS, Google 3D Tiles, OSM tiles are fetched by clients directly).
- Scraping sites whose terms forbid it, or headless-browser collection.
- Real-time sub-second latency guarantees.

## 6. Users and personas

| Persona | Description | Primary needs |
|---|---|---|
| **The Operator** (primary; the owner) | Runs Elaro on an always-on Linux bench server. Configures sources, budgets, retention and triggers through the API or the future operator console. | Reliability, visibility of spend and health, alerts, zero file editing |
| **The Occasional Runner** | Individual who starts Elaro with Docker Compose when they want to look at the world, then stops it. | Fast startup, sensible defaults, no mandatory API keys for a useful first run |
| **The Frontend Developer** | Builds a globe, 2D map, phone app, Grafana board or CLI against Elaro. | Stable, documented, typed API; GeoJSON; tiles; streaming; CORS |
| **The Agent** | An MCP-speaking or HTTP AI agent that queries data or maintains adapters. | Machine-readable health, raw-fetch diagnostics, predictable schemas |
| **The Contributor** | Open-source developer adding or fixing an adapter. | One small class per adapter, fixture-based tests, clear contract |

## 7. Core concepts

| Concept | Definition |
|---|---|
| **Source** | One upstream feed integration (e.g. `usgs-earthquakes`). Has an adapter, settings (enabled, interval, params, budget, retention, credentials) and health. |
| **Adapter** | The code that fetches one source and converts the response into normalized records. |
| **Subscription** | A source being *enabled*. The only thing that causes fetching. |
| **Record** | One normalized item (an earthquake, an aircraft, a fire pixel, a sanctions entry). Identified by `(source, externalId)`. |
| **Version** | An immutable revision of a record. A new version is written only when content changes. |
| **Kind** | The record's category (e.g. `earthquake`, `aircraft`, `vessel`, `satellite`, `fire`, `conflict-event`, `outage`, `weather-alert`, `camera`). |
| **Change** | The event of a new version being written. Changes are totally ordered by a global sequence number (`seq`). |
| **Budget** | A source's request allowance: max requests per minute and per day. |
| **Trigger rule** | A filter (source/kind) plus conditions (threshold, geofence) evaluated against changes. |
| **Alert** | A record of a trigger rule firing on a specific record version. |
| **Retention** | How long versions are kept; a global default plus per-source overrides. |

## 8. Functional requirements

### 8.1 Source management
- **FR-1** The broker exposes the catalogue of all available sources with their descriptor (id, name, kinds, homepage, attribution, whether credentials are required, default interval and budget).
- **FR-2** The operator can enable/disable a source, set its polling interval (never below the adapter's minimum), adapter parameters, and budget, via `PUT`.
- **FR-3** The operator can set upstream credentials per source via a write-only endpoint; credentials are never returned by any endpoint.
- **FR-4** Enabling a source starts collection within one scheduler tick; disabling stops it and cancels in-flight scheduling.
- **FR-5** A source requiring credentials cannot be enabled until credentials are set (returns 409 with an explanatory error).
- **FR-6** The operator can trigger a one-off fetch (`POST /v1/sources/{id}/fetch`) that ingests normally, subject to budget.
- **FR-7** The operator can request a diagnostic dry-run (`POST /v1/sources/{id}/probe`) that fetches once, returns the raw upstream response (truncated to a configurable size) plus the parsed records, and stores nothing. Probes consume budget.

### 8.2 Budgets and health
- **FR-8** Every outbound upstream request passes through the source's budget. Requests exceeding budget are refused (not queued) and recorded as `budget_exceeded`.
- **FR-9** Per-source usage is recorded per minute: requests, bytes received, errors, budget refusals.
- **FR-10** Per-source health reports: status (`healthy`, `degraded`, `failing`, `idle`, `disabled`), last attempt, last success, last error message, consecutive failures, records in last fetch, error rate over the last hour, budget used today / remaining.
- **FR-11** A fetch that succeeds but yields zero records when the previous N fetches yielded records marks the source `degraded` (silent-format-change detection), unless the adapter declares zero results normal.
- **FR-12** Upstream HTTP 429/503 responses trigger exponential backoff for that source, honouring `Retry-After`.

### 8.3 Ingestion and storage
- **FR-13** Adapters return normalized records: `externalId`, `kind`, `observedAt`, optional geometry (GeoJSON-compatible, WGS84), optional `validUntil`, and a JSON payload.
- **FR-14** For each record, the broker computes a content hash; if it matches the current version, nothing is written; otherwise a new version with the next `seq` is appended.
- **FR-15** Adapters may declare records removed (e.g. a sanctions entry delisted); the broker writes a tombstone version.
- **FR-16** Records whose `validUntil` has passed are excluded from "current" queries by default (e.g. aircraft not seen for N minutes).
- **FR-17** Retention is configured via `PUT /v1/settings/retention` with a global default and per-source overrides (ISO-8601 durations or shorthand like `7d`); a periodic job deletes versions older than the window. The current version of a record is kept unless it, too, is older than the window.

### 8.4 Query API
- **FR-18** `GET /v1/records` returns current records filtered by source(s), kind(s), bbox, time window, and free-text match on a small set of payload fields, with cursor pagination; output as JSON or GeoJSON.
- **FR-19** `GET /v1/records/{source}/{externalId}` and `/versions` return a record and its full version history.
- **FR-20** `GET /v1/history` returns all versions within a time range and bbox for playback.
- **FR-21** `GET /v1/changes?after={seq}` returns changes after a cursor; `GET /v1/changes/stream` streams changes over SSE, filterable by source, kind and bbox, resumable via `Last-Event-ID`.
- **FR-22** `GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt` serves Mapbox Vector Tiles of current records, where a layer is a kind or source.
- **FR-23** The OpenAPI document is served at `/v1/openapi.json`, with interactive docs at `/docs`.

### 8.5 Triggers and alerts
- **FR-24** The operator can create, update, enable/disable and delete trigger rules via the API.
- **FR-25** A rule has: name, enabled, optional source filter, optional kind filter, zero or more threshold conditions (`payload path`, operator `> >= < <= == !=`, value), optional geofence (GeoJSON Polygon/MultiPolygon), a `mode` (`match`: fire on every matching version; `enter`: fire when a record starts satisfying the rule, for example a vessel entering a geofence; `exit`: fire when it stops satisfying it), optional `onlyNew` (fire only on the first version of a record), and a cooldown per record.
- **FR-26** Rules are evaluated against each committed batch of changes. A match creates an alert containing the rule, record version and a rendered message.
- **FR-27** Alerts are queryable (`GET /v1/alerts`), streamable (`GET /v1/alerts/stream`, SSE) and acknowledgeable.
- **FR-28** The operator can register webhooks (URL, optional secret, rule filter); alerts are POSTed as JSON with an HMAC-SHA256 signature header, with retry and backoff.
- **FR-29** Rules can be tested against existing data without creating alerts (`POST /v1/triggers/{id}/test`).

### 8.6 System
- **FR-30** On first start the broker generates an admin token, stores its hash, and prints the token once to the log. The operator can rotate it via the API.
- **FR-31** All non-GET endpoints (and all credential/settings reads) require `Authorization: Bearer <admin token>`. GET endpoints for data are open.
- **FR-32** `GET /v1/system` reports version, uptime, DB status, record counts, storage size, and scheduler state. `GET /actuator/health` is available for container health checks.
- **FR-33** CORS is enabled for all origins on GET endpoints by default; allowed origins are configurable via the API.
- **FR-34** Configuration export/import: `GET /v1/settings/export` returns all settings (excluding credentials) as JSON; `PUT /v1/settings/import` applies them.

## 9. Non-functional requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-1 | Footprint | Idle broker with 10 sources enabled runs within 512 MB heap on a modest Linux box. |
| NFR-2 | Throughput | Sustain ingest of 20,000 record versions/minute (dense aircraft feed) on a single node. |
| NFR-3 | Latency | Change visible on the SSE stream ≤ 2 s after commit; alerts ≤ 5 s after the triggering commit. |
| NFR-4 | Startup | Broker ready ≤ 20 s after Postgres is reachable. |
| NFR-5 | Resilience | A failing or hanging adapter never affects other sources (isolated virtual threads, per-request timeouts). |
| NFR-6 | Persistence | Settings, rules, credentials and data survive restarts; scheduler resumes enabled sources on boot. |
| NFR-7 | Security | Admin token hashed at rest; credentials encrypted at rest with a key generated on first start; no secrets in logs. |
| NFR-8 | Testability | ≥ 90% line coverage across unit + integration tests; every adapter has a fixture-based contract test. |
| NFR-9 | Portability | Runs on Linux (amd64/arm64) and macOS via Docker Compose; also runnable as a plain JAR against any Postgres+PostGIS. |
| NFR-10 | API stability | `/v1` is additive-only once released; breaking changes go to `/v2`. |
| NFR-11 | Observability | Structured JSON logs; Micrometer metrics at `/actuator/prometheus`. |

## 10. Initial source set

The first milestone proves the end-to-end loop with **USGS Earthquakes**. The first wave then targets about ten high-value, mostly keyless sources across distinct kinds:

| Source | Kind | Key? |
|---|---|---|
| USGS Earthquakes (GeoJSON summary feeds) | earthquake | No |
| adsb.lol | aircraft | No |
| OpenSky Network | aircraft | Optional |
| AISStream | vessel | Yes |
| CelesTrak (GP elements) | satellite | No |
| NASA FIRMS | fire | Yes (free MAP_KEY) |
| GDELT (events) | conflict-event | No |
| NWS / NOAA active alerts | weather-alert | No |
| NHC tropical cyclones | cyclone | No |
| IODA internet outages | outage | No |
| GDACS disaster alerts | disaster | No |
| Launch Library 2 | launch | No |

Subsequent waves are defined in the Roadmap after the reference-repository inventory: God's Eye View ports (municipal cameras, transit, GBFS, weather depth), then intelligence sources (sanctions, KEV, abuse.ch, power plants, etc.).

## 11. Constraints and decisions

- Java 21 LTS, Spring Boot 4.1, Gradle (Kotlin DSL) with wrapper.
- PostgreSQL 17 + PostGIS 3.5; schema managed by Hibernate (`ddl-auto=update`) during development; Flyway introduced only when preparing for production.
- Single Gradle module, packages under `ai.elaro`.
- MIT licence. God's Eye View (MIT) code may be ported with its copyright notice retained in `THIRD_PARTY_NOTICES.md`. ShadowBroker, Velocity and World Monitor (AGPL) are reference material only — no code is copied.
- Development methodology: numbered prompts executed by Claude Code, each producing a completion report; tests are written alongside code but **run only once the project is code-complete**; each prompt ends with compile, commit, push.

## 12. Assumptions

- The operator's network is trusted for reads; Elaro is not exposed directly to the internet without a reverse proxy.
- Upstream feeds' terms permit personal, non-commercial use as configured; the operator is responsible for supplying their own keys.
- Docker is available on the target host.

## 13. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Upstream APIs change or vanish | Adapters silently break | Health API with zero-result detection; fixtures + contract tests; planned AI maintenance agent |
| Upstream rate limits / bans | Loss of a source; reputational harm | Mandatory per-source budgets; backoff on 429/503; conservative defaults |
| Storage growth from dense feeds | Disk exhaustion | Retention via API; per-source overrides; storage size in `/v1/system` |
| Single admin token leaked | Config tampering, credential exfiltration | Credentials write-only and encrypted; token rotation endpoint; reverse-proxy guidance |
| Hibernate DDL drift before Flyway | Schema surprises at production cut-over | Final roadmap step generates a Flyway baseline from the Hibernate schema |
| Scope creep toward a UI | Delays core | UI lives only in separate frontend projects |

## 14. Success criteria for v1

1. Elaro runs on the bench server continuously for 7 days with the first source wave enabled, with no source exceeding budget.
2. A trigger rule (e.g. `mag >= 6`) fires and is delivered by SSE and webhook.
3. A frontend renders live aircraft and earthquakes using only the OpenAPI spec, GeoJSON/MVT and SSE.
4. All tests pass with ≥ 90% coverage.
5. The health API correctly flags an adapter broken by a simulated upstream format change.

## 15. Follow-on projects

1. **Reference globe frontend** — CesiumJS client, reusing God's Eye View UI components.
2. **Operator console** — web UI over the settings, health, budget and trigger endpoints.
3. **Adapter maintenance agent** — AI agent that watches health, probes upstreams, repairs adapters and opens merge requests.
4. **MCP server** — exposing Elaro queries as agent tools.
5. **Licence/provenance layer** — per-record licence tracking and egress policy.
