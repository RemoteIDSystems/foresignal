# 044 — History Downsampling

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/044-CR-History-Downsampling.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/043-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §9 (partitioning, `PartitionManager`), §12 (asOf), §13 (retention, **Downsampling** paragraph), §1 (invariants I1, I7, I9), and the "030a History Downsampling" bullets under *Per-prompt changes* (030a = this prompt, Roadmap §5)
- `docs/OpenAPI.json` schemas `RecordVersion`, `RecordPage`, `RetentionSettings`, `SourceSettings`, `SourceSettingsUpdate`, `TriggerTestResult`, `SystemInfo`
- Completion Reports 007, 009, 014, 017, 019 (actual names of `PartitionManager`, `RetentionJob`, `RetentionState`, `AsOfQuery`, the trigger test service)

## Objective

Shrink TRACK-tier history older than a raw window without losing anything that matters: track leaves past `rawWindow` are rewritten into a thinned copy and swapped in atomically. Everything a head, an alert, a trigger edge or an asOf query depends on is kept; ordinary samples are thinned to one per entity per `downsampleInterval`. asOf over a downsampled range widens validity and reports `approximate`.

## Tasks

### 1. Hooks (check, add if missing)

- **019:** `RetentionSettings.trackWindow` (3d) and the `RetentionJob` TRACK leaf drop, consumer clamp (`min(trigger.cursor, embedding.cursor) + 1`) and `RetentionState`. Downsampling runs inside the same hourly job, after track drops.
- **007 / 009:** `PartitionManager` with its single `ReentrantLock`, dedicated autocommit connection, `lock_timeout='3s'` with 5 retries, and leaf naming `rv_t_<start>`. Reuse them; do not open a second lock.
- **014:** asOf per §12 with `approximate` and `horizon` on the response, and `/versions` ordered by seq.
- **017:** `testTrigger` with a `since`/seq range.

### 2. Settings

- `RetentionSettings` gains `trackRawWindow` (default `24h`; `null` disables downsampling) and `trackDownsampleInterval` (default `5m`). Validation: `trackRawWindow < trackWindow`, `30s ≤ trackDownsampleInterval ≤ trackRawWindow`. Errors use `VALIDATION_FAILED` with field paths.
- Per-source override: `source_settings.downsample_interval` (nullable interval), exposed as `downsampleInterval` on `SourceSettings` / `SourceSettingsUpdate`. Accepted only for TRACK sources (STANDARD → 400 `VALIDATION_FAILED`, field `downsampleInterval`); explicit null clears it.
- `effectiveDownsampleInterval(sourceId)` on `SourceService`.

### 3. Bookkeeping table

`history_downsample` (Hibernate entity): `leaf_name` PK, `history_tier`, `from_seq`, `to_seq`, `default_interval_s`, `rows_before`, `rows_after`, `started_at`, `completed_at`, `status` (RUNNING, DONE, FAILED), `error`. Queried by the asOf path, `/versions` and `testTrigger`. Cache the DONE ranges in a `DownsampleRanges` bean (refreshed after each run and on startup).

### 4. `HistoryDownsampler` (`ai.foresignal.retention`)

Runs from `RetentionJob` (not `@Transactional`), at most **one leaf per run**, under the `PartitionManager` lock.

**Eligible leaf:** a TRACK leaf with no DONE row whose upper bound ≤ `cutoffSeq(now − trackRawWindow)`, ≤ the consumer clamp, and > `cutoffSeq(now − trackWindow + 1h)` (do not rewrite a leaf about to be dropped). Oldest first.

**Keep set** (Core-Data-Model §13), computed over the leaf only:
1. `persist_reason` IN (FIRST 1, REAPPEAR 2, SIGNIFICANT 3, TOMBSTONE 4, TRIGGER_EDGE 9);
2. the **maximum seq per (source_id, external_id)** in the leaf;
3. every seq referenced by `record_head.seq` or `alert.seq` within the leaf's range;
4. among the remaining rows, one row per (source_id, external_id, `floor(extract(epoch from observed_at) / interval)`), the lowest seq, where `interval` is the source's effective downsample interval (join a `VALUES (source_id, interval_s)` list; sources without an override use the default).

**Rewrite and swap** (exact steps, each statement on the PartitionManager connection):
1. Insert a RUNNING `history_downsample` row.
2. `CREATE TABLE rv_t_<start>_ds (LIKE record_version INCLUDING DEFAULTS)`.
3. `INSERT INTO rv_t_<start>_ds SELECT * FROM rv_t_<start> WHERE <keep set>` (one statement, using a window function for rule 4).
4. Add the leaf indexes on the new table **before attaching** so ATTACH only links them: PK `(seq, history_tier)`, `(source_id, external_id, seq)`, `(source_id, observed_at)`, GiST `(geom)`.
5. `ALTER TABLE rv_t_<start>_ds ADD CONSTRAINT ds_bounds CHECK (history_tier = 1 AND seq >= :a AND seq < :b)` so ATTACH skips the validation scan.
6. In **one short transaction** with `SET LOCAL lock_timeout = '3s'`: `ALTER TABLE record_version_trk DETACH PARTITION rv_t_<start>`; `ALTER TABLE record_version_trk ATTACH PARTITION rv_t_<start>_ds FOR VALUES FROM (:a) TO (:b)`; commit. On lock timeout, roll back and retry up to 5 times with backoff; then mark FAILED and drop the copy. The writer is never blocked longer than the lock timeout.
7. `DROP TABLE rv_t_<start>`, `ALTER TABLE rv_t_<start>_ds RENAME TO rv_t_<start>`, drop the CHECK constraint, `ANALYZE rv_t_<start>`.
8. Mark DONE with row counts; refresh `DownsampleRanges`.

Rules: the writer never inserts into an eligible leaf (its range is far below `lastAllocatedSeq`), so no lock on writes is needed beyond step 6. **Track `floorSeq` is unchanged.** Startup recovery: a RUNNING row whose `_ds` table exists and is not attached is dropped and the row marked FAILED; if the swap had committed, finish steps 7–8. I9 holds: only native SQL touches `record_version`.

Metrics: `foresignal.history.downsample.rows_before`, `rows_after`, `duration`, `failures`. `/v1/system.history` per tier adds `downsampledLeaves` and `downsampledRows`.

### 5. Read-path effects

- **`/versions`:** each item gets `downsampled: true` when its seq falls in a DONE range. A `prevSeq` that no longer exists is returned as-is (document: "predecessor removed by downsampling").
- **asOf (§12):** when `[L, S]` for a TRACK source overlaps a DONE range, validity on the transaction axis widens from `ttl` to `max(ttl, effectiveDownsampleInterval)`: `L = S(T − max(ttl, interval) − 2 min)` and keep rows with `ingested_at + max(ttl, interval) > T`. Set `approximate = true`. STANDARD is unaffected.
- **`testTrigger`:** when the tested seq range overlaps a DONE range, add warning `DOWNSAMPLED_RANGE` ("match-mode results over downsampled history under-count; edge reasons are preserved") to `TriggerTestResult.warnings`.
- **Live triggers are unaffected:** downsampling only touches seqs below the consumer clamp.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `RecordVersion.downsampled` (boolean); `RetentionSettings.trackRawWindow`, `trackDownsampleInterval`; `SourceSettings`/`SourceSettingsUpdate.downsampleInterval` (nullable); `TriggerTestResult.warnings` (array of `{code, message}`) if missing; `SystemInfo.history` tier fields `downsampledLeaves`, `downsampledRows`. No new operations (no new `x-foresignal-scope` entries). Update the `updateRetention` and `updateSource` bodies in the Postman script's BODIES map, then regenerate with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Architecture.md` §11 (downsampling: keep set, swap, asOf widening); Operator Guide section "History downsampling" (what is kept, storage effect, how to disable).

## Tests to write (do not run)

- `DownsampleKeepSetIT` (seed through `TestRecords` only, small `foresignal.history.trackBlock` via `TestPartitions`): FIRST/REAPPEAR/SIGNIFICANT/TOMBSTONE/TRIGGER_EDGE kept; max seq per entity per leaf kept; head- and alert-referenced seqs kept; one sample per entity per interval; per-source interval override honoured.
- `HistoryDownsamplerIT`: full rewrite-and-swap; leaf bounds and indexes intact (`pg_inherits`, `pg_indexes`); concurrent writer batches into the newest leaf succeed during the swap; lock timeout path retries then fails cleanly with no orphan `_ds` table; startup recovery for both crash points; `floorSeq` unchanged; leaves near the drop horizon and above the consumer clamp skipped.
- `AsOfDownsampledIT`: a stationary keyframed aircraft queried between surviving rows is still present with `approximate=true`; STANDARD unaffected.
- `RecordVersionsApiIT` (extend): `downsampled` flag.
- `TriggerTestDownsampleTest`: warning present only for overlapping ranges.
- `RetentionSettingsValidationTest`: raw window ≥ track window rejected; interval bounds; STANDARD source override rejected.

## Acceptance criteria

- A leaf past `trackRawWindow` is replaced by a thinned copy holding exactly the keep set, with the writer never blocked longer than the lock timeout.
- Heads, alerts and edge versions still resolve; asOf over the range returns entities with `approximate=true`.
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
2. **Commit** all changes with the message `044: History Downsampling`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/044-CR-History-Downsampling.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `044: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
