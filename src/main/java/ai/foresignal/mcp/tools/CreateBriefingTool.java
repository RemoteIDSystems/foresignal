package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.briefing.BriefingService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code create_briefing} (agent): creates a scheduled briefing with the REST validation, as {@code createBriefing}. */
@Component
public class CreateBriefingTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("briefing", openObject("The briefing definition"
            + " (BriefingInput): name, schedule (5-field cron such as '0 7 * * *', at most every 15 minutes),"
            + " timezone (IANA, default UTC), scope {sources, kinds, bbox, ruleIds, includeHealth}, lookback (1h-7d,"
            + " omit for since the last run), mode (digest or llm), instructions (llm mode), channelIds,"
            + " maxItemsPerKind.", new LinkedHashMap<>(), "name", "schedule")), "briefing");

    private final BriefingService briefings;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public CreateBriefingTool(BriefingService briefings, McpOutputShaper shaper) {
        this.briefings = briefings;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "create_briefing";
    }

    @Override
    public String title() {
        return "Create a briefing";
    }

    @Override
    public String description() {
        return "Creates a scheduled briefing delivered to the operator's channels: for example a morning digest of"
                + " earthquakes (schedule '0 7 * * *', timezone 'America/Chicago', scope {kinds: [earthquake]}, mode"
                + " digest). Digest mode needs no LLM; llm mode writes a cited narrative and falls back to the digest."
                + " Every problem is reported at once; channel problems come back as warnings. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created briefing with its id and warnings");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Map<String, Object> created = briefings.create(args.get("briefing"), context.caller());
        return ToolResult.json(shaper.object(shaper.objectTree(created), Detail.LONG));
    }
}
