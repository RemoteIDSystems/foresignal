package ai.foresignal.media;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.Ulids;
import ai.foresignal.source.spi.ImageRejectedException;
import ai.foresignal.source.spi.MediaInput;
import ai.foresignal.source.spi.MediaRef;
import ai.foresignal.source.spi.MediaRole;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * The content-addressed media store (prompt 043, Weather-Imagery-Design §2.3, §2.4).
 *
 * <p>{@link #store(MediaInput)}:
 *
 * <ol>
 *   <li>streams the content to {@code media/tmp/}, hashing it with SHA-256 while writing and refusing
 *       more than {@code foresignal.media.max-file-bytes} with {@code 413 MEDIA_TOO_LARGE};
 *   <li>forces the staged file to disk and runs {@link ImageGuard} on it; a refusal deletes it;
 *   <li>atomically renames it to {@code media/{ab}/{sha256}.{ext}}, or, when that file exists, reuses
 *       it and deletes the staged copy (content addressing);
 *   <li>always inserts a new row.
 * </ol>
 *
 * <p>Steps 3 and 4 hold the hash's {@link MediaLocks} stripe, so retention never unlinks a file that
 * a store just reused. Nothing is decoded here: the guard reads headers only.
 */
@Component
public class MediaStore {

    private static final Logger log = LoggerFactory.getLogger(MediaStore.class);

    private final MediaPaths paths;
    private final MediaQueries queries;
    private final ImageGuard guard;
    private final MediaLocks locks;
    private final Clock clock;

    /** Creates the store. */
    public MediaStore(MediaPaths paths, MediaQueries queries, ImageGuard guard, MediaLocks locks, Clock clock) {
        this.paths = paths;
        this.queries = queries;
        this.guard = guard;
        this.locks = locks;
        this.clock = clock;
    }

    /** The layout. */
    public MediaPaths paths() {
        return paths;
    }

    /**
     * Stores one image.
     *
     * @return the new row's reference
     * @throws ImageRejectedException when the guard refuses the image; nothing is left behind
     * @throws ForesignalException {@code 413 MEDIA_TOO_LARGE} when the content exceeds the cap
     * @throws UncheckedIOException on an I/O error; nothing is left behind
     */
    public MediaRef store(MediaInput input) {
        return store(input, null, null).toRef();
    }

    /**
     * Stores one image as a variant of {@code variantOf} at {@code variantWidth} (thumbnails), or as an
     * original when both are null.
     */
    MediaRow store(MediaInput input, String variantOf, Integer variantWidth) {
        Instant now = clock.instant();
        String id = Ulids.next(now);
        long cap = guard.maxFileBytes();
        long known = input.knownSize();
        if (known > cap) {
            throw tooLarge(known, cap);
        }
        Path staged = paths.tmp().resolve(id + ".part");
        boolean placed = false;
        try {
            MessageDigest digest = sha256();
            long written = 0;
            try (InputStream in = input.open();
                    OutputStream out = new DigestOutputStream(Files.newOutputStream(staged,
                            StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE), digest)) {
                byte[] buffer = new byte[64 * 1024];
                int n;
                while ((n = in.read(buffer)) > 0) {
                    written += n;
                    if (written > cap) {
                        throw tooLarge(written, cap);
                    }
                    out.write(buffer, 0, n);
                }
            }
            try (FileChannel channel = FileChannel.open(staged, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            ImageInfo info = guard.inspect(staged, input.contentType(), input.allowMultiImage());
            String sha = HexFormat.of().formatHex(digest.digest());
            String relative = MediaPaths.relative(sha, info.format().extension());
            long size = written;
            MediaRow row = new MediaRow(id, sha, info.format().contentType(), info.width(), info.height(), size,
                    relative, input.role().wireValue(), input.sourceId(), input.recordSourceId(),
                    input.recordExternalId(), input.licence(), now, input.expiresAt(), variantOf, variantWidth);
            locks.withLock(sha, () -> {
                place(staged, paths.resolve(relative));
                queries.insert(row);
                return null;
            });
            placed = true;
            return row;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store media: " + e.getMessage(), e);
        } finally {
            if (!placed) {
                deleteQuietly(staged);
            }
        }
    }

    /**
     * Inserts a row that reuses {@code original}'s file as its variant at {@code width}: a thumbnail
     * request for an image already smaller than the target links the original instead of upscaling.
     */
    MediaRow link(MediaRow original, int width) {
        Instant now = clock.instant();
        MediaRow row = new MediaRow(Ulids.next(now), original.sha256(), original.contentType(), original.width(),
                original.height(), original.bytes(), original.path(), MediaRole.THUMBNAIL.wireValue(),
                original.sourceId(), original.recordSourceId(), original.recordExternalId(), original.licence(), now,
                original.expiresAt(), original.id(), width);
        return locks.withLock(original.sha256(), () -> {
            if (!Files.exists(paths.resolve(original.path()))) {
                throw MediaErrors.expired(original.id());
            }
            queries.insert(row);
            return row;
        });
    }

    /** The row with {@code id}. */
    public Optional<MediaRow> find(String id) {
        return queries.find(id);
    }

    /** The absolute file of a row. */
    public Path file(MediaRow row) {
        return paths.resolve(row.path());
    }

    /** Moves the staged file into place, or drops it when the content-addressed file exists. */
    private void place(Path staged, Path target) {
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
                // media/tmp is inside media/, so this only happens on an unusual mount; fail rather than
                // risk a half-written file under its final name.
                throw new UncheckedIOException("The media directory does not support atomic renames", e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not move media into " + target + ": " + e.getMessage(), e);
        }
    }

    private static ForesignalException tooLarge(long size, long cap) {
        return new ForesignalException(HttpStatus.CONTENT_TOO_LARGE, ErrorCodes.MEDIA_TOO_LARGE,
                "The image is " + (size > cap ? "over " : "") + size + " bytes; the limit is " + cap
                        + " bytes (foresignal.media.max-file-bytes).");
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete the staged media file {}; the orphan sweep removes it", file, e);
        }
    }
}
