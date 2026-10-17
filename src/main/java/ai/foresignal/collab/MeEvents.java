package ai.foresignal.collab;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The events of the personal {@code me} SSE topic (prompt 130) and their payloads: {@code share.received}
 * ({@code ShareReceivedEvent}), {@code mention} and {@code comment.reply} ({@code CommentEvent}) and
 * {@code saved-search.matches} ({@code SavedSearchMatchesEvent}).
 */
public final class MeEvents {

    /** An object was shared with the principal. */
    public static final String SHARE_RECEIVED = "share.received";

    /** The principal was mentioned in a comment. */
    public static final String MENTION = "mention";

    /** Someone replied in a thread the principal started or took part in. */
    public static final String COMMENT_REPLY = "comment.reply";

    /** A saved search of the principal has new matches (at most hourly). */
    public static final String SAVED_SEARCH_MATCHES = "saved-search.matches";

    /** The events this prompt emits. */
    public static final List<String> ALL = List.of(SHARE_RECEIVED, MENTION, COMMENT_REPLY, SAVED_SEARCH_MATCHES);

    /** Longest comment excerpt, in characters. */
    public static final int EXCERPT_MAX = 280;

    private MeEvents() {}

    /** The inbox category of an event. */
    public static String category(String event) {
        return switch (event) {
            case SHARE_RECEIVED -> "share";
            case MENTION -> "mention";
            case COMMENT_REPLY -> "comment-reply";
            case SAVED_SEARCH_MATCHES -> "saved-search";
            default -> "system";
        };
    }

    /** The notification-preference event type of an event, or null when no channel carries it. */
    public static String preferenceType(String event) {
        return switch (event) {
            case SHARE_RECEIVED -> "share";
            case MENTION -> "mention";
            case COMMENT_REPLY -> "comment-reply";
            default -> null;
        };
    }

    /**
     * The {@code ShareReceivedEvent} payload.
     *
     * @param ref the shared object
     * @param envelope its envelope, or null
     * @param role the granted role
     * @param sharedBy the sharing principal's summary
     * @param message the sharer's message, or null
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> shareReceived(String ref, Object envelope, String role,
            Map<String, Object> sharedBy, String message, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ref", ref);
        if (envelope != null) {
            out.put("envelope", envelope);
        }
        out.put("role", role);
        out.put("sharedBy", sharedBy);
        out.put("message", message);
        out.put("at", at);
        return out;
    }

    /**
     * The {@code CommentEvent} payload.
     *
     * @param reason {@code mention} or {@code reply}
     * @param annotationId the comment
     * @param parentId its thread root, or null for a root
     * @param targetRef the commented object
     * @param author the author's summary
     * @param body the comment body (cut to {@value #EXCERPT_MAX} characters)
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> comment(String reason, String annotationId, String parentId, String targetRef,
            Map<String, Object> author, String body, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reason", reason);
        out.put("annotationId", annotationId);
        out.put("parentId", parentId);
        out.put("targetRef", targetRef);
        out.put("author", author);
        String text = body == null ? "" : body.strip();
        out.put("excerpt", text.length() <= EXCERPT_MAX ? text : text.substring(0, EXCERPT_MAX - 1) + "…");
        out.put("at", at);
        return out;
    }

    /**
     * The {@code SavedSearchMatchesEvent} payload.
     *
     * @param savedSearchId the saved search
     * @param name its name
     * @param newMatches how many new matches
     * @param refs up to 20 of them
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> savedSearchMatches(String savedSearchId, String name, int newMatches,
            List<String> refs, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("savedSearchId", savedSearchId);
        out.put("name", name);
        out.put("newMatches", newMatches);
        out.put("refs", refs.size() <= 20 ? refs : refs.subList(0, 20));
        out.put("at", at);
        return out;
    }
}
