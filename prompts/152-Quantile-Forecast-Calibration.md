# 152 — Numeric / quantile forecast calibration

**Goal.** Forecast calibration today is **binary-only**; numeric forecasts are scored (CRPS, interval
coverage) but never *calibration-reported*. Add a numeric/quantile calibration so quantile forecasters
— including an external one like TimesFM — can be judged on whether their stated quantiles are
reliable, not just sharp.

**Read first.**
- `src/main/java/ai/foresignal/forecast/score/Calibration.java` — binary CORP/isotonic + Murphy +
  reliability bins.
- `src/main/java/ai/foresignal/forecast/score/Scorecards.java` — `calibration` (~L199-249, binary
  final probabilities only) and `scorecard`/`compare`.
- `src/main/java/ai/foresignal/forecast/score/ForecastScorer.java` — CRPS from quantiles (~L126-136),
  interval hits p05p95/p25p75 (~L139-144).
- `getForecastCalibration` in `ForecastController`; `Ledger.REQUIRED_QUANTILES` (p05,p25,p50,p75,p95).

**Build.**
- Add a **numeric branch** to `Scorecards.calibration`: over resolved NUMERIC questions, compute a
  **PIT histogram** and a **quantile-reliability diagram** — the empirical coverage of each stated
  quantile level vs its nominal level (e.g. does the p90 band actually contain the outcome ~90% of the
  time) — plus a calibration summary stat. Leave the binary path unchanged.
- Make `getForecastCalibration` return the numeric calibration for a numeric question or forecaster,
  the binary one otherwise.
- **No version bump** — this prompt is part of the **1.3.0** wave (151 did the bump). Add the numeric
  calibration response shape to `docs/OpenAPI.json` under 1.3.0; regenerate Postman.

**What runs.** Compile only (`./gradlew compileJava compileTestJava`); no tests or other checks per
`000-Run-Sequence.md`. Write tests (a well-calibrated synthetic quantile set scores near-nominal
coverage; a biased one is flagged) but do not run them. US English.

**Done when.** Numeric forecasters get a PIT/quantile-reliability calibration report via
`getForecastCalibration`; binary calibration unchanged; spec + Postman updated under 1.3.0; compile
passes.

**Commit.** `152: Numeric/quantile forecast calibration` + `completion-reports/152-CR-*.md`. **Stage
only this prompt's files.**
