# 040 — Gazetteer And Region Tagging

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/040-CR-Gazetteer-And-Region-Tagging.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–129).
- **Previous Completion Report:** read `completion-reports/039-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §6 "Gazetteer / region tagging" row, §8 item 25 ("028f" = this prompt)
- `docs/Core-Data-Model.md` §4 (what is hashed: region tags must stay **outside** the hash), §1 I1 (only the writer writes `record_head`/`record_live`, so tags live in their own table), §10.1 (ChangeBus, LiveBus)
- `docs/Source-Catalog.md` rows that need country centroids: `ioda-outages`, `abusech-feodo`, `unhcr-population`, travel advisories (FIPS→ISO)
- Completion Reports of:
  - 017: MessageTemplate, conditions, geofences, the PersistGate snapshot
  - 018: channel templates and filters, `units`/timezone
  - 027: embedding document text
  - 029: BriefingScope
  - 035: outbound URL policy
  - 036: places
- Data and licences, verified 2026-10:
  - GeoNames dumps, https://download.geonames.org/export/dump/ : `cities15000.zip` (~25k rows), `cities1000.zip`, `cities500.zip`, `admin1CodesASCII.txt`, `countryInfo.txt`. CC BY 4.0; attribution required. Use the bulk dumps only, never `api.geonames.org`.
  - Natural Earth 10m cultural vectors (public domain; current edition 5.1.x), as GeoJSON from https://github.com/nvkelso/natural-earth-vector/tree/master/geojson : `ne_10m_admin_0_countries`, `ne_10m_admin_1_states_provinces`, `ne_10m_admin_2_counties` (US only), `ne_10m_geography_marine_polys`. Pin a release tag and SHA-256 at implementation.

## Objective

Give every record a human place context without any online geocoder: country, first-level region, US county, sea area, and "12 km NNE of Austin, TX". Tags are stored beside the record, never in the payload, so they never create versions. They feed message placeholders, list filters, rule geofence presets, embeddings and MCP. A small bundled country table with label points also replaces the per-adapter centroid tables that IODA, abuse.ch, UNHCR and the advisories would otherwise each ship.

## Tasks

### 1. Bundled country table (no download needed)

- Generate `src/main/resources/reference/countries.csv` once from Natural Earth admin-0 and commit it with a generator script `scripts/build-countries.py`. Columns:
  - `iso2` (`ISO_A2_EH`; never the `-99` values of `ISO_A2`), `iso3` (`ISO_A3_EH`), `isoNumeric`, `fips` (`FIPS_10`)
  - `name`, `nameLong`, `continent`
  - `labelLon`, `labelLat` (NE `LABEL_X`/`LABEL_Y`; these always fall inside the country, unlike geometric centroids)
  - `aliases` (a pipe-separated list including the GeoNames `countryInfo` names, plus common variants such as "Türkiye/Turkey", "Czechia/Czech Republic", "Côte d'Ivoire/Ivory Coast")
- `CountryCentroids` bean (`ai.foresignal.reference`): `byIso2`, `byIso3`, `byFips`, `byName` (case- and accent-insensitive, aliases included), `all()`. Available at startup with no database or network.
- Check whether 068, 076 or 090 have already bundled their own table. If one has, switch it to `CountryCentroids` now; otherwise document that those adapters must use it.

### 2. Reference tables and loader

- Hibernate entities (GiST through `SpatialIndexInitializer`):
  - `ref_country`: `iso2` PK, `iso3`, `name`, `geom` MultiPolygon 4326, `geom_simple`
  - `ref_admin1`: `code` PK (ISO 3166-2, e.g. `US-TX`; fall back to NE `adm1_code` when missing), `iso2`, `name`, `geom`, `geom_simple`
  - `ref_admin2`: `fips5` PK, `state_code`, `name`, `geom`
  - `ref_sea`: `id` PK, `name`, `geom`
  - `gazetteer_place`: `geonameid` PK, `name`, `ascii_name`, `iso2`, `admin1_code`, `feature_code`, `population`, `timezone`, `geog geography(Point)` with GiST, for KNN `<->` in metres
  - `ref_dataset`: `name` PK, `edition`, `url`, `sha256`, `licence`, `attribution`, `rows`, `loaded_at`
- `reference/manifest.json` (a resource): per dataset, the URL, pinned SHA-256, edition, licence and attribution. Defaults:
  - `ne-admin0`, `ne-admin1`, `ne-admin2-us`, `ne-marine`
  - `geonames-cities15000` + `geonames-admin1` + `geonames-countryinfo`
  - optional `geonames-cities1000` and `geonames-cities500`
  - Marine Regions IHO sea areas are **not** offered: their terms ask that products not be re-hosted. NE marine polygons are the default sea layer.
- `ReferenceDataLoader`:
  - Downloads through `UpstreamClient` with an identifiable UA and a small budget. The URLs come from the bundled manifest; there are no caller-supplied URLs.
  - Writes to `${FORESIGNAL_DATA_DIR}/reference/` and verifies SHA-256 (mismatch → `REFERENCE_CHECKSUM_MISMATCH`, dataset not loaded).
  - `foresignal.reference.offlineDir` loads pre-placed files instead.
  - Parses GeoJSON with streaming Jackson plus JTS `GeoJsonReader`, applying `GeometryFixer`. Parses GeoNames TSV.
  - Loads into a staging table, then swaps inside one transaction (`TRUNCATE` + `INSERT … SELECT`). `geom_simple` = `ST_SimplifyPreserveTopology(geom, 0.01)`.
  - Runs async on first boot when `foresignal.reference.autoLoad=true` (default). It never blocks startup or readiness; failures surface as a `reference` readiness detail and a system event.
- API:
  - `GET /v1/reference` (read): datasets, editions, row counts, attributions, load state
  - `POST /v1/reference/load {datasets[]}` (admin, async, 202)
  - `GET /v1/reference/countries` (read): the bundled table
  - `GET /v1/reference/reverse?lon&lat` (read): `RegionTags`
- Licensing:
  - GeoNames CC BY 4.0 attribution goes in `THIRD_PARTY_NOTICES.md`, in `/v1/reference`, and in any response field derived from `near`. Expose `RegionAttribution` so prompt 109 can aggregate it.
  - Natural Earth needs no attribution, but credit it anyway.
  - The docs state that NE draws de facto boundaries; `foresignal.reference.neAdmin0Variant` can select an NE point-of-view file (e.g. `_ind`), with its URL and hash in the manifest.

### 3. RegionTagger

- `RegionTagger.tag(Geometry) → RegionTags{iso2, admin1, admin2, sea, near{geonameid, name, iso2, admin1, distanceKm, bearingDeg, compass16, population}}` on the representative point (`ST_PointOnSurface` for areas).
- Containment uses `ST_Covers` on `ref_country`/`ref_admin1`/`ref_admin2`. For coastal points within 5 km, fall back to the nearest country by distance; at sea, `iso2` is null and `sea` is set.
- `near` is the KNN nearest place with `population ≥ foresignal.reference.nearMinPopulation` (default 0, since the cities15000 file is already filtered).
- An LRU cache on coordinates rounded to 0.01° (50,000 entries).
- If the reference tables are empty, `near`, `admin1` and so on are null and `iso2` comes from a point-in-polygon test against bundled simplified country polygons. If that is too large to bundle, `iso2` is null. Never fail.

### 4. Tags stored outside the hash

- Table `record_region`: PK `(source_id, external_id)`, with `iso2`, `admin1`, `admin2`, `sea`, `near_id`, `near_name`, `near_km`, `near_bearing`, `tagged_lon`, `tagged_lat`, `tagged_at`. Index `(iso2)` and `(iso2, admin1)`. It is written by `RegionTagWorker`, never by the ingest writer, and is not part of any hash.
- `RegionTagWorker`:
  - durable cursor `region.cursor` over ChangeBus batches (persisted versions)
  - plus a LiveBus subscription for moving entities
  - re-tags only when the entity moved more than `retagKm` (default 2 km; 10 km for TRACK sources) from `tagged_lon/lat`, or a tombstone arrived (the row is deleted)
- Per-source setting `regionTagging: eager|lazy|off`. The default is eager for STANDARD and lazy for TRACK. Lazy means tagging on demand (`RegionTagger.tagRecord(...)`) when a template renders, an alert fires, a briefing is built or a record is requested with `include=region`; the result is upserted.
- Rows are deleted by an orphan sweep after HeadPurge and by 039's purge fan-out.
- **Alerts** snapshot `alert.region jsonb` at fire time.

### 5. Templates, filters, rules

- MessageTemplate placeholders (017/018), each empty when unknown:
  - `{{near.name}}`, `{{near.km}}`, `{{near.mi}}`, `{{near.bearing}}` (compass, e.g. `NNE`)
  - `{{near.text}}` (`12 km NNE of Austin, TX`; imperial when the channel's `units=imperial`, or with the filter `|imperial`)
  - `{{country}}` (ISO2), `{{country.name}}`, `{{admin1}}`, `{{admin1.name}}`, `{{county}}`, `{{sea}}`
  - Add them to the Architecture template list and the channel docs.
- **Filters** `country` (an ISO2 list), `admin1` (an ISO 3166-2 list) and `county` (FIPS5) on:
  - `/v1/records` and GeoJSON, joining `record_region` (document that live movers may lag by up to one retag threshold)
  - `/v1/search`, `/v1/alerts` (via `alert.region`)
  - 038 raw stats
  - `BriefingScope`
  - Unknown codes → 400 `UNKNOWN_REGION`.
- **Rule geofence presets:** a trigger rule may set `geofenceRef: {country?: [...], admin1?: [...], county?: [...]}` instead of a polygon. `RuleSqlBuilder` renders `ST_Intersects(v.geom, (SELECT ST_Union(geom) FROM ref_… WHERE code = ANY(:codes)))`. `TriggerPersistGate` loads the same union as a JTS `PreparedGeometry` into its snapshot, so evaluation is exact with no tag lag. If the reference data is missing → 409 `REFERENCE_DATA_MISSING` at rule save. Add a row to the shared operator truth table.

### 6. Embeddings, MCP, records

- The 027 embedding text appends `Near: {near.text}. Region: {admin1.name}, {country.name}.` when tags exist. Retagging does not trigger re-embedding unless the country or admin1 changes.
- The MCP short record shape gains `near` (the text). New tool `reverse_geocode {lon, lat}` (read).
- `include=region` on the record GETs returns `region`. GeoJSON features gain `properties.near` when `include=region`.
- 038's glance `latest` alerts may show `near` through a `GlanceContributor`.

### 7. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - operations `getReference`, `loadReference` (admin, 202), `listReferenceCountries`, `reverseGeocode`, each with `x-foresignal-scope`
  - parameters `country`, `admin1`, `county` on `listRecords`, `search`, `listAlerts`; `include=region`
  - schemas `RegionTags`, `ReferenceStatus`, `CountryRef`; `geofenceRef` on trigger schemas; `region` on `Alert`, `Record`, `BriefingScope`
  - error codes `UNKNOWN_REGION`, `REFERENCE_DATA_MISSING`, `REFERENCE_CHECKSUM_MISMATCH`
- Add a `BODIES` entry for `loadReference`, then regenerate Postman.
- `AccessPolicy`: READ for `GET /v1/reference/**`; ADMIN for `POST /v1/reference/load`.
- `docs/Operator-Guide.md`: datasets, disk use, offline loading, licences, the boundary disclaimer, and template examples.
- `docs/Adapter-Guide.md`: use `CountryCentroids` for country-level records and set `geometryApproximate: true` in the payload.

## Tests to write (do not run)

- `CountryCentroidsTest`: the bundled table loads; ISO2, ISO3, FIPS and alias lookups (Türkiye, Czechia, Kosovo `XK`); no `-99` codes; every label point lies inside its country's bbox.
- `ReferenceDataLoaderIT` (WireMock serving small fixture GeoJSON and TSV files): checksum mismatch rejected, staging swap is atomic, offline dir, status endpoint.
- `RegionTaggerIT`: inland point (country, admin1, county), coastal fallback, sea point, KNN near text and bearing, empty-reference degradation.
- `RegionTagWorkerIT`: eager tagging, retag threshold for movers, lazy mode on demand, tombstone deletes the row, cursor survives restart.
- `RegionHashInvarianceIT`: tagging never changes `static_hash` or `full_hash` and never writes a version.
- `RegionTemplateTest`: every placeholder, imperial, missing values.
- `RegionFiltersIT`: records, search and alerts filters; `UNKNOWN_REGION`.
- `GeofenceRefIT` plus a truth-table fixture row: a country preset fires enter and exit; `TriggerPersistGateTest` agrees with SQL.
- `ReverseGeocodeControllerIT`, `McpReverseGeocodeToolIT`.

## Acceptance criteria

- Records gain region tags without any new versions or hash changes.
- `{{near.text}}` renders in alert messages.
- `country=` filters work, and a `geofenceRef` rule on a country fires exactly like a polygon rule.
- `CountryCentroids` works with no network and no database rows.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 129, 132, 133, 134).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 134).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `040: Gazetteer And Region Tagging`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/040-CR-Gazetteer-And-Region-Tagging.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `040: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
