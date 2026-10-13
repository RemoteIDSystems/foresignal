package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static ai.foresignal.notify.NotifyFixtures.withMap;
import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.NotifyProperties;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.net.TestOutbound;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code discord} with a map (prompt 055): {@code multipart/form-data} with {@code payload_json} (the embed
 * with {@code image.url = attachment://map.png} and {@code attachments[{id: 0, filename}]}) and the file in
 * {@code files[0]}, still {@code ?wait=true}; sent through the real {@link ChannelSender} to WireMock.
 */
class DiscordAttachmentTest extends WireMockSupport {

    static final JsonMapper JSON = JsonMapper.builder().build();

    final DiscordChannel type = new DiscordChannel();

    PreparedRequest render() {
        return type.render(withMap(message()), settings(Map.of(), Map.of("webhookUrl", url("/api/webhooks/1/T"))));
    }

    @Test
    @DisplayName("the request is multipart: payload_json first, then files[0] with the PNG")
    void structure() {
        PreparedRequest request = render();
        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.url()).endsWith("/api/webhooks/1/T?wait=true");
        PreparedRequest.Multipart body = (PreparedRequest.Multipart) request.body();
        assertThat(body.parts()).extracting(PreparedRequest.Part::name).containsExactly("payload_json", "files[0]");
        PreparedRequest.Part payloadPart = body.parts().get(0);
        assertThat(payloadPart.contentType()).isEqualTo("application/json");
        JsonNode payload = JSON.readTree(new String(payloadPart.bytes(), StandardCharsets.UTF_8));
        assertThat(payload.get("embeds").get(0).get("image").get("url").asString()).isEqualTo("attachment://map.png");
        assertThat(payload.get("attachments").get(0).get("id").asInt()).isZero();
        assertThat(payload.get("attachments").get(0).get("filename").asString()).isEqualTo("map.png");
        assertThat(payload.get("allowed_mentions").get("parse")).isEmpty();
        PreparedRequest.Part file = body.parts().get(1);
        assertThat(file.filename()).isEqualTo("map.png");
        assertThat(file.contentType()).isEqualTo("image/png");
        assertThat(file.bytes()).isEqualTo(NotifyFixtures.mapPng());
        assertThat(request.headers()).containsEntry("X-Foresignal-Origin", "abcd1234").doesNotContainKey("Content-Type");
    }

    @Test
    @DisplayName("on the wire: both parts arrive and the message id is still the reference")
    void wire() throws Exception {
        WIREMOCK.stubFor(post(urlEqualTo("/api/webhooks/1/T?wait=true")).willReturn(okJson("{\"id\":\"9001\"}")));
        ChannelSender sender = new ChannelSender(NotifyProperties.defaults(), TestOutbound.offlinePolicy(),
                TestOutbound.pinnedHttp());
        ChannelResponse response = sender.send(render());
        assertThat(response.success()).isTrue();
        assertThat(type.extractRef(response)).contains("9001");
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/api/webhooks/1/T?wait=true"))
                .withHeader("Content-Type", containing("multipart/form-data; boundary="))
                .withRequestBodyPart(aMultipart().withName("payload_json")
                        .withBody(equalToJson("{\"embeds\":[{\"image\":{\"url\":\"attachment://map.png\"}}]}", true, true))
                        .build())
                .withRequestBodyPart(aMultipart().withName("files[0]")
                        .withHeader("Content-Disposition", containing("filename=\"map.png\""))
                        .withBody(binaryEqualTo(NotifyFixtures.mapPng())).build()));
    }

    @Test
    @DisplayName("without an image the request is the unchanged JSON embed; attachments are a capability")
    void unchangedWithoutImage() {
        PreparedRequest request = type.render(message(), settings(Map.of(), Map.of("webhookUrl", url("/x"))));
        assertThat(request.body()).isInstanceOf(PreparedRequest.Bytes.class);
        assertThat(NotifyFixtures.json(request).get("embeds").get(0).has("image")).isFalse();
        assertThat(type.capabilities().attachments()).isTrue();
        assertThat(type.capabilities().editInPlace()).isTrue();
    }
}
