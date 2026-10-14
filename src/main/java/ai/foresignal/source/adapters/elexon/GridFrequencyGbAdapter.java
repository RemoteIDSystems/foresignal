package ai.foresignal.source.adapters.elexon;

import ai.foresignal.source.series.FrequencyExcursionDetector;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
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
import java.time.Year;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code grid-frequency-gb} (prompt 093): Great Britain's 15-second system frequency from the Elexon BMRS API.
 *
 * <p>The {@link FrequencyExcursionDetector} turns a run of samples that leaves the ±0.2 Hz operational band into a
 * {@code frequency-excursion} record, and every poll emits one {@code grid-frequency} state record. No key is
 * required; the samples are about two minutes behind real time.
 */
@Component
public class GridFrequencyGbAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "grid-frequency-gb";

    /** The grid name used in ids. */
    public static final String GRID = "gb";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://data.elexon.co.uk";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.elexon.base-url";

    /** The GB operational band. */
    static final FrequencyExcursionDetector.Config CONFIG =
            new FrequencyExcursionDetector.Config(GRID, 0.2, 0.5);

    /** How far back the first poll reaches. */
    static final Duration FIRST_WINDOW = Duration.ofMinutes(5);

    /** The licence. */
    public static final String LICENCE = "BMRS Data Licence";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public GridFrequencyGbAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /** The frequency request URL for a window. */
    public URI frequencyUri(Instant from, Instant to) {
        return URI.create(baseUrl + "/bmrs/api/v1/system/frequency?from=" + from + "&to=" + to);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        FrequencyExcursionDetector.State state = FrequencyExcursionDetector.read(ctx.state(), GRID);
        Instant now = ctx.now();
        Instant from = state.lastSampleTime() != null ? state.lastSampleTime() : now.minus(FIRST_WINDOW);
        UpstreamResponse response = ctx.http().get(frequencyUri(from, now));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        FrequencyExcursionDetector.Result result = FrequencyExcursionDetector.process(CONFIG, state,
                ElexonFrequencyParser.parse(response.body()), now);
        FrequencyExcursionDetector.write(ctx.state(), GRID, result.state());
        ctx.log().debug("{}: {} record(s)", ID, result.records().size());
        return FetchResult.complete(result.records());
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GB grid frequency (Elexon)")
                .description("Great Britain's electricity system frequency, 15-second samples from the Elexon BMRS"
                        + " API. A run of samples outside the ±0.2 Hz operational band becomes a"
                        + " frequency-excursion record; a grid-frequency state record carries the banded state.")
                .homepage("https://bmrs.elexon.co.uk/")
                .attribution("Contains BMRS data © Elexon Limited copyright and database right " + Year.now())
                .licence(LICENCE)
                .termsUrl("https://www.elexon.co.uk/bmrs/data-licence/")
                .mode(Mode.POLLING)
                .kinds(FrequencyExcursionDetector.EXCURSION_KIND, FrequencyExcursionDetector.STATE_KIND)
                .interval(Duration.ofMinutes(1), Duration.ofMinutes(2))
                .defaultBudget(Budget.of(2, 1500))
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
