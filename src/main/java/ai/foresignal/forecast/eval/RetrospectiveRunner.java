package ai.foresignal.forecast.eval;

import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecasterEntity;
import ai.foresignal.forecast.baseline.BaselineForecaster;
import java.time.Instant;

/**
 * Runs a non-baseline forecaster as of a past time for retrospective evaluation (prompt 122 §7). The built-in
 * baselines are run directly; later prompts register a runner for their forecasters (128: the LLM forecaster, with
 * its tools and readers forced to {@code asOf}).
 */
public interface RetrospectiveRunner {

    /** True when this runner can run {@code forecaster}. */
    boolean handles(ForecasterEntity forecaster);

    /**
     * Forecasts a question as of {@code asOf} without writing anything.
     *
     * @param forecaster the forecaster
     * @param question the question
     * @param asOf the knowledge cutoff (the question's {@code opens_at})
     * @return the forecast, or a skip with the reason
     */
    BaselineForecaster.Outcome forecast(ForecasterEntity forecaster, ForecastQuestionEntity question, Instant asOf);
}
