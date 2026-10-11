# 127 — Analysis Cycles And Assessments

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/127-CR-Analysis-Cycles-And-Assessments.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/126-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2, §6.4 (wording rules), §8 (ledger, LLM forecasters, caps, leakage), §11 (cycles, roles, assessments, cadences, intel board), §13 (worked example), §14.3 (what works in LLM forecasting)
- `docs/Architecture.md` §19 (LLM subsystem: effort, caching, refusal fallback, budgets), §21–§22 (Ask loop, briefings)
- Completion Reports of 116 (evidence, observability, manifests), 123 (benchmark status and gating), 018 (event types), 026 (providers, `knowledgeCutoff` from 122), 027 (embedding), 028 (tool loop, citation verifier), 029 (scheduler, renderers), 033 (inbox), 038 (glance pattern), 102 (analyst profiles, runner, validator, proposals, evaluation), 118–126

## Objective

Give Foresignal an analytic rhythm. **Analysis cycles** run daily, weekly and monthly. Each run builds a deterministic packet of what changed since the previous run of the same cycle. With an LLM configured, it then runs four budgeted roles: **analyst** (key judgments), **red team** (the case against), **forecaster** (scored probabilities for open questions) and **proposer** (new sets, indicators, watches, questions). It always publishes an **assessment** that builds on the previous one and follows the ICD 203 standards. Monthly runs also trigger the maintenance jobs (base-rate refresh, precursor re-test, ensemble refits, index weight proposals). The **intel board** gives clients a small summary of everything.

## Principles (binding)

- **Deterministic first.** The packet and a digest-only assessment are always produced, with or without an LLM.
- **Every LLM claim is typed and cited**, validated by the 102 validator plus `EstimativeLanguage.lint` (122). Failing text is regenerated once, then downgraded or dropped with a warning.
- **LLM forecasts are just forecasts.** They go to the ledger under their own forecaster ids, capped and recalibrated by 122, and never change hypothesis probabilities or other forecasters' numbers.
- **Change is explained.** Each assessment links the previous one of its cadence and states what moved and why (ICD 203 standard 7).
- **Budgets bound everything** (102 profile limits and 026 provider budgets).

## Tasks

### 1. Cycles (`ai.foresignal.cycle`)

- `analysis_cycle`: `id`, `name`, `cadence` (daily | weekly | monthly), `schedule` (5-field cron + IANA tz), `scope` jsonb (regions, themes, situation ids, hypothesis set ids or `cadence-match`, sources, categories), `analyst_profile_id` (102; model, effort, budgets, `allow_actions`, owner token), `roles` (subset of analyst, red-team, forecaster, proposer), `forecaster_providers` (provider ids to run the forecaster role with; default the profile's provider), `samples` (forecaster samples per question, default 3, max 7), `max_questions` (default 15 daily, 40 weekly, 60 monthly), `channel_ids`, `notify_min_priority`, `enabled`, `next_run_at`, `last_run_id`.
- Defaults created on first start, **disabled**: "Daily brief" (06:00, operator timezone from 018's `server` setting), "Weekly assessment" (Monday 07:00), "Monthly outlook" (1st, 08:00).
- Scheduling: the 029/102 pattern (30-s check, persisted `next_run_at`, missed runs run once, at most one run per cycle and two globally).

### 2. Deterministic packet (`CyclePacketBuilder`, `cycle-packet` 1.0.0)

From the previous successful run of the same cycle (or the cadence's natural lookback for the first run), within scope:
- **Situations:** new, activated, escalated, dormant, closed; snapshot diffs (118).
- **Hypotheses:** revisions with drivers, band changes, indicators observed/absent/unknown-coverage, broken or questioned assumptions, sets due for review (`next_review_at` ≤ now and `cadence` matching).
- **Findings:** by type and severity, with base rates and caveats (119, 120, 125, 126).
- **Forecasts:** questions opened, due within the horizon (7 d daily, 30 d weekly, 90 d monthly), resolved (with each forecaster's score), and scorecard changes (122).
- **Outlooks and indices:** changes ≥ 5 index points; outlook windows resolving outside p10–p90 (125).
- **Coverage:** in-scope sources that were failing or degraded, `LOW_REPORTING_DENSITY` regions.
- Size-capped (the 102 cap of 30 ranked items per section for the LLM; complete in storage) and stored on the run.

### 3. Roles (LLM, optional)

Each role is a 028 tool loop through the 102 runner machinery with the read catalogue (now including the intelligence tools from 118–126), the packet as delimited input, the profile's limits, prompt caching of the system prompt and tools, refusal fallback and `stop_reason` checks (Architecture §19). Effort: daily `medium`, weekly and monthly `high` unless the profile caps it.

- **Analyst:** 3–7 key judgments `{text, probability?, confidenceStatement, citations, hypothesisSetId?, questionId?}`. Probability-bearing judgments must reference a ledger question or hypothesis set; the term is derived from the number by `EstimativeLanguage`, and the confidence statement is a separate sentence. Explains change against the previous assessment's judgments (which are in the packet).
- **Red team:** for up to 3 active hypothesis sets with the most movement or the highest stakes (severity of the linked situation): the strongest case for the runner-up hypothesis, disconfirming evidence searched with tools, assumptions most likely to be wrong, and a premortem ("assume the leading hypothesis proved wrong; why?"). Where a set has a ledger question, it submits a forecast as `llm-redteam:{profile}`.
- **Forecaster:** for up to `max_questions` open questions in scope, prioritised by **value of information**: `stakes × uncertainty`, where stakes come from the linked situation's severity (critical 3, warning 2, info 1, none 1) and uncertainty is `4 · p · (1 − p)` of the current ensemble (1 for questions with no forecast); ties broken by due date, then least-recently forecast by this forecaster. The cycle also enforces `max_tokens_per_cycle` (default 300k daily, 1.2M weekly, 2M monthly) across all roles:
  1. Quote the resolution criteria verbatim (from the question).
  2. **Outside view:** call `get_base_rate` and `find_analogs`; state the reference class and its rate.
  3. **Inside view:** adjustments with citations to current evidence, and the hypothesis set's state when linked.
  4. Output a probability (or distribution or quantiles).
  - Run `samples` independent times (temperature where the provider supports it; otherwise varied tool-call ordering seeds), aggregate by median log-odds (median per quantile for numeric), and submit to 122 as forecaster `llm:{profile}:{provider}` with the rationale (typed claims) and `inputs` (samples, model, effort, prompt hash). One forecaster per configured provider, so the ensemble can mix models.
- **Collection gaps:** the packet includes 124 collection gaps for sets in scope; the analyst may comment, and the assessment lists them under "What we cannot see".
- **Proposer:** proposals through the 102 pipeline (pending unless `allow_actions` and the type is allowed): `propose_hypothesis_set` (validated as a 124 draft), `propose_indicator`, `propose_watch`, `propose_question` (validated by 122), `propose_situation_merge`. New proposal types are added to the 102 `allowed_actions` enum; nothing deletes or disables anything.

Budget refusal or failure in a role marks that role `skipped` with a reason; the assessment is still written.

### 4. Assessments (`assessment`)

- Columns: `id`, `cycle_id`, `cadence`, `period_start`, `period_end`, `previous_assessment_id`, `status` (ok | partial | digest-only | failed), `bluf` jsonb (key judgments), `sections` jsonb, `markdown`, `html`, `packet_ref`, `roles` jsonb (per role: status, model, tokens, warnings), `citations`, `lint_warnings`, `private` (any private input), `created_at`.
- **Structure** (rendered by the 029 renderer; a deterministic template fills every section, and LLM text replaces or augments the narrative parts):
  1. **Bottom line up front:** key judgments (LLM) or the top deterministic changes (digest-only).
  2. **What changed** since the previous assessment of this cadence (link; deterministic diff of judgments, hypothesis probabilities and situations).
  3. **Situations:** status and trajectory.
  4. **Hypotheses:** table of sets with probability, term, change since last assessment, top drivers; red-team notes.
  5. **Indicators to watch** next period (pending indicators with `expected_by` in the next period, plus proposals).
  6. **Forecasts:** opened, due, resolved; the scorecard summary (weekly and monthly: calibration headline per forecaster, operator vs machine).
  7. **Speculation:** separately, including speculative-link hypotheses with alternatives and coincidence baselines.
  8. **Sources, coverage and caveats.**
  - Footer: "Automated analysis. Probabilities use ICD 203 terms; evidence is cited; speculation is labelled. Verify before acting."
- **Export:** `GET /v1/assessments/{id}/bundle?format=json|geoparquet` (read) returns the assessment's cited objects (findings, base rates, forecasts with scores, hypothesis revisions) with provenance and manifests, using 111's exporters for GeoParquet.
- **Delivery:** channels as event type `assessment.published` (≥ `notify_min_priority`; daily `normal`, weekly and monthly `high`), the 033 inbox, embedding (027), SSE topic `assessments`.

### 5. Monthly maintenance

The monthly cycle (or `POST /v1/analysis/maintenance/run`, agent) triggers, in order, recording each outcome in the run: 116 observability re-estimation; the 123 historical benchmark suite; 121 base-rate refresh; 120 precursor re-test; 122 Platt refits, ensemble weight refresh and extremizing check; 125 index weight fits (proposals) and series-method re-selection; archival proposals for hypothesis sets past `horizon`; and, when the profile's provider has a `knowledgeCutoff`, a 122 retrospective evaluation of `llm:*` forecasters over the past month's resolved questions.

### 5a. On-demand situation assessments

- `POST /v1/situations/{id}/assessment {roles?, effort?}` (agent, `alwaysAuthenticated`, 202): runs the cycle machinery scoped to one situation (its members, linked hypothesis sets, questions, findings, coverage) and stores an `assessment` with `cadence = on-demand` and `situation_id`, structured with the same sections (bottom line, key insights, what changed since the previous assessment of this situation, outlook, risks, speculation, sources). `GET /v1/situations/{id}/assessments` lists them. Each assessment exposes its model, effort, manifest and prompt template ids (116), so a client can show "view prompt" and "re-run".

### 6. Intel board

- `GET /v1/intel/board` (read; ≤ 8 KB; ETag; `Cache-Control: max-age=60`): top 5 situations by severity and recency; hypothesis band changes in the last 24 h; indicators observed in the last 24 h; forecasts due in the next 7 days (title, ensemble probability, term); the latest assessment id, cadence and BLUF headline for each cadence; the ensemble's Brier skill and calibration headline over the last 90 days, **next to the operator's** (operator vs machine, when the operator has ≥ 20 resolved forecasts); the latest benchmark status per component (123). Respects `private` and LOCAL_ONLY.
- MCP `get_intel_board` (read).

### 7. API, MCP, SSE

- Cycles: `GET /v1/analysis/cycles` (read), `GET/PUT/DELETE /v1/analysis/cycles/{id}`, `POST /v1/analysis/cycles` (agent), `POST /v1/analysis/cycles/{id}/run` (agent, `alwaysAuthenticated`, 202), `GET /v1/analysis/cycles/{id}/runs?cursor` (agent), `GET /v1/analysis/runs/{runId}` (agent; packet, roles, warnings).
- Assessments: `GET /v1/assessments?cadence&since&cursor` (read), `GET /v1/assessments/{id}?format=json|md|html` (read), `GET /v1/assessments/latest?cadence` (read), `POST /v1/assessments/{id}/feedback` (read; useful | not_useful | wrong, into 102's feedback).
- MCP: `get_assessment`, `list_assessments` (read; read catalogue); `run_cycle` (agent).
- SSE topic `assessments`: `assessment.published`, `cycle.run_failed`.
- Problem codes: `UNKNOWN_CYCLE`, `UNKNOWN_ASSESSMENT` (404); `CYCLE_RUN_IN_PROGRESS`, `LLM_NOT_CONFIGURED` (409 when roles require it; digest-only runs never need it).

### 8. Spec and docs

- `docs/OpenAPI.json`: `AnalysisCycle`, `AnalysisCycleInput`, `CycleRun`, `CyclePacket`, `Assessment`, `KeyJudgment`, `IntelBoard`; tag `Assessments`; `x-foresignal-scope`; SSE schemas; Postman BODIES for creating a weekly cycle and running it.
- AccessPolicy rows as listed.
- `docs/Operator-Guide.md` "Analysis cycles": enabling the defaults, choosing models and effort, cost per cycle (estimate from tokens), what each role does, reading an assessment, how the ledger keeps the LLM honest, and the monthly maintenance list.
- `docs/Agent-Guide.md`: read the latest assessment and the intel board before answering situation questions; cite assessments by id.

## Tests to write (do not run)

- `CyclePacketBuilderTest`: each section from seeded state; first-run lookback; caps for the LLM view, full packet stored.
- `CycleRunIT` (fake `LlmClient`): digest-only without a provider; all four roles with a scripted provider; forecaster runs outside view then inside view, aggregates samples by median log-odds and submits one forecast per provider forecaster; red-team forecast submitted under its own id; proposals pending without `allow_actions`; a role budget refusal → `skipped` and the assessment still written.
- `AssessmentLintIT`: a judgment mixing a likelihood term and a confidence level is regenerated once, then downgraded with a warning; numbers and terms agree in the final text.
- `AssessmentDiffTest`: "what changed" between two assessments (judgments, probabilities, situations).
- `MonthlyMaintenanceIT`: jobs triggered in order; failures recorded without stopping later steps; retrospective evaluation only with a cutoff.
- `IntelBoardTest`: size ≤ 8 KB with large inputs (truncation order documented), ETag stability, private exclusion.
- `CycleSchedulerIT`: missed runs once; one concurrent run per cycle.
- `CycleControllersIT`: endpoints, scopes, `alwaysAuthenticated`, formats, feedback, MCP shapes.

## Acceptance criteria

- Daily, weekly and monthly cycles publish assessments that build on their predecessors, with or without an LLM.
- LLM roles are budgeted, cited, linted, and their forecasts are scored in the ledger under their own ids.
- Monthly maintenance keeps base rates, precursors, ensemble weights, index weights and series methods current.
- The intel board summarises the intelligence layer in ≤ 8 KB.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `127: Analysis Cycles And Assessments`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/127-CR-Analysis-Cycles-And-Assessments.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `127: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
