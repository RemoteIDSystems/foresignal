package ai.foresignal.source.adapters.fred;

import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.CredentialsRejectedException;
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
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code fred-series} (prompt 117): rates and prices from the Federal Reserve Bank of St. Louis FRED API as
 * {@code series-point} and {@code series-latest} records.
 *
 * <p><strong>Licensed data stays local.</strong> The whole source is {@code egress=LOCAL_ONLY}: {@code EgressPolicy}
 * refuses its raw values on every path but the instance's own callers allowed local egress. FRED serves third-party
 * series whose owners keep their copyright; a series whose metadata {@code notes} contain "copyright" or "reprinted
 * with permission" is <strong>skipped</strong>, with a health warning, unless the operator lists it in
 * {@code allowCopyrighted} (personal use).
 *
 * <p>The metadata ({@code /fred/series}, plus {@code /fred/series/release} and {@code /fred/release/sources} for the
 * series' source line) is read when a series is first seen and weekly after that, and kept in adapter state.
 * Observations are read with {@code observation_start} = the last stored period − 14 days (the last 365 days on the
 * first run), so a revised value becomes a new version; the value {@code "."} means missing and makes no record.
 * {@code seriesId = fred.{id}}; family from units (percent → rate, dollars per barrel or MMBtu → price, otherwise
 * index); release lag 1 day. Every {@code series-latest} carries the attribution FRED's terms require: "This product
 * uses the FRED® API but is not endorsed or certified by the Federal Reserve Bank of St. Louis." plus the series'
 * own source line.
 */
@Component
public class FredSeriesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "fred-series";

    /** The production API host. */
    public static final String DEFAULT_BASE_URL = "https://api.stlouisfed.org";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.fred.base-url";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The series parameter. */
    public static final String SERIES_PARAM = "series";

    /** The copyrighted series the operator accepts for personal use. */
    public static final String ALLOW_COPYRIGHTED_PARAM = "allowCopyrighted";

    /** The default series. */
    public static final List<String> DEFAULT_SERIES = List.of("DGS10", "DGS2", "T10Y2Y", "DFF", "DTWEXBGS",
            "DCOILWTICO", "DHHNGSP");

    /** How often the metadata is re-read. */
    public static final Duration METADATA_REFRESH = Duration.ofDays(7);

    /** The revision window re-read before the last stored period. */
    public static final int REVISION_DAYS = 14;

    /** The first run's window. */
    public static final int FIRST_RUN_DAYS = 365;

    /** A FRED series id. */
    static final Pattern SERIES_ID = Pattern.compile("^[A-Z0-9_.]{1,40}$");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public FredSeriesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        for (String name : List.of(SERIES_PARAM, ALLOW_COPYRIGHTED_PARAM)) {
            for (String id : params.stringList(name)) {
                if (!SERIES_ID.matcher(id.strip().toUpperCase(Locale.ROOT)).matches()) {
                    problems.put(name, "'" + id + "' is not a FRED series id");
                    break;
                }
            }
        }
        return problems;
    }

    /** The series a run reads, upper case, in the configured order. */
    static List<String> series(ValidatedParams params) {
        List<String> wanted = params == null || params.stringList(SERIES_PARAM).isEmpty()
                ? DEFAULT_SERIES : params.stringList(SERIES_PARAM);
        return upper(wanted);
    }

    /** The copyrighted series the operator accepts. */
    static Set<String> allowed(ValidatedParams params) {
        return params == null ? Set.of() : new LinkedHashSet<>(upper(params.stringList(ALLOW_COPYRIGHTED_PARAM)));
    }

    private static List<String> upper(List<String> ids) {
        Set<String> out = new LinkedHashSet<>();
        for (String id : ids) {
            String upper = id == null ? "" : id.strip().toUpperCase(Locale.ROOT);
            if (SERIES_ID.matcher(upper).matches()) {
                out.add(upper);
            }
        }
        return List.copyOf(out);
    }

    /** {@code {base}/fred/{path}?{query}&api_key={key}&file_type=json}. */
    URI uri(String path, String query, String key) {
        return URI.create(baseUrl + "/fred/" + path + "?" + query + "&api_key=" + enc(key) + "&file_type=json");
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a free key from"
                    + " fred.stlouisfed.org)");
        }
        key = key.strip();
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        Set<String> allowed = allowed(ctx.params());
        List<String> skipped = new ArrayList<>();
        List<NormalizedRecord> out = new ArrayList<>();
        for (String id : series(ctx.params())) {
            FredParser.Stored meta = metadata(ctx, id, key, now);
            if (meta.copyrighted() && !allowed.contains(id)) {
                skipped.add(id);
                continue;
            }
            LocalDate last = parseDate(state.get(lastKey(id)).orElse(null));
            LocalDate start = last == null
                    ? now.atZone(ZoneOffset.UTC).toLocalDate().minusDays(FIRST_RUN_DAYS)
                    : last.minusDays(REVISION_DAYS);
            byte[] body = get(ctx, uri("series/observations", "series_id=" + enc(id) + "&observation_start="
                    + start, key));
            List<NormalizedRecord> points = FredParser.observations(meta.info(), body, now);
            out.addAll(points);
            LocalDate newest = newestPeriod(points);
            if (newest != null && (last == null || newest.isAfter(last))) {
                state.put(lastKey(id), newest.toString());
            }
            String attribution = FredParser.attribution(meta.sourceLine());
            SeriesMeta seriesMeta = FredParser.meta(meta.info().units()).withEgress(DESCRIPTOR.egress())
                    .withRegion("US");
            for (NormalizedRecord latest : SeriesPoint.latestPerSeries(points, p -> seriesMeta)) {
                out.add(SeriesPoint.withPayload(latest, "attribution", attribution));
            }
        }
        if (skipped.isEmpty()) {
            state.clearDegraded();
        } else {
            state.degrade("FRED series skipped because their notes mark them as copyrighted (add them to"
                    + " allowCopyrighted to accept them for personal use): " + String.join(", ", skipped));
        }
        ctx.log().debug("{}: {} record(s), {} copyrighted series skipped", ID, out.size(), skipped.size());
        return FetchResult.of(out);
    }

    /** The stored metadata of {@code id}, re-read when missing or older than {@link #METADATA_REFRESH}. */
    private FredParser.Stored metadata(FetchContext ctx, String id, String key, Instant now)
            throws AdapterException {
        AdapterStateStore state = ctx.state();
        FredParser.Stored stored = FredParser.decode(state.get(metaKey(id)).orElse(null));
        if (stored != null && stored.checkedAt() != null && stored.checkedAt().plus(METADATA_REFRESH).isAfter(now)
                && stored.info().id() != null) {
            return stored;
        }
        FredParser.SeriesInfo info = FredParser.series(get(ctx, uri("series", "series_id=" + enc(id), key)));
        String sourceLine;
        try {
            byte[] release = get(ctx, uri("series/release", "series_id=" + enc(id), key));
            Long releaseId = FredParser.releaseId(release);
            String sources = releaseId == null ? null : FredParser.sourceNames(
                    get(ctx, uri("release/sources", "release_id=" + releaseId, key)));
            sourceLine = FredParser.sourceLine(sources, FredParser.releaseName(release));
        } catch (UpstreamFormatException | UpstreamHttpException e) {
            // The source line is a courtesy; the series itself is still read.
            sourceLine = FredParser.sourceLine(null, null);
        }
        state.put(metaKey(id), info.encode(sourceLine, now));
        return new FredParser.Stored(info, info.copyrighted(), sourceLine, now);
    }

    private static byte[] get(FetchContext ctx, URI uri) throws AdapterException {
        UpstreamResponse response;
        try {
            response = ctx.http().get(uri, Map.of("Accept", "application/json"));
        } catch (UpstreamHttpException e) {
            String body = e.getBodySnippet() == null ? "" : e.getBodySnippet().toLowerCase(Locale.ROOT);
            if (e.getStatus() == 400 && body.contains("api_key")) {
                throw new CredentialsRejectedException(ID + ": FRED rejected the API key (" + snippet(body) + ")");
            }
            throw e;
        }
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": FRED" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " response exceeds the cap"), "");
        }
        return response.body();
    }

    private static String snippet(String body) {
        return body.length() <= 160 ? body : body.substring(0, 160);
    }

    private static LocalDate newestPeriod(List<NormalizedRecord> points) {
        LocalDate newest = null;
        for (NormalizedRecord point : points) {
            LocalDate date = parseDate(String.valueOf(point.payload().get("period")));
            if (date != null && (newest == null || date.isAfter(newest))) {
                newest = date;
            }
        }
        return newest;
    }

    static String metaKey(String id) {
        return "meta." + id;
    }

    static String lastKey(String id) {
        return "last." + id;
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

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("FRED economic series")
                        .description("Rates and prices from the Federal Reserve Bank of St. Louis FRED API (10- and"
                                + " 2-year Treasury yields, the 10y-2y spread, the effective federal funds rate,"
                                + " the broad dollar index, WTI and Henry Hub spot prices by default). LOCAL_ONLY:"
                                + " raw values are served only to this instance's callers allowed local egress."
                                + " Series whose notes mark them as copyrighted (for example VIXCLS, OVXCLS,"
                                + " DCOILBRENTEU) are skipped unless listed in allowCopyrighted. Needs a free FRED"
                                + " API key. " + FredParser.NOTICE)
                        .homepage("https://fred.stlouisfed.org/")
                        .attribution(FredParser.NOTICE)
                        .licence("FRED API terms of use; third-party series keep their owners' copyright"
                                + " (personal use)")
                        .termsUrl("https://fred.stlouisfed.org/docs/api/terms_of_use.html")
                        .mode(Mode.POLLING)
                        .egress(Egress.LOCAL_ONLY)
                        .credential(CredentialField.required(API_KEY,
                                "FRED API key (free from fred.stlouisfed.org), sent as the api_key parameter"))
                        .param(new ParamSpec(SERIES_PARAM, ParamType.STRING_LIST, "FRED series ids to read",
                                DEFAULT_SERIES, false, List.of()))
                        .param(new ParamSpec(ALLOW_COPYRIGHTED_PARAM, ParamType.STRING_LIST,
                                "Copyrighted FRED series you accept for personal use (otherwise skipped)",
                                List.of(), false, List.of()))
                        .interval(Duration.ofHours(1), Duration.ofHours(6))
                        .defaultBudget(Budget.of(30, 1500))
                        .fetchTimeout(Duration.ofMinutes(2))
                        .observedAtMode(ObservedAtMode.RECEIVED))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
