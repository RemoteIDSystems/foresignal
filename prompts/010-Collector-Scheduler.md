# 010 — Collector Scheduler

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/010-CR-Collector-Scheduler.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–138).
- **Previous Completion Report:** read `completion-reports/009-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §5.1 (CollectorScheduler), §5.5 (StreamingSupervisor), §4.1 (`FetchContext`)
- `docs/Core-Data-Model.md` §2 (`received_at` is stamped by the framework), §7 (**streaming sinks**), §8 (`fetch_log`), §14 (provenance), and the **"008 Collector Scheduler"** bullets under *Per-prompt changes* (008 is this prompt)
- `docs/Weather-Imagery-Design.md` §2.2 rows "Scheduler", "Probe" and "Modes"
- `docs/Product-Charter.md` FR-4, FR-6, FR-7, G1, NFR-5, NFR-6
- `docs/OpenAPI.json` schemas `FetchOutcome`, `ProbeResult`, `NormalizedRecordPreview`; operations `fetchSourceNow`, `probeSource`
- Code from prompts 004–009: `SourceService`, `CredentialsAccess`, `UpstreamClient`/`FetchTrace`/`RequestRecorder`, `BudgetGovernor`, `BackoffRegistry`, `HealthTracker`, `IngestQueue`, `IngestWriter.status()`, `StartupOrder`, `FetchLogEntry`, `FetchTrigger`, `FetchStatus`

## Objective

Connect subscription to collection. Enabled polling sources are fetched on schedule; disabled ones are not, and the change takes effect within seconds. Every fetch leaves a `fetch_log` row with its provenance and outcome. Streaming adapters run under supervision and feed a policy-aware sink that bounds batch delay and memory. Add the operator's fetch-now and probe services (the HTTP endpoints come in prompt 013).

## Tasks

### 1. `DefaultFetchContext` (`ai.foresignal.collect`)

- `http()`: the source-bound `UpstreamClient.forSource(…)`, with a per-fetch trace collector (prompt 005's `FetchTrace`) and, when probing, a `RequestRecorder`
- `params()` (validated effective params), `credentials()` (via `CredentialsAccess`), `clock()`, `lastSuccessAt()`, `state()` (`AdapterStateStore` on `source_health.adapter_state`), `descriptor()`, `log()` (SLF4J logger `foresignal.source.<id>`)
- DERIVED sources get a context whose `http()` throws (prompt 004 rule).

### 2. `FetchExecutor`

`FetchOutcome execute(String sourceId, FetchTrigger trigger /* SCHEDULED | MANUAL */)`:

1. `HealthTracker.recordAttempt`; `startedAt = clock.now()`.
2. Run `adapter.fetch(ctx)` on a **virtual thread** with the descriptor's `fetchTimeout`; on timeout interrupt and fail with `AdapterTimeoutException`. Dispatch through a small `FetchDispatcher` seam so prompt 042 can route heavy product decodes to a bounded platform pool.
3. `receivedAt = clock.now()` when `fetch` returns; wrap each record as `IngestRecord(record, receivedAt, arrivalIndex)`.
4. Build the `FetchLogEntry` from the collected traces: `request_count` = traces, `url_redacted`/`http_status`/`etag`/`body_sha256` from the last trace, `bytes` = sum, `adapter_version` = `ForesignalVersion`, `licence` from the descriptor, `principal` null, `coverage` from `FetchResult`.
5. Outcome table:

| Result | `fetch_log` | Health and ingest |
|---|---|---|
| Records returned | status OK, inserted **by the writer** (step 8 of prompt 009) | submit `IngestBatch` (timeout = remaining fetch time + 30 s), await `IngestOutcome`, `recordSuccess(outcome)`, which sets `last_data_at` |
| `notModified` (304) | NOT_MODIFIED, written here | `recordSuccess` with `notModified=true`; **`last_data_at` untouched** |
| Empty result (200, 0 records) | OK with zero counts, written here | `recordSuccess` (0 seen); `last_data_at` untouched |
| `BudgetExceededException` | REFUSED | not a health failure; outcome `success=false`, error "budget exceeded" |
| `InBackoffException` | none | skip, no health change |
| `IngestBackpressureException` | FAILED, error "ingest queue full" | logged warning; **not** counted as an upstream failure |
| Any other exception (incl. timeout, `UpstreamFormatException`) | FAILED with redacted error (≤ 2,000 chars) and `http_status` for `UpstreamHttpException` | `recordFailure` |
| SCHEDULED run whose source was disabled while in flight | DISCARDED with `records_seen = n` | nothing ingested |

   Rows written here use a `FetchLogWriter` (JDBC insert, `ingested_at` null). `fetch_log` is not covered by I1, so this does not conflict with the single writer.
6. Return `FetchOutcome`: `sourceId, startedAt, durationMs, success, recordsSeen, recordsChanged (= persisted), persisted, live, touched, touchesThrottled, late, skipped, rejected, byReason, fromSeq, toSeq, error`.

Also: a per-source `AtomicBoolean running` prevents overlap; a scheduled run that would overlap is skipped and counted (`foresignal.fetch.skipped{reason=overlap}`). Timer `foresignal.fetch.duration{source,result}`.

### 3. `CollectorScheduler`

- One `ScheduledExecutorService` with a single platform thread drives timing; fetches run on virtual threads.
- **Only POLLING sources are scheduled.** PUSH sources receive data through prompt 024's inbound endpoint; DERIVED sources are run by prompt 052's runtime. Neither is polled nor streamed here.
- `reconcile()` every 2 s, and immediately via `reconcileNow()` on `SourceSettingsChangedEvent` / `SourceCredentialsChangedEvent`:
  - newly enabled: first run immediately, then fixed delay = interval (measured from the end of the previous run)
  - interval changed: reschedule
  - disabled: cancel the future; in-flight runs finish and are DISCARDED per the table above
- Skip a run while the source is in backoff, logging once per backoff window.
- **Startup order** (`StartupOrder`): phase-1 schema (before the EntityManagerFactory) → `PartitionManager` schedule → `IngestWriter` (lock, `resetState`, clock guard) at `WRITER` → TriggerEngine at `TRIGGER_ENGINE` (prompt 017) → this scheduler and the streaming supervisor at `SCHEDULER`. On shutdown: stop scheduling, wait up to 30 s for in-flight fetches and flush streaming sinks, then let the writer drain.
- `SchedulerState state()`: running, scheduled sources, active fetches, active streams (for `/v1/system`, prompt 019).
- Class Javadoc: the only things that cause an upstream request are an enabled source, a manual fetch, or a probe.

### 4. `ManualFetchService` and `ProbeService`

- **`fetchNow(sourceId)`:** 404 unknown source; 409 `CREDENTIALS_REQUIRED` (missing required credentials); 409 `ALREADY_RUNNING`; 409 `IN_BACKOFF`; 409 `NOT_SUPPORTED_FOR_MODE` for streaming, PUSH and DERIVED sources (use probe for streams); executes with `MANUAL` even when the source is disabled; a budget refusal maps to `BudgetExceededProblem` (429).
- **`probe(sourceId, maxBytes, seconds)`:** never ingests, never writes `fetch_log`, never updates health; consumes budget normally.
  - Polling: context with a `RequestRecorder(maxBytes)`, `adapter.fetch` with the same timeout, returning `ProbeResult` with the recorded exchanges (credentials redacted) and up to 100 `NormalizedRecordPreview`s (GeoJSON geometry via JTS `GeoJsonWriter`, plus `eventTime`, `validFrom`, `quality` and `observers`). A parse exception fills `parseError` and still returns the exchanges.
  - Streaming: start the stream with a capturing sink for `seconds`, then close it; up to 100 records.

### 5. `StreamingSupervisor` and `PolicyAwareRecordSink`

- Reconcile starts and stops STREAMING sources. `start(source)` calls `adapter.start(streamContext, sink)` on a virtual thread. `StreamContext` adds `reportBytes(n)` (UsageMeter) and `connected()` / `disconnected(Throwable)` callbacks that update health. Add `StreamHandle.onClose(Consumer<Throwable>)` and `CredentialsRejectedException` to the SPI if missing.
- Reconnect ladder 5 s, 15 s, 60 s, 300 s, then 900 s repeating, reset after 10 minutes connected (configurable for tests). Each attempt consumes budget; a budget refusal waits until the next minute, never spinning; `CredentialsRejectedException` retries hourly.
- **`PolicyAwareRecordSink`** (Core-Data-Model §7) **replaces the pre-v1.2 `CoalescingRecordSink` design** (do not create that class), one per streaming source:
  - `receivedAt` is stamped on `accept`.
  - **One outstanding batch** per source: flush at 2 s or 2,000 entries, but only once the previous batch's future has completed; keep buffering meanwhile.
  - SAMPLED / LATEST: a map keyed by `externalId`; the greatest `observedAt` wins (`receivedAt` under `observedAtMode=RECEIVED`), ties go to the later arrival.
  - ALL: an ordered list capped at `allBufferMax` (50,000). At the cap `accept` blocks for up to 30 s, then drops the record and counts `foresignal.stream.records.dropped{source}`.
  - Each flush produces one `fetch_log` row (`trigger=STREAM`, written by the writer) and calls `HealthTracker.recordStreamFlush(outcome)`, which updates `last_data_at`. `stop(source)` closes the handle and flushes.
  - Javadoc the documented limit: under SAMPLED and LATEST, trigger edges and samples have *flush resolution* for streams (≥ 2 s, longer while a batch is in flight) and *poll resolution* for polling sources.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `FetchOutcome` gains `persisted`, `live`, `touched`, `touchesThrottled`, `late`, `skipped`, `rejected`, `byReason`; `NormalizedRecordPreview` gains `eventTime`, `validFrom`, `quality` (`$ref Quality`), `observers` (array of `Observer`); add `Quality`, `Observer` and `Coverage` schemas per Core-Data-Model §3. `fetchSourceNow` 409 description adds "not supported for this source mode". The operations already carry `x-foresignal-scope: admin`; no new operations. Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`).
- `docs/Architecture.md` is folded later; record the sink change in the Completion Report.

## Tests to write (do not run)

Use `FakePollingAdapter`, `FakeStreamingAdapter`, `MutableClock` and Awaitility.

- `FetchExecutorIT`: success path ingests and updates health; a `fetch_log` row per status (OK via the writer with counts and seqs, NOT_MODIFIED, FAILED with redacted error and HTTP status, REFUSED, DISCARDED); `last_data_at` set on OK with records and **not** touched by a 304 or an empty result; timeout interrupts; budget refusal is not a failure; overlap skipped; backpressure is not a health failure.
- `CollectorSchedulerIT`: enabling via `SourceService.update` triggers a fetch within ≤ 3 s; disabling stops further fetches and discards an in-flight result; an interval change reschedules; backoff skips runs; PUSH and DERIVED sources are never fetched; restart resumes enabled sources.
- `ManualFetchServiceIT`: every 404/409/429 path; works while disabled.
- `ProbeServiceIT`: raw body captured (WireMock) and truncated at `maxBytes`; credentials redacted; previews carry `eventTime`/`quality`/`observers`; nothing written to `record_version` or `fetch_log`; health unchanged.
- `PolicyAwareRecordSinkTest`: ALL preserves arrival order; blocking at the cap and drop after the timeout (short test timeout); SAMPLED latest-wins by `observedAt` with ties to the later arrival; no second batch while one is outstanding; size- and time-triggered flushes; flush on stop.
- `StreamingSupervisorIT`: flushes reach the writer with `trigger=STREAM` rows; reconnect ladder (short test ladder); credentials-rejected hourly path; stop flushes.

## Acceptance criteria

- Only an enabled POLLING source, a manual fetch or a probe causes an upstream request (Javadoc plus test).
- Every scheduled or manual fetch leaves exactly one `fetch_log` row, except in-backoff skips.
- Streaming sources flush only through `PolicyAwareRecordSink`; no latest-wins coalescing is applied to ALL sources.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 138, 141, 142, 143).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 143).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `010: Collector Scheduler`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/010-CR-Collector-Scheduler.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `010: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
