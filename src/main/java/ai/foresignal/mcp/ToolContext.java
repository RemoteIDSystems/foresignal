package ai.foresignal.mcp;

import ai.foresignal.api.support.ReadCaller;
import java.util.Objects;
import java.util.Set;

/**
 * What a tool knows about the call it is answering.
 *
 * @param caller the caller
 * @param sessionId the MCP session id
 * @param transport {@value McpSessions#STREAMABLE} or {@value McpSessions#LEGACY_SSE}
 */
public record ToolContext(McpCaller caller, String sessionId, String transport) {

    /** Requires the caller. */
    public ToolContext {
        Objects.requireNonNull(caller, "caller");
    }

    /** The read-side view of the caller. */
    public ReadCaller readCaller() {
        return caller.readCaller();
    }

    /** The private sources the caller may not see. */
    public Set<String> excludedSources() {
        return caller.excludedSources();
    }

    /** The actor recorded on writes. */
    public String actor() {
        return caller.actor();
    }
}
