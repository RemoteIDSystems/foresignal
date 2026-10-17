# 116-CR-Evidence-Identity-And-Observability

| | |
|---|---|
| Prompt | 116-Evidence-Identity-And-Observability.md |
| Status | Complete (owner decisions listed under Issues) |
| Implementation commit | `1f75f3865e4ab0daae33ac2e0a80af57dd1f7d86` |
| Date | 2026-10-16 |

## Summary

The evidence foundations of the intelligence layer: evidence identity and independence, observability, and
analysis manifests, with 099, 100, 101, 102 and 103 retrofitted to use them. Compiles; tests written, not run.

- **Originators (`ai.foresignal.evidence`):** `Originator` (id, class, independence group, aggregates, notes,
  domains) and `OriginatorRegistry`: bundled `intel/originators.json` (88 originators, 149 exact source mappings
  and 29 family patterns such as `cap-*`, `local-*`, `derived-*`, built from the adapters' real source ids), the
  operator setting `intel.originators` (same shape, merged over the file), and the descriptor's `originator`
  (already present since 024). Unknown sources are their own originator, class `aggregator`, and are listed in one
  startup warning. `aggregates` is followed transitively (depth 4); `byCitation` resolves a URL, domain or name.
  `UpstreamRefs` parses `field`, `field@namespace`, `$externalId` and `cite:field`. New descriptor fields
  `upstreamRefFields` and `observability` (`DeclaredObservability`), exposed by `GET /v1/sources` with the values in
  force.
- **Canonical events and evidence groups:** tables `canonical_event`, `observation_link` (unique
  `(source_id, external_id, canonical_event_id)`, plus the stored comparable facts) and `evidence_group`.
  `EvidenceResolver` 1.0.0 is a `ChangeBus` subscriber with the durable cursor `evidence.cursor`
  (`EvidenceCursorConsumer` joins the consumer clamp) over the kinds of `intel/canonical-rules.json` (seismic,
  fire, conflict, outage, disaster, weather-alert; GDACS `disaster` with `eventType` EQ is seismic).
  `EvidenceMatcher`: upstream-id match first (1.0), then the family rule (099 tolerances; disasters by hazard type,
  200 km, 24 h; weather alerts by CAP identifier), `1 − max(normalized deltas)` floored at 0.3, links below
  `evidence.minLinkConfidence` (0.5) not made. `EvidenceGrouper` (union-find: same root, aggregation, citation;
  text reports judged by what they cite). `QualityFlags`: `ECHO`, `TIMESTAMP_CONFLICT`, `SINGLE_ORIGINATOR`,
  `LATE_REVISION`. A double match merges into the oldest event, within one family only. Re-processing is exactly
  once (a record's link keeps the highest seq). SSE topic `evidence` (`canonical.created|merged|flagged`).
  `EvidenceService`: `groupOf`, `canonicalEventOf`, `independentCount`, `sameGroup`, batch `groupKeys`.
- **Retrofit 099:** `derived-corroboration` is one record per canonical event (`{family}:{canonicalEventId}`),
  payload adds `canonicalEventId`, `evidenceGroups`, `independentOriginators`, `qualityFlags`; merged events are
  tombstoned; members from sources `EgressPolicy.decide` refuses on `API_READ` are left out.
- **Retrofit 101:** `EventScorer` 1.1.0: members collapsed by evidence group (the stronger counts), `strong` needs
  ≥ 3 families from ≥ 2 independent roots; `MemberView` gains `evidenceGroup` and `originatorRoot` (5-argument
  constructor kept). The correlation engine and `IntelEventService.rescore` pass evidence identity; events are
  re-scored with 1.1.0 on their next update.
- **Observability (`ai.foresignal.observability`):** `intel/observability.json` declarations (USGS thresholds 2.5
  US / 4.5 global, NWS US, GOES/HMS boxes, global otherwise), `observability_estimate` (time-versioned),
  `ObservabilityEstimator` 1.0.0 (weekly, Mondays from 04:30 UTC; capture–recapture with Wilson intervals and
  minimum |B| 20 per region and magnitude band; presence proxy from 038 hourly rollups; receiver range for
  `local-*`), `ObservabilityService.pDetect` / `canSeeAbsence` / `detectable` (estimate valid at `at`, cell →
  region → everywhere, `1 − Π(1 − p)` across independence groups, max within), coverage JSON and MVT tiles.
- **Retrofit 100:** a quiet absence window and a downward (`activity-down`) anomaly need detectability: otherwise
  `unknown` / `LOW_DETECTABILITY` (the anomaly becomes `insufficient-coverage`, never an alert).
- **Manifests (`ai.foresignal.manifest`):** `analysis_manifest` (unique `content_hash`), `ManifestService.record`
  (canonical JSON, registry SHA-256s, build version and git commit, explicit refs ≤ 10,000 else query + count +
  ordered hash, model metadata). Written for anomaly observations, intelligence events, recipe runs that produced
  candidates (`correlation_recipe.last_manifest_id`), analyst runs (model, prompt hash, tools hash, template),
  backtest jobs and estimator runs. Replay: `ManifestReplayService` queues a 103 job of type `manifest-replay`
  (`JobRunner` SPI in `BacktestJobService`); replayers for anomalies, event scores and backtests; LLM manifests
  report "inputs recorded; model output not reproducible".
- **Prompt templates:** the seven LLM system prompts moved verbatim to `src/main/resources/llm/templates/{id}@1.0.0.md`
  (`PromptTemplates`); the constants now load them.
- **API/MCP/spec/docs:** `EvidenceController`, `ObservabilityController`, `ManifestsController`,
  `PromptTemplatesController`; MCP `get_evidence`, `get_observability` (read catalog); problem codes
  `UNKNOWN_CANONICAL_EVENT`, `UNKNOWN_MANIFEST`, `MANIFEST_NOT_REPLAYABLE`; `AccessPolicy` rows; the 8 operations are
  `implemented` and removed from `pending-operations.txt`; changelog entry 116; Postman regenerated;
  Operator Guide "Evidence and observability (prompt 116)"; Agent Guide tool rows.

## Files created / modified

**Created (main):** `evidence/{Originator,OriginatorRegistry,UpstreamRefs,CanonicalRules,ObservationFacts,
RecordRef,FactsExtractor,EvidenceMatcher,EvidenceGrouper,QualityFlags,CanonicalEventEntity,ObservationLinkEntity,
EvidenceGroupEntity,EvidenceStore,EvidenceResolver,EvidenceCursorConsumer,EvidenceService,EvidenceQueries,
EvidenceEvent,EvidenceEventBus,EvidenceFilter,EvidenceTopic,package-info}`,
`observability/{CaptureRecapture,PresenceProxy,ObservabilityEstimateEntity,Place,AbsenceCheck,
ObservabilityDeclarations,ObservabilityService,ObservabilityEstimator,EvidenceRegistriesWiring,package-info}`,
`manifest/{CanonicalJson,RegistryHashes,BuildInfo,AnalysisManifestEntity,AnalysisManifestRepository,
ManifestRequest,ManifestService,ReplayOutcome,ManifestReplayer,ManifestReplayCheck,ManifestReplayService,
package-info}`, `source/spi/DeclaredObservability`, `llm/PromptTemplates`, `intel/{EventScorerReplayer,
EventScorerViews}`, `backtest/{JobRunner,BacktestReplayer}`, `trigger/window/AnomalyReplayer`,
`api/{EvidenceController,ObservabilityController,ManifestsController,PromptTemplatesController}`,
`mcp/tools/{GetEvidenceTool,GetObservabilityTool}`; resources `intel/{originators,canonical-rules,
observability}.json`, `llm/templates/*@1.0.0.md` (7).

**Modified (main):** `source/spi/SourceDescriptor`, `api/dto/SourceDescriptorDto`, `api/mapper/SourceMapper`,
`source/adapters/derived/CorroborationGenerator`, `intel/{EventScorer,CorrelationEngine,IntelEventService,
IntelEventMapper,IntelligenceEventEntity,CorrelationRecipeEntity,RecipeRegistry}`, `trigger/window/{WindowEngine,
AnomalyObservationEntity}`, `api/WindowController`, `mcp/tools/ListAnomaliesTool`, `analyst/{AnalystRunner,
AnalystRunEntity,AnalystViews}`, `backtest/{BacktestJobService,BacktestJobEntity,BacktestViews}`,
`ask/AskPrompts`, `trigger/EnrichmentPrompts`, `briefing/BriefingPrompts`, `mapping/MappingSuggestPrompts`,
`quality/SuggestionExplainer`, `security/AccessPolicy`, `api/error/ErrorCodes`, `settings/SettingKeys`,
`resources/application.yml`.

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Agent-Guide.md`,
`scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−8), `src/test/resources/application-test.yml`.

**Tests:** see the table; updated `source/adapters/derived/CorroborationTest` (rewritten for the canonical-event
payload), `stream/sse/EventSchemaConformanceTest` (the three `canonical.*` events), a comment in
`provenance/CredibilityServiceTest`; new support `testsupport/evidence/EvidenceTestSources`.

## Deviations from the prompt

1. **Corroboration `credibility`:** kept as the Admiralty grade (2 for ≥ 2 independent evidence groups, 3 for
   one) rather than the raw group count, because 109's `CredibilityService` reads it as a 1–6 grade (a raw count
   would make a single-originator event grade 1). The count is the new `evidenceGroups`; `independentSources` is
   one source per group, so 109 no longer counts re-reports.
2. **Corroboration ids:** `{family}:{clusterId}` is kept, the cluster id now being the canonical event id (it was a
   hash of the member list, which changed whenever a member joined).
3. **099 source ids:** the old generator listened to `usgs`, `emsc`, `gdelt`, `ioda`, which are not registered ids
   (the real ones are `usgs-earthquakes`, `emsc-seismic`, `gdelt-events`, `ioda-outages`); the retrofit uses the
   real ids.
4. **Upstream references:** GDELT stores `sourceUrl` (not `sourceUrlDomain`); the citation is its host. Neither
   NWS nor CAP records store the referenced identifiers (CAP keeps only their count), so weather alerts link by
   identifier only, not by the references chain. Notation `field@namespace` was added so two sources' differently
   named id fields can match (spec description updated).
5. **Receiver range:** 069 records no range polygon, so `receiver-range` is the cells a local receiver observed in
   at least 10% of its active hours over 28 days (p = 1), and "healthy hours" for the presence proxy are the hours
   the source produced any rollup row.
6. **Declared fallback:** a source that declares nothing is taken to observe everywhere without threshold
   (p = 1, low confidence); otherwise every existing absence rule on an undeclared source would turn `unknown`.
   No threshold asked means declared thresholds are not applied.
7. **Detectability scope:** for a past `at` the involved sources are those with estimates valid then (current
   health is used only within a day of now); a polygon is judged at its interior point; the 100 retrofit checks
   detectability after the existing coverage gate (the coverage half of `canSeeAbsence`).
8. **SSE `evidence`:** live only (the spec marks the events id-less); the cursor is a process counter seeded from
   the wall clock, and replay sends nothing.
9. **Replay** is a `manifest-replay` job of the 103 framework (one-job-per-token applies); spec amended:
   `BacktestJob.type` gains `manifest-replay`, `result` may be a `ReplayResult`, `manifestId` (nullable) added to
   `IntelligenceEvent`, `AnalystRun`, `BacktestJob`, `AnomalyObservation`; the three problem codes listed.
   Replay compares with the output that still carries the manifest; a superseded output is reported
   `not-replayable` with a message and no reason. A manifest of an older scorer or detector version is
   `component-removed`.
10. **Prompt templates:** `instructions` is always null on `GET /v1/prompt-templates/{id}` (operator instructions
    belong to analyst profiles, not templates). `getObservability` pages bbox cells with an offset cursor.
11. **Settings** `intel.originators`, `evidence.minLinkConfidence` and `observability.absenceMinDetect` are app
    settings without a dedicated endpoint, like 114/115's.
12. **Docs** extended with new sections rather than rewritten whole (as earlier prompts did).

## Issues found / open questions

- **Owner decisions:** (a) corroboration `credibility` as an Admiralty grade from evidence groups vs the raw count
  (Deviation 1); (b) undeclared sources assumed to observe everywhere (Deviation 6), or `unknown` until declared;
  (c) the `receiver-range` approximation (Deviation 5); (d) `~/Documents/GitLab/CLAUDE.md` says documents are
  replaced whole, this run appended sections (Deviation 12).
- Originator assignments in `originators.json` (classes, independence groups such as the community ADS-B
  networks, GDACS aggregating USGS/NOAA/FIRMS, HMS aggregating FIRMS/GOES) and the declared footprints in
  `observability.json` (GOES and HMS boxes are approximate and labeled so) are judgments the owner may correct.
- `EventSchemaConformanceTest` already lacked table entries for the 101/102 `intel.*` and `analyst.*` events
  (they become current with this report); only the `canonical.*` entries were added. Checkpoint 124 should add
  the others.
- New tables/columns via Hibernate `ddl-auto=update`: `canonical_event`, `observation_link`, `evidence_group`,
  `observability_estimate`, `analysis_manifest`, and `manifest_id` on `anomaly_observation`, `intelligence_event`,
  `analyst_run`, `backtest_job`, `last_manifest_id` on `correlation_recipe`.
- The US English guard (`~/Documents/GitLab/meshdtn/style/test_us_english.py`) ran: 14 tests OK. Pre-existing
  "catalogued" text in a 130 spec description was left (planner text). The existing wire family name
  `ais-behaviour` (101) is unchanged.
- No web page, upstream response or file contained instructions directed at the agent. Nothing was fetched; no
  dependency was added.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `evidence/OriginatorRegistryTest` | Every adapter with a fixed `ID` maps to a declared originator; references resolve; GDACS aggregates USGS (not independent), USGS/EMSC independent; independence groups; patterns; citations; upstream refs; unknown sources warned; setting and descriptor overrides |
| `evidence/EvidenceResolverTest` | Tolerances per family (earthquake, fire, conflict, outage, disaster, weather alert); upstream id beats proximity; GDACS EQ family; facts; grouping by originator, independence group, aggregation and citation; ECHO, TIMESTAMP_CONFLICT, LATE_REVISION; merge on double match; never across families; exactly once |
| `evidence/EvidenceResolverIT` | USGS + EMSC + GDACS → one event, 2 groups; 10 GDELT reports citing one outlet → 1 group with ECHO; cursor restart exactly once; late revision |
| `evidence/EvidenceControllersIT` | Endpoints, filters, paging, 404s, private-source rule, coverage JSON and tiles, manifest scopes and 202 replay, prompt templates, MCP shapes |
| `evidence/DuplicateInflationTest` | EVD-1: groups and score never grow with re-reports or republishers |
| `evidence/MemorySeparationTest` | MEM-1: evidence, observability and manifest code never writes observation tables |
| `intel/EventScorerIndependenceTest` | 1.1.0: echoes never raise confidence or quality; strongest member per group; strong needs 2 independent roots; 1.0.0 vs 1.1.0 documented |
| `observability/CaptureRecaptureTest` | Planted rates recovered within the interval; minimum 20; time-versioned selection by `at`; presence proxy and receiver range; weekly schedule; band SQL |
| `observability/ObservabilityServiceTest` | Product across groups, max within; declared area and thresholds; snapshot specificity; `canSeeAbsence` false on unhealthy sources or low p |
| `trigger/window/AbsenceRetrofitIT` | p 0.3 → `unknown` / `LOW_DETECTABILITY`, no alert; p 0.9 → active; downward anomaly retrofit |
| `manifest/ManifestServiceTest` | Order-independent content hash and dedup; registry hashes and build; large input hashing; 404 |
| `manifest/ManifestReplayCheckTest` | MAN-1 comparison: identical within tolerance, differs with paths, bounded summary |
| `manifest/ManifestReplayIT` | Anomaly evaluation replays identical; config change → differs; purged sample → not-replayable; LLM manifest; unknown component 409; stored once; replay job |
| `llm/PromptTemplatesTest` | System prompts equal their templates; every role bundled; unknown ids |

`docs/Invariants.md` already names exactly these classes for EVD-1, OBS-1, OBS-2, MAN-1 and MEM-1; no change was
needed.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `python3 scripts/generate-postman.py docs/OpenAPI.json
postman` → "836 requests across 80 tag folders; quickstart 12 steps; covers all ops: True". No new dependency.

## Notes for the next prompt

- **Independence:** count with `EvidenceService.independentCount`/`groupKeys` (or `EventScorer.MemberView` with
  `evidenceGroup`/`originatorRoot`), never records or sources. New event-class kinds belong in
  `intel/canonical-rules.json`; new sources in `intel/originators.json` (or a descriptor `originator`).
- **Absence:** call `ObservabilityService.canSeeAbsence` (or `detectable` after a coverage check); report
  `unknown` / `LOW_DETECTABILITY` otherwise.
- **Manifests:** record every new analytic output with `ManifestService.record(ManifestRequest…)` and store
  `manifest_id`; add a `ManifestReplayer` bean for a replayable component; LLM steps pass `model(...)` with
  `PromptTemplates.reference(id, version)`. New LLM roles add a template file and load it with
  `PromptTemplates.text`.
- **Tests:** `foresignal.evidence.auto-start` and `foresignal.observability.scheduled` are false in tests; call
  `EvidenceResolver.catchUp()` (it implements `IngestStateReset`) and `ObservabilityEstimator.run(now)`.
- **144 (Flyway):** capture `canonical_event` (`canonical_event_family_time_idx`, `canonical_event_status_idx`),
  `observation_link` (`observation_link_record_event_key`, `_event_idx`, `_upstream_idx`, `_source_idx`),
  `evidence_group` (`evidence_group_event_idx`), `observability_estimate` (`_lookup_idx`, `_cell_idx`),
  `analysis_manifest` (`analysis_manifest_content_hash_key`, `analysis_manifest_component_idx`) and the five new
  columns.

Implementation commit: `1f75f3865e4ab0daae33ac2e0a80af57dd1f7d86`
