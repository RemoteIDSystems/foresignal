# 138-CR-External-Data-Bindings-And-Provider-Kit

| | |
|---|---|
| Prompt | 138-External-Data-Bindings-And-Provider-Kit.md |
| Status | Complete (implemented and compiles; tests written and **not run** under the owner directive of 2026-10-17) |
| Implementation commit | `aa64341e3f64e68f0693c0af17cb5c5fe3a9da2a` |
| Date | 2026-10-18 |

## Summary

EDP streams can now be **bound** to where they belong in the intelligence layer, and the provider kit lets specialist
sources live in their own projects.

- **Bindings (`ai.foresignal.inbound.edp.binding`):**
  - Tables: `external_binding`, `external_binding_applied` and `external_question_link`.
  - The binding row holds the target, config, 017 filter, enabled flag, state (`active`, `disabled`, `backfilling`,
    `error`), applied count, `lastError`, `targetRef`, `latest`, `manifest_sha256` and `created_by`.
  - The idempotency key is `(binding, source, externalId, version)`, hashed.
- **`BindingRouter`:**
  - A change-bus subscriber with the durable cursor `external.binding.cursor`. `BindingCursorConsumer` joins 019's
    consumer clamp.
  - It reads only the versions of providers that have enabled bindings. A version belongs to a stream only when it
    carries that stream's `edp` provenance and kind, so records the bindings write themselves are never routed.
  - `BindingApplier` applies each matching binding exactly once per record version, in its own transaction. A failure
    rolls back, records `lastError` and marks the version handled.
  - Backfill applies the newest 100,000 stored versions at or below the router cursor. It runs inline when
    `foresignal.external.bindings.auto-start` is false (tests).
- **Targets:**
  - `series`: `ExternalSeriesReader` implements the new `ExternalSeriesSource` SPI of `SeriesReader`.
    - Refs are `series:ext-{id}/{kind}/{seriesKey}`. `knownAt` is `effectiveKnownAt`, and as-of reads filter on it.
    - Day and week series are registered in `analysis_series` and kept by a new registrar candidate method.
  - `canonical-family`: `EvidenceResolver.resolveBound` takes the family rule, with overrides or a default proximity
    rule for unknown families.
    - Items count under the item or manifest originator. `OriginatorRegistry.registerExternal` holds external
      originators with their `aggregates`; a curated originator always wins.
    - Upstream ids and `derivedFrom` match in `upstreamNamespace`.
    - Stream `coverage` reaches observability through the new `ExternalCoverage` SPI (`ExternalCoverageRegistry`). It
      adds the bound kinds to the family and gives a low-confidence `declared` estimate.
  - `social`: posts go through `SocialCollector` as 135 `social-post` records under the provider.
    - Collection is `import`; posts are counted `sampled` unless the stream declares `complete: true`.
    - The author key is salted by the collector.
  - `news`: documents become 133 `news-article` records under the provider, with the outlet resolved from the URL's
    domain.
  - `evidence`: 125 ACH rows through `HypothesisService.addEvidence`.
  - `indicator`: `observe` for physical streams, `reported` for documents.
  - `forecast`: forecasts by the external forecaster `edp:{sourceId}/{streamId}`.
    - Questions come from a question link (with orientation) or `questionRef`.
    - A forecast is recorded on the first item, on a 0.02 move, or daily. `made_at` is stamped by the ledger.
    - The ensemble admits the forecaster after 30 resolved linked questions, through the new `ExternalForecasterCounts`
      SPI.
  - `place`: the `Place.external` block, served by `PlaceMapper.placeWithExternal` on `GET /v1/places/{id}`.
  - `situation`: a pinned member, or `SituationLinker.consider` (never seeds).
  - `chronicle`: adds a layer of the now-implemented `external-stream` selector type.
- **Suggested bindings, preview, breaking updates:**
  - `accept-suggested` creates the manifest's suggestions; invalid ones are created disabled in state `error`.
  - `preview` calls only `plan()` and has no side effects.
  - `EdpManifestService` publishes an `ExternalSourceChangedEvent` on register, approve, suspend, manifest change and
    delete. On a breaking update, bindings of changed streams are disabled, with `external.binding.disabled` and a
    `system.binding_disabled` notification.
- **Surface:**
  - `ExternalBindingsController` serves the 10 operations, now `implemented`; their 10 lines are removed from
    `pending-operations.txt` (21 left).
  - SSE topic `external` (`ExternalTopic`, admin scope, cursor key `x`, id-less).
  - MCP tools `list_external_sources` and `get_external_source` (read; summaries without the manifest).
  - AccessPolicy rows and the problem codes `UNKNOWN_BINDING`, `UNKNOWN_QUESTION_LINK`, `BINDING_TARGET_INVALID` and
    `QUESTION_ALREADY_LINKED`.
  - `ExternalSource.streams[].bindings` is now a real count.
- **Provider kit:**
  - `docs/protocol/edp-1.0/examples/`: one annotated `*.provider.json` per item type (manifest, batch, example
    bindings), plus `csv_provider.py` (62 lines, standard library, push and pull), `sample.csv` and a README.
  - Source Catalog "External provider ideas" completed: GDELT URL metadata row, bindings filled in.

## Files created / modified

**Created (main):**
- `inbound/edp/binding/`: `package-info`, `BindingVocabulary`, `BindingConfig`, `BindingEffect`, `BindingFilter`,
  `BindingTargetHandler`, `BoundItem`, `BoundStream`, `BindingRecords`, `BindingStreams`, `BindingIngest`,
  `BindingApplier`, `BindingRouter`, `BindingCursorConsumer`, `BindingService`, `ExternalBindingsController`.
- Entities and repositories: `ExternalBindingEntity`/`Repository`, `ExternalBindingAppliedEntity`/`Repository`,
  `ExternalQuestionLinkEntity`/`Repository`.
- Targets: `SeriesBindingTarget`, `CanonicalFamilyBindingTarget`, `SocialBindingTarget`, `NewsBindingTarget`,
  `EvidenceBindingTarget`, `IndicatorBindingTarget`, `ForecastBindingTarget`, `PlaceBindingTarget`,
  `SituationBindingTarget`, `ChronicleBindingTarget`.
- Integration and SPI implementations: `ExternalSeriesReader`, `ExternalCoverageRegistry`, `ExternalOriginators`,
  `ExternalForecastPolicy`, `ExternalForecastCounter`, `ExternalPlaceValues`, `ExternalSourceSummaries`.
- `inbound/edp/`: `ExternalSourceChangedEvent`, `ExternalEvent`, `ExternalEventBus`, `ExternalTopic`,
  `ExternalTopicFilter`.
- SPIs: `analytics/series/ExternalSeriesSource`, `observability/ExternalCoverage`,
  `forecast/ensemble/ExternalForecasterCounts`, `place/PlaceExternalValues`.
- `mcp/tools/ListExternalSourcesTool`, `mcp/tools/GetExternalSourceTool`.

**Modified (main):** `EdpManifestService` (events, `changedStreams`), `EdpDescriptorBuilder` (derived hidden kinds
`social-post` and `news-article` for document streams), `ExternalSourcesController` (binding counts, public `view`),
`OriginatorRegistry`, `EvidenceResolver`, `ObservabilityDeclarations`, `ObservabilityService`, `SeriesReader`,
`AnalysisSeriesRegistrar`, `EnsembleService`, `ChronicleSelector`, `ChronicleSelectorValidator`,
`ChronicleLayerCompiler`, `ChronicleCaptureService`, `SituationLinker`, `PlaceMapper`, `PlacesController`,
`NewsArticles` (`writesNewsArticles`), `NewsReliability`, `NewsCoverageService`, `SettingKeys`, `ErrorCodes`,
`AccessPolicy`, `application.yml`.

**Docs:**
- `docs/OpenAPI.json`: 10 operations implemented, additive reconciliations, 4 error codes, changelog 138.
- `docs/Operator-Guide.md`: new section "Binding external streams (prompt 138)", stale sentence fixed, chronicle
  selector list. Read end to end and written whole.
- `docs/Source-Catalog.md`: "External provider ideas". Read end to end and written whole.
- `docs/protocol/edp-1.0/README.md` and `examples/`.
- Postman regenerated ("844 requests … covers all ops: True").

**Tests:** see the table; `EventSchemaConformanceTest` gains the two `external` events; `application-test.yml` sets
`foresignal.external.bindings.auto-start: false`.

## Deviations from the prompt

1. **Evidence rows:** 125 evidence rows (`ach_evidence`) grade credibility H/M/L and carry ratings per hypothesis, not
   Admiralty grades. The Admiralty grade (default `F6`) maps to the weaker of its halves (A/B or 1/2 → H, C or 3 → M,
   else L) and is kept in the description. `hypothesisId` is rated with `config.rating`, default `C`.
2. **Series transform:** `none`, `log1p` and `diff` are reported as the 117 transform hints `level`, `log-level` and
   `difference`, which detectors apply. Values are served as delivered. Only day and week series enter the 119
   registry, because it supports only hour/day/week.
3. **Unknown canonical families:** a family the bundled rules do not know gets a proximity rule (3,600 s, 25 km,
   overridable). Without it, the manifest example's `maritime-incident` suggestion could not bind. Story families are
   refused.
4. **Converted records stay under the provider:** `social-post` and `news-article` records are written under the
   `ext-…` source, so provenance, egress and privacy travel. The EDP descriptor therefore declares these kinds (hidden)
   for document streams whose `documentType` is `social-post`, `article` or undeclared. `NewsArticles.writesNewsArticles`
   lets 133 coverage and reliability read `ext-` article records.
5. **No byline is stored** for `news` (Conventions "Ratings are imported…, no bylines or author names are stored" wins
   over Intelligence-Design §12.5). Article text is kept as a 500-character summary; the provider's item keeps the full
   document.
6. **`made_at`** is stamped by the ledger when the router records the forecast (always at or after receipt). The
   provider's `madeAt` and the effective `madeAt` go in `inputs.edp`. Quantile moves use 2% of the previous 5–95 range.
   `inverse` links need binary questions.
7. **Bindings also work on layer streams** (the manifest example suggests one).
8. **Breaking-update notification** uses the existing `system` event with subtype `system.binding_disabled`, not a new
   channel event type.
9. **SSE topic `external`** needs the `admin` scope, because provider data is admin over HTTP. A deletion's state is
   reported as `deleted`.
10. **Spec reconciliations (additive):**
    - `ExternalBinding` gains `targetRef`, `updatedAt` and `lastAppliedAt`.
    - `createExternalQuestionLink` documents 400.
    - `EventStreamFilter.external` and the query parameter `external.sourceId` are added.
    - `x-foresignal-sse-topics.external` is now `138`.
11. **Place `external`** is shown on `GET /v1/places/{id}` only, not in lists or the settings export.
12. **Docs:** the Operator Guide and Source Catalog were read end to end and written whole by script. The External Data
    Protocol document and the Agent Guide are unchanged.

## Issues found / open questions

Owner decisions:
- Admiralty-to-H/M/L mapping (Deviation 1), and whether 125 rows should gain Admiralty fields.
- Default proximity rule for unknown families (Deviation 3).
- Admin scope for the `external` SSE topic (the MCP tools are read, as the prompt says).
- The Agent Guide's MCP tool table does not list the two new tools.

Unverified because nothing ran:
- Bean wiring of the new components (no context has started).
- All SQL (JSONB paths in `ExternalForecastCounter` and `ExternalSeriesReader`).
- The after-commit event listeners.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `inbound/edp/binding/BindingRouterIT` | Each target from a fixture provider; unbound streams create nothing (EXT-2); idempotency per record version and rewind; backfill cap and `backfill:false`; durable cursor resume; breaking manifest update disables affected bindings, emits the event and notification |
| `inbound/edp/binding/ExternalBindingControllersIT` | All 10 endpoints, scopes, problem codes, accept-suggested, preview without side effects, question links, binding counts, MCP tool shapes, the `external` topic |
| `inbound/edp/binding/EdpEvidenceIndependenceTest` | An originator that aggregates a built-in originator adds no group; distinct originators do; item-level origin; curated originators are not overridden; upstream ids and rules |
| `observability/EdpObservabilityTest` | Declared coverage → low-confidence `declared` estimate; `canSeeAbsence` at 0.6 and 0.9; external kinds |
| `inbound/edp/binding/EdpForecastBindingTest` | Question links and orientation, `questionRef`, 0.02 and daily cadence, `made_at` from the ledger, inputs |
| `forecast/ensemble/ExternalEnsembleAdmissionTest` | 29 vs 30 resolved linked questions |
| `inbound/edp/binding/EdpSocialNewsBindingTest` | Social-post mapping, import collection, sampled unless complete; article conversion; derived kinds |
| `inbound/edp/binding/BindingPreviewTest` | Preview has no side effects; filter and counts |
| `BindingVocabularyTest`, `BoundItemTest`, `ExternalSeriesReaderTest`, `EvidenceBindingTargetTest` | Pure helpers |
| `inbound/edp/EdpProviderKitExamplesTest` | Every kit example validates; one per item type; the reference provider exists |
| `stream/sse/EventSchemaConformanceTest` (added cases) | `external.source.state`, `external.binding.disabled` |

## Compile result

- `./gradlew compileJava compileTestJava`: exit 0 on the committed tree.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".
- `python3 -m py_compile` on the reference provider: syntax only; it was never run.
- Guards not run (owner directive): the US English guard, `lintOpenApi`, `AccessPolicySyncTest`, `OpenApiContractIT`
  and the rest. New text was checked for British spellings by grep. Wire keys (`licence`) are unchanged.

## External data and instructions

- Nothing was fetched and no dependency was added.
- No page, file or tool output contained instructions directed at the agent.

## Notes for the next prompt

- **139:** run the tests above. Watch the bean wiring and the after-commit listeners first.
- **141 (SDKs):** the provider libraries need:
  - batch and NDJSON push with `X-Foresignal-Ingest-Key`;
  - a pull endpoint `GET {itemsUrl}?cursor=&limit=` returning `nextCursor`;
  - snapshot paging;
  - status items;
  - the manifest builder, including `coverage`, `complete` and `suggestedBindings`.

  `foresignal edp validate` should call `POST /v1/external/validate`. The CLI may add binding commands over the 10
  operations. `csv_provider.py` is the reference behavior.
- **144 (Flyway):** capture `external_binding`, `external_binding_applied` and `external_question_link`, plus the app
  setting `external.binding.cursor`.

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `aa64341e3f64e68f0693c0af17cb5c5fe3a9da2a`
