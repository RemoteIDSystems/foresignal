package ai.foresignal.source.adapters.acars;

import ai.foresignal.source.codec.AcarsJsonCodec;
import ai.foresignal.source.spi.Budget;
import org.springframework.stereotype.Component;

/**
 * {@code local-acars-aggregator} (prompt R2-021): the datalink aggregator's ACARS, VDL Mode 2 and HFDL read — the
 * operator's own acars_router (fed by acarsdec, dumpvdl2 and dumphfdl), configured by {@code PUT
 * /v1/aggregators/datalink} rather than directly.
 *
 * <p>It is a {@link LocalAcarsAdapter} with its own descriptor: the same three endpoint parameters, metadata-only
 * mapping, privacy by descriptor and fixed {@code LOCAL_ONLY} egress. The aggregator writes its endpoints and
 * {@code stationId} and turns it on while the aggregator is live with an ACARS, VDLM2 or HFDL decoder selected.
 */
@Component
public class LocalAcarsAggregatorAdapter extends LocalAcarsAdapter {

    /** The source id. */
    public static final String ID = "local-acars-aggregator";

    /**
     * Creates the adapter.
     *
     * @param codec the {@value AcarsJsonCodec#ID} codec
     */
    public LocalAcarsAggregatorAdapter(AcarsJsonCodec codec) {
        super(describe(ID, "Local aviation datalink aggregator", "ACARS, VDL Mode 2 and HFDL message metadata from the"
                + " operator's own acars_router (fed by acarsdec, dumpvdl2 and dumphfdl), configured by the datalink"
                + " aggregator (PUT /v1/aggregators/datalink), not directly. Metadata only: message text is never"
                + " stored, logged, embedded or forwarded (18 U.S.C. §2511, UK Wireless Telegraphy Act 2006 s.48)."
                + " Private by descriptor. Receive-only. Disabled by default; egress LOCAL_ONLY, not overridable.",
                Budget.of(6, 600)), codec);
    }
}
