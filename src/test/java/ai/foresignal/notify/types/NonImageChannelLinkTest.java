package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.MAP_URL;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.AlertMapService;
import ai.foresignal.notify.ChannelType;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;

/**
 * The channel types without native images (prompt 055; Weather-Imagery-Design §8): the map arrives as the
 * text plus an in-app link {@code {publicBaseUrl}/v1/media/{id}} in the body, which every one of them
 * renders, and none of them declares the attachment capability. The webhook default body also carries
 * {@code attachments[{mediaId, url, contentType, bytes}]}.
 */
class NonImageChannelLinkTest {

    /** The link-only message as {@code AlertMapService} prepares it. */
    static NotificationMessage linked() {
        return AlertMapService.withLink(NotifyFixtures.withMapLink(NotifyFixtures.message()), MAP_URL);
    }

    static Stream<Arguments> types() {
        return Stream.of(
                Arguments.of(new SlackChannel(), Map.of(), Map.of("webhookUrl", "https://h.example.invalid/x")),
                Arguments.of(new MattermostChannel(), Map.of(), Map.of("webhookUrl", "https://h.example.invalid/x")),
                Arguments.of(new TeamsChannel(), Map.of(), Map.of("workflowUrl", "https://prod.example.invalid/x")),
                Arguments.of(new GoogleChatChannel(), Map.of(), Map.of("webhookUrl", "https://h.example.invalid/x")),
                Arguments.of(new GotifyChannel(), Map.of("serverUrl", "https://g.example.invalid"),
                        Map.of("appToken", "t")),
                Arguments.of(new MatrixChannel(), Map.of("homeserverUrl", "https://m.example.invalid", "roomId", "!r:x"),
                        Map.of("accessToken", "t")),
                Arguments.of(new WebhookChannel(), Map.of("url", "https://r.example.invalid/h"), Map.of()),
                Arguments.of(new HomeAssistantChannel(), Map.of("baseUrl", "https://ha.example.invalid"),
                        Map.of("webhookId", "w")),
                Arguments.of(new HomeAssistantChannel(), Map.of("baseUrl", "https://ha.example.invalid", "mode",
                        "notify", "service", "mobile_app_x"), Map.of("accessToken", "t")),
                Arguments.of(new AppriseChannel(), Map.of("serverUrl", "https://a.example.invalid"), Map.of()),
                Arguments.of(new ZulipChannel(), Map.of("site", "https://z.example.invalid", "stream", "s", "topic", "t",
                        "email", "b@z.example.invalid"), Map.of("apiKey", "k")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("types")
    @DisplayName("the text carries the media link; the type declares no image support")
    void linkInText(ChannelType type, Map<String, Object> config, Map<String, String> secrets) {
        PreparedRequest request = type.render(linked(), settings(config, secrets));
        String body = request.bodyText() != null ? request.bodyText()
                : request.body() instanceof PreparedRequest.Form form ? form.fields().toString() : "";
        assertThat(body).contains("01JA0000000000000000000000");
        assertThat(type.capabilities().attachments()).isFalse();
    }

    @Test
    @DisplayName("webhook: the default body has attachments[{mediaId, url, contentType, bytes}]")
    void webhookMetadata() {
        PreparedRequest request = new WebhookChannel().render(linked(),
                settings(Map.of("url", "https://r.example.invalid/h"), Map.of()));
        JsonNode attachments = NotifyFixtures.json(request).get("attachments");
        assertThat(attachments).hasSize(1);
        assertThat(attachments.get(0).get("mediaId").asString()).isEqualTo("01JA0000000000000000000000");
        assertThat(attachments.get(0).get("url").asString()).isEqualTo(MAP_URL);
        assertThat(attachments.get(0).get("contentType").asString()).isEqualTo("image/png");
        assertThat(attachments.get(0).get("bytes").asLong()).isEqualTo(4321);
        JsonNode message = NotifyFixtures.json(request).get("message").get("attachments").get(0);
        assertThat(message.get("url").asString()).isEqualTo(MAP_URL);
        assertThat(message.get("bytes").asLong()).isEqualTo(4321);
        PreparedRequest plain = new WebhookChannel().render(NotifyFixtures.message(),
                settings(Map.of("url", "https://r.example.invalid/h"), Map.of()));
        assertThat(NotifyFixtures.json(plain).has("attachments")).as("no map, no member").isFalse();
    }

    @Test
    @DisplayName("the five image types declare the capability")
    void imageTypes() {
        for (ChannelType type : List.<ChannelType>of(new DiscordChannel(), new TelegramChannel(), new NtfyChannel(),
                new PushoverChannel(), new EmailChannel())) {
            assertThat(type.capabilities().attachments()).as(type.id()).isTrue();
        }
    }
}
