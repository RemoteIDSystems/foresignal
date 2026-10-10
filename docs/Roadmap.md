# Elaro — Roadmap

One ordered list of prompts that takes Elaro from an empty repository to a production-ready release. Companion to the [Executive Summary](Executive-Summary.md), [Product Charter](Product-Charter.md), [Architecture](Architecture.md), [OpenAPI spec](OpenAPI.json) and [Source Catalog](Source-Catalog.md).

## 1. Process

1. Each unit of work is a prompt in `/prompts`, named `NNN-<Prompt-Description>.md`.
2. A Claude Code session runs one prompt at a time, in order.
3. When a prompt finishes, it writes `/completion-reports/NNN-CR-<Prompt-Description>.md` and returns the commit hash.
4. The planner reads the report, adjusts the next prompts if needed, and the next prompt runs.

**Status values:** **Done** (completed and pushed), **Ready** (written in `/prompts`), **To write** (in this list but not yet written; it is written once the reports it depends on have been reviewed, because its details depend on what was actually built).

## 2. Guardrails (in every prompt)

1. **Tests:** write unit and integration tests targeting ≥ 90% line coverage, but **do not run any tests** until the project is code complete. Tests are first run in prompt 061.
2. **Finish with Compile → Commit → Push.** Compile with `./gradlew compileJava compileTestJava` (never `build`, `test` or `check`). Commit, push to `origin main`, and report the commit hash in the Completion Report.
3. **Hibernate only during development.** The schema is managed by Hibernate (`ddl-auto=update`). **Flyway is not added until prompt 063.**

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
2. Write the report containing that hash.
3. Commit the report separately (`NNN: completion report`).
4. Push both. The final chat message states the implementation commit hash.

## 4. The list

| # | Prompt | Scope | Status |
|---|---|---|---|
| 001 | `001-Project-Scaffold.md` | Gradle wrapper and build, version catalog, app skeleton, config, Docker, licence, notices, test base classes, JaCoCo gate | Done |
| 002 | `002-Persistence-Model.md` | All JPA entities and repositories, `SchemaBootstrap`, `AppSettingService`, durations, test database cleaner | Done |
| 003 | `003-Security-And-Secrets.md` | Bootstrap admin token, token filter and `AccessPolicy`, credential encryption, install id, version bean, ProblemDetail errors, dynamic CORS | Ready |
| 004 | `004-Source-Framework.md` | Adapter SPI (polling, streaming, push), descriptors, param validation, registry (static and dynamic), families, `SourceService` | Ready |
| 005 | `005-Upstream-Client-And-Budgets.md` | `UpstreamClient`, `BudgetGovernor`, `UsageMeter`, conditional GET, response caps, backoff | Ready |
| 006 | `006-Health-Tracking.md` | `HealthTracker`, status derivation, silent-format-change detection | Ready |
| 007 | `007-Ingest-Writer-And-Change-Bus.md` | Single-writer ingest, hashing, versioning, head upserts, `ChangeBus` | Ready |
| 008 | `008-Collector-Scheduler.md` | Reconciling scheduler, fetch execution, fetch-now and probe, streaming supervisor | Ready |
| 009 | `009-USGS-Earthquakes-Adapter.md` | First adapter, fixtures, contract tests, Adapter Guide | Ready |
| 010 | `010-Sources-API.md` | `/v1/sources/**` | Ready |
| 011 | `011-Records-Changes-API.md` | Records, kinds, versions, history, changes, GeoJSON, cursors | Ready |
| 012 | `012-SSE-Change-Stream.md` | Generic SSE channel, `/v1/changes/stream`, replay and resume | Ready |
| 013 | `013-Vector-Tiles.md` | `/v1/tiles/{layer}/{z}/{x}/{y}.mvt` | Ready |
| 014 | `014-Trigger-Engine-And-Alerts.md` | Rules (match/enter/exit, severity, enrichment fields), engine with durable cursor, alerts API and SSE, `AlertBus` | Ready |
| 015 | `015-Notification-Channels.md` | Webhook, Slack, Mattermost, Discord, Teams, Google Chat, Telegram, Matrix, ntfy, Gotify, Pushover, email; routing, durable delivery, rate limits, system events | Ready |
| 016 | `016-Retention-Settings-System.md` | Retention (pluggable steps), CORS, export/import, system info, token rotation, metrics | Ready |
| 017 | `017-OpenAPI-Conformance.md` | Serve the authored spec, Swagger UI, drift test, Postman sync and contract test | Ready |
| 018 | `018-End-To-End-Milestone.md` | E2E test (USGS → trigger → SSE → channels), README quickstart, Operator Guide, smoke script | Ready |
| 019 | `019-Scoped-API-Tokens.md` | `api_token`, read/agent/admin scopes, bootstrap-token migration, `/v1/tokens`, `openReads`, server settings | Ready |
| 020 | `020-Inbound-Webhooks-And-Custom-Sources.md` | PUSH sources, `/v1/custom-sources`, ingest keys, `POST /v1/ingest/{sourceId}` | Ready |
| 021 | `021-MCP-Server.md` | MCP streamable HTTP at `/mcp`, tool catalogue with scopes, output shaping | Ready |
| 022 | `022-LLM-Providers.md` | Anthropic (official SDK), OpenAI-compatible, Ollama; roles, budgets, usage, provider health, compose `local-llm` profile | Ready |
| 023 | `023-Embeddings-And-Semantic-Search.md` | DB image with pgvector, `embedding_document`, embedding worker, reindex, `/v1/search` | Ready |
| 024 | `024-Ask-Elaro.md` | Tool loop, retrieval-only mode, verified citations, threads, proposed actions, streaming | Ready |
| 025 | `025-Scheduled-Briefings.md` | Cron briefings, digest and LLM modes, runs, delivery to channels, preview | Ready |
| 026 | `026-Alert-Enrichment.md` | LLM context summaries on alerts, channel enrichment wait, `alert.enriched` events | Ready |
| 027 | `027-Web-Push.md` | VAPID keys, subscriptions, RFC 8291 encryption, `webpush` channel | Ready |
| 028 | `028-Agent-Integrations.md` | OpenClaw skill, Claude Desktop/Code and n8n MCP configs, agent cookbook | Ready |
| 029 | `029-Aircraft-Adapters.md` | `adsblol-mil`, adsb.lol global or regional, `opensky-states` | To write |
| 030 | `030-AISStream-Adapter.md` | `aisstream` (first streaming adapter) | To write |
| 031 | `031-Space-Adapters.md` | `celestrak-gp`, `launch-library` | To write |
| 032 | `032-FIRMS-Adapter.md` | `nasa-firms` | To write |
| 033 | `033-GDELT-Events-Adapter.md` | `gdelt-events` | To write |
| 034 | `034-Hazard-Adapters.md` | `nws-alerts`, `nhc-cyclones`, `gdacs` | To write |
| 035 | `035-IODA-Outages-Adapter.md` | `ioda-outages` | To write |
| 036 | `036-Local-ADSB-Receiver.md` | `local-adsb-*` | To write |
| 037 | `037-Transit-GTFS-RT.md` | `transit-*` | To write |
| 038 | `038-GBFS-Bikeshare.md` | `gbfs-*` | To write |
| 039 | `039-CCTV-Catalogs.md` | `cctv-*` | To write |
| 040 | `040-Fire-Perimeters-Radio-Cyclone-Cones.md` | `nifc-perimeters`, `radio-browser`, `nhc-cyclone-cones` | To write |
| 041 | `041-Cyber-Feeds.md` | `cisa-kev`, `abusech-feodo`, `abusech-urlhaus` | To write |
| 042 | `042-Space-Weather-And-Ground-Stations.md` | `noaa-swpc`, `satnogs-stations`, `tinygs`, `sondehub` | To write |
| 043 | `043-Environment-Sensors.md` | `openaq`, `safecast-radiation`, `epa-radnet`, `ndbc-buoys` | To write |
| 044 | `044-Rail-And-Supplemental-Aircraft.md` | `amtraker`, `digitraffic-rail`, `airplaneslive-mil`, `adsbfi-region-*` | To write |
| 045 | `045-Conflict-Sources.md` | `deepstate-frontline`, `alerts-in-ua`, `ua-air-raid-siren`, `acled`, `ucdp-ged` | To write |
| 046 | `046-Radio-Sources.md` | `pskreporter-ft8`, `kiwisdr`, `openmhz-systems`, `aprs-is` | To write |
| 047 | `047-Meshtastic-Sources.md` | `meshtastic-mqtt` (Mesh.md rules), `meshtastic-map` | To write |
| 048 | `048-Keyless-AIS.md` | `ais-kystverket` (raw TCP), `ais-digitraffic` (MQTT) | To write |
| 049 | `049-Aviation-Safety.md` | `faa-tfr`, `faa-nas-status`, `awc-sigmets` | To write |
| 050 | `050-Maritime-Intelligence.md` | `nga-msi-warnings`, `imf-portwatch-chokepoints`, `gfw-events` | To write |
| 051 | `051-Weather-And-Seismic-International.md` | `meteoalarm`, `spc-storm-reports`, `emsc-seismic` | To write |
| 052 | `052-Disasters-And-Humanitarian.md` | `nasa-eonet`, `reliefweb-disasters`, `unhcr-population`, `who-don` | To write |
| 053 | `053-Sanctions-And-Filings.md` | `ofac-sdn`, `uk-ofsi-sanctions`, `un-sanctions`, `sec-8k` | To write |
| 054 | `054-Internet-Measurement.md` | `cloudflare-radar-outages`, `ripe-atlas-probes`, `gpsjam-daily` | To write |
| 055 | `055-Energy-And-Markets-Series.md` | `eia-petroleum`, `gie-agsi`, `ember-electricity`, `kalshi` (records without geometry) | To write |
| 056 | `056-Reference-Datasets.md` | `wri-power-plants`, `ourairports`, `submarine-cables`, `gvp-volcanoes` | To write |
| 057 | `057-Derived-Layers.md` | GPS jamming, AIS gap, loiter, rendezvous, deception checks, holding patterns: derived sources fed by the change stream | To write |
| 058 | `058-Gridded-Weather-Products.md` | GFS/ECMWF wind via GRIB2 byte-range, a products API for grids | To write |
| 059 | `059-Licence-And-Provenance.md` | Per-record licence tracking, egress policy, attribution aggregation | To write |
| 060 | `060-Adapter-Doctor.md` | LLM-assisted diagnosis of failing adapters: probe, compare with the fixture, explain, propose a parser fix | To write |
| 061 | `061-Full-Test-Run-And-Fix.md` | First test execution: run everything, fix failures, reach ≥ 90% coverage | To write |
| 062 | `062-Runtime-Verification.md` | `docker compose up`; core sources live; budgets, health, triggers, channels, briefings, Ask, MCP; smoke script and Postman via Newman; soak checklist | To write |
| 063 | `063-Production-Hardening.md` | Flyway baseline (extensions, GiST, HNSW, GIN), `ddl-auto=validate`, remove `SchemaBootstrap`, release docs, tag `v1.0.0` | To write |

## 5. API tooling

- `postman/Elaro.postman_collection.json` and `postman/Elaro-Local.postman_environment.json` are generated from `docs/OpenAPI.json` by `scripts/generate-postman.py`.
- The collection contains:
  - every operation, grouped by tag
  - two quickstart folders (USGS end to end; channels, briefings and Ask)
  - a streams folder
  - auth wired by scope (`{{adminToken}}`, `{{agentToken}}`, `{{ingestKey}}`)
  - test scripts that capture IDs
- Regenerate it whenever the spec changes. Prompt 017 adds a contract test that fails if any operation is missing.

Frontends built on Elaro (a reference globe, an operator console, mobile) are separate repositories with their own roadmaps. They consume the API, MCP server and Web Push described here.
