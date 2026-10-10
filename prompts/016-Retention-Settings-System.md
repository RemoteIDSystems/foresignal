# 016 — Retention Settings System

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/016-CR-Retention-Settings-System.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/015-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §11 (retention), §7.2 (admin token rotation)
- `docs/Product-Charter.md` FR-17, FR-30, FR-32, FR-33, FR-34, NFR-11
- `docs/OpenAPI.json`: operations tagged `Settings` and `System`, schemas `RetentionSettings`, `RetentionRunResult`, `CorsSettings`, `SettingsBundle`, `ImportReport`, `SystemInfo`, `AdminTokenResponse`

## Objective

Finish the operator surface so that **everything** is configurable through the API, as a headless service requires: retention, CORS, export/import, system status, token rotation and metrics.

## Tasks

### 1. Retention (`ai.elaro.retention`)

- **API:** `getRetention` / `updateRetention`.
  - PUT replaces the whole `RetentionSettings`. Validate durations (≥ 1h). **Reject** unknown source ids in `sources` with a 400 that lists them.
  - Store in `app_setting['retention']`.
- **`RetentionJob`:** `@Scheduled` hourly, with an initial delay of 5 min after startup. Also `runRetention` (POST) runs it synchronously and returns `RetentionRunResult`.
  - A guard prevents concurrent runs: a second call while one is running returns 409 `ALREADY_RUNNING`.
- **Algorithm (per source, window = source override ?? default):**
  1. Delete stale heads: `DELETE FROM record_head WHERE source_id = ? AND observed_at < now() - window`, in chunks.
  2. Delete versions: `DELETE FROM record_version WHERE seq IN (SELECT seq FROM record_version v WHERE v.source_id = ? AND v.ingested_at < now() - window AND NOT EXISTS (SELECT 1 FROM record_head h WHERE h.seq = v.seq) LIMIT 10000)`, looping until 0. Each chunk is its own transaction.
  3. Also run the version cleanup for `source_id`s present in `record_version` but no longer registered, using the default window.
- **Other tables:**
  - alerts older than `alerts`
  - `source_usage` older than `usage`
  - `notification_delivery` in `delivered`/`failed` older than 7 days
- **Settings shape:** `RetentionSettings` gains `briefings` (default `180d`) and `ask` (default `90d`), accepted and stored now. Their tables arrive in prompts 022, 024 and 025, and each of those prompts extends `RetentionJob` with its own cleanup step. Design `RetentionJob` as a list of `RetentionStep` beans so later prompts just add a step.
- **Metrics:** `elaro.retention.deleted{table}`; log a summary line per run.
- **Concurrency:** this job deletes `record_head` rows, while the ingest writer upserts them. That is safe: a deleted stale head simply gets recreated as version N+1 if the record reappears. Document that version numbering continues from the remaining versions; if none remain, it restarts at 1 and history continuity is lost by design. Choose the simplest consistent behaviour and document it.

### 2. CORS API

`getCors` / `updateCors`, using `CorsSettingsService` from prompt 003.

### 3. Export and import (`ai.elaro.settings`)

- **`exportSettings`** returns a `SettingsBundle` containing:
  - all source settings (enabled, interval, params, budget, retention; **no credentials**)
  - retention, CORS
  - trigger rules (full objects with ids)
  - notification channels (no secret config)
  - `exportedAt`, `elaroVersion`
- **`importSettings`** (PUT):
  - applies the source settings for known ids and lists unknown ids in `sourcesUnknown`
  - enabling a source that needs missing credentials is applied as `enabled=false`, with a warning
  - upserts triggers and channels by id; imported channels without secret config keep the existing secrets, if any
  - applies retention and CORS if present
  - everything runs in one transaction; return `ImportReport`
  - validation errors anywhere → 400 with all errors, nothing applied

### 4. System

- **`getSystem`** (open), returning `SystemInfo`:
  - version (from the `ElaroVersion` bean, prompt 003), `startedAt`, `uptimeSeconds`
  - database: status via `SELECT 1`, `pg_database_size(current_database())`, `postgis_lib_version()`
  - records: current count (`record_head`, non-tombstoned), versions (an estimate via `pg_class.reltuples` is acceptable; note it), `lastSeq`
  - sources: total, enabled, healthy, degraded, failing
  - scheduler: running, activeFetches, ingestQueueDepth
  - streams: change and alert subscriber counts
- **`rotateAdminToken`** (admin): returns `AdminTokenResponse` exactly once. The old token stops working immediately.

### 5. Metrics

Ensure `/actuator/prometheus` exposes the custom metrics from earlier prompts, plus `elaro.records.current` (gauge) and `elaro.sse.clients{stream}` (gauge). Review the names for consistency and list them in the CR.

## Tests to write (do not run)

- `RetentionJobIT`:
  - per-source windows and the default
  - heads kept when fresh, deleted when stale
  - versions referenced by a head are never deleted
  - unregistered-source cleanup
  - alerts, usage and delivery cleanup
  - chunking (seed > 10k rows using `generate_series` inserts)
  - concurrent-run 409
  - ingest after head deletion behaves as documented
- `RetentionControllerIT`: get/put validation (unknown source → 400, below 1h → 400), run endpoint.
- `SettingsExportImportIT`: a round trip reproduces the state; credentials and secrets are never exported; unknown sources reported; credential-less enable downgraded with a warning; atomic failure.
- `SystemControllerIT`: fields present and plausible; open access.
- `AdminTokenRotationIT`: the new token works, the old one fails, the response is shown once.
- `CorsControllerIT`.

## Acceptance criteria

- All `Settings` and `System` operations match `docs/OpenAPI.json`.
- No configuration requires editing files beyond the bootstrap env vars in Architecture §13.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `016: Retention Settings System`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/016-CR-Retention-Settings-System.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `016: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
