package ai.foresignal.raster.render;

import ai.foresignal.raster.RasterCacheRegistry;
import ai.foresignal.raster.RasterSettings;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The rendered-tile cache (prompt 045; Weather-Imagery-Design §2.6): encoded tiles and images,
 * byte-weighted, bounded by {@code foresignal.raster.tile-cache-mb}. An empty render is held as the
 * shared {@link #EMPTY} marker, so it costs no bytes.
 */
@Component
public class TileCache {

    /** The cache's name in {@code /v1/system}. */
    public static final String CACHE_NAME = "tile";

    /** The marker of a render whose every pixel is transparent. */
    public static final byte[] EMPTY = new byte[0];

    /**
     * The key of one rendered output.
     *
     * @param productId the product
     * @param frameKey the frame's content-addressed key ({@code timeKey~sha8})
     * @param style the versioned style
     * @param region {@code z/x/y@scale} for a tile, or an image's crs, bounds, size and format
     */
    public record Key(String productId, String frameKey, String style, String region) {

        /** The key of a tile. */
        public static Key tile(String productId, String frameKey, String style, int z, int x, int y, int scale) {
            return new Key(productId, frameKey, style, z + "/" + x + "/" + y + "@" + scale);
        }
    }

    private final Cache<Key, byte[]> cache;
    private final long maxBytes;

    /** Creates the cache with the profile's bound and registers its metrics. */
    @Autowired
    public TileCache(RasterSettings settings, RasterCacheRegistry caches) {
        this(settings.tileCacheBytes());
        caches.register(CACHE_NAME, cache, maxBytes);
    }

    /** Creates a cache of {@code maxBytes} (tests). */
    public TileCache(long maxBytes) {
        this.maxBytes = maxBytes;
        this.cache = Caffeine.newBuilder()
                .maximumWeight(maxBytes)
                .weigher((Key k, byte[] v) -> v.length + 96)
                .recordStats()
                .build();
    }

    /** The cached bytes, or null. */
    public byte[] get(Key key) {
        return cache.getIfPresent(key);
    }

    /** Stores {@code bytes} ({@link #EMPTY} for an empty render). */
    public void put(Key key, byte[] bytes) {
        cache.put(key, bytes);
    }

    /** Drops every entry of {@code productId}. */
    public void invalidateProduct(String productId) {
        cache.asMap().keySet().removeIf(k -> k.productId().equals(productId));
    }

    /** Entries held (tests). */
    public long size() {
        cache.cleanUp();
        return cache.estimatedSize();
    }

    /** The underlying cache (tests and metrics). */
    public Cache<Key, byte[]> cache() {
        return cache;
    }
}
