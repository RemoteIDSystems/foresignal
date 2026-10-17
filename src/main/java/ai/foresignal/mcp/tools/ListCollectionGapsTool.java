package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.hypothesis.CollectionGapEntity;
import ai.foresignal.hypothesis.HypothesisAnalysis;
import ai.foresignal.hypothesis.HypothesisService;
import ai.foresignal.hypothesis.HypothesisViews;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code list_collection_gaps} (read, prompt 125): indicators that would matter but cannot currently be observed,
 * with recommendations that never change configuration.
 */
@RestrictedRead("collection gaps hold statistics and recommendations, no record content; private sets need private"
        + " access")
@Component
public class ListCollectionGapsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "setId", string("Only gaps of this hypothesis set (ULID)."),
            "limit", integer("How many, 1 to 200.", 1, 200, 50L)));

    private final HypothesisAnalysis analysis;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListCollectionGapsTool(HypothesisAnalysis analysis, IntelVisibility visibility, JsonMapper json) {
        this.analysis = analysis;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_collection_gaps";
    }

    @Override
    public String title() {
        return "List collection gaps";
    }

    @Override
    public String description() {
        return "Lists collection gaps: indicators with high potential information gain that cannot currently be"
                + " observed, with recommendations (enable a cataloged source, add a local receiver or sensor, or"
                + " none). Recommendations are advice; enabling a source stays an operator action.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A page of collection gaps");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        HypothesisService.Page<CollectionGapEntity> page = analysis.listGaps(McpArgs.text(args, "setId"), null,
                Math.max(1, Math.min(200, McpArgs.integer(args, "limit", 50))),
                visibility.seesPrivate(context.caller()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (CollectionGapEntity g : page.items()) {
            items.add(HypothesisViews.gap(g));
        }
        return ToolResult.json(json.valueToTree(HypothesisViews.page(items, page.nextCursor())));
    }
}
