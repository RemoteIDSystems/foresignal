# 007 — History Schema And Partitions

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/007-CR-History-Schema-And-Partitions.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/006-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/design-history/Core-Data-Model.md` §0, §1 (invariants I1, I9, I10), §8 (tables), §9 (partitioning and schema ownership), §16 (deployment), §17 (`SchemaParityIT`), and the **"006a History Schema And Partitions"** bullets under *Per-prompt changes* (006a is this prompt; its "016" is prompt 019 and its "063" is prompt 144)
- `docs/design-history/Gap-Analysis.md` §8.A item 2 (`validFrom`, `record_head.title`)
- `docs/Architecture.md` §7.1, §7.3 (superseded by Core-Data-Model §8–§9 where they differ)
- Code: `config/SchemaBootstrap`, `config/PostgisExtensionInitializer`, `config/SpatialIndexInitializer`, `store/*` (record entities from prompt 002), `test/DatabaseCleaner`, `store/EntityMappingIT`, `config/SchemaBootstrapIT`

## Objective

Replace the prompt-002 history tables with the v1.2 storage model: a `record_version` partitioned by `LIST (history_tier)` then `RANGE (seq)` whose DDL is owned by bootstrap SQL, a slim `record_head` (persisted state), a hot `record_live` (current state), `fetch_log` and `seq_clock`. Add `PartitionManager`, which keeps seq coverage ahead of allocation and can drop leaves safely. No ingest logic yet: the writer (prompt 009) and retention (prompt 019) build on this.

## Tasks

### 1. Entities (`ai.foresignal.store`), implement exactly as Core-Data-Model §8

- **`RecordVersionEntity`** (rewrite; read-only mapping):
  - `@Immutable`, `@Id seq` with `@SequenceGenerator(name="record_seq", sequenceName="record_seq", allocationSize=1)`. **No** `@UniqueConstraint`, **no** `@Index`, no `content_hash`. The database PK is `(seq, history_tier)`; mapping only `seq` as `@Id` is intentional.
  - Columns in this order: `seq bigint`, `ingested_at`, `observed_at`, `received_at timestamptz` (not null), `event_time`, `valid_until`, `valid_until_explicit timestamptz` (null), `prev_seq`, `batch_id bigint` (null), `version int`, `history_tier`, `persist_reason`, `qa_flags smallint` (not null), `tombstone bool`, `source_id varchar(64)`, `external_id varchar(256)`, `kind varchar(64)`, `geom geometry(Geometry,4326)` (null), `payload jsonb` (not null), `quality jsonb` (null), `observer jsonb` (null), then **`valid_from timestamptz` (null)**, appended for Gap-Analysis §8.A item 2.
  - smallints map as `Short` with `@JdbcTypeCode(SqlTypes.SMALLINT)`. Add `HistoryTier.code()` (STANDARD=0, TRACK=1) and `HistoryTier.fromCode(short)` to the SPI enum if missing.
- **`RecordHeadEntity`** (rewrite; written only on PERSIST, recipe migration and purge): PK `(source_id, external_id)` (keep `RecordHeadId`); `kind`, `seq`, `version`, `event_time`, `valid_until_explicit`, `valid_from`, `tombstone`, `static_hash bigint`, `hash_recipe int`, `persisted_observed_at`, `persisted_received_at timestamptz`, `persisted_geom geometry(Geometry,4326)` null (**no index**), `payload jsonb`, `quality jsonb`, `search_text text`, `title varchar(256)` null (filled by prompt 008's title resolver). Index `(kind)` only. Remove `observed_at`, `valid_until`, `geom`, `content_hash`.
- **`RecordLiveEntity`** (new): PK `(source_id, external_id)` (reuse `RecordHeadId`); `observed_at`, `last_received_at`, `valid_until timestamptz`, `geom geometry(Geometry,4326)` null, `full_hash bigint`, `dirty bool`, `kinematic jsonb not null default '{}'`, `quality jsonb`, `qa_flags smallint`. No JPA indexes. Javadoc: **"No index on `observed_at`, by design: touches must stay HOT updates. Do not add one."**
- **`FetchLogEntity`** (new): `id` identity PK, `source_id varchar(64)` not null, `trigger` (SCHEDULED, MANUAL, STREAM, PUSH) and `status` (OK, NOT_MODIFIED, FAILED, REFUSED, DISCARDED) as `varchar(16)` enums (`FetchTrigger`, `FetchStatus`), `started_at`, `received_at`, `ingested_at` (null unless the writer inserted it), `http_status`, `request_count`, `url_redacted`, `etag`, `bytes`, `body_sha256`, `adapter_version`, `licence`, `principal`, `records_seen`, `rejected`, `late`, `skipped`, `versions_written`, `live_updates`, `touches`, `from_seq`, `to_seq`, `coverage jsonb`, `error text`. `@Index(columnList = "source_id, started_at DESC")`. `batch_id` on versions references `fetch_log.id` with **no FK**.
- **`SeqClockEntity`** (new): `minute timestamptz` PK, `first_seq bigint` not null.
- **Existing tables:** `AlertEntity` + `observed_at timestamptz` and `record_snapshot jsonb`; `TriggerRuleEntity` + `reappear_as_enter boolean not null default false`; `SourceHealthEntity` + `last_data_at timestamptz` (check first: prompt 006 may already have added it, and `last_coverage` / `recent_coverage`; add whichever is missing).
- **Repositories:** delete `RecordVersionRepository.countBySourceId` and `RecordHeadRepository.deleteBySourceIdAndObservedAtBefore`; rename the version finders to order by `seq`; add `RecordLiveRepository`, `FetchLogRepository` (keyset `findBySourceIdAndIdLessThanOrderByIdDesc`), `SeqClockRepository`. Fix every caller.

### 2. `HistorySchemaInitializer` (`ai.foresignal.store.history`)

Register in `SchemaBootstrap` as bean `historySchemaInitializer` with `@DependsOn("postgisExtensionInitializer")`, and add it to the `EntityManagerFactoryDependsOnPostProcessor` names. It depends only on `DataSource`, `MeterRegistry` and `HistoryProperties`, never on JPA. On `afterPropertiesSet`, on one connection:

1. `CREATE SEQUENCE IF NOT EXISTS record_seq INCREMENT BY 1`.
2. **relkind guard:** if `record_version` exists with `relkind = 'r'` (the prompt-002 plain table): when `record_version` **and** `record_head` are both empty, `DROP TABLE record_version, record_head CASCADE` (dropping the head too, so Hibernate recreates it without the old NOT NULL columns); otherwise fail fast: `"record_version holds pre-v1.2 data; run docker compose down -v and restart"`.
3. `CREATE TABLE IF NOT EXISTS record_version (… all §1 columns …, PRIMARY KEY (seq, history_tier)) PARTITION BY LIST (history_tier)`; `CREATE TABLE IF NOT EXISTS record_version_std PARTITION OF record_version FOR VALUES IN (0) PARTITION BY RANGE (seq)`; same for `record_version_trk` `IN (1)`. Then `ALTER TABLE record_version ADD COLUMN IF NOT EXISTS …` for every column, so a later column addition is applied in place.
4. `CREATE INDEX IF NOT EXISTS` on the parent: `record_version_source_external_seq_idx (source_id, external_id, seq)`, `record_version_source_observed_idx (source_id, observed_at)`, `record_version_geom_gist USING GIST (geom)`. No `ingested_at` index, no unique constraint.
5. `PartitionManager.ensureAhead()`.

Keep the DDL as constants in one class (prompt 144 moves it into the Flyway baseline). Never create a DEFAULT partition.

### 3. `PartitionManager` (`ai.foresignal.store.history`)

- `HistoryProperties` (`foresignal.history`): `track-block` 20,000,000; `standard-block` 250,000,000; `maintenance.enabled` true; `maintenance.interval` 10m.
- **Leaves:** `rv_s_<start>` / `rv_t_<start>` for `[k·block, (k+1)·block)`, attached under the tier parent. seq is global across tiers.
- **Locking:** every operation holds one in-process `ReentrantLock` and runs on a dedicated **autocommit** connection from the `DataSource` with `SET lock_timeout = '3s'`, retrying `55P03` up to 5 times (backoff 200 ms × 2ⁿ). Never `@Transactional`.
- **Coverage is range-based:** keep an in-memory map of attached leaves per tier (refreshed from `pg_inherits` + `pg_get_expr(relpartbound)` at start, after every change and on `refresh()`).
  - `coveredUpTo(tier, fromSeq)`: exclusive end of the contiguous attached range starting at `fromSeq`'s block, or `fromSeq` when that block is missing. Cheap (no SQL); the writer calls it per batch.
  - `ensureFor(tier, toSeq)` / `ensureRange(tier, fromSeq, toSeq)`: create every missing block intersecting the range.
  - `ensureAhead()`: per tier, `ensureRange(tier, last_value, last_value + 1.5 × block)` where `last_value` comes from `SELECT last_value FROM record_seq`.
- **Create:** re-check `pg_inherits`; `CREATE TABLE rv_t_<start> (LIKE record_version INCLUDING DEFAULTS)`; `ALTER TABLE record_version_trk ATTACH PARTITION rv_t_<start> FOR VALUES FROM (a) TO (b)` (indexes and PK are created by the attach). Treat `42P07` (table exists: attach it if unattached) and `42P17` when the re-check finds a covering leaf as success.
- **Drop** (`drop(tier, leafName)`, called from prompt 019): throw `IllegalStateException` when `TransactionSynchronizationManager.isActualTransactionActive()`; run `ensureAhead()` first; `SET statement_timeout = '10min'`; `ALTER TABLE <tier parent> DETACH PARTITION <leaf> CONCURRENTLY`; `DROP TABLE <leaf>`. Every maintenance run first finishes any leaf with `pg_inherits.inhdetachpending` via `DETACH PARTITION … FINALIZE` then drops it.
- `leaves(tier)` → `List<LeafInfo(name, tier, fromSeq, toSeq, detachPending)>` for prompt 019 and `/v1/system.history`.
- **Schedule:** a `@Scheduled` maintenance run every `maintenance.interval` (finalize, `ensureAhead`) and a daily `ANALYZE record_version` (03:17 UTC), both skipped when `maintenance.enabled=false`; also `ANALYZE` after each attach or drop, under the same lock.
- Gauge `foresignal.history.partition.headroom{tier}` = `coveredUpTo(tier, last_value) − last_value`; log a warning when headroom is below one block.

### 4. Spatial indexes and storage parameters

Update `SpatialIndexInitializer.STATEMENTS`: remove the `record_version` GiST (now owned by §2); replace the `record_head` GiST with `DROP INDEX IF EXISTS record_head_geom_gist`; add `CREATE INDEX IF NOT EXISTS record_live_geom_gist ON record_live USING GIST (geom)` and `ALTER TABLE record_live SET (fillfactor = 70, autovacuum_vacuum_scale_factor = 0.02, autovacuum_vacuum_cost_limit = 2000)`. Keep the trigger-rule geofence index.

### 5. Configuration and deployment (Core-Data-Model §9, §16)

- Main `application.yml`: `spring.jpa.properties.hibernate.hbm2ddl.extra_physical_table_types: PARTITIONED TABLE`; `spring.datasource.hikari.data-source-properties.reWriteBatchedInserts: true` (this also covers the Testcontainers `@ServiceConnection` URL); the `foresignal.history.*` defaults; `foresignal.ingest.writer.exclusive: true`.
- `application-test.yml`: `foresignal.ingest.writer.exclusive: false`, `foresignal.history.maintenance.enabled: false`, `foresignal.retention.scheduled: false` (read by prompt 019). It must **not** override `spring.jpa.properties`.
- Fix the Flyway references in `SchemaBootstrap` Javadoc, `SpatialIndexInitializer` and the `application.yml` comment from "prompt 042" to "prompt 144".
- `docker-compose.yml`: add to `db` `command: ["postgres","-c","shared_buffers=512MB","-c","max_wal_size=8GB","-c","checkpoint_timeout=15min","-c","wal_compression=lz4","-c","max_locks_per_transaction=256","-c","autovacuum_vacuum_cost_limit=2000"]`; ports `"${FORESIGNAL_DB_BIND:-127.0.0.1}:5433:5432"` and `"${FORESIGNAL_BIND:-127.0.0.1}:8080:8080"`.

### 6. Test support

- **`DatabaseCleaner`:** exclude every table that is a `pg_inherits` child (tier parents and leaves) from the discovered list; `TRUNCATE record_version` recurses. After truncating and restarting `record_seq`, call `resetState()` on every bean of a new main-code interface `ai.foresignal.ingest.IngestStateReset` (via `ObjectProvider.orderedStream()`; none exist until prompt 009's `IngestWriter` implements it).
- **`TestPartitions`** (`src/test/java/ai/foresignal/test`): `leaves(jdbc, tier)`, `dropAllLeaves(jdbc)` (plain DETACH + DROP, tables must be empty), and `restore(partitionManager)` (`refresh()` + `ensureAhead()`), so a test can run a `PartitionManager` with tiny blocks and put the defaults back in `@AfterEach`.
- Update `EntityMappingIT` (raw-SQL `insert_record_version` with the new columns, head/live/fetch_log/seq_clock/alert/trigger-rule round trips) and `SchemaBootstrapIT` (index list, table list, `record_version` is partitioned).

### 7. Docs

- `README.md` deployment notes: binds default to localhost (`FORESIGNAL_BIND=0.0.0.0` for LAN access, `FORESIGNAL_DB_BIND` likewise); expect about 10–15 GB/day of history writes plus 15–20 GB/day of WAL at full load; a 3d track window holds about 40M rows (~20 GB with indexes); NTP must be synced before Foresignal starts or the writer's clock guard (prompt 009) holds ingest.
- No API change in this prompt; OpenAPI and Postman are untouched.

## Tests to write (do not run)

- `SchemaParityIT` (Core-Data-Model §17): `record_version` has `relkind='p'`; `information_schema.columns` matches the entity's mapped columns by name, nullability and type (derive the expected set from `@Column`/`@JdbcTypeCode` metadata by reflection); no constraint `record_version_source_external_version_key`; a second Hibernate schema update (`SchemaUpdate` with `TargetType.SCRIPT` over `Metadata` built with the application's JPA properties, including `extra_physical_table_types`) emits no statement mentioning `record_version`.
- `HistorySchemaInitializerIT`: an empty plain `record_version` + `record_head` is converted to the partitioned layout; a non-empty plain table fails with the documented message; a second run is a no-op; `ADD COLUMN IF NOT EXISTS` restores a dropped nullable column.
- `PartitionManagerIT` (own `PartitionManager` with blocks of 100, via `TestPartitions`): ensure is idempotent; concurrent `ensureFor` from two threads creates each leaf once; attach succeeds while another session holds an open insert transaction on an existing leaf (assert success under `lock_timeout`, no sleeps); `drop` inside a `TransactionTemplate` throws; detach then drop; a leaf left `inhdetachpending` is finalized on the next run; a dropped low block is recreated by `ensureRange`; headroom gauge value.
- `EntityMappingIT` (update) and `DatabaseCleanerIT` (partitions survive cleaning; leaves are not truncated individually; `IngestStateReset` beans are invoked, using a stub provider).
- `SpatialIndexInitializerTest`: the statement list.

## Acceptance criteria

- On an empty database, startup creates the partitioned `record_version` with both tier parents, at least 1.5 blocks of leaves per tier, and the three partitioned indexes, before the `EntityManagerFactory` initialises.
- Hibernate issues no DDL for `record_version` on a second start.
- `record_live` has a GiST index on `geom` and no index on `observed_at`; `record_head` has only the PK and `(kind)` indexes.
- Compiles.

## Sanctioned test run

This prompt is one of three core prompts (007, 008, 009) allowed a narrow test run, because every later prompt builds on partitioned history schema, bootstrap DDL and the test database cleaner (`docs/design-history/Core-Data-Model.md` §17; owner decision 2026-10-11). After compiling, run **only** these classes, once, and fix failures in production code (change a test only if it asserts the wrong thing, and record it):

```bash
./gradlew test --tests "*SchemaParityIT" --tests "*HistorySchemaInitializerIT" --tests "*PartitionManagerIT" --tests "*EntityMappingIT" --tests "*DatabaseCleanerIT" --tests "*SpatialIndexInitializerTest"
```

Start Docker first if the run includes `*IT` classes (Testcontainers). Do not run any other tests. Record the result (counts, failures fixed) under "Tests run" in the Completion Report.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests except the Sanctioned test run below.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Then do the Sanctioned test run (only the listed classes) and fix what it finds.
2. **Commit** all changes with the message `007: History Schema And Partitions`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/007-CR-History-Schema-And-Partitions.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `007: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
