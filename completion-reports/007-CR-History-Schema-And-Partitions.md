# 007-CR-History-Schema-And-Partitions

| | |
|---|---|
| Prompt | 007-History-Schema-And-Partitions.md |
| Status | Complete |
| Implementation commit | 855b47581d3b63e6498330ae0fd9783edd874932 |
| Date | 2026-10-11 |

## Summary

The prompt-002 history tables are replaced by the v1.2 storage model (Core-Data-Model §8–§9).

**`record_version` is now partitioned and owned by bootstrap SQL.** The layout is
`LIST (history_tier)` → `record_version_std` (0) / `record_version_trk` (1), and each of those is
`RANGE (seq)`. The PK is `(seq, history_tier)`. There are three partitioned indexes
(`(source_id, external_id, seq)`, `(source_id, observed_at)`, GiST `(geom)`), and no unique
constraint, no `ingested_at` index, no content hash and no DEFAULT partition. The DDL lives as
constants in one class, `store.history.HistoryDdl`, ready for prompt 144 to move into Flyway.

**`HistorySchemaInitializer`** is registered in `SchemaBootstrap` as `historySchemaInitializer`,
`@DependsOn("postgisExtensionInitializer")`, and added to the
`EntityManagerFactoryDependsOnPostProcessor` names, so it runs before the
`EntityManagerFactory`. On one connection it:
1. creates `record_seq`
2. runs the relkind guard:
   - an empty plain `record_version` (plus an empty or missing `record_head`) is dropped together
     with the head
   - a populated one stops startup with `record_version holds pre-v1.2 data; run docker compose down -v and restart`
3. creates the parent and both tier parents, then runs `ADD COLUMN IF NOT EXISTS` for every column
4. creates the indexes
5. calls `PartitionManager.ensureAhead()`

**`PartitionManager`** does the following:
- **Leaves:** names them `rv_s_<start>` / `rv_t_<start>`. Blocks are 250M (STANDARD) and 20M (TRACK)
  by default.
- **Locking:** holds one in-process `ReentrantLock`. Each operation runs on its own autocommit pooled
  connection with `lock_timeout = '3s'`, retries `55P03` five times with backoff of 200 ms × 2ⁿ, and
  resets the session settings before returning the connection. It is never `@Transactional`.
- **Coverage map:** keeps an in-memory, range-based map of attached leaves, read from `pg_inherits` +
  `pg_get_expr(relpartbound)`. `coveredUpTo` needs no SQL.
- **Ensuring leaves:** `ensureFor`, `ensureRange` and `ensureAhead` (1.5 blocks past
  `record_seq.last_value`, per tier). Create re-checks the catalog, then runs `CREATE TABLE … (LIKE
  record_version INCLUDING DEFAULTS)` + `ATTACH PARTITION`. Losing a race counts as success when the
  re-check finds a covering leaf.
- **Dropping:** `drop(tier, leaf)` refuses to run inside a transaction, runs `ensureAhead` first,
  then `DETACH … CONCURRENTLY` (10-minute statement timeout) and `DROP TABLE`.
- **Pending detaches:** `finalizePendingDetaches()` finishes `inhdetachpending` leaves with `FINALIZE` +
  `DROP`.
- **Schedule:** `maintain()` runs every `maintenance.interval` (10m), and `ANALYZE record_version`
  runs daily at 03:17 UTC. Both are skipped when `maintenance.enabled=false`. `ANALYZE` also runs
  after each attach or drop.
- **Metrics:** the gauge is `foresignal.history.partition.headroom{tier}`. A warning is logged when
  headroom is below one block.

**Entities:**
- `RecordVersionEntity` is a read-only mapping with columns in DDL order, `Short` smallints and
  `valid_from` appended.
- `RecordHeadEntity` was rewritten to §8. It now has `persisted_geom`, `static_hash`, `hash_recipe`,
  `valid_from` and `title`, and an index on `(kind)` only.
- New: `RecordLiveEntity` (with `kinematic jsonb not null default '{}'`, and no index on
  `observed_at`, which its Javadoc says), `FetchLogEntity` (`FetchTrigger`, `FetchStatus`),
  `SeqClockEntity`.
- `alert` gained `observed_at` and `record_snapshot`. `trigger_rule` gained
  `reappear_as_enter boolean not null default false`. `source_health` already had `last_data_at`,
  `last_coverage` and `recent_coverage` from 006, so nothing was added there.

**Spatial indexes and configuration:**
- `SpatialIndexInitializer` now:
  - drops `record_head_geom_gist`
  - creates `record_live_geom_gist`
  - sets the `record_live` storage parameters
  - keeps the geofence index
- `application.yml` gains `extra_physical_table_types: PARTITIONED TABLE`,
  `reWriteBatchedInserts`, the `foresignal.history.*` defaults and
  `foresignal.ingest.writer.exclusive: true`.
- The test profile sets `exclusive: false`, maintenance off, and `retention.scheduled: false`.
- The compose file gets the PostgreSQL flags and binds both ports to localhost by default.
- The README gets deployment notes.

**Test support:**
- `DatabaseCleaner` skips every `pg_inherits` child and calls each `ingest.IngestStateReset` bean
  after cleaning.
- `TestPartitions` provides `leaves`, `dropAllLeaves` and `restore`.

## Files created / modified

**Created (main)**
- `store/history/`: `HistoryDdl`, `HistoryProperties`, `HistorySchemaInitializer`, `LeafInfo`,
  `PartitionManager`, `package-info`
- `store/`: `RecordLiveEntity`, `RecordLiveRepository`, `FetchLogEntity`, `FetchLogRepository`,
  `FetchTrigger`, `FetchStatus`, `SeqClockEntity`, `SeqClockRepository`
- `ingest/IngestStateReset`

**Modified (main)**
- `store/RecordVersionEntity`, `RecordHeadEntity`: rewritten
- `store/RecordVersionRepository`:
  - `countBySourceId` removed
  - `…OrderByVersionDesc` replaced by `…OrderBySeqDesc` / `…OrderBySeqAsc`
- `store/RecordHeadRepository`: `deleteBySourceIdAndObservedAtBefore` removed. No main-code callers
  existed.
- `store/AlertEntity`, `store/TriggerRuleEntity`: the new columns above
- `source/spi/HistoryTier`: `code()`, `fromCode(short)`
- `config/SchemaBootstrap`, `config/SpatialIndexInitializer`: wiring, statements, "prompt 144"
  Flyway references
- `source/InboxFiles`, `security/SecretKeyStore`: `@Autowired` on the public constructor (see
  Deviations)
- `application.yml`, `application-test.yml`, `docker-compose.yml`, `README.md`

**Tests**
- New:
  - `store/history/SchemaParityIT`
  - `store/history/HistorySchemaInitializerIT`
  - `store/history/PartitionManagerIT`
  - `store/history/HistoryPropertiesTest`
  - `config/SpatialIndexInitializerTest`
  - `source/spi/HistoryTierTest`
  - `store/NoJpaWritesToRecordVersionTest`
  - `test/TestPartitions`
- Updated: `store/EntityMappingIT`, `config/SchemaBootstrapIT`, `test/DatabaseCleaner`,
  `test/DatabaseCleanerIT`

No dependency changes. No Flyway, Liquibase or SQL migration files. OpenAPI and Postman are
untouched, since this prompt has no API change.

## Deviations from the prompt

1. **Hibernate 7.4 has no `SchemaUpdate` class.** It was removed in Hibernate 7, which I checked by
   listing `hibernate-core-7.4.5.Final.jar`. `SchemaParityIT` therefore calls the same machinery
   directly: `SchemaManagementTool.getSchemaMigrator(...).doMigration(metadata, options,
   ContributableMatcher.ALL, target)` with a `TargetType.SCRIPT` target that collects statements.
   The `Metadata` is built from every `@Entity` and the application's own Hibernate settings
   (`HibernateProperties.determineHibernateProperties` plus the registered customizers, including
   `extra_physical_table_types`).
2. **`extra_physical_table_types` is redundant with Hibernate 7.4's `PostgreSQLDialect`,** which
   already adds `PARTITIONED TABLE` to its physical table types (checked with `strings` on the
   class). The setting is kept as the prompt and §9 require. My first control test assumed Hibernate
   would emit `create table record_version` without the setting, and that assumption was wrong. The
   test now pins the dialect behavior instead.
3. **`PartitionManager` is its own bean (`partitionManager`)** rather than being built inside the
   initializer, so prompt 019 and `/v1/system` can inject it. The initializer depends on
   `DataSource` + `PartitionManager`. Transitively that is still only `DataSource`, `MeterRegistry`
   and `HistoryProperties`, with no JPA.
4. **`drop` has one guard beyond the spec.** It refuses a leaf whose range reaches past
   `last_value + 1`, because that leaf may still receive rows. It also refuses names that don't match
   `rv_[st]_<digits>`, because the name is spliced into DDL.
5. **`ensureFor(tier, toSeq)` ensures only the block containing `toSeq`.** `ensureRange(tier, from,
   to)` covers a span, and that is what the writer should call for a batch that crosses blocks.
6. **Lost create races are also detected through `23505`.** Two sessions creating the same table at
   the same instant can fail on `pg_type_typname_nsp_index` (`23505`) instead of `42P07`; the
   sanctioned run reproduced this. Both codes now lead to the re-check-then-attach path. An attach
   failure other than a lock timeout is treated as success whenever the re-check finds a covering
   leaf. This covers 42P17 and "already a partition".
7. **Column types the prompt left open:**
   - `observer` is mapped as `List<Map<String,Object>>`, because `NormalizedRecord.observers` is a
     list.
   - `record_live.qa_flags` is `smallint not null default 0`.
   - `record_head` `static_hash` / `hash_recipe` / `persisted_*_at` / `payload` are NOT NULL.
   - `fetch_log` counters are `int not null`. Text widths: `etag varchar(512)`, `body_sha256
     varchar(64)`, `adapter_version varchar(64)`, `licence varchar(128)`, `principal varchar(128)`,
     `url_redacted text`, `error text`.
   - `fetch_log.coverage` uses 006's `CoverageSample`.
8. **Production fix outside scope: `InboxFiles` and `SecretKeyStore`.** Each `@Component` had two
   constructors (one public, one package-private for tests) and no `@Autowired`, so Spring could not
   instantiate them. Every `@IntegrationTest` context therefore failed to start. This defect dates
   from prompts 003/004 and only surfaced now because no tests had been run before. The public
   constructor of each is now marked `@Autowired`. A scan of every
   `@Component/@Service/@Repository/@Configuration` class found no other case.
9. **`NoJpaWritesToRecordVersionTest`** (ING-6, owner 007 in `docs/Invariants.md`) was written even
   though the prompt doesn't list it, so the invariant's named test exists. It is not in the
   sanctioned set and was not run.

## Issues found / open questions

1. **Other cached contexts hold stale leaf maps.** Each Spring context has its own
   `PartitionManager` whose in-memory map is loaded at startup. When `PartitionManagerIT` drops and
   restores leaves, only the context it runs in is refreshed. Nothing reads `coveredUpTo` yet. When
   prompt 009's writer does, its pre-flight should fall back to `refresh()` and `ensureRange` when
   coverage looks short; the design already says to fail only when a range is truly uncovered.
2. **`licence` spelling.** The column `fetch_log.licence` follows Core-Data-Model §8 as written. My
   prose and Javadoc say "license". The persisted name was not changed, because it is a stored
   identifier.
3. **Startup takes an `ACCESS EXCLUSIVE` lock on `record_version`.** The `ADD COLUMN IF NOT EXISTS`
   statement takes this lock even when nothing is missing, and it reaches every leaf. That is fine at
   startup. `max_locks_per_transaction=256` in compose gives headroom for many leaves.
4. **The README "Status" section is stale.** It still says tests are first executed in prompt 061.
   This prompt didn't touch that text.

No instructions were found in data or tool output.

## Test results

Sanctioned narrow run (`docs/Core-Data-Model.md` §17), exactly the listed classes:

```
./gradlew test --tests "*SchemaParityIT" --tests "*HistorySchemaInitializerIT" --tests "*PartitionManagerIT" --tests "*EntityMappingIT" --tests "*DatabaseCleanerIT" --tests "*SpatialIndexInitializerTest"
```

**Final result: 94 tests, 0 failures, 0 errors, 0 skipped.** `BUILD SUCCESSFUL`.

| Class | Tests | Failures |
|---|---|---|
| `SpatialIndexInitializerTest` | 4 | 0 |
| `EntityMappingIT` | 45 | 0 |
| `HistorySchemaInitializerIT` | 7 | 0 |
| `PartitionManagerIT` | 20 | 0 |
| `SchemaParityIT` | 6 | 0 |
| `DatabaseCleanerIT` | 12 | 0 |

I ran the command three times, fixing failures in between:

**Run 1: 94 tests, 90 failed.** Every Spring context failed to start (`InboxFiles`: "No default
constructor found"). Fixed in production code (Deviation 8).

**Run 2: 94 tests, 7 failed.**
- *Production fixes:*
  - `HistorySchemaInitializer`'s emptiness query named `record_head` even when it was missing, and
    PostgreSQL fails that at parse time. The query is split into an existence check plus an
    emptiness check.
  - Concurrent create from two managers failed with `23505` (Deviation 6).
- *Test fixes, where the test asserted the wrong thing:*
  - `SchemaParityIT`'s control assumption (Deviation 2).
  - `EntityMappingIT.appSettingStoresRawJson` (pre-existing) compared jsonb text verbatim, but jsonb
    prints its canonical form. It now compares ignoring whitespace.
  - `DatabaseCleanerIT.leavesRowsBehind` (pre-existing) ran `select nextval` through
    `JdbcTemplate.update`, which pgjdbc rejects. It now uses `queryForObject`.
  - `HistorySchemaInitializerIT`'s snapshot query concatenated a `"char"` without a cast.
- *Robustness fixes:*
  - `PartitionManagerIT.runConcurrently` now waits for both tasks before reporting.
  - `TestPartitions.dropAllLeaves` also drops unattached leaf-named tables.
  - Together these removed a cascade failure in `droppedLowBlockIsRecreated`.

**Run 3: all green.** Startup logs show the four default leaves attached before the
`EntityManagerFactory`. No Hibernate DDL warnings mention `record_version`.

No other tests were run.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 749ms
```

## Notes for the next prompt

1. **Writer pre-flight (009).**
   - Before inserting a batch with seqs `[a, b]`, check `partitionManager.coveredUpTo(tier, a) > b`.
     If it isn't, call `partitionManager.ensureRange(tier, a, b)`, and fail the batch only if it is
     still uncovered.
   - Tier codes come from `HistoryTier.code()`.
2. **Writer state reset (009).** `IngestWriter` should implement `ai.foresignal.ingest.IngestStateReset`.
   `DatabaseCleaner` already calls every such bean after truncating and restarting `record_seq`.
3. **Native inserts (009).** `HistoryDdl.COLUMNS` is the authoritative column order for the native
   `record_version` insert. `persist_reason` and `qa_flags` are `smallint` and NOT NULL.
   `batch_id` = `fetch_log.id` (no FK).
4. **Retention (019).**
   - Inject `PartitionManager`, list TRACK leaves with `leaves(TRACK)`, and call
     `drop(TRACK, name)` outside any transaction.
   - `drop` refuses leaves the allocator hasn't passed.
   - `foresignal.retention.scheduled` is already `false` in the test profile.
5. **Writer lock (009).** `foresignal.ingest.writer.exclusive` is set (`true` in main, `false` in
   tests), but nothing binds it yet. 009 owns the properties class.
6. **Raw-SQL seeding (008, 009).** ITs written after 011 must seed history through `TestRecords` /
   the writer, not raw SQL (Core-Data-Model §17). `EntityMappingIT` and `PartitionManagerIT` use raw
   SQL on purpose, because they test the schema itself.
