# 156-CR-Probabilistic-Threshold-Crossing

| | |
|---|---|
| Prompt | 156-Probabilistic-Threshold-Crossing.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `be3f5cbc3f5150e7309cac7f60c3955e6d788ada` |
| Date | 2026-10-22 |

## What was added

A "will this series cross X by date Y" binary question is now answered from a numeric **predictive
distribution** instead of a flat confidence. The deterministic resolver and every other forecaster are intact;
this is an added forecaster plus a narrowing of persistence's reach.

### A — the crossing probability (pure)

`ThresholdCrossing` (new) reads `P(value op threshold)` off the CDF implied by a quantile map: the quantiles
are points (level → value), the CDF at the threshold is linear interpolation between the two bracketing
quantiles (and linear extrapolation of the nearest segment beyond the outermost ones, clamped to `[0, 1]`), and
`gt`/`gte` → `1 − CDF`, `lt`/`lte` → `CDF`. The result is clamped away from certainty to `[0.01, 0.99]`.
Crossed quantiles are rearranged so the CDF is monotone. An `eq` op, or a distribution with fewer than two
distinct points, returns `NaN` (the forecaster then skips).

### B — the forecaster

`ThresholdCrossingForecaster` (new baseline, id/type `threshold`) supports a BINARY `series-value` or
`count-threshold` question that has an `op`, a `threshold` and a series `ref`. It assembles the numeric
predictive distribution for the resolution window and reads the crossing probability off it:

- **Numeric baseline** — `ClimatologyForecaster` run on an in-memory numeric twin of the question (same series,
  window and aggregate, no `op`/`threshold`), reusing climatology's existing logic unchanged. For a
  `count-threshold` the twin aggregates the rollup series with `sum`.
- **Statistical series forecast** — when `SeriesForecastService` has a forecast row whose target window matches
  (ETS today; an external quantile forecaster such as TimesFM tomorrow).
- When both are present they are combined with `EnsembleMath.combineQuantiles` (the **ensemble quantile pool**);
  otherwise whichever one is available is used; if neither is, the forecaster skips rather than guessing.

### C — persistence no longer flat-guesses these

`PersistenceForecaster.supports` now returns false for a binary `series-value` question with an `op` (a
threshold), so its flat `binaryConfidence` no longer contributes there. Numeric `series-value` (carry the last
value forward) and `situation-status` are unchanged.

### D — registration and docs

- `ForecasterRegistry`: the `threshold` builtin is registered at startup (constant, default config, display
  name "Threshold crossing"), so the `BaselineRunner` runs it on open questions and the evaluator backtests it
  like any baseline.
- `docs/OpenAPI.json` (1.3.0, no version bump): `threshold` added to the four forecaster-type enums
  (`listForecasters` filter, `Forecaster`, `ScorecardEntry` and the forecast type list) and a 1.3.0 changelog
  entry. No operation or schema shape changed, so the Postman collection is byte-identical
  (`scripts/generate-postman.py`: 855 requests, unchanged) and was not re-committed.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `ThresholdCrossingTest` — a threshold at the median is ≈ 0.5 for either direction; a threshold deep in a
    tail is ≈ 0.01 / 0.99; the probability tracks the CDF between quantiles (P(>20) = 0.75, CDF(25) = 0.375);
    `eq` and a one-point distribution yield `NaN`; crossed quantiles are rearranged monotone.
  - `ThresholdCrossingForecasterTest` — it supports binary series threshold questions and not numeric ones; the
    crossing probability comes from the numeric baseline distribution (median ≈ 0.5, deep tail ≈ 0.99); with no
    distribution available it skips.
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive). US English verified manually
  over the changed non-excluded files against `gradle/us-english/british-words.txt` — none. `docs/OpenAPI.json`
  validated as well-formed JSON.

## Owner decisions / notes

- **The distribution is the predictive quantiles, pooled.** The prompt's priority (ensemble pool / external-155
  quantiles, else the numeric baseline) is realized as: climatology (always available where there is history) +
  the statistical series forecast (the "better distribution": it selects ETS now and will carry a 155 external
  quantile forecaster such as TimesFM), combined when both are present. A `count-threshold` over a `selector`
  (occurrence count, no rollup series) has no series distribution and is left to the base-rate forecaster —
  flagged as the one threshold shape this forecaster does not answer.
- **Persistence was narrowed, not removed.** Only the flat-confidence contribution to binary series-value
  threshold questions is withdrawn, exactly as the prompt asks; persistence still carries numeric series-value
  forward and still answers situation-status. Any test that asserted persistence's old flat answer on a binary
  series-value threshold question will need updating by the owner when the suite runs.
- **`eq` and degenerate distributions skip.** An equality crossing has probability zero under a continuous
  distribution, and a point distribution (a single quantile value) carries no crossing information, so the
  forecaster declines rather than emit a false 0/1.
- **`checkApiCompat` (owner run):** the only API change is adding the `threshold` enum value to forecaster-type
  enums — additive; confirm oasdiff treats the request-parameter and response enum additions as non-breaking.
- No tests, runtime or soak verification performed; deferred to the owner at code-complete.
