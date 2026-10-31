-- Source groups (prompt SG-002). Additive only.
--
-- source_group: a named set of member sources (custom or catalog) a user owns, with its own correlation and
-- notification threshold, usable as an edition scope (BriefingScope.sourceGroupIds) and a trigger scope
-- (trigger_rule.source_group_id). Owned by a v2.0 user (owner_user_id uuid), deleted with that user. The id is
-- server-generated text. source_ids and kinds are jsonb text arrays; correlation and notification_threshold are jsonb
-- objects. edition_id is the edition generated for the group, or null.
create table source_group (
    id varchar(64) not null,
    owner_user_id uuid not null,
    name varchar(200) not null,
    description text,
    source_ids jsonb not null,
    kinds jsonb not null,
    correlation jsonb,
    notification_threshold jsonb,
    edition_id uuid,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_source_group_owner_user foreign key (owner_user_id) references "user" (id) on delete cascade,
    constraint fk_source_group_edition foreign key (edition_id) references briefing (id) on delete set null
);
create index source_group_owner_user_idx on source_group (owner_user_id, id);

-- trigger_rule.source_group_id: the source group whose member sources scope the rule (prompt SG-002), ANDed with the
-- rule's sources/kinds; null for no group scope. Not a foreign key: a group is resolved and fails closed if deleted,
-- the same way a geofence preset's reference data is resolved at evaluation time.
alter table trigger_rule add column source_group_id varchar(64);
