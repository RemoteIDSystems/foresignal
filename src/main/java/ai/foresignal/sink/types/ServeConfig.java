package ai.foresignal.sink.types;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.sink.ServeGuard;
import ai.foresignal.sink.SinkConfig;
import java.net.InetAddress;
import java.util.Map;

/**
 * The common SERVE configuration (prompt 110): {@code bind} (default 127.0.0.1), {@code port}
 * (required, no default), {@code allowAllInterfaces}, {@code allowTailnet} and {@code maxClients}
 * (default 8).
 */
public record ServeConfig(InetAddress bind, int port, boolean allowAllInterfaces, boolean allowTailnet,
        int maxClients) {

    /** The {@code config} JSON Schema fragment shared by the SERVE types. */
    public static final Map<String, Object> SCHEMA = Map.of("type", "object", "properties", Map.of(
            "mode", Map.of("type", "string", "enum", java.util.List.of("PUSH", "SERVE")),
            "bind", Map.of("type", "string", "default", "127.0.0.1"),
            "port", Map.of("type", "integer", "minimum", 1, "maximum", 65535),
            "allowAllInterfaces", Map.of("type", "boolean", "default", false),
            "allowTailnet", Map.of("type", "boolean", "default", false),
            "maxClients", Map.of("type", "integer", "minimum", 1, "default", 8)));

    /** Parses and validates the SERVE config, throwing {@code 400 VALIDATION_FAILED}. */
    public static ServeConfig of(ServeGuard guard, SinkConfig config) {
        return parse(guard, config.config());
    }

    /** Validates the SERVE config. */
    public static void validate(ServeGuard guard, Map<String, Object> config) {
        parse(guard, config);
    }

    private static ServeConfig parse(ServeGuard guard, Map<String, Object> config) {
        boolean allowAll = bool(config, "allowAllInterfaces");
        boolean allowTailnet = bool(config, "allowTailnet");
        Object port = config == null ? null : config.get("port");
        if (port == null) {
            throw ValidationException.forField("config.port", "is required for a SERVE sink");
        }
        int portNumber = port instanceof Number n ? n.intValue() : Integer.parseInt(String.valueOf(port));
        if (portNumber < 1 || portNumber > 65535) {
            throw ValidationException.forField("config.port", "must be 1..65535");
        }
        String bind = config.get("bind") == null ? ServeGuard.DEFAULT_BIND : String.valueOf(config.get("bind"));
        InetAddress address = guard.resolveBind(bind, allowAll, allowTailnet);
        int maxClients = config.get("maxClients") instanceof Number n ? n.intValue() : 8;
        return new ServeConfig(address, portNumber, allowAll, allowTailnet, Math.max(1, maxClients));
    }

    private static boolean bool(Map<String, Object> config, String key) {
        Object value = config == null ? null : config.get(key);
        return value instanceof Boolean b ? b : value != null && Boolean.parseBoolean(String.valueOf(value));
    }
}
