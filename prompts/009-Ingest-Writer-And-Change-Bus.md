# 009 — Ingest Writer And Change Bus

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/009-CR-Ingest-Writer-And-Change-Bus.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/008-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §1 (all invariants), §4 (lazy recipe migration), §6 (writer: lifecycle, state, clock guard, **batch steps 1–15**, retry, HeadPurge, IngestOutcome, metrics), §8 (`seq_clock`, current payload), §10.1 (ChangeBus, LiveBus), §17 (`TestRecords`), and the **"007b Ingest Writer And Change Bus"** bullets under *Per-prompt changes* (007b is this prompt)
- `docs/Architecture.md` §6.1 (why a single writer), §8.1 (with the v1.2 note: seq is monotonic in commit order, not gap-free)
- Code from prompts 005–008: `UsageMeter.recordIngest`/`IngestCounts`, `PartitionManager`, `IngestStateReset`, the record entities, and everything in `ai.foresignal.ingest` (Normalizer, ContentHasher, IngestPolicyEngine, EntityState, SourceIngestProfiles, PersistGate)

## Objective

Implement the single ingest writer that turns `IngestCommand`s into versions, head and live rows, touches and `seq_clock` entries in one transaction per batch, with seq monotonic in commit order (I2), and that announces each committed batch on the ChangeBus (persisted versions) and LiveBus (live updates and touches). Correctness beats cleverness: this is the heart of the broker.

## Tasks

### 1. `IngestQueue` (`ai.foresignal.ingest`)

- Bounded `ArrayBlockingQueue<IngestCommand>`, capacity 64 (`foresignal.ingest.queue-capacity`).
- `CompletableFuture<IngestOutcome> submit(String sourceId, FetchLogEntry log, List<IngestRecord> records, Duration timeout)` blocks up to the timeout, then fails with `IngestBackpressureException`. `submitPurge(List<PurgeSpec>)` for prompt 019.
- Gauge `foresignal.ingest.queue.depth`.

### 2. `IngestWriter` lifecycle and state (§6.1)

- Platform thread `foresignal-ingest-writer`, started by an `ApplicationReadyEvent` listener ordered by a new `ai.foresignal.config.StartupOrder` (`WRITER = 10`, `TRIGGER_ENGINE = 20`, `SCHEDULER = 30`; prompts 010 and 017 use it). Shutdown drains the queue for up to 30 s. The loop body catches everything: **the thread never dies.**
- **Lock** when `foresignal.ingest.writer.exclusive=true` (production default; false in tests): open a dedicated connection with `DriverManager` from `JdbcConnectionDetails` (outside Hikari), `SELECT pg_try_advisory_lock(hashtext('foresignal.ingest.writer'))`. If false, fail startup: "another Foresignal instance holds the ingest writer lock on this database". Keepalive `SELECT 1` every 30 s; on connection loss pause command processing, reacquire with backoff (1 s doubling to 30 s), count `foresignal.ingest.writer.lock_lost` and raise a health warning.
- **`resetState()`** (implements `IngestStateReset`; serialised with batch processing by a lock): `lastSeq, lastIngestedAt` from `SELECT seq, ingested_at FROM record_version ORDER BY seq DESC LIMIT 1`; raise `lastIngestedAt` to `max(it, max(seq_clock.minute), max(fetch_log.ingested_at))`; `lastAllocatedSeq` from `SELECT last_value FROM record_seq`; clear the migrated-recipe map; reset `ChangeBus` to `lastSeq`. Called at start and by `DatabaseCleaner`.
- **Clock guard:** at start (and after `resetState`), if `clock.now < lastIngestedAt − 5 min`, hold all batches (queue fills, producers back off), set status `CLOCK_BEHIND` and re-check every 30 s. Per batch, compare the wall-clock delta with the `System.nanoTime` delta since the previous batch; a divergence above 5 min counts `foresignal.ingest.clock.jump{direction}` and raises a warning. Backward skew is absorbed by `ingestedAt = max(clock.now, lastIngestedAt)` and counted as `foresignal.ingest.clock.clamped`; ingest never stops for it.
- `IngestWriter.status()` → `WriterStatus(state RUNNING|CLOCK_BEHIND|LOCK_LOST|STOPPED, warnings, lastSeq, lastIngestedAt, queueDepth)` for `/v1/system` (prompt 019); gauge `foresignal.ingest.writer.state`.
- Use the injected `Clock` everywhere. Profiles come from `SourceIngestProfiles`; read the gate snapshot **once** at batch start.

### 3. Batch processing (§6.2, implement steps 1–15 exactly)

1. **Pre-flight:** `tier = profile.tier`; if `lastAllocatedSeq + n ≥ partitionManager.coveredUpTo(tier, lastAllocatedSeq + 1)`, call `ensureFor(tier, lastAllocatedSeq + n)` inline and fail the batch only if still uncovered; under one block of headroom, request an async `ensureAhead` and warn.
2. **Normalize** with `Normalizer` (keeps duplicates). 3. Stable sort (done by the normalizer).
4. **Recipe check** when the source is not in the migrated map: `SELECT 1 FROM record_head WHERE source_id=? AND hash_recipe<>? LIMIT 1`; if found, migrate in chunks of 2,000 in **separate** transactions before the batch: recompute `h.static_hash` from the head columns (kind, event_time, valid_until_explicit, valid_from, tombstone, `h.payload`, `h.persisted_geom IS NULL`), `l.kinematic` and `l.full_hash` from the merged payload `jsonb_strip_nulls(h.payload || l.kinematic)` and `l.geom`, set `h.hash_recipe`; leave `dirty` unchanged.
5. **Begin** (`TransactionTemplate`; `SET LOCAL lock_timeout` = `foresignal.ingest.writer.lock-timeout`, default 10s). `ingestedAt = max(clock.now, lastIngestedAt)`.
6. **Load state** with the single §6.2 query (`record_head h LEFT JOIN record_live l USING (source_id, external_id) WHERE h.source_id=? AND h.external_id = ANY(?)`), selecting the listed head and live columns, `ST_AsBinary(l.geom)` for TRACK only, one `h.payload #> ?::text[]` column per `profile.loadPaths` entry, and `ST_AsBinary(h.persisted_geom)` when the gate needs geometry. A head without a live row is repaired (live row rebuilt from the head, counted `foresignal.ingest.repair.live_missing`). Fold each observation through `prepare` → gate → `decide`, applying the `EntityState` transition after each outcome.
7. **Allocate** `n` = number of PERSIST outcomes: `SELECT nextval('record_seq') FROM generate_series(1, ?)`; sort in Java; set `lastAllocatedSeq = max` immediately (even if the batch later rolls back, I2); assign in fold order; `version = running.version + 1`, `prev_seq = running.seq`.
8. `INSERT INTO fetch_log (…) VALUES (…) RETURNING id` → `batch_id`, with counts, `ingested_at`, `from_seq`, `to_seq`.
9. **Versions:** `JdbcTemplate.batchUpdate` with the full column list; geometry `ST_GeomFromWKB(?, 4326)`, jsonb `?::jsonb`, `history_tier` from the profile, `persist_reason` code, `qa_flags`, `valid_until` (effective) and `valid_until_explicit`.
10. **Head upsert:** `INSERT … ON CONFLICT (source_id, external_id) DO UPDATE SET … = EXCLUDED.…`, one row per entity with ≥ 1 PERSIST (assert unique keys before calling; a duplicate is an `IllegalStateException`): kind, seq, version, event_time, valid_until_explicit, valid_from, tombstone, static_hash, hash_recipe, persisted_observed_at, persisted_received_at, persisted_geom, payload, quality, search_text, title.
11. **Live upsert:** one row per entity with ≥ 1 PERSIST or LIVE, carrying the final running live state (`kinematic`, `dirty`, `full_hash`, `geom`, `valid_until`, `quality`, `qa_flags`); `last_received_at` = the entity's maximum `received_at` among non-LATE observations in the batch; tombstones get `geom NULL`, `kinematic {}`.
12. **Touch:** for entities whose only outcomes are TOUCH (others fold into step 11), one row per key with the latest touch: `UPDATE record_live SET observed_at=?, last_received_at=?, valid_until=? WHERE source_id=? AND external_id=? AND observed_at <= ?`; `valid_until` = explicit, else `anchor + ttl`. The driver does not rewrite updates, so the summed update counts must equal the row count, or the batch fails.
13. `INSERT INTO seq_clock(minute, first_seq) VALUES (date_trunc('minute', ?::timestamptz), ?) ON CONFLICT DO NOTHING` with `lastAllocatedSeq` (before step 7) `+ 1`, on **every** batch, no cache.
14. **Commit;** advance `lastSeq` and `lastIngestedAt`.
15. **Publish** `ChangeBatch` (when n > 0), then `LiveBatch` (when there are live updates or touches), then complete the future, then `UsageMeter.recordIngest(sourceId, IngestCounts)` and `EntityKeyListener` beans for persisted entities with keys.

- **Retry:** on SQLState `08*`, `40*`, `55P03` or `57014`, roll back and retry the whole batch (steps 5–14, re-folding from fresh state) up to 3 times with 0.5, 1 and 2 s backoff (`foresignal.ingest.batch.retries`). Other errors, or exhausted retries: fail the future exceptionally, log with the source id, continue.
- **`IngestOutcome`:** the §6 counts; `version_bytes` = UTF-8 length of payload JSON + quality JSON + observer JSON + 120 per version.
- **Metrics:** `foresignal.ingest.records.{seen,persisted,live,touched,touches_throttled,late,skipped,clamped,rejected}{source}`, `foresignal.ingest.persist{source,reason}`, `foresignal.ingest.batch.duration{phase}` (normalize, load, fold, write, publish).
- Class Javadoc: why commit order equals seq order with one writer (Architecture §6.1), and that gaps from rolled-back allocations are expected and harmless.

### 4. `HeadPurge` (§6)

For each `PurgeSpec`, in chunks of 10,000, in one transaction per chunk: `SELECT h.external_id FROM record_head h JOIN record_live l USING (source_id, external_id) WHERE h.source_id=? AND h.seq < :floorSeq AND l.last_received_at < :staleBefore LIMIT 10000`, then `DELETE FROM record_live …` and `DELETE FROM record_head … WHERE external_id = ANY(?)`. If more remain, re-enqueue the remainder at the queue tail with a non-blocking `offer` (if the queue is full, continue inline) so batches interleave. Complete the future with `PurgeOutcome` when all specs are done.

### 5. ChangeBus and LiveBus (`ai.foresignal.stream`, §10.1)

- `ChangeBatch(sourceId, batchId, ingestedAt, fromSeq, toSeq, List<ChangeSummary>)`; `ChangeSummary(seq, prevSeq, historyTier, sourceId, externalId, kind, version, persistReason, observedAt, eventTime, validUntil, tombstone, qaFlags, bbox, prevPersistedBbox, prevLiveBbox)`. The prev boxes come from the running state just before that version (`h.persisted_geom`, `l.geom`), so several versions of one entity in a batch chain correctly.
- `ChangeBus.subscribe(name, ChangeListener, queueCapacity)` → `Subscription`; `ChangeListener { onBatch(ChangeBatch); onGap(long lastDeliveredSeq); }`. Each subscriber has its own bounded queue and **virtual** delivery thread; `publish` never blocks. On overflow drop the batch, mark the subscriber lagging, and once the queue drains call `onGap(lastDeliveredSeq)` once so it catches up from the database. `lastPublishedSeq()` (initialised from `SELECT coalesce(max(seq),0) FROM record_version`; read by prompt 014 as `watermarkSeq`) and `resetWatermark(long)`. Subscribers later: SseHub (015), TriggerEngine (017), EmbeddingWorker (027).
- `LiveBatch(sourceId, List<LiveUpdate>, List<Touch>)`: `LiveUpdate(sourceId, externalId, kind, observedAt, validUntil, version, geometry, bbox, prevBbox, properties, quality)` where `properties` is the kinematic delta (JSON null = key removed; a LIVE outcome never changes significant keys), one per entity per batch; `Touch(sourceId, externalId, observedAt, validUntil, bbox)`. `LiveBus.subscribe(name, LiveListener, capacity)` with bounded queues; on overflow drop the batch, count `foresignal.stream.live.dropped{subscriber}` and call `onLiveDropped()`. **Change is always published before live.**

### 6. `TestRecords` (`src/test/java/ai/foresignal/testsupport/ingest`)

Seeds data only through `IngestWriter`: `ingest(sourceId, NormalizedRecord…)` and `ingestAt(sourceId, Instant receivedAt, NormalizedRecord…)` submit an `IngestBatch` with a synthetic `FetchLogEntry` (MANUAL, OK) and return the `IngestOutcome`. Register test sources alongside prompt 004's fakes so all ITs share one context: `test-standard` (ALL, STANDARD, no TTL), `test-ttl` (ALL, STANDARD, TTL 10 min, RECEIVED anchor), `test-track` (SAMPLED 60 s / keyframe 240 s / 1,000 m, significant `callsign, squawk`, thresholds `alt` 1,000 and angular `track` 20°, motion `gs` knots + `track`, TRACK, TTL 5 min), `test-latest` (LATEST, significant `name`), and a PUSH-mode `test-dynamic` that tests re-register with different `volatileFields`/`significantFields`. From prompt 011 on, ITs must not seed `record_version`, `record_head` or `record_live` with raw SQL.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json` `IngestOutcome`: add `persisted`, `live`, `touched`, `touchesThrottled`, `late`, `skipped`, `byReason` (object keyed by persist reason), keeping `changed` as an alias of `persisted`. Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`).
- No new endpoints.

## Tests to write (do not run)

`IngestWriterIT` (`@IntegrationTest`, `MutableClock`, `TestRecords`), one test each:
- 5 observations of one entity in one batch under ALL → 5 versions; head and live equal the 5th; seq and version chain via `prev_seq`.
- LATE has no side effects (no version, live unchanged, counted); clamp sets `FUTURE_CLAMPED` beyond skew only.
- TTL re-observation is a touch; a USGS-style fixed `observed_at` refreshes `last_received_at` once `touchMin` has passed, and is TOUCH_THROTTLED before.
- HEARTBEAT vs KEYFRAME; retention keyframe at `window/4`; SAMPLED deviation and threshold; LATEST.
- TRACK null geometry carries forward with `POSITION_STALE` and no SIGNIFICANT.
- LIVE → PERSIST → LIVE keeps `jsonb_strip_nulls(h.payload || l.kinematic)` correct when a key drops.
- Adding `volatileFields` → 0 versions on re-fetch; adding a significant field → no version storm (lazy migration, `hash_recipe` updated).
- HeadPurge respects `floorSeq` and `staleBefore`, and chunks re-enqueue.
- A second writer with `exclusive=true` refuses while the first holds the lock.
- Restart with an earlier clock waits (`CLOCK_BEHIND`), then proceeds when the clock catches up.
- `resetState` after truncate restarts state and the ChangeBus watermark.
- Transient retry: another session holds `LOCK TABLE record_live`, lock timeout 500 ms → `55P03`, retry counter ≥ 1 (Awaitility), release, the batch succeeds.
- A NOT NULL failure (a `FetchLogEntry` with a null trigger) fails only that batch; the next succeeds.
- Change is published before live; tombstone cases (existing head, missing head → SKIP, already tombstoned → SKIP); seq strictly increasing across concurrent submitters, checked from a bus subscriber; geometry stored with SRID 4326; `seq_clock` row on a batch with zero versions; a missing live row is repaired.

Also `IngestQueueTest` (back-pressure timeout), `ChangeBusTest` (fan-out, slow subscriber isolation, overflow → one `onGap` with the right seq, unsubscribe), `LiveBusTest` (overflow → `onLiveDropped`).

## Acceptance criteria

- Only the writer thread inserts `record_version` and writes `record_head`, `record_live` and `seq_clock` (I1); `record_version` is changed only by native SQL (I9).
- Every head has exactly one live row after any batch (I10).
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `009: Ingest Writer And Change Bus`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/009-CR-Ingest-Writer-And-Change-Bus.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `009: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
