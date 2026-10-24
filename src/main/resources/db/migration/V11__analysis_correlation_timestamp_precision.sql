-- analysis_correlation (V5) declared its instants as timestamptz; every other table, and the entity, use
-- timestamp(6) with time zone. Microseconds are already timestamptz's precision, so no stored value changes.
alter table analysis_correlation alter column created_at type timestamp(6) with time zone;
alter table analysis_correlation alter column fulfilled_at type timestamp(6) with time zone;
