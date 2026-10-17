package ai.foresignal.evidence;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The comparable facts of one observation (prompt 116): what the {@link EvidenceMatcher} compares and what an
 * {@code observation_link} stores, so later observations are matched against stored facts.
 *
 * @param sourceId the source
 * @param externalId the record
 * @param seq the version's seq
 * @param version the record version, or null
 * @param eventTime the event time (the version's {@code event_time}, else {@code observed_at})
 * @param observedAt the version's {@code observed_at}
 * @param lon the longitude of the representative point, or null
 * @param lat the latitude of the representative point, or null
 * @param attribute the compared attribute value (the magnitude), or null
 * @param country the country (outages), or null
 * @param asn the autonomous system (outages), or null
 * @param start the interval start (outages), or null
 * @param end the interval end (outages), or null
 * @param hazard the normalized hazard type (disasters), or null
 * @param upstreamIds the upstream event ids as {@code namespace:value}
 * @param citation the normalized cited original source, or null
 */
public record ObservationFacts(String sourceId, String externalId, long seq, Integer version, Instant eventTime,
        Instant observedAt, Double lon, Double lat, Double attribute, String country, String asn, Instant start,
        Instant end, String hazard, List<String> upstreamIds, String citation) {

    /** Copies the list. */
    public ObservationFacts {
        upstreamIds = upstreamIds == null ? List.of() : List.copyOf(upstreamIds);
    }

    /** True when the observation has a point. */
    public boolean located() {
        return lon != null && lat != null;
    }

    /** The record ref {@code record:{source}/{externalId}@{seq}}. */
    public String ref() {
        return RecordRef.format(sourceId, externalId, seq);
    }

    /** The stored {@code attributes} jsonb. */
    public Map<String, Object> attributes() {
        Map<String, Object> out = new LinkedHashMap<>();
        if (attribute != null) {
            out.put("attribute", attribute);
        }
        if (country != null) {
            out.put("country", country);
        }
        if (asn != null) {
            out.put("asn", asn);
        }
        if (start != null) {
            out.put("start", start.toString());
        }
        if (end != null) {
            out.put("end", end.toString());
        }
        if (hazard != null) {
            out.put("hazard", hazard);
        }
        if (!upstreamIds.isEmpty()) {
            out.put("upstreamIds", upstreamIds);
        }
        if (citation != null) {
            out.put("citation", citation);
        }
        return out;
    }

    /** Facts rebuilt from a stored link's columns and {@code attributes}. */
    public static ObservationFacts fromStored(String sourceId, String externalId, long seq, Integer version,
            Instant eventTime, Instant observedAt, Double lon, Double lat, Map<String, Object> attributes) {
        Map<String, Object> a = attributes == null ? Map.of() : attributes;
        List<String> upstream = new java.util.ArrayList<>();
        if (a.get("upstreamIds") instanceof List<?> list) {
            for (Object item : list) {
                upstream.add(String.valueOf(item));
            }
        }
        return new ObservationFacts(sourceId, externalId, seq, version, eventTime, observedAt, lon, lat,
                a.get("attribute") instanceof Number n ? n.doubleValue() : null, text(a.get("country")),
                text(a.get("asn")), instant(a.get("start")), instant(a.get("end")), text(a.get("hazard")), upstream,
                text(a.get("citation")));
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Instant instant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
