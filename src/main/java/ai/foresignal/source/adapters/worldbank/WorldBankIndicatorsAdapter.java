package ai.foresignal.source.adapters.worldbank;

import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import java.time.ZoneOffset;
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
 * {@code worldbank-indicators} (prompt 117): slow structural covariates from the World Bank Indicators API v2, one
 * annual series per indicator and country, as {@code series-point} and {@code series-latest} records.
 *
 * <p>A run (weekly) reads the country list once ({@code /v2/country?format=json&per_page=400}) to skip aggregates
 * (regions and income groups: {@code region.id = "NA"}) and map ISO3 to the 040 ISO2 region code, then per
 * indicator its metadata ({@code /v2/indicator/{id}}, for the name and {@code sourceOrganization}) and its data
 * ({@code /v2/country/all/indicator/{id}?format=json&per_page=20000&date={fromYear}:{thisYear}&page={n}}), paging
 * until the last page. An unknown indicator is skipped with a health warning. {@code seriesId = wb.{indicator}.{iso2}},
 * annual periods, family structural, {@code regionCode} set, release lag 180 days. Most indicators are CC BY 4.0; the
 * {@code series-latest} attribution names the indicator's {@code sourceOrganization}.
 */
@Component
public class WorldBankIndicatorsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "worldbank-indicators";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.worldbank.org";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.worldbank.base-url";

    /** The indicators parameter. */
    public static final String INDICATORS_PARAM = "indicators";

    /** The first-year parameter. */
    public static final String FROM_YEAR_PARAM = "fromYear";

    /** The default indicators. */
    public static final List<String> DEFAULT_INDICATORS = List.of("SP.POP.TOTL", "NY.GDP.PCAP.KD",
            "SP.DYN.IMRT.IN", "SP.URB.TOTL.IN.ZS", "NE.TRD.GNFS.ZS", "EG.ELC.ACCS.ZS");

    /** The default first year. */
    public static final int DEFAULT_FROM_YEAR = 2000;

    /** The page size asked for. */
    public static final int PER_PAGE = 20000;

    /** The most data pages read per indicator and run. */
    static final int MAX_PAGES = 10;

    /** A World Bank indicator id. */
    static final Pattern INDICATOR_ID = Pattern.compile("^[A-Za-z0-9_.]{2,64}$");

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public WorldBankIndicatorsAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        for (String id : params.stringList(INDICATORS_PARAM)) {
            if (!INDICATOR_ID.matcher(id.strip()).matches()) {
                problems.put(INDICATORS_PARAM, "'" + id + "' is not a World Bank indicator id");
                break;
            }
        }
        Integer from = params.integer(FROM_YEAR_PARAM);
        if (from != null && (from < 1960 || from > 2100)) {
            problems.put(FROM_YEAR_PARAM, "must be between 1960 and 2100");
        }
        return problems;
    }

    /** The indicators a run reads. */
    static List<String> indicators(ValidatedParams params) {
        List<String> wanted = params == null || params.stringList(INDICATORS_PARAM).isEmpty()
                ? DEFAULT_INDICATORS : params.stringList(INDICATORS_PARAM);
        Set<String> out = new LinkedHashSet<>();
        for (String id : wanted) {
            String stripped = id == null ? "" : id.strip();
            if (INDICATOR_ID.matcher(stripped).matches()) {
                out.add(stripped.toUpperCase(Locale.ROOT));
            }
        }
        return List.copyOf(out);
    }

    /** The country-list request. */
    URI countriesUri() {
        return URI.create(baseUrl + "/v2/country?format=json&per_page=400");
    }

    /** An indicator's metadata request. */
    URI indicatorUri(String id) {
        return URI.create(baseUrl + "/v2/indicator/" + enc(id) + "?format=json");
    }

    /** One data page request. */
    URI dataUri(String id, int fromYear, int toYear, int page) {
        return URI.create(baseUrl + "/v2/country/all/indicator/" + enc(id) + "?format=json&per_page=" + PER_PAGE
                + "&date=" + fromYear + ":" + toYear + "&page=" + page);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Instant now = ctx.now();
        int fromYear = ctx.params() == null ? DEFAULT_FROM_YEAR
                : ctx.params().integer(FROM_YEAR_PARAM, DEFAULT_FROM_YEAR);
        int toYear = now.atZone(ZoneOffset.UTC).getYear();
        Map<String, String> countries = WorldBankParser.countries(WorldBankParser.page(get(ctx, countriesUri()))
                .rows());
        if (countries.isEmpty()) {
            throw new UpstreamFormatException(ID + ": the World Bank country list holds no countries", "");
        }
        List<NormalizedRecord> out = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        for (String id : indicators(ctx.params())) {
            WorldBankParser.Indicator indicator;
            try {
                indicator = WorldBankParser.indicator(id, WorldBankParser.page(get(ctx, indicatorUri(id))));
            } catch (UpstreamFormatException e) {
                unknown.add(id);
                continue;
            }
            List<NormalizedRecord> points = new ArrayList<>();
            int page = 1;
            int pages = 1;
            while (page <= pages && page <= MAX_PAGES) {
                WorldBankParser.Page data = WorldBankParser.page(get(ctx, dataUri(id, fromYear, toYear, page)));
                points.addAll(WorldBankParser.points(indicator, data.rows(), countries, now));
                pages = data.pages();
                page++;
            }
            out.addAll(points);
            String attribution = WorldBankParser.attribution(indicator);
            for (NormalizedRecord latest : SeriesPoint.latestPerSeries(points,
                    p -> WorldBankParser.meta(p).withEgress(DESCRIPTOR.egress()))) {
                out.add(SeriesPoint.withPayload(latest, "attribution", attribution));
            }
        }
        if (unknown.isEmpty()) {
            ctx.state().clearDegraded();
        } else {
            ctx.state().degrade("World Bank indicators not found and skipped: " + String.join(", ", unknown));
        }
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        return FetchResult.of(out);
    }

    private static byte[] get(FetchContext ctx, URI uri) throws AdapterException {
        UpstreamResponse response = ctx.http().get(uri, Map.of("Accept", "application/json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": the World Bank" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " response exceeds the cap"), "");
        }
        return response.body();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("World Bank indicators")
                        .description("Slow structural covariates from the World Bank Indicators API: population,"
                                + " GDP per capita, infant mortality, urbanization, trade share and electricity"
                                + " access by default, one annual series per country (aggregates skipped). Release"
                                + " lag about 180 days. Keyless.")
                        .homepage("https://datahelpdesk.worldbank.org/knowledgebase/articles/889392")
                        .attribution("World Bank, World Development Indicators")
                        .licence("CC BY 4.0 (most indicators; the source organization is recorded per indicator)")
                        .termsUrl("https://www.worldbank.org/en/about/legal/terms-of-use-for-datasets")
                        .mode(Mode.POLLING)
                        .egress(Egress.OPEN)
                        .param(new ParamSpec(INDICATORS_PARAM, ParamType.STRING_LIST, "World Bank indicator ids",
                                DEFAULT_INDICATORS, false, List.of()))
                        .param(ParamSpec.optional(FROM_YEAR_PARAM, ParamType.INTEGER, "First year read",
                                DEFAULT_FROM_YEAR))
                        .interval(Duration.ofDays(1), Duration.ofDays(7))
                        .defaultBudget(Budget.of(30, 200))
                        .fetchTimeout(Duration.ofMinutes(5))
                        .observedAtMode(ObservedAtMode.RECEIVED))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
