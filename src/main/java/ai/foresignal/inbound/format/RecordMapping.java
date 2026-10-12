package ai.foresignal.inbound.format;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

/**
 * The minimal declarative mapping of the {@code mapping} ingest format (prompt 024), stored per
 * custom source under {@code formats.mapping}. Every path is a JSON Pointer (RFC 6901). Prompt 059
 * extends it with units, previews, LLM drafts and presets.
 *
 * @param recordsPath pointer to the array of records; empty for the root (a root object is one
 *     record)
 * @param externalId pointer to the external id within a record; required
 * @param kind pointer to the kind, or null when the source has a single kind
 * @param observedAt pointer to the observation time, or null
 * @param observedAtFormat {@code ISO}, {@code epoch-s} or {@code epoch-ms}
 * @param lat pointer to the latitude, or null
 * @param lon pointer to the longitude, or null
 * @param geometry pointer to a GeoJSON geometry, or null
 * @param payload the payload fields to copy
 */
public record RecordMapping(
        String recordsPath,
        String externalId,
        String kind,
        String observedAt,
        String observedAtFormat,
        String lat,
        String lon,
        String geometry,
        List<Field> payload) {

    /** The accepted {@code observedAtFormat} values. */
    public static final Set<String> TIME_FORMATS = Set.of("ISO", "epoch-s", "epoch-ms");

    private static final Pattern PAYLOAD_KEY = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    /** Copies the field list and fills defaults. */
    public RecordMapping {
        recordsPath = recordsPath == null ? "" : recordsPath;
        observedAtFormat = observedAtFormat == null ? "ISO" : observedAtFormat;
        payload = payload == null ? List.of() : List.copyOf(payload);
    }

    /**
     * One payload field.
     *
     * @param from pointer within the record
     * @param to the top-level payload key
     */
    public record Field(String from, String to) {}

    /**
     * Reads and validates a mapping.
     *
     * @throws ValidationException listing every problem, with fields under {@code formats.mapping}
     */
    public static RecordMapping parse(JsonNode node) {
        List<FieldError> errors = new ArrayList<>();
        if (node == null || !node.isObject()) {
            throw new ValidationException("The mapping is not valid.",
                    List.of(new FieldError("formats.mapping", "must be an object")));
        }
        String recordsPath = pointer(node, "recordsPath", false, errors);
        String externalId = pointer(node, "externalId", true, errors);
        String kind = pointer(node, "kind", false, errors);
        String observedAt = pointer(node, "observedAt", false, errors);
        String format = text(node.get("observedAtFormat"));
        if (format != null && !TIME_FORMATS.contains(format)) {
            errors.add(new FieldError("formats.mapping.observedAtFormat", "must be ISO, epoch-s or epoch-ms"));
        }
        String lat = pointer(node, "lat", false, errors);
        String lon = pointer(node, "lon", false, errors);
        String geometry = pointer(node, "geometry", false, errors);
        if ((lat == null) != (lon == null)) {
            errors.add(new FieldError("formats.mapping.lat", "lat and lon must be set together"));
        }
        if (geometry != null && lat != null) {
            errors.add(new FieldError("formats.mapping.geometry", "use either geometry or lat/lon, not both"));
        }
        List<Field> fields = new ArrayList<>();
        JsonNode payload = node.get("payload");
        if (payload != null && !payload.isNull()) {
            if (!payload.isArray()) {
                errors.add(new FieldError("formats.mapping.payload", "must be an array of {from, to}"));
            } else {
                int i = 0;
                for (JsonNode entry : payload) {
                    String where = "formats.mapping.payload[" + i++ + "]";
                    String from = text(entry.get("from"));
                    String to = text(entry.get("to"));
                    if (from == null || !validPointer(from)) {
                        errors.add(new FieldError(where + ".from", "must be a JSON Pointer"));
                    }
                    if (to == null || !PAYLOAD_KEY.matcher(to).matches()) {
                        errors.add(new FieldError(where + ".to", "must match ^[A-Za-z0-9_]{1,64}$"));
                    }
                    fields.add(new Field(from, to));
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The mapping is not valid.", errors);
        }
        return new RecordMapping(recordsPath, externalId, kind, observedAt, format, lat, lon, geometry, fields);
    }

    private static String pointer(JsonNode node, String name, boolean required, List<FieldError> errors) {
        String value = text(node.get(name));
        if (value == null) {
            if (required) {
                errors.add(new FieldError("formats.mapping." + name, "is required"));
            }
            return null;
        }
        if (!validPointer(value)) {
            errors.add(new FieldError("formats.mapping." + name, "must be a JSON Pointer such as /id"));
            return null;
        }
        return value;
    }

    /** True when {@code value} is a syntactically valid JSON Pointer. */
    static boolean validPointer(String value) {
        if (value == null) {
            return false;
        }
        if (value.isEmpty()) {
            return true;
        }
        try {
            JsonPointer.compile(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String text(JsonNode value) {
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }
}
