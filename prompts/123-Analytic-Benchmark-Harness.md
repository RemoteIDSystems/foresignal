# 123 — Analytic Benchmark Harness

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/123-CR-Analytic-Benchmark-Harness.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/122-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2, §8.7 (benchmark harness), §14.3 (what makes analysis credible)
- `docs/Invariants.md` (the `BEN-*` invariants)
- Completion Reports of 101 (recipe dry-run), 103 (job framework, caveats), 116 (evidence groups, observability, manifests), 118 (linker dry-run), 119 (detectors, `SeriesReader` interface), 120 (precursor mining), 121 (base rates, analogs), 122 (ledger scoring, calibration, forecasters)

## Objective

Software tests prove the code does what it says; they do not prove the analysis is any good. Build a **benchmark harness** that measures analytic usefulness before rolling hypotheses (124) and the autonomous cycle roles (127) build on the detectors: false discoveries on null data, recall and detection delay on planted signals, behaviour under reporting outages, duplicate reports and late revisions, resistance to misleading correlations, and calibration and skill of the forecasters against simple baselines. It runs on deterministic synthetic scenarios and on a curated historical corpus, never touches production analytic tables, and publishes pass/warn/fail results that gate the autonomous features.

## Principles (binding)

- **Isolation.** Benchmarks run components in their pure or dry-run modes against in-memory inputs (an in-memory `SeriesReader`, in-memory event streams, the 101 and 118 dry-run paths). They never write findings, events, situations, forecasts or hypotheses to production tables; only `benchmark_*` tables.
- **Determinism.** Every scenario is generated from a seed; the same suite version and seed give identical results.
- **Honest baselines.** Every skill number is relative to a naive baseline (seasonal naive, base rate, climatology).

## Tasks

### 1. Scenario generator (`ai.foresignal.benchmark.scenario`, `bench-scenarios` 1.0.0)

Deterministic generators producing series, event streams and records with **known ground truth**:

| Scenario | What it plants | What a good system does |
|---|---|---|
| `null-seasonal` | seasonal counts or levels with noise, no signal | no significant findings beyond the stated false-alarm rate |
| `step-change` | a level shift of known size at a known time | change point found, delay within bounds |
| `spike` | a short burst | anomaly or exceedance found; no change point |
| `coverage-outage` | true events during a source outage or low detectability | `unknown` / `LOW_DETECTABILITY`, never "absent" or a downward finding |
| `duplicate-echo` | one originator's report re-published 10× by aggregators | one evidence group; confidence and independence unchanged |
| `late-revision` | values or event times revised after first publication | no look-ahead: decisions use the value known at the time |
| `common-driver` | two series sharing a seasonal driver and trend but no lead–lag | no validated precursor; reverse flag or no survivor |
| `true-precursor` | X precedes Y with a known lag and hit rate | validated precursor, lag and rate recovered within intervals |
| `base-rate-drift` | the event rate changes halfway | base rates and analog forecasts adapt; calibration degrades gracefully |
| `pre-event-move` | an abnormal market move before a planted event, plus placebo periods | `pre-event-signal` found with a placebo p consistent with truth; no signal on placebo events |

Each scenario carries its truth record (planted times, sizes, lags, groups) and, where relevant, binary or numeric questions with known outcomes for the forecasters.

### 2. Historical corpus (`benchmark_case`)

- Columns: `id`, `name`, `description`, `window_from`, `window_to`, `region_codes`, `expectations` jsonb (expected finding types or their absence, expected precursor outcomes, questions with known resolutions), `source` (operator | agent | auto), `created_by`, `created_at`.
- **Auto null cases:** monthly, sample 20 random region-weeks from retained history with no intel event ≥ warning and no situation activity as null cases (expect no significant findings).
- Operator and agent cases describe known past episodes (for example a chokepoint disruption or a regional outage) with what the detectors should find. Cases run in replay mode with `asOf` at the window start, stepping forward.

### 3. Metrics (`BenchmarkScorer` 1.0.0)

Per component and scenario type:
- **Detectors (119 change points, event studies, co-occurrence; 100 anomalies):** false-discovery rate on null data, recall on planted signals, precision, detection delay (median and p90), share of correct `unknown` under coverage outages.
- **Evidence (116):** duplicate inflation (any increase in independent count, confidence or evidence quality from echoes must be 0); canonical-event linking precision and recall on planted multi-source events.
- **Precursors (120):** validated false positives on `common-driver` and null scenarios; recovery of `true-precursor` lag and rate.
- **Situations (118):** linking precision and recall on planted storylines; spurious merges.
- **Forecasters (121, 122):** Brier, log score, reliability (CORP) and skill vs the base-rate or climatology baseline on scenario questions with known outcomes, per forecaster.

### 4. Thresholds and gating

- `benchmark_threshold` defaults (in `src/main/resources/intel/benchmark-thresholds.json`, operator-overridable): change-point false-alarm rate on `null-seasonal` ≤ 1.5 × its configured rate; co-occurrence FDR ≤ q + 0.05; duplicate inflation = 0; correct `unknown` under outages ≥ 95%; precursor false validation on `common-driver` ≤ 5%; recall on `step-change` ≥ 80% for effects ≥ 2 SD; base-rate forecaster reliability within ± 0.05 per populated bin.
- Each component's result is `pass | warn | fail`. A run's status is the worst component status.
- **Gating:** `BenchmarkGate.status(component)` exposes the latest status. 127's proposer auto-execution and LLM-forecaster weight in the 122 ensemble require the latest benchmark status of the components they depend on to be `pass` or `warn`; on `fail` they degrade (proposals stay pending; detectors keep running, and their findings carry `BENCHMARK_FAIL` in caveats until the next passing run).

### 5. Runs, API, docs

- `benchmark_run`: `id`, `suite` (synthetic | historical | all), `suite_version`, `seed`, `components`, `status`, `results` jsonb (per component and scenario metrics, thresholds, status), `manifest_id` (116), `started_at`, `finished_at`. A 103 job (`type=benchmark`).
- Schedules: the synthetic suite runs weekly (Sunday 03:00 UTC) and after any detector version change (detected at startup by comparing component versions with the last run); the historical suite runs monthly (127's monthly cycle triggers it).
- API: `POST /v1/benchmarks/runs {suite, components?, seed?}` (agent, 202), `GET /v1/benchmarks/runs?cursor` and `/runs/{id}` (read), `GET /v1/benchmarks/status` (read; latest status per component), `GET/POST/DELETE /v1/benchmarks/cases` (agent).
- MCP: `get_benchmark_status` (read; read catalogue); `run_benchmark` (agent).
- `docs/OpenAPI.json`: `BenchmarkRun`, `BenchmarkComponentResult`, `BenchmarkCase`, `BenchmarkStatus`; tag `Analysis`; Postman entries. AccessPolicy rows. Problem codes: `UNKNOWN_BENCHMARK_RUN` (404), `BENCHMARK_IN_PROGRESS` (409).
- `docs/Operator-Guide.md` "Benchmarks": what each scenario tests, reading the report, adding historical cases, what gating does.
- `docs/Invariants.md`: confirm the `BEN-*` entries name the tests you wrote.

## Tests to write (do not run)

- `ScenarioGeneratorTest`: each scenario is deterministic per seed and its truth record matches the generated data.
- `BenchmarkScorerTest`: metrics on hand-built detector outputs (FDR, recall, delay, duplicate inflation, reliability) against reference values.
- `BenchmarkIsolationIT`: a full synthetic run writes only `benchmark_*` rows; production analytic tables are unchanged.
- `BenchmarkRunIT`: a synthetic run over the real 116/118–122 components produces results for every scenario; a deliberately broken detector (test double) yields `fail`; gating degrades 127 proposer auto-execution and LLM ensemble weight on `fail` (stubbed consumers).
- `HistoricalCaseIT`: an operator case replays with `asOf` stepping; auto null cases sampled deterministically.
- `BenchmarkControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- The synthetic suite measures false discoveries, recall, delay, coverage handling, duplicate resistance, look-ahead safety, precursor validity and forecaster calibration for every intelligence component built so far, deterministically.
- Historical cases replay without look-ahead.
- Results gate autonomous features and are visible per component.
- No production analytic table is written by a benchmark.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `123: Analytic Benchmark Harness`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/123-CR-Analytic-Benchmark-Harness.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `123: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
