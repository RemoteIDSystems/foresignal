-- Per-user preferences, saved views and workspace ownership (prompt R2-012, docs/RBAC-Design.md §D). Additive only.
--
-- user_preferences holds the parts of a user's UserPreferences that had no home before: locale, theme and the
-- free-form UI bag. The timezone is not repeated here: it stays on the user's principal (principal.timezone), the
-- column personal notification channels already render in, so there is one timezone per user. The notification
-- switches are not repeated either: they read and write the user's existing notification preferences
-- (principal_notification and its channels). A user with no row reads the documented defaults.
create table user_preferences (
    user_id uuid primary key,
    locale varchar(35),
    theme varchar(16),
    ui jsonb not null default '{}'::jsonb,
    updated_at timestamp(6) with time zone not null,
    constraint fk_user_preferences_user foreign key (user_id) references "user" (id) on delete cascade
);

-- A saved view gains an owner: the principal of the user who created it, the convention of trigger_rule (V16),
-- notification_channel and llm_provider (V18). A null owner is a shared view, managed by an admin: every view that
-- exists before this migration stays shared, so briefings, chronicles, share links and workspaces that name one keep
-- resolving it. workspace.owner already holds the creating caller's principal, which for a signed-in user is the
-- user's principal, so the workspace table needs no change.
alter table saved_view add column owner_principal_id varchar(64);
create index saved_view_owner_idx on saved_view (owner_principal_id) where owner_principal_id is not null;
