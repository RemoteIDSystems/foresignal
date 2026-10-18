package ai.foresignal.social;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The built-in social platform adapters (prompt 135, docs/Source-Catalog.md "Social platforms"): what each one is,
 * how it collects, what it costs and what its terms say, so the operator can decide. Every adapter but
 * {@code bluesky-jetstream} ships disabled; nothing is hard-blocked.
 */
public final class SocialPlatformCatalog {

    /**
     * One collection path.
     *
     * @param path {@code stream}, {@code replay}, {@code search}, {@code counts}, {@code trends},
     *     {@code channel-poll}, {@code account-poll}, {@code hashtag-search} or {@code cue}
     * @param collectionType {@code complete}, {@code paged} or {@code sampled}
     * @param option the {@code options} member that switches it on, or null when it is always on
     */
    public record PathDef(String path, String collectionType, String option) {}

    /**
     * One quota the platform imposes.
     *
     * @param unit {@code requests}, {@code units}, {@code queries}, {@code posts} or {@code hashtags}
     * @param limit the limit, or null when unknown
     * @param window the ISO-8601 window
     * @param rolling true for a rolling window
     * @param description what it is
     */
    public record QuotaDef(String unit, Long limit, String window, boolean rolling, String description) {}

    /**
     * One operator-editable price.
     *
     * @param item the billable item
     * @param usd US dollars
     * @param per the units the price covers
     */
    public record Price(String item, BigDecimal usd, int per) {}

    /**
     * One adapter.
     *
     * @param id the adapter (source) id
     * @param network the network
     * @param name the display name
     * @param type {@code STREAMING}, {@code POLLING}, {@code STREAMING+POLLING} or {@code CUE}
     * @param defaultEnabled true only for {@code bluesky-jetstream}
     * @param defaultMode the default mode, or null
     * @param paths the collection paths
     * @param credentialFields the credential field names
     * @param credentialsRequired true when the adapter cannot run without credentials
     * @param deletionSyncDefault the default deletion sync
     * @param termsSummary the terms summary
     * @param termsUrl the terms URL
     * @param costs the cost summary
     * @param quotas the quotas
     * @param defaultMonthlyCapUsd the default money cap, or null for free platforms
     * @param defaultPrices the seeded prices
     * @param pricesAsOf the date the prices were taken from the pricing page, or null
     */
    public record Definition(String id, String network, String name, String type, boolean defaultEnabled,
            String defaultMode, List<PathDef> paths, List<String> credentialFields, boolean credentialsRequired,
            boolean deletionSyncDefault, String termsSummary, String termsUrl, String costs, List<QuotaDef> quotas,
            BigDecimal defaultMonthlyCapUsd, List<Price> defaultPrices, String pricesAsOf) {

        /** The path definition of a path, if the adapter has it. */
        public Optional<PathDef> path(String name) {
            return paths.stream().filter(p -> p.path().equals(name)).findFirst();
        }
    }

    /** The X prices' source date (docs.x.com/x-api/getting-started/pricing, read 2026-10-17). */
    public static final String X_PRICES_AS_OF = "2026-10-17";

    /** X: a post read. */
    public static final String POST_READ = "post-read";

    /** X: a user read. */
    public static final String USER_READ = "user-read";

    /** X: one {@code counts/recent} request. */
    public static final String COUNTS_RECENT_REQUEST = "counts-recent-request";

    /** X: one {@code counts/all} request. */
    public static final String COUNTS_ALL_REQUEST = "counts-all-request";

    private static final Map<String, Definition> ALL = build();

    private SocialPlatformCatalog() {}

    /** Every adapter, in the spec's order. */
    public static List<Definition> all() {
        return List.copyOf(ALL.values());
    }

    /** One adapter. */
    public static Optional<Definition> get(String id) {
        return Optional.ofNullable(id == null ? null : ALL.get(id));
    }

    private static Map<String, Definition> build() {
        Map<String, Definition> m = new LinkedHashMap<>();
        m.put(SocialVocabulary.BLUESKY_JETSTREAM, new Definition(SocialVocabulary.BLUESKY_JETSTREAM,
                SocialVocabulary.BLUESKY, "Bluesky Jetstream (live)", "STREAMING", true, null,
                List.of(new PathDef("stream", SocialVocabulary.COMPLETE, null),
                        new PathDef("replay", SocialVocabulary.COMPLETE, "replay")),
                List.of("apiToken"), false, true,
                "Bluesky's public Jetstream: the whole network's posts and reposts, open, no account needed. Network"
                        + " Replay backfill needs the operator's API token from bsky.network/account. Delete events are"
                        + " honored by deletion sync.",
                "https://bsky.network/docs/jetstream/", "free; replay metered by the operator's API key",
                List.of(new QuotaDef("requests", null, "PT36H", true,
                        "Bluesky-hosted Jetstream keeps a 36-hour cursor window")),
                null, List.of(), null));
        m.put(SocialVocabulary.BLUESKY_SEARCH, new Definition(SocialVocabulary.BLUESKY_SEARCH,
                SocialVocabulary.BLUESKY, "Bluesky search", "POLLING", false, null,
                List.of(new PathDef("search", SocialVocabulary.PAGED, null),
                        new PathDef("cue", SocialVocabulary.SAMPLED, null)),
                List.of("identifier", "appPassword"), false, true,
                "AppView app.bsky.feed.searchPosts with sort=latest for topics and cues; some AppViews require an"
                        + " authenticated session (an app password).",
                "https://bsky.social/about/support/tos", "free",
                List.of(), null, List.of(), null));
        m.put(SocialVocabulary.MASTODON_ADAPTER, new Definition(SocialVocabulary.MASTODON_ADAPTER,
                SocialVocabulary.MASTODON, "Mastodon", "STREAMING+POLLING", false, null,
                List.of(new PathDef("stream", SocialVocabulary.COMPLETE, null),
                        new PathDef("trends", SocialVocabulary.COMPLETE, null),
                        new PathDef("account-poll", SocialVocabulary.PAGED, null)),
                List.of("accessToken"), true, false,
                "Per configured instance: hashtag streams (a user token with read:statuses is required since"
                        + " Mastodon 4.2), /api/v1/trends/tags hourly and followed accounts' statuses. Each instance's"
                        + " own rules apply.",
                "https://docs.joinmastodon.org/methods/streaming/", "free; 300 requests per 5 minutes per account",
                List.of(new QuotaDef("requests", 300L, "PT5M", true, "Mastodon default per account and per IP")),
                null, List.of(), null));
        m.put(SocialVocabulary.REDDIT_ADAPTER, new Definition(SocialVocabulary.REDDIT_ADAPTER,
                SocialVocabulary.REDDIT, "Reddit", "POLLING", false, null,
                List.of(new PathDef("channel-poll", SocialVocabulary.PAGED, null),
                        new PathDef("search", SocialVocabulary.PAGED, null),
                        new PathDef("cue", SocialVocabulary.SAMPLED, null)),
                List.of("clientId", "clientSecret", "contactEmail"), true, false,
                "Reddit Data API Terms: an OAuth app the operator registers (approval is manual), non-commercial use,"
                        + " a descriptive User-Agent (Foresignal's own, with the operator's contact, for example"
                        + " /u/name). Application-only OAuth (client credentials); /r/{sub}/new per channel and"
                        + " /search for topics and cues.",
                "https://redditinc.com/policies/data-api-terms",
                "free tier; the rate limit is reported in X-Ratelimit headers (100 queries per minute per client is the"
                        + " commonly published figure)",
                List.of(new QuotaDef("queries", 100L, "PT1M", true, "per OAuth client, shared by every call")),
                null, List.of(), null));
        m.put(SocialVocabulary.YOUTUBE_ADAPTER, new Definition(SocialVocabulary.YOUTUBE_ADAPTER,
                SocialVocabulary.YOUTUBE, "YouTube", "POLLING", false, null,
                List.of(new PathDef("search", SocialVocabulary.SAMPLED, null),
                        new PathDef("channel-poll", SocialVocabulary.COMPLETE, null)),
                List.of("apiKey"), true, false,
                "YouTube API Services Terms. search.list (order=date) is a ranked sample; videos.list keeps tracked"
                        + " videos' statistics current; comment threads optionally.",
                "https://developers.google.com/youtube/terms/api-services-terms-of-service",
                "free: 10,000 units per day (videos.list and commentThreads.list 1 unit each) and, since 2026-06,"
                        + " search.list in its own bucket of 100 calls per day",
                List.of(new QuotaDef("units", 10_000L, "P1D", false, "Data API default quota, resets daily"),
                        new QuotaDef("queries", 100L, "P1D", false, "search.list calls (Search Queries bucket)")),
                null, List.of(), null));
        m.put(SocialVocabulary.X_ADAPTER, new Definition(SocialVocabulary.X_ADAPTER, SocialVocabulary.X, "X",
                "POLLING", false, "counts",
                List.of(new PathDef("counts", SocialVocabulary.COMPLETE, "standingCounts"),
                        new PathDef("search", SocialVocabulary.SAMPLED, null),
                        new PathDef("cue", SocialVocabulary.SAMPLED, null)),
                List.of("bearerToken"), true, true,
                "X Developer Agreement and Policy: stored content must be deleted or modified within 24 hours of a"
                        + " deletion or change on X. Pay-per-use: hourly counts for standing topics (billed per"
                        + " request), recent search for cues and sampled standing topics (billed per post returned).",
                "https://docs.x.com/developer-terms/policy",
                "pay-per-use: $0.005 per post read, $0.005 per counts/recent request, $0.010 per counts/all request,"
                        + " $0.010 per user read (pricing page read 2026-10-17); monthly cap social.x.monthlyCapUsd",
                List.of(new QuotaDef("posts", 3_000_000L, "P1M", false, "X post reads per month")),
                new BigDecimal("25"), List.of(new Price(POST_READ, new BigDecimal("0.005"), 1),
                        new Price(COUNTS_RECENT_REQUEST, new BigDecimal("0.005"), 1),
                        new Price(COUNTS_ALL_REQUEST, new BigDecimal("0.010"), 1),
                        new Price(USER_READ, new BigDecimal("0.010"), 1)), X_PRICES_AS_OF));
        m.put(SocialVocabulary.THREADS_ADAPTER, new Definition(SocialVocabulary.THREADS_ADAPTER,
                SocialVocabulary.THREADS, "Threads keyword search", "CUE", false, null,
                List.of(new PathDef("search", SocialVocabulary.PAGED, null),
                        new PathDef("cue", SocialVocabulary.SAMPLED, null)),
                List.of("accessToken"), true, true,
                "Meta Platform Terms: delete Platform Data promptly when asked and when no longer needed. keyword_search"
                        + " needs the threads_basic and threads_keyword_search permissions (App Review); without"
                        + " approval it covers only the operator's own posts.",
                "https://developers.facebook.com/terms/",
                "free; 2,200 queries per rolling 24 hours per user (queries without results do not count)",
                List.of(new QuotaDef("queries", 2_200L, "PT24H", true, "keyword_search queries per user")),
                null, List.of(), null));
        m.put(SocialVocabulary.INSTAGRAM_ADAPTER, new Definition(SocialVocabulary.INSTAGRAM_ADAPTER,
                SocialVocabulary.INSTAGRAM, "Instagram hashtag search", "POLLING", false, null,
                List.of(new PathDef("hashtag-search", SocialVocabulary.SAMPLED, null)),
                List.of("accessToken", "userId"), true, false,
                "Meta Platform Terms. ig_hashtag_search and recent_media need the Instagram Public Content Access"
                        + " feature and instagram_basic, with a user token tied to a Facebook Page; recent_media covers"
                        + " the last 24 hours.",
                "https://developers.facebook.com/terms/", "free; 30 unique hashtags per rolling 7 days",
                List.of(new QuotaDef("hashtags", 30L, "P7D", true, "unique hashtags searched per account")),
                null, List.of(), null));
        return m;
    }
}
