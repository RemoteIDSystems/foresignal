package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** {@code list_analyst_proposals} (agent, prompt 102). */
@Component
public class ListAnalystProposalsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "status", enumeration("Proposal status.", "pending", "pending", "executed", "rejected", "expired", "failed"),
            "limit", integer("How many at most.", 1, 200, 50L)));

    private final AnalystService analyst;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public ListAnalystProposalsTool(AnalystService analyst, McpOutputShaper shaper) {
        this.analyst = analyst;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "list_analyst_proposals";
    }

    @Override
    public String title() {
        return "List analyst proposals";
    }

    @Override
    public String description() {
        return "Lists the analyst's pending and decided proposals. A proposal executes only when confirmed"
                + " (or auto-executed by an action-enabled profile with an active agent-scope owner).";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Analyst proposals");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        int limit = McpArgs.integer(args, "limit", 50);
        List<ObjectNode> items = new ArrayList<>();
        for (var proposal : analyst.listProposals(McpArgs.text(args, "status"), limit)) {
            items.add(shaper.objectTree(AnalystViews.proposal(proposal)));
        }
        return ToolResult.json(shaper.list(items, items.size(), false, Detail.SHORT, null));
    }
}
