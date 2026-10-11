# 017 — Trigger Engine And Alerts

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/017-CR-Trigger-Engine-And-Alerts.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/016-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §1 (I8), §5.2 (row 9, `TRIGGER_EDGE`), §8 (`alert`, `trigger_rule` additions), §11 (triggers, **implement exactly**), *Per-prompt changes* → the *Trigger Engine And Alerts* entry, *Spec changes*
- `docs/Weather-Imagery-Design.md` §7.1 (derived records use enter/exit), §7.3 (`cooldownScope`, `near`)
- `docs/Gap-Analysis.md` §1 items 2 and 6, §5 rows "Watchlists + set operators", "Forecast-aware alerts", "Case files / alert status", §8.A item 7 ("Extend 014" there; 014 is now this prompt)
- `docs/Architecture.md` §8.3, §7.1 (`trigger_rule`, `alert`); `docs/Product-Charter.md` §8.5 (FR-24 to FR-29), G5
- `docs/OpenAPI.json`: operations tagged `Triggers` and `Alerts`, schemas `TriggerRule`, `TriggerRuleInput`, `Condition`, `TriggerTestResult`, `Alert`, `AlertPage`
- Completion Reports of 007 (`alert.observed_at`, `record_snapshot`, `trigger_rule.reappear_as_enter`), 008 (`PersistGate`, `GateSpec`, `NoopPersistGate`, `validFrom`), 009 (`ChangeBus`, `TestRecords`), 014 (`SeqClockQuery`, `RetentionState`), 015 (`SseClient`, `EventTopic`)

## Objective

The alerting layer: rules evaluated against persisted versions, so a subscribed-but-hidden layer can announce itself. The persist gate guarantees that every predicate flip is persisted (I8), so edges are exact even under SAMPLED policies. Alerts have a lifecycle (firing, acknowledged, resolved), feedback and snooze, and rules can watch sets of values across several sources, within time windows and route corridors, with storm protection.

## Tasks

### 1. Rule model and validation (`ai.foresignal.trigger`)

Extend `TriggerRuleEntity` (add columns via Hibernate) and `TriggerRuleInput`:

- `name` 1–200 chars; `enabled`; `tags` (≤ 16, each `^[a-z0-9][a-z0-9_-]{0,31}$`), copied to alerts.
- `sources` and `kinds` (arrays, ≤ 50 each). Keep scalar `source`/`kind` as deprecated aliases that normalise into the arrays (both given → 400). Store as jsonb. Unknown sources → 400; unknown kinds warn and are allowed.
- At least one of `sources`, `kinds`, `conditions`, `geofence` or `near`.
- **Conditions** `{path, op, value}`, `path` `^[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$`. Ops from a fixed `OperatorRegistry` (036 adds `inWatchlist` there without touching the engine):
  - `> >= < <=` (number value); `== !=` (number, string or boolean)
  - `in`, `notIn`: non-empty homogeneous array of scalars, ≤ 1,000 values
  - `exists`: boolean value (true = present and not JSON null)
  - `startsWith`: non-empty string ≤ 256
  - `matches`: regex ≤ 256 chars; must compile in Java; reject backreferences, lookaround and nested quantifiers like `(a+)+`. Run SQL with the statement timeout and the gate with a deadline-checking `CharSequence` (10 ms).
  - **Volatile paths:** a path whose root is in `volatileFields` of any applicable source (named sources, or every source emitting a named kind) → 400 `TRIGGER_PATH_VOLATILE`.
- **Geofence:** GeoJSON Polygon/MultiPolygon, or LineString/MultiLineString with `bufferMeters` (1–50,000) buffered server-side as `ST_Buffer(line::geography, :m)::geometry`. Fix with `GeometryFixer` (empty → 400), split at the antimeridian, store as MultiPolygon 4326 in `geofence`, keep the original line in `geofence_route` (GeoJSON) for display. ≤ 10,000 vertices.
- **`hysteresisMeters`** (0–5,000, default 0; geofence and `near` rules with `enter`/`exit` only): an `enter` edge requires the position inside the fence shrunk by the band (`ST_Buffer(fence::geography, −h)`), an `exit` edge requires it outside the fence grown by the band, and edges per `(rule, entity)` must alternate, using the per-entity edge state the cooldown already keeps. GPS or AIS jitter along a boundary therefore produces no edges. Validation: the shrunk fence must not be empty (400 `GEOFENCE_TOO_SMALL_FOR_HYSTERESIS`). Add `hysteresisMeters` to the spec; test with a jittering track along a boundary (no edges) and a real crossing (one `enter`, one `exit`).
- **`near`** `{point: [lon, lat] | placeId, radiusKm (0.1–2000)}`, materialised into `geofence` as `ST_Buffer(point::geography, r)`; mutually exclusive with `geofence`. `placeId` needs a `PlaceLookup` bean (prompt 036, which also re-materialises on place edits); without one → 400 `PLACES_UNAVAILABLE`.
- `mode` `match` (default) | `enter` | `exit`; edge modes need a geofence or a condition (else 400). `reappearAsEnter` default true when the rule has a geofence, else false. `onlyNew` (version = 1).
- `cooldown` (duration, default 0) and `cooldownScope` `record` (default) | `rule`.
- `severity` `info|warning|critical` (default warning); `enrich`, `enrichRadiusKm` (1–1000, default 100), `enrichWindow` (default 24h) stored for prompt 030.
- `autoResolve` `{onInverseEdge, onTombstone, onExpiry, staleAfter}`; default all true (staleAfter null) for `enter` rules, null (off) for `match`; not allowed on `exit` rules (400).
- `maxAlertsPerHour` (nullable → app setting `triggers.defaultMaxAlertsPerHour`, default 120; 0 = unlimited) and `autoDisableOnStorm` (default false).
- `activeFrom`, `activeUntil` (timestamptz, nullable) and `activeWindow` `{timezone (IANA), windows: [{days: [MON…SUN], start: "HH:mm", end: "HH:mm"}]}`; `end < start` spans midnight.
- `forecastHorizon` (duration, nullable). Requires `record_version.valid_from` (008/009); if that column is absent, reject with 400 `FEATURE_UNAVAILABLE` and record it.
- `messageTemplate` ≤ 1000 chars. `snoozedUntil` is read-only here (set through the snooze endpoint).

### 2. RuleSqlBuilder

One parameterized statement per rule over `record_version v` for `seq ∈ (:fromSeq, :toSeq]`, **excluding `HEARTBEAT`** (`v.persist_reason <> 8`). Render `P(alias)` (conditions + geofence) for `v` and `p`:

- `source_id = ANY(:sources)`, `kind = ANY(:kinds)`
- numeric: `(CASE WHEN jsonb_typeof(a.payload #> :pN) = 'number' THEN (a.payload #>> :pN)::numeric END) <op> :vN`
- string `==`/`!=`: `(a.payload #>> :pN) = :vN` (`!=` adds `IS NOT NULL`); boolean with a `jsonb_typeof = 'boolean'` guard
- `in`/`notIn`: typed guard plus `= ANY(:vN)` / `<> ALL(:vN)` (`notIn` requires presence); `exists`: `a.payload #> :pN IS NOT NULL AND jsonb_typeof(…) <> 'null'`; `startsWith`: `left(a.payload #>> :pN, :len) = :vN`; `matches`: `(a.payload #>> :pN) ~ :vN`
- geofence: `a.geom IS NOT NULL AND ST_Intersects(a.geom, :fenceWkb)`
- `forecastHorizon`: `(v.valid_from IS NULL OR v.valid_from <= :evalTime + :horizon)`
- **Edge join on seq only:** `LEFT JOIN record_version p ON p.seq = v.prev_seq`.
  - `match` → `NOT v.tombstone AND P(v)`
  - `enter` → `NOT v.tombstone AND P(v) AND (v.prev_seq IS NULL OR p.tombstone OR (:reappearAsEnter AND v.persist_reason = 2) OR (p.seq IS NOT NULL AND NOT P(p)))`
  - `exit` → `p.seq IS NOT NULL AND NOT p.tombstone AND P(p) AND (v.tombstone OR NOT P(v))`
  - A missing `p` (retention) fires nothing except via FIRST or REAPPEAR. Expiry itself is not an exit.
- **Cooldown seed on `observed_at`:** record scope `NOT EXISTS (SELECT 1 FROM alert a WHERE a.rule_id = :rule AND a.source_id = v.source_id AND a.external_id = v.external_id AND NOT a.suppressed AND a.observed_at > v.observed_at - :cooldown)`; rule scope (Weather §7.3) drops the entity terms. The §7.3 `ROW_NUMBER() … = 1` reduction is replaced by the exact Java pass in task 3, because Core-Data-Model §11 puts cooldown on `observed_at`.
- `ORDER BY v.seq`. Operators come from the registry; values and paths are always bound. Unit-test SQL text and parameters.

### 3. TriggerEngine and the gate

- **Engine:** one virtual-thread loop on `ChangeBus` (queue 1,024). Durable cursor `app_setting['trigger.cursor']`: catch up from it on start and on `onGap` in 10,000-seq chunks; ignore batches with `toSeq ≤ cursor`. Pre-filter rules in memory by the batch's sources and kinds. Rules outside `activeFrom/activeUntil` or `activeWindow` (evaluated on the version's `observed_at` in the rule's timezone with `ZonedDateTime`, DST-safe) are skipped per row.
- **Java pass** per rule, in seq order: cooldown per (rule, entity) or per rule on `observed_at`; alert-level snooze (an unexpired `snoozed_until` on an open alert of the same rule and entity) → insert with `suppressed = true, suppressed_reason = 'alert_snoozed'`; rule snooze → `suppressed_reason = 'rule_snoozed'`; then the **storm cap**: a per-rule sliding one-hour window of unsuppressed alerts (seeded from the DB at start). Over the cap, do not insert; increment `trigger_rule.suppressed_count`, set `last_storm_at`, publish `RuleStormEvent` once per window (prompt 018 routes it as a system event), and disable the rule when `autoDisableOnStorm`.
- **Insert** in one transaction per chunk with the cursor update: `fired_at = clock.now()`, `observed_at = v.observed_at`, `record_snapshot` (kind, version, observedAt, GeoJSON geometry, payload; ≤ 16 KB, else drop the payload and set `truncated: true`), rendered message, severity, tags, `state = 'firing'`, `rev = nextval('alert_rev')`.
- **Auto-resolve:** for rules with `onInverseEdge`/`onTombstone`, run the rule's exit predicate (`P(p) AND (v.tombstone OR NOT P(v))`, tombstone-only when just `onTombstone`) over the same range, restricted to entities with an open alert of that rule, and resolve those alerts (`resolved_by = 'inverse_edge'|'tombstone'`, `resolved_seq = v.seq`). `AlertResolver` runs every 60 s for `onExpiry` (`record_live.valid_until < now()` or the head is gone → `expiry`) and `staleAfter` (`l.observed_at < now() − staleAfter` → `stale`). It also disables rules past `activeUntil` and clears expired rule snoozes, publishing `TriggerRulesChangedEvent`.
- After commit, publish `AlertEvent(type: fired|updated|resolved, alert, suppressed)` on `AlertBus` (small listener registry, per-listener async delivery, same pattern as `ChangeBus`). Listeners: the alert stream now, notification channels (018), enrichment (030). Suppressed alerts are published with the flag so 018 does not route them.
- **`TriggerPersistGate`** (`@Primary`, implements 008's `PersistGate`): keeps a `GateSnapshot` (`AtomicReference`) of enabled rules compiled to JTS `PreparedGeometry` and path evaluators, plus `GateSpec` per source (payload paths, `needsGeometry`), rebuilt on `TriggerRulesChangedEvent`. For each applicable rule it compares P(persisted: `h.payload` paths + `h.persisted_geom`) with P(candidate: incoming snapshot + carried-forward geometry) and reports a flip. Evaluation is exact, using the same operator semantics as `RuleSqlBuilder`; when either side's JSON type differs from the operator's expected type, or a value is missing, report a flip iff before ≠ candidate (conservative). `edgeMinInterval` is applied by the 008 policy engine, not here.
- **Shared fixture** `src/test/resources/trigger/operator-truth-table.json`: rows `{op, value, payload, expected}` covering every operator, type mismatches, missing paths and JSON null. `RuleSqlBuilderIT` and `TriggerPersistGateTest` both consume it.
- Isolation: a failing rule is logged, gets `last_error`/`last_error_at`, and is skipped; it never stalls the engine.

### 4. Message templates (`MessageTemplate`)

Placeholders `{{name}}`, `{{source}}`, `{{externalId}}`, `{{kind}}`, `{{title}}`, `{{severity}}`, `{{observedAt}}`, `{{eventTime}}`, `{{time}}` (effective time), `{{validFrom}}`, `{{payload.<dot.path>}}`. Missing → empty; no expressions, no recursion; output ≤ 2000 chars. Default `"{{name}}: {{source}}/{{externalId}}"`. Prompt 018 adds timezone and filters on top.

### 5. Alert lifecycle and APIs

Alert columns: `state` (`firing|acknowledged|resolved`), `acknowledged_at`, `acknowledged_by`, `resolved_at`, `resolved_by` (`inverse_edge|tombstone|expiry|stale|token:<name>|admin`), `resolved_seq`, `feedback` (`useful|noise|late|false_positive`), `feedback_at`, `note` (≤ 2000), `snoozed_until`, `suppressed`, `suppressed_reason`, `tags`, `observed_at`, `record_snapshot`, `rev`. Indexes `(rule_id, source_id, external_id, observed_at)`, `(rule_id, observed_at)`, `(rev)`, and a partial index on open alerts (`state <> 'resolved'`) through `SchemaBootstrap` SQL. `*_by` is the token name from the security context (single-operator model, no user directory).

- **Triggers:** `listTriggers` (+ `tag`, `enabled` filters), `getTrigger`, `createTrigger` (201 + `Location`), `updateTrigger` (full replace), `deleteTrigger` (204; alerts kept, no cascade). Responses add `lastFiredAt`, `alertCount`, `openAlertCount`, `suppressedCount`, `lastStormAt`, `feedbackCounts`. `POST /v1/triggers/{id}/snooze` `{until | for}` (≤ 7 days, required expiry) and `DELETE …/snooze`.
- **`testTrigger` (backtest):** `fromSeq`/`toSeq` or `since`/`until` (translated to seq via `seq_clock`; default last 24 h), `limit` (≤ 1,000), `simulate` (default false). Without `simulate`, return raw matches ignoring cooldown, snooze and the cap; with it, run the Java pass (cooldown, cap, active windows) in seq order. Evaluate in 50,000-seq chunks under the statement timeout. Response adds `fromSeq`, `toSeq`, `matchCount`, `wouldFire`, `wouldSuppress`, and `truncatedByRetention` when `fromSeq < RetentionState.lowestRetainedSeq`. Creates nothing.
- **Alerts:** `listAlerts` newest first, keyset on id; filters `ruleId`, `source`, `kind`, `since`, `acknowledged`, `state`, `feedback`, `minSeverity`, `tag`, `includeSuppressed` (default false). `getAlert` loads the version by `seq = ?` and falls back to `record_snapshot`. `acknowledgeAlert` (optional body `{note, feedback}`; idempotent; no-op on resolved). New `POST /v1/alerts/{id}/resolve` `{note, feedback}`, `POST /v1/alerts/{id}/snooze` `{until | for}`, `DELETE /v1/alerts/{id}/snooze`, `PUT /v1/alerts/{id}/feedback` `{feedback, note}`. Resolved is terminal; the next enter edge fires a new alert, and cooldown still applies from the resolved alert's `observed_at`.
- **`streamAlerts`:** reuse 015's `SseClient`. `id` = alert `rev`; events `alert` (fired), `alert.updated`, `alert.resolved`. Replay rows with `rev > Last-Event-ID` in their current state (clients upsert by alert id). Filters `ruleId`, `minSeverity`, `source`, `kind`, `includeSuppressed`. Register an `alerts` `EventTopic` (cursor `a` = rev; filters `alerts.minSeverity|ruleId|source|kind`) on 015's `/v1/events/stream`.
- Design services so prompt 025 can expose `set_alert_status`, `snooze_rule` and `test_trigger` as MCP tools without web types.

### 6. Spec, Postman, docs

- `docs/OpenAPI.json`: `TriggerRuleInput`/`TriggerRule` (+ every field in task 1; `Condition.op` enum with the new operators and array values; `geofence` accepts lines with `bufferMeters`; `near`), `Alert` (+ lifecycle fields, `observedAt`, `recordSnapshot`, `tags`), `TriggerTestResult` (+ backtest fields), alert event schemas (`AlertEvent` with `type`). New operations `snoozeTrigger`, `unsnoozeTrigger`, `resolveAlert`, `snoozeAlert`, `unsnoozeAlert`, `setAlertFeedback`, each `"x-foresignal-scope": "agent"`; new parameters on `listTriggers`, `testTrigger`, `listAlerts`, `streamAlerts`. Error codes `TRIGGER_PATH_VOLATILE`, `PLACES_UNAVAILABLE`, `FEATURE_UNAVAILABLE`.
- `AccessPolicy`: `POST /v1/alerts/*/resolve`, `POST|DELETE /v1/alerts/*/snooze`, `PUT /v1/alerts/*/feedback` → AGENT, above the `GET /v1/alerts/**` rule (`/v1/triggers/**` writes are already AGENT). Extend `AccessPolicyTest`.
- Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`); add `BODIES` for `createTrigger` (a `kinds` array with an `in` condition and a route geofence), `snoozeTrigger`, `resolveAlert`, `snoozeAlert`, `setAlertFeedback`.
- `docs/Agent-Guide.md` "Rules" section: prefer `enter`/`exit` or cooldown on SAMPLED sources and derived records; flush or poll resolution; edges need the predecessor version to be retained; a tombstone after an inside version is an exit; `forecastHorizon` fires on new versions only.

## Tests to write (do not run)

Seed only through `TestRecords`.

- `RuleValidatorTest`: every rule above, including volatile paths, regex rejection, line buffers, `near` without Places, `autoResolve` on exit rules.
- `RuleSqlBuilderTest`: SQL and parameters per operator, geofence, onlyNew, both cooldown scopes, each mode, HEARTBEAT exclusion, seq-only join; no value interpolation.
- `RuleSqlBuilderIT` and `TriggerPersistGateTest`: the shared truth table; gate conservative on type mismatch.
- `MessageTemplateTest`, `ActiveWindowTest` (both DST transitions with `MutableClock`).
- `TriggerEngineIT`:
  - `mag >= 6` rule; `kinds` array with `in`; `startsWith` on callsign
  - **geofence crossing between samples** (SAMPLED source) → `TRIGGER_EDGE` versions plus one enter and one exit alert
  - **rule created while live and persisted diverge** → converges on the next observation
  - flapping at a threshold bounded by `edgeMinInterval`
  - **HEARTBEAT does not re-fire match**
  - **tombstone, then REAPPEAR inside, fires enter**; a missing prev fires nothing
  - deterministic cooldown on `observed_at` (record and rule scope)
  - auto-resolve on inverse edge, tombstone and expiry; `alert.resolved` published
  - storm cap suppresses, counts and emits `RuleStormEvent` once; rule and alert snooze record suppressed alerts
  - cursor survives restart (exactly once); `onGap` catch-up; a broken rule does not block others
- `TriggersControllerIT` / `AlertsControllerIT`: CRUD, 401s, 400s, backtest with `fromSeq/toSeq` and `simulate`, lifecycle endpoints, filters and pagination.
- `AlertStreamIT`: fired, updated and resolved events ≤ 5 s; `rev` resume; `minSeverity` filter; alerts topic on `/v1/events/stream`.

## Acceptance criteria

- All `Triggers` and `Alerts` operations match `docs/OpenAPI.json`.
- An alert is created exactly once per (rule, matching version), subject to cooldown, snooze and the storm cap, including across restarts.
- `TriggerPersistGate` is the `@Primary` `PersistGate` and passes the shared truth table.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `017: Trigger Engine And Alerts`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/017-CR-Trigger-Engine-And-Alerts.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `017: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
