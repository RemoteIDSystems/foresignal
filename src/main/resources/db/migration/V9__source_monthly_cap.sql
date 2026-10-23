-- Budget.monthlyCapUsd: a per-source money cap per UTC calendar month (OpenAPI Budget, 135). Null means no cap.
alter table source_settings add column budget_monthly_cap_usd numeric(12, 4);
