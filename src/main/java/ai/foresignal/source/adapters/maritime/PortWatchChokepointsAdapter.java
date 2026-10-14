package ai.foresignal.source.adapters.maritime;

import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Daily vessel transits through the world's maritime chokepoints from IMF PortWatch ({@code imf-portwatch-chokepoints},
 * prompt 088), joined to the chokepoint points, on prompt 073's {@link ArcGisQueryClient} ({@link PortWatchSupport}).
 *
 * <p><strong>Run:</strong> first the chokepoint points ({@code PortWatch_chokepoints_database}, 28 points on
 * 2026-10-14) are loaded, read again only when that layer's edit date changes and otherwise taken from the state
 * ({@value #POINTS_KEY}); then {@code Daily_Chokepoints_Data} is queried with {@code date > DATE '{lastDate}'} (first
 * run: {@value #BACKFILL_PARAM} days, default 30), ordered by date and paged, at most {@value #MAX_PAGES} pages per
 * run. When the daily table's edit date is unchanged, no table query is sent and the run is
 * {@link FetchResult#unchanged() unchanged}.
 *
 * <p><strong>Record</strong> per chokepoint and day: kind {@value #KIND}, externalId {@code portid:YYYY-MM-DD},
 * {@code eventTime} = {@code observedAt} = the day (the measurement period, UTC midnight), geometry the chokepoint
 * point, payload {@code chokepoint, nTotal, nTanker, nContainer, nDryBulk, nGeneralCargo, nRoro, nCargo,
 * capacityTotal, capacityTanker} (capacities in deadweight tonnes), rank field {@code nTotal}. A re-read day produces
 * the same record, which the ingest hash makes a no-op.
 *
 * <p>12-hour poll (the data updates weekly), budget 20/min and 200/day, retention 3650 days ({@code series}).
 */
@Component
public class PortWatchChokepointsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "imf-portwatch-chokepoints";

    /** The record kind. */
    public static final String KIND = "chokepoint-transit";

    /** The first-run window parameter. */
    public static final String BACKFILL_PARAM = "backfillDays";

    /** The default first-run window, days. */
    public static final int DEFAULT_BACKFILL_DAYS = 30;

    /** The largest first-run window, days. */
    public static final int MAX_BACKFILL_DAYS = 365;

    /** Daily-table pages per run (28 chokepoints a day: one page holds five weeks). */
    public static final int MAX_PAGES = 10;

    /** The state key of the point table. */
    public static final String POINTS_KEY = "cp:points";

    /** The state key of the newest day read (ISO date). */
    public static final String LAST_DATE_KEY = "cp:lastDate";

    /** The point attributes requested. */
    static final List<String> POINT_FIELDS = List.of("portid", "portname", "fullname", "lat", "lon");

    /** The daily attributes requested. */
    static final List<String> DAILY_FIELDS = List.of("date", "portid", "portname", "n_container", "n_dry_bulk",
            "n_general_cargo", "n_roro", "n_tanker", "n_cargo", "n_total", "capacity_tanker", "capacity");

    private final URI pointsUrl;
    private final URI dailyUrl;
    private final ArcGisQueryClient arcgis = new ArcGisQueryClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value MaritimeSources#BASE_URL_PROPERTY}; blank in production
     */
    public PortWatchChokepointsAdapter(@Value("${" + MaritimeSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String base = baseUrlOverride == null ? "" : baseUrlOverride;
        this.pointsUrl = MaritimeSources.uri(PortWatchSupport.CHOKEPOINTS_URL, base);
        this.dailyUrl = MaritimeSources.uri(PortWatchSupport.DAILY_CHOKEPOINTS_URL, base);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Accepts a first-run window of 1 to 365 days. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params != null && params.has(BACKFILL_PARAM)) {
            Integer days = params.integer(BACKFILL_PARAM);
            if (days == null || days < 1 || days > MAX_BACKFILL_DAYS) {
                problems.put(BACKFILL_PARAM, "must be between 1 and " + MAX_BACKFILL_DAYS);
            }
        }
        return problems;
    }

    /**
     * Loads the points, then reads the new days of the daily table.
     *
     * @throws AdapterException when a request fails or the parameters are invalid
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params() == null ? ValidatedParams.empty() : ctx.params();
        Map<String, String> problems = checkParams(params);
        if (!problems.isEmpty()) {
            throw new AdapterException(ID + ": " + problems);
        }
        int backfill = params.has(BACKFILL_PARAM) ? params.integer(BACKFILL_PARAM) : DEFAULT_BACKFILL_DAYS;
        AdapterStateStore state = ctx.state();
        Map<String, PortWatchSupport.Place> points = points(ctx);
        LocalDate today = ctx.now().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate from = PortWatchSupport.from(PortWatchSupport.storedDate(state.get(LAST_DATE_KEY).orElse(null)),
                today, backfill);
        FeatureFetch daily = PortWatchSupport.read(arcgis, ctx, PortWatchSupport.DAILY_SCOPE,
                PortWatchSupport.request(dailyUrl, PortWatchSupport.dateWhere(from), DAILY_FIELDS, MAX_PAGES,
                        PortWatchSupport.DAILY_ORDER));
        if (daily.unchanged()) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> records = new ArrayList<>();
        LocalDate newest = null;
        int rejected = 0;
        int unplaced = 0;
        for (JsonNode feature : daily) {
            JsonNode p = PortWatchSupport.properties(feature);
            LocalDate day = PortWatchSupport.date(p.get("date"));
            String portId = MaritimeSources.text(p.get("portid"));
            if (day == null || portId == null) {
                rejected++;
                continue;
            }
            PortWatchSupport.Place place = points.get(portId);
            if (place == null) {
                unplaced++;
            }
            records.add(record(portId, day, p, place));
            newest = newest == null || day.isAfter(newest) ? day : newest;
        }
        state.put(LAST_DATE_KEY, PortWatchSupport.nextLastDate(from, newest, daily.complete()).toString());
        if (!daily.complete()) {
            ctx.log().info("{}: the daily read stopped early ({}); the next run resumes", ctx.sourceId(),
                    daily.stopReason());
        }
        ctx.log().debug("{}: {} chokepoint-day(s) after {}, {} rejected, {} without a point", ctx.sourceId(),
                records.size(), from, rejected, unplaced);
        return FetchResult.of(records);
    }

    /** The point table: read again when the point layer changed (or none is stored), else the stored one. */
    private Map<String, PortWatchSupport.Place> points(FetchContext ctx) throws AdapterException {
        AdapterStateStore state = ctx.state();
        Map<String, PortWatchSupport.Place> stored = PortWatchSupport.decode(state.get(POINTS_KEY).orElse(null));
        if (stored.isEmpty()) {
            PortWatchSupport.forceRead(state, PortWatchSupport.POINTS_SCOPE);
        }
        FeatureFetch fetch = PortWatchSupport.read(arcgis, ctx, PortWatchSupport.POINTS_SCOPE,
                PortWatchSupport.request(pointsUrl, "1=1", POINT_FIELDS, 2, null));
        if (fetch.unchanged()) {
            return stored;
        }
        if (!fetch.complete()) {
            throw new UpstreamFormatException("PortWatch chokepoint points stopped early (" + fetch.stopReason() + ")",
                    "");
        }
        Map<String, PortWatchSupport.Place> places = new LinkedHashMap<>();
        for (JsonNode feature : fetch) {
            String id = MaritimeSources.text(PortWatchSupport.properties(feature).get("portid"));
            PortWatchSupport.Place place = PortWatchSupport.place(feature);
            if (id != null && place != null) {
                places.put(id, place);
            }
        }
        state.put(POINTS_KEY, PortWatchSupport.encode(places));
        return places;
    }

    /**
     * The record of one chokepoint-day.
     *
     * @param portId the chokepoint id ({@code chokepoint1})
     * @param day the day
     * @param p the daily row
     * @param place the chokepoint point, or null
     * @return the record
     */
    static NormalizedRecord record(String portId, LocalDate day, JsonNode p, PortWatchSupport.Place place) {
        Instant at = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        String name = place != null && place.name() != null ? place.name() : MaritimeSources.text(p.get("portname"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(portId + ":" + day, KIND)
                .eventTime(at)
                .observedAt(at)
                .put("chokepoint", name)
                .put("nTotal", MaritimeSources.integer(p.get("n_total")))
                .put("nTanker", MaritimeSources.integer(p.get("n_tanker")))
                .put("nContainer", MaritimeSources.integer(p.get("n_container")))
                .put("nDryBulk", MaritimeSources.integer(p.get("n_dry_bulk")))
                .put("nGeneralCargo", MaritimeSources.integer(p.get("n_general_cargo")))
                .put("nRoro", MaritimeSources.integer(p.get("n_roro")))
                .put("nCargo", MaritimeSources.integer(p.get("n_cargo")))
                .put("capacityTotal", MaritimeSources.integer(p.get("capacity")))
                .put("capacityTanker", MaritimeSources.integer(p.get("capacity_tanker")));
        if (place != null) {
            b.point(place.lon(), place.lat());
        }
        return b.build();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("IMF PortWatch chokepoint transits")
                .description("Daily vessel transits and capacity through 28 maritime chokepoints (Suez, Panama,"
                        + " Hormuz, Malacca, Bab el-Mandeb and others) from IMF PortWatch, estimated from AIS. The data"
                        + " updates weekly with a lag of about ten days; a run sends no table query while the table"
                        + " is unchanged.")
                .kind(KIND)
                .homepage(PortWatchSupport.HOMEPAGE)
                .attribution(PortWatchSupport.ATTRIBUTION)
                .licence(PortWatchSupport.LICENSE)
                .termsUrl(PortWatchSupport.TERMS_URL)
                .mode(Mode.POLLING)
                .param(ParamSpec.optional(BACKFILL_PARAM, ParamType.INTEGER, "Days read on the first run, 1 to 365",
                        DEFAULT_BACKFILL_DAYS))
                .interval(Duration.ofHours(1), Duration.ofHours(12))
                .defaultBudget(Budget.of(20, 200))
                .maxResponseBytes(8 * 1024 * 1024)
                .fetchTimeout(Duration.ofSeconds(60))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(MaritimeSources.SERIES_RETENTION)
                .kindRetention(KIND, MaritimeSources.SERIES_RETENTION)
                .zeroResultsNormal(true)
                .titleField("chokepoint")
                .rankField("nTotal")
                .searchFields("chokepoint")
                .tileProperties("chokepoint", "nTotal", "nTanker");
        MaritimeSources.text(b, "chokepoint", "Chokepoint", "The chokepoint name, for example Suez Canal");
        MaritimeSources.integer(b, "nTotal", null, "Transits", "Vessel transits that day, all types");
        MaritimeSources.integer(b, "nTanker", null, "Tankers", "Tanker transits");
        MaritimeSources.integer(b, "nContainer", null, "Container ships", "Container ship transits");
        MaritimeSources.integer(b, "nDryBulk", null, "Dry bulk", "Dry bulk carrier transits");
        MaritimeSources.integer(b, "nGeneralCargo", null, "General cargo", "General cargo ship transits");
        MaritimeSources.integer(b, "nRoro", null, "Ro-ro", "Roll-on/roll-off ship transits");
        MaritimeSources.integer(b, "nCargo", null, "Cargo", "Cargo ship transits (all but tankers)");
        MaritimeSources.integer(b, "capacityTotal", "t", "Capacity", "Deadweight tonnage of all transits, tonnes");
        MaritimeSources.integer(b, "capacityTanker", "t", "Tanker capacity", "Deadweight tonnage of tanker"
                + " transits, tonnes");
        return b.build();
    }
}
