package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.infra.InfraImpactService;
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
            "depth", integer("Traversal depth, 1-3.", 1, 3, 2L)));

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
        Geometry area = null;
        if (McpArgs.text(args, "eventId") != null) {
            List<String> wkt = jdbc.queryForList("SELECT ST_AsText(geom) FROM intelligence_event WHERE id = ?",
                    String.class, McpArgs.text(args, "eventId"));
            if (!wkt.isEmpty()) {
                try {
                    area = new org.locationtech.jts.io.WKTReader(Geo.factory()).read(wkt.get(0));
                } catch (Exception e) {
                    area = null;
                }
            }
        } else if (args.hasNonNull("geometry")) {
            area = Geo.fromGeoJson(args.get("geometry").toString());
        }
        if (area == null) {
            return ToolResult.error("Provide eventId or geometry.");
        }
        Map<String, Object> result = impact.affected(area, McpArgs.integer(args, "depth", 2), "possible", List.of());
        List<?> dependents = (List<?>) result.getOrDefault("dependents", List.of());
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("caveats", result.get("caveats"));
        out.put("summary", result.get("summary"));
        out.put("attribution", result.get("attribution"));
        out.put("dependents", dependents.subList(0, Math.min(20, dependents.size())));
        return ToolResult.json(json.valueToTree(out));
    }
}
