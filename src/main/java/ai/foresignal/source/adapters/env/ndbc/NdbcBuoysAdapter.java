package ai.foresignal.source.adapters.env.ndbc;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
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
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ndbc-buoys} (prompt 078): NOAA NDBC's latest observation of every buoy and coastal station, as {@code
 * buoy-observation} point records ({@link NdbcLatestObsParser}).
 *
 * <ul>
 *   <li><strong>Each run:</strong> a conditional GET of {@value #LATEST_OBS_URL} (state key {@value
 *       #OBS_KEY}); a 304 is {@link FetchResult#unchanged()}.
 *   <li><strong>Names:</strong> {@value #STATIONS_URL} at most once per 24 h, a second request, cached in state
 *       ({@value #STATIONS_KEY}, gzipped when large; {@value #STATIONS_AT_KEY} holds the read time). A failed refresh
 *       keeps the cached table.
 * </ul>
 */
@Component
public class NdbcBuoysAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ndbc-buoys";

    /** The latest observations file. */
    public static final String LATEST_OBS_URL = "https://www.ndbc.noaa.gov/data/latest_obs/latest_obs.txt";

    /** The active station list. */
    public static final String STATIONS_URL = "https://www.ndbc.noaa.gov/activestations.xml";

    /** The conditional GET state key of the observations file. */
    public static final String OBS_KEY = "latest-obs";

    /** The state key of the cached station table. */
    public static final String STATIONS_KEY = "ndbc:stations";

    /** The state key of the time the station table was read, epoch seconds. */
    public static final String STATIONS_AT_KEY = "ndbc:stationsAt";

    /** How long the station table is used before it is read again. */
    public static final Duration STATIONS_REFRESH = Duration.ofHours(24);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(3);

    /** Raw retention: station readings are the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    private final URI latestObs;
    private final URI stationsList;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value EnvSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public NdbcBuoysAdapter(@Value("${" + EnvSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public NdbcBuoysAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.latestObs = EnvSources.uri(LATEST_OBS_URL, baseUrlOverride);
        this.stationsList = EnvSources.uri(STATIONS_URL, baseUrlOverride);
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * Refreshes the station table when due, then reads the observations conditionally.
     *
     * @throws AdapterException when the observations request fails
     * @throws UpstreamFormatException when the observations file does not parse (its validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Map<String, NdbcStationsParser.Station> stations = stations(ctx);
        UpstreamResponse response = ctx.http().getConditional(latestObs, OBS_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        byte[] body = EnvSources.body(ctx, response, OBS_KEY, latestObs);
        EnvSources.Parsed parsed;
        try {
            parsed = NdbcLatestObsParser.parse(body, stations, ctx.now(),
                    EnvSources.maxAge(ctx.params(), NdbcLatestObsParser.DEFAULT_MAX_AGE));
        } catch (UpstreamFormatException e) {
            ai.foresignal.source.spi.SourceHttp.forgetValidators(ctx.state(), OBS_KEY);
            throw e;
        }
        SkipCounter.count(meters, EnvSources.SKIPPED_METRIC, EnvSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        ctx.log().debug("{}: {} station(s), skipped {}", ID, parsed.records().size(), parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    /** The station table: the cache, refreshed when it is older than {@link #STATIONS_REFRESH}. */
    Map<String, NdbcStationsParser.Station> stations(FetchContext ctx) {
        AdapterStateStore state = ctx.state();
        Map<String, NdbcStationsParser.Station> cached =
                NdbcStationsParser.fromJson(EnvSources.unpack(state.get(STATIONS_KEY).orElse(null)));
        if (!cached.isEmpty() && !due(state.get(STATIONS_AT_KEY), ctx.now())) {
            return cached;
        }
        try {
            Map<String, NdbcStationsParser.Station> fresh = NdbcStationsParser.parse(
                    EnvSources.body(ctx, ctx.http().get(stationsList), null, stationsList));
            state.put(STATIONS_KEY, EnvSources.pack(NdbcStationsParser.toJson(fresh)));
            state.put(STATIONS_AT_KEY, Long.toString(ctx.now().getEpochSecond()));
            return fresh;
        } catch (AdapterException | UpstreamFormatException e) {
            ctx.log().warn("{}: activestations.xml could not be read ({}); {} cached station(s) kept", ID,
                    e.getMessage(), cached.size());
            return cached;
        }
    }

    private static boolean due(Optional<String> at, Instant now) {
        try {
            return at.isEmpty() || !Instant.ofEpochSecond(Long.parseLong(at.get().strip())).plus(STATIONS_REFRESH)
                    .isAfter(now);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = NdbcLatestObsParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NDBC buoys")
                .description("The latest observation of every NOAA National Data Buoy Center buoy and coastal station:"
                        + " wind, waves, pressure, air and water temperature, dew point, visibility and tide, with"
                        + " station names and types from the active station list. Observations older than maxAge (6 h)"
                        + " are skipped.")
                .kind(kind)
                .homepage("https://www.ndbc.noaa.gov/")
                .attribution("NOAA National Data Buoy Center")
                .licence("US-PD")
                .termsUrl("https://www.ndbc.noaa.gov/faq/measdes.shtml")
                .mode(Mode.POLLING)
                .param(EnvSources.maxAgeParam("6h"))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(10))
                .defaultBudget(Budget.of(2, 300))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .tileProperties("windSpeedMs", "windDirDeg", "waveHeightM", "waterTempC")
                .searchFields("name", "owner")
                .titleField("name")
                .rankField("waveHeightM");
        text(b, "name", "Name", "Station name (activestations.xml)");
        text(b, "stationType", "Station type", "buoy, fixed, dart, oilrig, tao, usv or other (activestations.xml)");
        text(b, "owner", "Owner", "Station owner (activestations.xml)");
        number(b, "windDirDeg", "deg", "Wind direction", "Direction the wind blows from, degrees from true north");
        number(b, "windSpeedMs", "m/s", "Wind speed", "Average wind speed");
        number(b, "gustMs", "m/s", "Gust", "Peak gust speed");
        number(b, "waveHeightM", "m", "Wave height", "Significant wave height");
        number(b, "dominantPeriodS", "s", "Dominant period", "Dominant wave period");
        number(b, "averagePeriodS", "s", "Average period", "Average wave period");
        number(b, "meanWaveDirDeg", "deg", "Wave direction", "Mean direction waves come from at the dominant period");
        number(b, "pressureHpa", "hPa", "Pressure", "Sea-level pressure");
        number(b, "pressureTendencyHpa", "hPa", "Pressure tendency", "Pressure change over the last 3 hours");
        number(b, "airTempC", "Cel", "Air temperature", "Air temperature");
        number(b, "waterTempC", "Cel", "Water temperature", "Sea surface temperature");
        number(b, "dewPointC", "Cel", "Dew point", "Dew point");
        number(b, "visibilityNmi", "[nmi_i]", "Visibility", "Station visibility in nautical miles");
        number(b, "tideFt", "[ft_i]", "Tide", "Water level in feet above or below mean lower low water");
        b.kindDisplay(kind, new KindDisplay("{name}", "buoy", "#1864AB", null));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }

    private static void number(SourceDescriptor.Builder b, String key, String unit, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.NUMBER, unit, label, description, null, Set.of()));
    }
}
