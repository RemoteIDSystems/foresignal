# 083-CR-Mesh-Sources

| | |
|---|---|
| Prompt | 083-Mesh-Sources.md |
| Status | Complete |
| Implementation commit | 63492de3dabb86cb3d0f5ecaf06e0236d4f15e19 |
| Date | 2026-10-13 |

## Summary

Five mesh sources, all **disabled by default**, without any Meshtastic protobuf (every Meshtastic path reads the
firmware's JSON). All five descriptors pass `DescriptorValidator` and `IngestPolicyValidator` (scratch check; see
"Compile result").

**1. Shared mesh model** (`ai.foresignal.source.adapters.mesh`):
- **`MeshRecords`:** kinds `mesh-node` and `mesh-message`, the allowlisted payload, and the shared descriptor
  defaults:
  - STANDARD, ALL (keyframe 5 h), TTL 6 h OBSERVED, retention 365 d (`state`);
  - the prompt's significant, tile and search fields; telemetry and the heard and advert times volatile;
  - field specs; no `embedFields`.

  It also builds the records: `lowBattery` (`batteryLevel < 15`), and `Quality.accuracyMeters` from the precision
  bits (`2^(32−bits) × 1e-7°`, halved).
- **IDs:** externalId `!` + 8 hex (unsigned) or `mc:` + 12 hex; Meshtastic role numbers become role names.
- **`MeshNodeState` / `MeshNodeStates`:** the per-node merge (rule 7), 20,000 nodes, LRU, thread-safe. A telemetry
  message still carries the last position.
- **`MeshTextMessage` and `MeshMessageBus`** (in-process, nothing stored). `meshtastic-local`, `meshcore-local` and
  `meshcore-bridge` publish every text to it.
- **Observers and coverage:** the gateway or own node is the observer (RSSI, SNR). The new internal descriptor flag
  `coverageFromObservers` makes each stream flush report the distinct observer ids as `Coverage.receivers`
  (`PolicyAwareRecordSink.coverage`).
- **`MeshtasticJsonParser`** (pure):
  - only `<root>/2/json/<channel>/<gateway>` is parsed; `/2/e/` and `/2/map/` are dropped by topic, unparsed;
  - types `nodeinfo`, `position`, `telemetry` (device metrics) and `text`; unknown types are reported for counting;
  - node numbers are read as unsigned, including old signed values and `!xxxxxxxx` strings;
  - other string ids are malformed. Real public traffic has third-party variants whose decimal and hex strings cannot
    be told apart.
- **`MeshtasticStream`:** the shared per-message pipeline:
  - the inbound cap before parsing (`foresignal.mesh.dropped{source,reason}`, rate WARN at most every 10 min);
  - parse, merge, text handling.
- **MQTT (060's client):** `MqttConnectionSpec` gains `MqttSessionOptions` (keepalive, clean start, subscribe QoS,
  auto-reconnect), and the status topic is optional for a non-reconnecting subscriber. `MqttConnection` gains:
  - no Will and no publishes without a status topic;
  - `onDisconnected`, `connectedFuture()` and `subscribeAcknowledged` (checks SUBACK codes).

  060's existing behavior is unchanged (the 9-argument constructor keeps the defaults). `MeshMqttSubscriber` charges
  one unit for the CONNECT and one per SUBSCRIBE (`ctx.chargeRequest`, the existing `consumeBudget` equivalent) and
  returns a handle that reports a drop to the supervisor.
- **LAN URLs:** `mqtt` and `mqtts` join `tcp`/`udp`, with an explicit port and the host pinned private like `tcp://`
  (`ParamValidator`, `LanEndpoint.Scheme.MQTT/MQTTS`).

**2. `meshtastic-mqtt`** (`MeshtasticMqttAdapter`, `MeshtasticMqttRules`): the Mesh.md rules in code.
- **Connection:**
  - broker `mqtt.meshtastic.org:1883` (descriptor `allowedHosts`), public credentials as descriptor constants,
    MQTT 3.1.1;
  - client id `foresignal-<installId>`, keepalive 120 s, clean session, nothing published.
- **Params:** `root` (pattern enforced; wildcards refused), `extraRoots`, `maxSubscriptions` (default 1, at most 3,
  roots must fit; `checkParams` gives 400 `VALIDATION_FAILED` at PUT), `includeText`.
- **Subscriptions:** `<root>/2/json/#` per distinct root, QoS 0.
- **Limits:**
  - budget 4/min (one CONNECT and three SUBSCRIBEs) and 100/day;
  - at most 12 connects in any rolling hour (in-adapter limiter);
  - reconnect through the supervisor only, 15 s → 300 s ±20%;
  - inbound 100/min.
- **Behavior:** a WARN summarizes the rules when the stream starts (at most hourly); `zeroResultsNormal=true`.

**3. `meshtastic-map`** (`MeshtasticMapAdapter`, `MeshtasticMapParser`):
- one conditional GET a day; minInterval 24 h; budget 1/min and 2/day; 64 MB cap; 180 s timeout;
- Jackson streaming parser, one node tree at a time (the whole document is never a tree);
- `observedAt` = `updated_at`, else `position_updated_at`; a node with neither is skipped and counted;
- attribution "Meshtastic Map by Liam Cottle", `licence` "unspecified (community service)".

**4. `meshtastic-local`** (`MeshtasticLocalAdapter`):
- **Broker:** `brokerUrl` (`mqtt://` connects to the pinned address; `mqtts://` connects by name for TLS after
  re-checking that it resolves privately), optional `username`/`password` credentials.
- **Subscriptions:** `root` (default `msh/local`), and `channels` (empty means `<root>/2/json/#`, otherwise
  `<root>/2/json/<channel>/#` each).
- **Records:** same parser and merge; a 1,000/min safety cap; every text published to the bus; text records only
  with `includeText`.
- **Descriptor:** private; budget 20/min and 2,000/day; reconnect 5 s → 5 min.

**5. `meshcore-local`** (`MeshCoreLocalAdapter`, `MeshCoreCompanionLink`, `MeshCoreLinks`, `MeshCoreEventMapper`,
`RepeaterHealth`, and `source.formats.meshcore`):
- **Protocol** (`MeshCoreProtocol`, `MeshCoreFrameDecoder`, `MeshCoreFrames`), verified against the MIT firmware
  source in the local clone:
  - `>`/`<` + uint16 LE framing; frames over 176 bytes skipped; resync after junk;
  - the contact layout (148 bytes, ×1e-6 coordinates, NUL-terminated name), self info, device info, battery,
    channel names (the secret is never read), and V3 and older text frames.
- **`MeshCoreCommandGuard`:**
  - the eight read commands;
  - `allowSend` accepts only 0x02, 0x03 and 0x3E (for 084);
  - a named never-list: every set, key, contact edit, reboot, factory reset and remote-request command;
  - an exact envelope.
- **Command writer:** `SocketOptions.commandWriter(CommandGuard)` (new `WriteMode.GUARDED_COMMANDS`) makes
  `DefaultSocketTransport` return a `CommandHandle`. Its `send` passes the guard before writing and refuses after
  close. Every other handle stays receive-only.
- **Link:** one per pinned `host:port`, shared, owners attach and detach.
  - Reconnect 5 s → 5 min, reset after 10 min.
  - One command in flight, 5 s timeout, `GET_CONTACTS` ends on `END_OF_CONTACTS`.
  - Startup: app start "Foresignal", device query, channels 0–7, contacts, battery, then sync until no more messages.
  - `MSG_WAITING` restarts the sync; `PUSH_CODE_ADVERT` re-queries with `GET_CONTACT_BY_KEY`.
  - A battery poll every 5 min under a 15 min silence timeout.
- **Records:**
  - contacts, the companion itself and live adverts (`lastAdvertAt`; `advertTimestamp` volatile);
  - `advertStatus` ok / late / silent (13 h / 26 h params), re-evaluated every 15 min and emitted only on change;
  - RECEIVED times; private; budget 1/min and 1,440/day.
- **Codec** `meshcore-companion` (`MeshCoreCompanionCodec`): the reassembly plus the mapper.

**6. `meshcore-bridge`** (`MeshCoreBridgeAdapter`, `inbound.formats.mesh.MeshCoreJsonFormat`):
- **The PUSH source** is private. It authenticates with its `ingestKey` credential: `IngestAuthenticator` now
  accepts a built-in PUSH source's declared `ingestKey` on its own format paths (054 did this for media paths only).
  An admin token also works.
- **The format:**
  - accepts `{"events":[…]}` or a single event, with meshcore_py payload keys;
  - maps through the same `MeshCoreEventMapper` and publishes texts to the bus;
  - rejects bad items one by one (`MESHCORE_EVENT_INVALID`, `MESHCORE_EVENT_UNKNOWN`).
- **`contrib/meshcore-bridge/`:** `bridge.py` (MIT; serial, BLE or TCP; only app start, contacts, channel names,
  contact by key and message fetching), `requirements.txt` (`meshcore>=2.3,<3`), README and a systemd unit.

**Docs:**
- `docs/Source-Catalog.md`: the new section "Mesh networks (prompt 083)" with all five rows; the two old rows point
  to it.
- `docs/Operator-Guide.md`: "Mesh networks" (the public-broker rules and why they are fixed, private-broker setup,
  bridge vs TCP firmware, the one-client limit, repeater health).
- `docs/Adapter-Guide.md` §20: guarded commands, `mqtt`/`mqtts` LAN URLs, the codec row, "MQTT sources".
- `THIRD_PARTY_NOTICES.md`: MeshCore (MIT, consulted, commit recorded); Meshtastic (GPL, reference only, no code);
  meshcore_py (bridge dependency, not vendored).
- `docs/OpenAPI.json`: the `meshcore-json` format in the ingest path parameter, the `mesh-node` note, the
  `mesh-message` payload fields and note, `ParamSpec.allowed` (mqtt/mqtts) and a changelog entry. No operation
  changes.
- `docs/Invariants.md`: SRC-1, SRC-7, EGR-1 and PRC-2 test entries.

## Files created / modified

- **Created, main:**
  - `source/adapters/mesh/{package-info, MeshRecords, MeshNodeState, MeshNodeStates, MeshTextMessage, MeshMessageBus,
    MeshtasticPacket, MeshtasticJsonParser, MeshtasticStream, SlidingWindowLimiter, MeshtasticMqttRules,
    MeshMqttSubscriber, MeshtasticMqttAdapter, MeshtasticLocalAdapter, MeshtasticMapParser, MeshtasticMapAdapter,
    RepeaterHealth, MeshCoreEventMapper, MeshCoreCompanionLink, MeshCoreLinks, MeshCoreLocalAdapter,
    MeshCoreBridgeAdapter}`;
  - `source/formats/meshcore/{package-info, MeshCoreProtocol, MeshCoreFrameDecoder, MeshCoreFrames,
    MeshCoreCommandGuard}`;
  - `source/codec/MeshCoreCompanionCodec`; `source/spi/{CommandGuard, CommandHandle}`; `mqtt/MqttSessionOptions`;
  - `inbound/formats/mesh/{package-info, MeshCoreJsonFormat}`.
- **Modified, main:**
  - `source/spi/{SocketOptions, LanEndpoint, SourceDescriptor (coverageFromObservers)}`;
  - `collect/socket/{DefaultSocketTransport, package-info}`; `collect/PolicyAwareRecordSink`;
  - `mqtt/{MqttConnection, MqttConnectionSpec}`;
  - `source/ParamValidator`; `inbound/IngestAuthenticator`; `source/codec/package-info`.
- **Created, test:**
  - `source/adapters/mesh/{MeshFixtures, MeshtasticJsonParserTest, MeshtasticMqttRulesTest, MeshtasticMqttAdapterTest,
    MeshtasticStreamTest, MeshtasticMapParserTest, MeshCoreFrameCodecTest, MeshCoreEventMapperTest,
    MeshCoreCommandGuardTest, RepeaterHealthTest, MeshCoreLinkTest, MeshIngestIT, NoMeshtasticProtobufTest}`;
  - `inbound/formats/mesh/MeshCoreJsonFormatTest`; `source/MqttLanUrlTest`; `collect/StreamCoverageTest`.
- **Modified, test:**
  - `retention/FamilyDefaultsTest` (five `state` entries);
  - `testsupport/stream/SocketStreamContext` (`withCredentials`);
  - `testsupport/weather/WeatherIngestSupport` (the `meshtastic-map` base-URL property).
- **Fixtures** (each with a README):
  - `meshtastic-map/nodes-200.json` (real, anonymized);
  - `meshtastic-mqtt/public-json-sample.tsv` (real, anonymized);
  - `meshtastic-local/private-broker-5min.tsv`, `meshcore-local/{companion-tcp.hex, commands.hex}` and
    `meshcore-bridge/events.json` (synthetic).
- **Other:** `contrib/meshcore-bridge/{bridge.py, requirements.txt, README.md, meshcore-bridge.service}`.
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Adapter-Guide.md`, `docs/OpenAPI.json`,
  `docs/Invariants.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **`meshtastic-map` TTL is 30 h, not 6 h.** `IngestPolicyValidator` requires `keyframe ≤ ttl − poll − 30 s`,
   which a 6 h TTL cannot meet with a 24 h poll. Keyframe 5 h.
2. **Budgets in per-minute and per-day units** (no hourly bucket, as in 082):
   - `meshtastic-mqtt` is 4/min and 100/day, because the CONNECT and up to three SUBSCRIBEs all cost units;
   - "12 connects/hour" is an in-adapter rolling-hour limiter.
3. **The supervisor owns MQTT reconnects.** The prompt asked for a reconnect of 15 s → 300 s; 060's client
   reconnects on its own ladder (1 s → 120 s), unbudgeted. Mesh MQTT sessions therefore set
   `autoReconnect=false`: a drop reaches the supervisor, which uses the descriptor's `ReconnectPolicy`, so every
   CONNECT is paid for. Each start creates a fresh connection.
4. **No `StreamContext.consumeBudget`:** `chargeRequest(label)` (005/069) already is that, as the prompt allows.
5. **`MeshCoreCompanionLink` owns reconnects, not the supervisor**, as prompted. The source handle stays running
   while the link reconnects; the descriptor still sets the same ladder for failed starts.
6. **The link uses `MeshCoreFrameDecoder` directly.** The `meshcore-companion` codec bean wraps the same decoder and
   mapper, because the codec SPI has no way to write commands.
7. **Retention class `state` (365 d) for both kinds.** The prompt names none; `FamilyDefaultsTest` checks one class
   per adapter, so `mesh-message` shares it.
8. **`hwModel` is a string:** the map's `hardware_model_name` and MeshCore's device model, or the Meshtastic model
   number as text when only that is known. `role` is the role name.
9. **Spec reconciliation:** the `mesh-message` payload fields were empty in the spec; they are now
   `network, channelIndex, channelName, fromId, toId, text`. MeshCore channel messages have no `fromId`, because the
   firmware does not report the sender.
10. **`meshtastic-mqtt` logs its rules WARN when the stream starts** (at most hourly), not on the settings change.
11. **A built-in PUSH source's `ingestKey`** is accepted on its own format paths (an `IngestAuthenticator`
    extension), so the bridge needs no admin token.
12. **Without a JSON `timestamp`, Meshtastic records use the receive time.** Under UPSTREAM a missing `observedAt`
    is rejected; the framework clamps future values.
13. **The private-broker, MeshCore TCP and bridge fixtures are synthetic.** No node, broker or companion was
    reachable. The public sample and the map list are real captures, anonymized.

## Issues found / open questions

1. **Descriptor warnings:** all five warn that tile property `batteryLevel` is not significant (the prompt requires
   both the tile property and its volatility).
2. **Public broker, verified 2026-10-14** (two 3-minute captures, one connection each, inside the rules): about
   20 JSON messages a minute on `msh/US`. About a third were third-party variants (string ids, `sendtext`, empty
   type, a topic without a gateway), which are dropped. Retained messages arrived with old timestamps.
3. **The map serves some 32-bit-precision positions** (exact) for nodes that published them publicly. Foresignal
   stores what the map serves.
4. **US English:** the checker on every new and changed file flags only:
   - the `licence` descriptor field (crosses the wire);
   - the map's upstream key `neighbours` (in the fixture and quoted in its README).

   It also flags existing lines this prompt did not write (for example `initialiser` in `SourceDescriptor`).
5. **Instructions in data:** none. The MeshCore protocol doc's security note is addressed to implementers; nothing
   was acted on.
6. `docs/Source-Catalog.html` was not regenerated (as in 070–082).
7. **meshcore_py API:** checked from its source on GitHub (2.3.15: `create_serial/ble/tcp`, `commands.get_contacts`,
   `get_channel`, `get_contact_by_key`, `send_appstart`, `start_auto_message_fetching`, the event names and payload
   keys). Whether `subscribe` is a coroutine differs between versions, so the bridge handles both. Unverified against
   a radio.

**Owner questions (non-blocking):**
1. Accept the 30 h TTL for `meshtastic-map` (Deviation 1) and the 4/min budget with a 12/hour limiter (Deviation 2)?
2. Accept the `state` class (365 d) for `mesh-message` (Deviation 7), or should text have a shorter class
   (`social`, 30 d) on its own?
3. Accept built-in PUSH sources authenticating with their declared `ingestKey` credential (Deviation 11)?

## Tests written (not run)

- **`MeshtasticJsonParserTest`:**
  - each type from the private fixture; unsigned, signed and `!hex` ids; unknown types;
  - `/2/e/` and `/2/map/` never parsed (even with a JSON body); malformed bodies and topics; zeroes;
  - the real public sample's outcome counts (1 topic, 3 unknown, 17 malformed, 1 text, 59 in all).
- **`MeshtasticMqttRulesTest`:**
  - `maxSubscriptions` > 3, 0, or too small for the roots: refused; wildcard and above-root roots refused;
  - client id, keepalive 120 s, clean, QoS 0, no status topic, 3.1.1;
  - the status-topic rules; reconnect bounds 15 s–300 s with jitter; the descriptor;
  - the 101st message in a minute dropped and counted; the sliding window;
  - the 13th connect in an hour refused before any unit is charged; params validated at start.
- **`MeshtasticMqttAdapterTest`** (060's Mosquitto Testcontainer):
  - the public source: exactly its two subscriptions (protobuf, other-root and map topics never arrive), 3 units,
    nodeinfo and position merged into one snapshot with accuracy and observer;
  - the local source: password listener, one subscription per channel, text on the bus, no records without
    `includeText`;
  - a Toxiproxy cut reported through `onClose` and no further CONNECT paid for.
- **`MeshtasticStreamTest`:**
  - the private capture: 11 snapshots of 3 nodes; telemetry keeps the position; lowBattery; observers; 3 bus
    messages;
  - text records with stable ids; the public sample: 37 snapshots of 26 nodes, no bus messages;
  - the fallbacks and the LRU bound.
- **`MeshtasticMapParserTest`:**
  - the trimmed real list: 200 records, 138 positioned, 93 unsigned ids, the 1e-7 scaling, names, string numbers;
  - 10,000 synthetic nodes, with the first record arriving before 5 % of the file was read;
  - skips and the fallback time; format errors; the descriptor and the base-URL override.
- **`MeshCoreFrameCodecTest`:** 25 frames; reads split anywhere; oversize skipped across reads; junk resync; the
  startup commands equal `commands.hex`; the codec session.
- **`MeshCoreEventMapperTest`:**
  - contact decode (×1e-6, NUL-padded name, 0,0 no position); the stream's records (companion, contacts, the
    re-queried repeater as a live advert, the new sensor);
  - a contact-list repeater late; channel names without secrets; messages on the bus and as records only with
    `includeText`;
  - text frame layouts; own frames before self info.
- **`MeshCoreCommandGuardTest`:** of 256 codes only the eight reads pass; never-list codes cannot be registered;
  084's sends can; the envelope; `SocketOptions.commandWriter` rules.
- **`RepeaterHealthTest`:** the thresholds; ok → late → silent emitted once each over 30 h of 15-minute
  re-evaluations; back to ok on a fresh advert; only repeaters.
- **`MeshCoreLinkTest`** (scripted companion on loopback):
  - `meshcore-local` writes exactly `commands.hex` (17 frames, all allowlisted), with one connect, the records and two
    bus messages;
  - off-list commands are refused with no byte written; `allowSend` works;
  - a `CommandHandle` guard refuses before writing, and nothing is written after close; terminal codes; the link
    registry.
- **`MeshCoreJsonFormatTest`:** the bridge fixture gives 6 records, 2 bus messages and 2 item rejections; a single
  event; a non-JSON body is a 400; invalid events rejected one by one.
- **`MeshIngestIT`** (`WeatherIngestSupport`):
  - five sources registered and disabled, three private;
  - `meshtastic-mqtt` PUT limits;
  - a bridge push with the `ingestKey` (a wrong key is 401, an admin token works, no text records);
  - the own-node sources are private (hidden from anonymous readers, never embedded or federated), and
    `meshtastic-local` records ingest;
  - `meshtastic-map` through the real fetch path: 200 versions.
- **Others:**
  - `MqttLanUrlTest`: mqtt and mqtts pinned private; no default port, public host, path or unlisted scheme;
  - `StreamCoverageTest`: distinct receivers;
  - `NoMeshtasticProtobufTest`: no `.proto` file and no Meshtastic protobuf package anywhere.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: valid, 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests, covers all ops: True. The
  collection is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** throwaway `main` classes in the scratchpad, outside the repository, run with Gradle's JDK 21:
  - `DescriptorValidator.problems` and `IngestPolicyValidator.problems` are empty for all five descriptors (only the
    `batteryLevel` tile warning);
  - the map fixture parses to 200 records, 138 positioned, 93 unsigned, 1 low battery;
  - the stream counts used in the assertions (public 37/26 with drops 17/3/1; local 11/3 with 3 messages; 14 with
    text);
  - the MeshCore stream gives 25 frames, 1 oversize, 2 junk, and the repeater re-evaluation gives 0 / 1 / 1;
  - a scripted loopback companion: `meshcore-local` wrote exactly the 17 commands of `commands.hex`, made 8 records
    and published 2 bus messages, with one connect.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **084 (mesh channels):**
   - Subscribe to `MeshMessageBus` (`subscribe` returns an `AutoCloseable`). Messages carry `sourceId`, `network`,
     `channelIndex` (null for a direct message), `channelName`, `fromId` (`!xxxxxxxx`, `mc:` + 12 hex; null for
     MeshCore channel messages, whose text starts `name: `), `toId`, `text`, `receivedAt`, `rssi` and `snr`.
   - Get the companion link with `MeshCoreLinks.link(LanEndpoint)`, then `attach(owner, transport, listener)` and
     `detach(owner)`; the transport comes from `SocketTransportFactory` for a context.
   - Register sends with `link.guard().allowSend(0x02 | 0x03 | 0x3E)` and send with `link.command(payload)`. The
     terminal code is `RESP_SENT` for 0x02 and 0x03, `RESP_OK` otherwise.
   - For a Meshtastic downlink, use `MqttConnections` with a publisher spec (060's defaults) under a key other than
     `source:meshtastic-local`.
2. **085 / 086:**
   - an MQTT source uses `MqttSessionOptions.subscriber(keepAlive)` through `MeshMqttSubscriber`'s pattern (copy it;
     adapters do not import each other);
   - LAN brokers are `ParamSpec.lanUrl(name, …, "mqtt", "mqtts")` and `LanEndpoint.fromParams`;
   - `coverageFromObservers` is available to any stream.
3. **097 checkpoint:**
   - first run of every 083 test;
   - `MeshtasticMqttAdapterTest` uses 060's `MosquittoSupport` (Docker `eclipse-mosquitto:2` and Toxiproxy) and
     real time (Awaitility, a 3 s quiet wait);
   - `MeshCoreLinkTest` binds a loopback server socket (tests only);
   - `MeshIngestIT` uses `WeatherIngestSupport` (new property `foresignal.test.adapters.mesh.base-url`);
   - check the 401 for a wrong ingest key on a built-in PUSH source, and that `RejectionView` reasons appear in the
     push response body.
4. **109:** license strings:
   - "unspecified (Meshtastic community broker)";
   - "unspecified (community service)" (the map);
   - "Operator's own data (no upstream license)" (the three own-node sources).
5. **110 (serve sinks):** `mesh-message` is never embedded; the three own-node sources are private.
6. **143:**
   - recapture per the fixture READMEs: a real private-broker capture, a MeshCore TCP session, a bridge body, the map
     (at most daily), the public sample (once, a few minutes);
   - test the bridge against a real companion (USB and BLE) and `meshcore-local` against the WiFi firmware;
   - confirm the companion's response to `CMD_DEVICE_QUERY` 3 on current firmware.
7. **144 (Flyway):** no schema change. Adapter state: `meshtastic-map` keeps `etag:meshtastic-map` and
   `lastmod:meshtastic-map`; the streams keep only in-memory state.
8. **Owner decisions pending:** the three questions above, plus those carried from 012–082.
