package ai.foresignal.infra;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxFilter;
import ai.foresignal.api.support.BboxParser;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the nodes and edges of the <em>current</em> infrastructure build (prompt 104) for the API and
 * MCP: only the newest successful build is served, so a rebuild never shows two copies of the graph.
 * Lists filter by {@code bbox}, facility type, name, method and label, and page by id with an
 * opaque cursor bound to the build. Every response carries the attribution of the sources it
 * includes ("© OpenStreetMap contributors" for OSM-derived nodes and edges).
 */
@Component
public class InfraNodeQuery {

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private static final String NODE_COLUMNS = "id, facility_type, name, source_id, external_id, licence,"
            + " attributes::text AS attributes, region_tags::text AS region_tags, ST_AsGeoJSON(geom) AS geom_json,"
            + " ST_AsGeoJSON(point) AS point_json, built_at";

    private static final String EDGE_COLUMNS = "e.id, e.from_node, e.to_node, e.relation, e.method, e.label,"
            + " e.score, e.rule_id, e.rule_version, e.params::text AS params, e.evidence::text AS evidence, e.licence,"
            + " e.built_at";

    private final NamedParameterJdbcTemplate jdbc;
    private final InfraImpactService impact;
    private final JsonMapper json;

    /** Creates the query. */
    public InfraNodeQuery(NamedParameterJdbcTemplate jdbc, InfraImpactService impact, JsonMapper json) {
        this.jdbc = jdbc;
        this.impact = impact;
        this.json = json;
    }

    /**
     * One page of nodes of the current build.
     *
     * @param bbox {@code minLon,minLat,maxLon,maxLat}, or null
     * @param types facility types, or empty
     * @param q a case-insensitive name fragment, or null
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     * @throws ai.foresignal.api.error.ConflictException 409 {@code INFRA_GRAPH_NOT_BUILT}
     */
    public Map<String, Object> nodes(String bbox, List<String> types, String q, String cursor, int limit) {
        long buildId = impact.currentBuild();
        MapSqlParameterSource params = new MapSqlParameterSource("b", buildId).addValue("after", after(cursor, buildId));
        StringBuilder sql = new StringBuilder("SELECT " + NODE_COLUMNS + " FROM infra_node WHERE build_id = :b"
                + " AND id > :after");
        if (types != null && !types.isEmpty()) {
            sql.append(" AND facility_type IN (:types)");
            params.addValue("types", types);
        }
        if (q != null && !q.isBlank()) {
            sql.append(" AND name ILIKE :q");
            params.addValue("q", "%" + q.replace("%", "\\%").replace("_", "\\_") + "%");
        }
        appendBbox(sql, params, "point", BboxParser.parse(bbox));
        sql.append(" ORDER BY id LIMIT ").append(limit + 1);
        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, (rs, n) -> node(rs));
        return page(rows, limit, buildId, rows.stream().map(r -> String.valueOf(r.get("sourceId"))).toList());
    }

    /**
     * One node of the current build with its in and out edges.
     *
     * @throws NotFoundException 404 {@code UNKNOWN_INFRA_NODE}, also for a node of a superseded build
     */
    public Map<String, Object> node(long id) {
        long buildId = impact.currentBuild();
        MapSqlParameterSource params = new MapSqlParameterSource("b", buildId).addValue("id", id);
        List<Map<String, Object>> rows = jdbc.query("SELECT " + NODE_COLUMNS + " FROM infra_node WHERE build_id = :b"
                + " AND id = :id", params, (rs, n) -> node(rs));
        if (rows.isEmpty()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_INFRA_NODE, "No such infrastructure node: " + id);
        }
        Map<String, Object> out = rows.getFirst();
        List<Map<String, Object>> outEdges = jdbc.query("SELECT " + EDGE_COLUMNS + " FROM infra_edge e WHERE"
                + " e.build_id = :b AND e.from_node = :id ORDER BY e.id", params, (rs, n) -> edge(rs));
        List<Map<String, Object>> inEdges = jdbc.query("SELECT " + EDGE_COLUMNS + " FROM infra_edge e WHERE"
                + " e.build_id = :b AND e.to_node = :id ORDER BY e.id", params, (rs, n) -> edge(rs));
        Map<String, Object> edges = new LinkedHashMap<>();
        edges.put("in", inEdges);
        edges.put("out", outEdges);
        out.put("edges", edges);
        Set<String> sources = new LinkedHashSet<>();
        sources.add(String.valueOf(out.get("sourceId")));
        if (osmEdge(outEdges) || osmEdge(inEdges)) {
            sources.add(InfraGraphBuilder.OSM_SOURCE);
        }
        out.put("attribution", impact.attribution(sources));
        return out;
    }

    /**
     * One page of edges of the current build.
     *
     * @param bbox edges with either end's point in the box, or null
     * @param methods edge methods, or empty
     * @param labels edge labels, or empty
     * @param cursor the previous page's cursor, or null
     * @param limit the page size
     */
    public Map<String, Object> edges(String bbox, List<String> methods, List<String> labels, String cursor, int limit) {
        long buildId = impact.currentBuild();
        MapSqlParameterSource params = new MapSqlParameterSource("b", buildId).addValue("after", after(cursor, buildId));
        StringBuilder sql = new StringBuilder("SELECT " + EDGE_COLUMNS + " FROM infra_edge e WHERE e.build_id = :b"
                + " AND e.id > :after");
        if (methods != null && !methods.isEmpty()) {
            sql.append(" AND e.method IN (:methods)");
            params.addValue("methods", methods);
        }
        if (labels != null && !labels.isEmpty()) {
            sql.append(" AND e.label IN (:labels)");
            params.addValue("labels", labels);
        }
        BboxFilter filter = BboxParser.parse(bbox);
        if (filter != null) {
            StringBuilder inner = new StringBuilder();
            appendBbox(inner, params, "n.point", filter);
            sql.append(" AND EXISTS (SELECT 1 FROM infra_node n WHERE n.id IN (e.from_node, e.to_node)")
                    .append(inner).append(')');
        }
        sql.append(" ORDER BY e.id LIMIT ").append(limit + 1);
        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, (rs, n) -> edge(rs));
        List<String> sources = new ArrayList<>();
        if (osmEdge(rows)) {
            sources.add(InfraGraphBuilder.OSM_SOURCE);
        }
        return page(rows, limit, buildId, sources);
    }

    private static boolean osmEdge(List<Map<String, Object>> edges) {
        for (Map<String, Object> edge : edges) {
            if (String.valueOf(edge.get("licence")).contains("ODbL")) {
                return true;
            }
        }
        return false;
    }

    private static void appendBbox(StringBuilder sql, MapSqlParameterSource params, String column, BboxFilter filter) {
        if (filter == null) {
            return;
        }
        List<String> boxes = new ArrayList<>();
        int i = 0;
        for (Envelope envelope : filter.envelopes()) {
            String p = "bbox" + i++;
            boxes.add(column + " && ST_MakeEnvelope(:" + p + "x0, :" + p + "y0, :" + p + "x1, :" + p + "y1, 4326)");
            params.addValue(p + "x0", envelope.getMinX()).addValue(p + "y0", envelope.getMinY())
                    .addValue(p + "x1", envelope.getMaxX()).addValue(p + "y1", envelope.getMaxY());
        }
        sql.append(" AND (").append(String.join(" OR ", boxes)).append(')');
    }

    private Map<String, Object> page(List<Map<String, Object>> rows, int limit, long buildId, List<String> sources) {
        boolean more = rows.size() > limit;
        List<Map<String, Object>> items = more ? new ArrayList<>(rows.subList(0, limit)) : rows;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", more ? cursor(buildId, ((Number) items.getLast().get("id")).longValue()) : null);
        out.put("buildId", buildId);
        out.put("attribution", impact.attribution(new LinkedHashSet<>(sources)));
        return out;
    }

    /** The opaque cursor of {@code (build, last id)}. */
    static String cursor(long buildId, long lastId) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("i1:" + buildId + ":" + lastId).getBytes(StandardCharsets.UTF_8));
    }

    /** The last id of a cursor (0 without one); a cursor of another build is a 400. */
    static long after(String cursor, long buildId) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8).split(":");
            if (parts.length != 3 || !"i1".equals(parts[0])) {
                throw new IllegalArgumentException("bad cursor");
            }
            if (Long.parseLong(parts[1]) != buildId) {
                throw ValidationException.forField("cursor", "belongs to a superseded graph build; start again");
            }
            return Long.parseLong(parts[2]);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField("cursor", "is not a valid cursor");
        }
    }

    private Map<String, Object> node(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getLong("id"));
        row.put("facilityType", rs.getString("facility_type"));
        row.put("name", rs.getString("name"));
        row.put("sourceId", rs.getString("source_id"));
        row.put("externalId", rs.getString("external_id"));
        String attributes = rs.getString("attributes");
        row.put("attributes", attributes == null ? Map.of() : json.readValue(attributes, MAP));
        String tags = rs.getString("region_tags");
        row.put("regionTags", tags == null ? List.of() : json.readValue(tags, List.class));
        row.put("licence", rs.getString("licence"));
        row.put("geometry", geoJson(rs.getString("geom_json")));
        row.put("point", geoJson(rs.getString("point_json")));
        row.put("builtAt", rs.getTimestamp("built_at") == null ? null : rs.getTimestamp("built_at").toInstant());
        return row;
    }

    /** A GeoJSON geometry object from {@code ST_AsGeoJSON} text, or null. */
    private Map<String, Object> geoJson(String text) {
        return text == null ? null : json.readValue(text, MAP);
    }

    private Map<String, Object> edge(ResultSet rs) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", rs.getLong("id"));
        row.put("fromNode", rs.getLong("from_node"));
        row.put("toNode", rs.getLong("to_node"));
        row.put("relation", rs.getString("relation"));
        row.put("method", rs.getString("method"));
        row.put("label", rs.getString("label"));
        row.put("score", rs.getDouble("score"));
        row.put("ruleId", rs.getString("rule_id"));
        row.put("ruleVersion", String.valueOf(rs.getInt("rule_version")));
        String params = rs.getString("params");
        row.put("params", params == null ? Map.of() : json.readValue(params, MAP));
        String evidence = rs.getString("evidence");
        row.put("evidence", evidence == null ? Map.of() : json.readValue(evidence, MAP));
        row.put("licence", rs.getString("licence"));
        row.put("builtAt", rs.getTimestamp("built_at") == null ? null : rs.getTimestamp("built_at").toInstant());
        return row;
    }
}
