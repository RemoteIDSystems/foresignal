package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static ai.foresignal.notify.NotifyFixtures.withMap;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.ChannelSender;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.NotifyProperties;
import ai.foresignal.notify.PreparedRequest;
import ai.foresignal.testsupport.net.TestOutbound;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.BodyPart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code email} with a map (prompt 055): the HTML references {@code cid:map}, and the message sent to an
 * in-process GreenMail server is {@code multipart/alternative} of the plain text and a
 * {@code multipart/related} holding the HTML and the PNG with {@code Content-ID: <map>}.
 */
class EmailInlineCidTest {

    final EmailChannel type = new EmailChannel();

    Map<String, Object> config(int port) {
        return Map.of("smtpHost", "127.0.0.1", "smtpPort", port, "tls", "none", "from", "fs@example.invalid",
                "to", List.of("ops@example.invalid"));
    }

    @Test
    @DisplayName("the rendered mail has <img src=\"cid:map\"> and one inline image")
    void rendered() {
        PreparedRequest.Mail mail = (PreparedRequest.Mail) type.render(withMap(message()), settings(config(25),
                Map.of())).body();
        assertThat(mail.html()).contains("<img src=\"cid:map\"");
        assertThat(mail.inlines()).hasSize(1);
        assertThat(mail.inlines().get(0).contentId()).isEqualTo("map");
        assertThat(mail.inlines().get(0).bytes()).isEqualTo(NotifyFixtures.mapPng());
        PreparedRequest.Mail plain = (PreparedRequest.Mail) type.render(message(), settings(config(25), Map.of())).body();
        assertThat(plain.inlines()).isEmpty();
        assertThat(plain.html()).doesNotContain("cid:");
        Map<String, Object> textOnly = new HashMap<>(config(25));
        textOnly.put("plainTextOnly", true);
        PreparedRequest.Mail winlink = (PreparedRequest.Mail) type.render(withMap(message()), settings(textOnly,
                Map.of())).body();
        assertThat(winlink.html()).isNull();
        assertThat(winlink.inlines()).as("plain text only: no image").isEmpty();
    }

    @Test
    @DisplayName("on the wire: alternative { text/plain, related { text/html, image/png <map> } }")
    void structureOverSmtp() throws Exception {
        GreenMail server = new GreenMail(ServerSetupTest.SMTP.dynamicPort());
        server.start();
        try {
            ChannelSender sender = new ChannelSender(NotifyProperties.defaults(), TestOutbound.offlinePolicy(),
                    TestOutbound.pinnedHttp());
            int port = server.getSmtp().getPort();
            assertThat(sender.send(type.render(withMap(message()), settings(config(port), Map.of()))).status())
                    .isEqualTo(ChannelSender.SMTP_OK);
            assertThat(server.waitForIncomingEmail(5_000, 1)).isTrue();
            MimeMessage received = server.getReceivedMessages()[0];
            assertThat(received.getContentType()).startsWith("multipart/alternative");
            MimeMultipart alternative = (MimeMultipart) received.getContent();
            assertThat(alternative.getCount()).isEqualTo(2);
            assertThat(alternative.getBodyPart(0).getContentType()).startsWith("text/plain");
            BodyPart relatedPart = alternative.getBodyPart(1);
            assertThat(relatedPart.getContentType()).startsWith("multipart/related");
            MimeMultipart related = (MimeMultipart) relatedPart.getContent();
            assertThat(related.getCount()).isEqualTo(2);
            assertThat(related.getBodyPart(0).getContentType()).startsWith("text/html");
            assertThat((String) related.getBodyPart(0).getContent()).contains("cid:map");
            MimeBodyPart image = (MimeBodyPart) related.getBodyPart(1);
            assertThat(image.getContentType()).startsWith("image/png");
            assertThat(image.getContentID()).isEqualTo("<map>");
            assertThat(image.getDisposition()).isEqualTo(Part.INLINE);
            assertThat(image.getFileName()).isEqualTo("map.png");
            try (InputStream in = image.getInputStream()) {
                assertThat(in.readAllBytes()).isEqualTo(NotifyFixtures.mapPng());
            }
        } finally {
            server.stop();
        }
    }
}
