package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.doctor.DoctorOptions;
import ai.foresignal.doctor.DoctorReportView;
import ai.foresignal.doctor.DoctorService;
import ai.foresignal.doctor.DoctorTrigger;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code diagnose_source} (admin, cost-bearing; prompt 140): runs the adapter doctor on one source with
 * {@code trigger=MCP} and returns the report in compact form (see {@link GetDoctorReportTool#compact}). It spends the
 * source's request budget on a probe and, with {@code llm} auto or on, LLM tokens, so it always needs a token.
 */
@Component
@RestrictedRead("adapter-doctor diagnostics: findings, structure paths and capped examples, never records;"
        + " a hidden source is unknown")
public class DiagnoseSourceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "sourceId", string("The source id, e.g. usgs-earthquakes.", 1, 64),
            "probe", bool("Probe the live upstream (spends one budgeted request).", true),
            "llm", enumeration("auto (default): add an LLM explanation and patch proposal when a chat provider is"
                    + " assigned; on: ask for it; off: deterministic findings only.", "auto", "auto", "on", "off"),
            "context", enumeration("compact (default, fits small local models) or full (up to 30k tokens of LLM"
                    + " context).", "compact", "compact", "full"),
            "detail", JsonSchemas.detail()), "sourceId");

    private final DoctorService doctor;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public DiagnoseSourceTool(DoctorService doctor, McpOutputShaper shaper) {
        this.doctor = doctor;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "diagnose_source";
    }

    @Override
    public String title() {
        return "Diagnose a source with the adapter doctor";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Runs the adapter doctor on a source whose health is degraded or failing: probes the"
                + " upstream once, compares its structure with the recorded fixture, runs deterministic rules"
                + " (renamed or removed fields, type and format changes, HTML instead of data, auth and rate-limit"
                + " problems) and optionally asks the LLM to explain the change and propose a parser patch, which is"
                + " proposed, not applied. At most one run per source per 10 minutes. Needs an admin token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("DoctorReport (compact) {id, sourceId, status, trigger, createdAt, findings,"
                + " summary, likelyCause, patch (detail=long)}");
    }

    @Override
    public Scope requiredScope() {
        return Scope.ADMIN;
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
        String sourceId = McpArgs.requiredText(args, "sourceId");
        if (context.excludedSources().contains(sourceId)) {
            return ToolResult.error("There is no source '" + sourceId + "'.");
        }
        McpOutputShaper.Detail detail = McpOutputShaper.Detail.of(args);
        DoctorOptions options = DoctorOptions.of(McpArgs.optionalBool(args, "probe"), McpArgs.text(args, "llm"),
                McpArgs.text(args, "context"));
        DoctorReportView report = doctor.run(sourceId, options, DoctorTrigger.MCP);
        return ToolResult.json(shaper.tree(GetDoctorReportTool.compact(report, detail == McpOutputShaper.Detail.LONG)));
    }
}
