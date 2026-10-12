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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;

/** The {@code teams} type (prompt 018): Workflows URLs only, Adaptive Card 1.5. */
class TeamsChannelTest {

    final TeamsChannel type = new TeamsChannel();
    final Map<String, String> secrets = Map.of("workflowUrl",
            "https://prod-00.example.invalid/workflows/abc/triggers/manual/paths/invoke");

    @Test
    @DisplayName("matches the golden Adaptive Card request")
    void golden() {
        assertGolden("teams", type.render(message(), settings(Map.of(), secrets)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://contoso.webhook.office.com/webhookb2/abc@def/IncomingWebhook/123/456",
        "https://webhook.office.com/webhookb2/abc",
        "https://outlook.office.com/webhook/abc@def/IncomingWebhook/123/456",
        "HTTPS://Outlook.Office.com/WEBHOOK/x"
    })
    @DisplayName("Office 365 connector URLs are refused with a message pointing to Workflows")
    void retiredConnectors(String url) {
        assertThat(type.validate(Map.of(), Map.of("workflowUrl", url))).singleElement().satisfies(e -> {
            assertThat(e.field()).isEqualTo("secrets.workflowUrl");
            assertThat(e.message()).contains("Workflows");
        });
    }

    @Test
    @DisplayName("Workflows and other URLs are accepted")
    void workflowsAccepted() {
        assertThat(type.validate(Map.of(), secrets)).isEmpty();
        assertThat(type.validate(Map.of(), Map.of("workflowUrl", "https://outlook.office.com/mail"))).isEmpty();
    }

    @Test
    @DisplayName("the title color follows severity; text passes unescaped; long bodies keep a link")
    void severityAndText() {
        JsonNode card = json(type.render(NotifyFixtures.message("critical", "🔥 a_b <x> & y"), settings(Map.of(), secrets)))
                .get("attachments").get(0).get("content");
        assertThat(card.get("body").get(0).get("color").asString()).isEqualTo("Attention");
        assertThat(card.get("body").get(1).get("text").asString()).isEqualTo("🔥 a_b <x> & y");
        String longText = json(type.render(NotifyFixtures.message("info", NotifyFixtures.longBody(25_000)), settings(Map.of(), secrets)))
                .get("attachments").get(0).get("content").get("body").get(1).get("text").asString();
        assertThat(longText.length()).isLessThanOrEqualTo(20_000);
        assertThat(longText).endsWith("… [more](https://fs.example/v1/alerts/7)");
        assertThat(type.render(message(), settings(Map.of(), secrets)).headers()).containsEntry("X-Foresignal-Origin", "abcd1234");
    }
}
