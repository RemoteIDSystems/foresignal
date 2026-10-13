package ai.foresignal.media;

import ai.foresignal.source.spi.ImageRejectedException;
import ai.foresignal.source.spi.LiveImageHolder;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The in-memory latest live image per source (prompt 049, Weather-Imagery-Design §4.2): the SatDump
 * polar plot of an active pass.
 *
 * <p>Each image passes {@link ImageGuard} as a JPEG before it is held, and only the latest one per
 * source is kept, overwritten each time. Nothing reaches the disk or the media store, and nothing here
 * ever fetches: {@code GET /v1/ground-stations/{sourceId}/polarplot.jpeg} serves what the adapter last
 * fetched on its own schedule, or answers {@code 404 NO_ACTIVE_PASS}.
 */
@Component
public class LiveImageStore implements LiveImageHolder {

    /** The content type every held image has. */
    public static final String JPEG = "image/jpeg";

    /**
     * One held image.
     *
     * @param bytes the JPEG
     * @param fetchedAt when the adapter fetched it
     */
    public record LiveImage(byte[] bytes, Instant fetchedAt) {}

    private final ImageGuard guard;
    private final ConcurrentHashMap<String, LiveImage> images = new ConcurrentHashMap<>();

    /** Creates the store. */
    public LiveImageStore(ImageGuard guard) {
        this.guard = guard;
    }

    /**
     * Inspects {@code jpeg} with the guard and holds it as the latest image of {@code sourceId}.
     *
     * @throws ImageRejectedException when the guard refuses it; the previous image is dropped, so a
     *     stale plot is never served as current
     */
    @Override
    public void hold(String sourceId, byte[] jpeg, Instant fetchedAt) {
        try {
            guard.inspect(jpeg, JPEG, false);
        } catch (ImageRejectedException e) {
            images.remove(sourceId);
            throw e;
        }
        images.put(sourceId, new LiveImage(jpeg.clone(), fetchedAt));
    }

    @Override
    public void clear(String sourceId) {
        images.remove(sourceId);
    }

    /** The held image of {@code sourceId}, or empty when no pass is active. */
    public Optional<LiveImage> latest(String sourceId) {
        return Optional.ofNullable(images.get(sourceId));
    }
}
