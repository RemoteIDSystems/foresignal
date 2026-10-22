package ai.foresignal.forecast.baseline;

import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.ensemble.EnsembleMath;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.outlook.series.SeriesForecastEntity;
import ai.foresignal.outlook.series.SeriesForecastService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The {@code threshold} forecaster (prompt 156): a "will this series cross X by date Y" binary question
 * ({@code series-value} or {@code count-threshold} with an {@code op} and {@code threshold} over a series) is
 * answered from a numeric <em>predictive distribution</em> rather than a flat confidence. It takes the numeric
 * forecaster's predictive quantiles for the resolution window and reads {@code P(value op threshold)} off the
 * implied CDF ({@link ThresholdCrossing}).
 *
 * <p>The distribution is, in order of preference, the <strong>ensemble quantile pool</strong> of the numeric
 * baseline ({@link ClimatologyForecaster}) and Foresignal's statistical series forecast
 * ({@link SeriesForecastService}, which selects ETS today and will carry an external quantile forecaster such as
 * TimesFM tomorrow), falling back to whichever single one is available. This forecaster replaces the
 * flat-confidence {@link PersistenceForecaster} contribution for these questions; the deterministic resolver and
 * the other forecasters are unchanged.
 */
@Component
public class ThresholdCrossingForecaster implements BaselineForecaster {

    private final ClimatologyForecaster climatology;
    private final ObjectProvider<SeriesForecastService> seriesForecasts;

    /** Creates the forecaster. */
    public ThresholdCrossingForecaster(ClimatologyForecaster climatology,
            ObjectProvider<SeriesForecastService> seriesForecasts) {
        this.climatology = climatology;
        this.seriesForecasts = seriesForecasts;
    }

    @Override
    public String id() {
        return ForecasterRegistry.THRESHOLD;
    }

    @Override
    public boolean supports(ForecastQuestionEntity q) {
        if (!Ledger.BINARY.equals(q.getType())) {
            return false;
        }
        Map<String, Object> resolver = q.getResolver();
        if (resolver == null || resolver.get("op") == null || resolver.get("threshold") == null) {
            return false;
        }
        String type = q.getResolverType();
        // A series distribution needs a series ref: series-value always has one; a count-threshold has one only
        // when it counts a rollup series (a selector-based count is left to the base-rate forecaster).
        return ("series-value".equals(type) || "count-threshold".equals(type)) && resolver.get("ref") != null;
    }

    @Override
    public Outcome forecast(ForecastQuestionEntity q, Instant asOf, boolean retrospective) {
        if (!supports(q)) {
            return Outcome.skip("the threshold forecaster answers series threshold-crossing questions only");
        }
        Map<String, Object> resolver = q.getResolver();
        String op = resolver.get("op").toString();
        if ("eq".equals(op)) {
            return Outcome.skip("a predictive distribution does not answer an equality threshold");
        }
        double threshold = ((Number) resolver.get("threshold")).doubleValue();
        String refText = resolver.get("ref").toString();
        Instant[] window = ResolverSpec.window(resolver, q.getResolveBy());
        String aggregate = "count-threshold".equals(q.getResolverType()) ? "sum"
                : resolver.get("aggregate") == null ? "last" : resolver.get("aggregate").toString();

        List<Map<String, Double>> members = new ArrayList<>();
        List<String> sources = new ArrayList<>();
        Map<String, Double> climatologyQuantiles = climatologyQuantiles(q, refText, window, aggregate, asOf,
                retrospective);
        if (climatologyQuantiles != null) {
            members.add(climatologyQuantiles);
            sources.add("climatology");
        }
        Map<String, Double> statisticalQuantiles = statisticalQuantiles(refText, window);
        if (statisticalQuantiles != null) {
            members.add(statisticalQuantiles);
            sources.add("statistical");
        }
        if (members.isEmpty()) {
            return Outcome.skip("no numeric predictive distribution is available for " + refText);
        }
        Map<String, Double> pooled = members.size() == 1 ? members.get(0)
                : EnsembleMath.combineQuantiles(members, equalWeights(members.size()));
        double p = ThresholdCrossing.probability(pooled, op, threshold);
        if (Double.isNaN(p)) {
            return Outcome.skip("the predictive distribution has too few quantile points to cross " + threshold);
        }

        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("asOf", asOf.toString());
        inputs.put("seriesRef", refText);
        inputs.put("op", op);
        inputs.put("threshold", threshold);
        inputs.put("aggregate", aggregate);
        inputs.put("distribution", sources);
        inputs.put("quantiles", pooled);
        String symbol = switch (op) {
            case "gt" -> ">";
            case "gte" -> "≥";
            case "lt" -> "<";
            default -> "≤";
        };
        return Outcome.of(ForecastValue.binary(p), "Threshold crossing: P(" + aggregate + " of " + refText + " "
                + symbol + " " + threshold + ") read off the " + String.join(" + ", sources)
                + " predictive distribution for the resolution window.", inputs);
    }

    /** The climatology numeric distribution of the series over the question's window, or null. */
    private Map<String, Double> climatologyQuantiles(ForecastQuestionEntity q, String refText, Instant[] window,
            String aggregate, Instant asOf, boolean retrospective) {
        ForecastQuestionEntity twin = new ForecastQuestionEntity("threshold-twin", asOf);
        twin.setType(Ledger.NUMERIC);
        Map<String, Object> resolver = new LinkedHashMap<>();
        resolver.put("type", "series-value");
        resolver.put("ref", refText);
        resolver.put("window", Map.of("start", window[0].toString(), "end", window[1].toString()));
        resolver.put("aggregate", aggregate);
        twin.setResolver(resolver);
        twin.setResolveBy(q.getResolveBy());
        try {
            Outcome outcome = climatology.forecast(twin, asOf, retrospective);
            if (outcome.skipped() || outcome.value() == null || outcome.value().quantiles() == null) {
                return null;
            }
            return outcome.value().quantiles();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** The statistical series-forecast distribution whose target window matches, or null. */
    private Map<String, Double> statisticalQuantiles(String refText, Instant[] window) {
        SeriesForecastService service = seriesForecasts.getIfAvailable();
        if (service == null) {
            return null;
        }
        try {
            for (SeriesForecastEntity row : service.latest(refText)) {
                if (row.getTargetStart().equals(window[0])) {
                    Map<String, Double> quantiles = new LinkedHashMap<>();
                    if (row.getQuantiles() != null) {
                        quantiles.putAll(row.getQuantiles());
                    }
                    if (row.getTails() != null) {
                        quantiles.putAll(row.getTails());
                    }
                    return quantiles.isEmpty() ? null : quantiles;
                }
            }
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    private static double[] equalWeights(int n) {
        double[] w = new double[n];
        java.util.Arrays.fill(w, 1.0 / n);
        return w;
    }
}
