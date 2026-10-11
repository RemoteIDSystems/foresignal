# 038 — Stats Rollups And Glance

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/038-CR-Stats-Rollups-And-Glance.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/037-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §1 item 10, §2 (widgets, e-ink and watches row), §3 rows "Stats / rollups", "Glance" and "Metrics", §8 item 23 ("028d" = this prompt)
- `docs/Core-Data-Model.md`:
  - §8: `record_version` columns, `persist_reason` codes, `seq_clock` and `cutoffSeq`
  - §11: the trigger engine's durable cursor, the pattern to copy
  - §13: the consumer clamp, which this prompt extends
- `docs/Architecture.md` §22 (digest counts), §15 (`openReads`)
- Completion Reports of 017 (alert lifecycle, severity), 019 (RetentionStep, RetentionSettings, data-age gauges), 023 (scopes, private gate), 025 (MCP registry), 029 (DigestRenderer activity query, `GET /v1/digest`) and 037 (`TimelineBackend`)
- H3 Java binding `com.uber:h3` (Apache-2.0; 4.5.0 on Maven Central as of 2026-10; check that native binaries ship for linux-x86_64, linux-aarch64 and darwin)
- Prometheus exposition format and OpenMetrics text format

## Objective

Give every client the same cheap aggregates instead of letting each one page raw history:

- a rollup table that keeps activity counts after retention deletes the versions
- time series, grids and top-N endpoints, plus a count mode
- a ≤ 2 KB glance document for widgets, watches and e-ink
- operator-defined metric projections that Prometheus can scrape

The rollup is driven by a durable seq cursor, never by an in-memory listener alone, so a crash never makes counts drift.

> **v1.3 note:** prompt 119 adds region resolutions (2 = region-hour, 3 = region-day) to `activity_rollup` with a nullable `region_code` column. Keep the entity, job and stats API generic enough to accept extra resolutions without a redesign.

## Tasks

### 1. `activity_rollup` (`ai.foresignal.stats`)

- Hibernate entity `ActivityRollupEntity`:
  - PK `(resolution smallint, bucket_start timestamptz, source_id, kind, cell bigint)`. `resolution` is 0 = hour, 1 = day. `cell` is an H3 index at `foresignal.stats.cellRes` (default 4, ≈ 1,770 km²), or 0 for records without geometry.
  - `cell_lon`, `cell_lat` (the cell centre, used for bbox filtering).
  - Measures: `versions` (persisted, excluding HEARTBEAT and KEYFRAME), `entities` (distinct `external_id`), `firsts` (FIRST and REAPPEAR), `tombstones`, `edges` (TRIGGER_EDGE).
  - Indexes `(resolution, bucket_start)` and `(source_id, resolution, bucket_start)`.
- Buckets are on the **transaction axis**: hour H covers `seq ∈ [S(H), S(H+1h))` from `seq_clock`. That is exact, prunes partitions, and is the only axis computable after the raw rows are gone. Document that `timeField=event|observed` stats are served from raw history inside retention (§2).
- **`RollupJob`** runs every 5 min:
  - It keeps a durable cursor `stats.rollup.cursor` (`{lastHour, lastDay, seq}`) in `app_setting`.
  - Hour H is closed once `now ≥ H + 1 h + 2 min` and the writer's last ingested time has passed it.
  - For each closed hour it runs one `GROUP BY source_id, kind, cell` over that seq range (centroid → H3 in Java, streamed with a JDBC fetch size). It writes the rows and advances the cursor, in one transaction.
  - Days are computed the same way from raw rows (distinct entities are not additive, so never sum hours into days).
  - First start: backfill from the oldest retained seq, bounded by `foresignal.stats.backfill` (default 30d).
  - Catch up in chunks of ≤ 6 hours per run.
- **Retention interaction:**
  - Add `stats.rollup.cursor` to 019's consumer clamp (`min(trigger, embedding, rollup)`), so raw rows are never deleted before they are rolled up.
  - Add a `StatsRollupRetentionStep` with `RetentionSettings.stats = {hourly: "35d", daily: "1825d"}`.
- **Privacy:** sources whose descriptor or settings are `private` (008 flag) are skipped unless the setting `stats.includePrivate[sourceId]` is true. Rows hold no entity ids. Add the per-source setting `statsEnabled` (default true) to exclude noisy sources.
- `POST /v1/stats/rollup/rebuild {from, to}` (admin) recomputes closed buckets that still have raw data.

### 2. Stats API (read scope; `openReads` applies)

- `GET /v1/stats/timeseries`
  - Parameters: `source`, `kind` (multi), `bbox|place`, `from`, `to`, `bucket` (`1h|1d|auto`, ≤ 2,000 buckets), `measure=versions|entities|firsts|tombstones|edges|alerts`, `groupBy=none|source|kind`, `timeField=ingested|effective|observed|event`.
  - Backend selection: `timeField=ingested` reads `activity_rollup`, plus raw rows for the open hour. Other time fields read raw `record_version` and require `from ≥ retainedFrom`. `alerts` reads `alert.fired_at`.
  - The response states `backend: rollup|raw|mixed`.
- `GET /v1/stats/grid`
  - Parameters: `source`, `kind`, `bbox` (required), `from`, `to`, `measure`, `res` (≤ `cellRes`).
  - Rollup mode returns an H3 GeoJSON `FeatureCollection`: aggregate to parents with `cellToParent` in Java, build polygons with `cellToBoundary`, and split at the antimeridian.
  - Raw mode (`mode=raw&sizeKm=`, range ≤ 7d) uses `ST_HexagonGrid` **in EPSG:3857**, then transforms back.
  - Cap 10,000 cells, else 400 `STATS_TOO_LARGE`. The response carries `cellSystem: h3|hex3857`.
- `GET /v1/stats/top?by=source|kind|cell|entity&measure&from&to&limit ≤ 100`. `entity` is raw only, inside retention; it returns `{sourceId, externalId, title, count}`.
- `count=true` on `GET /v1/records`, `/v1/history` and `/v1/search` returns `{count, exact, watermarkSeq}` with no items. It counts with `LIMIT 1000001`; above that it returns `exact: false`.
- Every stats query sets `SET LOCAL statement_timeout='10s'`; on timeout return 503 `QUERY_TIMEOUT`.
- Register a `RollupTimelineBackend` so 037's `/v1/timeline` serves `timeField=ingested` ranges older than retention from the rollup.

### 3. Glance: `GET /v1/glance`

- `GlanceService` assembles:
  - `serverTime`
  - `status` (`ok|degraded|failing`: the worst of the writer clock guard, readiness and source-health tallies)
  - `alerts{open: {critical, warning, info}, latest[≤3 {id, severity, shortTitle, firedAt, near?}]}`, where open means unresolved and unacknowledged per 017
  - `sources{healthy, degraded, failing}`
  - `briefing{title, at, url}` (the latest run)
  - `activity{versionsLastHour}`
  - `lastSeq`
  - `links` (deep links via `clientLinks` from 023)
- Add a `GlanceContributor` SPI (ordered beans, each given a byte budget). Prompt 056 adds weather-at-place through it; 040 may add `near` text.
- **Size:** ≤ 2,048 bytes of UTF-8 JSON. Drop `latest` entries, then contributor sections, until it fits. Assert this in tests.
- **Visibility:** read scope. Under `openReads` with no token, return `status`, `serverTime`, `alerts.latest` (already public through `/v1/alerts`) and `lastSeq`; **omit all counts** unless `glance.exposeCountsAnonymously=true` (app setting, default false). Private-source alerts follow the 023 gate.
- `?place=<id>` scopes the alerts to a 036 place.
- `?format=text` (or `Accept: text/plain`): ≤ 8 lines × 40 ASCII chars, for e-ink, LED matrices and `curl`.
- Caching: 15 s server cache per (caller class, place, format). Strong ETag over the body, `If-None-Match` → 304, `Cache-Control: private, max-age=15`.
- MCP `get_glance` (read).
- Docs: TRMNL (cloud webhooks ≤ 12/hour, so coalesce to ≥ 10 min; BYOS needs a renderer such as Terminus), MagicMirror, Home Assistant REST sensor, Garmin and Wear OS (these need native code).

### 4. Metric projections: `/v1/metrics`

- Definitions in `app_setting['metrics'] = {projections: [...]}`, edited with `GET/PUT /v1/settings/metrics` (admin). Each projection has:
  - `name`: `^[a-z_][a-z0-9_]*$`, emitted as `foresignal_data_<name>`
  - `source`, optional `kind`
  - `path`: the 017 trigger path syntax; reuse its parser
  - `labels[≤ 4 {name, path}]`; the label value is always `source` + `externalId` unless `labels` replaces it
  - `unit`, `help`, `maxSeries` (default 500)
- Validation: name regex, no duplicate names, paths not in `volatileFields`, private sources rejected unless `allowPrivate: true`.
- `ProjectionService`:
  - at start, seeds from `record_head ⋈ record_live` (the merged payload)
  - subscribes to ChangeBus and LiveBus; under SAMPLED, live values matter
  - non-numeric values are skipped and counted in `foresignal.projection.coercion_failures{projection}`
  - drops a series when the head is tombstoned, expires (`valid_until`, checked every minute) or is purged
- A dedicated `PrometheusMeterRegistry` (not the global one), rendered by `GET /v1/metrics`:
  - text format 0.0.4, or OpenMetrics when `Accept` asks for it
  - each value has a companion `foresignal_data_<name>_observed_timestamp_seconds`
  - a global cap of 10,000 series raises a system event once and stops adding series
- `/v1/metrics` is READ (Prometheus sends `authorization: Bearer`). It is **not** on the open actuator endpoint.
- If 019 did not add `foresignal.source.data_age_seconds{source}` and `foresignal.alerts.open{severity}`, add them to the main registry.

### 5. Briefing digest on stats

- Replace 029's bespoke activity counts in `DigestRenderer` (and `GET /v1/digest`) with `StatsService.timeseries(... groupBy=kind|source, timeField=ingested)` over the period and the previous equal-length period, and delete the old query.
- `/v1/kinds` counts stay as they are (current state).
- The golden digest fixtures must still match, or the change is documented in the CR.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - Operations `getStatsTimeseries`, `getStatsGrid`, `getStatsTop`, `rebuildStatsRollup` (admin), `getGlance` (read; `application/json` and `text/plain`), `getMetrics` (read; `text/plain`), `getMetricSettings`/`updateMetricSettings` (admin), each with `x-foresignal-scope`.
  - The `count` parameter on `listRecords`, `queryHistory` and `search`.
  - Schemas `StatsSeries`, `StatsTop`, `CountResult`, `Glance`, `MetricProjection`, `MetricSettings`.
  - `RetentionSettings.stats`, and error codes `STATS_TOO_LARGE` and `QUERY_TIMEOUT`.
- Add a `BODIES` entry for `updateMetricSettings` (a USGS magnitude gauge) and `rebuildStatsRollup`, then regenerate Postman.
- `AccessPolicy`:
  - READ for `GET /v1/stats/**`, `/v1/glance` and `/v1/metrics`
  - ADMIN for `POST /v1/stats/rollup/rebuild`
  - `/v1/settings/metrics` is already admin
- `docs/Operator-Guide.md`:
  - stats and rollup storage estimate (rows ≈ hours × sources × kinds × occupied cells; global aircraft at res 4 ≈ 0.5M rows/day, so lower `cellRes` or exclude the source)
  - a Prometheus scrape config with a bearer token
  - a Grafana Infinity example
- `docs/Agent-Guide.md`: `get_glance`.

## Tests to write (do not run)

- `RollupJobIT`:
  - closed-hour detection
  - exact counts per measure for seeded versions across two hours and kinds
  - HEARTBEAT excluded
  - daily distinct entities differ from the sum of hours
  - cursor survives restart (no double count)
  - backfill bound
  - private source skipped
  - retention clamp holds raw rows behind the cursor
- `StatsRollupRetentionStepIT`.
- `StatsServiceIT`: backend selection (rollup / raw / mixed), bucket limit, the grid in H3 and hex3857 (cells not distorted at 60°N), top entity, 503 on an injected timeout.
- `H3CellsTest`: parent aggregation, boundary polygons, antimeridian.
- `CountModeIT`: exact count; `exact=false` above the cap.
- `GlanceServiceTest`: ≤ 2,048 bytes under a flood of alerts and contributors; anonymous view has no counts; opt-in setting; text format line and width limits.
- `GlanceControllerIT`: ETag/304, scopes, `place`.
- `ProjectionServiceIT`: seeding from heads, live update, tombstone and expiry drop, coercion counter, series cap event, label validation.
- `MetricsEndpointIT`: exposition format parses; 401 with `openReads=false` and no token.
- `DigestRendererIT` (update): counts come from `StatsService`.

## Acceptance criteria

- Counts for a rolled-up hour are unchanged after the raw versions are deleted by retention.
- `/v1/glance` never exceeds 2 KB and exposes no counts anonymously by default.
- `/v1/metrics` serves projected values with observed timestamps and never appears on `/actuator/prometheus`.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `038: Stats Rollups And Glance`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/038-CR-Stats-Rollups-And-Glance.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `038: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
