package ai.foresignal.mcp;

import ai.foresignal.security.Scope;

/**
 * One MCP resource template (prompt 052): a family of resources under a URI template, such as
 * {@code foresignal://products/{id}/legend}. Declare an implementation as a bean and the server lists it in
 * {@code resources/templates/list} and serves {@code resources/read} for every URI it {@linkplain #matches
 * matches} that no fixed {@link ForesignalMcpResource} claims.
 */
public interface ForesignalMcpResourceTemplate {

    /** The RFC 6570 URI template, for example {@code foresignal://products/{id}/legend}. */
    String uriTemplate();

    /** A short name. */
    String name();

    /** What the resources hold. */
    String description();

    /** The media type of {@link #read}. */
    default String mimeType() {
        return "application/json";
    }

    /** The least scope that may read them. */
    default Scope requiredScope() {
        return Scope.READ;
    }

    /** True when they always need a token, even while {@code openReads} is on. */
    default boolean alwaysAuthenticated() {
        return false;
    }

    /** True when {@code uri} is one of this template's resources. */
    boolean matches(String uri);

    /**
     * The resource's current text, as {@code context}'s caller may see it.
     *
     * @throws McpToolException when the resource does not exist or cannot be read
     */
    String read(ToolContext context, String uri);
}
