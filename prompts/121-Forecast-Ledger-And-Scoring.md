# 121 — Forecast Ledger And Scoring

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/121-CR-Forecast-Ledger-And-Scoring.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/120-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §0, §2 (principles 4, 5, 7), §6.4 (ICD 203 table and lint rules), §8 (the whole ledger section), §14.3–§14.4
- Completion Reports of 014/037 (`asOf` reads), 017 (alerts), 026 (LLM providers: add `knowledgeCutoff`), 061 (chat commands), 101 (intel events), 103 (job framework), 117 (situations), 118 (findings, series reader, statistics package), 119 (`precursor-active`), 120 (base rates, analogs)

## Objective

Build the **forecast ledger**: resolvable questions, immutable timestamped forecasts from many forecasters, automatic resolution from Foresignal's own data, proper scoring, calibration and bias monitoring, a track-record-weighted ensemble with recalibration, and leak-free retrospective evaluation. Add the baseline forecasters (base rate, climatology, persistence, precursor, analog), the operator as a forecaster, and the `EstimativeLanguage` utility every component uses to turn numbers into ICD 203 words.

## Principles (binding)

- **Forecasts are append-only.** A forecast is never edited; a new one supersedes it. `made_at` and `asof_seq` are set by the server.
- **Resolution is mechanical where possible.** Criteria text and a machine resolver are both stored and shown to every forecaster verbatim.
- **Scores are fair.** Forecasters are compared only on shared questions, against out-of-sample references, with intervals; small samples are labelled.
- **Numbers are the truth.** Terms are derived and linted.

## Tasks

### 1. `EstimativeLanguage` (`ai.foresignal.forecast.language`)

- `term(p)` → the ICD 203 term (one row only: almost no chance, very unlikely, unlikely, roughly even chance, likely, very likely, almost certain); `band(p)`; `crossesBand(p0, p1)`.
- `lint(text, claims)`: rejects (a) a likelihood term whose band disagrees with the claim's number, (b) terms from the second ICD row (remote, highly improbable, improbable, roughly even odds, probable, highly probable, nearly certain) mixed with the first, (c) a likelihood term and a confidence level ("low/moderate/high confidence") in the same sentence. Returns violations with offsets. Used by 122, 125 and the 028/102 validators for intelligence outputs.

### 2. Data model (`ai.foresignal.forecast`)

- `forecast_question`: `id` (ULID), `title`, `type` (binary | multiple | ordered | numeric), `outcomes` jsonb (multiple/ordered labels; numeric `unit`, `lower`, `upper`), `resolution_criteria` (text, ≤ 2,000 chars), `resolver` jsonb (§3) or `{type: manual}`, `opens_at`, `closes_at`, `resolve_by`, `status` (open | closed | resolved | annulled), `resolution` jsonb (`{outcome | value, resolvedAt, resolvedBy, evidence[], note}`), `category` (e.g. `conflict`, `maritime`, `markets`, `weather-impact`, `activity`), `tags`, links (`situation_id`, `hypothesis_set_id`, `precursor_id`, `finding_id`, `series_ref`, `outlook_id`), `origin` (operator | agent | system | analyst), `dedup_key`, `private`, `version`, timestamps. Indexes `(status, resolve_by)`, `(category, status)`.
- `forecaster`: `id` (slug), `type` (`base-rate | climatology | persistence | statistical | precursor | analog | impact | indicator-bayes | llm | operator | external | ensemble`), `name`, `config` jsonb, `version`, `active`, `created_at`.
- `forecast`: `id`, `question_id`, `forecaster_id`, `made_at`, `asof_seq`, `probability` (binary), `distribution` jsonb (multiple/ordered: outcome → p, summing to 1 ± 1e-6), `quantiles` jsonb (numeric: p05, p10, p25, p50, p75, p90, p95, monotone), `rationale` (markdown; LLM forecasters store typed claims), `citations` jsonb, `inputs` jsonb (base-rate ids, analog ids, precursor id, model, provider, effort, samples, prompt hash), `supersedes_id`, `recalibrated` (bool, for derived ensemble inputs). Indexes `(question_id, forecaster_id, made_at DESC)`.
- `forecast_score`: per resolved question and forecaster: `brier`, `log_score`, `rps` (ordered), `crps` (numeric), `pinball` jsonb, `time_averaged_brier`, `n_days`, `final_probability`, computed by `ForecastScorer` version; recomputed only when the scorer version changes.

### 3. Resolvers and resolution

- Machine resolvers (pure, versioned, each with a validator):
  - `event-exists {selector (103 selector types), area (bbox | polygon | placeId | regionCodes), window}`;
  - `count-threshold {selector, area, window, op, threshold}` (records or region rollups);
  - `series-value {ref, period | window, aggregate: last|mean|max|sum, op?, threshold?}` (numeric when no threshold);
  - `alert-fired {ruleId, window, area?}`;
  - `situation-status {situationId, at: resolve_by, status | minSeverity}`;
  - `hypothesis-outcome {setId}` (resolved by the operator's confirmation in 122).
- `ResolutionJob` hourly (and on demand): resolves due questions; resolves **early** when an `event-exists` or `alert-fired` question's event occurs before `resolve_by`; uses `asOf` = `resolve_by + lag` where `lag` is the involved sources' `releaseLagDays` (so late-published data is waited for, up to 14 days, then resolves with what exists and a caveat).
- Coverage: if the resolver's sources were failing or not covering for more than 20% of the window, the question becomes `annulled` with reason `COVERAGE_GAP` (not scored).
- Manual questions: at `resolve_by`, raise a notification (`forecast.resolution_needed`) and list them at `GET /v1/forecasts/questions?status=closed&resolver=manual`; `POST /v1/forecasts/questions/{id}/resolve` (agent) resolves with evidence.
- Every resolution writes evidence refs (pinned records or rollup rows) and emits `forecast.resolved`.

### 4. Baseline forecasters

All run automatically on every new open question of a compatible type and re-forecast when their inputs change (at most daily):
- `base-rate` (binary, multiple): from 120 (`p_at_least_one` for the question's family, area and window, or conditional when a precursor or finding links one). Outputs nothing (and says why) when no base rate applies.
- `climatology` (numeric): empirical quantiles of the same series and window over history (seasonal window ± 14 days, all years).
- `persistence` (numeric; binary for "will the current state still hold"): the last known value or state.
- `precursor` (binary): for questions opened from `precursor-active` findings, the finding's shrunk probability.
- `analog` (binary): 120's analog outcome distribution when the question links a situation or region.
- `operator`: one forecaster per token principal that submits forecasts (`operator:{principal}`).
- `ensemble` (§6).

Auto-created questions:
- From `precursor-active` findings: "Will {consequent} occur in {region} between {from} and {to}?" (`event-exists`), origin `system`, dedup by precursor and window.
- Other components (122 hypothesis sets, 123 outlooks and series, 124 impacts) create their own questions through `ForecastService.openQuestion(...)`.

### 5. Scoring, calibration, bias (`ForecastScorer` 1.0.0)

- **Brier:** binary `(p − o)²`; multiple `Σₖ (pₖ − oₖ)²` (0–2). **RPS** for ordered. **Log score** `−ln p(outcome)` with p clipped to [0.01, 0.99]. **CRPS** for numeric from quantiles via mean pinball loss over the stored levels; interval coverage for p05–p95 and p25–p75.
- **Time-averaged Brier:** each day the question is open, score the forecaster's standing forecast; carry forward until superseded; days before its first forecast use the `base-rate` forecaster's probability (Good Judgment convention; document it).
- **Murphy decomposition** (reliability, resolution, uncertainty) per forecaster and category, with within-bin terms stored separately.
- **CORP reliability:** isotonic regression of outcomes on forecasts by pool-adjacent-violators, giving the recalibrated curve and the CORP decomposition (miscalibration, discrimination, uncertainty); a 10-bin table with counts as a fallback when n < 50.
- **Skill:** `1 − S / S_ref` with `S_ref` the out-of-sample `base-rate` (binary/multiple) or `climatology` (numeric) on the same questions; pairwise comparison on shared questions with a paired bootstrap 95% interval (1,000 resamples, deterministic seed).
- **Bias monitor:** per forecaster and category, mean forecast − observed frequency, share of forecasts in [0.4, 0.6], and mean forecast on questions that resolved "no".
- Labels: `insufficient` when fewer than 20 resolved questions.
- Scorecards cached and recomputed after each resolution batch.

### 6. Ensemble and recalibration

- `ensemble` forecaster: on each question, combine the latest forecasts of active forecasters (excluding stale ones older than `ensemble.maxAge`, default 7 days, unless the question is younger) by weighted mean of log-odds (binary; per outcome then renormalised for multiple) or weighted quantile averaging (numeric).
- Weights `w ∝ exp(−η · relBrier)` (η = 2) using each forecaster's relative Brier against the median over the last 100 resolved questions in the category, shrunk toward equal weights by `n ÷ (n + 20)`.
- **Platt recalibration:** per forecaster and category, once ≥ 50 resolved, fit `p' = σ(a · logit p + b)` (Hipparchus or a small Newton solver) on resolved forecasts; the ensemble uses recalibrated inputs and stores both.
- **LLM caps:** forecasts from `llm*` forecasters are clamped to [0.03, 0.97] before recalibration (raw values kept in `inputs`).
- **Extremizing:** `p* = σ(a · Σ wᵢ logit pᵢ)`, default `a = 1`; a monthly check (`POST /v1/forecasts/ensemble/refit`, or 125's monthly cycle) fits `a` on resolved questions and enables it only when ≥ 5 forecasters with distinct `type` are active and out-of-sample log score improves over ≥ 100 resolutions. Output capped to [0.01, 0.99].

### 7. Retrospective evaluation

- `POST /v1/forecasts/evaluations {forecasterId, from, to, categories?, maxQuestions?}` (agent) → 103 job `type=forecast-evaluation`: for each **resolved** question that was open in the range, run the forecaster as of the question's `opens_at` (tools and readers forced to `asOf`), score against the actual resolution, and produce a scorecard marked `retrospective`.
- Statistical and baseline forecasters are fully valid.
- LLM forecasters (registered by 125): add `knowledgeCutoff` (date) to `llm_provider` model config (026); questions with `opens_at` before it are skipped and counted under `LEAKAGE_RISK`. Without a configured cutoff, LLM retrospective evaluation is refused (`409 KNOWLEDGE_CUTOFF_REQUIRED`).

### 8. API, SSE, MCP, notifications

- Questions: `GET /v1/forecasts/questions?status&category&linked&dueBefore&cursor` (read), `GET /v1/forecasts/questions/{id}` (read; criteria, resolver, forecasts by forecaster with the latest first, ensemble, ICD term, scores when resolved), `POST /v1/forecasts/questions` (agent), `PATCH /v1/forecasts/questions/{id}` (agent; title, tags, `closes_at`; criteria and resolver are immutable once a forecast exists), `POST /v1/forecasts/questions/{id}/annul` (agent, reason), `POST /v1/forecasts/questions/{id}/resolve` (agent; manual).
- Forecasts: `POST /v1/forecasts/questions/{id}/forecasts` (read scope submits as `operator:{principal}`; agent scope may name another registered `operator` or `external` forecaster), `GET /v1/forecasts/questions/{id}/forecasts?forecaster` (read).
- Forecasters and scores: `GET /v1/forecasts/forecasters` (read), `PUT /v1/forecasts/forecasters/{id}` (admin: active, config), `GET /v1/forecasts/scorecard?forecaster&category&from&to` (read), `GET /v1/forecasts/calibration?forecaster&category` (read; CORP curve points and bins), `GET /v1/forecasts/compare?a&b&category` (read; shared questions, paired bootstrap).
- Evaluations: `POST /v1/forecasts/evaluations`, `GET /v1/forecasts/evaluations/{id}` (agent).
- SSE topic `forecasts`: `question.opened|closed|resolved|annulled`, `forecast.submitted` (summary only).
- MCP: `list_questions`, `get_question`, `get_scorecard` (read; read catalogue); `create_question`, `submit_forecast` (agent).
- Chat commands (061): `/forecast <questionId> <probability>` and `/questions` for the operator, when 061 exists.
- Notifications: `forecast.resolved` (default off), `forecast.resolution_needed` (default on for manual questions).
- Problem codes: `UNKNOWN_QUESTION`, `UNKNOWN_FORECASTER` (404); `QUESTION_CLOSED`, `QUESTION_IMMUTABLE`, `KNOWLEDGE_CUTOFF_REQUIRED` (409); `FORECAST_INVALID`, `RESOLVER_INVALID` (400).

### 9. Spec and docs

- `docs/OpenAPI.json`: all schemas above; tag `Forecasts`; `x-foresignal-scope` everywhere; SSE schemas; Postman BODIES for create question (each resolver type), submit forecast (binary, multiple, numeric), resolve, evaluation.
- AccessPolicy rows as listed.
- `docs/Operator-Guide.md` "Forecast ledger": writing resolvable questions, reading Brier, log score, CRPS, skill and calibration plots, why LLM forecasts are capped and recalibrated, leakage, and "operator vs machine".
- `docs/Agent-Guide.md`: always submit probabilities with base-rate reasoning; quote criteria verbatim.

## Tests to write (do not run)

- `EstimativeLanguageTest`: every band boundary (inclusive/exclusive rules documented), mixed-row detection, term+confidence in one sentence, number-term disagreement.
- `ResolverTest` per type: early resolution, release-lag wait, coverage annulment, numeric aggregation.
- `ForecastScorerTest`: Brier, multi-category Brier, RPS, log score clipping, CRPS from quantiles against reference values; time-averaged Brier with carry-forward and the base-rate fill.
- `CorpReliabilityTest`: PAV on a known sequence; decomposition sums to the score.
- `SkillAndCompareTest`: out-of-sample reference; shared-question restriction; bootstrap interval determinism.
- `EnsembleTest`: log-odds weighting, shrinkage, stale exclusion, LLM caps, Platt fit on synthetic miscalibrated forecasts, extremizing gate.
- `BaselineForecastersIT`: base-rate, climatology, persistence, precursor and analog forecasts on seeded questions; nothing emitted when inputs are missing.
- `RetrospectiveEvaluationIT`: as-of enforcement (a later record changes nothing); LLM cutoff skips and `LEAKAGE_RISK` counts; 409 without a cutoff.
- `ForecastControllersIT`: immutability rules, operator forecaster per principal, scopes, SSE topic, MCP shapes.

## Acceptance criteria

- Questions resolve automatically from Foresignal's data, early when possible, after release lags, and annul on coverage gaps.
- Every forecaster's forecasts are immutable, timestamped with their knowledge cutoff, and scored with Brier/log/RPS/CRPS, time-averaged, with calibration, skill and bias reports.
- The ensemble weights by track record, recalibrates, caps LLM extremes and only extremizes when justified.
- Retrospective evaluation is leak-free for statistical forecasters and refuses leakage-prone LLM questions.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `121: Forecast Ledger And Scoring`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/121-CR-Forecast-Ledger-And-Scoring.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `121: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
