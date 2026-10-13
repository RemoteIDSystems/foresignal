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
[`completion-reports/`](completion-reports/) for what has been finished. The core (prompts
001–021: collection, versioned storage, the read API, streams, tiles, triggers, notification
channels and retention) is built. Tests are written with every change and run at the checkpoint
prompts (012, 022, 041, …); the latest run is recorded in the newest checkpoint report.

## Requirements

Either:

- **Docker** (with Compose) — the only requirement for the all-in-one setup; or
- **Java 21** (Temurin recommended) plus a reachable **PostgreSQL 15+ with
  PostGIS**, if you prefer to run the JAR standalone.

No local Gradle install is needed: use the committed wrapper (`./gradlew`).

## Quickstart

1. **Start the stack** (PostGIS and the broker, both bound to `127.0.0.1`):

   ```sh
   docker compose up -d
   ```

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

Run only the database in Docker and the broker from the working tree:

```sh
docker compose up -d db
./gradlew bootRun
```

Postgres then listens on `localhost:5433` (database, user and password all `foresignal`), which
matches the application's defaults. The token banner appears in the `bootRun` output.

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
  `FORESIGNAL_BIND=0.0.0.0` for LAN access to the broker; leave the database local.
- **Remote access:** `TS_AUTHKEY=… docker compose --profile tailscale up -d` serves the broker over
  HTTPS on your tailnet through Tailscale Serve; a Caddy example is in `docker/caddy/`, and an Avahi
  mDNS file in `docker/avahi/`. See the Operator Guide's "Running the host".
- **Disk.** At full load expect about 10–15 GB/day of history writes plus
  15–20 GB/day of WAL. A 3-day track window holds about 40M rows, roughly 20 GB
  including indexes.
- **Clock.** NTP must be synced before Foresignal starts. Otherwise the ingest
  writer's clock guard holds ingest until the clock is sane.
- **Backups** are a `pg_dump -Fc` plus `${FORESIGNAL_DATA_DIR}/secret.key`; without the key, stored
  credentials and channel secrets cannot be decrypted.

## Development

- `./gradlew compileJava compileTestJava` compiles; `./gradlew check` runs the tests, the coverage
  gate and the OpenAPI lint.
- `docs/OpenAPI.json` is the authored API contract, served byte for byte at `/v1/openapi.json`
  (Swagger UI at `/docs`). Changes follow the API evolution rules in
  [Architecture §10.1](docs/Architecture.md#101-api-evolution): `/v1` is additive only.
- After changing the spec, run `./gradlew generatePostman` and `./gradlew lintOpenApi` (Node.js
  ≥ 20.19 with `npx`; pass `-PskipOpenApiLint=true` to skip the lint where Node.js is missing).
- `./gradlew checkSdkGeneration` optionally generates TypeScript and Dart clients into
  `build/sdk-check` as a smoke test.

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
- [Operator Guide](docs/Operator-Guide.md) — running, tuning and backing up an instance
- [Frontend Guide](docs/Frontend-Guide.md) — map clients: tile keys, templates, retries and per-client recipes
- [Adapter Guide](docs/Adapter-Guide.md) — writing a source adapter

## Licence

MIT — see [LICENSE](LICENSE). Third-party notices and Foresignal's code-reuse policy
are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
