# 010-CR-Collector-Scheduler

| | |
|---|---|
| Prompt | 010-Collector-Scheduler.md |
| Status | Complete |
| Implementation commit | 4c451ae5ba487be77fa4b4b1a8bc8b5bb919989e |
| Date | 2026-10-11 |

## Summary

This prompt connects subscription to collection: the scheduler, the fetch path with its `fetch_log`
rows, the streaming supervisor with the policy-aware sink, and the operator's fetch-now and probe
services (their HTTP endpoints come in 013). All new classes are in `ai.foresignal.collect`.

**`DefaultFetchContext` / `DefaultStreamContext` / `FetchContextFactory`:**
- one context per fetch
- `http()` is a fresh `UpstreamClient.forSource(…)` binding, created on first use, with a
  `RequestRecorder` when probing; `traces()` returns that binding's `FetchTrace`s
- params (`SourceService.effectiveParams`), credentials (`CredentialsAccess`) and `lastSuccessAt`
  (`source_health.last_success_at`) are read lazily, at most once
- `state()` is `PersistentAdapterStateStore`; `log()` is the logger `foresignal.source.<id>` (a
  default method on the SPI)
- a DERIVED source's `http()` throws `UnsupportedOperationException`
- the stream context sends received bytes to the new `UsageMeter.recordBytes` (bytes without a
  request) and forwards `connected()` / `disconnected(Throwable)` to the supervisor

**`FetchExecutor.execute(sourceId, SCHEDULED | MANUAL)`:**
- a per-source `AtomicBoolean` overlap guard; an overlapping run is skipped and counted as
  `foresignal.fetch.skipped{source,reason=overlap}`
- a source in backoff is skipped with no row and no health change (`reason=backoff`)
- `recordAttempt`, then `adapter.fetch` through the `FetchDispatcher` seam
  (`VirtualThreadFetchDispatcher`: one virtual thread per fetch) with the descriptor's
  `fetchTimeout`; on timeout the task is interrupted and the fetch fails with
  `AdapterTimeoutException`
- `receivedAt` is stamped when `fetch` returns; each record becomes
  `IngestRecord(record, receivedAt, arrival)` with a per-source arrival counter
- the `FetchLogEntry` is built from the fetch's own traces: `request_count` is the number of
  traces, `bytes` is their sum, URL/status/ETag/body hash come from the last trace;
  `adapter_version` is `ForesignalVersion`, `licence` comes from the descriptor, and `coverage`
  from the `FetchResult`
- the outcome table is implemented as written:
  - OK with records: written by the writer; submit timeout = remaining fetch time + 30 s; awaited;
    `recordSuccess(outcome)`
  - NOT_MODIFIED and empty OK: written here; `last_data_at` untouched
  - REFUSED: written here; not a health failure
  - ingest queue full or writer failure: FAILED, a warning, not an upstream failure
  - any other failure: FAILED with the redacted error (at most 2,000 characters, through
    `HealthTracker.errorMessage`) and the `UpstreamHttpException` status; `recordFailure`
  - SCHEDULED run whose source was disabled in flight: DISCARDED with `records_seen = n`
- `FetchLogWriter` inserts the rows written here with JDBC, `ingested_at` null
- timer `foresignal.fetch.duration{source,result}`

**`CollectorScheduler`:**
- one daemon platform thread (`foresignal-collector`) drives all timing; fetches run on virtual
  threads
- `reconcile()` runs every 2 s on the timer thread and immediately via `reconcileNow()` on
  `SourceSettingsChangedEvent` (when `affectsScheduling()`), `SourceCredentialsChangedEvent` and
  `SourceRegistryChangedEvent`
- only enabled POLLING sources are scheduled: the first run is immediate, then a fixed delay
  measured from the end of the previous run; an interval change reschedules; disabling cancels
  the future
- enabled STREAMING sources go to `StreamingSupervisor.reconcile`; PUSH and DERIVED are ignored
- a run while in backoff is logged once per backoff window
- starts on `ApplicationReadyEvent` at `StartupOrder.SCHEDULER` when
  `foresignal.collector.scheduled=true`
- as a `SmartLifecycle` it stops before singletons are destroyed: it stops scheduling, waits up
  to 30 s for in-flight fetches and flushes the streaming sinks, and only then does the writer's
  `destroy()` drain the queue
- `state()` returns `SchedulerState(running, scheduledSources, activeFetches, activeStreams)`
- the class Javadoc states that only an enabled source, a manual fetch or a probe causes an
  upstream request

**`ManualFetchService.fetchNow` / `ProbeService.probe`:**
- `fetchNow`:
  - 404 for an unknown source
  - 409 `NOT_SUPPORTED_FOR_MODE` for streaming, PUSH and DERIVED sources
  - 409 `CREDENTIALS_REQUIRED`, `ALREADY_RUNNING` and `IN_BACKOFF`, each checked before the fetch
    and again on the outcome, to cover races
  - 429 `BudgetExceededProblem` on a REFUSED outcome
  - runs while the source is disabled
- `probe`:
  - never ingests, writes `fetch_log` or calls `HealthTracker`, and consumes budget
  - polling: `RequestRecorder(maxBytes)`, the same timeout, and up to 100
    `NormalizedRecordPreview`s with GeoJSON geometry, `eventTime`, `validFrom`, `quality` and
    `observers`
  - a failure fills `parseError` and still returns the exchanges
  - streaming: the stream is started with a capturing sink for `seconds`, then closed
  - argument ranges are taken from the spec (`maxBytes` 1024–10485760, default 262144;
    `seconds` 1–60, default 10)

**`StreamingSupervisor` and `PolicyAwareRecordSink`:**
- **Supervisor:**
  - one virtual thread per stream: `adapter.start(streamContext, sink)`, then a wait for the close
    signal (`StreamHandle.onClose`, `ctx.disconnected`, or `isRunning()` polled each second)
  - reconnect ladder 5/15/60/300/900 s, with the last step repeating; it resets after 10 minutes
    connected
  - a budget refusal waits until the next minute, backoff waits out the backoff, and
    `CredentialsRejectedException` retries hourly; all of these are configurable
  - connection changes go to `HealthTracker`, completed flushes to `recordStreamFlush`, and a flush
    the writer failed gets a FAILED STREAM row
- **Sink:**
  - `receivedAt` and the arrival number are stamped on `accept`
  - one outstanding batch: a flush is due at 2 s or 2,000 entries, but only after the previous
    future completed
  - SAMPLED/LATEST use a map with latest-wins by `observedAt` (by `receivedAt` under RECEIVED);
    ties go to the later arrival
  - ALL uses an ordered list capped at 50,000; at the cap `accept` blocks up to 30 s, then drops
    and counts `foresignal.stream.records.dropped{source}`
  - each flush is one writer-written `fetch_log` row (`trigger=STREAM`)
  - `flushAndClose` flushes on stop
  - the flush-resolution limit is in the Javadoc
- No `CoalescingRecordSink` exists. **Architecture §5.5's "coalesces by externalId (latest
  wins)" is superseded by this sink (Core-Data-Model §7); the planner should fold that in.**

**SPI additions:**
- `FetchContext.log()`
- `StreamContext.reportBytes`, `connected()`, `disconnected(Throwable)`
- `StreamHandle.onClose(Consumer<Throwable>)`, a default no-op
- `CredentialsRejectedException`

**Configuration:**
- `foresignal.collector.*` (`CollectorProperties`) in `application.yml`
- `scheduled: false` in `application-test.yml`

## Files created / modified

**Created (main)**
- `collect/`: `AdapterTimeoutException`, `CollectorProperties`, `CollectorScheduler`,
  `DefaultFetchContext`, `DefaultStreamContext`, `FetchContextFactory`, `FetchDispatcher`,
  `VirtualThreadFetchDispatcher`, `FetchExecutor`, `FetchLogWriter`, `FetchOutcome`,
  `ManualFetchService`, `ProbeService`, `ProbeResult`, `NormalizedRecordPreview`,
  `PolicyAwareRecordSink`, `StreamingSupervisor`, `SchedulerState`
- `source/spi/CredentialsRejectedException`
- `source/formats/geojson/GeoJsonWriter`

**Modified (main)**
- `source/spi/FetchContext` (`log()`), `StreamContext` (callbacks, `reportBytes`), `StreamHandle`
  (`onClose`)
- `collect/UsageMeter` (`recordBytes`)
- `collect/RequestRecorder` (`TRUNCATION_MARKER` constant; behavior unchanged)
- `collect/package-info`
- `api/error/ConflictException` (`NOT_SUPPORTED_FOR_MODE`)
- `application.yml`, `application-test.yml`

**Docs**
- `docs/OpenAPI.json`: `probeSource` gains a 409 `NOT_SUPPORTED_FOR_MODE` response and description
  sentence, plus a changelog entry
- `postman/Foresignal.postman_collection.json` regenerated (`806 requests … covers all ops: True`)

**Tests (new)**
- `collect/FetchExecutorIT`, `CollectorSchedulerIT`, `NoClientTriggeredFetchIT`,
  `ManualFetchServiceIT`, `ProbeServiceIT`, `StreamingSupervisorIT`, `PolicyAwareRecordSinkTest`,
  `FetchDispatcherTest`, `DefaultFetchContextTest`, `CollectorPropertiesTest`
- `source/formats/geojson/GeoJsonWriterTest`
- `testsupport/collect/ControllablePollingAdapter`, `CollectorTestSources`

**Tests (modified)**
- `testsupport/adapters/FakeStreamingAdapter`: `Handle.onClose`, `Handle.drop(Throwable)` and
  `lastHandle()`

No dependency changes. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` changes.

## Deviations from the prompt

1. **Task 6 was mostly already done.** `docs/OpenAPI.json` already had every `FetchOutcome` and
   `NormalizedRecordPreview` field, the `Quality`, `Observer` and `Coverage` schemas, and the
   `fetchSourceNow` 409 text, so I verified these rather than adding them. The only spec change is
   a **409 `NOT_SUPPORTED_FOR_MODE` on `probeSource`**, for PUSH and DERIVED sources, which have no
   upstream; the spec had no response for that case. It is recorded in the changelog. The
   operations stay `planned`, because their endpoints belong to 013.
2. **No JTS `GeoJsonWriter`.** It lives in `jts-io-common`, which the build removed on purpose
   (Conventions, "GeoJSON reading"). I added Foresignal's own
   `source.formats.geojson.GeoJsonWriter` as the counterpart of `GeoJsonReader`.
3. **`FetchOutcome` has three components the prompt did not list:**
   - `fetchId`, which the spec already defines
   - `status`, the `fetch_log` status (Java only)
   - `skipReason`, `overlap` / `backoff` (Java only)

   013 maps the API fields. For writer-written OK rows, `fetchId` is looked up by
   `(source_id, started_at)`, because `IngestOutcome` carries no batch id. `startedAt` is truncated
   to microseconds so the lookup matches.
4. **The backoff check comes before `recordAttempt`.** A run skipped for backoff leaves health
   untouched, as the table requires. An adapter that hits `InBackoffException` mid-fetch has
   already recorded its attempt, but nothing else changes.
5. **The ingest await can time out.** If the writer has not finished within remaining time + 30
   s, the outcome is `success=false` with no row written here. The batch is still queued, and the
   writer will write the row, so writing one here would make two. A writer that *fails* the batch
   gets a FAILED row ("ingest failed: …"). Like backpressure, this is not an upstream failure.
6. **`StreamContext.reportBytes` is an alias of the existing `recordReceivedBytes`.** 005 had
   already added `recordReceivedBytes`, so `reportBytes` delegates to it. Stream bytes are metered
   with the new `UsageMeter.recordBytes`, which does not count a request.
7. **The scheduler skips a source missing a required credential.** That situation is only
   reachable if the credentials were deleted outside `SourceService`, which itself disables the
   source. This is why credential events trigger `reconcileNow()`.
8. **`foresignal.collector.scheduled` (default true, false in tests)** stops every cached test
   context from fetching enabled fake sources behind other tests. `CollectorSchedulerIT` and
   `NoClientTriggeredFetchIT` turn it on and share one context.
9. **Shutdown uses `SmartLifecycle.stop()`.** Startup stays on the ready event, because the prompt
   asks for that ordering. `SmartLifecycle` guarantees the scheduler and sinks stop before the
   writer's `DisposableBean.destroy()` drains. `isAutoStartup()` is false, so the context refresh
   does not start it early.
10. **The probe's body cap counts characters, not bytes.** That is how `RequestRecorder` (005)
    caps. For ASCII bodies the two are the same. `truncated` is true when the response was
    truncated, the body was capped, or `bytes > maxBytes`.
11. **A probe does call `BackoffRegistry.onSuccess` and `UsageMeter` through `BoundSourceHttp`.**
    That is budget and backoff bookkeeping from 005, not `HealthTracker`. Nothing in
    `source_health` that health reports (attempt, success, error, failures) changes, and the test
    asserts this.
12. **Test sources.** `CollectorTestSources` (`collect-poll`, `collect-keyed`) uses a new
    `ControllablePollingAdapter`, whose steps can block, fail or call WireMock. `FakePollingAdapter`
    can only return canned results. `FakeStreamingAdapter` is used as the prompt asks, extended with
    `onClose` and `drop`.
13. **The supervisor is built inside `StreamingSupervisorIT`.** It uses the context's real
    collaborators but millisecond ladders, so short ladders do not leak into other contexts.
    Backpressure and writer failures in `FetchExecutorIT` use a mocked `IngestQueue`, because
    filling the real 64-slot queue is not deterministic.

## Issues found / open questions

1. **A timed-out adapter that ignores interruption keeps running** on its virtual thread after the
   overlap guard is released, so a later run could overlap it. Interruption is the only lever
   Java offers here.
2. **Architecture §5.5 is out of date** (latest-wins coalescing for every source). The planner
   should fold Core-Data-Model §7 in.
3. **004's note 6 ("Prompt 010 will need …")** uses pre-v1.3 numbering. It is the Sources API,
   which is now 013, so it was not done here.
4. **`fetch_log.licence`, `FetchLogEntry.licence` and the `CollectorTestSources.LICENCE` constant**
   keep the persisted spelling. Prose uses "license".
5. **`BudgetGovernorTest` has "behaviour" in a Javadoc** (pre-existing, from 005). I left it
   alone, since it is out of scope.

No instructions were found in data or tool output.

## Tests written (not run)

As a feature prompt requires, no tests were run.
- `FetchExecutorIT` (22), in nested groups:
  - records: OK row via the writer with counts, seqs, licence, version and coverage; health and
    `last_data_at`; `receivedAt` stamping; row fields from traces (WireMock)
  - no records: NOT_MODIFIED leaves `last_data_at`; empty OK leaves `last_data_at` null
  - failures: HTTP 500 → FAILED with status and redacted error; truncation; timeout interrupts;
    REFUSED is not a failure; a wrapped refusal is still a refusal
  - skips: backoff (no row, no health); backoff mid-fetch; overlap skipped and counted
  - DISCARDED when disabled in flight; manual fetch while disabled
  - ingest trouble: backpressure and writer failure are FAILED but not health failures; the submit
    timeout
  - argument errors
- `CollectorSchedulerIT` (9):
  - enable via `SourceService.update` fetches within 3 s
  - periodic fetching
  - disable stops fetching and discards the in-flight result
  - an interval change reschedules
  - backoff skips runs, which resume after it
  - PUSH and DERIVED are never fetched, and disabled polling sources are untouched
  - a stream starts and stops
  - a restart resumes enabled sources
  - started on ready
- `NoClientTriggeredFetchIT` (1): client GETs and service reads never reach an adapter (SRC-6).
- `ManualFetchServiceIT` (9): 404; NOT_SUPPORTED_FOR_MODE for streaming, PUSH and DERIVED;
  CREDENTIALS_REQUIRED; ALREADY_RUNNING; IN_BACKOFF (before and mid-fetch); 429 with a REFUSED row;
  works while disabled; failure returned rather than thrown.
- `ProbeServiceIT` (11):
  - raw body captured (WireMock) and truncated at `maxBytes`
  - credentials redacted from URL and body
  - previews carry `eventTime`, `validFrom`, `quality`, `observers` and GeoJSON
  - previews capped at 100
  - `parseError` with exchanges
  - 429
  - stream capture, and a stream start failure
  - PUSH 409; range validation
  - nothing written to `record_version`, `record_head` or `fetch_log`; health unchanged
- `PolicyAwareRecordSinkTest` (15):
  - ALL order; block at the cap then drop; unblock on flush
  - SAMPLED latest-wins by `observedAt`, ties to the later arrival; LATEST keyed;
    `observedAtMode=RECEIVED`
  - no second batch while one is outstanding; size- and time-triggered flushes
  - flush on stop and drop after close; close waits for the outstanding batch
  - STREAM provenance; failure to the listener; backpressure retry; unstarted sink
- `StreamingSupervisorIT` (10):
  - flushes reach the writer as STREAM rows and set `last_data_at`
  - stop flushes
  - reconnect ladder, and the ladder reset
  - the credentials-rejected path, and the hourly default
  - budget waits for the next minute; backoff wait
  - reconcile; non-streaming refused
- Unit tests:
  - `FetchDispatcherTest` (3)
  - `DefaultFetchContextTest` (4), including DERIVED `http()` throwing
  - `CollectorPropertiesTest` (4)
  - `GeoJsonWriterTest` (3)

`docs/Invariants.md` already names `CollectorSchedulerIT` and `NoClientTriggeredFetchIT` for
SRC-6, and both now exist.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
```

## Notes for the next prompt

1. **011 (USGS):** a polling adapter needs nothing beyond the SPI.
   - Use `ctx.http().getConditional(...)` and return `FetchResult.unchanged()` on a 304; the
     executor writes NOT_MODIFIED and leaves `last_data_at` alone.
   - Use `ctx.log()` for adapter logging.
   - An adapter test can call `FetchExecutor.execute(id, MANUAL)` and read `fetch_log`, or drive
     `ManualFetchService`.
2. **013 (Sources API):** map these onto the operations:
   - `ManualFetchService.fetchNow` → `fetchSourceNow`, and `ProbeService.probe(id, maxBytes,
     seconds)` → `probeSource`
   - both already throw the right `ForesignalException`s
   - serialize `FetchOutcome` without `status` / `skipReason`, and with `byReason` keyed by the
     `PersistReason` names
   - `ProbeResult.requests` already has the spec's request shape
   - remove both operations from `pending-operations.txt` once that file exists (020)
3. **019 (`/v1/system`):** `CollectorScheduler.state()` gives `SchedulerState`;
   `IngestWriter.status()` gives the writer.
4. **042 (raster products):** replace `VirtualThreadFetchDispatcher` with a `FetchDispatcher` bean
   that routes heavy decodes to a bounded platform pool (`@Primary`, or remove the default).
5. **Streaming adapters (063 onward):**
   - implement `StreamHandle.onClose` so drops are noticed at once; otherwise the supervisor
     polls `isRunning()` every second
   - throw or report `CredentialsRejectedException` for refused keys
   - call `ctx.reportBytes(n)` per batch
6. **ITs that need the scheduler running** must add
   `@TestPropertySource(properties = "foresignal.collector.scheduled=true")`, plus
   `@AutoConfigureMockMvc` and `@Import(CollectorTestSources.class)`, to share the
   `CollectorSchedulerIT` context. Other contexts never fetch on their own.
