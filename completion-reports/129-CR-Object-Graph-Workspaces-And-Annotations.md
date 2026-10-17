# 129-CR-Object-Graph-Workspaces-And-Annotations

| | |
|---|---|
| Prompt | 129-Object-Graph-Workspaces-And-Annotations.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `3ee3821f050f8fb02a22279fe5499fd6728c5185` |
| Date | 2026-10-17 |

## Summary

This prompt gives every analytic object one reference and one envelope, builds a typed relation graph with a
projector, adds annotations and verdicts with precision metrics, and adds shared workspaces that several clients
follow over SSE. It also adds dossiers, context summaries and sensor discovery. The code lives in the new package
`ai.foresignal.object`, with one resolver per object type in the owning package. It compiles. **The tests are written
but were not run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report is
measured.

- **Object contract** (`ObjectTypes`, `ObjectRef`, `ObjectEnvelope`, `ObjectAction`, `ObjectActions`,
  `EnvelopeGeometry`, `ObjectCaller`, `ObjectCallers`, `ResolveContext`, `ObjectResolver`, `ObjectService`):
  - There are 23 object types and 15 relation types, with the labels `confirmed`, `likely`, `possible` and
    `speculative`, the client kinds, and contract version `1.0` (`GET /v1/contract`).
  - Refs are `{type}:{id}`. A record ref is `record:{sourceId}/{externalId}[@seq]`, with the external id
    percent-encoded. A sensor ref is `sensor:{kind}:{id}`. A bad ref is 400 `OBJECT_REF_INVALID`.
  - The envelope carries the title, time, geometry (at most 500 vertices), status or severity, `private`, provenance,
    and the caller's actions. Actions depend on scope and on object state.
  - `ObjectService` covers get, find, batch resolve (at most 500; unknown refs are listed in `missing`) and the `asOf`
    check.
- **Resolvers** in the owning packages:
  - `store/query/RecordObjectResolver`: `asOf` through `AsOfQueryService`, and `@seq` versions.
  - `situation/SituationObjectResolver`: `asOf` through snapshots.
  - Findings, intel events, investigations, canonical events, entities, precursors, base rates, questions, forecasts,
    hypothesis sets with their hypotheses and indicators (`HypothesisObjectResolvers`), assessments, alerts, places,
    views, watchlists and sources.
  - Workspaces, sensors and annotations (`ObjectContractResolvers`).
- **Relations** (`RelationEntity`, table `relation`; `RelationRepository`, `RelationQuery`, `RelationService`,
  `RelationProjector`, `RelationCursorConsumer`):
  - Each edge has a type, a label, a validity interval, an owner and a projection key.
  - The service provides related objects (types, direction, `asOf`, `minConfidence`), neighbors, `located-in` computed
    from 036 places, subgraphs (at most 50 seeds and 2,000 edges), common dependencies (at most 20 refs, 4 hops),
    facilities near a point, and deterministic ids.
  - The projector (`relation-projector` 1.0.0) follows the change bus with the durable cursor `relation.cursor` (in
    the retention consumer clamp) for record-backed owners. A nightly reconcile at 03:40 UTC
    (`foresignal.relations.reconcile-cron`) covers the rest: entity links, infrastructure edges (owner `infra:graph`),
    canonical events, questions and investigations. It also runs once when no projected relation exists.
  - `foresignal.relations.auto-start` defaults to `foresignal.analytics.scheduled`.
  - Operator relations are `POST /v1/relations` (agent) and `DELETE /v1/relations/{id}`. A projected relation is 409
    `RELATION_NOT_EDITABLE`.
- **Annotations** (`AnnotationEntity`, table `annotation`; `AnnotationService`, `PrecisionService`,
  `BenchmarkCaseProposals` with table `benchmark_case_proposal`):
  - A note needs any token: a body of at most 10,000 characters and at most 20 tags. A verdict needs agent scope:
    `confirmed`, `rejected`, `needs-review` or `duplicate-of`.
  - An annotation of a private object is private.
  - `applyStatus: true` on a rejected verdict dismisses a finding (`FindingService.dismiss`) or an intel event.
  - A verdict on an object with a time window proposes a 123 benchmark case, stored pending.
  - Precision is the share confirmed among the latest live verdicts per producing component, with a 95% Wilson
    interval.
  - `CyclePacketReader` now reads the 128 packet's verdicts section from the annotation table.
- **Workspaces** (`WorkspaceEntity`, table `workspace`; `WorkspaceRepository`, `WorkspaceAccess`,
  `WorkspaceService`, `WorkspaceContextValidator`, `JsonMergePatch`, `ContextLinks`, `WorkspaceTopic`,
  `WorkspaceFilter`, `WorkspacePresence`, `WorkspaceShareTarget`):
  - Endpoints: CRUD, layout (at most 64 KB, 413 above), the context as a merge patch with `If-Match` (412
    `WORKSPACE_CONTEXT_STALE` carries `contextVersion`), the summary (409 `WORKSPACE_CONTEXT_INCOMPLETE` without a
    viewport or a time range), a deep link (`#fs=`, at most 2,048 bytes) and share.
  - Share goes through the 112 `ShareLinkService`, which now accepts extra targets (`share/ExtraShareTarget`).
  - The SSE topic `workspace` (cursor key `w`) carries `workspace.context` (with `originClientId` from
    `Foresignal-Client-Id`) and `workspace.presence`, which is diffed every 2 seconds.
- **Dossier, summary and sensors** (`DossierService`, `ContextSummaryService`, `EvidenceClasses`,
  `SensorDiscoveryService`, `SatelliteCapabilities`):
  - The dossier has sections, a 64 KB cap with trimming and `truncated`, and an ETag with 304. A failing section
    renders as unavailable.
  - The context summary has layer counts (the new `RecordQueryService.countsIn`), activity, insights and related
    objects. It is cached 30 seconds and capped at 32 KB.
  - Sensor discovery lists sources by 116 detection probability above 0.2, receivers within 400 km, stations within
    2,500 km, satellite passes at 10° or more from `PassPredictor`, STAC scenes and raster products. The window is at
    most 7 days, and every answer carries the no-tasking note.
  - Resource `intel/satellite-capabilities.json`: 39 satellites, NORAD ids checked against CelesTrak GP on
    2026-10-17. An operator override file is set with `foresignal.sensors.capabilities-file`.
- **API, MCP, docs:**
  - `api/ObjectsController` and `api/WorkspacesController` serve all 27 operations. `config/EncodedSlashConfig` sets
    Tomcat `%2F` pass-through.
  - Other changes: `AccessPolicy` rows, nine Problem codes, `SettingKeys.RELATION_CURSOR`, and
    `StartupOrder.RELATIONS = 41`.
  - MCP: `get_object`, `get_related`, `get_workspace_context` and `annotate` (read; verdicts need agent scope), and
    `set_workspace_focus` (agent). `get_object` and `get_related` join the analyst read catalog.
  - The spec marks the 27 operations implemented, adds the error codes, the `workspace` SSE topic and a changelog
    entry, and removes `tag` from `AnnotationInput.kind`. `pending-operations.txt` loses its 27 lines.
  - The Postman generator gains path variables, merge-patch bodies, `If-Match` and `Foresignal-Client-Id` headers,
    and bodies and captures for the 129 operations. The collection was regenerated.
  - `docs/Client-Contract.md` was replaced whole. The Agent Guide gains five tool rows, "Objects, relations and
    workspaces", a vocabulary bullet and two scope rows. The Operator Guide gains "Objects, workspaces and validation
    (prompt 129)", and its share-link sentence now names the workspace target.

## Files created / modified

**Created (main):**
- `object/`: `package-info`, `ObjectTypes`, `ObjectRef`, `ObjectEnvelope`, `ObjectAction`, `ObjectActions`,
  `EnvelopeGeometry`, `ObjectCaller`, `ObjectCallers`, `ResolveContext`, `ObjectResolver`, `ObjectService`,
  `ObjectContractResolvers`, `RelationEntity`, `RelationRepository`, `RelationQuery`, `RelationService`,
  `RelationProjector`, `RelationCursorConsumer`, `AnnotationEntity`, `AnnotationRepository`, `AnnotationService`,
  `PrecisionService`, `BenchmarkCaseProposalEntity`, `BenchmarkCaseProposalRepository`, `BenchmarkCaseProposals`,
  `WorkspaceEntity`, `WorkspaceRepository`, `WorkspaceAccess`, `WorkspaceService`, `WorkspaceContextValidator`,
  `JsonMergePatch`, `ContextLinks`, `WorkspaceTopic`, `WorkspaceFilter`, `WorkspacePresence`,
  `WorkspaceShareTarget`, `SatelliteCapabilities`, `SensorDiscoveryService`, `EvidenceClasses`, `DossierService`,
  `ContextSummaryService`.
- Resolvers: `store/query/RecordObjectResolver`, `situation/SituationObjectResolver`,
  `analytics/finding/FindingObjectResolver`, `intel/IntelEventObjectResolver`, `intel/InvestigationObjectResolver`,
  `evidence/CanonicalEventObjectResolver`, `entity/EntityObjectResolver`, `precursor/PrecursorObjectResolver`,
  `baserate/BaseRateObjectResolver`, `forecast/QuestionObjectResolver`, `forecast/ForecastObjectResolver`,
  `hypothesis/HypothesisObjectResolvers`, `cycle/AssessmentObjectResolver`, `trigger/AlertObjectResolver`,
  `place/PlaceObjectResolvers`, `source/SourceObjectResolver`.
- `share/ExtraShareTarget`, `config/EncodedSlashConfig`, `api/ObjectsController`, `api/WorkspacesController`.
- `mcp/tools/{GetObjectTool,GetRelatedTool,AnnotateTool,GetWorkspaceContextTool,SetWorkspaceFocusTool}`.
- Resource `intel/satellite-capabilities.json`.

**Modified (main):**
- `api/error/ErrorCodes`: `OBJECT_REF_INVALID`, `UNKNOWN_OBJECT`, `UNKNOWN_WORKSPACE`, `UNKNOWN_ANNOTATION`,
  `UNKNOWN_RELATION`, `RELATION_NOT_EDITABLE`, `WORKSPACE_CONTEXT_STALE`, `WORKSPACE_CONTEXT_INCOMPLETE`,
  `WORKSPACE_FORBIDDEN`.
- `settings/SettingKeys` (`relation.cursor`), `config/StartupOrder` (`RELATIONS = 41`), `security/AccessPolicy`.
- `share/ShareLinkService`: extra targets, `targetTypes()`.
- `store/query/RecordQueryService` and `Slices`: `countsIn` and `AreaCount`.
- `intel/InvestigationRepository`: `findByDisplayId`.
- `analyst/AnalystRunner`: `get_object` and `get_related` in the read catalog.
- `cycle/CyclePacketReader`: the verdicts section.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Client-Contract.md`, `docs/Agent-Guide.md`,
`docs/Operator-Guide.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−27).

**Tests:** the new classes below, plus the helper `object/ObjectItSupport`. Modified:
- `stream/sse/EventSchemaConformanceTest`: payload factories for `workspace.context` and `workspace.presence`.
- `retention/ConsumerClampIT`: `relation.cursor`.

## Deviations from the prompt

1. **Five MCP tools.** The prompt names `get_workspace_context` and `set_workspace_focus`. The design document's
   object section also lists `get_object`, `get_related` and `annotate`, so all five are built.
2. **Workspace roles.**
   - Context and layout changes need owner or editor, with any scope.
   - Metadata changes need the owner, or an editor with agent scope.
   - Delete needs the owner or an admin. An admin may read any workspace.
   - A caller with no role gets 404, not 403, so workspace ids do not leak.
3. **Annotation kinds.**
   - The spec's `tag` kind is removed: tags are a field of notes.
   - "Propose watch" is a note tagged `propose-watch`, not a 102 proposal, because a 102 proposal needs a profile.
4. **Benchmark proposals** are stored in their own table, with service methods `propose`, `pending`, `accept` and
   `reject` and no REST. Accepting creates a 123 case.
5. **Projection coverage.**
   - Only record-backed owners are incremental. Entity links, infrastructure edges, canonical events, questions and
     investigations refresh on the nightly reconcile.
   - `precedes` and `affects` are not projected, and neither is `causes` from hypothesis statements.
   - `located-in` is computed on demand from 036 places only.
   - Common dependencies reach 104 facilities through those that directly affect the objects, then follow
     `depends-on`.
6. **`causes` is capped** at `possible` unless an admin token asserts it.
7. **Context summary.** Layer counts are current state, and `asOf` bounds the activity only. There is no `record`
   activity type, and `narrative` is null (no LLM).
8. **Dossier.** Imagery uses the current raster-frame and satellite-scene heads only. Official forecasts cover NHC
   only.
9. **Sensors.**
   - Receiver positions use the receiver source's last record position as a proxy.
   - Geostationary satellites are excluded. Capabilities are nominal figures.
   - The satellite link points at Foresignal's own CelesTrak compatibility route (`/compat/celestrak/gp.php`).
10. **Alert visibility** checks excluded and private sources only, not the restriction snapshot.
11. **Encoded slashes.** Refs in paths need `%2F`, so Tomcat's `EncodedSolidusHandling` is set to `PASS_THROUGH`
    (`EncodedSlashConfig`). `TokenFilter` already matches the raw URI. A reverse proxy must forward `%2F` still
    encoded; the Operator Guide says so.
12. **Postman.** Token-required reads use `readToken` in the generator.
13. **Edited by script, not rewritten by hand:**
    - The spec, as in every earlier prompt.
    - The Operator Guide was read end to end in chunks. It was then written whole by script, with one section appended
      and one sentence replaced.
    - The Agent Guide was read whole and edited by script.
    - `docs/Client-Contract.md` was replaced whole.
    - `docs/Invariants.md` was not changed: OBJ-1 and OBJ-2 already name `WorkspaceSyncIT` and `RelationProjectorIT`.

## Issues found / open questions

**Owner decisions:**
- (a) the workspace role matrix (Deviation 2);
- (b) dropping the `tag` annotation kind from the spec, and propose-watch as a note (Deviation 3);
- (c) benchmark proposals with no REST surface (Deviation 4);
- (d) nightly rather than incremental projection for the non-record owners (Deviation 5);
- (e) Tomcat encoded-slash pass-through, server-wide (Deviation 11);
- (f) the curated satellite list and its override file (Deviation 9).

**Known limits:**
- Presence is in-memory per instance.
- The context-summary cache holds at most 500 entries.

**Schema via Hibernate, for prompt 144:**
- New tables: `relation` (indexes on from, to, validity and owner key, and unique `projection_key`), `annotation`,
  `workspace` and `benchmark_case_proposal`.
- New setting key: `relation.cursor`.
- New properties: `foresignal.relations.auto-start`, `foresignal.relations.reconcile-cron` and
  `foresignal.sensors.capabilities-file`.

**Guards not run:** the US English guard, `lintOpenApi` and every contract test (`AccessPolicySyncTest`,
`OpenApiContractIT`, `EventSchemaConformanceTest`, `SkillFrontmatterTest`) were not run (no-tests directive). New
prose was checked by grep for British spellings; there were no hits besides existing wire names (`licences`,
`standardised`, the "labelled" footer).

**Risks for the owner's first test run:**
- `WorkspaceSyncIT.encodedSlash` depends on the connector customizer under `@WebIntegrationTest`.
- `SensorDiscoveryIT.predictedPasses` uses a hand-built Sentinel-2A element set. It expects at least one pass of 10° or
  more over 42–46° E, 12–16° N within a day of the test clock.
- `RelationProjectorIT` drives `catchUp` and `reconcile` directly, because auto-start is off in tests.

**External data:** NORAD catalog numbers were fetched from CelesTrak GP as data only. No file, page or tool output
contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `object/ObjectRefTest` | Round trip of every type; record source, external id and seq; percent-encoding of `/`, `@` and `%`; sensor kinds; malformed and over-long refs |
| `object/ObjectActionsTest` | Actions for anonymous, read, agent and admin callers; state-dependent alert acknowledgment; containers not annotated |
| `object/ContextLinkTest` | Encode and decode; ad-hoc contexts; 413 above 2 KB; context schema validation; malformed payloads; merge patch semantics |
| `object/ObjectResolverIT` | Every type resolves to the envelope; record and situation `asOf`; private hiding; batch resolve with missing refs |
| `object/DossierIT` | Storm record with official cone and ledger question; failing section unavailable; 64 KB trimming; `include` validation |
| `object/ContextSummaryIT` | Layer counts in the viewport; activity order and families; insights with issuer, term, base rate and cutoff; 30 s cache and 32 KB cap; viewport and time-range validation |
| `object/RelationProjectorIT` | Situation members and retirement; intel-event roles; canonical-event observed-by and same-as; entity links; 104 depends-on edges; nightly reconcile repair and idempotence; cursor restart exactly once |
| `object/GraphQueryTest` | Depth, type and edge limits; `asOf` validity; common dependencies with the weakest label; private relations hidden |
| `object/AnnotationIT` | Notes and agent verdicts; private annotations; precision with Wilson intervals; benchmark proposals; `applyStatus` dismissal |
| `object/WorkspaceSyncIT` | `If-Match` patch fan-out with `originClientId` and 412; presence joins and leaves; roles; deep links and summaries; `%2F` through the servlet container |
| `object/SensorDiscoveryIT` | Sources by detection probability; receivers by range; satellite passes with capabilities; STAC scenes; validation |
| `object/ObjectControllersIT` | Every object and workspace endpoint over MockMvc: status codes, scopes, ETag, merge patches, 409 summary, and the five MCP tools |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **130 (collaboration):** tags in envelopes, object ACLs and annotation threads build on `AnnotationService` (the
  `thread` parameter exists) and `ObjectActions`.
- **141 (SDK):** a `WorkspaceSync` helper should wrap `If-Match`, 412 re-read and re-apply, `Foresignal-Client-Id`,
  and the `workspace` topic.
- **144 (Flyway):** capture the four tables and their indexes above.
- **Tests:** call `RelationProjector.catchUp()` and `reconcile()` directly. Build object refs in paths with
  `ObjectItSupport.objectPath`, which encodes `/` as `%2F`.

Implementation commit: `3ee3821f050f8fb02a22279fe5499fd6728c5185`
