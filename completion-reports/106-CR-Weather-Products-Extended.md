# 106-CR-Weather-Products-Extended

| | |
|---|---|
| Prompt | 106-Weather-Products-Extended.md |
| Status | Partial |
| Implementation commit | `c6960386e4327a7aafc827bb267932ffe57b75e2` |
| Date | 2026-10-15 |

## Summary

Extended the weather catalogue with the client-facing pieces and a first wave of new products, and
recorded the rest as deferred. Compiles; tests written, not run.

Delivered in full:
- **Field encodings (task 7).** `VelocityJsonEncoder` and `PngUvEncoder`, the two new endpoints
  `getProductFieldVelocityJson` / `getProductFieldPngUv`, the `encodings` block in the field manifest,
  and the `400 FIELD_TOO_LARGE` cap (300,000 points per component). Served for the global
  `kind=field` wind products (`gfs`, `ifs`, `aifs`).
- **Position sample (task 8).** `/v1/sample?record={sourceId}/{externalId}` samples at an entity's
  live position under the 039 private/restriction gate, with a `position` block and `stale-position`
  after 15 minutes; `POST /v1/sample/track` samples each point of a track at its own time; the
  `UNKNOWN_RECORD` and `stale-position` codes; the AccessPolicy row; MCP `sample_weather` gains
  `record`, and the new `sample_weather_along_track` tool.
- **Product enablement (task 1).** `ProductEnablement` (a `products` string-list param with profile
  defaults) applied to `hrrr`, `gfs-wave` and `aifs`, and a new `BudgetOutlookHealthRule` degrading a
  source whose projected day's download exceeds 120% of `mbPerDay`.
- **Products:** `hrrr-smoke-column` (HRRR COLMD, column smoke) + place-weather `smoke.hrrrColumnMax24h`;
  `gfs-wave` (HTSGW/PERPW/DIRPW) + place-weather `marine` for `coastal` places; `aifs` (ECMWF AIFS
  Single). New styles `smoke@2`, `wave@1`, `ltg-prob@1`, `vil@1`, `echotop@1`. Place-weather gains
  `marine`, `aurora`, `heat` and `uv` sections (the latter three are `unavailable` placeholders; see
  Deviations).
- **Spec/Postman/docs:** the three pre-seeded operations flipped to `implemented` and removed from
  `pending-operations.txt`; `uv-forecast` kind and a changelog entry; Postman regenerated (BODIES for
  `sampleTrack`); Source-Catalog rows for `gfs-wave` and `aifs`; Operator-Guide and Frontend-Guide
  sections.

## Files created / modified

**Created (main):** `raster/model/VelocityJsonEncoder`, `raster/model/PngUvEncoder`,
`raster/ProductEnablement`, `collect/BudgetOutlookHealthRule`,
`source/adapters/gfswave/GfsWaveAdapter`, `source/adapters/ecmwf/AifsAdapter`,
`mcp/tools/RecordPositions`, `mcp/tools/SampleWeatherAlongTrackTool`,
`resources/raster/styles/{smoke@2,wave@1,ltg-prob@1,vil@1,echotop@1}.json`.

**Modified (main):** `api/ModelFieldsController` (two endpoints, point cap), `api/SampleController`
(`record=`, `sampleTrack`), `api/error/ErrorCodes` (`FIELD_TOO_LARGE`, `UNKNOWN_RECORD`),
`raster/model/FieldService` (`encodings`), `raster/sample/SampleService` (`sampleTrack`,
`MAX_TRACK_POINTS`), `raster/sample/SampleStatus` (`stale-position`), `raster/sample/TimeSpec` (`at`),
`security/AccessPolicy`, `source/adapters/hrrr/HrrrAdapter` (`hrrr-smoke-column`, `products` param),
`source/adapters/wx/WxPlacesGenerator` + `PlaceWeatherState` (marine/aurora/heat/uv, smoke column,
lightning probability), `mcp/tools/SampleWeatherTool` (`record`).

**Docs/spec:** `docs/OpenAPI.json`, `postman/` (regenerated), `scripts/generate-postman.py` (BODIES),
`docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `docs/Frontend-Guide.md`,
`src/test/resources/openapi/pending-operations.txt` (−3 lines).

**Tests:** `raster/model/VelocityJsonEncoderTest`, `PngUvEncoderTest`,
`raster/ProductProfileDefaultsTest`, `raster/sample/SampleTrackStatusTest`; `retention/FamilyDefaultsTest`
updated (`GfsWaveAdapter`, `AifsAdapter`).

## Deviations from the prompt

1. **Partial delivery.** Tasks 2 (satellite C08/C09/C02/mesoscale and `goes-west-abi`), the RTMA-RU and
   NBM models, the MRMS extras *products* (lightning probability, VIL, echo top), `nexrad-l3`,
   the `swpc-ovation` raster, `sst-geopolar`, `wpc-heatrisk` and `epa-uv` are **not implemented**. The
   place-weather `aurora`, `heat` and `uv` sections exist as always-`unavailable` placeholders, and the
   `lightning.prob30mMaxInRadius` metric is computed only when an `mrms-lightning-prob` product exists
   (it does not yet), so it is currently omitted. This is a deliberate scoping decision; the three
   client-facing operations the spec pre-seeded are complete.
2. **`hasFields` still requires global coverage.** HRRR's Lambert winds are not stored as field files
   (051 writes global lat/lon `kind=field` only), so `velocity-json`/`png-uv` are served for `gfs`,
   `ifs` and `aifs` (and any future global wind model), not HRRR. HRRR keeps the barb vectors and the
   `wind-speed` raster. The `requireFields` message still says "gfs only" (cosmetic).
3. **`png-uv` is not gzipped** (PNG is already compressed); `velocity-json` is gzipped like `.f32`.
   Both follow `RasterCachePolicy` and support 304.
4. **Track sampling uses the product's `maxGap`** chosen by `SampleService.choose`; a request `maxGap`
   is applied as a post-check that marks an otherwise-`ok` value `stale` when its frame is older than
   it, rather than changing frame selection.
5. **`sample_weather`'s `record`** resolves the live position and includes a `record` key in the output;
   it does not (yet) emit the `position` block that the REST `getSample` does.
6. **HRRR COLMD units.** The 2026-10-15 f01 COLMD field ranges 8.3e-12–7.1e-5 (kg m⁻²); it is stored
   as µg/m² (× 10⁹) at scale 10. MASSDEN was already kg m⁻³ × 10⁹.
7. **AIFS `tp` is (0,1,52)**, not IFS's (0,1,193); verified on the live 00z f024 index/message. AIFS
   stores the run total (no 3 h/24 h difference).
8. **The budget-outlook rule** projects `usedToday × 24 / hoursElapsedInTheUtcDay` against
   `1.2 × mbPerDay` and needs at least one hour of the UTC day; it reports `degraded`.
9. **No new dependencies.**

## Issues found / open questions

- **Place-weather payload changed again:** `marine`, `aurora`, `heat`, `uv` are always present
  (unavailable when not computed), and `smoke`/`lightning` gain metrics; the 052/053/105 place-weather
  tests likely need updates at the checkpoints.
- **New sources and a new MCP tool** may affect exact-set tests (`FamilyDefaultsTest` is updated;
  restricted-read coverage and MCP tool-count tests are not).
- **`FamilyDefaultsTest`** classifies `GfsWaveAdapter` and `AifsAdapter` as `raster`; their
  `mbPerDay` and product counts should be re-measured at 143.
- **Deferred features** above are recorded for 107/108 (they share `Weather-Imagery-Design §5.3`).

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/model/VelocityJsonEncoderTest` | Two components with header fields, row-major data, parameter numbers 2/3, refTime/forecastTime; a wrong-length buffer refused |
| `raster/model/PngUvEncoderTest` | R = u, G = v, B = 0 with the computed bounds; a flat field's 0..1 range; channel scale and clamp |
| `raster/ProductProfileDefaultsTest` | Small vs large defaults; explicit `products` wins; unknown ids ignored; AIFS off on small, on for large; `on()` |
| `raster/sample/SampleTrackStatusTest` | `maxGap` marks an old value stale (and leaves a fresh one); non-ok values untouched; worst-status ranking |
| `retention/FamilyDefaultsTest` (updated) | `GfsWaveAdapter` and `AifsAdapter` classified `raster` with §2.4 retention |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid. `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 835
requests, all operations covered. No new dependencies.

## Notes for the next prompt

- **107 (SatDump extras)** and **108 (map interop)** are independent of the deferred 106 sources; 108
  reuses `PngUvEncoder` for tiled `.png` fields (the manifest already reserves `encodings.i16`,
  `pngTiles`, `tilejson`).
- **The deferred 106 sources** (RTMA-RU, NBM, MRMS extras, NEXRAD L3, OVATION raster, SST, HeatRisk,
  EPA UV, GOES West, C08/C09/C02/mesoscale) remain; each is a `ProductSpec` on a new or existing
  source, and each should use `ProductEnablement`.
- **143** should measure the new `gfs-wave`/`aifs` downloads, the AIFS `GfsWaveAdapter` request counts,
  and the `velocity-json`/`png-uv` encode times.
- **144 (Flyway):** no new tables or columns.

Implementation commit: `c6960386e4327a7aafc827bb267932ffe57b75e2`
