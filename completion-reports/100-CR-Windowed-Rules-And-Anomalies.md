# 100-CR-Windowed-Rules-And-Anomalies

| | |
|---|---|
| Prompt | 100-Windowed-Rules-And-Anomalies.md |
| Status | Complete (post-review fixes applied 2026-10-16; see the last section) |
| Implementation commit | `c7314a42238d26954c02eb42368d55041d1b3020` |
| Earlier fix commits | `d1081acf3f94a7ce7235e6c1c1b3928e1e4ffa6b`, `f63256c2f081dd0efee716c5e8b680b44feed686`, `43d71b59c93c97ff5ffdd999e70f71ce72ba25b8` |
| Post-review fix commit | `419574c` (`100: fix post-review defects`) |
| Date | 2026-10-15 (fixes 2026-10-16) |

## Summary

Added windowed trigger rules — **count**, **absence** and **anomaly** — on top of the 017 trigger model.
A rule has a `type` and a `window` spec; the windowed types reason over a time window rather than a
single version edge, alert and resolve through the 017 lifecycle, and their alerts reference the
`rule-windows` DERIVED records. Every output is reproducible from stored versions, live rows,
`fetch_log` and the rule; a coverage drop is never reported as a drop in activity. Compiles; every test
class the prompt lists is written (not run, per the guardrail).

- **Rule model** (`ai.foresignal.trigger.window`): `RuleType`, `WindowSpec` (the union of
  `CountWindow`/`AbsenceWindow`/`AnomalyWindow`), `WindowSpecValidator` (`type`/`window` validation, the
  single-version options rejected with 400 `WINDOW_RULE_INVALID`), `Durations`. `TriggerRuleEntity`
  gains `type` (default `record`) and `window_spec` (jsonb); `TriggerRuleService.create/upsert/update`
  validate and store them; `TriggerMapper` emits `type`/`window`. Only `record` rules are compiled into
  the per-version `TriggerEngine` and `TriggerPersistGate` (`RuleType.recordEngine`).
- **Membership** (`WindowMembership`): the distinct entities seen in `(from, to]` through a live row or a
  non-tombstone, non-heartbeat version with `seq ≤ watermark`. Source, kind, geofence, both time bounds
  and the knowledge bound (versions `ingested_at`, live rows `last_received_at` ≤ the decision time)
  are in SQL, with the 098 entity link valid at `to`; rows come back in a fixed order and are streamed
  into per-group distinct sets (conditions and group keys in Java). More than 200,000 candidate rows is
  `unknown`, never a partial count. Identity: the linked entity, else the strongest declared
  `entityKeys` value, else `(source, externalId)`.
- **Evaluation** (`WindowEngine`): count and absence evaluate the current window on each due step;
  anomaly fills completed buckets in order from a durable cursor (`anomaly.cursor.{ruleId}`), with
  catch-up after downtime and a 12-week backfill on creation or rebuild. Alerts are owed on
  clear → active and written once the state's `rule-windows` version exists (so `alert.seq` references
  it), retried until written; cooldown and `maxAlertsPerHour` apply per `(rule, group)`; `unknown` and
  `learning` never fire or resolve. An absence rule that cannot be evaluated raises one
  `system.window_rule_unknown` event per transition. `WindowScheduler` drives the `rule-windows` source
  and prunes `activity_sample` after 26 weeks; `RuleWindowsGenerator` is the DERIVED source (kinds
  `window-state` and `anomaly`).
- **Coverage gate** (`CoverageGate`): `ok` only when every involved source (the rule's, or for a
  kind-only rule every source declaring the kinds) is enabled, not failing or coverage/format-degraded,
  in fresh contact within `max(window, 2 × interval)`, has an OK/NOT_MODIFIED share ≥ `coverageMin`
  (streams: ≤ 10% failures) and shows no ≥ 50% receivers/expected drop against the median of the last
  20 (`fetch_log.coverage`). A past bucket is judged from `fetch_log` history before its end only.
- **Anomaly baselines**: `activity_sample` and `anomaly_observation`; `PoissonTail`, `NegBinTail`
  (method of moments), `Baseline` (coverage-ok, unflagged samples of the last 12 weeks, winsorized at
  p99, weeks counted per the bucket and slot mode), `AnomalyDetector` (λ = slot mean × coverage ratio
  for both models; `hour-of-day` may start after 14 days with `lowConfidence`) and `AnomalyClassifier`
  (`activity-up`/`activity-down`/`coverage-drop`/`insufficient-coverage`/`learning`/`normal`). Every
  result records the detector name, version and params, the baseline summary, the watermark, up to 20
  member references with their seqs and the `fetch_log` id range per source; anomaly alerts carry
  `alert.context.anomaly` and the message "Unusual activity: 42 observed vs ~11 expected (p≈2e-6 under a
  Poisson model)".
- **API/MCP/spec**: `WindowController` (`getTriggerWindowState`, `getTriggerBaseline` per slot in the
  `AnomalyBaseline` shape, `rebuildBaseline` 202 `BaselineRebuildResult`, `listAnomalies`);
  `testTrigger` for windowed types (`WindowTester`: step evaluations from versions only,
  `approximate: true`, `windowEvaluations`); problem codes `WINDOW_RULE_INVALID`/`BASELINE_NOT_READY`;
  `AccessPolicy`; MCP `list_anomalies`, `get_window_state`, `watch_for_unusual_activity`, and
  `create_watch`/`update_watch` advertising `type` and `window`. `docs/OpenAPI.json`'s four operations
  are `implemented`; Postman regenerated; `docs/Operator-Guide.md` and `docs/Agent-Guide.md` have the
  windowed-rule sections.

## Files created / modified

Created (main): `trigger/window/` (`RuleType`, `WindowSpec`, `WindowSpecValidator`, `Durations`,
`WindowStateEntity`, `ActivitySampleEntity`, `AnomalyObservationEntity` and their repositories,
`CoverageGate`, `WindowMembership`, `WindowEngine`, `RuleWindowsGenerator`, `WindowScheduler`,
`WindowCursorConsumer`, `PoissonTail`, `NegBinTail`, `Gammas`, `Baseline`, `AnomalyDetector`,
`AnomalyClassifier`, post-review `WindowTester`), `api/WindowController`,
`mcp/tools/ListAnomaliesTool`, `mcp/tools/GetWindowStateTool`, `mcp/tools/WatchForUnusualActivityTool`.

Created (test): `trigger/window/WindowSpecValidatorTest`, `PoissonTailTest`, `NegBinTailTest`,
`AnomalyClassifierTest`, `CoverageGateTest`, `WindowEvaluatorIT`, `AbsenceRuleIT`, `AnomalyRuleIT`,
`WindowControllersIT`, `RuleWindowsIngestIT`, `testsupport/ingest/CoverageSupport`; post-review
`DeterminismTest`, `LookAheadTest`, `RuleTypeTest`, `WindowEmitters`.

Modified: `store/TriggerRuleEntity`, `trigger/TriggerRuleService`, `trigger/TriggerEngine` and
`trigger/TriggerPersistGate` (post-review: record rules only), `api/TriggersController` (post-review:
windowed `testTrigger`), `api/mapper/TriggerMapper`, `mcp/tools/WatchItems` (post-review),
`api/error/ErrorCodes`, `security/AccessPolicy`, `settings/SettingKeys`, `docs/OpenAPI.json`,
`src/test/resources/openapi/pending-operations.txt`, `src/test/resources/application-test.yml`
(post-review: `foresignal.windows.scheduled: false`), `postman/` (regenerated),
`docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

- **Driving.** `WindowScheduler` does not own a virtual-thread loop; every 5 s it ensures the
  `rule-windows` DERIVED source is enabled and asks the 052 runtime for a tick; the runtime runs
  `RuleWindowsGenerator.onTick`, and `WindowEngine` throttles each rule to its own `step`.
  `foresignal.windows.scheduled` (default true) turns the background driving off; the test profile sets
  it false so the window ITs evaluate explicitly.
- **Alert timing.** An alert is written on the tick after the clear → active transition (about 5 s
  later), once the `rule-windows` version of that state is persisted, so `alert.seq` references it.
  If no such version appears within 5 minutes (for example `rule-windows` ingest failing), the alert is
  written anyway referencing the evaluation watermark, and a warning is logged.
- **Anomaly buckets are evaluated from versions only, two minutes after the bucket ends.** A live row
  is the present state, so reading it for a past bucket would depend on when the bucket is evaluated.
  Versions known by `bucketEnd + 2 min` make a live evaluation and a later catch-up of the same bucket
  identical (INT-1, INT-2). Entities only touched (no version) inside a bucket are not counted,
  consistently in the baseline and the bucket.
- **Catch-up and backfill only fill tables.** Buckets older than the newest settled one write samples
  and observations (and `anomaly` records when under 24 h old) but never move the window state or
  alert; at most 24 buckets are evaluated per rule per tick, so a 12-week backfill spreads over ticks.
- **The anomaly cursor lives in `app_setting`** (`anomaly.cursor.{ruleId}` = last bucket and its
  watermark), as the trigger and entity cursors do; with no stored cursor the engine continues after
  the rule's newest sample.
- **`activity_rollup` is not read.** Samples are computed from membership counts; this records the
  path used (`samplePath: activity_sample`).
- **Coverage freshness counts the last successful contact, a 304 included.** The prompt's gate says
  `last_data_at ≥ t − max(window, 2 × interval)`, but its test list says "a 304 streak with steady
  coverage still counts as covered" and its example rule is "no NWS alert updates for 2 h while
  healthy"; `last_data_at` does not move on a 304, so the literal check would make both impossible.
  The gate uses the latest of `last_data_at`, `last_success_at` and the last OK/NOT_MODIFIED fetch.
  **Recorded as a conflict within the prompt for the owner to confirm.**
- **Past-bucket health is derived from `fetch_log`**: failing when the last three attempted fetches
  before the bucket end failed, format-degraded when a fetch in the bucket failed on the format; there
  is no settings history, so "enabled for the whole bucket" means the source's first fetch is at or
  before the bucket start and it fetched in the bucket.
- **Absence warm-up** uses the rule's `createdAt` and each involved source's first `fetch_log` row; a
  rule in warm-up is `learning` with the reason.
- **Group-by coverage.** `source`, `h3:<res>`, payload paths and `place` are evaluated directly;
  `country` and `admin1` need the 040 reference data (unloaded → `*`).
- **OpenAPI kind docs.** The `rule-windows` kinds are exposed at runtime through the descriptor's
  `fieldSpecs`; no new static kind block was authored in `docs/OpenAPI.json`.
- **`TriggerRule.windowStatus`** (state summary, groups, `learningUntil`) is not added to the rule
  representation; `/window-state` and `/baseline` carry that information.

## Issues found / open questions

- The engine creates one `window_state` row and one emit per group per evaluation; on a large grouped
  rule this is many rows. The `maxGroups` cap bounds it, with `groupsTruncated` set when exceeded (the
  top groups by count, ties by key).
- `test_watch` (MCP) still runs the per-version backtester for every rule; windowed rules get their
  step evaluations through the REST `testTrigger` only.
- The membership query sorts the candidate union; on a very busy source a long window costs a sort of
  up to the row cap before the result is known.

## Tests written (not run)

- `WindowSpecValidatorTest` — every field, type-specific rejections, window bounds, the group cap, and
  the `onlyNew`/`mode`/`reappearAsEnter` rejection.
- `PoissonTailTest` / `NegBinTailTest` — tails against hard-coded reference values, the overdispersion
  switch and `minAbsoluteDelta`; post-review: the NB tail uses the coverage-adjusted mean.
- `AnomalyClassifierTest` — activity-up/down, coverage-drop vs insufficient-coverage, learning,
  winsorizing, the NB switch; post-review: coverage-drop is judged before insufficient-coverage, weeks
  follow the bucket, `hour-of-day` starts after 14 days with `lowConfidence`.
- `CoverageGateTest` — the health × fetch-share × coverage-drop × enabled × stream-disconnect table
  through the pure `CoverageGate.decide`.
- `RuleTypeTest` (post-review) — only `record` rules reach the per-version engine.
- `DeterminismTest` (INT-1, post-review) — baseline, detector, classifier, tails, slots, bucket floors
  and the alert text are pure and order-insensitive.
- `LookAheadTest` (INT-2, post-review, `@WebIntegrationTest`) — a version ingested after the decision
  time, a live row received after it, or an observation after the bucket is not counted; the gate for a
  past bucket reads `fetch_log` history and ignores today's failing health; re-evaluating a past bucket
  after late data gives the same result.
- `WindowEvaluatorIT` (rewritten) — a count rule fires once on clear → active with `alert.seq` equal to
  the `rule-windows` head seq and resolves on active → clear; distinct entities not versions; an entity
  seen only through a touch counts through its live row; per-group states with `groupBy=place`; a
  windowed rule never fires per version through the 017 engine; a window over the cap is unknown.
- `AbsenceRuleIT` (rewritten) — a healthy quiet source fires; a record in the window is clear; a failing
  source is unknown with one system event and no alert; a 304 streak counts as covered; rule and source
  warm-up; a kind-only rule with no enabled source is unknown; active → unknown keeps the alert firing.
- `AnomalyRuleIT` (rewritten) — learning until `minWeeks`; a spike is `activity-up` with detector
  version, real evidence seqs and `fetch_log` ranges, and alerts with `context.anomaly` and the default
  message; halving the receivers is `coverage-drop` with no alert; the cursor catches up missed buckets
  in order and stops at the newest settled one.
- `WindowControllersIT` — CRUD with each type, window-state, baseline 409 and (post-review) the per-slot
  baseline shape, the rebuild that clears and resets the cursor (202) and rejects a count rule, the
  anomalies feed, and the windowed `testTrigger`.
- `RuleWindowsIngestIT` — a `window-state` record persists through ingest; post-review: the owed alert
  references its version.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (also after the post-review fixes).

## Notes for the next prompt

- Windowed rules live in `ai.foresignal.trigger.window`; `WindowEngine` is the evaluation entry point
  and `rule-windows` the DERIVED source. 101 can consume `window-state`/`anomaly` records directly.
- The new tables (`window_state`, `activity_sample`, `anomaly_observation`) are Hibernate-managed;
  `window_state` gained `alert_pending`, `alert_pending_since`, `pending_context` and
  `pending_message` post-review.
- Run `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

## Earlier fix passes (2026-10-15, summarized)

- `d1081ac`: `unknown` no longer resolves a firing alert; `window-state`/`anomaly` records use the
  evaluation instant as `observedAt` (epoch was rejected at ingest); anomaly warm-up could complete
  (12-week lookback, per-slot weeks); the real per-bucket coverage ratio made `coverage-drop` reachable.
- `f63256c`: `CoverageSupport` test helper and frozen-clock repairs of the coverage-gated tests.
- `43d71b5`: `AbsenceRuleIT` advanced past the step before its second evaluation.

## Post-review fixes (2026-10-16)

Fix commit `419574c`. Every finding was re-verified against the code before it was changed.

1. **Windowed rules fired per version (CRITICAL, fixed).** `TriggerEngine.reloadRules` and
   `TriggerPersistGate.rebuild` compiled every enabled rule; a windowed rule's stored mode defaults to
   `match`, so it fired on every version and gated persistence. Both now skip any type other than
   `record` (`RuleType.recordEngine`, unknown types included). Guarded by `RuleTypeTest` and
   `WindowEvaluatorIT.windowedRuleNeverFiresPerVersion`.
2. **Membership was unfiltered, capped without order and could under-count (CRITICAL, fixed).** See
   "Membership" in the summary; the cap now yields `unknown`.
3. **Look-ahead (HIGH, fixed).** The live branch had no upper bound, past buckets counted entities seen
   after the bucket, and the gate read current health for past buckets. Every read is now bounded by the
   decision time; past buckets read versions and `fetch_log` history only.
4. **Kind-only rules passed the gate unchecked (HIGH, fixed).** Kinds resolve to the declaring sources;
   none → `unknown`.
5. **INT-1/INT-2 tests missing (HIGH, fixed).** `DeterminismTest` and `LookAheadTest` written.
6. **Alert seq, evidence seq, fetch_log ranges (MEDIUM, fixed).** `alert.seq` is the `rule-windows`
   version seq; evidence members carry their real seqs; evidence lists `fetch_log` id ranges per source.
7. **A failed alert write was lost (MEDIUM, fixed).** The alert stays owed and is retried each tick.
8. **The NB tail ignored the coverage ratio (MEDIUM, fixed).** `p = r/(r + mean × ratio)`.
9. **The retention clamp counted disabled rules and vanished groups (MEDIUM, fixed).** Only enabled
   anomaly rules' cursors count, at the `seq_clock` cutoff an hour before each rule's next bucket.
10. **Undisclosed missing requirements (fixed):** windowed `testTrigger` with `approximate`;
    `create_watch`/`update_watch` advertise `type`/`window`; `rebuildBaseline` really clears and
    re-backfills; `/baseline` is per slot of the rule's slot mode; anomaly catch-up after downtime and
    backfill; `activity_sample` pruned after 26 weeks; absence warm-up; per-group cooldown/hourly cap.
11. **Also fixed while there:** an ungrouped rule now evaluates group `*` even with zero members (a
    `<=` count rule could never become active, and zero-count buckets were never sampled); hour-of-week
    slots are 0–167 as the spec documents (they were 24–191); `deleteBy…` repository methods are
    `@Transactional` (a derived delete outside a transaction can fail when a rule is deleted).

**Inaccuracies corrected in this report:** the original said window alerts reference the
`rule-windows` version via `alert.seq`, but `seq` was `changeBus.lastPublishedSeq()` (now true after
fix 6); it said evidence refs were recorded (their seq was always 0); it described the anomaly cursor as
the `window_state` watermark and `activity_sample` as unpruned, `rebuildBaseline` as a rebuild (it only
returned `rebuilt: true`), and the baseline API as per slot (it was one hard-coded hour-of-week slot);
"Complete" claims for count/absence/anomaly evaluation are now backed by the tests above rather than by
tests that asserted constants or could not run.
