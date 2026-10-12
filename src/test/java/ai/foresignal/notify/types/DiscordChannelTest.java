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
import tools.jackson.databind.JsonNode;

/** The {@code discord} type (prompt 018). */
class DiscordChannelTest {

    final DiscordChannel type = new DiscordChannel();
    final Map<String, String> secrets = Map.of("webhookUrl", "https://discord.example.invalid/api/webhooks/123/TOKEN");

    @Test
    @DisplayName("matches the golden embed request, with ?wait=true")
    void golden() {
        assertGolden("discord", type.render(message(), settings(Map.of("username", "Foresignal"), secrets)));
    }

    @Test
    @DisplayName("markdown characters are escaped, emoji pass, mentions are disabled")
    void escaping() {
        JsonNode body = json(type.render(NotifyFixtures.message("info", "🔥 a_b*c ~x~ @everyone"), settings(Map.of(), secrets)));
        assertThat(body.get("embeds").get(0).get("description").asString()).isEqualTo("🔥 a\\_b\\*c \\~x\\~ @everyone");
        assertThat(body.get("allowed_mentions").get("parse")).isEmpty();
    }

    @Test
    @DisplayName("a 5,000-character body is cut to 4,096 with a link")
    void truncation() {
        String d = json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(Map.of(), secrets)))
                .get("embeds").get(0).get("description").asString();
        assertThat(d.length()).isLessThanOrEqualTo(4096);
        assertThat(d).endsWith("… [more](https://fs.example/v1/alerts/7)");
    }

    @Test
    @DisplayName("color by severity; the message id is the reference; origin header present")
    void colorRefOrigin() {
        assertThat(json(type.render(NotifyFixtures.message("critical", "x"), settings(Map.of(), secrets)))
                .get("embeds").get(0).get("color").asInt()).isEqualTo(0xDC2626);
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"id\":\"1122334455\",\"type\":0}")))
                .contains("1122334455");
        assertThat(type.render(message(), settings(Map.of(), secrets)).headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
        assertThat(type.capabilities().editInPlace()).isTrue();
    }
}
