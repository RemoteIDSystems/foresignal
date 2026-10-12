# 041-CR-Checkpoint-Platform

| | |
|---|---|
| Prompt | 041-Checkpoint-Platform.md |
| Status | Complete |
| Implementation commit | e985b184b4dc817ef5072ca245155f95efd73cee |
| Date | 2026-10-12 |

## Summary

This was the first run of the tests written in prompts 023–040, together with the 001–022 suite.
The suite is now green: `./gradlew test` passes **4669 tests, 0 failed, 0 skipped**, in two full
runs in a row (run 4, and run 5 with `--rerun`). The last runs took 8 min 5 s and 7 min 58 s.

Getting there took:
- **12 production fixes**, grouped by root cause under Test results;
- **harness fixes**: the test heap, the Spring context cache, the database cleaner, and a test
  producer that leaked;
- **test changes**, each with the document or code it was checked against;
- **5 new tests or guards**:
  - a JVM-wide guard that fails any test which looks up a host outside this machine;
  - openReads-on cases for stream tickets, tile tickets and feed keys;
  - a 2xx-exception list and validator entries for the 026–040 operations;
  - a matcher case for templated path segments.

Run 1 ran out of memory. Run 2 slowed to a crawl because a failed test left an ingest producer
writing into the shared database. Both causes are fixed and explained below.

Things that are now verified for the first time:
- every Natural Earth or GeoNames reference load. Before this checkpoint all of them failed in
  production code (Jackson 3 trailing-token check);
- the PostGIS behaviors earlier reports flagged as unverified;
- the 42710 partition race code, confirmed on PostgreSQL 17.5;
- the logged asOf, track and timeline query plans.

## Files created / modified

Production:
- `reference/GeoJsonFeatures.java`: streaming feature reads disable `FAIL_ON_TRAILING_TOKENS`.
- `ingest/IngestWriter.java`: a writer held for its clock wakes as soon as the hold is released
  (`sleepWhileClockBehind`).
- `llm/client/ToolLoop.java`: a tool cut off at the deadline stops the loop with `DEADLINE`.
- `notify/NotificationMessage.java`: `withText` keeps `shortTitle`.
- `notify/push/PushMessage.java` (`withSilent`) and `notify/push/WebPushTransport.java`: a
  quiet-hours downgrade makes the Web Push payload `silent`.
- `api/ActionsController.java`: the same-origin check uses the server name and port Tomcat
  resolved, not the raw `Host` header.
- `search/EmbeddingEligibility.java`: turning `embed` off on a source's first settings update
  deletes its documents.
- `security/restrict/ReadRestriction.java`: `toStored()` returns null for a restriction that only
  carries request filters.
- `src/main/resources/application.yml`: `logging.level.org.springframework.web.servlet.mvc.method.annotation: info`.

Spec, generated files and build:
- `docs/OpenAPI.json`: `streamEvents.badge` gains `"id": false`. `external.source.state` is gated
  on prompt 138. Two changelog entries were added.
- `scripts/generate-postman.py`: an `ingestRecordsPathKey` example body (an OwnTracks location),
  and `updateBriefing` names only the `earthquake` kind.
- `postman/Foresignal.postman_collection.json`: regenerated.
- `build.gradle.kts`: `test` gets `maxHeapSize = "2g"`.

New test code:
- `test/LocalOnlyResolverProvider.java`, `test/NoExternalNetworkExtension.java`;
- `META-INF/services/java.net.spi.InetAddressResolverProvider` and
  `META-INF/services/org.junit.jupiter.api.extension.Extension`;
- `junit-platform.properties`: extension autodetection;
- new test methods: `StreamTicketIT.ticketsUnderOpenReads` and `PathKeyFilterIT.keyUnderOpenReads`.

Test support:
- `IntegrationTest` (`@AutoConfigureMockMvc`) and `DatabaseCleaner` (retries a deadlocked
  TRUNCATE);
- `ReferenceFixtures`, `SearchTestSources` (offline `HostResolver`), `SearchItSupport` (region
  tagging off) and `IntegrationDocs`;
- `RouteContract` (push 2xx exceptions).

Changed tests (each listed with its reason under Test results):
- API and ingest: `LlmControllersIT`, `PrivateEgressIT`, `TriggersControllerIT`,
  `IngestControllerIT`, `GpsLoggerFormatTest`, `OsmAndFormatTest`
- Contract and guards: `UpstreamClientIsTheOnlyHttpClientTest`, `SpatialIndexInitializerTest`,
  `ComposeFilesTest`, `CookbookApiReferenceTest`, `PostmanCollectionContractTest`,
  `JacksonDurationIT`, `EventSchemaConformanceTest`
- MCP and search: `McpServerIT`, `McpTimeTravelToolsIT`, `McpToolSchemaTest`, `EmbeddingWorkerIT`
- Security: `OutboundUrlPolicyTest`, `AdminTokenFilterIT`, `StreamTicketIT`,
  `ClientRateLimitFilterIT`, `ClientRateLimitFilterTest`, `PathKeyFilterIT`
- Data and stats: `RetentionJobIT`, `StatsServiceIT`, `GlanceServiceTest`, `EntityPurgeIT`,
  `DatabaseCleanerIT`
- Triggers and places: `EnrichmentPromptInjectionIT`, `EnrichmentWorkerIT`, `MovingAnchorIT`,
  `SavedViewsIT`, `WatchlistsIT`

## Deviations from the prompt

1. **What "no socket outside localhost" covers.**
   - The guard replaces the test JVM's `InetAddress` resolver. Loopback names and the machine's
     own name resolve; every other name is refused and fails the test during which it was asked.
   - Address literals never reach a resolver, so a connect to a literal public IP is not caught.
     No test does that: every stand-in is WireMock on localhost.
   - Names reserved as never resolvable (RFC 2606/6761: `.invalid`, `.test`, `.example`,
     `example.com|net|org`) are refused but not reported. The tests use them on purpose, and
     refusing them gives the answer they would get anyway, without a query leaving the machine.
2. **Context-cache fix.** I fixed only the largest cause of the 48 cached contexts:
   - ten shared configurations were used both with and without `@AutoConfigureMockMvc`, so each
     combination was its own context;
   - `@IntegrationTest` now always includes MockMvc, which should leave about 38 keys;
   - the counts come from a static analysis of the annotations. Spring's cache holds 32
     contexts, and run 2's thread dump showed 32 live contexts and 55 created, so the cache was
     evicting and rebuilding;
   - full runs went from over 26 min (run 2, unfinished) to 14.5 min (run 3) and 8 min (runs 4–5).

   The other candidate merges (one-property splits, nested stub configurations) are listed under
   Notes for the owner to decide.
3. **The HttpClient guard was already an allowlist,** but it had the wrong files on it:
   - it sanctioned `ChannelSender` and `LlmHttp`, which stopped building clients in 035;
   - it was missing `inbound/edp/EdpHttpClient`.

   It is now an exact map of file to justification: `net/PinnedHttp`, `inbound/edp/EdpHttpClient`,
   `notify/push/PushHttp` and `notify/push/NativePushHttp`. It fails for any other file, and for a
   listed file that no longer builds a client.
4. **The search ITs switch region tagging off for their own sources.** Whether a region suffix
   is in the embedding text depends on timing (Issue 3), so the search assertions were not
   deterministic.

## Issues found / open questions

**Needs the owner:**

1. **HTTP clients: shared client vs allowlist** (carried from 019 and 022). Four files build a JDK
   `HttpClient` outside `collect`. The guard now allowlists them exactly. **Owner:** keep the
   allowlist, or route these senders through one shared, policy-pinned client.
2. **Region worker throughput.** Under the `RetentionJobIT` burst, `ChangeBus` logged
   "Change subscriber region-tagger is lagging; dropping batches", and the worker's cursor could
   not catch up while the producer ran.
   - Every eager record costs one or more PostGIS lookups.
   - The worker is in the retention clamp, so a lagging worker holds retention back until its
     2 × window release.
   - **Owner / 057:** measure the tagger at NFR-2 rates in the performance smoke.
3. **Embedding text depends on which worker runs first (040).**
   - The region suffix ("Region: Niger.") is added only when the region worker tagged the record
     before the embedding worker read it. The two consume the change bus independently.
   - A record embedded first stays without the suffix until its text changes.
   - **Owner:** accept this, re-embed on tag, or tag synchronously before embedding.
4. **Doc vs code: watchlist item search.**
   - OpenAPI `listWatchlistItems.q` says "value or label prefix".
   - The code matches a case-insensitive substring of value or label.
   - The test was corrected to the spec's members (it searched notes). **Which should change?**
5. **DEBUG never logs request or response bodies now.** Spring MVC's body processors log every
   body at DEBUG, including minted tokens, tickets, feed keys and credentials (found by
   `StreamTicketIT`). Their package is pinned at INFO, so an operator who sets root DEBUG no
   longer sees bodies. **Owner:** confirm.
6. **After-commit listeners that write through JDBC** (see the audit below). They do not lose
   writes today, but they rely on Hibernate restoring auto-commit before Spring runs after-commit
   callbacks. I did not change them, because the brief limits fixes to lost writes. **Owner:**
   should they move to `REQUIRES_NEW` like 027's?
7. **Owner decisions already listed in earlier reports are unchanged.** The tests exposed none of
   them, apart from noting:
   - briefing deliveries to channels are still not redacted (039 Issue 2);
   - the SSE memory budget (022 item 1) was not re-measured; `perfTest` is not part of this
     prompt.

**Other findings:**
- **Single-place rule errors are reported on the plural field.** An unknown `near.placeId` is
  reported as a field error on `near.placeIds`. The behavior was kept; the test now asserts the
  real field.
- **TRUNCATE can deadlock with the region worker.** The worker's reads and the cleaner's TRUNCATE
  can deadlock in tests. The worker logs "paused" and retries on its 5 s tick, and the cleaner now
  retries. Production has no TRUNCATE.
- **Data:** the only instructions directed at an agent were the deliberate injection fixtures of
  `EnrichmentPromptInjectionIT`, `PromptInjectionIT` and `AskRestrictionIT`. They were treated as
  data, and nothing else was found in data, fixtures or tool output.

## Test results

All runs used Docker with the Testcontainers image `foresignal-db:17-3.5-pgvector`, built from
`docker/db`, and `./gradlew test --continue` unless noted otherwise.

| Run | Tests | Failed | Skipped | Notes |
|---|---|---|---|---|
| 1 | 252 (then stopped) | 14 | 1 | Test JVM `OutOfMemoryError` (512 MB default heap); executor exit 137 |
| targeted | 282 | 4 | 0 | guard and contract unit tests after the first fixes |
| targeted | 247 / 6 / 6 | 1 / 1 / 0 | 0 | Postman validator iterations |
| 2 | 3808 (then stopped) | 111 | 1 | Degraded after `RetentionJobIT`'s burst producer leaked; 32 live contexts and 55 created at the thread dump; the test JVM was stopped |
| targeted | 403 | 15 | 0 | 44 classes after the fixes for run 2 |
| targeted | 161 | 0 | 0 | |
| 3 | 4669 | 15 | 0 | 14 min 33 s |
| targeted | 94 | 2 | 0 | then `SavedViewsIT` 6/0 |
| 4 | 4669 | 0 | 0 | 8 min 5 s |
| 5 | 4669 | 0 | 0 | `--rerun`, 7 min 58 s |

### Production fixes, by root cause

**A. Every reference load failed** (`ReferenceDataLoaderIT` 4, `RegionTaggerIT` 6,
`RegionTagWorkerIT` 5, `RegionFiltersIT` 3, `ReverseGeocodeControllerIT`,
`McpReverseGeocodeToolIT`, `RegionHashInvarianceIT`).
- *Cause:* Jackson 3 turns on `DeserializationFeature.FAIL_ON_TRAILING_TOKENS` by default. In
  `GeoJsonFeatures`, `readTree(parser)` inside the `features` array therefore failed with
  "Trailing token found after value" on the second feature.
- *Change:* the streaming mapper disables the feature. I checked that the constant exists in
  jackson-databind 3.1.5 with `javap`.

**B. Push and notification content** (`WebPushDeliveryIT` 2).
- `NotificationMessage.withText` (018) dropped `shortTitle`. Every `alert.fired` passes through
  the channel templates, so push showed "[critical] Quakes" instead of the rule name. It now
  keeps `shortTitle`.
- A quiet-hours downgrade set `Urgency: very-low`, but the payload stayed `silent: false`.
  `PushOptions.silent` now reaches the payload (`PushMessage.withSilent`).

**C. The ingest writer stayed held after its clock hold was released** (`AgentWorkflowIT`, 15 s
HTTP timeout in the full run).
- *Cause:* a context that started against another context's rows went `CLOCK_BEHIND`. The
  cleaner's `resetState()` released it, but the writer thread was inside a 30 s recheck sleep and
  kept ingest held.
- *Change:* that sleep ends as soon as the state leaves `CLOCK_BEHIND`.

**D. The ToolLoop deadline** (`ToolLoopTest`).
- *Cause:* the timed wait is rounded to milliseconds and could return just before the deadline.
  The loop then made one more model call after a tool had been cut off at the deadline.
- *Change:* a tool timeout stops the loop with `DEADLINE`.

**E. The action Origin check** (`ActionEndpointIT`).
- *Cause:* the check compared against the raw `Host` header, which is absent in MockMvc and is the
  proxy's own address behind a forwarding proxy.
- *Change:* it now uses `getServerName()`/`getServerPort()`, "as Tomcat resolved them", which is
  what the method's documentation and 033's report specify.

**F. Turning `embed` off on a first update left documents searchable** (`EmbeddingWorkerIT`).
- *Cause:* `SourceSettingsChangedEvent.before` is null when the update creates the settings row,
  and the listener required `before.embed()`.
- *Change:* deletion happens when `after.embed()` is false and `before` is null or embedded.

**G. A filter-only restriction stored an empty `TokenRestrictions`** (`ReadRestrictionFilterTest`).
`toStored()` used `hasDataRestrictions()`, which counts request filters that are never stored.

**H. Secrets in DEBUG logs** (`StreamTicketIT`). See Issue 5.

**I. The spec's SSE annotations** (`OpenApiStructureTest`, `EventSchemaConformanceTest`).
- `badge` did not state its `id` line; InboxTopic sends it without one.
- `external.source.state` was gated on 024, which emits nothing on it. Prompt 138's text owns the
  `external` topic.

### Harness fixes

- **Heap.** The test JVM gets 2 GB, as `perfTest` already does. Each cached context holds 040's
  bundled country polygons (1.24 M JTS coordinates across 13 live contexts in run 1's histogram).
- **Context cache.** `@AutoConfigureMockMvc` is on `@IntegrationTest` (Deviation 2).
- **Leaked producer.** `RetentionJobIT.retentionDuringIngestBurst` now stops its producer in a
  `finally`, waits for the consumers before the burst, and runs the job during it. Before, the
  consumers could never catch up with an unbounded burst, `runAt` timed out, and the producer ran
  for the rest of the suite.
- **Cleaner deadlock.** `DatabaseCleaner` retries a TRUNCATE chosen as a deadlock victim (40P01),
  up to 5 times.
- **Offline DNS in search.** `SearchTestSources` resolves offline: the default `ollama` base URL
  was looked up in real DNS.
- **Event publisher.** `ReferenceFixtures` passed `getBean(ApplicationEventPublisher)`. That is not
  a bean, so the context itself is now passed.

### Test changes and why

Each test below asserted something that the spec, a Core-Data-Model section or the code's own
documentation contradicts.

**LLM providers and ingest:**
- **`LlmControllersIT`** (8 failures).
  - Old: `embeddingDimensions: 3` (and `modelId` `…:3`).
  - New: 8, with 8-wide stub vectors.
  - Why: OpenAPI `LlmProviderInput.embeddingDimensions` has minimum 8.
- **`EnrichmentWorkerIT`.**
  - Old: `tokensPerDay: 1`.
  - New: 1000.
  - Why: OpenAPI minimum 1000. `AskServiceIT` uses the same value to provoke the budget refusal.
- **`PrivateEgressIT`** (5).
  - Old: `asOf` with no filter.
  - New: `kind=<point>,<secret>`.
  - Why: Core-Data-Model §12 step 1 makes a source or kind filter required (`ASOF_FILTER_REQUIRED`).
- **`IngestControllerIT`.**
  - Old: `observed_at` read from `record_head`.
  - New: read from `record_live`.
  - Why: Core §8; the head has `persisted_observed_at`.
- **`GpsLoggerFormatTest`, `OsmAndFormatTest`** (4).
  - Old: epoch `1760255970`.
  - New: `1791791970`.
  - Why: the expected instant is 2026-10-12T07:59:30Z. The old literal is 2025-10-12.

**Triggers, places and watchlists:**
- **`TriggersControllerIT`.**
  - Old: `PLACES_UNAVAILABLE`.
  - New: `VALIDATION_FAILED` on `near.placeIds`.
  - Why: places exist since 036, so an unknown place is a field error.
- **`MovingAnchorIT`** (4) and **`SavedViewsIT`** (3).
  - Old: points at (0,0).
  - New: points shifted 0.01°.
  - Why: (0,0) is Null Island, whose geometry is dropped unless `allowNullIsland` (008).
  - `SavedViewsIT`'s view also projects `externalId`, because the projection is literal
    (OpenAPI `fields`).
- **`WatchlistsIT`** (2).
  - `q=medevac` → `q=N456`: OpenAPI says `q` is a value or label prefix (Issue 4).
  - A pre-encoded `%20` in the query → `.param(...)`: MockMvc encodes the template again.

**MCP:**
- **`McpServerIT`.**
  - A payload change is `SIGNIFICANT`, not `CHANGE`: Core §5 row 6 precedes row 7, and
    `IngestWriterIT` asserts the same.
  - An agent token sees every tool except the admin ones (039's `purge_entity` and
    `export_entity`).
- **`McpToolSchemaTest`.** An agent or admin tool that declares itself read-only (039's
  `export_entity`) may carry `readOnlyHint`. Scope is who may call a tool, not whether it writes.
- **`McpTimeTravelToolsIT`** (3). `structuredContent` exists only for tools with an
  `outputSchema` (025 `McpToolRegistry.toResult`), so the test reads the JSON text block.

**Security:**
- **`AdminTokenFilterIT`** (21, context load).
  - The `POST /v1/ingest/{sourceId}` stub collided with 024's controller.
  - The stub was removed, and the admin case expects the controller's 404 for an unknown source.
- **`OutboundUrlPolicyTest`.**
  - `bad.test` resolved to 8.8.8.8, which is outside the deny CIDR `8.8.8.0/29`.
  - It now resolves to 8.8.8.3.
- **`ClientRateLimitFilterIT`.** Search is always authenticated (SEC-5), so it is now called with
  a read token.
- **`ClientRateLimitFilterTest`.** The mapper registers Spring's `ProblemDetailJacksonMixin`, as
  the application mapper does.

**Retention and purge:**
- **`EntityPurgeIT`.**
  - A marker minute is ingested after the purge.
  - Why: Core §8's `cutoffSeq` floors on the latest minute at or before the cutoff (022
    deviation 6).
- **`DatabaseCleanerIT`** (2). The region worker re-seeds `region.cursor` as the trigger engine
  re-seeds `trigger.cursor`.

**Stats and search:**
- **`StatsServiceIT`.** The `cellRes` reason is checked in the field error, not the generic
  message.
- **`GlanceServiceTest`** (4). Mocks are now stubbed outside `thenReturn`'s argument list (Mockito
  `UnfinishedStubbingException`).
- **`EnrichmentPromptInjectionIT`.**
  - The check is now on citation ids.
  - Why: the kept real citation's title is the injected record's own text, which quotes
    "[evil:payload@1]".
- **`EmbeddingWorkerIT`, `SearchServiceIT`, `ReindexIT`** (a region suffix in the text, cosine
  0.99926, 2 extra embeds). All three pass since region tagging is off for the search sources
  (Deviation 4). The suffix explains the first two; I did not trace the 2 extra embeds of
  `ReindexIT` separately.

**Contract tests and stale pins:**
- **`SpatialIndexInitializerTest`.** The pin now includes 036's and 040's statements.
- **`ComposeFilesTest`** (2). It allows 026's optional `ollama` service under its `local-llm`
  profile.
- **`CookbookApiReferenceTest`/`IntegrationDocs`.** Templated segments such as `{y}.mvt` now
  match, with new cases.
- **`JacksonDurationIT`.** The exact JSON gains `audit`, `inbox` and `stats` (023, 033, 038).
- **`EventSchemaConformanceTest`.** A `badge` payload factory was added.
- **`PostmanCollectionContractTest`** (3).
  - New validator entries: `createLlmProvider`, `updateLlmProvider`, `ask`, `askStream`,
    `createBriefing`, `updateBriefing` and `previewBriefing`. Each uses the production parser.
  - Schema-only with reasons: `updateLlmSettings`, `createPushSubscription`,
    `verifyPushSubscription` and `ingestRecordsPathKey`.
  - Disabled Postman headers are ignored.
  - `ingestKeyPath` (key in the path) needs no ingest-key header.
- **`OpenApiContractIT`/`RouteContract`.** Exceptions were added for the six push operations whose
  status `ResponseEntity` chooses at run time. Their ITs assert 201/204/200.

### Known items from earlier reports

- **HttpClient guard.** It is an exact allowlist with justifications (Deviation 3, Issue 1).
- **Postman validator table.** All 026–040 operations are covered (above).
- **`JacksonDurationIT`.** Updated to the current `RetentionSettings` shape.
- **`TimeTravelPlansIT` (037)** passes all 4 tests, with sequential scans off and tiny tables.
  - Track: `Index Scan using rv_t_0_source_id_external_id_seq_idx` and
    `rv_t_20000000_source_id_external_id_seq_idx` (Index Cond on source and external id; filter
    on `observed_at` and tier).
  - Timeline: `Index Only Scan using rv_s_0/rv_s_250000000/rv_t_0/rv_t_20000000_source_id_observed_at_idx`
    under a `HashAggregate` on `date_bin`.
  - asOf tile: `cand` is an `Index Scan using rv_t_0_geom_idx` (GiST) under a `HashAggregate`.
    The pick is an `Index Scan Backward using rv_t_0_source_id_external_id_seq_idx` with
    `Limit 1`.
- **After-commit writers (027 Issue 3).** I audited every callback registered in `afterCommit`:
  `SourceService`, `TokenService`, `FeedKeyService`, `AppSettingService`, and llm `AfterCommit`.
  - These events are published there: `SourceSettingsChangedEvent`,
    `SourceCredentialsChangedEvent`, `ApiTokenRevokedEvent`, `AppSettingChangedEvent` and
    `EmbeddingModelChangedEvent`.
  - Listeners that write:
    - 027's three (`EmbeddingEligibility` ×2, `ReindexService`, `EmbeddingWorker`) already use
      `REQUIRES_NEW`;
    - `PushSubscriptionService.onTokenRevoked` and `PushDeviceService.onTokenRevoked` use plain
      `JdbcTemplate` deletes.
  - The JDBC deletes persist: `PushSubscriptionsIT.revokeCascade` and `PushDevicesIT` revoke
    cascades pass.
  - Every other listener only touches memory (caches, buckets, schedules, SSE clients, tickets).
  - `AlertStore.write` runs its after-commit actions after the transaction has ended.
  - No write is lost, so none was changed. Issue 6 covers the fragility.
- **PostGIS behaviors**, all verified by passing tests:
  - empty tiles: `TileQueryServiceIT` 7/7;
  - negative geography buffers: `GeofenceHysteresisIT`, `PlacesIT`;
  - geography KNN `<->` and `ST_Azimuth` on geography: `RegionTaggerIT` "KNN near: 12 km NNE of
    Austin, TX, with the bearing from the place";
  - `ST_MakeValid`/`ST_Union` in the swap: `ReferenceDataLoaderIT` 8/8 (staged swap, -99
    skipped, `geom_simple`).
- **Partition race SQLState 42710 (009)**, confirmed on the test image (PostgreSQL 17.5) in a
  rolled-back transaction. After `CREATE DOMAIN checkpoint041_probe`,
  `CREATE TABLE checkpoint041_probe` fails with `ERROR: 42710: type "checkpoint041_probe" already exists`
  (`heap_create_with_catalog`, heap.c:1176). A colliding composite type gives `42P07` instead.
  Both are handled.

### Scrutiny (focus areas)

All of these pass in runs 4 and 5.

- **Tokens and scopes:**
  - `AccessPolicySyncTest` covers every implemented operation; planned routes as in 023
    deviation 2.
  - `TokenFilterIT` 52: the read, agent, admin, restricted, expired and revoked matrix, openReads
    on and off.
  - Tickets and feed keys with openReads off: `StreamTicketIT`, `PathKeyFilterIT`; on: the new
    tests.
  - `AlwaysAuthenticatedIT`: every `alwaysAuthenticated()` rule is 401 anonymously under openReads.
  - Unmatched `/v1/**`: `AccessPolicyTest`, `AdminTokenFilterIT`.
  - Revocation cascades to tickets, push subscriptions and devices.
  - `AuditIT` 8 and the MCP registry audit.
- **Ingest formats:** `IngestControllerIT`, `CapParserTest` (DTD and external entities refused),
  `CompatPublishIT`, `PersonalLocationIngestIT` 9, the format unit tests, `InboundHelpersTest`.
- **MCP:** `McpServerIT` 20, `McpTimeTravelToolsIT`, `EntityToolsTest`, `AgentWorkflowIT`
  (admin tools absent for agents, not in Ask's catalog).
- **LLM:**
  - WireMock and fake providers only.
  - Budget refusal: `AskServiceIT`, `EnrichmentWorkerIT`.
  - Refusal before content: `ToolLoopTest`.
  - No test reached the network; the resolver guard reported nothing.
- **Embeddings:** `ReindexIT` 9 (dual write, resume, atomic swap), `EmbeddingWorkerIT` 11,
  `ConsumerClampIT`.
- **Ask:** `PromptInjectionIT`, `AskActionsIT` 9, `CitationVerifierTest`.
- **Briefings:** `BriefingSchedulerIT` 6.
  - DST: "07:00 Chicago moves from 12:00 to 13:00 UTC" holds.
  - Also SKIP LOCKED with two schedulers, missed runs, no overlap.
- **Push:** `WebPushCryptoTest` (RFC vectors), `NativePushDeliveryIT` 12, `PushSubscriptionsIT` 13,
  `PushDevicesIT`.
- **Actions:** `ActionEndpointIT` 7.
- **Perimeter:** `OutboundUrlPolicyTest` 239, `OutboundRetrofitIT`, `ClientRateLimitFilterIT` 13.
- **Places:** `PlacesIT`, `MovingAnchorIT` 5, `WatchlistRuleIT`, `SavedViewsIT` 6.
- **asOf:** `HistoryFramesIT`, `AsOfQueryIT`, `TimeTravelPlansIT`.
- **Stats:** `RollupJobIT` 9, `GlanceControllerIT`, `MetricsEndpointIT`.
- **Private sources:** `PrivateEgressIT` 35, `SseHubPrivateIT`, `EntityPurgeIT` 9.
- **Gazetteer:** `RegionHashInvarianceIT`, `GeofenceRefIT`, `CountryCentroidsTest`.
- **Cross-cutting:**
  - I1 holds: `EntityPurgeService` purges through `IngestQueue.submitEntityPurge`, and
    `record_region` and `activity_rollup` are written only by their own workers and `PurgeFanout`.
  - `DatabaseCleaner` discovers tables from `information_schema`, so it covers every new table.

### Invariants check (owner ≤ 040)

Counts are tests/failures in run 4.

| Invariant | Tests |
|---|---|
| ING-1, ING-2, ING-5 | `IngestWriterIT` 24/0 |
| ING-3 | `IngestClockGuardTest` 3/0, `IngestWriterIT` |
| ING-4 | `TimeContractTest` 2/0 |
| ING-6 | `NoJpaWritesToRecordVersionTest` 3/0 |
| ING-7 | `ContentHasherTest` 24/0 |
| ING-8 | `IngestPolicyEngineTest` 67/0, `IngestPolicyValidatorTest` 24/0 |
| HIST-1 | `PartitionManagerIT` 20/0 |
| HIST-2 | `CurrentReadsUseHeadLiveIT` 3/0 |
| HIST-3 | `AsOfQueryIT` 11/0 (014's wording question is still open) |
| RET-1 | `RetentionJobIT` 11/0 |
| RET-2 | `ConsumerClampIT` 2/0 |
| RET-3 | `RetentionResolverTest` 14/0, `FamilyDefaultsTest` 2/0 |
| RET-4 | `PurgeSeqIT` 2/0 |
| SRC-1 | `UpstreamClientIsTheOnlyHttpClientTest` 2/0 (exact allowlist), `UpstreamClientIT` 49/0 |
| SRC-2 | `BudgetGovernorTest` 21/0, `WebSocketBudgetIT` 11/0 |
| SRC-3 | `BackoffRegistryTest` 32/0, `BackoffRegistryIT` 11/0 |
| SRC-4 | `UsageMeterIT` 31/0 |
| SRC-5 | `UrlRedactorTest` 17/0, `RequestRecorderTest` 6/0, `FetchTraceTest` 7/0 |
| SRC-6 | `CollectorSchedulerIT` 9/0, `NoClientTriggeredFetchIT` 1/0 |
| SRC-7 | `ParamValidatorTest` 70/0, `PinnedTargetTest` 4/0, `LocalAddressesTest` 26/0 |
| SEC-1 | `AccessPolicyTest` 207/0 |
| SEC-2 | `AdminTokenServiceIT` 14/0, `TokenHasherTest` 11/0 |
| SEC-3 | `CredentialCipherTest` 15/0, `SecretKeyStoreTest` 9/0 |
| SEC-4 | `ScopeTest` 25, `TokenFilterIT` 52, `AccessPolicySyncTest` 3, `RestrictionLeakMatrixIT` 28, `AskRestrictionIT` 1, `RestrictedReadCoverageTest` 2, `RestrictionClampTest` 11, `PathKeyFilterIT` 7, `McpServerIT` 20, `McpToolRegistryTest` 13, `AgentWorkflowIT` 3, `PlacesIT` 5, `McpPlaceToolsIT` 4, `WatchlistRuleIT` 4, `SavedViewsIT` 6; all 0 failed |
| SEC-5 | `AlwaysAuthenticatedIT` 3, `McpServerIT`, `AskServiceIT` 14, `BriefingsControllerIT` 8; all 0 failed |
| EGR-1 | `PrivateEgressIT` 35, `EgressPolicyTest` 10, `EmbeddingWorkerIT` 11, `AskServiceIT`, `LlmBriefingIT` 7, `EnrichmentContextIT` 8; all 0 failed |
| EGR-3 | `AskActionsIT` 9, `PromptInjectionIT` 1, `EnrichmentPromptInjectionIT` 1; all 0 failed |
| SSE-1 | `SseReplayLiveBoundaryIT` 1/0 |
| TRG-1 | `TriggerPersistGateTest` 159, `RuleSqlBuilderIT` 72, `RecordPredicateTruthTableTest` 167, `PlacesIT`; all 0 failed |
| TRG-2 | `GeofenceHysteresisIT` 1/0 |
| EXT-1 | `EdpIngestIT` 8/0, `EdpManifestServiceTest` 13/0 |
| EXT-4 | `EdpTimeTest` 6/0 |
| EXT-5 | `EdpSnapshotIT` 8/0 |
| EXT-6 | `EdpUrlGuardTest` 25/0, `EdpSchemaSafetyTest` 9/0 |
| PRC-1 | `OpenApiContractIT` 6/0, `PostmanCollectionContractTest` 6/0 |
| PRC-2 | `LicenseAllowlistTest` 3/0 |

Every named test class exists, and none had to be added.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. No new dependencies.

## Notes for the next prompt

1. **The test JVM has 2 GB.** Count new Spring test configurations before adding one: the cache
   holds 32, and `@IntegrationTest` now always includes MockMvc. Remaining candidate merges, which
   need the owner's call:
   - the one-property splits: `ActionBackoffIT`, `RateLimitSettingsIT`, `McpLegacySseIT`, and
     the three rate-limit or proxy variants of the stream key;
   - the nested stub configurations: `TokenServiceIT`, `AppSettingServiceIT`, `CorsFilterIT`,
     `AdminTokenFilterIT`;
   - the imports one layer too low: `HealthServiceIT`, `IngestWriterIT`, `GrafanaMetricNamesIT`.
2. **No test may resolve a host outside this machine.**
   - Use the application's `HostResolver` stand-ins (`TestOutbound.offlineResolver()`,
     `MutableResolver`), or names under `.test`, `.invalid` or `.example`.
   - The guard fails the test otherwise.
3. **Test points must not sit at (0,0).** Null Island geometry is dropped unless the source
   allows it.
4. **Background workers that join the retention clamp must keep up with bursts in ITs.**
   `RetentionItSupport.awaitTriggerCursor` waits for the trigger and region cursors.
5. **057:** measure the region tagger at NFR-2 (Issue 2) and re-measure the SSE memory budget.
6. **Owner decisions pending:** Issues 1–6 above, plus those carried from 012–040.
