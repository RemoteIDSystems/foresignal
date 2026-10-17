package ai.foresignal.hypothesis;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code hypothesis_log} rows (prompt 125). */
public interface HypothesisLogRepository extends JpaRepository<HypothesisLogEntity, Long> {

    /** The rows after {@code after}, for SSE replay. */
    List<HypothesisLogEntity> findByIdGreaterThanOrderByIdAsc(long after, Limit limit);

    /** The newest row. */
    Optional<HypothesisLogEntity> findTop1ByOrderByIdDesc();
}
