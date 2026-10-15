package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.analyst.AnalystService;
import ai.foresignal.analyst.AnalystViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_analyst_notes} (read, prompt 102). */
@RestrictedRead
@Component
public class ListAnalystNotesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "profileId", string("Only this profile.", 1, 26),
            "status", enumeration("Note status.", null, "new", "read", "dismissed"),
            "priority", enumeration("Note priority.", null, "low", "normal", "high"),
            "limit", integer("How many at most.", 1, 200, 50L)));

    private final AnalystService analyst;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListAnalystNotesTool(AnalystService analyst, McpOutputShaper shaper) {
        this.analyst = analyst;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_analyst_notes";
    }

    @Override
    public String title() {
        return "List analyst notes";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Lists the autonomous analyst's notes, with their typed, cited claims."
                + " Evidence is cited; inference and speculation are labelled.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Analyst notes");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        int limit = McpArgs.integer(args, "limit", 50);
        List<ObjectNode> items = new ArrayList<>();
        for (var note : analyst.listNotes(McpArgs.text(args, "profileId"), McpArgs.text(args, "status"),
                McpArgs.text(args, "priority"), null, limit)) {
            items.add(shaper.objectTree(AnalystViews.note(note)));
        }
        return ToolResult.json(shaper.list(items, items.size(), false, Detail.SHORT, null));
    }
}
