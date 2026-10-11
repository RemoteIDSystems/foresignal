# 086 — Local RF Decoders

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/086-CR-Local-RF-Decoders.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–133).
- **Previous Completion Report:** read `completion-reports/085-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §4a rows "ACARS / VDL2 / HFDL", "radiosonde_auto_rx", "Drone Remote ID", "GNSS integrity sentinel", "Raspberry Shake, lightning, NOAA SAME…", §9 (DJI DroneID rejected), §8.C item 45 ("048a" = this prompt)
- `docs/Core-Data-Model.md` §2, §3 (adapter rules, limits), §5.1/§5.3, §14 (observer)
- Completion Reports of 024 (IngestFormat SPI), 039 (private enforcement), 040 (US county gazetteer), 059 (RecordMapping presets), 060 (MQTT client), 069 (socket transport, UDP listener with `allowSenders`, codec SPI, `local-gpsd-{n}`, egress), 082 (`SocketOptions.receiveOnly()`), 083 (`LAN_URL mqtt://`), 077 (`local-sonde`, SondeHub payload contract)
- Upstream: https://github.com/sdr-enthusiasts/acars_router (MIT), https://github.com/projecthorus/radiosonde_auto_rx/wiki, https://gpsd.gitlab.io/gpsd/gpsd_json.html, https://github.com/alphafox02/DragonSync (Apache-2.0; `docs/mqtt-schema.md`), https://raspishake.github.io/rsudp/, https://www.weather.gov/nwr/sameinfo, multimon-ng EAS decoder

## Objective

Bring the operator's own SDR decoders in through the 069 socket transport and the 024/059 push paths: aviation datalink metadata, radiosondes, a GNSS integrity sentinel, drone Remote ID, Raspberry Shake triggers and NOAA Weather Radio SAME headers. Several of these carry legal or privacy constraints, which are enforced in code rather than left to configuration.

## Tasks

All socket sources here use 069's transport (`ctx.sockets()`), its codec SPI and its slot-family pattern (`foresignal.local-receivers.slots`, disabled by default, `egress` LOCAL_ONLY), and connect with `SocketOptions.receiveOnly()` (082) unless stated otherwise.

### 1. `local-acars-{n}` (STREAMING, metadata only)

- Params: `acarsEndpoint`, `vdlm2Endpoint`, `hfdlEndpoint` (each optional `LAN_URL tcp://`; at least one required), `stationId` (Observer). Codec `acars-json` (`Framing.LINES`). acars_router's TCP server defaults are 15550 (ACARS), 15555 (VDLM2), 15556 (HFDL) (**verified 2026-10**, README), but no port is assumed: the operator supplies it. One connection per configured endpoint; JSON objects per line (verify framing at fixture capture; fall back to a streaming JSON object splitter).
- **`AcarsMetadataMapper`** (pure) for the three shapes: acarsdec (`timestamp, station_id, channel, freq, level, error, mode, label, block_id, ack, tail, flight, msgno, text`), dumpvdl2 (`vdl2.t`, `vdl2.freq`, `vdl2.sig_level`, `vdl2.avlc.src.addr`, `vdl2.avlc.acars.{reg,flight,label,msg_num,msg_text}`) and dumphfdl (`hfdl.t`, `hfdl.freq`, `hfdl.sig_level`, `hfdl.lpdu.src`, `hfdl.lpdu.hfnpdu.acars…`, `hfdl.lpdu.hfnpdu.pos`).
- Payload allowlist: `protocol, tail, flight, label, mode, blockId, msgNum, freqMhz, signalDbm, icaoHex, groundStation, hasText`. **Message text is never stored, logged, embedded or forwarded**; there is no parameter to enable it. Interception-disclosure laws (US 18 U.S.C. §2511, UK Wireless Telegraphy Act 2006 s.48 and equivalents) make content redistribution unlawful, so the mapper drops `text`/`msg_text` before the record is built and tests assert no text-bearing key survives.
- Geometry only from decoded position reports (HFDL `hfnpdu.pos`), else null.
- Record: kind `acars-message`, externalId = `protocol|station|epochMillis|tail|msgNum`, `eventTime = observedAt` = decoder timestamp, STANDARD, ALL, `defaultRetention` 7 d, no TTL, no `embedFields`.
- **Private by descriptor:** `private=true` on the descriptor. Under 039 effective privacy is descriptor OR settings, so an admin cannot make it public. Private sources are excluded from `openReads`, tiles, embeddings, LLM egress and sinks (039); `egressOverridable=false` as well. Add a test that MCP and Ask tools cannot read this kind.

### 2. radiosonde_auto_rx

- First read 077's Completion Report. If `local-sonde` already consumes auto_rx `PAYLOAD_SUMMARY` UDP, extend it only with the missing items below; otherwise add the UDP path to it (do not create a second sonde source with a different payload contract).
- Param `endpoint` (`LAN_URL udp://bindHost:port`; auto_rx's conventional broadcast port is 55673, not assumed) with 069's required `allowSenders`; codec `sonde-udp` on `ctx.sockets().udp(…)`. Parse `type=PAYLOAD_SUMMARY` JSON (`callsign, latitude, longitude, altitude, speed, heading, time, comment, model, freq, temp, humidity, frame, sdr_device_idx`), ignore other types.
- Map onto 077's SondeHub payload keys and descriptor (tier, policy and TTL as 077 set them). Observer `receiverId` = station callsign param, `frequencyHz` from `freq`.

### 3. GNSS integrity sentinel (extends 069's `local-gpsd-{n}`)

069 already ships `local-gpsd-{n}` (codec `gpsd`, kind `gnss-fix`, private, `navDegraded`) and says this prompt builds the sentinel on it. Do not add a second gpsd source: extend that codec session and source.

- New params on `local-gpsd-{n}`: `fixedStation` (boolean, default true), `ubxMonitor` (boolean, default false), `stationLabel` (string, used in messages).
- The `onConnect` writer stays 069's `?WATCH={"enable":true,"json":true};` (plus `"raw":2` when `ubxMonitor=true`). Add a **`GpsdCommandGuard`** to that writer: only `?WATCH`, `?VERSION`, `?DEVICES` and `?POLL` may ever be written. UBX messages are decoded from raw passthrough but never sent (no CFG writes).
- Consume `TPV` (`mode, time, lat, lon, altHAE, eph, epx, epy`), `SKY` (`nSat, uSat, hdop`, satellite `ss` for mean C/N0 of used satellites), `PPS`/`TOFF` (offset ns) and, with `ubxMonitor`, UBX-MON-HW `jamInd` and UBX-MON-RF `jammingState`.
- **`GnssIntegrityEvaluator`** (pure, 30 s windows, 15-min rolling baseline): `integrityState` = `LOST` (mode < 2 for > 60 s), `JAMMED` (receiver jamming state warning/critical, or mean C/N0 drop ≥ 10 dB with uSat drop ≥ 50 %), `SUSPECT_SPOOF` (fixed station: position jump > 500 m or clock offset step > 1 s while C/N0 rises), `DEGRADED` (eph > 3× baseline or uSat < 4), else `OK`.
- Second kind on the same source: `gnss-integrity`, externalId = device, `observedAt` = TPV `time` (same UPSTREAM time base as `gnss-fix`), payload `integrityState` (significant), `mode, uSat, nSat, hdop, eph, cn0MeanDb, jamIndicator, clockOffsetNs` (volatile), geometry = the fix rounded to 2 decimals (the source stays private, as 069 set it). STANDARD; `kindRetention{gnss-integrity: 30d}`. If the 069 descriptor's LATEST policy needs `significantFields` per kind, add `integrityState` to the list.

### 4. `local-remoteid` (STREAMING, DragonSync MQTT)

- Params: `brokerUrl` (`LAN_URL mqtt://`/`mqtts://`, 083's extension), `topics` (string-list, default `wardragon/#`; verify topic names against DragonSync `docs/mqtt-schema.md` at fixture capture and update the default), credentials optional. Connect through 060's `MqttConnections`; disabled by default, `egress` LOCAL_ONLY.
- **`RemoteIdMapper`**: drone id (serial or session id), position, `alt`, `agl`, `speed`, `vspeed`, `course`, `uaType`, `idType`, `operatorId`, `rssi`, `freqMhz`, `seen_by`, `observed_at`. `observedAt` = DragonSync `observed_at` (kit time); `rid_timestamp` is not UTC and goes nowhere.
- **Drop DJI DroneID** detections (proprietary, not ASTM F3411/ASD-STAN Remote ID; Gap §9). Identify them by the protocol or source field documented in `mqtt-schema.md` (record which field in the fixture README); count `foresignal.remoteid.dropped{reason=dji}`.
- **Strip pilot and home positions** (`pilot_lat/lon`, `home_lat/lon`, operator location) before the record is built. Param `keepOperatorPosition` (default false) is accepted only when the source's effective privacy (039) is on (400 `PRIVATE_REQUIRED` otherwise) and then stores them as payload `operatorPoint` (never geometry).
- Record kind `drone`, externalId `rid:` + id, TRACK, SAMPLED (minInterval 5 s, keyframe 60 s, TTL 3 min, deviation 25 m, edgeMinInterval 2 s), significant `idType, uaType, operatorId`, `motion {speedField: speed, speedUnit: m/s, courseField: course}`, Observer = `seen_by` + rssi + freq.

### 5. Push presets: Raspberry Shake and NOAA SAME

- **rsudp:** 059 mapping preset `rsudp-alarm` for rsudp's `custom` module (a documented script that POSTs `{"event":"ALARM"|"RESET","time":…,"station":…,"channel":…}` to `/v1/ingest/{id}/mapping`). Kind `seismic-trigger`, geometry = the custom source's configured station point, `eventTime = time`. Ship the script under `contrib/rsudp/`.
- **SAME:** IngestFormat `same` on 024's SPI (`POST /v1/ingest/{id}/same`, `text/plain`). Accept multimon-ng (`EAS: ZCZC-…`) and bare header lines; ignore `NNNN`. Parse `ZCZC-ORG-EEE-PSSCCC(-PSSCCC)*+TTTT-JJJHHMM-LLLLLLLL-`: event code, part-of-county digit, state and county FIPS (`000` = whole state), purge `TTTT`, issue time (Julian day + HHMM UTC, year from the receive clock with rollover handling), sender. Collapse the three header repetitions (identical header within 10 s). Record kind `same-alert`, externalId `sender|issue|event`, `eventTime = issue`, `validUntil = issue + purge` (upstream-asserted), geometry = union of 040 county polygons (state polygon for `000`), payload `eventCode, eventName, org, locations, sender, rawHeader`. Bundle the NWS SAME event-code table (public domain) as a resource. Ship a `contrib/same/` recipe (`rtl_fm … | multimon-ng -a EAS -t raw - | foresignal-post`).

### 6. Fixtures

Capture from the operator's own decoders (or the upstream projects' published samples): one JSON line per acarsdec/dumpvdl2/dumphfdl shape, ten auto_rx summaries, a 5-minute gpsd session including a fix loss, a DragonSync MQTT session including one DJI detection, and three SAME headers (including a `000` county and a purge rollover). Document each capture in a `README.md` beside it; scrub tail numbers and callsigns that are not public.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`: add `same` to the IngestFormat enum on `/v1/ingest/{sourceId}/{format}` and error code `PRIVATE_REQUIRED`; kinds `acars-message, gnss-integrity, drone, seismic-trigger, same-alert` where kinds are listed. Regenerate Postman (add a `same` example body).
- `docs/Source-Catalog.md`: rows for `local-acars-*`, `local-remoteid`, the `local-gpsd-*` integrity kind, and the presets. `docs/Operator-Guide.md` "Local RF decoders": acars_router, auto_rx, gpsd, DragonSync, rsudp and SAME setup; the legal note on datalink content; the Remote ID privacy defaults.

## Tests to write (do not run)

- `AcarsMetadataMapperTest`: each shape; assert no key named or containing `text` survives; HFDL position mapped.
- `LocalAcarsPrivacyIT`: anonymous `openReads` read, tile and MCP `search` return nothing for `acars-message`; settings cannot make the source public (039 rule).
- `AutoRxSummaryParserTest`; `GpsdCommandGuardTest` (only allowlisted commands written; extends 069's `GpsdCodecTest` session); `GnssIntegrityEvaluatorTest` (each state from scripted windows); `UbxMonParserTest`.
- `RemoteIdMapperTest`: DJI dropped, pilot/home stripped, `keepOperatorPosition` rejected on a non-private source.
- `SameHeaderParserTest`: multi-county, `000`, repetition collapse, Julian rollover at year end, `NNNN` ignored; `SameIngestIT` (county geometry via 040, `validUntil` set).
- `LocalRfAdaptersTest`: embedded TCP/UDP/MQTT servers for each streaming source, including reconnect.

## Acceptance criteria

- ACARS/VDL2/HFDL content never reaches storage, logs, embeddings, MCP or sinks.
- Remote ID records never carry pilot or home positions on non-private sources; DJI DroneID is never ingested.
- The gpsd connection writes only allowlisted commands.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `086: Local RF Decoders`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/086-CR-Local-RF-Decoders.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `086: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
