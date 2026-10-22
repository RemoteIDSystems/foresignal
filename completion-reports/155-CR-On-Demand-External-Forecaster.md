# 155-CR-On-Demand-External-Forecaster

| | |
|---|---|
| Prompt | 155-On-Demand-External-Forecaster.md |
| Status | Complete (compile only; no test, `checkUsEnglish`, `lintOpenApi` or `checkApiCompat` was run, per `000-Run-Sequence.md` and the prompt) |
| Implementation commit | `1aaf5d1f41197934560712462517fb4ad1aab7d8` |
| Date | 2026-10-22 |

## What was added

A continuous external forecaster already worked (a pushed `forecast` item is scored like any forecaster, no
backend code). Prompt 155 makes a model like **TimesFM** first-class: an on-demand "forecast this question now"
round-trip, the ability to backtest it, admin naming/config, and acceptance of a decile provider. TimesFM stays
an external Apache-2.0 Python service; **no Python enters the Java backend.**

### A — analysis-request transport (the missing primitive)

`EdpManifestModel.AnalysisRequest(url, auth, header)` (new) generalizes the formerly inert `transport.search`
into an outbound endpoint Foresignal POSTs an on-demand request to, parsed from `transport.analysisRequest`
(`search` stays inert). `EdpHttpClient.post(uri, headers, json, allowPrivateNetwork, maxBytes)` (new) mirrors
`get()` through the same URL vetting, redirect and size guards; the private `send` now takes an optional body
publisher and content type (null body = GET).

`AnalysisRequestService` (new) runs the round-trip:

- `request(forecasterId, questionId, orientation, horizon, actor)` resolves the `edp:{sourceId}/{streamId}`
  forecaster to its provider (which must declare `transport.analysisRequest`), checks the question is open,
  persists a correlation (ULID id), and POSTs `{questionId, horizon, correlationId, callbackUrl}`. A provider
  that answers **synchronously** (forecast in the HTTP response) is recorded at once.
- `fulfill(correlationId, probability, quantiles)` is the **asynchronous** path: it resolves the correlation to
  its question and external forecaster and records the forecast through the usual `ForecastService.record` path.
- `forecastSyncAsOf(sourceId, streamId, question, asOf)` issues a synchronous as-of request for the backtest
  runner.

`AnalysisCorrelationEntity` + `AnalysisCorrelationRepository` + `V5__analysis_request_correlation.sql` (new)
store the pending request (`requested` → `fulfilled` / `failed`), keyed by the unguessable correlation id.
`AnalysisRequestController` (new, tag `External`) exposes `requestAnalysis` (POST `/v1/external/analysis/requests`)
and `analysisCallback` (POST `/v1/external/analysis/callback`). `AccessPolicy`: the request is `agent`; the
callback is `open` because the unguessable `correlationId` in the body is the credential (the same pattern as
`/v1/external/register`).

### B — backtesting external forecasters

`ExternalRetrospectiveRunner` (new) implements `RetrospectiveRunner` for `type=external`, so the evaluator no
longer skips external forecasters: it issues a synchronous as-of analysis request per question and wraps the
result as a `BaselineForecaster.Outcome` (or a skip when the provider returns nothing usable or only answers
asynchronously). `EvaluationService.forecastAsOf` already picks runners by `handles(forecaster)`, so no change
there was needed.

### C — first-class external-forecaster config

- `ForecasterRegistry.registerExternal(id, name, config)` (new) pre-registers, names and reconfigures an
  external forecaster independent of the auto-derived id; `update(id, active, config, name)` (new overload) sets
  a display name on update (the 3-arg `update` delegates to it, so existing callers are unaffected).
- `ForecastController.registerExternalForecaster` (POST `/v1/forecasts/forecasters`, admin) and the updated
  `updateForecaster` (now passes `name`).
- `ForecastInputs.externalForecasterRegistration` (new) and `forecasterUpdate` (now parses optional `name`,
  1–200 chars).
- `EnsembleService`: the external-resolved-count gate reads `forecaster.configNumber("externalMinResolved", …)`,
  so a named external forecaster can override the ensemble's global `externalMinResolved`.

### D — decile mapping

`DecileQuantiles.toLedger(quantiles)` (new, pure) maps a decile forecast (`p10`…`p90`, as TimesFM emits) onto
`Ledger.REQUIRED_QUANTILES` (`p05`, `p25`, `p50`, `p75`, `p95`) by monotone piecewise-linear interpolation
within the provided range and extrapolation of the `p05`/`p95` tails from the two nearest points, clamped
non-decreasing. A set that already carries every required quantile, or that has fewer than two points, is
returned unchanged. `ForecastBindingTarget.apply` wraps its quantiles through it, so a decile provider is
accepted on both the push/pull binding path and the analysis-request path without loosening the ledger's
required set.

### E — spec, EDP schema and Postman

- `docs/protocol/edp-1.0/manifest.schema.json` (served by `getEdpSchema`): `EdpAnalysisRequest` `$def` and the
  `transport.analysisRequest` property.
- `docs/OpenAPI.json` (1.3.0, no version bump): the `EdpAnalysisRequest` schema and `transport.analysisRequest`;
  the `requestAnalysis` and `analysisCallback` operations; `registerExternalForecaster` with the
  `ExternalForecasterRegistration` schema; `AnalysisRequestSubmission`, `AnalysisRequestResult` and
  `AnalysisCallback` schemas; `name` and the `externalMinResolved` note on `ForecasterUpdate`; a 1.3.0 changelog
  entry.
- `docs/External-Data-Protocol.md` rewritten whole: §4.4 "Analysis request", the transport-table row, the
  decile-mapping note in §5, the forecast-item note, and `analysisRequest.url` in the §7 URL-vetting list.
- Postman regenerated with `scripts/generate-postman.py`: 855 requests, the three new operations added.

## What runs

- `./gradlew compileJava compileTestJava` — passes (main + test).
- No tests run (owner directive). Tests written but **not** executed:
  - `DecileQuantilesTest` — a full decile set maps to every required quantile (interpolated p25/p50/p75,
    extrapolated p05/p95); the output stays non-decreasing when deciles crowd; a complete set and a
    too-small set are returned unchanged; null/empty are handled.
  - `AnalysisRequestServiceTest` — a request persists the correlation (right question and forecaster id) and
    records a synchronous provider's forecast at once against the correlated question; a callback records
    against the question the correlation names and marks it `fulfilled`; an unknown correlation is ignored;
    the `edp:` forecaster id parses to its source and stream.
  - `ExternalRetrospectiveRunnerTest` — the runner handles `external` and no other type; a usable as-of
    forecast becomes an outcome (source/stream from config, or falling back to the forecaster id); no forecast
    is a skip, not a failure.
- `checkUsEnglish`, `lintOpenApi`, `checkApiCompat` **not** run (owner directive). US English verified manually
  against `gradle/us-english/british-words.txt` over the changed non-excluded files: the only hits are the EDP
  `source.licence` wire field (a protocol field — exempt) and a pre-existing "metres" in a schema description
  line that was not touched. `docs/OpenAPI.json` and the manifest schema validated as well-formed JSON.

## Owner decisions / notes

- **One endpoint serves both the on-demand round-trip and the backtest.** A provider answers synchronously (the
  forecast in the HTTP response — used by `request()` for immediate recording and by the backtest runner) and/or
  asynchronously (a later POST to `/v1/external/analysis/callback`). A provider that only pushes or only answers
  asynchronously cannot be backtested, and those questions are skipped — flagged, not worked around.
- **The callback is `open`, authenticated by the correlation id.** The ULID correlation id is sent to the
  provider and echoed back; an unknown or already-answered id is accepted and ignored. This matches the existing
  "credential in the body" pattern (`/v1/external/register`), since a provider will not usually hold an admin
  token. If the owner wants the callback behind a token or a per-source shared secret instead, that is a small
  change to `AccessPolicy` and the controller.
- **Decile mapping is binding-side, so the ledger's required set is unchanged.** `Ledger.REQUIRED_QUANTILES`
  stays `p05/p25/p50/p75/p95`; the mapping is applied where an external forecast enters (`ForecastBindingTarget`
  and the analysis-request path), never by relaxing the ledger. The tail extrapolation is linear from the two
  nearest deciles; a provider that already sends the required levels is passed through untouched.
- **`registerExternal` keeps an existing forecaster's type.** Re-registering an id that already exists renames
  and reconfigures it in place rather than forcing it to `external`, so it is safe to call on any id.
- **`checkApiCompat` (owner run):** three additive operations and four additive schemas, plus optional
  `transport.analysisRequest` and an optional `name` on `ForecasterUpdate` — confirm oasdiff treats these as
  non-breaking when the gate runs.
- No tests, runtime, migration (V5) or soak verification performed; deferred to the owner at code-complete.
