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

/** The {@code telegram} type (prompt 018). */
class TelegramChannelTest {

    final TelegramChannel type = new TelegramChannel();
    final Map<String, Object> config = Map.of("chatId", "-100123");
    final Map<String, String> secrets = Map.of("botToken", "123456:TESTTOKEN");

    @Test
    @DisplayName("matches the golden sendMessage request")
    void golden() {
        assertGolden("telegram", type.render(message(), settings(config, secrets)));
    }

    @Test
    @DisplayName("only allowed tags are produced and text entities are escaped")
    void escaping() {
        String text = json(type.render(NotifyFixtures.message("info", "🔥 <script>alert(1)</script> & *x* ~~y~~"),
                settings(config, secrets))).get("text").asString();
        assertThat(text).contains("🔥 &lt;script&gt;alert(1)&lt;/script&gt; &amp; <i>x</i> <s>y</s>");
        assertThat(text).doesNotContain("<script>");
    }

    @Test
    @DisplayName("a 5,000-character body is cut to 4,096 characters with tags closed and a link")
    void truncation() {
        String text = json(type.render(NotifyFixtures.message("info", "**" + NotifyFixtures.longBody(5000) + "**"),
                settings(config, secrets))).get("text").asString();
        assertThat(text.length()).isLessThanOrEqualTo(4096);
        assertThat(text).endsWith("</b>… <a href=\"https://fs.example/v1/alerts/7\">more</a>");
    }

    @Test
    @DisplayName("result.message_id is the reference; a custom API base is used; origin header present")
    void refAndBase() {
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"ok\":true,\"result\":{\"message_id\":321}}")))
                .contains("321");
        var request = type.render(message(), settings(Map.of("chatId", "1", "apiBaseUrl", "http://localhost:9/"), secrets));
        assertThat(request.url()).isEqualTo("http://localhost:9/bot123456:TESTTOKEN/sendMessage");
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
