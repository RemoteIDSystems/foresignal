package ai.foresignal.social;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code social_event_log} rows (prompt 135). */
public interface SocialEventLogRepository extends JpaRepository<SocialEventLogEntity, Long> {

    /** The rows after {@code after}, for SSE replay. */
    List<SocialEventLogEntity> findByIdGreaterThanOrderByIdAsc(long after, Limit limit);

    /** The newest row. */
    Optional<SocialEventLogEntity> findTop1ByOrderByIdDesc();
}
