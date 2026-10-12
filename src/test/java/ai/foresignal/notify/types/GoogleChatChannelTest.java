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

/** The {@code google-chat} type (prompt 018). */
class GoogleChatChannelTest {

    final GoogleChatChannel type = new GoogleChatChannel();
    final Map<String, String> secrets = Map.of("webhookUrl", "https://chat.example.invalid/v1/spaces/AAA/messages?key=k&token=t");

    @Test
    @DisplayName("matches the golden Card v2 request")
    void golden() {
        assertGolden("google-chat", type.render(message(), settings(Map.of(), secrets)));
    }

    @Test
    @DisplayName("text is HTML-escaped; emoji pass; the subtitle carries the severity")
    void escapingAndSeverity() {
        var card = json(type.render(NotifyFixtures.message("critical", "🔥 <b> & _x_"), settings(Map.of(), secrets)))
                .get("cardsV2").get(0).get("card");
        assertThat(card.get("sections").get(0).get("widgets").get(0).get("textParagraph").get("text").asString())
                .isEqualTo("🔥 &lt;b&gt; &amp; <i>x</i>");
        assertThat(card.get("header").get("subtitle").asString()).startsWith("critical");
    }

    @Test
    @DisplayName("a 25,000-character body is cut on a safe boundary with a link; origin header present")
    void truncation() {
        var request = type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(25_000)), settings(Map.of(), secrets));
        String text = json(request).get("cardsV2").get(0).get("card").get("sections").get(0).get("widgets").get(0)
                .get("textParagraph").get("text").asString();
        assertThat(text.length()).isLessThanOrEqualTo(20_000);
        assertThat(text).endsWith("… <a href=\"https://fs.example/v1/alerts/7\">more</a>");
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
