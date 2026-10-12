package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.json;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.NotifyFixtures;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code apprise} type (prompt 018). */
class AppriseChannelTest {

    final AppriseChannel type = new AppriseChannel();
    final Map<String, Object> config = Map.of("serverUrl", "https://apprise.example.invalid", "configKey", "foresignal", "tag", "ops");

    @Test
    @DisplayName("matches the golden stateful request")
    void golden() {
        assertGolden("apprise", type.render(message(), settings(config, Map.of())));
    }

    @Test
    @DisplayName("stateless mode posts to /notify/ with the secret urls")
    void stateless() {
        var request = type.render(message(), settings(Map.of("serverUrl", "https://apprise.example.invalid"),
                Map.of("urls", "mailto://user:pass@example.invalid")));
        assertThat(request.url()).isEqualTo("https://apprise.example.invalid/notify/");
        assertThat(json(request).get("urls").asString()).isEqualTo("mailto://user:pass@example.invalid");
    }

    @Test
    @DisplayName("type follows severity: info, warning, failure; markdown escaped; long bodies whole; origin present")
    void severity() {
        assertThat(json(type.render(NotifyFixtures.message("critical", "x"), settings(config, Map.of()))).get("type").asString())
                .isEqualTo("failure");
        assertThat(json(type.render(NotifyFixtures.message("info", "🔥 a_b*c"), settings(config, Map.of()))).get("body").asString())
                .isEqualTo("🔥 a\\_b\\*c");
        assertThat(json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(config, Map.of())))
                .get("body").asString()).hasSize(5000);
        assertThat(type.render(message(), settings(config, Map.of())).headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }

    @Test
    @DisplayName("424 is a partial success; validation needs a key or urls")
    void partial() {
        assertThat(type.partialSuccess(new ChannelResponse(424, Map.of(), ""))).isTrue();
        assertThat(type.partialSuccess(new ChannelResponse(500, Map.of(), ""))).isFalse();
        assertThat(type.validate(Map.of(), Map.of())).hasSize(1);
        assertThat(type.validate(Map.of("configKey", "bad key!"), Map.of())).hasSize(1);
        assertThat(type.validate(Map.of(), Map.of("urls", "json://x"))).isEmpty();
    }
}
