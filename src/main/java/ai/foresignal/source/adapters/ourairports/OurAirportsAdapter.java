package ai.foresignal.source.adapters.ourairports;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.ReferenceSupport;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ourairports}: airports (and optional navaids) from OurAirports (prompt 094). Public domain.
 *
 * <p>{@code airports.csv} is rebuilt nightly; the conditional GET returns 304 when nothing changed. The
 * {@code types} parameter selects airport types and {@code navaids=true} adds {@code navaids.csv} as kind
 * {@code navaid}. Complete-set reconciliation runs only when every fetched file arrived fresh, so a 304 on one
 * file never tombstone's the other's records.
 */
@Component
public class OurAirportsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ourairports";

    /** The production airports CSV. */
    public static final String AIRPORTS_URL = "https://davidmegginson.github.io/ourairports-data/airports.csv";

    /** The production navaids CSV. */
    public static final String NAVAIDS_URL = "https://davidmegginson.github.io/ourairports-data/navaids.csv";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ourairports.base-url";

    /** The airports conditional-GET state key. */
    public static final String AIRPORTS_STATE = "airports";

    /** The navaids conditional-GET state key. */
    public static final String NAVAIDS_STATE = "navaids";

    /** The parameter selecting airport types. */
    public static final String TYPES = "types";

    /** The parameter adding the navaids file. */
    public static final String NAVAIDS = "navaids";

    private final URI airportsUri;
    private final URI navaidsUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public OurAirportsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.airportsUri = URI.create(KitUrls.rebased(AIRPORTS_URL, baseUrlOverride));
        this.navaidsUri = URI.create(KitUrls.rebased(NAVAIDS_URL, baseUrlOverride));
    }

    /** The airports URL this adapter fetches. */
    public URI airportsUri() {
        return airportsUri;
    }

    /** The navaids URL this adapter fetches. */
    public URI navaidsUri() {
        return navaidsUri;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String typesValue = ctx.params().string(TYPES);
        // The descriptor's default is applied by the collector; fall back to it when a caller (a test
        // harness, or a fetch context without defaults) leaves the parameter absent.
        Set<String> types = OurAirportsParser.types(
                typesValue == null ? OurAirportsParser.DEFAULT_TYPES : typesValue);
        boolean withNavaids = ctx.params().bool(NAVAIDS, false);

        UpstreamResponse airports = ctx.http().getConditional(airportsUri, AIRPORTS_STATE);
        if (!withNavaids && airports.notModified()) {
            return FetchResult.unchanged();
        }
        boolean allFresh = true;
        String release;
        if (airports.notModified()) {
            allFresh = false;
            release = null;
        } else {
            requireComplete(airports, AIRPORTS_STATE, ctx, "airports.csv");
            try {
                OurAirportsParser.Result result = OurAirportsParser.parseAirports(airports.body(), types, ctx::emitChunk);
                ctx.log().debug("{}: parsed {} airport(s), skipped {}", ID, result.records(), result.skipped());
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), AIRPORTS_STATE);
                throw e;
            }
            release = release(airports);
        }

        if (withNavaids) {
            UpstreamResponse navaids = ctx.http().getConditional(navaidsUri, NAVAIDS_STATE);
            if (navaids.notModified()) {
                allFresh = false;
            } else {
                requireComplete(navaids, NAVAIDS_STATE, ctx, "navaids.csv");
                try {
                    OurAirportsParser.Result result = OurAirportsParser.parseNavaids(navaids.body(), ctx::emitChunk);
                    ctx.log().debug("{}: parsed {} navaid(s), skipped {}", ID, result.records(), result.skipped());
                } catch (UpstreamFormatException e) {
                    SourceHttp.forgetValidators(ctx.state(), NAVAIDS_STATE);
                    throw e;
                }
                String navaidsRelease = release(navaids);
                release = release == null ? navaidsRelease : release + ";navaids=" + navaidsRelease;
            }
        }
        return allFresh
                ? FetchResult.complete(List.of(), release)
                : FetchResult.of(List.of());
    }

    private static void requireComplete(UpstreamResponse response, String stateKey, FetchContext ctx, String what) {
        if (response.status() != 200 || response.truncated()) {
            SourceHttp.forgetValidators(ctx.state(), stateKey);
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? ": " + what + " answered HTTP " + response.status()
                    : ": " + what + " exceeds the response cap"), "");
        }
    }

    private static String release(UpstreamResponse response) {
        String etag = response.header("ETag");
        if (etag != null && !etag.isBlank()) {
            return etag;
        }
        return response.header("Last-Modified");
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = ReferenceSupport.defaults(SourceDescriptor.builder(ID)
                .name("OurAirports")
                .description("Airports from OurAirports, rebuilt nightly: name, type, elevation, country and"
                        + " region, codes and scheduled service, with a point geometry. The types parameter"
                        + " selects large, medium and small airports by default; navaids=true adds navaids.csv"
                        + " as kind navaid. Public domain.")
                .kind(OurAirportsParser.AIRPORT_KIND)
                .kind(OurAirportsParser.NAVAID_KIND)
                .kind("dataset-release")
                .homepage("https://ourairports.com/data/")
                .attribution("OurAirports, https://ourairports.com")
                .licence("Public domain")
                .termsUrl("https://ourairports.com/data/")
                .titleField("name")
                .searchFields("name", "municipality", "ident")
                .tileProperties("name", "type", "iataCode")
                .param(ParamSpec.optional(TYPES, ParamType.STRING,
                        "Comma-separated airport types to include", OurAirportsParser.DEFAULT_TYPES))
                .param(ParamSpec.optional(NAVAIDS, ParamType.BOOLEAN, "Also load navaids.csv as kind navaid",
                        Boolean.FALSE))
                .kindDisplay(OurAirportsParser.AIRPORT_KIND, new KindDisplay("{name}", "airport", "#1C7ED6", null))
                .kindDisplay(OurAirportsParser.NAVAID_KIND, new KindDisplay("{name}", "antenna", "#7048E8", null)));
        text(b, "name", "Name", "The airport or navaid name");
        text(b, "type", "Type", "The airport type (large_airport, medium_airport, small_airport, heliport,"
                + " seaplane_base, balloonport or closed) or the navaid type");
        integer(b, "elevationFt", "Elevation", "Elevation in feet (MSL)", "[ft_i]");
        text(b, "isoCountry", "Country", "ISO 3166-1 alpha-2 country");
        text(b, "isoRegion", "Region", "ISO 3166-2 region");
        text(b, "municipality", "Municipality", "The city or municipality");
        text(b, "scheduledService", "Scheduled service", "yes or no");
        text(b, "icaoCode", "ICAO code", "The ICAO code, when published");
        text(b, "iataCode", "IATA code", "The IATA code, when published");
        text(b, "gpsCode", "GPS code", "The GPS code, when published");
        text(b, "localCode", "Local code", "The local code, when published");
        text(b, "ident", "Ident", "The navaid identifier");
        integer(b, "frequencyKhz", "Frequency", "Navaid radio frequency in kHz", "kHz");
        text(b, "usageType", "Usage", "The navaid usage type");
        text(b, "power", "Power", "The navaid power class");
        text(b, "associatedAirport", "Airport", "The navaid's associated airport");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void integer(SourceDescriptor.Builder b, String key, String label, String description,
            String unit) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }
}
