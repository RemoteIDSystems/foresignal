# 006 — Health Tracking

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/006-CR-Health-Tracking.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/005-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §5.4 (HealthTracker and the status table)
- `docs/Product-Charter.md` FR-10, FR-11, G6, G8
- `docs/OpenAPI.json` schemas `SourceHealth`, `HealthStatus`

## Objective

Make every source self-diagnosing. The health model is how the operator, and later the AI adapter-maintenance agent, learn that an adapter is broken, including the silent case where a fetch "succeeds" but returns nothing because the upstream format changed.

## Tasks

### 1. HealthTracker (`ai.elaro.collect`)

Backed by `source_health`; extend `SourceHealthEntity` if needed. All updates are short transactions.

- `recordAttempt(sourceId)`: sets `last_attempt_at`.
- `recordSuccess(sourceId, int seen, int changed, boolean notModified)`:
  - sets `last_success_at` and `last_records_seen` / `last_records_changed`
  - resets `consecutive_failures`
  - appends `seen` to `recent_record_counts` (keep the last 5) **unless** `notModified` (a 304 says nothing about format health)
  - calls `BackoffRegistry.onSuccess`
- `recordFailure(sourceId, Throwable error)`:
  - increments `consecutive_failures`
  - sets `last_error_at`, `last_error` (exception class simple name + ": " + message, ≤ 2000 chars, no credentials; reuse the redaction helper from prompt 005) and `last_http_status` when the error is an `UpstreamHttpException`
- `recordRefused(sourceId)`: a budget refusal. This is **not** a failure; it is counted via `UsageMeter` only.
- Streaming hooks: `recordStreamConnected`, `recordStreamDisconnected(error)` and `recordStreamFlush(seen, changed)`, which call the methods above.

### 2. Status derivation (`HealthEvaluator`, pure function)

Inputs: settings (enabled), the health row, `lastHour` usage, `BudgetStatus`, `zeroResultsNormal` and the clock. Output: `HealthStatus` plus `statusReason`. Apply the table from Architecture §5.4 **in order**:

1. Not enabled → `disabled`, reason "source disabled".
2. No attempt yet → `idle`.
3. In backoff → `failing`, reason "in backoff until …". Also `failing` when `consecutive_failures ≥ 3`, with reason "3 consecutive failures: <last_error>".
4. Last run failed (1–2 consecutive) → `degraded`.
5. Silent-break rule: the last success had 0 records seen while ≥ 3 of the previous 5 counts were > 0, and `zeroResultsNormal` is false → `degraded`, reason "returned 0 records; previously non-empty — possible upstream format change".
6. Refused > 20% of attempted requests in the last hour → `degraded`, reason "budget refusals x% in last hour".
7. Streaming source enabled but not connected for > 2 minutes → `degraded`.
8. Otherwise → `healthy`.

Keep thresholds as named constants.

### 3. HealthService

- `SourceHealthView health(sourceId)` and `List<SourceHealthView> all(Optional<Set<HealthStatus>> filter)`.
- Each view includes every field of OpenAPI `SourceHealth`:
  - `errorRateLastHour` = errors / requests over the last hour from `UsageMeter` (0 when there were no requests)
  - budget fields from `BudgetGovernor.status`
  - `requestsLastHour`, `bytesLastHour`, `refusedLastHour`
- Wire health into `SourceService` views (the seam left in prompt 004), so the source summary and detail carry `status`, `lastSuccessAt` and `budgetUsedToday`.
- Expose Micrometer gauges: `elaro.source.status{source, status}` (1 for the current status) and `elaro.source.consecutive_failures{source}`.

## Tests to write (do not run)

- `HealthEvaluatorTest`: table-driven, covering every rule, rule precedence, and boundary values (exactly 3 failures, exactly 20%, a 304 not polluting counts, `zeroResultsNormal`).
- `HealthTrackerIT`: success and failure transitions persisted; ring buffer of 5; error message truncation and redaction; HTTP status captured.
- `HealthServiceIT`: views combine usage and budget correctly; filtering by status.

## Acceptance criteria

- A source whose adapter starts returning empty results after a history of non-empty ones shows `degraded` with the format-change reason.
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
2. **Commit** all changes with the message `006: Health Tracking`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/006-CR-Health-Tracking.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `006: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
