# 077 — Space Weather And Ground Stations

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/077-CR-Space-Weather-And-Ground-Stations.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/076-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `noaa-swpc`, `satnogs-stations`, `tinygs`, `sondehub`
- `docs/design-history/Gap-Analysis.md` §4b row "SWPC extension + DONKI" (alerts, RTSW downsampled, X-ray, aurora at places; DONKI without a key), §4a row "radiosonde_auto_rx" (PAYLOAD_SUMMARY over UDP), §8.C item 38
- `docs/design-history/Weather-Imagery-Design.md` §5.3 (the OVATION **raster** product belongs to prompt 106; this prompt only samples it at places)
- `docs/design-history/Core-Data-Model.md` §2 (time contract, "Measurements" row), §3, §5.3
- Prompt 036 `PlaceService` (places), prompt 039 `private` flag, prompt 069 socket transport (UDP listener with sender allowlist, codec SPI, LAN validator, local-only egress default)
- Upstream docs: SWPC data services (`services.swpc.noaa.gov`, `www.swpc.noaa.gov/content/data-access`), DONKI API (`ccmc.gsfc.nasa.gov/DONKI-API`), JPL SSD APIs (`ssd-api.jpl.nasa.gov/doc/fireball.html`, `/doc/cad.html`), SatNOGS Network API (`docs.satnogs.org/projects/satnogs-network/en/latest/api.html`), SondeHub v2 API (`github.com/projecthorus/sondehub-infra/wiki`), radiosonde_auto_rx wiki (GPL-3.0: reference only)

## Objective

Replace the single `noaa-swpc` placeholder with focused SWPC sources (alerts and scales, Kp, downsampled solar wind, X-ray flares, aurora probability at places), add NASA DONKI events, JPL fireballs and asteroid close approaches, SatNOGS ground stations, SondeHub radiosondes and a local radiosonde receiver over UDP. Keep every poll small: several SWPC files are megabytes and change every minute.

## Tasks

Common: all sources in `ai.foresignal.source.adapters.space` (one sub-package each), `historyTier=STANDARD`, policy ALL, all **disabled** by default, public domain unless stated.

### 1. SWPC sources

All under `https://services.swpc.noaa.gov/`; every file verified 2026-10 to send ETag + Last-Modified and `Cache-Control: max-age=60`, so always use conditional GET. Timestamps without a zone are UTC.

| Source id | Files | Records | Interval · budget |
|---|---|---|---|
| `swpc-alerts` | `products/alerts.json` (53 KB), `products/noaa-scales.json` (1 KB) | `space-weather-alert` per message; `space-weather-scales` single entity `current` | 5 min · 2/min, 600/day |
| `swpc-kp` | `json/planetary_k_index_1m.json` (28 KB), `products/noaa-planetary-k-index.json` (5 KB) | `geomagnetic-index`: entity `kp-estimated` (latest 1-min row) and `kp-official` (latest 3-hour row) | 5 min · 2/min, 600/day |
| `swpc-rtsw` | `products/summary/solar-wind-speed.json`, `products/summary/solar-wind-mag-field.json` (≈60 bytes each); `json/rtsw/rtsw_wind_1m.json` (2.7 MB) at most hourly | `solar-wind`, single entity `l1` | 5 min · 4/min, 900/day, `mbPerDay` 80 |
| `swpc-xray` | `json/goes/primary/xray-flares-latest.json` (0.5 KB) | `solar-flare` per flare; `xray-current` single entity | 5 min · 2/min, 600/day |
| `swpc-aurora` | `json/ovation_aurora_latest.json` (930 KB) | `aurora-probability` per place | 15 min · 1/min, 100/day, `mbPerDay` 100 |

- **`swpc-alerts`:** `[{product_id, issue_datetime, message}]`. `SwpcMessageParser` (pure) reads `Space Weather Message Code:` (e.g. `ALTK05`, `WARK04`, `WATA20`, `SUMX01`), `Serial Number:`, `Issue Time:`, `NOAA Scale:`, `Valid From:`/`Valid To:`, `Threshold Reached:`/`Begin Time:` and `CANCEL`/`Cancel Serial Number:`. `externalId = code + ":" + serial`; `category` from the code prefix (`ALT` alert, `WAR` warning, `WAT` watch, `SUM` summary, `CAN` cancel); `observedAt = issue_datetime`; `eventTime` = threshold/begin time when present; `validFrom`/`validUntil` from Valid From/To (upstream-asserted); a cancel message tombstones the referenced serial. Payload `code, category, serial, noaaScale, kIndex` (parsed when present), `message` (CR/LF normalised, ≤ 4 KB). `embedFields = message`, `recordTtl` 7 d (RECEIVED). The scales entity carries `R`, `S`, `G` current values and day 1–3 probabilities; `observedAt` from `DateStamp`+`TimeStamp`.
- **`swpc-kp`:** `observedAt = time_tag` of the chosen row; `eventTime` = the same (measurement); payload `kp` (number), `kpText` (`6M`), `aRunning`, `stationCount`. TTL 3 h, `ttlAnchor=OBSERVED`.
- **`swpc-rtsw` (downsampled):** verified 2026-10 the old `products/solar-wind/*` 5-minute files are gone (404) and the RTSW 1-minute files are 1.5–2.7 MB with rows from several spacecraft (`source` `IMAP`, `ACE`, …) flagged by `active`. Every 5 min read the two summary files; once per `densityInterval` (param, default 60 min, min 30 min) read `rtsw_wind_1m.json` and take the newest row with `active=true` for `density`/`temperature`. Payload rounded so noise does not create versions: `speedKms` (nearest 10), `btNt`, `bzNt` (nearest 0.5), `densityCc` (0.1), `spacecraft`. `observedAt` = the newer summary `time_tag`; TTL 30 min OBSERVED.
- **`swpc-xray`:** flare entity `externalId = begin_time + ":" + satellite`, payload `beginTime, maxTime, endTime, beginClass, maxClass, currentClass, satellite`; `eventTime = begin_time`, `observedAt = time_tag`. `xray-current`: `currentClass`, `currentRatio`. `recordTtl` is per source, so the source has none (flares are history; the current entity is refreshed every poll).
- **`swpc-aurora` (at places):** OVATION grid `coordinates: [[lon 0..359, lat -90..90, probability]]` with `Observation Time` and `Forecast Time`. Parse once into a 360×181 `byte[]`. For each place from prompt 036 `PlaceService` (point, or polygon/route centroid; at most 50, param `maxPlaces`), emit `externalId = place id`, geometry = the place point, payload `placeName, probability` (cell at rounded lon mod 360 / lat), `maxWithin300Km` (max over cells within 300 km), `forecastTime`. `observedAt = Observation Time`, `eventTime = Forecast Time`, TTL 1 h OBSERVED. If `PlaceService` is absent, return an empty complete result and set health reason "no places". Places reveal operator locations: declare `private=true` (prompt 008/039 flag). Rendering the OVATION grid as a raster stays in prompt 106.

### 2. `nasa-donki`

- Verified 2026-10: the old `kauai.ccmc.gsfc.nasa.gov/DONKI/WS/get/*` endpoints now redirect (301) to a CCMC announcement page; the documented API is `https://ccmc.gsfc.nasa.gov/DONKI-API/get/{type}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` (OpenAPI at `/DONKI-API/v3/api-docs`), keyless, no ETag. Types: `FLR, CME, CMEAnalysis, GST, IPS, SEP, MPC, RBE, HSS, WSAEnlilSimulations, notifications`.
- Param `types` (string-list, default `FLR, CME, GST, IPS, SEP`), `lookbackDays` (default 7). One request per type per run.
- Record `kind = space-weather-event` (`notifications` → `space-weather-notice`); `externalId` = `flrID` / `activityID` / `gstID` / `sepID` / … / `messageID`; payload `type, catalog, beginTime, peakTime, endTime, classType, sourceLocation, activeRegionNum, instruments` (display names), `kpMax` (max of GST `allKpIndex[].kpIndex`), `cmeSpeedKms, cmeHalfAngle` (from the most-accurate analysis), `linkedEvents` (ids), `link, versionId`, notices add `messageType, messageBody` (≤ 4 KB, `embedFields`, untrusted). `observedAt = submissionTime` (`messageIssueTime` for notices), `eventTime = beginTime|startTime|eventTime`. Interval 30 min, budget 2/min, 400/day. Licence: NASA public data.

### 3. JPL small bodies

- **`jpl-fireballs`:** `https://ssd-api.jpl.nasa.gov/fireball.api?date-min={today-30d}&req-loc=true` (verified 2026-10: signature version 1.2; fields `date, energy, impact-e, lat, lat-dir, lon, lon-dir, alt, vel`, values as strings). `kind = fireball`, `externalId = date`, point from lat/lon with N/S and E/W signs, payload `radiatedEnergyJ` (`energy × 1e10`), `impactEnergyKt, altitudeKm, velocityKms`. `eventTime = date`; `observedAtMode=RECEIVED` (rows are revised without a revision stamp). Interval 6 h, budget 1/min, 10/day.
- **`jpl-close-approaches`:** `https://ssd-api.jpl.nasa.gov/cad.api?date-min=now&date-max=%2B60&dist-max={distMaxAu}&fullname=true` (param `distMaxAu`, default 0.05; verified 2026-10: version 1.5, fields `des, orbit_id, jd, cd, dist, dist_min, dist_max, v_rel, v_inf, t_sigma_f, h`). `kind = close-approach`, no geometry, `externalId = des + "@" + cd`; payload `designation, fullName, distAu, distLunar` (`dist × 389.17`), `distMinAu, distMaxAu, vRelKms, absMagnitude, orbitId, timeSigma`; `eventTime = cd` (TDB; note the ≈69 s offset from UTC in `fieldSpecs` description), `observedAtMode=RECEIVED`, `recordTtl` 7 d. Interval 12 h, budget 1/min, 10/day.

### 4. Ground stations

- **`satnogs-stations`:** `https://network.satnogs.org/api/stations/?status=2` (Online), follow the DRF `Link: rel="next"` header (max 20 pages). The endpoint timed out from the verification host on 2026-10-10: set `fetchTimeout` 120 s and keep the interval at 6 h. Record `kind = ground-station`, `externalId = id`, point `lat/lng`, payload `name, status, altitudeM, qthLocator, antennas` (band/type list), `clientVersion`; declare `lastSeen` volatile; leave out `observations` (raw counter). `observedAtMode=RECEIVED`. Licence CC BY-SA 4.0 (SatNOGS states all API data is freely distributed under CC BY-SA).
- **`tinygs`: not in this prompt.** Verified 2026-10: `api.tinygs.com/v1/satellitesWorldmap` answers 403 `MISSING_CLIENT_TIMESTAMP` to non-browser clients and TinyGS publishes no API terms. List it under "External provider ideas"; an EDP provider can serve it.

### 5. Radiosondes

- **`sondehub`:** `https://api.v2.sondehub.org/sondes?lat={lat}&lon={lon}&distance={radiusM}&last={windowS}` (verified 2026-10: object keyed by serial; fields `serial, type, subtype, manufacturer, datetime, lat, lon, alt, vel_v, vel_h, heading, temp, humidity, pressure, frequency, batt, sats, frame, uploader_callsign, uploader_position, time_received`). Params `center` (lat,lon; default: the first place from `PlaceService`, else required), `radiusKm` (default 500, max 1500), `windowS` (default 600). The SondeHub docs ask clients not to poll the full telemetry endpoint regularly: use only `/sondes`, interval 5 min (min 2 min), budget 2/min, 400/day.
  - Record `kind = radiosonde`, `externalId = serial`, point + payload `altitudeM` (altitude convention from the Adapter Guide: metres in the payload, not geometry Z), `type, subtype, manufacturer, verticalSpeedMs, horizontalSpeedMs, headingDeg, tempC, humidityPct, pressureHpa, frequencyMHz`. `observedAt = datetime`, TTL 30 min, `ttlAnchor=OBSERVED`. `uploader_callsign` → `Observer.receiverId` (with `snr` when present); **never** store `uploader_position` or `uploader_antenna` (receiver locations of private stations). Leave out `frame` and `batt` (counters / noise). Licence CC BY-SA 2.0 (SondeHub data licence); sensor values are uncalibrated (say so in `description`).
- **`local-sonde` (socket transport, prompt 069):** a STREAMING source that listens for radiosonde_auto_rx `PAYLOAD_SUMMARY` UDP datagrams. auto_rx broadcasts them by default (`payload_summary_enabled = True`, `payload_summary_host = <broadcast>`, `payload_summary_port = 55673` in `station.cfg`; verified 2026-10 in the project's example config).
  - Params (prompt 069 conventions): `endpoint` (`LAN_URL` `udp://bindHost:port`, **required**; document 55673 as auto_rx's default port, do not assume it; `bindHost` `0.0.0.0`, `::` or a local interface address) and `allowSenders` (string-list of IPs/CIDRs, **required**, private/loopback/link-local/ULA only). Listen with `ctx.sockets().udp(bind, allowSenders, handler)`; never construct sockets directly.
  - Codec `sonde-udp` (the name prompts 069 and 086 reserve) via 069's codec SPI: one JSON object per datagram with `type == "PAYLOAD_SUMMARY"` (ignore other types), fields `station, callsign` (sonde serial), `latitude, longitude, altitude, speed` (km/h), `heading, time, datetime` (ISO), `model, freq, temp, frame`, optional `humidity, pressure, sats, batt, snr`. Write the codec from the JSON field list only; auto_rx is GPL-3.0 and no code is copied.
  - Records use the same `radiosonde` kind and payload names as `sondehub` (`externalId = callsign`), `observedAt = datetime`, `receivedAt` stamped by the sink, `station` → `Observer.receiverId`. Set `egress=LOCAL_ONLY` with `egressOverridable=true` (prompt 069's default for local receivers). Prompt 086 adds auto_rx presets and docs on top of this source; it must not add a second auto_rx listener.

### 6. Fixtures, docs

- Real captures for every polled file (trim large ones: OVATION to a band of rows plus the header; RTSW 1m to 20 rows incl. inactive spacecraft), README with dates and URLs; a recorded set of `PAYLOAD_SUMMARY` datagrams (generate with a small script from the field list, not from auto_rx code). Edge cases: SWPC cancel message, DONKI empty array, CAD with `h` missing, SondeHub record without position.
- `docs/Source-Catalog.md`: replace `noaa-swpc` with the five SWPC rows; add DONKI, JPL, SatNOGS, SondeHub, `local-sonde`; record the DONKI endpoint move, the removed SWPC files, the TinyGS exclusion.
- No API change.

## Tests to write (do not run)

- `SwpcMessageParserTest` (codes, serials, valid windows, cancel → tombstone), `RtswDownsamplerTest` (active-row choice, rounding, density cadence), `OvationSamplerTest` (lon wrap, 300 km max, antimeridian place), `DonkiParserTest` (each type), `JplParsersTest` (signs, string numbers, lunar distance), `SatnogsParserTest`, `SondehubParserTest` (uploader position never stored), `AutoRxPayloadCodecTest` (non-summary types ignored, malformed JSON counted): real fixtures, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- Adapter tests (WireMock): conditional GET on every SWPC file; RTSW large file fetched at most once per `densityInterval` (`MutableClock`); DONKI one request per type; SatNOGS Link-header paging.
- `LocalSondeIT` (`@IntegrationTest`): datagrams from an allowed sender become records; a disallowed sender is dropped and counted; `allowSenders` with a public address is rejected at PUT.
- `SpaceWeatherIngestIT`: rounded RTSW values unchanged across polls create no versions; aurora records exist per place and the source is private.

## Acceptance criteria

- `swpc-alerts`, `swpc-kp`, `swpc-rtsw`, `swpc-xray`, `swpc-aurora`, `nasa-donki`, `jpl-fireballs`, `jpl-close-approaches`, `satnogs-stations`, `sondehub` and `local-sonde` register, disabled; `noaa-swpc` and `tinygs` are not registered.
- No adapter polls `rtsw_*_1m.json` more often than hourly by default or contacts `kauai.ccmc.gsfc.nasa.gov`.
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
2. **Commit** all changes with the message `077: Space Weather And Ground Stations`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/077-CR-Space-Weather-And-Ground-Stations.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `077: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
