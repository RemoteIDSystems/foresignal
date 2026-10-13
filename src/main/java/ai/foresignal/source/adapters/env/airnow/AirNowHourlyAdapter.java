package ai.foresignal.source.adapters.env.airnow;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code airnow-hourly} (prompt 078): U.S. EPA AirNow hourly monitor values in one box, as {@code air-quality}
 * point records ({@link AirNowParser}).
 *
 * <ul>
 *   <li><strong>Key:</strong> credential {@value #API_KEY}, sent as the {@value #KEY_QUERY} query parameter
 *       (verified 2026-10: 401 without it); the upstream client redacts it from every trace.
 *   <li><strong>Each run:</strong> one request for the newest hour expected to be posted, {@link #hourFor}: AirNow
 *       posts the previous hour's values 10 to 30 minutes past the hour (AirNow API FAQ), so at {@code now} the
 *       hour starting at {@code truncate(now - 30 min) - 1 h} is read. The scheduler has no phase offset, so this
 *       choice gives the 30-minute offset whatever minute the poll runs at.
 *   <li><strong>Not used:</strong> the keyless hourly files under {@code files.airnowtech.org}.
 * </ul>
 */
@Component
public class AirNowHourlyAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "airnow-hourly";

    /** The data endpoint. */
    public static final String API = "https://www.airnowapi.org/aq/data/";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The query parameter carrying the key. */
    public static final String KEY_QUERY = "API_KEY";

    /** The parameters parameter. */
    public static final String PARAMETERS_PARAM = "parameters";

    /** The box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** The AirNow parameter codes accepted. */
    public static final List<String> ALLOWED_PARAMETERS = List.of("PM25", "PM10", "OZONE", "CO", "NO2", "SO2");

    /** The default parameter codes. */
    public static final List<String> DEFAULT_PARAMETERS = List.of("PM25", "OZONE");

    /** How long after the hour the previous hour's values are taken as posted. */
    public static final Duration POSTING_OFFSET = Duration.ofMinutes(30);

    /** {@code 2026-10-13T20}. */
    static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH", Locale.ROOT)
            .withZone(ZoneOffset.UTC);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(3);

    /** Raw retention: monitor readings are the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    private final String api;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value EnvSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public AirNowHourlyAdapter(@Value("${" + EnvSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public AirNowHourlyAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.api = EnvSources.uri(API, baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The start of the hour read at {@code now}: {@code truncate(now - 30 min, hours) - 1 h}. */
    public static Instant hourFor(Instant now) {
        return now.minus(POSTING_OFFSET).truncatedTo(ChronoUnit.HOURS).minus(Duration.ofHours(1));
    }

    /** The request URL for {@code hour}, {@code parameters} and {@code box}, with the key appended. */
    public URI requestUri(Instant hour, List<String> parameters, Envelope box, String key) {
        String h = HOUR.format(hour);
        return URI.create(api + "?startDate=" + h + "&endDate=" + h + "&parameters=" + String.join(",", parameters)
                + "&BBOX=" + box.getMinX() + "," + box.getMinY() + "," + box.getMaxX() + "," + box.getMaxY()
                + "&dataType=B&format=application/json&verbose=1&monitorType=0&includerawconcentrations=0&"
                + KEY_QUERY + "=" + URLEncoder.encode(key, StandardCharsets.UTF_8));
    }

    /**
     * Reads one hour (see the class note).
     *
     * @throws AdapterException when the key or the box is missing, or the request fails (a 401 included)
     * @throws UpstreamFormatException when the answer is not a JSON array
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a free key from"
                    + " docs.airnowapi.org)");
        }
        ValidatedParams params = ctx.params();
        Envelope box = params != null && params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : null;
        if (box == null || box.isNull()) {
            throw new AdapterException(ID + ": the bbox parameter is required");
        }
        List<String> parameters = params.stringList(PARAMETERS_PARAM).isEmpty() ? DEFAULT_PARAMETERS
                : params.stringList(PARAMETERS_PARAM).stream().map(p -> p.strip().toUpperCase(Locale.ROOT)).toList();
        URI uri = requestUri(hourFor(ctx.now()), parameters, box, key.strip());
        EnvSources.Parsed parsed = AirNowParser.parse(
                EnvSources.body(ctx, ctx.http().get(uri, Map.of("Accept", "application/json")), null, uri),
                ctx.now(), EnvSources.maxAge(params, AirNowParser.DEFAULT_MAX_AGE));
        SkipCounter.count(meters, EnvSources.SKIPPED_METRIC, EnvSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} monitor value(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = AirNowParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("AirNow hourly")
                .description("Hourly U.S. EPA AirNow monitor values (concentration and AQI) in one box, PM2.5 and ozone"
                        + " by default. Needs a free AirNow API key; the box is required. Each run reads the newest"
                        + " posted hour (values post about 10 to 30 minutes past the hour). Data are preliminary.")
                .kind(kind)
                .homepage("https://www.airnow.gov/")
                .attribution("U.S. EPA AirNow; data are preliminary and not fully verified")
                .licence("US-PD")
                .termsUrl("https://docs.airnowapi.org/docs/DataUseGuidelines.pdf")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(API_KEY, "AirNow API key (free from docs.airnowapi.org), sent as"
                        + " the API_KEY query parameter"))
                .param(new ParamSpec(PARAMETERS_PARAM, ParamType.STRING_LIST, "AirNow parameter codes", DEFAULT_PARAMETERS,
                        false, ALLOWED_PARAMETERS))
                .param(ParamSpec.required(BBOX_PARAM, ParamType.BBOX, "The box to read, west,south,east,north"))
                .param(EnvSources.maxAgeParam("6h"))
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(60))
                .defaultBudget(Budget.of(2, 100))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .zeroResultsNormal(true)
                .tileProperties("parameter", "aqi", "aqiCategory")
                .searchFields("siteName", "agency", "parameter")
                .titleField("siteName")
                .rankField("aqi");
        text(b, "parameter", "Parameter", "AirNow parameter (PM2.5, PM10, OZONE, CO, NO2, SO2)");
        b.fieldSpec("value", new FieldSpec(FieldType.NUMBER, null, "Value",
                "The hourly concentration, in the record's unit (UCUM code in 'unit')", null, Set.of()));
        text(b, "unit", "Unit", "UCUM code of the value's unit (ug/m3, [ppb], [ppm])");
        b.fieldSpec("aqi", new FieldSpec(FieldType.INTEGER, "1", "AQI", "U.S. EPA Air Quality Index", null, Set.of()));
        b.fieldSpec("aqiCategory", new FieldSpec(FieldType.INTEGER, "1", "AQI category", "1 Good, 2 Moderate, 3"
                + " Unhealthy for Sensitive Groups, 4 Unhealthy, 5 Very Unhealthy, 6 Hazardous", null, Set.of()));
        text(b, "siteName", "Site", "Monitoring site name");
        text(b, "agency", "Agency", "Reporting agency");
        b.kindDisplay(kind, new KindDisplay("{parameter} AQI {aqi}", null, "#2F9E44", null));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
