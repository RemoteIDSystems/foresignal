# 105-CR-Weather-Models-And-Storm-Tracking

| | |
|---|---|
| Prompt | 105-Weather-Models-And-Storm-Tracking.md |
| Status | Partial: implemented and post-review fixed; open items are listed under "Issues found / open questions" |
| Implementation commit | `a46d492ec7ab0edf94bb2bb18b4d97a3e061e429` |
| Date | 2026-10-15 (post-review fixes 2026-10-16) |

## Summary

Added the v1.1 weather layer. ECMWF IFS is a third forecast model; the places AOI has `forecast-area`
threshold polygons; MRMS storm cells get TITAN-lite tracking with merges, splits, nowcast and a GLM
lightning-jump flag; `wx-places` and `/v1/sample` carry a model-disagreement flag; and weather-impact
correlation recipes plus an `EventAnnotator` SPI turn weather co-occurrences into intelligence events.
Compiles; tests written, not run. A code review on 2026-10-16 found defects in the first implementation;
they are fixed in the commit `105: fix post-review defects` (see "Post-review fixes" below). This report
describes the code as it stands after that commit.

- **`ecmwf-ifs`** (`source.adapters.ecmwf`): a POLLING source on the 051 model framework with a dedicated
  engine (`EcmwfRunIngest`). It reads the `ecmwf-forecasts` S3 mirror (or `data.ecmwf.int/forecasts` by
  editing `pathTemplate`), plans ranges from each step's JSON `.index` (`EcmwfIndex` + `EcmwfRunPlanner`,
  using the explicit `_offset`/`_length`, so no object size is needed), and decodes with `Grib2Decoder`
  (`GRIB2_CCSDS`). Products: `ifs` (field), `ifs-gust`, `ifs-t2m`, `ifs-mslp`, `ifs-precip-3h`,
  `ifs-precip-24h` (i16, 1440 × 721). 00z/12z, steps 0–144 by 3, budget 20/min · 3,000/day · 1,200 MB/day.
  A step is marked fetched only when every product that has it committed it; otherwise it is retried on later
  fetches, at most 3 times. Precipitation differences are clamped at zero. **Verified 2026-10-15** against the
  live mirror and indices (see the Source-Catalog row).
- **ECMWF attribution**: the product attribution ("Contains modified Copernicus/ECMWF data … Changes made:
  resampled, quantised, derived fields.", the text the prompt prescribes) is on every IFS product (tiles,
  fields, samples), and since the post-review fixes also on every `forecast-area` from IFS or AIFS
  (`attribution`) and in the place-weather `forecast.ifs` section. The text lives in
  `EcmwfDefinition.CHANGES_MADE`.
- **`wx-forecast-areas`** (`forecast-area`): thresholds a model field over the places AOI on each completed
  run, unions the per-step masks (3×3 opening, `MaskPolygons`, 0.02°), sets `validFrom`/`validUntil`, and
  writes a version only when the quantized payload changes or the polygon changes by more than 10%
  (symmetric difference over union; otherwise the stored geometry and its area are kept). `run` is volatile.
  When a newer run drops the exceedance the area is tombstoned. The peak of a `<=` threshold is the minimum.
- **Per-model run staleness** (`ModelRunAge`): a model's run is `stale` only after cadence + availability lag +
  ingest allowance (IFS 22 h, AIFS 16 h, GFS 11.5 h, GFS-Wave 12 h, HRRR 7 h 50 min, NBM 9 h), used by
  `wx-places` and `wx-forecast-areas`.
- **`wx-storm-tracks`** (`storm-track`, `storm-swath`): `Hungarian` (O(n³), greedy above 300) +
  `TitanLiteTracker` (advected-polygon IoU, merges in which the largest-area track continues, splits,
  `titan-lite-v1`) + `LightningJump` (`2sigma-glm-v1`, 2-min bins). Track records carry the cell summary, a
  12-position `history` (volatile; a frame that only moves the history writes no version), motion (least
  squares over the newest 5 positions), trend, the jump flag, a GeometryCollection of the +15/+30/+60 nowcast
  polygons and a detector version. Swaths are the union along the track, written every 10 min and when a track
  ends by a merge or by two missed frames; `reconcile` restores the stored swath, history, motion and links.
- **Model disagreement**: `ModelDisagreement` (pure) with gust 8 m/s, precip `max(15 mm, mean/2)` and t2m 4 K
  thresholds and 75% hysteresis; wired into `wx-places` (`forecast.disagreement`) and `/v1/sample`
  (`disagreement`). In `wx-places` like is compared with like over the next 24 h: a model joins only when its
  run reaches now + 24 h (18-hour HRRR runs are left out), gust and t2m are maxima over the same window, and
  precipitation is the next-24-hour total for every model (GFS and AIFS from run totals, IFS from its 24-hour
  product at the step ending near now + 24 h). AIFS (prompt 106) joins t2m and precipitation.
- **Annotators**: recipes `dry-lightning-ignition`, `airport-weather`, `smoke-aq` (type `weather-impact`), and
  the `EventAnnotator` SPI with `WeatherEventAnnotator` (`outage-weather`) adding `context.weather` to
  `infrastructure-disruption`/`weather-impact` events as context members. It reads the stored record versions in
  the event's ±3 h window (`RecordWindowReader`, `JdbcRecordWindowReader` over `record_version`) and keeps only
  NWS warnings, radar ≥ 50 dBZ (`wx-area` reflectivity ≥ 50, `storm-track` cells ≥ 50) and gusts ≥ 25 m/s
  (`forecast-area` gust areas, METAR gusts). `CorrelationEngine.applyAnnotators` replaces an alternative with
  the same hypothesis instead of appending it again.
- **API/MCP/spec/docs**: `weather_forecast` gains `ifs` and `disagreement`; new MCP `list_storm_tracks`;
  OpenAPI kinds, provider enum, changelog; Source-Catalog row; Operator-Guide section; Architecture appendix;
  THIRD_PARTY_NOTICES.

## Files created / modified

**Created (main):**
- `raster/model/`: `EcmwfIndex`, `EcmwfRunPlanner`, `EcmwfDefinition`, `EcmwfRunIngest`
- `source/adapters/ecmwf/`: `EcmwfAdapter`
- `source/adapters/wx/`: `Hungarian`, `TitanLiteTracker`, `LightningJump`, `WxForecastAreasGenerator`,
  `WxStormTracksGenerator`, `ModelRunAge` (post-review)
- `raster/sample/ModelDisagreement`
- `intel/EventAnnotator`, `intel/WeatherEventAnnotator`, `intel/RecordWindowReader` and
  `intel/JdbcRecordWindowReader` (post-review)
- `mcp/tools/ListStormTracksTool`
- `resources/raster/styles/pressure@1.json`
- `resources/intel/recipes/{dry-lightning-ignition,airport-weather,smoke-aq}.json`

**Modified (main):** `raster/model/ModelCycleSelector` (`{step}` token); `raster/sample/SampleService`
(`disagreement`), `raster/sample/ForecastSeriesService` (`ifs` provider, `sourceOf`);
`source/adapters/wx/WxPlacesGenerator` (IFS section + disagreement), `WxInput` (per-model forecast age),
`PlaceWeatherState` (`ifs`, `disagreement`); `intel/CorrelationEngine` (annotator hook), `IntelEventMapper`,
`IntelligenceEventEntity` (`context`), `RecipeRegistry`; `mcp/tools/WeatherForecastTool`.

**Docs/spec:** `docs/OpenAPI.json`, `postman/` (regenerated at implementation, unchanged), `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`, `docs/Architecture.md`, `THIRD_PARTY_NOTICES.md`.

**Tests:** see "Tests written (not run)".

## Deviations from the prompt

1. **A dedicated `EcmwfRunIngest`, not an extension of `ModelRunIngest`.** The NCEP engine hardcodes the `.idx`
   path/format, a zero-padded step token and one message range per step. ECMWF uses `.index` (JSON lines), an
   unpadded step and difference products. The engine mirrors `ModelRunIngest`'s structure (state keys, candidate
   cycles, completeness, probe) and shares `ModelCycleSelector`, which gained an unpadded `{step}` token.
2. **No S3 listing.** The prompt says "via `listS3` and `getRange`". The engine does not list: it GETs each
   step's `.index` directly (a 404/403 means "not published yet") and range-reads from it. A run is complete when
   every planned step's index has been read and its products committed, which is the prompt's completeness rule
   without the extra listing request. The WireMock test therefore stubs indices and ranges, not a listing.
3. **`ifs-precip-3h`/`ifs-precip-24h` are differences of run totals.** ECMWF's `tp` is accumulated from the run
   start (verified: 6 h of accumulation at f006), so each precipitation product reads its step's `tp` and the
   reference step's (`step−3`/`step−24`) `tp` and subtracts, clamped at zero. The reference message is a second
   `.index` GET plus a ranged GET per product per step (about 180 extra requests per run); this fits the
   3,000/day budget.
4. **The prompt's gust fact is off by step.** It says `10fg3` for steps 3–144; the live 2026-10-15 00z run lists
   `10fg` at f012 and `10fg3` at f144. `EcmwfIndex.normalizeParam` folds `10fg3`/`10fg6` onto `10fg`.
5. **Automatic host failover is not implemented.** The S3 mirror is used by default; the fallback is a
   `pathTemplate` edit (`https://data.ecmwf.int/forecasts/{...}`). A mid-run failover would need a client-level
   host switch.
6. **`pressure@1` style added** for `ifs-mslp` (the style list had none for pressure). Scale 1.0, offset
   100000 Pa.
7. **`forecast-area` thresholds use the product's units.** t2m is stored in K, so the defaults are 313.15 K
   (40 °C) and 263.15 K (−10 °C).
8. **The 052 `radar-cell` upgrade is not done.** `radar-cell` records do not carry `trackId` or
   `motion.method: titan-lite-v1`, and `place-weather`'s `cell.eta50Min` does not prefer track motion; this needs
   `wx-cells` to consume storm-track state.
9. **The 101 engine's limits constrain the recipes.** It honors seed kinds, the cluster distance/time from the
   *first* join, join kinds/role/family/weight, `healthOf` contradictions, thresholds and dedup; it does not
   enforce per-join distance/conditions, the `emerging` status, or the `smoke-aq`/`dry-lightning-ignition`
   special conditions. The recipes are authored within those semantics; `smoke-aq`'s contradiction uses an empty
   `healthOf` (alternatives fire only for an unhealthy `healthOf` source), so its "local source" alternative is
   authored but rarely attached.
10. **The annotator runs after the recipe event is persisted** (same correlation thread), adding context members
    with role `context` and weight 0, and merges `context.weather`. A new `context` jsonb column on
    `intelligence_event` (Hibernate `ddl-auto=update`). `context.weather` gained a `gusts` list (post-review);
    the existing `warnings`, `radar` and `tracks` keys are unchanged.
11. **`list_storm_tracks` builds its own compact items** (RecordItems' compact shape drops the nested
    motion/nowcast), reading current heads via `RecordQueryService`.
12. **The model-disagreement window rule.** The prompt says "HRRR (when the horizon ≤ 18 h)". `wx-places`
    compares over the next 24 h only, so a model joins when its run reaches now + 24 h (within 1 h): 18-hour HRRR
    runs are left out and 48-hour HRRR runs join. `/v1/sample` compares the values at one sampled time, so no
    window applies there.
13. **No new dependencies.**

## Issues found / open questions

- **Place-weather payload changed.** `forecast.ifs` (always present, now with `attribution` and the next-24-hour
  precipitation) and `forecast.disagreement` change place-weather payloads; existing 052/053 place-weather tests
  with exact-map assertions may need updating at the 124 checkpoint.
- **Provider/tool sets changed.** `ForecastSeriesService.PROVIDERS` includes `ifs` (and `aifs` from 106) and a new
  MCP tool exists; tests asserting exact provider or tool sets (e.g. `WeatherMcpToolsIT`, restricted-read
  coverage) may need updating.
- **TITAN-lite benchmark not written.** The prompt's ≤ 50 ms per 100-cell micro-benchmark is not a test yet.
- **Recipe ITs not written.** `WeatherAnnotatorsIT`'s dry-lightning → fire, airport-weather severity and
  smoke-aq contradiction cases test conditions the 101 engine cannot express (deviation 9); only the
  `outage-weather` annotator is tested (`WeatherEventAnnotatorTest`, unit level with a fake version reader).
- **Spec/code type mismatch left alone:** `ForecastAreaPayload.threshold.value` is a number in the spec, but the
  payload carries a string (`"25"`, `"263.15"`). It is a persisted wire value, so it was not changed. Which one
  should change?
- **Performance unmeasured:** the `WxStormTracksGenerator` on the real 7000 × 3500 grid, the `wx-places`
  disagreement scans, the IFS precipitation reference reads, and the `record_version` window query of
  `outage-weather` (filtered by source, kind, time and bounding box).
- **ECMWF run lag** is 7 h by default (the 2026-10-15 00z run appeared about 7.5 h after its reference time).
- The `EcmwfIndex` parser skips a line with a missing `_offset`/`_length`; a format change surfaces as a missing
  field (reported as a format change, the step retried and then skipped) rather than an empty index.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `raster/model/EcmwfIndexTest` | Valid JSON lines parsed, malformed skipped; the empty body refused; find by param/levtype/levelist; `10fg3`/`10fg6` folded onto `10fg`; `label()`; `end` = offset + length − 1 |
| `raster/model/EcmwfRunPlannerTest` | Spans from `_offset`/`_length`; adjacent coalescing; non-adjacent separation; the 8 MB cap; duplicates read once |
| `raster/model/EcmwfDecodeContractTest` (post-review) | The engine's band decoding on the 046 self-test IFS CCSDS fixture: 1440 × 721 grid from the header, the product factor, a difference clamped at zero |
| `source/adapters/ecmwf/EcmwfAdapterTest` (post-review, WireMock) | Index → ranges → commits → runs complete; a missing reference leaves the step undone and a later fetch completes it; a missing required field is retried then skipped after 3 attempts and the run never completes; a 503 (with Retry-After) fails the fetch and marks nothing; object URLs and the fallback host |
| `source/adapters/wx/HungarianTest` | Known optimum; rectangular; transpose; forbidden pairs; greedy; empty |
| `source/adapters/wx/TitanLiteTrackerTest` | Steady overlap; merge (the ended track merges into the continuing one); the largest-area track continues even when the solver paired the smaller (post-review); split; too far; advection; IoU; destination |
| `source/adapters/wx/StormTrackGeneratorTest` (post-review; the prompt's `StormTrackIT` as a generator test on the in-memory derived world) | Synthetic MRMS frames: the id is kept, versions only while motion settles (not for history), a 12-position ring, two missed frames end the track with a final swath; a merge sets `mergedInto` on the smaller track and writes its swath; a restart restores swath, history and motion and the next frame extends the stored union |
| `source/adapters/wx/ForecastAreaGeneratorTest` (post-review) | Thresholding and step union, `validFrom`/`validUntil`, peak and attribution; no version for a newer run with the same area; a < 10% change keeps the geometry; tombstone when a run drops the exceedance; the minimum as the `<=` peak; IFS current for 22 h then stale with kept values; nothing written for an area that never exceeded |
| `source/adapters/wx/PlaceDisagreementTest` (post-review) | 18-hour HRRR left out, 48-hour HRRR joins; AIFS joins t2m and precipitation; next-24-hour totals; `forecast.ifs` attribution; `coversWindow` |
| `source/adapters/wx/ModelRunAgeTest` (post-review) | Each model's maximum run age; the 12 h fallback |
| `source/adapters/wx/LightningJumpTest` | A 2σ jump at the rate floor; below the floor; empty; steady high rate |
| `raster/sample/ModelDisagreementTest` | Gust spread with models; below threshold; hysteresis; two-model minimum; precip `max(15, mean/2)`; t2m 4 K; NaN values absent (post-review); AIFS as a model (post-review) |
| `intel/WeatherEventAnnotatorTest` (post-review) | Only warnings, ≥ 50 dBZ and ≥ 25 m/s gusts qualify; versions inside the window count although the head is newer; context members with weight 0; the current-heads fallback; the window rules |
| `retention/FamilyDefaultsTest` (updated at implementation) | The three new sources classified and their kinds' retention |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL` (implementation and post-review fixes).

`./gradlew lintOpenApi` → valid at implementation. No new dependencies.

## Notes for the next prompt

- **The IFS model** is a `@Component` adapter; adding another model to `EcmwfRunIngest` is an `EcmwfDefinition`
  plus a decode request. The changes-made attribution text is `EcmwfDefinition.CHANGES_MADE`
  (`CHANGES_MADE_AIFS` for AIFS).
- **Derived forecast sections** should use `WxInput.forecast(ctx, productId, now)` so staleness follows
  `ModelRunAge`.
- **Open items carried from 105:** the `radar-cell` `trackId`/titan-lite upgrade and the place-weather
  `cell.eta50Min` track preference; the TITAN-lite benchmark; per-join recipe conditions; the
  `smoke-aq`/`dry-lightning-ignition` emergent status; the recipe-level `WeatherAnnotatorsIT` cases.
- **143** should measure the IFS download (~0.5–1 GB/day), the storm-track association on the real grid, the
  wx-places disagreement scans and the `outage-weather` window query.
- **144 (Flyway):** no new tables or columns from the post-review fixes; `intelligence_event.context` (jsonb) is
  the only schema addition of 105. No new operations, so `pending-operations.txt` is unchanged.

## Post-review fixes (2026-10-16)

Commit `105: fix post-review defects`. Each review finding was re-verified against the code first.

| # | Finding | Result |
|---|---|---|
| 1 | HIGH: a fixed 12 h run age made IFS stale about 75% of the day | **Fixed.** `ModelRunAge`: cadence + lag + ingest allowance per model; `WxInput.forecast(ctx, product, now)` in `wx-places`; `wx-forecast-areas` uses it too. |
| 2 | MEDIUM: a step was marked done after a missing reference, a failed commit or a missing field, and never retried | **Fixed.** Done only when every product that has the step committed it; retried up to 3 times (`tries:{cycle}`), then skipped with a warning so one permanently missing field cannot starve the run. |
| 3 | MEDIUM: forecast-area wrote a `no-coverage` record with null geometry instead of a tombstone | **Fixed.** Tombstone; a later exceedance starts a fresh record. Areas that never had a record write nothing while their input is unavailable. |
| 4 | MEDIUM: the 10% geometry rule had no effect and `run` made every run a version | **Fixed.** The stored geometry (and its area) is kept unless the change is over 10%; `run` is volatile (descriptor `volatileFields`) and left out of the emit decision. |
| 5 | MEDIUM: the peak of a `<=` threshold was the maximum | **Fixed.** `peakOf` uses the threshold's direction. |
| 6 | MEDIUM: history capped at 5; history timestamps versioned every frame; no final swath on missed frames; reconcile lost the swath | **Fixed.** History ring 12 (motion over the newest 5); `history` volatile and left out of the emit decision; final swath on a missed-frame end; reconcile restores swath, history, motion, trend, MESH, jump and links. Also: a merge now keeps the largest-area track, as the prompt says, even when the solver paired the cell with a smaller one. |
| 7 | MEDIUM: disagreement compared an 18 h HRRR max with 24 h maxima and IFS trailing-24 h maxima with GFS next-24 h totals | **Fixed.** Models must reach now + 24 h; IFS precipitation is the 24-hour total ending near now + 24 h (also in `forecast.ifs`). |
| 8 | MEDIUM: `applyAnnotators` appended alternatives on every recipe hit | **Fixed defensively.** Re-verified: `persist` resets the alternatives to the recipe's before the annotators run, so the duplicate did not occur as described; it would as soon as a recipe defines the same hypothesis or `persist` merges alternatives. `applyAnnotators` now replaces an alternative with the same hypothesis. Only the `applyAnnotators` hunk of `CorrelationEngine` was touched. |
| 9 | MEDIUM: "changes made" attribution missing on forecast-area and `forecast.ifs` | **Fixed.** `attribution` in the payload of IFS/AIFS forecast areas and in `forecast.ifs`; the descriptor attribution names it. |
| 10 | LOW: precipitation differences could go negative | **Fixed.** Clamped at zero. |
| — | outage-weather read current heads only and lacked the ≥ 50 dBZ / gust ≥ 25 m/s filter | **Fixed.** Stored versions in the window via `RecordWindowReader`; warnings only, ≥ 50 dBZ, gusts ≥ 25 m/s from `forecast-area` and METAR. |
| — | Missing tests | `EcmwfAdapterTest`, `ForecastAreaGeneratorTest`, `StormTrackGeneratorTest` (for `StormTrackIT`), `EcmwfDecodeContractTest` and the Titan `mergedInto` assertions written; `WeatherAnnotatorsIT` written only for `outage-weather`, as a unit test (see open questions). |

Corrections to the first version of this report: it said the track history held 12 positions (it held 5), that
`forecast-area` emitted on a 10% geometry change (the rule had no effect), that every derived output carried the
changes-made attribution (`forecast-area` and `forecast.ifs` did not), and it did not record that the engine
never lists the bucket (deviation 2). Its test table omitted the missing WireMock, forecast-area, storm-track and
decode-contract tests.

Implementation commit: `a46d492ec7ab0edf94bb2bb18b4d97a3e061e429`
