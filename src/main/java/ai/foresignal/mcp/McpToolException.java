package ai.foresignal.mcp;

/**
 * A tool call that cannot be answered, with a message written for the calling agent. The registry
 * turns it into an MCP tool error ({@code isError: true}), not a protocol error, so the agent can
 * read the reason and correct its call.
 */
public class McpToolException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Creates the exception with the message shown to the agent. */
    public McpToolException(String message) {
        super(message);
    }
}
