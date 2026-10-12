package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code pushover} type (prompt 018). */
class PushoverChannelTest {

    final PushoverChannel type = new PushoverChannel();
    final Map<String, String> secrets = Map.of("appToken", "apptoken", "userKey", "userkey");

    Map<String, String> form(PreparedRequest request) {
        return ((PreparedRequest.Form) request.body()).fields();
    }

    @Test
    @DisplayName("matches the golden form request")
    void golden() {
        assertGolden("pushover", type.render(message(), settings(Map.of(), secrets)));
    }

    @Test
    @DisplayName("priority info 0, warning 1, critical 2 with retry 300 and expire 3600")
    void priority() {
        assertThat(form(type.render(NotifyFixtures.message("info", "x"), settings(Map.of(), secrets)))).containsEntry("priority", "0")
                .doesNotContainKey("retry");
        assertThat(form(type.render(NotifyFixtures.message("critical", "x"), settings(Map.of(), secrets))))
                .containsEntry("priority", "2").containsEntry("retry", "300").containsEntry("expire", "3600");
    }

    @Test
    @DisplayName("the receipt is the reference; resolve and acknowledge cancel it; other updates do not")
    void receiptCancel() {
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"status\":1,\"request\":\"r\",\"receipt\":\"rcpt1\"}")))
                .contains("rcpt1");
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"status\":1,\"request\":\"r\"}"))).isEmpty();
        var cancel = type.beforeUpdate(NotifyFixtures.update("alert.resolved"), "rcpt1", settings(Map.of(), secrets));
        assertThat(cancel).singleElement().satisfies(r -> {
            assertThat(r.url()).isEqualTo("https://api.pushover.net/1/receipts/rcpt1/cancel.json");
            assertThat(form(r)).containsEntry("token", "apptoken");
        });
        assertThat(type.beforeUpdate(NotifyFixtures.update("alert.acknowledged"), "rcpt1", settings(Map.of(), secrets))).hasSize(1);
        assertThat(type.beforeUpdate(NotifyFixtures.update("alert.snoozed"), "rcpt1", settings(Map.of(), secrets))).isEmpty();
        assertThat(type.beforeUpdate(NotifyFixtures.update("alert.resolved"), null, settings(Map.of(), secrets))).isEmpty();
        assertThat(type.renderUpdate(NotifyFixtures.update("alert.resolved"), "rcpt1", settings(Map.of(), secrets))).isEmpty();
    }

    @Test
    @DisplayName("a 5,000-character body is cut to 1,024 characters; HTML is escaped; origin header present")
    void truncationAndEscaping() {
        var request = type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(5000)), settings(Map.of(), secrets));
        assertThat(form(request).get("message").length()).isLessThanOrEqualTo(1024);
        assertThat(form(type.render(NotifyFixtures.message("info", "🔥 <x> & _y_"), settings(Map.of(), secrets))).get("message"))
                .isEqualTo("🔥 &lt;x&gt; &amp; <i>y</i>");
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
