package ai.elaro.source.spi;

import java.time.Instant;

/**
 * One object from a public S3 bucket listing.
 *
 * @param key the object key
 * @param size size in bytes, which a byte budget can be charged against before downloading
 * @param etag the object ETag, useful as a change marker
 * @param lastModified when it was last written
 */
public record S3Object(String key, long size, String etag, Instant lastModified) {}
