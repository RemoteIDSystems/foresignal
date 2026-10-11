package ai.foresignal.source.spi;

import java.util.List;

/**
 * One adapter parameter: its type, whether it is required, and what values it accepts.
 *
 * <p>Declaring parameters rather than letting adapters read free-form maps is what allows
 * {@link ai.foresignal.source.ParamValidator} to reject bad configuration at the API boundary, before it
 * reaches an upstream.
 *
 * @param name the key in {@code source_settings.params}
 * @param type how the value is coerced and checked
 * @param description shown in {@code GET /v1/sources}
 * @param defaultValue applied when the operator supplies nothing; null means no default
 * @param required true when the source cannot run without it
 * @param allowed when non-empty, the only accepted values (compared as strings)
 */
public record ParamSpec(
        String name,
        ParamType type,
        String description,
        Object defaultValue,
        boolean required,
        List<String> allowed) {

    public ParamSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Param name is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Param '" + name + "' needs a type");
        }
        allowed = allowed == null ? List.of() : List.copyOf(allowed);
    }

    /** An optional parameter with a default. */
    public static ParamSpec optional(String name, ParamType type, String description, Object defaultValue) {
        return new ParamSpec(name, type, description, defaultValue, false, List.of());
    }

    /** A parameter the source cannot run without. */
    public static ParamSpec required(String name, ParamType type, String description) {
        return new ParamSpec(name, type, description, null, true, List.of());
    }

    /** An optional parameter restricted to a fixed set of values. */
    public static ParamSpec enumerated(
            String name, String description, Object defaultValue, List<String> allowed) {
        return new ParamSpec(name, ParamType.STRING, description, defaultValue, false, allowed);
    }

    public boolean hasDefault() {
        return defaultValue != null;
    }
}
