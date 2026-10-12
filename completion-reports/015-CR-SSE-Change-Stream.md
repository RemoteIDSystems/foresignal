# 015-CR-SSE-Change-Stream

| | |
|---|---|
| Prompt | 015-SSE-Change-Stream.md |
| Status | Complete |
| Implementation commit | 80ce38af10fb4986d48ccd53067b485cdf8088c6 |
| Date | 2026-10-11 |

## Summary

This prompt adds live streaming of changes over Server-Sent Events (SSE). The three operations
assigned to 015 in `docs/OpenAPI.json` are implemented and marked
`x-foresignal-status: implemented`:

- `streamChanges`: `GET /v1/changes/stream`
- `streamEvents`: `GET /v1/events/stream`
- `updateEventStreamFilter`: `PATCH /v1/events/stream/{streamId}/filter`

**Generic machinery (`ai.foresignal.stream.sse`).**

- `SseClient` is one open stream. It wraps an `SseSender`; `EmitterSseSender` adapts a Spring
  `SseEmitter(0L)`. Each client has a dedicated virtual sender thread. All writes happen in
  `pump()`, which unit tests drive directly with a fake sender and a `MutableClock`. The locking
  uses `ReentrantLock`, so a waiting virtual thread never pins its carrier.
- **The FIFO lane** holds id-bearing events, bounded by `clientBuffer`. On overflow the client
  gets `reset {buffer_overflow, resumeAfter, watermarkSeq}` and the stream completes.
- **The conflation lane** is a map keyed by `(sourceId, externalId)`:
  - a `live` replaces whatever is pending;
  - a `touch` updates only `observedAt`, `validUntil` and `bbox` of a pending `live`, or replaces a
    pending `touch`;
  - the map is drained every `liveIntervalMs`;
  - past `liveMaxEntities` the map is cleared and the client gets `resync {live_cap}`.
- **Event ids.** Only FIFO events carry an `id:`. `live`, `touch`, `resync`, `heartbeat`, `reset`,
  `hello` and `filter-applied` are built with `SseFrame.control`, which has no id.
- **Replay, then live.** The client registers with the hub before it replays. While it replays it
  is buffering:
  - live FIFO events queue up;
  - live-lane updates go into the map, and the map is not drained.

  The replay runs up to `replayCap`; past the cap the client gets `reset {replay_cap, resumeAfter}`
  and the stream closes. The replay reports the cursor it scanned through. The buffer is then
  flushed, dropping every event at or below the cursor already delivered. This makes the
  hand-over free of gaps and duplicates. A bus gap goes through the same path.
- **Filter swap.** The `StreamFilter` sits in an `AtomicReference`. Events are filtered when they
  are queued, under the client lock. A swap sets the new filter and queues a marker under the same
  lock. When the sender thread reaches the marker, it drops the conflation entries the new filter
  rejects and emits `filter-applied {atSeq, filterVersion}`.
- **Heartbeat.** A named `heartbeat` event with data `{}` every `heartbeatSeconds`.
- `StreamPrincipalResolver` / `HeaderStreamPrincipalResolver` resolve the caller once, at
  connect:
  - only the `Authorization` header is read;
  - a token caller's id is `token:<first 16 hex digits of the token's SHA-256>`;
  - an anonymous caller's id is `anon:<remote address>`.

  Per-principal limits and stream ownership key on this id.
- `StreamLimits` is stored as the app setting `stream.limits`. The defaults are 100 / 1,000 /
  10,000 / 15 s / 1,000 ms / 50,000 / 8 / 4. A missing or non-positive value takes its default.
- `SseHub` holds the topic registry, the open clients and the limits:
  - over `maxClients` a connect is `503 STREAM_LIMIT`;
  - over the per-token or per-address limit it is `429 STREAM_LIMIT` with `Retry-After: 5`;
  - it fans events out to clients;
  - it decides stream ownership;
  - `stats()` returns the counts for `/v1/system.streams`.

  Topics subscribe to their buses on `ApplicationReadyEvent`, at the new `StartupOrder.SSE_HUB`
  (15).
- `EventTopic<F>` is the SPI, documented in its class Javadoc and in `package-info`. A topic
  provides `name`, `cursorKey`, `cursorOrder`, `requiredScope`, `filterType`, `parseQuery`,
  `parseJson`, `head`, `replay(ReplayRequest, F, ReplaySink)`, `subscribe(SseHub)` and
  `loadPayloads`. Topics are discovered as Spring beans, so 017 and 018 need no change to the hub.
- `CompositeCursor` handles both id forms:
  - the composite form `c=1234;a=56` (format and parse), where a malformed id is
    `400 INVALID_CURSOR`;
  - the single-seq form of `/v1/changes/stream`.

**The `changes` topic (`ChangesTopic`)** has one `ChangeBus` subscription and one `LiveBus`
subscription, shared by all clients.

- **Live changes.** For each batch, the versions that at least one client accepts are loaded once
  (`RecordQueryService.changesBySeq`: `WHERE seq = ANY(?)`, with geometry and no payload). Each
  client renders its own copy with its own `leftFilter`.
- **Payloads.** `payload=true`, and kinds in `inlinePayloadKinds`, mark the event for a payload.
  The sender thread loads payloads in batches of up to 64 with `RecordQueryService.payloadsBySeq`
  (`WHERE seq = ANY(?)`).
- **Replay** pages through `RecordQueryService.changes`. Before a resume, it checks the floor
  `after < max(lowestRetainedSeq, purgeHorizon)` over the filtered sources; below the floor the
  client gets `reset {resync_required}`.
- **Gaps and drops.**
  - `onGap`: every client replays from its last delivered cursor. Clients with `live=true` also get
    `resync {gap}`.
  - `onLiveDropped`: clients with `live=true` get `resync {live_dropped}`.

**The three-box filter (`ChangeFilter`).** A change matches when its `bbox`, its
`prevPersistedBbox` or its `prevLiveBbox` intersects the bbox (antimeridian boxes are split by
014's `BboxParser`). With `near`, the haversine distance from the center to the nearest point of
the box must be within `radiusKm`.

- If only a previous box matches, the result is `LEFT`, which sets `leftFilter: true`.
- A `live` event uses `bbox` / `prevBbox` with the same rule.
- An event without any box never matches a spatial filter.
- Tombstones carry their previous boxes, so they are delivered as `LEFT`.

**The endpoints (`StreamController`)** validate everything before a stream opens. Responses carry
`Cache-Control: no-cache` and `X-Accel-Buffering: no`; `SseEmitter` sets `text/event-stream`.
`text/event-stream` is still absent from `server.compression.mime-types` (checked in
`application.yml`).

- `/v1/changes/stream` resumes from `Last-Event-ID`, else `after`, else live from now.
- `/v1/events/stream`:
  - `topics` defaults to `changes`;
  - an unknown topic is `400 UNKNOWN_TOPIC`;
  - a topic whose scope the caller lacks is `403`;
  - filters are prefixed per topic (`changes.*`);
  - a composite `Last-Event-ID` (or `?lastEventId=`) resumes; the header wins when both are present;
  - the first event is `hello {streamId (22 base64url characters), topics, watermarkSeq,
    filterVersion: 1, streamKey (anonymous callers only)}`.
- `PATCH …/filter`:
  - the whole body is validated before anything is applied;
  - a topic block that is present replaces that topic's filter; omitted blocks are unchanged;
  - a topic added by `topics` starts at its head, without replay;
  - a caller who does not own the stream gets 404;
  - the response is 200 with the same body.

**Other changes.**

- `ErrorCodes.STREAM_LIMIT` and `ErrorCodes.UNKNOWN_TOPIC`.
- `ForesignalException.withHeader`. `GlobalExceptionHandler` copies the headers onto the problem
  response, which is how `Retry-After` is sent.
- `AccessPolicy`: `GET /v1/events/stream` and `PATCH /v1/events/stream/*/filter` both need `read`.
- `RecordMapper.streamChange` (two overloads), `box` and `bbox`.
- `ChangesQuery` gains `near` and `prevBoxes`. The old 11-argument constructor is kept.
- `RecordRow` gains `prevBbox` (labels `pbx1`…`pby2`).
- `RecordQueryService` gains `changesBySeq` and `payloadsBySeq`. Change queries join
  `record_version p ON p.seq = v.prev_seq` when `prevBoxes` is set.
- `docs/Agent-Guide.md` has a new "Streaming" section:
  - the two lanes, and why `live` and `touch` have no ids;
  - the client contract;
  - debounce-then-PATCH for map pans (300–500 ms);
  - composite ids and `lastEventId`;
  - limits.
- Postman: a `BODIES` example for `updateEventStreamFilter` (a new bbox and `live: true`), and the
  collection regenerated.

## Files created / modified

**Created (main)**
- `api/StreamController.java`
- `stream/sse/`:
  - `package-info`, `SseFrame`, `SseSender`, `EmitterSseSender`
  - `StreamLimits`, `StreamPrincipal`, `StreamPrincipalResolver`, `HeaderStreamPrincipalResolver`
  - `FilterMatch`, `ChangeFilter`, `StreamFilter`, `FifoEvent`, `TopicParams`
  - `ReplayRequest`, `ReplaySink`, `ReplayOutcome`, `CompositeCursor`, `LiveEvents`
  - `EventTopic`, `SseClient`, `SseHub`, `ChangesTopic`

**Modified (main)**
- `api/ChangesController.java` (Javadoc only)
- `api/error/ErrorCodes.java`, `ForesignalException.java`, `GlobalExceptionHandler.java`
- `api/mapper/RecordMapper.java`
- `config/StartupOrder.java`
- `security/AccessPolicy.java`
- `store/query/ChangesQuery.java`, `RecordQueryService.java`, `RecordRow.java`, `RecordRowMapper.java`

**Tests**
- New unit tests in `stream/sse/`: `SseClientTest`, `StreamFilterTest`, `CompositeCursorTest`,
  `SseFrameTest`, `ChangesTopicTest`, with support classes `FakeSender` and `SseFixtures`.
- New integration tests: `api/ChangeStreamIT`, `api/EventStreamIT`.
- New test support:
  - `test/WebIntegrationTest` (`@IntegrationTest` with `RANDOM_PORT`)
  - `testsupport/stream/SseTestClient` (`HttpClient`; parses `id`/`event`/`data` frames, with
    timeouts)
  - `testsupport/stream/StreamTestSources`
- Modified:
  - `security/AccessPolicyTest`
  - `api/mapper/RecordMapperTest` (the new `RecordRow` argument)
  - `testsupport/records/Queries` (`near`, `prevBoxes`)
  - `testsupport/records/RecordsTestSources` (`StubRetentionState.setLowestRetainedSeq`)

**Docs and tooling**
- `docs/OpenAPI.json` (status flips, plus the reconciliations in Deviation 11)
- `docs/Agent-Guide.md`
- `scripts/generate-postman.py` (`BODIES`)
- `postman/Foresignal.postman_collection.json` (regenerated; one body changed)

No new dependency. No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` changes. Nothing was
ported from God's Eye View.

## Deviations from the prompt

1. **Replay through an extended `ChangesQuery`.** The replay uses `RecordQueryService.changes`, as
   014's notes ask. A plain `seq > after` query would miss an entity that left the area, so the SSE
   replay sets the new `prevBoxes`:
   - it does `LEFT JOIN record_version p ON p.seq = v.prev_seq` (a seq-only join);
   - it ORs the spatial clause over `p.geom`;
   - it selects the previous box, so replayed leavers carry `leftFilter: true` and
     `prevPersistedBbox`.

   `prevLiveBbox` is not stored, so replayed changes have it as null. `near` was added to
   `ChangesQuery` for the same path.
2. **Live changes load their geometry.** `ChangeSummary` has boxes but no geometry, and the event
   uses 014's `Change` shape, which includes geometry. The hub therefore loads each batch's matched
   versions once with `seq = ANY(?)`, without payload. If a version is already gone, the event is
   built from the summary with no geometry.
3. **Composite `StreamFilter`.** `StreamFilter` holds the topics, one filter per topic and a
   version. The `changes` filter, with the three-box rule, is `ChangeFilter`. `StreamFilterTest`
   covers both.
4. **The `EventTopic` SPI has more than the five methods listed:**
   - `cursorKey` and `cursorOrder`, for the composite id;
   - `parseQuery` and `parseJson`, for connect and PATCH validation;
   - `head`, for "live from now" and for topics added by PATCH;
   - `loadPayloads`, for deferred payloads.

   `replay` takes a `ReplayRequest(after, checkFloor)` rather than a bare cursor.
5. **"Live from now" still replays.** The head is read before the client registers, and the
   (usually empty) range after it is replayed without the floor check. Otherwise a batch committed
   between reading the watermark and registering would be lost.
6. **Touches and kinds.** A `Touch` carries no kind. A kind filter matches a standalone touch when
   any kind the touch's source declares is selected. A touch merges into a pending `live` whatever
   `touches` is set to; `touches=false` suppresses only standalone `touch` events.
7. **Stream ownership.**
   - An anonymous stream is owned by whoever presents its `streamKey`, compared in constant time.
     The remote address is not checked, because it can change behind NAT or a proxy.
   - A token stream is owned by its principal id.
8. **PATCH semantics in detail.**
   - Unknown fields in the `changes` block are rejected with 400.
   - A block for a topic that is not registered is `400 UNKNOWN_TOPIC`.
   - A block for a registered topic that is not subscribed is validated and then ignored.
   - The filter is re-parsed with the stream owner's principal, which decides which private
     sources are excluded.
9. **A header on `ForesignalException`.** `Retry-After` needed a way onto problem responses.
   `withHeader` is generic, and the handler copies it.
10. **No `/v1/system` endpoint yet.** `SseHub.stats()` returns `{clients, maxClients,
    changeSubscribers, eventSubscribers}`, which matches `SystemInfo.streams`. No `/v1/system`
    controller exists yet, so nothing serves it. The prompt that builds `getSystem` should read it.
    `alertSubscribers` belongs to 017.
11. **Spec reconciliations**, all recorded in `info.x-foresignal-changelog`:
    - `streamChanges`' 429 now describes `STREAM_LIMIT` (it said `RATE_LIMITED`);
    - `streamChanges` and `streamEvents` document `404` for an unknown source in the filter, which
      is the behavior of 014's read endpoints;
    - `updateEventStreamFilter`'s 404 description includes that case.

    No parameter or schema changed.
12. **A second web test context.** `@WebIntegrationTest` mirrors `@IntegrationTest` with
    `webEnvironment = RANDOM_PORT`, because the prompt asks for `HttpClient` over a socket. Both SSE
    ITs import `StreamTestSources` and share that one additional context.
13. **Stream id.** 17 random bytes, base64url, truncated to the spec's 22 characters. That keeps
    132 random bits, which is at least the 129 the spec asks for.
14. **`pending-operations.txt` does not exist yet** (prompt 020 creates it), so there were no lines
    to delete.

## Issues found / open questions

1. **CORS blocks the filter PATCH from browsers.** `CorsFilter` allows
   `GET, HEAD, POST, PUT, DELETE, OPTIONS`. It allows neither `PATCH` nor the
   `Foresignal-Stream-Key` header. It also judges any non-GET request against the *write* origins.
   A browser map on a read-only origin therefore cannot PATCH cross-origin. I did not change it:
   the CORS header set belongs to prompt 050. **Owner:** should the stream-filter PATCH count as a
   read for CORS?
2. **The resume floor is implemented exactly as written,
   `after < max(lowestRetainedSeq, purgeSeq)`.** Since `lowestRetainedSeq` is the lowest seq
   *retained*, a client at exactly `lowestRetainedSeq − 1` is still complete but gets a reset. That
   is harmless (one extra re-snapshot) and stays dormant until 019 publishes real floors. **Owner:**
   keep it, or use `after + 1 < lowestRetainedSeq`?
3. **`LiveEvent.properties`.** The prompt says "merged payload"; the spec and `LiveUpdate` say the
   kinematic overlay, where a null value means the key was removed. I followed the spec and `LiveUpdate`.
4. **Problem responses to `Accept: text/event-stream` are unverified.** The limit and validation
   errors are thrown before the emitter exists, and Spring is expected to render `ProblemDetail` as
   `application/problem+json`. That has not been checked by running anything; the ITs assert status
   codes only, and checkpoint 022 runs them.
5. The owner decisions carried from 012–014 are still open.

No instructions directed at an agent were found in data, fixtures or tool output.

## Tests written (not run)

As a feature prompt requires, no tests were run. The integration tests seed only through
`TestRecords`.

- **`SseClientTest`** (fake sender, `MutableClock`):
  - conflation under a burst: 1,000 updates for 10 entities give 10 per drain, newest wins;
  - a `touch` merges into a pending `live`;
  - standalone touches only with `touches=true`;
  - `live=false`;
  - live updates buffered in the map during a replay and sent after the replayed changes;
  - FIFO dedupe on the replay/live boundary;
  - overflow sends `reset {buffer_overflow}`, then the stream completes and unregisters;
  - `live_cap` sends `resync`;
  - heartbeat scheduling;
  - the replay cap sends `reset {replay_cap, resumeAfter}`;
  - `resync_required`;
  - a gap replays from the last delivered seq;
  - deferred payload batches;
  - composite ids;
  - no `id:` line on live, touch, resync, heartbeat, reset, hello or filter-applied, and `id:<seq>`
    on change;
  - a failed write closes the client.
- **`StreamFilterTest`**:
  - three-box matching and `leftFilter`, including tombstones;
  - no spatial filter;
  - live matching;
  - an antimeridian bbox;
  - the near radius (nearest box point, across the antimeridian);
  - entity and private-source filters;
  - touch kinds;
  - the `StreamFilter` shape;
  - a filter swap drops conflation entries that no longer match and emits `filter-applied {atSeq}`;
  - events queued after a swap use the new filter.
- **`CompositeCursorTest`**: the round trip, nine malformed forms giving `INVALID_CURSOR`, single
  seqs.
- **`SseFrameTest`**: the wire form, limit defaults and partial limits, principals.
- **`ChangesTopicTest`**: identity, query parsing with prefixed errors, strict PATCH parsing,
  render, the floor.
- **`ChangeStreamIT`**:
  - the response headers;
  - a change within 2 s, with `id` equal to seq and the `Change` fields;
  - filters by source, kind, externalId and bbox;
  - a tombstone;
  - the ghost case (`live=false`, leaving the box gives exactly one event with `leftFilter`);
  - `Last-Event-ID` and `after` resume, without duplicates;
  - `payload=true`, with and without, `raster-frame` inline, and the replay path;
  - the replay cap gives `reset`;
  - a resume below the floor (stub `purgeSeq` and stub `lowestRetainedSeq`) gives
    `reset {resync_required}`;
  - `resync {gap}` after a forced `onGap`, sent only to `live=true` clients;
  - `resync {live_dropped}` after a forced LiveBus drop;
  - a `live` event without an id;
  - 503 beyond `maxClients`;
  - 429 with `Retry-After: 5` beyond the per-token and per-address limits;
  - 400s and a 404.
- **`EventStreamIT`**:
  - `hello`, then changes with ids `c=<seq>`;
  - a token stream's `hello` has no `streamKey`;
  - composite `Last-Event-ID`, `?lastEventId=`, and the header winning;
  - a PATCH swaps the bbox and `filter-applied {atSeq, filterVersion: 2}` arrives before the first
    event under the new filter;
  - a PATCH from another principal, with a wrong key or for an unknown stream is 404;
  - an unknown topic is 400 on connect and on PATCH, as is an invalid PATCH, and nothing is applied;
  - a malformed cursor is 400.
- **`AccessPolicyTest`**: both new rules, and their fail-closed neighbors.

## Compile result

```
$ ./gradlew clean compileJava compileTestJava
BUILD SUCCESSFUL in 3s
5 actionable tasks: 5 executed
```

## Notes for the next prompt

1. **016 (tiles):** nothing here changes tiles.
2. **017 (alerts):**
   - Implement `EventTopic<AlertFilter>` as a `@Component`, with name `alerts`, cursor key `a` and
     a `cursorOrder` between 0 and 100 (for example 10).
   - Call `SseHub.publish("alerts", filter -> event-or-null)` from its bus, and
     `SseHub.gap("alerts")` on a gap.
   - Return `ReplayOutcome`s from `replay`.
   - The hub needs no change. Add `alertSubscribers` to `SseHub.stats()`.
3. **018 (notifications, system):** the same pattern as 017, with keys `n` and `s`.
4. **019 (retention):** publish `lowestRetainedSeq` and `purgeSeq`. `ChangesTopic.floor` reads
   both. See Issue 2.
5. **020 (pending operations):** `streamChanges`, `streamEvents` and `updateEventStreamFilter` are
   implemented; do not list them.
6. **022 (checkpoint):**
   - The SSE ITs need a real port (`@WebIntegrationTest`).
   - The SSE memory budget applies to `clientBuffer` × `maxClients` plus the conflation maps.
   - Check Issue 4 when the tests run.
7. **023 (tokens, stream tickets):**
   - Provide a `@Primary StreamPrincipalResolver` that maps a `ticket` to its issuing token's
     principal id.
   - Replace `HeaderStreamPrincipalResolver`'s `token:<hash>` id with the token's id.
   - `TokenFilter` must accept tickets on these two paths only.
8. **039 (EgressPolicy):** private-source exclusion for streams is
   `ChangesTopic.excludedSources`. Move it into `EgressPolicy`.
9. **050 (CORS):** see Issue 1. Add `PATCH` and `Foresignal-Stream-Key`, and decide which origin
   list the filter PATCH is judged against.
10. **The prompt that builds `getSystem`:** serve `SseHub.stats()` as `streams`.
