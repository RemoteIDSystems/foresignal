# 094 — Reference Datasets

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/094-CR-Reference-Datasets.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/093-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Source-Catalog.md` rows `wri-power-plants`, `ourairports`, `gvp-volcanoes`, `submarine-cables`
- `docs/Gap-Analysis.md` §8 item 52 (labelled 056 = this prompt: World Port Index buffered, NPS boundaries), §4c row "Dataset-by-URL" ("tombstone absent heads")
- `docs/Core-Data-Model.md` §2 ("Catalogue and reference": descriptor picks UPSTREAM or RECEIVED), §3 (limits: ≤ 10,000 vertices), §5.2 rows 2–3 and 11 (tombstones, touch throttle), §6 (writer, HeadPurge chunking pattern), §13 (STANDARD retention keyframe at window/4)
- Completion Reports for 010 (FetchExecutor, fetch_log), 073 (ArcGIS FeatureServer kit), 092 (H3, Commons Compress)
- Upstream pages: WRI GPPD `https://datasets.wri.org/datasets/global-power-plant-database`; OurAirports `https://ourairports.com/data/`; TeleGeography citation `https://www2.telegeography.com/cite-telegeography-map`; GVP web services `https://volcano.si.edu/database/webservices.cfm`, citation and terms; NGA MSI World Port Index; NPS Land Resources Division FeatureServer; GCAT `https://planet4589.org/space/gcat/`; FEMA Nuclear Plant Power Reactor Status; Wikidata SPARQL and its User-Agent policy; PeeringDB AUP `https://www.peeringdb.com/aup`

## Objective

Load the slow-changing reference layers (power plants, airports, cables, volcanoes, ports, park boundaries, launch sites, nuclear facilities) as ordinary records polled weekly. Versioning must detect what appeared, changed and disappeared between releases. That needs two framework pieces this prompt adds: chunked fetches for large files, and complete-set reconciliation that tombstones absent records. 098 (`faa-registry`) and 099 (port buffers, airports) build on both.

## Tasks

### 1. Chunked fetch and complete-set reconciliation (`ai.foresignal.collect`, `ai.foresignal.ingest`)

- **Chunked fetch:** check what 010 provides; if missing, add `FetchContext.emitChunk(List<NormalizedRecord>)` (≤ 5,000 records per chunk). `FetchExecutor` submits each chunk as its own `IngestBatch` and waits for the previous future before accepting the next (back-pressure; heap stays bounded). Each chunk writes its own `fetch_log` row; add `fetch_log.run_id` (shared by the chunks of one run). The final `FetchResult` may carry remaining records and `complete`.
- **Forced touches:** batches of a complete-set run set `IngestBatch.forceTouch=true`, so unchanged records always update `record_live.last_received_at` (outcome TOUCH, never TOUCH_THROTTLED), even on a manual re-run inside `touchMin`.
- **`TombstoneAbsent` writer command:** extend the sealed `IngestCommand` with `TombstoneAbsent(sourceId, Set<String> kinds, Instant runStartedAt, double maxAbsentRatio, future)`, submitted by `FetchExecutor` after the last chunk of a successful run with `complete=true`.
  - Candidates: `record_head h JOIN record_live l` where `h.source_id = ?`, `h.kind = ANY(?)`, `NOT h.tombstone`, `l.last_received_at < :runStartedAt`.
  - **Shrink guard:** if candidates / (candidates + records seen in the run) > `maxAbsentRatio` (descriptor `reconcile.maxAbsentRatio`, default 0.2), write nothing, mark the run `DISCARDED` for reconciliation, and raise health reason "dataset shrank by N% — reconciliation skipped". The operator can pass `allowShrink=true` on `POST /v1/sources/{id}/fetch` once.
  - Otherwise write tombstones through the normal fold (`persist_reason=TOMBSTONE`) in chunks of 10,000, re-enqueuing itself at the queue tail like `HeadPurge`.
  - Never runs after a 304, a failed run or a partial run.
- **Release summary:** after reconciliation, each reference source emits one record, kind `dataset-release`, `externalId = release`, payload `upstreamVersion` (ETag, Last-Modified or the dataset's own version), `records, appeared, changed, disappeared` (from the run's `byReason`: FIRST+REAPPEAR, SIGNIFICANT+CHANGE, TOMBSTONE), `runId`. Rules and briefings can then notice a release.
- **Descriptor defaults for reference sources:** STANDARD, ALL, no TTL, `observedAtMode=RECEIVED`, `zeroResultsNormal=false`, interval 7 d (min 1 d), budget 2/min and 20/day, `defaultRetention` 730 d. The long window keeps the retention keyframe (window/4 ≈ 6 months) from rewriting every record each week. Conditional GET wherever the host supports it; a 304 skips the run.

### 2. Datasets

Each adapter stream-parses into chunks and declares `fieldSpecs`, `titleField`, `licence`, `termsUrl` and attribution. Verified 2026-10 unless marked otherwise.

| Source | Upstream | Kind / externalId / geometry | Payload and notes |
|---|---|---|---|
| `wri-power-plants` | `https://raw.githubusercontent.com/wri/global-power-plant-database/master/output_database/global_power_plant_database.csv` (about 12 MB; ETag) | `power-plant` / `gppd_idnr` / Point | `name, country, capacityMw, primaryFuel, otherFuels[], commissioningYear, owner, source, url, geolocationSource, yearOfCapacityData`. Frozen at v1.3.0 (2021); keep the weekly conditional GET (it costs one 304). CC BY 4.0, "World Resources Institute". GitHub Releases stop at v1.1.0, and datasets.wri.org links are not stable: do not use them. |
| `ourairports` | `https://davidmegginson.github.io/ourairports-data/airports.csv` (ETag, rebuilt nightly) | `airport` / `ident` / Point | `name, type, elevationFt, isoCountry, isoRegion, municipality, scheduledService, icaoCode, iataCode, gpsCode, localCode`. Param `types` (default `large_airport, medium_airport, small_airport`; `heliport, seaplane_base, balloonport, closed` optional). Param `navaids` (default false) adds `navaids.csv` as kind `navaid`. Do **not** use `ourairports.com/data/…` (only redirects; its TLS certificate had expired on 2026-10-10). Public domain. |
| `submarine-cables` | `https://www.submarinecablemap.com/api/v3/cable/cable-geo.json`, `…/api/v3/landing-point/landing-point-geo.json` (Last-Modified; max-age 7200) | `cable` / `cable:{id}` / MultiLineString (stylised); `cable-landing` / `landing:{id}` / Point | `name, color, isTbd`. **Disabled by default.** The JSON is the public map's undocumented data, the geometry is stylised, the citation page states CC BY-SA 4.0 for the public maps and the underlying database is subscriber-only. Set `licence = "CC BY-SA 4.0 (TeleGeography public map; underlying data restricted) — treat as non-commercial"`, `nonCommercial: true` in the descriptor attribution metadata, egress local-only for 109/110, and docs "display only, attribute TeleGeography". |
| `gvp-volcanoes` | WFS `https://webservices.volcano.si.edu/geoserver/GVP-VOTW/ows?service=WFS&version=2.0.0&request=GetFeature&typeName=GVP-VOTW:Smithsonian_VOTW_Holocene_Volcanoes&outputFormat=application/json` (about 2.4 MB, 1,214 features, one response) | `volcano` / `Volcano_Number` / Point (`GeoLocation`) | `name, landform, primaryType, lastEruptionYear` (negative = BCE; omit when null), `country, region, subregion, elevationM, tectonicSetting, evidenceCategory, rockType`, `summary` (≤ 2 KB, `embedFields`). Use only the webservices host (`volcano.si.edu` itself challenges scripted clients). Licence "Smithsonian Terms of Use (non-commercial; cite VOTW DOI)"; attribution "Global Volcanism Program, Volcanoes of the World v5.4.0 (2026), doi:10.5479/si.GVP.VOTW5-2026.5.4"; read the current version from the citation page at implementation. |
| `world-port-index` | `https://msi.nga.mil/api/publications/world-port-index?output=json` (about 6.3 MB, `{"ports":[…]}`, 2,951 ports) | `port` / `wpi:{portNumber}` / Point; `port-area` / `wpi-area:{portNumber}` / Polygon | Use `ycoord`/`xcoord` (decimal); `latitude`/`longitude` are DMS strings. Payload `portName, countryCode, countryName, unloCode, regionName, harborSize, harborType, harborUse, shelter, channelDepthM, anchorageDepthM, maxVesselLengthM, maxVesselDraftM`, `alternateName`. **Buffered:** `port-area` is a 64-vertex geodesic circle with radius by `harborSize` (V 2 km, S 3 km, M 6 km, L 10 km; unknown 3 km), payload `bufferKm` and `portNumber`. 099 uses these buffers. Public domain (US Government). |
| `nps-boundaries` | ArcGIS `services1.arcgis.com/fBc8EJBxQRMcHlei/arcgis/rest/services/NPS_Land_Resources_Division_Boundary_and_Tract_Data_Service/FeatureServer/2/query?where=1=1&outFields=*&f=geojson&outSR=4326&maxAllowableOffset=0.0005&geometryPrecision=6` (442 features, one page); use the 073 ArcGIS kit | `park-boundary` / `UNIT_CODE` / (Multi)Polygon | `unitName, unitType, parkName, state, region, dateEdit, status`. Check the layer's `editingInfo.dataLastEditDate` (`…/FeatureServer/2?f=json`) first and skip the query when unchanged. Simplify with JTS `TopologyPreservingSimplifier` until ≤ 10,000 vertices (§3 limit; set `quality.extra["geom.simplified"]`). Public domain; "informational, not a legal boundary". |
| `spaceports` | GCAT `https://planet4589.org/space/gcat/tsv/tables/sites.tsv` and `…/lp.tsv` | `launch-site` / `site:{Site}` / Point; `launch-pad` / `pad:{Site}` / Point | `code, type, stateCode, name, tStart, tStop, parent, errorKm`. GCAT TSV: `#`-prefixed header, `-` for blank fields, coordinates may be missing (null geometry, still emitted). CC BY 4.0, "Data from J. McDowell, planet4589.org". Launch Library 2 (`/2.3.0/locations/`, 15 requests/hour anonymous, no published licence) is not used; 064 already covers launches. |
| `nuclear-reactors-us` | FEMA `https://gis.fema.gov/arcgis/rest/services/Partner/Nuclear_Plant_Power_Reactor_Status/FeatureServer/0/query?where=1=1&outFields=*&f=geojson` (99 reactors) | `nuclear-reactor` / `reactor:{license_number}` / Point | `reactorName, powerPlant, state, reactorType, status, powerPct, reportDate, mwe, ownerOperator, nrcRegion, eiaPlantCode`. Updated daily from NRC: interval 24 h (min 6 h); `powerPct` is significant (a power change is a version). Public domain. The NRC `reactors-operating.xls` returns 403 to non-browser clients and has no coordinates: do not use it. |
| `nuclear-plants-wikidata` | `https://query.wikidata.org/sparql` (POST, `Accept: application/sparql-results+json`) with a fixed query: items `wdt:P31/wdt:P279* wd:Q134447` with `wdt:P625`, optional country (`P17`), installed capacity, state of use (`P5817`) and inception (`P571`); confirm every property id on Wikidata before coding | `nuclear-plant` / `wikidata:{Qid}` / Point | `name` (English label), `country, capacityMw, stateOfUse, inception`. CC0. Wikidata requires a descriptive UA with contact (already ours); honour 429 and `Retry-After`; query timeout 60 s. |

### 3. Evaluated and excluded

- **PeeringDB facilities:** the AUP reserves all rights and forbids storing or reproducing the data except for approved Internet-operational purposes (anonymous limits 20/min, and 1/hour for repeated responses over 100 KB). Not implemented; catalogue note "ask support@peeringdb.com before adding".
- **IAEA PRIS:** no open licence. **GEM trackers:** CC BY 4.0 but behind a request form with no stable file URL. Both noted, not implemented.
- **NRC dataset pages:** blocked for scripted clients (see the FEMA row).

### 4. Spec, Postman, docs

- `docs/OpenAPI.json`: add `allowShrink` (boolean) to `POST /v1/sources/{sourceId}/fetch`; add `runId` to `FetchLogEntry`; document kinds `dataset-release`, `port-area` and the others through `fieldSpecs`. Regenerate Postman with `python3 scripts/generate-postman.py docs/OpenAPI.json postman`.
- `docs/Source-Catalog.md`: rows for every source with the verified 2026-10 differences (OurAirports URL, TeleGeography licence, GVP terms and DOI, WPI decimal fields, GCAT instead of LL2, FEMA plus Wikidata for nuclear) and the exclusions. `docs/Adapter-Guide.md`: "Reference datasets" (chunking, `complete`, shrink guard, `dataset-release`, retention choice). `docs/Operator-Guide.md`: `allowShrink`, licence flags for cables and volcanoes.

## Tests to write (do not run)

- `TombstoneAbsentIT` (`@IntegrationTest`, `TestRecords`): run 1 loads 10 records; run 2 omits 1 → one TOMBSTONE version and a `dataset-release` with `disappeared=1`; run 2 omitting 5 of 10 trips the shrink guard and writes nothing; `allowShrink` overrides once; a manual re-run within `touchMin` does not tombstone (forced touches); a 304 run never reconciles; a reappearing record gets `REAPPEAR`.
- `ChunkedFetchIT`: 12,000 records arrive as three chunks with one `run_id`; back-pressure holds the next chunk until the previous future completes.
- Parser contract tests with trimmed real fixtures (README with URL and date): `GppdParserTest`, `OurAirportsParserTest` (type filter, navaids), `SubmarineCableParserTest`, `GvpWfsParserTest` (BCE years, null eruption), `WpiParserTest` (decimal fields, buffer radius per size, DMS fields ignored), `NpsBoundaryParserTest` (simplification to ≤ 10,000 vertices), `GcatParserTest` (`#` header, `-` blanks), `FemaReactorParserTest`, `WikidataNuclearParserTest`.
- WireMock adapter tests per source: conditional GET 304 skips the run; NPS `editingInfo` short-circuit; Wikidata POST with UA and 429 backoff.
- `ReferenceDescriptorsTest`: STANDARD/ALL/RECEIVED, 730-d retention, `submarine-cables` disabled by default with the non-commercial flag.

## Acceptance criteria

- A reference dataset run that drops a record writes exactly one tombstone for it, and a truncated download (> 20% missing) writes none and degrades health.
- Heap stays bounded: no adapter materialises a full dataset in memory.
- Every descriptor carries `licence`, `termsUrl` and attribution, including the restricted ones.
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
2. **Commit** all changes with the message `094: Reference Datasets`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/094-CR-Reference-Datasets.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `094: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
