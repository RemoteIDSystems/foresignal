package ai.foresignal.outlook.index;

import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * The request bodies of the regional index operations (prompt 126), parsed and validated without a database:
 * {@code RegionalIndexDefInput} ({@link #definition}) and {@code IndexWeightsInput} ({@link #weights}). Every problem
 * is a 400 {@code INDEX_INVALID} with field errors.
 */
public final class IndexInputs {

    /** An index id. */
    public static final Pattern ID = Pattern.compile("^[a-z][a-z0-9-]{0,63}$");

    /** A component key. */
    public static final Pattern KEY = Pattern.compile("^[a-z][a-z0-9_-]{0,31}$");

    /** A {@code finding:{type}:{family}} target. */
    public static final Pattern FINDING_TARGET = Pattern.compile("^finding:[a-z][a-z-]{0,40}:[a-z][a-z0-9-]{0,40}$");

    /** The transforms. */
    public static final List<String> TRANSFORMS = List.of("log1p", "zscore", "pctrank", "level");

    /** The windows. */
    public static final Map<String, Integer> WINDOWS = Map.of("P7D", 7, "P28D", 28, "P90D", 90);

    /** The weights sources. */
    public static final List<String> WEIGHT_SOURCES = List.of("fitted", "operator", "default");

    /** The most components. */
    public static final int MAX_COMPONENTS = 20;

    /** The most floor rules. */
    public static final int MAX_FLOORS = 10;

    /** The tolerance of the weights' sum. */
    public static final double SUM_TOLERANCE = 1e-6;

    private static final Set<String> DEF_FIELDS = Set.of("name", "description", "components", "weights", "floors",
            "target", "enabled", "autoApplyFittedWeights");

    private static final Set<String> WEIGHT_FIELDS = Set.of("source", "version", "weights", "rationale");

    private IndexInputs() {}

    /**
     * A validated definition.
     *
     * @param name the name
     * @param description the description, or null
     * @param components the components ({@code key, ref, transform, window, direction, floor?})
     * @param weights the weights
     * @param floors the floor rules ({@code id, description, condition, minValue})
     * @param target the target
     * @param enabled whether it is computed
     * @param autoApplyFittedWeights whether fitted weights apply automatically
     */
    public record Definition(String name, String description, List<Map<String, Object>> components,
            Map<String, Double> weights, List<Map<String, Object>> floors, String target, boolean enabled,
            boolean autoApplyFittedWeights) {}

    /**
     * A validated weights change.
     *
     * @param source {@code fitted}, {@code operator} or {@code default}
     * @param version the fitted weights version (fitted only), or null
     * @param weights the operator weights (operator only), or null
     * @param rationale why, or null
     */
    public record Weights(String source, Integer version, Map<String, Double> weights, String rationale) {}

    /** Validates an index id. */
    public static String id(String id) {
        if (id == null || !ID.matcher(id).matches()) {
            throw invalid(List.of(new FieldError("indexId", "must match " + ID.pattern())));
        }
        return id;
    }

    /** Parses a {@code RegionalIndexDefInput}. */
    public static Definition definition(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || !body.isObject()) {
            throw invalid(List.of(new FieldError("body", "must be a RegionalIndexDefInput object")));
        }
        for (String field : body.propertyNames()) {
            if (!DEF_FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not a RegionalIndexDefInput member"));
            }
        }
        String name = text(body, "name", 200, true, errors);
        String description = text(body, "description", 4_000, false, errors);
        List<Map<String, Object>> components = components(body.get("components"), errors);
        Set<String> keys = new LinkedHashSet<>();
        for (Map<String, Object> c : components) {
            keys.add(c.get("key").toString());
        }
        Map<String, Double> weights = weightMap(body.get("weights"), keys, "weights", errors);
        List<Map<String, Object>> floors = floors(body.get("floors"), keys, errors);
        String target = text(body, "target", 128, true, errors);
        if (target != null && !ReferenceFamilies.FAMILY.matcher(target).matches()
                && !FINDING_TARGET.matcher(target).matches()) {
            errors.add(new FieldError("target", "must be an outlook family (conflict:battles) or"
                    + " finding:{findingType}:{signalFamily}"));
        }
        Boolean enabled = bool(body, "enabled", true, errors);
        Boolean auto = bool(body, "autoApplyFittedWeights", false, errors);
        if (!errors.isEmpty()) {
            throw invalid(errors);
        }
        return new Definition(name, description, components, weights, floors, target, Boolean.TRUE.equals(enabled),
                Boolean.TRUE.equals(auto));
    }

    /** Parses an {@code IndexWeightsInput} against the definition's component keys. */
    public static Weights weights(JsonNode body, Set<String> keys) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || !body.isObject()) {
            throw invalid(List.of(new FieldError("body", "must be an IndexWeightsInput object")));
        }
        for (String field : body.propertyNames()) {
            if (!WEIGHT_FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not an IndexWeightsInput member"));
            }
        }
        String source = text(body, "source", 16, true, errors);
        if (source != null && !WEIGHT_SOURCES.contains(source)) {
            errors.add(new FieldError("source", "must be one of " + WEIGHT_SOURCES));
        }
        Integer version = null;
        if (body.hasNonNull("version")) {
            if (!body.get("version").isIntegralNumber() || body.get("version").asInt() < 1) {
                errors.add(new FieldError("version", "must be a positive integer"));
            } else {
                version = body.get("version").asInt();
            }
        }
        Map<String, Double> weights = null;
        if ("fitted".equals(source) && version == null) {
            errors.add(new FieldError("version", "is required for fitted weights"));
        }
        if ("operator".equals(source)) {
            if (!body.hasNonNull("weights")) {
                errors.add(new FieldError("weights", "is required for operator weights"));
            } else {
                weights = weightMap(body.get("weights"), keys, "weights", errors);
            }
        } else if (body.hasNonNull("weights")) {
            errors.add(new FieldError("weights", "is only accepted with source operator"));
        }
        String rationale = text(body, "rationale", 4_000, false, errors);
        if (!errors.isEmpty()) {
            throw invalid(errors);
        }
        return new Weights(source, version, weights, rationale);
    }

    /** The components of a stored definition as computing records. */
    public static List<IndexComputer.Component> toComponents(List<Map<String, Object>> components) {
        List<IndexComputer.Component> out = new ArrayList<>();
        for (Map<String, Object> c : components) {
            Object floor = c.get("floor");
            out.add(new IndexComputer.Component(c.get("key").toString(), c.get("ref").toString(),
                    String.valueOf(c.getOrDefault("transform", "pctrank")),
                    WINDOWS.getOrDefault(String.valueOf(c.getOrDefault("window", "P28D")), 28),
                    c.get("direction") instanceof Number d && d.intValue() < 0 ? -1 : 1,
                    floor instanceof Number f ? f.doubleValue() : null));
        }
        return out;
    }

    /** The floors of a stored definition as computing records. */
    @SuppressWarnings("unchecked")
    public static List<IndexComputer.Floor> toFloors(List<Map<String, Object>> floors) {
        List<IndexComputer.Floor> out = new ArrayList<>();
        int i = 0;
        for (Map<String, Object> f : floors) {
            i++;
            Object min = f.get("minValue");
            out.add(new IndexComputer.Floor(f.get("id") == null ? "floor-" + i : f.get("id").toString(),
                    String.valueOf(f.getOrDefault("description", "")), f.get("condition") instanceof Map<?, ?> m
                            ? (Map<String, Object>) m : Map.of(), min instanceof Number n ? n.doubleValue() : 0));
        }
        return out;
    }

    // ------------------------------------------------------------------------- members

    private static List<Map<String, Object>> components(JsonNode node, List<FieldError> errors) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (node == null || !node.isArray() || node.isEmpty()) {
            errors.add(new FieldError("components", "must be a non-empty array"));
            return out;
        }
        if (node.size() > MAX_COMPONENTS) {
            errors.add(new FieldError("components", "holds at most " + MAX_COMPONENTS + " components"));
        }
        Set<String> keys = new LinkedHashSet<>();
        int i = 0;
        for (JsonNode c : node) {
            String path = "components[" + i++ + "]";
            if (!c.isObject()) {
                errors.add(new FieldError(path, "must be an object"));
                continue;
            }
            for (String field : c.propertyNames()) {
                if (!Set.of("key", "ref", "transform", "window", "direction", "floor").contains(field)) {
                    errors.add(new FieldError(path + "." + field, "is not a component member"));
                }
            }
            String key = c.hasNonNull("key") ? c.get("key").asString() : null;
            if (key == null || !KEY.matcher(key).matches()) {
                errors.add(new FieldError(path + ".key", "must match " + KEY.pattern()));
                continue;
            }
            if (!keys.add(key)) {
                errors.add(new FieldError(path + ".key", "'" + key + "' is repeated"));
            }
            String ref = c.hasNonNull("ref") ? c.get("ref").asString() : null;
            if (ref == null || !ref.contains("{region}")) {
                errors.add(new FieldError(path + ".ref", "must be a series reference template with {region}"));
            } else {
                try {
                    SeriesRef.parse(ref.replace("{region}", "XX"));
                } catch (ValidationException e) {
                    errors.add(new FieldError(path + ".ref", "is not a series reference once {region} is filled: "
                            + e.getMessage()));
                }
            }
            String transform = c.hasNonNull("transform") ? c.get("transform").asString() : null;
            if (transform == null || !TRANSFORMS.contains(transform)) {
                errors.add(new FieldError(path + ".transform", "must be one of " + TRANSFORMS));
            }
            String window = c.hasNonNull("window") ? c.get("window").asString() : null;
            if (window == null || !WINDOWS.containsKey(window)) {
                errors.add(new FieldError(path + ".window", "must be one of P7D, P28D or P90D"));
            }
            JsonNode direction = c.get("direction");
            if (direction == null || !direction.isIntegralNumber() || Math.abs(direction.asInt()) != 1) {
                errors.add(new FieldError(path + ".direction", "must be 1 or -1"));
            }
            Double floor = null;
            if (c.hasNonNull("floor")) {
                if (!c.get("floor").isNumber() || c.get("floor").asDouble() < 0 || c.get("floor").asDouble() > 100) {
                    errors.add(new FieldError(path + ".floor", "must be a number from 0 to 100"));
                } else {
                    floor = c.get("floor").asDouble();
                }
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", key);
            m.put("ref", ref);
            m.put("transform", transform);
            m.put("window", window);
            m.put("direction", direction == null || !direction.isIntegralNumber() ? 1 : direction.asInt());
            m.put("floor", floor);
            out.add(m);
        }
        return out;
    }

    private static Map<String, Double> weightMap(JsonNode node, Set<String> keys, String path,
            List<FieldError> errors) {
        Map<String, Double> out = new LinkedHashMap<>();
        if (node == null || !node.isObject()) {
            errors.add(new FieldError(path, "must be an object of component key to weight"));
            return out;
        }
        double sum = 0;
        for (String key : node.propertyNames()) {
            JsonNode v = node.get(key);
            if (!keys.isEmpty() && !keys.contains(key)) {
                errors.add(new FieldError(path + "." + key, "is not a component key"));
                continue;
            }
            if (!v.isNumber() || v.asDouble() < 0) {
                errors.add(new FieldError(path + "." + key, "must be a non-negative number"));
                continue;
            }
            out.put(key, v.asDouble());
            sum += v.asDouble();
        }
        for (String key : keys) {
            if (!out.containsKey(key)) {
                errors.add(new FieldError(path + "." + key, "is missing"));
            }
        }
        if (Math.abs(sum - 1) > SUM_TOLERANCE) {
            errors.add(new FieldError(path, "must sum to 1 (they sum to " + sum + ")"));
        }
        return out;
    }

    private static List<Map<String, Object>> floors(JsonNode node, Set<String> keys, List<FieldError> errors) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > MAX_FLOORS) {
            errors.add(new FieldError("floors", "must be an array of at most " + MAX_FLOORS + " rules"));
            return out;
        }
        int i = 0;
        for (JsonNode f : node) {
            String path = "floors[" + i++ + "]";
            if (!f.isObject()) {
                errors.add(new FieldError(path, "must be an object"));
                continue;
            }
            String description = f.hasNonNull("description") ? f.get("description").asString() : null;
            if (description == null || description.isBlank()) {
                errors.add(new FieldError(path + ".description", "is required"));
            }
            JsonNode min = f.get("minValue");
            if (min == null || !min.isNumber() || min.asDouble() < 0 || min.asDouble() > 100) {
                errors.add(new FieldError(path + ".minValue", "must be a number from 0 to 100"));
            }
            Map<String, Object> condition = new LinkedHashMap<>();
            JsonNode c = f.get("condition");
            if (c == null || !c.isObject()) {
                errors.add(new FieldError(path + ".condition", "must be an object"));
            } else {
                String type = c.hasNonNull("type") ? c.get("type").asString() : null;
                if ("component".equals(type)) {
                    String key = c.hasNonNull("key") ? c.get("key").asString() : null;
                    if (key == null || !keys.contains(key)) {
                        errors.add(new FieldError(path + ".condition.key", "must be a component key"));
                    }
                    if (!c.hasNonNull("gte") || !c.get("gte").isNumber()) {
                        errors.add(new FieldError(path + ".condition.gte", "must be a number"));
                    }
                    condition.put("type", type);
                    condition.put("key", key);
                    condition.put("gte", c.hasNonNull("gte") ? c.get("gte").asDouble() : null);
                } else if ("reference".equals(type)) {
                    String family = c.hasNonNull("family") ? c.get("family").asString() : null;
                    if (family == null || !ReferenceFamilies.FAMILY.matcher(family).matches()) {
                        errors.add(new FieldError(path + ".condition.family", "must be a reference family"));
                    }
                    if (!c.hasNonNull("minEvents") && !c.hasNonNull("minFatalities")) {
                        errors.add(new FieldError(path + ".condition", "needs minEvents or minFatalities"));
                    }
                    int within = c.hasNonNull("withinDays") ? c.get("withinDays").asInt() : 365;
                    if (within < 1 || within > 3_650) {
                        errors.add(new FieldError(path + ".condition.withinDays", "must be 1 to 3650"));
                    }
                    condition.put("type", type);
                    condition.put("family", family);
                    condition.put("source", c.hasNonNull("source") ? c.get("source").asString() : null);
                    condition.put("minEvents", c.hasNonNull("minEvents") ? c.get("minEvents").asDouble() : null);
                    condition.put("minFatalities", c.hasNonNull("minFatalities") ? c.get("minFatalities").asDouble()
                            : null);
                    condition.put("withinDays", within);
                } else {
                    errors.add(new FieldError(path + ".condition.type", "must be component or reference"));
                }
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.hasNonNull("id") ? f.get("id").asString() : "floor-" + i);
            m.put("description", description);
            m.put("condition", condition);
            m.put("minValue", min != null && min.isNumber() ? min.asDouble() : 0);
            out.add(m);
        }
        return out;
    }

    private static String text(JsonNode body, String field, int max, boolean required, List<FieldError> errors) {
        JsonNode v = body.get(field);
        if (v == null || v.isNull()) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return null;
        }
        if (!v.isString() || v.asString().isBlank() || v.asString().length() > max) {
            errors.add(new FieldError(field, "must be a non-blank string of at most " + max + " characters"));
            return null;
        }
        return v.asString();
    }

    private static Boolean bool(JsonNode body, String field, boolean required, List<FieldError> errors) {
        JsonNode v = body.get(field);
        if (v == null || v.isNull()) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return null;
        }
        if (!v.isBoolean()) {
            errors.add(new FieldError(field, "must be a boolean"));
            return null;
        }
        return v.asBoolean();
    }

    /** A 400 {@code INDEX_INVALID}. */
    public static ValidationException invalid(List<FieldError> errors) {
        return new ValidationException(ErrorCodes.INDEX_INVALID, "The regional index is not valid.", errors);
    }
}
