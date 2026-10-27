-- Per-user quotas (prompt R2-026). Additive only.
--
-- user_quota: at most one row per user, deleted with the user. Every limit is nullable; a null limit falls through to
-- the instance default (app_setting key quota.default), and a limit unset there too is unlimited.
-- api_rate_limit_per_minute bounds the requests of the user's sessions and own API tokens together; max_storage_bytes
-- bounds the user's attachments and workspaces; max_alerts, max_channels and max_saved_views bound the trigger rules,
-- notification channels and saved views the user owns.
create table user_quota (
    user_id uuid not null,
    api_rate_limit_per_minute integer,
    max_storage_bytes bigint,
    max_alerts integer,
    max_channels integer,
    max_saved_views integer,
    updated_at timestamp(6) with time zone not null,
    primary key (user_id),
    constraint fk_user_quota_user foreign key (user_id) references "user" (id) on delete cascade,
    constraint user_quota_non_negative check (
        (api_rate_limit_per_minute is null or api_rate_limit_per_minute >= 0)
        and (max_storage_bytes is null or max_storage_bytes >= 0)
        and (max_alerts is null or max_alerts >= 0)
        and (max_channels is null or max_channels >= 0)
        and (max_saved_views is null or max_saved_views >= 0))
);
