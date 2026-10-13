package ai.foresignal.source.adapters.hydro.usgswater;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.kit.feature.OgcFeaturesClient;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
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
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code usgs-water} (prompt 079): USGS real-time gauge readings through the Water Data OGC API (the legacy
 * {@code waterservices.usgs.gov} NWIS service is being retired and is never called).
 *
 * <ul>
 *   <li><strong>Each run:</strong> {@code {base}/collections/latest-continuous/items?f=json&limit=1000
 *       &parameter_code={codes}&bbox={bbox}} (or {@code monitoring_location_id={list}} instead of the box), cursor
 *       paging through {@code links[rel=next]} by the prompt 073 {@link OgcFeaturesClient}, at most
 *       {@value #MAX_PAGES} pages;
 *   <li><strong>Names:</strong> once per 24 h, {@code collections/monitoring-locations/items} for the sites the
 *       run saw ({@code id={list}}, {@value #NAME_BATCH} per request, only the name and site type), cached in state
 *       ({@value #NAMES_KEY}); a failed refresh keeps the cache;
 *   <li><strong>Key:</strong> the optional credential {@value #API_KEY} goes in the {@code X-Api-Key} header (the
 *       upstream client redacts it); each page's {@code X-RateLimit-Remaining} is kept in state
 *       ({@value #RATE_LIMIT_KEY}) and logged, warning below {@value #LOW_RATE_LIMIT}.
 * </ul>
 */
@Component
public class UsgsWaterAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "usgs-water";

    /** The OGC API landing URL. */
    public static final String API_URL = "https://api.waterdata.usgs.gov/ogcapi/v1";

    /** The readings collection. */
    public static final String LATEST = "latest-continuous";

    /** The site collection. */
    public static final String LOCATIONS = "monitoring-locations";

    /** The optional key credential. */
    public static final String API_KEY = "apiKey";

    /** The header carrying the key. */
    public static final String KEY_HEADER = "X-Api-Key";

    /** The box parameter. */
    public static final String BBOX_PARAM = "bbox";

    /** The parameter-code list parameter. */
    public static final String PARAMETER_CODES_PARAM = "parameterCodes";

    /** The site list parameter, instead of the box. */
    public static final String LOCATIONS_PARAM = "monitoringLocations";

    /** The default parameter codes: discharge and gage height. */
    public static final List<String> DEFAULT_PARAMETER_CODES = List.of("00060", "00065");

    /** The largest box side, degrees. */
    public static final double MAX_BOX_DEGREES = 5.0;

    /** The most sites in {@value #LOCATIONS_PARAM}. */
    public static final int MAX_LOCATIONS = 100;

    /** Pages read per run. */
    public static final int MAX_PAGES = 20;

    /** Features per page. */
    public static final int PAGE_SIZE = 1000;

    /** Sites per name request. */
    public static final int NAME_BATCH = 100;

    /** The state key of the name table. */
    public static final String NAMES_KEY = "usgs:names";

    /** The state key of the name table's read time, epoch seconds. */
    public static final String NAMES_AT_KEY = "usgs:namesAt";

    /** The state key of the last {@code X-RateLimit-Remaining}. */
    public static final String RATE_LIMIT_KEY = "usgs:rateLimitRemaining";

    /** Below this remaining hourly allowance a warning is logged. */
    public static final int LOW_RATE_LIMIT = 50;

    /** How long the name table is used before it is read again. */
    public static final Duration NAMES_REFRESH = Duration.ofHours(24);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(3);

    private static final Pattern PARAMETER_CODE = Pattern.compile("^\\d{5}$");
    private static final Pattern LOCATION = Pattern.compile("^[A-Z]+-[0-9A-Za-z]+$");

    private final URI api;
    private final Supplier<MeterRegistry> meters;
    private final OgcFeaturesClient ogc = new OgcFeaturesClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public UsgsWaterAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, HydroSources.meters(meters));
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public UsgsWaterAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.api = HydroSources.uri(API_URL, baseUrlOverride);
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Requires a box of at most 5° × 5° or a site list, and five-digit parameter codes. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> locations = params.stringList(LOCATIONS_PARAM);
        Envelope box = params.has(BBOX_PARAM) ? params.bbox(BBOX_PARAM) : null;
        if (locations.isEmpty()) {
            String problem = HydroSources.boxProblem(box, MAX_BOX_DEGREES);
            if (problem != null) {
                problems.put(BBOX_PARAM, problem + (box == null ? ", or list " + LOCATIONS_PARAM : ""));
            }
        } else if (locations.size() > MAX_LOCATIONS) {
            problems.put(LOCATIONS_PARAM, "at most " + MAX_LOCATIONS + " sites, but " + locations.size()
                    + " were given");
        } else {
            List<String> bad = locations.stream().filter(l -> !LOCATION.matcher(l).matches()).toList();
            if (!bad.isEmpty()) {
                problems.put(LOCATIONS_PARAM, "not monitoring location ids (USGS-06710385): " + bad);
            }
        }
        List<String> badCodes = params.stringList(PARAMETER_CODES_PARAM).stream()
                .filter(c -> !PARAMETER_CODE.matcher(c).matches()).toList();
        if (!badCodes.isEmpty()) {
            problems.put(PARAMETER_CODES_PARAM, "not five-digit USGS parameter codes: " + badCodes);
        }
        return problems;
    }

    /**
     * Reads the latest readings, refreshes the names when due, and maps them.
     *
     * @throws AdapterException when the parameters are invalid or a readings request fails
     * @throws UpstreamFormatException when a readings page is not a feature collection
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        Map<String, String> headers = new LinkedHashMap<>();
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key != null && !key.isBlank()) {
            headers.put(KEY_HEADER, key.strip());
        }
        FeatureFetch fetch = ogc.fetch(ctx, latestRequest(params, headers), rateLimit(ctx));
        TreeSet<String> sites = new TreeSet<>();
        for (JsonNode feature : fetch) {
            JsonNode p = feature.get("properties");
            String site = p == null ? null : HydroSources.text(p.get("monitoring_location_id"));
            if (site != null) {
                sites.add(site);
            }
        }
        Map<String, UsgsLatestContinuousParser.Site> names = names(ctx, sites, headers);
        EnvSources.Parsed parsed = UsgsLatestContinuousParser.parse(fetch.features(), names);
        SkipCounter.count(meters, HydroSources.SKIPPED_METRIC, HydroSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} reading(s) in {} page(s) ({}), skipped {}", ID, parsed.records().size(), fetch.pages(),
                fetch.stopReason(), parsed.skipped());
        if (!fetch.complete()) {
            ctx.log().warn("{}: paging stopped early ({}); returning a partial set", ID, fetch.stopReason());
            return FetchResult.of(parsed.records());
        }
        return FetchResult.complete(parsed.records());
    }

    /** The readings query for {@code params}. */
    OgcFeaturesClient.Request latestRequest(ValidatedParams params, Map<String, String> headers) {
        List<String> codes = params.stringList(PARAMETER_CODES_PARAM);
        if (codes.isEmpty()) {
            codes = DEFAULT_PARAMETER_CODES;
        }
        Map<String, String> filters = new LinkedHashMap<>();
        filters.put("parameter_code", String.join(",", codes));
        List<String> locations = params.stringList(LOCATIONS_PARAM);
        List<Double> bbox = List.of();
        if (!locations.isEmpty()) {
            filters.put("monitoring_location_id", String.join(",", locations));
        } else {
            bbox = HydroSources.bboxList(params.bbox(BBOX_PARAM));
        }
        return new OgcFeaturesClient.Request(api, LATEST, PAGE_SIZE, MAX_PAGES, PAGE_SIZE * MAX_PAGES, bbox, null,
                filters, headers, Map.of());
    }

    /**
     * The name table: the cache, refreshed for {@code sites} when it is older than {@link #NAMES_REFRESH}.
     *
     * @param sites the sites of this run
     * @param headers the request headers (the key)
     */
    Map<String, UsgsLatestContinuousParser.Site> names(FetchContext ctx, TreeSet<String> sites,
            Map<String, String> headers) {
        AdapterStateStore state = ctx.state();
        Map<String, UsgsLatestContinuousParser.Site> cached =
                UsgsLatestContinuousParser.sitesFromJson(EnvSources.unpack(state.get(NAMES_KEY).orElse(null)));
        if (sites.isEmpty() || !HydroSources.due(state, NAMES_AT_KEY, ctx.now(), NAMES_REFRESH)) {
            return cached;
        }
        Map<String, UsgsLatestContinuousParser.Site> fresh = new TreeMap<>();
        try {
            List<String> all = new ArrayList<>(sites);
            for (int from = 0; from < all.size(); from += NAME_BATCH) {
                List<String> batch = all.subList(from, Math.min(all.size(), from + NAME_BATCH));
                Map<String, String> filters = new LinkedHashMap<>();
                filters.put("id", String.join(",", batch));
                filters.put("properties", "monitoring_location_name,site_type_code");
                filters.put("skipGeometry", "true");
                FeatureFetch page = ogc.fetch(ctx, new OgcFeaturesClient.Request(api, LOCATIONS, PAGE_SIZE, 1,
                        PAGE_SIZE, List.of(), null, filters, headers, Map.of()), rateLimit(ctx));
                fresh.putAll(UsgsLatestContinuousParser.sites(page.features()));
            }
        } catch (AdapterException | UpstreamFormatException e) {
            ctx.log().warn("{}: monitoring-locations could not be read ({}); {} cached name(s) kept", ID,
                    e.getMessage(), cached.size());
            return cached;
        }
        state.put(NAMES_KEY, EnvSources.pack(UsgsLatestContinuousParser.sitesToJson(fresh)));
        HydroSources.stamp(state, NAMES_AT_KEY, ctx.now());
        return fresh;
    }

    /** Keeps each page's {@code X-RateLimit-Remaining} in state and the log. */
    private static Consumer<UpstreamResponse> rateLimit(FetchContext ctx) {
        return response -> {
            String remaining = response.header("x-ratelimit-remaining");
            if (remaining == null || remaining.isBlank()) {
                return;
            }
            ctx.state().put(RATE_LIMIT_KEY, remaining.strip());
            try {
                int left = Integer.parseInt(remaining.strip());
                if (left < LOW_RATE_LIMIT) {
                    ctx.log().warn("{}: X-RateLimit-Remaining is {}; add or check the apiKey credential", ID, left);
                } else {
                    ctx.log().debug("{}: X-RateLimit-Remaining {}", ID, left);
                }
            } catch (NumberFormatException e) {
                ctx.log().debug("{}: unreadable X-RateLimit-Remaining '{}'", ID, remaining);
            }
        };
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = UsgsLatestContinuousParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("USGS water gauges")
                .description("The latest real-time readings of USGS stream, lake and groundwater gauges in a box (at"
                        + " most 5° × 5°) or a site list, through the USGS Water Data OGC API (latest-continuous):"
                        + " discharge and gage height by default. One record per site and parameter; site names are"
                        + " refreshed daily. The legacy waterservices.usgs.gov service is not used.")
                .kind(kind)
                .homepage("https://api.waterdata.usgs.gov/")
                .attribution("U.S. Geological Survey, Water Data for the Nation")
                .licence("US-PD")
                .termsUrl("https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits")
                .mode(Mode.POLLING)
                .credential(CredentialField.optional(API_KEY, "An api.data.gov key for the USGS Water Data APIs"
                        + " (api.waterdata.usgs.gov/docs/ogcapi/keys); raises the hourly limit; sent as X-Api-Key"))
                .param(ParamSpec.optional(BBOX_PARAM, ParamType.BBOX, "The box to read, west,south,east,north, at most"
                        + " 5° × 5°; required unless monitoringLocations is set", null))
                .param(ParamSpec.optional(LOCATIONS_PARAM, ParamType.STRING_LIST, "Monitoring location ids to read"
                        + " instead of a box (USGS-06710385), at most 100", null))
                .param(ParamSpec.optional(PARAMETER_CODES_PARAM, ParamType.STRING_LIST, "USGS parameter codes:"
                        + " 00060 discharge, 00065 gage height, 00010 water temperature, 72019 depth to water",
                        DEFAULT_PARAMETER_CODES))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(30, 1500))
                .fetchTimeout(Duration.ofSeconds(90))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(HydroSources.STATE_RETENTION)
                .kindRetention(kind, HydroSources.STATE_RETENTION)
                .tileProperties("siteName", "parameterName", "value", "unit")
                .searchFields("siteName", "parameterName")
                .titleField("siteName")
                .rankField("value");
        HydroSources.text(b, "siteName", "Site", "Monitoring location name (monitoring-locations, refreshed daily)");
        HydroSources.text(b, "siteType", "Site type", "USGS site type code: ST stream, LK lake, GW well, ES estuary");
        HydroSources.text(b, "parameterCode", "Parameter code", "USGS parameter code (00060, 00065, 00010, 72019)");
        HydroSources.text(b, "parameterName", "Parameter", "Discharge, Gage height, Water temperature or Depth to"
                + " water; absent for other codes");
        HydroSources.number(b, "value", null, "Value", "The reading, in the unit given by unit (it varies by"
                + " parameter)");
        HydroSources.text(b, "unit", "Unit", "The reading's unit: ft3/s, ft or degC");
        HydroSources.text(b, "approval", "Approval", "Provisional or Approved");
        HydroSources.text(b, "qualifier", "Qualifier", "USGS qualifier codes, comma-separated (SEASONAL, Ice, Eqp);"
                + " a non-numeric reading is recorded here instead of value");
        b.kindDisplay(kind, new KindDisplay("{siteName}", "water", "#1971C2", null));
        return b.build();
    }
}
