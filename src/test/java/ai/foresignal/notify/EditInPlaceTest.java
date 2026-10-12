package ai.foresignal.notify;

import static ai.foresignal.notify.NotifyFixtures.json;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.types.DiscordChannel;
import ai.foresignal.notify.types.GotifyChannel;
import ai.foresignal.notify.types.MatrixChannel;
import ai.foresignal.notify.types.SlackChannel;
import ai.foresignal.notify.types.TelegramChannel;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** Lifecycle updates that edit the original message, and the follow-up fallbacks (prompt 018). */
class EditInPlaceTest {

    final NotificationMessage resolved = NotifyFixtures.update("alert.resolved");

    @Test
    @DisplayName("Discord edits with PATCH {webhookUrl}/messages/{id}, keeping the webhook's query")
    void discord() {
        var type = new DiscordChannel();
        var request = type.renderUpdate(resolved, "998877",
                settings(Map.of(), Map.of("webhookUrl", "https://discord.example.invalid/api/webhooks/1/TOK?thread_id=5"))).orElseThrow();
        assertThat(request.method()).isEqualTo("PATCH");
        assertThat(request.url()).isEqualTo("https://discord.example.invalid/api/webhooks/1/TOK/messages/998877?thread_id=5");
        JsonNode embed = json(request).get("embeds").get(0);
        assertThat(embed.get("title").asString()).startsWith("✅ Resolved");
        assertThat(embed.get("description").asString()).contains("~~M5.1 near Austin~~");
        assertThat(json(request).has("username")).isFalse();
    }

    @Test
    @DisplayName("Telegram calls editMessageText with the numeric message id; a photo reference edits the caption")
    void telegram() {
        var type = new TelegramChannel();
        var s = settings(Map.of("chatId", "-100123"), Map.of("botToken", "123456:TESTTOKEN"));
        var request = type.renderUpdate(resolved, "321", s).orElseThrow();
        assertThat(request.url()).isEqualTo("https://api.telegram.org/bot123456:TESTTOKEN/editMessageText");
        JsonNode body = json(request);
        assertThat(body.get("message_id").isNumber()).isTrue();
        assertThat(body.get("message_id").asLong()).isEqualTo(321L);
        assertThat(body.get("text").asString()).contains("<s>M5.1 near Austin</s>").startsWith("<b>✅ Resolved");
        var photo = type.renderUpdate(resolved, "photo:654", s).orElseThrow();
        assertThat(photo.url()).endsWith("/editMessageCaption");
        assertThat(json(photo).get("caption").asString().length()).isLessThanOrEqualTo(1024);
    }

    @Test
    @DisplayName("Matrix sends an m.replace event with '* ' bodies and m.new_content")
    void matrix() {
        var type = new MatrixChannel();
        var request = type.renderUpdate(resolved, "$orig:example.invalid", settings(
                Map.of("homeserverUrl", "https://matrix.example.invalid", "roomId", "!r:example.invalid"),
                Map.of("accessToken", "syt"))).orElseThrow();
        assertThat(request.method()).isEqualTo("PUT");
        assertThat(request.url()).endsWith("/send/m.room.message/99");
        JsonNode body = json(request);
        assertThat(body.get("body").asString()).startsWith("* ✅ Resolved");
        assertThat(body.get("formatted_body").asString()).startsWith("* <strong>");
        assertThat(body.get("m.new_content").get("body").asString()).startsWith("✅ Resolved");
        assertThat(body.get("m.new_content").has("m.relates_to")).isFalse();
        assertThat(body.get("m.relates_to").get("rel_type").asString()).isEqualTo("m.replace");
        assertThat(body.get("m.relates_to").get("event_id").asString()).isEqualTo("$orig:example.invalid");
    }

    @Test
    @DisplayName("types without edit support, and edits without a stored reference, fall back to a follow-up")
    void followUps() {
        for (ChannelType type : List.<ChannelType>of(new SlackChannel(), new GotifyChannel())) {
            assertThat(type.capabilities().editInPlace()).isFalse();
            assertThat(type.renderUpdate(resolved, "ref", settings(Map.of(), Map.of()))).isEmpty();
        }
        assertThat(new DiscordChannel().renderUpdate(resolved, null, settings(Map.of(), Map.of("webhookUrl", "https://d.invalid/x"))))
                .isEmpty();
        assertThat(new TelegramChannel().renderUpdate(resolved, " ", settings(Map.of("chatId", "1"), Map.of("botToken", "t"))))
                .isEmpty();
        assertThat(new MatrixChannel().renderUpdate(resolved, null, settings(Map.of(), Map.of()))).isEmpty();
    }
}
