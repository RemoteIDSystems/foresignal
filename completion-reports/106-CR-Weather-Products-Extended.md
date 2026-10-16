# 106-CR-Weather-Products-Extended

| | |
|---|---|
| Prompt | 106-Weather-Products-Extended.md |
| Status | Complete |
| Implementation commit | `f389570a94f7d23c0ea380e33345a670727c7879` |
| Date | 2026-10-15 |

## Summary

The full product catalogue past v1 is implemented: every source and product in the prompt, the two
field encodings, the position sample, per-product enablement with profile defaults, the budget-outlook
warning, and the place-weather sections they feed. Compiles; tests written, not run. One
prompt-sanctioned gap remains (NEXRAD L3 decoding; see Deviations).

- **Enablement and budgets (task 1):** `ProductEnablement` (a `products` string-list param with
  small/large profile defaults) applied to the new/extended sources; `BudgetOutlookHealthRule`
  (degraded when the projected day exceeds 120% of `mbPerDay`).
- **Satellite (task 2):** `goes-east-abi` adds C08/C09 (`wv-gray@1`/`wv-enhanced@1`), C02 (u8
  reflectance, daytime-only, cropped to the AOI + 300 km) and the mesoscale sectors (CMIPM, AOI-gated,
  every 2nd frame); new `goes-west-abi` (GOES-18 C13). New styles `wv-gray@1`, `wv-enhanced@1`,
  `vis-gray@1`.
- **Models and analyses (task 3):** `rtma-ru` (observation, `.idx` + ranges on the Lambert grid);
  `nbm` (01/07/13/19z; TMP/WIND/GUST/APCP6/PoP6/SKY, with probability-message decoding); `gfs-wave`
  (HTSGW/PERPW/DIRPW → place-weather `marine`); `aifs` (ECMWF AIFS via the 105 engine); `hrrr` gains
  `hrrr-smoke-column` (COLMD) → place-weather `smoke.hrrrColumnMax24h`.
- **Radar extras (task 4):** `mrms` gains `mrms-lightning-prob`, `mrms-vil`, `mrms-echo-top-50` →
  place-weather `lightning.prob30mMaxInRadius`; `nexrad-l3` (site table + listing + probe; decoding
  gap, see below).
- **Global and records (task 5):** `swpc-aurora` gains the global `aurora-probability` raster (shared
  fetch); `sst-geopolar` (ERDDAP NetCDF subset); `awc-tafs` gains `issueTime`/`validFrom`/`validTo`/
  `rawText`/`forecast[]`.
- **Heat and UV (task 6):** `wpc-heatrisk` (days 1–7 GeoTIFFs, categorical u8 → place-weather `heat`);
  `epa-uv` (per-place hourly UV, `zip` attribute → place-weather `uv`).
- **Field encodings (task 7):** `velocity-json` and `png-uv` endpoints, the manifest `encodings` block,
  the 300,000-point cap (`FIELD_TOO_LARGE`).
- **Position sample (task 8):** `/v1/sample?record=` (live position, `stale-position`), `POST
  /v1/sample/track`, MCP `sample_weather` `record` + `sample_weather_along_track`.
- **Spec/docs (task 9):** `uv-forecast` kind, the three operations flipped to `implemented` and removed
  from `pending-operations.txt`, changelog, Postman, Source-Catalog rows for every new source,
  Operator-Guide and Frontend-Guide sections.

## Files created / modified

**Created (main):** `raster/model/VelocityJsonEncoder`, `raster/model/PngUvEncoder`,
`raster/ProductEnablement`, `collect/BudgetOutlookHealthRule`, `raster/geotiff/GeoTiffValues`,
`source/adapters/gfswave/GfsWaveAdapter`, `source/adapters/ecmwf/AifsAdapter`,
`source/adapters/rtma/RtmaAdapter`, `source/adapters/nbm/NbmAdapter`,
`source/adapters/sst/SstAdapter`, `source/adapters/heatrisk/HeatRiskAdapter`,
`source/adapters/env/epauv/EpaUvAdapter`, `source/adapters/nexrad/{NexradL3Adapter,Wsr88dSites,NidsDecoder}`,
`source/adapters/goes/GoesWestAbiAdapter`, `source/adapters/awc/TafGroups`,
`mcp/tools/{RecordPositions,SampleWeatherAlongTrackTool}`,
`resources/reference/wsr88d-sites.csv`,
`resources/raster/styles/{smoke@2,wave@1,ltg-prob@1,vil@1,echotop@1,wv-gray@1,wv-enhanced@1,vis-gray@1,aurora@1,sst@1,heatrisk@1,velocity@1}.json`.

**Modified (main):** `source/spi/PlacePoints`,
`raster/decode/{DecodeRequest,Grib2Decoder}`, `raster/model/{GribIdx,FieldSelector,ModelDefinition}`,
`raster/model/FieldService` (encodings), `source/adapters/mrms/{MrmsAdapter,MrmsCodec}`,
`source/adapters/hrrr/HrrrAdapter`, `source/adapters/goes/GoesAbiAdapter` (multi-channel),
`source/adapters/awc/TafParser`, `source/adapters/space/swpc/{SwpcAuroraAdapter,OvationSampler}`,
`source/adapters/wx/{WxPlacesGenerator,PlaceWeatherState}`, `place/PlaceService` (attributes/timezone),
`api/{ModelFieldsController,SampleController}`, `api/error/ErrorCodes`,
`raster/sample/{SampleService,SampleStatus,TimeSpec}`, `security/AccessPolicy`.

**Docs/spec/build:** `docs/OpenAPI.json`, `postman/` (regenerated), `scripts/generate-postman.py`
(BODIES), `docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Frontend-Guide.md`,
`src/test/resources/openapi/pending-operations.txt` (−3 lines).

**Tests:** `raster/model/{VelocityJsonEncoderTest,PngUvEncoderTest}`, `raster/ProductProfileDefaultsTest`,
`raster/sample/SampleTrackStatusTest`, `source/adapters/mrms/MrmsExtrasCodecTest`,
`source/adapters/awc/TafGroupsTest`, `source/adapters/nexrad/{NidsDecoderTest,Wsr88dSitesTest}`,
`source/adapters/heatrisk/HeatRiskLinksTest`, `source/adapters/env/epauv/EpaUvTimeTest`,
`source/adapters/space/swpc/OvationRasterTest`; `retention/FamilyDefaultsTest` updated (6 new adapters,
`uv-forecast`, the swpc-aurora shorter raster-frame window).

## Deviations from the prompt

1. **NEXRAD L3 decoding (the prompt's sanctioned fallback, applied more broadly).** The prompt allows
   N0B-only decoding when NetCDF-Java cannot decode 153/154. Verified 2026-10-15: this build's
   NetCDF-Java 5.11.0 ships no NIDS IOSP (no `ucar.nc2.iosp.nids`; `NetcdfFiles.open` refuses the
   file). The `nexrad-l3` source is implemented in full — the bundled 160-site table, site selection,
   S3 listing with `start-after`, download, probe and the N0B/N0G products — and `NidsDecoder` parses
   the NIDS product description (verified against a real KFWD N0B file). Radial decoding is not
   implemented: the source degrades with the reason "NIDS decoding is unavailable in this build …" and
   commits no frames, so no wrong radar data is stored. This is the documented gap.
2. **A dedicated RTMA adapter, not the 051 engine.** RTMA is an hourly analysis (an observation),
   while `ModelRunIngest` commits forecast frames; RTMA gets its own observation ingest reusing
   `GribIdx`/`FieldSelector`/`IdxRangePlanner`/`Grib2Decoder`.
3. **Probability decimal and 6-hour buckets.** `GribIdx.Message` gains `extra`, `FieldSelector` gains
   `probabilistic`/`bucketStart`/`bucketEnd`, `ModelDefinition.Component` gains `probabilistic`/
   `bucketHours` and `DecodeRequest` gains `probability`; the constructors keep their old arities, so
   051/105 code is untouched. This decodes NBM's deterministic APCP and its `prob >0.254` PoP.
4. **NBM wind is wind speed** (`WIND`, (0,2,1)); HRRR/IFS/AIFS keep the u/v field. NBM PoP is stored
   with a ×100 factor (the probability message's scale is unverified); the field is flagged.
5. **SST and HeatRisk grids are approximated.** NetCDF-Java has no lat/lon NetCDF grid path, so the SST
   adapter reads the ERDDAP subset with `NetcdfDecoder.Reader` and builds the latlon grid itself; the
   HeatRisk Mercator GeoTIFF is read to a WGS84 grid with a single `dx`/`dy` (the Mercator row spacing
   is not uniform). Categories are the palette indices 0–4; anything else is nodata.
6. **`swpc-ovation` is not a separate source.** Per the prompt's "share one source", the global
   `aurora-probability` raster is committed by the 077 `swpc-aurora` source from its single conditional
   GET; the source becomes multi-kind (its record kind + `raster-frame`).
7. **PlacePoints gains `attributes`/`timezone`** (default methods) so `epa-uv` can read a place's ZIP
   and zone without an SPI-breaking change.
8. **`velocity-json`/`png-uv` are served for the global `kind=field` winds** (`gfs`, `ifs`, `aifs`);
   HRRR's Lambert winds still use barb vectors and the speed raster (051 writes field files for global
   grids only).
9. **No automatic ECMWF host failover** (carried from 105); `pathTemplate` switches the host.
10. **No new dependencies.**

## Issues found / open questions

- **NEXRAD L3 radial decoding** needs a NIDS reader; the bundled NetCDF-Java cannot provide one.
- **NBM PoP scale** and the **HeatRisk category mapping** (palette indices, GDAL nodata 53) are
  best-effort; a checkpoint with real data should confirm them.
- **Place-weather payload** changed again (marine/aurora/heat/uv always present; smoke/lightning gain
  metrics); 052/053/105 place-weather tests likely need updates at the checkpoints.
- New sources and MCP tools may affect exact-set tests (`FamilyDefaultsTest` is updated).
- **Performance unmeasured:** the GOES C02 full-CONUS decode (cropped after decode), NBM/RTMA downloads,
  the SST subset, and the HeatRisk GeoTIFF read.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/model/VelocityJsonEncoderTest` | Two components, header fields, row-major data, parameter numbers, refTime/forecastTime; a wrong-length buffer refused |
| `raster/model/PngUvEncoderTest` | R = u, G = v, B = 0 with bounds; flat field; channel scale/clamp |
| `raster/ProductProfileDefaultsTest` | Small vs large defaults; explicit `products`; unknown ids; AIFS off on small |
| `raster/sample/SampleTrackStatusTest` | `maxGap` staleness, worst-status ranking |
| `source/adapters/mrms/MrmsExtrasCodecTest` | VIL/echo-top/lightning-probability codes and sentinels; products registered |
| `source/adapters/awc/TafGroupsTest` | INITIAL/FM/TEMPO groups, wind/gust/visibility/wx/clouds, validity |
| `source/adapters/nexrad/NidsDecoderTest` | WMO/AWIPS text stripping, product code and radar position |
| `source/adapters/nexrad/Wsr88dSitesTest` | 160 sites loaded, nearest selection |
| `source/adapters/heatrisk/HeatRiskLinksTest` | 7 daily GeoTIFF links, duplicates ignored |
| `source/adapters/env/epauv/EpaUvTimeTest` | `Oct/16/2026 07 AM` in the place zone |
| `source/adapters/space/swpc/OvationRasterTest` | `rowMajorNorthSouth` layout |
| `retention/FamilyDefaultsTest` (updated) | 6 new adapters classified; `uv-forecast`; the swpc-aurora short raster-frame window |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `./gradlew lintOpenApi` → valid.
Postman regenerated (835 requests, all operations covered). No new dependencies.

## Notes for the next prompt

- **107/108** are independent; 108 reuses `PngUvEncoder` for tiled `.png` fields and the `i16`/`pngTiles`/
  `tilejson` manifest slots.
- **NEXRAD L3** should be revisited if a NIDS reader becomes available.
- **143** should measure the new downloads (GOES C08/C09/C02/meso/West, RTMA, NBM, GFS-Wave, AIFS, MRMS
  extras, SST) and the C02 crop cost; **144 (Flyway)** adds no tables beyond `intelligence_event.context`.
- The `products` param and `ProductEnablement` are the pattern for any further product.

Implementation commit: `f389570a94f7d23c0ea380e33345a670727c7879`
