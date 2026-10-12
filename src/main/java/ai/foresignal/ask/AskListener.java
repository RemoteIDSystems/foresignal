package ai.foresignal.ask;

import java.util.Map;

/**
 * Receives the events of one Ask as they happen (prompt 028): the SSE stream forwards them; the plain
 * request ignores them. Every method has a no-op default. Methods may be called from tool threads,
 * so implementations must be thread-safe.
 */
public interface AskListener {

    /** A listener that ignores everything. */
    AskListener NONE = new AskListener() {};

    /** True when answer text should be streamed from the provider as it is generated. */
    default boolean streaming() {
        return false;
    }

    /**
     * Tool activity ({@code AskProgressEvent}).
     *
     * @param tool the tool
     * @param status {@code calling} or {@code done}
     * @param summary a short description, or null
     */
    default void progress(String tool, String status, String summary) {}

    /** Unverified answer text ({@code AskDeltaEvent}). */
    default void delta(String text) {}

    /** A verified citation ({@code AskCitation}). */
    default void citation(AskCitation citation) {}

    /** A warning ({@code AskWarningEvent}). */
    default void warning(String message) {}

    /** A proposed or executed action ({@code AskAction}). */
    default void action(Map<String, Object> action) {}

    /** The full verified answer ({@code AskResponse}), identical to the stored message. */
    default void done(Map<String, Object> response) {}
}
