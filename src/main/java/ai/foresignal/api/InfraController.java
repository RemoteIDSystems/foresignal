package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.infra.InfraGraphBuilder;
import ai.foresignal.infra.InfraImpactService;
import ai.foresignal.source.spi.Geo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The infrastructure graph API (prompt 104). */
@RestController
@Tag(name = "Infrastructure")
@RequestMapping("/v1/infra")
public class InfraController {

    private final JdbcTemplate jdbc;
    private final InfraGraphBuilder builder;
    private final InfraImpactService impact;
    private final JsonMapper json;

    /** Creates the controller. */
    public InfraController(JdbcTemplate jdbc, InfraGraphBuilder builder, InfraImpactService impact, JsonMapper json) {
        this.jdbc = jdbc;
        this.builder = builder;
        this.impact = impact;
        this.json = json;
    }

    /** {@code listInfraNodes}. */
    @GetMapping("/nodes")
    @Operation(operationId = "listInfraNodes", tags = "Infrastructure")
    public Map<String, Object> listInfraNodes(@RequestParam(required = false) String bbox,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        StringBuilder sql = new StringBuilder("SELECT id, facility_type, name, source_id, external_id,"
                + " attributes::text AS attributes, ST_AsText(point) AS wkt FROM infra_node WHERE 1=1");
        List<Object> args = new java.util.ArrayList<>();
        if (type != null) {
            sql.append(" AND facility_type = ?");
            args.add(type);
        }
        if (q != null) {
            sql.append(" AND name ILIKE ?");
            args.add("%" + q + "%");
        }
        sql.append(" LIMIT ").append(size);
        List<Map<String, Object>> items = jdbc.query(sql.toString(), (rs, n) -> node(rs, n), args.toArray());
        return page(items);
    }

    /** {@code getInfraNode}. */
    @GetMapping("/nodes/{id}")
    @Operation(operationId = "getInfraNode", tags = "Infrastructure")
    public Map<String, Object> getInfraNode(@PathVariable long id) {
        List<Map<String, Object>> rows = jdbc.query("SELECT id, facility_type, name, source_id, external_id,"
                        + " attributes::text AS attributes, ST_AsText(point) AS wkt FROM infra_node WHERE id = ?",
                (rs, n) -> node(rs, n), id);
        if (rows.isEmpty()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_INFRA_NODE, "No such infrastructure node: " + id);
        }
        Map<String, Object> out = rows.get(0);
        out.put("outEdges", jdbc.queryForList(
                "SELECT id, to_node AS \"toNode\", relation, method, label, score FROM infra_edge WHERE from_node = ?",
                id));
        out.put("inEdges", jdbc.queryForList(
                "SELECT id, from_node AS \"fromNode\", relation, method, label, score FROM infra_edge WHERE to_node = ?",
                id));
        return out;
    }

    /** {@code listInfraEdges}. */
    @GetMapping("/edges")
    @Operation(operationId = "listInfraEdges", tags = "Infrastructure")
    public Map<String, Object> listInfraEdges(@RequestParam(required = false) String bbox,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit) {
        int size = QueryParams.intInRange("limit", limit, 1, 500, 100);
        StringBuilder sql = new StringBuilder("SELECT id, from_node, to_node, relation, method, label, score FROM"
                + " infra_edge WHERE 1=1");
        List<Object> args = new java.util.ArrayList<>();
        if (method != null) {
            sql.append(" AND method = ?");
            args.add(method);
        }
        if (label != null) {
            sql.append(" AND label = ?");
            args.add(label);
        }
        sql.append(" LIMIT ").append(size);
        List<Map<String, Object>> items = jdbc.query(sql.toString(),
                (rs, n) -> Map.<String, Object>of("id", rs.getLong("id"), "fromNode", rs.getLong("from_node"),
                        "toNode", rs.getLong("to_node"), "relation", rs.getString("relation"),
                        "method", rs.getString("method"), "label", rs.getString("label"), "score", rs.getDouble("score")),
                args.toArray());
        return page(items);
    }

    /** {@code computeAffectedInfrastructure}. */
    @PostMapping("/affected")
    @Operation(operationId = "computeAffectedInfrastructure", tags = "Infrastructure")
    public Map<String, Object> computeAffectedInfrastructure(@RequestBody JsonNode body) {
        Geometry area = area(body);
        int depth = body.path("depth").asInt(2);
        String minLabel = body.path("minLabel").asString("possible");
        List<String> types = new java.util.ArrayList<>();
        if (body.has("types") && body.get("types").isArray()) {
            for (JsonNode node : body.get("types")) {
                types.add(node.asString());
            }
        }
        return impact.affected(area, depth, minLabel, types);
    }

    /** {@code getInfraStatus}. */
    @GetMapping("/status")
    @Operation(operationId = "getInfraStatus", tags = "Infrastructure")
    public Map<String, Object> getInfraStatus() {
        List<Map<String, Object>> rows = jdbc.query(
                "SELECT id, started, finished, rules_version AS \"rulesVersion\", nodes,"
                        + " edges_by_method::text AS \"edgesByMethod\", edges_by_label::text AS \"edgesByLabel\""
                        + " FROM infra_build ORDER BY id DESC LIMIT 1",
                (rs, n) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("started", rs.getTimestamp("started").toInstant());
                    row.put("finished", rs.getTimestamp("finished") == null ? null
                            : rs.getTimestamp("finished").toInstant());
                    row.put("rulesVersion", rs.getInt("rulesVersion"));
                    row.put("nodes", rs.getLong("nodes"));
                    row.put("edgesByMethod", rs.getString("edgesByMethod"));
                    row.put("edgesByLabel", rs.getString("edgesByLabel"));
                    return row;
                });
        return rows.isEmpty() ? Map.of("built", false) : rows.get(0);
    }

    /** {@code rebuildInfraGraph}. */
    @PostMapping("/rebuild")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(operationId = "rebuildInfraGraph", tags = "Infrastructure")
    public Map<String, Object> rebuildInfraGraph() {
        var build = builder.rebuild(Map.of());
        return Map.of("buildId", build.getId(), "nodes", build.getNodes(),
                "edgesByMethod", build.getEdgesByMethod() == null ? Map.of() : build.getEdgesByMethod());
    }

    private Geometry area(JsonNode body) {
        if (body.hasNonNull("geometry")) {
            return Geo.fromGeoJson(body.get("geometry").toString());
        }
        if (body.hasNonNull("eventId")) {
            List<String> wkt = jdbc.queryForList("SELECT ST_AsText(geom) FROM intelligence_event WHERE id = ?",
                    String.class, body.get("eventId").asString());
            if (!wkt.isEmpty()) {
                try {
                    return new org.locationtech.jts.io.WKTReader(Geo.factory()).read(wkt.get(0));
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static Map<String, Object> node(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getLong("id"));
        row.put("facilityType", rs.getString("facility_type"));
        row.put("name", rs.getString("name"));
        row.put("sourceId", rs.getString("source_id"));
        row.put("externalId", rs.getString("external_id"));
        row.put("attributes", rs.getString("attributes"));
        row.put("point", rs.getString("wkt"));
        return row;
    }

    private static Map<String, Object> page(List<Map<String, Object>> items) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", null);
        return out;
    }
}
