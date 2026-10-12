package ai.foresignal.notify;

import ai.foresignal.api.error.FieldError;
import java.util.List;
import java.util.Map;

/**
 * A channel's units (prompt 040): {@code config.units} is {@code metric} (default) or {@code imperial};
 * imperial renders {@code {{near.text}}} in miles.
 */
public final class ChannelUnits {

    /** The config key. */
    public static final String CONFIG_KEY = "units";

    /** The accepted values. */
    public static final List<String> VALUES = List.of("metric", "imperial");

    private ChannelUnits() {}

    /** True when {@code config} has {@code units: imperial}. */
    public static boolean imperial(Map<String, Object> config) {
        return config != null && "imperial".equals(config.get(CONFIG_KEY));
    }

    /** Adds an error when {@code config.units} is present and not {@code metric} or {@code imperial}. */
    public static void validate(Map<String, Object> config, List<FieldError> errors) {
        Object units = config == null ? null : config.get(CONFIG_KEY);
        if (units != null && !VALUES.contains(units)) {
            errors.add(new FieldError("config." + CONFIG_KEY, "must be metric or imperial"));
        }
    }
}
