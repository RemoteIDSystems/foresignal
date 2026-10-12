package ai.foresignal.testsupport.mcp;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tools for MCP tests (prompt 025): a cost-bearing read tool and an image tool, standing in for the
 * LLM-backed and imagery tools of later prompts.
 */
public final class McpTestTools {

    /** The 1×1 transparent PNG, base64. */
    public static final String PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private McpTestTools() {}

    /** {@code test_cost}: read, cost-bearing; echoes {@code text}. */
    public static ForesignalMcpTool costBearing() {
        return new ForesignalMcpTool() {
            @Override
            public String name() {
                return "test_cost";
            }

            @Override
            public String description() {
                return "A test stand-in for an LLM-backed tool: it spends a budget, so it always needs a token.";
            }

            @Override
            public Map<String, Object> inputSchema() {
                return JsonSchemas.object(JsonSchemas.props("text", JsonSchemas.string("Text to echo.", 1, 100)), "text");
            }

            @Override
            public Map<String, Object> outputSchema() {
                return JsonSchemas.objectOutput("{echo}");
            }

            @Override
            public Scope requiredScope() {
                return Scope.READ;
            }

            @Override
            public boolean costBearing() {
                return true;
            }

            @Override
            public ToolResult call(ToolContext context, JsonNode arguments) {
                return ToolResult.json(JSON.createObjectNode().put("echo", McpArgs.requiredText(arguments, "text")));
            }
        };
    }

    /** {@code test_image}: read; returns a PNG with a caption. */
    public static ForesignalMcpTool image() {
        return new ForesignalMcpTool() {
            @Override
            public String name() {
                return "test_image";
            }

            @Override
            public String description() {
                return "A test stand-in for get_map_image: returns a one-pixel PNG with a caption describing it.";
            }

            @Override
            public Map<String, Object> inputSchema() {
                return JsonSchemas.object(JsonSchemas.props());
            }

            @Override
            public Scope requiredScope() {
                return Scope.READ;
            }

            @Override
            public ToolResult call(ToolContext context, JsonNode arguments) {
                return ToolResult.image(PIXEL_PNG, "image/png", "A transparent pixel.", null);
            }
        };
    }
}
