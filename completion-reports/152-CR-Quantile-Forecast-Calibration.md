# 152-CR-Quantile-Forecast-Calibration

| | |
|---|---|
| Prompt | 152-Quantile-Forecast-Calibration.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `a9f861c9368cb63564dc597b4411413aba1289f3` |
| Date | 2026-10-22 |

## What was added

Forecast calibration was binary-only (final probabilities → CORP/bins). Numeric forecasters were *scored*
(CRPS, interval coverage) but never *calibration-reported*. This adds a numeric/quantile calibration so a
quantile forecaster (including an external one like TimesFM) is judged on whether its stated quantiles are
reliable, not just sharp.

### A — per-level coverage captured at scoring time

The score row (`ForecastScoreEntity`) already stored CRPS, per-level pinball losses and the two interval
hits, but not the raw quantiles or the realized value, so per-level coverage could not be recovered after the
fact. `ForecastScorer.quantileCoverage(quantiles, y)` now records, per stored level, whether the outcome fell
at or below that quantile (`y ≤ q_τ`); `QuestionScorer.numeric` sets it on every numeric row. New nullable
`quantile_coverage` jsonb column (migration `V3`), null for pre-152 and non-numeric rows.

The empirical mean of each level's indicator is the coverage `P(y ≤ q_τ)`, which equals τ when the quantiles
are reliable; the indicators also place the outcome in a PIT bin.

### B — the numeric branch of `Scorecards.calibration`

`Scorecards.calibration` now partitions the forecaster's admitted rows into binary (final probability present)
and numeric (`NUMERIC` with coverage). If any binary rows exist it runs the **unchanged** binary path; a
numeric-only forecaster/question runs `numericCalibration`, which builds:

- **quantileReliability** — one point per stated level: `nominal` (τ) vs `empirical` coverage and `n`.
- **pit** — a PIT histogram over the required-quantile bins (`[0,.05) [.05,.25) [.25,.5) [.5,.75) [.75,.95)
  [.95,1]`), each bin with its `count`, `frequency` and `expected` width; a calibrated forecaster's frequency
  is near the width.
- **summary** — `meanAbsoluteCoverageError` and `maxCoverageDeviation` over the stated levels.

`getForecastCalibration` is unchanged in signature and returns the numeric report for a numeric-only
forecaster/question and the binary one otherwise (the routing lives in `Scorecards`).

### C — spec (1.3.0, no version bump)

- Added `ForecastNumericCalibration` (`type: numeric`); `getForecastCalibration`'s 200 response is now
  `oneOf [ForecastCalibration, ForecastNumericCalibration]`, with an updated description and a 1.3.0 changelog
  entry. Postman regenerated with `scripts/generate-postman.py`. `info.version` stays `1.3.0` (151 did the
  bump).

## What runs

- `./gradlew compileJava compileTestJava` — passes.
- No tests run (owner directive). Tests written but **not** executed: `NumericCalibrationTest` — evenly spread
  outcomes give coverage within 0.03 of each nominal level and a small mean error; outcomes forced into the
  low tail over-cover every level and are flagged by a large coverage error; a binary forecaster still gets
  the binary (`bins`) report.
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (the prompt says no other Gradle checks). US
  English verified manually against `gradle/us-english/british-words.txt` (no British inflection in the diff);
  `docs/OpenAPI.json` validated as well-formed JSON.

## Owner decisions / notes

- **`checkApiCompat` (owner run):** the 200 response of `getForecastCalibration` changes from a single
  `$ref` to `oneOf [ForecastCalibration, ForecastNumericCalibration]`. The binary schema is unchanged and
  included in the `oneOf`; this is a response widening, but confirm oasdiff treats it as non-breaking when the
  gate runs.
- **Existing rows:** numeric calibration only sees rows scored at or after 152 (older rows have null
  `quantile_coverage`). A full re-score (`ScoringService.rescoreOutdated`) is triggered by a scorer-version
  change, which 152 did not make; if the owner wants the back-catalog covered, bump `ForecastScorer.VERSION`
  or re-score numeric rows. Flagged rather than bumped, to keep the change additive.
- **Mixed forecasters:** a forecaster with both binary and numeric rows reports the binary calibration (binary
  rows present). Documented; per-type calibration would need a type parameter on the endpoint.
- Flyway `V3` is the second additive migration after `V2` (151); both for the owner's baseline verification.
- No tests, runtime, migration or soak verification performed; deferred to the owner at code-complete.
