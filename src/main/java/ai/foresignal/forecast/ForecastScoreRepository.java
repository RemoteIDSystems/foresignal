package ai.foresignal.forecast;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code forecast_score} rows (prompt 122). */
public interface ForecastScoreRepository extends JpaRepository<ForecastScoreEntity, String> {

    /** The scores of one question. */
    List<ForecastScoreEntity> findByQuestionId(String questionId);

    /** The scores of one forecaster. */
    List<ForecastScoreEntity> findByForecasterId(String forecasterId);

    /** The scores of one category, oldest resolution first. */
    List<ForecastScoreEntity> findByCategoryOrderByResolvedAtAsc(String category);

    /** Every score, oldest resolution first. */
    List<ForecastScoreEntity> findAllByOrderByResolvedAtAsc();
}
