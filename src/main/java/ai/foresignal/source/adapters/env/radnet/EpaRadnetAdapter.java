package ai.foresignal.source.adapters.env.radnet;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.spi.AdapterException;
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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code epa-radnet} (prompt 078): the newest reading of each chosen EPA RadNet fixed air monitor, as a
 * {@code radiation} point record ({@link RadnetCsvParser}, stations from {@link RadnetStations}).
 *
 * <ul>
 *   <li><strong>Param:</strong> {@value #STATIONS_PARAM} (required, at most {@value #MAX_STATIONS}), each a
 *       {@code STATE/CITY} id from the bundled list.
 *   <li><strong>Each run:</strong> one request per station for the current UTC year's CSV ({@value #CSV_URL}…),
 *       keeping only the newest row. On 1 January the previous year's file is also read, once per station and
 *       year (state key {@value #ROLLOVER_PREFIX}{@code {station}}), so the last readings of 31 December are not
 *       lost while the new file is still empty. A station whose request fails is logged and skipped; the run fails
 *       only when every station failed.
 *   <li><strong>Cost:</strong> about 600 KB per station by October with no validators, hence a 3 h interval.
 * </ul>
 */
@Component
public class EpaRadnetAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "epa-radnet";

    /** The CSV root; {@code {year}/fixed/{STATE}/{CITY}} follows. */
    public static final String CSV_URL = "https://radnet.epa.gov/cdx-radnet-rest/api/rest/csv/";

    /** The stations parameter. */
    public static final String STATIONS_PARAM = "stations";

    /** The most stations one source reads. */
    public static final int MAX_STATIONS = 10;

    /** The state key prefix recording the year whose rollover read is done. */
    public static final String ROLLOVER_PREFIX = "rollover:";

    /** The default {@code maxAge}. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofHours(24);

    /** The record TTL. */
    static final Duration TTL = Duration.ofHours(24);

    /** Raw retention: monitor readings are the {@code state} class (365 d). */
    static final Duration RETENTION = Duration.ofDays(365);

    private final String csvRoot;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value EnvSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public EpaRadnetAdapter(@Value("${" + EnvSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public EpaRadnetAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.csvRoot = EnvSources.uri(CSV_URL, baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** At most {@value #MAX_STATIONS} stations, each in the bundled list. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> ids = params.stringList(STATIONS_PARAM);
        if (ids.size() > MAX_STATIONS) {
            problems.put(STATIONS_PARAM, "at most " + MAX_STATIONS + " stations, but " + ids.size() + " were given");
            return problems;
        }
        List<String> unknown = new ArrayList<>();
        for (String id : ids) {
            if (RadnetStations.find(id).isEmpty()) {
                unknown.add(id);
            }
        }
        if (!unknown.isEmpty()) {
            problems.put(STATIONS_PARAM, "not RadNet stations: " + unknown + "; use STATE/CITY as EPA names them,"
                    + " for example CO/DENVER or CA/LOS ANGELES");
        }
        return problems;
    }

    /** The CSV URL of {@code station} for {@code year}; spaces in the city become {@code %20}. */
    public URI csvUri(RadnetStations.Station station, int year) {
        return URI.create(csvRoot + year + "/fixed/" + station.state() + "/" + station.city().replace(" ", "%20"));
    }

    /**
     * Reads every chosen station once (see the class note).
     *
     * @throws AdapterException when every station's request failed, or no station is configured
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Set<RadnetStations.Station> stations = new LinkedHashSet<>();
        for (String id : ctx.params() == null ? List.<String>of() : ctx.params().stringList(STATIONS_PARAM)) {
            RadnetStations.find(id).ifPresent(stations::add);
        }
        if (stations.isEmpty()) {
            throw new AdapterException(ID + ": the stations parameter names no RadNet station");
        }
        Instant now = ctx.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        EnvSources.Collector collector = new EnvSources.Collector(now,
                EnvSources.maxAge(ctx.params(), DEFAULT_MAX_AGE));
        Exception firstFailure = null;
        int failed = 0;
        for (RadnetStations.Station station : stations) {
            try {
                Optional<RadnetCsvParser.Row> row = read(ctx, station, today.getYear());
                if (today.getDayOfYear() == 1 && !String.valueOf(today.getYear())
                        .equals(ctx.state().get(ROLLOVER_PREFIX + station.id()).orElse(null))) {
                    Optional<RadnetCsvParser.Row> previous = read(ctx, station, today.getYear() - 1);
                    ctx.state().put(ROLLOVER_PREFIX + station.id(), String.valueOf(today.getYear()));
                    if (previous.isPresent() && (row.isEmpty() || previous.get().time().isAfter(row.get().time()))) {
                        row = previous;
                    }
                }
                if (row.isPresent()) {
                    collector.add(RadnetCsvParser.record(station, row.get()));
                } else {
                    collector.skip("no-rows");
                }
            } catch (AdapterException | UpstreamFormatException e) {
                failed++;
                if (firstFailure == null) {
                    firstFailure = e;
                }
                ctx.log().warn("{}: station {} failed: {}", ID, station.id(), e.getMessage());
            }
        }
        if (failed == stations.size()) {
            if (firstFailure instanceof AdapterException a) {
                throw a;
            }
            throw (UpstreamFormatException) firstFailure;
        }
        EnvSources.Parsed parsed = collector.result();
        SkipCounter.count(meters, EnvSources.SKIPPED_METRIC, EnvSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                parsed.skipped());
        return FetchResult.of(parsed.records());
    }

    private Optional<RadnetCsvParser.Row> read(FetchContext ctx, RadnetStations.Station station, int year)
            throws AdapterException {
        URI uri = csvUri(station, year);
        return RadnetCsvParser.newest(EnvSources.body(ctx, ctx.http().get(uri), null, uri));
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = RadnetCsvParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("EPA RadNet")
                .description("The newest gamma reading of up to 10 chosen U.S. EPA RadNet fixed air monitors: gross"
                        + " count rate, the eight energy bands and, when published, the dose equivalent rate. One"
                        + " CSV download per station per run (about 600 KB by October), every 3 h by default. Station"
                        + " points are approximate (the city).")
                .kind(kind)
                .homepage("https://www.epa.gov/radnet")
                .attribution("U.S. Environmental Protection Agency, RadNet")
                .licence("US-PD")
                .termsUrl("https://www.epa.gov/radnet/radnet-csv-file-downloads")
                .mode(Mode.POLLING)
                .param(new ParamSpec(STATIONS_PARAM, ParamType.STRING_LIST, "RadNet stations as STATE/CITY (for"
                        + " example CO/DENVER, CA/LOS ANGELES), at most 10, from the bundled station list", null, true,
                        List.of()))
                .param(EnvSources.maxAgeParam("24h"))
                .interval(Duration.ofHours(1), Duration.ofHours(3))
                .defaultBudget(Budget.of(12, 100, 150))
                .maxResponseBytes(8 * 1024 * 1024)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(TTL)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .tileProperties("grossCpm", "doseRateNsvH")
                .searchFields()
                .rankField("grossCpm");
        b.fieldSpec("doseRateNsvH", new FieldSpec(FieldType.NUMBER, "nSv/h", "Dose equivalent rate",
                "Dose equivalent rate as published (often empty)", null, Set.of()));
        b.fieldSpec("grossCpm", new FieldSpec(FieldType.NUMBER, "{counts}/min", "Gross count rate",
                "Sum of the gamma count rates of bands R02 to R09", null, Set.of()));
        b.fieldSpec("bands", new FieldSpec(FieldType.OBJECT, "{counts}/min", "Gamma bands",
                "Gamma count rate per energy band, R02 to R09", null, Set.of()));
        b.fieldSpec("status", new FieldSpec(FieldType.STRING, null, "Status", "APPROVED or PRELIMINARY", null,
                Set.of()));
        b.kindDisplay(kind, new KindDisplay("{grossCpm} CPM", "radiation", "#E8590C", null));
        return b.build();
    }
}
