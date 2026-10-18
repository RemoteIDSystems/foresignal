package ai.foresignal.notify;

import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaStore;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Briefing audio on notifications (prompt 141), applied by the {@link DeliveryWorker} at send time.
 *
 * <p>A {@code briefing} notification whose run synthesized audio carries {@code data.audio} with
 * {@code status: ok} and the 043 {@code mediaId}. For a channel type that sends audio itself
 * ({@link ChannelType#audioAttachmentLimit()} at least the file's size: Telegram, Matrix, email, ntfy) the bytes
 * are loaded from the media store and attached; every other type, or a file over the type's limit, gets a
 * {@code [Listen](…)} link to {@code {publicBaseUrl}/v1/media/{id}} in the body. Nothing is attached when the run
 * has no audio, the media row is gone, or the message was reduced by the private-payload rule. The bytes are read
 * on every attempt, so a retry resends them.
 */
@Component
public class AudioAttachments {

    private static final Logger log = LoggerFactory.getLogger(AudioAttachments.class);

    /** The label of the audio link. */
    public static final String LINK_LABEL = "Listen";

    private final MediaStore media;
    private final ChannelService channels;

    /** Creates the service. */
    public AudioAttachments(MediaStore media, ChannelService channels) {
        this.media = media;
        this.channels = channels;
    }

    /**
     * {@code message} with the run's audio attached or linked for a channel of {@code type}.
     *
     * @param stored the stored notification (its {@code data} names the audio)
     * @param message the message as the channel would send it
     * @param type the channel's type
     */
    public NotificationMessage attach(NotificationMessage stored, NotificationMessage message, ChannelType type) {
        Optional<String> mediaId = mediaId(stored);
        if (mediaId.isEmpty() || isPrivate(message)) {
            return message;
        }
        Optional<MediaRow> row;
        try {
            row = media.find(mediaId.get());
        } catch (RuntimeException e) {
            log.warn("Briefing audio {} could not be looked up: {}", mediaId.get(), e.getMessage());
            return message;
        }
        if (row.isEmpty()) {
            return message;
        }
        MediaRow audio = row.get();
        String url = mediaUrl(audio.id());
        long limit = type.audioAttachmentLimit();
        if (limit > 0 && audio.bytes() <= limit) {
            try {
                byte[] bytes = Files.readAllBytes(media.file(audio));
                List<NotificationMessage.Attachment> attachments = new ArrayList<>(message.attachments());
                attachments.add(new NotificationMessage.Attachment(filename(audio.contentType()), audio.contentType(),
                        bytes, audio.id(), bytes.length, url));
                return message.withAttachments(attachments);
            } catch (IOException | RuntimeException e) {
                log.warn("Briefing audio {} could not be read; sending a link: {}", audio.id(), e.getMessage());
            }
        }
        return withLink(message, url);
    }

    /** The {@code mediaId} of a briefing notification's successful audio, if any. */
    static Optional<String> mediaId(NotificationMessage stored) {
        if (stored == null || !NotificationMessage.BRIEFING.equals(stored.event()) || stored.data() == null) {
            return Optional.empty();
        }
        if (stored.data().get("audio") instanceof Map<?, ?> audio && "ok".equals(audio.get("status"))
                && audio.get("mediaId") instanceof String id && !id.isBlank()) {
            return Optional.of(id);
        }
        return Optional.empty();
    }

    /** Appends a {@code [Listen](url)} markdown link to the body. */
    public static NotificationMessage withLink(NotificationMessage message, String url) {
        String link = "[" + LINK_LABEL + "](" + url + ")";
        String body = message.bodyMarkdown() == null || message.bodyMarkdown().isBlank() ? link
                : message.bodyMarkdown() + "\n\n" + link;
        return message.withText(message.title(), body);
    }

    /** {@code briefing.mp3} or {@code briefing.ogg} by content type. */
    static String filename(String contentType) {
        return contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).startsWith("audio/ogg")
                ? "briefing.ogg" : "briefing.mp3";
    }

    private static boolean isPrivate(NotificationMessage message) {
        return message.data() != null && Boolean.TRUE.equals(message.data().get("private"));
    }

    private String mediaUrl(String id) {
        String base;
        try {
            base = channels.serverSettings().normalizedPublicBaseUrl();
        } catch (RuntimeException e) {
            base = null;
        }
        return (base == null ? NotificationRouter.DEFAULT_BASE_URL : base) + "/v1/media/" + id;
    }
}
