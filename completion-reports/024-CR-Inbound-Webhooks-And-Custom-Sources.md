# 024-CR-Inbound-Webhooks-And-Custom-Sources

| | |
|---|---|
| Prompt | 024-Inbound-Webhooks-And-Custom-Sources.md |
| Status | Complete |
| Implementation commit | d97eb51a59bd8e5a7af2df87bd97a64d0cf6e17e |
| Date | 2026-10-12 |

## Summary

Anything can now push data into Foresignal, and third-party providers can register through the
External Data Protocol (EDP 1.0) core. All of the prompt's sections landed:

- **Custom sources** (`custom_source`): create, update, delete and rotate.
  - The descriptor is built from the row: PUSH, `custom=true`, STANDARD, the chosen time base, TTL
    and anchor, volatile/significant/title fields, `fieldSpecs`, `private`, license, budget
    `rateLimitPerMinute` (× 1440 per day) and `zeroResultsNormal`.
  - The ingest key `eik_…` is shown once and stored as SHA-256 plus an 8-character prefix.
  - Updates refuse `KIND_IN_USE` and `OBSERVED_AT_MODE_LOCKED` and re-run `IngestPolicyValidator`.
    They re-register the source (`REDEFINED`) and publish its settings event, so the writer's
    existing lazy recipe migration (009) runs before the next batch.
  - Every persisted source is registered at startup, before the web server and the writer,
    trigger engine and scheduler start.
- **Auth:** `IngestAuthenticator` behind the `INGEST_KEY_OR_ADMIN` extension point in
  `TokenFilter`. It accepts the key as the header, as Bearer `eik_…`, as the Basic password, as
  ntfy `?auth=`, and as Gotify `X-Gotify-Key` or `?token=`; an admin token also works. A key is
  bound to its source. Ingest-key writes are audited as `ingest_key`, and `auth`/`token` query
  values are masked in logs and in `fetch_log`.
- **`PushIngestService`**, shared by every endpoint:
  - refusals: 404, `SOURCE_DISABLED`, request budget (429 `RATE_LIMITED`), records-per-minute
    bucket (429 `INGEST_RECORD_RATE`), each with `Retry-After` and a `REFUSED` fetch row;
  - limits: 5 MB (`gzip` decompressed first) and 10,000 records;
  - one PUSH batch with principal, bytes, body hash, redacted URL and license; queue timeout →
    503 `INGEST_BUSY`;
  - usage and health are recorded;
  - the response is the v1.2 `IngestOutcome`, with `changed` as an alias.
- **IngestFormat SPI:**
  - formats: `native`, `alertmanager`, `cloudevents` (structured, batch, binary and the `OPTIONS`
    handshake), `cap`, and the JSON-Pointer `mapping`;
  - the shared `CapParser`, with secure XML;
  - signature verifiers: `none`, `hmac-sha256`, `standard-webhooks` (±5 min, 10-minute replay
    cache) and `foresignal`, with `keyOptional`;
  - `media`, `rasters` and `mapping-preview` are reserved.
- **ntfy- and Gotify-compatible publish** under `/compat/**`, in every shape the prompt lists. A
  message carrying this instance's own origin marker is dropped (018's loop guard).
- **EDP core:**
  - **Manifests:** validation (schema, protocol minor, delivery mechanism, unique streams, layer
    item types, `payloadSchema`s compiled with networknt plus RE2/J, 64 KB, local `$ref`s only).
    The `ext-{id}` custom source gets its descriptor from the manifest, including egress from
    `redistribution`, the originator and the access block.
  - **Registration:** by operator (manifest or URL), by registration token (`fsx_…`), approve,
    suspend, delete, and manifest updates (additive or breaking → pending).
  - **Items:** push as JSON or NDJSON, with per-item validation in the §3.1 order and the item
    mapper; status items become health only.
  - **Snapshots** per §3.2, with staging, the window, the absent ratio and a hold.
  - **Pull:** a POLLING adapter with cursors, the source budget and credentials.
  - **Security:** `EdpUrlGuard` (resolve once, pin the address, block private/metadata ranges,
    same-host redirects only); validation dry-run; the protocol and its schemas served.

**API:**
- 31 operations owned by 024 plus the new `allowExternalSnapshot` are `implemented` and off
  `pending-operations.txt`.
- The `edpProviderItems` webhook is implemented; `edpProviderSearch` is re-assigned to 137.
- `AccessPolicy` agrees with the spec, and the changelog has a 024 entry.
- Postman is regenerated with bodies for each format, compat and EDP request.
- `./gradlew lintOpenApi` is valid, with 70 warnings (unchanged).
- The `Edp*` schemas did not change, and re-exporting `docs/protocol/edp-1.0/` produced no diff.

Tests were written, not run (feature prompt).

## Files created / modified

**New: `ai.foresignal.inbound`**
- Custom sources: `CustomSourceEntity`/`Repository`, `CustomSourceDefinition`,
  `CustomSourceInputReader`, `CustomSourceDescriptors`, `CustomSourceVerifiers`,
  `CustomSourceService`, `CustomSourceRegistrar`, `DynamicSourceFactory`.
- Push pipeline: `IngestKeys`, `IngestAuthenticator`, `IngestUrls`, `IngestBodyReader`,
  `IngestRecordLimiter`, `PushIngestService`, `IngestOutcomeView`, `InboundRequests`, `LoopGuard`.
- Controllers and compat: `IngestController`, `CustomSourcesController`, `CompatPublishService`,
  `CompatPublishController`.

**New: `inbound.format`**
- SPI: `IngestFormat`, `IngestRequest`, `ParsedIngest`, `ItemRejection`, `FormatOptions`,
  `FormatOptionSpec`, `IngestFormatRegistry`.
- Formats: `IngestTimes`, `GeoJsonIngestMapper`, `NativeFormat`, `AlertmanagerFormat`,
  `CloudEventsFormat`, `CapFormat`, `RecordMapping`, `RecordMappingEngine`, `MappingFormat`.

**New: `inbound.signature`**
- `SignatureVerifier`, `VerifierConfig`, `Verification`, `SignatureVerifiers`.
- The four verifiers, plus `Hmac`.

**New: `inbound.edp`**
- Protocol and model: `EdpProtocol`, `EdpProperties`, `EdpManifestModel`, `EdpValidItem`,
  `EdpSchemaError`, `EdpItemRejection`, `EdpStats`.
- Validation: `EdpSchemaCompiler`, `CompiledPayloadSchema`, `EdpSchemaException`,
  `Re2RegularExpressionFactory`, `EdpManifestValidator`, `EdpBatchReader`, `EdpBatchChecks`,
  `EdpGeometryValidator`, `EdpPeriods`, `EdpItemValidator`.
- Mapping and processing: `EdpTimes`, `EdpItemMapper`, `EdpDescriptorBuilder`,
  `EdpBatchProcessor`.
- Services: `EdpManifestService`, `EdpIngestService`, `EdpSnapshotService`, `EdpStatusStore`,
  `EdpStatusHealthRule`, `RegistrationTokenService`, `EdpSelfRegistration`.
- Pull and URL security: `EdpPullAdapters`, `EdpPullAdapter`, `EdpUrlGuard`, `EdpHttpClient`,
  `EdpUrlRefusedException`, `EdpFetchException`.
- Entities and repositories: `ExternalSourceEntity`/`Repository`,
  `RegistrationTokenEntity`/`Repository`, `EdpSnapshotEntity`/`Repository`,
  `EdpSnapshotStageEntity`.
- `ExternalSourcesController`.

**New: other packages**
- `source.formats.cap`: `CapParser`, `CapAlert`.
- `security`: `IngestCaller`, `IngestKeyAuthenticator`.
- `source.spi`: `Egress`, `SourceAccess`.

**Changed**
- Source model:
  - `SourceDescriptor`: `custom`, `egress`, `originator`, `access`, `hiddenKinds` and
    `kindPresentation`, with builder methods.
  - `SourceRegistry`: `registerDynamic(descriptor, adapter)`, so dynamic POLLING sources need a
    polling adapter; and `redefineDynamic`.
  - `SourceRegistryChangedEvent.REDEFINED`.
  - `SourceService`: create, refresh, enable and delete for dynamic settings.
- API and security:
  - `SourceMapper` and `SourceDescriptorDto`: `custom`, `egress`, `originator`, `access`.
  - `TokenFilter`: the ingest-key path and its audit.
  - `AccessPolicy`: the 024 rules.
  - `ErrorCodes`: 16 codes.
- Other code:
  - `TileLayerResolver`: hidden kinds stay out of the index.
  - `TileJsonMapper`: `foresignal:presentation`.
  - `BudgetGovernor.secondsUntilAvailable`.
  - `StartupOrder.CUSTOM_SOURCES`.
  - `application.yml`: `foresignal.edp.*`.
- Build and notices:
  - `build.gradle.kts`: json-schema-validator moved to `implementation`, RE2/J added, and
    `processResources` copies `docs/protocol/edp-1.0/*.schema.json` to `edp/schemas/`.
  - `gradle/libs.versions.toml`.
  - `THIRD_PARTY_NOTICES.md`: json-schema-validator and RE2/J.
- Spec, Postman and docs:
  - `docs/OpenAPI.json`: statuses, reconciliations, schemas, error codes, one new operation and
    the changelog.
  - `src/test/resources/openapi/pending-operations.txt`: −31 lines.
  - `scripts/generate-postman.py`: ingest-key auth for every ingest operation, raw text and XML
    bodies, format path variants, EDP variables and captures. Both `postman/` files regenerated.
  - `docs/Operator-Guide.md`: new "Inbound webhooks" and "External data providers" sections.

## Deviations from the prompt

1. **The CAP parser lives at `ai.foresignal.source.formats.cap.CapParser`.** Conventions'
   "Shared parsers" rule wins over the prompt's `ai.foresignal.format.cap`.
2. **Ingest-key scope in the spec.** `pushExternalItems` and `updateExternalSourceManifest` had
   `x-foresignal-scope: agent`. They are now `open` with the ingest-key schemes, like
   `ingestRecords` (prompt §6). `AccessPolicy` gives them `INGEST_KEY_OR_ADMIN`.
3. **Schema names.**
   - The column is `custom_source.private_source`, not `private`, to stay clear of SQL keywords.
   - The token table is `edp_registration_token`.
   - Snapshots use a header table `edp_snapshot` plus the prompt's `edp_snapshot_stage` of staged
     ids.
4. **Provider URLs are fetched with `EdpHttpClient`, not `UpstreamClient`/`BoundSourceHttp`.** That
   client cannot pin an HTTPS connection to a vetted address. `EdpHttpClient` connects to the
   pinned IP:
   - over HTTP, with the original `Host`;
   - over HTTPS, with SNI and its own RFC 6125 certificate-name check against the original host.

   Pull requests are still charged to `BudgetGovernor` and `UsageMeter`. The one-time `manifestUrl`
   fetch of a registration is not budgeted.
5. **`allowPrivateNetwork`.** The spec had no per-source place for it, so it was added to
   `ExternalSourceRegistration` and `ExternalSource`. It is set at registration only, and it is
   also what permits plain `http`. Metadata addresses stay refused even with it.
6. **Pull providers are dynamic POLLING sources with an `EdpPullAdapter`.** They still accept
   pushes, because push acceptance is PUSH mode or `custom=true`.
7. **Per-stream manifest fields are folded into one descriptor:**
   - `recordTtl` is the longest stream TTL;
   - significant and volatile fields are unions;
   - the title field is the first one declared;
   - the time base is UPSTREAM, and streams declaring `observedAtMode: received` get
     `observedAt = receivedAt` from the item mapper, so no source mixes time bases.
8. **EDP record payload.**
   - Item `properties` are top-level, and the type's fields override them.
   - `edp` holds `{stream, mode, itemType, originator, originatorClass, upstreamId, derivedFrom,
     aggregates}`, the prompt's "metadata"; `NormalizedRecord` has no metadata field.
   - `edpTimes` holds `effectiveKnownAt`, `effectivePublishedAt`, `effectiveMadeAt` and
     `receivedAt`. It is volatile, so a repeated item stays a touch.
9. **Breaking manifest changes also include** a changed update style, geometry type, pull
   `itemsUrl` or search URL. A new address returns the provider to pending, for review.
10. **New operation `allowExternalSnapshot`**
    (`POST /v1/external/sources/{id}/snapshots/{snapshotId}/allow`, admin). It is the operator's
    "allow once" for a held deletion (§3.2); the spec had no operation for it.
11. **Webhooks.** `edpProviderItems` is implemented. `edpProviderSearch` is re-assigned to 137,
    which makes the call (the prompt says 137; the spec text said 136). 024 stores
    `transport.search`.
12. **`formats.mapping` uses the 024 JSON-Pointer shape,** published as the new schema
    `IngestRecordMapping`, alongside 059's `RecordMapping`. `ingestRecordsFormat`'s JSON body now
    also admits any JSON document, which the `mapping` format reads.
13. **`CustomSourceInput` members later prompts enforce are refused with 400:** `profile`,
    `allowPathKey`, `ingestAllowCidrs`, `mapping`, `preset`, `presetParams`, `captureSamples`,
    `licenceTerms`. Accepting them unenforced would fail open, as in 023.
14. **Signatures.**
    - An admin token is not asked for a sender signature.
    - A request with no key on a `keyOptional` source passes the filter as `signature-only`, and
      the controller then requires a valid signature.
15. **Compatibility messages.**
    - They take `observedAt = receivedAt` even under UPSTREAM, because the protocols carry no
      observation time.
    - The ntfy reply's `id` is the record's external id; the Gotify reply's `id` is the last seq.
16. **Startup registration runs in `SmartInitializingSingleton`, not on `ApplicationReadyEvent`,**
    so it precedes the web server too. `CustomSourceRegistrar` is also an `IngestStateReset`, so a
    test truncation unregisters stale `custom-*`/`ext-*` sources.
17. **Additions beyond the prompt's text.**
    - The inbound loop guard, from 018's `OriginMarker` contract.
    - A new error code `SOURCE_ID_NOT_ALLOWED` (409), for a self-registering provider outside its
      token's prefix.
    - A batch of status items only records a health attempt, so the provider's reported status
      applies.
18. **IngestControllerIT checks change events through `/v1/changes`, not an SSE client.**

## Issues found / open questions

1. **Doc vs code: the EDP §3 example batch.** Its observation has no `observedAt`, but §3 makes it
   required unless the stream is `observedAtMode: received`. The validator rejects such an item
   `MISSING_OBSERVED_AT`, and the Postman example adds `observedAt`. Which should change: the
   document example, or the rule?
2. **Unverified at runtime.** No test has run. Two library paths are unexercised:
   - the HTTPS pinning path (custom `X509ExtendedTrustManager`);
   - networknt's `schemaLoader(… fetchRemoteResources(false))`.

   The next test-run prompt (041) will exercise them.
3. **The pull cursor is stored after each page,** before the writer commits that run's records. A
   writer failure right after a fetch would skip those items, unless the provider replays them.
   Repeats are idempotent.
4. **Hidden atomic kinds still resolve as a layer by name.** They are hidden from the TileJSON index
   only, as the prompt says ("hidden from default tile layer lists").
5. **Data:** no instructions directed at an agent were found in data, fixtures or tool output.
6. **Owner decisions carried from 012–022** are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `inbound/CustomSourceServiceIT` | Create:<br>• the key once<br>• descriptor fields, STANDARD tier, settings enabled, `custom: true`<br>• a taken id (registered source) → 409; invalid input lists every field<br>Update:<br>• `KIND_IN_USE`, `OBSERVED_AT_MODE_LOCKED`<br>• SAMPLED override plus emptied significant fields → `INGEST_POLICY_INVALID`<br>Other:<br>• volatile edit → recipe migration and 0 versions on the jittered re-push<br>• delete unregisters, rotate, startup re-registration |
| `inbound/IngestControllerIT` | Body and credentials:<br>• native GeoJSON and records, JSON and geo+json<br>• header, Bearer, Basic, admin; wrong or foreign key → 401; rotation<br>Refusals:<br>• non-push 404; disabled 409<br>• 413; 10,001 records → 400; gzip<br>• request and record caps → 429 + `Retry-After` + `REFUSED` row<br>Time base and provenance:<br>• `MISSING_OBSERVED_AT`; RECEIVED stamping<br>• PUSH fetch row with principal, hash, URL<br>• touch on re-push; change feed<br>Triggers and formats:<br>• a trigger fires on the custom kind<br>• hmac and `keyOptional`, write-only secret<br>• Alertmanager firing and resolved, CAP, `FORMAT_UNKNOWN`/reserved/415<br>• CloudEvents handshake, `listIngestFormats`, loop guard |
| `inbound/CompatPublishIT` | ntfy:<br>• POST, PUT, GET `/publish` with `?auth=`, POST `/send`, unknown action<br>• JSON publish and topic binding<br>Gotify:<br>• header, form `?token=`, extras id, reply shape, principal<br>Other:<br>• `auth`/`token` redacted in `fetch_log` and logs<br>• loop guard |
| `inbound/InboundHelpersTest` | Redaction, path and credential parsing, keys, rejection merging, ntfy/kind helpers, callers |
| `inbound/edp/EdpIngestIT` | Registration and approval:<br>• pending → 409 with nothing stored; approve<br>• feature, document, forecast; NDJSON events and deletes<br>Items:<br>• observations with `effectiveKnownAt`<br>• touch on repeat; delete → tombstone<br>• status → health only<br>• per-item rejections with schema errors; `SOURCE_MISMATCH`; `PROTOCOL_UNSUPPORTED`<br>Registration tokens:<br>• single use, prefix 409, listing without the secret, revoke<br>Other:<br>• atomic kinds hidden from `/v1/tiles.json` but queryable; TileJSON presentation<br>• additive vs breaking manifest updates by ingest key<br>• suspend and delete<br>• protocol and schemas open; validate dry run; `MANIFEST_INVALID` |
| `inbound/edp/EdpPullIT` | WireMock provider:<br>• cursor paging with the header credential; stored cursor and resume<br>• budget ends the run early<br>• 503 → health and `pull.lastError`<br>• suspended not pulled |
| `inbound/edp/EdpSnapshotIT` | Clean snapshots:<br>• complete two-page snapshot removes only its scope's absent entity<br>Removes nothing:<br>• missing page, rejected item, unhealthy status, timeout, scope mismatch<br>Mass deletion and pull:<br>• a mass deletion is held (degraded) and released once with `allowExternalSnapshot`<br>• a pull snapshot ends at the null cursor |
| `inbound/edp/EdpManifestServiceTest` | Conformance manifests valid/invalid; descriptor fields; egress from `redistribution`; originator and access; breaking changes; pull URL and auth headers |
| `inbound/edp/EdpValidationTest`, `EdpTimeTest`, `EdpUrlGuardTest`, `EdpSchemaSafetyTest`, `EdpSchemaExportTest` | As the prompt lists |
| `inbound/format/AlertmanagerFormatTest`, `CloudEventsFormatTest`, `GeoJsonIngestMapperTest`, `RecordMappingEngineTest`; `source/formats/cap/CapParserTest`; `inbound/signature/SignatureVerifierTest` | As the prompt lists |

Fixtures: `src/test/resources/edp/conformance/` holds valid and invalid manifests and batches for
every item type, NDJSON, and a README.

Test support:
- `testsupport/inbound/InboundIt`
- `testsupport/inbound/EdpConformance`
- `testsupport/inbound/InboundBodies` (the production parsers, for the Postman contract test)

Changed tests:
- `AccessPolicyTest`: ingest, compat and EDP rules.
- `SchemaBootstrapIT`: five tables.
- `SourceRegistryTest`: redefine and dynamic POLLING.
- `TileLayerResolverTest`: hidden kinds.
- `PostmanCollectionContractTest`:
  - ingest-key auth for every ingest operation;
  - non-JSON bodies skipped;
  - the schema chosen by `Content-Type`;
  - 11 new validators;
  - three operations schema-only.

## Compile result

`./gradlew compileJava compileTestJava --rerun-tasks` → `BUILD SUCCESSFUL in 7s`.

`./gradlew lintOpenApi` → `Woohoo! Your API description is valid.`, 70 warnings, 0 errors.

**New and changed dependencies** (verified on Maven Central via `maven-metadata.xml`):
- `com.google.re2j:re2j` 1.8: the Go license (BSD-3-Clause), no runtime dependencies.
- `com.networknt:json-schema-validator` 3.0.8 (latest), Apache-2.0: moved from test to runtime.
  Its runtime dependencies `com.ethlo.time:itu` and `jackson-dataformat-yaml` are Apache-2.0; joni
  and GraalJS are optional and not pulled in.

## Notes for the next prompt

1. **039 (device keys):** the device ingest-key path is `/v1/ingest/{sourceId}/k/{key}/{format}`.
   `IngestAuthenticator.IngestPath` currently reads segment 4 as the format, so `k` would be taken
   for one; teach it the `k` path. 039 also owns:
   - `profile`, `allowPathKey` and `ingestAllowCidrs` in `CustomSourceInputReader.LATER_MEMBERS`;
   - folding `SourceVisibilityGate` and the new `SourceDescriptor.egress` into `EgressPolicy`.
2. **054:** register `media` and `rasters` as `IngestFormat`s, and remove them from
   `IngestFormatRegistry.RESERVED`.
3. **059:** `inbound.format.RecordMapping` is the 024 JSON-Pointer mapping. The spec's
   `RecordMapping` (JSONPath) is yours, and so are `mapping`, `preset`, `presetParams`,
   `captureSamples` and `mapping-preview`.
4. **109:** `licenceTerms`; the descriptor's `egress` default comes from an EDP manifest's
   `redistribution`.
5. **116:** read `SourceDescriptor.originator` and the record payload's `edp.originator`,
   `edp.upstreamId` and `edp.derivedFrom`.
6. **093, 122:** read `edpTimes.effectiveKnownAt` and `effectiveMadeAt`.
7. **137:** `transport.search` is in the stored manifest (`EdpManifestModel.search()`), and
   `edpProviderSearch` is yours. Call it through `EdpHttpClient` with the source's
   `allowPrivateNetwork`.
8. **138:** build on these:
   - `EdpManifestService`, `EdpBatchProcessor` and `EdpItemMapper`;
   - `SourceDescriptor.hiddenKinds`, which keeps atomic kinds out of the layer index;
   - `ExternalStreamStats.bindings`, reported as 0.

   Nothing emits the SSE `external.source.state` event yet.
9. **Tests:** contexts with `@Import(CollectorTestSources.class)` share these ITs. Source ids
   must be unique per test (`InboundIt.newId`), because the request and record buckets live per
   source id for the context's life.
