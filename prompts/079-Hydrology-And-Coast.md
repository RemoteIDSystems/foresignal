# 079 — Hydrology And Coast

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/079-CR-Hydrology-And-Coast.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/078-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §4b row "US hydrology (USGS OGC API, NWPS flood categories, SNOTEL, USDM)" ("NWPS supplies the flood stages that make gauge triggers meaningful"), §8.C item 40 (Gap label 043a = this prompt: usgs-water, nwps-gauges, snotel, usdm, noaa-coops-tides; EA/ECCC optional)
- `docs/Core-Data-Model.md` §2 ("Measurements" row), §3 (limits: 10,000 vertices; sentinel scrubbing), §5.3
- Prompt 073 kits (`OgcFeaturesClient`, `FeatureGeometry` simplification, feature spec format)
- Upstream docs: USGS Water Data APIs (`api.waterdata.usgs.gov/docs/`, keys at `/docs/ogcapi/keys`), NOAA NWPS API (`api.water.noaa.gov/nwps/v1/docs/`), NRCS AWDB REST (`wcc.sc.egov.usda.gov/awdbRestApi/swagger-ui/index.html`), US Drought Monitor data (`droughtmonitor.unl.edu/DmData/GISData.aspx`), NOAA CO-OPS Data and Metadata APIs (`api.tidesandcurrents.noaa.gov/api/prod/`, `/mdapi/prod/`), Environment Agency flood-monitoring API, MSC GeoMet OGC API (`api.weather.gc.ca`)

## Objective

Add river, snow, drought and coastal water sources so flood and water triggers have meaning: USGS real-time gauges through the new OGC API, NWPS gauges carrying official flood categories and stage thresholds, SNOTEL snowpack, the weekly US Drought Monitor, and NOAA tide gauges with flood thresholds; plus optional UK and Canadian equivalents.

## Tasks

Common: package `ai.foresignal.source.adapters.hydro.<name>`; `historyTier=STANDARD`, policy ALL; measurement time base (`observedAt = eventTime` = measurement time, `ttlAnchor=OBSERVED`) unless stated; `fieldSpecs` with UCUM units; scrub sentinels (`-999`, `-9999`, `0001-01-01T00:00:00Z`) to omitted fields; all sources **disabled** by default; US federal data is public domain unless stated.

### 1. `usgs-water` (OGC kit)

- Verified 2026-10: `https://api.waterdata.usgs.gov/ogcapi/v1/collections/latest-continuous/items` serves GeoJSON with cursor paging (`links[rel=next]` carries `cursor=`); `v0` still answers but its links point at `v1`, so use `v1`. Item properties: `time_series_id, monitoring_location_id` (`USGS-06720990`), `parameter_code, statistic_id, time, value` (**a string**), `unit_of_measure, approval_status` (`Provisional|Approved`), `qualifier, last_modified`; Point geometry. The legacy `waterservices.usgs.gov/nwis/iv` still answered on 2026-10-10 but is being retired: do not use it.
- Query: `f=json&limit=1000&parameter_code={codes}&bbox={bbox}` (params `parameterCodes`, string-list, default `00060,00065` (discharge, gage height); `bbox` **required**, ≤ 5°×5°; optional `monitoringLocations` list instead of bbox). Max 20 pages.
- Optional credential `apiKey` sent as header `X-Api-Key`. USGS publishes no anonymous limit; keys raise the hourly limit (the docs show `X-RateLimit-Limit: 1000` as an example). Read `X-RateLimit-Remaining` and record it in the fetch trace; budget 4/min, 1500/day without a key.
- Names: once per 24 h read `collections/monitoring-locations/items?f=json&bbox=…` for `monitoring_location_name`, `site_type_code`, `state_name` (cache in state).
- Record `kind = water-observation`, `externalId = monitoring_location_id + ":" + parameter_code`; payload `siteName, siteType, parameterCode, parameterName` (from a small bundled table: `00060 Discharge`, `00065 Gage height`, `00010 Water temperature`, `72019 Depth to water`), `value` (number), `unit` (`ft3/s`, `ft`, `degC`), `approval, qualifier`. Leave out `last_modified` and `time_series_id`. TTL 3 h. Interval 15 min (min 5 min).

### 2. `nwps-gauges`

- List: `https://api.water.noaa.gov/nwps/v1/gauges?bbox.xmin=…&bbox.ymin=…&bbox.xmax=…&bbox.ymax=…&srid=EPSG_4326` (param `bbox` **required**, ≤ 10°×10°). Verified 2026-10 list fields: `lid, name, rfc{abbreviation}, wfo{abbreviation}, state{abbreviation}, latitude, longitude, pedts, status.observed {primary, primaryUnit, secondary, secondaryUnit, floodCategory, validTime}`, `status.forecast {…}` (sentinels `-999` and `0001-01-01T00:00:00Z` when no current forecast; category `fcst_not_current`).
- Thresholds: `GET /nwps/v1/gauges/{lid}` once per 24 h per gauge (spread across the day; cache in state): `flood.categories.{action,minor,moderate,major}.{stage,flow}` (`-9999` = undefined), `flood.stageUnits`, `flood.flowUnits`, `usgsId`, `inService`, `forecastReliability`. Cap gauges per source at 300 (param `maxGauges`).
- Record `kind = river-gauge`, `externalId = lid`, point; payload `name, rfc, wfo, state, usgsId, inService, stage, stageUnit, flow, flowUnit, floodCategory` (`no_flooding|action|minor|moderate|major|not_defined|obs_not_current|out_of_service`), `forecastCategory, forecastStage, forecastValidTime` (omitted when not current), `actionStage, minorStage, moderateStage, majorStage`, `stageToMinor` (minor − stage, when both exist). `observedAt = eventTime = status.observed.validTime`; TTL 3 h.
- Interval 15 min (min 10 min), budget 4/min, 1500/day (list calls plus up to 300 detail calls/day). `tileProperties = name, floodCategory, stage`; `significantFields` empty. Document trigger examples (`floodCategory in [minor, moderate, major]`) in the Source Catalog.

### 3. `snotel`

- NRCS AWDB REST (verified 2026-10): stations `https://wcc.sc.egov.usda.gov/awdbRestApi/services/v1/stations?stationTriplets=*:{state}:SNTL&returnForecastPointMetadata=false` (check during capture that the triplet wildcard filters to SNTL; the `networkCds`/`stateCds` form returned other networks), daily metadata refresh; data `…/v1/data?stationTriplets={list}&elements=WTEQ,SNWD,PREC,TOBS&duration=DAILY&beginDate={d-2}&endDate={today}` → `[{stationTriplet, data:[{stationElement{elementCode, storedUnitCode}, values:[{date, value}]}]}]`.
- Params `states` (string-list, required) or `stations` (triplets), ≤ 200 stations; batch 50 triplets per data call.
- Record `kind = snow-observation`, `externalId = stationTriplet`, point from metadata, payload `name, elevationM` (ft → m), `sweMm` (`WTEQ` in → mm), `snowDepthCm` (`SNWD`), `precipAccumMm` (`PREC`), `airTempC` (`TOBS` °F → °C). `observedAt = eventTime` = the value date at 00:00 in the station's `dataTimeZone`, converted to UTC. TTL 48 h. Interval 6 h, budget 2/min, 50/day.

### 4. `usdm`

- `https://droughtmonitor.unl.edu/data/json/usdm_current.json` (verified 2026-10: 4.6 MB FeatureCollection of 5 MultiPolygons with only `OBJECTID, DM` (0–4), `Shape_Length, Shape_Area`; ETag + Last-Modified; released Thursdays). Conditional GET makes six of seven days a 304.
- Map date: the USDM map is valid for the Tuesday before the Thursday release. Compute it from `Last-Modified` (latest Tuesday on or before that date), then confirm with `HEAD https://droughtmonitor.unl.edu/data/json/usdm_{yyyyMMdd}.json` (200 expected; on 404 step back one week once and log).
- Record `kind = drought-area`, `externalId = "D" + DM`; geometry simplified with `FeatureGeometry` to ≤ 9,500 vertices (each category is far above the 10,000-vertex limit); payload `category` (`D0`…`D4`), `label` (`Abnormally Dry`, `Moderate Drought`, `Severe Drought`, `Extreme Drought`, `Exceptional Drought`), `mapDate`, `geometrySimplified`, `simplifyToleranceDeg`. `observedAt = eventTime = mapDate` (UPSTREAM). No TTL; a category absent from a new map (e.g. no D4) is tombstoned immediately.
- Interval 6 h, budget 1/min, 10/day, `mbPerDay` 30, `maxResponseBytes` 16 MB. Attribution (required): "The U.S. Drought Monitor is jointly produced by the National Drought Mitigation Center at the University of Nebraska-Lincoln, the United States Department of Agriculture, and the National Oceanic and Atmospheric Administration. Map courtesy of NDMC." Say in `description` that geometry is simplified.

### 5. `noaa-coops-tides`

- Params: `stations` (string-list, **required**, ≤ 20), validated against the water-level station list `https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi/stations.json?type=waterlevels` (777 KB; cached 7 d).
- Every run, per station: `https://api.tidesandcurrents.noaa.gov/api/prod/datagetter?product=water_level&station={id}&date=latest&datum=MLLW&units=metric&time_zone=gmt&application=Foresignal&format=json` (verified 2026-10: `{metadata{id,name,lat,lon}, data:[{t, v, s, f, q}]}`, values are strings; `q` `p` preliminary / `v` verified). The `application` parameter is required by CO-OPS etiquette.
- Every 6 h per station: `product=predictions&interval=hilo&date=today` plus tomorrow (`begin_date`/`end_date`) → next high and low after now.
- Daily per station: `…/mdapi/prod/webapi/stations/{id}/floodlevels.json` (verified: `nos_minor, nos_moderate, nos_major, nws_minor, nws_moderate, nws_major, action`, in feet) and `…/stations/{id}/datums.json`. Flood levels are not relative to MLLW (The Battery: NOS minor 10.19 ft while the MLLW water level was 1.4 m), so confirm their reference datum in the CO-OPS metadata docs during capture and convert with the station datums before comparing. Compute `floodCategory` (`none|action|minor|moderate|major`) from the NOS thresholds (fallback NWS).
- Record `kind = tide-gauge`, `externalId = station id`, point from metadata; payload `name, waterLevelM, datum` (`MLLW`), `sigmaM, quality, nextHighTime, nextHighM, nextLowTime, nextLowM, floodCategory, minorFloodM, moderateFloodM, majorFloodM` (converted to MLLW metres). `observedAt = eventTime = t` (GMT). TTL 1 h.
- Interval 6 min (CO-OPS 6-minute data; min 6 min), budget 20/min, 6000/day.

### 6. Optional sources (implement, ship disabled)

- **`ea-flood-warnings`** (England): `https://environment.data.gov.uk/flood-monitoring/id/floods` (verified 2026-10: JSON-LD `items[]`, Last-Modified; licence OGL v3 stated in `meta.licence`). Record `kind = flood-warning`, `externalId = floodAreaID`, payload `severity` (text), `severityLevel` (1 severe … 4 no longer in force), `description, eaAreaName, message` (≤ 4 KB, `embedFields`, untrusted), `isTidal`; `observedAt = timeMessageChanged`, `eventTime = timeRaised`; level 4 → tombstone. Polygon from `floodArea.polygon` (a separate GeoJSON URL per area, fetched once and cached 30 d; ≤ 30 new areas per run). Interval 15 min. Attribution "This uses Environment Agency flood and river level data from the real-time data API (Beta)".
- **`eccc-hydrometric`** (Canada) as a prompt 073 feature spec: OGC API `https://api.weather.gc.ca/collections/hydrometric-realtime/items` (verified: properties `STATION_NUMBER, STATION_NAME, PROV_TERR_STATE_LOC, DATETIME, LEVEL, DISCHARGE`, symbol fields). The collection holds many rows per station: query with `bbox` (required), `datetime={now-2h}/..`, `sortby=-DATETIME`, keep the newest row per `STATION_NUMBER` (add a `latestPerKey` option to the feature spec mapper). `kind = water-observation`. Licence Open Government Licence – Canada.

### 7. Fixtures, docs

- Real captures for every endpoint (USGS two pages with a `next` link; NWPS list and one detail; AWDB stations and data; USDM trimmed to the D3/D4 features plus headers; CO-OPS water level, predictions, floodlevels, datums; EA floods and one polygon; ECCC two pages). README with dates and URLs. Edge cases: NWPS sentinels, USGS non-numeric `value` (`Ice`, `Eqp`) → omit value and set `qualifier`, CO-OPS error object `{"error":{"message":…}}` → `UpstreamFormatException`.
- `docs/Source-Catalog.md`: rows for all sources (required params, key notes, the USDM attribution text, the NWPS threshold refresh cost, legacy NWIS retirement).
- `docs/Operator-Guide.md`: a "Flood watching" recipe (NWPS category trigger, CO-OPS flood category trigger, place-scoped bbox).
- No API change.

## Tests to write (do not run)

- `UsgsLatestContinuousParserTest`, `NwpsGaugeMapperTest` (sentinels, threshold join, `stageToMinor`), `AwdbParserTest` (unit conversions, local-date → UTC), `UsdmMapperTest` (map-date computation, simplification flags, missing category → tombstone), `CoopsMapperTest` (string numbers, hilo selection, datum conversion, flood category), `EaFloodParserTest`, ECCC latest-per-key: real fixtures, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- Adapter tests (WireMock): USGS cursor paging and `X-Api-Key` redaction; NWPS detail calls at most once per gauge per day (`MutableClock`); USDM 304 path and the HEAD confirmation; CO-OPS `application` param always present.
- `HydroIngestIT` (`@IntegrationTest`): an NWPS category change creates a version with `floodCategory` updated; an unchanged USDM week is a 304 with no versions.

## Acceptance criteria

- `usgs-water`, `nwps-gauges`, `snotel`, `usdm`, `noaa-coops-tides`, `ea-flood-warnings` and `eccc-hydrometric` register, disabled, with required params enforced.
- No adapter calls `waterservices.usgs.gov`.
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
2. **Commit** all changes with the message `079: Hydrology And Coast`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/079-CR-Hydrology-And-Coast.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `079: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
