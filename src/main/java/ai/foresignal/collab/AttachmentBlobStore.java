package ai.foresignal.collab;

import ai.foresignal.config.ForesignalProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * The content-addressed store of attachment bytes (prompt 130): {@code {data-dir}/attachments/{ab}/{sha256}.{ext}},
 * the 043 media store's layout in its own directory, so 043 retention never touches an attachment. Identical uploads
 * share one file (dedup by SHA-256); the file is removed when the last attachment row using it is deleted.
 */
@Component
public class AttachmentBlobStore {

    private final Path root;

    /** Creates the store under {@code foresignal.data-dir}. */
    @org.springframework.beans.factory.annotation.Autowired
    public AttachmentBlobStore(ForesignalProperties properties) {
        this(properties.dataDir());
    }

    /** Creates the store under {@code dataDir} (tests). */
    public AttachmentBlobStore(Path dataDir) {
        this.root = dataDir.toAbsolutePath().normalize().resolve("attachments");
        try {
            Files.createDirectories(root.resolve("tmp"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the attachment directory " + root, e);
        }
    }

    /** The SHA-256 of {@code bytes}, lower-case hex. */
    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is missing", e);
        }
    }

    /** The relative path of a stored file. */
    public static String relative(String sha256, String extension) {
        String hex = sha256.toLowerCase(Locale.ROOT);
        return hex.substring(0, 2) + "/" + hex + "." + extension;
    }

    /**
     * Stores {@code bytes} under their hash, unless that file exists.
     *
     * @return the relative path
     */
    public String store(byte[] bytes, String sha256, String extension) {
        String relative = relative(sha256, extension);
        Path target = resolve(relative);
        if (Files.exists(target)) {
            return relative;
        }
        Path staged = root.resolve("tmp").resolve(UUID.randomUUID() + ".part");
        try {
            Files.createDirectories(target.getParent());
            Files.write(staged, bytes);
            try {
                Files.move(staged, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (FileAlreadyExistsException e) {
                Files.deleteIfExists(staged);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(staged, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return relative;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(staged);
            } catch (IOException ignored) {
                // best effort
            }
            throw new UncheckedIOException("Could not store the attachment: " + e.getMessage(), e);
        }
    }

    /** The bytes of a stored file. */
    public byte[] read(String relative) {
        try {
            return Files.readAllBytes(resolve(relative));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the attachment: " + e.getMessage(), e);
        }
    }

    /** True when the stored file exists. */
    public boolean exists(String relative) {
        return Files.exists(resolve(relative));
    }

    /** Deletes a stored file (no other attachment uses it). */
    public void delete(String relative) {
        try {
            Files.deleteIfExists(resolve(relative));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete the attachment: " + e.getMessage(), e);
        }
    }

    /** The absolute path of a relative path, refusing anything outside the store. */
    Path resolve(String relative) {
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root) || resolved.startsWith(root.resolve("tmp"))) {
            throw new IllegalArgumentException("Attachment path escapes the store: " + relative);
        }
        return resolved;
    }
}
