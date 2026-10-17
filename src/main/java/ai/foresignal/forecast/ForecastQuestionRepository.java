package ai.foresignal.forecast;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code forecast_question} rows (prompt 122). */
public interface ForecastQuestionRepository extends JpaRepository<ForecastQuestionEntity, String> {

    /** Questions with a dedup key. */
    List<ForecastQuestionEntity> findByDedupKey(String dedupKey);

    /** Questions in the given statuses, oldest due first. */
    List<ForecastQuestionEntity> findByStatusInOrderByResolveByAsc(Collection<String> statuses);

    /** Open questions past their closing time. */
    List<ForecastQuestionEntity> findByStatusAndClosesAtLessThanEqual(String status, Instant at);

    /** Questions in one status, by id. */
    List<ForecastQuestionEntity> findByStatusOrderByIdAsc(String status);
}
