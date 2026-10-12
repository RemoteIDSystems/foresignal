package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.system.SystemInfoService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code system_status} (read): the {@code /v1/system} document from {@link SystemInfoService}; the
 * full document for a token, the public subset without one (as {@code getSystem}).
 */
@Component
public class SystemStatusTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("detail", detail()));

    /** Members of the short form. */
    static final List<String> SHORT_MEMBERS = List.of("version", "status", "uptimeSeconds", "records", "sources",
            "ingestWriter", "catchUp", "mcp", "warnings");

    private final SystemInfoService system;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public SystemStatusTool(SystemInfoService system, McpOutputShaper shaper) {
        this.system = system;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "system_status";
    }

    @Override
    public String title() {
        return "System status";
    }

    @Override
    public String description() {
        return "Reports whether Foresignal is healthy: overall status (ok, degraded, down), version and uptime,"
                + " record counts and the latest seq, source health counts, the ingest writer and trigger catch-up,"
                + " and active warnings. Use it when results look stale or empty.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The /v1/system document (SystemInfo), or its short form");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        ObjectNode full = shaper.objectTree(system.build(!context.caller().anonymous()));
        if (detail == Detail.LONG) {
            return ToolResult.json(shaper.object(full, detail));
        }
        ObjectNode out = shaper.object();
        for (String member : SHORT_MEMBERS) {
            if (full.has(member)) {
                out.set(member, full.get(member));
            }
        }
        return ToolResult.json(shaper.object(out, detail));
    }
}
