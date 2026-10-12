package ai.foresignal.raster;

import ai.foresignal.collect.UsageMeter;
import ai.foresignal.system.SystemBlockContributor;
import ai.foresignal.system.SystemWarningContributor;
import ai.foresignal.system.SystemWarnings;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * The {@code rasters} block of {@code /v1/system} ({@code SystemRasters}, prompt 042;
 * Weather-Imagery-Design §2.6, §6.11): profile, products, frames, store bytes, disk quota and its
 * use, per-cache metrics, the render queue (filled by prompt 045), {@code RENDER_BUSY} responses in
 * the last hour (045) and megabytes downloaded today by product sources.
 *
 * <p>Also the {@value #QUOTA_WARNING} warning: at {@value #QUOTA_WARN_PCT} % of the quota or more,
 * {@code /v1/system} reports {@code degraded}.
 */
@Component
public class RasterSystemBlock implements SystemBlockContributor, SystemWarningContributor {

    /** The warning code of a nearly full raster store. */
    public static final String QUOTA_WARNING = "RASTER_DISK_QUOTA";

    /** The quota share at which the warning applies. */
    public static final double QUOTA_WARN_PCT = 90.0;

    private static final double MB = 1024d * 1024d;

    private final RasterSettings settings;
    private final ProductRegistry registry;
    private final RasterFrameQueries frames;
    private final RasterCacheRegistry caches;
    private final UsageMeter usage;

    /** Creates the block. */
    public RasterSystemBlock(RasterSettings settings, ProductRegistry registry, RasterFrameQueries frames,
            RasterCacheRegistry caches, UsageMeter usage) {
        this.settings = settings;
        this.registry = registry;
        this.frames = frames;
        this.caches = caches;
        this.usage = usage;
    }

    @Override
    public String key() {
        return "rasters";
    }

    @Override
    public Map<String, Object> block() {
        RasterFrameQueries.Totals totals = frames.totals();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profile", settings.profile().wireValue());
        out.put("products", registry.all().size());
        out.put("frames", totals.frames());
        out.put("storeBytes", totals.bytes());
        out.put("diskQuotaGb", settings.diskQuotaGb());
        out.put("diskQuotaPct", quotaPct(totals.bytes(), settings.diskQuotaBytes()));
        Map<String, Object> cacheBlock = new LinkedHashMap<>();
        caches.metrics().forEach((name, m) -> {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("sizeBytes", m.sizeBytes());
            c.put("maxBytes", m.maxBytes());
            c.put("hitRate", round(m.hitRate()));
            c.put("evictionRate", round(m.evictionRate()));
            cacheBlock.put(name, c);
        });
        out.put("caches", cacheBlock);
        Map<String, Object> render = new LinkedHashMap<>();
        render.put("depth", 0);
        render.put("active", 0);
        render.put("poolSize", settings.renderPool());
        out.put("renderQueue", render);
        out.put("renderBusyLastHour", 0);
        out.put("mbDownloadedToday", round(bytesToday() / MB));
        return out;
    }

    @Override
    public List<SystemWarnings.Warning> warnings() {
        double pct = quotaPct(frames.totals().bytes(), settings.diskQuotaBytes());
        if (pct < QUOTA_WARN_PCT) {
            return List.of();
        }
        return List.of(new SystemWarnings.Warning(QUOTA_WARNING, "warning", "The raster store is at " + pct
                + " % of its " + settings.diskQuotaGb() + " GB quota (FORESIGNAL_RASTER_DISK_QUOTA_GB); retention evicts"
                + " the oldest frames above 100 %. Raise the quota or shorten the retention ladders."));
    }

    /** The share of {@code quota} that {@code bytes} uses, percent, two decimals. */
    static double quotaPct(long bytes, long quota) {
        return quota <= 0 ? 0.0 : Math.round(bytes * 10_000.0 / quota) / 100.0;
    }

    private long bytesToday() {
        Set<String> sources = new LinkedHashSet<>();
        registry.all().forEach(p -> sources.add(p.sourceId()));
        long total = 0;
        for (String source : sources) {
            try {
                total += usage.bytesToday(source);
            } catch (RuntimeException e) {
                // A source without usage rows counts as zero.
            }
        }
        return total;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
