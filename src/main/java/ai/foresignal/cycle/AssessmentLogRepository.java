package ai.foresignal.cycle;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code assessment_log} rows (prompt 128). */
public interface AssessmentLogRepository extends JpaRepository<AssessmentLogEntity, Long> {

    /** The rows after {@code after}, for SSE replay. */
    List<AssessmentLogEntity> findByIdGreaterThanOrderByIdAsc(long after, Limit limit);

    /** The newest row. */
    Optional<AssessmentLogEntity> findTop1ByOrderByIdDesc();
}
