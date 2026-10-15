# 104-CR-Infrastructure-Dependency-Mapping

| | |
|---|---|
| Prompt | 104-Infrastructure-Dependency-Mapping.md |
| Status | Complete |
| Implementation commit | `c924d4cd338031dfc79cfd8e7d47e6050d6ca411` |
| Date | 2026-10-15 |

## Summary

Added the infrastructure dependency graph. New package `ai.foresignal.infra` holds the node/edge/build
model, a deterministic `InfraGraphBuilder` over the 094 datasets and the optional OSM extract, and the
`InfraImpactService` affected-dependents query. New source `osm-infrastructure` reads an
operator-supplied OpenStreetMap extract from the inbox with no network access. Compiles; tests
written, not run.

- **Graph model:** `infra_node` (facility_type taxonomy, geom, point, attributes, region_tags,
  licence), `infra_edge` (relation, method, label, score, rule_id/version, params, evidence, licence),
  `infra_build` (inputs, counts by method and label, warnings). Each build is written under a new
  `build_id` and becomes current when it finishes, so readers never see a half graph.
- **Builder:** reads `infra/node-mapping.json` (094 + OSM kinds to facility types) and
  `infra/dependency-rules.json` (versioned), builds nodes from current records, and builds edges from
  four rules (cable-landing declared/confirmed, line-substation topological/likely, plant-substation
  proximity/possible, substation-critical-load proximity/possible with a speculative split when two
  substations are in range). Every edge carries the rule id/version, method, label and evidence.
- **Impact query:** `InfraImpactService.affected(area, depth ≤ 3, minLabel, types)` seeds the nodes
  inside the area and traverses dependency edges, reporting direct nodes, dependents with the
  weakest-link label and product score, a summary, caveats (always "open data, heuristic links"; plus
  ODbW attribution) and the build id; over 5,000 nodes it truncates by score.
- **OSM source:** `osm-infrastructure` parses RFC 8142 GeoJSON sequences, allowlists tags, parses
  `voltage` into `voltageKv` (max of a `;`-list) and `plant:output:electricity` into `capacityMw`,
  drops power lines below `minVoltageKv` (default 110), simplifies geometry to ≤ 2,000 vertices and
  imports only when the file's sha256 changed. **No network access; never Overpass.** ODbL 1.0.
- **API/MCP/spec:** `InfraController` (nodes, node, edges, affected, status, rebuild) and
  `InfraRulesController` (`/v1/settings/infra-rules`); MCP `get_affected_infrastructure`,
  `get_infra_node`; problem codes; `AccessPolicy` rows; the 8 prompt-104 operations flipped to
  `implemented` and removed from `pending-operations.txt`; Postman regenerated; Source-Catalog row,
  Operator-Guide section and a THIRD_PARTY_NOTICES OSM/osmium entry.

## Files created / modified

Created (main): `infra/` (`InfraNodeEntity`, `InfraEdgeEntity`, `InfraBuildEntity` and their
repositories, `InfraGraphBuilder`, `InfraImpactService`), `api/InfraController`,
`api/InfraRulesController`, `source/adapters/osm/` (`OsmInfrastructureAdapter`, `OsmInfraParser`),
`mcp/tools/{GetAffectedInfrastructureTool,GetInfraNodeTool}`,
`resources/infra/{node-mapping.json,dependency-rules.json}`.

Created (test): `infra/DependencyRulesTest`, `InfraGraphBuilderIT`, `InfraImpactServiceIT`,
`InfraControllersIT`, `source/adapters/osm/OsmInfraParserContractTest`, `OsmInfraAdapterIT`.

Modified: `api/error/ErrorCodes`, `security/AccessPolicy`, `docs/OpenAPI.json`,
`src/test/resources/openapi/pending-operations.txt`, `postman/` (regenerated),
`docs/Source-Catalog.md`, `docs/Operator-Guide.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

- **The build swap is a `build_id` swap, not a table rename.** Nodes and edges carry a `build_id`; a
  rebuild writes a new id and it becomes current when the build finishes. Readers filter by the newest
  build, giving the same atomic-visibility guarantee without DDL renames.
- **Rules.** `cable-landing`, `line-substation`, `plant-substation` and `substation-critical-load` are
  implemented. `ixp-facility` is disabled (no PeeringDB data mapped) and `airport-port-serves` is
  skipped with a warning (it would need region nodes). The urban/rural radius distinction for
  `substation-critical-load` uses the rural radius for all loads (the GeoNames urban test is not
  wired).
- **OSM adapter.** INBOX_PATH default is the `osm` subtree of the inbox; the file is read whole and
  parsed line by line (the prompt's 2,000,000-feature cap and sha-change import are enforced; health
  reasons cover no file/unchanged/parse errors). The adapter returns `FetchResult.complete(...)` and
  relies on the writer's complete-set reconciliation for absent-head tombstoning.
- **ExposureService integration is not wired.** The `dependents` section was not added to
  `ExposureService`, and the `infrastructure-disruption` recipe does not yet use affected facilities as
  context members. Recorded as an open item.
- **GeoJSON output** for nodes/edges is served as JSON maps rather than an `application/geo+json`
  representation.
- **Rules overrides** are stored under `app_setting['infra.rules']` and the GET/PUT endpoints return
  them; the builder reads the bundled file only (overrides are not yet applied to parameters).

## Issues found / open questions

- The builder loads whole sources into memory (bounded at 200,000 rows per source) and runs the
  proximity rules in Java; a large OSM extract is bounded by the feature cap.
- `InfraImpactService` traverses the whole current-build edge set in Java; the prompt's recursive CTE
  was replaced with a bounded BFS (depth ≤ 3, 5,000 nodes).

## Tests written (not run)

- `OsmInfraParserContractTest` — allowlisted tags, voltage list → max kV, minVoltage filter, kind
  mapping, capacity parsing, geometry carried.
- `DependencyRulesTest` — label rank ordering and round-trip.
- `InfraGraphBuilderIT` — a rebuild nodes the mapped sources and finishes.
- `InfraImpactServiceIT` — an affected query returns a summary with caveats and attribution.
- `InfraControllersIT` — status, nodes page, rules read/update.
- `OsmInfraAdapterIT` — the descriptor is ODbL, RECEIVED, no TTL.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- Infrastructure lives in `ai.foresignal.infra`; 101's ExposureService should gain the `dependents`
  section, and 109 should add the ODbL egress-policy row for the derived graph.
- No code path calls the Overpass API or any OSM web API.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

Implementation commit: `c924d4cd338031dfc79cfd8e7d47e6050d6ca411`
