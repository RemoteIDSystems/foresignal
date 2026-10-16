package ai.foresignal.export;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bulk export limits (prompt 111), bound from {@code foresignal.exports.*}.
 *
 * @param maxConcurrent how many export workers run at once (default 1)
 * @param maxRows the largest row count a single export may write (default 5,000,000)
 * @param diskQuotaGb the largest total bytes of export and pack directories (default 20)
 * @param ttl how long an export survives before {@code ExportRetentionStep} expires it (default 7 d)
 * @param packMaxTiles the largest tile count an offline pack may cover (default 200,000)
 */
@ConfigurationProperties("foresignal.exports")
public record ExportProperties(int maxConcurrent, long maxRows, long diskQuotaGb, Duration ttl, long packMaxTiles) {

    /** Validates and fills defaults. */
    public ExportProperties {
        maxConcurrent = maxConcurrent <= 0 ? 1 : maxConcurrent;
        maxRows = maxRows <= 0 ? 5_000_000L : maxRows;
        diskQuotaGb = diskQuotaGb <= 0 ? 20L : diskQuotaGb;
        ttl = ttl == null ? Duration.ofDays(7) : ttl;
        packMaxTiles = packMaxTiles <= 0 ? 200_000L : packMaxTiles;
    }
}
