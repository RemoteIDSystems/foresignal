# Elaro — Roadmap & Task Decomposition

Companion to the [Executive Summary](Executive-Summary.md), [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json) and [Source Catalog](Source-Catalog.md).

## 1. Process

1. Each unit of work is a prompt file in `/prompts`, named `NNN-<Prompt-Description>.md`.
2. A Claude Code session executes one prompt at a time, in order.
3. On completion it writes a Completion Report to `/completion-reports/NNN-CR-<Prompt-Description>.md` and returns the commit hash.
4. The planner reads the report and adjusts the next prompt(s) if needed. Then the next prompt runs.
5. This repeats until the project is built.

Prompts marked **Drafted** exist in `/prompts`. **Planned** prompts are written after the preceding Completion Reports are reviewed, because their content depends on what was actually built.

## 2. Guardrails (in every prompt)

1. **Tests:** write unit and integration tests targeting ≥ 90% line coverage for all code in the prompt, but **do not run any tests** until the project is code complete. Tests are first run in prompt 040.
2. **Finish with Compile → Commit → Push.** Compile with `./gradlew compileJava compileTestJava` (never `build`, `test` or `check`, which would run tests). Commit, push to `origin main`, and report the commit hash in the Completion Report.
3. **Hibernate only during development.** Hibernate manages the schema with `ddl-auto=update`. **Flyway is not added until prompt 042** (production hardening).

## 3. Completion Report template

```markdown
# NNN-CR-<Prompt-Description>

| | |
|---|---|
| Prompt | NNN-<Prompt-Description>.md |
| Status | Complete / Partial / Blocked |
| Implementation commit | <full 40-char hash> |
| Date | YYYY-MM-DD |

## Summary
What was accomplished, in 3–6 sentences.

## Files created / modified
- path — purpose

## Deviations from the prompt
Each deviation, and why.

## Issues found / open questions
Problems, ambiguities, things the next prompt must know.

## Tests written (not run)
Test classes and what they cover; estimated coverage of new code.

## Compile result
Output summary of `./gradlew compileJava compileTestJava`.

## Notes for the next prompt
Anything the planner should adjust.
```

**Commit sequence:**
1. Commit the implementation and capture its hash as the *implementation commit*.
2. Write the Completion Report containing that hash.
3. Commit the report separately (`NNN: completion report`).
4. Push both.

The final chat message must state the implementation commit hash.

## 4. Prompt index

### Phase A — Foundation

| # | Prompt | Scope | Depends on | Status |
|---|---|---|---|---|
| 001 | `001-Project-Scaffold.md` | Gradle wrapper and build, version catalog, Spring Boot app, config, Docker Compose, Dockerfile, LICENSE, README, notices, test base classes, JaCoCo gate | — | Drafted |
| 002 | `002-Persistence-Model.md` | All JPA entities and repositories, `SchemaBootstrap`, `AppSettingService`, duration parsing | 001 | Drafted |
| 003 | `003-Security-And-Secrets.md` | Admin token, auth filter, credential encryption, install id, ProblemDetail handling, dynamic CORS | 002 | Drafted |
| 004 | `004-Source-Framework.md` | Adapter SPI, descriptors, param validation, registry, families, `SourceService` (settings, credentials) | 003 | Drafted |
| 005 | `005-Upstream-Client-And-Budgets.md` | `UpstreamClient`, `BudgetGovernor`, `UsageMeter`, conditional GET, caps, backoff | 004 | Drafted |
| 006 | `006-Health-Tracking.md` | `HealthTracker`, status derivation, zero-result detection | 005 | Drafted |
| 007 | `007-Ingest-Writer-And-Change-Bus.md` | Single-writer ingest, hashing, versioning, head upserts, change bus | 006 | Drafted |
| 008 | `008-Collector-Scheduler.md` | Reconciling scheduler, fetch execution, fetch-now/probe services, streaming supervisor | 007 | Drafted |
| 009 | `009-USGS-Earthquakes-Adapter.md` | First adapter, fixture and contract tests, reference adapter pattern | 008 | Drafted |

### Phase B — API

| # | Prompt | Scope | Depends on | Status |
|---|---|---|---|---|
| 010 | `010-Sources-API.md` | `/v1/sources/**` endpoints | 009 | Drafted |
| 011 | `011-Records-Changes-API.md` | `/v1/records`, `/kinds`, `/versions`, `/history`, `/changes`, GeoJSON, cursors | 010 | Drafted |
| 012 | `012-SSE-Change-Stream.md` | `SseHub`, `/v1/changes/stream`, replay and resume | 011 | Drafted |
| 013 | `013-Vector-Tiles.md` | `/v1/tiles/{layer}/{z}/{x}/{y}.mvt` | 011 | Drafted |

### Phase C — Triggers and operations

| # | Prompt | Scope | Depends on | Status |
|---|---|---|---|---|
| 014 | `014-Trigger-Engine-And-Alerts.md` | Rules CRUD, rule SQL, engine with durable cursor, alerts API, alert SSE, test endpoint | 012 | Drafted |
| 015 | `015-Webhooks.md` | Webhook CRUD, deliveries, HMAC signing, retry worker, test endpoint | 014 | Drafted |
| 016 | `016-Retention-Settings-System.md` | Retention job and API, CORS API, export/import, system info, token rotation, metrics | 015 | Drafted |
| 017 | `017-OpenAPI-Conformance.md` | springdoc wiring, annotations, `/v1/openapi.json`, `/docs`, contract test against `docs/OpenAPI.json` | 016 | Drafted |
| 018 | `018-End-To-End-Milestone.md` | E2E test (USGS → trigger → SSE → webhook), README quickstart, operator guide | 017 | Drafted |

### Phase D — Wave 1 sources (see Source Catalog)

| # | Prompt | Sources | Status |
|---|---|---|---|
| 019 | `019-Aircraft-Adapters.md` | `adsblol-mil`, `adsblol-region-*`, `opensky-states` | Planned |
| 020 | `020-AISStream-Adapter.md` | `aisstream` (first streaming adapter) | Planned |
| 021 | `021-Space-Adapters.md` | `celestrak-gp`, `launch-library` | Planned |
| 022 | `022-FIRMS-Adapter.md` | `nasa-firms` | Planned |
| 023 | `023-GDELT-Events-Adapter.md` | `gdelt-events` | Planned |
| 024 | `024-Hazard-Adapters.md` | `nws-alerts`, `nhc-cyclones`, `gdacs` | Planned |
| 025 | `025-IODA-Outages-Adapter.md` | `ioda-outages` | Planned |

### Phase E — Wave 2 (God's Eye View ports)

| # | Prompt | Sources | Status |
|---|---|---|---|
| 026 | `026-Local-ADSB-Receiver.md` | `local-adsb-*` | Planned |
| 027 | `027-Transit-GTFS-RT.md` | `transit-*` | Planned |
| 028 | `028-GBFS-Bikeshare.md` | `gbfs-*` | Planned |
| 029 | `029-CCTV-Catalogs.md` | `cctv-*` | Planned |
| 030 | `030-Fire-Perimeters-Radio-Cyclone-Cones.md` | `nifc-perimeters`, `radio-browser`, `nhc-cyclone-cones` | Planned |

### Phase F — Wave 3 (intelligence and specialist)

| # | Prompt | Sources | Status |
|---|---|---|---|
| 031 | `031-Cyber-Feeds.md` | `cisa-kev`, `abusech-feodo`, `abusech-urlhaus` | Planned |
| 032 | `032-Space-Weather-And-Ground-Stations.md` | `noaa-swpc`, `satnogs-stations`, `tinygs` | Planned |
| 033 | `033-Environment-And-Maritime-Intel.md` | `openaq`, `gvp-volcanoes`, `gfw-events`, `ripe-atlas-probes` | Planned |
| 034 | `034-Rail-And-Supplemental-Aircraft.md` | `amtraker`, `digitraffic-rail`, `airplaneslive-mil`, `adsbfi-region-*` | Planned |
| 035 | `035-Conflict-Sources.md` | `deepstate-frontline`, `alerts-in-ua` | Planned |
| 036 | `036-Radio-Sources.md` | `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `aprs-is` (TCP streaming) | Planned |
| 037 | `037-Meshtastic-Sources.md` | `meshtastic-mqtt` (Mesh.md rules), `meshtastic-map` | Planned |
| 038 | `038-Reference-Datasets.md` | `wri-power-plants`, `ourairports` | Planned |
| 039 | `039-Velocity-WorldMonitor-Additions.md` | Sources found in the Velocity / World Monitor surveys not already covered | Planned |

### Phase G — Verification and hardening

| # | Prompt | Scope | Status |
|---|---|---|---|
| 040 | `040-Full-Test-Run-And-Fix.md` | First test execution: run all tests, fix failures, reach ≥ 90% coverage (JaCoCo gate passes) | Planned |
| 041 | `041-Runtime-Verification.md` | `docker compose up`; enable wave-1 sources live; verify budgets, health, triggers, SSE, tiles; run `scripts/smoke.sh` and the Postman collection via Newman against the live broker; soak checklist | Planned |
| 042 | `042-Production-Hardening.md` | Flyway baseline from the Hibernate schema, `ddl-auto=validate`, remove `SchemaBootstrap`, release docs, tag `v1.0.0` | Planned |

## 5. API tooling

- `postman/Elaro.postman_collection.json` and `postman/Elaro-Local.postman_environment.json` are generated from `docs/OpenAPI.json` by `scripts/generate-postman.py`. The collection holds every operation grouped by tag, a 12-step "Quickstart — USGS end to end" folder, an SSE streams folder, bearer auth wired to `{{adminToken}}`, and test scripts that capture ids.
- Regenerate the collection whenever the spec changes. Prompt 017 adds a contract test that fails if any operation is missing.

## 6. Follow-on projects (separate repositories)

1. Reference globe frontend (CesiumJS; reuses God's Eye View UI components)
2. Operator console
3. AI adapter-maintenance agent
4. MCP server for Elaro
5. Gridded products (GFS/ECMWF wind, imagery manifests)
6. Licence and provenance layer
7. Derived layers (GPS jamming, dark vessels, holding patterns)
