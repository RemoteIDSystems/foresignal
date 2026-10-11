# 104 — Infrastructure Dependency Mapping

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/104-CR-Infrastructure-Dependency-Mapping.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–139).
- **Previous Completion Report:** read `completion-reports/103-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §5 row "Impact and exposure", §9 (rejected scraping and unofficial data)
- `docs/Core-Data-Model.md` §2 (catalogue and reference time base), §3 (descriptor fields), §12 (current reads)
- Completion Reports of 094 (reference datasets: WRI power plants, OurAirports, submarine cables and landing points, World Port Index, PeeringDB, nuclear facilities, spaceports), 040 (region tags), 054 (INBOX/GeoTIFF patterns, optional GDAL subprocess pattern), 101 (ExposureService, `infrastructure-disruption` recipe), 109 if present (licence egress)
- OpenStreetMap licence: `https://www.openstreetmap.org/copyright` (ODbL 1.0, attribution "© OpenStreetMap contributors"); Geofabrik extracts `https://download.geofabrik.de/`; Overpass usage guidance `https://wiki.openstreetmap.org/wiki/Overpass_API` (verified 2026-10: low-volume targeted queries only, and bulk use should download an extract instead); osmium-tool `https://osmcode.org/osmium-tool/` (GPL-3.0, run only as a separate process)

## Objective

Build a facility graph of power, telecom, transport, health, water and space infrastructure from open reference datasets and an operator-supplied OSM extract. The graph has typed relationships that each carry an uncertainty label and the evidence for it. It answers "what depends on things inside this outage or event area?" with paths and explicit uncertainty. All of it is open data, processed locally. Foresignal never queries the public Overpass API in bulk.

## Principles (binding)

- **Every edge states how it is known.** `declared` (the dataset says so), `topological` (geometry connects), `proximity` (nearest-candidate heuristic) or `inferred` (a rule over attributes). Labels are `confirmed | likely | possible | speculative`. Proximity edges are never better than `possible`.
- **Reproducible.** Edges record the rule id and version, the parameters and the evidence (dataset, distance, matched attributes). A rebuild over the same inputs gives the same graph.
- **No overstatement.** Results say "derived from open data; not authoritative; heuristic links may be wrong". A path's label is its weakest edge.

## Tasks

### 1. OSM infrastructure source (`osm-infrastructure`)

- POLLING adapter with a weekly default interval and no network access. It reads `inboxPath` (`INBOX_PATH` param, default `${FORESIGNAL_INBOX_DIR}/osm`).
- **Input:** RFC 8142 GeoJSON text sequences produced by the operator, or by the optional `WITH_OSMIUM=true` image build-arg (`ProcessBuilder` only, like GDAL in 054; osmium is GPL-3.0 and is never linked or bundled by default). The Operator Guide documents the commands:
  ```
  osmium tags-filter region-latest.osm.pbf \
    nwr/power=plant,substation,generator nw/power=line,cable nwr/amenity=hospital nwr/healthcare=hospital \
    nwr/telecom=data_center,exchange nwr/aeroway=aerodrome nwr/landuse=port nwr/harbour=yes \
    nwr/man_made=wastewater_plant,water_works -o infra.osm.pbf
  osmium export infra.osm.pbf -f geojsonseq --add-unique-id=type_id -o infra.geojsonseq
  ```
  Verify the flags against the installed osmium version and record any differences.
- **Records:**
  - Kinds: `substation`, `power-line`, `power-plant-osm`, `hospital`, `data-centre-osm`, `telecom-exchange`, `aerodrome-osm`, `port-osm`, `water-facility`.
  - externalId: the osmium unique id (`n123`, `w456`, `r789`).
  - Payload: allowlisted tags only (`name`, `operator`, `voltage` → `voltageKv` (max of a `;`-list), `plant:output:electricity` → `capacityMw`, `beds`, `emergency`, `iata`/`icao`, `ref`), plus `osmType` and `osmVersion` when present.
  - Geometry: point for nodes; polygon or line for ways and relations, simplified to ≤ 2,000 vertices.
  - Power lines below `minVoltageKv` (param, default 110) are dropped.
- **Descriptor:**
  - STANDARD, `observedAtMode=RECEIVED` (catalogue, Core-Data-Model §2), no TTL, `defaultRetention` 365d, `zeroResultsNormal=false`;
  - `volatileFields: [osmVersion]`, `significantFields` = the allowlisted tags plus geometry;
  - `licence: "ODbL-1.0"`, `termsUrl: https://www.openstreetmap.org/copyright`, `titleField: name`, `fieldSpecs` with units (kV, MW).
- Each scan imports only when the file's sha256 changed. Records absent from a complete new file are tombstoned (`FetchResult.complete=true`; use the writer's absent-head tombstoning if 009 or 024 added it, else tombstone explicitly from adapter state).
- Cap 2,000,000 features per file, with a health reason above it. Health reasons: "no file in inbox", "file unchanged since …", "parse errors: n".
- **Never call Overpass** or `openstreetmap.org` APIs. Record that decision in the adapter Javadoc.
- **Licence handling:**
  - Every OSM-derived record and edge carries `licence: ODbL-1.0`.
  - The facility graph is a derived database. Exports, sinks and federation (110, 111, 113) must offer it under ODbL with attribution. Add an egress-policy row if 109 exists; otherwise add a TODO note to 109's area in the Completion Report.
  - Attribution "© OpenStreetMap contributors" appears in every response that includes OSM-derived data.
- Docs: a fixture capture procedure (a small extract around a test city, processed with the commands above and trimmed to about 200 features), a parser contract test, an ingest IT, and a `docs/Source-Catalog.md` row.

### 2. Graph model (`ai.foresignal.infra`)

- **`infra_node`:**
  - Columns: id bigserial, `source_id`, `external_id` (unique pair), `facility_type`, name, `geom` (Geometry 4326), `point` (Point, a representative point), `attributes` jsonb (capacityMw, voltageKv, iata, beds, …), `region_tags`, `licence`, `built_at`.
  - Indexes: GiST(`geom`), GiST(`point`), `(facility_type)`.
  - `facility_type` taxonomy (a code enum): `power.generation`, `power.nuclear`, `power.substation`, `power.line`, `telecom.datacentre`, `telecom.ixp`, `telecom.exchange`, `telecom.cable`, `telecom.landing`, `transport.airport`, `transport.port`, `health.hospital`, `water.treatment`, `space.spaceport`.
  - Mapping from source kinds lives in `infra/node-mapping.json`: 094 kinds plus the OSM kinds above. A node is a view of a record; it is never edited by hand.
- **`infra_edge`:**
  - Columns: id, `from_node`, `to_node`, `relation` (`supplies_power`, `transmits_power`, `lands_at`, `interconnects`, `hosts`, `serves`), `method` (declared | topological | proximity | inferred), `label` (confirmed | likely | possible | speculative), `score` (0–1), `rule_id`, `rule_version`, `params` jsonb, `evidence` jsonb (`{dataset, distanceKm, matched, notes}`), `licence` (the most restrictive input), `built_at`.
  - Unique `(from_node, to_node, relation, rule_id)`.
- **`InfraGraphBuilder`:**
  - Rebuilds into staging tables, then swaps them in one transaction (rename), so readers never see a half graph.
  - Runs on `POST /v1/infra/rebuild` (admin), after any import of a mapped source (debounced 10 min), and weekly.
  - Records `infra_build(id, started, finished, rulesVersion, inputs {source: lastSeq}, nodes, edges by method and label, warnings)`.
- **Rules in `src/main/resources/infra/dependency-rules.json`** (versioned):
  1. `cable-landing`, declared: submarine cable → landing point from 094 cable data → `lands_at`, confirmed.
  2. `ixp-facility`, declared: PeeringDB `ixfac`/`netfac` → `interconnects`/`hosts`, confirmed.
  3. `line-substation`, topological: a power-line endpoint within 50 m of a substation polygon or point → `transmits_power`. Labelled likely; confirmed when both line ends hit substations.
  4. `plant-substation`, proximity: a generation node → nearest substation within `min(5 km, 2 × sqrt(capacityMw) km)` → `supplies_power`, possible.
  5. `substation-critical-load`, proximity: hospital, data centre, airport, water facility or port → nearest substation within 3 km (urban) or 10 km (others; urban = inside a GeoNames populated place with population > 100k from 040) → `supplies_power` toward the load, possible. When ≥ 2 substations lie within range, attach the two nearest, each `speculative`.
  6. `airport-port-serves`, inferred: airports and ports → the region they serve (admin1 tag) → `serves`, likely.
  - Each rule has `enabled` and parameters overridable through `app_setting['infra.rules']`.

### 3. Affected-dependents query

- `InfraImpactService.affected(area | eventId | recordRef, depth ≤ 3, minLabel, types[])`:
  - **Seeds:** nodes whose `geom` intersects the area. For an event, use its `geom` (101). For a record (an IODA outage region, a power outage polygon), use its geometry, or the region polygon from 040 when the geometry is a centroid. Mark region-derived seeds as `approximateArea`.
  - **Traversal:** a recursive CTE over `infra_edge` in the dependency direction (power flows `from → to`; `lands_at` is bidirectional), keeping only edges with label ≥ `minLabel` (default `possible`).
  - **Output:**
    - `direct[]` (inside the area);
    - `dependents[]` with `{node, depth, pathLabel (weakest), pathScore (product), path [{edgeId, relation, method, label}]}`;
    - `summary` by facility type and label;
    - `caveats[]` (always "open data, heuristic links"; "region-level area" when approximate; "OSM extract dated <file mtime>"; rules disabled);
    - `attribution[]`;
    - `buildId`.
  - Limits: 5,000 nodes and 200 ms target. Above the limit, truncate by pathScore and set `truncated`.
- **Integration with 101:**
  - Add a `dependents` section to `ExposureService` (summary counts by label).
  - The `infrastructure-disruption` recipe uses affected facilities as **context** members only, never as supporting evidence.

### 4. API and MCP

- `GET /v1/infra/nodes?bbox&type&q&cursor` (read)
- `GET /v1/infra/nodes/{id}` (read; with in and out edges)
- `GET /v1/infra/edges?bbox&method&label&cursor` (read)
- `POST /v1/infra/affected` (read; body `{geometry | eventId | record: {sourceId, externalId}, depth, minLabel, types}`)
- `GET /v1/infra/status` (read; the last build and counts)
- `POST /v1/infra/rebuild` (admin; 202)
- `GET/PUT /v1/settings/infra-rules` (admin)
- GeoJSON output is supported for nodes and edges (`Accept: application/geo+json`).
- **MCP:** `get_affected_infrastructure` (read; compact output: summary, top 20 dependents, caveats first) and `get_infra_node` (read).
- **Problem codes:** `UNKNOWN_INFRA_NODE` (404); `INFRA_GRAPH_NOT_BUILT` (409); `INFRA_QUERY_TOO_LARGE` (400, area > 2,000,000 km² without `types`).

### 5. Spec, Postman, docs

- `docs/OpenAPI.json`:
  - schemas `InfraNode`, `InfraEdge`, `InfraImpactRequest`, `InfraImpactResult`, `InfraBuild`, `InfraRules`;
  - tag `Infrastructure`; `x-foresignal-scope` on every operation;
  - regenerate Postman with BODIES for `computeAffectedInfrastructure`, `rebuildInfraGraph` and `updateInfraRules`.
- AccessPolicy rows as above.
- `docs/Source-Catalog.md`: an `osm-infrastructure` row (inbox, ODbL, no network).
- `docs/Operator-Guide.md` "Infrastructure graph":
  - downloading a Geofabrik extract for your region (refresh monthly);
  - the osmium commands; why not Overpass;
  - ODbL obligations (attribution; share-alike for the derived graph if you redistribute it);
  - edge labels and how to read results.
- `THIRD_PARTY_NOTICES.md`: OpenStreetMap (ODbL) as a data notice. osmium is listed as an optional external tool with no code included.

## Tests to write (do not run)

- `OsmInfraParserContractTest`: the fixture GeoJSONSeq → records (tags allowlist, voltage parsing `110000;220000` → 220 kV, minVoltage filter, geometry simplification, ids).
- `OsmInfraAdapterIT`: unchanged sha → no import; changed file tombstones removed features; feature cap → health reason; no network calls (the client is not invoked).
- `DependencyRulesTest`: each rule on synthetic nodes (landing declared, line endpoints topological, proximity radius with capacity, urban vs rural radius, two-substation speculative split); labels never exceed the method cap.
- `InfraGraphBuilderIT`: staging swap is atomic (a concurrent reader sees the old or the new graph); a rebuild is deterministic (same inputs → same edges and params); build record counts.
- `InfraImpactServiceIT`: direct and depth-2 dependents with the weakest-link label; `minLabel` filtering; a region-derived seed flagged approximate; truncation; attribution includes OSM when present.
- `InfraControllersIT`: endpoints, GeoJSON output, scopes, 409 before the first build, MCP tool shape.

## Acceptance criteria

- The graph builds from 094 datasets alone (OSM optional) and from an OSM inbox file, with every edge carrying method, label, rule version and evidence.
- The affected-dependents query returns paths with weakest-link labels, caveats and attribution.
- No code path calls the public Overpass API or any OSM web API. OSM-derived output is labelled ODbL.
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
2. **Commit** all changes with the message `104: Infrastructure Dependency Mapping`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/104-CR-Infrastructure-Dependency-Mapping.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `104: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
