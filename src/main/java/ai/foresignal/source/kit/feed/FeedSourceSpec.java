package ai.foresignal.source.kit.feed;

import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.List;

/**
 * One feed-kit source, as reviewed in {@code src/main/resources/kits/feeds/*.json} (prompt 073). Bound with unknown
 * properties refused and checked by {@link FeedSpecValidator} at startup.
 *
 * @param id the source id ({@code feed-…}; overlay specs {@code op-…})
 * @param name the human-readable name
 * @param description what the feed holds
 * @param url the feed URL
 * @param kind the record kind (default {@code bulletin})
 * @param language the feed's language, when known
 * @param categories an include/exclude filter on item categories
 * @param maxItems the most items kept per fetch (default 200)
 * @param recordTtl how long an item stays current (default 30 d)
 * @param ttlAnchor {@code RECEIVED} (default) or {@code OBSERVED}
 * @param observedAt {@code updated}, {@code published} or {@code RECEIVED} (default); an item without the named date
 *     is skipped, never given the other one
 * @param interval the default poll interval (default 15 min)
 * @param minInterval the floor on the poll interval (default 5 min)
 * @param budget the default request budget
 * @param license the data license (the descriptor's {@code licence})
 * @param termsUrl the upstream's terms
 * @param attribution the credit line
 * @param homepage where a human reads about the upstream
 * @param itemMapper the name of a reviewed {@link FeedItemMapper} for source-specific parsing, or null
 * @param websub true to subscribe through WebSub when it is enabled and the feed advertises a hub
 * @param retention the default raw retention (default 1825 d, the {@code event} class)
 * @param maxResponseBytes the cap on one response (default 8 MB)
 */
public record FeedSourceSpec(
        String id,
        String name,
        String description,
        String url,
        String kind,
        String language,
        CategoryFilter categories,
        Integer maxItems,
        String recordTtl,
        String ttlAnchor,
        String observedAt,
        String interval,
        String minInterval,
        Budget budget,
        String license,
        String termsUrl,
        String attribution,
        String homepage,
        String itemMapper,
        Boolean websub,
        String retention,
        Integer maxResponseBytes) {

    /** The default kind. */
    public static final String DEFAULT_KIND = "bulletin";

    /** The default item cap. */
    public static final int DEFAULT_MAX_ITEMS = 200;

    /** The default record TTL. */
    public static final Duration DEFAULT_TTL = Duration.ofDays(30);

    /** The default poll interval. */
    public static final Duration DEFAULT_INTERVAL = Duration.ofMinutes(15);

    /** The smallest poll interval allowed. */
    public static final Duration MIN_INTERVAL = Duration.ofMinutes(5);

    /** The default raw retention: the {@code event} class (Conventions). */
    public static final Duration DEFAULT_RETENTION = Duration.ofDays(1825);

    /** The default response cap, the parser's limit. */
    public static final int DEFAULT_MAX_RESPONSE_BYTES = FeedParser.MAX_BYTES;

    /** {@code observedAt}: the item's {@code updated} date. */
    public static final String UPDATED = "updated";

    /** {@code observedAt}: the item's {@code published} date. */
    public static final String PUBLISHED = "published";

    /** {@code observedAt}: when Foresignal received it. */
    public static final String RECEIVED = "RECEIVED";

    /** Null filters become empty. */
    public FeedSourceSpec {
        categories = categories == null ? CategoryFilter.NONE : categories;
    }

    /** The kind. */
    public String effectiveKind() {
        return kind == null || kind.isBlank() ? DEFAULT_KIND : kind;
    }

    /** The item cap. */
    public int effectiveMaxItems() {
        return maxItems == null ? DEFAULT_MAX_ITEMS : maxItems;
    }

    /** The {@code observedAt} rule. */
    public String effectiveObservedAt() {
        return observedAt == null || observedAt.isBlank() ? RECEIVED : observedAt;
    }

    /** The parsed record TTL. */
    public Duration recordTtlDuration() {
        return parse(recordTtl, DEFAULT_TTL);
    }

    /** The parsed default interval. */
    public Duration intervalDuration() {
        return parse(interval, DEFAULT_INTERVAL);
    }

    /** The parsed minimum interval. */
    public Duration minIntervalDuration() {
        return parse(minInterval, MIN_INTERVAL);
    }

    /** The parsed retention. */
    public Duration retentionDuration() {
        return parse(retention, DEFAULT_RETENTION);
    }

    /** The response cap. */
    public int effectiveMaxResponseBytes() {
        return maxResponseBytes == null ? DEFAULT_MAX_RESPONSE_BYTES : maxResponseBytes;
    }

    /** True when WebSub is requested. */
    public boolean wantsWebSub() {
        return Boolean.TRUE.equals(websub);
    }

    static Duration parse(String text, Duration fallback) {
        return text == null || text.isBlank() ? fallback : Durations.parse(text);
    }

    /**
     * Which items are kept by category, compared case-insensitively.
     *
     * @param include when non-empty, only items with at least one of these categories are kept
     * @param exclude items with any of these categories are dropped
     */
    public record CategoryFilter(List<String> include, List<String> exclude) {

        /** No filter. */
        public static final CategoryFilter NONE = new CategoryFilter(null, null);

        /** Null lists become empty. */
        public CategoryFilter {
            include = include == null ? List.of() : List.copyOf(include);
            exclude = exclude == null ? List.of() : List.copyOf(exclude);
        }

        /** True when an item with {@code categories} is kept. */
        public boolean keeps(List<String> categories) {
            List<String> lower = categories.stream().map(c -> c.toLowerCase(java.util.Locale.ROOT)).toList();
            if (!include.isEmpty()
                    && include.stream().noneMatch(i -> lower.contains(i.toLowerCase(java.util.Locale.ROOT)))) {
                return false;
            }
            return exclude.stream().noneMatch(e -> lower.contains(e.toLowerCase(java.util.Locale.ROOT)));
        }
    }

    /**
     * The default request budget.
     *
     * @param perMinute requests per minute
     * @param perDay requests per day
     * @param mbPerDay megabytes per day, or null
     */
    public record Budget(Integer perMinute, Integer perDay, Integer mbPerDay) {}
}
