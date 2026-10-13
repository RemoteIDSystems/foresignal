package ai.foresignal.weather;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

/**
 * The record reads of the weather intelligence layer (prompt 056): current heads through
 * {@code record_head ⋈ record_live} (invariant HIST-2) and, for briefing periods, versions by seq range.
 * Nothing here writes or fetches anything upstream.
 */
public interface WeatherRecords {

    /**
     * One record (a current head or a version).
     *
     * @param sourceId the source
     * @param externalId the entity
     * @param kind the kind
     * @param seq the version seq
     * @param observedAt its observation time
     * @param payload its payload (current payload with live fields for a head)
     * @param geometry its geometry, or null
     * @param distanceKm the distance to the query geometry in km, or null when not asked
     */
    record Row(String sourceId, String externalId, String kind, long seq, Instant observedAt,
            Map<String, Object> payload, Geometry geometry, Double distanceKm) {

        /** Copies the payload. */
        public Row {
            payload = payload == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(payload));
        }
    }

    /**
     * Current heads of one kind within {@code radiusKm} of {@code geometry}, nearest first.
     *
     * @param sourceId the source
     * @param kind the kind
     * @param geometry the query geometry (WGS84)
     * @param radiusKm the search radius
     * @param limit the most rows
     */
    List<Row> nearby(String sourceId, String kind, Geometry geometry, double radiusKm, int limit);

    /**
     * Current heads of one kind whose geometry intersects {@code geometry} and that are not expired at {@code at}.
     *
     * @param sourceId the source
     * @param kind the kind
     * @param geometry the query geometry (WGS84)
     * @param at the time judged
     * @param limit the most rows
     */
    List<Row> intersecting(String sourceId, String kind, Geometry geometry, Instant at, int limit);

    /** The current head of one entity, or empty (also for a tombstone). */
    Optional<Row> head(String sourceId, String externalId);

    /**
     * The versions of one entity with {@code fromSeq < seq ≤ toSeq}, oldest first, preceded by the latest version at
     * or before {@code fromSeq} (the state at the start of the window) when one is retained.
     *
     * @param limit the most versions inside the window
     */
    List<Row> versions(String sourceId, String externalId, long fromSeq, long toSeq, int limit);

    /**
     * The versions with {@code fromSeq < seq ≤ toSeq} of the given sources and kinds that intersect {@code area}
     * while their previous version did not (or had none): hazards that entered the area. One row per entity (its
     * first entry), oldest first.
     *
     * @param limit the most rows
     */
    List<Row> entered(List<String> sources, List<String> kinds, Geometry area, long fromSeq, long toSeq, int limit);
}
