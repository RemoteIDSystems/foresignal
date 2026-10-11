# 074 — Road Events And Work Zones

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/074-CR-Road-Events-And-Work-Zones.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/073-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §4b row "Road events (Open511, WZDx, DATEX II)" ("Digitraffic JSON instead of DATEX; NDW v3") and §8.C item 35 (Gap label 039b = this prompt)
- `docs/Core-Data-Model.md` §2 (time contract), §3 (descriptor fields, adapter rules 7–13, limits), §5.3 ("Everything else" defaults)
- Prompt 008 descriptor extras (`startField`/`endField`, `validFrom`, `titleField`, `fieldSpecs`) and prompt 072's `AbsenceTombstoner`
- Prompt 073 kits (`FeatureGeometry` simplification, hardened StAX factory)
- Upstream docs: Open511 spec (`open511.org`), DriveBC Open511 API (`api.open511.gov.bc.ca`), WZDx spec v4.2 (`github.com/usdot-jpo-ode/wzdx`), USDOT WZDx Feed Registry (`data.transportation.gov` dataset `69qe-yiui`), Digitraffic road traffic messages (`www.digitraffic.fi/en/road-traffic/`), NDW open data (`opendata.ndw.nu`, `docs.ndw.nu`), DATEX II v3 Situation schema

## Objective

Ingest current road events (incidents, closures, roadworks, restrictions) from four open formats into one normalised `road-event` kind, so triggers, tiles and briefings treat a BC closure, a US work zone, a Finnish roadwork and a Dutch accident alike. Use Digitraffic's JSON API instead of DATEX II for Finland, and NDW's DATEX II v3 situation feed for the Netherlands.

## Tasks

### 1. Shared model (`ai.elaro.source.adapters.roads`)

- `RoadEventPayload` builder producing a slim, allowlisted payload (omit nulls):
  - `category`: `roadwork|incident|closure|restriction|weather|event|other` (mapping tables per format below)
  - `subtype` (upstream code, lower-kebab), `severity` (`unknown|minor|moderate|major|severe`), `headline` (≤ 200 chars), `description` (plain text, ≤ 4 KB), `roads` (≤ 10 names/numbers), `direction` (`both|northbound|…|unknown`), `status` (`active|planned`), `isClosure` (bool), `lanesAffected` (text), `speedLimitKph`, `startsAt`, `endsAt` (ISO strings, also `startField`/`endField`), `format` (`open511|wzdx|digitraffic|datex3`), `url`, `language`.
- Descriptor defaults for every source here: `kind = road-event`, `historyTier=STANDARD`, policy ALL, `observedAtMode=UPSTREAM` (each format has an update time, listed per source), `ttlAnchor=RECEIVED`, `recordTtl=null` (events end by tombstone), `significantFields` empty, `volatileFields` empty, `titleField=headline`, `searchFields=headline,description,roads`, `tileProperties=category,severity,isClosure,headline`, `embedFields=headline,description`, `startField=startsAt`, `endField=endsAt`. All ship **disabled**.
- Time mapping (Core-Data-Model §2): `observedAt` = the format's update/version time; `eventTime` = null (road events are periods, not instants); `validFrom` (prompt 008) = scheduled start when upstream gives one; `validUntil` = the upstream-asserted end of the **last** schedule interval only (never computed).
- Removal: each source fetches the full current set; use `AbsenceTombstoner` (two consecutive complete misses) and emit an immediate tombstone when the upstream marks an event ended (`ARCHIVED`, `endTime` in the past with `status=inactive`, etc.).
- Geometry: GeoJSON/GML lines and points as published; MultiLineString allowed; run `FeatureGeometry` simplification above 9,500 vertices.

### 2. `open511-*` family

- `Open511Family` with a reviewed list; ship `open511-drivebc`: `https://api.open511.gov.bc.ca/events?format=json&status=ACTIVE&limit=500`, follow `pagination.next_url` (max 10 pages). Verified 2026-10: fields `id` (`drivebc.ca/RIDE-102765`), `headline` (event type text), `status`, `created`, `updated`, `description`, `event_type` (`CONSTRUCTION|INCIDENT|SPECIAL_EVENT|WEATHER_CONDITION|ROAD_CONDITION`), `event_subtypes`, `severity` (`MINOR|MODERATE|MAJOR|UNKNOWN`), `geography` (GeoJSON), `roads[{name, from, to, direction, state}]`, `schedule.intervals` (`"start/end"`, open-ended allowed), plus `+ivr_message` / `+linear_reference_km` extensions (drop them).
- Mapping: `CONSTRUCTION → roadwork`, `INCIDENT → incident`, `SPECIAL_EVENT → event`, `WEATHER_CONDITION|ROAD_CONDITION → weather`; `isClosure` when any road `state` is `CLOSED`; `observedAt = updated`.
- Interval 5 min (min 2 min), budget 4/min, 1500/day. Licence: Open Government Licence – British Columbia. Search for other live Open511 servers during capture; add only ones that answer the standard `/events` route with published terms.

### 3. `wzdx-*` family

- Vendor a reviewed snapshot of the USDOT WZDx Feed Registry as `src/main/resources/roads/wzdx-feeds.json` (verified 2026-10: 43 registry rows; versions 3.1, 4, 4.1, 4.2 and "CWZ 1.0"). Include only rows with `active=true`, `format=geojson`, version 4.x, and a URL that **does not embed a token**; feeds with `needapikey=true` get a required credential `apiKey` placed as the registry documents (query or header, redacted). Record each row's `feedname`, `issuingorganization`, `state`, `datafeed_frequency_update`. Never read the registry at runtime (callers cannot add feeds).
- Source ids `wzdx-<feedname>` (e.g. `wzdx-mndot`, `wzdx-wsdot`). Interval = registry update frequency clamped to [5 min, 30 min]; budget 2/min, 600/day.
- Parser `WzdxParser` (v4.0–4.2): FeatureCollection with `feed_info` (v4.x; `road_event_feed_info` in 4.0); per feature `properties.core_details {event_type (work-zone|detour|restriction), data_source_id, road_names, direction, description, update_date, name}`, `start_date`, `end_date`, `is_start_date_verified`, `vehicle_impact` (`all-lanes-closed|some-lanes-closed|all-lanes-open|alternating-one-way|…`), `reduced_speed_limit_kph`, `lanes[]`, `worker_presence`, `restrictions[]`; geometry LineString or MultiPoint.
- Mapping: `work-zone → roadwork`, `detour → closure`, `restriction → restriction`; `isClosure` when `vehicle_impact = all-lanes-closed`; `observedAt = core_details.update_date`; feature id → `externalId`. Reject a 3.x or CWZ document with `UpstreamFormatException("unsupported WZDx version …")` so health shows it.
- Licence per feed (most are US public domain; record the registry's issuing organisation as attribution).

### 4. `digitraffic-road`

- `https://tie.digitraffic.fi/api/traffic-message/v1/messages?inactiveHours=0&includeAreaGeometry=false&situationType={type}`, one request per value of param `situationTypes` (string-list, allowed `TRAFFIC_ANNOUNCEMENT, ROAD_WORK, WEIGHT_RESTRICTION, EXEMPTED_TRANSPORT`, default `TRAFFIC_ANNOUNCEMENT, ROAD_WORK`). Headers: `Digitraffic-User: Elaro/<version>`, gzip. Conditional GET per type (`getConditional(uri, "msg:" + type)`); ETag + Last-Modified verified 2026-10.
- Verified 2026-10 shape: GeoJSON FeatureCollection with `dataUpdatedTime`; feature properties `situationId`, `situationType`, `trafficAnnouncementType`, `version`, `releaseTime`, `versionTime`, `announcements[{language, title, location{description}, locationDetails, features[{name, quantity, unit}], roadWorkPhases[], timeAndDuration{startTime, endTime}, additionalInformation, sender}]`, `contact` (drop: personal contact data). About 515 active `ROAD_WORK` situations on capture day.
- Mapping: `externalId = situationId`; `observedAt = versionTime`; `ROAD_WORK → roadwork`, `WEIGHT_RESTRICTION|EXEMPTED_TRANSPORT → restriction`, `TRAFFIC_ANNOUNCEMENT` → by `trafficAnnouncementType` (`ACCIDENT_REPORT|PRELIMINARY_ACCIDENT_REPORT → incident`, closures in `features[].name` → `closure`, else `other`); headline from the first announcement title; `roads` from `locationDetails.roadAddressLocation.primaryPoint.roadAddress.road`; `language = fi` unless the announcement says otherwise.
- Interval 5 min (min 1 min), budget 6/min, 3000/day. Licence CC BY 4.0, attribution "Fintraffic / digitraffic.fi". Note in the Source Catalog that Digitraffic also publishes DATEX II, deliberately not used.

### 5. `ndw-situations` and `ndw-srti` (DATEX II v3)

- `ndw-situations`: `https://opendata.ndw.nu/actueel_beeld.xml.gz` (current situations). `ndw-srti`: `https://opendata.ndw.nu/veiligheidsgerelateerde_berichten_srti.xml.gz` (EU safety-related traffic information). Verified 2026-10: gzip files rewritten every minute, ETag + Last-Modified, about 370 KB and 11 KB compressed; root `mc:messageContainer` with `sit:SituationPublication` (`modelBaseVersion="3"`). The 17 MB planned-works feed (`planningsfeed_wegwerkzaamheden_en_evenementen.xml.gz`) is out of scope.
- `Datex3SituationParser`: streaming StAX over the gunzipped body (hardened factory, 64 MB decompressed cap). Per `sit:situation`: `id`, `overallSeverity`, `situationVersionTime`; per `sit:situationRecord`: `xsi:type` (seen on capture: `RoadOrCarriagewayOrLaneManagement`, `SpeedManagement`, `ReroutingManagement`, `VehicleObstruction`, `Accident`, `GeneralObstruction`; handle `MaintenanceWorks`, `AbnormalTraffic`, `PoorEnvironmentConditions` too), `validity/validityTimeSpecification/overallStartTime|overallEndTime`, `cause/causeType`, `roadOrCarriagewayOrLaneManagementType`, `temporarySpeedLimit`, location from `loc:gmlLineString/loc:posList` or `loc:pointByCoordinates/loc:pointCoordinates/{latitude,longitude}`.
- One record per situation: geometry = the record locations combined (GeometryCollection → simplest valid type); category from the first record type (`Accident|VehicleObstruction|GeneralObstruction → incident`, `RoadOrCarriagewayOrLaneManagement` with a closure type → `closure`, `MaintenanceWorks → roadwork`, `SpeedManagement → restriction`, `PoorEnvironmentConditions → weather`); `severity` from `overallSeverity`; `observedAt = situationVersionTime`.
- **posList axis order:** DATEX II GML uses "lat lon" pairs; confirm on the fixture (all points must fall inside lat 50.5–53.7, lon 3.2–7.3) and fail the contract test otherwise.
- Interval 5 min (min 1 min), budget 4/min, 2000/day each. Licence: CC0 per NDW's copyright notice (verify the portal statement during capture). Descriptions are Dutch (`language = nl`).

### 6. Fixtures, docs

- Capture real responses per source (`curl --compressed -A "Elaro-fixture/<version>"`; for NDW keep the `.xml.gz` and a trimmed `.xml` of ≤ 30 situations). README with dates, URLs and the WZDx registry snapshot date.
- `docs/Source-Catalog.md`: rows for every source, the WZDx registry snapshot procedure (re-vendoring is a reviewed change), and the DATEX/Digitraffic decision.
- `docs/Adapter-Guide.md`: "Normalised kinds across formats" using `road-event` as the example.
- No API change.

## Tests to write (do not run)

- `Open511ParserTest`, `WzdxParserTest` (4.0 and 4.2 fixtures; 3.1 rejected), `DigitrafficRoadParserTest`, `Datex3SituationParserTest`: real fixtures, category/severity mapping tables (parameterised), axis order, simplification, contact data dropped, `SnapshotContract.assertStableStaticHash`, fieldSpecs contract.
- `RoadEventPayloadTest`: truncation, closure inference, null omission.
- `WzdxRegistrySnapshotTest`: no snapshot URL contains a token or credential-looking query value; ids unique; key-required feeds declare the credential.
- Adapter tests (WireMock): Open511 paging via `next_url`; Digitraffic one request per type with conditional GET and `Digitraffic-User`; NDW gzip handling and 304; WZDx key placement redacted in traces.
- `RoadEventsIngestIT` (`@IntegrationTest`): Digitraffic fixture ingests N events; an event absent twice is tombstoned; an Open511 `ARCHIVED` event tombstones immediately; `validFrom`/`validUntil` populated from schedules.

## Acceptance criteria

- `open511-drivebc`, the vendored `wzdx-*` feeds, `digitraffic-road`, `ndw-situations` and `ndw-srti` register, all disabled, all emitting `kind = road-event` with the shared payload.
- No upstream contact data (phone, email) reaches a payload.
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
2. **Commit** all changes with the message `074: Road Events And Work Zones`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/074-CR-Road-Events-And-Work-Zones.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `074: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
