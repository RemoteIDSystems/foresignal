package ai.foresignal.media;

import ai.foresignal.source.spi.ImageRejectedException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * The audio formats the media store accepts for {@code role=audio} (prompt 141): MP3 and Ogg (the container of
 * the Opus speech that OpenAI-compatible {@code /audio/speech} endpoints return for {@code response_format=opus}).
 * Recognized by their first bytes; nothing is decoded.
 */
public enum AudioFormat {

    /** MPEG-1/2 Layer III: an {@code ID3} tag or an MPEG frame sync ({@code FF Ex}/{@code FF Fx}). */
    MP3("audio/mpeg", "mp3"),

    /** Ogg, magic {@code OggS}. */
    OGG("audio/ogg", "ogg");

    /** The rejection tag of a file that is not the declared audio format. */
    public static final String TAG_TYPE_MISMATCH = "audio-type-mismatch";

    private final String contentType;
    private final String extension;

    AudioFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    /** The canonical content type. */
    public String contentType() {
        return contentType;
    }

    /** The file extension of stored files. */
    public String extension() {
        return extension;
    }

    /**
     * The format of a declared content type ({@code audio/mpeg}, {@code audio/mp3}, {@code audio/ogg},
     * {@code audio/opus}); parameters and case are ignored.
     */
    public static Optional<AudioFormat> ofContentType(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        String base = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return switch (base) {
            case "audio/mpeg", "audio/mp3" -> Optional.of(MP3);
            case "audio/ogg", "audio/opus" -> Optional.of(OGG);
            default -> Optional.empty();
        };
    }

    /** The format the first bytes show, or empty. */
    public static Optional<AudioFormat> sniff(byte[] head, int length) {
        if (length >= 4 && head[0] == 'O' && head[1] == 'g' && head[2] == 'g' && head[3] == 'S') {
            return Optional.of(OGG);
        }
        if (length >= 3 && head[0] == 'I' && head[1] == 'D' && head[2] == '3') {
            return Optional.of(MP3);
        }
        if (length >= 2 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xE0) == 0xE0) {
            return Optional.of(MP3);
        }
        return Optional.empty();
    }

    /**
     * Checks a staged file against its declared type.
     *
     * @return the format
     * @throws ImageRejectedException ({@value #TAG_TYPE_MISMATCH}) when the declared type is not an audio type or
     *     the content is not that format
     */
    public static AudioFormat inspect(Path file, String declaredContentType) {
        Optional<AudioFormat> declared = ofContentType(declaredContentType);
        byte[] head = new byte[4];
        int read;
        try (InputStream in = Files.newInputStream(file)) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read staged audio: " + e.getMessage(), e);
        }
        Optional<AudioFormat> sniffed = sniff(head, read);
        if (declared.isEmpty() || sniffed.isEmpty() || declared.get() != sniffed.get()) {
            throw new ImageRejectedException(TAG_TYPE_MISMATCH, "The audio is not " + (declaredContentType == null
                    ? "a supported audio type" : declaredContentType) + " (MP3 and Ogg are accepted).");
        }
        return declared.get();
    }
}
