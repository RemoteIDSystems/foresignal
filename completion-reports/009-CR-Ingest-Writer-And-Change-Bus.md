# 009-CR-Ingest-Writer-And-Change-Bus

| | |
|---|---|
| Prompt | 009-Ingest-Writer-And-Change-Bus.md |
| Status | Complete |
| Implementation commit | 53b6f6d377465c519898121ed027682920ef3e5b |
| Date | 2026-10-11 |

## Summary

This prompt builds the single ingest writer (Core-Data-Model §6) on top of 008's pure core, plus the
ChangeBus and LiveBus (§10.1) and the `TestRecords` seeding helper (§17).

**`IngestQueue`** (`ai.foresignal.ingest`):
- a bounded `ArrayBlockingQueue<IngestCommand>` with capacity `foresignal.ingest.queue-capacity` (64)
- `submit(sourceId, log, records, timeout)` blocks for up to the timeout, then throws
  `IngestBackpressureException`
- `submitPurge(specs)` for prompt 019
- a non-blocking `offer` (used by purge re-enqueue) and `drain`
- gauge `foresignal.ingest.queue.depth`

**`IngestWriter`** (`ai.foresignal.ingest`), lifecycle and state:
- **Thread:** one platform thread, `foresignal-ingest-writer`. It starts from an
  `ApplicationReadyEvent` listener ordered by the new `config.StartupOrder` (`WRITER = 10`,
  `TRIGGER_ENGINE = 20`, `SCHEDULER = 30`). The loop catches `Throwable`, so the thread never dies.
- **Shutdown** (`DisposableBean`): drains the queue for up to `shutdownDrain` (30 s), stops the
  thread, fails any commands left over and releases the lock.
- **Lock (`IngestWriterLock`):** used when `foresignal.ingest.writer.exclusive=true`.
  - opens a dedicated `DriverManager` connection from `JdbcConnectionDetails`, outside Hikari
  - runs `pg_try_advisory_lock(hashtext('foresignal.ingest.writer'))`; when the lock is held,
    startup fails with "another Foresignal instance holds the ingest writer lock on this database"
  - checks the connection with a `SELECT 1` keepalive every 30 s
  - on connection loss: state `LOCK_LOST`, counter `foresignal.ingest.writer.lock_lost`, a warning,
    and reacquire attempts with backoff from 1 s doubling to 30 s, followed by `resetState()`
- **`resetState()`** (implements `IngestStateReset`, serialized with batches by a `ReentrantLock`):
  - reads `lastSeq` and `lastIngestedAt` from the newest version
  - raises `lastIngestedAt` to `max(seq_clock.minute)` and `max(fetch_log.ingested_at)`
  - reads `lastAllocatedSeq` from `record_seq`
  - clears the migrated-recipe map
  - resets the ChangeBus watermark
  - re-runs the clock guard
- **Clock guard** (pure `ClockGuard`):
  - when `now < lastIngestedAt − 5 min`, the writer goes to `CLOCK_BEHIND`, holds batches and
    re-checks every 30 s
  - per batch, a wall-clock versus `System.nanoTime` divergence above 5 min counts
    `foresignal.ingest.clock.jump{direction}` and raises a warning
  - a backward step is clamped by `ingestedAt = max(now, lastIngestedAt)` and counted as
    `foresignal.ingest.clock.clamped`
- **Status:** `status()` returns `WriterStatus(state, warnings, lastSeq, lastIngestedAt,
  queueDepth)`. Gauge `foresignal.ingest.writer.state`.

**Batch steps 1–15:**
1. Pre-flight partition coverage. Calls `ensureRange` inline and `refresh()` as a fallback. Fails
   only when the range is still uncovered. Starts an asynchronous `ensureAhead` with a warning when
   headroom is under one block.
2. Normalize, and 3. sort, both through 008's `Normalizer`.
4. Lazy recipe migration (`RecipeMigrator`): chunks of 2,000 heads, each in its own transaction.
5. A `TransactionTemplate` transaction with `set_config('lock_timeout', …, true)`.
6. The single load query: `record_head ⋈ LEFT JOIN record_live`, plus:
   - `ST_AsBinary(l.geom)` for TRACK only
   - one `(h.payload #> ?::text[])::text` per load path
   - `ST_AsBinary(h.persisted_geom)` when the gate needs geometry
   - a missing live row is rebuilt from its head inside the transaction and counted as
     `foresignal.ingest.repair.live_missing`

   Then the pure `BatchFold` runs prepare → gateFlip → decide → apply per observation, using
   provisional seqs.
7. `nextval` × n, sorted in Java. `lastAllocatedSeq` is set immediately.
8. `fetch_log … RETURNING id`.
9. Versions, through `batchUpdate` with the full `HistoryDdl` column order, `ST_GeomFromWKB(?,
   4326)` and `?::jsonb`.
10. Head upsert with unique keys asserted.
11. Live upsert.
12. Touches, with a check that the summed update count matches.
13. `seq_clock` on every batch.
14. Commit, then advance state.
15. Publish the ChangeBatch, then the LiveBatch, then complete the future, then
    `UsageMeter.recordIngest`, then the `EntityKeyListener` beans.

**Retries and failures:**
- SQLState `08*`, `40*`, `55P03` and `57014` are retried with 0.5, 1 and 2 s backoff, and each
  retry counts `foresignal.ingest.batch.retries`.
- Any other error fails only that batch's future.

**Metrics:**
- `records.{seen,persisted,live,touched,touches_throttled,late,skipped,clamped,rejected}{source}`
- `persist{source,reason}`
- `batch.duration{phase}` (normalize, load, fold, write, publish)
- `records.valid_until_ignored{source}`

**HeadPurge:**
- deletes the live row, then the head, in one transaction per chunk of 10,000
- when more remain, it re-enqueues the remainder at the queue tail with a non-blocking `offer`, or
  continues inline if the queue is full
- completes with `PurgeOutcome`

**Buses (`ai.foresignal.stream`):**
- Types: `Bbox`, `ChangeSummary`, `ChangeBatch`, `ChangeListener`, `Subscription`, `LiveUpdate`,
  `Touch`, `LiveBatch`, `LiveListener`.
- **`ChangeBus`:**
  - each subscriber gets its own bounded queue and virtual delivery thread, and `publish` never
    blocks
  - on overflow it drops the batch, marks the subscriber lagging and drops every batch until the
    queue drains, then calls `onGap(lastDeliveredSeq)` once
  - `lastPublishedSeq()` is initialized lazily from `coalesce(max(seq),0)`; `resetWatermark(long)`
    resets it
- **`LiveBus`:** bounded queues. On overflow it drops the batch, counts
  `foresignal.stream.live.dropped{subscriber}` and calls `onLiveDropped()`.

**Test support:**
- `testsupport/ingest/TestIngestSources` registers `test-standard`, `test-ttl`, `test-track` and
  `test-latest`, plus a runtime-registered PUSH source, `test-dynamic`. It imports `FakeAdapters`
  and `MutableClock.TestConfig`.
- `TestRecords` provides `ingest` and `ingestAt`, which submit a synthetic `MANUAL`/`OK` batch and
  return the `IngestOutcome`.

## Files created / modified

**Created (main)**
- `config/StartupOrder`
- `ingest/`: `IngestQueue`, `IngestBackpressureException`, `IngestProperties`, `IngestWriter`,
  `IngestWriterLock`, `ClockGuard`, `WriterState`, `WriterStatus`, `BatchFold`, `RecipeMigrator`,
  `IngestJson`, `JdbcValues`
- `stream/`: `Bbox`, `ChangeSummary`, `ChangeBatch`, `ChangeListener`, `ChangeBus`, `Subscription`,
  `LiveUpdate`, `Touch`, `LiveBatch`, `LiveListener`, `LiveBus`

**Modified (main)**
- `store/history/PartitionManager`: treats the `42710` create race as a lost race (see Deviation 13)
- `ingest/IngestOutcome`: Javadoc for `live` and `touched` (no shape change)
- `ingest/package-info`: describes the writer
- `application.yml`: `foresignal.ingest.queue-capacity`, `writer.lock-timeout`
- `application-test.yml`: `writer.lock-timeout: 500ms`

**Docs**
- `docs/OpenAPI.json`: `IngestOutcome` descriptions for `byReason`, `live` and `touched`, plus one
  changelog entry. Postman was regenerated and its output was unchanged.
- `docs/Invariants.md`: test names for ING-1, ING-2, ING-3 and ING-5

**Tests (new)**
- `ingest/IngestWriterIT` (24 tests)
- `ingest/IngestQueueTest`
- `ingest/IngestClockGuardTest`
- `stream/ChangeBusTest`
- `stream/LiveBusTest`
- `testsupport/ingest/TestIngestSources`
- `testsupport/ingest/TestRecords`

No dependency changes. No Flyway, Liquibase or SQL files. No new endpoints.

## Deviations from the prompt

1. **`IngestQueue.submit` throws `IngestBackpressureException` synchronously** after the timeout,
   rather than returning an already-failed future. This lets producers back off at the call site.
2. **`foresignal.ingest.batch.retries` is a counter.** Core-Data-Model §6 lists it under Metrics,
   and the prompt's retry test reads it as a counter. The retry count and backoff are configured as
   `foresignal.ingest.writer.retry-backoff` (default `[500ms, 1s, 2s]`), where the list's size is
   the number of retries.
3. **`lastAllocatedSeq` reads `CASE WHEN is_called THEN last_value ELSE last_value - 1 END`** and
   takes the max with `lastSeq`. A plain `last_value` is 1 on a fresh or restarted sequence, which
   would make `seq_clock.first_seq` one too high.
4. **Every observation instant is truncated to microseconds before the fold.** PostgreSQL rounds
   `timestamptz` to microseconds. Without this, a re-observation carrying nanoseconds could compare
   as LATE against its own stored, rounded `observed_at`.
5. **Pre-flight uses `records.size()` as the upper bound on n,** because n is only known after the
   fold. It calls `ensureRange` (007 deviation 5), not `ensureFor`, and falls back to `refresh()`
   (007 issue 1: other contexts hold stale leaf maps). There are two extra safeguards:
   - after allocation, coverage of the actual seqs is checked again and created inline
   - a `23514` "no partition of relation" insert error refreshes the map and is retried
6. **Recipe migration also writes JSON-null masks.** For every key of `h.payload` that is kinematic
   under the new recipe but absent from the current payload, the migrated `kinematic` gets a JSON
   null. Without it, a key the entity had dropped while live would reappear in
   `jsonb_strip_nulls(h.payload || l.kinematic)`.
7. **Live upsert `last_received_at` is `max(batch max non-LATE received_at, running lastReceivedAt)`,**
   so it can never move backwards. Touch-only rows use the running state, which excludes
   `TOUCH_THROTTLED` observations. That exclusion is what keeps the received-axis throttle working
   (§18, first blocker).
8. **`IngestOutcome.live` and `touched` count observations (LIVE and TOUCH outcomes),** not rows.
   `fetch_log.live_updates` and `touches` match. The OpenAPI descriptions were amended (description
   only).
9. **Which entities get which live event:**
   - a `LiveUpdate` goes to each entity whose last content outcome in the batch was LIVE; its
     `properties` is the final kinematic overlay, with nulls for removed keys
   - a `Touch` goes to each entity with touches but no `LiveUpdate`, which includes
     persist-then-touch
10. **`ChangeListener.onGap` is at least once.** While a subscriber is lagging, every batch is
    dropped, which keeps delivery ordered. After the gap, batches published during catch-up may
    overlap what the subscriber reads from the database, so listeners deduplicate by seq. This is
    documented on the interface.
11. **Test sources live in a separate `TestIngestSources` config,** which imports `FakeAdapters`
    and `MutableClock`. Adding them to `FakeAdapters` itself would change the source list that
    `HealthServiceIT` and `SourceServiceIT` assert on. All test sources are PUSH, because
    `test-track`'s 5-min TTL with a 240 s keyframe only validates at the 2 s stream interval:
    `keyframe ≤ ttl − poll − 30 s`.
12. **Test-only hooks:**
    - `IngestWriterIT` is in package `ai.foresignal.ingest`, so it can use the package-private
      `setPurgeChunk` and `sqlState`.
    - The lock, clock-behind and publish-order tests construct extra `IngestWriter` instances with
      their own queues.
    - Publish order is proven with recording bus subclasses. Deliveries on two buses' separate
      threads cannot prove publish order.
    - `application-test.yml` sets `lock-timeout: 500ms`.
13. **Production fix outside scope: `PartitionManager`.** The sanctioned run reproduced a third
    form of the concurrent-create race, `type "rv_t_300" already exists`. I took its SQLState to be
    `42710` from PostgreSQL's source but did not verify it in this session; the next run did not
    hit the race, so the fix is unconfirmed. It is now treated like `42P07` and `23505`, via
    re-check, then attach.
14. **"Health warning" means `WriterStatus.warnings` (a bounded list) plus a log WARN.** No
    actuator `HealthIndicator` was added; `/v1/system` belongs to 019.
15. **Extra metrics:** `foresignal.ingest.purge.requeued` and `foresignal.stream.change.dropped{subscriber}`.
16. **`docs/Invariants.md` test names were updated, as the file requires when a prompt names tests
    differently.** ING-1, ING-2 and ING-5 now name `IngestWriterIT`. ING-3 names
    `IngestClockGuardTest` and `IngestWriterIT`.

## Issues found / open questions

1. **Clock-jump warnings in tests.** Tests that move `MutableClock` between batches log clock-jump
   warnings and count `foresignal.ingest.clock.jump`. This is harmless.
2. **Every cached test context runs its own idle writer thread** (exclusive=false). Each context's
   `DatabaseCleaner` resets only its own writer, which is enough because the others are idle.
3. **`IngestOutcome.clamped` is not in the OpenAPI schema** (008 deviation 1, still open for 013 and
   024).
4. **`fetch_log.licence` / `FetchLogEntry.licence` keep the stored spelling** (a persisted
   identifier). Prose uses "license".
5. **A tombstone's `title` and `search_text` on the head come from the normalized tombstone
   observation.** That is null unless the normalizer derives them.

No instructions were found in data or tool output.

## Test results

Sanctioned narrow run (`docs/Core-Data-Model.md` §17), exactly the listed classes:

```
./gradlew test --tests "*IngestWriterIT" --tests "*ChangeBusTest" --tests "*IngestQueueTest" --tests "*LiveBusTest" --tests "*SchemaParityIT" --tests "*HistorySchemaInitializerIT" --tests "*PartitionManagerIT" --tests "*EntityMappingIT" --tests "*DatabaseCleanerIT"
```

**Final result: 129 tests, 0 failures, 0 errors, 0 skipped. `BUILD SUCCESSFUL`.**

| Class | Tests | Failures |
|---|---|---|
| `IngestWriterIT` (5 nested groups) | 24 | 0 |
| `IngestQueueTest` | 6 | 0 |
| `ChangeBusTest` | 7 | 0 |
| `LiveBusTest` | 2 | 0 |
| `EntityMappingIT` | 45 | 0 |
| `HistorySchemaInitializerIT` | 7 | 0 |
| `PartitionManagerIT` | 20 | 0 |
| `SchemaParityIT` | 6 | 0 |
| `DatabaseCleanerIT` | 12 | 0 |

**Run 1: 129 tests, 2 failed.**
- `IngestWriterIT.lateHasNoSideEffects` had a bad test fixture. The first record's `observed_at`
  (T+10 s) was after its `received_at` (T), so it was clamped to T, and the "late" record was not
  late. I changed the test (it asserted the wrong thing): the first record is now received at
  T+10 s.
- `PartitionManagerIT.concurrentEnsureTwoManagers` hit a concurrent-create race that failed as
  `type … already exists`. Fixed in production code (Deviation 13).

**Run 2: all green.** No other tests were run. After the run, I changed only comments, Javadoc, an
unused import and docs, and recompiled.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
```

## Notes for the next prompt

1. **Submitting batches (010).**
   - Call `IngestQueue.submit(sourceId, FetchLogEntry, List<IngestRecord>, timeout)`.
   - On `IngestBackpressureException`, back off.
   - The future completes after commit with the `IngestOutcome`, or exceptionally when the batch
     failed. Log the failure with the source.
   - `FetchLogEntry.trigger`, `status` and `startedAt` must be non-null; they are NOT NULL columns.
2. **Start order.** The scheduler's start listener should be
   `@EventListener(ApplicationReadyEvent.class) @Order(StartupOrder.SCHEDULER)`.
3. **Stamping records.** The collector stamps `IngestRecord.receivedAt` and a per-source `arrival`
   counter. Instants are truncated to microseconds by the writer anyway.
4. **Writer state (019).** `IngestWriter.status()` gives the writer state for `/v1/system`.
5. **Head purges (019).** Use `IngestQueue.submitPurge(List<PurgeSpec>)`. The future completes with
   heads deleted per source. Purges interleave with batches through re-enqueue.
6. **Bus subscribers (015, 017, 027).**
   - Subscribe with `ChangeBus.subscribe(name, listener, capacity)` and
     `LiveBus.subscribe(name, listener, capacity)`.
   - Deduplicate by seq after `onGap`.
   - Read `ChangeBus.lastPublishedSeq()` as `watermarkSeq` before querying (014).
7. **Seeding in ITs (011 onward).** Import `TestIngestSources` and seed with `TestRecords`
   (`ingest` / `ingestAt`). The import puts all ingest ITs in one shared context with a
   `MutableClock`. Raw SQL seeding of `record_version`, `record_head` and `record_live` is no longer
   allowed.
8. **Trigger gate (017).** `TriggerPersistGate` replaces `NoopPersistGate` as `@Primary`. The writer
   reads `snapshot()` once per batch, and loads `profile.loadPaths(snapshot)` and the persisted
   geometry only when the gate asks for them.
