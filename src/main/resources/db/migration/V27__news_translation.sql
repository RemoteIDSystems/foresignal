-- News translation at ingest (prompt NP-002). Additive only.
--
-- news_translation: one row per (news object, target language), beside the untouched original. object_type is
-- 'article' (object_id the news_article id, {sourceId}/{externalId}) or 'story' (object_id the news_story id).
-- source_hash is the SHA-256 of the source text the row was made from, so unchanged content is never translated twice
-- and changed content is. status is 'ok' (translated_title / translated_excerpt hold the translation), 'skipped'
-- (budget exhausted, no chat provider, or the source language below the confidence floor) or 'failed' (provider
-- error or a rejected output); only 'ok' rows are served. cost_usd is what the call cost, summed per window for the
-- translation budget cap. Rows of an article go with the article's retention (NewsTranslationStore deletes them with
-- the article); story rows stay with the story.
create table news_translation (
    object_type varchar(16) not null,
    object_id varchar(400) not null,
    target_language varchar(35) not null,
    source_language varchar(35),
    source_hash varchar(64) not null,
    status varchar(16) not null,
    translated_title varchar(1000),
    translated_excerpt varchar(1000),
    engine varchar(32) not null,
    model varchar(200),
    reason varchar(200),
    cost_usd numeric(14, 6) not null default 0,
    translated_at timestamp(6) with time zone not null,
    checked_at timestamp(6) with time zone not null,
    primary key (object_type, object_id, target_language),
    constraint news_translation_type check (object_type in ('article', 'story')),
    constraint news_translation_status check (status in ('ok', 'skipped', 'failed'))
);

create index news_translation_at_idx on news_translation (translated_at);
