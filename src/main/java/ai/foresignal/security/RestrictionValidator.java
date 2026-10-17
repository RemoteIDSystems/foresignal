package ai.foresignal.security;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.source.SourceRegistry;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Parses and validates {@code restrictions} for tokens and pairing codes (OpenAPI
 * {@code TokenRestrictions}, prompt 023).
 *
 * <ul>
 *   <li>{@code endpoints}: each {@code "METHOD /v1/path/pattern"} (Ant style, method may be
 *       {@code *}) must match at least one spec operation that the token's scope can reach;
 *   <li>{@code bbox}: four numbers within ±180/±90 with {@code min < max} on both axes;
 *   <li>{@code sources} and {@code kinds} must exist.
 * </ul>
 *
 * <p>Prompt 035 accepts the {@code ReadRestriction} members it enforces: {@code area} (a GeoJSON
 * Polygon or MultiPolygon, valid, at most 2,000 vertices), {@code excludePrivate} (boolean) and
 * {@code maxRequestsPerMinute} (integer, at least 1). Prompt 036 accepts {@code placeIds} (existing
 * place ids, resolved to their areas through {@code PlaceAreas}). Prompt 113 accepts {@code federation}
 * and {@code includeExercise} (booleans), enforced by the federation feed. {@link #LATER_MEMBERS} names
 * members a later prompt enforces; they are rejected rather than stored, so a restriction is never accepted
 * that nothing enforces.
 *
 * <p>{@link #structural()} validates shape only (no source, kind or endpoint lookups), for settings
 * parsed without the application context.
 */
@Component
public class RestrictionValidator {

    /** {@code METHOD /path}, as the spec's pattern states. */
    static final Pattern ENDPOINT = Pattern.compile("^(\\*|GET|HEAD|POST|PUT|PATCH|DELETE) (/\\S*)$");

    static final Set<String> MEMBERS = Set.of("sources", "kinds", "bbox", "endpoints", "area", "excludePrivate",
            "maxRequestsPerMinute", "placeIds", "federation", "includeExercise");

    /** Members no prompt enforces yet (none since 113). */
    static final Set<String> LATER_MEMBERS = Set.of();

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();

    private final SourceRegistry registry;
    private final SpecOperationIndex spec;

    /** Creates the validator. */
    public RestrictionValidator(SourceRegistry registry, SpecOperationIndex spec) {
        this.registry = registry;
        this.spec = spec;
    }

    /** A validator that checks shape only: no source, kind or endpoint lookups. */
    public static RestrictionValidator structural() {
        return new RestrictionValidator(null, null);
    }

    /**
     * Parses {@code node} for a token of {@code scope}, adding every problem to {@code errors}
     * under {@code field}.
     *
     * @return the restrictions, {@link TokenRestrictions#NONE} for null or absent
     */
    public TokenRestrictions parse(JsonNode node, Scope scope, String field, List<FieldError> errors) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return TokenRestrictions.NONE;
        }
        if (!node.isObject()) {
            errors.add(new FieldError(field, "must be an object"));
            return TokenRestrictions.NONE;
        }
        for (String member : node.propertyNames()) {
            if (LATER_MEMBERS.contains(member)) {
                errors.add(new FieldError(field + "." + member,
                        "is not enforced by this version yet and cannot be set"));
            } else if (!MEMBERS.contains(member)) {
                errors.add(new FieldError(field + "." + member, "is not a restriction member"));
            }
        }
        List<String> sources = strings(node.get("sources"), field + ".sources", errors);
        if (sources != null && registry != null) {
            List<String> unknown = sources.stream().filter(id -> !registry.contains(id)).toList();
            if (!unknown.isEmpty()) {
                errors.add(new FieldError(field + ".sources", "unknown source id(s): " + String.join(", ", unknown)));
            }
        }
        List<String> kinds = strings(node.get("kinds"), field + ".kinds", errors);
        if (kinds != null && registry != null) {
            Set<String> known = registry.kinds();
            List<String> unknown = kinds.stream().filter(k -> !known.contains(k)).toList();
            if (!unknown.isEmpty()) {
                errors.add(new FieldError(field + ".kinds", "unknown kind(s): " + String.join(", ", unknown)));
            }
        }
        List<Double> bbox = bbox(node.get("bbox"), field + ".bbox", errors);
        List<String> endpoints = strings(node.get("endpoints"), field + ".endpoints", errors);
        if (endpoints != null) {
            for (String endpoint : endpoints) {
                var m = ENDPOINT.matcher(endpoint);
                if (!m.matches()) {
                    errors.add(new FieldError(field + ".endpoints",
                            "'" + endpoint + "' must be 'METHOD /v1/path/pattern' (method GET, HEAD, POST, PUT, PATCH, DELETE or *)"));
                } else if (spec != null && !spec.matchesReachable(m.group(1), m.group(2), scope)) {
                    errors.add(new FieldError(field + ".endpoints", "'" + endpoint
                            + "' matches no operation a " + scope.wireValue() + " token can call"));
                }
            }
        }
        Map<String, Object> area = area(node.get("area"), field + ".area", errors);
        Boolean excludePrivate = null;
        JsonNode exclude = node.get("excludePrivate");
        if (exclude != null && !exclude.isNull()) {
            if (exclude.isBoolean()) {
                excludePrivate = exclude.asBoolean();
            } else {
                errors.add(new FieldError(field + ".excludePrivate", "must be a boolean"));
            }
        }
        Integer rpm = null;
        JsonNode limit = node.get("maxRequestsPerMinute");
        if (limit != null && !limit.isNull()) {
            if (limit.isIntegralNumber() && limit.canConvertToInt() && limit.asInt() >= 1) {
                rpm = limit.asInt();
            } else {
                errors.add(new FieldError(field + ".maxRequestsPerMinute", "must be an integer of at least 1"));
            }
        }
        List<String> placeIds = strings(node.get("placeIds"), field + ".placeIds", errors);
        if (placeIds != null) {
            List<String> unknown = placeIds.stream()
                    .filter(id -> Boolean.FALSE.equals(ai.foresignal.security.restrict.PlaceAreas.exists(id)))
                    .toList();
            if (!unknown.isEmpty()) {
                errors.add(new FieldError(field + ".placeIds", "unknown place id(s): " + String.join(", ", unknown)));
            }
        }
        Boolean federation = flag(node.get("federation"), field + ".federation", errors);
        Boolean includeExercise = flag(node.get("includeExercise"), field + ".includeExercise", errors);
        return new TokenRestrictions(sources, kinds, bbox, endpoints, area, excludePrivate, rpm, placeIds,
                federation, includeExercise);
    }

    /** A boolean member (113), null when absent; anything else is a problem. */
    private static Boolean flag(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isBoolean()) {
            errors.add(new FieldError(field, "must be a boolean"));
            return null;
        }
        return node.asBoolean();
    }

    /** A valid GeoJSON Polygon or MultiPolygon of at most 2,000 vertices, as a map; null when absent. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> area(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            errors.add(new FieldError(field, "must be a GeoJSON Polygon or MultiPolygon"));
            return null;
        }
        try {
            org.locationtech.jts.geom.Geometry g = new ai.foresignal.source.formats.geojson.GeoJsonReader(
                    ai.foresignal.security.restrict.ReadRestriction.MAX_AREA_VERTICES, 1).read(node);
            if (!(g instanceof org.locationtech.jts.geom.Polygon)
                    && !(g instanceof org.locationtech.jts.geom.MultiPolygon)) {
                errors.add(new FieldError(field, "must be a GeoJSON Polygon or MultiPolygon"));
                return null;
            }
            if (!g.isValid()) {
                errors.add(new FieldError(field, "is not a valid polygon (self-intersecting or badly nested rings)"));
                return null;
            }
            org.locationtech.jts.geom.Envelope e = g.getEnvelopeInternal();
            if (e.getMinX() < -180 || e.getMaxX() > 180 || e.getMinY() < -90 || e.getMaxY() > 90) {
                errors.add(new FieldError(field, "longitudes must be within ±180 and latitudes within ±90"));
                return null;
            }
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, e.getMessage() == null ? "is not valid GeoJSON" : e.getMessage()));
            return null;
        }
        return JSON.convertValue(node, Map.class);
    }

    private static List<String> strings(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings or null"));
            return null;
        }
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank()) {
                errors.add(new FieldError(field, "must contain only non-blank strings"));
                return null;
            }
            out.add(item.asString().strip());
        }
        return new ArrayList<>(out);
    }

    private static List<Double> bbox(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.size() != 4) {
            errors.add(new FieldError(field, "must be [minLon, minLat, maxLon, maxLat]"));
            return null;
        }
        List<Double> values = new ArrayList<>(4);
        for (JsonNode item : node) {
            if (!item.isNumber() || !Double.isFinite(item.asDouble())) {
                errors.add(new FieldError(field, "must contain four finite numbers"));
                return null;
            }
            values.add(item.asDouble());
        }
        double minLon = values.get(0);
        double minLat = values.get(1);
        double maxLon = values.get(2);
        double maxLat = values.get(3);
        if (Math.abs(minLon) > 180 || Math.abs(maxLon) > 180 || Math.abs(minLat) > 90 || Math.abs(maxLat) > 90) {
            errors.add(new FieldError(field, "longitudes must be within ±180 and latitudes within ±90"));
            return null;
        }
        if (minLon >= maxLon || minLat >= maxLat) {
            errors.add(new FieldError(field, "min must be below max on both axes (a box may not cross the antimeridian)"));
            return null;
        }
        return values;
    }
}
