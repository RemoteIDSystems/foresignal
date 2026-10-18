package ai.foresignal.source.adapters.bluesky;

import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.source.formats.social.SocialJson;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Maps an {@code app.bsky.feed.post} record (lexicon {@code app/bsky/feed/post.json}, verified 2026-10-17) to a
 * {@link SocialPost}, shared by the Jetstream stream and the AppView search (prompt 135).
 *
 * <ul>
 *   <li>{@code text}, {@code createdAt}, {@code langs[0]}, {@code tags} (hashtags);
 *   <li>facets: {@code app.bsky.richtext.facet#link} {@code uri} (links), {@code #tag} {@code tag} (hashtags),
 *       {@code #mention} {@code did} (mentions);
 *   <li>{@code reply.parent.uri} (reply), {@code app.bsky.embed.record} {@code record.uri} or
 *       {@code app.bsky.embed.recordWithMedia} {@code record.record.uri} (quote, when it names a post);
 *   <li>media: {@code app.bsky.embed.images} (alt text), {@code app.bsky.embed.video}, {@code app.bsky.embed.external}
 *       (link card). A record carries blob references, not URLs; the media link is then the post's own web page, and
 *       the AppView search supplies real image URLs from its views.
 * </ul>
 */
public final class BlueskyRecords {

    /** The post collection. */
    public static final String POST = "app.bsky.feed.post";

    /** The repost collection. */
    public static final String REPOST = "app.bsky.feed.repost";

    private BlueskyRecords() {}

    /** The AT-URI of a post. */
    public static String atUri(String did, String collection, String rkey) {
        return "at://" + did + "/" + collection + "/" + rkey;
    }

    /** The web URL of a post AT-URI, or null when it is not a post URI. */
    public static String webUrl(String atUri) {
        if (atUri == null || !atUri.startsWith("at://")) {
            return null;
        }
        String[] parts = atUri.substring(5).split("/");
        if (parts.length != 3 || !POST.equals(parts[1])) {
            return null;
        }
        return "https://bsky.app/profile/" + parts[0] + "/post/" + parts[2];
    }

    /** The DID of an AT-URI, or null. */
    public static String didOf(String atUri) {
        if (atUri == null || !atUri.startsWith("at://")) {
            return null;
        }
        String rest = atUri.substring(5);
        int slash = rest.indexOf('/');
        return slash > 0 ? rest.substring(0, slash) : rest;
    }

    /** A reference to a post AT-URI, or null when it is not a post URI. */
    public static SocialPost.PostRef ref(String atUri) {
        if (webUrl(atUri) == null) {
            return null;
        }
        return new SocialPost.PostRef(SocialVocabulary.BLUESKY, atUri, webUrl(atUri));
    }

    /**
     * The post of a record.
     *
     * @param atUri the post's AT-URI
     * @param author the author
     * @param record the {@code app.bsky.feed.post} record
     * @param fallbackCreated used when the record has no parsable {@code createdAt}
     * @param engagement the counts (AppView), or null
     * @param extraMedia media from an AppView embed view (with URLs), or null to read the record's embed
     * @param collection the post collection
     * @return the post, or null when the record is not an object
     */
    public static SocialPost post(String atUri, SocialPost.Author author, JsonNode record, Instant fallbackCreated,
            SocialPost.Engagement engagement, List<SocialPost.Media> extraMedia, String collection) {
        if (record == null || !record.isObject()) {
            return null;
        }
        String text = SocialJson.text(record, "text");
        Instant created = SocialJson.instant(record, "createdAt");
        List<String> langs = SocialJson.strings(record.get("langs"));
        Set<String> links = new LinkedHashSet<>();
        Set<String> tags = new LinkedHashSet<>(SocialJson.strings(record.get("tags")));
        Set<String> mentions = new LinkedHashSet<>();
        JsonNode facets = record.get("facets");
        if (facets != null && facets.isArray()) {
            for (JsonNode facet : facets) {
                JsonNode features = facet.get("features");
                if (features == null || !features.isArray()) {
                    continue;
                }
                for (JsonNode f : features) {
                    String type = SocialJson.text(f, "$type");
                    if (type == null) {
                        continue;
                    }
                    if (type.endsWith("#link") && SocialJson.text(f, "uri") != null) {
                        links.add(SocialJson.text(f, "uri"));
                    } else if (type.endsWith("#tag") && SocialJson.text(f, "tag") != null) {
                        tags.add(SocialJson.text(f, "tag"));
                    } else if (type.endsWith("#mention") && SocialJson.text(f, "did") != null) {
                        mentions.add(SocialJson.text(f, "did"));
                    }
                }
            }
        }
        SocialPost.PostRef replyTo = ref(SocialJson.text(SocialJson.at(record, "reply", "parent"), "uri"));
        SocialPost.PostRef quoteOf = null;
        List<SocialPost.Media> media = new ArrayList<>();
        String webUrl = webUrl(atUri);
        JsonNode embed = record.get("embed");
        if (embed != null && embed.isObject()) {
            String type = SocialJson.text(embed, "$type");
            JsonNode mediaNode = embed;
            if ("app.bsky.embed.record".equals(type)) {
                quoteOf = ref(SocialJson.text(SocialJson.at(embed, "record"), "uri"));
                mediaNode = null;
            } else if ("app.bsky.embed.recordWithMedia".equals(type)) {
                quoteOf = ref(SocialJson.text(SocialJson.at(embed, "record", "record"), "uri"));
                mediaNode = SocialJson.at(embed, "media");
            }
            if (mediaNode != null) {
                media.addAll(media(mediaNode, webUrl, links));
            }
        }
        if (extraMedia != null && !extraMedia.isEmpty()) {
            media = new ArrayList<>(extraMedia);
        }
        return new SocialPost(SocialVocabulary.BLUESKY, atUri, webUrl, author, text, langs.isEmpty() ? null
                : langs.get(0), created != null ? created : fallbackCreated, replyTo, quoteOf, null,
                new ArrayList<>(links), new ArrayList<>(tags), new ArrayList<>(mentions), media, engagement, null, null,
                collection, null);
    }

    /**
     * The post of an AppView {@code app.bsky.feed.defs#postView} ({@code uri}, {@code author}, {@code record},
     * {@code embed} view, {@code replyCount}, {@code repostCount}, {@code likeCount}, {@code quoteCount},
     * {@code indexedAt}; verified against the lexicons 2026-10-18). Media URLs come from the embed view
     * ({@code app.bsky.embed.images#view} {@code fullsize}/{@code thumb}, {@code video#view} {@code playlist},
     * {@code external#view}).
     *
     * @param view the post view
     * @param collection the post collection
     * @return the post, or null when the view has no URI or record
     */
    public static SocialPost fromPostView(JsonNode view, String collection) {
        String uri = SocialJson.text(view, "uri");
        JsonNode record = SocialJson.at(view, "record");
        if (uri == null || record == null) {
            return null;
        }
        JsonNode a = SocialJson.at(view, "author");
        String did = SocialJson.text(a, "did");
        SocialPost.Author author = new SocialPost.Author(SocialJson.text(a, "handle") != null
                ? SocialJson.text(a, "handle") : did, SocialJson.text(a, "displayName"), did != null ? did
                : didOf(uri), SocialJson.instant(a, "createdAt"), null, null);
        SocialPost.Engagement engagement = new SocialPost.Engagement(SocialJson.number(view, "replyCount"),
                SocialJson.number(view, "repostCount"), SocialJson.number(view, "quoteCount"),
                SocialJson.number(view, "likeCount"), null);
        List<SocialPost.Media> media = viewMedia(SocialJson.at(view, "embed"));
        return post(uri, author, record, SocialJson.instant(view, "indexedAt"), engagement, media, collection);
    }

    private static List<SocialPost.Media> viewMedia(JsonNode embed) {
        List<SocialPost.Media> out = new ArrayList<>();
        if (embed == null) {
            return out;
        }
        String type = SocialJson.text(embed, "$type");
        if ("app.bsky.embed.recordWithMedia#view".equals(type)) {
            return viewMedia(SocialJson.at(embed, "media"));
        }
        if ("app.bsky.embed.images#view".equals(type) && embed.get("images") != null) {
            for (JsonNode image : embed.get("images")) {
                String full = SocialJson.text(image, "fullsize");
                if (full != null) {
                    out.add(new SocialPost.Media("image", full, SocialJson.text(image, "thumb"),
                            SocialJson.text(image, "alt")));
                }
            }
        } else if ("app.bsky.embed.video#view".equals(type) && SocialJson.text(embed, "playlist") != null) {
            out.add(new SocialPost.Media("video", SocialJson.text(embed, "playlist"),
                    SocialJson.text(embed, "thumbnail"), SocialJson.text(embed, "alt")));
        } else if ("app.bsky.embed.external#view".equals(type)) {
            JsonNode external = SocialJson.at(embed, "external");
            if (SocialJson.text(external, "uri") != null) {
                out.add(new SocialPost.Media("link-card", SocialJson.text(external, "uri"),
                        SocialJson.text(external, "thumb"), SocialJson.text(external, "title")));
            }
        }
        return out;
    }

    private static List<SocialPost.Media> media(JsonNode embed, String webUrl, Set<String> links) {
        List<SocialPost.Media> out = new ArrayList<>();
        String type = SocialJson.text(embed, "$type");
        if (type == null) {
            return out;
        }
        switch (type) {
            case "app.bsky.embed.images" -> {
                JsonNode images = embed.get("images");
                if (images != null && images.isArray() && webUrl != null) {
                    for (JsonNode image : images) {
                        out.add(new SocialPost.Media("image", webUrl, null, SocialJson.text(image, "alt")));
                    }
                }
            }
            case "app.bsky.embed.video" -> {
                if (webUrl != null) {
                    out.add(new SocialPost.Media("video", webUrl, null, SocialJson.text(embed, "alt")));
                }
            }
            case "app.bsky.embed.external" -> {
                String uri = SocialJson.text(SocialJson.at(embed, "external"), "uri");
                if (uri != null) {
                    out.add(new SocialPost.Media("link-card", uri, null,
                            SocialJson.text(SocialJson.at(embed, "external"), "title")));
                    links.add(uri);
                }
            }
            default -> {
                // other embeds (gallery and future types) carry no media link Foresignal reads
            }
        }
        return out;
    }
}
