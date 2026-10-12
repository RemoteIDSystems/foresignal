package ai.foresignal.collect;

import ai.foresignal.source.HealthStatus;
import java.time.Instant;

/**
 * A source's derived health status changed (prompt 018). {@link HealthService} publishes it as a
 * Spring event whenever an evaluation (an API read or the 30-second gauge refresh) yields a status
 * different from the previous evaluation in this process; the notification router turns a change
 * into {@code failing} into a {@code system.source_failing} notification.
 *
 * @param sourceId the source
 * @param previous the previous status
 * @param current the new status
 * @param reason the new status's reason, or null
 * @param at when the change was observed
 */
public record SourceHealthChangedEvent(
        String sourceId, HealthStatus previous, HealthStatus current, String reason, Instant at) {}
