# 069 — Local Receivers

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/069-CR-Local-Receivers.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/068-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/design-history/Gap-Analysis.md` §1 item 9, §4a rows (socket transport + codec SPI, readsb v2, station health, weather stations, GNSS sentinel), §8.C item 32
- `docs/design-history/Core-Data-Model.md` §2 (readsb row: `now − seen`, `lastPosition`), §3 (rules 7 and 11; `Observer`; carry-forward), §5.3 (`local-adsb-*` row), §14 (observer gating)
- `docs/Source-Catalog.md` row `local-adsb-*` (the LAN exception to the static-feed-list rule)
- `docs/design-history/Weather-Imagery-Design.md` §2.2 (`LAN_URL` row: pinned IP, original Host, no redirects)
- Completion Reports of 004 (`LAN_URL`), 005 (pinned connect), 010 (streaming supervisor and ladder), 039 (`private` flag), 062 (`formats.readsb` mapper, `egress` field, `EgressPolicy`), 063 (streaming patterns)
- GEV (MIT, portable): `server/providers/local-receivers.js` (band 1090/978, path must end in `aircraft.json`, 2 s timeout, 2 MB cap, stale at 10 s), `src/sources/adsbRecords.js` (`normalizeDump1090Aircraft`), `src/data/tapAddress.js`
- Upstream:
  - readsb `https://github.com/wiedehopf/readsb/blob/dev/README-json.md`
  - dump978 `https://github.com/flightaware/dump978`
  - gpsd JSON `https://gpsd.gitlab.io/gpsd/gpsd_json.html`
  - Davis WLL local API `https://weatherlink.github.io/weatherlink-live-local-api/`
  - Ecowitt local polling (unofficial, via Ecowitt's own `https://github.com/Ecowitt/ha-ecowitt-iot`; read only, copy nothing)

## Objective

Give the operator's own hardware a general way in.
- A LAN-only **socket transport** (TCP client and UDP listener) with a **codec SPI**, which 082, 083, 085 and 086 reuse.
- First codecs: SBS-1, gpsd and readsb JSON.
- `local-adsb-*` on readsb v2 (978 variant, MLAT, TIS-B, NACp).
- A `station-*` receiver-health family from `stats.json`.
- Local polling of Davis WeatherLink Live and Ecowitt gateways.

Everything here defaults to **local-only egress**, and MLAT positions are never redistributable.

## Tasks

### 1. LAN endpoints

Extend `LAN_URL` validation (004/005) to the schemes `tcp://host:port` and `udp://bindHost:port`.

- **TCP:** the host resolves once to a loopback, RFC 1918, link-local or ULA address, which is pinned; the connect goes to the pinned IP.
- **UDP:** `bindHost` must be `0.0.0.0`, `::` or a local interface address. A companion param of type `string-list` named `allowSenders` is **required**: IPs or CIDRs, each wholly inside private, loopback, link-local or ULA space. Validate it with `LocalAddresses`.
- **The port is always explicit.** No defaults are assumed: conventional ports such as 30003 appear in the docs only.
- **No server sockets.** Foresignal never listens on TCP for these sources; inbound TCP serving is 110's SERVE sinks.

### 2. Socket transport (`ai.foresignal.collect.socket`)

- `StreamContext.sockets()` returns `SocketTransport`. Add it to the SPI; adapters still never construct sockets themselves.
- **`tcp(LanEndpoint, SocketOptions, Framing, FrameHandler) → StreamHandle`:**
  - consumes **one budget unit per connect attempt** and meters bytes through `reportBytes`
  - connect timeout 5 s, TCP keepalive on
  - a read idle watchdog (`SocketOptions.silenceTimeout`, default 120 s) closes the connection so 010's ladder reconnects
  - `Framing.LINES` (LF or CRLF, max 64 KB per line; longer lines are dropped and counted) and `Framing.RAW` (chunks, for KISS in 082)
  - an optional `onConnect` writer, for gpsd's `?WATCH`
  - back-pressure: the handler may block in `RecordSink.accept`
- **`udp(LanEndpoint bind, Set<Cidr> allowSenders, DatagramHandler) → StreamHandle`:**
  - a `DatagramChannel` read on a virtual thread; one budget unit per bind
  - datagrams ≤ 64 KB
  - datagrams from senders outside the allowlist are dropped and counted (`foresignal.socket.udp.rejected{source}`), never parsed
- Metrics: `foresignal.socket.connects{source,result}`, `foresignal.socket.frames{source}`, `foresignal.socket.frames.dropped{source,reason}`.

### 3. Codec SPI (`ai.foresignal.source.codec`)

- `SocketCodec`:
  - `String id()`
  - `Framing framing()`
  - `CodecSession newSession(CodecContext)`, where a session holds per-connection state (e.g. SBS per-hex merge) and returns `List<NormalizedRecord> onFrame(byte[] frame, Instant receivedAt)`
- `CodecRegistry` collects the beans. Codecs are pure apart from session state, and fixture-testable.
- First codecs: `sbs1`, `gpsd`. 085 adds `aivdm`, 082 `kiss-ax25`, 086 `acars-json` and `sonde-udp`. Document the SPI in the Adapter Guide.

### 4. Sources

Slot families from code-reviewed lists: the operator supplies only LAN params, never arbitrary hosts. `foresignal.local-receivers.slots` (default 2) sets how many slots each family registers. All are disabled by default with `egress` LOCAL_ONLY.

**`local-adsb-{n}`** (POLLING; readsb, dump1090 or skyaware978 `aircraft.json`):
- Params: `url` (`LAN_URL`, http/https, path must end in `aircraft.json`, no credentials, query or fragment) and `band` (`1090|978`, default 1090).
- Interval 2 s (min 1 s, max 5 s); budget 60/min, 50,000/day; timeout 2 s; `maxResponseBytes` 4 MB.
- Parse with 062's `ReadsbAircraftMapper`, adding these readsb v2 specifics:
  - `type` values: `adsb_icao, adsb_icao_nt, adsr_icao, tisb_icao, adsc, mlat, other, mode_s, adsb_other, adsr_other, tisb_other, tisb_trackfile` → `posSource`
  - keep `nac_p, nic` (and `nic_baro, sil, gva, sda` as `quality.extra["adsb.*"]`)
  - for 978, `uat_version` → `quality.extra["adsb.uat_version"]`, payload `band: "978"`
- `observedAt = now − seen` from the document's `now`.
- One `Observer(receiverId = sourceId, rssi)` per record (`rssi` dBFS). Gated per CDM §14.
- Policy (CDM §5.3): `SAMPLED{10s, 120s, 300 m DR, thresholds, edgeMin 5s}` with `alt_baro` 150 m (500 ft) and `track` 10°; TRACK, TTL 5 min.
- `FetchResult.coverage = Coverage(receivers=1, actual=aircraft with position)`, so 006's coverage rule catches a dead antenna.
- A document whose `now` is older than 10 s against `receivedAt` sets health `degraded` ("receiver feed stale").

**`local-sbs-{n}`** (STREAMING over TCP, codec `sbs1`):
- Param `endpoint` (`LAN_URL tcp://`).
- Line format, 22 CSV fields: `MSG,type,session,aircraftId,hex,flightId,dateGen,timeGen,dateLog,timeLog,callsign,altitude,groundSpeed,track,lat,lon,verticalRate,squawk,alert,emergency,spi,isOnGround`.
  - Types: 1 ident, 2 surface position, 3 airborne position, 4 velocity, 5 surveillance altitude, 6 surveillance id, 7 air-to-air, 8 all-call.
  - Merge per hex into a snapshot (rule 7) using the same payload keys as the readsb mapper; altitude feet → metres.
- **Time base:** SBS dates and times are decoder-local wall clock with no zone, so declare `observedAtMode` RECEIVED (the framework imputes and flags `TIME_IMPUTED`), with `ttlAnchor` OBSERVED and TRACK. Never parse the SBS times into `observedAt`.
- Same policy as `local-adsb`. Sessions evict hexes unseen for 5 min.
- Readsb leaves SBS output off unless `--net-sbs-port` is set (the port is conventionally 30003); document this.

**`local-gpsd-{n}`** (STREAMING over TCP, codec `gpsd`):
- Param `endpoint` (`tcp://`; gpsd's conventional port is 2947, but it is not assumed). On connect, send `?WATCH={"enable":true,"json":true};` and expect a `VERSION` object (`proto_major` 3).
- Kind `gnss-fix`, one record, `externalId = device`.
- From TPV: `mode` (0–3), `time` → `observedAt` (UPSTREAM), lat/lon/`altHAE`, `eph, epv, ept, speed` (m/s), `track`.
- From SKY: `nSat, uSat, hdop`.
- Significant: `mode`, `fixQuality` (`none|2d|3d`), `navDegraded` (`mode < 3 || uSat < 4 || eph > 50`).
- STANDARD, `LATEST{keyframe 20m, edgeMin 15s}`, TTL 30 min.
- **`private: true`**: it is the operator's own location. 039 keeps it out of `openReads`, tiles, embeddings and LLM context. 086 builds the GNSS-integrity sentinel on it.

**`station-readsb-{n}`** (POLLING; `stats.json`):
- Param `url` (`LAN_URL`, path ending `stats.json`). Interval 60 s; budget 2/min, 2000/day. Kind `receiver-status`, `externalId = sourceId`.
- From `last1min` (periods `latest, last1min, last5min, last15min, total`, each with `start, end, local, remote, cpu, cpr, tracks, messages`):
  - `messagesPerSec` (`messages / (end − start)`)
  - `signalDbfs, noiseDbfs, peakSignalDbfs, strongSignals` (from `local`)
  - `tracksNew`
  - `aircraftWithPos` when present (verify on the fixture)
  - `state` (`ok | low-rate | silent`; thresholds param `minMessagesPerSec`, default 1.0)
- `observedAt = last1min.end`. STANDARD, `LATEST{keyframe 20m}` with significant `[state]`, TTL 30 min (keyframe 1200 ≤ 1800 − 60 − 30). A dead receiver expires and a rule on `state` sees `silent`.
- This replaces graphs1090 scraping (rejected in Gap §9).

**`wxstation-davis-{n}`** (POLLING; Davis WeatherLink Live):
- GET `http://<pinned>/v1/current_conditions`. **Never more often than every 10 s** (Davis API rule); default 60 s, min 10 s.
- Params: `url` (`LAN_URL`), `lat`, `lon` (required numbers, **rounded to 2 dp**, about 1 km: coarse geometry), `includeIndoor` (false).
- `data.conditions[]` by `data_structure_type`:
  - 1 ISS: `temp, hum, dew_point, wind_speed_last, wind_dir_last, wind_speed_hi_last_10_min, rain_rate_last, rainfall_daily, rain_size, trans_battery_flag`
  - 3 barometer: `bar_sea_level, bar_trend`
  - 4 indoor: only when `includeIndoor`
  - 2 (leaf/soil): ignored in v1
- Convert to SI: °F→°C, mph→m/s, inHg→hPa. Rain counts × `rain_size` (1 = 0.01 in, 2 = 0.2 mm, 3 = 0.1 mm, 4 = 0.001 in) → mm.
- Kind `weather-observation`, `externalId = data.did`, `observedAt = eventTime = data.ts` (measurement row). STANDARD, ALL, TTL 10 min, `ttlAnchor` OBSERVED.
- `fieldSpecs` with UCUM units (`Cel`, `%`, `m/s`, `deg`, `mm/h`, `mm`, `hPa`).

**`wxstation-ecowitt-{n}`** (POLLING; marked **experimental** in its description):
- GET `http://<pinned>/get_livedata_info`. Verified 2026-10: there is no official spec; the shape (`common_list[{id,val,unit}]`, `wh25`, `rain`, `piezoRain`, `ch_*`) is firmware-dependent.
- Tolerant parser: map known `common_list` ids through a table (outdoor temperature, humidity, wind speed, gust, direction, solar, UV, pressure), and read units from the `unit` string before converting. Unknown ids are ignored and counted.
- An unparseable body → `UpstreamFormatException`, so the health format-change rule fires after a firmware update.
- Same record model, coarse geometry and policy as Davis. Ecowitt's form-POST push is 059's preset (cleartext LAN key), not this source.

### 5. Egress defaults and MLAT

- Every `local-*`, `station-*` and `wxstation-*` descriptor sets `egress=LOCAL_ONLY` and `egressOverridable=true` (new boolean, default false).
- Add `source_settings.egress` (nullable override, accepted only when overridable; otherwise 400 `EGRESS_FIXED`) to `SourceService`, the 013 DTOs and the OpenAPI `SourceSettings`/`SourceSettingsUpdate`.
- **MLAT is never redistributable, even when the source is set to OPEN:**
  - `EgressPolicy.allows(record, path)` returns false for any record whose `posSource` is `mlat` on every non-local path (sinks, federation, share links, public feed keys).
  - It also strips `posSource=mlat` records from `openReads` anonymous responses when `security.exposeMlat` is false (new setting, default false).
  - 109, 110 and 113 call this; document the contract in its Javadoc.

### 6. Fixtures

Capture with a README (date, device, firmware), on the operator's hardware where possible:
- readsb `aircraft.json` (1090) and skyaware978 `aircraft.json` (`http://host:8978/data/aircraft.json` or `/skyaware978/data/aircraft.json`)
- `stats.json`
- 200 SBS lines (`nc <host> 30003 | head -200`)
- a gpsd session (`gpspipe -w -n 20`)
- a Davis `current_conditions`
- an Ecowitt `get_livedata_info`

If no device is available, hand-make from the cited specs and mark it in the README. Hand-made edge cases: `~` hex, mlat/tisb positions, `alt_baro: "ground"`, an SBS line with a missing field, a gpsd mode-1 TPV, Davis `rain_size` 2.

### 7. Spec, catalogue, docs

- `docs/OpenAPI.json`: `SourceSettings.egress` / `SourceSettingsUpdate.egress`, `SourceDescriptor.egressOverridable`, the `EGRESS_FIXED` problem code, the `security.exposeMlat` setting on `SecuritySettings`. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md`: replace the `local-adsb-*` row and add rows for `local-sbs-*`, `local-gpsd-*`, `station-readsb-*`, `wxstation-davis-*` and `wxstation-ecowitt-*` (tier, policy, TTL, time base, private/egress).
- `docs/Adapter-Guide.md`: "Socket transport and codecs", "LAN endpoints".
- Operator Guide: "Local receivers": enabling readsb JSON/SBS output, dump978 ports, gpsd, firewalling UDP, why data stays local, MLAT terms.
- `THIRD_PARTY_NOTICES.md`: GEV entries for the `local-receivers.js` / `adsbRecords.js` / `tapAddress.js` ports.

## Tests to write (do not run)

- `LanEndpointValidationTest`:
  - `tcp://8.8.8.8:30003` rejected; `.local` resolving to a public IP rejected; pinning
  - `udp://` without `allowSenders` rejected; a public CIDR in `allowSenders` rejected
  - a missing port rejected
- `TcpTransportIT` (loopback test server):
  - line framing including CRLF and an oversize line
  - `onConnect` write
  - silence watchdog → close → reconnect through the ladder (`MutableClock`)
  - one budget unit per connect
- `UdpTransportIT`: allowed sender parsed; disallowed sender dropped and counted; oversize datagram.
- `Sbs1CodecTest`: all 8 types merged per hex; feet→metres; a malformed line skipped; eviction.
- `GpsdCodecTest`: `?WATCH` sent; TPV/SKY merge; `navDegraded` boundaries; deprecated `alt` ignored.
- `ReadsbV2MappingTest`: every `type` → `posSource`; 978 `uat_version`; `quality.extra` keys; observer `rssi`.
- `ReadsbStatsParserTest`: `messagesPerSec`, `state` thresholds, a missing optional field.
- `DavisWllParserTest`: unit conversions; each `rain_size`; indoor excluded by default; coordinate rounding.
- `EcowittLiveDataParserTest`: known ids with °F/mph units; unknown ids counted; garbage → `UpstreamFormatException`.
- `LocalAdsbAdapterTest` (WireMock on 127.0.0.1): stale `now` → degraded; coverage reported; non-`aircraft.json` path rejected at validation.
- `EgressPolicyTest`: LOCAL_ONLY denies remote paths; an OPEN override is allowed only when overridable; mlat denied even when OPEN; the `exposeMlat` filter.
- `LocalReceiversIngestIT` (`@IntegrationTest`): `local-adsb` TRACK SAMPLED with local defaults (a 400 m move after 12 s → SAMPLE); the `station-readsb` silent transition → SIGNIFICANT; a gpsd record is private (absent from an anonymous `openReads` query when 039 is present).

## Acceptance criteria

- No local source can reach a non-LAN address, and UDP never parses a datagram from an unlisted sender.
- All local sources register with `egress=LOCAL_ONLY`, and MLAT positions are denied on every non-local path.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `069: Local Receivers`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/069-CR-Local-Receivers.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `069: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
