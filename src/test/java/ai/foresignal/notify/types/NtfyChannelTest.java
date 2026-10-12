package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.Truncator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code ntfy} type (prompt 018). */
class NtfyChannelTest {

    final NtfyChannel type = new NtfyChannel();
    final Map<String, Object> config = Map.of("serverUrl", "https://ntfy.example.invalid/", "topic", "alerts-test");
    final Map<String, String> secrets = Map.of("accessToken", "tk_test");

    @Test
    @DisplayName("matches the golden publish request with its headers, origin tag included")
    void golden() {
        assertGolden("ntfy", type.render(message(), settings(config, secrets)));
    }

    @Test
    @DisplayName("priority follows severity: info 3, warning 4, critical 5")
    void priority() {
        assertThat(type.render(NotifyFixtures.message("info", "x"), settings(config, Map.of())).headers()).containsEntry("Priority", "3");
        assertThat(type.render(NotifyFixtures.message("critical", "x"), settings(config, Map.of())).headers()).containsEntry("Priority", "5");
    }

    @Test
    @DisplayName("X-Call is sent on critical only, when configured")
    void call() {
        Map<String, Object> withCall = Map.of("topic", "t", "call", "yes");
        assertThat(type.render(NotifyFixtures.message("critical", "x"), settings(withCall, Map.of())).headers()).containsEntry("X-Call", "yes");
        assertThat(type.render(NotifyFixtures.message("warning", "x"), settings(withCall, Map.of())).headers()).doesNotContainKey("X-Call");
        assertThat(type.render(NotifyFixtures.message("critical", "x"), settings(Map.of("topic", "t"), Map.of())).headers())
                .doesNotContainKey("X-Call");
    }

    @Test
    @DisplayName("a non-ASCII title is RFC 2047 encoded; markdown is escaped in the body")
    void encoding() {
        var m = new ai.foresignal.notify.NotificationMessage(1L, "alert", "alert.fired", "Feuer 🔥 é", null, "a_b*c 🔥",
                "info", null, null, null, null, null, null, "abcd1234", NotifyFixtures.T, null, null, null);
        var request = type.render(m, settings(Map.of("topic", "t"), Map.of()));
        String title = request.headers().get("Title");
        assertThat(title).startsWith("=?UTF-8?B?").endsWith("?=");
        assertThat(new String(Base64.getDecoder().decode(title.substring(10, title.length() - 2)), StandardCharsets.UTF_8))
                .isEqualTo("Feuer 🔥 é");
        assertThat(request.bodyText()).isEqualTo("a\\_b\\*c 🔥");
        assertThat(request.url()).isEqualTo("https://ntfy.sh/t");
    }

    @Test
    @DisplayName("a 5,000-character body is cut to 4,096 UTF-8 bytes with a link")
    void truncation() {
        String body = type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(config, Map.of())).bodyText();
        assertThat(Truncator.size(body, Truncator.Unit.UTF8_BYTES)).isLessThanOrEqualTo(4096);
        assertThat(body).endsWith("… [more](https://fs.example/v1/alerts/7)");
    }
}
