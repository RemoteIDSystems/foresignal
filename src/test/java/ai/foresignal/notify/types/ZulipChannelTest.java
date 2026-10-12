package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.assertGolden;
import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The {@code zulip} type (prompt 018). */
class ZulipChannelTest {

    final ZulipChannel type = new ZulipChannel();
    final Map<String, Object> config = Map.of("site", "https://zulip.example.invalid", "stream", "alerts",
            "botEmail", "bot@zulip.example.invalid");
    final Map<String, String> secrets = Map.of("apiKey", "zuliptestkey");

    Map<String, String> form(PreparedRequest request) {
        return ((PreparedRequest.Form) request.body()).fields();
    }

    @Test
    @DisplayName("matches the golden form request; the topic is the rule name")
    void golden() {
        assertGolden("zulip", type.render(message(), settings(config, secrets)));
    }

    @Test
    @DisplayName("topic templates: placeholders, a default without a rule, 60 characters at most")
    void topics() {
        assertThat(ZulipChannel.topic("{{severity}} / {{ruleName}}", message())).isEqualTo("warning / Quakes");
        NotificationMessage system = NotificationMessage.builder("system", "system.source_failing", "Source x is failing", "warning").build();
        assertThat(ZulipChannel.topic(ZulipChannel.DEFAULT_TOPIC, system)).isEqualTo("Foresignal");
        assertThat(ZulipChannel.topic("{{unknown}}", system)).isEqualTo("Foresignal");
        assertThat(ZulipChannel.topic("x".repeat(100), system)).hasSizeLessThanOrEqualTo(60);
    }

    @Test
    @DisplayName("markdown is escaped; a 15,000-character body is cut to 10,000 with a link; id is the reference")
    void escapingAndTruncation() {
        assertThat(form(type.render(NotifyFixtures.message("info", "🔥 a_b*c"), settings(config, secrets))).get("content"))
                .contains("🔥 a\\_b\\*c");
        String content = form(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(15_000)), settings(config, secrets)))
                .get("content");
        assertThat(content.length()).isLessThanOrEqualTo(10_000);
        assertThat(content).endsWith("… [more](https://fs.example/v1/alerts/7)");
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"id\":42,\"result\":\"success\"}"))).contains("42");
        assertThat(type.render(message(), settings(config, secrets)).headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
