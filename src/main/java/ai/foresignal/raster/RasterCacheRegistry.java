package ai.foresignal.raster;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The raster caches' metrics for the {@code /v1/system} {@code rasters.caches} block
 * (Weather-Imagery-Design §2.6): bytes held, bound, hit rate and evictions per minute, so the cache
 * sizes can be tuned from evidence. The frame cache registers here (042); 045 adds the tile and
 * index-map caches.
 */
@Component
public class RasterCacheRegistry {

    private final Map<String, Entry> caches = new ConcurrentHashMap<>();
    private final Clock clock;

    /** Creates the registry. */
    public RasterCacheRegistry(Clock clock) {
        this.clock = clock;
    }

    /**
     * Registers a byte-weighted Caffeine cache built with {@code recordStats()}.
     *
     * @param name {@code frame}, {@code tile} or {@code index}
     * @param cache the cache
     * @param maxBytes its weight bound
     */
    public void register(String name, Cache<?, ?> cache, long maxBytes) {
        caches.put(name, new Entry(cache, maxBytes, clock.instant()));
    }

    /**
     * One cache's metrics.
     *
     * @param sizeBytes the weight currently held
     * @param maxBytes the bound
     * @param hitRate hits over requests since start, 0 to 1 (1 before any request)
     * @param evictionRate evictions per minute since start
     */
    public record Metrics(long sizeBytes, long maxBytes, double hitRate, double evictionRate) {}

    /** Every registered cache's metrics, by name. */
    public Map<String, Metrics> metrics() {
        Map<String, Metrics> out = new LinkedHashMap<>();
        Instant now = clock.instant();
        caches.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> out.put(e.getKey(),
                e.getValue().metrics(now)));
        return out;
    }

    private record Entry(Cache<?, ?> cache, long maxBytes, Instant since) {

        Metrics metrics(Instant now) {
            CacheStats stats = cache.stats();
            long size = cache.policy().eviction().map(e -> e.weightedSize().orElse(0L)).orElse(0L);
            double minutes = Math.max(1.0, Duration.between(since, now).toSeconds() / 60.0);
            return new Metrics(size, maxBytes, stats.hitRate(), stats.evictionCount() / minutes);
        }
    }
}
