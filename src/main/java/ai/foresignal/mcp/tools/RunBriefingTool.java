package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.briefing.BriefingService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code run_briefing} (agent, cost-bearing): runs a briefing now, stores and delivers it, as
 * {@code runBriefing} (prompt 029). LLM mode spends the chat provider's budget, so a token is always
 * required.
 */
@Component
public class RunBriefingTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "briefingId", uuid("The briefing id (from list_briefings).")), "briefingId");

    private final BriefingService briefings;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public RunBriefingTool(BriefingService briefings, McpOutputShaper shaper) {
        this.briefings = briefings;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "run_briefing";
    }

    @Override
    public String title() {
        return "Run a briefing now";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Runs a briefing immediately (up to 120 s), stores the run and delivers it to the"
                + " briefing's channels, then returns it. Fails with BRIEFING_RUNNING while a run of it is in progress."
                + " Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The briefing run");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean costBearing() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectNode run = shaper.objectTree(briefings.runNow(McpArgs.requiredUuid(args, "briefingId")));
        run.remove("stats");
        return ToolResult.json(shaper.object(run, Detail.LONG));
    }
}
