package ai.foresignal.source.adapters.cftc;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code cftc-cot} (prompt 117): weekly futures positioning from the CFTC Commitments of Traders, read from the
 * CFTC Public Reporting Environment (Socrata), as {@code series-point} and {@code series-latest} records.
 *
 * <p><strong>Incremental reads.</strong> Per report a run sends {@code GET /resource/{dataset}.json} with
 * {@code $where=report_date_as_yyyy_mm_dd > '{cursor}' AND cftc_contract_market_code in (…)},
 * {@code $order=report_date_as_yyyy_mm_dd,id}, {@code $limit=1000} and {@code $offset} paging (at most
 * {@value #MAX_PAGES_PER_REPORT} pages per report per run). The cursor is the newest report date read, kept per
 * report, dataset variant and contract set in adapter state, so a re-poll asks only for newer reports and never
 * duplicates a version; the first run reads the last {@value #FIRST_RUN_DAYS} days. The optional credential
 * {@code appToken} is sent as {@code X-App-Token}.
 *
 * <p><strong>History backfill.</strong> {@code POST /v1/sources/cftc-cot/backfill} writes a request
 * ({@value #BACKFILL_REQUEST_KEY} = {@code {jobId}|{from}}) into this source's adapter state. While it is open, each
 * run also pages backwards from the oldest report it has stored ({@code report_date < '{bound}' AND >= '{from}'},
 * newest first), at most {@value #BACKFILL_PAGES_PER_RUN} pages per run and never past the budget: a budget refusal
 * ends the run's backfill quietly. The bound is staged with the run's records, so an interrupted backfill resumes
 * where its last successful run stopped, and re-reading a report is idempotent (identical content makes no new
 * version). The adapter never reads the store (Architecture §3); its progress is in adapter state, which the
 * backfill service reads.
 *
 * <p>US government work, public domain; analytic class {@code series}.
 */
@Component
public class CftcCotAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "cftc-cot";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://publicreporting.cftc.gov";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cftc.base-url";

    /** The optional Socrata app token credential. */
    public static final String APP_TOKEN = "appToken";

    /** The reports parameter. */
    public static final String REPORTS_PARAM = "reports";

    /** The futures-only parameter. */
    public static final String FUTURES_ONLY_PARAM = "futuresOnly";

    /** The contracts parameter. */
    public static final String CONTRACTS_PARAM = "contracts";

    /** The default reports. */
    public static final List<String> DEFAULT_REPORTS = List.of("disaggregated", "legacy", "tff");

    /**
     * The default contracts, each confirmed under its expected name in the 2026-10-13 report: WTI crude, natural
     * gas, RBOB, ULSD (NYMEX); gold, copper (COMEX); wheat, corn (CBOT); E-mini S&amp;P 500; 10-year T-note; US
     * dollar index (ICE Futures U.S.); euro FX.
     */
    public static final List<String> DEFAULT_CONTRACTS = List.of("067651", "023651", "111659", "022651", "088691",
            "085692", "001602", "002602", "13874A", "043602", "098662", "099741");

    /** Socrata's page size for this source. */
    public static final int PAGE_SIZE = 1000;

    /** The most incremental pages per report and run. */
    public static final int MAX_PAGES_PER_REPORT = 3;

    /** The most backfill pages per run. */
    public static final int BACKFILL_PAGES_PER_RUN = 5;

    /** How far back the first incremental run reads. */
    public static final int FIRST_RUN_DAYS = 365;

    /** The adapter-state key of an open backfill request: {@code {jobId}|{from}}. */
    public static final String BACKFILL_REQUEST_KEY = "backfill.request";

    /** The adapter-state key holding the id of the last backfill job that reached its {@code from}. */
    public static final String BACKFILL_FINISHED_KEY = "backfill.finished";

    /** The adapter-state key counting the backfill pages read for the open job: {@code {jobId}|{pages}}. */
    public static final String BACKFILL_PAGES_KEY = "backfill.pages";

    /** The adapter-state key holding the oldest report date the backfill reached: {@code {jobId}|{date}}. */
    public static final String BACKFILL_OLDEST_KEY = "backfill.oldest";

    /** The adapter-state key of the last incremental read time (ISO instant). */
    public static final String INCREMENTAL_AT_KEY = "incremental.at";

    /** While a backfill is open, the incremental read is skipped if it ran less than this long ago. */
    static final Duration INCREMENTAL_MIN_GAP_DURING_BACKFILL = Duration.ofHours(1);

    /** A CFTC contract market code. */
    static final Pattern CONTRACT_CODE = Pattern.compile("^[0-9A-Z]{6}$");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public CftcCotAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        for (String report : params.stringList(REPORTS_PARAM)) {
            if (CotReport.fromWire(report) == null) {
                problems.put(REPORTS_PARAM, "'" + report + "' is not one of disaggregated, legacy, tff");
                break;
            }
        }
        for (String code : params.stringList(CONTRACTS_PARAM)) {
            if (!CONTRACT_CODE.matcher(code.strip().toUpperCase(Locale.ROOT)).matches()) {
                problems.put(CONTRACTS_PARAM, "'" + code + "' is not a six-character CFTC contract market code");
                break;
            }
        }
        return problems;
    }

    /** The reports a run reads, in a stable order. */
    static List<CotReport> reports(ValidatedParams params) {
        List<String> wanted = params == null || params.stringList(REPORTS_PARAM).isEmpty()
                ? DEFAULT_REPORTS : params.stringList(REPORTS_PARAM);
        Set<CotReport> out = new LinkedHashSet<>();
        for (String wire : wanted) {
            CotReport report = CotReport.fromWire(wire);
            if (report != null) {
                out.add(report);
            }
        }
        return List.copyOf(out);
    }

    /** The contract codes a run reads, upper case, sorted. */
    static Set<String> contracts(ValidatedParams params) {
        List<String> wanted = params == null || params.stringList(CONTRACTS_PARAM).isEmpty()
                ? DEFAULT_CONTRACTS : params.stringList(CONTRACTS_PARAM);
        Set<String> out = new TreeSet<>();
        for (String code : wanted) {
            String upper = code == null ? "" : code.strip().toUpperCase(Locale.ROOT);
            if (CONTRACT_CODE.matcher(upper).matches()) {
                out.add(upper);
            }
        }
        return out;
    }

    /** True unless the operator asked for the futures-and-options datasets. */
    static boolean futuresOnly(ValidatedParams params) {
        return params == null || params.bool(FUTURES_ONLY_PARAM, true);
    }

    /**
     * The state-key suffix of one report under the current dataset variant and contract set, so a changed contract
     * list or variant starts its own cursors instead of skipping the new contracts' history.
     */
    static String scope(CotReport report, boolean futuresOnly, Set<String> contracts) {
        return report.wire() + "." + (futuresOnly ? "f" : "c") + "."
                + Integer.toHexString(String.join(",", contracts).hashCode());
    }

    /** The incremental cursor key of a report scope. */
    static String cursorKey(String scope) {
        return "cursor." + scope;
    }

    /** The key of the oldest report date stored for a report scope. */
    static String oldestKey(String scope) {
        return "oldest." + scope;
    }

    /** The backfill bound key of a report scope (exclusive upper report date). */
    static String backfillKey(String scope) {
        return "backfill.bound." + scope;
    }

    /** The SoQL {@code in (…)} list of {@code contracts}. */
    static String inList(Set<String> contracts) {
        StringBuilder out = new StringBuilder("(");
        boolean first = true;
        for (String code : contracts) {
            if (!first) {
                out.append(',');
            }
            out.append('\'').append(code).append('\'');
            first = false;
        }
        return out.append(')').toString();
    }

    /** The incremental request of one page. */
    URI incrementalUri(String dataset, LocalDate cursor, Set<String> contracts, int page) {
        String where = "report_date_as_yyyy_mm_dd > '" + cursor + "' AND cftc_contract_market_code in "
                + inList(contracts);
        return URI.create(baseUrl + "/resource/" + dataset + ".json?$where=" + enc(where)
                + "&$order=" + enc("report_date_as_yyyy_mm_dd,id") + "&$limit=" + PAGE_SIZE
                + "&$offset=" + ((long) page * PAGE_SIZE));
    }

    /** The backfill request of one page: reports older than {@code bound} and not older than {@code from}. */
    URI backfillUri(String dataset, LocalDate bound, LocalDate from, Set<String> contracts) {
        String where = "report_date_as_yyyy_mm_dd < '" + bound + "' AND report_date_as_yyyy_mm_dd >= '" + from
                + "' AND cftc_contract_market_code in " + inList(contracts);
        return URI.create(baseUrl + "/resource/" + dataset + ".json?$where=" + enc(where)
                + "&$order=" + enc("report_date_as_yyyy_mm_dd DESC,id") + "&$limit=" + PAGE_SIZE + "&$offset=0");
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        List<CotReport> reports = reports(params);
        Set<String> contracts = contracts(params);
        boolean futuresOnly = futuresOnly(params);
        Map<String, String> headers = headers(ctx);
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        BackfillRequest backfill = BackfillRequest.parse(state.get(BACKFILL_REQUEST_KEY).orElse(null));
        boolean backfillOpen = backfill != null
                && !backfill.jobId().equals(state.get(BACKFILL_FINISHED_KEY).orElse(null));

        List<NormalizedRecord> incremental = new ArrayList<>();
        if (!backfillOpen || incrementalDue(state, now)) {
            for (CotReport report : reports) {
                incremental.addAll(readIncremental(ctx, report, futuresOnly, contracts, headers, now));
            }
            state.put(INCREMENTAL_AT_KEY, now.toString());
        }
        List<NormalizedRecord> out = new ArrayList<>(incremental);
        out.addAll(SeriesPoint.latestPerSeries(incremental,
                point -> CotParser.meta(point).withEgress(DESCRIPTOR.egress())));
        if (backfillOpen) {
            out.addAll(readBackfill(ctx, backfill, reports, futuresOnly, contracts, headers, now));
        }
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        return FetchResult.of(out);
    }

    private static boolean incrementalDue(AdapterStateStore state, Instant now) {
        Instant last = parseInstant(state.get(INCREMENTAL_AT_KEY).orElse(null));
        return last == null || !last.plus(INCREMENTAL_MIN_GAP_DURING_BACKFILL).isAfter(now);
    }

    private List<NormalizedRecord> readIncremental(FetchContext ctx, CotReport report, boolean futuresOnly,
            Set<String> contracts, Map<String, String> headers, Instant now) throws AdapterException {
        AdapterStateStore state = ctx.state();
        String scope = scope(report, futuresOnly, contracts);
        LocalDate cursor = parseDate(state.get(cursorKey(scope)).orElse(null));
        if (cursor == null) {
            cursor = now.atZone(ZoneOffset.UTC).toLocalDate().minusDays(FIRST_RUN_DAYS);
        }
        LocalDate newest = cursor;
        LocalDate oldest = parseDate(state.get(oldestKey(scope)).orElse(null));
        List<NormalizedRecord> out = new ArrayList<>();
        String dataset = report.dataset(futuresOnly);
        for (int page = 0; page < MAX_PAGES_PER_REPORT; page++) {
            JsonNode rows = read(ctx, incrementalUri(dataset, cursor, contracts, page), headers);
            out.addAll(CotParser.parse(report, rows, contracts, now));
            LocalDate pageNewest = CotParser.newest(rows);
            if (pageNewest != null && pageNewest.isAfter(newest)) {
                newest = pageNewest;
            }
            LocalDate pageOldest = CotParser.oldest(rows);
            if (pageOldest != null && (oldest == null || pageOldest.isBefore(oldest))) {
                oldest = pageOldest;
            }
            if (rows.size() < PAGE_SIZE) {
                // Read to the end: the cursor may move to the newest report.
                if (newest.isAfter(cursor)) {
                    state.put(cursorKey(scope), newest.toString());
                }
                break;
            }
            if (page == MAX_PAGES_PER_REPORT - 1) {
                // More pages remain; the cursor moves only to the day before the newest report read, so the next
                // run re-reads that report whole (idempotent) instead of skipping its unread rows.
                LocalDate safe = newest.minusDays(1);
                if (safe.isAfter(cursor)) {
                    state.put(cursorKey(scope), safe.toString());
                }
            }
        }
        if (oldest != null) {
            state.put(oldestKey(scope), oldest.toString());
        }
        return out;
    }

    private List<NormalizedRecord> readBackfill(FetchContext ctx, BackfillRequest request, List<CotReport> reports,
            boolean futuresOnly, Set<String> contracts, Map<String, String> headers, Instant now)
            throws AdapterException {
        AdapterStateStore state = ctx.state();
        List<NormalizedRecord> out = new ArrayList<>();
        int pagesThisRun = 0;
        int pagesTotal = counter(state.get(BACKFILL_PAGES_KEY).orElse(null), request.jobId());
        LocalDate reached = null;
        boolean allDone = true;
        for (CotReport report : reports) {
            String scope = scope(report, futuresOnly, contracts);
            LocalDate bound = parseDate(state.get(backfillKey(scope)).orElse(null));
            if (bound == null) {
                bound = parseDate(state.get(oldestKey(scope)).orElse(null));
            }
            if (bound == null) {
                bound = now.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1);
            }
            while (bound.isAfter(request.from()) && pagesThisRun < BACKFILL_PAGES_PER_RUN) {
                JsonNode rows;
                try {
                    rows = read(ctx, backfillUri(report.dataset(futuresOnly), bound, request.from(), contracts),
                            headers);
                } catch (BudgetExceededException e) {
                    // Budget-limited: the next run (or the backfill service's next tick) continues from the bound.
                    ctx.log().debug("{}: backfill paused, the budget is spent", ID);
                    pagesThisRun = BACKFILL_PAGES_PER_RUN;
                    break;
                }
                pagesThisRun++;
                pagesTotal++;
                out.addAll(CotParser.parse(report, rows, contracts, now));
                LocalDate pageOldest = CotParser.oldest(rows);
                if (rows.size() < PAGE_SIZE || pageOldest == null) {
                    bound = request.from();
                } else if (CotParser.distinctDates(rows) > 1) {
                    // Re-read the oldest report of a full page whole next time: its other rows may be on the next
                    // page. Identical content is no new version.
                    bound = pageOldest.plusDays(1);
                } else {
                    bound = pageOldest;
                }
                state.put(backfillKey(scope), bound.toString());
            }
            if (bound.isAfter(request.from())) {
                allDone = false;
            }
            if (reached == null || bound.isBefore(reached)) {
                reached = bound;
            }
        }
        state.put(BACKFILL_PAGES_KEY, request.jobId() + "|" + pagesTotal);
        if (reached != null) {
            state.put(BACKFILL_OLDEST_KEY, request.jobId() + "|" + reached);
        }
        if (allDone) {
            state.put(BACKFILL_FINISHED_KEY, request.jobId());
        }
        return out;
    }

    private JsonNode read(FetchContext ctx, URI uri, Map<String, String> headers) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri, headers);
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": the CFTC portal" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " response exceeds the cap"), "");
        }
        return CotParser.rows(response.body());
    }

    private static Map<String, String> headers(FetchContext ctx) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/json");
        String token = ctx.credentials() == null ? null : ctx.credentials().get(APP_TOKEN);
        if (token != null && !token.isBlank()) {
            headers.put("X-App-Token", token.strip());
        }
        return headers;
    }

    /** The count stored as {@code {jobId}|{n}} when it belongs to {@code jobId}, else 0. */
    public static int counter(String stored, String jobId) {
        if (stored == null) {
            return 0;
        }
        int bar = stored.indexOf('|');
        if (bar < 0 || !stored.substring(0, bar).equals(jobId)) {
            return 0;
        }
        try {
            return Integer.parseInt(stored.substring(bar + 1).strip());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Instant parseInstant(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text.strip());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * An open backfill request, as the backfill service writes it into adapter state.
     *
     * @param jobId the backfill job id
     * @param from the oldest report date to fetch
     */
    public record BackfillRequest(String jobId, LocalDate from) {

        /** The stored form {@code {jobId}|{from}}. */
        public String encode() {
            return jobId + "|" + from;
        }

        /** The request stored as {@code {jobId}|{from}}, or null when absent or malformed. */
        public static BackfillRequest parse(String stored) {
            if (stored == null) {
                return null;
            }
            int bar = stored.indexOf('|');
            if (bar <= 0) {
                return null;
            }
            LocalDate from = parseDate(stored.substring(bar + 1));
            return from == null ? null : new BackfillRequest(stored.substring(0, bar), from);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("CFTC Commitments of Traders")
                        .description("Weekly futures positioning from the CFTC Commitments of Traders (legacy,"
                                + " disaggregated and Traders in Financial Futures reports): open interest and the"
                                + " net positions of each trader category per contract, as series. Positions are"
                                + " as of Tuesday and published on Friday (a 3-day release lag). Keyless; an"
                                + " optional Socrata app token raises the rate limit. History can be backfilled with"
                                + " POST /v1/sources/cftc-cot/backfill.")
                        .homepage("https://publicreporting.cftc.gov/")
                        .attribution("U.S. Commodity Futures Trading Commission, Commitments of Traders")
                        .licence("Public domain (US government work)")
                        .termsUrl("https://www.cftc.gov/MarketReports/CommitmentsofTraders/index.htm")
                        .mode(Mode.POLLING)
                        .egress(Egress.OPEN)
                        .credential(CredentialField.optional(APP_TOKEN,
                                "Socrata app token (optional, free from publicreporting.cftc.gov), sent as"
                                        + " X-App-Token"))
                        .param(new ParamSpec(REPORTS_PARAM, ParamType.STRING_LIST,
                                "Reports to read: any of disaggregated, legacy, tff", DEFAULT_REPORTS, false,
                                List.of()))
                        .param(ParamSpec.optional(FUTURES_ONLY_PARAM, ParamType.BOOLEAN,
                                "Futures-only reports (true) or the futures-and-options combined reports (false)",
                                true))
                        .param(new ParamSpec(CONTRACTS_PARAM, ParamType.STRING_LIST,
                                "CFTC contract market codes (six characters)", DEFAULT_CONTRACTS, false, List.of()))
                        .interval(Duration.ofHours(1), Duration.ofHours(6))
                        .defaultBudget(Budget.of(10, 200))
                        .fetchTimeout(Duration.ofMinutes(3))
                        .observedAtMode(ObservedAtMode.RECEIVED))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
