package ai.foresignal.impact;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.infra.InfraImpactService;
import ai.foresignal.intel.ExposureService;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * What a hazard area exposes (prompt 127 §2): the admin1 regions it intersects (040 {@code ref_admin1}), the 104
 * infrastructure facilities inside it with their dependency paths to downstream assets (each with its weakest-edge
 * label), the 036 places it intersects, and the 101 exposure summary (facility counts, places, population) of an area.
 *
 * <p>Each part degrades on its own: without reference regions there are no admin1 regions (and so no projection);
 * without an infrastructure build, or for an area too large to traverse, the facilities are left out with a caveat.
 */
@Component
public class ImpactExposure {

    private static final Logger log = LoggerFactory.getLogger(ImpactExposure.class);

    /** The dependency depth followed from an exposed facility. */
    public static final int DEPTH = 2;

    /** The weakest label a dependency path may have. */
    public static final String MIN_LABEL = "possible";

    /** The most facilities kept per hazard. */
    public static final int MAX_FACILITIES = 500;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectProvider<InfraImpactService> infra;
    private final ObjectProvider<PlaceService> places;
    private final ObjectProvider<ExposureService> exposure;

    /** Creates the component. */
    public ImpactExposure(NamedParameterJdbcTemplate jdbc, ObjectProvider<InfraImpactService> infra,
            ObjectProvider<PlaceService> places, ObjectProvider<ExposureService> exposure) {
        this.jdbc = jdbc;
        this.infra = infra;
        this.places = places;
        this.exposure = exposure;
    }

    /**
     * An admin1 region.
     *
     * @param code the 040 code ({@code US-TX})
     * @param name its name
     * @param iso2 its country
     * @param geometry its (simplified) polygon
     */
    public record Admin1(String code, String name, String iso2, Geometry geometry) {}

    /**
     * An exposed asset.
     *
     * @param ref its object reference ({@code record:…} for a facility, {@code place:…} for a place)
     * @param title its name
     * @param kind its facility type, or {@code place}
     * @param geometry its geometry
     */
    public record Asset(String ref, String title, String kind, Geometry geometry) {}

    /**
     * A dependency path from an exposed facility to a downstream asset.
     *
     * @param from the exposed facility
     * @param to the downstream asset
     * @param hops the nodes between them, in order
     * @param weakestEdgeLabel the path's weakest edge label (104 vocabulary)
     * @param depth the path's length in edges
     */
    public record Path(String from, String to, List<String> hops, String weakestEdgeLabel, int depth) {}

    /**
     * The facilities and paths of an area.
     *
     * @param facilities the exposed facilities
     * @param paths their dependency paths
     * @param caveats why a part is missing (code and text)
     */
    public record Infrastructure(List<Asset> facilities, List<Path> paths, List<Map<String, Object>> caveats) {}

    /** The admin1 regions intersecting an area, by code. */
    public List<Admin1> admin1(Geometry area) {
        if (area == null || area.isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource p = new MapSqlParameterSource("wkb", new WKBWriter().write(area));
        List<Admin1> out = new ArrayList<>();
        jdbc.query("SELECT code, name, iso2, ST_AsBinary(coalesce(geom_simple, geom)) AS g FROM ref_admin1 WHERE"
                + " ST_Intersects(geom, ST_SetSRID(ST_GeomFromWKB(:wkb), 4326)) ORDER BY code", p, rs -> {
                    try {
                        Geometry g = new WKBReader().read(rs.getBytes("g"));
                        g.setSRID(4326);
                        out.add(new Admin1(rs.getString("code"), rs.getString("name"), rs.getString("iso2"), g));
                    } catch (ParseException e) {
                        log.debug("Unreadable admin1 polygon {}", rs.getString("code"));
                    }
                });
        return out;
    }

    /** The 036 places whose area intersects {@code area}. */
    public List<Asset> places(Geometry area) {
        PlaceService service = places.getIfAvailable();
        if (service == null || area == null) {
            return List.of();
        }
        List<Asset> out = new ArrayList<>();
        for (PlaceView place : service.all()) {
            Geometry g = place.area() != null ? place.area() : place.geometry();
            if (g != null && !place.sensitive() && g.intersects(area)) {
                out.add(new Asset("place:" + place.id(), place.name(), "place", g));
            }
        }
        return out;
    }

    /** The 101 exposure summary of an area (facility counts, places, population), or an unavailable marker. */
    public Map<String, Object> summary(Geometry area) {
        ExposureService service = exposure.getIfAvailable();
        if (service == null || area == null || area.isEmpty()) {
            return Map.of("status", "unavailable");
        }
        try {
            return service.exposure(area, true);
        } catch (RuntimeException e) {
            log.debug("Exposure summary failed: {}", e.toString());
            return Map.of("status", "unavailable");
        }
    }

    /** The 104 facilities inside an area and their dependency paths. */
    @SuppressWarnings("unchecked")
    public Infrastructure infrastructure(Geometry area) {
        InfraImpactService service = infra.getIfAvailable();
        List<Map<String, Object>> caveats = new ArrayList<>();
        if (service == null || area == null || area.isEmpty()) {
            return new Infrastructure(List.of(), List.of(), caveats);
        }
        Map<String, Object> affected;
        try {
            affected = service.affected(area, DEPTH, MIN_LABEL, null);
        } catch (ForesignalException e) {
            caveats.add(Map.of("code", "INFRASTRUCTURE_NOT_EVALUATED", "text", "Infrastructure dependencies were not"
                    + " evaluated: " + e.getMessage()));
            return new Infrastructure(List.of(), List.of(), caveats);
        } catch (RuntimeException e) {
            log.warn("Infrastructure exposure failed: {}", e.toString());
            caveats.add(Map.of("code", "INFRASTRUCTURE_NOT_EVALUATED", "text", "Infrastructure dependencies were not"
                    + " evaluated."));
            return new Infrastructure(List.of(), List.of(), caveats);
        }
        List<Asset> facilities = new ArrayList<>();
        Map<Long, String> refs = new HashMap<>();
        for (Object o : (List<Object>) affected.getOrDefault("direct", List.of())) {
            Map<String, Object> node = (Map<String, Object>) o;
            Asset asset = asset(node);
            if (asset != null && facilities.size() < MAX_FACILITIES) {
                facilities.add(asset);
                refs.put(((Number) node.get("id")).longValue(), asset.ref());
            }
        }
        List<Map<String, Object>> dependents = (List<Map<String, Object>>) affected.getOrDefault("dependents",
                List.of());
        Set<Long> edgeIds = new LinkedHashSet<>();
        for (Map<String, Object> d : dependents) {
            for (Object e : (List<Object>) d.getOrDefault("path", List.of())) {
                Object id = ((Map<String, Object>) e).get("edgeId");
                if (id instanceof Number n) {
                    edgeIds.add(n.longValue());
                }
            }
        }
        Map<Long, long[]> edges = edges(edgeIds);
        Set<Long> nodeIds = new LinkedHashSet<>();
        edges.values().forEach(e -> {
            nodeIds.add(e[0]);
            nodeIds.add(e[1]);
        });
        nodeIds.removeAll(refs.keySet());
        refs.putAll(nodeRefs(nodeIds));
        List<Path> paths = new ArrayList<>();
        for (Map<String, Object> d : dependents) {
            Map<String, Object> node = (Map<String, Object>) d.get("node");
            if (node == null || !(node.get("id") instanceof Number target)) {
                continue;
            }
            List<Long> sequence = sequence((List<Object>) d.getOrDefault("path", List.of()), edges,
                    target.longValue());
            if (sequence.size() < 2 || refs.get(sequence.get(0)) == null || refs.get(target.longValue()) == null) {
                continue;
            }
            List<String> hops = new ArrayList<>();
            for (int i = 1; i < sequence.size() - 1; i++) {
                String ref = refs.get(sequence.get(i));
                if (ref != null) {
                    hops.add(ref);
                }
            }
            paths.add(new Path(refs.get(sequence.get(0)), refs.get(target.longValue()), hops,
                    String.valueOf(d.get("pathLabel")), ((Number) d.getOrDefault("depth", sequence.size() - 1))
                            .intValue()));
        }
        for (Object c : (List<Object>) affected.getOrDefault("caveats", List.of())) {
            if (c instanceof Map<?, ?> m) {
                caveats.add((Map<String, Object>) m);
            }
        }
        return new Infrastructure(facilities, paths, caveats);
    }

    /**
     * The node sequence of a path that ends at {@code target}: walked backwards from the target over the edges.
     *
     * @param path the path's edges ({@code edgeId}), from the exposed facility outwards
     * @param edges edge id → {from node, to node}
     * @param target the downstream node
     */
    @SuppressWarnings("unchecked")
    static List<Long> sequence(List<Object> path, Map<Long, long[]> edges, long target) {
        List<Long> reversed = new ArrayList<>();
        reversed.add(target);
        long current = target;
        for (int i = path.size() - 1; i >= 0; i--) {
            Object id = ((Map<String, Object>) path.get(i)).get("edgeId");
            long[] e = id instanceof Number n ? edges.get(n.longValue()) : null;
            if (e == null) {
                return List.of();
            }
            long next = e[0] == current ? e[1] : e[1] == current ? e[0] : -1;
            if (next < 0) {
                return List.of();
            }
            reversed.add(next);
            current = next;
        }
        List<Long> out = new ArrayList<>();
        for (int i = reversed.size() - 1; i >= 0; i--) {
            out.add(reversed.get(i));
        }
        return out;
    }

    private Map<Long, long[]> edges(Set<Long> ids) {
        Map<Long, long[]> out = new HashMap<>();
        List<Long> all = new ArrayList<>(ids);
        for (int i = 0; i < all.size(); i += 1000) {
            List<Long> chunk = all.subList(i, Math.min(all.size(), i + 1000));
            jdbc.query("SELECT id, from_node, to_node FROM infra_edge WHERE id IN (:ids)",
                    new MapSqlParameterSource("ids", chunk), rs -> {
                        out.put(rs.getLong("id"), new long[] {rs.getLong("from_node"), rs.getLong("to_node")});
                    });
        }
        return out;
    }

    private Map<Long, String> nodeRefs(Set<Long> ids) {
        Map<Long, String> out = new HashMap<>();
        List<Long> all = new ArrayList<>(ids);
        for (int i = 0; i < all.size(); i += 1000) {
            List<Long> chunk = all.subList(i, Math.min(all.size(), i + 1000));
            jdbc.query("SELECT id, source_id, external_id FROM infra_node WHERE id IN (:ids)",
                    new MapSqlParameterSource("ids", chunk), rs -> {
                        out.put(rs.getLong("id"), recordRef(rs.getString("source_id"), rs.getString("external_id")));
                    });
        }
        return out;
    }

    private static Asset asset(Map<String, Object> node) {
        Object wkt = node.get("point");
        Geometry g = null;
        if (wkt != null) {
            try {
                g = new WKTReader().read(wkt.toString());
                g.setSRID(4326);
            } catch (ParseException e) {
                g = null;
            }
        }
        if (g == null || node.get("sourceId") == null || node.get("externalId") == null) {
            return null;
        }
        return new Asset(recordRef(node.get("sourceId").toString(), node.get("externalId").toString()),
                node.get("name") == null ? null : node.get("name").toString(),
                node.get("facilityType") == null ? "facility" : node.get("facilityType").toString(), g);
    }

    /** The unpinned record reference of a facility. */
    static String recordRef(String sourceId, String externalId) {
        return "record:" + sourceId + "/" + externalId.replace("%", "%25").replace("/", "%2F").replace("@", "%40");
    }

    /** A copy of the facility counts of an exposure summary, for the projection's inputs. */
    static Map<String, Object> compact(Map<String, Object> summary) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : List.of("facilities", "places", "population")) {
            if (summary.containsKey(key)) {
                out.put(key, summary.get(key));
            }
        }
        return out;
    }
}
