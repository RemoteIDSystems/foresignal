package ai.foresignal.source;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.source.spi.InboxAccess;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Reads files from the inbox directory without letting a symlink escape it.
 *
 * <p>Validating an {@code INBOX_PATH} parameter once is not enough. Validation happens when an
 * operator saves a setting; the read happens minutes or days later, and in between, anything that
 * can write to the inbox — which is the point of an inbox — could replace a file with a symlink to
 * {@code /etc/shadow} or to the secret key. So every path is re-checked immediately before it is
 * opened, and opened with {@code NOFOLLOW_LINKS} so the check cannot be invalidated between the
 * check and the open.
 *
 * <p>Re-checking per file and per scan is what Weather-Imagery-Design §2.2 requires, and the reason
 * is exactly this time-of-check/time-of-use gap.
 */
@Component
public class InboxFiles implements InboxAccess {

    private final Path root;

    /**
     * Creates the reader for the configured inbox. Marked {@code @Autowired} because the class has a
     * second, test-only constructor, and Spring cannot choose between two otherwise.
     */
    @Autowired
    public InboxFiles(ForesignalProperties properties) {
        this(properties.inboxDir());
    }

    InboxFiles(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    /**
     * Reads files under another Foresignal-owned root with the same checks (prompt 054: the SatDump
     * upload area {@code rasters/satdump/}).
     *
     * @param root the root every path must stay inside
     */
    public static InboxFiles rootedAt(Path root) {
        return new InboxFiles(root);
    }

    /** The inbox root, as an absolute normalized path. */
    @Override
    public Path root() {
        return root;
    }

    /**
     * Resolves {@code candidate} and confirms its realpath is inside the inbox.
     *
     * @throws InvalidInputException when it is outside, or cannot be resolved
     */
    @Override
    public Path requireInside(Path candidate) {
        Path real = realPath(candidate);
        Path realRoot = realPath(root);
        if (!real.startsWith(realRoot)) {
            throw new InvalidInputException(
                    "'"
                            + candidate
                            + "' resolves to "
                            + real
                            + ", which is outside the inbox directory "
                            + realRoot
                            + ". Inbox paths may not point outside it, directly or through a symlink.");
        }
        return real;
    }

    /**
     * Opens a file for reading, refusing a symlink outright.
     *
     * <p>A symlink is rejected even when its target is inside the inbox: the inbox is writable by
     * design, so a link there is attacker-controllable and its target can change after the check.
     */
    @Override
    public InputStream open(Path file) throws IOException {
        // Checked on the path as given, before resolving: requireInside returns a realpath, which
        // by definition is never a symlink, so testing the resolved path would always pass.
        if (Files.isSymbolicLink(file)) {
            throw new InvalidInputException("'" + file + "' is a symbolic link and will not be read.");
        }
        Path checked = requireInside(file);
        // Open the realpath, not the path as given, so the bytes read come from exactly the file
        // whose location was just confirmed. NOFOLLOW_LINKS then closes the remaining gap: if the
        // final component is swapped for a link between the check and the open, the open fails
        // rather than following it.
        return Files.newInputStream(checked, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Lists the regular files directly under {@code directory}, each re-checked.
     *
     * <p>Symlinks are skipped rather than failing the scan: one planted link should not stop an
     * inbox from being processed.
     */
    @Override
    public List<Path> listRegularFiles(Path directory) throws IOException {
        Path checked = requireInside(directory);
        try (Stream<Path> entries = Files.list(checked)) {
            return entries
                    .filter(path -> !Files.isSymbolicLink(path))
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(this::isInsideQuietly)
                    .sorted()
                    .toList();
        }
    }

    /**
     * Lists the directories directly under {@code directory}, each re-checked (prompt 049).
     *
     * <p>Symlinked directories are skipped, like symlinked files in {@link #listRegularFiles}.
     */
    @Override
    public List<Path> listDirectories(Path directory) throws IOException {
        Path checked = requireInside(directory);
        try (Stream<Path> entries = Files.list(checked)) {
            return entries
                    .filter(path -> !Files.isSymbolicLink(path))
                    .filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(this::isInsideQuietly)
                    .sorted()
                    .toList();
        }
    }

    /**
     * The size and modification time of a regular file, read without following a link (prompt 049).
     *
     * @throws InvalidInputException when it is a link or outside the inbox
     * @throws IOException when it cannot be read or is not a regular file
     */
    @Override
    public FileStat stat(Path file) throws IOException {
        if (Files.isSymbolicLink(file)) {
            throw new InvalidInputException("'" + file + "' is a symbolic link and will not be read.");
        }
        Path checked = requireInside(file);
        BasicFileAttributes attributes =
                Files.readAttributes(checked, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isRegularFile()) {
            throw new IOException("'" + file + "' is not a regular file");
        }
        return new FileStat(checked, attributes.size(), attributes.lastModifiedTime().toInstant());
    }

    private boolean isInsideQuietly(Path path) {
        try {
            requireInside(path);
            return true;
        } catch (InvalidInputException e) {
            return false;
        }
    }

    private static Path realPath(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            // An absent file still has to be judged, so fall back to the lexical form. That is
            // safe here because startsWith is then checked against the normalized root.
            return path.toAbsolutePath().normalize();
        }
    }
}
