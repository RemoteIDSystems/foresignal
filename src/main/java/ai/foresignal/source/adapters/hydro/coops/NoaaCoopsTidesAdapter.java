package ai.foresignal.source.adapters.hydro.coops;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
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
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code noaa-coops-tides} (prompt 079): NOAA CO-OPS tide gauges with the latest water level, the next high and low
 * water and a flood category from the station's flood thresholds ({@link CoopsMapper}). Every data request carries
 * {@code application=Foresignal}, as CO-OPS asks.
 *
 * <ul>
 *   <li><strong>Validation:</strong> {@code stations} (at most {@value #MAX_STATIONS}) are checked against
 *       {@value #STATIONS_URL} (777 KB, cached 7 d as the id list; Great Lakes stations are left out, they have no
 *       MLLW). An unknown station is skipped and keeps the source {@code degraded}; without a list nothing is
 *       refused;
 *   <li><strong>Each run, per station:</strong> {@code water_level&date=latest} in meters above MLLW, GMT;
 *   <li><strong>Every 6 h per station:</strong> {@code predictions&interval=hilo} for today and tomorrow (UTC);
 *   <li><strong>Daily per station:</strong> {@code floodlevels.json} and {@code datums.json}.
 * </ul>
 *
 * <p>At most {@value #REFRESHES_PER_RUN} prediction and {@value #REFRESHES_PER_RUN} threshold refreshes run per
 * run, so a new station list spreads its metadata requests over the first runs. A failing station is logged and
 * skipped; the run fails only when every station failed.
 */
@Component
public class NoaaCoopsTidesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "noaa-coops-tides";

    /** The data API. */
    public static final String DATA_URL = "https://api.tidesandcurrents.noaa.gov/api/prod/datagetter";

    /** The metadata API root. */
    public static final String MD_URL = "https://api.tidesandcurrents.noaa.gov/mdapi/prod/webapi";

    /** The water-level station list. */
    public static final String STATIONS_URL = MD_URL + "/stations.json?type=waterlevels";

    /** The application name every data request carries. */
    public static final String APPLICATION = "Foresignal";

    /** The station list parameter. */
    public static final String STATIONS_PARAM = "stations";

    /** The most stations. */
    public static final int MAX_STATIONS = 20;

    /** Prediction (and, separately, threshold) refreshes per run. */
    public static final int REFRESHES_PER_RUN = 4;

    /** The state key of the valid station ids, comma-separated. */
    public static final String STATIONS_KEY = "coops:stations";

    /** The state key of the station list's read time, epoch seconds. */
    public static final String STATIONS_AT_KEY = "coops:stationsAt";

    /** The state key prefix of a station's predictions. */
    public static final String PRED_PREFIX = "coops:pred:";

    /** The state key prefix of a station's predictions' read time. */
    public static final String PRED_AT_PREFIX = "coops:predAt:";

    /** The state key prefix of a station's thresholds. */
    public static final String FLOOD_PREFIX = "coops:flood:";

    /** The state key prefix of a station's thresholds' read time. */
    public static final String FLOOD_AT_PREFIX = "coops:floodAt:";

    /** How long the station list is used. */
    public static final Duration STATIONS_REFRESH = Duration.ofDays(7);

    /** How long predictions are used. */
    public static final Duration PRED_REFRESH = Duration.ofHours(6);

    /** How long thresholds are used. */
    public static final Duration FLOOD_REFRESH = Duration.ofHours(24);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(1);

    private static final Pattern STATION = Pattern.compile("^\\d{7}$");
    private static final DateTimeFormatter COMPACT = DateTimeFormatter.BASIC_ISO_DATE;

    private final String data;
    private final String md;
    private final URI stationsList;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     */
    @Autowired
    public NoaaCoopsTidesAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.data = HydroSources.uri(DATA_URL, baseUrlOverride).toString();
        this.md = HydroSources.uri(MD_URL, baseUrlOverride).toString();
        this.stationsList = HydroSources.uri(STATIONS_URL, baseUrlOverride);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Requires 1–20 seven-digit station ids. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> stations = params.stringList(STATIONS_PARAM);
        if (stations.isEmpty()) {
            problems.put(STATIONS_PARAM, "list 1–" + MAX_STATIONS + " CO-OPS water-level station ids (8518750)");
        } else if (stations.size() > MAX_STATIONS) {
            problems.put(STATIONS_PARAM, "at most " + MAX_STATIONS + " stations, but " + stations.size()
                    + " were given");
        } else {
            List<String> bad = stations.stream().filter(s -> !STATION.matcher(s).matches()).toList();
            if (!bad.isEmpty()) {
                problems.put(STATIONS_PARAM, "not seven-digit CO-OPS station ids: " + bad);
            }
        }
        return problems;
    }

    /** The latest water level URL of {@code station}. */
    public URI waterLevelUri(String station) {
        return URI.create(data + "?product=water_level&station=" + station + "&date=latest&datum=MLLW&units=metric"
                + "&time_zone=gmt&application=" + APPLICATION + "&format=json");
    }

    /** The high/low prediction URL of {@code station} for {@code today} and the day after. */
    public URI predictionsUri(String station, LocalDate today) {
        return URI.create(data + "?product=predictions&interval=hilo&begin_date=" + today.format(COMPACT)
                + "&end_date=" + today.plusDays(1).format(COMPACT) + "&station=" + station + "&datum=MLLW"
                + "&units=metric&time_zone=gmt&application=" + APPLICATION + "&format=json");
    }

    /** The flood-level URL of {@code station}. */
    public URI floodLevelsUri(String station) {
        return URI.create(md + "/stations/" + station + "/floodlevels.json");
    }

    /** The datums URL of {@code station}. */
    public URI datumsUri(String station) {
        return URI.create(md + "/stations/" + station + "/datums.json");
    }

    /**
     * Reads every station's latest water level, refreshing predictions and thresholds when due.
     *
     * @throws AdapterException when the parameters are invalid, or every station failed
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        Set<String> valid = validStations(ctx);
        List<String> unknown = new ArrayList<>();
        List<NormalizedRecord> records = new ArrayList<>();
        int[] refreshes = {0, 0};
        int failed = 0;
        int tried = 0;
        AdapterException last = null;
        for (String station : new LinkedHashSet<>(params.stringList(STATIONS_PARAM))) {
            if (!valid.isEmpty() && !valid.contains(station)) {
                unknown.add(station);
                continue;
            }
            tried++;
            try {
                List<CoopsMapper.Tide> tides = tides(ctx, station, refreshes);
                CoopsMapper.FloodLevels levels = levels(ctx, station, refreshes);
                URI uri = waterLevelUri(station);
                CoopsMapper.WaterLevel level = CoopsMapper.waterLevel(
                        EnvSources.body(ctx, ctx.http().get(uri), null, uri));
                NormalizedRecord record = CoopsMapper.record(station, level, tides, levels, ctx.now());
                if (record == null) {
                    ctx.log().warn("{}: station {} answered without a time or location", ID, station);
                } else {
                    records.add(record);
                }
            } catch (AdapterException e) {
                failed++;
                last = e;
                ctx.log().warn("{}: station {} failed ({})", ID, station, e.getMessage());
            } catch (UpstreamFormatException e) {
                failed++;
                last = new AdapterException(ID + ": station " + station + ": " + e.getMessage(), e);
                ctx.log().warn("{}: station {} did not parse ({})", ID, station, e.getMessage());
            }
        }
        if (unknown.isEmpty()) {
            ctx.state().degrade(null);
        } else {
            ctx.log().warn("{}: not CO-OPS water-level stations (or Great Lakes stations without MLLW): {}", ID,
                    unknown);
            ctx.state().degrade("Unknown CO-OPS water-level stations: " + String.join(", ", unknown));
        }
        if (tried > 0 && failed == tried && last != null) {
            throw last;
        }
        return failed > 0 || !unknown.isEmpty() ? FetchResult.of(records) : FetchResult.complete(records);
    }

    /** The valid station ids: the cache, refreshed weekly; empty when no list could ever be read. */
    Set<String> validStations(FetchContext ctx) {
        AdapterStateStore state = ctx.state();
        Set<String> cached = new LinkedHashSet<>();
        for (String id : EnvSources.unpack(state.get(STATIONS_KEY).orElse("")).split(",")) {
            if (!id.isBlank()) {
                cached.add(id.strip());
            }
        }
        if (!cached.isEmpty() && !HydroSources.due(state, STATIONS_AT_KEY, ctx.now(), STATIONS_REFRESH)) {
            return cached;
        }
        try {
            Set<String> fresh = CoopsMapper.stationIds(HydroSources.json(
                    EnvSources.body(ctx, ctx.http().get(stationsList), null, stationsList), "CO-OPS station list"));
            state.put(STATIONS_KEY, EnvSources.pack(String.join(",", fresh)));
            HydroSources.stamp(state, STATIONS_AT_KEY, ctx.now());
            return fresh;
        } catch (AdapterException | UpstreamFormatException e) {
            ctx.log().warn("{}: the station list could not be read ({}); {} cached id(s) kept", ID, e.getMessage(),
                    cached.size());
            return cached;
        }
    }

    /** {@code station}'s predictions: the cache, refreshed every 6 h while this run has refreshes left. */
    List<CoopsMapper.Tide> tides(FetchContext ctx, String station, int[] refreshes) {
        AdapterStateStore state = ctx.state();
        List<CoopsMapper.Tide> cached = CoopsMapper.tidesFromJson(state.get(PRED_PREFIX + station).orElse(null));
        if (!HydroSources.due(state, PRED_AT_PREFIX + station, ctx.now(), PRED_REFRESH)
                || refreshes[0] >= REFRESHES_PER_RUN) {
            return cached;
        }
        refreshes[0]++;
        URI uri = predictionsUri(station, LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC));
        try {
            List<CoopsMapper.Tide> fresh = CoopsMapper.predictions(EnvSources.body(ctx, ctx.http().get(uri), null,
                    uri));
            state.put(PRED_PREFIX + station, CoopsMapper.tidesToJson(fresh));
            HydroSources.stamp(state, PRED_AT_PREFIX + station, ctx.now());
            return fresh;
        } catch (AdapterException | UpstreamFormatException e) {
            ctx.log().warn("{}: predictions of {} could not be read ({}); cached ones kept", ID, station,
                    e.getMessage());
            return cached;
        }
    }

    /** {@code station}'s thresholds: the cache, refreshed daily while this run has refreshes left. */
    CoopsMapper.FloodLevels levels(FetchContext ctx, String station, int[] refreshes) {
        AdapterStateStore state = ctx.state();
        CoopsMapper.FloodLevels cached = CoopsMapper.levelsFromJson(state.get(FLOOD_PREFIX + station).orElse(null));
        if (!HydroSources.due(state, FLOOD_AT_PREFIX + station, ctx.now(), FLOOD_REFRESH)
                || refreshes[1] >= REFRESHES_PER_RUN) {
            return cached;
        }
        refreshes[1]++;
        try {
            URI floodUri = floodLevelsUri(station);
            URI datumUri = datumsUri(station);
            CoopsMapper.FloodLevels fresh = CoopsMapper.floodLevels(
                    HydroSources.json(EnvSources.body(ctx, ctx.http().get(floodUri), null, floodUri),
                            "CO-OPS floodlevels"),
                    HydroSources.json(EnvSources.body(ctx, ctx.http().get(datumUri), null, datumUri),
                            "CO-OPS datums"));
            state.put(FLOOD_PREFIX + station, CoopsMapper.levelsToJson(fresh));
            HydroSources.stamp(state, FLOOD_AT_PREFIX + station, ctx.now());
            return fresh;
        } catch (AdapterException | UpstreamFormatException e) {
            ctx.log().warn("{}: flood levels of {} could not be read ({}); cached ones kept", ID, station,
                    e.getMessage());
            return cached;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = CoopsMapper.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NOAA tide gauges")
                .description("NOAA CO-OPS water-level stations (1–20 listed ids): the latest 6-minute water level in"
                        + " meters above MLLW, the next predicted high and low water, and a flood category (none,"
                        + " action, minor, moderate, major) from the NOS flood thresholds (NWS where NOS has none),"
                        + " converted from the station datum to MLLW meters.")
                .kind(kind)
                .homepage("https://tidesandcurrents.noaa.gov/")
                .attribution("NOAA Center for Operational Oceanographic Products and Services (CO-OPS)")
                .licence("US-PD")
                .termsUrl("https://api.tidesandcurrents.noaa.gov/api/prod/")
                .mode(Mode.POLLING)
                .param(ParamSpec.required(STATIONS_PARAM, ParamType.STRING_LIST, "CO-OPS water-level station ids"
                        + " (8518750 The Battery), 1–20; Great Lakes stations are not supported"))
                .interval(Duration.ofMinutes(6), Duration.ofMinutes(6))
                .defaultBudget(Budget.of(40, 6000))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(HydroSources.STATE_RETENTION)
                .kindRetention(kind, HydroSources.STATE_RETENTION)
                .tileProperties("name", "waterLevelM", "floodCategory")
                .searchFields("name")
                .titleField("name")
                .rankField("waterLevelM");
        HydroSources.text(b, "name", "Name", "Station name");
        HydroSources.height(b, "waterLevelM", "m", CoopsMapper.DATUM, "Water level", "The latest 6-minute water level"
                + " above mean lower low water");
        HydroSources.text(b, "datum", "Datum", "The datum of every height: MLLW");
        HydroSources.number(b, "sigmaM", "m", "Sigma", "Standard deviation of the 1-second samples behind the"
                + " reading");
        HydroSources.text(b, "quality", "Quality", "preliminary or verified");
        HydroSources.timestamp(b, "nextHighTime", "Next high water", "Time of the next predicted high water");
        HydroSources.height(b, "nextHighM", "m", CoopsMapper.DATUM, "Next high height", "Predicted height of the next"
                + " high water");
        HydroSources.timestamp(b, "nextLowTime", "Next low water", "Time of the next predicted low water");
        HydroSources.height(b, "nextLowM", "m", CoopsMapper.DATUM, "Next low height", "Predicted height of the next low"
                + " water");
        HydroSources.text(b, "floodCategory", "Flood category", "none, action, minor, moderate or major, from the NOS"
                + " thresholds (NWS where NOS has none); absent without a minor threshold");
        HydroSources.height(b, "minorFloodM", "m", CoopsMapper.DATUM, "Minor flood level", "Minor flood threshold");
        HydroSources.height(b, "moderateFloodM", "m", CoopsMapper.DATUM, "Moderate flood level", "Moderate flood"
                + " threshold");
        HydroSources.height(b, "majorFloodM", "m", CoopsMapper.DATUM, "Major flood level", "Major flood threshold");
        b.kindDisplay(kind, new KindDisplay("{name}", "harbor", "#0B7285", null));
        return b.build();
    }
}
