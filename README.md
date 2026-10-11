# Foresignal

**Foresignal** ([foresignal.ai](https://foresignal.ai)) is an open-source, self-hosted
**intelligence broker**: a headless service that continuously collects public
telemetry from many upstream feeds, normalizes it into one versioned store, and
re-serves it over a single documented API. It is the data foundation for a
family of frontends — a 3D globe, a 2D analyst map, an operator console, mobile
clients, agent and MCP clients — that will be built on top of it.

Every existing open-source "god's-eye" project fuses three concerns into one
application: *subscription* (what gets collected, how often, at what cost to
upstreams), *display* (what a screen shows) and *alerting* (what deserves
attention). Foresignal separates them. Subscription is server-side and is the only
thing that causes fetching, with per-source schedules and budgets so the broker
always knows what it is spending on each upstream and refuses to exceed it.
Display is the client's business, served by filtered queries, vector tiles and
filtered live streams that never reach the collector. Alerting is a trigger
engine evaluated against the change stream, so a source that is subscribed but
not displayed can still announce itself. Everything collected is stored as
versioned records in Postgres + PostGIS, giving every frontend current state,
history and playback for free. All configuration happens through the API —
there is no UI inside the broker.

## Status

**Under construction.** The project is being built prompt by prompt; see
[`docs/Roadmap.md`](docs/Roadmap.md) for the plan and
[`completion-reports/`](completion-reports/) for what has been finished. Tests
are written alongside each change but are first executed in prompt 061, so the
build is not yet verified by a test run.

## Requirements

Either:

- **Docker** (with Compose) — the only requirement for the all-in-one setup; or
- **Java 21** (Temurin recommended) plus a reachable **PostgreSQL 15+ with
  PostGIS**, if you prefer to run the JAR standalone.

No local Gradle install is needed: use the committed wrapper (`./gradlew`).

## Quickstart

> Placeholder — the end-to-end quickstart (bring up the stack, mint an admin
> token, enable the USGS earthquakes source, watch the change stream) is
> written in prompt 018, once those pieces exist.

For now, to bring up only the database for local development:

```sh
docker compose up -d db
```

Postgres then listens on `localhost:5433` (database, user and password all
`foresignal`), which matches the application's defaults.

To compile:

```sh
./gradlew compileJava compileTestJava
```

## Configuration

Bootstrap configuration is environment-variable driven; everything else lives in
the database and is managed through the API.

| Variable | Default | Purpose |
|---|---|---|
| `FORESIGNAL_DB_URL` | `jdbc:postgresql://localhost:5433/foresignal` | Database URL |
| `FORESIGNAL_DB_USER` | `foresignal` | Database user |
| `FORESIGNAL_DB_PASSWORD` | `foresignal` | Database password |
| `FORESIGNAL_PORT` | `8080` | HTTP port |
| `FORESIGNAL_DATA_DIR` | `./data` | Secret key file location |
| `FORESIGNAL_RESET_ADMIN_TOKEN` | `false` | Regenerate the admin token on start |

## Deployment notes

- **Ports bind to localhost by default.** `docker compose` publishes the broker
  on `127.0.0.1:8080` and the database on `127.0.0.1:5433`. Set
  `FORESIGNAL_BIND=0.0.0.0` for LAN access to the broker, and
  `FORESIGNAL_DB_BIND=0.0.0.0` likewise for the database.
- **Disk.** At full load expect about 10–15 GB/day of history writes plus
  15–20 GB/day of WAL. A 3-day track window holds about 40M rows, roughly 20 GB
  including indexes.
- **Clock.** NTP must be synced before Foresignal starts. Otherwise the ingest
  writer's clock guard holds ingest until the clock is sane.

## Documentation

- [Executive Summary](docs/Executive-Summary.md) — what Foresignal is and why
- [Product Charter](docs/Product-Charter.md) — scope, requirements, decisions
- [Architecture](docs/Architecture.md) — stack, packages, data model, deployment
- [OpenAPI spec](docs/OpenAPI.json) — the API contract, served at `/v1/openapi.json`
- [Source Catalog](docs/Source-Catalog.md) — the upstream feeds Foresignal collects
- [Roadmap](docs/Roadmap.md) — task decomposition and build order
- [Intelligence Design](docs/Intelligence-Design.md) — evidence, discovery, hypotheses, forecast ledger, analysis cycles
- [Client Contract](docs/Client-Contract.md) — object references, relations, workspaces and deep links for frontends
- [Invariants](docs/Invariants.md) — the rules every checkpoint verifies
- [Conventions](docs/Conventions.md) — cross-cutting build rules and sources of truth

## Licence

MIT — see [LICENSE](LICENSE). Third-party notices and Foresignal's code-reuse policy
are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
