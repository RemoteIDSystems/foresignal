package ai.foresignal.collect;

import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Instant;

/**
 * Everything {@link HealthEvaluator} needs to derive one source's status.
 *
 * @param descriptor the source's descriptor (mode, {@code zeroResultsNormal})
 * @param enabled whether the source is enabled
 * @param health the health row, or {@link HealthSnapshot#empty(String)}
 * @param backoffUntil the live backoff deadline from {@link BackoffRegistry}, or null
 * @param lastHour usage over the last hour
 * @param budget what the budget has left, or null when the source has no request budget
 * @param stream the stream state for a streaming source, or null for any other mode
 * @param now the evaluation instant, from the injected clock
 */
public record HealthInputs(
        SourceDescriptor descriptor,
        boolean enabled,
        HealthSnapshot health,
        Instant backoffUntil,
        UsageTotalsWindow lastHour,
        BudgetStatus budget,
        StreamState stream,
        Instant now) {

    public HealthInputs {
        health = health == null ? HealthSnapshot.empty(descriptor.id()) : health;
        lastHour = lastHour == null ? UsageTotalsWindow.ZERO : lastHour;
    }

    /** The descriptor's {@code zeroResultsNormal}. */
    public boolean zeroResultsNormal() {
        return descriptor.zeroResultsNormal();
    }
}
