package ai.foresignal.source.adapters.adsbfi;

import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.formats.readsb.AircraftFields;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code adsbfi-mil}: aircraft flagged military in adsb.fi's database, worldwide (prompt 080), the keyless
 * alternative to {@code airplaneslive-mil}.
 *
 * <p>One GET of {@code /api/v2/mil} per poll (readsb {@code ac[]} with {@code dbFlags}, {@code mlat[]} and
 * {@code tisb[]}), every 30 s, paced with the other adsb.fi sources (see the package note). The result is the
 * complete current set, but absent aircraft are never tombstoned: the 6-minute TTL retires them.
 *
 * <p>Off by default: Shadowbroker's military feed is adsb.lol and airplanes.live only, so this source is an
 * addition the operator turns on.
 */
@Component
public class AdsbFiMilAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "adsbfi-mil";

    /** The test property replacing the upstream origin. */
    public static final String BASE_URL_PROPERTY = AdsbFi.BASE_URL_PROPERTY;

    /** The request path. */
    static final String PATH = "/api/v2/mil";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String origin;

    /**
     * Creates the adapter.
     *
     * @param originOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@code https://opendata.adsb.fi}
     */
    @Autowired
    public AdsbFiMilAdapter(@Value("${" + AdsbFi.BASE_URL_PROPERTY + ":}") String originOverride) {
        this.origin = AdsbFi.origin(originOverride);
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
     * @throws AdapterException when the upstream fails or refuses this client
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        return FetchResult.complete(AdsbFi.parse(ctx, ctx.http().get(uri())).records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("adsb.fi military aircraft")
                .description("Aircraft flagged military in the adsb.fi database, worldwide, from community ADS-B and"
                        + " MLAT receivers (opendata.adsb.fi /api/v2/mil). Personal, non-commercial use only; cite"
                        + " adsb.fi. Requests to opendata.adsb.fi are serial across every adsbfi-* source, at least"
                        + " 1.1 s apart. TRACK with a 6-minute TTL and a trail point every 60 s. Off by default.");
        AdsbFi.upstream(builder)
                .autoEnable(false);
        AircraftDescriptors.global(builder);
        builder.kindDisplay(AircraftFields.KIND, AircraftDescriptors.militaryDisplay());
        return builder.build();
    }
}
