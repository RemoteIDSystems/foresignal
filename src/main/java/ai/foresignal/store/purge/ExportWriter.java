package ai.foresignal.store.purge;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * Writes one entity export (prompt 039). Every byte written before the manifest goes through a
 * SHA-256 digest; the manifest itself is written after it and carries the digest as {@code sha256}.
 * A reader verifies an export by hashing every byte that precedes the last occurrence of the
 * manifest marker ({@link #manifestMarker()}).
 *
 * <p>Call order: {@link #start}, {@link #current} (at most once), {@link #version} in seq order,
 * {@link #alert}, then {@link #finish} or {@link #finishWithHash}. Not thread-safe.
 */
public abstract class ExportWriter {

    private final OutputStream raw;
    private final MessageDigest digest;
    private final DigestOutputStream content;

    /** Creates a writer over {@code out}, which it does not close. */
    protected ExportWriter(OutputStream out) {
        this.raw = out;
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
        this.content = new DigestOutputStream(out, digest);
    }

    /**
     * The text that opens the manifest; the hash covers every byte before its last occurrence (the
     * manifest is always last).
     */
    public abstract String manifestMarker();

    /** Writes the opening, with the entity's identity. */
    public abstract void start(String sourceId, String externalId, String kind);

    /** Writes the current state (head plus live). */
    public abstract void current(ExportItem item);

    /** Writes one retained version. */
    public abstract void version(ExportItem item);

    /** Writes one alert referencing the entity. */
    public abstract void alert(Map<String, Object> alert);

    /** Ends the content and writes {@code manifest}; the stream is flushed, not closed. */
    public final void finish(Map<String, Object> manifest) {
        finishWithHash(manifest, () -> { });
    }

    /**
     * Ends the content, runs {@code beforeManifest} (to add late counts), puts the content hash into
     * {@code manifest} as {@code sha256} and writes it. The stream is flushed, not closed.
     *
     * @return the content hash
     */
    public final String finishWithHash(Map<String, Object> manifest, Runnable beforeManifest) {
        endContent();
        beforeManifest.run();
        String sha = contentSha256();
        manifest.put("sha256", sha);
        writeManifest(manifest);
        try {
            raw.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sha;
    }

    /** Writes whatever closes the content before the manifest. */
    protected abstract void endContent();

    /** Writes the manifest and the closing after it, through {@link #rawWrite}. */
    protected abstract void writeManifest(Map<String, Object> manifest);

    /** The lowercase hex SHA-256 of the content written so far. */
    public String contentSha256() {
        return HexFormat.of().formatHex(((MessageDigest) cloneDigest()).digest());
    }

    /** Writes hashed content. */
    protected final void write(String text) {
        write(text.getBytes(StandardCharsets.UTF_8));
    }

    /** Writes hashed content. */
    protected final void write(byte[] bytes) {
        try {
            content.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Writes unhashed bytes (the manifest and what follows it). */
    protected final void rawWrite(String text) {
        try {
            raw.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Object cloneDigest() {
        try {
            return digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("SHA-256 digest cannot be cloned", e);
        }
    }
}
