-- Per-user editions (prompt NP-003). Additive only.
--
-- briefing.owner_user_id: the v2.0 user who owns the briefing as an edition, set when a signed-in user (or the user's
-- own API token) creates it; null for briefings created with an instance token, which keep working exactly as before.
-- An owned edition is deleted with its user. briefing.output_language: the BCP 47 language the edition is written and
-- spoken in (BriefingInput.outputLanguage); null for the owner's UserPreferences.locale, or none.
alter table briefing add column owner_user_id uuid;
alter table briefing add column output_language varchar(35);
alter table briefing add constraint fk_briefing_owner_user foreign key (owner_user_id) references "user" (id)
    on delete cascade;
create index briefing_owner_user_idx on briefing (owner_user_id, created_at);

-- user_news_subscription: the outlets (133) and languages a user follows (getMyNewsSubscriptions /
-- updateMyNewsSubscriptions). With no entry of a type there is no restriction of that type; with at least one, the
-- user's editions keep only news matching one of them. Deleted with the user.
create table user_news_subscription (
    user_id uuid not null,
    type varchar(16) not null,
    value varchar(200) not null,
    updated_at timestamp(6) with time zone not null,
    primary key (user_id, type, value),
    constraint fk_user_news_subscription_user foreign key (user_id) references "user" (id) on delete cascade,
    constraint user_news_subscription_type check (type in ('outlet', 'language'))
);
