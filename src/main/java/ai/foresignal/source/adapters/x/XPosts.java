package ai.foresignal.source.adapters.x;

import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.source.formats.social.SocialJson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Maps X API v2 post objects (prompt 135) to {@link SocialPost}s: {@code id}, {@code text}, {@code created_at},
 * {@code lang}, {@code author_id} (with {@code includes.users}: {@code username}, {@code name}, {@code created_at},
 * {@code public_metrics.followers_count}, {@code verified}), {@code public_metrics} ({@code reply_count},
 * {@code repost_count} or {@code retweet_count}, {@code quote_count}, {@code like_count}, {@code impression_count}),
 * {@code entities} ({@code hashtags[].tag}, {@code urls[].expanded_url}, {@code mentions[].username}), the referenced
 * posts ({@code referenced_posts} in the live OpenAPI, {@code referenced_tweets} in the data dictionary; types
 * {@code replied_to}, {@code quoted}, {@code retweeted} or {@code reposted}), media ({@code includes.media}) and
 * {@code geo}. Both namings are read because X's own documents disagree (2026-10-17).
 */
public final class XPosts {

    private XPosts() {}

    /** The posts of a search or lookup answer. */
    public static List<SocialPost> posts(JsonNode root, String collection) {
        List<SocialPost> out = new ArrayList<>();
        JsonNode data = root == null ? null : root.get("data");
        if (data == null || !data.isArray()) {
            return out;
        }
        Map<String, JsonNode> users = index(SocialJson.at(root, "includes", "users"), "id");
        Map<String, JsonNode> media = index(SocialJson.at(root, "includes", "media"), "media_key");
        Map<String, JsonNode> places = index(SocialJson.at(root, "includes", "places"), "id");
        for (JsonNode p : data) {
            SocialPost post = post(p, users, media, places, collection);
            if (post != null) {
                out.add(post);
            }
        }
        return out;
    }

    private static Map<String, JsonNode> index(JsonNode array, String key) {
        Map<String, JsonNode> out = new HashMap<>();
        if (array != null && array.isArray()) {
            for (JsonNode n : array) {
                if (SocialJson.text(n, key) != null) {
                    out.put(SocialJson.text(n, key), n);
                }
            }
        }
        return out;
    }

    static SocialPost post(JsonNode p, Map<String, JsonNode> users, Map<String, JsonNode> media,
            Map<String, JsonNode> places, String collection) {
        String id = SocialJson.text(p, "id");
        if (id == null) {
            return null;
        }
        String authorId = SocialJson.text(p, "author_id");
        JsonNode user = authorId == null ? null : users.get(authorId);
        String username = SocialJson.text(user, "username");
        SocialPost.Author author = new SocialPost.Author(username == null ? (authorId == null ? "" : authorId)
                : username, SocialJson.text(user, "name"), authorId, SocialJson.instant(user, "created_at"),
                SocialJson.number(SocialJson.at(user, "public_metrics"), "followers_count"),
                user != null && user.get("verified") != null && user.get("verified").isBoolean()
                        ? user.get("verified").asBoolean() : null);
        JsonNode metrics = SocialJson.at(p, "public_metrics");
        Long reposts = SocialJson.number(metrics, "repost_count");
        if (reposts == null) {
            reposts = SocialJson.number(metrics, "retweet_count");
        }
        SocialPost.Engagement engagement = metrics == null ? null : new SocialPost.Engagement(
                SocialJson.number(metrics, "reply_count"), reposts, SocialJson.number(metrics, "quote_count"),
                SocialJson.number(metrics, "like_count"), SocialJson.number(metrics, "impression_count"));
        List<String> hashtags = new ArrayList<>();
        List<String> links = new ArrayList<>();
        List<String> mentions = new ArrayList<>();
        JsonNode entities = SocialJson.at(p, "entities");
        if (entities != null) {
            for (JsonNode h : iter(entities.get("hashtags"))) {
                if (SocialJson.text(h, "tag") != null) {
                    hashtags.add(SocialJson.text(h, "tag"));
                }
            }
            for (JsonNode u : iter(entities.get("urls"))) {
                String expanded = SocialJson.text(u, "expanded_url");
                if (expanded != null) {
                    links.add(expanded);
                }
            }
            for (JsonNode m : iter(entities.get("mentions"))) {
                if (SocialJson.text(m, "username") != null) {
                    mentions.add(SocialJson.text(m, "username"));
                }
            }
        }
        SocialPost.PostRef replyTo = null;
        SocialPost.PostRef quoteOf = null;
        SocialPost.PostRef repostOf = null;
        JsonNode refs = p.get("referenced_posts") != null ? p.get("referenced_posts") : p.get("referenced_tweets");
        for (JsonNode ref : iter(refs)) {
            String type = SocialJson.text(ref, "type");
            String refId = SocialJson.text(ref, "id");
            if (type == null || refId == null) {
                continue;
            }
            SocialPost.PostRef r = new SocialPost.PostRef(SocialVocabulary.X, refId, "https://x.com/i/status/" + refId);
            switch (type) {
                case "replied_to" -> replyTo = r;
                case "quoted" -> quoteOf = r;
                case "retweeted", "reposted" -> repostOf = r;
                default -> {
                    // other reference types are not followed
                }
            }
        }
        List<SocialPost.Media> mediaOut = new ArrayList<>();
        for (JsonNode key : iter(SocialJson.at(p, "attachments", "media_keys"))) {
            JsonNode m = media.get(key.asString());
            if (m == null) {
                continue;
            }
            String url = SocialJson.text(m, "url") != null ? SocialJson.text(m, "url")
                    : SocialJson.text(m, "preview_image_url");
            if (url != null) {
                String type = SocialJson.text(m, "type");
                mediaOut.add(new SocialPost.Media("photo".equals(type) ? "image" : "animated_gif".equals(type) ? "gif"
                        : "video".equals(type) ? "video" : "other", url, SocialJson.text(m, "preview_image_url"),
                        SocialJson.text(m, "alt_text")));
            }
        }
        SocialPost.Geo geo = null;
        JsonNode g = SocialJson.at(p, "geo");
        if (g != null) {
            JsonNode coords = SocialJson.at(g, "coordinates", "coordinates");
            Double lon = coords != null && coords.isArray() && coords.size() == 2 ? coords.get(0).asDouble() : null;
            Double lat = coords != null && coords.isArray() && coords.size() == 2 ? coords.get(1).asDouble() : null;
            String placeId = SocialJson.text(g, "place_id");
            JsonNode place = placeId == null ? null : places.get(placeId);
            geo = new SocialPost.Geo(lon, lat, SocialJson.text(place, "full_name"), placeId);
        }
        String url = "https://x.com/" + (username == null ? "i" : username) + "/status/" + id;
        return new SocialPost(SocialVocabulary.X, id, url, author, SocialJson.text(p, "text"), SocialJson.text(p, "lang"),
                SocialJson.instant(p, "created_at"), replyTo, quoteOf, repostOf, links, hashtags, mentions, mediaOut,
                engagement, geo, null, collection, null);
    }

    private static Iterable<JsonNode> iter(JsonNode array) {
        List<JsonNode> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            array.forEach(out::add);
        }
        return out;
    }
}
