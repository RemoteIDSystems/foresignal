package ai.foresignal.analyst;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.trigger.Severity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Envelope;

/**
 * An analyst profile's {@code scope} (prompt 102 §1), parsed: which inputs a run considers.
 *
 * @param sources alert and anomaly sources (empty: all)
 * @param kinds alert kinds (empty: all)
 * @param places place ids whose areas, with {@code bbox}, bound events and alerts (empty: anywhere)
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param eventTypes intelligence-event types (empty: all)
 * @param ruleTags alert and anomaly rule tags, any of which matches (empty: all)
 * @param minSeverity the lowest alert and event severity considered, or null for all
 */
public record AnalystScope(List<String> sources, List<String> kinds, List<String> places, Envelope bbox,
        List<String> eventTypes, List<String> ruleTags, Severity minSeverity) {

    /** Copies the lists. */
    public AnalystScope {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        places = places == null ? List.of() : List.copyOf(places);
        eventTypes = eventTypes == null ? List.of() : List.copyOf(eventTypes);
        ruleTags = ruleTags == null ? List.of() : List.copyOf(ruleTags);
    }

    /** No scope: everything. */
    public static AnalystScope all() {
        return new AnalystScope(List.of(), List.of(), List.of(), null, List.of(), List.of(), null);
    }

    /**
     * Parses the stored {@code scope} jsonb.
     *
     * @throws ValidationException naming the {@code scope.*} member that is malformed
     */
    public static AnalystScope of(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return all();
        }
        Envelope bbox = null;
        Object rawBox = map.get("bbox");
        if (rawBox != null) {
            if (!(rawBox instanceof List<?> box) || box.size() != 4 || !box.stream().allMatch(v -> v instanceof Number)) {
                throw ValidationException.forField("scope.bbox", "must be [minLon, minLat, maxLon, maxLat]");
            }
            double minLon = ((Number) box.get(0)).doubleValue();
            double minLat = ((Number) box.get(1)).doubleValue();
            double maxLon = ((Number) box.get(2)).doubleValue();
            double maxLat = ((Number) box.get(3)).doubleValue();
            if (minLon > maxLon || minLat > maxLat || minLon < -180 || maxLon > 180 || minLat < -90 || maxLat > 90) {
                throw ValidationException.forField("scope.bbox", "must be [minLon, minLat, maxLon, maxLat] in degrees");
            }
            bbox = new Envelope(minLon, maxLon, minLat, maxLat);
        }
        Severity minSeverity = null;
        Object rawSeverity = map.get("minSeverity");
        if (rawSeverity != null) {
            try {
                minSeverity = Severity.fromWire(String.valueOf(rawSeverity));
            } catch (IllegalArgumentException e) {
                throw ValidationException.forField("scope.minSeverity", "must be one of info, warning, critical");
            }
        }
        return new AnalystScope(strings(map, "sources"), strings(map, "kinds"), strings(map, "places"), bbox,
                strings(map, "eventTypes"), strings(map, "ruleTags"), minSeverity);
    }

    /** True when {@code severity} (a wire value) is at or above {@link #minSeverity}. */
    public boolean severityAllowed(String severity) {
        if (minSeverity == null) {
            return true;
        }
        try {
            return Severity.fromWire(severity).compareTo(minSeverity) >= 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** True when any of {@code tags} is one of {@link #ruleTags} (always true when no rule tags are set). */
    public boolean tagsAllowed(List<String> tags) {
        if (ruleTags.isEmpty()) {
            return true;
        }
        if (tags == null) {
            return false;
        }
        for (String tag : tags) {
            if (ruleTags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    /** True when a scope area ({@code bbox} or {@code places}) is set. */
    public boolean hasArea() {
        return bbox != null || !places.isEmpty();
    }

    private static List<String> strings(Map<String, Object> map, String key) {
        Object raw = map.get(key);
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw ValidationException.forField("scope." + key, "must be an array of strings");
        }
        List<String> out = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof String s)) {
                throw ValidationException.forField("scope." + key, "must be an array of strings");
            }
            out.add(s);
        }
        return out;
    }
}
