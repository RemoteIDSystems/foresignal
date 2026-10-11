# 102 — Autonomous Analyst

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/102-CR-Autonomous-Analyst.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/101-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §19 (LLM subsystem: budgets, safety, proposed actions, effort per use), §21 (Ask tool loop, verified citations, `allowActions`), §22 (briefings scheduler pattern)
- `docs/Product-Charter.md` G10, G11, §8.11–§8.15
- Completion Reports of 023 (scopes, restricted tokens, `alwaysAuthenticated`, audit log), 026 (providers, roles, `llm_usage`, `LLM_BUDGET_EXCEEDED`), 027 (search), 028 (tool loop, citation verifier, `ask_action`), 029 (cron and timezone scheduling), 033 (notification inbox), 039 (private egress), 098, 100 (anomalies), 101 (intelligence events, investigations)

## Objective

Add a scheduled LLM analyst. On each run it looks at what is new: intelligence events (101), anomalies (100) and alerts (017). It compares them with history and writes **analyst notes**. It may also **propose** watches, intelligence events or investigation items. The analyst explains evidence that deterministic detectors already produced; it never manufactures evidence. Its proposals execute only when the profile has `allowActions=true` **and** its owner token still holds `agent` scope. Every run is budgeted and logged for evaluation.

## Analytic principles (binding)

1. **Evidence comes from detectors.** Every factual claim must cite an item returned by a tool in this run: record@seq, alert, anomaly or event.
2. **Claims are typed.** Each claim is `evidence` (directly stated by a cited item), `inference` (reasoned from cited items) or `speculation` (a possibility to check). Untyped or uncited claims are downgraded to speculation or dropped.
3. **No confidence inflation.** A proposed intelligence event gets its confidence from the 101 `EventScorer` over its members. Any LLM-supplied number is discarded.
4. **Coverage-aware.** The system prompt instructs, and the validator checks keywords, that the analyst must not assert something "stopped" or "is absent" unless a cited 100 result had `CoverageGate` ok.
5. **Untrusted content.** Record text is data. Tool results are delimited, and instructions inside them are ignored (Architecture §19).

> **v1.3 note:** prompt 127 reuses analyst profiles, `AnalystRunner` machinery, `AnalystOutputValidator` and the proposal pipeline for the analysis-cycle roles (analyst, red team, forecaster, proposer), and adds proposal types. Keep these components reusable by other callers, and keep `allowed_actions` an extensible enum.

## Tasks

### 1. Profiles (`ai.foresignal.analyst`)

- `analyst_profile` columns:
  - identity and timing: id, name, enabled, `schedule` (5-field cron + IANA tz, or `every` duration ≥ 10m; default every 30m), `scope` jsonb (sources, kinds, places, bbox, event types, rule tags, `minSeverity`), `lookback` (default: since the last run, capped at 7d; history tools may reach back 90d), `instructions` (≤ 4,000 chars);
  - model: `provider_id` (null = chat role), `effort` (default `low`; `medium` allowed);
  - budgets: `max_runs_per_day` (48), `max_tokens_per_run` (60k), `max_tool_calls` (16), `max_wall_seconds` (180);
  - actions: `allow_actions` (default false), `allowed_actions` (subset of `propose_watch`, `propose_intel_event`, `propose_investigation_item`, `annotate_event`), `max_auto_actions_per_day` (5);
  - delivery: `channel_ids`, `notify_min_priority` (normal);
  - ownership: `owner_token_id`, created_at, updated_at.
- Validation:
  - setting `allow_actions=true` requires the caller to hold `agent` scope;
  - restricted tokens (023) cannot own an action-enabled profile;
  - enabling without a chat provider → 409 `LLM_NOT_CONFIGURED`.
- With no LLM configured, the feature is inert. Every non-LLM feature is unaffected (G11).

### 2. Run pipeline (`AnalystRunner`)

1. **Scheduling** follows the 029 `BriefingScheduler` pattern (30-s check, `next_run_at` persisted, missed runs run once, not replayed). Each run executes on a virtual thread, with at most one concurrent run per profile and 2 globally.
2. **Change detection (no LLM).**
   - Read the profile cursor `{seq, alertId, eventUpdatedAt, anomalyId}`.
   - Collect in-scope inputs that are new or updated since then: intelligence events, `anomaly_observation` rows with classification `activity-up|activity-down`, and alerts (≥ `minSeverity`). Exclude inputs whose `origin=analyst` unless deterministic evidence updated them since (loop guard).
   - If there is nothing new, record a `noop` run and make no LLM call.
3. **Input packet.** Rank by severity, then confidence, then recency, and cap at 30 items. Include the compact summaries (evidence sections only) and coverage/health status for involved sources.
4. **Tool loop.** Reuse the 028 tool-loop runner with the read tool catalogue (025):
   - `query_records`, `get_record_history`, `search`, `list_intel_events`, `get_intel_event`, `list_anomalies`, `get_window_state`, `get_exposure`, `get_entity`, `source_health`;
   - plus `find_similar_events` (new read tool: same type within 200 km over the history window, from `intelligence_event`, deterministic);
   - plus the analyst output tools `record_note`, `propose_watch`, `propose_intel_event`, `propose_investigation_item`, `annotate_event`.
   - Limits come from the profile. Use effort from the profile; cache the system prompt and tools; enable refusal fallback and check `stop_reason` (Architecture §19).
5. **Validation (pure `AnalystOutputValidator`):**
   - Every citation id must have appeared in this run's tool output (028 verifier).
   - Claims typed `evidence` without a valid citation become `speculation` with a warning.
   - `propose_intel_event` needs ≥ 2 existing members (verified by seq) and a recipe-registry `type`. Its confidence is computed by `EventScorer`.
   - `propose_watch` is validated by the 017/100 rule validator and is tagged `origin:analyst`.
   - Notes over 2,000 words are truncated with a warning.
6. **Persist and deliver** (§3, §4), advance the cursor, and finish the run log (§5).

- Failures: provider error or refusal → `failed` with the reason; budget refusal → `budget_exceeded` plus one system event (018) per profile per day; wall-clock limit → `partial` (keep validated output).

### 3. Notes

- `analyst_note` columns: id, profile_id, run_id, title, body (markdown), priority (low | normal | high), `claims` jsonb (`[{text, basis, citations[]}]`), citations, `private`, status (new | read | dismissed), created_at.
- A note citing anything private is private (039: excluded from non-private egress and from cloud providers unless the operator allows it).
- Notes are embedded (027) and delivered to `channel_ids` as event type `analyst.note` (018) when priority ≥ `notify_min_priority`. They carry the footer "Automated analysis by <model>. Evidence is cited; inference and speculation are labelled. Verify before acting." and appear in the 033 inbox.

### 4. Proposals and execution

- `analyst_proposal` columns: id, run_id, profile_id, type, payload, rationale, citations, status (pending | executed | rejected | expired | failed), executed_ref, decided_by, decided_at. Pending proposals expire after 7 d.
- `POST /v1/analyst/proposals/{id}/confirm` and `/reject` (agent) follow the 028 pattern.
- **Auto-execution** requires all of the following, all re-checked at execution time:
  - `allow_actions=true`;
  - the type is in `allowed_actions`;
  - the owner token is unrevoked, unexpired and holds `agent` scope;
  - the daily cap is not reached.
- Effects of each executed type:
  - watches are created **enabled** with `tags: [analyst]`;
  - events are created with `origin=analyst`, status `emerging`;
  - investigation items go only into open investigations;
  - `annotate_event` appends to `narrative` (labelled speculation) and never touches members or confidence.
- The analyst can never delete or disable anything, or touch sources, channels, tokens, providers or settings. Every execution is written to the 023 audit log with the profile and run id.

### 5. Evaluation log

- `analyst_run` columns:
  - id, profile_id, started_at, finished_at, status (ok | noop | partial | failed | budget_exceeded);
  - `cursor_from`/`cursor_to`, input counts by type;
  - provider, model, effort, tokens in/out, cached tokens, tool calls, duration;
  - notes, proposals, auto-executed counts;
  - `citations_dropped`, `claims_downgraded`, `warnings`;
  - `prompt_hash` (sha256 of system prompt + tools + instructions);
  - `detector_version` (`analyst-1.0.0`).
- Prompt and response content is stored only when `llm.logContent=true`.
- `analyst_feedback` columns: target (note | proposal), target_id, `rating` (useful | not_useful | wrong), comment, by, at.
- `GET /v1/analyst/evaluation?profileId&since` returns per-profile aggregates: runs by status, noop share, tokens per day, proposal acceptance rate (confirmed ÷ decided), notes dismissed share, useful/wrong ratios, citations dropped per run.

### 6. API and MCP

- **Profiles:** `GET/POST /v1/analyst/profiles`, `GET/PUT/DELETE /v1/analyst/profiles/{id}` (agent; GET read), `POST /v1/analyst/profiles/{id}/run` (agent, `alwaysAuthenticated`, 202 + run id).
- **Runs:** `GET /v1/analyst/runs?profileId&status&cursor`, `GET /v1/analyst/runs/{id}` (agent).
- **Notes:** `GET /v1/analyst/notes?profileId&status&priority&since&cursor` (read), `PATCH /v1/analyst/notes/{id}` (read: status), `POST /v1/analyst/notes/{id}/feedback` (read).
- **Proposals:** `GET /v1/analyst/proposals?status` (agent), confirm/reject (agent), `POST /v1/analyst/proposals/{id}/feedback` (agent).
- **Evaluation:** `GET /v1/analyst/evaluation` (agent).
- **SSE:** topic `analyst` on `/v1/events/stream` (`analyst.note`, `analyst.proposal`).
- **MCP:** `list_analyst_notes`, `get_analyst_note` (read); `list_analyst_proposals`, `confirm_analyst_proposal`, `reject_analyst_proposal`, `run_analyst` (agent).
- Every LLM-spending path is `alwaysAuthenticated`, even under `openReads`.
- Problem codes: `UNKNOWN_ANALYST_PROFILE`, `UNKNOWN_PROPOSAL` (404); `PROPOSAL_NOT_PENDING` (409); `ANALYST_RUN_IN_PROGRESS` (409); `LLM_NOT_CONFIGURED` (409).

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - schemas `AnalystProfile`, `AnalystProfileInput`, `AnalystRun`, `AnalystNote`, `AnalystClaim`, `AnalystProposal`, `AnalystFeedback`, `AnalystEvaluation`;
  - tag `Analyst`; `x-foresignal-scope` on every operation;
  - regenerate Postman with BODIES for `createAnalystProfile`, `runAnalystProfile`, `updateAnalystNote`, `analystNoteFeedback`, `confirmAnalystProposal`.
- AccessPolicy rows as above.
- `docs/Operator-Guide.md` "Autonomous analyst": cost and egress (cloud vs Ollama), budgets, `allowActions` risks, how to read claims, the evaluation endpoint.
- `docs/Agent-Guide.md`: proposal workflow.

## Tests to write (do not run)

- `AnalystOutputValidatorTest`:
  - uncited evidence → speculation;
  - unknown citation dropped;
  - event proposal with fewer than 2 members rejected;
  - LLM-supplied confidence discarded;
  - an absence claim without a coverage-ok citation warned.
- `AnalystChangeDetectorTest`: noop when nothing is new; loop guard excludes `origin=analyst`; ranking and cap.
- `AnalystRunnerIT` (a fake `LlmClient` scripted with tool calls):
  - a note with typed claims persisted and delivered;
  - proposals pending when `allowActions=false`;
  - auto-executed with `allowActions` and an agent owner;
  - **not** executed after the owner token is revoked;
  - daily cap;
  - budget refusal → `budget_exceeded` plus a single system event;
  - wall-clock limit → partial;
  - `logContent=false` stores no content.
- `AnalystSchedulerIT`: missed runs run once; one concurrent run per profile.
- `AnalystControllersIT`: CRUD, scope rules (`allow_actions` needs agent), alwaysAuthenticated under openReads, confirm/reject, feedback, evaluation aggregates, MCP tool shapes.

## Acceptance criteria

- With no new inputs, a scheduled run makes no LLM call.
- Every stored claim is typed. Evidence claims cite items returned by tools in that run.
- No proposal executes unless `allowActions=true`, the type is allowed, the owner token currently has agent scope and the cap is not reached.
- Runs, tokens, outcomes and feedback are queryable through the evaluation endpoint.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `102: Autonomous Analyst`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/102-CR-Autonomous-Analyst.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `102: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
