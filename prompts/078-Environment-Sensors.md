# 078 — Environment Sensors

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/078-CR-Environment-Sensors.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/077-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `openaq`, `safecast-radiation`, `epa-radnet`, `ndbc-buoys`
- `docs/design-history/Gap-Analysis.md` §4a row "Local air quality" (PurpleAir/airrohr/AirGradient LAN poll; separate indoor kind), §8.C item 39 (`sensor-community`, `airnow-hourly`, `airq-local`)
- `docs/design-history/Core-Data-Model.md` §2 ("Measurements (OpenAQ, NDBC, EIA)" row: eventTime = measurement period, observedAt = measurement time, ttlAnchor OBSERVED), §3 (limits, null island), §5.1 (SAMPLED validation), §5.3
- Prompt 004 `LAN_URL` param type and prompt 005 pinned-IP connect; prompt 039 `private` flag; prompt 059 (device presets: the airrohr **push** preset stays there; this prompt adds LAN **polling**)
- Upstream docs: OpenAQ v3 (`docs.openaq.org`), Safecast (`api.safecast.org`, realtime `tt.safecast.org`), EPA RadNet (`www.epa.gov/radnet`), NDBC (`www.ndbc.noaa.gov/faq/measdes.shtml`), Sensor.Community API (`github.com/opendata-stuttgart/meta/wiki/EN-APIs`), AirNow API (`docs.airnowapi.org`), PurpleAir local JSON, AirGradient local server API, airrohr firmware

## Objective

Add environmental measurement sources: air quality (OpenAQ, Sensor.Community, AirNow), radiation (Safecast, EPA RadNet), marine buoys (NDBC), and the operator's own LAN air-quality monitors with a separate indoor kind. All are point measurements on a fixed measurement-time base with freshness TTLs.

## Tasks

Common: package `ai.foresignal.source.adapters.env.<name>`; `historyTier=STANDARD`; policy ALL unless stated; time base per Core-Data-Model §2 "Measurements": `observedAt` = measurement time, `eventTime` = measurement period end (or the same instant when no period is published), `ttlAnchor=OBSERVED`; `fieldSpecs` with UCUM units on every numeric field (`ug/m3`, `[ppm]`, `Cel`, `%`, `hPa`, `m/s`, `m`, `s`, `{counts}/min`, `nSv/h`); skip measurements older than param `maxAge` (default 6 h; RadNet 24 h) and count them; all sources **disabled** by default.

### 1. `openaq`

- Required credential `apiKey`, header `X-API-Key` (verified 2026-10: 401 without it). Published limits for free keys: 60 requests/min and 2,000/hour (confirm on `docs.openaq.org` during capture) → budget 20/min, 2000/day.
- Param `parameters` (string-list, default `pm25`; allowed `pm25, pm10, o3, no2, so2, co`). Resolve names → ids once per 24 h from `GET /v3/parameters` (cache in state; do not hard-code ids).
- Fetch `GET https://api.openaq.org/v3/parameters/{id}/latest?limit=1000&page={n}` until a short page (max 30 pages). Optional param `bbox` filters after parsing (check during capture whether the endpoint accepts a server-side `bbox`; use it if so).
- Record `kind = air-quality`, `externalId = locationsId + ":" + parameter`, point from `coordinates`, payload `parameter, value, unit, sensorId, locationId`; `observedAt = eventTime = datetime.utc`. TTL 3 h. Interval 30 min (min 15 min). Attribution "OpenAQ" plus "data licences vary by provider (see each location's `licenses`)"; `licence = "various (OpenAQ providers)"`.

### 2. `safecast-radiation`

- Use Safecast's realtime fixed-device feed `https://tt.safecast.org/devices` (verified 2026-10: 1.4 MB JSON array; fields `device_urn, device_class, device, when_captured, loc_lat, loc_lon, loc_name, loc_country`, radiation fields `lnd_7318u`, `lnd_7128ec`, `lnd_712u` in CPM, plus air fields on Airnote devices). The historical `api.safecast.org/measurements.json` returned 204 for an unfiltered newest-first query and mixes mobile bGeigie survey points; do not use it for this source.
- Keep only devices with at least one `lnd_*` field; `externalId = device_urn`; point; payload `cpm` (per tube field present), `doseRateUsvH` (CPM ÷ 334 for `lnd_7318u`, ÷ 108 for `lnd_7128ec`; document the factors in `fieldSpecs` as approximate), `locationName, country, deviceClass`; `observedAt = eventTime = when_captured`; skip `when_captured` older than 48 h. TTL 6 h.
- Interval 30 min (min 15 min), budget 1/min, 100/day, `mbPerDay` 100. Licence CC0 1.0 (Safecast).

### 3. `epa-radnet`

- `https://radnet.epa.gov/cdx-radnet-rest/api/rest/csv/{year}/fixed/{state}/{city}` (verified 2026-10: one CSV per station-year, 600 KB by October for Denver, `Content-Type: text/text`, no ETag). Columns: `LOCATION_NAME, SAMPLE COLLECTION TIME` (`MM/dd/yyyy HH:mm:ss`, local standard time of the station: map each station to its zone in a bundled table), `DOSE EQUIVALENT RATE (nSv/h)` (often empty), `GAMMA COUNT RATE R02…R09 (CPM)`, `STATUS`.
- Param `stations` (string-list of `STATE/CITY`, **required**, ≤ 10, validated against a bundled station list with coordinates `radnet/stations.json` built from EPA's monitor list during capture). Each run downloads the station CSVs and keeps only the **last** row: one request per station → interval 3 h (min 1 h), budget 2/min, 100/day, `mbPerDay` 150. Handle the year rollover (on 1 January also read the previous year's file once).
- Record `kind = radiation`, `externalId = STATE/CITY`, payload `doseRateNsvH, grossCpm` (sum of R02–R09), `bands` (R02–R09 map), `status` (`APPROVED|PRELIMINARY`); TTL 24 h. Public domain.

### 4. `ndbc-buoys`

- `https://www.ndbc.noaa.gov/data/latest_obs/latest_obs.txt` (verified 2026-10: 104 KB fixed-width text, ETag + Last-Modified; a `#STN LAT LON YYYY MM DD hh mm WDIR WSPD GST WVHT DPD APD MWD PRES PTDY ATMP WTMP DEWP VIS TIDE` header, a units row, `MM` = missing). Parse by whitespace split against the header; missing → omit.
- Station names and types: `https://www.ndbc.noaa.gov/activestations.xml` once per 24 h (second request, cached in state).
- Record `kind = buoy-observation`, `externalId = STN`, point, payload `name, stationType, owner, windDirDeg, windSpeedMs, gustMs, waveHeightM, dominantPeriodS, averagePeriodS, meanWaveDirDeg, pressureHpa, pressureTendencyHpa, airTempC, waterTempC, dewPointC, visibilityNmi, tideFt`; `observedAt = eventTime` from the UTC date/time columns. Station `15009` reports lat `0.000` / lon `-3.051`: valid (not exactly 0,0). TTL 3 h. Interval 10 min (min 5 min), budget 2/min, 300/day. Public domain.

### 5. `sensor-community`

- Verified 2026-10: the global dumps `data.sensor.community/static/v2/data.json` (5-min averages) and `data.1h.json` are 8.5–8.7 MB each with only Last-Modified; polling them every few minutes would cost gigabytes a day. Use the area filter API instead: `https://data.sensor.community/airrohr/v1/filter/{area}`, where param `area` (**required**) is one of `box=lat1,lon1,lat2,lon2`, `area=lat,lon,radiusKm`, or `country=XX` (validated; box ≤ 10°×10°). Berlin's box returned 510 KB.
- Records per sensor: `externalId = sensor.id`, point from `location.latitude/longitude` (strings), `kind = air-quality` or `indoor-air-quality` when `location.indoor == 1` (indoor sensors are dropped unless param `includeIndoor=true`), payload `sensorType` (`sensor.sensor_type.name`), `pm10` (`P1`), `pm25` (`P2`), `tempC, humidityPct, pressureHpa` from `sensordatavalues`, `exactLocation` (bool). `observedAt = eventTime = timestamp` (UTC, `yyyy-MM-dd HH:mm:ss`). Several readings per sensor in one response: keep the newest. TTL 1 h.
- Interval 5 min (min 5 min), budget 2/min, 400/day. Licence ODbL 1.0 (database) / DbCL (contents); attribution "Sensor.Community".

### 6. `airnow-hourly`

- Required credential `apiKey` (verified 2026-10: 401 without it), sent as query param `API_KEY` (redacted by prompt 005). Endpoint `https://www.airnowapi.org/aq/data/?startDate={hourUtc}&endDate={hourUtc}&parameters={list}&BBOX={minLon,minLat,maxLon,maxLat}&dataType=B&format=application/json&verbose=1&monitorType=0&includerawconcentrations=0`. Params `parameters` (default `PM25,OZONE`), `bbox` (required). Published limit 500 requests/hour per key (confirm in the AirNow docs) → budget 2/min, 100/day.
- Record `kind = air-quality`, `externalId = FullAQSCode + ":" + Parameter`, point, payload `parameter, value, unit, aqi, aqiCategory, siteName, agency`; `observedAt = eventTime = UTC` (hour). TTL 3 h. Interval 60 min (data post at about :25 past; schedule with an offset of 30 min). Attribution "U.S. EPA AirNow; data are preliminary and not fully verified". The keyless hourly files under `files.airnowtech.org` exist (verified) but are not used.

### 7. `airq-local-1` … `airq-local-4` (LAN polling)

- A family of four operator-configured slots (precedent: `local-adsb-*`, LAN URLs are the documented exception to static feed lists). Params: `deviceType` (`purpleair|airrohr|airgradient`, required), `url` (`LAN_URL`, required; connects to the pinned IP with redirects off), `lat`, `lon` (required for airrohr and AirGradient, optional override for PurpleAir), `indoor` (boolean; default derived per device), `name`.
- Endpoints and mapping (check each against a real device during capture; firmware varies):
  - PurpleAir: `GET {url}/json?live=true`; fields `SensorId, DateTime, lat, lon, place` (`inside|outside`), `pm2_5_atm`, `pm2_5_atm_b` (B channel), `pm1_0_atm, pm10_0_atm, current_temp_f` (convert to °C; the sensor reads warm, keep raw), `current_humidity, pressure`. `indoor` default from `place`. Flag `channelDisagreement` when A/B differ by > 5 µg/m³ and > 70%.
  - airrohr (Sensor.Community firmware): `GET {url}/data.json`; `software_version`, `sensordatavalues[{value_type, value}]` (`SDS_P1`, `SDS_P2`, `BME280_temperature`, …). No location: use params. `indoor` default false.
  - AirGradient: `GET {url}/measures/current`; fields `serialno, model, pm01, pm02, pm10, rco2, atmp, rhum, tvocIndex, noxIndex, firmware`. `indoor` default from `model` (`I-` prefix indoor, `O-` outdoor).
- Records: `externalId = deviceType + ":" + device serial` (or the slot id), point, `kind = indoor-air-quality` when indoor else `air-quality`; payload `pm1, pm25, pm10, tempC, humidityPct, pressureHpa, co2Ppm, tvocIndex, noxIndex, deviceModel, channelDisagreement`.
- Time base: `observedAtMode=RECEIVED` for every slot (devices disagree on clocks; a LAN poll is near-instant). TTL 10 min, OBSERVED anchor.
- Policy (deviation from §5.3, justified by 60-s LAN sampling): `SAMPLED`, `minInterval` 60 s, `keyframe` 8 min (`≤ ttl − pollInterval − 30 s` = 600 − 60 − 30 = 510 s), `significantFields = deviceModel, channelDisagreement`, thresholds `pm25` 5, `pm10` 10, `co2Ppm` 100, `tempC` 1, `humidityPct` 5 (thresholds are not significant; the validator checks this).
- Interval 60 s (min 30 s), budget 4/min, 3000/day. `private=true` (home location) and `egress=LOCAL_ONLY` with `egressOverridable=true` (prompt 062 field, prompt 069 convention for local receivers). The slot count follows `foresignal.local-receivers.slots` if prompt 069 added it (default 4 here otherwise).

### 8. Fixtures, docs

- Real captures (OpenAQ and AirNow with your key, removed from the README; Safecast trimmed to 20 devices incl. an Airnote without `lnd_*`; RadNet last 50 rows; NDBC first 40 lines; Sensor.Community a small box), plus device JSON samples for the three LAN types from real units or vendor docs, with the firmware version noted.
- `docs/Source-Catalog.md`: rows for all sources (key requirements as verified, the Sensor.Community filter decision, the Safecast realtime choice, RadNet request cost, the indoor kind and private default for `airq-local-*`).
- `docs/Operator-Guide.md`: "Local air-quality monitors" (enabling local APIs on each device, LAN_URL rules).
- No API change.

## Tests to write (do not run)

- Parser tests per source with real fixtures: `OpenAqLatestParserTest`, `SafecastDevicesParserTest` (radiation filter, dose factors), `RadnetCsvParserTest` (last row, local time → UTC, empty dose column), `NdbcLatestObsParserTest` (`MM`, station 15009), `SensorCommunityParserTest` (newest reading per sensor, indoor handling), `AirNowParserTest`, `AirqLocalParsersTest` (each device type, °F → °C, indoor inference, A/B disagreement); each with `SnapshotContract.assertStableStaticHash` and the fieldSpecs contract.
- `OpenAqParameterResolverTest`: name → id cache.
- Adapter tests (WireMock): API keys in header/query and redacted in traces; NDBC conditional GET and the daily station-list request; Sensor.Community `area` validation; RadNet one request per station.
- `AirqLocalIT` (`@IntegrationTest`, WireMock bound to 127.0.0.1): `LAN_URL` accepted for loopback, public URL rejected; a 2 µg/m³ change creates no version, a 6 µg/m³ change creates a SAMPLE version; the source is private.

## Acceptance criteria

- `openaq` and `airnow-hourly` require credentials; `sensor-community` requires `area`; `epa-radnet` requires `stations`; the four `airq-local-*` slots validate LAN URLs and are private.
- No source downloads the global Sensor.Community dumps.
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
2. **Commit** all changes with the message `078: Environment Sensors`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/078-CR-Environment-Sensors.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `078: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
