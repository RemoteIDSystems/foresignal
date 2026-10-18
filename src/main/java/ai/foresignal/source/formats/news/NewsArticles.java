package ai.foresignal.source.formats.news;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The {@code news-article} record (prompt 133), shared by {@code gdelt-articles} and the {@code news-feed-*} outlet
 * feeds: metadata only.
 *
 * <ul>
 *   <li>{@code externalId}: the SHA-256 of the canonical URL ({@link ArticleUrls});
 *   <li>{@code observedAt} = {@code seenAt}, {@code eventTime} = {@code publishedAt} when given, else {@code seenAt};
 *   <li>payload {@code title}, {@code url} (canonical), {@code domain}, {@code outletId} (null when unknown),
 *       {@code language}, {@code sourceCountry}, {@code seenAt}, {@code publishedAt}, {@code topicIds},
 *       {@code summary} (the feed's own summary, at most {@value #MAX_SUMMARY} characters) and {@code imageUrl} (a
 *       link only, never fetched).
 * </ul>
 *
 * <p>No body, byline or author name is ever stored (NEWS-2): an EDP provider may bind bodies as {@code article}
 * documents later (138). Analytic class {@code bulk-event}: {@value #RETENTION_DAYS} days.
 */
public final class NewsArticles {

    /** The record kind. */
    public static final String KIND = "news-article";

    /** The longest stored summary, characters. */
    public static final int MAX_SUMMARY = 500;

    /** The longest stored title, characters. */
    public static final int MAX_TITLE = 500;

    /** The analytic retention of articles ({@code bulk-event}), days. */
    public static final int RETENTION_DAYS = 90;

    /** The analytic retention of articles as a duration. */
    public static final Duration RETENTION = Duration.ofDays(RETENTION_DAYS);

    /** The source id prefix of the outlet-feed family. */
    public static final String FEED_PREFIX = "news-feed-";

    /** The overlay source id prefix of operator outlet feeds (073 overlay ids start with {@code op-}). */
    public static final String OVERLAY_FEED_PREFIX = "op-news-feed-";

    /** The GDELT articles source. */
    public static final String GDELT_SOURCE = "gdelt-articles";

    /** Every payload key, in order. */
    public static final List<String> KEYS = List.of("title", "url", "domain", "outletId", "language", "sourceCountry",
            "seenAt", "publishedAt", "topicIds", "summary", "imageUrl");

    private NewsArticles() {}

    /** True when {@code sourceId} writes news articles: {@code gdelt-articles} or a {@code news-feed-*} source. */
    public static boolean isNewsSource(String sourceId) {
        return sourceId != null && (GDELT_SOURCE.equals(sourceId) || sourceId.startsWith(FEED_PREFIX)
                || sourceId.startsWith(OVERLAY_FEED_PREFIX));
    }

    /**
     * True when {@code sourceId} may hold news-article records: a {@linkplain #isNewsSource news source}, or an external
     * EDP provider ({@code ext-…}) whose {@code article} documents a {@code news} binding converts (prompt 138).
     */
    public static boolean writesNewsArticles(String sourceId) {
        return isNewsSource(sourceId) || (sourceId != null && sourceId.startsWith(EXTERNAL_PREFIX));
    }

    /** The source-id prefix of external EDP providers (prompt 024). */
    static final String EXTERNAL_PREFIX = "ext-";

    /**
     * One article as the adapter or feed mapper read it.
     *
     * @param title the headline
     * @param url the article URL
     * @param outletId the registry outlet id, or null
     * @param language the language code, or null
     * @param sourceCountry the ISO 3166-1 alpha-2 country, or null
     * @param seenAt when the upstream saw the article
     * @param publishedAt the publication time the feed gives, or null
     * @param topicIds the attention topics it was found for
     * @param summary the feed's own summary, or null
     * @param imageUrl the image link, or null
     */
    public record Article(String title, String url, String outletId, String language, String sourceCountry,
            Instant seenAt, Instant publishedAt, List<String> topicIds, String summary, String imageUrl) {

        /** Copies the topics. */
        public Article {
            topicIds = topicIds == null ? List.of() : List.copyOf(topicIds);
        }
    }

    /** The record of an article, or null when its URL or title is not usable. */
    public static NormalizedRecord record(Article a) {
        String canonical = ArticleUrls.canonical(a.url());
        String title = clip(a.title(), MAX_TITLE);
        if (canonical == null || title == null || a.seenAt() == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(ArticleUrls.sha256(canonical), KIND)
                .observedAt(a.seenAt())
                .eventTime(a.publishedAt() != null ? a.publishedAt() : a.seenAt())
                .put("title", title)
                .put("url", canonical)
                .put("domain", ArticleUrls.domain(canonical))
                .put("outletId", a.outletId())
                .put("language", a.language())
                .put("sourceCountry", a.sourceCountry())
                .put("seenAt", a.seenAt().toString())
                .put("publishedAt", a.publishedAt() == null ? null : a.publishedAt().toString())
                .put("topicIds", a.topicIds().isEmpty() ? null : new ArrayList<>(a.topicIds()))
                .put("summary", clip(a.summary(), MAX_SUMMARY));
        String image = a.imageUrl() == null || a.imageUrl().isBlank() ? null : ArticleUrls.canonical(a.imageUrl());
        b.put("imageUrl", image);
        return b.build();
    }

    /** {@code text} stripped and cut to {@code max} characters on a code-point boundary; null for blank. */
    public static String clip(String text, int max) {
        if (text == null) {
            return null;
        }
        String t = text.strip().replaceAll("\\s+", " ");
        if (t.isEmpty()) {
            return null;
        }
        if (t.length() <= max) {
            return t;
        }
        int end = t.offsetByCodePoints(0, Math.min(max, t.codePointCount(0, t.length())));
        return t.substring(0, Math.min(end, max));
    }

    /** Declares the payload's field specs on a descriptor builder. */
    public static void declareFieldSpecs(SourceDescriptor.Builder builder) {
        Set<String> kinds = Set.of(KIND);
        builder.fieldSpec("title", new FieldSpec(FieldType.STRING, null, "Title", "Headline as published", null,
                kinds));
        builder.fieldSpec("url", new FieldSpec(FieldType.STRING, null, "Article link",
                "Canonical URL (lower-case host, no tracking parameters, no fragment); outbound link only", null,
                kinds));
        builder.fieldSpec("domain", new FieldSpec(FieldType.STRING, null, "Domain", "Outlet domain", null, kinds));
        builder.fieldSpec("outletId", new FieldSpec(FieldType.STRING, null, "Outlet",
                "Media outlet registry id (null when unknown)", null, kinds));
        builder.fieldSpec("language", new FieldSpec(FieldType.STRING, null, "Language", "ISO 639 code", null,
                kinds));
        builder.fieldSpec("sourceCountry", new FieldSpec(FieldType.STRING, null, "Source country",
                "ISO 3166-1 alpha-2 country of the outlet", null, kinds));
        builder.fieldSpec("seenAt", new FieldSpec(FieldType.TIMESTAMP, null, "Seen at",
                "When the upstream saw the article", null, kinds));
        builder.fieldSpec("publishedAt", new FieldSpec(FieldType.TIMESTAMP, null, "Published at",
                "Publication time when the feed gives it", null, kinds));
        builder.fieldSpec("topicIds", new FieldSpec(FieldType.ARRAY, null, "Attention topics",
                "Topics the article was found for", null, kinds));
        builder.fieldSpec("summary", new FieldSpec(FieldType.STRING, null, "Summary",
                "The feed's own summary, at most 500 characters; never the article body", null, kinds));
        builder.fieldSpec("imageUrl", new FieldSpec(FieldType.STRING, null, "Image link",
                "Link only; never fetched by Foresignal", null, kinds));
    }
}
