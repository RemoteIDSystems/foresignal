package ai.foresignal.source.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Counts the upstream entries an adapter skipped, by reason (prompt 075), in one Micrometer counter tagged
 * {@code source} and {@code reason}. Nothing is counted when no registry is available (a unit test).
 */
public final class SkipCounter {

    private SkipCounter() {}

    /**
     * Adds {@code skipped} to {@code metric}.
     *
     * @param meters supplies the registry, or null
     * @param metric the counter name
     * @param description the counter's description
     * @param sourceId the {@code source} tag
     * @param skipped reason to count; empty counts nothing
     */
    public static void count(Supplier<MeterRegistry> meters, String metric, String description, String sourceId,
            Map<String, Integer> skipped) {
        MeterRegistry registry = meters == null ? null : meters.get();
        if (registry == null || skipped == null || skipped.isEmpty()) {
            return;
        }
        skipped.forEach((reason, n) -> Counter.builder(metric).tag("source", sourceId).tag("reason", reason)
                .description(description).register(registry).increment(n));
    }
}
