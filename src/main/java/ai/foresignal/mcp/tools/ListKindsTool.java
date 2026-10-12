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
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.Slices;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_kinds} (read): the kinds currently present, with counts and sources, as {@code listKinds}. */
@Component
public class ListKindsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("detail", detail()));

    private final RecordQueryService queries;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListKindsTool(RecordQueryService queries, McpOutputShaper shaper) {
        this.queries = queries;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_kinds";
    }

    @Override
    public String title() {
        return "List record kinds";
    }

    @Override
    public String description() {
        return "Lists the kinds of record currently stored (for example earthquake, aircraft, weather-alert), each"
                + " with the number of current records and the sources that emit it. Use the kind names as the kind"
                + " filter of query_records and in watches.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Kinds with counts");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        return ToolResult.json(list(context.excludedSources(), detail));
    }

    /** The kinds visible with {@code excluded} hidden, as a capped list (shared with the resource). */
    ObjectNode list(Set<String> excluded, Detail detail) {
        List<ObjectNode> items = new ArrayList<>();
        for (Slices.KindRow row : queries.kinds(excluded)) {
            ObjectNode item = shaper.object();
            item.put("kind", row.kind());
            item.put("count", row.count());
            item.set("sources", shaper.tree(row.sources()));
            items.add(item);
        }
        return shaper.list(items, items.size(), false, detail, null);
    }
}
