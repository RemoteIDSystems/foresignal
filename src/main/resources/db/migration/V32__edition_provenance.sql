-- Edition provenance and corroboration (prompt NP-007). Additive only.
--
-- briefing.include_provenance / include_corroboration: BriefingInput.includeProvenance and includeCorroboration
-- (NP-001). Both default false, so no existing briefing's citations change; each run's citations carry provenance
-- and corroboration only while the flag is on.
alter table briefing add column include_provenance boolean not null default false;
alter table briefing add column include_corroboration boolean not null default false;
