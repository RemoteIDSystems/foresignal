# 087 — Aviation Safety

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/087-CR-Aviation-Safety.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/086-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `faa-tfr`, `faa-nas-status`, `awc-sigmets` (Velocity survey) and `faa-nas-status` (World Monitor survey)
- `docs/Core-Data-Model.md` §2 (catalogue and NWS-style rows), §3 (descriptor, `validUntil` only when upstream-asserted, limits: 10,000 vertices), §5.3 ("Everything else")
- `docs/Gap-Analysis.md` §4b row "FAA UAS maps, AWC PIREP/G-AIRMET/METAR, NOTAMs", §8.C item 46 ("Extend 049" = this prompt)
- Completion Reports of 008 (`validFrom`, `startField`/`endField`, `titleField`), 047 (`awc-metars`), 073 (ArcGIS FeatureServer kit)
- Upstream: https://aviationweather.gov/data/api/, https://tfr.faa.gov, https://nasstatus.faa.gov, https://udds-faa.opendata.arcgis.com, https://www.faa.gov/about/initiatives/notam/faqs

## Objective

Add the US aviation-safety picture: temporary flight restrictions with real polygons, airport delay programmes, SIGMETs and AIRMETs, PIREPs, CWAs and TAFs, and the FAA's drone airspace layers. All are public-domain US Government data; the work is in faithful geometry, correct validity windows and staying inside AWC's published rate limit.

## Tasks

### 1. `faa-tfr` (POLLING)

- List: `GET https://tfr.faa.gov/tfrapi/exportTfrList`. **Verified 2026-10:** a JSON array of `{notam_id: "6/8784", type, facility, state, description, creation_date}`, served `Cache-Control: no-cache` (no validators).
- Detail: `GET https://tfr.faa.gov/download/detail_{notam_id with "/" → "_"}.xml` (XNOTAM-Update). Fetch details **only** for ids that are new or whose list row hash changed; keep `id → rowHash` in adapter state; at most 40 detail fetches per run, serial.
- **`XnotamParser`** (StAX, DTDs and external entities disabled):
  - Areas: each `abdMergedArea` (fallback `Abd`) → polygon from `Avx` points (`codeType` `GRC`/`RHL`, `geoLat` like `39.00838055N`, `geoLong` like `105.41666667W`, `codeDatum` `WGE`). Circle and arc vertices (`codeType` `CWA`/`CCA` with `geoLatArc`/`geoLongArc`, `valRadiusArc`, `uomRadiusArc` NM/KM) are densified geodesically (≤ 2° steps); a lone circle becomes a 64-vertex polygon. Several areas → MultiPolygon. Close rings and run `GeometryFixer` through the normal path.
  - Vertical limits: `valDistVerLower/Upper`, `uomDistVerLower/Upper`, `codeDistVerLower/Upper` (SFC/ALT/HEI) → payload `lowerFt`, `upperFt`, `lowerRef`, `upperRef`.
  - Times: `dateEffective`, `dateExpire` (UTC unless `codeTimeZone` says otherwise; convert), `dateIssued`.
  - Text: `txtDescrTraditional` (fallback `txtDescrModern`), `codeType` (e.g. `91.137(a)(1)`), `txtNameCity`, `txtNameUSState`.
- Record: kind `airspace-restriction`, externalId `notam_id`, `observedAt` = `dateIssued`, `validFrom` = `dateEffective`, `validUntil` = `dateExpire` (upstream-asserted; null when "until further notice"). Payload `notamId, tfrType, facility, state, city, regulation, lowerFt, upperFt, lowerRef, upperRef, description`. titleField `description`, tileProperties `tfrType, lowerFt, upperFt`, searchFields `description, notamId, city`, `embedFields` `description` (untrusted text).
- `complete=true`; ids that leave the list get an explicit tombstone (adapter keeps the last id set). Interval 15 min, budget 45/min, 2,000/day.

### 2. `faa-nas-status` (POLLING)

- `GET https://nasstatus.faa.gov/api/airport-status-information`. **Verified 2026-10:** XML (`AIRPORT_STATUS_INFORMATION`, `Update_Time` like `Sat Oct 10 23:14:47 2026 GMT`, `Delay_type` blocks named Ground Delay Programs, Ground Stops, General Arrival/Departure Delays, Airport Closures, Deicing). The catalogue's "REST JSON" is wrong; correct the row.
- Parse each `Delay_type` (`Ground_Delay {ARPT, Reason, Avg, Max}`, `Program {ARPT, Reason, End_Time}`, `Delay {ARPT, Reason, Arrival_Departure {Type, Min, Max, Trend}}`, `Airport {ARPT, Reason, Start, Reopen}`). Durations like "2 hours and 48 minutes" → minutes.
- Geometry: bundle `src/main/resources/reference/us-airports.csv` (FAA LID, ICAO, name, lat, lon; generated once by `scripts/build-us-airports.py` from OurAirports public-domain data; commit both). Unknown ARPT → null geometry, counted.
- Record: kind `airport-delay`, externalId `ARPT|delayKind`, `observedAt` = `Update_Time`, payload `airport, delayKind, reason, avgMin, maxMin, minMin, trend, endTime, reopen`. `complete=true` with tombstones for cleared programmes. Interval 5 min, budget 2/min, 600/day.

### 3. AWC products (POLLING, shared `AwcSupport` in `adapters.awc`)

AWC limits all clients to **100 requests/min**, asks for no more than 1 request/min per endpoint per thread, and asks for a custom User-Agent (verified 2026-10). Every AWC source has `minInterval` ≥ 2 min and is serial; a 429 honours `Retry-After` through the standard backoff.

| Source | Request | Kind | externalId | Validity |
|---|---|---|---|---|
| `awc-sigmets` | `api/data/airsigmet?format=geojson` and `api/data/isigmet?format=geojson` (two requests) | `aviation-hazard` | `icaoId:seriesId:validTimeFrom` (intl: `firId:seriesId:validTimeFrom`) | `validFrom`/`validUntil` = `validTimeFrom`/`validTimeTo` |
| `awc-gairmets` | `api/data/gairmet?format=geojson` | `aviation-hazard` | `product:tag:hazard:validTime:forecast` | `validFrom` = `validTime`; `recordTtl` 3 h |
| `awc-cwas` | `api/data/cwa?format=geojson` | `aviation-hazard` | `cwsu:seriesId:validTimeFrom` | from/to |
| `awc-pireps` | `api/data/pirep?format=geojson&age=2&bbox={bbox}` (bbox is mandatory upstream; param default CONUS `-130,20,-60,55`) | `pirep` | `receiptTime:lat:lon:sha1(rawOb)[0..8]` | `recordTtl` 2 h |
| `awc-tafs` | see below | `taf` | station id | `validFrom`/`validUntil` = TAF valid times |

- Hazard payload: `product` (`SIGMET`, `AIRMET`, `ISIGMET`, `GAIRMET`, `CWA`), `hazard, severity, qualifier, baseFt, topFt, movementDir, movementKt, raw`. PIREP payload: `fltLvl, acType, turbulence, icing, skyCover, wx, raw`, `eventTime = obsTime`. Raw text is untrusted.
- **METAR/TAF reuse:** 047 added `awc-metars`. Do not add a second METAR source. Put the station-list and cache-file logic in `AwcSupport` (move it from 047 if needed, keeping 047's behaviour and tests green) and build `awc-tafs` on it: `api/data/taf?ids={stations}&format=json` for ≤ 400 stations per request, or the AWC TAF cache file for bulk (confirm its current name under `/data/cache/` at fixture capture). Interval 10 min.
- Budgets: sigmets 3/min, 1,500/day (5 min); gairmets and cwas 1/min, 500/day (10 min); pireps 1/min, 500/day (5 min); tafs 2/min, 300/day.

### 4. `faa-uasfm` (POLLING, on 073's `ArcGisQueryClient`)

- Layer `https://services6.arcgis.com/ssFJjBXIUyZDrSYZ/ArcGIS/rest/services/FAA_UAS_FacilityMap_Data/FeatureServer/0` (**verified 2026-10**: polygons, maxRecordCount 2000, fields `CEILING, UNIT, MAP_EFF, LAST_EDIT, ARPT_COUNT, APT1_FAAID…APT5_*, APT*_LAANC, AIRS_COUNT, GLOBALID`). The org also publishes `_V5`, `_Primary` and `_Secondary` variants: confirm which is the live publication during fixture capture and record it.
- A small hand-written adapter on 073's `ArcGisQueryClient` (`outSR=4326`, `f=geojson`, offset paging, the `editingInfo.lastEditDate` short-circuit). It is not a kit spec because it needs an operator bbox and incremental queries. The grid is national and large, so param `bbox` is **required** (no default) and `maxFeatures` caps at 50,000. Incremental: `where=LAST_EDIT > {lastMax}` after the first full load; a full reload every 56 days (the FAA chart cycle) with tombstones for missing GLOBALIDs.
- Record kind `uas-facility-cell`, externalId `GLOBALID`, payload `ceilingFt, airports` (list of FAA ids), `laanc, mapEffective`. `observedAtMode` UPSTREAM from `LAST_EDIT`. Interval 7 d, budget 20/min, 400/day.

### 5. `faa-uas-*` restrictions (073 feature-kit specs, no adapter code)

- Add `src/main/resources/kits/features/faa-uas.json`: one 073 `FeatureSourceSpec` per layer (`protocol: arcgis`, `outFields` allowlist, `absenceTombstones: true`, licence `US-PD`, interval 24 h, budget 10/min, 100/day, disabled). **Verified 2026-10** service names under `services6.arcgis.com/ssFJjBXIUyZDrSYZ`: `DoD_Mar_13` (National Security UAS Flight Restrictions; fields `Proponent, Branch, Base, Facility, Airspace, Reason, State, FAA_ID, Floor, Ceiling, County`), `Part_Time_National_Security_UAS_Flight_Restrictions`, `Recreational_Flyer_Fixed_Sites`, `Stadiums`, `Prohibited_Areas`, `National_Defense_Airspace_TFR_Areas`. Sources: `faa-uas-nsufr`, `faa-uas-nsufr-parttime`, `faa-uas-recreational-sites`, `faa-uas-stadiums`, `faa-uas-prohibited`, `faa-uas-defense-tfr`.
- Kind `uas-restriction` (recreational sites: `uas-site`), `idField` = the layer's `GlobalID` if present, else `OBJECTID` (record which is stable across publications), fields mapped to `name, facility, reason, state, floorFt, ceilingFt`. The restriction type is carried by the source id; if 073's spec format supports constant fields, also add `restrictionType`. `observedAt: RECEIVED`.

### 6. NOTAMs (documentation only)

The FAA moved NOTAM distribution to the NOTAM Management Service (cutover reported for 18 April 2026). API access requires approval by e-mail to `notams@faa.gov`, and its endpoints are not publicly documented. Do not build an adapter. Add a "planned" row to `docs/Source-Catalog.md` with these facts and an Operator Guide note.

### 7. Fixtures

Capture with `curl -A "Foresignal/<ver> (+https://foresignal.ai)"`: the TFR list and three details (polygon, circle, multi-area), one NAS status document with every `Delay_type`, one response per AWC endpoint, one page from `faa-uasfm` and each restriction layer. Hand-make: an XNOTAM with a DOCTYPE (must throw), a TFR without expiry, a G-AIRMET with a `forecast` > 0.

### 8. Spec, Postman, docs

- No new operations. Add kinds `airspace-restriction, airport-delay, aviation-hazard, pirep, taf, uas-facility-cell, uas-restriction, uas-site` where kinds are enumerated.
- `docs/Source-Catalog.md`: correct the three existing rows, add the new ones and the AWC rate-limit note. `docs/Operator-Guide.md`: AWC limits and choosing a PIREP bbox.

## Tests to write (do not run)

- `XnotamParserTest`: polygon, circle densification, multi-area MultiPolygon, coordinate formats, vertical limits, time zones, DOCTYPE rejected.
- `FaaTfrAdapterTest` (WireMock): details fetched only for new or changed rows, 40-fetch cap, tombstone on removal, detail URL underscore form.
- `NasStatusParserTest`: every delay type, duration parsing, unknown airport → null geometry.
- `AwcParsersTest`: each GeoJSON product, PIREP id stability, TAF validity; `AwcAdapterTest` (WireMock): UA present, bbox required for PIREPs, 429 backoff.
- `UasfmAdapterTest`: paging through `ArcGisQueryClient`, incremental `LAST_EDIT`, 56-day full reload with tombstones, bbox required. `FaaUasKitSpecTest`: the spec file loads through 073's family, ids are valid, field maps cover the fixtures.
- `AviationIngestIT`: TFR with `validFrom` in the future is not active until it starts (008 semantics); expiry drops it from current reads.

## Acceptance criteria

- `faa-tfr` records carry true XNOTAM geometry and validity windows.
- No second METAR source exists; `awc-tafs` shares 047's AWC support code.
- Every AWC source respects the 100 requests/min ceiling with its budget.
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
2. **Commit** all changes with the message `087: Aviation Safety`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/087-CR-Aviation-Safety.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `087: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
