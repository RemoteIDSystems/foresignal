package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import java.util.List;
import java.util.Map;

/** Reads typed members of a binding's {@code config} and records what is wrong with them (prompt 138). Pure. */
public final class BindingConfig {

    private BindingConfig() {}

    /** A string member, or null when absent, null or blank. */
    public static String text(Map<String, Object> config, String name) {
        Object value = config == null ? null : config.get(name);
        return value instanceof String s && !s.isBlank() ? s.strip() : null;
    }

    /** A number member, or null. */
    public static Double number(Map<String, Object> config, String name) {
        Object value = config == null ? null : config.get(name);
        return value instanceof Number n && Double.isFinite(n.doubleValue()) ? n.doubleValue() : null;
    }

    /** A boolean member, or {@code fallback}. */
    public static boolean flag(Map<String, Object> config, String name, boolean fallback) {
        Object value = config == null ? null : config.get(name);
        return value instanceof Boolean b ? b : fallback;
    }

    /** Adds an error when the member is present but not a string. */
    public static void string(Map<String, Object> config, String name, List<FieldError> errors) {
        Object value = config == null ? null : config.get(name);
        if (value != null && !(value instanceof String)) {
            errors.add(new FieldError("config." + name, "must be a string"));
        }
    }

    /** Adds an error when the member is missing or blank. */
    public static String required(Map<String, Object> config, String name, List<FieldError> errors) {
        String value = text(config, name);
        if (value == null) {
            errors.add(new FieldError("config." + name, "is required"));
        }
        return value;
    }

    /** Adds an error when the member is present but not a number in {@code [min, max]}. */
    public static void range(Map<String, Object> config, String name, double min, double max,
            List<FieldError> errors) {
        Object value = config == null ? null : config.get(name);
        if (value == null) {
            return;
        }
        if (!(value instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() < min
                || n.doubleValue() > max) {
            errors.add(new FieldError("config." + name, "must be a number from " + min + " to " + max));
        }
    }

    /** Adds an error when the member is present but not one of {@code allowed}. */
    public static void oneOf(Map<String, Object> config, String name, List<String> allowed,
            List<FieldError> errors) {
        Object value = config == null ? null : config.get(name);
        if (value != null && !(value instanceof String s && allowed.contains(s))) {
            errors.add(new FieldError("config." + name, "must be one of " + allowed));
        }
    }

    /** Adds an error when the member is present but not a boolean. */
    public static void bool(Map<String, Object> config, String name, List<FieldError> errors) {
        Object value = config == null ? null : config.get(name);
        if (value != null && !(value instanceof Boolean)) {
            errors.add(new FieldError("config." + name, "must be a boolean"));
        }
    }
}
