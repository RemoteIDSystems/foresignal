-- Shared collection and per-user source views (prompt R2-010, docs/RBAC-Design.md §D). The instance collects each
-- source once; a user's view says which of the collected sources they see, with an optional label and color. A user
-- with no row for a source sees it (the view is opt-out), so an instance whose users never touch their views keeps
-- collecting exactly what it collected before this migration. The collector pulls a source when it is enabled in
-- source_settings (the instance set, collection.manage) and at least one active user's view does not disable it, or
-- when there is no active user at all. Additive only.

create table user_source_pref (
    user_id uuid not null,
    source_id varchar(64) not null,
    enabled boolean not null,
    display_name varchar(200),
    color varchar(7),
    updated_at timestamp(6) with time zone not null,
    primary key (user_id, source_id),
    constraint fk_user_source_pref_user foreign key (user_id) references "user" (id) on delete cascade
);

-- The primary key serves "one user's view"; this one serves the collector's "who disabled this source".
create index user_source_pref_disabled_idx on user_source_pref (source_id) where not enabled;
