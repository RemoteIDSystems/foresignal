# 094-CR-Reference-Datasets

| | |
|---|---|
| Prompt | 094-Reference-Datasets.md |
| Status | Complete |
| Implementation commit | b00df1651dfb96adb05bb12480333e03668b594c |
| Date | 2026-10-14 |

## Summary

The slow-changing reference layers land as ordinary records polled weekly, versioned by complete-set
reconciliation, plus two framework pieces the rest of the roadmap builds on.

**Shared framework (`ai.foresignal.collect`, `ai.foresignal.ingest`).**
- `FetchContext.emitChunk(List<NormalizedRecord>)` (≤ 5,000 records; `DefaultFetchContext.chunkSink`). The
  `FetchExecutor` installs a per-run `RunChunks` sink, submits each chunk as its own `IngestBatch` with a
  shared `run_id`, and awaits the previous chunk before accepting the next (back-pressure). `FetchResult`
  gained a `release` (the dataset's own version: ETag, Last-Modified or version string).
- `IngestBatch.forceTouch`: every batch of a complete-set run writes an unchanged record as TOUCH, not
  TOUCH_THROTTLED, so `record_live.last_received_at` always advances inside `touchMin`.
- `TombstoneAbsent(sourceId, kinds, runStartedAt, maxAbsentRatio, allowShrink, future)` (new sealed
  `IngestCommand`) + `TombstoneOutcome`. `IngestWriter.processTombstoneAbsent` computes the candidate/total
  ratio, trips the shrink guard (writes nothing, degrades health "dataset shrank by N% — reconciliation
  skipped"), or writes tombstones through the normal fold (TOMBSTONE) in chunks of 10,000, re-enqueuing itself
  at the queue tail like `HeadPurge`. A 304, a failed run and a partial run never reconcile.
- `dataset-release` record after reconciliation: `upstreamVersion`, `records`, `appeared` (FIRST+REAPPEAR),
  `changed` (SIGNIFICANT+CHANGE), `disappeared` (TOMBSTONE), `runId`.
- `fetch_log.run_id` (Hibernate entity + both inserts), `FetchLogEntry.runId`, `FetchLogEntryDto.runId`.
- `SourceDescriptor.reconcileMaxAbsentRatio` (default 0.2, internal) and `ReferenceSupport.defaults`
  (STANDARD, ALL, no TTL, RECEIVED, `zeroResultsNormal=false`, 7 d/1 d, 2/min + 20/day, 730 d).

**Datasets (all STANDARD/ALL, no TTL, RECEIVED, 730 d, `dataset-release`; disabled by default).**
`wri-power-plants` (streaming CSV, frozen v1.3.0), `ourairports` (params `types`, `navaids`), `submarine-cables`
(non-commercial, LOCAL_ONLY), `gvp-volcanoes` (WFS GeoJSON, BCE years), `world-port-index` (decimal
`ycoord`/`xcoord` + 64-vertex geodesic `port-area` buffers for 099), `nps-boundaries` (073 ArcGIS kit,
`editingInfo` short-circuit, JTS simplification), `spaceports` (GCAT `sites.tsv`/`lp.tsv`), `nuclear-reactors-us`
(FEMA, 24 h) and `nuclear-plants-wikidata` (fixed SPARQL, CC0).

**API / spec / docs.** `POST /v1/sources/{sourceId}/fetch` gains `allowShrink` (query) and `FetchLogEntry`
gains `runId`, both already in `docs/OpenAPI.json`; a 094 `info.x-foresignal-changelog` entry was added.
Postman regenerated (no diff). `docs/Source-Catalog.md` rows and a "Reference datasets (prompt 094)" section
with the exclusions; `docs/Adapter-Guide.md` §25 "Reference datasets"; `docs/Operator-Guide.md`
"Reference datasets (prompt 094)" (`allowShrink`, the cable/volcano licence flags).

## Files created / modified

**Created, main**
- `ingest/{TombstoneAbsent,TombstoneOutcome}.java`
- `source/support/ReferenceSupport.java`
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
- `source/spi/{FetchContext,FetchResult,SourceDescriptor}.java`
- `collect/{DefaultFetchContext,FetchExecutor,FetchLogWriter,ManualFetchService}.java`
- `ingest/{FetchLogEntry,IngestBatch,IngestCommand,IngestQueue,IngestWriter,BatchFold}.java`
- `store/FetchLogEntity.java`, `api/dto/FetchLogEntryDto.java`, `api/mapper/FetchLogMapper.java`
- `api/SourcesController.java` (`allowShrink` request parameter)

**Created, test**
- `collect/ChunkedFetchIT.java`, `ingest/TombstoneAbsentIT.java`
- `testsupport/adapters/{ChunkedTestAdapter,ReferenceTestAdapter}.java`
- `source/adapters/{wri,ourairports,submarinecables,gvp,worldportindex,nps,spaceports,fema,wikidata}/…Test.java`
- `source/adapters/reference/ReferenceDescriptorsTest.java`
- fixtures under `src/test/resources/fixtures/{wri-power-plants,ourairports,submarine-cables,gvp-volcanoes,world-port-index,nps-boundaries,spaceports,nuclear-reactors-us,nuclear-plants-wikidata}/` (each with a README)

**Modified, test**
- `retention/FamilyDefaultsTest.java` (nine reference adapters classified)
- `testsupport/adapters/AdapterHttpHarness.java` (chunk-sink context overload)
- `ingest/IngestTypesTest.java` (the new sealed case)

**Docs**: `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Adapter-Guide.md`, `docs/Operator-Guide.md`.

## Deviations from the prompt

1. **`TombstoneAbsent` carries `allowShrink`.** The prompt's signature omitted it; the operator's one-off
   override has to travel from `POST …/fetch?allowShrink=true` to the writer, so the command carries a boolean.
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
   rather than a feature-kit spec, because a spec cannot set `quality.extra["geom.simplified"]`.
7. **FEMA query URL adds `&outSR=4326`** (the prompt's URL omitted it) so the geometry is WGS84.
8. **Wikidata "installed capacity" uses `P2109`** (nominal power capacity); the property ids were taken from
   the prompt and pinned in `WikidataNuclearParser.QUERY`.
9. **Fixtures are synthetic** (no network during the build); each README carries the URL and
   `synthetic: true — recapture per prompt 143`. The GVP attribution version/DOI is the prompt's stated
   citation (the citation page was not fetched).
10. **`dataset-release.records` is the run's total records seen**; `disappeared` includes reconciliation
    tombstones (`TombstoneOutcome.tombstoned()`) plus any per-record TOMBSTONE.

## Issues found / open questions

1. **Tests not run** (guardrail 1); they run first at checkpoint 097.
2. **Reconciliation writes its own `fetch_log` rows** (principal `reconcile`), so the run's `FetchOutcome`
   counts exclude the tombstones; the `dataset-release` record carries the count instead.
3. **Wikidata property ids (`P2109`, `P5817`) are unverified against live Wikidata** (no network during the
   build); re-confirm at 143.
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
  isTbd), `GvpWfsParserTest` (BCE years, null summary), `WpiParserTest` (decimal vs DMS, buffer radius by size,
  port-area vertices, no-coordinate port), `NpsBoundaryParserTest` (Polygon/MultiPolygon, a generated
  12,000-vertex polygon simplified to ≤ 10,000 with `geom.simplified`), `GcatParserTest` (# header, `-` blanks,
  no-geometry row, missing header error), `FemaReactorParserTest` (power percentages),
  `WikidataNuclearParserTest` (Qid, WKT point, omitted optionals, pinned query).
  Every one calls `FieldSpecContract.assertPayloadKeysDeclared`.
- **WireMock adapter tests:** `WriPowerPlantsAdapterTest` (200 into chunks, 304 skips, non-200), `OurAirportsAdapterTest`
  (default types, navaids), `SubmarineCablesAdapterTest` (both files; a 304 makes the run incomplete),
  `GvpVolcanoesAdapterTest` (Last-Modified release, 304), `WorldPortIndexAdapterTest` (ports + areas),
  `NpsBoundariesAdapterTest` (`editingInfo` short-circuit verifies one query), `SpaceportsAdapterTest` (sites +
  pads), `NuclearReactorsUsAdapterTest` (`outSR=4326`, descriptor validates), `NuclearPlantsWikidataAdapterTest`
  (POST + Accept + User-Agent, `429 Retry-After` backoff).
- **`ReferenceDescriptorsTest`:** STANDARD/ALL/RECEIVED, no TTL, 730 d, 7 d/1 d, 2/min + 20/day, every source
  declares `dataset-release`, licence/terms/attribution, `submarine-cables` non-commercial and LOCAL_ONLY, the
  0.2 shrink-guard default.
- **ITs:** `TombstoneAbsentIT` (`@IntegrationTest`, real executor + writer): a dropped record is one TOMBSTONE
  and a `dataset-release` with `disappeared=1`; a truncated run trips the shrink guard and writes nothing;
  `allowShrink` overrides once; a re-run inside `touchMin` force-touches and never tombstones; a 304 never
  reconciles; a reappearing record is a REAPPEAR. `ChunkedFetchIT`: 12,000 records arrive as three chunks with
  one `run_id`, and the first chunk's row is committed before the second is emitted (back-pressure).

## Compile result

- `./gradlew compileJava compileTestJava`: **BUILD SUCCESSFUL**.
- `docs/OpenAPI.json`: valid JSON; `fetchSourceNow.allowShrink` and `FetchLogEntry.runId` reconciled to the
  dto; Postman regenerated with no diff.

## Notes for the next prompt

1. **097 checkpoint:** first run of the 094 tests. `FamilyDefaultsTest` gained nine `reference` entries; nine
   fixture directories exist; `pending-operations.txt` is unchanged (094 adds no API operation).
2. **098 (`faa-registry`) and 099 (port buffers, airports)** build on `ctx.emitChunk`, `TombstoneAbsent`,
   `dataset-release` and the `port-area` buffers of `world-port-index`.
3. **144 (Flyway):** one new column, `fetch_log.run_id` (Hibernate entity, two inserts); no other schema
   change. `SourceDescriptor.reconcileMaxAbsentRatio` is in-memory only.
4. **143:** recapture the nine synthetic fixtures with trimmed real captures per their READMEs, and re-confirm
   the Wikidata property ids.
5. **109/110:** `submarine-cables` is `LOCAL_ONLY` with `nonCommercial: true`; `gvp-volcanoes` is
   non-commercial. The licence-policy prompt should read those flags.

Implementation commit: b00df1651dfb96adb05bb12480333e03668b594c
