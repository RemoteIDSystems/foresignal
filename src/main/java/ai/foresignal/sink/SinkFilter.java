package ai.foresignal.sink;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * Which records a sink receives (prompt 110): {@code sources[]}, {@code kinds[]}, a {@code bbox}, a
 * {@code placeId} or {@code watchlistId} (036) and {@code includePrivate} (039).
 *
 * @param sources source ids to include (empty = all)
 * @param kinds kinds to include (empty = all)
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param placeId a place whose geometry filters records, or null
 * @param watchlistId a watchlist whose members are delivered, or null
 * @param includePrivate include private sources (only for a local target)
 */
public record SinkFilter(List<String> sources, List<String> kinds, double[] bbox, String placeId, String watchlistId,
        boolean includePrivate) {

    /** The empty filter. */
    public static final SinkFilter ALL = new SinkFilter(List.of(), List.of(), null, null, null, false);

    /** Validates and copies. */
    public SinkFilter {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
    }

    /** Parses a {@code SinkFilter} body (null → {@link #ALL}). */
    public static SinkFilter parse(JsonNode node) {
        if (node == null || node.isNull()) {
            return ALL;
        }
        if (!node.isObject()) {
            throw ValidationException.forField("filter", "must be an object");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String field : node.propertyNames()) {
            if (!List.of("sources", "kinds", "bbox", "placeId", "watchlistId", "includePrivate").contains(field)) {
                errors.add(new FieldError("filter." + field, "is not a SinkFilter member"));
            }
        }
        List<String> sources = strings(node.get("sources"), "filter.sources", errors);
        List<String> kinds = strings(node.get("kinds"), "filter.kinds", errors);
        double[] box = box(node.get("bbox"), errors);
        String placeId = text(node.get("placeId"));
        String watchlistId = text(node.get("watchlistId"));
        if (watchlistId != null) {
            try {
                java.util.UUID.fromString(watchlistId);
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("filter.watchlistId", "must be a watchlist id (UUID)"));
            }
        }
        boolean includePrivate = node.has("includePrivate") && node.get("includePrivate").asBoolean(false);
        if (!errors.isEmpty()) {
            throw new ValidationException("The sink filter is not valid.", errors);
        }
        return new SinkFilter(sources, kinds, box, placeId, watchlistId, includePrivate);
    }

    /** The API map. */
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("sources", sources);
        out.put("kinds", kinds);
        out.put("bbox", bbox == null ? null : List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
        out.put("placeId", placeId);
        out.put("watchlistId", watchlistId);
        out.put("includePrivate", includePrivate);
        return out;
    }

    /**
     * True when {@code record} passes every member of the filter: sources, kinds and bbox here, then
     * {@code placeId} and {@code watchlistId} through {@code scope} ({@link SinkScope}).
     */
    public boolean matches(SinkRecord record, SinkScope scope) {
        return matches(record) && (placeId == null && watchlistId == null || scope.matches(this, record));
    }

    /**
     * True when {@code record} passes the source, kind and bbox filters only. Callers that deliver records use
     * {@link #matches(SinkRecord, SinkScope)} so {@code placeId} and {@code watchlistId} are applied too.
     */
    public boolean matches(SinkRecord record) {
        if (!sources.isEmpty() && !sources.contains(record.sourceId())) {
            return false;
        }
        if (!kinds.isEmpty() && !kinds.contains(record.kind())) {
            return false;
        }
        if (bbox != null) {
            Double lon = record.lon();
            Double lat = record.lat();
            if (lon == null || lat == null) {
                return false;
            }
            return lon >= bbox[0] && lon <= bbox[2] && lat >= bbox[1] && lat <= bbox[3];
        }
        return true;
    }

    private static List<String> strings(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isString()) {
                errors.add(new FieldError(field, "must be an array of strings"));
                return List.of();
            }
            out.add(item.asString());
        }
        return out;
    }

    private static double[] box(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.size() != 4) {
            errors.add(new FieldError("filter.bbox", "must be [minLon, minLat, maxLon, maxLat]"));
            return null;
        }
        double[] out = new double[4];
        for (int k = 0; k < 4; k++) {
            out[k] = node.get(k).asDouble();
        }
        return out;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }
}
