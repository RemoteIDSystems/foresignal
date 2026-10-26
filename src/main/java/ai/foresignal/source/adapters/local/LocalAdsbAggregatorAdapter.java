package ai.foresignal.source.adapters.local;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * {@code local-adsb-aggregator} (prompt R2-019): the first-party ADS-B aggregator's read — the whole
 * {@code aircraft.json} of the operator's own readsb in one request, with no world tiling against a public host.
 *
 * <p>It is a {@link LocalAdsbAdapter} with its own descriptor: the same parse, staleness rule, policy and
 * {@code LOCAL_ONLY} egress (overridable), but sized for a feed that may carry the whole world (32 MB, 10 s timeout,
 * every 5 s by default). Its {@code url} is written by the ADS-B aggregator ({@code PUT /v1/aggregators/adsb}), which
 * also turns it on and off; an operator never needs to configure it directly. Its id carries the {@code local-adsb-}
 * prefix, so the derived aircraft layers (fusion, GPS jamming, squawks) read it like any local receiver.
 */
@Component
public class LocalAdsbAggregatorAdapter extends LocalAdsbAdapter {

    /** The source id. */
    public static final String ID = "local-adsb-aggregator";

    /** The response cap: a global aircraft.json is several megabytes. */
    static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    /** The request timeout. */
    static final Duration TIMEOUT = Duration.ofSeconds(10);

    /** Creates the adapter. */
    public LocalAdsbAggregatorAdapter() {
        super(buildDescriptor());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("Local ADS-B aggregator")
                .description("Every aircraft the operator's own readsb holds — from its own receivers, from feeds it"
                        + " aggregates, or both — read as one aircraft.json (the sdr-enthusiasts ultrafeeder container"
                        + " serves it at http://host/data/aircraft.json). Configured by the ADS-B aggregator"
                        + " (PUT /v1/aggregators/adsb), not directly. Every 5 s by default (1 s minimum). TRACK, TTL"
                        + " 5 min, observedAt = now - seen. MLAT positions are never redistributable."
                        + LocalReceivers.LOCAL_NOTE)
                .mode(Mode.POLLING)
                .interval(Duration.ofSeconds(1), Duration.ofSeconds(5))
                .defaultBudget(60, 100_000)
                .fetchTimeout(TIMEOUT)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .followRedirects(false)
                .zeroResultsNormal(true)
                .param(ParamSpec.lanUrl(URL_PARAM, "The readsb aircraft.json, set by the ADS-B aggregator, for example"
                        + " http://readsb/data/aircraft.json; http or https, no credentials, query or fragment", true,
                        "http", "https"))
                .param(ParamSpec.enumerated(BAND_PARAM, "1090 for Mode S/ADS-B, 978 for UAT", "1090",
                        List.of("1090", "978")));
        LocalReceivers.local(builder);
        AircraftDescriptors.local(builder, ObservedAtMode.UPSTREAM);
        builder.kindDisplay(AircraftFields.KIND, new KindDisplay("{flight}", "plane", "#0B7285", null,
                AircraftFields.TRACK, null, null));
        return builder.build();
    }
}
