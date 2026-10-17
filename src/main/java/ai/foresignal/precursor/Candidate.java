package ai.foresignal.precursor;

import ai.foresignal.analytics.math.Seeds;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * One candidate pattern of a mining batch (prompt 120): "does the antecedent tend to precede the consequent?".
 *
 * @param kind {@code event} or {@code series}
 * @param antecedent the antecedent side ({@code PrecursorSide}: {@code selector} or {@code seriesRef}, {@code family},
 *     {@code label}, {@code threshold})
 * @param consequent the consequent side
 * @param spatial the spatial rule ({@code PrecursorSpatial}: {@code rule}, {@code withinKm}, {@code regionLevel})
 * @param lagMin the shortest lag that counts (series: one bucket)
 * @param lagMax the longest lag that counts (series: {@code L} buckets)
 * @param regionCodes the 040 regions the candidate is about
 * @param origin {@code auto} or {@code operator}
 * @param candidateId the operator candidate it came from, or null
 * @param nAntecedent the antecedent's occurrence count (events) or usable points (series), for ranking
 * @param overlap the pair's data overlap, for ranking
 * @param frequency the series pair's bucket frequency ({@code day} or {@code week}), or null for events
 */
public record Candidate(String kind, Map<String, Object> antecedent, Map<String, Object> consequent,
        Map<String, Object> spatial, Duration lagMin, Duration lagMax, List<String> regionCodes, String origin,
        String candidateId, long nAntecedent, long overlap, String frequency) {

    /** An event candidate. */
    public static final String EVENT = "event";

    /** A series candidate. */
    public static final String SERIES = "series";

    /** Copies the collections. */
    public Candidate {
        antecedent = antecedent == null ? Map.of() : new LinkedHashMap<>(antecedent);
        consequent = consequent == null ? Map.of() : new LinkedHashMap<>(consequent);
        spatial = spatial == null ? Map.of("rule", "none") : new LinkedHashMap<>(spatial);
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
    }

    /**
     * The library key: the SHA-256 of the definition (kind, both sides' selectors or series references, the spatial
     * rule and the lag bounds), so that the same pattern found again updates its library row instead of adding one.
     */
    public String key() {
        return Seeds.sha256(kind, canonical(definition(antecedent)), canonical(definition(consequent)),
                canonical(spatial), String.valueOf(lagMin == null ? 0 : lagMin.toSeconds()),
                String.valueOf(lagMax == null ? 0 : lagMax.toSeconds()));
    }

    /** True for an event candidate. */
    public boolean event() {
        return EVENT.equals(kind);
    }

    /** The selector of a side, or null. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> selector(Map<String, Object> side) {
        return side.get("selector") instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    /** The series reference of a side, or null. */
    public static String seriesRef(Map<String, Object> side) {
        return side.get("seriesRef") == null ? null : side.get("seriesRef").toString();
    }

    private static Object definition(Map<String, Object> side) {
        Map<String, Object> out = new TreeMap<>();
        if (side.get("selector") != null) {
            out.put("selector", side.get("selector"));
        }
        if (side.get("seriesRef") != null) {
            out.put("seriesRef", side.get("seriesRef"));
        }
        return out;
    }

    /** A stable text form: maps with sorted keys, lists in order. */
    static String canonical(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((k, v) -> sorted.put(String.valueOf(k), v));
            StringBuilder out = new StringBuilder("{");
            sorted.forEach((k, v) -> out.append(k).append('=').append(canonical(v)).append(';'));
            return out.append('}').toString();
        }
        if (value instanceof List<?> list) {
            List<String> parts = new ArrayList<>();
            for (Object o : list) {
                parts.add(canonical(o));
            }
            return parts.toString();
        }
        if (value instanceof Number n) {
            double d = n.doubleValue();
            return d == Math.rint(d) && Math.abs(d) < 1e15 ? String.valueOf((long) d) : String.valueOf(d);
        }
        return String.valueOf(value);
    }
}
