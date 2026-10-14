# 085-CR-Keyless-AIS

| | |
|---|---|
| Prompt | 085-Keyless-AIS.md |
| Status | Complete |
| Implementation commit | 99b095016a0e8d5a1e93ca1e504ad09e888c8dfa |
| Date | 2026-10-13 |

## Summary

Three keyless vessel sources now share one in-house AIVDM codec, and all three emit prompt 063's `vessel` payload:
- `ais-kystverket`: Norway's open AIS stream;
- `ais-digitraffic`: Finland's Digitraffic MQTT;
- `local-ais-{n}`: the operator's own AIS-catcher.

The codec is a platform package, `ai.foresignal.codec.ais`, outside the adapter packages. It reads and writes:
prompt 110's NMEA sink can encode with it. No third-party AIS library is used.

**1. Shared AIVDM codec** (`ai.foresignal.codec.ais`, Java 21 records and sealed types):
- **`TagBlock`:** IEC 61162-450 `\k:v,…*hh\`.
  - The checksum is verified: a wrong one is a bad checksum, a missing one is malformed.
  - `c` is read in seconds, or in milliseconds above 11 digits; also `s`, `g` (`n-m-id`) and `n`.
  - A line without a block is not an error.
- **`NmeaSentence`:**
  - accepts `!AIVDM`, `!AIVDO` and `!BSVDM` with all their fields;
  - `parse` returns `Parsed`, `BadChecksum` or `Malformed` and never throws;
  - `format()` computes the checksum.
- **`FragmentAssembler`:**
  - key `(talker, TAG g id or sequential id, channel, count)`, at most 9 fragments, any order accepted;
  - 2 s timeout on the caller's receive time; at most 1,000 open groups, the oldest dropped and counted as expired;
  - counters and a listener for `foresignal.ais.fragments.{completed,expired}`.
- **`SixBit`:** armoring both ways, a bit `Reader` and `Writer` with signed fields, and six-bit text. Text is cut at
  the first `@` and stripped; empty or all-`@` text is absent.
- **`AisMessage`** (sealed) and **`AisDecoder`:**
  - types 1/2/3 `ClassAPosition`, 4 `BaseStationReport` (counted only), 5 `StaticVoyage`, 18 `ClassBPosition`,
    19 `ClassBExtended`, 21 `AidToNavigation` (counted only), 24 `StaticDataA`/`StaticDataB` (auxiliary craft carry
    the mother ship), 27 `LongRangePosition` at 1/10 minute; anything else is `Unsupported`;
  - a minimum length per type; a shorter payload is an `AisDecodeException`;
  - every sentinel becomes null: lat 91 / lon 181 or out of range, SOG 1023, COG ≥ 3600, heading > 359,
    ROT −128, status 15, draft 0, IMO 0, type 0, an ETA with any part unavailable, all-zero dimensions, and type 27's
    SOG 63 and COG 511.
- **`AisEncoder`** (types 1/2/3, 5, 18, 24A/B) and **`NmeaWriter`:**
  - absent values are written back as their sentinels, and values are kept on the wire resolution, so
    `decode(encode(x)) == x`;
  - a sentence is at most 82 characters with CR LF (60 payload characters per fragment);
  - sequential ids 0–9 per writer, fill bits on the last fragment only.
- **`VesselStateMerger`** and the source-neutral **`VesselUpdate`** (`Position`, `Static`), ported from prompt 063's
  merge (the God's Eye View origin header is carried; `THIRD_PARTY_NOTICES.md` row):
  - **Payload:** exactly 063's keys and units.
  - **Holding and time:** static data is held until the first usable position. `observedAt` is the newest merged
    time for an upstream time base; RECEIVED sources leave it out. Late data never rolls the state back.
  - **Quality:** REPORTED. `accuracyMeters` is 10 (the accuracy flag is high), 100 (low) or 185 (type 27).
    `quality.extra` carries `ais.rot` (raw ROT<sub>AIS</sub>) and `ais.pos_acc`.
  - **Observers:** the newest update's observer.
  - **Cache:** 100,000 MMSIs, least recently updated first out; forgotten after 6 h without an update.
  - **MMSI:** only 100000000–999999999; anything else is refused and counted. Base-station MMSIs are not counted.
  - **Seeding:** `seed` stores metadata without emitting anything.
- **`AivdmSocketCodec`** (`source.codec`, id `aivdm`, `Framing.LINES`, a Spring bean):
  - each session owns a `FragmentAssembler` and a `VesselStateMerger`;
  - `Options` set the time base (`TAG`, where a line without `c` is dropped and counted, never stamped; or
    `RECEIVED`) and the observer (TAG `s`, or a station name);
  - meters `foresignal.ais.messages{source,type}`, `foresignal.ais.dropped{source,reason}` (`checksum`,
    `malformed`, `untimed`, `undecodable`, `mmsi`) and the fragment counters;
  - a `Listener` feeds the source's station statistics.

**2. Common vessel descriptor** (`source.formats.ais.VesselDescriptors`):
- **Policy:** TRACK; SAMPLED {30 s, keyframe 1,200 s, 250 m DR, edge 15 s}; TTL 30 min, `ttlAnchor` OBSERVED;
  thresholds `cog` 15° (angular) and `sog` 3 kn; motion `{sog, kn, cog}`.
- **Fields:** significant `name, callsign, imo, shipType, destination, navStatus, draught`; tiles
  `name, shipType, sog, cog, heading, navStatus`; search `name, callsign, mmsi, imo`; title `name`.
- **Identity and display:** entity keys `mmsi`, `imo` and `callsign`; the `ship` icon; 063's `fieldSpecs`.
- **063:** `aisstream` now takes its policy and `fieldSpecs` from here.

**3. `ais-kystverket`** (`adapters.ais.KystverketAdapter`):
- **Connection:**
  - `tcpPublic` to `153.44.253.27:5631` (the descriptor's only `allowedHosts` entry), receive-only;
  - connect timeout 20 s, silence watchdog 120 s;
  - reconnect 30 s → 15 min with ±20% jitter, budget 1/min and 200/day, and at most 12 connects in any hour.
- **Records:** UPSTREAM from TAG `c`; the observer is TAG `s`.
- **License:** NLOD-2.0 with the prompt's attribution; egress OPEN.
- **Health details:** `messagesPerMin`, `distinctMmsi10m`, `lastMessageAt`, `vessels`.

**4. `ais-digitraffic`** (`DigitrafficAisAdapter`, `DigitrafficAisParser`, `AisMqttSubscriber`):
- **Connection:**
  - `wss://meri.digitraffic.fi:443/mqtt` through 060's `MqttConnections`, MQTT 3.1.1, a clean subscriber session
    that never reconnects on its own or publishes;
  - three subscriptions; client id `Foresignal; {installId}-{random6}`;
  - `Digitraffic-User: Foresignal/{version} ({installId})` on the WebSocket upgrade (new
    `MqttConnectionSpec.webSocketHeaders`, passed to HiveMQ's `httpHeaders`) and on the REST call;
  - one unit per CONNECT and per SUBSCRIBE, budget 5/min and 400/day, at most 12 connects an hour;
  - reconnect 15 s → 10 min with ±20% jitter.
- **Mapping:**
  - the MMSI comes from the topic;
  - `refA + refB` → `lengthM`, `refC + refD` → `beamM`;
  - draft from tenths of a meter; the ETA from the AIS bit field;
  - the REST list's `referencePointA..D` and `shipType` are read too.
- **Stale fixes:** a fix older than 24 h at arrival is dropped and counted.
- **Metadata bootstrap:**
  - at start, then whenever 12 h have passed (checked on every message and run on a virtual thread);
  - it seeds the merger; a failure is logged and the stream still runs.
- **Coverage:** a status `updated` older than 10 min is health `degraded` (`AisSourcesHealth`, a `HealthRule` and
  `HealthDetailsContributor` for all three sources).
- **License:** CC BY 4.0, egress OPEN.

**5. `local-ais-{n}`** (`LocalAisAdapter`, `LocalAisFamily`):
- **Slots:** on `foresignal.local-receivers.slots`, disabled, LOCAL_ONLY and overridable.
- **Params:**
  - `endpoint`, a `tcp://` `LAN_URL` with no default port;
  - `stationName`, the observer (default `local`);
  - `publishStationPosition` (default false) with `stationLat` and `stationLon`.
- **Connection:** receive-only, RECEIVED, reconnect 5 s → 5 min, budget 2/min and 200/day.
- **Station position:**
  - with publishing on, one `ais-receiver` record (externalId = `stationName`, STATIC position), sent on a
    connection's first line and every 10 minutes after;
  - with publishing off, `stationLat` and `stationLon` are refused, so the position is never stored or emitted.
- **Health details:** as for Kystverket.

**6. Fixtures** (`src/test/resources/fixtures/ais/`, with a README):
- **Real:**
  - a 10-minute Digitraffic MQTT capture (21,551 messages, gzipped);
  - the Digitraffic vessel list, trimmed to 100.
- **Synthetic:** Kystverket was unreachable; no AIS-catcher was available.
  - a 290-line Kystverket-style TAG-block stream;
  - the same sentences without TAG blocks for AIS-catcher.
- **Hand-made goldens:** every decoded type, sentinel variants, the two gpsd samples, an out-of-order type 5 and a
  bad checksum.

**7. Spec and docs:**
- **OpenAPI:** no operation changes. The `ais-receiver` kind entry is new, the `vessel` kind note is extended, and
  there is a changelog entry.
- **Source Catalog:** both Velocity rows updated with the verification notes, and a new section "Keyless AIS
  (prompt 085)" with all three rows.
- **Adapter Guide:**
  - the `aivdm` codec row;
  - the connect timeout and WebSocket upgrade headers;
  - §24 "Shared codecs", the template for platform packages that adapters and sinks share;
  - the package note at the top amended.
- **Operator Guide:** "Keyless AIS and your own AIS-catcher": the AIS-catcher `-S <port>` TCP server and the
  station-position privacy default.
- **Invariants:** SRC-1 and EGR-2 test names. **`THIRD_PARTY_NOTICES.md`:** one row.

## Files created / modified

- **Created, main:**
  - `codec/ais/`: `package-info`, `SixBit`, `TagBlock`, `NmeaSentence`, `FragmentAssembler`, `AisMessage`,
    `AisDecoder`, `AisEncoder`, `NmeaWriter`, `VesselUpdate`, `VesselStateMerger`;
  - `source/codec/AivdmSocketCodec`;
  - `source/formats/ais/`: `package-info`, `VesselDescriptors`;
  - `source/support/ConnectRateLimiter`;
  - `source/adapters/ais/`: `package-info`, `AisStationStats`, `AisMqttSubscriber`, `KystverketAdapter`,
    `DigitrafficAisAdapter`, `DigitrafficAisParser`, `LocalAisAdapter`, `LocalAisFamily`, `AisSourcesHealth`.
- **Modified, main:**
  - `source/spi/SocketOptions` (`connectTimeout`, `withConnectTimeout`);
  - `collect/socket/DefaultSocketTransport` (uses it);
  - `mqtt/MqttConnectionSpec` (`webSocketHeaders`; the 10- and 9-argument constructors kept), `mqtt/MqttConnection`
    (upgrade headers);
  - `source/adapters/aisstream/AisStreamAdapter` (policy and field specs from `VesselDescriptors`);
  - `source/codec/package-info`.
- **Created, test:**
  - `codec/ais/`: `AisGoldens`, `SixBitTest`, `TagBlockTest`, `NmeaSentenceTest`, `FragmentAssemblerTest`,
    `AisMessageDecoderTest`, `AisEncoderRoundTripTest`, `VesselStateMergerTest`;
  - `source/adapters/ais/`: `AisLoopbackTransport`, `KystverketAdapterTest`, `DigitrafficAdapterTest`,
    `LocalAisAdapterTest`, `VesselIngestIT`;
  - `fixtures/ais/`: `README.md`, `golden-sentences.tsv`, `kystverket-stream.txt`, `aiscatcher-stream.txt`,
    `digitraffic-mqtt.jsonl.gz`, `digitraffic-vessels.json`.
- **Modified, test:**
  - `retention/FamilyDefaultsTest` (three entries, class `track`);
  - `testsupport/mqtt/MosquittoSupport` (WebSocket listener 9001, `webSocketUrl()`), `mqtt/mosquitto.conf`.
- **Docs:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/Operator-Guide.md`,
  `docs/Invariants.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **The codec package follows the prompt, not Conventions' "Shared parsers" row.**
   - Conventions lists AIVDM under `ai.foresignal.source.formats.*`. The prompt names `ai.foresignal.codec.ais` and
     says why: 110's sink uses it.
   - The prompt's explicit placement was followed. The Adapter Guide's package note now names the exception.
   - `docs/Conventions.md` was not edited (owner question 1).
2. **`positionSource` is REPORTED, not "AIS".** `PositionSource` has no AIS value. Its Javadoc names AIS GNSS as
   REPORTED, and 063 uses REPORTED too.
3. **The rate-of-turn sentinel is −128, not −129.** ROT<sub>AIS</sub> is a signed 8-bit field (−128 to 127), and ITU-R
   M.1371 uses −128 for "not available". Digitraffic documents the same. `ais.rot` holds the raw ROT<sub>AIS</sub>
   value.
4. **The 6 h TTL applies per MMSI, not per static field.**
   - An MMSI that receives no update of any kind for 6 h is forgotten.
   - Digitraffic publishes metadata only on change, and its list is fetched every 12 h. Expiring static fields after
     6 h would strip names and create SIGNIFICANT versions for vessels that are still reporting.
5. **Static messages also emit a snapshot once the vessel has a position** (063's behavior). "Merged into each
   position" alone would not produce the SIGNIFICANT version on a destination change that `VesselIngestIT` asserts.
6. **`ais-receiver` is stored in the TRACK tier.**
   - The history tier is per source, so the station record cannot be STANDARD inside a TRACK source.
   - It is repeated every 10 minutes while connected, so it stays live within the 30-minute TTL.
   - "Never stored" is enforced by refusing `stationLat` and `stationLon` without `publishStationPosition`.
7. **Station health goes to `SourceHealth.details`.** 069's `station-*` family is a polling `stats.json` source with
   nothing to feed, so `messagesPerMin`, `distinctMmsi10m` and `lastMessageAt` are health details instead
   (`AisSourcesHealth`).
8. **Hourly budgets became per-minute and per-day units plus an in-adapter hourly limiter**, as in 082 and 083.
   - Kystverket: 1/min and 200/day, plus 12 connects an hour.
   - Digitraffic: 5/min and 400/day (a CONNECT, three SUBSCRIBEs and the GET), plus 12 connects an hour.
   - The limiter is `source.support.ConnectRateLimiter`.
9. **New transport options.**
   - `SocketOptions.connectTimeout`: the connect timeout was fixed at 5 s.
   - `MqttConnectionSpec.webSocketHeaders`: needed for `Digitraffic-User`.
10. **`Accept-Encoding: gzip`.** `UpstreamClient` always sends it and drops a caller's own value. The adapter passes the
    header anyway and documents that the client guarantees it.
11. **Digitraffic details.**
    - MQTT 3.1.1, verified to connect and subscribe in the capture.
    - `class` is absent: Digitraffic does not report it.
    - The REST list uses other names (`referencePointA..D`, `shipType`) than MQTT (`refA..D`, `type`); both are read.
12. **The Kystverket homepage moved.** The prompt's link now answers "page does not exist". The descriptor and the
    docs use `https://www.kystverket.no/en/sea-transport-and-ports/ais/access-to-ais-data/`, which still lists
    `153.44.253.27` port 5631, free and registration-free under NLOD (checked 2026-10-13).
13. **"Embedded MQTT broker over WebSocket" uses 060's Mosquitto Testcontainer** with a new WebSocket listener (9001),
    not an in-process broker. The upgrade header is asserted against 063's `LoopbackWebSocketServer`.
14. **The Kystverket and AIS-catcher fixtures are synthetic,** as the prompt allows ("use public AIVDM samples with
    TAG blocks"): the host timed out, and no AIS-catcher was available.
    - The streams were generated in a scratch environment with pyais 3.3.1 (MIT), which was also used as an
      independent decoder for the golden expectations.
    - No pyais code is in the repository.
    - The two gpsd samples are public. See the fixture README.
15. **`aisstream` keeps its own `VesselStateStore`.** Its quality (`ais.position_age_s`, no accuracy) and its
    message-type rules differ. Only the descriptor defaults are shared.
    - The shared field spec label is "Draft" (US English), which also changes `aisstream`'s label.
    - The payload key `draught` (063, on the wire) is unchanged.
16. **The new sources search `name, callsign, mmsi, imo`,** as prompted. `aisstream` keeps its 063 list, which also
    has `destination`.
17. **Only the three named talkers are accepted** (`AIVDM`, `AIVDO`, `BSVDM`); any other is malformed and counted.

## Issues found / open questions

1. **Owner question (Conventions):** Conventions' "Shared parsers" row puts AIVDM under `source.formats.*`, while the
   code (as prompted) has `ai.foresignal.codec.ais`. Should the Conventions row be updated to name platform codec
   packages, or the codec moved? Conventions was left unedited.
2. **Kystverket is unverified live.** It was unreachable from this host on 2026-10-13: a 25 s TCP connect timeout.
   At 143, from a host that can reach it:
   - confirm that every line carries `c` (otherwise switch the whole source to RECEIVED);
   - recapture the fixture.
3. **US English:**
   - **Left unchanged:**
     - the 063 payload key `draught` crosses the wire (British; US is "draft");
     - the descriptor builder method `licence(...)` is an existing API;
     - the attribution texts quoted from Kystverket and Fintraffic ("licence");
     - `standardised` in the EGR-2 row of `docs/Invariants.md`, which predates this prompt.
   - **Should the payload key be migrated?**
   - **Prose and new identifiers use "draft".**
4. **Possible 097 fragility:**
   - `DigitrafficAdapterTest.upgradeHeader` expects the HiveMQ start to fail after the upgrade (it may wait up to
     the 20 s subscriber timeout);
   - the WebSocket listener is new in the shared Mosquitto container;
   - the capture replay asserts exact counts (21,551 lines, 21,262 records);
   - `VesselIngestIT` relies on RECEIVED time from the `MutableClock` through the real socket transport.
5. **Instructions in data:** none. The Digitraffic payloads, the Kystverket page, the AIS-catcher source and the pyais
   package were read; none contained instructions to an agent.

## Tests written (not run)

- **`SixBitTest`:** armoring both ways and the gap; signed and unsigned fields across characters; fill bits; bounds and
  `peekOrZero`; text upper-casing, `@` padding, absent all-`@`, unknown characters.
- **`TagBlockTest`:** Kystverket-style `s` and `c` in seconds; `c` in milliseconds; `g` and `n`; a wrong checksum
  (bad checksum) and a missing one (malformed); malformed groups and numbers; a missing block.
- **`NmeaSentenceTest`:** fields and `format()`; the last fragment with a sequential id and 2 fill bits; a bad checksum
  dropped; `BSVDM` and `AIVDO` accepted; other talkers, a missing checksum, 10 fragments and a bad character
  malformed; CR LF.
- **`FragmentAssemblerTest`:** a two-part type 5; a three-part one out of order; the golden out-of-order pair;
  interleaved channels; TAG group keys; 2 s expiry with the listener; the bound of open groups; single sentences.
- **`AisMessageDecoderTest`:** golden sentences for 1/2/3, the gpsd type 1, 5 (and the gpsd type 5), 18, 19, 24A, 24B
  and 27 with expected fields; every sentinel (types 1, 5, 27); 4 and 21 counted only; unsupported types; too-short
  payloads; an auxiliary craft's part B.
- **`AisEncoderRoundTripTest`:**
  - encode → decode equality for every encodable golden; bit-for-bit payloads; hand-built messages at the
    extremes; types the encoder refuses;
  - type 5 split at the 82-character limit, identical to the independently produced sentences, fill bits on the
    last fragment;
  - sequential ids cycling 0–9; more than 9 sentences refused.
- **`VesselStateMergerTest`:**
  - static then position with every 063 key, quality, observer and the `FieldSpecContract`;
  - `SnapshotContract.assertStableStaticHash` across repeated positions;
  - Class B parts merging; type 19; type 27 at 185 m; sentinel positions; no rollback;
  - RECEIVED time; MMSI validation; bounds and the 6 h TTL; seeding; ETA rollover.
- **`KystverketAdapterTest`** (embedded TCP server, `AisLoopbackTransport`):
  - the fixture replayed into 272 records with TAG times and base-station observers;
  - zero bytes written; one unit;
  - receive-only options with 20 s and 120 s;
  - untimed and bad-checksum lines dropped and counted (with the meter); the hourly cap;
  - the descriptor.
- **`DigitrafficAdapterTest`** (`@IntegrationTest`, real `UpstreamClient`, WireMock, Mosquitto over WebSocket):
  - **Stream:** the three subscriptions paid (4 units); a location carrying the bootstrapped metadata; status
    tracking.
  - **Connection:** the spec (3.1.1, subscriber session, client id format, user header); the upgrade header at
    `LoopbackWebSocketServer`.
  - **Mapping:** 24 h stale drop and malformed counts; metadata mapping (lengths, tenths, ETA bits).
  - **Capture:** the 10-minute capture replayed.
  - **Bootstrap:** at start and after 12 h, with WireMock answering 406 without gzip; a failed bootstrap.
  - **Health and descriptor:** status older than 10 min degraded; the descriptor.
- **`LocalAisAdapterTest`** (embedded TCP server):
  - no station record by default, records observed by `local`, RECEIVED, nothing written, one unit, health details;
  - the published station record and its 10-minute repeat;
  - station parameter rules; start refusing bad parameters before any unit;
  - the family's slot range and validation; the descriptor.
- **`VesselIngestIT`:** `local-ais-1` through the real supervisor, sink and writer: FIRST and a KEYFRAME after 1,200 s,
  every version in the TRACK tier; a 300 m move after 40 s is a SAMPLE; a destination change is SIGNIFICANT.
- **Updated:** `FamilyDefaultsTest` (three adapters, `track`).

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "834 requests across 80 tag folders … covers all
  ops: True". The collection is unchanged.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK. The added lines
  were also checked with `check_us_english.py` (see Issue 3).
- **Scratch checks** (throwaway `main` classes in the scratchpad, outside the repository, with Gradle's JDK 21):
  - every golden decodes to the oracle's values and round-trips;
  - the encoder reproduces the golden payloads bit for bit, and `NmeaWriter`'s type 5 sentences match the
    independently produced ones;
  - both streams give 272 records (240 type 1, 20 type 18, 4 type 24, 8 type 5) from 5 vessels with the expected
    observers;
  - the Digitraffic capture gives 21,262 records from 599 MMSIs, and the list gives 100 seeds;
  - `DescriptorValidator.problems` and `IngestPolicyValidator.problems` are empty for all four descriptors. The
    warnings are the same as `aisstream`'s: `mmsi` searched but not significant; `sog`, `cog` and `heading` tiled but
    not significant.
- No project tests were run (feature prompt).

## Notes for the next prompt

1. **086 (and later socket or MQTT sources):**
   - `SocketOptions.withConnectTimeout` sets a connect timeout other than 5 s;
   - `source.support.ConnectRateLimiter` caps connects per hour;
   - `MqttConnectionSpec.webSocketHeaders` adds headers to a WebSocket MQTT upgrade;
   - `AisMqttSubscriber` is 083's subscriber pattern again (adapters do not import each other).
2. **097 checkpoint:**
   - first run of every 085 test;
   - `DigitrafficAdapterTest` is an `@IntegrationTest` that also uses Docker (Mosquitto with the new WebSocket
     listener on 9001);
   - `VesselIngestIT` builds its own supervisor (200 ms flush, 100 ms ladder) and a loopback server;
   - `FamilyDefaultsTest` has the three new adapters;
   - see Issue 4.
3. **099 (derived layers):** every vessel source has the same payload keys; `class` is absent for `ais-digitraffic`;
   `quality.accuracyMeters` is set by the 085 sources only (not `aisstream`).
4. **110 (NMEA sink):**
   - encode with `AisEncoder` and `NmeaWriter` (types 1, 5, 18, 24A/B);
   - map a stored vessel snapshot back to `AisMessage` values on the wire resolution (positions to 1/10,000 minute,
     SOG and COG to 1/10, draft to 1/10 m);
   - call the row-level `EgressPolicy.allows` (069): `ais-kystverket` and `ais-digitraffic` are OPEN, `local-ais-{n}`
     LOCAL_ONLY (overridable), `aisstream` LOCAL_ONLY.
5. **143:**
   - recapture per `fixtures/ais/README.md`: Kystverket from a host that reaches it (confirm `c` on every line), an
     AIS-catcher `-S` stream, a Digitraffic capture;
   - verify live that the `Digitraffic-User` upgrade header is accepted.
6. **144 (Flyway):** no schema change. The new sources keep only in-memory state.
7. **Owner decisions pending:** open questions 1 and 3, plus those carried from 012–084.
