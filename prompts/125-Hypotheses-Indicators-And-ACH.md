# 125 — Hypotheses Indicators And ACH

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/125-CR-Hypotheses-Indicators-And-ACH.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/124-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2, §6.2–§6.5 (hypothesis sets, speculative links, indicators, ACH, key assumptions, updating, ICD 203 wording, drafting), §8 (ledger links), §13 (worked example)
- Completion Reports of 116 (evidence groups, observability, manifests), 123 (benchmark status), 017 (rule model, tags, validator, AlertBus), 028 (tool loop, citation verifier), 100 (window and anomaly rules, CoverageGate), 102 (analyst profiles, proposals, output validator), 109 (reliability grades), 118 (situations), 119 (findings, event studies and placebo baselines, signal families), 120 (precursors), 121 (base rates, analogs), 122 (ledger, `EstimativeLanguage`, `indicator-bayes` forecaster type)

## Objective

Implement **rolling hypotheses**. A hypothesis set frames one key question and its competing answers. Each hypothesis has a prior with a stated basis, a probability that moves only through recorded revisions, a separate confidence, and key assumptions. Indicators are observable signals with likelihood ratios that **watch for themselves** through ordinary trigger rules; when they fire (or fail to appear when expected), a deterministic Bayesian updater revises the set and explains the change. An ACH matrix records consistency of evidence across hypotheses. Speculative-link hypotheses (such as "unusual positioning reflected advance knowledge") are supported with mandatory alternatives and coincidence baselines. Sets link to ledger questions, so every revision is also a scored forecast.

## Principles (binding)

- **Revisions are the record.** Probabilities change only through `hypothesis_revision` rows with a cause, an actor, `asof_seq`, citations and a change rationale relative to the previous revision (ICD 203 standard 7).
- **Deterministic updates.** The updater is pure and versioned. The LLM may draft sets and propose indicators, ratings and evidence; proposals become effective only through operator or agent confirmation (102 rules). LLM forecasts on the linked question are separate forecasters in the ledger and never overwrite a hypothesis probability.
- **Outside view first.** Every prior names its basis (a 121 base rate, an analog set, or an operator judgment with rationale).
- **Numbers are the truth.** Terms come from `EstimativeLanguage`; all generated text passes its lint.
- **No accusations.** Speculative links about market activity are phrased as associations and always carry alternatives and the coincidence baseline.

## Tasks

### 1. Data model (`ai.foresignal.hypothesis`)

- **`hypothesis_set`:** `id` (ULID), `question` (the key question, ≤ 300 chars), `description`, `kind` (exclusive | independent), `cadence` (daily | weekly | monthly), `horizon` (date by which the question resolves), `status` (active | resolved | archived), `situation_id` (nullable), `question_id` (nullable; 122), `region_codes`, `themes`, `origin` (operator | agent | analyst), `confidence` (low | moderate | high, set-level), `last_reviewed_at`, `next_review_at`, `private`, `version`, timestamps.
- **`hypothesis`:** `id`, `set_id`, `label` (short), `statement`, `type` (outcome | explanation | speculative-link | other), `prior`, `prior_basis` jsonb (`{type: base-rate|analogs|operator, ref, rationale}`), `probability`, `estimative_term` (derived), `confidence` (low | moderate | high), `confidence_basis` jsonb (evidence quality, source reliability from 109, coverage, reporting density, agreement between forecasters), `ledger_outcome` (which outcome of the linked question this hypothesis maps to), `status` (open | confirmed | refuted | expired), `version`.
  - An `exclusive` set always contains exactly one `type=other` hypothesis ("other / none of these"); probabilities sum to 1.
- **Speculative-link fields** (required when `type=speculative-link`): `linked_finding_ids` (≥ 1, 119 findings), `coincidence_baseline` jsonb (the placebo rate from the linked event study and its method), `alternatives` (≥ 2 other hypotheses in the same set, e.g. scheduled news or inventory data, contract roll or rebalancing, coincidence at the base rate), `probability_cap` (computed, §4.6).
- **`hypothesis_revision`** (append-only): `id`, `set_id`, `at`, `cause` (indicator-observed | indicator-absent | evidence-added | evidence-removed | base-rate-refresh | review | operator | created), `actor`, `asof_seq`, `before` and `after` (probabilities, terms, confidences for every hypothesis), `drivers` jsonb (indicator or evidence ids and their log-LR contributions), `citations`, `rationale` (deterministic text for automatic causes; operator or validated analyst text for reviews), `updater_version`.
- **`indicator`:** `id`, `set_id`, `description`, `signal` jsonb (one of: `rule` (an inline 017/100 rule spec), `precursor` (120 id), `series` (ref + condition or `changePoint: true`), `intelEvent` (type + area), `situation` (id + status or minimum severity), `question` (122 id + outcome), `manual`), `signal_family` (119 registry), `credibility` and `relevance` (H | M | L), `expected_by` (nullable), `lr` jsonb (per hypothesis id: `{class: high|medium|low|neutral, direction: supports|weakens, value?, rationale?}`), `absence_lr` jsonb (optional overrides), `state` (pending | emerging | observed | absent | unknown-coverage | retired), `watch_rule_id`, `observed_evidence` jsonb, `last_state_change_at`, `origin`.
- **`ach_evidence`:** `id`, `set_id`, `source` (indicator | record | finding | intel_event | alert | manual), `ref`, `description`, `credibility`, `relevance`, `ratings` jsonb (per hypothesis: CC | C | N | NA | I | II), `added_by`, `added_at`, `removed_at`.
- **`key_assumption`:** `id`, `set_id`, `text`, `linchpin` bool, `rationale`, `would_be_wrong_if`, `status` (solid | caveated | unsupported | broken), `indicator_ids`, `review_due`, `updated_by`, `updated_at`.
- **Records:** publish each set as an `foresignal-intel` record (kind `hypothesis-set`; payload: question, kind, hypotheses with probabilities and terms, set confidence, status, last revision time), emitted on change.

### 2. Indicators that watch for themselves

- For `rule`, `series` (as a 100 window or anomaly rule, or a rule on 119 `change-point` findings for the ref), `intelEvent` and `situation` signals, `IndicatorWatchService` creates and keeps in sync one 017 trigger rule tagged `indicator:{id}`, `origin:hypothesis`, with no channels by default. Editing or retiring the indicator updates or disables the rule.
- `precursor` signals subscribe to 120 `precursor-active` findings; `question` signals subscribe to 122 resolutions.
- An alert or finding for the indicator moves it to `observed` (or `emerging` when the signal defines a two-stage threshold), records the evidence, and calls the updater.
- `expected_by` passing with 116 `canSeeAbsence` true for the indicator's family, area and window → `absent`, and the updater applies `absence_lr`; without coverage → `unknown-coverage` (no update).

### 3. ACH matrix

- Evidence rows are one per 116 evidence group (re-reports attach to the existing row). Observed indicators become `ach_evidence` rows automatically, with ratings derived from their LRs: LR ≥ 4 → CC; ≥ 1.5 → C; ≤ 0.25 → II; ≤ 0.67 → I; otherwise N; `NA` when the indicator has no LR for that hypothesis. Manual evidence is rated by its author.
- **Inconsistency score** per hypothesis: `−Σ w · inc` with inc(I) = 1, inc(II) = 2 and `w = credibility × relevance` (L 0.5, M 1, H 2). Rows rated identically across all hypotheses are flagged `non-diagnostic`.
- `GET /v1/hypotheses/sets/{id}/matrix` returns hypotheses, evidence rows with ratings and weights, scores, the least-inconsistent hypothesis, and non-diagnostic flags. The matrix is an audit view: probabilities come from the updater, and the ledger scores them.

### 4. Updater (`HypothesisUpdater`, `indicator-bayes` 1.0.0)

1. **Inputs:** the set's current probabilities and the new driver (indicator state change, evidence change, base-rate refresh, review).
2. **Log-odds update:** for each hypothesis, add `ln(LR)` of each driver. LR values by class: high 5.0 (supports) or 0.2 (weakens), medium 2.5 or 0.4, low 1.4 or 0.7, neutral 1.0, unless an explicit `value` with rationale is set (bounded to [0.05, 20]). Default `absence_lr`: the reciprocal of the class value moved halfway toward 1 in log space (e.g. high-supports absence → 1/√5 ≈ 0.45). Absence is scaled by detectability: `absence_lr_eff = 1 + (absence_lr − 1) × p_detect` (116), so a barely observable indicator moves the set barely at all.
3. **Correlated evidence:** observations in the same 116 evidence group are one piece of evidence (an echoed report never updates twice); within one `signal_family`, only the largest |ln LR| per hypothesis per UTC day applies; the others are recorded as `suppressed` drivers.
4. **Damping:** total change per hypothesis per UTC day ≤ 1.5 logits; probabilities clamped to [0.02, 0.98] while `active`.
5. **Normalisation:** `exclusive` sets renormalise by softmax over log-odds (the `other` hypothesis takes the residual mass); `independent` sets do not.
6. **Speculative-link cap:** `probability_cap = min(0.98, posterior implied by the linked findings' likelihood ratio, where LR = (1 − placebo rate) ÷ placebo rate bounded to [1, 20], applied to the hypothesis prior)`. The updater never raises a speculative-link hypothesis above its cap; an operator override is allowed, logged and labelled.
7. **Confidence:** recomputed by rule (`ConfidenceAssessor` 1.0.0): high requires ≥ 2 observed indicators from ≥ 2 families with credibility ≥ M, no broken linchpin, sources with 109 grade ≥ B (or default), and no `LOW_REPORTING_DENSITY`; moderate requires ≥ 1 observed indicator with credibility ≥ M; otherwise low. A broken linchpin lowers confidence one step.
8. **Output:** a `hypothesis_revision` with drivers and a deterministic rationale ("Moved 'response within 72 h' from unlikely (31%) to roughly even chance (49%): NOTAMs closing southern Saudi airspace observed (high diagnosticity); naval co-occurrence absent with coverage (low)."), lint-checked; and, when the set has `question_id`, a ledger forecast from forecaster `indicator-bayes` (multiple-choice distribution mapped by `ledger_outcome`).
9. **Band changes** (`EstimativeLanguage.crossesBand`) emit SSE `set.band_changed` on topic `hypotheses` and the 018 notification event `hypothesis.band_changed`, rate-limited to one per set per 6 h (the latest wins).

### 5. Lifecycle and reviews

- Creating a set: validate structure (exclusive sets have `other`; priors sum to 1 for exclusive sets; each prior has a basis); create the ledger question when `horizon` and a resolver are provided (122 `hypothesis-outcome` resolver by default, or a machine resolver such as `event-exists`); create indicator watch rules.
- **Reviews:** `next_review_at` from `cadence`; 128's cycles perform reviews. `POST /v1/hypotheses/sets/{id}/review` (agent) records a review revision with an operator rationale (or confirms an analyst proposal); a review may change LRs, assumptions or probabilities directly, always as a revision with cause `review` or `operator`.
- **Resolution:** when the linked question resolves, hypotheses become `confirmed`/`refuted` accordingly and the set `resolved`; sets past `horizon` without a question become `archived` after a review.
- **Base-rate refresh:** when a prior's base rate changes by more than 20% relative, record a `base-rate-refresh` revision that shifts the log-odds by the change in prior log-odds (evidence contributions are kept).

### 6. Drafting (LLM, optional)

- `POST /v1/hypotheses/sets/draft {text, situationId?, regionCodes?}` (agent, `alwaysAuthenticated`): a 028 tool loop (effort `medium`) with the read catalogue that must call `get_base_rate` and `find_analogs` before proposing priors. Output: a draft set with competing hypotheses including `other`, priors with bases, 3–10 indicators as rule specs validated by the 017/100 validators, LR classes with rationales, key assumptions with linchpin flags, and a proposed resolver. The draft is validated (structure, lint, rule specs) and returned with warnings; nothing is created until `POST /v1/hypotheses/sets` is called with it.
- MCP `draft_hypothesis_set` (agent).

### 6a. Information value and collection gaps

- **Expected information gain** (`InformationValue` 1.0.0): for each pending indicator, `EIG = H(P) − Σ_o P(o) · H(P | o)` over outcomes o ∈ {observed, absent}, where `P(observed) = Σ_h P(h) · q_h · p_detect`, `q_h` derived from the indicator's LR and the set's base rate of the signal, and `H` the Shannon entropy of the set's distribution. `GET /v1/hypotheses/sets/{id}/information-value` (read) ranks indicators by EIG and shows each one's `p_detect`.
- **Collection gaps:** an indicator whose EIG would be in the set's top three if it were fully observable (`p_detect = 1`) but whose actual `p_detect < 0.5`, or whose sources are disabled, becomes a `collection_gap` row (`set_id`, `indicator_id`, `potential_eig`, `actual_eig`, `p_detect`, `recommendations` jsonb: enable a catalogued source by id, add a local receiver or sensor type (069/078/086 families), or "no catalogued source observes this"). Recommendations never change configuration; enabling a source stays an operator action. `GET /v1/collection-gaps?setId&cursor` (read). 128's cycles report them.

### 7. API, SSE, MCP

- Sets: `GET /v1/hypotheses/sets?status&cadence&situationId&region&cursor` (read), `GET /v1/hypotheses/sets/{id}` (read; hypotheses with probability, term, confidence and change since the previous revision; indicators; assumptions; linked question and ensemble), `POST /v1/hypotheses/sets` (agent), `PATCH /v1/hypotheses/sets/{id}` (agent; description, cadence, status), `GET /v1/hypotheses/sets/{id}/revisions?cursor` (read), `GET /v1/hypotheses/sets/{id}/matrix` (read), `POST /v1/hypotheses/sets/{id}/review` (agent).
- Hypotheses: `POST /v1/hypotheses/sets/{id}/hypotheses`, `PATCH /v1/hypotheses/{id}` (agent; statement, prior basis, operator overrides logged).
- Indicators: `POST /v1/hypotheses/sets/{id}/indicators`, `PATCH /v1/indicators/{id}`, `DELETE /v1/indicators/{id}` (retire) (agent); `GET /v1/indicators?state&setId` (read).
- Evidence and assumptions: `POST /v1/hypotheses/sets/{id}/evidence`, `DELETE /v1/hypotheses/sets/{id}/evidence/{evidenceId}`, `POST/PATCH /v1/hypotheses/sets/{id}/assumptions[/{aid}]` (agent).
- SSE topic `hypotheses`: `set.created|revised|band_changed|resolved`, `indicator.observed|absent`, `assumption.broken`.
- MCP: `list_hypothesis_sets`, `get_hypothesis_set`, `get_ach_matrix`, `get_information_value`, `list_collection_gaps` (read; read catalogue); `create_hypothesis_set`, `add_evidence`, `review_hypothesis_set`, `draft_hypothesis_set` (agent).
- Notifications: `hypothesis.band_changed` (default on for sets with `cadence` daily or weekly), `indicator.observed`, `assumption.broken` (default off).
- Problem codes: `UNKNOWN_HYPOTHESIS_SET`, `UNKNOWN_HYPOTHESIS`, `UNKNOWN_INDICATOR` (404); `HYPOTHESIS_SET_INVALID`, `INDICATOR_INVALID`, `LINT_FAILED` (400); `HYPOTHESIS_SET_VERSION_CONFLICT`, `HYPOTHESIS_SET_RESOLVED` (409).

### 8. Spec and docs

- `docs/OpenAPI.json`: `HypothesisSet`, `HypothesisSetInput`, `Hypothesis`, `HypothesisRevision`, `Indicator`, `IndicatorInput`, `AchMatrix`, `AchEvidence`, `KeyAssumption`, `HypothesisSetDraft`; tag `Hypotheses`; `x-foresignal-scope`; SSE schemas; Postman BODIES for creating the worked-example set (Intelligence-Design §13) with indicators, adding evidence, a review, and a draft.
- AccessPolicy rows as listed.
- `docs/Operator-Guide.md` "Rolling hypotheses": key questions, competing hypotheses and "other", priors from base rates, indicators and LR classes, absence, damping, ACH as an audit trail, key assumptions, speculative links and why they are capped, reading revisions.
- `docs/Agent-Guide.md`: never present a speculative link without its alternatives and baseline; keep likelihood terms and confidence levels in separate sentences.
- Note for 136–137 (social signals): evidence items gain source `social-claim`, and indicators gain the state `reported` (a social claim) distinct from `observed` (physical evidence); keep the state and source enums extensible.

## Tests to write (do not run)

- `HypothesisUpdaterTest`: log-odds per class, explicit LR bounds, absence LR, same-family suppression, daily damping, clamping, exclusive renormalisation with `other`, independent sets untouched, speculative-link cap, base-rate refresh shift.
- `ConfidenceAssessorTest`: each rule and the linchpin downgrade.
- `AchMatrixTest`: LR → rating mapping, weighted inconsistency, non-diagnostic flag.
- `IndicatorWatchIT`: watch rule created with tags; an alert observes the indicator and produces one revision and one ledger forecast; `expected_by` with coverage → absent; without coverage → unknown-coverage and no revision; retire disables the rule.
- `HypothesisLifecycleIT`: structure validation; question creation; resolution confirms/refutes; review revisions; band-change notification rate limit.
- `RevisionRationaleTest`: deterministic text passes `EstimativeLanguage.lint`; term + confidence never in one sentence.
- `DraftHypothesisSetIT` (fake `LlmClient`): base-rate and analog tools called first; invalid rule specs rejected with warnings; nothing persisted.
- `HypothesisControllersIT`: endpoints, scopes, optimistic locking, `private`, SSE topic, MCP shapes.

## Acceptance criteria

- Hypothesis sets with priors, indicators, assumptions and a linked ledger question can be created; indicators create their own watch rules.
- Observed and absent indicators revise probabilities deterministically, with damping, family de-duplication, revision rationale and a ledger forecast per revision.
- Speculative-link hypotheses require alternatives and a coincidence baseline and cannot exceed their cap without a logged override.
- The ACH matrix and key assumptions are available as audit views.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `125: Hypotheses Indicators And ACH`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/125-CR-Hypotheses-Indicators-And-ACH.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `125: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
