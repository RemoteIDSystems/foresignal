> ⚠⚠ OWNER OVERRIDE — 2026-10-17: DO NOT RUN ANY TESTS. ⚠⚠
> Ignore every instruction below that says to run tests, the test suite, a checkpoint test run, a soak, a benchmark, a performance smoke, or a coverage/jacoco pass. This prompt is COMPILE-AND-REPORT ONLY: implement any code it specifies, run only `./gradlew compileJava compileTestJava`, write a short completion report noting tests were deferred to the owner at code-complete, commit, and push. Execute NO tests. (Owner directive; prompts/000 enforces the same.)
# 139 — Checkpoint Intelligence

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/139-CR-Checkpoint-Intelligence.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/138-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- The Completion Reports for 098–138, especially each report's "Tests written (not run)", "Deviations" and "Notes for the next prompt" sections; the 097 checkpoint report
- `docs/Intelligence-Design.md` §2 (principles) and §15 (prompts 116–138)
- `docs/Conventions.md` "Analytic retention" and "Intelligence conventions"

## Objective

Run the whole suite built so far (001–138) and fix what fails. The intelligence block (098–138) added detectors, scorers, updaters, forecasters and LLM roles whose tests were written but never run, and much of it is statistical code where a subtle error produces plausible but wrong numbers. This checkpoint makes the suite green and verifies the properties the intelligence layer promises: determinism, no look-ahead, honest wording, and private and licensed data staying where it belongs.

## Test run

1. Start Docker if needed (Testcontainers). Run `./gradlew test`.
2. Triage failures by root cause and group them. Fix production code. Change a test only if it asserts the wrong thing, and document each such change with the reason.
3. Re-run until green. Record pass/fail counts per run and every fix in the Completion Report.
4. Scrutinise these focus areas. Where a required test is missing, write it, run it and fix what it finds:
   1. **Golden statistics.** Every function in `ai.foresignal.analytics.math` and the 126 forecasting methods has a golden-value test against reference values computed independently (document the source of each reference value: a textbook example, a published table, or a calculation shown in the test). Brier, log, RPS, CRPS, Murphy and CORP decompositions sum correctly.
   2. **Determinism.** A `DeterminismTest` runs every detector, scorer, updater, forecaster and miner twice on identical inputs, seeds and `asOfSeq`, and asserts identical output (including permutation, placebo and bootstrap statistics).
   3. **No look-ahead.** A `LookAheadTest` inserts records and series points after a cutoff and asserts unchanged outputs for: event studies (batch and live), precursor discovery and validation windows, method selection (126), index fitting, retrospective evaluation (122) and analog search (121). Live pre-event checks ignore points whose `knownAt` is after the event.
   4. **Wording.** Every template (revision rationale, assessment sections, finding explanations, precursor descriptions) and every text fixture passes `EstimativeLanguage.lint`; a test generates each template across all ICD bands.
   5. **Egress.** An `IntelligenceEgressTest` seeds private and LOCAL_ONLY inputs (a private source, FRED and ACLED-derived data) and asserts they never appear in `openReads` responses, non-local tokens' responses, SSE payloads, MCP tool output, tiles, embeddings sent to a cloud provider, or LLM request fixtures (derived standardised statistics are allowed where the docs say so).
   6. **Durable cursors.** The retention consumer clamp includes `situation.cursor`, `stats.region.cursor`, `precursor.cursor`, `impact.cursor` and `social.cursor`; each subscriber resumes exactly once after a restart.
   7. **Ledger integrity.** Forecasts are immutable; `made_at` and `asof_seq` are server-set; annulled questions are never scored; time-averaged scoring carries forecasts forward; LLM caps and Platt recalibration apply only to their forecasters.
   8. **API surface.** Every new endpoint from 116–138 is in `docs/OpenAPI.json` with `x-foresignal-scope`, in AccessPolicy and in the Postman collection; 020's drift and coverage tests pass; `src/test/resources/openapi/pending-operations.txt` lists only operations that are genuinely not built.
   9. **Evidence and observability.** A planted duplicate-echo stream never raises independence, confidence, corroboration or hypothesis probabilities (`DuplicateInflationTest`); absence conclusions with low detectability are `unknown` everywhere (100, 119, 120, 122, 125).
   10. **Manifests.** Every analytic output table from 100 onward has a populated `manifest_id`; a sample of deterministic manifests replays `identical` (`ManifestReplayCheckTest`).
   11. **Benchmark.** Run the 123 synthetic suite; every component must be `pass` or `warn`. Fix `fail`s as defects (a detector, threshold or scenario bug) and record the metrics in the report.
   12. **Objects and workspaces (129).** Every object type resolves to an envelope; the relation projector's output matches its source structures after a full reconcile; two simulated clients stay in sync on a workspace through SSE with version checks.
   13. **Collaboration, chronicles, reports, news, chronicle network (130–134).** Access lists and TLP hold for every shareable type across REST, envelopes, search, SSE, exports, share links and packages (`CollaborationEgressTest`); a chronicle keeps and replays its history after global retention removes the source rows (`ChronicleRetentionIndependenceIT`); a signed package round-trips between two instances read-only (`ExchangeRoundTripIT`); news reprints never count as independent and author names come only from bound EDP article documents (`NewsIndependenceTest`); chronicle discovery never discloses beyond visibility, discoverability and TLP, and circular reporting is counted once (`DisclosureTest`, `CircularReportingTest`).
   14. **Social signals and external data (024, 135–138).** Social claims never corroborate physical events or their own narratives, and amplification never adds evidence groups (`SocialIndependenceTest`: a planted 10,000-repost cascade from 3 posts gives 3 groups and leaves physical corroboration unchanged); `unobserved` and attention-gap outcomes appear only when detectability or social coverage is adequate (`SocialAbsenceTest`); cue results never feed velocity baselines and a cue never corroborates its own trigger (`SocialCueLoopTest`); the X spend cap is never exceeded (`XSpendTest`); deletion sync tombstones posts while chronicles keep claim history (`SocialDeletionIT`); the 123 social scenarios pass. External data (024, 138): items from a pending source are never stored, atomic items do nothing until bound, bound EDP events count once per declared originator and corroborate other sources only through independent evidence groups, and external forecasts never carry a `made_at` before receipt and join the ensemble only after 30 resolutions (`EdpIngestIT`, `EdpEvidenceIndependenceTest`, `EdpForecastBindingTest`); the EDP conformance fixtures validate as labelled (`EdpSchemaExportTest`).
   15. **Invariants.** Every `docs/Invariants.md` entry owned by a prompt ≤ 138 names at least one existing test that asserts it (`InvariantRegistryTest` checks names against the test sources).
   16. **Coverage.** Run `./gradlew jacocoTestReport` and report line coverage for `ai.foresignal.evidence`, `observability`, `manifest`, `object`, `collab`, `chronicle`, `report`, `media`, `attention`, `situation`, `analytics`, `precursor`, `baserate`, `forecast`, `hypothesis`, `outlook`, `impact`, `benchmark`, `cycle`, `social` and `inbound.edp`. Raise any package under 85% toward 90% with meaningful tests, or explain why not.

## Acceptance criteria

- `./gradlew test` passes.
- `DeterminismTest`, `LookAheadTest`, `IntelligenceEgressTest`, `DuplicateInflationTest`, `ManifestReplayCheckTest` and `InvariantRegistryTest`, `CollaborationEgressTest`, `ChronicleRetentionIndependenceIT`, `NewsIndependenceTest`, `SocialIndependenceTest`, `SocialAbsenceTest`, `SocialCueLoopTest`, `EdpEvidenceIndependenceTest` and `EdpForecastBindingTest` exist and pass; the 123 synthetic benchmark has no `fail`.
- The report records pass/fail counts per run, every fix grouped by root cause, each test changed with its reason, and coverage per intelligence package.
- No feature work beyond fixes; compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `139: Checkpoint Intelligence`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/139-CR-Checkpoint-Intelligence.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `139: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
