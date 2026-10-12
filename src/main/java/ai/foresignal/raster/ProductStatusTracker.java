package ai.foresignal.raster;

import ai.foresignal.ingest.IngestStateReset;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Per-product conditions that are not frame age (prompt 042): a silent upstream format change
 * reported by a decoder or adapter, and a product disabled outright (prompt 046's decoder self-test).
 *
 * <p>Held in memory: a format change is re-detected on the next fetch after a restart, and a
 * self-test runs again at startup. A successful commit of the product clears its format change.
 */
@Component
public class ProductStatusTracker implements IngestStateReset {

    /**
     * A recorded condition.
     *
     * @param reason why
     * @param at when it was recorded
     */
    public record Report(String reason, Instant at) {}

    private final Map<String, Report> formatChanges = new ConcurrentHashMap<>();
    private final Map<String, Report> disabled = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher events;

    /** Creates the tracker. */
    public ProductStatusTracker(ApplicationEventPublisher events) {
        this.events = events;
    }

    /** Records a silent format change of {@code productId}. */
    public void formatChanged(String productId, String reason, Instant at) {
        formatChanges.put(productId, new Report(reason, at));
    }

    /** Clears {@code productId}'s format change (after a successful commit). */
    public void clearFormatChange(String productId) {
        formatChanges.remove(productId);
    }

    /** {@code productId}'s outstanding format change, if any. */
    public Optional<Report> formatChange(String productId) {
        return Optional.ofNullable(formatChanges.get(productId));
    }

    /**
     * Disables {@code productId}: its frame endpoints answer {@code 503 PRODUCT_UNAVAILABLE}.
     *
     * @param sourceId the owning source, for the event
     */
    public void disable(String productId, String sourceId, String reason, Instant at) {
        disabled.put(productId, new Report(reason, at));
        events.publishEvent(new ProductCatalogChangedEvent(productId, sourceId,
                ProductCatalogChangedEvent.Change.DISABLED));
    }

    /** Re-enables {@code productId}. */
    public void enable(String productId, String sourceId) {
        if (disabled.remove(productId) != null) {
            events.publishEvent(new ProductCatalogChangedEvent(productId, sourceId,
                    ProductCatalogChangedEvent.Change.ENABLED));
        }
    }

    /** Why {@code productId} is disabled, if it is. */
    public Optional<Report> disabled(String productId) {
        return Optional.ofNullable(disabled.get(productId));
    }

    /** Forgets everything (tests). */
    public void reset() {
        formatChanges.clear();
        disabled.clear();
    }

    /** Forgets everything when the test cleaner empties the tables. */
    @Override
    public void resetState() {
        reset();
    }
}
