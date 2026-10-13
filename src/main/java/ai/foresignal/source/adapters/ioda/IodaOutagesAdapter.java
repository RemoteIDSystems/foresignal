package ai.foresignal.source.adapters.ioda;

import ai.foresignal.reference.Admin1Centroids;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
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
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Internet-outage alerts from IODA, Georgia Tech's Internet Outage Detection and Analysis project (prompt 068).
 *
 * <p><strong>State, not events.</strong> Each poll asks {@code GET /v2/outages/alerts?from&until&limit=500&page=n}
 * for the last {@code window} (default 12 h) and folds the alerts into one {@code outage} record per entity and
 * datasource ({@code externalId = {entityType}:{entityCode}:{datasource}}), carrying the newest alert's
 * {@code level}. IODA raises an alert ({@code critical}) when a signal drops below its threshold and clears it with
 * a {@code normal} alert, so the record's level gives rules clean enter and exit transitions. The raw measurements
 * ({@code value}, {@code historyValue}, {@code ratio}) are volatile: they change on every alert and never make a
 * version alone.
 *
 * <p><strong>Expiry, not tombstones.</strong> The result is never complete (it is a time window). Every poll
 * re-observes the newest alert of each key still inside the window; a key that falls out of the window expires
 * {@code recordTtl} (6 h, anchored on receipt) after it was last seen.
 *
 * <p><strong>Requests</strong> (verified 2026-10-13): {@code from} and {@code until} are epoch seconds; pages are
 * numbered from 0 ({@code page=0} returns the same rows as no page); rows come in ascending time. A page of 500 rows
 * is followed by the next, up to {@value #MAX_PAGES} pages; when the cap is hit the run reports
 * {@code Coverage(expected=null, actual=rows)} and logs it at most once an hour. {@code entityType} is sent only when
 * exactly one type is configured, and {@code entityCode} only when exactly one country is configured and the only
 * type is {@code country}: IODA matches {@code entityCode} against the entity's own code, so a country code would
 * drop every region of that country. Every other filter runs here.
 *
 * <p><strong>Geometry</strong> comes from {@link IodaGeometry}: 040's bundled country label points and Natural
 * Earth admin-1 label points, never a geocoding service.
 *
 * <p><strong>License.</strong> Every response carries "This data is Copyright (c) 2021-2025 Georgia Tech Research
 * Corporation. All Rights Reserved." and IODA publishes no terms of use, so the source is {@code LOCAL_ONLY}: the
 * data is not re-served beyond this instance's own clients without permission.
 */
@Component
public class IodaOutagesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ioda-outages";

    /** The record kind. */
    public static final String KIND = IodaAlertsParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.ioda.inetintel.cc.gatech.edu";

    /** Test-only override of {@link #DEFAULT_BASE_URL}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ioda.base-url";

    /** The alerts path. */
    public static final String PATH = "/v2/outages/alerts";

    /** The {@code entityTypes} parameter. */
    public static final String ENTITY_TYPES_PARAM = "entityTypes";

    /** The {@code datasources} parameter. */
    public static final String DATASOURCES_PARAM = "datasources";

    /** The {@code window} parameter. */
    public static final String WINDOW_PARAM = "window";

    /** The {@code countries} parameter. */
    public static final String COUNTRIES_PARAM = "countries";

    /** IODA's project page; no terms of use are published. */
    public static final String TERMS_URL = "https://ioda.inetintel.cc.gatech.edu/";

    /** The license string, as the prompt gives it (a published descriptor value). */
    public static final String LICENSE =
            "IODA, Georgia Tech Research Corporation — all rights reserved (no published licence)";

    /** Rows per page. */
    public static final int PAGE_SIZE = 500;

    /** The most pages one run reads. */
    public static final int MAX_PAGES = 3;

    /** The default entity types. */
    public static final List<String> DEFAULT_ENTITY_TYPES = List.of("country", "region");

    /** The default datasources. */
    public static final List<String> DEFAULT_DATASOURCES = List.of("bgp", "ping-slash24");

    /** The default window. */
    public static final Duration DEFAULT_WINDOW = Duration.ofHours(12);

    /** The shortest window. */
    static final Duration MIN_WINDOW = Duration.ofHours(1);

    /** The longest window. */
    static final Duration MAX_WINDOW = Duration.ofHours(24);

    /** How long a key stays current after it was last re-observed. */
    static final Duration RECORD_TTL = Duration.ofHours(6);

    /** Raw retention: outages are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The volatile measurements. */
    static final String[] VOLATILE = {"value", "historyValue", "ratio"};

    private static final Pattern ISO2 = Pattern.compile("^[A-Za-z]{2}$");
    private static final Duration CAP_LOG_EVERY = Duration.ofHours(1);
    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;
    private final IodaGeometry geometry;
    private volatile Instant lastCapLog;

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank means {@value #DEFAULT_BASE_URL}
     * @param countries the bundled country table (040); null places no country
     * @param admin1 the admin-1 label points (040); null places no region
     * @param meters the meter registry; null uses a private one
     */
    @Autowired
    public IodaOutagesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries, Admin1Centroids admin1, MeterRegistry meters) {
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank() ? DEFAULT_BASE_URL : baseUrlOverride.trim();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.geometry = new IodaGeometry(countries, admin1, meters);
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * The request URL of one page.
     *
     * @param from the window start, epoch seconds
     * @param until the window end, epoch seconds
     * @param page the page, from 0
     * @param entityType the single entity type, or null
     * @param entityCode the single entity code, or null
     * @return the URL
     */
    public URI alertsUri(long from, long until, int page, String entityType, String entityCode) {
        StringBuilder url = new StringBuilder(baseUrl).append(PATH).append("?from=").append(from).append("&until=")
                .append(until).append("&limit=").append(PAGE_SIZE).append("&page=").append(page);
        if (entityType != null) {
            url.append("&entityType=").append(entityType);
        }
        if (entityCode != null) {
            url.append("&entityCode=").append(entityCode);
        }
        return URI.create(url.toString());
    }

    /** Refuses a window outside 1–24 h, a malformed country code, and explicitly empty type or datasource lists. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        Duration window = params.duration(WINDOW_PARAM);
        if (window != null && (window.compareTo(MIN_WINDOW) < 0 || window.compareTo(MAX_WINDOW) > 0)) {
            problems.put(WINDOW_PARAM, "must be 1 h to 24 h, but was " + window);
        }
        if (params.has(ENTITY_TYPES_PARAM) && params.stringList(ENTITY_TYPES_PARAM).isEmpty()) {
            problems.put(ENTITY_TYPES_PARAM, "must name at least one of " + String.join(", ", IodaAlertsParser.ENTITY_TYPES));
        }
        if (params.has(DATASOURCES_PARAM) && params.stringList(DATASOURCES_PARAM).isEmpty()) {
            problems.put(DATASOURCES_PARAM, "must name at least one of " + String.join(", ", IodaAlertsParser.DATASOURCES));
        }
        for (String code : params.stringList(COUNTRIES_PARAM)) {
            if (code == null || !ISO2.matcher(code.strip()).matches()) {
                problems.put(COUNTRIES_PARAM, "'" + code + "' is not an ISO 3166-1 alpha-2 code");
                break;
            }
        }
        return problems;
    }

    /** The configured window, 12 h when unset. */
    static Duration window(ValidatedParams params) {
        Duration window = params.duration(WINDOW_PARAM);
        return window == null ? DEFAULT_WINDOW : window;
    }

    /** The configured entity types, lower-cased and de-duplicated; the defaults when none are set. */
    static Set<String> entityTypes(ValidatedParams params) throws AdapterException {
        return allowed(params, ENTITY_TYPES_PARAM, IodaAlertsParser.ENTITY_TYPES, DEFAULT_ENTITY_TYPES);
    }

    /** The configured datasources, lower-cased and de-duplicated; the defaults when none are set. */
    static Set<String> datasources(ValidatedParams params) throws AdapterException {
        return allowed(params, DATASOURCES_PARAM, IodaAlertsParser.DATASOURCES, DEFAULT_DATASOURCES);
    }

    /** The configured countries, upper-cased; empty means every country. */
    static Set<String> countries(ValidatedParams params) {
        Set<String> out = new LinkedHashSet<>();
        for (String code : params.stringList(COUNTRIES_PARAM)) {
            if (code != null && !code.isBlank()) {
                out.add(code.strip().toUpperCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static Set<String> allowed(ValidatedParams params, String name, List<String> allowed, List<String> defaults)
            throws AdapterException {
        Set<String> out = new LinkedHashSet<>();
        for (String value : params.has(name) ? params.stringList(name) : List.<String>of()) {
            String v = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
            if (v.isEmpty()) {
                continue;
            }
            if (!allowed.contains(v)) {
                throw new AdapterException("ioda-outages " + name + " value '" + value + "' is not one of " + allowed);
            }
            out.add(v);
        }
        return out.isEmpty() ? new LinkedHashSet<>(defaults) : out;
    }

    /**
     * Reads up to {@value #MAX_PAGES} pages of the window and folds them into one record per key.
     *
     * @return the newest state per key, never complete
     * @throws AdapterException when a request fails; {@link UpstreamFormatException} when a body is truncated or not
     *     the IODA envelope
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        Set<String> types = entityTypes(params);
        Set<String> sources = datasources(params);
        Set<String> countries = countries(params);
        Instant now = ctx.now();
        long until = now.getEpochSecond();
        long from = now.minus(window(params)).getEpochSecond();
        String typeParam = types.size() == 1 ? types.iterator().next() : null;
        String codeParam = countries.size() == 1 && "country".equals(typeParam) ? countries.iterator().next() : null;

        List<IodaAlertsParser.Alert> alerts = new ArrayList<>();
        int rows = 0;
        int skipped = 0;
        boolean capped = false;
        for (int page = 0; page < MAX_PAGES; page++) {
            UpstreamResponse response = ctx.http().get(alertsUri(from, until, page, typeParam, codeParam),
                    Map.of("Accept", "application/json"));
            if (response.truncated()) {
                throw new UpstreamFormatException("IODA alerts page " + page + " exceeded maxResponseBytes", null);
            }
            IodaAlertsParser.Page parsed = IodaAlertsParser.parse(response.body());
            for (IodaAlertsParser.Alert a : parsed.alerts()) {
                alerts.add(a.at(rows + a.index()));
            }
            rows += parsed.rows();
            skipped += parsed.skipped();
            if (parsed.rows() < PAGE_SIZE) {
                break;
            }
            capped = page == MAX_PAGES - 1;
        }

        List<NormalizedRecord> records = new ArrayList<>();
        for (IodaAlertsParser.Alert a : IodaAlertsParser.fold(alerts, types, sources)) {
            if (!countries.isEmpty() && !countries.contains(IodaAlertsParser.country(a))) {
                continue;
            }
            records.add(IodaAlertsParser.toRecord(a, geometry.resolve(a)));
        }
        ctx.log().debug("IODA: {} record(s) from {} row(s), {} unreadable, {} page cap {}", records.size(), rows, skipped,
                MAX_PAGES, capped ? "hit" : "not hit");
        FetchResult result = FetchResult.of(records);
        if (capped) {
            logCap(ctx, now, rows);
            result = result.withCoverage(new Coverage(null, null, rows, null));
        }
        return result;
    }

    private void logCap(FetchContext ctx, Instant now, int rows) {
        Instant last = lastCapLog;
        if (last == null || !now.isBefore(last.plus(CAP_LOG_EVERY))) {
            lastCapLog = now;
            ctx.log().warn("IODA: the {}-page cap was hit ({} rows); older alerts in the window were not read."
                    + " Narrow entityTypes, datasources or window.", MAX_PAGES, rows);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("IODA Internet Outages")
                .description("Internet-outage alerts from IODA (Georgia Tech) for countries, regions and networks, as"
                        + " one state per entity and datasource whose level is critical while IODA's alert is raised"
                        + " and normal once it clears. A critical level on one datasource is a signal, not a confirmed"
                        + " outage: corroborate across datasources. Countries and regions are placed at approximate"
                        + " label points; networks have no geometry. LOCAL_ONLY: every response is copyright Georgia"
                        + " Tech Research Corporation with no published terms, so the data is not re-served.")
                .kind(KIND)
                .homepage("https://ioda.inetintel.cc.gatech.edu/")
                .attribution("IODA, Internet Outage Detection and Analysis, Georgia Tech")
                .licence(LICENSE)
                .termsUrl(TERMS_URL)
                .egress(Egress.LOCAL_ONLY)
                .mode(Mode.POLLING)
                .param(new ParamSpec(ENTITY_TYPES_PARAM, ParamType.STRING_LIST,
                        "Entity types kept: country, region (first-level region) and asn (network)",
                        DEFAULT_ENTITY_TYPES, false, IodaAlertsParser.ENTITY_TYPES))
                .param(new ParamSpec(DATASOURCES_PARAM, ParamType.STRING_LIST,
                        "Datasources kept: bgp (route visibility), ping-slash24 (active probing), gtr (Google"
                                + " Transparency Report) and merit-nt (network telescope)",
                        DEFAULT_DATASOURCES, false, IodaAlertsParser.DATASOURCES))
                .param(ParamSpec.optional(WINDOW_PARAM, ParamType.DURATION,
                        "How far back each poll reads alerts, 1 h to 24 h", "12h"))
                .param(new ParamSpec(COUNTRIES_PARAM, ParamType.STRING_LIST,
                        "ISO 3166-1 alpha-2 codes of the countries kept (country and region entities); empty keeps"
                                + " every country. Networks have no country and are dropped when this is set.",
                        null, false, List.of()))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(5))
                .defaultBudget(4, 1200)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(RECORD_TTL)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .kindRetention(KIND, RETENTION)
                .volatileFields(VOLATILE)
                .titleField("summary")
                .searchFields("entityName", "entityCode")
                .tileProperties("level", "datasource", "entityType", "ratio")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{summary}", "communications-tower", "#C2255C", null));

        string(builder, "entityType", "Entity type", "country, region or asn");
        string(builder, "entityCode", "Entity code", "ISO2 for a country, IODA's region id, or the AS number");
        string(builder, "entityName", "Entity", "Entity name as IODA writes it");
        string(builder, "datasource", "Datasource", "bgp, ping-slash24, gtr or merit-nt");
        builder.fieldSpec("level", new FieldSpec(FieldType.STRING, null, "Level",
                "critical while IODA's alert is raised, normal once cleared (warning possible)", null,
                Set.of()));
        string(builder, "condition", "Condition", "The alert threshold, for example < 0.99, or normal");
        builder.fieldSpec("value", new FieldSpec(FieldType.NUMBER, null, "Value",
                "The signal at the alert time (volatile)", null, Set.of()));
        builder.fieldSpec("historyValue", new FieldSpec(FieldType.NUMBER, null, "History",
                "The signal's historical baseline (volatile)", null, Set.of()));
        builder.fieldSpec("ratio", new FieldSpec(FieldType.NUMBER, "1", "Ratio",
                "value / historyValue to 3 decimal places, when the history is positive (volatile)", null, Set.of()));
        string(builder, "method", "Method", "Detection method, for example median or sarima");
        string(builder, "summary", "Summary", "{entityName}: {datasource} {level}");
        string(builder, "country", "Country", "ISO 3166-1 alpha-2 of a country, or of a region's country");
        builder.fieldSpec("geometryApproximate", new FieldSpec(FieldType.BOOLEAN, null, "Approximate geometry",
                "True: the geometry is a country or region label point, not a measured position", null, Set.of()));
        return builder.build();
    }

    private static void string(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
