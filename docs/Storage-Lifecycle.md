# Storage, data lifecycle & archival

The operational maturity a self-hosted instance needs so it never fills its disk, keeps only what the
operator wants, moves cold data off the hot database, and can be backed up and restored. This is the
design the Release 2 prompts (backend 177–180) build to; `docs/OpenAPI.json` (API **2.0.0**) is the
contract, and the running system is what is true.

It is built **on top of** the retention machinery that already exists — not a parallel system. The job
here is to put a disk-safety floor under it, give the operator one simple control over it, add a cold
tier below it, and make the whole database backup-and-restorable.

## What exists today (verified 2026-10-26)

| Capability | Where | What it does |
|---|---|---|
| Record retention | `retention/RetentionJob.java`, `RetentionPlanner`, `RetentionResolver`, `RetentionSettingsService` | hourly job; per-analytic-class windows; downsampling; `HeadPurgeStep`, `StandardLeafDropStep`, `TrackPartitionStep` |
| Retention settings | `RetentionSettings` schema; `GET/PUT /v1/settings/retention`, `POST /v1/settings/retention/run` | operator-settable windows per class, per source, per kind |
| Consumer clamp | `retention/RetentionConsumer.java`, `RetentionConsumerProvider` | retention never deletes rows a durable consumer has not processed (RET-2) |
| Raster/media byte quota | `raster/FrameStore.java` (prompt 042) | a byte cap on the frame store; raises `/v1/system` warnings and **evicts oldest frames** |
| System info | `system/SystemInfoService.java` | DB size, counts, budget outlook |

What is **missing** and this design adds: host **disk free-space** monitoring, a single **cap-history /
overwrite-oldest** control, a **cold-storage archive** tier, and **backup/restore** tooling.

## A. Disk & resource monitoring, thresholds, and alerting

A host-level monitor reads **disk free space** (`java.nio.file.FileStore.getUsableSpace()` on the data
directory), **memory**, and the **database size** already in `SystemInfoService`. It exposes one gauge
object per resource and fires alerts on breach.

- **Endpoint:** `GET /v1/system/resources` (`getResourceStatus` → `ResourceStatus`): a `disk` gauge, a
  `memory` gauge, a `database` reading, a worst-of `status`, human-readable `warnings`, and `checkedAt`.
  Each `ResourceGauge` carries `totalBytes`, `usedBytes`, `freeBytes`, `usedPercent`, and a
  `status` of `ok` / `warn` / `critical`.
- **The slider — admin threshold setting.** "Warn when free disk drops below N% / N GB," with a second
  **critical** threshold. Thresholds are an admin setting (`storage.manage`), so the operator tunes the
  box to its hardware.
- **Breaches both surface and alert.** A breach raises a `/v1/system` warning **and** fires an alert
  through the existing channel system (email/push/webhook/MQTT/…) to users with `audit.read` / Admin —
  reusing the notification plumbing rather than inventing a second path.
- **Generalized health alerting.** The same mechanism carries **source-failure storms** and **collector
  backlog**, so any system-health condition can alert through channels, not only disk.
- The admin **Storage** screen shows live disk/memory/DB gauges from `getResourceStatus`.

## B. Retention control — cap history / overwrite-oldest

The existing retention machinery is powerful but low-level (per-class, per-source, per-kind windows).
This exposes it as **one simple admin control** without replacing it.

- A **checkbox to cap history** and a single knob: "keep at most **N days**" and/or "keep the database
  under **N GB**, overwrite oldest first." The knob drives the per-subsystem windows
  (`RetentionSettings`: records, frames, media, chronicles) underneath.
- **Default off** (owner decision): a fresh instance **keeps everything**; the cap is opt-in. History
  cannot be backfilled from live feeds, so the safe default is to retain.
- **Preview before it bites.** `GET /v1/settings/retention/preview` (`previewRetention` →
  `RetentionPreview`, a list of `RetentionPreviewItem`) shows what a given policy *would* prune before
  the operator commits it — no silent deletion.
- **Unifies the frame-store eviction.** The raster/media quota in `FrameStore` already evicts oldest
  frames; this folds that into one coherent policy with one admin UI, so "overwrite oldest" means the
  same thing for records, frames and media.
- Writes go through `GET/PUT /v1/settings/retention` and `POST /v1/settings/retention/run`; the cap is
  expressed in the existing `RetentionSettings` fields, not a new parallel setting.

**The archive interlock:** when a cold-storage target is configured (C), "overwrite oldest" becomes
**archive-then-purge** instead of hard-delete-oldest — aging data is offloaded and verified before it
leaves the hot database.

## C. Cold-storage archival

A pluggable **archive tier** that phases aging data out of the hot database to cheaper/cold storage,
then purges it locally — so the hot database stays small and fast while history is kept.

- **Pluggable target, external-disk first** (owner decision): build the local/external-disk tier first,
  with **S3/Glacier** behind the same interface. `ArchiveSettings` (`GET/PUT /v1/settings/archive`):
  `enabled` (default false), `target` (`local` / `s3`), `root` (default
  `${FORESIGNAL_DATA_DIR}/archive`), a `sources` allowlist (empty = all non-private STANDARD and TRACK
  sources), `keepLocal`, and write-only `secrets` for S3.
- **Flow:** select data older than a cutoff → export to the archive target → **verify by checksum** →
  mark archived → purge from hot storage. Private sources are excluded by default (the allowlist omits
  them), keeping EGR-1 intact.
- **Inventory and restore.** `GET /v1/archive/catalog` (`getArchiveCatalog` → `ArchivePartition`
  pages) lists archived partitions and files with sizes; a **restore** path (`ArchiveRestoreRequest`)
  pulls an archive back into the hot database when it is needed again. The existing chronicle archive
  (`POST /v1/chronicles/{chronicleId}/archive`) is the same tier applied to a chronicle's own history.
- The admin configures target, cutoff, schedule, and sees the archive inventory + sizes on the Storage
  screen.

## D. Backup & restore

Full-instance recoverability, separate from the record-archival tier.

- **Scheduled database backup** (`pg_dump` / base backup) to a configured target. `BackupSettings`
  (`GET/PUT /v1/settings/backup`): `enabled`, a cron `schedule`, `target` (`local` / `external` / `s3`),
  `root`, `keepBackups` (older backups pruned), write-only `secrets`, and `lastRunAt`.
- **Config export/import** — sources, roles, settings as a portable bundle (the existing
  `exportSettings` / `importSettings`) — so the instance can be rebuilt or migrated independently of the
  data.
- The admin **Storage → Backup** screen: run-now, schedule, history, and restore
  (`BackupRunRequest`, `BackupRestoreRequest`).

## API surface (API 2.0.0)

| Operation | Method / path | Scope | Status in spec |
|---|---|---|---|
| `getResourceStatus` | `GET /v1/system/resources` | admin | planned |
| `getRetention` / `updateRetention` | `GET` / `PUT /v1/settings/retention` | admin | implemented |
| `previewRetention` | `GET /v1/settings/retention/preview` | admin | planned |
| `runRetention` | `POST /v1/settings/retention/run` | admin | implemented |
| `getArchiveSettings` / `updateArchiveSettings` | `GET` / `PUT /v1/settings/archive` | admin | implemented |
| `getArchiveCatalog` | `GET /v1/archive/catalog` | admin | implemented |
| `getBackupSettings` / `updateBackupSettings` | `GET` / `PUT /v1/settings/backup` | admin | planned |

All storage operations are `admin` scope, permission `storage.manage`.

## Invariants this design must keep

Extend `docs/Invariants.md` (the implementing prompts register them with test names):

| Id | Invariant |
|---|---|
| STO-1 | Retention and overwrite-oldest never delete rows a durable consumer has not processed (extends RET-2, the consumer clamp) |
| STO-2 | A head is purged only when every version of it is gone (extends RET-1), whether the trigger is a window, the capacity cap, or archive-then-purge |
| STO-3 | Archive-then-purge removes data from hot storage only after the archived copy is written and its checksum verified; a failed or unverified archive never purges |
| STO-4 | Private sources are never archived to a shared/cold target unless explicitly allowlisted; archival honors EGR-1 |
| STO-5 | A disk or resource breach both raises a `/v1/system` warning and, when channels are configured, fires an alert to `audit.read`/Admin |
| STO-6 | The default install caps nothing and backs up nothing until the operator opts in; nothing is deleted or moved by default |
