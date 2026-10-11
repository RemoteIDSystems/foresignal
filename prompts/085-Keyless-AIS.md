# 085 — Keyless AIS

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/085-CR-Keyless-AIS.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/084-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `ais-kystverket`, `ais-digitraffic` (Velocity survey) and `aisstream`
- `docs/Core-Data-Model.md` §2 (streaming row: observed = newest merged message time), §3 (adapter rules 7 and 11, sentinels), §5.3 (Vessels row and vessel significant fields), §7 (streaming sinks), §14 (observer)
- `docs/Gap-Analysis.md` §4a row "AIS-catcher", §2 row "OpenCPN / Signal K", §8.C item 44 ("Extend 048" = this prompt)
- Completion Reports of 063 (`aisstream`: vessel payload names, fieldSpecs, static merge), 069 (socket transport, codec SPI, station health), 060 (MQTT client library)
- ITU-R M.1371-5 and the AIVDM/AIVDO protocol notes (https://gpsd.gitlab.io/gpsd/AIVDM.html); https://www.kystverket.no/en/navigation-and-monitoring/ais/access-to-ais-data/; https://www.digitraffic.fi/en/marine-traffic/

## Objective

Add three keyless vessel sources (Norway's open AIS stream, Finland's Digitraffic MQTT and the operator's own AIS-catcher receiver) on one shared, in-house AIVDM codec. The codec is a platform piece: prompt 110's NMEA sink encodes with it, so it lives outside the adapter packages and covers both directions.

## Tasks

### 1. Shared AIVDM codec (`ai.elaro.codec.ais`)

Implement in-house, in Java 21 records and sealed types. Do not use `tbsalling/aismessages` (non-free licence). `dk.dma.ais.lib` (Apache-2.0) may be consulted, but write our own so the encoder and sentinel handling match Elaro's rules.

- **`TagBlock`**: parse and strip an IEC 61162-450 TAG block `\k:v,k:v*hh\` before the sentence (`c` unix time in seconds or milliseconds, `s` source station, `g` group `n-m-id`, `n` line count), verifying the checksum. A missing TAG block is not an error.
- **`NmeaSentence`**: `!AIVDM`/`!AIVDO`/`!BSVDM` with fragment count, number, sequential id, channel, payload, fill bits and `*hh` checksum (bad checksum → dropped and counted).
- **`FragmentAssembler`**: key `(talker, seqId or TAG group id, channel, fragCount)`, max 9 fragments, 2 s timeout, bounded at 1,000 open groups; out-of-order fragments accepted; counts `elaro.ais.fragments.{completed,expired}`.
- **`SixBit`**: armour decode/encode (ASCII 48–119 → 6-bit), bit reader/writer with signed fields, 6-bit text with `@` padding trimmed.
- **`AisMessage`** sealed decoders: 1/2/3 (Class A position), 4 (base station, counted only), 5 (static and voyage), 18 (Class B position), 19 (Class B extended), 21 (AtoN, counted only), 24A/24B (Class B static), 27 (long-range; position at 1/10 minute). Other types are counted and dropped.
- **Sentinels → absent** (adapter rule 11): lat 91 / lon 181, SOG 1023 (102.3 kn), COG 3600, heading 511, ROT −128, draught 0, ETA month 0, empty or all-`@` names.
- **Encoder** (for prompt 110): `AisEncoder` for types 1, 5, 18 and 24A/B, and `NmeaWriter` that splits into fragments ≤ 82 characters with sequential ids 0–9 and correct fill bits. Round-trip tests prove decode(encode(x)) == x.
- **`AivdmSocketCodec`** (id `aivdm`, `Framing.LINES`) in 069's `ai.elaro.source.codec` wraps the codec for socket sources; its session owns a `FragmentAssembler` and a `VesselStateMerger`.
- **`VesselStateMerger`**: per-MMSI static cache (types 5, 19, 24A/B; 6 h TTL; bounded 100k LRU) merged into each position into one complete snapshot (adapter rule 7). Use **exactly 063's `aisstream` payload keys, units and fieldSpecs** so derived layers (099) and the 110 sink treat every vessel source alike: `mmsi, name, callsign, imo, shipType` (int), `class` (`A|B`), `destination, navStatus, draught` (m), `lengthM` (A+B), `beamM` (C+D), `eta` (ISO-8601 UTC, omitted when any part is unavailable), `sog, cog, heading`. Rate of turn and the position-accuracy flag go to `quality.extra` (`ais.rot`, `ais.pos_acc`), not the payload. Static data for an MMSI with no position yet is held, not emitted (063's rule). If 063's CR shows different names, follow 063.

### 2. Common vessel descriptor

- Kind `vessel`, externalId = MMSI as a 9-digit string; reject MMSI 0 and values outside 100000000–999999999.
- §5.3 Vessels row: TRACK, SAMPLED, minInterval 30 s, keyframe 1,200 s, TTL 30 min, `ttlAnchor` OBSERVED, deviation 250 m dead reckoning, thresholds `cog` 15° (angular) and `sog` 3 kn, edgeMinInterval 15 s, significant fields `name, callsign, imo, shipType, destination, navStatus, draught`, `motion {speedField: sog, speedUnit: kn, courseField: cog}`.
- tileProperties `name, shipType, sog, cog, heading, navStatus`; searchFields `name, callsign, mmsi, imo`; titleField `name` (fallback `mmsi`).
- Quality: `accuracyMeters` 10 when `posAccuracy` is high, 100 otherwise, 185 for type 27; `positionSource` AIS.

### 3. `ais-kystverket` (STREAMING, raw TCP)

- Host `153.44.253.27`, port `5631` (descriptor `allowedHosts`) via 082's `ctx.sockets().tcpPublic(…, SocketOptions.receiveOnly(), Framing.LINES, …)` (add it here if 082 has not run yet), codec `aivdm`. NMEA lines with TAG blocks.
- **Verified 2026-10:** Kystverket's access page still lists this endpoint as open and registration-free under NLOD 2.0, but a TCP connect from our US test host timed out. Treat it as best effort: connect timeout 20 s, reconnect 30 s → 15 min with jitter, silence watchdog 120 s, budget 12 connects/hour, 200/day. Health shows `failing` when unreachable; never route around it.
- `observedAtMode` UPSTREAM: `observedAt` = TAG `c` (seconds or milliseconds). During fixture capture confirm every line carries `c`; if not, switch the descriptor to RECEIVED for the whole source (never a per-record fallback) and note it.
- Observer: `receiverId` = TAG `s` (base station id).
- Open-tier coverage: Norwegian economic zone and the Svalbard/Jan Mayen protection zones; fishing vessels under 15 m and leisure craft under 45 m are excluded upstream. Licence `NLOD-2.0`, attribution "Contains data under the Norwegian licence for Open Government data (NLOD) distributed by the Norwegian Coastal Administration".

### 4. `ais-digitraffic` (STREAMING, MQTT over WSS)

- `wss://meri.digitraffic.fi:443/mqtt` through 060's `MqttConnections` (HiveMQ WebSocket transport; this host is fixed in the descriptor, not operator input). Subscribe `vessels-v2/+/location`, `vessels-v2/+/metadata` and `vessels-v2/status` (3 subscriptions). Client id `Elaro; {installId}-{random6}` (the format in Fintraffic's examples); send `Digitraffic-User: Elaro/{version} ({installId})` on the WebSocket upgrade.
- Location JSON `{time (s), sog, cog, navStat, rot, posAcc, raim, heading, lon, lat}`; metadata JSON `{timestamp (ms), destination, name, draught, eta, posType, refA..refD, callSign, imo, type}`. MMSI comes from the topic. Map into the 063 payload keys; `refA+refB` → `lengthM`, `refC+refD` → `beamM`.
- Metadata bootstrap: `GET https://meri.digitraffic.fi/api/ais/v1/vessels` every 12 h with `Accept-Encoding: gzip` (**verified 2026-10:** the API answers 406 without it) and the `Digitraffic-User` header; seed `VesselStateMerger`.
- **Drop location fixes older than 24 h** (Velocity found many stale fixes on the REST side; apply the guard on MQTT too). Never use the REST `locations` endpoint.
- `vessels-v2/status` `updated` older than 10 min → mark coverage degraded. Budget 12 connects/hour; reconnect 15 s → 10 min. Licence CC BY 4.0, attribution "Source: Fintraffic / digitraffic.fi, licence CC 4.0 BY".

### 5. `local-ais-{n}` (STREAMING, AIS-catcher over TCP)

- Slot family on 069's pattern (disabled, `egress` LOCAL_ONLY, `egressOverridable=true`). Param `endpoint` (`LAN_URL tcp://`, e.g. AIS-catcher started with `-S <port>` as a TCP server; no default port), `stationName` (Observer `receiverId`, default `local`), `publishStationPosition` (boolean, default false) with `stationLat`/`stationLon`.
- `ctx.sockets().tcp(endpoint, SocketOptions.receiveOnly(), Framing.LINES, …)` with the `aivdm` codec registered on 069's codec SPI. `observedAtMode` RECEIVED (AIS-catcher NMEA carries no timestamp; LAN latency is negligible).
- Station health: feed 069's `station-*` health family (messages/min, distinct MMSI/10 min, last message) if present.
- When `publishStationPosition=true`, emit one record kind `ais-receiver` (STANDARD) with the station position; otherwise the station location is never stored or emitted. Mention in docs that the 059 AIS-catcher HTTP preset remains the alternative for remote stations.

### 6. Fixtures

Record 10 minutes of each stream (`ais-kystverket` raw lines with TAG blocks, Digitraffic MQTT messages as JSON lines with their topics, AIS-catcher NMEA) plus the Digitraffic metadata response trimmed to 100 vessels. Add hand-made golden sentences for every decoded type, a two-part type 5 out of order, and a bad checksum. If the Kystverket host is unreachable at capture time, use public AIVDM samples with TAG blocks and note it in the README.

### 7. Spec, Postman, docs

- No new operations. If kinds are enumerated in `docs/OpenAPI.json`, add `ais-receiver`.
- `docs/Source-Catalog.md`: update both Velocity rows (verification notes above) and add `local-ais-*`. `docs/Adapter-Guide.md`: "Shared codecs" (the AIVDM codec is the template for non-adapter packages used by adapters and sinks). `docs/Operator-Guide.md`: AIS-catcher TCP server setup and the station-position privacy default.

## Tests to write (do not run)

- `SixBitTest`, `TagBlockTest` (checksum, `c` in s and ms, missing block), `NmeaSentenceTest` (bad checksum dropped, fill bits).
- `FragmentAssemblerTest`: 2- and 3-part type 5, out of order, interleaved channels, expiry, bound.
- `AisMessageDecoderTest`: golden sentences for 1/2/3/5/18/19/24A/24B/27 with expected fields and every sentinel.
- `AisEncoderRoundTripTest`: encode → decode equality; fragment split at the 82-character limit.
- `VesselStateMergerTest`: static + position snapshot; `SnapshotContract.assertStableStaticHash` across repeated positions.
- `KystverketAdapterTest` (embedded TCP server replaying a captured fixture), `DigitrafficAdapterTest` (embedded MQTT broker over WebSocket: subscriptions, client id, 24 h stale drop; WireMock REST metadata with the gzip requirement), `LocalAisAdapterTest` (station position absent by default; nothing written to the socket).
- `VesselIngestIT`: SAMPLED persistence of a vessel track (keyframe and SIGNIFICANT on destination change), TRACK tier rows.

## Acceptance criteria

- One codec decodes all three feeds and encodes the types 110 needs, with no third-party AIS dependency.
- Vessel records from all three sources share 063's payload contract.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `085: Keyless AIS`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/085-CR-Keyless-AIS.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `085: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
