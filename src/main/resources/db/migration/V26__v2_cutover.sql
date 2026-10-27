-- The v2.0 cutover (prompt R2-027). Additive only.
--
-- What owner-less configuration means after R2 needs no rewrite: a null owner already reads as shared/global on every
-- table that has one (trigger_rule and alert V16, notification_channel and llm_provider V18, saved_view V19), so
-- source settings, collector and server settings, global channels, the shared LLM providers, and the rules and views
-- that existed before R2 all stay shared. What does move is what the single operator of a v1 instance owned through the
-- instance "owner" principal, the principal of the bootstrap admin token: that operator's personal notification
-- channels and workspaces, and any rule, view or provider bound to it. They become the first admin's. The first admin
-- does not exist when this migration runs, so ai.foresignal.setup.CutoverMigrator moves those rows when setup creates
-- the admin (or at startup on an instance whose admin already exists) and records it here.
--
-- v2_cutover holds at most one row: when the move ran, to which user, from which principal, and how many rows of each
-- kind moved. Its presence makes the move a no-op the second time.
create table v2_cutover (
    id smallint not null,
    admin_user_id uuid,
    from_principal_id varchar(64),
    migrated_at timestamp(6) with time zone not null,
    counts jsonb not null,
    primary key (id),
    constraint v2_cutover_single_row check (id = 1)
);
