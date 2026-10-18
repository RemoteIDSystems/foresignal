# 075 — Fire Perimeters Radio Cyclone Cones

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/075-CR-Fire-Perimeters-Radio-Cyclone-Cones.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/074-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `nifc-perimeters`, `radio-browser`, `nhc-cyclone-cones`, `nhc-cyclones`
- `docs/design-history/Gap-Analysis.md` §4b row "Smoke (HMS) + Canada fire (CWFIS) + InciWeb" ("Hotspot layers duplicate FIRMS"), §8.C item 36
- `docs/design-history/Core-Data-Model.md` §2, §3 (limits: 10,000 vertices), §5.3
- Prompt 073 kits (`ArcGisQueryClient`, `WfsJsonClient`, `FeatureGeometry`, `FeedParser`, feed `itemMapper` hook) and prompt 072 `AbsenceTombstoner`
- Prompt 067 `nhc-cyclones` (its `externalId` format) and prompt 048 (HMS smoke and fire: **already implemented there, do not duplicate**)
- GEV reference (MIT, portable): `server/providers/firePerimeters.js`, `src/layers/perimeters/records.js`, `server/providers/radio/{catalog,constants,stations}.js`, `src/sources/radioBrowser.js`, `server/providers/cyclones.js`
- Upstream docs: NIFC Open Data (WFIGS), InciWeb, CWFIS GeoServer (`cwfis.cfs.nrcan.gc.ca`), Radio Browser API (`api.radio-browser.info`), NOAA `mapservices.weather.noaa.gov/tropical` (NHC GIS)

## Objective

Add the remaining fire, broadcast-radio and tropical-cyclone geometry sources on top of the prompt 073 kits: US wildfire perimeters and incident summaries, Canadian fire perimeters and fire-weather indices, the Radio Browser station directory with mirror discovery, and NHC forecast cones, tracks, points and watches/warnings. HMS smoke and fire stay in prompt 048.

## Tasks

### 1. `nifc-perimeters` (ArcGIS kit, custom mapper)

- Layer `https://services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/WFIGS_Interagency_Perimeters_Current/FeatureServer/0`. Verified 2026-10: `maxRecordCount` 2000, `editingInfo.lastEditDate` present (use the kit's not-modified short-circuit).
- Query as GEV: `where=1=1`, `outFields=poly_IncidentName,poly_GISAcres,poly_DateCurrent,attr_UniqueFireIdentifier,attr_IrwinID,attr_IncidentSize,attr_PercentContained,attr_POOState,attr_POOCounty,attr_IncidentTypeCategory,attr_FireDiscoveryDateTime,attr_FireCause,attr_FireBehaviorGeneral,attr_TotalIncidentPersonnel,attr_IncidentComplexityLevel,attr_CpxName`, `maxAllowableOffset=0.001`, `outSR=4326`, `f=geojson`, ≤ 5 pages.
- Record: `kind = fire-perimeter`; `externalId = attr_UniqueFireIdentifier` (fallback `attr_IrwinID`; skip if both missing); polygon geometry (kit simplification above 9,500 vertices); `observedAt = poly_DateCurrent`; `eventTime = attr_FireDiscoveryDateTime`.
- Payload (renamed, nulls omitted): `name, gisAcres` (rounded to 1 acre), `incidentSizeAcres, percentContained, state, county, incidentType, cause, behavior, personnel` (declare `personnel` volatile; it changes every shift without changing the fire), `complexityLevel, complexName, irwinId`.
- Descriptor: interval 5 min (min 5 min), budget 2/min, 600/day, STANDARD, ALL, `ttlAnchor=RECEIVED`, no TTL, absence tombstones on, `zeroResultsNormal=true` (winter), `titleField=name`, `rankField=gisAcres`, tile `name, gisAcres, percentContained`. Licence: US public domain (NIFC Open Data).

### 2. `inciweb-incidents` (feed kit + item mapper)

- `https://inciweb.wildfire.gov/incidents/rss.xml` (verified 2026-10: RSS 2.0, 50 items). GEV's two other routes (the undocumented `POST /api/single-publication/` index and HTML scraping of incident pages) are not ported; list them under "External provider ideas".
- `InciwebItemMapper` (pure) parses the description text:
  - `Last updated: YYYY-MM-DD`, `State: …`, incident type ("The type of incident is …"), units, and the `Incident Overview:` paragraph (plain text, ≤ 2 KB).
  - Coordinates appear as `Latitude: 39° 32 21  Longitude: 119° 59 16` (degree sign, then minutes and seconds without symbols, no hemisphere). Parse D M S; InciWeb covers US units, so latitude is north and longitude west (negate). Accept only results inside the US envelope (incl. Alaska, Hawaii, Puerto Rico, Guam); otherwise null geometry and a `geometryRejected` counter.
  - The title carries the unit prefix (`NVTMFX Bull Fire`): split into `unitCode` and `name`.
- Record: `kind = fire-incident`, `externalId = guid`, `eventTime = pubDate`, `observedAtMode=RECEIVED` (the only update stamp is a date without time, so it cannot be the fixed time base), payload `name, unitCode, incidentType, state, lastUpdated` (date string), `overview, link`; `embedFields=name, overview` (untrusted text, prompt 028 rules); `recordTtl` 14 d, `ttlAnchor=RECEIVED`; no tombstones. Interval 30 min, budget 1/min, 100/day. Public domain.

### 3. `cwfis-*` family (WFS kit)

Base `https://cwfis.cfs.nrcan.gc.ca/geoserver/public/wfs`, `outputFormat=application/json`, `count=1000`, `startIndex` paging. Verified 2026-10: GeoJSON coordinates are lon,lat although `crs` reads `urn:ogc:def:crs:EPSG::4326` (kit `axisOrder=lonlat`); the global capabilities list `public:m3_polygons_current`, `public:firewx_stns_current`, `public:hotspots_last24hrs`, `public:fdr_current_shp` among others. Licence: Open Government Licence – Canada; attribution "Natural Resources Canada, Canadian Wildland Fire Information System". All disabled by default.

| Source id | typeName | Kind | Mapping |
|---|---|---|---|
| `cwfis-perimeters` | `public:m3_polygons_current` (736 features on capture day) | `fire-perimeter` | Hotspot-derived burned-area polygons, **not** agency perimeters (say so in `description`). Payload `hotspotCount` (`hcount`), `firstDetected`, `lastDetected`, `areaHa` (`area`), `perimeterSource=m3`. `observedAt = lastdate`, `eventTime = firstdate`. Feature ids look like `m3_polygons_current.226354`: during fixture capture fetch twice a day apart; if ids are not stable, use `externalId = firstdate + "@" + round(centroid, 2)`. Interval 3 h, absence tombstones on. |
| `cwfis-fwi-stations` | `public:firewx_stns_current` (2,110 stations) | `fire-weather` | Point per station; `externalId = aes` (fallback `wmo`); trim the space-padded `name`/`agency`; payload `name, province, agency, elevationM, tempC, rhPct, windKph, gustKph, windDirDeg, precipMm, ffmc, dmc, dc, isi, bui, fwi, dsr`; `observedAt = rep_date`, `eventTime = rep_date` (measurement); TTL 36 h, `ttlAnchor=OBSERVED`; `fieldSpecs` with UCUM units. Interval 3 h. |
| `cwfis-hotspots` | `public:hotspots_last24hrs` | `fire-detection` | Duplicates FIRMS detections; kept for its Fire Behaviour Prediction fields: `hfi, ros, fuel, cfb, sfc, tfc, frp, sensor, satellite, agency`. `externalId = rep_date + lat/lon (5 dp) + satellite`; `observedAt = eventTime = rep_date`; TTL 24 h OBSERVED. Interval 1 h. State in the description that FIRMS (prompt 065) is the primary hotspot source. |

`public:fdr_current_shp` (fire-danger class polygons carrying only `GRIDCODE`) is raster-like: not ingested; note it as a candidate for prompt 106.

### 4. `radio-browser`

- Mirror discovery: `GET https://all.api.radio-browser.info/json/servers` → `[{ip, name}]`; keep names matching `^[a-z0-9-]+\.api\.radio-browser\.info$` (reject everything else; this is the SSRF guard), de-duplicate, shuffle, cache 6 h in `AdapterStateStore`. Fallback list when discovery fails: `de1`, `de2`, `nl1` (from GEV constants). Verified 2026-10: discovery returned only `de1.api.radio-browser.info` (IPv4 + IPv6). On a mirror failure (IO, 5xx) try the next mirror within the same fetch, counting each attempt against the budget.
- Crawl: `/json/stations/search?has_geo_info=true&hidebroken=true&order=stationuuid&limit=1000&offset=N` until a short page or `maxStations` (param, default 40,000). Send Foresignal's UA (the API asks for a descriptive one).
- Record: `kind = radio-station`, `externalId = stationuuid`, point from `geo_lat`/`geo_long` (reject 0,0 per the null-island rule), `observedAt = lastchangetime_iso8601`. Payload: `name` (trim tabs/whitespace), `streamUrl` (`url_resolved`), `homepage, favicon, tags` (split, ≤ 15), `countryCode, state, languages, codec, bitrate, hls` (bool), `online` (`lastcheckok == 1`). **Leave out** `votes, clickcount, clicktrend, clicktimestamp*, lastchecktime*, lastlocalchecktime*, changeuuid` (raw counters and churn, adapter rule 8).
- Descriptor: interval 6 h (min 6 h), budget 2/min, 200/day, STANDARD, ALL, no TTL, absence tombstones on (only after an untruncated crawl), `titleField=name`, `searchFields=name,tags,state`, tile `name, codec, online`. Stream URLs are links for clients; Foresignal never fetches a stream. Licence: Radio Browser data is published as free/open (record the exact statement from `api.radio-browser.info` during capture).
- Port notes: GEV header and `THIRD_PARTY_NOTICES.md` entry for the radio files used.

### 5. `nhc-cyclone-cones`

- Service `https://mapservices.weather.noaa.gov/tropical/rest/services/tropical/NHC_tropical_weather_summary/MapServer` (all active storms in one service). Verified 2026-10 layer ids: 5 Forecast Points, 6 Forecast Track, 7 Forecast Cone, 8 Watch-Warning; `maxRecordCount` 2000; no `editingInfo`. Resolve ids **by layer name** from `?f=json` once per 24 h (state cache) and fail with `UpstreamFormatException` if a name is missing. (The per-slot service `NHC_tropical_weather` repeats these per AT1–AT5/EP1–EP5/CP slot with a stride of 26 ids; not used.)
- Query each layer with `where=1=1&outFields=*&returnGeometry=true&outSR=4326&f=geojson` via `ArcGisQueryClient`.
- Kinds and ids (`stormId` = lower-case basin + two-digit `stormnum` + year, e.g. `al092026`; **match prompt 067's `nhc-cyclones` externalId format**, read its parser and adapt):
  - `cyclone-cone` (`externalId = stormId`), `cyclone-track` (`stormId`), `cyclone-warning` (`stormId:<tcww type>:<n>`), `cyclone-forecast-point` (`stormId:tau<tau>`).
- Payload allowlist: common `stormId, stormName, stormType, basin, advisoryNumber` (`advisnum`), `advisoryDate` (`advdate` text), `forecastPeriod`; points add `tau, validTime, maxWindKt, gustKt, mslpHpa, development` (`tcdvlp`), `label` (`dvlbl`), `motionDirDeg` (`tcdir`), `motionSpeedKt` (`tcspd`), `saffirSimpson` (`ssnum`). Drop `objectid, idp_*, st_area*, st_perimeter*, binnumber, shape`.
- Time: `observedAt = idp_filedate` (epoch ms, the advisory product time; fixed for every kind), `eventTime = validtime` for points only. A new advisory changes the content and yields new versions of the same ids.
- Descriptor: interval 10 min (min 5 min), budget 4/min, 1200/day, STANDARD, ALL, no TTL, absence tombstones on (dissipated storms disappear), `zeroResultsNormal=true`, `titleField=stormName`. Coverage note: Atlantic, eastern and central Pacific only. Public domain.

### 6. Fixtures and docs

- Real captures per source (trim to ≤ 25 features; keep one large perimeter to exercise simplification), README with dates and URLs; hand-made edge cases: InciWeb description without coordinates, CWFIS swapped-axis sample, radio station with tab-prefixed name and 0,0 location, NHC layer list missing a name.
- `docs/Source-Catalog.md`: rows for all sources above (tier, policy, TTL, time base, licence), the InciWeb scraping exclusion, the CWFIS hotspot/FIRMS overlap note, and "HMS lives in prompt 048".
- `THIRD_PARTY_NOTICES.md` entries for the GEV files ported. No API change.

## Tests to write (do not run)

- `NifcPerimeterMapperTest`, `InciwebItemMapperTest` (DMS without symbols, US envelope, title split), `CwfisMappersTest` (padded strings, id-stability fallback), `RadioBrowserParserTest` (excluded fields never present, null island), `NhcConeMapperTest` (stormId format equals prompt 067's, dropped fields): real fixtures, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- `RadioMirrorDiscoveryTest` (WireMock): host-pattern rejection, shuffle and cache, failover to the next mirror, fallback list when discovery fails.
- `NhcLayerResolverTest`: name → id resolution, missing name throws.
- Adapter tests (WireMock): NIFC paging and the `lastEditDate` short-circuit; CWFIS paging; InciWeb conditional GET.
- `FireAndCycloneIngestIT` (`@IntegrationTest`): perimeters ingest and tombstone after two misses; an NHC advisory change creates version 2 of the cone; out-of-season empty result keeps health `healthy`.

## Acceptance criteria

- All listed sources register, disabled, with licences; no HMS source is added here.
- No HTML page is fetched by any adapter in this prompt.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `075: Fire Perimeters Radio Cyclone Cones`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/075-CR-Fire-Perimeters-Radio-Cyclone-Cones.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `075: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
