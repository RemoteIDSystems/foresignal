package ai.foresignal.trigger;

import ai.foresignal.security.EgressPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Where and when an alert's enrichment context is gathered (prompt 030): the alert, the anchor time
 * {@code T = alert.observedAt}, the anchor geometry (the alerted version's, else the snapshot's),
 * the rule's radius and window, and the LLM destination the context is for.
 *
 * @param alert the alert being enriched
 * @param geometryJson the anchor geometry as GeoJSON (SRID 4326), or null when the record has none
 * @param time the anchor time {@code T}
 * @param radiusKm the rule's {@code enrichRadiusKm}
 * @param window the rule's {@code enrichWindow}; the context covers {@code T ± window}
 * @param destination the chat provider the context will be sent to
 * @param excludedSources sources whose records may not be sent to {@code destination}
 */
public record EnrichmentAnchor(AlertView alert, String geometryJson, Instant time, double radiusKm, Duration window,
        EgressPolicy.LlmDestination destination, Set<String> excludedSources) {

    /** Requires the alert, time, window and destination; copies the exclusions. */
    public EnrichmentAnchor {
        Objects.requireNonNull(alert, "alert");
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(destination, "destination");
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }

    /** True when the alerted record has a geometry, so the spatial parts apply. */
    public boolean spatial() {
        return geometryJson != null;
    }

    /** The radius in meters. */
    public double radiusMeters() {
        return radiusKm * 1000.0;
    }

    /** The start of the window, {@code T − window}. */
    public Instant from() {
        return time.minus(window);
    }

    /** The end of the window, {@code T + window}. */
    public Instant to() {
        return time.plus(window);
    }

    /** True when records of {@code sourceId} may be put into this context. */
    public boolean allows(String sourceId) {
        return sourceId != null && !excludedSources.contains(sourceId);
    }

    /** {@link #allows} as a predicate. */
    public Predicate<String> allowed() {
        return this::allows;
    }
}
