# 051 — Model Grids And Wind

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/051-CR-Model-Grids-And-Wind.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.2 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, and `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump.
- **Previous Completion Report:** read `completion-reports/050-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (rows "GRIB2/NetCDF decoder", "Where frames live", "God's Eye View port"), §1.4 (GFS/HRRR facts), §2.3 (`raster_frame`, `.elg`), §2.4 (fields layout, quantisation, model ladder), §2.5 (`gfs`, `hrrr` budgets), §2.6 (HRRR runs per profile), §5.1 (`gfs`, `hrrr` rows), §6.1 (frame keys, aliases, `RUN_REQUIRED`), §6.2 (timeline), §6.4 (fields and wind), §6.8 (forecast SSE events), §6.10 (particles), §10.4. Design prompt 038 = v1.2 051.
- `docs/Gap-Analysis.md` §8 item 57 (alternate field formats and HRRR smoke fields go to 106; nothing here).
- Completion Reports of **042** (ProductSpec, FrameStore, frame conventions), **045** (GridDef `latlon`/`lcc`, TileRenderer, styles, render queue), **046** (NetCDF-Java, `RasterDecoder` SPI, decode pool), **050** (frontend guide), **016** (MVT path).
- God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`): `server/providers/wind/{catalog,gfs,grid,decode}.js` and `src/layers/wind/*`. `server/providers/wind.js` (the GEV proxy) and `server/providers/wind/ifs.js` are reference only here (IFS is 105).
- Upstreams: `https://noaa-gfs-bdp-pds.s3.amazonaws.com/`, `https://noaa-hrrr-bdp-pds.s3.amazonaws.com/`; NCEP GRIB2 `.idx` inventory format; PNS 26-29 / 26-30 (GFSv17).

## Objective

Add forecast models: GFS 0.25° and HRRR CONUS as budgeted product sources, with wind delivered three ways that cover every client (a GEV-compatible Float32 field for web particles, a barb MVT layer, and a `wind-speed` raster). Complete the forecast semantics (runs, steps, `RUN_REQUIRED`, aliases) and ship the cross-product `/v1/products/timeline`. Byte-range reads planned from `.idx` inventories keep downloads to about 0.5 GB/day (GFS) and 0.4–0.8 GB/day (HRRR).

## Tasks

### 1. GEV port: inventory, ranges, cycles (`ai.elaro.raster.model`)

Port to Java with a file header naming the origin file and the MIT notice, and add rows to the `THIRD_PARTY_NOTICES.md` "Ported files" table:
- **`GribIdx`** (from `gfs.js` `parseGfsIdx`): lines `msg:offset:d=YYYYMMDDHH:VAR:LEVEL:TIME:…`; skip malformed lines; reject an inventory with no valid line (an HTML error page is never an empty inventory). Keep the time-range field (e.g. `0-6 hour acc fcst`).
- **`IdxRangePlanner`** (from `windMessageRanges`): a message spans its offset to the next message's offset − 1. For the last message, the end is the object size from `listS3` `Size` (GEV throws there; Elaro must not). Adjacent wanted messages are coalesced into one `getRange`. Each range is charged to `mbPerDay` in advance from the offsets (005).
- **Field matching** is by `(variable, level[, timeRange])`, **never by message number**. Verified 2026-10: GFS `f006` lists `APCP:surface:0-6 hour acc fcst` twice, so take the first match and match accumulations by the run-total range `0-{step} hour acc`. A missing required field degrades health ("possible format change: GFS idx missing GUST:surface") and writes no frame. A missing optional field drops only that product (GEV `loadOptionalScalar` pattern).
- **`ModelCycleSelector`** (from `catalog.js`): candidate cycles from the clock with a configurable availability lag (GFS 3.5 h, HRRR 50 min), confirmed by the `.idx` existing. Not GEV's `nearestGfsStep` (hourly to f120); Elaro's step lists are below.
- **Limits** (from `decode.js`/`grid.js`): ≤ 8 MB per GRIB message, `ni × nj ≤ 2,000,000` (HRRR 1799×1059 = 1.9 M fits), all values finite after scale/offset. Decoding uses 046's NetCDF-Java `RasterDecoder` on a temp file (`Grib2RecordScanner.readData`); ecCodes WASM is not ported.
- **`WindResampler`** (from `grid.js` `resampleWindGrid`): bilinear, longitude wraps, latitude clamps; output `lo1=0, la1=90`, `nx=360/dx`, `ny=180/dy+1`.

### 2. Sources and products

Both are POLLING product sources (`zeroResultsNormal=true`, `observedAtMode=UPSTREAM`, STANDARD tier, `licence="US-PD"`, `kindRetention{raster-frame: 7d}`), with params `pathTemplate`, `steps`, `runEvery`, `availabilityLag`:

| Source | Template (default) | Runs / steps | Budget |
|---|---|---|---|
| `gfs` | `gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.0p25.f{FFF}` | 4/day; f000–f048 3-hourly, f054–f120 6-hourly (29 steps) | 60/min, 3,000/day, 1,000 MB/day; 15-min check |
| `hrrr` | `hrrr.{yyyyMMdd}/conus/hrrr.t{HH}z.wrfsfcf{FF}.grib2` | every 3 h (`ELARO_PROFILE=small`: 6 h); f00–f18 | 60/min, 4,000/day, 2,000 MB/day; 10-min check |

Products (ProductSpec, `temporal.type=forecast`, `maxGap` = step spacing, ladder "current complete run + previous complete run"):
- `gfs` (kind `field`): UGRD/VGRD `10 m above ground`; `.elg` i16 two bands (u, v; scale 0.01 m/s) on `latlon` 1440×721 for tiles, sampling and vectors, **plus** field files `rasters/fields/gfs/{run}/{step}.f32.gz` at 1° and 0.25° (precompressed gzip). Native max zoom 4.
- `gfs-gust` (`GUST:surface`), `gfs-t2m` (`TMP:2 m above ground`), `gfs-apcp` (`APCP:surface:0-{step} hour acc`, run-total; consumers difference steps): i16 with scale/offset.
- `hrrr` (kind `field`, `UGRD/VGRD:10 m above ground`), `hrrr-refc` (`REFC:entire atmosphere`, styled `nws-reflectivity`), `hrrr-gust` (`GUST:surface`), `hrrr-smoke` (`MASSDEN:8 m above ground`, style `smoke`): `lcc` 1799×1059 i16. Native max zoom 8. Verified 2026-10: these exact strings are present in `wrfsfcf01.grib2.idx`. Verify MASSDEN units (kg m⁻³ expected; convert to µg m⁻³ for display) at implementation.
- **HRRR winds are grid-relative.** Check GRIB2 "resolution and component flags" bit 5; when set, rotate to earth-relative before storing: `α = n·(λ − λ₀)` with cone constant `n = sin(φ₁)` for the tangent LCC (HRRR: φ₁ = 38.5°, λ₀ = −97.5°), `u_e = u·cos α + v·sin α`, `v_e = −u·sin α + v·cos α`. GFS (latlon) is earth-relative. Add a fixture test with a known rotation.
- **Run lifecycle:** frames are written per step as they decode (`frame_id` `2026101012Z-f006`, `reference_time`, `forecast_step`, `valid_time`). A run is **complete** when every configured step has a complete frame; then write **one** `raster-frame` record version (externalId = productId) with payload `{run, steps[], validTimes[], tilesTemplate}`. While a run is in progress, optionally upsert `{productId}:partial`. No version per step.
- Decode on 046's bounded pool; S3 access only through `ctx.http()` (`listS3`, `getRange`). Health: the frame-age rule uses the run cadence; a missing `.idx` past `availabilityLag + 2 h` degrades.
- Probe: read the newest `.idx`, range-read one message, decode the header, return the grid summary; write nothing.

### 3. Forecast semantics (complete what 042/045 started)

- Concrete forecast time without `run` → 400 `RUN_REQUIRED`. `run=latest|previous|<runIso>`; `run=latest` is accepted and never immutable.
- `latest` (forecast) = newest complete run's step nearest now; `now` = the frame whose valid time is at or before now with the smallest gap within `maxGap`.
- Immutable only when `run` is pinned, the frame key carries `~sha8` and the frame is complete (design §6.1).
- Reserve product ids `timeline` and `loop` in `ProductRegistry` (route collision with `/v1/products/{id}`).

### 4. Fields endpoints (`gfs` only in v1)

- `GET /v1/products/gfs/fields/{frameKey}.json?run&res=1|0.25&bbox` → GEV `schemaVersion: 1` manifest: `model`, `cycle{date, hour, forecastHour, runIso, validIso}`, `level`, `units: "m/s"`, `grid{nx, ny, lo1, la1, dx, dy}`, `stale`, `unavailable`, `reason`; Elaro additions `components[{name:"u",offset:0},{name:"v",offset:nx·ny·4}]`, `byteLength`, `sha256`, `attribution`, and a content-addressed `dataUrl` (`…/fields/{frameKey}.f32?run=<runIso>&res=…&bbox=<snapped>`).
- `GET …/fields/{frameKey}.f32` → planar little-endian Float32 u then v, rows north→south, longitudes 0..359 (1°: 360×181, 521,280 bytes raw; 0.25°: 1440×721, 8.3 MB). Served precompressed with `Content-Encoding: gzip` (return 406 only if the client refuses gzip). `bbox` (recommended for 0.25°) is snapped outward to grid cells and wraps across 0°; bbox subsets are cut on the fly from the 0.25° file and cached in `tileCacheMb`.
- Cache policy per design §6.1 (fields are covered by the same rule).

### 5. Barb vectors and the wind-speed raster

- `GET /v1/products/{id}/vectors/{frameKey}/{z}/{x}/{y}.mvt?run` for `gfs` and `hrrr` (`application/vnd.mapbox-vector-tile`): sample the u/v frame on a ~32 px lattice per zoom inside the tile; properties `speedMs` (0.1), `fromDeg` (`(270 − atan2(v,u)·180/π) mod 360`, integer), `barbKt` (5-kt bucket). Build with PostGIS `ST_AsMVT` over `unnest(?::float8[] …)` arrays and `ST_AsMVTGeom(…, ST_TileEnvelope(z,x,y))`, reusing 016's MVT plumbing. Cached and render-queued like raster tiles.
- `style=wind-speed@1`: a speed raster computed from u/v in `TileRenderer` (works for every client). Seed its colour stops from GEV `src/layers/wind/fields.js` `WIND_FIELDS.speed` (0–30 m/s; MIT header in the style resource notes) and register a JSON legend.

### 6. Timeline

`GET /v1/products/timeline?products=a,b&from&to` and `?mode=seamless&past=mrms-reflectivity&future=hrrr-refc` (read): ticks marked `observed|forecast`, the frame per product per tick honouring `maxGap`, the `run` used (one run per forecast product, never mixed), `nowIndex`, expanded tile templates. External catalogue entries get synthesised ticks with `availability: "unverified"`. Cap 500 ticks (400 `VALIDATION_FAILED` beyond).

### 7. Frontend guide: wind

In `docs/Frontend-Guide.md` "Wind particles": the framework-neutral GEV modules (`model.js`, `streamlines.js`, `fields.js`, `source.js`) can be reused by a web client under the MIT notice; `rendering.js`/`gpuRendering.js` are Cesium-specific, so MapLibre needs a `CustomLayerInterface` wrapper. Give the manifest adapter (Elaro's `dataUrl` replaces GEV's `/api/wind/grid` regex check). Flutter and Cesium use barb MVT or the `wind-speed` raster; particles stay a web-only progressive enhancement.

### 8. Spec, Postman, docs

- `docs/OpenAPI.json`: `getProductFieldManifest`, `getProductFieldData` (`application/octet-stream`), `getProductVectorTile` (`application/vnd.mapbox-vector-tile`), `getProductTimeline`; `run` parameter docs and `RUN_REQUIRED` on frame/tile/image operations; schemas `FieldManifest`, `Timeline`, `TimelineTick`. All `x-elaro-scope: read`; `AccessPolicy` READ for these GETs. Regenerate Postman.
- `docs/Source-Catalog.md`: `gfs` and `hrrr` rows (tier, policy, budgets, `mbPerDay`, licence US-PD, configurable templates for GFSv17, field matching by variable/level); replace line 48 if 045 has not.
- `docs/Operator-Guide.md`: model sources, profile-dependent HRRR cadence, GFSv17 template override.

## Tests to write (do not run)

- `GribIdxTest`: real GFS and HRRR `.idx` fixtures; malformed lines skipped; HTML body rejected; duplicate APCP takes the first match.
- `IdxRangePlannerTest`: next-offset ranges, last message uses object size, coalescing, advance `mbPerDay` charge.
- `ModelCycleSelectorTest` (`MutableClock`): lag, cycle rollover at 00z, small-profile HRRR cadence.
- `WindResamplerTest`: seam wrap, latitude clamp, 1° output dims, invalid geometry rejected (port GEV's test cases).
- `HrrrWindRotationTest`: grid-relative to earth-relative against hand-computed values.
- `GfsAdapterWireMockTest` / `HrrrAdapterWireMockTest`: listing, `.idx`, range reads from captured message fixtures (capture procedure in the test Javadoc: `curl -r` one message per field from a real run), missing-field health degradation with no frame written.
- `ForecastRunIT`: steps arrive, one `raster-frame` version only at run completion, `:partial` upserts, ladder keeps two complete runs.
- `ForecastSemanticsIT`: `RUN_REQUIRED`, `latest`/`now`/`previous`, immutability only with pinned run and `~sha8`.
- `FieldEndpointIT`: manifest shape, byte length 521,280 at 1°, gzip encoding, bbox snapping and seam wrap.
- `BarbVectorTileIT`: MVT decodes, spacing, `fromDeg` and `barbKt` correct for a synthetic field.
- `TimelineIT`: seamless MRMS past + HRRR future, no mixed runs, `nowIndex`, external ticks unverified.

## Acceptance criteria

- GFS and HRRR ingest through budgeted range reads only, matched by variable and level; a removed field degrades health without empty frames.
- One record version per completed forecast run; all frame URLs follow the forecast caching rule.
- The 1° `.f32` field is byte-compatible with the GEV particle layer's planar u/v layout.
- `THIRD_PARTY_NOTICES.md` lists every ported GEV file.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 118, 119, 120).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 120).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `051: Model Grids And Wind`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/051-CR-Model-Grids-And-Wind.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `051: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
