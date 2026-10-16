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
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.visibility.Viewer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code create_export} (agent; prompt 111): submits a bulk export with the caller's rights and
 * returns the job with its download and manifest URLs, never file bytes.
 */
@Component
public class CreateExportTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("export", openObject("The export specification"
            + " (ExportSpec): format (geoparquet, geopackage, flatgeobuf or gpx; default geoparquet), sources[],"
            + " kinds[], bbox [minLon,minLat,maxLon,maxLat] or placeId, from/to with timeField"
            + " (effective|observed|event|ingested), mode (current|history|asOf|atSeq), asOf or atSeq, fields[],"
            + " flatten, includePrivate (admin), personalUse.", new LinkedHashMap<>(), "format")), "export");

    private final ExportJobService exports;
    private final ProductUrls urls;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public CreateExportTool(ExportJobService exports, ProductUrls urls, McpOutputShaper shaper) {
        this.exports = exports;
        this.urls = urls;
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
                + " a license sidecar and a signed manifest; FORBIDDEN and LOCAL_ONLY sources are excluded unless"
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
        ExportJobEntity job = exports.submit(spec, caller(context));
        return ToolResult.json(shaper.object(shaper.objectTree(exports.view(job, urls.storedBase())), Detail.LONG));
    }

    /** The export caller of an MCP session: the token id, admin flag, egress viewer and restriction. */
    static ExportJobService.Caller caller(ToolContext context) {
        Scope scope = context.readCaller().scope();
        boolean admin = scope != null && scope.includes(Scope.ADMIN);
        String principal = context.caller().token() == null || context.caller().token().id() == null ? null
                : context.caller().token().id().toString();
        Viewer viewer = new Viewer(scope, context.readCaller().privateAccess(), TokenRestrictions.NONE);
        return new ExportJobService.Caller(principal, admin, viewer, context.restriction());
    }
}
