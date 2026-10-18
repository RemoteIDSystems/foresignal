> ⚠⚠ OWNER OVERRIDE — 2026-10-17: DO NOT RUN ANY TESTS. ⚠⚠
> Ignore every instruction below that says to run tests, the test suite, a checkpoint test run, a soak, a benchmark, a performance smoke, or a coverage/jacoco pass. This prompt is COMPILE-AND-REPORT ONLY: implement any code it specifies, run only `./gradlew compileJava compileTestJava`, write a short completion report noting tests were deferred to the owner at code-complete, commit, and push. Execute NO tests. (Owner directive; prompts/000 enforces the same.)
# 124 — Checkpoint Intelligence Foundations

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/124-CR-Checkpoint-Intelligence-Foundations.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/123-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- The Completion Reports for 098–123, especially each report's "Tests written (not run)", "Deviations" and "Notes for the next prompt" sections; the 097 checkpoint report
- `docs/Intelligence-Design.md` §2 (principles), §4.5–§4.7 (evidence, observability, manifests), §5 (discovery), §7–§8 (base rates, ledger, benchmark)
- `docs/Conventions.md` "Analytic retention" and "Intelligence conventions"; `docs/Invariants.md`

## Objective

Run the whole suite built so far (001–123) and fix what fails, before hypotheses, cycles, collaboration, chronicles, social signals and external data build on the intelligence foundation. Prompts 098–123 added entities, derived layers, anomalies, incidents, the analyst, backtests, evidence identity and observability, market and attention series, situations, change points and event studies, precursors, base rates and analogs, the forecast ledger and the benchmark harness, with tests written but never run. Statistical code can produce plausible but wrong numbers, so this checkpoint verifies the properties later prompts rely on.

## Test run

1. Start Docker if needed (Testcontainers). Run `./gradlew test`.
2. Triage failures by root cause and group them. Fix production code. Change a test only if it asserts the wrong thing, and document each such change with the reason.
3. Re-run until green. Record pass/fail counts per run and every fix in the Completion Report.
4. Scrutinise these focus areas. Where a required test is missing, write it, run it and fix what it finds:
   1. **Golden statistics.** Every function in `ai.foresignal.analytics.math` (CUSUM, BOCPD, Benjamini–Hochberg/Yekutieli, Wilson, Brier, log, RPS, CRPS, Murphy and CORP decompositions, PAV, Platt) has a golden-value test against independently computed reference values (document each source).
   2. **Determinism.** `DeterminismTest` runs every detector, scorer, miner, base-rate and analog component and forecaster from 100–123 twice on identical inputs, seeds and `asOfSeq` and asserts identical output, including permutation, placebo and bootstrap statistics.
   3. **No look-ahead.** `LookAheadTest` inserts records and series points after a cutoff and asserts unchanged outputs for event studies (batch and live), precursor discovery and validation windows, analog search, base rates and retrospective ledger evaluation; live checks ignore points whose `knownAt` is after the decision time.
   4. **Evidence and observability.** A planted duplicate-echo stream never raises independence, corroboration or confidence (`DuplicateInflationTest`); absence conclusions with low detectability are `unknown` in 100, 119, 120 and 122.
   5. **Manifests.** Every analytic output table from 100 onward has a populated `manifest_id`; a sample of deterministic manifests replays `identical` (`ManifestReplayCheckTest`).
   6. **Ledger integrity.** Forecasts are immutable; `made_at` and `asof_seq` are server-set; annulled questions are never scored; time-averaged scoring carries forecasts forward; LLM caps and Platt recalibration apply only to their forecasters.
   7. **Egress.** `IntelligenceEgressTest` (first version): private and `LOCAL_ONLY` inputs (a private source, FRED, ACLED-derived data) never appear in `openReads` responses, non-local tokens' responses, SSE payloads, MCP tool output, tiles or LLM request fixtures.
   8. **Durable cursors.** The retention consumer clamp includes `evidence.cursor`, `situation.cursor`, `stats.region.cursor` and `precursor.cursor`; each subscriber resumes exactly once after a restart.
   9. **Benchmark.** Run the 123 synthetic suite; every component must be `pass` or `warn`. Fix `fail`s as defects (detector, threshold or scenario) and record the metrics; these become the baseline 139 compares against.
   10. **API surface.** Every endpoint from 098–123 is in `docs/OpenAPI.json` with `x-foresignal-scope`, in AccessPolicy and in the Postman collection; 020's drift and coverage tests pass; `pending-operations.txt` lists only operations that are genuinely not built.
   11. **Invariants.** Every `docs/Invariants.md` entry owned by a prompt ≤ 123 names at least one existing test that asserts it (`InvariantRegistryTest`).
   12. **Coverage.** Run `./gradlew jacocoTestReport` and report line coverage for `ai.foresignal.entity`, `anomaly`, `incident`, `evidence`, `observability`, `manifest`, `attention`, `situation`, `analytics`, `precursor`, `baserate`, `forecast` and `benchmark`. Raise any package under 85% toward 90% with meaningful tests, or explain why not.

## Acceptance criteria

- `./gradlew test` passes.
- `DeterminismTest`, `LookAheadTest`, `DuplicateInflationTest`, `ManifestReplayCheckTest`, `IntelligenceEgressTest` and `InvariantRegistryTest` exist and pass; the 123 synthetic benchmark has no `fail`.
- The report records pass/fail counts per run, every fix grouped by root cause, each test changed with its reason, the benchmark metrics, and coverage per package.
- No feature work beyond fixes; compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `124: Checkpoint Intelligence Foundations`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/124-CR-Checkpoint-Intelligence-Foundations.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `124: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
