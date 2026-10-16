package ai.foresignal.sink;

import java.util.Map;

/**
 * An opened sink's configuration (prompt 110): the persisted settings a {@link SinkType} needs.
 *
 * @param id the sink id
 * @param type the type id
 * @param name the name
 * @param mode PUSH or SERVE
 * @param config the non-secret settings
 * @param secrets the decrypted write-only secrets
 * @param filter the record filter
 */
public record SinkConfig(String id, String type, String name, SinkMode mode, Map<String, Object> config,
        Map<String, String> secrets, SinkFilter filter) {

    /** Validates and copies. */
    public SinkConfig {
        config = config == null ? Map.of() : Map.copyOf(config);
        secrets = secrets == null ? Map.of() : Map.copyOf(secrets);
        filter = filter == null ? SinkFilter.ALL : filter;
    }

    /** A string config value, or null. */
    public String config(String key) {
        Object value = config.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** A string config value with a default. */
    public String config(String key, String fallback) {
        String value = config(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    /** An integer config value with a default. */
    public int intConfig(String key, int fallback) {
        Object value = config.get(key);
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s && !s.isBlank()) {
            try {
                return Integer.parseInt(s.strip());
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
        return fallback;
    }

    /** A boolean config value with a default. */
    public boolean boolConfig(String key, boolean fallback) {
        Object value = config.get(key);
        return value instanceof Boolean b ? b : fallback;
    }
}
