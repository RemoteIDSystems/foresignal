-- Shared and personal LLM providers with per-user metering and budgets (prompt R2-011, docs/RBAC-Design.md §E).
--
-- An LLM provider gains an owner: the principal of the user whose personal provider it is (users and principals are
-- one-to-one, R2-001; trigger rules, alerts and channels use the same convention, R2-008/R2-009). Null means shared,
-- managed under llm.shared.manage; every provider that exists before this migration stays shared.
--
-- A shared provider may carry per-user USD caps (hourly, daily, monthly) that apply to each user's own spend on it, so
-- one user cannot exhaust the shared budget; its existing caps keep bounding the instance total. A personal provider's
-- existing caps are its owner's own, because only its owner's calls reach it. The per-user caps live on the provider
-- (one cap for every user) rather than in a per-user table, because no operation sets a cap for one named user.
--
-- llm_user_usage is the per-user rollup of llm_usage: the same per-minute counters for calls made on behalf of a user,
-- keyed by provider, principal and minute. llm_usage keeps the per-provider totals of every call, a user's or a
-- machine's, so its readers are unchanged. Additive only.

alter table llm_provider add column owner_principal_id varchar(64);
alter table llm_provider add column per_user_hourly_cap_usd numeric(14, 4);
alter table llm_provider add column per_user_daily_cap_usd numeric(14, 4);
alter table llm_provider add column per_user_monthly_cap_usd numeric(14, 4);
create index llm_provider_owner_idx on llm_provider (owner_principal_id) where owner_principal_id is not null;

create table llm_user_usage (
    minute timestamp(6) with time zone not null,
    provider_id varchar(64) not null,
    principal_id varchar(64) not null,
    requests bigint not null,
    input_tokens bigint not null,
    output_tokens bigint not null,
    cached_tokens bigint not null,
    errors bigint not null,
    refused bigint not null,
    cost_usd numeric(24, 12) not null default 0,
    primary key (provider_id, principal_id, minute)
);
create index llm_user_usage_principal_idx on llm_user_usage (principal_id, minute);
