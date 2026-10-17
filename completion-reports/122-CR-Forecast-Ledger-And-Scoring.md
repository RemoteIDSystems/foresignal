# 122-CR-Forecast-Ledger-And-Scoring

| | |
|---|---|
| Prompt | 122-Forecast-Ledger-And-Scoring.md |
| Status | Complete (tests written, not run; owner decisions under Issues) |
| Implementation commit | `27df057ffbbb324cad21cea58378cf25acbf3a8e` |
| Date | 2026-10-17 |

## Summary

This prompt builds the forecast ledger under `ai.foresignal.forecast`. It compiles. The tests are written but were not run.

- **`EstimativeLanguage`** (`forecast.language`):
  - `term`, `band`, `crossesBand` and `termOrNull` cover the first ICD 203 row only.
  - The scale is symmetric about one half. Below 0.5 a band includes its lower bound and excludes its upper one; above
    0.5 the reverse; the middle band [0.45, 0.55] includes both. So `term(p)` and `term(1 − p)` are always mirror terms.
  - `lint(text, claims)` reports each violation with its offsets:
    - `TERM_MISMATCH`: the term disagrees with a claim whose span contains it, or with a percentage in the same
      sentence;
    - `MIXED_ROWS`: a second-row term in text that also uses the first row;
    - `TERM_WITH_CONFIDENCE`: a likelihood term and a confidence level in the same sentence.
  - Terms match longest first, so "very unlikely" is never also read as "unlikely" or "likely".
  - The 102 `AnalystOutputValidator` (which the 118 brief also uses) now lints the note and every claim, and turns each
    violation into a warning.
- **Data model** (all via Hibernate):
  - `forecast_question` (indexes `(status, resolve_by)`, `(category, status)` and `dedup_key`) holds every field the
    prompt lists, plus `resolver_type`, `annulment`, `ensemble_probability`, `baseline_notes`,
    `resolution_requested_at` and `manifest_id`.
  - `forecaster`.
  - `forecast`: append-only, with every column `updatable = false`. Index `(question_id, forecaster_id, made_at DESC)`.
  - `forecast_score`: unique per question and forecaster, with copies of the category and type for filters, plus the
    observed outcome and hedged counts.
  - `forecast_log`: the SSE cursor.
  - `forecast_calibration`: Platt fit and relative Brier per forecaster and category.
  - The setting `forecast.ensemble` holds the extremizing state.
- **Questions and forecasts** (`ForecastService`):
  - `openQuestion(draft, origin, actor)` is the entry point for later prompts. It dedupes by `dedupKey` and checks the
    selector against the 119 `EventSelectorResolver` (`RESOLVER_INVALID`). It also stores a manifest, emits
    `question.opened` and starts the baselines once the transaction commits.
  - Criteria and resolver become immutable after the first forecast (`QUESTION_IMMUTABLE`). A finished question
    accepts no more changes (`QUESTION_CLOSED`).
  - `record(...)` is the only write path for forecasts, so `made_at` and `asof_seq` are set by the server (LED-1).
    - A new forecast supersedes the forecaster's previous one through `supersedes_id`.
    - If two forecasts land in the same clock tick, the second is stamped 1 ms later to keep them ordered.
    - Every forecast gets a manifest (`forecaster:{type}`).
    - Every non-ensemble forecast refreshes the ensemble in the same transaction.
  - A read token forecasts as `operator:{principal handle}` (falling back to a slug of the token name). An agent may
    name another `operator` or `external` forecaster.
  - Private questions return 404 unless the caller has private access.
- **Resolution** (`forecast.resolve`):
  - `ResolverSpec` validates and versions the six resolvers plus `manual` (1.0.0).
  - `ResolverEngine` is pure, over a `ResolutionData` port. It resolves early for `event-exists` and `alert-fired`, and
    waits for release lags as follows:
    - the series' `releaseLagDays`, or 7 days for `referenceFamily`;
    - for a series period that ends after `resolve_by`, the lag counts from the period's end;
    - after 14 days it resolves anyway, with a `LATE_DATA` caveat.
  - `COVERAGE_GAP` applies when coverage is below 0.8 or a series has more than 20% gaps. It annuls negative and numeric
    answers; a positive answer stands.
  - A negative answer needs 116 `canSeeAbsence`, else `LOW_DETECTABILITY`.
  - `JdbcResolutionData` does the reading:
    - occurrences through the 119 resolver, with region narrowing, or geometry through stored geometries for intel
      events, situations, findings and alerts;
    - series through `SeriesReader` as of the resolution time;
    - situation state from `situation_log`;
    - coverage from `CoverageGate.coverageRatioAsOf`.
  - `ResolutionService.runOnce` runs hourly at :07. It closes due questions, resolves or annuls each question in its
    own transaction, and raises `forecast.resolution_needed` once for manual and `hypothesis-outcome` questions. Each
    resolution pins evidence, stores a manifest, emits `question.resolved` and is scored.
- **Baselines** (`forecast.baseline`):
  - `base-rate`: from 121. The family comes from the `referenceFamily` selector, else the linked precursor's
    consequent, else a `family:` tag. The rate is conditional on the precursor's antecedent when one is linked. It is
    scaled to the window as `1 − (1 − p)^(D/w)`, or a Poisson count for thresholds.
  - `climatology`: same season ±14 days in past years.
  - `persistence`: last value or state, with confidence 0.9.
  - `precursor`: the finding's probability.
  - `analog`: the 121 outcome distribution.
  - `BaselineRunner` runs every baseline on new questions (asynchronously) and every 30 minutes. Each baseline writes
    at most once a day, and only if its value or inputs changed. Skip reasons go to `baselineNotes`.
  - `PrecursorQuestionOpener` (every 5 minutes) opens the `event-exists` question of each `precursor-active` finding.
    It dedupes by precursor and window, and links the question both ways through the new
    `FindingService.linkQuestion`.
- **Scoring** (`forecast.score`):
  - `ForecastScorer` 1.0.0 computes:
    - Brier, multi-category Brier, RPS normalized by K − 1, and log score clipped to [0.01, 0.99];
    - CRPS as twice the mean pinball loss, plus pinball losses and interval hits;
    - time-averaged Brier with carry-forward, where days before the first forecast use the base-rate forecaster
      (documented).
  - `Calibration`: Murphy decomposition (with the within-bin terms the exact identity needs), CORP by
    pool-adjacent-violators with ties pooled, a 10-bin table and sharpness.
  - `Skill`: skill against the out-of-sample `base-rate` or `climatology` forecaster on shared questions, a paired
    bootstrap (1,000 resamples, seeded) and the bias monitor.
  - `Scorecards` builds the reports, with the label `insufficient` under 20 resolved questions. `ScoringService`
    stores the scores, re-scores outdated rows daily and caches scorecards until the next resolution batch.
- **Ensemble** (`forecast.ensemble`):
  - Members are dropped when stale (7 days), and `external` forecasters need 30 resolved questions first.
  - LLM forecasts are clamped to [0.03, 0.97], then Platt-recalibrated (from 50 resolved). Raw, capped and
    recalibrated values are all stored.
  - Pooling uses the weighted mean of log-odds (per label and renormalized for multiple), or a quantile average for
    numeric questions.
  - Weights follow `exp(−2 · relBrier)`, shrunk by n ÷ (n + 20).
  - A new ensemble forecast is written only when its members change or its value moves by at least 0.005.
  - `refit()` runs monthly on the 1st at 04:00, or through `POST /v1/forecasts/ensemble/refit`. It refits relative
    Brier (last 100 per category), Platt (Newton–Raphson, in-house) and the extremizing gate: at least 5 distinct types,
    at least 100 resolutions, an out-of-sample gain (fit on the older half, test on the newer half) and `a > 1`.
- **Retrospective evaluation** (`forecast.eval`):
  - Runs as a 103 job of type `forecast-evaluation`. It replays the forecaster as of each resolved question's
    `opens_at`, writes nothing to the ledger, and scores the results into a `retrospective` scorecard (skill against
    the reference baseline replayed the same way).
  - LLM forecasters need the provider's `knowledgeCutoff` (`config.provider`, else the chat provider), otherwise 409
    `KNOWLEDGE_CUTOFF_REQUIRED`. Questions opened before the cutoff count as `LEAKAGE_RISK`.
  - `RetrospectiveRunner` is the extension point prompt 128 will implement.
- **API, SSE, MCP, chat and notifications**:
  - `api/ForecastController` serves all 17 operations, and `AccessPolicy` has the rows.
  - The SSE topic `forecasts` (`ForecastsTopic`, cursor `q`) carries the four question events and `forecast.submitted`.
    Private events reach only callers with private access.
  - MCP read tools: `list_questions`, `get_question` and `get_scorecard`, also added to the analyst's read catalog. MCP
    agent tools: `create_question` and `submit_forecast`.
  - Chat commands `/questions` and `/forecast <questionId> <probability>` (`ForecastChatCommands`).
  - `forecast.resolved` is channel opt-in. `forecast.resolution_needed` also appears in the inbox (on by default).
  - The seven Problem codes are in `ErrorCodes`.
- **Spec and docs**:
  - All 17 operations are `implemented` and their lines are gone from `pending-operations.txt`. The spec has the
    Problem codes and a changelog entry.
  - Postman is regenerated with a body for each resolver type, each forecast shape, resolve, annul, forecaster update
    and evaluation, plus captures.
  - Operator Guide section "Forecast ledger (prompt 122)", five Agent Guide tool rows, and Invariants test names for
    INT-1 and INT-2.

## Files created / modified

**Created (main):**
- `forecast/`: `package-info`, `ForecastStatus`, `Ledger`, `JsonValues`, `ForecastInputs`, `ForecastQuestionEntity`,
  `ForecastQuestionRepository`, `ForecasterEntity`, `ForecasterRepository`, `ForecastEntity`, `ForecastRepository`,
  `ForecastScoreEntity`, `ForecastScoreRepository`, `ForecastLogEntity`, `ForecastLogRepository`,
  `ForecastCalibrationEntity`, `ForecastCalibrationRepository`, `ForecasterRegistry`, `ForecastService`,
  `ForecastViews`, `ForecastEvents`, `ForecastsTopic`, `ForecastFilter`, `ForecastNotifications`, `LedgerHooks`,
  `LedgerCoordinator` and `LedgerScheduler`.
- `forecast/language/{package-info,EstimativeLanguage}`.
- `forecast/resolve/{package-info,ResolverSpec,ResolutionData,ResolverEngine,JdbcResolutionData,ResolutionService}`.
- `forecast/score/{package-info,ForecastScorer,Calibration,Skill,QuestionScorer,Scorecards,ScoringService}`.
- `forecast/ensemble/{package-info,EnsembleMath,EnsembleService}`.
- `forecast/baseline/{package-info,BaselineForecaster,BaseRateForecaster,ClimatologyForecaster,PersistenceForecaster,PrecursorForecaster,AnalogForecaster,BaselineRunner,PrecursorQuestionOpener}`.
- `forecast/eval/{package-info,RetrospectiveRunner,EvaluationService,EvaluationJobRunner}`.
- `api/ForecastController` and `chatops/ForecastChatCommands`.
- `mcp/tools/{ListQuestionsTool,GetQuestionTool,GetScorecardTool,CreateQuestionTool,SubmitForecastTool}`.

**Modified (main):**
- `baserate/BaseRateService`: new `computeAsOf(definition, asOf)`. The private `compute` takes the end week, and live
  behavior is unchanged.
- `analytics/finding/FindingService`: new `linkQuestion`.
- `analyst/AnalystOutputValidator`: the estimative-language warnings. `analyst/AnalystRunner`: the three read tools.
- `chatops/ChatCommand` and `chatops/CommandRouter`: two commands.
- `notify/ChannelTypeSpec`, `notify/ChannelInputParser` and `notify/inbox/InboxService`: the two events.
- `api/error/ErrorCodes`, `security/AccessPolicy` and `settings/SettingKeys` (`forecast.ensemble`).

**Docs, spec and tooling:**
- `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md` and `docs/Invariants.md`.
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`.
- `src/test/resources/openapi/pending-operations.txt` (−17).

**Tests:**
- New: the classes below.
- Modified:
  - `stream/sse/EventSchemaConformanceTest`: payload factories for the five `forecasts` events.
  - `openapi/PostmanCollectionContractTest`: validator entries for the seven 122 request bodies.

## Deviations from the prompt

1. **`knowledgeCutoff` already existed.** `llm_provider.knowledge_cutoff` and its API field were already built (026,
   checkpoint 041), so nothing was added. An LLM forecaster names its provider in `config.provider`, falling back to
   the configured chat provider.
2. **Spec reconciliations (additive, with a changelog entry):**
   - `submitForecast` gains `x-foresignal-always-authenticated: true`. Its description already required a token, and
     the forecast is recorded under the caller's principal.
   - `resolveForecastQuestion` answers 409 `QUESTION_CLOSED` for machine-resolved questions; annul those instead.
   - `ForecastQuestion` gains `baselineNotes`, where baselines "say why", and `manifestId`.
   - The seven Problem codes are added.
3. **Detectability needs an observability family.** A negative answer can be confirmed only for selectors with a 116
   observability family: `referenceFamily` (its root), change points and unexplained moves, and alert rules (the
   family of their kinds). For intel events, situations, anomalies and explicit timestamps a "no" is annulled
   `LOW_DETECTABILITY`, which follows the prompt's detectability rule strictly. A "yes" always resolves.
4. **Release lags.** Only series carry `releaseLagDays`. Event selectors get 0, except `referenceFamily` at 7 days
   (weekly history and the weekly fold).
5. **Coverage.** "Failing or not covering for more than 20% of the window" is implemented as the 100
   `CoverageGate.coverageRatioAsOf` over the resolver's sources or kinds being below 0.8, or a series' gap share above
   0.2. A positive event answer is kept even when coverage is short.
6. **Base-rate mapping.**
   - Library windows are chosen by question length and scaled to it.
   - `count-threshold` uses a Poisson count.
   - Multiple, ordered and numeric questions, non-count resolvers and geometric areas get no base rate, and say why.
   - The prompt's "binary, multiple" cannot be met for multiple questions: no base rate maps onto arbitrary labels.
7. **Persistence for binary questions** forecasts 0.9 or 0.1 (`binaryConfidence`, configurable), not 1 or 0.
8. **Analogs are not replayed.** The analog forecaster skips retrospective evaluation, because 121's analog search
   reads current signatures and has no `asOf`. Base rates are computed as of the question's ISO week
   (`computeAsOf`). Rows imported later but about earlier weeks still count: imports are historical facts, not new
   knowledge about the future.
9. **Retrospective forecasts are not ledger rows.** They are computed in memory and scored into the job result, so
   the live ledger stays as it was made.
10. **CRPS from quantiles** is defined as 2 × the mean pinball loss over the stored levels (the quantile decomposition
    of CRPS). RPS is normalized by K − 1.
11. **Sharpness** is the variance of the final forecasts. The prompt does not define it.
12. **`forecast.resolution_needed` "default on"** means it is an inbox event. `forecast.resolved` reaches only channels
    that list it. Channels still opt in to both: the 018 default for channel events stays `["alert"]`.
13. **Ensemble settings** (`maxAgeDays`, `eta`, `shrink`, `externalMinResolved`) live in the `ensemble` forecaster's
    `config`, which `PUT /v1/forecasts/forecasters/ensemble` can change. The extremizing state is the setting
    `forecast.ensemble`. The EXT-3 check (30 resolved for external forecasters, owned by prompt 138) is already in place.
14. **Monthly refit.** Prompt 128 is not built, so `LedgerScheduler` refits on the 1st of each month at 04:00 UTC.
15. **No Hipparchus.** Platt and extremizing fits use small in-house Newton solvers, as the prompt allows, so no
    dependency was added.
16. **Lint consumers.** Of the 028/102 consumers, only the 102 validator (and therefore the 118 brief) was wired, with
    warnings. Ask (028) streams free text and is not linted yet.
17. **Manual-question notes are left out of the inbox for private questions,** and private questions send no
    notifications at all (as 119's private findings send none).

## Issues found / open questions

- **Owner decisions:**
  - (a) the strict detectability rule for selectors without an observability family (Deviation 3);
  - (b) persistence's 0.9 confidence (Deviation 7);
  - (c) whether Ask (028) should lint answers now or in 128 (Deviation 16);
  - (d) the monthly refit scheduled here until 128 (Deviation 14);
  - (e) the appended Operator and Agent Guide sections versus "documents are replaced whole" in
    `~/Documents/GitLab/CLAUDE.md` (the same open question as 116–121).
- **Pre-existing test gap for checkpoint 124:**
  - `PostmanCollectionContractTest.validatorTableCoversTheParsedBodies` lists 56 implemented operations from 098–121
    with request bodies but no validator or schema-only entry, for example `createBaseRate`, `createSituation` and
    `createIntelEvent`.
  - The seven 122 operations were added. The rest predate this prompt, so the test will fail at 124 until they are
    covered.
- **New tables via Hibernate:** `forecast_question`, `forecaster`, `forecast`, `forecast_score`, `forecast_log` and
  `forecast_calibration`. Prompt 144 (Flyway) should capture them with their indexes. New setting: `forecast.ensemble`.
- **ITs must register the built-in forecasters.** Built-ins register on `ApplicationReadyEvent`, but the test cleaner
  truncates tables before each method. ITs that touch the ledger therefore call
  `ForecasterRegistry.registerBuiltIns()` in `@BeforeEach`, as the new ITs do.
- **Checks run:**
  - US English guard (`python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`): 14 tests OK on the final tree.
  - `./gradlew lintOpenApi`: BUILD SUCCESSFUL, "Your API description is valid", with the same 70 earlier warnings.
- No upstream was fetched. No file or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `forecast/language/EstimativeLanguageTest` | Every band boundary (17 points) and the inclusive/exclusive rule; mirror symmetry; band changes; non-probabilities refused; claim and percentage mismatches with offsets; longest-first matching; mixed rows; a term with a confidence level in the same sentence versus separate sentences |
| `forecast/resolve/ResolverTest` | Early resolution; waiting; the release-lag wait and `asOf`; `LATE_DATA`; `COVERAGE_GAP` (positive kept); `LOW_DETECTABILITY`; `alert-fired`; count thresholds and the low-count detectability check; rollup gaps; numeric and threshold series aggregation read as of period end plus lag; `situation-status`; manual questions; every validator and period form |
| `forecast/score/ForecastScorerTest` | Brier, multi-category Brier, RPS, log-score clipping, pinball and CRPS against hand-computed values, interval hits, time-averaged Brier with carry-forward and the base-rate fill |
| `forecast/score/CorpReliabilityTest` | PAV on a known sequence; CORP blocks and the decomposition equal to the score (fixed and 20 random samples); the exact Murphy identity; bins and sharpness |
| `forecast/score/SkillAndCompareTest` | Skill on shared questions only against base-rate, with an interval; the comparison's shared-question restriction and bootstrap determinism; the bias monitor; `insufficient` and no skill for the reference itself |
| `forecast/ensemble/EnsembleTest` | Log-odds pooling, caps and extremizing direction; weights and shrinkage; LLM caps; distributions and quantiles; Platt recovering a synthetic overconfidence (a ≈ 0.5); the extremizing fit and every gate condition; relative Brier; the change rule |
| `forecast/ForecastInputsTest` | Question, forecast, resolution, forecaster-update, annulment, patch and evaluation parsing with their Problem codes |
| `forecast/baseline/BaselineForecastersIT` | Base rate: P28D, the ensemble that follows it, the at-most-daily rule, Poisson thresholds, skip notes, the multiple-question note; a precursor finding that opens a deduplicated, linked question forecast at its probability with a conditional base rate; climatology and persistence over a stubbed series (later-known values ignored, nothing without history); persistence of situation state; analogs (outcome mapping, no retrospective run) |
| `forecast/eval/RetrospectiveEvaluationIT` | As-of enforcement: covered, busier later history leaves the score unchanged, and nothing is written to the ledger; LLM `LEAKAGE_RISK` counting and caveat; 409 `KNOWLEDGE_CUTOFF_REQUIRED`, then a 202 job that finishes; 404s |
| `api/ForecastControllersIT` | Dedup 200/201, scopes, `RESOLVER_INVALID`; operator per principal, server-set `madeAt`, supersede-not-edit, the ensemble and its ICD term, forecaster naming rules, history; `QUESTION_IMMUTABLE`, annulment, `QUESTION_CLOSED`, no scores on annulled questions; manual resolution with scores, scorecard, calibration bins, compare and track record, refit; forecaster admin; the resolution job's closing and single notification; `forecasts` topic replay and private filtering; MCP scopes and shapes |

## Compile result

`./gradlew compileJava compileTestJava` gave `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` printed "844 requests across 80 tag folders; quickstart 12 steps; covers all ops: True".

## Notes for the next prompt

- **123 (benchmarks):**
  - The pure scoring pieces can be used directly: `ForecastScorer`, `Calibration`, `Skill`, `Scorecards`,
    `EnsembleMath`, and `ResolverEngine` over a fake `ResolutionData`.
  - A baseline can be replayed with `BaselineForecaster.forecast(q, asOf, true)` without writing anything.
  - LLM ensemble weight gating (`BenchmarkGate`) should plug into `EnsembleService.members`.
- **125 (hypotheses):**
  - Open questions with `ForecastService.openQuestion(draft, "system" | "agent", actor)` using a `hypothesis-outcome`
    resolver.
  - Record `indicator-bayes` forecasts by registering the forecaster with `ForecasterRegistry.ensure(...)`, then
    calling `ForecastService.record(...)`.
  - Confirmation resolves through `resolveManually`.
- **126 and 127:** register the `statistical` and `impact` forecasters the same way, and set `links.seriesRef` /
  `outlookId`.
- **128 (cycles):**
  - Implement `RetrospectiveRunner` for `llm:*` forecasters, recording `knowledgeCutoff` on their forecasts.
  - Call `EnsembleService.refit()` monthly and remove the cron in `LedgerScheduler`.
  - Lint assessments with `EstimativeLanguage.lint` (`AssessmentLintIT`).
- **138:** external forecasters already join the ensemble only after `externalMinResolved` (30) resolved questions.
- **Tests:** `foresignal.analytics.scheduled` is false in tests, so nothing runs in the background. Call these
  directly:
  - `BaselineRunner.runFor/runAll`;
  - `PrecursorQuestionOpener.scan`;
  - `ResolutionService.runOnce`;
  - `EnsembleService.refit/refreshAll`;
  - `ScoringService.rescoreOutdated`.

  Call `ForecasterRegistry.registerBuiltIns()` after the table truncation.
- **144 (Flyway):** capture the six tables above and the setting `forecast.ensemble`.

Implementation commit: `27df057ffbbb324cad21cea58378cf25acbf3a8e`
