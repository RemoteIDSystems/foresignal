# 078-CR-Environment-Sensors

| | |
|---|---|
| Prompt | 078-Environment-Sensors.md |
| Status | Complete |
| Implementation commit | 2f06cab0454581b310b1c6ca54625ffacbddf2af |
| Date | 2026-10-13 |

## Summary

Seven environmental sources are added under `ai.foresignal.source.adapters.env`, one sub-package each. All are
point measurements on the Core-Data-Model §2 measurement time base (`observedAt` = measurement time, `eventTime` the
same instant, `ttlAnchor=OBSERVED`), STANDARD, `state` retention (365 d) and disabled by default. Every numeric
field has a UCUM unit; readings older than `maxAge` are skipped and counted (`foresignal.env.skipped{source,reason}`).

**Shared** (`env.EnvSources`):
- the test property `foresignal.test.adapters.env.base-url`;
- the `maxAge` parameter;
- `Collector`: newest reading per entity, skip counts and the `maxAge` cut;
- UCUM unit mapping, the point and box checks, and gzip packing for large state values.

**1. `openaq`** (`OpenAqAdapter`, `OpenAqLatestParser`, `OpenAqParameterResolver`):
- credential `apiKey`, required, sent as the `X-API-Key` header;
- names in `parameters` (default `pm25`) resolve to every v3 id carrying them; the list comes from `/v3/parameters`
  once per 24 h and is cached in state;
- `/v3/parameters/{id}/latest?limit=1000&page={n}&datetime_min={now-maxAge}`, read until a short page (at most 30);
- optional `bbox` filters after parsing;
- externalId `locationsId:parameter`; payload `parameter, value, unit, sensorId, locationId`;
- TTL 3 h, 30 min (min 15), 20/min and 2,000/day.

**2. `safecast-radiation`** (`SafecastRadiationAdapter`, `SafecastDevicesParser`):
- one request for `tt.safecast.org/devices`; only devices with an `lnd_*` tube are kept;
- payload `cpm` (tube → CPM), `doseRateUsvH` (7318u ÷ 334, else 7128ec ÷ 108), `locationName, country, deviceClass`;
- `maxAge` 48 h, TTL 6 h, 30 min, 1/min, 100/day, 100 MB/day. CC0 1.0.

**3. `epa-radnet`** (`EpaRadnetAdapter`, `RadnetCsvParser`, `RadnetStations`):
- `stations` is required, at most 10, checked against the bundled `src/main/resources/radnet/stations.json` (132
  stations);
- one CSV request per station per run; the newest row is kept;
- on 1 January the previous year's file is also read, once per station and year;
- a failing station is logged and skipped; the run fails only when every station failed;
- payload `doseRateNsvH, grossCpm, bands, status`; TTL 24 h, 3 h (min 1 h).

**4. `ndbc-buoys`** (`NdbcBuoysAdapter`, `NdbcLatestObsParser`, `NdbcStationsParser`):
- `latest_obs.txt` by conditional GET; columns found by header name; `MM` values left out;
- `activestations.xml` (name, type, owner) at most once per 24 h, cached in state (gzipped);
- station `15009` at `0.000 -3.051` is kept;
- TTL 3 h, 10 min (min 5), 2/min, 300/day.

**5. `sensor-community`** (`SensorCommunityAdapter`, `SensorCommunityParser`, `SensorCommunityArea`):
- `area` is required and validated: `box` (≤ 10° a side), `area=lat,lon,radiusKm` or `country=XX`;
- only `/airrohr/v1/filter/{area}` is requested, never the global dumps;
- the newest reading per sensor; indoor sensors are dropped unless `includeIndoor`, then `indoor-air-quality`;
- pressure converted from Pa to hPa; TTL 1 h, 5 min, 2/min, 400/day.

**6. `airnow-hourly`** (`AirNowHourlyAdapter`, `AirNowParser`):
- credential `apiKey`, required, sent as the `API_KEY` query parameter (the upstream client redacts it);
- `bbox` is required; `parameters` defaults to `PM25,OZONE`;
- each run reads the hour starting at `truncate(now - 30 min) - 1 h`;
- externalId `FullAQSCode:Parameter`; `-999` means missing; TTL 3 h, 60 min, 2/min, 100/day.

**7. `airq-local-{n}`** (`AirqLocalFamily`, `AirqLocalAdapter`, `AirqLocalParsers`):
- one slot per `foresignal.local-receivers.slots`;
- params `deviceType` (required), `url` (`LAN_URL`), `lat`/`lon` (required for airrohr and AirGradient), `indoor`,
  `name`;
- PurpleAir `/json?live=true` (A/B channel mean, °F → °C, `channelDisagreement`), airrohr `/data.json`, AirGradient
  `/measures/current`;
- the `indoor-air-quality` kind for indoor monitors;
- time base RECEIVED, TTL 10 min;
- SAMPLED: 60 s, keyframe 8 min; significant `deviceModel, channelDisagreement`; thresholds `pm25` 5, `pm10` 10,
  `co2Ppm` 100, `tempC` 1, `humidityPct` 5;
- 60 s (min 30 s), 4/min, 3,000/day; `private`, egress `LOCAL_ONLY` and overridable.

**Docs:**
- `docs/Source-Catalog.md`: rows for all seven, the roadmap lines and a prompt 078 note;
- `docs/Operator-Guide.md`: "Local air-quality monitors";
- `docs/Invariants.md`: SRC-5, SRC-7, EGR-1 and EGR-2 test entries.
- **No API change.** The OpenAPI kind entries already list every payload name used here.

## Files created / modified

- **Created, main:** `source/adapters/env/`:
  - `package-info`, `EnvSources`;
  - `openaq/`: `package-info`, `OpenAqAdapter`, `OpenAqLatestParser`, `OpenAqParameterResolver`;
  - `safecast/`: `package-info`, `SafecastRadiationAdapter`, `SafecastDevicesParser`;
  - `radnet/`: `package-info`, `EpaRadnetAdapter`, `RadnetCsvParser`, `RadnetStations`;
  - `ndbc/`: `package-info`, `NdbcBuoysAdapter`, `NdbcLatestObsParser`, `NdbcStationsParser`;
  - `sensorcommunity/`: `package-info`, `SensorCommunityAdapter`, `SensorCommunityParser`, `SensorCommunityArea`;
  - `airnow/`: `package-info`, `AirNowHourlyAdapter`, `AirNowParser`;
  - `airq/`: `package-info`, `AirqLocalFamily`, `AirqLocalAdapter`, `AirqLocalParsers`.
- **Created, resource:** `src/main/resources/radnet/stations.json`.
- **Created, test:**
  - `env/`: `EnvTestSupport`, `EnvSourcesTest`, `EnvAdaptersTest`;
  - `openaq/{OpenAqLatestParserTest, OpenAqParameterResolverTest}`, `safecast/SafecastDevicesParserTest`;
  - `radnet/RadnetCsvParserTest`, `ndbc/NdbcLatestObsParserTest`, `sensorcommunity/SensorCommunityParserTest`;
  - `airnow/AirNowParserTest`, `airq/{AirqLocalParsersTest, AirqLocalIT}`.
- **Created, fixtures** (each with a README): `openaq/`, `safecast-radiation/`, `epa-radnet/`, `ndbc-buoys/`,
  `sensor-community/`, `airnow-hourly/`, `airq-local/`.
- **Modified, test:**
  - `testsupport/weather/WeatherIngestSupport`: the env base-URL property and `airq-local-1` in the backoff list;
  - `retention/FamilyDefaultsTest`: the seven adapter classes, all `state`.
- **Docs:** `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Invariants.md`.

## Deviations from the prompt

1. **RadNet times are UTC, not station-local standard time.** I fetched the newest rows of six stations at 21:34 UTC
   on 2026-10-13. Honolulu read 20:19, Anchorage 20:41, Denver 20:41, San Francisco 19:48, Boston 20:18 and San Juan
   20:12. Read as local standard time, several of these would be hours in the future. The times are parsed as UTC and
   no zone table is bundled.
2. **RadNet station coordinates are approximate.** EPA's CSV downloads page gives station names but no coordinates.
   `stations.json` therefore lists the 132 stations with a 2026 link and places each at the internal point of its city
   in the US Census Bureau 2024 Gazetteer (places; Edison, NJ from county subdivisions). San Francisco's internal point
   is offshore (−123.03), because the city includes the Farallon Islands.
3. **RadNet budget is 12/min, not 2/min.** A 10-station run needs 10 requests, and the governor refuses rather than
   queues (as in 075–077). The daily budget stays 100 and the daily byte budget 150 MB.
4. **AirNow offset.** The scheduler has no phase offset. The adapter gets the 30-minute offset by choosing the hour
   it requests: `truncate(now - 30 min) - 1 h`.
5. **OpenAQ `datetime_min`.** The v3 OpenAPI document lists `limit, page, datetime_min` for `latest`, and no `bbox`.
   The request therefore adds `datetime_min = now - maxAge`, and `bbox` filters after parsing.
6. **OpenAQ names can map to several ids.** v3 lists `o3` and `no2` in two units, so a name resolves to every id that
   carries it.
7. **The `value` fieldSpec has no unit (OpenAQ and AirNow).** The unit varies per record. It is carried as a UCUM code
   in the payload `unit` (`ug/m3`, `[ppm]`, `[ppb]`).
8. **Safecast tubes.** The live list also has `lnd_7318c` (247 devices) besides the three tubes the prompt names.
   Every `lnd_*` field goes into `cpm`. A dose rate is given only for 7318u and 7128ec, the two tubes whose factors the
   prompt provides. Devices without a location or with an unreadable `when_captured` are skipped. The live list held
   `""`, `2012-00-00…` and `2044-00-00…`.
9. **Two slots by default, not four.** The `airq-local-{n}` count follows `foresignal.local-receivers.slots`, which
   prompt 069 added with a default of 2. The prompt makes the property authoritative when it exists. The OpenAPI kind
   entry still says `airq-local-{1..4}`.
10. **`name` is not stored.** The OpenAPI payload lists for `airq-local` have no `name` field and the prompt makes no
    API change. The `name` parameter only labels log lines.
11. **Retention class.** Every source uses `state` (365 d, sensor status).
12. **Spring constructors.** Each new `@Component` has exactly one `@Autowired` injection constructor:
    - the six polling adapters take `(String, ObjectProvider<MeterRegistry>)`, plus an unannotated
      `(String, Supplier<MeterRegistry>)` constructor for tests;
    - `AirqLocalFamily` takes `(int)`.
13. **Fixture privacy.** I removed the Safecast `device_contact_*` members (contact names and emails).

## Issues found / open questions

1. **Synthetic fixtures.**
   - OpenAQ and AirNow: no key was available. OpenAQ follows its public v3 OpenAPI document. AirNow's query
     documentation is behind its login, so its field names, hour format and the 500/hour limit are unconfirmed.
   - The three LAN device samples come from vendor documentation (PurpleAir firmware 7.04, AirGradient 3.1.3) and the
     059 airrohr fixture.
   - All are marked `synthetic: true` for recapture in prompt 143.
2. **Faulty Sensor.Community sensors are stored as published.** The Berlin capture had a BME280 reporting 162,372 Pa
   and another reporting −146 °C. They are stored unchanged.
3. **Antimeridian boxes.** `ParamValidator` builds a JTS `Envelope`, which reorders `minLon > maxLon`, so a `bbox`
   crossing the antimeridian becomes a box spanning the other way. This was already the case before 078 and affects
   every BBOX parameter. It is not fixed here.
4. **US English.**
   - `check_us_english.py` on every new file: the only hits are the allowed `licence` builder and accessor.
   - The touched EGR-2 row of `docs/Invariants.md` already contained "standardised" (also in `docs/Conventions.md`);
     I did not change it.
5. **`docs/Source-Catalog.html`** was not regenerated (as in 070–077).
6. **Instructions in data:** none. I read the OpenAQ OpenAPI document and docs, the AirNow FAQ, PurpleAir's two
   community articles, AirGradient's local-server document, the Sensor.Community API wiki, EPA's RadNet pages, and
   the NDBC, Safecast and RadNet answers. Nothing in them addressed an agent.

**Owner questions (non-blocking):**
1. Accept UTC for RadNet (Deviation 1), and Gazetteer city points as RadNet station locations (Deviation 2)?
2. Should `airq-local-*` default to 4 slots, independent of `foresignal.local-receivers.slots`? Or should the OpenAPI
   entry say `{1..n}`? (Deviation 9)
3. Should the `airq-local` `name` parameter be stored, with the OpenAPI payload lists amended (Deviation 10)?
4. Should implausible citizen-sensor values be scrubbed (Issue 2)?

## Tests written (not run)

- **`OpenAqLatestParserTest`:**
  - the synthetic page: three records, newest per location, too old, missing and null-island coordinates;
  - UCUM units; skip reasons; format errors; the descriptor; the snapshot.
- **`OpenAqParameterResolverTest`:** name to ids (including double-unit names), the 24 h cache and its damage
  handling, format errors.
- **`SafecastDevicesParserTest`:** the radiation filter (12 of 19 entries with their skip reasons), the repeated
  device, the dose factors, `maxAge`, format errors, the descriptor, the snapshot.
- **`RadnetCsvParserTest`:**
  - last row, UTC time, empty dose column (Denver), dose present (Honolulu), header-only file;
  - out-of-order rows, quoted CSV;
  - the station list and URL encoding, `checkParams`, the descriptor, the snapshot.
- **`NdbcLatestObsParserTest`:** 38 stations, `MM`, station 15009, the minute column, stations without names, too old,
  short and empty rows, format errors, the station-table JSON and packed round trip, the descriptor, the snapshot.
- **`SensorCommunityParserTest`:** newest reading per sensor, indoor handling, Pa to hPa, skips, `area` validation and
  paths, `checkParams`, the descriptor, the snapshot.
- **`AirNowParserTest`:** rows and skips, error objects, the hour chosen, the request URL, the descriptor, the
  snapshot.
- **`AirqLocalParsersTest`:**
  - each device type, °F to °C, indoor inference and override, A/B disagreement, format errors;
  - the slot descriptor (private, `LOCAL_ONLY`, SAMPLED, thresholds), the family bounds, `checkParams`, the snapshot.
- **`EnvSourcesTest`:** the collector, `maxAge`, UCUM codes, points, boxes, rounding, packed state.
- **`EnvAdaptersTest`** (WireMock, real upstream client):
  - OpenAQ: no key means no request; the key in `X-API-Key`, absent from traces, recorded exchanges and the log; the
    parameter cache for 24 h; paging; `bbox`; two ids;
  - AirNow: the key in `API_KEY`, redacted the same way; key and `bbox` required;
  - NDBC: conditional GET and the daily station-list request;
  - Sensor.Community: `area` validation with no request, and no dump requested;
  - RadNet: one request per station, a failing station, all failing, the 1 January rollover;
  - Safecast: a single request.
- **`AirqLocalIT`** (`@IntegrationTest` through `WeatherIngestSupport`, stub reached as `127.0.0.1`):
  - every 078 source is registered and disabled;
  - a loopback `LAN_URL` is accepted and `http://8.8.8.8` refused;
  - +2 µg/m³ creates no version, +6 µg/m³ a SAMPLE version;
  - the slot is private and `LOCAL_ONLY`.
- **Updated:** `FamilyDefaultsTest`.

## Compile result

- `./gradlew compileJava compileTestJava`: BUILD SUCCESSFUL.
- `./gradlew lintOpenApi` and the Postman generator were not needed: `docs/OpenAPI.json` is unchanged.
- No project tests were run (feature prompt).
- **Scratch check:** a throwaway `main` in the scratchpad, outside the repository, ran `DescriptorValidator` on all
  eight descriptors (no problems, no warnings) and parsed every fixture to confirm the counts and values used in the
  assertions. No project test was executed.
- **Workspace guard** `python3 ~/Documents/GitLab/meshdtn/style/test_us_english.py`: ran 14 tests, OK.

## Notes for the next prompt

1. **079 onward:** environmental sources keep their caches in the adapter state store. `EnvSources.Collector` is the
   newest-per-entity pattern for point measurements. Adapters do not import each other; the `airq-local` family
   reads only the slot-count constants of `local.LocalReceivers`.
2. **097 checkpoint:**
   - first run of every 078 test;
   - `AirqLocalIT` uses `WeatherIngestSupport` (new property `foresignal.test.adapters.env.base-url`) and reaches
     the stub server as `127.0.0.1`;
   - check the hand-computed counts, WireMock's matching of `datetime_min=2026-10-13T15:35:00Z` and of the
     Sensor.Community filter path containing `=` and `,`;
   - 074 Issue 1 (constructors in 070–073) still applies.
3. **143:**
   - recapture per the fixture READMEs: OpenAQ and AirNow with real keys (`FORESIGNAL_VERIFY_*`), the LAN devices from
     real units;
   - remove the Safecast contact members again;
   - confirm the AirNow field names and limit, and re-check that RadNet times are still UTC.
4. **144 (Flyway):** no schema change. Adapter state:
   - `openaq`: `openaq:parameters`, `openaq:parametersAt`;
   - `ndbc-buoys`: `etag:latest-obs`, `lastmod:latest-obs`, `ndbc:stations` (gzip above 32 KB), `ndbc:stationsAt`;
   - `epa-radnet`: `rollover:{STATE/CITY}`.
5. **Owner decisions pending:** the four questions above, plus those carried from 012–077.
