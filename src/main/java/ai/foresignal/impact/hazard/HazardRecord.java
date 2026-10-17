package ai.foresignal.impact.hazard;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;

/**
 * One stored record version a hazard adapter reads (prompt 127): the current head of a hazard record, pinned by its
 * seq.
 *
 * @param sourceId the source
 * @param externalId the record's external id
 * @param seq the version's seq
 * @param kind the kind
 * @param payload the payload
 * @param geometry the geometry, or null
 * @param observedAt the observation time
 * @param eventTime the event time, or null
 * @param validFrom the start of validity, or null
 * @param validUntil the explicit end of validity, or null
 */
public record HazardRecord(String sourceId, String externalId, long seq, String kind, Map<String, Object> payload,
        Geometry geometry, Instant observedAt, Instant eventTime, Instant validFrom, Instant validUntil) {

    /** Copies the payload. */
    public HazardRecord {
        payload = payload == null ? Map.of() : java.util.Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }

    /** The pinned reference {@code record:{sourceId}/{externalId}@{seq}} (external id percent-encoded). */
    public String ref() {
        return "record:" + sourceId + "/" + encode(externalId) + "@" + seq;
    }

    /** A payload field as text, or null. */
    public String text(String field) {
        Object v = payload.get(field);
        return v == null ? null : v.toString();
    }

    /** A payload field as a number, or null when absent or not numeric. */
    public Double number(String field) {
        Object v = payload.get(field);
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        if (v instanceof String s) {
            try {
                return Double.parseDouble(s.strip());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A payload field as an instant, or null. */
    public Instant instant(String field) {
        String v = text(field);
        if (v == null) {
            return null;
        }
        try {
            return Instant.parse(v);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The Client Contract encoding of an external id ({@code %}, {@code /} and {@code @}). */
    static String encode(String externalId) {
        return externalId.replace("%", "%25").replace("/", "%2F").replace("@", "%40");
    }
}
