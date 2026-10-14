package ai.foresignal.source.adapters.awc;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AreaOfInterest;
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
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Terminal Aerodrome Forecasts from the Aviation Weather Center ({@code awc-tafs}, prompt 087), on the station scope
 * and allowlist that {@code awc-metars} uses ({@link AwcSupport}). There is one METAR source ({@code awc-metars});
 * this adapter reads TAFs only.
 *
 * <ul>
 *   <li><strong>{@code stations} set:</strong> {@code /api/data/taf?ids=…&format=json}, at most
 *       {@value #MAX_IDS_PER_REQUEST} stations per request (AWC returns at most 400 entries), the chunks one after
 *       another;
 *   <li><strong>otherwise:</strong> one conditional GET of the cache file {@value #CACHE_PATH} (every current TAF,
 *       refreshed every 10 minutes), filtered by {@code scope} ({@code aoi} default, {@code conus}, {@code global}).
 *       A 304 is unchanged; a truncated or unreadable file forgets the validators and fails the run.
 * </ul>
 *
 * <p>One {@code taf} record per station ({@link TafParser}); a TAF ends with its asserted validity, nothing is
 * tombstoned. 10-minute poll, never under 2 minutes, budget 2/min and 300/day. Retention 7 days, as METARs
 * (Weather-Imagery-Design §2.4 names no TAF window).
 */
@Component
public class AwcTafsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "awc-tafs";

    /** The record kind. */
    public static final String KIND = TafParser.KIND;

    /** The API path. */
    public static final String API_PATH = "/api/data/taf";

    /** The cache file path. */
    public static final String CACHE_PATH = "/data/cache/tafs.cache.xml.gz";

    /** The {@code scope} parameter. */
    public static final String SCOPE_PARAM = "scope";

    /** The {@code stations} parameter. */
    public static final String STATIONS_PARAM = "stations";

    /** Stations per API request. */
    public static final int MAX_IDS_PER_REQUEST = 400;

    /** The adapter-state key of the cache file's validators. */
    static final String STATE_KEY = "tafs";

    /** Retention, as {@code metar}. */
    static final Duration RETENTION = Duration.ofDays(7);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final AreaOfInterest aoi;
    private final String baseUrl;

    /**
     * Creates the adapter for Spring.
     *
     * @param aoi the places AOI
     * @param baseUrlOverride the value of {@value AwcSupport#BASE_URL_PROPERTY}; blank means the real site
     */
    @Autowired
    public AwcTafsAdapter(ObjectProvider<AreaOfInterest> aoi,
            @Value("${" + AwcSupport.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(aoi == null ? null : aoi.getIfAvailable(), baseUrlOverride);
    }

    /**
     * Creates the adapter.
     *
     * @param aoi the places AOI, or null for the default Texas box
     * @param baseUrlOverride a base URL, or blank for the real site
     */
    public AwcTafsAdapter(AreaOfInterest aoi, String baseUrlOverride) {
        this.aoi = aoi;
        this.baseUrl = AwcSupport.baseUrl(baseUrlOverride);
        if (aoi != null) {
            aoi.registerConsumer(ID);
        }
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The cache file URL. */
    URI cacheUri() {
        return URI.create(baseUrl + CACHE_PATH);
    }

    /** The API requests for an allowlist, one per chunk of at most {@value #MAX_IDS_PER_REQUEST} stations. */
    List<URI> apiRequests(Set<String> stations) {
        List<URI> out = new ArrayList<>();
        for (List<String> chunk : AwcSupport.chunks(stations, MAX_IDS_PER_REQUEST)) {
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("ids", String.join(",", chunk));
            query.put("format", "json");
            out.add(AwcSupport.uri(baseUrl, API_PATH, query));
        }
        return out;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Set<String> stations = AwcSupport.stations(params.stringList(STATIONS_PARAM));
        if (!stations.isEmpty()) {
            TafParser.Filter filter = new TafParser.Filter(null, stations);
            List<NormalizedRecord> records = new ArrayList<>();
            for (URI uri : apiRequests(stations)) {
                UpstreamResponse response = ctx.http().get(uri);
                records.addAll(TafParser.json(AwcSupport.body(response, "AWC taf"), filter).records());
            }
            return FetchResult.complete(records);
        }
        TafParser.Filter filter = new TafParser.Filter(
                AwcSupport.envelope(params.optionalString(SCOPE_PARAM).orElse("aoi"), aoi), Set.of());
        byte[] body = AwcSupport.conditionalBody(ctx, cacheUri(), STATE_KEY, "AWC TAF cache file");
        if (body == null) {
            return FetchResult.unchanged();
        }
        TafParser.Parsed parsed;
        try {
            parsed = TafParser.xml(body, filter);
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        ctx.log().debug("{}: {} TAF(s), {} kept, {} skipped, {} outside the scope", ctx.sourceId(), parsed.tafs(),
                parsed.records().size(), parsed.skipped(), parsed.filtered());
        return FetchResult.complete(parsed.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = AwcSupport.builder(ID, "AWC TAFs",
                "Terminal Aerodrome Forecasts from the NOAA Aviation Weather Center, one record per station: the TAF"
                        + " cache file filtered by scope, or the TAF API for a station allowlist (at most 400 stations"
                        + " per request). AWC limits every client to 100 requests per minute; the default 10-minute"
                        + " poll uses one request.", KIND)
                .mode(Mode.POLLING)
                .param(ParamSpec.enumerated(SCOPE_PARAM,
                        "aoi (the places-AOI envelope), conus or global; ignored when stations is set", "aoi",
                        AwcSupport.SCOPES))
                .param(ParamSpec.optional(STATIONS_PARAM, ParamType.STRING_LIST,
                        "ICAO station allowlist, for example KAUS, KDFW; overrides scope and uses the TAF API", null))
                .interval(AwcSupport.MIN_INTERVAL, Duration.ofMinutes(10))
                .defaultBudget(Budget.of(2, 300))
                .maxResponseBytes(8 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .titleField("stationId")
                .searchFields("stationId", "raw", "name")
                .tileProperties("stationId")
                .kindDisplay(KIND, new KindDisplay("TAF {stationId}", "forecast", "#1971C2", null));
        AwcSupport.text(b, "stationId", "Station", "ICAO station identifier");
        AwcSupport.text(b, "raw", "Forecast", "The raw TAF (upstream text, untrusted)");
        AwcSupport.number(b, "elevationM", "m", "Elevation", "Station elevation");
        AwcSupport.text(b, "name", "Name", "Station name (TAF API only)");
        return b.build();
    }
}
