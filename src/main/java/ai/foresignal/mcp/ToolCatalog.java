package ai.foresignal.mcp;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.Scope;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The read tools of the MCP server as a catalog other model-driven features use (prompt 028), so MCP
 * and Ask behave identically: a tool added later as a {@link ForesignalMcpTool} bean (029 briefings,
 * 036 places, 037 time travel, 052/056 weather, 098 entities) appears in Ask without any change here.
 *
 * <p>{@link #readTools(McpCaller)} lists the tools the caller may use that change nothing:
 * {@code read} scope, {@link ForesignalMcpTool#readOnlyHint() read only}, listed for the caller and
 * not refused by {@link McpToolRegistry#denial} (scope, the cost-bearing rule and restricted tokens).
 * Write tools are never part of the catalog; a model can only propose changes (Ask's
 * {@code propose_*} tools).
 *
 * <p>{@link #invoke} validates the arguments against the tool's input schema, runs it and turns
 * service exceptions into tool errors exactly as {@link McpToolRegistry#call} does, without the MCP
 * protocol types.
 */
@Component
public class ToolCatalog {

    private static final Logger log = LoggerFactory.getLogger(ToolCatalog.class);

    private final McpToolRegistry registry;
    private final JsonSchemaValidator validator;
    private final JsonMapper json;

    /** Creates the catalog. */
    public ToolCatalog(McpToolRegistry registry, JsonSchemaValidator validator, JsonMapper json) {
        this.registry = registry;
        this.validator = validator;
        this.json = json;
    }

    /**
     * The read-only tools {@code caller} may call, sorted by name (a stable order, which keeps a
     * provider's prompt cache valid).
     */
    public List<ForesignalMcpTool> readTools(McpCaller caller) {
        List<ForesignalMcpTool> out = new ArrayList<>();
        for (ForesignalMcpTool tool : registry.tools()) {
            if (isRead(tool) && McpToolRegistry.listed(tool, caller) && registry.denial(tool, caller).isEmpty()) {
                out.add(tool);
            }
        }
        return out;
    }

    /** The read-only tool named {@code name} when {@code caller} may call it. */
    public Optional<ForesignalMcpTool> readTool(String name, McpCaller caller) {
        return readTools(caller).stream().filter(tool -> tool.name().equals(name)).findFirst();
    }

    /** True when {@code tool} belongs in the catalog: {@code read} scope and read only. */
    public static boolean isRead(ForesignalMcpTool tool) {
        return tool.requiredScope() == Scope.READ && tool.readOnlyHint();
    }

    /** The input schema of {@code tool} as a JSON tree. */
    public JsonNode schema(ForesignalMcpTool tool) {
        return json.valueToTree(tool.inputSchema());
    }

    /**
     * Runs {@code tool} with {@code arguments}.
     *
     * @param tool a tool from {@link #readTools}
     * @param arguments the arguments (a JSON object; null for none)
     * @param context the caller
     * @return the result; a tool error for invalid arguments, refusals and service errors
     */
    public ToolResult invoke(ForesignalMcpTool tool, JsonNode arguments, ToolContext context) {
        Optional<String> denied = registry.denial(tool, context.caller());
        if (denied.isPresent()) {
            return ToolResult.error(denied.get());
        }
        JsonNode args = arguments == null || arguments.isNull() ? json.createObjectNode() : arguments;
        if (!args.isObject()) {
            return ToolResult.error("Invalid arguments for " + tool.name() + ": the arguments must be a JSON object.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> asMap = json.convertValue(args, Map.class);
        JsonSchemaValidator.ValidationResponse validation = validator.validate(tool.inputSchema(), asMap);
        if (!validation.valid()) {
            return ToolResult.error("Invalid arguments for " + tool.name() + ": " + validation.errorMessage());
        }
        try {
            return tool.call(context, args);
        } catch (McpToolException e) {
            return ToolResult.error(e.getMessage());
        } catch (ForesignalException e) {
            return ToolResult.error(McpToolRegistry.describe(e));
        } catch (RuntimeException e) {
            log.warn("Tool {} failed", tool.name(), e);
            return ToolResult.error("The tool " + tool.name() + " failed unexpectedly (" + e.getClass().getSimpleName()
                    + "). The failure is logged on the server.");
        }
    }
}
