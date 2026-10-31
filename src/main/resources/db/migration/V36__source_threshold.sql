-- Per-source notification thresholds (prompt SG-003). Additive only.
--
-- source_threshold: a user's per-source minimum notify severity, mute and cooldown. Keyed on owner_principal_id (a
-- string principal) + source_id, matching the alert enforcement hot path (alert.ownerPrincipalId / alert.sourceId),
-- so no per-alert id mapping is needed. A source with no row notifies at the default (warning, un-muted, no cooldown)
-- unless a source group covering it sets a group threshold. Rows are a user's own; they are removed when the user is
-- deleted (SourceThresholdReassigner, keyed on owner_principal_id).
create table source_threshold (
    owner_principal_id varchar(64) not null,
    source_id varchar(64) not null,
    min_severity varchar(16) not null,
    mute boolean not null,
    cooldown_seconds bigint,
    primary key (owner_principal_id, source_id)
);

-- source_notify_state: the last time a notification for (owner_principal_id, source_id) actually went out, for
-- cooldown enforcement. Kept separate from source_threshold so getMySourceThresholds returns only the entries the user
-- set, never a row that exists solely to track cooldown (the threshold may come from a source group, not a per-source
-- entry). Removed with the user alongside the thresholds.
create table source_notify_state (
    owner_principal_id varchar(64) not null,
    source_id varchar(64) not null,
    last_notified_at timestamp(6) with time zone not null,
    primary key (owner_principal_id, source_id)
);
