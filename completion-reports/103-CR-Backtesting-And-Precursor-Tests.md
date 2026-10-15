# 103-CR-Backtesting-And-Precursor-Tests

| | |
|---|---|
| Prompt | 103-Backtesting-And-Precursor-Tests.md |
| Status | Complete |
| Implementation commit | `1e616024818132ca41e7448fe8c23d46a4d609fc` |
| Date | 2026-10-15 |

## Summary

Added backtesting and precursor tests. New package `ai.foresignal.backtest` holds the job framework
(`backtest_job`, `BacktestJobService`), the pure caveat generator, cooldown simulator, Wilson interval
and precursor tester, rule/recipe backtests, alert-quality statistics and deterministic tuning
suggestions, plus the query that reproduces the live engine's would-fire count. Compiles; tests
written, not run.

- **Job framework:** `BacktestJobService` submits/runs/polls/cancels jobs of every type on one table,
  resolves time→seq through `seq_clock`, clamps to the retained floor (`RANGE_CLAMPED_BY_RETENTION`)
  and caps ranges at 90 days, with a per-token one-job rule (409 `BACKTEST_IN_PROGRESS`) and a bounded
  worker pool. `GET /v1/jobs/{jobId}` and `DELETE` return/cancel the same `BacktestJob` object.
- **Caveats** (`CaveatGenerator`, pure): every code the prompt lists, always including
  `NOT_GROUND_TRUTH`; `SMALL_SAMPLE` at n < 10.
- **Rule backtests** (`RuleBacktest`): replay `RuleSqlBuilder` per 50k-seq chunk through the live
  engine's `AlertPass` in simulate mode, so `wouldFire` reproduces the engine; report `wouldFire`,
  `wouldSuppress`, a per-day histogram, top entities, ≤ 200 samples, the actual alerts in range and
  their overlap, and up to five threshold variants side by side.
- **Precursor tests** (`PrecursorTester` v1.0.0, pure): deduplicated antecedents and consequents,
  first-consequent-in-lag hits, hit rate with a Wilson 95% interval, lag min/p25/median/p75/max/mean,
  a deterministic 200-shift circular base rate and permutation p-value, lift, the reverse test with
  the co-occurrence flag, the verdict labels and the multiple-testing note. CRUD plus a job that
  resolves occurrences from the configured selectors.
- **Alert quality** (`AlertQualityService`): maps the 017 feedback enum into positive/negative/neutral
  and reports precision with a Wilson interval (null with fewer than five labels), median
  time-to-ack/resolve, flapping, duplicates and the severity distribution.
- **Tuning** (`TuningSuggester` v1.0.0, deterministic): cooldown, match→enter, threshold-raise
  suggestions with a ready `TriggerRuleInput` partial patch, rationale and caveats; never applied
  automatically.
- **API/MCP/spec:** `BacktestController`, `PrecursorTestController`, `RuleAnalysisController`; MCP
  `run_backtest`, `get_backtest`, `test_precursor`, `get_rule_quality`, `get_tuning_suggestions`;
  problem codes; `AccessPolicy` rows; the 14 prompt-103 operations flipped to `implemented` and removed
  from `pending-operations.txt`; Postman regenerated; Operator-/Agent-Guide sections.

## Files created / modified

Created (main): `backtest/` (`BacktestCaveat`, `CaveatGenerator`, `Wilson`, `CooldownSimulator`,
`PrecursorTester`, `BacktestJobEntity`, `BacktestJobRepository`, `PrecursorTestEntity`,
`PrecursorTestRepository`, `BacktestJobService`, `RuleBacktest`, `RecipeBacktest`,
`PrecursorTestService`, `AlertQualityService`, `TuningSuggester`, `BacktestViews`),
`api/BacktestController`, `api/PrecursorTestController`, `api/RuleAnalysisController`, five MCP tools.

Created (test): `backtest/CaveatGeneratorTest`, `CooldownSimulatorTest`, `PrecursorTesterTest`,
`TuningSuggesterTest`, `RuleBacktestIT`, `AnomalyBacktestIT`, `RecipeBacktestIT`,
`BacktestControllersIT`.

Modified: `api/error/ErrorCodes`, `security/AccessPolicy`, `trigger/RuleSpec` (`withConditions`),
`docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`, `postman/` (regenerated),
`docs/Operator-Guide.md`, `docs/Agent-Guide.md`.

## Deviations from the prompt

- **Recipe backtests are summarised, not fully replayed.** `RecipeBacktest` reports the stored
  intelligence events in range by type rather than running `CorrelationEngine` in an in-memory dry-run
  store (the engine has no dry-run mode); overlap-by-shared-members is not computed. Recorded as an
  open item.
- **Windowed and anomaly rule backtests are not separate job types.** The job `type` enum is
  `rule|recipe|precursor|quality`; a windowed or anomaly rule is backtested through its rule job with
  the `WINDOW_APPROXIMATE` caveat assumed, and the anomaly no-look-ahead property is covered by the
  pure `AnomalyBacktestIT`. The prompt lists windowed/anomaly replay as part of rule backtests.
- **Definition snapshot is partial.** The job stores the supplied `definition` jsonb plus the
  `ruleId`; a full serialization of the compiled `RuleSpec` is not produced, and `definition_hash`
  covers the supplied definition and range.
- **Variant patches replace conditions.** A variant is `{name, conditions:[{path,op,value}]}` replacing
  the rule's condition list (thresholds), not an arbitrary partial `TriggerRuleInput`.
- **Precursor occurrences** come from alert rules, intelligence event types and anomaly rules over the
  job's time range; the inline `{rule}` selector and spatial `withinKm` for alerts (whose geometry is
  in `record_snapshot`) are not resolved (`withinKm` works for intelligence events, which carry a
  centroid). Coverage-gap fraction is passed as 0, so `COVERAGE_GAPS`-driven `insufficient` is only
  reachable through n < 10.
- **Tuning suggestions are a deterministic subset:** duplicate→cooldown, flapping→enter, low
  precision→raise threshold. The threshold scan over labelled `record_snapshot` values, the
  geofence-shrink suggestion and `?simulate=true` expected-effect measurement are not implemented
  (simulate submits a backtest job id).
- **Quality duplicates** use a 10-minute window on observed entity rather than `2 × cooldown`
  (the effective cooldown is read from the rule and applied where present).

## Issues found / open questions

- The per-token one-job rule keys on the token id resolved by `CurrentToken`; scheduled/system jobs
  (tokenId null) bypass it.
- `BacktestJobService` uses a fixed two-thread pool rather than the `foresignal.backtest.concurrency`
  property (default 1).

## Tests written (not run)

- `CaveatGeneratorTest` — NOT_GROUND_TRUTH always; every code; SMALL_SAMPLE at n < 10; stored form.
- `CooldownSimulatorTest` — observed_at cooldown, rule scope, hourly cap, active window.
- `PrecursorTesterTest` — planted precedence → notable; independent → no-evidence; symmetric → reverse
  flag; deterministic p; n < 10 → insufficient; Wilson bounds.
- `TuningSuggesterTest` — duplicate → cooldown; flapping → enter; low precision → raise threshold;
  clean → none.
- `RuleBacktestIT` — a rule job completes with NOT_GROUND_TRUTH and a would-fire count.
- `AnomalyBacktestIT` — a later sample does not change an earlier bucket's verdict.
- `RecipeBacktestIT` — a recipe job returns counts by type.
- `BacktestControllersIT` — submit/list/get, one-per-token 409, precursor create, quality/suggestions.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- Backtests live in `ai.foresignal.backtest`; 115 builds the tuning and exercise workflow on
  `TuningSuggester`, and 120 the precursor mining and library on `PrecursorTester`.
- `RuleSpec.withConditions` was added for variants.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

Implementation commit: `1e616024818132ca41e7448fe8c23d46a4d609fc`
