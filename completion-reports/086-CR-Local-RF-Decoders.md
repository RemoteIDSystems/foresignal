# 086-CR-Local-RF-Decoders

| | |
|---|---|
| Prompt | 086-Local-RF-Decoders.md |
| Status | Complete |
| Implementation commit | b729a08a7f384031ed90b043a00b943e40f0449a |
| Date | 2026-10-13 |

## Summary

The operator's own SDR decoders now come in through prompt 069's socket transport, 060's MQTT client and the 024/059
push paths. The legal and privacy rules are enforced in code; no parameter turns them off.

**1. `local-acars-{n}`** (`adapters.acars.LocalAcarsAdapter`, `LocalAcarsFamily`; codec `acars-json`
`AcarsJsonCodec`; `formats.acars.AcarsMetadataMapper`, `JsonObjectStream`):
- **Params:** `acarsEndpoint`, `vdlm2Endpoint`, `hfdlEndpoint` (each an optional `tcp://` `LAN_URL`, at least one,
  no port assumed; acars_router's defaults 15550/15555/15556 are documented, verified in its README 2026-10-13) and
  `stationId` (the observer).
- **Connections:** one receive-only connection per endpoint (`SocketOptions.receiveOnly()`, one budget unit each). A
  composite handle closes all of them when any drops, so the supervisor reconnects them together.
- **Mapper:** acarsdec, dumpvdl2 and dumphfdl shapes. The payload is built only from the allowlist `protocol, tail,
  flight, label, mode, blockId, msgNum, freqMhz, signalDbm, icaoHex, groundStation, hasText`. `text`/`msg_text` are
  only tested for presence and never copied, logged or stored.
  - Geometry only from HFDL `hfnpdu.pos`.
  - externalId `protocol|station|epochMillis|tail|msgNum`; `eventTime = observedAt` = the decoder time.
  - Observer: the station, with the receive frequency in Hz and the signal level.
- **Descriptor:** STANDARD, ALL, 7 d, no TTL, no `embedFields`. `privateSource(true)`, egress `LOCAL_ONLY`,
  `egressOverridable(false)`. Slots on `foresignal.local-receivers.slots`, disabled.

**2. radiosonde_auto_rx:** 077's `local-sonde` already consumed `PAYLOAD_SUMMARY` UDP through codec `sonde-udp`, so it
was extended rather than duplicated:
- param `stationCallsign` names the observer (otherwise auto_rx's `station`);
- the observer carries `freq` as `frequencyHz` (new `RadiosondeRecords.record(Observation, Long)`);
- a summary without `datetime` falls back to `time` (`HH:MM:SS` UTC on the nearest receive day);
- `comment`, `frame` and `sdr_device_idx` are not stored. Tier, policy and TTL are 077's.

**3. GNSS integrity sentinel** on 069's `local-gpsd-{n}` (no second gpsd source):
- **Params:** `fixedStation` (default true), `ubxMonitor` (default false), `stationLabel`.
- **Writes:** `GpsdCodec.watchCommand(raw)` builds `?WATCH={"enable":true,"json":true};` (plus `"raw":2` with
  `ubxMonitor`), and every write passes `GpsdCommandGuard`. The guard admits only `?WATCH` (optionally with a JSON
  object that cannot contain `;` or a line break), `?VERSION`, `?DEVICES` and `?POLL`. The connection uses
  `receiveOnlyAfterConnect().withOnConnect(...)`, so nothing else can be written. UBX is decoded, never sent.
- **Inputs:** `TPV`, `SKY` (mean `ss` of used satellites), `PPS`/`TOFF` (`clock − real` in ns). With `ubxMonitor` the
  stream is read RAW and split by `GpsdRawSplitter` into JSON lines and checksum-verified UBX frames;
  `UbxMonParser` reads MON-HW `jamInd`/`jammingState` and the worst MON-RF block.
- **`GnssIntegrityEvaluator`** (`formats.gnss`, pure): 30 s windows on the TPV time base and a 15-minute baseline built
  only from OK windows (at least 4 before baseline rules apply). States LOST > JAMMED > SUSPECT_SPOOF > DEGRADED > OK,
  with the prompt's thresholds.
- **Records:** kind `gnss-integrity`, one per device per closed window. externalId = device, `observedAt` = the
  window's last TPV time. Payload `integrityState, mode, uSat, nSat, hdop, eph, cn0MeanDb, jamIndicator,
  clockOffsetNs`; geometry = the last fix rounded to 2 decimals; `quality.extra["gnss.integrity_reason"]`.
- **Descriptor:** `integrityState` added to `significantFields`; `kindRetention{gnss-integrity: 30d}`. A state change is
  logged once at INFO with `stationLabel`.

**4. `local-remoteid`** (`adapters.remoteid.LocalRemoteIdAdapter`, `RemoteIdMapper`, `RemoteIdMqttSubscriber`):
- **Connection:** `brokerUrl` (`mqtt://`/`mqtts://` `LAN_URL`) and `topics` (default `wardragon/drones`, verified
  against DragonSync `docs/mqtt-schema.md`), optional `username`/`password`. Through `MqttConnections` with a
  non-reconnecting subscriber session; one unit per CONNECT and per SUBSCRIBE.
- **Topics:** only `<root>/drones` and `<root>/drone/<id>` are parsed; `pilot_attrs`, `home_attrs`, availability,
  aircraft and system topics are dropped by name.
- **DJI DroneID dropped:** an empty `transport` (DragonSync: "Empty for OcuSync"), or a `DJI…` description on a link
  that is not Wi-Fi/Bluetooth Remote ID. Counted in `foresignal.remoteid.dropped{reason=dji}`; other links are
  `not-remote-id`.
- **Pilot and home positions** are never read unless `keepOperatorPosition`, and then only the pilot position becomes
  payload `operatorPoint`.
- **`keepOperatorPosition`** is refused on a source that is not effectively private: new
  `SourceAdapter.paramsRequiringPrivacy`, enforced in `SourcesController.updateSource` with 400 `PRIVATE_REQUIRED`
  (new `ErrorCodes.PRIVATE_REQUIRED`), also when a request would make such a source public.
- **Record:** kind `drone`, `rid:<id>`, TRACK, SAMPLED {5 s, 60 s, 25 m, edge 2 s}, TTL 3 min, significant
  `idType, uaType, operatorId`, motion `speed` m/s / `course`, observer `seen_by` + RSSI + frequency, `observedAt` =
  `observed_at`.

**5. Push presets:**
- **`rsudp-alarm`** (059 preset, format `mapping`, location mode `const`, kind `seismic-trigger`): externalId
  `station.channel`, `observedAt = eventTime = time`. Script `contrib/rsudp/foresignal_alarm.py` (standard library,
  MIT) with a README.
- **`same`** (`inbound.formats.same.SameIngestFormat`, `SameHeaderParser`, `SameEventCodes`; `text/plain`):
  - input: multimon-ng `EAS:` lines or bare headers; `NNNN` ignored; an identical header within 10 s collapses, in one
    request or across requests;
  - issue year from the receive clock: the candidate year nearest the receive time;
  - record: externalId `sender|issue|event`, `validUntil` = issue + purge, geometry = 040 county union plus whole-state
    unions for `PSS000` (new `CountyGeometries.stateUnion`, implemented in `CountyUnionService`);
  - event-code table `same/event-codes.json` (NWS table + 47 CFR 11.31 national codes, public domain);
  - recipe `contrib/same/` (`receive-same.sh`, `foresignal-post`, README).

**6. Fixtures** (`src/test/resources/fixtures/local-rf/`, synthetic, README with recapture commands):
- one acarsdec, dumpvdl2 and dumphfdl line each;
- ten auto_rx summaries and one `LOG` datagram;
- a 5-minute gpsd session with a 90 s fix loss;
- a DragonSync session with one DJI detection and pilot/home topics;
- SAME headers with a `000` county and a purge rolling into the next year;
- `fixtures/mapping/rsudp-alarm/`.

**7. Spec and docs:**
- **OpenAPI:** `same` in the format parameter, the operation description and a `text/plain` example; `PRIVATE_REQUIRED`
  in the Problem codes and the `updateSource` description; built notes on the five kinds and on `radiosonde`;
  changelog entry.
- **Postman:** regenerated with a `same` variant.
- **Source Catalog:** section "Local RF decoders (prompt 086)", and the gpsd and local-sonde rows.
- **Operator Guide:** "Local RF decoders" (acars_router, auto_rx, gpsd, DragonSync, rsudp, SAME, the datalink legal note,
  the Remote ID privacy defaults) and an `rsudp-alarm` preset entry.
- **Adapter Guide:** codec table and "Parameters that need privacy". **Invariants:** SRC-1, EGR-1 and EGR-2 test names.

## Files created / modified

- **Created, main:**
  - `source/adapters/acars/`: `package-info`, `LocalAcarsAdapter`, `LocalAcarsFamily`;
  - `source/adapters/remoteid/`: `package-info`, `LocalRemoteIdAdapter`, `RemoteIdMapper`, `RemoteIdMqttSubscriber`;
  - `source/codec/`: `AcarsJsonCodec`, `GpsdCommandGuard`, `GpsdRawSplitter`, `UbxMonParser`;
  - `source/formats/acars/`: `package-info`, `AcarsMetadataMapper`, `JsonObjectStream`;
  - `source/formats/gnss/`: `package-info`, `GnssIntegrityEvaluator`;
  - `inbound/formats/same/`: `package-info`, `SameHeaderParser`, `SameEventCodes`, `SameIngestFormat`;
  - resources `mapping/presets/rsudp-alarm.json`, `same/event-codes.json`.
- **Modified, main:**
  - `source/codec/GpsdCodec`, `AutoRxPayloadCodec`, `package-info`;
  - `source/adapters/local/LocalGpsdAdapter`; `source/adapters/space/localsonde/LocalSondeAdapter`;
  - `source/formats/sonde/RadiosondeRecords`;
  - `source/spi/SourceAdapter` (`paramsRequiringPrivacy`), `source/spi/CountyGeometries` (`stateUnion`);
  - `reference/CountyUnionService`; `api/SourcesController`; `api/error/ErrorCodes`.
- **Created, contrib:** `contrib/rsudp/{README.md, foresignal_alarm.py}`,
  `contrib/same/{README.md, foresignal-post, receive-same.sh}`.
- **Created, test:**
  - `source/formats/acars/AcarsMetadataMapperTest`, `source/adapters/acars/LocalAcarsPrivacyIT`;
  - `source/codec/{AutoRxSummaryParserTest, GpsdCommandGuardTest, UbxMonParserTest, GpsdRawSplitterTest}`;
  - `source/formats/gnss/GnssIntegrityEvaluatorTest`;
  - `source/adapters/remoteid/RemoteIdMapperTest`;
  - `inbound/formats/same/{SameHeaderParserTest, SameIngestIT}`;
  - `mapping/preset/RsudpAlarmPresetContractTest`;
  - `source/adapters/LocalRfAdaptersTest`;
  - fixtures `local-rf/*`, `mapping/rsudp-alarm/*`.
- **Modified, test:** `GpsdCodecTest` (three integrity tests), `LocalReceiversFamilyTest`, `FamilyDefaultsTest`,
  `PresetCatalogTest`.
- **Docs and scripts:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`,
  `docs/Adapter-Guide.md`, `docs/Invariants.md`, `scripts/generate-postman.py`,
  `postman/Foresignal.postman_collection.json`.

## Deviations from the prompt

1. **`acars-json` is framed RAW with a JSON object splitter**, not `Framing.LINES` with a fallback.
   - acars_router's README documents the server ports but not the framing.
   - The splitter accepts newline-ended and back-to-back objects alike, so no fallback switch is needed.
2. **radiosonde: no new codec or listener.** 077's `local-sonde` already parsed `PAYLOAD_SUMMARY` through `sonde-udp`
   (UDP, required `allowSenders`). Only the missing items were added: the `stationCallsign` observer, observer
   `frequencyHz` and the `time` fallback. The param is named `stationCallsign`, not just "station callsign".
3. **`gnss-integrity` volatile fields are only `cn0MeanDb, jamIndicator, clockOffsetNs`.**
   - `mode` is significant for `gnss-fix` on the same descriptor, and `DescriptorValidator` refuses a field that is
     both volatile and significant (field lists are per source).
   - `uSat, nSat, hdop, eph` are left non-volatile as `gnss-fix` already has them; under LATEST they create no
     versions on their own.
4. **Integrity records are emitted when a window closes** (the first TPV past its 30 s end), not per TPV. A receiver
   that stops sending TPVs entirely produces no further window. gpsd keeps sending mode-1 TPVs without a fix, which is
   what LOST detects.
5. **UBX frames are attributed to the device that last sent a TPV.** gpsd's raw output does not name the device.
6. **The `ubxMonitor` behavior with `"raw":2` alongside JSON is unverified.** No u-blox receiver was available; the
   MON-HW and MON-RF offsets are from the u-blox interface description.
7. **`local-remoteid` default topic is `wardragon/drones`**, not `wardragon/#`. DragonSync publishes the same drone
   JSON on the aggregate and the per-drone topics plus pilot/home/availability topics; the aggregate carries `seen_by`.
   `wardragon/#` still works: non-drone topics are dropped by name.
8. **DJI identification field.** DragonSync's schema has no explicit protocol field. DJI is recognized by an empty
   `transport` or a `DJI…` description on a non-RID link (recorded in the fixture README). A DJI drone that broadcasts
   real Wi-Fi/Bluetooth Remote ID is kept. Other non-RID links (SiK, mLRS, FPV) are dropped as `not-remote-id`.
9. **Remote ID messages without `observed_at` are dropped** (`untimed`) rather than stamped with the receive time.
   `uaType` is the numeric ASTM category (`ua_type`). Altitudes outside −999…20,000 m are left out (RID's −1000
   "unknown" and DJI's 65531).
10. **`PRIVATE_REQUIRED` is enforced at `PUT /v1/sources/{id}`** through a new `SourceAdapter.paramsRequiringPrivacy`
    hook. Adapters cannot see effective privacy at stream start, so the API is the gate. Making the source public
    while the param is stored is refused too.
11. **ACARS retention class.** 7 d is shorter than every analytic class. `FamilyDefaultsTest` gained a `privacy`
    class (private source, exactly the prompt's window).
12. **`gnss-integrity` 30 d.** Kept below the `state` class (365 d): the Conventions override applies only to `event`
    and `series`. `FamilyDefaultsTest` lists it as a prompt-named shorter kind.
13. **SAME:**
    - a whole-state location uses the union of the state's counties in `ref_admin2` (new `stateUnion`), since no state
      polygon table exists;
    - the `same` format is a built-in format with no preset: the operator creates a custom source with the
      `same-alert` kind;
    - items that are not headers are rejected `INVALID_ITEM`; `NNNN` is not.
14. **`rsudp-alarm` posts only ALARM by itself.** rsudp's custom module (GPL-3.0, read, not copied) `exec()`s the code
    file only on ALARM and passes neither the event nor its time.
    - The script posts the current UTC time and reads the station and channel from rsudp's settings file.
    - RESET is posted with `foresignal_alarm.py RESET` from the operator's own wrapper.
15. **Fixtures are synthetic.** No acars_router, auto_rx, gpsd receiver, WarDragon kit or NWR receiver was available;
    recapture commands are in the fixture READMEs. The tail numbers, callsigns, serials and kit ids are fictitious.

## Issues found / open questions

1. **Owner question:** should `gnss-integrity` (30 d) and `acars-message` (7 d) get named classes in the Conventions
   retention table? `FamilyDefaultsTest` carries them as prompt-named exceptions.
2. **Owner question:** is dropping non-RID drone links (SiK, mLRS, FPV detections DragonSync also publishes on drone
   topics) right, or should they become their own kind later?
3. **Unverified at fixture capture (143):**
   - acars_router's TCP framing;
   - DragonSync's DJI marking on current releases;
   - gpsd's `"raw":2` stream together with JSON on a u-blox receiver;
   - rsudp's custom-module environment;
   - the multimon-ng EAS output format.
4. **US English:** nothing was left unchanged in new text. The EGR-2 row's existing word "standardised" was not
   touched (it predates this prompt). The descriptor builder method `licence(...)` is an existing API.
5. **Instructions in data:** none. The acars_router README, the DragonSync schema, the gpsd JSON reference, the rsudp
   settings page and `c_custom.py`, the NWS event-code page and 47 CFR 11.31 were read; none addressed an agent.

## Tests written (not run)

- **`AcarsMetadataMapperTest`:**
  - each shape;
  - no key named or containing `text` (besides `hasText`) and no message text anywhere in the record;
  - HFDL position mapped; edges; the object splitter.
- **`LocalAcarsPrivacyIT`:**
  - the descriptor and every egress path (sink, federation, share, embedding, remote LLM exclusion for the Ask tools);
  - under `openReads`, anonymous list, direct read (404), kinds, tiles (404) and `tiles.json` show nothing;
  - MCP `query_records` (anonymous) and `search` (agent token without the private grant) return nothing;
  - `private:false` refused; egress `OPEN` refused `EGRESS_FIXED`.
- **`AutoRxSummaryParserTest`:** ten summaries to 077 keys, the `LOG` ignored, the station callsign observer with
  `frequencyHz`, `time` fallback and midnight.
- **`GpsdCommandGuardTest`:** allowlisted commands pass; device configuration, smuggled commands, binary and missing
  terminators refused; the codec's writes pass.
- **`GpsdCodecTest` (extended):** the 5-minute session gives OK×5, DEGRADED×2, LOST, OK; MON-HW jamming → JAMMED; TOFF
  offset and C/N0 of used satellites.
- **`GnssIntegrityEvaluatorTest`:** each state from scripted windows, precedence, baseline trimming, window summary.
- **`UbxMonParserTest`**, **`GpsdRawSplitterTest`**.
- **`RemoteIdMapperTest`:** the session (4 records, DJI dropped and counted, 4 topics dropped), pilot/home never stored,
  `operatorPoint` with `keepOperatorPosition`, the privacy hook, the drop rules.
- **`SameHeaderParserTest`:** multi-county, `000`, repetition collapse within and across requests, Julian rollover
  both ways and leap day 366, `NNNN` ignored, rejections.
- **`SameIngestIT`:** a custom source through `POST /v1/ingest/{id}/same` with 040's county fixture; three records,
  county geometry and `validUntil`.
- **`RsudpAlarmPresetContractTest`:** the golden record.
- **`LocalRfAdaptersTest`:**
  - acars: three loopback TCP servers, back-to-back and split objects, nothing written, reconnect;
  - acars: params and a failed connect closing the others; the family bounds;
  - the descriptors pass `DescriptorValidator` and `IngestPolicyValidator`;
  - gpsd: plain and `ubxMonitor`, exactly the guarded `?WATCH` written, RAW framing, JAMMED from UBX;
  - local-sonde: over loopback UDP;
  - local-remoteid: against 060's Mosquitto (two units, DJI and topics dropped, a cut connection reported without
    reconnecting).
- **Updated:** `LocalReceiversFamilyTest`, `FamilyDefaultsTest`, `PresetCatalogTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (only the existing deprecation and unchecked notes).
- `./gradlew lintOpenApi`: "Your API description is valid", 71 warnings (unchanged).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "835 requests across 80 tag folders … covers all
  ops: True" (one new `same` variant).
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.
- **Scratch checks** (throwaway `main` classes in the scratchpad, outside the repository, with Gradle's JDK 21; no
  project tests were run):
  - the gpsd fixture gives 600 fixes and the nine expected integrity states;
  - the three ACARS shapes map with no text;
  - the DragonSync session gives 4 records, 1 DJI drop and 4 topic drops;
  - the SAME fixture gives 3 records (7 items, no rejections);
  - auto_rx gives 10 records with the callsign observer;
  - the raw splitter separates JSON and UBX across 7-byte chunks;
  - all four descriptors have no `DescriptorValidator` or `IngestPolicyValidator` problems;
  - the `rsudp-alarm` mapping compiles without warnings.

## Notes for the next prompt

1. **087 onward:** `SourceAdapter.paramsRequiringPrivacy` and 400 `PRIVATE_REQUIRED` are available for any parameter
   that needs a private source. `CountyGeometries.stateUnion` gives a whole-state outline from `ref_admin2`.
2. **097 checkpoint:**
   - first run of every 086 test;
   - `LocalRfAdaptersTest.remoteIdMqtt` needs Docker (`MosquittoSupport`, including Toxiproxy `cut()`), and the other
     cases bind loopback TCP/UDP sockets;
   - `LocalAcarsPrivacyIT` toggles `openReads` through the API and restores it;
   - `SameIngestIT` loads the `ne-admin2-us` reference fixture;
   - `GpsdCodecTest.integritySession` expects exact states from the 5-minute fixture.
3. **110 (sinks):** `acars-message` is private and `LOCAL_ONLY` (not overridable) and must never reach a sink;
   `local-remoteid` is `LOCAL_ONLY` (overridable); `drone` records never carry pilot or home positions except
   `operatorPoint` on a private source.
4. **143:** recapture per `fixtures/local-rf/README.md`. Verify:
   - acars_router's framing;
   - DragonSync's DJI marking and topics;
   - gpsd `"raw":2` with a u-blox receiver;
   - rsudp's custom module with a real Shake;
   - a real NWR receiver.
5. **144 (Flyway):** no schema change. The new sources keep only in-memory state; the SAME repetition cache is in
   memory.
6. **Owner decisions pending:** open questions 1 and 2, plus those carried from 012–085.
