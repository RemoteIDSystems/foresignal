package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.watch.WatchListEntity;
import ai.foresignal.watch.WatchlistMapper;
import ai.foresignal.watch.WatchlistService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code get_watchlist} (agent): one watchlist with its first items. */
@Component
public class GetWatchlistTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "watchlistId", uuid("The watchlist id (from list_watchlists)."),
            "limit", integer("Most items to return (1-500).", 1, 500, 100L),
            "detail", detail()), "watchlistId");

    private final WatchlistService watchlists;
    private final WatchlistMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetWatchlistTool(WatchlistService watchlists, WatchlistMapper mapper, McpOutputShaper shaper) {
        this.watchlists = watchlists;
        this.mapper = mapper;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_watchlist";
    }

    @Override
    public String title() {
        return "Get a watchlist";
    }

    @Override
    public String description() {
        return "Returns one watchlist and its items (value, label, notes, tags), ordered by value. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The watchlist with items");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        UUID id = McpArgs.requiredUuid(args, "watchlistId");
        WatchListEntity list = watchlists.get(id);
        WatchlistService.ItemPage page = watchlists.items(id, null, null, McpArgs.integer(args, "limit", 100));
        ObjectNode out = shaper.objectTree(mapper.watchlist(list));
        out.set("items", shaper.tree(page.items().stream().map(mapper::item).toList()));
        out.put("truncated", page.nextCursor() != null);
        return ToolResult.json(shaper.object(out, detail));
    }
}
