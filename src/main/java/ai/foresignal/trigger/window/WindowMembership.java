package ai.foresignal.trigger.window;

import ai.foresignal.entity.EntityKeys;
import ai.foresignal.entity.Key;
import ai.foresignal.geo.H3Cells;
import ai.foresignal.ingest.JsonValues;
import ai.foresignal.place.PlaceService;
import ai.foresignal.place.PlaceView;
import ai.foresignal.reference.RegionTags;
import ai.foresignal.reference.RegionTagger;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.trigger.RecordPredicate;
import ai.foresignal.trigger.RuleSpec;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Resolves the membership of a window (prompt 100): the distinct entities seen in
 * {@code (from, to]} either through a live row or through a non-tombstone, non-heartbeat version with
 * {@code seq ≤ watermark}, filtered by the rule's sources, kinds, geofence and conditions.
 *
 * <p><strong>In SQL:</strong> the source and kind filters, the geofence ({@code ST_Intersects}), the
 * time bounds on both sides, the knowledge bound ({@code ingested_at}/{@code last_received_at ≤
 * knownBy}, invariant INT-2) and the entity link valid at {@code to}. Rows come back in a fixed order
 * ({@code source_id, external_id, seq}), so samples and every derived value are deterministic
 * (invariant INT-1). Conditions, distinct keys and groups are then applied in Java while streaming:
 * no row list is materialized, only one distinct-key set per group and up to 20 sample references.
 *
 * <p><strong>Cap:</strong> at most {@value #ROW_CAP} candidate rows are read. A window that has more
 * returns {@link Membership#capped()} and no counts: the caller must treat it as unknown, never as a
 * (smaller) count.
 *
 * <p><strong>Identity:</strong> {@code distinctBy=entity} counts the 098 entity linked to the record at
 * {@code to}; otherwise the record's strongest descriptor {@code entityKeys} value; otherwise
 * {@code (source, externalId)}. Versions are never counted.
 */
@Service
public class WindowMembership {

    /** The most candidate rows one evaluation reads; more makes the result unknown. */
    public static final int ROW_CAP = 200_000;

    /** The most sample references kept per group. */
    public static final int SAMPLES_PER_GROUP = 20;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonMapper json;
    private final SourceRegistry registry;
    private final RegionTagger regions;
    private final PlaceService places;

    /** Creates the resolver. */
    public WindowMembership(NamedParameterJdbcTemplate jdbc, JsonMapper json, SourceRegistry registry,
            RegionTagger regions, PlaceService places) {
        this.jdbc = jdbc;
        this.json = json;
        this.registry = registry;
        this.regions = regions;
        this.places = places;
    }

    /**
     * One sampled member: the record and the version (or head) seq it was seen at.
     *
     * @param sourceId the source
     * @param externalId the record
     * @param kind the kind
     * @param observedAt when it was observed
     * @param seq the version's seq (for a live row, the head's seq)
     */
    public record Member(String sourceId, String externalId, String kind, Instant observedAt, long seq) {}

    /**
     * A window's membership.
     *
     * @param counts distinct members per group, largest first, at most {@code maxGroups}
     * @param samples up to {@value #SAMPLES_PER_GROUP} members per group, in row order
     * @param groupsTruncated true when there were more groups than {@code maxGroups}
     * @param capped true when the candidate rows exceeded {@value #ROW_CAP}: counts are then empty and
     *     the result is unknown
     * @param rowsRead candidate rows read
     */
    public record Membership(Map<String, Integer> counts, Map<String, List<Member>> samples,
            boolean groupsTruncated, boolean capped, int rowsRead) {}

    /**
     * Resolves the membership of the current window: live rows and versions, everything known now.
     *
     * @param rule the compiled rule (sources, kinds, conditions, geofence)
     * @param window the window spec (distinctBy, groupBy, maxGroups)
     * @param from the exclusive window start
     * @param to the inclusive window end (now)
     * @param watermark the highest version seq read
     */
    public Membership resolve(RuleSpec rule, WindowSpec window, Instant from, Instant to, long watermark) {
        return resolve(rule, window, from, to, watermark, true, to, ROW_CAP);
    }

    /**
     * Resolves the membership of a window.
     *
     * @param rule the compiled rule
     * @param window the window spec
     * @param from the exclusive window start
     * @param to the inclusive window end
     * @param watermark the highest version seq read
     * @param includeLive true to count entities through their live rows (the current window only); a
     *     past bucket reads versions only, so it is reproducible from stored inputs
     * @param knownBy the decision time: rows ingested (versions) or last received (live rows) after it
     *     are not read (INT-2)
     * @param cap the most candidate rows read before the result is unknown
     */
    public Membership resolve(RuleSpec rule, WindowSpec window, Instant from, Instant to, long watermark,
            boolean includeLive, Instant knownBy, int cap) {
        RecordPredicate predicate = new RecordPredicate(rule.sources(), rule.kinds(), rule.conditions(), rule.fence());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", Timestamp.from(from))
                .addValue("to", Timestamp.from(to))
                .addValue("knownBy", Timestamp.from(knownBy))
                .addValue("watermark", watermark)
                .addValue("limit", cap + 1);
        String filters = filters(rule, params);
        StringBuilder sql = new StringBuilder("SELECT q.source_id, q.external_id, q.kind, q.payload::text AS payload,"
                + " ST_AsText(q.geom) AS wkt, q.observed_at, q.seq, el.entity_id FROM (");
        if (includeLive) {
            sql.append(" SELECT h.source_id, h.external_id, h.kind, jsonb_strip_nulls(h.payload || l.kinematic) AS payload,"
                    + " l.geom AS geom, l.observed_at AS observed_at, h.seq AS seq"
                    + " FROM record_head h JOIN record_live l USING (source_id, external_id)"
                    + " WHERE NOT h.tombstone AND l.observed_at > :from AND l.observed_at <= :to"
                    + " AND l.last_received_at <= :knownBy")
                    .append(filters.replace("{s}", "h.source_id").replace("{k}", "h.kind").replace("{g}", "l.geom"))
                    .append(" UNION ALL");
        }
        sql.append(" SELECT v.source_id, v.external_id, v.kind, v.payload, v.geom, v.observed_at, v.seq"
                + " FROM record_version v"
                + " WHERE v.seq <= :watermark AND v.observed_at > :from AND v.observed_at <= :to"
                + " AND v.ingested_at <= :knownBy AND NOT v.tombstone AND v.persist_reason <> 8")
                .append(filters.replace("{s}", "v.source_id").replace("{k}", "v.kind").replace("{g}", "v.geom"))
                .append(") q LEFT JOIN LATERAL (SELECT l.entity_id FROM entity_link l"
                        + " WHERE l.source_id = q.source_id AND l.external_id = q.external_id"
                        + " AND l.valid_from <= :to AND (l.valid_to IS NULL OR l.valid_to > :to)"
                        + " ORDER BY l.valid_from DESC, l.id DESC LIMIT 1) el ON TRUE"
                        + " ORDER BY q.source_id, q.external_id, q.seq LIMIT :limit");

        Accumulator acc = new Accumulator(rule, window, predicate, cap);
        jdbc.query(sql.toString(), params, rs -> {
            acc.row(rs.getString("source_id"), rs.getString("external_id"), rs.getString("kind"),
                    rs.getString("payload"), rs.getString("wkt"), rs.getTimestamp("observed_at"), rs.getLong("seq"),
                    rs.getString("entity_id"));
        });
        return acc.result();
    }

    /** The source, kind and geofence filters, with {@code {s}/{k}/{g}} placeholders for the column names. */
    private static String filters(RuleSpec rule, MapSqlParameterSource params) {
        StringBuilder out = new StringBuilder();
        if (rule.sources() != null && !rule.sources().isEmpty()) {
            out.append(" AND {s} = ANY (CAST(:sources AS text[]))");
            params.addValue("sources", pgArray(rule.sources()));
        }
        if (rule.kinds() != null && !rule.kinds().isEmpty()) {
            out.append(" AND {k} = ANY (CAST(:kinds AS text[]))");
            params.addValue("kinds", pgArray(rule.kinds()));
        }
        if (rule.fence() != null && !rule.fence().isEmpty()) {
            out.append(" AND {g} IS NOT NULL AND ST_Intersects({g}, ST_GeomFromText(:fence, 4326))");
            params.addValue("fence", new WKTWriter().write(rule.fence()));
        }
        return out.toString();
    }

    /** A Postgres text-array literal of {@code values}, sorted (deterministic statements). */
    static String pgArray(Set<String> values) {
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (String value : new java.util.TreeSet<>(values)) {
            if (!first) {
                out.append(',');
            }
            first = false;
            out.append('"').append(value.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        }
        return out.append('}').toString();
    }

    /** Streams candidate rows into per-group distinct sets. */
    private final class Accumulator {

        private final RuleSpec rule;
        private final WindowSpec window;
        private final RecordPredicate predicate;
        private final int cap;
        private final Map<String, Set<String>> groups = new LinkedHashMap<>();
        private final Map<String, List<Member>> samples = new LinkedHashMap<>();
        private final Map<String, Set<String>> sampled = new LinkedHashMap<>();
        private int rows;

        Accumulator(RuleSpec rule, WindowSpec window, RecordPredicate predicate, int cap) {
            this.rule = rule;
            this.window = window;
            this.predicate = predicate;
            this.cap = cap;
        }

        void row(String sourceId, String externalId, String kind, String payloadText, String wkt, Timestamp observed,
                long seq, String entityId) {
            rows++;
            if (rows > cap) {
                return;
            }
            Map<String, Object> payload = payloadText == null ? Map.of() : json.readValue(payloadText, MAP);
            Geometry geometry = readWkt(wkt);
            if (!predicate.test(sourceId, kind, geometry, payload)) {
                return;
            }
            String key = distinctKey(window.distinctBy(), sourceId, externalId, payload, entityId);
            String group = groupKey(window.groupBy(), sourceId, payload, geometry);
            groups.computeIfAbsent(group, g -> new HashSet<>()).add(key);
            List<Member> list = samples.computeIfAbsent(group, g -> new ArrayList<>());
            if (list.size() < SAMPLES_PER_GROUP
                    && sampled.computeIfAbsent(group, g -> new HashSet<>()).add(sourceId + '\u0000' + externalId)) {
                list.add(new Member(sourceId, externalId, kind, observed == null ? null : observed.toInstant(), seq));
            }
        }

        Membership result() {
            if (rows > cap) {
                return new Membership(Map.of(), Map.of(), false, true, rows);
            }
            boolean truncated = groups.size() > window.maxGroups();
            List<Map.Entry<String, Set<String>>> ordered = new ArrayList<>(groups.entrySet());
            // Largest first, ties by group key, so the kept groups never depend on row order.
            ordered.sort(Comparator.<Map.Entry<String, Set<String>>>comparingInt(e -> -e.getValue().size())
                    .thenComparing(Map.Entry::getKey));
            Map<String, Integer> counts = new TreeMap<>();
            Map<String, List<Member>> kept = new TreeMap<>();
            for (Map.Entry<String, Set<String>> entry : ordered.subList(0, Math.min(ordered.size(), window.maxGroups()))) {
                counts.put(entry.getKey(), entry.getValue().size());
                kept.put(entry.getKey(), List.copyOf(samples.getOrDefault(entry.getKey(), List.of())));
            }
            return new Membership(counts, kept, truncated, false, rows);
        }
    }

    /** The distinct key of a member under {@code distinctBy}. */
    private String distinctKey(String distinctBy, String sourceId, String externalId, Map<String, Object> payload,
            String entityId) {
        if ("externalId".equals(distinctBy)) {
            return externalId;
        }
        if (distinctBy == null || "entity".equals(distinctBy)) {
            if (entityId != null) {
                return "entity:" + entityId;
            }
            String declared = declaredKey(sourceId, payload);
            return declared != null ? declared : sourceId + "|" + externalId;
        }
        Object value = JsonValues.at(payload, distinctBy);
        return value == null ? sourceId + "|" + externalId : String.valueOf(value);
    }

    /** The strongest {@code entityKeys} value the record declares ({@code key:<type>:<value>}), or null. */
    private String declaredKey(String sourceId, Map<String, Object> payload) {
        SourceDescriptor descriptor = registry.find(sourceId).map(r -> r.descriptor()).orElse(null);
        if (descriptor == null || descriptor.entityKeys().isEmpty()) {
            return null;
        }
        List<Key> keys = new ArrayList<>(EntityKeys.extract(descriptor, payload));
        keys.removeIf(k -> "callsign".equals(k.keyType()));
        if (keys.isEmpty()) {
            return null;
        }
        keys.sort(Comparator.comparing((Key k) -> !k.anchor()).thenComparing(Key::keyType).thenComparing(Key::keyValue));
        Key best = keys.get(0);
        return "key:" + best.keyType() + ":" + best.keyValue();
    }

    private String groupKey(String groupBy, String sourceId, Map<String, Object> payload, Geometry geometry) {
        if (groupBy == null || groupBy.isBlank()) {
            return "*";
        }
        if ("source".equals(groupBy)) {
            return sourceId;
        }
        if (groupBy.startsWith("h3:")) {
            if (geometry == null || geometry.isEmpty()) {
                return "*";
            }
            int res;
            try {
                res = Integer.parseInt(groupBy.substring(3));
            } catch (NumberFormatException e) {
                res = 5;
            }
            var centroid = geometry.getCentroid();
            return H3Cells.cellToAddress(H3Cells.latLngToCell(centroid.getY(), centroid.getX(), res));
        }
        if ("country".equals(groupBy) || "admin1".equals(groupBy)) {
            RegionTags tags = regionTags(geometry);
            String value = "country".equals(groupBy) ? tags.iso2() : tags.admin1();
            return value == null ? "*" : value;
        }
        if ("place".equals(groupBy)) {
            return place(geometry);
        }
        Object value = JsonValues.at(payload, groupBy);
        return value == null ? "*" : String.valueOf(value);
    }

    private RegionTags regionTags(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return RegionTags.EMPTY;
        }
        try {
            var centroid = geometry.getCentroid();
            return regions.tag(centroid.getX(), centroid.getY());
        } catch (RuntimeException e) {
            return RegionTags.EMPTY;
        }
    }

    /** The first place (by id) whose area contains the member; {@code *} when none does. */
    private String place(Geometry geometry) {
        if (geometry == null) {
            return "*";
        }
        List<PlaceView> all = new ArrayList<>(places.all());
        all.sort(Comparator.comparing(PlaceView::id));
        for (PlaceView place : all) {
            if (place.area() != null && !place.area().isEmpty() && place.area().intersects(geometry)) {
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
