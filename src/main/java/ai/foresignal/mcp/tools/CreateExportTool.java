package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.openObject;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.export.ExportJobEntity;
import ai.foresignal.export.ExportJobService;
import ai.foresignal.export.ExportSpec;
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

/** {@code create_export} (agent): submits a bulk export, returning the job with its download URL. */
@Component
public class CreateExportTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("export", openObject("The export specification"
            + " (ExportSpec): format (geoparquet, geopackage, flatgeobuf or gpx; default geoparquet), sources[],"
            + " kinds[], bbox [minLon,minLat,maxLon,maxLat] or placeId, from/to with timeField"
            + " (effective|observed|event|ingested), mode (current|history|asOf|atSeq), asOf or atSeq, fields[],"
            + " flatten, includePrivate (admin), personalUse.", new LinkedHashMap<>(), "format")), "export");

    private final ExportJobService exports;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public CreateExportTool(ExportJobService exports, McpOutputShaper shaper) {
        this.exports = exports;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "create_export";
    }

    @Override
    public String title() {
        return "Create a bulk export";
    }

    @Override
    public String description() {
        return "Submits an asynchronous bulk export of records (GeoParquet, GeoPackage, FlatGeobuf or GPX) and"
                + " returns the job with its download and manifest URLs. Poll it with get_export. The export carries"
                + " a licence sidecar and a signed manifest; FORBIDDEN and LOCAL_ONLY sources are excluded unless"
                + " personalUse is set. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created export job with its downloadUrl and manifestUrl");
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
        ExportSpec spec = ExportSpec.parse(args.get("export"));
        boolean admin = context.caller().scope() != null && context.caller().scope().includes(Scope.ADMIN);
        ExportJobService.Caller caller = new ExportJobService.Caller(context.actor(), admin,
                context.readCaller().privateAccess(), context.restriction());
        ExportJobEntity job = exports.submit(spec, caller);
        Map<String, Object> view = exports.view(job, "");
        return ToolResult.json(shaper.object(shaper.objectTree(view), Detail.LONG));
    }
}
