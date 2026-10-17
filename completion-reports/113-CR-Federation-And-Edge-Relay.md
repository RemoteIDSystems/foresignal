# 113-CR-Federation-And-Edge-Relay

| | |
|---|---|
| Prompt | 113-Federation-And-Edge-Relay.md |
| Status | Complete (owner decisions listed under Issues) |
| Implementation commit | `32a3babbdcc2b2dcc767763a404083c691edb775` |
| Date | 2026-10-16 |

## Summary

Foresignal instances share data with each other. Compiles; tests written, not run.

- **Identity and refs (`ai.foresignal.federation`):** `RecordRef` is the one class that formats and parses
  `urn:foresignal:{instanceId}:{sourceId}:{externalId}` (RFC 3986 percent-encoding of the external id) and maps a
  federated record to its local id `{originSourceId}/{originExternalId}` (`sha256:` + hex above 256 characters).
  `globalRef` is served on records, versions and changes (`RecordMapper`). `FederationIdentity`: instance id = the
  install id, display name from the `federation.instanceName` setting (new `SettingKeys.FEDERATION_INSTANCE_NAME`),
  else `foresignal.federation.instance-name`, else `instance.name`; the 4-byte short id for compact messages.
  `GET /v1/federation/info` is signed with a detached JWS (`JwsSigner`) in `Foresignal-Signature`.
- **Serving side:** `TokenRestrictions` gains `federation` and `includeExercise` (stored on `api_token`,
  accepted by `RestrictionValidator`). `FederationController` serves `/v1/federation/changes`, `/stream` (SSE:
  `change` with the full item and `id:` = seq, optional `live`, `heartbeat` every 15 s, `reset` below the purge
  horizon; `FederationStreamService`, at most 16 streams, `429 STREAM_LIMIT`) and `/snapshot` (paged
  `record_head ⋈ record_live` pinned to a watermark, `Cursor.ById`). All need read scope plus
  `restrictions.federation=true` (`403 RESTRICTED` otherwise); token restriction columns apply
  (`FederationQueries` with `RestrictionClamp`); `410 RESYNC_REQUIRED` below the purge horizon.
  `FederationFeedService` decides every row with `EgressPolicy.decide(FEDERATION, …)` with its resolved license
  (`LicenceResolver`, including a peer record's `_foresignal.originLicence`) and payload (MLAT), before
  serialization; private and `LOCAL_ONLY` sources are also pruned in SQL; exercise records (`"exercise": true`)
  only with `includeExercise`; refusals count in `foresignal.federation.denied{reason}`. Provenance: local records
  `chain:[origin]`, peer records their stored chain plus a hop for this instance.
- **Consuming side (`foresignal-peer`):** entity `peer` (`PeerEntity`: slug id, name, base URL, encrypted token,
  pinned kid and key, remote instance id, selection jsonb, mode `PULL|RECEIVE`, enabled, `sameOperator`,
  reliability, state, health reason, last seq, remote watermark, last error, ingest key hash/prefix).
  `PeerService` registers `peer-{id}` (STANDARD, `ALL`), `peer-{id}-trk` (TRACK, `OBSERVED`, TTL
  `selection.trackTtl`, `SAMPLED` with the §5.3 aircraft/vessel significant fields) and `peer-{id}-events` through
  `registerDynamic`; PULL sources are `STREAMING` with `PeerAdapter`/`PeerTrackAdapter`, RECEIVE sources `PUSH`.
  Descriptors: `observedAtMode=UPSTREAM`, license "mixed (per record)", reliability `F` unless configured,
  `volatileFields: ["_foresignal"]`, reconnect ladder 5 s → 5 min. `PeerSession` (one pass per supervisor start):
  `/info` JWS verified against the peer's JWKS (fetched from the peer's base URL) and the pinned kid (TOFU on first
  success; `PEER_KEY_MISMATCH` stops ingest), snapshot on first start or resync, catch-up via `/changes` pages,
  SSE from the cursor (`Last-Event-ID`), `reset` → resync then `TombstoneAbsent` of local heads absent from the
  snapshot; every request is a budgeted request of the peer's source. `FederationIngestor` applies, identically for
  PULL and RECEIVE: loop (`chain` names this instance) and hop limit (`maxHops`, default 3), private, the selection
  re-checked locally (sources, kinds, bbox, place polygon, `maxAge`), license (FORBIDDEN never; `LOCAL_ONLY` only
  from a `sameOperator` peer), and `OriginLedger` dedup on `federation_origin` (upsert only when the origin seq is
  greater; equal = duplicate, lower = `dedup`; first claimant owns the origin, later updates go into the owner's
  source; ownership moves at the next arrival once the owner's peer is disabled or deleted). Records keep kind,
  geometry, `eventTime`, explicit `validUntil` and quality, plus `_foresignal {originRef, originSeq, originLicence,
  chain}`; remote tombstones become local tombstones. Metrics `foresignal.federation.items{peer,result}`,
  `foresignal.federation.dropped{reason}`, `foresignal.federation.lag_seq{peer}`.
- **Edge relay:** `foresignal.mode=edge` (`FORESIGNAL_MODE`, `EdgeMode`) drops, by `@ConditionalOnProperty`, the
  embedding worker and cursor, Ask, the analyst controller, briefings (controller and scheduler), raster products
  and rendering (tiles, products, WMTS, static maps), OGC (both controllers), exports (exports, bulk exports,
  archive, packs, snapshots) and federation serving; `/v1/system` reports `mode` and turns those `features` off
  (`FederationFeature` adds `federation`/`federationServing`); `EdgeModeDefaults` seeds `retention` (6 h track
  window, 1 d default) when none is stored. Hub side: `POST /v1/federation/ingest/{peerId}`
  (`FederationIngestService`): peer key (`fsp_`, new `AccessRequirement.PEER`, `TokenFilter.peerByKey`), RECEIVE
  peer, gzip accepted, ≤ 1,000 items (413), JWS verified against the pinned key (first batch pins the key in
  `Foresignal-Key`), the same rules as PULL, idempotent on `(origin_ref, origin_seq)`. Edge side: the
  `federation-push` sink (`FederationPushSinkType`, 110 SPI, CHANGE_FEED, durable cursor) reads items back by seq,
  signs and gzips them; the sink gate uses the `FEDERATION` path with the `sameOperator` exception
  (`SinkRecordGate.allowFederation`, `EgressPolicy.EgressContext.federation`), which labels such records
  `local-only-operator` so the hub keeps them `LOCAL_ONLY`.
- **Compact messages (`ai.foresignal.federation.compact`):** `MeshCborMessage`/`MeshCborProfile` gain keys 10
  (origin short id), 11 (message id: first 8 bytes of SHA-256 of the origin ref) and 12 (expiry minutes) and type 5
  (intelligence event); the decoder skips unknown or ill-typed keys; ≤ 160 bytes, extended messages drop key 9 then
  8 and never 10–12. `CompactMessages` builds them from alerts and intelligence events only; `CompactOutbox`
  (priority, TTL, dedup with resend on severity change or resolution, per-transport byte budget);
  `CompactOutboxService` feeds it from `AlertBus`/`IntelEventBus`, gated by `EgressPolicy`; `CompactMeshTransport`
  sends through mesh channels with `compactEvents: true` via `MeshChannelType.renderCompact` and the 084
  governor path; `GET /v1/federation/bundle` (CBOR array) and `POST /v1/federation/bundle` (peer key →
  `remote-event` records on `peer-{id}-events`, reliability `F`, quality `compact: true`, never re-federated).
- **API, MCP, docs:** `PeersController` (`listPeers`, `createPeer`, `getPeer`, `updatePeer`, `deletePeer`,
  `testPeer`, `resyncPeer`, `rotatePeerIngestKey`; admin), `FederationReceiveController` (info, ingest, bundles).
  MCP `list_peers` and `peer_status` (admin). The 15 operations implemented and removed from
  `pending-operations.txt`; spec reconciled (see Deviations); Postman regenerated with BODIES for `createPeer`,
  `updatePeer`, `federationIngest` (836 requests). Operator Guide "Federation and edge relay (prompt 113)",
  Architecture §30 "Federation", Agent Guide rows, Invariants (EGR-1, EGR-2, ING-7 tests), `application.yml`
  (`foresignal.mode`, `foresignal.federation.*`).

## Files created / modified

**Created (main):** `federation/{RecordRef,FederationIdentity,FederationRules,FederationFeedService,
FederationStreamService,FederationIngestor,FederationIngestService,FederationWriter,FederationFeature,
FederationPushSinkType,OriginLedger,FederationOriginEntity,PeerEntity,PeerRepository,PeerSelection,PeerDescriptors,
PeerService,PeerStates,PeerRuntime,PeerSession,PeerAdapter,PeerTrackAdapter,PeerRemote,PeerKeys,package-info}`,
`federation/compact/{CompactMessages,CompactOutbox,CompactOutboxService,CompactBundles,CompactBundleReceiver,
CompactMeshTransport,package-info}`, `store/query/FederationQueries`, `api/{FederationController,
FederationReceiveController,PeersController}`, `config/{EdgeMode,EdgeModeDefaults}`,
`security/{PeerKeyAuthenticator,PeerCaller}`, `mcp/tools/{ListPeersTool,PeerStatusTool}`.

**Modified (main):** `security/{TokenRestrictions,RestrictionValidator,ApiTokenEntity,EgressPolicy,AccessPolicy,
AccessRequirement,TokenFilter}`, `chatops/ChatPrincipals`, `source/SourceRegistry` (dynamic STREAMING),
`api/mapper/RecordMapper` (`globalRef`), `api/error/ErrorCodes` (`PEER_KEY_MISMATCH`, `PEER_EXISTS`),
`settings/SettingKeys`, `sink/{SinkRecordGate,SinkSupervisor}`, `notify/mesh/{MeshChannelType,MeshChannels}`,
`notify/mesh/cbor/{MeshCborMessage,MeshCborProfile}`, `notify/types/{MeshtasticChannel,MeshCoreChannel}`,
`system/SystemInfoService`, `search/{SearchStatusService,EmbeddingWorker,EmbeddingCursorConsumer}`,
`briefing/BriefingScheduler`, `api/{AskController,AnalystController,BriefingsController,OgcController,
OgcFeaturesController,WmtsController,RasterTilesController,ProductsController,StaticMapController,ExportsController,
BulkExportsController,ArchiveController,PacksController,SnapshotsController}` (edge-mode conditions),
`resources/application.yml`.

**Docs/spec/tooling:** `docs/OpenAPI.json`, `docs/Operator-Guide.md`, `docs/Architecture.md`, `docs/Agent-Guide.md`,
`docs/Invariants.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−15).

**Tests:** `federation/{RecordRefTest,PeerDescriptorsTest,LoopAndDedupTest,FederationFeedIT,FederationIngestIT,
PeerSourceIT}`, `federation/compact/{CompactMessageCodecTest,CompactOutboxTest}`, `sink/FederationPushSinkIT`,
`config/EdgeModeContextTest`, `testsupport/federation/TestJws`; updated `notify/mesh/cbor/MeshCborProfileTest`
(extension vector), `security/AccessPolicySyncTest` (peer scope), `openapi/PostmanCollectionContractTest`
(peer and federation-token auth, schema-only peer bodies), `security/restrict/RestrictedReadCoverageTest`
(`list_peers`, `peer_status` exempt).

## Deviations from the prompt

1. **Dynamic STREAMING sources.** `SourceRegistry.registerDynamic` accepted only PUSH, DERIVED or POLLING-with-
   adapter; it now also accepts STREAMING with a `StreamingAdapter`, so `peer-{id}` runs under the 010 supervisor.
2. **One connection per peer.** `peer-{id}-trk` opens nothing: while it runs, the `peer-{id}` session hands TRACK
   items to its sink (`PeerTrackAdapter`); otherwise they go straight to the writer. To route them the feed item
   needs its tier, so `FederationItem` gains `historyTier` (spec amended). The remote kind's TTL is not in the feed,
   so the track TTL is `selection.trackTtl` (default 10 min). `peer-{id}-trk` is registered when the selection names
   no kinds or a kind some local TRACK source declares.
3. **Kinds of peer sources.** A descriptor must declare its kinds up front: `selection.kinds`, else every kind a
   local source declares (the local vocabulary); items of other kinds are dropped as `selection`.
4. **Live lane not applied.** `live=true` is requested when the selection asks, but a received `live` event carries
   only kinematic fields and is not applied locally (the persisted versions still arrive as `change`).
5. **Backfill.** The feed has no time → seq index, so `backfill: <duration>` pages `/changes` from seq 0 and keeps
   items observed within that duration; a 410 falls back to the snapshot.
6. **JWKS location.** The JWKS is always fetched from the peer's own base URL, never from the host `jwksUri` names.
7. **Edge key pinning.** A hub cannot fetch the JWKS of an edge behind NAT, so the first batch carries the edge's
   public key in a new optional `Foresignal-Key` header (spec amended); its RFC 7638 thumbprint must equal the JWS
   kid, and later batches must be signed by the pinned key.
8. **Operator-settable name.** There is no settings endpoint for single keys, so `federation.instanceName` is read
   from the app setting of that key or the property `foresignal.federation.instance-name`
   (`FORESIGNAL_FEDERATION_INSTANCE_NAME`).
9. **Admin tokens** need `restrictions.federation=true` too (the prompt: "read scope plus `federation=true`").
10. **Exercise records** (115 is not built) are records whose payload has `"exercise": true`, the flag 112's
    `CapService` reads; 115 must keep it or update `FederationRules.isExercise`.
11. **Private check on arrival:** an item marked `private: true` is dropped; the serving side never sends one.
12. **Same origin over two paths:** a later update arriving over a non-owning peer is written into the owner's
    source under the same local id, so there is exactly one local record.
13. **Snapshot replay** writes duplicates too (forced touches), so heads the snapshot lists are not tombstoned by
    the absent-head reconciliation; the existing `TombstoneAbsent` command is used (maxAbsentRatio 0.9; an
    operator's `resync` releases the shrink guard).
14. **MLAT under `sameOperator`:** MLAT positions still never leave, even to the operator's own hub (the 069 rule).
15. **CBOR drop order:** extended messages (keys 10–12 present) drop key 9, then key 8; a message without
    extensions keeps 084's order (8, then 9), so the 084 golden vectors are unchanged.
16. **Intelligence-event tombstones:** a resolved or merged event is sent as type 2 (resolved) with the same key 11.
17. **Mesh transport opt-in:** alerts already reach mesh channels through 084 rules, so compact messages go only to
    channels with `compactEvents: true` (new `MeshChannelCommonConfig` field, spec amended); the byte budget is
    `maxMessagesPerHour × 160` bytes per hour. The outbox is in memory (a restart loses queued messages).
18. **Bundle GET in edge mode:** `GET /v1/federation/bundle` stays available on an edge (store and forward is an
    edge's job); "federation serving" means changes, stream and snapshot.
19. **Edge mode** disables beans at their entry points (controllers, the embedding worker and cursor consumer,
    the briefing scheduler); `SearchStatusService` now takes the embedding worker through an `ObjectProvider`. The
    heap target is documented, not enforced. Retention defaults are seeded only when no retention settings exist,
    with track downsampling off (its raw window could not be shorter than a 6 h track window).
20. **`federation-push` gate** uses the `FEDERATION` egress path, not `SINK_PUSH`; records the feed refuses are
    acknowledged (skipped), as other refused sink records are.
21. **RECEIVE peers** register PUSH sources; every peer gets `peer-{id}-events`. RECEIVE peers show state
    `receiving`.
22. **Snapshot pages** may hold fewer than `limit` items: refused rows are removed after the keyset page is read.
23. **US English:** new identifiers say "license" (`LOCAL_ONLY_LICENSE`, `ORIGIN_LICENSE`); wire names
    (`licence`, `originLicence`, `licenceBasis`) are unchanged.

Spec changelog entry added (`info.x-foresignal-changelog`, 113): statuses, `globalRef`, `historyTier`,
`Foresignal-Key`, `dropped.selection`, `compactEvents`, `PEER_KEY_MISMATCH`/`PEER_EXISTS`, the new features flags.

## Issues found / open questions

- **Owner decisions:** (a) should admin tokens be exempt from the `federation` capability (Deviation 9)? (b) should
  received `live` events be applied (Deviation 4)? (c) should MLAT travel to a `sameOperator` hub (Deviation 14)?
  (d) compact messages on mesh as an opt-in per channel (Deviation 17)? These follow the prompt text or the stricter
  reading of the existing code; none blocks.
- `OriginLedger` records an arrival before the writer stores it; if the write then fails, a re-send counts as a
  duplicate and that version is lost until the next newer one.
- 112's open decision (private share targets) is unaffected.
- `peer`, `federation_origin` and the `api_token.federation`/`include_exercise` columns arrive through Hibernate
  `ddl-auto=update`; prompt 144 must capture them.
- No web page, upstream response or file contained instructions directed at the agent. Nothing was fetched from
  the network; no dependency was added.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `federation/RecordRefTest` | Format/parse round trip, percent-encoding and decoding, malformed refs and parts, local id hashing above 256 characters, message ids |
| `federation/PeerDescriptorsTest` | The three descriptors validate, dynamic STREAMING registration, kinds and TRACK by selection, selection parsing, defaults, round trip and errors, remote filter query |
| `federation/LoopAndDedupTest` | Own instance in chain dropped, `maxHops`, one local record for the same origin over two peers, stale seq dropped, duplicates, ownership transfer after peer deletion |
| `federation/FederationFeedIT` | Items with license and provenance; FORBIDDEN, `LOCAL_ONLY`, MLAT, private and exercise records excluded; `includeExercise`; 403 without `federation`; restrictions apply; 410 below the purge horizon; snapshot paging at a pinned watermark; peer record chain and origin license; same-operator push labels; signed `/info` |
| `federation/PeerSourceIT` | WireMock remote: JWS and TOFU pinning, mapping to `peer-{id}`/`peer-{id}-trk`, cursor and `Last-Event-ID`, token header, origin license, `_foresignal` not hashed, tombstone propagation, reset → resync → absent heads tombstoned, key mismatch stops ingest |
| `federation/FederationIngestIT` | Peer key auth (no key, admin token, other peer, rotated key), JWS and pinning, idempotency, gzip, loop rule, `sameOperator` LOCAL_ONLY rule and no re-federation |
| `sink/FederationPushSinkIT` | Hub down: cursor holds; hub back: catches up with signed gzip batches carrying provenance and the key; FORBIDDEN never leaves; gate on the FEDERATION path |
| `config/EdgeModeContextTest` | Edge context: heavy beans absent, core beans present, `mode` and `features`, retention defaults |
| `federation/compact/CompactMessageCodecTest` | Extended golden vector, old 084 vectors, ≤ 160 bytes with drop order 9, 8 before 10–12, title cut on a code-point boundary, type 5 and resolution, alerts, only events become messages |
| `federation/compact/CompactOutboxTest` | Priority order, TTL expiry, dedup and resend on severity change or resolution, byte budget and sliding window, unsend, capacity |

## Compile result

`./gradlew compileJava compileTestJava` → `BUILD SUCCESSFUL`. `docs/OpenAPI.json` parses; the 15 operations are
`implemented`; `python3 scripts/generate-postman.py docs/OpenAPI.json postman` → "836 requests across 80 tag
folders; quickstart 12 steps; covers all ops: True". No new dependency.

## Notes for the next prompt

- **114 onward:** a source whose records must never be federated needs nothing new: `EgressPolicy.decide(FEDERATION,
  …)` already applies private, `LOCAL_ONLY`, MLAT and license rules. Alerts and intelligence events become compact
  messages automatically (`CompactOutboxService`).
- **115 (exercises):** federation treats `"exercise": true` in a record's payload as an exercise record
  (`FederationRules.isExercise`); keep that flag or update it there and in `CapService.status`.
- **134 (chronicle sketches):** `POST /v1/federation/chronicle-sketches` already maps to `AccessRequirement.PEER`;
  `PeerCaller.of(request)` names the peer, and `PeerKeys.verify` checks a detached JWS against the peer's pinned key.
- **124 (checkpoint):** the new ITs share the `RecordsTestSources` / `SinkTestConfig` contexts; `EdgeModeContextTest`
  builds its own context with `foresignal.mode=edge`.
- **144 (Flyway):** capture `peer`, `federation_origin` and `api_token.federation`, `api_token.include_exercise`.

Implementation commit: `32a3babbdcc2b2dcc767763a404083c691edb775`
