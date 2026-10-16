package ai.foresignal.export;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bulk export, pack and hold limits (prompt 111), bound from {@code foresignal.exports.*}.
 *
 * @param maxConcurrent how many export, snapshot and pack workers run at once (default 1)
 * @param maxRows the largest row count a single export or snapshot may write (default 5,000,000)
 * @param diskQuotaGb the largest total size of the export directories, in GiB (default 20)
 * @param ttl how long an export or pack lives before {@code ExportRetentionStep} expires it (default 7 d)
 * @param packMaxTiles the largest tile count an offline pack may cover ({@code packs.maxTiles}, default 200,000)
 * @param holdsMax the most legal holds that may exist ({@code holds.max}, default 20)
 * @param pageSize rows per keyset page of the export queries (default 10,000)
 * @param queryTimeout the statement timeout of one export page (default 5 min)
 */
@ConfigurationProperties("foresignal.exports")
public record ExportProperties(int maxConcurrent, long maxRows, long diskQuotaGb, Duration ttl, long packMaxTiles,
        int holdsMax, int pageSize, Duration queryTimeout) {

    /** The default page size of the export queries. */
    public static final int DEFAULT_PAGE_SIZE = 10_000;

    /** Validates and fills defaults. */
    public ExportProperties {
        maxConcurrent = maxConcurrent <= 0 ? 1 : maxConcurrent;
        maxRows = maxRows <= 0 ? 5_000_000L : maxRows;
        diskQuotaGb = diskQuotaGb <= 0 ? 20L : diskQuotaGb;
        ttl = ttl == null ? Duration.ofDays(7) : ttl;
        packMaxTiles = packMaxTiles <= 0 ? 200_000L : packMaxTiles;
        holdsMax = holdsMax <= 0 ? 20 : holdsMax;
        pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        queryTimeout = queryTimeout == null ? Duration.ofMinutes(5) : queryTimeout;
    }

    /** The shipped defaults (tests and code paths without a bound configuration). */
    public static ExportProperties defaults() {
        return new ExportProperties(0, 0, 0, null, 0, 0, 0, null);
    }
}
