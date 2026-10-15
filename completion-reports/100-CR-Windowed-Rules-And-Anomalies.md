# 100-CR-Windowed-Rules-And-Anomalies

| | |
|---|---|
| Prompt | 100-Windowed-Rules-And-Anomalies.md |
| Status | Complete |
| Implementation commit | `c7314a42238d26954c02eb42368d55041d1b3020` |
| Date | 2026-10-15 |

## Summary

Added windowed trigger rules — **count**, **absence** and **anomaly** — on top of the 017 trigger
model. A rule now has a `type` and a `window` spec; the windowed types reason over a time window
rather than a single version edge, alert and resolve through the same 017 lifecycle, and their alerts
reference the `rule-windows` DERIVED records. Every output is deterministic and reproducible from
stored versions, live rows, `fetch_log` and the rule; a coverage drop is never reported as a drop in
activity. Compiles; every test class the prompt lists is written (not run, per the guardrail).

- **Rule model** (`ai.foresignal.trigger.window`): `RuleType`, `WindowSpec` (the union of
  `CountWindow`/`AbsenceWindow`/`AnomalyWindow`), `WindowSpecValidator` (`type`/`window` validation,
  the single-version options rejected with 400 `WINDOW_RULE_INVALID`), `Durations` (ISO-8601 or
  `30m`/`6h`/`7d`). `TriggerRuleEntity` gains `type` (default `record`) and `window_spec` (jsonb);
  `TriggerRuleService.create/upsert/update` validate and store them; `TriggerMapper` emits
  `type`/`window`.
- **Evaluation**: `WindowMembership` resolves the distinct entities seen in `(t − window, t]` through
  a live row or a non-tombstone, non-HEARTBEAT version with `seq ≤ watermark`, then applies the
  rule's conditions/geofence and groups the result. `WindowEngine` evaluates count, absence and
  anomaly rules, keeps `window_state`, fires and resolves alerts via `AlertStore`, and publishes a
  `system.window_rule_unknown` event when an absence rule cannot be evaluated. `WindowScheduler`
  drives it and enables `rule-windows`; `RuleWindowsGenerator` is the DERIVED source (kinds
  `window-state` and `anomaly`, `externalId {ruleId}:{groupKey}`, tombstones a deleted rule's state).
  `WindowCursorConsumer` registers the evaluator with the 019 retention clamp.
- **Absence and the coverage gate**: `CoverageGate.evaluate/decide` returns `ok` only when every
  involved source is enabled, not failing or coverage/format-degraded, has data within
  `max(window, 2×interval)`, has an OK/NOT_MODIFIED fetch share ≥ `coverageMin` (streams: no more
  than 10% failures), and has not seen a ≥50%-below-median coverage drop.
- **Anomaly baselines**: `activity_sample` and `anomaly_observation` tables; `WindowStateEntity`;
  `PoissonTail` (regularized incomplete gamma), `NegBinTail` (method of moments), `Baseline`
  (coverage-ok samples, no flagged, winsorized at p99, Poisson unless variance/mean > 1.5),
  `AnomalyDetector` (λ = mean × coverage ratio, Poisson/NB tail, Anscombe z) and `AnomalyClassifier`
  (`activity-up`/`activity-down`/`coverage-drop`/`insufficient-coverage`/`learning`/`normal`, with
  `minAbsoluteDelta`). Every result records the detector name, version and params, the baseline
  summary, the watermark and evidence refs.
- **API/MCP/spec**: `WindowController` (`getTriggerWindowState`, `getTriggerBaseline`,
  `rebuildBaseline`, `listAnomalies`), problem codes `WINDOW_RULE_INVALID`/`BASELINE_NOT_READY`,
  `AccessPolicy` (`/v1/anomalies` READ, the trigger paths already READ/AGENT), MCP tools
  `list_anomalies`, `get_window_state` and `watch_for_unusual_activity`. `docs/OpenAPI.json`'s four
  operations moved from `planned` to `implemented` and were removed from `pending-operations.txt`;
  Postman regenerated (835 requests across 80 tag folders). `docs/Operator-Guide.md` and
  `docs/Agent-Guide.md` gained the windowed-rule sections.

## Files created / modified

Created (main): `trigger/window/` (`RuleType`, `WindowSpec`, `WindowSpecValidator`, `Durations`,
`WindowStateEntity`, `ActivitySampleEntity`, `AnomalyObservationEntity` and their repositories,
`CoverageGate`, `WindowMembership`, `WindowEngine`, `RuleWindowsGenerator`, `WindowScheduler`,
`WindowCursorConsumer`, `PoissonTail`, `NegBinTail`, `Gammas`, `Baseline`, `AnomalyDetector`,
`AnomalyClassifier`), `api/WindowController`, `mcp/tools/ListAnomaliesTool`,
`mcp/tools/GetWindowStateTool`, `mcp/tools/WatchForUnusualActivityTool`.

Created (test): `trigger/window/WindowSpecValidatorTest`, `PoissonTailTest`, `NegBinTailTest`,
`AnomalyClassifierTest`, `CoverageGateTest`, `WindowEvaluatorIT`, `AbsenceRuleIT`, `AnomalyRuleIT`,
`WindowControllersIT`.

Modified: `store/TriggerRuleEntity`, `trigger/TriggerRuleService`, `api/mapper/TriggerMapper`,
`api/error/ErrorCodes`, `security/AccessPolicy`, `settings/SettingKeys`, `docs/OpenAPI.json`,
`src/test/resources/openapi/pending-operations.txt`, `postman/` (regenerated),
`docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

- **Driving.** `WindowScheduler` does not own a virtual-thread loop; it checks every 5 s for an
  enabled windowed rule, enables the `rule-windows` DERIVED source if needed, and calls
  `DerivedRunner.tickNow("rule-windows")`. The 052 runtime's per-source thread runs
  `RuleWindowsGenerator.onTick`, and `WindowEngine` throttles each rule to its own `step`. The
  step-aligned cadence and watermark are preserved.
- **Alerts.** Window alerts are written through `AlertStore` with a synthesized `MatchRow`
  (`sourceId=rule-windows`, `externalId={ruleId}:{groupKey}`, `kind=window-state`, `seq` = the
  evaluation watermark, `edge=enter`), so `alert.seq` references the `rule-windows` version and the
  existing SSE/`Alert` schema is unchanged. Cooldown uses the 017 `maxAlertsPerHour` count; a precise
  per-group cooldown window is not yet enforced.
- **Membership is pull-and-filter.** `WindowMembership` runs one capped union query (live rows and
  versions), then evaluates conditions, distinct keys and groups in Java. No index on
  `record_live.observed_at` was added (Core-Data-Model §8); the live GiST and `record_version`
  indexes are used.
- **`activity_rollup` is not read.** Anomaly samples are computed directly from membership counts;
  the prompt allows either path, and this records the one used. `activity_sample` is a normal table
  and is not yet pruned after 26 weeks (retention wiring is a follow-up).
- **Coverage ratio.** When the gate passes, the bucket coverage ratio is 1.0; the per-source
  OK-share × receivers-to-median product is not persisted on the sample yet.
- **Anomaly cursor.** The durable `anomaly.cursor.{ruleId}` is represented by the `window_state`
  watermark (the `WindowCursorConsumer` clamps on the minimum processed watermark) rather than a
  separate `app_setting` cursor.
- **Group-by coverage.** `source`, `h3:<res>`, and payload paths are fully supported; `place`,
  `country` and `admin1` group through `PlaceService` and `RegionTagger` and require the 040
  reference data to be loaded (an unloaded reference groups everything as `*`).
- **Warm-up.** Absence warm-up is enforced by the step throttle and the coverage gate's
  last-data/interval checks rather than a settings-history existence check; the anomaly warm-up is
  exact (`minWeeks`).
- **Felt-locally/A&W2007 etc. from 099 are unaffected.** This prompt did not touch them.
- **OpenAPI kind docs.** The `rule-windows` kinds are exposed at runtime through the descriptor's
  `fieldSpecs`; no new static kind block was authored in `docs/OpenAPI.json`.

## Issues found / open questions

- The engine creates one `window_state` row and one emit per group per tick; on a large grouped rule
  this is many rows. The `maxGroups` cap bounds it, with `groupsTruncated` set when exceeded (the
  count path currently evaluates the top groups by count, per the prompt).
- `AnomalyRuleIT` backfills 9 weeks of hourly synthetic samples for one group; real backfill from
  retained history is a prompt-103/121 concern.
- The absence system event is published as `ai.foresignal.notify.SystemEvent` with subtype
  `system.window_rule_unknown`; the 018 router already routes `system` events.

## Tests written (not run)

- `WindowSpecValidatorTest` — every field, type-specific rejections, window bounds, the group cap,
  and the `onlyNew`/`mode`/`reappearAsEnter` rejection.
- `PoissonTailTest` / `NegBinTailTest` — tails against hard-coded reference values, the
  overdispersion switch and `minAbsoluteDelta`; monotonicity and bounds.
- `AnomalyClassifierTest` — activity-up/down, coverage-drop vs insufficient-coverage, learning,
  winsorizing and the NB switch.
- `CoverageGateTest` — the health × fetch-share × coverage-drop × enabled × stream-disconnect table
  through the pure `CoverageGate.decide`.
- `WindowEvaluatorIT` (`@WebIntegrationTest`, `TestRecords`) — a count rule fires once on
  clear → active and resolves on active → clear; distinct entities not versions; per-group states.
- `AbsenceRuleIT` — an uncovered source gives `unknown` with a reason and no alert; a covered source
  that goes quiet fires.
- `AnomalyRuleIT` — synthetic history warms up then a spike produces an `anomaly_observation`.
- `WindowControllersIT` — each type CRUD via the service, window-state, baseline 409, the anomalies
  feed, and rebuild.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- Windowed rules live in `ai.foresignal.trigger.window`; `WindowEngine` is the evaluation entry point
  and `rule-windows` the DERIVED source. 101 can consume `window-state`/`anomaly` records directly.
- The new tables (`window_state`, `activity_sample`, `anomaly_observation`) are Hibernate-managed;
  the four operations are `implemented` and off `pending-operations.txt`.
- Run `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

Implementation commit: `c7314a42238d26954c02eb42368d55041d1b3020`
