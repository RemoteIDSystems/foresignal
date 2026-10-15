# 105-CR-Weather-Models-And-Storm-Tracking

| | |
|---|---|
| Prompt | 105-Weather-Models-And-Storm-Tracking.md |
| Status | Complete |
| Implementation commit | `a46d492ec7ab0edf94bb2bb18b4d97a3e061e429` |
| Date | 2026-10-15 |

## Summary

Added the v1.1 weather layer. ECMWF IFS is a third forecast model; the places AOI now has `forecast-area`
threshold polygons; MRMS storm cells get TITAN-lite tracking with merges, splits, nowcast and a GLM
lightning-jump flag; `wx-places` and `/v1/sample` carry a model-disagreement flag; and weather-impact
correlation recipes plus an `EventAnnotator` SPI turn weather co-occurrences into intelligence events.
Compiles; tests written, not run.

- **`ecmwf-ifs`** (`source.adapters.ecmwf`): a POLLING source on the 051 model framework, but with a
  dedicated engine. It reads the `ecmwf-forecasts` S3 mirror (or `data.ecmwf.int/forecasts` by editing
  `pathTemplate`), plans ranges from each step's JSON `.index` (`EcmwfIndex` + `EcmwfRunPlanner`, which
  use the explicit `_offset`/`_length` so no object size is needed), and decodes with `Grib2Decoder`
  (`GRIB2_CCSDS`). Products: `ifs` (field), `ifs-gust`, `ifs-t2m`, `ifs-mslp`, `ifs-precip-3h`,
  `ifs-precip-24h` (i16, 1440 × 721); every derived output carries the CC BY 4.0 "changes made"
  attribution. 00z/12z, steps 0–144 by 3, budget 20/min · 3,000/day · 1,200 MB/day.
  **Verified 2026-10-15** against the live mirror and indices (see the Source-Catalog row).
- **`wx-forecast-areas`** (`forecast-area`): thresholds a model field over the places AOI on each
  completed run, unions the per-step masks (3×3 opening, `MaskPolygons`, 0.02°), sets
  `validFrom`/`validUntil`, and emits on change (payload or 10% geometry symmetric difference) with a
  tombstone when the exceedance disappears.
- **`wx-storm-tracks`** (`storm-track`, `storm-swath`): `Hungarian` (O(n³), greedy above 300) +
  `TitanLiteTracker` (advected-polygon IoU, merges, splits, `titan-lite-v1`) + `LightningJump`
  (`2sigma-glm-v1`, 2-min bins). Track records carry the cell summary, a 12-position history, motion,
  trend, the jump flag, a GeometryCollection of the +15/+30/+60 nowcast polygons and a detector
  version; swaths are the union along the track.
- **Model disagreement**: `ModelDisagreement` (pure) with gust 8 m/s, precip `max(15 mm, mean/2)` and
  t2m 4 K thresholds and 75% hysteresis; wired into `wx-places` (`forecast.disagreement`) and
  `/v1/sample` (`disagreement`).
- **Annotators**: recipes `dry-lightning-ignition`, `airport-weather`, `smoke-aq` (type
  `weather-impact`), and the `EventAnnotator` SPI with `WeatherEventAnnotator` (`outage-weather`)
  adding `context.weather` to `infrastructure-disruption`/`weather-impact` events as context members.
- **API/MCP/spec/docs**: `weather_forecast` gains `ifs` and `disagreement`; new MCP
  `list_storm_tracks`; OpenAPI kinds, provider enum, changelog; Postman regenerated; Source-Catalog
  row; Operator-Guide section; Architecture appendix; THIRD_PARTY_NOTICES.

## Files created / modified

**Created (main):**
- `raster/model/`: `EcmwfIndex`, `EcmwfRunPlanner`, `EcmwfDefinition`, `EcmwfRunIngest`
- `source/adapters/ecmwf/`: `EcmwfAdapter`
- `source/adapters/wx/`: `Hungarian`, `TitanLiteTracker`, `LightningJump`, `WxForecastAreasGenerator`,
  `WxStormTracksGenerator`
- `raster/sample/ModelDisagreement`
- `intel/EventAnnotator`, `intel/WeatherEventAnnotator`
- `mcp/tools/ListStormTracksTool`
- `resources/raster/styles/pressure@1.json`
- `resources/intel/recipes/{dry-lightning-ignition,airport-weather,smoke-aq}.json`

**Modified (main):** `raster/model/ModelCycleSelector` (`{step}` token); `raster/sample/SampleService`
(`disagreement`), `raster/sample/ForecastSeriesService` (`ifs` provider, `sourceOf`);
`source/adapters/wx/WxPlacesGenerator` (IFS section + disagreement), `PlaceWeatherState`
(`ifs`, `disagreement`); `intel/CorrelationEngine` (annotator hook), `IntelEventMapper`,
`IntelligenceEventEntity` (`context`), `RecipeRegistry`; `mcp/tools/WeatherForecastTool`.

**Docs/spec:** `docs/OpenAPI.json`, `postman/` (regenerated, unchanged), `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`, `docs/Architecture.md`, `THIRD_PARTY_NOTICES.md`.

**Tests:** `raster/model/EcmwfIndexTest`, `EcmwfRunPlannerTest`; `source/adapters/wx/HungarianTest`,
`TitanLiteTrackerTest`, `LightningJumpTest`; `raster/sample/ModelDisagreementTest`; and
`retention/FamilyDefaultsTest` updated (new adapters/kinds).

## Deviations from the prompt

1. **A dedicated `EcmwfRunIngest`, not an extension of `ModelRunIngest`.** The NCEP engine hardcodes the
   `.idx` path/format, a zero-padded step token and one message range per step. ECMWF uses `.index`
   (JSON lines), an unpadded step and difference products. The engine mirrors `ModelRunIngest`'s
   structure (state keys, candidate cycles, completeness, probe) and shares `ModelCycleSelector`, which
   gained an unpadded `{step}` token. `GribIdx`/`IdxRangePlanner` and their tests are untouched.
2. **`ifs-precip-3h`/`ifs-precip-24h` are differences of run totals.** ECMWF's `tp` is accumulated
   from the run start (verified: 6 h of accumulation at f006), so each precipitation product reads its
   step's `tp` and the reference step's (`step−3`/`step−24`) `tp` and subtracts. The reference message
   is a second `.index` GET plus a ranged GET per product per step (about 180 extra requests per run);
   this fits the 3,000/day budget.
3. **The prompt's gust fact is off by step.** It says `10fg3` for steps 3–144; the live 2026-10-15 00z
   run lists `10fg` at f012 and `10fg3` at f144. `EcmwfIndex.normalizeParam` folds `10fg3`/`10fg6`
   onto `10fg`, so the gust component matches wherever.
4. **Automatic host failover is not implemented.** The S3 mirror is used by default; the fallback is a
   `pathTemplate` edit (`https://data.ecmwf.int/forecasts/{...}`). A real mid-run failover would need a
   client-level host switch.
5. **`pressure@1` style added** for `ifs-mslp` (the style list had none for pressure). Scale 1.0,
   offset 100000 Pa.
6. **`forecast-area` thresholds use the product's units.** t2m is stored in K, so the defaults are
   313.15 K (40 °C) and 263.15 K (−10 °C). The 10% geometry change is checked explicitly against the
   previous geometry (the ledger also skips identical payload+geometry).
7. **The 052 `radar-cell` upgrade is not done.** `radar-cell` records do not yet carry `trackId` or
   `motion.method: titan-lite-v1`, and `place-weather`'s `cell.eta50Min` does not prefer track motion.
   Tooltip: this needs `wx-cells` to consume storm-track state. Recorded as an open item.
8. **The 101 engine's limits constrain the recipes.** It honours seed kinds, the cluster distance/time
   from the *first* join, join kinds/role/family/weight, `healthOf` contradictions, thresholds and
   dedup; it does not enforce per-join distance/conditions, the `emerging` status, or the
   `smoke-aq`/`dry-lightning-ignition` special conditions. The recipes are authored within those
   semantics; `smoke-aq`'s contradiction uses an empty `healthOf` (alternatives fire only for an
   unhealthy `healthOf` source) so its "local source" alternative is authored but rarely attached.
9. **The annotator runs after the recipe event is persisted** (same correlation thread), adding context
   members with role `context` and weight 0, and merges `context.weather`. A new `context` jsonb column
   on `intelligence_event` (Hibernate `ddl-auto=update`).
10. **`list_storm_tracks` builds its own compact items** (RecordItems' compact shape drops the nested
    motion/nowcast), reading current heads via `RecordQueryService`.
11. **No new dependencies.**

## Issues found / open questions

- **Place-weather payload changed.** Adding `forecast.ifs` (always present) and `forecast.disagreement`
  changes every place-weather payload; the existing 052/053 place-weather tests (exact-map assertions)
  likely need updating at the 057/124 checkpoints.
- **Provider/tool sets changed.** `ForecastSeriesService.PROVIDERS` now includes `ifs` and a new MCP
  tool exists; tests asserting exact provider or tool sets (e.g. `WeatherMcpToolsIT`, restricted-read
  coverage) may need updating.
- **TITAN-lite benchmark not written.** The prompt's ≤ 50 ms per 100-cell micro-benchmark is not
  implemented as a test.
- **Performance unmeasured:** the `WxStormTracksGenerator` on the real 7000 × 3500 grid, the
  `wx-places` disagreement scans (several `windowMax` decodes per model change), and the IFS
  precipitation reference reads.
- **ECMWF run lag** is set to 7 h by default (the 2026-10-15 00z run appeared about 7.5 h after its
  reference time); adjust per host.
- The pip `EcmwfIndex` parser skips a line with a missing `_offset`/`_length`; a format change would
  surface as a missing field (a `SilentFormatChangeException` at decode) rather than an empty index.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/model/EcmwfIndexTest` | Valid JSON lines parsed, malformed skipped; the empty body refused; find by param/levtype/levelist; `10fg3`/`10fg6` folded onto `10fg`; `label()`; `end` = offset + length − 1 |
| `raster/model/EcmwfRunPlannerTest` | Spans from `_offset`/`_length` without an object size; adjacent coalescing; non-adjacent separation; the 8 MB message cap; duplicates read once |
| `source/adapters/wx/HungarianTest` | Known optimum; rectangular; transpose when rows > columns; forbidden pairs unmatched; greedy; empty matrices |
| `source/adapters/wx/TitanLiteTrackerTest` | Steady overlap keeps the pairing; a cell overlapping two tracks merges; a track overlapping two cells splits; too-far leaves both unmatched; advection north/east; IoU; destination |
| `source/adapters/wx/LightningJumpTest` | A sharp rise above 2σ at the rate floor is a jump; below the floor is not; empty; a steady high rate is not |
| `raster/sample/ModelDisagreementTest` | Gust spread flagged with models; below threshold null; hysteresis at 75%; two-model minimum; precip `max(15, mean/2)`; t2m 4 K |
| `retention/FamilyDefaultsTest` (updated) | The three new sources classified (`ecmwf-ifs` raster; `wx-forecast-areas`, `wx-storm-tracks` weather) and their kinds' §2.4 retention |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid.

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 835 requests, all operations covered.

No new dependencies.

## Notes for the next prompt

- **106** builds the extended products, HRRR smoke fields, HeatRisk, UV and alternate field formats; the
  `getSample` `record=`/`SamplePosition` slot is still open.
- **The IFS model** is a new `@Component` adapter; adding another model to `EcmwfRunIngest` is a
  `EcmwfDefinition` plus a decode request.
- **Open items carried from 105:** the `radar-cell` `trackId`/titan-lite upgrade and the place-weather
  `cell.eta50Min` track preference; the TITAN-lite benchmark; per-join recipe conditions; the
  `smoke-aq`/`dry-lightning-ignition` emergent status.
- **143** should measure the IFS download (~0.5–1 GB/day), the storm-track association on the real
  grid, and the wx-places disagreement scans.
- **144 (Flyway):** new table/columns are none except `intelligence_event.context` (jsonb). No new
  operations, so `pending-operations.txt` is unchanged.

Implementation commit: `a46d492ec7ab0edf94bb2bb18b4d97a3e061e429`
