-- SocialCue.externalResults: what each EDP provider's transport.search returned for a cue (edpProviderSearch, 137).
-- One object per provider: {sourceId, queries, items, error}.
alter table social_cue add column external_results jsonb not null default '[]'::jsonb;
