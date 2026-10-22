# 159 — On-demand forecast path parity + precursor calibration (155/151 cleanup)

**Goal.** Close the two small gaps the 151–158 as-built review surfaced. Both are "finish what was
started," not new capability. No new API surface.

1. **On-demand/callback forecasts bypass the push path's dedup + bookkeeping (155).** The callback and
   synchronous as-of paths record through the same `ForecastService.record` terminal and the same
   forecaster identity (so they are scored identically), but via a parallel method that **skips** the
   push path's material-change dedup (`ExternalForecastPolicy.shouldRecord`) and the question-link
   `lastForecastAt` update. So on-demand forecasts aren't deduped or tracked the way pushed ones are.
2. **Precursor probabilities aren't calibrated (151).** `PrecursorOutcomeResolver` feeds reliability
   into the `SCOPE_PRECURSOR` calibration, and `IntelCalibrationService.calibrate(SCOPE_PRECURSOR, …)`
   exists, but it is **never applied** — only intel-event confidence is remapped. The curve is fitted
   and exposed but not used.

**Read first.** The 155-CR and 151-CR; `inbound/edp/AnalysisRequestService` (`fulfillInternal` — the
parallel record path) and `inbound/edp/binding/ForecastBindingTarget.apply` + `ExternalForecastPolicy`
(`shouldRecord`, the `link.setLastForecastAt` update); `intel/IntelCalibrationService` (`calibrate`,
`CORP_MIN`), `precursor/PrecursorLiveWatch` (where the `precursor-active` probability is written) and
`precursor/PrecursorRules.probability`.

**Build.**
1. Route the callback and synchronous as-of forecast recording through the **same** material-change
   dedup (`ExternalForecastPolicy.shouldRecord`) and question-link `lastForecastAt` update the push
   path uses — either by having `fulfillInternal` call `ForecastBindingTarget.apply`, or by sharing
   those two steps. Keep the identical forecaster identity and `ForecastService.record` terminal, so
   nothing about scoring changes — only the dedup and bookkeeping become consistent across push and
   on-demand.
2. Apply the `SCOPE_PRECURSOR` calibration curve to the `precursor-active` probability before it is
   written in `PrecursorLiveWatch` — remap it through `IntelCalibrationService.calibrate` when enough
   resolved history exists (`CORP_MIN`), exactly as intel-event confidence is remapped; otherwise leave
   the raw shrink-to-base-rate probability from `PrecursorRules.probability` unchanged.

**Do not touch** `transport.search` — it is reserved for the prompt-137 cue service, not dead; the
separate `transport.analysisRequest` field added in 155 is the correct design.

**What runs.** Compile only — `./gradlew compileJava compileTestJava`. Per `000-Run-Sequence.md`, run no
tests or other checks. Write tests (an on-demand duplicate forecast is deduped like a pushed one and
updates `lastForecastAt`; a precursor probability is remapped once its scope has ≥ `CORP_MIN` resolved
outcomes) but do not run them. US English. No version bump; regenerate Postman only if the spec changes
(it should not).

**Done when.** On-demand/callback forecasts go through the same dedup and `lastForecastAt` as pushed
ones; precursor probabilities are calibrated when history allows; compile passes.

**Commit.** `159: On-demand forecast parity + precursor calibration` + `completion-reports/159-CR-*.md`.
**Stage only this prompt's files.**
