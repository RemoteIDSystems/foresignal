package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.cycle.AssessmentService;
import ai.foresignal.cycle.CycleScheduler;
import ai.foresignal.cycle.CycleService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code run_cycle} (agent, prompt 128): runs an analysis cycle now and returns the queued run. */
@ai.foresignal.security.restrict.RestrictedRead("starts a run; returns its id and status, no record content")
@Component
public class RunCycleTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(Map.of("cycleId", string("The analysis cycle id.", 1, 26)),
            "cycleId");

    private final CycleService cycles;
    private final CycleScheduler scheduler;
    private final AssessmentService assessments;
    private final JsonMapper json;

    /** Creates the tool. */
    public RunCycleTool(CycleService cycles, @Lazy CycleScheduler scheduler, AssessmentService assessments,
            JsonMapper json) {
        this.cycles = cycles;
        this.scheduler = scheduler;
        this.assessments = assessments;
        this.json = json;
    }

    @Override
    public String name() {
        return "run_cycle";
    }

    @Override
    public String title() {
        return "Run analysis cycle";
    }

    @Override
    public String description() {
        return "Runs an analysis cycle now: the deterministic packet, the configured LLM roles within budget, and an"
                + " assessment (digest-only without an LLM). Returns the queued run. Spending path: it uses the"
                + " configured LLM and its budget.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The queued cycle run");
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
    public ToolResult call(ToolContext context, JsonNode args) {
        var cycle = cycles.get(McpArgs.requiredText(args, "cycleId"));
        try {
            return ToolResult.json(json.valueToTree(assessments.runView(scheduler.runNow(cycle), false)));
        } catch (ConflictException e) {
            throw new McpToolException(e.getMessage());
        }
    }
}
