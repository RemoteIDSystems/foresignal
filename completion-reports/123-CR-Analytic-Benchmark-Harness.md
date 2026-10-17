# 123-CR-Analytic-Benchmark-Harness

| | |
|---|---|
| Prompt | 123-Analytic-Benchmark-Harness.md |
| Status | Complete (tests written, not run; owner decisions and checkpoint-124 items under Issues) |
| Implementation commit | `3df5e3bd2ed7ece951028c4a47ed353cbc7c4ac7` |
| Date | 2026-10-17 |

## Summary

This prompt builds the analytic benchmark harness under `ai.foresignal.benchmark`. It compiles. The tests are written
but were not run, so no benchmark result below is a measured number: the harness reports them when it runs.

- **Scenario generator** (`benchmark.scenario`, `bench-scenarios 1.0.0`):
  - `ScenarioGenerator` builds all ten scenarios from a seed and a replicate. Each one draws from its own `Rng`
    (`XO_RO_SHI_RO_128_PP` seeded with the SHA-256 of the seed and its name parts, as 119's `Seeds` does). Times are
    fixed from 2024-01-01, never taken from the clock.
  - Every scenario carries a truth record: planted indexes and times, sizes, directions, lags, hit rates, groups,
    outage variants, revision delays, and questions with known outcomes.
  - `InMemorySeries` is the in-memory stand-in for the 119 `SeriesReader`. Each bucket keeps every published version
    with its `knownAt`. `read(from, to, asOf)` returns the newest version known by `asOf`, as the same `SeriesData`
    the detectors get live; a bucket below 0.8 coverage is a gap. `canSeeDecline` stands in for the 116
    `canSeeAbsence`, using the planted coverage and detection probability (the 116 default `absenceMinDetect`, 0.8).
- **Component benchmarks** (`benchmark.component`). Each runs the real component code in its pure or isolated mode
  and pools the results over the replicates:
  - `change-point`: `ChangePointDetector.detect` as the nightly run calls it (400 days of history, alarms in the last 7
    buckets), with decisions every 4 days over 84 days. Each result is classified by the new
    `ChangePointService.classify`, the exact live state and severity logic.
  - `anomaly`: `Baseline.of` and `AnomalyDetector.evaluate` on hour-of-week slots (12 weeks of history, medium
    sensitivity, direction `both`).
  - `event-study`: the live pre-event rule (placebo p ≤ 0.01 and |CAR| ≥ 2) on `EventStudyEngine`. It is measured over
    the range the live check reads, and (`engine*`) over all history known at the event.
  - `co-occurrence`: `CoOccurrenceMiner.mine` at q = 0.05 on each of the last 28 days.
  - `evidence`: `EvidenceGrouper` and the 101 `EventScorer`, before and after ten echoes, over a synthetic originator
    registry.
  - `canonical-linking`: `EvidenceMatcher` under the bundled seismic rule. Observations are linked in memory as the
    `EvidenceResolver` links them: best match per event, and merge into the oldest.
  - `precursor`: the miner's procedure in memory:
    - the default 365-day lookback and a two-thirds discovery share;
    - event candidates at every lag of the default grid, through `PrecursorTester` with 999 shifts;
    - series candidates in both directions, through `Prewhitening` and `LeadLagCcf`;
    - one `Fdr` adjustment per batch;
    - validation by `PrecursorRules`.
  - `situation-linker`: the new `SituationLinker.simulate` (the dry run's procedure from an empty state, in memory, no
    database).
  - `forecaster:base-rate` uses `BaseRateCalculator` with `P28D` windows. `forecaster:analog` uses analogs with the same
    preceding count, shrunk by `AnalogSearch.distribution`. `forecaster:precursor` uses `PrecursorRules.probability`.
    `forecaster:ensemble` is `EnsembleMath.combineBinary` of base rate and analog.
  - Each forecaster's skill is measured against a naive reference: pooled climatology, the base-rate forecaster, or the
    circular-shift base rate.
- **Scorer and thresholds:**
  - `BenchmarkScorer` 1.0.0 holds the pure metric definitions: FDR `V / max(R, 1)`, recall, precision, median and p90
    delay, correct-unknown share, duplicate inflation, pairwise linking precision and recall, spurious merges, Brier,
    log score, reliability over populated bins, CORP miscalibration and skill.
  - `BenchmarkThresholds` loads `intel/benchmark-thresholds.json`, with the setting `benchmark.thresholds` merged over
    it. Comparators are `le`, `ge`, `eq` and `within`, each with a pass and a warn threshold; a metric that cannot be
    measured is a warning. It also holds the gating dependency lists and the finding-type-to-component map.
  - Every default the prompt names is in the file. Corpus checks have the scenario `historical`.
- **Suites and runs:**
  - `SyntheticSuite` is pure. A component that throws gets a failing scenario that names the error.
  - `HistoricalSuite` replays cases:
    - `change-point` expectations step a day at a time from the window start. Each step decides at 03:00 UTC as the
      nightly run does, through the real `SeriesReader` with `asOf` and `knownAt`.
    - Precursor expectations are checked against the precursor's lifecycle status.
    - Questions that carry a `baseRate` definition are forecast by `BaseRateService.computeAsOf` and scored.
    - `sampleNullCases` draws 20 null region-weeks a month, deterministically from the month. It leaves out
      region-weeks with an intel event at warning or above, or with situation activity.
  - `BenchmarkService` queues runs as 103 jobs of type `benchmark` (`BenchmarkJobRunner`). A run's id is its job's id.
    The service stores results, the worst status and a 116 manifest (`benchmark-harness` 1.0.0). It answers 409
    `BENCHMARK_IN_PROGRESS` while a run is queued or running.
  - `BenchmarkScheduler` runs the synthetic suite on Sunday at 03:00 UTC, and at startup when a component's version
    differs from the one last benchmarked. On the first of each month at 03:30 UTC it samples null cases and runs the
    historical suite.
- **Gating (BEN-2):**
  - `BenchmarkGate` gives the latest status per component, from the newest finished run that covered it.
  - `proposerAutoExecutionAllowed` and `llmEnsembleWeightAllowed` need every dependency to be `pass` or `warn`.
  - `findingCaveat` is `BENCHMARK_FAIL` while the component behind a finding type fails.
  - `FindingService.record` adds that caveat to new findings.
  - `EnsembleService.members` leaves `llm` forecasters out while their weight is degraded, and every ensemble forecast
    records `inputs.llmEnsembleWeight`.
  - The gate's cache is dropped when a run finishes and on `IngestStateReset` (when the test cleaner truncates the
    tables).
- **API, MCP and docs:**
  - `api/BenchmarkController` serves the seven operations, with their `AccessPolicy` rows.
  - MCP tools: `get_benchmark_status` (read, also added to the analyst's read catalog) and `run_benchmark` (agent).
  - Problem codes: `UNKNOWN_BENCHMARK_RUN`, `UNKNOWN_BENCHMARK_CASE` and `BENCHMARK_IN_PROGRESS`.
  - New setting key: `benchmark.thresholds`.
  - Operator Guide section "Benchmarks (prompt 123)", two Agent Guide tool rows, and the spec and Postman updates
    listed below.

## Files created / modified

**Created (main):**
- `benchmark/`: `package-info`, `Verdict`, `BenchmarkResults`, `BenchmarkScorer`, `BenchmarkThresholds`,
  `BenchmarkInputs`, `BenchmarkRunEntity`, `BenchmarkRunRepository`, `BenchmarkCaseEntity`,
  `BenchmarkCaseRepository`, `SyntheticSuite`, `HistoricalSuite`, `BenchmarkService`, `BenchmarkJobRunner`,
  `BenchmarkGate`, `BenchmarkScheduler` and `BenchmarkViews`.
- `benchmark/scenario/`: `package-info`, `Rng`, `ScenarioType`, `InMemorySeries` and `ScenarioGenerator`.
- `benchmark/component/`: `package-info`, `ComponentBenchmark`, `ChangePointBenchmark`, `AnomalyBenchmark`,
  `EventStudyBenchmark`, `CoOccurrenceBenchmark`, `EvidenceBenchmark`, `CanonicalLinkingBenchmark`,
  `PrecursorBenchmark`, `SituationLinkerBenchmark`, `ForecasterBenchmark` and `ForecasterBenchmarks`.
- `api/BenchmarkController`, `mcp/tools/GetBenchmarkStatusTool` and `mcp/tools/RunBenchmarkTool`.
- `src/main/resources/intel/benchmark-thresholds.json`.

**Modified (main):**
- `analytics/changepoint/ChangePointService`:
  - The state and severity logic moved, unchanged, into `public static classify(...)` with a `Classification`
    record, so the benchmark judges exactly what a live run writes.
  - `params(frequency)` and `historyBuckets(frequency)` are now public.
- `situation/SituationLinker`: `decide` takes the threshold through a static overload. `simulate(...)` and its
  `Isolated` state are new; live and dry-run behavior is unchanged.
- `analytics/finding/FindingService`: `withBenchmarkCaveat`. `forecast/ensemble/EnsembleService`: the LLM gate.
- `api/error/ErrorCodes`, `security/AccessPolicy` and `settings/SettingKeys` (`BENCHMARK_THRESHOLDS`, added to
  `ALL`).
- `analyst/AnalystRunner`: adds `get_benchmark_status` to the read catalog.

**Docs, spec and tooling:**
- `docs/OpenAPI.json`:
  - The seven operations are `implemented`, with three Problem codes and a changelog entry.
  - `BacktestJob.type` gains `benchmark`.
  - The component strings list all twelve components.
  - Threshold items gain `warn`, `scenario` and `note`.
- `docs/Operator-Guide.md` and `docs/Agent-Guide.md`.
- `scripts/generate-postman.py`: the `/v1/benchmarks/` path variables `benchmarkRunId` and `benchmarkCaseId`, two
  bodies, two captures and the collection variables. `postman/Foresignal.postman_collection.json` is regenerated.
- `src/test/resources/openapi/pending-operations.txt` (−7).

**Tests:**
- New: the classes below.
- Modified: `openapi/PostmanCollectionContractTest` gains validator entries for `createBenchmarkRun` and
  `createBenchmarkCase`.

## Deviations from the prompt

1. **The in-memory series is not a `SeriesReader`.** The 119 reader is a concrete database-backed class. The 122
   report called it an interface, but the code has `public class SeriesReader`. The harness's `InMemorySeries`
   therefore produces the same `SeriesData` through the same `knownAt` contract, and the detectors still read through
   `Grid.knownBy`.
2. **Two small, behavior-preserving refactors** let the benchmark use the live logic instead of copying it:
   - `ChangePointService.classify`;
   - `SituationLinker.simulate`, the 118 dry run without stored situations.

   The 101 recipe dry run is not used. Evidence independence is measured on `EvidenceGrouper` with the 101
   `EventScorer`, which is where duplicate inflation would show.
3. **Detectability in pure mode.** The 116 `canSeeAbsence` needs stored health and estimates. In synthetic runs the
   planted coverage and detection probability decide it, with the same 0.8 bounds. The historical suite uses the real
   `AnalyticsSupport.canSeeDecline`.
4. **Storylines and canonical events need a scenario home.** The prompt's table has no storyline scenario. Situation
   linking is measured on `spike` (five planted storylines as bursts of related inputs) and on `null-seasonal`
   (unrelated inputs only). Multi-source canonical events belong to `duplicate-echo`.
5. **Thresholds the prompt does not set are Foresignal defaults, documented in the file.** Examples: step-change
   delay, spike, anomaly, event-study, linking and situation precision and recall, and skill for the analog, ensemble
   and precursor forecasters. Every threshold has a warn band.
   - "Populated bin" means at least 100 forecasts (`suite.minBinCount`). Below that, sampling noise alone exceeds
     ±0.05.
   - The "1.5 × configured rate" check is the metric `falseAlarmRatio`: confirmed changes per bucket over
     `2 / targetArl` (two-sided). For anomalies it is alerts per bucket over α.
6. **Operator overrides are a setting, not a table.** They live in the setting `benchmark.thresholds`, in the same
   shape as the file, as `intel.originators` and `intel.themes` do. No `benchmark_threshold` table was created,
   because nothing in the spec writes one.
7. **"Correct `unknown` under outages"** is 1 − (wrong downward conclusions ÷ opportunities): replicates for change
   points, and outage hours classified `activity-down` for anomalies. A component that says nothing during an outage
   is also correct, because it drew no "absent" conclusion.
8. **Look-ahead.** `late-revision` counts covered values with `knownAt` after the decision in what the detector read.
   It checks both the reader contract and `knownBy` over every stored version. `oracleDetections` reports what a
   reading without `asOf` would have found.
9. **Historical replay scope.**
   - Only `change-point` finding expectations are replayable. Other finding types are counted as skipped, with a note.
   - Replays are capped at 92 days and 20 series per expectation.
   - Precursor expectations compare the current lifecycle status, because `precursor` is not time-versioned.
   - Questions are scored only when they carry a `baseRate` definition, an additional property allowed by the schema.
10. **Gating strictness.** The prompt requires the latest status to be `pass` or `warn`, so a component never
    benchmarked (`unknown`) also degrades autonomy until the first run. The startup version check queues that first
    run. Findings get `BENCHMARK_FAIL` only on `fail`.
11. **Tables written.** A run writes `benchmark_run` (and the corpus writes `benchmark_case`), plus its 103
    `backtest_job` row and its `analysis_manifest`. These are the job framework and manifest infrastructure the prompt
    requires (a 103 job, a `manifest_id`), not analytic tables. `BenchmarkIsolationIT` asserts that the 35 listed
    analytic tables (those that exist) are unchanged.
12. **Monthly historical run.** Prompt 128 is not built, so `BenchmarkScheduler` runs it on the first of each month at
    03:30 UTC, as 122 did for its refit.
13. **Spec reconciliations (additive, with a changelog entry):**
    - the three Problem codes;
    - `BacktestJob.type` `benchmark`;
    - the threshold items' `warn`, `scenario` and `note`;
    - the full component list in the component descriptions.

## Issues found / open questions

- **Owner decisions:**
  - (a) the thresholds the prompt leaves open (Deviation 5), above all `minBinCount` 100;
  - (b) whether `unknown` should degrade autonomy before the first run (Deviation 10);
  - (c) overrides as a setting rather than a `benchmark_threshold` table (Deviation 6);
  - (d) the appended Operator Guide section and Agent Guide rows versus "documents are replaced whole" in
    `~/Documents/GitLab/CLAUDE.md` (the same open question as 116–122).
- **The live pre-event check (119) can never fire. Found by reading the code, not by running it.**
  - `LiveEventStudyService.preEvent` reads only from `t0 + estFrom − 2` (252 days) to the event.
  - The engine's placebo positions must start at index 250 and stay 16 buckets away from the event at index 252, so
    none is eligible.
  - The placebo p is therefore `NaN`, and `!(placeboP <= 0.01)` returns `NONE` for every event.
  - The benchmark measures the live path as it is (`recall` will be 0, so `event-study` will fail and its findings
    will carry `BENCHMARK_FAIL`). The `engine*` metrics show the same engine with full history.
  - The fix belongs to 119's code (read at least `estFrom − placeboRoom` before the event). It was not made here.
- **Spring may refuse to start the context (checkpoint 124).**
  - A scanned bean with two or more constructors, none `@Autowired` and no no-argument constructor, fails with "No
    default constructor found".
  - Verified with a scratch Spring 7.0.9 context: a class with two public constructors, and one with a public and a
    private constructor, both failed this way.
  - The scan finds this pattern in five pre-existing beans: `evidence/CanonicalRules`, `evidence/OriginatorRegistry`,
    `observability/ObservabilityDeclarations`, `manifest/BuildInfo` (116) and `pol/PolGenerator` (114).
  - `BenchmarkThresholds`, which has the same shape, got `@Autowired` on its injection constructor. The others were
    left for 124: adding `@Autowired` to each one's injection constructor should be enough.
- **Pre-existing test gap for checkpoint 124:** the `PostmanCollectionContractTest` validator gap that 122 reported
  remains for operations before 122. The two 123 bodies are covered.
- **New tables via Hibernate:** `benchmark_run` (indexes `benchmark_run_finished_idx`, `benchmark_run_suite_idx`) and
  `benchmark_case` (`benchmark_case_source_idx`). Prompt 144 should capture them. New setting:
  `benchmark.thresholds`.
- **Checks run:**
  - US English guard (`python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`): 14 tests OK on the final tree.
  - `./gradlew lintOpenApi`: BUILD SUCCESSFUL, "Your API description is valid", with the same 70 earlier warnings.
- No upstream was fetched. No file or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `benchmark/scenario/ScenarioGeneratorTest` | `Rng` determinism; every daily scenario is deterministic per seed and differs for another seed. The truth records match the data: step position, direction and size; spike days; outage span and variant (coverage versus detectability); revision versions and their invisibility before `knownAt`. Also the in-memory reader's `asOf`, coverage and bounds; hourly spike and outage; pre-event planted versus placebo returns; co-occurrence planted pairs; precursor hit share at the planted lag; common-driver versus independent weekly patterns; echo members; canonical observation order and coverage; storyline inputs; forecasting outcomes and drift; scenario wire names |
| `benchmark/BenchmarkScorerTest` | FDR, recall, precision, rates, correct-unknown share, median and p90 delay, duplicate inflation, pairwise linking and spurious merges, Brier, log score, populated-bin reliability, CORP and skill against hand values; every comparator's verdicts; `evaluate`, headline keys and component status; the bundled thresholds cover every component with the prompt's defaults |
| `benchmark/component/ComponentBenchmarksTest` | Every component measures every scenario with the metrics its thresholds check; determinism per seed; duplicate inflation 0 and exact canonical linking (true by construction); no look-ahead in the change-point replay; a broken detector double fails; a throwing component fails with its error |
| `benchmark/BenchmarkIsolationIT` | BEN-1: a full synthetic run through the 103 job leaves the 35 listed production analytic tables (those that exist) unchanged, adds one `benchmark_run` and one job row, and stores results for all twelve components with a manifest |
| `benchmark/BenchmarkRunIT` | BEN-2: a synthetic run over the real components gives results for every scenario of every component and covers all ten scenarios; a broken detector double yields `fail`; on a failing run a stubbed 128 proposer stays pending, the real 122 ensemble leaves the LLM forecaster out and new findings carry `BENCHMARK_FAIL`; after a passing run all three are lifted |
| `benchmark/HistoricalCaseIT` | An operator case replays a planted level shift with `asOf` stepping (21 decisions, found, zero look-ahead, a later revision never read), and a non-replayable expectation is skipped; null cases are sampled deterministically (the same month gives the same 20), leave out region-weeks with a warning event or situation activity, add the remainder on the next run, and replay with no unexpected finding |
| `api/BenchmarkControllersIT` | Runs: 403 for read, 202 for agent, 409 `BENCHMARK_IN_PROGRESS`, reading the finished run, list filters and 400s, 404 `UNKNOWN_BENCHMARK_RUN`, body validation. Status per component with gating. Cases: scopes, source by scope, list by source, validation, delete and 404 `UNKNOWN_BENCHMARK_CASE`. MCP: scopes and shapes of `get_benchmark_status` and `run_benchmark` |

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **124 (checkpoint):**
  - Fix the five multi-constructor beans above first, or the context will not start.
  - Then expect `event-study` to fail its benchmark until `LiveEventStudyService.preEvent` reads enough history for
    placebo dates.
  - The ITs shorten runs with the setting `benchmark.thresholds` = `{"suite": {"replicates": 1}}`.
  - Benchmark threshold failures are results, not defects. Check them against the thresholds file before changing
    code.
- **125 (hypotheses):** an LLM-driven hypothesis step should check `BenchmarkGate.allows(...)` for the components its
  evidence rests on.
- **128 (cycles):**
  - The proposer calls `BenchmarkGate.proposerAutoExecutionAllowed(dependsOn)`; with null, every gated component
    counts.
  - The monthly cycle calls `HistoricalSuite.sampleNullCases(now)` and then
    `BenchmarkService.submit(new BenchmarkInputs.RunRequest("historical", BenchmarkInputs.COMPONENTS, null),
    "scheduler", "schedule")`. Then remove the monthly cron in `BenchmarkScheduler`.
  - Add the cycle's own components to `BenchmarkInputs.COMPONENTS`, as `ComponentBenchmark` beans and thresholds rows.
- **New components:** implement `ComponentBenchmark` (a bean), add the name to `BenchmarkInputs.COMPONENTS`, add rows
  to `intel/benchmark-thresholds.json` (and `gating` if it gates autonomy), and the version check will benchmark it
  at the next start.
- **Tests:** `foresignal.analytics.scheduled` is false in tests. Call `BenchmarkService.execute(...)` for pure runs and
  `submit(...)` plus polling for job runs. `HistoricalSuite.run/sampleNullCases` can be called directly.
- **144 (Flyway):** capture `benchmark_run`, `benchmark_case` and their indexes, and the setting
  `benchmark.thresholds`.

Implementation commit: `3df5e3bd2ed7ece951028c4a47ed353cbc7c4ac7`
