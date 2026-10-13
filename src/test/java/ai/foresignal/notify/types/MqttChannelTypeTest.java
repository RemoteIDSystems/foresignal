package ai.foresignal.notify.types;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.mqtt.MqttMessage;
import ai.foresignal.mqtt.channel.MqttPayloads;
import ai.foresignal.mqtt.channel.MqttTopics;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.ChannelWarning;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.notify.PrivatePayloads;
import ai.foresignal.testsupport.net.TestOutbound;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code mqtt} channel type (prompt 060): config validation, topic sanitizing, golden publishes per event,
 * the 256 KB cap and the private-payload reduction.
 */
class MqttChannelTypeTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String URL = NotifyFixtures.URL;
    static final UUID RULE = NotifyFixtures.RULE;
    static final UUID BRIEFING = UUID.fromString("0b0b0b0b-0000-0000-0000-000000000001");
    static final UUID RUN = UUID.fromString("0c0c0c0c-0000-0000-0000-000000000002");

    final MqttChannel type = new MqttChannel();
    final Map<String, Object> config = Map.of("brokerUrl", "mqtt://broker.lan:1883");

    static ChannelSettings settings(Map<String, Object> config) {
        return new ChannelSettings(NotifyFixtures.CHANNEL, "mqtt", config, Map.of(), ZoneId.of("UTC"), 99,
                NotifyFixtures.T, NotifyFixtures.ORIGIN, "Foresignal/test");
    }

    static NotificationMessage fired(String body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleId", RULE.toString());
        data.put("ruleName", "Quakes");
        data.put("sourceId", "usgs-earthquakes");
        data.put("externalId", "us7");
        data.put("kind", "earthquake");
        data.put("firedAt", "2026-05-01T12:00:00Z");
        data.put("state", "firing");
        data.put("record", Map.of("geometry", Map.of("type", "Point", "coordinates", List.of(-97.74, 30.27))));
        data.put("region", Map.of("near", Map.of("name", "Austin", "text", "12 km NE of Austin, Texas")));
        return NotificationMessage.builder("alert", "alert.fired", "[warning] M5.1 near Austin", "warning")
                .id(42L).shortTitle("Quakes").body(body).url(URL).data(data).tags(List.of("quake"))
                .origin(NotifyFixtures.ORIGIN).createdAt(NotifyFixtures.T).alertId(7L).ruleId(RULE).build();
    }

    static NotificationMessage resolved() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("alertId", 7);
        data.put("ruleName", "Quakes");
        data.put("state", "resolved");
        data.put("resolvedAt", "2026-05-01T12:05:00Z");
        data.put("acknowledgedBy", null);
        return NotificationMessage.builder("alert", "alert.resolved", "✅ Resolved", "warning")
                .id(43L).body("Resolved.").url(URL).data(data).alertId(7L).ruleId(RULE).updateOf(42L).build();
    }

    static NotificationMessage briefing(String markdown) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", RUN.toString());
        data.put("briefingId", BRIEFING.toString());
        data.put("status", "succeeded");
        data.put("startedAt", "2026-05-01T06:00:00Z");
        data.put("finishedAt", "2026-05-01T06:01:00Z");
        data.put("contentMarkdown", markdown);
        return NotificationMessage.builder("briefing", "briefing.run", "Morning — 2026-05-01", "info")
                .body(markdown).url("https://fs.example/v1/briefings/" + BRIEFING + "/runs/" + RUN + ".html")
                .data(data).briefingRunId(RUN).build();
    }

    static JsonNode fixture(String name) {
        try (InputStream in = MqttChannelTypeTest.class.getResourceAsStream("/fixtures/mqtt/" + name + ".json")) {
            assertThat(in).as("fixture " + name).isNotNull();
            return JSON.readTree(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static MqttMessage mqtt(PreparedRequest request) {
        assertThat(request.method()).isEqualTo("PUBLISH");
        assertThat(request.body()).isInstanceOf(PreparedRequest.Mqtt.class);
        PreparedRequest.Mqtt body = (PreparedRequest.Mqtt) request.body();
        assertThat(body.channelId()).isEqualTo(NotifyFixtures.CHANNEL);
        return body.message();
    }

    static void assertGolden(String name, PreparedRequest request) {
        JsonNode golden = fixture(name);
        MqttMessage m = mqtt(request);
        assertThat(m.topic()).isEqualTo(golden.get("topic").asString());
        assertThat(m.qos()).isEqualTo(golden.get("qos").asInt());
        assertThat(m.retain()).isEqualTo(golden.get("retain").asBoolean());
        JsonNode expiry = golden.get("messageExpirySeconds");
        if (expiry.isNull()) {
            assertThat(m.messageExpirySeconds()).isNull();
        } else {
            assertThat(m.messageExpirySeconds()).isEqualTo(expiry.asLong());
        }
        assertThat(m.contentType()).isEqualTo(golden.get("contentType").asString());
        assertThat(JSON.readTree(m.text())).isEqualTo(golden.get("payload"));
    }

    // ------------------------------------------------------------------------- golden publishes

    @Test
    @DisplayName("alert fired: {prefix}/alerts/{severity}, not retained, 24 h expiry, the full payload")
    void alertFiredGolden() {
        assertGolden("alert-fired", type.render(fired("M5.1 near Austin"), settings(config)));
    }

    @Test
    @DisplayName("alert resolved: {prefix}/alerts/updates with {id, state, resolvedAt, acknowledgedBy}")
    void alertUpdateGolden() {
        assertGolden("alert-update", type.render(resolved(), settings(config)));
    }

    @Test
    @DisplayName("briefing run: retained on {prefix}/briefings/{briefingId}, no expiry")
    void briefingGolden() {
        assertGolden("briefing", type.render(briefing("# Morning\n\nAll quiet."), settings(config)));
    }

    @Test
    @DisplayName("channel test: {prefix}/test")
    void testGolden() {
        NotificationMessage test = NotificationMessage.builder("test", "test", "Foresignal test notification", "info")
                .body("If you can read this, the channel works.").createdAt(NotifyFixtures.T).build();
        assertGolden("test", type.render(test, settings(config)));
    }

    @Test
    @DisplayName("system event: the NotificationMessage JSON on {prefix}/system/events")
    void systemEvent() {
        NotificationMessage system = NotificationMessage.builder("system", "system.source_failing",
                "Source x is failing", "warning").body("boom").url("https://fs.example/v1/sources/x/health")
                .data(Map.of("sourceId", "x")).build();
        MqttMessage m = mqtt(type.render(system, settings(config)));
        assertThat(m.topic()).isEqualTo("foresignal/system/events");
        assertThat(m.retain()).isFalse();
        JsonNode payload = JSON.readTree(m.text());
        assertThat(payload.get("event").asString()).isEqualTo("system");
        assertThat(payload.get("subtype").asString()).isEqualTo("system.source_failing");
        assertThat(payload.get("bodyMarkdown").asString()).isEqualTo("boom");
    }

    @Test
    @DisplayName("prefix, QoS 0, expiry and the payload template are honored; the test message ignores the template")
    void settingsHonored() {
        Map<String, Object> c = new HashMap<>(config);
        c.put("topicPrefix", "/home/fs/");
        c.put("qos", 0);
        c.put("messageExpiry", "15m");
        c.put("payloadTemplate", "{\"t\":\"{{title}}\",\"s\":\"{{severity}}\",\"r\":\"{{data.ruleName}}\"}");
        MqttMessage m = mqtt(type.render(fired("x"), settings(c)));
        assertThat(m.topic()).isEqualTo("home/fs/alerts/warning");
        assertThat(m.qos()).isZero();
        assertThat(m.messageExpirySeconds()).isEqualTo(900L);
        assertThat(JSON.readTree(m.text()).get("t").asString()).isEqualTo("[warning] M5.1 near Austin");
        assertThat(JSON.readTree(m.text()).get("r").asString()).isEqualTo("Quakes");
        NotificationMessage test = NotificationMessage.builder("test", "test", "T", "info").build();
        assertThat(JSON.readTree(mqtt(type.render(test, settings(c))).text()).get("event").asString()).isEqualTo("test");
    }

    @Test
    @DisplayName("an alert without a point geometry or a near place has point null and no near member")
    void noPointNoNear() {
        NotificationMessage m = NotificationMessage.builder("alert", "alert.fired", "t", "critical")
                .data(Map.of("alertId", 1, "record", Map.of("geometry", Map.of("type", "LineString")))).alertId(1L)
                .build();
        JsonNode payload = JSON.readTree(mqtt(type.render(m, settings(config))).text());
        assertThat(payload.get("point").isNull()).isTrue();
        assertThat(payload.has("near")).isFalse();
        assertThat(payload.get("event_type").asString()).isEqualTo("critical");
        assertThat(mqtt(type.render(m, settings(config))).topic()).isEqualTo("foresignal/alerts/critical");
    }

    // ------------------------------------------------------------------------- size and privacy

    @Test
    @DisplayName("a payload over 256 KB is cut in its body, keeping valid JSON and a link")
    void truncatesAt256Kb() {
        String big = "a".repeat(300 * 1024);
        MqttMessage m = mqtt(type.render(fired(big), settings(config)));
        assertThat(m.payload().length).isLessThanOrEqualTo(MqttPayloads.MAX_BYTES);
        JsonNode payload = JSON.readTree(m.text());
        assertThat(payload.get("body").asString()).endsWith("[Read more](" + URL + ")");
        assertThat(payload.get("ruleName").asString()).isEqualTo("Quakes");
    }

    @Test
    @DisplayName("a template that renders more than 256 KB is cut in its body")
    void truncatesTemplate() {
        Map<String, Object> c = new HashMap<>(config);
        c.put("payloadTemplate", "{\"b\":\"{{body}}\"}");
        MqttMessage m = mqtt(type.render(fired("é".repeat(200 * 1024)), settings(c)));
        assertThat(m.payload().length).isLessThanOrEqualTo(MqttPayloads.MAX_BYTES);
        assertThat(JSON.readTree(m.text()).get("b").asString()).contains("[Read more]");
    }

    @Test
    @DisplayName("briefing markdown is cut to 16 KB with a link to the run")
    void briefingMarkdownCut() {
        MqttMessage m = mqtt(type.render(briefing("x".repeat(40_000)), settings(config)));
        String markdown = JSON.readTree(m.text()).get("markdown").asString();
        assertThat(markdown.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(MqttPayloads.MAX_BRIEFING_MARKDOWN);
        assertThat(markdown).endsWith("[Read more](https://fs.example/v1/briefings/" + BRIEFING + "/runs/" + RUN + ".html)");
    }

    @Test
    @DisplayName("cutMarkdown never splits a multi-byte character")
    void cutOnCharacterBoundary() {
        String cut = MqttPayloads.cutMarkdown("🔥".repeat(100), 50, "u");
        assertThat(cut).startsWith("🔥").endsWith("[Read more](u)");
        assertThat(cut.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(50);
        assertThat(MqttPayloads.cutMarkdown("short", 50, "u")).isEqualTo("short");
        assertThat(MqttPayloads.cutMarkdown(null, 50, "u")).isNull();
    }

    @Test
    @DisplayName("a private-source alert reduced by prompt 039 carries only the rule name, severity and link")
    void privateReduction() {
        NotificationMessage reduced = PrivatePayloads.redact(fired("secret coordinates"));
        JsonNode payload = JSON.readTree(mqtt(type.render(reduced, settings(config))).text());
        assertThat(payload.get("ruleName").asString()).isEqualTo("Quakes");
        assertThat(payload.get("severity").asString()).isEqualTo("warning");
        assertThat(payload.get("url").asString()).isEqualTo(URL);
        assertThat(payload.get("sourceId").isNull()).isTrue();
        assertThat(payload.get("externalId").isNull()).isTrue();
        assertThat(payload.get("point").isNull()).isTrue();
        assertThat(payload.has("near")).isFalse();
        assertThat(payload.toString()).doesNotContain("secret coordinates").doesNotContain("-97.74");
    }

    // ------------------------------------------------------------------------- topics

    @Test
    @DisplayName("topic segments replace + # / NUL and control characters with _")
    void topicSanitizing() {
        assertThat(MqttTopics.segment("a+b#c/d\u0000e\u0007f")).isEqualTo("a_b_c_d_e_f");
        assertThat(MqttTopics.segment("zürich-1")).isEqualTo("zürich-1");
        assertThat(MqttTopics.segment("")).isEqualTo("_");
        assertThat(MqttTopics.segment(null)).isEqualTo("_");
        assertThat(MqttTopics.sourceHealth("fs", "a/b+c")).isEqualTo("fs/sources/a_b_c/health");
        assertThat(MqttTopics.alerts("fs", "we#ird")).isEqualTo("fs/alerts/we_ird");
        assertThat(MqttTopics.haDevice("homeassistant", "abcd1234"))
                .isEqualTo("homeassistant/device/foresignal_abcd1234/config");
        assertThat(MqttTopics.ruleState("fs", RULE)).isEqualTo("fs/rules/" + RULE + "/state");
    }

    // ------------------------------------------------------------------------- validation

    @Test
    @DisplayName("a valid config has no errors")
    void validConfig() {
        Map<String, Object> c = new HashMap<>();
        c.put("brokerUrl", "wss://broker.example:8884/mqtt");
        c.put("topicPrefix", "home/foresignal");
        c.put("qos", 0);
        c.put("messageExpiry", "PT1H");
        c.put("summaryInterval", "5m");
        c.put("haMaxRules", 0);
        c.put("haMaxSources", 500);
        c.put("payloadTemplate", "{\"t\":\"{{title}}\"}");
        assertThat(type.validate(c, Map.of("username", "u", "password", "p"))).isEmpty();
    }

    @Test
    @DisplayName("every invalid member is reported on its field")
    void invalidConfig() {
        Map<String, Object> c = new HashMap<>();
        c.put("brokerUrl", "http://broker.lan");
        c.put("topicPrefix", "a/+/b");
        c.put("haPrefix", "#");
        c.put("qos", 2);
        c.put("messageExpiry", "soon");
        c.put("summaryInterval", "5s");
        c.put("haMaxRules", 501);
        c.put("haMaxSources", -1);
        c.put("payloadTemplate", "{not json");
        List<String> fields = type.validate(c, Map.of("caCertPem", "garbage")).stream().map(FieldError::field).toList();
        assertThat(fields).contains("config.brokerUrl", "config.topicPrefix", "config.haPrefix", "config.qos",
                "config.messageExpiry", "config.summaryInterval", "config.haMaxRules", "config.haMaxSources",
                "config.payloadTemplate", "secrets.caCertPem");
    }

    @Test
    @DisplayName("broker URLs: credentials, a query or a non-WebSocket path are refused")
    void brokerUrlRules() {
        for (String bad : List.of("mqtt://u:p@broker", "mqtt://broker?x=1", "mqtt://broker/path", "ftp://broker",
                "mqtt://", "not a url")) {
            assertThat(type.validate(Map.of("brokerUrl", bad), Map.of())).as(bad)
                    .extracting(FieldError::field).contains("config.brokerUrl");
        }
        assertThat(type.validate(Map.of("brokerUrl", "mqtts://[::1]:8883"), Map.of())).isEmpty();
    }

    @Test
    @DisplayName("MQTT 3.1.1 needs a username with a password")
    void v311PasswordNeedsUser() {
        Map<String, Object> c = Map.of("brokerUrl", "mqtt://b", "protocolVersion", "3.1.1");
        assertThat(type.validate(c, Map.of("password", "p"))).extracting(FieldError::field)
                .containsExactly("secrets.username");
        assertThat(type.validate(Map.of("brokerUrl", "mqtt://b"), Map.of("password", "p"))).isEmpty();
    }

    @Test
    @DisplayName("the catalog entry lists every config and secret field; rate limit 50/s; not a fan-out type")
    void catalog() {
        assertThat(type.spec().configFields()).extracting("name").containsExactly("brokerUrl", "topicPrefix", "qos",
                "protocolVersion", "messageExpiry", "payloadTemplate", "publishHealth", "publishSummary",
                "summaryInterval", "haDiscovery", "haPrefix", "haMaxRules", "haMaxSources", "haRetainDiscovery");
        assertThat(type.spec().secretNames()).containsExactly("username", "password", "caCertPem");
        assertThat(type.rateLimit().capacity()).isEqualTo(50);
        assertThat(type.capabilities().fanOut()).isFalse();
    }

    // ------------------------------------------------------------------------- warnings

    @Test
    @DisplayName("MQTT_INSECURE_PUBLIC_BROKER for a public broker without TLS or credentials, never for a LAN one")
    void insecurePublicBrokerWarning() {
        MqttChannel withPolicy = new MqttChannel(TestOutbound.offlinePolicy());
        List<ChannelWarning> plain = withPolicy.warnings(Map.of("brokerUrl", "mqtt://8.8.8.8"), List.of("username"));
        assertThat(plain).extracting(ChannelWarning::code).containsExactly(MqttChannel.INSECURE_PUBLIC_BROKER);
        assertThat(withPolicy.warnings(Map.of("brokerUrl", "mqtts://8.8.8.8"), List.of()))
                .extracting(ChannelWarning::code).containsExactly(MqttChannel.INSECURE_PUBLIC_BROKER);
        assertThat(withPolicy.warnings(Map.of("brokerUrl", "mqtts://8.8.8.8"), List.of("username", "password")))
                .isEmpty();
        assertThat(withPolicy.warnings(Map.of("brokerUrl", "mqtt://192.168.1.10"), List.of())).isEmpty();
        assertThat(withPolicy.warnings(Map.of("brokerUrl", "mqtt://unresolvable.example"), List.of())).isEmpty();
        assertThat(type.warnings(Map.of("brokerUrl", "mqtt://8.8.8.8"), List.of())).isEmpty();
    }
}
