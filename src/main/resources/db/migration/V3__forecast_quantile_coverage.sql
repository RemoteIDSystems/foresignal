-- Prompt 152: numeric / quantile forecast calibration. The per-quantile coverage indicators
-- (whether the outcome fell at or below each stated quantile) that the PIT histogram and the
-- quantile-reliability diagram are built from. Null for rows scored before 152 and for non-numeric rows.
alter table forecast_score add column quantile_coverage jsonb;
