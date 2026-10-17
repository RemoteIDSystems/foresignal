package ai.foresignal.analytics.finding;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code analytic_finding_log} rows (prompt 119). */
public interface FindingLogRepository extends JpaRepository<FindingLogEntity, Long> {

    /** The log of one finding. */
    List<FindingLogEntity> findByFindingIdOrderByIdAsc(String findingId);

    /** The rows that announced an event after {@code after}, for SSE replay. */
    List<FindingLogEntity> findByIdGreaterThanAndEventIsNotNullOrderByIdAsc(long after, Limit limit);

    /** The newest row. */
    Optional<FindingLogEntity> findTop1ByOrderByIdDesc();
}
