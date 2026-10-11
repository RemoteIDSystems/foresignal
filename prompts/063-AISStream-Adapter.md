# 063 — AISStream Adapter

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/063-CR-AISStream-Adapter.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/062-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 (row "Streaming (AIS, MQTT)"), §3 (rules 7, 8 and 11; carry-forward), §5.3 (Vessels row and vessel significant fields), §7 (streaming sinks: one outstanding batch, SAMPLED map keyed by externalId, flush resolution)
- `docs/Source-Catalog.md` row `aisstream`
- `docs/Gap-Analysis.md` §9 (re-serving aisstream data to peers or sinks is rejected)
- Completion Report of 010 (`StreamingSupervisor`, reconnect ladder, `CredentialsRejectedException`, `PolicyAwareRecordSink`) and of 062 (`egress` descriptor field)
- GEV (MIT, portable): `server/providers/vessels/ais-live.js` (constants, ladder, auth probe), `server/providers/vessels/ais-store.js` (static merge, SOG/COG/heading sentinels, `time_utc` parsing), `src/data/aisStreamAdapter.js` (recognised envelope = liveness, error classification), `src/data/aisWatchdog.js` (report fast, recycle slowly)
- Upstream: `https://aisstream.io/documentation`, message models `https://github.com/aisstream/ais-message-models/blob/master/type-definition.yaml`

## Objective

Build Elaro's first streaming adapter: `aisstream`, a WebSocket feed of global AIS. It merges position and static messages into complete per-MMSI vessel snapshots, scrubs AIS sentinels, and stays polite under one connection per key with a slow reconnect ladder. It is also the reference pattern for every later streaming adapter (085, 082, 083, 089), so the Adapter Guide gains a streaming section here.

## Tasks

### 1. Descriptor (`ai.elaro.source.adapters.aisstream.AisStreamAdapter implements StreamingAdapter`)

| Field | Value |
|---|---|
| id / kinds | `aisstream` / `{vessel}` |
| mode | STREAMING |
| credentials | `apiKey` (required). The source therefore stays disabled until a key is set: enabling without one returns 409 `CREDENTIALS_REQUIRED` (004). |
| params | `bbox` (string-list of `minLon,minLat,maxLon,maxLat`, 1–10 entries, each range-checked; default `-180,-90,180,90`); `messageTypes` (string-list; allowed `PositionReport, StandardClassBPositionReport, ExtendedClassBPositionReport, ShipStaticData, StaticDataReport`; default all five); `mmsi` (string-list of 9-digit strings, ≤ 50, optional) |
| budget | 2/min, 144/day (each connection attempt is one unit; the ladder below fits) |
| tier / time | TRACK, `observedAtMode` UPSTREAM, `ttlAnchor` OBSERVED, `recordTtl` 30 min |
| policy | `SAMPLED{minInterval 30s, keyframe 1200s, minDeviationMeters 250, thresholds true, edgeMinInterval 15s}`. Check: `1200 ≤ 1800 − 2 − 30`. |
| significantFields | `name, callsign, imo, shipType, destination, navStatus, draught` |
| motion / thresholds | `{speedField: sog, speedUnit: KNOTS, courseField: cog}`; `cog` 15° angular, `sog` 3 kn |
| tile / search / title | tiles `name, shipType, sog, cog, heading, navStatus`; search `name, callsign, mmsi, imo, destination`; `titleField` `name` (fallback `mmsi`) |
| licence / egress | "aisstream.io terms (unverified)", `termsUrl` `https://aisstream.io`, `egress` **LOCAL_ONLY**. Verified 2026-10: the site has a privacy policy but no terms or redistribution clause, and its FAQ advises proxying "only the information your clients need". Never re-serve the raw feed. |

Keyless note for the descriptor description and the Source-Catalog row: no key means no source. Keyless official AIS (`ais-kystverket`, `ais-digitraffic`) and local AIS-catcher arrive in 085.

### 2. Connection and subscription

- Open with `ctx.http().openWebSocket(URI.create("wss://stream.aisstream.io/v0/stream"), listener, Map.of())`. It consumes budget and sets the Elaro UA.
- **Within 3 s of `onOpen`**, send one text frame:

  ```json
  {"APIKey":"…","BoundingBoxes":[[[minLat,minLon],[maxLat,maxLon]],…],"FilterMessageTypes":[…],"FiltersShipMMSI":[…]}
  ```

  Boxes are **latitude first**, one corner pair per box. Omit `FiltersShipMMSI` when the param is empty. Never log the frame unredacted.
- **Frames are binary UTF-8 JSON** (verified 2026-10). Handle both `onBinary` and `onText`. Accumulate fragments until `last=true`, cap a message at 256 KB (drop it and count larger ones), and call `request(1)` only after handing a message off, so back-pressure reaches the socket.
- **Compression (verified 2026-10):** since September 2026, aisstream applies per-user bandwidth limits to connections without `permessage-deflate` and silently drops the excess. JDK `java.net.http.WebSocket` cannot negotiate extensions.
  - Add `WebSocketOptions(boolean perMessageDeflate)` and an `openWebSocket(uri, listener, headers, options)` overload to `SourceHttp`, implemented in `collect` with a deflate-capable client. Prefer `org.java-websocket:Java-WebSocket` (MIT, `PerMessageDeflateExtension`); check Maven Central for the current release.
  - Budget, UA, metering (`reportBytes` on compressed bytes) and `FetchTrace` must behave exactly as in the JDK path. Request `perMessageDeflate=true` here.
  - Log whether `SubscriptionConfirmation.Message.CompressionEnabled` is true and expose it as a health detail.
  - If the library route fails, keep the JDK client, default the docs to a narrower bbox, and record the limitation in the Completion Report.
- **One connection per key.** The supervisor runs one stream per source, and aisstream allows 3 subscribed connections per account and 3 open connections per IP. The Operator Guide tells operators not to reuse the key elsewhere.
- **Parameter changes:** add an optional `default boolean update(ValidatedParams p)` to `StreamHandle` (returning false means "restart me"), if 010 lacks it. aisstream resends the subscription, which **replaces** the previous one, at most once per second (faster updates close the connection), without reconnecting.

### 3. Liveness, errors and reconnect

- **Recognised envelope** (the only proof of liveness): an object with a `MessageType` from the full AIS message-type set, a body under `Message.<MessageType>`, and an MMSI (`MetaData.MMSI`, else `UserID`). `SubscriptionConfirmation` is not liveness.
- **`{"error": "…"}` envelope:** if the text matches the auth pattern (port GEV's `AUTH_TEXT` regex: unauthorized, forbidden, invalid/bad/missing api key, authenticate), throw `CredentialsRejectedException`. Otherwise it is a transport error.
- **Invalid key behaviour (verified 2026-10):** an invalid subscription gets no reply, and a bad key just closes the socket. Two consecutive closes before any `SubscriptionConfirmation` or recognised envelope throw `CredentialsRejectedException`. 010 then retries hourly (GEV `AISSTREAM_AUTH_PROBE_MS`).
- **Ladder:** reuse 010's 5 s → 15 s → 60 s → 300 s → 900 s repeating, reset after 10 min healthy. Confirm the 010 implementation matches; adjust it if not.
- **Silence watchdog:**
  - Report `degraded` ("connected but silent for N s") after **120 s** with no recognised envelope, through a `HealthRule` (006 SPI).
  - Recycle (abort, never wait for a close handshake) only after **300 s** (2.5×). The recycle counts as a ladder step.
- On stop: abort the socket and flush the sink.

### 4. Parser and merged snapshots (`AisStreamMessageParser`, `VesselStateStore`)

- **Pure parser:** `parse(String json) → AisMessage | Error | Malformed`. Read `MetaData` keys **case-insensitively** (`latitude`/`Latitude`, `time_utc`/`TimeUtc`), since verified samples differ.
- **Time:** parse `time_utc` such as `2023-05-10 11:46:52.509865357 +0000 UTC` to nanosecond precision. A message without a parseable time is dropped and counted (`reason=untimed`), never stamped with receive time (the time base is fixed, CDM §2).
- **Positions** from `PositionReport`, `StandardClassBPositionReport` and `ExtendedClassBPositionReport`: `Latitude`, `Longitude`, `Sog`, `Cog`, `TrueHeading`, `NavigationalStatus` (Class A only).
- **Static data** from `ShipStaticData` (`Name`, `CallSign`, `ImoNumber`, `Type`, `Dimension{A,B,C,D}`, `Eta{Month,Day,Hour,Minute}`, `MaximumStaticDraught`, `Destination`) and `StaticDataReport` parts A (name) and B (type, callsign, dimension).
- **Sentinels (rule 11):**
  - lat 91 / lon 181, or out of range: no position (null geometry; TRACK carry-forward applies)
  - `Sog ≥ 102.3`: omit `sog`; `Cog ≥ 360`: omit `cog`; `TrueHeading 511` (or > 359): omit `heading`
  - `NavigationalStatus 15`: omit; IMO 0, draught 0 and zero dimensions: omit
  - strings: trim spaces and `@` padding; empty strings omitted
- **`VesselStateStore`:** a per-MMSI merged state held in the adapter instance. It is a bounded map (200,000 entries) that evicts entries unseen for 6 h.
  - Every recognised message updates the state and emits **one complete snapshot** `NormalizedRecord` (rule 7).
  - `externalId = mmsi`; `observedAt` = the time of the newest message merged into the state; `quality.extra["ais.position_age_s"]` holds the age of the merged position.
  - Static data for an MMSI with no position yet is held, not emitted.
  - Payload: `mmsi, name, callsign, imo, shipType` (int), `class` (`A|B`), `destination, navStatus, draught` (m), `lengthM` (A+B), `beamM` (C+D), `eta` (ISO-8601 UTC at the next occurrence of month/day/hour/minute, omitted when any part is "not available"), `sog, cog, heading`.
  - `quality.positionSource` REPORTED.
- Send records to the sink as they come. 010's `PolicyAwareRecordSink` keeps the newest `observedAt` per MMSI per 2 s flush.
- **Metrics:** `elaro.aisstream.messages{type}`, `elaro.aisstream.dropped{reason=untimed|oversize|malformed|unknown_type|no_mmsi}`, gauge `elaro.aisstream.vessels`.

### 5. Fixtures and capture

- `src/test/resources/fixtures/aisstream/README.md` documents the procedure:
  - `websocat -B 1048576 --binary wss://stream.aisstream.io/v0/stream`
  - paste a subscription with a test key and a small bbox (e.g. the Solent)
  - save ~200 frames to `frames.jsonl`
  - record the date and the `MetaData` key casing observed
  - **never commit the key**
- Hand-made fixtures: `sentinels.jsonl`, `static-before-position.jsonl`, `classb-parts.jsonl`, `error-auth.json`, `untimed.json`.

### 6. Docs, catalogue, spec

- `docs/Adapter-Guide.md`, new section "Streaming adapters":
  - liveness proof, subscription timing, back-pressure (`request(1)`)
  - auth vs transport classification, the ladder, report-fast/recycle-slow watchdogs
  - merged snapshots, sentinels, no receive-time stamping
  - the `update()` hook, `WebSocketOptions`
- `docs/Source-Catalog.md` `aisstream` row: tier, policy, TTL, time base, significant fields, egress LOCAL_ONLY, verified limits (3 connections per account and per IP, subscribe within 3 s, update ≤ 1/s, binary frames, compression bandwidth rule).
- Operator Guide: getting a key, one key per instance, bbox sizing vs bandwidth.
- OpenAPI: no operation changes. Postman unchanged.
- `THIRD_PARTY_NOTICES.md`: GEV entries for the sentinel, merge and watchdog logic, with origin headers in the ported classes.

## Tests to write (do not run)

- `AisStreamMessageParserTest`:
  - each message type from the captured fixture
  - case-insensitive `MetaData`
  - nanosecond `time_utc`
  - every sentinel; `@` trimming
  - malformed JSON, unknown type and error envelope
- `VesselStateStoreTest`:
  - static then position, position then static
  - Class B parts A/B merge
  - `observedAt` = newest merged message
  - eviction after 6 h (`MutableClock`), bounded size
  - `SnapshotContract.assertStableStaticHash` across position-only updates
- `AisStreamAdapterTest`, with an embedded WebSocket test server on loopback:
  - the subscription is sent within 3 s with lat-first boxes and no MMSI key when empty
  - fragmented binary frames are reassembled; an oversize message is dropped
  - an auth error envelope → `CredentialsRejectedException`
  - two closes before confirmation → `CredentialsRejectedException`
  - 120 s silence → degraded `HealthRule`; 300 s → recycle (`MutableClock`)
  - `update()` resends the subscription, rate-limited to 1/s, with no reconnect
  - the API key never appears in logs or `FetchTrace`
- `WebSocketOptionsIT`: a deflate-negotiating test server; budget consumed once per attempt; UA present.
- `AisStreamIngestIT` (`@IntegrationTest`, fake stream through the real supervisor and sink):
  - first snapshot → FIRST
  - 300 m move after 40 s → SAMPLE
  - destination change → SIGNIFICANT
  - sentinel position → geometry carried forward with `POSITION_STALE`
  - silence past 30 min → expiry, then REAPPEAR

## Acceptance criteria

- `aisstream` registers as STREAMING, TRACK + SAMPLED, with `egress=LOCAL_ONLY`, and cannot be enabled without `apiKey`.
- No reconnect path is faster than the ladder, and a rejected key retries only hourly.
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
2. **Commit** all changes with the message `063: AISStream Adapter`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/063-CR-AISStream-Adapter.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `063: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
