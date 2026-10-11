# 053 — MRMS Second Wave And Gridpoints

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/053-CR-MRMS-Second-Wave-And-Gridpoints.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/052-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §1.2 (second wave), §2.4 (kind retention, MRMS ladder, quantisation), §2.5 (`mrms`, `nws-gridpoints` budgets), §5.1 (record sources: `spc-storm-reports`, `nws-gridpoints`), §5.2 (second wave), §7.2 (place-weather `precip` and `forecast` sections), §9 (second-wave budget ≈ 0.5 GB/day). Design prompt 040 = v1.2 053; "old 051" = 089.
- `docs/Core-Data-Model.md` §2 (time mapping), §3 (descriptor fields, adapter rules 7–13), §5.3 (ALL/STANDARD defaults).
- `docs/Source-Catalog.md` rows `spc-storm-reports` (line ~120) and the "→ 051" list (line ~102); `nws-alerts` notes (User-Agent).
- Completion Reports of **046** (`mrms` source, u8 scheme, `RasterDecoder`), **045** (styles, legends), **047** (`nws-alerts` UA/contact handling), **036** (places), **052** (`wx-places`, SampleService, forecast series cache).
- Upstream: `https://noaa-mrms-pds.s3.amazonaws.com/` (CONUS prefixes), `https://www.weather.gov/documentation/services-web-api` (`/points`, `/gridpoints`), `https://www.spc.noaa.gov/climo/reports/` (`today.csv`, `yesterday.csv`).

## Objective

Add the P1 hydrology and severe-storm layers: four more MRMS products on the existing `mrms` source (rotation, QPE 1 h and 24 h, flash-flood guidance ratio), an `nws-gridpoints` source that gives each place an official NWS forecast, and `spc-storm-reports` (moved here from 089). Feed the new data into the `precip` and `forecast` sections of `place-weather`.

## Tasks

### 1. MRMS second wave (extend the `mrms` source from 046)

Verified 2026-10 by S3 listing (sizes per file, gzip):

| Product | Prefix | Upstream cadence / size | Stored |
|---|---|---|---|
| `mrms-rotation-60m` | `CONUS/RotationTrack60min_00.50/` | 2 min, ~0.49 MB | u16, scale per verified units (expected s⁻¹; store ×10⁻⁴) |
| `mrms-qpe-1h` | `CONUS/RadarOnly_QPE_01H_00.00/` | 2 min, ~1.19 MB | u16, 0.1 mm |
| `mrms-qpe-24h` | `CONUS/MultiSensor_QPE_24H_Pass2_00.00/` | hourly (~58 min latency), ~2.0 MB | u16, 0.1 mm |
| `mrms-ffg-ratio-1h` | `CONUS/FLASH_QPE_FFG01H_00.00/` | 2 min, ~0.22 MB | u16 **percent**, 0.1 % precision |

- **Budget finding:** taking every 2-min file of all four adds ≈ 1.4 GB/day, not the design's ≈ 0.5 GB. Add a per-product fixed `takeEvery` param (not adaptive cadence, which design §11 rejects): defaults rotation every frame (2 min), QPE 1 h and FFG ratio every 10 min, QPE 24 h every file. That gives ≈ 0.6 GB/day. Record the measured figure in the CR and the Source Catalog. Keep `mrms` `mbPerDay` at 4,000 unless the measured total of all MRMS products exceeds 3,500, then raise it to 4,500 and note why.
- Same S3 `start-after` listing path as 046; files share the MRMS `.grib2.gz` decode path and sentinel handling (−999 no coverage, −99/−1 no data: verify per product at implementation and map to code 0 / nodata).
- Ladder: MRMS default (every frame 6 h, then 10-min frames to 48 h); `mrms-qpe-24h` hourly to 48 h.
- Styles (versioned, 045 registry) with JSON legends and meanings: `rotation@1` (e.g. "≥ 0.010 s⁻¹: strong low-level rotation"), `qpe@1` (mm, imperial inches), `ffg-ratio@1` ("≥ 100%: rainfall exceeds flash-flood guidance").
- Native max zoom 8, `coverage: CONUS`. Warming as for reflectivity (radar z ≤ 7).
- Health: frame-age rule per product (`maxGap`: 6 min for 2-min products at their `takeEvery`, 90 min for QPE 24 h).

### 2. `nws-gridpoints` (POLLING, kind `nws-forecast`)

- Inputs: places from 036 (only places with `products` containing `nws`, or all places when the param `allPlaces=true`, default true). Max 50 places (param), else a health warning.
- Resolve each place once: `GET https://api.weather.gov/points/{lat},{lon}` (4-decimal coordinates) → `gridId`, `gridX`, `gridY`, `forecastGridData`, `timeZone`. Cache in `adapter_state` and re-resolve weekly, on 404/301, or when the place moves.
- Each place hourly: `GET {forecastGridData}` with `If-Modified-Since` (verified 2026-10: `Last-Modified` and `Cache-Control: public, max-age≈3600` are returned; no ETag seen). Spread places across the hour.
- Parse properties `temperature` (degC), `windSpeed`, `windGust` (km/h), `windDirection`, `probabilityOfPrecipitation` (%), `quantitativePrecipitation` (mm), `skyCover` (%), `hazards`. Values use ISO-8601 interval `validTime` (`start/PT1H`, `start/P1DT14H`); expand intervals into an hourly series for 72 h (accumulations divided evenly across the interval's hours). Convert wind to m/s.
- **Record** (externalId `place:{placeId}`, geometry = place point): payload `place`, `wfo`, `gridX`, `gridY`, `popMax24h`, `qpf24hMm`, `tMaxC24h`, `tMinC24h`, `gustMaxMs24h`, `hazards[]` (type + validTime, ≤ 10), `forecastUpdated`. `observedAt = updateTime` (UPSTREAM), `eventTime` null, no TTL, `volatileFields` none, `kindRetention{nws-forecast: 7d}`, STANDARD, ALL, `licence="US-PD"`, `titleField` `place`.
- **Series cache:** table `forecast_series_cache(place_id, provider, updated_at, valid_from, series jsonb)` (provider `nws`), replaced on each successful fetch; read by `GET /v1/places/{id}/weather`, `weather_forecast` (MCP) and 052's series merge. Tombstone the record and delete the cache row when a place is deleted.
- Budget 10/min, 3,000/day; identifiable UA plus operator contact exactly as 047's `nws-alerts`; honour `Retry-After`; 500/503 back off (NWS gridpoints are known to 500 transiently, keep the last good forecast).

### 3. `spc-storm-reports` (POLLING, kind `storm-report`)

- `GET https://www.spc.noaa.gov/climo/reports/today.csv` every 10 min with conditional GET (verified 2026-10: ETag, `Last-Modified`, `max-age=300`), plus `yesterday.csv` hourly to catch late reports after the 12 UTC day rollover.
- The file holds three CSV sections, each starting with its own header (verified): `Time,F_Scale,…` (tornado), `Time,Speed,…` (wind, knots or `UNK`), `Time,Size,…` (hail, hundredths of an inch). Columns `Time,<mag>,Location,County,State,Lat,Lon,Comments`. Comments may contain commas inside quotes and are truncated upstream.
- Time: `HHMM` UTC within the SPC convective day (12 UTC–12 UTC); rows with `HHMM < 1200` belong to the next calendar date. The convective date comes from the file (today vs yesterday) and the clock.
- externalId: first 12 hex of SHA-256 over `type|convDate|HHMM|lat|lon|location` (reports have no upstream id).
- Payload: `type` (tornado|wind|hail), `magnitude` (F-scale string, wind kt, hail inches as number; omit when `UNK`), `magnitudeUnits`, `location`, `county`, `state`, `wfo` (parsed from the trailing `(XXX)`), `comments` (≤ 500 chars; untrusted text), `title` ("Hail 1.75 in — 5 WNW Oliver, GA"). `eventTime = observedAt =` report time. Point geometry. No TTL, never tombstoned on absence. STANDARD, ALL, `kindRetention{storm-report: 30d}`, `licence="US-PD"`, `titleField=title`, `embedFields=[comments, location]`, `fieldSpecs` for every key.
- Budget 2/min, 400/day. Zero rows is normal (`zeroResultsNormal=true`; the header-only file is valid).
- `docs/Source-Catalog.md`: move `spc-storm-reports` out of the 089 list; 10-min interval, conditional GET, three-section CSV.

### 4. Place-weather integration (extend 052's `wx-places`)

- `precip` section adds `qpe1hMmMax`, `qpe24hMmMax` (max within the place radius) and `ffgRatio1hPctMax`; each keeps the section's status/asOf rules (inputs: the new products' frame events, `maxGap` per product).
- `forecast.nws` = `{status, popMax24h, qpf24hMm, gustMaxMs24h}` from the `nws-forecast` head; `status` is `stale` when `forecastUpdated` is older than 6 h, `unavailable` when the source is failing or disabled.
- `weather_forecast` MCP tool and `GET /v1/places/{id}/weather` include the NWS series. `wx-areas` accepts fields `rotation`, `qpe1h`, `ffg`.
- Recipe in `docs/Operator-Guide.md`: "Flash-flood guidance exceeded near home" (`precip.ffgRatio1hPctMax >= 100`, `enter`) and "Tornado report within 25 km" (`kind=storm-report`, `type == tornado`, `near{home, 25}`, `match` with `only_new`).

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`: no new operations; document kinds `nws-forecast` and `storm-report` (kind schemas via 014's `/v1/kinds/{kind}/schema` fieldSpecs), the new products and styles in examples, and `PlaceWeather.forecast.nws`. Regenerate Postman if anything changed.
- `docs/Source-Catalog.md`: rows for the four MRMS products (sizes, `takeEvery`), `nws-gridpoints`, `spc-storm-reports` with tier, policy, TTL, `ttlAnchor`, `observedAtMode`, licence.

## Tests to write (do not run)

- `MrmsSecondWaveDecodeTest`: one captured fixture per product (capture: `aws s3 cp --no-sign-request` the newest file of each prefix, keep ≤ 2 MB, record date in the Javadoc); sentinel mapping; FFG percent quantisation round-trip to 0.1 %.
- `MrmsTakeEveryTest`: thinning selects the expected keys from a listing and never adapts to weather.
- `NwsGridpointsParserTest` (fixture of a real `/gridpoints` body): interval expansion, accumulation splitting, unit conversion, 24-h summaries.
- `NwsGridpointsAdapterWireMockTest`: `/points` resolution cached, `If-Modified-Since` → 304 keeps the record, 500 keeps the last forecast and degrades after 3 failures, place move re-resolves, UA header.
- `SpcStormReportsParserTest` (fixtures `today.csv` with all three sections, header-only file, quoted commas): convective-day date logic, magnitude parsing, `UNK` omitted, stable externalIds.
- `SpcStormReportsAdapterWireMockTest`: ETag conditional GET, yesterday catch-up without duplicates.
- `StormReportsIngestIT`, `NwsForecastIngestIT`: records persisted, kind retention resolved, place delete tombstones and clears the cache row.
- `PlaceWeatherSecondWaveIT`: precip and forecast sections populate; a stale NWS forecast sets `forecast.nws.status=stale` and keeps the values.

## Acceptance criteria

- The four MRMS products render, sample and stat through the existing paths, within the measured download budget.
- Every place gets an hourly NWS forecast record and cached 72-h series using conditional requests.
- Storm reports ingest from all three CSV sections with correct convective-day dates and no duplicates.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `053: MRMS Second Wave And Gridpoints`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/053-CR-MRMS-Second-Wave-And-Gridpoints.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `053: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
