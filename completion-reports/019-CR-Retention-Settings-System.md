# 019-CR-Retention-Settings-System

| | |
|---|---|
| Prompt | 019-Retention-Settings-System.md |
| Status | Complete |
| Implementation commit | 03901048492436a8b54ae84b965c1f1f66d28c9e |
| Date | 2026-10-11 |

## Summary

This prompt finishes the operator surface:

- tier-aware retention, with the state that the read API and SSE need for `410` and `reset`;
- CORS, settings export and import, and the watchdog settings;
- a capability-rich `/v1/system` with an anonymous subset;
- probe groups and cached operator gauges;
- an off-box watchdog with an opt-in ingest canary;
- configurable log formats and trace ids in logs.

The 11 operations assigned to 019 in `docs/OpenAPI.json` are implemented and marked
`x-foresignal-status: implemented`:

- `getRetention`, `updateRetention`, `runRetention`
- `getCors`, `updateCors`
- `exportSettings`, `importSettings`
- `getWatchdogSettings`, `updateWatchdogSettings`
- `getSystem`, `rotateAdminToken`

**Retention settings and windows (`ai.foresignal.retention`, `ai.foresignal.settings`).**

- `RetentionSettings` now has `default` (7d), `trackWindow` (3d), `provenance` (30d), `alerts`
  (90d), `usage` (30d), `notifications` (90d), `briefings` (180d), `ask` (90d), `kinds` and
  `sources`. It keeps the shorthand serialization and the pre-019 five-argument constructor.
- `RetentionResolver`:
  - `window(source, kind)`: TRACK always uses `trackWindow`; STANDARD uses `kinds.{kind}`, then
    the per-source override, then the descriptor's `kindRetention`, then its `defaultRetention`,
    then `default`. Unregistered ids use `default`.
  - `shortestWindow`, `windowsByKind`, `standardWindows`, `largestWindow`, `overrides`.
  - `effectiveKeyframe` and `horizon(now, window, keyframe)`. 014's `AsOfQueryService.horizon`
    now calls these.
- `SourceService.effectiveRetention` delegates to `RetentionResolver.sourceWindow`.
  `SourceIngestProfiles` takes its window from `RetentionResolver.shortestWindow`.
- `RetentionSettingsService` (GET, PUT, and the import's parse and apply):
  - every duration must be at least 1h;
  - unknown sources and kinds are listed in one 400;
  - `RETENTION_TIER_FIXED` for a TRACK source or a TRACK-only kind, still with every error;
  - `sources` is a view over `source_settings.retention`. A PUT writes those rows through
    `SourceService.update` in the same transaction, clears the overrides it does not list, and
    keeps the `app_setting` copy empty.

**RetentionJob (not `@Transactional`).**

- Runs hourly with a 5-minute initial delay, off in the test profile (`foresignal.retention.*`).
  `runRetention` runs it synchronously; a second concurrent run is `409 ALREADY_RUNNING`.
- The steps are ordered `RetentionStep` beans, so later prompts add their own:
  - `TrackPartitionStep` (100): `PartitionManager.drop` for every `rv_t_*` leaf whose upper bound
    ≤ `cutoffSeq(now − trackWindow)`. The track floor is the lower bound of the oldest remaining
    leaf.
  - `StandardHistoryStep` (200): chunked deletes of 10,000 rows in autocommit transactions, with
    the outer seq bound. There is one statement per source when every kind shares a window;
    otherwise one per kind plus an "other kinds" pass. Unregistered sources are found by a loose
    index scan and use `default`. A failure skips the source: no floors and no purge.
  - `StandardLeafDropStep` (300): drops `rv_s_*` leaves below the largest window's cutoff. It
    checks that a leaf is empty first.
  - `HeadPurgeStep` (400): a read pre-check, then **one** `HeadPurge` with a
    `PurgeSpec(source, kind?, floorSeq, staleBefore)` per qualifying pair, where
    `staleBefore = min(now − window, last_data_at − touchMin)`.
  - `ProvenanceStep` (500): `fetch_log` rows with 0 versions after 7d and the others after
    `provenance`; `seq_clock` minutes older than the largest window + 1d.
  - `OperationalTablesStep` (700): resolved or suppressed alerts after `alerts` (open alerts are
    kept, as the 017 notes asked), `source_usage` after `usage`, settled deliveries after 7d, and
    `notification` rows with no delivery left after `notifications`.
- **Consumer clamp:**
  - the `RetentionConsumer` SPI (`name()`, `cursorSeq()`); `TriggerCursorConsumer` registers
    `trigger.cursor`;
  - every cutoff is clamped to `min(cursor) + 1`;
  - a cursor older than `cutoffSeq(now − 2 × window)` releases the clamp, increments
    `foresignal.retention.consumer_lag{consumer}` and publishes a `system.retention_consumer_lag`
    notification through a new `notify.SystemEvent`.
- **`PurgeSpec` gained an optional `kind`.** `PurgeOutcome` gained `watermarkSeq`, which the writer
  reads from `ChangeBus.lastPublishedSeq()` on its own thread when the purge completes. The writer
  filters candidates by kind when one is set.
- **`RetentionStateStore` replaces `DefaultRetentionState`:**
  - backed by the new Hibernate table `retention_state` (`source_id`, `kind`, `floor_seq`,
    `purge_seq`, `purged_at`, `last_run_at`, PK `(source_id, kind)`, with kind `*` for the source
    row);
  - loaded at startup (`StartupOrder.RETENTION_STATE = 12`), republished after every run, and
    reloaded by the test cleaner;
  - floors are never lowered;
  - `lowestRetainedSeq` is the largest floor over the set;
  - `purgeSeq` advances only when a purge removed at least one head of the source.
- `RetentionRunResult` carries every spec field, including `partitionsDropped`, `headsPurged`,
  `skippedSources`, `consumerClamp` and `perSource`.
- Metrics: `foresignal.retention.deleted{table}` and `foresignal.retention.duration`, plus one
  summary log line per run.
- The concurrency rules (I1, I6, I7, FR-17) are documented in the `retention` package Javadoc.

**CORS, export and import, watchdog settings.**

- `SettingsController`: CORS through `CorsSettingsService`; the watchdog through
  `WatchdogSettingsService`, which validates the URLs (http or https), `interval` ≥ 30s, the
  `GET`/`POST` method and the canary.
- `SettingsTransferService`:
  - **Export:** source settings (never credentials), retention, CORS, notifications, watchdog,
    full trigger rules with ids, channels without secrets, `exportedAt`, `foresignalVersion`.
  - **Import:** one transaction:
    - unknown sources are reported;
    - an enable without the required credentials is downgraded to disabled with a warning;
    - triggers and channels are upserted by id through the new `TriggerRuleService.upsert` and
      `ChannelService.upsert`; channels keep their stored secrets;
    - any error is a 400 with every error, prefixed by its bundle path, and the transaction rolls
      back.
- The notification-settings parsing moved from the controller into `NotificationSettingsService`,
  so the import shares it.

**System (`ai.foresignal.system`).**

- `SystemController`:
  - anonymous callers get `version`, `apiVersion` (the spec's `info.version`, read by
    `ApiVersion`), `startedAt`, `uptimeSeconds`, `status`, `features` and `limits`;
  - any valid token gets the full document: `database`, `records`, `sources`, `scheduler`,
    `ingestWriter`, `streams` (now with `alertSubscribers`), `notifications`, `history`, `catchUp`
    and `warnings`;
  - an invalid token is treated as anonymous, because the endpoint is open;
  - `rotateAdminToken` returns `{token}` once.
- `FeatureContributor` SPI, with `CoreFeatureContributor` (notifications, retention, watchdog,
  canary).
- `HistoryStats` reads `pg_partition_tree` per tier plus the versions per minute from
  `source_usage`, cached for 30 s.
- `CatchUpMonitor`: lag over 10,000 seq, or the queue above 80% for more than a minute.
- `SystemWarnings`: `OPEN_READS_EXPOSED`, `OPEN_READS_ALL_INTERFACES` (info), `HISTORY_RATE_HIGH`,
  `PARTITION_HEADROOM_LOW` and `RETENTION_CONSUMER_LAG`. Local addresses are judged with
  `LocalAddresses`, without DNS lookups.
- The new bootstrap property `foresignal.advertised-bind` (`${FORESIGNAL_BIND:127.0.0.1}`).

**Probes, gauges, watchdog, logging.**

- Health groups:
  - `liveness` = `livenessState`;
  - `readiness` = `readinessState`, `db`, `ingestWriter`, `partitions`;
  - `catchup` = `catchUp`;
  - details stay hidden.
- Compose: `curl` is installed in the runtime image, plus a readiness healthcheck (15 s interval,
  120 s start period). Compose passes `FORESIGNAL_BIND` and `FORESIGNAL_LOG_FORMAT`.
- `OperatorGauges` refreshes every 30 s: `foresignal.source.data_age_seconds{source}`,
  `foresignal.alerts.active{severity}`, `foresignal.records.current`,
  `foresignal.sse.clients{stream}` and `foresignal.history.rows{tier}`.
- `WatchdogService`:
  - pings `heartbeatUrl` while ready, and while the canary passes when it is enabled; otherwise it
    pings `failUrl`;
  - uses `HeartbeatPinger`: `HttpURLConnection`, 5 s timeout, no redirects, URL redacted in logs;
  - metric `foresignal.watchdog.pings{kind,result}`.
- `CanaryService` submits a synthetic record to the new built-in PUSH source `system-canary`
  (STANDARD, 1d retention, hidden from tiles through `TileLayerResolver.HIDDEN_FROM_TILES`). It
  waits up to 30 s for the change on `ChangeBus`, records `foresignal.canary.latency`, and
  publishes `system.canary_failed` on failure.
- `FORESIGNAL_LOG_FORMAT` (`ecs` under the `docker` profile, `text` otherwise) maps to
  `logging.structured.format.console` through `LogFormatEnvironmentPostProcessor`.
- Micrometer Tracing with the OpenTelemetry bridge and no exporter:
  `management.tracing.export.enabled: false`, sampling 1.0.

**Spec, Postman, docs.**

- The spec reconciliations are in Deviation 6 and in `info.x-foresignal-changelog`.
- Postman was regenerated:
  - `updateRetention`'s body now has `trackWindow`, `provenance`, `notifications` and `kinds`;
  - `updateWatchdogSettings` gained a body.
- `docs/Operator-Guide.md` gains:
  - **Retention**: tiers, windows, the resolution order, the job steps, FR-17, consumers and
    clients;
  - **Operating the instance**: probes and the healthcheck, watchdog and canary, `/v1/system`
    warnings, logs, export and import.

**Metric names (all, after review).**

| Area | Metrics |
|---|---|
| Fetch | `foresignal.fetch.duration`, `foresignal.fetch.skipped` |
| Source | `foresignal.source.status`, `foresignal.source.consecutive_failures`, `foresignal.source.data_age_seconds` |
| Ingest writer | `foresignal.ingest.batch.duration`, `.batch.retries`, `.persist`, `.purge.requeued`, `.queue.depth`, `.repair.live_missing`, `.writer.state`, `.writer.lock_lost` |
| Ingest clock | `foresignal.ingest.clock.clamped`, `.clock.jump` |
| Ingest records | `foresignal.ingest.records.{seen,persisted,live,touched,touches_throttled,late,skipped,rejected,clamped,valid_until_ignored}` |
| History | `foresignal.history.partition.headroom{tier}`, `foresignal.history.rows{tier}` |
| Streams | `foresignal.stream.change.dropped`, `.live.dropped`, `.records.dropped`; `foresignal.sse.clients{stream}` |
| Alerts | `foresignal.alerts.bus.dropped`, `foresignal.alerts.active{severity}` |
| Records | `foresignal.records.current` |
| Retention | `foresignal.retention.deleted{table}`, `.duration`, `.consumer_lag{consumer}` |
| Watchdog | `foresignal.watchdog.pings{kind,result}`, `foresignal.canary.latency` |

Every name is lowercase, dot-separated, under `foresignal.`, and puts dimensions in tags. The one
inconsistency is `foresignal.sse.clients` beside `foresignal.stream.*`. The prompt names it, so it
was kept.

## Files created / modified

**Created (main)**

- `retention/`:
  - `RetentionResolver`, `RetentionConsumer`, `RetentionStep`, `RetentionRun`, `RetentionJob`
  - `RetentionRunResult`, `RetentionProperties`, `RetentionStateStore`, `RetentionSettingsService`
  - `TrackPartitionStep`, `StandardHistoryStep`, `StandardLeafDropStep`, `HeadPurgeStep`
  - `ProvenanceStep`, `OperationalTablesStep`
- `store/`: `RetentionStateEntity`, `RetentionStateId`, `RetentionStateRepository`
- `system/`:
  - `FeatureContributor`, `CoreFeatureContributor`, `ApiVersion`, `HistoryStats`
  - `CatchUpMonitor`, `CatchUpHealthIndicator`, `IngestWriterHealthIndicator`,
    `PartitionsHealthIndicator`, `ReadinessCheck`
  - `SystemWarnings`, `SystemInfoService`, `OperatorGauges`
  - `WatchdogProperties`, `WatchdogService`, `HeartbeatPinger`, `CanaryService`
- `settings/`: `WatchdogSettings`, `WatchdogSettingsService`, `SettingsTransferService`
- `api/`: `RetentionController`, `SettingsController`, `SystemController`
- `notify/`: `SystemEvent`, `NotificationSettingsService`
- `trigger/TriggerCursorConsumer`
- `source/adapters/system/SystemCanaryAdapter`
- `config/LogFormatEnvironmentPostProcessor`
- `src/main/resources/META-INF/spring.factories`

**Deleted (main):** `retention/DefaultRetentionState`

**Modified (main)**

- `settings/RetentionSettings`, `settings/SettingKeys` (`WATCHDOG`)
- `ingest/PurgeSpec`, `ingest/PurgeOutcome`, `ingest/IngestWriter` (kind filter, watermark),
  `ingest/SourceIngestProfiles`
- `source/SourceService` (delegation), `store/query/AsOfQueryService` (horizon)
- `store/tiles/TileLayerResolver` (hidden sources), `stream/sse/SseHub` (`alertSubscribers`)
- `notify/NotificationRouter` (`SystemEvent` listener), `notify/ChannelService` (`upsert`),
  `trigger/TriggerRuleService` (`upsert`)
- `api/NotificationSettingsController` (delegates), `config/StartupOrder` (`RETENTION_STATE`,
  `CANARY`)
- `retention/RetentionState`, `retention/package-info`, `system/package-info`
- Javadoc prompt numbers: `security/AdminTokenService`, `security/TokenFilter`,
  `settings/CorsSettingsService`
- `src/main/resources/application.yml`: advertised bind, retention and watchdog mechanics, health
  groups, tracing; the docker-profile `ecs` block was replaced by the post-processor.

**Tests**

- New unit tests: `retention/RetentionResolverTest`, `retention/RetentionRunTest`,
  `system/SystemWarningsTest`, `system/CatchUpMonitorTest`, `system/HealthIndicatorsTest`,
  `system/CanaryServiceTest`, `system/HeartbeatPingerTest`, `system/WatchdogServiceTest`,
  `config/LogFormatEnvironmentPostProcessorTest`, `settings/WatchdogSettingsServiceTest`.
- New integration tests:
  - `retention/`: `RetentionJobIT`, `ConsumerClampIT`, `PurgeSeqIT`;
  - `api/`: `RetentionControllerIT`, `SettingsExportImportIT`, `SystemControllerIT`,
    `AdminTokenRotationIT`, `CorsControllerIT`;
  - `system/`: `HealthProbesIT`, `WatchdogIT`.
- New test support: `testsupport/retention/RetentionTestSources` (the shared context) and
  `testsupport/retention/RetentionItSupport`.
- Modified:
  - `ingest/SourceIngestProfilesTest` (mocks the resolver);
  - `settings/JacksonDurationIT` (the new members);
  - `testsupport/records/RecordsTestSources` (the stub wraps `RetentionStateStore`);
  - `application-test.yml` (`foresignal.watchdog.scheduled: false`).

**Build, deployment, docs and tooling**

- `gradle/libs.versions.toml`, `build.gradle.kts`, `Dockerfile`, `docker-compose.yml`
- `docs/OpenAPI.json`, `docs/Operator-Guide.md`
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`

**New dependencies** (versions from the Spring Boot 4.1.1 BOM; POMs checked on Maven Central)

- `org.springframework.boot:spring-boot-micrometer-tracing-opentelemetry` 4.1.1, Apache-2.0. It
  brings `spring-boot-micrometer-tracing`, `spring-boot-opentelemetry` and the OpenTelemetry SDK
  1.62.0, all Apache-2.0.
- `io.micrometer:micrometer-tracing-bridge-otel` 1.7.1, Apache-2.0.

`spring-boot-starter-opentelemetry` was deliberately **not** used: its POM adds the OTLP exporters
(`opentelemetry-exporter-otlp`, `micrometer-registry-otlp`), and the prompt asks for no exporter.

No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was ported from God's Eye
View.

## Deviations from the prompt

1. **The profile window is the shortest over the kinds.**
   - With per-kind windows, a source has several windows. `SourceIngestProfile.window`, which
     drives the retention keyframe (`window/4`, I6), and `RetentionState.window`, which drives the
     asOf horizon, use `RetentionResolver.shortestWindow`.
   - This keeps every kind's I6 guarantee and keeps asOf honest.
   - `SourceService.effectiveRetention` keeps its existing meaning (the override, else the
     descriptor default, else empty) and delegates to `RetentionResolver.sourceWindow`, so the
     sources API and its tests are unchanged.
2. **Horizon math is a static method.** `AsOfQueryService` calls
   `RetentionResolver.horizon(now, retention.window(id), keyframe)` rather than an instance method.
   This lets tests that stub `RetentionState.window` (014's `StubRetentionState`) keep working.
3. **Multi-window sources.** Records of kinds a descriptor does not declare are deleted with the
   source-level window. They are never head-purge candidates, because a source-wide purge at their
   floor could break I7 for kinds with lower floors.
4. **Alert retention.** Alerts are aged by `fired_at` (the table has no `created_at`), and only
   resolved or suppressed alerts are deleted. 017 noted that open alerts must stay.
5. **Other choices the prompt left open:**
   - `staleBefore` falls back to `now − window` when the source has no `last_data_at`.
   - `TriggerCursorConsumer` does not clamp while the engine is stopped.
   - The `status` rules: `down` when the database is down or the writer is stopped or has lost
     its lock; `degraded` on clock hold, catching up, a failing source or a warning.
   - `catchingUp` reads "lag > 10,000, or queue > 80% for more than 1 minute": only the queue
     condition needs the minute.
   - `limits.ingestMaxBytes` and `ingestMaxRecords` are 5 MB and 10,000, from Architecture §5
     "Ingest endpoint". 024 enforces them.
   - The canary interval minimum is 30 s.
   - Host names in `OPEN_READS_EXPOSED` are not resolved: only `localhost`, `*.localhost` and
     `*.local` names count as local.
6. **Spec reconciliations** (additive, recorded in the changelog):
   - `RetentionSettings.x-foresignal-property-prompts` listed `notifications` as 033's. The
     schema's own description, and this prompt, make `notifications` the notification-row window
     of 019, and `inbox` is 033's. The entry now lists `audit`, `inbox`, `trackRawWindow`,
     `trackDownsampleInterval` and `social` with their prompts.
   - `RetentionSettings` gained a description of the resolution order, and says that later
     members are accepted but not stored.
   - `SettingsBundle.channels` items are `ChannelInput` plus `id`, because import upserts by id.
   - `SettingsBundle.triggers` documents that ids are kept.
7. **The watchdog does not use `HttpClient`.** It pings through `HttpURLConnection`, because
   `UpstreamClientIsTheOnlyHttpClientTest` reserves `HttpClient` construction for
   `ai.foresignal.collect`. 035 retrofits the outbound URL policy.
8. **No request correlation id exists in the MDC.** The prompt says trace ids should appear "next
   to the correlation id". The only correlation id is the one `GlobalExceptionHandler` logs with an
   unexpected 500. Trace and span ids are in the MDC for every request. The Operator Guide says
   exactly this.
9. **`pending-operations.txt` does not exist yet** (020 creates it), so there were no lines to
   delete.

## Issues found / open questions

1. **A guard was already broken before this prompt.** 018's `notify/ChannelSender` calls
   `HttpClient.newBuilder()` outside `ai.foresignal.collect`, so
   `UpstreamClientIsTheOnlyHttpClientTest` will fail at checkpoint 022. This prompt adds no new
   violation. **Owner:** allow `notify` in the guard, or route channel sends through a shared
   client?
2. **A new built-in source.** `system-canary` is registered in every context and appears in
   `/v1/sources` and in exports. It is disabled, and only the canary writes to it. A test that
   counts registered sources would see one more.
3. **Test partitions.** `RetentionJobIT`'s track test moves `record_seq` to 20,000,000 and drops
   `rv_t_0`. It restores coverage afterwards; the writer's pre-flight recreates `rv_t_0` when a
   later test needs it.
4. **Trigger settings are not exported.** 017 asked to expose `TriggerSettings` (key `triggers`),
   but `SettingsBundle` has no member for it. **Owner:** add one to the spec?
5. **Single-process assumptions** (carried from 017 and 018):
   - the one-run guard and the canary's pending future are in-process;
   - the delete chunks and the purge are safe across processes; the job is not.
6. Owner decisions carried from 012–018 are still open.

No instructions directed at an agent were found in data, fixtures, web pages or tool output. Maven
Central POMs were the only fetched pages, read to verify the tracing artifacts.

## Tests written (not run)

As a feature prompt requires, no tests were run. The only external calls in tests go to WireMock.

- **`RetentionResolverTest`:**
  - each of the five steps, a null kind, and TRACK always `trackWindow`;
  - `effectiveKeyframe` and the horizon formula;
  - unregistered sources, `shortestWindow`, the override read from `source_settings`, the window
    maps, `largestWindow`.
- **`RetentionRunTest`:** the clamp (furthest cursor reported), the release at 2 × window, a
  throwing consumer, and the accumulators.
- **`RetentionJobIT`:**
  - a track leaf kept before the window and dropped after it, with the floor equal to the next
    leaf and the stale head purged;
  - 10,050-row chunked deletes;
  - a 365d kind surviving a 7d source window;
  - an observed head surviving the deletion of its version;
  - reappearance at version 1 with `FIRST`;
  - a failed delete (a temporary `BEFORE DELETE` trigger) skipping the source with no floor and no
    purge, while other sources proceed;
  - a 304 streak keeping the head;
  - `fetch_log` pruning at 7 and 30 days;
  - `seq_clock` pruning;
  - a concurrent run getting 409.
- **`ConsumerClampIT`:** the clamp holds the cutoff, then releases after 2 × window with the counter
  and the `system.retention_consumer_lag` notification; `trigger.cursor` is registered.
- **`PurgeSeqIT`:**
  - a version delete alone leaves `purgeSeq` at 0;
  - a head purge sets it to the watermark;
  - `/v1/changes` answers 410 below it and 200 at it;
  - a fresh store reloads the state from the table.
- **`RetentionControllerIT`:** defaults; the `sources` round trip through `source_settings` and the
  clear on the next PUT; every validation error in one 400; `RETENTION_TIER_FIXED` for a TRACK
  source and a TRACK-only kind; the run endpoint; 409 while running.
- **`SettingsExportImportIT`:**
  - an export with every section and no secrets or credentials;
  - a round trip with triggers and channels upserted by id and a channel secret kept;
  - unknown sources;
  - a credential-less enable downgraded with a warning;
  - an atomic failure (code, every error, nothing applied).
- **`SystemControllerIT`:** the anonymous subset; an invalid token treated as anonymous; the full
  document with history per tier; the operator gauges; `OPEN_READS_EXPOSED` on and off.
- **`SystemWarningsTest`:** every warning condition, and the local and public address cases.
- **`HealthProbesIT`** (mocked `db`, writer and catch-up): liveness UP with the database DOWN;
  readiness DOWN on the database or the writer; `catchup` independent of readiness.
  **`HealthIndicatorsTest`** covers every writer state and the partition headroom states.
- **`WatchdogIT`** (WireMock, `MutableClock`): the heartbeat once per interval; the fail URL while
  not ready; POST without following redirects; the canary succeeding with its latency recorded;
  nothing pinged when unconfigured. **`WatchdogServiceTest`** covers a canary failure choosing the
  fail URL, and **`CanaryServiceTest`** covers success, failure and a batch from another source.
- **`AdminTokenRotationIT`, `CorsControllerIT`, `LogFormatEnvironmentPostProcessorTest`,
  `WatchdogSettingsServiceTest`, `HeartbeatPingerTest`.**

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 2s
```

## Notes for the next prompt

1. **020 (pending operations):** every 019 operation is implemented; do not list them.
2. **022 (checkpoint):**
   - The 019 ITs share one new context (`RetentionTestSources`); `HealthProbesIT` adds one more
     (`@MockitoBean`).
   - Issue 1's guard failure predates 019.
   - `RetentionJobIT`'s 10,050-row test and its head purge are the slowest new tests.
3. **023:**
   - `getSystem` treats any valid token as full access; set the minimum to `read` there.
   - Add `security` and `server` to `SettingsTransferService` (export and `HANDLED`), and to the
     import.
   - `TokenFilter` and `AdminTokenService` comments now point at 023 for `api_token`.
4. **027, 028, 029, 023 (audit):** add a `RetentionStep` bean with an order between 700 and 900.
   Register cursors as `RetentionConsumer` beans: 027's embedding cursor, and the intelligence
   block's durable cursors.
5. **033:** the `notifications` window deletes `notification` rows with no delivery left; the inbox
   has its own `inbox` window.
6. **035:** apply the outbound URL policy in `HeartbeatPinger` (watchdog URLs).
7. **044:** `trackRawWindow` and `trackDownsampleInterval` are accepted by `updateRetention` and not
   stored; store them there, and register the downsampling job.
8. **Later feature flags:** declare a `FeatureContributor` bean (`llm`, `search`, `push`, `mcp`,
   `weather`, …).
9. **Owner decisions pending:** Issues 1 and 4, plus those carried from 012–018.
