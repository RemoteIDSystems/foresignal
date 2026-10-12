# 017-CR-Trigger-Engine-And-Alerts

| | |
|---|---|
| Prompt | 017-Trigger-Engine-And-Alerts.md |
| Status | Complete |
| Implementation commit | fb25f138ce29aec7e12ffdc9303378c0747dca16 |
| Date | 2026-10-11 |

## Summary

This prompt builds the alerting layer: rules evaluated against persisted versions, the persist gate
that makes every predicate flip a version (I8), and alerts with a lifecycle. The 16 operations
assigned to 017 in `docs/OpenAPI.json` are implemented and marked `x-foresignal-status: implemented`:

- `listTriggers`, `getTrigger`, `createTrigger`, `updateTrigger`, `deleteTrigger`, `testTrigger`
- `snoozeTrigger`, `unsnoozeTrigger`
- `listAlerts`, `getAlert`, `streamAlerts`
- `acknowledgeAlert`, `resolveAlert`, `snoozeAlert`, `unsnoozeAlert`, `setAlertFeedback`

The `alerts` topic of `/v1/events/stream` is registered too.

**Rule model (`trigger/RuleValidator`, `ValidatedRule`, `RuleSpec`, `RuleSpecs`).**

- The body is read as a JSON tree, so every field problem is reported in one `400` with field
  errors. Four findings have their own code and win over the rest: `TRIGGER_PATH_VOLATILE`,
  `PLACES_UNAVAILABLE`, `FEATURE_UNAVAILABLE` and `GEOFENCE_TOO_SMALL_FOR_HYSTERESIS`.
- Every task 1 field is covered:
  - `name`, `enabled`, `tags`;
  - `sources`/`kinds`, with the deprecated `source`/`kind` folded in (both given is `400`);
  - an unknown source is `400`; an unknown kind is logged as a warning;
  - the at-least-one-filter rule;
  - conditions, a geofence or `near`;
  - `mode`, `reappearAsEnter`, `onlyNew`;
  - `cooldown` and `cooldownScope`, `severity`, the `enrich` fields;
  - `autoResolve`, `maxAlertsPerHour`, `autoDisableOnStorm`;
  - `activeFrom`/`activeUntil`, `activeWindow`;
  - `forecastHorizon` (`record_version.valid_from` exists, checked once through
    `information_schema`);
  - `messageTemplate`.
- `TriggerRuleEntity` gained the columns through Hibernate. The scalar `source_id`/`kind` fields
  are gone from the entity (see Deviation 2).
- **Operators (`Operator`, `OperatorRegistry`, `Operators`).** The registry holds the built-ins plus
  every `Operator` bean, so 036 adds `inWatchlist` by declaring a bean.
  - Each operator has its SQL and its Java meaning in one class.
  - `matches` goes through `RegexGuard` (no backreferences, no `(?…)` except `(?:…)`, no nested or
    possessive quantifiers, a short escape whitelist) and, in Java, a `DeadlineCharSequence` with a
    10 ms budget.
- **Geofences (`GeofenceMaterializer`, `Antimeridian`).**
  - Polygons are fixed with JTS `GeometryFixer`.
  - Lines are buffered as `ST_Buffer(line::geography, :m)::geometry`, and `near.point` the same way
    by its radius.
  - The hysteresis band is a negative or positive geography buffer, stored as `geofence_inner` and
    `geofence_outer`.
  - Everything is split at the antimeridian and stored as MultiPolygon 4326; a route keeps its line
    in `geofence_route`.
  - `near.placeId` resolves through an optional `PlaceLookup` bean (036); without one it is
    `PLACES_UNAVAILABLE`.

**`RuleSqlBuilder`.**

- One statement per rule over `(fromSeq, toSeq]`, excluding HEARTBEAT (`v.persist_reason <> 8`).
- P is wrapped in `COALESCE(…, false)`; edge modes use `LEFT JOIN record_version p ON p.seq = v.prev_seq`.
- `match`, `enter` and `exit` follow Core-Data-Model §11 literally, including the tombstone and
  `reappearAsEnter` terms.
- Geofence: `ST_Intersects(a.geom, ST_GeomFromWKB(:fence))`, with the WKB bound.
- `onlyNew` and `forecastHorizon` filters.
- The cooldown seed is a `NOT EXISTS` on `observed_at`, in record or rule scope.
- An auto-resolve statement (`inverse`).
- A hysteresis rule selects both edges (enter against the shrunk fence, exit against the grown one)
  with an `edge` column.
- Every path and value is bound.

**`TriggerEngine`.**

- One `ChangeBus` subscription (queue 1,024).
- The durable cursor `app_setting['trigger.cursor']`. A fresh installation starts at the current
  head.
- Catch-up in 10,000-seq chunks on start, on `onGap`, and whenever a batch does not follow the
  cursor directly. A batch that follows directly is evaluated only with the rules whose source and
  kind filters can match it.
- Per chunk:
  - each rule's statements run in their own read transaction under the statement timeout
    (`ReadQueryRunner`); a failing rule gets `last_error` and is skipped;
  - `AlertPass` applies, in seq order: hysteresis alternation, active period and window, cooldown
    on `observed_at`, alert snooze, rule snooze, and the storm cap;
  - one transaction then inserts the alerts, resolves inverse edges and tombstones, updates the
    storm counters (and disables on storm) and advances the cursor.
- Events are published after commit. `RuleStormEvent` is published once per storm window.

**The gate (`TriggerPersistGate`, `@Primary`).**

- A `Compiled` snapshot in an `AtomicReference`, rebuilt on `TriggerRulesChangedEvent`,
  `SourceRegistryChangedEvent`, at start (new `StartupOrder.TRIGGER_GATE = 5`, before the writer)
  and on the test cleaner's reset.
- `GateSpec` per source: condition paths, plus `needsGeometry` when a fence applies.
- Exact evaluation with the same operators. Geofences use `PreparedGeometry.intersects`; a
  hysteresis rule is checked against both band fences.
- Conservative: a condition whose value is missing or of another type on either side flips iff the
  raw values differ.

**Alerts.**

- `AlertEntity` gained the lifecycle columns and the indexes `(rule_id, source_id, external_id,
  observed_at)`, `(rule_id, observed_at)` and `(rev)`.
- `SpatialIndexInitializer` (the `SchemaBootstrap` SQL step) creates the `alert_rev` sequence and
  the partial index `alert_open_idx … WHERE state <> 'resolved'`.
- `AlertStore` does all alert SQL. It serializes writes (see Issue 1).
- `AlertService` owns the lifecycle: idempotent ack, a no-op on resolved alerts, resolve terminal
  and idempotent, snoozes, feedback in any state.
- `AlertResolver` runs every 60 s:
  - `expiry`: live row expired, or head gone;
  - `stale`;
  - disables rules past `activeUntil` and clears expired rule snoozes.
- `AlertBus`: a per-listener queue and virtual thread, gap reporting, the `ChangeBus` pattern.
- `MessageTemplate`: every placeholder of task 4, a 2,000-character cap, no recursion.
- Record snapshots are at most 16 KB, otherwise the payload is dropped and `truncated: true` is set.

**API.**

- `TriggersController`, `AlertsController` (including `streamAlerts`), `TriggerMapper`,
  `AlertMapper`, and `api/support/Actors` (`admin` for the admin token).
- `stream/sse/AlertsTopic`:
  - cursor key `a`, cursor = `rev`;
  - replays the rows with `rev > cursor` in their current state;
  - filters `minSeverity`, `ruleId`, `source`, `kind`, `includeSuppressed`.
- `AccessPolicy`: `POST /v1/alerts/*/resolve`, `POST|DELETE /v1/alerts/*/snooze` and
  `PUT /v1/alerts/*/feedback` are AGENT, above `GET /v1/alerts/**`.
- `TriggerSettings` (`triggers.defaultMaxAlertsPerHour`, default 120) is stored under the new
  `SettingKeys.TRIGGERS`.

**Spec, Postman, docs.**

- The spec reconciliations in Deviation 8, recorded in `info.x-foresignal-changelog`.
- Postman was regenerated with `BODIES` for `createTrigger` (a `kinds` array, an `in` condition and
  a route geofence), `snoozeTrigger`, `resolveAlert`, `snoozeAlert` and `setAlertFeedback`.
- `docs/Agent-Guide.md` gains a "Rules and alerts" section, and its streaming section now lists
  `alerts`.

## Files created / modified

**Created (main)**

- `trigger/`:
  - the model: `Severity`, `AlertState`, `Feedback`, `CooldownScope`, `AutoResolve`, `ActiveWindow`
  - operators: `Operator`, `Evaluation`, `SqlTerm`, `Operators`, `OperatorRegistry`, `RegexGuard`,
    `DeadlineCharSequence`
  - rules: `RuleCondition`, `RuleSpec`, `RuleSpecs`, `ValidatedRule`, `RuleValidator`, `JsonTrees`
  - geofences: `PlaceLookup`, `Antimeridian`, `GeofenceMaterializer`
  - evaluation: `MessageTemplate`, `RuleSqlBuilder`, `MatchRow`, `ResolveRow`, `AlertPass`,
    `AlertSnapshots`, `TriggerEngine`, `TriggerPersistGate`
  - alerts: `AlertView`, `AlertEvent`, `AlertListener`, `AlertBus`, `AlertQuery`, `AlertStore`,
    `AlertService`, `AlertResolver`, `Snoozes`
  - events and services: `TriggerRulesChangedEvent`, `RuleStormEvent`, `TriggerRuleService`,
    `TriggerBacktester`
- `api/TriggersController`, `api/AlertsController`, `api/mapper/TriggerMapper`,
  `api/mapper/AlertMapper`, `api/support/Actors`
- `stream/sse/AlertsTopic`, `settings/TriggerSettings`

**Modified (main)**

- `store/TriggerRuleEntity`, `store/AlertEntity`
- `store/query/RecordQueryService` (`versionsBySeq`)
- `ingest/JsonValues` (`at` indexes arrays)
- `config/SpatialIndexInitializer`, `config/StartupOrder`
- `security/AccessPolicy`
- `settings/SettingKeys`
- `api/error/ErrorCodes`
- `trigger/package-info`

**Tests**

- New unit tests (`trigger/`): `TriggerPersistGateTest`, `RuleSqlBuilderTest`, `RuleValidatorTest`,
  `AlertPassTest`, `MessageTemplateTest`, `ActiveWindowTest`, `RegexGuardTest`, `AntimeridianTest`,
  `SnoozesTest`, `OperatorRegistryTest`, `AlertModelTest`, `AlertBusTest`.
- New unit test: `stream/sse/AlertsTopicTest`.
- New test helpers: `trigger/TruthTable`, `trigger/TestRules`.
- New integration tests: `trigger/RuleSqlBuilderIT`, `trigger/TriggerEngineIT`,
  `trigger/GeofenceHysteresisIT`, `api/TriggersControllerIT`, `api/AlertsControllerIT`,
  `api/AlertStreamIT`.
- New test support: `testsupport/trigger/TriggerTestSources`, `testsupport/trigger/TriggerFixture`.
- New fixture: `src/test/resources/trigger/operator-truth-table.json` (69 rows).
- Modified: `security/AccessPolicyTest`, `store/EntityMappingIT`, `api/EventStreamIT`,
  `config/SpatialIndexInitializerTest`, `config/SchemaBootstrapIT`, `ingest/IngestTypesTest`.

**Docs and tooling**

- `docs/OpenAPI.json`, `docs/Agent-Guide.md`
- `scripts/generate-postman.py` (`BODIES`)
- `postman/Foresignal.postman_collection.json` (regenerated)

No new dependency: `GeometryFixer`, `PreparedGeometry` and the WKB reader and writer are in the
existing JTS 1.19. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was ported
from God's Eye View.

## Deviations from the prompt

1. **String operators require a JSON string.** The prompt's SQL for string `==` is `(payload #>> :p) = :v`,
   with no type guard, so the number `5` would equal `"5"`. That breaks "exact, same semantics" with
   a typed Java gate. String `==`, `!=`, `in`, `notIn`, `startsWith` and `matches` therefore guard on
   `jsonb_typeof = 'string'`, as the numeric and boolean forms do. Every condition is NULL (not
   matching) when the value is missing or of another type, and the whole predicate is wrapped in
   `COALESCE(…, false)`, so `NOT P(p)` never turns NULL. `exists` is bound as
   `(present) = :v` rather than chosen by template. The truth table pins all of this.
2. **The scalar `source_id`/`kind` columns left the entity.** They became `sources`/`kinds` jsonb
   arrays. Hibernate's `update` does not drop columns, so an existing database keeps two unused
   nullable columns; a fresh one does not have them. `EntityMappingIT` was adapted.
3. **The cooldown seed and in-chunk writes.** SQL cannot see alerts inserted later in the same
   chunk, so the Java pass keeps the per-entity and per-rule last fire times in memory across
   chunks. The SQL seed covers alerts already in the database, including after a restart.
   Auto-resolve rows are therefore not restricted by an `EXISTS (open alert)` in SQL either: the
   writer applies inserts and resolves in seq order in one transaction, and an `UPDATE … WHERE seq <
   :resolvingSeq` resolves exactly the open alerts, including one inserted moments earlier in the
   same chunk.
4. **Hysteresis state.** Edge alternation uses the per-(rule, entity) state the pass keeps in
   memory. After a restart or a rule edit it is seeded from the entity's latest unsuppressed alert of
   that rule: an open enter alert means inside; a resolved one, or an exit alert, means outside;
   none means unknown, and any edge is accepted. The gate persists flips of both band fences.
5. **Backtest details.** `simulate` runs the pass from a fresh state, with the storm window on
   `observed_at` and without snoozes (the spec lists cooldown, cap and active windows). The cooldown
   seed is left out of backtests, so existing alerts do not hide historical matches.
   - For a hysteresis rule, `matchCount` counts only its own-mode edges.
   - `matches` are `Record` objects in the `RecordVersion` shape (`persistReason`, `prevSeq` and so
     on), which is a superset of `Record`.
   - With only `toSeq` given, `fromSeq` defaults to `S(now − 24 h)`; with only `fromSeq`, `toSeq`
     defaults to the watermark.
6. **Private sources.** `listAlerts`, `getAlert` (`404`) and the alert streams hide alerts of
   private sources from anonymous callers, as the 014 read paths do. The prompt does not mention it,
   but a record snapshot of a private source would otherwise leak; 039 moves this into
   `EgressPolicy`.
7. **Rule round trip.** `TriggerRule.geofence` holds the materialized MultiPolygon. A rule read
   back would then be refused (a polygon with `bufferMeters`), so when `geofenceRoute` (a line) is
   sent with `bufferMeters` it is the input and `geofence` is ignored. For `near` rules, `geofence`
   is returned as null, because `near` is the source of truth and the two are mutually exclusive on
   input.
8. **Spec reconciliations** (all additive, recorded in the changelog):
   - `EventStreamAlertsFilter.includeSuppressed` and the parameter `alerts.includeSuppressed` on
     `streamEvents`;
   - `streamAlerts` documents `400 INVALID_CURSOR` and `404` for an unknown source;
   - the private-source note on the three alert reads;
   - `Near.point.radiusKm` minimum 0.1 (was 0; the prompt says 0.1–2000);
   - `TriggerRuleInput.geofence` documents the route input;
   - `Condition.op` documents the type rules and the accepted regex subset;
   - `hysteresisMeters` documents its semantics.
9. **`AlertEvent.type`** uses the spec's enum: `fired`, `acknowledged`, `snoozed`, `updated`
   (feedback, unsnooze) and `resolved`. The SSE names are the three the prompt lists.
10. **`JsonValues.at` indexes arrays.** A numeric path step now indexes a list, as PostgreSQL's `#>`
    does with the same path. Without it, the gate's candidate value (Java) and its persisted value
    (`h.payload #> path`) disagreed for array paths. Threshold and title paths are unaffected unless
    they point into arrays.
11. **`near.anchor`** (036's moving anchor) is `400 FEATURE_UNAVAILABLE`.
12. **The actor.** `resolvedBy`/`acknowledgedBy` record `admin` for the bootstrap
    admin token. 023 adds named tokens (`token:<name>`) in `Actors`.
13. **`pending-operations.txt` does not exist yet** (020 creates it), so there were no lines to
    delete.

## Issues found / open questions

1. **Alert revisions and concurrency.** The alert stream drops events at or below the cursor a
   client already has, so revs must commit and be published in order. `AlertStore.write` holds one
   in-process lock across the transaction and the publishing of its events, for the engine, the
   resolver and the API alike. This is correct for one process. A second Foresignal process writing
   the same database would need an advisory lock instead. **Owner:** fine as built for the
   single-node design?
2. **PostgreSQL ARE versus Java regex.** `RegexGuard` keeps to a subset both engines read alike, but
   they still differ in corner cases (for example, the longest-match rules of alternation). The gate
   and SQL agree on every truth-table row; an exotic pattern could still disagree, in which case the
   worst outcome is an extra or a missing `TRIGGER_EDGE` version.
3. **Storm events are not routed yet.** `RuleStormEvent` is a Spring event; 018 routes it as a
   system event.
4. **Snoozing a resolved alert** is accepted and stored, but has no effect, because only open
   alerts suppress new matches. The spec does not say otherwise.
5. **The PostGIS behavior relied on is unverified until checkpoint 022:**
   - a negative `ST_Buffer` on geography for the hysteresis band;
   - geography buffers near the antimeridian (split in Java afterwards);
   - `ST_GeomFromWKB(bytea, 4326)` with bound WKB.
6. Owner decisions carried from 012–016 are still open.

No instructions directed at an agent were found in data, fixtures, web pages or tool output.

## Tests written (not run)

As a feature prompt requires, no tests were run. Integration tests seed records only through
`TestRecords`. Two tests delete a `record_version` row with SQL to simulate retention (a missing
predecessor, `getAlert`'s snapshot fallback). The trigger ITs share one context
(`@IntegrationTest @AutoConfigureMockMvc @Import(TriggerTestSources.class)`); `AlertStreamIT` uses
the web context with the same import.

- **`TriggerPersistGateTest`**:
  - every truth-table row (holds and ambiguous);
  - the gate against an empty payload for every row;
  - threshold flips;
  - conservative handling of type mismatch and missing values;
  - numeric JSON equality;
  - applicability by source and kind;
  - fences: entering, moving inside, no geometry, the boundary;
  - a fence AND a condition;
  - both hysteresis band fences;
  - specs per source.
- **`RuleSqlBuilderTest`**:
  - the frame: the seq range, HEARTBEAT exclusion, order;
  - sources and kinds; onlyNew; forecastHorizon;
  - every operator's SQL and parameters; no interpolation; COALESCE;
  - match without a join; enter and exit with the seq-only join; geofence WKB;
  - hysteresis edges;
  - both cooldown scopes; no seed;
  - the auto-resolve variants.
- **`RuleSqlBuilderIT`**:
  - every truth-table row in PostgreSQL;
  - geofence inside, boundary, outside and null;
  - match, enter and exit over seeded versions;
  - a window whose predecessor matched;
  - HEARTBEAT exclusion.
- **`RuleValidatorTest`** covers every rule of task 1:
  - defaults and `applyTo`; name; filters, aliases, unknown sources and kinds, limits; tags;
  - path grammar; operators; 15 bad values, including regex rejections; size limits;
  - volatile paths by source and by kind;
  - line buffers and the route round trip; wrong shapes; empty fences;
  - `near` point, place with and without Places, anchor;
  - edge modes; `autoResolve` (including on exit rules); hysteresis and too-small fences;
  - scalars; the storm cap; active periods and windows; `forecastHorizon` without `valid_from`;
    templates; collected errors.
- **`AlertPassTest`**:
  - record and rule cooldown, deterministic on `observed_at`;
  - snoozes;
  - the storm cap: once per window, auto-disable, unlimited, sliding;
  - active windows;
  - hysteresis alternation and seeding;
  - merged resolves;
  - simulate.
- **Other unit tests**:
  - `MessageTemplateTest`, `ActiveWindowTest` (both DST transitions with `MutableClock`, midnight
    spans, the period);
  - `RegexGuardTest`, `AntimeridianTest`, `SnoozesTest`, `OperatorRegistryTest`, `AlertModelTest`;
  - `AlertBusTest` (order, overflow and gap, a throwing listener);
  - `AlertsTopicTest` (identity, query and PATCH filters, private exclusion, replay mapping, cap).
- **`TriggerEngineIT`**:
  - `mag >= 6` with message, snapshot, severity, tags and rev;
  - a `kinds` array with `in`; `startsWith` on a callsign;
  - HEARTBEAT does not re-fire match;
  - a geofence crossing between samples (TRIGGER_EDGE ×2, one enter auto-resolved by the inverse
    edge, one exit, `alert.resolved` published);
  - convergence of a rule created while live and persisted diverge;
  - flapping bounded by `edgeMinInterval`;
  - tombstone then REAPPEAR fires enter, and the tombstone resolves;
  - a missing predecessor fires nothing;
  - cooldown on `observed_at` in both scopes, with a jumping clock;
  - rule and alert snoozes;
  - the storm cap, with its counter, `lastStormAt`, one `RuleStormEvent` and auto-disable;
  - expiry and stale resolution; housekeeping;
  - the cursor surviving restarts exactly once; `onGap` catch-up; a broken rule not blocking others.
- **`GeofenceHysteresisIT`** (TRG-2): jitter along the boundary gives one band edge version and no
  alerts; a real crossing gives one enter and one exit; further jitter adds nothing.
- **`TriggersControllerIT`**:
  - create (201, `Location`, every field), a route rule;
  - get, list with `tag`/`enabled`, full replace, delete keeping alerts, 404s;
  - 401s; 400 codes;
  - snooze and unsnooze;
  - backtest raw and simulated, by seq and by time, creating nothing; 400s; 404.
- **`AlertsControllerIT`**:
  - list filters and keyset pagination; bad filters;
  - `getAlert` with the record and the snapshot fallback;
  - private-source hiding;
  - ack idempotence, note and feedback; terminal, idempotent resolve; `feedbackCounts`;
  - snooze, unsnooze and feedback; 401s; 404s.
- **`AlertStreamIT`**:
  - fired, updated and resolved within 5 s with `id` = rev;
  - rev resume replaying current state once;
  - `minSeverity`; `includeSuppressed`; 400 and 404;
  - the `alerts` topic on `/v1/events/stream` with the composite id `c=…;a=<rev>`.
- **Updated**:
  - `AccessPolicyTest`: the new AGENT rules, plus exactness;
  - `EventStreamIT`: `notifications` replaces `alerts` as the unknown topic;
  - `SchemaBootstrapIT`: `alert_open_idx`, the new indexes, `alert_rev`;
  - `SpatialIndexInitializerTest`, `EntityMappingIT`, `IngestTypesTest` (array path steps).

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
4 actionable tasks: 3 executed, 1 up-to-date
```

## Notes for the next prompt

1. **018 (notification channels):**
   - Subscribe to `AlertBus` and skip events with `suppressed = true`.
   - Route `RuleStormEvent` (a Spring event) as a system event.
   - Register the `notifications` and `system` topics; `EventStreamIT` now uses `notifications` as
     its unknown topic, so update that test when 018 registers it.
   - Template timezones and filters go on top of `MessageTemplate`.
2. **019 (retention, settings):**
   - The consumer clamp reads `app_setting['trigger.cursor']`.
   - Expose `TriggerSettings` (key `triggers`).
   - Alert retention should keep open alerts, or the cooldown seed loses them.
3. **020 (pending operations):** every Triggers and Alerts operation of 017 is implemented; do not
   list them.
4. **022 (checkpoint):**
   - The trigger ITs add two cached contexts.
   - Check Issue 5.
   - The performance smoke can time `TriggerEngine` chunks with many rules and the gate's
     per-observation cost.
5. **023:** `api/support/Actors` is where named tokens become `token:<name>`. Stream tickets apply
   to `/v1/alerts/stream`.
6. **025 (MCP):** `AlertService.acknowledge/resolve` (`set_alert_status`),
   `TriggerRuleService.snooze(UUID, Instant)` (`snooze_rule`) and `TriggerBacktester.test`
   (`test_trigger`) take plain values.
7. **030:** `enrich`, `enrichRadiusKm` and `enrichWindow` are stored; subscribe to `AlertBus` for
   `fired`.
8. **036:**
   - Add `inWatchlist` as an `Operator` bean.
   - Provide `PlaceLookup` and re-materialize rules on place edits (`RuleValidator` is the place to
     reuse).
   - `near.anchor` is still `FEATURE_UNAVAILABLE`.
9. **039:** move the alert private-source filter (`AlertsController`, `AlertsTopic`) into
   `EgressPolicy`.
10. **Owner decisions pending:** Issue 1, plus those carried from 012–016.
