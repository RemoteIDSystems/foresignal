# 077-CR-Space-Weather-And-Ground-Stations

| | |
|---|---|
| Prompt | 077-Space-Weather-And-Ground-Stations.md |
| Status | Complete |
| Implementation commit | 47383f91e22828b0fd4db1f91cb495e11833739e |
| Date | 2026-10-13 |

## Summary

The `noaa-swpc` placeholder (a catalog row only; no adapter was ever registered) is replaced by five focused SWPC
sources. NASA DONKI, two JPL sources, SatNOGS stations, SondeHub radiosondes and a local radiosonde_auto_rx receiver
over UDP are added. All eleven sources are STANDARD, ALL and disabled by default, under
`ai.foresignal.source.adapters.space`. `tinygs` is not built.

**1. SWPC** (`space.swpc`). Every SWPC request is a conditional GET; a 304 file contributes nothing, all-304 is
`FetchResult.unchanged()`, and a file that does not parse forgets its validators.
- **`swpc-alerts`** (`SwpcAlertsAdapter`, `SwpcMessageParser`, `SwpcProductsParser.scales`):
  - messages: header lines read from the text; externalId `code:serial`; category from the code prefix; a
    `CANCEL …:` line or `Cancel Serial Number` makes a cancel, which tombstones `code:cancelSerial` (the canceled
    message itself is not emitted); `observedAt = issue_datetime`, `eventTime` = threshold or begin time,
    `validFrom`/`validUntil` from `Valid From` and `Valid To` (or `Now Valid Until`); message LF-normalized, ≤ 4 KB,
    embedded;
  - scales: entity `current`, `r, s, g`, their words, and `dayN{RMinorProb, RMajorProb, SProb, GScale}` for days 1–3;
  - TTL 7 d on receipt, 5 min, 2/min, 600/day, retention 1825 d (`event`).
- **`swpc-kp`** (`SwpcKpAdapter`): `kp-estimated` (newest 1-minute row, `kpText` from the file) and `kp-official`
  (newest 3-hour row, `kpText` derived from the thirds, `aRunning`, `stationCount`). TTL 3 h OBSERVED.
- **`swpc-rtsw`** (`SwpcRtswAdapter`, `RtswDownsampler`): the two summary files every 5 min; `rtsw_wind_1m.json`
  (2.6 MB) only once per `densityInterval` (default 60 min, min 30 min), also conditional, newest `active` row for
  density and spacecraft. Speed to 10 km/s, Bt/Bz to 0.5 nT, density to 0.1. Each part's last value is kept in
  adapter state, so a run where one part changed emits a complete snapshot. TTL 30 min OBSERVED, 4/min, 900/day,
  80 MB/day.
- **`swpc-xray`** (`SwpcXrayAdapter`): flares keyed `begin_time:satellite`, and `xray-current` entity `current`
  (`currentClass`, `currentRatio` to 4 decimals). No TTL.
- **`swpc-aurora`** (`SwpcAuroraAdapter`, `OvationSampler`): the grid parsed once into a 360 × 181 `byte[]`; one
  record per place from `PlacePoints` (prompt 036's `PlaceService`), at most `maxPlaces` (50) by id; `probability` at
  the rounded cell, `maxWithin300Km` by great-circle distance with longitude wrap; geometry the place point. No place:
  nothing is downloaded, empty complete result, health reason `no places`. Removed places are tombstoned
  (`LiveIdSet`). `private=true`. TTL 1 h OBSERVED, 15 min, 1/min, 100/day, 100 MB/day.

**2. `nasa-donki`** (`NasaDonkiAdapter`, `DonkiParser`): `ccmc.gsfc.nasa.gov/DONKI-API/get/{type}` only (no adapter
contacts `kauai.ccmc.gsfc.nasa.gov`). One request per type in `types` (default `FLR, CME, GST, IPS, SEP`; all 11 types
allowed) over `lookbackDays` (default 7, 1–60). Ids, times and payload as the prompt gives; a CME analysis is keyed
`CMEAnalysis/{n}` from its link; WSA-ENLIL simulations use `modelCompletionTime`. A failing type is logged and the
others kept. 30 min, 12/min, 400/day.

**3. JPL** (`space.jpl`, `JplParsers`): `jpl-fireballs` (point signed by `lat-dir`/`lon-dir`, `radiatedEnergyJ =
energy × 1e10`, 6 h) and `jpl-close-approaches` (`des@cd`, `distLunar = dist × 389.17`, `eventTime = cd` with the TDB
note in `fieldSpecs`, TTL 7 d, 12 h, param `distMaxAu`). Both RECEIVED, 1/min, 10/day; numbers are read from strings;
an empty window (`count 0` without `fields`) is a valid empty result.

**4. `satnogs-stations`** (`SatnogsStationsAdapter`, `SatnogsParser`): `?status=online`, DRF `Link: rel="next"` (or a
page's `next`) followed on the same origin, at most 20 pages; payload as the prompt gives, `lastSeen` volatile,
`observations` left out; fetch timeout 120 s, 6 h. Absence tombstones after two complete lists. CC BY-SA 4.0.

**5. Radiosondes:**
- **Shared contract** `ai.foresignal.source.formats.sonde.RadiosondeRecords`: kind, payload names, field specs,
  sentinel scrubbing and the observer, used by both sources so neither imports the other.
- **`sondehub`** (`SondehubAdapter`, `SondehubParser`): `/sondes` only; `center` (default the first place by id, else
  the run fails with a message), `radiusKm` (500, max 1500), `windowS` (600). `uploader_callsign` → observer with
  `snr`/`rssi`; `uploader_position`, `uploader_antenna`, `frame`, `batt` never stored. TTL 30 min OBSERVED, 5 min
  (min 2 min), 2/min, 400/day. CC BY-SA 2.0; the description says the values are uncalibrated.
- **`local-sonde`** (`LocalSondeAdapter`, codec `sonde-udp` = `ai.foresignal.source.codec.AutoRxPayloadCodec`):
  STREAMING, `endpoint` (`udp://…`, required) and `allowSenders` (required, private blocks only, checked by 069's
  `ParamValidator`), listens only through `ctx.sockets().udp(...)`. Only `PAYLOAD_SUMMARY` objects become records;
  other types are counted as ignored, malformed JSON as skipped. Egress `LOCAL_ONLY`, overridable. The codec was
  written from the prompt's field list; no auto_rx code was read.

**6. Docs:**
- `docs/Source-Catalog.md`: the `noaa-swpc` row replaced by the five SWPC rows, plus DONKI, both JPL sources,
  SatNOGS, SondeHub and `local-sonde`; the DONKI endpoint move, the removed SWPC files and the TinyGS exclusion
  (in its own row and in "External provider ideas"); a prompt 077 note; the roadmap lines.
- `docs/Invariants.md`: `LocalSondeIT` under SRC-7 and EGR-2, `SpaceWeatherIngestIT` under EGR-1.
- `source/codec/package-info.java`: `sonde-udp` is now built (077), not 086.
- **No API change.** The OpenAPI kind entries for all eleven kinds already list the payload names used here.

## Files created / modified

- **Created, main:**
  - `source/adapters/space/`: `package-info`, `SpaceSources`;
  - `space/swpc/`: `package-info`, `SwpcMessageParser`, `SwpcProductsParser`, `RtswDownsampler`, `OvationSampler`,
    `SwpcAlertsAdapter`, `SwpcKpAdapter`, `SwpcRtswAdapter`, `SwpcXrayAdapter`, `SwpcAuroraAdapter`;
  - `space/donki/`: `package-info`, `DonkiParser`, `NasaDonkiAdapter`;
  - `space/jpl/`: `package-info`, `JplParsers`, `JplFireballsAdapter`, `JplCloseApproachesAdapter`;
  - `space/satnogs/`: `package-info`, `SatnogsParser`, `SatnogsStationsAdapter`;
  - `space/sondehub/`: `package-info`, `SondehubParser`, `SondehubAdapter`;
  - `space/localsonde/`: `package-info`, `LocalSondeAdapter`;
  - `source/formats/sonde/`: `package-info`, `RadiosondeRecords`;
  - `source/codec/AutoRxPayloadCodec`.
- **Modified, main:** `source/codec/package-info`.
- **Created, test:** `space/swpc/{SwpcMessageParserTest, SwpcProductsParserTest, RtswDownsamplerTest,
  OvationSamplerTest}`, `space/donki/DonkiParserTest`, `space/jpl/JplParsersTest`, `space/satnogs/SatnogsParserTest`,
  `space/sondehub/SondehubParserTest`, `codec/AutoRxPayloadCodecTest`, `space/SpaceAdaptersTest`,
  `space/localsonde/LocalSondeIT`, `adapters/SpaceWeatherIngestIT`.
- **Created, fixtures** (each with a README): `swpc-alerts/`, `swpc-kp/`, `swpc-rtsw/`, `swpc-xray/`, `swpc-aurora/`,
  `nasa-donki/`, `jpl-fireballs/`, `jpl-close-approaches/`, `satnogs-stations/`, `sondehub/`, `local-sonde/`.
- **Modified, test:** `testsupport/weather/WeatherIngestSupport` (`foresignal.test.adapters.space.base-url`, three
  ids in the backoff list), `retention/FamilyDefaultsTest` (the eleven adapter classes).
- **Docs:** `docs/Source-Catalog.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **SatNOGS filter is `status=online`, not `status=2`.** On 2026-10-13 `status=2` and `status=Online` answered
   `400 Select a valid choice`. The answer was one unpaged array (297 stations, no `Link` header); Link paging is
   implemented and tested against stubs anyway.
2. **`kp-official` `observedAt` is the period end** (`time_tag` + 3 h); `eventTime` stays `time_tag`. With
   `observedAt = time_tag` and a 3 h OBSERVED TTL, the newest official row (15:00 at 20:46) would be expired the
   moment it was published.
3. **Budgets raised where one run needs more requests than the per-minute value** (the governor refuses rather than
   queues, as in 075 and 076): `nasa-donki` 12/min (one request per type, up to 11). Daily 400 is kept; 11 types every
   30 min would need 528/day, which the descriptor text leaves to the operator. `satnogs-stations` had no budget in
   the prompt: 20/min (pages) and 100/day.
4. **`lookbackDays` is capped at 60:** the API refuses longer ranges (`400 API Error: Date range cannot exceed 60
   days`).
5. **Sub-packages per family, not per source:** `swpc` (five sources), `jpl` (two), `donki`, `satnogs`, `sondehub`,
   `localsonde`. Shared helpers are in `space.SpaceSources` and `formats.sonde.RadiosondeRecords`; no adapter imports
   another.
6. **Codec placement and name:** the codec is `source.codec.AutoRxPayloadCodec` with id `sonde-udp`, per Conventions
   "Shared parsers" and the 069 codec package; the prompt's test name `AutoRxPayloadCodecTest` is kept.
7. **Cancel category:** a message with a `CANCEL …:` line is `category = cancel` even under a watch code (`WATA30`);
   the code prefix alone would call it a watch.
8. **Reused SWPC serials:** the file held `ALTEF3:3752` three times and `ALTTP2:1531` twice (continued or corrected
   messages); the newest issue is kept, the others counted as `duplicate-id`.
9. **Additions:**
   - `swpc-aurora` tombstones a place that leaves the set, re-samples the last grid in memory when only the places
     changed (no second request), and degrades with a `maxPlaces` warning;
   - `satnogs-stations` tombstones stations missing from two complete lists; a foreign-origin next link is not
     followed and leaves the list incomplete;
   - `swpc-rtsw` keeps the last part values in adapter state (needed for complete snapshots);
   - radiosonde sentinel scrubbing (temperature below -120 °C, humidity outside 0–100, pressure ≤ 0, heading mod 360);
   - the DONKI notification body is dropped when it is only Markdown heading marks (every body was `"## "`).
10. **`xray-current`** carries only `currentClass` and `currentRatio` (the OpenAPI list); `currentRatio` is rounded to
    4 decimals so float noise is not a version. The flare's `currentClass` still changes with each poll while the
    flare is the latest, as the prompt specifies.
11. **RTSW temperature** is read with the active row but not stored: the OpenAPI `solar-wind` payload has no
    temperature field and this prompt makes no API change.
12. **Retention by class:** alerts, flares (via `kindRetention`), DONKI, fireballs, close approaches `event` (1825 d);
    Kp, solar wind, current X-ray, aurora `state` (365 d); SatNOGS `reference` (90 d); both radiosonde sources
    `bulk-event` (90 d, high-volume telemetry).
13. **Keyframes** (required by the 008 validator wherever a TTL is set): alerts 6 d, Kp 2 h, RTSW 20 min, aurora
    40 min, close approaches 6 d, SondeHub and `local-sonde` 20 min.
14. **Spring constructors:** every new component has exactly one `@Autowired` injection constructor. Those that also
    take a test seam have a second, unannotated constructor: `SwpcAuroraAdapter` and `SondehubAdapter`
    (`Supplier<PlacePoints>`), `LocalSondeAdapter` (a `SocketCodec`, beside the `CodecRegistry` injection one).
15. **Test-only property:** `foresignal.test.adapters.space.base-url` moves every polling URL under one base as
    `/{host}{path}`.
16. **Fixture privacy:** SondeHub uploader callsigns, positions, antennas and altitudes, and SatNOGS owners,
    descriptions and images were replaced (see the READMEs).

## Issues found / open questions

1. **SWPC extended warnings** (`EXTENDED WARNING`, `Extension to Serial Number`) are separate records with the new
   `Now Valid Until`; the extended message is not tombstoned (its own `Valid To` passes). Should an extension retire
   the earlier serial?
2. **The JPL fireball 30-day window was nearly empty** (2 rows on 2026-10-13); the fixture uses a longer capture.
3. **`swpc-xray` versions:** the latest flare re-versions every poll while `currentClass` changes. Making
   `currentClass` volatile would also silence `xray-current`, because `volatileFields` is per source.
4. **US English:** `check_us_english.py` on every new file and README: the remaining hits are the `licence` builder
   (allowed), the `SondeHub-Data-Licence` wiki URL (a page name) and upstream text in `alerts.json`. "cancelled" and
   "centres" were corrected in code. The workspace guard `test_us_english.py` ran 14 tests, OK.
5. **`docs/Source-Catalog.html`** was not regenerated (as in 070–076).
6. **Instructions in data:** none. I read the SWPC files, the DONKI answers and API docs, the JPL answers, the SatNOGS
   answers and API page, the SondeHub answers and wiki, and the auto_rx example `station.cfg` lines
   (`payload_summary_*`). Nothing addressed an agent.

**Owner questions (non-blocking):**
1. Accept `kp-official` `observedAt` at the period end (Deviation 2)?
2. Accept `nasa-donki` 12/min, and should the daily budget be raised for all 11 types (Deviation 3)?
3. Should extended warnings tombstone the serial they extend (Issue 1)?
4. Radiosondes as `bulk-event` (90 d) or `state` (365 d)?

## Tests written (not run)

- **`SwpcMessageParserTest`:** descriptor; the 85-message fixture (82 records, categories, the `WATA30:285` tombstone
  at the cancel's issue time, `Now Valid Until`, threshold and begin times, scale codes, reused serials); header
  helpers (prefixes, scales, message times, inverted windows, cancel lines); hand-made cancel and skips; snapshot;
  fieldSpecs.
- **`SwpcProductsParserTest`:** scales; both Kp entities and the thirds notation; X-ray flare and current; descriptors;
  snapshot; fieldSpecs.
- **`RtswDownsamplerTest`:** active-row choice among SOLAR1, IMAP and ACE; rounding; record from the newer summary
  time; density cadence and `densityInterval` clamp and check; snapshot across noise.
- **`OvationSamplerTest`:** capture values; longitude wrap; the 300 km neighborhood (in and out, across 0°); an
  antimeridian place and a near-pole place; format errors and capping; the aurora record and private descriptor.
- **`DonkiParserTest`:** each event type (parameterized: count, id, `observedAt`, `eventTime`); payloads (flare, CME
  most-accurate analysis, GST `kpMax`, simulation inputs); notifications; the empty SEP array, API error text and
  skips; request URL, `lookbackDays`, type filtering; snapshot.
- **`JplParsersTest`:** fireball signs and string numbers; close approaches with lunar distance and TDB time; the
  edge rows (`h` missing, repeat, missing `des`); empty windows; format errors; descriptors and URLs; snapshots.
- **`SatnogsParserTest`:** fixture; DRF page and skips; `Link` header parsing; volatile `lastSeen` snapshot;
  descriptor.
- **`SondehubParserTest`:** fixture (uploader position and antenna never stored, observer with SNR/RSSI, altitude not
  geometry Z); edge cases (no position, sentinels, skips); params and URL; snapshot.
- **`AutoRxPayloadCodecTest`:** identity; the datagrams (4 records, 1 ignored type, 2 skipped); helpers; fresh
  sessions; snapshot; fieldSpecs.
- **`SpaceAdaptersTest`** (WireMock, real upstream client): conditional GET on every SWPC file (alerts, scales, both
  Kp files, X-ray, RTSW summaries and wind file, OVATION) with 304s; the RTSW wind file fetched once in 55 minutes of
  5-minute polls and again at 60 minutes (`MutableClock`); aurora with no places (no request, `no places`), per place,
  removal and `maxPlaces`; DONKI one request per type, a failing type, an error body; SatNOGS `Link` paging,
  absence tombstones and a foreign link; JPL and SondeHub request parameters.
- **`LocalSondeIT`** (`@IntegrationTest` through `WeatherIngestSupport`, real `StreamingSupervisor`): an allowed
  sender's datagram becomes a record; a disallowed sender is counted in `foresignal.socket.udp.rejected` and nothing
  is stored; a public or missing `allowSenders` is refused at PUT.
- **`SpaceWeatherIngestIT`:** all eleven registered and disabled, `noaa-swpc` and `tinygs` absent; RTSW values that
  round the same are a touch, a real change one version; aurora records per place, the source private.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi` and the Postman generator were not needed: `docs/OpenAPI.json` is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` in the scratchpad, outside the repository, ran `DescriptorValidator` and
  `IngestPolicyValidator` on all eleven descriptors (no problems) and parsed every fixture to confirm the counts and
  values used in the assertions. No project test was executed.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **078 onward:** prompt 077 sources keep their state in the adapter state store; `SpaceSources.conditional` is the
   pattern for multi-file conditional GETs. Radiosonde records must go through `formats.sonde.RadiosondeRecords`;
   prompt 086 extends `local-sonde` (presets, docs) and must not add a second auto_rx listener.
2. **097 checkpoint:**
   - first run of every 077 test;
   - `SpaceWeatherIngestIT` and `LocalSondeIT` use `WeatherIngestSupport` (new property
     `foresignal.test.adapters.space.base-url`);
   - `LocalSondeIT` binds a loopback UDP port and needs the `udp://127.0.0.1:{port}` endpoint accepted by
     `ParamValidator` as a local bind address;
   - check the hand-computed counts and WireMock's decoding of `date-max=%2B60`;
   - 074 Issue 1 (constructors in 070–073) still applies.
3. **106:** the OVATION raster product; `swpc-aurora` only samples the grid at places.
4. **143:** recapture per the fixture READMEs (redact SondeHub uploaders and SatNOGS owners again); re-check the
   SatNOGS filter value and paging, the DONKI 60-day limit, and whether DONKI notification bodies are still `"## "`.
5. **144 (Flyway):** no schema change. Adapter state:
   - `swpc-alerts`: `etag:alerts`, `lastmod:alerts`, `etag:scales`, `lastmod:scales`;
   - `swpc-kp`: `etag:kp-1m`, `lastmod:kp-1m`, `etag:kp-3h`, `lastmod:kp-3h`;
   - `swpc-rtsw`: `etag:/lastmod:` `speed`, `mag-field`, `wind-1m`; `rtsw:speed`, `rtsw:field`, `rtsw:density`,
     `rtsw:densityReadAt`;
   - `swpc-xray`: `etag:xray-flares`, `lastmod:xray-flares`;
   - `swpc-aurora`: `etag:ovation`, `lastmod:ovation`, `live:places`, `gone:places`, `aurora:places`;
   - `satnogs-stations`: `absence:ids`, `absence:missed`.
6. **Owner decisions pending:** the four questions above, plus those carried from 012–076.
