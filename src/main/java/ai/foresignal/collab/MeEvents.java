package ai.foresignal.collab;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The events of the personal {@code me} SSE topic (prompt 130) and their payloads: {@code share.received}
 * ({@code ShareReceivedEvent}), {@code mention} and {@code comment.reply} ({@code CommentEvent}),
 * {@code saved-search.matches} ({@code SavedSearchMatchesEvent}), and prompt 132's {@code report.received} and
 * {@code report.retracted} ({@code ReportEvent}) and {@code package.received} ({@code PackageReceivedEvent}), and
 * prompt 134's {@code chronicle.link.found} ({@code ChronicleLinkFoundEvent}) and {@code chronicle.access.requested},
 * {@code chronicle.access.granted} and {@code chronicle.access.declined} ({@code ChronicleAccessEvent}).
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

    /** A report was sent to the principal on this instance (prompt 132). */
    public static final String REPORT_RECEIVED = "report.received";

    /** A signed intelligence package from another instance was imported for the principal (prompt 132). */
    public static final String PACKAGE_RECEIVED = "package.received";

    /** A report revision sent to the principal was retracted (prompt 132). */
    public static final String REPORT_RETRACTED = "report.retracted";

    /** A related chronicle was found for one of the principal's chronicles (prompt 134). */
    public static final String CHRONICLE_LINK_FOUND = "chronicle.link.found";

    /** Someone asked for access to one of the principal's chronicles (prompt 134). */
    public static final String CHRONICLE_ACCESS_REQUESTED = "chronicle.access.requested";

    /** The principal's access request was granted (prompt 134). */
    public static final String CHRONICLE_ACCESS_GRANTED = "chronicle.access.granted";

    /** The principal's access request was declined (prompt 134). */
    public static final String CHRONICLE_ACCESS_DECLINED = "chronicle.access.declined";

    /** The events of the {@code me} topic: prompt 130's four, prompt 132's three and prompt 134's four. */
    public static final List<String> ALL = List.of(SHARE_RECEIVED, MENTION, COMMENT_REPLY, SAVED_SEARCH_MATCHES,
            REPORT_RECEIVED, PACKAGE_RECEIVED, REPORT_RETRACTED, CHRONICLE_LINK_FOUND, CHRONICLE_ACCESS_REQUESTED,
            CHRONICLE_ACCESS_GRANTED, CHRONICLE_ACCESS_DECLINED);

    /**
     * The analysis cycles proposed a chronicle to its owner (prompt 131). Inbox and personal channels only: it is not
     * an event of the {@code me} topic.
     */
    public static final String CHRONICLE_PROPOSED = "chronicle.proposed";

    /** A chronicle reached 90% of its size cap (prompt 131); inbox and personal channels only. */
    public static final String CHRONICLE_CAP_WARNING = "chronicle.cap_warning";

    /** A chronicle fell behind global retention and has a capture gap (prompt 131); inbox and personal channels only. */
    public static final String CHRONICLE_GAP = "chronicle.gap";

    /**
     * A stricter TLP label lowered a chronicle's discoverability (prompt 134); inbox and personal channels only, not an
     * event of the {@code me} topic.
     */
    public static final String CHRONICLE_DISCOVERABILITY_LOWERED = "chronicle.discoverability_lowered";

    /** The chronicle events of prompt 131, and prompt 134's discoverability notice. */
    public static final List<String> CHRONICLE = List.of(CHRONICLE_PROPOSED, CHRONICLE_CAP_WARNING, CHRONICLE_GAP,
            CHRONICLE_DISCOVERABILITY_LOWERED);

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
            case CHRONICLE_PROPOSED, CHRONICLE_CAP_WARNING, CHRONICLE_GAP, CHRONICLE_LINK_FOUND,
                    CHRONICLE_DISCOVERABILITY_LOWERED -> "chronicle";
            case CHRONICLE_ACCESS_REQUESTED, CHRONICLE_ACCESS_GRANTED, CHRONICLE_ACCESS_DECLINED -> "access-request";
            case REPORT_RECEIVED, REPORT_RETRACTED -> "report";
            case PACKAGE_RECEIVED -> "package";
            default -> "system";
        };
    }

    /** The notification-preference event type of an event, or null when no channel carries it. */
    public static String preferenceType(String event) {
        return switch (event) {
            case SHARE_RECEIVED -> "share";
            case MENTION -> "mention";
            case COMMENT_REPLY -> "comment-reply";
            case CHRONICLE_PROPOSED, CHRONICLE_CAP_WARNING, CHRONICLE_GAP, CHRONICLE_LINK_FOUND,
                    CHRONICLE_DISCOVERABILITY_LOWERED, CHRONICLE_ACCESS_REQUESTED, CHRONICLE_ACCESS_GRANTED,
                    CHRONICLE_ACCESS_DECLINED -> "chronicle";
            case REPORT_RECEIVED, REPORT_RETRACTED, PACKAGE_RECEIVED -> "report";
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

    /**
     * The {@code ReportEvent} payload of {@code report.received} and {@code report.retracted} (prompt 132).
     *
     * @param reportRef the report reference ({@code report:{id}} or {@code remote:…})
     * @param title the report title
     * @param tlp the TLP label
     * @param revision the revision, or null
     * @param from the sender's summary, or null
     * @param message the sender's message, or null
     * @param reason the retraction reason, or null
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> report(String reportRef, String title, String tlp, Integer revision,
            Map<String, Object> from, String message, String reason, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("reportRef", reportRef);
        out.put("title", title);
        out.put("tlp", tlp);
        out.put("revision", revision);
        out.put("from", from);
        out.put("message", message);
        out.put("reason", reason);
        out.put("at", at);
        return out;
    }

    /**
     * The {@code PackageReceivedEvent} payload (prompt 132).
     *
     * @param receivedId the received package's id here
     * @param packageId the package id the sender assigned
     * @param type {@code report}, {@code investigation}, {@code chronicle-extract} or {@code evidence}
     * @param senderInstanceId the sending instance
     * @param title the title
     * @param tlp the TLP label
     * @param signatureStatus {@code valid}, {@code invalid} or {@code unknown-key}
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> packageReceived(String receivedId, String packageId, String type,
            String senderInstanceId, String title, String tlp, String signatureStatus, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("receivedId", receivedId);
        out.put("packageId", packageId);
        out.put("type", type);
        out.put("senderInstanceId", senderInstanceId);
        out.put("title", title);
        out.put("tlp", tlp);
        out.put("signatureStatus", signatureStatus);
        out.put("at", at);
        return out;
    }

    /**
     * The {@code ChronicleLinkFoundEvent} payload (prompt 134).
     *
     * @param chronicleId the principal's chronicle
     * @param link the {@code ChronicleLink} or {@code OpaqueChronicleLink} at the principal's disclosure level
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> chronicleLinkFound(String chronicleId, Map<String, Object> link, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chronicleId", chronicleId);
        out.put("link", link);
        out.put("at", at);
        return out;
    }

    /**
     * The {@code ChronicleAccessEvent} payload (prompt 134). The requester's handle and message are only set on
     * {@code chronicle.access.requested}; a decision tells the requester only the status.
     *
     * @param requestId the request
     * @param linkId the link
     * @param status {@code pending}, {@code granted} or {@code declined}
     * @param requesterHandle the requester's handle (requested only), or null
     * @param message the requester's message (requested only), or null
     * @param chronicleId the owner's chronicle (requested) or the granted chronicle, or null
     * @param at when
     * @return the payload
     */
    public static Map<String, Object> chronicleAccess(String requestId, String linkId, String status,
            String requesterHandle, String message, String chronicleId, Instant at) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("requestId", requestId);
        out.put("linkId", linkId);
        out.put("status", status);
        out.put("requesterHandle", requesterHandle);
        out.put("message", message);
        out.put("chronicleId", chronicleId);
        out.put("at", at);
        return out;
    }
}
