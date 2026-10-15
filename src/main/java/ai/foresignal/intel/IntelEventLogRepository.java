package ai.foresignal.intel;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code intelligence_event_log} access (prompt 101). */
public interface IntelEventLogRepository extends JpaRepository<IntelEventLogEntity, Long> {

    /** An event's log, oldest first. */
    List<IntelEventLogEntity> findByEventIdOrderByIdAsc(String eventId);

    /** Log rows after {@code after}, oldest first (the SSE replay). */
    List<IntelEventLogEntity> findByIdGreaterThanOrderByIdAsc(long after, Limit limit);

    /** The newest log row, or null (the topic head). */
    java.util.Optional<IntelEventLogEntity> findTop1ByOrderByIdDesc();
}
