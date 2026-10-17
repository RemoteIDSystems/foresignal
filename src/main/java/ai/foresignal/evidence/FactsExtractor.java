package ai.foresignal.evidence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads the comparable {@link ObservationFacts} of one version (prompt 116) from its payload, under its family
 * rule and its source's upstream references. Pure.
 */
public final class FactsExtractor {

    private FactsExtractor() {}

    /**
     * The facts of a version.
     *
     * @param rule the version's family rule
     * @param refs the source's upstream references
     * @param rules the canonical rules (for the hazard synonyms)
     * @param sourceId the source
     * @param externalId the record
     * @param seq the seq
     * @param version the record version, or null
     * @param eventTime the event time (the version's {@code event_time}, else {@code observed_at})
     * @param observedAt the version's {@code observed_at}
     * @param lon the representative longitude, or null
     * @param lat the representative latitude, or null
     * @param payload the version's payload
     */
    public static ObservationFacts extract(CanonicalRules.FamilyRule rule, UpstreamRefs refs, CanonicalRules rules,
            String sourceId, String externalId, long seq, Integer version, Instant eventTime, Instant observedAt,
            Double lon, Double lat, Map<String, Object> payload) {
        Map<String, Object> p = payload == null ? Map.of() : payload;
        Double attribute = null;
        if (rule.attribute() != null) {
            for (String field : rule.attribute().fields()) {
                if (p.get(field) instanceof Number n && Double.isFinite(n.doubleValue())) {
                    attribute = n.doubleValue();
                    break;
                }
            }
        }
        String country = firstText(p, rule.countryFields());
        String asn = firstText(p, rule.asnFields());
        Instant start = firstInstant(p, rule.startFields());
        Instant end = firstInstant(p, rule.endFields());
        String hazard = null;
        for (String field : rule.typeFields()) {
            for (String candidate : texts(p.get(field))) {
                hazard = rules.hazard(candidate);
                if (hazard != null) {
                    break;
                }
            }
            if (hazard != null) {
                break;
            }
        }
        List<String> upstream = new ArrayList<>();
        for (UpstreamRefs.EventIdField field : refs.eventIds()) {
            Object value = UpstreamRefs.EXTERNAL_ID.equals(field.field()) ? externalId : p.get(field.field());
            for (String text : texts(value)) {
                String id = field.namespace() + ":" + text.strip();
                if (!upstream.contains(id)) {
                    upstream.add(id);
                }
            }
        }
        String citation = null;
        for (String field : refs.citations()) {
            citation = citation(p.get(field));
            if (citation != null) {
                break;
            }
        }
        return new ObservationFacts(sourceId, externalId, seq, version, eventTime, observedAt, lon, lat, attribute,
                country == null ? null : country.toUpperCase(Locale.ROOT), asn, start, end, hazard, upstream, citation);
    }

    /**
     * The normalized citation in a payload value: a string, the first element of a list, or the {@code url},
     * {@code id} or {@code name} member of an object.
     */
    static String citation(Object value) {
        if (value instanceof Collection<?> list) {
            for (Object item : list) {
                String c = citation(item);
                if (c != null) {
                    return c;
                }
            }
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            for (String key : List.of("url", "id", "name")) {
                String c = citation(map.get(key));
                if (c != null) {
                    return c;
                }
            }
            return null;
        }
        return value == null ? null : OriginatorRegistry.normalizeCitation(String.valueOf(value));
    }

    private static List<String> texts(Object value) {
        List<String> out = new ArrayList<>();
        if (value instanceof Collection<?> list) {
            for (Object item : list) {
                if (item != null && !(item instanceof Map<?, ?>) && !String.valueOf(item).isBlank()) {
                    out.add(String.valueOf(item));
                }
            }
        } else if (value != null && !(value instanceof Map<?, ?>) && !String.valueOf(value).isBlank()) {
            out.add(String.valueOf(value));
        }
        return out;
    }

    private static String firstText(Map<String, Object> payload, List<String> fields) {
        for (String field : fields) {
            Object value = payload.get(field);
            if (value != null && !String.valueOf(value).isBlank() && !(value instanceof Map<?, ?>)
                    && !(value instanceof Collection<?>)) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private static Instant firstInstant(Map<String, Object> payload, List<String> fields) {
        for (String field : fields) {
            Object value = payload.get(field);
            if (value == null) {
                continue;
            }
            if (value instanceof Number n) {
                long v = n.longValue();
                return v > 100_000_000_000L ? Instant.ofEpochMilli(v) : Instant.ofEpochSecond(v);
            }
            try {
                return OffsetDateTime.parse(String.valueOf(value)).toInstant();
            } catch (DateTimeParseException e) {
                try {
                    return Instant.parse(String.valueOf(value));
                } catch (DateTimeParseException ignored) {
                    // not a time
                }
            }
        }
        return null;
    }
}
