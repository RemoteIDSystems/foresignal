# 134-CR-Chronicle-Network-And-Discovery

| | |
|---|---|
| Prompt | 134-Chronicle-Network-And-Discovery.md |
| Status | Complete (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**) |
| Implementation commit | `c19675840d0bee2ff6ea15acea22f065eef80c08` |
| Date | 2026-10-17 |

## Summary

This prompt turns independent chronicles into a network. Foresignal discovers related chronicles from
**privacy-preserving fingerprints** of what they captured, never their contents: on this instance (other analysts'
private chronicles included) and across federated instances that agreed a network salt. What a viewer learns is bounded
by visibility, the owner's **discoverability** and TLP. Analysts can request access without revealing their own work,
shared evidence counts once, and **circular reporting** is detected so a judgment that comes back through other
analysts is never independent support. The code lives in the new package `ai.foresignal.chronicle.network`, plus a
controller, an MCP tool and two classes in `ai.foresignal.exchange`. It compiles. **The tests are written but were not
run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report is measured.

- **Evidence keys and fingerprints:**
  - `EvidenceKeys` derives global keys from captured content only: `ev:{family}:{originator}:{upstreamId}` (116
    canonical event with an upstream id), else `cell:{family}:{h3 res 5}:{utc hour}`; `ent:{keyType}:{value}:{utc day}`
    for `icao24`, `mmsi`, `imo` and `norad`; `at:{h3 res 4}:{utc day}`; `topic:{first 32 hex of SHA-256 of the
    normalized keyword set}`.
  - `ChronicleKeyExtractor` reads the chronicle's own `chronicle_version` rows by seq. Evidence keys come from
    `observation_link` joined to `canonical_event` (a merged event refers to its survivor). Entity keys come from 098
    `EntityKeys.extract`. Topic keys come from the chronicle's attention topics and the `topicIds` of captured news
    articles. Rows of private sources give no keys.
  - `KeyedHasher`: `HMAC-SHA256(salt, key)`, truncated to 64 bits. `NetworkSalts` keeps the instance salt (32 random
    bytes, rotated yearly) and per-network salts (90 days) encrypted with `CredentialCipher` in `chronicle_salt`. Salts
    are never exported.
  - `MinHashSketch`: 128 seeded, deterministic permutations (the SplitMix64 finalizer over value XOR a fixed seed
    sequence). It supports incremental `with` (equal to recompute), Jaccard, shared and containment estimates, 16×8 LSH
    band keys, bytes and base64, and log2 size bands.
  - `ChronicleSketcher` refreshes key sets (`chronicle_key`) and fingerprints (`chronicle_fingerprint`) incrementally
    per chronicle and salt. Network salts apply only while the effective discoverability is `network`. It marks
    discovery due on a key-total or slot change above 10% (`chronicle_sketch_state`). It runs hourly and when a
    chronicle stops (`ChronicleLifecycleListener`).
- **Discoverability and disclosure:**
  - Chronicle columns `discoverability`, `reveal_title`, `reveal_owner` and `discoverability_updated_at` (nullable,
    default `none`/false).
  - `DiscoverabilityLevel` caps by TLP: RED → none; AMBER+STRICT → at most instance. Too much is
    `409 DISCOVERABILITY_NOT_ALLOWED` with `rule`.
  - A stricter TLP label lowers discoverability through the new SPI `collab/TlpChangeListener`, called by
    `ShareService.replace` and `ChronicleService.patch`. The change is logged and the owner gets the inbox notice
    `chronicle.discoverability_lowered`.
  - `Disclosure` and `ChronicleLinkViews` decide full, opaque or nothing. Opaque links show only the strength band,
    dimensions, time overlap and the revealed title or handle. The daily cap of 20 new opaque links is kept in
    `chronicle_link_disclosure`.
  - `AntiProbing`: at least 50 keys before discovery; churn hold; 10 access requests per day. `LinkScores`: at least
    `network.minSharedKeys` (5) shared keys and a containment of 0.05; strength bands at 0.1 and 0.3.
- **Discovery** (`ChronicleDiscoveryJob`, nightly 03:30 UTC and after the hourly refresh when due):
  - LSH candidates per key type.
  - Exact scoring of local pairs from the key sets (`ChronicleKeyStore.shared`); estimated scoring of local × remote
    pairs from signatures and size bands.
  - `chronicle_link` rows with scores, dimensions, strength, temporal relation (first-seen of shared keys; ISO weeks for
    remote), time overlap, shared situations and shared key count. Links that no longer pass are removed; dismissed
    links are kept.
  - `chronicle_cluster` rows (union-find over medium+ active links, ids kept by greatest overlap).
  - `chronicle.link.found` to each local owner at that owner's disclosure level (`ChronicleNetworkNotices`).
- **Combined timeline** (`ChronicleNetworkService.timeline`): the visible members' evidence keys
  (`ChronicleKeyStore.heldEvidence`, grouped by keyed hash) and thread items (deduplicated by type and ref), each with
  `heldBy`. Opaque members contribute nothing.
- **Federation** (`FederatedSketches`):
  - A daily signed `ChronicleSketchBatch` per current network salt goes to every enabled peer with an exchange key
    (`POST {peer}/v1/federation/chronicle-sketches`, Bearer exchange key, `Foresignal-Signature` from `JwsSigner`).
    It carries a random sketch id per chronicle per salt, MinHash, size bands, ISO-week span, TLP, the revealed title
    or handle, and withdrawals.
  - Receiving verifies against the pinned key, else the JWKS (`PackageVerifier`). It requires `network.federated` and
    a current salt for the network, rejects RED and AMBER+STRICT sketches, stores sketches for 30 days per peer
    (`chronicle_remote_sketch`), and applies withdrawals.
  - Installing a salt (`PUT /v1/chronicle-network/salts`) retires the old one and forgets its sketches.
- **Access requests** (`ChronicleAccessService`, `chronicle_access_request`):
  - A request reveals only the handle, the message and an offer (a chronicle the requester owns). It is limited to
    10 per principal per day; repeats to an owner who declined within 30 days are suppressed.
  - A local grant is a 130 share (viewer or commenter, TLP checked), plus the offered chronicle shared back. A remote
    grant is a 132 package (`chronicle-extract`, or a `report` drafted with `chronicle-recap` and published), relayed
    with the decision. A decline is told as a status only.
  - Remote requests and decisions travel on the sketch channel (`accessRequests`, `accessDecisions`).
- **Circular reporting:**
  - `ProvenanceChains` builds chains for `remote:` (the package chain), `report:` (cited reports, received material
    and evidence items, then the owner) and `evidence-item:`.
  - `CircularReportingDetector` assigns the origin key `origin:{instanceId}/{productRef}` and flags a chain that names
    the consumer before its last hop (importers aside).
  - `HypothesisService.evidenceKey` uses the detector, so shared origins attach to one ACH row. The row gets the
    `CIRCULAR_REPORTING` caveat and stores `provenance_chain` and `caveats` (new columns on `ach_evidence`).
  - `addEvidence` gains a principal overload, used by REST and MCP.
  - `RevisionWriter` counts indicators of one origin once. `ConfidenceAssessor` 1.2.0 reports `circularReporting` and
    `circularSupports`.
  - `ReportDrafter` adds a `CIRCULAR_REPORTING` coverage caveat.
  - Report packages now carry the cited chains (`ExchangeService.reportChain`).
- **API, MCP, SSE:**
  - `api/ChronicleNetworkController` covers the 12 operations; AccessPolicy rows; 10 Problem codes.
  - MCP `get_related_chronicles` (read).
  - `me` events `chronicle.link.found` and `chronicle.access.requested|granted|declined` (`MeEvents`).
  - App settings `network.minSharedKeys` and `network.federated`; property `foresignal.chronicles.network-scheduled`
    (off in tests).
- **Spec and docs:**
  - `docs/OpenAPI.json`: the 12 operations are `implemented` (removed from `pending-operations.txt`, 57 lines left),
    with Problem-code descriptions and the additive reconciliations listed under Deviations, plus a changelog entry.
  - Postman regenerated ("844 requests … covers all ops: True") with four example bodies, capture scripts and variables.
  - `docs/Client-Contract.md` (§15 as built, intro, §2 and §17), `docs/Agent-Guide.md` (me topic, tool row,
    "Chronicle network" subsection, vocabulary, scopes, `add_evidence`) and `docs/Operator-Guide.md` (new "Chronicle
    network (prompt 134)", and sentences updated in the 125, 130, 131, 132 and 133 sections) were each read end to end
    and written whole by script.

## Files created / modified

**Created (main):**
- `chronicle/network/`: `package-info`, `NetworkVocabulary`, `EvidenceKeys`, `KeyedHasher`, `MinHashSketch`,
  `DiscoverabilityLevel`, `Disclosure`, `LinkScores`, `AntiProbing`, `IsoWeeks`, `NetworkSalts`,
  `ChronicleKeyExtractor`, `ChronicleKeyStore`, `ChronicleSketcher`, `ChronicleLinkViews`, `ChronicleNetworkNotices`,
  `ChronicleDiscoveryJob`, `FederatedSketches`, `ChronicleAccessService`, `ChronicleNetworkService`,
  `ChronicleNetworkJobs`.
- Entities and repositories: `ChronicleKeyEntity`, `ChronicleFingerprintEntity`, `ChronicleSketchStateEntity`,
  `ChronicleLinkDisclosureEntity`, `ChronicleSaltEntity` and repo, `ChronicleChurnEntity` and repo,
  `ChronicleLinkEntity` and repo, `ChronicleClusterEntity` and repo, `ChronicleRemoteSketchEntity` and repo,
  `ChronicleAccessRequestEntity` and repo.
- `chronicle/ChronicleLifecycleListener`, `collab/TlpChangeListener`, `exchange/ProvenanceChains`,
  `exchange/CircularReportingDetector`, `api/ChronicleNetworkController`, `mcp/tools/GetRelatedChroniclesTool`.

**Modified (main):** `chronicle/ChronicleEntity`, `ChronicleService`; `collab/MeEvents`, `ShareService`;
`exchange/ExchangeService`, `ExchangeInbox`; `hypothesis/AchEvidenceEntity`, `HypothesisService`, `HypothesisViews`,
`ConfidenceAssessor`, `RevisionWriter`; `report/ReportDrafter`; `api/HypothesisController`;
`mcp/tools/AddEvidenceTool`; `security/AccessPolicy`; `settings/SettingKeys`; `api/error/ErrorCodes`;
`application.yml`.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Client-Contract.md`, `docs/Agent-Guide.md`,
`docs/Operator-Guide.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−12).

**Tests:** the classes below and the helper `chronicle/network/NetworkItSupport` (built on `report/ReportItSupport`).
Modified: `stream/sse/EventSchemaConformanceTest` (the four `me` events), `hypothesis/ConfidenceAssessorTest` and
`news/CoverageInAnalysisIT` (assessor 1.2.0), test `application-test.yml` (`network-scheduled: false`).

## Deviations from the prompt

1. **`chronicle_key` has more columns than the prompt lists:** `salt_id`, so network-salt key sets can exist, and,
   under the instance salt only, `key_text` and `ref`, so full links and the timeline can name shared canonical events
   and entities exactly. Keyed hashes are 64-bit truncations of the HMAC.
2. **"Evidence keys" for the 50-key minimum** counts all global key types together; the `evidence` dimension is
   `ev:`/`cell:` only. Records with no 116 canonical event give no evidence key (no fallback cell is invented).
3. **Private-source rows give no keys at all**, not even under the local salt (principle 12).
4. **Temporal relation:** a side "precedes" when it saw at least twice as many shared keys more than an hour earlier;
   remote pairs compare ISO capture weeks.
5. **Churn rule:** an owner who deleted a chronicle less than 24 h after creating it, within the last 24 h, has their
   chronicles younger than 24 h held out of discovery (`chronicle_network_churn`).
6. **Federated recipients:** batches go to every enabled peer with an exchange key; `NetworkSaltInput` names no peers.
   `saltRef` is taken as the salt value (no secret-reference store exists).
7. **Remote access requests and decisions ride the sketch channel** (`accessRequests` and `accessDecisions` added to
   `ChronicleSketchBatch`, additive), since the spec has no other peer endpoint. A remote offer shows as
   `offerShare.chronicleId = remote:{instanceId}`.
8. **Offer on grant:** a local requester's offered chronicle is shared back with the owner as viewer when the request is
   granted; a TLP refusal is logged, not thrown.
9. **Remote `report` grants** draft a `chronicle-recap` report and publish it as the owner before packaging.
10. **Extra Problem codes** beyond the five named: `ACCESS_REQUEST_DECIDED`, `CHRONICLE_ALREADY_VISIBLE`,
    `FEDERATED_DISCOVERY_DISABLED`, `NETWORK_SALT_UNKNOWN` (409) and `SKETCH_BATCH_INVALID` (400).
11. **132 loop prevention changed:** `ExchangeInbox` (and forwarding) count only the transport hops of the package's own
    product (origin, forwarders, importers of the same product). Otherwise a report citing this instance's work, now
    carrying the cited chain, would be refused as `PACKAGE_LOOP` and circular reporting could not be detected.
12. **Circular items still count once, not zero:** the shared origin is one evidence group (as the prompt says); a
    support that returned to its author is marked and grouped, not discarded. Confidence gains a reported count, no
    extra step-down.
13. **Inbox-only notice** `chronicle.discoverability_lowered` (not a `me` SSE event; the spec lists four `me` events).
14. **Spec reconciliations (additive):** `ChronicleSketchPackage.minhash` said "129 MinHash values" and now says 128;
    `AchEvidence.caveats` added and `provenanceChain` marked read-only; the confidence basis gains
    `newsCoverage`, `circularReporting` and `circularSupports`; 400/409/413 descriptions with codes.
15. **Edited by script:** the spec, as in earlier prompts; the three documents were read end to end and written whole
    by script.

## Issues found / open questions

**Owner decisions:**
- (a) The loop-prevention change in 132 (Deviation 11).
- (b) Whether circular support should carry zero weight rather than count once (Deviation 12).
- (c) The churn rule (Deviation 5) and counting all key types toward the 50-key minimum (Deviation 2).
- (d) Peer selection for batches, and `saltRef` as a value (Deviation 6).
- (e) Access requests on the sketch channel and share-back of offers (Deviations 7, 8).
- (f) Excluding private-source rows from every key set (Deviation 3).

**Known limits:**
- Discovery loads all fingerprints of a salt into memory; LSH buckets over 500 members are skipped.
- The thread part of a cluster timeline reads up to 5,000 items per visible member.
- `ChronicleKeyExtractor` reads at most 40 × 5,000 rows per chronicle and salt per pass; a backlog catches up over
  passes.
- Topic keys of a chronicle's linked topics are re-derived on every pass (deduplicated by the unique key); a changed
  topic adds keys and never removes old ones.

**Schema for prompt 144:**
- Hibernate tables: `chronicle_key` (`chronicle_key_unique`, `chronicle_key_hkey_idx`), `chronicle_fingerprint`,
  `chronicle_sketch_state`, `chronicle_link_disclosure`, `chronicle_salt`, `chronicle_network_churn`, `chronicle_link`,
  `chronicle_cluster`, `chronicle_remote_sketch` and `chronicle_access_request`.
- New columns: `chronicle.discoverability`, `reveal_title`, `reveal_owner` and `discoverability_updated_at`;
  `ach_evidence.provenance_chain` and `caveats` (jsonb).
- New app settings: `network.minSharedKeys` and `network.federated`. New property:
  `foresignal.chronicles.network-scheduled`.

**Guards not run:**
- Not run, under the no-tests directive: the US English guard, `lintOpenApi` and every contract test
  (`AccessPolicySyncTest`, `OpenApiContractIT`, `EventSchemaConformanceTest`, `RestrictedReadCoverageTest`).
- New prose and code were checked by grep for British spellings; one hit ("travelled") was corrected.

**Risks for the owner's first test run:**
- The ITs insert `canonical_event` and `observation_link` rows directly (`NetworkItSupport.evidence`) instead of running
  the 116 resolver.
- `FederatedSketchIT` signs batches with a recorded peer key (`ExchangeTestPackages`) and builds the "peer's" sketch from
  this instance's own network fingerprint.
- `AccessRequestIT.remoteGrant` delivers to `127.0.0.1:9`; the package is created whatever the delivery outcome.

**External data:** none was fetched. No file, page or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `chronicle/network/EvidenceKeyTest` | `ev:`, `cell:`, `ent:`, `at:` and `topic:` keys stable across instances; allowed entity types; keyed hashing per salt |
| `chronicle/network/MinHashSketchTest` | Deterministic signatures and round trips; Jaccard within tolerance on planted overlaps; incremental update equals recompute; size bands |
| `chronicle/network/DisclosureTest` | Full, opaque and none per visibility, discoverability and TLP; RED never discoverable; title and owner only when revealed; strength bands without counts; time overlap (DISC-1) |
| `chronicle/network/AntiProbingTest` | Minimum shared keys and containment, the 50-key minimum, churn hold, the daily opaque-link cap, the 10% trigger |
| `chronicle/network/ChronicleDiscoveryIT` | Two analysts over the same war get opaque links; full link with shared events once shared; cluster timeline deduplicated (60, not 120); RED lowers discoverability; deletion and churn (DISC-2) |
| `chronicle/network/FederatedSketchIT` | Signed exchange with a shared salt gives an opaque cross-instance link; a different salt gives none; refusals; expiry, rotation with new sketch ids, withdrawal (DISC-1) |
| `chronicle/network/AccessRequestIT` | A request shows only handle, message and offer; local grant with share-back; decline, suppression and rate limit; remote grant as a package |
| `chronicle/network/CircularReportingTest` | Chain rules and shared origins; a report returning through another analyst is `CIRCULAR_REPORTING`, one ACH row and one updater revision; assessor basis (DISC-3) |
| `chronicle/network/ChronicleNetworkControllersIT` | Endpoints, scopes, ownership, Problem codes, salts and the MCP tool shape |
| `stream/sse/EventSchemaConformanceTest` (added cases) | `chronicle.link.found` and `chronicle.access.*` payloads |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: BUILD SUCCESSFUL.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **135–137 (social):** accounts and narratives that cite reports can use `ProvenanceChains` and
  `CircularReportingDetector`; `HypothesisService.addEvidence(…, principalId)` passes the consumer.
- **New peer messages:** the sketch channel (`FederatedSketches.relay`) carries signed JSON batches to a peer's exchange
  key.
- **144 (Flyway):** capture the ten tables and the new columns above.
- **Tests:** use `NetworkItSupport` (`active`, `war`, `evidence`, `discoverable`, `shareWith`, `sketchAndDiscover`).

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `c19675840d0bee2ff6ea15acea22f065eef80c08`
