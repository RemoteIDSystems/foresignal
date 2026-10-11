# 015 — SSE Change Stream

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/015-CR-SSE-Change-Stream.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/014-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §10 (buses and the SSE table, **implement exactly**), §12 (client contract, `watermarkSeq`), §13 (`RetentionState`), *Per-prompt changes* → the *SSE Change Stream* entry, *Spec changes* (`Change`, `LiveEvent`, `TouchEvent`, `ResyncEvent`)
- `docs/Gap-Analysis.md` §6 row "Re-filterable multiplexed SSE", §8.A item 5 ("Extend 012" there; 012 is now this prompt)
- `docs/Weather-Imagery-Design.md` §6.8 (raster-frame events), §10.1 (Records/SSE row: `externalId` filter, payload always inline)
- `docs/Architecture.md` §8.2; `docs/Product-Charter.md` FR-21, NFR-3
- `docs/OpenAPI.json` operation `streamChanges`, schema `Change`
- Completion Reports of 009 (`ChangeBatch`/`ChangeSummary`, `LiveBus`, `onGap`, `onLiveDropped`) and 014 (`BboxParser` with antimeridian split, `NearParser`, `RetentionState`, `inlinePayloadKinds`)

## Objective

Give clients a live, filtered, resumable stream with two lanes: lossless `change` events keyed by seq, and conflated `live`/`touch` position updates that are not persisted. Add a multiplexed `/v1/events/stream` whose filter can be swapped without reconnecting, so a globe can pan without a reconnect storm and one connection carries changes, alerts and notifications. Build it generically: 017 registers alerts and 018 registers notifications on the same machinery.

## Tasks

### 1. Generic SSE machinery (`ai.elaro.stream.sse`)

- `SseClient`: wraps a Spring `SseEmitter` (timeout 0) and a dedicated **virtual** sender thread. It owns two lanes:
  - **FIFO lane** for id-bearing events (`change`, later `alert`, `notification`): bounded queue of `clientBuffer` (1,000). Overflow sends `reset {reason:"buffer_overflow", resumeAfter, watermarkSeq}` and completes the emitter.
  - **Conflation lane** for `live` and `touch`: a map keyed by `(sourceId, externalId)`, where a newer entry replaces an older one (a `live` replaces a `touch`; a `touch` updates only `observedAt`/`validUntil`/`bbox` of a pending `live`). The sender drains it every `liveIntervalMs` (1,000). When it would exceed `liveMaxEntities` (50,000), clear it and send `resync {reason:"live_cap", watermarkSeq}`.
  - Events without `id:`: `live`, `touch`, `resync`, `heartbeat`, `reset`, `hello`, `filter-applied`. Never write an `id:` line for them, so `Last-Event-ID` always names a FIFO cursor.
- **Swappable filter:** each client holds its `StreamFilter` in an `AtomicReference`. Swapping drops conflation entries that no longer match.
- **Replay then live, no gap, no duplicates** (FIFO lane), on connect with resume point `after`:
  1. Mark the client buffering; live FIFO events queue but are not sent. **Live-lane updates go into the conflation map during replay** and drain only after step 4.
  2. Replay from the database in pages (`seq > after`, filtered) up to `replayCap` (10,000). At the cap send `reset {reason:"replay_cap", resumeAfter}` and complete.
  3. Flush buffered FIFO events, skipping ids ≤ the last replayed id.
  4. Switch to live.
- **Resume floor:** before replay, if `after < max(RetentionState.lowestRetainedSeq(filtered sources), max purgeSeq(filtered sources))`, send `reset {reason:"resync_required", watermarkSeq}` immediately and complete. The client re-snapshots per the 014 contract.
- **Heartbeat** every `heartbeatSeconds` (15) as a named `heartbeat` event with `{}` (not a comment).
- **Auth resolution:** resolve the caller once at connect through a `StreamPrincipalResolver` (token id, scope, or anonymous with remote address). Read only the `Authorization` header now; never accept tokens in query strings. Prompt 023 adds reconnect-aware stream tickets by plugging into this resolver, so per-token limits and stream ownership must key on the resolver's principal id, not on the raw token.
- **Limits** (`StreamLimits` under app setting `stream.limits`): `maxClients` 100 (beyond → 503 `STREAM_LIMIT`), `clientBuffer` 1,000, `replayCap` 10,000, `heartbeatSeconds` 15, `liveIntervalMs` 1,000, `liveMaxEntities` 50,000, `maxStreamsPerToken` 8 and `maxStreamsAnonymousPerAddress` 4 (beyond → 429 `STREAM_LIMIT` with `Retry-After: 5`). Track counts for `/v1/system.streams`.
- Response headers: `Content-Type: text/event-stream`, `Cache-Control: no-cache`, `X-Accel-Buffering: no`. Confirm `text/event-stream` is excluded from `server.compression`.

### 2. Change stream (`GET /v1/changes/stream`, `streamChanges`)

- Resume point: `Last-Event-ID` (preferred), else `after`, else live from now (`after = ChangeBus.lastPublishedSeq()`).
- Parameters: `source`, `kind`, `externalId` (Weather §6.8), `bbox` (antimeridian split via 014's parser), `near` + `radiusKm`, `payload` (false), `live` (true), `touches` (false).
- **Three-box filter:** a change matches the spatial filter when `bbox` OR `prevPersistedBbox` OR `prevLiveBbox` intersects it (or is within the radius). When only a prev box matches, set `leftFilter: true` so the client removes the feature. A `live` event matches on `bbox` OR `prevBbox`, with the same `leftFilter` rule. Records without any box are excluded when a spatial filter is set. Tombstones carry the previous boxes and are delivered.
- `change`: `id: <seq>`, `event: change`, `data: <Change JSON>` using the 014 DTO (`prevSeq, historyTier, persistReason, observedAt, eventTime, validUntil, qaFlags, prevPersistedBbox, prevLiveBbox, leftFilter`). With `payload=true`, load payloads on the client's sender thread in small batches with `WHERE seq = ANY(?)` (runtime pruning, one leaf per tier). **Kinds in `inlinePayloadKinds` (default `raster-frame`) always carry the payload.**
- `live` (`LiveEvent`: sourceId, externalId, kind, observedAt, validUntil, version, geometry, bbox, prevBbox, properties (merged payload), quality, leftFilter) and `touch` (`TouchEvent`: sourceId, externalId, observedAt, validUntil, bbox) come from one hub subscription to `LiveBus`. `touch` is sent only with `touches=true`; `live` only with `live=true`.
- **Bus wiring:** one `ChangeBus` subscription and one `LiveBus` subscription fan out to clients. Change is published before live by 009; preserve that order per client.
  - `onGap` (ChangeBus subscriber overflow): every client replays its FIFO lane from its last-sent seq (same path as resume); clients with `live=true` also get `resync {reason:"gap", watermarkSeq}`, because live updates cannot be replayed.
  - `onLiveDropped` (LiveBus overflow): send `resync {reason:"live_dropped", watermarkSeq}` to every client with `live=true`.
- Client contract (put it in the operation description): on `resync`, re-fetch current state for the view with `/v1/records?order=id` (or tiles) and keep the stream open; on `reset`, reconnect with `resumeAfter` or re-snapshot when the reason is `resync_required`; apply an event only if its `observedAt` ≥ the stored one.

### 3. Multiplexed stream (`GET /v1/events/stream`, `streamEvents`)

- `topics` (comma list; default `changes`). Topics come from an `EventTopic` SPI: `name()`, `requiredScope()`, `filterType()`, `replay(cursor, filter, sink)`, `subscribe(hub)`. Register `changes` here (it wraps task 2). **Prompt 017 registers `alerts`, prompt 018 registers `notifications` and `system`.** An unknown or not-yet-registered topic → 400 `UNKNOWN_TOPIC`; a topic whose scope the caller lacks → 403.
- First event `hello {streamId, topics, watermarkSeq, filterVersion: 1}`. `streamId` is 128 random bits (base64url), bound to the principal id from `StreamPrincipalResolver`. For anonymous callers, `hello` also carries a `streamKey` that must accompany a PATCH as `Elaro-Stream-Key`.
- **Composite cursor:** every id-bearing event on this endpoint carries `id: <topic cursors>` in a fixed form, e.g. `c=1234;a=56;n=7`, holding the latest delivered cursor of every subscribed topic. `Last-Event-ID` is parsed back into per-topic resume points; a malformed id → 400 `INVALID_CURSOR`.
- Filter parameters on connect use per-topic prefixes: `changes.source`, `changes.kind`, `changes.externalId`, `changes.bbox`, `changes.near`, `changes.radiusKm`, `changes.live`, `changes.touches`, `changes.payload`. Other topics define theirs (017: `alerts.minSeverity`, `alerts.ruleId`, `alerts.source`, `alerts.kind`).
- **`PATCH /v1/events/stream/{streamId}/filter`** (`updateEventStreamFilter`): body `EventStreamFilter {topics?, changes?: {...}, alerts?: {...}, notifications?: {...}}`; omitted topic blocks are unchanged. Validate fully before applying. Only the owning principal may patch (else 404, not 403, so ids are not probeable). On success the hub swaps the filter atomically on the sender thread, emits `filter-applied {atSeq, filterVersion}` where `atSeq` is the last change seq delivered under the old filter, and returns 200 with the same body. Events after that frame use the new filter. **No server-side spatial delta replay:** the client loads the newly exposed area with `/v1/records?bbox=…` or tiles.
- Adding a topic via PATCH starts it live from now (its cursor = its current head) and does not replay.

### 4. Spec, Postman, docs

- `docs/OpenAPI.json`: `streamChanges` + `externalId`, `near`, `radiusKm`, `live`, `touches`; document event types `change`, `live`, `touch`, `resync`, `heartbeat`, `reset` with schemas. New `streamEvents` (GET `/v1/events/stream`, `"x-elaro-scope": "read"`) and `updateEventStreamFilter` (PATCH, `"x-elaro-scope": "read"`, ownership enforced in code). Schemas: `Change` (+ fields above), new `LiveEvent`, `TouchEvent`, `ResyncEvent` (reason `gap|live_cap|live_dropped`, `watermarkSeq`), `ResetEvent` (reason `buffer_overflow|replay_cap|resync_required`, `resumeAfter`, `watermarkSeq`), `HelloEvent`, `FilterAppliedEvent`, `EventStreamFilter`. Error codes `STREAM_LIMIT`, `UNKNOWN_TOPIC`.
- `AccessPolicy`: `GET /v1/events/stream` READ and `PATCH /v1/events/stream/*/filter` READ; extend `AccessPolicyTest`.
- Regenerate Postman (`python3 scripts/generate-postman.py docs/OpenAPI.json postman`); add an example body for `updateEventStreamFilter` (new bbox and `live: true`) to `BODIES`.
- `docs/Agent-Guide.md`: "Streaming" section: the two lanes, the client contract, the debounce-then-PATCH pattern for map pans (300–500 ms), and why `live`/`touch` have no ids.

## Tests to write (do not run)

- `SseClientTest` (unit, fake sender, `MutableClock`): conflation under a burst (1,000 live updates for 10 entities → ≤ 10 per drain); `touch` merges into a pending `live`; live updates buffered in the map during replay and sent after; FIFO dedupe on the replay/live boundary; overflow → `reset` then complete; `live_cap` → `resync`; heartbeat scheduling; **no `id:` line on live, touch, resync, heartbeat, reset, hello, filter-applied**.
- `StreamFilterTest`: three-box matching and `leftFilter`; antimeridian bbox; near radius; filter swap drops non-matching conflation entries.
- `ChangeStreamIT` (`@IntegrationTest`, Java `HttpClient` plus an `SseTestClient` helper that parses `id/event/data` frames with timeouts; data via `TestRecords`):
  - change events arrive ≤ 2 s after ingest; filters by source, kind, externalId, bbox; a tombstone is delivered
  - **ghost case:** with `live=false`, an entity moving out of the box is delivered once with `leftFilter: true` via its persisted box
  - resume with `Last-Event-ID` replays exactly the missed changes
  - `payload=true` includes payloads; `raster-frame` carries its payload without it
  - replay cap → `reset`; **resume below the floor** (stub `RetentionState`) → `reset {resync_required}`
  - `resync {gap}` after a forced `onGap`; `resync {live_dropped}` after a forced LiveBus drop
  - 503 beyond `maxClients`; 429 beyond `maxStreamsPerToken`
- `EventStreamIT`: `hello` then changes; composite `Last-Event-ID` resume; PATCH swaps the bbox and yields `filter-applied {atSeq}` before the first event under the new filter; PATCH from another principal → 404; unknown topic → 400.

## Acceptance criteria

- `streamChanges`, `streamEvents` and `updateEventStreamFilter` match `docs/OpenAPI.json`.
- Change events carry `id: <seq>`; live-lane and control events never carry an id.
- The `EventTopic` SPI is documented so 017 and 018 can register topics with no hub changes.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `015: SSE Change Stream`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/015-CR-SSE-Change-Stream.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `015: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
