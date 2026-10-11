# 100 — Windowed Rules And Anomalies

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/100-CR-Windowed-Rules-And-Anomalies.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/099-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §5 rows "Windowed rules (count, absence)" and "Baselines + anomaly alerts", §8 item 55 (its "057a" is this prompt)
- `docs/Core-Data-Model.md` §8 (`fetch_log`, `source_health.last_coverage`/`recent_coverage`, `record_live`), §11 (triggers, edge semantics, cooldown on `observed_at`), §13 (retention, consumer clamp), §14 (coverage and the "coverage dropped" rule)
- `docs/Weather-Imagery-Design.md` §7.1 ("unknown is not clear": stale or missing input never produces an all-clear)
- Completion Reports of 017 (rule model, alert lifecycle, `cooldownScope`, `near`, rule `tags`, `max_alerts_per_hour`), 006 (coverage rules), 036 (places), 038 (`activity_rollup`), 052 (DERIVED runtime), 098 (entity registry), 099 (derived layers)
- `docs/OpenAPI.json` schemas `TriggerRuleInput`, `TriggerRule`, `Alert`, operation `testTrigger`

## Objective

Add rules that reason over time windows instead of single version edges: **count** rules (N distinct entities in a window), **absence** rules (nothing matching for a window) and **anomaly** rules (activity that is statistically unusual against a learned, coverage-aware baseline). This answers "tell me when activity here is unusual" without an LLM. Every output is reproducible from stored inputs, and a drop in receiver or fetch coverage is never reported as a drop in activity.

## Analytic principles (binding for this prompt)

1. **Deterministic detection.** Every evaluation is a pure function of stored versions, live rows, `fetch_log` and the rule definition. No LLM is involved.
2. **Reproducible.** Every result records the detector name, version, parameters, window bounds, the seq watermark it read up to, and sample evidence references.
3. **Not a probability.** A p-value or score assumes the stated count model. API fields, docs and alert text must not call it "the probability that something is happening".
4. **Absence of evidence is not evidence of absence.** Absence and downward-anomaly results are produced only when the input sources were healthy and covered the window. Otherwise the state is `unknown` with a reason.

## Tasks

### 1. Rule model (`ai.foresignal.trigger.window`)

- Add `trigger_rule.type`: `record` (default, the existing engine) | `count` | `absence` | `anomaly`. Existing rules migrate to `record`.
- Add the jsonb column `window_spec`. Validate it per type:
  - `window`: duration from 1m to 7d.
  - `step`: evaluation period, default `min(window/4, 5m)`, minimum 30s.
  - `distinctBy`: `entity` (default) | `externalId` | a payload path.
  - `groupBy`: null | `source` | `place` | `h3:<res 3..8>` | `admin1` | `country` (needs 040) | a payload path. Cap each rule at `maxGroups` 500. Over the cap, evaluate the top 500 by count and set `groupsTruncated`.
  - count: `op` (`>=` | `<=`), `threshold` (int ≥ 0).
  - anomaly: `bucket` (`15m` | `1h`, default `1h`); `slotMode` (`hour-of-week` (default) | `hour-of-day`); `direction` (`up` | `down` | `both`, default `up`); `sensitivity` (`low` | `medium` | `high` = α 1e-4 / 1e-3 / 1e-2); `minAbsoluteDelta` (default 3); `minWeeks` (8–12, default 8); `coverageMin` (default 0.8).
- Windowed rules reuse the 017 filters: source and kind arrays, conditions with set operators, geofence, `near`/place, active window, `tags`, severity, `max_alerts_per_hour`. Conditions apply to the merged payload. `onlyNew`, `mode` and `reappearAsEnter` are rejected for windowed types with 400 `WINDOW_RULE_INVALID`.
- **Entity identity:**
  - `entity` counts the 098 canonical entity id when the record is linked; otherwise the descriptor `entityKeys` (008); otherwise `(source, externalId)`.
  - Never count versions.
  - Document in the Operator Guide that one aircraft seen by two sources counts once only when 098 links them. Cluster identity (for example "one storm") is out of scope.

### 2. Window evaluation

- `WindowScheduler` runs one virtual-thread loop. Each enabled windowed rule is evaluated at step-aligned instants `t`. At every tick, record `watermarkSeq = ChangeBus.lastPublishedSeq()` before querying.
- **Membership.** An entity is "seen in `(t − window, t]`" when either holds:
  - its live row matches the filters and `l.observed_at > t − window` (current heads; `l.observed_at` is the latest observation, so it was seen in the window), or
  - a non-tombstone, non-HEARTBEAT version with `observed_at` in the window and `seq ≤ watermarkSeq` matches. This catches entities purged or tombstoned inside the window.
- Use `record_version (source_id, observed_at)` and the live GiST index. **Do not add an index on `record_live.observed_at`** (Core-Data-Model §8).
- **State table `window_state`:**
  - Columns: `rule_id`, `group_key`, `state` (`active` | `clear` | `unknown` | `learning`), `since`, `last_value`, `last_evaluated_at`, `last_watermark_seq`, `reason`, `last_alert_id`.
  - PK `(rule_id, group_key)`.
- **Transitions:**
  - Fire an alert when the window predicate goes clear → active.
  - When it goes active → clear, resolve that alert through the 017 lifecycle (`autoResolve`).
  - `unknown` never resolves an alert and never fires one.
  - Cooldown and `max_alerts_per_hour` apply per `(rule, group_key)`.
- **Output records.** Register a DERIVED source `rule-windows` (052 runtime: STANDARD, no TTL, emits only on change, tombstones on rule delete, reconciles at startup).
  - Kinds `window-state` and `anomaly`. externalId `{ruleId}:{groupKey}`.
  - The payload holds the rule id, name, type, groupKey, state, value, threshold or expected, classification and detector, and **excludes the evaluation timestamp**.
  - Window alerts reference that record's version (`alert.seq`), so `Alert` and SSE need no schema change, and 101 can consume anomalies as records.
- Add `window.cursor`-style catch-up only for anomaly buckets (§4). Count and absence evaluate the current window only; missed ticks are not replayed.

### 3. Absence rules and the coverage gate

- Absence is evaluated only on scheduler ticks, never on ChangeBus batches.
- **`CoverageGate.evaluate(sources, from, to)`** returns `ok` or `unknown(reason)`. It returns `ok` only when **every** involved source passes all of these:
  - it is enabled and was enabled for the whole window (from settings history or `fetch_log` presence);
  - its health is not `failing`, and not `degraded` for a coverage or format reason (006 rules);
  - `last_data_at ≥ t − max(window, 2 × interval)`;
  - the share of OK or NOT_MODIFIED `fetch_log` rows in the window is ≥ `coverageMin`. STREAM sources instead need no disconnect longer than 10% of the window;
  - `fetch_log.coverage` receivers or expected are not ≥ 50% below the median of the last 20 (the 006 rule).
- **Warm-up:** no absence evaluation until both the rule and the source have existed for one full window.
- On `unknown`, store the reason in `window_state` and raise a single system event (018) per transition: "Absence rule X cannot be evaluated: <reason>". Never alert.
- The Operator Guide states that receiver-down detection belongs to health and station events (006, 069), not to absence rules.

### 4. Coverage-aware baselines (anomaly)

- **Samples.** Add the table `activity_sample(rule_id, group_key, bucket_start, count, coverage_ratio, coverage_ok, flagged, PK(rule_id, group_key, bucket_start))`.
  - It is exempt from version retention and pruned after 26 weeks.
  - A durable cursor `anomaly.cursor.{ruleId}` fills completed buckets in order, including after downtime, as long as history is retained.
  - If 038's `activity_rollup` already carries the needed dimensions (source, kind, cell or place, distinct entities), read it instead, and record which path was used.
- **Coverage ratio** per bucket is the minimum over involved sources of (OK fetch share) × min(1, receivers ÷ median receivers of the last 20) when coverage is reported. Buckets below `coverageMin` are `coverage_ok = false`.
- **Model.**
  - Per slot (hour-of-week, or hour-of-day with a day-of-week factor), take the last `minWeeks`–12 weeks of `coverage_ok` samples, excluding `flagged` samples (prior anomalies), and winsorize at p99.
  - Expected count `λ = mean × coverage_ratio`.
  - Use a Poisson tail: `P(X ≥ k)` for up and `P(X ≤ k)` for down. If variance/mean > 1.5 over the slot history, use a negative binomial (method of moments) and record `model: nb`.
  - Fire when p < α **and** |k − λ| ≥ `minAbsoluteDelta`.
- **Warm-up.**
  - State is `learning` until every evaluated slot has ≥ `minWeeks` coverage-ok samples.
  - `hour-of-day` mode may start after 14 days, with `lowConfidence: true` on results.
  - On rule creation, backfill from retained history (and from rollups if used); report the weeks available.
- **Classification** of each evaluated bucket:
  - `activity-up` or `activity-down`;
  - `coverage-drop`: count down while the coverage ratio fell by ≥ 30% against the slot median. Never alert on it;
  - `insufficient-coverage`, `learning`, or `normal`.
- **Results table `anomaly_observation`:**
  - Columns: id, rule_id, group_key, bucket_start/end, observed, expected, exposure, p_value, `score` (Anscombe-transformed z), direction, classification;
  - `baseline` jsonb (weeks, slotMode, mean, variance, model);
  - `detector` jsonb (`{name: "poisson-tail", version: "1.0.0", params}`);
  - `evidence` jsonb (≤ 20 sample `(source, externalId, seq)` refs, and fetch_log id ranges per source);
  - `watermark_seq`.
  - Alerts set `alert.context.anomaly` with the same fields. The default message reads "Unusual activity: 42 observed vs ~11 expected (p≈2e-6 under a Poisson model)".

### 5. API, MCP and test endpoint

- **`TriggerRuleInput`/`TriggerRule`:** add `type` and `window` (oneOf `CountWindow`, `AbsenceWindow`, `AnomalyWindow`). `TriggerRule` adds `windowStatus` (state summary, groups, `learningUntil`).
- **New operations:**
  - `GET /v1/triggers/{ruleId}/window-state` (read): per-group state, value and reason.
  - `GET /v1/triggers/{ruleId}/baseline` (read): per-slot n, mean and variance, plus weeks available.
  - `POST /v1/triggers/{ruleId}/baseline/rebuild` (agent): recompute from samples and backfill.
  - `GET /v1/anomalies?ruleId&groupKey&classification&since&until&cursor` (read): newest first, keyset on id.
- `testTrigger` for windowed types evaluates over `fromSeq`/`toSeq` (or `since`), using versions only (no live rows). The response marks `approximate: true` with the reason "versions only; entities observed without a new version inside the window are missed when window < source keyframe". Full backtests are prompt 103.
- **MCP (025 registry):**
  - `list_anomalies` (read)
  - `get_window_state` (read)
  - `watch_for_unusual_activity` (agent): takes `{sources|kinds, place|bbox, groupBy?, direction, sensitivity}` and creates an anomaly rule; its reply states the warm-up period
  - extend `create_watch`/`update_watch` with `type` and `window`
- Problem codes: `WINDOW_RULE_INVALID` (400), `BASELINE_NOT_READY` (409, from `/baseline` before any sample).

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - the schemas and operations above, each with `x-foresignal-scope`;
  - the `rule-windows` kinds in the kind docs;
  - regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`, and add BODIES examples for a count rule, an absence rule and an anomaly rule, plus `rebuildBaseline`.
- AccessPolicy: GET window-state, baseline and anomalies → READ; POST rebuild → AGENT.
- Add the anomaly cursor and the window evaluator to the 019 retention consumer clamp, if 019 exposes a consumer registry. Otherwise extend the clamp and record this in the Completion Report.
- `docs/Operator-Guide.md` "Windowed rules and anomalies":
  - examples: "≥ 5 military aircraft within 100 km of home in 30 min"; "no NWS alert updates for 2 h while healthy"; "unusual vessel count in the strait";
  - warm-up, coverage gating, the four principles, and the meaning of p-values;
  - entity identity limits.
- `docs/Agent-Guide.md`: the MCP tools and how to phrase anomaly results honestly.

## Tests to write (do not run)

- `WindowSpecValidatorTest`: every field, type-specific rejections, group cap.
- `CoverageGateTest`: a table of health status × fetch share × coverage drop × enabled-duration × stream disconnects → ok or unknown with the reason.
- `PoissonTailTest` and `NegBinTailTest`: tails against reference values (R `ppois`/`pnbinom` values hard-coded), overdispersion switch, `minAbsoluteDelta`.
- `AnomalyClassifierTest`: activity-down vs coverage-drop vs insufficient-coverage; flagged samples excluded from training; winsorizing.
- `WindowEvaluatorIT` (`@IntegrationTest`, seeded with `TestRecords` only):
  - count rule fires once on clear → active and resolves on active → clear;
  - distinct entities, not versions;
  - an entity seen only via a touch (no new version) still counts through the live row;
  - per-group states with `groupBy=place`.
- `AbsenceRuleIT`:
  - a healthy source going quiet fires;
  - a failing source gives `unknown` plus one system event and no alert;
  - a 304 streak with steady coverage still counts as covered;
  - warm-up is respected.
- `AnomalyRuleIT`:
  - backfilled synthetic Poisson history (fixed seed, `MutableClock`) is learning until `minWeeks`;
  - a spike fires `activity-up` with detector version and evidence refs;
  - halving the receiver count yields `coverage-drop` and no alert;
  - the cursor catches up after simulated downtime.
- `WindowControllersIT`: CRUD with each type, window-state, baseline, anomalies paging, 400/409 codes, scopes.

## Acceptance criteria

- Count, absence and anomaly rules can be created through REST and MCP. They alert and resolve through the 017 lifecycle, and their alerts reference `rule-windows` record versions.
- A source outage or coverage drop never produces an absence alert or a downward anomaly. It produces `unknown` or `coverage-drop` with a reason.
- Every anomaly result carries the detector name, version and parameters, the baseline summary, `watermarkSeq` and evidence refs.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `100: Windowed Rules And Anomalies`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/100-CR-Windowed-Rules-And-Anomalies.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `100: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
