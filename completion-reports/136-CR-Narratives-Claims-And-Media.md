# 136-CR-Narratives-Claims-And-Media

| | |
|---|---|
| Prompt | 136-Narratives-Claims-And-Media.md |
| Status | Partial (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**; the PDQ golden vectors and the LLM extraction path have never executed) |
| Implementation commit | `47ef2ebf8338511fe5f5b20d037e310e943e252a` |
| Date | 2026-10-18 |

## Summary

This prompt turns social posts into units analysts can reason with:

- **narratives**: posts that say the same thing, grouped as 116 canonical events of the new family `social-narrative`;
- structured **claims** (what, where, when, who), each a 125 evidence item with an Admiralty grade;
- **PDQ media fingerprints**, with `media-reuse` findings;
- **propagation graphs** across reposts, platforms and news;
- daily **coordination indicators** against null models, with `social-coordination` findings.

Claims are reports, not facts. Foresignal records the earliest observed post, never an originator. Coordination is a set
of indicators, never a label on an account.

The code is in the new package `ai.foresignal.social.narrative`, plus a controller, four MCP tools, a claim lexicon and
an LLM template. It compiles. **The tests are written but were not run** (owner directive 2026-10-17), so no test
result, coverage figure or guard result in this report is measured.

- **Clustering:**
  - `NarrativeClusterer` 1.0.0 is a `StoryFamilyHandler` for `social-narrative`. `EvidenceResolver` now takes a list
    of story handlers and dispatches through `handles(family)`; `NewsStoryResolver` handles `news-story`.
  - Join order: repost or quote, then shared canonical link, then shared image (URL, later fingerprint), then text.
    A text join needs a cosine of 0.85 (027 embeddings under one model) or a MinHash Jaccard of 0.6 against the
    central member and the latest 50, and a shared topic, place or entity. The window is 72 h after the latest member.
  - A post collected by two sources joins once.
  - Rules live in `intel/canonical-rules.json`.
- **Evidence groups:** `NarrativeGrouper` runs union-find over repost, quote, link, media and author origins.
  `originConcentration` is the share of posts in the three largest groups.
- **Narrative upkeep:** `NarrativeService` handles refresh, centrality, titles, centroid, velocity and state
  (`emerging`, `active`, `fading`, `dormant`), linked news stories and situations, history, and merge and split under
  the 118 rules. Text-derived regions are counted into 135's `social_coverage`.
- **Claims:**
  - `ClaimExtractor` 1.0.0 uses `intel/social/claim-lexicon.json`: 23 types in 10 languages, with negations, denials,
    questions, hedges, actors and place aliases.
  - Places come from `GazetteerPlaceIndex` (036 places, 040 countries, admin-1 regions and gazetteer places with at
    least 15,000 inhabitants, with population-weighted ambiguity).
  - Entities come from `EntityNameIndex` (098 entities, IMO and MMSI numbers, 104 facilities). Times come from
    `TimeExpressions`.
  - Claims are aggregated per narrative by type, polarity and place.
  - `SocialEvidenceGrades` sets reliability: `F` by default, `E` or `D` from the 137 track record, or the operator
    override in `social.sourceReliability`. Credibility follows the claim's status.
- **LLM extraction (optional):** `LlmClaimExtraction` uses the `claim-extraction` role and template
  `claim-extraction-system@1.0.0`. It runs at 50 posts or 5 groups and again when the narrative has doubled, at most
  20 narratives a day. The data block is wrapped by `AskPrompts`, the answer is checked against the schema, and
  places are resolved deterministically. The model never sets a status, a count or a grade.
- **Media:**
  - `Pdq` is a port of Meta ThreatExchange PDQ (BSD, origin header and `THIRD_PARTY_NOTICES.md` section).
    `PdqIndex` is a 16 × 16-bit multi-index.
  - `MediaFingerprinter` drains the `social_media_queue` through `OutboundUrlPolicy` `PUBLIC_ONLY`, `PinnedHttp`, the
    install User-Agent and `ImageGuard`. Images are discarded unless `social.keepMedia` is on.
  - `MediaReuseDetector` judges `earlier` (≥ 7 days) or `elsewhere` (disjoint regions), with the `REFERENCE_WINDOW`
    caveat.
- **Propagation:** `PropagationTracer` 1.0.0 builds edges (`repost`, `quote`, `reply`, `link`, `text-match`,
  `media-match`, `cross-platform`) and reports the earliest post per platform, first news pickup, median repost delay
  and descent share. Graphs above 500 nodes are summarized. Narrative and claim provenance chains were added to
  `ProvenanceChains`.
- **Coordination:** `CoordinationDetector` 1.0.0 and `CoordinationRunner` run daily at 05:10 UTC. They measure
  synchrony, text duplication, account-age skew and co-repost bursts, apply Benjamini–Hochberg across the day's
  narratives, and require at least two indicators with q < 0.01.
- **API:** all 11 operations are in `NarrativeController`.
  - MCP tools `list_narratives`, `get_narrative`, `get_claims` and `match_media` (`@RestrictedRead`, in the analyst's
    read catalog).
  - SSE events on topic `social`: `narrative.emerged`, `narrative.updated`, `claim.extracted`, `media.reuse` and
    `coordination.detected`.
  - 129 object types `narrative` and `claim` (contract 1.5).
  - Four problem codes and 11 AccessPolicy rules.

## Files created / modified

**Created (main):**
- `social/narrative/`:
  - Core: `NarrativeVocabulary`, `TextSignatures`, `NarrativeGrouper`, `NarrativeClusterer`, `NarrativeService`,
    `NarrativeStore`, `NarrativeViews`, `NarrativeObjectResolvers`, `NarrativeJobs`.
  - Claims: `ClaimLexicon`, `ClaimExtractor`, `PlaceIndex`, `GazetteerPlaceIndex`, `EntityIndex`, `EntityNameIndex`,
    `TimeExpressions`, `LlmClaimExtraction`, `SocialEvidenceGrades`.
  - Media, propagation and coordination: `Pdq`, `PdqIndex`, `MediaFingerprinter`, `MediaReuseDetector`,
    `PropagationTracer`, `CoordinationDetector`, `CoordinationRunner`.
  - Entities: `SocialNarrativeEntity`, `SocialNarrativeMemberEntity`, `SocialNarrativeLogEntity`, `SocialClaimEntity`,
    `MediaFingerprintEntity`, `MediaUseEntity`, `MediaQueueEntity`, `PropagationEdgeEntity`.
  - `package-info`.
- `api/NarrativeController`.
- `mcp/tools/ListNarrativesTool`, `GetNarrativeTool`, `GetClaimsTool`, `MatchMediaTool`.
- Resources: `intel/social/claim-lexicon.json` and `llm/templates/claim-extraction-system@1.0.0.md`.

**Modified (main):**
- `evidence/StoryFamilyHandler`: default `handles`.
- `evidence/EvidenceResolver`: handler list.
- `news/NewsStoryResolver`.
- `object/ObjectTypes`: 1.5, `NARRATIVE`, `CLAIM`.
- `settings/SettingKeys`: four keys.
- `api/error/ErrorCodes`: four codes.
- `security/AccessPolicy`.
- `social/SocialEvents`: private emit.
- `social/SocialViews`: `narrativeIds` on posts.
- `llm/PromptTemplates`.
- `analyst/AnalystRunner`.
- `exchange/ProvenanceChains`.
- `application.yml`.
- `intel/canonical-rules.json`.

**Docs:**
- `docs/OpenAPI.json`: 11 operations implemented, four error codes, `ClaimPlace.regionCode`, contract example 1.5,
  changelog.
- `docs/Client-Contract.md`: rewritten whole, with a new §16 and contract 1.5.
- `docs/Operator-Guide.md`: rewritten whole. It adds a new "Narratives and claims (prompt 136)" section and updates the
  129 contract, the 119 finding producers, and the 135 streams and evidence paragraphs.
- `THIRD_PARTY_NOTICES.md`: Meta ThreatExchange PDQ.

**Tests:**
- Created: `testsupport/social/NarrativeTestSupport` and `social/narrative/` `ClaimExtractorTest`,
  `LlmClaimExtractionTest`, `PdqTest`, `CoordinationDetectorTest`, `PropagationTracerTest`, `SocialEvidenceItemTest`,
  `NarrativeClustererTest`, `MediaReuseTest` and `NarrativeControllersIT`.
- Resources: `src/test/resources/pdq/bridge-1-original.jpg` and `bridge-5-flipx.jpg`, copied from ThreatExchange's
  test data.
- Modified:
  - `NewsControllersIT` and `ReportControllersIT`: contract 1.5.
  - `EventSchemaConformanceTest`: five events and their factories.
  - `application-test.yml`.
  - `openapi/pending-operations.txt`: 11 lines removed, 39 left.

## Deviations from the prompt

1. **Reliability overrides are keyed by `account:{platform}:{authorKey}` or `channel:{platform}:{channel}`**, not by
   handle as the 137 SocialSettings spec sketches. Handles are not retained beyond posts, and the author key is what
   posts expose.
2. **Velocity is narrative-local**: the last closed hour over the median of the 24 hourly counts before it. It does not
   use 135's topic baselines. States use 6 h, 24 h and 72 h; velocity bands are 5 and 0.5.
3. **Link and media joins do not require a shared scope.** Text joins do.
4. **Media joins** happen at clustering by identical image URL. Once a fingerprint is computed, an asynchronous step
   merges one-post narratives whose image already sits in an active narrative.
5. **Shared external links root evidence groups** (`link:{url}`), as do shared fingerprints.
6. **Situation links come only through linked news stories**: a link to the story's article, or a title-token Jaccard
   of at least 0.5 with a shared topic or region in the window.
7. **ImageGuard accepts PNG, JPEG and TIFF only**, so GIF and WebP post images are skipped and GIF or WebP uploads to
   `/media/match` return `415 IMAGE_REJECTED`.
8. **Permutation p-values use a normal approximation** to the 200 shuffles, so small p-values are representable for
   BH. The text-duplication null comes from the same day's other narratives (median and scaled MAD).
9. **`llmClaimMinEvidenceGroups` is fixed at 5** rather than a setting. LLM claim support counts are deterministic:
   members whose post claims share the type.
10. **Official-record propagation nodes** come from posts linking a URL that matches a `record_head` `payload->>'url'`.
11. **Additive spec reconciliations:** `ClaimPlace.regionCode` was added. `NarrativePost.evidenceGroupId` may be `""`
    for a stub post whose record is gone.
12. **Member text is not stored.** Titles and LLM packets read the central posts' current `record_head` text, so an
    expired post contributes no text.
13. **Contract version 1.5** (narrative and claim object types).
14. **Wire keys kept as specified:** `summarised` on the propagation graph (the spec's field name) and the existing
    `ais-behaviour` signal-family id used by the lexicon's `seizure` type both stay as they are, because they cross a
    wire or persist.
15. **Edited by script:** the spec, as in earlier prompts. The three documents were read end to end and written whole
    by script. Postman was regenerated with no diff.

## Issues found / open questions

**Owner decisions:**
- (a) Override keys by author key or channel instead of handle (Deviation 1).
- (b) Narrative-local velocity and the state thresholds (Deviation 2).
- (c) Unscoped link and media joins (Deviation 3).
- (d) GIF and WebP not fingerprinted until ImageGuard accepts them (Deviation 7).
- (e) The normal-approximation p-values and the cross-narrative duplication null (Deviation 8).
- (f) `llmClaimMinEvidenceGroups` as a constant (Deviation 9).

**Unverified because nothing was run:**
- The PDQ golden test expects the reference hashes `f8f8f0ce…` and `f8f80f31…` for the ThreatExchange bridge images.
  That relies on JDK ImageIO's JPEG decoding matching the reference Java implementation's output.
- The LLM path has never called a model.
- Every integration test (clustering, media reuse, controllers) is untested against PostgreSQL.

**Schema for prompt 144:**
- Hibernate tables: `social_narrative`, `social_narrative_member`, `social_narrative_log`, `social_claim`,
  `media_fingerprint`, `media_use`, `social_media_queue` and `social_propagation_edge`.
- App settings: `social.keepMedia`, `social.llmClaimMinPosts`, `social.llmClaimsPerDay` and
  `social.sourceReliability`.
- Property: `foresignal.social.narratives-scheduled`.

**Guards not run:**
- Not run, under the no-tests directive: the US English guard, `lintOpenApi` and every contract test
  (`AccessPolicySyncTest`, `OpenApiContractIT`, `EventSchemaConformanceTest`, `RestrictedReadCoverageTest`).
- The diff was checked by grep for British spellings. The only hits are the two wire keys of Deviation 14 and the BSD
  license text.

**External data and instructions:**
- Read on 2026-10-18: the GitHub API and raw files of `facebook/ThreatExchange` (the PDQ Java reference, the BSD
  `LICENSE` and the two test images; main commit `53f2929e3700360f0aeb5d74a209426c087f6d6a`).
- No page, file or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `social/narrative/ClaimExtractorTest` | Types in several languages; denial, negation, question and hedge polarity; place resolution and ambiguity; entities by name and IMO; time expressions |
| `social/narrative/LlmClaimExtractionTest` | Eligibility and doubling; schema parsing; unknown types dropped; deterministic place resolution; daily cap and budget refusal |
| `social/narrative/PdqTest` | Reference vectors for the bridge images and their flip; quality; hex round trip; distance; multi-index search |
| `social/narrative/CoordinationDetectorTest` | Each indicator against a planted signal and a null crowd; BH adjustment; two-indicator rule; unavailable indicators |
| `social/narrative/PropagationTracerTest` | Edges and deltas; earliest post per platform; first news pickup; descent share; a 10,000-repost cascade summarized under 500 nodes |
| `social/narrative/SocialEvidenceItemTest` | Admiralty reliability from the track record and overrides; credibility by status; source keys |
| `social/narrative/NarrativeClustererTest` | Repost, quote, text, link and media joins; window and scope rules; groups by origin; merge and split with history; two-source dedup |
| `social/narrative/MediaReuseTest` | `earlier` reuse finding with `REFERENCE_WINDOW`; no reuse in the same place; fingerprint uses and matching |
| `social/narrative/NarrativeControllersIT` | All 11 operations, scopes, problem codes, multipart and hash matching, MCP tool shapes |
| `stream/sse/EventSchemaConformanceTest` (added cases) | The five new `social` topic events |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit 0.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests … covers all ops: True"; no diff.

## Notes for the next prompt

- **137 (cues and assessment):**
  - Set claim and narrative statuses through `NarrativeStore` (`social_claim.status`, `social_narrative.status`).
    `SocialEvidenceGrades` already derives credibility from status and reliability from `trackRecord`.
  - Emit `narrative.updated` through `NarrativeService.changed`.
- **144 (Flyway):** capture the eight tables above.
- **Tests:** use `NarrativeTestSupport` (`Spec`, `post`, `places`, `extract`).

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `47ef2ebf8338511fe5f5b20d037e310e943e252a`
