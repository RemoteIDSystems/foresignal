# R2-017-CR-Backup-Restore

| | |
|---|---|
| Prompt | R2-017-Backup-Restore.md |
| Status | Complete (compile only; `checkUsEnglish` run and green; no test, `lintOpenApi` or `checkApiCompat` was run) |
| Implementation commit | `40a736dca61be3c6635e316cf345b43386a7e664` |
| Date | 2026-10-26 |

## What was added

- **Migration `V22__backup_run.sql`** creates `backup_run`, one row per backup. Its columns:
  - the id (a ULID), `kind`, `status`, `trigger`, `label` and `target`;
  - `root`, the directory a disk backup was written under, so a later change of `backup.root` can still find it;
  - `path`, `bytes` and `checksum`;
  - `files` (jsonb: name, key, location, SHA-256, bytes, ETag) and `error`;
  - the created, updated, started, finished and restored timestamps.

  It has two indexes: one for newest-first paging, and one for paging and pruning by kind.
- **Settings** (`ai.foresignal.backup.BackupSettings` and `BackupSettingsService`, under the new key
  `SettingKeys.BACKUP` = `backup`, added to `ALL`):
  - The fields are `enabled` (default false), `schedule` (a six-field cron expression in UTC), `target`
    (`local`/`external`/`s3`), `root`, `s3`, `keepBackups` (default 7; null keeps every backup), `includeConfig`
    (default true) and the encrypted secrets.
  - A PUT replaces the settings. An omitted member takes its default, except the secrets: an omitted secret is kept,
    and an empty string or null clears it.
  - Every problem is reported in one 400:
    - `enabled` and `target` are required;
    - `external` needs an absolute `root`;
    - `s3` needs an endpoint and a bucket;
    - `keepBackups` must be 1–10000;
    - unknown members are rejected; the read-only `secretsSet` and `lastRunAt` are ignored.
  - The S3 endpoint must pass the outbound URL policy.
  - **One target interface:** `BackupSettings.tierSettings()` expresses the backup target as an R2-016
    `ArchiveSettings`. `local` and `external` both use the external-disk tier (copy, fsync, SHA-256 read back); `s3`
    uses the S3 tier (ETag and size checked). The secrets are decrypted by the same `CredentialCipher` path.
- **`BackupService`:**
  - **Run:** `start()` saves a `pending` row. A single worker then:
    - runs `pg_dump --format=custom --no-owner --no-privileges` with host, port, database and user from the
      `JdbcConnectionDetails` bean (the same source as `IngestWriter`). The password goes only into the subprocess's
      `PGPASSWORD`; it is never put on the command line or logged;
    - writes the `exportSettings` bundle (`SettingsTransferService.export()`, unchanged, no secrets);
    - offloads each file to `backups/{id}/database.dump` and/or `backups/{id}/config.json` and verifies it;
    - marks the row `complete`;
    - prunes per type.
  - **Failure:** a failed backup is marked `failed` with its `error` and raises `system.backup_failed`. The next success
    raises `system.backup_recovered` (R2-014 `OperationalAlertService`, key `backup`).
  - **Missing `pg_dump`:** the `PATH` is checked first. If `pg_dump` is absent, the backup fails with that reason and
    the server keeps running.
  - **Schedule:** a 60 s `@Scheduled` tick fires on the cron, gated by `foresignal.backup.scheduled`, which is off in
    tests.
  - **Pruning:** keeps the newest `keepBackups` stored backups of each type and deletes the rest with their files.
    Failed rows older than the oldest kept backup are dropped.
  - **Restore:**
    - `config`: imported synchronously through `SettingsTransferService.importBundle` (atomic). The response carries
      the `ImportReport`. A missing or corrupt copy is 409 BACKUP_NOT_RESTORABLE, the reason is kept in `error`, and
      the alert is raised.
    - `database` and `full`: run on the worker under `RetentionJob.lease()`, so no retention or archive pass overlaps.
      The worker runs `pg_restore --clean --if-exists --no-owner --no-privileges --single-transaction
      --exit-on-error`, then `Flyway.migrate()` and `AppSettingService.invalidateCache()`, then writes the row back as
      `restored`. That write-back is needed because the restore replaced `backup_run` with the dump's copy.
    - A failed restore puts the previous status back, with "The restore failed: …" in `error`.
  - **One at a time:** one backup or restore runs at a time; a second is 409 ALREADY_RUNNING.
  - **Configuration:** `foresignal.backup.pg-dump`, `pg-restore` (env `FORESIGNAL_BACKUP_PG_DUMP` /
    `FORESIGNAL_BACKUP_PG_RESTORE`) and `timeout` (6h).
- **API** (`api/BackupsController`, tag `Backups`): the five operations. All stay `x-foresignal-status: planned`.
- **Tests (written, not run):**
  - `BackupSettingsServiceTest` (unit, real cipher): defaults, round trip, encrypted at rest, replace semantics,
    every validation error, tier mapping.
  - `BackupServiceIT`:
    - a full backup writes checksummed files and a row, and `pg_dump` receives the password only through its
      environment;
    - a missing `pg_dump` fails with an alert while the server stays up, and a later config backup recovers;
    - `keepBackups` prunes;
    - the configured and one-at-a-time gates;
    - parsing the libpq options.
  - `ConfigBundleIT`: the bundle has no plaintext secret and restores the CORS settings and the channel (whose secret
    is kept); a corrupted bundle gives 409.
  - `BackupRestoreIT`:
    - a restore without `confirm: true` runs nothing;
    - a confirmed restore runs `pg_restore` with the expected flags;
    - a failed `pg_restore` keeps the backup restorable;
    - a config restore returns the `ImportReport`;
    - 404 for an unknown id; 409 for a failed backup and while the retention lease is held.
  - The ITs use stand-in `pg_dump` and `pg_restore` shell scripts (`BackupService.commands`), so no PostgreSQL client
    is needed where the tests run.

## Contract

- **`BackupSettings` and `BackupSettingsInput`:**
  - They gain `includeConfig`.
  - `keepBackups` has a default of 7, with minimum and maximum.
  - The field descriptions now say what each target means.
  - `BackupSettingsInput` describes the replace semantics.
- **`Backup`** gains `trigger`, `files`, `error`, `startedAt`, `finishedAt`, `restoredAt` and `importReport` (which
  references the existing `ImportReport`). `type`, `status`, `checksum` and `location` are described.
- **`BackupRunRequest`:** `type` describes its default, and `label` has `maxLength` 200.
- **Responses:**
  - `listBackups` gains a 400 for a bad type, cursor or limit.
  - `runBackup`'s 409 now lists BACKUP_NOT_CONFIGURED and ALREADY_RUNNING.
  - `restoreBackup` gains a 409 (ALREADY_RUNNING, BACKUP_NOT_RESTORABLE, BACKUP_NOT_CONFIGURED).
  - The operation descriptions carry an R2-017 paragraph. `restoreBackup`'s states the operational caveat.
- **Unchanged:**
  - `AccessPermissions` (900 rows, 550 with a permission).
  - The frozen `docs/api/v1.0.0` document.
- **Postman:** added examples for `updateBackupSettings`, `runBackup` and `restoreBackup`.

## Decisions and disagreements for the owner

1. **The shipped image has no `pg_dump`.** The runtime stage installs only `curl` (and optionally `gdal-bin`). The
   database is PostgreSQL 17 (`docker-compose.yml:31`), and `pg_dump` refuses to dump a newer server than itself.
   - As shipped, every `database` or `full` backup in the container fails, cleanly, with "pg_dump was not found on the
     PATH" and the alert. Config backups work.
   - I did not edit the Dockerfile. It is not this prompt's file, and I have not verified which PostgreSQL 17 client
     package the `eclipse-temurin:25-jre` base can install.
   - **Decide whether the image should carry a PostgreSQL 17 client, or whether backups should run `pg_dump` inside the
     database container.**
2. **The contract and the prompt name things differently, and the code follows the contract:**
   - types `database`/`config`/`full`, not `db`/`config`;
   - `keepBackups`, not `keep`.

   `includeConfig` was not in the contract, so I added it. `full` is how `includeConfig` shows up in the history.
3. **The prompt assumed R2-009 added roles to the bundle. It did not.** R2-009 was per-user channels, and roles live in
   `security/user/RoleAdminService`.
   - The bundle is the prompt-019 export, unchanged, as the prompt asked ("reuse verbatim").
   - It therefore carries no RBAC roles or users. A `database` backup does carry them.
   - Decide whether `exportSettings` should gain roles.
4. **A database restore runs in place, into the live database.**
   - It uses `--clean --if-exists --single-transaction`, so it is all or nothing.
   - The caveat is in the operation description: writes wait on its locks, anything written after the dump is lost,
     and the server should be restarted afterwards.
   - It does not stop the server or pause collection itself.
5. **`full` restores the database dump only.** The config bundle is already inside the dump, secrets included. The
   bundle's `config.json` is the portable copy for moving to another instance; restore it on its own through a `config`
   backup.
6. **`runBackup` requires `enabled: true`.** `enabled` is the master switch and `schedule` only times the runs. A restore
   does not require `enabled`.
7. **Default `includeConfig` is true.** The bundle is small, and it is the portable part.
8. **A restore of an S3 backup uses the current bucket settings and secrets,** as R2-016 does. A disk backup uses its
   own recorded root.
9. **Shared error codes.** ALREADY_RUNNING reuses `ConflictException.ALREADY_RUNNING`. The new codes BACKUP_NOT_RESTORABLE,
   BACKUP_NOT_CONFIGURED and UNKNOWN_BACKUP are constants on `BackupService`, the same way R2-016 keeps the archive
   codes on `ArchiveRunner`.

## What runs

- `./gradlew compileJava compileTestJava`: exit 0.
- `./gradlew checkUsEnglish`: exit 0 after one fix. A test's example of an unknown member used British spelling and was
  renamed `flavor`.
- `scripts/generate-access-permissions.py`: "900 rows (550 with a permission)", no diff.
- The static access check (`emulate.py`) printed "0 problems".
- `./gradlew generatePostman`: "936 requests across 90 tag folders; quickstart 12 steps; covers all ops: True".
- No test, `lintOpenApi` or `checkApiCompat` was run. `pg_dump` and `pg_restore` have never been executed against a
  real database in this pass.

## Notes for the next prompt

- **Migrations:** V22 is now the highest; the next is V23.
- **Serializing:** `BackupService.running()` reports a backup or restore in progress. A database restore also holds
  `RetentionJob.lease()`.
- **Targets:** backups and archives share `ArchiveTiers`. A new target type is one more `ArchiveTier` bean, plus a
  mapping in `BackupSettings.tierSettings`.
