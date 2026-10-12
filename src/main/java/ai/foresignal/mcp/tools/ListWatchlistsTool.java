package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.watch.WatchlistMapper;
import ai.foresignal.watch.WatchlistService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_watchlists} (agent): the watchlists with their identity and item counts. */
@Component
public class ListWatchlistsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("detail", detail()));

    private final WatchlistService watchlists;
    private final WatchlistMapper mapper;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListWatchlistsTool(WatchlistService watchlists, WatchlistMapper mapper, McpOutputShaper shaper) {
        this.watchlists = watchlists;
        this.mapper = mapper;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_watchlists";
    }

    @Override
    public String title() {
        return "List watchlists";
    }

    @Override
    public String description() {
        return "Lists the watchlists (lists of aircraft ICAO addresses, registrations, callsigns, vessel MMSIs or IMO"
                + " numbers) with their identity and item count. Use a watchlist id in a watch or view condition"
                + " {op: inWatchlist, value: <id>}. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Watchlists");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        List<ObjectNode> out = watchlists.all().stream().map(l -> shaper.objectTree(mapper.watchlist(l))).toList();
        return ToolResult.json(shaper.list(out, out.size(), false, detail, null));
    }
}
