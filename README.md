# Foresignal

**Foresignal** ([foresignal.ai](https://foresignal.ai)) is an open-source, self-hosted
**intelligence broker**: a headless service that continuously collects public
telemetry from many upstream feeds, normalizes it into one versioned store, alerts
on what matters, and re-serves it over a single documented API. It is the data
foundation for a family of frontends — a 3D globe, a 2D analyst map, an operator
console, mobile clients, agent and MCP clients — built on top of it.

> **Name note.** Foresignal (foresignal.ai) is **unrelated to foresignal.com**, a
> forex-signals site.

Every existing open-source "god's-eye" project fuses three concerns into one
application: *subscription* (what gets collected, how often, at what cost to
upstreams), *display* (what a screen shows) and *alerting* (what deserves
attention). Foresignal separates them. Subscription is server-side and is the only
thing that causes fetching, with per-source schedules and budgets so the broker
always knows what it is spending on each upstream and refuses to exceed it.
Display is the client's business, served by filtered queries, vector and raster
tiles and filtered live streams that never reach the collector. Alerting is a
trigger engine evaluated against the change stream, so a source that is subscribed
but not displayed can still announce itself. Everything collected is stored as
versioned records in Postgres + PostGIS, giving every frontend current state,
history and playback for free. All configuration happens through the API —
there is no UI inside the broker.

## Status

**v1.0.0, code-complete, pending owner verification.** All 144 roadmap prompts
([`docs/Roadmap.md`](docs/Roadmap.md)) are implemented and compile; each has a report in
[`completion-reports/`](completion-reports/). Not yet done:

- **Tests.** The suite last ran, green, at checkpoint 097 (8,084 tests). Under the owner
  directive of 2026-10-17, tests for prompts 098–144 were written but not run. Running the
  full suite with the coverage gate is the first verification step.
- **Runtime verification.** The procedure is in
  [`docs/Runtime-Verification.md`](docs/Runtime-Verification.md); nothing has been measured yet.
- **Schema.** The Flyway baseline (`src/main/resources/db/migration/V1__baseline.sql`) was
  derived by hand and must be compared with a pre-cutover schema (`scripts/db/dump-schema.sh`).
- **Tag.** `v1.0.0` is tagged by the owner after the suite passes.

## Features

- **Collection:** 105 adapter packages (earthquakes, aircraft, vessels, satellites, fires,
  weather, conflict, outages, cyber, space weather, environment, hydrology, transit, cameras,
  sanctions, energy, news, social platforms and more), plus local receivers (readsb, AIS-catcher,
  weather stations, APRS, Meshtastic and MeshCore nodes, RF decoders). Every source is installed
  disabled, budgeted per minute, per day and in bytes, and health-checked, with a raw probe and an
  adapter doctor when it breaks. See the [Source Catalog](docs/Source-Catalog.md).
- **Versioned history:** every revision of every record, in two tiers (STANDARD and TRACK) with
  partitioned storage, ingest policies, retention you set, downsampling, and reads "as of" any
  past instant: tracks, animation frames and timelines.
- **Read API:** records as JSON, GeoJSON, ndjson or CSV; vector tiles; server-sent event streams
  with resume; stats rollups and a compact glance; places, saved views and watchlists; an offline
  gazetteer; semantic and text search.
- **Alerts:** threshold, set and pattern operators, geofences with hysteresis, named places,
  watchlists, "near me", approach, enter/exit modes, active windows, windowed count, absence and
  anomaly rules, backtests and exercise replays; an alert lifecycle with snooze, feedback,
  auto-resolve and escalation.
- **Notifications:** 19 channel types — webhook, Slack, Mattermost, Discord, Teams, Google Chat,
  Telegram, Matrix, Zulip, ntfy, Gotify, Pushover, email, Home Assistant, Apprise, MQTT
  (with Home Assistant discovery), Meshtastic, MeshCore and Web Push (browsers, UnifiedPush,
  APNs, FCM) — with actions from the notification, an inbox, map attachments and interactive
  Telegram and Matrix commands.
- **Weather and imagery:** radar, satellite and model products (MRMS, GOES, GFS, HRRR, ECMWF)
  rendered by Foresignal into tiles, images, loops, wind fields and legends; point and area
  sampling; storm tracking; weather context on alerts; SatDump ground stations; static maps
  without third-party basemaps.
- **Optional LLM:** Ask with verified citations, scheduled briefings (text or audio), alert
  enrichment, an autonomous analyst, translation — with Anthropic, any OpenAI-compatible API or a
  local Ollama model, all budgeted.
- **Intelligence layer:** canonical events and evidence independence, observability, situations,
  change points and event studies, precursors, base rates and analogs, rolling hypotheses, a
  scored forecast ledger, outlooks and indices, impact projection, and daily, weekly and monthly
  assessments; news coverage and social narratives compared with physical evidence.
- **Collaboration:** principals and groups, TLP, tags, comments, graded evidence, investigations,
  chronicles, reports and signed packages between instances; workspaces that keep several
  clients in sync ([Client Contract](docs/Client-Contract.md)).
- **Interop:** MCP server for agents (with remote authorization), MapLibre styles, KML, Atom and
  JSON Feed, OGC API Features, Tiles and EDR, WMTS, CAP, iCalendar, CZML, QuakeML, STIX/TAXII;
  exports to GeoParquet, GeoPackage, FlatGeobuf, GPX, MBTiles and PMTiles; outbound sinks to
  MQTT, TAK, readsb, NMEA, APRS-IS and CloudEvents; federation between instances; the
  [External Data Protocol](docs/External-Data-Protocol.md) for out-of-tree providers; WASM parser
  plugins.
- **Privacy and licenses:** private sources and personal location, per-record license terms, and
  one egress policy on every outbound path; aggregated attributions; scoped tokens, feed keys and
  tile keys; an SSRF policy for every operator-supplied URL.
- **Clients:** generated Dart, TypeScript and Python SDKs ([sdk/README.md](sdk/README.md)) and a
  command-line client ([cli/README.md](cli/README.md)).

## Requirements

Either:

- **Docker** (with Compose) — the only requirement for the all-in-one setup; or
- **Java 21** (Temurin recommended) plus a reachable **PostgreSQL with the PostGIS and pgvector
  extensions** (the compose database image is PostgreSQL 17 with PostGIS 3.5 and pgvector), if you
  prefer to run the JAR standalone. The Flyway baseline creates both extensions; on a server where
  Foresignal's database user is not a superuser, a superuser must create them first.

No local Gradle install is needed: use the committed wrapper (`./gradlew`).

## Quickstart

1. **Start the stack** (the database and the broker, both bound to `127.0.0.1`):

   ```sh
   docker compose up -d
   ```

   On first start Flyway creates the schema in the empty database.

2. **Read the admin token.** It is printed once, on the first start, and only its hash is kept:

   ```sh
   docker compose logs foresignal | grep -A3 "FORESIGNAL ADMIN TOKEN"
   export FORESIGNAL_TOKEN=fsr_...   # the token from that block
   ```

   Lost it? Restart once with `FORESIGNAL_RESET_ADMIN_TOKEN=true` (see the
   [Operator Guide](docs/Operator-Guide.md#first-start-and-the-admin-token)).

3. **Enable the USGS earthquakes source.** Every source is installed disabled; enabling one is the
   only thing that makes Foresignal contact its upstream.

   ```sh
   curl -X PUT localhost:8080/v1/sources/usgs-earthquakes \
     -H "Authorization: Bearer $FORESIGNAL_TOKEN" -H 'Content-Type: application/json' \
     -d '{"enabled": true}'
   ```

4. **Watch changes arrive** (server-sent events; each quake is a `change`, revisions follow):

   ```sh
   curl -N localhost:8080/v1/changes/stream
   ```

   Current state is at `curl 'localhost:8080/v1/records?source=usgs-earthquakes&format=geojson'`.

5. **Check it end to end:** `FORESIGNAL_TOKEN=$FORESIGNAL_TOKEN scripts/smoke.sh` (needs `curl` and
   `jq`) checks the system document and readiness, enables USGS, waits for records and creates and
   deletes a test trigger.

Next: triggers, notification channels, remote access over HTTPS, backups and the rest are in the
**[Operator Guide](docs/Operator-Guide.md)**. The API is browsable at
[`localhost:8080/docs`](http://localhost:8080/docs) (Swagger UI over the served
[`/v1/openapi.json`](http://localhost:8080/v1/openapi.json)).

### Postman

Import [`postman/Foresignal.postman_collection.json`](postman/Foresignal.postman_collection.json)
and [`postman/Foresignal-Local.postman_environment.json`](postman/Foresignal-Local.postman_environment.json),
set the environment's `adminToken`, and run the folder **"00 Quickstart — USGS end to end"**. Or
headless:

```sh
npx newman run postman/Foresignal.postman_collection.json \
  --folder "00 Quickstart — USGS end to end" --env-var adminToken=$FORESIGNAL_TOKEN
```

### Local development

Run only the database in Docker and the broker from the working tree, under the capped launcher:

```sh
docker compose up -d db
./gradlew bootJar
scripts/run-server.sh start     # also: status, stop
```

Postgres then listens on `localhost:5433` (database, user and password all `foresignal`), which
matches the application's defaults. The token banner appears in `build/foresignal-server.log`.

**The broker runs only through `docker compose up -d` or `scripts/run-server.sh`, never a bare
`java -jar` or `./gradlew bootRun`.** Both supported paths are capped and cannot swap: compose
sets `memswap_limit` equal to `mem_limit` on the broker and the database, and the launcher runs
the JVM (`-Xmx4g`, native memory tracking on) in a systemd user scope with `MemoryMax=10G` and
`MemorySwapMax=0` (`FORESIGNAL_MEM_MAX` changes the cap). At the cap the broker is OOM-killed and
`scripts/run-server.sh status` says so; the host keeps running. An uncapped broker beside two
Gradle daemons filled 24 GB of swap on 2026-10-25 and the host had to be power-cycled.

To compile:

```sh
./gradlew compileJava compileTestJava
```

## Configuration

Bootstrap configuration is environment-variable driven; everything else lives in
the database and is managed through the API. The main variables:

| Variable | Default | Purpose |
|---|---|---|
| `FORESIGNAL_DB_URL` | `jdbc:postgresql://localhost:5433/foresignal` | Database URL |
| `FORESIGNAL_DB_USER` | `foresignal` | Database user |
| `FORESIGNAL_DB_PASSWORD` | `foresignal` | Database password |
| `FORESIGNAL_PORT` | `8080` | HTTP port |
| `FORESIGNAL_DATA_DIR` | `./data` | Secret key, media, rasters, exports and plugins |
| `FORESIGNAL_RESET_ADMIN_TOKEN` | `false` | Regenerate the admin token on start |
| `FORESIGNAL_BIND` | `127.0.0.1` | The address compose publishes the broker on |
| `FORESIGNAL_PROFILE` | `small` | Memory profile (`small` or `large`) for the raster caches |
| `FORESIGNAL_MEM_LIMIT` | `6g` | The broker container's memory limit, also its `memswap_limit` |
| `FORESIGNAL_DB_MEM_LIMIT` | `6g` | The database container's memory limit, also its `memswap_limit` |
| `FORESIGNAL_MEM_MAX` | `10G` | `scripts/run-server.sh`'s `MemoryMax` (swap is always 0) |
| `FORESIGNAL_LOG_FORMAT` | `ecs` under the `docker` profile | `ecs`, `gelf`, `logstash` or `text` |
| `FORESIGNAL_TRUSTED_PROXIES` | loopback | Proxies whose `X-Forwarded-*` headers are honored |

The complete list is in the [Operator Guide](docs/Operator-Guide.md) and the
[Architecture](docs/Architecture.md).

## Deployment notes

- **Ports bind to localhost by default.** `docker compose` publishes the broker
  on `127.0.0.1:8080` and the database on `127.0.0.1:5433`. Set
  `FORESIGNAL_BIND=0.0.0.0` for LAN access to the broker; leave the database local.
- **Compose profiles:** `tailscale` (HTTPS on your tailnet through Tailscale Serve), `web` (Caddy
  with its own local certificate authority, for map clients), `local-llm` (a local Ollama) and
  `satdump` (a SatDump ground station from an image you build). An Avahi mDNS file is in
  `docker/avahi/`. See the Operator Guide.
- **Schema upgrades:** Flyway applies `V{n}__*.sql` migrations on start, and Hibernate validates
  the result. A database created by a pre-1.0 build is adopted at version 1 without running the
  baseline; compare it with `scripts/db/dump-schema.sh`, or recreate the volume
  (`docker compose down -v`) to start clean.
- **Disk.** At full load expect about 10–15 GB/day of history writes plus
  15–20 GB/day of WAL. A 3-day track window holds about 40M rows, roughly 20 GB
  including indexes. These are design projections, not yet measured.
- **Clock.** NTP must be synced before Foresignal starts. Otherwise the ingest
  writer's clock guard holds ingest until the clock is sane.
- **Backups** are a `pg_dump -Fc` plus `${FORESIGNAL_DATA_DIR}/secret.key`; without the key, stored
  credentials and channel secrets cannot be decrypted.

## Development

- `./gradlew compileJava compileTestJava` compiles; `./gradlew check` runs the tests, the coverage
  gate and the OpenAPI lint.
- `docs/OpenAPI.json` is the authored API contract, served byte for byte at `/v1/openapi.json`
  (Swagger UI at `/docs`). `/v1` is additive only (see the API evolution rules in the
  [Architecture](docs/Architecture.md)); `./gradlew checkApiCompat` compares the spec with the frozen
  v1.0.0 copy in `docs/api/v1.0.0/OpenAPI.json` using oasdiff (a local binary or Docker, else it is
  skipped with a warning).
- After changing the spec, run `./gradlew generatePostman` and `./gradlew lintOpenApi` (Node.js
  ≥ 20.19 with `npx`; pass `-PskipOpenApiLint=true` to skip the lint where Node.js is missing).
- `./gradlew generateSdks` regenerates the Dart, TypeScript and Python clients under `sdk/`
  (see [sdk/README.md](sdk/README.md)); `./gradlew checkSdkGeneration` preprocesses the spec for Dart
  and dry-runs the dart-dio generator.
- The command-line client is a separate Gradle build in `cli/` (see [cli/README.md](cli/README.md)).
- Schema changes are new Flyway migrations in `src/main/resources/db/migration/`, never edits to an
  applied one.
- No CI configuration is committed; the proposed pipeline is in [CI Proposal](docs/CI-Proposal.md).

## Documentation

**Design (authoritative)**

- [Product Charter](docs/Product-Charter.md) — scope, requirements, decisions, success criteria
- [Architecture](docs/Architecture.md) — stack, packages, data model, weather and imagery, deployment
- [OpenAPI spec](docs/OpenAPI.json) — the API contract, served at `/v1/openapi.json`
- [Source Catalog](docs/Source-Catalog.md) — every upstream feed, with tier, policy, license and egress
- [Intelligence Design](docs/Intelligence-Design.md) — evidence, discovery, hypotheses, forecast ledger, analysis cycles
- [Client Contract](docs/Client-Contract.md) — object references, relations, workspaces and deep links for frontends
- [External Data Protocol](docs/External-Data-Protocol.md) — writing out-of-tree data providers
- [Plugin Design](docs/Plugin-Design.md) — out-of-tree WASM parser plugins (ABI, limits, security model)

**Guides**

- [Operator Guide](docs/Operator-Guide.md) — running, tuning, exposing and backing up an instance
- [Adapter Guide](docs/Adapter-Guide.md) — writing a source adapter
- [Agent Guide](docs/Agent-Guide.md) — agents, dashboards, home automation and MCP
- [Client Guide](docs/Client-Guide.md) — how a client app works with Foresignal
- [Frontend Guide](docs/Frontend-Guide.md) — map clients: tile keys, templates, retries and per-client recipes
- [Metrics](docs/Metrics.md) — every Micrometer meter Foresignal registers
- [SDKs](sdk/README.md) and [CLI](cli/README.md)

**Release and verification**

- [Release Notes v1.0.0](docs/Release-Notes-v1.0.0.md)
- [Runtime Verification](docs/Runtime-Verification.md) — the verification procedure (not yet run)
- [CI Proposal](docs/CI-Proposal.md) — the proposed pipeline for the owner to adopt

**Project**

- [Executive Summary](docs/Executive-Summary.md) — what Foresignal is and why
- [Roadmap](docs/Roadmap.md) — the 144 prompts and their status
- [Conventions](docs/Conventions.md) — cross-cutting build rules
- [Invariants](docs/Invariants.md) — the rules every checkpoint verifies
- [Design history](docs/design-history/) — the v1.2 design inputs (Core Data Model, Weather & Imagery Design, Gap Analysis), folded into the Architecture

## License

MIT © RemoteID Systems, LLC — see [LICENSE](LICENSE). Third-party notices and Foresignal's code-reuse policy
are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
