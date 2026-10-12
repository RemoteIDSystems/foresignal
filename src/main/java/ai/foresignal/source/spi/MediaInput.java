package ai.foresignal.source.spi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * One image to store in the media store (prompt 043): {@code MediaStore.store(MediaInput)} and
 * {@code ctx.media().store(MediaInput)}.
 *
 * <p>Exactly one of {@link #path()} and {@link #bytes()} carries the content. A path is copied, never
 * moved, so the caller keeps its file (an inbox file stays where SatDump wrote it).
 *
 * @param path the file to store, or null when {@code bytes} is given
 * @param bytes the content, or null when {@code path} is given
 * @param contentType the declared type; must agree with the sniffed format ({@code image/png},
 *     {@code image/jpeg} or {@code image/tiff})
 * @param role what the image is for
 * @param sourceId the producing source; {@code ctx.media()} fills it in
 * @param recordSourceId the owning record's source, or null
 * @param recordExternalId the owning record's external id, or null
 * @param licence the license or attribution of the image (the {@code licence} column), or null
 * @param expiresAt when the image expires, or null to follow the owning record's retention
 * @param allowMultiImage true to accept a TIFF with more than one image (only the first is used)
 */
public record MediaInput(
        Path path,
        byte[] bytes,
        String contentType,
        MediaRole role,
        String sourceId,
        String recordSourceId,
        String recordExternalId,
        String licence,
        Instant expiresAt,
        boolean allowMultiImage) {

    /** Requires exactly one content carrier, a content type, a role and a complete record ref. */
    public MediaInput {
        if ((path == null) == (bytes == null)) {
            throw new IllegalArgumentException("A media input needs exactly one of a path and bytes.");
        }
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(role, "role");
        if ((recordSourceId == null) != (recordExternalId == null)) {
            throw new IllegalArgumentException("A record ref needs both its source id and its external id.");
        }
    }

    /** An image stored from a file (copied). */
    public static MediaInput of(Path path, String contentType, MediaRole role) {
        return new MediaInput(path, null, contentType, role, null, null, null, null, null, false);
    }

    /** An image stored from bytes. */
    public static MediaInput of(byte[] bytes, String contentType, MediaRole role) {
        return new MediaInput(null, bytes, contentType, role, null, null, null, null, null, false);
    }

    /** A copy produced by {@code sourceId}. */
    public MediaInput withSourceId(String value) {
        return new MediaInput(path, bytes, contentType, role, value, recordSourceId, recordExternalId, licence,
                expiresAt, allowMultiImage);
    }

    /** A copy owned by the record {@code (sourceId, externalId)}; its retention follows that record. */
    public MediaInput withRecord(String sourceIdOfRecord, String externalId) {
        return new MediaInput(path, bytes, contentType, role, sourceId, sourceIdOfRecord, externalId, licence,
                expiresAt, allowMultiImage);
    }

    /** A copy with a license or attribution. */
    public MediaInput withLicence(String value) {
        return new MediaInput(path, bytes, contentType, role, sourceId, recordSourceId, recordExternalId, value,
                expiresAt, allowMultiImage);
    }

    /** A copy that expires at {@code value} (null follows the owning record). */
    public MediaInput withExpiresAt(Instant value) {
        return new MediaInput(path, bytes, contentType, role, sourceId, recordSourceId, recordExternalId, licence,
                value, allowMultiImage);
    }

    /** A copy that accepts a multi-image TIFF. */
    public MediaInput allowingMultiImage() {
        return new MediaInput(path, bytes, contentType, role, sourceId, recordSourceId, recordExternalId, licence,
                expiresAt, true);
    }

    /** True when the image is owned by a record. */
    public boolean hasRecord() {
        return recordSourceId != null;
    }

    /** Opens the content; the caller closes the stream. */
    public InputStream open() throws IOException {
        return path != null ? Files.newInputStream(path) : new ByteArrayInputStream(bytes);
    }

    /** The content size when it is known up front (bytes, or a readable file), else -1. */
    public long knownSize() {
        if (bytes != null) {
            return bytes.length;
        }
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }
}
