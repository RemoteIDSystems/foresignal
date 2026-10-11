# 070 — Transit GTFS RT

> Elaro build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/elaro`).
> Completion Report: `completion-reports/070-CR-Transit-GTFS-RT.md`

## Context

- **Project:** Elaro, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.elaro`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/069-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Core-Data-Model.md` §2 (GTFS-RT row: vehicle `timestamp`, else header `timestamp`, fixed per feed), §3 (rules 7, 8 and 13), §5.3 ("Transit and rail" row)
- `docs/Architecture.md` §4.2 rule 5 (families from a static, code-reviewed list; never caller URLs)
- `docs/Gap-Analysis.md` §4b ("GTFS-RT ServiceAlerts + MobilityData catalogue: vendored CC0 snapshot, not caller URLs"), §8.C item 33
- `docs/Source-Catalog.md` row `transit-*`
- Completion Reports of 035 (outbound URL policy), 062 (`egress`), 067 (alert time mapping: `validFrom`/`validUntil`, cancellation and absence rules)
- GEV (MIT, portable): `src/data/transitFeeds.js` (the reviewed registry with licence quotes and route-mode functions), `src/data/gtfsRealtime.js`, `src/sources/transitService.js`
- Upstream:
  - GTFS-RT reference and proto `https://github.com/google/transit/blob/master/gtfs-realtime/proto/gtfs-realtime.proto`
  - Mobility Database catalogue `https://files.mobilitydatabase.org/feeds_v2.csv` with schema `https://github.com/MobilityData/mobility-feed-api/blob/main/docs/SpreadsheetSchemaV2.md`

## Objective

Add the `transit-*` families:
- GTFS-RT **VehiclePositions** as TRACK `transit-vehicle` records
- **ServiceAlerts** as `transit-alert` records

Both come from a static, code-reviewed feed list. Add a **MobilityData catalogue resolver** that lets an operator enable further keyless feeds by id from a vendored CC0 snapshot, never from a URL supplied at runtime.

## Tasks

### 1. Dependency

- Add `org.mobilitydata:gtfs-realtime-bindings:0.2.0` (verified 2026-10, released 2026-07-29; depends on `com.google.protobuf:protobuf-java` 4.34.0). Pin protobuf-java 4.x in `gradle/libs.versions.toml`, and check `./gradlew dependencies` for conflicts.
- Do not use the old `com.google.transit` artifact: it stopped at 0.0.4.
- Parse with `FeedMessage.parseFrom` on a size-capped body (`maxResponseBytes` 16 MB). A protobuf parse failure → `UpstreamFormatException`. An HTML or JSON 200 is detected by its first byte before parsing.

### 2. Reviewed feed list (`ai.elaro.source.adapters.transit.TransitFeeds`)

- Port GEV's `TRANSIT_FEED_REGISTRY` to a Java record list. Fields: `id, name, operator, region, vehiclePositionsUrl, alertsUrl` (nullable), `headers, licence, licenceUrl, attribution, timeBase` (`VEHICLE|HEADER`), `routeMode` (function or default mode).
- Endpoints verified 2026-10:

| id | VehiclePositions | Alerts | Notes |
|---|---|---|---|
| `mbta` | `https://cdn.mbta.com/realtime/VehiclePositions.pb` | `…/Alerts.pb` | MassDOT Developers License; text credit only |
| `capmetro-austin` | `https://data.texas.gov/download/eiei-9rpf/application%2Foctet-stream` | none (alerts are JSON only) | CapMetro terms |
| `metrotransit-msp` | `https://svc.metrotransit.org/mtgtfs/vehiclepositions.pb` | `…/alerts.pb` | |
| `hsl-helsinki` | `https://realtime.hsl.fi/realtime/vehicle-positions/v2/hsl` | `https://realtime.hsl.fi/realtime/service-alerts/v2/hsl` | CC BY 4.0. The Digitransit API host needs a subscription key, but these realtime hosts do not. |
| `ovapi-nl` | `https://gtfs.ovapi.nl/nl/vehiclePositions.pb` | `https://gtfs.ovapi.nl/nl/alerts.pb` | README asks for an identifying UA, conditional GET when polling faster than 1/min, and gzip; terms otherwise unverified |
| `entur-norway` | `https://api.entur.io/realtime/v1/gtfs-rt/vehicle-positions` | `https://api.entur.io/realtime/v1/gtfs-rt/alerts` | NLOD; **`ET-Client-Name: elaro-{installId}`** is mandatory; data refreshes every 15 s |
| `translink-seq` | `https://gtfsrt.api.translink.com.au/api/realtime/SEQ/VehiclePositions` | `…/SEQ/alerts` | CC BY 4.0 (unverified); text credit only |

- The `ET-Client-Name` value is built from the install id, so each installation is identifiable. It is the only header a feed may add; `User-Agent` stays Elaro's.

### 3. `transit-{id}` VehiclePositions (`TransitVehicleFamily`)

- One POLLING source per feed. Interval 30 s (min 15 s); budget 4/min, 3000/day; `getConditional`.
- **Descriptor:** kind `transit-vehicle`; TRACK, `ttlAnchor` OBSERVED, TTL 5 min.
  - `SAMPLED{minInterval 15s, keyframe 240s, minDeviationMeters 150, thresholds true, edgeMinInterval 8s}`. Check: `240 ≤ 300 − 30 − 30`. The validator rejects intervals above 30 s unless the keyframe is shortened; state this.
  - `significantFields = [routeId, tripId, directionId, label, mode]`. `currentStatus` and `stopId` change at every stop, so they are deliberately not significant.
  - motion `{speedField: speedMs, speedUnit: MPS, courseField: bearing}`; threshold `bearing` 30° angular.
  - Licence and attribution from the feed entry. `egress` OPEN unless the feed entry says otherwise.
- **Time base, fixed per feed (CDM §2, rule 13):**
  - `VEHICLE`: `observedAt = vehicle.timestamp`. A vehicle without one is dropped and counted (`elaro.transit.untimed{source}`), never given the header time.
  - `HEADER`: always `header.timestamp`.
  - Choose per feed from the fixture: VEHICLE when ≥ 95% of entities carry a timestamp.
- **Record:**
  - `externalId = vehicle.vehicle.id`, else `entity.id`
  - geometry `Point(position.longitude, position.latitude)`; skip 0/0 and out-of-range positions
  - payload: `routeId, tripId, directionId, startDate, label, currentStatus, stopId, currentStopSequence, occupancyStatus, congestionLevel, bearing, speedMs, mode, agency`
  - `licensePlate` is omitted
  - `quality.positionSource` REPORTED
  - `titleField label` (fallback `routeId`); tiles `routeId, mode, bearing, occupancyStatus`

### 4. `transit-alerts-{id}` ServiceAlerts

- One POLLING source per feed with an `alertsUrl`. Interval 60 s (min 30 s); budget 2/min, 1500/day. STANDARD, ALL, no TTL. `zeroResultsNormal` true.
- **Proto changes (verified 2026-10):** `active_period` (1) is **deprecated** in favour of `communication_period` (2) and `impact_period` (3) (2026-06-30). The new fields are `cause_detail`, `effect_detail` and `image`, and `cause` gained `SPECIAL_EVENT`. If 0.2.0 lacks the new fields, read them from unknown fields or record that as a gap.
- **Record (follow 067's alert mapping):**
  - kind `transit-alert`, `externalId = entity.id`
  - `observedAt = header.timestamp` (alerts carry no own timestamp)
  - `validFrom` = earliest start of `impact_period`, else `active_period`
  - `eventTime` = the same start
  - `validUntil` = latest end of those periods when every period has an end (upstream-asserted); else null
  - payload:
    - `headerText`, `descriptionText` (≤ 4 KB), `url`: translation chosen by param `language` (default `en`, else the first)
    - `cause, effect, severityLevel, causeDetail, effectDetail`
    - `communicationPeriods, impactPeriods` (ISO pairs, ≤ 20)
    - `informed` (≤ 50 `{agencyId, routeId, routeType, stopId, tripId}`)
  - no geometry (stops need static GTFS, which is out of scope)
  - `embedFields = [headerText, descriptionText]` (untrusted text)
- **Absence:** GTFS-RT `FULL_DATASET` semantics mean an alert missing from the next full feed has ended. When `header.incrementality == FULL_DATASET`, emit explicit tombstones for ids in adapter state `alerts` that are missing from a successful response. A 304 changes nothing. `DIFFERENTIAL` feeds tombstone only on `is_deleted`.

### 5. MobilityData catalogue resolver

- **Vendored snapshot:** `src/main/resources/transit/mobility-gtfs-rt.csv`, generated by a dev-time script `scripts/update-mobility-catalog.py` from `feeds_v2.csv` (CC0 metadata; verified 2026-10).
  - Keep rows with `data_type = gtfs_rt`, `status = active`, `urls.authentication_type = 0` and an https `urls.direct_download`.
  - Keep columns `id, provider, location.country_code, entity_type, urls.direct_download, urls.license, static_reference`.
  - Note the snapshot date in a sibling README.
  - The legacy `bit.ly/catalogs-csv` / `sources.csv` (with `mdb_source_id`) is deprecated; do not use it.
- **`MobilityCatalog`:** loads the snapshot. `search(country, query, entityType)`; `get(id)`.
- **Enabling a catalogue feed:**
  - bootstrap property `elaro.transit.catalog-feeds` (comma-separated MDB ids, default empty)
  - at startup the family registers `transit-mdb-{id}` (vehicle positions, entity `vp`) and `transit-alerts-mdb-{id}` (`sa`), using only the vendored URL, `timeBase=HEADER` (unknown feeds), and licence from `urls.license` or "see provider terms"
  - each URL must pass 035's outbound URL policy at startup; on failure, skip it and log
  - a restart is required; document why: URLs are never accepted at runtime
- **Admin search endpoint:** `GET /v1/transit/catalog?country=&q=&entityType=vp|tu|sa&limit=` (`x-elaro-scope: admin`) returns snapshot rows and whether each is already enabled. It is read-only, and there is no POST.

### 6. Fixtures

- Capture a VehiclePositions and an Alerts `.pb` from MBTA, Entur (with `ET-Client-Name`) and HSL, each with a README (date, URL, headers) and a `protoc --decode_raw` dump for review. Trim with a small test utility that rewrites a FeedMessage to the first 30 entities.
- Hand-made: `untimed-vehicle.pb`, `full-dataset-alert-removed.pb`, `alert-legacy-active-period.pb`, `alert-impact-period.pb`, `html-200.html`.

### 7. Spec, catalogue, docs

- `docs/OpenAPI.json`: `GET /v1/transit/catalog` with schema `TransitCatalogEntry`, `x-elaro-scope: admin`; add the AccessPolicy rule; regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`. MCP: none.
- `docs/Source-Catalog.md`: the `transit-*` row (TTL 5 min, SAMPLED, per-feed time base, significant fields), new `transit-alerts-*` and `transit-mdb-*` rows, the verified endpoint table.
- Operator Guide: "Adding a transit feed from the Mobility Database" (search, property, restart, licence check).
- `THIRD_PARTY_NOTICES.md`: GEV entries for `transitFeeds.js`, `gtfsRealtime.js` and `transitService.js`; MobilityData catalogue (CC0); gtfs-realtime-bindings (Apache-2.0); protobuf-java (BSD-3).

## Tests to write (do not run)

- `GtfsRtVehicleParserTest`:
  - MBTA/Entur/HSL fixtures
  - VEHICLE vs HEADER time base; untimed vehicle dropped
  - externalId fallback; 0/0 skipped; mode mapping
  - `SnapshotContract.assertStableStaticHash` across a stop change
- `GtfsRtAlertParserTest`:
  - impact vs legacy active periods; open-ended period → no `validUntil`
  - translation choice; informed-entity cap; text caps
- `TransitFamilyTest`: one source per reviewed feed; alerts source only where `alertsUrl` exists; the Entur header carries the install id; registry validation passes (keyframe constraint).
- `TransitAdapterTest` (WireMock): conditional GET 304; protobuf garbage → `UpstreamFormatException`; HTML 200 detected; a FULL_DATASET removal → tombstone; DIFFERENTIAL `is_deleted`.
- `MobilityCatalogTest`: snapshot load; filters; `catalog-feeds` registers only vendored URLs; a URL failing 035 is skipped.
- `TransitCatalogControllerIT`: admin only; search filters; the enabled flag.
- `TransitIngestIT` (`@IntegrationTest`): a vehicle moving 200 m after 20 s → SAMPLE; a trip change → SIGNIFICANT; alert FIRST → tombstone on removal; `validFrom` stored.

## Acceptance criteria

- No transit URL comes from an API caller. Every feed is from the reviewed list or the vendored snapshot.
- `transit-*` sources register as TRACK + SAMPLED with valid defaults.
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
2. **Commit** all changes with the message `070: Transit GTFS RT`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/070-CR-Transit-GTFS-RT.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `070: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
