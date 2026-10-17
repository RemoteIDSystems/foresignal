package ai.foresignal.trip;

import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.trigger.GeofenceMaterializer;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Finds the hazards of a trip occurrence (prompt 114): current records ({@code record_head ⋈ record_live}) of the
 * trip's hazard kinds within twice the buffer of the route, classified as
 *
 * <ul>
 *   <li>{@code on-route}: intersecting the corridor;
 *   <li>{@code near-route}: within 2 × the buffer but outside the corridor;
 *   <li>{@code timing}: a forecast hazard (008 {@code validFrom} still in the future) whose {@code validFrom} falls
 *       in {@code [departAt, departAt + expectedDuration]}; one valid only later is left out;
 * </ul>
 *
 * <p>and ordered by position along the route ({@link RouteMeasure}). Records that expire before departure,
 * earthquakes below M4, hazards below the trip's {@code minSeverity} (when their severity is known) and private
 * sources (a briefing leaves through notification channels) are left out.
 */
@Component
public class TripHazards {

    /** The most hazard records one check reads. */
    static final int MAX_HAZARDS = 2_000;

    /** The smallest earthquake magnitude reported. */
    static final double MIN_MAGNITUDE = 4.0;

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final GeofenceMaterializer geofences;
    private final SourceVisibility visibility;
    private final JsonMapper json;

    /** Creates the finder. */
    public TripHazards(NamedParameterJdbcTemplate jdbc, GeofenceMaterializer geofences, SourceVisibility visibility,
            JsonMapper json) {
        this.jdbc = jdbc;
        this.geofences = geofences;
        this.visibility = visibility;
        this.json = json;
    }

    /**
     * One hazard (OpenAPI {@code TripHazard}).
     *
     * @param sourceId the source
     * @param externalId the entity
     * @param kind the kind
     * @param title the title, or null
     * @param severity {@code info}, {@code warning}, {@code critical} or null
     * @param section {@code on-route}, {@code near-route} or {@code timing}
     * @param alongMeters the distance along the route of its projection
     * @param offsetMeters its distance from the route
     * @param validFrom its validity start, or null
     * @param validUntil its expiry, or null
     * @param ahead with an anchor: true when ahead of the anchor's projection; null without one
     * @param etaSeconds with an anchor: the ETA along the route; null otherwise
     */
    public record Hazard(String sourceId, String externalId, String kind, String title, String severity, String section,
            double alongMeters, double offsetMeters, Instant validFrom, Instant validUntil, Boolean ahead,
            Double etaSeconds) {

        /** This hazard with anchor progress. */
        public Hazard withProgress(Boolean value, Double eta) {
            return new Hazard(sourceId, externalId, kind, title, severity, section, alongMeters, offsetMeters, validFrom,
                    validUntil, value, eta);
        }

        /** The OpenAPI {@code TripHazard} object. */
        public Map<String, Object> toMap() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ref", "record:" + sourceId + "/" + java.net.URLEncoder.encode(externalId,
                    java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));
            out.put("sourceId", sourceId);
            out.put("externalId", externalId);
            out.put("kind", kind);
            out.put("title", title);
            out.put("severity", severity);
            out.put("section", section);
            out.put("distanceAlongRouteM", Math.round(alongMeters));
            out.put("distanceFromRouteM", Math.round(offsetMeters));
            out.put("validFrom", validFrom);
            out.put("validUntil", validUntil);
            out.put("ahead", ahead);
            out.put("etaSeconds", etaSeconds == null ? null : (double) Math.round(etaSeconds));
            return out;
        }
    }

    /**
     * The hazards of {@code trip} for the occurrence departing at {@code departAt}, ordered along the route.
     *
     * @param trip the trip
     * @param departAt the departure
     * @param now the evaluation time
     */
    public List<Hazard> find(TripEntity trip, Instant departAt, Instant now) {
        Instant arrive = departAt.plusSeconds(trip.getExpectedDurationSeconds());
        MultiPolygon corridor = trip.getCorridor();
        MultiPolygon near = geofences.offset(corridor, trip.getBufferMeters());
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("corridor", new WKBWriter(2).write(corridor))
                .addValue("near", new WKBWriter(2).write(near))
                .addValue("kinds", ai.foresignal.store.query.PgArrays.text(trip.getHazardKinds()))
                .addValue("departAt", Timestamp.from(departAt));
        String sql = """
                SELECT h.source_id, h.external_id, h.kind, h.title, h.valid_from, l.valid_until,
                       jsonb_strip_nulls(h.payload || l.kinematic)::text AS payload, ST_AsBinary(l.geom) AS geom,
                       ST_Intersects(l.geom, ST_GeomFromWKB(CAST(:corridor AS bytea), 4326)) AS on_route
                  FROM record_head h JOIN record_live l USING (source_id, external_id)
                 WHERE NOT h.tombstone AND h.kind = ANY (CAST(:kinds AS text[])) AND l.geom IS NOT NULL
                   AND (l.valid_until IS NULL OR l.valid_until > :departAt)
                   AND l.geom && ST_GeomFromWKB(CAST(:near AS bytea), 4326)
                   AND ST_Intersects(l.geom, ST_GeomFromWKB(CAST(:near AS bytea), 4326))
                 LIMIT """ + MAX_HAZARDS;
        RouteMeasure measure = new RouteMeasure(RouteMeasure.splitAntimeridian(
                (org.locationtech.jts.geom.LineString) trip.getRoute()));
        int minRank = trip.getMinSeverity() == null ? -1 : rank(trip.getMinSeverity());
        List<Hazard> out = new ArrayList<>();
        jdbc.query(sql, p, rs -> {
            String sourceId = rs.getString("source_id");
            if (visibility.isPrivate(sourceId)) {
                return;
            }
            String kind = rs.getString("kind");
            Map<String, Object> payload = rs.getString("payload") == null ? Map.of()
                    : json.readValue(rs.getString("payload"), MAP);
            if ("earthquake".equals(kind)) {
                Object mag = payload.get("mag");
                if (!(mag instanceof Number m) || m.doubleValue() < MIN_MAGNITUDE) {
                    return;
                }
            }
            String severity = severity(kind, payload);
            if (minRank >= 0 && severity != null && rank(severity) < minRank) {
                return;
            }
            Instant validFrom = instant(rs.getObject("valid_from", OffsetDateTime.class));
            Instant validUntil = instant(rs.getObject("valid_until", OffsetDateTime.class));
            String section;
            if (validFrom != null && validFrom.isAfter(now)) {
                if (validFrom.isBefore(departAt) || validFrom.isAfter(arrive)) {
                    return;
                }
                section = "timing";
            } else {
                section = rs.getBoolean("on_route") ? "on-route" : "near-route";
            }
            Geometry geometry = read(rs.getBytes("geom"));
            RouteMeasure.Located at = measure.locate(geometry);
            String title = rs.getString("title");
            out.add(new Hazard(sourceId, rs.getString("external_id"), kind, title, severity, section, at.alongMeters(),
                    "on-route".equals(section) ? Math.min(at.offsetMeters(), trip.getBufferMeters()) : at.offsetMeters(),
                    validFrom, validUntil, null, null));
        });
        out.sort(Comparator.comparingDouble(Hazard::alongMeters).thenComparing(Hazard::sourceId)
                .thenComparing(Hazard::externalId));
        return out;
    }

    /**
     * Anchor progress (prompt 114): hazards behind the anchor's projection are dropped; the rest are marked ahead
     * with the ETA along the route at {@code speedMps} (null without a usable speed).
     *
     * @param hazards the hazards, ordered along the route
     * @param anchorAlong the anchor's projection along the route, meters
     * @param speedMps the anchor's recent average speed, or null
     */
    public static List<Hazard> ahead(List<Hazard> hazards, double anchorAlong, Double speedMps) {
        List<Hazard> out = new ArrayList<>();
        for (Hazard h : hazards) {
            if (h.alongMeters() < anchorAlong) {
                continue;
            }
            Double eta = speedMps == null || speedMps < 0.5 ? null : (h.alongMeters() - anchorAlong) / speedMps;
            out.add(h.withProgress(true, eta));
        }
        return out;
    }

    /**
     * The severity of a hazard record: from an earthquake's magnitude (M6+ critical, M5+ warning, else info), or
     * from a payload {@code severity} string (extreme or critical → critical; severe, major, high, moderate or
     * warning → warning; minor, low, info or unknown → info); null when unknown.
     */
    static String severity(String kind, Map<String, Object> payload) {
        if ("earthquake".equals(kind) && payload.get("mag") instanceof Number m) {
            return m.doubleValue() >= 6 ? "critical" : m.doubleValue() >= 5 ? "warning" : "info";
        }
        Object value = payload.get("severity");
        if (!(value instanceof String s)) {
            return null;
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "extreme", "critical" -> "critical";
            case "severe", "major", "high", "moderate", "warning" -> "warning";
            case "minor", "low", "info", "unknown" -> "info";
            default -> null;
        };
    }

    /** The rank of a severity ({@code info} 0, {@code warning} 1, {@code critical} 2), or −1. */
    public static int rank(String severity) {
        return severity == null ? -1 : switch (severity) {
            case "info" -> 0;
            case "warning" -> 1;
            case "critical" -> 2;
            default -> -1;
        };
    }

    private static Instant instant(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }

    private static Geometry read(byte[] wkb) {
        try {
            Geometry g = new WKBReader(RouteMeasure.FACTORY).read(wkb);
            g.setSRID(4326);
            return g;
        } catch (ParseException e) {
            throw new IllegalStateException("PostGIS returned unreadable WKB: " + e.getMessage(), e);
        }
    }
}
