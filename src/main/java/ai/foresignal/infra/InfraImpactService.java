package ai.foresignal.infra;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.provenance.AttributionService;
import ai.foresignal.source.spi.Geo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * The affected-dependents query (prompt 104).
 *
 * <p><strong>Seeds</strong> are the current build's nodes whose geometry intersects the area: a
 * geometry, an intelligence event's area (a private event only for a caller with the private
 * grant), or a record's geometry, or its 040 region polygon (admin1, else country) when the record is
 * only a point, in which case the seeds are flagged {@code approximateArea}.
 *
 * <p><strong>Traversal</strong> runs level by level in the database, one indexed query per depth (in
 * chunks of {@value #CHUNK} node ids), over edges at or above {@code minLabel}. Power flows
 * {@code from → to}; {@code lands_at}, {@code interconnects} and {@code transmits_power} are walked
 * both ways, because a cable joins its landings and a line carries power between the substations at
 * its ends, so a line is never a dead end. Each node keeps its shallowest path, preferring the
 * strongest weakest link and then the highest score. Node details are read in one batched query.
 *
 * <p>The result lists {@code direct} nodes and {@code dependents} with the weakest-link label and
 * the product score, a summary by facility type and label, caveats, attribution for every source
 * that contributed (always "© OpenStreetMap contributors" when OSM data is included) and the build
 * id. A {@code types} filter restricts the facilities listed (the traversal still passes through
 * other types). Over {@value #NODE_LIMIT} nodes the dependents are truncated by path score.
 */
@Service
public class InfraImpactService {

    /** The most nodes returned before truncation. */
    public static final int NODE_LIMIT = 5_000;

    /** The largest area queried without a {@code types} filter, km². */
    public static final double MAX_AREA_KM2 = 2_000_000;

    /** The node ids per traversal query. */
    public static final int CHUNK = 1_000;

    /** The relations walked in both directions. */
    public static final Set<String> BIDIRECTIONAL = Set.of("lands_at", "interconnects", "transmits_power");

    /** The OSM attribution every response that includes OSM-derived data carries. */
    public static final String OSM_ATTRIBUTION = "© OpenStreetMap contributors";

    /** The label ranks (kept for callers of the 104 API; see {@link InfraRules#LABELS}). */
    static final Map<String, Integer> LABEL_RANK = Map.of("speculative", 0, "possible", 1, "likely", 2,
            "confirmed", 3);

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final InfraGraphBuilder builder;
    private final AttributionService attributions;
    private final JsonMapper json;

    /** Creates the service. */
    public InfraImpactService(NamedParameterJdbcTemplate jdbc, InfraGraphBuilder builder,
            AttributionService attributions, JsonMapper json) {
        this.jdbc = jdbc;
        this.builder = builder;
        this.attributions = attributions;
        this.json = json;
    }

    /**
     * The area a query starts from.
     *
     * @param area the area
     * @param approximate true when the area is a region polygon standing in for a point
     * @param origin {@code geometry}, {@code event} or {@code record}
     */
    public record Seed(Geometry area, boolean approximate, String origin) {}

    /** The current (newest successful) build, or 409 {@code INFRA_GRAPH_NOT_BUILT}. */
    public InfraBuildEntity currentBuildEntity() {
        return builder.current().orElseThrow(() -> new ConflictException(ErrorCodes.INFRA_GRAPH_NOT_BUILT,
                "The infrastructure graph has not been built yet."));
    }

    /** The current build id, or 409. */
    public long currentBuild() {
        return currentBuildEntity().getId();
    }

    /**
     * The area of an intelligence event the caller may see. The caller resolves the event through the shared
     * visibility rule ({@code IntelEventService.visible}, {@code 404 UNKNOWN_EVENT} for a hidden or missing
     * event), exactly as the intel endpoints do; this method only reads its area.
     *
     * @param event the visible event
     * @throws ValidationException when the event has no area
     */
    public Seed eventSeed(IntelligenceEventEntity event) {
        Geometry area = event.getGeom();
        if (area == null || area.isEmpty()) {
            throw ValidationException.forField("eventId", "the event has no area");
        }
        return new Seed(area, false, "event");
    }

    /**
     * The area of a record: its geometry, or the region polygon from 040 (admin1, else country)
     * when the geometry is only a point.
     *
     * @throws NotFoundException {@code UNKNOWN_RECORD} when there is no such live record
     */
    public Seed recordSeed(String sourceId, String externalId) {
        MapSqlParameterSource params = new MapSqlParameterSource("s", sourceId).addValue("e", externalId);
        List<String> geometry = jdbc.queryForList("SELECT ST_AsText(geom) FROM record_live WHERE source_id = :s"
                + " AND external_id = :e", params, String.class);
        if (geometry.isEmpty()) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_RECORD, "No such record: " + sourceId + "/" + externalId);
        }
        Geometry own = wkt(geometry.getFirst());
        if (own instanceof Polygonal) {
            return new Seed(own, false, "record");
        }
        List<String> region = jdbc.queryForList("SELECT coalesce((SELECT ST_AsText(a.geom) FROM ref_admin1 a WHERE"
                + " a.code = r.admin1), (SELECT ST_AsText(c.geom) FROM ref_country c WHERE c.iso2 = r.iso2))"
                + " FROM record_region r WHERE r.source_id = :s AND r.external_id = :e", params, String.class);
        Geometry polygon = region.isEmpty() ? null : wkt(region.getFirst());
        if (polygon != null) {
            return new Seed(polygon, true, "record");
        }
        if (own == null) {
            throw ValidationException.forField("record", "the record has no geometry and no region");
        }
        return new Seed(own, own instanceof Point, "record");
    }

    /** Computes the affected dependents of an area given as a geometry. */
    public Map<String, Object> affected(Geometry area, int depth, String minLabel, List<String> types) {
        return affected(new Seed(area, false, "geometry"), depth, minLabel, types);
    }

    /** Computes the affected dependents of a seed area. */
    public Map<String, Object> affected(Seed seed, int depth, String minLabel, List<String> types) {
        Geometry area = seed == null ? null : seed.area();
        if (area == null || area.isEmpty()) {
            throw ValidationException.forField("geometry", "is required");
        }
        String minimum = minLabel == null ? "possible" : minLabel;
        if (!InfraRules.LABELS.contains(minimum)) {
            throw ValidationException.forField("minLabel", "must be confirmed, likely, possible or speculative");
        }
        Set<String> typeFilter = types == null ? Set.of() : new LinkedHashSet<>(types);
        if (envelopeKm2(area) > MAX_AREA_KM2 && typeFilter.isEmpty()) {
            throw new ValidationException(ErrorCodes.INFRA_QUERY_TOO_LARGE,
                    "An area over 2,000,000 km² needs a types filter.", List.of());
        }
        InfraBuildEntity build = currentBuildEntity();
        long buildId = build.getId();
        int maxDepth = Math.max(1, Math.min(3, depth));
        List<String> labels = InfraRules.LABELS.subList(InfraRules.rank(minimum), InfraRules.LABELS.size());

        List<Long> seeds = jdbc.queryForList("SELECT id FROM infra_node WHERE build_id = :b AND geom IS NOT NULL"
                        + " AND ST_Intersects(geom, ST_GeomFromText(:wkt, 4326)) ORDER BY id LIMIT " + (NODE_LIMIT + 1),
                new MapSqlParameterSource("b", buildId).addValue("wkt", area.toText()), Long.class);
        boolean truncated = seeds.size() > NODE_LIMIT;
        if (truncated) {
            seeds = seeds.subList(0, NODE_LIMIT);
        }
        Map<Long, Path> reached = traverse(buildId, seeds, maxDepth, labels);
        if (reached.size() > NODE_LIMIT) {
            truncated = true;
        }

        Set<Long> ids = new LinkedHashSet<>(seeds);
        ids.addAll(reached.keySet());
        Map<Long, Map<String, Object>> nodes = nodes(ids);

        List<Map<String, Object>> direct = new ArrayList<>();
        for (Long id : seeds) {
            Map<String, Object> node = nodes.get(id);
            if (node != null && (typeFilter.isEmpty() || typeFilter.contains(node.get("facilityType")))) {
                direct.add(node);
            }
        }
        List<Map.Entry<Long, Path>> ordered = new ArrayList<>(reached.entrySet());
        ordered.sort(Comparator.<Map.Entry<Long, Path>>comparingDouble(e -> -e.getValue().score())
                .thenComparingInt(e -> e.getValue().depth()).thenComparingLong(Map.Entry::getKey));
        List<Map<String, Object>> dependents = new ArrayList<>();
        for (Map.Entry<Long, Path> entry : ordered) {
            Map<String, Object> node = nodes.get(entry.getKey());
            if (node == null || (!typeFilter.isEmpty() && !typeFilter.contains(node.get("facilityType")))) {
                continue;
            }
            if (dependents.size() >= NODE_LIMIT) {
                truncated = true;
                break;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("node", node);
            row.put("depth", entry.getValue().depth());
            row.put("pathLabel", InfraRules.label(entry.getValue().weakestRank()));
            row.put("pathScore", entry.getValue().score());
            row.put("path", entry.getValue().path());
            dependents.add(row);
        }

        Set<String> sources = new LinkedHashSet<>();
        direct.forEach(n -> sources.add(String.valueOf(n.get("sourceId"))));
        dependents.forEach(d -> sources.add(String.valueOf(((Map<?, ?>) d.get("node")).get("sourceId"))));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("buildId", buildId);
        out.put("approximateArea", seed.approximate());
        out.put("direct", direct);
        out.put("dependents", dependents);
        out.put("summary", summary(direct, dependents));
        out.put("caveats", caveats(seed, build, sources, truncated));
        out.put("attribution", attribution(sources));
        out.put("truncated", truncated);
        return out;
    }

    /** The shallowest, strongest path to every node reachable from {@code seeds} (seeds excluded). */
    private Map<Long, Path> traverse(long buildId, List<Long> seeds, int maxDepth, List<String> labels) {
        Map<Long, Path> reached = new LinkedHashMap<>();
        Set<Long> visited = new LinkedHashSet<>(seeds);
        Map<Long, Path> frontier = new LinkedHashMap<>();
        for (Long seed : seeds) {
            frontier.put(seed, new Path(0, InfraRules.rank("confirmed"), 1.0, List.of()));
        }
        for (int depth = 1; depth <= maxDepth && !frontier.isEmpty() && reached.size() <= NODE_LIMIT; depth++) {
            Map<Long, Path> next = new TreeMap<>();
            List<Long> ids = new ArrayList<>(frontier.keySet());
            for (int i = 0; i < ids.size(); i += CHUNK) {
                List<Long> chunk = ids.subList(i, Math.min(ids.size(), i + CHUNK));
                for (Edge edge : edges(buildId, chunk, labels)) {
                    step(edge, edge.fromNode(), edge.toNode(), frontier, visited, next, depth);
                    if (BIDIRECTIONAL.contains(edge.relation())) {
                        step(edge, edge.toNode(), edge.fromNode(), frontier, visited, next, depth);
                    }
                }
            }
            visited.addAll(next.keySet());
            reached.putAll(next);
            frontier = next;
        }
        return reached;
    }

    private static void step(Edge edge, long from, long to, Map<Long, Path> frontier, Set<Long> visited,
            Map<Long, Path> next, int depth) {
        Path state = frontier.get(from);
        if (state == null || visited.contains(to)) {
            return;
        }
        List<Map<String, Object>> path = new ArrayList<>(state.path());
        Map<String, Object> hop = new LinkedHashMap<>();
        hop.put("edgeId", edge.id());
        hop.put("relation", edge.relation());
        hop.put("method", edge.method());
        hop.put("label", edge.label());
        path.add(hop);
        Path candidate = new Path(depth, Math.min(state.weakestRank(), InfraRules.rank(edge.label())),
                state.score() * edge.score(), path);
        Path existing = next.get(to);
        if (existing == null || candidate.better(existing)) {
            next.put(to, candidate);
        }
    }

    private List<Edge> edges(long buildId, List<Long> ids, List<String> labels) {
        MapSqlParameterSource params = new MapSqlParameterSource("b", buildId).addValue("ids", ids)
                .addValue("labels", labels).addValue("bidirectional", List.copyOf(BIDIRECTIONAL));
        return jdbc.query("SELECT id, from_node, to_node, relation, method, label, score FROM infra_edge"
                        + " WHERE build_id = :b AND label IN (:labels) AND (from_node IN (:ids)"
                        + " OR (to_node IN (:ids) AND relation IN (:bidirectional))) ORDER BY id",
                params, (rs, n) -> new Edge(rs.getLong("id"), rs.getLong("from_node"), rs.getLong("to_node"),
                        rs.getString("relation"), rs.getString("method"), rs.getString("label"), rs.getDouble("score")));
    }

    /** The API shape of {@code ids}, read in batched queries. */
    private Map<Long, Map<String, Object>> nodes(Set<Long> ids) {
        Map<Long, Map<String, Object>> out = new HashMap<>();
        List<Long> all = new ArrayList<>(ids);
        for (int i = 0; i < all.size(); i += CHUNK) {
            List<Long> chunk = all.subList(i, Math.min(all.size(), i + CHUNK));
            jdbc.query("SELECT id, facility_type, name, source_id, external_id, licence, attributes::text AS attributes,"
                    + " ST_AsText(point) AS wkt FROM infra_node WHERE id IN (:ids)",
                    new MapSqlParameterSource("ids", chunk), rs -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", rs.getLong("id"));
                        row.put("facilityType", rs.getString("facility_type"));
                        row.put("name", rs.getString("name"));
                        row.put("sourceId", rs.getString("source_id"));
                        row.put("externalId", rs.getString("external_id"));
                        row.put("licence", rs.getString("licence"));
                        String attributes = rs.getString("attributes");
                        row.put("attributes", attributes == null ? Map.of() : json.readValue(attributes, MAP));
                        row.put("point", rs.getString("wkt"));
                        out.put(rs.getLong("id"), row);
                    });
        }
        return out;
    }

    private static Map<String, Object> summary(List<Map<String, Object>> direct, List<Map<String, Object>> dependents) {
        Map<String, Map<String, Integer>> byType = new TreeMap<>();
        Map<String, Integer> byLabel = new TreeMap<>();
        for (Map<String, Object> node : direct) {
            byType.computeIfAbsent(String.valueOf(node.get("facilityType")), k -> new TreeMap<>())
                    .merge("direct", 1, Integer::sum);
        }
        for (Map<String, Object> dependent : dependents) {
            String label = String.valueOf(dependent.get("pathLabel"));
            Map<?, ?> node = (Map<?, ?>) dependent.get("node");
            byType.computeIfAbsent(String.valueOf(node.get("facilityType")), k -> new TreeMap<>())
                    .merge(label, 1, Integer::sum);
            byLabel.merge(label, 1, Integer::sum);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("direct", direct.size());
        out.put("dependents", dependents.size());
        out.put("byType", byType);
        out.put("byLabel", byLabel);
        return out;
    }

    private static List<Map<String, Object>> caveats(Seed seed, InfraBuildEntity build, Set<String> sources,
            boolean truncated) {
        List<Map<String, Object>> caveats = new ArrayList<>();
        caveats.add(Map.of("code", "OPEN_DATA", "text",
                "Derived from open data; not authoritative; heuristic links may be wrong."));
        caveats.add(Map.of("code", "NOT_GROUND_TRUTH", "text", "Edges carry an uncertainty label."));
        if (seed.approximate()) {
            caveats.add(Map.of("code", "REGION_LEVEL_AREA", "text",
                    "The area is a region polygon standing in for a point; facilities in the region but far from"
                            + " the event are included."));
        }
        if (sources.contains(InfraGraphBuilder.OSM_SOURCE)) {
            caveats.add(Map.of("code", "OSM_EXTRACT_DATE", "text", build.getOsmExtractDate() == null
                    ? "OSM extract date unknown."
                    : "OSM extract dated " + build.getOsmExtractDate() + "."));
        }
        List<String> disabled = new ArrayList<>();
        for (String warning : build.getWarnings() == null ? List.<String>of() : build.getWarnings()) {
            if (warning.endsWith(" is disabled")) {
                disabled.add(warning.substring("rule ".length(), warning.length() - " is disabled".length()));
            }
        }
        if (!disabled.isEmpty()) {
            caveats.add(Map.of("code", "RULES_DISABLED", "text", "Rules disabled in this build: "
                    + String.join(", ", disabled) + "; their links are missing."));
        }
        if (truncated) {
            caveats.add(Map.of("code", "TRUNCATED", "text",
                    "More than " + NODE_LIMIT + " facilities; the list was cut by path score."));
        }
        return caveats;
    }

    /** The attribution lines of the contributing sources ("© OpenStreetMap contributors" for OSM). */
    List<String> attribution(Set<String> sources) {
        Set<String> out = new LinkedHashSet<>();
        for (String source : sources) {
            if (InfraGraphBuilder.OSM_SOURCE.equals(source)) {
                out.add(OSM_ATTRIBUTION);
                continue;
            }
            for (AttributionService.Attribution attribution : attributions.scope(source, null)) {
                if (attribution.text() != null && !attribution.text().isBlank()) {
                    out.add(attribution.text());
                }
            }
        }
        return new ArrayList<>(out);
    }

    /** The label of a rank. */
    static String label(int rank) {
        return InfraRules.label(rank);
    }

    /** The area of the envelope of {@code area}, km² (an upper bound for the size check). */
    static double envelopeKm2(Geometry area) {
        org.locationtech.jts.geom.Envelope envelope = area.getEnvelopeInternal();
        double midLat = Math.toRadians((envelope.getMinY() + envelope.getMaxY()) / 2);
        return Math.abs((envelope.getMaxX() - envelope.getMinX()) * 111.32 * Math.cos(midLat)
                * (envelope.getMaxY() - envelope.getMinY()) * 110.57);
    }

    private static Geometry wkt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return new org.locationtech.jts.io.WKTReader(Geo.factory()).read(text);
        } catch (Exception e) {
            return null;
        }
    }

    private record Edge(long id, long fromNode, long toNode, String relation, String method, String label,
            double score) {}

    /** A path to a node: its depth, weakest label rank, product score and hops. */
    private record Path(int depth, int weakestRank, double score, List<Map<String, Object>> path) {

        /** True when this path beats {@code other} at the same depth: stronger weakest link, then higher score. */
        boolean better(Path other) {
            if (weakestRank != other.weakestRank) {
                return weakestRank > other.weakestRank;
            }
            return score > other.score;
        }
    }
}
