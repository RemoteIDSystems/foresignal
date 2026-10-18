package ai.foresignal.notify.types;

import static ai.foresignal.notify.NotifyFixtures.message;
import static ai.foresignal.notify.NotifyFixtures.settings;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.AudioUpload;
import ai.foresignal.notify.ChannelResponse;
import ai.foresignal.notify.NotificationMessage;
import ai.foresignal.notify.NotifyFixtures;
import ai.foresignal.notify.PreparedRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** Briefing audio on the channel types that carry it (prompt 141): Telegram, Matrix, email and ntfy. */
class AudioChannelsTest {

    static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFB};
    static final byte[] OGG = {'O', 'g', 'g', 'S', 0, 2};
    static final String URL = "https://fs.example/v1/media/01JB0000000000000000000000";

    static NotificationMessage withAudio(NotificationMessage m, String contentType, byte[] bytes) {
        String name = contentType.startsWith("audio/ogg") ? "briefing.ogg" : "briefing.mp3";
        return m.withAttachments(List.of(new NotificationMessage.Attachment(name, contentType, bytes,
                "01JB0000000000000000000000", bytes.length, URL)));
    }

    static PreparedRequest.Part part(PreparedRequest request, String name) {
        return ((PreparedRequest.Multipart) request.body()).parts().stream().filter(p -> p.name().equals(name))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("audio is not an image: image() skips it, audio() finds it")
    void imageVersusAudio() {
        NotificationMessage m = withAudio(message(), "audio/mpeg", MP3);
        assertThat(m.image()).isNull();
        assertThat(m.audio()).isNotNull().satisfies(a -> assertThat(a.isAudio()).isTrue());
        assertThat(NotifyFixtures.withMap(message()).audio()).isNull();
        assertThat(NotifyFixtures.withMap(message()).image()).isNotNull();
    }

    @Test
    @DisplayName("telegram: MP3 goes out with sendAudio (audio part, caption, title, performer); its reference allows caption edits")
    void telegramSendAudio() {
        TelegramChannel type = new TelegramChannel();
        PreparedRequest request = type.render(withAudio(message(), "audio/mpeg", MP3),
                settings(Map.of("chatId", "-100"), Map.of("botToken", "1:T")));

        assertThat(request.url()).endsWith("/bot1:T/sendAudio");
        assertThat(part(request, "audio").bytes()).isEqualTo(MP3);
        assertThat(part(request, "audio").filename()).isEqualTo("briefing.mp3");
        assertThat(part(request, "audio").contentType()).isEqualTo("audio/mpeg");
        assertThat(new String(part(request, "caption").bytes(), StandardCharsets.UTF_8)).contains("Quake &lt;M5&gt;");
        assertThat(part(request, "performer")).isNotNull();
        assertThat(part(request, "voice")).isNull();
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(),
                "{\"ok\":true,\"result\":{\"message_id\":9,\"audio\":{\"file_id\":\"x\"}}}"))).contains("photo:9");
        assertThat(type.audioAttachmentLimit()).isEqualTo(50L * 1024 * 1024);
    }

    @Test
    @DisplayName("telegram: Ogg Opus goes out with sendVoice; a long text follows the caption as sendMessage")
    void telegramSendVoice() {
        TelegramChannel type = new TelegramChannel();
        NotificationMessage m = withAudio(message("info", NotifyFixtures.longBody(3000)), "audio/ogg", OGG);
        var s = settings(Map.of("chatId", "-100"), Map.of("botToken", "1:T"));

        PreparedRequest request = type.render(m, s);

        assertThat(request.url()).endsWith("/sendVoice");
        assertThat(part(request, "voice").bytes()).isEqualTo(OGG);
        assertThat(part(request, "title")).isNull();
        assertThat(type.followUps(m, s)).singleElement().satisfies(f -> assertThat(f.url()).endsWith("/sendMessage"));
        assertThat(type.extractRef(new ChannelResponse(200, Map.of(),
                "{\"ok\":true,\"result\":{\"message_id\":3,\"voice\":{}}}"))).contains("photo:3");
    }

    @Test
    @DisplayName("matrix: the text event as usual, then an upload whose content_uri becomes an m.audio event")
    void matrixUpload() {
        MatrixChannel type = new MatrixChannel();
        var s = settings(Map.of("homeserverUrl", "https://matrix.example.invalid/", "roomId", "!r:example.invalid"),
                Map.of("accessToken", "syt_test"));
        NotificationMessage m = withAudio(message(), "audio/mpeg", MP3);

        assertThat(type.render(m, s).url()).contains("/send/m.room.message/99");
        Optional<AudioUpload> upload = type.audioUpload(m, s);

        assertThat(upload).isPresent();
        PreparedRequest up = upload.get().upload();
        assertThat(up.method()).isEqualTo("POST");
        assertThat(up.url()).isEqualTo("https://matrix.example.invalid/_matrix/media/v3/upload?filename=briefing.mp3");
        assertThat(up.headers()).containsEntry("Authorization", "Bearer syt_test").containsEntry("Content-Type", "audio/mpeg");
        assertThat(((PreparedRequest.Bytes) up.body()).bytes()).isEqualTo(MP3);

        PreparedRequest post = upload.get().then().apply(new ChannelResponse(200, Map.of(),
                "{\"content_uri\":\"mxc://example.invalid/abc\"}")).orElseThrow();
        assertThat(post.method()).isEqualTo("PUT");
        assertThat(post.url()).endsWith("/send/m.room.message/99-audio");
        JsonNode content = NotifyFixtures.json(post);
        assertThat(content.get("msgtype").asString()).isEqualTo("m.audio");
        assertThat(content.get("url").asString()).isEqualTo("mxc://example.invalid/abc");
        assertThat(content.at("/info/mimetype").asString()).isEqualTo("audio/mpeg");
        assertThat(content.at("/info/size").asInt()).isEqualTo(MP3.length);

        assertThat(upload.get().then().apply(new ChannelResponse(200, Map.of(), "{\"content_uri\":\"https://x\"}")))
                .as("only mxc URIs").isEmpty();
        assertThat(type.audioUpload(message(), s)).isEmpty();
    }

    @Test
    @DisplayName("email: the audio is a file attachment, also in plain-text mode")
    void emailAttachment() {
        EmailChannel type = new EmailChannel();
        Map<String, Object> config = new java.util.HashMap<>(Map.of("smtpHost", "smtp.example.invalid", "smtpPort", 587,
                "tls", "starttls", "from", "foresignal@example.invalid", "to", List.of("ops@example.invalid")));
        NotificationMessage m = withAudio(message(), "audio/mpeg", MP3);

        PreparedRequest.Mail mail = (PreparedRequest.Mail) type.render(m, settings(config, Map.of())).body();
        assertThat(mail.attachments()).singleElement().satisfies(a -> {
            assertThat(a.filename()).isEqualTo("briefing.mp3");
            assertThat(a.contentType()).isEqualTo("audio/mpeg");
            assertThat(a.bytes()).isEqualTo(MP3);
        });
        assertThat(mail.inlines()).as("audio is never an inline image").isEmpty();

        config.put("plainTextOnly", true);
        PreparedRequest.Mail plain = (PreparedRequest.Mail) type.render(m, settings(config, Map.of())).body();
        assertThat(plain.html()).isNull();
        assertThat(plain.attachments()).hasSize(1);
        assertThat(((PreparedRequest.Mail) type.render(message(), settings(config, Map.of())).body()).attachments())
                .isEmpty();
    }

    @Test
    @DisplayName("ntfy: the audio is PUT as the body with Filename; the text travels in the Message header")
    void ntfyAttachment() {
        NtfyChannel type = new NtfyChannel();
        PreparedRequest request = type.render(withAudio(message(), "audio/ogg", OGG),
                settings(Map.of("serverUrl", "https://ntfy.example.invalid/", "topic", "alerts-test"), Map.of()));

        assertThat(request.method()).isEqualTo("PUT");
        assertThat(request.headers()).containsEntry("Filename", "briefing.ogg").containsEntry("Content-Type", "audio/ogg")
                .containsKey("Message");
        assertThat(((PreparedRequest.Bytes) request.body()).bytes()).isEqualTo(OGG);
        assertThat(type.audioAttachmentLimit()).isEqualTo(15L * 1024 * 1024);
    }

    @Test
    @DisplayName("types without audio support keep the default limit 0 (they get a Listen link)")
    void others() {
        assertThat(new SlackChannel().audioAttachmentLimit()).isZero();
        assertThat(new DiscordChannel().audioAttachmentLimit()).isZero();
        assertThat(new SlackChannel().audioUpload(withAudio(message(), "audio/mpeg", MP3), settings(Map.of(), Map.of())))
                .isEmpty();
    }
}
