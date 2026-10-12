package ai.foresignal.stats.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * The metric projections, {@code app_setting['metrics']} (OpenAPI {@code MetricSettings}, prompt 038).
 *
 * @param projections the projections, in order
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MetricSettings(List<MetricProjection> projections) {

    /** Copies the list. */
    public MetricSettings {
        projections = projections == null ? List.of() : List.copyOf(projections);
    }

    /** No projections. */
    public static MetricSettings empty() {
        return new MetricSettings(List.of());
    }
}
