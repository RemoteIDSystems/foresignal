package ai.foresignal.source.adapters.mastodon;

import ai.foresignal.social.SocialPost;
import ai.foresignal.social.SocialVocabulary;
import ai.foresignal.source.formats.social.SocialHtml;
import ai.foresignal.source.formats.social.SocialJson;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * Maps a Mastodon {@code Status} entity (docs.joinmastodon.org/entities/Status, verified 2026-10-17) to a
 * {@link SocialPost} (prompt 135): {@code postId} = {@code {instance}/{id}}, {@code content} HTML to text,
 * {@code account} ({@code acct}, {@code display_name}, {@code id}, {@code created_at}, {@code followers_count}),
 * {@code language}, {@code in_reply_to_id}, {@code media_attachments} ({@code type}, {@code url},
 * {@code preview_url}, {@code description}), {@code tags[].name}, {@code mentions[].acct}, {@code card.url} and the
 * counts {@code replies_count}, {@code reblogs_count}, {@code quotes_count} (4.5), {@code favourites_count}. The
 * channel is the instance. A boost ({@code reblog} set) is an amplification of the boosted status, never a post.
 */
public final class MastodonStatuses {

    private MastodonStatuses() {}

    /** The boosted status of a boost, or null. */
    public static JsonNode reblog(JsonNode status) {
        JsonNode r = status == null ? null : status.get("reblog");
        return r == null || r.isNull() || !r.isObject() ? null : r;
    }

    /**
     * The post of a status.
     *
     * @param instance the instance host the status was read from
     * @param status the status
     * @param collection the post collection
     * @return the post, or null without an id
     */
    public static SocialPost post(String instance, JsonNode status, String collection) {
        String id = SocialJson.text(status, "id");
        if (id == null) {
            return null;
        }
        JsonNode account = SocialJson.at(status, "account");
        SocialPost.Author author = new SocialPost.Author(SocialJson.text(account, "acct"),
                SocialJson.text(account, "display_name"), SocialJson.text(account, "id") == null ? null
                : instance + "/" + SocialJson.text(account, "id"), SocialJson.instant(account, "created_at"),
                SocialJson.number(account, "followers_count"), null);
        List<String> links = new ArrayList<>();
        String card = SocialJson.text(SocialJson.at(status, "card"), "url");
        if (card != null) {
            links.add(card);
        }
        List<String> tags = new ArrayList<>();
        JsonNode tagNodes = status.get("tags");
        if (tagNodes != null && tagNodes.isArray()) {
            for (JsonNode t : tagNodes) {
                if (SocialJson.text(t, "name") != null) {
                    tags.add(SocialJson.text(t, "name"));
                }
            }
        }
        List<String> mentions = new ArrayList<>();
        JsonNode mentionNodes = status.get("mentions");
        if (mentionNodes != null && mentionNodes.isArray()) {
            for (JsonNode m : mentionNodes) {
                if (SocialJson.text(m, "acct") != null) {
                    mentions.add(SocialJson.text(m, "acct"));
                }
            }
        }
        List<SocialPost.Media> media = new ArrayList<>();
        JsonNode attachments = status.get("media_attachments");
        if (attachments != null && attachments.isArray()) {
            for (JsonNode a : attachments) {
                String url = SocialJson.text(a, "url") != null ? SocialJson.text(a, "url")
                        : SocialJson.text(a, "remote_url");
                if (url != null) {
                    media.add(new SocialPost.Media(type(SocialJson.text(a, "type")), url,
                            SocialJson.text(a, "preview_url"), SocialJson.text(a, "description")));
                }
            }
        }
        String reply = SocialJson.text(status, "in_reply_to_id");
        return new SocialPost(SocialVocabulary.MASTODON, instance + "/" + id, SocialJson.text(status, "url"), author,
                SocialHtml.text(SocialJson.text(status, "content")), SocialJson.text(status, "language"),
                SocialJson.instant(status, "created_at"), reply == null ? null
                : new SocialPost.PostRef(SocialVocabulary.MASTODON, instance + "/" + reply, null), null, null, links,
                tags, mentions, media, new SocialPost.Engagement(SocialJson.number(status, "replies_count"),
                SocialJson.number(status, "reblogs_count"), SocialJson.number(status, "quotes_count"),
                SocialJson.number(status, "favourites_count"), null), null, instance, collection, null);
    }

    private static String type(String mastodonType) {
        if (mastodonType == null) {
            return "other";
        }
        return switch (mastodonType) {
            case "image" -> "image";
            case "video" -> "video";
            case "gifv" -> "gif";
            case "audio" -> "audio";
            default -> "other";
        };
    }
}
