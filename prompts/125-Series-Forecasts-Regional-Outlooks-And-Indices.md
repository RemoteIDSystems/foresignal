# 125 — Series Forecasts Regional Outlooks And Indices

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/125-CR-Series-Forecasts-Regional-Outlooks-And-Indices.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/124-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2, §9 (series forecaster, regional outlooks, scored indices), §12 (libraries), §14.4
- Completion Reports of 116 (canonical events, observability, manifests), 038 (glance pattern), 040 (regions), 081 (ACLED/UCDP event types), 100 (slot baseline), 103 (job framework), 117 (series metadata, World Bank covariates), 119 (`SeriesReader`, registry, findings, statistics package), 121 (base rates, reference history, reporting density), 122 (ledger, `openQuestion`, forecasters, scoring)

## Objective

Add Foresignal's own statistical forecasts and make them accountable. A **series forecaster** chooses a method per series by rolling-origin backtest and publishes quantile forecasts for 1, 7 and 28 days. **Regional outlooks** (in the style of ACLED CAST) forecast event counts per region and family for the next six 4-week windows, each one a ledger question so its accuracy is published beside it. **Regional indices** are transparent, decomposed composites whose weights are fitted to outcomes and whose out-of-sample skill is always shown, unlike competitors' hand-weighted indices.

## Principles (binding)

- Every forecast here is written to the ledger and scored; every index shows its current out-of-sample skill.
- Method selection, weight fitting and evaluation use rolling origins with no look-ahead (`knownAt`, `asOf`).
- Indices never claim to be probabilities. Fitted weights are **proposed**, not silently applied.

## Tasks

### 1. Series forecaster (`ai.foresignal.outlook.series`, `series-forecast` 1.0.0)

- Dependency: Hipparchus (optimisers; verify the current 4.x on Maven Central and that its LICENSE is Apache-2.0) in addition to 119's Commons Statistics.
- **Methods** (each a pure class with golden tests):
  - `seasonal-naive` (the benchmark; weekly season for daily data);
  - `ets-ann` and `ets-aaa` (additive error, no or additive trend, additive weekly season) on `log1p(x)` for counts and on levels otherwise, parameters by maximum likelihood (Hipparchus bounded optimiser), prediction intervals from the ETS variance formula, back-transformed;
  - `nb-glm` for counts: `log μ = β₀ + β₁·t + day-of-week + 2 annual Fourier pairs + β·log1p(y_{t−7})`, fitted by IRLS (Poisson start, NB dispersion by moments); quantiles from the NB predictive distribution with a 200-draw parametric bootstrap of β for parameter uncertainty;
  - `damped-trend` (ETS(A,Ad,N)) for continuous series.
- **Selection:** for each registered series with `detectors ∋ forecast` (enable by default for region-day rollups with ≥ 365 days and for 117 market and attention series), run a rolling-origin backtest over the last 8 origins (weekly spacing) per method and horizon; pick the method with the lowest mean pinball loss across quantiles at the 7-day horizon; store `series_forecast_model(ref, method, params, backtest jsonb, selected_at, version)`. Re-select monthly.
- **Forecasts:** nightly after 119's change-point run: horizons 1, 7, 28 days; quantiles p05, p10, p25, p50, p75, p90, p95 (monotone-adjusted); stored in `series_forecast(ref, made_at, asof_seq, horizon, target_period, quantiles, method, version)`.
- **Ledger:** for refs listed in `analysis.forecasts.questionSeries` (operator setting; defaults: PortWatch Bab el-Mandeb, Hormuz and Suez transits, COT WTI `mm_net`), open numeric questions per horizon via `ForecastService.openQuestion` (`series-value` resolver, category `activity` or `markets`), forecaster `statistical`, plus the 122 `climatology` and `persistence` baselines automatically.
- **Exceedance:** a realised value outside the 1-step p05–p95 band → `forecast-exceedance` finding (119 table; `notable`; `significant` outside p01–p99 computed from the same distribution).
- API: `GET /v1/analytics/series/forecast?ref&horizon` (read; LOCAL_ONLY refs return quantiles in standardised units unless the caller may see local values), `GET /v1/analytics/series/forecast/model?ref` (read; method and backtest).

### 2. Regional outlooks (`ai.foresignal.outlook.regional`, `regional-outlook` 1.0.0)

- **Families** (configurable in `src/main/resources/intel/outlook-families.json`): `conflict:battles`, `conflict:explosions-remote`, `conflict:violence-civilians`, `conflict:protests` (ACLED where available, UCDP otherwise), `outage` (IODA + Cloudflare Radar), `weather-severe` (NWS and CAP warning counts), `seismic:m4.5`. Regions: countries, plus admin1 where the family's data has ≥ 52 weeks with ≥ 5 events in that admin1.
- **Model:** per region and family, an NB GLM on weekly counts with: lagged counts (1, 2, 4 weeks; log1p), a 13-week moving average, seasonality (annual Fourier pair), neighbours' lagged counts (040 adjacency, sum of log1p), the region's index (§3) when defined, and a region random-effect approximated by partial pooling toward the region-class mean (empirical Bayes shrinkage of the intercept). Fit on reference history (121) plus Foresignal's own rollups; refit monthly; rolling-origin evaluation over the last 26 weeks against seasonal-naive and the 122 `climatology` forecaster.
- **Output:** weekly (Monday 06:00 UTC), for the next six rolling 4-week windows, quantiles of the count: a `regional_outlook` row and an `foresignal-outlook` record (kind `regional-outlook`, `externalId = {region}:{family}:{windowStart}`), plus a numeric ledger question per window (`count-threshold` resolver in numeric mode over the same data source; category `conflict` or the family's category) with forecaster `statistical`. The record payload carries the latest out-of-sample skill and coverage of the p10–p90 interval from the ledger, so accuracy travels with the forecast.
- Counts for families with canonical events (116) count canonical events; outlooks for regions with low detectability carry `LOW_DETECTABILITY`; every forecast, outlook, index value and fit stores a `manifest_id`.
- **Outlook exceedance:** when a window resolves above its p95 (or below p05 with coverage ok) → `outlook-exceedance` finding.
- Regions with `LOW_REPORTING_DENSITY` (121) are flagged on every outlook.
- API: `GET /v1/outlooks?family&region&cursor` (read), `GET /v1/outlooks/{region}/{family}` (read; the six windows, history of past outlooks vs actuals, skill).

### 3. Regional indices (`ai.foresignal.outlook.index`, `regional-index` 1.0.0)

- `regional_index_def`: `id`, `name`, `description`, `components` jsonb (each `{key, ref (SeriesRef template with {region}), transform: log1p|zscore|pctrank|level, window: P7D|P28D|P90D, direction: +1|−1, floor?}`), `weights` jsonb (component → weight, summing to 1), `floors` jsonb (rules such as "if `ucdp war` in last 12 months, index ≥ 70"), `target` (an outlook family the index should anticipate), `weights_version`, `weights_origin` (default | fitted | operator), `enabled`.
- Built-in definitions (disabled until their component series exist): **`instability`** (conflict events, protests, fatalities when available, outages, travel advisory level from 090, GDELT tone for the country topic if defined, attention, sanctions changes) targeting `conflict:battles`; **`maritime-risk`** (chokepoint transit change, AIS-gap and loiter findings, nav warnings, GNSS interference) targeting chokepoint disruption change points; **`infrastructure-stress`** (outages, grid-frequency excursions, severe weather) targeting `outage`.
- **Computation:** daily per region: each component normalised over the region-class history (percentile rank by default) into [0, 100], weighted sum, floors applied, 24 h and 7 d deltas. Output: `regional_index_value(def_id, region, day, value, components jsonb (each component's normalised value and contribution), floors_applied, asof_seq)` and an `foresignal-outlook` record (kind `regional-index`), emitted on change ≥ 1 point.
- **Fitting:** monthly (127) or `POST /v1/indices/{id}/fit` (agent): regularised NB regression (L2, λ by rolling-origin validation) of the next-4-week target count on the lagged normalised components across all regions; non-negative weights for components with `direction` declared, renormalised to 1. Report out-of-sample skill (pinball and log score of the implied count forecast vs the seasonal-naive outlook), per component significance, and the proposed weights. Applying proposed weights is `POST /v1/indices/{id}/weights {source: fitted, version}` (agent; logged), or automatic only when the operator set `autoApplyFittedWeights=true`.
- Every index response includes `skill` (latest out-of-sample) and `weightsOrigin`, and the decomposition.
- API: `GET /v1/indices` (read), `GET /v1/indices/{id}?region&from&to` (read), `PUT /v1/indices/{id}` (agent; definition, enabled), `POST /v1/indices/{id}/fit` (agent, 202 job), `POST /v1/indices/{id}/weights` (agent).

### 4. MCP, SSE, spec, docs

- MCP: `get_series_forecast`, `get_outlook`, `get_index` (read; read catalogue); `fit_index` (agent).
- SSE topic `analytics`: `outlook.published`, `index.changed` (≥ 5 points), `index.weights_proposed`.
- `docs/OpenAPI.json`: `SeriesForecast`, `SeriesForecastModel`, `RegionalOutlook`, `OutlookWindow`, `RegionalIndexDef`, `RegionalIndexValue`, `IndexFitResult`; tag `Outlooks`; `x-foresignal-scope`; Postman BODIES for an index definition, fit and weights.
- AccessPolicy rows. Problem codes: `UNKNOWN_INDEX`, `UNKNOWN_OUTLOOK` (404), `INDEX_INVALID` (400), `FORECAST_MODEL_NOT_READY` (409).
- `docs/Operator-Guide.md` "Outlooks and indices": method selection, reading quantiles and pinball loss, CAST-style windows, why index weights are fitted and proposed, reporting-density caveats.

## Tests to write (do not run)

- Method golden tests: seasonal naive; ETS(A,N,N) and (A,A,A) fits and intervals against reference values from a known series; NB GLM IRLS on a synthetic dataset recovers coefficients within tolerance; bootstrap quantiles monotone.
- `MethodSelectionTest`: rolling-origin pinball selects the planted best method; no look-ahead (a future spike does not change the selection).
- `SeriesForecastIT`: nightly forecasts stored; ledger questions opened for configured refs with `statistical`, `climatology` and `persistence` forecasts; exceedance finding.
- `RegionalOutlookIT`: six windows per region and family; admin1 eligibility rule; ledger questions; skill carried in the record; exceedance on resolution; low-density flag.
- `RegionalIndexTest`: normalisation, weights, floors, decomposition sums to the value, deltas.
- `IndexFitIT`: planted relationship recovered; weights proposed not applied; auto-apply only with the setting; skill reported.
- `OutlookControllersIT`: endpoints, scopes, LOCAL_ONLY handling, MCP shapes.

## Acceptance criteria

- Registered series get method-selected quantile forecasts that are also ledger questions, scored automatically.
- Regional outlooks publish six 4-week windows per region and family with their measured accuracy attached.
- Regional indices are decomposed, fitted to outcomes on request, and always show out-of-sample skill and weight origin.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `125: Series Forecasts Regional Outlooks And Indices`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/125-CR-Series-Forecasts-Regional-Outlooks-And-Indices.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `125: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
