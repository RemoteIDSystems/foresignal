package ai.foresignal.trigger.window;

import ai.foresignal.entity.EntityService;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.ingest.JsonValues;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Resolves the membership of a window (prompt 100): the distinct entities seen in
 * {@code (t − window, t]} either through a live row ({@code observed_at} in the window) or through a
 * non-tombstone, non-heartbeat version with {@code seq ≤ watermark}, filtered by the rule's
 * source/kind/condition/geofence filters.
 *
 * <p>Distinct keys follow the window's {@code distinctBy}; versions are never counted. The rows are
 * capped, then conditions and groups are evaluated in Java, so a rule can group by payload path,
 * source, H3 cell, place or region.
 */
@Service
public class WindowMembership {

    /** The most candidate rows one evaluation loads. */
    public static final int ROW_CAP = 200_000;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final EntityService entities;
    private final RegionTagger regions;
    private final PlaceService places;

    /** Creates the resolver. */
    public WindowMembership(JdbcTemplate jdbc, JsonMapper json, EntityService entities, RegionTagger regions,
            PlaceService places) {
        this.jdbc = jdbc;
        this.json = json;
        this.entities = entities;
        this.regions = regions;
        this.places = places;
    }

    /** One candidate record. */
    public record Member(String sourceId, String externalId, String kind, Map<String, Object> payload,
            Geometry geometry, Instant observedAt, long seq) {}

    /** A window's membership. */
    public record Membership(Map<String, Integer> counts, Map<String, List<Member>> samples,
            boolean groupsTruncated) {}

    /** Resolves the membership of a window for a rule. */
    public Membership resolve(RuleSpec rule, WindowSpec window, Instant from, Instant to, long watermark) {
        RecordPredicate predicate = new RecordPredicate(rule.sources(), rule.kinds(), rule.conditions(), rule.fence());
        List<Member> members = candidates(from, to, watermark, predicate);
        Map<String, String> distinct = distinctKeys(members, window.distinctBy());
        Map<String, Set<String>> groups = new LinkedHashMap<>();
        Map<String, List<Member>> samples = new LinkedHashMap<>();
        for (Member member : members) {
            String key = distinct.get(member.sourceId() + '\u0000' + member.externalId());
            if (key == null) {
                continue;
            }
            String group = groupKey(rule, window.groupBy(), member);
            groups.computeIfAbsent(group, k -> new LinkedHashSet<>()).add(key);
            samples.computeIfAbsent(group, k -> new ArrayList<>()).add(member);
        }
        boolean truncated = groups.size() > window.maxGroups();
        Map<String, Integer> counts = new TreeMap<>(Comparator.reverseOrder());
        Map<String, List<Member>> boundedSamples = new TreeMap<>(Comparator.reverseOrder());
        List<Map.Entry<String, Set<String>>> ordered = new ArrayList<>(groups.entrySet());
        ordered.sort((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()));
        for (Map.Entry<String, Set<String>> entry : ordered.subList(0, Math.min(ordered.size(), window.maxGroups()))) {
            counts.put(entry.getKey(), entry.getValue().size());
            boundedSamples.put(entry.getKey(), samples.get(entry.getKey()));
        }
        return new Membership(counts, boundedSamples, truncated);
    }

    private List<Member> candidates(Instant from, Instant to, long watermark, RecordPredicate predicate) {
        String sql = "SELECT source_id, external_id, kind, payload, ST_AsText(geom) AS wkt, observed_at, seq FROM ("
                + " SELECT h.source_id, h.external_id, h.kind, jsonb_strip_nulls(h.payload || l.kinematic) AS payload,"
                + " l.geom AS geom, l.observed_at AS observed_at, h.seq AS seq"
                + " FROM record_head h JOIN record_live l USING (source_id, external_id)"
                + " WHERE NOT h.tombstone AND l.observed_at > ?"
                + " UNION ALL"
                + " SELECT v.source_id, v.external_id, v.kind, v.payload, v.geom, v.observed_at, v.seq"
                + " FROM record_version v"
                + " WHERE v.seq <= ? AND v.observed_at > ? AND v.observed_at <= ? AND NOT v.tombstone"
                + "   AND v.persist_reason <> 8"
                + ") q LIMIT " + ROW_CAP;
        List<Member> out = new ArrayList<>();
        jdbc.query(sql, rs -> {
            Map<String, Object> payload;
            String text = rs.getString("payload");
            payload = text == null ? Map.of() : json.readValue(text, MAP);
            Geometry geometry = readWkt(rs.getString("wkt"));
            if (!predicate.test(rs.getString("source_id"), rs.getString("kind"), geometry, payload)) {
                out.add(null);
                return;
            }
            out.add(new Member(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"), payload,
                    geometry, null, 0));
        }, java.sql.Timestamp.from(from), watermark, java.sql.Timestamp.from(from), java.sql.Timestamp.from(to));
        out.removeIf(java.util.Objects::isNull);
        return out;
    }

    private Map<String, String> distinctKeys(List<Member> members, String distinctBy) {
        Map<String, String> keys = new LinkedHashMap<>();
        if ("externalId".equals(distinctBy)) {
            for (Member member : members) {
                keys.put(member.sourceId() + '\u0000' + member.externalId(), member.externalId());
            }
            return keys;
        }
        if ("entity".equals(distinctBy)) {
            List<Map.Entry<String, String>> pairs = new ArrayList<>();
            for (Member member : members) {
                pairs.add(Map.entry(member.sourceId(), member.externalId()));
            }
            Map<String, String> entityIds = entities.openEntityIds(pairs);
            for (Member member : members) {
                String key = member.sourceId() + '\u0000' + member.externalId();
                String entityId = entityIds.get(member.sourceId() + ":" + member.externalId());
                keys.put(key, entityId != null ? "entity:" + entityId : member.sourceId() + "|" + member.externalId());
            }
            return keys;
        }
        for (Member member : members) {
            Object value = JsonValues.at(member.payload(), distinctBy);
            keys.put(member.sourceId() + '\u0000' + member.externalId(),
                    value == null ? member.sourceId() + "|" + member.externalId() : String.valueOf(value));
        }
        return keys;
    }

    private String groupKey(RuleSpec rule, String groupBy, Member member) {
        if (groupBy == null || groupBy.isBlank()) {
            return "*";
        }
        if ("source".equals(groupBy)) {
            return member.sourceId();
        }
        if (groupBy.startsWith("h3:")) {
            Double lon = member.geometry() == null ? null : member.geometry().getCentroid().getX();
            Double lat = member.geometry() == null ? null : member.geometry().getCentroid().getY();
            if (lon == null || lat == null) {
                return "*";
            }
            int res;
            try {
                res = Integer.parseInt(groupBy.substring(3));
            } catch (NumberFormatException e) {
                res = 5;
            }
            return H3Cells.cellToAddress(H3Cells.latLngToCell(lat, lon, res));
        }
        if ("country".equals(groupBy) || "admin1".equals(groupBy)) {
            RegionTags tags = regionTags(member);
            String value = "country".equals(groupBy) ? tags.iso2() : tags.admin1();
            return value == null ? "*" : value;
        }
        if ("place".equals(groupBy)) {
            return place(member);
        }
        Object value = JsonValues.at(member.payload(), groupBy);
        return value == null ? "*" : String.valueOf(value);
    }

    private RegionTags regionTags(Member member) {
        Double lon = member.geometry() == null ? null : member.geometry().getCentroid().getX();
        Double lat = member.geometry() == null ? null : member.geometry().getCentroid().getY();
        if (lon == null || lat == null) {
            return RegionTags.EMPTY;
        }
        try {
            return regions.tag(lon, lat);
        } catch (RuntimeException e) {
            return RegionTags.EMPTY;
        }
    }

    private String place(Member member) {
        if (member.geometry() == null) {
            return "*";
        }
        for (PlaceView place : places.all()) {
            if (place.area() != null && !place.area().isEmpty() && place.area().intersects(member.geometry())) {
                return place.id();
            }
        }
        return "*";
    }

    private static Geometry readWkt(String wkt) {
        if (wkt == null || wkt.isBlank()) {
            return null;
        }
        try {
            Geometry geometry = new WKTReader(Geo.factory()).read(wkt);
            geometry.setSRID(Geo.SRID);
            return geometry;
        } catch (Exception e) {
            return null;
        }
    }
}
