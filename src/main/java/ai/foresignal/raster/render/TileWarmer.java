package ai.foresignal.raster.render;

import ai.foresignal.place.PlacesAoiService;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameCommittedEvent;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.ProductSpec;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Warms the newest frame (prompt 045; Weather-Imagery-Design §6.3): when a complete frame is
 * committed, the tiles intersecting the places-AOI envelope (036 {@link PlacesAoiService}, which falls
 * back to {@code weather.defaultAoi} with a health warning when there are no places) are rendered at
 * every zoom from the product's min zoom up to its {@code maxZoomForAnimation} (radar 7, IR 6), at 1×
 * and 2×, into the tile cache, at the queue's highest priority. Nothing is fetched upstream.
 *
 * <p>Warming is asynchronous: the commit never waits for it. A product without
 * {@code maxZoomForAnimation}, or whose default style is not registered, is not warmed. At most
 * {@value #MAX_TILES} tiles are queued per frame.
 */
@Component
public class TileWarmer {

    /** The most tiles queued for one frame (both scales counted). */
    public static final int MAX_TILES = 512;

    private static final Logger log = LoggerFactory.getLogger(TileWarmer.class);

    private final ProductRegistry registry;
    private final RasterFrameQueries frames;
    private final RasterRenderService renderer;
    private final ObjectProvider<PlacesAoiService> aoi;

    /** Creates the warmer. */
    public TileWarmer(ProductRegistry registry, RasterFrameQueries frames, RasterRenderService renderer,
            ObjectProvider<PlacesAoiService> aoi) {
        this.registry = registry;
        this.frames = frames;
        this.renderer = renderer;
        this.aoi = aoi;
    }

    /** Warms a newly committed complete frame. */
    @EventListener
    public void onFrameCommitted(RasterFrameCommittedEvent event) {
        if (!RasterFrameRow.COMPLETE.equals(event.status())) {
            return;
        }
        try {
            warm(event.productId(), event.frameId());
        } catch (RuntimeException e) {
            log.debug("Could not warm {}/{}", event.productId(), event.frameId(), e);
        }
    }

    /**
     * Queues the AOI tiles of one frame.
     *
     * @return the number of tiles queued
     */
    public int warm(String productId, String frameId) {
        Optional<ProductRegistry.Registered> product = registry.find(productId);
        Optional<RasterFrameRow> row = frames.find(productId, frameId);
        if (product.isEmpty() || row.isEmpty() || !row.get().complete()) {
            return 0;
        }
        ProductSpec spec = product.get().spec();
        if (spec.isExternal() || spec.spatial() == null || spec.spatial().maxZoomForAnimation() == null) {
            return 0;
        }
        StyleRegistry.Resolved style;
        try {
            style = renderer.style(spec, null);
        } catch (RuntimeException e) {
            return 0;
        }
        PlacesAoiService service = aoi.getIfAvailable();
        if (service == null) {
            return 0;
        }
        double[] envelope = service.envelope();
        double[] grid = renderer.projection(row.get().gridId()).bounds();
        if (!RasterRenderService.intersects(envelope, grid)) {
            return 0;
        }
        int min = spec.spatial().minZoom() == null ? 0 : spec.spatial().minZoom();
        int max = Math.min(spec.spatial().maxZoomForAnimation(),
                spec.spatial().nativeMaxZoom() == null ? 0 : spec.spatial().nativeMaxZoom());
        int queued = 0;
        for (int z = min; z <= max; z++) {
            int[] range = WebMercator.tileRange(z, envelope[0], envelope[1], envelope[2], envelope[3]);
            for (int x = range[0]; x <= range[2]; x++) {
                for (int y = range[1]; y <= range[3]; y++) {
                    if (!RasterRenderService.intersects(WebMercator.tileBounds(z, x, y), grid)) {
                        continue;
                    }
                    for (int scale = 1; scale <= 2; scale++) {
                        if (queued >= MAX_TILES) {
                            return queued;
                        }
                        renderer.warm(spec, row.get(), style, z, x, y, scale);
                        queued++;
                    }
                }
            }
        }
        return queued;
    }
}
