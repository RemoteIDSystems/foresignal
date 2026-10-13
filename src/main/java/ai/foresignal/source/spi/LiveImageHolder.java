package ai.foresignal.source.spi;

import java.time.Instant;

/**
 * Holds the latest live image of a source in memory only (prompt 049, Weather-Imagery-Design §4.2):
 * the SatDump polar plot during an active pass, served by
 * {@code GET /v1/ground-stations/{sourceId}/polarplot.jpeg}.
 *
 * <p>Nothing is written to disk or to the media store, and a client request never causes a fetch:
 * the adapter fetches on its own schedule and overwrites the held image. Adapters depend only on SPI
 * types (Architecture §3); {@code ai.foresignal.media.LiveImageStore} implements it.
 */
public interface LiveImageHolder {

    /**
     * Replaces the held JPEG of {@code sourceId}.
     *
     * @param sourceId the producing source
     * @param jpeg the image; it passes {@code ImageGuard} before it is held
     * @param fetchedAt when it was fetched
     * @throws ImageRejectedException when the guard refuses it; the previous image is dropped
     */
    void hold(String sourceId, byte[] jpeg, Instant fetchedAt);

    /** Drops the held image of {@code sourceId}, for example at LOS. */
    void clear(String sourceId);
}
