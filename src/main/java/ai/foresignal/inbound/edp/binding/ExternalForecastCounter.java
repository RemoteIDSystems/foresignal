package ai.foresignal.inbound.edp.binding;

import ai.foresignal.forecast.ensemble.ExternalForecasterCounts;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Counts an external forecaster's resolved linked questions for the 122 ensemble (prompt 138, EXT-3): scored
 * questions it forecast through a question link of its stream, or through a {@code questionRef} item.
 */
@Component
public class ExternalForecastCounter implements ExternalForecasterCounts {

    private final JdbcTemplate jdbc;

    /** Creates the counter. */
    public ExternalForecastCounter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean handles(String forecasterId) {
        return forecasterId != null && forecasterId.startsWith(BindingVocabulary.FORECASTER_PREFIX);
    }

    @Override
    public int resolvedLinked(String forecasterId) {
        Integer n = jdbc.queryForObject("SELECT count(DISTINCT s.question_id) FROM forecast_score s"
                + " WHERE s.forecaster_id = ? AND (s.question_id IN (SELECT l.question_id FROM external_question_link l"
                + " WHERE l.forecaster_id = ?) OR s.question_id IN (SELECT f.question_id FROM forecast f"
                + " WHERE f.forecaster_id = ? AND f.inputs->'edp'->>'via' = 'questionRef'))", Integer.class,
                forecasterId, forecasterId, forecasterId);
        return n == null ? 0 : n;
    }
}
