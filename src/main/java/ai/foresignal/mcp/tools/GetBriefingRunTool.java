package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.uuid;

import ai.foresignal.briefing.BriefingRunEntity;
import ai.foresignal.briefing.BriefingService;
import ai.foresignal.briefing.BriefingViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code get_briefing_run} (read): one run of a briefing, or its latest, as {@code getBriefingRun}
 * and {@code getLatestBriefingRun} (prompt 029). The short form leaves out the statistics.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class GetBriefingRunTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "briefingId", uuid("The briefing id (from list_briefings)."),
            "runId", uuid("The run id; omit for the latest completed run."),
            "detail", detail()), "briefingId");

    private final BriefingService briefings;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetBriefingRunTool(BriefingService briefings, McpOutputShaper shaper) {
        this.briefings = briefings;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_briefing_run";
    }

    @Override
    public String title() {
        return "Get a briefing run";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns a briefing run: its markdown, the period it covers, its status (succeeded,"
                + " fallback when LLM mode fell back to the digest, failed), the verified citations and any warnings."
                + " Omit runId for the latest run. Cite a run as [briefing:runId]; detail=long adds the statistics.";
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
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        UUID briefingId = McpArgs.requiredUuid(args, "briefingId");
        UUID runId = McpArgs.uuid(args, "runId");
        briefings.requireVisible(briefingId, context.restriction());
        BriefingRunEntity run = runId == null ? briefings.latest(briefingId) : briefings.run(briefingId, runId);
        // A run that cites a private source is shown only to callers that can see private sources, and
        // never into an Ask loop whose provider may not receive them (its excluded set, prompt 039).
        java.util.Set<String> hidden = new java.util.TreeSet<>(briefings.privateSources());
        if (ai.foresignal.security.visibility.SourceVisibility.canSeePrivate(context.caller().readCaller().scope(),
                context.caller().readCaller().privateAccess())) {
            hidden.retainAll(context.excludedSources());
        }
        if (briefings.citesAny(run, hidden)) {
            throw ai.foresignal.api.error.NotFoundException.of("Briefing run", run.getId());
        }
        ObjectNode out = shaper.objectTree(BriefingViews.run(run));
        out.put("title", run.getBriefingName());
        if (detail == Detail.SHORT) {
            out.remove("stats");
            out.remove("usage");
        }
        return ToolResult.json(shaper.object(out, detail));
    }
}
