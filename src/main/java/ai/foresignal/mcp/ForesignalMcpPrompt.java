package ai.foresignal.mcp;

import ai.foresignal.security.Scope;
import java.util.List;
import java.util.Map;

/**
 * One MCP prompt template (prompt 025): {@code situation-report} here. Declare an implementation as
 * a bean and the server lists and serves it.
 */
public interface ForesignalMcpPrompt {

    /** The prompt name. */
    String name();

    /** What the prompt does. */
    String description();

    /** The arguments the prompt takes. */
    List<Argument> arguments();

    /** The least scope that may get it. */
    default Scope requiredScope() {
        return Scope.READ;
    }

    /**
     * The user message the prompt expands to.
     *
     * @param arguments the client's argument values (missing optional arguments are absent)
     * @return the message text
     * @throws McpToolException when a required argument is missing or malformed
     */
    String render(Map<String, String> arguments);

    /**
     * One argument.
     *
     * @param name the name
     * @param description what it is
     * @param required true when it must be given
     */
    record Argument(String name, String description, boolean required) {}
}
