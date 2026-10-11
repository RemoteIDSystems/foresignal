# 136 — Narratives Claims And Media

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/136-CR-Narratives-Claims-And-Media.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/135-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principles 1, 2, 9, 14, 16), §4.5 (evidence identity), §12.5 (news coverage), §12.6 (circular reporting), §12.7 (social signals)
- `docs/Client-Contract.md` §2–§6 (object references, envelopes), §16 (social signals)
- Completion Reports of 026 (LLM roles and budgets), 027 (embeddings), 036 (places, gazetteer), 040 (regions), 043 (media store, ImageGuard), 098 (entities), 116 (canonical events, evidence groups, prompt templates, manifests), 118 (situations and linker), 119 (findings), 125 (evidence items, source reliability), 129 (object graph), 133 (news stories, articles), 134 (provenance chains), 135 (social posts, topics, series)
- Libraries (verify licence and version): PDQ image hashing (Meta ThreatExchange `pdq`, BSD; port the reference algorithm to Java with an origin header and a `THIRD_PARTY_NOTICES.md` entry, golden-tested against the reference test vectors), Lingua (from 135)

## Objective

Turn social posts into the units analysts reason with. Group posts that say the same thing into **narratives**; extract structured **claims** (what, where, when, who) from them; fingerprint shared **images** so reused or recycled media is recognised; trace **propagation** across reposts, platforms and news; and report **coordination indicators**. Every narrative is a 116 canonical event whose independent evidence groups are counted by origin, so 50,000 posts that descend from three reports count as three.

## Principles (binding)

- **Claims are reports, not facts.** A claim's status describes its relationship to independent evidence (137), never its truth by assertion. Nothing in this prompt marks a claim true or false.
- **Earliest observed, not originator.** Foresignal records the earliest post it observed for a narrative, on each platform; it never asserts who started a narrative.
- **Indicators, not labels.** Coordination is reported as statistics on a narrative (synchronised timing, near-identical text, account-age skew), never as a label on an account ("bot", "troll", "state actor").
- **Deterministic first.** Narrative clustering, place and entity resolution, fingerprints and propagation are deterministic. The LLM may extract claims for large narratives within its budget, using a versioned prompt template (116), and never sets a status or a count.

## Tasks

### 1. Narratives (`ai.foresignal.social.narrative`, `NarrativeClusterer` 1.0.0)

- 116 family **`social-narrative`** in `canonical-rules.json`: a post joins a narrative when (a) it reposts or quotes a member, or (b) its text embedding (027) has cosine ≥ 0.85 with the narrative centroid, or (without embeddings) its normalised-token MinHash Jaccard ≥ 0.6, **and** it is within 72 h of the narrative's latest member **and** shares a topic, a resolved place or an entity. Links and identical media fingerprints also join posts (`linkReason`: `repost | quote | text | link | media`).
- Object type **`narrative`** (129 envelope): `id`, `title` (deterministic: the most central member's text, truncated to 140 chars, then the claim summary once 2 or more claims exist), `canonicalEventId`, `topics`, `placeRefs`, `entityRefs`, `regionCodes`, `languages`, `platforms` (`{platform, posts, uniqueAuthors, firstObservedAt, earliestPostRef}`), `firstObservedAt`, `lastActivityAt`, `postCount`, `uniqueAuthors`, `evidenceGroupCount` (independent origins), `originConcentration` (share of posts descending from the top three evidence groups), `velocity` (current hourly ratio from 135 baselines), `mediaFingerprints`, `newsStoryIds` (133 stories linked by URL or text), `situationIds`, `status` (from 137: `unassessed | corroborated | unobserved | contradicted | mixed | insufficient-coverage`), `coordination` (indicators below), `state` (`emerging | active | fading | dormant`, by velocity and age), `tlp` (default `CLEAR` for public content; derived objects follow 130).
- Evidence groups: members that repost, quote or link the same original post, or share a media fingerprint first seen in one post, are one evidence group rooted at that post's originator; independent authors posting their own text and media form separate groups. Narratives merge and split like situations (118 rules), with history.

### 2. Claims (`ClaimExtractor` 1.0.0, deterministic; `claim-extraction` LLM role, optional)

- **Deterministic extraction** per post and aggregated per narrative: event-type lexicons per language (`src/main/resources/intel/social/claim-lexicon.json`: explosion, strike, drone, missile, gunfire, fire, smoke, flood, earthquake, outage, blackout, internet-down, closure, blockade, seizure, evacuation, protest, collapse, spill, crash, …, each mapped to a 118 signal family), negation and hedge markers ("reportedly", "unconfirmed", "denied"), place resolution (036 gazetteer and 040 region names with multilingual aliases, longest match, confidence by ambiguity and population), entity resolution (098 names and identifiers: vessel names and IMO/MMSI, airports, ports, facilities from 104), time expressions relative to `createdAt`.
- **Claim** object: `id`, `narrativeId`, `type` (lexicon id), `signalFamily`, `polarity` (`asserted | denied | questioned`), `hedged`, `place` (`{ref?, name, geom?, precision: point | locality | admin1 | country, confidence}`), `entityRefs`, `eventTime` (`{from, to, precision}`), `actor` (organisation name if stated), `supportCount` (posts), `evidenceGroupCount`, `firstObservedAt`, `extraction` (`deterministic | llm`), `manifestId`, `confidence` (0–1, extraction confidence only), `status` (set by 137).
- **LLM extraction** (026 role `claim-extraction`, budgeted per day, off without a provider): for narratives with ≥ `social.llmClaimMinPosts` (default 50) posts or ≥ 5 evidence groups, a versioned template receives the 20 most central member texts (data block, never instructions) and returns claim tuples in a JSON schema; outputs are validated against the lexicon and gazetteer (unknown places are kept as names without geometry) and stored with `extraction = llm`.
- Claims are 125 evidence items (`source = social-claim`) with Admiralty grades: source reliability `F` by default (`E` after an account or channel has ≥ 10 claims assessed by 137 with ≥ 70% corroborated, `D` at ≥ 85%; operator overrides per account or channel), information credibility from 137's status (`6` unassessed, `2` corroborated, `5` unobserved, `4` contradicted).

### 3. Media fingerprints (`MediaFingerprinter` 1.0.0)

- For each post image (and video thumbnail) URL: fetch through 043's `ImageGuard` (size, type and SSRF limits), compute PDQ (256-bit hash and quality score), discard the image unless `social.keepMedia` is on (default off; then stored content-addressed in 043 under the post's retention). Fingerprints with quality < 50 are kept but not matched.
- `media_fingerprint` (`id`, `pdq`, `quality`, `firstSeenAt`, `firstPostRef`, `useCount`, `platforms`, `narrativeIds`) and `media_use` (`fingerprintId`, `postRef`, `seenAt`). Matching: Hamming distance ≤ 31 (PDQ's recommended threshold), via a BK-tree or multi-index hashing over 16-bit chunks.
- **Reuse detection:** a fingerprint attached to a new narrative whose `firstSeenAt` is ≥ 7 days earlier, or attached to an earlier narrative in a different place, yields a `media-reuse` finding (119 table) with both narratives, dates and places. The limit is stated with every finding: Foresignal only knows media it has fingerprinted (`REFERENCE_WINDOW` caveat with the fingerprint store's start date).
- `POST /v1/social/media/match` lets an analyst upload an image (multipart, through `ImageGuard`) or submit a PDQ hex string and get matches with first-seen dates and narratives.

### 4. Propagation (`PropagationTracer` 1.0.0)

- Per narrative, a directed graph: nodes are posts (grouped by evidence group when large), news articles (133) and official sources (records from agency adapters cited by link); edges `repost | quote | reply | link | text-match | media-match | cross-platform` with time deltas. Cross-platform edges come from shared URLs, shared media fingerprints or near-identical text (MinHash Jaccard ≥ 0.8) across platforms.
- Outputs: earliest observed post per platform, first news pickup (133 story linked by URL or text), the **descent share** (posts and articles reachable from the top three evidence-group roots), cross-platform order, and the median repost delay. Large graphs are summarised to ≤ 500 nodes by collapsing evidence groups; the full edge list is kept for the narrative's lifetime.
- Feeds 134: social narratives and the analytic products that cite them carry provenance chains, so a claim that returns through news, reports or other analysts is recognised as circular.

### 5. Coordination indicators (`CoordinationDetector` 1.0.0)

- Per narrative and window: **synchrony** (share of distinct authors posting near-identical text within 60 s windows, against a shuffled-time null of the same narrative), **text duplication** (largest near-duplicate cluster share across distinct authors), **account-age skew** (share of authors with `accountCreatedAt` within 30 days, where the platform provides it, against the platform's topic baseline), **co-repost bursts** (author pairs reposting the same ≥ 5 posts within 10 min of each other, against a degree-preserving null).
- `coordination` block on the narrative: each indicator with value, null expectation and p-value; a `social-coordination` finding when ≥ 2 indicators have p < 0.01 after Benjamini–Hochberg across the day's narratives. Wording: "coordinated-amplification indicators present", never about specific accounts.

### 6. API, MCP, SSE, spec, docs

- `GET /v1/social/narratives?topic&situationId&place&status&state&platform&since&cursor` (read), `GET /v1/social/narratives/{narrativeId}` (read; envelope with claims, platforms, media, coordination, linked stories and situations), `GET /v1/social/narratives/{narrativeId}/posts?platform&evidenceGroup&cursor` (read), `GET /v1/social/narratives/{narrativeId}/propagation` (read), `GET /v1/social/narratives/{narrativeId}/timeline?interval` (read; posts and unique authors per platform per interval), `POST /v1/social/narratives/{narrativeId}/merge {narrativeIds}` and `POST /v1/social/narratives/{narrativeId}/split {postRefs}` (agent).
- `GET /v1/social/claims?narrativeId&situationId&type&status&bbox&since&cursor` (read), `GET /v1/social/claims/{claimId}` (read).
- `GET /v1/social/media/{fingerprintId}` (read), `POST /v1/social/media/match` (read).
- Narratives and claims are 129 object types (`narrative`, `claim`) with envelopes, relations, annotations and verdicts; workspaces and chronicles can include them.
- MCP: `list_narratives`, `get_narrative`, `get_claims`, `match_media` (read).
- SSE topic `social`: `narrative.emerged`, `narrative.updated` (state or counts crossed a band), `claim.extracted`, `media.reuse`, `coordination.detected`.
- `docs/OpenAPI.json`: as specified there (`Narrative`, `NarrativeSummary`, `Claim`, `MediaFingerprint`, `MediaMatch`, `PropagationGraph`, `CoordinationIndicators`); status implemented; pending list updated.
- `docs/Operator-Guide.md` "Narratives and claims": reading narratives, origin concentration and descent share, what reuse findings can and cannot show, coordination indicators and their limits.
- AccessPolicy rows. Problem codes: `UNKNOWN_NARRATIVE`, `UNKNOWN_CLAIM`, `UNKNOWN_FINGERPRINT` (404), `IMAGE_REJECTED` (415, from ImageGuard).

## Tests to write (do not run)

- `NarrativeClustererTest`: reposts, quotes, text, link and media joins; 72 h window; merge and split with history; evidence groups by origin (a planted 10,000-repost cascade from 3 posts gives 3 groups and descent share 1.0).
- `ClaimExtractorTest`: lexicon matches in English, Arabic, Hebrew, Persian and Russian fixtures; negation and hedges; place resolution and ambiguity; time expressions.
- `LlmClaimExtractionTest` (stubbed model): thresholds, budget refusal, schema validation, unknown places kept without geometry, prompt-injection text in posts stays data.
- `PdqTest`: Java port matches the reference test vectors; quality scores; Hamming search over 100,000 hashes.
- `MediaReuseTest`: an image first seen 3 years earlier in another place yields a reuse finding with the `REFERENCE_WINDOW` caveat.
- `PropagationTracerTest`: cross-platform edges by URL, media and text; earliest observed per platform; news pickup; summarisation to 500 nodes.
- `CoordinationDetectorTest`: planted synchronous cluster detected; organic burst after a real event not flagged; BH control across narratives.
- `SocialEvidenceItemTest`: Admiralty grades from track records and operator overrides.
- `NarrativeControllersIT`: endpoints, scopes, envelopes, MCP shapes.

## Acceptance criteria

- Posts group into narratives whose independent origins are counted by evidence group, with earliest observed posts per platform and news pickup.
- Narratives carry structured claims with places, entities, times and extraction provenance; claims are usable as graded hypothesis evidence.
- Reused images are recognised within the fingerprint window, and coordination indicators are reported with their null comparisons.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 124, 139, 142, 143, 144) and in the sanctioned narrow runs of 007–009.
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 144).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `136: Narratives Claims And Media`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/136-CR-Narratives-Claims-And-Media.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `136: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
