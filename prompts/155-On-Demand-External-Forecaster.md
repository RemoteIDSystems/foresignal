# 155 — On-demand analysis round-trip + first-class external forecaster (TimesFM)

**Goal.** A continuous external forecaster already works via the EDP (a pushed `forecast` item is
scored like any forecaster — zero backend code needed). Make an external forecaster like **TimesFM**
*first-class*: an **on-demand "forecast this question now" round-trip** (correlation id + callback), the
ability to **backtest** it, and admin config to **name/configure** it. TimesFM itself runs as its own
Apache-2.0 Python service — **no Python enters the Java backend.**

**Read first.**
- Transports: `src/main/java/ai/foresignal/inbound/edp/EdpManifestModel.java` — `push`/`pull`/`search`;
  **`transport.search` is an inert reserved field** (parsed, never invoked, ~L251). `EdpPullAdapter`.
- Push + bind: `ExternalSourcesController.pushExternalItems` (~L231); `binding/ForecastBindingTarget.apply`
  (~L98-143 → `ForecastService.record`); `binding/ExternalForecastPolicy`;
  `binding/ExternalQuestionLinkEntity` + `ExternalBindingsController`.
- Forecasters: `forecast/ForecasterRegistry.ensure` (external id `edp:{sourceId}/{streamId}`,
  `externalMinResolved=30` ~L74, ~L124-133); `ForecastController.updateForecaster` (~L227, toggles
  active + config only); `forecast/eval/EvaluationService.forecastAsOf` (~L282-298, **skips
  `type=external`** — no `RetrospectiveRunner`); `forecast/Ledger.java` `REQUIRED_QUANTILES`
  (p05,p25,p50,p75,p95, ~L39). TimesFM emits deciles 0.1–0.9.
- Context: `docs/Future-Roadmap.md` "External analyzer / on-demand analysis callback (EDP round-trip)".

**Build.**
- **Analysis-request transport (the one genuinely missing primitive):** generalize the inert
  `transport.search` into an outbound **analysis-request** — ForeSignal sends `{seriesRef or
  questionId, horizon, correlationId, callbackUrl}` to the provider, persists the correlation, and adds
  a **callback-ingest endpoint** that resolves the correlation and routes the returned forecast through
  the existing `ForecastBindingTarget` / `ForecastService.record` path. Enables "forecast THIS question
  now." Update the EDP protocol + schemas (`getEdpProtocol`/`getEdpSchema`) accordingly.
- **External `RetrospectiveRunner`:** add a runner for `type=external` so `createForecastEvaluation`
  can backtest an external forecaster by issuing as-of on-demand requests (depends on the round-trip).
  Today external forecasters are skipped entirely.
- **First-class external-forecaster config:** let an operator pre-register, name, and configure an
  external forecaster (display name; per-external `externalMinResolved`) independent of the auto-derived
  `edp:{…}` id — extend `ForecasterRegistry` + `updateForecaster`/`ForecastController`.
- **Decile mapping:** allow a decile provider (0.1–0.9) to map to `Ledger.REQUIRED_QUANTILES`
  (p05/p25/p50/p75/p95) via a documented binding-side mapping, so TimesFM's output is accepted without
  loosening the ledger's required set.

**API / docs.** No version bump (1.3.0 wave). Add the analysis-request transport to the manifest schema,
the callback-ingest endpoint, and the external-forecaster config fields to `docs/OpenAPI.json` under
1.3.0; update the EDP schema export; regenerate Postman. Keep `checkApiCompat` additive.

**What runs.** Compile only; no tests/checks per `000-Run-Sequence.md`. Write tests (an analysis
request + callback records a forecast against the right question via correlation id; an external
forecaster backtests; decile input maps to the required quantiles) but do not run them. US English.

**Done when.** ForeSignal can request an on-demand forecast and ingest the correlated result; an
external forecaster can be backtested and named/configured; a decile provider is accepted; spec +
EDP schema + Postman updated under 1.3.0; compile passes. **TimesFM remains an external Python service;
no Python is added to the backend.**

**Commit.** `155: On-demand external forecaster round-trip` + `completion-reports/155-CR-*.md`. **Stage
only this prompt's files.**
