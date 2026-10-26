# R2-015-CR-Retention-Control

| | |
|---|---|
| Prompt | R2-015-Retention-Control.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `f10bc7813f53854c6c9975e5b5b3e5abd239d0dc` |
| Date | 2026-10-26 |

## What was added

- **No migration.** The cap fields serialize into the existing `retention` `app_setting` row. The size cap's working
  state is a second row under a new internal key, `retention.dbCap` (`SettingKeys.RETENTION_DB_CAP`, added to
  `SettingKeys.ALL`). V20 is still the highest migration.
- **Cap fields on `RetentionSettings`:**
  - `capHistory` (boolean, default false), `maxDays` (whole days, nullable), `maxDbGb` (nullable) and
    `overwriteOldest` (default true).
  - The old 16-argument constructor is kept, so every existing caller still means "no cap".
  - New helpers:
    - `capWindow()` is the day cap in force: `maxDays`, but only when `capHistory` is on.
    - `capped(window)` never lengthens a window.
    - `effectiveTrackWindow()` is the TRACK window under the cap.
    - `capBytes()` gives the size cap in bytes; 1 GB = 1024³ bytes, the same as the raster quota.
    - `withMaxDays(days)` returns the settings with the cap tightened to `days`.
  - The JSON helpers are `@JsonIgnore`d. The existing shorthand duration serialization is unchanged.
- **Validation** (`RetentionSettingsService`), with every problem reported in one 400:
  - `capHistory` and `overwriteOldest` must be booleans.
  - `maxDays` must be a whole number from 1 to 36,500. One day is above the 1 h `MIN_WINDOW`.
  - `maxDbGb` must be above 0.
  - `capHistory: true` with neither bound is an error.
- **One control drives every subsystem:**
  - **Resolution:** `RetentionResolver.resolve` now returns `global.capped(uncapped)`, so the cap applies on top of
    each per-kind, per-source, descriptor and default window. The unregistered default and `largestWindow` are capped
    too. A new `standardWindows(RetentionSettings)` overload lets a run plan from its own settings.
  - **Records and TRACK:**
    - `RetentionPlanner` plans from the run's own settings and uses `effectiveTrackWindow()`.
    - So do `TrackPartitionStep`, `HistoryDownsampler`, `DownsampleFidelity`, `HistoryStats`,
      `SourceIngestProfile` (keyframes, I6) and `SourceMapper.effectiveRetention`.
    - The consumer clamp (`run.clamp`) still applies after the cap. The cap only ever moves a cutoff earlier in
      time, never past the clamp.
  - **Frames:**
    - `RasterRetentionStep` keeps its per-product ladders, then deletes every frame whose `valid_time` is older than
      the day cap.
    - That pass uses a keyset walk (`RasterFrameQueries.oldestBefore`) and skips pinned frames.
  - **Media:** `MediaRetentionStep` wraps each of its windows in `capped(...)`.
  - **Chronicles:**
    - `ChronicleRetentionJob` takes the resolver.
    - `keep(retention, cap)`, now public static, bounds `forever` and any longer retention by the day cap.
    - A running chronicle is never touched.
    - The job is still the only code that deletes chronicles (CHR-1).
- **Database-size cap** (`RetentionDbCap`, called by `RetentionJob` after the window pass):
  - `pg_database_size` does not fall after a DELETE; it falls only after a partition drop or a table rewrite. So the
    cap is a ratchet, not a "delete until it falls" loop:
    - Each run first applies the last stepped-down window (`tighten`).
    - When the database is **over the cap and larger than at the last run**, the cap halves the window and prunes
      again, re-measuring after each step. Steps are whole days, at most 8 per run, never below the 1-day floor.
      Stepping stops at the cap, or as soon as a step does not shrink the database.
    - Over the cap but not growing, it **holds** the window, because the freed space is being reused.
    - Below 0.8 × the cap, it **relaxes**: the window doubles back toward the configured one.
  - **When the cap cannot be met,** the run raises the R2-014 operational alert `system.retention_cap_unmet`
    (`warning`) once, through `OperationalAlertService`. This happens at the floor while still over the cap, or when
    `overwriteOldest` is off. The alert body says which cause applies. `system.retention_cap_met` clears it.
  - Its key is `retention_cap`.
  - Switching the size cap off clears the working state.
- **R2-016 seam:**
  - `RetentionCapPruner` is a functional interface, `prune(tightenedSettings, steps)`.
  - The default, `RERUN`, reruns every retention step under the tightened settings.
  - `RetentionJob` takes it through an `ObjectProvider`, so R2-016 replaces it by registering a bean. The prune is not
    assumed to be a hard delete.
- **`RetentionRunResult`** gains:
  - `capWindowDays`: the day cap the run applied, null when uncapped;
  - `dbCap`: an `Outcome` holding the cap, the sizes before and after, `bytesReclaimed`, `windowDays`, `steps`,
    `action` (`under`, `stepped`, `holding`, `floor` or `disabled`) and the rows deleted per table.
  - A 14-argument compatibility constructor is kept.
- **`GET /v1/settings/retention/preview`** (`RetentionController.previewRetention`, served by
  `RetentionPreviewService`):
  - It computes with the settings the next run would apply, including a window the size cap has stepped down to, on
    a scratch `RetentionRun`. It deletes nothing.
  - Its items:
    - **`records`, one per kind:**
      - counts come from the planner's own standard scopes and cutoffs, run as `SELECT count(*)`;
      - bytes are estimated from the average row size of the `record_version` partition tree;
      - `cutoff` is now minus the longest window for that kind.
    - **`track`:** the leaves that would be dropped, using `pg_class.reltuples` and `pg_total_relation_size`.
    - **`frames`:** the ladder plan plus the cap's age cut, with pinned frames excluded and frames counted once.
    - **`media`:** `MediaRetentionStep.preview`, built on the new `MediaQueries` totals queries.
    - **`chronicles`:** stopped chronicles past `keep(...)`.
  - It also returns a `dbCap` section: the cap, the current size, whether the cap is exceeded, the working window and
    `overwriteOldest`. The section is null when no size cap is set.

## Contract

- **Schema changes, all additive:**
  - `RetentionSettings` gains `capHistory`, `maxDays`, `maxDbGb` and `overwriteOldest` (decision 1).
  - `RetentionRunResult` gains `capWindowDays` and `dbCap`.
  - `RetentionPreview` gains `dbCap`, and its fields and its items' fields got descriptions.
  - The `previewRetention` description was rewritten.
- `previewRetention` stays `x-foresignal-status: planned`. The other three retention operations keep their paths and
  shapes.
- The frozen v1.0.0 spec and the changelog history were not touched.
- `AccessPermissions` was regenerated with no change: 900 rows, 550 with a permission. `previewRetention` already
  carried `storage.manage`. The static access check printed "0 problems".
- **Postman:** regenerated. The `updateRetention` example body in `scripts/generate-postman.py` now shows the four
  cap fields, with `capHistory: false`. The SDKs were not regenerated.
- **Tests written, not run:**
  - `RetentionSettingsCapTest`:
    - off by default;
    - a cap that is set but switched off has no effect;
    - when on, the cap clamps longer windows only;
    - binary GB;
    - every validation rule in one 400;
    - `withMaxDays`;
    - a JSON round trip.
  - `RetentionResolverCapTest`:
    - with the cap off, resolution is unchanged;
    - with it on, every longer window (kind, descriptor, track, default) is clamped;
    - a shorter window is never lengthened;
    - the 1-day floor;
    - a run's own settings drive the plan windows;
    - the chronicle bound.
  - `RetentionDbCapTest`, a unit test using a scripted size probe:
    - off, under, stepping until a step does not shrink, stopping at the cap, holding, relaxing back to the
      configured window, the floor and disabled;
    - the base window.
  - `RetentionPreviewIT`:
    - the preview's count for a kind equals what the run then deletes, and the preview deletes nothing;
    - `maxDays: 30` caps the 365-day kind, in both the preview and the run;
    - the endpoint returns the preview to an admin token.
  - `RetentionDbCapIT`, with a cap of about 1 KB so the database is over it:
    - the run steps down and persists its working state;
    - at the floor, `system.retention_cap_unmet` fires once, does not re-fire, and an entity reported within the day
      keeps its head and version;
    - `overwriteOldest: false` reports `disabled`;
    - `system.retention_cap_met` fires on recovery;
    - switching the cap off clears the state.

## Decisions and disagreements for the owner

1. **The prompt says the cap fields are already authored. They were not.** The `RetentionSettings` schema had no cap
   members, so I added them, as the prompt allows ("add any you need"). Two choices are mine:
   - `maxDays` is whole days, not a duration string;
   - `overwriteOldest` defaults to true always, rather than "when a cap is set". It has no effect while no cap is set.
2. **`bytesReclaimed` is often 0, and that is accurate.** It is the measured `pg_database_size` before the cap step
   minus the size after. After plain DELETEs, Postgres keeps the freed pages for reuse, so the figure moves only when
   a TRACK partition is dropped. That is also why the cap only steps down while the database is growing. Without that
   rule, each run would halve the window until the floor and delete nearly all history. The rows deleted per table
   are in `dbCap.deleted`. Should the cap run `VACUUM FULL` or `pg_repack`? I did not: `VACUUM FULL` takes an
   exclusive lock on the table.
3. **The cap removes current records of entities that are not reported.**
   - Head purge (I7, `HeadPurgeStep`) removes the head and live row of an entity not reported within its window.
   - A day cap shortens that window, so a 30-day cap also drops the current record of something last seen 31 days
     ago.
   - The size cap never goes below 1 day, so anything reported within a day keeps its current record.
   - The prompt says "rather than deleting live/current state". I read that as "do not go below the floor"; the cap
     does not override I7. Say if heads should be exempt from the cap.
4. **The cap covers only data history:** records, TRACK history, frames, media and chronicles. It does not cover
   alerts, audit, notifications, usage, provenance or the other operational windows. That keeps the audit trail
   (RBAC-19) out of reach of a size squeeze. Should any of those join the cap?
5. **Chronicles:** while the cap is on, a stopped chronicle set to `forever` is deleted once it is older than the cap.
   That changes what `forever` means. Should chronicles be exempt?
6. **Frames:**
   - The cap's age cut uses `valid_time`.
   - Pinned frames are kept, even when they are older than the cap.
   - The raster disk quota (prompt 042) still evicts on its own; the preview does not predict quota eviction.
   - So "frames" are unified with records by window, but the quota is still a separate knob.
7. **The preview has no `source` field,** so records are grouped per kind across sources, with the longest window of
   that kind as the cutoff.
8. **The size-cap working state persists in `app_setting` under `retention.dbCap`.** It is internal. It is not in
   the settings export, and it is cleared when the size cap is switched off.
9. **I corrected one of my own lines before committing.** I had first written that current state "is never removed
   for the cap". That is false, for the reason in item 3. The Javadoc, the alert text and the `maxDbGb` description
   now state what happens.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- **R2-016:** to archive before pruning, register a `RetentionCapPruner` bean. It receives:
  - the tightened `RetentionSettings`;
  - a function that runs every retention step under them.
  Archive what the tightened window would remove, then call the function. Prompt 111's archive step already runs
  inside that re-run.
- `RetentionRunResult.dbCap()` records each size-cap action.
- `RetentionSettings.capped(window)` is the one way to apply the day cap to any new history window.
- V20 is still the highest migration; the next is V21.
