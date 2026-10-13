package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * METARs from the Aviation Weather Center cache file ({@code awc-metars}, prompt 047).
 *
 * <p>One conditional GET of {@code /data/cache/metars.cache.csv.gz} per run (ETag, Last-Modified and
 * {@code max-age=30} verified 2026-10-13; the file is served as {@code application/octet-stream}
 * without {@code Content-Encoding}, so the adapter inflates it). AWC asks for at most one request per
 * minute per thread and 100 per minute overall; the 5-minute poll complies. A 304 is unchanged; a
 * truncated or unreadable body forgets the validators and fails the run.
 *
 * <p>Scope: {@code aoi} (default; the places-AOI envelope), {@code conus} or {@code global};
 * {@code stations} overrides it with an allowlist. One record per station, {@code externalId} = the
 * station id, a new report is a new version and an identical re-fetch is a touch. Old reports leave
 * through the 3-hour TTL anchored on the observation time, so a station that stops reporting never
 * reads as current; nothing is tombstoned.
 */
@Component
public class AwcMetarsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "awc-metars";

    /** The record kind. */
    public static final String KIND = MetarCsvParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://aviationweather.gov";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.awc.base-url";

    /** The cache file path. */
    public static final String PATH = "/data/cache/metars.cache.csv.gz";

    /** The {@code scope} parameter. */
    public static final String SCOPE_PARAM = "scope";

    /** The {@code stations} parameter. */
    public static final String STATIONS_PARAM = "stations";

    /** The scope values. */
    public static final List<String> SCOPES = List.of("aoi", "conus", "global");

    /** The conterminous US box used by {@code scope=conus}. */
    static final double[] CONUS = {-125.0, 24.0, -66.5, 50.0};

    /** The AOI used when no AOI service is wired (unit tests): the Texas box. */
    static final double[] DEFAULT_ENVELOPE = {-106.65, 25.84, -93.51, 36.50};

    /** The adapter-state key of the file's validators. */
    static final String STATE_KEY = "metars";

    /** Retention: Weather-Imagery-Design §2.4 keeps {@code metar} for 7 days. */
    static final Duration RETENTION = Duration.ofDays(7);

    /** A report stays current for 3 hours after its observation time. */
    static final Duration TTL = Duration.ofHours(3);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final AreaOfInterest aoi;
    private final String baseUrl;

    /**
     * Creates the adapter for Spring.
     *
     * @param aoi the places AOI
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means the real site
     */
    @Autowired
    public AwcMetarsAdapter(ObjectProvider<AreaOfInterest> aoi, @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(aoi == null ? null : aoi.getIfAvailable(), baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param aoi the places AOI, or null for the default Texas box
     * @param baseUrlOverride a base URL, or blank for the real site
     */
    public AwcMetarsAdapter(AreaOfInterest aoi, String baseUrlOverride) {
        this.aoi = aoi;
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        if (aoi != null) {
            aoi.registerConsumer(ID);
        }
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The cache file URL. */
    public URI cacheUri() {
        return URI.create(baseUrl + PATH);
    }

    /** The station filter for the given parameters. */
    MetarCsvParser.Filter filter(String scope, List<String> stations) {
        Set<String> allow = new LinkedHashSet<>();
        for (String station : stations) {
            if (station != null && !station.isBlank()) {
                allow.add(station.strip().toUpperCase(Locale.ROOT));
            }
        }
        double[] envelope = switch (scope == null ? "aoi" : scope) {
            case "global" -> null;
            case "conus" -> CONUS.clone();
            default -> aoi == null ? DEFAULT_ENVELOPE.clone() : aoi.envelope();
        };
        return new MetarCsvParser.Filter(envelope, allow);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        MetarCsvParser.Filter filter = filter(ctx.params().optionalString(SCOPE_PARAM).orElse("aoi"),
                ctx.params().stringList(STATIONS_PARAM));
        UpstreamResponse response = ctx.http().getConditional(cacheUri(), STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        MetarCsvParser.ParseResult parsed;
        try {
            if (response.truncated()) {
                throw new UpstreamFormatException("AWC METAR file exceeded maxResponseBytes and was truncated", null);
            }
            parsed = MetarCsvParser.parse(response.body(), filter);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        ctx.log().debug("AWC METARs: {}", parsed.diagnostics());
        return FetchResult.complete(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("AWC METARs")
                .description("Surface weather reports (METAR and SPECI) from the NOAA Aviation Weather Center cache"
                        + " file, one record per station. AWC asks for at most 1 request per minute per thread and"
                        + " 100 per minute overall; the default 5-minute poll complies.")
                .kind(KIND)
                .homepage("https://aviationweather.gov/data/api/")
                .attribution("NOAA/NWS Aviation Weather Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .param(ParamSpec.enumerated(SCOPE_PARAM,
                        "aoi (the places-AOI envelope), conus or global; ignored when stations is set", "aoi", SCOPES))
                .param(ParamSpec.optional(STATIONS_PARAM, ParamType.STRING_LIST,
                        "ICAO station allowlist, for example KAUS, KDFW; overrides scope", null))
                .interval(Duration.ofMinutes(1), Duration.ofMinutes(5))
                .defaultBudget(Budget.of(2, 400, 100))
                .maxResponseBytes(4 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("stationId")
                .searchFields("stationId", "raw")
                .tileProperties("stationId", "flightCategory", "tempC", "windKt", "gustKt", "visibilitySm")
                .kindDisplay(KIND, new KindDisplay("{stationId} {flightCategory}", "station", "#1971C2", null));
        string(builder, "raw", "Report", "The raw METAR or SPECI text");
        string(builder, "stationId", "Station", "ICAO station identifier");
        string(builder, "metarType", "Type", "METAR or SPECI");
        number(builder, "tempC", "Cel", "Temperature", "Air temperature");
        number(builder, "dewpointC", "Cel", "Dew point", "Dew point temperature");
        builder.fieldSpec("windDirDeg", new FieldSpec(FieldType.INTEGER, "deg", "Wind direction",
                "Direction the wind blows from; left out when variable", null, Set.of()));
        bool(builder, "windVariable", "Variable wind", "True when the wind direction is variable (VRB)");
        builder.fieldSpec("windKt", new FieldSpec(FieldType.INTEGER, "[kn_i]", "Wind speed", "Wind speed", null, Set.of()));
        builder.fieldSpec("gustKt", new FieldSpec(FieldType.INTEGER, "[kn_i]", "Gust", "Wind gust", null, Set.of()));
        number(builder, "visibilitySm", "[mi_i]", "Visibility", "Visibility in statute miles");
        bool(builder, "visibilityPlus", "Visibility or more", "True when visibility is at least visibilitySm (10+ or 6+)");
        number(builder, "altimInHg", "[in_i'Hg]", "Altimeter", "Altimeter setting");
        number(builder, "slpMb", "mbar", "Sea-level pressure", "Sea-level pressure");
        string(builder, "wx", "Weather", "Present weather, for example -RA BR");
        builder.fieldSpec("sky", new FieldSpec(FieldType.ARRAY, null, "Sky",
                "Cloud layers: cover (FEW, SCT, BKN, OVC, OVX, CLR, SKC) and baseFtAgl in feet above ground", null,
                Set.of()));
        string(builder, "flightCategory", "Flight category", "VFR, MVFR, IFR or LIFR");
        builder.fieldSpec("elevationM", new FieldSpec(FieldType.NUMBER, "m", "Elevation", "Station elevation",
                "MSL", Set.of()));
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }

    private static void bool(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.BOOLEAN, null, label, description, null, Set.of()));
    }
}
