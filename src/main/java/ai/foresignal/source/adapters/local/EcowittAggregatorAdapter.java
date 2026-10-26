package ai.foresignal.source.adapters.local;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code wxstation-ecowitt-aggregator} (prompt R2-023): the ground-sensor aggregator's Ecowitt gateway read,
 * configured by {@code PUT /v1/aggregators/ground-sensors} ({@code config.pws} with {@code station: ecowitt}) rather
 * than directly.
 *
 * <p>It is an {@link EcowittAdapter} with its own descriptor: the same request, parser and policy as the
 * {@code wxstation-ecowitt-{n}} slots.
 */
@Component
public class EcowittAggregatorAdapter extends EcowittAdapter {

    /** The source id. */
    public static final String ID = "wxstation-ecowitt-aggregator";

    /**
     * Creates the adapter.
     *
     * @param meters the registry of the unknown-id counter, resolved lazily
     */
    public EcowittAggregatorAdapter(ObjectProvider<MeterRegistry> meters) {
        super(buildDescriptor(ID, "Ecowitt gateway (ground-sensor aggregator)"),
                meters == null ? () -> null : meters::getIfAvailable);
    }
}
