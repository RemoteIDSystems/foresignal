# 134 — Chronicle Network And Discovery

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/134-CR-Chronicle-Network-And-Discovery.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/133-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §3.1 (three memories), §12.6 (chronicle network), §2 (principles 9, 12, 14)
- `docs/Client-Contract.md` §11 (chronicles), §15 (chronicle network)
- Completion Reports of 098 (entity keys), 109 (instance signing key), 113 (federation peers, provenance, loop prevention), 116 (canonical events, evidence groups, originators), 118 (situations), 130 (principals, ACL, TLP), 131 (chronicles), 132 (reports, signed packages, received material)

## Objective

Turn independent chronicles into a collaborative intelligence network. Foresignal discovers when chronicles relate to each other (on this instance, including other analysts' private ones, and across federated instances) by comparing **privacy-preserving fingerprints** of what they captured, never their contents. It reports relationships only as far as each owner's **discoverability** setting and TLP allow, lets analysts **request access** without revealing their own work, counts shared evidence **once** across chronicles, and detects **circular reporting** so an analysis that comes back through other analysts is never mistaken for independent confirmation.

## Principles (binding)

- **Fingerprints, not content.** Discovery compares keyed hashes and sketches. Titles, owners, layers and captured data are disclosed only when the viewer may see the chronicle or its owner chose to reveal them.
- **The owner decides; TLP caps it.** Discoverability is `none` by default. TLP RED chronicles are never discoverable; AMBER+STRICT at most within the instance.
- **No probing.** Relationships are reported only above a minimum overlap, discovery runs on chronicles (not ad-hoc queries), and request rates are limited.
- **Evidence counts once.** Overlap and any combined view count global evidence keys and evidence groups (116), never records or chronicles.
- **Interpretations are not evidence.** Judgments that travel between analysts carry their provenance chain; circular chains never count as independent support.

## Tasks

### 1. Evidence keys and fingerprints (`ai.foresignal.chronicle.network`)

- **Global evidence keys** (stable across instances, derived from captured content): for canonical events with upstream ids, `ev:{family}:{originator}:{upstreamId}`; otherwise `cell:{family}:{h3 res 5}:{utc hour}`; for entities, `ent:{keyType}:{value}:{utc day}` (ICAO hex, MMSI, IMO, NORAD); area-time cells `at:{h3 res 4}:{utc day}`; topics `topic:{normalised keyword set hash}`.
- **Keyed hashing:** `HMAC-SHA256(salt, key)`, with a per-instance discovery salt (generated, stored with the secret key material, rotated yearly) for local discovery, and a per-network salt for federated discovery (§4).
- **Sketches** per chronicle and key type: MinHash with 128 permutations (seeded, deterministic) plus the exact set size; recomputed incrementally as capture proceeds (at most hourly) and on stop. Stored in `chronicle_fingerprint(chronicle_id, key_type, salt_id, minhash bytea, size, updated_at)`. The hashed key sets themselves are kept only for the instance's own chronicles (`chronicle_key(chronicle_id, key_type, hkey, first_seen)`), never sent anywhere.

### 2. Discoverability and disclosure

- Chronicle field `discoverability` (none | instance | network), plus `reveal_title` and `reveal_owner` (bools, default false). Validation against TLP: RED → none; AMBER+STRICT → none | instance; AMBER, GREEN, CLEAR → any. Changing TLP to a stricter label lowers discoverability automatically (logged, owner notified).
- **Disclosure levels** for a relationship between a viewer's chronicle A and chronicle B:
  - viewer can see B (owner or ACL): full details, including the shared canonical events and entities (computed exactly from both key sets);
  - viewer cannot see B, B is discoverable to the viewer: an **opaque link**: `{linkId, strength: low | medium | high, dimensions: [evidence, entities, area-time, topics], timeOverlap: none | partial | full, ownerHandle?, title?}` (the last two only if revealed); no counts finer than the strength band;
  - otherwise nothing.
- **Minimum overlap:** a link exists only when the estimated shared evidence keys ≥ `network.minSharedKeys` (default 5) **and** the containment of the smaller chronicle ≥ 0.05; strength bands from estimated Jaccard (low < 0.1 ≤ medium < 0.3 ≤ high).
- **Anti-probing:** discovery runs only for chronicles that have captured ≥ 50 evidence keys; a principal gets at most 20 new opaque links per day; creating and deleting chronicles repeatedly within 24 h does not trigger fresh discovery.

### 3. Discovery and the link graph

- `ChronicleDiscoveryJob` (nightly, and when a chronicle's sketch changes by > 10%): LSH banding over MinHash signatures (16 bands × 8 rows) to find candidate pairs among the instance's chronicles and received peer sketches; estimate Jaccard and containment per key type; for local pairs above threshold compute exact overlap from the key sets.
- `chronicle_link`: `id`, `a`, `b` (local chronicle ids or `remote:{instanceId}/chronicle:{sketchId}`), `scores` jsonb (per key type: Jaccard, containment, shared estimate), `temporal` (a-precedes-b | b-precedes-a | concurrent, from capture spans and first-seen of shared keys), `shared_situations` (local ids), `strength`, `first_found`, `last_updated`, `status` (active | dismissed by either owner).
- **Clusters:** connected components of `medium`+ links → `chronicle_cluster` (id, members, updated). A viewer sees a cluster only as far as their disclosure allows.
- **Combined timeline:** `GET /v1/chronicle-network/clusters/{id}/timeline` (read) merges the visible members' threads, **deduplicated by global evidence key**, each item listing which visible chronicles hold it; opaque members contribute nothing.

### 4. Across instances

- Peers (113) that both enable `network.federated=true` share a **network salt** agreed out of band by their admins (`PUT /v1/chronicle-network/salts {networkId, saltRef}`, admin; the salt is stored with secret material and never exported) and exchange, for `network`-discoverable chronicles only, signed sketch packages: `{sketchId (random per chronicle per salt), instanceId, keyTypes, minhash, sizeBand (log2 bucket), captureSpan (week granularity), revealed title or owner if allowed, tlp}` via the 113 peer channel (`POST /v1/federation/chronicle-sketches`, peer credential), signed with the instance key (109).
- Received sketches are stored per peer, expire after 30 days unless refreshed, and take part in discovery like local ones (opaque links only).
- Salt rotation every 90 days (new sketch ids), so long-term linkage requires continued consent.

### 5. Access requests

- `POST /v1/chronicle-network/links/{linkId}/request-access {message, offerShare?: chronicleId}` (read): sends the owner of the other chronicle (local principal, or through the peer channel for remote ones) a request containing only the requester's handle, the message and, if the requester chooses, an offer to share their own chronicle. The owner may grant (130 ACL on this instance; for remote requesters, a 132 package of a chronicle extract or report), decline, or ignore; the requester is notified of grant or decline only.
- Rate limits: 10 requests per principal per day; repeated requests to a declining owner are suppressed for 30 days.

### 6. Circular reporting

- Analytic products that cross analysts or instances (reports, received packages, imported judgments, hypothesis evidence items citing reports) carry a **provenance chain**: the ordered list of `(instance, principal, productRef, manifestId)` from origin to now (132 packages already carry authors and forwarders).
- `CircularReportingDetector`: when a judgment, report or evidence item used as support for a hypothesis (125) or a confidence assessment has a provenance chain that includes the consuming analyst's own instance-and-principal earlier, or two supporting items share an origin, mark them `CIRCULAR_REPORTING` and treat them as one evidence group (116 rules); the 125 updater and confidence assessor count them once, and the hypothesis shows the caveat.

### 7. API, MCP, SSE, docs

- `GET /v1/chronicles/{id}/related` (read; links visible to the caller with their disclosure level), `PUT /v1/chronicles/{id}/discoverability` (owner), `POST /v1/chronicle-network/links/{linkId}/dismiss` (owner of either side), `GET /v1/chronicle-network/clusters` and `/{id}` (read), `GET /v1/chronicle-network/clusters/{id}/timeline` (read), `POST /v1/chronicle-network/links/{linkId}/request-access`, `GET /v1/chronicle-network/requests` (read; incoming and outgoing), `POST /v1/chronicle-network/requests/{id}/grant|decline` (owner), `PUT /v1/chronicle-network/salts` (admin), `POST /v1/federation/chronicle-sketches` (peer credential).
- MCP: `get_related_chronicles` (read).
- SSE `me` topic: `chronicle.link.found`, `chronicle.access.requested`, `chronicle.access.granted|declined`.
- `docs/OpenAPI.json`: `ChronicleLink`, `OpaqueChronicleLink`, `ChronicleCluster`, `ClusterTimelineItem`, `Discoverability`, `AccessRequest`, `ChronicleSketchPackage`; tag `Chronicles`; `x-foresignal-scope`.
- AccessPolicy rows. Problem codes: `UNKNOWN_LINK`, `UNKNOWN_CLUSTER`, `UNKNOWN_ACCESS_REQUEST` (404), `DISCOVERABILITY_NOT_ALLOWED` (409, with the TLP rule), `REQUEST_RATE_LIMITED` (429).
- `docs/Operator-Guide.md` "Chronicle network": what is shared (sketches, never content), discoverability and TLP, federated salts, access requests, circular reporting.

## Tests to write (do not run)

- `EvidenceKeyTest`: key derivation per family and entity type is stable across instances for the same upstream content.
- `MinHashSketchTest`: deterministic signatures; Jaccard estimates within tolerance on planted overlaps; incremental update equals recompute.
- `DisclosureTest`: full, opaque and none disclosure per visibility, discoverability and TLP; RED never discoverable; title and owner only when revealed; strength bands without finer counts.
- `AntiProbingTest`: minimum shared keys and size, daily link cap, create-and-delete churn does not trigger discovery.
- `ChronicleDiscoveryIT`: two analysts' chronicles over the same war overlap → opaque link for each other; owners see full shared events; cluster timeline deduplicates shared evidence.
- `FederatedSketchIT`: signed sketch exchange between two instances with a shared network salt yields an opaque cross-instance link; a different salt yields none; expiry and rotation.
- `AccessRequestIT`: request reveals only handle and message; grant applies an ACL locally or sends a package remotely; rate limits.
- `CircularReportingTest`: a report that returns through another analyst is marked circular and counted once by the hypothesis updater.
- `ChronicleNetworkControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- Related chronicles are discovered locally and across consenting instances from fingerprints alone, with disclosure bounded by visibility, discoverability and TLP.
- Shared evidence is counted once in every combined view, and circular reporting is detected and never counted as independent support.
- Analysts can ask for access without revealing their own work.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/design-history/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `134: Chronicle Network And Discovery`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/134-CR-Chronicle-Network-And-Discovery.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `134: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
