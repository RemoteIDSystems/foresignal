package ai.elaro.source.spi;

import java.nio.file.Path;

/**
 * A body streamed to a file rather than held in memory.
 *
 * <p>Raster frames and model grids are tens of megabytes; reading one into a byte array would make
 * heap use a function of what an upstream decided to publish.
 *
 * @param file where the bytes were written
 * @param bytes how many were written
 * @param contentType the declared content type
 * @param sha256 lowercase hex SHA-256 of the bytes, computed while streaming
 */
public record DownloadResult(Path file, long bytes, String contentType, String sha256) {}
