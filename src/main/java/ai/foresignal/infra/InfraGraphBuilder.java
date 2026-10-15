package ai.foresignal.infra;

import ai.foresignal.source.spi.Geo;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the infrastructure graph (prompt 104): nodes are views of stored records (094 datasets and
 * the OSM extract) mapped by {@code infra/node-mapping.json}; edges come from the versioned rules in
 * {@code infra/dependency-rules.json}. Each build is written under a new {@code build_id} and becomes
 * current when it finishes, so readers never see a half graph. Rebuilds are deterministic over the
 * same inputs.
 */
@Service
public class InfraGraphBuilder {

    private static final Logger log = LoggerFactory.getLogger(InfraGraphBuilder.class);

    private static final String NODE_MAPPING = "/infra/node-mapping.json";
    private static final String RULES = "/infra/dependency-rules.json";
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final InfraNodeRepository nodes;
    private final InfraEdgeRepository edges;
    private final InfraBuildRepository builds;
    private final Clock clock;

    /** Creates the builder. */
    public InfraGraphBuilder(JdbcTemplate jdbc, JsonMapper json, InfraNodeRepository nodes,
            InfraEdgeRepository edges, InfraBuildRepository builds, Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.nodes = nodes;
        this.edges = edges;
        this.builds = builds;
        this.clock = clock;
    }

    /** One node under construction. */
    record NodeData(long id, String sourceId, String externalId, String facilityType, String name, Geometry geom,
            Point point, Map<String, Object> attributes, String licence, String region) {}

    /** Rebuilds the graph into a new build id. */
    @Transactional
    public InfraBuildEntity rebuild(Map<String, Object> overrides) {
        Instant started = clock.instant();
        Map<String, Object> mapping = read(NODE_MAPPING);
        Map<String, Object> rules = read(RULES);
        int rulesVersion = rules.get("version") instanceof Number n ? n.intValue() : 1;
        InfraBuildEntity build = builds.save(new InfraBuildEntity(started, rulesVersion));
        long buildId = build.getId();
        List<String> warnings = new ArrayList<>();

        List<NodeData> nodeData = new ArrayList<>();
        long[] counter = {0};
        Map<String, Object> sources = asMap(mapping.get("sources"));
        for (Map.Entry<String, Object> source : sources.entrySet()) {
            Map<String, Object> kinds = asMap(source.getValue());
            for (Map.Entry<String, Object> kind : kinds.entrySet()) {
                load(source.getKey(), kind.getKey(), String.valueOf(kind.getValue()), buildId, nodeData, counter);
            }
        }
        Map<String, Object> edgesByMethod = new LinkedHashMap<>();
        Map<String, Object> edgesByLabel = new LinkedHashMap<>();
        List<InfraEdgeEntity> built = rules(rules, nodeData, buildId, warnings, edgesByMethod, edgesByLabel);
        edges.saveAll(built);
        build.setNodes(nodeData.size());
        build.setEdgesByMethod(edgesByMethod);
        build.setEdgesByLabel(edgesByLabel);
        build.setWarnings(warnings);
        build.setInputs(Map.of("sources", sources.keySet()));
        build.setFinished(clock.instant());
        return builds.save(build);
    }

    private void load(String sourceId, String kind, String facilityType, long buildId, List<NodeData> out,
            long[] counter) {
        String licence = "osm-infrastructure".equals(sourceId) ? "ODbL-1.0" : licenceOf(sourceId);
        jdbc.query("SELECT h.external_id, h.payload::text AS payload, ST_AsText(l.geom) AS wkt"
                        + " FROM record_head h JOIN record_live l USING (source_id, external_id)"
                        + " WHERE h.source_id = ? AND h.kind = ? AND NOT h.tombstone LIMIT 200000",
                (rs, n) -> {
                    Map<String, Object> payload = rs.getString("payload") == null ? Map.of()
                            : json.readValue(rs.getString("payload"), MAP);
                    Geometry geom = wkt(rs.getString("wkt"));
                    Point point = representative(geom);
                    String name = nameOf(payload);
                    String region = payload.get("admin1") == null ? null : String.valueOf(payload.get("admin1"));
                    InfraNodeEntity node = new InfraNodeEntity(buildId, sourceId, rs.getString("external_id"),
                            facilityType, clock.instant());
                    node.setName(name);
                    node.setGeom(geom);
                    node.setPoint(point);
                    node.setAttributes(payload);
                    node.setLicence(licence);
                    if (region != null) {
                        node.setRegionTags(List.of(region));
                    }
                    InfraNodeEntity saved = nodes.save(node);
                    out.add(new NodeData(saved.getId(), sourceId, saved.getExternalId(), facilityType, name, geom,
                            point, payload, licence, region));
                    return null;
                }, sourceId, kind);
    }

    private static String nameOf(Map<String, Object> payload) {
        for (String key : List.of("name", "portName", "title", "icao", "iata", "ref")) {
            if (payload.get(key) != null) {
                return String.valueOf(payload.get(key));
            }
        }
        return null;
    }

    private List<InfraEdgeEntity> rules(Map<String, Object> rules, List<NodeData> nodeData, long buildId,
            List<String> warnings, Map<String, Object> byMethod, Map<String, Object> byLabel) {
        List<InfraEdgeEntity> out = new ArrayList<>();
        for (Object raw : asList(rules.get("rules"))) {
            Map<String, Object> rule = asMap(raw);
            if (!Boolean.TRUE.equals(rule.get("enabled"))) {
                warnings.add("rule " + rule.get("id") + " is disabled");
                continue;
            }
            String id = String.valueOf(rule.get("id"));
            switch (id) {
                case "cable-landing" -> cableLanding(rule, nodeData, buildId, out);
                case "line-substation" -> lineSubstation(rule, nodeData, buildId, out);
                case "plant-substation" -> plantSubstation(rule, nodeData, buildId, out);
                case "substation-critical-load" -> substationLoad(rule, nodeData, buildId, out);
                case "airport-port-serves" -> warnings.add("rule airport-port-serves needs region nodes; skipped");
                default -> warnings.add("rule " + id + " is not implemented");
            }
        }
        for (InfraEdgeEntity edge : out) {
            byMethod.merge(edge.getMethod(), 1, (a, b) -> ((Number) a).intValue() + 1);
            byLabel.merge(edge.getLabel(), 1, (a, b) -> ((Number) a).intValue() + 1);
        }
        return out;
    }

    private void cableLanding(Map<String, Object> rule, List<NodeData> nodeData, long buildId, List<InfraEdgeEntity> out) {
        List<NodeData> cables = of(nodeData, "telecom.cable");
        List<NodeData> landings = of(nodeData, "telecom.landing");
        for (NodeData cable : cables) {
            for (NodeData landing : landings) {
                if (cable.geom() == null || landing.point() == null) {
                    continue;
                }
                double distance = cable.geom().distance(landing.point()) * 111.0;
                if (distance <= 0.5) {
                    out.add(edge(buildId, cable, landing, "lands_at", "declared", "confirmed", 1.0, "cable-landing", 1,
                            Map.of("distanceKm", distance), "ODbL-1.0"));
                }
            }
        }
    }

    private void lineSubstation(Map<String, Object> rule, List<NodeData> nodeData, long buildId,
            List<InfraEdgeEntity> out) {
        List<NodeData> lines = of(nodeData, "power.line");
        List<NodeData> substations = of(nodeData, "power.substation");
        for (NodeData line : lines) {
            if (!(line.geom() instanceof LineString ls)) {
                continue;
            }
            int hits = 0;
            for (Coordinate endpoint : new Coordinate[] {ls.getStartPoint().getCoordinate(),
                    ls.getEndPoint().getCoordinate()}) {
                NodeData nearest = nearest(substations, endpoint, 0.05);
                if (nearest != null) {
                    hits++;
                    out.add(edge(buildId, nearest, line, "transmits_power", "topological", "possible", 0.5,
                            "line-substation", 1, Map.of("endpoint", endpoint.toString()), "ODbL-1.0"));
                }
            }
            if (hits == 2 && !out.isEmpty()) {
                // confirmed when both ends hit substations: upgrade the last two edges
                for (int i = out.size() - 2; i < out.size(); i++) {
                    out.get(i).setScore(1.0);
                }
            }
        }
    }

    private void plantSubstation(Map<String, Object> rule, List<NodeData> nodeData, long buildId,
            List<InfraEdgeEntity> out) {
        double capacityFactor = number(asMap(rule.get("params")).get("capacityFactorKm"), 2);
        double maxKm = number(asMap(rule.get("params")).get("maxKm"), 5);
        List<NodeData> substations = of(nodeData, "power.substation");
        for (NodeData plant : of(nodeData, "power.generation")) {
            if (plant.point() == null) {
                continue;
            }
            double capacity = number(plant.attributes().get("capacityMw"), 0);
            double radius = Math.min(maxKm, capacity > 0 ? capacityFactor * Math.sqrt(capacity) : maxKm);
            NodeData closest = nearest(substations, plant.point().getCoordinate(), radius);
            if (closest != null) {
                double distance = nearest(plant, closest);
                out.add(edge(buildId, plant, closest, "supplies_power", "proximity", "possible",
                        Math.max(0, 1 - distance / radius), "plant-substation", 1,
                        Map.of("distanceKm", distance), licenceOf(plant)));
            }
        }
    }

    private void substationLoad(Map<String, Object> rule, List<NodeData> nodeData, long buildId,
            List<InfraEdgeEntity> out) {
        double urbanKm = number(asMap(rule.get("params")).get("urbanKm"), 3);
        double ruralKm = number(asMap(rule.get("params")).get("ruralKm"), 10);
        List<NodeData> substations = of(nodeData, "power.substation");
        for (String type : List.of("health.hospital", "telecom.datacentre", "transport.airport", "water.treatment",
                "transport.port")) {
            for (NodeData load : of(nodeData, type)) {
                if (load.point() == null) {
                    continue;
                }
                double radius = ruralKm;
                Nearest two = twoNearest(substations, load.point().getCoordinate(), radius);
                if (two.nearest() != null) {
                    double distance = nearest(load, two.nearest());
                    String label = two.second() != null ? "speculative" : "possible";
                    out.add(edge(buildId, two.nearest(), load, "supplies_power", "proximity", label,
                            Math.max(0, 1 - distance / radius), "substation-critical-load", 1,
                            Map.of("distanceKm", distance), licenceOf(load)));
                }
                if (two.second() != null) {
                    double distance = nearest(load, two.second());
                    out.add(edge(buildId, two.second(), load, "supplies_power", "proximity", "speculative",
                            Math.max(0, 1 - distance / radius), "substation-critical-load", 1,
                            Map.of("distanceKm", distance), licenceOf(load)));
                }
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static InfraEdgeEntity edge(long buildId, NodeData from, NodeData to, String relation, String method,
            String label, double score, String ruleId, int version, Map<String, Object> evidence, String licence) {
        InfraEdgeEntity edge = new InfraEdgeEntity(buildId, from.id(), to.id(), relation, method, label, ruleId, version,
                java.time.Instant.now());
        edge.setScore(Math.max(0, Math.min(1, score)));
        edge.setEvidence(new LinkedHashMap<>(evidence));
        edge.setParams(Map.of());
        edge.setLicence(licence);
        return edge;
    }

    private static List<NodeData> of(List<NodeData> nodes, String type) {
        return nodes.stream().filter(n -> n.facilityType().equals(type)).toList();
    }

    private static NodeData nearest(List<NodeData> candidates, Coordinate point, double km) {
        return nearestWithin(candidates, point, km).nearest();
    }

    private record Nearest(NodeData nearest, NodeData second) {}

    private static Nearest nearestWithin(List<NodeData> candidates, Coordinate point, double km) {
        NodeData first = null;
        NodeData second = null;
        double firstDistance = Double.MAX_VALUE;
        double secondDistance = Double.MAX_VALUE;
        for (NodeData candidate : candidates) {
            if (candidate.point() == null) {
                continue;
            }
            double distance = Geo.haversineKm(point.y, point.x, candidate.point().getY(), candidate.point().getX());
            if (distance <= km && distance < firstDistance) {
                second = first;
                secondDistance = firstDistance;
                first = candidate;
                firstDistance = distance;
            } else if (distance <= km && distance < secondDistance) {
                second = candidate;
                secondDistance = distance;
            }
        }
        return new Nearest(first, second);
    }

    private static Nearest twoNearest(List<NodeData> candidates, Coordinate point, double km) {
        return nearestWithin(candidates, point, km);
    }

    private static double nearest(NodeData a, NodeData b) {
        if (a.point() == null || b.point() == null) {
            return Double.MAX_VALUE;
        }
        return Geo.haversineKm(a.point().getY(), a.point().getX(), b.point().getY(), b.point().getX());
    }

    private static Geometry wkt(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            Geometry geometry = new org.locationtech.jts.io.WKTReader(Geo.factory()).read(text);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }

    private static Point representative(Geometry geom) {
        if (geom == null || geom.isEmpty()) {
            return null;
        }
        return geom.getCentroid();
    }

    private static String licenceOf(String sourceId) {
        return switch (sourceId) {
            case "wri-power-plants", "world-port-index" -> "CC BY 4.0";
            case "ourairports" -> "public domain";
            case "submarine-cables" -> "CC BY-SA 4.0 (TeleGeography)";
            default -> "open";
        };
    }

    private static String licenceOf(NodeData node) {
        return node.licence();
    }

    private Map<String, Object> read(String resource) {
        try (InputStream in = InfraGraphBuilder.class.getResourceAsStream(resource)) {
            return in == null ? Map.of() : json.readValue(in, MAP);
        } catch (Exception e) {
            log.warn("Could not read {}: {}", resource, e.toString());
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object raw) {
        return raw instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    static List<Object> asList(Object raw) {
        return raw instanceof List<?> list ? new ArrayList<>(list) : List.of();
    }

    private static double number(Object raw, double fallback) {
        return raw instanceof Number n ? n.doubleValue() : fallback;
    }
}
