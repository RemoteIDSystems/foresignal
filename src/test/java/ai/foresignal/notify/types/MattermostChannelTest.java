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

/** The {@code mattermost} type (prompt 018). */
class MattermostChannelTest {

    final MattermostChannel type = new MattermostChannel();
    final Map<String, String> secrets = Map.of("webhookUrl", "https://mattermost.example.invalid/hooks/xxxx");

    @Test
    @DisplayName("matches the golden request")
    void golden() {
        assertGolden("mattermost", type.render(message(), settings(Map.of("channel", "alerts"), secrets)));
    }

    @Test
    @DisplayName("markdown characters in text are backslash-escaped; emoji and < & pass")
    void escaping() {
        JsonNode body = json(type.render(NotifyFixtures.message("info", "🔥 a_b*c <x> & y"), settings(Map.of(), secrets)));
        assertThat(body.get("attachments").get(0).get("text").asString()).isEqualTo("🔥 a\\_b\\*c <x> & y");
    }

    @Test
    @DisplayName("the attachment color follows severity")
    void colors() {
        assertThat(json(type.render(NotifyFixtures.message("info", "x"), settings(Map.of(), secrets)))
                .get("attachments").get(0).get("color").asString()).isEqualTo("#3B82F6");
        assertThat(json(type.render(NotifyFixtures.message("critical", "x"), settings(Map.of(), secrets)))
                .get("attachments").get(0).get("color").asString()).isEqualTo("#DC2626");
    }

    @Test
    @DisplayName("a 5,000-character body fits the post limit unchanged; the origin header is present")
    void longBody() {
        var request = type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(Map.of(), secrets));
        assertThat(json(request).get("attachments").get(0).get("text").asString()).hasSize(5000);
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
