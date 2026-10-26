package ai.foresignal.source.adapters.local;

import org.springframework.stereotype.Component;

/**
 * {@code wxstation-davis-aggregator} (prompt R2-023): the ground-sensor aggregator's Davis WeatherLink Live read,
 * configured by {@code PUT /v1/aggregators/ground-sensors} ({@code config.pws} with {@code station: davis}) rather
 * than directly.
 *
 * <p>It is a {@link DavisWllAdapter} with its own descriptor: the same request, parser, schedule and policy as the
 * {@code wxstation-davis-{n}} slots.
 */
@Component
public class DavisWllAggregatorAdapter extends DavisWllAdapter {

    /** The source id. */
    public static final String ID = "wxstation-davis-aggregator";

    /** Creates the adapter. */
    public DavisWllAggregatorAdapter() {
        super(buildDescriptor(ID, "Davis WeatherLink Live (ground-sensor aggregator)"));
    }
}
