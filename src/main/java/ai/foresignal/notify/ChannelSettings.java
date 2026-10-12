package ai.foresignal.notify;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Everything a channel type needs to render one request: the channel's decrypted settings and the
 * delivery's identity.
 *
 * @param channelId the channel
 * @param channelName the channel's name
 * @param config the non-secret settings
 * @param secrets the decrypted secret settings
 * @param zone the channel's effective timezone
 * @param deliveryId the delivery id (0 for a channel test)
 * @param now the send time
 * @param origin this instance's install id ({@link OriginMarker})
 * @param userAgent the User-Agent of every request
 * @param actions the action capabilities minted for this delivery (prompt 033); empty for most sends
 */
public record ChannelSettings(
        UUID channelId,
        String channelName,
        Map<String, Object> config,
        Map<String, String> secrets,
        ZoneId zone,
        long deliveryId,
        Instant now,
        String origin,
        String userAgent,
        List<NotificationAction> actions) {

    /** Copies the maps and the actions. */
    public ChannelSettings {
        config = config == null ? Map.of() : Map.copyOf(withoutNulls(config));
        secrets = secrets == null ? Map.of() : Map.copyOf(secrets);
        actions = actions == null ? List.of() : List.copyOf(actions);
    }

    /** Settings without actions (the 018 form). */
    public ChannelSettings(UUID channelId, String channelName, Map<String, Object> config, Map<String, String> secrets,
            ZoneId zone, long deliveryId, Instant now, String origin, String userAgent) {
        this(channelId, channelName, config, secrets, zone, deliveryId, now, origin, userAgent, List.of());
    }

    /** A copy carrying {@code newActions}. */
    public ChannelSettings withActions(List<NotificationAction> newActions) {
        return new ChannelSettings(channelId, channelName, config, secrets, zone, deliveryId, now, origin, userAgent,
                newActions);
    }

    private static Map<String, Object> withoutNulls(Map<String, Object> map) {
        java.util.LinkedHashMap<String, Object> out = new java.util.LinkedHashMap<>();
        map.forEach((k, v) -> {
            if (v != null) {
                out.put(k, v);
            }
        });
        return out;
    }

    /** A config string, or {@code def} when absent or blank. */
    public String config(String name, String def) {
        Object value = config.get(name);
        if (value == null) {
            return def;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? def : text;
    }

    /** A config boolean, or {@code def} when absent. */
    public boolean flag(String name, boolean def) {
        Object value = config.get(name);
        if (value instanceof Boolean b) {
            return b;
        }
        return value == null ? def : Boolean.parseBoolean(String.valueOf(value));
    }

    /** A config string list, or empty. */
    public List<String> list(String name) {
        Object value = config.get(name);
        if (value instanceof List<?> items) {
            return items.stream().map(String::valueOf).toList();
        }
        if (value instanceof String text && !text.isBlank()) {
            return List.of(text.split("\\s*,\\s*"));
        }
        return List.of();
    }

    /** A config map of strings, or empty. */
    public Map<String, String> map(String name) {
        Object value = config.get(name);
        if (value instanceof Map<?, ?> items) {
            java.util.LinkedHashMap<String, String> out = new java.util.LinkedHashMap<>();
            items.forEach((k, v) -> out.put(String.valueOf(k), v == null ? "" : String.valueOf(v)));
            return out;
        }
        return Map.of();
    }

    /** A secret, or null. */
    public String secret(String name) {
        String value = secrets.get(name);
        return value == null || value.isBlank() ? null : value;
    }

    /** {@code baseUrl} without trailing slashes. */
    public static String trimSlash(String baseUrl) {
        String out = baseUrl == null ? "" : baseUrl.strip();
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }
}
