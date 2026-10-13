package ai.foresignal.derive;

import java.time.Instant;
import java.util.Objects;

/**
 * A raster frame that reached a {@link DerivedGenerator} (prompt 052): a stored frame, or a forecast
 * run that just completed.
 *
 * <p>The runner coalesces frame events per product: when several frames of one product arrive while
 * the generator is busy, only the newest is delivered, because a generator always reads the frame
 * store's current state.
 *
 * @param productId the product
 * @param frameId the frame id ({@code null} for a run completion)
 * @param status {@code partial} or {@code complete}
 * @param validTime the frame's valid time, or null when unknown (a run completion)
 * @param referenceTime the forecast run, or null for an observation frame
 * @param runComplete true when this event announces a completed forecast run
 * @param observedAt the time a derived record built from this event is observed at: the frame's valid
 *     time for an observation, never later than the time the event was received
 */
public record RasterFrameEvent(String productId, String frameId, String status, Instant validTime,
        Instant referenceTime, boolean runComplete, Instant observedAt) {

    /** Requires the product and the observation time. */
    public RasterFrameEvent {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(observedAt, "observedAt");
    }

    /** True for a complete frame or a completed run. */
    public boolean complete() {
        return runComplete || "complete".equals(status);
    }
}
