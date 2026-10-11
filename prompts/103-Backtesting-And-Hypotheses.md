# 103 — Backtesting And Hypotheses

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/103-CR-Backtesting-And-Hypotheses.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/102-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §5 row "Alert-quality feedback and backtests", §8 item 7 (feedback enum, `fromSeq`/`toSeq` rule test)
- `docs/Core-Data-Model.md` §8 (`seq_clock`, `S(T)`, `cutoffSeq`), §11 (edge join, cooldown on `observed_at`, HEARTBEAT exclusion), §12 (asOf, `approximate`), §13 (retention floors, downsampling)
- Completion Reports of 017 (RuleSqlBuilder, rule test with `fromSeq`/`toSeq`, feedback enum, alert lifecycle, `max_alerts_per_hour`, `cooldownScope`), 019 (`RetentionState.lowestRetainedSeq`, floors), 044 (downsampled ranges), 098, 100 (window evaluator, anomaly model), 101 (CorrelationEngine dry-run mode)

## Objective

Let the operator ask "what would this rule or recipe have done?" and "does X tend to precede Y within T?" over retained history, and get answers that state their own limits. Results are reproducible: a definition snapshot, a seq range and a detector version give the same output. Add alert-quality statistics from the 017 feedback enum, and deterministic tuning suggestions that are reported and never auto-applied. Prompt 115 builds the tuning and exercise workflow on top.

## Principles (binding)

- Backtests replay what Elaro **stored**, not ground truth. Every result carries generated caveats (§2).
- **No look-ahead.** Anomaly and recipe replays use only data with `seq ≤` the replay cursor at each step. Baselines train only on buckets before the evaluated bucket.
- Hit rates, lifts and p-values are descriptive statistics with stated assumptions. They are never presented as calibrated probabilities. Small samples are labelled `insufficient`.

## Tasks

### 1. Job framework (`ai.elaro.backtest`)

- `backtest_job` columns:
  - id (ULID), `type` (rule | recipe | hypothesis | quality);
  - `target_id` (nullable), `definition` jsonb (a snapshot of the rule, recipe or hypothesis at submit time), `definition_hash`;
  - `from_seq`, `to_seq`, requested time range;
  - status (queued | running | done | failed | cancelled), progress (0–1), started/finished;
  - `result` jsonb (≤ 1 MB; larger detail is truncated with `truncated: true`), `caveats` jsonb, `detector_version`, created_by.
- `BacktestWorker`: one platform thread by default (`elaro.backtest.concurrency`, max 2).
  - It processes in 50k-seq chunks, each with `statement_timeout=60s`, and checks for cancellation between chunks.
  - At most one queued or running job per token (409 `BACKTEST_IN_PROGRESS`).
  - Jobs are capped at 90 days or the retained range, whichever is shorter.
- **Range resolution:** time → seq via `seq_clock` (`S(T)`, Core-Data-Model §8). A start below `RetentionState.lowestRetainedSeq` for the involved sources is clamped, and the caveat `RANGE_CLAMPED_BY_RETENTION` is added.
- Finished jobs are kept for 30 days.

### 2. Caveat generator (pure, shared by every job type)

Emit each caveat that applies, with a code, text and data:

- `RANGE_CLAMPED_BY_RETENTION`
- `DOWNSAMPLED_RANGE`: 044 leaves in range; edges and samples between kept rows are missing.
- `SAMPLED_SOURCE`: SAMPLED or LATEST sources; values between persisted versions were never stored, and edges have poll or flush resolution.
- `COVERAGE_GAPS`: per source, intervals where `fetch_log` shows FAILED/REFUSED runs or no rows for > 3 × interval, or coverage dropped (006). Absence and downward results inside gaps are excluded.
- `DEFINITION_DIFFERS`: the snapshot differs from the current rule or recipe.
- `ENTITY_RESOLUTION_CHANGED`: 098 links changed in range.
- `WINDOW_APPROXIMATE`: windowed rules replayed from versions only, at keyframe resolution.
- `SMALL_SAMPLE`: n < 10.
- `NOT_GROUND_TRUTH`: always present.

### 3. Rule backtests

- **Record rules:** run `RuleSqlBuilder` per chunk with alert insertion disabled.
  - Simulate in Java, in seq order: cooldown per `(rule, entity)` on `observed_at` (honouring `cooldownScope`), `max_alerts_per_hour` and the active window.
  - Result:
    - `wouldFire` total;
    - a per-day histogram;
    - top 20 entities;
    - up to 200 sample hits `{seq, source, externalId, observedAt, edge}`;
    - `actual` (alerts that rule really fired in range, when the definition hash matches the alert's rule version or the rule is unchanged);
    - `overlap {both, onlyBacktest, onlyActual}`.
- **Windowed rules (100):** replay `WindowEvaluator` at step instants using versions only. Add the caveat `WINDOW_APPROXIMATE`. Coverage-gate each step with `fetch_log` as of that step.
- **Anomaly rules:** replay buckets in order. The baseline at bucket b uses only samples before b. Report the warm-up period consumed, anomalies by classification, and `coverage-drop` counts.
- **Variant comparison:** `variants[]` (≤ 5 partial rule patches) run in the same pass and are reported side by side, for example thresholds 5.5 / 6.0 / 6.5.

### 4. Recipe backtests (101)

- Run `CorrelationEngine` in dry-run mode over the range into an in-memory event store, with dedup and lifecycle simulated on observed time.
- Result: events (type, start/end, members with join reasons, confidence, evidence quality), counts by type, and overlap with real `intelligence_event` rows in range (matched by shared members).

### 5. Hypotheses

- `hypothesis` columns:
  - id, name, statement (text);
  - `antecedent` and `consequent` selectors, each one of: `{rule: inline filter like 017}`, `{alertRuleId}`, `{intelEventType}`, `{anomalyRuleId, classification}`;
  - `minLag` (default 0), `maxLag` (required, ≤ 30 d);
  - `spatial`: `sameEntity` | `withinKm: n` | `sameRegion: country|admin1` | `none`;
  - `dedupWindow` (antecedent occurrences closer than this merge; default = maxLag);
  - created_by, created_at.
- **`HypothesisTester` (version `1.0.0`):**
  - Build antecedent occurrences A (deduplicated) and consequent occurrences C over the range, each with time, location or region, and entity.
  - **Hit:** the first c ∈ C with `a.t + minLag ≤ c.t ≤ a.t + maxLag` satisfying `spatial`. Record its lag.
  - **Stats:**
    - `nAntecedent`, `hits`, `hitRate` with a Wilson 95% interval;
    - lag min / p25 / median / p75 / max / mean;
    - `baseRate`: the fraction of 200 circular time-shifted copies of A (deterministic seed = hash of definition + range; shifts uniform in [maxLag, range − maxLag]) whose occurrences find a consequent under the same rule;
    - `lift = hitRate ÷ mean(shifted hit rates)`;
    - permutation `pValue` = (1 + #shifted ≥ observed) ÷ 201;
    - `reverse`: the same test with roles swapped. If the reverse is as strong, flag "co-occurrence, not precedence".
  - **Verdict labels:** `insufficient` (nAntecedent < 10 or any `COVERAGE_GAPS` covering > 30% of the range), `no-evidence-of-precedence`, `weak`, `notable`. Thresholds go in documented constants.
  - **Multiple-testing note:** report how many hypothesis tests this instance ran in the last 30 days, and the Bonferroni-adjusted p-value.
  - The output text says "association in stored data; not causation".
- `POST /v1/hypotheses/{id}/test {from, to | fromSeq, toSeq}` submits a `hypothesis` job.

### 6. Alert quality and tuning suggestions

- **`AlertQualityService.forRule(ruleId, since)`.** Map the 017 feedback enum into `positive`, `negative` and `neutral` classes in one table in code. Read the actual enum values from 017's Completion Report and record the mapping.
  - Report: alerts, labelled share, precision = positive ÷ (positive + negative) with a Wilson interval (null when labelled < 5), median time-to-ack and time-to-resolve, flapping (enter→exit < 10 min), duplicates (same entity within 2 × cooldown), and severity distribution.
- **`TuningSuggester` (deterministic; version `1.0.0`; never applied automatically).** Each suggestion holds `{id, kind, rationale, patch (a ready TriggerRuleInput partial), expectedEffect, caveats}`:
  - **raise/lower threshold:** for each numeric condition, scan candidate values from labelled alerts' `record_snapshot` values. Pick the value that removes the most negatives while keeping ≥ 90% of positives.
  - **add cooldown:** when there are duplicates.
  - **switch `match` → `enter`, or add `edgeMinInterval`:** when there is flapping.
  - **shrink geofence:** when ≥ 70% of negatives fall outside the convex hull of the positives buffered by 10%.
  - **lower anomaly sensitivity:** 100 rules with low precision.
  - `expectedEffect` comes from a rule backtest of the patched variant over the same range, run as a job when requested (`?simulate=true`).
- Applying a suggestion is an ordinary rule update by an agent. 115 adds the workflow.

### 7. API and MCP

- **Backtests:** `POST /v1/backtests` (agent; body `{type, targetId? | definition?, from|fromSeq, to|toSeq, variants?}` → 202 with the job), `GET /v1/backtests?status&type&cursor` (agent), `GET /v1/backtests/{id}` (agent), `DELETE /v1/backtests/{id}` (agent; cancel).
- **Hypotheses:** `GET/POST /v1/hypotheses`, `GET/PUT/DELETE /v1/hypotheses/{id}` (agent; GET read), `POST /v1/hypotheses/{id}/test` (agent).
- **Quality:** `GET /v1/triggers/{ruleId}/quality?since` (read), `GET /v1/triggers/{ruleId}/suggestions?since&simulate` (read; `simulate=true` needs agent and returns a job id).
- **MCP:** `run_backtest`, `get_backtest`, `test_hypothesis` (agent); `get_rule_quality`, `get_tuning_suggestions` (read). Outputs are compact, with caveats first.
- **Problem codes:** `UNKNOWN_BACKTEST`, `UNKNOWN_HYPOTHESIS` (404); `BACKTEST_IN_PROGRESS` (409); `BACKTEST_RANGE_INVALID` (400: empty, inverted, or wholly below retention); `HYPOTHESIS_INVALID` (400).

### 8. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - schemas `BacktestJob`, `BacktestRequest`, `BacktestCaveat`, `RuleBacktestResult`, `RecipeBacktestResult`, `Hypothesis`, `HypothesisInput`, `HypothesisResult`, `RuleQuality`, `TuningSuggestion`;
  - tag `Analysis`; `x-elaro-scope` on every operation;
  - regenerate Postman with BODIES for `createBacktest` (rule with variants), `createHypothesis` ("GPS jamming precedes AIS gaps within 6 h within 100 km") and `testHypothesis`.
- AccessPolicy rows as above.
- `docs/Operator-Guide.md` "Backtesting and hypotheses": caveat codes, no look-ahead, reading lift and p-values, multiple testing, the feedback mapping.
- `docs/Agent-Guide.md`: always quote caveats when reporting results.

## Tests to write (do not run)

- `CaveatGeneratorTest`: every code from synthetic retention, downsampling, `fetch_log` gap and definition-diff inputs.
- `CooldownSimulatorTest`: `observed_at` cooldown, `cooldownScope=rule`, hourly cap, active window.
- `HypothesisTesterTest`:
  - planted precedence (fixed seed) → notable;
  - independent processes → no-evidence;
  - symmetric co-occurrence → reverse flag;
  - deterministic shifts (same input → same p);
  - Wilson bounds against reference values;
  - n < 10 → insufficient.
- `TuningSuggesterTest`: threshold scan on labelled snapshots; duplicate → cooldown; flapping → enter; geofence shrink.
- `RuleBacktestIT` (`TestRecords`): wouldFire equals the alerts the live engine produced for the same seeded range and rule; variants side by side; range clamped by retention with a caveat.
- `AnomalyBacktestIT`: no look-ahead (a spike at the end does not change earlier verdicts).
- `RecipeBacktestIT`: dry-run events match live engine events on the same seeded stream.
- `BacktestControllersIT`: submit, progress, cancel, one-job-per-token 409, scopes, result truncation flag, MCP tool shapes.

## Acceptance criteria

- The same definition snapshot and seq range always produce identical results, including permutation statistics.
- Every result includes `NOT_GROUND_TRUTH` plus each applicable caveat. Ranges below retention are clamped and reported.
- A record-rule backtest over a seeded range reproduces the alerts the live engine fired.
- Tuning suggestions include a ready patch and are never applied automatically.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `103: Backtesting And Hypotheses`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/103-CR-Backtesting-And-Hypotheses.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `103: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
