package ai.foresignal.forecast;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code forecast_log} rows (prompt 122). */
public interface ForecastLogRepository extends JpaRepository<ForecastLogEntity, Long> {

    /** The rows after {@code after}, for SSE replay. */
    List<ForecastLogEntity> findByIdGreaterThanOrderByIdAsc(long after, Limit limit);

    /** The newest row. */
    Optional<ForecastLogEntity> findTop1ByOrderByIdDesc();
}
