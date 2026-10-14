package ai.foresignal.source.adapters.ucdp;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.adapters.conflict.ConflictSources;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
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
import ai.foresignal.source.support.AbsenceTombstoner;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ucdp-ged} (prompt 081): UCDP GED candidate events ({@link UcdpParser}).
 *
 * <ul>
 *   <li><strong>Auth:</strong> the required credential {@value #TOKEN}, sent as the header {@value #TOKEN_HEADER}
 *       (redacted from traces like every credential value). A 401 is {@link CredentialsRejectedException}.
 *   <li><strong>Version discovery</strong> (once a day, cached in state {@value #VERSION_KEY}): the monthly candidate
 *       releases are named {@code YY.0.M} (the docs name {@code 26.0.8}); {@code GET …/gedevents/{YY}.0.{M}?pagesize=1}
 *       is probed from the current month backwards, at most {@value #MAX_PROBES} months (crossing into the previous
 *       year), and the first version with {@code TotalCount > 0} is current. A probe that errors (other than a 429,
 *       which backs off) counts as empty. Param {@value #VERSION_PARAM} overrides discovery.
 *   <li><strong>Events:</strong> {@code GET …/gedevents/{version}?pagesize=1000&StartDate={today − lookbackDays}
 *       &EndDate={today}} (both filter on {@code date_end}), then every further page up to {@code TotalPages}
 *       (out-of-range pages error, so pages are never guessed: the first page's {@code NextPageUrl} tells which
 *       {@code page} number comes next, and the rest follow from it). At most {@value #MAX_PAGES} pages per run.
 *   <li><strong>Merge:</strong> an event keeps {@code externalId = id} across candidate versions, so a revision is a
 *       new version ({@code datasetVersion} is volatile). After a complete read, an event missing from two consecutive
 *       reads <em>inside the queried window</em> is tombstoned ({@link AbsenceTombstoner}); one whose {@code date_end}
 *       has left the window is forgotten, not tombstoned (state {@value #ENDS_KEY}).
 * </ul>
 *
 * <p>Annual GED backfill is out of scope; param {@value #ANNUAL_PARAM} is reserved (default false).
 */
@Component
public class UcdpGedAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ucdp-ged";

    /** The gedevents resource, with {@code {version}}. */
    public static final String EVENTS_URL = "https://ucdpapi.pcr.uu.se/api/gedevents/{version}";

    /** The token credential. */
    public static final String TOKEN = "token";

    /** The token header. */
    public static final String TOKEN_HEADER = "x-ucdp-access-token";

    /** The version override parameter. */
    public static final String VERSION_PARAM = "version";

    /** The window parameter, days. */
    public static final String LOOKBACK_PARAM = "lookbackDays";

    /** The reserved annual backfill parameter. */
    public static final String ANNUAL_PARAM = "includeAnnual";

    /** The default window, days. */
    public static final int DEFAULT_LOOKBACK_DAYS = 120;

    /** Events per page. */
    public static final int PAGE_SIZE = 1000;

    /** The most pages one run reads. */
    public static final int MAX_PAGES = 25;

    /** The most months probed by discovery. */
    public static final int MAX_PROBES = 14;

    /** How often the version is discovered again. */
    public static final Duration DISCOVERY_PERIOD = Duration.ofHours(24);

    /** The state key of the discovered version. */
    public static final String VERSION_KEY = "ucdp:version";

    /** The state key of when it was discovered (epoch seconds). */
    public static final String VERSION_AT_KEY = "ucdp:versionAt";

    /** The state key of the tracked events' {@code date_end}: {@code id=yyyy-MM-dd} lines, packed. */
    public static final String ENDS_KEY = "ucdp:ends";

    /** A UCDP version name. */
    static final Pattern VERSION = Pattern.compile("^[0-9]{1,2}(\\.[0-9]{1,4}){1,4}$");

    private final String eventsUrl;
    private final CountryCentroids countries;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value ConflictSources#BASE_URL_PROPERTY}; blank in production
     * @param countries the bundled country table, for country-level events
     */
    @Autowired
    public UcdpGedAdapter(@Value("${" + ConflictSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries) {
        this.eventsUrl = ConflictSources.uri(EVENTS_URL.replace("{version}", "VERSION"), baseUrlOverride).toString();
        this.countries = countries;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The gedevents URL of {@code version} with {@code query} (without the leading {@code ?}). */
    public URI eventsUri(String version, String query) {
        return URI.create(eventsUrl.replace("VERSION", version) + "?" + query);
    }

    /** The monthly candidate version of {@code month}: {@code YY.0.M}. */
    public static String candidateVersion(YearMonth month) {
        return (month.getYear() % 100) + ".0." + month.getMonthValue();
    }

    /** {@code version} must look like a UCDP version; {@code lookbackDays} must be 1 to 3650. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.has(VERSION_PARAM)) {
            String version = params.string(VERSION_PARAM);
            if (version == null || !VERSION.matcher(version.strip()).matches()) {
                problems.put(VERSION_PARAM, "must be a UCDP version such as 26.0.8 or 26.1, but was '" + version + "'");
            }
        }
        Integer lookback = params.integer(LOOKBACK_PARAM);
        if (lookback != null && (lookback < 1 || lookback > 3650)) {
            problems.put(LOOKBACK_PARAM, "must be 1 to 3650 days, but was " + lookback);
        }
        return problems;
    }

    /**
     * Discovers the version when due, reads the window's events and tombstones the ones gone.
     *
     * @throws CredentialsRejectedException when the token is missing or refused (401)
     * @throws AdapterException when a request fails otherwise
     * @throws UpstreamFormatException when no candidate release is found or a page does not parse
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String token = ConflictSources.trimmed(ctx.credentials().get(TOKEN));
        if (token == null) {
            throw new CredentialsRejectedException(ID + " needs the " + TOKEN + " credential (a UCDP API token)");
        }
        Map<String, String> headers = Map.of(TOKEN_HEADER, token);
        if (ctx.params().bool(ANNUAL_PARAM, false)) {
            ctx.log().info("{}: {} is reserved; the annual GED is not read", ID, ANNUAL_PARAM);
        }
        String version = version(ctx, headers);
        int lookback = ctx.params().integer(LOOKBACK_PARAM, DEFAULT_LOOKBACK_DAYS);
        LocalDate today = LocalDate.ofInstant(ctx.now(), ZoneOffset.UTC);
        LocalDate windowStart = today.minusDays(lookback);
        String query = "pagesize=" + PAGE_SIZE + "&StartDate=" + windowStart + "&EndDate=" + today;

        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, LocalDate> ends = new LinkedHashMap<>();
        Map<String, Integer> skipped = new TreeMap<>();
        UcdpParser.Page first = page(ctx, eventsUri(version, query), version, headers);
        collect(first, records, ends, skipped);
        int total = first.totalPages();
        int firstIndex = first.nextPage() == null ? 0 : first.nextPage() - 1;
        boolean complete = true;
        for (int n = 1; n < total; n++) {
            if (n >= MAX_PAGES) {
                complete = false;
                ctx.log().warn("{}: {} pages in the window; read the first {} (shorten {})", ID, total, MAX_PAGES,
                        LOOKBACK_PARAM);
                break;
            }
            collect(page(ctx, eventsUri(version, query + "&page=" + (firstIndex + n)), version, headers), records,
                    ends, skipped);
        }
        if (complete) {
            records.addAll(absence(ctx.state(), ends, windowStart, ctx.now()));
        }
        if (!skipped.isEmpty()) {
            ctx.log().debug("{}: skipped {}", ID, skipped);
        }
        return FetchResult.of(records);
    }

    /** The version to read: the parameter, the cached discovery, or a fresh discovery. */
    String version(FetchContext ctx, Map<String, String> headers) throws AdapterException {
        if (ctx.params().has(VERSION_PARAM)) {
            return ctx.params().string(VERSION_PARAM).strip();
        }
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        String cached = state.get(VERSION_KEY).orElse(null);
        if (cached != null && !due(state.get(VERSION_AT_KEY).orElse(null), now)) {
            return cached;
        }
        YearMonth month = YearMonth.from(LocalDate.ofInstant(now, ZoneOffset.UTC));
        for (int i = 0; i < MAX_PROBES; i++) {
            String candidate = candidateVersion(month.minusMonths(i));
            URI probe = eventsUri(candidate, "pagesize=1");
            long count;
            try {
                count = UcdpParser.totalCount(ConflictSources.body(ctx, ctx.http().get(probe, headers), null, probe));
            } catch (AdapterException e) {
                int status = e.httpStatus().orElse(0);
                if (status == 401) {
                    throw rejected(e);
                }
                if (status == 0 || status == 429) {
                    throw e;
                }
                ctx.log().debug("{}: probe of {} answered HTTP {}", ID, candidate, status);
                continue;
            } catch (UpstreamFormatException e) {
                ctx.log().debug("{}: probe of {} was unreadable: {}", ID, candidate, e.getMessage());
                continue;
            }
            if (count > 0) {
                state.put(VERSION_KEY, candidate);
                state.put(VERSION_AT_KEY, Long.toString(now.getEpochSecond()));
                if (!candidate.equals(cached)) {
                    ctx.log().info("{}: the current candidate release is {}", ID, candidate);
                }
                return candidate;
            }
        }
        if (cached != null) {
            ctx.log().warn("{}: no candidate release found in the last {} months; keeping {}", ID, MAX_PROBES, cached);
            return cached;
        }
        throw new UpstreamFormatException(ID + ": no UCDP candidate release with events in the last " + MAX_PROBES
                + " months (set the " + VERSION_PARAM + " parameter)", "");
    }

    private UcdpParser.Page page(FetchContext ctx, URI uri, String version, Map<String, String> headers)
            throws AdapterException {
        try {
            return UcdpParser.parse(ConflictSources.body(ctx, ctx.http().get(uri, headers), null, uri), version,
                    countries);
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) == 401) {
                throw rejected(e);
            }
            throw e;
        }
    }

    private static void collect(UcdpParser.Page page, List<NormalizedRecord> records, Map<String, LocalDate> ends,
            Map<String, Integer> skipped) {
        records.addAll(page.records());
        ends.putAll(page.dateEnds());
        page.skipped().forEach((k, v) -> skipped.merge(k, v, Integer::sum));
    }

    /**
     * The tombstones of the events missing twice inside the window; events whose {@code date_end} left the window are
     * forgotten. Updates {@value #ENDS_KEY}.
     */
    static List<NormalizedRecord> absence(AdapterStateStore state, Map<String, LocalDate> current,
            LocalDate windowStart, Instant now) {
        Map<String, LocalDate> known = readEnds(state);
        known.putAll(current);
        Set<String> forget = new HashSet<>();
        known.forEach((id, end) -> {
            if (end.isBefore(windowStart) && !current.containsKey(id)) {
                forget.add(id);
            }
        });
        List<String> gone = AbsenceTombstoner.update(state, null, AbsenceTombstoner.Outcome.COMPLETE,
                current.keySet(), forget);
        Set<String> tracked = new HashSet<>(current.keySet());
        tracked.addAll(AbsenceTombstoner.read(state, AbsenceTombstoner.MISSED_KEY));
        known.keySet().retainAll(tracked);
        writeEnds(state, known);
        return AbsenceTombstoner.tombstones(gone, UcdpParser.KIND, now);
    }

    static Map<String, LocalDate> readEnds(AdapterStateStore state) {
        Map<String, LocalDate> out = new TreeMap<>();
        String text = ConflictSources.unpack(state.get(ENDS_KEY).orElse(null));
        if (text == null) {
            return out;
        }
        for (String line : text.split("\n")) {
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            try {
                out.put(line.substring(0, eq), LocalDate.parse(line.substring(eq + 1)));
            } catch (DateTimeParseException e) {
                // a damaged line is dropped; that id is then tracked without a date until it is read again
            }
        }
        return out;
    }

    static void writeEnds(AdapterStateStore state, Map<String, LocalDate> ends) {
        if (ends.isEmpty()) {
            state.remove(ENDS_KEY);
            return;
        }
        StringBuilder text = new StringBuilder();
        new TreeMap<>(ends).forEach((id, end) -> text.append(id).append('=').append(end).append('\n'));
        state.put(ENDS_KEY, ConflictSources.pack(text.toString()));
    }

    private static boolean due(String at, Instant now) {
        try {
            return at == null || !Instant.ofEpochSecond(Long.parseLong(at.strip())).plus(DISCOVERY_PERIOD).isAfter(now);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static CredentialsRejectedException rejected(AdapterException e) {
        return new CredentialsRejectedException("UCDP refused the token (HTTP 401); check the " + TOKEN
                + " credential", e);
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = UcdpParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("UCDP GED candidate events")
                .description("Organized violence events from the UCDP Georeferenced Event Dataset candidate release"
                        + " (monthly, discovered daily; param version overrides), for the last lookbackDays (default"
                        + " 120). Candidate events are preliminary and may be revised or removed in later releases: a"
                        + " revision is a new version and an event missing from a newer release inside the window is"
                        + " retired. Needs a UCDP API token (issued on request by the API maintainer; 5,000 requests a"
                        + " day, errors included). The source headline is stored as untrusted text.")
                .kind(kind)
                .homepage("https://ucdp.uu.se")
                .attribution("UCDP Georeferenced Event Dataset (candidate), Uppsala Conflict Data Program")
                .licence("CC BY 4.0")
                .termsUrl("https://ucdp.uu.se/downloads/")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(TOKEN, "Your UCDP API token, sent as the x-ucdp-access-token"
                        + " header"))
                .param(ParamSpec.optional(VERSION_PARAM, ParamType.STRING, "A UCDP GED version to read instead of the"
                        + " discovered candidate release, for example 26.0.8", null))
                .param(ParamSpec.optional(LOOKBACK_PARAM, ParamType.INTEGER, "Days of events read (by date_end)",
                        DEFAULT_LOOKBACK_DAYS))
                .param(ParamSpec.optional(ANNUAL_PARAM, ParamType.BOOLEAN, "Reserved: annual GED backfill (not"
                        + " built)", false))
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(30, 300))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(ConflictSources.EVENT_RETENTION)
                .kindRetention(kind, ConflictSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .egress(Egress.OPEN)
                .volatileFields("datasetVersion")
                .titleField("conflictName")
                .tileProperties("violenceType", "deathsBest", "country")
                .searchFields("conflictName", "dyadName", "sideA", "sideB", "country", "adm1", "sourceHeadline")
                .embedFields("sourceHeadline")
                .rankField("deathsBest");
        ConflictSources.text(b, "conflictName", "Conflict", "The UCDP conflict name");
        ConflictSources.text(b, "dyadName", "Dyad", "The UCDP dyad name");
        ConflictSources.text(b, "sideA", "Side A", "Side A of the dyad");
        ConflictSources.text(b, "sideB", "Side B", "Side B of the dyad");
        ConflictSources.text(b, "violenceType", "Violence type", "state-based, non-state or one-sided");
        ConflictSources.text(b, "country", "Country", "The UCDP country name");
        ConflictSources.text(b, "region", "Region", "The UCDP region");
        ConflictSources.text(b, "adm1", "Admin 1", "The first-order administrative division");
        ConflictSources.text(b, "adm2", "Admin 2", "The second-order administrative division");
        ConflictSources.integer(b, "deathsBest", "Deaths (best)", "The best estimate of deaths");
        ConflictSources.integer(b, "deathsLow", "Deaths (low)", "The low estimate of deaths");
        ConflictSources.integer(b, "deathsHigh", "Deaths (high)", "The high estimate of deaths");
        ConflictSources.integer(b, "deathsCivilians", "Civilian deaths", "Civilian deaths");
        ConflictSources.integer(b, "sourceCount", "Sources", "The number of sources");
        ConflictSources.text(b, "sourceHeadline", "Source headline", "The source headline (untrusted text, at most"
                + " 1 KB)");
        ConflictSources.integer(b, "datePrecision", "Date precision", "1 exact day … 5 a year or longer");
        ConflictSources.integer(b, "wherePrecision", "Location precision", "1 exact … 6 country only, 7 waters or"
                + " airspace");
        ConflictSources.text(b, "datasetVersion", "Dataset version", "The candidate release read (volatile)");
        ConflictSources.bool(b, "geometryApproximate", "Approximate position", "True for a country-level event"
                + " (the country's label point) or one in international waters or airspace");
        b.kindDisplay(kind, new KindDisplay("{conflictName}", "danger", "#A61E4D", null));
        return b.build();
    }
}
