package ai.foresignal.raster;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The {@code foresignal.raster.*} settings (prompt 042). Every sizing value is optional: when absent
 * it comes from the {@link RasterProfile} ({@code foresignal.profile}); {@link RasterSettings} holds
 * the resolved values.
 *
 * @param frameCacheMb the decoded-frame cache, MB ({@code FORESIGNAL_RASTER_FRAME_CACHE_MB})
 * @param tileCacheMb the tile cache, MB (045; {@code FORESIGNAL_RASTER_TILE_CACHE_MB})
 * @param indexCacheMb the index-map cache, MB (045; {@code FORESIGNAL_RASTER_INDEX_CACHE_MB})
 * @param renderPool render threads (045; {@code FORESIGNAL_RASTER_RENDER_POOL})
 * @param decodeConcurrency concurrent decodes ({@code FORESIGNAL_RASTER_DECODE_CONCURRENCY})
 * @param diskQuotaGb the frame store quota, GB; default 200 ({@code FORESIGNAL_RASTER_DISK_QUOTA_GB})
 * @param orphanGrace how old an orphan file or row must be before the sweep removes it, so a commit in
 *     flight is never mistaken for one; default 10 minutes
 */
@ConfigurationProperties("foresignal.raster")
public record RasterProperties(
        Integer frameCacheMb,
        Integer tileCacheMb,
        Integer indexCacheMb,
        Integer renderPool,
        Integer decodeConcurrency,
        Double diskQuotaGb,
        Duration orphanGrace) {

    /** The default disk quota, GB. */
    public static final double DEFAULT_DISK_QUOTA_GB = 200;

    /** The default orphan grace. */
    public static final Duration DEFAULT_ORPHAN_GRACE = Duration.ofMinutes(10);

    /** Applies the profile-independent defaults. */
    public RasterProperties {
        diskQuotaGb = diskQuotaGb == null || diskQuotaGb <= 0 ? DEFAULT_DISK_QUOTA_GB : diskQuotaGb;
        orphanGrace = orphanGrace == null || orphanGrace.isNegative() ? DEFAULT_ORPHAN_GRACE : orphanGrace;
    }

    /** Every value at its default. */
    public static RasterProperties defaults() {
        return new RasterProperties(null, null, null, null, null, null, null);
    }
}
