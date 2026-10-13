package ai.foresignal.mqtt.channel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The Home Assistant device config of prompt 060: golden payload, caps and removal. */
class HaDiscoveryPayloadTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final UUID R1 = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    static final UUID R2 = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    static final UUID R3 = UUID.fromString("00000000-0000-0000-0000-0000000000a3");

    static HaDiscoveryPayloads.Input input(List<HaDiscoveryPayloads.Rule> rules, List<String> sources, int maxRules,
            int maxSources) {
        return new HaDiscoveryPayloads.Input("abcd1234", "1.2.3", "https://fs.example", "foresignal", rules, sources,
                maxRules, maxSources);
    }

    static List<HaDiscoveryPayloads.Rule> rules() {
        return List.of(new HaDiscoveryPayloads.Rule(R2, "Alpha", "warning", true),
                new HaDiscoveryPayloads.Rule(R1, "Zeta", "critical", true),
                new HaDiscoveryPayloads.Rule(R3, "Disabled", "critical", false));
    }

    static JsonNode goldenFixture() {
        try (InputStream in = HaDiscoveryPayloadTest.class.getResourceAsStream("/fixtures/mqtt/ha-device.json")) {
            return JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @DisplayName("the device config matches the golden payload: dev, o, availability, qos and every component")
    void goldenDevice() {
        Map<String, Object> device = HaDiscoveryPayloads.device(input(rules(), List.of("usgs-earthquakes"), 50, 100));
        assertThat((JsonNode) JSON.valueToTree(device)).isEqualTo(goldenFixture());
    }

    @Test
    @DisplayName("components come in a stable order and every unique_id is foresignal_{installId}_{key}")
    void orderAndUniqueIds() {
        Map<String, Map<String, Object>> cmps = HaDiscoveryPayloads.components(input(rules(), List.of("b", "a"), 50, 100));
        assertThat(cmps.keySet()).containsExactly("alerts_open_critical", "alerts_open_warning", "alerts_open_info",
                "sources_failing", "sources_degraded", "last_briefing", "alerts_info", "alerts_warning",
                "alerts_critical", "rule_" + R1, "rule_" + R2, "source_a", "source_b");
        cmps.forEach((key, c) -> assertThat(c.get("unique_id")).isEqualTo("foresignal_abcd1234_" + key));
        assertThat(cmps.get("alerts_critical")).containsEntry("p", "event")
                .containsEntry("state_topic", "foresignal/alerts/critical")
                .containsEntry("event_types", List.of("critical"));
        assertThat(cmps.get("last_briefing")).containsEntry("device_class", "timestamp");
    }

    @Test
    @DisplayName("rules: enabled only, critical first, then by name, capped at haMaxRules")
    void ruleCap() {
        List<HaDiscoveryPayloads.Rule> many = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            many.add(new HaDiscoveryPayloads.Rule(new UUID(0, i), String.format("rule %02d", i),
                    i % 3 == 0 ? "critical" : "info", true));
        }
        List<HaDiscoveryPayloads.Rule> selected = HaDiscoveryPayloads.selectRules(many, 50);
        assertThat(selected).hasSize(50);
        assertThat(selected.subList(0, 20)).allMatch(r -> r.severity().equals("critical"));
        assertThat(selected.get(0).name()).isEqualTo("rule 00");
        assertThat(selected.get(20).name()).isEqualTo("rule 01");
        assertThat(HaDiscoveryPayloads.selectRules(rules(), 50)).extracting(HaDiscoveryPayloads.Rule::id)
                .containsExactly(R1, R2);
        assertThat(HaDiscoveryPayloads.selectRules(rules(), 0)).isEmpty();
    }

    @Test
    @DisplayName("sources: sorted, de-duplicated, capped at haMaxSources; ids sanitized in component keys")
    void sourceCap() {
        List<String> sources = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            sources.add(String.format("src-%03d", i));
        }
        assertThat(HaDiscoveryPayloads.selectSources(sources, 100)).hasSize(100).first().isEqualTo("src-000");
        Map<String, Map<String, Object>> cmps = HaDiscoveryPayloads.components(
                input(List.of(), List.of("a.b c", "a.b c"), 50, 100));
        assertThat(cmps).containsKey("source_a_b_c");
        assertThat(cmps.get("source_a_b_c")).containsEntry("state_topic", "foresignal/sources/a.b c/health");
        assertThat(cmps.keySet().stream().filter(k -> k.startsWith("source_"))).hasSize(1);
    }

    @Test
    @DisplayName("removal: a dropped component is first published as {p}, and the platforms diff finds it")
    void removal() {
        Map<String, Object> before = HaDiscoveryPayloads.device(input(rules(), List.of("s1"), 50, 100));
        Map<String, Object> after = HaDiscoveryPayloads.device(input(rules().subList(0, 1), List.of(), 50, 100));
        Map<String, String> removed = HaDiscoveryPayloads.removed(HaDiscoveryPayloads.platforms(before),
                HaDiscoveryPayloads.platforms(after));
        assertThat(removed).containsExactly(Map.entry("rule_" + R1, "binary_sensor"),
                Map.entry("source_s1", "binary_sensor"));
        JsonNode step1 = JSON.valueToTree(HaDiscoveryPayloads.withRemoved(after, removed));
        assertThat(step1.get("cmps").get("rule_" + R1)).isEqualTo(JSON.readTree("{\"p\":\"binary_sensor\"}"));
        assertThat(step1.get("cmps").get("source_s1")).isEqualTo(JSON.readTree("{\"p\":\"binary_sensor\"}"));
        assertThat(step1.get("cmps").has("rule_" + R2)).isTrue();
        assertThat(step1.get("dev")).isEqualTo(JSON.valueToTree(after).get("dev"));
        JsonNode step2 = JSON.valueToTree(after);
        assertThat(step2.get("cmps").has("rule_" + R1)).isFalse();
    }

    @Test
    @DisplayName("without a public base URL the device has no configuration URL")
    void noConfigurationUrl() {
        Map<String, Object> device = HaDiscoveryPayloads.device(new HaDiscoveryPayloads.Input("abcd1234", "dev", null,
                "fs", List.of(), List.of(), 50, 100));
        assertThat(JSON.valueToTree(device).get("dev").has("cu")).isFalse();
        assertThat(JSON.valueToTree(device).get("availability").get(0).get("topic").asString()).isEqualTo("fs/status");
    }

    @Test
    @DisplayName("a rule with an unknown severity sorts last instead of failing")
    void unknownSeverity() {
        List<HaDiscoveryPayloads.Rule> list = List.of(new HaDiscoveryPayloads.Rule(R1, "x", null, true),
                new HaDiscoveryPayloads.Rule(R2, "y", "info", true));
        assertThat(HaDiscoveryPayloads.selectRules(list, 5)).extracting(HaDiscoveryPayloads.Rule::id)
                .containsExactly(R2, R1);
    }
}
