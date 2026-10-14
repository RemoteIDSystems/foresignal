package ai.foresignal.source.adapters.carbon;

import ai.foresignal.source.series.SeriesPoint;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code gb-carbon-intensity} (prompt 093): Great Britain's national and regional carbon intensity and the
 * generation mix, as {@code series-point} records. No authentication.
 *
 * <p>The national point uses the actual intensity when present and the forecast otherwise (with
 * {@code status=forecast}); regions have forecasts only. {@code /intensity/pt24h} is re-read once an hour so late
 * actuals are picked up. The time base is {@code RECEIVED}, because the feed mixes forecasts and actuals.
 */
@Component
public class CarbonIntensityAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "gb-carbon-intensity";

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://api.carbonintensity.org.uk";

    /** Test-only override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.carbon.base-url";

    /** The adapter-state key holding the last hour the pt24h window was read. */
    static final String PT24H_STATE_KEY = "carbon:pt24h:hour";

    private static final SourceDescriptor DESCRIPTOR = buildDescriptor();

    private final String baseUrl;

    /** Creates the adapter. */
    public CarbonIntensityAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.trim().replaceAll("/+$", "");
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> points = new ArrayList<>();
        points.addAll(CarbonIntensityParser.national(get(ctx, "/intensity")));
        points.addAll(CarbonIntensityParser.regional(get(ctx, "/regional")));
        points.addAll(CarbonIntensityParser.generation(get(ctx, "/generation")));
        String hour = ctx.now().truncatedTo(ChronoUnit.HOURS).toString();
        if (!hour.equals(ctx.state().get(PT24H_STATE_KEY).orElse(null))) {
            points.addAll(CarbonIntensityParser.national(get(ctx, "/intensity/pt24h")));
            ctx.state().put(PT24H_STATE_KEY, hour);
        }
        List<NormalizedRecord> out = new ArrayList<>(points);
        out.addAll(SeriesPoint.latestPerSeries(points));
        ctx.log().debug("{}: {} record(s)", ID, out.size());
        return FetchResult.complete(out);
    }

    private byte[] get(FetchContext ctx, String path) throws AdapterException {
        URI uri = URI.create(baseUrl + path);
        UpstreamResponse response = ctx.http().get(uri);
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException(ID + ": " + path + (response.status() != 200
                    ? " answered HTTP " + response.status() : " exceeds the response cap"), "");
        }
        return response.body();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder builder = SeriesPoint.seriesDefaults(SourceDescriptor.builder(ID)
                        .name("GB carbon intensity")
                        .description("Great Britain's carbon intensity from the National Grid ESO / NESO Carbon"
                                + " Intensity API: national half-hours (actual or forecast), per-region forecasts and"
                                + " the generation mix, as time-series records. No key required.")
                        .homepage("https://carbonintensity.org.uk/")
                        .attribution("Carbon Intensity API (NESO, EDF, University of Oxford, WWF)")
                        .licence("CC BY 4.0")
                        .termsUrl("https://carbon-intensity.github.io/api-definitions/")
                        .mode(Mode.POLLING)
                        .interval(Duration.ofMinutes(15), Duration.ofMinutes(30))
                        .defaultBudget(Budget.of(3, 300))
                        .observedAtMode(ObservedAtMode.RECEIVED))
                .kind(SeriesPoint.KIND)
                .kind(SeriesPoint.LATEST_KIND);
        SeriesPoint.declareFieldSpecs(builder);
        SeriesPoint.declareKindDisplay(builder);
        return builder.build();
    }
}
