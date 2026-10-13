# 051-CR-Model-Grids-And-Wind

| | |
|---|---|
| Prompt | 051-Model-Grids-And-Wind.md |
| Status | Complete |
| Implementation commit | 7fdcdb907d5573a6df381acb3e439f703a25cb00 |
| Date | 2026-10-12 |

## Summary

Foresignal now ingests two forecast models, GFS 0.25° and HRRR CONUS, through budgeted byte-range reads
planned from each step's `.idx` inventory. Wind is delivered three ways:
- a God's Eye View compatible Float32 field for web particles (`gfs`);
- a barb vector tile layer (`gfs`, `hrrr`);
- the `wind-speed@1` raster style, which every client can draw.

Forecast runs complete per product with one record version, and `/v1/products/timeline` joins
products. The four 051 operations are `implemented` and off `pending-operations.txt`.

**1. God's Eye View port** (`ai.foresignal.raster.model`, MIT headers, `THIRD_PARTY_NOTICES.md` rows)
- `GribIdx` (from `gfs.js` `parseGfsIdx`):
  - parses `msg:offset:d=…:VAR:LEVEL:TIME:` lines, skips malformed ones, and keeps the date and
    time-range fields;
  - refuses a body with no valid line, so an HTML page is an error, never an empty inventory.
- `FieldSelector` matches by variable, level and optionally the run-total accumulation, never by
  message number:
  - the first match wins, so the duplicated `APCP:surface:0-6 hour acc fcst` at f006 takes message 596;
  - at whole days the run total is written `0-N day acc` (verified on the 2026-10-12 12z run at f024,
    f048, f072, f096 and f120), so `0-(N/24) day acc` matches too.
- `IdxRangePlanner` (from `windMessageRanges`):
  - a message spans its offset to the next message's offset − 1;
  - the last message ends at the listed object size (`listS3` `Size`);
  - adjacent wanted messages are coalesced into one ranged GET, at most 16 MB per range;
  - a message over 8 MB is refused;
  - the budgeted `getRange` (005) charges each range to `mbPerDay` before sending.
- `ModelCycleSelector` (from `catalog.js`):
  - candidate cycles from the clock and the availability lag, for any cadence dividing 24 h;
  - object keys from a path template (`{yyyyMMdd}`, `{HH}`, `{FFF}`, `{FF}`);
  - `nearestGfsStep` is not ported.
- `WindResampler` (from `grid.js` `resampleWindGrid`):
  - bilinear, longitude wraps, latitude clamps;
  - output at `lo1=0, la1=90`, `nx=360/dx`, `ny=180/dy+1`;
  - the God's Eye View limits: at most 2,000,000 source points and every value finite.
- `WindRotation`: grid-relative to earth-relative, `α = n·(λ − λ₀)`. It is applied when flag table 3.3
  bit 5 is set: HRRR's octet 47 is `0x08`, GFS's flags are `0x30`.

**2. Decoder.** `Grib2Decoder` now reads grid template 3.30 (Lambert conformal) from its raw octets:
- it builds an `lcc` grid whose `la1/lo1` is the south-west cell, whatever the scanning mode;
- the box is walked along the grid outline, which gives HRRR −134.12…−60.89, 21.12…52.63;
- the radius comes from the earth shape (6 → 6,371,229 m, 0, or 1 scaled); other shapes are refused;
- the metadata gains `componentFlags` and `gridRelativeWinds`.

Checked with a scratch program against the real messages: the four HRRR corners equal 045's numbers to
1e-4 degrees.

**3. Sources** (`source.adapters.gfs.GfsAdapter`, `source.adapters.hrrr.HrrrAdapter`)
- Both are thin descriptors over `ModelRunIngest`:
  - POLLING, STANDARD, UPSTREAM, `zeroResultsNormal`, licence US-PD, `kindRetention{raster-frame: 7d}`;
  - params `pathTemplate`, `steps` (`N` / `A-B/S` items, `ForecastSteps`), `runEvery`,
    `availabilityLag`.
- `gfs`:
  - 15 min; 60/min, 3,000/day, 1,000 MB/day;
  - lag 210 min; steps `0-48/3, 54-120/6` (29 steps).
- `hrrr`:
  - 10 min; 60/min, 4,000/day, 2,000 MB/day;
  - lag 50 min; steps `0-18/1`;
  - `runEvery` defaults to 6 h with `FORESIGNAL_PROFILE=small` and 3 h with large (`RasterProfile.hrrrRunEvery`).
- Products, all forecast, i16, ladder of 2 complete runs:
  - `gfs`: `kind=field`, u/v at 0.01 m/s on 1440 × 721, max zoom 4, `wind-speed@1`, bilinear,
    required;
  - `gfs-gust`, `gfs-t2m` (K, offset 273.15) and `gfs-apcp` (mm, run total, no f000, `qpe@1`):
    `kind=raster`, optional;
  - `hrrr`: `kind=field`, u/v rotated to earth-relative on `lcc` 1799 × 1059, max zoom 8, required;
  - `hrrr-refc` (`nws-reflectivity@1`), `hrrr-gust` and `hrrr-smoke`: optional. `hrrr-smoke` is
    `MASSDEN:8 m above ground`, converted from kg m⁻³ to µg m⁻³ (× 10⁹), `smoke@1`.
- `DecoderDependent`: all products need `GRIB2_COMPLEX_PACKING`.

**4. Engine** (`ModelRunIngest`)
- Per fetch it takes the two newest candidate cycles, oldest first, skipping cycles every product has
  completed, and reads at most 10 steps.
- Per step:
  - reads the `.idx` (403/404 means not published yet and ends the cycle);
  - matches every product's fields;
  - plans and reads the ranges, and writes each message to its own file in `rasters/tmp`;
  - commits one frame per product, with a decoder that runs on the decode pool: decode, size and
    finiteness limits (at most 2,000,000 points, every value finite), the factor, the rotation;
  - deletes every staged file in `finally`.
- A required field missing writes no frame for the step. An optional one drops only its product. Both
  record "possible format change: GFS idx missing GUST:surface" for the product.
- A product whose every configured step has a frame gets `completeRun`, so its one record version.
- Adapter state: `done:{cycle}`, `steps:{product}:{cycle}` and `complete:{product}`. Keys of cycles
  that left the window are removed.
- Health:
  - `state.degrade(…)` when the newest cycle's first `.idx` is still missing 2 h after the lag;
    cleared when it appears;
  - `ForecastRunCadence` (new SPI, implemented by the engine) makes `FrameAgeHealthRule` judge
    forecast products against two run cadences instead of their step spacing.
- The probe reads the newest `.idx`, range-reads one message, decodes its header and returns the grid
  summary. It writes nothing, adapter state included.

**5. Fields** (`FieldStore`, `FieldService`, `api.ModelFieldsController`)
- The pipeline writes the God's Eye View fields on the decode pool for any forecast frame of a global
  `kind=field` product with two physical bands:
  - path `rasters/fields/{product}/{run}/f{FFF}.{1p00|0p25}.f32.gz`, gzip;
  - contents: planar little-endian u then v, from the decoded floats;
  - the 0.25° file is the native GFS grid unchanged, and 1° is resampled;
  - the frame's `stats.fields` lists path, sha256 of the raw bytes, byte length and grid.
- The orphan sweep removes run directories with no frame left.
- `GET …/fields/{frameKey}.json?run&res&bbox`:
  - the God's Eye View `schemaVersion: 1` manifest plus `components`, `byteLength`, `sha256`,
    `attribution` and the content-addressed `dataUrl` (frame key, pinned run, `res`, snapped `bbox`);
  - `stale` is true when the run is not the newest complete run.
- `GET …/fields/{frameKey}.f32`:
  - serves the stored gzip with `Content-Encoding: gzip`, and `406 NOT_ACCEPTABLE` only when gzip is
    refused;
  - a `bbox` is snapped outward to grid points, wraps across 0°, and is cut on the fly from the file of
    the requested resolution; subsets and their hashes are cached in the tile cache.
- Both follow `RasterCachePolicy`. Fields are served for `gfs` only (`kind=field`, two bands, global
  coverage); anything else is `400`.

**6. Barbs and speed**
- `VectorTileService` serves `/vectors/{frameKey}/{z}/{x}/{y}.mvt` for u/v products:
  - an 8 × 8 lattice at 32 px spacing per 256 px tile, sampled bilinearly, skipping nodata;
  - properties `speedMs` (0.1), `fromDeg` (`(270 − atan2(v,u)·180/π) mod 360`, integer) and `barbKt`
    (5-kt bucket);
  - built by PostGIS: `ST_AsMVT` over `unnest(float8[]/int4[])` with
    `ST_AsMVTGeom(…, ST_TileEnvelope(z,x,y), 4096, 0, true)`, layer `wind`;
  - cached and queued like raster tiles.
- `StyleDefinition` gains `derive` (`speed`). `TileRenderer` draws √(u² + v²) of bands 0 and 1, by
  nearest or bilinear sampling. A one-band frame draws its own value, so `wind-speed@1` also colors
  the gust products.
- `wind-speed@1` is reseeded from God's Eye View `WIND_FIELDS.speed`: 0–30 m/s, six stops, MIT notice
  in the style.

**7. Timeline** (`TimelineService`, `api.ProductTimelineController`)
- Ticks are the union of frame valid times in the window. Per tick and product, the frame is the one
  at or before the tick with the smallest gap within `maxGap`.
- Each forecast product contributes its newest complete run only (`runs`).
- Seamless mode: past ticks up to now are `observed`, future ticks after now `forecast`.
- External entries get synthesized ticks every `timeDimension.period` up to `now − defaultLag`, with
  `{time}` filled and `availability: unverified`.
- `nowIndex`; at most 500 ticks and 10 products. Default windows: now − 6 h to now (+ 48 h with a
  forecast), seamless now − 3 h to now + 18 h.

**8. Forecast semantics.** 042/045 had already built them: `RUN_REQUIRED`, `run=latest|previous|<runIso>`,
`latest`/`now`, immutability only with a pinned run, `~sha8`, a versioned style and a complete frame,
and the reserved ids `timeline`/`loop`. `ForecastSemanticsIT` now covers them on `gfs`, through tiles,
images, fields and vectors.

**9. Spec, Postman, docs**
- OpenAPI: four operations `implemented` with fuller descriptions (406, the `wind` layer, timeline
  defaults and a 503), and `Style.derive`; changelog entry.
- Postman regenerated.
- `docs/Source-Catalog.md`: forecast model section with both rows and the verified facts.
- `docs/Operator-Guide.md`: "Forecast models (GFS and HRRR)", covering bandwidth, the profile-dependent
  HRRR cadence, the GFSv17 `pathTemplate` override and health.
- `docs/Frontend-Guide.md`: "Wind particles" — barbs and speed for every client, the field endpoints,
  which GEV modules are reusable, the `CustomLayerInterface` note, and a manifest adapter that replaces
  `source.js`.
- `docs/Architecture.md` §3 (`raster.model`, the adapter rule) and §28.
- `THIRD_PARTY_NOTICES.md`: the "Ported files" table.

## Files created / modified

**Created, main:**
- `raster/ForecastRunCadence`
- `raster/model/`: `package-info`, `GribIdx`, `FieldSelector`, `IdxRangePlanner`, `ModelCycleSelector`,
  `WindResampler`, `WindRotation`, `ForecastSteps`, `ModelDefinition`, `ModelRunIngest`, `FieldStore`,
  `FieldService`, `VectorTileService`, `TimelineService`
- `source/adapters/gfs/{GfsAdapter,package-info}`, `source/adapters/hrrr/{HrrrAdapter,package-info}`
- `api/ModelFieldsController`, `api/ProductTimelineController`
- `resources/raster/styles/qpe@1.json`

**Modified, main:**
- `raster/decode/Grib2Decoder` (template 3.30, flags)
- `raster/RasterPipeline` (field files), `RasterRetentionStep` (field sweep), `RasterPaths` (`fields()`),
  `RasterProfile` (`hrrrRunEvery`), `FrameAgeHealthRule` (run cadence)
- `raster/render/StyleDefinition`, `StyleRegistry` (`derive`), `TileRenderer` (speed)
- `resources/raster/styles/wind-speed@1.json`

**Build, docs, spec:** `build.gradle.kts` (ranged pinned fixtures), `docs/OpenAPI.json`,
`postman/Foresignal.postman_collection.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`,
`docs/Frontend-Guide.md`, `docs/Architecture.md`, `THIRD_PARTY_NOTICES.md`,
`src/test/resources/openapi/pending-operations.txt` (−4 lines).

**Tests and fixtures:**
- New tests: listed below.
- New test support: `testsupport/raster/ModelFixtures`.
- Extended test support: `ProductAdapterHarness` (`ParamContext`, recorded `completeRun`) and
  `DecoderFixtures` (GFS/HRRR helpers).
- Fixtures: `src/test/resources/fixtures/gfs/` (three `.idx`, four 1° messages, README) and
  `fixtures/hrrr/` (two `.idx`, the REFC message, README).
- Updated tests: `StyleRegistryTest`, `LegendServiceTest`, `FamilyDefaultsTest`,
  `RasterAccessPolicyTest`, `NoClientTriggeredFetchIT` and `FrameAgeHealthRuleTest`.

## Deviations from the prompt

1. **Timeline schema name.** The spec already had `ProductTimeline` (with `TimelineTick`). `Timeline` is
   037's event-density histogram, so the response keeps `ProductTimeline`. The changelog says so.
2. **`wind-speed@1` was reseeded in place**, not bumped to `@2`.
   - The prompt and the spec's product examples name `wind-speed@1`. 045 had seeded it with viridis
     0–40 m/s.
   - It also gained `derive: speed`. A one-band frame renders as before, apart from the colors.
   - See Issue 1.
3. **`qpe@1` ships now** (mm, 0.25–203.2 mm in inch steps). The spec's product examples give `gfs-apcp`
   the default style `qpe@1`, which belongs to 053's style list. No other style measures accumulation.
4. **Scalar products are `kind=raster`.** The spec's `x-foresignal-products` lists `gfs-gust`,
   `gfs-t2m`, `gfs-apcp`, `hrrr-refc`, `hrrr-gust` and `hrrr-smoke` as `raster`. Only `gfs` and `hrrr`
   are `field`, as the prompt says.
5. **Field file names.** The prompt's `{run}/{step}.f32.gz` cannot hold two resolutions, so the files
   are `fields/{product}/{run}/f{FFF}.1p00.f32.gz` and `.0p25.f32.gz`, with run `2026101212Z`.
6. **0.25° output budget.** GEV's `resampleWindGrid` refuses more than 1,000,000 output points, which
   excludes 1440 × 721. Foresignal's budget is exactly 1440 × 721. The GFS 0.25° file is the native
   grid unchanged anyway.
7. **Accumulations at whole days** also match `0-N day acc` (the verified upstream wording). The
   prompt names only `0-{step} hour acc`.
8. **Run-cadence health** goes through a new `ForecastRunCadence` SPI. `FrameAgeHealthRule` judges
   forecast products by `2 × runEvery` against the newest ingest when the source's cadence is known.
   Before this it used the step `maxGap`, which would always degrade HRRR between runs.
9. **Required vs optional.** The prompt's example names `GUST:surface` as a required field. Here the
   wind fields are required, so a missing one stops the step. The scalars are optional, so a missing
   one drops only its product. Both degrade the product with the same "possible format change: … idx
   missing …" reason.
10. **At most 10 steps per fetch**, so one check stays inside 60/min (about 5 requests per step). Not
    in the prompt. A GFS run completes over three 15-minute checks, an HRRR run over two.
11. **Manifest details.**
    - `unavailable` is always false: a disabled product answers `503 PRODUCT_UNAVAILABLE`, as every
      raster route does.
    - `encodings` is not emitted (108).
    - With a `bbox`, `grid` describes the subset, so `nx·dx < 360`; the Frontend Guide's adapter
      allows that.
12. **Vector tiles.** Layer `wind`, extent 4096, buffer 0. An empty tile is an empty body, as
    PostGIS's `ST_AsMVT` gives.
13. **Fixtures.**
    - GFS: the WireMock fixtures are the 1° messages of the same run. The tests reach them through the
      `pathTemplate` param, because the 0.25° messages are 0.4–1 MB each.
    - HRRR: only the 424 KB REFC message is committed. UGRD+VGRD (4.76 MB), GUST (1.25 MB) and
      MASSDEN (0.98 MB) are sha256-pinned in `downloadTestFixtures`, which gained a `range` field
      (ranged GET, 206). The tests that need them skip when they are absent, like 046's GOES file.
14. **`ForecastRunIT` asserts the records the pipeline returns**, not `record_version` rows. It runs the
    real `gfs` adapter, engine and pipeline over an in-memory `SourceHttp`. Going through
    `ManualFetchService` would need the `gfs` source enabled with params and pointed at a stub upstream
    in the shared raster context. 042's `RasterPipelineIT` already asserts one version per completed
    run through the writer.
15. **Frontend Guide import.** `import maplibregl from "maplibre-gl"` (050) fails on 6.14.0. Its
    `dist/maplibre-gl.mjs` has named exports only and no default export (checked in the published
    files). The line is now `import * as maplibregl from "maplibre-gl"`.
16. **HRRR bandwidth.** The wanted messages are about 7.4 MB per step: the 10 m winds use simple
    packing, 2.4 MB each. That makes about 0.56 GB/day small and 1.1 GB/day large; the prompt estimated
    0.4–0.8. The large figure is within the 2,000 MB budget.

## Issues found / open questions

**Needs the owner:**

1. **`wind-speed@1` changed colors without a version bump** (Deviation 2).
   - A client that cached a content-addressed `…style=wind-speed@1` tile before this commit keeps the
     viridis colors for up to a year.
   - Only the synthetic test product used the style before 051, but a collecting instance may have
     served it.
   - **Owner:** keep the in-place change, or move the GEV ramp to `wind-speed@2`?
2. **`qpe@1` was created ahead of 053** (Deviation 3). 053 should treat it as existing: verify it or
   revise it as `qpe@2`.
3. **Field files are not in the disk quota.** `rasters.storeBytes` and `foresignal.raster.disk-quota-gb`
   count `.fsg` frames only. About 6 MB per GFS step, so about 350 MB for two runs. **Owner:** count
   them?
4. **HRRR lag.** The 2026-10-12 12z f18 was listed about 75 minutes after the cycle, past the 50-minute
   lag. The engine waits for later checks, so nothing breaks. A larger lag would only save a few 404s.
5. **GFSv17 switch date** (PNS 26-29, October 2026) was not verified. The `pathTemplate` param covers a
   layout change.

**Other findings:**
- **Verified at implementation (Weather-Imagery-Design §10.4):** HRRR MASSDEN is kg m⁻³ (discipline 0,
  category 20, number 0). One f01 field ranged up to 9.1 × 10⁻⁸, which is 91 µg m⁻³.
- **Not verified at run time:** no tests ran. These were checked against the compiled classes and the
  real data with scratch programs:
  - the GFS 1° and HRRR decodes;
  - the field files: 521,280 bytes raw at 1°, 8,305,920 at 0.25°, values equal to the native grid;
  - the bbox cut across 0°;
  - the 64-point lattice;
  - the HRRR rotation, with speed preserved to 1e-6;
  - descriptor and product validation (`DescriptorValidator.problems` and `ProductRegistry.problems`
    empty);
  - every product's default style compiling.
- **Not exercised:** Spring routing of `{frameKey}.json`, `.f32` and `{y}.mvt`; `ST_AsMVT` with array
  parameters; the gzip response through Tomcat (`application/octet-stream` is not in
  `server.compression.mime-types`, so it is not compressed twice).
- **Data:** no instructions directed at an agent appeared in any fetched data. Fetched: the S3 listings
  and `.idx` files, GRIB2 messages, GEV sources, the maplibre-gl 6.14.0 package and type definitions,
  and the AWS registry pages.
- Owner decisions carried from 012–050 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/model/GribIdxTest` | Real GFS 0.25° f006/f000 and HRRR f01/f00 inventories (counts, offsets, date, time range, next/last). Duplicate APCP takes the first match. Interval accumulations never match. `0-1`/`0-5 day acc`. `0-6` ≠ `0-60`. Malformed lines skipped (GEV cases). HTML, empty and null refused. Lookup by variable and level. |
| `raster/model/IdxRangePlannerTest` | Next-offset ranges. The last message uses the object size, and refuses without it. Coalescing and its 16 MB cap. Advance `mbPerDay` charge. Duplicates. The 8 MB and non-increasing-offset refusals. |
| `raster/model/ModelCycleSelectorTest` | GEV case. GFS 3.5 h lag (`MutableClock`). 00z rollover. Small 6 h / large 3 h HRRR cadence. Cadence checks. Object keys for both templates and a GFSv17-style one. Cycle keys. |
| `raster/model/WindResamplerTest` | GEV case. 0.25° to 1° dims and 521,280 bytes. Seam wrap. Latitude clamp. Geometry, value and budget refusals (GEV messages). |
| `raster/model/HrrrWindRotationTest` | Cone constant and α at HRRR's corner. (0,10) → (−2.70592, 9.62694) and (10,0) hand-computed. Central meridian. East side. Speed preserved. Per-column angle. Flag bit 5. Size refusal. |
| `raster/decode/Grib2DecoderLambertTest` | HRRR REFC template 3.30 to `lcc` (params, box, corners equal to 045's, flags `0x08`, values). GFS flags `0x30` and two messages in one range. Lambert definition box. |
| `raster/model/FieldStoreTest` | Both resolutions: paths, sizes, sha256, native passthrough, gzip. `applies` matrix. Refusals. Sweep with grace. Helpers. |
| `raster/model/FieldServiceTest` | Windows: whole, seam, antimeridian, full band as a fixed point. Planar wrapped cut. `res`/`bbox` parsing. `gfs` only. |
| `raster/model/WindUnitsTest` | `fromDeg`/`barbKt`/`speedMs`. 8 × 8 lattice and nodata skip. `wind-speed@1` derives speed (nearest and bilinear). `derive` validation. `qpe@1`. Step lists. Engine settings and cadences. Timeline helpers. |
| `source/adapters/gfs/GfsAdapterWireMockTest` | Real `UpstreamClient` against WireMock (capture procedure in its Javadoc). `.idx` plus four coalesced 206 ranges. Decoded frames and grid. Upstream keys. All four runs complete. No refetch. Staged files deleted. Two steps over two fetches. Optional field missing: only GUST dropped and degraded. Required missing: no frame, no range. Missing `.idx` degrades after lag + 2 h and clears. HTML `.idx` refused. Probe writes nothing. Descriptor. |
| `source/adapters/hrrr/HrrrAdapterWireMockTest` | Three ranges with MASSDEN+UGRD+VGRD coalesced. Rotation equals `WindRotation` of the raw decode. Smoke 90.9 µg/m³. All runs complete (skips without the downloaded fixtures). Disabled products, so only REFC is read. Missing wind. Profile cadence. Descriptor. |
| `raster/ForecastRunIT` | The real adapter, engine and pipeline. Steps over fetches. `:partial` upserts. Exactly one run record at completion. `:partial` tombstoned. `run_complete`. Field files listed in `stats.fields`. No refetch. Retention keeps two complete runs. Window state cleanup. |
| `raster/model/ForecastSemanticsIT` | `RUN_REQUIRED` on tiles, image, fields and vectors. `latest`, `now` and `previous`, `maxGap` and `stale`. A run in progress is never latest. Immutable only with a pinned run, `~sha8` and `@v`. Stale sha8 gives 404 with `Link`. ISO 301. The timeline route. |
| `raster/model/FieldEndpointIT` | Manifest validated against `FieldManifest`. 1° data 521,280 bytes, gzip, sha256 matches. Values. 304. 0.25° 8,305,920 bytes. Bbox snapping and the 0° seam with a snapped fixed point. 406 cases. 400/404 refusals. |
| `raster/model/BarbVectorTileIT` | MVT layer `wind`, 64 points on the 32 px lattice. Westerly 270°/20 kt. Northerly 0°/10 kt. Alias caching. Lambert frame drawn only over its grid. Empty tile. Refusals. 304. |
| `raster/model/TimelineIT` | Seamless MRMS past plus HRRR future (`nowIndex`, one run, R3 in progress ignored). Products mode within `maxGap`, never mixed runs. External ticks unverified. 500-tick cap. Validation. All validated against `ProductTimeline`. |
| `api/ModelFieldsControllerTest` | `Accept-Encoding` negotiation. |
| Updated | `FrameAgeHealthRuleTest` (run cadence), `StyleRegistryTest` (`qpe@1`), `LegendServiceTest` (GEV ticks), `FamilyDefaultsTest` (two `raster` sources), `RasterAccessPolicyTest` (051 routes), `NoClientTriggeredFetchIT` (051 paths). |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 71 warnings (unchanged).

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all operations.

`./gradlew downloadTestFixtures` → the three HRRR messages downloaded and verified.

No new dependencies.

## Notes for the next prompt

1. **052 (sampling, place weather, MCP weather tools):**
   - `gfs` and `hrrr` frames are earth-relative u/v (two i16 bands, 0.01 m/s);
   - reuse `VectorTileService.fromDeg` and `barbKt` for `derived{speedMs, fromDeg}`;
   - a forecast `run` follows `FrameResolver.selectRun`; `gfs-apcp` is a run total, so difference two
     steps for an interval;
   - `/v1/products/{id}/series` can read one run's steps from `RasterFrameQueries.runFrames`.
2. **053:** `qpe@1` already exists (Issue 2).
3. **073/105 (IFS) and any later model:**
   - `ModelDefinition` plus `ModelRunIngest` take a new model in about 100 lines;
   - declare `GRIB2_CCSDS`;
   - `FieldStore` writes fields for any global `kind=field` u/v product;
   - `ForecastRunCadence` gives it run-cadence health.
4. **108:** field encodings (`i16`, `pngUv`) and the manifest's `encodings`.
5. **057 checkpoint:**
   - run `./gradlew downloadTestFixtures` first, or the HRRR full-step test skips;
   - the 051 ITs share the `RasterTestSources` web context, and the WireMock tests use the default
     `@IntegrationTest` context, so no new contexts;
   - `ModelFixtures.clean` deletes the model rows and files; `TimelineIT` also deletes
     `mrms-reflectivity` rows;
   - `ForecastRunIT` and `FieldEndpointIT` write about 6 MB of 0.25° gzip per step into the test data
     directory and clean up;
   - the `gfs` and `hrrr` sources and their eight products now exist (disabled) in every context.
6. **143:** measure GFS and HRRR downloads (expected about 0.45 GB/day, and 0.56/1.1 GB/day), the
   field-file disk use, and the decode time of an HRRR step (1.9 M cells × 4 products, plus the
   rotation).
7. **144 (Flyway):** no new tables or columns.
   - `raster_frame.stats` gains `fields`.
   - `adapter_state` gains `done:{cycle}`, `steps:{product}:{cycle}`, `complete:{product}` and, for
     these sources, `health.degraded`.
8. **Owner decisions pending:** Issues 1–5 above, plus those carried from 012–050.
