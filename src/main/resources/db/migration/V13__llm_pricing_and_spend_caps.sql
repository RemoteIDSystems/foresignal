-- LlmProviderInput.pricing and the hourly, daily and monthly USD caps (API 1.5.0). Prices are USD per million tokens;
-- null means not priced, and a null cap means no cap. llm_usage.cost_usd is each minute's spend, priced at the
-- provider's prices when the call was made, so a later price change does not rewrite what was spent.
alter table llm_provider add column price_input_usd_per_mtok numeric(14, 6);
alter table llm_provider add column price_output_usd_per_mtok numeric(14, 6);
alter table llm_provider add column price_cached_input_usd_per_mtok numeric(14, 6);
alter table llm_provider add column price_embedding_usd_per_mtok numeric(14, 6);
alter table llm_provider add column hourly_cap_usd numeric(14, 4);
alter table llm_provider add column daily_cap_usd numeric(14, 4);
alter table llm_provider add column monthly_cap_usd numeric(14, 4);
alter table llm_usage add column cost_usd numeric(24, 12) not null default 0;
