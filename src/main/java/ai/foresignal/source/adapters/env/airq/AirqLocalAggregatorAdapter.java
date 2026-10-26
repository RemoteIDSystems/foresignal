package ai.foresignal.source.adapters.env.airq;

import org.springframework.stereotype.Component;

/**
 * {@code airq-local-aggregator} (prompt R2-023): the ground-sensor aggregator's air-quality read — the operator's own
 * PurpleAir, airrohr or AirGradient monitor, configured by {@code PUT /v1/aggregators/ground-sensors}
 * ({@code config.airq}) rather than directly.
 *
 * <p>It is an {@link AirqLocalAdapter} with its own descriptor: the same parameters, parsers, policy, privacy and
 * {@code LOCAL_ONLY}, overridable egress as the {@code airq-local-{n}} slots.
 */
@Component
public class AirqLocalAggregatorAdapter extends AirqLocalAdapter {

    /** The source id. */
    public static final String ID = "airq-local-aggregator";

    /** Creates the adapter. */
    public AirqLocalAggregatorAdapter() {
        super(buildDescriptor(ID, "Local air-quality monitor (ground-sensor aggregator)"));
    }
}
