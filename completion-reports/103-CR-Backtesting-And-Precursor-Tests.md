# 103-CR-Backtesting-And-Precursor-Tests

| | |
|---|---|
| Prompt | 103-Backtesting-And-Precursor-Tests.md |
| Status | Complete, with the deviations below |
| Implementation commit | `1e616024818132ca41e7448fe8c23d46a4d609fc` |
| Post-review fix commit | `186597c` (`103: fix post-review defects`) |
| Date | 2026-10-15 (implementation); 2026-10-16 (post-review fixes) |

## Summary

Added backtesting and precursor tests in `ai.foresignal.backtest`: the job framework
(`backtest_job`, `BacktestJobService`), the pure caveat generator, cooldown simulator, Wilson interval
and precursor tester, rule and recipe backtests, alert-quality statistics and deterministic tuning
suggestions. Compiles; tests written, not run.

The original report overstated several things (see "Post-review fixes"). This report describes the
code as it stands after the 2026-10-16 fixes.

- **Job framework.** `BacktestJobService` submits, runs, polls and cancels jobs of every type on one
  table. Submit validates the target (404 for an unknown rule or precursor test; 400 for a rule job
  without `targetId`), resolves the requested range (`from`/`to` through `seq_clock` `S(T)`, or
  `fromSeq`/`toSeq`), rejects ranges that are empty, inverted, longer than 90 days or wholly below
  retention (400 `BACKTEST_RANGE_INVALID`), clamps `toSeq` to the published watermark and the start
  of a rule job to the retained floor of the rule's sources (`RANGE_CLAMPED_BY_RETENTION`), snapshots
  the definition (rule fields or the precursor test), and enforces one queued or running job per
  token (409 `BACKTEST_IN_PROGRESS`; REST and MCP both pass the token id). The job is handed to the
  worker pool after the submitting transaction commits. Run claims the job with a conditional
  update, records `progress` and checks for cancellation between chunks, and writes the terminal
  state with a conditional update, so a cancellation sticks. Any `Throwable` ends a job `failed`.
  Worker threads: `foresignal.backtest.concurrency` (default 1, at most 2). Finished jobs are purged
  after 30 days. `GET/DELETE /v1/jobs/{jobId}` return and cancel the same `BacktestJob`.
- **Caveats** (`CaveatGenerator`, pure) are computed from the stored history: range clamping,
  downsampled TRACK history, SAMPLED/LATEST ingest policies of the involved sources, coverage gaps
  from `fetch_log` (`CoverageGaps`: failed/refused runs and silences over 3 × the median spacing),
  rule edits after the range start (`DEFINITION_DIFFERS`), entity-link changes in range, windowed
  rules (`WINDOW_APPROXIMATE` for count and absence rules only), `SMALL_SAMPLE` from the result's
  sample size, and always `NOT_GROUND_TRUTH`.
- **Rule backtests** (`RuleBacktest`): record rules replay `RuleSqlBuilder` per 50k-seq chunk, each
  in its own read transaction with a 60 s statement timeout, through the live engine's `AlertPass` in
  simulate mode. Forecast-horizon rules are matched as the live engine saw each version at its own
  ingest time (no wall-clock look-ahead). Results: `wouldFire`, `wouldSuppress`, per-day histogram,
  top 20 entities, up to 200 samples, `actual` (unsuppressed alerts in range), `overlap` by seq, and
  up to five condition variants side by side. Anomaly rules are replayed bucket by bucket from the
  stored `activity_sample` counts (`AnomalyReplay`): baselines use only earlier buckets, and the result
  reports anomalies by classification, the warm-up consumed and `coverage-drop` counts.
- **Precursor tests** (`PrecursorTester` v1.0.0, pure): deduplicated antecedents (dedup window
  defaults to `maxLag`), first-consequent-in-lag hits (binary search), hit rate with a Wilson 95%
  interval, lag statistics, 200 true circular (wrapping) time shifts seeded with SHA-256 of the
  definition hash and range, base rate, lift, permutation p-value, the reverse direction computed
  without recursion and the co-occurrence flag, verdict labels from documented constants, and the
  multiple-testing note (precursor jobs run in the last 30 days, Bonferroni). Spatial rules:
  `sameEntity` over entity sets (event members, alerted records, anomaly groups), `withinKm`
  (event centroid or the alert snapshot's geometry), `sameRegion` by `country` or `admin1` (event
  region tags, alert region snapshot).
- **Recipe backtests** (`RecipeBacktest`) report the stored intelligence events that started in the
  requested range, of the requested recipe, excluding merged and private events (see Deviations).
- **Alert quality** (`AlertQualityService`): the 017 feedback mapping (`useful` → positive; `noise`,
  `false_positive` → negative; `late` → neutral), precision with a Wilson interval (null under five
  labels), median time-to-ack/resolve, flapping, duplicates within 2 × the rule's cooldown (10 minutes
  without one) and the severity distribution.
- **Tuning** (`TuningSuggester` v1.0.0, deterministic, never applied): `add_cooldown`,
  `switch_to_enter`, `raise_threshold` / `lower_threshold` from a scan of labeled alerts' snapshot
  values (the most negatives removed while keeping ≥ 90% of positives; the patch is the whole
  condition list with one value changed), `shrink_geofence` (≥ 70% of negatives outside the positives'
  convex hull buffered by 10%), `lower_anomaly_sensitivity`. `?simulate=true` (agent) submits a rule
  backtest with the condition patches as variants.
- **API/MCP/spec:** `BacktestController`, `PrecursorTestController`, `RuleAnalysisController`; MCP
  `run_backtest`, `get_backtest`, `test_precursor` (with range arguments), `get_rule_quality`,
  `get_tuning_suggestions`; problem codes; `AccessPolicy` rows; Postman regenerated in the
  implementation commit; Operator-/Agent-Guide sections.

## Files created / modified

Implementation commit — created (main): `backtest/` (`BacktestCaveat`, `CaveatGenerator`, `Wilson`,
`CooldownSimulator`, `PrecursorTester`, `BacktestJobEntity`, `BacktestJobRepository`,
`PrecursorTestEntity`, `PrecursorTestRepository`, `BacktestJobService`, `RuleBacktest`,
`RecipeBacktest`, `PrecursorTestService`, `AlertQualityService`, `TuningSuggester`, `BacktestViews`),
`api/BacktestController`, `api/PrecursorTestController`, `api/RuleAnalysisController`, five MCP tools.
Tests: `CaveatGeneratorTest`, `CooldownSimulatorTest`, `PrecursorTesterTest`, `TuningSuggesterTest`,
`RuleBacktestIT`, `AnomalyBacktestIT`, `RecipeBacktestIT`, `BacktestControllersIT`. Modified:
`api/error/ErrorCodes`, `security/AccessPolicy`, `trigger/RuleSpec` (`withConditions`),
`docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`, `postman/`,
`docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

Post-review commit — created: `backtest/AnomalyReplay`, `BacktestCoverage`, `CoverageGaps`,
`JobMonitor`; tests `CoverageGapsTest`, `RuleBacktestTest`. Modified: the backtest package, the two
backtest MCP tools, `PrecursorTestController`, `RuleAnalysisController`, the backtest tests and
`docs/Operator-Guide.md` (backtesting section).

## Deviations from the prompt

- **Recipe backtests report stored events, not a dry-run replay.** `CorrelationEngine` has no dry-run
  mode and lives in `intel/`, outside this prompt's fix scope. The result says so
  (`replay: "stored-events"`) and no overlap with real events is computed (they are the real events).
- **Count and absence rules** are replayed at record level (the rule's filter over versions), not as
  window states; the result carries `windowedReplay` and the job `WINDOW_APPROXIMATE`.
- **Anomaly replay uses the stored per-bucket counts**, so a variant may change detector parameters
  (sensitivity, direction, minimum delta, minimum weeks) but not the bucket or grouping.
- **Variants** replace the rule's condition list (record rules) or merge `window` detector parameters
  (anomaly rules); cooldown or mode patches cannot be simulated (`RuleSpec` in `trigger/` exposes only
  `withConditions`).
- **Rule jobs need `targetId`**; an inline `definition` is recorded in the snapshot but not compiled.
- **The inline `{rule}` precursor selector** is rejected at run time; alert, intelligence-event and
  anomaly selectors work. Intelligence-event selectors contribute no sources to coverage, so their
  `COVERAGE_GAPS`-driven `insufficient` is reachable only through alert or anomaly selectors.
- **Coverage gaps** use the median spacing of a source's `fetch_log` rows as its interval, and a
  source with no rows in the range counts as wholly uncovered. Absence and downward results inside
  gaps are not excluded from rule results (only the precursor verdict uses the gap fraction).
- **Retention clamping** applies to rule jobs (record history); precursor and recipe jobs read
  alerts, anomaly observations and events by time.
- **`DEFINITION_DIFFERS`** for rule jobs means the rule was edited after the range began (so the
  actual alerts came from an earlier definition); for precursor jobs, the test changed after submit.
- **Wire shapes that differ from `docs/OpenAPI.json` were left as they were** (renaming wire values is
  out of scope): suggestion kinds use underscores (`raise_threshold`, `add_cooldown`, …; the spec
  says `raise-threshold`, …) and the new kinds follow that style; `actual` is a number and `perDay` a
  map (spec: object and array); `countsByType` is a list; `hitRateInterval` is a list (spec
  `hitRateCi` object). `BacktestJob` now also carries `from`, `to` and `definition`.

## Issues found / open questions

- Jobs that were queued or running when the process stopped stay in that state and keep blocking
  their token; there is no restart recovery (a multi-instance deployment would need a lease).
- `docs/OpenAPI.json` and the code disagree on the suggestion-kind spelling and several result
  shapes (above): which should change?

## Tests written (not run)

- `CaveatGeneratorTest` — NOT_GROUND_TRUTH always; every code; SMALL_SAMPLE; stored form.
- `CooldownSimulatorTest` — observed_at cooldown, rule scope, hourly cap, active window.
- `CoverageGapsTest` — failures, refusals at the end, silences, no rows, merging.
- `PrecursorTesterTest` — planted precedence → notable and not co-occurrence; independent →
  no-evidence; symmetric → reverse flag without recursion; deterministic p and seed; n < 10 and
  coverage gaps → insufficient; circular shift wraps; sameRegion, sameEntity, withinKm; dedup;
  verdict thresholds; Wilson reference values.
- `RuleBacktestTest` — horizon rows kept by their own ingest time; sample sizes; checkpoints.
- `TuningSuggesterTest` — cooldown; enter; threshold scan keeps all conditions; 90% positives;
  never an empty condition list; geofence shrink; anomaly sensitivity; clean rule.
- `RuleBacktestIT` — open-ended seq range bounded by the watermark, `wouldFire` and caveats; variants;
  time range honored, 90-day cap and inverted ranges 400; retention clamp with caveat and 400 below
  retention; cancellation sticks; 30-day purge.
- `AnomalyBacktestIT` (INT-2) — `AnomalyReplay`: a spike at the end changes no earlier verdict;
  samples after the range are ignored; warm-up reported; variants change only detector parameters.
- `RecipeBacktestIT` — only events of the range and recipe, not merged or private.
- `BacktestControllersIT` — submit/list/get/progress; 404/400 targets; one job per token across REST
  and MCP; precursor job end to end; quality/suggestions/simulate; truncation; MCP shape.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (post-review).

## Notes for the next prompt

- 115 builds the tuning workflow on `TuningSuggester`; 120 builds precursor mining on
  `PrecursorTester` and `PrecursorTestService` (which no longer depends on `BacktestJobService`).
- `JobMonitor` is the checkpoint contract for any new job type on this framework.

## Post-review fixes (2026-10-16)

Every finding was re-verified against the code before it was changed.

- **Constructor cycle (critical, confirmed):** `PrecursorTestService` took `BacktestJobService` and
  never used it; removed.
- **Unbounded recursion (confirmed):** `test()` → `reverseStrong()` → `test()` recursed for symmetric
  input. The reverse direction is now computed once by a non-recursive `direction()`; the job
  boundary catches `Throwable`.
- **Requested ranges ignored (confirmed):** `rangeFrom/rangeTo` were never set; now resolved and
  stored at submit and used by every job type.
- **Circular shift did not wrap (confirmed):** shifted antecedents left the range and found no
  consequent, inflating lift; now `start + ((t − start + s) mod span)`.
- **Recipe backtest counted every event ever (confirmed):** now restricted to the range and recipe.
- **No range cap (confirmed):** `MAX_DAYS` was unused and `toSeq = Long.MAX_VALUE` looped for ever;
  now 400 beyond 90 days, `toSeq` clamped to the watermark, and a cancel can no longer be overwritten.
- **Raise-threshold patch emptied the conditions (confirmed):** replaced with the threshold scan.
- **Medium:** jobs now start after commit; horizon rules use ingest time; caveat inputs computed;
  the precursor REST endpoint passes the token; `sameEntity`/`sameRegion` can match; the
  multiple-testing count is precursor jobs run, not tests created.
- **Undisclosed gaps implemented:** 60 s statement timeout per chunk, progress, working cancel,
  30-day purge, configurable concurrency.
- **CR corrections:** the original claimed ranges were capped at 90 days and resolved "in range"
  (they were not), that every caveat was emitted (inputs were hard-coded), and that
  `AnomalyBacktestIT` covered the backtest's no-look-ahead (it exercised the prompt 100 detector).
  `AnomalyBacktestIT` now tests the backtest's own replay; `docs/Invariants.md` INT-2 already names
  that class and needed no change.
- **Result shapes aligned with `RuleBacktestResult` (confirmed against `docs/OpenAPI.json`):**
  `topEntities` items are now `{sourceId, externalId, entityId, count}` for every rule type (record
  rules resolve the entity link valid at the latest hit; anomaly groups are not entities, so
  `entityId` is null). Anomaly `samples` are `{seq, source, externalId, observedAt, edge}`, with the
  alert identity (`rule-windows`, `ruleId:group`) and the live evaluation's stored
  `anomaly_observation.watermark_seq` as `seq`; a fired bucket without one is counted but not
  sampled. Count and absence rules replace the non-spec `windowedReplay` string with the `window`
  block: `steps` (aligned step instants whose whole window is in range) and `unknownSteps` (over the
  row cap, or an absence step the historical coverage gate rejects), replayed through
  `WindowMembership` and `CoverageGate` bounded by each step instant (`WindowReplay`).
  **Limitations:** `wouldFire`, `perDay`, `topEntities` and `samples` of windowed rules remain
  record-level matches, not window transitions (the `WINDOW_APPROXIMATE` caveat now says so); the
  replay skips the rule-age warm-up and has no step cap (one membership query per step; the job is
  cancelable between steps).
