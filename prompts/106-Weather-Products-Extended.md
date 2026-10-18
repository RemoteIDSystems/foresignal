# 106 — Weather Products Extended

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/106-CR-Weather-Products-Extended.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/105-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/design-history/Weather-Imagery-Design.md`:
  - §10.3 row "074" (this prompt; its 074 = 106), §5.3 "Products" list
  - §1.4 (bucket facts), §2.3–§2.6 (quantisation, ladders, budgets, profiles), §6.1–§6.5 (frame keys, caching, fields, sampling), §7.1 (status sections, `999`), §9 (resource budget)
  - Translate design numbers with `docs/Roadmap.md` §5
- `docs/design-history/Gap-Analysis.md` §8 item 57 ("Extend 058": HRRR smoke fields, WPC HeatRisk, EPA UV, `f=png-uv`/`velocity-json` alternates, position sample)
- Completion Reports of 042, 045, 046 (NetCDF-Java, `RasterDecoder`, MRMS u8 code scheme, `goes-east-abi` on a `geos` grid), 047 (`awc-metars`), 051 (GEV `.idx` planner, fields `.json`/`.f32`), 052 (`SampleService`, `wx-places`), 036 (places), 037 (`/track`), 077 (SWPC split), 078 (`airnow-hourly`), 105 (IFS, `EcmwfIndexParser`)
- Upstreams (verify each path against a live listing and record differences):
  - NODD buckets `noaa-goes18`, `noaa-goes19`, `noaa-rtma-pds`, `noaa-nbm-grib2-pds`, `noaa-gfs-bdp-pds` (wave), `noaa-mrms-pds`, `unidata-nexrad-level3`
  - SWPC `https://services.swpc.noaa.gov/json/ovation_aurora_latest.json`
  - CoastWatch ERDDAP `https://coastwatch.noaa.gov/erddap/` (Geo-Polar Blended SST)
  - ECMWF `aifs-single`
  - AWC `https://aviationweather.gov/data/cache/tafs.cache.xml.gz`
  - WPC HeatRisk `https://www.wpc.ncep.noaa.gov/heatrisk/data.html` (verified 2026-10: daily KML and GeoTIFF for days 1–7 linking to the latest forecast; the product is labelled experimental)
  - EPA Envirofacts UV `https://data.epa.gov/efservice/getEnvirofactsUVHOURLY/ZIP/{zip}/JSON` (verified 2026-10: fields `ORDER, ZIP, DATE_TIME, UV_VALUE`, local time of the ZIP)

## Objective

Extend the product catalogue past v1. Add more GOES channels and sectors, RTMA-RU, NBM, GFS-Wave, NEXRAD Level III for the nearest sites, SWPC OVATION, Geo-Polar SST, AIFS, MRMS lightning probability, VIL and echo tops, TAF, HRRR smoke fields, HeatRisk and the UV index. Add client-friendly field encodings (`velocity-json`, `png-uv`) and position sampling for moving entities. Every product reuses the 042–052 pipeline: frames, styles, legends, sampling and place-weather sections. Heavy products are off by default on the small profile.

## Tasks

### 1. Product enablement and budgets

- Each new product is a `ProductSpec` on an existing or new source, with a per-product `enabled` flag in the source's `products` param (string-list).
- Defaults by profile (design §2.6):
  - **Small:** C08, C09, RTMA-RU, NBM, OVATION, TAF, HRRR smoke, HeatRisk and UV on; everything else off.
  - **Large:** also C02 (AOI-cropped), GOES-18 C13, GFS-Wave, MRMS extras, SST and AIFS.
  - Mesoscale sectors and NEXRAD L3 are opt-in on both profiles.
- Add each product's `mbPerDay` to its source budget and update the design §9 table copy in the Operator Guide. Set the source health "budget outlook" (006) warning when the projected download exceeds 120% of `mbPerDay`.
- Each product declares retention ladders: observations 24 h (C02: 6 h), models current + previous run, OVATION 24 h, SST 7 d.

### 2. Satellite (extend `goes-east-abi`; new `goes-west-abi`)

- **GOES-19 CMIPC C08 and C09** (water vapour): u16 0.01 K, styles `wv-gray@1` and `wv-enhanced@1`.
- **C02** (0.5 km visible): decode, then **crop to the places AOI envelope + 300 km** before quantising to u8 reflectance. This keeps disk and CPU bounded. Style `vis-gray@1`. Daytime only: a frame whose solar zenith exceeds 85° over the whole AOI is skipped (`zeroResultsNormal`).
- **Mesoscale** `ABI-L2-CMIPM` (M1/M2, 1-min): ingest a sector only while its footprint intersects the places AOI. Keep every 2nd frame by default (`mesoStride`).
- **`goes-west-abi`:** GOES-18 `noaa-goes18` CMIPC C13, same as East. Its `geos` grid is at the GOES-West longitude, read from the file.

### 3. Models and analyses

- **`rtma-ru`:** RTMA Rapid Update 2.5 km, every 15 min. Fields 2 m temperature, dewpoint, 10 m wind, gust and visibility, from the `.grb2` 2dvaranl file (verify the `noaa-rtma-pds` naming), on an `lcc` grid. An observation product.
- **`nbm`:** National Blend of Models CONUS core with `.idx`. Fields TMP 2 m, WIND, GUST, APCP06, PoP 6-h, SKY. Run 01/07/13/19z, f001–f036 hourly and to f072 3-hourly (configurable). A forecast product with the 051 run/step semantics.
- **`gfs-wave`:** global 0.25°. HTSGW (significant height), PERPW, DIRPW. Steps as GFS. Add place-weather `marine` section `{status, waveHeightMaxM24h}` only for places with `tags` containing `coastal`.
- **`aifs`:** ECMWF `aifs-single/0p25/oper`, 0–240 by 6, fields 10u/10v/2t/tp/msl. Reuse the 105 `EcmwfIndexParser`, CC BY 4.0 attribution, and serial requests. Included in the 105 disagreement flag as an extra model when enabled.
- **HRRR smoke fields** (extend `hrrr`): `MASSDEN:8 m above ground` (exists) plus `COLMD:entire atmosphere (considered as a single layer)` (vertically integrated smoke), as products `hrrr-smoke-surface` and `hrrr-smoke-column`, style `smoke@2`. place-weather `smoke` gains `hrrrColumnMax24h`. Verify HRRR MASSDEN and COLMD units in the `.idx` (design §10.4) and record them.

### 4. Radar extras

- **MRMS** (extend `mrms`): `LightningProbabilityNext30minGrid_scale_1` (u8 percent), `VIL` (kg/m²), `EchoTop_50` (km). Verify exact prefixes in a live `noaa-mrms-pds` listing. Styles `ltg-prob@1`, `vil@1`, `echotop@1`. place-weather `lightning.prob30mMaxInRadius`.
- **`nexrad-l3`:** for 1–3 nearest sites to the home place (`sites` param, else computed from a bundled WSR-88D site table), products N0B (super-res base reflectivity) and N0G (velocity) from `unidata-nexrad-level3`. Use the S3 listing with `start-after` per site and product.
  - Decode with the NetCDF-Java NIDS reader, if it supports products 153/154 (verify). If not, implement only N0B with a documented gap.
  - Polar → latlon resampling to a local grid of 0.005° within 230 km. Styles `nws-reflectivity@3` and `velocity@1`.

### 5. Global and records

- **`swpc-ovation`:** the `ovation_aurora_latest.json` 1° grid of aurora probability (0–100) → global u8 product `aurora-probability`, every 5 min with conditional GET. Style `aurora@1`. place-weather `aurora {status, probMaxNearby}` only when |lat| ≥ 40°. Coordinate with the 077 SWPC split (no duplicate fetch: the record source and this product share one source if 077 already fetches the file).
- **`sst-geopolar`:** ERDDAP griddap subset over a configurable bbox (default AOI envelope + 1000 km), daily, `.nc` via `download`. Verify the dataset id and record it. u16 0.01 K, style `sst@1`.
- **`awc-tafs`** (extend 047's AWC source family): `tafs.cache.xml.gz` every 10 min.
  - kind `taf`, externalId station; payload stationId, issueTime, validFrom/To, forecast groups (wind, visibility, wx, clouds, change indicator), `rawText`.
  - `observedAt = issue_time`; `validFrom`, `validUntil` explicit; STANDARD, 7 d retention.
  - The AWC compliance note (≤ 1 req/min per thread) goes in the descriptor.

### 6. Heat and UV

- **`wpc-heatrisk`:** daily GeoTIFFs for days 1–7. Discover URLs from the data page once per day (conditional GET), then download each day's GeoTIFF.
  - Read with the 054 GeoTIFF reader (EPSG:4326/3857); otherwise use the optional GDAL path or set a health reason.
  - Categorical u8 product `heatrisk` (0 Little/None, 1 Minor, 2 Moderate, 3 Major, 4 Extreme), style `heatrisk@1` with categories and labels.
  - place-weather `heat {status, riskMaxDay1, riskMax7d, peakDay}`.
  - Descriptor notes "experimental NWS product"; US-PD.
- **`epa-uv`:** hourly UV forecast per place.
  - Needs a US ZIP per place: add an optional `place.attributes.zip`, else skip that place with a health note.
  - Budget 1 request per place per 6 h. Parse `DATE_TIME` in the place timezone.
  - kind `uv-forecast`, externalId `place:{id}`, payload `{zip, hours[{time, uv}], maxUv, maxAt}`. place-weather `uv {status, maxToday}`.

### 7. Field encodings (`/v1/products/{id}/fields/{frameKey}.{ext}`)

- **`velocity-json`** (leaflet-velocity / wind-js format): a JSON array of two objects (U and V). Each has `header {parameterCategory: 2, parameterNumber: 2|3, lo1, la1, lo2, la2, dx, dy, nx, ny, refTime, forecastTime}` and `data[]` row-major north→south.
  - Requires `res=1` or a bbox (≤ 300k points per component, else 400 `FIELD_TOO_LARGE`).
  - Gzip; same cache rules as `.f32`.
- **`png-uv`:** an 8-bit RGB PNG with R = u and G = v linearly scaled over `[uMin, uMax]`, `[vMin, vMax]`, and B = 0. The companion `.json` manifest gains `encodings.pngUv {url, uMin, uMax, vMin, vMax, width, height, bounds}`. This is the format WebGL wind-particle layers expect.
  - 108 reuses this encoder for tiled `.png` fields.
- Both are listed in the field manifest `encodings`, and served for `gfs`, `hrrr`, `ifs` and `aifs` wind fields. Add TileJSON-style links.

### 8. Position sample

- `GET /v1/sample?record={sourceId}/{externalId}&products&time` samples at the entity's current live position (head ⋈ live).
  - Respect `private` (039) and observer gating.
  - The response adds `position {lon, lat, observedAt, ageSeconds}`.
  - If the position is older than 15 min, status is `stale-position`.
- `POST /v1/sample/track` with body `{record | points[{lon, lat, time}] ≤ 500, products, maxGap}` samples each point at its time, using the nearest frame within `maxGap` (default per-product `maxGap`), or forecast frames for future times with `run`.
  - With `record`, points come from the 037 `/track` simplification over `from/to`.
  - Each sample carries `status`.
- MCP: extend `sample_weather` with `record`; add `sample_weather_along_track` (read).

### 9. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - new product ids in examples, `taf`/`uv-forecast` kind schemas;
  - field `ext` enum (`json`, `f32`, `velocity-json`, `png-uv`); `/v1/sample` `record` param; `POST /v1/sample/track` (read, `x-foresignal-scope: read`);
  - problem codes `FIELD_TOO_LARGE` (400) and `UNKNOWN_RECORD` (404) if missing;
  - regenerate Postman with a BODIES example for `sampleTrack`.
- AccessPolicy: `POST /v1/sample/track` → READ.
- `docs/Source-Catalog.md` rows: `goes-west-abi`, `rtma-ru`, `nbm`, `gfs-wave`, `aifs`, `nexrad-l3`, `swpc-ovation`, `sst-geopolar`, `awc-tafs`, `wpc-heatrisk`, `epa-uv`, plus extended `mrms`, `hrrr` and `goes-east-abi` notes. Each carries access, cadence, size, licence (US-PD; ECMWF CC BY 4.0) and verification notes.
- `docs/Operator-Guide.md`: product enablement per profile, the updated download budget, HeatRisk "experimental", and setting `zip` on places for UV.
- Frontend guide: `velocity-json` with leaflet-velocity, and `png-uv` with WebGL particle layers.

## Tests to write (do not run)

- Fixture capture procedure per new upstream (range-read or small file, ≤ 2 MB). For each product family, a parser/decoder contract test:
  - `GoesWvAndVisDecodeTest` (crop and solar-zenith skip)
  - `RtmaRuDecodeTest`, `NbmIdxPlanTest`, `GfsWaveDecodeTest`, `AifsPlanTest`, `HrrrSmokeFieldsTest`
  - `MrmsExtrasTest`, `NexradL3DecodeTest`, `OvationParserTest`, `SstErddapTest`
  - `TafParserContractTest`, `HeatRiskGeoTiffTest`, `EpaUvParserContractTest` (local-time parsing)
- WireMock adapter tests: `RtmaRuAdapterTest`, `NbmAdapterTest`, `NexradL3AdapterTest`, `OvationAdapterTest` (conditional GET), `HeatRiskAdapterTest` (link discovery), `EpaUvAdapterTest` (per-place budget, missing ZIP note), `TafAdapterTest`.
- `VelocityJsonEncoderTest`: header fields, row order, size cap. `PngUvEncoderTest`: round trip within quantisation error.
- `PositionSampleIT`: live position sample; stale position; private source hidden without scope; track sampling uses the nearest frame within maxGap.
- `ProductProfileDefaultsTest`: which products are enabled per profile.
- Ingest ITs for `taf` and `uv-forecast` records (`TestRecords` patterns, `validFrom`/`validUntil`).

## Acceptance criteria

- Each enabled product appears in `/v1/products` with styles, legend, ladder and attribution, and renders through the existing tile, image, sample and series paths.
- The small profile enables only the light products by default. Budgets include `mbPerDay`.
- `velocity-json` and `png-uv` encodings are served for wind fields with the documented manifests.
- `/v1/sample?record=` and `POST /v1/sample/track` return per-point statuses.
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
2. **Commit** all changes with the message `106: Weather Products Extended`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/106-CR-Weather-Products-Extended.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `106: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
