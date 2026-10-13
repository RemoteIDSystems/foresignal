package ai.foresignal.source.adapters.hydro.snotel;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code snotel} (prompt 079): NRCS SNOTEL snowpack stations through the AWDB REST API ({@link AwdbParser}).
 *
 * <ul>
 *   <li><strong>Metadata:</strong> {@code /v1/stations?stationTriplets=*:{state}:SNTL,…} (the triplet wildcard
 *       returns SNOTEL sites only; the {@code networkCds}/{@code stateCds} form returns every network) or the listed
 *       triplets, at most once per 24 h or when the parameters change, cached in state ({@value #STATIONS_KEY});
 *       at most {@value #MAX_STATIONS} stations, by triplet;
 *   <li><strong>Each run:</strong> {@code /v1/data?stationTriplets={≤ 50}&elements=WTEQ,SNWD,PREC,TOBS
 *       &duration=DAILY&beginDate={today − 2}&endDate={today}} per batch (UTC dates). A failing batch is logged and
 *       skipped; the run fails only when every batch failed.
 * </ul>
 */
@Component
public class SnotelAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "snotel";

    /** The AWDB REST root. */
    public static final String API_URL = "https://wcc.sc.egov.usda.gov/awdbRestApi/services/v1";

    /** The state list parameter. */
    public static final String STATES_PARAM = "states";

    /** The triplet list parameter. */
    public static final String STATIONS_PARAM = "stations";

    /** The most stations read. */
    public static final int MAX_STATIONS = 200;

    /** Triplets per data request. */
    public static final int BATCH = 50;

    /** The state key of the station table. */
    public static final String STATIONS_KEY = "snotel:stations";

    /** The state key of the station table's read time, epoch seconds. */
    public static final String STATIONS_AT_KEY = "snotel:stationsAt";

    /** The state key of the query the station table answers. */
    public static final String QUERY_KEY = "snotel:query";

    /** How long the station table is used before it is read again. */
    public static final Duration STATIONS_REFRESH = Duration.ofHours(24);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(48);

    private static final Pattern STATE = Pattern.compile("^[A-Za-z]{2}$");
    private static final Pattern TRIPLET = Pattern.compile("^[0-9A-Za-z]+:[A-Za-z]{2}:SNTL$");

    private final String api;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public SnotelAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, HydroSources.meters(meters));
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public SnotelAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.api = HydroSources.uri(API_URL, baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Requires {@code states} (two-letter codes) or {@code stations} (at most 200 SNOTEL triplets). */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> states = params.stringList(STATES_PARAM);
        List<String> stations = params.stringList(STATIONS_PARAM);
        if (states.isEmpty() && stations.isEmpty()) {
            problems.put(STATES_PARAM, "list states (CO, UT) or stations (1344:CO:SNTL)");
            return problems;
        }
        List<String> badStates = states.stream().filter(s -> !STATE.matcher(s).matches()).toList();
        if (!badStates.isEmpty()) {
            problems.put(STATES_PARAM, "not two-letter state codes: " + badStates);
        }
        if (stations.size() > MAX_STATIONS) {
            problems.put(STATIONS_PARAM, "at most " + MAX_STATIONS + " stations, but " + stations.size()
                    + " were given");
        } else {
            List<String> bad = stations.stream().filter(s -> !TRIPLET.matcher(s).matches()).toList();
            if (!bad.isEmpty()) {
                problems.put(STATIONS_PARAM, "not SNOTEL station triplets (id:ST:SNTL): " + bad);
            }
        }
        return problems;
    }

    /** The {@code stationTriplets} value of the metadata request for {@code params}. */
    static String query(ValidatedParams params) {
        List<String> parts = new ArrayList<>();
        for (String state : params.stringList(STATES_PARAM)) {
            parts.add("*:" + state.toUpperCase(Locale.ROOT) + ":SNTL");
        }
        for (String triplet : params.stringList(STATIONS_PARAM)) {
            parts.add(triplet.toUpperCase(Locale.ROOT));
        }
        return String.join(",", parts);
    }

    /** The metadata URL for a {@code stationTriplets} value. */
    public URI stationsUri(String triplets) {
        return URI.create(api + "/stations?stationTriplets=" + KitUrls.encode(triplets)
                + "&returnForecastPointMetadata=false");
    }

    /** The data URL of one batch for the three days ending {@code today}. */
    public URI dataUri(List<String> triplets, LocalDate today) {
        return URI.create(api + "/data?stationTriplets=" + KitUrls.encode(String.join(",", triplets)) + "&elements="
                + KitUrls.encode(AwdbParser.ELEMENTS) + "&duration=DAILY&beginDate=" + today.minusDays(2)
                + "&endDate=" + today);
    }

    /**
     * Refreshes the station table when due, then reads the daily data in batches.
     *
     * @throws AdapterException when the parameters are invalid, no station table can be had, or every batch failed
     * @throws UpstreamFormatException when the station answer does not parse and nothing is cached
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        Map<String, AwdbParser.Station> stations = stations(ctx, query(params));
        if (stations.isEmpty()) {
            return FetchResult.complete(List.of());
        }
        List<String> triplets = new ArrayList<>(stations.keySet());
        boolean capped = triplets.size() > MAX_STATIONS;
        if (capped) {
            ctx.log().warn("{}: {} SNOTEL stations match; only the first {} by triplet are read", ID, triplets.size(),
                    MAX_STATIONS);
            triplets = triplets.subList(0, MAX_STATIONS);
        }
        LocalDate today = LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC);
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        int failed = 0;
        int batches = 0;
        AdapterException last = null;
        for (int from = 0; from < triplets.size(); from += BATCH) {
            batches++;
            List<String> batch = triplets.subList(from, Math.min(triplets.size(), from + BATCH));
            URI uri = dataUri(batch, today);
            try {
                EnvSources.Parsed parsed = AwdbParser.data(HydroSources.json(
                        EnvSources.body(ctx, ctx.http().get(uri), null, uri), "AWDB data"), stations);
                records.addAll(parsed.records());
                parsed.skipped().forEach((reason, n) -> skipped.merge(reason, n, Integer::sum));
            } catch (AdapterException e) {
                failed++;
                last = e;
                ctx.log().warn("{}: data batch {} failed ({})", ID, batches, e.getMessage());
            } catch (UpstreamFormatException e) {
                failed++;
                last = new AdapterException(e.getMessage(), e);
                ctx.log().warn("{}: data batch {} did not parse ({})", ID, batches, e.getMessage());
            }
        }
        if (failed == batches && last != null) {
            throw last;
        }
        SkipCounter.count(meters, HydroSources.SKIPPED_METRIC, HydroSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                skipped);
        ctx.log().debug("{}: {} station(s), skipped {}", ID, records.size(), skipped);
        return capped || failed > 0 ? FetchResult.of(records) : FetchResult.complete(records);
    }

    /** The station table for {@code query}: the cache, refreshed daily or when the query changed. */
    Map<String, AwdbParser.Station> stations(FetchContext ctx, String query) throws AdapterException {
        AdapterStateStore state = ctx.state();
        Map<String, AwdbParser.Station> cached =
                AwdbParser.stationsFromJson(EnvSources.unpack(state.get(STATIONS_KEY).orElse(null)));
        boolean sameQuery = query.equals(state.get(QUERY_KEY).orElse(null));
        if (sameQuery && !cached.isEmpty() && !HydroSources.due(state, STATIONS_AT_KEY, ctx.now(), STATIONS_REFRESH)) {
            return cached;
        }
        URI uri = stationsUri(query);
        try {
            Map<String, AwdbParser.Station> fresh = AwdbParser.stations(HydroSources.json(
                    EnvSources.body(ctx, ctx.http().get(uri), null, uri), "AWDB stations"));
            state.put(STATIONS_KEY, EnvSources.pack(AwdbParser.stationsToJson(fresh)));
            state.put(QUERY_KEY, query);
            HydroSources.stamp(state, STATIONS_AT_KEY, ctx.now());
            return fresh;
        } catch (AdapterException | UpstreamFormatException e) {
            if (!sameQuery || cached.isEmpty()) {
                throw e;
            }
            ctx.log().warn("{}: the station list could not be read ({}); {} cached station(s) kept", ID,
                    e.getMessage(), cached.size());
            return cached;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = AwdbParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("SNOTEL snowpack")
                .description("NRCS SNOTEL stations in the listed states or by triplet (at most 200), from the AWDB"
                        + " REST API: the latest daily snow water equivalent, snow depth, accumulated precipitation"
                        + " and observed air temperature, converted to mm, cm and °C. The observation time is the"
                        + " value date at midnight in the station's data time zone.")
                .kind(kind)
                .homepage("https://www.nrcs.usda.gov/wps/portal/wcc/home/")
                .attribution("USDA Natural Resources Conservation Service, National Water and Climate Center")
                .licence("US-PD")
                .termsUrl("https://wcc.sc.egov.usda.gov/awdbRestApi/swagger-ui/index.html")
                .mode(Mode.POLLING)
                .param(ParamSpec.optional(STATES_PARAM, ParamType.STRING_LIST, "Two-letter states whose SNOTEL"
                        + " stations are read (CO, UT); required unless stations is set", null))
                .param(ParamSpec.optional(STATIONS_PARAM, ParamType.STRING_LIST, "SNOTEL station triplets"
                        + " (1344:CO:SNTL), at most 200; required unless states is set", null))
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(6, 50))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(HydroSources.STATE_RETENTION)
                .kindRetention(kind, HydroSources.STATE_RETENTION)
                .tileProperties("name", "sweMm", "snowDepthCm")
                .searchFields("name")
                .titleField("name")
                .rankField("sweMm");
        HydroSources.text(b, "name", "Name", "Station name");
        HydroSources.height(b, "elevationM", "m", "MSL", "Elevation", "Station elevation (from feet)");
        HydroSources.number(b, "sweMm", "mm", "Snow water equivalent", "WTEQ, from inches");
        HydroSources.number(b, "snowDepthCm", "cm", "Snow depth", "SNWD, from inches");
        HydroSources.number(b, "precipAccumMm", "mm", "Precipitation accumulation", "PREC, water-year to date, from"
                + " inches");
        HydroSources.number(b, "airTempC", "Cel", "Air temperature", "TOBS, observed at the daily reading, from °F");
        b.kindDisplay(kind, new KindDisplay("{name}", "snow", "#4DABF7", null));
        return b.build();
    }
}
