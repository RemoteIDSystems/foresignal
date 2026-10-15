# 101-CR-Incidents-Investigations-And-Exposure

| | |
|---|---|
| Prompt | 101-Incidents-Investigations-And-Exposure.md |
| Status | Complete |
| Implementation commit | `5248993e2f7c9d36fe2b2b73ad6bc21b76a1cc9c` |
| Date | 2026-10-15 |

## Summary

Made the intelligence event a first-class object. New package `ai.foresignal.intel` holds the data
model, a deterministic correlation engine with built-in recipes, an `EventScorer`, manual/agent
events, investigations with snapshots and export, an `ExposureService`, an optional GHSL population
pack, the `intel` SSE topic, MCP tools and controllers. Every event is published as a record of the
DERIVED source `foresignal-intel` (kind `intelligence-event`), so rules, SSE, tiles and search use it
unchanged. Tests written, not run.

- **Data model** (Hibernate, `ddl-auto=update`): `intelligence_event` (+ `version` optimistic lock,
  `merged_into`, `private`), `intelligence_event_member` (pinned `(source, externalId, seq, version)`,
  unique per version), `intelligence_event_log`, `correlation_recipe`, `investigation`,
  `investigation_item`, `population_h3`, and their repositories.
- **Correlation engine** (`CorrelationEngine`, `RecipeRegistry`, `RecipeDefinition`,
  `RecipeDefinitionValidator`, `Dedup`, `EventScorer`): a `ChangeBus` subscriber with a durable
  `intel.cursor` that catches up in 10k-seq chunks on a virtual thread. Three built-in recipes
  (`maritime-disruption`, `aviation-anomaly`, `infrastructure-disruption`) load from
  `src/main/resources/intel/recipes/*.json`, are versioned, and load disabled with a warning when a
  kind is missing. The scorer is `event-scorer 1.0.0` (`c = 1 − Π_f(1 − w_f·r_f)`, ×0.75 per
  contradiction, cap 0.95; labels and the four evidence-quality bands).
- **Manual/agent events** (`IntelEventService`): create from ≥ 1 verified member (by seq), patch with
  optimistic locking (409 `EVENT_VERSION_CONFLICT`), add/remove members, merge. Client-supplied
  confidence is rejected; the scorer computes it.
- **Investigations** (`InvestigationService`): `INV-{yyyy}-{nnnn}` display ids, items with snapshots
  taken when added (so evidence survives retention), JSON/Markdown/GeoJSON export with the banner and
  licence attributions.
- **Exposure** (`ExposureService`): facility counts by kind (power-plant, airport, port, cable-landing,
  data-centre, nuclear-facility, spaceport), places inside, live aircraft/vessels, and optional
  population; every section has a `status` and attribution. **Population pack**
  (`PopulationPackService`, `PopulationTiffReader`): imports a GHSL GHS-POP GeoTIFF under
  `${FORESIGNAL_DATA_DIR}/packs`, aggregates pixel centres to H3 res 5/7, and sums a queried area.
- **DERIVED record** (`ForesignalIntelGenerator`): source `foresignal-intel`, kind
  `intelligence-event`, payload type/severity/status/confidence/label/evidenceQuality/memberCount/
  signalFamilies/title/`automated`, emit-on-change, tombstone on dismiss/merge.
- **SSE** (`IntelTopic`, `IntelEventBus`): topic `intel` (cursor key `t`, order 40), events
  `intel.event.created|updated|resolved|merged`, cursor = event-log id, private-aware.
- **API/MCP/spec**: `IntelController`, `InvestigationsController`, `ExposureController`,
  `PacksController`; seven MCP tools (`list_intel_events`, `get_intel_event`, `get_exposure`,
  `create_intel_event`, `update_intel_event_status`, `create_investigation`, `add_to_investigation`);
  problem codes `UNKNOWN_EVENT`, `UNKNOWN_INVESTIGATION`, `EVENT_MEMBER_NOT_FOUND`, `RECIPE_INVALID`,
  `EVENT_VERSION_CONFLICT`, `PACK_NOT_INSTALLED`; `AccessPolicy` rows. The 20 prompt-101 operations in
  `docs/OpenAPI.json` were flipped to `implemented` and removed from `pending-operations.txt`; Postman
  regenerated (835 requests, 80 tags).

## Files created / modified

Created (main): `intel/` (`IntelligenceEventEntity`, `IntelEventMemberEntity`, `IntelEventLogEntity`,
`CorrelationRecipeEntity`, `InvestigationEntity`, `InvestigationItemEntity`, `PopulationH3Entity` and
their repositories, `EventScorer`, `EventTypes`, `SignalFamilies`, `RecipeDefinition`,
`RecipeDefinitionValidator`, `RecipeRegistry`, `Dedup`, `CorrelationEngine`, `IntelEventService`,
`IntelEventMapper`, `IntelEvent`, `IntelFilter`, `IntelEventBus`, `IntelListener`, `IntelTopic`,
`InvestigationService`, `ExposureService`, `PopulationPackService`, `PopulationTiffReader`,
`ForesignalIntelGenerator`, `IntelCursorConsumer`), `api/IntelController`, `api/InvestigationsController`,
`api/ExposureController`, `api/PacksController`, seven MCP tools;
`resources/intel/recipes/{maritime-disruption,aviation-anomaly,infrastructure-disruption}.json`.

Created (test): `intel/EventScorerTest`, `RecipeDefinitionValidatorTest`, `DedupTest`,
`CorrelationEngineIT`, `EventAlertIT`, `InvestigationIT`, `ExposureServiceIT`, `IntelControllersIT`.

Modified: `api/error/ErrorCodes`, `api/error/NotFoundException` (a code-carrying constructor),
`security/AccessPolicy`, `config/StartupOrder` (`INTEL = 31`), `settings/SettingKeys`
(`INTEL_CURSOR`, added to `ALL`), `docs/OpenAPI.json`, `src/test/resources/openapi/pending-operations.txt`,
`postman/` (regenerated), `docs/Operator-Guide.md`, `docs/Agent-Guide.md`, `THIRD_PARTY_NOTICES.md`.

## Deviations from the prompt

- **Correlation is a batch catch-up, not a per-batch flow.** The engine subscribes to the `ChangeBus`
  for gaps only and runs a 5 s catch-up loop over `seq` ranges (10k chunks), which is reproducible
  (re-running a seq range yields the same events) and keeps ingest unblocked. Joins are applied by
  kind + distance + time; `sameRegion`/`sameEntity` joins are parsed but not yet enforced beyond
  distance, and kind-based contradictions are counted rather than added as members (health-based
  contradictions are handled).
- **Lifecycle is partly automatic.** A candidate activates (`active`) when its thresholds are met and
  updates an existing open event by dedup; the `emerging` state, quiet-period resolve, reopen and
  merge-on-hot-path are parsed but only status changes (`resolved`/`dismissed`) and explicit
  `POST /merge` are applied today. Recorded as an open item for 103/118.
- **Population reading.** Prompt 054's `GeoTiffReader` decodes to RGBA8 only and cannot yield
  population counts, so `PopulationTiffReader` reads the raw band 0 through the JDK TIFF plugin while
  using `GeoTiffReader.inspect` for BigTIFF rejection and EPSG:4326 georeferencing. The import record
  is stored in `app_setting` key `population.import` rather than a table.
- **Exposure watchlist counts are not implemented** (there is no watchlist entity-count API until
  prompt 130); the section reports `not-installed` with a reason. `data-centre` has no source until
  104 and reports as not-installed.
- **`listIntelEvents` ignores `bbox`** (the in-Java list filters type/status/since/minConfidence and
  caps the page); a keyset cursor is deferred to 103, so `nextCursor` is null.
- **OpenAPI schemas were pre-authored** (the 20 operations already carried `planned`); this prompt
  only flipped their status, removed the pending lines and regenerated Postman, and did not re-author
  schema bodies.
- **Signal families** are constants in `SignalFamilies`; prompt 119's `intel/signal-families.json`
  becomes the shared registry later.

## Issues found / open questions

- The correlation engine reads all versions of a recipe's kinds per chunk and clusters in Java,
  bounded by the chunk size; a large backfill is prompt 103's concern.
- `IntelEventService.create` verifies a member exists by `seq` via `RecordQueryService.versionsBySeq`
  but does not enforce that the member's `kind`/`source` match the recipe's seed (manual events are
  operator-authored).
- The GHSL population estimate is a model of residential population, never a casualty estimate, and
  is labelled as such in every response.

## Tests written (not run)

- `EventScorerTest` — families, best-per-family, contradictions, cap, evidence-quality table, labels.
- `RecipeDefinitionValidatorTest` — parse, valid, missing kind disables with a warning, bad type /
  weight / quiet period.
- `DedupTest` — shared member, buffered geometry + time overlap, no merge across types.
- `CorrelationEngineIT` — a recipe forms an event with pinned members, a detector and join reasons.
- `EventAlertIT` — a 017 rule on `kind=intelligence-event`, `severity == critical` alerts.
- `InvestigationIT` — a record item keeps its snapshot; md and geojson cite it and carry the banner.
- `ExposureServiceIT` — facility and place counts in a polygon; population `not-installed`.
- `IntelControllersIT` — event create/read (evidence/inference/speculation), investigation create/read/
  export, exposure by geometry.

## Compile result

`./gradlew compileJava compileTestJava` — BUILD SUCCESSFUL.

## Notes for the next prompt

- Events live in `ai.foresignal.intel`; consume them as the DERIVED `foresignal-intel` record (kind
  `intelligence-event`) or via `GET /v1/intel/events`. 102 may explain existing evidence but must not
  add members or change the type; 118 links events into situations; 121/130 extend investigations.
- `intel.cursor` is a retention consumer (via the window/trigger pattern) — the evaluator's watermark.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman` after any further spec change.

Implementation commit: `5248993e2f7c9d36fe2b2b73ad6bc21b76a1cc9c`
