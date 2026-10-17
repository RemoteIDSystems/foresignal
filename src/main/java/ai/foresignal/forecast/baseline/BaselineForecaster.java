package ai.foresignal.forecast.baseline;

import ai.foresignal.forecast.ForecastInputs.ForecastValue;
import ai.foresignal.forecast.ForecastQuestionEntity;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A deterministic baseline forecaster (prompt 122 §4): {@code base-rate}, {@code climatology}, {@code persistence},
 * {@code precursor} or {@code analog}. It forecasts a question from Foresignal's stored data as of a time (now for
 * live forecasts, the question's {@code opens_at} for retrospective evaluation), or says why it cannot.
 */
public interface BaselineForecaster {

    /** The forecaster id (also its type). */
    String id();

    /** True when it may forecast questions of this kind at all (type and resolver). */
    boolean supports(ForecastQuestionEntity question);

    /**
     * Forecasts a question.
     *
     * @param question the question (its criteria and resolver are the forecaster's only description of it)
     * @param asOf the knowledge cutoff: nothing known after it may be used
     * @param retrospective true during retrospective evaluation (no caches, nothing written)
     * @return the forecast, or a skip with the reason
     */
    Outcome forecast(ForecastQuestionEntity question, Instant asOf, boolean retrospective);

    /**
     * A baseline's answer.
     *
     * @param value the forecast, or null when skipped
     * @param rationale the rationale (Markdown), or null
     * @param inputs the inputs to store
     * @param skipReason why nothing was forecast, or null
     */
    record Outcome(ForecastValue value, String rationale, Map<String, Object> inputs, String skipReason) {

        /** A forecast. */
        public static Outcome of(ForecastValue value, String rationale, Map<String, Object> inputs) {
            return new Outcome(value, rationale, inputs == null ? new LinkedHashMap<>() : inputs, null);
        }

        /** Nothing, and why. */
        public static Outcome skip(String reason) {
            return new Outcome(null, null, new LinkedHashMap<>(), reason);
        }

        /** True when skipped. */
        public boolean skipped() {
            return value == null;
        }
    }
}
