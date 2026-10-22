package ai.foresignal.source.adapters.adsbfi;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code adsbfi-blindspots}: the eight 250 nm circles of {@link AircraftRegion#BLIND_SPOTS} that the main feed
 * covers thinly, from adsb.fi, as Shadowbroker fills them.
 *
 * <p>Every 30 minutes, the circles one after another from {@code /api/v3/lat/{lat}/lon/{lon}/dist/250} with
 * 1.1 s between requests, merged by ICAO address. The record TTL is 35 minutes, so an aircraft seen here stays
 * on the map, at its last reported position, until the next poll replaces it. Free and keyless, so it starts
 * enabled.
 */
@Component
public class AdsbFiBlindSpotAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "adsbfi-blindspots";

    /** The poll interval. */
    static final Duration INTERVAL = Duration.ofMinutes(30);

    /** The shortest interval an operator may choose. */
    static final Duration MIN_INTERVAL = Duration.ofMinutes(5);

    /** The record TTL: one interval, the 240 s keyframe and the 30 s margin, rounded up. */
    static final Duration TTL = Duration.ofMinutes(35);

    /** The pause between two requests. */
    static final Duration PAUSE = Duration.ofMillis(1100);

    /** The longest a poll may take: eight requests and their pauses. */
    static final Duration FETCH_TIMEOUT = Duration.ofMinutes(4);

    /** Requests per minute: the eight of one poll, with a margin. */
    static final int PER_MINUTE = 10;

    /** Requests per day: eight every 30 minutes, with a margin. */
    static final int PER_DAY = 400;

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String origin;
    private final RegionPoller.Pauser pauser;

    /**
     * Creates the adapter.
     *
     * @param originOverride the value of {@code foresignal.test.adapters.adsbfi.base-url}; blank means
     *     {@code https://opendata.adsb.fi}
     */
    @Autowired
    public AdsbFiBlindSpotAdapter(@Value("${" + AdsbFi.BASE_URL_PROPERTY + ":}") String originOverride) {
        this(originOverride, RegionPoller.SLEEP);
    }

    /**
     * Creates the adapter with a chosen pauser (tests).
     *
     * @param originOverride the test origin, or blank
     * @param pauser waits between requests
     */
    AdsbFiBlindSpotAdapter(String originOverride, RegionPoller.Pauser pauser) {
        this.origin = AdsbFi.origin(originOverride);
        this.pauser = pauser;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL of one circle. */
    public URI uri(AircraftRegion region) {
        return URI.create(origin + "/api/v3/lat/" + region.latText() + "/lon/" + region.lonText() + "/dist/"
                + region.radiusNm());
    }

    /**
     * Fetches the eight circles one after another and merges them by ICAO address.
     *
     * @throws AdapterException when every circle failed
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return RegionPoller.serial(ctx, AircraftRegion.BLIND_SPOTS, PAUSE, Duration.ZERO, pauser,
                region -> AdsbFi.parse(ctx, ctx.http().get(uri(region))));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("adsb.fi blind spots")
                .description("Aircraft in eight 250 nm circles the worldwide feed covers thinly (Yekaterinburg,"
                        + " Novosibirsk, Krasnoyarsk, Vladivostok, Urumqi, Chengdu, Lagos-Accra, Addis Ababa), from"
                        + " adsb.fi community receivers (opendata.adsb.fi /api/v3/lat/lon/dist), one circle after"
                        + " another every 30 minutes, the gap-fill Shadowbroker runs. Each aircraft stays 35 minutes"
                        + " at its last reported position. Personal, non-commercial use only; cite adsb.fi.");
        AdsbFi.upstream(builder)
                .interval(MIN_INTERVAL, INTERVAL)
                .defaultBudget(PER_MINUTE, PER_DAY)
                .fetchTimeout(FETCH_TIMEOUT)
                .zeroResultsNormal(true);
        AircraftDescriptors.global(builder, TTL);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.civilDisplay());
        return builder.build();
    }
}
