# 132 — News Coverage And Media Bias

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/132-CR-News-Coverage-And-Media-Bias.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/131-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principles 9, 13, 14), §12.5 (news coverage and media bias), §14.5 (data terms)
- `docs/Source-Catalog.md` "News and media ratings (prompt 132)" and the excluded list
- Completion Reports of 027 (embeddings), 066 (GDELT), 073 (feed kits), 101 (EventScorer reliability input), 109 (reliability grades, licence policy), 116 (originators, canonical events, evidence groups), 117 (attention topics, GDELT DOC client and its pacing), 118 (situations, linker), 119 (findings, series registry, event studies), 124 (hypotheses, confidence assessor), 127 (assessments), 128 (dossier)
- Upstreams (verify each at fixture capture): GDELT DOC 2.0 `mode=ArtList`; Wikidata Query Service (`https://query.wikidata.org/sparql`, CC0, User-Agent policy); The Partisan Web data (`github.com/stockHuman/tpw-data`, CC BY 4.0: confirm the licence covers the data files and record the field meanings); AllSides media bias ratings (licence reported as CC BY-NC 4.0: confirm on allsides.com before enabling); Media Bias/Fact Check ratings API (paid, through the operator's own key)

## Objective

Bring Ground News-style coverage analysis into Foresignal's intelligence layer. Collect news **articles** (metadata only) for the operator's topics, keep an **outlet registry** with lean, factuality, ownership and state-affiliation ratings from declared providers, cluster articles into **stories** using the 116 evidence machinery (so 50 reprints of one wire story are one originator), measure each story's **coverage balance** (left, center, right; by country and outlet type), flag **blindspots** and **coverage divergence**, and feed all of it into situations, hypotheses and assessments as evidence quality and caveats.

## Principles (binding)

- **Ratings are imported, never invented.** Lean and factuality come only from declared providers with their licence and date; no LLM assigns ratings. Disagreement between providers is shown, not hidden.
- **Metadata only.** Store title, URL, outlet, language, country, times and the feed's own summary (≤ 500 chars). Never fetch or scrape article bodies; never store bylines or author names.
- **Coverage is not truth.** Balance and blindspots describe how a story is covered; they never raise or lower the truth of a claim by themselves. Corroboration still comes from independent evidence groups and sensors (116, 118).
- **Licences travel.** Each rating carries its provider's licence and egress (`LOCAL_ONLY` for paid or non-commercial data where the terms require).

## Tasks

### 1. Articles

- **`gdelt-articles`** (POLLING): for each enabled attention topic (117), GDELT DOC 2.0 `mode=ArtList&format=json&maxrecords=250&sort=DateDesc&timespan=…` since the last run (max 1 day). Share the GDELT host pacing with 117 (one request per 5 s across both sources: a shared `HostPacer` for `api.gdeltproject.org` in `collect`, created here if 117 used a per-source interval). Interval 30 min.
- **Outlet feeds:** operator-added RSS/Atom feeds through 073's feed kit (`news-feed-*` family) map items to the same kind.
- Records: kind `news-article`, `externalId` = sha256 of the canonicalised URL (lower-case host, no tracking parameters, no fragment), payload `title`, `url`, `domain`, `outletId` (resolved via the registry; null when unknown), `language`, `sourceCountry`, `seenAt`, `publishedAt` (when the feed gives it), `topicIds`, `summary` (feed summary only, ≤ 500 chars), `imageUrl` (link only). Analytic class `bulk-event` (90 d); story aggregates are kept.
- Unknown domains are added to the registry as `unrated` outlets with their country and language.

### 2. Outlet registry and ratings (`ai.foresignal.media`)

- `media_outlet`: `id`, `name`, `domains` text[], `country`, `languages`, `type` (wire | national | regional | local | public-broadcaster | state-affiliated | aggregator | blog | official | other), `owner_org` and `parent_org` (organisation names only), `wikidata_qid`, `originator_id` (116; wires map to their own originator, syndicating outlets record `aggregates: [wire originators]`), timestamps.
- `media_rating`: `outlet_id`, `provider`, `dimension` (lean | factuality | reliability | state-affiliation | ownership-type), `value_norm` (lean on −2…+2: left, lean-left, center, lean-right, right; factuality on 1…5), `value_raw`, `label`, `rated_at`, `licence`, `attribution`, `url`, `egress`, `imported_at`.
- **Providers** (`media_provider` settings: enabled, licence acknowledged by an admin, last import):
  - `wikidata` (default on, CC0): country, owner, instance-of (state media, public broadcaster, news agency) and political alignment (P1387) where present, via SPARQL for registry outlets with a QID (QID lookup by official website P856 domain match), budgeted and cached 30 d.
  - `partisan-web` (default on once the licence is confirmed, CC BY 4.0): import the published data files; map its axes to `value_norm` with the mapping documented in the CR (record any axis that is not left–right as a separate dimension, never forced into lean).
  - `allsides` (default **off**): operator-triggered import of the published ratings under its licence (CC BY-NC 4.0 if confirmed), attribution required; `egress=LOCAL_ONLY` unless the operator records permission.
  - `mbfc` (default **off**): the operator's own API key (credential), `LOCAL_ONLY`.
  - `operator`: CSV/JSON import (`domain, dimension, value, label, licence, attribution`).
- **Consensus:** `outlet_consensus` per outlet and dimension: median of `value_norm` over enabled providers, `n_providers`, `disagreement` (max − min), `bucket` (left | center | right for lean, where |median| < 0.75 is center), recomputed on import. Responses always include the per-provider breakdown.

### 3. Stories (extends 116)

- Add family `news-story` to 116's `canonical-rules.json`: an article joins a story when its title embedding (027) has cosine ≥ 0.82 with the story centroid, within 48 h of the story's latest article, and shares a topic or a region tag; without embeddings, a normalised-title token Jaccard ≥ 0.6. Story centroid and title are updated as articles join (deterministic title: the most central article's title).
- **Evidence groups for news:** articles from the same wire (by outlet registry and near-identical title, Jaccard ≥ 0.9) form one evidence group with the wire as originator; other outlets form their own groups; a `syndicated` flag records reprints.
- **Linking to the physical world:** the 118 linker treats `news-story` canonical events like other inputs (topic, region, time), so situations show their coverage; 116 corroboration marks a story `corroborated` when its canonical event links to ≥ 1 non-news evidence group (sensor, agency) in the same place and window.

### 4. Coverage analytics (`CoverageAnalyzer` 1.0.0)

- Per story: article and evidence-group counts by lean bucket (left, center, right, unrated), by country, by outlet type, by factuality band; `coverageBalance` (shares); `stateAffiliatedShare`; `blindspot` = `left` or `right` when ≥ 75% of **rated** evidence groups come from one side with n_rated ≥ 8; `corroborated` (above).
- Per topic and per situation, daily series `rollup:news/{topicId|situationId}/{bucket}` registered in 119's analysis-series registry (so change points, event studies and precursor mining can use coverage).
- Findings (119 table, new `findingType` values): `coverage-blindspot` (story level) and `coverage-divergence` (a topic's coverage from one bucket rises ≥ 3 SD against its baseline while the others do not).
- Reliability mapping (setting `media.reliabilityMap`): outlet factuality consensus → 109 source reliability grade (A–F) for `news-article` evidence, used by 101's `EventScorer` and 124's confidence assessor.

### 5. Use in analysis

- **Situations (118):** the situation response and the 128 dossier gain a `coverage` section (balance, blindspot, state-affiliated share, corroboration share of news members).
- **Hypotheses (124):** for each hypothesis, the lean and state-affiliation balance of its supporting news evidence; caveat `SKEWED_COVERAGE` when ≥ 75% comes from one bucket or from state-affiliated outlets; the confidence assessor lowers confidence one step when supporting evidence is skewed **and** has no non-news corroboration.
- **Assessments (127):** a "Coverage" section (how each top situation is covered, blindspots, divergences), deterministic.
- **Reports (131):** the `coverage` block uses these statistics.

### 6. API, MCP, spec, docs

- `GET /v1/news/stories?topic&region&situationId&since&blindspot&cursor` (read), `GET /v1/news/stories/{id}` (read; articles grouped by outlet with each outlet's ratings and consensus, coverage statistics, linked situations and canonical events), `GET /v1/news/articles?story&outlet&since&cursor` (read).
- `GET /v1/media/outlets?q&country&type&bucket&cursor` (read), `GET /v1/media/outlets/{id}` (read; ratings per provider with licence and date, consensus, owner), `PATCH /v1/media/outlets/{id}` (admin: type, owner, QID, merge domains), `GET/PUT /v1/media/providers[/{id}]` (admin), `POST /v1/media/ratings/import` (admin; provider-specific or operator CSV).
- MCP: `list_news_stories`, `get_news_story`, `get_outlet` (read; read catalogue).
- SSE topic `analytics` gains the new finding types.
- `docs/OpenAPI.json`: `NewsArticle`, `NewsStory`, `CoverageStats`, `MediaOutlet`, `MediaRating`, `OutletConsensus`, `MediaProvider`; tags `News`, `Media`; `x-foresignal-scope`; Postman entries.
- `docs/Source-Catalog.md`: `gdelt-articles`, the news feed family, and every ratings provider with licence, egress and default state. `THIRD_PARTY_NOTICES.md`: provider attributions as their licences require.
- `docs/Operator-Guide.md` "News coverage and media bias": what the ratings mean and where they come from, provider licences, enabling AllSides or MBFC with your own rights, why ratings are never generated, reading blindspots.
- AccessPolicy rows. Problem codes: `UNKNOWN_STORY`, `UNKNOWN_OUTLET` (404), `PROVIDER_LICENCE_NOT_ACKNOWLEDGED` (409), `RATINGS_IMPORT_INVALID` (400).

## Tests to write (do not run)

- `GdeltArticlesAdapterTest` (WireMock): ArtList parsing, URL canonicalisation, shared host pacing with 117, no body fetching.
- `OutletRegistryTest`: domain resolution, unknown domains added as unrated, wire and syndication mapping to originators.
- `RatingProvidersTest`: Wikidata SPARQL mapping (fixture), Partisan Web axis mapping, operator CSV, licence acknowledgement gate, LOCAL_ONLY egress for AllSides and MBFC.
- `ConsensusTest`: median, disagreement, buckets at the boundaries.
- `NewsStoryClusteringIT`: articles cluster by embedding and by token fallback; 30 reprints of a wire story form one evidence group with `syndicated` members; a story links to a situation and is `corroborated` by a sensor-based canonical event.
- `CoverageAnalyzerTest`: balance, blindspot threshold and minimum n, state-affiliated share, divergence finding on a planted spike.
- `CoverageInAnalysisIT`: situation coverage section; hypothesis `SKEWED_COVERAGE` caveat and the confidence step-down only without non-news corroboration.
- `NewsControllersIT`: endpoints, scopes, MCP shapes; no author names anywhere in responses.

## Acceptance criteria

- Articles for the operator's topics are collected as metadata only and clustered into stories whose reprints count once.
- Outlets carry lean, factuality, ownership and state-affiliation ratings from declared, licensed providers, with consensus and disagreement; nothing is rated by a model.
- Stories, situations and hypotheses report coverage balance, blindspots and divergence, and skewed, uncorroborated support lowers confidence.
- Compiles.

## Guardrails (mandatory)

1. **Tests: write them, don't run them.** Write unit and integration tests for all code in this prompt, targeting ≥ 90% line coverage. **Do not execute any tests.** That means no `./gradlew test`, `build`, `check` or `jacocoTestReport`, and no IDE test runs. Tests run only in the test-run prompts (012, 022, 041, 057, 097, 133, 136, 137, 138).
2. **Hibernate only.** Hibernate manages the schema (`spring.jpa.hibernate.ddl-auto=update`), plus the bootstrap SQL that `docs/Core-Data-Model.md` §9 assigns for partitioned history. **Do not add Flyway, Liquibase or SQL migration files** (Flyway arrives in prompt 138).
3. **Reuse policy.** Never copy code from ShadowBroker, Velocity or World Monitor (AGPL) or SatDump (GPL). They are reference material only. God's Eye View (MIT, `~/Documents/GitLab/gods-eye-view`) may be ported to Java if you add an origin header comment and an entry in `THIRD_PARTY_NOTICES.md`.
4. **Stay in scope.** Implement what this prompt specifies, no more. If the docs conflict with reality (for example, a library API differs from what the docs assume), choose the approach most faithful to `docs/Architecture.md` and `docs/Intelligence-Design.md`, and record it under "Deviations" in the Completion Report.
5. **Verify versions.** When adding a dependency, confirm the version exists on Maven Central and is compatible with Spring Boot 4.1 and Java 21.
6. **Finish with Compile → Commit → Push** (see the final section).

## Finish: Compile → Commit → Push

1. **Compile:** `./gradlew compileJava compileTestJava` must succeed. Fix every compile error. Do not run tests.
2. **Commit** all changes with the message `132: News Coverage And Media Bias`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/132-CR-News-Coverage-And-Media-Bias.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `132: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
