package ai.foresignal.raster;

import ai.foresignal.collect.UsageMeter;
import ai.foresignal.media.MediaQueries;
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
 * use, per-cache metrics (frame, and from prompt 045 tile and index), the render queue's depth, active
 * renders and pool size (045), {@code RENDER_BUSY} responses in the last hour (045) and megabytes
 * downloaded today by product sources. Prompt 043 adds {@code mediaBytes}, the media store's bytes on disk, which count against the same quota.
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
    private final MediaQueries media;
    private final org.springframework.beans.factory.ObjectProvider<ai.foresignal.raster.render.RenderQueue> queue;

    /** Creates the block. */
    public RasterSystemBlock(RasterSettings settings, ProductRegistry registry, RasterFrameQueries frames,
            RasterCacheRegistry caches, UsageMeter usage, MediaQueries media,
            org.springframework.beans.factory.ObjectProvider<ai.foresignal.raster.render.RenderQueue> queue) {
        this.settings = settings;
        this.registry = registry;
        this.frames = frames;
        this.caches = caches;
        this.usage = usage;
        this.media = media;
        this.queue = queue;
    }

    @Override
    public String key() {
        return "rasters";
    }

    @Override
    public Map<String, Object> block() {
        RasterFrameQueries.Totals totals = frames.totals();
        long mediaBytes = media.totalBytes();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profile", settings.profile().wireValue());
        out.put("products", registry.all().size());
        out.put("frames", totals.frames());
        out.put("storeBytes", totals.bytes());
        out.put("mediaBytes", mediaBytes);
        out.put("diskQuotaGb", settings.diskQuotaGb());
        out.put("diskQuotaPct", quotaPct(totals.bytes() + mediaBytes, settings.diskQuotaBytes()));
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
        ai.foresignal.raster.render.RenderQueue q = queue == null ? null : queue.getIfAvailable();
        Map<String, Object> render = new LinkedHashMap<>();
        render.put("depth", q == null ? 0 : q.depth());
        render.put("active", q == null ? 0 : q.active());
        render.put("poolSize", q == null ? settings.renderPool() : q.poolSize());
        out.put("renderQueue", render);
        out.put("renderBusyLastHour", q == null ? 0 : q.busyLastHour());
        out.put("mbDownloadedToday", round(bytesToday() / MB));
        return out;
    }

    @Override
    public List<SystemWarnings.Warning> warnings() {
        double pct = quotaPct(frames.totals().bytes() + media.totalBytes(), settings.diskQuotaBytes());
        if (pct < QUOTA_WARN_PCT) {
            return List.of();
        }
        return List.of(new SystemWarnings.Warning(QUOTA_WARNING, "warning", "The raster and media stores are at " + pct
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
