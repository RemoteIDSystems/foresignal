-- Breaking-news special editions (prompt NP-006). Additive only.
--
-- briefing.breaking_news: the edition's BreakingNewsConfig {enabled, ruleIds, minSeverity, cooldownMinutes}
-- (BriefingInput.breakingNews, NP-001); null for none. briefing.last_breaking_at: when the last breaking run was
-- claimed, so the cooldown holds across instances (one conditional update claims it).
-- briefing_run.breaking: the alert that fired a breaking run {alertId, ruleId, ruleName, severity, firedAt}; null for
-- scheduled and manual runs, so a scheduled edition never counts a breaking run as its previous period.
alter table briefing add column breaking_news jsonb;
alter table briefing add column last_breaking_at timestamp(6) with time zone;
alter table briefing_run add column breaking jsonb;
