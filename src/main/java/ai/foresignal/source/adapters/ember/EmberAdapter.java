package ai.foresignal.source.adapters.ember;

import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
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
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code ember-electricity} (prompt 093): Ember monthly electricity generation (with the generation mix share),
 * carbon intensity and demand, as {@code series-point} records.
 *
 * <p>The options endpoint {@code /v1/options/{dataset}/monthly/date} is checked first and the data call is skipped
 * when the newest month has not moved (adapter state). Published twice a month. Needs an Ember API key, sent as the
 * {@code api_key} query parameter.
 */
@Component
public class EmberAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ember-electricity";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.ember-energy.org";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ember.base-url";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The entities parameter. */
    public static final String ENTITIES_PARAM = "entities";

    /** The datasets parameter. */
    public static final String DATASETS_PARAM = "datasets";

    /** The default entities. */
    public static final List<String> DEFAULT_ENTITIES = List.of("USA", "GBR", "DEU", "FRA", "CHN", "IND");

    /** The trailing window. */
    static final int MONTHS = 12;

    /** The adapter-state prefix of a dataset's newest month. */
    static final String MONTH_STATE_PREFIX = "ember:month:";

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public EmberAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The options date URL of one dataset. */
    public URI optionsUri(String dataset, String key) {
        return URI.create(baseUrl + "/v1/options/" + dataset + "/monthly/date?api_key=" + key);
    }

    /** The monthly data URL of one dataset. */
    public URI dataUri(String dataset, List<String> entities, Instant now, String key) {
        String start = MONTH.format(now.atZone(ZoneOffset.UTC).minusMonths(MONTHS));
        return URI.create(baseUrl + "/v1/" + dataset + "/monthly?entity_code=" + String.join(",", entities)
                + "&start_date=" + start + "&api_key=" + key);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a key from"
                    + " ember-energy.org)");
        }
        Map<String, Object> params = ctx.params() == null ? Map.of() : ctx.params().values();
        @SuppressWarnings("unchecked")
        List<String> datasets = params.get(DATASETS_PARAM) instanceof List<?> list
                ? list.stream().map(String::valueOf).toList() : List.of(EmberParser.GENERATION);
        List<String> entities = ctx.params() == null || ctx.params().stringList(ENTITIES_PARAM).isEmpty()
                ? DEFAULT_ENTITIES : ctx.params().stringList(ENTITIES_PARAM);
        List<NormalizedRecord> out = new ArrayList<>();
        boolean anyChange = false;
        for (String dataset : datasets) {
            String stateKey = MONTH_STATE_PREFIX + dataset;
            String newest = options(ctx, dataset, key.strip());
            String stored = ctx.state().get(stateKey).orElse(null);
            if (newest != null && newest.equals(stored)) {
                continue;
            }
            anyChange = true;
            List<NormalizedRecord> points = EmberParser.parse(
                    body(ctx, ctx.http().get(dataUri(dataset, entities, ctx.now(), key.strip()))), dataset);
            out.addAll(points);
            out.addAll(SeriesPoint.latestPerSeries(points));
            if (newest != null) {
                ctx.state().put(stateKey, newest);
            }
        }
        if (!anyChange) {
            return FetchResult.unchanged();
        }
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        return FetchResult.complete(out);
    }

    private String options(FetchContext ctx, String dataset, String key) {
        try {
            UpstreamResponse response = ctx.http().get(optionsUri(dataset, key));
            if (response.status() != 200 || response.truncated()) {
                return null;
            }
            return EmberParser.newestMonth(response.body());
        } catch (AdapterException e) {
            ctx.log().warn("{}: options check for {} failed ({}); reading the data anyway", ID, dataset,
                    e.getMessage());
            return null;
        }
    }

    private static byte[] body(FetchContext ctx, UpstreamResponse response) {
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ctx.sourceId() + ": Ember" + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("Ember electricity")
                        .description("Ember monthly electricity data: generation and the generation mix share per"
                                + " entity, with optional carbon intensity and demand, as time-series records"
                                + " (trailing 12 months). Needs a free Ember API key.")
                        .homepage("https://ember-energy.org/")
                        .attribution("Ember")
                        .licence("CC BY 4.0")
                        .termsUrl("https://ember-energy.org/data/api-terms/")
                        .mode(Mode.POLLING)
                        .credential(CredentialField.required(API_KEY,
                                "Ember API key (free from ember-energy.org), sent as the api_key query parameter"))
                        .param(new ParamSpec(ENTITIES_PARAM, ParamType.STRING_LIST,
                                "Ember entity codes to read", DEFAULT_ENTITIES, false, List.of()))
                        .param(new ParamSpec(DATASETS_PARAM, ParamType.STRING_LIST,
                                "Datasets: electricity-generation (default), carbon-intensity, electricity-demand",
                                List.of(EmberParser.GENERATION), false, EmberParser.DATASETS))
                        .interval(Duration.ofHours(1), Duration.ofHours(24))
                        .defaultBudget(Budget.of(2, 20))
                        .observedAtMode(ObservedAtMode.UPSTREAM))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
