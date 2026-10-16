# 094-CR-Reference-Datasets

| | |
|---|---|
| Prompt | 094-Reference-Datasets.md |
| Status | Complete (post-review fixes 2026-10-16) |
| Implementation commit | b00df1651dfb96adb05bb12480333e03668b594c |
| Post-review fix commits | 94a06bdd0c778319829d3d1be589a805432ddfea (framework), e59d3908c86551cb30ce3477ec7beb75d37bc977 (094) |
| Date | 2026-10-14; corrected 2026-10-16 |

## Summary

The slow-changing reference layers land as ordinary records polled weekly, versioned by complete-set
reconciliation, plus two framework pieces the rest of the roadmap builds on.

**Shared framework (`ai.foresignal.collect`, `ai.foresignal.ingest`).**
- `FetchContext.emitChunk(List<NormalizedRecord>)` (≤ 5,000 records; `DefaultFetchContext.chunkSink`). The
  `FetchExecutor` installs a per-run `RunChunks` sink, submits each chunk as its own `IngestBatch` with a
  shared `run_id`, and awaits the previous chunk before accepting the next (back-pressure). `FetchResult`
  gained a `release` (the dataset's own version: ETag, Last-Modified or version string) and, after the review,
  `reconcileKinds` (`FetchResult.completeFor`) for multi-file sources.
- `IngestBatch.forceTouch`: every batch of a complete-set run writes an unchanged record as TOUCH, not
  TOUCH_THROTTLED, so `record_live.last_received_at` always advances inside `touchMin`.
- `TombstoneAbsent(sourceId, kinds, runStartedAt, maxAbsentRatio, allowShrink, runId, future)` (new sealed
  `IngestCommand`) + `TombstoneOutcome`. `IngestWriter.processTombstoneAbsent` computes the candidate/total
  ratio, trips the shrink guard (writes nothing; the executor then degrades health "dataset shrank by N% —
  reconciliation skipped" and writes a `DISCARDED` `fetch_log` row carrying the run's `run_id`), or writes
  tombstones through the normal fold (TOMBSTONE) in chunks of 10,000, re-enqueuing itself at the queue tail like
  `HeadPurge`; the tombstone batches' `fetch_log` rows carry the run's `run_id`. A reconciliation that runs
  clears a degradation the guard set. A 304, a failed run and a partial run never reconcile.
- `dataset-release` record after reconciliation: `upstreamVersion`, `records`, `appeared` (FIRST+REAPPEAR),
  `changed` (SIGNIFICANT+CHANGE), `disappeared` (TOMBSTONE), `runId`.
- `fetch_log.run_id` (Hibernate entity + both inserts), `FetchLogEntry.runId`, `FetchLogEntryDto.runId`.
- `SourceDescriptor.reconcileMaxAbsentRatio` (default 0.2, internal) and `ReferenceSupport.defaults`
  (STANDARD, ALL, no TTL, RECEIVED, `zeroResultsNormal=false`, 7 d/1 d, 2/min + 20/day, 730 d).
- `allowShrink`: the operator's re-run sends no stored conditional-GET validators, so it re-reads every file
  and reconciles (before the review a conditional-GET source answered it with a 304 and never reconciled).

**Datasets (all STANDARD/ALL, no TTL, RECEIVED, 730 d, `dataset-release`; disabled by default).**
`wri-power-plants` (streaming CSV, frozen v1.3.0), `ourairports` (params `types`, `navaids`; streaming CSV),
`submarine-cables` (non-commercial, LOCAL_ONLY), `gvp-volcanoes` (WFS GeoJSON, BCE years), `world-port-index`
(decimal `ycoord`/`xcoord` + 64-vertex geodesic `port-area` buffers for 099, split at the antimeridian),
`nps-boundaries` (073 ArcGIS kit, `editingInfo` short-circuit, JTS simplification), `spaceports` (GCAT
`sites.tsv`/`lp.tsv`), `nuclear-reactors-us` (FEMA, 24 h, min 6 h) and `nuclear-plants-wikidata` (fixed,
aggregated SPARQL, CC0).

**Streaming.** As implemented in b00df16, only `wri-power-plants` and `ourairports` streamed into chunks; the
other seven parsed the whole document into a tree and a full `List<NormalizedRecord>` before
`ReferenceSupport.emit` cut it into chunks. Since e59d390 every parser streams: the JSON sources walk the
document with `ReferenceStreams.forEachElement` (one element tree at a time) and the TSV source reads line by
line, all into a `ReferenceSupport.ChunkEmitter`; `nps-boundaries` maps the 073 kit's features straight into
chunks (the kit itself still holds the layer's 442 feature trees while paging). The response body itself is a
byte array (the `SourceHttp` API), at most the descriptor's response cap.

**API / spec / docs.** `POST /v1/sources/{sourceId}/fetch` gains `allowShrink` (query) and `FetchLogEntry`
gains `runId`, both already in `docs/OpenAPI.json`; a 094 `info.x-foresignal-changelog` entry was added, and the
`allowShrink` description now says the run re-reads every file. Postman regenerated at 094 (no diff); the
coordinator regenerates it after the review. `docs/Source-Catalog.md` rows and a "Reference datasets
(prompt 094)" section with the exclusions; `docs/Adapter-Guide.md` §25 "Reference datasets";
`docs/Operator-Guide.md` "Reference datasets (prompt 094)" (`allowShrink`, the cable/volcano licence flags).

## Files created / modified

**Created, main**
- `ingest/{TombstoneAbsent,TombstoneOutcome}.java`
- `source/support/ReferenceSupport.java`; `source/support/ReferenceStreams.java` (review)
- `source/adapters/wri/{package-info,GppdParser,WriPowerPlantsAdapter}.java`
- `source/adapters/ourairports/{package-info,OurAirportsParser,OurAirportsAdapter}.java`
- `source/adapters/submarinecables/{package-info,SubmarineCableParser,SubmarineCablesAdapter}.java`
- `source/adapters/gvp/{package-info,GvpWfsParser,GvpVolcanoesAdapter}.java`
- `source/adapters/worldportindex/{package-info,WpiParser,WorldPortIndexAdapter}.java`
- `source/adapters/nps/{package-info,NpsBoundaryParser,NpsBoundariesAdapter}.java`
- `source/adapters/spaceports/{package-info,GcatParser,SpaceportsAdapter}.java`
- `source/adapters/fema/{package-info,FemaReactorParser,NuclearReactorsUsAdapter}.java`
- `source/adapters/wikidata/{package-info,WikidataNuclearParser,NuclearPlantsWikidataAdapter}.java`

**Modified, main**
- `source/spi/{FetchContext,FetchResult,SourceDescriptor}.java`; `source/spi/SourceHttp.java` (review: `post`
  with a request timeout)
- `collect/{DefaultFetchContext,FetchExecutor,FetchLogWriter,ManualFetchService}.java`;
  `collect/BoundSourceHttp.java` (review)
- `ingest/{FetchLogEntry,IngestBatch,IngestCommand,IngestQueue,IngestWriter,BatchFold}.java`
- `store/FetchLogEntity.java`, `api/dto/FetchLogEntryDto.java`, `api/mapper/FetchLogMapper.java`
- `api/SourcesController.java` (`allowShrink` request parameter)

**Created, test**
- `collect/ChunkedFetchIT.java`, `ingest/TombstoneAbsentIT.java`
- `testsupport/adapters/{ChunkedTestAdapter,ReferenceTestAdapter}.java`;
  `testsupport/adapters/ConditionalReferenceTestAdapter.java` (review)
- `source/adapters/{wri,ourairports,submarinecables,gvp,worldportindex,nps,spaceports,fema,wikidata}/…Test.java`
- `source/adapters/reference/ReferenceDescriptorsTest.java`; `source/support/ReferenceSupportTest.java` (review)
- fixtures under `src/test/resources/fixtures/{wri-power-plants,ourairports,submarine-cables,gvp-volcanoes,world-port-index,nps-boundaries,spaceports,nuclear-reactors-us,nuclear-plants-wikidata}/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (nine reference adapters classified)
- `testsupport/adapters/AdapterHttpHarness.java` (chunk-sink context overload)
- `ingest/IngestTypesTest.java` (the new sealed case)

**Docs**: `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/Operator-Guide.md`.

## Deviations from the prompt

1. **`TombstoneAbsent` carries `allowShrink` and `runId`.** The prompt's signature omitted both; the operator's
   one-off override has to travel from `POST …/fetch?allowShrink=true` to the writer, and the tombstone
   batches' `fetch_log` rows need the run id to join the run's chunk rows.
2. **Chunk batches always force-touch.** A chunk is emitted before `complete` is known, so a chunked
   emission (only reference adapters use it) implies a complete-set run and force-touches; the final
   non-chunked batch force-touches only when the run is reconcilable (declares `dataset-release`).
3. **`dataset-release` is gated on the descriptor declaring the kind** (not a separate "reference" flag); the
   upstream version travels on `FetchResult.release` (added with a secondary factory).
4. **`reconcile.maxAbsentRatio` is a new internal `SourceDescriptor` component**
   (`reconcileMaxAbsentRatio`, default 0.2, clamped to `(0,1]`); it is not exposed over the API, so no
   OpenAPI/dto change.
5. **"Disabled by default" is the framework default.** Every built-in source seeds a disabled settings row
   (093's deviation 8), so no descriptor flag was added; `ReferenceDescriptorsTest` asserts the non-commercial
   flag and `LOCAL_ONLY` egress instead.
6. **NPS is a hand-written adapter using the 073 kit's pieces** (`ArcGisQueryClient`, `FeatureGeometry`)
   rather than a feature-kit spec, because a spec cannot set `quality.extra["geom.simplified"]`. The kit holds
   the layer's features while it pages; the adapter streams the records from them into chunks.
7. **FEMA query URL adds `&outSR=4326`** (the prompt's URL omitted it) so the geometry is WGS84.
8. **Wikidata "installed capacity" uses `P2109`** (nominal power capacity), read from its best-rank
   **normalized** value (`psn:P2109`, SI watts) and converted to MW; the query groups by item with fixed
   aggregates (see the review section).
9. **Fixtures are synthetic** (no network during the build); each README carries the URL and
   `synthetic: true — recapture per prompt 143`. The GVP attribution version/DOI is the prompt's stated
   citation (the citation page was not fetched).
10. **`dataset-release.records` is the run's total records seen**; `disappeared` includes reconciliation
    tombstones (`TombstoneOutcome.tombstoned()`) plus any per-record TOMBSTONE.
11. **The shrink guard's `DISCARDED` row is written by the executor** (status `DISCARDED`, the run's `run_id`,
    the guard's reason as `error`, 0 records), next to the run's `OK` chunk rows; the run's own outcome stays
    a success, because its records were ingested.
12. **Multi-file sources reconcile per kind.** Each file of `ourairports`, `spaceports` and
    `submarine-cables` is its own kind; a run reconciles the kinds whose file arrived fresh
    (`FetchResult.completeFor`), so a 304 on one file neither tombstones its records nor blocks the other's
    reconciliation.

## Issues found / open questions

1. **Tests not run** (guardrail 1); they run first at checkpoint 097, and the review fixes at the next
   checkpoint.
2. **Reconciliation writes its own `fetch_log` rows** (principal `reconcile`, carrying the run's `run_id`), so
   the run's `FetchOutcome` counts exclude the tombstones; the `dataset-release` record carries the count
   instead.
3. **Wikidata property ids (`P2109`, `P5817`) and the `psn:`/`wikibase:BestRank` query shape are unverified
   against live Wikidata** (no network during the build or the review); re-confirm at 143.
4. **`nuclear-plants-wikidata` has no conditional GET** (a SPARQL POST), so every run re-parses; a version is
   written only when the payload changes.
5. **`dataset-release` accumulates one version per run** (the externalId is the release), which is the intent:
   a rule or briefing notices a release. If a deployment finds the churn unwelcome, the release could be
   anchored to the source id instead.
6. **Instructions in data:** the GVP geologic summary and the cable names are upstream text; they are data,
   never instructions (028).

## Tests written (not run)

- **Parser contracts with fixtures:** `GppdParserTest` (row skip, capacities, other fuels),
  `OurAirportsParserTest` (type filter, navaids, no-coordinate row), `SubmarineCableParserTest` (cable/landing,
  isTbd), `GvpWfsParserTest` (BCE years, null summary; streaming with skipped and malformed-geometry counts),
  `WpiParserTest` (decimal vs DMS, buffer radius by size, port-area vertices, no-coordinate port),
  `NpsBoundaryParserTest` (Polygon/MultiPolygon, a generated 12,000-vertex polygon simplified to ≤ 10,000 with
  `geom.simplified`), `GcatParserTest` (# header, `-` blanks, no-geometry row, missing header error),
  `FemaReactorParserTest` (power percentages), `WikidataNuclearParserTest` (Qid, WKT point, omitted optionals,
  pinned and aggregated query, watts to MW, duplicate Qid dropped, missing bindings). Every one calls
  `FieldSpecContract.assertPayloadKeysDeclared`.
- **`ReferenceSupportTest`:** chunk emitter sizes, antimeridian split of a geodesic area, longitude
  normalization, nested-array streaming and its format errors.
- **WireMock adapter tests:** `WriPowerPlantsAdapterTest` (200 into chunks, 304 skips, non-200), `OurAirportsAdapterTest`
  (default types, navaids, a 304 on airports still reconciles navaids), `SubmarineCablesAdapterTest` (both
  files; a 304 on one file reconciles only the other's kind; both 304 is a 304 run), `GvpVolcanoesAdapterTest`
  (Last-Modified release, 304), `WorldPortIndexAdapterTest` (ports + areas), `NpsBoundariesAdapterTest`
  (`editingInfo` short-circuit verifies one query), `SpaceportsAdapterTest` (sites + pads; a 304 on sites still
  reconciles pads), `NuclearReactorsUsAdapterTest` (`outSR=4326`, descriptor validates),
  `NuclearPlantsWikidataAdapterTest` (POST + Accept + User-Agent, `429 Retry-After` backoff).
- **`ReferenceDescriptorsTest`:** STANDARD/ALL/RECEIVED, no TTL, 730 d, 7 d/1 d (24 h/6 h for
  `nuclear-reactors-us`), 2/min + 20/day, every source declares `dataset-release`, licence/terms/attribution,
  `submarine-cables` non-commercial and LOCAL_ONLY, the 0.2 shrink-guard default.
- **ITs:** `TombstoneAbsentIT` (`@IntegrationTest`, real executor + writer): a dropped record is one TOMBSTONE
  and a `dataset-release` with `disappeared=1`, the tombstone row carrying the run's `run_id`; a truncated run
  trips the shrink guard, writes nothing, degrades health and leaves a `DISCARDED` row with the run id;
  `allowShrink` overrides once and clears the degradation; a later clean complete run clears it too; on a
  conditional-GET source the `allowShrink` re-run sends no `If-None-Match` and reconciles, and the next ordinary
  run is a 304; a re-run inside `touchMin` force-touches and never tombstones; a 304 never reconciles; a
  reappearing record is a REAPPEAR. `ChunkedFetchIT`: 12,000 records arrive as three chunks with one `run_id`,
  and the first chunk's row is committed before the second is emitted (back-pressure).

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL** (at b00df16 and again after the review fixes).
- `docs/OpenAPI.json`: valid JSON; `fetchSourceNow.allowShrink` and `FetchLogEntry.runId` reconciled to the
  dto.

## Notes for the next prompt

1. **Next checkpoint:** run the 094 tests including the review additions (`TombstoneAbsentIT`'s conditional-GET
   and recovery cases, `ReferenceSupportTest`, the multi-file adapter cases). `FamilyDefaultsTest` has nine
   `reference` entries; nine fixture directories exist; `pending-operations.txt` is unchanged (094 adds no API
   operation).
2. **098 (`faa-registry`) and 099 (port buffers, airports)** build on `ctx.emitChunk`, `ReferenceSupport.chunks`,
   `ReferenceStreams`, `TombstoneAbsent`, `dataset-release` and the `port-area` buffers of `world-port-index`. A
   multi-file source returns `FetchResult.completeFor(freshKinds, …)`.
3. **144 (Flyway):** one new column, `fetch_log.run_id` (Hibernate entity, two inserts); no other schema
   change. `SourceDescriptor.reconcileMaxAbsentRatio` is in-memory only.
4. **143:** recapture the nine synthetic fixtures with trimmed real captures per their READMEs, and re-confirm
   the Wikidata property ids and the aggregated query.
5. **109/110:** `submarine-cables` is `LOCAL_ONLY` with `nonCommercial: true`; `gvp-volcanoes` is
   non-commercial (its licence text) but its egress is not restricted. The licence-policy prompt should read
   those flags.

## Post-review fixes (2026-10-16)

Commits: `94a06bdd0c778319829d3d1be589a805432ddfea` (framework: shared fetch path, including the executor and
writer parts of findings 1, 2, 4 and 6) and `e59d3908c86551cb30ce3477ec7beb75d37bc977` (094 adapters, tests
and docs).

| # | Finding | Fix |
|---|---|---|
| 1 | `allowShrink` ineffective on conditional-GET sources | Validators are staged with the fetch's other adapter state and committed only after its records are written; the `allowShrink` run's `BoundSourceHttp` sends no stored validators. `TombstoneAbsentIT.allowShrinkOnConditionalSource` with `ConditionalReferenceTestAdapter`. |
| 2 | Shrink-guard degradation never cleared | The executor clears a degradation whose reason starts with `TombstoneOutcome.SHRINK_REASON_PREFIX` whenever a reconciliation runs (override or clean run); other adapter reasons are left alone. Tested (`shrinkGuard`, `shrinkGuardRecovers`). |
| 3 | `nuclear-reactors-us` interval lost | The 24 h (min 6 h) interval is applied after `ReferenceSupport.defaults`, which had overridden it (the 097 checkpoint then removed the dead call); `ReferenceDescriptorsTest` asserts it. |
| 4 | No `DISCARDED` mark, reconcile rows without `run_id` | `TombstoneAbsent.runId` stamps the tombstone rows; a guard trip writes a `DISCARDED` row with the run id and reason. Tested. |
| 5 | Seven adapters materialized whole datasets | Streaming parsers into `ChunkEmitter` (see "Streaming" above). |
| 6 | Multi-file 304 blocked reconciliation | `FetchResult.reconcileKinds` / `completeFor`; the executor reconciles only the fresh kinds; both-304 returns `unchanged()`. |
| 7 | Wikidata duplicate bindings, unit-less capacity | `GROUP BY ?item` with `MIN` labels/coordinate, earliest inception, `MAX` best-rank `psn:P2109` watts → MW; duplicate Qids dropped in the parser. |
| 8 | Antimeridian, silent geometry loss, no 60 s timeout | `ReferenceSupport.geodesicArea` splits a port-area at ±180 (and `geodesicCircle` normalizes its center); GVP, NPS, FEMA and cables count and log malformed geometry; a new `SourceHttp.post(…, Duration)` gives the SPARQL request 60 s (the client default is 20 s) and the source a 120 s fetch timeout. |

CR corrections made in this document: the original claimed streaming for every dataset (only two streamed),
claimed the guard marked the run `DISCARDED` and that `allowShrink` overrode the guard on every source (neither
held for a conditional-GET source), and did not disclose the missing `run_id` on reconcile rows or the lost
`nuclear-reactors-us` interval.

Implementation commit: b00df1651dfb96adb05bb12480333e03668b594c
