package ai.foresignal.forecast.baseline;

import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolutionData;
import ai.foresignal.forecast.resolve.ResolverSpec;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The {@code persistence} forecaster (prompt 122 §4): "it stays as it is". For a numeric {@code series-value} question
 * every quantile is the last known value; for a binary one ({@code series-value} with a threshold, or
 * {@code situation-status}) the probability is {@code binaryConfidence} (default 0.9) when the current value or state
 * already satisfies the question and {@code 1 − binaryConfidence} when not. Only data known by the forecast time is
 * read.
 */
@Component
public class PersistenceForecaster implements BaselineForecaster {

    private final SeriesReader reader;
    private final ResolutionData data;
    private final ForecasterRegistry registry;

    /** Creates the forecaster. */
    public PersistenceForecaster(SeriesReader reader, ResolutionData data, ForecasterRegistry registry) {
        this.reader = reader;
        this.data = data;
        this.registry = registry;
    }

    @Override
    public String id() {
        return ForecasterRegistry.PERSISTENCE;
    }

    @Override
    public boolean supports(ForecastQuestionEntity q) {
        return "series-value".equals(q.getResolverType())
                || ("situation-status".equals(q.getResolverType()) && Ledger.BINARY.equals(q.getType()));
    }

    @Override
    public Outcome forecast(ForecastQuestionEntity q, Instant asOf, boolean retrospective) {
        if (!supports(q)) {
            return Outcome.skip("persistence forecasts series-value and situation-status questions only");
        }
        ForecasterEntity config = registry.find(id()).orElse(null);
        double confidence = config == null ? 0.9 : config.configNumber("binaryConfidence", 0.9);
        confidence = Math.max(0.5, Math.min(0.99, confidence));
        Map<String, Object> resolver = q.getResolver();
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("asOf", asOf.toString());
        if ("situation-status".equals(q.getResolverType())) {
            String situationId = resolver.get("situationId").toString();
            ResolutionData.SituationState state = data.situationAt(situationId, asOf);
            if (state == null) {
                return Outcome.skip("the situation is unknown at the forecast time");
            }
            Object status = resolver.get("status");
            Object min = resolver.get("minSeverity");
            boolean holds = (status == null || status.toString().equals(state.status()))
                    && (min == null || ResolverSpec.SEVERITIES.indexOf(state.severity())
                            >= ResolverSpec.SEVERITIES.indexOf(min.toString()));
            inputs.put("status", state.status());
            inputs.put("severity", state.severity());
            double p = holds ? confidence : 1 - confidence;
            return Outcome.of(ForecastValue.binary(p), "Persistence: the situation is now " + state.status() + " ("
                    + state.severity() + "); the question " + (holds ? "already holds" : "does not hold yet")
                    + ", so it is taken to stay so.", inputs);
        }
        String refText = resolver.get("ref").toString();
        SeriesData series;
        try {
            series = reader.read(SeriesRef.parse(refText), null, asOf, asOf);
        } catch (ForesignalException e) {
            return Outcome.skip("the series " + refText + " cannot be read: " + e.getMessage());
        }
        SeriesValue last = null;
        List<SeriesValue> points = series.points();
        for (SeriesValue v : points) {
            if (v.usable() && (v.knownAt() == null || !v.knownAt().isAfter(asOf))) {
                last = v;
            }
        }
        if (last == null) {
            return Outcome.skip("the series has no known value before the forecast time");
        }
        double value = last.value();
        inputs.put("seriesRef", refText);
        inputs.put("lastValue", value);
        inputs.put("lastPeriod", last.periodStart().toString());
        inputs.put("asOfSeq", series.asOfSeq());
        if (resolver.get("op") == null) {
            Map<String, Double> quantiles = new LinkedHashMap<>();
            for (String level : Ledger.QUANTILES) {
                quantiles.put(level, value);
            }
            return Outcome.of(ForecastValue.quantiles(quantiles), "Persistence: the last known value of " + refText
                    + " (" + value + " for " + last.periodStart() + ") is carried forward.", inputs);
        }
        boolean holds = ResolverSpec.compare(value, resolver.get("op").toString(),
                ((Number) resolver.get("threshold")).doubleValue());
        double p = holds ? confidence : 1 - confidence;
        return Outcome.of(ForecastValue.binary(p), "Persistence: the last known value " + value + " "
                + (holds ? "already meets" : "does not meet") + " the threshold, and is taken to stay so.", inputs);
    }
}
