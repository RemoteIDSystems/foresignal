package ai.foresignal.source.aggregator;

import java.util.List;
import java.util.Map;

/**
 * One domain's first-party aggregator configuration (prompt R2-019), stored under the setting
 * {@code aggregator.<domain>}.
 *
 * @param enabled whether the aggregator is on (default false)
 * @param mode {@code receiver}, {@code feed} or {@code hybrid}, or null
 * @param endpoint the local URL its source reads, or null
 * @param config domain-specific settings
 * @param disableSupersededSources whether the public/mirror sources it supersedes are kept off while it is live
 *     (default true)
 * @param disabledByAggregator the superseded sources this aggregator turned off, so turning it off can turn exactly
 *     those back on
 */
public record AggregatorSettings(
        boolean enabled,
        String mode,
        String endpoint,
        Map<String, Object> config,
        Boolean disableSupersededSources,
        List<String> disabledByAggregator) {

    /** The modes. */
    public static final List<String> MODES = List.of("receiver", "feed", "hybrid");

    /** Fills defaults. */
    public AggregatorSettings {
        config = config == null ? Map.of() : Map.copyOf(config);
        disableSupersededSources = disableSupersededSources == null ? Boolean.TRUE : disableSupersededSources;
        disabledByAggregator = disabledByAggregator == null ? List.of() : List.copyOf(disabledByAggregator);
    }

    /** Off, with nothing configured. */
    public static AggregatorSettings defaults() {
        return new AggregatorSettings(false, null, null, Map.of(), true, List.of());
    }

    /** Whether it is on with an endpoint to read. */
    public boolean live() {
        return enabled && endpoint != null;
    }

    /** This configuration with {@code ids} as the sources it turned off. */
    public AggregatorSettings withDisabledByAggregator(List<String> ids) {
        return new AggregatorSettings(enabled, mode, endpoint, config, disableSupersededSources, ids);
    }
}
