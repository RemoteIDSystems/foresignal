package ai.foresignal.notify.push.apns;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.push.NativeContent;
import ai.foresignal.notify.push.PushMessage;
import ai.foresignal.notify.push.PushOptions;
import ai.foresignal.notify.push.PushTopic;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

/** The APNs body and headers (prompt 032): content levels, interruption levels, priority, limits. */
class ApnsPayloadTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant NOW = Instant.parse("2026-05-01T12:00:00Z");
    static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    static final String SECRET_TITLE = "M7.4 near the secret facility at grid 12345";

    static PushMessage alert(String severity, String shortTitle) {
        return new PushMessage("alert", 42L, 7L, RULE, severity, SECRET_TITLE, shortTitle,
                "Full body with private details " + "x".repeat(900), "https://fs.example/v1/alerts/7",
                "foresignal://alerts/7", null, List.of("quake"), "foresignal-alert-7",
                PushTopic.alert(RULE.toString(), "usgs", "us7"), false, NOW.toEpochMilli(), null, null, null,
                "FORESIGNAL_ALERT");
    }

    static PushOptions options(PushMessage message, boolean downgraded) {
        return PushOptions.of(message, downgraded).withDispatchId(99L);
    }

    JsonNode body(PushMessage message, boolean downgraded, String level, boolean timeSensitive) {
        return JSON.readTree(ApnsPayloads.fit(JSON, message, options(message, downgraded), level, timeSensitive));
    }

    @Test
    @DisplayName("minimal: title Foresignal, body '<Severity> alert'; ids, severity, event and link in e")
    void minimal() {
        JsonNode body = body(alert("warning", "Quakes"), false, "minimal", false);
        JsonNode aps = body.get("aps");
        assertThat(aps.get("alert").get("title").asString()).isEqualTo("Foresignal");
        assertThat(aps.get("alert").get("body").asString()).isEqualTo("Warning alert");
        assertThat(aps.get("sound").asString()).isEqualTo("default");
        assertThat(aps.get("mutable-content").asInt()).isEqualTo(1);
        assertThat(aps.get("thread-id").asString()).isEqualTo("foresignal-" + RULE);
        assertThat(aps.get("category").asString()).isEqualTo("FORESIGNAL_ALERT");
        assertThat(aps.has("content-available")).isFalse();
        JsonNode e = body.get("e");
        assertThat(e.get("v").asInt()).isEqualTo(1);
        assertThat(e.get("id").asLong()).isEqualTo(42);
        assertThat(e.get("a").asLong()).isEqualTo(7);
        assertThat(e.get("s").asString()).isEqualTo("warning");
        assertThat(e.get("ev").asString()).isEqualTo("alert");
        assertThat(e.get("l").asString()).isEqualTo("foresignal://alerts/7");
        assertThat(body.toString()).doesNotContain("secret facility").doesNotContain("private details")
                .doesNotContain("Quakes");
    }

    @Test
    @DisplayName("short (opt-in): title 'Foresignal · <severity>', body the short title cut to 40 characters")
    void shortLevel() {
        JsonNode body = body(alert("critical", "A very long rule name that goes well past forty characters"),
                false, "short", false);
        JsonNode alert = body.get("aps").get("alert");
        assertThat(alert.get("title").asString()).isEqualTo("Foresignal · critical");
        assertThat(alert.get("body").asString()).hasSizeLessThanOrEqualTo(NativeContent.SHORT_TITLE)
                .startsWith("A very long rule name");
        assertThat(body.toString()).doesNotContain("secret facility").doesNotContain("private details");
    }

    @Test
    @DisplayName("briefing and system minimal texts")
    void otherEvents() {
        PushMessage briefing = new PushMessage("briefing", 5L, null, null, "info", "Morning brief", null, "secret",
                null, null, null, List.of(), "foresignal-briefing-5", PushTopic.briefing("b"), false, 0, null, null);
        PushMessage system = new PushMessage("system", 6L, null, null, "warning", "Source failing", null,
                "system.source_failing", null, null, null, List.of(), "foresignal-system-6", null, false, 0, null, null);
        assertThat(body(briefing, false, "short", false).get("aps").get("alert").get("body").asString())
                .isEqualTo("Briefing ready");
        JsonNode sys = body(system, false, "minimal", false);
        assertThat(sys.get("aps").get("alert").get("body").asString()).isEqualTo("System notice");
        assertThat(sys.get("aps").get("thread-id").asString()).isEqualTo("foresignal-system");
        assertThat(sys.get("aps").get("category").asString()).isEqualTo("FORESIGNAL_SYSTEM");
        assertThat(sys.get("e").has("a")).isTrue();
        assertThat(sys.get("e").get("a").isNull()).isTrue();
    }

    @ParameterizedTest(name = "{0}, downgraded={1}, timeSensitive={2} -> {3}")
    @CsvSource({
        "info, false, false, passive",
        "warning, false, false, active",
        "critical, false, false, active",
        "critical, false, true, time-sensitive",
        "warning, true, false, passive",
        "critical, true, true, passive",
    })
    void interruptionLevels(String severity, boolean downgraded, boolean timeSensitive, String expected) {
        assertThat(ApnsPayloads.interruptionLevel(severity, downgraded, timeSensitive)).isEqualTo(expected);
        assertThat(expected).isNotEqualTo("critical");
    }

    @Test
    @DisplayName("quiet-hours downgrade: passive, no sound, priority 5; otherwise priority 10")
    void downgrade() {
        PushMessage message = alert("warning", "Quakes");
        JsonNode body = body(message, true, "minimal", true);
        assertThat(body.get("aps").get("interruption-level").asString()).isEqualTo("passive");
        assertThat(body.get("aps").has("sound")).isFalse();
        assertThat(ApnsPayloads.priority(options(message, true))).isEqualTo("5");
        assertThat(ApnsPayloads.priority(options(message, false))).isEqualTo("10");
    }

    @Test
    @DisplayName("headers: topic, push type alert, expiration by TTL, collapse id <= 64 bytes, apns-id from the dispatch")
    void headers() {
        PushMessage message = alert("critical", "Quakes");
        Map<String, String> headers = ApnsPayloads.headers("JWT", "ai.foresignal.app", message, options(message, false),
                NOW);
        assertThat(headers).containsEntry("authorization", "bearer JWT").containsEntry("apns-topic", "ai.foresignal.app")
                .containsEntry("apns-push-type", "alert").containsEntry("apns-priority", "10")
                .containsEntry("apns-expiration", Long.toString(NOW.plus(Duration.ofHours(24)).getEpochSecond()));
        String collapse = headers.get("apns-collapse-id");
        assertThat(collapse).startsWith("a-").isEqualTo("a-" + message.topic());
        assertThat(collapse.getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
        assertThat(headers.get("apns-id")).isEqualTo(ApnsPayloads.apnsId(99L).toString())
                .matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        assertThat(ApnsPayloads.apnsId(99L)).isEqualTo(ApnsPayloads.apnsId(99L));
        assertThat(ApnsPayloads.expiration(PushOptions.of(new PushMessage("briefing", 1L, null, null, "info", "t", null,
                null, null, null, null, List.of(), "t", null, false, 0, null, null), false), NOW))
                .isEqualTo(Long.toString(NOW.plus(Duration.ofHours(12)).getEpochSecond()));
        assertThat(ApnsPayloads.expiration(PushOptions.of(new PushMessage("system", 1L, null, null, "info", "t", null,
                null, null, null, null, List.of(), "t", null, false, 0, null, null), false), NOW))
                .isEqualTo(Long.toString(NOW.plus(Duration.ofHours(1)).getEpochSecond()));
    }

    @Test
    @DisplayName("the body never exceeds 4096 bytes: a huge deep link is dropped first")
    void budget() {
        PushMessage base = alert("warning", "Quakes");
        PushMessage huge = new PushMessage(base.event(), base.id(), base.alertId(), base.ruleId(), base.severity(),
                base.title(), base.shortTitle(), base.body(), base.linkApi(), "foresignal://alerts/" + "9".repeat(5000),
                null, base.tags(), base.tag(), base.topic(), false, base.ts(), null, null, 3, base.category());
        byte[] bytes = ApnsPayloads.fit(JSON, huge, options(huge, false), "short", false);
        assertThat(bytes.length).isLessThanOrEqualTo(ApnsPayloads.MAX_BYTES);
        JsonNode body = JSON.readTree(bytes);
        assertThat(body.get("e").has("l")).isFalse();
        assertThat(body.get("aps").get("badge").asInt()).isEqualTo(3);
        assertThat(ApnsPayloads.fit(JSON, base, options(base, false), "short", false).length)
                .isLessThanOrEqualTo(ApnsPayloads.MAX_BYTES);
    }
}
