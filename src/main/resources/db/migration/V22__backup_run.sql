-- Backup & restore (prompt R2-017). Additive only.
--
-- backup_run: one row per backup, the history of GET /v1/backups. kind is the Backup schema's type: database (a
-- pg_dump custom-format archive), config (the portable settings bundle of exportSettings, no secrets) or full (both).
-- Each file is listed in files with its key, SHA-256, bytes and ETag, stored on the target (local = a directory under
-- the data directory, external = a mounted disk, s3) through the cold-archive tiers of prompt R2-016. root is the
-- directory a local or external backup was written under, so a later change of backup.root can still find it.
-- status follows the Backup schema: pending, running, complete, failed, restoring, restored.
create table backup_run (
    id varchar(26) primary key,
    kind varchar(16) not null,
    status varchar(16) not null,
    trigger varchar(16) not null,
    label text,
    target varchar(16) not null,
    root text,
    path text,
    bytes bigint,
    checksum varchar(64),
    files jsonb not null default '[]',
    error text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    started_at timestamptz,
    finished_at timestamptz,
    restored_at timestamptz
);

-- listBackups pages newest first, optionally of one kind; pruning walks one kind newest first.
create index backup_run_created_idx on backup_run (created_at desc, id desc);
create index backup_run_kind_created_idx on backup_run (kind, created_at desc, id desc);
