package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.InfraController;
import ai.foresignal.infra.InfraImpactService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code get_affected_infrastructure} (read, prompt 104): what depends on facilities inside an area,
 * caveats first, top 20 dependents.
 */
@RestrictedRead
@Component
public class GetAffectedInfrastructureTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "eventId", string("Use an intelligence event's area.", 1, 26),
            "geometry", JsonSchemas.openObject("A GeoJSON geometry to use as the area.", Map.of()),
            "record", JsonSchemas.openObject("A record whose geometry (or region polygon) is the area.", props(
                    "sourceId", string("The source id.", 1, 64),
                    "externalId", string("The external id.", 1, 256)), "sourceId", "externalId"),
            "depth", integer("Traversal depth, 1-3.", 1, 3, 2L),
            "minLabel", enumeration("The weakest edge label followed.", "possible", "confirmed", "likely",
                    "possible", "speculative"),
            "types", JsonSchemas.stringArray("Facility types to list (for example health.hospital).", 14)));

    private final InfraImpactService impact;
    private final JdbcTemplate jdbc;
    private final tools.jackson.databind.json.JsonMapper json;

    /** Creates the tool. */
    public GetAffectedInfrastructureTool(InfraImpactService impact, JdbcTemplate jdbc,
            tools.jackson.databind.json.JsonMapper json) {
        this.impact = impact;
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_affected_infrastructure";
    }

    @Override
    public String title() {
        return "Affected infrastructure";
    }

    @Override
    public String description() {
        return "What depends on the facilities inside an area, with paths and explicit uncertainty labels."
                + " Derived from open data; not authoritative; heuristic links may be wrong.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("Affected infrastructure");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        if (!args.hasNonNull("eventId") && !args.hasNonNull("geometry") && !args.hasNonNull("record")) {
            return ToolResult.error("Provide eventId, geometry or record.");
        }
        boolean privateAccess = context.caller().currentToken().map(CurrentToken::privateAccess).orElse(false);
        InfraImpactService.Seed seed = InfraController.seed(impact, args, privateAccess);
        Map<String, Object> result = impact.affected(seed, McpArgs.integer(args, "depth", 2),
                McpArgs.text(args, "minLabel") == null ? "possible" : McpArgs.text(args, "minLabel"),
                McpArgs.strings(args, "types"));
        List<?> dependents = (List<?>) result.getOrDefault("dependents", List.of());
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("caveats", result.get("caveats"));
        out.put("buildId", result.get("buildId"));
        out.put("approximateArea", result.get("approximateArea"));
        out.put("truncated", result.get("truncated"));
        out.put("summary", result.get("summary"));
        out.put("attribution", result.get("attribution"));
        out.put("dependents", dependents.subList(0, Math.min(20, dependents.size())));
        return ToolResult.json(json.valueToTree(out));
    }
}
