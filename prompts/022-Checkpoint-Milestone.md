# 022 — Checkpoint Milestone

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/022-CR-Checkpoint-Milestone.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/021-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- All Completion Reports `001…021`, especially their "Issues found" and "Notes for the next prompt" sections, and the 012 checkpoint report (fixes already made to the core pipeline)
- `docs/Core-Data-Model.md` §1 (invariants I1–I10), §10 (buses and SSE), §11 (trigger edges), §12 (asOf), §13 (retention), §17 (test strategy)
- `docs/Gap-Analysis.md` §8.A items 4–9 (sync protocol, lifecycle, channel extensions, readiness)
- `docs/OpenAPI.json` and `src/test/resources/openapi/pending-operations.txt` (020)

## Objective

Run every test written in 001–021 and fix failures at their root cause, so the API, alerting and operations layer is proven before platform work (023+) builds on it. No feature work.

## Test run

1. Start Docker if needed (Testcontainers needs it). Run `./gradlew test`. Also run `./gradlew lintOpenApi` (020) and fix spec lint errors.
2. Triage failures by root cause and group them (one cause often fails many tests). Fix production code. Change a test only when it asserts the wrong thing; document each such change with the reason and the doc section that justifies it.
3. Re-run until green. Record pass/fail counts for every run and every fix (cause, files, affected tests) in the Completion Report.
4. Scrutinise these areas even where tests pass, and add a missing test when an invariant below is unprotected:
   - **API contract vs spec:** `OpenApiContractIT` passes with only genuinely unimplemented operations on the pending list (each tagged with its prompt, 023+); no stale entries. `EventSchemaConformanceTest` and `PostmanCollectionContractTest` pass; Postman regenerated if the spec changed during fixes. Every endpoint has an AccessPolicy rule matching its `x-foresignal-scope`; anonymous `/v1/system` leaks nothing beyond the documented subset.
   - **Records API (014):** current reads touch only `record_head ⋈ record_live` (I5); no index on `record_live.observed_at` was added; snapshot paging is stable under concurrent ingest; `watermarkSeq` is read before the query; asOf validity on the transaction axis (`ingested_at + ttl`), `L = S(T − ttl − 2 min)` for TRACK and `0` for STANDARD; the three asOf 400 codes; `410 RESYNC_REQUIRED` below the purge horizon.
   - **SSE replay/live boundary and resync (015):** no duplicate and no missing `change` across the replay→live switch (subscribe before the replay query, dedupe by seq); `change` is the only event with an `id:` line; live and touch events are conflated during replay; `resync` on `gap`, `live_cap` and `live_dropped`; `reset` when `after < RetentionState.lowestRetainedSeq`; the three-box filter sets `leftFilter`; per-token stream limits; the multiplexed `/v1/events/stream` PATCH filter emits `filter-applied {atSeq}`.
   - **Triggers (017):** the shared `operator-truth-table.json` drives both `RuleSqlBuilderIT` and `TriggerPersistGateTest` and they agree; enter/exit edges per Core §11 including tombstone predecessors, `reappearAsEnter` and missing predecessors (fire nothing); HEARTBEAT excluded; cooldown on `observed_at` deterministic; `edgeMinInterval` bounds flapping; I8 holds after rule creation (convergence on the next observation); lifecycle transitions (fire → acknowledge → snooze → auto-resolve on the inverse edge, tombstone or expiry) emit exactly one `AlertBus` event each; `max_alerts_per_hour` enforced.
   - **Channels (018):** golden fixtures for all 15 types; escaping and `Truncator` boundaries; the retry ladder, `Retry-After`, Apprise 424 never retried; no double send under `SKIP LOCKED`; redirects never followed; secrets never in logs or `last_error`; edit-in-place for Discord, Telegram and Matrix with follow-ups elsewhere; quiet hours, pause, folding and grouping under `MutableClock`; Teams connector URLs rejected; every message has a `notification` row.
   - **Retention vs ingest concurrency (019):** retention never writes heads or live rows (I1); a head is purged only below `floorSeq` (I7); a 304 streak never purges; the consumer clamp holds back cutoffs; failed chunk deletes skip HeadPurge for that source; reappearance after a purge restarts at version 1; retention running during a sustained ingest burst produces no writer failure and no lost version above the cutoff.
   - **Partition maintenance (007, 019):** `ensureAhead` keeps ≥ 1.5 blocks of headroom; drops use DETACH CONCURRENTLY outside a transaction and recover with FINALIZE; concurrent ensure from two threads is safe; the writer's pre-flight never fails a batch while a covering leaf exists; `DatabaseCleaner` resets writer state after truncation, so test order does not matter.
   - **Probes and watchdog (019):** liveness never depends on the database; readiness reflects writer state and partition headroom; the catch-up group is independent of readiness.
   - **E2E (021):** `MilestoneE2EIT` passes reliably; run it three times in a row and fix any flakiness (timing, SSE reader races, WireMock state) at its cause rather than by lengthening timeouts.
5. **Performance smoke (record numbers; fail only below the NFRs).** In a dedicated `@Tag("perf")` IT against the Testcontainers database: sustain synthetic ingest through the real writer at the NFR-2 rate (20,000 versions/min) for 10 minutes and at 2× for 2 minutes, with 20 SSE subscribers and one `asOf` reader running; meanwhile force a partition creation and a retention drop. Record sustained throughput, commit latency p50/p95/p99, change-to-SSE delay p95 (NFR-3: ≤ 2 s), heap high-water mark per SSE emitter, and any writer back-pressure. Fail if the NFR-2 rate cannot be sustained or NFR-3 is missed; otherwise report the numbers and the headroom.
6. **Invariants.** For every `docs/Invariants.md` entry whose owner prompt is ≤ 021, confirm a test asserts it (the test named there, or add one), and list the result in the report.
7. Look for test-isolation problems: settings caches (`AppSettingService.invalidateCache()`), the writer's in-memory state, static clocks, and leaked SSE emitters or executor threads between Spring context reuse.

## Acceptance criteria

- `./gradlew test` passes, and `./gradlew lintOpenApi` passes.
- `MilestoneE2EIT` passes three consecutive runs.
- Every fix and every changed test is recorded in the Completion Report with pass/fail counts per run.
- No feature work beyond fixes.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `022: Checkpoint Milestone`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/022-CR-Checkpoint-Milestone.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `022: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
