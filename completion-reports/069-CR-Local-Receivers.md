# 069-CR-Local-Receivers

| | |
|---|---|
| Prompt | 069-Local-Receivers.md |
| Status | Complete |
| Implementation commit | 9925416e7c9df0442676a3bfa0502d62c6d263d3 |
| Date | 2026-10-13 |

## Summary

The operator's own hardware now has a general way in: a LAN-only socket transport with a codec SPI, the first two
codecs (`sbs1`, `gpsd`), six source families on numbered slots, the egress override for overridable descriptors, and
the MLAT rule in `EgressPolicy`. Every local source is disabled by default and `LOCAL_ONLY`.

**1. LAN endpoints** (`ParamValidator`, `LocalAddresses`, `spi/LanEndpoint`, `ParamSpec.lanUrl`):
- **Schemes per parameter:** for a `LAN_URL`, `ParamSpec.allowed` now lists the accepted schemes. Empty keeps the
  historical `http, https, ws, wss`; `tcp` and `udp` are accepted only where listed, so no existing parameter changed.
- **`tcp://host:port`:** the host is resolved once under `OutboundUrlPolicy` `LAN_ONLY` and the address is pinned.
- **`udp://bindHost:port`:** `bindHost` must be `0.0.0.0`, `::` or an address literal of a local interface
  (`NetworkInterface.getByInetAddress`); a name is refused.
- **`allowSenders`:** the required `string-list` companion (`ParamValidator.ALLOW_SENDERS`). Each entry is an IP or
  CIDR wholly inside loopback, RFC 1918, link-local or ULA space (`LocalAddresses.isPrivateBlock`). A block that only
  overlaps private space (`10.0.0.0/7`) is refused.
- **Shape:** the port is always explicit; a path, query, fragment or user info is refused.
- **Reading it back:** `LanEndpoint.fromParams(params, name)` gives the scheme, host, port and the pinned (TCP) or bind
  (UDP) address.

**2. Socket transport** (`ai.foresignal.collect.socket`; SPI types in `source.spi`):
- **SPI:** `StreamContext.sockets()` returns a `SocketTransport` (`Framing`, `SocketOptions`, `FrameHandler`,
  `DatagramHandler`). `FetchContextFactory` wires `SocketTransportFactory` behind it by setter injection, so the
  existing constructors are unchanged.
- **`DefaultSocketTransport.tcp`:**
  - re-checks that the address is loopback, private or ULA (and `LAN_ONLY`) before spending anything;
  - takes one budget unit per connect attempt through `ctx.chargeRequest`;
  - connect timeout 5 s, keepalive on, optional `onConnect` write;
  - the silence watchdog is the socket read timeout: a connection silent for `silenceTimeout` (default 120 s) ends
    with `SilenceTimeoutException`, so the supervisor's ladder reconnects;
  - `LINES` (LF/CRLF, 64 KB; a longer line is dropped whole and counted, `LineFramer`) and `RAW`;
  - bytes are metered with `reportBytes` after every read; a handler may block in the sink.
- **`udp`:** a `DatagramChannel` read on a virtual thread, one unit per bind. A sender outside the allowlist is counted
  (`foresignal.socket.udp.rejected`) and its bytes are never looked at; a datagram over 64 KB is dropped and counted.
- **Metrics:** `foresignal.socket.connects{source,result}`, `.frames{source}`, `.frames.dropped{source,reason}`.
- **No server socket anywhere.** A new guard in `UpstreamClientIsTheOnlyHttpClientTest` allows socket construction only
  in `DefaultSocketTransport`.

**3. Codec SPI** (`ai.foresignal.source.codec`):
- **SPI:** `SocketCodec` (`id`, `framing`, `newSession`, optional `onConnect`), `CodecSession`
  (`onFrame(byte[], Instant)`, `skipped()`), `CodecContext`, and `CodecRegistry` (duplicate or malformed ids fail
  startup).
- **`Sbs1Codec`:**
  - 22-field lines, all eight types merged per hex into the readsb payload keys; feet → meters; `posSource: other`;
  - one observer (the source) per record;
  - times never parsed; hexes evicted after 5 min;
  - malformed lines skipped and counted; `SEL/ID/AIR/STA/CLK` ignored.
- **`GpsdCodec`:**
  - writes `?WATCH={"enable":true,"json":true};\n` and requires VERSION `proto_major` 3;
  - TPV and SKY are merged per device; `observedAt` = TPV `time`;
  - `fixQuality`, and `navDegraded` (`mode < 3 || uSat < 4 || eph > 50`, unknown terms left out);
  - deprecated `alt` ignored; values rounded so a stationary receiver does not version every second.
- **Documented** in Adapter Guide §20.

**4. Sources** (`ai.foresignal.source.adapters.local`, `LocalReceiversFamily`): `foresignal.local-receivers.slots`
(default 2, 1–16, `FORESIGNAL_LOCAL_RECEIVERS_SLOTS` in `application.yml`) slots of each family.
- **`local-adsb-{n}`:**
  - shape and schedule: POLLING; `url` (http/https, path ending in `aircraft.json`) and `band`; 2 s (min 1 s);
    60/min and 50,000/day; timeout 2 s; 4 MB;
  - records: the shared mapper with `MapperOptions.localReceiver` (new: observer with `rssi`, `band: "978"`,
    `nic_baro/sil/gva/sda` and `uat_version` as `quality.extra["adsb.*"]`);
  - policy: TRACK, TTL 5 min, SAMPLED `{10s, 120s, 300 m, thresholds, edgeMin 5s}`, `alt_baro` 150 m, `track` 10°
    (`AircraftDescriptors.local`);
  - coverage `Coverage(1, null, positioned, null)`; a `now` over 10 s old degrades health ("receiver feed stale"),
    and a fresh one clears only that reason.
- **`local-sbs-{n}`:** STREAMING via `CodecStream` + `sbs1`; `observedAtMode` RECEIVED, `ttlAnchor` OBSERVED; same
  policy; budget 2/min, 200/day.
- **`local-gpsd-{n}`:** STREAMING via `gpsd`; kind `gnss-fix`; STANDARD, `LATEST{20m, edgeMin 15s}`; significant
  `mode, fixQuality, navDegraded`; TTL 30 min; retention 365 d; **private**.
- **`station-readsb-{n}`:**
  - POLLING `stats.json`, 60 s (min 30 s), 2/min and 2000/day;
  - payload `messagesPerSec, signalDbfs, noiseDbfs, peakSignalDbfs, strongSignals, tracksNew, aircraftWithPos, state`;
  - `observedAt = last1min.end`; `LATEST{20m}` significant `[state]`; TTL 30 min; retention 365 d.
- **`wxstation-davis-{n}`:**
  - `GET /v1/current_conditions` (resolved against the console URL); min interval 10 s, default 60 s;
  - `lat`/`lon` rounded to 2 dp; ISS, barometer and (optional) indoor records in SI units; rain counts × `rain_size`;
  - `externalId = did`, `observedAt = eventTime = ts`; STANDARD, ALL, TTL 10 min (OBSERVED), retention 365 d, UCUM
    `fieldSpecs`.
- **`wxstation-ecowitt-{n}`:**
  - experimental: `GET /get_livedata_info`; tolerant table of `common_list` ids; units from `unit` or the value
    suffix;
  - unknown ids counted (`foresignal.ecowitt.unknown_ids`); an HTML page or a body without any list is an
    `UpstreamFormatException`.
- **Payload keys of the weather stations** are those of the 059 `ecowitt`/`wu-pws` presets (`tempC, rh, dewPointC,
  windSpeedMs, windDirDeg, windGustMs, rainRateMmH, rainDailyMm, pressureHpa`, ...).

**5. Egress and MLAT:**
- **Descriptor:** `SourceDescriptor.egressOverridable` (new component, default false). Every local descriptor sets
  `LOCAL_ONLY` and `egressOverridable(true)`.
- **Settings:** `source_settings.egress` (nullable column, Hibernate `update`), `SourceSettingsUpdate.egress/clearEgress`,
  the API DTOs (`egress` with present-tracking, `SourceSettingsDto.egress`, `SourceDescriptorDto.egressOverridable`),
  `SourceService.egressOverride/effectiveEgress`. An override on a fixed descriptor is `400 EGRESS_FIXED` (new
  `ErrorCodes` constant).
- **`EgressPolicy`:**
  - `isLocalOnly` reads the effective egress, cached per source and dropped on `SourceSettingsChangedEvent`;
  - the new record-level hooks `allows(EgressPath, sourceId, payload, viewer)` and `allows(RecordRow, EgressPath)` refuse
    `posSource = mlat` on SINK, FEDERATION, SHARE and FEED even when OPEN; the contract for 109/110/113 is in the
    Javadoc;
  - `withoutMlatForAnonymous` / `allowsAnonymous` strip MLAT from anonymous reads unless `security.exposeMlat`.
- **Records API:** `RecordsController` applies the anonymous MLAT filter to list (JSON, GeoJSON, streams), asOf, get
  (404), versions and history.
- **`security.exposeMlat`:** `SecuritySettings.exposeMlat` (default false) is accepted by `updateSecuritySettings`; it
  was refused with 400 until now.

**6. Fixtures:** `src/test/resources/fixtures/local-receivers/`, all hand-made (`synthetic: true`), with a README
naming each spec and the recapture command. They include every prompt edge case (`~` hex, mlat and tisb positions,
`alt_baro: "ground"`, an SBS line missing a field, a gpsd mode-1 TPV, Davis `rain_size` 2) plus imperial Ecowitt units
and garbage.

**7. Spec and docs:**
- **`docs/OpenAPI.json`** (verified and reconciled; the 069 properties were already in the spec):
  - `SecuritySettings.exposeMlat` description and its operations' `x-foresignal-extended-by`;
  - the `updateSecuritySettings` description;
  - `EGRESS_FIXED` in `Problem.x-foresignal-error-codes`;
  - `ParamSpec.allowed` for lan-url schemes;
  - the `aircraft`, `gnss-fix`, `receiver-status` and `weather-observation` kind entries (Davis/Ecowitt
    `payloadFields`);
  - a changelog entry.
- **Postman** regenerated (832 requests, all operations covered).
- **`docs/Source-Catalog.md`:** the `local-adsb-*` row is replaced and five rows are added.
- **`docs/Adapter-Guide.md`:** §19 "LAN endpoints" and §20 "Socket transport and codecs".
- **`docs/Operator-Guide.md`:** "Local receivers" (outputs to enable, ports, gpsd, firewalling UDP, why data stays
  local, MLAT terms).
- **`THIRD_PARTY_NOTICES.md`:** the GEV rows.
- **`docs/Invariants.md`:** SRC-1, SRC-7, EGR-1 and EGR-2 test names.

## Files created / modified

- **Created, main:**
  - `collect/StreamContextDefaults.java`;
  - `collect/socket/` (`DefaultSocketTransport`, `SocketTransportFactory`, `LineFramer`, `SocketMetrics`,
    `package-info`);
  - `source/spi/` (`LanEndpoint`, `SocketTransport`, `SocketOptions`, `Framing`, `FrameHandler`, `DatagramHandler`);
  - `source/codec/` (`SocketCodec`, `CodecSession`, `CodecContext`, `CodecRegistry`, `Sbs1Codec`, `GpsdCodec`,
    `package-info`);
  - `source/adapters/local/` (`LocalReceiversFamily`, `LocalReceivers`, `LocalAdsbAdapter`, `LocalSbsAdapter`,
    `LocalGpsdAdapter`, `CodecStream`, `StationReadsbAdapter`, `ReadsbStatsParser`, `DavisWllAdapter`,
    `DavisWllParser`, `EcowittAdapter`, `EcowittLiveDataParser`, `WeatherStations`, `package-info`).
- **Modified, main:**
  - `source/spi/` `SourceDescriptor`, `StreamContext`, `ParamSpec`;
  - `source/` `ParamValidator`, `LocalAddresses`, `SourceService`, `SourceSettingsUpdate`;
  - `source/formats/readsb/` `MapperOptions`, `ReadsbAircraftMapper`, `AircraftFields`, `AircraftDescriptors`;
  - `collect/` `DefaultStreamContext`, `FetchContextFactory`;
  - `security/EgressPolicy`, `settings/SecuritySettings`, `settings/SecuritySettingsService`;
  - `api/` `RecordsController`, `SourcesController`, `dto/SourceDescriptorDto`, `dto/SourceSettingsDto`,
    `dto/SourceSettingsUpdate`, `mapper/SourceMapper`, `error/ErrorCodes`;
  - `store/SourceSettingsEntity`, `application.yml`.
- **Created, test:**
  - `source/LanEndpointValidationTest`;
  - `collect/socket/` (`TcpTransportIT`, `UdpTransportIT`, `LineFramerTest`, `TestStreamContext`);
  - `source/codec/` (`Sbs1CodecTest`, `GpsdCodecTest`, `CodecRegistryTest`);
  - `source/formats/readsb/ReadsbV2MappingTest`;
  - `source/adapters/local/` (`ReadsbStatsParserTest`, `DavisWllParserTest`, `EcowittLiveDataParserTest`,
    `LocalAdsbAdapterTest`, `LocalReceiversFamilyTest`, `LocalReceiversIngestIT`);
  - `fixtures/local-receivers/` (README and 11 fixtures).
- **Modified, test:**
  - `security/EgressPolicyTest` (nested 069 class);
  - `settings/SecuritySettingsServiceTest`, `api/SecuritySettingsIT` (`exposeMlat` is now accepted);
  - `source/formats/readsb/ReadsbAircraftMapperTest` (`adsc` → `adsc`, REPORTED);
  - `retention/FamilyDefaultsTest` (the six adapters; a `track` class branch);
  - `collect/UpstreamClientIsTheOnlyHttpClientTest` (the socket guard).
- **Docs:** `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`, `docs/Source-Catalog.md`,
  `docs/Adapter-Guide.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

1. **Link-local TCP and HTTP destinations are refused.** The prompt lists link-local among the addresses a `tcp://`
   host may resolve to. 035's `LAN_ONLY` profile (the one SSRF guard, Adapter Guide §12) blocks 169.254/16 and
   fe80::/10 in every profile because of cloud metadata, and HTTP `LAN_URL`s already follow it. Link-local is accepted
   in `allowSenders`, as the prompt says.
2. **Budget and backoff on sockets.** Units go through `ctx.chargeRequest` (budget and usage metering). A source in
   upstream backoff is not checked before a LAN connect, because the backoff registry is fed by HTTP status codes a
   socket never produces.
3. **The silence watchdog is the socket read timeout,** measured in real time. `TcpTransportIT` uses a `MutableClock`
   for the frame stamps and drives the reconnect (a second `tcp()` paying a second unit) directly; the supervisor's
   ladder treats the `SilenceTimeoutException` like any drop.
4. **No maximum interval exists in the descriptor model.** `local-adsb`'s "max 5 s" is stated in the description only;
   the operator can set a slower interval, and the TTL and keyframe stay valid up to 150 s.
5. **Ecowitt's time base is RECEIVED:** `/get_livedata_info` carries no timestamp, so "the same record model as Davis"
   could not include `observedAt = eventTime = ts`. `externalId` is the source id (the endpoint names no device).
6. **`adsc` maps to `posSource: adsc`** (REPORTED) in the shared mapper, for every readsb-format source (062's mapper
   returned `other`). `ReadsbAircraftMapperTest` was updated. Every other prompt type maps as before.
7. **Anonymous MLAT stripping covers the records API only** (list, get, versions, history, asOf). Tiles, SSE, search and
   MCP do not strip yet (Issue 1). A projection (`fields=`) that leaves `posSource` out cannot be judged, so aircraft
   rows from sources that report `posSource` are withheld from anonymous callers in that case (fail closed).
8. **Weather-station extras:** `pressureTrendHpa` and `batteryLow` (Davis) and `solarWm2`/`uvIndex` (Ecowitt) are stored
   beyond the minimal key list; the OpenAPI kind entry lists them.
9. **`stats.json` fields:** readsb's README documents neither `noise` (dump1090-fa's) nor `aircraft_with_pos`. Both are
   read only when present, from the top level or `last1min`. There was no real `stats.json` to verify them on
   (Issue 2).
10. **The SBS emergency flag** says only that an emergency is on. A set flag maps squawk 7500, 7600 and 7700 to
    `unlawful`, `nordo` and `general`, and anything else to `general`; a cleared flag removes it.
11. **Retention class:** GNSS fixes, receiver health and station readings are `state` (365 d). `FamilyDefaultsTest`
    gained a `track` class branch for the two aircraft families (TRACK, no `defaultRetention`).
12. **Icons and colors** (not specified): `plane #0B7285`, `marker #5F3DC4`, `antenna #2B8A3E`, `forecast #1971C2`.

## Issues found / open questions

1. **MLAT on the other anonymous read paths.** Tiles, SSE, search and MCP are not filtered by `exposeMlat` yet, so an
   anonymous tile or SSE reader can still see an MLAT position from a local receiver. The SQL-level fix is a flag in
   `ReadRestriction` / `RestrictionClamp`; it touches every query builder and was left for 109 or the 097 checkpoint.
2. **Fixtures are hand-made.** No receiver, gpsd, WeatherLink Live or Ecowitt gateway was reachable (or probed) from
   this build. They should be recaptured on the operator's hardware per the fixture README. The Ecowitt shape in
   particular is firmware-dependent.
3. **`FamilyDefaultsTest`** still fails on the unclassified 062–064 adapters (carried from 065–068). The 069 adapters
   are classified.
4. **Instructions in data:** none. The readsb README, the WeatherLink Live API page and Ecowitt's `wittiot` source were
   read; none contained instructions to an agent.

**Owner questions (non-blocking):**
1. Should link-local addresses be allowed for `tcp://` receivers (Deviation 1)? That would need a narrower
   `OutboundUrlPolicy` exception that still refuses the metadata addresses.
2. Is it acceptable that anonymous MLAT stripping covers only the records API until Issue 1 is done, or should tiles
   and SSE be gated before 097?

## Tests written (not run)

- **`LanEndpointValidationTest`:**
  - `tcp://8.8.8.8:30003` and a `.local` name resolving public refused (`URL_NOT_ALLOWED`);
  - pinning survives a rebinding; loopback and ULA literals accepted;
  - missing or impossible ports refused; path, query, fragment and user info refused; schemes per parameter;
  - `udp://` without `allowSenders` refused, also on a descriptor that does not declare it;
  - public, overlapping, CGNAT and malformed sender blocks refused; private ones accepted;
  - non-local bind hosts refused; loopback and `::` binds accepted;
  - `isPrivateBlock`.
- **`TcpTransportIT`** (loopback server):
  - LF/CRLF framing with an oversize line dropped and counted, metered bytes; RAW framing;
  - the `onConnect` write;
  - the silence watchdog, and the reconnect paying a second unit;
  - a peer close and a throwing handler;
  - one unit per connect (failed ones included); a spent budget connects nothing;
  - a public, CGNAT or UDP endpoint refused before any unit.
- **`UdpTransportIT`:** an allowed sender delivered; a disallowed sender dropped and counted, never parsed; an
  oversize datagram dropped and counted; refusals before a unit. **`LineFramerTest`.**
- **`Sbs1CodecTest`:** 200 lines; all 8 types merged per hex; feet → meters; surface; edge cases (4 skipped, 7700,
  `~` hex); eviction; independent sessions.
- **`GpsdCodecTest`:** `?WATCH`; the captured-style session (mode 1, 2D, 3D, SKY merge, `alt` ignored, junk skipped);
  `navDegraded` boundaries; SKY before TPV; missing time; devices; a wrong protocol major. **`CodecRegistryTest`.**
- **`ReadsbV2MappingTest`:** every `type` → `posSource`; position sources; the 1090 fixture; `quality.extra` keys; the
  observer `rssi`; 978 `uat_version` and `band`; global defaults unchanged.
- **`ReadsbStatsParserTest`:** `messagesPerSec`, signal fields, thresholds, silent, `--net-only`, missing optional
  fields, format errors.
- **`DavisWllParserTest`:** conversions, each `rain_size`, unknown size, indoor excluded by default, coordinate
  rounding, format errors.
- **`EcowittLiveDataParserTest`:** metric and imperial (°F, mph, inHg, in) with unknown ids counted, unit table, unknown
  units, garbage.
- **`LocalAdsbAdapterTest`** (WireMock on 127.0.0.1, real `UpstreamClient`):
  - coverage and observers;
  - stale `now` degrading and clearing; the 10 s boundary and a foreign reason kept;
  - 978 band; a non-JSON 200;
  - a non-`aircraft.json` path, a query, a `tcp://` URL and a public host refused at validation;
  - the descriptor.
- **`LocalReceiversFamilyTest`:** slots and range; every descriptor valid (`DescriptorValidator`), `LOCAL_ONLY`,
  overridable, LAN-only params; per-family time base, tier and policy.
- **`EgressPolicyTest`** (nested 069 class):
  - LOCAL_ONLY denies remote paths;
  - the OPEN override only when overridable, with cache and invalidation;
  - MLAT denied on every non-local path even when OPEN (payload and row overloads, fail closed);
  - the `exposeMlat` filter.
- **`LocalReceiversIngestIT`** (`WeatherIngestSupport`):
  - `local-adsb` 400 m after 12 s → SAMPLE;
  - `station-readsb` ok → silent → SIGNIFICANT, retention 365 d;
  - `local-gpsd` private (hidden from anonymous, never embedded or federated);
  - the egress override, `EGRESS_FIXED` on `ioda-outages`, and MLAT on SINK/FEED.
- **Updated:** `SecuritySettingsServiceTest`, `SecuritySettingsIT`, `ReadsbAircraftMapperTest`, `FamilyDefaultsTest`,
  `UpstreamClientIsTheOnlyHttpClientTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL (the existing unchecked-operations note).
- `./gradlew lintOpenApi`: BUILD SUCCESSFUL; the same 71 warnings as before the change (counted with and without it).
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: 832 requests across 80 tag folders, covers all ops.
- No tests and no guards were run: this is a feature prompt.

## Notes for the next prompt

1. **082, 083, 085 and 086 (socket sources):**
   - declare the endpoint with `ParamSpec.lanUrl(name, description, true, "tcp")` (or `"udp"` plus a `string-list`
     `allowSenders`);
   - read it with `LanEndpoint.fromParams`;
   - add a `SocketCodec` bean in `source.codec` and run it like `CodecStream.start`. Adapters cannot import
     `adapters.local`, so copy that small helper or move it to `source.codec` when the second family needs it;
   - a UDP source calls `ctx.sockets().udp(bind, allowSenders, handler)`;
   - a codec that splits entity state across frames merges per entity in its session (rule 7).
2. **086 (GNSS integrity):** `local-gpsd-{n}` records are `gnss-fix` per device, with `mode, fixQuality, navDegraded,
   eph, uSat, nSat, hdop`. The codec ignores `GST`, `PPS` and the satellite list's per-satellite C/N0, which the
   sentinel will need (`satellites[].ss`).
3. **080 (feeder sources):** use `MapperOptions` as needed. `localReceiver(sourceId, band)` adds the observer, the band
   and the integrity extras; `adsc` is now its own `posSource`.
4. **097 checkpoint:**
   - first run of every 069 test;
   - `LocalReceiversIngestIT` shares `WeatherIngestSupport`'s context (no new property: the local URLs point at its
     WireMock);
   - the socket ITs bind loopback ports;
   - `FamilyDefaultsTest` still fails on 062–064 only;
   - consider Issue 1 (MLAT on tiles and SSE).
5. **109, 110, 112 and 113:** re-serving paths must call `EgressPolicy.allows(EgressPath, sourceId, payload, viewer)`
   or `allows(RecordRow, EgressPath)`, never only the source-level overload, so MLAT and the effective egress apply.
   109's operator override for licensed sources is separate from `egressOverridable`, which only the local families set.
6. **143:** optionally recapture the fixtures on the operator's hardware per the README. Check `aircraft_with_pos` and
   `noise` in a real `stats.json`, and the Ecowitt ids on the installed firmware.
7. **144 (Flyway):** new nullable column `source_settings.egress` (varchar 16). Adapter state: `health.degraded` of
   `local-adsb-{n}` ("receiver feed stale").
8. **Owner decisions pending:** the two questions above, plus those carried from 012–068.
