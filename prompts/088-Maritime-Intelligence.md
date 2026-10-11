# 088 — Maritime Intelligence

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/088-CR-Maritime-Intelligence.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/087-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `nga-msi-warnings` (Velocity survey), `imf-portwatch-chokepoints` (World Monitor survey), `gfw-events`
- `docs/Core-Data-Model.md` §2 (catalogue and measurement rows), §3 (descriptor, `embedFields`, limits), §5.3 ("Everything else")
- `docs/Gap-Analysis.md` §8.C item 47 ("Extend 050 with `imf-portwatch-ports`" = this prompt)
- Completion Reports of 008 (`startField`/`endField`, `titleField`), 073 (ArcGIS FeatureServer kit). Licence enforcement arrives in 109, so set each descriptor's `licence` exactly now.
- Upstream: https://msi.nga.mil/api, https://portwatch.imf.org/pages/faqs, https://globalfishingwatch.org/our-apis/documentation, https://www.navcen.uscg.gov/msi

## Objective

Add maritime safety and trade intelligence: NGA navigational warnings with geometry recovered from free text, IMF PortWatch chokepoint and port activity with disruptions, Global Fishing Watch events, and US Coast Guard Local Notices to Mariners if a documented machine-readable feed exists. These feed prompt 099's chokepoint and port-call layers.

## Tasks

### 1. `nga-msi-warnings` (POLLING)

- `GET https://msi.nga.mil/api/publications/broadcast-warn?status=active&output=json`. **Verified 2026-10:** our identifiable User-Agent receives HTTP 200 (about 206 KB) without a browser UA or Referer. Never add either; if a block appears later, the source goes `failing` and the catalogue row says so.
- Response `{"broadcast-warn":[{msgYear, msgNumber, navArea, subregion, text, status, issueDate, authority, cancelDate, cancelNavArea, cancelMsgYear, cancelMsgNumber, year}]}`.
- **`MsiCoordinateExtractor`** (pure): regex for `DD-MM.mmN DDD-MM.mmW` and `DD-MM-SS.sN DDD-MM-SS.sW` (also without the space, and `N`/`S`/`E`/`W` validation; degrees ≤ 90/180, minutes and seconds < 60). Geometry rules, in order:
  1. text contains `BOUNDED BY` / `AREA BOUNDED` and ≥ 3 following points → Polygon (closed);
  2. `WITHIN <n> (MILES|NM|METERS) OF <point>` → geodesic circle (64 vertices);
  3. `TRACKLINE` / `BETWEEN … AND …` with ≥ 2 points → LineString;
  4. otherwise every point → MultiPoint (single point → Point);
  5. no coordinates (about 4 % per the catalogue) → null geometry, flagged in payload `geometryFromText=false`.
- Record: kind `maritime-warning`, externalId `navArea/msgYear/msgNumber` (e.g. `4/2024/517`), `observedAt` = `issueDate` (`DDHHMMZ MON YYYY`, UTC), payload `navArea, subregion, msgYear, msgNumber, authority, text` (≤ 8 KB), `cancels` (when the text cancels another warning). titleField: first line of `text` (≤ 120 chars), `embedFields` `text` (untrusted upstream text), searchFields `text, authority`.
- `complete=true`; warnings no longer active get explicit tombstones. Interval 15 min, conditional GET if validators are returned, budget 2/min, 200/day. Licence: US Government public domain.

### 2. IMF PortWatch (POLLING, on 073's `ArcGisQueryClient`)

**Verified 2026-10** under `https://services9.arcgis.com/weJ1QsnbMYJlCHdG/ArcGIS/rest/services/`: `PortWatch_chokepoints_database` (points), `Daily_Chokepoints_Data` (table: `date, year, month, day, portid, portname, n_container, n_dry_bulk, n_general_cargo, n_roro, n_tanker, n_cargo, n_total, capacity_*`, `capacity`), `PortWatch_ports_database` (points: `portid, portname, country, ISO3, continent, fullname, lat, lon, vessel_count_*, industry_top1..3, share_country_maritime_import/export, LOCODE`), `Daily_Ports_Data` (table: `date, portid, portname, country, ISO3, portcalls_*`, `portcalls`, `import_*`, `import`, `export_*`, `export`), `portwatch_disruptions_database` (polygons: `eventid, eventtype, eventname, alertlevel, country, fromdate, todate, severitytext, affectedports, n_affectedports, affectedpopulation`). maxRecordCount 1,000 (disruptions 2,000). Daily tables were last edited on a Tuesday: the data updates **weekly** with a lag.

- **Change check:** use 073's `editingInfo.lastEditDate` short-circuit (verified present on these layers as `dataLastEditDate`/`lastEditDate`): unchanged → `FetchResult.unchanged()`.
- The two daily tables are non-spatial and must be joined to the point layers, so chokepoints and ports are hand-written adapters on `ArcGisQueryClient`; disruptions are a plain 073 feature-kit spec.
- **`imf-portwatch-chokepoints`**: load the chokepoint points once per run (cached in state), then query `Daily_Chokepoints_Data` with `where=date > DATE '{lastDate}'` (first run: `backfillDays`, default 30), ordered by date, paged. One record per chokepoint-day: kind `chokepoint-transit`, externalId `portid:YYYY-MM-DD`, `eventTime = observedAt = date` (measurement period), geometry = chokepoint point, payload `chokepoint, nTotal, nTanker, nContainer, nDryBulk, nGeneralCargo, nRoro, nCargo, capacityTotal, capacityTanker` (deadweight tonnes), `rankField` `nTotal`.
- **`imf-portwatch-ports`**: ports reference records (kind `port`, externalId `portid`, Point from `lat`/`lon`, payload `name, country, iso3, locode, vesselCountTotal, industries`, `observedAtMode` RECEIVED, refreshed when the layer edit date changes) plus daily activity (kind `port-activity`, externalId `portid:YYYY-MM-DD`, payload `portcalls, portcallsTanker, portcallsContainer, importTonnes, exportTonnes`). Param `ports` (string-list of `portid`, default empty = all, about 2,000 ports), `backfillDays` (default 14).
- **`imf-portwatch-disruptions`** (spec in `kits/features/imf-portwatch.json`, `absenceTombstones: true`): kind `port-disruption`, externalId `eventid`, polygon geometry, `validFrom` = `fromdate`, `validUntil` = `todate` when set, payload `eventType, name, alertLevel, country, severity, affectedPorts, nAffectedPorts, affectedPopulation`; drop `htmldescription`.
- All three: interval 12 h, budget 20/min, 200/day. Licence "IMF PortWatch terms (attribution: IMF PortWatch)"; verify the terms page at fixture capture and record its URL as `termsUrl`.

### 3. `gfw-events` (POLLING)

- Credential `apiToken` (required; GFW registration). `GET https://gateway.api.globalfishingwatch.org/v3/events?datasets[0]={dataset}&start-date={d}&end-date={d}&limit=500&offset={n}` with `Authorization: Bearer`. When param `bbox` is set use the POST form with a `region` GeoJSON body.
- Params: `datasets` (string-list, default `public-global-fishing-events:latest`; allowed adds encounters, loitering and port-visit datasets: confirm the exact current dataset ids from the API docs at fixture capture), `windowDays` (default 10; publication lag is several days), `bbox` (optional).
- Record: kind by event type (`fishing-event`, `vessel-encounter`, `loitering-event`, `port-visit`), externalId event `id`, `eventTime = start`, `observedAt = end` (fallback `start`), startField/endField `start`/`end`, Point from `position`, payload `vesselId, mmsi` (`ssvid`), `vesselName, flag, durationH, distanceKm`, encounter partner `encounterMmsi`, `portName` for visits. Paging via `nextOffset`; stop at `total`.
- Interval 1 h (minInterval 30 min), budget 10/min, 300/day (GFW's numeric limits are not published on the docs page we checked; stay low and honour 429 `Retry-After`). Licence **CC BY-NC 4.0** (non-commercial): put it in `licence` exactly so 109's egress policy can enforce it.

### 3a. `gfw-sar-detections` (optional, POLLING)

- Global Fishing Watch publishes vessel detections from Sentinel-1 SAR, including whether each detection matched an AIS-broadcasting vessel. Verify the current dataset id and access route (the v3 4Wings or detections endpoints) at fixture capture; if no stable official endpoint returns individual detections, record that in the Completion Report and the Source Catalog and do not implement it.
- Same credential, budget style and licence (**CC BY-NC 4.0**) as `gfw-events`. Record kind `sar-vessel-detection`, externalId the detection id, point, `eventTime` = acquisition time, payload `matched` (bool), `lengthM` (if provided), `confidence`, `aoi` param (bbox list) required. Analytic class `event`. Disabled by default.
- Used by 099's `derived-dark-vessel-match`.

### 4. USCG Local Notices to Mariners (verify first)

**Verified 2026-10:** NAVCEN relaunched LNM and the Light List as a geospatial application in late 2024; it offers GeoJSON downloads by district or area, refreshed every 15 minutes, but we found no published API documentation.

1. During implementation, open `https://www.navcen.uscg.gov/msi` and the LNM application and look for a **documented, stable** GeoJSON download URL per district.
2. If one exists, build family `uscg-lnm-*` (districts d1, d5, d7, d8, d9, d11, d13, d14, d17; reviewed static list): kind `notice-to-mariners`, externalId = the notice id field, geometry from the GeoJSON, `validFrom`/`validUntil` from the notice dates when present, `embedFields` the notice text; interval 1 h, budget 2/min, 100/day, conditional GET.
3. If the data is reachable only through the web application's undocumented internal calls, **do not build it**: add a "planned" catalogue row with the finding. Never parse PDFs or HTML pages.

### 5. Fixtures

Capture with an identifiable UA: one NGA active list trimmed to 30 warnings (keep examples of each geometry rule and one with no coordinates), one page of each PortWatch layer plus layer metadata, one GFW page per dataset (token from the operator's environment, never committed), and the LNM GeoJSON if step 4.2 applies. Hand-make NGA texts for the circle and trackline rules.

### 6. Spec, Postman, docs

- No new operations. Add kinds `maritime-warning, chokepoint-transit, port, port-activity, port-disruption, fishing-event, vessel-encounter, loitering-event, port-visit, notice-to-mariners` where enumerated.
- `docs/Source-Catalog.md`: update the three rows, add ports and disruptions, the weekly PortWatch cadence, the NGA UA finding, the GFW non-commercial licence and the LNM decision.

## Tests to write (do not run)

- `MsiCoordinateExtractorTest`: each format, invalid minutes rejected, each geometry rule, no-coordinate case.
- `NgaMsiParserTest` (real fixture: count, ids, `issueDate` parsing) and `NgaMsiAdapterTest` (WireMock: tombstone on removal, UA header, no Referer).
- `PortWatchAdapterTest` (WireMock ArcGIS: metadata short-circuit when unchanged, incremental `date >` query, paging, chokepoint join, disruption validity).
- `GfwEventsAdapterTest` (WireMock: Bearer header, dataset params, paging to `total`, POST region with bbox, 429 handling).
- `MaritimeIngestIT`: chokepoint daily records are idempotent on re-fetch; NGA cancelled warning tombstoned.

## Acceptance criteria

- NGA warnings carry geometry wherever the text holds coordinates, with no browser impersonation.
- PortWatch sources make no table queries when the layer edit date is unchanged.
- GFW records carry the CC BY-NC 4.0 licence.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 139, 142, 143, 144).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `088: Maritime Intelligence`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/088-CR-Maritime-Intelligence.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `088: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
