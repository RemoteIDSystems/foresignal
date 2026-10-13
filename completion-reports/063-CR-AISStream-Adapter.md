# 063-CR-AISStream-Adapter

| | |
|---|---|
| Prompt | 063-AISStream-Adapter.md |
| Status | Complete |
| Implementation commit | 6f9998783d28bd9e7d38fc4088a6b8530706bba8 |
| Date | 2026-10-13 |

## Summary

Foresignal has its first streaming adapter: `aisstream`, global AIS over aisstream.io's WebSocket, merged per MMSI into
complete vessel snapshots. It is TRACK + SAMPLED, LOCAL_ONLY, and cannot be enabled without an `apiKey`. No key was
available, so nothing was verified against the live service (see Issues); the code follows aisstream's published
documentation, read on 2026-10-13. Fixtures are synthetic and marked for recapture.

**1. Descriptor** (`ai.foresignal.source.adapters.aisstream.AisStreamAdapter implements StreamingAdapter`).
- `aisstream`, kind `vessel`, STREAMING. `apiKey` is a required credential, so enabling without it is
  409 `CREDENTIALS_REQUIRED` (004's existing rule).
- Params:
  - `bbox`: string-list of `minLon,minLat,maxLon,maxLat`, 1–10 entries, range-checked in `checkParams`; default
    `["-180,-90,180,90"]`;
  - `messageTypes`: string-list restricted (`ParamSpec.allowed`) to the five merged types; default all five;
  - `mmsi`: at most 50 nine-digit strings.
- Budget 2/min, 144/day. TRACK, UPSTREAM, OBSERVED, TTL 30 min.
- SAMPLED `{30 s, keyframe 1200 s, 250 m, thresholds, edge 15 s}`. `IngestPolicyValidator.keyframeCeiling` is 1768 s.
- Significant `name, callsign, imo, shipType, destination, navStatus, draught`. Motion `{sog, KNOTS, cog}`; thresholds
  `cog` 15° (angular) and `sog` 3 kn.
- Tiles, search and title as specified.
- Also declared:
  - entity keys `mmsi`, `imo`, `callsign` (Adapter Guide §13);
  - a `fieldSpecs` entry for every payload key;
  - kind display `{name}`, icon `ship`, rotated by `cog`.
- License "aisstream.io terms (unverified)", terms `https://aisstream.io`, egress **LOCAL_ONLY**. The description
  carries the keyless note and the one-key-per-instance rule.

**2. Connection** (`AisStreamSession`, the `WebSocket.Listener` and `StreamHandle` of one connection).
- **Open:** `ctx.http().openWebSocket(wss://stream.aisstream.io/v0/stream, session, {}, WebSocketOptions.DEFLATE)`.
- **Subscription:**
  - sent in `onOpen`;
  - boxes latitude first, `FiltersShipMMSI` left out when empty;
  - `AisSubscription.toString()` redacts the key, and only `toJson()` (the frame) contains it.
- **Frames:**
  - binary and text both handled; fragments accumulate until `last`;
  - a message over 256 KB is dropped and counted `oversize`;
  - `request(1)` is called only after the message was handed to the sink.
- **Byte metering:** `ctx.reportBytes` every 250 ms watchdog tick. On a `MeteredWebSocket` it reports the compressed
  wire bytes, otherwise the message bytes.
- **`update(params)`:**
  - resends the subscription on the same connection, at most once a second;
  - a change made within the second waits for the tick, and the newest one wins;
  - unchanged parameters send nothing;
  - returns false after the end or for unusable parameters, which makes the supervisor restart the stream.
- **Liveness** is only a recognized envelope (GEV rule): a `MessageType` from aisstream's complete 25-value enum, a
  body under it, and an MMSI. The confirmation is not liveness.
- **Errors:**
  - an `{"error"}` whose text matches GEV's `AUTH_TEXT` (extended with "missing api key") ends the stream with
    `CredentialsRejectedException`; any other error is an `AdapterException`;
  - two consecutive closes before any confirmation or envelope are a `CredentialsRejectedException`. The count lives
    in the adapter and resets on a confirmation, an envelope, or a different key (SHA-256 fingerprint).
- **Watchdog:**
  - `AisStreamHealth` (a `HealthRule`) reports `degraded` "connected but silent for N s" at 120 s;
  - the session aborts the socket at 300 s and ends with an `AdapterException`, which is one ladder step;
  - silence uses `ctx.clock()`.
- **Stop:** `close()` aborts the socket; the supervisor flushes the sink, as before.
- **010's ladder** (5/15/60/300/900 s, reset after 10 min, hourly credentials retry) was confirmed in
  `CollectorProperties` and `StreamingSupervisor.nextDelay`; nothing needed changing.

**3. Parser and merge.**
- **`AisStreamMessageParser.parse(String)`** (pure) returns a sealed `Result`:
  - `AisMessage`, `Ignored`, `Untimed`, `Confirmation`, `ErrorEnvelope`, `Unrecognized` (`unknown_type` /
    `no_mmsi`), `Malformed`;
  - `MetaData` keys are read case-insensitively;
  - `time_utc` is parsed to the nanosecond (ISO-8601 accepted too); untimed messages are dropped, never stamped;
  - MMSIs are zero-padded to nine digits.
- **Sentinels:**
  - lat 91 / lon 181 or out of range: no position;
  - SOG ≥ 102.3, COG ≥ 360, heading 511 or > 359, status 15: omitted;
  - IMO, type, draught and all-zero dimensions of 0: omitted;
  - ETA parts "not available": omitted;
  - `@` and spaces are trimmed at both ends.
- **`VesselStateStore`:**
  - per-MMSI state, access-ordered, bounded at 200,000 entries, evicting entries unseen for 6 h (injected clock);
  - every message returns one complete snapshot;
  - `observedAt` is the newest merged time, and late position or static data never rolls the state back;
  - geometry is the newest position report's position, null when that report had a sentinel (carry-forward applies);
  - `quality` is REPORTED, with `extra["ais.position_age_s"]`;
  - static data is held until the first usable position;
  - `eta` is the next occurrence at or after the static message's time (an impossible date is omitted);
  - the payload keys are exactly those of the spec's `vessel` kind.
- **Metrics:** `foresignal.aisstream.messages{type}`, `foresignal.aisstream.dropped{reason}` and gauge
  `foresignal.aisstream.vessels`.

**4. Framework additions.**
- **SPI:**
  - `WebSocketOptions(perMessageDeflate)` with `DEFAULT` and `DEFLATE`;
  - a default `SourceHttp.openWebSocket(uri, listener, headers, options)`;
  - `MeteredWebSocket` (`wireBytesReceived`, `perMessageDeflateNegotiated`);
  - a default `StreamHandle.update(ValidatedParams)` that returns false.
- **`collect/DeflateWebSocket`** wraps Java-WebSocket 1.6.0 (MIT) with `PerMessageDeflateExtension` behind the JDK
  `WebSocket` interface:
  - whole messages; demand-driven delivery (the read thread waits, so TCP pushes back);
  - one terminal event, and nothing after `abort()`;
  - a counting draft for wire bytes; `maxResponseBytes` as the frame cap.
- **`BoundSourceHttp.openWebSocket(…, options)`:** the same backoff check, budget unit, User-Agent (a caller's is
  ignored), usage metering and backoff reset as the JDK path. Like the JDK path, it records no `FetchTrace`.
- **`StreamingSupervisor`:**
  - `paramsChanged(id)` tries `handle.update(effectiveParams)` and restarts the stream when that fails;
  - `restart(id)`.
- **`CollectorScheduler`:**
  - calls `paramsChanged` on the timer thread when an enabled source's params change;
  - calls `restart` when credentials are stored.
- **`SourceHealth.details`** (spec field assigned to 063):
  - new `HealthDetailsContributor` beans, merged by `HealthService`;
  - `SourceHealthView`/`SourceHealthDto.details`, omitted when empty;
  - aisstream gives `compressionEnabled`, `perMessageDeflate` and `vessels`.
- **`TitleResolver`** falls back to `mmsi` after `hex`.
- **The SRC-1 guard** (`UpstreamClientIsTheOnlyHttpClientTest`) also forbids `org.java_websocket.client` outside
  `collect`.

**5. Docs.**
- **Adapter Guide:** new §16 "Streaming adapters" (§9's bullet points to it).
- **Source Catalog:** the `aisstream` row, with the verified limits.
- **Operator Guide:** "Vessels from aisstream.io" (getting a key, one key per instance, bbox sizing versus
  bandwidth, health).
- **Invariants:** SRC-1 and EGR-2 test lists.
- **`THIRD_PARTY_NOTICES.md`:** four GEV rows plus Java-WebSocket. The ported classes carry origin headers.
- **Version catalog:** `javaWebsocket = "1.6.0"`.
- **OpenAPI:** no operation changes. The `SourceHealth.details` and `Record.title` descriptions were amended, with a
  changelog entry. `lintOpenApi` passes; the Postman collection was regenerated and is unchanged.

## Files created / modified

**Created**
- `src/main/java/ai/foresignal/source/adapters/aisstream/`: `package-info`, `AisStreamAdapter`, `AisStreamSession`,
  `AisSubscription`, `AisStreamMessageParser`, `AisMessage`, `VesselStateStore`, `AisStreamMetrics`,
  `AisStreamHealth`.
- `src/main/java/ai/foresignal/source/spi/WebSocketOptions.java`, `MeteredWebSocket.java`.
- `src/main/java/ai/foresignal/collect/DeflateWebSocket.java`, `HealthDetailsContributor.java`.
- Tests:
  - `source/adapters/aisstream/AisStreamMessageParserTest`, `VesselStateStoreTest`, `AisStreamAdapterTest`,
    `AisStreamIngestIT` and the helper `AisTestContext`;
  - `collect/WebSocketOptionsIT`, `collect/HealthDetailsTest`;
  - `testsupport/websocket/LoopbackWebSocketServer`.
- Fixtures: `src/test/resources/fixtures/aisstream/` (`README.md`, `frames.jsonl`, `sentinels.jsonl`,
  `static-before-position.jsonl`, `classb-parts.jsonl`, `error-auth.json`, `untimed.json`).

**Modified**
- `source/spi/SourceHttp` (options overload), `source/spi/StreamHandle` (`update`).
- `collect/BoundSourceHttp`, `collect/StreamingSupervisor`, `collect/CollectorScheduler`, `collect/HealthService`,
  `collect/SourceHealthView`, `api/dto/SourceHealthDto`, `api/mapper/HealthMapper`, `ingest/TitleResolver`.
- Tests: `collect/StreamingSupervisorIT` (three update/restart tests), `collect/UpstreamClientIsTheOnlyHttpClientTest`,
  `ingest/TitleResolverTest`, `testsupport/adapters/FakeStreamingAdapter` (`acceptUpdates`, `Handle.update`).
- `build.gradle.kts`, `gradle/libs.versions.toml`.
- `docs/OpenAPI.json`, `docs/Adapter-Guide.md`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`,
  `docs/Invariants.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **The parser's result has seven variants, not three.** The prompt's `AisMessage | Error | Malformed` became
   `AisMessage | Ignored | Untimed | Confirmation | ErrorEnvelope | Unrecognized | Malformed`. The liveness rule,
   the drop reasons and the compression confirmation each need their own case.
2. **`ExtendedClassBPositionReport` also merges its `Name`, `Type` and `Dimension`.** The message carries them
   (`type-definition.yaml`); the prompt lists static data only from `ShipStaticData` and `StaticDataReport`.
3. **Ship type 0 is omitted** ("not available" in ITU-R M.1371), like IMO 0. The prompt's list does not name type 0.
4. **Static fields keep per-field times.** `ShipStaticData` replaces the voyage block (destination, ETA, draught,
   IMO), where an absent value means cleared. Name, call sign, type and dimensions are set by whichever message
   carries them. This keeps a part B from erasing a part A's name.
5. **A vessel whose position reports were all sentinels is held**, like static-only data. A sentinel report after a
   real fix emits null geometry, so the TRACK carry-forward applies.
6. **Credentials change → stream restart, and params change → `update()` or restart.** 010 never re-applied
   parameters to a running stream: its reconcile only compares membership. Both hooks were added in
   `CollectorScheduler`/`StreamingSupervisor`. The prompt asks only for the `update()` hook.
7. **`SourceHealth.details` was implemented here** (its spec field names 063), through a new
   `HealthDetailsContributor` bean type. Besides `compressionEnabled` it carries `perMessageDeflate` and `vessels`.
   The spec description was amended to list them.
8. **`Record.title` gains the `mmsi` fallback** in `TitleResolver` (the prompt's "fallback `mmsi`"). The order is
   `name`, then `callsign` (already a generic fallback), then `mmsi`. The spec description and changelog say so.
9. **No `FetchTrace` for a WebSocket.** The JDK path records none, and "exactly as in the JDK path" was followed.
   `WebSocketOptionsIT` asserts there are none.
10. **Java-WebSocket is Java 7 bytecode** (class version 51), not 8; recorded in the catalog comment. Its only runtime
    dependency is `slf4j-api`.
11. **`bbox` must be a JSON array** of strings: `ParamValidator` splits a single string at its commas. A box crossing
    the antimeridian is refused (min must not exceed max), and the message says to split it.
12. **Watchdog timing uses real time for the update rate limit and `ctx.clock()` for silence**, so `MutableClock` can
    drive the watchdog in tests while aisstream's one-update-per-second rule stays real.

## Issues found / open questions

1. **Unverified live (missing access, Conventions "No credentials during construction"):** no aisstream key was
   available, so the following were checked only against the documentation, not live:
   - `wss://stream.aisstream.io/v0/stream`;
   - the subscription and confirmation shapes, and `CompressionEnabled`;
   - binary framing; the casing of `MetaData` keys;
   - the invalid-key behavior (silent close);
   - Java-WebSocket's `permessage-deflate` negotiation with aisstream's server.

   This does not make the prompt Partial: no required piece needed the key, and the fixtures are marked
   `synthetic: true` with the capture procedure. **143** should run the capture with `FORESIGNAL_VERIFY_*`.
2. **The docs and the prompt disagree on the MMSI filter limit:** aisstream documents up to 200 MMSIs per
   subscription; the prompt caps `mmsi` at 50. The code follows the prompt. Raise it?
3. **A network error before the confirmation counts toward the two-close rule.** An `onError` after the handshake
   but before the confirmation is counted like a close. Two such errors in a row would read as a rejected key and wait
   an hour. This is rare (the confirmation follows the subscription at once), but possible on a flaky link.
4. **Back-pressure versus keepalive on the deflate client:** while the sink blocks, the library's read thread does not
   read. Java-WebSocket's connection-lost timer (pings every 60 s; it closes after about 90 s without a pong) could then
   close a connection whose writer is stalled for more than about 90 s. The ladder reconnects.
5. **Possible 097 fragility:**
   - `AisStreamIngestIT.sentinelCarriedForward` assumes a TOUCH updates `record_live.qa_flags`;
   - `reappear` depends on the watchdog recycling, and the test supervisor reconnecting, within 20 s of advancing the
     clock 31 minutes;
   - `AisStreamAdapterTest.updateRateLimited` measures the 1 s gap at the server with a 50 ms allowance.
6. **Instructions in data:** none. aisstream's documentation advises keeping the key in a server-side environment
   variable; that is advice to readers, not an instruction to the agent.

## Tests written (not run)

- **`AisStreamMessageParserTest`:**
  - each message type of the capture, the confirmation and an ignored type;
  - case-insensitive `MetaData`, the metadata position fallback;
  - nanosecond and other `time_utc` forms, untimed, the MMSI from `UserID` with padding;
  - every sentinel and the helper boundaries, `@` trimming;
  - malformed, `unknown_type`, `no_mmsi`, auth and non-auth error envelopes.
- **`VesselStateStoreTest`:**
  - static then position, position then static, Class B parts A/B;
  - `observedAt` = newest, with no rollback;
  - a sentinel after a fix, a vessel with no fix ever, clearing semantics;
  - eviction after 6 h (`MutableClock`), the bound;
  - `SnapshotContract.assertStableStaticHash` across position-only updates;
  - `FieldSpecContract` over all fixtures; `nextEta`.
- **`AisStreamAdapterTest`** (`LoopbackWebSocketServer`):
  - the subscription within 3 s with lat-first boxes and no MMSI key; the defaults and filters; `DEFLATE` requested;
  - fragmented binary, text, oversize, back-pressure, drop counters;
  - an auth envelope → `CredentialsRejectedException`; a transport envelope; two closes → `CredentialsRejectedException`;
    reset by a confirmation and by a new key; no key; the replayed close;
  - 120 s → degraded, 300 s → recycle; an envelope resets the silence; details and other sources;
  - `update()` rate-limited with no reconnect; unchanged and ended handles;
  - the key never in the logs at TRACE;
  - descriptor validity; `checkParams`.
- **`WebSocketOptionsIT`** (deflate loopback server):
  - negotiation, `MeteredWebSocket`;
  - the User-Agent (a caller's is ignored);
  - one budget unit per attempt, metered, the third refused, no `FetchTrace`;
  - backoff; a failed connect metered as an error;
  - inflated whole messages with compressed wire bytes; back-pressure; `DEFAULT` stays JDK.
- **`AisStreamIngestIT`** (real supervisor, sink and writer): FIRST; a 300 m move after 40 s → SAMPLE; a destination
  change → SIGNIFICANT; a sentinel → carried forward with `POSITION_STALE`; past the 30 min TTL → REAPPEAR.
- **`StreamingSupervisorIT`:** `paramsAppliedInPlace`, `paramsRestart`, `restart`.
- `HealthDetailsTest`; `TitleResolverTest.vesselFallback`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "832 requests across 80 tag folders … covers all
  ops: True" (collection unchanged).

## Notes for the next prompt

1. **Later streaming adapters (082, 083, 085, 089):** follow Adapter Guide §16.
   - Open with `openWebSocket(…, WebSocketOptions.DEFLATE)` when the upstream compresses.
   - Report `MeteredWebSocket.wireBytesReceived()` deltas.
   - Implement `onClose` with replay, and `update()` when the upstream can resubscribe in place.
   - Contribute a `HealthRule` for silence and a `HealthDetailsContributor` for details.
   - Test against `testsupport/websocket/LoopbackWebSocketServer` (with `start(true)` for deflate).
2. **085 (keyless AIS, local AIS-catcher):** reuse the `vessel` payload keys and the §5.3 policy;
   `AisStreamAdapter.defaultPolicy()` and `VesselStateStore` are package-local to `aisstream`. Move them to a
   `formats.ais` package if 085 needs them (no cross-adapter imports).
3. **097 checkpoint:**
   - first run of `DeflateWebSocket`, the aisstream tests and the supervisor's `paramsChanged`/`restart`;
   - `AisStreamIngestIT` uses its own supervisor (200 ms flush, 100 ms ladder) and a static loopback server set
     through `foresignal.test.adapters.aisstream.url`, so it is a separate cached context;
   - see open question 5.
4. **143:** with a real key (`FORESIGNAL_VERIFY_AISSTREAM_KEY` or similar):
   - recapture `frames.jsonl` per its README;
   - confirm `CompressionEnabled: true` with Java-WebSocket, the binary framing, the casing of `MetaData` keys and the
     silent close on a bad key.
5. **144 (Flyway):** no schema change.
6. **Owner decisions pending:** open questions 2 and 3, plus those carried from 012–062.
