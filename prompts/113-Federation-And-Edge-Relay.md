# 113 — Federation And Edge Relay

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/113-CR-Federation-And-Edge-Relay.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults) applies to every prompt and wins over conflicting prompt text.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and `docs/Intelligence-Design.md` is authoritative for the intelligence layer (prompts 098–126).
- **Previous Completion Report:** read `completion-reports/112-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Gap-Analysis.md` §6 row "Out-of-tree plugins, federation, edge relay"; §9 (re-serving OpenSky/ACLED/aisstream "to peers or sinks" rejected)
- `docs/Core-Data-Model.md` §2 (time contract, `observedAtMode`), §3 (ingest input), §4 (hashing; `volatileFields` are excluded from the hash), §10 (ChangeBus, LiveBus, SSE events `change`/`live`/`reset`/`resync`, client contract), §12 (`/v1/changes`), §13 (consumer clamp)
- Completion Reports of 003 (`InstallIdService`), 004 (`registerDynamic`, `Mode.STREAMING`), 010 (streaming supervisor), 014 (`/v1/changes`, `410 RESYNC_REQUIRED`), 015 (SSE lanes, `reset`), 023 (restricted tokens), 035 (outbound URL policy), 039 (private flag), 084 (compact mesh profile, duty cycle), 101 (`intelligence_event`), 109 (`LicenceEgressGuard` `FEDERATION`, per-record `originLicence`, JWKS and `JwsVerifier`), 110 (Sink SPI, CHANGE_FEED cursors)

## Objective

Let Foresignal instances share data with each other safely. A new `foresignal-peer` source family consumes another instance's change feed with a provenance chain (origin instance id and origin seq) on every record, selective replication, licence and permission checks on the serving side, loop prevention, dedup and globally unique record refs. An edge-relay mode lets a lightweight node (a Pi at a receiver site) collect locally and forward over intermittent links, and a compact, prioritised message format carries only events, never raw tracks, over mesh or other delay-tolerant links.

## Tasks

### 1. Identity and global record refs (`ai.foresignal.federation`)

- **Instance id** = the install id from `InstallIdService` (003). Add an operator-settable display name `federation.instanceName`.
- **`RecordRef`:** `urn:foresignal:{instanceId}:{sourceId}:{externalId}` (externalId percent-encoded). Expose `ref` on the Record DTO, version items and `Change` (014). Parse and format in one class.
- **`GET /v1/federation/info`** (read): `{instanceId, instanceName, version, apiVersion, jwksUri, capabilities:["changes","stream","snapshot","ingest"], time}`, signed: the response carries an `Foresignal-Signature` detached JWS over the body (109 `JwsSigner`).

### 2. Serving side: federation feed

- **Token capability:** add `federation` (bool) to restricted tokens (023/035). Federation endpoints require read scope plus `federation=true`. Restriction columns (sources, kinds, bbox) still apply.
- **Endpoints:**
  - `GET /v1/federation/changes?after&limit` (≤ 1,000): full version items (seq, ref, sourceId, externalId, kind, version, persistReason, observedAt, eventTime, validUntil, tombstone, geometry, payload, quality, `licence` terms id, `provenance`).
  - `GET /v1/federation/stream`: SSE with the 015 lanes and events. `change` carries the full item; `live` is optional with `live=true`.
  - `GET /v1/federation/snapshot?atSeq&cursor`: paged `record_head ⋈ record_live` at the current watermark, for resync.
  - Below the purge horizon → `410 RESYNC_REQUIRED` and SSE `reset`, exactly as 014/015.
- **`provenance`:**
  - for local records: `{origin:{instanceId, sourceId, externalId, seq, ingestedAt}, chain:[origin]}`
  - for records that came from a peer: the stored chain plus a hop `{instanceId, sourceId, externalId, seq, ingestedAt}` appended for this instance.
- **Egress checks per record,** before serialisation:
  - `LicenceEgressGuard.allows(FEDERATION, terms)`: FORBIDDEN and LOCAL_ONLY are excluded, so `opensky-states`, `acled`, `aisstream`, local receivers and MLAT positions never leave.
  - Private sources (039) are always excluded.
  - Exercise records (115) are excluded unless the token sets `includeExercise`.
  - Count exclusions in `foresignal.federation.denied{reason}`.

### 3. Consuming side: the `foresignal-peer` source family

- **Entity `peer`:** `id` (slug), `name`, `base_url` (outbound URL policy 035; LAN, Tailnet and public allowed per policy), `token` (encrypted credential), `pinned_kid` (JWK thumbprint pinned on first successful `/info`; TOFU, changeable only by admin), `remote_instance_id`, `selection` jsonb, `mode` (`PULL | RECEIVE`), `enabled`, `state`, `last_seq`, `last_error`.
- **Dynamic sources:** each enabled peer registers via `SourceRegistry.registerDynamic` (004): `peer-{id}` (STANDARD) and, when the selection includes TRACK-tier kinds, `peer-{id}-trk` (TRACK, `ttlAnchor=OBSERVED`, TTL = the remote kind's TTL or `selection.trackTtl`, default 10 min).
  - Descriptor: `Mode.STREAMING`, `observedAtMode=UPSTREAM` (the remote `observedAt`), `licence` "mixed (per record)", `reliability` `F` unless configured, `volatileFields: ["_foresignal"]`.
  - Default ingest policy: STANDARD → `ALL`; TRACK → `SAMPLED` with the Core-Data-Model §5.3 track defaults.
- **Mapping:**
  - local `externalId` = `{originSourceId}/{originExternalId}` (if > 256 chars: `sha256:` + hex plus the original in the payload)
  - kind, geometry, `eventTime`, `validUntil` (explicit only) and quality preserved
  - payload = remote payload plus `_foresignal: {originRef, originSeq, originLicence, chain}` (volatile, so never hashed)
  - 109's resolver uses `_foresignal.originLicence` as the per-record licence.
- **Selection:** `sources[]`, `kinds[]`, `bbox` or `placeId` polygon, `maxAge` (drop items with `observedAt` older than this), `backfill` (`none` = current snapshot only, default; or a duration of history to page from `/changes`), `live` (bool), `maxHops` (default 3). Remote filters are passed as query parameters; re-check locally.
- **PULL runtime** (streaming supervisor, 010):
  1. `/info`: verify the JWS against the remote JWKS and the pinned kid; on mismatch → `failing` with a clear health reason, no ingest.
  2. Snapshot on first start or resync.
  3. SSE from `last_seq`, with reconnect backoff 5 s → 5 min, honouring `Retry-After`.
  4. Catch up via `/changes` pages after gaps.
  - Budget: SSE connects count as requests (005); snapshot and catch-up pages are budgeted.
- **Loop prevention:** drop any item whose `chain` contains the local instance id, or whose chain length ≥ `maxHops` (`foresignal.federation.dropped{reason="loop"|"hops"}`).
- **Dedup across paths:** table `federation_origin(origin_ref PK, owner_source_id, local_external_id, origin_seq bigint, updated_at)`. On arrival, upsert only if `origin_seq` > stored; otherwise drop as `dedup`. The first peer source to claim an origin ref owns it; ownership moves when the owning peer is disabled or deleted, at the next arrival. Origin seq is monotonic per origin instance, so this orders updates arriving over different paths.
- **Tombstones** propagate (remote tombstone → local tombstone). A remote `reset` → resync snapshot, then tombstone local heads absent from the snapshot (the "tombstone absent heads" writer support; check it exists, else implement it as an `IngestBatch` flag).

### 4. Edge relay mode

- **Profile `foresignal.mode=edge`** (env `FORESIGNAL_MODE=edge`): enables collection (local receivers 069, mesh 083, keyless AIS 085, decoders 086), the writer, triggers and channels. Disables embeddings, LLM features, raster products and rendering, briefings, OGC, exports and federation serving, via feature flags checked at bean creation (`@ConditionalOnProperty`). Defaults: `retention.trackWindow=6h`, `retention.defaultWindow=1d`, JVM heap target 256 MB documented. `/v1/system.features` reflects the mode.
- **Push to a hub** (`mode=RECEIVE` on the hub's peer row):
  - The hub exposes `POST /v1/federation/ingest/{peerId}`. Auth by a peer ingest key (`fsp_…`, shown once; Bearer). Body: up to 1,000 version items with provenance (gzip accepted). Idempotent on `(origin_ref, origin_seq)` via `federation_origin`. Same loop, dedup, licence and private checks as PULL; the hub also verifies the edge's JWS over each batch body against its pinned key.
  - The edge runs a `federation-push` sink (110 Sink SPI, CHANGE_FEED, durable cursor) to the hub URL. Offline periods hold the cursor; the 110 clamp release and resync rules apply.
- Edge records keep the edge's licence terms. `LOCAL_ONLY` sources may be pushed to a hub **only** when the operator marks the peer `sameOperator=true` (their own hub); this is the one documented exception, and the hub then treats those records as `LOCAL_ONLY` again (never re-federated).

### 5. Compact prioritised intelligence messages

- **Scope:** only events: fired and resolved alerts (017) and `intelligence_event` rows (101), plus tombstones of those. Never raw tracks, live updates or bulk records.
- **`CompactMessage`: reuse 084's `MeshCborProfile` codec** (integer-key CBOR map: `0` version 1, `1` type, `2` short code, `3` severity 0–2, `4` kind ≤ 16 chars, `5`/`6` lat/lon int ×1e5, `7` epoch seconds, `8` title ≤ 60 chars, `9` source id ≤ 24 chars; ≤ 160 bytes, dropping key 8 then 9 to fit). Extend it compatibly:
  - a new type `5` (intelligence event);
  - optional keys `10` origin instance short id (4 bytes), `11` message id (first 8 bytes of SHA-256 of the origin ref), `12` expiry minutes after key 7.
  - Make the 084 decoder ignore unknown keys if it doesn't already, and extend its golden vectors.
  - Size budget stays ≤ 160 bytes. Drop keys 9, then 8, before 10–12, which are needed for dedup and loop prevention.
- **`CompactOutbox`:** a priority queue (critical, then warning, then info; newest first within a class) with per-message TTL (drop when expired), dedup by message id (key 11; a resend only on severity change or resolve), and a byte budget per window taken from the 084 duty-cycle settings.
- **Transports:**
  - the 084 mesh channels: send through the channel's `profile` exactly as 084 does (`text` short form within the MeshCore 133-char cap, or `cbor` sent as `~E1 ` + base64url in the Meshtastic JSON downlink);
  - `GET /v1/federation/bundle?since=` (federation token) returning a CBOR array of pending messages for store-and-forward gateways (sneakernet or DTN tooling such as BPv7 bundle agents);
  - `POST /v1/federation/bundle` (peer ingest key) accepting the same, decoded into alerts on a `peer-{id}-events` source (kind `remote-event`, STANDARD, TTL = `x`).
- Received compact messages carry `grade` reliability `F` and a `compact: true` quality flag, and never trigger outbound federation of themselves (loop rule via keys 10 and 11).

### 6. API, health, spec and docs

- **Peers:** `GET/POST /v1/peers`, `GET/PUT/DELETE /v1/peers/{id}`, `POST /v1/peers/{id}/test` (fetch `/info`, verify, report), `POST /v1/peers/{id}/resync`, `POST /v1/peers/{id}/ingest-key` (rotate; shown once). All admin.
- **Health:** peer sources use 006 health with reasons `PEER_KEY_MISMATCH`, `PEER_UNREACHABLE`, `PEER_RESYNC`. Metrics `foresignal.federation.items{peer,result}`, `lag_seq{peer}`.
- `docs/OpenAPI.json`: schemas `FederationInfo`, `FederationItem`, `Provenance` (extend 109's), `Peer`, `PeerCreate`, `PeerSelection`, `CompactMessage` (documented CBOR keys); `ref` on `Record`, `RecordVersion`, `Change`; the operations above with `x-foresignal-scope` (`read` + federation capability noted in the description; ingest endpoints `ingest-key`). Update `AccessPolicy`. Regenerate Postman with BODIES for `createPeer`, `updatePeer`, `federationIngest`.
- MCP (025): `list_peers`, `peer_status` (admin).
- `docs/Operator-Guide.md` "Federation and edge relay": topology examples (home hub ⇄ cabin edge; two friends' instances), the licence rules (what never leaves), key pinning, edge profile memory notes, compact messages over mesh.
- `docs/Architecture.md`: a short "Federation" section (refs, chain, loop and dedup rules).

## Tests to write (do not run)

- `RecordRefTest`: format and parse round trip, encoding, long externalId hashing.
- `FederationFeedIT`: items carry provenance and licence; FORBIDDEN, LOCAL_ONLY, MLAT, private and exercise records excluded; token without `federation` → 403; purge horizon → 410; snapshot paging.
- `PeerSourceIT` (two Spring contexts are expensive: serve the remote side from WireMock fixtures recorded from `FederationFeedIT` output): JWS and pinned-kid verification; mapping to `peer-{id}` and `peer-{id}-trk`; `_foresignal` excluded from hashing (identical remote re-send makes no version); per-record licence via `originLicence`; tombstone propagation; reset → resync plus tombstoning of absent heads.
- `LoopAndDedupTest`: own instance in chain dropped; `maxHops`; the same origin over two peers yields one local record; a stale origin seq dropped; ownership transfer after peer deletion.
- `EdgeModeContextTest`: with `foresignal.mode=edge`, LLM, embedding, raster and export beans are absent and `features` reports them off.
- `FederationIngestIT`: ingest key auth, idempotency on `(origin_ref, origin_seq)`, JWS check, `sameOperator` LOCAL_ONLY rule; `FederationPushSinkIT`: offline buffering via the cursor and resume.
- `CompactMessageCodecTest`: extended keys round trip through the 084 codec; old 084 golden vectors still decode; ≤ 160 bytes with the drop order 9, 8 before 10–12; title truncation on a UTF-8 boundary; type 5 for intel events. `CompactOutboxTest`: priority order, TTL expiry, dedup and resend on resolve, byte budget.

## Acceptance criteria

- Instance B replicates a selected subset of A with provenance chains; a ring A→B→C→A produces no loop and no duplicates.
- Nothing from `opensky-states`, `acled`, `aisstream`, private sources or LOCAL_ONLY receivers is served to a peer.
- An edge node buffers while offline and catches up without loss.
- Alerts and intelligence events encode to compact messages within the byte budget; raw tracks never do.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 126, 129, 130, 131).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 131).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `113: Federation And Edge Relay`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/113-CR-Federation-And-Edge-Relay.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `113: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
