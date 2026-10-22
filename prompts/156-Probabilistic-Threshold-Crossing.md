# 156 — Probabilistic threshold-crossing forecaster

**Goal.** "Will this series cross X by date Y" binary questions are currently answered with a **flat**
confidence (`PersistenceForecaster`). Derive the crossing probability from the numeric forecaster's
**predictive quantile distribution** instead — the one place a proper distribution (ETS today, TimesFM
tomorrow) materially improves the answer.

**Read first.**
- `src/main/java/ai/foresignal/forecast/resolve/ResolverSpec.java` — `series-value` / `count-threshold`
  with `op` + `threshold` → a BINARY question (~L121-185).
- `forecast/baseline/PersistenceForecaster.java` (flat confidence) and `ClimatologyForecaster.java`
  (NUMERIC quantiles).
- Series subsystem: `outlook/series/SeriesForecastService.java` (~L82), `outlook/series/QuantileSet.java`
  (levels 0.01–0.99, monotone), `forecast/ensemble/EnsembleMath.java` `combineQuantiles`.
- `forecast/ForecastService.record` (the shared write path).

**Build.**
- For a `series-value` / `count-threshold` BINARY question, compute **P(value `op` threshold)** by
  interpolating the numeric forecaster's predictive `QuantileSet` for the resolution horizon, and submit
  that as the crossing probability via `ForecastService.record` — replacing the flat-confidence
  `PersistenceForecaster` contribution for these questions. Keep the deterministic resolver and the
  existing forecasters otherwise intact; this is an added/overriding forecaster for threshold questions.
- When a better distribution is available (the ensemble quantile pool, or an external quantile
  forecaster from 155), use it; fall back to the existing numeric baseline distribution otherwise.

**API / docs.** Likely none (internal forecaster surfaced through existing forecast endpoints); if any
response shape changes, add it to `docs/OpenAPI.json` under **1.3.0** (no version bump) and regenerate
Postman.

**What runs.** Compile only; no tests/checks per `000-Run-Sequence.md`. Write tests (crossing
probability tracks the quantile distribution — a threshold near p50 ≈ 0.5, deep in a tail ≈ 0/1) but do
not run them. US English.

**Done when.** Threshold-crossing binary questions are answered from a predictive distribution, not a
flat confidence; existing forecasters unchanged otherwise; compile passes.

**Commit.** `156: Probabilistic threshold-crossing forecaster` + `completion-reports/156-CR-*.md`.
**Stage only this prompt's files.**
