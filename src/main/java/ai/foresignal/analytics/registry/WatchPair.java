package ai.foresignal.analytics.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An event-study watch pair (prompt 119, OpenAPI {@code WatchPair}): qualifying events of {@code eventFamily}, in
 * {@code regionCodes} (empty = anywhere), are checked live against the registry row's series over the pre-event
 * window.
 *
 * @param eventFamily the signal family of the events
 * @param regionCodes the event regions (empty = any)
 * @param selector an optional narrower event selector (stored, applied by later prompts), or null
 * @param minEffect the minimum |CAR| in standard deviations (default 2.0)
 * @param releaseLagDays the series' publication lag for the re-check, or null for the series' own lag
 */
public record WatchPair(String eventFamily, List<String> regionCodes, Map<String, Object> selector, double minEffect,
        Integer releaseLagDays) {

    /** The default minimum effect. */
    public static final double DEFAULT_MIN_EFFECT = 2.0;

    /** Copies the collections and fills the default effect. */
    public WatchPair {
        regionCodes = regionCodes == null ? List.of() : List.copyOf(regionCodes);
        selector = selector == null ? null : Map.copyOf(selector);
        minEffect = minEffect > 0 ? minEffect : DEFAULT_MIN_EFFECT;
    }

    /** True when an event of {@code family} in {@code regions} qualifies. */
    public boolean matches(String family, java.util.Collection<String> regions) {
        if (!eventFamily.equals(family)) {
            return false;
        }
        if (regionCodes.isEmpty()) {
            return true;
        }
        for (String region : regions) {
            if (regionCodes.contains(region) || (region.length() > 2 && regionCodes.contains(region.substring(0, 2)))) {
                return true;
            }
        }
        return false;
    }

    /** The stored jsonb form. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("eventFamily", eventFamily);
        out.put("regionCodes", new ArrayList<>(regionCodes));
        out.put("selector", selector);
        out.put("minEffect", minEffect);
        out.put("releaseLagDays", releaseLagDays);
        return out;
    }

    /** Parses the stored form; null when it has no event family. */
    @SuppressWarnings("unchecked")
    public static WatchPair fromMap(Map<String, Object> map) {
        if (map == null || !(map.get("eventFamily") instanceof String family) || family.isBlank()) {
            return null;
        }
        List<String> regions = new ArrayList<>();
        if (map.get("regionCodes") instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof String s) {
                    regions.add(s);
                }
            }
        }
        double effect = map.get("minEffect") instanceof Number n ? n.doubleValue() : DEFAULT_MIN_EFFECT;
        Integer lag = map.get("releaseLagDays") instanceof Number n ? n.intValue() : null;
        Map<String, Object> selector = map.get("selector") instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
        return new WatchPair(family, regions, selector, effect, lag);
    }

    /** The parsed pairs of a row. */
    public static List<WatchPair> of(AnalysisSeriesEntity row) {
        List<WatchPair> out = new ArrayList<>();
        for (Map<String, Object> m : row.getWatchPairs()) {
            WatchPair pair = fromMap(m);
            if (pair != null) {
                out.add(pair);
            }
        }
        return out;
    }
}
