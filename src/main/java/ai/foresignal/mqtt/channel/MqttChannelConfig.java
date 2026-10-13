package ai.foresignal.mqtt.channel;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.mqtt.MqttConnectionSpec;
import ai.foresignal.mqtt.MqttEndpoint;
import ai.foresignal.mqtt.MqttTls;
import ai.foresignal.notify.PrivatePayloads;
import ai.foresignal.settings.Durations;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The settings of one {@code mqtt} channel (OpenAPI {@code MqttChannelConfig}, prompt 060), with defaults.
 *
 * @param brokerUrl {@code mqtt://}, {@code mqtts://}, {@code ws://} or {@code wss://}
 * @param topicPrefix the topic prefix, default {@code foresignal}
 * @param qos 0 or 1, default 1
 * @param protocolVersion {@code 5} (default) or {@code 3.1.1}
 * @param messageExpiry the expiry of non-retained event messages, default 24 h
 * @param payloadTemplate the optional JSON template of event payloads, or null
 * @param publishHealth whether source health is published, default true
 * @param publishSummary whether the glance summary is published, default true
 * @param summaryInterval how often the summary is checked, default 60 s
 * @param haDiscovery whether Home Assistant discovery is published, default false
 * @param haPrefix the discovery prefix, default {@code homeassistant}
 * @param haMaxRules the most rules given a problem sensor, default 50
 * @param haMaxSources the most sources given a problem sensor, default 100
 * @param haRetainDiscovery whether the discovery config is retained, default false
 * @param privatePayload whether private-source alerts carry their payload (prompt 039)
 */
public record MqttChannelConfig(
        String brokerUrl,
        String topicPrefix,
        int qos,
        String protocolVersion,
        Duration messageExpiry,
        String payloadTemplate,
        boolean publishHealth,
        boolean publishSummary,
        Duration summaryInterval,
        boolean haDiscovery,
        String haPrefix,
        int haMaxRules,
        int haMaxSources,
        boolean haRetainDiscovery,
        boolean privatePayload) {

    /** The default topic prefix. */
    public static final String DEFAULT_PREFIX = "foresignal";

    /** The default discovery prefix. */
    public static final String DEFAULT_HA_PREFIX = "homeassistant";

    /** A topic prefix: letters, digits, {@code _}, {@code -} and {@code /}, 1–64 characters, no wildcards. */
    public static final Pattern PREFIX = Pattern.compile("^[A-Za-z0-9_\\-/]{1,64}$");

    /** The largest {@code haMaxRules} and {@code haMaxSources}, which keeps the discovery config small. */
    public static final int MAX_HA_ENTITIES = 500;

    /** The shortest summary interval. */
    public static final Duration MIN_SUMMARY_INTERVAL = Duration.ofSeconds(10);

    /** The longest MQTT 5 message expiry interval (a four-byte unsigned integer of seconds). */
    public static final long MAX_EXPIRY_SECONDS = 4_294_967_295L;

    /** The settings of {@code config}, defaults filled in; assumes {@link #validate} passed. */
    public static MqttChannelConfig of(Map<String, Object> config) {
        Map<String, Object> c = config == null ? Map.of() : config;
        return new MqttChannelConfig(
                text(c, "brokerUrl", null),
                trimSlashes(text(c, "topicPrefix", DEFAULT_PREFIX)),
                integer(c, "qos", 1),
                MqttConnectionSpec.V311.equals(text(c, "protocolVersion", "5")) ? MqttConnectionSpec.V311
                        : MqttConnectionSpec.V5,
                duration(c, "messageExpiry", Duration.ofHours(24)),
                text(c, "payloadTemplate", null),
                flag(c, "publishHealth", true),
                flag(c, "publishSummary", true),
                duration(c, "summaryInterval", Duration.ofSeconds(60)),
                flag(c, "haDiscovery", false),
                trimSlashes(text(c, "haPrefix", DEFAULT_HA_PREFIX)),
                integer(c, "haMaxRules", 50),
                integer(c, "haMaxSources", 100),
                flag(c, "haRetainDiscovery", false),
                PrivatePayloads.optedIn(c));
    }

    /** The broker endpoint. */
    public MqttEndpoint endpoint() {
        return MqttEndpoint.parse(brokerUrl);
    }

    /**
     * The type-specific checks of a channel's {@code config} and {@code secrets}: broker URL, prefixes, QoS,
     * durations, discovery caps, the payload template and the CA PEM.
     */
    public static List<FieldError> validate(Map<String, Object> config, Map<String, String> secrets) {
        List<FieldError> errors = new ArrayList<>();
        try {
            MqttEndpoint.parse(config.get("brokerUrl") instanceof String s ? s : null);
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("config.brokerUrl", e.getMessage()));
        }
        prefix(config, "topicPrefix", errors);
        prefix(config, "haPrefix", errors);
        Object qos = config.get("qos");
        if (qos != null && !(qos instanceof Number n && (n.doubleValue() == 0 || n.doubleValue() == 1))) {
            errors.add(new FieldError("config.qos", "must be 0 or 1"));
        }
        Duration expiry = durationField(config, "messageExpiry", errors);
        if (expiry != null && expiry.toSeconds() > MAX_EXPIRY_SECONDS) {
            errors.add(new FieldError("config.messageExpiry", "must be at most " + MAX_EXPIRY_SECONDS + " seconds"));
        }
        Duration interval = durationField(config, "summaryInterval", errors);
        if (interval != null && interval.compareTo(MIN_SUMMARY_INTERVAL) < 0) {
            errors.add(new FieldError("config.summaryInterval", "must be at least 10s"));
        }
        for (String cap : List.of("haMaxRules", "haMaxSources")) {
            Object value = config.get(cap);
            if (value != null && !(value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())
                    && n.longValue() >= 0 && n.longValue() <= MAX_HA_ENTITIES)) {
                errors.add(new FieldError("config." + cap, "must be an integer from 0 to " + MAX_HA_ENTITIES));
            }
        }
        if (config.get("payloadTemplate") instanceof String template && !template.isBlank()
                && !MqttPayloads.templateRendersJson(template)) {
            errors.add(new FieldError("config.payloadTemplate", "must render to valid JSON"));
        }
        String problem = MqttTls.problem(secrets == null ? null : secrets.get("caCertPem"));
        if (problem != null) {
            errors.add(new FieldError("secrets.caCertPem", problem));
        }
        if (secrets != null && secrets.get("password") != null && secrets.get("username") == null
                && MqttConnectionSpec.V311.equals(String.valueOf(config.get("protocolVersion")))) {
            errors.add(new FieldError("secrets.username", "is required with a password on MQTT 3.1.1"));
        }
        return errors;
    }

    private static void prefix(Map<String, Object> config, String field, List<FieldError> errors) {
        Object value = config.get(field);
        if (value == null) {
            return;
        }
        if (!(value instanceof String text) || !PREFIX.matcher(text).matches() || trimSlashes(text).isEmpty()) {
            errors.add(new FieldError("config." + field,
                    "must be 1-64 letters, digits, _, - or / (no + or # wildcards)"));
        }
    }

    private static Duration durationField(Map<String, Object> config, String field, List<FieldError> errors) {
        Object value = config.get(field);
        if (value == null) {
            return null;
        }
        try {
            return Durations.parsePositive(String.valueOf(value));
        } catch (RuntimeException e) {
            errors.add(new FieldError("config." + field, "must be a positive duration such as 60s, 15m or PT1H"));
            return null;
        }
    }

    private static String text(Map<String, Object> c, String key, String def) {
        Object value = c.get(key);
        return value == null || String.valueOf(value).isBlank() ? def : String.valueOf(value).strip();
    }

    private static int integer(Map<String, Object> c, String key, int def) {
        return c.get(key) instanceof Number n ? n.intValue() : def;
    }

    private static boolean flag(Map<String, Object> c, String key, boolean def) {
        Object value = c.get(key);
        return value instanceof Boolean b ? b : def;
    }

    private static Duration duration(Map<String, Object> c, String key, Duration def) {
        Object value = c.get(key);
        if (value == null) {
            return def;
        }
        try {
            return Durations.parsePositive(String.valueOf(value));
        } catch (RuntimeException e) {
            return def;
        }
    }

    /** {@code prefix} without leading and trailing slashes, so topics never contain an empty level. */
    static String trimSlashes(String prefix) {
        String out = prefix == null ? "" : prefix;
        while (out.startsWith("/")) {
            out = out.substring(1);
        }
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }
}
