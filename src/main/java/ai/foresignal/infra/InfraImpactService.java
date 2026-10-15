package ai.foresignal.infra;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * The affected-dependents query (prompt 104): given an area, seeds the nodes inside it and traverses
 * the dependency edges up to a depth, keeping only edges at or above a minimum label. Paths carry the
 * weakest-link label and the product score; the result always states its caveats and attribution.
 */
@Service
public class InfraImpactService {

    /** The most nodes returned before truncation. */
    public static final int NODE_LIMIT = 5_000;

    /** The label ranks. */
    static final Map<String, Integer> LABEL_RANK = Map.of("speculative", 0, "possible", 1, "likely", 2,
            "confirmed", 3);

    private final JdbcTemplate jdbc;
    private final InfraBuildRepository builds;

    /** Creates the service. */
    public InfraImpactService(JdbcTemplate jdbc, InfraBuildRepository builds) {
        this.jdbc = jdbc;
        this.builds = builds;
    }

    /** The current build id, or 409. */
    public long currentBuild() {
        return builds.findTop1ByOrderByIdDesc().map(InfraBuildEntity::getId).orElseThrow(
                () -> new ConflictException(ErrorCodes.INFRA_GRAPH_NOT_BUILT,
                        "The infrastructure graph has not been built yet."));
    }

    /** Computes the affected dependents. */
    public Map<String, Object> affected(Geometry area, int depth, String minLabel, List<String> types) {
        if (area == null || area.isEmpty()) {
            throw ValidationException.forField("geometry", "is required");
        }
        double areaKm2 = envelopeKm2(area);
        if (areaKm2 > 2_000_000 && (types == null || types.isEmpty())) {
            throw new ValidationException(ErrorCodes.INFRA_QUERY_TOO_LARGE,
                    "An area over 2,000,000 km² needs a types filter.", List.of());
        }
        long buildId = currentBuild();
        int minRank = LABEL_RANK.getOrDefault(minLabel == null ? "possible" : minLabel, 1);
        String wkt = area.toText();
        List<Long> direct = jdbc.queryForList("SELECT id FROM infra_node WHERE build_id = ? AND geom IS NOT NULL AND"
                + " ST_Intersects(geom, ST_GeomFromText(?, 4326)) LIMIT " + NODE_LIMIT, Long.class, buildId, wkt);
        List<Edge> edges = jdbc.query("SELECT id, from_node, to_node, relation, method, label, score FROM infra_edge"
                + " WHERE build_id = ?",
                (rs, n) -> new Edge(rs.getLong("id"), rs.getLong("from_node"), rs.getLong("to_node"),
                        rs.getString("relation"), rs.getString("method"), rs.getString("label"), rs.getDouble("score")),
                buildId);
        Map<Long, List<Edge>> out = new LinkedHashMap<>();
        for (Edge edge : edges) {
            out.computeIfAbsent(edge.fromNode(), k -> new ArrayList<>()).add(edge);
            if (isBidirectional(edge.relation())) {
                out.computeIfAbsent(edge.toNode(), k -> new ArrayList<>()).add(edge.reverse());
            }
        }
        Map<Long, Path> reached = new LinkedHashMap<>();
        Deque<State> queue = new ArrayDeque<>();
        for (Long seed : direct) {
            reached.put(seed, new Path(0, 3, 1.0, List.of()));
            queue.add(new State(seed, 0, 3, 1.0, List.of()));
        }
        Set<Long> directSet = new LinkedHashSet<>(direct);
        while (!queue.isEmpty()) {
            State state = queue.poll();
            if (state.depth() >= Math.max(1, Math.min(3, depth))) {
                continue;
            }
            for (Edge edge : out.getOrDefault(state.node(), List.of())) {
                int rank = LABEL_RANK.getOrDefault(edge.label(), 0);
                if (rank < minRank) {
                    continue;
                }
                long next = edge.toNode();
                int weakest = Math.min(state.weakestRank(), rank);
                double score = state.score() * edge.score();
                Path existing = reached.get(next);
                if (existing != null && existing.depth() <= state.depth() + 1) {
                    continue;
                }
                List<Map<String, Object>> path = new ArrayList<>(state.path());
                path.add(Map.of("edgeId", edge.id(), "relation", edge.relation(), "method", edge.method(),
                        "label", edge.label()));
                Path value = new Path(state.depth() + 1, weakest, score, path);
                reached.put(next, value);
                queue.add(new State(next, state.depth() + 1, weakest, score, path));
            }
        }
        boolean truncated = reached.size() > NODE_LIMIT;
        List<Map<String, Object>> dependents = new ArrayList<>();
        List<Map.Entry<Long, Path>> ordered = new ArrayList<>(reached.entrySet());
        ordered.sort((a, b) -> Double.compare(b.getValue().score(), a.getValue().score()));
        for (Map.Entry<Long, Path> entry : ordered.subList(0, Math.min(ordered.size(), NODE_LIMIT))) {
            if (directSet.contains(entry.getKey())) {
                continue;
            }
            Map<String, Object> node = node(entry.getKey());
            if (node == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("node", node);
            row.put("depth", entry.getValue().depth());
            row.put("pathLabel", label(entry.getValue().weakestRank()));
            row.put("pathScore", entry.getValue().score());
            row.put("path", entry.getValue().path());
            dependents.add(row);
        }
        List<Map<String, Object>> directNodes = new ArrayList<>();
        for (Long id : direct) {
            Map<String, Object> node = node(id);
            if (node != null) {
                directNodes.add(node);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("direct", directNodes.size());
        summary.put("dependents", dependents.size());
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(Map.of("code", "OPEN_DATA", "text",
                "Derived from open data; not authoritative; heuristic links may be wrong."));
        caveats.add(Map.of("code", "NOT_GROUND_TRUTH", "text", "Edges carry an uncertainty label."));
        Map<String, Object> out2 = new LinkedHashMap<>();
        out2.put("buildId", buildId);
        out2.put("direct", directNodes);
        out2.put("dependents", dependents);
        out2.put("summary", summary);
        out2.put("caveats", caveats);
        out2.put("attribution", attribution());
        out2.put("truncated", truncated);
        return out2;
    }

    private Map<String, Object> node(long id) {
        List<Map<String, Object>> rows = jdbc.query("SELECT id, facility_type, name, source_id, external_id,"
                        + " attributes::text AS attributes, ST_AsText(point) AS wkt FROM infra_node WHERE id = ?",
                (rs, n) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("facilityType", rs.getString("facility_type"));
                    row.put("name", rs.getString("name"));
                    row.put("sourceId", rs.getString("source_id"));
                    row.put("externalId", rs.getString("external_id"));
                    row.put("point", rs.getString("wkt"));
                    return row;
                }, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<String> attribution() {
        List<String> out = new ArrayList<>();
        out.add("© OpenStreetMap contributors (ODbL 1.0) where OSM data is included");
        return out;
    }

    private static boolean isBidirectional(String relation) {
        return "lands_at".equals(relation) || "interconnects".equals(relation);
    }

    static String label(int rank) {
        for (Map.Entry<String, Integer> entry : LABEL_RANK.entrySet()) {
            if (entry.getValue() == rank) {
                return entry.getKey();
            }
        }
        return "speculative";
    }

    static double envelopeKm2(Geometry area) {
        org.locationtech.jts.geom.Envelope envelope = area.getEnvelopeInternal();
        double midLat = Math.toRadians((envelope.getMinY() + envelope.getMaxY()) / 2);
        return Math.abs((envelope.getMaxX() - envelope.getMinX()) * 111.32 * Math.cos(midLat)
                * (envelope.getMaxY() - envelope.getMinY()) * 110.57);
    }

    private record Edge(long id, long fromNode, long toNode, String relation, String method, String label, double score) {

        Edge reverse() {
            return new Edge(id, toNode, fromNode, relation, method, label, score);
        }
    }

    private record State(long node, int depth, int weakestRank, double score, List<Map<String, Object>> path) {}

    private record Path(int depth, int weakestRank, double score, List<Map<String, Object>> path) {}
}
