package ai.foresignal.inbound.edp.binding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.baseline.BaselineForecaster;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Backtesting an external forecaster (prompt 155): the retrospective runner issues a synchronous as-of analysis
 * request per question, so {@code type=external} forecasters are no longer skipped by the evaluator.
 */
class ExternalRetrospectiveRunnerTest {

    private final AnalysisRequestService analysis = mock(AnalysisRequestService.class);
    private final ExternalRetrospectiveRunner runner = new ExternalRetrospectiveRunner(analysis);

    private ForecasterEntity external(Map<String, Object> config) {
        ForecasterEntity f = mock(ForecasterEntity.class);
        when(f.getType()).thenReturn("external");
        when(f.getId()).thenReturn("edp:ext-timesfm/transits");
        when(f.getConfig()).thenReturn(config);
        return f;
    }

    @Test
    @DisplayName("the runner handles external forecasters and no others")
    void handlesExternalOnly() {
        assertThat(runner.handles(external(Map.of()))).isTrue();
        ForecasterEntity baseline = mock(ForecasterEntity.class);
        when(baseline.getType()).thenReturn("base-rate");
        assertThat(runner.handles(baseline)).isFalse();
    }

    @Test
    @DisplayName("a usable as-of forecast becomes an outcome; the source and stream come from the config")
    void wrapsUsableForecast() {
        ForecasterEntity forecaster = external(Map.of("sourceId", "ext-timesfm", "streamId", "transits"));
        ForecastQuestionEntity q = mock(ForecastQuestionEntity.class);
        Instant asOf = Instant.parse("2026-06-01T00:00:00Z");
        ForecastInputs.ForecastValue value = ForecastInputs.ForecastValue.binary(0.4);
        when(analysis.forecastSyncAsOf(eq("ext-timesfm"), eq("transits"), eq(q), eq(asOf))).thenReturn(value);

        BaselineForecaster.Outcome outcome = runner.forecast(forecaster, q, asOf);

        assertThat(outcome.skipped()).isFalse();
        assertThat(outcome.value()).isEqualTo(value);
    }

    @Test
    @DisplayName("source and stream fall back to the edp forecaster id when the config omits them")
    void fallsBackToForecasterId() {
        ForecasterEntity forecaster = external(Map.of());
        ForecastQuestionEntity q = mock(ForecastQuestionEntity.class);
        Instant asOf = Instant.parse("2026-06-01T00:00:00Z");
        when(analysis.forecastSyncAsOf(eq("ext-timesfm"), eq("transits"), eq(q), eq(asOf)))
                .thenReturn(ForecastInputs.ForecastValue.binary(0.5));

        BaselineForecaster.Outcome outcome = runner.forecast(forecaster, q, asOf);

        assertThat(outcome.skipped()).isFalse();
    }

    @Test
    @DisplayName("no usable forecast is a skip, not a failure")
    void skipsWhenNoForecast() {
        ForecasterEntity forecaster = external(Map.of("sourceId", "ext-timesfm", "streamId", "transits"));
        ForecastQuestionEntity q = mock(ForecastQuestionEntity.class);
        Instant asOf = Instant.parse("2026-06-01T00:00:00Z");
        when(analysis.forecastSyncAsOf(any(), any(), any(), any())).thenReturn(null);

        BaselineForecaster.Outcome outcome = runner.forecast(forecaster, q, asOf);

        assertThat(outcome.skipped()).isTrue();
    }
}
