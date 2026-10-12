package ai.foresignal.source.spi;

/**
 * A stored media row as its producer sees it (prompt 043): what {@link MediaSink#store} returns.
 *
 * <p>Media is content-addressed: two rows with the same {@code sha256} share one file.
 *
 * @param id the media id (ULID), or null for the preview a probing sink returns
 * @param sha256 the lower-case hex SHA-256 of the file
 * @param contentType {@code image/png}, {@code image/jpeg} or {@code image/tiff}
 * @param width the width in pixels
 * @param height the height in pixels
 * @param bytes the file size
 */
public record MediaRef(String id, String sha256, String contentType, int width, int height, long bytes) {}
