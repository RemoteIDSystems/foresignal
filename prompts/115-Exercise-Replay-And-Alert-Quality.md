# 115 — Exercise Replay And Alert Quality

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/115-CR-Exercise-Replay-And-Alert-Quality.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/114-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §5 rows "Alert-quality feedback and backtests" (feedback column in 017 now, tuning later), "Kinematic CPA/ETA, exercise mode…"; §8 item A.7 (alert lifecycle, feedback enum, snooze, `max_alerts_per_hour`, `fromSeq`/`toSeq` backtesting)
- `docs/Core-Data-Model.md` §8 (`seq_clock`, `cutoffSeq`, `S(T)`), §11 (edge join on `prev_seq`, enter/exit, a missing predecessor fires nothing, cooldown on `observed_at`, `edgeMinInterval`), §13 (retention floors: replay is limited to retained history)
- Completion Reports of 017 (alert `state`, `feedback` enum `useful|noise|late|false_positive`, `PUT /v1/alerts/{id}/feedback`, snooze, `max_alerts_per_hour`, AlertBus), 018 (NotificationMessage, persisted notifications, quiet hours, folding), 023 (audit log), 028 (proposed actions), 029 (digest renderer), 030 (enrichment), 031/032/033 (push payloads, signed actions, read cursors, escalation), 038 (rollups), 061 (chat commands), 100 (windowed and anomaly rules), **103** (rule backtest engine, `AlertQualityService.forRule`, `TuningSuggester`, `GET /v1/triggers/{id}/quality` and `/suggestions`, MCP `get_rule_quality`/`get_tuning_suggestions`), 112 (CAP `status`)

## Objective

Two operator workflows on top of the trigger engine and the 103 backtest layer. **Exercise/replay** replays a past window of retained history through selected rules to opted-in channels as clearly marked exercise alerts, so the operator can rehearse the alert chain end to end without touching real alert state. **Alert-quality tuning** completes 103's statistics and suggestions into a workflow: feedback captured from every channel, implicit signals and daily trends, extra suggestion types, review, apply as an explicit rule update, an audit log and undo. Nothing changes a rule automatically.

## Tasks

### 1. Exercise flag everywhere (`ai.elaro.exercise`)

- **Columns:** `alert.exercise bool not null default false`, `alert.exercise_run_id`; `notification.exercise` on the persisted notification row (018); `channel.accept_exercise bool default false`.
- **Marking rules** (one helper, `ExerciseMarking`, used by every renderer):
  - NotificationMessage title prefix `[EXERCISE] ` and body footer "This is an exercise replay of data from {from}–{to}. No action required."
  - Webhook header `X-Elaro-Exercise: true` and payload field `exercise: true`
  - Push payloads (031/032) `exercise: true` with urgency forced to `normal`, never time-sensitive or critical interruption levels
  - CAP (112) `status=Exercise`
  - MCP and SSE alert events (`alert`, `alert.updated`, `alert.resolved`) carry `exercise: true`
- **Isolation:**
  - Exercise alerts are delivered **only** to channels with `accept_exercise=true` and to channels named in the run.
  - Excluded by default from `GET /v1/alerts`, `streamAlerts` and the `alerts` topic of `/v1/events/stream` (`includeExercise=true` shows them), digests and briefings (029), rollups and stats (038), glance, escalation policies (033; never escalate), enrichment (030; unless `enrich=true` on the run), federation (113), and all quality statistics (task 3).
  - They never update real cooldown state, `max_alerts_per_hour` counters or lifecycle links of real alerts, and never create proposed actions.

### 2. Replay runs

- **Entity `exercise_run`:** `id`, `name`, `rule_ids[]` (or `tag`), `from`/`to` (time) or `fromSeq`/`toSeq`, `speed` (`realtime | x10 | x60 | max`), `channel_ids[]`, `enrich` bool, `status` (`PLANNED | RUNNING | DONE | CANCELLED | FAILED`), `created_by`, `started_at`, `finished_at`, `stats` jsonb (versions scanned, alerts by rule, notifications by channel, suppressed by cooldown or rate cap, `skippedRules`, caveats).
- **Bounds:** translate times to seq with `seq_clock` (`cutoffSeq`/`S(T)`). Reject a window below any involved source's floor (`RetentionState`, 019) with `400 REPLAY_BEYOND_RETENTION`, naming the source and its earliest replayable time. Maximum window `exercise.maxWindow` (7 d); one run at a time (`409 EXERCISE_RUNNING`).
- **Evaluation:** reuse the 103 rule backtest engine: `RuleSqlBuilder` per chunk with alert insertion disabled, then Java simulation in seq order of cooldown per `(rule, entity)` on `observed_at` (with `cooldownScope`), `max_alerts_per_hour` and active windows, all in **run-local** state. Enter/exit use the edge join on `prev_seq`; a missing predecessor at the window start fires nothing (record it as caveat `PREDECESSOR_OUTSIDE_WINDOW`). Windowed and anomaly rules (100) replay through 103's evaluators with the caveats 103 attaches; rules 103 can't replay go in `stats.skippedRules`.
- **Pacing:** emit matches in seq order. For `realtime`/`x10`/`x60`, sleep by the scaled difference of `ingested_at` between consecutive matches (cap any single sleep at 60 s ÷ speed factor; `max` = no sleep). Runs on a virtual thread and checks cancellation between matches.
- **Delivery:** each match becomes an alert with `exercise=true`, `exercise_run_id`, `observed_at` from the version and `record_snapshot`, published on the AlertBus and delivered through the normal 018 pipeline. Rate limits, quiet hours and folding apply; storm control counts exercise alerts separately.
- **Lifecycle:** `DELETE /v1/exercises/{id}/alerts` purges a run's alerts and notifications. Exercise alerts expire after `exercise.alertRetention` (7 d, `RetentionStep`).
- **API** (agent, `alwaysAuthenticated`):
  - `POST /v1/exercises` (create and start, or `dryRun=true` to return projected counts per rule and channel without delivering)
  - `GET /v1/exercises`, `GET /v1/exercises/{id}`, `POST /v1/exercises/{id}/cancel`, `DELETE /v1/exercises/{id}/alerts`
  - Audit events (023) for start, cancel and purge.
  - A `system` notification at start and finish to channels subscribed to system events, also marked exercise.

### 3. Feedback capture, implicit signals and trends

- **Capture everywhere:** feedback (`useful|noise|late|false_positive`, 017) must be settable from each interaction surface. Check each one and add what is missing:
  - 033 signed actions: add `feedback:useful` and `feedback:noise` action variants on ntfy, Telegram, Web Push and Matrix renderings.
  - 061 chat commands: `/feedback <alertId> <value>`.
  - MCP `set_alert_feedback` (agent) if 025 or 017 didn't add it.
  - Ask proposed actions (028).
- **Implicit signals** (non-exercise alerts only; stored in `alert_signal` (`alert_id`, `type`, `at`), never as feedback):
  - `ACK_FAST`: acknowledged within 15 min
  - `SNOOZED`: alert or rule snoozed
  - `FLAP`: resolved via `inverse_edge` within 10 min of firing
  - `UNOPENED`: notification not read within 24 h, only where 033 read cursors exist
- **Extend 103's `AlertQualityService`** (don't fork it): add implicit-signal counts and a `signalScore` (weights in one documented table: `ACK_FAST` +0.5, `SNOOZED` −0.5, `FLAP` −0.5, `UNOPENED` −0.25), shown **next to**, never mixed into, the feedback-based precision. Exclude exercise alerts.
- **Daily trends:** a nightly job writes `rule_quality_daily` (`rule_id`, `day`, `fired`, counts per feedback value, `signals` jsonb, `median_ack_seconds`, `flaps`, `suppressed_by_cooldown`, `suppressed_by_rate_cap`), retained 400 days. `GET /v1/triggers/{id}/quality?since` (103's endpoint) gains `daily[]` from this table.
- **Weekly quality digest** (optional briefing section, 029): rules with the lowest precision (≥ 10 labelled), noisiest rules by alerts per day, rules with pending suggestions. Off by default (`briefing.scope.includeAlertQuality`).

### 4. Tuning workflow

- **More suggestion kinds in 103's `TuningSuggester`** (bump its version and keep it deterministic):
  - `ACTIVE_WINDOW`: when ≥ 70% of negatives fall in hours that hold < 10% of positives, propose `active_from`/`until` or an active window covering the positive hours.
  - `SEVERITY_UP`: when `late` feedback ≥ 30% of labels on an `info` or `warning` rule, propose the next severity.
  - `SEVERITY_DOWN`: when `noise` ≥ 50% and no `useful` on a `critical` rule, propose `warning`.
- **Suggestion lifecycle:** persist suggestions in `tuning_suggestion` (`id`, `rule_id`, `rule_rev` (the rule's definition hash at generation), `kind`, `patch`, `rationale`, `expected_effect`, `status` `OPEN | APPLIED | DISMISSED | STALE`, `created_at`, `decided_by`, `decided_at`). Generation happens on `GET …/suggestions` (103) and in the nightly job, deduplicated by `(rule_id, kind, patch)`. A rule edit marks its open suggestions `STALE`.
- **Apply:** `POST /v1/triggers/{id}/suggestions/{suggestionId}/apply` (agent). It refuses with `409 SUGGESTION_STALE` when `rule_rev` no longer matches, then applies the patch through the normal `updateTrigger` path (same validation, same events) and writes `rule_tuning_log` (`rule_id`, `suggestion_id`, `before`, `after`, `applied_by`, `applied_at`).
- `POST /v1/triggers/{id}/suggestions/{suggestionId}/dismiss` (agent, optional reason).
- `POST /v1/triggers/{id}/tuning-log/{logId}/undo` (agent) restores `before` when the rule is unchanged since (`409` otherwise).
- `GET /v1/triggers/{id}/tuning-log` (read).
- **Agents and Ask:** MCP `apply_tuning_suggestion` (agent) and Ask present a suggestion as a **proposed action** (FR-46) showing the patch verbatim. It runs only on confirmation or for an agent-scope caller that allows actions.
- Optional `explain=true` on `GET …/suggestions`: a short plain-language summary via the 026 chat role (budgeted; never generates or edits patches; skipped without a provider).

### 5. Spec, MCP, docs

- `docs/OpenAPI.json`: `exercise` on `Alert`, the alert event schemas and `NotificationMessage`; `includeExercise` on `listAlerts`, `streamAlerts` and the events-stream alerts filter; `acceptExercise` on channel schemas; schemas `ExerciseRun`, `ExerciseRunCreate`, `ExerciseStats`, `AlertSignal`, `RuleQualityDaily`, `TuningSuggestionRecord` (extends 103's `TuningSuggestion` with id, status, `ruleRev`), `TuningLogEntry`; `RuleQuality` gains `signals`, `signalScore` and `daily`. Operations above, each with `x-elaro-scope`; error codes `REPLAY_BEYOND_RETENTION`, `EXERCISE_RUNNING`, `SUGGESTION_STALE`. Update `AccessPolicy`. Regenerate Postman with BODIES for `createExercise`, `applyTuningSuggestion`, `dismissTuningSuggestion`.
- MCP (025): `start_exercise`, `get_exercise` (agent), `apply_tuning_suggestion` (agent), `set_alert_feedback` (agent, if missing).
- `docs/Operator-Guide.md`:
  - "Exercises and replay": a rehearsal checklist, which channels to opt in, marking examples per channel type, retention limits on replay windows.
  - "Tuning alert quality": giving feedback from each channel, implicit signals, reading the quality view and trends, applying, dismissing and undoing suggestions.

## Tests to write (do not run)

- `ExerciseMarkingTest`: every channel renderer and push payload carries the marker; CAP status Exercise; push urgency downgraded.
- `ExerciseIsolationIT`: exercise alerts reach only opted-in or named channels; hidden from default lists, streams, digests, rollups, glance, escalation, federation and quality stats; real cooldown and rate counters untouched.
- `ReplayRunIT` (`TestRecords`): a window with a revision fires the same alerts as the live engine produced for that range (enter/exit/match); run-local cooldown; a missing predecessor at the window start fires nothing and adds the caveat; beyond floor → 400 naming the source; dry run counts without delivery; cancellation mid-run; purge removes alerts and notifications.
- `ReplayPacingTest` (`MutableClock`): sleeps scale with speed, single-sleep cap, `max` = no sleep.
- `FeedbackCaptureIT`: feedback via signed action, chat command and MCP lands in `alert.feedback`.
- `AlertSignalTest` and `RuleQualityDailyJobIT`: signal detection, weights, daily aggregation, exercise excluded, `daily[]` in the quality response.
- `TuningSuggesterExtensionTest`: `ACTIVE_WINDOW`, `SEVERITY_UP`, `SEVERITY_DOWN` fixtures; silence below sample minimums; deterministic output.
- `TuningWorkflowIT`: suggestion persisted and deduplicated; a rule edit marks it STALE and apply → 409; apply writes the tuning log and goes through `updateTrigger`; undo restores, and refuses after a later edit; dismiss.

## Acceptance criteria

- A replay of a retained window produces marked exercise alerts, delivered only to opted-in channels, and never touches real alert state or statistics.
- Feedback can be given from every surface. Quality views show feedback precision, implicit signals and daily trends.
- Suggestions are reviewed, applied only through an explicit update with an audit log, and can be undone.
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
2. **Commit** all changes with the message `115: Exercise Replay And Alert Quality`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/115-CR-Exercise-Replay-And-Alert-Quality.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `115: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
