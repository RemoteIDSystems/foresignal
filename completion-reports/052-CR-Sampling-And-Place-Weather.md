# 052-CR-Sampling-And-Place-Weather

| | |
|---|---|
| Prompt | 052-Sampling-And-Place-Weather.md |
| Status | Complete |
| Implementation commit | 7e4b8742ed322a58ad49b156ea6f0fc0383daf29 |
| Date | 2026-10-12 |

## Summary

Weather can now be read by programs and alerted on without false all-clears:
- a generic DERIVED runtime (`ai.foresignal.derive`), which 099 reuses;
- point, batch, series and area sampling with explicit status semantics (`ai.foresignal.raster.sample`);
- three derived sources, `wx-places`, `wx-areas` and `wx-cells`, whose records feed the existing trigger
  engine, SSE and MCP;
- four MCP tools, a resource and a resource template.

Missing or stale data never reads as clear. The five 052 operations are `implemented` and off
`pending-operations.txt`.

**1. DERIVED runtime** (`ai.foresignal.derive`, no weather type in it)
- `DerivedGenerator` (a `SourceAdapter` with `Mode.DERIVED`):
  - `inputs(ValidatedParams)` returns `DerivedInputs`: change subscription (`sources`, `kinds`, ANDed; a
    source never receives its own changes), raster `products`, an optional `tick`, and `places`
    (reconcile on place edits);
  - hooks `onChanges`, `onFrame(RasterFrameEvent)`, `onTick` (all optional) and `reconcile(ownHeads)`.
- `ctx.http()` throws for DERIVED sources (it already did, `DefaultFetchContext`); the registry already
  refused a non-null `recordTtl` (`DescriptorValidator`). Both are now covered by `DerivedRunnerIT`.
- `DerivedContext`: `emit`, `tombstone`, `state()` (the source's `AdapterStateStore`), `rasters()` (the
  read-only `RasterReader`), `records()` (`JdbcDerivedRecords`, `record_head ⋈ record_live` only),
  `places()` (`ServiceDerivedPlaces`: place snapshots and the places AOI), `sourceStatus(id)`, `clock()`.
- `EmitLedger` (per source):
  - skips an emit whose canonical payload and geometry equal the entity's previous one;
  - raises `observedAt` to the previous emit's when it would go backwards (a staleness change seen on a
    tick must not make the next frame LATE, ING-4);
  - drops redundant tombstones;
  - is seeded from the heads before every reconcile.
- `DerivedRunner`:
  - one virtual thread per enabled derived source, started on `ApplicationReadyEvent`
    (`StartupOrder.DERIVED = 29`, after the product catalog, before the scheduler), restarted on settings and
    registry changes;
  - bounded inbox: change batches up to `foresignal.derive.inbox-capacity` (64), overflow drops them and
    schedules a reconcile; frame events coalesced per product; one pending tick; reconcile at start, on
    enable, after a change-bus gap, an overflow, or a place edit;
  - each invocation is treated like a fetch: `recordAttempt`, one `fetch_log` row with `principal=derived`
    (written by the writer when records were emitted, by the runner otherwise), one `IngestBatch`, then
    `recordSuccess(IngestOutcome)` or a FAILED row plus `recordFailure`;
  - implements `IngestStateReset` (reconciles after the test database cleaner).
- Frame events: `RasterFrameCommittedEvent` (042) plus a new `RasterRunCompletedEvent`, published by
  `RasterPipeline.completeRun`.
- `DerivedGraph` (a `SmartInitializingSingleton`): builds "A's records feed B's subscription" from every
  generator's default inputs; a cycle (a self-subscription included) fails startup naming the cycle.

**2. Sampling** (`ai.foresignal.raster.sample`)
- `SampleService` (also the `RasterReader` of the generators):
  - time `latest|now|<iso>|-PT1H|+PT6H` (`TimeSpec`); past times choose observation frames; future times
    and forecast products the newest complete run (or `run`), the step at or before the time;
  - status order: `unavailable` (product disabled, source **failing**, no frame, frame expired or its file
    gone) → `no-coverage` (off the grid or a `noCoverage` code, MRMS 0, with the "not evidence of clear
    weather" text) → `no-data` → `stale` (older than `maxGap` at the requested time) → `ok`;
  - non-ok values carry `statusText` and `value: null`;
  - `nearest` or `bilinear` from the product spec; u/v (`kind=field`, two bands) adds `derived{speedMs,
    fromDeg, compass}` with 051's `VectorTileService.fromDeg`;
  - every value has `value`, `units`, `label`, `frameKey`, `validTime`, `localTime` (place zone, else UTC),
    `ageMinutes`, `run`, `step`, `method`, `coverage`, `status`, `statusText` and the attribution;
  - imperial units through 045's `UnitConversion`;
  - series: observation frames in `from`..`to` (default the last 6 h) or one run's steps; more than 500 is
    400.
- `FrameView`: decoded frame plus projection, code classification and point sampling.
- `AreaStatsService` + `AreaScan`: cells whose center lies in a geometry, place or bbox; min, max, mean, p95
  (nearest rank), `coveragePct`, area above each threshold (thresholds in the requested units); geometry at
  most 10,000 vertices and 2,000,000 km² (400).
- `CellAreas`: spherical cell areas per grid type (exact on latlon, `dx·dy/k²` on lcc, corner polygons
  otherwise) and spherical polygon areas.
- `PlaceGridIndex`: per place and grid, the cells within 100 km sorted by distance (cached); one scan gives
  every threshold's nearest distance and the in-radius maximum, stopping past the radius once all are found.
- `ForecastSeriesService`: cached GFS (to 120 h) and HRRR point series per run (wind speed and direction,
  gusts, °C, per-step precipitation from the run total, smoke).
- Endpoints:
  - `api.SampleController`: `getSample`, `postSample` (at most 100 points), `getProductSeries`,
    `postProductStats`;
  - `api.PlaceWeatherController`: `getPlaceWeather` (the head under the caller's restrictions, plus the
    forecast series).
- A `place` needs a token (401) on every one of them. Sampling never fetches (added to
  `NoClientTriggeredFetchIT`).

**3. Places hook.** Checked: `place` already has `timezone`, `radius_km` (default 20 in `PlaceService`),
`kind`, `tags` and `products`. Place saves re-materialize `near` fences in the save's own transaction
(036 `RuleValidator.rematerialize`). Nothing was missing.

**4. `wx-places`** (`source.adapters.wx.WxPlacesGenerator`, kind `place-weather`, externalId `place:{id}`)
- Inputs:
  - frames of `mrms-reflectivity`, `mrms-mesh-60m`, `mrms-precip-rate`, `hrrr-smoke`, `gfs`, `gfs-gust`,
    `gfs-t2m`, `gfs-apcp`;
  - `radar-cell` changes of `wx-cells`;
  - place edits;
  - a 60 s tick (staleness and the GLM ring, read through `FlashSink`).
- Payload exactly as design §7.2: `radar`, `hail`, `precip`, `lightning`, `cell`, `smoke`, `forecast.gfs`,
  `place`, `radiusKm`, `automated: true`, `notAWarning: true`.
- Each section is a `WxSection`:
  - not ok keeps the previous metrics, sets the status and its fixed `statusText`, and `asOf` (last good
    data, fixed while not ok);
  - before any good data, status only (metric keys omitted, never null);
  - MRMS stale after 6 min, GLM after 2 min (from `newestFlashFileTime()`), model runs after 12 h;
  - `unavailable` for failing inputs, no-coverage when fewer than half the radius cells hold a value.
- Values:
  - 999 = none within 100 km or no approaching core;
  - distances to 0.5 km below 10 km, else 1 km; ETAs up to 5 min; rates and whole units to 1; dBZ to 0.5;
  - `nearest50Km10m` is a rolling 10-minute minimum, rebuilt in `reconcile` from the last 10 minutes of
    frames; `nearestKm30m` is the ring's 30-minute minimum;
  - verified-clear helpers `radar.verifiedNearest50Km10m` and `lightning.verifiedNearestKm30m`;
  - lightning `nearestKm15m`, `nearestKm30m`, `count15km15m`; `cell.eta50Min` from `CellEta`;
  - `smoke.hrrrPm25Max24h`; `forecast.gfs.gustMax24h` and `precip24hMm`.
- Frame ids, ages and timestamps are never in the payload.
- The record has **no geometry** (see Deviation 2).
- A deleted place is tombstoned (on the tick and on the place-edit reconcile); `reconcile` tombstones heads
  of deleted places; never a tombstone for staleness.
- Descriptor: `kindRetention{place-weather: 14d}`, `significantFields` empty, `titleField` `place`, licence
  "derived (see inputs)", `zeroResultsNormal`.

**5. `wx-areas`** (`WxAreasGenerator`, kind `wx-area`, externalId `{field}:{threshold}:{aoi}`)
- Params:
  - `areas` (STRING_LIST of `field:threshold:aoi`, default `refl:50:aoi`, `mesh:25:aoi`);
  - `interval` (6–10 min, default 8; `checkParams` refuses others).
- Payload: `field`, `threshold`, `aoi`, `status`, `asOf`, `areaKm2` (2 significant figures), `maxValue`,
  `coveragePct` (integer).
- Geometry: mask polygons by run-rectangle union (`MaskPolygons`), simplified and snapped to 0.01°, or null.
- Same keep-previous rules, geometry included. A deleted place's area, or one that left `areas`, is
  tombstoned.
- `kindRetention` 7 d.

**6. `wx-cells`** (`WxCellsGenerator` + `RadarCellTracker`, kind `radar-cell`, externalId `c-{ulid}`)
- Scope: the places AOI; `scope=conus` is honored on the large profile only.
- Per reflectivity frame no older than 6 min: 40 dBZ mask, 3×3 opening, 8-connected union-find, split of a
  component at ≥ 2 separate 50 dBZ cores (multi-source BFS), centroid in the AOI.
- `centroid-v1` matching: nearest pairs first, one-to-one, 30 km per 10 min of gap (2–30 min).
- Motion: least squares over the last 4 positions (3 matches) in a local km plane.
- Unmatched cells get new ids; vanished cells are tombstoned.
- Properties: `maxDbz` (0.5), `areaKm2` (2 significant figures), `meshMax` (current same-grid MESH frame,
  omitted otherwise), `glmFlashes5m` (bucket), `motion{speedKmh (5), bearingDeg (10°), method}`.
- Geometry snapped to 0.005°. `reconcile` restores tracks from heads and tombstones nothing.
- `kindRetention` 3 d.
- `wx-places` reads the cells for `cell.eta50Min`, `etaCellId`, `etaSpeedKmh` and `etaBearingDeg`.

**7. Trigger hooks and recipes.** Checked: 017's `cooldownScope` (record/rule, `RuleSqlBuilder.cooldownSeed`,
`AlertPass`) and 036's `near` materialized into `geofence` exist; conditions are AND-only, hence the
verified-clear helpers. The §7.4 recipes are in `docs/Operator-Guide.md` "Weather alerts":
- derived rules use `enter`/`exit`;
- "Echoes cleared" and the lightning all-clear use the helpers;
- "weather feed down" is the existing source-health event, not a rule;
- deleting a place tombstones its record and can fire `exit` rules.

**8. MCP** (025 registry)
- `list_products` (id, title, kind, units, coverage, latest, health).
- `sample_weather` (`@RestrictedRead`): values plus NWS alerts covering the point (`automated: false`) and
  ProbSevere objects within 50 km (`automated: true`).
- `weather_forecast`: 3-hourly to 120 h from GFS/HRRR; compact keeps 10 steps per provider.
- `area_weather_stats`: wraps `/stats` for a place or bbox.
- Every value carries `validTime`, `localTime`, `ageMinutes`, `run`, `coverage` and `status`; compact
  ≤ 4 KB, `detail=long` ≤ 50 KB. A place needs a token. `create_place` and `delete_place` are 036's (agent).
- Resources: `foresignal://products` (new) and `foresignal://places` (036). The template
  `foresignal://products/{id}/legend` is new: `ForesignalMcpResourceTemplate`, served by `McpProtocol` in
  `resources/templates/list` and `resources/read`.

**9. Spec, Postman, docs**
- OpenAPI: five operations `implemented` with fuller descriptions; `Record.x-foresignal-kinds` gains
  `place-weather`, `wx-area` and `radar-cell`; changelog entry.
- `AccessPolicy`: `GET /v1/sample`, `POST /v1/sample` and `POST /v1/products/*/stats` are `read`; the series
  and place weather were already covered.
- Postman `BODIES` for `postSample` and `postProductStats`; regenerated.
- Docs:
  - `docs/Adapter-Guide.md` §14 "Derived sources" (SPI, runner, emit-on-change, tombstone and reconcile
    rules, the cycle guard);
  - `docs/Source-Catalog.md` "Derived weather sources (prompt 052)";
  - `docs/Operator-Guide.md` "Weather alerts";
  - `docs/Agent-Guide.md`: tool rows, resources, the place-metrics pointer;
  - `docs/Architecture.md` §3 (dependency rule for the derived generators) and §28 (sampling, derived
    weather).

## Files created / modified

**Created, main:**
- `derive/`: `package-info`, `DerivedGenerator`, `DerivedInputs`, `RasterFrameEvent`, `HeadSnapshot`,
  `DerivedContext`, `DefaultDerivedContext`, `DerivedRecords`, `JdbcDerivedRecords`, `DerivedPlaces`,
  `ServiceDerivedPlaces`, `EmitLedger`, `DerivedGraph`, `DerivedRunner`, `DerivedProperties`
- `raster/RasterRunCompletedEvent`
- `raster/sample/`: `package-info`, `SampleService`, `SampleValue`, `SampleStatus`, `TimeSpec`,
  `FrameChoice`, `FrameView`, `RasterReader`, `GridWindow`, `CellAreas`, `AreaScan`, `AreaStatsService`,
  `PlaceGridIndex`, `ForecastSeriesService`
- `source/adapters/wx/`: `package-info`, `WxPlacesGenerator`, `PlaceWeatherState`, `WxSection`, `WxRound`,
  `WxInput`, `CellEta`, `WxAreasGenerator`, `WxCellsGenerator`, `RadarCellTracker`, `MaskPolygons`, `CellIds`
- `api/SampleController`, `api/PlaceWeatherController`
- `mcp/ForesignalMcpResourceTemplate`
- `mcp/tools/`: `ListProductsTool`, `SampleWeatherTool`, `WeatherForecastTool`, `AreaWeatherStatsTool`,
  `WeatherToolSupport`, `ProductsResource`, `ProductLegendResource`

**Modified, main:**
- `raster/RasterPipeline` (publishes `RasterRunCompletedEvent`)
- `config/StartupOrder` (`DERIVED`)
- `collect/HealthTracker` (`errorMessage` and `truncate` made public, for the runner)
- `mcp/McpProtocol` (resource templates), `mcp/McpServerConfiguration`
- `security/AccessPolicy`

**Spec, docs, build:** `docs/OpenAPI.json`, `postman/Foresignal.postman_collection.json`,
`scripts/generate-postman.py`, `docs/Adapter-Guide.md`, `docs/Agent-Guide.md`, `docs/Architecture.md`,
`docs/Operator-Guide.md`, `docs/Source-Catalog.md`, `src/test/resources/openapi/pending-operations.txt`
(−5 lines).

**Tests and test support:** see the table below.
- New support: `testsupport/raster/SampleFixtures`, `testsupport/raster/TestDerivedGenerator` (joined to
  `RasterTestSources`), `testsupport/weather/FakeDerived`, `testsupport/weather/PlaceWeatherSupport`.
- `RasterApiSupport` gained `post(...)`.

## Deviations from the prompt

1. **Where the weather generators live.** The prompt puts the SPI in `ai.foresignal.derive` and requires that
   package to hold no weather type. So the generators are in `source.adapters.wx`. They read rasters only
   through `DerivedContext.rasters()` (`raster.sample.RasterReader`), which Architecture §3 now names.
2. **`place-weather` has no geometry.** A place's position is sensitive (036: places are always
   authenticated and read tokens never see coordinates), and a record geometry would expose it through
   `/v1/records` under `openReads`. Rules target it by `source`, `kind` and payload conditions.
3. **Distances from the representative point.** Distances (and the radius) are measured from the place's
   representative point: the point, or the interior point of a polygon or route, not from the place's area
   outline.
4. **Coverage test.** A radar or hail section is `no-coverage` when fewer than half of the cells within the
   radius hold a value (the point's own cell counts when the radius is smaller than a cell). Precipitation is
   judged at the point; its `no-data` maps to `no-coverage`, since section statuses have no `no-data`.
5. **Section `statusText`.** Every non-ok section also carries a fixed `statusText` (the `PlaceWeather` schema
   has the field). `wx-area` payloads leave it out (`WxAreaPayload` has none).
6. **Sample values when not ok.** Following the spec ("null for … non-ok statuses"), a `stale` sample
   returns its frame key and times but `value: null`. Likewise non-ok `/stats` carry no statistics and an
   empty `thresholds`.
7. **Source disabled is not `unavailable`.** The prompt lists "source failing". A disabled source's frames
   simply age into `stale`; health is cached 10 s.
8. **Series semantics.** One product, so "observed past plus forecast future" means observation frames for an
   observation product and one run's steps (`kind: forecast`) for a forecast product. More than 500 steps is
   400 VALIDATION_FAILED, as the spec says, rather than a silent cut. A disabled product's steps are
   `unavailable`.
9. **`getSample` `record=`** (in the spec, marked for 106) answers 400 until 106 builds it.
10. **`fetch_log.trigger`.** Derived invocations are written as `SCHEDULED` with `principal=derived`. A new
    `DERIVED` trigger value would change a stored enum and the spec's `FetchLogEntry.trigger`.
11. **Forecast window.** `smoke.hrrrPm25Max24h` and `forecast.gfs` are computed when a run completes (and at
    reconcile) over the steps in [now − 1 h, now + 24 h]; ticks only judge their staleness (run older than
    12 h). `precip24hMm` is the run total at the step nearest now + 24 h minus the total at the step at or
    before now.
12. **GLM-stops IT.** The design's three ITs are named as the prompt names them. `GlmStopsNoAllClearIT` drives
    the GLM ring through `FlashStore.append` and the `goes-glm` input age through its adapter state; 048's
    `GlmFeedStopsIT` already covers the cluster records.
13. **StatsIT's lcc case.** The "latlon and lcc within 1%" check: latlon runs over HTTP in `StatsIT`; lcc runs
    as `CellAreasTest.lccWithinOnePercent` over a synthetic HRRR-like grid. The shared web context has no
    lcc product, and adding one would mean another context.
14. **MCP resource templates** were not supported by 025's protocol (`resources/templates/list` returned
    nothing). `ForesignalMcpResourceTemplate` and the `McpProtocol` changes add them.

## Issues found / open questions

**Needs the owner** (non-blocking; defaults chosen and documented):
1. **Place-weather geometry** (Deviation 2). The record carries no position, so it is invisible on maps and
   to `near` rules. **Owner:** keep it so, or give it the place's point and rely on `private`/restrictions?
2. **Polygon and route places** (Deviation 3). Distances run from the representative point. **Owner:**
   measure from the area's outline instead (more expensive index builds)?
3. **Source-disabled semantics** (Deviation 7). **Owner:** should a disabled input source make samples
   `unavailable` immediately rather than `stale` after `maxGap`?

**Other findings:**
- **Not verified at run time:** no tests ran. In particular:
  - Spring wiring of the new beans (`DerivedRunner`, `DerivedGraph`, `McpProtocol` with templates);
  - the `fetch_log` rows written by the writer for derived batches;
  - the trigger engine's handling of `radar.*` nested paths on `wx-places`;
  - the `PlaceGridIndexBenchmarkTest` timing on the checkpoint machine.
- **Data:** no web pages or upstream data were fetched; no instructions directed at an agent were seen.
- Owner decisions carried from 012–051 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `derive/DerivedRunnerIT` | `test-derived` on the real runtime: reconcile first on enable; one `principal=derived` `fetch_log` row per invocation; health success. Frame events with valid time (other products ignored). Per-product coalescing while busy. Change batches of the subscribed source only. Overflow → reconcile, dropped batches lost. Gap → reconcile. A failing hook → FAILED row and a health failure, then recovery. `ctx.http()` throws. TTL rejected by `DescriptorValidator`. A cycle fails `DerivedGraph`. Forced ticks. |
| `derive/DerivedGraphTest` | The shipped weather graph is acyclic. Two-node cycle named. Self-loop. Chains and frame-only inputs. Change matching rules. |
| `derive/EmitLedgerTest` | Identical payloads skipped across number types. `observedAt` raised. Tombstones once and only for existing entities. Seeding from heads; `forget`. |
| `raster/sample/SampleServiceTest` | Mocked store: ok nearest (value, units, label, frame key, age, attribution). Named code `noEcho`. MRMS code 0 and off-grid → no-coverage text. no-data. stale. unavailable (no frame, disabled, failing, expired; degraded stays ok). −PT1H and +PT6H on an observation product. A forecast step at or before +PT6H, bilinear. `localTime` in America/Chicago and UTC. u/v derivation (270°, W). Imperial. `TimeSpec` parsing. Product resolution, 404s, the 20-product cap. Compass. |
| `raster/sample/SampleEndpointIT` | HTTP `getSample` validated against `SampleResponse`. no-coverage text, unavailable past, stale. Forecast `+PT1H`. Place: 401 anonymous, local time with a token, 404. `postSample` order, validated, 101 points → 400. Errors (404 product, bad lon, missing point, `record=`, bad time). `getPlaceWeather` (401, `PlaceWeatherResponse`, 404). |
| `raster/sample/SeriesIT` | Observed frames in order with values and statuses; no-coverage. A forecast run's steps. 500 steps ok, 501 → 400. Bad range 400, unknown product 404. All validated against `Series`. |
| `raster/sample/StatsIT` | bbox over the whole synthetic grid: area within 1% of the exact box, coverage 66.7, min/max, areas above thresholds. Middle-row polygon. stale/no-coverage/unavailable carry no statistics. Exactly one area form; 2,000,000 km² limit; place needs a token; 404. Validated against `StatsResponse`. |
| `raster/sample/CellAreasTest` | latlon cells sum to the exact box; polygon areas; lcc (HRRR-like) box within 1%. Scan statistics (min, max, mean, p95, area above, mask). Geometry limits. |
| `raster/sample/PlaceGridIndexBenchmarkTest` | 50 places over a 0.01° grid: best of 30 scans under 5 ms × a CI margin of 4. Index sorted; thresholds' nearest distances; radius maximum. |
| `source/adapters/wx/PlaceWeatherGeneratorTest` | Fake world, no Spring. Status-only sections before data (no nulls, no geometry). Radar metrics, 0.5 km rounding, 999. 10-minute rolling minimum. Stale keeps values with a fixed `asOf` and zeroes the helper. No-coverage keeps values. Identical frames emit nothing. Lightning (15/30 min, count, stale, failing). 999 for no lightning. Deleted places tombstoned; reconcile tombstones and restores. Cell ETA 10 min / 999 / unavailable. Rounding helpers. |
| `source/adapters/wx/RadarCellTrackerTest` | Labeling. Opening removes speckle and bridges. Core split. Matching radius per gap. Least-squares motion. `wx-cells` lifecycle: ids persist, motion after a match, vanished cells tombstoned. Reconcile restores tracks. Flash buckets. |
| `source/adapters/wx/WxAreasTest` | `refl:50:home` area to 2 significant figures, max, coverage, polygon; empty → null geometry. Parameters and checks. Stale keeps area and geometry with `asOf`; deleted place and removed area tombstoned. |
| `source/adapters/wx/GlmStopsNoAllClearIT` | Required §7.4: GLM stops → lightning `stale` with values and a fixed `asOf`, verified helper 0, no exit alert, no new version later; `goes-glm` `degraded` and the `SourceHealthChangedEvent` fires. |
| `source/adapters/wx/UnchangedPlaceWeatherIT` | Required §7.4: 15 identical frames over 30 min with ticks → no new versions, the enter alert fires once; one `fetch_log` row per invocation. |
| `source/adapters/wx/MrmsNoCoverageIT` | Required §7.4: a no-coverage frame keeps the radar values (`no-coverage`, `asOf`, helper 0) and fires no all-clear; covered and far later → exactly one exit. |
| `source/adapters/wx/PlaceDeleteTombstoneIT` | Deleting the place tombstones `place:{id}` and fires the exit rule (documented). |
| `mcp/WeatherMcpToolsIT` | Compact ≤ 4 KB and long ≤ 50 KB; status fields on every value; no-coverage text; the untrusted note; hazards with `automated`; places need a token, no coordinates; `weather_forecast` and `area_weather_stats` fields; place tools agent-scoped; the resource and the template. |
| Updated | `FamilyDefaultsTest` (the three generators in `weather`, regex includes `DerivedGenerator`, §2.4 kinds). `RestrictedReadCoverageTest` (exempt `list_products`, `weather_forecast`, `area_weather_stats`, `ProductsResource`). `NoClientTriggeredFetchIT` (sample and series paths). `RasterAccessPolicyTest` (sampling routes, place weather always authenticated). |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 71 warnings (unchanged).

`python3 scripts/generate-postman.py docs/OpenAPI.json postman` → 829 requests, covers all operations.

No new dependencies.

## Notes for the next prompt

1. **053 (MRMS second wave, gridpoints):**
   - add `forecast.nws` and `precip.qpe*`/`ffgRatio1hPctMax` to `PlaceWeatherState` as new `WxSection`s;
   - add `rotation`, `qpe1h` and `ffg` fields to `WxAreasGenerator.AreaDef` (its pattern accepts only `refl`,
     `mesh` and `precip` today);
   - add an `nws` provider to `ForecastSeriesService.PROVIDERS`;
   - `qpe@1` already exists (051).
2. **099 and later derived sources:** implement `DerivedGenerator`; follow Adapter Guide §14. Declare
   `zeroResultsNormal(true)` and no TTL, and list the generator in `FamilyDefaultsTest.CLASSES`. A generator
   that consumes another's kinds is checked by `DerivedGraph`.
3. **055 (`get_map_image`) and 106:**
   - `SampleService.valueOf`, `FrameView` and `RasterReader` are reusable;
   - 106 implements `getSample`'s `record=` (`SamplePosition`) and the `stale-position` status;
   - 105 adds `disagreement`.
4. **057 checkpoint:**
   - the 052 web ITs share `RasterTestSources` (now with `test-derived`);
   - the place-weather ITs share 047/048's `WeatherIngestSupport` context (`PlaceWeatherSupport`), each on its
     own date;
   - `DerivedRunner` starts in every context but runs only enabled derived sources, and every test disables
     what it enabled;
   - check `PlaceGridIndexBenchmarkTest`'s timing.
5. **143:** measure the `wx-places` scan per MRMS frame on the real 7000 × 3500 grid; the frame cache holds
   the decoded 24.5 MB frame. Also measure the `wx-cells` window size for the configured AOI and
   `ForecastSeriesService`'s first-request time (all GFS steps decoded).
6. **144 (Flyway):** no new tables or columns. `fetch_log.principal` takes `derived`. New sources `wx-places`,
   `wx-areas` and `wx-cells` get `source_settings` rows.
7. **Owner decisions pending:** Issues 1–3 above, plus those carried from 012–051.
