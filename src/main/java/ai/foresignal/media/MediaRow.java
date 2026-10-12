package ai.foresignal.media;

import ai.foresignal.source.spi.MediaRef;
import java.time.Instant;

/**
 * One {@code media} row (prompt 043).
 *
 * @param id the media id (ULID)
 * @param sha256 the SHA-256 of the file, lower-case hex
 * @param contentType the content type
 * @param width the width, pixels
 * @param height the height, pixels
 * @param bytes the file size
 * @param path the file, relative to {@code media/}
 * @param role {@code image}, {@code thumbnail}, {@code projected} or {@code map}
 * @param sourceId the producing source, or null
 * @param recordSourceId the owning record's source, or null
 * @param recordExternalId the owning record's external id, or null
 * @param licence the license (column {@code licence}), or null
 * @param createdAt when stored
 * @param expiresAt when it expires, or null to follow the owning record
 * @param variantOf the original of a thumbnail, or null
 * @param variantWidth the long-edge width of a thumbnail, or null
 */
public record MediaRow(
        String id,
        String sha256,
        String contentType,
        int width,
        int height,
        long bytes,
        String path,
        String role,
        String sourceId,
        String recordSourceId,
        String recordExternalId,
        String licence,
        Instant createdAt,
        Instant expiresAt,
        String variantOf,
        Integer variantWidth) {

    /** The reference a producer sees. */
    public MediaRef toRef() {
        return new MediaRef(id, sha256, contentType, width, height, bytes);
    }

    /** True when the row has expired at {@code now}. */
    public boolean expired(Instant now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    /** The long edge, pixels. */
    public int longEdge() {
        return Math.max(width, height);
    }
}
