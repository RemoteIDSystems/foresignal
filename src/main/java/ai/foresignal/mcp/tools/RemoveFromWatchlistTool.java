package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.stringArray;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.watch.WatchlistMapper;
import ai.foresignal.watch.WatchlistService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code remove_from_watchlist} (agent): removes values, as {@code removeWatchlistItems}. */
@Component
public class RemoveFromWatchlistTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "watchlistId", uuid("The watchlist id (from list_watchlists)."),
            "values", stringArray("The values to remove (normalized as the list normalizes).", 1000)),
            "watchlistId", "values");

    private final WatchlistService watchlists;
    private final WatchlistMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public RemoveFromWatchlistTool(WatchlistService watchlists, WatchlistMapper mapper, McpOutputShaper shaper) {
        this.watchlists = watchlists;
        this.mapper = mapper;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "remove_from_watchlist";
    }

    @Override
    public String title() {
        return "Remove from a watchlist";
    }

    @Override
    public String description() {
        return "Removes identifiers from a watchlist. Watches stop matching them on each entity's next observation."
                + " Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("WatchlistImportResult");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean destructiveHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        List<String> values = McpArgs.strings(args, "values");
        if (values.isEmpty()) {
            throw new McpToolException("values must name at least one value.");
        }
        return ToolResult.json(shaper.objectTree(mapper.result(watchlists.remove(McpArgs.requiredUuid(args,
                "watchlistId"), values))));
    }
}
