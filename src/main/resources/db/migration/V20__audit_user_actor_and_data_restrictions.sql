-- Per-user data access and the user audit actor (prompt R2-013, docs/RBAC-Design.md §C2 and §H). Additive only.
--
-- An audit entry can now name the signed-in user who acted: actor_kind 'user' with user_id and user_email. The email
-- is copied when the entry is written, so the row still says who acted after the user is renamed or deleted (there
-- is deliberately no foreign key). Token, ingest-key, pairing, system and the other actor kinds are unchanged.
alter table audit_event add column user_id uuid;
alter table audit_event add column user_email varchar(320);
create index audit_event_user_idx on audit_event (user_id, id desc) where user_id is not null;
create index audit_event_actor_kind_idx on audit_event (actor_kind, id desc);

-- The audit log is append-only: an entry is never changed after it is written. Retention still deletes entries older
-- than the audit window (AuditRetentionStep); nothing may rewrite one.
create function audit_event_append_only() returns trigger language plpgsql as $$
begin
    raise exception 'audit_event is append-only: entries cannot be updated';
end;
$$;
create trigger audit_event_no_update before update on audit_event
    for each row execute function audit_event_append_only();

-- A user's data restriction: the same members as an API token's ReadRestriction data part (sources, kinds, bbox,
-- area, places), stored per user and managed by an admin through updateUser. Null members mean "any"; a user with no
-- row is unrestricted. A session's reads are intersected with it, on top of the user's source view (R2-010).
create table user_data_restriction (
    user_id uuid primary key,
    allowed_sources text[],
    allowed_kinds text[],
    allowed_bbox geometry(Polygon,4326),
    allowed_area geometry(Geometry,4326),
    allowed_place_ids text[],
    updated_at timestamp(6) with time zone not null,
    constraint fk_user_data_restriction_user foreign key (user_id) references "user" (id) on delete cascade
);
