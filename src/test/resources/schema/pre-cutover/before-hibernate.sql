-- The pre-1.0 bootstrap SQL that ran BEFORE Hibernate's ddl-auto=update, preserved verbatim in meaning for the
-- test-only pre-cutover schema generator (FlywaySchemaParityIT, prompt 144). Sources, all deleted in 144:
-- PostgisExtensionInitializer, VectorExtensionInitializer, HistorySchemaInitializer (HistoryDdl),
-- ChronicleSchemaInitializer (ChronicleDdl) and ExportSchemaInitializer. Statements are separated by a line holding
-- only "--;" so the generator can split them without parsing SQL.
CREATE EXTENSION IF NOT EXISTS postgis
--;
CREATE EXTENSION IF NOT EXISTS vector
--;
CREATE SEQUENCE IF NOT EXISTS record_seq INCREMENT BY 1
--;
CREATE TABLE IF NOT EXISTS record_version (
    seq bigint NOT NULL,
    ingested_at timestamptz NOT NULL,
    observed_at timestamptz NOT NULL,
    received_at timestamptz NOT NULL,
    event_time timestamptz,
    valid_until timestamptz,
    valid_until_explicit timestamptz,
    prev_seq bigint,
    batch_id bigint,
    version integer NOT NULL,
    history_tier smallint NOT NULL,
    persist_reason smallint NOT NULL,
    qa_flags smallint NOT NULL,
    tombstone boolean NOT NULL,
    source_id varchar(64) NOT NULL,
    external_id varchar(256) NOT NULL,
    kind varchar(64) NOT NULL,
    geom geometry(Geometry,4326),
    payload jsonb NOT NULL,
    quality jsonb,
    observer jsonb,
    valid_from timestamptz,
    PRIMARY KEY (seq, history_tier)
) PARTITION BY LIST (history_tier)
--;
CREATE TABLE IF NOT EXISTS record_version_std PARTITION OF record_version FOR VALUES IN (0) PARTITION BY RANGE (seq)
--;
CREATE TABLE IF NOT EXISTS record_version_trk PARTITION OF record_version FOR VALUES IN (1) PARTITION BY RANGE (seq)
--;
CREATE INDEX IF NOT EXISTS record_version_source_external_seq_idx ON record_version (source_id, external_id, seq)
--;
CREATE INDEX IF NOT EXISTS record_version_source_observed_idx ON record_version (source_id, observed_at)
--;
CREATE INDEX IF NOT EXISTS record_version_geom_gist ON record_version USING GIST (geom)
--;
CREATE TABLE IF NOT EXISTS chronicle_version (
    chronicle_id varchar(26) NOT NULL,
    layer_id varchar(26) NOT NULL,
    seq bigint NOT NULL,
    ingested_at timestamptz NOT NULL,
    observed_at timestamptz NOT NULL,
    received_at timestamptz NOT NULL,
    event_time timestamptz,
    valid_until timestamptz,
    valid_until_explicit timestamptz,
    prev_seq bigint,
    batch_id bigint,
    version integer NOT NULL,
    history_tier smallint NOT NULL,
    persist_reason smallint NOT NULL,
    qa_flags smallint NOT NULL,
    tombstone boolean NOT NULL,
    source_id varchar(64) NOT NULL,
    external_id varchar(256) NOT NULL,
    kind varchar(64) NOT NULL,
    geom geometry(Geometry,4326),
    payload jsonb NOT NULL,
    quality jsonb,
    observer jsonb,
    valid_from timestamptz,
    backfilled boolean NOT NULL DEFAULT false,
    bucket_start timestamptz,
    captured_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (chronicle_id, layer_id, seq)
) PARTITION BY LIST (chronicle_id)
--;
CREATE INDEX IF NOT EXISTS chronicle_version_seq_idx ON chronicle_version (seq)
--;
CREATE INDEX IF NOT EXISTS chronicle_version_layer_observed_idx ON chronicle_version (layer_id, observed_at)
--;
CREATE INDEX IF NOT EXISTS chronicle_version_geom_gist ON chronicle_version USING GIST (geom)
--;
CREATE INDEX IF NOT EXISTS chronicle_version_source_external_seq_idx ON chronicle_version (source_id, external_id, seq)
--;
CREATE TABLE IF NOT EXISTS record_version_held (
    hold_id varchar(26) NOT NULL,
    seq bigint NOT NULL,
    ingested_at timestamptz NOT NULL,
    observed_at timestamptz NOT NULL,
    received_at timestamptz NOT NULL,
    event_time timestamptz,
    valid_until timestamptz,
    valid_until_explicit timestamptz,
    prev_seq bigint,
    batch_id bigint,
    version integer NOT NULL,
    history_tier smallint NOT NULL,
    persist_reason smallint NOT NULL,
    qa_flags smallint NOT NULL,
    tombstone boolean NOT NULL,
    source_id varchar(64) NOT NULL,
    external_id varchar(256) NOT NULL,
    kind varchar(64) NOT NULL,
    geom geometry(Geometry,4326),
    payload jsonb NOT NULL,
    quality jsonb,
    observer jsonb,
    valid_from timestamptz,
    PRIMARY KEY (hold_id, seq)
)
--;
CREATE INDEX IF NOT EXISTS record_version_held_source_idx ON record_version_held (source_id, external_id, seq)
--;
CREATE INDEX IF NOT EXISTS record_version_held_geom_gist ON record_version_held USING GIST (geom)
--;
