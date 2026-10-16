# 101-CR-Incidents-Investigations-And-Exposure

| | |
|---|---|
| Prompt | 101-Incidents-Investigations-And-Exposure.md |
| Status | Complete (post-review fixes applied 2026-10-16) |
| Implementation commit | `5248993e2f7c9d36fe2b2b73ad6bc21b76a1cc9c` |
| Post-review fix commit | `b456967682f9528881a33ef20b48687494118fdc` |
| Date | 2026-10-15 (fixes 2026-10-16) |

## Summary

Made the intelligence event a first-class object. Package `ai.foresignal.intel` holds the data model,
a deterministic correlation engine with built-in recipes, an `EventScorer`, manual/agent events,
investigations with snapshots and export, an `ExposureService`, an optional GHSL population pack, the
`intel` SSE topic, MCP tools and controllers. Every **public** event is published as a record of the
DERIVED source `foresignal-intel` (kind `intelligence-event`), so rules, SSE, tiles and search use it
unchanged; private events are not published there (see "Post-review fixes"). Tests written, not run.

- **Data model** (Hibernate, `ddl-auto=update`): `intelligence_event` (+ `version` optimistic lock,
  `merged_into`, `private`, `operator_notes`), `intelligence_event_member` (pinned
  `(source, externalId, seq, version)`, unique `(event_id, member_type, source_id, external_id, seq)`),
  `intelligence_event_log`, `correlation_recipe`, `investigation` (unique `display_id`),
  `investigation_item` (+ `private`), `population_h3`, and their repositories.
- **Correlation engine** (`CorrelationEngine`, `RecipeRegistry`, `RecipeDefinition`,
  `RecipeDefinitionValidator`, `Dedup`, `EventScorer`): a `ChangeBus` subscriber with a durable
  `intel.cursor` that catches up in 10k-seq chunks on a virtual thread, with a time look-back across
  chunk boundaries. Six bundled recipes load from `src/main/resources/intel/recipes/*.json`
  (`maritime-disruption`, `aviation-anomaly`, `infrastructure-disruption`, plus `dry-lightning-ignition`,
  `airport-weather` and `smoke-aq` added by later prompts), are versioned, and load disabled with a
  warning when a kind is missing. The scorer is `event-scorer 1.0.0` (`c = 1 − Π_f(1 − w_f·r_f)`,
  ×0.75 per contradiction, cap 0.95; labels and the four evidence-quality bands).
- **Manual/agent events** (`IntelEventService`): create from ≥ 1 verified member (by seq, identity
  checked), patch with optimistic locking (409 `EVENT_VERSION_CONFLICT`) and validated status/severity,
  add/remove members, merge. Client-supplied confidence is rejected; the scorer computes it; member
  signal families and weights are derived by the server.
- **Investigations** (`InvestigationService`): `INV-{yyyy}-{nnnn}` display ids, items with snapshots
  taken when added (≤ 64 KB, so evidence survives retention), JSON/Markdown/GeoJSON export with the
  banner and the license attributions of the cited sources.
- **Exposure** (`ExposureService`): facility counts by kind, places inside, live aircraft/vessels, and
  optional population; every section has a `status` and attribution. **Population pack**
  (`PopulationPackService`, `PopulationTiffReader`): imports a GHSL GHS-POP GeoTIFF under
  `${FORESIGNAL_DATA_DIR}/packs`, streaming the raster in strips, aggregates pixel centers to H3 res 5/7,
  and sums a queried area.
- **DERIVED record** (`ForesignalIntelGenerator`): source `foresignal-intel`, kind
  `intelligence-event`, payload type/severity/status/confidence/label/evidenceQuality/memberCount/
  signalFamilies/title/`automated`, emit-on-change (the derived context's), tombstone on
  dismiss/merge/turning private.
- **SSE** (`IntelTopic`, `IntelEventBus`): topic `intel` (cursor key `t`, order 40), events
  `intel.event.created|updated|resolved|merged`, cursor = event-log id, private-aware.
- **API/MCP/spec**: `IntelController`, `InvestigationsController`, `ExposureController`,
  `PacksController`; seven MCP tools (`list_intel_events`, `get_intel_event`, `get_exposure`,
  `create_intel_event`, `update_intel_event_status`, `create_investigation`, `add_to_investigation`);
  problem codes `UNKNOWN_EVENT`, `UNKNOWN_INVESTIGATION`, `EVENT_MEMBER_NOT_FOUND`, `RECIPE_INVALID`,
  `EVENT_VERSION_CONFLICT`, `PACK_NOT_INSTALLED`; `AccessPolicy` rows. The 20 prompt-101 operations in
  `docs/OpenAPI.json` were flipped to `implemented` and removed from `pending-operations.txt`; Postman
  regenerated (835 requests, 80 tags) in the implementation commit.

## Files created / modified

Created (main): `intel/` (`IntelligenceEventEntity`, `IntelEventMemberEntity`, `IntelEventLogEntity`,
`CorrelationRecipeEntity`, `InvestigationEntity`, `InvestigationItemEntity`, `PopulationH3Entity` and
their repositories, `EventScorer`, `EventTypes`, `SignalFamilies`, `RecipeDefinition`,
`RecipeDefinitionValidator`, `RecipeRegistry`, `Dedup`, `CorrelationEngine`, `IntelEventService`,
`IntelEventMapper`, `IntelEvent`, `IntelFilter`, `IntelEventBus`, `IntelListener`, `IntelTopic`,
`InvestigationService`, `ExposureService`, `PopulationPackService`, `PopulationTiffReader`,
`ForesignalIntelGenerator`, `IntelCursorConsumer`; post-review: `IntelVisibility`, `SnapshotCaps`),
`api/IntelController`, `api/InvestigationsController`, `api/ExposureController`, `api/PacksController`,
seven MCP tools; `resources/intel/recipes/{maritime-disruption,aviation-anomaly,infrastructure-disruption}.json`.

Created (test): `intel/EventScorerTest`, `RecipeDefinitionValidatorTest`, `DedupTest`,
`CorrelationEngineIT`, `EventAlertIT`, `InvestigationIT`, `ExposureServiceIT`, `IntelControllersIT`;
post-review: `InvestigationsControllerIT`, `ForesignalIntelGeneratorTest`, `SnapshotCapsTest`.

Modified: `api/error/ErrorCodes`, `api/error/NotFoundException` (a code-carrying constructor),
`security/AccessPolicy`, `config/StartupOrder` (`INTEL = 31`), `settings/SettingKeys`
(`INTEL_CURSOR`, added to `ALL`), `docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`,
`postman/` (regenerated), `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

- **Correlation is a batch catch-up, not a per-batch flow.** The engine subscribes to the `ChangeBus`
  for gaps only and runs a 5 s catch-up loop over `seq` ranges (10k chunks plus a time look-back of
  older versions), which is reproducible (re-running a seq range yields the same events and membership)
  and keeps ingest unblocked. Joins are applied by kind + (distance or `sameRegion: admin1|country`) +
  time window. `sameEntity` joins are parsed but not enforced; recipe `sources` and `conditions` filters
  are parsed but not enforced (kinds select rows).
- **Lifecycle.** `emerging` → `active` (when `activateAt` holds over all members), quiet-period resolve,
  and reopen within `reopenWindow` are implemented (post-review). Resolve "when every member is
  tombstoned or expired" is not implemented. Reopened events return to `emerging`/`active` (the status
  enum has no `reopened`); the log records `reopened`.
- **`healthOf` contradictions are coverage-aware** (post-review): only a running, healthy source that
  covers the area (reported coverage region, or a report within 7 days near the area) and observed
  nothing near the event contradicts; an unhealthy covering source adds its alternative as a known
  coverage gap without lowering confidence; disabled/idle/non-covering sources are ignored. Health is
  read live at correlation time and recorded in `detector.params.contradictionInputs`.
- **Population reading.** Prompt 054's `GeoTiffReader` decodes to RGBA8 only and cannot yield
  population counts, so `PopulationTiffReader` reads the raw band 0 through the JDK TIFF plugin (in
  256-row strips) while using `GeoTiffReader.inspect` for BigTIFF rejection and EPSG:4326
  georeferencing. The import record is stored in `app_setting` key `population.import` rather than a
  table. The header inspection still reads the file into memory once (the 054 reader takes bytes).
- **Exposure watchlist counts are not implemented** (there is no watchlist entity-count API until
  prompt 130); the section reports `not-installed` with a reason. `data-centre` has no source until
  104 and reports as not-installed.
- **`listIntelEvents` ignores `bbox`** (the in-Java list filters type/status/since/minConfidence over
  the newest 2,000 events and caps the page); a keyset cursor is deferred to 103, so `nextCursor` is null.
- **OpenAPI schemas were pre-authored**; this prompt flipped their status. Post-review, the
  `IntelEventMemberInput.signalFamily`/`weight` descriptions say they are ignored (server-derived), with
  a changelog entry. Postman was not regenerated for that description-only change.
- **Signal families** are constants in `SignalFamilies` (`SignalFamilies.forKind`); member families and
  weights come from the stored recipes (`RecipeRegistry.familyWeight`). Prompt 119's
  `intel/signal-families.json` becomes the shared registry later.
- **Private events are not `foresignal-intel` records** (post-review). `foresignal-intel` is a public
  source, so publishing private events there would leak them through tiles, search and
  `query_records`; they are omitted (and tombstoned if they turn private). Consequently ordinary 017 rules
  on `kind=intelligence-event` never fire for private events. A private derived source would close this
  gap; it is recorded as an open question rather than added.
- **Dry runs** (`POST /v1/intel/recipes/{id}/test`) cover at most 100,000 seqs and return at most 100
  would-be events (`truncated` flags more).

## Issues found / open questions

- `api/InfraController` (`eventId` body) and `mcp/tools/GetAffectedInfrastructureTool` read
  `intelligence_event.geom` directly by id without the private-event check; they belong to the
  infrastructure prompt and were left unchanged here. They should call `IntelEventService.visible`.
- The engine reads all versions of a recipe's kinds per chunk (plus up to 20,000 look-back versions)
  and clusters in Java; a large backfill is prompt 103's concern.
- `intel.cursor` is held, not skipped, when a recipe throws; a permanently failing recipe stops
  correlation until it is fixed or disabled (one `system.intel_correlation_failed` system event is raised
  after three failed passes).
- The GHSL population estimate is a model of residential population, never a casualty estimate, and
  is labeled as such in every response.

## Tests written (not run)

- `EventScorerTest` — families, best-per-family, contradictions, cap, evidence-quality table, labels.
- `RecipeDefinitionValidatorTest` — parse, valid, missing kind disables with a warning, bad type /
  weight / quiet period.
- `DedupTest` — shared member, buffered geometry + time overlap, no merge across types.
- `CorrelationEngineIT` — forms with pinned members and a detector; `emerging` → `active` as a second
  family joins from a later chunk (far and out-of-window rows never join); a seed is never counted twice;
  a kind contradiction lowers confidence and adds an alternative; a non-running `healthOf` source never
  contradicts; quiet period → resolved, new evidence reopens; a rewound cursor keeps membership
  exactly-once; a private member marks the event private.
- `ForesignalIntelGeneratorTest` — private events never published, tombstoned when they turn private;
  full passes page through every event; the published set comes from own heads; incremental passes;
  dismissed/merged tombstoned; 1825-day retention.
- `EventAlertIT` — a 017 rule on `kind=intelligence-event`, `severity == critical` alerts.
- `InvestigationIT` — a record item keeps its snapshot (also after the version is purged); md and
  geojson cite it and carry the banner; snapshots capped at 64 KB; sequential display ids.
- `ExposureServiceIT` — facility and place counts in a polygon; population `not-installed`.
- `IntelControllersIT` (MockMvc) — CRUD with the three sections; scopes (403); 409 version conflict;
  400 on bad status/severity; notes apart from the narrative; client weight/family ignored; member
  identity mismatch 400; merge with a shared member; member removal announced on the bus; private event
  hidden from read/agent on get, list, exposure and patch but visible with private access; private
  member refused without access; recipe dry run, 404 unknown, 400 oversized range; exposure by geometry.
- `InvestigationsControllerIT` (MockMvc) — CRUD and scopes, sequential ids, export attributions,
  item removal scoped to its investigation, missing seq 400, private items.
- `SnapshotCapsTest` — unchanged, payload dropped, identity only.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL (implementation and post-review commits).

## Notes for the next prompt

- Events live in `ai.foresignal.intel`; consume them as the DERIVED `foresignal-intel` record (kind
  `intelligence-event`, public events only) or via `GET /v1/intel/events`. Every read of an event must
  pass the caller's private access (`IntelVisibility`, `IntelEventService.visible/list`). 102 may explain
  existing evidence but must not add members or change the type; 118 links events into situations;
  121/130 extend investigations.
- `intel.cursor` is a retention consumer (via the window/trigger pattern) — the evaluator's watermark.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

## Post-review fixes (2026-10-16)

Commit `b456967682f9528881a33ef20b48687494118fdc` (`101: fix post-review defects`).

1. **EGR-1, private events on REST/MCP (critical).** New `IntelVisibility` applies the private-source
   rule to events: visible only to callers that may see private sources, whose restriction does not
   exclude them and from whom no private source is hidden. `IntelEventService.visible/list/create/
   patch/addMember/removeMember/merge` take the caller's access; `IntelController`,
   `ExposureController` (`eventId`), `GetIntelEventTool`, `ListIntelEventsTool`, `GetExposureTool`,
   `CreateIntelEventTool` and `UpdateIntelEventStatusTool` pass it. A hidden event is `404
   UNKNOWN_EVENT`. The `private` flag of manual, agent and analyst events is derived from the member
   sources; a caller without private access cannot pin a private version.
2. **Private events as public records (high).** `ForesignalIntelGenerator` never emits private events
   and tombstones a published one that turns private.
3. **Health contradictions (high).** See "Deviations": only healthy, covering sources that observed
   nothing contradict; disabled/idle sources never do; inputs stored in the detector params.
4. **Joins, clustering, chunks, failures, persistence (high).** Joins skip rows already in the cluster
   (no double counting), require geometry, and check the time window and distance/region; chunks read a
   time look-back of older versions; a failing recipe holds the cursor (retry, `last_error`, system event
   after three passes); `persist` adds only new members, rescores over all members, derives title and
   explanation from them, and runs in one transaction with the stream event announced after it.
   Lifecycle (`emerging`, quiet resolve, reopen) implemented.
5. **INT-3 member input (high).** Member identity is checked against the version at `seq`; signal family
   and weight are derived from the recipe registry; `memberType`/`role`/`status`/`severity`/`title` are
   validated; notes are stored in `operator_notes` and returned as `notes`, not in the LLM `narrative`.
6. **Generator coverage (medium).** Pages through all events after `reconcile` (which rebuilds the
   published set from own heads) and through recently updated events afterwards.
7. **Merge with shared members (medium).** Members the target already pins are dropped instead of moved;
   the unique constraint the prompt requires was added (it was missing).
8. **Member removal (medium).** Publishes `intel.event.updated` and logs the removal.
9. **Investigation item removal (medium).** An item of another investigation is `404`.
10. **Investigation private items (medium).** Items built from private sources are stored `private`,
    added only with private access, and left out of reads and exports otherwise.
11. **Display ids (low).** Unique constraint plus an advisory-lock-serialized numeric allocation.
- **Undisclosed gaps closed:** recipe dry run implemented with 404 for unknown recipes; member (16 KB)
  and item (64 KB) snapshot caps; export attributions from the cited sources' licenses (109
  `AttributionService`) instead of hard-coded GHSL/H3; population import streams strips; the
  `foresignal-intel` retention follows the `intel` analytic class (1825 d).
- **Corrections to this report:** the original said `intelligence_event_member` was "unique per version"
  (no constraint existed), that health-based contradictions were "handled" (any non-healthy source,
  disabled ones included, counted as a contradiction), that a candidate "activates when its thresholds
  are met" (sub-threshold candidates were dropped, never stored as `emerging`), and that every event is
  published as a record (private ones now are not). It did not disclose that the recipe dry run always
  returned an empty list, that snapshot caps were missing, that export attributions were hard-coded, or
  that the population import held every pixel in memory.
