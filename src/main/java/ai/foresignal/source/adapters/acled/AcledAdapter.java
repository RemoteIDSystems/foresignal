package ai.foresignal.source.adapters.acled;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
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
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code acled} (prompt 081): ACLED events for the operator's countries ({@link AcledParser}), with OAuth tokens held
 * in memory ({@link AcledTokens}).
 *
 * <ul>
 *   <li><strong>Events:</strong> {@code GET /api/acled/read?_format=json&limit=5000&page={n}&fields=…&iso={codes}}
 *       ({@code iso} is the numeric ISO 3166 list of param {@value #COUNTRIES_PARAM}, at most 30, joined with
 *       {@code |}). The first run reads an {@code event_date} window of {@value #LOOKBACK_PARAM} days
 *       ({@code event_date={from}|{today}&event_date_where=BETWEEN}); later runs read {@code timestamp={cursor}
 *       &timestamp_where=>=}. Pages run until one holds fewer than 5,000 rows (at most {@value #MAX_EVENT_PAGES}); the
 *       cursor (the largest {@code timestamp} seen, state {@value #CURSOR_KEY}) advances only after every page
 *       succeeded.
 *   <li><strong>Deletions:</strong> {@code GET /api/deleted/read?_format=json&deleted_timestamp={cursor}&limit=5000
 *       &page={n}} (the deleted endpoint filters on {@code deleted_timestamp}, default type {@code >=}); the ids of the
 *       configured countries (by their ISO alpha-3 prefix) become tombstones; cursor {@value #DELETED_CURSOR_KEY}.
 *   <li><strong>Auth:</strong> the required credentials {@value #EMAIL} and {@value #PASSWORD} (encrypted at rest);
 *       the bearer token is fetched with them and never stored. A 401 on a data request refreshes it once and
 *       retries. A 403 after a successful login means the account's access level does not allow the query: the
 *       descriptor's {@code accessDeniedIsTerminal} turns it into a refusal with that health reason and a one-hour
 *       backoff.
 * </ul>
 *
 * <p><strong>License:</strong> {@code egress: LOCAL_ONLY} (the EULA forbids redistributing the raw data).
 */
@Component
public class AcledAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "acled";

    /** The API base. */
    public static final String API_URL = "https://acleddata.com";

    /** The events path. */
    public static final String EVENTS_PATH = "/api/acled/read";

    /** The deletions path. */
    public static final String DELETED_PATH = "/api/deleted/read";

    /** The token path. */
    public static final String TOKEN_PATH = "/oauth/token";

    /** The email credential. */
    public static final String EMAIL = "email";

    /** The password credential. */
    public static final String PASSWORD = "password";

    /** The countries parameter (ISO 3166 numeric codes). */
    public static final String COUNTRIES_PARAM = "countries";

    /** The first-run window parameter, days. */
    public static final String LOOKBACK_PARAM = "lookbackDays";

    /** The default first-run window, days. */
    public static final int DEFAULT_LOOKBACK_DAYS = 30;

    /** The most countries one source reads. */
    public static final int MAX_COUNTRIES = 30;

    /** Rows per page. */
    public static final int PAGE_LIMIT = 5000;

    /** The most event pages one run reads. */
    public static final int MAX_EVENT_PAGES = 20;

    /** The most deletion pages one run reads. */
    public static final int MAX_DELETED_PAGES = 5;

    /** The state key of the event cursor (epoch seconds). */
    public static final String CURSOR_KEY = "acled:cursor";

    /** The state key of the deletion cursor (epoch seconds). */
    public static final String DELETED_CURSOR_KEY = "acled:deletedCursor";

    /** The requested columns, checked against the ACLED endpoint guide's Table 4. */
    public static final String FIELDS = "event_id_cnty|event_date|timestamp|disorder_type|event_type|sub_event_type"
            + "|actor1|actor2|interaction|civilian_targeting|country|iso|admin1|admin2|location|latitude|longitude"
            + "|geo_precision|time_precision|fatalities|source|notes";

    /** The health reason of an account whose access level does not allow the query. */
    public static final String ACCESS_LEVEL_REASON = "account access level does not allow this query";

    private static final Pattern ISO_NUMERIC = Pattern.compile("^[0-9]{1,3}$");

    private final String base;
    private final AcledTokens tokens;
    private final CountryCentroids countries;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value ConflictSources#BASE_URL_PROPERTY}; blank in production
     * @param countries the bundled country table (numeric to alpha-3 codes)
     */
    @Autowired
    public AcledAdapter(@Value("${" + ConflictSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries) {
        this.base = ConflictSources.uri(API_URL + "/", baseUrlOverride).toString().replaceAll("/+$", "");
        this.tokens = new AcledTokens(URI.create(base + TOKEN_PATH));
        this.countries = countries;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The token endpoint. */
    public URI tokenUri() {
        return tokens.tokenUri();
    }

    /** Forgets the tokens held for the source, as a restart does (tests). */
    void forgetTokens(String sourceId) {
        tokens.forget(sourceId);
    }

    /** True when tokens are held in memory for the source (tests). */
    boolean holdsTokens(String sourceId) {
        return tokens.holds(sourceId);
    }

    /** {@code countries} must hold 1 to 30 numeric ISO codes; {@code lookbackDays} must be 1 to 3650. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        List<String> codes = params.stringList(COUNTRIES_PARAM);
        if (codes == null || codes.isEmpty()) {
            problems.put(COUNTRIES_PARAM, "at least one ISO 3166 numeric country code is required (for example 804)");
        } else if (codes.size() > MAX_COUNTRIES) {
            problems.put(COUNTRIES_PARAM, "at most " + MAX_COUNTRIES + " countries, but " + codes.size() + " were given");
        } else {
            for (String code : codes) {
                if (code == null || !ISO_NUMERIC.matcher(code.strip()).matches()) {
                    problems.put(COUNTRIES_PARAM, "'" + code + "' is not an ISO 3166 numeric country code");
                    break;
                }
            }
        }
        Integer lookback = params.integer(LOOKBACK_PARAM);
        if (lookback != null && (lookback < 1 || lookback > 3650)) {
            problems.put(LOOKBACK_PARAM, "must be 1 to 3650 days, but was " + lookback);
        }
        return problems;
    }

    /**
     * Reads the new and changed events and the deletions since the cursors.
     *
     * @throws CredentialsRejectedException when the credentials are missing or the login is refused
     * @throws AdapterException when a request fails otherwise (the cursors stay where they were)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        String email = ConflictSources.trimmed(ctx.credentials().get(EMAIL));
        String password = ctx.credentials().get(PASSWORD);
        if (email == null || password == null || password.isEmpty()) {
            throw new CredentialsRejectedException(ID + " needs both the " + EMAIL + " and " + PASSWORD
                    + " credentials of a myACLED account");
        }
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        List<String> codes = codes(ctx.params());
        int lookback = ctx.params().integer(LOOKBACK_PARAM, DEFAULT_LOOKBACK_DAYS);
        Optional<Long> cursor = state.get(CURSOR_KEY).map(AcledAdapter::parseLong);

        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Long max = null;
        boolean all = false;
        for (int page = 1; page <= MAX_EVENT_PAGES; page++) {
            URI uri = eventsUri(codes, cursor.orElse(null), now, lookback, page);
            AcledParser.Page parsed = AcledParser.events(get(ctx, uri, email, password));
            records.addAll(parsed.records());
            parsed.skipped().forEach((k, v) -> skipped.merge(k, v, Integer::sum));
            if (parsed.maxTimestamp() != null && (max == null || parsed.maxTimestamp() > max)) {
                max = parsed.maxTimestamp();
            }
            if (parsed.rows() < PAGE_LIMIT) {
                all = true;
                break;
            }
        }

        Set<String> prefixes = alpha3(codes);
        long deletedFrom = state.get(DELETED_CURSOR_KEY).map(AcledAdapter::parseLong)
                .orElse(now.minus(Duration.ofDays(lookback)).getEpochSecond());
        Long deletedMax = null;
        boolean allDeleted = false;
        int deletions = 0;
        for (int page = 1; page <= MAX_DELETED_PAGES; page++) {
            AcledParser.Deleted parsed = AcledParser.deleted(get(ctx, deletedUri(deletedFrom, page), email, password));
            for (String id : parsed.ids()) {
                if (matches(id, prefixes)) {
                    records.add(NormalizedRecord.tombstone(id, AcledParser.KIND, now));
                    deletions++;
                }
            }
            if (parsed.maxDeletedTimestamp() != null && (deletedMax == null || parsed.maxDeletedTimestamp() > deletedMax)) {
                deletedMax = parsed.maxDeletedTimestamp();
            }
            if (parsed.rows() < PAGE_LIMIT) {
                allDeleted = true;
                break;
            }
        }

        if (all) {
            if (max != null) {
                state.put(CURSOR_KEY, Long.toString(max));
            } else if (cursor.isEmpty()) {
                state.put(CURSOR_KEY, Long.toString(now.minus(Duration.ofDays(lookback)).getEpochSecond()));
            }
        } else {
            ctx.log().warn("{}: more than {} pages of events; the cursor stays until a run reads them all (narrow"
                    + " the countries)", ID, MAX_EVENT_PAGES);
        }
        if (allDeleted) {
            state.put(DELETED_CURSOR_KEY, Long.toString(deletedMax != null ? deletedMax : deletedFrom));
        }
        ctx.log().debug("{}: {} event(s), {} deletion(s), skipped {}", ID, records.size() - deletions, deletions,
                skipped);
        return FetchResult.of(records);
    }

    /** The events URL of one page. */
    URI eventsUri(List<String> codes, Long cursor, Instant now, int lookbackDays, int page) {
        StringBuilder url = new StringBuilder(base).append(EVENTS_PATH).append("?_format=json&limit=").append(PAGE_LIMIT)
                .append("&page=").append(page);
        if (cursor == null) {
            LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
            url.append("&event_date=").append(today.minusDays(lookbackDays)).append("%7C").append(today)
                    .append("&event_date_where=BETWEEN");
        } else {
            url.append("&timestamp=").append(cursor).append("&timestamp_where=%3E%3D");
        }
        url.append("&fields=").append(FIELDS.replace("|", "%7C"))
                .append("&iso=").append(String.join("%7C", codes));
        return URI.create(url.toString());
    }

    /** The deletions URL of one page. */
    URI deletedUri(long from, int page) {
        return URI.create(base + DELETED_PATH + "?_format=json&deleted_timestamp=" + from + "&limit=" + PAGE_LIMIT
                + "&page=" + page);
    }

    /** One authenticated GET; a 401 refreshes the token once and retries. */
    private byte[] get(FetchContext ctx, URI uri, String email, String password) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(uri, bearer(tokens.accessToken(ctx, email, password)));
        } catch (AdapterException e) {
            if (e.httpStatus().orElse(0) != 401) {
                throw e;
            }
            tokens.invalidateAccess(ctx.sourceId());
            response = ctx.http().get(uri, bearer(tokens.accessToken(ctx, email, password)));
        }
        return ConflictSources.body(ctx, response, null, uri);
    }

    private static Map<String, String> bearer(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }

    /** The configured codes, trimmed, without leading zeros. */
    static List<String> codes(ValidatedParams params) {
        Set<String> out = new LinkedHashSet<>();
        List<String> raw = params.stringList(COUNTRIES_PARAM);
        if (raw != null) {
            for (String code : raw) {
                if (code != null && ISO_NUMERIC.matcher(code.strip()).matches()) {
                    out.add(Integer.toString(Integer.parseInt(code.strip())));
                }
            }
        }
        return List.copyOf(out);
    }

    /** The ISO alpha-3 codes of the numeric {@code codes} the bundled table knows. */
    Set<String> alpha3(List<String> codes) {
        Set<String> out = new LinkedHashSet<>();
        for (String code : codes) {
            String padded = String.format(Locale.ROOT, "%03d", Integer.parseInt(code));
            for (CountryRef country : countries.all()) {
                if (padded.equals(country.isoNumeric()) && country.iso3() != null) {
                    out.add(country.iso3().toUpperCase(Locale.ROOT));
                }
            }
        }
        return out;
    }

    /**
     * True when a deleted {@code event_id_cnty} belongs to the configured countries: its letters (the ISO alpha-3
     * code ACLED puts before the number) are among {@code prefixes}. With no known prefix every id matches.
     */
    static boolean matches(String id, Set<String> prefixes) {
        if (prefixes.isEmpty()) {
            return true;
        }
        int end = 0;
        while (end < id.length() && Character.isLetter(id.charAt(end))) {
            end++;
        }
        return prefixes.contains(id.substring(0, end).toUpperCase(Locale.ROOT));
    }

    private static Long parseLong(String text) {
        try {
            return Long.parseLong(text.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = AcledParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("ACLED conflict events")
                .description("Political violence, demonstration and strategic development events from the Armed"
                        + " Conflict Location & Event Data project (ACLED) for the countries you choose (param"
                        + " countries: ISO 3166 numeric codes, at most 30). Needs your own myACLED account (email and"
                        + " password, encrypted at rest; tokens are kept in memory only). What the account receives"
                        + " (event-level rows, recency) depends on its ACLED access level. The ACLED terms forbid"
                        + " redistributing raw data, so this source is LOCAL_ONLY: served to this instance's own"
                        + " clients and notifications, never re-served by sinks, federation, share links, feed keys or"
                        + " exports. Notes are ACLED's text, stored as untrusted text.")
                .kind(kind)
                .homepage("https://acleddata.com")
                .attribution("Armed Conflict Location & Event Data (ACLED), acleddata.com")
                .licence("ACLED Terms of Use: no redistribution of raw data")
                .termsUrl("https://acleddata.com/eula")
                .mode(Mode.POLLING)
                .credential(CredentialField.required(EMAIL, "The email address of your myACLED account"))
                .credential(CredentialField.required(PASSWORD, "The password of your myACLED account"))
                .param(new ParamSpec(COUNTRIES_PARAM, ParamType.STRING_LIST, "ISO 3166 numeric country codes (at most"
                        + " 30), for example 804 for Ukraine", null, true, List.of()))
                .param(ParamSpec.optional(LOOKBACK_PARAM, ParamType.INTEGER, "Days of events read on the first run",
                        DEFAULT_LOOKBACK_DAYS))
                .interval(Duration.ofHours(1), Duration.ofHours(6))
                .defaultBudget(Budget.of(30, 200))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(ConflictSources.EVENT_RETENTION)
                .kindRetention(kind, ConflictSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .egress(Egress.LOCAL_ONLY)
                .accessDeniedIsTerminal(true)
                .accessDeniedReason("Access denied", ACCESS_LEVEL_REASON)
                .accessDeniedReason("Consent must be accepted", "accept the ACLED consent on acleddata.com")
                .accessDeniedReason("fill in all the required fields", "complete the myACLED profile on acleddata.com")
                .titleField("location")
                .tileProperties("eventType", "subEventType", "fatalities", "country")
                .searchFields("eventType", "subEventType", "actor1", "actor2", "location", "admin1", "country",
                        "notes")
                .embedFields("notes")
                .rankField("fatalities");
        ConflictSources.text(b, "eventType", "Event type", "ACLED event type");
        ConflictSources.text(b, "subEventType", "Sub-event type", "ACLED sub-event type");
        ConflictSources.text(b, "disorderType", "Disorder type", "Political violence, Demonstrations or Strategic"
                + " developments");
        ConflictSources.text(b, "actor1", "Actor 1", "The first actor");
        ConflictSources.text(b, "actor2", "Actor 2", "The second actor");
        ConflictSources.text(b, "interaction", "Interaction", "The actor types, for example State forces-Civilians");
        ConflictSources.text(b, "civilianTargeting", "Civilian targeting", "Civilian targeting, when coded");
        ConflictSources.text(b, "country", "Country", "The country");
        ConflictSources.text(b, "admin1", "Admin 1", "The first-level administrative area");
        ConflictSources.text(b, "admin2", "Admin 2", "The second-level administrative area");
        ConflictSources.text(b, "location", "Location", "The named location");
        ConflictSources.integer(b, "fatalities", "Fatalities", "ACLED's reported fatalities");
        ConflictSources.text(b, "sources", "Sources", "The reporting sources (untrusted text)");
        ConflictSources.text(b, "notes", "Notes", "ACLED's event description (untrusted text, at most 2 KB)");
        ConflictSources.integer(b, "geoPrecision", "Geo precision", "1 town, 2 nearby area, 3 larger region");
        ConflictSources.integer(b, "timePrecision", "Time precision", "1 day, 2 week, 3 month");
        b.kindDisplay(kind, new KindDisplay("{eventType}: {location}", "danger", "#C92A2A", null));
        return b.build();
    }
}
