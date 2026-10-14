package ai.foresignal.source.adapters.fingrid;

import ai.foresignal.source.series.FrequencyExcursionDetector;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code grid-frequency-fingrid} (prompt 093, disabled by default): Finland's system frequency from Fingrid's open
 * data API (dataset 177).
 *
 * <p>Needs a free Fingrid API key, sent as the {@code x-api-key} header; the Nordic operational band is ±0.1 Hz.
 * Updated every three minutes; the API allows 10,000 requests a day and one request per two seconds.
 */
@Component
public class GridFrequencyFingridAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "grid-frequency-fingrid";

    /** The grid name used in ids. */
    public static final String GRID = "fi";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://data.fingrid.fi";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.fingrid.base-url";

    /** The credential. */
    public static final String API_KEY = "apiKey";

    /** The header carrying the key. */
    public static final String KEY_HEADER = "x-api-key";

    /** The Fingrid frequency dataset. */
    static final int DATASET = 177;

    /** The Nordic operational band. */
    static final FrequencyExcursionDetector.Config CONFIG =
            new FrequencyExcursionDetector.Config(GRID, 0.1, 0.5);

    /** How far back the first poll reaches. */
    static final Duration FIRST_WINDOW = Duration.ofMinutes(10);

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public GridFrequencyFingridAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The frequency request URL for a window. */
    public URI frequencyUri(Instant from, Instant to) {
        return URI.create(baseUrl + "/api/datasets/" + DATASET + "/data?startTime=" + from + "&endTime=" + to
                + "&pageSize=20000");
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String key = ctx.credentials() == null ? null : ctx.credentials().get(API_KEY);
        if (key == null || key.isBlank()) {
            throw new AdapterException(ID + ": the credential " + API_KEY + " is not set (a key from"
                    + " data.fingrid.fi)");
        }
        FrequencyExcursionDetector.State state = FrequencyExcursionDetector.read(ctx.state(), GRID);
        Instant now = ctx.now();
        Instant from = state.lastSampleTime() != null ? state.lastSampleTime() : now.minus(FIRST_WINDOW);
        UpstreamResponse response = ctx.http().get(frequencyUri(from, now), Map.of(KEY_HEADER, key.strip()));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        FrequencyExcursionDetector.Result result = FrequencyExcursionDetector.process(CONFIG, state,
                FingridFrequencyParser.parse(response.body()), now);
        FrequencyExcursionDetector.write(ctx.state(), GRID, result.state());
        ctx.log().debug("{}: {} record(s)", ID, result.records().size());
        return FetchResult.complete(result.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("Finland grid frequency (Fingrid)")
                .description("Finland's electricity system frequency from Fingrid's open data API (dataset 177)."
                        + " Disabled by default; needs a free Fingrid API key.")
                .homepage("https://data.fingrid.fi/en/datasets/177")
                .attribution("Fingrid")
                .licence("CC BY 4.0")
                .termsUrl("https://data.fingrid.fi/en/instructions")
                .mode(Mode.POLLING)
                .kinds(FrequencyExcursionDetector.EXCURSION_KIND, FrequencyExcursionDetector.STATE_KIND)
                .credential(CredentialField.required(API_KEY,
                        "Fingrid API key (free from data.fingrid.fi), sent as the x-api-key header"))
                .interval(Duration.ofMinutes(1), Duration.ofMinutes(3))
                .defaultBudget(Budget.of(1, 600))
                .historyTier(ai.foresignal.source.spi.HistoryTier.STANDARD)
                .defaultIngestPolicy(ai.foresignal.source.spi.IngestPolicy.all())
                .recordTtl(null)
                .ttlAnchor(ai.foresignal.source.spi.TtlAnchor.OBSERVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .defaultRetention(Duration.ofDays(1825))
                .titleField("grid")
                .searchFields("grid", "direction", "severity")
                .volatileFields("latestHz", "latestAt")
                .zeroResultsNormal(true);
        FrequencyExcursionDetector.declareFieldSpecs(builder);
        builder.kindDisplay(FrequencyExcursionDetector.EXCURSION_KIND,
                        new ai.foresignal.source.spi.KindDisplay("{grid} {direction} {extremeHz} Hz", null,
                                "#E8590C", null))
                .kindDisplay(FrequencyExcursionDetector.STATE_KIND,
                        new ai.foresignal.source.spi.KindDisplay("{grid} {state}", null, "#74B816", null));
        return builder.build();
    }
}
