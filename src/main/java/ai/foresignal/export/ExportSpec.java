package ai.foresignal.export;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * A bulk export request (prompt 111; OpenAPI {@code ExportSpec}).
 *
 * @param name a display name, or null
 * @param format the format
 * @param sources source ids (empty = all permitted)
 * @param kinds kinds (empty = all)
 * @param bbox {@code [minLon, minLat, maxLon, maxLat]}, or null
 * @param placeId a place whose geometry bounds the export, or null
 * @param from the lower time bound, or null
 * @param to the upper time bound, or null
 * @param timeField effective|observed|event|ingested
 * @param mode current|history|asOf|atSeq
 * @param asOf the asOf instant, or null
 * @param atSeq the atSeq, or null
 * @param fields a payload allowlist (empty = all)
 * @param flatten promote fieldSpecs keys to columns
 * @param includePrivate include private sources (admin only)
 * @param personalUse include FORBIDDEN/LOCAL_ONLY records
 */
public record ExportSpec(String name, ExportFormat format, List<String> sources, List<String> kinds, double[] bbox,
        String placeId, Instant from, Instant to, String timeField, String mode, Instant asOf, Long atSeq,
        List<String> fields, boolean flatten, boolean includePrivate, boolean personalUse) {

    /** The export modes. */
    public static final List<String> MODES = List.of("current", "history", "asOf", "atSeq");

    /** The time axes. */
    public static final List<String> TIME_FIELDS = List.of("effective", "observed", "event", "ingested");

    /** Validates and copies. */
    public ExportSpec {
        sources = sources == null ? List.of() : List.copyOf(sources);
        kinds = kinds == null ? List.of() : List.copyOf(kinds);
        fields = fields == null ? List.of() : List.copyOf(fields);
        format = format == null ? ExportFormat.GEOPARQUET : format;
        mode = mode == null || mode.isBlank() ? "current" : mode;
        timeField = timeField == null || timeField.isBlank() ? "effective" : timeField;
    }

    /** Parses an {@code ExportSpec} body. */
    public static ExportSpec parse(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an ExportSpec object.");
        }
        List<FieldError> errors = new ArrayList<>();
        String name = text(body.get("name"));
        ExportFormat format;
        try {
            format = ExportFormat.parse(text(body.get("format")));
        } catch (ValidationException e) {
            errors.addAll(e.getErrors());
            format = ExportFormat.GEOPARQUET;
        }
        List<String> sources = strings(body.get("sources"), "sources", errors);
        List<String> kinds = strings(body.get("kinds"), "kinds", errors);
        double[] bbox = box(body.get("bbox"), errors);
        String placeId = text(body.get("placeId"));
        Instant from = instant(body.get("from"), "from", errors);
        Instant to = instant(body.get("to"), "to", errors);
        String timeField = oneOf(body.get("timeField"), "timeField", TIME_FIELDS, errors, "effective");
        String mode = oneOf(body.get("mode"), "mode", MODES, errors, "current");
        Instant asOf = instant(body.get("asOf"), "asOf", errors);
        Long atSeq = body.get("atSeq") == null || body.get("atSeq").isNull() ? null : body.get("atSeq").asLong();
        List<String> fields = strings(body.get("fields"), "fields", errors);
        boolean flatten = body.has("flatten") && body.get("flatten").asBoolean(false);
        boolean includePrivate = body.has("includePrivate") && body.get("includePrivate").asBoolean(false);
        boolean personalUse = body.has("personalUse") && body.get("personalUse").asBoolean(false);
        if (format == ExportFormat.GPX && !"history".equals(mode)) {
            errors.add(new FieldError("format", "gpx requires mode=history"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The export spec is not valid.", errors);
        }
        return new ExportSpec(name, format, sources, kinds, bbox, placeId, from, to, timeField, mode, asOf, atSeq,
                fields, flatten, includePrivate, personalUse);
    }

    /** A copy with the bounding box replaced (the resolved {@code placeId}). */
    public ExportSpec withBbox(double[] newBbox) {
        return new ExportSpec(name, format, sources, kinds, newBbox, placeId, from, to, timeField, mode, asOf, atSeq,
                fields, flatten, includePrivate, personalUse);
    }

    /** The stored spec map. */
    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("name", name);
        out.put("format", format.wire());
        out.put("sources", sources);
        out.put("kinds", kinds);
        out.put("bbox", bbox == null ? null : List.of(bbox[0], bbox[1], bbox[2], bbox[3]));
        out.put("placeId", placeId);
        out.put("from", from == null ? null : from.toString());
        out.put("to", to == null ? null : to.toString());
        out.put("timeField", timeField);
        out.put("mode", mode);
        out.put("asOf", asOf == null ? null : asOf.toString());
        out.put("atSeq", atSeq);
        out.put("fields", fields);
        out.put("flatten", flatten);
        out.put("includePrivate", includePrivate);
        out.put("personalUse", personalUse);
        return out;
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
            errors.add(new FieldError("bbox", "must be [minLon, minLat, maxLon, maxLat]"));
            return null;
        }
        double[] out = new double[4];
        for (int k = 0; k < 4; k++) {
            out[k] = node.get(k).asDouble();
        }
        return out;
    }

    private static Instant instant(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            return Instant.parse(node.asString());
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, "must be an ISO-8601 date-time"));
            return null;
        }
    }

    private static String oneOf(JsonNode node, String field, List<String> allowed, List<FieldError> errors,
            String fallback) {
        if (node == null || node.isNull()) {
            return fallback;
        }
        String value = node.asString().toLowerCase(Locale.ROOT);
        if (!allowed.contains(value)) {
            errors.add(new FieldError(field, "must be one of " + allowed));
            return fallback;
        }
        return value;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asString();
    }
}
