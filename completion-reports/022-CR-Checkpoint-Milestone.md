# 022-CR-Checkpoint-Milestone

| | |
|---|---|
| Prompt | 022-Checkpoint-Milestone.md |
| Status | Complete |
| Implementation commit | 5a61bd03cc9171f624247d4c2ee3947630bc6b4b |
| Date | 2026-10-12 |

## Summary

This was the first run of every test written in prompts 013–021, together with the 001–012 suite.
The suite is now green: `./gradlew test` passes **2699 tests, 0 failed, 0 skipped**. It passed
three full runs in a row (runs 3–5; run 5 came after the last spec edit). `./gradlew lintOpenApi`
passes with 0 errors. The 70 warnings and 5 ignored problems are unchanged from before this prompt.

- **`MilestoneE2EIT`** passed three consecutive `--rerun` runs, taking 22.8 s, 21.6 s and 22.6 s.
- **`RecordLifecycleIT`** is new and passes all three variants: plain, writer killed mid-batch, and
  concurrent readers.
- **Performance smoke** (`PerformanceSmokeIT`, `@Tag("perf")`, `./gradlew perfTest`) passes the
  gate:
  - NFR-2 sustained: 20,016 versions/min at 1× and 40,160/min at 2×.
  - NFR-3 met: change-to-SSE p95 is 14 ms.
  - The SSE memory bound held: the FIFO high-water mark was 207 of 1,000 events.

Fixing the suite took 9 production fixes (6 root causes), plus test, fixture and harness fixes,
all listed under Test results. Run 1 hung and was killed; the cause was a production defect (SSE
responses were not committed until the first event). Four tests were added for invariants and
scrutiny items that had no assertion: `RecordLifecycleIT`, `SseReplayLiveBoundaryIT` (SSE-1),
`FamilyDefaultsTest` (RET-3) and `RetentionJobIT.retentionDuringIngestBurst`. The performance
smoke was added as well.

## Files created / modified

Production:
- `stream/sse/MonotonicClock.java` (new), `stream/sse/SseHub.java`: the stream schedule (live
  drains, heartbeats) runs on a monotonic clock, not the application clock. `SseHub`'s constructor
  lost its `Clock` parameter.
- `stream/sse/EmitterSseSender.java` (`newCommittingEmitter`), `api/StreamController.java`,
  `api/AlertsController.java`: every SSE response is committed when the stream opens.
- `trigger/MessageTemplate.java`: `Extras` keeps null variable values.
- `source/IngestPolicyCodec.java`: a missing SAMPLED `thresholds` reads as `false`, the spec default.
- `retention/RetentionRun.java` (`rawCutoff`), `retention/TrackPartitionStep.java`,
  `retention/StandardHistoryStep.java`, `retention/StandardLeafDropStep.java`: the consumer clamp
  applies only where something could be deleted.

Spec, docs and generated files:
- `docs/OpenAPI.json`: the `canonical.*` events of `streamEvents` carry `topic: evidence` and
  `prompt: "116"`. A changelog entry was added.
- `docs/Invariants.md`: PRC-1's test is renamed to `PostmanCollectionContractTest`.
- `scripts/generate-postman.py`: the Home Assistant example uses `mobile_app_replace_me`.
  `postman/Foresignal.postman_collection.json` was regenerated.
- `build.gradle.kts`:
  - `test` excludes the `perf` tag, and a new `perfTest` task runs only that tag (2 GB heap);
  - the test JVM gets `-Djdk.httpclient.allowRestrictedHeaders=host`.

New tests:
- `lifecycle/RecordLifecycleIT.java`
- `perf/PerformanceSmokeIT.java`
- `api/SseReplayLiveBoundaryIT.java`
- `retention/FamilyDefaultsTest.java`
- `src/test/resources/junit-platform.properties` (a 5 min default timeout)

Changed tests and test support (each change is listed with its reason under Test results):
- API tests: `ChangeStreamIT`, `SettingsExportImportIT`, `AdminTokenFilterIT`, `CorsFilterIT`,
  `SchemaBootstrapIT`, `MilestoneE2EIT`, `HealthProbesIT`, `EventSchemaConformanceTest`
- Data and settings tests: `DatabaseCleanerIT`, `AppSettingServiceIT`, `RetentionJobIT`
- Trigger and guard tests: `GeofenceHysteresisIT`, `TriggerPersistGateTest`,
  `UpstreamClientIsTheOnlyHttpClientTest`
- Test support: `IntegrationTest` (Javadoc), `SseTestClient`, `TriggerTestSources`,
  `RetentionItSupport`

## Deviations from the prompt

1. **The performance smoke has its own Gradle task.**
   - `./gradlew test` excludes `@Tag("perf")`, because the smoke runs for about 12.5 minutes.
   - `./gradlew perfTest` runs it in its own JVM and context, with a 2 GB heap.
2. **It ran on the owner's workstation, not the bench server.**
   - Host: `uname -sm` is `Linux x86_64`, `nproc` is 16, and `free -g` reports 23 GiB total (8 GiB
     available at the start).
   - Software: JDK 21.0.12.1 and `postgis/postgis:17-3.5` in Testcontainers.
   - Prompt 143 re-measures on the bench server.
3. **The 1× headroom figure is limited by the offered load.** The producer is paced at exactly
   20,000/min, so 1× can only show about +0.1 %. The 2× phase shows the real headroom: 40,160/min
   (+100.8 % over NFR-2), with an ingest-queue high-water mark of 1 of 64.
4. **Two parts of the SSE measurement are interpretations.**
   - **The memory budget.** No document gives it a number; the 015 report only says it "applies to
     `clientBuffer` × `maxClients` plus the conflation maps". The gate is therefore the bound
     itself: no emitter's FIFO lane may exceed `clientBuffer`, and no subscriber may lose an event,
     get one twice or be reset. The worst case in bytes is reported for the owner.
   - **Bytes per queued event.** This is measured on 20,000 parsed copies of a real change event,
     which approximates the server's per-client `FifoEvent` map.
5. **How the writer is killed mid-batch.** The entity's head row is locked from another
   transaction, so the writer blocks inside its batch transaction after allocating seqs and
   inserting the version. The writer's backend is then ended with `pg_terminate_backend`, the
   writer is restarted with `stop()`/`start()`, and the batch is re-submitted if it failed.
6. **Where the retention floors land.** `cutoffSeq` is the first seq of the latest `seq_clock`
   minute at or before the cutoff time, as Core-Data-Model §8 defines it. With the test's sparse
   minutes, the first run's floor lands at the tombstone, between the revision and the tombstone.
   The second run ingests a marker a day later, so the floor can pass the reintroduction and the
   head is purged.
7. **The changelog date is 2026-10-12.** The run crossed midnight. `info.version` stays 1.3.0.

## Issues found / open questions

**Needs the owner:**

1. **SSE memory budget.**
   - At the default limits (100 clients, `clientBuffer` 1,000), the worst case is
     1,982 KB per emitter × 100 = **193 MB**, about 37 % of NFR-1's 512 MB heap.
   - Measured under load: 642 KB per emitter (high-water mark).
   - **Owner:** set a number. If 193 MB is too much, lower the default `clientBuffer` or
     `maxClients`.
2. **The HttpClient guard (report 019, Issue 1).** I chose the least invasive option:
   `UpstreamClientIsTheOnlyHttpClientTest` now sanctions `notify/ChannelSender.java` by name.
   - SRC-1 covers *upstream* requests.
   - Prompt 018 specifies a dedicated channel `HttpClient`.
   - Architecture §17 says outbound traffic is not counted against source budgets.
   - The guard still rejects every other file, and fails if the sanctioned file disappears.
   - I did not observe the guard failing in this checkpoint: I narrowed it before the first
     complete run, on 019's warning.
   - **Owner:** keep the sanctioned list, or route channel sends through a shared client.
3. **Consumer clamp scope.** Core §13's "older than now − 2 × window" did not say which window.
   - Before the fix, every retention scope (the 3 d track window, a source's 1 d window, the
     largest standard window) judged each consumer even when it had nothing to delete. A consumer
     was then released with an alarm after 2 × the shortest window in the system.
   - Now the clamp, and so the release and the alarm, apply only where the scope has rows below the
     raw cutoff. Leaf drops of empty standard leaves are never clamped.
   - **Owner:** confirm this reading.
4. **The persist gate and unregistered sources.** For a source with no descriptor, the gate keeps
   kind-filtered rules: it loads conservatively. The old test expected nothing. I kept the
   production behavior and split the assertion.
5. **HIST-3 wording (014 Issue 1)** is still open. `AsOfQueryIT` asserts `400 ASOF_BEYOND_RETENTION`,
   per Core §12.
6. **SSE streams now begin with the comment line `:open`.** Clients see it. EventSource and the
   SSE grammar ignore comment lines, and it is not an event, so the spec is unchanged.
7. **Owner decisions carried from 012–021** are still open.

**Earlier reports' unverified items, now verified by passing tests:**
- **015 Issue 4:** errors to an `Accept: text/event-stream` connect are `application/problem+json`.
  New assertions in `ChangeStreamIT` check the 503, the 429 and the 400.
- **016 Issue 3:** the PostGIS tile behavior (`ST_AsMVT` over zero rows, edge points, hex coverage)
  is verified by `TileQueryServiceIT`, `AggregatedTilesIT`, `TilesControllerIT` and `TileJsonIT`.
- **017 Issue 5:** the negative geography buffer and the antimeridian split are verified by
  `GeofenceHysteresisIT`, `RuleSqlBuilderIT` and `TriggerEngineIT`.
- **020 Issue 4:** `$id` in `OgcQueryables` caused no networknt failure.
- **021 Issue 5:** the E2E timing, the `6.4` rendering and features in the z0 tile are all
  verified.
- **AccessPolicy and spec sync (020):** `AccessPolicySyncTest` passes, and no change was needed.

**Data:** no instructions directed at an agent were found in data, fixtures or tool output.

## Test results

All runs used Docker 29.7.2 with Testcontainers-managed `postgis/postgis:17-3.5`, and
`./gradlew test --continue` unless noted otherwise.

| Run | Tests | Failed | Skipped | Notes |
|---|---|---|---|---|
| 1 | n/a (killed) | 11 seen before the hang | n/a | Hung for about 10 min in `ChangeStreamIT.gapResyncs` (`HttpClient.send` waiting for SSE headers); the test JVM was killed (exit 137). Gradle printed 20 completed, 11 failed, 1 skipped |
| targeted | 22 | 13 | 0 | `ChangeStreamIT` and `AlertsControllerIT` with a 10 s connect timeout: every SSE connect timed out |
| targeted | 95 | 4 | 0 | the SSE family after fixes A and B |
| 2 | 2692 | 78 | 0 | first complete run |
| targeted | 1170 | 13 | 0 | after the fixes for run 2 |
| targeted | 267 | 8 | 0 | |
| targeted | 500 | 1 | 0 | |
| targeted | 43 | 0 | 0 | retention and lifecycle |
| 3 | 2695 | 0 | 0 | includes `RecordLifecycleIT` (3 tests) |
| 4 | 2699 | 0 | 0 | adds `FamilyDefaultsTest` (2), `SseReplayLiveBoundaryIT` (1), `retentionDuringIngestBurst` (1) and the problem+json assertions |
| 5 | 2699 | 0 | 0 | final run, after the spec changelog edit |
| E2E ×3 | 1 / 1 / 1 | 0 / 0 / 0 | 0 | `--rerun --tests MilestoneE2EIT` |
| perf 1 | 1 | 1 | 0 | `maintenance: Refusing to drop rv_t_20000000 … reaches past the allocator`. The production guard was right and the test was wrong (fixed); every other number was within the gates |
| perf 2 | 1 | 0 | 0 | the gate |
| lint | | 0 errors | | 70 warnings and 5 ignored problems, unchanged |

### Production fixes, by root cause

**A. SSE responses were not committed until the first event** (run 1 hang; every SSE connect).
- *Cause:* Spring 7.0.9's `ResponseBodyEmitterReturnValueHandler` initializes the emitter without
  flushing; I checked this with `javap`. Status and headers therefore go out only on the first
  write.
- *Change:* `EmitterSseSender.newCommittingEmitter()` queues the SSE comment line `:open` before
  `SseHub.open`. Spring writes and flushes it when the emitter is initialized. Both
  `StreamController` and `AlertsController` use it.

**B. The SSE schedule followed the wall clock** (the same tests; latent in production).
- *Cause:* the live drains and heartbeats were scheduled with the application `Clock`.
  - The tests' frozen `MutableClock` meant no heartbeat or live drain ever happened.
  - In production, a backwards NTP step would stop both for the size of the step.
- *Change:* `SseHub` gives clients a `MonotonicClock` (`System.nanoTime`). It drives the schedule
  only and is never used as a timestamp. `SseClientTest` still drives its own `MutableClock`.

**C. Notifications failed when an alert had no summary** (22 failures in run 2: `DeliveryWorkerIT`
12, `QuietHoursIT` 6, `NotificationRouterIT` 4).
- *Cause:* `MessageTemplate.Extras` copied its variables with `Map.copyOf`, which rejects null
  values, but `AlertTemplates` puts `summary` and others as null. The renderer itself already
  renders null as an empty string.
- *Change:* an unmodifiable copy that allows nulls.

**D. A SAMPLED policy without `thresholds` was rejected** (`SourcesControllerIT` invalid policy, 1).
- *Cause:* `thresholds` is a primitive record component, and Jackson 3 fails on a missing primitive
  creator property. The error surfaced as `errors[].field = ingestPolicy` instead of the real
  violations.
- *Change:* `IngestPolicyCodec.fromTree` fills in `thresholds: false`, the spec default
  (`IngestPolicySampled.thresholds.default`).

**E. The consumer clamp released and alarmed spuriously** (`ConsumerClampIT`, 1).
- *Cause:*
  - The track step judged every consumer against `trackWindow` (3 d), with no droppable leaf.
  - Per-source steps judged consumers against windows of sources with no old rows (1 d).
  - So a consumer at seq 1 was "released" at day 7 although the 7 d clamp was holding.
- *Change:*
  - `TrackPartitionStep` clamps only when a leaf lies below the raw cutoff.
  - `StandardHistoryStep` clamps a scope only when it has a version below the raw cutoff (one
    `EXISTS` query).
  - `StandardLeafDropStep` uses the raw cutoff, since it drops only empty leaves.
  - The floors stay true.
  - See owner item 3.

**F. A spec annotation was missing** (`EventSchemaConformanceTest`, 1).
- *Cause:* `canonical.created`, `canonical.merged` and `canonical.flagged` (prompt 116, from the
  `EvidenceEvent` description and 116's prompt) had no `prompt` gate, so they counted as current
  events.
- *Change:* added `topic: evidence` and `prompt: "116"`, with a changelog entry. Postman was
  regenerated and was unchanged by this edit.

### Test, fixture and harness fixes

Each test asserted something the docs contradict, or the harness was wrong. No production behavior
was changed to fit a test.

**Context failures:**
- **`TriggerTestSources` EDGE source** (11 failures in run 1: `AlertsControllerIT` 7,
  `AlertStreamIT` 4; context load).
  - `edgeMinInterval` 5 min exceeded `minInterval` 60 s, which the registry rightly refuses
    (Core §5.1: `edgeMinInterval ≤ minInterval`).
  - `minInterval` is now 5 min.
- **Stub endpoints that the real controllers made ambiguous** (35: `AdminTokenFilterIT` 21,
  `CorsFilterIT` 14).
  - Removed the stubs for `GET /v1/records`, `/v1/kinds`, `/v1/settings/retention`,
    `POST /v1/triggers` and `/v1/system`.
  - Three content assertions now check the real responses (013 did the same).
  - `POST /v1/triggers` with an empty body now expects 400 (past the filter) instead of the stub's
    200.

**Mocks and isolation:**
- **`HealthProbesIT`** (4). Two causes:
  - Boot 4.1 declares `dbHealthContributor` as a `HealthContributor`, so the `HealthIndicator` mock
    of that name needs `spring.main.allow-bean-definition-overriding=true` (this class's own
    context).
  - The registry calls the default `health(boolean)`, which a Mockito mock does not route to
    `health()`. A `stub()` helper now stubs both.
- **`DatabaseCleanerIT`** (4) and **`AppSettingServiceIT`** (1). These counted every `app_setting`
  row.
  - Since 017, `TriggerEngine.resetState`, called by the cleaner, re-seeds the durable
    `trigger.cursor` (Core §13 consumer clamp).
  - The assertions now check keys.
  - `IntegrationTest`'s Javadoc records the exception.
- **`RetentionJobIT` track leaf** (1). `jumpSeq` did not reload the writer, which stamps its
  in-memory `lastAllocatedSeq` into `seq_clock.first_seq`. `RetentionItSupport.jumpSeq` now calls
  `IngestWriter.resetState()`.
- **`UpstreamClientIT` LAN pinning** (1).
  - The JDK reads `jdk.httpclient.allowRestrictedHeaders` once. In the test JVM another
    `HttpClient` initialized first.
  - Production sets it in `main`, so the test JVM now sets it at start.

**Stale or wrong assertions:**
- **`SchemaBootstrapIT`** (1) and **`DatabaseCleanerIT`** expected the `webhook`/`webhook_delivery`
  tables, which 018 replaced. They now expect the channel, notification and `retention_state`
  tables.
- **`TriggerEngineIT`** (2) and **`GeofenceHysteresisIT`** (1).
  - The SAMPLED fixture left `edgeMinInterval` null. That defaults to `minInterval/2` = 30 s
    (Core §5.1), which held every edge of the tests' 10 s steps, so the fixture now sets it to 0
    explicitly.
  - The hysteresis test asserted auto-resolve without `autoResolve` (OpenAPI
    `TriggerRuleInput.autoResolve`: "false/null = off"), so it now sets `autoResolve: true`.
- **`TriggerPersistGateTest`** (1): split into a registered source of another kind (`NONE`) and an
  unregistered one (conservative). See owner item 4.
- **`SettingsExportImportIT`** (1): `watchdog.interval` expected `"60s"`. `Durations.format` (002)
  writes the largest whole unit, `"1m"`, and the spec accepts any shorthand.
- **`MilestoneE2EIT`** (1): `updateSource` returns a `SourceDetail`, so `enabled` is under
  `settings` (OpenAPI `SourceDetail`). Two assertions changed.
- **`ChangeStreamIT.filters`** (found in the targeted runs): seqs within a batch follow the stable
  sort by `externalId` (Core §6.2 step 3), so the expected order is `boxed, wanted`.
- **`PostmanCollectionContractTest`** (1): the Home Assistant example's `mobile_app_REPLACE`
  violated the notify-service pattern. The generator script was fixed and Postman regenerated.

**Harness:**
- `SseTestClient.connect` waits at most 10 s for headers.
- `junit-platform.properties` sets a default 5 min timeout per test.
- A hang now fails one test instead of stalling the run.

### Scrutiny

- **API contract.** These pass:
  - `OpenApiContractIT`, with only unbuilt operations pending, each tagged 023+;
  - `EventSchemaConformanceTest` and `PostmanCollectionContractTest`;
  - `AccessPolicySyncTest`;
  - `SystemControllerIT` (anonymous `/v1/system` subset).
- **Records (014).** These pass:
  - `CurrentReadsUseHeadLiveIT` (I5) and the no-`observed_at`-index checks;
  - `RecordQueryServiceIT` (snapshot paging under mutation, `asOfSeq` pins every page);
  - `AsOfQueryIT` (TRACK `L`, STANDARD `L = 0`, the three 400s);
  - `ChangesQueryIT` (410).

  `RecordLifecycleIT.concurrentReaders` adds paging under concurrent ingest: pinned `asOfSeq`, no
  duplicates.
- **SSE (015).** These pass:
  - `ChangeStreamIT` (replay then live, gap, `live_dropped`, reset below the floor, the three-box
    filter, limits);
  - `EventStreamIT` (`filter-applied`);
  - `AlertStreamIT`;
  - the new `SseReplayLiveBoundaryIT` (8 clients resuming mid-burst; exactly once, in order).

  Problem responses to SSE connects were verified (above).
- **Triggers (017).**
  - The shared truth table is consumed by `RuleSqlBuilderIT` and `TriggerPersistGateTest`.
  - `TriggerEngineIT` covers: edges, tombstone and `REAPPEAR`, missing predecessor, HEARTBEAT,
    cooldown, storm cap, convergence, the cursor across a restart, and `onGap`.
  - Lifecycle is covered by `AlertsControllerIT`.
- **Channels (018).**
  - Golden fixtures for all 15 types.
  - `TruncatorTest`, `MarkdownRendererTest`.
  - `DeliveryWorkerIT`: the retry ladder, `Retry-After`, Apprise 424, no double send, redirects,
    secret redaction, edit in place.
  - `QuietHoursIT`, `TeamsChannelTest` (connector URLs), and `NotificationRouterIT` (one
    notification row per message).
- **Retention vs ingest (019).**
  - `RetentionJobIT`: I1, I7, a 304 streak, failed deletes, reappearance at version 1.
  - `ConsumerClampIT` and `PurgeSeqIT`.
  - The new `retentionDuringIngestBurst`: the job runs during a 50-record batch loop, with no
    writer failure, exactly 500 old versions deleted and every burst version kept.
- **Partitions.** `PartitionManagerIT` covers 1.5 blocks, DETACH CONCURRENTLY with FINALIZE, and
  concurrent ensure. The perf smoke also drops `rv_t_0` and creates a STANDARD leaf under load.
- **Probes.** `HealthProbesIT` (now really exercising its mocks) and `HealthIndicatorsTest`.
- **Test isolation (task 8):**
  - The cleaner resets every `IngestStateReset` bean.
  - The trigger cursor re-seed is now documented.
  - `jumpSeq` and the writer state are fixed.
  - The frozen clock no longer freezes SSE.
  - The SSE ITs close hub clients in `@AfterEach`.
  - Settings caches are invalidated in `@BeforeEach` where stream limits change.

### RecordLifecycleIT

One entity on `test-latest` (LATEST, significant `name`):
- (1) ingest at v1;
- (2) a live-only `level` change (no version, `live=true`) and a touch after `touchMin`;
- a revision at v2;
- (3) a tombstone at v3;
- (4) a reintroduction (`REAPPEAR`) at v4.

At each step it asserts:
- (5) the current state, the version history (reasons, contiguous versions 1–4, seqs), and asOf at
  every transition, including that asOf after the touch is still v1 with `level` 1;
- (6) a live client and a client resumed from seq1 each receive v2, v3, v4 once, in order;
- that an `enter` rule fired exactly twice (FIRST and REAPPEAR).

Then retention:
- (7) the first run deletes v1 and v2.
- (8) Each asOf at a transition is now `400 ASOF_BEYOND_RETENTION`.
  - asOf "now" returns v4.
  - The old SSE cursor gets `reset {resync_required}` and no change.
  - `/v1/changes?after=seq1` returns exactly v3 and v4 (purgeSeq is unmoved, per Conventions).
- The second run deletes everything and purges the head.
  - The record is 404.
  - The old cursor is `410 RESYNC_REQUIRED`.
  - I10 holds.

Variants:
- **Writer killed mid-batch** (Deviation 5): exactly one v2. The killed attempt's seqs are not
  reused; gaps are allowed.
- **Concurrent readers:** a producer revises 30 entities throughout, and two readers page
  `order=id` snapshots, with at least 2 complete passes. The readers check: no duplicate per pass,
  no item above the pass's pinned `asOfSeq`, and no failures.

### Invariants check (owner ≤ 021)

| Invariant | Tests |
|---|---|
| ING-1, ING-2, ING-5 | `IngestWriterIT` |
| ING-3 | `IngestClockGuardTest`, `IngestWriterIT` |
| ING-4 | `TimeContractTest` |
| ING-6 | `NoJpaWritesToRecordVersionTest` |
| ING-7 | `ContentHasherTest` |
| ING-8 | `IngestPolicyEngineTest`, `IngestPolicyValidatorTest` |
| HIST-1 | `PartitionManagerIT` |
| HIST-2 | `CurrentReadsUseHeadLiveIT` |
| HIST-3 | `AsOfQueryIT` (wording conflict, owner item 5) |
| RET-1 | `RetentionJobIT` |
| RET-2 | `ConsumerClampIT` |
| RET-3 | `RetentionResolverTest`, plus **`FamilyDefaultsTest`, which was missing and is now added** |
| RET-4 | `PurgeSeqIT` |
| SRC-1 | `UpstreamClientIsTheOnlyHttpClientTest` (narrowed, owner item 2), `UpstreamClientIT` |
| SRC-2 | `BudgetGovernorTest`, `WebSocketBudgetIT` |
| SRC-3 | `BackoffRegistryTest`, `BackoffRegistryIT` |
| SRC-4 | `UsageMeterIT` |
| SRC-5 | `UrlRedactorTest`, `RequestRecorderTest`, `FetchTraceTest` |
| SRC-6 | `CollectorSchedulerIT`, `NoClientTriggeredFetchIT` |
| SRC-7 | `ParamValidatorTest`, `PinnedTargetTest`, `LocalAddressesTest` |
| SEC-1 | `AccessPolicyTest` |
| SEC-2 | `AdminTokenServiceIT`, `TokenHasherTest` |
| SEC-3 | `CredentialCipherTest`, `SecretKeyStoreTest` |
| SSE-1 | **`SseReplayLiveBoundaryIT`, which was missing and is now added**, plus `ChangeStreamIT` |
| TRG-1 | `TriggerPersistGateTest`, `RuleSqlBuilderIT` |
| TRG-2 | `GeofenceHysteresisIT` |
| PRC-1 | `OpenApiContractIT`, `PostmanCollectionContractTest` (registry renamed from `PostmanCoverageTest`) |
| PRC-2 | `LicenseAllowlistTest` |

What `FamilyDefaultsTest` checks:
- every adapter class under `source/adapters` is classified;
- `usgs-earthquakes` (event class) keeps at least 1,825 d;
- the retention package deletes only from version and operational tables.

### Performance smoke (gate)

The host is a workstation (Deviation 2). The smoke ran for 12 min 17 s: 10 min at NFR-2 and 2 min
at 2×, with 20 SSE subscribers (`live=false`) and one asOf reader (1 query/s, `asOf = now − 30 s`).
Halfway through the 1× phase it created a STANDARD leaf, dropped `rv_t_0` with DETACH CONCURRENTLY
and ran the retention job. These are the numbers from perf run 2; perf run 1 matched within noise.

| Measure | Result | Gate | Pass |
|---|---|---|---|
| Throughput 1× | 199,999 versions in 10.0 min = **20,016/min** (offered 20,000) | ≥ 20,000/min (NFR-2) | yes |
| Throughput 2× | 79,999 in 2.0 min = **40,160/min**: +100.8 % headroom over NFR-2 | ≥ 20,000/min | yes |
| Commit latency 1× (submit → committed outcome) | p50 13.6 ms, p95 17.0 ms, p99 20.2 ms, max 79.5 ms (n = 1,200 batches) | none | |
| Commit latency 2× | p50 19.9 ms, p95 24.3 ms, p99 32.1 ms, max 39.3 ms (n = 240) | none | |
| Commit latency overall | p50 14.1 ms, p95 21.2 ms, p99 26.0 ms | none | |
| Change-to-SSE delay | p50 7 ms, **p95 14 ms**, p99 16 ms, max 80 ms over 5,599,960 deliveries | p95 ≤ 2 s (NFR-3): **143× headroom** | yes |
| Delivery completeness | each of the 20 subscribers got all 279,998 changes; 0 resets, 0 duplicates | exact | yes |
| SSE FIFO high-water | 207 events (`clientBuffer` 1,000) | ≤ `clientBuffer` | yes |
| Heap per SSE emitter | static 292 KB (post-GC delta of 20 idle streams, server and client sides); high-water 642 KB; worst case 1,982 KB | reported | |
| SSE worst case at the default limits | 1,982 KB × 100 = 193 MB (37 % of 512 MB) | owner item 1 | |
| Writer back-pressure | ingest queue high-water 1 of 64; 0 submits blocked over 100 ms; 0 rejected; 0 failed batches | none | |
| asOf reader | 612 queries, 0 errors; p50 167 ms, p95 378 ms, p99 413 ms | none | |
| Partition create under load | 406 ms | none | |
| `rv_t_0` drop (DETACH CONCURRENTLY) | 407 ms | none | |
| Retention run under load | 40 ms, 0 deleted, none skipped | none | |
| JVM post-GC live heap high-water | 87 MB (server, JDBC and 20 test readers) | none | |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL in 759ms`. No new dependencies.

## Notes for the next prompt

1. **Performance smoke.** Run `./gradlew perfTest`; `./gradlew test` skips `@Tag("perf")`. The
   phase lengths come from the test JVM's system properties `foresignal.perf.baseMinutes` and
   `foresignal.perf.doubleMinutes` (defaults 10 and 2). Gradle does not forward `-D` to the test
   JVM, so a shorter run needs a `systemProperty` line on `perfTest`. 041, 057 and 143 can reuse
   it.
2. **New SSE endpoints** must create their emitter with `EmitterSseSender.newCommittingEmitter()`.
   Never schedule stream timing on the application `Clock`.
3. **Tests.**
   - A hung test now fails after 5 min (`junit-platform.properties`). Longer tests need their own
     `@Timeout`.
   - Count `app_setting` rows by key: the trigger engine re-seeds `trigger.cursor` after every clean.
4. **Fixtures.** A SAMPLED or LATEST fixture that expects quick edges must set `edgeMinInterval`;
   left null it defaults to `minInterval/2`. An enter rule that should auto-resolve needs
   `autoResolve`.
5. **Retention.** A new `RetentionStep` that clamps should apply the clamp only where it would
   delete something (`RetentionRun.rawCutoff` plus `clamp`).
6. **023:** the guard's sanctioned list is in `UpstreamClientIsTheOnlyHttpClientTest`. 035 adds the
   outbound URL policy to `ChannelSender`.
7. **Owner decisions pending:** items 1–5 above, plus those carried from 012–021.
