package ai.foresignal.security;

/**
 * The later egress paths that ask {@link EgressPolicy#allows(EgressPath, String,
 * ai.foresignal.security.visibility.Viewer)} before a record leaves Foresignal (prompt 039, §2 "Later
 * egress hooks"). Prompts 058 (feeds), 110 (sinks), 111 (exports), 112 (share links) and 113
 * (federation) must call it; the paths built so far (API reads, tiles, SSE, search, LLM context, MCP,
 * notifications, push) have their own {@code EgressPolicy} methods.
 */
public enum EgressPath {
    /** A per-entity or bulk export ({@code exportRecord} 039, 111). */
    EXPORT,
    /** An outbound sink that pushes records to another system (110). */
    SINK,
    /** Records sent to a federated peer instance (113). */
    FEDERATION,
    /** A share link ({@code fss_}, 112). */
    SHARE,
    /** An Atom/JSON feed or a feed-key URL (058, 035). */
    FEED
}
