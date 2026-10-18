package ai.foresignal.media;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.MediaRole;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Stores generated documents that are not images in the content-addressed media store (prompt 141): the CZML the MCP
 * {@code get_track} tool links to instead of returning inline.
 *
 * <p>{@link MediaStore} accepts only images that pass {@link ImageGuard}; a document Foresignal wrote itself needs no
 * header inspection, only the same layout ({@code media/{ab}/{sha256}.{ext}}), the same hash stripe lock and a row
 * with role {@link MediaRole#DOCUMENT}, width and height 0, and an expiry so {@link MediaRetentionStep} removes it.
 * The row names the record it describes, so {@link MediaService#visible} hides it from callers who may not see that
 * record's source.
 */
@Component
public class MediaDocuments {

    /** The largest document, bytes. */
    public static final int MAX_BYTES = 32 * 1024 * 1024;

    /** How long a document is kept unless the caller says otherwise. */
    public static final Duration DEFAULT_LIFETIME = Duration.ofHours(24);

    private static final Pattern EXTENSION = Pattern.compile("^[a-z0-9]{1,8}$");

    private final MediaPaths paths;
    private final MediaQueries queries;
    private final MediaLocks locks;
    private final Clock clock;

    /** Creates the store. */
    public MediaDocuments(MediaPaths paths, MediaQueries queries, MediaLocks locks, Clock clock) {
        this.paths = paths;
        this.queries = queries;
        this.locks = locks;
        this.clock = clock;
    }

    /**
     * One document to store.
     *
     * @param bytes the content
     * @param contentType the content type served back (at most 32 characters, the column width)
     * @param extension the file extension, lower-case letters and digits
     * @param recordSourceId the source of the record the document describes, or null
     * @param recordExternalId the external id of that record, or null (both or neither)
     * @param lifetime how long to keep it; null for {@link #DEFAULT_LIFETIME}
     */
    public record Document(byte[] bytes, String contentType, String extension, String recordSourceId,
            String recordExternalId, Duration lifetime) {

        /** Validates the members. */
        public Document {
            Objects.requireNonNull(bytes, "bytes");
            Objects.requireNonNull(contentType, "contentType");
            if (contentType.length() > 32) {
                throw new IllegalArgumentException("A media content type is at most 32 characters");
            }
            if (extension == null || !EXTENSION.matcher(extension).matches()) {
                throw new IllegalArgumentException("A document extension is 1 to 8 lower-case letters or digits");
            }
            if ((recordSourceId == null) != (recordExternalId == null)) {
                throw new IllegalArgumentException("A record ref needs both its source id and its external id");
            }
            if (lifetime != null && (lifetime.isZero() || lifetime.isNegative())) {
                throw new IllegalArgumentException("A document lifetime must be positive");
            }
        }
    }

    /**
     * Stores {@code document} and inserts its row.
     *
     * @return the new row
     * @throws ForesignalException {@code 413 MEDIA_TOO_LARGE} above {@value #MAX_BYTES} bytes
     * @throws UncheckedIOException on an I/O error; nothing is left behind
     */
    public MediaRow store(Document document) {
        byte[] bytes = document.bytes();
        if (bytes.length > MAX_BYTES) {
            throw new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.MEDIA_TOO_LARGE,
                    "The document is " + bytes.length + " bytes; the limit is " + MAX_BYTES + " bytes.");
        }
        Instant now = clock.instant();
        String id = Ulids.next(now);
        String sha = HexFormat.of().formatHex(sha256().digest(bytes));
        String relative = MediaPaths.relative(sha, document.extension());
        Duration lifetime = document.lifetime() == null ? DEFAULT_LIFETIME : document.lifetime();
        MediaRow row = new MediaRow(id, sha, document.contentType(), 0, 0, bytes.length, relative,
                MediaRole.DOCUMENT.wireValue(), document.recordSourceId(), document.recordSourceId(),
                document.recordExternalId(), null, now, now.plus(lifetime), null, null);
        Path staged = paths.tmp().resolve(id + ".part");
        boolean placed = false;
        try {
            Files.createDirectories(staged.getParent());
            Files.write(staged, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                    StandardOpenOption.SYNC);
            locks.withLock(sha, () -> {
                place(staged, paths.resolve(relative));
                queries.insert(row);
                return null;
            });
            placed = true;
            return row;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store a media document: " + e.getMessage(), e);
        } finally {
            if (!placed) {
                try {
                    Files.deleteIfExists(staged);
                } catch (IOException ignored) {
                    // The orphan sweep removes stale staging files.
                }
            }
        }
    }

    /** Moves the staged file into place, or drops it when the content-addressed file exists. */
    private static void place(Path staged, Path target) {
        try {
            if (Files.exists(target)) {
                Files.deleteIfExists(staged);
                return;
            }
            Files.createDirectories(target.getParent());
            try {
                Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (FileAlreadyExistsException e) {
                Files.deleteIfExists(staged);
            } catch (AtomicMoveNotSupportedException e) {
                throw new UncheckedIOException("The media directory does not support atomic renames", e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not move a media document into " + target + ": " + e.getMessage(), e);
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
