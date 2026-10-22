package ai.foresignal.forecast.baseline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.outlook.series.SeriesForecastService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The threshold forecaster (prompt 156): it answers a binary series threshold-crossing question from the numeric
 * baseline's predictive distribution, and leaves non-threshold questions to the other forecasters.
 */
class ThresholdCrossingForecasterTest {

    private final ClimatologyForecaster climatology = mock(ClimatologyForecaster.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<SeriesForecastService> series = mock(ObjectProvider.class);

    private final ThresholdCrossingForecaster forecaster = new ThresholdCrossingForecaster(climatology, series);

    private ForecastQuestionEntity thresholdQuestion(String op, double threshold) {
        ForecastQuestionEntity q = new ForecastQuestionEntity("q-1", Instant.parse("2026-06-01T00:00:00Z"));
        q.setType(Ledger.BINARY);
        Map<String, Object> resolver = new LinkedHashMap<>();
        resolver.put("type", "series-value");
        resolver.put("ref", "series:imf-portwatch-chokepoints/chokepoint4");
        resolver.put("op", op);
        resolver.put("threshold", threshold);
        resolver.put("aggregate", "mean");
        resolver.put("window", Map.of("start", "2026-07-01T00:00:00Z", "end", "2026-07-08T00:00:00Z"));
        q.setResolver(resolver);
        q.setResolveBy(Instant.parse("2026-07-08T00:00:00Z"));
        return q;
    }

    private Map<String, Double> symmetric() {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("p05", 10.0);
        m.put("p25", 20.0);
        m.put("p50", 30.0);
        m.put("p75", 40.0);
        m.put("p95", 50.0);
        return m;
    }

    @Test
    @DisplayName("it supports binary series threshold questions and not numeric or non-threshold ones")
    void supportsThresholdQuestionsOnly() {
        assertThat(forecaster.supports(thresholdQuestion("gt", 30.0))).isTrue();
        ForecastQuestionEntity numeric = thresholdQuestion("gt", 30.0);
        numeric.setType(Ledger.NUMERIC);
        numeric.getResolver().remove("op");
        numeric.getResolver().remove("threshold");
        assertThat(forecaster.supports(numeric)).isFalse();
    }

    @Test
    @DisplayName("the crossing probability comes from the numeric baseline distribution")
    void forecastsFromDistribution() {
        when(series.getIfAvailable()).thenReturn(null);
        when(climatology.forecast(any(), any(), anyBoolean()))
                .thenReturn(BaselineForecaster.Outcome.of(ForecastValue.quantiles(symmetric()), "clim", null));

        BaselineForecaster.Outcome outcome = forecaster.forecast(thresholdQuestion("gt", 30.0),
                Instant.parse("2026-06-01T00:00:00Z"), false);

        assertThat(outcome.skipped()).isFalse();
        // Threshold at the median: about even.
        assertThat(outcome.value().probability()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("a threshold deep below the distribution is near-certain to be exceeded")
    void tailThresholdIsNearCertain() {
        when(series.getIfAvailable()).thenReturn(null);
        when(climatology.forecast(any(), any(), anyBoolean()))
                .thenReturn(BaselineForecaster.Outcome.of(ForecastValue.quantiles(symmetric()), "clim", null));

        BaselineForecaster.Outcome outcome = forecaster.forecast(thresholdQuestion("gt", 5.0),
                Instant.parse("2026-06-01T00:00:00Z"), false);

        assertThat(outcome.value().probability()).isEqualTo(ThresholdCrossing.MAX);
    }

    @Test
    @DisplayName("with no distribution available it skips rather than guessing")
    void skipsWithoutDistribution() {
        when(series.getIfAvailable()).thenReturn(null);
        when(climatology.forecast(any(), any(), anyBoolean()))
                .thenReturn(BaselineForecaster.Outcome.skip("no history"));

        BaselineForecaster.Outcome outcome = forecaster.forecast(thresholdQuestion("gt", 30.0),
                Instant.parse("2026-06-01T00:00:00Z"), false);

        assertThat(outcome.skipped()).isTrue();
    }
}
