# 041 — Checkpoint Platform

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/041-CR-Checkpoint-Platform.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/040-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- Completion Reports 023–040 (every "Deviations" and "Issues found" section), plus the 022 checkpoint report for known-flaky areas
- `docs/Core-Data-Model.md` §1 (invariants I1–I10), §10 (SSE), §12 (asOf), §13 (retention and the consumer clamp)
- `docs/Architecture.md` §15 (scopes), §18 (MCP), §19 (LLM safety), §20 (embeddings), §21 (Ask), §22 (briefings), §24 (Web Push)
- `docs/Gap-Analysis.md` §1 items 4, 5 and 7, §6 and §7 (the platform behaviours 023–040 were meant to deliver)
- `docs/OpenAPI.json` (every `x-elaro-scope`)

## Objective

Run the whole suite written by prompts 001–040 and make it green before the weather, imagery and adapter work starts. This block added the security perimeter, agents, the LLM features, push, privacy and history playback. Those are the areas where a silent bug leaks data or spends money, so fix root causes and add no features.

## Test run

1. Start Docker if needed (Testcontainers needs it; the DB image is built from `docker/db/Dockerfile`). Run `./gradlew test`.
2. Triage failures by root cause and group them; one cause often fails many ITs. Fix production code. Change a test only when it asserts the wrong thing, and document each such change (test, old assertion, new assertion, why) in the Completion Report.
3. Re-run until green. Record the pass/fail/skip counts for every run and every fix (cause, files, affected tests) in the Completion Report.
4. **Focus areas.** Scrutinise each of the following even when its tests pass. If an invariant below has no test, add a minimal one; that is the only new test code allowed.
   - **Tokens and scopes vs spec (023, 035):**
     - `AccessPolicySpecSyncTest` covers every operation now in `docs/OpenAPI.json` (37 prompts added paths).
     - The `TokenFilterIT` matrix covers read, agent, admin, restricted, expired and revoked tokens, plus stream tickets, feed keys and tile tickets, each with `openReads` on and off.
     - `alwaysAuthenticated` endpoints (Ask, search, push-subscribe, LLM-backed MCP tools, mapping suggest if present) refuse anonymous callers even with `openReads=true`.
     - Unmatched `/v1/**` paths still require admin.
     - Revoking a token cascades to its push subscriptions and stream tickets.
     - Audit rows exist for writes and agent tool calls.
   - **Ingest formats (024, 039):**
     - `alertmanager`, `cloudevents`, `cap` (XXE disabled: DTD and external entities rejected), `mapping`, `owntracks`, `osmand`, `overland`, `gpslogger`.
     - The ingest key is accepted as `X-Elaro-Ingest-Key`, Bearer and Basic; device keys are bound to one entity.
     - Each format's reply shape is exact (OwnTracks `[]`, Overland `{"result":"ok"}`).
     - Keys never appear in logs or `fetch_log.url_redacted`.
     - ntfy/Gotify-compatible publish paths work.
   - **MCP (025):**
     - Tool visibility per scope.
     - The 50 KB cap and short-mode caps.
     - `set_alert_status`, `get_state_at`, `get_track`, `get_glance`, `reverse_geocode`.
     - Admin-only tools (`purge_entity`, `export_entity`) are absent for agent tokens and never offered to Ask's loop.
   - **LLM stubs (026):** WireMock stubs for Anthropic, OpenAI-compatible and Ollama; budget refusal → `LLM_BUDGET_EXCEEDED` plus a system event; `stop_reason == "refusal"` is handled before content is read; no test reaches a real provider. Fail any test that opens a socket outside localhost.
   - **Embeddings reindex (027):** zero-downtime dual index (searches answer during reindex and switch atomically); HNSW DDL with a dynamic dimension; the `embedding.cursor` consumer clamp; unchanged `text_hash` skipped.
   - **Ask injection containment (028):** record text with embedded instructions cannot trigger tool calls outside the catalogue or any configuration write; citations to ids never returned by tools are dropped; proposed actions need confirmation unless `allowActions` is set with agent scope.
   - **Briefings scheduling (029):** cron plus timezone across a DST change; missed runs run once; no overlap; `SKIP LOCKED` with two schedulers; LLM fallback to digest; digest counts now come from the 038 stats service.
   - **Push crypto (031):** RFC 8291 Appendix A and RFC 8188 known-answer vectors pass byte-for-byte; VAPID ES256 JWT verifies; the UnifiedPush allowlist, send-time IP recheck, registration verification and 4 KB byte cap hold.
   - **APNs and FCM (032):** WireMock stubs; the APNs ES256 provider token is cached and refreshed; visible alert payloads carry only id and severity; `410` (APNs `Unregistered`, FCM `UNREGISTERED`) deletes the device.
   - **Action signing (033):** signed actions are single-use (replay → 409/410), expire, and are bound to the action and alert; GET opens a confirm page and never mutates; presence deferral still sends critical alerts.
   - **Perimeter and SSRF (035):** the outbound URL policy blocks loopback, RFC 1918, link-local, ULA, metadata IPs and DNS rebinding (resolve, pin, recheck) for operator-supplied URLs unless allowlisted; redirects are not followed off-policy; per-client rate limits back off rather than lock out.
   - **Places and watchlists (036):** CSV import validation; `inWatchlist` operator; a moving-anchor "near me" rule re-evaluates when the anchor moves; saved views render consistently across records, tiles and SSE.
   - **asOf and tracks (037):** frames reconstruct asOf at every boundary; past-tile `immutable` only under the stated rule; the `/track` antimeridian split; `ASOF_BEYOND_RETENTION` at the horizon.
   - **Stats (038):** rollup counts survive deletion of raw rows; the rollup cursor is in the retention clamp; no double counting after restart; glance ≤ 2 KB with no anonymous counts; `/v1/metrics` is not on the open actuator.
   - **Private enforcement (039):** run the full `PrivateEnforcementIT` matrix; SSE hub filtering; no private text reaches a remote LLM; purge leaves no older versions and fans out the tombstone.
   - **Gazetteer (040):** region tagging never changes hashes or writes versions; the `geofenceRef` truth-table row matches between SQL and the gate; the bundled `CountryCentroids` works offline.
   - **Cross-cutting:**
     - I1 (writer-only writes) is still true: `EntityPurge` runs on the writer thread, and `record_region` and the rollup tables are not touched by the writer.
     - Durable cursors (trigger, embedding, rollup, region) resume without gaps or duplicates.
     - `DatabaseCleaner` covers every new table.
     - The Spring context count stays low; record any new context-cache splits and fix accidental ones.

## Acceptance criteria

- `./gradlew test` passes.
- Every test whose assertion changed is listed and justified in the Completion Report.
- No feature work beyond fixes.
- Compiles.

## Guardrails (mandatory)

1. **Tests: this is a test-run prompt.** Run the test suite as the Test run section describes, and fix failures until it passes. Fix production code, and change a test only when the test itself is wrong (record each such change in the Completion Report). Don't add features.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `041: Checkpoint Platform`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/041-CR-Checkpoint-Platform.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `041: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
