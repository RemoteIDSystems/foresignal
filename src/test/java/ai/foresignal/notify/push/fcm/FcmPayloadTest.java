package ai.foresignal.notify.push.fcm;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.push.NativeDeviceTarget;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import ai.foresignal.notify.push.PushTopic;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The FCM HTTP v1 message (prompt 032): data message, priority, TTL, collapse key, apns block. */
class FcmPayloadTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant NOW = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000c1");

    static PushMessage alert(String severity) {
        return new PushMessage("alert", 42L, 7L, RULE, severity, "M7.4 near the secret facility", "Quakes",
                "Full body with private details", "https://fs.example/v1/alerts/7", "foresignal://alerts/7", null,
                List.of("quake"), "foresignal-alert-7", PushTopic.alert(RULE.toString(), "usgs", "us7"), false,
                NOW.toEpochMilli(), null, null, null, "FORESIGNAL_ALERT");
    }

    static NativeDeviceTarget android(String level) {
        return new NativeDeviceTarget(UUID.randomUUID(), "fcm", "android", "ai.foresignal.app", null, "fcm-token-1",
                level, null);
    }

    JsonNode message(NativeDeviceTarget target, PushMessage message, boolean downgraded) {
        Map<String, Object> body = FcmPayloads.message(target, message, PushOptions.of(message, downgraded), NOW);
        return JSON.valueToTree(body).get("message");
    }

    @Test
    @DisplayName("an Android data message: string data with ids, severity, event, link and minimal text; no notification block")
    void dataMessage() {
        JsonNode m = message(android("minimal"), alert("warning"), false);
        assertThat(m.get("token").asString()).isEqualTo("fcm-token-1");
        assertThat(m.has("notification")).isFalse();
        assertThat(m.has("apns")).isFalse();
        JsonNode data = m.get("data");
        data.properties().forEach(e -> assertThat(e.getValue().isString()).as(e.getKey()).isTrue());
        assertThat(data.get("v").asString()).isEqualTo("1");
        assertThat(data.get("id").asString()).isEqualTo("42");
        assertThat(data.get("a").asString()).isEqualTo("7");
        assertThat(data.get("s").asString()).isEqualTo("warning");
        assertThat(data.get("ev").asString()).isEqualTo("alert");
        assertThat(data.get("l").asString()).isEqualTo("foresignal://alerts/7");
        assertThat(data.get("t").asString()).isEqualTo("Warning alert");
        assertThat(data.get("c").asString()).isEqualTo("FORESIGNAL_ALERT");
        JsonNode android = m.get("android");
        assertThat(android.get("priority").asString()).isEqualTo("HIGH");
        assertThat(android.get("ttl").asString()).isEqualTo("86400s");
        assertThat(android.get("collapse_key").asString()).startsWith("a-");
        assertThat(android.get("collapse_key").asString().getBytes(StandardCharsets.UTF_8).length)
                .isLessThanOrEqualTo(64);
        assertThat(m.toString()).doesNotContain("secret facility").doesNotContain("private details")
                .doesNotContain("Quakes");
        assertThat(JSON.writeValueAsBytes(m).length).isLessThanOrEqualTo(4096);
    }

    @Test
    @DisplayName("short content level puts the short title in t")
    void shortText() {
        JsonNode data = message(android("short"), alert("critical"), false).get("data");
        assertThat(data.get("t").asString()).isEqualTo("Quakes");
        assertThat(data.toString()).doesNotContain("secret facility");
    }

    @ParameterizedTest(name = "{0}, downgraded={1} -> {2}")
    @CsvSource({"info, false, NORMAL", "warning, false, HIGH", "critical, false, HIGH", "critical, true, NORMAL",
            "warning, true, NORMAL"})
    void priority(String severity, boolean downgraded, String expected) {
        PushMessage message = alert(severity);
        assertThat(FcmPayloads.priority(message, PushOptions.of(message, downgraded))).isEqualTo(expected);
    }

    @Test
    @DisplayName("an Apple device via FCM gets an apns block with the headers and aps payload")
    void appleViaFcm() {
        NativeDeviceTarget ios = new NativeDeviceTarget(UUID.randomUUID(), "fcm", "ios", "ai.foresignal.app", null,
                "fcm-token-2", "minimal", null);
        JsonNode apns = message(ios, alert("critical"), false).get("apns");
        assertThat(apns.get("headers").get("apns-push-type").asString()).isEqualTo("alert");
        assertThat(apns.get("headers").get("apns-priority").asString()).isEqualTo("10");
        assertThat(apns.get("headers").get("apns-collapse-id").asString()).startsWith("a-");
        assertThat(apns.get("headers").has("authorization")).isFalse();
        JsonNode aps = apns.get("payload").get("aps");
        assertThat(aps.get("alert").get("title").asString()).isEqualTo("Foresignal");
        assertThat(aps.get("mutable-content").asInt()).isEqualTo(1);
        assertThat(aps.get("interruption-level").asString()).isEqualTo("active");
        assertThat(apns.get("payload").get("e").get("id").asLong()).isEqualTo(42);
    }

    @Test
    @DisplayName("briefing TTL 43200s; system without alert id has no a")
    void ttlAndIds() {
        PushMessage briefing = new PushMessage("briefing", 5L, null, null, "info", "Brief", null, null, null, null,
                null, List.of(), "foresignal-briefing-5", null, false, 0, null, null);
        JsonNode m = message(android("minimal"), briefing, false);
        assertThat(m.get("android").get("ttl").asString()).isEqualTo("43200s");
        assertThat(m.get("data").has("a")).isFalse();
        assertThat(m.get("data").has("l")).isFalse();
        assertThat(m.get("data").get("t").asString()).isEqualTo("Briefing ready");
    }
}
