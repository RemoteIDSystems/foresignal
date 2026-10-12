package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.json;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.HmacSigner;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** The {@code webhook} type (prompt 018). */
class WebhookChannelTest {

    final WebhookChannel type = new WebhookChannel();

    PreparedRequest render(Map<String, Object> config) {
        return type.render(message(), settings(config, Map.of("hmacSecret", "s3cret", "header.Authorization", "Bearer hdrsecret")));
    }

    @Test
    @DisplayName("matches the golden request: default body, delivery headers, configured and secret headers")
    void golden() {
        assertGolden("webhook", render(Map.of("url", "https://receiver.example.invalid/hook", "headers", Map.of("X-Static", "yes"))));
    }

    @Test
    @DisplayName("signs sha256=HMAC(\"<timestamp>.<body>\") with the secret")
    void signature() {
        PreparedRequest request = render(Map.of("url", "https://receiver.example.invalid/hook"));
        String timestamp = request.headers().get("X-Foresignal-Timestamp");
        byte[] body = ((PreparedRequest.Bytes) request.body()).bytes();
        assertThat(request.headers().get("X-Foresignal-Signature")).isEqualTo(HmacSigner.sign("s3cret", timestamp, body))
                .startsWith("sha256=").hasSize(7 + 64);
    }

    @Test
    @DisplayName("no signature without a secret; PUT when configured")
    void unsignedPut() {
        PreparedRequest request = type.render(message(), settings(Map.of("url", "https://r.example.invalid", "method", "PUT"), Map.of()));
        assertThat(request.method()).isEqualTo("PUT");
        assertThat(request.headers()).doesNotContainKey("X-Foresignal-Signature");
    }

    @Test
    @DisplayName("a body template substitutes JSON-escaped values, including emoji and quotes")
    void template() {
        var m = NotifyFixtures.message("critical", "say \"hi\" 🔥 <b>&_*");
        PreparedRequest request = type.render(m, settings(Map.of("url", "https://r.example.invalid",
                "bodyTemplate", "{\"t\":\"{{title}}\",\"b\":\"{{body}}\",\"s\":\"{{severity}}\",\"r\":\"{{data.ruleName}}\"}"), Map.of()));
        JsonNode body = json(request);
        assertThat(body.get("b").asString()).isEqualTo("say \"hi\" 🔥 <b>&_*");
        assertThat(body.get("s").asString()).isEqualTo("critical");
        assertThat(body.get("r").asString()).isEqualTo("Quakes");
        assertThat(body.get("t").asString()).isEqualTo(NotifyFixtures.TITLE);
    }

    @Test
    @DisplayName("a 5,000-character body is carried whole (webhooks have no limit) and the origin marker is present")
    void longBodyAndOrigin() {
        PreparedRequest request = type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)),
                settings(Map.of("url", "https://r.example.invalid"), Map.of()));
        JsonNode body = json(request);
        assertThat(body.get("message").get("bodyMarkdown").asString()).hasSize(5000);
        assertThat(body.get("foresignalOrigin").asString()).isEqualTo("abcd1234");
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }

    @Test
    @DisplayName("a template that does not render to JSON is refused")
    void invalidTemplate() {
        assertThat(type.validate(Map.of("bodyTemplate", "{not json"), Map.of())).singleElement()
                .satisfies(e -> assertThat(e.field()).isEqualTo("config.bodyTemplate"));
        assertThat(type.validate(Map.of("bodyTemplate", "{\"a\":\"{{title}}\"}"), Map.of())).isEmpty();
    }
}
