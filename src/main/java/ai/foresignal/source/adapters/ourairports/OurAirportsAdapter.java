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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ourairports}: airports (and optional navaids) from OurAirports (prompt 094). Public domain.
 *
 * <p>{@code airports.csv} is rebuilt nightly; the conditional GET returns 304 when nothing changed.
 * {@code runways.csv} and {@code airport-frequencies.csv} give each airport how a VFR sectional chart draws it
 * ({@link AirportChart}: towered or not, its runway, its chart symbol). The airports file decides: a 304 on it
 * leaves the airports alone, and when it changed the other two are fetched whole and the airports rebuilt from
 * all three. OurAirports rebuilds every file nightly, so a weekly run sees a change and picks up changed runways
 * and frequencies with it. A run is one request when nothing changed and three when it did (four with
 * navaids), inside the source's budget of {@value #BUDGET_PER_MINUTE} a minute. The
 * {@code types} parameter selects airport types and {@code navaids=true} adds {@code navaids.csv} as kind
 * {@code navaid}. Each file is its own kind and is reconciled when it arrived fresh, so a 304 on one file never
 * tombstones that file's records and never blocks the other file's reconciliation (2026-10 review).
 */
@Component
public class OurAirportsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ourairports";

    /** The production airports CSV. */
    public static final String AIRPORTS_URL = "https://davidmegginson.github.io/ourairports-data/airports.csv";

    /** The production navaids CSV. */
    public static final String NAVAIDS_URL = "https://davidmegginson.github.io/ourairports-data/navaids.csv";

    /** The production runways CSV, for the chart symbols. */
    public static final String RUNWAYS_URL = "https://davidmegginson.github.io/ourairports-data/runways.csv";

    /** The production airport frequencies CSV, for the towered airports. */
    public static final String FREQUENCIES_URL =
            "https://davidmegginson.github.io/ourairports-data/airport-frequencies.csv";

    /** The test-only base-URL property. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ourairports.base-url";

    /** The airports conditional-GET state key. */
    public static final String AIRPORTS_STATE = "airports";

    /**
     * Requests a minute: a run with navaids makes four (airports, runways, frequencies, navaids), and a refused
     * request refuses the whole run, so the shared reference budget of two is too small.
     */
    public static final int BUDGET_PER_MINUTE = 4;

    /** Requests a day: seven runs at the largest. */
    public static final int BUDGET_PER_DAY = 28;

    /**
     * The longest a run may take: three downloads and the ingest of about 84,000 airports. On 2026-10-22 a full
     * rebuild on the throwaway stack was interrupted at the 60 s default after 10,000.
     */
    public static final Duration FETCH_TIMEOUT = Duration.ofMinutes(10);

    /**
     * The adapter-state key recording which build of the chart details the stored airports carry. A run that finds
     * another value (an install upgraded from airports without chart symbols) fetches {@code airports.csv} whole
     * rather than revalidating it, so every airport is rebuilt once; the value is written with the run's records,
     * so a failed run tries again.
     */
    public static final String CHART_STATE = "chart";

    /** The current {@link #CHART_STATE} value; raise it whenever {@link AirportChart} draws differently. */
    public static final String CHART_VERSION = "1";

    /** The navaids conditional-GET state key. */
    public static final String NAVAIDS_STATE = "navaids";

    /** The parameter selecting airport types. */
    public static final String TYPES = "types";

    /** The parameter adding the navaids file. */
    public static final String NAVAIDS = "navaids";

    private final URI airportsUri;
    private final URI navaidsUri;
    private final URI runwaysUri;
    private final URI frequenciesUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public OurAirportsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.airportsUri = URI.create(KitUrls.rebased(AIRPORTS_URL, baseUrlOverride));
        this.navaidsUri = URI.create(KitUrls.rebased(NAVAIDS_URL, baseUrlOverride));
        this.runwaysUri = URI.create(KitUrls.rebased(RUNWAYS_URL, baseUrlOverride));
        this.frequenciesUri = URI.create(KitUrls.rebased(FREQUENCIES_URL, baseUrlOverride));
    }

    /** The runways URL this adapter fetches. */
    public URI runwaysUri() {
        return runwaysUri;
    }

    /** The airport frequencies URL this adapter fetches. */
    public URI frequenciesUri() {
        return frequenciesUri;
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

        boolean chartCurrent = ctx.state().get(CHART_STATE).filter(CHART_VERSION::equals).isPresent();
        if (!chartCurrent) {
            SourceHttp.forgetValidators(ctx.state(), AIRPORTS_STATE);
        }
        UpstreamResponse airports = ctx.http().getConditional(airportsUri, AIRPORTS_STATE);
        if (!withNavaids && airports.notModified()) {
            return FetchResult.unchanged();
        }
        Set<String> fresh = new java.util.LinkedHashSet<>();
        String release;
        if (airports.notModified()) {
            release = null;
        } else {
            requireComplete(airports, AIRPORTS_STATE, ctx, "airports.csv");
            UpstreamResponse runways = ctx.http().get(runwaysUri);
            requireComplete(runways, AIRPORTS_STATE, ctx, "runways.csv");
            UpstreamResponse frequencies = ctx.http().get(frequenciesUri);
            requireComplete(frequencies, AIRPORTS_STATE, ctx, "airport-frequencies.csv");
            try {
                Map<String, AirportChart.Info> chart = AirportChart.index(runways.body(), frequencies.body());
                OurAirportsParser.Result result =
                        OurAirportsParser.parseAirports(airports.body(), types, chart, ctx::emitChunk);
                ctx.log().debug("{}: parsed {} airport(s), skipped {}", ID, result.records(), result.skipped());
            } catch (UpstreamFormatException e) {
                SourceHttp.forgetValidators(ctx.state(), AIRPORTS_STATE);
                throw e;
            }
            release = release(airports);
            ctx.state().put(CHART_STATE, CHART_VERSION);
            fresh.add(OurAirportsParser.AIRPORT_KIND);
        }

        if (withNavaids) {
            UpstreamResponse navaids = ctx.http().getConditional(navaidsUri, NAVAIDS_STATE);
            if (!navaids.notModified()) {
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
                fresh.add(OurAirportsParser.NAVAID_KIND);
            }
        }
        if (fresh.isEmpty()) {
            return FetchResult.unchanged();
        }
        return FetchResult.completeFor(fresh, List.of(), release);
    }

    /**
     * Fails the run unless {@code response} is a whole 200, forgetting the validators under {@code stateKey} so
     * the next run fetches that dataset again (a failed runways or frequencies file forgets the airports').
     */
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
                        + " region, codes and scheduled service, with a point geometry, and how a VFR sectional"
                        + " chart draws each one (towered or not, its longest runway, its chart symbol) from"
                        + " runways.csv and airport-frequencies.csv. The types parameter"
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
                .tileProperties("name", "type", "iataCode", "chartSymbol", "towered", "runwayHeading")
                .param(ParamSpec.optional(TYPES, ParamType.STRING,
                        "Comma-separated airport types to include", OurAirportsParser.DEFAULT_TYPES))
                .param(ParamSpec.optional(NAVAIDS, ParamType.BOOLEAN, "Also load navaids.csv as kind navaid",
                        Boolean.FALSE))
                .kindDisplay(OurAirportsParser.AIRPORT_KIND, AIRPORT_DISPLAY)
                .kindDisplay(OurAirportsParser.NAVAID_KIND, new KindDisplay("{name}", "antenna", "#7048E8", null)));
        // After the reference defaults, whose budget of two a minute would refuse every run.
        b.defaultBudget(BUDGET_PER_MINUTE, BUDGET_PER_DAY);
        b.fetchTimeout(FETCH_TIMEOUT);
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
        b.fieldSpec("towered", new FieldSpec(FieldType.BOOLEAN, null, "Towered",
                "Whether the airport has a control tower (it publishes a TWR frequency)", null, Set.of()));
        text(b, "runwaySurface", "Runway surface", "The longest runway's surface: hard, soft or water");
        integer(b, "longestRunwayFt", "Longest runway", "The longest open runway in feet", "[ft_i]");
        b.fieldSpec("runwayHeading", new FieldSpec(FieldType.NUMBER, "deg", "Runway heading",
                "The longest runway's true heading, 0 to 180 degrees", null, Set.of()));
        text(b, "chartSymbol", "Chart symbol", "How a VFR sectional chart draws the airport: major, hard, soft,"
                + " heliport, seaplane or closed");
        text(b, "ident", "Ident", "The navaid identifier");
        integer(b, "frequencyKhz", "Frequency", "Navaid radio frequency in kHz", "kHz");
        text(b, "usageType", "Usage", "The navaid usage type");
        text(b, "power", "Power", "The navaid power class");
        text(b, "associatedAirport", "Airport", "The navaid's associated airport");
        return b.build();
    }

    /**
     * The airport symbols of a VFR sectional chart: the {@code chartSymbol} picks the sprite icon and turns with
     * the runway heading, blue when towered and magenta otherwise. The two colors are lightened from the
     * chart's print colors so they read on a dark basemap.
     */
    static final KindDisplay AIRPORT_DISPLAY = new KindDisplay("{name}", "airport-soft", "#D04FB4", null,
            "runwayHeading", null, "towered", "chartSymbol",
            Map.of(AirportChart.MAJOR, "airport-major", AirportChart.HARD, "airport-hard",
                    AirportChart.SOFT, "airport-soft", AirportChart.HELIPORT, "airport-heliport",
                    AirportChart.SEAPLANE, "airport-seaplane", AirportChart.CLOSED, "airport-closed"),
            Map.of("true", "#4F8FE8"));

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void integer(SourceDescriptor.Builder b, String key, String label, String description,
            String unit) {
        b.fieldSpec(key, new FieldSpec(FieldType.INTEGER, unit, label, description, null, Set.of()));
    }
}
