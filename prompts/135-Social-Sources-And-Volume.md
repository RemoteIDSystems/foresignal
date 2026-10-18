# 135 — Social Sources And Volume

> Foresignal build prompt. Run it in a Claude Code session at the repository root (`~/Documents/GitLab/foresignal`).
> Completion Report: `completion-reports/135-CR-Social-Sources-And-Volume.md`

## Context

- **Project:** Foresignal, a headless intelligence broker. Java 21, Spring Boot 4.1, PostgreSQL + PostGIS, Gradle (Kotlin DSL), single module, base package `ai.foresignal`.
- **Adapt to reality:** before starting, check the Completion Reports and the code for anything earlier prompts did differently from what this prompt assumes. Follow what was actually built, and record each adaptation under "Deviations". Older text may use pre-v1.3 prompt numbers; translate them with `docs/Roadmap.md` §5.
- **Conventions:** `docs/Conventions.md` (shared components, key URLs, the pending-operations list, overriding defaults, analytic retention, intelligence conventions) applies to every prompt and wins over conflicting prompt text. `docs/Invariants.md` lists the invariants each prompt must keep.
- **Authoritative docs:** `docs/Product-Charter.md`, `docs/Architecture.md`, `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Roadmap.md`. **`docs/design-history/Core-Data-Model.md` (v1.2) supersedes Architecture §4–§11 wherever they conflict**, `docs/design-history/Weather-Imagery-Design.md` is authoritative for weather, imagery and SatDump, and **`docs/Intelligence-Design.md` is authoritative for the intelligence layer** (situations, findings, precursors, base rates, analogs, the forecast ledger, hypotheses, forecasts, impact projection and analysis cycles).
- **Previous Completion Report:** read `completion-reports/134-CR-*.md` and follow its "Notes for the next prompt".

## Read first

- `docs/Intelligence-Design.md` §2 (principles 6, 9, 14, 16), §4.5–§4.6 (evidence identity, observability), §12.7 (social signals)
- `docs/Source-Catalog.md` "Social platforms (prompts 135–137)"
- Completion Reports of 004/005 (descriptors, budgets), 010 (scheduler, STREAMING sources), 024 (custom sources and ingest formats), 027 (embeddings), 040 (regions), 043 (media store), 093 (series model), 109 (licence metadata), 116 (originators, canonical events, evidence groups, observability), 117 (attention topics), 119 (analysis-series registry), 131 (chronicle capture)
- Upstreams (verify each at fixture capture, and record the version and date in the CR): Bluesky Jetstream v2 (`wss://jetstream.us-east.bsky.network`, `wss://jetstream.us-west.bsky.network`; Network Replay `network.bsky.jetstream.planSnapshot` / `listSegments` / `getSegment` with an API token from `bsky.network/account`); Bluesky AppView `app.bsky.feed.searchPosts`; Mastodon REST (`/api/v1/trends/tags`, `/api/v1/timelines/tag/{hashtag}`, `/api/v1/accounts/{id}/statuses`) and streaming (`/api/v1/streaming`, `stream=hashtag`); Reddit OAuth API (`/r/{sub}/new`, `/search`, 100 queries per minute per client); YouTube Data API v3 (`search.list` 100 units, `videos.list` 1 unit, default 10,000 units per day); X API v2 pay-per-use (`/2/tweets/counts/recent` billed per request, `/2/tweets/search/recent` billed per post returned, `/2/tweets/search/all` and `/2/tweets/counts/all` on upgraded access); Threads keyword search (`graph.threads.net/v1.0/keyword_search`, permission `threads_keyword_search`, 2,200 queries per rolling 24 h); Instagram Graph API hashtag search (business account, Public Content Access, 30 unique hashtags per rolling 7 days)

## Objective

Collect public social media as an intelligence signal. Foresignal is a single-operator, self-hosted tool: every platform is an **adapter the operator chooses to enable** with their own credentials and budget, off by default except the free, open Bluesky live stream. Posts become versioned records; tracked topics, places and entities become **mention-volume series** with seasonal baselines; collection health becomes part of the **observability** model, so that later prompts can tell "nobody is talking about it" from "we could not see the conversation". Prompt 136 turns posts into claims and narratives; 137 correlates them with what Foresignal observes physically.

## Principles (binding)

- **Operator choice.** Adapters for platforms with restrictive or paid terms (X, Threads, Instagram, Reddit) ship disabled. The Source Catalog and the platform's settings page summarise each platform's terms and costs; the operator decides. Foresignal does not hard-block any adapter or use.
- **Accounts are sources.** Account handles are kept with posts so analysts can drill down. Analysis treats accounts as reporting sources (like outlets in 133); Foresignal builds no dossiers of private individuals.
- **Spend is explicit.** Every paid call is estimated before it is made, charged to the 005 budget for its source in money as well as calls, and refused (`BUDGET_EXCEEDED`) when the monthly cap would be exceeded.
- **Measured versus sampled.** Each collection path records whether it is a **complete stream** for its filter (Jetstream, Mastodon hashtag stream), a **paged search** (complete within the API's window and limits), or a **ranked sample** (search APIs that return "top" posts, model-mediated search). Only complete and paged paths feed baselines and velocity; samples feed claims and leads.
- **Platform obligations are settings.** Deletion sync and post retention are per-platform settings with defaults that keep the operator inside their own API agreement (deletion sync on for X, Bluesky and Threads), and the operator can change them.

## Tasks

### 1. Social post model (`ai.foresignal.social`)

- Record kind **`social-post`**: `externalId` = `{platform}:{postId}`; payload `platform`, `postId`, `url`, `author` (`{handle, displayName, accountId, accountCreatedAt?, followers?, verified?}` as the platform returns them), `authorKey` (HMAC-SHA256 of `{platform}:{accountId}` with the instance social salt; used for distinct-author counts and coordination statistics), `text` (≤ 5,000 chars), `language` (platform-supplied, else detected), `createdAt`, `observedAt`, `replyTo`, `quoteOf`, `repostOf` (post refs), `links` (expanded URLs), `hashtags`, `mentions`, `media` (`[{type, url, thumbUrl, altText}]`, links only; 136 fingerprints them), `engagement` (`{replies, reposts, quotes, likes, views}` at capture, nullable per platform), `geo` (only when the platform supplies coordinates or a place), `channel` (subreddit, Mastodon instance, YouTube channel, or the platform's equivalent), `collection` (`stream | search | sample | cue | import`), `topicIds`, `cueId` (137), `deleted` (bool).
- `geom` only from platform geo; text-derived places come from 136 and are never written to `geom`.
- Retention: analytic class `social` (new): posts 30 d by default (`social.postRetention`, per platform), aggregates (series, narratives, claims) under their own classes (series 10 y, intel indefinitely). Chronicles (131) keep what their capture mode says.
- Edits: a changed post is a new version; repost and like events are counted into aggregates and stored as posts only when they quote or comment.
- Originators (116): each post's originator is `social:{platform}:{authorKey}`; reposts and quotes inherit the original post's originator for evidence grouping (an amplification is not an independent observation). Add the `social` class to the originator registry.

### 2. Tracked topics (extends 117)

- `attention_topic` gains a `social` block: `terms` per language (`{lang: [string]}`, ≤ 50 total; phrases allowed), `hashtags` (≤ 30), `accounts` (`{platform, handle}`, ≤ 100), `channels` (subreddits, Mastodon instances and hashtags, YouTube channels; ≤ 100), `exclude` terms, `platforms` (subset), `placeIds` (036) and `regionCodes` (040) whose names and aliases are added as terms, `entityRefs` (098 entities whose names and identifiers are terms), `mode` (`standing | cue-only`).
- **Term index:** an Aho–Corasick matcher over all enabled topics' terms (case- and diacritic-folded, word-boundary aware, per script), rebuilt on topic change; a post matches a topic when it matches a term, hashtag, account or channel and no exclude term. Matching posts are stored; non-matching posts on complete streams are only counted (denominators per platform and language per hour).

### 3. Platform adapters (each a source with descriptor, budget, health, credentials)

| Source id | Type | Default | Collection | Credentials |
|---|---|---|---|---|
| `bluesky-jetstream` | STREAMING | **on** | complete stream of `app.bsky.feed.post` and `app.bsky.feed.repost`; local term matching; cursor = `time_us`, resumed on reconnect; optional Network Replay backfill (≤ 7 d per request) with the operator's API token | none (replay: API token) |
| `bluesky-search` | POLLING / cue | off | `searchPosts` with `sort=latest` (paged) for topics and cues | app password if the AppView requires auth |
| `mastodon` | STREAMING + POLLING | off | per configured instance: hashtag streams (complete per instance), `/api/v1/trends/tags` hourly (daily `uses` and `accounts` history), account statuses | optional user token per instance |
| `reddit` | POLLING / cue | off | `/r/{sub}/new` per channel (paged), `/search` for topics and cues; 100 QPM shared | OAuth script app (operator's) |
| `youtube` | POLLING / cue | off | `search.list` (ranked sample, `order=date`), `videos.list` stats for tracked videos, optional comment threads; units budgeted per day | API key |
| `x` | POLLING / cue | off | `counts/recent` hourly per standing topic (complete counts, billed per request), `search/recent` for cues and sampled standing topics (billed per post), optional full-archive endpoints | bearer token |
| `threads` | cue | off | keyword search (`search_type=RECENT`; paged) for topics and cues; 2,200 queries / 24 h | access token with `threads_keyword_search` |
| `instagram-hashtags` | POLLING | off | `ig_hashtag_search` + `recent_media` for ≤ 30 hashtags per 7 days (quota tracked) | business account token |

- **Other platforms** (Telegram channels, Facebook pages, TikTok, forums, anything else): **EDP providers** (024) with `document` streams of `documentType: social-post`, built as separate projects; prompt 138's `social` binding converts their items into `social-post` records with `collection = import`, so narratives and comparisons include them.
- **X spend model:** before each call compute the maximum cost (`max_results × post price` for search, request price for counts) from `social.x.prices` (operator-editable, seeded from the pricing page at fixture time with its date); refuse when `spent + estimate > social.x.monthlyCapUsd` (default 25); record actual cost from the response size. Hourly counts for N standing topics are shown as a monthly estimate on the settings page before the operator enables them.
- **Deletion sync:** Jetstream delete events, X (periodic batch status checks of stored ids, daily, budgeted), Threads and Bluesky search re-checks for posts inside retention; a deleted post is tombstoned (text and media links removed, aggregates kept). Setting per platform `deletionSync` (default on for X, Bluesky, Threads).
- **Language:** platform language tags first; otherwise Lingua (Apache-2.0; verify the Java artifact and version) restricted to the configured `social.languages` (default `en, ar, he, fa, ru, uk, fr, es, de, tr, zh`).

### 4. Volume series and baselines

- Hourly series per (topic | place | entity) × platform × language: `posts`, `uniqueAuthors`, `reposts`, `engagement`, and per platform-language **denominators** (all posts seen on complete streams), registered in 119's analysis-series registry as `social:{scope}/{platform}/{metric}` so change points, event studies and precursor tests apply unchanged.
- `SocialBaseline` 1.0.0: seasonal baseline per series (hour-of-week median and MAD over the trailing 8 weeks, scaled by the platform-language denominator to remove platform growth and outages), `velocityRatio` = observed / baseline, `zRobust`. Series from sampled paths are excluded.
- **Velocity finding** (119 table, `findingType` `social-velocity`): `velocityRatio ≥ 5` and `uniqueAuthors ≥ 20` in an hour (settings), severity from the robust z; dedup per scope per 6 h.

### 5. Collection health and social observability (extends 116)

- Per source: stream lag, gaps (reconnects with cursor loss, replay coverage), quota or budget exhaustion, error rates → 006 health and a `social_coverage` table per platform × region (040, from platform geo and 136's text-derived places) × hour: posts seen, denominators, `complete` flag.
- **Social observability:** `ObservabilityService` gains family `social-attention` with method `platform-volume`: for a region and window, the expected post volume per platform from the trailing 8-week baseline and whether collection was complete; `canSeeAbsence(social-attention, region, window)` is `ok` only when at least one complete-stream platform was healthy over the window and the region's baseline is ≥ `social.minRegionBaseline` posts per hour (default 20). Below that the result is `LOW_SOCIAL_COVERAGE`.

### 6. API, MCP, SSE, spec, docs

- `GET /v1/social/platforms` (read; each adapter's state, mode, collection type, health, quota, spend this month and estimate), `GET /v1/social/platforms/{platformId}` (read), `PUT /v1/social/platforms/{platformId}` (admin; `SocialPlatformSettings`: enabled, mode, channels, deletionSync, postRetention, budgets, `monthlyCapUsd`, prices), `GET /v1/social/coverage?platform&region&since&until` (read), `GET /v1/social/volume?topic|place|entity&platform&metric&interval&since&until` (read; observed, baseline, ratio, complete flag), `GET /v1/social/posts?topic&platform&q&since&until&cursor` (read; `social-post` records with filters), `GET /v1/social/posts/{platform}/{postId}` (read). Credentials use the existing source credential endpoints (013).
- MCP: `get_social_volume`, `search_social_posts` (read).
- SSE topic `social`: `social.velocity` (finding created), `social.platform.state` (enabled, degraded, budget exhausted).
- `docs/OpenAPI.json`: as specified there (`SocialPost`, `SocialPlatform`, `SocialPlatformSettings`, `SocialCoverage`, `SocialVolume`; `AttentionTopic.social`); `x-foresignal-status` → implemented and the operations removed from the pending list.
- `docs/Source-Catalog.md` "Social platforms": every adapter with access route, costs, quotas, terms summary, default state, collection type and deletion behaviour. `docs/Operator-Guide.md` "Social signals": enabling platforms, credentials, the X spend cap, what is stored and for how long, deletion sync.
- AccessPolicy rows. Problem codes: `UNKNOWN_PLATFORM` (404), `PLATFORM_DISABLED` (409), `BUDGET_EXCEEDED` (429, with the cap and spend).

## Tests to write (do not run)

- `JetstreamAdapterTest` (WebSocket stub): post and repost parsing, `time_us` cursor resume, delete events tombstone, term matching and denominators, replay cutover without gaps or duplicates.
- `TermIndexTest`: multilingual folding, word boundaries in Latin, Arabic, Hebrew and Cyrillic scripts, hashtags, excludes, place-alias expansion.
- `PlatformAdaptersTest` (WireMock per platform): Mastodon trends history, Reddit paging and rate limits, YouTube unit accounting, Threads quota, Instagram 30-hashtag window.
- `XSpendTest`: estimate before call, refusal at the cap, actual cost recording, counts billed per request.
- `SocialBaselineTest`: hour-of-week baseline, denominator scaling removes a planted platform-wide drop, velocity finding thresholds, sampled series excluded.
- `SocialObservabilityTest`: `canSeeAbsence` ok, `LOW_SOCIAL_COVERAGE`, failure on a stream gap.
- `SocialOriginatorTest`: reposts and quotes join the original's evidence group.
- `SocialControllersIT`: endpoints, scopes, MCP shapes.

## Acceptance criteria

- The operator can enable any supported platform with their own credentials and budget; Bluesky's live stream works with no credentials.
- Matching posts are stored as versioned records with deletion sync per platform setting; topics, places and entities have hourly volume series with seasonal, denominator-scaled baselines and velocity findings.
- Collection health feeds a social-attention observability estimate that distinguishes silence from missing data.
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
2. **Commit** all changes with the message `135: Social Sources And Volume`. Capture the full hash with `git rev-parse HEAD`; this is the *implementation commit*.
3. **Write** `completion-reports/135-CR-Social-Sources-And-Volume.md` using the template in `docs/Roadmap.md` §3, including the implementation commit hash.
4. **Commit** the report with the message `135: completion report`, then run `git push origin main`.
5. End your reply with the line `Implementation commit: <hash>`.
