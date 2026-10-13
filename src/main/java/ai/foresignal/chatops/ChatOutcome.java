package ai.foresignal.chatops;

import java.util.Objects;

/**
 * What a command did (prompt 061): the audited result and the reply.
 *
 * @param result {@value #OK}, {@value #INVALID}, {@value #NOT_FOUND}, {@value #DENIED}, {@value #RATE_LIMITED},
 *     {@value #UNAVAILABLE} or {@value #ERROR}
 * @param reply the reply, or null for none
 */
public record ChatOutcome(String result, ChatReply reply) {

    /** Done. */
    public static final String OK = "ok";

    /** Malformed arguments; the reply shows the usage. */
    public static final String INVALID = "invalid";

    /** The alert, briefing or code does not exist or is not visible to the chat. */
    public static final String NOT_FOUND = "not_found";

    /** The chat's scope does not allow it. */
    public static final String DENIED = "denied";

    /** The per-chat command or Ask limit. */
    public static final String RATE_LIMITED = "rate_limited";

    /** A service refused (Ask not configured, budget exhausted, a briefing already running). */
    public static final String UNAVAILABLE = "unavailable";

    /** An unexpected failure. */
    public static final String ERROR = "error";

    /** Requires the result. */
    public ChatOutcome {
        Objects.requireNonNull(result, "result");
    }

    /** A successful text reply. */
    public static ChatOutcome ok(String markdown, String replyTo) {
        return new ChatOutcome(OK, ChatReply.text(markdown, replyTo));
    }

    /** A text reply with {@code result}. */
    public static ChatOutcome of(String result, String markdown, String replyTo) {
        return new ChatOutcome(result, ChatReply.text(markdown, replyTo));
    }
}
