package ai.foresignal.security;

/**
 * The later egress paths that ask {@link EgressPolicy#allows(EgressPath, String,
 * ai.foresignal.security.visibility.Viewer)} before a record leaves Foresignal (prompt 039, §2 "Later
 * egress hooks"). Prompts 058 (feeds), 110 (sinks), 111 (exports), 112 (share links) and 113
 * (federation) must call it; the paths built so far (API reads, tiles, SSE, search, LLM context, MCP,
 * notifications, push) have their own {@code EgressPolicy} methods.
 */
public enum EgressPath {
    /** A record read over the API ({@code /v1/records}, history, versions, changes). */
    API_READ,
    /** A vector or raster tile (016, 045). */
    TILE,
    /** A server-sent events stream (015). */
    SSE,
    /** An MCP tool result (025). */
    MCP,
    /** A prompt to a local LLM provider (026). */
    LLM_LOCAL,
    /** A prompt to a remote LLM provider (026). */
    LLM_REMOTE,
    /** A notification payload (018, 055). */
    NOTIFICATION,
    /** A per-entity or bulk export ({@code exportRecord} 039, 111). */
    EXPORT,
    /** An outbound sink that pushes records to another system (110). */
    SINK,
    /** A sink that serves records over the LAN (110). */
    SINK_SERVE_LAN,
    /** A sink that pushes records off the network (110). */
    SINK_PUSH,
    /** Records sent to a federated peer instance (113). */
    FEDERATION,
    /** A public share link ({@code fss_}, 112). */
    PUBLIC_SHARE,
    /** A share link ({@code fss_}, 112). */
    SHARE,
    /** An Atom/JSON feed or a feed-key URL (058, 035). */
    FEED,
    /** An operator-owned cold archive (111). */
    ARCHIVE
}
