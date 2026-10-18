package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.doctor.DoctorFinding;
import ai.foresignal.doctor.DoctorReportView;
import ai.foresignal.doctor.DoctorService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code get_doctor_report} (agent; prompt 140): one adapter-doctor report in compact form: the verdict, the findings
 * (code, severity, path, suggested action), the LLM summary and likely cause; the proposed patch only with
 * {@code detail=long}, always labeled "proposed, not applied". A report of a source the caller may not see is unknown.
 */
@Component
@RestrictedRead("adapter-doctor diagnostics: findings, structure paths and capped examples, never records;"
        + " a report of a hidden source is unknown")
public class GetDoctorReportTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "reportId", string("The doctor report id (a ULID), from diagnose_source or the source's health.", 26, 26),
            "detail", JsonSchemas.detail()), "reportId");

    private final DoctorService doctor;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetDoctorReportTool(DoctorService doctor, McpOutputShaper shaper) {
        this.doctor = doctor;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_doctor_report";
    }

    @Override
    public String title() {
        return "Get an adapter-doctor report";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one adapter-doctor report: its verdict (OK, DEGRADED_INFO, BROKEN), the"
                + " deterministic findings with a suggested action each (for example FIELD_RENAMED, PARSER_REJECTS,"
                + " AUTH_FAILED), and the LLM explanation when one was asked for. With detail=long it adds the"
                + " structure diff and the parser patch, which is proposed, not applied: a human reviews it and applies"
                + " it by hand.";
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
        return Scope.AGENT;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String reportId = McpArgs.requiredText(args, "reportId");
        McpOutputShaper.Detail detail = McpOutputShaper.Detail.of(args);
        if (doctor.isRunning(reportId)) {
            return ToolResult.text("Doctor report " + reportId + " is still running; ask again in a minute.");
        }
        Optional<DoctorReportView> report = doctor.report(reportId);
        if (report.isEmpty() || context.excludedSources().contains(report.get().sourceId())) {
            return ToolResult.error("There is no doctor report '" + reportId + "'.");
        }
        return ToolResult.json(shaper.tree(compact(report.get(), detail == McpOutputShaper.Detail.LONG)));
    }

    /**
     * The compact form of a report shared by {@code get_doctor_report} and {@code diagnose_source}: findings, summary,
     * and the patch, diff and probe only when {@code full}.
     */
    public static Map<String, Object> compact(DoctorReportView report, boolean full) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", report.id());
        out.put("sourceId", report.sourceId());
        out.put("status", report.status().name());
        out.put("trigger", report.trigger().name());
        out.put("createdAt", report.createdAt());
        List<Map<String, Object>> findings = new ArrayList<>();
        for (DoctorFinding f : report.findings()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", f.code().name());
            m.put("severity", f.severity().wireValue());
            if (f.path() != null) {
                m.put("path", f.path());
            }
            m.put("suggestedAction", f.suggestedAction());
            if (full) {
                m.put("evidence", f.evidence());
                m.put("heuristic", f.heuristic());
            }
            findings.add(m);
        }
        out.put("findings", findings);
        Map<String, Object> llm = report.llm();
        if (llm != null) {
            out.put("llmStatus", llm.get("status"));
            if (llm.get("summary") != null) {
                out.put("summary", llm.get("summary"));
            }
            if (llm.get("likelyCause") != null) {
                out.put("likelyCause", llm.get("likelyCause"));
            }
            if (full) {
                out.put("suggestedActions", llm.get("suggestedActions"));
                out.put("patch", llm.get("patch"));
            } else if (llm.get("patch") != null) {
                out.put("patchAvailable", "a patch was proposed (not applied); ask with detail=long to see it");
            }
        }
        if (full) {
            out.put("diff", report.diff());
            out.put("probe", report.probe());
            out.put("fixture", report.fixture());
        }
        return out;
    }
}
