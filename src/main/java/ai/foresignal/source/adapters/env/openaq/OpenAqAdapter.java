package ai.foresignal.source.adapters.env.openaq;

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
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code openaq} (prompt 078): the latest OpenAQ v3 measurement of every sensor reporting the chosen parameters, as
 * {@code air-quality} point records ({@link OpenAqLatestParser}).
 *
 * <ul>
 *   <li><strong>Key:</strong> credential {@value #API_KEY}, sent as the {@value #KEY_HEADER} header (verified
 *       2026-10: {@code /v3/parameters} answers 401 without it). The upstream client redacts it from every trace.
 *   <li><strong>Parameters:</strong> names from param {@value #PARAMETERS_PARAM} resolve to ids through
 *       {@link OpenAqParameterResolver} (one {@code /v3/parameters} request per 24 h, cached in state).
 *   <li><strong>Each run:</strong> per parameter id, {@code GET /v3/parameters/{id}/latest?limit=1000&page={n}
 *       &datetime_min={now - maxAge}} until a page shorter than 1000 (at most {@value #MAX_PAGES} pages). The v3
 *       OpenAPI document lists {@code limit, page, datetime_min} for this endpoint and no {@code bbox}, so the optional
 *       {@value #BBOX_PARAM} filters after parsing; {@code datetime_min} drops stale sensors on the server.
 *   <li><strong>Limits:</strong> free keys get 60 requests/min and 2,000/hour (docs.openaq.org "Rate limits",
 *       2026-10); the default budget is 20/min and 2,000/day.
 * </ul>
 */
@Component
public class OpenAqAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "openaq";

    /** The API root. */
    public static final String API = "https://api.openaq.org";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The header carrying the key. */
    public static final String KEY_HEADER = "X-API-Key";

    /** The parameter-names parameter. */
    public static final String PARAMETERS_PARAM = "parameters";

    /** The optional area filter. */
    public static final String BBOX_PARAM = "bbox";

    /** The parameter names this source accepts. */
    public static final List<String> ALLOWED_PARAMETERS = List.of("pm25", "pm10", "o3", "no2", "so2", "co");

    /** The default parameter names. */
    public static final List<String> DEFAULT_PARAMETERS = List.of("pm25");

    /** Results per page. */
    public static final int PAGE_SIZE = 1000;

    /** The most pages read per parameter id. */
    public static final int MAX_PAGES = 30;

    /** The default {@code maxAge}. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(6);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(3);

    /** Raw retention: sensor readings are the {@code state} class (365 d). */
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
    public OpenAqAdapter(@Value("${" + EnvSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public OpenAqAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.api = EnvSources.uri(API, baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The parameters list URL. */
    public URI parametersUri() {
        return URI.create(api + "/v3/parameters?limit=" + PAGE_SIZE);
    }

    /** The URL of page {@code page} of parameter {@code id}'s latest values measured at or after {@code since}. */
    public URI latestUri(long id, int page, Instant since) {
        return URI.create(api + "/v3/parameters/" + id + "/latest?limit=" + PAGE_SIZE + "&page=" + page
                + "&datetime_min=" + since.truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Fetches the latest values (see the class note).
     *
     * @throws AdapterException when the key is missing or a request fails (a 401 included)
     * @throws UpstreamFormatException when an answer is not the v3 shape
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a free key from"
                    + " explore.openaq.org)");
        }
        Map<String, String> headers = Map.of(KEY_HEADER, key.strip(), "Accept", "application/json");
        ValidatedParams params = ctx.params();
        Instant now = ctx.now();
        Duration maxAge = EnvSources.maxAge(params, DEFAULT_MAX_AGE);
        List<String> names = params == null || params.stringList(PARAMETERS_PARAM).isEmpty() ? DEFAULT_PARAMETERS
                : params.stringList(PARAMETERS_PARAM);
        Envelope box = params != null && params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : null;

        List<OpenAqParameterResolver.Parameter> known = resolve(ctx, headers, now);
        EnvSources.Collector collector = new EnvSources.Collector(now, maxAge);
        for (Map.Entry<String, List<OpenAqParameterResolver.Parameter>> e
                : OpenAqParameterResolver.byName(known, names).entrySet()) {
            if (e.getValue().isEmpty()) {
                ctx.log().warn("{}: OpenAQ lists no parameter named '{}'", ID, e.getKey());
                collector.skip("unknown-parameter");
            }
            for (OpenAqParameterResolver.Parameter parameter : e.getValue()) {
                readParameter(ctx, headers, parameter, now.minus(maxAge), box, collector);
            }
        }
        EnvSources.Parsed parsed = collector.result();
        SkipCounter.count(meters, EnvSources.SKIPPED_METRIC, EnvSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} measurement(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private void readParameter(FetchContext ctx, Map<String, String> headers, OpenAqParameterResolver.Parameter p,
            Instant since, Envelope box, EnvSources.Collector collector) throws AdapterException {
        for (int page = 1; page <= MAX_PAGES; page++) {
            URI uri = latestUri(p.id(), page, since);
            OpenAqLatestParser.Page parsed = OpenAqLatestParser.parse(
                    EnvSources.body(ctx, ctx.http().get(uri, headers), null, uri), p);
            for (MappedRecord m : parsed.entries()) {
                if (m.accepted() && !EnvSources.inBox(box, m.record().geometry().getCoordinate().x,
                        m.record().geometry().getCoordinate().y)) {
                    collector.skip(EnvSources.OUTSIDE_AREA);
                } else {
                    collector.add(m);
                }
            }
            if (parsed.resultCount() < PAGE_SIZE) {
                return;
            }
        }
        ctx.log().warn("{}: parameter {} ({}) has more than {} pages; the rest is not read", ID, p.name(), p.id(),
                MAX_PAGES);
    }

    /** The parameter list: the cache, refreshed when due; a failed refresh falls back to an existing cache. */
    private List<OpenAqParameterResolver.Parameter> resolve(FetchContext ctx, Map<String, String> headers,
            Instant now) throws AdapterException {
        List<OpenAqParameterResolver.Parameter> cached = OpenAqParameterResolver.cached(ctx.state());
        if (!cached.isEmpty() && !OpenAqParameterResolver.due(ctx.state(), now)) {
            return cached;
        }
        try {
            URI uri = parametersUri();
            List<OpenAqParameterResolver.Parameter> fresh = OpenAqParameterResolver.parse(
                    EnvSources.body(ctx, ctx.http().get(uri, headers), null, uri));
            OpenAqParameterResolver.store(ctx.state(), fresh, now);
            return fresh;
        } catch (AdapterException | UpstreamFormatException e) {
            if (cached.isEmpty()) {
                throw e;
            }
            ctx.log().warn("{}: the parameter list could not be refreshed ({}); using the cached list", ID,
                    e.getMessage());
            return cached;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = OpenAqLatestParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("OpenAQ air quality")
                .description("The latest measurement of every OpenAQ sensor reporting the chosen parameters (pm25 by"
                        + " default; pm10, o3, no2, so2 and co allowed), from the OpenAQ v3 API. Needs a free API key."
                        + " Readings older than maxAge (6 h) are skipped; optional bbox keeps one area.")
                .kind(kind)
                .homepage("https://openaq.org/")
                .attribution("OpenAQ; data licenses vary by provider (see each location's licenses)")
                .licence("various (OpenAQ providers)")
                .termsUrl("https://docs.openaq.org/about/terms")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(API_KEY, "OpenAQ API key (free from explore.openaq.org), sent"
                        + " as the X-API-Key header"))
                .param(new ParamSpec(PARAMETERS_PARAM, ParamType.STRING_LIST, "Parameter names to read, one request"
                        + " series each", DEFAULT_PARAMETERS, false, ALLOWED_PARAMETERS))
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX, "Keep only sensors inside this box"
                        + " (west,south,east,north); filtered after parsing", null))
                .param(EnvSources.maxAgeParam("6h"))
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(20, 2000))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .zeroResultsNormal(true)
                .tileProperties("parameter", "value", "unit")
                .searchFields("parameter")
                .rankField("value");
        b.fieldSpec("parameter", new FieldSpec(FieldType.STRING, null, "Parameter",
                "OpenAQ parameter name (pm25, pm10, o3, no2, so2, co)", null, Set.of()));
        b.fieldSpec("value", new FieldSpec(FieldType.NUMBER, null, "Value",
                "The measured value, in the record's unit (UCUM code in 'unit')", null, Set.of()));
        b.fieldSpec("unit", new FieldSpec(FieldType.STRING, null, "Unit",
                "UCUM code of the value's unit (ug/m3, [ppm], [ppb])", null, Set.of()));
        b.fieldSpec("sensorId", new FieldSpec(FieldType.INTEGER, null, "Sensor id", "OpenAQ sensors id", null,
                Set.of()));
        b.fieldSpec("locationId", new FieldSpec(FieldType.INTEGER, null, "Location id", "OpenAQ locations id", null,
                Set.of()));
        b.kindDisplay(kind, new KindDisplay("{parameter} {value}", null, "#2F9E44", null));
        return b.build();
    }
}
