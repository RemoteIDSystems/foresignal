-- Self-service custom sources (prompt SG-001). Additive only.
--
-- custom_source.owner_user_id: the v2.0 user who owns the custom source, set when a signed-in user creates one through
-- /v1/me/custom-sources; null for sources created through the operator /v1/custom-sources (and for external providers),
-- which keep working exactly as before and are not returned by the self-service surface. An owned source is deleted
-- with its user. Mirrors briefing.owner_user_id (V28, editions): nullable, FK to "user"(id) on delete cascade.
alter table custom_source add column owner_user_id uuid;
alter table custom_source add constraint fk_custom_source_owner_user foreign key (owner_user_id) references "user" (id)
    on delete cascade;
create index custom_source_owner_user_idx on custom_source (owner_user_id, source_id);
