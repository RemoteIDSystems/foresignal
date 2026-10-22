-- Prompt 153: de-hype & partisan vetting. The neutral restatement, the deterministic framing/partisan
-- score of the source post, and the rated-outlet reputation (factuality + lean + state-affiliation)
-- folded into the claim's reliability. Null for claims extracted before 153.
alter table social_claim add column restatement varchar(240);
alter table social_claim add column framing_score double precision;
alter table social_claim add column framing_label varchar(16);
alter table social_claim add column reputation jsonb;
