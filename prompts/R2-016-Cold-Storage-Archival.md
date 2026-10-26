# R2-016 — Cold-storage archival (external disk first, S3/Glacier behind one interface)

**Context.** v2.0 (R2) backend, Part B. Depends on R2-015 (retention cap / overwrite-oldest, whose
DB-size path becomes archive-then-purge when a target is configured) and R2-014 (alerts on archive failure).
Builds on the existing archive subsystem (prompt 111: `export/ArchiveService`, `ArchiveStep`,
`ArchiveSettingsService`, `ArchiveCatalogService`, `/v1/settings/archive`). Permission `storage.manage`.

**Goal.** Phase aging history out of the hot database to cheaper/cold storage, then purge it locally —
**external disk first**, with **S3/Glacier behind the same pluggable interface**. The flow is: select data
older than a cutoff → export to the archive target (checksum-verified) → mark archived → purge from hot
storage. A restore path pulls an archive back when needed.

## Operations (authored, status `planned`, `x-foresignal-prompt: R2-016`)

| operationId | method + path | scope |
|---|---|---|
| `listArchives` | `GET /v1/archives` | admin · `storage.manage` |
| `runArchive` | `POST /v1/archives/run` | admin · `storage.manage` |
| `restoreArchive` | `POST /v1/archives/{id}/restore` | admin · `storage.manage` |

Plus the existing archive settings `getArchiveSettings`/`updateArchiveSettings` (`/v1/settings/archive`,
prompt 111) — **extend** them with the cold-storage config; do not break their raster/pmtiles archive
semantics. Schemas authored: `Archive`, `ArchivePage`, `ArchiveRunRequest`, `ArchiveRestoreRequest`.

## Read first

- `export/ArchiveService.java`, `export/ArchiveStep.java`, `export/ArchiveSettingsService.java`,
  `export/ArchiveCatalogService.java`, `export/ArchiveWatermarkEntity.java`, `export/ArchiveSystemBlock.java`,
  `api/ArchiveController.java` (what `/v1/archive/catalog` and `/v1/settings/archive` do today).
- `retention/RetentionResolver.java` + `retention/RetentionJob.java` + `retention/HeadPurgeStep.java` (the
  cutoff math and the only sanctioned purge path — archival reuses it, it does not delete heads itself),
  and the R2-015 overwrite-oldest strategy seam.
- `security/CredentialCipher.java` + `security/SecretKeyStore.java` + `security/Encrypted.java` (how secrets
  are encrypted at rest — S3/Glacier credentials use this, never the app), `collect/S3ListingParser.java`
  (existing S3 wire handling to reuse).
- `system/SystemInfoService.java` (`pg_database_size`), R2-014 `OperationalAlertService`.

## Build

1. **Pluggable archive tier** (`ai.foresignal.export`). An `ArchiveTier` interface:
   `offload(ArchiveSpec) -> ArchiveRef`, `verify(ArchiveRef) -> boolean` (checksum), `restore(ArchiveRef) -> ...`,
   `list()`, `delete(ArchiveRef)`. Implementations:
   - **`ExternalDiskArchiveTier` (build this first)** — writes compressed, checksummed archive files to a
     configured root (a mounted external disk). Fully functional with no cloud account.
   - **`S3ArchiveTier` / `GlacierArchiveTier`** — the same interface over object storage; credentials encrypted
     via `CredentialCipher`. Verify any AWS SDK version on Maven Central before adding it; keep the interface
     thin so a missing SDK does not break the external-disk path.

2. **Archive run.** An `ArchiveRunner` that, for data older than the cutoff (reuse `RetentionResolver`):
   exports the selected range (per domain/kind, e.g. partition dumps or `COPY`) → `verify` the checksum →
   write an `archive_manifest` row (id, domain/kind, seq range + time range, target, bytes, checksum, status
   `archived`, `created_at`) → **then** purge the range from hot storage through the existing
   `RetentionJob`/`HeadPurge` path (never a raw delete of heads/live rows; keep I7/I6). A failed verify aborts
   before any purge and raises `system.archive_failed` via R2-014. `runArchive` triggers a run now;
   a `@Scheduled` cron (from settings) runs it on a schedule. One run at a time → `409 ALREADY_RUNNING`.

3. **Restore.** `restoreArchive(id)` pulls a manifest back from its tier, re-verifies the checksum, and reloads
   the range into hot storage (or a restore area). Mark the manifest `restored`. Document that a restore may
   be large/slow and is admin-gated.

4. **Inventory + settings.** `listArchives` pages `archive_manifest` (`ArchivePage` of `Archive`: id, domain,
   range, target, bytes, checksum, status, timestamps). Extend `ArchiveSettingsService`/`getArchiveSettings`/
   `updateArchiveSettings` with the cold-tier config: `target` (external/s3/glacier), `root`/bucket, `cutoff`,
   `schedule`, encrypted `secrets`. Keep prompt 111's existing fields.

5. **Tie into R2-015.** When a cold-storage target is configured, replace R2-015's overwrite-oldest
   hard-delete with archive-then-purge via this runner (fill the strategy seam R2-015 left).

**Migration.** Add the next free additive Flyway migration `V{n}__archive_manifest.sql` (table above; `n` one
past the highest `V*` now present — R2-001…R2-013 claim V14 upward, read the directory). Additive only.

## Contract

The three new ops are authored `planned`; implement to `Archive`/`ArchivePage`/`ArchiveRunRequest`/
`ArchiveRestoreRequest`. Extend `/v1/settings/archive`'s schema with the cold-tier fields if needed, regenerate
Postman (`python3 scripts/generate-postman.py`), but **leave `x-foresignal-status: planned` on the new ops and
do not regenerate the SDKs** (R2-027 does the flip + SDK regen). `docs/OpenAPI.json` is the only contract; keep
secrets write-only in it.

## Tests to write (do not run)

- `ExternalDiskArchiveTierTest` — offload writes a verifiable checksummed file; `verify` catches corruption;
  `restore` round-trips.
- `ArchiveRunnerIT` — select-older-than → export → verify → manifest `archived` → range purged from hot storage
  with I7/I6 intact; a failed verify purges nothing and raises the alert; a second run → `409`.
- `ArchiveRestoreIT` — restore reloads a range and marks the manifest `restored`.
- `ArchiveSettingsIT` — the cold-tier config round-trips; S3 secrets are write-only and encrypted at rest.
- `RetentionArchiveSeamIT` — with a target configured, R2-015's overwrite-oldest archives-then-purges.

## What runs

Compile only — `./gradlew compileJava compileTestJava`. No test suite, no `lintOpenApi`/`checkApiCompat`.
**Do** run `./gradlew checkUsEnglish`. Write the tests but do not run them. US English.

## Done when

An admin can configure an external-disk (or S3/Glacier) target, run or schedule an archive that offloads aging
history, verifies it, records a manifest, and purges hot storage; `GET /v1/archives` lists the inventory;
`POST /v1/archives/{id}/restore` restores one; archival replaces hard-delete in R2-015's cap path when a target
is set; compile passes; `checkUsEnglish` green.

## Do not touch

Any `CLAUDE.md`/`CONVENTIONS.md`; the frozen `docs/api/v1.0.0/OpenAPI.json`; the `x-foresignal` changelog;
`.gitlab-ci.yml`; other prompts' files. **Stage only this prompt's files.**

## Commit

`R2-016: Cold-storage archival (external disk, S3/Glacier)`, then
`completion-reports/R2-016-CR-Cold-Storage-Archival.md` with the 40-character implementation hash.
