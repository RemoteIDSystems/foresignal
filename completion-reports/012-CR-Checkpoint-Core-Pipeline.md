# 012-CR-Checkpoint-Core-Pipeline

| | |
|---|---|
| Prompt | 012-Checkpoint-Core-Pipeline.md |
| Status | Complete |
| Implementation commit | f3cb62ab32647be4c4780dab049ce5a620d00634 |
| Date | 2026-10-11 |

## Summary

This was the first run of every test written in prompts 001–011. The suite is now green:
`./gradlew test` passes 1577 tests with 0 failures and 0 skipped, and it passed three times in a row
(runs 3–5, the last two with `--rerun`).

Fixing it took nine production fixes and four test-infrastructure or fixture fixes. Two tests were
added where a stated invariant had no assertion: `LicenseAllowlistTest` (PRC-2), and the
EntityManagerFactory ordering check in `SchemaBootstrapIT`.

## Files created / modified

Production:
- `src/main/java/ai/foresignal/collect/HealthTracker.java`: the ring query now binds `sourceId`
- `src/main/java/ai/foresignal/collect/UsageMeter.java`: increments run inside `ConcurrentHashMap.compute`; series buckets use `date_trunc(?, minute, 'UTC')`
- `src/main/java/ai/foresignal/api/error/GlobalExceptionHandler.java`: Spring's null-body problems are filled in before shaping
- `src/main/java/ai/foresignal/settings/AppSettingService.java`: `invalidateCache()` announces every key
- `src/main/java/ai/foresignal/settings/SettingKeys.java`: adds `ALL`
- `src/main/java/ai/foresignal/collect/CollectorScheduler.java`: `isAutoStartup()` returns true once the scheduler has started
- `src/main/java/ai/foresignal/store/TriggerRuleEntity.java`: `mode` uses `length = 8` plus `@ColumnDefault("'match'")`
- `build.gradle.kts`: the test task passes the runtime classpath as `-Dforesignal.test.runtime-classpath`

Tests and test support:
- `src/test/java/ai/foresignal/test/PostgisContainers.java`: the shared container ignores `stop()`; starts with `max_connections=500` and `max_locks_per_transaction=256`
- `src/test/java/ai/foresignal/test/WireMockSupport.java`: `gzipDisabled(true)`
- `src/test/java/ai/foresignal/collect/WebSocketBudgetIT.java`: corrected the RFC 6455 GUID
- `src/test/java/ai/foresignal/LicenseAllowlistTest.java`: new (PRC-2)
- `src/test/java/ai/foresignal/config/SchemaBootstrapIT.java`: new EMF `dependsOn` assertion
- `src/test/java/ai/foresignal/settings/AppSettingServiceIT.java`: new assertion that `invalidateCache` announces every key

Docs:
- `docs/Invariants.md`: PRC-2's test is now named `LicenseAllowlistTest` (US English). The file
  requires this update when a prompt names a test differently.

## Deviations from the prompt

1. **The PRC-2 test is named `LicenseAllowlistTest`, not `LicenceAllowlistTest`,** because the
   project uses US English. `docs/Invariants.md` was updated in the same commit, as that file
   requires.
2. **Run 1 was aborted** after about 12 minutes, so it has no final count. A connection-exhaustion
   cascade had made every later database test wait for a 30-second Hikari timeout (see Test results).
3. **Test infrastructure changed, not only production code.** The shared container, the WireMock
   gzip setting and the WebSocket fixture were wrong in the test harness. None of these changes
   alters what any test asserts. Each one is listed under Test results.

## Issues found / open questions

1. **Core-Data-Model §6 and the writer disagree on `touched`.** §6 defines `touched` as "touch rows
   written", and this prompt's scrutiny list says "touch counts equal row counts".
   `IngestWriter` sets `IngestOutcome.touched` and `fetch_log.touches` to the number of TOUCH
   **observations**. It writes `touch()` rows only for touch-only entities, one per key. The two
   numbers differ when one entity is touched twice in a batch, or when a touch is folded into a live
   upsert. Prompt 009 made this choice on purpose (009 Deviation 8) and amended the OpenAPI
   descriptions. I did not change it. **Owner:** decide whether the code or §6 should change.
2. **`geolatte-geom` 1.11 (pulled in by `hibernate-spatial`) is LGPL-3.0.** PRC-2 forbids GPL and
   AGPL, and the new test does not reject LGPL. Logback (EPL-2.0 / LGPL-2.1 dual) and
   `jakarta.annotation`/`jakarta.transaction` (EPL-2.0 / GPL-2.0 with Classpath Exception) also pass.
   **Owner:** say whether LGPL is acceptable.
3. **The ingest writer logs an expected error during `IngestWriterIT.notNullFailure`.** It reads
   `null value in column "trigger"`; that test exercises the failure path on purpose.
4. Spring Framework 7's test context cache **pauses** inactive contexts by default
   (`spring.test.context.cache.pause`). This is what exposed the scheduler restart defect. Future
   `SmartLifecycle` beans that start on the ready event need the same `isAutoStartup()` treatment.

No instructions directed at an agent were found in data, fixtures or tool output.

## Test results

All runs used `./gradlew test --continue`, with Docker 29.7.2 and Testcontainers-managed
`postgis/postgis:17-3.5`.

| Run | Tests | Failed | Skipped | Notes |
|---|---|---|---|---|
| 1 | n/a (aborted) | 63 reported before abort | n/a | PostgreSQL ran out of connections, the shared container stopped, and everything after it failed with connection refused |
| 2 | 1572 | 34 | 0 | after the container fixes and the `trigger_rule.mode` fix |
| 3 | 1576 | 0 | 0 | after all fixes, plus 4 new tests |
| 4 | 1577 | 0 | 0 | `--rerun`, plus the `SchemaBootstrapIT` ordering test |
| 5 | 1577 | 0 | 0 | `--rerun`; flakiness check, identical to run 4 |

Fixes, grouped by root cause:

**A. Test-context sharing and the database (run 1, about 50 failures and the abort).**
- *Cause:* each of the 11 cached contexts holds a 10-connection Hikari pool. On the eleventh
  context (`UsgsIngestIT`), PostgreSQL refused connections ("sorry, too many clients already",
  `max_connections=100`). The context failed to load, and Spring closed it. `@ImportTestcontainers`
  registers the static container as an `AutoCloseable` bean, so closing that context **stopped the
  shared container**. Every later test then failed with `Connection to localhost:32779 refused`.
- *Change:* `PostgisContainers` now uses a subclass whose `stop()` is a no-op. Ryuk removes the
  container when the JVM exits. The server starts with `max_connections=500` and
  `max_locks_per_transaction=256` (the latter as in `docker-compose.yml`).

**B. Hibernate DDL error on every context start** (`TriggerRuleEntity`, production).
- *Cause:* `columnDefinition = "varchar(8) default 'match'"` made schema update emit
  `alter column mode set data type varchar(8) default 'match'`, which PostgreSQL rejects. It was
  logged 18 times in run 1.
- *Change:* `length = 8` with `@ColumnDefault("'match'")`. Run 3 logged no DDL errors.

**C. Health was never recorded** (`HealthTracker.lockRings`, production; 13 failures in
`HealthTrackerIT`, `HealthServiceIT`, `FetchExecutorIT`, `StreamingSupervisorIT` and `UsgsIngestIT`).
- *Cause:* `select … where source_id = ? for update` was run without its bind value, so every
  `recordSuccess` failed with BadSqlGrammar. The failure was logged and swallowed, so
  `last_success_at` and `last_data_at` were never set.
- *Change:* pass `sourceId`.

**D. Lost usage increments** (`UsageMeter`, production; `UsageMeterIT` concurrency, 3999 instead of 4000).
- *Cause:* a recorder could get a `Counters` from `computeIfAbsent`, a flush could then `remove` and
  sum it, and the recorder's increment landed afterwards in an entry nobody would read.
- *Change:* every increment runs inside `live.compute(...)`. That shares the bin lock with `remove`,
  so an increment is always either in the drained snapshot or in a new entry.

**E. Usage day buckets in the session time zone** (`UsageMeter.series`, production; `UsageMeterIT`
day resolution, `06:00Z` instead of `00:00Z`).
- *Cause:* `date_trunc('day', timestamptz)` uses the JDBC session's time zone, which is the JVM's
  (America/Chicago on this host).
- *Change:* `date_trunc(?, minute, 'UTC')`. The other `date_trunc` in the code (`seq_clock`,
  `'minute'`) does not depend on the time zone.

**F. Spring MVC errors without `code`/`type`** (`GlobalExceptionHandler`, production; 2 failures in
`GlobalExceptionHandlerTest`, 405 and 415).
- *Cause:* Spring 7.0.9's built-in handlers call `handleExceptionInternal` with a null body; I
  checked this with `javap`. The override only shaped a non-null `ProblemDetail`.
- *Change:* fill the body from `ErrorResponse.updateAndGetBody(...)` before shaping it.

**G. Stale CORS (and token-filter) setting after cache invalidation** (`AppSettingService`,
production; 2 failures in `CorsFilterIT`).
- *Cause:* `CorsFilter` and `TokenFilter` keep their own copy of a setting and drop it only on
  `AppSettingChangedEvent`. `invalidateCache()`, which configuration import is documented to use,
  cleared only the service cache.
- *Change:* `invalidateCache()` publishes the event for every key in the new `SettingKeys.ALL`. Test
  added: `AppSettingServiceIT.invalidateCacheAnnouncesEveryKey`.

**H. Scheduler not restarted after a context pause** (`CollectorScheduler`, production;
`NoClientTriggeredFetchIT`, `isRunning()` false).
- *Cause:* Spring 7 pauses a cached context on every context switch, which stops lifecycle beans.
  `restart()` restarts only beans that are auto-startup, and `isAutoStartup()` was a constant
  `false`.
- *Change:* `isAutoStartup()` returns true once `start()` has run. It is still false during the
  initial refresh, so the `ApplicationReadyEvent` start order is unchanged.

**I. WireMock rewrote ETags** (test infrastructure; 3 failures in `UpstreamClientIT` and
`FetchExecutorIT`, `"v1--gzip"` instead of `"v1"`).
- *Cause:* Jetty's gzip handler compresses any response to a request that accepts gzip, and
  `UpstreamClient` always sends `Accept-Encoding: gzip`. Jetty then appends `--gzip` to the ETag.
  Production stores validators verbatim, which is correct.
- *Change:* `WireMockSupport` uses `gzipDisabled(true)`. The gzip-decoding test stubs its own
  `Content-Encoding: gzip` body and still passes.

**J. Wrong WebSocket fixture** (test change; 8 failures in `WebSocketBudgetIT`, `Bad Sec-WebSocket-Accept`).
- *Cause:* the handshake server's GUID was `258EAFA5-E914-47DA-95CA-5AB0DC85B11F`. RFC 6455 §4.2.2
  specifies `258EAFA5-E914-47DA-95CA-C5AB0DC85B11`.
- *Change:* corrected the GUID. The test now computes the handshake as RFC 6455 §4.2.2 specifies.
  No assertion changed.

No test assertion was changed.

### Scrutiny

I checked every area in the prompt's list. Where tests already assert it, they pass:
- **Schema parity and partitions:**
  - `SchemaParityIT.secondSchemaUpdateLeavesRecordVersionAlone`
  - `PartitionManagerIT`: 1.5-block headroom, dropped-low-block recreate, drop refused inside a
    transaction, detach concurrently, FINALIZE of pending detaches
  - `DatabaseCleanerIT`: leaves not truncated individually, `IngestStateReset` invoked
  - Initializer before the EntityManagerFactory **had no assertion** (only log inspection in 007),
    so `SchemaBootstrapIT.entityManagerFactoryWaitsForTheInitializers` was added.
- **Writer I1–I10:** `IngestWriterIT`:
  - exclusive lock
  - `seqMonotonicUnderConcurrency`
  - `notNullFailure` (next seq above the lost one)
  - `clockBehind`
  - `lateHasNoSideEffects`
  - `repairsMissingLive` plus `assertHeadsAndLiveRowsPair` after each test
  - `NoJpaWritesToRecordVersionTest` (I9)
- **Policy matrix:** `IngestPolicyEngineTest` covers the 12 rows, each with a firing, boundary and
  near-miss case. `IngestWriterIT` covers `heartbeat`, `retentionKeyframe` and `carryForward`.
- **Touch and heartbeat:** `touchAndThrottle` and `heartbeat`. The touch-count semantics differ from
  §6 (Issue 1).
- **LATE and clock guard:**
  - equal `observed_at` is not LATE (row-1 boundary)
  - stable arrival order (`NormalizerTest`)
  - `clamp`
  - `IngestClockGuardTest` (behind, clamp, jump)
- **fetch_log:** `FetchExecutorIT` covers OK, NOT_MODIFIED, REFUSED and DISCARDED, plus
  `last_data_at` across 304 and empty results. `StreamingSupervisorIT` covers STREAM rows.
  `seqClockOnEmptyBatch` covers `seq_clock` on a zero-version batch.
- **Scheduler timing:**
  - `CollectorSchedulerIT`: enable within 3 s, disable discards, PUSH/DERIVED never polled
  - `FetchExecutorIT`: overlap skipped
  - `PolicyAwareRecordSinkTest`: one outstanding batch, latest-wins, block then drop
- **USGS end to end:** all five `UsgsIngestIT` scenarios pass.
- **Flakiness:** the full suite, including every IT, passed three consecutive times (runs 3–5). No
  sleeps were added.

### Invariants check (owner ≤ 011)

Each of these has at least one existing, passing test:

| Invariant | Tests |
|---|---|
| ING-1, ING-2, ING-5 | `IngestWriterIT` |
| ING-3 | `IngestClockGuardTest`, `IngestWriterIT` |
| ING-4 | `TimeContractTest` |
| ING-6 | `NoJpaWritesToRecordVersionTest` |
| ING-7 | `ContentHasherTest` |
| ING-8 | `IngestPolicyEngineTest`, `IngestPolicyValidatorTest` |
| HIST-1 | `PartitionManagerIT` |
| SRC-1 | `UpstreamClientIsTheOnlyHttpClientTest`, `UpstreamClientIT` |
| SRC-2 | `BudgetGovernorTest`, `WebSocketBudgetIT` |
| SRC-3 | `BackoffRegistryTest`, `BackoffRegistryIT` |
| SRC-4 | `UsageMeterIT` |
| SRC-5 | `UrlRedactorTest`, `RequestRecorderTest`, `FetchTraceTest` |
| SRC-6 | `CollectorSchedulerIT`, `NoClientTriggeredFetchIT` |
| SRC-7 | `ParamValidatorTest`, `PinnedTargetTest`, `LocalAddressesTest` |
| SEC-1 | `AccessPolicyTest` |
| SEC-2 | `AdminTokenServiceIT`, `TokenHasherTest` |
| SEC-3 | `CredentialCipherTest`, `SecretKeyStoreTest` |
| PRC-2 | **was missing**; added `LicenseAllowlistTest` |

`LicenseAllowlistTest` reads the 113 jars of the runtime classpath, using embedded `pom.xml`
licenses and `Bundle-License`. It rejects GPL or AGPL with no non-copyleft alternative and no
Classpath Exception, and it deny-lists Smile, Weka, JSAT and jdistlib. A non-vacuity test requires
at least 10 jars with license metadata; 54 have it.

## Compile result

`./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL. The only output is the existing
unchecked-operations note on `EntityMappingIT`.

## Notes for the next prompt

1. **Every new IT shares one PostGIS container.** It now survives any context closing. Each new
   distinct context still adds a 10-connection pool; `max_connections=500` fits the cache's 32
   contexts.
2. **Startup on the ready event.** A `SmartLifecycle` bean that starts on the ready event must
   become auto-startup after its first start, or a paused and restarted context leaves it stopped
   (see `CollectorScheduler.isAutoStartup`).
3. **New setting keys.** Add any new setting key to `SettingKeys.ALL`.
4. **Grouping SQL by day.** SQL that groups by day or hour must pass `'UTC'` to `date_trunc`. JDBC
   sessions run in the JVM's time zone.
5. **WireMock gzip.** `WireMockSupport` has gzip off. Stub compressed bodies explicitly.
6. **Owner decisions pending:** Issue 1 (`touched` semantics) and Issue 2 (LGPL `geolatte-geom`).
