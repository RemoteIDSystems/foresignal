package ai.foresignal.source.adapters.adsblol;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.AircraftRegion;
import ai.foresignal.source.formats.readsb.RegionPoller;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code adsblol-world}: every aircraft adsb.lol sees, worldwide, as Shadowbroker's main aircraft feed polls it.
 *
 * <p>Each poll requests the six continent-sized circles of {@link AircraftRegion#WORLD} at once from
 * {@code /v2/lat/{lat}/lon/{lon}/dist/{dist}} (unlike {@code /v2/point}, this path takes radii far above 250 nm),
 * and keeps each aircraft once, from the first circle that reports it. Every 60 s, budget 12 per minute (six
 * requests a poll) and 9,000 per day. A circle that fails leaves the others' aircraft standing; the poll fails
 * only when all six do. Free and keyless, so it starts enabled.
 */
@Component
public class AdsbLolWorldAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "adsblol-world";

    /** The poll interval: Shadowbroker's fast tier. */
    static final Duration INTERVAL = Duration.ofSeconds(60);

    /** Requests per minute: the six of one poll, twice over for a manual fetch. */
    static final int PER_MINUTE = 12;

    /** Requests per day: six a minute, with a margin. */
    static final int PER_DAY = 9_000;

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@code foresignal.test.adapters.adsblol.base-url}; blank means
     *     {@code https://api.adsb.lol}
     */
    public AdsbLolWorldAdapter(@Value("${" + AdsbLol.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = AdsbLol.baseUrl(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL of one circle. */
    public URI uri(AircraftRegion region) {
        return URI.create(baseUrl + "/v2/lat/" + region.latText() + "/lon/" + region.lonText() + "/dist/"
                + region.radiusNm());
    }

    /**
     * Fetches the six circles at once and merges them by ICAO address.
     *
     * @throws AdapterException when every circle failed
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return RegionPoller.parallel(ctx, AircraftRegion.WORLD,
                region -> AdsbLol.parse(ctx, ctx.http().get(uri(region))));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("adsb.lol worldwide aircraft")
                .description("Every aircraft adsb.lol community receivers see, worldwide: six continent-sized circles"
                        + " (North America, Europe, Asia, Australia 2,000 nm; Africa 2,500 nm; South America 2,000"
                        + " nm) requested together every 60 s from api.adsb.lol /v2/lat/lon/dist and merged by ICAO"
                        + " address, the coverage and cadence of Shadowbroker's main aircraft feed. TRACK with a"
                        + " 6-minute TTL and a trail point every 60 s.");
        AdsbLol.upstream(builder)
                .interval(INTERVAL, INTERVAL)
                .defaultBudget(PER_MINUTE, PER_DAY);
        AircraftDescriptors.global(builder);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.civilDisplay());
        return builder.build();
    }
}
