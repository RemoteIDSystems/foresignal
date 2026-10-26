# R2-017 — Backup & restore (DB backup + config bundle)

**Context.** v2.0 (R2) backend, Part B. Depends on R2-014 (alert on backup failure) and reuses the settings
export/import built in prompt 019. Permission `storage.manage`.

**Goal.** So a self-hosted instance can be rebuilt or migrated: a scheduled **database backup** (`pg_dump`) to a
configured target (local / external disk / S3) with its own retention of backups, plus a **config export/import
bundle** (sources, roles, settings) that is portable. Admin surface: run-now, schedule, history, restore.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-017`)

| operationId | method + path | scope |
|---|---|---|
| `getBackupSettings` | `GET /v1/settings/backup` | admin · `storage.manage` |
| `updateBackupSettings` | `PUT /v1/settings/backup` | admin · `storage.manage` |
| `listBackups` | `GET /v1/backups` | admin · `storage.manage` |
| `runBackup` | `POST /v1/backups/run` | admin · `storage.manage` |
| `restoreBackup` | `POST /v1/backups/{id}/restore` | admin · `storage.manage` |

Schemas authored: `BackupSettings`, `BackupSettingsInput`, `Backup`, `BackupPage`, `BackupRunRequest`,
`BackupRestoreRequest`.

## Read first

- `settings/SettingsTransferService.java` + `api/SettingsController.java` (`exportSettings`/`importSettings`,
  `ImportReport` — reuse these verbatim for the config bundle, including their "no credentials/secrets in a
  plain export" rules and atomic-import behavior).
- `settings/AppSettingService.java`, `settings/SettingKeys.java`, `settings/WatchdogSettingsService.java`
  (typed-settings-service pattern), `settings/BackupSettings` does not exist yet — create it.
- `security/CredentialCipher.java` / `security/SecretKeyStore.java` (encrypt S3 secrets at rest, never in the app).
- `export/ArchiveTier` from R2-016 (reuse its external-disk / S3 target abstraction for where backups land, so
  backup and archive share one target interface), `system/SystemInfoService.java` (DB connection params/size),
  R2-014 `OperationalAlertService`.

## Build

1. **Backup settings** (`ai.foresignal.backup`). `BackupSettings` record + `BackupSettingsService` over
   `AppSettingService` (key `backup`): `enabled` (default **false**), `schedule` (cron), `target`
   (`local`/`external`/`s3`), `root`/bucket, `keep` (retain N backups), `includeConfig` (also write a config
   bundle each run), encrypted `secrets` (write-only in the contract). `getBackupSettings`/`updateBackupSettings`
   (PUT replaces; validate cron, target, `keep` ≥ 1).

2. **Database backup** (`BackupService`). On the cron (and on `runBackup`), run `pg_dump` via `ProcessBuilder`
   using the datasource's host/port/db/user (password via `PGPASSWORD` in the subprocess env, never logged),
   to a timestamped, checksummed file on the configured target (reuse R2-016's `ArchiveTier` for placement).
   **Guard:** if `pg_dump` is not on `PATH`, do not fail the app — record the run as `failed` with a clear
   reason and raise `system.backup_failed` via R2-014. Record each run in a `backup_run` row (id, kind
   `db`|`config`, target, path, bytes, checksum, status, `started_at`, `finished_at`). Prune runs beyond `keep`.

3. **Config bundle.** When `includeConfig` (or a config-only `runBackup`), call
   `SettingsTransferService.exportSettings` to produce the portable bundle (sources, roles once R2-009 adds them,
   settings) and store it as a `backup_run` of kind `config`. Keep its no-plaintext-secrets guarantee.

4. **History + restore.** `listBackups` pages `backup_run` (`BackupPage` of `Backup`). `runBackup` triggers a
   run now (`BackupRunRequest` chooses db/config/both). `restoreBackup(id)`:
   - **DB restore** → `pg_restore`/`psql` of the chosen dump. This is **destructive** and may require downtime:
     admin-gated, require an explicit confirmation flag in `BackupRestoreRequest`, and document the operational
     caveat in the op description. Do not attempt an in-place restore of the live schema without the flag.
   - **config restore** → `SettingsTransferService.importSettings` (atomic; returns `ImportReport`).

**Migration.** Add the next free additive Flyway migration `V{n}__backup_run.sql` (`n` one past the highest
`V*` now present — R2-001…R2-013 claim V14 upward, read the directory). Settings live in `app_setting`. Additive only.

## Contract

The five ops are authored `planned`; implement to their schemas; secrets write-only. Extend a schema / add an
error code if needed, regenerate Postman (`python3 scripts/generate-postman.py`), but **leave
`x-foresignal-status: planned` and do not regenerate the SDKs** (R2-027 flips + regenerates). `docs/OpenAPI.json`
is the only contract.

## Tests to write (do not run)

- `BackupSettingsServiceTest` — validation + round trip; S3 secrets write-only and encrypted.
- `BackupServiceIT` — a `runBackup` writes a checksummed file and a `backup_run` row; missing `pg_dump` →
  `failed` + alert, app stays up; `keep` prunes old runs.
- `ConfigBundleIT` — the config bundle round-trips through export/import with no plaintext secrets.
- `BackupRestoreIT` — DB restore refuses without the confirmation flag; config restore returns an `ImportReport`.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

An admin can schedule and run a DB backup (+ optional config bundle) to local/external/S3 with its own
retention; `GET /v1/backups` shows history; `POST /v1/backups/run` and `POST /v1/backups/{id}/restore` work
(DB restore gated behind an explicit confirmation); a missing `pg_dump` degrades gracefully with an alert;
compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-017: Backup & restore (DB backup + config bundle)`, then
`completion-reports/R2-017-CR-Backup-Restore.md` with the 40-character implementation hash.
