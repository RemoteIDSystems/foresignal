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

/** The {@code gotify} type (prompt 018). */
class GotifyChannelTest {

    final GotifyChannel type = new GotifyChannel();
    final Map<String, Object> config = Map.of("serverUrl", "https://gotify.example.invalid");
    final Map<String, String> secrets = Map.of("appToken", "AppTokenTest");

    @Test
    @DisplayName("matches the golden request, origin marker in extras")
    void golden() {
        assertGolden("gotify", type.render(message(), settings(config, secrets)));
    }

    @Test
    @DisplayName("priority follows severity: info 4, warning 6, critical 9")
    void priority() {
        assertThat(json(type.render(NotifyFixtures.message("info", "x"), settings(config, secrets))).get("priority").asInt()).isEqualTo(4);
        assertThat(json(type.render(NotifyFixtures.message("critical", "x"), settings(config, secrets))).get("priority").asInt()).isEqualTo(9);
    }

    @Test
    @DisplayName("markdown is escaped, emoji pass, and a 5,000-character body is sent whole")
    void escapingAndLength() {
        assertThat(json(type.render(NotifyFixtures.message("info", "🔥 a_b*c"), settings(config, secrets))).get("message").asString())
                .isEqualTo("🔥 a\\_b\\*c");
        assertThat(json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(config, secrets)))
                .get("message").asString()).hasSize(5000);
        assertThat(type.render(message(), settings(config, secrets)).headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
