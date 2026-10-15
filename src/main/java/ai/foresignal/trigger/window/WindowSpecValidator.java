package ai.foresignal.trigger.window;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.trigger.window.WindowSpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Validates the {@code type} and {@code window} of a trigger rule input (prompt 100). A windowed
 * rule must carry a valid {@code window}, and must not set the single-version options {@code onlyNew},
 * a non-{@code match} {@code mode}, or {@code reappearAsEnter}: any of those is 400
 * {@code WINDOW_RULE_INVALID}.
 */
public final class WindowSpecValidator {

    /** The group-by forms other than a payload path. */
    static final Set<String> GROUP_SPECIAL = Set.of("source", "place", "admin1", "country");

    private WindowSpecValidator() {}

    /**
     * Validates the rule body and returns the parsed window spec (null for a {@code record} rule).
     *
     * @param body the rule input JSON
     * @throws ValidationException {@code WINDOW_RULE_INVALID} when the type or window is invalid
     */
    public static RuleType type(JsonNode body) {
        JsonNode type = body.get("type");
        if (type == null || type.isNull()) {
            return RuleType.RECORD;
        }
        if (!type.isString()) {
            throw invalid("type", "must be a string");
        }
        try {
            return RuleType.fromWire(type.asString());
        } catch (IllegalArgumentException e) {
            throw invalid("type", e.getMessage());
        }
    }

    /**
     * Validates the rule body and returns the parsed window spec (null for a {@code record} rule).
     *
     * @throws ValidationException {@code WINDOW_RULE_INVALID} when the type or window is invalid
     */
    public static WindowSpec validate(RuleType ruleType, JsonNode body) {
        if (ruleType == RuleType.RECORD) {
            return null;
        }
        rejectSingleVersionOptions(body);
        JsonNode window = body.get("window");
        if (window == null || window.isNull()) {
            throw invalid("window", "is required for a " + ruleType.wire() + " rule");
        }
        if (!window.isObject()) {
            throw invalid("window", "must be an object");
        }
        List<FieldError> errors = new ArrayList<>();
        Duration windowLength = duration(window, "window", errors, true);
        if (windowLength != null && (windowLength.compareTo(Duration.ofMinutes(1)) < 0
                || windowLength.compareTo(WindowSpec.MAX_WINDOW) > 0)) {
            errors.add(new FieldError("window.window", "must be between 1 minute and 7 days"));
        }
        Duration step = duration(window, "step", errors, false);
        if (step != null && step.compareTo(WindowSpec.MIN_STEP) < 0) {
            errors.add(new FieldError("window.step", "must be at least 30 seconds"));
        }
        String distinctBy = text(window, "distinctBy", "entity");
        String groupBy = text(window, "groupBy", null);
        if (groupBy != null && !validGroupBy(groupBy)) {
            errors.add(new FieldError("window.groupBy",
                    "must be source, place, admin1, country, h3:<res 3..8> or a payload path"));
        }
        int maxGroups = integer(window, "maxGroups", WindowSpec.DEFAULT_MAX_GROUPS, errors);
        if (maxGroups < 1 || maxGroups > WindowSpec.DEFAULT_MAX_GROUPS) {
            errors.add(new FieldError("window.maxGroups", "must be between 1 and 500"));
        }
        String op = null;
        Integer threshold = null;
        Duration bucket = Duration.ofHours(1);
        String slotMode = "hour-of-week";
        String direction = "up";
        String sensitivity = "medium";
        double minAbsoluteDelta = 3;
        int minWeeks = 8;
        double coverageMin = 0.8;
        if (ruleType == RuleType.COUNT) {
            op = text(window, "op", null);
            if (op == null || (!op.equals(">=") && !op.equals("<="))) {
                errors.add(new FieldError("window.op", "must be >= or <="));
            }
            JsonNode t = window.get("threshold");
            if (t == null || !t.isNumber() || t.asInt() < 0) {
                errors.add(new FieldError("window.threshold", "must be an integer of at least 0"));
            } else {
                threshold = t.asInt();
            }
        } else if (ruleType == RuleType.ABSENCE) {
            coverageMin = fraction(window, "coverageMin", 0.8, errors);
        } else {
            bucket = anomalyBucket(window, errors);
            slotMode = enumText(window, "slotMode", Set.of("hour-of-week", "hour-of-day"), "hour-of-week", errors);
            direction = enumText(window, "direction", Set.of("up", "down", "both"), "up", errors);
            sensitivity = enumText(window, "sensitivity", Set.of("low", "medium", "high"), "medium", errors);
            minAbsoluteDelta = number(window, "minAbsoluteDelta", 3, errors);
            minWeeks = integer(window, "minWeeks", 8, errors);
            if (minWeeks < 8 || minWeeks > 12) {
                errors.add(new FieldError("window.minWeeks", "must be between 8 and 12"));
            }
            coverageMin = fraction(window, "coverageMin", 0.8, errors);
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(ErrorCodes.WINDOW_RULE_INVALID,
                    "The rule's window is not valid.", errors);
        }
        return new WindowSpec(windowLength, step, distinctBy, groupBy, maxGroups, op, threshold, bucket, slotMode,
                direction, sensitivity, minAbsoluteDelta, minWeeks, coverageMin);
    }

    private static void rejectSingleVersionOptions(JsonNode body) {
        if (body.path("onlyNew").asBoolean(false)) {
            throw invalid("onlyNew", "is not allowed for a windowed rule");
        }
        if (body.path("reappearAsEnter").asBoolean(false)) {
            throw invalid("reappearAsEnter", "is not allowed for a windowed rule");
        }
        JsonNode mode = body.get("mode");
        if (mode != null && !mode.isNull() && (!mode.isString() || !"match".equals(mode.asString()))) {
            throw invalid("mode", "is not allowed for a windowed rule");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(ErrorCodes.WINDOW_RULE_INVALID,
                "The rule is not valid: " + field + " " + message + ".",
                List.of(new FieldError(field, message)));
    }

    private static Duration duration(JsonNode window, String field, List<FieldError> errors, boolean required) {
        JsonNode node = window.get(field);
        if (node == null || node.isNull()) {
            if (required) {
                errors.add(new FieldError("window." + field, "is required"));
            }
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError("window." + field, "must be a duration string"));
            return null;
        }
        try {
            return Durations.parse(node.asString());
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("window." + field, e.getMessage()));
            return null;
        }
    }

    private static Duration anomalyBucket(JsonNode window, List<FieldError> errors) {
        String value = text(window, "bucket", "1h");
        try {
            Duration bucket = Durations.parse(value);
            if (!bucket.equals(Duration.ofMinutes(15)) && !bucket.equals(Duration.ofHours(1))) {
                errors.add(new FieldError("window.bucket", "must be 15m or 1h"));
                return Duration.ofHours(1);
            }
            return bucket;
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("window.bucket", "must be 15m or 1h"));
            return Duration.ofHours(1);
        }
    }

    private static String text(JsonNode window, String field, String fallback) {
        JsonNode node = window.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        return node.isString() ? node.asString() : fallback;
    }

    private static String enumText(JsonNode window, String field, Set<String> allowed, String fallback,
            List<FieldError> errors) {
        String value = text(window, field, fallback);
        if (!allowed.contains(value)) {
            errors.add(new FieldError("window." + field, "must be one of " + allowed));
            return fallback;
        }
        return value;
    }

    private static int integer(JsonNode window, String field, int fallback, List<FieldError> errors) {
        JsonNode node = window.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        if (!node.isNumber()) {
            errors.add(new FieldError("window." + field, "must be a number"));
            return fallback;
        }
        return node.asInt();
    }

    private static double number(JsonNode window, String field, double fallback, List<FieldError> errors) {
        JsonNode node = window.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        if (!node.isNumber()) {
            errors.add(new FieldError("window." + field, "must be a number"));
            return fallback;
        }
        return node.asDouble();
    }

    private static double fraction(JsonNode window, String field, double fallback, List<FieldError> errors) {
        double value = number(window, field, fallback, errors);
        if (value < 0 || value > 1) {
            errors.add(new FieldError("window." + field, "must be between 0 and 1"));
            return fallback;
        }
        return value;
    }

    /** True when {@code groupBy} is one of the supported forms. */
    public static boolean validGroupBy(String groupBy) {
        if (GROUP_SPECIAL.contains(groupBy)) {
            return true;
        }
        if (groupBy.startsWith("h3:")) {
            try {
                int res = Integer.parseInt(groupBy.substring(3));
                return res >= 3 && res <= 8;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        // Any other string is treated as a payload path.
        return !groupBy.isBlank() && !groupBy.contains("[");
    }
}
