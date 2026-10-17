package ai.foresignal.forecast.baseline;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.JdbcResolutionData;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.forecast.score.Skill;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code climatology} forecaster (prompt 122 §4): for a numeric {@code series-value} question, the empirical
 * quantiles of the same aggregate of the same series over the same calendar window in past years, with the window
 * shifted by up to {@code seasonalDays} (default 14) in 7-day steps, across up to {@code maxYears} (default 30) years.
 * Only data known by the forecast time is read (the {@code SeriesReader} {@code asOf}); a past window with more than
 * 20% of its buckets missing is left out, and fewer than {@code minSamples} (default 5) windows forecast nothing.
 */
@Component
public class ClimatologyForecaster implements BaselineForecaster {

    private final SeriesReader reader;
    private final ForecasterRegistry registry;

    /** Creates the forecaster. */
    public ClimatologyForecaster(SeriesReader reader, ForecasterRegistry registry) {
        this.reader = reader;
        this.registry = registry;
    }

    @Override
    public String id() {
        return ForecasterRegistry.CLIMATOLOGY;
    }

    @Override
    public boolean supports(ForecastQuestionEntity q) {
        return Ledger.NUMERIC.equals(q.getType()) && "series-value".equals(q.getResolverType());
    }

    /** The type-7 quantiles p05…p95 of a sample. */
    public static Map<String, Double> quantiles(double[] sample) {
        double[] sorted = sample.clone();
        Arrays.sort(sorted);
        Map<String, Double> out = new LinkedHashMap<>();
        for (String level : Ledger.QUANTILES) {
            out.put(level, Skill.quantile(sorted, Ledger.level(level)));
        }
        return out;
    }

    @Override
    public Outcome forecast(ForecastQuestionEntity q, Instant asOf, boolean retrospective) {
        if (!supports(q)) {
            return Outcome.skip("climatology forecasts numeric series-value questions only");
        }
        ForecasterEntity config = registry.find(id()).orElse(null);
        int seasonal = config == null ? 14 : (int) config.configNumber("seasonalDays", 14);
        int maxYears = config == null ? 30 : (int) config.configNumber("maxYears", 30);
        int minSamples = config == null ? 5 : (int) config.configNumber("minSamples", 5);
        Map<String, Object> resolver = q.getResolver();
        String refText = resolver.get("ref").toString();
        String aggregate = resolver.get("aggregate") == null ? "last" : resolver.get("aggregate").toString();
        Instant[] window = ResolverSpec.window(resolver, q.getResolveBy());
        SeriesRef ref = SeriesRef.parse(refText);
        SeriesData data = null;
        int years = maxYears;
        while (years >= 1 && data == null) {
            Instant from = shift(window[0], -years, -seasonal);
            Instant to = shift(window[1], -1, seasonal);
            if (to.isAfter(asOf)) {
                to = asOf;
            }
            if (!from.isBefore(to)) {
                return Outcome.skip("no history before the question's window");
            }
            try {
                data = reader.read(ref, from, to, asOf);
            } catch (ForesignalException e) {
                if (e.getStatus().value() == 404) {
                    return Outcome.skip("the series " + refText + " does not exist");
                }
                years = years / 2;
            }
        }
        if (data == null) {
            return Outcome.skip("the series is too dense to read " + maxYears + " years of history");
        }
        SeriesData.Grid grid = data.grid().knownBy(asOf);
        List<Double> sample = new ArrayList<>();
        for (int k = 1; k <= years; k++) {
            for (int d = -seasonal; d <= seasonal; d += 7) {
                Instant from = shift(window[0], -k, d);
                Instant to = shift(window[1], -k, d);
                if (to.isAfter(asOf)) {
                    continue;
                }
                List<Double> values = new ArrayList<>();
                int buckets = 0;
                for (int i = 0; i < grid.size(); i++) {
                    Instant start = grid.starts()[i];
                    if (start.isBefore(from) || !start.isBefore(to)) {
                        continue;
                    }
                    buckets++;
                    if (!grid.gap()[i] && !Double.isNaN(grid.values()[i])) {
                        values.add(grid.values()[i]);
                    }
                }
                if (buckets == 0 || values.size() < 0.8 * buckets) {
                    continue;
                }
                Double v = JdbcResolutionData.aggregate(values, aggregate);
                if (v != null && Double.isFinite(v)) {
                    sample.add(v);
                }
            }
        }
        if (sample.size() < minSamples) {
            return Outcome.skip("only " + sample.size() + " comparable past windows (at least " + minSamples
                    + " needed)");
        }
        double[] values = sample.stream().mapToDouble(Double::doubleValue).toArray();
        Map<String, Double> quantiles = quantiles(values);
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("seriesRef", refText);
        inputs.put("aggregate", aggregate);
        inputs.put("samples", values.length);
        inputs.put("years", years);
        inputs.put("seasonalDays", seasonal);
        inputs.put("asOf", asOf.toString());
        inputs.put("asOfSeq", data.asOfSeq());
        return Outcome.of(ForecastValue.quantiles(quantiles), "Climatology: empirical quantiles of the " + aggregate
                + " of " + refText + " over " + values.length + " comparable windows (same season ±" + seasonal
                + " days, up to " + years + " years).", inputs);
    }

    private static Instant shift(Instant t, int years, int days) {
        ZonedDateTime z = t.atZone(ZoneOffset.UTC).plusYears(years).plusDays(days);
        return z.toInstant();
    }
}
