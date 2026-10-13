package ai.foresignal.mqtt.channel;

import ai.foresignal.trigger.Severity;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * The Home Assistant device-based MQTT discovery config of a Foresignal install (prompt 060), published on
 * {@code {haPrefix}/device/foresignal_{installId}/config}.
 *
 * <ul>
 *   <li>{@code dev}: {@code ids [foresignal_{installId}]}, name, manufacturer, model, software version and
 *       {@code cu} (the public base URL, when set); {@code o}: the origin (name, version, project URL);
 *       {@code availability}: {@code {prefix}/status} ({@code online}/{@code offline}); {@code qos} 1.
 *   <li>{@code cmps}, each with {@code unique_id foresignal_{installId}_{key}}: the sensors
 *       {@code alerts_open_critical|warning|info}, {@code sources_failing}, {@code sources_degraded} and
 *       {@code last_briefing} (a timestamp) read from the {@code {prefix}/summary} glance; the event entities
 *       {@code alerts_info|warning|critical} on {@code {prefix}/alerts/{severity}} (a state topic cannot hold
 *       a wildcard, hence one per severity); a {@code binary_sensor} {@code rule_{id}} (device class
 *       {@code problem}) per enabled rule, critical first then by name, at most {@code haMaxRules}; and a
 *       {@code binary_sensor} {@code source_{id}} per non-private source, by id, at most
 *       {@code haMaxSources}.
 * </ul>
 *
 * <p>Removal (Home Assistant's rules): a component that leaves the set is first published reduced to
 * {@code {p}}, then left out; the whole device is removed by an empty retained payload.
 *
 * <p>{@code device_tracker} entities for watched aircraft and vessels are deferred: Home Assistant uses GPS
 * trackers only to place a device inside its zones, which adds little for objects that are almost never in
 * one.
 */
public final class HaDiscoveryPayloads {

    /** The project URL of the {@code o} (origin) block. */
    public static final String PROJECT_URL = "https://foresignal.ai";

    /** The severities, in the order of the event entities. */
    public static final List<String> SEVERITIES = List.of("info", "warning", "critical");

    private HaDiscoveryPayloads() {}

    /**
     * A rule as discovery sees it.
     *
     * @param id the rule id
     * @param name the rule name
     * @param severity the severity wire value
     * @param enabled whether it is enabled
     */
    public record Rule(UUID id, String name, String severity, boolean enabled) {}

    /**
     * Everything the device config is built from.
     *
     * @param installId the install id
     * @param version Foresignal's version
     * @param publicBaseUrl the public base URL, or null
     * @param topicPrefix the channel's topic prefix
     * @param rules every rule
     * @param sources every source id that is not private
     * @param maxRules the rule cap
     * @param maxSources the source cap
     */
    public record Input(String installId, String version, String publicBaseUrl, String topicPrefix, List<Rule> rules,
            List<String> sources, int maxRules, int maxSources) {}

    /** The device config. */
    public static Map<String, Object> device(Input in) {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> dev = new LinkedHashMap<>();
        dev.put("ids", List.of("foresignal_" + in.installId()));
        dev.put("name", "Foresignal");
        dev.put("mf", "Foresignal");
        dev.put("mdl", "Foresignal broker");
        dev.put("sw", in.version());
        if (in.publicBaseUrl() != null) {
            dev.put("cu", in.publicBaseUrl());
        }
        out.put("dev", dev);
        Map<String, Object> origin = new LinkedHashMap<>();
        origin.put("name", "Foresignal");
        origin.put("sw", in.version());
        origin.put("url", PROJECT_URL);
        out.put("o", origin);
        out.put("availability", List.of(Map.of("topic", MqttTopics.status(in.topicPrefix()))));
        out.put("qos", 1);
        out.put("cmps", components(in));
        return out;
    }

    /** The components, in a stable order. */
    public static Map<String, Map<String, Object>> components(Input in) {
        Map<String, Map<String, Object>> cmps = new LinkedHashMap<>();
        String summary = MqttTopics.summary(in.topicPrefix());
        for (String severity : List.of("critical", "warning", "info")) {
            String key = "alerts_open_" + severity;
            Map<String, Object> c = component(in, "sensor", key, "Open " + severity + " alerts");
            c.put("state_topic", summary);
            c.put("value_template", "{{ value_json.alerts.open." + severity + " | default(0) }}");
            c.put("state_class", "measurement");
            c.put("icon", "critical".equals(severity) ? "mdi:alert-octagon" : "mdi:alert");
            cmps.put(key, c);
        }
        for (String state : List.of("failing", "degraded")) {
            String key = "sources_" + state;
            Map<String, Object> c = component(in, "sensor", key, capitalize(state) + " sources");
            c.put("state_topic", summary);
            c.put("value_template", "{{ value_json.sources." + state + " | default(0) }}");
            c.put("state_class", "measurement");
            c.put("icon", "mdi:database-alert");
            cmps.put(key, c);
        }
        Map<String, Object> briefing = component(in, "sensor", "last_briefing", "Last briefing");
        briefing.put("device_class", "timestamp");
        briefing.put("state_topic", summary);
        briefing.put("value_template", "{{ value_json.briefing.at if value_json.briefing is defined else None }}");
        cmps.put("last_briefing", briefing);
        for (String severity : SEVERITIES) {
            String key = "alerts_" + severity;
            Map<String, Object> c = component(in, "event", key, capitalize(severity) + " alert");
            c.put("state_topic", MqttTopics.alerts(in.topicPrefix(), severity));
            c.put("event_types", List.of(severity));
            c.put("icon", "mdi:bell-alert");
            cmps.put(key, c);
        }
        for (Rule rule : selectRules(in.rules(), in.maxRules())) {
            String key = "rule_" + keyPart(rule.id().toString());
            String topic = MqttTopics.ruleState(in.topicPrefix(), rule.id());
            Map<String, Object> c = component(in, "binary_sensor", key, rule.name() == null ? "Rule " + rule.id()
                    : rule.name());
            c.put("device_class", "problem");
            c.put("state_topic", topic);
            c.put("value_template", "{{ 'ON' if value_json.state == 'firing' else 'OFF' }}");
            c.put("json_attributes_topic", topic);
            cmps.put(key, c);
        }
        for (String source : selectSources(in.sources(), in.maxSources())) {
            String key = "source_" + keyPart(source);
            String topic = MqttTopics.sourceHealth(in.topicPrefix(), source);
            Map<String, Object> c = component(in, "binary_sensor", key, "Source " + source);
            c.put("device_class", "problem");
            c.put("state_topic", topic);
            c.put("value_template", "{{ 'ON' if value_json.status in ['degraded', 'failing'] else 'OFF' }}");
            c.put("json_attributes_topic", topic);
            cmps.put(key, c);
        }
        return cmps;
    }

    /** Enabled rules, critical first, then by name (case-insensitive) and id, at most {@code max}. */
    public static List<Rule> selectRules(List<Rule> rules, int max) {
        return rules.stream().filter(Rule::enabled)
                .sorted(Comparator.comparing((Rule r) -> -rank(r.severity()))
                        .thenComparing(r -> r.name() == null ? "" : r.name().toLowerCase(Locale.ROOT))
                        .thenComparing(r -> r.id().toString()))
                .limit(Math.max(0, max)).toList();
    }

    /** Source ids in order, at most {@code max}. */
    public static List<String> selectSources(List<String> sources, int max) {
        return sources.stream().sorted().distinct().limit(Math.max(0, max)).toList();
    }

    /**
     * The device config with {@code removed} components reduced to {@code {p}} (the first step of removing
     * them).
     *
     * @param device the new device config, without the removed components
     * @param removed the removed component keys with their platforms
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> withRemoved(Map<String, Object> device, Map<String, String> removed) {
        Map<String, Object> out = new LinkedHashMap<>(device);
        Map<String, Object> cmps = new LinkedHashMap<>((Map<String, Object>) device.get("cmps"));
        removed.forEach((key, platform) -> cmps.put(key, Map.of("p", platform)));
        out.put("cmps", cmps);
        return out;
    }

    /** The platforms of the components of {@code device}, by key. */
    @SuppressWarnings("unchecked")
    public static Map<String, String> platforms(Map<String, Object> device) {
        Map<String, String> out = new LinkedHashMap<>();
        Object cmps = device == null ? null : device.get("cmps");
        if (cmps instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (v instanceof Map<?, ?> c && c.get("p") != null) {
                    out.put(String.valueOf(k), String.valueOf(c.get("p")));
                }
            });
        }
        return out;
    }

    /** The components of {@code previous} that {@code current} no longer has, with their platforms. */
    public static Map<String, String> removed(Map<String, String> previous, Map<String, String> current) {
        Map<String, String> out = new LinkedHashMap<>();
        previous.forEach((key, platform) -> {
            if (!current.containsKey(key)) {
                out.put(key, platform);
            }
        });
        return out;
    }

    private static int rank(String severity) {
        try {
            return Severity.fromWire(severity).ordinal();
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    private static Map<String, Object> component(Input in, String platform, String key, String name) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("p", platform);
        c.put("name", name);
        c.put("unique_id", "foresignal_" + in.installId() + "_" + key);
        return c;
    }

    /** {@code text} with every character outside {@code [A-Za-z0-9_-]} replaced by {@code _}. */
    static String keyPart(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            out.append((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')
                    || ch == '_' || ch == '-' ? ch : '_');
        }
        return out.toString();
    }

    private static String capitalize(String text) {
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
