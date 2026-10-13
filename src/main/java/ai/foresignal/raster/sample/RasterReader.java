package ai.foresignal.raster.sample;

import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.source.spi.FlashSink;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Read-only raster access (prompt 052): what a DERIVED generator gets as {@code ctx.rasters()}, and what
 * the sampling endpoints use. Nothing here writes a frame or causes an upstream fetch.
 */
public interface RasterReader {

    /** The product's spec, or empty when unknown. */
    Optional<ProductSpec> product(String productId);

    /**
     * Why the product cannot give current values, or empty when it can: the product is unknown,
     * external or disabled, or its source is disabled or failing.
     */
    Optional<String> unavailableReason(String productId);

    /** The newest complete frame of an observation product, or empty. */
    Optional<RasterFrameRow> newestFrame(String productId);

    /** The complete observation frames valid in {@code [from, to]}, oldest first. */
    List<RasterFrameRow> frames(String productId, Instant from, Instant to);

    /** The reference time of the newest complete run of a forecast product, or empty. */
    Optional<Instant> latestCompleteRun(String productId);

    /** The complete steps of one run, by step. */
    List<RasterFrameRow> runFrames(String productId, Instant run);

    /**
     * Decodes a frame (through the frame cache).
     *
     * @return the view, or empty when the file is gone (expired) or unreadable
     */
    Optional<FrameView> view(RasterFrameRow frame);

    /** The GLM flash ring and archive (prompt 048), read-only use expected. */
    FlashSink flashes();
}
