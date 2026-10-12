# 044-CR-History-Downsampling

| | |
|---|---|
| Prompt | 044-History-Downsampling.md |
| Status | Complete |
| Implementation commit | da8bc55b61d056930586f426adf847eb976fb852 |
| Date | 2026-10-12 |

## Summary

Foresignal now thins TRACK history once it is older than a raw window. Each hourly retention run
rewrites at most one track leaf into a copy that holds only the keep set, and swaps the copy in
atomically. Heads, alerts, trigger edges and asOf reads still resolve over thinned history. All six
tasks are done. No operation was added, so `pending-operations.txt` is unchanged.

**1. Hooks.** All four already existed, so none were added:
- 019: `RetentionSettings.trackWindow`, the `TrackPartitionStep` leaf drop, the consumer clamp and
  `RetentionState`.
- 007/009: `PartitionManager`, with its single `ReentrantLock`, autocommit connection,
  `lock_timeout='3s'`, retries and the `rv_t_<start>` leaf names.
- 014/037: `AsOfResolver` / `AsOfPlan`, which already have `approximate` and `horizon`, plus the
  `HistoryFidelity` hook that 037 left for this prompt.
- 017: `TriggerBacktester`, with its seq and time ranges.

`PartitionManager` gains one method, `exclusive(ConnectionWork)`. It takes the same lock and the same
kind of autocommit connection with `lock_timeout='3s'`, but does not retry automatically, so the
downsampler can retry only its swap. No second lock was opened.

**2. Settings.**
- `RetentionSettings` gains two members:
  - `trackRawWindow`, default `24h`. An API `null` disables downsampling (Deviation 2).
  - `trackDownsampleInterval`, default `5m`.
- `RetentionSettingsService.parse` checks two rules, each against the values in force after the PUT,
  so an omitted member is checked at its default:
  - `trackRawWindow < trackWindow`;
  - `30s ≤ trackDownsampleInterval ≤ trackRawWindow`.

  A violation is `VALIDATION_FAILED`, with the field path `trackRawWindow` or
  `trackDownsampleInterval`.
- The per-source override is stored in `source_settings.downsample_interval`, a nullable PostgreSQL
  `interval` (`@JdbcTypeCode(SqlTypes.INTERVAL_SECOND)`). It is exposed as `downsampleInterval` on
  `SourceSettings` and `SourceSettingsUpdate`:
  - only TRACK sources accept it; a STANDARD source gets 400 `VALIDATION_FAILED` on the field
    `downsampleInterval`;
  - it must be at least 30 s, and no longer than `trackRawWindow` while downsampling is on;
  - an explicit `null` clears it.
- `SourceService` gains `effectiveDownsampleInterval(sourceId)` and `downsampleIntervalOverride(id)`.

**3. Bookkeeping.**
- The Hibernate entity `HistoryDownsampleEntity` maps the table `history_downsample`, with the columns
  the prompt lists. Its `status` (`DownsampleStatus`) is RUNNING, DONE or FAILED.
- The table is written only with plain JDBC on the `PartitionManager` connection.
- `DownsampleRanges` caches the DONE ranges. It reloads them:
  - in `afterSingletonsInstantiated`;
  - after every run and every recovery;
  - after the test cleaner truncates the table (it implements `IngestStateReset`).

**4. `HistoryDownsampler`** (`ai.foresignal.retention`). It is a `RetentionStep` at order 150, right
after the track drops at 100, and is not `@Transactional`.

*Eligible leaf.* It rewrites at most one leaf per run, oldest first. The leaf must be an attached
`rv_t_*` leaf with no DONE row, and its upper bound must be:
- ≤ `cutoffSeq(now − trackRawWindow)`;
- ≤ `RetentionRun.consumerBound()`. This is `min(cursor) + 1` over the consumers, and unlike the
  deletion clamp it is never released;
- \> `cutoffSeq(now − trackWindow + 1h)`.

*Keep set.* One `INSERT … SELECT` builds it, with CTEs and two window functions. It keeps:
- the reasons FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE and TRIGGER_EDGE;
- the newest seq of each entity, `max(seq) OVER (PARTITION BY source_id, external_id)`;
- the `record_head` and `alert` seqs inside the leaf's range;
- among the remaining rows, `row_number() = 1` per (pinned flag, source, entity,
  `floor(epoch(observed_at) / interval)`). Each source's interval comes from a `VALUES` join of the
  overrides, or the default.

*Rewrite and swap.* These are the exact steps 1–8 of the prompt, all under `PartitionManager.exclusive`:
1. Upsert a RUNNING row.
2. `CREATE TABLE rv_t_<start>_ds (LIKE record_version INCLUDING DEFAULTS)`.
3. Fill the copy with the keep set.
4. Add the PK and the three indexes.
5. Add the `ds_bounds` CHECK.
6. Swap in one transaction with `SET LOCAL lock_timeout='3s'`: DETACH the original, then ATTACH the
   copy. A lock timeout is retried 5 times, with backoff starting at 200 ms. After that the attempt is
   marked FAILED and the copy dropped.
7. DROP the original, RENAME the copy, rename its indexes to the leaf's usual names, DROP the CHECK,
   and ANALYZE.
8. Mark the row DONE with its row counts, and refresh the cached ranges.

Track `floorSeq` is unchanged.

*Recovery.* It runs at startup (`StartupOrder.HISTORY_DOWNSAMPLE = 13`) and again at the start of
every rewrite. For each RUNNING row:
- a copy that exists but is not attached is dropped, and the row marked FAILED;
- an attached copy, or a leaf that still carries `ds_bounds`, means the swap committed. The
  downsampler finishes step 7 and marks the row DONE;
- anything else is marked FAILED. The leaf is intact, and repeating a rewrite is idempotent.

Rows for leaves that retention has dropped are deleted (never a RUNNING row).

*Metrics.* `foresignal.history.downsample.rows_before`, `.rows_after`, `.duration` and `.failures`.
`/v1/system` `history.tiers[]` gains `downsampledLeaves` and `downsampledRows`.

**5. Read paths.**
- `DownsampleFidelity` implements 037's `HistoryFidelity`:
  - `downsampled`: a TRACK range overlaps a DONE range;
  - `validityWidening`: `max(ttl, interval) − ttl`;
  - `finalBefore(TRACK)`: `now − trackWindow + 1h` while downsampling is on.
- `/versions` and history items carry `downsampled: true` inside a DONE range (`RecordMapper`).
  `prevSeq` is returned as stored.
- asOf: `AsOfResolver.finish` computes `L` with the widened validity. When that range `[L, S]` is
  downsampled, the plan uses validity `max(ttl, interval)` and that `L`, and sets `downsampled` and
  `approximate`. Otherwise it uses the plain TTL. STANDARD is unaffected.
- `testTrigger` adds the warning `{code: DOWNSAMPLED_RANGE, message: "match-mode results over
  downsampled history under-count; edge reasons are preserved"}` when `(fromSeq, toSeq]` overlaps a
  DONE range.
- Live triggers are unaffected, because only seqs below the consumer bound are rewritten.

**6. Spec, Postman, docs.**
- The spec already had `RecordVersion.downsampled`, `RetentionSettings.trackRawWindow` and
  `trackDownsampleInterval`, `SourceSettings`/`SourceSettingsUpdate.downsampleInterval` and
  `TriggerTestResult.warnings`. The prompt asks for them to be added, and they were checked against
  the code instead.
- Added to the spec: `SystemInfo.history.tiers[].downsampledLeaves` and `downsampledRows`, plus a
  changelog entry.
- The `BODIES` entries for `updateRetention` and `updateSource` in the Postman script were updated,
  and the collection regenerated.
- `docs/Architecture.md`: §7 (`history_downsample`, `source_settings.downsample_interval`) and §11
  (the keep set, the swap, the asOf widening).
- `docs/Operator-Guide.md`: a new "History downsampling" section covering what is kept, the storage
  effect and how to disable it.

## Files created / modified

Production, created (`retention/`): `HistoryDownsampler`, `DownsampleRanges`, `DownsampleFidelity`,
`HistoryDownsampleEntity`, `DownsampleStatus`.

Production, modified:
- `store/history/`:
  - `PartitionManager`: `exclusive`, and `ConnectionWork` made public;
  - `AsOfResolver`, `AsOfPlan`: the widening now applies only over downsampled ranges;
  - `HistoryFidelity`, `RawHistoryFidelity`: Javadoc;
  - `HistoryFidelityConfiguration`: **deleted** (Deviation 4).
- `retention/`: `RetentionRun` (`consumerBound`), `RetentionSettingsService`, `package-info`.
- `settings/RetentionSettings`: two members, the raw-window serializer and deserializer, and a
  038-shape constructor.
- `source/`: `SourceService`, `SourceSettingsUpdate`. `store/SourceSettingsEntity`.
- `api/`:
  - `SourcesController`, `TriggersController`;
  - `dto/SourceSettingsDto`, `dto/SourceSettingsUpdate`;
  - `mapper/SourceMapper`, `mapper/RecordMapper`.
- `trigger/TriggerBacktester`: `Warning`, `Result.warnings` and `warnings(...)`.
- `system/`: `HistoryStats`, `SystemInfoService`. `config/StartupOrder`.

Docs and spec: `docs/OpenAPI.json`, `docs/Architecture.md`, `docs/Operator-Guide.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`.

Tests:
- New: listed below.
- Modified:
  - `store/history/AsOfResolverTest`: the widening semantics, and the configuration bean removed;
  - `settings/JacksonDurationIT`: the exact JSON now carries the new members, and a test for a
    disabled raw window was added;
  - `test/TestPartitions`: `dropAllLeaves` also drops orphan `rv_*_ds` tables.

## Deviations from the prompt

1. **The default block and `jumpSeq`, not a small `foresignal.history.trackBlock`.** The ITs seed
   versions into `rv_t_0` and then move the allocator into the next leaf, as `RetentionJobIT` does.
   A property override would have added another cached Spring context, and the 041 report warns that
   the test JVM's cache is nearly full. All 044 ITs share 019's `RetentionTestSources` context.
2. **How a disabled raw window is stored.** The record is both the API shape and the stored
   `app_setting` value, and an absent member must still mean "default 24h". So a disabled window is
   held internally as `Duration.ZERO` (`RetentionSettings.DOWNSAMPLING_DISABLED`) and serialized as
   `null`. The field's deserializer maps an explicit `null` to disabled and leaves an absent member
   null, so the default applies (`getNullValue` / `getAbsentValue`). Code should use
   `downsamplingEnabled()`.
3. **Widening is decided per tier, over the widened range.** `AsOfResolver` computes
   `L = S(T − (ttl + widening) − 2 min)` and asks whether `[L, S]` is downsampled.
   - If it is, the tier uses the widened validity.
   - If not, the tier uses the plain TTL and its own `L`.

   Before this prompt, 037 always added the widening. `AsOfResolverTest` was updated to match.
4. **`HistoryFidelityConfiguration` was deleted.** 037 registered `RawHistoryFidelity` through
   `@ConditionalOnMissingBean` in a plain `@Configuration`, which is not reliable outside
   auto-configuration. `DownsampleFidelity` is now the only bean of that type. Tests that need a stub
   still declare it `@Primary` (`TimeTravelTestSources`). `RawHistoryFidelity` stays as a plain class.
5. **Clearing an override is accepted for a STANDARD source.** `downsampleInterval: null` on a STANDARD
   source is a harmless no-op, so the Postman `updateSource` body, which targets a STANDARD source, can
   show the field as `null`. Setting a value on a STANDARD source is still 400.
6. **Per-source override bounds.** The prompt bounds only the global interval. The same rule is applied
   to the override: at least `30s`, and no longer than `trackRawWindow` while downsampling is on.
7. **Recovery also runs at the start of every rewrite,** not only at startup, under the same lock. A
   rewrite whose final statements failed after a committed swap is left RUNNING on purpose, so the next
   run finishes it.
8. **The bookkeeping row is upserted** (`ON CONFLICT (leaf_name)`), because `leaf_name` is the key and a
   FAILED leaf is tried again. Rows for leaves that retention has dropped are deleted, so
   `downsampledLeaves` counts only leaves that are still attached.
9. **`RecordVersionsApiIT` does not exist.** The versions endpoint is tested in `RecordsControllerIT`,
   which uses another context. The `downsampled` flag and `prevSeq` checks are in
   `HistoryDownsamplerIT.versionsFlag`, through MockMvc, in the shared retention context.
10. **Index renaming after the swap is cosmetic and best effort.** The copy's indexes are created as
    `rv_t_<start>_ds_*` and renamed to `rv_t_<start>_*` once the original is dropped. A failed rename
    is only logged.

## Issues found / open questions

**Needs the owner:**

1. **The swap's DETACH takes ACCESS EXCLUSIVE on `record_version_trk`.** This is a plain,
   non-concurrent DETACH, as the prompt specifies. While the swap waits for its 3 s lock timeout, TRACK
   writes and reads queue behind it, up to 3 s per attempt and at most 6 attempts per run. That matches
   "the writer is never blocked longer than the lock timeout", but each attempt can stall TRACK traffic
   by that much. **Owner:** keep this, or shorten the swap's lock timeout?
2. **`docs/Core-Data-Model.md` §13 says validity widens "to `downsampleInterval`".** The prompt and the
   code use `max(ttl, interval)`. **Owner:** which one should the document say?

**Other findings:**
- Not verified at run time; the ITs first run at 057:
  - that PostgreSQL accepts the keep-set statement as written (`WITH … INSERT … SELECT`, with a window
    function inside a boolean expression);
  - that ATTACH links the pre-built indexes rather than building new ones. `HistoryDownsamplerIT`
    checks this through `pg_inherits` on the index relations.
- The US English guard named in `~/Documents/GitLab/CLAUDE.md` (`meshdtn/style/test_us_english.py`)
  covers the MeshDTN repositories, not this one, so it was not run. A grep of the diff for common
  British spellings found none.
- No instructions directed at an agent appeared in any data, file or tool output.

## Tests written (not run)

The ITs are `@IntegrationTest` with `@Import(RetentionTestSources.class)`, sharing the 019 context
(no new cached context). They extend the new `testsupport/retention/DownsampleItSupport`.

| Test | What it covers |
|---|---|
| `retention/DownsampleKeepSetIT` | One sample per entity per 5-minute bucket (FIRST, lowest remaining seq per bucket, newest kept; the head still resolves); FIRST/SIGNIFICANT/TOMBSTONE/REAPPEAR kept; TRIGGER_EDGE (a real geofence enter rule) and alert-referenced seqs kept; every alert seq still resolves; a 10-minute per-source override; the next leaf untouched |
| `retention/HistoryDownsamplerIT` | Full rewrite and swap: bounds, 4 indexes attached (`pg_indexes`, `pg_inherits`), no copy, no `ds_bounds`, DONE row counts, metrics, unchanged `floorSeq`, `/v1/system` counters; `/versions` `downsampled` and the dangling `prevSeq`; a concurrent writer burst into the newest leaf during the swap; the lock-timeout path (6 × 3 s, FAILED, no orphan copy, retried successfully next run); recovery before and after a committed swap; eligibility (raw window, consumer bound, drop horizon); `trackRawWindow: null` |
| `retention/AsOfDownsampledIT` | A stationary keyframed aircraft with a 15-minute override, queried between survivors: present only through the widening, `approximate=true`, plan validity 15 min; raw plan before; STANDARD asOf unaffected |
| `retention/RetentionSettingsValidationTest` | Defaults, explicit values, `null` disables, raw window ≥ track window (explicit and default), interval bounds, malformed/zero values; per-source: STANDARD refused, TRACK stored, < 30 s and > raw window refused, null clears |
| `retention/DownsampleFidelityTest` | Range `contains`/`overlaps`/summary, failed reload keeps ranges, widening `max(ttl, interval) − ttl`, cached override and invalidation, `finalBefore` |
| `retention/HistoryDownsamplerSelectTest` | Eligibility ordering and filters; keep-set SQL shape with and without overrides |
| `trigger/TriggerTestDownsampleTest` | `DOWNSAMPLED_RANGE` only for overlapping `(fromSeq, toSeq]`; raw history and empty ranges have none |
| `store/history/AsOfResolverTest` (updated) | Widening only over a downsampled widened range; flag without widening |
| `settings/JacksonDurationIT` (updated) | New members in the exact JSON; a disabled raw window round-trips as `null`; an absent one is the default |

## Compile result

- `./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.
- `./gradlew lintOpenApi` → valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all
  operations. Two request bodies changed.
- No new dependencies.

## Notes for the next prompt

1. **057 checkpoint:**
   - run the 044 tests;
   - `HistoryDownsamplerIT.lockTimeout` takes about 20 s by design (six 3 s lock timeouts);
   - the ITs move `record_seq` with `jumpSeq`, as 019's ITs do.
2. **Read paths added later** (tiles, tracks, frames and the timeline already do this) must ask
   `AsOfResolver` / `HistoryFidelity` rather than `DownsampleRanges` directly, so a test stub still
   applies.
3. **144 (Flyway):** baseline the following.
   - Table: `history_downsample`.
   - Column: `source_settings.downsample_interval` (`interval second(6)`).
   - The copies' index names follow `rv_t_<start>_{pkey,source_id_external_id_seq_idx,source_id_observed_at_idx,geom_idx}`.
4. **Owner decisions pending:** Issues 1–2 above, plus those carried from 012–043.
