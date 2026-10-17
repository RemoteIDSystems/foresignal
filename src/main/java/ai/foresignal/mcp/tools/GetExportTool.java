package ai.foresignal.mcp.tools;

import ai.foresignal.export.ExportJobService;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code get_export} (agent; prompt 111): an export, snapshot or pack job's status, license summary and
 * URLs, never the file bytes. A job is visible to the token that created it and to admin.
 */
@ai.foresignal.security.restrict.RestrictedRead("a job is visible to the token that created it and to admin; returns status and URLs")
@Component
public class GetExportTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = JsonSchemas.object(
            JsonSchemas.props("exportId", JsonSchemas.string("The export job id")), "exportId");

    private final ExportJobService exports;
    private final ProductUrls urls;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public GetExportTool(ExportJobService exports, ProductUrls urls, McpOutputShaper shaper) {
        this.exports = exports;
        this.urls = urls;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "get_export";
    }

    @Override
    public String title() {
        return "Get a bulk export";
    }

    @Override
    public String description() {
        return "Returns the status, progress, row count, license summary and download/manifest URLs of a bulk"
                + " export, snapshot or offline pack. It returns URLs, never file bytes.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The export job with its downloadUrl and manifestUrl");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ExportJobService.Caller caller = CreateExportTool.caller(context);
        String id = args.get("exportId") == null ? "" : args.get("exportId").asString();
        return ToolResult.json(shaper.object(shaper.objectTree(exports.view(
                exports.visible(id, caller.principal(), caller.admin()), urls.storedBase())), Detail.LONG));
    }
}
