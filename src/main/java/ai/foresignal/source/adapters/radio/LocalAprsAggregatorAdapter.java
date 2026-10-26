package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.codec.KissAx25Codec;
import ai.foresignal.source.formats.aprs.AprsPacketDedup;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * {@code local-aprs-aggregator} (prompt R2-021): the datalink aggregator's APRS read — the operator's own igate's
 * TNC (for example Direwolf's KISS TCP port), configured by {@code PUT /v1/aggregators/datalink} rather than directly.
 *
 * <p>It is a {@link LocalAprsAdapter} with its own descriptor: the same KISS decoding, provably receive-only socket,
 * duplicate registration (so {@code aprs-is} drops its igated copy of a packet heard here) and overridable
 * {@code LOCAL_ONLY} egress. The aggregator writes its {@code endpoint}, {@code stationCallsign} and
 * {@code includeMessages} and turns it on while the aggregator is live with the APRS decoder selected.
 */
@Component
public class LocalAprsAggregatorAdapter extends LocalAprsAdapter {

    /** The source id. */
    public static final String ID = "local-aprs-aggregator";

    /**
     * Creates the adapter.
     *
     * @param codec the {@value KissAx25Codec#ID} codec
     * @param dedup the shared duplicate filter
     * @param meters the registry of the counters, resolved lazily
     */
    public LocalAprsAggregatorAdapter(KissAx25Codec codec, AprsPacketDedup dedup,
            ObjectProvider<MeterRegistry> meters) {
        super(describe(ID, "Local APRS aggregator (igate TNC)", "APRS heard on RF by the operator's own igate's TNC"
                + " (for example Direwolf's KISSPORT), read as KISS over TCP and configured by the datalink aggregator"
                + " (PUT /v1/aggregators/datalink), not directly. Provably receive-only: nothing is written to the"
                + " socket and Foresignal never igates. Same records as aprs-is; aprs-is drops its copy of a packet"
                + " heard here first. Disabled by default; egress LOCAL_ONLY (the operator may set it to OPEN)."),
                codec, dedup, meters == null ? () -> null : meters::getIfAvailable);
    }
}
