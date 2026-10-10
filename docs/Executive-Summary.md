# Elaro — Executive Summary

**Elaro** (elaro.ai) is an open-source, self-hosted **intelligence broker**: a headless service that continuously collects public telemetry from many upstream feeds, normalizes it into one versioned store, and re-serves it over a single documented API. It is the data foundation for a family of frontends (3D globe, 2D analyst map, operator console, mobile, agent/MCP clients) that will be built on top of it.

## The problem

Every existing open-source "god's-eye" project — ShadowBroker, God's Eye View, Velocity, World Monitor — fuses the same three concerns into one application:

1. **Subscription** — what data is collected, how often, and at what cost to upstreams.
2. **Display** — what a particular screen shows.
3. **Alerting** — what deserves attention.

Because they are fused, turning a layer off on the map stops collecting it. You cannot "watch quietly." History lives only as long as the browser tab or a narrow position cache. Each project re-implements the same 40–100 upstream integrations, inside a UI, with little or no accounting of how much load it puts on those upstreams — ShadowBroker was formally called out by the Meshtastic project for generating "1–2 orders of magnitude more traffic than typical clients."

## The solution

Elaro separates the three concerns:

- **Subscription is server-side and is the only thing that causes fetching.** Each source is enabled, scheduled and **budgeted** (requests per minute and per day) through the API. The broker always knows what it is spending per upstream and refuses to exceed it.
- **Display is the client's business.** Frontends choose what to render via filtered queries, vector tiles and filtered live streams. Their choices never reach the collector.
- **Alerting is a trigger engine** evaluated against the change stream. A layer that is subscribed but not displayed can still announce itself: a quake over M6, a vessel entering a geofence, a region's connectivity dropping.

Everything Elaro collects is stored as **versioned records** in Postgres + PostGIS, so every frontend gets current state, history and playback for free. **All configuration is done through the API** — sources, credentials, budgets, retention, triggers — because there is no UI in the broker.

## Who it is for

Individuals: OSINT hobbyists, analysts, researchers, makers and developers who want to own their data feed. It runs as an always-on service on a home server, or as something you `docker compose up` when you want it.

## Scope at a glance

| In scope (v1) | Out of scope (v1) |
|---|---|
| Ingest, normalize, version, re-serve public telemetry | Any UI inside the broker |
| Per-source scheduling, budgets, health | Licence/provenance enforcement (deferred) |
| Versioned store, retention via API | Multi-tenant SaaS, horizontal scale-out |
| Trigger engine + alert stream (SSE, webhooks) | Fine-grained auth (single admin token for now) |
| REST + GeoJSON + vector tiles + SSE, OpenAPI-first | Basemap/imagery hosting (clients fetch tiles directly) |
| ~10 high-value sources first, growing toward ~90 | Scraping sites that forbid it |

## Key decisions

| Decision | Choice |
|---|---|
| Language / framework | Java 21 (virtual threads), Spring Boot 4.1 |
| Store | PostgreSQL + PostGIS; Hibernate-managed schema in development, Flyway introduced only for production |
| Packaging | Single Gradle module; Docker Compose (broker + PostGIS) |
| API | REST under `/v1`, OpenAPI-first, GeoJSON, MVT tiles, Server-Sent Events |
| Security | Open reads on the local network; single admin bearer token for all writes |
| Licence | MIT |
| Reuse | God's Eye View (MIT) code may be ported with notice; ShadowBroker, Velocity, World Monitor (AGPL) are reference only — no code copied |

## Differentiators

1. **Subscription decoupled from display** — watch quietly; alert loudly.
2. **Per-source budgets and health** — a good upstream citizen by construction, and self-diagnosing when an adapter breaks.
3. **Versioned history you own** — every record, every revision, with retention you set.
4. **API as the product** — one contract, many frontends.
5. **Built for an AI maintainer** — health API, raw-fetch diagnostics and per-adapter fixture tests let a future maintenance agent detect and repair broken adapters.

## Roadmap shape

1. **Foundation** — project skeleton, store, source framework, budgets, scheduler, admin token.
2. **API** — records, changes, streams, GeoJSON, tiles, settings, health, OpenAPI.
3. **Triggers** — rule engine, alerts, webhooks.
4. **First source set** — USGS earthquakes end to end, then ~10 high-value adapters (aircraft, vessels, satellites, fires, conflict events, outages, weather alerts).
5. **Adapter waves** — porting from God's Eye View and re-implementing from the reference inventories.
6. **Verification** — full test run to 90% coverage, then production hardening (Flyway baseline).

Follow-on projects: a reference globe frontend, an operator console, and an AI adapter-maintenance agent.

See the [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json) and [Roadmap](Roadmap.md).
