# 106-CR-Weather-Products-Extended

| | |
|---|---|
| Prompt | 106-Weather-Products-Extended.md |
| Status | Partial: implemented and post-review fixed (see "Post-review fixes"); some prompt tests remain unwritten (listed) |
| Implementation commit | `f389570a94f7d23c0ea380e33345a670727c7879` (the "(full)" commit; `c696038` was a partial first pass) |
| Post-review fix commit | `8172ac1d82c6f8114a633c14cd88e54d1799c021` |
| Date | 2026-10-15 (implementation), 2026-10-16 (post-review fixes) |

## Summary

The product catalog past v1: every source and product in the prompt, the two field encodings, the
position sample, per-product enablement with profile defaults, the budget-outlook warning, and the
place-weather sections they feed. Compiles; tests written, not run.

The first version of this report overstated three things, corrected here and fixed in the post-review
commit: `nexrad-l3` was described as "implemented in full" but ingested nothing (fetch only degraded
health); `rtma-ru` read the hourly plain RTMA file, not the 15-minute RTMA-RU; and the profile defaults
diverged from the prompt's table without being disclosed (and, because the `products` param carried a
descriptor default that the parameter validator applies as if the operator had set it, the profile never
decided anything at runtime).

- **Enablement and budgets (task 1):** `ProductEnablement` (a `products` string-list param with
  small/large profile defaults; the param has no descriptor default, so an unset param means the profile
  default) on every new/extended source; `BudgetOutlookHealthRule` (degraded when the projected day exceeds
  120% of `mbPerDay`). Defaults, as the prompt's table: **small** — GOES-East C13 (v1) + C08/C09,
  RTMA-RU, NBM, OVATION, TAF, HRRR smoke, HeatRisk, UV; **large** — also C02, GOES-18 C13, GFS-Wave, MRMS
  extras, SST, AIFS; mesoscale sectors opt-in on both; `nexrad-l3` opt-in like every source.
- **Satellite (task 2):** `goes-east-abi` adds C08/C09 (`wv-gray@1`/`wv-enhanced@1`), C02 (u8
  reflectance, skipped when the solar zenith exceeds 85° over the whole AOI, cropped in scan-angle space to
  the AOI + 300 km with bounds projected from the fixed grid) and the mesoscale sectors (CMIPM, AOI-gated,
  every 2nd frame); new `goes-west-abi` (GOES-18 C13). New styles `wv-gray@1`, `wv-enhanced@1`,
  `vis-gray@1`.
- **Models and analyses (task 3):** `rtma-ru` (the 15-minute RTMA-RU analysis, `.idx` + ranges on the
  Lambert grid, grid-relative winds rotated); `nbm` (01/07/13/19z via a cycle offset; TMP/WIND/GUST/APCP6/
  PoP6/SKY, with probability-message decoding); `gfs-wave` (HTSGW/PERPW/DIRPW → place-weather `marine`);
  `aifs` (ECMWF AIFS via the 105 engine); `hrrr` gains `hrrr-smoke-column` (COLMD) → place-weather
  `smoke.hrrrColumnMax24h`.
- **Radar extras (task 4):** `mrms` gains `mrms-lightning-prob`, `mrms-vil`, `mrms-echo-top-50` (behind
  `products`) → place-weather `lightning.prob30mMaxInRadius`; `nexrad-l3` lists each site's day prefix with
  `start-after`, downloads, decodes N0B (153) and N0G (154) with NetCDF-Java's NIDS IOSP, resamples to
  0.005° within 230 km and commits u8 frames.
- **Global and records (task 5):** `swpc-aurora` gains the global `aurora-probability` raster (shared
  fetch); `sst-geopolar` (ERDDAP NetCDF subset); `awc-tafs` gains `issueTime`/`validFrom`/`validTo`/
  `rawText`/`forecast[]`.
- **Heat and UV (task 6):** `wpc-heatrisk` (days 1–7 GeoTIFFs, categorical u8 on the file's EPSG:3857
  `affine` grid → place-weather `heat`); `epa-uv` (per-place hourly UV, `zip` attribute, externalId
  `place:{id}` → place-weather `uv`).
- **Field encodings (task 7):** `velocity-json` and `png-uv` endpoints, the manifest `encodings` block,
  the 300,000-point cap (`FIELD_TOO_LARGE`).
- **Position sample (task 8):** `/v1/sample?record=` (live position, `stale-position`), `POST
  /v1/sample/track`, MCP `sample_weather` `record` + `sample_weather_along_track`.
- **Spec/docs (task 9):** `uv-forecast` kind, the three operations flipped to `implemented` and removed
  from `pending-operations.txt`, changelog, Postman, Source-Catalog rows for every new source,
  Operator-Guide and Frontend-Guide sections.

## Files created / modified

**Created (main, implementation):** `raster/model/VelocityJsonEncoder`, `raster/model/PngUvEncoder`,
`raster/ProductEnablement`, `collect/BudgetOutlookHealthRule`, `raster/geotiff/GeoTiffValues`,
`source/adapters/gfswave/GfsWaveAdapter`, `source/adapters/ecmwf/AifsAdapter`,
`source/adapters/rtma/RtmaAdapter`, `source/adapters/nbm/NbmAdapter`,
`source/adapters/sst/SstAdapter`, `source/adapters/heatrisk/HeatRiskAdapter`,
`source/adapters/env/epauv/EpaUvAdapter`, `source/adapters/nexrad/{NexradL3Adapter,Wsr88dSites,NidsDecoder}`,
`source/adapters/goes/GoesWestAbiAdapter`, `source/adapters/awc/TafGroups`,
`mcp/tools/{RecordPositions,SampleWeatherAlongTrackTool}`,
`resources/reference/wsr88d-sites.csv`,
`resources/raster/styles/{smoke@2,wave@1,ltg-prob@1,vil@1,echotop@1,wv-gray@1,wv-enhanced@1,vis-gray@1,aurora@1,sst@1,heatrisk@1,velocity@1}.json`.

**Created (main, post-review):** `source/adapters/nexrad/{NidsRadialReader,PolarScan,PolarResampler}`.

**Modified (main):** `source/spi/PlacePoints`,
`raster/decode/{DecodeRequest,Grib2Decoder,GeosGrids}`, `raster/model/{GribIdx,FieldSelector,ModelDefinition,
ModelCycleSelector,ModelRunIngest}`, `raster/model/FieldService` (encodings),
`source/adapters/mrms/{MrmsAdapter,MrmsCodec}`, `source/adapters/hrrr/HrrrAdapter`,
`source/adapters/goes/GoesAbiAdapter` (multi-channel), `source/adapters/awc/TafParser`,
`source/adapters/space/swpc/{SwpcAuroraAdapter,OvationSampler}`,
`source/adapters/wx/{WxPlacesGenerator,PlaceWeatherState}`, `place/PlaceService` (attributes/timezone),
`api/{ModelFieldsController,SampleController}`, `api/error/ErrorCodes`,
`raster/sample/{SampleService,SampleStatus,TimeSpec}`, `security/AccessPolicy`.

**Docs/spec/build:** `docs/OpenAPI.json`, `postman/` (regenerated), `scripts/generate-postman.py`
(BODIES), `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Frontend-Guide.md`,
`src/test/resources/openapi/pending-operations.txt` (−3 lines); post-review: `build.gradle.kts`,
`gradle/libs.versions.toml`, `gradle/verification-metadata.xml` (`edu.ucar:cdm-radial`),
`THIRD_PARTY_NOTICES.md`.

**Tests:** `raster/model/{VelocityJsonEncoderTest,PngUvEncoderTest}`, `raster/ProductProfileDefaultsTest`,
`raster/sample/SampleTrackStatusTest`, `source/adapters/mrms/MrmsExtrasCodecTest`,
`source/adapters/awc/TafGroupsTest`, `source/adapters/nexrad/{NidsDecoderTest,Wsr88dSitesTest}`,
`source/adapters/heatrisk/HeatRiskLinksTest`, `source/adapters/env/epauv/EpaUvTimeTest`,
`source/adapters/space/swpc/OvationRasterTest`; `retention/FamilyDefaultsTest` updated (6 new adapters,
`uv-forecast`, the swpc-aurora shorter raster-frame window). Post-review additions are listed under
"Post-review fixes".

## Deviations from the prompt

1. **`nexrad-l3` decodes with `edu.ucar:cdm-radial`, a NetCDF-Java module the build did not have.** The
   first version stated that NetCDF-Java 5.11.0 has no NIDS IOSP; that was true of `cdm-core` alone. The
   NIDS reader (`ucar.nc2.iosp.nids.Nidsiosp`) is in `cdm-radial` 5.11.0 (BSD-3, listed in the 5.11.0
   BOM, same Unidata repository and `exclusiveContent` rule; jar SHA-1 matches the repository's). It
   decodes products 153 and 154 (verified 2026-10-16 on live files). Both N0B and N0G are implemented, so
   the prompt's N0B-only fallback is not needed. `docs/Architecture.md` §2 and Weather-Imagery-Design §0.1
   still list the modules as "cdm-core, grib"; they need a line for `cdm-radial` (not edited here).
2. **`nws-reflectivity@1`, not `@3`, for N0B.** No `nws-reflectivity@3` style exists; `@1` is the same
   dBZ palette MRMS and HRRR use.
3. **With `siteCount` above 1, the sites share the two `nexrad-l3` products**, each frame on its own
   site's grid, so an animation alternates sites. Default is one site.
4. **A dedicated RTMA adapter, not the 051 engine.** RTMA-RU is an analysis (an observation), while
   `ModelRunIngest` commits forecast frames; RTMA gets its own observation ingest reusing
   `GribIdx`/`FieldSelector`/`IdxRangePlanner`/`Grib2Decoder`/`WindRotation`.
5. **RTMA-RU budget is 3,600 MB/day.** Measured from the live `.idx`: about 35 MB per analysis for the five
   products, 3.4 GB/day at 15 minutes. The prompt keeps RTMA-RU on the small profile; an `analysisEvery`
   param (`30m`, `60m`) thins it.
6. **Probability decimal and 6-hour buckets.** `GribIdx.Message` gains `extra`, `FieldSelector` gains
   `probabilistic`/`bucketStart`/`bucketEnd`, `ModelDefinition.Component` gains `probabilistic`/
   `bucketHours` and `DecodeRequest` gains `probability`; the constructors keep their old arities.
7. **NBM wind is wind speed** (`WIND`, (0,2,1)); HRRR/IFS/AIFS keep the u/v field. NBM PoP is stored
   with a ×100 factor (the probability message's scale is unverified); the field is flagged.
8. **NBM cycles come from a cycle offset.** `ModelDefinition` gains `cycleOffset` (default zero, old
   constructor kept) and `ModelCycleSelector.candidates` an overload with the offset; NBM sets 1 h.
9. **SST grid is built by the adapter.** NetCDF-Java has no lat/lon NetCDF grid path here, so the SST
   adapter reads the ERDDAP subset with `NetcdfDecoder.Reader` and builds the latlon grid itself.
10. **HeatRisk is stored on an `affine` EPSG:3857 grid** (the file's geotransform) rather than resampled
    to latlon; the affine grid inverts the Mercator rows exactly. Categories are the palette indices 0–4;
    anything else is nodata.
11. **`swpc-ovation` is not a separate source.** Per the prompt's "share one source", the global
    `aurora-probability` raster is committed by the 077 `swpc-aurora` source from its single conditional
    GET; the source becomes multi-kind (its record kind + `raster-frame`).
12. **PlacePoints gains `attributes`/`timezone`** (default methods) so `epa-uv` can read a place's ZIP
    and zone without an SPI-breaking change; `epa-uv` also takes `PlaceTimeZones` for its fallback.
13. **`velocity-json`/`png-uv` are served for the global `kind=field` winds** (`gfs`, `ifs`, `aifs`);
    HRRR's Lambert winds still use barb vectors and the speed raster (051 writes field files for global
    grids only).
14. **No automatic ECMWF host failover** (carried from 105); `pathTemplate` switches the host.
15. **One new dependency** (post-review): `edu.ucar:cdm-radial` 5.11.0, version from the existing BOM.

## Issues found / open questions

- **New dependency** `edu.ucar:cdm-radial` 5.11.0 (BSD-3, from the NetCDF-Java BOM already used, checksum-pinned)
  for the NIDS reader. `docs/Architecture.md` §2 and Weather-Imagery-Design §0.1 still list only `cdm-core` and
  `grib` as the NetCDF-Java modules; they were not edited. Should they gain `cdm-radial`?
- **NBM PoP scale** and the **HeatRisk category mapping** (palette indices, GDAL nodata 53) are
  best-effort; a checkpoint with real data should confirm them.
- **Place-weather payload** changed again (marine/aurora/heat/uv always present; smoke/lightning gain
  metrics); 052/053/105 place-weather tests likely need updates at the checkpoints.
- New sources and MCP tools may affect exact-set tests (`FamilyDefaultsTest` is updated).
- **Performance unmeasured** in the application: the GOES C02 decode (cropped after decode), NBM/RTMA
  downloads, the SST subset, the HeatRisk GeoTIFF read. The NEXRAD resampling took about 70 ms per scan in
  a standalone check.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/model/VelocityJsonEncoderTest` | Two components, header fields, row-major data, parameter numbers, refTime/forecastTime; a wrong-length buffer refused |
| `raster/model/PngUvEncoderTest` | R = u, G = v, B = 0 with bounds; flat field; channel scale/clamp |
| `raster/ProductProfileDefaultsTest` | (rewritten) The real adapters' small/large defaults per the prompt's table; MRMS extras by profile; no `products` descriptor default on any source |
| `raster/model/ModelCycleSelectorTest` | (extended) NBM 01/07/13/19z via the cycle offset; zero offset unchanged; bad offsets refused |
| `raster/sample/SampleDisagreementTest` (post-review) | `/v1/sample`: `aifs-t2m` is an extra disagreement model; run totals stay out |
| `source/adapters/wx/PlaceDisagreementTest` (post-review, written with the 105 fixes) | `wx-places`: AIFS joins t2m and next-24-hour precipitation |
| `source/adapters/wx/PlaceUvKeyTest` (post-review) | The place-weather `uv` section reads `epa-uv` records keyed `place:{id}` |
| `raster/sample/SampleTrackStatusTest` | `maxGap` staleness, worst-status ranking |
| `source/adapters/goes/GoesWvAndVisDecodeTest` | Scan-angle crop window, geographic cropped bounds, index consistency, off-disc/clamped windows, whole-AOI solar zenith |
| `source/adapters/rtma/RtmaRuDecodeTest` | RTMA-RU keys, 15-minute candidates, `analysisEvery`, state keys, wind rotation on Lambert, refusal elsewhere, descriptor budget |
| `source/adapters/heatrisk/HeatRiskGeoTiffTest` | Affine EPSG:3857 grid; a mid-CONUS latitude on its Mercator row |
| `source/adapters/nexrad/NexradL3DecodeTest` | Real N0B/N0G fixtures through the NIDS IOSP; resampling counts; bearing/gate selection; codes; key grammar |
| `source/adapters/nexrad/NexradL3AdapterTest` | WireMock: day-prefix listing, lookback, download, decode, commit, `start-after`, N0G, undecodable file |
| `source/adapters/hrrr/HrrrSmokeFieldsTest` | Column smoke 0 … 10 g/m² fits i16; quiet-day resolution |
| `source/adapters/env/epauv/EpaUvAdapterTest` | `place:{id}`; timezone fallback place → gazetteer → none; missing-ZIP note; ZIP shape |
| `source/adapters/mrms/MrmsExtrasCodecTest` | VIL/echo-top/lightning-probability codes and sentinels; products registered |
| `source/adapters/awc/TafGroupsTest` | INITIAL/FM/TEMPO groups, wind/gust/visibility/wx/clouds, validity |
| `source/adapters/nexrad/NidsDecoderTest` | WMO/AWIPS text stripping, product code and radar position |
| `source/adapters/nexrad/Wsr88dSitesTest` | 160 sites loaded, nearest selection |
| `source/adapters/heatrisk/HeatRiskLinksTest` | 7 daily GeoTIFF links, duplicates ignored |
| `source/adapters/env/epauv/EpaUvTimeTest` | `Oct/16/2026 07 AM` in the place zone |
| `source/adapters/space/swpc/OvationRasterTest` | `rowMajorNorthSouth` layout |
| `retention/FamilyDefaultsTest` (updated) | 6 new adapters classified; `uv-forecast`; the swpc-aurora short raster-frame window |

**Prompt tests still not written:** `NbmIdxPlanTest`, `GfsWaveDecodeTest`, `AifsPlanTest`, `SstErddapTest`,
`TafParserContractTest` (beyond `TafGroupsTest`), `EpaUvParserContractTest` (beyond `EpaUvTimeTest`) and the
WireMock `RtmaRuAdapterTest`, `NbmAdapterTest`, `OvationAdapterTest`, `HeatRiskAdapterTest`, `EpaUvAdapterTest`
(fetch path), `TafAdapterTest`; `PositionSampleIT`; the `taf`/`uv-forecast` ingest ITs. Each GRIB/NetCDF one
needs a captured fixture (≤ 2 MB range read) that has not been taken.

## Compile result

`./gradlew compileJava compileTestJava` → success (post-review). Implementation: `./gradlew lintOpenApi`
valid; Postman regenerated (835 requests). The post-review pass changed no operation.

## Notes for the next prompt

- **107/108** are independent; 108 reuses `PngUvEncoder` for tiled `.png` fields and the `i16`/`pngTiles`/
  `tilejson` manifest slots.
- **143** should measure the new downloads (GOES C08/C09/C02/meso/West, RTMA-RU at 15 minutes, NBM,
  GFS-Wave, AIFS, MRMS extras, SST, NEXRAD L3) and the C02 crop cost; **144 (Flyway)** adds no tables
  beyond `intelligence_event.context`.
- The `products` param and `ProductEnablement` are the pattern for any further product; never give the
  param a descriptor default.

## Post-review fixes (2026-10-16)

Commit `106: fix post-review defects`.

1. **HIGH — GOES C02 crop in mixed units.** Fixed. `croppedGrid` added `col0 · |dx|` (radians) to the
   parent's west longitude. The crop window is now worked out in scan-angle space (points along the
   widened AOI box's sides mapped with `GeosGrid.scanAngles`, clamped to the image) and the cropped grid's
   box is projected back from scan angles (`GeosGrids.boxOf`). Test: `GoesWvAndVisDecodeTest`.
2. **HIGH — NBM cycles 00/06/12/18z.** Fixed. `ModelDefinition.cycleOffset` and an offset overload of
   `ModelCycleSelector.candidates`; NBM uses 1 h (01/07/13/19z, as its Javadoc and Source-Catalog say).
   GFS, HRRR, GFS-Wave and the ECMWF engine are unchanged (offset 0). Test: `ModelCycleSelectorTest`.
3. **MEDIUM — profile defaults.** Fixed. NBM and HeatRisk on for both; GFS-Wave uses the profile (was
   hard-coded "small") and is large-only; GOES-18 C13 large-only; MRMS extras gain a `products` param,
   large-only. A further defect found while fixing it: every `products` param had a descriptor default,
   which `ParamValidator` applies as if set, so the profile never applied; the defaults are removed.
   AIFS/GFS-Wave/NBM no longer ingest the probe product when nothing is enabled.
   `ProductProfileDefaultsTest` now reads the adapters' own declarations.
4. **MEDIUM — RTMA vs RTMA-RU, no wind rotation.** Fixed. Verified 2026-10-16 against a live listing:
   `rtma2p5_ru.{yyyyMMdd}/rtma2p5_ru.t{HHmm}z.2dvaranl_ndfd.grb2` at :00/:15/:30/:45. Grid-relative winds
   are rotated with `WindRotation` (refused on a non-Lambert grid). Test: `RtmaRuDecodeTest`.
5. **MEDIUM — HeatRisk Mercator rows as linear latitude.** Fixed by storing the frame on the file's
   EPSG:3857 `affine` grid, which inverts the Mercator projection per row exactly. Test:
   `HeatRiskGeoTiffTest`.
6. **MEDIUM — AIFS not in the disagreement flag.** Fixed: AIFS joins the model-disagreement comparison in
   `wx-places`, `/v1/sample` and the forecast series provider list. In `wx-places`, `aifs-t2m` joins the t2m
   maximum and `aifs-tp` (a run total) the next-24-hour precipitation; this change is in the 105 fix commit,
   because it rewrote the same method. `/v1/sample` already took every `-t2m` product (now documented and
   tested); `ForecastSeriesService` gains the `aifs` provider (wind, t2m, precipitation) and the OpenAPI
   `ForecastSeries.provider` enum gains `aifs` (changelog entry added).
7. **MEDIUM — `nexrad-l3` ingested nothing.** Fixed with a real implementation (see Deviation 1):
   listing with `start-after`, download, NIDS decode through `SafeNetcdf`, resampling, u8 commit; the
   probe now decodes the newest scan of the day prefix (it used to list from the start of the archive).
   Real fixtures committed. Tests: `NexradL3DecodeTest`, `NexradL3AdapterTest`.
8. **LOW — HRRR column smoke, EPA UV, solar zenith.** Fixed. Column smoke is i16 at 1 mg/m² from an
   offset (0 … 65.5 g/m²; was 10 µg/m², saturating at 0.33 g/m² when a quiet day already reaches
   0.071 g/m²). EPA UV externalId is `place:{id}` (tombstones too), the timezone falls back to the
   gazetteer zone at the place and then to UTC with a warning, and places without a ZIP are named in the
   health note; the place-weather `uv` section now looks the records up by `place:{id}`. The daytime check takes the smallest zenith over a 3 × 3 lattice of the AOI. Tests:
   `HrrrSmokeFieldsTest`, `EpaUvAdapterTest`, `GoesWvAndVisDecodeTest`.
9. **Report inaccuracies.** Corrected above: `nexrad-l3`, RTMA vs RTMA-RU, and the profile defaults.

Implementation commit: `f389570a94f7d23c0ea380e33345a670727c7879`
