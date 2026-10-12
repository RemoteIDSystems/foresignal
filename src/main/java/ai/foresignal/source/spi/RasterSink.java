package ai.foresignal.source.spi;

import java.time.Instant;
import java.util.List;

/**
 * The raster pipeline as an adapter sees it: {@code ctx.rasters()} (Weather-Imagery-Design §2.1).
 *
 * <p>Adapters never touch the frame store or the {@code raster_frame} table. They hand each frame to
 * {@link #commit}, which quantizes it per the product spec, computes statistics and the footprint,
 * writes the {@code .fsg} file, upserts the row and returns the {@code raster-frame} record the
 * adapter includes in its {@link FetchResult}. That record is what makes SSE, resume, MCP and
 * triggers work with no raster-specific distribution code.
 *
 * <p>During a probe the sink is {@linkplain #probing() probing}: it writes nothing and returns a
 * preview record carrying a grid summary, so an adapter can probe by "committing" a frame decoded
 * from its header only.
 */
public interface RasterSink {

    /**
     * Commits one frame.
     *
     * <ul>
     *   <li>Observation, complete: returns the frame's {@code raster-frame} record (one version per
     *       frame, externalId = productId).
     *   <li>Observation, partial: the frame is stored and returned by the API with
     *       {@code includePartial=true}; the result is null, because a partial frame never makes a
     *       version.
     *   <li>Forecast step: returns the run-in-progress record (externalId {@code {productId}:partial});
     *       the run's own version comes from {@link #completeRun}.
     * </ul>
     *
     * @param frame the frame
     * @return the record to include in the fetch result, or null
     * @throws AdapterException when the frame cannot be decoded or stored
     * @throws SilentFormatChangeException when the decoder found an expected variable missing; the
     *     product is marked degraded and nothing is written
     */
    NormalizedRecord commit(FrameInput frame) throws AdapterException;

    /**
     * Marks a forecast run complete: its frames become eligible for {@code latest} and the run gets
     * its one record version.
     *
     * @param productId the forecast product
     * @param referenceTime the run's reference time
     * @return the run's record and the tombstone of the {@code {productId}:partial} record; empty
     *     when the run has no frames
     * @throws AdapterException when the run cannot be read back
     */
    List<NormalizedRecord> completeRun(String productId, Instant referenceTime) throws AdapterException;

    /**
     * Reports that the upstream input of {@code productId} silently changed (a missing field or
     * variable) without throwing: the product and its source show degraded health with
     * {@code reason} until the next successful commit of the product.
     */
    void reportFormatChange(String productId, String reason);

    /** True during a probe: nothing is written and {@link #commit} returns a preview. */
    boolean probing();

    /**
     * True when {@code productId} is disabled (prompt 046: its decoder failed the startup self-test).
     * An adapter skips a disabled product instead of spending budget on files nothing can decode.
     *
     * @param productId the product
     * @return true when the product's frame endpoints answer {@code 503 PRODUCT_UNAVAILABLE}
     */
    default boolean disabled(String productId) {
        return false;
    }
}
