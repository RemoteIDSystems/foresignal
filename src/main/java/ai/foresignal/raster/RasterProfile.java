package ai.foresignal.raster;

import java.util.Locale;

/**
 * The memory profile selected by {@code FORESIGNAL_PROFILE} ({@code foresignal.profile},
 * Weather-Imagery-Design §2.6). It sets the raster cache sizes, the render pool and the decode
 * concurrency; each value can still be overridden on its own. The profile is chosen by the operator,
 * never derived from {@code MaxRAMPercentage}.
 */
public enum RasterProfile {

    /** A 32 GB host: {@code -Xmx4g}, G1, a 6 GB container. */
    SMALL("small", 768, 128, 64, 1),

    /** A 64 GB host: {@code -Xmx8g}, generational ZGC, an 11 GB container. */
    LARGE("large", 1536, 256, 128, 2);

    private final String wireValue;
    private final int frameCacheMb;
    private final int tileCacheMb;
    private final int indexCacheMb;
    private final int decodeConcurrency;

    RasterProfile(String wireValue, int frameCacheMb, int tileCacheMb, int indexCacheMb, int decodeConcurrency) {
        this.wireValue = wireValue;
        this.frameCacheMb = frameCacheMb;
        this.tileCacheMb = tileCacheMb;
        this.indexCacheMb = indexCacheMb;
        this.decodeConcurrency = decodeConcurrency;
    }

    /** {@code small} or {@code large}. */
    public String wireValue() {
        return wireValue;
    }

    /** The default frame cache, MB. */
    public int frameCacheMb() {
        return frameCacheMb;
    }

    /** The default tile cache, MB (used by prompt 045). */
    public int tileCacheMb() {
        return tileCacheMb;
    }

    /** The default index-map cache, MB (used by prompt 045). */
    public int indexCacheMb() {
        return indexCacheMb;
    }

    /** The default number of concurrent decodes. */
    public int decodeConcurrency() {
        return decodeConcurrency;
    }

    /** The default render pool for {@code cores} available processors: {@code min(cores−2, 6)} or {@code cores−2}. */
    public int renderPool(int cores) {
        int spare = Math.max(1, cores - 2);
        return this == SMALL ? Math.min(spare, 6) : spare;
    }

    /**
     * The profile named {@code value}; null or blank is {@link #SMALL}.
     *
     * @throws IllegalArgumentException for any other name
     */
    public static RasterProfile parse(String value) {
        if (value == null || value.isBlank()) {
            return SMALL;
        }
        String wanted = value.strip().toLowerCase(Locale.ROOT);
        for (RasterProfile profile : values()) {
            if (profile.wireValue.equals(wanted)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("FORESIGNAL_PROFILE must be small or large, was '" + value + "'");
    }
}
