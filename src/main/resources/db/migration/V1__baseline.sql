-- =====================================================================================================================
-- Foresignal 1.0.0 — Flyway baseline (V1). Architecture §25.
--
-- The complete schema as of prompt 144: every extension, the partitioned history tables, every entity table and every
-- index the pre-1.0 bootstrap code created. Applied once to an empty database. Never edit this file after it has been
-- applied anywhere: later changes are new migrations, V{n}__{description}.sql, additive by default, one per change.
--
-- How it was made: the generation procedure (scripts/db/dump-schema.sh, a pg_dump --schema-only of a pre-cutover
-- ddl-auto=update database) could not be run when this file was written (owner directive of 2026-10-17: nothing is
-- executed until code-complete). It was therefore hand-derived from the 238 JPA entities (Hibernate 7.4.5 type and
-- naming rules, read from the Hibernate and Spring Boot jars) and from the bootstrap SQL it replaces
-- (HistorySchemaInitializer, ChronicleSchemaInitializer, ExportSchemaInitializer, SpatialIndexInitializer,
-- InfraSchemaInitializer, EmbeddingSchemaManager's base statements). FlywaySchemaParityIT compares it with the
-- pre-cutover generator; the owner verifies it with scripts/db/dump-schema.sh before release.
--
-- Deliberate differences from Hibernate's own DDL (allowlisted in FlywaySchemaParityIT):
--   * no CHECK constraints for enum columns: the application enforces enum values (this replaces the old
--     DeliveryStatusConstraint, which dropped one of them at every start);
--   * constraint and index names are descriptive, not Hibernate's hashed UK/FK names;
--   * columns are listed in declaration order (Hibernate 7 orders them by size; order has no effect on behavior).
--
-- What is NOT here, by design (dynamic DDL, Architecture §25):
--   * record_version and chronicle_version leaves: the PartitionManager and ChronicleStorage create them at runtime;
--   * the embedding vector slots (embedding_a/b, model_a/b, HNSW indexes): the reindex creates them at the configured
--     dimension;
--   * the reference loader's ref_stage_* working tables.
--
-- Extensions: on an external PostgreSQL where the application role is not a superuser, a superuser must run
--   CREATE EXTENSION IF NOT EXISTS postgis; CREATE EXTENSION IF NOT EXISTS vector;
-- in the foresignal database before the first start. The supported image (docker/db, foresignal-db:17-3.5-pgvector)
-- needs nothing.
-- =====================================================================================================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS vector;

-- =====================================================================================================================
-- History (Architecture §8, §9): the global seq allocator and the tiered, partitioned record_version.
-- =====================================================================================================================

CREATE SEQUENCE record_seq INCREMENT BY 1;

-- Partitioned by tier, then by seq range. The primary key includes the partition key; there is deliberately no unique
-- constraint, no ingested_at index, no content hash and never a DEFAULT partition. No leaves: the PartitionManager
-- attaches them after this migration (HistoryPartitionBootstrap) and keeps them ahead of record_seq.
CREATE TABLE record_version (
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
) PARTITION BY LIST (history_tier);

CREATE TABLE record_version_std PARTITION OF record_version FOR VALUES IN (0) PARTITION BY RANGE (seq);
CREATE TABLE record_version_trk PARTITION OF record_version FOR VALUES IN (1) PARTITION BY RANGE (seq);

CREATE INDEX record_version_source_external_seq_idx ON record_version (source_id, external_id, seq);
CREATE INDEX record_version_source_observed_idx ON record_version (source_id, observed_at);
CREATE INDEX record_version_geom_gist ON record_version USING GIST (geom);

-- Legal holds (prompt 111): the plain archive-then-delete copy of held versions. Not mapped by Hibernate.
CREATE TABLE record_version_held (
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
);

CREATE INDEX record_version_held_source_idx ON record_version_held (source_id, external_id, seq);
CREATE INDEX record_version_held_geom_gist ON record_version_held USING GIST (geom);

-- Chronicles (prompt 131): one LIST leaf per chronicle, created on activation and dropped on deletion, archive or the
-- chronicle's own retention (ChronicleStorage). Not mapped by Hibernate.
CREATE TABLE chronicle_version (
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
) PARTITION BY LIST (chronicle_id);

CREATE INDEX chronicle_version_seq_idx ON chronicle_version (seq);
CREATE INDEX chronicle_version_layer_observed_idx ON chronicle_version (layer_id, observed_at);
CREATE INDEX chronicle_version_geom_gist ON chronicle_version USING GIST (geom);
CREATE INDEX chronicle_version_source_external_seq_idx ON chronicle_version (source_id, external_id, seq);

-- Alert revisions (prompt 017): every native alert write takes rev from this sequence.
CREATE SEQUENCE alert_rev INCREMENT BY 1;

-- =====================================================================================================================
-- Entity tables, grouped by subsystem (package). Primary keys list their columns in Hibernate 7.4 constraint order
-- (ColumnOrderingStrategyStandard: ascending physical size, then name). A unique @Index is a UNIQUE constraint, as
-- Hibernate creates it.
-- =====================================================================================================================

-- ===== store =====

-- SystemSecretEntity
create table system_secret (
    key varchar(128) not null,
    value bytea not null,
    primary key (key)
);

-- SeqClockEntity
create table seq_clock (
    minute timestamp(6) with time zone not null,
    first_seq bigint not null,
    primary key (minute)
);

-- AppSettingEntity
create table app_setting (
    key varchar(128) not null,
    value jsonb not null,
    updated_at timestamp(6) with time zone not null,
    primary key (key)
);

-- SourceCredentialEntity
create table source_credential (
    source_id varchar(64) not null,
    ciphertext bytea not null,
    nonce bytea not null,
    updated_at timestamp(6) with time zone not null,
    primary key (source_id)
);

-- RetentionStateEntity (prompt 019) — @EmbeddedId RetentionStateId
create table retention_state (
    kind varchar(64) not null,
    source_id varchar(64) not null,
    floor_seq bigint not null,
    purge_seq bigint not null,
    purged_at timestamp(6) with time zone,
    last_run_at timestamp(6) with time zone,
    primary key (kind, source_id)
);

-- SourceHealthEntity (prompt 064)
create table source_health (
    source_id varchar(64) not null,
    last_attempt_at timestamp(6) with time zone,
    last_success_at timestamp(6) with time zone,
    last_error_at timestamp(6) with time zone,
    last_error text,
    last_http_status integer,
    consecutive_failures integer not null,
    last_records_seen integer,
    last_records_changed integer,
    recent_record_counts jsonb not null,
    backoff_until timestamp(6) with time zone,
    backoff_halted boolean,
    last_coverage jsonb,
    recent_coverage jsonb,
    last_data_at timestamp(6) with time zone,
    adapter_state jsonb not null,
    primary key (source_id)
);

-- SourceUsageEntity — @EmbeddedId SourceUsageId
create table source_usage (
    minute timestamp(6) with time zone not null,
    source_id varchar(64) not null,
    requests bigint not null,
    bytes bigint not null,
    errors bigint not null,
    refused bigint not null,
    records_seen bigint not null,
    versions bigint not null,
    live_updates bigint not null,
    touches bigint not null,
    late bigint not null,
    skipped bigint not null,
    rejected bigint not null,
    version_bytes bigint not null,
    clamped bigint default 0 not null,
    trigger_edges bigint default 0 not null,
    primary key (minute, source_id)
);

-- SourceSettingsEntity (prompts 027, 044, 069, 109)
create table source_settings (
    source_id varchar(64) not null,
    enabled boolean not null,
    interval_seconds integer not null,
    params jsonb not null,
    budget_per_minute integer not null,
    budget_per_day integer not null,
    retention varchar(255),
    ingest_policy jsonb,
    embed boolean,
    downsample_interval interval second(6),
    egress varchar(16),
    reliability varchar(1),
    updated_at timestamp(6) with time zone not null,
    lock_version bigint not null,
    primary key (source_id)
);

-- RecordHeadEntity (prompt 008) — @EmbeddedId RecordHeadId
create table record_head (
    external_id varchar(256) not null,
    source_id varchar(64) not null,
    kind varchar(64) not null,
    seq bigint not null,
    version integer not null,
    event_time timestamp(6) with time zone,
    valid_until_explicit timestamp(6) with time zone,
    valid_from timestamp(6) with time zone,
    tombstone boolean not null,
    static_hash bigint not null,
    hash_recipe integer not null,
    persisted_observed_at timestamp(6) with time zone not null,
    persisted_received_at timestamp(6) with time zone not null,
    persisted_geom geometry(Geometry,4326),
    payload jsonb not null,
    quality jsonb,
    search_text text,
    title varchar(256),
    primary key (source_id, external_id)
);
create index record_head_kind_idx on record_head (kind);

-- RecordLiveEntity — @EmbeddedId RecordHeadId
create table record_live (
    external_id varchar(256) not null,
    source_id varchar(64) not null,
    observed_at timestamp(6) with time zone not null,
    last_received_at timestamp(6) with time zone not null,
    valid_until timestamp(6) with time zone,
    geom geometry(Geometry,4326),
    full_hash bigint not null,
    dirty boolean not null,
    kinematic jsonb default '{}'::jsonb not null,
    quality jsonb,
    qa_flags smallint default 0 not null,
    primary key (source_id, external_id)
);

-- FetchLogEntity (prompt 094)
create table fetch_log (
    id bigint generated by default as identity,
    source_id varchar(64) not null,
    trigger varchar(16) not null,
    status varchar(16) not null,
    started_at timestamp(6) with time zone not null,
    received_at timestamp(6) with time zone,
    ingested_at timestamp(6) with time zone,
    http_status integer,
    request_count integer not null,
    url_redacted text,
    etag varchar(512),
    bytes bigint,
    body_sha256 varchar(64),
    adapter_version varchar(64),
    licence varchar(128),
    principal varchar(128),
    records_seen integer not null,
    rejected integer not null,
    late integer not null,
    skipped integer not null,
    versions_written integer not null,
    live_updates integer not null,
    touches integer not null,
    from_seq bigint,
    to_seq bigint,
    coverage jsonb,
    error text,
    run_id varchar(64),
    primary key (id)
);
create index fetch_log_source_started_idx on fetch_log (source_id, started_at desc);

-- EscalationPolicyEntity (prompt 033)
create table escalation_policy (
    id uuid not null,
    name varchar(200) not null,
    steps jsonb not null,
    repeat jsonb,
    stop_on jsonb not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- TriggerRuleEntity (prompts 017, 030, 033, 036)
create table trigger_rule (
    id uuid not null,
    name varchar(255) not null,
    enabled boolean not null,
    sources jsonb default '[]'::jsonb not null,
    kinds jsonb default '[]'::jsonb not null,
    tags jsonb default '[]'::jsonb not null,
    conditions jsonb not null,
    geofence geometry(MultiPolygon,4326),
    geofence_route jsonb,
    buffer_meters double precision,
    hysteresis_meters double precision default 0 not null,
    geofence_inner geometry(MultiPolygon,4326),
    geofence_outer geometry(MultiPolygon,4326),
    near jsonb,
    geofence_origin varchar(8),
    geofence_ref jsonb,
    mode varchar(8) default 'match' not null,
    type varchar(16) default 'record' not null,
    window_spec jsonb,
    only_new boolean not null,
    cooldown_seconds integer default 0 not null,
    cooldown_scope varchar(8) default 'record' not null,
    severity varchar(16) default 'warning' not null,
    enrich boolean default false not null,
    enrich_radius_km double precision default 100 not null,
    enrich_window_seconds bigint default 86400 not null,
    weather_context varchar(8) default 'auto',
    auto_resolve jsonb,
    max_alerts_per_hour integer,
    auto_disable_on_storm boolean default false not null,
    active_from timestamp(6) with time zone,
    active_until timestamp(6) with time zone,
    active_window jsonb,
    forecast_horizon_seconds bigint,
    message_template text,
    reappear_as_enter boolean default false not null,
    snoozed_until timestamp(6) with time zone,
    muted_until timestamp(6) with time zone,
    escalation_policy_id uuid,
    suppressed_count bigint default 0 not null,
    last_storm_at timestamp(6) with time zone,
    last_error text,
    last_error_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- AlertEntity (prompts 017, 030, 036, 040)
create table alert (
    id bigint generated by default as identity,
    rule_id uuid not null,
    seq bigint not null,
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    kind varchar(64),
    fired_at timestamp(6) with time zone not null,
    message text,
    severity varchar(16) default 'warning' not null,
    state varchar(16) default 'firing' not null,
    acknowledged_at timestamp(6) with time zone,
    acknowledged_by varchar(200),
    resolved_at timestamp(6) with time zone,
    resolved_by varchar(200),
    resolved_seq bigint,
    feedback varchar(16),
    feedback_at timestamp(6) with time zone,
    note text,
    snoozed_until timestamp(6) with time zone,
    suppressed boolean default false not null,
    suppressed_reason varchar(32),
    tags jsonb default '[]'::jsonb not null,
    rev bigint default 0 not null,
    observed_at timestamp(6) with time zone,
    record_snapshot jsonb,
    summary text,
    enriched_at timestamp(6) with time zone,
    summary_citations jsonb,
    sensitive boolean default false not null,
    context jsonb,
    region jsonb,
    exercise boolean default false not null,
    exercise_run_id varchar(26),
    primary key (id)
);
create index alert_rule_entity_observed_idx on alert (rule_id, source_id, external_id, observed_at);
create index alert_rule_observed_idx on alert (rule_id, observed_at);
create index alert_rev_idx on alert (rev);
create index alert_fired_at_idx on alert (fired_at);
create index alert_exercise_run_idx on alert (exercise_run_id);

-- AlertEscalationEntity (prompt 033)
create table alert_escalation (
    alert_id bigint not null,
    policy_id uuid not null,
    next_step integer not null,
    next_at timestamp(6) with time zone,
    repeats_done integer not null,
    state varchar(16) not null,
    stopped_reason varchar(64),
    updated_at timestamp(6) with time zone not null,
    primary key (alert_id)
);
create index alert_escalation_due_idx on alert_escalation (state, next_at);

-- NotificationEntity (prompts 018, 033, 115)
create table notification (
    id bigint generated by default as identity,
    event varchar(32) not null,
    subtype varchar(64) not null,
    severity varchar(16) not null,
    title text not null,
    short_title varchar(40),
    body_markdown text,
    url text,
    links jsonb,
    data jsonb,
    alert_id bigint,
    briefing_run_id uuid,
    rule_id uuid,
    tags text[],
    update_of bigint,
    origin varchar(64),
    exercise boolean default false not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index notification_created_idx on notification (created_at);
create index notification_alert_idx on notification (alert_id);

-- NotificationChannelEntity (prompts 018, 033, 115)
create table notification_channel (
    id uuid not null,
    name varchar(200) not null,
    type varchar(32) not null,
    config jsonb default '{}'::jsonb not null,
    secret_ciphertext bytea,
    secret_nonce bytea,
    events jsonb default '[]'::jsonb not null,
    rule_ids jsonb,
    min_severity varchar(16) not null,
    title_template text,
    body_template text,
    wait_for_enrichment_seconds integer default 0 not null,
    enabled boolean not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    timezone varchar(64),
    quiet_hours jsonb,
    group_window_seconds integer default 0 not null,
    backlog_cap integer default 20 not null,
    edit_in_place boolean default true not null,
    defer_while_active_seconds integer default 0 not null,
    accept_exercise boolean default false not null,
    language varchar(35),
    owner_principal_id varchar(64),
    primary key (id)
);

-- NotificationDeliveryEntity (prompts 018, 033, 055, 084)
create table notification_delivery (
    id bigint generated by default as identity,
    channel_id uuid not null,
    notification_id bigint not null,
    event varchar(32) not null,
    alert_id bigint,
    briefing_run_id uuid,
    status varchar(16) not null,
    kind varchar(16) not null,
    attempts integer not null,
    last_error text,
    next_attempt_at timestamp(6) with time zone,
    delivered_at timestamp(6) with time zone,
    provider_message_ref varchar(512),
    deferred_reason varchar(32),
    note varchar(200),
    edited boolean default false not null,
    folded_into bigint,
    leased_until timestamp(6) with time zone,
    retry_after_counted_at timestamp(6) with time zone,
    escalation_step integer,
    created_at timestamp(6) with time zone not null,
    attachment_level varchar(16),
    attachments jsonb,
    short_code varchar(8),
    primary key (id)
);
create index notification_delivery_due_idx on notification_delivery (status, next_attempt_at);
create index notification_delivery_channel_idx on notification_delivery (channel_id, id desc);
create index notification_delivery_notification_idx on notification_delivery (notification_id);

-- PushSubscriptionEntity (prompt 031)
create table push_subscription (
    id uuid not null,
    name varchar(200),
    endpoint text not null,
    p256dh varchar(100) not null,
    auth varchar(40) not null,
    filters jsonb default '{}'::jsonb not null,
    token_id uuid,
    created_at timestamp(6) with time zone not null,
    last_success_at timestamp(6) with time zone,
    status varchar(16) not null,
    verify_code_hash varchar(64),
    verify_expires_at timestamp(6) with time zone,
    verify_attempts integer default 0 not null,
    endpoint_host varchar(255) not null,
    transport_label varchar(16) not null,
    payload_format varchar(16) not null,
    lang varchar(35),
    failure_count integer default 0 not null,
    last_failure_at timestamp(6) with time zone,
    last_status integer,
    recheck_failures integer default 0 not null,
    primary key (id)
);
create index push_subscription_token_idx on push_subscription (token_id);
create index push_subscription_status_idx on push_subscription (status, created_at);

-- PushDispatchEntity (prompt 031)
create table push_dispatch (
    id bigint generated by default as identity,
    delivery_id bigint not null,
    target_type varchar(32) not null,
    target_id varchar(64) not null,
    status varchar(16) not null,
    attempts integer default 0 not null,
    next_attempt_at timestamp(6) with time zone,
    last_status integer,
    last_error varchar(500),
    downgraded boolean default false not null,
    content jsonb not null,
    leased_until timestamp(6) with time zone,
    sent_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index push_dispatch_due_idx on push_dispatch (status, next_attempt_at);
create index push_dispatch_delivery_idx on push_dispatch (delivery_id);
create index push_dispatch_target_idx on push_dispatch (target_type, target_id);

-- ===== source =====

-- DcpLocationEntity (prompt 107)
create table dcp_location (
    address varchar(32) not null,
    lat double precision,
    lon double precision,
    name varchar(128),
    agency varchar(64),
    source varchar(64),
    updated_at timestamp(6) with time zone,
    primary key (address)
);

-- WebSubSubscriptionEntity (prompt 073)
create table websub_subscription (
    source_id varchar(64) not null,
    hub varchar(2048) not null,
    topic varchar(2048) not null,
    token varchar(128) not null,
    secret_enc bytea not null,
    state varchar(16) not null,
    lease_expires_at timestamp(6) with time zone,
    last_verified_at timestamp(6) with time zone,
    last_push_at timestamp(6) with time zone,
    requested_at timestamp(6) with time zone,
    primary key (source_id)
);

-- ===== retention (prompt 044) =====

-- HistoryDownsampleEntity (prompt 044)
create table history_downsample (
    leaf_name varchar(63) not null,
    history_tier smallint not null,
    from_seq bigint not null,
    to_seq bigint not null,
    default_interval_s integer not null,
    rows_before bigint,
    rows_after bigint,
    started_at timestamp(6) with time zone not null,
    completed_at timestamp(6) with time zone,
    status varchar(16) not null,
    error text,
    primary key (leaf_name)
);

-- ===== provenance (prompt 031) =====

-- SigningKeyEntity (prompts 031, 109)
create table signing_key (
    kid varchar(64) not null,
    private_key bytea not null,
    public_key bytea not null,
    status varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    retired_at timestamp(6) with time zone,
    primary key (kid)
);

-- ===== observability (prompt 116) =====

-- ObservabilityEstimateEntity (prompt 116)
create table observability_estimate (
    id varchar(26) not null,
    source_id varchar(64) not null,
    family varchar(64) not null,
    region_code varchar(16),
    cell varchar(16),
    band varchar(32),
    period_start timestamp(6) with time zone not null,
    period_end timestamp(6) with time zone not null,
    p_detect double precision not null,
    lo double precision not null,
    hi double precision not null,
    n bigint not null,
    method varchar(32) not null,
    confidence varchar(16) not null,
    computed_at timestamp(6) with time zone not null,
    manifest_id varchar(26),
    primary key (id)
);
create index observability_estimate_lookup_idx on observability_estimate (family, source_id, period_end);
create index observability_estimate_cell_idx on observability_estimate (family, cell);

-- ===== manifest (prompt 116) =====

-- AnalysisManifestEntity (prompt 116)
create table analysis_manifest (
    id varchar(26) not null,
    component varchar(128) not null,
    component_version varchar(64) not null,
    build jsonb not null,
    config jsonb not null,
    config_hash varchar(64) not null,
    registries jsonb not null,
    asof_seq bigint,
    inputs jsonb not null,
    model jsonb,
    content_hash varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    primary key (id),
    constraint analysis_manifest_content_hash_key unique (content_hash)
);
create index analysis_manifest_component_idx on analysis_manifest (component, created_at);

-- ===== search (prompt 027) =====

-- EmbeddingDocumentEntity — @IdClass EmbeddingDocumentId
-- The tsv generated column, the vector slot columns and the GIN/GiST/source_time/HNSW indexes are NOT
-- entity-mapped; EmbeddingSchemaManager adds them at runtime (src/main/java/ai/foresignal/search/EmbeddingSchemaManager.java:49-55, 100-110).
create table embedding_document (
    doc_id varchar(400) not null,
    doc_type varchar(16) not null,
    source_id varchar(64),
    kind varchar(64),
    seq bigint,
    geom geometry(Geometry,4326),
    effective_time timestamp(6) with time zone,
    observed_at timestamp(6) with time zone,
    text text not null,
    text_hash bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (doc_type, doc_id)
);

-- ===== security (prompt 023) =====

-- PrincipalEntity (prompts 023, 130)
create table principal (
    id varchar(26) not null,
    handle varchar(32) not null,
    display_name varchar(200) not null,
    kind varchar(16) not null,
    disabled boolean not null,
    email varchar(320),
    avatar_media_id varchar(80),
    timezone varchar(64),
    organization varchar(200),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint uk_principal_handle unique (handle)
);

-- ApiTokenEntity (prompts 023, 035)
create table api_token (
    id uuid not null,
    name varchar(200) not null,
    scope varchar(16) not null,
    token_hash varchar(64) not null,
    token_prefix varchar(8) not null,
    created_at timestamp(6) with time zone not null,
    last_used_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    revoked_at timestamp(6) with time zone,
    allowed_sources text[],
    allowed_kinds text[],
    allowed_bbox geometry(Polygon,4326),
    allowed_endpoints text[],
    allowed_area geometry(Geometry,4326),
    exclude_private boolean,
    max_requests_per_minute integer,
    allowed_place_ids text[],
    federation boolean,
    include_exercise boolean,
    private_access boolean,
    licence_policy text[],
    type varchar(16),
    prefixes text[],
    device_name varchar(200),
    device_platform varchar(16),
    app_version varchar(64),
    device_reported_at timestamp(6) with time zone,
    created_via varchar(16) not null,
    principal_id varchar(26) not null,
    primary key (id)
);
alter table api_token add constraint api_token_hash_idx unique (token_hash);

-- PairingCodeEntity (prompt 023)
create table pairing_code (
    id uuid not null,
    code_hash varchar(64) not null,
    name varchar(200) not null,
    scope varchar(16) not null,
    restrictions jsonb,
    principal_id varchar(26),
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    claimed_at timestamp(6) with time zone,
    claimed_token_id uuid,
    private_access boolean,
    created_by_token_id uuid,
    primary key (id)
);
alter table pairing_code add constraint pairing_code_hash_idx unique (code_hash);

-- AuditEventEntity (prompt 023)
create table audit_event (
    id bigint generated by default as identity,
    at timestamp(6) with time zone not null,
    actor_kind varchar(16) not null,
    token_id uuid,
    token_name varchar(200),
    principal_id varchar(26),
    scope varchar(16),
    method varchar(16),
    path varchar(2048),
    operation_id varchar(128),
    status integer,
    target_type varchar(64),
    target_id varchar(256),
    remote_ip varchar(64),
    detail jsonb default '{}'::jsonb not null,
    primary key (id)
);
create index audit_event_at_idx on audit_event (at desc);
create index audit_event_token_at_idx on audit_event (token_id, at desc);

-- ===== social (prompt 135) =====

-- SocialCoverageEntity
create table social_coverage (
    id varchar(120) not null,
    platform varchar(32) not null,
    region_code varchar(16) not null,
    hour timestamp(6) with time zone not null,
    posts_seen bigint not null,
    denominator bigint,
    complete boolean,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_coverage_region_idx on social_coverage (region_code, platform, hour);

-- SocialDenominatorHourlyEntity
create table social_denominator_hourly (
    id varchar(120) not null,
    platform varchar(32) not null,
    language varchar(16) not null,
    hour timestamp(6) with time zone not null,
    posts bigint not null,
    complete boolean,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_denominator_hourly_idx on social_denominator_hourly (platform, hour);

-- SocialEventLogEntity
create table social_event_log (
    id bigint generated by default as identity,
    event varchar(32) not null,
    payload jsonb not null,
    private boolean not null,
    at timestamp(6) with time zone not null,
    primary key (id)
);

-- SocialPlatformEntity
create table social_platform (
    id varchar(64) not null,
    enabled boolean not null,
    mode varchar(32),
    channels jsonb,
    deletion_sync boolean not null,
    post_retention varchar(32),
    monthly_cap_usd numeric(12,4),
    prices jsonb,
    prices_as_of varchar(10),
    options jsonb,
    state varchar(32),
    state_reason varchar(500),
    deletion_last_run_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- SocialQuotaUseEntity
create table social_quota_use (
    id varchar(26) not null,
    source_id varchar(64) not null,
    unit varchar(16) not null,
    use_key varchar(200),
    amount bigint not null,
    at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_quota_use_idx on social_quota_use (source_id, unit, at);

-- SocialSaltEntity
create table social_salt (
    id varchar(32) not null,
    ciphertext bytea not null,
    nonce bytea not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);

-- SocialSpendEntity
create table social_spend (
    id varchar(80) not null,
    source_id varchar(64) not null,
    month varchar(7) not null,
    spent_usd numeric(14,6) not null,
    calls bigint not null,
    refused bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- SocialStreamGapEntity
create table social_stream_gap (
    id varchar(26) not null,
    source_id varchar(64) not null,
    gap_from timestamp(6) with time zone not null,
    gap_to timestamp(6) with time zone,
    replayed boolean not null,
    reason varchar(200),
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_stream_gap_source_idx on social_stream_gap (source_id, gap_from);

-- SocialVolumeHourlyEntity
create table social_volume_hourly (
    id varchar(300) not null,
    scope_type varchar(16) not null,
    scope_id varchar(64) not null,
    platform varchar(32) not null,
    language varchar(16) not null,
    hour timestamp(6) with time zone not null,
    collection_type varchar(16) not null,
    posts bigint not null,
    unique_authors bigint not null,
    reposts bigint not null,
    engagement bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_volume_hourly_scope_idx on social_volume_hourly (scope_type, scope_id, platform, hour);

-- ===== social.narrative (prompt 136) =====

-- MediaFingerprintEntity
create table media_fingerprint (
    id varchar(26) not null,
    pdq varchar(64) not null,
    quality integer not null,
    first_seen_at timestamp(6) with time zone not null,
    first_post_ref varchar(500) not null,
    first_post_key varchar(256) not null,
    use_count integer not null,
    platforms text[],
    narrative_ids text[],
    media_id varchar(26),
    reuse_finding_ids text[],
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index media_fingerprint_first_idx on media_fingerprint (first_seen_at);
alter table media_fingerprint add constraint media_fingerprint_pdq_uq unique (pdq);

-- MediaQueueEntity
create table social_media_queue (
    id varchar(64) not null,
    url varchar(2000) not null,
    source_id varchar(128) not null,
    external_id varchar(256) not null,
    post_key varchar(256) not null,
    platform varchar(32),
    narrative_id varchar(26),
    seen_at timestamp(6) with time zone not null,
    attempts integer not null,
    status varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_media_queue_status_idx on social_media_queue (status, created_at);

-- MediaUseEntity
create table media_use (
    id varchar(300) not null,
    fingerprint_id varchar(26) not null,
    post_ref varchar(500) not null,
    post_key varchar(256) not null,
    url varchar(2000),
    platform varchar(32),
    seen_at timestamp(6) with time zone not null,
    primary key (id)
);
create index media_use_fingerprint_idx on media_use (fingerprint_id, seen_at);
create index media_use_post_idx on media_use (post_key);

-- PropagationEdgeEntity
create table social_propagation_edge (
    id varchar(26) not null,
    narrative_id varchar(26) not null,
    from_node varchar(500) not null,
    to_node varchar(500) not null,
    type varchar(16) not null,
    delta_seconds double precision not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_propagation_edge_narrative_idx on social_propagation_edge (narrative_id);

-- SocialClaimEntity
create table social_claim (
    id varchar(26) not null,
    narrative_id varchar(26) not null,
    claim_key varchar(600) not null,
    type varchar(64) not null,
    signal_family varchar(64),
    polarity varchar(16) not null,
    hedged boolean not null,
    place jsonb,
    place_lon double precision,
    place_lat double precision,
    region_code varchar(16),
    entity_refs text[],
    event_time jsonb,
    actor varchar(200),
    support_count integer not null,
    evidence_group_count integer not null,
    first_observed_at timestamp(6) with time zone not null,
    extraction varchar(16) not null,
    manifest_id varchar(26),
    confidence double precision not null,
    status varchar(32) not null,
    comparison_id varchar(26),
    source_key varchar(300),
    source_reliability varchar(1) not null,
    information_credibility integer not null,
    tlp varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_claim_narrative_idx on social_claim (narrative_id);
create index social_claim_first_idx on social_claim (first_observed_at);
create index social_claim_source_idx on social_claim (source_key);
alter table social_claim add constraint social_claim_key_uq unique (narrative_id, claim_key, extraction);

-- SocialNarrativeEntity
create table social_narrative (
    id varchar(26) not null,
    title varchar(300) not null,
    title_source varchar(16),
    central_post varchar(400),
    central_signature bytea,
    centroid text,
    centroid_model varchar(200),
    centroid_n integer not null,
    topic_ids text[],
    place_refs text[],
    entity_refs text[],
    region_codes text[],
    languages text[],
    platforms jsonb,
    first_observed_at timestamp(6) with time zone not null,
    last_activity_at timestamp(6) with time zone not null,
    post_count integer not null,
    unique_authors integer not null,
    evidence_group_count integer not null,
    origin_concentration double precision not null,
    velocity double precision,
    media_fingerprints text[],
    news_story_ids text[],
    situation_ids text[],
    status varchar(32) not null,
    state varchar(16) not null,
    coordination jsonb,
    coordination_finding_id varchar(26),
    merged_into varchar(26),
    tlp varchar(16) not null,
    manifest_id varchar(26),
    dirty boolean not null,
    llm_extracted_at timestamp(6) with time zone,
    llm_post_count integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_narrative_activity_idx on social_narrative (last_activity_at);
create index social_narrative_updated_idx on social_narrative (updated_at);
create index social_narrative_dirty_idx on social_narrative (dirty);

-- SocialNarrativeLogEntity
create table social_narrative_log (
    id varchar(26) not null,
    narrative_id varchar(26) not null,
    at timestamp(6) with time zone not null,
    action varchar(16) not null,
    actor varchar(128) not null,
    related_ids text[],
    from_value varchar(300),
    to_value varchar(300),
    reason varchar(2000),
    primary key (id)
);
create index social_narrative_log_narrative_idx on social_narrative_log (narrative_id, at);

-- SocialNarrativeMemberEntity
create table social_narrative_member (
    id varchar(400) not null,
    narrative_id varchar(26) not null,
    source_id varchar(128) not null,
    external_id varchar(256) not null,
    seq bigint not null,
    link_id varchar(26),
    platform varchar(32) not null,
    post_key varchar(256) not null,
    url varchar(2000),
    author_key varchar(64),
    originator_id varchar(200),
    source_key varchar(300),
    account_created_at timestamp(6) with time zone,
    signature bytea,
    language varchar(16),
    created_at timestamp(6) with time zone not null,
    link_reason varchar(16) not null,
    matched_key varchar(400),
    similarity double precision,
    repost_of varchar(256),
    quote_of varchar(256),
    reply_to varchar(256),
    links text[],
    media_urls text[],
    fingerprints text[],
    topic_ids text[],
    place_refs text[],
    entity_refs text[],
    region_codes text[],
    claims jsonb,
    evidence_group_key varchar(400),
    evidence_group_id varchar(26),
    centrality double precision,
    joined_at timestamp(6) with time zone not null,
    primary key (id)
);
create index social_narrative_member_narrative_idx on social_narrative_member (narrative_id, created_at);
create index social_narrative_member_post_idx on social_narrative_member (post_key);

-- ===== social.correlation (prompt 137) =====

-- SocialComparisonEntity
create table social_comparison (
    id varchar(26) not null,
    claim_id varchar(26) not null,
    narrative_id varchar(26) not null,
    claim_type varchar(64) not null,
    signal_family varchar(64) not null,
    place jsonb,
    place_lon double precision,
    place_lat double precision,
    region_code varchar(16),
    window_start timestamp(6) with time zone not null,
    window_end timestamp(6) with time zone not null,
    status varchar(32) not null,
    physical_evidence jsonb,
    detectability jsonb,
    lead_times jsonb,
    contradiction jsonb,
    caveats jsonb,
    found_by varchar(16) not null,
    cue_id varchar(26),
    trigger_ref varchar(600),
    platforms text[],
    evidence_group_count integer not null,
    question_id varchar(26),
    revision integer not null,
    final_at timestamp(6) with time zone not null,
    manifest_id varchar(26),
    evaluated_at timestamp(6) with time zone not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
alter table social_comparison add constraint social_comparison_claim_uq unique (claim_id);
create index social_comparison_narrative_idx on social_comparison (narrative_id);
create index social_comparison_status_idx on social_comparison (status, final_at);
create index social_comparison_evaluated_idx on social_comparison (evaluated_at);

-- SocialComparisonRevisionEntity
create table social_comparison_revision (
    id varchar(26) not null,
    comparison_id varchar(26) not null,
    revision integer not null,
    at timestamp(6) with time zone not null,
    status varchar(32) not null,
    previous_status varchar(32),
    reason varchar(1000) not null,
    physical_evidence_refs text[],
    manifest_id varchar(26),
    primary key (id)
);
alter table social_comparison_revision add constraint social_comparison_revision_uq unique (comparison_id, revision);

-- SocialCueEntity
create table social_cue (
    id varchar(26) not null,
    trigger_type varchar(32) not null,
    trigger_ref varchar(600),
    backend varchar(32) not null,
    queries jsonb,
    platforms text[],
    window_start timestamp(6) with time zone not null,
    window_end timestamp(6) with time zone,
    status varchar(16) not null,
    status_reason varchar(500),
    results jsonb,
    narrative_ids text[],
    cost_usd numeric(12,4) not null,
    max_cost_usd numeric(12,4) not null,
    input jsonb,
    created_by varchar(200) not null,
    created_at timestamp(6) with time zone not null,
    started_at timestamp(6) with time zone,
    completed_at timestamp(6) with time zone,
    manifest_id varchar(26),
    primary key (id)
);
create index social_cue_created_idx on social_cue (created_at);
create index social_cue_status_idx on social_cue (status);
create index social_cue_trigger_idx on social_cue (trigger_ref);

-- ===== news (prompt 133) =====

-- NewsArticleEntity
create table news_article (
    id varchar(400) not null,
    source_id varchar(128) not null,
    external_id varchar(256) not null,
    seq bigint not null,
    story_id varchar(26),
    link_id varchar(26),
    evidence_group_key varchar(400),
    evidence_group_id varchar(26),
    outlet_id varchar(26),
    domain varchar(253) not null,
    title varchar(500) not null,
    title_tokens text[],
    url varchar(4000) not null,
    language varchar(8),
    source_country varchar(2),
    seen_at timestamp(6) with time zone not null,
    published_at timestamp(6) with time zone,
    topic_ids text[],
    region_codes text[],
    summary varchar(500),
    image_url varchar(4000),
    syndicated boolean not null,
    wire_originator varchar(120),
    similarity double precision,
    match_method varchar(16),
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index news_article_story_idx on news_article (story_id);
create index news_article_outlet_idx on news_article (outlet_id);
create index news_article_seen_idx on news_article (seen_at);
create index news_article_record_idx on news_article (source_id, external_id);

-- NewsCoverageDailyEntity
create table news_coverage_daily (
    id varchar(200) not null,
    scope_type varchar(16) not null,
    scope_id varchar(64) not null,
    day timestamp(6) with time zone not null,
    bucket varchar(16) not null,
    evidence_groups integer not null,
    articles integer not null,
    computed_at timestamp(6) with time zone not null,
    primary key (id)
);
create index news_coverage_daily_scope_idx on news_coverage_daily (scope_id, bucket, day);

-- NewsStoryEntity
create table news_story (
    id varchar(26) not null,
    title varchar(500) not null,
    title_tokens text[],
    central_article varchar(400),
    centroid text,
    centroid_model varchar(200),
    centroid_n integer not null,
    first_seen_at timestamp(6) with time zone not null,
    last_seen_at timestamp(6) with time zone not null,
    article_count integer not null,
    outlet_count integer not null,
    evidence_group_count integer not null,
    topic_ids text[],
    region_codes text[],
    coverage jsonb,
    blindspot varchar(8),
    skewed boolean not null,
    n_rated integer not null,
    state_affiliated_share double precision not null,
    corroborated boolean not null,
    corroborated_by text[],
    situation_ids text[],
    blindspot_finding_id varchar(26),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index news_story_last_seen_idx on news_story (last_seen_at);
create index news_story_updated_idx on news_story (updated_at);

-- ===== markets (prompt 117) =====

-- BackfillJobEntity
create table history_backfill_job (
    id varchar(26) not null,
    source_id varchar(64) not null,
    status varchar(16) not null,
    from_date date not null,
    cursor_date date,
    pages_fetched integer not null,
    versions_written bigint not null,
    baseline_heads bigint not null,
    created_by varchar(200),
    created_at timestamp(6) with time zone not null,
    started_at timestamp(6) with time zone,
    finished_at timestamp(6) with time zone,
    error varchar(1000),
    primary key (id)
);

-- ===== cap (prompt 112) =====

-- CapMessageEntity
create table cap_message (
    identifier varchar(128) not null,
    alert_id bigint not null,
    n integer not null,
    msg_type varchar(16) not null,
    status varchar(16) not null,
    event varchar(32) not null,
    sent timestamp(6) with time zone not null,
    headline varchar(160),
    xml text not null,
    references_text text,
    primary key (identifier)
);
create index cap_message_alert_idx on cap_message (alert_id, n);
create index cap_message_sent_idx on cap_message (sent);

-- ===== attention (prompt 117; extended 118, 135) =====

-- AttentionTopicEntity
create table attention_topic (
    id varchar(48) not null,
    name varchar(200) not null,
    description varchar(2000),
    gdelt_query varchar(250),
    wiki_articles jsonb,
    keywords jsonb,
    region_codes jsonb,
    themes jsonb,
    enabled boolean not null,
    social jsonb,
    created_by varchar(200),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- ===== share (prompt 112) =====

-- ShareLinkEntity
create table share_link (
    id varchar(26) not null,
    key_hash varchar(64) not null,
    key_prefix varchar(16) not null,
    target_type varchar(16) not null,
    target_ref varchar(600) not null,
    field_mask jsonb,
    precision_m integer,
    masked boolean not null,
    expires_at timestamp(6) with time zone not null,
    max_uses integer,
    uses integer not null,
    created_by varchar(64),
    created_at timestamp(6) with time zone not null,
    revoked_at timestamp(6) with time zone,
    last_used_at timestamp(6) with time zone,
    restriction_snapshot jsonb,
    primary key (id)
);
alter table share_link add constraint share_link_key_hash_idx unique (key_hash);
create index share_link_created_by_idx on share_link (created_by, id);

-- ===== chronicle (prompt 131) =====

-- ChronicleEntity
create table chronicle (
    id varchar(26) not null,
    display_id varchar(32) not null,
    name varchar(200) not null,
    description text,
    status varchar(16) not null,
    area jsonb,
    started_at timestamp(6) with time zone,
    stopped_at timestamp(6) with time zone,
    capture_from_seq bigint,
    capture_to_seq bigint,
    cursor_seq bigint,
    last_capture_at timestamp(6) with time zone,
    retention varchar(32) not null,
    size_cap_bytes bigint not null,
    bytes_used bigint not null,
    rows_captured bigint not null,
    cap_state varchar(16) not null,
    workspace_id varchar(26),
    situation_ids jsonb not null,
    hypothesis_set_ids jsonb not null,
    investigation_ids jsonb not null,
    topic_ids jsonb not null,
    origin varchar(16) not null,
    proposal_id varchar(26),
    auto_activated_at timestamp(6) with time zone,
    leaf_created boolean not null,
    archive jsonb,
    history_expired_at timestamp(6) with time zone,
    created_by varchar(200),
    discoverability varchar(16),
    reveal_title boolean,
    reveal_owner boolean,
    discoverability_updated_at timestamp(6) with time zone,
    version integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index chronicle_status_idx on chronicle (status);
alter table chronicle add constraint chronicle_display_idx unique (display_id);

-- ChronicleLayerEntity
create table chronicle_layer (
    id varchar(26) not null,
    chronicle_id varchar(26) not null,
    name varchar(200) not null,
    selector jsonb not null,
    area_override jsonb,
    capture jsonb not null,
    enabled boolean not null,
    added_at timestamp(6) with time zone not null,
    added_at_seq bigint,
    removed_at timestamp(6) with time zone,
    estimate jsonb,
    rows_captured bigint not null,
    bytes_captured bigint not null,
    frame_counter bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index chronicle_layer_chronicle_idx on chronicle_layer (chronicle_id);

-- ChronicleEventEntity
create table chronicle_event (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    type varchar(32) not null,
    at timestamp(6) with time zone not null,
    seq bigint,
    layer_id varchar(26),
    title varchar(512) not null,
    summary text,
    ref varchar(600),
    snapshot jsonb,
    gap jsonb,
    backfilled boolean not null,
    private boolean not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index chronicle_event_chronicle_idx on chronicle_event (chronicle_id, id);
create index chronicle_event_at_idx on chronicle_event (chronicle_id, at);

-- ChronicleFrameEntity
create table chronicle_frame (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    layer_id varchar(26) not null,
    product_id varchar(64) not null,
    frame_key varchar(128) not null,
    valid_time timestamp(6) with time zone,
    seq bigint,
    captured_at timestamp(6) with time zone not null,
    primary key (id),
    constraint chronicle_frame_key unique (chronicle_id, product_id, frame_key)
);
create index chronicle_frame_pin_idx on chronicle_frame (product_id, frame_key);

-- ChronicleReadCursorEntity
create table chronicle_read_cursor (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    principal_id varchar(200) not null,
    up_to_id bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint chronicle_read_cursor_key unique (chronicle_id, principal_id)
);

-- ChronicleRollupEntity
create table chronicle_rollup (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    layer_id varchar(26) not null,
    region varchar(16) not null,
    hour_start timestamp(6) with time zone not null,
    versions bigint not null,
    backfilled boolean not null,
    primary key (id),
    constraint chronicle_rollup_key unique (chronicle_id, layer_id, region, hour_start)
);

-- ChronicleSeriesPointEntity
create table chronicle_series_point (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    layer_id varchar(26) not null,
    ref varchar(600) not null,
    period varchar(64),
    period_start timestamp(6) with time zone not null,
    period_end timestamp(6) with time zone,
    value double precision,
    unit varchar(64),
    status varchar(16),
    point_version integer,
    known_at timestamp(6) with time zone,
    coverage_ok boolean,
    seq bigint,
    backfilled boolean not null,
    captured_at timestamp(6) with time zone not null,
    primary key (id),
    constraint chronicle_series_point_key unique (chronicle_id, ref, period_start)
);

-- ===== chronicle.network (prompt 134) =====

-- ChronicleSaltEntity
create table chronicle_salt (
    id varchar(64) not null,
    kind varchar(16) not null,
    network_id varchar(64),
    ciphertext bytea not null,
    nonce bytea not null,
    created_at timestamp(6) with time zone not null,
    rotates_at timestamp(6) with time zone not null,
    retired_at timestamp(6) with time zone,
    primary key (id)
);
create index chronicle_salt_kind_idx on chronicle_salt (kind, network_id);

-- ChronicleKeyEntity
create table chronicle_key (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    salt_id varchar(64) not null,
    key_type varchar(16) not null,
    hkey bigint not null,
    first_seen timestamp(6) with time zone not null,
    key_text varchar(700),
    ref varchar(200),
    primary key (id),
    constraint chronicle_key_unique unique (chronicle_id, salt_id, key_type, hkey)
);
create index chronicle_key_hkey_idx on chronicle_key (salt_id, key_type, hkey);

-- ChronicleFingerprintEntity
create table chronicle_fingerprint (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    key_type varchar(16) not null,
    salt_id varchar(64) not null,
    minhash bytea not null,
    size bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint chronicle_fingerprint_unique unique (chronicle_id, key_type, salt_id)
);

-- ChronicleSketchStateEntity
create table chronicle_sketch_state (
    id bigint generated by default as identity,
    chronicle_id varchar(26) not null,
    salt_id varchar(64) not null,
    key_seq bigint not null,
    total_keys bigint not null,
    discovered_keys bigint not null,
    discovery_due boolean not null,
    sketched_at timestamp(6) with time zone,
    sketch_id varchar(32),
    sent_at timestamp(6) with time zone,
    withdrawn_at timestamp(6) with time zone,
    primary key (id),
    constraint chronicle_sketch_state_unique unique (chronicle_id, salt_id)
);

-- ChronicleRemoteSketchEntity
create table chronicle_remote_sketch (
    id varchar(26) not null,
    peer_id varchar(64),
    instance_id varchar(128) not null,
    network_id varchar(64) not null,
    salt_id varchar(64) not null,
    sketch_id varchar(64) not null,
    key_types jsonb not null,
    minhash jsonb not null,
    size_band jsonb not null,
    capture_from varchar(10) not null,
    capture_to varchar(10),
    title varchar(200),
    owner_handle varchar(64),
    tlp varchar(16) not null,
    issued_at timestamp(6) with time zone not null,
    received_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    primary key (id),
    constraint chronicle_remote_sketch_key unique (instance_id, sketch_id)
);
create index chronicle_remote_sketch_salt_idx on chronicle_remote_sketch (salt_id);
create index chronicle_remote_sketch_expiry_idx on chronicle_remote_sketch (expires_at);

-- ChronicleLinkEntity
create table chronicle_link (
    id varchar(26) not null,
    a varchar(26) not null,
    b varchar(300) not null,
    scores jsonb not null,
    dimensions jsonb not null,
    temporal varchar(16) not null,
    time_overlap varchar(16) not null,
    shared_situations jsonb not null,
    shared_key_count bigint not null,
    strength varchar(8) not null,
    salt_id varchar(64) not null,
    first_found timestamp(6) with time zone not null,
    last_updated timestamp(6) with time zone not null,
    status varchar(16) not null,
    dismissed_by varchar(200),
    primary key (id),
    constraint chronicle_link_pair unique (a, b)
);
create index chronicle_link_a_idx on chronicle_link (a);
create index chronicle_link_b_idx on chronicle_link (b);

-- ChronicleLinkDisclosureEntity
create table chronicle_link_disclosure (
    id bigint generated by default as identity,
    principal_id varchar(200) not null,
    link_id varchar(26) not null,
    disclosed_at timestamp(6) with time zone not null,
    primary key (id),
    constraint chronicle_link_disclosure_unique unique (principal_id, link_id)
);
create index chronicle_link_disclosure_time_idx on chronicle_link_disclosure (principal_id, disclosed_at);

-- ChronicleAccessRequestEntity
create table chronicle_access_request (
    id varchar(26) not null,
    link_id varchar(26) not null,
    requester_principal varchar(200),
    requester_handle varchar(64) not null,
    requester_instance varchar(128),
    remote_request_id varchar(26),
    target_chronicle_id varchar(26),
    target_node varchar(300),
    network_id varchar(64),
    owner_principal varchar(200),
    message varchar(1000),
    offer_share varchar(26),
    offer_remote boolean not null,
    status varchar(16) not null,
    suppressed boolean not null,
    grant_info jsonb,
    created_at timestamp(6) with time zone not null,
    decided_at timestamp(6) with time zone,
    primary key (id)
);
create index chronicle_access_request_requester_idx on chronicle_access_request (requester_principal, created_at);
create index chronicle_access_request_owner_idx on chronicle_access_request (owner_principal, created_at);
create index chronicle_access_request_link_idx on chronicle_access_request (link_id);

-- ChronicleClusterEntity
create table chronicle_cluster (
    id varchar(26) not null,
    members jsonb not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- ChronicleChurnEntity
create table chronicle_network_churn (
    id bigint generated by default as identity,
    owner varchar(200) not null,
    created_at timestamp(6) with time zone not null,
    deleted_at timestamp(6) with time zone not null,
    primary key (id)
);
create index chronicle_network_churn_owner_idx on chronicle_network_churn (owner, deleted_at);

-- ===== collab (prompt 130) =====

-- ShareableObjectEntity
create table shareable_object (
    object_ref varchar(128) not null,
    type varchar(32) not null,
    owner varchar(64) not null,
    visibility varchar(16) not null,
    tlp varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (object_ref)
);
create index shareable_object_owner_idx on shareable_object (owner, updated_at);
create index shareable_object_visibility_idx on shareable_object (visibility, updated_at);

-- ObjectAclEntity
create table object_acl (
    id varchar(26) not null,
    object_ref varchar(128) not null,
    grantee varchar(128) not null,
    role varchar(16) not null,
    granted_by varchar(64),
    granted_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone,
    primary key (id),
    constraint object_acl_object_grantee_key unique (object_ref, grantee)
);
create index object_acl_grantee_idx on object_acl (grantee, granted_at);

-- GroupEntity
create table principal_group (
    id varchar(26) not null,
    name varchar(200) not null,
    description text,
    created_by varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- GroupMemberEntity
create table group_member (
    id varchar(26) not null,
    group_id varchar(26) not null,
    principal_id varchar(64) not null,
    role varchar(16) not null,
    added_by varchar(64),
    added_at timestamp(6) with time zone not null,
    primary key (id),
    constraint group_member_key unique (group_id, principal_id)
);
create index group_member_principal_idx on group_member (principal_id);

-- TagEntity
create table tag (
    id varchar(26) not null,
    name varchar(64) not null,
    namespace varchar(16) not null,
    color varchar(7),
    description text,
    created_by varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index tag_name_idx on tag (namespace, name);

-- TagAssignmentEntity
create table tag_assignment (
    id varchar(26) not null,
    tag_id varchar(26) not null,
    object_ref varchar(1024) not null,
    assigned_by varchar(64),
    assigned_at timestamp(6) with time zone not null,
    primary key (id),
    constraint tag_assignment_key unique (tag_id, object_ref)
);
create index tag_assignment_object_idx on tag_assignment (object_ref);

-- AttachmentEntity
create table attachment (
    id varchar(26) not null,
    media_id varchar(80) not null,
    filename varchar(255) not null,
    content_type varchar(100) not null,
    size bigint not null,
    sha256 varchar(64) not null,
    storage_path varchar(200) not null,
    text text,
    text_truncated boolean not null,
    image_meta jsonb,
    uploaded_by varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index attachment_sha_idx on attachment (sha256);

-- EvidenceItemEntity
create table evidence_item (
    id varchar(26) not null,
    title varchar(300) not null,
    description text,
    attachment_ids jsonb not null,
    url varchar(2048),
    source_description varchar(2000),
    source_reliability varchar(1) not null,
    information_credibility integer not null,
    cited_source_id varchar(64),
    cited_refs jsonb not null,
    geom geometry(Geometry,4326),
    event_time timestamp(6) with time zone,
    created_by varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- InboxItemEntity
create table principal_inbox (
    id bigint generated by default as identity,
    principal_id varchar(64) not null,
    event varchar(64) not null,
    category varchar(32) not null,
    severity varchar(16) not null,
    title varchar(500) not null,
    body text,
    object_ref varchar(128),
    actor_id varchar(64),
    tlp varchar(16),
    payload jsonb,
    created_at timestamp(6) with time zone not null,
    read_at timestamp(6) with time zone,
    primary key (id)
);
create index principal_inbox_principal_idx on principal_inbox (principal_id, id);

-- PrincipalNotificationEntity
create table principal_notification (
    id varchar(26) not null,
    principal_id varchar(64) not null,
    event_types jsonb not null,
    channel_id uuid not null,
    language varchar(35),
    quiet_hours jsonb,
    enabled boolean not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index principal_notification_principal_idx on principal_notification (principal_id);

-- SavedSearchEntity
create table saved_search (
    id varchar(26) not null,
    name varchar(200) not null,
    query jsonb not null,
    notify boolean not null,
    last_run_at timestamp(6) with time zone,
    last_notified_at timestamp(6) with time zone,
    last_match_count integer,
    last_match_refs jsonb,
    created_by varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index saved_search_notify_idx on saved_search (notify, last_notified_at);

-- ===== reference (prompt 040) =====

-- RefDatasetEntity
create table ref_dataset (
    name varchar(64) not null,
    edition varchar(300),
    url varchar(1000),
    sha256 varchar(64),
    licence varchar(100),
    attribution varchar(1000),
    rows bigint,
    loaded_at timestamp(6) with time zone,
    state varchar(16),
    error varchar(2000),
    primary key (name)
);

-- RefCountryEntity
create table ref_country (
    iso2 varchar(2) not null,
    iso3 varchar(3),
    name varchar(200) not null,
    geom geometry(MultiPolygon,4326) not null,
    geom_simple geometry(MultiPolygon,4326),
    primary key (iso2)
);

-- RefAdmin1Entity
create table ref_admin1 (
    code varchar(32) not null,
    iso2 varchar(2),
    name varchar(200) not null,
    gn_code varchar(32),
    geom geometry(MultiPolygon,4326) not null,
    geom_simple geometry(MultiPolygon,4326),
    primary key (code)
);

-- RefAdmin2Entity
create table ref_admin2 (
    fips5 varchar(5) not null,
    state_code varchar(8),
    name varchar(200) not null,
    geom geometry(MultiPolygon,4326) not null,
    primary key (fips5)
);

-- RefSeaEntity
create table ref_sea (
    id bigint not null,
    name varchar(200) not null,
    geom geometry(MultiPolygon,4326) not null,
    primary key (id)
);

-- GazetteerCountryEntity
create table gazetteer_country (
    iso2 varchar(2) not null,
    iso3 varchar(3),
    iso_numeric varchar(3),
    fips varchar(2),
    name varchar(200) not null,
    capital varchar(200),
    population bigint,
    continent varchar(2),
    geonameid bigint,
    primary key (iso2)
);

-- GazetteerAdmin1Entity
create table gazetteer_admin1 (
    code varchar(32) not null,
    name varchar(200) not null,
    ascii_name varchar(200),
    geonameid bigint,
    primary key (code)
);

-- GazetteerPlaceEntity
create table gazetteer_place (
    geonameid bigint not null,
    name varchar(200) not null,
    ascii_name varchar(200),
    iso2 varchar(2),
    admin1_code varchar(20),
    feature_code varchar(10),
    population bigint,
    timezone varchar(40),
    geog geography(Point,4326) not null,
    primary key (geonameid)
);

-- RecordRegionEntity (@EmbeddedId ai.foresignal.store.RecordHeadId)
create table record_region (
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    iso2 varchar(2),
    admin1 varchar(32),
    admin2 varchar(5),
    sea varchar(200),
    near_id bigint,
    near_name varchar(200),
    near_km double precision,
    near_bearing double precision,
    tagged_lon double precision not null,
    tagged_lat double precision not null,
    tagged_at timestamp(6) with time zone not null,
    primary key (source_id, external_id)
);

-- ===== inbound (prompt 024; device 039; edp/binding 138) =====

-- CustomSourceEntity
create table custom_source (
    source_id varchar(64) not null,
    origin varchar(16) not null,
    name varchar(200) not null,
    description text,
    kinds jsonb not null,
    attribution varchar(500),
    record_ttl varchar(32),
    tile_properties jsonb,
    search_fields jsonb,
    embed_fields jsonb,
    rank_field varchar(64),
    rate_limit_per_minute integer not null,
    observed_at_mode varchar(16) not null,
    ttl_anchor varchar(16) not null,
    volatile_fields jsonb,
    significant_fields jsonb,
    title_field varchar(64),
    field_specs jsonb,
    private_source boolean not null,
    licence varchar(200),
    licence_terms text,
    records_per_minute integer not null,
    formats jsonb,
    verifiers jsonb,
    verifier_secret_ciphertext bytea,
    verifier_secret_nonce bytea,
    profile varchar(32),
    allow_path_key boolean,
    ingest_allow_cidrs jsonb,
    mapping jsonb,
    default_format varchar(32),
    preset varchar(32),
    capture_samples_until timestamp(6) with time zone,
    ingest_key_hash varchar(64) not null,
    ingest_key_prefix varchar(8) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    last_ingest_at timestamp(6) with time zone,
    primary key (source_id)
);
alter table custom_source add constraint custom_source_key_hash_idx unique (ingest_key_hash);

-- IngestDeviceEntity
create table ingest_device (
    id uuid not null,
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    name varchar(200) not null,
    key_hash varchar(64) not null,
    key_prefix varchar(8) not null,
    created_at timestamp(6) with time zone not null,
    last_seen_at timestamp(6) with time zone,
    revoked_at timestamp(6) with time zone,
    primary key (id)
);
alter table ingest_device add constraint ingest_device_key_hash_idx unique (key_hash);
create index ingest_device_source_idx on ingest_device (source_id);

-- ExternalSourceEntity
create table external_source (
    source_id varchar(64) not null,
    manifest jsonb not null,
    manifest_sha256 varchar(64) not null,
    state varchar(16) not null,
    registered_via varchar(32) not null,
    approved_by varchar(200),
    approved_at timestamp(6) with time zone,
    allow_private_network boolean not null,
    pull_cursor varchar(2048),
    last_poll_at timestamp(6) with time zone,
    last_pull_error varchar(2000),
    last_item_at timestamp(6) with time zone,
    stats jsonb,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (source_id)
);

-- RegistrationTokenEntity
create table edp_registration_token (
    id varchar(26) not null,
    name varchar(200) not null,
    token_hash varchar(64) not null,
    prefix varchar(8) not null,
    expires_at timestamp(6) with time zone,
    max_uses integer not null,
    uses integer not null,
    source_id_prefix varchar(48),
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
alter table edp_registration_token add constraint edp_registration_token_hash_idx unique (token_hash);

-- EdpSnapshotEntity
create table edp_snapshot (
    snapshot_key varchar(200) not null,
    source_id varchar(64) not null,
    snapshot_id varchar(128) not null,
    stream varchar(48) not null,
    scope varchar(256),
    pages text not null,
    final_page integer,
    failed_reason varchar(500),
    held boolean not null,
    first_page_at timestamp(6) with time zone not null,
    primary key (snapshot_key)
);

-- EdpSnapshotStageEntity
create table edp_snapshot_stage (
    id bigint generated by default as identity,
    snapshot_key varchar(200) not null,
    external_id varchar(320) not null,
    primary key (id)
);
alter table edp_snapshot_stage add constraint edp_snapshot_stage_key_idx unique (snapshot_key, external_id);

-- ExternalBindingEntity
create table external_binding (
    id varchar(26) not null,
    source_id varchar(64) not null,
    stream_id varchar(48) not null,
    target varchar(32) not null,
    config jsonb,
    filter jsonb,
    enabled boolean not null,
    backfill boolean not null,
    state varchar(16) not null,
    applied bigint not null,
    last_error varchar(2000),
    target_ref varchar(600),
    latest jsonb,
    manifest_sha256 varchar(64),
    created_by varchar(200) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    last_applied_at timestamp(6) with time zone,
    primary key (id)
);
create index external_binding_source_idx on external_binding (source_id, stream_id);
create index external_binding_target_idx on external_binding (target);

-- ExternalBindingAppliedEntity
create table external_binding_applied (
    applied_key varchar(64) not null,
    binding_id varchar(26) not null,
    external_id varchar(600) not null,
    version integer,
    seq bigint not null,
    action varchar(64) not null,
    target_ref varchar(600),
    applied_at timestamp(6) with time zone not null,
    primary key (applied_key)
);
create index external_binding_applied_binding_idx on external_binding_applied (binding_id, applied_at);

-- ExternalQuestionLinkEntity
create table external_question_link (
    id varchar(26) not null,
    source_id varchar(64) not null,
    stream_id varchar(48) not null,
    external_question_id varchar(256) not null,
    question_id varchar(26) not null,
    orientation varchar(8) not null,
    forecaster_id varchar(128) not null,
    created_by varchar(200) not null,
    created_at timestamp(6) with time zone not null,
    last_forecast_at timestamp(6) with time zone,
    primary key (id),
    constraint external_question_link_key unique (source_id, stream_id, external_question_id)
);
create index external_question_link_question_idx on external_question_link (question_id);

-- ===== trigger (prompt unknown) =====

-- TriggerAnchorStateEntity (@EmbeddedId TriggerAnchorStateEntity.Key)
create table trigger_anchor_state (
    rule_id uuid not null,
    anchor_source varchar(64) not null,
    anchor_external_id varchar(256) not null,
    eval_geom geometry(Point,4326),
    eval_seq bigint,
    eval_at timestamp(6) with time zone,
    status varchar(8) not null,
    primary key (rule_id, anchor_source, anchor_external_id)
);

-- WindowStateEntity (@IdClass WindowStateEntity.Key)
create table window_state (
    rule_id uuid not null,
    group_key varchar(256) not null,
    state varchar(16) not null,
    since timestamp(6) with time zone,
    last_value double precision,
    last_evaluated_at timestamp(6) with time zone,
    last_watermark_seq bigint,
    reason varchar(255),
    last_alert_id bigint,
    alert_pending boolean,
    alert_pending_since timestamp(6) with time zone,
    pending_context text,
    pending_message text,
    primary key (rule_id, group_key)
);

-- ActivitySampleEntity (@IdClass ActivitySampleEntity.Key)
create table activity_sample (
    rule_id uuid not null,
    group_key varchar(256) not null,
    bucket_start timestamp(6) with time zone not null,
    count integer not null,
    coverage_ratio double precision,
    coverage_ok boolean not null,
    flagged boolean not null,
    primary key (bucket_start, rule_id, group_key)
);

-- AnomalyObservationEntity
create table anomaly_observation (
    id bigint generated by default as identity,
    rule_id uuid not null,
    group_key varchar(256) not null,
    bucket_start timestamp(6) with time zone not null,
    bucket_end timestamp(6) with time zone not null,
    observed integer not null,
    expected double precision,
    exposure double precision,
    p_value double precision,
    score double precision,
    direction varchar(8),
    classification varchar(32) not null,
    low_confidence boolean not null,
    baseline jsonb,
    detector jsonb,
    evidence jsonb,
    watermark_seq bigint,
    manifest_id varchar(26),
    primary key (id)
);

-- ===== notify (prompt 018; push 032; inbox and action 033) =====

-- PushProviderEntity
create table push_provider (
    id varchar(64) not null,
    type varchar(8) not null,
    app_ids jsonb not null,
    config jsonb not null,
    secret_ciphertext bytea,
    secret_nonce bytea,
    enabled boolean not null,
    last_test_at timestamp(6) with time zone,
    last_error varchar(500),
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- PushDeviceEntity
create table push_device (
    id uuid not null,
    transport varchar(8) not null,
    platform varchar(16) not null,
    app_id varchar(255) not null,
    environment varchar(16),
    token_ciphertext bytea not null,
    token_nonce bytea not null,
    token_hash varchar(64) not null,
    token_suffix varchar(6) not null,
    token_id uuid not null,
    name varchar(200),
    device_name varchar(200),
    app_version varchar(64),
    filters jsonb default '{}'::jsonb not null,
    content_level varchar(16) not null,
    lang varchar(35),
    status varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    last_success_at timestamp(6) with time zone,
    last_failure_at timestamp(6) with time zone,
    failure_count integer default 0 not null,
    last_status integer,
    primary key (id),
    constraint push_device_token_uq unique (transport, token_hash)
);
create index push_device_token_idx on push_device (token_id);
create index push_device_status_idx on push_device (status, created_at);

-- NotificationReadEntity (@IdClass NotificationReadEntity.Key)
create table notification_read (
    device_key varchar(160) not null,
    notification_id bigint not null,
    primary key (notification_id, device_key)
);

-- NotificationReadCursorEntity
create table notification_read_cursor (
    device_key varchar(160) not null,
    read_up_to_id bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (device_key)
);

-- ClientPresenceEntity
create table client_presence (
    device_key varchar(160) not null,
    kind varchar(16) not null,
    state varchar(16) not null,
    expires_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (device_key)
);

-- ActionCapabilityEntity
create table action_capability (
    id bigint generated by default as identity,
    token_hash varchar(64) not null,
    alert_id bigint not null,
    action varchar(16) not null,
    params jsonb default '{}'::jsonb not null,
    channel_id uuid,
    delivery_id bigint,
    device_id varchar(160),
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    used_at timestamp(6) with time zone,
    used_via varchar(200),
    result varchar(64),
    primary key (id)
);
alter table action_capability add constraint action_capability_token_idx unique (token_hash);
create index action_capability_alert_idx on action_capability (alert_id);
create index action_capability_expires_idx on action_capability (expires_at);

-- ===== chatops (prompt 061) =====

-- ChatopsPendingEntity
create table chatops_pending (
    token varchar(64) not null,
    channel_id uuid not null,
    chat_id varchar(255) not null,
    user_id varchar(255),
    action varchar(32) not null,
    ref varchar(128) not null,
    expires_at timestamp(6) with time zone not null,
    used_at timestamp(6) with time zone,
    primary key (token)
);
create index chatops_pending_expires_idx on chatops_pending (expires_at);

-- ChatopsCursorEntity
create table chatops_cursor (
    channel_id uuid not null,
    cursor text not null,
    updated_at timestamp(6) with time zone not null,
    primary key (channel_id)
);

-- ===== hypothesis (prompt 125) =====

-- AchEvidenceEntity
create table ach_evidence (
    credibility varchar(1) not null,
    relevance varchar(1) not null,
    added_at timestamp(6) with time zone not null,
    removed_at timestamp(6) with time zone,
    evidence_group_id varchar(26),
    id varchar(26) not null,
    indicator_id varchar(26),
    set_id varchar(26) not null,
    source varchar(32) not null,
    added_by varchar(128) not null,
    evidence_group_key varchar(128),
    ref varchar(512),
    description varchar(2000) not null,
    applied jsonb not null,
    caveats jsonb,
    citations jsonb not null,
    provenance_chain jsonb,
    ratings jsonb not null,
    refs jsonb not null,
    primary key (id)
);
create index ach_evidence_set_idx on ach_evidence (set_id);
create index ach_evidence_group_idx on ach_evidence (set_id, evidence_group_key);

-- CollectionGapEntity
create table collection_gap (
    actual_eig double precision not null,
    created_at timestamp(6) with time zone not null,
    p_detect double precision,
    potential_eig double precision not null,
    updated_at timestamp(6) with time zone not null,
    id varchar(26) not null,
    indicator_id varchar(26) not null,
    set_id varchar(26) not null,
    recommendations jsonb not null,
    primary key (id),
    constraint collection_gap_indicator_uq unique (indicator_id)
);
create index collection_gap_set_idx on collection_gap (set_id);

-- HypothesisEntity
create table hypothesis (
    position integer not null,
    version integer not null,
    created_at timestamp(6) with time zone not null,
    prior double precision not null,
    probability double precision not null,
    probability_cap double precision,
    updated_at timestamp(6) with time zone not null,
    confidence varchar(16) not null,
    status varchar(16) not null,
    id varchar(26) not null,
    set_id varchar(26) not null,
    estimative_term varchar(32) not null,
    type varchar(32) not null,
    ledger_outcome varchar(64),
    label varchar(80) not null,
    statement varchar(2000) not null,
    alternatives jsonb not null,
    cap_override jsonb,
    coincidence_baseline jsonb,
    confidence_basis jsonb not null,
    linked_finding_ids jsonb not null,
    prior_basis jsonb not null,
    primary key (id)
);
create index hypothesis_set_idx on hypothesis (set_id);

-- HypothesisLogEntity
create table hypothesis_log (
    private boolean not null,
    at timestamp(6) with time zone not null,
    id bigint generated by default as identity,
    set_id varchar(26) not null,
    event varchar(32) not null,
    payload jsonb not null,
    primary key (id)
);
create index hypothesis_log_set_idx on hypothesis_log (set_id, at);

-- HypothesisRevisionEntity
create table hypothesis_revision (
    band_changed boolean not null,
    override boolean not null,
    asof_seq bigint not null,
    at timestamp(6) with time zone not null,
    rationale_origin varchar(16) not null,
    id varchar(26) not null,
    ledger_forecast_id varchar(26),
    set_id varchar(26) not null,
    cause varchar(32) not null,
    updater_version varchar(64) not null,
    actor varchar(128) not null,
    rationale text not null,
    after_state jsonb not null,
    before_state jsonb not null,
    citations jsonb not null,
    drivers jsonb not null,
    primary key (id)
);
create index hypothesis_revision_set_idx on hypothesis_revision (set_id, at);

-- HypothesisSetEntity
create table hypothesis_set (
    horizon date,
    private boolean not null,
    review_flagged boolean not null,
    version integer not null,
    created_at timestamp(6) with time zone not null,
    last_band_notified_at timestamp(6) with time zone,
    last_reviewed_at timestamp(6) with time zone,
    last_revision_at timestamp(6) with time zone,
    next_review_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone not null,
    cadence varchar(16) not null,
    confidence varchar(16) not null,
    kind varchar(16) not null,
    origin varchar(16) not null,
    status varchar(16) not null,
    id varchar(26) not null,
    last_revision_id varchar(26),
    question_id varchar(26),
    situation_id varchar(26),
    resolved_outcome varchar(64),
    created_by varchar(128),
    question varchar(300) not null,
    description varchar(4000),
    pending_band_change jsonb,
    region_codes text[],
    themes text[],
    primary key (id)
);
create index hypothesis_set_status_idx on hypothesis_set (status, cadence);
create index hypothesis_set_situation_idx on hypothesis_set (situation_id);
create index hypothesis_set_question_idx on hypothesis_set (question_id);

-- IndicatorEntity
create table indicator (
    credibility varchar(1) not null,
    relevance varchar(1) not null,
    version integer not null,
    created_at timestamp(6) with time zone not null,
    expected_by timestamp(6) with time zone,
    last_state_change_at timestamp(6) with time zone,
    p_detect double precision,
    updated_at timestamp(6) with time zone not null,
    emerging_rule_id uuid,
    origin varchar(16) not null,
    watch_rule_id uuid,
    id varchar(26) not null,
    set_id varchar(26) not null,
    state varchar(32) not null,
    signal_family varchar(64) not null,
    description varchar(2000) not null,
    absence_lr jsonb not null,
    lr jsonb not null,
    observed_evidence jsonb,
    signal jsonb not null,
    primary key (id)
);
create index indicator_set_idx on indicator (set_id);
create index indicator_state_idx on indicator (state, expected_by);
create index indicator_rule_idx on indicator (watch_rule_id);

-- KeyAssumptionEntity
create table key_assumption (
    linchpin boolean not null,
    review_due timestamp(6) with time zone,
    updated_at timestamp(6) with time zone not null,
    status varchar(16) not null,
    id varchar(26) not null,
    set_id varchar(26) not null,
    updated_by varchar(128),
    text varchar(2000) not null,
    rationale varchar(4000),
    would_be_wrong_if varchar(4000),
    indicator_ids jsonb not null,
    primary key (id)
);
create index key_assumption_set_idx on key_assumption (set_id);

-- ===== outlook (prompt 126) =====

-- RegionalIndexDefEntity (outlook.index)
create table regional_index_def (
    auto_apply_fitted_weights boolean not null,
    built_in boolean not null,
    enabled boolean not null,
    weights_version integer not null,
    created_at timestamp(6) with time zone not null,
    row_version bigint not null,
    updated_at timestamp(6) with time zone not null,
    weights_origin varchar(16) not null,
    id varchar(64) not null,
    target varchar(128) not null,
    name varchar(200) not null,
    description text,
    components jsonb,
    default_weights jsonb,
    floors jsonb,
    skill jsonb,
    weights jsonb,
    weights_log jsonb,
    primary key (id)
);

-- RegionalIndexFitEntity (outlook.index)
create table regional_index_fit (
    applied boolean not null,
    origins integer,
    regions integer,
    weights_version integer,
    applied_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    lambda double precision,
    started_at timestamp(6) with time zone,
    status varchar(16) not null,
    id varchar(26) not null,
    manifest_id varchar(26),
    index_id varchar(64) not null,
    requested_by varchar(200),
    error text,
    components jsonb,
    proposed_weights jsonb,
    skill jsonb,
    primary key (id)
);
create index regional_index_fit_index_idx on regional_index_fit (index_id, created_at);

-- RegionalIndexValueEntity (outlook.index)
create table regional_index_value (
    day date not null,
    weights_version integer not null,
    asof_seq bigint not null,
    computed_at timestamp(6) with time zone not null,
    delta_24h double precision,
    delta_7d double precision,
    value double precision not null,
    region varchar(16) not null,
    id varchar(26) not null,
    manifest_id varchar(26),
    def_id varchar(64) not null,
    components jsonb,
    floors_applied jsonb,
    primary key (id)
);
create index regional_index_value_def_region_idx on regional_index_value (def_id, region, day);
create index regional_index_value_def_day_idx on regional_index_value (def_id, day);
alter table regional_index_value add constraint regional_index_value_uq unique (def_id, region, day);

-- RegionalOutlookEntity (outlook.regional)
create table regional_outlook (
    latest boolean not null,
    low_detectability boolean not null,
    low_reporting_density boolean not null,
    asof_seq bigint not null,
    published_at timestamp(6) with time zone not null,
    region_level varchar(8) not null,
    updated_at timestamp(6) with time zone not null,
    region varchar(16) not null,
    id varchar(26) not null,
    manifest_id varchar(26),
    source varchar(32),
    model_version varchar(40) not null,
    category varchar(64) not null,
    family varchar(64) not null,
    caveats jsonb,
    skill jsonb,
    windows jsonb,
    primary key (id)
);
create index regional_outlook_region_family_idx on regional_outlook (region, family, published_at);
create index regional_outlook_updated_idx on regional_outlook (updated_at);
create index regional_outlook_latest_idx on regional_outlook (latest, family, region);

-- RegionalOutlookModelEntity (outlook.regional)
create table regional_outlook_model (
    fitted_at timestamp(6) with time zone not null,
    region varchar(16) not null,
    manifest_id varchar(26),
    version varchar(32) not null,
    family varchar(64) not null,
    id varchar(120) not null,
    evaluation jsonb,
    fit jsonb,
    primary key (id)
);
create index regional_outlook_model_family_idx on regional_outlook_model (family);

-- SeriesForecastEntity (outlook.series)
create table series_forecast (
    exceedance_checked boolean not null,
    horizon integer not null,
    local_only boolean not null,
    asof_seq bigint not null,
    made_at timestamp(6) with time zone not null,
    target_end timestamp(6) with time zone not null,
    target_start timestamp(6) with time zone not null,
    finding_id varchar(26),
    id varchar(26) not null,
    manifest_id varchar(26),
    question_id varchar(26),
    method varchar(32) not null,
    version varchar(32) not null,
    unit varchar(64),
    ref varchar(600) not null,
    quantiles jsonb not null,
    standardization jsonb,
    tails jsonb,
    primary key (id)
);
create index series_forecast_ref_idx on series_forecast (ref, made_at);
create index series_forecast_target_idx on series_forecast (ref, horizon, target_start);
create index series_forecast_check_idx on series_forecast (exceedance_checked, horizon);

-- SeriesForecastModelEntity (outlook.series)
create table series_forecast_model (
    bucket_days integer not null,
    counts boolean not null,
    season integer not null,
    selected_at timestamp(6) with time zone not null,
    manifest_id varchar(26),
    method varchar(32) not null,
    version varchar(32) not null,
    ref varchar(600) not null,
    backtest jsonb,
    params jsonb,
    primary key (ref)
);

-- ===== forecast (prompt 122) =====

-- ForecastCalibrationEntity
create table forecast_calibration (
    platt_n integer not null,
    weight_n integer not null,
    platt_a double precision,
    platt_b double precision,
    platt_fitted_at timestamp(6) with time zone,
    rel_brier double precision,
    updated_at timestamp(6) with time zone not null,
    category varchar(64) not null,
    forecaster_id varchar(128) not null,
    id varchar(200) not null,
    primary key (id)
);

-- ForecastEntity
create table forecast (
    knowledge_cutoff date,
    recalibrated boolean not null,
    asof_seq bigint not null,
    made_at timestamp(6) with time zone not null,
    probability double precision,
    id varchar(26) not null,
    manifest_id varchar(26),
    question_id varchar(26) not null,
    supersedes_id varchar(26),
    forecaster_type varchar(32) not null,
    forecaster_id varchar(128) not null,
    rationale text,
    citations jsonb,
    claims jsonb,
    distribution jsonb,
    inputs jsonb,
    quantiles jsonb,
    primary key (id)
);
create index forecast_question_forecaster_idx on forecast (question_id, forecaster_id, made_at desc);
create index forecast_forecaster_idx on forecast (forecaster_id, made_at);

-- ForecasterEntity
create table forecaster (
    active boolean not null,
    created_at timestamp(6) with time zone not null,
    type varchar(32) not null,
    version varchar(32) not null,
    id varchar(128) not null,
    name varchar(200) not null,
    config jsonb not null,
    primary key (id)
);

-- ForecastLogEntity
create table forecast_log (
    private boolean not null,
    at timestamp(6) with time zone not null,
    id bigint generated by default as identity,
    question_id varchar(26) not null,
    event varchar(32) not null,
    payload jsonb not null,
    primary key (id)
);
create index forecast_log_question_idx on forecast_log (question_id, at);

-- ForecastQuestionEntity
create table forecast_question (
    private boolean not null,
    version integer not null,
    closes_at timestamp(6) with time zone not null,
    created_at timestamp(6) with time zone not null,
    ensemble_probability double precision,
    opens_at timestamp(6) with time zone not null,
    resolution_requested_at timestamp(6) with time zone,
    resolve_by timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    origin varchar(16) not null,
    status varchar(16) not null,
    type varchar(16) not null,
    finding_id varchar(26),
    hypothesis_set_id varchar(26),
    id varchar(26) not null,
    manifest_id varchar(26),
    outlook_id varchar(26),
    precursor_id varchar(26),
    situation_id varchar(26),
    resolver_type varchar(32) not null,
    category varchar(64) not null,
    created_by varchar(200),
    dedup_key varchar(200),
    title varchar(300) not null,
    series_ref varchar(500),
    resolution_criteria varchar(2000) not null,
    annulment jsonb,
    baseline_notes jsonb,
    outcomes jsonb not null,
    resolution jsonb,
    resolver jsonb not null,
    tags text[],
    primary key (id)
);
create index forecast_question_status_due_idx on forecast_question (status, resolve_by);
create index forecast_question_category_idx on forecast_question (category, status);
create index forecast_question_dedup_idx on forecast_question (dedup_key);

-- ForecastScoreEntity
-- @Index(name = "forecast_score_key_idx", unique = true): Hibernate 7.4 IndexBinder turns a unique @Index into a
-- UniqueKey (a constraint, not an index) when the dialect supports unique constraints, so it is inline here.
create table forecast_score (
    hedged_forecasts integer not null,
    n_days integer not null,
    n_forecasts integer not null,
    brier double precision,
    computed_at timestamp(6) with time zone not null,
    crps double precision,
    final_probability double precision,
    log_score double precision,
    observed double precision,
    resolved_at timestamp(6) with time zone not null,
    rps double precision,
    time_averaged_brier double precision,
    question_type varchar(16) not null,
    scorer_version varchar(16) not null,
    question_id varchar(26) not null,
    forecaster_type varchar(32) not null,
    category varchar(64) not null,
    forecaster_id varchar(128) not null,
    id varchar(160) not null,
    interval_hits jsonb,
    pinball jsonb,
    primary key (id),
    constraint forecast_score_key_idx unique (question_id, forecaster_id)
);
create index forecast_score_forecaster_idx on forecast_score (forecaster_id, category, resolved_at);

-- ===== baserate (prompt 121) =====

-- BaseRateEntity
create table base_rate (
    n_windows integer not null,
    period_from date,
    period_to date,
    computed_at timestamp(6) with time zone not null,
    interval_lower double precision not null,
    interval_upper double precision not null,
    n_events bigint not null,
    p_at_least_one double precision not null,
    rate_per_window double precision not null,
    window_code varchar(8) not null,
    origin varchar(16) not null,
    id varchar(26) not null,
    manifest_id varchar(26),
    region_class varchar(32),
    definition_key varchar(64) not null,
    family varchar(64) not null,
    created_by varchar(200),
    name varchar(200),
    caveats jsonb not null,
    computer jsonb not null,
    definition jsonb not null,
    licences jsonb not null,
    region_codes text[],
    source_mix jsonb not null,
    unconditional jsonb,
    primary key (id)
);
create index base_rate_key_idx on base_rate (definition_key, origin);
create index base_rate_family_idx on base_rate (family, window_code);

-- RegionProfileEntity
create table region_profile (
    density_decile integer not null,
    computed_at timestamp(6) with time zone not null,
    reporting_density double precision not null,
    region_code varchar(16) not null,
    primary key (region_code)
);

-- ReferenceImportEntity (baserate.reference)
create table reference_import (
    period_from date,
    period_to date,
    created_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    rows_read bigint not null,
    rows_written bigint not null,
    started_at timestamp(6) with time zone,
    dataset varchar(16) not null,
    egress varchar(16) not null,
    status varchar(16) not null,
    id varchar(26) not null,
    job_id varchar(26),
    file_sha256 varchar(64),
    import_key varchar(64) not null,
    licence varchar(64) not null,
    created_by varchar(200) not null,
    status_reason varchar(500),
    error text,
    params jsonb not null,
    progress jsonb not null,
    primary key (id)
);
create index reference_import_key_idx on reference_import (import_key);
create index reference_import_created_idx on reference_import (created_at);

-- ReferenceHistoryEntity (baserate.reference; @IdClass ReferenceHistoryEntity.Key)
create table reference_history (
    week_start date not null,
    count bigint not null,
    fatalities bigint,
    egress varchar(16) not null,
    region_code varchar(16) not null,
    import_id varchar(26),
    source varchar(32) not null,
    family varchar(64) not null,
    licence varchar(64) not null,
    primary key (week_start, region_code, source, family)
);
create index reference_history_import_idx on reference_history (import_id);
create index reference_history_region_idx on reference_history (region_code, family);

-- SituationOutcomeEntity (baserate.analog)
create table situation_outcome (
    is_final boolean not null,
    computed_at timestamp(6) with time zone not null,
    duration_days double precision,
    reference_at timestamp(6) with time zone not null,
    peak_severity varchar(16),
    situation_id varchar(26) not null,
    follow_on_30d text[],
    follow_on_7d text[],
    hypothesis_outcome jsonb,
    primary key (situation_id)
);

-- SituationSignatureEntity (baserate.analog; @IdClass SituationSignatureEntity.Key)
create table situation_signature (
    day_n integer not null,
    computed_at timestamp(6) with time zone not null,
    version varchar(16) not null,
    situation_id varchar(26) not null,
    embedding_ref varchar(400),
    feature_names text[] not null,
    features float8[] not null,
    primary key (day_n, situation_id)
);

-- ===== backtest (prompt 103) =====

-- BacktestJobEntity
create table backtest_job (
    cancelled boolean not null,
    range_clamped boolean,
    created_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    from_seq bigint,
    progress double precision not null,
    range_from timestamp(6) with time zone,
    range_to timestamp(6) with time zone,
    started_at timestamp(6) with time zone,
    to_seq bigint,
    status varchar(16) not null,
    id varchar(26) not null,
    manifest_id varchar(26),
    detector_version varchar(32),
    type varchar(32) not null,
    worker_instance varchar(36),
    created_by varchar(64),
    definition_hash varchar(64),
    target_id varchar(64),
    caveats jsonb,
    definition jsonb,
    result jsonb,
    primary key (id)
);

-- PrecursorTestEntity
create table precursor_test (
    created_at timestamp(6) with time zone not null,
    dedup_window_seconds bigint,
    max_lag_seconds bigint not null,
    min_lag_seconds bigint not null,
    id varchar(26) not null,
    created_by varchar(64),
    name varchar(200) not null,
    statement text,
    antecedent jsonb not null,
    consequent jsonb not null,
    spatial jsonb,
    primary key (id)
);

-- ===== benchmark (prompt 123) =====

-- BenchmarkCaseEntity
create table benchmark_case (
    created_at timestamp(6) with time zone not null,
    window_from timestamp(6) with time zone not null,
    window_to timestamp(6) with time zone not null,
    source varchar(16) not null,
    id varchar(26) not null,
    created_by varchar(200),
    name varchar(200) not null,
    description text,
    expectations jsonb not null,
    region_codes jsonb not null,
    primary key (id)
);
create index benchmark_case_source_idx on benchmark_case (source, id);

-- BenchmarkRunEntity
create table benchmark_run (
    created_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    seed bigint not null,
    started_at timestamp(6) with time zone,
    status varchar(8),
    suite varchar(16) not null,
    id varchar(26) not null,
    manifest_id varchar(26),
    run_trigger varchar(32) not null,
    thresholds_version varchar(32),
    scorer_version varchar(64) not null,
    suite_version varchar(64) not null,
    created_by varchar(200),
    error text,
    component_versions jsonb not null,
    components jsonb not null,
    params jsonb not null,
    results jsonb not null,
    primary key (id)
);
create index benchmark_run_finished_idx on benchmark_run (finished_at);
create index benchmark_run_suite_idx on benchmark_run (suite, id);

-- ===== intel (prompt 101) =====

-- CorrelationRecipeEntity
create table correlation_recipe (
    id varchar(64) not null,
    version integer not null,
    enabled boolean not null,
    definition jsonb not null,
    params jsonb,
    last_run_at timestamp(6) with time zone,
    last_error text,
    last_manifest_id varchar(26),
    primary key (id)
);

-- IntelligenceEventEntity
create table intelligence_event (
    id varchar(26) not null,
    type varchar(64) not null,
    title varchar(512) not null,
    severity varchar(16) not null,
    status varchar(16) not null,
    start_time timestamp(6) with time zone not null,
    end_time timestamp(6) with time zone,
    last_evidence_at timestamp(6) with time zone,
    geom geometry(Geometry,4326),
    centroid geometry(Point,4326),
    region_tags jsonb default '[]'::jsonb not null,
    confidence double precision,
    confidence_label varchar(16),
    confidence_method varchar(64),
    calibrated boolean default false not null,
    evidence_quality varchar(16),
    detector jsonb,
    manifest_id varchar(26),
    recipe_id varchar(64),
    recipe_version integer,
    origin varchar(16) not null,
    explanation text,
    alternatives jsonb,
    narrative text,
    operator_notes text,
    exposure jsonb,
    context jsonb,
    dedup_key varchar(256),
    merged_into varchar(26),
    private boolean default false not null,
    version integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    member_count integer,
    primary key (id)
);

-- IntelEventLogEntity
create table intelligence_event_log (
    id bigint generated by default as identity,
    event_id varchar(26) not null,
    at timestamp(6) with time zone not null,
    actor varchar(128),
    action varchar(32) not null,
    before jsonb,
    after jsonb,
    primary key (id)
);

-- IntelEventMemberEntity
create table intelligence_event_member (
    id bigint generated by default as identity,
    event_id varchar(26) not null,
    role varchar(16) not null,
    member_type varchar(16) not null,
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    seq bigint not null,
    version integer,
    kind varchar(64),
    observed_at timestamp(6) with time zone,
    join_reason jsonb,
    signal_family varchar(64),
    weight double precision,
    snapshot jsonb,
    added_at timestamp(6) with time zone not null,
    added_by varchar(128),
    primary key (id),
    constraint intelligence_event_member_pin_uq unique (event_id, member_type, source_id, external_id, seq)
);

-- InvestigationEntity
create table investigation (
    id varchar(26) not null,
    display_id varchar(32) not null,
    title varchar(512) not null,
    status varchar(16) not null,
    summary text,
    tags jsonb default '[]'::jsonb not null,
    workspace_id varchar(26),
    hypothesis_set_ids jsonb,
    question_ids jsonb,
    chronicle_ids jsonb,
    report_ids jsonb,
    created_by varchar(128),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint investigation_display_id_uq unique (display_id)
);

-- InvestigationItemEntity
create table investigation_item (
    id bigint generated by default as identity,
    investigation_id varchar(26) not null,
    item_type varchar(24) not null,
    ref varchar(256) not null,
    seq bigint,
    snapshot jsonb,
    private boolean default false not null,
    note text,
    added_at timestamp(6) with time zone not null,
    added_by varchar(128),
    primary key (id)
);

-- PopulationH3Entity (@IdClass PopulationH3Entity.Key: h3, res)
create table population_h3 (
    h3 bigint not null,
    res smallint not null,
    pop double precision not null,
    epoch smallint not null,
    primary key (res, h3)
);

-- ===== situation (prompt 118) =====

-- SituationEntity
create table situation (
    id varchar(26) not null,
    title varchar(300) not null,
    theme varchar(32) not null,
    status varchar(16) not null,
    severity varchar(16) not null,
    severity_override varchar(16),
    region_codes text[],
    geom geometry(Geometry,4326),
    centroid geometry(Point,4326),
    area_km2 double precision,
    started_at timestamp(6) with time zone not null,
    last_activity_at timestamp(6) with time zone not null,
    closed_at timestamp(6) with time zone,
    topic_ids text[],
    entity_ids text[],
    investigation_ids text[],
    summary text not null,
    brief text,
    brief_generated_at timestamp(6) with time zone,
    brief_model varchar(200),
    brief_manifest_id varchar(26),
    brief_core_mark integer default 0 not null,
    brief_pending boolean default false not null,
    origin varchar(16) not null,
    linker_version varchar(32),
    merged_into varchar(26),
    private boolean default false not null,
    member_count integer default 0 not null,
    signal_families text[],
    corroboration_share double precision,
    manifest_id varchar(26),
    version integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index situation_status_activity_idx on situation (status, last_activity_at desc);

-- SituationLogEntity
create table situation_log (
    id bigint generated by default as identity,
    situation_id varchar(26) not null,
    at timestamp(6) with time zone not null,
    actor varchar(200),
    action varchar(32) not null,
    event varchar(32),
    before jsonb,
    after jsonb,
    primary key (id)
);
create index situation_log_situation_idx on situation_log (situation_id, at);

-- SituationMemberEntity
create table situation_member (
    id varchar(26) not null,
    situation_id varchar(26) not null,
    member_type varchar(16) not null,
    ref varchar(600) not null,
    seq bigint,
    version integer,
    kind varchar(64),
    signal_family varchar(64),
    title varchar(512),
    severity varchar(16),
    theme varchar(32),
    observed_at timestamp(6) with time zone,
    role varchar(16) not null,
    join_reason jsonb,
    corroboration varchar(16) not null,
    geom geometry(Geometry,4326),
    region_codes text[],
    entity_ids text[],
    evidence_refs text[],
    source_id varchar(128),
    private_member boolean default false not null,
    added_at timestamp(6) with time zone not null,
    added_by varchar(200) not null,
    primary key (id),
    constraint situation_member_pin_key unique (situation_id, member_type, ref, seq)
);
create index situation_member_situation_idx on situation_member (situation_id, observed_at);
create index situation_member_ref_idx on situation_member (member_type, ref);

-- SituationSnapshotEntity (@IdClass SituationSnapshotEntity.Key: situationId, day)
create table situation_snapshot (
    situation_id varchar(26) not null,
    day date not null,
    status varchar(16) not null,
    severity varchar(16) not null,
    member_counts jsonb not null,
    new_members integer not null,
    area_km2 double precision,
    region_codes text[],
    key_values jsonb not null,
    asof_seq bigint not null,
    manifest_id varchar(26),
    created_at timestamp(6) with time zone not null,
    primary key (day, situation_id)
);

-- ThemeClassificationEntity
create table situation_theme_cache (
    text_hash varchar(64) not null,
    theme varchar(32) not null,
    model varchar(200),
    created_at timestamp(6) with time zone not null,
    primary key (text_hash)
);

-- ===== cycle (prompt 128) =====

-- AnalysisCycleEntity
create table analysis_cycle (
    id varchar(26) not null,
    name varchar(200) not null,
    cadence varchar(16) not null,
    schedule jsonb not null,
    scope jsonb not null,
    analyst_profile_id varchar(26),
    roles jsonb not null,
    forecaster_providers jsonb not null,
    samples integer not null,
    max_questions integer not null,
    max_tokens_per_cycle bigint not null,
    channel_ids jsonb not null,
    notify_min_priority varchar(8) not null,
    enabled boolean not null,
    built_in boolean not null,
    next_run_at timestamp(6) with time zone,
    last_run_id varchar(26),
    running_since timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- AssessmentEntity
create table assessment (
    id varchar(26) not null,
    cycle_id varchar(26),
    situation_id varchar(26),
    cadence varchar(16) not null,
    period_start timestamp(6) with time zone not null,
    period_end timestamp(6) with time zone not null,
    previous_assessment_id varchar(26),
    status varchar(16) not null,
    bluf jsonb not null,
    headline varchar(1000),
    sections jsonb not null,
    markdown text,
    html text,
    packet_ref varchar(26),
    roles jsonb not null,
    model varchar(200),
    effort varchar(8),
    manifest_id varchar(26),
    prompt_templates jsonb not null,
    citations jsonb not null,
    lint_warnings jsonb not null,
    private boolean not null,
    news_coverage jsonb,
    social jsonb,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index assessment_cadence_idx on assessment (cadence, id);
create index assessment_situation_idx on assessment (situation_id, id);

-- AssessmentLogEntity
create table assessment_log (
    id bigint generated by default as identity,
    event varchar(32) not null,
    payload jsonb not null,
    private boolean not null,
    at timestamp(6) with time zone not null,
    primary key (id)
);

-- CycleRunEntity
create table cycle_run (
    id varchar(26) not null,
    cycle_id varchar(26),
    situation_id varchar(26),
    cadence varchar(16) not null,
    run_trigger varchar(16) not null,
    status varchar(16) not null,
    period_start timestamp(6) with time zone not null,
    period_end timestamp(6) with time zone not null,
    previous_run_id varchar(26),
    started_at timestamp(6) with time zone,
    finished_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    packet jsonb,
    roles jsonb not null,
    requested_roles jsonb,
    requested_effort varchar(8),
    tokens_used bigint not null,
    assessment_id varchar(26),
    maintenance_id varchar(26),
    manifest_id varchar(26),
    warnings jsonb not null,
    error text,
    primary key (id)
);
create index cycle_run_cycle_idx on cycle_run (cycle_id, id);
create index cycle_run_situation_idx on cycle_run (situation_id, id);

-- MaintenanceRunEntity
create table maintenance_run (
    id varchar(26) not null,
    run_trigger varchar(16) not null,
    status varchar(16) not null,
    cycle_run_id varchar(26),
    started_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    steps jsonb not null,
    primary key (id)
);

-- ===== analyst (prompt 102) =====

-- AnalystFeedbackEntity
create table analyst_feedback (
    id varchar(26) not null,
    target varchar(16) not null,
    target_id varchar(26) not null,
    rating varchar(16) not null,
    comment text,
    by varchar(128) not null,
    at timestamp(6) with time zone not null,
    primary key (id)
);

-- AnalystNoteEntity
create table analyst_note (
    id varchar(26) not null,
    profile_id varchar(26) not null,
    run_id varchar(26) not null,
    title varchar(512) not null,
    body text not null,
    priority varchar(8) not null,
    claims jsonb default '[]'::jsonb not null,
    citations jsonb default '[]'::jsonb not null,
    private boolean not null,
    status varchar(16) not null,
    model varchar(128),
    created_at timestamp(6) with time zone not null,
    primary key (id)
);

-- AnalystProfileEntity
create table analyst_profile (
    id varchar(26) not null,
    name varchar(200) not null,
    enabled boolean not null,
    schedule jsonb not null,
    scope jsonb,
    lookback_seconds integer,
    instructions text,
    provider_id varchar(64),
    effort varchar(8) not null,
    max_runs_per_day integer not null,
    max_tokens_per_run integer not null,
    max_tool_calls integer not null,
    max_wall_seconds integer not null,
    allow_actions boolean not null,
    allowed_actions jsonb default '[]'::jsonb not null,
    max_auto_actions_per_day integer not null,
    channel_ids jsonb default '[]'::jsonb not null,
    notify_min_priority varchar(8) not null,
    owner_token_id varchar(64),
    cursor jsonb,
    next_run_at timestamp(6) with time zone,
    last_run_at timestamp(6) with time zone,
    running_since timestamp(6) with time zone,
    last_budget_event_on date,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- AnalystProposalEntity
create table analyst_proposal (
    id varchar(26) not null,
    run_id varchar(26) not null,
    profile_id varchar(26) not null,
    type varchar(32) not null,
    payload jsonb not null,
    rationale text,
    citations jsonb default '[]'::jsonb not null,
    status varchar(16) not null,
    auto_executed boolean not null,
    private boolean default false not null,
    executed_ref varchar(128),
    decided_by varchar(128),
    decided_at timestamp(6) with time zone,
    reason text,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    primary key (id)
);

-- AnalystRunEntity
create table analyst_run (
    id varchar(26) not null,
    profile_id varchar(26) not null,
    status varchar(16) not null,
    started_at timestamp(6) with time zone,
    finished_at timestamp(6) with time zone,
    cursor_from jsonb,
    cursor_to jsonb,
    input_counts jsonb,
    provider varchar(64),
    model varchar(128),
    effort varchar(8),
    tokens_in bigint not null,
    tokens_out bigint not null,
    cached_tokens bigint not null,
    tool_calls integer not null,
    duration_ms bigint not null,
    notes integer not null,
    proposals integer not null,
    auto_executed integer not null,
    citations_dropped integer not null,
    claims_downgraded integer not null,
    warnings jsonb default '[]'::jsonb not null,
    reason text,
    prompt_hash varchar(64),
    manifest_id varchar(26),
    detector_version varchar(32) not null,
    content jsonb,
    primary key (id)
);

-- ===== quality (prompt 115) =====

-- AlertSignalEntity
create table alert_signal (
    id bigint generated by default as identity,
    alert_id bigint not null,
    rule_id uuid,
    type varchar(16) not null,
    at timestamp(6) with time zone not null,
    primary key (id),
    constraint alert_signal_alert_type_key unique (alert_id, type)
);
create index alert_signal_rule_at_idx on alert_signal (rule_id, at);

-- RuleQualityDailyEntity
create table rule_quality_daily (
    id bigint generated by default as identity,
    rule_id uuid not null,
    day date not null,
    fired integer default 0 not null,
    useful integer default 0 not null,
    noise integer default 0 not null,
    late integer default 0 not null,
    false_positive integer default 0 not null,
    signals jsonb,
    median_ack_seconds double precision,
    flaps integer default 0 not null,
    suppressed_by_cooldown integer default 0 not null,
    suppressed_by_rate_cap integer default 0 not null,
    updated_at timestamp(6) with time zone,
    primary key (id),
    constraint rule_quality_daily_rule_day_key unique (rule_id, day)
);

-- RuleTuningLogEntity
create table rule_tuning_log (
    id varchar(26) not null,
    rule_id uuid not null,
    suggestion_id varchar(26),
    before_input jsonb,
    after_input jsonb,
    after_rev varchar(64) not null,
    note text,
    applied_by varchar(200) not null,
    applied_at timestamp(6) with time zone not null,
    undone_by varchar(200),
    undone_at timestamp(6) with time zone,
    primary key (id)
);
create index rule_tuning_log_rule_idx on rule_tuning_log (rule_id, applied_at);

-- TuningSuggestionEntity
create table tuning_suggestion (
    id varchar(26) not null,
    rule_id uuid not null,
    rule_rev varchar(64) not null,
    kind varchar(40) not null,
    suggestion_key varchar(200),
    patch jsonb,
    patch_hash varchar(64) not null,
    rationale text,
    expected_effect jsonb,
    caveats jsonb,
    status varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    decided_by varchar(200),
    decided_at timestamp(6) with time zone,
    decision_reason text,
    primary key (id),
    constraint tuning_suggestion_dedupe_key unique (rule_id, kind, patch_hash)
);
create index tuning_suggestion_rule_status_idx on tuning_suggestion (rule_id, status);

-- ===== precursor (prompt 120) =====

-- PrecursorBatchEntity
create table precursor_batch (
    id varchar(26) not null,
    job_id varchar(26),
    mode varchar(8) not null,
    trigger varchar(16) not null,
    status varchar(16) not null,
    started_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    candidates_generated integer not null,
    tested integer not null,
    capped integer not null,
    insufficient integer not null,
    survivors integer not null,
    validated integer not null,
    q double precision not null,
    method varchar(4) not null,
    windows jsonb,
    error text,
    manifest_id varchar(26),
    primary key (id)
);
create index precursor_batch_started_idx on precursor_batch (started_at);

-- PrecursorCandidateEntity
create table precursor_candidate (
    id varchar(26) not null,
    name varchar(200),
    kind varchar(8) not null,
    antecedent jsonb not null,
    consequent jsonb not null,
    spatial jsonb,
    lag_min_seconds bigint not null,
    lag_max_seconds bigint not null,
    region_codes text[],
    created_by varchar(200) not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);

-- PrecursorEntity
create table precursor (
    id varchar(26) not null,
    candidate_key varchar(64) not null,
    kind varchar(8) not null,
    antecedent jsonb not null,
    consequent jsonb not null,
    spatial jsonb,
    region_codes text[],
    families text[],
    lag_min_seconds bigint not null,
    lag_max_seconds bigint not null,
    best_lag_seconds bigint,
    lag_distribution jsonb,
    hit_rate double precision,
    hit_rate_lower double precision,
    hit_rate_upper double precision,
    base_rate double precision,
    lift double precision,
    correlation double precision,
    correlation_sign varchar(8),
    n_antecedent bigint not null,
    hits bigint not null,
    discovery jsonb,
    validation jsonb,
    last_retest jsonb,
    batch_id varchar(26),
    batch_size integer not null,
    status varchar(16) not null,
    status_reason text,
    stale_count integer not null,
    operator_validated boolean not null,
    detector jsonb,
    detector_version varchar(64),
    first_validated_at timestamp(6) with time zone,
    last_tested_at timestamp(6) with time zone,
    origin varchar(16) not null,
    candidate_id varchar(26),
    description text,
    caveats jsonb,
    history jsonb,
    manifest_id varchar(26),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
alter table precursor add constraint precursor_key_idx unique (candidate_key);
create index precursor_status_idx on precursor (status, id);

-- ===== report (prompt 132) =====

-- ReportEntity
create table report (
    id varchar(26) not null,
    display_id varchar(32) not null,
    title varchar(300) not null,
    subtitle varchar(300),
    status varchar(16) not null,
    template varchar(32) not null,
    authors jsonb not null,
    source jsonb not null,
    source_ref varchar(1024),
    time_window jsonb,
    area jsonb,
    sections jsonb not null,
    version integer not null,
    published_revision integer,
    published_at timestamp(6) with time zone,
    manifest_id varchar(26),
    created_by varchar(128),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
alter table report add constraint report_display_idx unique (display_id);
create index report_status_idx on report (status);

-- ReportRevisionEntity
create table report_revision (
    id varchar(26) not null,
    report_id varchar(26) not null,
    revision integer not null,
    title varchar(300) not null,
    tlp varchar(16) not null,
    status varchar(16) not null,
    published_at timestamp(6) with time zone not null,
    published_by varchar(128) not null,
    retracted_at timestamp(6) with time zone,
    retraction_reason varchar(2000),
    manifest_id varchar(26),
    pinned_refs jsonb not null,
    bundle jsonb not null,
    bundle_sha256 varchar(64) not null,
    markdown text not null,
    html text not null,
    machine_generated boolean not null,
    primary key (id)
);
alter table report_revision add constraint report_revision_report_idx unique (report_id, revision);

-- ReportSendEntity
create table report_send (
    id varchar(26) not null,
    report_id varchar(26) not null,
    revision integer,
    sent_by varchar(128) not null,
    sent_at timestamp(6) with time zone not null,
    message varchar(2000),
    recipients jsonb not null,
    principal_ids jsonb not null,
    primary key (id)
);
create index report_send_report_idx on report_send (report_id);

-- ===== media (prompt 043; outlet subpackage prompt 133) =====

-- MediaEntity
create table media (
    id varchar(26) not null,
    sha256 char(64) not null,
    content_type varchar(32) not null,
    width integer not null,
    height integer not null,
    bytes bigint not null,
    path varchar(128) not null,
    role varchar(16) not null,
    source_id varchar(64),
    record_source_id varchar(64),
    record_external_id varchar(256),
    licence varchar(512),
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone,
    variant_of varchar(26),
    variant_width integer,
    primary key (id)
);
create index media_sha256_idx on media (sha256);
create index media_record_idx on media (record_source_id, record_external_id);
create index media_variant_of_idx on media (variant_of);
create index media_expires_at_idx on media (expires_at);

-- MediaTombstoneEntity
create table media_tombstone (
    id varchar(26) not null,
    deleted_at timestamp(6) with time zone not null,
    primary key (id)
);

-- MediaOutletEntity
create table media_outlet (
    id varchar(26) not null,
    name varchar(200) not null,
    primary_domain varchar(253) not null,
    domains text[] not null,
    country varchar(2),
    languages text[],
    type varchar(32) not null,
    owner_org varchar(300),
    parent_org varchar(300),
    wikidata_qid varchar(20),
    originator_id varchar(120),
    aggregates text[],
    rated boolean not null,
    merged_into varchar(26),
    wikidata_checked_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint media_outlet_primary_domain_key unique (primary_domain)
);
create index media_outlet_country_idx on media_outlet (country);

-- MediaProviderEntity
create table media_provider (
    id varchar(32) not null,
    enabled boolean not null,
    licence_acknowledged boolean not null,
    acknowledged_by varchar(200),
    acknowledged_at timestamp(6) with time zone,
    permission_recorded boolean not null,
    api_key_ciphertext bytea,
    api_key_nonce bytea,
    last_import_at timestamp(6) with time zone,
    last_import_status varchar(16),
    last_import_message varchar(1000),
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- MediaRatingEntity
create table media_rating (
    id varchar(26) not null,
    outlet_id varchar(26) not null,
    provider varchar(32) not null,
    dimension varchar(80) not null,
    value_norm double precision,
    value_raw varchar(500),
    label varchar(300),
    rated_at timestamp(6) with time zone,
    licence varchar(300) not null,
    attribution varchar(500) not null,
    url varchar(2000),
    egress varchar(16) not null,
    imported_at timestamp(6) with time zone not null,
    primary key (id),
    constraint media_rating_outlet_provider_dim_key unique (outlet_id, provider, dimension)
);
create index media_rating_provider_idx on media_rating (provider);

-- OutletConsensusEntity
create table outlet_consensus (
    id varchar(120) not null,
    outlet_id varchar(26) not null,
    dimension varchar(80) not null,
    median double precision,
    n_providers integer not null,
    disagreement double precision,
    bucket varchar(16),
    providers jsonb,
    computed_at timestamp(6) with time zone not null,
    primary key (id)
);
create index outlet_consensus_outlet_idx on outlet_consensus (outlet_id);
create index outlet_consensus_dimension_bucket_idx on outlet_consensus (dimension, bucket);

-- ===== raster (prompt 042; export subpackage prompt 108) =====

-- RasterGridEntity
create table raster_grid (
    id varchar(64) not null,
    type varchar(16) not null,
    definition jsonb not null,
    nx integer not null,
    ny integer not null,
    bbox geometry(Polygon,4326) not null,
    primary key (id)
);

-- RasterProductEntity
create table raster_product (
    id varchar(64) not null,
    source_id varchar(64) not null,
    category varchar(16),
    kind varchar(16) not null,
    spec jsonb not null,
    dynamic boolean not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- RasterFrameEntity (@IdClass RasterFrameId: productId, frameId)
create table raster_frame (
    product_id varchar(64) not null,
    frame_id varchar(32) not null,
    valid_time timestamp(6) with time zone not null,
    reference_time timestamp(6) with time zone,
    forecast_step integer,
    obs_start timestamp(6) with time zone,
    obs_end timestamp(6) with time zone,
    grid_id varchar(64) not null,
    path varchar(512) not null,
    bytes bigint not null,
    sha256 varchar(64) not null,
    dtype varchar(8) not null,
    codec integer not null,
    stats jsonb,
    footprint geometry(MultiPolygon,4326),
    upstream_key varchar(512),
    status varchar(8) not null,
    thinned boolean not null,
    run_complete boolean default false not null,
    ingested_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone,
    primary key (frame_id, product_id)
);
create index raster_frame_product_run_idx on raster_frame (product_id, reference_time, forecast_step);

-- ExportJobEntity (ai.foresignal.raster.export)
create table raster_export_job (
    id varchar(26) not null,
    kind varchar(16) not null,
    format varchar(16) not null,
    product_id varchar(64),
    spec jsonb,
    status varchar(16) not null,
    progress integer not null,
    files jsonb,
    row_count bigint,
    bytes bigint,
    manifest_sha256 varchar(64),
    error varchar(500),
    created_by varchar(64),
    created_at timestamp(6) with time zone not null,
    started_at timestamp(6) with time zone,
    finished_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    input_hash varchar(64),
    primary key (id)
);

-- ===== object (prompts 129, 130) =====

-- AnnotationEntity
create table annotation (
    id varchar(26) not null,
    target_ref varchar(1024) not null,
    kind varchar(16) not null,
    verdict varchar(16),
    duplicate_of_ref varchar(1024),
    body text,
    tags jsonb not null,
    author varchar(200) not null,
    private boolean not null,
    status_applied boolean not null,
    target_component varchar(32),
    target_key varchar(300),
    precursor_key varchar(300),
    parent_id varchar(26),
    mentions jsonb,
    resolved boolean default false not null,
    edited_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    deleted_at timestamp(6) with time zone,
    primary key (id)
);
create index annotation_target_idx on annotation (target_ref, created_at);
create index annotation_verdict_idx on annotation (kind, target_component, created_at);
create index annotation_parent_idx on annotation (parent_id, created_at);

-- RelationEntity
create table relation (
    id varchar(26) not null,
    from_ref varchar(1024) not null,
    to_ref varchar(1024) not null,
    type varchar(32) not null,
    valid_from timestamp(6) with time zone not null,
    valid_to timestamp(6) with time zone,
    confidence double precision not null,
    label varchar(16) not null,
    provenance jsonb not null,
    projected boolean not null,
    owner_key varchar(1100),
    projection_key varchar(64),
    private boolean not null,
    created_at timestamp(6) with time zone not null,
    retired_at timestamp(6) with time zone,
    primary key (id)
);
create index relation_from_idx on relation (from_ref, type);
create index relation_to_idx on relation (to_ref, type);
create index relation_validity_idx on relation (valid_from, valid_to);
create index relation_owner_idx on relation (owner_key);
alter table relation add constraint relation_projection_key unique (projection_key);

-- BenchmarkCaseProposalEntity
create table benchmark_case_proposal (
    id varchar(26) not null,
    annotation_id varchar(26) not null,
    target_ref varchar(1024) not null,
    name varchar(200) not null,
    window_from timestamp(6) with time zone not null,
    window_to timestamp(6) with time zone not null,
    region_codes jsonb not null,
    expectations jsonb not null,
    status varchar(16) not null,
    private boolean not null,
    created_by varchar(200) not null,
    created_at timestamp(6) with time zone not null,
    decided_by varchar(200),
    decided_at timestamp(6) with time zone,
    case_id varchar(26),
    primary key (id)
);

-- WorkspaceEntity
create table workspace (
    id varchar(26) not null,
    name varchar(200) not null,
    owner varchar(200) not null,
    members jsonb not null,
    context jsonb not null,
    context_version bigint not null,
    layout jsonb not null,
    saved_view_ids jsonb not null,
    investigation_id varchar(64),
    private boolean not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index workspace_owner_idx on workspace (owner, id);

-- ===== exchange (prompt 132) =====

-- ExchangePackageEntity
create table exchange_package (
    id varchar(26) not null,
    type varchar(32) not null,
    object_ref varchar(1024) not null,
    tlp varchar(16) not null,
    manifest jsonb,
    manifest_json text,
    signature text,
    status varchar(16) not null,
    size_bytes bigint,
    file_path varchar(512),
    deliveries jsonb not null,
    reductions jsonb not null,
    deliver varchar(8) not null,
    created_by varchar(128) not null,
    created_at timestamp(6) with time zone not null,
    error varchar(2000),
    primary key (id)
);
create index exchange_package_created_by_idx on exchange_package (created_by);
create index exchange_package_object_idx on exchange_package (object_ref);

-- ExchangeSenderKeyEntity
create table exchange_sender_key (
    instance_id varchar(128) not null,
    jwks jsonb not null,
    pinned_by varchar(128),
    pinned_at timestamp(6) with time zone not null,
    primary key (instance_id)
);

-- ReceivedPackageEntity
create table received_package (
    id varchar(26) not null,
    package_id varchar(26) not null,
    type varchar(32) not null,
    object_ref varchar(1100) not null,
    title varchar(600) not null,
    sender jsonb not null,
    authors jsonb not null,
    tlp varchar(16) not null,
    signature_status varchar(16) not null,
    key_source varchar(16),
    hashes_verified boolean not null,
    received_via varchar(8) not null,
    received_at timestamp(6) with time zone not null,
    peer_id varchar(48),
    recipient_ids jsonb not null,
    everyone boolean not null,
    message varchar(2000),
    acknowledged_at timestamp(6) with time zone,
    acknowledged_by varchar(26),
    provenance_chain jsonb not null,
    contents jsonb not null,
    manifest jsonb not null,
    bundle jsonb,
    markdown text,
    html text,
    file_path varchar(512),
    primary key (id)
);
alter table received_package add constraint received_package_package_idx unique (package_id);
create index received_package_object_idx on received_package (object_ref);

-- ReceivedObjectEntity
create table received_object (
    id varchar(26) not null,
    received_id varchar(26) not null,
    remote_ref varchar(1100) not null,
    envelope jsonb not null,
    primary key (id)
);
create index received_object_ref_idx on received_object (remote_ref);
alter table received_object add constraint received_object_package_idx unique (received_id, remote_ref);

-- ===== entity (prompt 098) =====

-- EntityEntity
create table entity (
    id varchar(26) not null,
    type varchar(16) not null,
    display_name varchar(200),
    attributes jsonb,
    provisional boolean not null,
    status varchar(16) not null,
    merged_into varchar(26),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- EntityAliasEntity
create table entity_alias (
    id bigint generated by default as identity,
    entity_id varchar(26) not null,
    alias varchar(200) not null,
    alias_type varchar(16) not null,
    valid_from timestamp(6) with time zone not null,
    valid_to timestamp(6) with time zone,
    origin varchar(16) not null,
    primary key (id)
);
create index entity_alias_entity_idx on entity_alias (entity_id);

-- EntityKeyEntity
create table entity_key (
    id bigint generated by default as identity,
    entity_id varchar(26) not null,
    key_type varchar(32) not null,
    key_value varchar(64) not null,
    role varchar(8) not null,
    confidence real not null,
    origin varchar(16) not null,
    origin_ref varchar(320),
    valid_from timestamp(6) with time zone not null,
    valid_to timestamp(6) with time zone,
    retracted boolean not null,
    primary key (id)
);
create index entity_key_entity_idx on entity_key (entity_id);

-- EntityLinkEntity
create table entity_link (
    id bigint generated by default as identity,
    entity_id varchar(26) not null,
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    key_type varchar(32) not null,
    key_value varchar(64) not null,
    confidence real not null,
    valid_from timestamp(6) with time zone not null,
    valid_to timestamp(6) with time zone,
    first_seq bigint,
    primary key (id)
);
create index entity_link_entity_idx on entity_link (entity_id, valid_to);

-- ===== pol (prompt 114) =====

-- PolSubjectEntity
create table pol_subject (
    id varchar(26) not null,
    subject_type varchar(16) not null,
    ref varchar(330) not null,
    enabled boolean not null,
    params jsonb,
    private_subject boolean not null,
    precision_decimals integer not null,
    created_by varchar(200),
    created_at timestamp(6) with time zone not null,
    last_seq bigint,
    last_computed_at timestamp(6) with time zone,
    recompute_requested boolean not null,
    state jsonb,
    primary key (id)
);

-- PolPlaceEntity
create table pol_place (
    id varchar(26) not null,
    subject_id varchar(26) not null,
    cluster_id integer not null,
    centroid geometry(Point,4326) not null,
    radius_m double precision not null,
    visits integer not null,
    total_dwell_minutes double precision not null,
    hour_of_week integer[],
    first_seen timestamp(6) with time zone not null,
    last_seen timestamp(6) with time zone not null,
    label varchar(100),
    primary key (id)
);
create index pol_place_subject_idx on pol_place (subject_id);

-- PolStayEntity
create table pol_stay (
    id varchar(26) not null,
    subject_id varchar(26) not null,
    arrive timestamp(6) with time zone not null,
    depart timestamp(6) with time zone not null,
    centroid geometry(Point,4326) not null,
    place_id varchar(26),
    cluster_id integer,
    fixes integer not null,
    record_ref varchar(330),
    primary key (id)
);
create index pol_stay_subject_arrive_idx on pol_stay (subject_id, arrive);
create index pol_stay_record_idx on pol_stay (record_ref);

-- ===== infra (prompt 104) =====

-- InfraBuildEntity
create table infra_build (
    id bigint generated by default as identity,
    started timestamp(6) with time zone not null,
    finished timestamp(6) with time zone,
    rules_version integer not null,
    inputs jsonb,
    nodes bigint not null,
    edges_by_method jsonb,
    edges_by_label jsonb,
    warnings jsonb default '[]'::jsonb not null,
    error text,
    osm_extract_date timestamp(6) with time zone,
    primary key (id)
);

-- InfraNodeEntity
create table infra_node (
    id bigint generated by default as identity,
    build_id bigint not null,
    source_id varchar(64) not null,
    external_id varchar(256) not null,
    facility_type varchar(32) not null,
    name varchar(512),
    geom geometry(Geometry,4326),
    point geometry(Point,4326),
    attributes jsonb,
    region_tags jsonb default '[]'::jsonb not null,
    licence varchar(128),
    built_at timestamp(6) with time zone not null,
    primary key (id)
);

-- InfraEdgeEntity
create table infra_edge (
    id bigint generated by default as identity,
    build_id bigint not null,
    from_node bigint not null,
    to_node bigint not null,
    relation varchar(32) not null,
    method varchar(16) not null,
    label varchar(16) not null,
    score double precision not null,
    rule_id varchar(64) not null,
    rule_version integer not null,
    params jsonb,
    evidence jsonb,
    licence varchar(128),
    built_at timestamp(6) with time zone not null,
    primary key (id)
);

-- ===== export (prompt 111) =====

-- ArchiveWatermarkEntity
create table archive_watermark (
    id varchar(512) not null,
    through_seq bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- ExportJobEntity (ai.foresignal.export, @Entity(name = "BulkExportJob"))
create table export_job (
    id varchar(26) not null,
    kind varchar(16) not null,
    format varchar(16) not null,
    spec jsonb,
    plan jsonb,
    status varchar(16) not null,
    progress integer not null,
    principal varchar(64),
    row_count bigint,
    bytes bigint,
    dir varchar(1024),
    manifest_sha256 varchar(64),
    license_summary jsonb,
    watermark_seq bigint,
    personal_use boolean not null,
    created_at timestamp(6) with time zone not null,
    started_at timestamp(6) with time zone,
    finished_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    error varchar(1000),
    primary key (id)
);
create index export_job_principal_idx on export_job (principal);

-- HoldEntity
create table hold (
    id varchar(26) not null,
    name varchar(200) not null,
    reason varchar(2000) not null,
    sources jsonb,
    kinds jsonb,
    from_time timestamp(6) with time zone,
    to_time timestamp(6) with time zone,
    from_seq bigint,
    to_seq bigint,
    expires_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    created_by varchar(64),
    floors_at_create jsonb,
    primary key (id)
);

-- ===== evidence (prompt 116) =====

-- CanonicalEventEntity
create table canonical_event (
    id varchar(26) not null,
    family varchar(64) not null,
    event_time timestamp(6) with time zone not null,
    geom geometry(Geometry,4326),
    region_codes text[],
    band_value double precision,
    first_observed_at timestamp(6) with time zone not null,
    first_observation_ref varchar(600) not null,
    observation_count integer not null,
    evidence_group_count integer not null,
    quality_flags text[],
    source_ids text[],
    status varchar(16) not null,
    merged_into varchar(26),
    resolver_version varchar(32) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index canonical_event_family_time_idx on canonical_event (family, event_time);
create index canonical_event_status_idx on canonical_event (status, updated_at);

-- EvidenceGroupEntity
create table evidence_group (
    id varchar(26) not null,
    canonical_event_id varchar(26) not null,
    originator_root varchar(256) not null,
    member_count integer not null,
    first_seq bigint not null,
    primary key (id)
);
create index evidence_group_event_idx on evidence_group (canonical_event_id);

-- ObservationLinkEntity
create table observation_link (
    id varchar(26) not null,
    canonical_event_id varchar(26) not null,
    source_id varchar(64) not null,
    external_id varchar(512) not null,
    seq bigint not null,
    version integer,
    originator_id varchar(128) not null,
    independence_root varchar(128) not null,
    upstream_event_id varchar(600),
    evidence_group_id varchar(26),
    link_confidence double precision not null,
    link_reason jsonb not null,
    event_time timestamp(6) with time zone,
    observed_at timestamp(6) with time zone,
    lon double precision,
    lat double precision,
    attributes jsonb,
    linked_at timestamp(6) with time zone not null,
    primary key (id),
    constraint observation_link_record_event_key unique (source_id, external_id, canonical_event_id)
);
create index observation_link_event_idx on observation_link (canonical_event_id);
create index observation_link_upstream_idx on observation_link (upstream_event_id);
create index observation_link_source_idx on observation_link (source_id, canonical_event_id);

-- ===== ask =====

-- AskThreadEntity
create table ask_thread (
    id uuid not null,
    title varchar(80) not null,
    token_id uuid,
    principal_id varchar(64),
    message_count integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index ask_thread_token_idx on ask_thread (token_id, updated_at);
create index ask_thread_updated_idx on ask_thread (updated_at);

-- AskMessageEntity
create table ask_message (
    id bigint generated by default as identity,
    thread_id uuid not null,
    position integer not null,
    role varchar(16) not null,
    content jsonb not null,
    citations jsonb,
    warnings jsonb,
    usage jsonb,
    mode varchar(16),
    stop_reason varchar(16),
    refusal_category varchar(64),
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index ask_message_thread_idx on ask_message (thread_id, position);

-- AskActionEntity
create table ask_action (
    id uuid not null,
    thread_id uuid not null,
    message_id bigint,
    token_id uuid,
    type varchar(32) not null,
    description text not null,
    payload jsonb not null,
    status varchar(16) not null,
    result jsonb,
    created_at timestamp(6) with time zone not null,
    executed_at timestamp(6) with time zone,
    decided_by varchar(256),
    primary key (id)
);
create index ask_action_thread_idx on ask_action (thread_id);
create index ask_action_token_idx on ask_action (token_id);

-- ===== analytics (prompt 119) =====

-- AnalysisSeriesEntity (analytics.registry)
create table analysis_series (
    ref varchar(600) not null,
    enabled boolean not null,
    detectors text[],
    frequency varchar(8) not null,
    family varchar(64),
    region_code varchar(16),
    watch_pairs jsonb,
    origin varchar(16) not null,
    volume double precision not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    last_run_at timestamp(6) with time zone,
    last_error text,
    primary key (ref)
);
create index analysis_series_family_idx on analysis_series (family);
create index analysis_series_region_idx on analysis_series (region_code);

-- AnalyticFindingEntity (analytics.finding)
create table analytic_finding (
    id varchar(26) not null,
    finding_type varchar(32) not null,
    title varchar(512) not null,
    severity varchar(16) not null,
    status varchar(16) not null,
    geom geometry(Geometry,4326),
    region_codes text[],
    window_start timestamp(6) with time zone,
    window_end timestamp(6) with time zone,
    detected_at timestamp(6) with time zone not null,
    series_refs text[],
    members jsonb,
    asof_seq bigint not null,
    stats jsonb,
    base_rate jsonb,
    caveats jsonb,
    detector jsonb,
    situation_id varchar(26),
    precursor_id varchar(26),
    question_ids text[],
    explanation text,
    private boolean not null,
    signal_family varchar(64),
    local_only_sources text[],
    raw_stat_keys text[],
    dedup_key varchar(700),
    superseded_by varchar(26),
    version integer not null,
    manifest_id varchar(26),
    dismissal jsonb,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index analytic_finding_type_detected_idx on analytic_finding (finding_type, detected_at desc);
create index analytic_finding_dedup_idx on analytic_finding (dedup_key, status);
create index analytic_finding_updated_idx on analytic_finding (updated_at);

-- FindingLogEntity (analytics.finding)
create table analytic_finding_log (
    id bigint generated by default as identity,
    finding_id varchar(26) not null,
    at timestamp(6) with time zone not null,
    actor varchar(200),
    action varchar(32) not null,
    event varchar(32),
    reason text,
    primary key (id)
);
create index analytic_finding_log_finding_idx on analytic_finding_log (finding_id, at);

-- ===== watch =====

-- WatchListEntity
create table watch_list (
    id uuid not null,
    name varchar(200) not null,
    identity varchar(16) not null,
    normalize varchar(8) not null,
    description text,
    item_count integer default 0 not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- WatchListItemEntity (@EmbeddedId WatchListItemEntity.Key: list_id, value)
create table watch_list_item (
    list_id uuid not null,
    value varchar(256) not null,
    label varchar(200),
    notes text,
    tags text[],
    added_at timestamp(6) with time zone not null,
    origin varchar(8) not null,
    primary key (list_id, value)
);

-- ===== trip (prompt 114) =====

-- TripEntity
create table trip (
    id varchar(26) not null,
    name varchar(200) not null,
    route_place_id varchar(64),
    route geometry(Geometry,4326) not null,
    buffer_m integer not null,
    schedule jsonb,
    expected_duration_seconds bigint not null,
    lead_time_seconds bigint not null,
    hazard_kinds jsonb,
    min_severity varchar(16),
    channel_ids jsonb,
    anchor varchar(330),
    notify_when_clear boolean not null,
    enabled boolean not null,
    corridor geometry(MultiPolygon,4326) not null,
    active_rule_id uuid,
    active_occurrence timestamp(6) with time zone,
    active_until timestamp(6) with time zone,
    created_by varchar(200),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- TripRunEntity
create table trip_run (
    id varchar(26) not null,
    trip_id varchar(26) not null,
    occurrence timestamp(6) with time zone not null,
    status varchar(16) not null,
    clear boolean not null,
    hazards jsonb,
    briefing text,
    delivered_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
create index trip_run_trip_idx on trip_run (trip_id, created_at);
create index trip_run_occurrence_idx on trip_run (trip_id, occurrence);

-- ===== sink (prompt 110) =====

-- SinkEntity
create table sink (
    id varchar(64) not null,
    type varchar(32) not null,
    name varchar(200) not null,
    enabled boolean not null,
    config jsonb default '{}'::jsonb,
    secret_ciphertext bytea,
    secret_nonce bytea,
    filter jsonb default '{}'::jsonb,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- SinkCursorEntity
create table sink_cursor (
    sink_id varchar(64) not null,
    last_seq bigint not null,
    watermark_seq bigint not null,
    last_delivered_at timestamp(6) with time zone,
    state varchar(16) not null,
    last_error varchar(500),
    failing_since timestamp(6) with time zone,
    resync_count bigint not null,
    delivered bigint not null,
    dropped bigint not null,
    denied jsonb default '{}'::jsonb,
    primary key (sink_id)
);

-- ===== place =====

-- PlaceEntity
create table place (
    id varchar(64) not null,
    name varchar(200) not null,
    shape varchar(8) not null,
    geom geometry(Geometry,4326) not null,
    radius_km double precision,
    buffer_m double precision,
    area geometry(MultiPolygon,4326) not null,
    timezone varchar(64),
    kind varchar(16) not null,
    tags text[],
    products jsonb,
    attributes jsonb,
    sensitive boolean default true not null,
    version integer default 1 not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- ForecastSeriesCacheEntity (@IdClass ForecastSeriesCacheEntity.Key: place_id, provider)
create table forecast_series_cache (
    place_id varchar(64) not null,
    provider varchar(20) not null,
    updated_at timestamp(6) with time zone not null,
    valid_from timestamp(6) with time zone,
    series jsonb not null,
    primary key (provider, place_id)
);

-- ===== llm =====

-- LlmProviderEntity
create table llm_provider (
    id varchar(64) not null,
    type varchar(32) not null,
    base_url varchar(2048) not null,
    chat_model varchar(200),
    embedding_model varchar(200),
    embedding_dimensions integer,
    options jsonb default '{}'::jsonb not null,
    secret_ciphertext bytea,
    secret_nonce bytea,
    requests_per_minute integer not null,
    tokens_per_day bigint not null,
    enabled boolean not null,
    knowledge_cutoff date,
    supports_tools boolean,
    last_success_at timestamp(6) with time zone,
    last_error_at timestamp(6) with time zone,
    last_error text,
    consecutive_failures integer default 0 not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);

-- LlmUsageEntity (@EmbeddedId LlmUsageId: provider_id, minute)
create table llm_usage (
    minute timestamp(6) with time zone not null,
    provider_id varchar(64) not null,
    requests bigint not null,
    input_tokens bigint not null,
    output_tokens bigint not null,
    cached_tokens bigint not null,
    errors bigint not null,
    refused bigint not null,
    primary key (minute, provider_id)
);

-- ===== federation (prompt 113) =====

-- FederationOriginEntity
create table federation_origin (
    origin_ref varchar(1024) not null,
    owner_source_id varchar(64) not null,
    local_external_id varchar(256) not null,
    origin_seq bigint not null,
    updated_at timestamp(6) with time zone not null,
    primary key (origin_ref)
);

-- PeerEntity
create table peer (
    id varchar(48) not null,
    name varchar(200) not null,
    base_url varchar(2048) not null,
    token_ciphertext bytea,
    token_nonce bytea,
    pinned_kid varchar(128),
    pinned_key bytea,
    remote_instance_id varchar(128),
    selection jsonb default '{}'::jsonb,
    mode varchar(16) not null,
    enabled boolean not null,
    same_operator boolean default false not null,
    reliability varchar(1),
    state varchar(32) not null,
    health_reason varchar(32),
    last_seq bigint,
    remote_watermark bigint,
    last_error varchar(1000),
    ingest_key_hash varchar(64),
    ingest_key_prefix varchar(16),
    exchange_key_ciphertext bytea,
    exchange_key_nonce bytea,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index peer_ingest_key_hash_idx on peer (ingest_key_hash);

-- ===== briefing =====

-- BriefingEntity
create table briefing (
    id uuid not null,
    name varchar(200) not null,
    enabled boolean not null,
    schedule varchar(200) not null,
    timezone varchar(64) not null,
    scope jsonb not null,
    lookback varchar(32),
    mode varchar(16) not null,
    instructions text,
    channel_ids jsonb not null,
    max_items_per_kind integer not null,
    audio jsonb,
    next_run_at timestamp(6) with time zone,
    last_run_at timestamp(6) with time zone,
    last_run_status varchar(16),
    running_since timestamp(6) with time zone,
    owner_token_id uuid,
    owner_scope varchar(16),
    restriction_snapshot jsonb,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
create index briefing_next_run_idx on briefing (enabled, next_run_at);

-- BriefingRunEntity
create table briefing_run (
    id uuid not null,
    briefing_id uuid not null,
    briefing_name varchar(200) not null,
    status varchar(16) not null,
    mode varchar(16) not null,
    timezone varchar(64) not null,
    started_at timestamp(6) with time zone not null,
    finished_at timestamp(6) with time zone,
    period_from timestamp(6) with time zone not null,
    period_to timestamp(6) with time zone not null,
    content_markdown text not null,
    signature varchar(4096),
    citations jsonb,
    stats jsonb,
    error text,
    usage jsonb,
    deliveries integer not null,
    warnings jsonb,
    audio jsonb,
    primary key (id)
);
create index briefing_run_briefing_idx on briefing_run (briefing_id, started_at);
create index briefing_run_started_idx on briefing_run (started_at);

-- ===== view =====

-- SavedViewEntity
create table saved_view (
    id uuid not null,
    slug varchar(64) not null,
    name varchar(200) not null,
    sources text[],
    kinds text[],
    place_ids text[],
    bbox jsonb,
    area geometry(MultiPolygon,4326),
    conditions jsonb not null,
    time_field varchar(16) not null,
    include_expired boolean default false not null,
    fields jsonb,
    version integer default 1 not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint saved_view_slug_key unique (slug)
);

-- ===== stats (prompt 119 adds region rows) =====

-- ActivityRollupEntity (@EmbeddedId ActivityRollupId: resolution, bucket_start, source_id, kind, cell)
create table activity_rollup (
    bucket_start timestamp(6) with time zone not null,
    cell bigint not null,
    kind varchar(64) not null,
    resolution smallint not null,
    source_id varchar(64) not null,
    cell_lon double precision,
    region_code varchar(16),
    cell_lat double precision,
    versions bigint not null,
    entities bigint not null,
    firsts bigint not null,
    tombstones bigint not null,
    edges bigint not null,
    changes bigint not null,
    primary key (resolution, bucket_start, cell, kind, source_id)
);
create index activity_rollup_resolution_bucket_idx on activity_rollup (resolution, bucket_start);
create index activity_rollup_source_resolution_bucket_idx on activity_rollup (source_id, resolution, bucket_start);
create index activity_rollup_region_idx on activity_rollup (resolution, region_code, kind, bucket_start);

-- ===== exercise (prompt 115) =====

-- ExerciseRunEntity
create table exercise_run (
    id varchar(26) not null,
    name varchar(200) not null,
    rule_ids jsonb,
    tag varchar(64),
    range_from timestamp(6) with time zone,
    range_to timestamp(6) with time zone,
    from_seq bigint,
    to_seq bigint,
    speed varchar(16) not null,
    channel_ids jsonb,
    enrich boolean default false not null,
    status varchar(16) not null,
    created_by varchar(200),
    created_at timestamp(6) with time zone not null,
    started_at timestamp(6) with time zone,
    finished_at timestamp(6) with time zone,
    stats jsonb,
    primary key (id)
);
create index exercise_run_status_idx on exercise_run (status, created_at);

-- ===== doctor (prompt 140) =====

-- DoctorReportEntity
create table doctor_report (
    id varchar(26) not null,
    source_id varchar(64) not null,
    created_at timestamp(6) with time zone not null,
    trigger varchar(8) not null,
    status varchar(16) not null,
    findings jsonb not null,
    diff jsonb,
    llm jsonb,
    probe jsonb,
    fixture jsonb,
    adapter_version varchar(64),
    primary key (id)
);
create index doctor_report_source_idx on doctor_report (source_id, created_at);
create index doctor_report_created_idx on doctor_report (created_at);

-- =====================================================================================================================
-- Indexes and storage parameters JPA cannot express (formerly SpatialIndexInitializer, InfraSchemaInitializer and
-- EmbeddingSchemaManager's base statements).
-- =====================================================================================================================

-- record_live (Architecture §8): the only record GiST index, plus storage parameters that keep touches HOT.
-- record_head deliberately has no spatial index (its persisted_geom is never searched).
CREATE INDEX record_live_geom_gist ON record_live USING GIST (geom);
ALTER TABLE record_live SET (fillfactor = 70, autovacuum_vacuum_scale_factor = 0.02, autovacuum_vacuum_cost_limit = 2000);

-- Series points (prompt 093): /v1/series/{source}/{id} reads one series in period order.
CREATE INDEX record_head_series_point_idx ON record_head (source_id, (payload->>'series'), event_time)
    WHERE kind = 'series-point';

-- Trigger rules (prompt 017): geofence containment.
CREATE INDEX trigger_rule_geofence_gist ON trigger_rule USING GIST (geofence);

-- Open alerts (prompt 017): auto-resolve, alert snoozes and openAlertCount look only at these.
CREATE INDEX alert_open_idx ON alert (rule_id, source_id, external_id) WHERE state <> 'resolved';

-- Places, saved views (prompt 036): area is what filters, rules and the weather AOI intersect.
CREATE INDEX place_area_gist ON place USING GIST (area);
CREATE INDEX saved_view_place_ids_idx ON saved_view USING GIN (place_ids);

-- Reference geography (prompt 040): containment and KNN for region tagging, the filters' lookups.
CREATE INDEX ref_country_geom_gist ON ref_country USING GIST (geom);
CREATE INDEX ref_country_geom_simple_gist ON ref_country USING GIST (geom_simple);
CREATE INDEX ref_admin1_geom_gist ON ref_admin1 USING GIST (geom);
CREATE INDEX ref_admin1_geom_simple_gist ON ref_admin1 USING GIST (geom_simple);
CREATE INDEX ref_admin1_gn_code_idx ON ref_admin1 (gn_code);
CREATE INDEX ref_admin2_geom_gist ON ref_admin2 USING GIST (geom);
CREATE INDEX ref_sea_geom_gist ON ref_sea USING GIST (geom);
CREATE INDEX gazetteer_place_geog_gist ON gazetteer_place USING GIST (geog);
CREATE INDEX record_region_iso2_idx ON record_region (iso2);
CREATE INDEX record_region_iso2_admin1_idx ON record_region (iso2, admin1);

-- Raster frames (prompt 042, Weather and imagery data model): aliases read the newest frame first; footprints are
-- searched spatially.
CREATE INDEX raster_frame_product_valid_idx ON raster_frame (product_id, valid_time DESC);
CREATE INDEX raster_frame_footprint_gist ON raster_frame USING GIST (footprint);

-- Entity registry (prompt 098): at most one open holder per key, at most one open link per record, prefix search over
-- aliases, and the open links of an entity.
CREATE UNIQUE INDEX entity_key_open_idx ON entity_key (key_type, key_value) WHERE valid_to IS NULL;
CREATE UNIQUE INDEX entity_link_open_idx ON entity_link (source_id, external_id) WHERE valid_to IS NULL;
CREATE INDEX entity_alias_lower_idx ON entity_alias (lower(alias) text_pattern_ops);
CREATE INDEX entity_link_entity_open_idx ON entity_link (entity_id) WHERE valid_to IS NULL;

-- Infrastructure graph (prompt 104).
CREATE INDEX infra_node_geom_gist ON infra_node USING GIST (geom);
CREATE INDEX infra_node_point_gist ON infra_node USING GIST (point);
CREATE INDEX infra_node_type_idx ON infra_node (facility_type);
CREATE INDEX infra_node_build_type_idx ON infra_node (build_id, facility_type, id);
CREATE UNIQUE INDEX infra_node_build_record_idx ON infra_node (build_id, source_id, external_id);
CREATE UNIQUE INDEX infra_edge_unique_idx ON infra_edge (from_node, to_node, relation, rule_id);
CREATE INDEX infra_edge_build_from_idx ON infra_edge (build_id, from_node);
CREATE INDEX infra_edge_build_to_idx ON infra_edge (build_id, to_node);

-- Trips (prompt 114): the materialized corridor is what hazards are intersected with.
CREATE INDEX trip_corridor_gist ON trip USING GIST (corridor);

-- Situations (prompt 118): the list's bbox filter and the region filter.
CREATE INDEX situation_geom_gist ON situation USING GIST (geom);
CREATE INDEX situation_region_codes_gin ON situation USING GIN (region_codes);

-- Analytic findings (prompt 119): placement for the linker and maps, and the region filter.
CREATE INDEX analytic_finding_geom_gist ON analytic_finding USING GIST (geom);
CREATE INDEX analytic_finding_region_codes_gin ON analytic_finding USING GIN (region_codes);

-- Embedding documents (prompt 027): full-text search works without pgvector; vector slots are added by the reindex.
ALTER TABLE embedding_document ADD COLUMN tsv tsvector
    GENERATED ALWAYS AS (to_tsvector('simple', coalesce(text, ''))) STORED;
CREATE INDEX embedding_document_tsv_gin ON embedding_document USING GIN (tsv);
CREATE INDEX embedding_document_geom_gist ON embedding_document USING GIST (geom);
CREATE INDEX embedding_document_source_time_idx ON embedding_document (source_id, effective_time);
