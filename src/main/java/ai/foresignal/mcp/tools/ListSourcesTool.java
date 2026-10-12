package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.api.dto.SourceSummary;
import ai.foresignal.api.mapper.SourceMapper;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpCaller;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_sources} (read): every source the caller may see, with its status, as {@code listSources}. */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class ListSourcesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("detail", detail()));

    private final SourceService sources;
    private final SourceMapper mapper;
    private final SourceVisibilityGate visibility;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListSourcesTool(SourceService sources, SourceMapper mapper, SourceVisibilityGate visibility,
            McpOutputShaper shaper) {
        this.sources = sources;
        this.mapper = mapper;
        this.visibility = visibility;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_sources";
    }

    @Override
    public String title() {
        return "List data sources";
    }

    @Override
    public String description() {
        return "Lists Foresignal's data sources (feeds such as USGS earthquakes or ADS-B aircraft), each with id,"
                + " name, kinds, whether it is enabled, its health status (healthy, degraded, failing, idle,"
                + " disabled) and when it last delivered data. Use the ids as the source filter of other tools."
                + " source_health explains a degraded or failing source.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Sources sorted by id");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(list(context.caller(), Detail.of(args)));
    }

    /** The sources {@code caller} may see, as a capped list (shared with the resource). */
    ObjectNode list(McpCaller caller, Detail detail) {
        List<ObjectNode> items = new ArrayList<>();
        sources.list().stream()
                .filter(view -> visibility.visible(view.id(), caller.currentToken()))
                .sorted(Comparator.comparing(SourceView::id))
                .map(mapper::summary)
                .forEach(summary -> items.add(item(summary, detail)));
        return shaper.list(items, items.size(), false, detail, null);
    }

    private ObjectNode item(SourceSummary summary, Detail detail) {
        ObjectNode full = shaper.objectTree(summary);
        if (detail == Detail.LONG) {
            return full;
        }
        ObjectNode out = shaper.object();
        for (String member : List.of("id", "name", "kinds", "mode", "enabled", "status", "lastDataAt")) {
            if (full.has(member)) {
                out.set(member, full.get(member));
            }
        }
        return out;
    }
}
