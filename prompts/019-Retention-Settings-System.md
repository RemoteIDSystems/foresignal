# 019 — Retention Settings System

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/019-CR-Retention-Settings-System.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/018-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §13 (retention, authoritative), §1 (I1, I6, I7), §8 (`seq_clock`, `fetch_log`), §9 (`PartitionManager`), §10.2 (resume floor), §15 (storage visibility, `/v1/system.history`), §16 (binds, NTP), and *Per-prompt changes* → "016 Retention Settings System" (= 019)
- `docs/Weather-Imagery-Design.md` §2.4 ("Per-kind record retention"; row 016 of §10.1 = 019)
- `docs/Gap-Analysis.md` §6 (readiness, watchdog, capabilities, OpenTelemetry rows) and §8.A item 9 ("extend 016" = 019)
- `docs/Architecture.md` §11, §7.2 (admin token rotation); `docs/Product-Charter.md` FR-17 (as amended by Core-Data-Model §13), FR-30, FR-32–FR-34, NFR-11
- `docs/OpenAPI.json`: operations tagged `Settings` and `System`; schemas `RetentionSettings`, `RetentionRunResult`, `CorsSettings`, `SettingsBundle`, `ImportReport`, `SystemInfo`, `AdminTokenResponse`
- Completion Reports of 007 (`PartitionManager.drop`), 009 (`HeadPurge`/`PurgeSpec`), 014 (purge horizon and `410 RESYNC_REQUIRED`), 015 (reset floor), 017 (alert lifecycle)

## Objective

Finish the operator surface: tier-aware retention that drops whole track partitions and chunk-deletes standard history without breaking I7, the retention state that SSE and the records API need for `reset` and `410`, CORS, export/import, a capability-rich `/v1/system`, readiness probes and an off-box watchdog. Everything is configurable through the API.

> **v1.3 note:** adapters set retention defaults by analytic class through `defaultRetention` / `kindRetention` (`docs/Conventions.md` "Analytic retention"); the resolver order below is unchanged. Tables added by the intelligence block (116–128) are never touched by `RetentionJob` unless their prompt says so, and their durable cursors join the consumer clamp registry built here, so make that registry open for later cursors.

## Tasks

### 1. Retention settings and window resolution (`ai.foresignal.retention`)

- Extend `ai.foresignal.settings.RetentionSettings`: `default` (7d), `trackWindow` (3d), `provenance` (30d), `alerts` (90d), `usage` (30d), `notifications` (90d), `briefings` (180d), `ask` (90d), `kinds` (map kind → duration), `sources` (map id → duration). Keep the shorthand serialisation.
- `getRetention` / `updateRetention` (PUT replaces the whole object). Validation, all errors collected: every duration ≥ 1h; unknown source ids and unknown kinds listed in one 400; a `sources` entry for a TRACK source, or a `kinds` entry for a kind only TRACK sources emit, → `400 RETENTION_TIER_FIXED`.
- **One store for per-source overrides:** `retention.sources` is a view over `source_settings.retention` (004). PUT writes those rows in the same transaction; GET reads them. `SourceService.effectiveRetention(id)` delegates to `RetentionResolver`.
- **`RetentionResolver.window(source, kind)`:** TRACK → `trackWindow` (always). STANDARD, in order: (1) `retention.kinds.{kind}`, (2) the per-source override, (3) descriptor `kindRetention[kind]`, (4) descriptor `defaultRetention`, (5) `retention.default`.
- `RetentionResolver.effectiveKeyframe(source)` = `min(policy keyframe, window/4)` and `horizon(source) = now − window + effectiveKeyframe + 1h` (Core §12), used by 014's asOf. If 014 computed these inline, move them here and point 014 at the resolver.

### 2. RetentionJob (per Core-Data-Model §13; not `@Transactional`)

Hourly `@Scheduled`, initial delay 5 min, disabled in the test profile; `runRetention` (POST) runs it synchronously. A second run while one is active → `409 ALREADY_RUNNING`. Structure it as ordered `RetentionStep` beans so later prompts add steps (027 embeddings, 029 briefings, 028 Ask threads, 023 audit).

- **Cutoffs:** `cutoffSeq(t)` from `seq_clock` (Core §8). **Consumer clamp:** clamp every cutoff to `min(consumer cursors) + 1`. Consumers register through a `RetentionConsumer` SPI (`name()`, `cursorSeq()`); register the trigger cursor (`trigger.cursor`) now; 027 adds the embedding cursor. If a cursor is older than `now − 2 × window`, release the clamp for that run, increment `foresignal.retention.consumer_lag` and publish a `system` notification (018).
1. **TRACK:** `PartitionManager.drop` for every `rv_t_*` leaf whose upper bound ≤ `cutoffSeq(now − trackWindow)`. `floorSeq(track source)` = lower bound of the oldest remaining track leaf (or the next leaf's start when none remain).
2. **STANDARD:** per (source, kind) with its own window, chunked deletes, one transaction per chunk of 10,000, with the outer seq bound:
   ```sql
   DELETE FROM record_version WHERE history_tier = 0 AND seq < :cutoff
     AND seq IN (SELECT seq FROM record_version WHERE history_tier = 0 AND source_id = ? AND kind = ? AND seq < :cutoff LIMIT 10000)
   ```
   Omit the kind predicate when a source has a single window. On completion, `floorSeq(source, kind) = cutoff`; on failure skip that source for this run (no HeadPurge). Rows of source ids no longer registered use `retention.default`.
3. **STANDARD leaf drop** for every `rv_s_*` whose upper bound ≤ `cutoffSeq(now − largest effective standard window)`; those leaves are already empty.
4. **Heads (I7):** pre-check candidates with a read query (`h.seq < floorSeq AND l.last_received_at < staleBefore`), then submit **one** `HeadPurge` command to the writer with a `PurgeSpec(source, kind?, floorSeq, staleBefore)` per source/kind that succeeded and has candidates. `staleBefore = min(now − window, source_health.last_data_at − touchMin)`, so a 304 streak never ages out entities from the last full response. If 009's `PurgeSpec` lacks an optional kind, add it. Retention never writes heads or live rows itself (I1).
5. `fetch_log`: delete rows with `versions_written = 0` older than 7d, other rows older than `provenance`.
6. `seq_clock`: delete minutes older than (largest window + 1d).
7. Other tables: `alert` (`alerts`, by `created_at`), `source_usage` (`usage`), `notification_delivery` in `delivered`/`failed` older than 7d, `notification` (`notifications`).
- **`RetentionState`**: replace 014's default bean (keep its interface: `purgeSeq(sourceId)`, `lowestRetainedSeq(Set<String>)`, `window(sourceId)`) with the real one, published after every run and loaded at startup from a new `retention_state` table (`source_id`, `kind`, `floor_seq`, `purge_seq`, `purged_at`, `last_run_at`):
  - `floorSeq(source)` = min over its kinds. `lowestRetainedSeq(sources)` = the max `floorSeq` over the set (all sources when unfiltered): a replay from `after` is complete only when `after ≥` that value. 015 sends `reset` below it.
  - **Purge horizon (014's `410 RESYNC_REQUIRED`):** `purgeSeq(source)` advances only when a HeadPurge removes ≥ 1 head of the source: record `ChangeBus.lastPublishedSeq()` at completion (use the command's completion future; add one to `HeadPurge` if 009 did not). A head purge emits no change event, so any client whose cursor is below it may still hold a deleted head. Deleting old versions does **not** advance `purgeSeq` (a cursor above the floor missed nothing; `lowestRetainedSeq` covers it); correct 014's Javadoc if it says otherwise, or every hourly run would force all clients to resync.
  - `window(sourceId)` delegates to `RetentionResolver`.
- **Concurrency (document in the package Javadoc):** only the writer inserts versions and writes heads and live rows (I1); head purges run on the writer. Retention deletes only versions below a cutoff taken from the past, and drops leaves through `PartitionManager`'s lock. Observed entities keep a version newer than window/4 via the retention keyframe (I6), so their heads are never purge candidates. A purged entity that reappears starts again at version 1 with reason `FIRST`, and no older versions survive (I7). FR-17 as amended: current state is kept while current.
- `RetentionRunResult` gains `partitionsDropped`, `headsPurged`, `fetchLogDeleted`, `seqClockDeleted`, `notificationsDeleted`, `skippedSources`, `consumerClamp` (`{active, consumer, cursorSeq}`), `perSource` (versions deleted, floorSeq). Metrics `foresignal.retention.deleted{table}`, `foresignal.retention.duration`; one summary log line per run.

### 3. CORS and export/import (`ai.foresignal.settings`)

- `getCors` / `updateCors` via `CorsSettingsService` (003).
- `exportSettings` → `SettingsBundle`: source settings (no credentials), retention (with `kinds`), CORS, notification settings (018), watchdog, trigger rules (full, with ids), channels (no secrets), `exportedAt`, `foresignalVersion` (from the `ForesignalVersion` bean). 023 adds `security` and `server`.
- `importSettings` (PUT): one transaction; known sources applied, unknown listed in `sourcesUnknown`; an enable whose required credentials are missing is applied as `enabled=false` with a warning; triggers and channels upserted by id (channels without secrets keep existing secrets); retention, CORS, notifications and watchdog applied when present. Any validation error → 400 with all errors and nothing applied.

### 4. System info (`ai.foresignal.system`)

- `getSystem` stays `open`, but **anonymous callers get only** `version`, `apiVersion` (the spec's `info.version`), `startedAt`, `uptimeSeconds`, `status` (`ok|degraded|down`), `features`, `limits`. Callers presenting any valid token get the full document (023 sets the minimum to `read`). State this in the operation description.
- Full document adds to the existing fields: `database` (status, `pg_database_size`, `postgis_lib_version()`), `records` (`current` from `record_head` non-tombstoned, `versions` estimated from `pg_class.reltuples`, `lastSeq`), sources, scheduler, streams, notifications (018 counts), and:
  - **`history`** (Core §15): per tier from `pg_partition_tree('record_version')` leaves: `partitions`, `rows` (Σ reltuples), `bytes` (Σ `pg_total_relation_size`), `headroomSeq`, `windowDays`; totals `versionsPerMinute`, `projectedBytes`.
  - **`catchUp`**: `triggerLagSeq` (lastPublishedSeq − trigger cursor), `ingestQueueDepth`, `catchingUp` (lag > 10,000 seq or queue > 80% for > 1 min).
  - **`features`**: map of booleans contributed by `FeatureContributor` beans (`notifications`, `retention`, later `llm`, `search`, `push`, `mcp`, `weather`…). **`limits`**: page max 5,000, SSE client buffer, `liveMaxEntities`, ingest max bytes and records, tile feature cap.
  - **`warnings[]`** (`{code, message}`): `OPEN_READS_EXPOSED` when `openReads=true` and `publicBaseUrl`'s host or the advertised bind is not loopback, RFC 1918, CGNAT 100.64/10 (tailnets), link-local, ULA or `.local` (reuse `LocalAddresses` from 004); `OPEN_READS_ALL_INTERFACES` (info) when the bind is `0.0.0.0`; `HISTORY_RATE_HIGH` when versions/min > 20,000; `PARTITION_HEADROOM_LOW`; `RETENTION_CONSUMER_LAG`.
  - Advertised bind: compose passes `FORESIGNAL_BIND` into the container; add bootstrap property `foresignal.advertised-bind` (`${FORESIGNAL_BIND:127.0.0.1}`).
- `rotateAdminToken` (admin) returns `AdminTokenResponse` exactly once; the old token stops working immediately.

### 5. Probes, gauges, watchdog, logging

- **Health groups:** `management.endpoint.health.probes.enabled=true`; `liveness` = `livenessState` only (never the database, to avoid restart loops); `readiness` = `readinessState`, `db`, `ingestWriter` (lock held when exclusive, state loaded, clock guard not holding), `partitions` (headroom ≥ 1 block per tier). Add a separate `catchup` group with a `CatchUpHealthIndicator` that reports `OUT_OF_SERVICE` while `catchingUp`; it is **not** part of readiness. Details stay hidden (`show-details: never`).
- **Compose healthcheck for `foresignal`:** install `curl` in the runtime image (`apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*`) and add `test: ["CMD","curl","-fsS","http://localhost:8080/actuator/health/readiness"]`, interval 15s, start_period 120s.
- **Gauges** (refreshed every 30 s from cached queries, not per scrape): `foresignal.source.data_age_seconds{source}` (now − `last_data_at`), `foresignal.alerts.active{severity}` (unresolved alerts, 017 lifecycle), `foresignal.records.current`, `foresignal.sse.clients{stream}`, `foresignal.history.rows{tier}`.
- **Watchdog** (`app_setting['watchdog']`, `GET/PUT /v1/settings/watchdog`): `heartbeatUrl`, optional `failUrl`, `interval` (default 60s, min 30s), `method` (GET|POST). Ping `heartbeatUrl` each interval only while readiness is UP (and the canary passes when enabled); otherwise ping `failUrl` if set. Works with Healthchecks.io and Uptime Kuma push monitors. 5 s timeout, no redirects, URL redacted in logs; 035 retrofits the outbound URL policy. **Opt-in canary** (`canary.enabled`, `canary.interval` default 1h): submit one synthetic record for a built-in PUSH source `system-canary` (STANDARD, `defaultRetention` 1d, hidden from tiles), wait ≤ 30 s for its change on `ChangeBus`, record `foresignal.canary.latency`, and publish a `system` notification on failure.
- **Logging:** bootstrap env `FORESIGNAL_LOG_FORMAT` (`ecs` default in the `docker` profile, `text` otherwise) maps to `logging.structured.format.console`. Add Micrometer Tracing with the OpenTelemetry bridge and **no exporter** (export off by default) so `traceId`/`spanId` appear in the MDC and ECS logs next to the correlation id. Verify the Boot 4.1 property names and versions on Maven Central.
- Review metric names for consistency and list them all in the CR.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `RetentionSettings` (+`trackWindow`, `provenance`, `notifications`, `kinds`), `RetentionRunResult` additions, `SystemInfo` (+`apiVersion`, `status`, `features`, `limits`, `history`, `catchUp`, `warnings`; describe the anonymous subset), new `WatchdogSettings` with `getWatchdogSettings`/`updateWatchdogSettings` (`"x-foresignal-scope": "admin"`, covered by `/v1/settings/**`), `SettingsBundle` (+`notifications`, `watchdog`), error codes `RETENTION_TIER_FIXED`, `ALREADY_RUNNING`. Regenerate Postman; add BODIES for `updateRetention` (with `kinds`) and `updateWatchdogSettings`.
- `docs/Operator-Guide.md`: retention (tiers, windows, kind resolution order, FR-17 amendment), probes and the compose healthcheck, watchdog and canary, `/v1/system` warnings, log formats.

## Tests to write (do not run)

- `RetentionResolverTest`: the five-step order, TRACK always `trackWindow`, horizon math.
- `RetentionJobIT`: a track leaf dropped only past the window (seed with `TestPartitions` and `TestRecords`); standard chunked deletes (> 10k rows); per-kind windows (a 365d kind survives a 7d source window); a head survives a drop of its referenced version while observed; reappearance after a purge restarts at version 1 with no older versions; failed deletes → no HeadPurge for that source; 304 streak → no purge; consumer clamp holds the cutoff and releases after 2 × window with the alarm; `fetch_log` and `seq_clock` pruning; `purgeSeq` advances only when heads were purged, never on version deletes alone; `RetentionState` reloads after restart; concurrent run → 409.
- `RetentionControllerIT`: validation (unknown source/kind, < 1h, TRACK override → `RETENTION_TIER_FIXED`); `sources` round trip through `source_settings`; run endpoint.
- `SettingsExportImportIT`: round trip; no credentials or secrets; unknown sources; credential-less enable downgraded; atomic failure.
- `SystemControllerIT`: anonymous subset vs full document with a token; `history` per tier; each warning condition; `features`/`limits` present.
- `HealthProbesIT`: liveness UP with the DB stopped-equivalent (indicator mocked), readiness DOWN without writer state, `catchup` group independent of readiness.
- `WatchdogIT` (WireMock, `MutableClock`): heartbeat while ready, fail URL when not ready, canary success and failure.
- `AdminTokenRotationIT`, `CorsControllerIT`.

## Acceptance criteria

- `Settings` and `System` operations match `docs/OpenAPI.json`.
- TRACK history is removed only by partition drops; no head is purged while any of its versions remain (I7).
- `RetentionState` serves 014's `410` and 015's `reset`.
- No configuration requires editing files beyond bootstrap env vars.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 129, 132, 133, 134).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `019: Retention Settings System`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/019-CR-Retention-Settings-System.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `019: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
