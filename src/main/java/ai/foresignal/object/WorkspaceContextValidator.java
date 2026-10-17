package ai.foresignal.object;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Ulids;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Validates a workspace context against docs/Client-Contract.md §5 (schema version
 * {@value ObjectTypes#CONTEXT_SCHEMA_VERSION}): well-formed references, at most {@value #MAX_SELECTED} selected refs,
 * an ordered time range, a viewport that is either a bbox or a center with a radius, opaque layers and simple
 * filters validated for shape and size only. Unknown members are rejected.
 */
public final class WorkspaceContextValidator {

    /** Most selected references. */
    public static final int MAX_SELECTED = 200;

    /** Most layers. */
    public static final int MAX_LAYERS = 200;

    /** Largest serialized filters object, in bytes. */
    public static final int MAX_FILTERS_BYTES = 4096;

    /** Largest serialized context, in bytes. */
    public static final int MAX_CONTEXT_BYTES = 16_384;

    /** The context members. */
    public static final Set<String> FIELDS = Set.of("selectedRefs", "focusRef", "timeRange", "asOf", "playback",
            "viewport", "activeSituationId", "activeHypothesisSetId", "filters", "layers");

    private WorkspaceContextValidator() {}

    /**
     * Validates {@code context}.
     *
     * @param context the context object
     * @param prefix the field prefix for errors (e.g. {@code context})
     * @throws ValidationException 400 ({@code OBJECT_REF_INVALID} when only references are wrong)
     */
    public static void validate(JsonNode context, String prefix) {
        List<FieldError> errors = new ArrayList<>();
        List<FieldError> refErrors = new ArrayList<>();
        if (context == null || context.isNull()) {
            return;
        }
        if (!context.isObject()) {
            throw ValidationException.forField(prefix, "must be an object");
        }
        String p = prefix + ".";
        for (String name : context.propertyNames()) {
            if (!FIELDS.contains(name)) {
                errors.add(new FieldError(p + name, "is not a workspace context member"));
            }
        }
        JsonNode selected = context.get("selectedRefs");
        if (selected != null && !selected.isNull()) {
            if (!selected.isArray()) {
                errors.add(new FieldError(p + "selectedRefs", "must be an array of references"));
            } else {
                if (selected.size() > MAX_SELECTED) {
                    errors.add(new FieldError(p + "selectedRefs", "holds more than " + MAX_SELECTED + " references"));
                }
                int i = 0;
                for (JsonNode r : selected) {
                    if (!r.isString() || !ObjectRef.isValid(r.asString())) {
                        refErrors.add(new FieldError(p + "selectedRefs[" + i + "]", "is not a valid object reference"));
                    }
                    i++;
                }
            }
        }
        JsonNode focus = context.get("focusRef");
        if (focus != null && !focus.isNull() && (!focus.isString() || !ObjectRef.isValid(focus.asString()))) {
            refErrors.add(new FieldError(p + "focusRef", "is not a valid object reference"));
        }
        timeRange(context.get("timeRange"), p + "timeRange", errors);
        JsonNode asOf = context.get("asOf");
        if (asOf != null && !asOf.isNull() && parse(asOf) == null) {
            errors.add(new FieldError(p + "asOf", "must be an RFC 3339 date-time"));
        }
        playback(context.get("playback"), p + "playback", errors);
        viewport(context.get("viewport"), p + "viewport", errors);
        for (String id : List.of("activeSituationId", "activeHypothesisSetId")) {
            JsonNode n = context.get(id);
            if (n != null && !n.isNull() && (!n.isString() || !Ulids.isUlid(n.asString()))) {
                errors.add(new FieldError(p + id, "must be a ULID or null"));
            }
        }
        JsonNode filters = context.get("filters");
        if (filters != null && !filters.isNull()) {
            if (!filters.isObject()) {
                errors.add(new FieldError(p + "filters", "must be an object"));
            } else {
                for (String name : filters.propertyNames()) {
                    if (!simple(filters.get(name))) {
                        errors.add(new FieldError(p + "filters." + name,
                                "must be a string, number, boolean, null or an array of those"));
                    }
                }
                if (filters.toString().getBytes(StandardCharsets.UTF_8).length > MAX_FILTERS_BYTES) {
                    errors.add(new FieldError(p + "filters", "is larger than " + MAX_FILTERS_BYTES + " bytes"));
                }
            }
        }
        JsonNode layers = context.get("layers");
        if (layers != null && !layers.isNull()) {
            if (!layers.isArray() || layers.size() > MAX_LAYERS) {
                errors.add(new FieldError(p + "layers", "must be an array of at most " + MAX_LAYERS + " strings"));
            } else {
                for (JsonNode l : layers) {
                    if (!l.isString() || l.asString().isBlank() || l.asString().length() > 128) {
                        errors.add(new FieldError(p + "layers", "each layer is a string of 1 to 128 characters"));
                        break;
                    }
                }
            }
        }
        if (context.toString().getBytes(StandardCharsets.UTF_8).length > MAX_CONTEXT_BYTES) {
            errors.add(new FieldError(prefix, "is larger than " + MAX_CONTEXT_BYTES + " bytes"));
        }
        if (!errors.isEmpty()) {
            errors.addAll(refErrors);
            throw new ValidationException("The workspace context is not valid.", errors);
        }
        if (!refErrors.isEmpty()) {
            throw new ValidationException(ai.foresignal.api.error.ErrorCodes.OBJECT_REF_INVALID,
                    "The workspace context holds invalid object references.", refErrors);
        }
    }

    private static void timeRange(JsonNode range, String field, List<FieldError> errors) {
        if (range == null || range.isNull()) {
            return;
        }
        if (!range.isObject()) {
            errors.add(new FieldError(field, "must be an object {start, end}"));
            return;
        }
        Instant start = parse(range.get("start"));
        if (start == null) {
            errors.add(new FieldError(field + ".start", "is required (an RFC 3339 date-time)"));
        }
        JsonNode endNode = range.get("end");
        if (endNode != null && !endNode.isNull()) {
            Instant end = parse(endNode);
            if (end == null) {
                errors.add(new FieldError(field + ".end", "must be an RFC 3339 date-time or null"));
            } else if (start != null && end.isBefore(start)) {
                errors.add(new FieldError(field + ".end", "must not be before start"));
            }
        }
        for (String name : range.propertyNames()) {
            if (!name.equals("start") && !name.equals("end")) {
                errors.add(new FieldError(field + "." + name, "is not a time range member"));
            }
        }
    }

    private static void playback(JsonNode playback, String field, List<FieldError> errors) {
        if (playback == null || playback.isNull()) {
            return;
        }
        if (!playback.isObject()) {
            errors.add(new FieldError(field, "must be an object {state, rate}"));
            return;
        }
        JsonNode state = playback.get("state");
        if (state == null || !state.isString() || !List.of("playing", "paused").contains(state.asString())) {
            errors.add(new FieldError(field + ".state", "must be playing or paused"));
        }
        JsonNode rate = playback.get("rate");
        if (rate != null && !rate.isNull() && (!rate.isNumber() || rate.asDouble() <= 0)) {
            errors.add(new FieldError(field + ".rate", "must be a number above 0"));
        }
    }

    private static void viewport(JsonNode viewport, String field, List<FieldError> errors) {
        if (viewport == null || viewport.isNull()) {
            return;
        }
        if (!viewport.isObject()) {
            errors.add(new FieldError(field, "must be an object"));
            return;
        }
        boolean hasBbox = viewport.hasNonNull("bbox");
        boolean hasCenter = viewport.hasNonNull("center") || viewport.hasNonNull("radiusKm");
        if (hasBbox == hasCenter) {
            errors.add(new FieldError(field, "must be either bbox or center with radiusKm"));
            return;
        }
        if (hasBbox) {
            JsonNode b = viewport.get("bbox");
            if (!b.isArray() || b.size() != 4 || !allNumbers(b)) {
                errors.add(new FieldError(field + ".bbox", "must be [minLon, minLat, maxLon, maxLat]"));
            } else if (!lon(b.get(0)) || !lon(b.get(2)) || !lat(b.get(1)) || !lat(b.get(3))
                    || b.get(1).asDouble() >= b.get(3).asDouble()) {
                errors.add(new FieldError(field + ".bbox", "needs longitudes in [-180, 180], latitudes in [-90, 90]"
                        + " and minLat below maxLat"));
            }
        } else {
            JsonNode c = viewport.get("center");
            if (c == null || !c.isArray() || c.size() != 2 || !allNumbers(c) || !lon(c.get(0)) || !lat(c.get(1))) {
                errors.add(new FieldError(field + ".center", "must be [lon, lat]"));
            }
            JsonNode r = viewport.get("radiusKm");
            if (r == null || !r.isNumber() || r.asDouble() <= 0 || r.asDouble() > 20_000) {
                errors.add(new FieldError(field + ".radiusKm", "must be a number in (0, 20000]"));
            }
        }
        for (String name : viewport.propertyNames()) {
            if (!Set.of("bbox", "center", "radiusKm").contains(name)) {
                errors.add(new FieldError(field + "." + name, "is not a viewport member (camera details belong in"
                        + " layout)"));
            }
        }
    }

    private static boolean simple(JsonNode n) {
        if (n == null || n.isNull() || n.isString() || n.isNumber() || n.isBoolean()) {
            return true;
        }
        if (n.isArray()) {
            for (JsonNode e : n) {
                if (!(e.isNull() || e.isString() || e.isNumber() || e.isBoolean())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean allNumbers(JsonNode array) {
        for (JsonNode n : array) {
            if (!n.isNumber()) {
                return false;
            }
        }
        return true;
    }

    private static boolean lon(JsonNode n) {
        return n.asDouble() >= -180 && n.asDouble() <= 180;
    }

    private static boolean lat(JsonNode n) {
        return n.asDouble() >= -90 && n.asDouble() <= 90;
    }

    /** The instant in a string node, or null. */
    static Instant parse(JsonNode n) {
        if (n == null || !n.isString()) {
            return null;
        }
        try {
            return Instant.parse(n.asString());
        } catch (RuntimeException e) {
            try {
                return java.time.OffsetDateTime.parse(n.asString()).toInstant();
            } catch (RuntimeException e2) {
                return null;
            }
        }
    }
}
