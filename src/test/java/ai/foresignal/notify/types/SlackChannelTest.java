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

/** The {@code slack} type (prompt 018). */
class SlackChannelTest {

    final SlackChannel type = new SlackChannel();
    final Map<String, String> secrets = Map.of("webhookUrl", "https://hooks.example.invalid/services/T000/B000/XXXX");

    @Test
    @DisplayName("matches the golden Block Kit request")
    void golden() {
        assertGolden("slack", type.render(message(), settings(Map.of("username", "Foresignal"), secrets)));
    }

    @Test
    @DisplayName("mrkdwn escapes &, < and >; emoji pass through; snake_case stays text")
    void escaping() {
        JsonNode body = json(type.render(NotifyFixtures.message("info", "🔥 a<b & c>d snake_case_name *bold*"),
                settings(Map.of(), secrets)));
        String section = body.get("blocks").get(1).get("text").get("text").asString();
        assertThat(section).isEqualTo("🔥 a&lt;b &amp; c&gt;d snake_case_name _bold_");
    }

    @Test
    @DisplayName("a 5,000-character body is cut to Slack's 3,000-character section with a link")
    void truncation() {
        JsonNode body = json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(Map.of(), secrets)));
        String section = body.get("blocks").get(1).get("text").get("text").asString();
        assertThat(section.length()).isLessThanOrEqualTo(3000);
        assertThat(section).endsWith("… <https://fs.example/v1/alerts/7|more>");
    }

    @Test
    @DisplayName("the context line carries the severity; the origin marker is a header")
    void severityAndOrigin() {
        var request = type.render(NotifyFixtures.message("critical", "x"), settings(Map.of(), secrets));
        assertThat(json(request).get("blocks").get(2).get("elements").get(0).get("text").asString()).contains("*critical*");
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
        assertThat(type.capabilities().editInPlace()).isFalse();
    }
}
