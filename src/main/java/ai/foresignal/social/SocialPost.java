package ai.foresignal.social;

import java.time.Instant;
import java.util.List;

/**
 * One public post as a platform adapter parsed it (prompt 135), before {@link SocialCollector} keys its author,
 * detects its language, matches it against the tracked topics and turns it into a {@code social-post} record
 * ({@link SocialPosts}).
 *
 * @param platform the network ({@code bluesky}, {@code mastodon}, …)
 * @param postId the platform post id (Bluesky AT-URI, Mastodon {@code {instance}/{id}}, …)
 * @param url the public URL, or null
 * @param author the author as the platform returns it
 * @param text the text (clipped to {@value SocialVocabulary#MAX_TEXT} characters when stored), or null
 * @param language the platform-supplied language (BCP 47), or null to detect
 * @param createdAt the platform creation time
 * @param replyTo the post replied to, or null
 * @param quoteOf the post quoted, or null
 * @param repostOf the post reposted, or null
 * @param links expanded URLs
 * @param hashtags hashtags without {@code #}
 * @param mentions mentioned handles
 * @param media media links (links only)
 * @param engagement engagement counts at capture, or null
 * @param geo platform-supplied location, or null
 * @param channel the subreddit, instance, YouTube channel or equivalent, or null
 * @param collection {@code stream}, {@code search}, {@code sample}, {@code cue} or {@code import}
 * @param cueId the 137 cue, or null
 */
public record SocialPost(String platform, String postId, String url, Author author, String text, String language,
        Instant createdAt, PostRef replyTo, PostRef quoteOf, PostRef repostOf, List<String> links,
        List<String> hashtags, List<String> mentions, List<Media> media, Engagement engagement, Geo geo,
        String channel, String collection, String cueId) {

    /** Copies the lists. */
    public SocialPost {
        links = links == null ? List.of() : List.copyOf(links);
        hashtags = hashtags == null ? List.of() : List.copyOf(hashtags);
        mentions = mentions == null ? List.of() : List.copyOf(mentions);
        media = media == null ? List.of() : List.copyOf(media);
    }

    /** The {@code externalId} of the record. */
    public String externalId() {
        return SocialVocabulary.externalId(platform, postId);
    }

    /** This post with another collection and cue (137 cues, 138 imports). */
    public SocialPost withCollection(String newCollection, String newCueId) {
        return new SocialPost(platform, postId, url, author, text, language, createdAt, replyTo, quoteOf, repostOf,
                links, hashtags, mentions, media, engagement, geo, channel, newCollection, newCueId);
    }

    /** This post with a detected language. */
    public SocialPost withLanguage(String newLanguage) {
        return new SocialPost(platform, postId, url, author, text, newLanguage, createdAt, replyTo, quoteOf, repostOf,
                links, hashtags, mentions, media, engagement, geo, channel, collection, cueId);
    }

    /**
     * A post author as the platform returns it. Accounts are reporting sources, never profiled.
     *
     * @param handle the handle
     * @param displayName the display name, or null
     * @param accountId the platform account id
     * @param accountCreatedAt when the account was created, or null
     * @param followers the follower count, or null
     * @param verified the platform's verification flag, or null
     */
    public record Author(String handle, String displayName, String accountId, Instant accountCreatedAt,
            Long followers, Boolean verified) {}

    /**
     * A reference to another post.
     *
     * @param platform the network
     * @param postId the platform post id
     * @param url the public URL, or null
     */
    public record PostRef(String platform, String postId, String url) {

        /** The {@code externalId} of the referenced post. */
        public String externalId() {
            return SocialVocabulary.externalId(platform, postId);
        }
    }

    /**
     * A media link.
     *
     * @param type {@code image}, {@code video}, {@code gif}, {@code audio}, {@code link-card} or {@code other}
     * @param url the media URL
     * @param thumbUrl the thumbnail URL, or null
     * @param altText the alternative text, or null
     */
    public record Media(String type, String url, String thumbUrl, String altText) {}

    /**
     * Engagement counts at capture; null where the platform does not supply them.
     *
     * @param replies replies
     * @param reposts reposts
     * @param quotes quotes
     * @param likes likes
     * @param views views
     */
    public record Engagement(Long replies, Long reposts, Long quotes, Long likes, Long views) {

        /** The sum of the counts the platform supplied. */
        public long total() {
            return n(replies) + n(reposts) + n(quotes) + n(likes);
        }

        private static long n(Long value) {
            return value == null ? 0 : Math.max(0, value);
        }
    }

    /**
     * A platform-supplied location.
     *
     * @param lon the longitude, or null
     * @param lat the latitude, or null
     * @param placeName the platform place name, or null
     * @param placeId the platform place id, or null
     */
    public record Geo(Double lon, Double lat, String placeName, String placeId) {

        /** True when the platform supplied coordinates. */
        public boolean located() {
            return lon != null && lat != null && Double.isFinite(lon) && Double.isFinite(lat)
                    && Math.abs(lon) <= 180 && Math.abs(lat) <= 90;
        }
    }
}
