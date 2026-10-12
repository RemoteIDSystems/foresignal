package ai.foresignal.stats.metrics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * One metric projection (OpenAPI {@code MetricProjection}, prompt 038): a payload field of a source's
 * current records exposed as the gauge {@code foresignal_data_<name>}, with a companion
 * {@code foresignal_data_<name>_observed_timestamp_seconds}.
 *
 * @param name {@code ^[a-z_][a-z0-9_]*$}, unique
 * @param source the source id
 * @param kind only records of this kind, or null for all
 * @param path the payload path in the 017 trigger syntax (dotted steps), not a volatile field
 * @param labels at most 4 labels read from payload paths; when empty, each series is labelled
 *     {@code source} and {@code external_id}
 * @param unit the unit, shown in the help text, or null
 * @param help the help text, or null
 * @param maxSeries the most series of this projection (default 500)
 * @param allowPrivate true to allow a private source
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MetricProjection(String name, String source, String kind, String path, List<Label> labels, String unit,
        String help, Integer maxSeries, Boolean allowPrivate) {

    /** The default {@code maxSeries}. */
    public static final int DEFAULT_MAX_SERIES = 500;

    /** Normalizes nulls. */
    public MetricProjection {
        labels = labels == null ? List.of() : List.copyOf(labels);
        maxSeries = maxSeries == null ? DEFAULT_MAX_SERIES : maxSeries;
        allowPrivate = allowPrivate != null && allowPrivate;
    }

    /** The exposed gauge name. */
    public String metricName() {
        return "foresignal_data_" + name;
    }

    /** The exposed observed-timestamp gauge name. */
    public String timestampName() {
        return metricName() + "_observed_timestamp_seconds";
    }

    /**
     * One label.
     *
     * @param name the label name ({@code ^[a-zA-Z_][a-zA-Z0-9_]*$})
     * @param path the payload path of its value
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Label(String name, String path) {}
}
