package ai.foresignal.forecast;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code forecast} rows (prompt 122). Forecasts are inserted, never updated. */
public interface ForecastRepository extends JpaRepository<ForecastEntity, String> {

    /** Every forecast on a question, oldest first. */
    List<ForecastEntity> findByQuestionIdOrderByMadeAtAscIdAsc(String questionId);

    /** The newest forecast of a forecaster on a question. */
    Optional<ForecastEntity> findTop1ByQuestionIdAndForecasterIdOrderByMadeAtDescIdDesc(String questionId,
            String forecasterId);

    /** How many forecasts a question has. */
    long countByQuestionId(String questionId);

    /** Every forecast of a forecaster on the given questions. */
    List<ForecastEntity> findByForecasterIdAndQuestionIdIn(String forecasterId, List<String> questionIds);
}
