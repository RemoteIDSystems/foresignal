# 039-CR-Private-Sources-And-Personal-Location

| | |
|---|---|
| Prompt | 039-Private-Sources-And-Personal-Location.md |
| Status | Complete |
| Implementation commit | 25e42a9a88e4c750e267709bdc500eecfea99184 |
| Date | 2026-10-12 |

## Summary

Family phones and personal trackers can now be fed into Foresignal safely. All six tasks landed. The
ten 039 operations are `implemented` and off `pending-operations.txt`.

**1. Visibility model** (`ai.foresignal.security.visibility`)
- `SourceVisibility` decides which sources are private: the descriptor flag (008; a custom
  source's `private` and an EDP manifest's flag become that flag) or the operator's tightening
  (`SourceSettings.private`, stored in `app_setting['visibility']`).
  - The operator can tighten any source but cannot make a descriptor-private source public
    (`private: false` is a 400).
  - The set is cached and refreshed on registry and setting events.
  - A source that becomes private raises `SourceBecamePrivateEvent`.
- Who can see private sources (`canSeePrivate`):
  - an admin token, always;
  - a read or agent token, a feed key, a pairing code or a ticket only when it carries the private
    grant `privateAccess` (`api_token.private_access`, `pairing_code.private_access`);
  - an anonymous caller, never.
  - **An agent token without the grant no longer sees private sources.**
  - A token's own source restriction still applies.
- `PrivateFilter` (`sql(alias)`, `allows(id)`, `hidden()`) is the per-caller exclusion. `Viewer`
  carries scope, grant and restrictions.
- `privateAccess` reaches every principal type: `AuthenticatedToken`, `CurrentToken`,
  `RequestPrincipal`, `ReadCaller`, `StreamPrincipal`, `TokenSpec`.
- The 023 `SourceVisibilityGate` is deleted and folded into `SourceVisibility` and `EgressPolicy`.
  Its test is replaced by `SourceVisibilityTest`.
- A direct GET of a hidden record is `404` everywhere.

**2. Enforcement** (`EgressPolicy`, extended in place, plus `EgressPath`)
- **Records, history, frames, timeline, changes, asOf, count, kinds, sources, stats, glance,
  metrics, alerts, inbox, MCP:** all of these already passed through
  `ReadCallerResolver.excludedSources`, which now uses the new rule. 038's exclusions are kept.
  `StatsSettingsService`, `MetricSettingsService`, `TrackService` and `AsOfResolver` use effective
  privacy.
- **Tiles:**
  - Kind layers exclude private sources, a private source layer is `404`, and TileJSON and the
    index omit them for callers without access.
  - A tile whose layer has a private source, and a TileJSON or index seen by a caller who can see
    private sources, is `Cache-Control: private`.
- **SSE:**
  - The hub filters per client using the principal's grant.
  - Revoking a token, a ticket's token or a feed key (`FeedKeyService.revoke` now publishes
    `ApiTokenRevokedEvent`) sends `reset` with reason `revoked`, then closes the stream
    (`SseClient.revoke`, `SseHub.revokePrincipal`).
- **Search and embeddings:**
  - `allowsEmbedding` is false for private sources, and `embed` reads false for them.
  - `embed: true` on a private source is a 400.
  - When a source becomes private, `EmbeddingEligibility` deletes every `embedding_document` row of
    that source.
- **LLM egress:**
  - A private record reaches a provider only when the provider is local *and*
    `llm.allowPrivateLocal` is true; that setting is now accepted and defaults to false.
  - Local means Ollama or `options.local`, with a base URL that passes 035's `OutboundUrlPolicy`
    `LAN_ONLY` profile. An unresolvable host counts as remote.
  - This covers Ask (`llmExcludedSources`, `ToolOutputFilter`), briefings and enrichment.
    Enrichment of a private-source alert is skipped unless both conditions hold.
- **Notifications:**
  - `DeliveryWorker.privacy` reduces a private-source message to rule name, severity and link
    (`PrivatePayloads`) unless the channel has `config.privatePayload: true`; the parser checks
    that value is a boolean.
  - Fan-out push types (Web Push, APNs, FCM) are always reduced.
- **Briefings:** a run whose citations or text name a private source is `404`, or left out of the
  list, for callers who cannot see private sources (Deviation 6).
- **Later hook:** `EgressPolicy.allows(EgressPath, sourceId, Viewer)` with EXPORT, SINK,
  FEDERATION, SHARE and FEED.
  - A private source may leave only for a viewer with access, and never on FEDERATION.
  - Architecture §15.4 says prompts 058, 110, 111, 112 and 113 must call it.
- **Audit:**
  - `PrivateReadAuditor` writes one `audit_event` (`targetType: private_read`) per request when a
    non-admin principal that holds the grant reads data.
  - Exports and purges are audited (`entity_export`, `entity_purge`).
  - Device-key writes are audited with actor kind `ingest_device`.

**3. Personal-location formats** (`ai.foresignal.inbound.formats.location`)
- `owntracks`:
  - Replies `200 []`. Accepts a single object or an array.
  - Location fields map per the prompt. `transition` becomes kind `tracker-transition` under
    externalId `<id>:transition`.
  - `waypoint`, `lwt`, `card` and `status` are ignored.
  - `encrypted` is dropped and counted (`ENCRYPTED_UNSUPPORTED`). A HealthRule shows "OwnTracks
    payload encryption is not supported; disable it" for 24 h after the last such payload.
- `osmand`:
  - GET or POST, with query, form or Traccar Client JSON. Replies empty `200`.
  - Timestamps: epoch s, epoch ms, ISO, or `yyyy-MM-dd HH:mm:ss` UTC.
  - Query speed is in knots; JSON speed is m/s.
- `overland`: at most 1,000 points (more is a 400). Always replies `{"result":"ok"}`. `wifi` is
  kept only with the `keepWifi` option.
- `gpslogger`: GET or POST, form or JSON. Parameter names are case-insensitive. Replies `200`.
- Shared rules:
  - `maxAccuracyMeters` (format option, default 200) rejects worse fixes with `LOW_ACCURACY`.
  - `INVALID_FIX` covers `valid=false` and missing or out-of-range coordinates; `NULL_ISLAND`
    covers (0,0).
  - `IngestFormat` gained `nativeReply(ParsedIngest)` and `pathKeyFriendly()`.
- **Key redaction:** `IngestKeyRedactionFilter` replaces `/k/{key}/` with `/k/redacted/` before
  TokenFilter, problem documents, the fetch log and the access log see the path. `key=` values are
  redacted alongside `auth=` and `token=`.
- **Profile `personal-location`:**
  - private (cannot be unset), STANDARD tier, kinds `tracker` and `tracker-transition`;
  - `UPSTREAM` observed time with the `OBSERVED` TTL anchor, 24 h TTL, 7 d retention;
  - SAMPLED policy: 30 s, 10 min, 50 m, `[motion, charging]`;
  - no `embedFields`;
  - the field specs listed in the prompt.
- `allowPathKey` and `ingestAllowCidrs` are implemented.
  - A path key, a query key or a GET push from outside the CIDR list (default: private ranges) is
    `403 INGEST_SOURCE_DENIED`.
  - An `eik_` key in the path is accepted only with `allowPathKey`.

**4. Device keys** (`inbound/device`; table `ingest_device`)
- An `eid_` key is accepted as a header, Bearer, Basic password or path key, for its own source
  only.
- It forces the bound externalId. An explicit payload id that differs (osmand
  `id`/`deviceid`/`device_id`, overland `device_id`, gpslogger `dev`), or any record of another
  entity after parsing, is `403 DEVICE_ENTITY_MISMATCH` and nothing is written. The OwnTracks
  `<bound>:transition` entity is allowed.
- A revoked key is `401` immediately. `last_seen_at` is written at most once a minute.
- Endpoints: list, create (key returned once plus setup data), delete (revoke), rotate, and purge
  (revoke, then purge).
- The setup bundle is built on `publicBaseUrl`:
  - an OwnTracks `.otrc` with `_type configuration`, `mode` 3, `url`, `auth`, `username`,
    `password`, `deviceId` and `tid`;
  - Overland endpoint and token;
  - the Traccar server URL with the key path;
  - the GPSLogger URL template, method GET.

**5. Export and purge** (`store/purge`, `api/EntityPurgeController`)
- `EntityPurge(sourceId, externalId, future)` is a new sealed writer command. It runs in one
  writer transaction:
  1. Lock the head and allocate one seq.
  2. Write a `fetch_log` row (MANUAL, principal `purge`).
  3. Write a tombstone with `persist_reason` TOMBSTONE and qa flag `PURGED` (32). Its
     `observed_at` is never earlier than the entity's last.
  4. Run `DELETE … seq < tombstoneSeq` across both tiers.
  5. Set the head and live rows to the tombstone state.

  The change is published with `purged: true` (`ChangeSummary.purged()`, `RecordMapper`) on SSE
  and `/v1/changes`. A later HeadPurge removes the head (I7). I1 and I9 hold.
- `PurgeFanout`, each step in its own transaction:
  - deletes embeddings;
  - redacts alerts (snapshot, message, summary and context become `{"redacted":true}`), or deletes
    them and their escalation rows with `purgeAlerts`;
  - redacts stored notification bodies;
  - drops `record_region` rows (a no-op until 040);
  - drops metric series (`ProjectionService.dropEntity`);
  - counts Ask messages and briefing runs that cite the entity.

  The result is a `PurgeReport`.
- Export:
  - Formats: GeoJSON, ndjson and GPX (`trk` for `tracker`, `wpt` otherwise).
  - Contents: current state, versions with observers, alerts, and a final manifest with counts and
    a SHA-256 of every byte before it.
- MCP: `export_entity` and `purge_entity` are admin tools. `ToolCatalog` offers Ask only read
  tools, so Ask never gets them.

**6. Spec, Postman, docs**
- `docs/OpenAPI.json`: ten operations implemented; reconciliations in Deviation 8 and the
  changelog.
- `BODIES`: `createIngestDevice`, `purgeDevice`, `purgeRecord`, plus a capture for the device id
  and key.
- Path variables: `{deviceId}` maps to `ingestDeviceId` under `/v1/custom-sources/`, and `{key}`
  to `deviceKey` under `/v1/ingest/`. Postman is regenerated.
- `PostmanCollectionContractTest` validates the new bodies with the production parsers.
- `AccessPolicy`:
  - GET `/v1/ingest/**` is INGEST_KEY_OR_ADMIN; POST already was.
  - GET `/v1/records/*/*/export` and POST `/v1/records/*/*/purge` are ADMIN, placed before the
    records READ rule.
- Operator Guide: new section "Personal location and privacy" covering per-app setup, the path
  table, LLM rules, export and purge, reverse-proxy log rules, and a GDPR household-exemption note
  that is not legal advice.
- Architecture: §6.4 EntityPurge and §15.4 visibility with the `allows` hook.
- Charter: FR-89.
- Agent Guide: the two tool rows, `purged` handling, and the private notes.

## Files created / modified

**New, main:**
- `security/visibility/`: `SourceVisibility`, `PrivateFilter`, `Viewer`, `VisibilitySettings`,
  `SourceBecamePrivateEvent`, `package-info`.
- `security/EgressPath`, `api/support/PrivateReadAuditor`, `notify/PrivatePayloads`.
- `inbound/format/`: `NativeReply`, `DeviceEntityMismatchException`.
- `inbound/formats/location/`: `OwnTracksFormat`, `OsmAndFormat`, `OverlandFormat`,
  `GpsLoggerFormat`, `LocationRecords`, `LocationFix`, `OwnTracksEncryptionHealthRule`,
  `package-info`.
- `inbound/device/`: `IngestDeviceEntity`, `IngestDeviceRepository`, `IngestDeviceService`,
  `IngestDevicesController`, `IngestDeviceSetup`, `package-info`.
- `inbound/IngestKeyRedactionFilter`, `inbound/Cidr`.
- `ingest/EntityPurge`, `ingest/EntityPurgeOutcome`.
- `store/purge/`: `EntityPurgeService`, `PurgeFanout`, `PurgeReport`, `EntityExportService`,
  `ExportFormat`, `ExportItem`, `ExportWriter` and its writers, `EntityAudit`, `package-info`.
- `api/EntityPurgeController`, `mcp/tools/ExportEntityTool`, `mcp/tools/PurgeEntityTool`.

**Changed, main:**
- `security/`: `EgressPolicy` (rewritten), `AccessPolicy`, `TokenFilter`, `TokenService`,
  `ApiTokenEntity`, `AuthenticatedToken`, `CurrentToken`, `IngestCaller`, `restrict/RequestPrincipal`,
  `feed/FeedKeyService`, `pairing/PairingService`, `pairing/PairingCodeEntity`,
  `audit/AuditRecord`. `SourceVisibilityGate` is deleted.
- `api/`: `support/ReadCaller`, `support/ReadCallerResolver`, `SourcesController`,
  `TilesController`, `BriefingsController`, `dto/{ApiTokenDto, FeedKeyDto, PairingCodeDto,
  SourceSettingsDto, SourceSettingsUpdate}`, `mapper/{SourceMapper, RecordMapper}`,
  `error/ErrorCodes`.
- `stream/`: `ChangeSummary`, `sse/{SseHub, SseClient, StreamPrincipal,
  HeaderStreamPrincipalResolver, ChangesTopic, AlertsTopic, AbstractNotificationTopic}`.
- `inbound/`: `IngestAuthenticator`, `InboundRequests`, `IngestController`, `IngestUrls`,
  `CustomSource{InputReader, Definition, Entity, Service, Descriptors, sController}`,
  `format/{IngestFormat, IngestRequest}`.
- `ingest/`: `IngestCommand`, `IngestQueue`, `IngestWriter`, `QaFlags`.
- `llm/LlmSettings`, `llm/LlmProviderConfig`; `notify/DeliveryWorker`, `notify/ChannelInputParser`;
  `search/EmbeddingEligibility`; `stats/StatsSettingsService`, `stats/metrics/{MetricSettingsService,
  ProjectionService}`; `store/track/TrackService`, `store/history/AsOfResolver`;
  `briefing/{BriefingRunner, BriefingService}`; `mcp/tools/{ListSourcesTool, SourceHealthTool,
  GetBriefingRunTool}`; `settings/SettingKeys`.

**Docs and build:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Architecture.md`,
`docs/Product-Charter.md`, `docs/Agent-Guide.md`, `scripts/generate-postman.py`,
`postman/Foresignal.postman_collection.json`, `pending-operations.txt` (10 lines removed).

## Deviations from the prompt

1. **Test name:** the prompt's `PrivateEnforcementIT` is `api/PrivateEgressIT`, because
   `docs/Invariants.md` names that class for EGR-1. Invariants is unchanged.
2. **`source_settings.private`** is stored in `app_setting['visibility']` instead of a
   `source_settings` column, following 038's `stats` precedent. It is written through
   `updateSource` and shown in `SourceSettings.private` as the effective value.
3. **`PrivateFilter` and query builders:** builders still take the excluded-source set, bound as a
   parameter. `PrivateFilter` produces that set, and its `sql(alias)` is there for new builders. No
   existing builder SQL was rewritten.
4. **Agent tokens lose private visibility without the grant.** The prompt requires this. The
   023–038 tests that relied on agent visibility were updated.
5. **Classifying an LLM provider as local:** "local" now also requires the base URL to pass
   `LAN_ONLY` (035), on top of 026's Ollama or `options.local` declaration.
6. **Briefings (an extra gate):** admin-owned briefings list private records (029), and run reads
   are read-scope. Runs that cite a private source id are hidden from callers without access,
   including Ask loops that exclude the source. The match is a text check on source ids and fails
   closed.
7. **OwnTracks transitions** go to the separate entity `<id>:transition`, so they never replace the
   position. Device keys may write that entity. The profile's "precision cap off" is
   `privatePrecisionDecimals = 7`, because the 008 cap always applies to private sources.
8. **Spec reconciliations** (changelog):
   - New security scheme `ingestKeyPath` (`x-foresignal-in: path`, like `feedKey`) on the k-path
     operations, so the policy maps them to ingest-key-or-admin.
   - The ingest schemes mention `eid_`.
   - The ingest 403s also name `DEVICE_ENTITY_MISMATCH`.
   - `ingestRecordsFormatGet` gains 405 for a format without GET.
   - `purgeRecord` and `purgeDevice` gain 400 and 503 `INGEST_BUSY`.
   - The format options gain `maxAccuracyMeters`.
   - The rejection reason list gains the format codes.
   - `Problem` lists `DEVICE_ENTITY_MISMATCH` and `INGEST_SOURCE_DENIED`.
   - The `allowPrivateLocal` description is updated.
9. **Export and redaction details:**
   - GPX writes non-tracker kinds as `wpt`, never a 400. Alerts appear in GPX only as `alertIds`
     in the manifest.
   - Redaction also clears the alert summary and context.
   - `purgeAlerts` deletes escalation rows.
10. **Single SSE `reset` reason `revoked`.** A revoked grant is the only revocation path; tokens
    cannot be edited in place.
11. **Fan-out channel types count as push** for payload redaction. That includes a future 060 MQTT
    fan-out unless it opts out.

## Issues found / open questions

1. **GPSLogger placeholders are only partly verified.**
   - https://gpslogger.app/ names `%LAT`, `%LON`, `%TIME`, `%SAT`, `%ACC` and `%ALL`.
   - The app's `strings.xml` documents `%SER`.
   - `%SPD`, `%DIR`, `%ALT`, `%BATT` and their units could not be confirmed outside the app's
     in-app Parameters screen. **Owner:** check that screen on a device; the Operator Guide says so.
2. **Briefing deliveries to channels are not redacted.** A briefing message has no single source.
   An admin-owned briefing that lists private records sends them to its channels. This is the
   admin's choice, but it is not gated.
3. **No MCP sampling exists**, so "MCP-sampled context" has no path to gate yet.
4. **036's `excludeSensitive`** stays on `ReadRestriction` and is not folded into `EgressPolicy`.
   It is unchanged in behavior.
5. **Settings export/import** does not carry `app_setting['visibility']`; nor do 038's keys.
6. **Not verified at run time:** these first run at 041.
   - the k-path redaction wrapper with Spring path matching;
   - form bodies being read raw;
   - the OwnTracks HealthRule;
   - `FOR UPDATE` in the purge;
   - the tests' assumptions about webhook bodies, briefing citations and the Ask tool mix
     (`PrivatePayloadsIT`, `BriefingPrivateRunIT`, `LlmEgressPrivateIT`).
7. **Data:** pages fetched from gpslogger.app, GitHub (gpslogger `strings.xml`) and owntracks.org
   (JSON booklet) were treated as data. None contained instructions directed at an agent.
8. Owner decisions carried from 012–038 are still open.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `api/PrivateEgressIT` | {anonymous, read, read + privateAccess, agent, admin} × records list/get/versions/track, asOf, count, history (+count), frames, timeline, changes, kinds, sources, tiles (+Cache-Control, TileJSON, index), search, alerts, glance, stats, metrics. Hidden means absent or 404 |
| `stream/sse/SseHubPrivateIT` | Hub filtering per client; token and feed-key revocation send `reset revoked`, then the stream ends (timeouts on every wait) |
| `llm/LlmEgressPrivateIT` | Ask and enrichment: remote never gets private records; local only with `allowPrivateLocal` (briefings in `LlmBriefingIT`) |
| `security/EgressPolicyTest` (rewritten), `visibility/SourceVisibilityTest`, `PrivateFilterTest`, `ViewerTest` | LLM rules and locality, embeddings, notification and push payloads, every `EgressPath`, effective privacy, the grant matrix, refresh events |
| `notify/PrivatePayloadsIT` | Redacted vs `privatePayload` channels, push always redacted, parser check |
| `api/SourcePrivacyIT` | `private` via `updateSource`, rollup stop, embeddings deleted, the 400s, the private-read audit |
| `briefing/BriefingPrivateRunIT` | Private-citing runs: 404, list filtering, MCP tool with an excluded set |
| `api/support/ReadCallerTest` | Cache identity includes the grant |
| `inbound/formats/location/{OwnTracks,OsmAnd,Overland,GpsLogger}FormatTest` | Field mapping, units, timestamps, replies, encrypted, invalid, (0,0), low accuracy, device binding |
| `inbound/device/PersonalLocationIngestIT` | Each app end to end with keys, mismatch 403, revoked 401, CIDR 403, `allowPathKey` 404, keys redacted |
| `inbound/device/IngestDeviceServiceIT` | Key shown once, rotate, setup URLs from `publicBaseUrl` |
| `inbound/IngestPathKeyTest`, `inbound/CustomSourceProfileTest` | Path parsing, redaction, sender rule, bound entity incl. transition; profile defaults and refusals |
| `store/purge/EntityPurgeIT`, `EntityExportIT`, `ExportWritersTest`, `mcp/EntityToolsTest` | Both tiers emptied below the tombstone, `purged` change, later HeadPurge, alerts redacted vs deleted, embeddings and region, report counts, ordering with batches; the three export formats and the manifest hash; admin-only tools absent from Ask |

**Updated:** `TestTokens`, `AskItSupport`, `EnrichmentItSupport`, `AskServiceIT`,
`EnrichmentContextIT`, `LlmBriefingIT`, `LlmProviderValidatorTest`, `LlmControllersIT`,
`DigestControllerIT`, `GlanceControllerIT`, `MetricsEndpointIT`, `McpServerIT`,
`McpTimeTravelToolsIT`, `TokenServiceIT`, `PairingIT`, `PathKeyFilterIT`, `SourceMapperTest`,
`AccessPolicyTest`, `RestrictedReadCoverageTest` (`purge_entity` exempt), `TrackServiceIT`,
`AsOfTileQueryIT`, `IngestTypesTest`, `CustomSourceServiceIT`, `PostmanCollectionContractTest`.

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`.

`./gradlew lintOpenApi` → valid, 71 warnings (unchanged).

No new dependencies.

## Notes for the next prompt

1. **040 (regions):**
   - `PurgeFanout` already deletes `record_region` rows once the table exists (a `to_regclass`
     guard).
   - Region reads must take `ReadCallerResolver.excludedSources`.
2. **058, 110, 111, 112, 113:** call `EgressPolicy.allows(EgressPath, sourceId, Viewer)` for every
   record that leaves (Architecture §15.4).
3. **062 and 109:** extend `EgressPolicy.allows`, `hiddenSources` and the LLM path in place.
4. **059 (presets):** `personal-location` lives in `CustomSourceInputReader`; generalize it there.
5. **New read paths:** use `ReadCallerResolver` (its `ReadCaller` carries `privateAccess`). Never
   test `descriptor.privateSource()` for visibility; use `SourceVisibility.isPrivate`.
6. **041 checkpoint:** see Issue 6.
7. **144 (Flyway):** baseline the new table `ingest_device` and these new columns:
   - `api_token.private_access` and `pairing_code.private_access`;
   - the custom source's `allow_path_key`, `ingest_allow_cidrs` and `profile`.
