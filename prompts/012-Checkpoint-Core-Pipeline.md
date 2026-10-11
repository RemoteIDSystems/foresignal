# 012 — Checkpoint Core Pipeline

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/012-CR-Checkpoint-Core-Pipeline.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–134).
- **Previous Completion Report:** read `completion-reports/011-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- Completion Reports 001–011, especially every "Deviations" and "Notes for the next prompt" section
- `docs/Core-Data-Model.md` §1 (invariants I1–I10), §5.2 (decision table), §6 (writer), §7 (streaming sinks), §8–§9 (tables, partitions), §17 (test strategy: this checkpoint replaces the "sanctioned test runs" of 006a, 007 and 007b, i.e. prompts 007, 008 and 009)
- `docs/Architecture.md` §12 (testing strategy)

## Objective

Run every test written in prompts 001–011 for the first time and make the suite green by fixing production code. The core pipeline (schema, normalizer, policy engine, writer, buses, scheduler, first adapter) is what every later prompt builds on, so scrutinise its invariants rather than just silencing failures.

## Test run

1. Start Docker if needed (Testcontainers needs it). Run `./gradlew test`.
2. Triage failures by root cause and group them (for example: schema bootstrap ordering, Hibernate mapping, partition coverage, writer SQL, bus timing, test-context sharing). Fix production code. Change a test only if it asserts the wrong thing, and document each such change with the reason and the doc section it now matches.
3. Re-run until green. Record pass/fail/skip counts for every run and every fix (file, cause, change) in the Completion Report.
4. Scrutinise these areas even where tests pass, and add a missing assertion only when a stated invariant has none:
   - **Schema parity and partitions:** `SchemaParityIT` passes with a second Hibernate update emitting nothing for `record_version`; `HistorySchemaInitializer` runs before the EntityManagerFactory; both tier parents exist; coverage ≥ 1.5 blocks ahead; range-based `ensureRange` recreates a dropped low block (tests restart `record_seq` at 1); `DETACH … CONCURRENTLY` never runs inside a transaction; FINALIZE recovery; `DatabaseCleaner` skips `pg_inherits` children and calls `IngestStateReset`.
   - **Writer invariants I1–I10:** only the writer thread writes `record_version`/`record_head`/`record_live`/`seq_clock` (I1); seq strictly increasing in commit order across concurrent submitters, with `lastAllocatedSeq` advanced even on rollback (I2); `ingested_at` never decreases (I3); LATE drops leave no side effects (I4); one live row per head after every batch, including the repair path (I10); `record_version` touched only by native SQL (I9).
   - **Policy matrix:** `IngestPolicyEngineTest` rows 1–12 with their boundaries match Core-Data-Model §5.2 exactly, first match wins; HEARTBEAT vs KEYFRAME on `dirty`; retention keyframe at `window/4`; TRACK carry-forward never flips the static hash.
   - **Touch and heartbeat:** touch counts equal row counts; throttling on received OR observed time against `touchMin`; touches folded into the live upsert when the entity also persisted or went LIVE; HEARTBEAT versions written only when keyframe is due.
   - **LATE and clock guard:** equal `observed_at` accepted in arrival order; future clamp with `FUTURE_CLAMPED` only beyond skew; writer holds batches when the clock is more than 5 min behind `lastIngestedAt` and resumes when it catches up; jump metric.
   - **fetch_log:** exactly one row per fetch (OK via the writer with counts and seqs; NOT_MODIFIED, FAILED, REFUSED, DISCARDED written by the collector); `last_data_at` untouched by 304s and empty results; STREAM rows per flush; `seq_clock` written on every batch, including zero-version batches.
   - **Scheduler timing:** enable → first fetch within 3 s; disable stops further fetches and discards in-flight results; PUSH and DERIVED never polled; overlap skipped; `PolicyAwareRecordSink` keeps one outstanding batch, latest-wins for SAMPLED/LATEST, blocking then dropping at the ALL cap.
   - **USGS end to end through the writer:** first fetch → FIRST versions; revision → one SIGNIFICANT version 2; identical re-fetch → 0 versions and a (throttled) touch; `updated`-only change → 0 versions; 304 → NOT_MODIFIED row.
   - **Flakiness:** run the IT suite twice; any test that fails intermittently is fixed (no sleeps; Awaitility with explicit conditions; `MutableClock` for time).

**Invariants check.** For every `docs/Invariants.md` entry whose owner prompt is ≤ 011, confirm at least one test asserts it (the test named there, or add one) and list the result in the Completion Report.

## Acceptance criteria

- `./gradlew test` passes.
- No feature work beyond fixes; every changed test is justified in the Completion Report.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 139).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `012: Checkpoint Core Pipeline`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/012-CR-Checkpoint-Core-Pipeline.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `012: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
