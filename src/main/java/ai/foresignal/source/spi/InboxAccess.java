package ai.foresignal.source.spi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * The inbox directory as an adapter sees it: {@code ctx.inbox()} (prompt 049, Weather-Imagery-Design
 * §2.2 {@code INBOX_PATH}).
 *
 * <p>Adapters depend only on SPI types (Architecture §3), so the reads that a SatDump station's inbox
 * loop needs reach it through this interface; {@code ai.foresignal.source.InboxFiles} implements it.
 * Every method re-checks the realpath of what it touches against the inbox root, and every open uses
 * {@code NOFOLLOW_LINKS}, so a symlink planted after the {@code INBOX_PATH} parameter was validated
 * cannot redirect a read (the check runs per file and per scan, as §2.2 requires).
 */
public interface InboxAccess {

    /**
     * The size and modification time of one regular file, read without following a link.
     *
     * @param path the file
     * @param size its size in bytes
     * @param modified its last modification time
     */
    record FileStat(Path path, long size, Instant modified) {}

    /** The inbox root, as an absolute normalized path. */
    Path root();

    /**
     * Resolves {@code candidate} and confirms its realpath is inside the inbox.
     *
     * @return the realpath
     * @throws RuntimeException when it is outside the inbox or cannot be resolved
     */
    Path requireInside(Path candidate);

    /**
     * Opens a regular file for reading; a symlink is refused outright.
     *
     * @throws IOException when it cannot be opened
     * @throws RuntimeException when it is a link or outside the inbox
     */
    InputStream open(Path file) throws IOException;

    /** The regular files directly under {@code directory}, each re-checked; links are skipped. */
    List<Path> listRegularFiles(Path directory) throws IOException;

    /** The directories directly under {@code directory}, each re-checked; links are skipped. */
    List<Path> listDirectories(Path directory) throws IOException;

    /**
     * The size and modification time of {@code file}, read with {@code NOFOLLOW_LINKS} after the
     * realpath check.
     *
     * @throws IOException when it cannot be read
     */
    FileStat stat(Path file) throws IOException;

    /**
     * Reads a whole file through {@link #open}.
     *
     * @param maxBytes the largest file accepted
     * @throws IOException when it cannot be read or is larger than {@code maxBytes}
     */
    default byte[] readAll(Path file, long maxBytes) throws IOException {
        try (InputStream in = open(file)) {
            byte[] bytes = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, maxBytes + 1));
            if (bytes.length > maxBytes) {
                throw new IOException(file.getFileName() + " is larger than " + maxBytes + " bytes");
            }
            return bytes;
        }
    }
}
