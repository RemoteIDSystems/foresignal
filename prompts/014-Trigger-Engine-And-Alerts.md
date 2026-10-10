# 014 — Trigger Engine And Alerts

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/014-CR-Trigger-Engine-And-Alerts.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/013-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §8.3 (TriggerEngine), §7.1 (`trigger_rule`, `alert`)
- `docs/Product-Charter.md` §8.5 (FR-24 to FR-29), G5
- `docs/OpenAPI.json`: operations tagged `Triggers` and `Alerts`, schemas `TriggerRule`, `TriggerRuleInput`, `Condition`, `TriggerTestResult`, `Alert`, `AlertPage`

## Objective

The alerting layer: rules evaluated against the change stream, so a subscribed-but-hidden layer can announce itself. Alerts are durable, queryable, acknowledgeable and streamed live.

## Tasks

### 1. Rule model and validation (`ai.elaro.trigger`)

- `TriggerRuleInput` validation:
  - `name` 1–200 chars
  - at least one of `source`, `kind`, `conditions` (non-empty) or `geofence`
  - `source` must be a registered id if given; `kind` must be a known kind if given (warn but allow unknown kinds; record the decision)
- Each `Condition`:
  - `path` matches `^[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)*$`
  - `op` ∈ `> >= < <= == !=`
  - `value` is a number, string or boolean; ordering ops require a number
- `geofence`: GeoJSON Polygon or MultiPolygon, valid (use `GeometryFixer`, reject if the result is empty), stored as MultiPolygon with SRID 4326; vertex limit 10,000.
- `mode` ∈ `match` (default), `enter`, `exit`. Edge modes need a geofence or at least one condition; otherwise there is no predicate to transition on, so return 400.
- `cooldown` uses duration shorthand (default `0`); `messageTemplate` is ≤ 1000 chars.

### 2. RuleSqlBuilder

Compile a rule into **one parameterized SQL statement** over `record_version v` for a seq range `(:fromSeq, :toSeq]`:

- source/kind filters → `v.source_id = :source`, `v.kind = :kind`
- numeric condition → `(CASE WHEN jsonb_typeof(v.payload #> :pathN) = 'number' THEN (v.payload #>> :pathN)::numeric END) <op> :valueN`, where `:pathN` is a `text[]`
- string `==` / `!=` → `(v.payload #>> :pathN) = :valueN` (for `!=`, treat a missing value as not matching, so add `IS NOT NULL`)
- boolean `==` / `!=` → `jsonb_typeof(...) = 'boolean' AND (v.payload #>> :pathN)::boolean = :valueN`
- geofence → `v.geom IS NOT NULL AND ST_Intersects(v.geom, (SELECT geofence FROM trigger_rule WHERE id = :ruleId))`, or pass the WKB as a parameter
- `onlyNew` → `v.version = 1`
- **`mode`** (see Architecture §8.3). Render the predicate `P` (conditions plus geofence) as a function of a table alias, so it can be applied to both `v` and the previous version `p`. Join `LEFT JOIN record_version p ON p.source_id = v.source_id AND p.external_id = v.external_id AND p.version = v.version - 1`.
  - `match` → `P(v)`
  - `enter` → `NOT v.tombstone AND P(v) AND (v.version = 1 OR (p.seq IS NOT NULL AND NOT P(p)))`
  - `exit` → `p.seq IS NOT NULL AND P(p) AND (v.tombstone OR NOT P(v))`
  - A missing `p` with `v.version > 1` (removed by retention) never fires an edge rule.
  - For `exit`, the "tombstones never match" rule does not apply: a tombstone after an inside version is an exit.
- `NOT v.tombstone` (for `match` and `enter`)
- cooldown (when > 0) → `NOT EXISTS (SELECT 1 FROM alert a WHERE a.rule_id = :ruleId AND a.source_id = v.source_id AND a.external_id = v.external_id AND a.fired_at > :now - :cooldown)`
- `ORDER BY v.seq`

Operators come from a fixed whitelist map; values and paths are always bound parameters. Unit-test the generated SQL text and parameters.

### 3. TriggerEngine

- A single dedicated virtual-thread loop subscribed to `ChangeBus` (queue capacity 1024).
- **Durable cursor:** `app_setting['trigger.cursor']` stores the last fully processed seq.
  - On startup, catch up from the cursor to `ChangeBus.lastPublishedSeq()` in chunks of 10,000 seq.
  - On `onGap`, catch up from the cursor.
  - Ignore any `ChangeBatch` with `toSeq ≤ cursor`.
- For each batch or chunk, evaluate the enabled rules whose source/kind could match. Pre-filter in memory using the batch summaries (sources and kinds present) to skip irrelevant rules.
- For each match, in one transaction per chunk:
  - insert an `alert` with `fired_at = clock.now()` and a rendered message
  - advance the cursor in the same transaction
- After commit, publish each `Alert` to an `AlertBus` with listeners (the alert SSE channel, and webhooks in prompt 015).
- Rule set: cache the enabled rules in memory and refresh on `TriggerRulesChangedEvent`.
- Isolation: a failing rule (SQL error) is logged, marked with `last_error` (add `last_error`, `last_error_at` columns to `trigger_rule` and record this in the CR) and skipped. It must never stall the engine.

### 4. Message templates (`MessageTemplate`)

- Placeholders: `{{name}}`, `{{source}}`, `{{externalId}}`, `{{kind}}`, `{{observedAt}}`, `{{payload.<dot.path>}}`.
- Missing values render as an empty string. No expression evaluation, no recursion, output ≤ 2000 chars.
- Default template: `"{{name}}: {{source}}/{{externalId}}"`.

### 5. APIs (controllers matching the spec exactly)

**Triggers:**

- `listTriggers`, `getTrigger`
- `createTrigger` → 201 with `Location: /v1/triggers/{id}`
- `updateTrigger` → full replace
- `deleteTrigger` → 204; existing alerts are kept and `rule_id` stays as is (no FK cascade; document this)
- `testTrigger`:
  - evaluates the rule over `record_version` with `ingested_at >= :since` (default now-24h), ignoring cooldown, capped at `limit`
  - returns `TriggerTestResult` with full `Record` DTOs
  - creates nothing
- The `TriggerRule` response includes `lastFiredAt` and `alertCount` (aggregate query, or counters maintained on the rule row; choose and record).

**Alerts:**

- `listAlerts`: newest first; keyset cursor on id; filters `ruleId`, `source`, `since`, `acknowledged`.
- `getAlert`: includes the `record` (the version at `alert.seq`).
- `acknowledgeAlert`: sets `acknowledged_at`; idempotent.
- `streamAlerts`: SSE via `SseChannel<Alert>` from prompt 012.
  - event `alert`, `id` = alert id
  - replay from the `alert` table by id for `Last-Event-ID`
  - filter by `ruleId` (repeatable)
  - heartbeat

## Tests to write (do not run)

- `RuleValidatorTest`: every validation rule.
- `RuleSqlBuilderTest`: SQL and parameters for each condition type, geofence, onlyNew, cooldown, each `mode`, combinations. No interpolation of values.
- `MessageTemplateTest`: placeholders, nested paths, missing values, length cap.
- `TriggerEngineIT`:
  - ingest quakes and get an alert for a `mag >= 6` rule only for matching records
  - geofence rule
  - onlyNew fires once across versions
  - **enter/exit:** a moving point (successive versions) outside → inside → inside → outside produces exactly one `enter` alert and one `exit` alert; a record created inside fires `enter`; a tombstone after inside fires `exit`; a missing predecessor (deleted) fires nothing
  - a threshold rule in `enter` mode fires when `mag` first crosses 6, not on later revisions that stay above 6
  - cooldown suppresses re-fires within the window and allows them after (`MutableClock`)
  - the cursor survives a restart: alerts are created exactly once after a simulated restart
  - `onGap` catch-up
  - a broken rule doesn't block others
  - tombstones never match
- `TriggersControllerIT` / `AlertsControllerIT`: CRUD, 401s, validation 400s, test endpoint, list filters and pagination, ack.
- `AlertStreamIT`: a live alert is delivered ≤ 5 s after the ingest that triggered it; resume works.

## Acceptance criteria

- All `Triggers` and `Alerts` operations match `docs/OpenAPI.json`.
- An alert is created exactly once per (rule, matching version), subject to cooldown, including across restarts.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 040.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `014: Trigger Engine And Alerts`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/014-CR-Trigger-Engine-And-Alerts.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `014: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
