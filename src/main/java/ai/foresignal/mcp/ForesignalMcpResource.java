package ai.foresignal.mcp;

import ai.foresignal.security.Scope;

/**
 * One MCP resource (prompt 025): {@code foresignal://sources} and {@code foresignal://kinds} here;
 * later prompts add {@code foresignal://products} and {@code foresignal://places}. Declare an
 * implementation as a bean and the server lists and serves it.
 */
public interface ForesignalMcpResource {

    /** The resource URI, for example {@code foresignal://sources}. */
    String uri();

    /** A short name. */
    String name();

    /** What the resource holds. */
    String description();

    /** The media type of {@link #read}. */
    default String mimeType() {
        return "application/json";
    }

    /** The least scope that may read it. */
    default Scope requiredScope() {
        return Scope.READ;
    }

    /** The resource's current text, as {@code context}'s caller may see it. */
    String read(ToolContext context);
}
