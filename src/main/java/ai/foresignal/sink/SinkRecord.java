package ai.foresignal.sink;

import ai.foresignal.provenance.LicenceTerms;
import java.time.Instant;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * One record handed to a sink (prompt 110): the neutral shape every sink type encodes. Carries the
 * record's geometry, its properties (payload), and its licence terms and attribution so an encoder can
 * print credits and the gate can refuse it.
 *
 * @param sourceId the source
 * @param externalId the record's external id
 * @param kind the record kind
 * @param seq the version seq (0 for a live-only update)
 * @param version the version number
 * @param observedAt the observation time
 * @param validUntil when the record expires, or null
 * @param tombstone true when the record was removed
 * @param geometry the geometry (a Point for a position), or null
 * @param properties the payload
 * @param licence the licence terms in force (prompt 109)
 * @param attribution the attribution line, or null
 */
public record SinkRecord(
        String sourceId,
        String externalId,
        String kind,
        long seq,
        int version,
        Instant observedAt,
        Instant validUntil,
        boolean tombstone,
        Geometry geometry,
        Map<String, Object> properties,
        LicenceTerms licence,
        String attribution) {

    /** The record's longitude when it is a point, else null. */
    public Double lon() {
        return geometry != null && !geometry.isEmpty() ? geometry.getCoordinate().x : null;
    }

    /** The record's latitude when it is a point, else null. */
    public Double lat() {
        return geometry != null && !geometry.isEmpty() ? geometry.getCoordinate().y : null;
    }

    /** A string property, or null. */
    public String string(String key) {
        Object value = properties == null ? null : properties.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** A numeric property, or null. */
    public Double number(String key) {
        Object value = properties == null ? null : properties.get(key);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
