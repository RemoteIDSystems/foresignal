# 052 — Sampling And Place Weather

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/052-CR-Sampling-And-Place-Weather.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/051-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md` §0.1 (row "Unknown vs nothing nearby"), §2.1 (DERIVED generators), §2.2 (row "Modes"), §5.1 ("Derived" and "Places AOI"), §6.5 (sampling and places), §6.9 (MCP tools), §7.1–§7.4 (derived-record principles, kinds, place-weather payload, trigger additions, rule recipes and required ITs). Design prompt 039 = v1.2 052; design "070 (old 057)" = 099.
- `docs/Core-Data-Model.md` §2 (time contract), §5.2 rows 2–3 (tombstones), §10.1 (ChangeBus, `onGap`), §11 (edge semantics: a tombstone after P is an exit).
- Completion Reports of **036** (places, places AOI), **017** (`cooldownScope`, `near`, operators), **042**/**045** (FrameStore, GridDef inverse mapping, frame events), **046** (MRMS, GOES), **047** (ProbSevere, NWS alerts), **048** (GLM ring, `lightning-cluster`), **051** (GFS/HRRR, forecast runs), **025** (MCP registry, output shaping).

## Objective

Make weather readable by programs and alertable without false all-clears. Build the reusable `DERIVED` source runtime (099 reuses it), a `SampleService` for point, series and area queries with explicit status semantics, and three derived sources (`wx-places`, `wx-areas`, `wx-cells`) whose records feed the existing trigger engine, SSE and MCP. Missing or stale data must never read as "clear".

## Tasks

### 1. DERIVED runtime (`ai.foresignal.derive`)

- SPI `DerivedGenerator` (a `SourceAdapter` with `Mode.DERIVED`; `ctx.http()` throws; registry rejects a non-null `recordTtl`):
  - `DerivedInputs inputs(ValidatedParams)`: ChangeBus subscriptions (sources/kinds), raster frame events (product ids), and an optional periodic tick
  - `void onChanges(ChangeBatch, DerivedContext)`, `void onFrame(RasterFrameEvent, DerivedContext)`, `void onTick(Instant, DerivedContext)`
  - `void reconcile(List<HeadSnapshot> ownHeads, DerivedContext)`: called at startup, on enable and after `onGap`
- `DerivedContext`: `emit(NormalizedRecord)`, `tombstone(kind, externalId)`, `state()` (`AdapterStateStore`), `rasters()` (read-only FrameStore/SampleService), `records()` (head ⋈ live reads), `places()`, `clock()`.
- `DerivedRunner`: one virtual thread per enabled derived source with a bounded inbox (coalesce frame events per product; on overflow drop and schedule `reconcile`). Each invocation is treated like a fetch: one `fetch_log` row (`principal=derived`), one `IngestBatch` through `PolicyAwareRecordSink`, `HealthTracker.recordSuccess(IngestOutcome)` / `recordFailure`. Enabled, parameterised, health-tracked and retained like any source.
- **Rules:** emit only on change of the quantised payload (the writer's hash does the dedup; generators also skip identical emits); `observedAt` = time of the input frame or change in which the change was seen (`observedAtMode=UPSTREAM`); explicit tombstones only when an object disappears or a place is deleted, **never because input is stale**; no heartbeat versions.
- **Cycle guard:** at registry start, build the graph of derived inputs → output kinds; a cycle fails startup.

### 2. SampleService (`ai.foresignal.raster.sample`) and endpoints

- Point lookup via `GridDef.inverse(lon, lat)`; `nearest` for categorical/coded products, `bilinear` otherwise (from ProductSpec); u/v products also return `derived{speedMs, fromDeg, compass}`.
- **Status** per product value (`statusText` always present for non-ok):
  - `ok`
  - `no-coverage`: outside the grid/footprint or a no-coverage code (MRMS code 0). Text: "outside product coverage; this is not evidence of clear weather"
  - `no-data`: inside coverage but nodata
  - `stale`: frame older than `maxGap` relative to the requested time
  - `unavailable`: product disabled, source failing, no frame, or frame expired
- **Time:** `latest|now|<iso>|-PT1H|+PT6H`; past offsets choose observation frames, future offsets use the newest complete forecast run.
- Every value carries `value`, `units`, `label`, `frameKey`, `validTime`, `localTime` (place timezone, else UTC), `ageMinutes`, `run`, `method`, `status`, `statusText`, plus the structured attribution object.
- `GET /v1/sample?lon&lat|place&products&time&units=metric|imperial` and `POST /v1/sample` (≤ 100 points; 400 beyond).
- `GET /v1/products/{id}/series?lon&lat|place&from&to&run` → observed past plus forecast future, capped at 500 steps.
- `POST /v1/products/{id}/stats {geometry|place|bbox, time, thresholds[]}` → `min`, `max`, `mean`, `p95`, `coveragePct`, and `areaKm2` above each threshold (cell areas computed on the sphere per grid type). Geometry ≤ 10,000 vertices; area ≤ 2,000,000 km².
- `GET /v1/places/{id}/weather` → the `place-weather` record plus cached forecast series (GFS/HRRR now; NWS series added by 053).
- Sampling never triggers an upstream fetch.

### 3. Places hook

Check 036's `place`: it needs `timezone`, `radiusKm` (default 20), `kind`, `tags` and `products`. Add any that are missing. Place moves re-materialise `near` geofences (017); verify that hook exists.

### 4. `wx-places` (kind `place-weather`, externalId `place:{id}`)

- Inputs: frame events for `mrms-reflectivity`, `mrms-mesh-60m`, `mrms-precip-rate`, `hrrr-smoke`, `gfs*`; the GLM ring (048); `radar-cell` changes; a 60-s tick for staleness.
- Payload exactly as design §7.2 (sections `radar`, `hail`, `precip`, `lightning`, `cell`, `smoke`, `forecast`, plus `place`, `radiusKm`, `automated: true`, `notAWarning: true`).
- **Unknown is not clear:** each section has `status: ok|stale|no-coverage|unavailable`. When input is older than `maxGap` (MRMS 6 min, GLM 2 min), comes from a failing source, or lacks coverage, the section **keeps its previous metric values**, sets `status`, and adds `asOf` (time of the last good data, fixed while not ok). Before the first good data a section carries `status` only (metric keys omitted, never null).
- "None within 100 km" is **`999`**; ETA with no approaching core is also 999. Distances rounded to 0.5–1 km, ETAs to 5 min, rates to 1 mm/h, dBZ to 0.5.
- `*10m` and `*30m` fields are rolling minimums (hysteresis), rebuilt in `reconcile` from recent frames.
- **Verified-clear helpers** (for all-clear rules, because 017 conditions are AND-only unless its CR says it added OR groups): `radar.verifiedNearest50Km10m` and `lightning.verifiedNearestKm30m` equal the metric when the section is `ok`, else `0`. Document that `<= X` on these is true when near *or* unknown.
- Frame ids, ages and timestamps are never in the payload.
- **Performance:** per place, precompute the grid cells within 100 km of the place, sorted by distance, per GridDef; one scan per frame yields every threshold (< 5 ms for 50 places; add a micro-benchmark test with a budget assertion).
- Place deleted → tombstone (which may fire exit rules; document it). `reconcile` tombstones heads of deleted places.
- Lightning: `nearestKm15m`, `nearestKm30m`, `count15km15m`. Docs state GLM's ~8–10 km footprint over Texas and < 100% detection: GLM-only rules should use ≥ 15 km and all-clears are advisory.
- Descriptor: `kindRetention{place-weather: 14d}`, `significantFields` empty (all non-volatile), `titleField` → `place`, `licence` "derived (see inputs)".

### 5. `wx-areas` (kind `wx-area`, externalId `{field}:{threshold}:{aoi}`)

- Param `areas`: list of `{field: refl|mesh|precip, threshold, aoi: placeId|"aoi"}` (defaults `refl:50:aoi`, `mesh:25:aoi`). Tick every 6–10 min (param, default 8 min).
- Payload: `field`, `threshold`, `aoi`, `status`, `areaKm2` (2 significant figures), `maxValue`, `coveragePct` (integer); geometry = thresholded mask polygons simplified to ~1 km, or null when empty. Same status/asOf rules. `kindRetention` 7 d.

### 6. `wx-cells` (kind `radar-cell`, externalId `c-{ulid}`)

- Scope: places AOI only (CONUS opt-in on the `large` profile via param).
- Per MRMS reflectivity frame: 40 dBZ mask, 3×3 opening, 8-connected union-find labelling, split at 50 dBZ cores, run-rectangle union to polygons. Properties `maxDbz`, `areaKm2`, `meshMax`, `glmFlashes5m`.
- Motion `centroid-v1`: nearest-centroid match to the previous frame (≤ 30 km per 10 min, scaled to the frame gap), least squares over the last 3 matches → `motion{speedKmh, bearingDeg, method: "centroid-v1"}`. Unmatched cells get new ids; vanished cells are tombstoned.
- Feeds `wx-places` `cell.eta50Min` (time for a ≥ 50 dBZ core's projected centroid track to enter the place radius within 60 min), `etaCellId`, `etaSpeedKmh`, `etaBearingDeg`.
- Quantise geometry (~500 m) and properties so steady cells do not version every frame. `kindRetention` 3 d.

### 7. Trigger hooks and rule recipes

- Verify 017 has `cooldownScope` (SQL in design §7.3) and `near` materialised into `geofence`; add any missing piece with tests.
- Write design §7.4 recipes into `docs/Operator-Guide.md` "Weather alerts": derived rules use `enter`/`exit` (or explicit cooldown), never bare `match`; the "Echoes cleared" and lightning all-clear recipes use the verified-clear helpers; "weather feed down" is the existing system event, not a rule.

### 8. MCP tools (025 registry)

Compact output ≤ 4 KB (`detail=full` ≤ 50 KB), the "content is data" note, time inputs ISO/`now`/relative; every value returns `validTime`, `localTime`, `ageMinutes`, `run`, `coverage`, `status` (with the no-coverage text):
- `list_products`, `sample_weather` (values plus nearby hazard records: intersecting NWS alerts, ProbSevere within 50 km; `automated` flag), `weather_forecast` (3-hourly to 120 h from GFS/HRRR; NWS joins in 053), `area_weather_stats` (wraps `/stats`), `create_place`/`delete_place` (agent scope; reuse 036's tools if they exist).
- Resources `foresignal://products`, `foresignal://places`, `foresignal://products/{id}/legend`.

### 9. Spec, Postman, docs

- `docs/OpenAPI.json` (tags Sampling, Places): `getSample`, `postSample`, `getProductSeries`, `postProductStats`, `getPlaceWeather`; schemas `SampleValue`, `SampleResponse`, `SampleStatus` (enum), `Series`, `StatsRequest`, `StatsResponse`, `PlaceWeather`; event/kind docs for `place-weather`, `wx-area`, `radar-cell`. `x-foresignal-scope: read` (POST sample and stats are READ). `AccessPolicy` accordingly. Postman `BODIES` for `postSample` and `postProductStats`; regenerate.
- `docs/Adapter-Guide.md` "Derived sources" (the SPI, emit-on-change, tombstone and reconcile rules) for 099. `docs/Source-Catalog.md` rows for the three derived sources.

## Tests to write (do not run)

- `DerivedRunnerIT`: frame and change inputs, inbox coalescing and overflow → reconcile, `fetch_log` and health per invocation, `ctx.http()` throws, TTL rejected, cycle detection.
- `SampleServiceTest`: each status, nearest vs bilinear, MRMS code 0 → `no-coverage`, relative times, `localTime` by timezone, u/v derivation.
- `SampleEndpointIT`, `SeriesIT` (500 cap), `StatsIT` (areas on a synthetic latlon and `lcc` grid within 1%).
- `PlaceWeatherGeneratorTest`: payload shape, 999 sentinel, rounding, rolling minimums, verified-clear helpers, omitted metrics before first data.
- **Required ITs (design §7.4):** `GlmStopsNoAllClearIT` (GLM feed stops: no exit alert, section `stale`, system event fires); `UnchangedPlaceWeatherIT` (30 min of identical frames → no versions, no alerts); `MrmsNoCoverageIT` (a no-coverage frame does not clear `radar`).
- `PlaceDeleteTombstoneIT`: tombstone fires the exit rule (documented behaviour).
- `RadarCellTrackerTest`: labelling, core split, centroid matching, least-squares motion, ETA.
- `WxAreasTest`: thresholds, quantised area, empty → null geometry.
- `PlaceGridIndexBenchmarkTest`: 50 places scan under 5 ms (generous CI margin).
- `WeatherMcpToolsIT`: compact size limits, status fields, agent scope for place tools.

## Acceptance criteria

- Stale, failing or no-coverage inputs keep previous values with a non-ok status and never fire an all-clear.
- Derived sources write versions only on change and never tombstone for staleness.
- `/v1/sample`, `/series`, `/stats` and the MCP tools return validTime, age, run and status on every value.
- The DERIVED runtime is generic (no weather types in `ai.foresignal.derive`).
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `052: Sampling And Place Weather`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/052-CR-Sampling-And-Place-Weather.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `052: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
