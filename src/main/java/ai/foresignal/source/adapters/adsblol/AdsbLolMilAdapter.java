package ai.foresignal.source.adapters.adsblol;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.formats.readsb.ReadsbAircraftMapper;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code adsblol-mil}: aircraft flagged military in adsb.lol's database, worldwide (prompt 062).
 *
 * <p>One GET of {@code /v2/mil} per poll: 30 s by default (15 s minimum), budget 4 per minute and 3,000 per
 * day. adsb.lol publishes no numbers and its limits are dynamic, so the source stays at this rate. The
 * result is the complete current set, but absent aircraft are never tombstoned: the 6-minute TTL retires
 * them.
 *
 * <p>An HTTP 200 that is not JSON is an {@code UpstreamFormatException} ("non-JSON 200, treated as
 * throttling"); a 451 or 403 is a refusal of this client (see the package note).
 */
@Component
public class AdsbLolMilAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "adsblol-mil";

    /** The test property replacing the upstream base URL. */
    public static final String BASE_URL_PROPERTY = AdsbLol.BASE_URL_PROPERTY;

    /** The request path. */
    static final String PATH = "/v2/mil";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@code https://api.adsb.lol}
     */
    public AdsbLolMilAdapter(@Value("${" + AdsbLol.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = AdsbLol.baseUrl(baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The request URL. */
    public URI uri() {
        return URI.create(baseUrl + PATH);
    }

    /**
     * Fetches the military list once.
     *
     * @return every mapped aircraft as a complete result
     * @throws AdapterException when the upstream fails or refuses this client
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri());
        ReadsbAircraftMapper.MapResult mapped = AdsbLol.parse(ctx, response);
        return FetchResult.complete(mapped.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("adsb.lol military aircraft")
                .description("Aircraft flagged military in the adsb.lol database, worldwide, from community ADS-B"
                        + " and MLAT receivers (api.adsb.lol /v2/mil). TRACK with a 6-minute TTL and the 240 s"
                        + " keyframe: keyframe <= ttl - interval - 30 s holds at the default 30 s interval and up to"
                        + " 90 s; a slower interval needs a shorter keyframe in the ingest policy.");
        AdsbLol.upstream(builder)
                // adsb.lol publishes no limits and adjusts them dynamically; stay at this rate.
                .interval(Duration.ofSeconds(15), Duration.ofSeconds(30))
                .defaultBudget(4, 3_000);
        AircraftDescriptors.global(builder);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.militaryDisplay());
        return builder.build();
    }
}
