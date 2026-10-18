# 133-CR-News-Coverage-And-Media-Bias

| | |
|---|---|
| Prompt | 133-News-Coverage-And-Media-Bias.md |
| Status | Partial (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**; the live MBFC API client is not built because its endpoint could not be verified, and the AllSides license could not be confirmed) |
| Implementation commit | `895d62c3e893b48066c58d9d1ccc03a2d8c00276` |
| Date | 2026-10-17 |

## Summary

This prompt brings news coverage analysis into the intelligence layer: news **articles** (metadata only), an **outlet
registry** with ratings from declared providers, **stories** built on the 116 evidence machinery (wire reprints count
once), **coverage balance**, **blindspots** and **coverage divergence**, and their use in situations, dossiers,
hypotheses, assessments and reports. The code lives in `ai.foresignal.news`, `ai.foresignal.media.outlet` and
`ai.foresignal.source.formats.news`, plus two adapters, two controllers and three MCP tools. It compiles. **The tests are
written but were not run** (owner directive 2026-10-17): no test result, coverage figure or guard result in this report
is measured.

- **Articles:**
  - `gdelt-articles` (`GdeltArticlesAdapter`, `GdeltArtListParser`): one GDELT DOC 2.0 `mode=ArtList&format=json&
    maxrecords=250&sort=DateDesc` request per enabled attention topic, `timespan` since the topic's last run (15 min to
    1 day; first run 1 day), at most 10 topics per run, every 30 minutes. Attention topics now also sync into this
    source's `topics` parameter.
  - **Shared host pacing:** 117 used a per-source `RequestSpacing`. Both GDELT descriptors now declare `hostPacing` 5 s,
    so `UpstreamClient`'s existing shared `HostPacer` (prompt 080) keeps them serial on `api.gdeltproject.org`.
  - The ArtList shape was verified against a live answer on 2026-10-18 (fixture `artlist-live.json`).
  - Outlet feeds: the 073 feed kit's reviewed item mapper `news-article` (`NewsArticleItemMapper`) maps the items of
    `news-feed-*` (bundled) and `op-news-feed-*` (operator overlay) specs to the same kind; the validator enforces the
    pairing, and authors are dropped.
  - Records (`NewsArticles`): kind `news-article`, `externalId` = SHA-256 of the canonical URL (`ArticleUrls`: lower-case
    host, no default port, fragment or tracking parameters), payload `title`, `url`, `domain`, `outletId`, `language`,
    `sourceCountry`, `seenAt`, `publishedAt`, `topicIds`, `summary` (≤ 500), `imageUrl` (link only); `bulk-event`, 90 d.
    No body, byline or image is ever fetched.
- **Outlet registry and ratings (`ai.foresignal.media.outlet`):**
  - Tables `media_outlet`, `media_rating`, `outlet_consensus`, `media_provider` (Hibernate), written by SQL.
  - `OutletRegistry`: domain resolution (parent domains, merges), unknown domains registered as `unrated`
    (`NewsOutletLookup`, used by the adapter), 13 news agencies seeded from `media/wires.json` as type `wire` mapped to
    their own 116 originators (added to `intel/originators.json`), syndication through `aggregates`, the admin merge
    patch (ratings never edited).
  - `MediaProviders`: `wikidata` (CC0, on), `partisan-web` (CC BY 4.0, on), `allsides` (off, `LOCAL_ONLY` unless
    permission is recorded), `mbfc` (off, always `LOCAL_ONLY`, encrypted key), `operator` (on, per-row license). The
    admin license acknowledgement gate is `409 PROVIDER_LICENCE_NOT_ACKNOWLEDGED`.
  - `RatingImportService` with `WikidataParser` (SPARQL by official website P856; alignment P1387 to lean; state media,
    public broadcaster and news agency), `PartisanWebParser` (front-matter axes) and `OperatorRatingsParser` (CSV/JSON;
    AllSides and MBFC label tables). The upstream imports run through `ProbeService.probeCall` on the new
    `media-ratings` source, so they are budgeted. Wikidata daily (30-day cache), Partisan Web weekly, when enabled.
  - `Consensus`: median, `nProviders`, disagreement, lean bucket (|median| < 0.75 center), recomputed on import and when
    the enabled set or an egress changes; every response carries the provider breakdown.
  - `OutletViews`: `LOCAL_ONLY` ratings are withheld (and the consensus recomputed from `OPEN` providers) on paths that
    may not carry local-only data (`EgressPolicy.allowsLocalOnlyData`).
- **Stories (`ai.foresignal.news`):**
  - Family `news-story` in `intel/canonical-rules.json` (1.1.0, `match: "story"`, cosine 0.82, 48 h, Jaccard 0.6, wire
    Jaccard 0.9). `EvidenceResolver` hands story-family versions to the new SPI `StoryFamilyHandler` on its own cursor.
  - `NewsStoryResolver`: title-embedding cosine against the story centroid (027 vectors read by `TitleEmbeddings`), else
    the token Jaccard (`TitleTokens`); candidates share a topic or region within 48 h; deterministic title (most central
    article); the story is a canonical event and each article an observation link.
  - `NewsGrouper`: reprint clusters (Jaccard ≥ 0.9 across outlets) become `wire:{originator}` groups with `syndicated`
    members; reprints of unknown origin one `syndicated:` group; otherwise one group per outlet. Flags `ECHO`,
    `SINGLE_ORIGINATOR`.
  - Tables `news_story`, `news_article`, `news_coverage_daily`; stories published as `foresignal-intel` records of kind
    `news-story` (`NewsStoryRecordPublisher`), which the 118 linker reads as `context` members.
  - Corroboration (`NewsCoverageJob`): a non-news canonical event in a shared region within 24 h with a sensor-network
    or agency observation.
- **Coverage (`CoverageAnalyzer` 1.0.0, `NewsCoverageService`, `NewsCoverageJob`):** counts by lean bucket, country,
  outlet type and factuality band; balance; state-affiliated share; blindspot (≥ 75% of ≥ 8 rated evidence groups from
  one side); skew (`SKEWED_COVERAGE`); daily series `rollup:news/{topicId|situationId}/{bucket}` (new `SeriesRef` type
  `NEWS`, read by `SeriesReader`, registered by `AnalysisSeriesRegistrar`); findings `coverage-blindspot` and
  `coverage-divergence` (3 SD over a 28-day baseline, other buckets below); article rows pruned after 90 days.
- **Use in analysis:** the situation `coverage` member (`SituationViews`), the dossier `coverage` member
  (`include=coverage`), `Hypothesis.newsCoverage` with the `ConfidenceAssessor` 1.1.0 step-down for skewed, uncorroborated
  news support (`RevisionWriter`), the assessment Coverage section and `coverage` member (`AssessmentComposer`,
  `CycleRunner`, new `assessment.news_coverage` column), and the report coverage block (`ReportDrafter`,
  `ReportRenderer`). Setting `media.reliabilityMap` maps outlet factuality to a 109 grade (`NewsReliability`), used by
  the `EventScorer` member views (`CorrelationEngine`, `IntelEventService`) and the confidence assessor.
- **API, MCP, contract:** `api/NewsController` (3 operations), `api/MediaOutletsController` (7 operations), MCP
  `list_news_stories`, `get_news_story`, `get_outlet` (read, in the analyst read catalog), contract 1.4 (object types
  `news-story` and `outlet` with resolvers), four Problem codes, `AccessPolicy` rows.
- **Spec and docs:** the 10 operations are `implemented` and removed from `pending-operations.txt` (69 lines left);
  Problem codes, `MediaOutletPatch.aggregates`, the `rollup:news/` grammar, the contract example and a changelog entry in
  `docs/OpenAPI.json`; Postman regenerated ("844 requests … covers all ops: True") with three new example bodies.
  `docs/Source-Catalog.md` (the 133 section replaced with the as-built rows; the 117 `gdelt-timelines` and attention
  rows updated), `THIRD_PARTY_NOTICES.md` (data-provider notices), `docs/Operator-Guide.md` (new "News coverage and
  media bias (prompt 133)", plus updated sentences in the 109, 117, 118, 119, 125, 128, 129 and 132 sections),
  `docs/Client-Contract.md` (contract 1.4, §13 as built) and `docs/Agent-Guide.md` (three tool rows, a "News coverage"
  subsection, vocabulary and scope rows) were each read end to end and written whole by script.

## Files created / modified

**Created (main):**
- `news/`: `package-info`, `NewsStoryEntity`, `NewsArticleEntity`, `NewsCoverageDailyEntity`, `NewsStoryStore`,
  `TitleTokens`, `Vectors`, `TitleEmbeddings`, `NewsGrouper`, `CoverageAnalyzer`, `NewsStoryResolver`,
  `NewsCoverageService`, `NewsCoverageJob`, `NewsStoryRecordPublisher`, `NewsReliability`, `NewsViews`,
  `NewsStoryObjectResolver`.
- `media/outlet/`: `package-info`, `MediaVocabulary`, `MediaOutletEntity`, `MediaRatingEntity`, `OutletConsensusEntity`,
  `MediaProviderEntity`, `Consensus`, `MediaProviders`, `OutletRegistry`, `RatingRow`, `PartisanWebParser`,
  `WikidataParser`, `OperatorRatingsParser`, `RatingImportService`, `OutletViews`, `OutletObjectResolver`.
- `source/formats/news/`: `package-info`, `ArticleUrls`, `NewsNames`, `NewsOutletLookup`, `NewsArticles`.
- `source/adapters/gdelt/GdeltArticlesAdapter`, `GdeltArtListParser`; `source/adapters/media/MediaRatingsAdapter`
  (+ `package-info`); `source/kit/feed/NewsArticleItemMapper`; `evidence/StoryFamilyHandler`.
- `api/NewsController`, `api/MediaOutletsController`; `mcp/tools/ListNewsStoriesTool`, `GetNewsStoryTool`,
  `GetOutletTool`; resource `media/wires.json`.

**Modified (main):** `evidence/EvidenceResolver`, `CanonicalRules`; `observability/ObservabilityDeclarations`;
`source/adapters/gdelt/GdeltTimelineAdapter`; `source/kit/feed/FeedDescriptors`, `FeedItemMappers`, `FeedSpecValidator`;
`attention/AttentionTopicService`; `analytics/series/SeriesRef`, `SeriesReader`, `analytics/AnalyticsSupport`,
`analytics/registry/AnalysisSeriesRegistrar`, `analytics/finding/FindingService`; `situation/LinkerInputReader`,
`SituationViews`; `intel/ForesignalIntelGenerator`, `CorrelationEngine`, `IntelEventService`;
`hypothesis/ConfidenceAssessor`, `RevisionWriter`, `HypothesisService`, `IndicatorWatchService`;
`cycle/AssessmentEntity`, `AssessmentService`, `AssessmentComposer`, `CycleRunner`; `object/ObjectTypes`,
`DossierService`; `report/ReportDrafter`, `ReportRenderer`; `security/AccessPolicy`, `EgressPolicy`;
`settings/SettingKeys`; `api/error/ErrorCodes`; `analyst/AnalystRunner`; resources `intel/canonical-rules.json`,
`intel/originators.json`, `intel/signal-families.json`, `intel/themes.json`.

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`,
`docs/Client-Contract.md`, `docs/Agent-Guide.md`, `THIRD_PARTY_NOTICES.md`, `scripts/generate-postman.py`,
`postman/Foresignal.postman_collection.json`, `src/test/resources/openapi/pending-operations.txt` (−10).

**Tests:** the new classes below, the helper `testsupport/news/NewsTestSupport`, fixtures `fixtures/gdelt-articles/`
and `fixtures/media-ratings/` (each with a README). Modified: `retention/FamilyDefaultsTest` (two adapters),
`report/ReportControllersIT` (contract 1.4), `hypothesis/ConfidenceAssessorTest` (the 1.1.0 step-down).

## Deviations from the prompt

1. **Package:** the registry is `ai.foresignal.media.outlet`, not `ai.foresignal.media`, which is already the 043 image
   store. Coverage lives in `ai.foresignal.news`.
2. **Host pacing:** `HostPacer` already existed in `collect` (prompt 080, per host, inside `UpstreamClient`). Instead of
   creating one, both GDELT descriptors declare `hostPacing` 5 s; `gdelt-timelines` keeps its own `RequestSpacing` too.
3. **Story clustering runs on the evidence cursor through a new SPI** (`StoryFamilyHandler`, `match: "story"`) rather
   than the 116 proximity matcher, because stories have no place. Embeddings are read from the active 027 index only;
   an article not yet embedded is matched by the token fallback, so the method can differ from a later replay.
   Candidates need a shared topic or region; an article with neither (most feed items) may join any story in the
   window.
4. **Story id = canonical event id**; story references are `news-story:{id}` (contract 1.4, with `outlet:{id}`), which
   needed resolvers and a contract bump.
5. **Article rows are copied into `news_article`** (metadata, story, group, `syndicated`), pruned after 90 days, so the
   API and coverage need no record reads; feed articles get their outlet from the registry here (a feed mapper is
   pure), GDELT articles at fetch.
6. **Grouping beyond the prompt:** near-identical reprints whose wire article is absent form one `syndicated:` group of
   unknown origin; the 13 seeded wires and the `aggregates` member (`MediaOutletPatch.aggregates`, additive spec
   change; empty means any wire) decide wire groups.
7. **Upstream imports through `ProbeService.probeCall` on a new source `media-ratings`** (writes no records) so Wikidata
   and GitHub requests are budgeted and identified. Scheduled imports run only while that source and the provider are
   enabled.
8. **MBFC live API not built:** the RapidAPI endpoint, host and response shape could not be verified (the listing page
   is not readable without an account), and guessing an API is forbidden. MBFC ratings are imported from the operator's
   own export (CSV/JSON) with `LOCAL_ONLY`; the key is stored encrypted for a future verified client. → Status Partial.
9. **AllSides license not confirmed:** allsides.com answered 403 to automated reads on 2026-10-17; the only statement
   found was secondhand. The provider stays off, labeled "reported; confirm", `LOCAL_ONLY` until permission is
   recorded, and imports only the operator's own rows (no fetching from allsides.com).
10. **Partisan Web mapping:** lean = `bias_social` ÷ 5; factuality = 1 + 4 × `fact_sourcing` ÷ 10; every other axis as
    `partisan-web:{axis}` with its published value; template-default files give no rating; `ownership` is not imported
    because it can name a person (AFP's file names its chief executive). The data was last pushed 2021-10-09.
11. **Wikidata mapping:** the P1387 table and item choice in `WikidataParser`; registry facts (QID, country, owner,
    parent, type) are applied as facts, ratings only for alignment, state media and ownership type.
12. **Operator row egress** is inferred from the declared license (open licenses `OPEN`, anything else `LOCAL_ONLY`):
    the spec row has no egress field.
13. **`LOCAL_ONLY` ratings** are withheld by path (`EgressPolicy.allowsLocalOnlyData`, the `LOCAL_ONLY` path set), so
    the API and MCP show them and share links, feeds, sinks and federation never would; coverage statistics use every
    enabled provider (derived counts).
14. **Blindspot value** names the side the coverage comes from (`right` = covered mostly by right-rated outlets);
    skew needs at least 3 evidence groups (the prompt names no minimum).
15. **Corroboration** is a periodic job (non-news canonical event sharing a region within 24 h, with a sensor-network or
    agency observation), not a 116 resolver step, since sensor events may arrive after the story.
16. **News stories join situations as `context` members**, so they never seed, revive or escalate a situation.
17. **Hypothesis news support** is read from live ACH rows rated C/CC (indicator observations also produce ACH rows);
    the set-level confidence uses the leading hypothesis's judgment. `ConfidenceAssessor` is now 1.1.0 (the old
    constructor is kept).
18. **Assessments** store the Coverage section in a new nullable column `assessment.news_coverage`.
19. **Reports:** `MEDIA_COVERAGE_UNAVAILABLE` remains only when the news layer is absent; `NO_NEWS_COVERAGE` is new.
20. **Divergence detector** (`coverage-divergence` 1.0.0): 28-day baseline, at least 14 baseline days, SD floor 1, at
    least 3 groups on the day; topics only (situations have series but no divergence finding).
21. **Edited by script:** the spec, as in earlier prompts; the five documents were read end to end and written whole by
    script (by documentation subagents, verified against the code).

## Issues found / open questions

**Owner decisions:**
- (a) the AllSides license (Deviation 9) and whether to build an MBFC API client once its documentation is available
  (Deviation 8);
- (b) the Partisan Web and Wikidata mappings (Deviations 10, 11) and the default reliability map (5→A … 1→E,
  unrated→F; numbers A 0.9 … E 0.3);
- (c) operator-row egress inference (Deviation 12);
- (d) the blindspot value convention and the skew minimum (Deviation 14);
- (e) whether news stories should ever be `core` situation members (Deviation 16);
- (f) the seeded wire list (`media/wires.json`) and "no aggregates means any wire" (Deviation 6);
- (g) `intel/observability.json` has no entries for `gdelt-articles`, `media-ratings` or the news feeds; they fall back
  to the 116 "observes everywhere" default, which the Source Catalog's sentence "every catalogued source also has an
  originator and observability entry" does not match. Which should change?

**Known limits:**
- Story clustering is order-dependent (first-come centroids) and cannot split or merge stories after the fact.
- The Partisan Web data is small (about 70 outlets) and dated; most outlets will be `unrated` until the operator imports
  ratings.
- Older British spellings outside this prompt's text remain (for example the Source Catalog's `## Licence` heading and
  some Operator Guide sections); they were not touched.

**Schema for prompt 144:**
- Hibernate tables: `media_outlet` (`media_outlet_primary_domain_key`, `media_outlet_country_idx`), `media_rating`
  (`media_rating_outlet_provider_dim_key`, `media_rating_provider_idx`), `outlet_consensus`
  (`outlet_consensus_outlet_idx`, `outlet_consensus_dimension_bucket_idx`), `media_provider`, `news_story`
  (`news_story_last_seen_idx`, `news_story_updated_idx`), `news_article` (`news_article_story_idx`, `_outlet_idx`,
  `_seen_idx`, `_record_idx`), `news_coverage_daily` (`news_coverage_daily_scope_idx`).
- New column: `assessment.news_coverage` (jsonb). New app setting: `media.reliabilityMap`.

**Guards not run:**
- Not run, under the no-tests directive: the US English guard, `lintOpenApi` and every contract test
  (`AccessPolicySyncTest`, `OpenApiContractIT`, `RestrictedReadCoverageTest`, `OriginatorRegistryTest`).
- New prose and code were checked by grep for British spellings; the only hits are wire names (`licence` keys and CSV
  column, `PROVIDER_LICENCE_NOT_ACKNOWLEDGED`) and the upstream path `organisations/`.

**Risks for the owner's first test run:**
- `RatingProvidersTest.partisanWebImport` goes through `ProbeService` to WireMock on localhost; if the outbound policy or
  a disabled `media-ratings` source refuses that in tests, the test needs a policy or source setup.
- `GdeltTimelineAdapterTest` now also waits on the real 5 s host gate (four requests, about 15 s).
- `NewsStoryClusteringIT` replaces `TitleEmbeddings` with a `@MockitoBean` (its own Spring context).

**External data:** fetched during construction: one GDELT ArtList answer (2026-10-18), Wikidata `wbgetentities`,
`wbsearchentities` and one SPARQL query, the tpw-data `LICENSE`, README, contents listing and two organization files,
the MBFC data-API page (RapidAPI listing unreadable), and a search for the AllSides license (allsides.com 403). No page,
response or file contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `source/adapters/gdelt/GdeltArticlesAdapterTest` (WireMock) | ArtList request parameters and timespans, parsing of a live and a synthetic answer, canonical URLs and SHA-256 ids, topics merged, outlet lookup, text and HTTP failures, the 5 s host gate shared with `gdelt-timelines`, no other request |
| `source/formats/news/ArticleUrlsTest` | Canonicalization, domains, country and language names, record metadata limits |
| `media/outlet/OutletRegistryTest` | Wire seeding and originators, unknown domains as unrated, parent domains, syndication, PATCH and merges, consensus over enabled providers and buckets |
| `media/outlet/RatingProvidersTest` | Wikidata SPARQL mapping (live capture), Partisan Web axes (live and synthetic files), operator CSV/JSON and label tables, defaults, the license gate (NEWS-1), `LOCAL_ONLY` for AllSides and MBFC, operator import and consensus, the Partisan Web import path |
| `media/outlet/ConsensusTest` | Median, disagreement, label-only providers, buckets at ±0.75, the OPEN-only consensus |
| `news/NewsStoryClusteringIT` | Token fallback, embedding clustering, window and topic rules, 30 wire reprints as one group with `syndicated` members, exactly-once processing, situation link and corroboration by a sensor event (NEWS-2) |
| `news/NewsIndependenceTest` | NEWS-2 grouping rules and no bylines; the news-feed family |
| `news/CoverageAnalyzerTest` | Balance, blindspot threshold and minimum, state share and skew, factuality bands, the divergence detector on a planted spike |
| `news/CoverageInAnalysisIT` | Situation, dossier and assessment coverage; hypothesis `SKEWED_COVERAGE`; the confidence step-down only without non-news (or story) corroboration |
| `news/NewsControllersIT` | Endpoints, filters, cursors, Problem codes, scopes, imports (CSV, JSON, multipart), contract 1.4 and object resolution, MCP tool shapes |
| `hypothesis/ConfidenceAssessorTest` (added case) | The 1.1.0 step-down |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit 0, no errors.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **134–137:** news articles are `news-article` records of `gdelt-articles` and `news-feed-*`; stories are
  `news-story:{id}` (= canonical event id); coverage via `NewsCoverageService`; outlets via `OutletRegistry` (accounts in
  136 are analyzed like outlets).
- **138 (EDP bindings):** article bodies and bylines arrive as EDP `article` documents bound to `news-article` records by
  `source/externalId` (`news_article.id`); `NewsViews.article` has no author field until then.
- **Tests:** `foresignal.analytics.scheduled` is false in tests; drive `NewsStoryResolver.process`,
  `NewsCoverageJob.runOnce/corroborate/situations/daily/blindspots/divergences` and `RatingImportService.importRatings`;
  use `NewsTestSupport`.
- **144 (Flyway):** capture the seven tables and the `assessment.news_coverage` column above.

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `895d62c3e893b48066c58d9d1ccc03a2d8c00276`
