package ai.foresignal.raster.render;

import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.RasterCacheRegistry;
import ai.foresignal.raster.RasterSettings;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The index-map cache (prompt 045; Weather-Imagery-Design §2.6): a byte-weighted Caffeine cache of
 * {@link IndexMap}s bounded by {@code foresignal.raster.index-cache-mb}. A grid's maps are shared by
 * every frame on that grid, so after the first frame a tile render is only a gather.
 */
@Component
public class IndexMapCache {

    /** The cache's name in {@code /v1/system}. */
    public static final String CACHE_NAME = "index";

    /**
     * The key of a tile's map.
     *
     * @param gridId the grid
     * @param z zoom
     * @param x column
     * @param y row
     * @param scale 1 (256 px) or 2 (512 px)
     * @param bilinear the four-cell variant
     */
    public record TileKey(String gridId, int z, int x, int y, int scale, boolean bilinear) {}

    /**
     * The key of an image's map.
     *
     * @param gridId the grid
     * @param mercator EPSG:3857 output
     * @param west snapped west
     * @param south snapped south
     * @param east snapped east
     * @param north snapped north
     * @param width pixels
     * @param height pixels
     * @param bilinear the four-cell variant
     */
    public record ImageKey(String gridId, boolean mercator, double west, double south, double east, double north,
            int width, int height, boolean bilinear) {}

    private final Cache<Object, IndexMap> cache;
    private final long maxBytes;

    /** Creates the cache with the profile's bound and registers its metrics. */
    @Autowired
    public IndexMapCache(RasterSettings settings, RasterCacheRegistry caches) {
        this(settings.indexCacheBytes());
        caches.register(CACHE_NAME, cache, maxBytes);
    }

    /** Creates a cache of {@code maxBytes} (tests). */
    public IndexMapCache(long maxBytes) {
        this.maxBytes = maxBytes;
        this.cache = Caffeine.newBuilder()
                .maximumWeight(maxBytes)
                .weigher((Object k, IndexMap v) -> (int) Math.min(Integer.MAX_VALUE, v.bytes()))
                .recordStats()
                .build();
    }

    /** The map of a tile, built on a miss. */
    public IndexMap tile(String gridId, GridDef grid, int z, int x, int y, int scale, boolean bilinear) {
        int size = WebMercator.TILE * scale;
        return cache.get(new TileKey(gridId, z, x, y, scale, bilinear),
                k -> IndexMap.build(grid, size, size, IndexMap.tile(z, x, y, size), bilinear));
    }

    /** The map of an image, built on a miss. */
    public IndexMap image(String gridId, GridDef grid, ImageKey key) {
        return cache.get(key, k -> IndexMap.build(grid, key.width(), key.height(),
                IndexMap.image(key.west(), key.south(), key.east(), key.north(), key.width(), key.height(),
                        key.mercator()), key.bilinear()));
    }

    /** The underlying cache (tests and metrics). */
    public Cache<Object, IndexMap> cache() {
        return cache;
    }

    /** The bound, bytes. */
    public long maxBytes() {
        return maxBytes;
    }
}
