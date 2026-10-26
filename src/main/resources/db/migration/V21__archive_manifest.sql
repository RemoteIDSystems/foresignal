-- Cold-storage archival (prompt R2-016). Additive only.
--
-- archive_manifest: one row per archive segment, the inventory of GET /v1/archives. A segment is the history one
-- archive run moved out of the hot database: one gzip-compressed NDJSON file per source, each listed in files with its
-- key, seq range, rows, bytes and SHA-256, offloaded to the tier named by target (local = the external-disk root, s3,
-- glacier) and verified there before retention purged the rows. status follows the Archive schema: pending, running,
-- verifying, complete (verified and recorded), purged (and gone from the hot store), failed, restoring, restored.
create table archive_manifest (
    id varchar(26) primary key,
    status varchar(16) not null,
    trigger varchar(16) not null,
    domain varchar(32) not null default 'record_version',
    target varchar(16) not null,
    location text,
    cutoff timestamptz,
    sources text[] not null default '{}',
    from_seq bigint,
    to_seq bigint,
    from_time timestamptz,
    to_time timestamptz,
    size_bytes bigint,
    item_count bigint,
    checksum varchar(64),
    files jsonb not null default '[]',
    purged_from_hot boolean not null default false,
    error text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    completed_at timestamptz,
    purged_at timestamptz,
    restored_at timestamptz
);
create index archive_manifest_created_idx on archive_manifest (created_at desc, id desc);
create index archive_manifest_status_idx on archive_manifest (status, created_at desc, id desc);

-- The restore area: rows restoreArchive loaded back from a segment, with record_version's columns plus archive_id.
-- They are not put back into record_version, whose TRACK partitions for those seqs are dropped and whose retention
-- would delete them again within the hour. Restoring a segment again replaces its rows. Not mapped by Hibernate.
create table record_version_restored (
    archive_id varchar(26) not null references archive_manifest (id) on delete cascade,
    seq bigint not null,
    ingested_at timestamptz not null,
    observed_at timestamptz not null,
    received_at timestamptz not null,
    event_time timestamptz,
    valid_until timestamptz,
    valid_until_explicit timestamptz,
    prev_seq bigint,
    batch_id bigint,
    version integer not null,
    history_tier smallint not null,
    persist_reason smallint not null,
    qa_flags smallint not null,
    tombstone boolean not null,
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    kind varchar(64) not null,
    geom geometry(Geometry, 4326),
    payload jsonb not null,
    quality jsonb,
    observer jsonb,
    valid_from timestamptz,
    primary key (archive_id, seq, history_tier)
);
create index record_version_restored_source_idx on record_version_restored (source_id, external_id, seq);
create index record_version_restored_geom_gist on record_version_restored using gist (geom);
