package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.json;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotifyFixtures;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code matrix} type (prompt 018). */
class MatrixChannelTest {

    final MatrixChannel type = new MatrixChannel();
    final Map<String, Object> config = Map.of("homeserverUrl", "https://matrix.example.invalid", "roomId", "!room:example.invalid");
    final Map<String, String> secrets = Map.of("accessToken", "syt_test");

    @Test
    @DisplayName("matches the golden PUT with the delivery id as transaction id")
    void golden() {
        assertGolden("matrix", type.render(message(), settings(config, secrets)));
    }

    @Test
    @DisplayName("formatted_body escapes HTML; body is plain text; emoji pass")
    void escaping() {
        var body = json(type.render(NotifyFixtures.message("info", "🔥 <img src=x> & **b**"), settings(config, secrets)));
        assertThat(body.get("formatted_body").asString()).contains("🔥 &lt;img src=x&gt; &amp; <strong>b</strong>");
        assertThat(body.get("body").asString()).contains("🔥 <img src=x> & b");
    }

    @Test
    @DisplayName("a channel test uses a unique transaction id; event_id is the reference; origin header present")
    void testTxnAndRef() {
        ChannelSettings test = new ChannelSettings(NotifyFixtures.CHANNEL, "t", config, secrets, ZoneId.of("UTC"), 0,
                NotifyFixtures.T, "abcd1234", "UA");
        assertThat(type.render(message(), test).url()).endsWith("/send/m.room.message/test-" + NotifyFixtures.T.toEpochMilli());
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"event_id\":\"$abc:example.invalid\"}")))
                .contains("$abc:example.invalid");
        assertThat(type.render(message(), test).headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }

    @Test
    @DisplayName("a 5,000-character body is sent whole (no Matrix text limit at this size)")
    void longBody() {
        var body = json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(config, secrets)));
        assertThat(body.get("body").asString()).contains(NotifyFixtures.longBody(5000).strip());
    }
}
