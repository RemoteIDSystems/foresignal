package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static ai.foresignal.notify.NotifyFixtures.withMap;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.AlertMapService;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.NotifyProperties;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.net.TestOutbound;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code ntfy} with a map (prompt 055): {@code PUT {serverUrl}/{topic}} with the PNG as the body,
 * {@code Filename: map.png} and the text in the {@code Message} header (newlines as the literal
 * {@code \n} ntfy converts back, non-ASCII RFC 2047 encoded); a 413 or a 400 with code 40014 counts as a
 * refused attachment, and the text message is what the worker then sends.
 */
class NtfyAttachmentTest extends WireMockSupport {

    final NtfyChannel type = new NtfyChannel();

    ChannelSettings s() {
        return settings(Map.of("serverUrl", baseUrl(), "topic", "alerts"), Map.of());
    }

    @Test
    @DisplayName("PUT with the PNG body, Filename and the Message header; the publish headers are kept")
    void structure() {
        PreparedRequest request = type.render(withMap(message()), s());
        assertThat(request.method()).isEqualTo("PUT");
        assertThat(request.url()).isEqualTo(baseUrl() + "/alerts");
        assertThat(request.headers()).containsEntry("Filename", "map.png").containsEntry("Content-Type", "image/png")
                .containsEntry("Priority", "4").containsEntry("Markdown", "yes").containsKey("Title");
        assertThat(((PreparedRequest.Bytes) request.body()).bytes()).isEqualTo(NotifyFixtures.mapPng());
        String header = request.headers().get("Message");
        assertThat(header).doesNotContain("\n");
    }

    @Test
    @DisplayName("Message header: newlines become the literal \\n; non-ASCII is RFC 2047 encoded")
    void messageHeader() {
        assertThat(NtfyChannel.messageHeader("line one\nline two")).isEqualTo("line one\\nline two");
        assertThat(NtfyChannel.messageHeader("a\r\nb")).isEqualTo("a\\nb");
        assertThat(NtfyChannel.messageHeader("hail ≥ 2 in")).startsWith("=?UTF-8?B?").endsWith("?=");
    }

    @Test
    @DisplayName("413 and 400/40014 are refused attachments; other failures are not")
    void refusal() {
        assertThat(type.attachmentRefused(new ChannelResponse(413,
                Map.of(), "{\"code\":41301,\"http\":413,\"error\":\"attachment too large, or bandwidth limit reached\"}")))
                .isTrue();
        assertThat(type.attachmentRefused(new ChannelResponse(400, Map.of(),
                "{\"code\":40014,\"http\":400,\"error\":\"invalid request: attachments not allowed\"}"))).isTrue();
        assertThat(type.attachmentRefused(new ChannelResponse(400, Map.of(), "{\"code\":40001}"))).isFalse();
        assertThat(type.attachmentRefused(new ChannelResponse(500, Map.of(), "boom"))).isFalse();
    }

    @Test
    @DisplayName("413 on the wire, then the text fallback is the usual POST with a Map link")
    void fallbackOnWire() throws Exception {
        WIREMOCK.stubFor(put(urlEqualTo("/alerts")).willReturn(aResponse().withStatus(413)
                .withBody("{\"code\":41301,\"http\":413}")));
        ChannelSender sender = new ChannelSender(NotifyProperties.defaults(), TestOutbound.offlinePolicy(),
                TestOutbound.pinnedHttp());
        NotificationMessage withImage = withMap(message());
        ChannelResponse response = sender.send(type.render(withImage, s()));
        assertThat(response.status()).isEqualTo(413);
        assertThat(type.attachmentRefused(response)).isTrue();
        WIREMOCK.verify(putRequestedFor(urlEqualTo("/alerts")).withHeader("Filename", equalTo("map.png"))
                .withRequestBody(binaryEqualTo(NotifyFixtures.mapPng())));
        // What DeliveryWorker sends next: the message without the image, with a Map link.
        NotificationMessage text = AlertMapService.withLink(withImage.withAttachments(List.of()),
                withImage.image().url());
        PreparedRequest fallback = type.render(text, s());
        assertThat(fallback.method()).isEqualTo("POST");
        assertThat(fallback.bodyText()).contains(NotifyFixtures.MAP_URL);
        assertThat(fallback.headers()).doesNotContainKey("Filename");
        assertThat(type.capabilities().attachments()).isTrue();
    }
}
