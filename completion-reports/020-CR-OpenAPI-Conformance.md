# 020-CR-OpenAPI-Conformance

| | |
|---|---|
| Prompt | 020-OpenAPI-Conformance.md |
| Status | Complete |
| Implementation commit | 34cb25bbb1e2c4342e72540dca062b11d5ea3245 |
| Date | 2026-10-11 |

## Summary

The authored spec is now the served contract, and automated checks keep the code, the spec and the
Postman collection in step.

**Serving (Task 1).**

- `processResources` copies `docs/OpenAPI.json` byte for byte to `openapi/openapi.json` on the
  classpath, outside `static/`. The new `api/OpenApiController` (`getOpenApiDocument`, now
  `implemented`) streams it at `GET /v1/openapi.json` as `application/json`. It is never parsed, so
  the server URL is kept. The path was already open in `AccessPolicy`.
- `ApiVersion.RESOURCE` (now public) names that resource, so `/v1/system.apiVersion` reads the same
  file.
- `/docs` serves Swagger UI (springdoc 3.1.1, built against Spring Boot 4.1.0) over
  `/v1/openapi.json` only (`disable-swagger-default-url`). Its `swagger-config` at
  `/v1/openapi-generated.json/swagger-config` is now an open path.

**Generated spec (Task 2).**

- springdoc serves `/v1/openapi-generated.json` and scans `/v1/**` plus the spec's protocol
  prefixes outside `/v1` (`/compat/**`, `/taxii2/**`, `/.well-known/**`, `/fdsnws/**`), so later
  prompts' routes are compared too. The generated document's own paths are excluded.
- All 62 handlers carry `@Operation(operationId, tags)` and all 13 controllers carry `@Tag`. No
  schema documentation was duplicated.

**Spec (Task 3).**

- Every item of Core-Data-Model *Spec changes* was checked by script against the spec: all schemas,
  properties, parameters, event types and the five error codes are present. Nothing was missing.
  The script's name differences: `listHistory` is `queryHistory`, `updateSourceSettings` is
  `updateSource`, and `TriggerRuleCreate`/`Update` is `TriggerRuleInput`.
- All the event schemas the prompt lists already existed (015, 017, 018). None were added.
- Every SSE operation (7, including the planned ones) now documents `text/event-stream` as an
  `anyOf` of the schemas in its `x-foresignal-sse-events` (Deviation 2). Each map entry states
  `id`, except events of later prompts, which carry a `prompt`.
- `webhooks.notification` and `webhooks.watchdogHeartbeat` already held the headers, body,
  signature recipe and retry ladder. They are now `implemented` (018, 019). Every webhook declares
  `security: []` and `x-foresignal-scope: open`.
- Other reconciliations are listed in Deviation 5 and in `info.x-foresignal-changelog`.

**Contract tests (Tasks 4–6).** Described under "Tests written".

- `src/test/resources/openapi/pending-operations.txt` was seeded from the spec. It has **744
  lines**: every route whose status is `planned`, all owned by prompts 023–141. The full list is at
  the end of this report.
- Postman was regenerated (816 requests). `BODIES` changed in two places: it gained
  `acknowledgeAlert`, and `snoozeAlert` now uses `{"for": "8h"}` because the fixed date would
  expire. All 30 example bodies of implemented operations were checked against their request
  schemas with Python `jsonschema` (Draft 2020-12): 0 failures. The quickstart folder needed no
  change.

**Lint (Task 7).**

- `redocly.yaml` extends `recommended`. It sets `operation-operationId-unique` and
  `operation-4xx-response` to error and `no-unused-components` to warn. A configurable rule requires
  `x-foresignal-scope` in `open|read|agent|admin|peer`.
- `.redocly.lint-ignore.yaml` ignores `no-path-trailing-slash` for the five TAXII 2.1 paths, whose
  trailing slash is deliberate.
- Gradle `lintOpenApi` runs `npx --yes @redocly/cli@2.62.1 lint docs/OpenAPI.json` and is wired
  into `check`. Without `npx` it fails with an install hint, unless `-PskipOpenApiLint=true` is
  passed.
- The first run found 22 errors, all fixed (Deviation 5).
- `./gradlew lintOpenApi` now passes: "Your API description is valid", 70 warnings, 5 ignored.
- `checkSdkGeneration` (optional, not wired to anything) runs
  `@openapitools/openapi-generator-cli@2.41.0`. It pins generator jar 7.26.0 through a generated
  `build/sdk-check/openapitools.json`, and generates `typescript-fetch` and `dart-dio`. It was run
  once and **both generators finished** (BUILD SUCCESSFUL). The errors they logged are under Issues.

**Evolution rules (Task 8).** `docs/Architecture.md` gained §10.1 "API evolution", and `README.md`
gained a "Development" section that links to it.

**Gradle `generatePostman`** (Exec) runs `python3 scripts/generate-postman.py docs/OpenAPI.json
postman`. It is not wired to `compileJava`.

## Files created / modified

**Created (main):** `api/OpenApiController.java`

**Modified (main)**

- Every controller in `api/` (`@Tag`, `@Operation`): `AlertsController`, `ChangesController`,
  `ChannelsController`, `NotificationSettingsController`, `RecordsController`,
  `RetentionController`, `SettingsController`, `SourcesController`, `StreamController`,
  `SystemController`, `TilesController`, `TriggersController`.
- `security/AccessPolicy` (the swagger-config open path) and `system/ApiVersion` (the resource
  path).
- `src/main/resources/application.yml` (springdoc).

**Created (test)**

- `openapi/`: `OpenApiContractIT`, `OpenApiServingIT`, `SpecAnnotationsTest`,
  `OpenApiStructureTest`, `PostmanCollectionContractTest`, `RouteContract` (the comparison and its
  readable diff).
- `stream/sse/EventSchemaConformanceTest`, placed in that package to reuse `SseFixtures` and
  `FakeSender`.
- `security/AccessPolicySyncTest`.
- `testsupport/openapi/`: `OpenApiSpec` (document, operations, pending list, current prompt) and
  `SpecSchemas` (the networknt 2020-12 validator over the spec's components).
- `src/test/resources/openapi/pending-operations.txt`.

**Modified (test):** `security/AccessPolicyTest` (the swagger-config path is open).

**Build, docs, tooling**

- `build.gradle.kts`: the `processResources` target, the test dependency, and the
  `generatePostman`, `lintOpenApi` (in `check`), `checkSdkGeneration` and
  `checkSdkGeneration-<generator>` tasks.
- `gradle/libs.versions.toml`, `redocly.yaml`, `.redocly.lint-ignore.yaml`.
- `docs/OpenAPI.json`, `docs/Architecture.md`, `README.md`.
- `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`.

**New dependency (test only):** `com.networknt:json-schema-validator` 3.0.8, Apache-2.0. It is the
latest release on Maven Central (`maven-metadata.xml` lists `<release>3.0.8</release>`). 3.x is
built on Jackson 3 (`tools.jackson`), the same as Boot 4. It brings `com.ethlo.time:itu` 1.14.0,
Apache-2.0. Its Jackson modules are managed by the Boot BOM.

**npm tools (run through `npx`, not installed into the repository):**

- `@redocly/cli` 2.62.1, the latest 2.x on npm (`npm view @redocly/cli version`).
- `@openapitools/openapi-generator-cli` 2.41.0 with generator jar 7.26.0, the latest
  `org.openapitools:openapi-generator-cli` on Maven Central.

No Flyway, Liquibase or SQL files. No `.gitlab-ci.yml` change. Nothing was ported from God's Eye
View.

## Deviations from the prompt

1. **The spec is served by a controller, not as a static resource.**
   - Static serving worked, but the contract test requires authored minus pending to equal
     generated. `getOpenApiDocument` is 020's own operation, so it cannot stay pending, and
     springdoc does not see static resources.
   - A controller streaming the classpath resource makes the operation real and still byte for
     byte.
   - So the static spec path is not excluded from springdoc. Only the generated document's own
     endpoints are.
2. **The SSE stream schemas are `anyOf`, not `oneOf`.**
   - The SSE `event:` line, not the data, selects the schema. Several payloads match more than one
     schema: heartbeat `{}` matches every all-optional schema, and `ResetEvent` overlaps others.
   - So `oneOf` is unsatisfiable for a validator, and Redocly flagged 9 overlaps
     (`no-illogical-composition-keywords`).
   - The schema's description says so, and the changelog records it.
3. **The scope enum includes `peer`.**
   - The spec already uses `x-foresignal-scope: peer` on 5 federation and exchange operations
     (113, 132, 134).
   - The lint rule, `SpecAnnotationsTest` and §10.1 accept `open|read|agent|admin|peer`.
   - The Postman generator gives `peer` requests no auth, as it did before. 113 can add a
     `{{peerCredential}}` variable.
4. **The "current prompt" is derived, and two places only check part of the spec.**
   - `SpecAnnotationsTest` fails planned operations owned by prompts ≤ the current one. It takes
     the current prompt from the highest `completion-reports/NNN-CR-*.md`, never lower than 020, so
     every checkpoint enforces it without editing the test.
   - `OpenApiStructureTest` requires an `id` flag on every SSE event entry except those carrying a
     later `prompt`. The intelligence-topic entries of `streamEvents` (101–129) declare `id` when
     they are built.
5. **Spec reconciliations** (additive, in `info.x-foresignal-changelog`; `info.version` stays 1.3.0
   as in 013–019):
   - `getOpenApiDocument` and the two 018/019 webhooks are `implemented`.
   - The text/event-stream schemas are `anyOf`, as Deviation 2 explains.
   - Every webhook has `security: []`. The lint's `security-defined` rule required it (6 errors).
   - `getOpenApiDocument`, `getSystem`, `getVapidPublicKey`, `getJwks`, `getSystemJwks` and
     `getEdpProtocol` gained a `4XX` problem response. `operation-4xx-response` required it (6
     errors).
   - `Alert.ruleName` is nullable. `AlertView` documents null once the rule is deleted, and
     `AlertMapper` writes it.
   - `streamAlerts`' `alert.enriched` entry carries `prompt: "030"`, as on `streamEvents`.
6. **The status exception list.** `RouteContract.STATUS_EXCEPTIONS` holds `createTrigger`,
   `createChannel` (201) and `deleteTrigger`, `deleteChannel` (204). They return `ResponseEntity`
   with a run-time status that springdoc reports as 200. `setSourceCredentials` and
   `deleteSourceCredentials` use `@ResponseStatus(NO_CONTENT)`, which springdoc does read.
7. **Postman body checks.**
   - Typed DTOs (`updateSource`, `setSourceCredentials`, `updateCors`) are checked with Jackson
     plus Bean Validation. The mapper used has `DurationShorthandModule`, as production does, and
     fails on unknown members, so an example key the DTO ignores is caught.
   - JsonNode bodies run through their real parsers: `NotificationSettingsService.parse`,
     `WatchdogSettingsService.parse`, `AlertService.transition`, `Snoozes.expiry`, and
     `ChannelInputParser` with the 15 channel types.
   - Trigger rules, retention, import and event-stream filters need the database or the registry,
     so they are checked against their schema only. A test pins that list.
   - Every implemented body is also validated against its spec request schema.
8. **An extra test.** `AccessPolicySyncTest` (unit) checks `AccessPolicy` against the scope of every
   *implemented* route, and excludes webhooks as the prompt asks. No such sync check existed before;
   invariant PRC-1 requires the policy to match. A Python simulation of the policy table found 0
   mismatches over the 62 implemented routes.

## Issues found / open questions

1. **Invariant PRC-1 names a different test.** `docs/Invariants.md` lists `PostmanCoverageTest`;
   the prompt names `PostmanCollectionContractTest`, which was used. **Owner:** rename one of them?
2. **`AccessPolicy` is far behind the spec for planned routes.** The Python simulation found 555
   differences across all 806 routes. They do not fail anything now, but each prompt that
   implements an operation must add or fix its rule, or `AccessPolicySyncTest` fails.
3. **SDK generator errors** (generator 7.26.0; both runs finished):
   - `OpenAPINormalizer`: "Type object not yet supported in openapi-normalizer to process OpenAPI
     3.1 spec with multiple types". These are the OAS 3.1 nullable unions such as
     `["object","null"]`, in both generators, including `dart-dio`.
   - `InlineModelResolver`: "Illegal schema found with $ref combined with other properties"
     (`EventStreamFilter.alerts` puts `properties` next to `$ref`).
   - Warnings:
     - name clashes, renamed by the generators: `Record` → `ModelRecord` (TypeScript), `Source`,
       `Null` → `ModelSource`, `ModelNull` (Dart);
     - file path collisions on case-insensitive filesystems (`SystemApi`, `SinksApi`,
       `ExternalApi`, `ChannelsApi` in TypeScript);
     - `text/plain` on `/compat/ntfy/{topic}` is not supported by dart-dio.
   - Prompt 141 owns the pre-processed spec.
4. **A property named `$id`.** `OgcQueryables` has a property called `$id`. Python's `jsonschema`
   4.10 crashes while resolving `$ref`s in a document that contains it. networknt is not expected
   to have this problem, but it was not verified because tests were not run.
5. Owner decisions carried from 012–019 are still open.

No instructions directed at an agent were found in data, tool output, npm or Maven metadata, or the
generator logs.

## Tests written (not run)

As a feature prompt requires, no tests were run.

- **`OpenApiContractIT`** (`@IntegrationTest`, MockMvc, shares `SourcesControllerIT`'s context):
  - fetches `/v1/openapi-generated.json`;
  - checks that authored minus pending equals generated in both directions, with path variables
    normalized, operationIds matching per route, and authored 2xx codes contained in the generated
    ones (with the exception list);
  - a stale pending entry (`getSystem` added to the list) fails with "remove it from
    pending-operations.txt";
  - an unlisted, unimplemented operation is reported missing in code;
  - generated routes have operationIds and spec tags;
  - the generated document excludes itself;
  - `/v1/openapi.json` is byte-identical to the file.
  - On failure, `RouteContract.Report.describe()` prints the diff in sections: missing in code,
    missing in spec, stale pending, operationId and status mismatches.
- **`OpenApiServingIT`:** `application/json`; no token needed, and an invalid token does not block
  it; byte equality including `servers`; `/docs` answers 200 or redirects to `swagger-ui`; the
  swagger-config is open and points at `/v1/openapi.json`.
- **`SpecAnnotationsTest`:**
  - every route and webhook has a valid scope, prompt and status;
  - operationIds are unique;
  - implemented operations are not pending; planned routes are pending under their own prompt;
  - the pending file lists only existing routes;
  - no planned operation is at or below the current prompt ("implement or re-assign").
- **`OpenApiStructureTest`:**
  - the document is OAS 3.1;
  - `webhooks.notification` has the signature headers, the `WebhookEnvelope` body, the recipe and
    the ladder; `watchdogHeartbeat` is a bodiless GET and POST; all webhooks are open and declare
    security;
  - every SSE operation has `x-foresignal-sse-events`, with existing schemas listed in its
    `anyOf` and `id` flags;
  - only `change` has an id on `/v1/changes/stream`;
  - the event schemas exist;
  - every operation has a scope.
- **`EventSchemaConformanceTest`** (unit; networknt 2020-12, `$ref`s resolved within the spec):
  - `SSE_EVENTS` maps 13 event names to factories that use the real serializers:
    - `hello` through `SseHub.open`;
    - `live`, `touch`, `resync`, `filter-applied`, `heartbeat` and `reset` (all three reasons)
      through `SseClient`;
    - `change` through `RecordMapper.streamChange` from both a row and a summary, sent by
      `SseClient` with the deferred payload loaded;
    - the alert events through `AlertMapper.event`, with a snapshot from `AlertSnapshots.build`;
    - `notification` and `system` through `NotificationJson`.
  - The table must equal the current event names of `streamChanges`, `streamAlerts` and
    `streamEvents`, so a new event name without a schema, or one the spec lacks, fails.
  - An `AlertEvent` of each of the five types validates, with the rule present and deleted.
  - The `NotificationMessage` instances validate.
  - The `webhook` channel's rendered default body validates against `WebhookEnvelope` and carries
    the signature headers.
  - A negative check proves the validator rejects bad payloads.
- **`PostmanCollectionContractTest`** (unit, no Spring):
  - every route operationId appears as ``operationId: `<id>` ``, and no request names an unknown
    operation;
  - auth follows the scope (`admin` → `{{adminToken}}`, `agent` → `{{agentToken}}`, `ingestRecords`
    → `X-Foresignal-Ingest-Key: {{ingestKey}}`, everything else `noauth`);
  - the bodies of implemented operations pass their schema and their implemented validation, and
    pending operations are skipped;
  - the validator table is complete;
  - a negative check.
- **`AccessPolicySyncTest`:** implemented routes against `AccessPolicy`; webhooks excluded.

## Compile result

```
$ ./gradlew compileJava compileTestJava
BUILD SUCCESSFUL in 1s
```

```
$ ./gradlew lintOpenApi
Woohoo! Your API description is valid. 🎉
You have 70 warnings.
5 problems are explicitly ignored.
BUILD SUCCESSFUL in 1s
```

## Notes for the next prompt

1. **Every prompt that implements an operation:**
   - set its `x-foresignal-status` to `implemented` and delete its line from
     `src/test/resources/openapi/pending-operations.txt`;
   - add `@Operation(operationId = "...", tags = "...")` (and `@Tag` on a new controller);
   - make `AccessPolicy` match its scope;
   - if the controller returns a run-time status through `ResponseEntity` other than 200, use
     `@ResponseStatus` or add it to `RouteContract.STATUS_EXCEPTIONS` with a comment;
   - after any spec change, run `./gradlew generatePostman` and `./gradlew lintOpenApi` and add a
     changelog entry.
2. **Prompts that add an SSE event:** add it to the spec's `x-foresignal-sse-events` with `id`, and
   to `EventSchemaConformanceTest.SSE_EVENTS` with a factory that uses the real serializer.
   Implementing a later-prompt event means removing its `prompt` gate, or letting the current
   prompt pass it.
3. **Prompts that implement a webhook** (024 `edpProviderItems`/`edpProviderSearch`, 110
   `sinkCloudEvent`): set its status to `implemented`. Webhooks are not on the pending list.
4. **022 (checkpoint):**
   - `OpenApiContractIT` and `OpenApiServingIT` share `SourcesControllerIT`'s cached context.
   - The other new tests are unit tests.
   - `lintOpenApi` runs inside `check` (Node.js ≥ 20.19 with `npx`; this host has v24.20.0).
   - Issue 4 is the only uncertainty about the validator.
5. **023:**
   - delete the 15 `023` lines;
   - `getSystem` already has a `4XX` response;
   - the `peer` scope stays open until 113.
6. **141 (SDKs):** see Issue 3. `checkSdkGeneration` reproduces it.
7. **142** requires `pending-operations.txt` to be empty. The file has no header or comments, so
   empty means zero bytes.
8. **Owner decisions pending:** Issue 1, plus those carried from 012–019.

## Pending operations (744, from `pending-operations.txt`)

| Prompt | Count | Operations |
|---|---|---|
| 023 | 15 | `listTokens`, `createToken`, `revokeToken`, `getSecuritySettings`, `updateSecuritySettings`, `getServerSettings`, `updateServerSettings`, `createStreamTicket`, `updateCurrentTokenDevice`, `listPairingCodes`, `createPairingCode`, `deletePairingCode`, `getPairingCodeQr`, `claimPairingCode`, `listAuditEvents` |
| 024 | 31 | `listCustomSources`, `createCustomSource`, `getCustomSource`, `updateCustomSource`, `deleteCustomSource`, `rotateIngestKey`, `ingestRecords`, `ingestRecordsFormat`, `ingestWebhookHandshake`, `listIngestFormats`, `ntfyPublish`, `ntfyPublishPut`, `ntfyPublishViaGet`, `ntfyPublishViaPost`, `ntfyPublishJson`, `gotifyPublish`, `getEdpProtocol`, `getEdpSchema`, `listExternalSources`, `registerExternalSource`, `getExternalSource`, `deleteExternalSource`, `updateExternalSourceManifest`, `approveExternalSource`, `suspendExternalSource`, `pushExternalItems`, `listRegistrationTokens`, `createRegistrationToken`, `deleteRegistrationToken`, `selfRegisterExternalSource`, `validateEdp` |
| 026 | 10 | `listLlmProviders`, `createLlmProvider`, `getLlmProvider`, `updateLlmProvider`, `deleteLlmProvider`, `testLlmProvider`, `listLlmProviderModels`, `getLlmSettings`, `updateLlmSettings`, `getLlmUsage` |
| 027 | 4 | `search`, `getSearchStatus`, `reindexSearch`, `cancelReindex` |
| 028 | 7 | `ask`, `askStream`, `listAskThreads`, `getAskThread`, `deleteAskThread`, `confirmAskAction`, `rejectAskAction` |
| 029 | 11 | `listBriefings`, `createBriefing`, `previewBriefing`, `getBriefing`, `updateBriefing`, `deleteBriefing`, `runBriefing`, `listBriefingRuns`, `getBriefingRun`, `getLatestBriefingRun`, `getDigest` |
| 031 | 5 | `getVapidPublicKey`, `listPushSubscriptions`, `createPushSubscription`, `deletePushSubscription`, `verifyPushSubscription` |
| 032 | 9 | `listPushProviders`, `putPushProvider`, `deletePushProvider`, `testPushProvider`, `listPushDevices`, `registerPushDevice`, `updatePushDevice`, `deletePushDevice`, `testPushDevice` |
| 033 | 17 | `getActionConfirm`, `executeAction`, `listNotifications`, `getNotification`, `markNotificationsRead`, `getNotificationBadge`, `streamNotifications`, `updatePresence`, `listClients`, `listEscalationPolicies`, `createEscalationPolicy`, `getEscalationPolicy`, `updateEscalationPolicy`, `deleteEscalationPolicy`, `getAlertEscalation`, `muteTrigger`, `unmuteTrigger` |
| 035 | 7 | `listFeedKeys`, `createFeedKey`, `revokeFeedKey`, `getRateLimits`, `updateRateLimits`, `getOutboundSettings`, `updateOutboundSettings` |
| 036 | 25 | `listPlaces`, `createPlace`, `getPlace`, `updatePlace`, `deletePlace`, `getPlacesAoi`, `listViews`, `createView`, `getView`, `updateView`, `deleteView`, `getViewRecords`, `getViewTile`, `getViewTileJson`, `streamView`, `listWatchlists`, `createWatchlist`, `getWatchlist`, `updateWatchlist`, `deleteWatchlist`, `listWatchlistItems`, `addWatchlistItems`, `removeWatchlistItems`, `importWatchlist`, `exportWatchlist` |
| 037 | 3 | `getRecordTrack`, `getHistoryFrames`, `getTimeline` |
| 038 | 8 | `getStatsTimeseries`, `getStatsGrid`, `getStatsTop`, `rebuildStatsRollup`, `getGlance`, `getMetrics`, `getMetricSettings`, `updateMetricSettings` |
| 039 | 10 | `ingestRecordsFormatGet`, `listIngestDevices`, `createIngestDevice`, `deleteIngestDevice`, `rotateIngestDeviceKey`, `purgeDevice`, `exportRecord`, `purgeRecord`, `ingestRecordsPathKeyGet`, `ingestRecordsPathKey` |
| 040 | 4 | `getReference`, `loadReference`, `listReferenceCountries`, `reverseGeocode` |
| 042 | 4 | `listProducts`, `getProduct`, `listProductFrames`, `getLatestProductFrame` |
| 043 | 3 | `getMedia`, `getMediaThumbnail`, `getMediaMeta` |
| 045 | 8 | `getProductTile`, `getProductTileJson`, `getProductImage`, `getProductLoop`, `listStyles`, `getStyle`, `getProductLegend`, `getProductLegendPng` |
| 049 | 1 | `getGroundStationPolarplot` |
| 050 | 3 | `listTileKeys`, `createTileKey`, `revokeTileKey` |
| 051 | 4 | `getProductFieldManifest`, `getProductFieldData`, `getProductVectorTile`, `getProductTimeline` |
| 052 | 5 | `getSample`, `postSample`, `getProductSeries`, `postProductStats`, `getPlaceWeather` |
| 054 | 2 | `ingestSourceMedia`, `ingestSourceRasters` |
| 055 | 1 | `getStaticMap` |
| 056 | 2 | `getWeatherSettings`, `updateWeatherSettings` |
| 058 | 11 | `getMapStyle`, `getMapSprite`, `getMapIcon`, `getAlertsAtom`, `getAlertsJsonFeed`, `getBriefingRunsAtom`, `getBriefingRunsJsonFeed`, `getKmlNetworkLink`, `getKmlRecords`, `getKmlTracks`, `getTriggerKml` |
| 059 | 5 | `previewMapping`, `suggestMapping`, `listMappingPresets`, `listCustomSourceSamples`, `testCustomSourceMapping` |
| 060 | 1 | `republishMqttChannel` |
| 061 | 1 | `getChannelInteractiveStatus` |
| 064 | 2 | `getCelestrakGp`, `getCelestrakGpAlias` |
| 070 | 1 | `searchTransitCatalog` |
| 073 | 2 | `verifyWebSubSubscription`, `receiveWebSubNotification` |
| 084 | 1 | `getChannelAirtime` |
| 093 | 2 | `listSeries`, `getSeriesPoints` |
| 098 | 7 | `listEntities`, `resolveEntity`, `getEntity`, `listEntityRecords`, `addEntityKey`, `retractEntityKey`, `mergeEntities` |
| 100 | 4 | `getTriggerWindowState`, `getTriggerBaseline`, `rebuildBaseline`, `listAnomalies` |
| 101 | 20 | `listIntelEvents`, `createIntelEvent`, `getIntelEvent`, `updateIntelEvent`, `addIntelEventMember`, `removeIntelEventMember`, `mergeIntelEvent`, `listIntelRecipes`, `updateRecipe`, `testRecipe`, `listInvestigations`, `createInvestigation`, `getInvestigation`, `updateInvestigation`, `deleteInvestigation`, `addInvestigationItem`, `removeInvestigationItem`, `exportInvestigation`, `computeExposure`, `importPopulationPack` |
| 102 | 16 | `listAnalystProfiles`, `createAnalystProfile`, `getAnalystProfile`, `updateAnalystProfile`, `deleteAnalystProfile`, `runAnalystProfile`, `listAnalystRuns`, `getAnalystRun`, `listAnalystNotes`, `updateAnalystNote`, `analystNoteFeedback`, `listAnalystProposals`, `confirmAnalystProposal`, `rejectAnalystProposal`, `analystProposalFeedback`, `getAnalystEvaluation` |
| 103 | 14 | `createBacktest`, `listBacktests`, `getBacktest`, `cancelBacktest`, `listPrecursorTests`, `createPrecursorTest`, `getPrecursorTest`, `updatePrecursorTest`, `deletePrecursorTest`, `testPrecursorTest`, `getTriggerQuality`, `getTriggerSuggestions`, `getJob`, `cancelJob` |
| 104 | 8 | `listInfraNodes`, `getInfraNode`, `listInfraEdges`, `computeAffectedInfrastructure`, `getInfraStatus`, `rebuildInfraGraph`, `getInfraRules`, `updateInfraRules` |
| 106 | 3 | `sampleTrack`, `getProductFieldVelocityJson`, `getProductFieldPngUv` |
| 107 | 1 | `getCelestrakGroupTle` |
| 108 | 29 | `getProductTileTms`, `getProductTileWebp`, `getProductDataTile`, `getProductFieldI16`, `getProductFieldTile`, `getProductLoopGif`, `getProductStyleFragment`, `getProductsStyleFragment`, `getProductMobacXml`, `getWmtsCapabilities`, `getWmtsKvp`, `getWmtsTile`, `ogcLanding`, `ogcConformance`, `ogcCollections`, `ogcCollection`, `ogcApiDefinition`, `ogcTileMatrixSets`, `ogcTileMatrixSet`, `ogcCollectionTilesets`, `ogcCollectionTileset`, `ogcCollectionTile`, `edrPosition`, `edrArea`, `edrInstances`, `createProductExport`, `getExport`, `downloadExportFile`, `createPlacePack` |
| 109 | 4 | `getAttributions`, `getJwks`, `getSystemJwks`, `rotateSigningKey` |
| 110 | 9 | `listSinkTypes`, `listSinks`, `createSink`, `getSink`, `updateSink`, `deleteSink`, `testSink`, `getSinkStatus`, `resyncSink` |
| 111 | 14 | `deleteExport`, `createExport`, `listExports`, `downloadExport`, `getExportManifest`, `createSnapshot`, `listHolds`, `createHold`, `deleteHold`, `listHoldVersions`, `getArchiveSettings`, `updateArchiveSettings`, `getArchiveCatalog`, `createPack` |
| 112 | 14 | `ogcItems`, `ogcItem`, `ogcQueryables`, `capAtom`, `getCapForAlert`, `getCapMessage`, `getCalendar`, `createShareLink`, `listShareLinks`, `revokeShareLink`, `openShareLink`, `openShareLinkGeoJson`, `getShareLinkTile`, `downloadShareLink` |
| 113 | 15 | `getFederationInfo`, `listFederationChanges`, `streamFederation`, `getFederationSnapshot`, `federationIngest`, `getFederationBundle`, `receiveFederationBundle`, `listPeers`, `createPeer`, `getPeer`, `updatePeer`, `deletePeer`, `testPeer`, `resyncPeer`, `rotatePeerIngestKey` |
| 114 | 17 | `listPolSubjects`, `createPolSubject`, `deletePolSubject`, `listPolPlaces`, `updatePolPlace`, `listPolStays`, `recomputePolSubject`, `listTrips`, `createTrip`, `getTrip`, `updateTrip`, `deleteTrip`, `checkTrip`, `listTripRuns`, `getCpa`, `getEta`, `cpaBatch` |
| 115 | 9 | `createExercise`, `listExercises`, `getExercise`, `cancelExercise`, `purgeExerciseAlerts`, `applyTuningSuggestion`, `dismissTuningSuggestion`, `undoTuning`, `listTuningLog` |
| 116 | 8 | `listCanonicalEvents`, `getCanonicalEvent`, `getRecordEvidence`, `getObservability`, `getObservabilityTile`, `getManifest`, `replayManifest`, `getPromptTemplate` |
| 117 | 6 | `listAttentionTopics`, `createAttentionTopic`, `getAttentionTopic`, `updateAttentionTopic`, `deleteAttentionTopic`, `backfillCftcCot` |
| 118 | 13 | `listSituations`, `createSituation`, `getSituation`, `updateSituation`, `listSituationMembers`, `addSituationMember`, `removeSituationMember`, `mergeSituation`, `splitSituation`, `regenerateSituationBrief`, `getSituationTimeline`, `getSituationChanges`, `testSituationLinker` |
| 119 | 8 | `getAnalyticsSeriesData`, `listAnalysisSeries`, `upsertAnalysisSeries`, `deleteAnalysisSeries`, `listFindings`, `getFinding`, `updateFinding`, `createEventStudy` |
| 120 | 10 | `listPrecursors`, `getPrecursor`, `updatePrecursor`, `listPrecursorBatches`, `getPrecursorBatch`, `listPrecursorCandidates`, `createPrecursorCandidate`, `deletePrecursorCandidate`, `minePrecursors`, `retestPrecursors` |
| 121 | 12 | `createReferenceImport`, `listReferenceImports`, `getReferenceImport`, `deleteReferenceImport`, `listBaseRates`, `createBaseRate`, `queryBaseRate`, `refreshBaseRates`, `getBaseRate`, `deleteBaseRate`, `getSituationAnalogs`, `queryAnalogs` |
| 122 | 17 | `listForecastQuestions`, `createForecastQuestion`, `getForecastQuestion`, `updateForecastQuestion`, `annulForecastQuestion`, `resolveForecastQuestion`, `getForecastQuestionHistory`, `submitForecast`, `listForecasts`, `listForecasters`, `updateForecaster`, `getForecastScorecard`, `getForecastCalibration`, `compareForecasters`, `createForecastEvaluation`, `getForecastEvaluation`, `refitForecastEnsemble` |
| 123 | 7 | `createBenchmarkRun`, `listBenchmarkRuns`, `getBenchmarkRun`, `getBenchmarkStatus`, `listBenchmarkCases`, `createBenchmarkCase`, `deleteBenchmarkCase` |
| 125 | 20 | `listHypothesisSets`, `createHypothesisSet`, `draftHypothesisSet`, `getHypothesisSet`, `updateHypothesisSet`, `listHypothesisRevisions`, `getAchMatrix`, `reviewHypothesisSet`, `addHypothesis`, `updateHypothesis`, `createIndicator`, `listIndicators`, `updateIndicator`, `retireIndicator`, `addAchEvidence`, `removeAchEvidence`, `createKeyAssumption`, `updateKeyAssumption`, `getInformationValue`, `listCollectionGaps` |
| 126 | 9 | `listRegionalOutlooks`, `getRegionalOutlook`, `listRegionalIndices`, `getRegionalIndex`, `putRegionalIndex`, `fitRegionalIndex`, `applyRegionalIndexWeights`, `getSeriesForecast`, `getSeriesForecastModel` |
| 127 | 3 | `listProjectedImpacts`, `getProjectedImpact`, `listImpactRates` |
| 128 | 17 | `listAnalysisCycles`, `createAnalysisCycle`, `getAnalysisCycle`, `updateAnalysisCycle`, `deleteAnalysisCycle`, `runAnalysisCycle`, `listCycleRuns`, `getCycleRun`, `runAnalysisMaintenance`, `listAssessments`, `getLatestAssessment`, `getAssessment`, `getAssessmentBundle`, `submitAssessmentFeedback`, `createSituationAssessment`, `listSituationAssessments`, `getIntelBoard` |
| 129 | 27 | `getContract`, `getObject`, `resolveObjects`, `getObjectDossier`, `listRelatedObjects`, `summarizeContext`, `queryGraph`, `findCommonDependencies`, `createRelation`, `deleteRelation`, `listObjectAnnotations`, `createAnnotation`, `updateAnnotation`, `deleteAnnotation`, `getPrecision`, `listSensors`, `listWorkspaces`, `createWorkspace`, `getWorkspace`, `updateWorkspace`, `deleteWorkspace`, `putWorkspaceLayout`, `patchWorkspaceContext`, `getWorkspaceSummary`, `getWorkspaceContextLink`, `createContextLink`, `shareWorkspace` |
| 130 | 47 | `getMe`, `listMyNotificationPreferences`, `replaceMyNotificationPreferences`, `listInbox`, `listPrincipals`, `createPrincipal`, `getPrincipal`, `updatePrincipal`, `createPrincipalToken`, `listGroups`, `createGroup`, `getGroup`, `updateGroup`, `deleteGroup`, `addGroupMembers`, `removeGroupMembers`, `getObjectAcl`, `replaceObjectAcl`, `shareObject`, `listSharedWithMe`, `listMyObjects`, `listTags`, `createTag`, `getTag`, `updateTag`, `deleteTag`, `assignTag`, `unassignTag`, `listTaggedObjects`, `listObjectTags`, `replyToAnnotation`, `resolveAnnotation`, `uploadAttachment`, `getAttachment`, `deleteAttachment`, `getAttachmentContent`, `listEvidenceItems`, `createEvidenceItem`, `getEvidenceItem`, `updateEvidenceItem`, `deleteEvidenceItem`, `listSavedSearches`, `createSavedSearch`, `getSavedSearch`, `updateSavedSearch`, `deleteSavedSearch`, `runSavedSearch` |
| 131 | 25 | `listChronicles`, `createChronicle`, `suggestChronicle`, `getChronicle`, `updateChronicle`, `deleteChronicle`, `createChronicleLayer`, `updateChronicleLayer`, `deleteChronicleLayer`, `activateChronicle`, `pauseChronicle`, `resumeChronicle`, `stopChronicle`, `archiveChronicle`, `restoreChronicle`, `getChronicleEstimate`, `exportChronicle`, `getChronicleThread`, `markChronicleThreadRead`, `getChronicleState`, `getChronicleTile`, `getChronicleFrames`, `getChronicleTrack`, `getChronicleSeries`, `getChronicleSummary` |
| 132 | 27 | `listReports`, `createReport`, `draftReport`, `getReport`, `updateReport`, `deleteReport`, `createReportBlock`, `updateReportBlock`, `deleteReportBlock`, `publishReport`, `retractReport`, `listReportRevisions`, `getReportRendition`, `sendReport`, `createReportShareLink`, `openReportShareLink`, `createExchangePackage`, `getExchangePackage`, `downloadExchangePackage`, `receiveExchangePackage`, `receiveExchangeReceipt`, `importExchangePackage`, `listReceivedPackages`, `getReceivedPackage`, `getReceivedPackageRendition`, `acknowledgeReceivedPackage`, `listSentPackages` |
| 133 | 10 | `listNewsStories`, `getNewsStory`, `listNewsArticles`, `listMediaOutlets`, `getMediaOutlet`, `updateMediaOutlet`, `listMediaProviders`, `getMediaProvider`, `updateMediaProvider`, `importMediaRatings` |
| 134 | 12 | `listRelatedChronicles`, `setChronicleDiscoverability`, `dismissChronicleLink`, `requestChronicleAccess`, `listChronicleClusters`, `getChronicleCluster`, `getChronicleClusterTimeline`, `listChronicleAccessRequests`, `grantChronicleAccessRequest`, `declineChronicleAccessRequest`, `setChronicleNetworkSalt`, `receiveChronicleSketches` |
| 135 | 7 | `listSocialPlatforms`, `getSocialPlatform`, `updateSocialPlatform`, `getSocialCoverage`, `getSocialVolume`, `listSocialPosts`, `getSocialPost` |
| 136 | 11 | `listNarratives`, `getNarrative`, `listNarrativePosts`, `getNarrativePropagation`, `getNarrativeTimeline`, `mergeNarratives`, `splitNarrative`, `listClaims`, `getClaim`, `getMediaFingerprint`, `matchMedia` |
| 137 | 8 | `listSocialComparisons`, `getSocialComparison`, `listSocialCues`, `createSocialCue`, `getSocialCue`, `getSocialLeadTimes`, `getSocialSettings`, `updateSocialSettings` |
| 138 | 10 | `listExternalBindings`, `createExternalBinding`, `acceptSuggestedBindings`, `getExternalBinding`, `updateExternalBinding`, `deleteExternalBinding`, `previewExternalBinding`, `listExternalQuestionLinks`, `createExternalQuestionLink`, `deleteExternalQuestionLink` |
| 140 | 3 | `runSourceDoctor`, `listSourceDoctorReports`, `getDoctorReport` |
| 141 | 18 | `getProtectedResourceMetadata`, `getMcpProtectedResourceMetadata`, `getMcpAuthSettings`, `updateMcpAuthSettings`, `listPlugins`, `updatePluginSettings`, `getPlugin`, `updatePlugin`, `queryFdsnEvents`, `getFdsnEventVersion`, `getFdsnEventWadl`, `exportStix`, `getTaxiiDiscovery`, `getTaxiiApiRoot`, `listTaxiiCollections`, `getTaxiiCollection`, `getTaxiiCollectionObjects`, `getBriefingPodcast` |
