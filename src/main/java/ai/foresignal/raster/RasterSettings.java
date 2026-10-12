package ai.foresignal.raster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The resolved raster settings (Weather-Imagery-Design §2.6): each {@link RasterProperties} value
 * when set, else the {@link RasterProfile}'s default. Nothing is derived from the heap size.
 */
@Component
public class RasterSettings {

    private static final long MB = 1024L * 1024L;
    private static final double GB = 1024d * 1024d * 1024d;

    private final RasterProfile profile;
    private final RasterProperties properties;
    private final int cores;

    /** Creates the settings from {@code foresignal.profile} and {@code foresignal.raster.*}. */
    @org.springframework.beans.factory.annotation.Autowired
    public RasterSettings(@Value("${foresignal.profile:small}") String profile, RasterProperties properties) {
        this(RasterProfile.parse(profile), properties, Runtime.getRuntime().availableProcessors());
    }

    /** Creates the settings for an explicit profile and core count (tests). */
    public RasterSettings(RasterProfile profile, RasterProperties properties, int cores) {
        this.profile = profile;
        this.properties = properties == null ? RasterProperties.defaults() : properties;
        this.cores = cores;
    }

    /** The selected profile. */
    public RasterProfile profile() {
        return profile;
    }

    /** The frame cache bound, bytes. */
    public long frameCacheBytes() {
        return mb(properties.frameCacheMb(), profile.frameCacheMb());
    }

    /** The tile cache bound, bytes (prompt 045). */
    public long tileCacheBytes() {
        return mb(properties.tileCacheMb(), profile.tileCacheMb());
    }

    /** The index-map cache bound, bytes (prompt 045). */
    public long indexCacheBytes() {
        return mb(properties.indexCacheMb(), profile.indexCacheMb());
    }

    /** Render threads (prompt 045). */
    public int renderPool() {
        Integer set = properties.renderPool();
        return set != null && set > 0 ? set : profile.renderPool(cores);
    }

    /** Concurrent decodes. */
    public int decodeConcurrency() {
        Integer set = properties.decodeConcurrency();
        return set != null && set > 0 ? set : profile.decodeConcurrency();
    }

    /** The disk quota, GB. */
    public double diskQuotaGb() {
        return properties.diskQuotaGb();
    }

    /** The disk quota, bytes. */
    public long diskQuotaBytes() {
        return (long) (properties.diskQuotaGb() * GB);
    }

    /** How old an orphan must be before the sweep removes it. */
    public java.time.Duration orphanGrace() {
        return properties.orphanGrace();
    }

    private static long mb(Integer set, int fallback) {
        return (set != null && set > 0 ? set : fallback) * MB;
    }
}
