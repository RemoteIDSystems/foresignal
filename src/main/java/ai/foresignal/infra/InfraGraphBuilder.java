package ai.foresignal.infra;

import ai.foresignal.provenance.LicenceCatalog;
import ai.foresignal.provenance.LicenceTerms;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the infrastructure graph (prompt 104).
 *
 * <p><strong>Nodes</strong> are views of stored records (094 datasets and the OSM extract) mapped by
 * {@code infra/node-mapping.json}, read in a fixed order (mapping order, then {@code external_id}).
 * <strong>Edges</strong> come from the versioned rules in {@code infra/dependency-rules.json} with
 * the operator overrides of {@code app_setting['infra.rules']} ({@link InfraRules}). Every edge
 * records its rule id, the rules version, the effective parameters, the evidence and its license:
 * the most restrictive license of its two nodes ({@link InfraLicences}).
 *
 * <p><strong>Build lifecycle.</strong> {@link #start} records a {@code running} build;
 * {@link #execute} writes that build's nodes and edges in one transaction, records the counts,
 * the inputs ({@code source → last seq}) and the OSM extract date, and deletes the nodes and edges
 * of every superseded build in the same transaction, so readers (which read only the newest
 * successful build) see the old graph or the new one, never half of one, and rebuilds do not
 * accumulate copies. A failed build records its error and leaves the previous graph current.
 *
 * <p><strong>Reproducible.</strong> Over the same records, rules and overrides a rebuild produces the
 * same nodes and the same edges with the same parameters and evidence.
 */
@Service
public class InfraGraphBuilder {

    private static final Logger log = LoggerFactory.getLogger(InfraGraphBuilder.class);

    /** The node-mapping resource. */
    public static final String NODE_MAPPING = "/infra/node-mapping.json";

    /** The dependency-rules resource. */
    public static final String RULES = "/infra/dependency-rules.json";

    /** The settings key of the rule overrides. */
    public static final String OVERRIDES_KEY = "infra.rules";

    /** The most records read per mapped source and kind; a warning is recorded when it is reached. */
    public static final int MAX_ROWS_PER_KIND = 1_000_000;

    /** A build still running after this long is treated as abandoned (a restart interrupted it). */
    public static final Duration STALE_BUILD = Duration.ofHours(6);

    /** The adapter-state key under which the OSM adapter records its extract's modification time. */
    public static final String OSM_MTIME_STATE = "osm.mtime";

    /** The OSM source id. */
    public static final String OSM_SOURCE = "osm-infrastructure";

    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    private static final String INSERT_NODES = """
            INSERT INTO infra_node (build_id, source_id, external_id, facility_type, name, geom, point, attributes,
                                    region_tags, licence, built_at)
            SELECT ?, h.source_id, h.external_id, ?,
                   coalesce(h.payload ->> 'name', h.payload ->> 'portName', h.payload ->> 'title',
                            h.payload ->> 'icao', h.payload ->> 'iata', h.payload ->> 'ref'),
                   l.geom, CASE WHEN l.geom IS NULL OR ST_IsEmpty(l.geom) THEN NULL ELSE ST_PointOnSurface(l.geom) END,
                   h.payload,
                   coalesce((SELECT jsonb_agg(t) FROM unnest(ARRAY[r.iso2::text, r.admin1::text]) AS t
                             WHERE t IS NOT NULL), '[]'::jsonb),
                   ?, ?
              FROM record_head h
              JOIN record_live l USING (source_id, external_id)
              LEFT JOIN record_region r USING (source_id, external_id)
             WHERE h.source_id = ? AND h.kind = ? AND NOT h.tombstone
             ORDER BY h.external_id
             LIMIT ?
            RETURNING id, external_id, name, ST_AsText(geom) AS wkt, ST_AsText(point) AS point_wkt,
                      attributes::text AS attributes
            """;

    private static final String INSERT_EDGE = "INSERT INTO infra_edge (build_id, from_node, to_node, relation, method,"
            + " label, score, rule_id, rule_version, params, evidence, licence, built_at)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?, ?)";

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final InfraBuildRepository builds;
    private final SourceRegistry registry;
    private final LicenceCatalog licences;
    private final AppSettingService settings;
    private final TransactionTemplate transactions;
    private final Clock clock;

    /** Creates the builder. */
    public InfraGraphBuilder(JdbcTemplate jdbc, JsonMapper json, InfraBuildRepository builds, SourceRegistry registry,
            LicenceCatalog licences, AppSettingService settings, PlatformTransactionManager transactionManager,
            Clock clock) {
        this.jdbc = jdbc;
        this.json = json;
        this.builds = builds;
        this.registry = registry;
        this.licences = licences;
        this.settings = settings;
        this.transactions = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    /** One node under construction (its index is its position in the fixed build order). */
    record NodeData(int index, long id, String sourceId, String externalId, String facilityType, String name,
            Geometry geom, Point point, Map<String, Object> attributes, LicenceTerms licence) {}

    /** One edge under construction. */
    record EdgeData(NodeData from, NodeData to, String relation, String method, String label, double score,
            String ruleId, Map<String, Object> params, Map<String, Object> evidence) {

        String key() {
            return from.id() + "|" + to.id() + "|" + relation + "|" + ruleId;
        }
    }

    /**
     * A started build.
     *
     * @param build the build row
     * @param created true when this call created it, false when a build was already running
     */
    public record Started(InfraBuildEntity build, boolean created) {}

    /** The newest successful build: the current graph. */
    public Optional<InfraBuildEntity> current() {
        return builds.findTop1ByFinishedIsNotNullAndErrorIsNullOrderByIdDesc();
    }

    /** The build running now, if any (an abandoned build older than {@link #STALE_BUILD} does not count). */
    public Optional<InfraBuildEntity> running() {
        Instant cutoff = clock.instant().minus(STALE_BUILD);
        return builds.findTop1ByFinishedIsNullAndErrorIsNullOrderByIdDesc()
                .filter(build -> build.getStarted().isAfter(cutoff));
    }

    /** Records a running build, or returns the one already running. */
    @Transactional
    public synchronized Started start() {
        Optional<InfraBuildEntity> running = running();
        if (running.isPresent()) {
            return new Started(running.get(), false);
        }
        Map<String, Object> rules = read(RULES);
        int rulesVersion = rules.get("version") instanceof Number n ? n.intValue() : 1;
        return new Started(builds.save(new InfraBuildEntity(clock.instant(), rulesVersion)), true);
    }

    /** Builds synchronously with the stored overrides (scheduled rebuilds and tests). */
    public InfraBuildEntity rebuild() {
        return rebuild(null);
    }

    /**
     * Builds synchronously.
     *
     * @param overrides rule overrides to use instead of the stored ones; null or empty uses the stored ones
     */
    public InfraBuildEntity rebuild(Map<String, Object> overrides) {
        Started started = start();
        if (!started.created()) {
            return started.build();
        }
        return execute(started.build().getId(), overrides);
    }

    /** Runs a started build with the stored overrides. */
    public InfraBuildEntity execute(long buildId) {
        return execute(buildId, null);
    }

    /**
     * Runs a started build: nodes, edges, counts and the removal of superseded builds, in one
     * transaction. A failure is recorded on the build and the previous graph stays current.
     */
    @SuppressWarnings("unchecked")
    public InfraBuildEntity execute(long buildId, Map<String, Object> overrides) {
        try {
            Map<String, Object> effectiveOverrides = overrides != null && !overrides.isEmpty() ? overrides
                    : (Map<String, Object>) settings.get(OVERRIDES_KEY, Map.class).orElse(Map.of());
            return transactions.execute(status -> build(buildId, effectiveOverrides));
        } catch (RuntimeException e) {
            log.warn("infrastructure build {} failed: {}", buildId, e.toString());
            transactions.executeWithoutResult(status -> builds.findById(buildId).ifPresent(build -> {
                build.setError(String.valueOf(e.getMessage() == null ? e : e.getMessage()));
                build.setFinished(clock.instant());
                builds.save(build);
            }));
            return builds.findById(buildId).orElseThrow(() -> e);
        }
    }

    private InfraBuildEntity build(long buildId, Map<String, Object> overrides) {
        InfraBuildEntity build = builds.findById(buildId).orElseThrow(
                () -> new IllegalStateException("No such infrastructure build: " + buildId));
        Instant builtAt = build.getStarted();
        Map<String, Object> mapping = read(NODE_MAPPING);
        Map<String, Object> bundled = read(RULES);
        int rulesVersion = bundled.get("version") instanceof Number n ? n.intValue() : 1;
        List<InfraRules.Rule> rules = InfraRules.effective(bundled, overrides);
        List<String> warnings = new ArrayList<>();

        List<NodeData> nodeData = new ArrayList<>();
        Map<String, Object> sources = asMap(mapping.get("sources"));
        for (Map.Entry<String, Object> source : sources.entrySet()) {
            LicenceTerms licence = licenceOf(source.getKey());
            for (Map.Entry<String, Object> kind : asMap(source.getValue()).entrySet()) {
                int loaded = load(buildId, source.getKey(), kind.getKey(), String.valueOf(kind.getValue()), licence,
                        builtAt, nodeData);
                if (loaded >= MAX_ROWS_PER_KIND) {
                    warnings.add("node cap reached for " + source.getKey() + "/" + kind.getKey() + " ("
                            + MAX_ROWS_PER_KIND + " records); the rest were not mapped");
                }
            }
        }

        List<EdgeData> edgeData = edges(rules, nodeData, warnings);
        Map<String, Object> byMethod = new TreeMap<>();
        Map<String, Object> byLabel = new TreeMap<>();
        Timestamp at = Timestamp.from(builtAt);
        List<Object[]> batch = new ArrayList<>();
        for (EdgeData edge : edgeData) {
            byMethod.merge(edge.method(), 1, (a, b) -> ((Number) a).intValue() + 1);
            byLabel.merge(edge.label(), 1, (a, b) -> ((Number) a).intValue() + 1);
            batch.add(new Object[] {buildId, edge.from().id(), edge.to().id(), edge.relation(), edge.method(),
                    edge.label(), edge.score(), edge.ruleId(), rulesVersion, json.writeValueAsString(edge.params()),
                    json.writeValueAsString(edge.evidence()),
                    InfraLicences.mostRestrictive(List.of(edge.from().licence(), edge.to().licence())), at});
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(INSERT_EDGE, batch);
        }

        Map<String, Object> inputs = new LinkedHashMap<>();
        for (String source : sources.keySet()) {
            Long seq = jdbc.queryForObject("SELECT max(seq) FROM record_head WHERE source_id = ?", Long.class, source);
            if (seq != null) {
                inputs.put(source, seq);
            }
        }
        build.setNodes(nodeData.size());
        build.setEdgesByMethod(byMethod);
        build.setEdgesByLabel(byLabel);
        build.setWarnings(warnings);
        build.setInputs(inputs);
        build.setOsmExtractDate(inputs.containsKey(OSM_SOURCE) ? osmExtractDate() : null);
        build.setFinished(clock.instant());
        InfraBuildEntity saved = builds.save(build);
        // The superseded graphs go in the same transaction that makes this one current.
        jdbc.update("DELETE FROM infra_edge WHERE build_id <> ?", buildId);
        jdbc.update("DELETE FROM infra_node WHERE build_id <> ?", buildId);
        return saved;
    }

    /** Inserts the nodes of one mapped source and kind and returns how many were read. */
    private int load(long buildId, String sourceId, String kind, String facilityType, LicenceTerms licence,
            Instant builtAt, List<NodeData> out) {
        List<NodeData> rows = jdbc.query(INSERT_NODES, (rs, n) -> {
            String attributes = rs.getString("attributes");
            return new NodeData(0, rs.getLong("id"), sourceId, rs.getString("external_id"), facilityType,
                    rs.getString("name"), wkt(rs.getString("wkt")), (Point) wkt(rs.getString("point_wkt")),
                    attributes == null ? Map.of() : json.readValue(attributes, MAP), licence);
        }, buildId, facilityType, licence.id(), Timestamp.from(builtAt), sourceId, kind, MAX_ROWS_PER_KIND);
        List<NodeData> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparing(NodeData::externalId));
        for (NodeData row : sorted) {
            out.add(new NodeData(out.size(), row.id(), row.sourceId(), row.externalId(), row.facilityType(), row.name(),
                    row.geom(), row.point(), row.attributes(), row.licence()));
        }
        return rows.size();
    }

    private LicenceTerms licenceOf(String sourceId) {
        if (registry.contains(sourceId)) {
            return licences.forDescriptor(registry.descriptor(sourceId));
        }
        return OSM_SOURCE.equals(sourceId) ? licences.terms("ODbL-1.0") : licences.terms(null);
    }

    private Instant osmExtractDate() {
        try {
            List<String> values = jdbc.queryForList("SELECT adapter_state ->> ? FROM source_health WHERE source_id = ?",
                    String.class, OSM_MTIME_STATE, OSM_SOURCE);
            return values.isEmpty() || values.getFirst() == null ? null : Instant.parse(values.getFirst());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The edges of every enabled rule, deduplicated by {@code (from, to, relation, rule)}. */
    List<EdgeData> edges(List<InfraRules.Rule> rules, List<NodeData> nodeData, List<String> warnings) {
        Map<String, EdgeData> out = new LinkedHashMap<>();
        for (InfraRules.Rule rule : rules) {
            if (!rule.enabled()) {
                warnings.add("rule " + rule.id() + " is disabled");
                continue;
            }
            List<EdgeData> produced = switch (rule.id()) {
                case "cable-landing" -> cableLanding(rule, nodeData);
                case "line-substation" -> lineSubstation(rule, nodeData);
                case "plant-substation" -> plantSubstation(rule, nodeData);
                case "substation-critical-load" -> substationLoad(rule, nodeData, warnings);
                case "ixp-facility" -> {
                    warnings.add("rule ixp-facility has no PeeringDB facility data mapped; no edges");
                    yield List.of();
                }
                case "airport-port-serves" -> {
                    warnings.add("rule airport-port-serves needs region nodes, which the graph does not have; no edges");
                    yield List.of();
                }
                default -> {
                    warnings.add("rule " + rule.id() + " is not implemented");
                    yield List.of();
                }
            };
            for (EdgeData edge : produced) {
                out.putIfAbsent(edge.key(), edge);
            }
        }
        return new ArrayList<>(out.values());
    }

    // ------------------------------------------------------------------ rules

    /**
     * {@code cable-landing}: a cable whose route passes within {@code withinKm} of a landing point
     * lands there. The 094 cable data does not list each cable's landing points, so this is a
     * geometry match ({@code topological}, {@code likely}), not a declaration.
     */
    List<EdgeData> cableLanding(InfraRules.Rule rule, List<NodeData> nodeData) {
        double withinKm = rule.number("withinKm", 5);
        Index cables = new Index(of(nodeData, "telecom.cable"));
        List<EdgeData> out = new ArrayList<>();
        for (NodeData landing : of(nodeData, "telecom.landing")) {
            if (landing.point() == null) {
                continue;
            }
            for (NodeData cable : cables.near(landing.point().getCoordinate(), withinKm)) {
                double distance = distanceKm(cable.geom(), landing.point());
                if (distance <= withinKm) {
                    out.add(new EdgeData(cable, landing, rule.relation(), rule.method(), rule.label(),
                            score(distance, withinKm), rule.id(), rule.params(),
                            evidence(cable, landing, distance, Map.of("cable", String.valueOf(cable.name()),
                                    "landing", String.valueOf(landing.name())),
                                    "geometry match: the cable route passes within " + withinKm + " km of the"
                                            + " landing point; the dataset does not declare landings per cable")));
                }
            }
        }
        return out;
    }

    /**
     * {@code line-substation}: a power-line endpoint within {@code withinMeters} of a substation's
     * geometry. {@code likely}; {@code confirmed} when both ends reach (different) substations.
     */
    List<EdgeData> lineSubstation(InfraRules.Rule rule, List<NodeData> nodeData) {
        double withinKm = rule.number("withinMeters", 50) / 1000.0;
        Index substations = new Index(of(nodeData, "power.substation"));
        List<EdgeData> out = new ArrayList<>();
        for (NodeData line : of(nodeData, "power.line")) {
            Coordinate[] ends = endpoints(line.geom());
            if (ends == null) {
                continue;
            }
            NodeData[] hit = new NodeData[2];
            double[] distance = new double[2];
            for (int i = 0; i < 2; i++) {
                Point end = Geo.point(ends[i].x, ends[i].y);
                double best = Double.MAX_VALUE;
                for (NodeData substation : substations.near(ends[i], withinKm)) {
                    double d = distanceKm(substation.geom() != null ? substation.geom() : substation.point(), end);
                    if (d <= withinKm && d < best) {
                        best = d;
                        hit[i] = substation;
                    }
                }
                distance[i] = best;
            }
            boolean both = hit[0] != null && hit[1] != null && hit[0].id() != hit[1].id();
            String label = both ? "confirmed" : rule.label();
            for (int i = 0; i < 2; i++) {
                if (hit[i] == null || (i == 1 && hit[0] != null && hit[0].id() == hit[1].id())) {
                    continue;
                }
                out.add(new EdgeData(hit[i], line, rule.relation(), rule.method(), InfraRules.cap(rule.method(), label),
                        both ? 1.0 : 0.8, rule.id(), rule.params(),
                        evidence(hit[i], line, distance[i], Map.of("endpoint", i == 0 ? "start" : "end",
                                "bothEnds", both), both ? "both line ends reach substations"
                                        : "one line end reaches a substation")));
            }
        }
        return out;
    }

    /**
     * {@code plant-substation}: a generating plant supplies the nearest substation within
     * {@code min(maxKm, capacityFactorKm × √capacityMw)} km ({@code maxKm} when the capacity is
     * unknown). Proximity, so never better than {@code possible}.
     */
    List<EdgeData> plantSubstation(InfraRules.Rule rule, List<NodeData> nodeData) {
        double capacityFactor = rule.number("capacityFactorKm", 2);
        double maxKm = rule.number("maxKm", 5);
        Index substations = new Index(of(nodeData, "power.substation"));
        List<NodeData> plants = new ArrayList<>(of(nodeData, "power.generation"));
        plants.addAll(of(nodeData, "power.nuclear"));
        List<EdgeData> out = new ArrayList<>();
        for (NodeData plant : plants) {
            if (plant.point() == null) {
                continue;
            }
            double capacity = number(plant.attributes().get("capacityMw"), 0);
            double radius = capacity > 0 ? Math.min(maxKm, capacityFactor * Math.sqrt(capacity)) : maxKm;
            List<Ranked> ranked = substations.ranked(plant.point(), radius);
            if (ranked.isEmpty()) {
                continue;
            }
            Ranked closest = ranked.getFirst();
            Map<String, Object> matched = new LinkedHashMap<>();
            matched.put("capacityMw", capacity > 0 ? capacity : null);
            matched.put("radiusKm", radius);
            out.add(new EdgeData(plant, closest.node(), rule.relation(), rule.method(), rule.label(),
                    score(closest.distanceKm(), radius), rule.id(), rule.params(),
                    evidence(plant, closest.node(), closest.distanceKm(), matched, "nearest substation within the"
                            + " capacity-scaled radius")));
        }
        return out;
    }

    /**
     * {@code substation-critical-load}: a hospital, data center, airport, water facility or port is
     * supplied by the nearest substation within the radius ({@code possible}); when two or more lie
     * in range, the two nearest each get a {@code speculative} edge. The urban radius needs populated
     * place polygons, which the reference data does not have, so the rural radius applies everywhere
     * and the build records a warning.
     */
    List<EdgeData> substationLoad(InfraRules.Rule rule, List<NodeData> nodeData, List<String> warnings) {
        double radius = rule.number("ruralKm", 10);
        warnings.add("rule substation-critical-load uses the rural radius (" + radius + " km) everywhere; the urban"
                + " radius needs populated-place polygons");
        Index substations = new Index(of(nodeData, "power.substation"));
        List<EdgeData> out = new ArrayList<>();
        for (String type : List.of("health.hospital", "telecom.datacentre", "transport.airport", "water.treatment",
                "transport.port")) {
            for (NodeData load : of(nodeData, type)) {
                if (load.point() == null) {
                    continue;
                }
                List<Ranked> ranked = substations.ranked(load.point(), radius);
                boolean split = ranked.size() >= 2;
                for (int i = 0; i < Math.min(2, ranked.size()) && (i == 0 || split); i++) {
                    Ranked substation = ranked.get(i);
                    out.add(new EdgeData(substation.node(), load, rule.relation(), rule.method(),
                            split ? "speculative" : rule.label(), score(substation.distanceKm(), radius), rule.id(),
                            rule.params(), evidence(substation.node(), load, substation.distanceKm(),
                                    Map.of("rank", i + 1, "inRange", ranked.size()),
                                    split ? "two or more substations in range; the two nearest are attached"
                                            : "the only substation in range")));
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ helpers

    private static Map<String, Object> evidence(NodeData from, NodeData to, double distanceKm,
            Map<String, Object> matched, String notes) {
        Map<String, Object> out = new LinkedHashMap<>();
        Set<String> datasets = new LinkedHashSet<>(List.of(from.sourceId(), to.sourceId()));
        out.put("dataset", String.join(",", datasets));
        out.put("distanceKm", Double.isFinite(distanceKm) ? Math.round(distanceKm * 1000.0) / 1000.0 : null);
        out.put("matched", matched);
        out.put("notes", notes);
        return out;
    }

    private static double score(double distanceKm, double radiusKm) {
        return radiusKm <= 0 ? 0 : Math.max(0, Math.min(1, 1 - distanceKm / radiusKm));
    }

    /** The two ends of a line (first and last coordinate), or null for anything else. */
    static Coordinate[] endpoints(Geometry geom) {
        if (geom instanceof LineString line && !line.isEmpty()) {
            return new Coordinate[] {line.getCoordinateN(0), line.getCoordinateN(line.getNumPoints() - 1)};
        }
        if (geom instanceof MultiLineString multi && !multi.isEmpty()) {
            LineString first = (LineString) multi.getGeometryN(0);
            LineString last = (LineString) multi.getGeometryN(multi.getNumGeometries() - 1);
            return new Coordinate[] {first.getCoordinateN(0), last.getCoordinateN(last.getNumPoints() - 1)};
        }
        return null;
    }

    /** The great-circle distance from {@code point} to the nearest point of {@code geom}, km. */
    static double distanceKm(Geometry geom, Point point) {
        if (geom == null || point == null || geom.isEmpty()) {
            return Double.MAX_VALUE;
        }
        Coordinate nearest = geom instanceof Point p ? p.getCoordinate() : DistanceOp.nearestPoints(geom, point)[0];
        return Geo.haversineKm(point.getY(), point.getX(), nearest.y, nearest.x);
    }

    private static List<NodeData> of(List<NodeData> nodes, String type) {
        return nodes.stream().filter(n -> n.facilityType().equals(type)).toList();
    }

    /** A candidate with its distance. */
    record Ranked(NodeData node, double distanceKm) {}

    /** An R-tree over node envelopes, for the radius searches of the rules. */
    static final class Index {

        private final STRtree tree = new STRtree();

        Index(List<NodeData> nodes) {
            for (NodeData node : nodes) {
                Geometry shape = node.geom() != null ? node.geom() : node.point();
                if (shape != null && !shape.isEmpty()) {
                    tree.insert(shape.getEnvelopeInternal(), node);
                }
            }
        }

        /** The nodes whose envelope lies within about {@code km} of {@code at}, in build order. */
        @SuppressWarnings("unchecked")
        List<NodeData> near(Coordinate at, double km) {
            double dLat = km / 110.574;
            double dLon = km / (111.320 * Math.max(0.01, Math.cos(Math.toRadians(at.y))));
            List<NodeData> found = new ArrayList<>(tree.query(new Envelope(at.x - dLon, at.x + dLon, at.y - dLat,
                    at.y + dLat)));
            found.sort(Comparator.comparingInt(NodeData::index));
            return found;
        }

        /** The nodes within {@code km} of {@code at} by their representative point, nearest first. */
        List<Ranked> ranked(Point at, double km) {
            List<Ranked> out = new ArrayList<>();
            for (NodeData node : near(at.getCoordinate(), km)) {
                if (node.point() == null) {
                    continue;
                }
                double d = Geo.haversineKm(at.getY(), at.getX(), node.point().getY(), node.point().getX());
                if (d <= km) {
                    out.add(new Ranked(node, d));
                }
            }
            out.sort(Comparator.comparingDouble(Ranked::distanceKm).thenComparingInt(r -> r.node().index()));
            return out;
        }
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

    /** The source ids the node mapping reads (an import of one of them makes the graph stale). */
    public Set<String> mappedSources() {
        return new LinkedHashSet<>(asMap(read(NODE_MAPPING).get("sources")).keySet());
    }

    /** The bundled rules resource, parsed. */
    public Map<String, Object> bundledRules() {
        return read(RULES);
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
        if (raw instanceof Number n) {
            return n.doubleValue();
        }
        if (raw != null) {
            try {
                return Double.parseDouble(String.valueOf(raw));
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
        return fallback;
    }
}
