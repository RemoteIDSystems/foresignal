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
import java.time.ZoneOffset;
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

    /**
     * The longest window one poll asks for: after an outage the poll resumes from at most this far back rather than
     * from the last sample, so one request never asks for hours of 15-second samples.
     */
    static final Duration MAX_WINDOW = Duration.ofHours(1);

    /** The licence. */
    public static final String LICENCE = "BMRS Data Licence";

    private final String baseUrl;

    /** The descriptor of the current year (the attribution names the year), rebuilt when the year turns. */
    private volatile Built built;

    private record Built(Year year, SourceDescriptor descriptor) {}

    /** Creates the adapter. */
    public GridFrequencyGbAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    /** The descriptor; its attribution names the current year, so it is rebuilt when the year turns. */
    @Override
    public SourceDescriptor descriptor() {
        Year year = Year.now(ZoneOffset.UTC);
        Built current = built;
        if (current == null || !current.year().equals(year)) {
            current = new Built(year, buildDescriptor(year));
            built = current;
        }
        return current.descriptor();
    }

    /** The BMRS attribution line for {@code year}. */
    static String attribution(Year year) {
        return "Contains BMRS data © Elexon Limited copyright and database right " + year;
    }

    /** The start of the next poll's window: the last sample, at most {@link #MAX_WINDOW} ago. */
    static Instant windowStart(Instant lastSample, Instant now) {
        Instant earliest = now.minus(MAX_WINDOW);
        if (lastSample == null) {
            return now.minus(FIRST_WINDOW);
        }
        return lastSample.isBefore(earliest) ? earliest : lastSample;
    }

    /** The frequency request URL for a window. */
    public URI frequencyUri(Instant from, Instant to) {
        return URI.create(baseUrl + "/bmrs/api/v1/system/frequency?from=" + from + "&to=" + to);
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        FrequencyExcursionDetector.State state = FrequencyExcursionDetector.read(ctx.state(), GRID);
        Instant now = ctx.now();
        Instant from = windowStart(state.lastSampleTime(), now);
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

    private static SourceDescriptor buildDescriptor(Year year) {
        SourceDescriptor.Builder builder = SourceDescriptor.builder(ID)
                .name("GB grid frequency (Elexon)")
                .description("Great Britain's electricity system frequency, 15-second samples from the Elexon BMRS"
                        + " API. A run of samples outside the ±0.2 Hz operational band becomes a"
                        + " frequency-excursion record; a grid-frequency state record carries the banded state.")
                .homepage("https://bmrs.elexon.co.uk/")
                .attribution(attribution(year))
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
