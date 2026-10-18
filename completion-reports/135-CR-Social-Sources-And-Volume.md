# 135-CR-Social-Sources-And-Volume

| | |
|---|---|
| Prompt | 135-Social-Sources-And-Volume.md |
| Status | Partial (compile-and-report only under the owner directive of 2026-10-17: tests written, **not run**; Bluesky Network Replay is planned but not applied, and the Reddit listing fields and X field naming are unverified until a real call) |
| Implementation commit | `1ef57fb3d1275dd7be2ed6bc6d31fd55f180faf0` |
| Date | 2026-10-18 |

## Summary

This prompt adds public social media as a signal. Every platform is an adapter that the operator enables with their own
credentials and budget. All adapters are off by default except the open Bluesky Jetstream firehose. Matching posts
become versioned `social-post` records. Tracked topics, places and entities get hourly mention-volume series with
seasonal baselines scaled by platform-language denominators. Collection health feeds a `social-attention`
observability family, so silence is told apart from missing data.

The code lives in the new package `ai.foresignal.social`, eight adapters under `source/adapters/{bluesky, mastodon,
reddit, youtube, x, threads, instagram}`, shared parsing in `source/formats/social`, a controller and two MCP tools. It
compiles. **The tests are written but were not run** (owner directive 2026-10-17), so no test result, coverage figure
or guard result in this report is measured.

- **Post model:**
  - `SocialPost` holds the author, refs, media, engagement and geo. `SocialPosts` builds the `social-post` record:
    `externalId` `{platform}:{postId}`, `observedAt` and `eventTime` set to the creation time.
  - The payload follows the prompt. `observedAt` and `engagement` are volatile, except on YouTube, where tracked
    videos' statistics are its complete path. `geom` comes only from platform geo. Text is capped at 5,000 characters.
  - `AuthorKeys` computes the author key as HMAC-SHA256 of `{platform}:{accountId}` under the instance social salt.
    `SocialSalt` keeps that salt in `social_salt` (32 random bytes, encrypted with `CredentialCipher`, never exported).
  - Analytic class `social`, 30 days by default. Each platform's `postRetention` is applied as its source's retention
    override.
  - Deletion produces a tombstoned version: text, links and media links are removed and `deleted: true` is set, while
    author, counts and topics are kept (`SocialPosts.tombstoned`).
  - Originators: `OriginatorRegistry` synthesizes `social:{platform}:{authorKey}` with class `social`. Seven platform
    originators are added to `intel/originators.json`. `SocialOriginators` makes reposts and quotes inherit the
    original's originator.
- **Tracked topics:**
  - `attention_topic` gains a jsonb `social` column. `SocialTopicBlock` holds `terms` per language (≤ 50 in total),
    `hashtags` (≤ 30), `accounts` `{platform, handle}` (≤ 100), `channels` `{type, id}` (≤ 100), `exclude`,
    `platforms`, `placeIds`, `regionCodes`, `entityRefs` and `mode` (`standing | cue-only`). `AttentionTopicService`
    validates and stores it and publishes `AttentionTopicsChangedEvent`.
  - Term matching: `TermIndex` is an Aho–Corasick matcher (`AhoCorasick`) over folded text (`TextFolding`: case,
    diacritics, word boundaries per script, Arabic and Hebrew proclitics, unspaced scripts matched anywhere).
    `TermExpansion` adds place, region and entity names and aliases as terms. `TermIndexService` rebuilds the index
    after each topic change commits.
- **Collection:**
  - `SocialCollector` handles the language (the platform tag, else Lingua 1.2.2 among `social.languages` in
    `SocialLanguages`), the author key, and the denominator (only streams that carry the whole platform). It also does
    topic matching, count-once (a post seen again stores a new version but adds no counts), amplification (reposts,
    quotes and replies of recently matched posts) and region coverage. Deletions are checked against a warm cache of
    stored ids (`StoredPosts`).
  - `VolumeAccumulator` drains into `SocialVolumeStore`: `social_volume_hourly`, `social_denominator_hourly` and
    `social_coverage`.
- **Adapters:**
  - `bluesky-jetstream` is a Jetstream v2 WebSocket (subprotocol `xrpc.v1.json`, posts and reposts). The cursor is the
    event `seq`, resumed within 35 h; gaps are recorded in `social_stream_gap`. Delete events are tombstoned.
    `JetstreamReplay` calls `planSnapshot` only.
  - `bluesky-search` uses `searchPosts` with `sort=latest` (paged) on `api.bsky.app`, or a session on `bsky.social`.
    Deletion checks run daily through `getPosts`.
  - `mastodon` covers each configured instance: hashtag streams, `trends/tags` turned into daily `series-point`
    records, and account statuses. It needs a user token.
  - `reddit` uses client-credentials OAuth, `/r/{sub}/new` and `/search`, 100 queries per minute enforced locally,
    `X-Ratelimit-Remaining` honored, and `/api/info` for deletion checks.
  - `youtube` uses `search.list` (sampled) and `videos.list` statistics, with the separate quota bucket of 100
    searches a day plus 10,000 units.
  - `x` covers hourly `counts/recent` per standing topic, `search/recent`, deletion lookups and optional full archive.
    Each call is estimated and charged by `XSpend`.
  - `threads` uses `keyword_search`, with a quota of 2,200 per rolling 24 h.
  - `instagram-hashtags` uses `ig_hashtag_search` and `recent_media`, within 30 hashtags per rolling 7 days.
  - Quotas are counted locally (`QuotaLedger`, `QuotaUnits`, `social_quota_use`). Platform settings live in
    `social_platform` (`SocialPlatformService`, `SocialPlatformCatalog`, `SocialPlatformSettings`). `bluesky-jetstream`
    is enabled when its row is first created and `foresignal.social.default-enable` is on.
- **Spend (SOC-4):** `XSpend` estimates each call from the price table before it is sent (seeded with X's prices of
  2026-10-17). It refuses with `BudgetExceededException` / `BudgetExceededProblem` (`monthlyCapUsd`, `spentThisMonth`,
  `estimateUsd`), charges the actual cost to `social_spend` and implements the new `collect/SourceSpendLookup` SPI, so
  `SourceHealth.spentUsdThisMonth` shows the month's spend. The monthly estimate for standing counts is shown on the
  platform.
- **Baselines and velocity:** `SocialBaseline` 1.0.0 compares each hour with the same hour of the week over 8 weeks
  (at least 4 samples), takes median and MAD over denominator-scaled rates, and floors the baseline at 1 and sigma at
  √baseline. `VelocityDetector` records `social-velocity` findings: ratio ≥ 5 and ≥ 20 distinct authors (setting
  `social.velocity`), severity significant at z ≥ 8 and notable at z ≥ 4, deduplicated per scope × platform for 6 h.
  Sampled paths are never baselined.
- **Series (119):** `SeriesRef.Type.SOCIAL` covers `social:{topic|place|entity}:{id}/{platform}/{metric}[/{lang}]` and
  `social:all/{platform}/denominator`. `SeriesReader` reads them, and `AnalysisSeriesRegistrar` registers scopes once
  they have 168 hours.
- **Observability (116):** `SocialObservability`, delegated from `ObservabilityService` for family `social-attention`
  (method `platform-volume`), answers ok only when a complete stream was healthy over the window and the region's
  8-week mean is at least `social.minRegionBaseline` (20). Otherwise it answers `LOW_SOCIAL_COVERAGE` or
  `SOCIAL_STREAM_GAP`. `SocialHealthDetails` adds stream lag, cursor and gaps to source health.
- **API, MCP and SSE:**
  - `api/SocialController` serves the seven operations, with matching AccessPolicy rows. New Problem codes
    `UNKNOWN_PLATFORM` (404) and `PLATFORM_DISABLED` (409); `BUDGET_EXCEEDED` is reused.
  - MCP adds `get_social_volume` and `search_social_posts` (read; also in the analyst's read catalog).
  - SSE topic `social` (`SocialTopic`, cursor key `o`, FIFO from `social_event_log`) carries `social.velocity`, plus an
    id-less `social.platform.state`.
  - App settings `social.languages`, `social.minRegionBaseline` and `social.velocity`. Properties
    `foresignal.social.scheduled` and `foresignal.social.default-enable`, both off in tests.
- **Spec and docs:**
  - `docs/OpenAPI.json`: the seven operations are marked implemented (removed from `pending-operations.txt`, 50 lines
    left), with additive reconciliations and a changelog entry. Postman regenerated ("844 requests across 80 tag
    folders; quickstart 12 steps; covers all ops: True").
  - `THIRD_PARTY_NOTICES.md` adds Lingua and a social platform data table.
  - `provenance/licences.json` adds six entries.
  - `docs/Source-Catalog.md` (the "Social platforms" section as built) and `docs/Operator-Guide.md` (new "Social
    signals (prompt 135)" section, plus the attention-topic paragraph) were each read end to end and written whole by
    script.

## Files created / modified

**Created (main):**
- `social/`: `package-info`, `SocialVocabulary`, `SocialPost`, `SocialPosts`, `AuthorKeys`, `SocialSalt`,
  `SocialLanguages`, `TextFolding`, `AhoCorasick`, `SocialTopicBlock`, `TermIndex`, `TermExpansion`,
  `TermIndexService`, `VolumeAccumulator`, `SocialVolumeStore`, `StreamLedger`, `SocialPlatformCatalog`,
  `SocialPlatformSettings`, `SocialPlatformService`, `QuotaUnits`, `QuotaLedger`, `XSpend`, `StoredPosts`,
  `SocialCollector`, `SocialBaseline`, `VelocityDetector`, `SocialObservability`, `SocialEvents`, `SocialTopic`,
  `SocialFilter`, `SocialJobs`, `SocialRuntime`, `SocialOriginators`, `SocialViews`, `SocialHealthDetails`.
- Entities and repositories: `SocialSaltEntity` and repo, `SocialVolumeHourlyEntity`, `SocialDenominatorHourlyEntity`,
  `SocialCoverageEntity`, `SocialStreamGapEntity`, `SocialSpendEntity`, `SocialQuotaUseEntity`, `SocialPlatformEntity`
  and repo, `SocialEventLogEntity` and repo.
- Adapters: `bluesky/` (`BlueskyJetstreamAdapter`, `JetstreamSession`, `JetstreamReplay`, `BlueskySearchAdapter`,
  `BlueskyRecords`), `mastodon/` (`MastodonAdapter`, `MastodonSession`, `MastodonStatuses`), `reddit/RedditAdapter`,
  `youtube/YouTubeAdapter`, `x/` (`XAdapter`, `XPosts`), `threads/ThreadsAdapter`,
  `instagram/InstagramHashtagsAdapter`, each with a `package-info`.
- `source/formats/social/` (`SocialDescriptors`, `SocialJson`, `SocialHtml`, `SocialQueries`, `package-info`).
- `api/SocialController`, `mcp/tools/GetSocialVolumeTool`, `mcp/tools/SearchSocialPostsTool`,
  `collect/SourceSpendLookup`, `attention/AttentionTopicsChangedEvent`.

**Modified (main):**
- `analyst/AnalystRunner`.
- `analytics/AnalyticsSupport`, `analytics/registry/AnalysisSeriesRegistrar`, `analytics/series/SeriesReader` and
  `SeriesRef`.
- `api/dto/SourceHealthDto`, `api/error/ErrorCodes`, `api/mapper/HealthMapper`.
- `attention/AttentionTopicEntity`, `AttentionTopicInput`, `AttentionTopicService`.
- `collect/BoundSourceHttp` (WebSocket subprotocol header), `HealthService`, `SourceHealthView`.
- `evidence/OriginatorRegistry`, `observability/ObservabilityService`, `security/AccessPolicy`,
  `settings/SettingKeys`.
- Resources: `application.yml`, `intel/originators.json`, `provenance/licences.json`.
- Build: `gradle/libs.versions.toml` and `build.gradle.kts` (Lingua 1.2.2).

**Docs, spec and tooling:** `docs/OpenAPI.json`, `docs/Source-Catalog.md`, `docs/Operator-Guide.md`,
`THIRD_PARTY_NOTICES.md`, `scripts/generate-postman.py`, `postman/Foresignal.postman_collection.json`,
`src/test/resources/openapi/pending-operations.txt` (−7).

**Tests:**
- The classes in the table below, plus the helpers `testsupport/social/SocialStreamContext` and `SocialTestSupport`.
- Modified: `retention/FamilyDefaultsTest` (the eight adapters are class `social`),
  `stream/sse/EventSchemaConformanceTest` (`social.velocity` and `social.platform.state`), and test
  `application-test.yml` (both social properties off).

**In the report commit:** small corrections to `docs/Operator-Guide.md` and `docs/Source-Catalog.md` made after the
implementation commit:
- the topic `social` block shape: `exclude`, `accounts {platform, handle}`, `channels {type, id}`, and 50 terms in
  total;
- the platform state names;
- the Instagram credential `userId`;
- `PLATFORM_DISABLED` being reserved for 137.

## Deviations from the prompt

1. **Jetstream cursor is the v2 event `seq`, not `time_us`.** Jetstream v2 (verified 2026-10-17 on
   bsky.network/docs/jetstream) frames events as `{"$type":"message","payload":{…}}` and resumes by `seq`. A stored
   value ≥ 10^15 is still treated as `time_us`, and the v1 frame shape is parsed too. Hosted instances keep 36 h, so a
   cursor older than 35 h connects live and records the gap as not replayed.
2. **Network Replay is planned, not applied.** `planSnapshot` is called and its plan reported in the gap reason.
   `listSegments` and `getSegment` are not used because the segment byte format could not be verified. This is the
   main reason for **Partial**.
3. **Reddit uses application-only OAuth** (`client_credentials`) with the operator's app, not a script app with a user
   password. Reddit's listing field names are not in its published reference, so the fixtures are synthetic. The
   100 QPM figure is the commonly published one and is not on Reddit's own pages (an archived page says 60).
4. **X field naming is unverified.** The live OpenAPI (v2.172) says `post.fields` and `post_count`; the data dictionary
   says `tweet.fields` and `tweet_count`. Both answers are read, and option `fieldsParam` (default `post`) picks the
   request parameter.
5. **X search is treated as sampled** (ranked by the API within `max_results`). Hourly counts are the complete X path,
   written to language bucket `other`.
6. **YouTube comment threads are not read** (option `comments` is accepted): their response fields were not verified.
7. **Media URLs for Jetstream posts** point to the post's web page. Blob CDN URLs are not guessed. Search post views
   give real `fullsize`/`thumb`/`playlist` URLs (lexicons verified 2026-10-18).
8. **Mastodon trends** become daily `series-point` records (`mastodon-trend:{instance}:{tag}`, family `attention`), not
   volume series. Hashtag streams are complete for their filter but are **not** denominators (the collector's
   `denominator` flag), so zero-denominator rows never scale baselines.
9. **Velocity is computed per scope × platform with languages summed**, and series carry an optional language suffix in
   `SeriesRef`. The baseline is floored at 1 post and sigma at √baseline (Poisson floor).
10. **Region coverage** is attributed from platform geo and from the region names the topics track. Prompt 136 adds
    text-derived places.
11. **Counting happens at collection.** There is no separate durable `social.cursor` over `record_version`: counts are
    accumulated in memory, flushed every minute and closed hourly, and a post is counted once (bounded cache, 3 days).
    Counts in the last unflushed minute can be lost on a crash.
12. **Licenses:** redistribution is `FORBIDDEN` for X, Reddit, YouTube, Threads and Instagram, and `LOCAL_ONLY` for
    Bluesky and Mastodon (`provenance/licences.json`; the aliases match the descriptor license strings).
13. **No `intel/observability.json` entries** for the social adapters: they are observed through the
    `social-attention` family instead.
14. **Additions not named by the prompt:**
    - `BoundSourceHttp` sends a WebSocket subprotocol header, which Jetstream needs.
    - `SourceHealth.spentUsdThisMonth`, through the `SourceSpendLookup` SPI.
    - `social_event_log` backs the SSE topic (cursor key `o`).
15. **Spec reconciliations (additive):** `SeriesRef` documents the social forms, `SocialPlatformSettings.options` gains
    `maxResults` and `fieldsParam`, and the `mode` example is `counts`.
16. **`PLATFORM_DISABLED`** is defined (and `SocialPlatformService.requireEnabled` exists) but no 135 operation raises
    it. The spec uses it for the 137 cue operation.
17. **Edited by script:** the spec, as in earlier prompts. The two documents were read end to end and written whole by
    script.

## Issues found / open questions

**Owner decisions:**
- (a) Ship Network Replay as plan-only until a segment can be captured (Deviation 2), or drop the option.
- (b) Reddit client credentials versus a script app (Deviation 3).
- (c) Keep `fieldsParam` defaulting to `post` (Deviation 4).
- (d) Licenses: `LOCAL_ONLY` for Bluesky and Mastodon, `FORBIDDEN` for the rest (Deviation 12).
- (e) Counting at collection without a durable cursor (Deviation 11).
- (f) Jetstream on by default at first seeding (`foresignal.social.default-enable`), as the prompt asks.

**Unverified until a real call** (no credentials were available):
- Reddit listing fields and rate figure.
- X field and count naming, and actual billing.
- Threads paging (undocumented) and the absence of an author id.
- Instagram `recent_media` fields.
- YouTube's separate search bucket accounting.
- Mastodon streaming against a live instance.
- The Jetstream adapter was not connected to the live service.

**Known limits:**
- The count-once and amplification caches are in memory (bounded).
- Region coverage depends on topic region names until 136.
- Lingua loads only the configured languages' low-accuracy models. The jar is about 80 MB, which adds to the image.

**Schema for prompt 144:**
- Hibernate tables: `social_salt`, `social_platform`, `social_volume_hourly`, `social_denominator_hourly`,
  `social_coverage`, `social_stream_gap`, `social_spend`, `social_quota_use` and `social_event_log`.
- New column: `attention_topic.social` (jsonb).
- App settings: `social.languages`, `social.minRegionBaseline` and `social.velocity`.
- Properties: `foresignal.social.scheduled` and `foresignal.social.default-enable`.

**Guards not run:**
- Not run, under the no-tests directive: the US English guard, `lintOpenApi` and every contract test
  (`AccessPolicySyncTest`, `OpenApiContractIT`, `EventSchemaConformanceTest`, `RestrictedReadCoverageTest`).
- New prose and code were checked by grep for British spellings; "catalogued" and "summarised" were corrected.
  `favourites_count` (a Mastodon API field) and the existing `licence` keys, resource names and builder method were
  left as they are, because they cross a wire or persist.

**Risks for the owner's first test run:**
- `PlatformAdaptersTest` runs on WireMock with synthetic fixtures (Reddit and Threads especially).
- `JetstreamAdapterTest` uses a local WebSocket stub (`SocialStreamContext`).
- `SocialObservabilityTest` seeds hourly rows directly.
- `XSpendTest` uses the seeded prices.

**External data and instructions:**
- Upstream documentation was read on 2026-10-17 and 2026-10-18: Jetstream, the atproto lexicons, Mastodon, Reddit,
  YouTube, X (pricing page and OpenAPI), Threads and the Instagram Graph API, plus Maven Central metadata for Lingua.
- No page, file or tool output contained instructions directed at the agent.

## Tests written (not run)

| Test | What it covers |
|---|---|
| `source/adapters/bluesky/JetstreamAdapterTest` | Post and repost parsing, term matching and denominators; `seq` cursor resume; a too-old cursor recorded as an unreplayed gap; delete events tombstone; reconnect cutover without gaps or duplicates; v1 frame shape; descriptor |
| `social/TermIndexTest` | Folding; word boundaries in Latin, Arabic (proclitics), Hebrew and Cyrillic; unspaced scripts; hashtags, accounts, channels, excludes; platforms and mode; place, region and entity expansion; validation; the automaton |
| `social/PlatformAdaptersTest` | Mastodon trends history; Reddit paging and rate limits; YouTube unit and search-bucket accounting; Threads quota; Instagram 30-hashtag window |
| `social/XSpendTest` | Estimates before calls, reserve and charge, counts billed per request, refusal at the cap, search charged per post |
| `social/SocialBaselineTest` | Hour-of-week baseline; denominator scaling removes a planted platform-wide drop; minimum samples; sampled series excluded; velocity thresholds; finding record and dedup |
| `social/SocialObservabilityTest` | `canSeeAbsence` ok; `LOW_SOCIAL_COVERAGE`; failure on a stream gap; incomplete hours |
| `social/SocialOriginatorTest` | Reposts and quotes join the original's evidence group; unseen originals and chains; registry answers `social:` ids |
| `social/SocialControllersIT` | Platform list, view and update; volume, coverage and posts; the topic `social` block; scopes; MCP tool shapes |
| `retention/FamilyDefaultsTest`, `stream/sse/EventSchemaConformanceTest` (added cases) | Class `social` defaults; `social.velocity` and `social.platform.state` payloads |

## Compile result

- `./gradlew compileJava compileTestJava` on the committed tree: exit 0.
- `python3 scripts/generate-postman.py docs/OpenAPI.json postman`: "844 requests across 80 tag folders; quickstart 12
  steps; covers all ops: True".

## Notes for the next prompt

- **136 (claims and narratives):** posts carry `media` links, `authorKey`, `topicIds` and `collection`. Add
  text-derived places to `social_coverage` through `SocialVolumeStore` (`coverage`), never to `geom`.
- **137 (cues):** use the `cue` collection paths of `bluesky-search`, `reddit`, `x` and `threads`,
  `SocialPlatformService.requireEnabled` (`PLATFORM_DISABLED`), `XSpend.reserve` for money, and `cueId` on posts. The
  SSE topic `social` is where 136 and 137 add their events.
- **144 (Flyway):** capture the nine tables and the new column above.
- **Tests:** use `SocialTestSupport` and `SocialStreamContext`.

Tests were written but **NOT run** (owner directive 2026-10-17).

Implementation commit: `1ef57fb3d1275dd7be2ed6bc6d31fd55f180faf0`
