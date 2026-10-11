# 105 — Weather Models And Storm Tracking

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/105-CR-Weather-Models-And-Storm-Tracking.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/104-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Weather-Imagery-Design.md`:
  - §10.3 row "073" (this prompt; its 073 = 105)
  - §5.3 (IFS rules), §1.4 (ECMWF licence facts), §2.3–§2.5 (frames, ladders, budgets), §6.1/§6.8 (forecast run semantics, SSE), §7.1–§7.4 (derived-record principles: emit on change, no TTL, tombstone and reconcile, status sections, `999` sentinel, enter/exit recipes), §11 (rows deferred to 073)
  - Translate its numbers with `docs/Roadmap.md` §5: its 029/031/032/038/039 = 042/045/046/051/052, and its 035 = 048
- Completion Reports of 042, 045, 046 (NetCDF-Java GRIB decoder, `RasterDecoder` SPI, startup self-test with the 5.42 fixture), 048 (GLM ring, `lightning-cluster`, FDC, HMS), 051 (GEV `.idx` range planning, `gfs`/`hrrr`, run/step semantics, timeline), 052 (DERIVED runtime, `wx-places`, `wx-areas`, `radar-cell` centroid-v1), 008 (`validFrom`), 017 (`forecastHorizon`, `cooldownScope`, `near`), 101 (recipe SPI, intelligence events), 078 (AQ sensors), 087 (FAA NAS status), 094 (`ourairports`)
- ECMWF open data: `https://www.ecmwf.int/en/forecasts/datasets/open-data`, file browser `https://data.ecmwf.int/forecasts/`, AWS mirror `https://registry.opendata.aws/ecmwf-forecasts/`
- Verified 2026-10:
  - The open resolution is still **0.25°**. The 0.1° move in design §1.4 has not happened; keep the resolution segment configurable.
  - IFS 00/12z steps: 0–144 by 3, then 150–360 by 6. Since cycle 50r1 (13 May 2026), 06/18z sit under `stream=oper`, steps 0–144 by 3.
  - The gust parameter appears as `10fg3` for steps 3–144.
  - The whole real-time catalogue is CC BY 4.0.

## Objective

Deliver the v1.1 weather layer:
- ECMWF IFS as a third model;
- `forecast-area` threshold polygons that forecast-aware rules can watch;
- TITAN-lite storm tracking with merges, splits, swaths and a GLM lightning-jump flag;
- a model-disagreement flag;
- correlation annotators that turn weather co-occurrences into intelligence events (101) with explicit evidence and alternatives.

## Tasks

### 1. `ecmwf-ifs` model source

- POLLING product source on the 051 model framework.
- **Access:** prefer the `ecmwf-forecasts` S3 mirror via `listS3` and `getRange`; fall back to `https://data.ecmwf.int/forecasts/`.
  - Path template (configurable): `{yyyymmdd}/{HH}z/ifs/{res}/oper/{yyyymmdd}{HH}0000-{step}h-oper-fc.grib2`, with `res` default `0p25`.
  - Parse the per-file `.index`, which is JSON lines with `param`, `levtype`, `levelist`, `_offset` and `_length`. It is **not** the NCEP `.idx` format, so add an `EcmwfIndexParser` alongside the GEV `.idx` planner and plan byte ranges from it.
  - Verify the template and index keys against a live listing and record any differences.
- **Fields:** `10u`/`10v` (wind field + barbs), `10fg3`/`10fg` (gust), `2t`, `tp` (accumulated, so derive 3-h and 24-h), `msl`.
  - Match by `param` + `levtype` (+ `levelist`), never by message order.
  - The grid comes from the GRIB header; normalise lo1 = 180 (−180..180 → 0..359.75 for field output, as GFS).
- **Run selection:** 00/12z only by default (`runs` param), steps 0–144 by 3 (configurable list up to 240). A run is complete when every planned step's index exists.
- **Requests** are serial, honour Retry-After, and back off on 503. Budget 20/min, 3,000/day, `mbPerDay` 1,200.
- **Descriptor:**
  - `licence: "CC-BY-4.0"`, `termsUrl` set;
  - structured attribution "Contains modified Copernicus/ECMWF data: ECMWF open data, CC BY 4.0. Changes made: resampled, quantised, derived fields." on every derived output (tiles, fields, samples, `forecast-area`, place-weather forecast section).
- **Products:** `ifs-gust`, `ifs-t2m`, `ifs-precip-3h`, `ifs-precip-24h`, `ifs-mslp` (i16 frames), and an `ifs` field set for `/fields` and barb MVT. Ladder: current + previous complete run. Forecast `raster-frame` versions are one per completed run (design §0.1).
- Add IFS to `/v1/products/timeline` and to `/sample`/`/series` model lists. Add the place-weather forecast key `ifs`.
- `THIRD_PARTY_NOTICES.md`: ECMWF CC BY 4.0.
- **Tests:** GRIB fixture capture procedure (range-read 2 messages from one live file, ≤ 2 MB); `EcmwfIndexParserTest`; WireMock adapter test (listing → index → ranges → run complete); decode contract test using the 046 self-test fixture.

### 2. `forecast-area` (DERIVED source `wx-forecast-areas`)

- On each completed model run (GFS, HRRR, IFS), threshold the configured fields over the places AOI:
  - defaults: gust ≥ 20 and ≥ 25 m/s; precip-24h ≥ 50 mm; t2m ≥ 40 °C and ≤ −10 °C;
  - configurable `thresholds[] {product, op, value, horizon ≤ 120h}`.
- Build per-step masks, then union the steps into one polygon per (model, field, threshold, AOI) over the horizon: 3×3 opening, run-rectangle union (reuse the 052 polygoniser), JTS simplify at 0.02°.
- **Records:**
  - kind `forecast-area`, externalId `{model}:{field}:{op}{value}:{aoi}`;
  - `validFrom` = the first exceeding valid time; `validUntil` (explicit) = the last exceeding valid time + step;
  - payload: model, field, threshold, `firstExceedAt`, `peakValue` (quantised), `peakAt`, `areaKm2` (2 significant figures), `run`, `automated: true`, `notAWarning: true`.
- Emit only on change of the quantised payload or of geometry (symmetric difference > 10%). When the exceedance disappears in a newer run, tombstone. Reconcile at startup.
- Document recipes: "IFS gust ≥ 25 m/s within 48 h at home" → `kind=forecast-area`, `near{home}`, `forecastHorizon 48h`, `enter`.

### 3. TITAN-lite storm tracking (DERIVED `wx-storm-tracks`)

- **Input:** each `mrms-reflectivity` frame event, within the places AOI (CONUS opt-in on the large profile, design §2.6). Reuse the 052 cell segmentation: 40 dBZ mask, 3×3 opening, 8-connected union-find, split at 50 dBZ cores, run-rectangle polygons.
- **Association (pure `TitanLiteTracker`, version `titan-lite-v1`):**
  1. Advect each active track's last polygon to the new frame time using its motion vector.
  2. Cost matrix: `1 − IoU(advected_prev, current)`. When IoU = 0, use `1 + d/30 km` if the centroid distance d ≤ 30 km per 10 min, else ∞.
  3. Solve with an in-house Hungarian algorithm (O(n³), n ≤ 300; above that, fall back to greedy by IoU with a health note).
  4. Unmatched current cell → new track. Unmatched track → `missed`; end after 2 missed frames.
  5. **Merge:** a current cell overlapping ≥ 2 advected tracks with IoU ≥ 0.1 each → the largest-area track continues; the others end with `mergedInto`.
  6. **Split:** one advected track overlapping ≥ 2 current cells with IoU ≥ 0.1 → the largest continues; the others start with `parentTrackId`.
- **Motion:** time-weighted least squares over the last 5 centroids (minimum 3; otherwise centroid-v1). Speed and bearing are quantised to 5 km/h and 10°.
- **Nowcast polygons:** +15, +30 and +60 min, advected linearly and labelled `method: linear-advection`.
- **Records:**
  - **`storm-track`** (externalId `t-{ulid}`):
    - current cell summary (maxDbz to 0.5, areaKm², meshMaxMm, flashRate);
    - `history[]` ring of the last 12 positions;
    - `motion {speedKmh, bearingDeg, method}`;
    - `trend {maxDbz: rising|steady|falling}`;
    - `lightningJump`;
    - `nowcast[]` (geometry stored as a GeometryCollection);
    - status (active | ended);
    - merge/split links.
    - Emit on change of quantised fields. Tombstone 30 min after `ended`.
  - **`storm-swath`** (externalId = track id): the union of the cell polygons along the track, plus `meshMaxMm` and the duration. Written every 10 min while active and at the end.
- Upgrade 052 `radar-cell` records to carry `trackId` and `motion.method: titan-lite-v1` when a track exists. place-weather `cell.eta50Min` prefers track motion when available.
- **GLM lightning jump** (Schultz et al. 2009, 2σ):
  - Per track, count GLM flashes from the ring inside the cell polygon buffered by 5 km (GLM footprint about 8–10 km over Texas, design §7.2), in 2-min bins.
  - `DFRDT = (rate_t − rate_{t−1}) / 2 min`.
  - A jump occurs when DFRDT > 2σ of the previous 5 DFRDT values and the rate ≥ 10 flashes/min.
  - Set `lightningJump {at, sigma, rateFpm, method: "2sigma-glm-v1"}` for 10 min.
  - Docs: GLM detection efficiency is below 100%; the flag is advisory.
- **Budget:** under 50 ms per frame for 100 cells, asserted by a micro-benchmark test with a generous bound.

### 4. Model-disagreement flag

- In `wx-places`, for each place, compare GFS, HRRR (when the horizon ≤ 18 h) and IFS over the next 24 h: gust max, precip 24 h and t2m max.
- `forecast.disagreement` = `{fields: ["gust"], spread: {gust: 9.5}, models: ["gfs","ifs"]}` when the spread exceeds thresholds: gust 8 m/s; precip `max(15 mm, 50% of the mean)`; t2m 4 °C. Values are quantised, with hysteresis (clear at 75% of the threshold), so place-weather does not churn.
- `/v1/sample` with several model products returns `disagreement` the same way.

### 5. Correlation annotators (101 recipes and annotations)

Implement as 101 recipes with `type: weather-impact`, plus an `EventAnnotator` SPI that adds `context.weather` and alternatives to existing events. Each has detector version, params, members with join reasons, and alternatives.

1. **`dry-lightning-ignition`:**
   - Seed: `lightning-cluster` (048) where MRMS precip rate < 1 mm/h and QPE-1h < 2.5 mm at the cluster.
   - Consequent: a new `fire-detection` (048 FDC) or FIRMS (065) within 10 km, 6–48 h later.
   - Status `emerging` until the fire is seen.
   - Alternatives: "human-caused or unrelated ignition", "fire existed before the lightning but was undetected".
2. **`outage-weather`** (annotator): for `infrastructure-disruption` events and outage records (068/092), add `context.weather` when within ±3 h and the area of:
   - NWS warnings (047);
   - radar ≥ 50 dBZ, from `wx-areas` or storm tracks;
   - forecast or observed gust ≥ 25 m/s.
   - Add the alternative "weather-related damage" with those members as **context**, not supporting evidence.
3. **`airport-weather`:** a large or medium `ourairports` airport (094) inside a storm-track nowcast polygon, within 10 km of a ≥ 50 dBZ cell, or inside a convective SIGMET (087). Supporting members: the FAA NAS status delay or ground stop for that airport when present. Without NAS evidence, severity is `info`.
4. **`smoke-aq`:** an HMS smoke polygon or HRRR MASSDEN ≥ 35 µg/m³ overlapping AQ sensors (078) with PM2.5 ≥ 35 µg/m³ within ±3 h. Contradiction: elevated PM2.5 with no smoke overlap → alternative "local source". The supporting evidence must include ≥ 2 sensors from ≥ 1 network.

- Missing optional sources (078, 087) disable the recipe with a reason (101 behaviour).

### 6. API, MCP, spec, docs

- No new endpoints beyond the products, kinds and recipe registrations. Products appear in `/v1/products`, kinds in `/v1/kinds`, and recipes in `/v1/intel/recipes`.
- **MCP:**
  - extend `weather_forecast` with `ifs` and the disagreement block;
  - add `list_storm_tracks` (read: active tracks near a place or point, compact: motion, ETA, trend, lightningJump, nowcast summary);
  - all with `validTime`, `run`, `ageMinutes` and `status` per design §6.9.
- `docs/OpenAPI.json`: kind schemas for `forecast-area`, `storm-track` and `storm-swath` in the kind schema docs; the product enum examples. Regenerate Postman.
- `docs/Source-Catalog.md`: an `ecmwf-ifs` row (S3 mirror first, CC BY 4.0, budget, steps, the 2026-10 facts above).
- `docs/Operator-Guide.md`: IFS cost (about 0.5–1 GB/day), forecast-area recipes, TITAN-lite limits (2-min frames, linear nowcast, AOI default), lightning-jump caveats, annotator meanings.
- `docs/Architecture.md`: an appendix line for the new derived sources.

## Tests to write (do not run)

- `EcmwfIndexParserTest`, `EcmwfRunPlannerTest` (00/12z steps, completeness, configurable resolution segment), `EcmwfAdapterTest` (WireMock: S3 listing, index, ranges, 503 backoff, Retry-After, fallback host).
- `ForecastAreaGeneratorTest`: thresholding, step union, quantised emit-on-change, tombstone when a run drops the exceedance, `validFrom`/`validUntil`.
- `HungarianTest`: known optimal assignments, rectangular matrices, ∞ costs.
- `TitanLiteTrackerTest` (synthetic polygon sequences): steady motion keeps the id; a merge ends the smaller track with `mergedInto`; a split gives `parentTrackId`; 2 missed frames end a track; least-squares motion.
- `LightningJumpTest`: a 2σ jump on a synthetic flash series; below the rate floor no jump.
- `ModelDisagreementTest`: thresholds, hysteresis, HRRR horizon cut-off.
- `WeatherAnnotatorsIT` (`TestRecords`): dry-lightning → fire within window creates an event with both members; outage-weather adds context only; airport-weather severity depends on NAS evidence; smoke-aq contradiction adds the alternative.
- `StormTrackIT`: a sequence of MRMS fixture frames → `storm-track` versions only on change, and a `storm-swath` at the end.

## Acceptance criteria

- IFS runs ingest from the S3 mirror with range reads planned from `.index`. Every IFS-derived output carries the CC BY 4.0 "changes made" attribution.
- `forecast-area` records carry `validFrom`/`validUntil` and work with `forecastHorizon` rules.
- Storm tracks keep ids across frames, record merges and splits, and expose motion and a lightning-jump flag with method versions.
- The annotators create or annotate intelligence events with members, join reasons, detector versions and alternatives.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `105: Weather Models And Storm Tracking`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/105-CR-Weather-Models-And-Storm-Tracking.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `105: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
