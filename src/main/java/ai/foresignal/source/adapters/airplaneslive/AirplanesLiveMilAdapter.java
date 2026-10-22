package ai.foresignal.source.adapters.airplaneslive;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.formats.readsb.ReadsbResponses;
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
 * {@code airplaneslive-mil}: aircraft flagged military in airplanes.live's database, worldwide (prompt 080).
 *
 * <p>One GET of {@code /v2/mil} per poll, every 60 s as Shadowbroker polls it, paced 1.1 s per host. Anonymous access is refused today
 * (see the package note): the 403 becomes {@code UpstreamAccessDeniedException} with the reason
 * {@value #ACCESS_REASON}, an hourly retry and health {@code failing}.
 */
@Component
public class AirplanesLiveMilAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "airplaneslive-mil";

    /** Test-only override of the origin (Adapter Guide §8); the request path is kept. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.airplaneslive.base-url";

    /** The production origin. */
    static final String DEFAULT_ORIGIN = "https://api.airplanes.live";

    /** The request path. */
    static final String PATH = "/v2/mil";

    /** The text of airplanes.live's approval refusal that the reason is keyed on. */
    public static final String ACCESS_BODY_KEY = "contact@airplanes.live";

    /** The health reason of the approval refusal. */
    public static final String ACCESS_REASON = "access approval required (contact airplanes.live)";

    /** The spacing between request starts (1 request per second, plus a margin). */
    static final Duration HOST_PACING = Duration.ofMillis(1100);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String origin;

    /**
     * Creates the adapter.
     *
     * @param originOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@code https://api.airplanes.live}
     */
    @Autowired
    public AirplanesLiveMilAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String originOverride) {
        String chosen = originOverride == null || originOverride.isBlank() ? DEFAULT_ORIGIN : originOverride.trim();
        this.origin = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL. */
    public URI uri() {
        return URI.create(origin + PATH);
    }

    /**
     * Fetches the military list once.
     *
     * @return every mapped aircraft as a complete result
     * @throws AdapterException when the upstream fails or refuses this client (today: the approval 403)
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ReadsbAircraftMapper.MapResult mapped = ReadsbResponses.map(ctx.http().get(uri()), "airplanes.live");
        if (mapped.skipped() > 0) {
            ctx.log().debug("{}: skipped {} aircraft without hex", ctx.sourceId(), mapped.skipped());
        }
        return FetchResult.complete(mapped.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("airplanes.live military aircraft")
                .description("Aircraft flagged military in the airplanes.live database, worldwide (api.airplanes.live"
                        + " /v2/mil). Anonymous requests are refused with HTTP 403 until airplanes.live approves the"
                        + " project by email (contact@airplanes.live); the source then fails with that reason and is"
                        + " retried hourly. adsbfi-mil is the keyless alternative. Non-commercial, no SLA. TRACK with"
                        + " a 6-minute TTL and a trail point every 60 s.")
                .mode(Mode.POLLING)
                .homepage("https://airplanes.live")
                .attribution("airplanes.live")
                .licence("airplanes.live API terms: non-commercial use, no SLA, attribution required")
                .termsUrl("https://airplanes.live/api-guide/")
                .egress(Egress.LOCAL_ONLY)
                .accessDeniedIsTerminal(true)
                .accessDeniedReason(ACCESS_BODY_KEY, ACCESS_REASON)
                .hostPacing(HOST_PACING)
                .interval(Duration.ofSeconds(30), Duration.ofSeconds(60))
                .defaultBudget(2, 2_880);
        AircraftDescriptors.global(builder);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.militaryDisplay());
        return builder.build();
    }
}
