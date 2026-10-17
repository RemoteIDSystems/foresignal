> ⚠⚠ OWNER OVERRIDE — 2026-10-17: DO NOT RUN ANY TESTS. ⚠⚠
> Ignore every instruction below that says to run tests, the test suite, a checkpoint test run, a soak, a benchmark, a performance smoke, or a coverage/jacoco pass. This prompt is COMPILE-AND-REPORT ONLY: implement any code it specifies, run only `./gradlew compileJava compileTestJava`, write a short completion report noting tests were deferred to the owner at code-complete, commit, and push. Execute NO tests. (Owner directive; prompts/000 enforces the same.)
# 142 — Full Test Run And Fix

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/142-CR-Full-Test-Run-And-Fix.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/141-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- Every Completion Report in `completion-reports/`, especially the checkpoint reports 012, 022, 041, 057 and 097 (known flaky tests, deferred fixes, test-only workarounds) and the "Issues found / open questions" sections of 098–141
- `docs/Core-Data-Model.md` §1 (invariants I1–I10), §17 (test strategy, `TestRecords`, `SchemaParityIT`, operator truth-table fixture)
- `docs/Architecture.md` §12 (test layers, JaCoCo gate) and `build.gradle.kts` (`jacocoTestCoverageVerification`: LINE, BUNDLE, ≥ 0.90, excluding `ForesignalApplication`)
- `docs/Weather-Imagery-Design.md` §10.3 (the full test run includes fixture contract tests for every weather and SatDump output)

## Objective

The final regression before runtime verification. Run the whole suite with coverage and fix every failure at its root cause. Then raise line coverage to **≥ 90% at BUNDLE level** by adding missing tests. No feature work: only fixes, tests and the minimum refactoring needed to make code testable.

## Test run

1. Start Docker if needed (Testcontainers needs it; the Foresignal DB image from `docker/db/Dockerfile` must build). Run `./gradlew clean test jacocoTestReport jacocoTestCoverageVerification`. Use `--continue` so one failing class doesn't hide others, and keep the HTML reports (`build/reports/tests/test`, `build/reports/jacoco/test/html`).
2. Triage failures by root cause and group them (one shared cause → one fix): context-load failures first (they cascade), then schema and bootstrap, then the writer and invariants, then API, then adapters. Fix production code. Change a test only when it asserts the wrong thing, and document each such change (test, old assertion, why it was wrong, new assertion) in the Completion Report.
3. Re-run until green. Record pass/fail/skip counts and the coverage percentage for every run, and every fix (commit-level summary: cause → change → tests affected) in the Completion Report.
4. **Coverage to ≥ 90% LINE (BUNDLE):**
   - From the JaCoCo XML, list packages and classes by missed lines. Write tests for the largest gaps first; count real behaviour, not getters.
   - Allowed exclusions, each justified in the Completion Report: `ForesignalApplication`, generated code, `record` classes with no logic (only if JaCoCo counts their synthetic methods), and `main()`-style dev tools. Never exclude a package to pass the gate. Never weaken the rule element, counter or minimum.
   - The `cli/` build, `sdk/` code and the WASM example are outside the bundle; don't count them in either direction.
5. **Focus areas** (read the code for each, add tests where behaviour is unpinned, fix what breaks):
   - **Core pipeline:**
     - seq monotonic in commit order (I2), `ingested_at` non-decreasing (I3), LATE drops (I4), the exclusive advisory lock (I1), I10 live-row repair
     - persist decisions against the §5.2 table, keyframe and retention keyframe (I6), TRACK geometry carry-forward
     - `PersistGate` vs `RuleSqlBuilder` on the shared `operator-truth-table.json`, including the 114 `approach` condition
   - **History and retention:**
     - PartitionManager `ensureAhead`, detach/finalize, headroom
     - TRACK leaf drops vs STANDARD chunked deletes, `floorSeq` and I7 head purges, the 304-streak `staleBefore` rule, the consumer clamp including sink cursors (110)
     - `HoldCopyStep` and `ArchiveStep` (111) run before deletes and skip on failure; downsampling (044) keeps referenced seqs
   - **Distribution:**
     - SSE replay/live boundary (change before live), `reset` below the floor, `resync` on `live_dropped`, multiplexed filter PATCH (`filter-applied {atSeq}`), stream tickets surviving reconnects
     - `/v1/changes` `410 RESYNC_REQUIRED`, asOf correctness on both tiers (`L`, `S(T)`, `approximate`)
   - **Triggers and delivery:**
     - edge semantics (FIRST, REAPPEAR, tombstone predecessor), cooldown on `observed_at`, `edgeMinInterval`, `max_alerts_per_hour`
     - alert lifecycle and `autoResolve`; channel retries with `Retry-After`, quiet hours, folding, edit-in-place refs; exercise isolation (115)
   - **Access and privacy:**
     - AccessPolicy fail-closed for every unmatched `/v1/**` path; add a test that walks every operation in `docs/OpenAPI.json` and asserts its `x-foresignal-scope` equals the enforced scope
     - restricted tokens, feed keys and share keys bound to their paths
     - private sources excluded from openReads, tiles, embeddings, LLM context, sinks, exports and federation
     - licence egress denials on every `EgressPath` (109)
   - **LLM features:** no test calls a real model (assert the WireMock stubs only); budgets refuse; prompt-injection fixtures for Ask, enrichment, analyst (102), doctor (140) and translation (141)
   - **Adapters and weather:**
     - every adapter has a passing fixture contract test and a fieldSpecs contract test; every weather and SatDump output has a fixture or golden test
     - `.fsg` golden file, MRMS sentinel round trip, tile golden images within tolerance
   - **Contract:** the OpenAPI drift test, Postman completeness test (020), required lint if `npx` is present, event schemas versus emitted payloads
6. **Flakiness:** run the full suite **twice more** after green. Any test that fails intermittently gets fixed (Awaitility with explicit timeouts, `MutableClock` instead of sleeps, deterministic ordering, isolated ports), never `@Disabled`. List any test that still needs a retry, with the reason.
7. Run `./gradlew compileJava compileTestJava` and the `cli/` build's `test` once (record the result; CLI failures are fixed if cheap, else listed).

**Invariants check.** For every `docs/Invariants.md` entry whose owner prompt is ≤ 141, confirm at least one test asserts it (the test named there, or add one) and list the result in the Completion Report.

## Acceptance criteria

- `./gradlew test jacocoTestReport jacocoTestCoverageVerification` passes, with line coverage ≥ 90% at BUNDLE level and the rule unchanged.
- Two consecutive additional full runs are green.
- Every test modification and every coverage exclusion is listed and justified in the Completion Report, with per-run counts.
- No feature work beyond fixes and tests; `docs/Roadmap.md` marks 001–142 Done with their implementation commit hashes.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `142: Full Test Run And Fix`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/142-CR-Full-Test-Run-And-Fix.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `142: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
