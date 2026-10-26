# R2-016-CR-Cold-Storage-Archival

| | |
|---|---|
| Prompt | R2-016-Cold-Storage-Archival.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `31e718681e541ca9cccc466415daa1618d769901` |
| Date | 2026-10-26 |

## What was added

- **Migration `V21__archive_manifest.sql`** adds two tables:
  - `archive_manifest`, one row per archive segment. It holds:
    - the status, the trigger (`manual`, `schedule` or `cap`), the domain, the target and the location;
    - the cutoff, the sources, and the seq and time ranges;
    - the size, the item count and the segment checksum;
    - the `files` jsonb list (key, SHA-256, bytes, ETag);
    - `purged_from_hot` and `error`;
    - the created, updated, completed, purged and restored timestamps.
  - `record_version_restored`, the restore area. It has every `record_version` column plus `archive_id` (an FK that
    cascades on delete). Its primary key is `(archive_id, seq, history_tier)`.
- **Archive tiers** (`ArchiveTier`, with the registry in `ArchiveTiers`). Each tier can offload, verify, restore, list
  and delete. There are three:
  - `local`, which is `ExternalDiskArchiveTier`:
    - copies to a temporary file, fsyncs it, then moves it into place atomically;
    - reads the SHA-256 back to verify;
    - refuses a key that would escape the root.
  - `s3`, which is `S3ArchiveTier`:
    - uploads, then checks the ETag and size with HEAD;
    - checks the SHA-256 when it downloads.
  - `glacier`, which is `GlacierArchiveTier`:
    - uses storage class `GLACIER` by default, or `DEEP_ARCHIVE`;
    - restore first sends HEAD (`x-amz-restore`). A thawing object is pending. A frozen object gets RestoreObject
      (7 days, Standard tier) and is then pending. A thawed object is downloaded.
- **`S3Uploader`** now also does HEAD, GET, RestoreObject, DELETE and ListObjectsV2 (with continuation). It sends and
  signs `x-amz-storage-class`. There is still no AWS SDK; the existing SigV4 client is reused.
- **Settings.** `ArchiveSettings` gains three members, plus `glacier` as a target. Every problem is reported in one 400.
  - `s3.storageClass`. It must be one of the known classes. Glacier needs `GLACIER` or `DEEP_ARCHIVE`, and `s3` may not
    use either of them.
  - `cutoff`, a duration of at least 1 day.
  - `schedule`, a Spring 6-field cron expression in UTC.
- **`ArchiveRunner`**:
  - **Run:** select what is older than the cutoff, write gzip NDJSON staging files, offload them and verify them on the
    tier, write the manifest, then purge through the retention steps.
  - **Failure:** a failed run purges nothing. It marks the segment `failed` and raises `system.archive_failed`; a later
    success raises `system.archive_recovered`.
  - **One at a time:** runs are serialized with retention through the new `RetentionJob.lease()`. A second run gets
    409 ALREADY_RUNNING.
  - **Restore:** reloads a segment into `record_version_restored`, verifying each file's SHA-256 on the way.
  - **Schedule and recovery:** a 60 s `@Scheduled` tick starts scheduled runs and resumes Glacier restores that are
    pending.
- **Retention seams.**
  - `RetentionRun` takes per-source STANDARD caps for one run, and `RetentionPlanner` applies them as a minimum.
  - `RetentionCapPruner` now receives a `Passes` (plan and execute) in place of a `Function`. `ArchiveCapPruner` fills
    the R2-015 seam: when the archive is configured, the size cap archives first and then purges the same planned run.
- **API** (`ArchivesController`): `listArchives`, `runArchive` (202, dry run supported) and `restoreArchive`
  (`confirm: true` required, 202). All three stay `x-foresignal-status: planned`.
- **Tests:**
  - `ExternalDiskArchiveTierTest`;
  - `ArchiveRunnerIT`: archive then purge, failed verification, one at a time, not configured and validation, dry run,
    inventory;
  - `ArchiveRestoreIT`: rows come back unchanged, guards, a corrupted copy;
  - `ArchiveSettingsIT`: round-trip, secrets write-only and encrypted at rest, validation;
  - `RetentionArchiveSeamIT`: the cap archives then purges; with the archive off, the R2-015 behavior is unchanged.

## Contract

- **`ArchiveSettings`:**
  - `target` gains `glacier`.
  - It gains `s3.storageClass`, `cutoff` and `schedule`, with property prompts.
  - Secrets remain write-only and the response returns only `secretsSet`.
- **`Archive`** gains `trigger`, `domain`, `sources`, `fromSeq`, `toSeq`, `fromTime`, `toTime`, `files`, `error`,
  `updatedAt`, `completedAt` and `purgedAt`.
- **`runArchive`** accepts `cutoff`, `sources` and `dryRun`. Unknown members are rejected with a 400.
- **New 409 codes:**
  - `runArchive`: ARCHIVE_NOT_CONFIGURED and ALREADY_RUNNING.
  - `restoreArchive`: ALREADY_RESTORED, ALREADY_RUNNING and ARCHIVE_NOT_RESTORABLE.
- **Unchanged:**
  - `AccessPermissions` (900 rows, 550 with a permission).
  - The frozen `docs/api/v1.0.0` document.
- **Postman:** examples were added for `runArchive` and `restoreArchive`, and the `updateArchiveSettings` example was
  extended.

## Decisions and disagreements for the owner

1. **Segments are gzip NDJSON, not Parquet.** The Parquet reader needs Hadoop, which is not on the runtime classpath,
   and restore must read segments back. Prompt 111's `ArchiveStep` still writes its Parquet copies during the purge
   pass, so that data exists in two formats by design.
2. **A restore loads into `record_version_restored`, not `record_version`.** TRACK partitions are dropped, and
   retention would delete restored rows again on its next pass. **No API reads the restore area yet.** Decide whether
   one should.
3. **Names in the prompt versus the contract:**
   - The prompt's "external" target is the existing `local` target, which is the external-disk tier.
   - The prompt's `archived` status is `complete` in the contract.
4. **A manual cutoff caps only the archived STANDARD sources.** TRACK history is archived as its leaves drop under the
   normal `trackWindow`, and the size cap tightens TRACK globally.
5. **The purge reuses the run that was archived, so the cutoffs cannot drift between archive and purge.**
   - Rows the purge holds back leave the segment `complete` with a note, not `purged`.
   - The size-cap path purges even when nothing was archivable; otherwise the cap could never make progress.
6. **A dry run returns an unstored segment with status `pending`** and its counts.
7. **The Glacier thaw is fixed at 7 days on the Standard tier,** with no setting. Prompt 111's Parquet copies upload
   for `s3` only, not for `glacier`.
8. **A failed restore puts the segment back to its prior status** (`purged` or `complete`) with "The restore failed: …"
   in `error`, and raises the alert.
9. **Restore uses the current S3 settings and secrets with the segment's recorded target.** If the bucket has moved
   since the archive was taken, the restore fails.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0. `meshdtn/style/test_us_english.py`: "Ran 14 tests … OK".
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run.

## Notes for the next prompt

- **R2-017 (backup):**
  - `archive_manifest` and `record_version_restored` are new tables that a backup must include.
  - Archive segments live outside the database, on the tier, and are not part of a database backup.
- **Serializing with retention:** `RetentionJob.lease()` is the single guard for anything that must not overlap a
  retention pass.
- **Alerts:** use `OperationalAlertService` key `archive` for archive failures.
- **Migrations:** V21 is now the highest; the next is V22.
