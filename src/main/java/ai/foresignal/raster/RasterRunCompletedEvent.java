package ai.foresignal.raster;

import java.time.Instant;

/**
 * Published after a forecast run of a product was marked complete (prompt 052), so the DERIVED runtime
 * can recompute what depends on the newest complete run (place-weather smoke and forecast sections).
 *
 * @param productId the forecast product
 * @param run the run's reference time
 */
public record RasterRunCompletedEvent(String productId, Instant run) {}
