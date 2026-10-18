package ai.foresignal.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaStore;
import ai.foresignal.notify.types.SlackChannel;
import ai.foresignal.notify.types.TelegramChannel;
import ai.foresignal.settings.ServerSettings;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** {@link AudioAttachments} (prompt 141) and the email {@code multipart/mixed} body of {@link ChannelSender}. */
class AudioAttachmentsTest {

    static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFB};
    static final String ID = "01JB0000000000000000000000";

    @TempDir Path dir;
    final MediaStore media = mock(MediaStore.class);
    final ChannelService channels = mock(ChannelService.class);
    final AudioAttachments attachments = new AudioAttachments(media, channels);
    MediaRow row;

    @BeforeEach
    void setUp() throws Exception {
        Path file = Files.write(dir.resolve("a.mp3"), MP3);
        row = new MediaRow(ID, "ab".repeat(32), "audio/mpeg", 0, 0, MP3.length, "ab/x.mp3", "audio", null, null, null,
                null, Instant.EPOCH, null, null, null);
        when(media.find(ID)).thenReturn(Optional.of(row));
        when(media.file(row)).thenReturn(file);
        when(channels.serverSettings()).thenReturn(new ServerSettings("https://fs.example"));
    }

    static NotificationMessage briefing(String status, boolean privatePayload) {
        Map<String, Object> audio = new LinkedHashMap<>();
        audio.put("status", status);
        audio.put("mediaId", ID);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("audio", audio);
        if (privatePayload) {
            data.put("private", true);
        }
        return NotificationMessage.builder("briefing", "briefing.run", "Morning", "info").body("Quiet night.")
                .data(data).build();
    }

    @Test
    @DisplayName("a type that carries audio gets the bytes attached with the media URL")
    void attached() {
        NotificationMessage m = briefing("ok", false);
        NotificationMessage out = attachments.attach(m, m, new TelegramChannel());

        assertThat(out.audio()).isNotNull().satisfies(a -> {
            assertThat(a.bytes()).isEqualTo(MP3);
            assertThat(a.filename()).isEqualTo("briefing.mp3");
            assertThat(a.url()).isEqualTo("https://fs.example/v1/media/" + ID);
        });
        assertThat(out.bodyMarkdown()).isEqualTo("Quiet night.");
    }

    @Test
    @DisplayName("a type without audio, or a file over the type's limit, gets a Listen link instead")
    void linked() {
        NotificationMessage m = briefing("ok", false);
        NotificationMessage out = attachments.attach(m, m, new SlackChannel());
        assertThat(out.attachments()).isEmpty();
        assertThat(out.bodyMarkdown()).isEqualTo("Quiet night.\n\n[Listen](https://fs.example/v1/media/" + ID + ")");

        MediaRow huge = new MediaRow(ID, row.sha256(), "audio/mpeg", 0, 0, 60L * 1024 * 1024, row.path(), "audio", null,
                null, null, null, Instant.EPOCH, null, null, null);
        when(media.find(ID)).thenReturn(Optional.of(huge));
        assertThat(attachments.attach(m, m, new TelegramChannel()).bodyMarkdown()).contains("[Listen](");
    }

    @Test
    @DisplayName("nothing for non-briefing messages, failed or skipped audio, private payloads or a vanished row")
    void untouched() {
        NotificationMessage alert = NotificationMessage.builder("alert", "alert.fired", "A", "info").build();
        assertThat(attachments.attach(alert, alert, new TelegramChannel())).isSameAs(alert);
        NotificationMessage failed = briefing("failed", false);
        assertThat(attachments.attach(failed, failed, new TelegramChannel())).isSameAs(failed);
        NotificationMessage hidden = briefing("ok", true);
        assertThat(attachments.attach(hidden, hidden, new TelegramChannel())).isSameAs(hidden);
        when(media.find(ID)).thenReturn(Optional.empty());
        NotificationMessage gone = briefing("ok", false);
        assertThat(attachments.attach(gone, gone, new TelegramChannel())).isSameAs(gone);
        assertThat(AudioAttachments.filename("audio/ogg")).isEqualTo("briefing.ogg");
        assertThat(AudioAttachments.filename(null)).isEqualTo("briefing.mp3");
    }

    @Test
    @DisplayName("an email with attachments is multipart/mixed: the body first, then each file as an attachment")
    void mixedMail() throws Exception {
        PreparedRequest.Mail mail = new PreparedRequest.Mail("smtp.example.invalid", 587, "starttls", null, null,
                "a@example.invalid", List.of("b@example.invalid"), "Morning", "text", "<p>html</p>", Map.of(), List.of(),
                List.of(new PreparedRequest.FileAttachment("briefing.mp3", "audio/mpeg", MP3)));

        MimeMultipart mixed = ChannelSender.mixedWithAttachments(mail);

        assertThat(mixed.getContentType()).startsWith("multipart/mixed");
        assertThat(mixed.getCount()).isEqualTo(2);
        MimeBodyPart body = (MimeBodyPart) mixed.getBodyPart(0);
        assertThat(((MimeMultipart) body.getContent()).getContentType()).startsWith("multipart/alternative");
        MimeBodyPart file = (MimeBodyPart) mixed.getBodyPart(1);
        assertThat(file.getDisposition()).isEqualTo("attachment");
        assertThat(file.getFileName()).isEqualTo("briefing.mp3");
        assertThat(file.getInputStream().readAllBytes()).isEqualTo(MP3);

        PreparedRequest.Mail plain = new PreparedRequest.Mail("smtp.example.invalid", 587, "starttls", null, null,
                "a@example.invalid", List.of("b@example.invalid"), "Morning", "text", null, Map.of(), List.of(),
                mail.attachments());
        MimeMultipart plainMixed = ChannelSender.mixedWithAttachments(plain);
        assertThat(((MimeBodyPart) plainMixed.getBodyPart(0)).getContent()).isEqualTo("text");
    }
}
