# 159-CR-On-Demand-Forecast-Parity-And-Precursor-Calibration

| | |
|---|---|
| Prompt | 159-On-Demand-Forecast-Parity-And-Precursor-Calibration.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `25188926a05efd16bf7b344938ec80aac772289c` |
| Date | 2026-10-22 |

## What was added

Both gaps the 151–158 as-built review surfaced are closed. Both are "finish what was started"; no new
API surface, no spec change, so Postman was not regenerated.

### 1 — On-demand / callback forecasts go through the push path's dedup + bookkeeping (155)

- The push path's material-change dedup is extracted from `ForecastBindingTarget.apply` into a shared
  static `ForecastBindingTarget.shouldRecordStanding(forecasts, questionId, forecasterId, value, now)`:
  it looks up the forecaster's standing forecast for the question and returns
  `ExternalForecastPolicy.shouldRecord(...)`. `apply` now calls it (behaviour unchanged there).
- `AnalysisRequestService.fulfillInternal` (the callback and synchronous as-of recording path) now:
  - calls `ForecastBindingTarget.shouldRecordStanding(...)` and, when it returns false, marks the
    correlation `FULFILLED` and records nothing — the same material-change dedup a pushed forecast gets;
  - on a record, stamps `lastForecastAt` on every question link of the correlation's `(sourceId,
    streamId, questionId)` via the new
    `ExternalQuestionLinkRepository.findBySourceIdAndStreamIdAndQuestionId`.
  - The forecaster identity (`ForecasterRegistry.ensure` of `edp:{sourceId}/{streamId}`) and the
    `ForecastService.record` terminal are untouched, so scoring is identical to before; only the dedup
    and link bookkeeping become consistent with the push path.
- The retrospective backtest path (`forecastSyncAsOf`) is unchanged — it returns a value for the runner
  to score and records nothing, so there is nothing to dedup there.

### 2 — Precursor-active probabilities are calibrated (151)

- `PrecursorLiveWatch.write` now remaps the raw shrink-to-base-rate probability from
  `PrecursorRules.probability` through `IntelCalibrationService.calibrate(SCOPE_PRECURSOR, raw)` when the
  scope is calibrated (≥ `Calibration.CORP_MIN` resolved outcomes), exactly as `IntelEventService` remaps
  intel-event confidence; otherwise the raw probability stands unchanged. `IntelCalibrationService` is
  injected as an `ObjectProvider` (matching the watch's existing optional dependencies).
- The finding's `stats` now carry `rawProbability` (the uncalibrated value) and a `calibrated` boolean
  alongside `probability` (the reported, calibrated-or-raw value). The base-rate caveat text states the
  recalibration when it is applied, and keeps its original wording verbatim when it is not.
- `PrecursorOutcomeResolver.probability` now reads `stats.rawProbability` (falling back to `probability`
  for findings written before 159), so the calibration curve is always fitted on raw scores and never on
  a probability it already remapped — avoiding a feedback loop that would corrupt the fit.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `AnalysisRequestServiceTest` — two new cases: a callback whose value matches the standing forecast
    within a day is deduped (no `ledger.record`, correlation still `FULFILLED`, empty result); a recorded
    callback stamps `lastForecastAt` on the question link and saves it. The constructor gained the
    `ExternalQuestionLinkRepository` mock; the existing synchronous/callback cases still pass (the
    standing-forecast finder defaults to empty, so they record as before).
  - `PrecursorLiveWatchIT.precursorProbabilityIsCalibrated` — seeds ≥ `CORP_MIN` resolved
    `precursor-active` outcomes whose observed frequency is above the forecast, confirms the fit moves the
    raw probability, then asserts a new `precursor-active` finding reports `calibrated=true`,
    `rawProbability` equal to the raw value, and `probability` equal to the service's calibrated value. The
    existing `antecedentOccurrence` case still asserts the raw probability (its scope has no outcomes, and
    every IT method starts with empty tables).
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive). US English verified
  manually over the changed files against `gradle/us-english/british-words.txt` — none (the `fulfill` /
  `FULFILLED` / `setFulfilledAt` identifiers are the American double-l spelling, pre-existing from 155).
  No spec change, so no OpenAPI/Postman regeneration.

## Owner decisions / notes

- **Dedup shared as a static, not by calling `apply`.** `fulfillInternal` cannot build the
  `BoundItem`/`BoundStream` that `apply` needs, so the prompt's "share those two steps" was realized by
  extracting the material-change decision (`shouldRecordStanding`) and having both paths call it, each
  stamping `lastForecastAt` from its own link source. Lower coupling than a reverse call into `apply`.
- **The on-demand link is found by `(sourceId, streamId, questionId)`.** The push path knows the
  provider's external question id; the on-demand correlation knows only the ledger question, so the new
  finder stamps every link of that stream pointing at the question (normally one). No schema change.
- **A deduped on-demand answer is `FULFILLED`, and `Requested.recorded` is false.** The provider answered,
  so the correlation is fulfilled; no new forecast was recorded, so `recorded` is false — honest for the
  rare case of a same-day no-material-change duplicate.
- **Raw vs reported probability kept separate (feedback-loop safety).** Because
  `PrecursorOutcomeResolver` reads the finding's probability back as the calibration training input,
  writing the calibrated value into the field it reads would double-apply calibration over time. The raw
  value is therefore preserved in `stats.rawProbability` and the resolver reads that; `stats.probability`
  carries the reported (calibrated) value. This mirrors the intel-event design (`rawConfidence` recorded,
  `confidence` reported).
- **`docs/Roadmap.md` was modified in the working tree before this run and is not mine.** It marks 151–158
  Done and adds a 159 row as `Planned`. I left it unstaged and did not touch it (stage-only discipline);
  since 159 is now implemented, you may want to flip that row to `Done` yourself.
- No tests, runtime or soak verification performed; deferred to the owner at code-complete.
