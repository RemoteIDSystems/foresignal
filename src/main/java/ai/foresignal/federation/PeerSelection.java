package ai.foresignal.federation;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Selective replication from a peer (prompt 113, OpenAPI {@code PeerSelection}). Remote filters (sources, kinds,
 * bbox) are passed as query parameters and re-checked locally; {@code maxAge}, {@code maxHops} and a place's
 * polygon are applied locally only.
 *
 * @param sources remote source ids, empty for all
 * @param kinds kinds, empty for all
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param placeId a place whose area bounds what is kept (checked locally), or null
 * @param maxAge items whose {@code observedAt} is older than this are dropped, or null for no limit
 * @param backfill {@code none} (the current snapshot only) or a duration of history to page from {@code /changes}
 * @param live also receive the live lane
 * @param maxHops the hop limit (default 3)
 * @param trackTtl the TTL of {@code peer-{id}-trk} (default 10 min)
 */
public record PeerSelection(List<String> sources, List<String> kinds, List<Double> bbox, String placeId,
        Duration maxAge, String backfill, boolean live, int maxHops, Duration trackTtl) {

    /** The default track TTL. */
    public static final Duration DEFAULT_TRACK_TTL = Duration.ofMinutes(10);

    /** The shortest track TTL (the keyframe must fit below it). */
    public static final Duration MIN_TRACK_TTL = Duration.ofMinutes(2);

    /** {@code backfill: none}. */
    public static final String NO_BACKFILL = "none";

    /** Normalizes nulls. */
    public PeerSelection {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        bbox = bbox == null ? null : List.copyOf(bbox);
        backfill = backfill == null || backfill.isBlank() ? NO_BACKFILL : backfill.strip();
        maxHops = maxHops < 1 ? FederationRules.DEFAULT_MAX_HOPS : maxHops;
        trackTtl = trackTtl == null ? DEFAULT_TRACK_TTL : trackTtl;
    }

    /** Everything, with the defaults. */
    public static PeerSelection all() {
        return new PeerSelection(List.of(), List.of(), null, null, null, NO_BACKFILL, false,
                FederationRules.DEFAULT_MAX_HOPS, DEFAULT_TRACK_TTL);
    }

    /** The backfill duration, or null for {@code none}. */
    public Duration backfillDuration() {
        if (NO_BACKFILL.equalsIgnoreCase(backfill)) {
            return null;
        }
        try {
            return Durations.parsePositive(backfill);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Parses a stored or requested selection, adding every problem to {@code errors} under {@code field}.
     *
     * @param map the JSON object, or null for {@link #all()}
     */
    @SuppressWarnings("unchecked")
    public static PeerSelection parse(Map<String, Object> map, String field, List<FieldError> errors) {
        if (map == null) {
            return all();
        }
        List<String> sources = strings(map.get("sources"), field + ".sources", errors);
        List<String> kinds = strings(map.get("kinds"), field + ".kinds", errors);
        List<Double> bbox = null;
        Object rawBbox = map.get("bbox");
        if (rawBbox instanceof List<?> list) {
            bbox = new ArrayList<>();
            for (Object value : list) {
                if (value instanceof Number n) {
                    bbox.add(n.doubleValue());
                }
            }
            if (bbox.size() != 4 || bbox.get(0) >= bbox.get(2) || bbox.get(1) >= bbox.get(3)
                    || bbox.get(0) < -180 || bbox.get(2) > 180 || bbox.get(1) < -90 || bbox.get(3) > 90) {
                errors.add(new FieldError(field + ".bbox", "must be [minLon, minLat, maxLon, maxLat] within ±180/±90"));
                bbox = null;
            }
        } else if (rawBbox != null) {
            errors.add(new FieldError(field + ".bbox", "must be an array of four numbers or null"));
        }
        String placeId = map.get("placeId") instanceof String s && !s.isBlank() ? s.strip() : null;
        Duration maxAge = duration(map.get("maxAge"), field + ".maxAge", errors);
        String backfill = map.get("backfill") instanceof String s ? s.strip() : NO_BACKFILL;
        if (!NO_BACKFILL.equalsIgnoreCase(backfill)) {
            try {
                Durations.parsePositive(backfill);
            } catch (RuntimeException e) {
                errors.add(new FieldError(field + ".backfill", "must be 'none' or a positive duration"));
                backfill = NO_BACKFILL;
            }
        }
        boolean live = Boolean.TRUE.equals(map.get("live"));
        int maxHops = FederationRules.DEFAULT_MAX_HOPS;
        Object hops = map.get("maxHops");
        if (hops instanceof Number n && n.intValue() >= 1 && n.doubleValue() == n.intValue()) {
            maxHops = n.intValue();
        } else if (hops != null) {
            errors.add(new FieldError(field + ".maxHops", "must be an integer of at least 1"));
        }
        Duration trackTtl = duration(map.get("trackTtl"), field + ".trackTtl", errors);
        if (trackTtl != null && trackTtl.compareTo(MIN_TRACK_TTL) < 0) {
            errors.add(new FieldError(field + ".trackTtl", "must be at least " + MIN_TRACK_TTL.toMinutes() + " minutes"));
            trackTtl = null;
        }
        return new PeerSelection(sources, kinds, bbox, placeId, maxAge, backfill, live, maxHops, trackTtl);
    }

    /** The JSON form (durations as ISO-8601). */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("sources", sources);
        out.put("kinds", kinds);
        out.put("bbox", bbox);
        out.put("placeId", placeId);
        out.put("maxAge", maxAge == null ? null : maxAge.toString());
        out.put("backfill", backfill);
        out.put("live", live);
        out.put("maxHops", maxHops);
        out.put("trackTtl", trackTtl.toString());
        return out;
    }

    /** True when {@code lon, lat} lies in the bbox (or no bbox is set). */
    public boolean inBbox(double lon, double lat) {
        return bbox == null || (lon >= bbox.get(0) && lon <= bbox.get(2) && lat >= bbox.get(1) && lat <= bbox.get(3));
    }

    private static List<String> strings(Object value, String field, List<FieldError> errors) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof String s && !s.isBlank()) {
                out.add(s.strip());
            } else {
                errors.add(new FieldError(field, "must contain only non-blank strings"));
                return List.of();
            }
        }
        return out;
    }

    private static Duration duration(Object value, String field, List<FieldError> errors) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text)) {
            errors.add(new FieldError(field, "must be a duration (ISO-8601 or 15m, 12h, 7d)"));
            return null;
        }
        try {
            return Durations.parsePositive(text);
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, "must be a positive duration (ISO-8601 or 15m, 12h, 7d)"));
            return null;
        }
    }
}
