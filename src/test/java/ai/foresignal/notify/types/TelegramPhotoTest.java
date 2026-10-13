package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static ai.foresignal.notify.NotifyFixtures.withMap;
import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.NotifyProperties;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.net.TestOutbound;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code telegram} with a map (prompt 055): {@code sendPhoto} multipart ({@code chat_id}, {@code photo},
 * {@code caption} in HTML mode at most 1,024 characters, {@code reply_markup}), the overflow text in a
 * follow-up {@code sendMessage}, and the reference {@code photo:<id>} so edits change the caption.
 */
class TelegramPhotoTest extends WireMockSupport {

    final TelegramChannel type = new TelegramChannel();

    ChannelSettings s() {
        return settings(Map.of("chatId", "-100", "apiBaseUrl", baseUrl()), Map.of("botToken", "TOK"));
    }

    static String field(PreparedRequest request, String name) {
        return ((PreparedRequest.Multipart) request.body()).parts().stream().filter(p -> p.name().equals(name))
                .map(p -> new String(p.bytes(), StandardCharsets.UTF_8)).findFirst().orElse(null);
    }

    @Test
    @DisplayName("sendPhoto multipart with the whole text as caption when it fits; no follow-up")
    void shortCaption() {
        PreparedRequest request = type.render(withMap(message()), s());
        assertThat(request.url()).endsWith("/botTOK/sendPhoto");
        PreparedRequest.Multipart body = (PreparedRequest.Multipart) request.body();
        assertThat(body.parts()).extracting(PreparedRequest.Part::name)
                .containsExactly("chat_id", "photo", "caption", "parse_mode");
        assertThat(field(request, "chat_id")).isEqualTo("-100");
        assertThat(field(request, "parse_mode")).isEqualTo("HTML");
        assertThat(field(request, "caption")).isEqualTo(TelegramChannel.text(withMap(message()), 4096));
        PreparedRequest.Part photo = body.parts().get(1);
        assertThat(photo.filename()).isEqualTo("map.png");
        assertThat(photo.bytes()).isEqualTo(NotifyFixtures.mapPng());
        assertThat(type.followUps(withMap(message()), s())).isEmpty();
    }

    @Test
    @DisplayName("a long text: the caption is the bold title (≤ 1,024) and the text follows in sendMessage")
    void overflow() {
        var long5000 = withMap(NotifyFixtures.message("warning", NotifyFixtures.longBody(3000)));
        PreparedRequest request = type.render(long5000, s());
        String caption = field(request, "caption");
        assertThat(caption.length()).isLessThanOrEqualTo(1024);
        assertThat(caption).startsWith("<b>").endsWith("</b>");
        List<PreparedRequest> followUps = type.followUps(long5000, s());
        assertThat(followUps).hasSize(1);
        assertThat(followUps.get(0).url()).endsWith("/botTOK/sendMessage");
        assertThat(NotifyFixtures.json(followUps.get(0)).get("text").asString()).contains("Lorem ipsum");
        assertThat(type.followUps(NotifyFixtures.message("warning", NotifyFixtures.longBody(3000)), s()))
                .as("no image, no follow-up").isEmpty();
    }

    @Test
    @DisplayName("action buttons travel as reply_markup JSON on the photo")
    void keyboard() {
        ChannelSettings withActions = s().withActions(List.of(new NotificationAction("ack", "Acknowledge",
                "https://fs.example/v1/actions/x", "tok123", null, Instant.parse("2026-05-01T13:00:00Z"))));
        assertThat(field(type.render(withMap(message()), withActions), "reply_markup"))
                .contains("\"callback_data\":\"ea:tok123\"");
    }

    @Test
    @DisplayName("on the wire, and the photo reference is photo:<id> so an edit uses editMessageCaption")
    void wireAndRef() throws Exception {
        WIREMOCK.stubFor(post(urlEqualTo("/botTOK/sendPhoto")).willReturn(okJson(
                "{\"ok\":true,\"result\":{\"message_id\":321,\"photo\":[{\"file_id\":\"f\"}]}}")));
        ChannelSender sender = new ChannelSender(NotifyProperties.defaults(), TestOutbound.offlinePolicy(),
                TestOutbound.pinnedHttp());
        ChannelResponse response = sender.send(type.render(withMap(message()), s()));
        assertThat(type.extractRef(response)).contains("photo:321");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/botTOK/sendPhoto"))
                .withRequestBodyPart(aMultipart().withName("chat_id").withBody(equalTo("-100")).build())
                .withRequestBodyPart(aMultipart().withName("photo").withBody(binaryEqualTo(NotifyFixtures.mapPng()))
                        .build()));
        assertThat(type.renderUpdate(NotifyFixtures.update("alert.resolved"), "photo:321", s()).orElseThrow().url())
                .endsWith("/editMessageCaption");
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(), "{\"ok\":true,\"result\":{\"message_id\":5}}")))
                .contains("5");
    }
}
