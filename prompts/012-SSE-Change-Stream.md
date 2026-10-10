# 012 — SSE Change Stream

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/012-CR-SSE-Change-Stream.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`.
- **Previous Completion Report:** read `completion-reports/011-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Architecture.md` §8.2 (SseHub)
- `docs/OpenAPI.json` operation `streamChanges` and schema `Change`
- `docs/Product-Charter.md` FR-21, NFR-3

## Objective

Give frontends a live, filtered, resumable change stream, so a globe can subscribe to just the kinds and region in view. This is the display half of subscription/display/alerting, and nothing here reaches the collector. Build the SSE machinery generically so prompt 014 can reuse it for alerts.

## Tasks

### 1. Generic SSE channel (`ai.elaro.stream.sse`)

- `SseChannel<E>`: manages client connections for one event type.
- `SseClient<E>`: wraps a Spring `SseEmitter` (timeout 0, meaning infinite) with:
  - a bounded outbound queue (capacity from `stream.limits.clientBuffer`, default 1000)
  - a dedicated **virtual** thread that drains the queue and sends
  - a filter predicate
- **Replay then live, with no gap and no duplicates.** On connect with a resume point `after`:
  1. Register the client as "buffering" so live events are queued but not yet sent.
  2. Replay from the database in pages (`seq > after`, filtered), up to a replay cap (default 10,000 events). If the cap is hit, send a `reset` event and stop replaying.
  3. Flush buffered live events, **skipping ids ≤ the last replayed id**.
  4. Switch to live mode.
- **Heartbeat:** every 15 s, sent as a named `heartbeat` event with data `{}`. A comment line is not enough, because some clients ignore comments.
- **Overflow:** if a client's queue fills, send `reset` (`data: {"reason":"buffer_overflow","resumeAfter":<lastSentId>}`) and complete the emitter. The client resumes via `Last-Event-ID`.
- **Limits:** a max-client limit from `stream.limits.maxClients` (default 100); beyond it, return 503 problem `code STREAM_LIMIT`.
- Clean up on completion, timeout or error. Track counts for `/v1/system`.
- Expose a `StreamLimits` settings record (`maxClients`, `clientBuffer`, `replayCap`, `heartbeatSeconds`) under app setting `stream.limits`, with defaults. No API to change it yet; prompt 016 may add one.

### 2. Change stream (`/v1/changes/stream`, operationId `streamChanges`)

- Resume point: `Last-Event-ID` header (preferred), else the `after` query param, else "live only from now" (`after = ChangeBus.lastPublishedSeq()`).
- Filters: `source`, `kind`, `bbox` (same parsers as prompt 011).
  - A change matches the bbox if its summary bbox intersects the filter. Changes without geometry are **excluded** when a bbox filter is set.
  - Tombstones are delivered so clients can remove the feature, using the last known bbox from the summary. Tombstone summaries carry the previous geometry's bbox; extend the writer if needed and record the change.
- `payload=true`: live events need the payload, but the bus carries summaries. Load payloads in small batches by seq (`WHERE seq = ANY(?)`) on the client's sender thread.
- **Event format:** `id: <seq>`, `event: change`, `data: <Change JSON>` (geometry as GeoJSON). Use the same DTO as `/v1/changes` items.
- Subscribe the hub to `ChangeBus` with one subscription that fans out to clients. On `onGap`, have every live client replay from DB starting at its last-sent id (the same mechanism as resume).

### 3. Response headers

`Content-Type: text/event-stream`, `Cache-Control: no-cache`, and `X-Accel-Buffering: no` (for reverse proxies).

## Tests to write (do not run)

- `SseChannelTest` (unit, fake sender): buffering during replay; dedupe on the replay/live boundary; overflow sends `reset` then completes; heartbeat scheduling; max-client limit.
- `ChangeStreamIT` (`@IntegrationTest`, real HTTP via `TestRestTemplate`/`WebClient`, or Java `HttpClient` reading the stream with a line parser helper in test support):
  - live events arrive ≤ 2 s after an ingest
  - filters by source, kind and bbox; a tombstone is delivered
  - resume with `Last-Event-ID` replays exactly the missed events, without duplicates
  - `payload=true` includes payloads
  - the replay cap triggers `reset`
  - 503 beyond max clients
- `SseTestClient` helper in test support: parses `id/event/data` frames, with timeouts.

## Acceptance criteria

- `streamChanges` matches `docs/OpenAPI.json`.
- `SseChannel` is generic and documented for reuse by the alert stream.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests are first run in prompt 061 (Full Test Run And Fix).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`). **Do not add Flyway, Liquibase or SQL migration files.**
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor, which are AGPL. They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `012: SSE Change Stream`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/012-CR-SSE-Change-Stream.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `012: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
