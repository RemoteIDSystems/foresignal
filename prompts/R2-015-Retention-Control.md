# R2-015 — Retention control (cap history / overwrite-oldest) + preview

**Context.** v2.0 (R2) backend, Part B. Depends on R2-014 (operational alerting, for a cap breach notice)
and on the existing retention subsystem (prompt 019) and frame-store eviction (prompt 042). Permission
`storage.manage`.

**Goal.** The `retention/` machinery already drops track partitions, chunk-deletes standard history, and the
frame store already evicts oldest frames — but each has its own window and there is no single admin control
and no "what would this prune?" preview. Expose one simple policy: a **cap-history** switch with a single
knob — "keep at most N days" and/or "keep the database under N GB, overwrite oldest first" — that drives the
existing per-subsystem windows (records, frames, media, chronicles). **Default off (keep everything).**

## Operations

| operationId | method + path | scope | status |
|---|---|---|---|
| `previewRetention` | `GET /v1/settings/retention/preview` | admin · `storage.manage` | **planned (R2-015)** |
| `getRetention` | `GET /v1/settings/retention` | admin | already implemented (019) |
| `updateRetention` | `PUT /v1/settings/retention` | admin | already implemented (019) |
| `runRetention` | `POST /v1/settings/retention/run` | admin | already implemented (019) |

New schema already authored: `RetentionPreview`. Extend `RetentionSettings` with the cap fields (below).

## Read first

- `settings/RetentionSettings.java` (the record + shorthand duration serialization), `retention/RetentionSettingsService.java`
  (`get`/`update`/`parse`/`apply`, `MIN_WINDOW`), `retention/RetentionResolver.java` (window + cutoff math),
  `retention/RetentionJob.java` + the ordered `RetentionStep` beans (`StandardHistoryStep`, `TrackPartitionStep`,
  `HeadPurgeStep`, …), `retention/RetentionRunResult.java`.
- `raster/RasterRetentionStep.java` and `media/MediaRetentionStep.java` (the frame/media eviction this policy
  must unify), `raster/RasterProperties.java`.
- `system/SystemInfoService.java` (`pg_database_size`), prompt 019's Completion Report.

## Build

1. **Cap policy fields on `RetentionSettings`.** Add `capHistory` (boolean, default **false**), `maxDays`
   (nullable duration/int days), `maxDbGb` (nullable), `overwriteOldest` (boolean, default true when a cap is
   set). Keep the existing shorthand serialization and the current per-kind/per-source windows untouched —
   the cap is an **upper bound layered over** them, never a replacement. Validate in `RetentionSettingsService`:
   `maxDays` ≥ `MIN_WINDOW`; `maxDbGb` > 0; collect all errors into one `400`.

2. **Drive the existing subsystems from the cap.** When `capHistory` is on, `RetentionResolver` clamps every
   effective window to `maxDays` (records, frames, media, chronicles alike), so the hourly `RetentionJob` and
   the `RasterRetentionStep`/`MediaRetentionStep` prune to the single policy. Keep I7/I6 and the consumer-clamp
   invariants from 019 intact — the cap only ever *tightens* a cutoff, never bypasses the clamp.

3. **Database-size cap (overwrite-oldest).** In `RetentionJob`, after the window pass, if `maxDbGb` is set and
   `pg_database_size` exceeds it and `overwriteOldest` is on, advance cutoffs oldest-first (step the effective
   window down in bounded increments, re-measuring) until the database is under the cap or the floor can go no
   lower. This unifies the frame-store's existing oldest-eviction with record/chronicle pruning under one knob.
   Record what it did in `RetentionRunResult` (bytes reclaimed, windows applied). If it cannot reach the cap
   (e.g. everything is already at the floor), raise the R2-014 operational alert `system.retention_cap_unmet`
   rather than deleting live/current state.

4. **`previewRetention`.** A read-only endpoint that computes, **without deleting anything**, what the current
   policy would prune on the next run: per subsystem (records, frames, media, chronicles) the row/frame counts
   and byte estimates, the effective cutoff(s), and whether the DB-size cap is currently exceeded. Use the same
   selects `RetentionJob` uses but as `SELECT count/size` only. Return `RetentionPreview`. When `capHistory` is
   off, the preview reflects only the existing per-subsystem windows (which is what runs today).

5. **Archive-aware (forward hook).** Leave a seam so that when R2-016 configures a cold-storage target, the
   DB-size cap archives-then-purges instead of hard-deleting oldest. R2-016 wires it; here, just don't make the
   overwrite-oldest path assume hard delete is the only option (route it through a strategy the archival prompt
   can replace).

**Migration.** `retention` is an `app_setting` row — the new cap fields serialize into it, so **no new table**.
Only add a Flyway migration if you introduce a persisted preview cache (you should not); if you do, use the next
free `V{n}` (R2-001…R2-013 claim V14 upward — read the directory).

## Contract

`previewRetention` is authored `planned`; implement to `RetentionPreview`. Keep the three existing retention ops
implemented and unbroken. You may extend `RetentionSettings` and `RetentionPreview` schemas with the cap fields
(they are authored — confirm the fields match; add any you need), regenerate Postman with
`python3 scripts/generate-postman.py`, but **leave `x-foresignal-status: planned` on `previewRetention` and do
not regenerate the SDKs** (R2-027 does the flip + SDK regen). `docs/OpenAPI.json` is the only contract.

## Tests to write (do not run)

- `RetentionSettingsCapTest` — validation of `maxDays`/`maxDbGb`; cap off = today's behavior unchanged.
- `RetentionResolverCapTest` — the cap tightens every window; never loosens one; never below the floor.
- `RetentionPreviewIT` — preview counts match what a run would prune, and preview deletes nothing.
- `RetentionDbCapIT` — with `maxDbGb` set and the DB over it, the run advances cutoffs oldest-first and records
  bytes reclaimed; an unmet cap raises the alert instead of touching current state.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`
(R2-027 runs them). **Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

An admin can switch on a single cap (N days and/or N GB, overwrite oldest) that drives records/frames/media/
chronicles from one control; `GET /v1/settings/retention/preview` reports what would be pruned without deleting;
the default is off (keep everything); 019's invariants hold; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-015: Retention control (cap history / overwrite-oldest) and preview`, then
`completion-reports/R2-015-CR-Retention-Control.md` with the 40-character implementation hash.
