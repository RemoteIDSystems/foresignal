# 126-CR-Series-Forecasts-Regional-Outlooks-And-Indices

| | |
|---|---|
| Prompt | 126-Series-Forecasts-Regional-Outlooks-And-Indices.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `04b52ef37a495f9fd2538f4a4f0f79e3e43ec31b` |
| Date | 2026-10-17 |

## Summary

This prompt adds Foresignal's own statistical forecasts under `ai.foresignal.outlook` and makes them accountable
through the 122 ledger. It compiles. **The tests are written but were not run** (owner directive 2026-10-17): no test
result, coverage figure or guard result in this report is measured.

- **Dependency:** `org.hipparchus:hipparchus-optim` 4.0.3, checked on Maven Central on 2026-10-17:
  - it is the latest release in `maven-metadata.xml`;
  - its license is Apache-2.0 (the POM's `<licenses>` and the project `LICENSE.txt`);
  - it is Java 8 bytecode, and the same version Orekit 13.1.9 already puts on the classpath.

  It is recorded in `gradle/libs.versions.toml`, `build.gradle.kts` and `THIRD_PARTY_NOTICES.md`.
- **Series forecaster** (`outlook.series`, `series-forecast` 1.0.0). The methods are pure classes:
  - `SeasonalNaive`;
  - `EtsAnn` and `EtsAaa`, both on `EtsModel`, an additive-error state-space core;
  - `NbGlmMethod`;
  - `DampedTrend`.

  How the methods work:
  - ETS fits maximum likelihood (Gaussian, concentrated in σ²) with the Hipparchus BOBYQA optimizer over a unit box
    mapped to the admissible region. Its intervals use the Hyndman et al. (2008) class-1 variance formula. Counts are
    fitted on `log1p` and their quantiles back-transformed.
  - The NB GLM is `analytics.math.NbGlm`, an in-house IRLS: Poisson start, dispersion by moments, optional L2 penalty
    and non-negative columns. Its regressors are trend, weekday dummies, two annual Fourier pairs and
    `log1p(y(t−7))`. Its quantiles come from a seeded 200-draw parametric bootstrap of β × 5 simulated paths
    (`analytics.math.NbSampling`, a gamma–Poisson sampler).
  - `QuantileSet` holds the nine levels p01–p99, the rearrangement into monotone order and the pinball loss.

  Selection and storage:
  - `MethodSelector` backtests over the last 8 weekly origins. At each origin it uses only the values known by then
    (`knownAt`), and it picks the lowest mean pinball loss at 7 days.
  - Results go to `series_forecast_model` and `series_forecast`, which hold p05–p95 plus p01/p99 tails, the
    standardization, a manifest, the question id and exceedance bookkeeping.

  `SeriesForecastService`:
  - selects a method when none exists or the current one is older than 30 days;
  - forecasts 1, 7 and 28 days nightly;
  - opens numeric `series-value` ledger questions for `analysis.forecasts.questionSeries` and records the
    `statistical` forecast. The default series are PortWatch chokepoint4/6/1 (Bab el-Mandeb, Hormuz, Suez) and COT
    `cot.067651.disaggregated.mm_net`;
  - writes `forecast-exceedance` findings: `notable` outside p05–p95, `significant` outside p01–p99, and `info` with
    `LOW_DETECTABILITY` for a fall of counts that could not have been seen.

  The 122 `climatology` and `persistence` baselines forecast the questions when they open.
- **Registry defaults:** automatic analysis-series rows now get the `forecast` detector for:
  - region-day rollups with at least 365 days of history;
  - `cftc-cot`, `fred-series`, `wikimedia-pageviews`, `gdelt-timelines` and market or attention families;
  - the PortWatch chokepoints.
- **Regional outlooks** (`outlook.regional`, `regional-outlook` 1.0.0):
  - **Families:** seven, in `intel/outlook-families.json` (`OutlookFamilies`).
  - **Counts:** `baserate.reference.ReferenceCounts` reads `reference_history`, one source per region by the 121
    priority. Families with canonical events read the folded canonical events.
  - **Eligible regions:** countries with 52 weeks of history and an event in the last two years; admin-1 regions with
    52 weeks of 5 or more events.
  - **Model (`RegionalOutlookModel`):** an NB GLM with:
    - lags 1, 2 and 4 weeks (log1p) and a 13-week mean;
    - an annual Fourier pair;
    - the neighbors' Σ log1p (`RegionNeighbors`);
    - the region's index when an enabled index targets the family.

    A partially pooled intercept (empirical Bayes toward the continent class) stands in for the region effect.
    Regions with too little history fall back to the 52-week mean with an NB spread. Each fit is evaluated over the
    last 26 weekly origins against seasonal naive and climatology. Fits are stored in `regional_outlook_model` and
    redone after 30 days.
  - **Weekly publication:** six 4-week windows from next Monday, simulated with 100 coefficient draws × 5 paths. Each
    publication writes:
    - a `regional_outlook` row;
    - six numeric `count-threshold` ledger questions forecast by `statistical`;
    - the `outlook.published` event;
    - `foresignal-outlook` records.
  - **Skill:** the outlook carries its skill: ledger n, pinball, CRPS, skill against climatology, p10–p90 coverage,
    and skill against seasonal naive from the evaluation. It also carries the flags `LOW_REPORTING_DENSITY`,
    `LOW_DETECTABILITY`, `LAGGED_PUBLICATION` and `SMALL_SAMPLE`.
  - **Resolution:** a resolution fills in the window's actual count and p10–p90 hit. Above p95, or below p05 when
    detectability is adequate, it writes `outlook-exceedance`.
- **Regional indices** (`outlook.index`, `regional-index` 1.0.0):
  - **Tables:** `regional_index_def`, `regional_index_value` and `regional_index_fit`.
  - **Built-ins:** three in `intel/regional-indices.json`, seeded disabled.
  - **`IndexInputs`:** validation, every problem `INDEX_INVALID`.
  - **`IndexComputer` (pure):**
    - normalization by pctrank, zscore, log1p or level against the continent pool;
    - direction and component floors;
    - weights renormalized over the components present, so the decomposition sums to the value;
    - floor rules, either `component` or `reference` (UCDP fatalities or event counts);
    - 24 h and 7 d deltas.
  - **`RegionalIndexService`:**
    - daily computation, with a backfill of 104 Mondays and 8 days on the first run;
    - `index.changed` at a move of 5 points or more;
    - PUT, with weights changes logged and `weightsVersion` bumped;
    - weights `fitted`, `operator` or `default`.
  - **`IndexFitter` (pure):**
    - an L2 NB regression with non-negative component coefficients;
    - λ chosen by forward-chaining validation;
    - holdout skill (pinball and log score) against the seasonal-naive outlook;
    - per-component Wald significance;
    - proposed weights.
  - **`IndexFitService`:** a 103 `JobRunner` of type `index-fit`. It proposes the weights (`index.weights_proposed`)
    and applies them only with the index's `autoApplyFittedWeights`.
- **Records:** `ForesignalOutlookGenerator` is the DERIVED source `foresignal-outlook`, 1825 days, with an originator
  mapping in `intel/originators.json`. It emits one record per outlook window
  (`{region}:{family}:{windowStart}`, on change) and one per index region (`{indexId}:{region}`, on a move of 1 point
  or more).
- **API, SSE, MCP:**
  - `api/OutlookController` serves the 9 operations, and `AccessPolicy` has their rows.
  - The `analytics` topic carries `outlook.published`, `index.changed` and `index.weights_proposed`:
    - `FindingService.announce` writes them to `analytic_finding_log`;
    - payloads come from the new `AnalyticsEventSummaries` (implemented by `OutlookEventSummaries`);
    - `FindingEvent` sends the summary as the payload.
  - MCP adds `get_series_forecast`, `get_outlook` and `get_index` (read; also in the analyst's read catalog) and
    `fit_index` (agent).
  - Problem codes: `UNKNOWN_INDEX`, `UNKNOWN_OUTLOOK`, `INDEX_INVALID` and `FORECAST_MODEL_NOT_READY`.
  - New app setting: `analysis.forecasts.questionSeries`.
  - `OutlookScheduler`, all UTC: series 03:30, indices 03:45, outlooks Monday 06:00, fits on the 1st at 04:30.
- **Ledger extensions:**
  - `count-threshold` has a numeric mode: no `op`/`threshold`, and the count is the answer. A `referenceFamily`
    selector then counts reference events once the source covers the window; after another 14 days without it the
    question is annulled `COVERAGE_GAP`.
  - `ResolutionData.referenceCount` is a default method; `JdbcResolutionData` implements it.
  - `ClimatologyForecaster` also forecasts numeric reference-count questions.
- **Spec and docs:**
  - The 9 operations are `implemented`, and their lines are gone from `pending-operations.txt`.
  - The spec has reconciliations and a changelog entry.
  - Postman has bodies for a PUT index and the weights change, plus a fit capture. The collection is regenerated.
  - The Operator Guide gains "Outlooks and indices (prompt 126)" and updated cross-references. The Agent Guide gains
    four tool rows and "Forecasts, outlooks and indices". `docs/Invariants.md` gains test names.

## Files created / modified

**Created (main):**
- `outlook/`: `package-info`, `OutlookRecords`, `ForesignalOutlookGenerator`, `OutlookEventSummaries`, `OutlookScheduler`.
- `outlook/series/`: `package-info`, `QuantileSet`, `ForecastMethod`, `SeriesPrep`, `SeasonalNaive`, `EtsModel`,
  `EtsAnn`, `EtsAaa`, `DampedTrend`, `NbGlmMethod`, `MethodSelector`, `SeriesForecastModelEntity`,
  `SeriesForecastModelRepository`, `SeriesForecastEntity`, `SeriesForecastRepository`, `SeriesForecastService`,
  `SeriesForecastViews`.
- `outlook/regional/`: `package-info`, `OutlookFamilies`, `RegionNeighbors`, `RegionalOutlookModel`,
  `RegionalOutlookModelEntity`, `RegionalOutlookModelRepository`, `RegionalOutlookEntity`, `RegionalOutlookRepository`,
  `RegionalOutlookService`, `OutlookViews`.
- `outlook/index/`: `package-info`, `RegionalIndexDefEntity`, `RegionalIndexDefRepository`, `RegionalIndexValueEntity`,
  `RegionalIndexValueRepository`, `RegionalIndexFitEntity`, `RegionalIndexFitRepository`, `IndexInputs`,
  `IndexComputer`, `IndexFitter`, `RegionalIndexService`, `IndexFitService`, `IndexViews`.
- `analytics/math/{NbGlm,NbSampling}`, `analytics/finding/AnalyticsEventSummaries`,
  `baserate/reference/ReferenceCounts`, `api/OutlookController`.
- `mcp/tools/{GetSeriesForecastTool,GetOutlookTool,GetIndexTool,FitIndexTool}`.
- Resources `intel/outlook-families.json` and `intel/regional-indices.json`.

**Modified (main):**
- `analytics/finding/FindingService` (`announce`, flat events in `eventOf`), `analytics/finding/FindingEvent` (three
  event names, flat payloads).
- `analytics/registry/AnalysisSeriesRegistrar` (forecast defaults).
- `forecast/resolve/{ResolverSpec,ResolverEngine,ResolutionData,JdbcResolutionData}` (numeric count mode),
  `forecast/baseline/ClimatologyForecaster` (reference counts).
- `analyst/AnalystRunner` (three read tools), `api/error/ErrorCodes`, `security/AccessPolicy`, `settings/SettingKeys`.
- Resource `intel/originators.json`.

**Build, docs and tooling:** `build.gradle.kts`, `gradle/libs.versions.toml`, `THIRD_PARTY_NOTICES.md`,
`docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `docs/Invariants.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−9).

**Tests:** the new classes below. Modified:
- `stream/sse/EventSchemaConformanceTest`: payload factories for the three events;
- `openapi/PostmanCollectionContractTest`: validators for `putRegionalIndex` and `applyRegionalIndexWeights`;
- `manifest/ManifestReplayCheckIT`: the six new `manifest_id` columns in `COVERAGE`.

## Deviations from the prompt

1. **No 040 adjacency exists.** Neighbors are derived from the 040 boundaries (`ref_country` simplified geometry, and
   `ref_admin1` within one country) with `ST_DWithin` at 0.05°, cached for 24 h. Without loaded reference data a
   region has no neighbors, and the term is left out of its fit.
2. **Outlook counts come from `reference_history`.** That table already holds the 121 imports (ACLED before UCDP) and
   the nightly fold of 116 canonical events (`source=foresignal`), so "reference history plus Foresignal's own
   rollups" is read from one table, one source per region.
   - `weather-severe` maps to the canonical `weather-alert` family, which also contains tsunami warnings.
   - `outage` maps to the canonical `outage` family.
3. **Numeric `count-threshold`.** The prompt asks for a numeric mode that did not exist; it was added to the 122
   resolver. Validation is additive, and a binary question still needs `op` and `threshold`.
   - A `referenceFamily` selector counts events, not the weeks that have events. It waits for source coverage
     instead of requiring 116 detectability: the imported dataset is what the question resolves on.
   - Other selectors in numeric mode do require detectability.
4. **Climatology for outlook questions.** The prompt compares against the 122 `climatology` forecaster, which handled
   only `series-value`, so it was extended to numeric reference counts. `persistence` does not forecast them.
5. **Where the skill numbers come from.** The ledger supplies n, pinball, CRPS, skill against climatology on shared
   questions, and p10–p90 coverage from the resolved windows. Skill against seasonal naive comes from the monthly
   rolling-origin evaluation, because the ledger has no seasonal-naive forecaster.
   - The evaluation scores window 1 only, at each of the last 26 weekly origins (refitting each time), with 200
     paths.
6. **Windows.** The six windows are consecutive, non-overlapping 4-week blocks from the next Monday. `windowEnd` is
   the last day, inclusive.
   - Each weekly outlook opens six new questions, because window starts move by a week.
7. **Built-in components** use the rollup kinds that exist. Left out, because no `{region}` series exists for them:
   - protests (no separate kind);
   - fatalities;
   - GDELT tone and attention (topic series);
   - chokepoint transit change.

   Two adaptations:
   - The 090 "travel-advisory level" is the advisory record count over 90 days, not the level.
   - `maritime-risk` targets `finding:change-point:ais-behaviour`. "Chokepoint disruption change points" is not an
     outlook family, so a target form `finding:{type}:{family}` (findings per region-week) was added.
8. **`autoApplyFittedWeights` is per index.** The spec's `RegionalIndexDef` has the field; there is no global
   operator setting.
9. **Index normalization and regions.**
   - The pool is the class regions' values on the same weekday over the preceding 365 days.
   - The class is the continent from the bundled country table.
   - Regions are those with region-day rollup rows of a component kind in the last 90 days.
   - The first computation backfills 104 Mondays (and the last 8 days), so a fit has history.
10. **Fit details.**
    - The target is the next four weeks after the Monday the components were read.
    - The baseline is the same four weeks a year earlier, as an NB with moment dispersion.
    - `logScore` is the mean negative log predictive probability, and `skillLog = 1 − model/baseline`.
    - Fewer than 30 rows or 10 weeks fails the fit with a reason.
11. **Series frequencies.**
    - Weekly series map horizons to buckets: 1 and 7 days are the next week, 28 days is 4 weeks.
    - Hourly series are forecast as days. Monthly and irregular series answer 409 `FORECAST_MODEL_NOT_READY`.
    - The 1-day target is the current UTC day, so its question closes one hour after the forecast.
12. **`LOCAL_ONLY`.** API reads are local paths in `EgressPolicy`, so the standardized units (and the withheld model
    parameters) apply where egress refuses raw values, for example MCP for such callers. Backtest pinball losses stay
    visible.
13. **Monthly work is scheduled here** because prompt 128 is not built:
    - method re-selection and outlook refits run when older than 30 days;
    - index fits run on the 1st at 04:30.
14. **Who gets `LOW_DETECTABILITY`.** Only outlooks whose counts come from Foresignal's own events are flagged; the
    detectability of imported datasets is not judged by Foresignal's sources. Below-p05 outlook exceedances are
    suppressed while the flag is set.
15. **Wire names kept as published** (US English rule, "anything that crosses a wire"):
    - `normalised` (RegionalIndexValue components);
    - `standardised` (SeriesForecast units);
    - `L2-regularised NB regression, rolling origin` (IndexFitResult method default);
    - the persisted family id `ais-behaviour`;
    - the `licence` column and builder method.
16. **Spec reconciliations** (additive, with a changelog entry):
    - the three SSE events carry ids;
    - the series forecast 404 is `SERIES_NOT_FOUND` (the spec said `UNKNOWN_SERIES`, which the code base never had);
    - `RegionalOutlook` gains `id`, `source` and `skill.backtest`;
    - `RegionalIndexValue.components[].missing`;
    - `SeriesForecastModel.manifestId`;
    - the `ForecastResolver` op/threshold wording;
    - the four Problem codes.
17. **Edited by script, not rewritten by hand.**
    - The spec (166,000 lines) was edited by script, as in every earlier prompt.
    - The Operator Guide (451 KB) and Agent Guide were read end to end by a review subagent for places 126 changes.
      Its 126 findings were applied (retention classes, Discovery's detector sentence, registry defaults, the
      `analytics` event list, the `count-threshold` and forecaster lists, `list_findings`, `get_backtest`,
      `create_question`), and both files were written whole by script.

## Issues found / open questions

**Owner decisions:**
- (a) the numeric `count-threshold` semantics: event counts, and waiting for coverage instead of detectability
  (Deviations 3 and 4);
- (b) question volume. Six questions per region and family per week. With every family and many admin-1 regions this
  could reach thousands of questions a week, each forecast by `statistical` and `climatology`. A cap, or only country
  regions, may be wanted;
- (c) the built-in component choices and the `finding:` target form (Deviation 7);
- (d) per-index `autoApplyFittedWeights` (Deviation 8);
- (e) the schedules held here until 128 (Deviation 13).

**Known limits:**
- An annulled window question leaves its outlook window `open`, because the 122 resolution listener is called on
  resolution only.
- The outlook's neighbor and index terms are held at their last values across the forecast horizon.
- ETS(A,A,A) initial states come from the classical decomposition of the first two seasons; only the smoothing
  parameters are estimated.

**Schema via Hibernate, for prompt 144:** the tables and their indexes:
- `series_forecast_model`;
- `series_forecast` (`series_forecast_ref_idx`, `series_forecast_target_idx`, `series_forecast_check_idx`);
- `regional_outlook_model` (`regional_outlook_model_family_idx`);
- `regional_outlook` (`regional_outlook_region_family_idx`, `regional_outlook_updated_idx`,
  `regional_outlook_latest_idx`);
- `regional_index_def`;
- `regional_index_value` (`regional_index_value_def_region_idx`, `regional_index_value_def_day_idx`, unique
  `regional_index_value_uq`);
- `regional_index_fit` (`regional_index_fit_index_idx`).

New setting: `analysis.forecasts.questionSeries`.

**Guards not run:** the US English guard and `lintOpenApi` were not run (no-tests directive). The new and changed
files were checked by grep for British spellings; the only hits are the wire names and persisted identifiers of
Deviation 15.

**Risks for the owner's first test run:**
- `RegionalOutlookIT` resolves a window through `ResolutionService.runOnce` and then calls `onResolved` directly, so
  it does not depend on after-commit ordering.
- `SeriesForecastIT` needs 420 seeded daily points, so the climatology baseline has five comparable past windows.
- The golden tolerances of the MLE and IRLS recovery tests were set by reasoning, not measured.

No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `outlook/series/SeasonalNaiveTest` | Point forecasts per season slot, zero spread on a perfect season, hand-computed intervals with √⌈h/m⌉, count floor, short history refused |
| `outlook/series/EtsModelTest` | ETS(A,N,N) filter, SSE and flat forecast by hand; the variance formula; one ETS(A,A,A) step, its forecasts and variance by hand; damped sums; MLE recovering α and σ² of a simulated series with nominal interval width; an AAA fit tracking trend and season; counts on log1p |
| `analytics/math/NbGlmTest` | IRLS recovering coefficients and dispersion of a synthetic NB dataset; Poisson data; L2 shrinkage and non-negative columns; CDF, quantile and log-pmf agreement; solver, inverse and Cholesky on a known system |
| `outlook/series/NbGlmMethodTest` | Recovery of the lag, weekend and annual coefficients; bootstrap quantiles monotone, non-negative and deterministic |
| `outlook/series/MethodSelectionTest` | The planted seasonal and trend methods win at 7 days; a future spike and a late revision change nothing (INT-2); steps per horizon; too short a history selects nothing |
| `outlook/series/SeriesForecastIT` | Nightly selection, three monotone horizons with manifests and questions; no duplicates on a rerun; numeric `series-value` questions with `statistical`, then `climatology` and `persistence`; a significant exceedance finding checked once; inside the band nothing; ineligible series skipped |
| `outlook/regional/RegionalOutlookModelTest` | Six monotone, deterministic windows; the fallback; empirical Bayes pooling toward the class mean; the rolling-origin evaluation and the seasonal-naive and climatology quantiles; the eligibility rules; a fit round-tripping through its stored map |
| `outlook/regional/RegionalOutlookIT` | Six windows per eligible region; the admin-1 rule; window questions (numeric `count-threshold`, `outlookId`, category, `statistical`); `outlook.published`; latest-flag replacement; skill and coverage on the outlook and its record payload; the low-density flag; resolution above p95 giving the actual and an `outlook-exceedance` finding; the generator's record ids and emit-on-change; list, cursor, history and 404 |
| `forecast/resolve/NumericCountResolverTest` | Numeric versus binary validation; a covered reference count resolving after the lag; waiting and then `COVERAGE_GAP`; occurrence counts needing detectability; rollup sums |
| `outlook/index/RegionalIndexTest` | Normalization by every transform; weights, direction and component floors with a decomposition summing to the value; a missing component's weight redistributed; floor rules; deltas and backfill days; pools; definition and weights validation (`INDEX_INVALID`); the pure fitter on a planted relationship |
| `outlook/index/IndexFitIT` | A planted relationship recovered from stored Mondays and reference history; weights proposed, not applied, with skill on the fit and the definition and `index.weights_proposed`; auto-apply only with the setting; the weights operation (fitted version, refused version, default); too little history failing with a reason |
| `outlook/OutlookControllersIT` | Series forecast 409 then 200, one horizon, 400 and 404; the model; `LOCAL_ONLY` native on API reads and standardized off them; indices list (built-ins disabled with skill and origin), PUT 403/201/200/400, 404, detail, bad dates; fit 403/409/404; weights 200/400/403; outlooks 404, list, filters, detail with skill and history; MCP scopes and shapes |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **127 (impact):**
  - Register the `impact` forecaster with `ForecasterRegistry.ensure`. `statistical` is registered on first use
    (`SeriesForecastService.ensureForecaster`).
  - Findings and outlook events go through `FindingService.record` and `FindingService.announce`. A new event
    family needs an `AnalyticsEventSummaries` bean.
- **128 (cycles):** take over the monthly work:
  - `IndexFitService.submitMonthly()`;
  - `RegionalOutlookService.refitDue(now)`;
  - re-selection through `SeriesForecastService.select(ref, now)`.

  Then remove `OutlookScheduler.monthlyFits` and the age rules. Assessments can quote `RegionalOutlookService.skill`
  and the index `skill`.
- **Tests:** `foresignal.analytics.scheduled` is false in tests. After the table truncation, call these directly:
  - `SeriesForecastService.runNightly/checkExceedances`;
  - `RegionalOutlookService.publishAll/refitDue/onResolved`;
  - `RegionalIndexService.ensureBuiltIns/computeDaily/computeDays`;
  - `IndexFitService.runFit`;
  - `ForecasterRegistry.registerBuiltIns()`.
- **144 (Flyway):** capture the seven tables and indexes listed above.

Implementation commit: `04b52ef37a495f9fd2538f4a4f0f79e3e43ec31b`
