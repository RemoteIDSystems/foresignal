package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.outlook.index.IndexViews;
import ai.foresignal.outlook.index.RegionalIndexDefEntity;
import ai.foresignal.outlook.index.RegionalIndexService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_index} (read, prompt 126): a regional index with its weights origin, out-of-sample skill and values with
 * their decomposition (one region over a range, or the latest value per region); without {@code indexId}, the list
 * of index definitions.
 */
@RestrictedRead
@Component
public class GetIndexTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "indexId", string("Regional index id, e.g. instability."),
            "region", string("040 region code."),
            "from", string("First day (YYYY-MM-DD)."),
            "to", string("Last day (YYYY-MM-DD).")));

    private final RegionalIndexService indices;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetIndexTool(RegionalIndexService indices, JsonMapper json) {
        this.indices = indices;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_index";
    }

    @Override
    public String title() {
        return "Get regional index";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns a transparent regional index (0–100) with each component's normalized value and"
                + " contribution, the floors applied, its weights origin (default, fitted or operator) and its"
                + " out-of-sample skill. An index is a composite, never a probability; say so when quoting it.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The index and its values, or the list of indices");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.text(args, "indexId");
        if (id == null) {
            RegionalIndexService.Page page = indices.list(null, 100);
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("items", page.items().stream().map(d -> IndexViews.def(d, indices.ready(d))).toList());
            out.put("nextCursor", page.nextCursor());
            return ToolResult.json(json.valueToTree(out));
        }
        RegionalIndexDefEntity def = indices.get(id);
        String region = McpArgs.text(args, "region");
        LocalDate from = ai.foresignal.api.OutlookController.date("from", McpArgs.text(args, "from"));
        LocalDate to = ai.foresignal.api.OutlookController.date("to", McpArgs.text(args, "to"));
        return ToolResult.json(json.valueToTree(IndexViews.detail(def, indices.ready(def), region, from, to,
                indices.values(id, region, from, to), indices.latestFit(id))));
    }
}
