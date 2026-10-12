package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.watch.WatchlistMapper;
import ai.foresignal.watch.WatchlistService;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code add_to_watchlist} (agent): adds or updates items, as {@code addWatchlistItems}. */
@Component
public class AddToWatchlistTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "watchlistId", uuid("The watchlist id (from list_watchlists)."),
            "items", Map.of("type", "array", "minItems", 1, "maxItems", 1000,
                    "description", "Items {value, label?, notes?, tags?}; values are validated for the list's identity"
                            + " (icao24 six hex digits, mmsi nine digits, imo seven digits with check digit).",
                    "items", JsonSchemas.openObject("One item", Map.of(), "value"))), "watchlistId", "items");

    private final WatchlistService watchlists;
    private final WatchlistMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public AddToWatchlistTool(WatchlistService watchlists, WatchlistMapper mapper, McpOutputShaper shaper) {
        this.watchlists = watchlists;
        this.mapper = mapper;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "add_to_watchlist";
    }

    @Override
    public String title() {
        return "Add to a watchlist";
    }

    @Override
    public String description() {
        return "Adds identifiers to a watchlist (or updates their label, notes and tags). Takes effect on each entity's"
                + " next observation; nothing fires for the past. Invalid values are reported, valid ones applied."
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
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectNode body = shaper.object();
        body.set("items", args.get("items"));
        return ToolResult.json(shaper.objectTree(mapper.result(watchlists.add(McpArgs.requiredUuid(args, "watchlistId"),
                body))));
    }
}
