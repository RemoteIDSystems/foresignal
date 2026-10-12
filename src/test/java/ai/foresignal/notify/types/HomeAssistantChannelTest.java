package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.json;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.NotifyFixtures;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** The {@code homeassistant} type (prompt 018). */
class HomeAssistantChannelTest {

    final HomeAssistantChannel type = new HomeAssistantChannel();
    final Map<String, Object> notify = Map.of("baseUrl", "https://ha.example.invalid", "mode", "notify", "service", "mobile_app_pixel");

    @Test
    @DisplayName("webhook mode matches the golden request with the default webhook body")
    void golden() {
        assertGolden("homeassistant", type.render(message(), settings(Map.of("baseUrl", "https://ha.example.invalid/"),
                Map.of("webhookId", "whTEST"))));
    }

    @Test
    @DisplayName("notify mode: bearer, title/message/data, the origin marker inside data")
    void notifyMode() {
        var request = type.render(NotifyFixtures.message("warning", "🔥 a_b"), settings(notify, Map.of("accessToken", "llat")));
        assertThat(request.url()).isEqualTo("https://ha.example.invalid/api/services/notify/mobile_app_pixel");
        assertThat(request.headers()).containsEntry("Authorization", "Bearer llat").containsEntry("X-Foresignal-Origin", "abcd1234");
        JsonNode body = json(request);
        assertThat(body.get("message").asString()).isEqualTo("🔥 a_b");
        assertThat(body.get("data").get("foresignalOrigin").asString()).isEqualTo("abcd1234");
        assertThat(body.get("data").has("push")).isFalse();
    }

    @Test
    @DisplayName("critical asks for the iOS critical sound and the Android alarm stream, each switchable")
    void critical() {
        JsonNode data = json(type.render(NotifyFixtures.message("critical", "x"), settings(notify, Map.of("accessToken", "t"))))
                .get("data");
        assertThat(data.get("push").get("sound").get("critical").asInt()).isEqualTo(1);
        assertThat(data.get("push").get("sound").get("volume").asDouble()).isEqualTo(1.0);
        assertThat(data.get("ttl").asInt()).isZero();
        assertThat(data.get("priority").asString()).isEqualTo("high");
        assertThat(data.get("channel").asString()).isEqualTo("alarm_stream");
        Map<String, Object> off = new java.util.HashMap<>(notify);
        off.put("iosCritical", false);
        off.put("androidAlarmStream", false);
        JsonNode plain = json(type.render(NotifyFixtures.message("critical", "x"), settings(off, Map.of("accessToken", "t")))).get("data");
        assertThat(plain.has("push")).isFalse();
        assertThat(plain.has("channel")).isFalse();
    }

    @Test
    @DisplayName("validation per mode; a 5,000-character body is sent whole")
    void validation() {
        assertThat(type.validate(Map.of("mode", "notify"), Map.of())).hasSize(2);
        assertThat(type.validate(Map.of(), Map.of())).singleElement().satisfies(e -> assertThat(e.field()).isEqualTo("secrets.webhookId"));
        assertThat(type.validate(notify, Map.of("accessToken", "t"))).isEmpty();
        assertThat(json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(notify, Map.of("accessToken", "t"))))
                .get("message").asString()).hasSize(5000);
    }
}
