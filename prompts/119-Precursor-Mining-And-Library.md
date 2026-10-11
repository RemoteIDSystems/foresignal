# 119 — Precursor Mining And Library

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/119-CR-Precursor-Mining-And-Library.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/118-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principle 8), §5.5 (precursors and lead–lag), §5.6, §14.4
- Completion Reports of 103 (precursor tests: tester, circular-shift baseline, lift, permutation p, reverse test, caveats, job framework), 118 (signal families, `SeriesReader`, findings, statistics package, `unexplained-move`), 100 (anomaly classifications), 101 (intel event types), 117 (situations)

## Objective

Turn 103's one-off "does X precede Y?" test into a discovery and validation loop. Generate candidate patterns automatically (event streams and series pairs), test them on a discovery window with false-discovery control, confirm survivors on a later, disjoint window, and keep the results in a **precursor library** with a lifecycle. When a validated precursor's antecedent occurs, emit a `precursor-active` finding. Everything is deterministic and states its limits.

## Principles (binding)

- **Association, not causation.** Every output says "association in stored data". Reverse-direction tests are always reported.
- **Multiple testing is counted.** Every batch records its size; p-values are BH-adjusted within the batch; validation uses data the discovery step never saw.
- **No look-ahead, coverage-aware** (103's caveats apply unchanged).

## Tasks

### 1. Naming

103 calls the object a **precursor test** (`precursor_test`, `/v1/precursor-tests`, MCP `test_precursor`, job type `precursor`). If 103 was built with the older name `hypothesis`, rename table, classes, endpoints, MCP tools, OpenAPI schemas and Postman items now, keeping old paths as deprecated aliases that return `Deprecation` headers until 131. Record what was renamed in the CR.

### 2. Candidate generation (`ai.elaro.precursor`)

- **Event families** (from 118's registry and data): intel event types (101), alert rule tags (017), anomaly classifications per rule (100), change points per series family (118), `unexplained-move` findings per series family (118), situation activations per theme (117), and reference-import families (120, when present).
- **Event-stream candidates:** (a) operator-registered pairs (`precursor_candidate` rows with antecedent and consequent selectors, spatial rule, lag bounds); (b) automatic pairs among the top-K families (K = 12) per region by occurrence count over the discovery window, with `minLag` 0, `maxLag` from `{1 d, 3 d, 7 d, 14 d}` and spatial rule `sameRegion: country` (admin1 when both families support it).
- **Series candidates:** pairs of enabled analysis series that share a region, theme or topic, or that the operator registers; lags `1…L` (L = 14 for daily, 8 for weekly series).
- Cap: `analysis.precursors.maxTestsPerRun` (500); candidates are ranked by the antecedent's occurrence count and the pair's data overlap, and the cap is reported.

### 3. Testing

- **Windows:** the available range for the pair is split by time: discovery = oldest two thirds, validation = newest third (configurable). Ranges below retention or with coverage gaps over 30% are skipped with `insufficient`.
- **Event streams:** 103's tester on the discovery window (circular-shift baseline, lift, permutation p, reverse test).
- **Series pairs** (`leadlag-ccf` 1.0.0): pre-whiten each series by its baseline residuals (118 baseline for counts; the series' own seasonal median and MAD otherwise); statistic = max over lags `1…L` of the cross-correlation with the antecedent leading; null distribution from 500 circular block-shift surrogates of the antecedent with block length = max(7, first lag at which the residual autocorrelation falls below 0.2); p = (1 + #surrogates ≥ observed) ÷ 501; report the best lag and its sign. Also run the reverse (consequent leading) and flag "co-movement, not precedence" when the reverse is as strong.
- **FDR:** Benjamini–Hochberg at q = 0.10 across the batch; Benjamini–Yekutieli when the batch contains overlapping series pairs. Persist raw and adjusted p.
- **Validation:** each survivor is re-tested on the validation window with the same definition. Event streams: `validated` when the verdict is again `notable` and lift ≥ 1.5 with the Wilson interval for the hit rate above the base rate. Series: `validated` when the same lag sign holds and p ≤ 0.05.

### 4. Library

- `precursor`: `id`, `kind` (event | series), `antecedent` and `consequent` (selectors or series refs), `spatial`, `lag_min`, `lag_max`, `best_lag`, `lag_distribution` (p25, median, p75), `hit_rate` and Wilson interval (event), `base_rate`, `lift`, `correlation` and sign (series), `n_antecedent`, `discovery` and `validation` stats jsonb (window, n, p, adjusted p, verdict), `batch_id` and `batch_size`, `status` (candidate | validated | stale | retired), `status_reason`, `detector_version`, `first_validated_at`, `last_tested_at`, `origin`.
- `precursor_batch`: id, started_at, finished_at, candidates generated, tested, capped, survivors, validated, `q`, method.
- **Lifecycle:** survivors that fail validation stay `candidate` with the reason; a monthly re-test (triggered by 125's monthly cycle, or by `POST /v1/precursors/retest`) uses the newest third of the available range: `validated` → `stale` when lift < 1.2 or the hit-rate interval includes the base rate; `stale` twice in a row → `retired`; a stale pattern that validates again returns to `validated`.
- Plain-language description generated deterministically: "In {regions}, {antecedent} was followed by {consequent} within {lag} in {hits}/{n} cases ({rate}, 95% interval {lo}–{hi}) vs {base} expected by chance; validated on {window}."

### 5. Live watch

- A ChangeBus/AlertBus subscriber with the durable cursor `precursor.cursor` (in the consumer clamp): when an occurrence matches the antecedent of a `validated` event precursor (or a series antecedent crosses its standardised threshold of 2.0 at the best lag), record a `precursor-active` finding (118 table) with the precursor id, the expected consequent window, and the probability `p = (hits + 1) ÷ (n + 2)` shrunk toward the base rate by `n ÷ (n + 20)`. 121 subscribes to these to open forecast questions.

### 6. Scheduling, API, MCP, docs

- Mining runs nightly at 01:30 UTC as a 103 job (`type=precursor-mining`), at most one at a time, with 103's chunking and statement timeouts; `POST /v1/precursors/mine` (agent) runs it now.
- API: `GET /v1/precursors?status&kind&region&family&cursor` (read), `GET /v1/precursors/{id}` (read), `PATCH /v1/precursors/{id}` (agent: retire with reason, or pin as operator-validated, logged and labelled), `GET /v1/precursors/batches` and `/batches/{id}` (agent), `GET/POST/DELETE /v1/precursors/candidates` (agent).
- MCP: `list_precursors`, `get_precursor` (read; read catalogue); `mine_precursors` (agent).
- SSE: topic `analytics` gains `precursor.validated|stale|retired`.
- `docs/OpenAPI.json`: `Precursor`, `PrecursorBatch`, `PrecursorCandidate`; tag `Analytics`; Postman BODIES for a candidate and mine. AccessPolicy rows. Problem codes: `UNKNOWN_PRECURSOR` (404), `PRECURSOR_MINING_IN_PROGRESS` (409).
- `docs/Operator-Guide.md` "Precursors": what validated means, why most candidates fail, multiple testing in plain words, the monthly re-test.

## Tests to write (do not run)

- `CandidateGeneratorTest`: top-K per region, lag grid, cap and ranking, operator pairs always included.
- `LeadLagCcfTest`: planted lagged dependence found at the right lag and sign; independent AR(1) series not significant (fixed seed); reverse flag; block length rule.
- `FdrTest`: BH and BY adjust a known p-vector to reference values.
- `PrecursorValidationIT` (`TestRecords`): a planted precedence valid in both windows becomes `validated`; one present only in the discovery window stays `candidate`; re-test with a decayed pattern → `stale`, then `retired`.
- `PrecursorLiveWatchIT`: an antecedent occurrence produces one `precursor-active` finding with the shrunk probability; cursor restart exactly-once.
- `PrecursorRenameIT` (only if 103 used `hypothesis`): old paths answer with `Deprecation` headers.
- `PrecursorControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Nightly mining generates, tests, FDR-adjusts and validates candidates reproducibly, and records batch sizes.
- Only patterns that hold on later, unseen data are `validated`; the library decays patterns that stop holding.
- Validated precursors produce live `precursor-active` findings with a stated, shrunk probability.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `119: Precursor Mining And Library`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/119-CR-Precursor-Mining-And-Library.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `119: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
