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
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
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
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * The IMF PortWatch port catalog and daily port activity ({@code imf-portwatch-ports}, prompt 088), on prompt 073's
 * {@link ArcGisQueryClient} ({@link PortWatchSupport}).
 *
 * <p><strong>Ports</strong> ({@code PortWatch_ports_database}, 2,065 points on 2026-10-14): read again only when the
 * layer's edit date changes. One record per port: kind {@value #PORT_KIND}, externalId {@code portid}, Point from
 * {@code lat}/{@code lon}, payload {@code name, country, iso3, locode, vesselCountTotal, industries}. The source's time
 * base is upstream, so a port's {@code observedAt} is the layer's edit date (its publication time; see the 088
 * report). After a complete read, ports missing from it are tombstoned ({@link LiveIdSet}, scope {@value #PORT_KIND}).
 * The points are also kept in the state ({@value #POINTS_KEY}) to place the daily rows.
 *
 * <p><strong>Activity</strong> ({@code Daily_Ports_Data}): {@code date > DATE '{lastDate}'} (first run:
 * {@value #BACKFILL_PARAM} days, default 14), ordered by date and paged, at most {@value #MAX_PAGES} pages per run
 * (about 2,000 rows a day, so a 14-day backfill takes two runs). Kind {@value #ACTIVITY_KIND}, externalId
 * {@code portid:YYYY-MM-DD}, {@code eventTime} = {@code observedAt} = the day, payload {@code name, portcalls,
 * portcallsTanker, portcallsContainer, importTonnes, exportTonnes}.
 *
 * <p><strong>Scope:</strong> param {@value #PORTS_PARAM} (port ids, default empty = all) narrows both reads with
 * {@code portid IN (…)}; changing it starts over (a full catalog read and a new backfill). When neither layer changed,
 * no query is sent and the run is {@link FetchResult#unchanged() unchanged}.
 *
 * <p>12-hour poll, budget 20/min and 200/day. Retention: 90 days for the catalog ({@code reference}), 3650 days for the
 * activity ({@code series}).
 */
@Component
public class PortWatchPortsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "imf-portwatch-ports";

    /** The port catalog kind. */
    public static final String PORT_KIND = "port";

    /** The daily activity kind. */
    public static final String ACTIVITY_KIND = "port-activity";

    /** The port filter parameter. */
    public static final String PORTS_PARAM = "ports";

    /** The first-run window parameter. */
    public static final String BACKFILL_PARAM = "backfillDays";

    /** The default first-run window, days. */
    public static final int DEFAULT_BACKFILL_DAYS = 14;

    /** The largest first-run window, days. */
    public static final int MAX_BACKFILL_DAYS = 365;

    /** The most port ids in the filter (the ids go into the query string). */
    public static final int MAX_PORTS = 200;

    /** Daily-table pages per run; with the catalog pages and two metadata reads this stays within 20 a minute. */
    public static final int MAX_PAGES = 15;

    /** Catalog pages per run (2,065 ports in pages of 1,000). */
    public static final int CATALOG_PAGES = 4;

    /** The state key of the point table. */
    public static final String POINTS_KEY = "ports:points";

    /** The state key of the newest day read (ISO date). */
    public static final String LAST_DATE_KEY = "ports:lastDate";

    /** The state key of the port filter the stored state belongs to. */
    public static final String SCOPE_KEY = "ports:scope";

    /** The scope of the catalog layer. */
    static final String CATALOG_SCOPE = "ports";

    /** A port id. */
    static final Pattern PORT_ID = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");

    /** The catalog attributes requested. */
    static final List<String> PORT_FIELDS = List.of("portid", "portname", "fullname", "country", "ISO3", "LOCODE",
            "lat", "lon", "vessel_count_total", "industry_top1", "industry_top2", "industry_top3");

    /** The daily attributes requested. */
    static final List<String> DAILY_FIELDS = List.of("date", "portid", "portname", "portcalls", "portcalls_tanker",
            "portcalls_container", "import", "export");

    private final URI portsUrl;
    private final URI dailyUrl;
    private final ArcGisQueryClient arcgis = new ArcGisQueryClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value MaritimeSources#BASE_URL_PROPERTY}; blank in production
     */
    public PortWatchPortsAdapter(@Value("${" + MaritimeSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        String base = baseUrlOverride == null ? "" : baseUrlOverride;
        this.portsUrl = MaritimeSources.uri(PortWatchSupport.PORTS_URL, base);
        this.dailyUrl = MaritimeSources.uri(PortWatchSupport.DAILY_PORTS_URL, base);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** Accepts at most 200 well-formed port ids and a first-run window of 1 to 365 days. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params == null) {
            return problems;
        }
        if (params.has(PORTS_PARAM)) {
            List<String> ports = params.stringList(PORTS_PARAM);
            if (ports != null && ports.size() > MAX_PORTS) {
                problems.put(PORTS_PARAM, "at most " + MAX_PORTS + " port ids");
            } else if (ports != null && ports.stream().anyMatch(p -> p == null || !PORT_ID.matcher(p.strip())
                    .matches())) {
                problems.put(PORTS_PARAM, "port ids are PortWatch portid values such as port1325");
            }
        }
        if (params.has(BACKFILL_PARAM)) {
            Integer days = params.integer(BACKFILL_PARAM);
            if (days == null || days < 1 || days > MAX_BACKFILL_DAYS) {
                problems.put(BACKFILL_PARAM, "must be between 1 and " + MAX_BACKFILL_DAYS);
            }
        }
        return problems;
    }

    /**
     * Reads the catalog when it changed, then the new days of the activity table.
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
        List<String> ports = ports(params);
        int backfill = params.has(BACKFILL_PARAM) ? params.integer(BACKFILL_PARAM) : DEFAULT_BACKFILL_DAYS;
        AdapterStateStore state = ctx.state();
        String scope = ports.isEmpty() ? "*" : String.join(",", ports);
        if (!scope.equals(state.get(SCOPE_KEY).orElse(null))) {
            state.remove(POINTS_KEY);
            state.remove(LAST_DATE_KEY);
            PortWatchSupport.forceRead(state, CATALOG_SCOPE);
            PortWatchSupport.forceRead(state, PortWatchSupport.DAILY_SCOPE);
            state.put(SCOPE_KEY, scope);
        }
        Instant now = ctx.now();
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, PortWatchSupport.Place> points = PortWatchSupport.decode(state.get(POINTS_KEY).orElse(null));
        if (points.isEmpty()) {
            PortWatchSupport.forceRead(state, CATALOG_SCOPE);
        }
        FeatureFetch catalog = PortWatchSupport.read(arcgis, ctx, CATALOG_SCOPE,
                PortWatchSupport.request(portsUrl, portFilter(ports), PORT_FIELDS, CATALOG_PAGES, null));
        if (!catalog.unchanged()) {
            Instant published = PortWatchSupport.layerEditDate(state, CATALOG_SCOPE);
            Map<String, PortWatchSupport.Place> read = new LinkedHashMap<>();
            for (JsonNode feature : catalog) {
                JsonNode p = PortWatchSupport.properties(feature);
                String id = MaritimeSources.text(p.get("portid"));
                if (id == null) {
                    continue;
                }
                PortWatchSupport.Place place = PortWatchSupport.place(feature);
                if (place != null) {
                    read.put(id, place);
                }
                records.add(portRecord(id, p, place, published == null ? now : published));
            }
            if (catalog.complete() && !read.isEmpty()) {
                points = read;
                for (String id : LiveIdSet.absentSince(state, PORT_KIND, read.keySet())) {
                    records.add(NormalizedRecord.tombstone(id, PORT_KIND, now));
                }
            } else {
                points.putAll(read);
            }
            state.put(POINTS_KEY, PortWatchSupport.encode(points));
        }
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate from = PortWatchSupport.from(PortWatchSupport.storedDate(state.get(LAST_DATE_KEY).orElse(null)),
                today, backfill);
        String where = PortWatchSupport.dateWhere(from) + (ports.isEmpty() ? "" : " AND " + portFilter(ports));
        FeatureFetch daily = PortWatchSupport.read(arcgis, ctx, PortWatchSupport.DAILY_SCOPE,
                PortWatchSupport.request(dailyUrl, where, DAILY_FIELDS, MAX_PAGES, PortWatchSupport.DAILY_ORDER));
        if (catalog.unchanged() && daily.unchanged()) {
            return FetchResult.unchanged();
        }
        int activity = 0;
        if (!daily.unchanged()) {
            LocalDate newest = null;
            for (JsonNode feature : daily) {
                JsonNode p = PortWatchSupport.properties(feature);
                LocalDate day = PortWatchSupport.date(p.get("date"));
                String id = MaritimeSources.text(p.get("portid"));
                if (day == null || id == null) {
                    continue;
                }
                records.add(activityRecord(id, day, p, points.get(id)));
                activity++;
                newest = newest == null || day.isAfter(newest) ? day : newest;
            }
            state.put(LAST_DATE_KEY, PortWatchSupport.nextLastDate(from, newest, daily.complete()).toString());
            if (!daily.complete()) {
                ctx.log().info("{}: the daily read stopped early ({}); the next run resumes", ctx.sourceId(),
                        daily.stopReason());
            }
        }
        ctx.log().debug("{}: catalog {}, {} port-day(s) after {}", ctx.sourceId(),
                catalog.unchanged() ? "unchanged" : catalog.size() + " port(s)", activity, from);
        return FetchResult.of(records);
    }

    /** The validated port ids, trimmed and sorted. */
    static List<String> ports(ValidatedParams params) {
        if (!params.has(PORTS_PARAM) || params.stringList(PORTS_PARAM) == null) {
            return List.of();
        }
        TreeSet<String> out = new TreeSet<>();
        for (String p : params.stringList(PORTS_PARAM)) {
            if (p != null && !p.isBlank()) {
                out.add(p.strip());
            }
        }
        return List.copyOf(out);
    }

    /** {@code portid IN ('a','b')}, or {@code 1=1} for all ports. */
    static String portFilter(List<String> ports) {
        if (ports.isEmpty()) {
            return "1=1";
        }
        List<String> quoted = new ArrayList<>();
        for (String p : ports) {
            quoted.add("'" + p.replace("'", "") + "'");
        }
        return "portid IN (" + String.join(",", quoted) + ")";
    }

    /**
     * The catalog record of one port.
     *
     * @param id the port id
     * @param p the catalog row
     * @param place its point, or null
     * @param published the layer's edit date
     * @return the record
     */
    static NormalizedRecord portRecord(String id, JsonNode p, PortWatchSupport.Place place, Instant published) {
        List<String> industries = new ArrayList<>();
        for (String field : List.of("industry_top1", "industry_top2", "industry_top3")) {
            String value = MaritimeSources.text(p.get(field));
            if (value != null) {
                industries.add(value);
            }
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, PORT_KIND)
                .observedAt(published)
                .put("name", MaritimeSources.text(p.get("portname")))
                .put("country", MaritimeSources.text(p.get("country")))
                .put("iso3", MaritimeSources.text(p.get("ISO3")))
                .put("locode", MaritimeSources.text(p.get("LOCODE")))
                .put("vesselCountTotal", MaritimeSources.integer(p.get("vessel_count_total")))
                .put("industries", industries.isEmpty() ? null : industries);
        if (place != null) {
            b.point(place.lon(), place.lat());
        }
        return b.build();
    }

    /**
     * The activity record of one port-day.
     *
     * @param id the port id
     * @param day the day
     * @param p the daily row
     * @param place the port point, or null
     * @return the record
     */
    static NormalizedRecord activityRecord(String id, LocalDate day, JsonNode p, PortWatchSupport.Place place) {
        Instant at = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        String name = MaritimeSources.text(p.get("portname"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(id + ":" + day, ACTIVITY_KIND)
                .eventTime(at)
                .observedAt(at)
                .put("name", name != null ? name : place == null ? null : place.name())
                .put("portcalls", MaritimeSources.integer(p.get("portcalls")))
                .put("portcallsTanker", MaritimeSources.integer(p.get("portcalls_tanker")))
                .put("portcallsContainer", MaritimeSources.integer(p.get("portcalls_container")))
                .put("importTonnes", MaritimeSources.integer(p.get("import")))
                .put("exportTonnes", MaritimeSources.integer(p.get("export")));
        if (place != null) {
            b.point(place.lon(), place.lat());
        }
        return b.build();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("IMF PortWatch ports")
                .description("About 2,000 ports from IMF PortWatch with their country, UN/LOCODE, vessel count and"
                        + " top industries, and each port's daily port calls and estimated import and export volumes"
                        + " from AIS. The data updates weekly with a lag of about ten days; nothing is queried while"
                        + " both layers are unchanged. Optionally limited to a list of PortWatch port ids.")
                .kinds(PORT_KIND, ACTIVITY_KIND)
                .homepage(PortWatchSupport.HOMEPAGE)
                .attribution(PortWatchSupport.ATTRIBUTION)
                .licence(PortWatchSupport.LICENSE)
                .termsUrl(PortWatchSupport.TERMS_URL)
                .mode(Mode.POLLING)
                .param(ParamSpec.optional(PORTS_PARAM, ParamType.STRING_LIST, "PortWatch port ids (portid, for"
                        + " example port1325) to read; empty reads all ports", List.of()))
                .param(ParamSpec.optional(BACKFILL_PARAM, ParamType.INTEGER, "Days of activity read on the first run,"
                        + " 1 to 365", DEFAULT_BACKFILL_DAYS))
                .interval(Duration.ofHours(1), Duration.ofHours(12))
                .defaultBudget(Budget.of(20, 200))
                .maxResponseBytes(16 * 1024 * 1024)
                .fetchTimeout(Duration.ofSeconds(90))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(MaritimeSources.SERIES_RETENTION)
                .kindRetention(PORT_KIND, MaritimeSources.REFERENCE_RETENTION)
                .kindRetention(ACTIVITY_KIND, MaritimeSources.SERIES_RETENTION)
                .zeroResultsNormal(true)
                .titleField("name")
                .rankField("portcalls")
                .searchFields("name", "country", "locode")
                .tileProperties("name", "portcalls", "vesselCountTotal");
        MaritimeSources.text(b, "name", "Port", "The port name");
        MaritimeSources.text(b, "country", "Country", "The country name");
        MaritimeSources.text(b, "iso3", "ISO 3166-1 alpha-3", "The country code");
        MaritimeSources.text(b, "locode", "UN/LOCODE", "The UN/LOCODE, for example JP TRG");
        MaritimeSources.integer(b, "vesselCountTotal", null, "Vessels", "Vessels that called at the port in"
                + " PortWatch's reference period");
        MaritimeSources.array(b, "industries", "Top industries", "The port's top three industries");
        MaritimeSources.integer(b, "portcalls", null, "Port calls", "Port calls that day, all types");
        MaritimeSources.integer(b, "portcallsTanker", null, "Tanker calls", "Tanker port calls that day");
        MaritimeSources.integer(b, "portcallsContainer", null, "Container calls", "Container ship port calls that day");
        MaritimeSources.integer(b, "importTonnes", "t", "Imports", "Estimated import volume that day, metric tonnes");
        MaritimeSources.integer(b, "exportTonnes", "t", "Exports", "Estimated export volume that day, metric tonnes");
        return b.build();
    }
}
