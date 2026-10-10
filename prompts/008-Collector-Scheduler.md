# 008 — Collector Scheduler

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/008-CR-Collector-Scheduler.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/007-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §5.1 (CollectorScheduler), §5.5 (StreamingSupervisor), §4.1 (`FetchContext`)
- `docs/Product-Charter.md` FR-4, FR-6, FR-7, G1, NFR-5, NFR-6
- `docs/OpenAPI.json` schemas `FetchOutcome`, `ProbeResult`, `NormalizedRecordPreview`

## Objective

Connect subscription to collection. Enabled sources are fetched on schedule; disabled ones are not, and the change takes effect within seconds. Add the operator's "fetch now" and "probe" capabilities. Run streaming adapters under supervision with reconnect backoff.

## Tasks

### 1. FetchContext implementation (`ai.elaro.collect`)

`DefaultFetchContext` provides:

- `http()`: `UpstreamClient.forSource(…)`, with the recorder when probing
- `params()`: the validated effective params
- `credentials()`: via `CredentialsAccess`
- `clock()`
- `lastSuccessAt()`
- `state()`: an `AdapterStateStore` backed by `source_health.adapter_state`
- `descriptor()`
- `log()`: an SLF4J logger named `elaro.source.<id>`

### 2. FetchExecutor

`FetchOutcome execute(String sourceId, FetchTrigger trigger /* SCHEDULED | MANUAL */)`:

1. `HealthTracker.recordAttempt`.
2. Run `adapter.fetch(ctx)` on a **virtual thread** with the descriptor's `fetchTimeout`. On timeout, interrupt the thread and record a failure (`AdapterTimeoutException`).
3. Handle the result:

| Result | Action |
|---|---|
| `notModified` | `recordSuccess(0, 0, notModified=true)` |
| Records returned | Submit to `IngestQueue` (timeout = remaining fetch time + 30 s), await `IngestOutcome`, then `recordSuccess(seen, changed, false)` |
| `BudgetExceededException` | Record refused; outcome success=false, error "budget exceeded". Not a health failure. |
| `InBackoffException` | Skip; no health change |
| Any other exception | `recordFailure` |

4. For `SCHEDULED` runs, if the source was disabled while the fetch was in flight, discard the records (do not ingest).
5. Return a `FetchOutcome` matching the OpenAPI schema.

Also:

- Per-source `AtomicBoolean running` guards against overlap. A scheduled run that would overlap is skipped and counted (`elaro.fetch.skipped{reason=overlap}`).
- Metrics: timer `elaro.fetch.duration{source,result}`.

### 3. CollectorScheduler

- **Timing:** one `ScheduledExecutorService` with a single platform thread drives timing; actual fetches are dispatched to virtual threads.
- **Reconcile:** `reconcile()` runs every 2 s, and immediately on `SourceSettingsChangedEvent` / `SourceCredentialsChangedEvent` via `reconcileNow()`. It compares the enabled polling sources with the scheduled set:
  - **newly enabled:** schedule the first run immediately, then fixed delay = interval (measured from the end of the previous run)
  - **interval changed:** reschedule
  - **disabled:** cancel the scheduled future. In-flight runs finish; see the discard rule above.
- Skip a run while the source is in backoff (`BackoffRegistry`), and log once per backoff window.
- **Lifecycle:** start on `ApplicationReadyEvent` after `IngestWriter` and `SchemaBootstrap`. On shutdown, stop scheduling, wait up to 30 s for in-flight fetches, then let the writer drain.
- `SchedulerState state()`: running, scheduled sources, active fetches. Used by `/v1/system` later.

### 4. Manual fetch and probe services

- **`ManualFetchService.fetchNow(sourceId)`:**
  - 404 for an unknown source
  - 409 `CREDENTIALS_REQUIRED` if required credentials are missing
  - 409 `ALREADY_RUNNING` if running
  - 409 `IN_BACKOFF` if in backoff
  - executes with `MANUAL` (allowed even when the source is disabled)
  - maps a budget refusal to `BudgetExceededProblem` (429)
  - streaming sources return 409 `NOT_SUPPORTED_FOR_STREAMING` (use probe instead)
- **`ProbeService.probe(sourceId, maxBytes, seconds)`:**
  - **polling:** builds a context with a `RequestRecorder(maxBytes)`, calls `adapter.fetch` directly (same timeout) and **never** submits to ingest. Returns `ProbeResult` with the recorded exchanges and the parsed records as `NormalizedRecordPreview` (geometry as GeoJSON via a JTS `GeoJsonWriter`), capped at 100 records. A parse exception populates `parseError` while still returning the exchanges.
  - **streaming:** starts the stream with a capturing sink for `seconds`, then closes it. Exchanges hold the connect info. Records are capped at 100.
  - Budget is consumed normally. Health is **not** updated by probes.

### 5. StreamingSupervisor

- **Starting and stopping:** reconcile starts and stops streaming sources (same reconcile loop).
- **`start(source)`:**
  - calls `adapter.start(streamContext, sink)` on a virtual thread
  - `StreamContext` extends the fetch context with `reportBytes(n)` (UsageMeter) and `connected()` / `disconnected(Throwable)` callbacks that update health
- **Reconnect:** when the handle signals termination (`StreamHandle.onClose(Consumer<Throwable>)`; adapt the SPI if needed and record the change), wait and restart.
  - Reconnect ladder: 5 s, 15 s, 60 s, 300 s, then 900 s repeating. Reset after 10 minutes of a healthy connection.
  - Each attempt consumes budget through `openWebSocket` or an explicit `ctx.http()` call.
  - Budget refused: wait until the next minute and retry, never spinning.
  - Authentication rejected (the adapter throws `CredentialsRejectedException`; add it to the SPI): retry only hourly.
- **`CoalescingRecordSink`:** a map keyed by `externalId` (latest wins), flushed every 2 s or at 2,000 entries to `IngestQueue`, reporting each flush to health via `recordStreamFlush`. Flush on stop.
- **`stop(source)`:** close the handle and flush.

## Tests to write (do not run)

Use `FakePollingAdapter`, `FakeStreamingAdapter`, `MutableClock` where practical, and Awaitility.

- `FetchExecutorIT`: success path ingests and updates health; `notModified`; timeout interrupts; budget refusal is not a failure; disabled-in-flight results are discarded; overlap skipped.
- `CollectorSchedulerIT`:
  - enabling via `SourceService.update` triggers a fetch within ≤ 3 s
  - disabling stops further fetches
  - an interval change reschedules
  - backoff skips runs
  - restart resumes enabled sources (simulate via a fresh scheduler bean)
- `ManualFetchServiceIT`: every 409/429/404 path; works while disabled.
- `ProbeServiceIT`: the raw body is captured (WireMock) and truncated at `maxBytes`; credentials are redacted; nothing is written to `record_version`; health is unchanged.
- `StreamingSupervisorIT`: records are coalesced and flushed; reconnect after a failure uses the ladder (use a short test ladder via config); stop flushes; credentials-rejected leads to the hourly retry path.
- `CoalescingRecordSinkTest`: latest wins, size-triggered and time-triggered flush.

## Acceptance criteria

- The only thing that causes an upstream request is an enabled source, a manual fetch, or a probe. This is documented in the `CollectorScheduler` Javadoc.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 040.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `008: Collector Scheduler`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/008-CR-Collector-Scheduler.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `008: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
