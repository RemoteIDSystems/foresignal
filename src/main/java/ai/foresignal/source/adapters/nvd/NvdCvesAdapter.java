package ai.foresignal.source.adapters.nvd;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
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
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * NVD CVEs with EPSS bands (prompt 076): {@code vulnerability} records from the NVD CVE API 2.0 and
 * {@code cve-epss} band records from the FIRST.org EPSS API, in one source.
 *
 * <p><strong>CVE phase:</strong> {@code GET https://services.nvd.nist.gov/rest/json/cves/2.0?lastModStartDate=
 * {cursor}&lastModEndDate={now}&resultsPerPage=2000&startIndex={k}}, the optional credential {@value #API_KEY} sent as
 * the {@code apiKey} header.
 * <ul>
 *   <li>The span from the cursor to now is split into windows of at most 120 days ({@link NvdWindows}); each window is
 *       paged with {@code startIndex} until {@code totalResults}.
 *   <li>The cursor ({@value #CURSOR_KEY}) moves to a window's end only after every page of that window succeeded.
 *       The first run starts {@value #BACKFILL_PARAM} days back (default 7, at most 120).
 *   <li>Without a key the adapter pauses {@value #DEFAULT_PAUSE_TEXT} between requests (NVD publishes 5 requests per
 *       30 s without a key, 50 with one, and asks automated clients to sync no more than once every two hours, hence
 *       the 2 h interval).
 *   <li>When a request fails after records were collected, the records are returned, the window's position is kept
 *       in {@value #WINDOW_KEY} and the next run resumes it one page early (an overlap that cannot lose a CVE when
 *       others leave the window), without moving the cursor. A failure before anything was collected fails the run.
 *   <li>Filter ({@link NvdParser.Filter}): {@value #MIN_CVSS_PARAM} (default 7.0, v4.0 base score when present,
 *       else v3.1) and {@value #VENDORS_PARAM} (CPE vendors); a CVE with {@code cisaExploitAdd} is always kept. A
 *       {@code Rejected} CVE is tombstoned (with its EPSS record); a tombstone for an entity never ingested is a
 *       no-op in the writer.
 * </ul>
 *
 * <p><strong>EPSS phase:</strong> {@code GET https://api.first.org/data/v1/epss?cve=CVE-a,CVE-b,…} with at most
 * {@value #EPSS_BATCH} ids per request and a URL under {@value #MAX_URL_LENGTH} characters, paced like the NVD
 * requests. After each CVE phase the CVEs kept in this run are scored; once a UTC day every CVE in the
 * {@link CveLedger} (ingested in the last {@value #EPSS_REFRESH_PARAM} days, default 30, at most 20,000) is
 * re-scored. A failed EPSS request stops the phase; an interrupted daily refresh resumes from
 * {@value #REFRESH_FROM_KEY} later the same day. Records are {@link EpssParser}'s bands: a score change inside a band
 * is a touch, a band change one version.
 *
 * <p>Both upstreams count against this source's budget (10/min, 600/day by default: 400 NVD requests and the
 * {@code ceil(20000/100)} = 200 EPSS calls of a full daily refresh). "This product uses the NVD API but is not
 * endorsed or certified by the NVD." "EPSS scores from FIRST.org (first.org/epss)."
 */
@Component
public class NvdCvesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nvd-cves";

    /** The optional credential, sent as the {@code apiKey} header. */
    public static final String API_KEY = "apiKey";

    /** The minimum-score parameter. */
    public static final String MIN_CVSS_PARAM = "minCvss";

    /** The CPE-vendor parameter. */
    public static final String VENDORS_PARAM = "vendors";

    /** The first-run backfill parameter. */
    public static final String BACKFILL_PARAM = "backfillDays";

    /** The EPSS refresh window parameter. */
    public static final String EPSS_REFRESH_PARAM = "epssRefreshDays";

    /** The default minimum score. */
    public static final double DEFAULT_MIN_CVSS = 7.0;

    /** The default EPSS refresh window, in days. */
    public static final int DEFAULT_EPSS_REFRESH_DAYS = 30;

    /** The NVD CVE API. */
    public static final String CVE_API = "https://services.nvd.nist.gov/rest/json/cves/2.0";

    /** The FIRST.org EPSS API. */
    public static final String EPSS_API = "https://api.first.org/data/v1/epss";

    /** The page size (the API's maximum). */
    public static final int PAGE_SIZE = 2000;

    /** The most ids per EPSS request. */
    public static final int EPSS_BATCH = 100;

    /** The longest EPSS request URL. */
    public static final int MAX_URL_LENGTH = 2000;

    /** The cursor state key. */
    public static final String CURSOR_KEY = "cursor:lastMod";

    /** The state key of a partly read window ({@code start|end|startIndex}). */
    public static final String WINDOW_KEY = "cursor:window";

    /** The state key of the last completed daily EPSS refresh (a UTC date). */
    public static final String REFRESHED_ON_KEY = "epss:refreshedOn";

    /** The state key of an interrupted daily EPSS refresh ({@code yyyy-MM-dd|offset}). */
    public static final String REFRESH_FROM_KEY = "epss:refreshFrom";

    /** The test-only property moving every cyber-source URL under one base as {@code {base}/{host}{path}}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cyber.base-url";

    /** The test-only property of the pause between requests without a key ({@value #DEFAULT_PAUSE_TEXT}). */
    public static final String PAUSE_PROPERTY = "foresignal.test.adapters.nvd.pause";

    /** The default pause between requests without a key. */
    public static final String DEFAULT_PAUSE_TEXT = "PT6S";

    /** The attribution notices the NVD and FIRST.org require. */
    public static final String ATTRIBUTION = "This product uses the NVD API but is not endorsed or certified by the"
            + " NVD. EPSS scores from FIRST.org (first.org/epss).";

    /** Raw retention: CVEs and their EPSS bands are the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** Waits between requests; replaceable in tests. */
    @FunctionalInterface
    public interface Pauser {

        /**
         * Waits {@code duration}.
         *
         * @param duration how long
         * @throws InterruptedException when interrupted
         */
        void pause(Duration duration) throws InterruptedException;
    }

    private final String baseUrlOverride;
    private final Duration pause;
    private final Pauser pauser;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param pause the pause between requests without a key (property {@value #PAUSE_PROPERTY})
     */
    @Autowired
    public NvdCvesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${" + PAUSE_PROPERTY + ":" + DEFAULT_PAUSE_TEXT + "}") Duration pause) {
        this(baseUrlOverride, pause, d -> Thread.sleep(d.toMillis()));
    }

    /**
     * Creates the adapter with a chosen pauser (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param pause the pause between requests without a key
     * @param pauser waits; records the pauses in tests
     */
    public NvdCvesAdapter(String baseUrlOverride, Duration pause, Pauser pauser) {
        this.baseUrlOverride = baseUrlOverride;
        this.pause = pause == null ? Duration.ofSeconds(6) : pause;
        this.pauser = pauser;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code minCvss} 0–10, {@code backfillDays} 1–120, {@code epssRefreshDays} 1–365. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.has(MIN_CVSS_PARAM)) {
            Double min = params.number(MIN_CVSS_PARAM);
            if (min == null || min < 0 || min > 10) {
                problems.put(MIN_CVSS_PARAM, "must be between 0 and 10, but was " + min);
            }
        }
        if (params.has(BACKFILL_PARAM)) {
            Integer days = params.integer(BACKFILL_PARAM);
            if (days == null || days < 1 || days > NvdWindows.MAX_BACKFILL_DAYS) {
                problems.put(BACKFILL_PARAM, "must be between 1 and " + NvdWindows.MAX_BACKFILL_DAYS + ", but was "
                        + days);
            }
        }
        if (params.has(EPSS_REFRESH_PARAM)) {
            Integer days = params.integer(EPSS_REFRESH_PARAM);
            if (days == null || days < 1 || days > 365) {
                problems.put(EPSS_REFRESH_PARAM, "must be between 1 and 365, but was " + days);
            }
        }
        return problems;
    }

    /**
     * The URL of one CVE page.
     *
     * @param window the last-modified window
     * @param startIndex the first result
     */
    public URI cveUri(NvdWindows.Window window, int startIndex) {
        return URI.create(KitUrls.rebased(CVE_API + "?lastModStartDate=" + window.startParam() + "&lastModEndDate="
                + window.endParam() + "&resultsPerPage=" + PAGE_SIZE + "&startIndex=" + startIndex, baseUrlOverride));
    }

    /**
     * The URL of one EPSS request.
     *
     * @param ids the CVE ids
     */
    public URI epssUri(List<String> ids) {
        return URI.create(KitUrls.rebased(EPSS_API + "?cve=" + String.join(",", ids), baseUrlOverride));
    }

    /**
     * {@code ids} split into EPSS requests of at most {@value #EPSS_BATCH} ids whose production URL stays under
     * {@value #MAX_URL_LENGTH} characters.
     *
     * @param ids the CVE ids, in order
     */
    public static List<List<String>> epssBatches(List<String> ids) {
        List<List<String>> out = new ArrayList<>();
        List<String> batch = new ArrayList<>();
        int length = (EPSS_API + "?cve=").length();
        for (String id : ids) {
            int added = id.length() + (batch.isEmpty() ? 0 : 1);
            if (!batch.isEmpty() && (batch.size() >= EPSS_BATCH || length + added >= MAX_URL_LENGTH)) {
                out.add(List.copyOf(batch));
                batch.clear();
                length = (EPSS_API + "?cve=").length();
                added = id.length();
            }
            batch.add(id);
            length += added;
        }
        if (!batch.isEmpty()) {
            out.add(List.copyOf(batch));
        }
        return out;
    }

    /**
     * One run: the CVE phase, then the EPSS phase (see the class note).
     *
     * @throws AdapterException when the first request of the run fails
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now().truncatedTo(ChronoUnit.MILLIS);
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        if (key != null && !key.isBlank()) {
            headers.put("apiKey", key.strip());
        }
        Duration gap = headers.containsKey("apiKey") ? Duration.ZERO : pause;
        Run run = new Run(ctx, headers, gap);

        NvdParser.Filter filter = filter(ctx.params());
        run.cvePhase(now, filter, ctx.params() != null && ctx.params().has(BACKFILL_PARAM)
                ? ctx.params().integer(BACKFILL_PARAM) : null);

        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        int refreshDays = ctx.params() != null && ctx.params().has(EPSS_REFRESH_PARAM)
                ? ctx.params().integer(EPSS_REFRESH_PARAM, DEFAULT_EPSS_REFRESH_DAYS) : DEFAULT_EPSS_REFRESH_DAYS;
        CveLedger ledger = CveLedger.load(ctx.state());
        for (String id : run.kept) {
            ledger.ingested(id, today);
        }
        for (String id : run.rejected) {
            ledger.remove(id);
        }
        ledger.prune(today.minusDays(Math.max(1, refreshDays)));
        ledger.save(ctx.state());
        run.epssPhase(today, ledger);
        List<NormalizedRecord> out = new ArrayList<>(run.cves.values());
        out.addAll(run.records);

        ctx.log().debug("{}: {} record(s) from {} request(s); kept {}, rejected {}, skipped {}", ctx.sourceId(),
                out.size(), run.requests, run.kept.size(), run.rejected.size(), run.skipped);
        return FetchResult.of(out);
    }

    /** The filter from the parameters. */
    static NvdParser.Filter filter(ValidatedParams params) {
        double min = DEFAULT_MIN_CVSS;
        Set<String> vendors = Set.of();
        if (params != null) {
            if (params.has(MIN_CVSS_PARAM) && params.number(MIN_CVSS_PARAM) != null) {
                min = params.number(MIN_CVSS_PARAM);
            }
            if (params.has(VENDORS_PARAM) && params.stringList(VENDORS_PARAM) != null) {
                vendors = new LinkedHashSet<>(params.stringList(VENDORS_PARAM));
            }
        }
        return new NvdParser.Filter(min, vendors);
    }

    /** The state of one run. */
    private final class Run {

        private final FetchContext ctx;
        private final Map<String, String> headers;
        private final Duration gap;
        private final Map<String, NormalizedRecord> cves = new LinkedHashMap<>();
        private final List<NormalizedRecord> records = new ArrayList<>();
        private final Set<String> kept = new TreeSet<>();
        private final Set<String> rejected = new TreeSet<>();
        private final Map<String, Integer> skipped = new TreeMap<>();
        private int requests;

        Run(FetchContext ctx, Map<String, String> headers, Duration gap) {
            this.ctx = ctx;
            this.headers = headers;
            this.gap = gap;
        }

        /** One budgeted GET, paced after the first request of the run. */
        private UpstreamResponse get(URI uri, Duration pace) throws AdapterException {
            if (requests > 0 && !pace.isZero()) {
                try {
                    pauser.pause(pace);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AdapterException(ID + ": interrupted between requests", e);
                }
            }
            requests++;
            UpstreamResponse response = ctx.http().get(uri, headers);
            if (response.status() != 200 || response.truncated()) {
                throw new UpstreamFormatException(ID + (response.status() != 200 ? ": " + uri.getHost()
                        + " answered HTTP " + response.status() : ": a response exceeds the cap"), "");
            }
            return response;
        }

        void cvePhase(Instant now, NvdParser.Filter filter, Integer backfillDays) throws AdapterException {
            AdapterStateStore state = ctx.state();
            Instant cursor = parseInstant(state.get(CURSOR_KEY).orElse(null));
            if (cursor == null) {
                cursor = NvdWindows.initialCursor(now, backfillDays);
            }
            Resume resume = Resume.parse(state.get(WINDOW_KEY).orElse(null));
            List<NvdWindows.Window> windows = new ArrayList<>();
            if (resume != null && resume.window().start().equals(cursor) && !resume.window().end().isAfter(now)) {
                windows.add(resume.window());
                windows.addAll(NvdWindows.split(resume.window().end(), now));
            } else {
                resume = null;
                state.remove(WINDOW_KEY);
                windows.addAll(NvdWindows.split(cursor, now));
            }
            for (int w = 0; w < windows.size(); w++) {
                NvdWindows.Window window = windows.get(w);
                int startIndex = w == 0 && resume != null ? Math.max(0, resume.startIndex() - PAGE_SIZE) : 0;
                while (true) {
                    NvdParser.Page page;
                    try {
                        page = NvdParser.parse(get(cveUri(window, startIndex), gap).body(), filter);
                    } catch (AdapterException | UpstreamFormatException e) {
                        if (cves.isEmpty()) {
                            throw e;
                        }
                        state.put(WINDOW_KEY, new Resume(window, startIndex).format());
                        ctx.log().warn("{}: stopped at {} of the window {}..{} ({}); the next run resumes it",
                                ctx.sourceId(), startIndex, window.startParam(), window.endParam(), e.getMessage());
                        return;
                    }
                    accept(page);
                    startIndex += page.count();
                    if (page.count() == 0 || startIndex >= page.totalResults()) {
                        break;
                    }
                }
                state.put(CURSOR_KEY, window.end().toString());
                state.remove(WINDOW_KEY);
            }
        }

        /** Keeps the last state of each CVE seen in this run (a resumed window re-reads one page). */
        private void accept(NvdParser.Page page) {
            for (NormalizedRecord r : page.records()) {
                cves.remove(EpssParser.ID_PREFIX + r.externalId());
                cves.put(r.externalId(), r);
                kept.add(r.externalId());
                rejected.remove(r.externalId());
            }
            for (String id : page.rejected()) {
                rejected.add(id);
                kept.remove(id);
                cves.put(id, NormalizedRecord.tombstone(id, NvdParser.KIND, ctx.now()));
                cves.put(EpssParser.ID_PREFIX + id, NormalizedRecord.tombstone(EpssParser.ID_PREFIX + id,
                        EpssParser.KIND, ctx.now()));
            }
            page.skipped().forEach((reason, n) -> skipped.merge(reason, n, Integer::sum));
        }

        void epssPhase(LocalDate today, CveLedger ledger) {
            AdapterStateStore state = ctx.state();
            boolean refresh = !today.toString().equals(state.get(REFRESHED_ON_KEY).orElse(null));
            List<String> ids;
            int offset = 0;
            if (refresh) {
                ids = ledger.ids();
                String from = state.get(REFRESH_FROM_KEY).orElse("");
                if (from.startsWith(today + "|")) {
                    try {
                        offset = Math.max(0, Integer.parseInt(from.substring(from.indexOf('|') + 1)));
                    } catch (NumberFormatException e) {
                        offset = 0;
                    }
                }
                if (offset > 0) {
                    // A refresh that resumes still scores this run's new CVEs, which may sort before the offset.
                    Set<String> rest = new LinkedHashSet<>(kept);
                    rest.addAll(ids.subList(Math.min(offset, ids.size()), ids.size()));
                    ids = List.copyOf(rest);
                }
            } else {
                ids = List.copyOf(kept);
            }
            int done = offset;
            int missing = 0;
            for (List<String> batch : epssBatches(ids)) {
                EpssParser.Result result;
                try {
                    result = EpssParser.parse(get(epssUri(batch), pause).body(), batch);
                } catch (AdapterException | UpstreamFormatException e) {
                    if (refresh) {
                        state.put(REFRESH_FROM_KEY, today + "|" + done);
                    }
                    ctx.log().warn("{}: EPSS stopped after {} id(s) ({}); {}", ctx.sourceId(), done, e.getMessage(),
                            refresh ? "the daily refresh resumes on the next run" : "the daily refresh will score"
                                    + " the rest");
                    return;
                }
                records.addAll(result.records());
                missing += result.missing().size();
                done += batch.size();
            }
            if (missing > 0) {
                skipped.merge("epss-missing", missing, Integer::sum);
            }
            if (refresh) {
                state.put(REFRESHED_ON_KEY, today.toString());
                state.remove(REFRESH_FROM_KEY);
            }
        }
    }

    /** A partly read window and the position to resume at. */
    record Resume(NvdWindows.Window window, int startIndex) {

        String format() {
            return window.start() + "|" + window.end() + "|" + startIndex;
        }

        static Resume parse(String value) {
            if (value == null) {
                return null;
            }
            String[] parts = value.split("\\|");
            if (parts.length != 3) {
                return null;
            }
            Instant start = parseInstant(parts[0]);
            Instant end = parseInstant(parts[1]);
            try {
                int index = Integer.parseInt(parts[2]);
                return start == null || end == null || !end.isAfter(start) || index < 0 ? null
                        : new Resume(new NvdWindows.Window(start, end), index);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String vuln = NvdParser.KIND;
        String epss = EpssParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NVD CVEs with EPSS bands")
                .description("CVEs from the NIST National Vulnerability Database (CVE API 2.0), synced incrementally"
                        + " by last-modified date and filtered by CVSS score (v4.0 when present, else v3.1) and CPE"
                        + " vendor; CVEs in the CISA KEV catalog are always kept. Each kept CVE also gets a cve-epss"
                        + " record holding its FIRST.org EPSS exploit probability and percentile as bands only"
                        + " (refreshed daily for 30 days). No geometry. Descriptions are upstream text. "
                        + ATTRIBUTION)
                .kinds(vuln, epss)
                .homepage("https://nvd.nist.gov/developers/vulnerabilities")
                .attribution(ATTRIBUTION)
                .licence("NVD: US Government public domain; EPSS: free to use with attribution to FIRST.org"
                        + " (first.org/epss)")
                .termsUrl("https://nvd.nist.gov/developers/terms-of-use")
                .mode(Mode.POLLING)
                .credential(CredentialField.optional(API_KEY, "NVD API key (free, nvd.nist.gov/developers/request-an"
                        + "-api-key), sent as the apiKey header; without it requests are paced 6 s apart"))
                .interval(Duration.ofHours(2), Duration.ofHours(2))
                .defaultBudget(Budget.of(10, 600))
                .maxResponseBytes(32 * 1024 * 1024)
                .fetchTimeout(Duration.ofMinutes(30))
                .zeroResultsNormal(true)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(vuln, RETENTION)
                .kindRetention(epss, RETENTION)
                .titleField("cveId")
                .searchFields("cveId", "description", "vendors", "products")
                .embedFields("description")
                .rankField("cvssScore")
                .volatileFields("modelDate")
                .param(ParamSpec.optional(MIN_CVSS_PARAM, ParamType.NUMBER, "The lowest CVSS base score kept (v4.0"
                        + " when present, else v3.1); KEV CVEs are always kept", DEFAULT_MIN_CVSS))
                .param(ParamSpec.optional(VENDORS_PARAM, ParamType.STRING_LIST, "Keep only CVEs whose CPE vendor is"
                        + " one of these (for example microsoft, cisco); empty keeps every vendor", null))
                .param(ParamSpec.optional(BACKFILL_PARAM, ParamType.INTEGER, "How many days back the first run"
                        + " starts (1-120)", NvdWindows.DEFAULT_BACKFILL_DAYS))
                .param(ParamSpec.optional(EPSS_REFRESH_PARAM, ParamType.INTEGER, "How many days a CVE's EPSS bands"
                        + " are refreshed daily after it was last ingested (1-365; at most 20,000 CVEs)",
                        DEFAULT_EPSS_REFRESH_DAYS));
        Set<String> v = Set.of(vuln);
        Set<String> e = Set.of(epss);
        b.fieldSpec("cveId", new FieldSpec(FieldType.STRING, null, "CVE", "The CVE id", null, Set.of()));
        text(b, v, "vulnStatus", "Status", "NVD's status (Analyzed, Modified, Deferred, Received, ...)");
        text(b, v, "description", "Description", "The English description, at most 4 KB; upstream text, untrusted");
        b.fieldSpec("cvssV40", new FieldSpec(FieldType.OBJECT, null, "CVSS 4.0", "baseScore, baseSeverity and"
                + " vectorString of NVD's (Primary) CVSS 4.0 metric, else the first", null, v));
        b.fieldSpec("cvssV31", new FieldSpec(FieldType.OBJECT, null, "CVSS 3.1", "baseScore, baseSeverity and"
                + " vectorString of NVD's (Primary) CVSS 3.1 metric, else the first", null, v));
        b.fieldSpec("cvssScore", new FieldSpec(FieldType.NUMBER, "1", "CVSS score", "The score filtered and ranked"
                + " on: the CVSS 4.0 base score when present, else 3.1", null, v));
        b.fieldSpec("cwes", new FieldSpec(FieldType.ARRAY, null, "CWEs", "CWE ids, at most 10", null, v));
        b.fieldSpec("vendors", new FieldSpec(FieldType.ARRAY, null, "Vendors", "CPE vendors of the vulnerable"
                + " configurations, at most 20", null, v));
        b.fieldSpec("products", new FieldSpec(FieldType.ARRAY, null, "Products", "CPE products of the vulnerable"
                + " configurations, at most 20", null, v));
        b.fieldSpec("references", new FieldSpec(FieldType.ARRAY, null, "References", "Reference URLs, at most 10;"
                + " upstream links, never fetched", null, v));
        b.fieldSpec("kev", new FieldSpec(FieldType.BOOLEAN, null, "In KEV", "True when the NVD record carries CISA"
                + " KEV data (cisaExploitAdd)", null, v));
        text(b, v, "cisaActionDue", "CISA due date", "The KEV remediation due date (yyyy-MM-dd)");
        text(b, v, "cisaRequiredAction", "CISA required action", "The KEV required action; upstream text, untrusted");
        text(b, e, "epssBand", "EPSS band", "very-low (< 0.01), low (< 0.05), medium (< 0.2), high (< 0.5) or"
                + " very-high; the raw score is not stored");
        text(b, e, "percentileBand", "EPSS percentile band", "<p50, p50, p90, p95 or p99; the raw percentile is not"
                + " stored");
        text(b, e, "modelDate", "EPSS model date", "The EPSS model date (yyyy-MM-dd); volatile");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, Set<String> kinds, String key, String label,
            String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, kinds));
    }
}
