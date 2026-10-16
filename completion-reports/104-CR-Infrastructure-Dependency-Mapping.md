# 104-CR-Infrastructure-Dependency-Mapping

| | |
|---|---|
| Prompt | 104-Infrastructure-Dependency-Mapping.md |
| Status | Complete, with the deviations below |
| Implementation commit | `c924d4cd338031dfc79cfd8e7d47e6050d6ca411` |
| Post-review fix commit | `7fac186` (`104: fix post-review defects`) |
| Date | 2026-10-15 (implementation); 2026-10-16 (post-review fixes) |

## Summary

Added the infrastructure dependency graph (`ai.foresignal.infra`) and the `osm-infrastructure`
source. Compiles; tests written, not run. The original report misstated several labels and features
(see "Post-review fixes"); this report describes the code after the 2026-10-16 fixes.

- **Graph model:** `infra_node` (facility type, geom, representative point, attributes, region tags
  from `record_region`, licence, built_at), `infra_edge` (relation, method, label, score, rule id,
  rules version, effective params, evidence `{dataset, distanceKm, matched, notes}`, licence) and
  `infra_build` (status running/done/failed, rules version, inputs `{source: lastSeq}`, counts by
  method and label, warnings, OSM extract date, error). Indexes (`InfraSchemaInitializer`): GiST on
  `geom` and `point`, `(facility_type)`, build lookups, unique `(build_id, source_id, external_id)`,
  unique `(from_node, to_node, relation, rule_id)` and the traversal indexes.
- **Builder** (`InfraGraphBuilder`): reads `infra/node-mapping.json` and the versioned
  `infra/dependency-rules.json` (now version 2) with the operator overrides of
  `app_setting['infra.rules']` (`enabled` and `params` only; method and label cannot be overridden,
  and proximity is capped at `possible`). Nodes are read in a fixed order (mapping order, then
  `external_id`; up to 1,000,000 per source and kind, with a warning at the cap) and written with
  JDBC. Each edge records its rule, the rules version, the effective params, evidence and the most
  restrictive licence of its two nodes (from the 109 licence catalog). A build is started
  (`running`), executed in one transaction that also deletes every superseded build's nodes and
  edges, and marked done; a failure records the error and the previous graph stays current. Readers
  read only the newest successful build. Same inputs → same nodes and edges.
- **Rules:** `cable-landing` (geometry match: the cable route within `withinKm` of the landing point →
  `lands_at`, topological, likely); `line-substation` (endpoint within `withinMeters` of a substation's
  geometry → `transmits_power`, likely; confirmed when both ends reach different substations);
  `plant-substation` (nearest substation within `min(maxKm, capacityFactorKm × √capacityMw)` →
  `supplies_power`, possible); `substation-critical-load` (nearest substation within the radius →
  possible; two nearest, each speculative, when two or more are in range). Candidate searches use a
  JTS R-tree.
- **Rebuilds** (`InfraRebuildScheduler`): `POST /v1/infra/rebuild` answers 202 with the running build
  and builds on a background worker; a rebuild runs 10 minutes after the last import of a mapped
  source (ChangeBus subscription) and weekly (`foresignal.infra.rebuild-cron`); both automatic paths
  are off with `foresignal.infra.auto-rebuild=false` (set in the test profile).
- **Impact query** (`InfraImpactService`): seeds from a geometry, an intelligence event (a private
  event needs the private grant; 404 `UNKNOWN_EVENT` otherwise) or a record (its geometry, or its 040
  admin1/country polygon when it is a point → `approximateArea`). Traversal is level by level in SQL
  (≤ 3 depths, chunked id lists) over edges at or above `minLabel`; `lands_at`, `interconnects` and
  `transmits_power` are walked both ways, so a power line connects the substations at its ends. Node
  details are read in batched queries. Output: `direct`, `dependents` (weakest-link label, product
  score, path), `summary` (counts, `byType`, `byLabel`), caveats (open data; region-level area; OSM
  extract date; disabled rules; truncation), attribution for every contributing source
  ("© OpenStreetMap contributors" for OSM), `buildId`, `truncated` (5,000 nodes). `types` filters
  the facilities listed.
- **Reads** (`InfraNodeQuery`, used by REST and MCP): nodes (`bbox`, `type`, `q`), node with in/out
  edges, edges (`bbox`, `method`, `label`), all from the current build, paged by an opaque id cursor
  bound to the build, each with attribution. 409 `INFRA_GRAPH_NOT_BUILT` before the first build.
- **OSM source:** `osm-infrastructure` reads an RFC 8142 GeoJSON sequence from the inbox, never the
  network (no Overpass, no `openstreetmap.org`). The file is hashed and parsed as streams; it imports
  only when the SHA-256 changed; a file over 2,000,000 features is refused with a health reason;
  invalid lines are counted ("parse errors: n"); the extract's modification time is recorded for the
  graph's caveat. Allowlisted tags; `voltage` → `voltageKv` (max of a `;`-list);
  `plant:output:electricity` → `capacityMw` with its unit (W, kW, MW, GW; the OSM key has no default
  unit, so `yes` or a bare number is unknown). Power lines under `minVoltageKv` (110) are dropped.
  ODbL 1.0 with attribution "© OpenStreetMap contributors".
- **API/MCP/spec:** `InfraController`, `InfraRulesController`; MCP `get_affected_infrastructure`
  (geometry, eventId or record; caveats first, top 20 dependents) and `get_infra_node`; problem codes;
  `AccessPolicy` rows; the prompt-104 operations flipped to `implemented`; Postman regenerated in the
  implementation commit; Source-Catalog row; Operator-Guide section; THIRD_PARTY_NOTICES entry.

## Files created / modified

Implementation commit — created (main): `infra/` (`InfraNodeEntity`, `InfraEdgeEntity`,
`InfraBuildEntity` and their repositories, `InfraGraphBuilder`, `InfraImpactService`),
`api/InfraController`, `api/InfraRulesController`, `source/adapters/osm/` (`OsmInfrastructureAdapter`,
`OsmInfraParser`), the two MCP tools, `resources/infra/{node-mapping.json,dependency-rules.json}`;
tests `DependencyRulesTest`, `InfraGraphBuilderIT`, `InfraImpactServiceIT`, `InfraControllersIT`,
`OsmInfraParserContractTest`, `OsmInfraAdapterIT`. Modified: `ErrorCodes`, `AccessPolicy`,
`docs/OpenAPI.json`, `pending-operations.txt`, `postman/`, `docs/Source-Catalog.md`,
`docs/Operator-Guide.md`, `THIRD_PARTY_NOTICES.md`.

Post-review commit — created: `infra/InfraRules`, `InfraLicences`, `InfraNodeQuery`,
`InfraRebuildScheduler`, `InfraSchemaInitializer`. Modified: the infra package, the OSM adapter and
parser, the two MCP tools, `InfraController`, `dependency-rules.json`, the infra and OSM tests,
`src/test/resources/application-test.yml` (`foresignal.infra.auto-rebuild: false`) and
`docs/Operator-Guide.md` (infrastructure section).

## Deviations from the prompt

- **The build swap is a build-id swap with deletion, not a table rename.** Nodes and edges carry
  `build_id`; readers read the newest successful build; the transaction that finishes a build also
  deletes the superseded builds' rows, so readers see the old graph or the new one.
- **`cable-landing` is topological/likely, not declared/confirmed.** The 094 submarine-cable payload
  (`name`, `color`, `isTbd`) does not list a cable's landing points, so the link is a geometry match.
- **`ixp-facility`** produces no edges (no PeeringDB facility data is mapped) and **`airport-port-serves`**
  produces none (the graph has no region nodes); each records a build warning.
- **Urban radius:** `substation-critical-load` uses the rural radius everywhere (the reference data has
  no populated-place polygons); the build records a warning.
- **Traversal** is a level-by-level SQL walk, not a single recursive CTE (a CTE enumerates every path;
  the walk keeps one best path per node).
- **ExposureService `dependents` section and the `infrastructure-disruption` recipe's context members
  are not wired** (both live in `intel/`, outside this prompt's fix scope).
- **GeoJSON output** (`Accept: application/geo+json`) for nodes and edges is not implemented.
- **Node licences** now come from the 109 licence catalog (`CC-BY-4.0`, `ODbL-1.0`, …) instead of the
  hard-coded strings of the implementation commit. Node `attributes` are returned as objects and
  `edgesByMethod`/`edgesByLabel` in the status as objects (they were JSON text).
- **Rebuild while a build runs** returns 202 with the running build rather than 409 (the spec's 409
  names no problem code).

## Issues found / open questions

- A build left `running` by a restart is ignored after 6 hours (`STALE_BUILD`), so a new rebuild can
  start; no restart recovery marks it failed.
- 109's egress policy should carry the ODbL share-alike obligation for exports of the derived graph
  (110, 111, 113); not added here.

## Tests written (not run)

- `OsmInfraParserContractTest` — allowlisted tags; voltage list → max kV; minVoltage filter; kinds;
  capacity `500 MW` → 500; units W/kW/MW/GW, `yes`/bare/unknown → null; streamed parse counts parse
  errors and skips record separators; feature cap.
- `OsmInfraAdapterIT` — descriptor ODbL, RECEIVED, no TTL, OSM attribution.
- `DependencyRulesTest` — cable landing topological/likely; line one end likely, both ends confirmed;
  capacity-scaled plant radius; one substation possible, two speculative; overrides change enabled
  and params only; duplicate edges dropped; label ceilings; most restrictive licence; distances.
- `InfraGraphBuilderIT` — nodes, inputs, counts, rules version, edge licence and params; a second
  rebuild is identical and replaces the first; an override disables a rule with a warning.
- `InfraImpactServiceIT` — 409 before a build; depth-2 dependents with the weakest-link label; OSM
  attribution and caveats; `minLabel` and `types`; private event 404; record seed approximate; 404
  for an unknown record.
- `InfraControllersIT` — 409 and status before a build; current build only, cursor paging, type and
  bbox filters, edges with params/evidence/licence and OSM attribution; superseded node 404;
  rebuild 202 and status; affected needs exactly one seed; MCP tool caveats first; rules settings.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (post-review).

## Notes for the next prompt

- Read the graph through `InfraNodeQuery` / `InfraImpactService` (current build only); start
  rebuilds through `InfraRebuildScheduler.requestRebuild()`.
- 101's `ExposureService` should gain the `dependents` section; 109 should add the ODbL egress row.
- No code path calls the Overpass API or any OSM web API.

## Post-review fixes (2026-10-16)

Every finding was re-verified against the code before it was changed.

- **Capacity unit (confirmed):** `capacityMw` stripped the unit and divided by 1,000,000 (`500 MW` →
  0.0005); it now parses the unit. The contract test's expectation (500.0) was right and now holds.
- **No build filter (confirmed):** list/get read every build and `deleteByBuildId` had no caller, so
  N rebuilds gave N copies; reads now serve the current build and a build deletes superseded rows.
- **Whole extract in memory (confirmed):** `Files.readAllBytes` for the hash and a full line list
  before the cap; both are now streams with the cap enforced while reading.
- **Cable landing labels and edge licence (confirmed):** geometry-only matches were labeled
  declared/confirmed and every edge licence was hard-coded `ODbL-1.0`; now topological/likely and the
  most restrictive input licence.
- **Medium:** `line-substation` was `possible` (prompt: likely, confirmed on both ends) — fixed; lines
  were dead ends in traversal — `transmits_power` is walked both ways; `/affected` loaded the whole edge
  set and queried each node — now per-level SQL and batched node reads; rebuilds were not reproducible
  (`LIMIT` without `ORDER BY`, empty params, no inputs) — ordered, params and inputs recorded; the
  "202" rebuild ran synchronously — now background.
- **Low:** private events are enforced; attribution is per contributing source; parse errors are
  counted with the health reason.
- **Undisclosed gaps implemented:** indexes, scheduled and import-debounced rebuilds, record seeds,
  the `types` filter, `bbox` and `cursor` on nodes and edges, rule overrides applied.
- **CR corrections:** the original said `line-substation` was likely (code: possible), that health
  reasons covered parse errors (they did not), that rule params/version were recorded (params were
  empty, overrides unused), and that cable landings were declared (they were geometry matches).
