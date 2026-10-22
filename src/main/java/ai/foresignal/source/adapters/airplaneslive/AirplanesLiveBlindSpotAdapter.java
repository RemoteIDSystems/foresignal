package ai.foresignal.source.adapters.airplaneslive;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.AircraftRegion;
import ai.foresignal.source.formats.readsb.ReadsbResponses;
import ai.foresignal.source.formats.readsb.RegionPoller;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code airplaneslive-blindspots}: the eight 250 nm circles of {@link AircraftRegion#BLIND_SPOTS} from
 * airplanes.live, as Shadowbroker fills them.
 *
 * <p>Every 30 minutes, the circles one after another from {@code /v2/point/{lat}/{lon}/250}, with 1.2 s plus up
 * to 0.4 s of random jitter between requests, merged by ICAO address. The record TTL is 35 minutes. Anonymous
 * access is refused today, exactly as for {@code airplaneslive-mil} (see the package note): the first 403 stops
 * the poll, health is {@code failing} with {@value AirplanesLiveMilAdapter#ACCESS_REASON}, and it is retried
 * hourly.
 */
@Component
public class AirplanesLiveBlindSpotAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "airplaneslive-blindspots";

    /** The poll interval. */
    static final Duration INTERVAL = Duration.ofMinutes(30);

    /** The shortest interval an operator may choose. */
    static final Duration MIN_INTERVAL = Duration.ofMinutes(5);

    /** The record TTL: one interval, the 240 s keyframe and the 30 s margin, rounded up. */
    static final Duration TTL = Duration.ofMinutes(35);

    /** The fixed part of the pause between two requests. */
    static final Duration PAUSE = Duration.ofMillis(1200);

    /** The most random jitter added to {@link #PAUSE}. */
    static final Duration JITTER = Duration.ofMillis(400);

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
     * @param originOverride the value of {@value AirplanesLiveMilAdapter#BASE_URL_PROPERTY}; blank means
     *     {@code https://api.airplanes.live}
     */
    @Autowired
    public AirplanesLiveBlindSpotAdapter(
            @Value("${" + AirplanesLiveMilAdapter.BASE_URL_PROPERTY + ":}") String originOverride) {
        this(originOverride, RegionPoller.SLEEP);
    }

    /**
     * Creates the adapter with a chosen pauser (tests).
     *
     * @param originOverride the test origin, or blank
     * @param pauser waits between requests
     */
    AirplanesLiveBlindSpotAdapter(String originOverride, RegionPoller.Pauser pauser) {
        String chosen = originOverride == null || originOverride.isBlank()
                ? AirplanesLiveMilAdapter.DEFAULT_ORIGIN : originOverride.trim();
        this.origin = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.pauser = pauser;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL of one circle. */
    public URI uri(AircraftRegion region) {
        return URI.create(origin + "/v2/point/" + region.latText() + "/" + region.lonText() + "/"
                + region.radiusNm());
    }

    /**
     * Fetches the eight circles one after another and merges them by ICAO address.
     *
     * @throws AdapterException when every circle failed (today: the approval 403, then backoff)
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return RegionPoller.serial(ctx, AircraftRegion.BLIND_SPOTS, PAUSE, JITTER, pauser,
                region -> ReadsbResponses.map(ctx.http().get(uri(region)), "airplanes.live"));
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("airplanes.live blind spots")
                .description("Aircraft in eight 250 nm circles the worldwide feed covers thinly (Yekaterinburg,"
                        + " Novosibirsk, Krasnoyarsk, Vladivostok, Urumqi, Chengdu, Lagos-Accra, Addis Ababa), from"
                        + " airplanes.live (api.airplanes.live /v2/point), one circle after another every 30 minutes,"
                        + " the gap-fill Shadowbroker runs. Anonymous requests are refused with HTTP 403 until"
                        + " airplanes.live approves the project by email (contact@airplanes.live); the source then"
                        + " fails with that reason and is retried hourly. adsbfi-blindspots covers the same circles."
                        + " Each aircraft stays 35 minutes at its last reported position. Non-commercial, no SLA.")
                .mode(Mode.POLLING)
                .homepage("https://airplanes.live")
                .attribution("airplanes.live")
                .licence("airplanes.live API terms: non-commercial use, no SLA, attribution required")
                .termsUrl("https://airplanes.live/api-guide/")
                .egress(Egress.LOCAL_ONLY)
                .accessDeniedIsTerminal(true)
                .accessDeniedReason(AirplanesLiveMilAdapter.ACCESS_BODY_KEY, AirplanesLiveMilAdapter.ACCESS_REASON)
                .hostPacing(AirplanesLiveMilAdapter.HOST_PACING)
                .interval(MIN_INTERVAL, INTERVAL)
                .defaultBudget(PER_MINUTE, PER_DAY)
                .fetchTimeout(FETCH_TIMEOUT)
                .zeroResultsNormal(true);
        AircraftDescriptors.global(builder, TTL);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.civilDisplay());
        return builder.build();
    }
}
