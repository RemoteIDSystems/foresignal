package ai.foresignal.social;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code social-post} record (prompt 135, OpenAPI {@code SocialPostPayload}): building it from a
 * {@link SocialPost}, the tombstoned version deletion sync writes, and reading a stored payload back.
 *
 * <p>The record's {@code observedAt} and {@code eventTime} are the platform's creation time, so the same post seen
 * twice hashes the same; the payload's {@code observedAt} (when Foresignal saw it) and {@code engagement} are volatile
 * fields, so a changed text is a new version and a changed like count is not. {@code geom} comes only from platform
 * geo; text-derived places (136) never become geometry.
 */
public final class SocialPosts {

    /** Payload fields excluded from version hashing. */
    public static final List<String> VOLATILE_FIELDS = List.of("observedAt", "engagement");

    /** Payload fields searched as text. */
    public static final List<String> SEARCH_FIELDS = List.of("text", "hashtags", "channel");

    private static final Pattern HASHTAG = Pattern.compile("(?<![\\p{L}\\p{N}_&])#([\\p{L}\\p{N}_]{1,100})");
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"']{3,2000}");

    private SocialPosts() {}

    /**
     * The record of a post.
     *
     * @param post the post (its language already detected when the platform gave none)
     * @param authorKey the author's HMAC key ({@link AuthorKeys})
     * @param topicIds the attention topics it matched
     * @param observedAt when Foresignal saw it
     */
    public static NormalizedRecord record(SocialPost post, String authorKey, List<String> topicIds,
            Instant observedAt) {
        Map<String, Object> payload = payload(post, authorKey, topicIds, observedAt);
        Instant created = post.createdAt() != null ? post.createdAt() : observedAt;
        NormalizedRecord.Builder b = NormalizedRecord.builder(post.externalId(), SocialVocabulary.KIND)
                .observedAt(created)
                .eventTime(created)
                .putAll(payload);
        if (post.geo() != null && post.geo().located()) {
            b.point(post.geo().lon(), post.geo().lat());
        }
        return b.build();
    }

    /** The payload of a post (OpenAPI {@code SocialPostPayload}). */
    public static Map<String, Object> payload(SocialPost post, String authorKey, List<String> topicIds,
            Instant observedAt) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("platform", post.platform());
        p.put("postId", post.postId());
        p.put("url", post.url());
        p.put("author", author(post.author()));
        p.put("authorKey", authorKey);
        p.put("text", clip(post.text()));
        p.put("language", post.language());
        Instant created = post.createdAt() != null ? post.createdAt() : observedAt;
        p.put("createdAt", created.toString());
        p.put("observedAt", observedAt.toString());
        p.put("replyTo", ref(post.replyTo()));
        p.put("quoteOf", ref(post.quoteOf()));
        p.put("repostOf", ref(post.repostOf()));
        p.put("links", distinct(post.links().isEmpty() ? links(post.text()) : post.links()));
        p.put("hashtags", distinct(post.hashtags().isEmpty() ? hashtags(post.text()) : post.hashtags()));
        p.put("mentions", distinct(post.mentions()));
        List<Map<String, Object>> media = new ArrayList<>();
        for (SocialPost.Media m : post.media()) {
            if (m == null || m.url() == null || m.url().isBlank()) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", m.type() == null ? "other" : m.type());
            item.put("url", m.url());
            item.put("thumbUrl", m.thumbUrl());
            item.put("altText", m.altText());
            media.add(item);
        }
        p.put("media", media);
        p.put("engagement", engagement(post.engagement()));
        p.put("geo", geo(post.geo()));
        p.put("channel", post.channel());
        p.put("collection", post.collection() == null ? SocialVocabulary.COLLECTION_STREAM : post.collection());
        p.put("topicIds", topicIds == null ? List.of() : List.copyOf(new LinkedHashSet<>(topicIds)));
        p.put("cueId", post.cueId());
        p.put("deleted", false);
        return p;
    }

    /**
     * The tombstoned version of a stored payload (deletion sync): {@code deleted} true, text, links and media links
     * removed; author, counts, topics and timestamps kept so aggregates stay explainable.
     *
     * @param stored the stored payload
     * @param observedAt when the deletion was seen
     * @return the record, or null when the payload names no platform or post id
     */
    public static NormalizedRecord tombstoned(Map<String, Object> stored, Instant observedAt) {
        if (stored == null || stored.get("platform") == null || stored.get("postId") == null) {
            return null;
        }
        Map<String, Object> p = new LinkedHashMap<>(stored);
        p.put("text", null);
        p.put("links", List.of());
        p.put("media", List.of());
        p.put("deleted", true);
        p.put("observedAt", observedAt.toString());
        String platform = String.valueOf(stored.get("platform"));
        String postId = String.valueOf(stored.get("postId"));
        Instant created = instant(stored.get("createdAt"), observedAt);
        return NormalizedRecord.builder(SocialVocabulary.externalId(platform, postId), SocialVocabulary.KIND)
                .observedAt(created)
                .eventTime(created)
                .putAll(p)
                .build();
    }

    /** The text clipped to {@value SocialVocabulary#MAX_TEXT} characters (never splitting a surrogate pair). */
    public static String clip(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() <= SocialVocabulary.MAX_TEXT) {
            return text;
        }
        int end = SocialVocabulary.MAX_TEXT;
        if (Character.isHighSurrogate(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }

    /** Hashtags found in a text, without {@code #}, in order. */
    public static List<String> hashtags(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        Matcher m = HASHTAG.matcher(text);
        while (m.find()) {
            out.add(m.group(1));
        }
        return List.copyOf(out);
    }

    /** URLs found in a text, in order (trailing punctuation dropped). */
    public static List<String> links(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        Matcher m = URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            while (!url.isEmpty() && ".,;:!?)]}".indexOf(url.charAt(url.length() - 1)) >= 0) {
                url = url.substring(0, url.length() - 1);
            }
            if (url.length() > 10) {
                out.add(url);
            }
        }
        return List.copyOf(out);
    }

    /** The author key stored in a payload, or null. */
    public static String authorKeyOf(Map<String, Object> payload) {
        Object key = payload == null ? null : payload.get("authorKey");
        return key == null ? null : key.toString();
    }

    /** The post references of a payload member ({@code replyTo}, {@code quoteOf}, {@code repostOf}), or null. */
    public static SocialPost.PostRef refOf(Map<String, Object> payload, String member) {
        Object value = payload == null ? null : payload.get(member);
        if (!(value instanceof Map<?, ?> map) || map.get("platform") == null || map.get("postId") == null) {
            return null;
        }
        return new SocialPost.PostRef(map.get("platform").toString(), map.get("postId").toString(),
                map.get("url") == null ? null : map.get("url").toString());
    }

    /** The topic ids of a payload. */
    public static List<String> topicIdsOf(Map<String, Object> payload) {
        Object value = payload == null ? null : payload.get("topicIds");
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o != null) {
                out.add(o.toString());
            }
        }
        return out;
    }

    private static Map<String, Object> author(SocialPost.Author a) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (a == null) {
            out.put("handle", "");
            out.put("accountId", "");
            return out;
        }
        out.put("handle", a.handle() == null ? "" : a.handle());
        out.put("displayName", a.displayName());
        out.put("accountId", a.accountId() == null ? "" : a.accountId());
        out.put("accountCreatedAt", a.accountCreatedAt() == null ? null : a.accountCreatedAt().toString());
        out.put("followers", a.followers());
        out.put("verified", a.verified());
        return out;
    }

    private static Map<String, Object> ref(SocialPost.PostRef r) {
        if (r == null || r.postId() == null || r.platform() == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("platform", r.platform());
        out.put("postId", r.postId());
        out.put("url", r.url());
        return out;
    }

    private static Map<String, Object> engagement(SocialPost.Engagement e) {
        if (e == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("replies", e.replies());
        out.put("reposts", e.reposts());
        out.put("quotes", e.quotes());
        out.put("likes", e.likes());
        out.put("views", e.views());
        return out;
    }

    private static Map<String, Object> geo(SocialPost.Geo g) {
        if (g == null || (!g.located() && g.placeName() == null && g.placeId() == null)) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (g.located()) {
            Map<String, Object> geometry = new LinkedHashMap<>();
            geometry.put("type", "Point");
            geometry.put("coordinates", List.of(g.lon(), g.lat()));
            out.put("geometry", geometry);
        } else {
            out.put("geometry", null);
        }
        out.put("placeName", g.placeName());
        out.put("placeId", g.placeId());
        return out;
    }

    private static List<String> distinct(List<String> values) {
        Set<String> out = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                out.add(v.strip());
            }
        }
        return List.copyOf(out);
    }

    /** Parses an instant member, or returns {@code fallback}. */
    static Instant instant(Object value, Instant fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    /** The primary language subtag of a BCP 47 tag, lower-case, or null. */
    public static String primaryLanguage(String tag) {
        if (tag == null || tag.isBlank()) {
            return null;
        }
        String t = tag.strip().replace('_', '-');
        int dash = t.indexOf('-');
        String primary = (dash > 0 ? t.substring(0, dash) : t).toLowerCase(Locale.ROOT);
        return primary.matches("^[a-z]{2,3}$") ? primary : null;
    }
}
