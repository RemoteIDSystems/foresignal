package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static ai.foresignal.notify.NotifyFixtures.withMap;
import static com.github.tomakehurst.wiremock.client.WireMock.aMultipart;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.ChannelSettings;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.NotifyProperties;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.net.TestOutbound;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code pushover} with a map (prompt 055): the same fields as the form request, now as multipart parts in
 * the same order, plus the image as the {@code attachment} part.
 */
class PushoverAttachmentTest extends WireMockSupport {

    final PushoverChannel type = new PushoverChannel();

    ChannelSettings s() {
        return settings(Map.of("apiBaseUrl", baseUrl()), Map.of("appToken", "APP", "userKey", "USER"));
    }

    @Test
    @DisplayName("the multipart fields equal the form fields, followed by the attachment part")
    void structure() {
        PreparedRequest.Form form = (PreparedRequest.Form) type.render(message(), s()).body();
        PreparedRequest request = type.render(withMap(message()), s());
        assertThat(request.url()).isEqualTo(baseUrl() + "/1/messages.json");
        PreparedRequest.Multipart body = (PreparedRequest.Multipart) request.body();
        Map<String, String> fields = new LinkedHashMap<>();
        PreparedRequest.Part last = null;
        for (PreparedRequest.Part part : body.parts()) {
            if (part.filename() == null) {
                fields.put(part.name(), new String(part.bytes(), StandardCharsets.UTF_8));
            }
            last = part;
        }
        assertThat(fields).containsExactlyEntriesOf(form.fields());
        assertThat(last.name()).isEqualTo("attachment");
        assertThat(last.filename()).isEqualTo("map.png");
        assertThat(last.contentType()).isEqualTo("image/png");
        assertThat(last.bytes()).isEqualTo(NotifyFixtures.mapPng());
        assertThat(type.capabilities().attachments()).isTrue();
    }

    @Test
    @DisplayName("on the wire: token, user and the attachment arrive as parts")
    void wire() throws Exception {
        WIREMOCK.stubFor(post(urlEqualTo("/1/messages.json")).willReturn(okJson("{\"status\":1,\"request\":\"r\"}")));
        ChannelSender sender = new ChannelSender(NotifyProperties.defaults(), TestOutbound.offlinePolicy(),
                TestOutbound.pinnedHttp());
        assertThat(sender.send(type.render(withMap(message()), s())).success()).isTrue();
        WIREMOCK.verify(postRequestedFor(urlEqualTo("/1/messages.json"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBodyPart(aMultipart().withName("token").withBody(equalTo("APP")).build())
                .withRequestBodyPart(aMultipart().withName("user").withBody(equalTo("USER")).build())
                .withRequestBodyPart(aMultipart().withName("attachment")
                        .withHeader("Content-Type", equalTo("image/png"))
                        .withBody(binaryEqualTo(NotifyFixtures.mapPng())).build()));
    }
}
