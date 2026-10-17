package ai.foresignal.situation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The situation log (prompt 118). */
public interface SituationLogRepository extends JpaRepository<SituationLogEntity, Long> {

    /** The log of a situation, oldest first. */
    List<SituationLogEntity> findBySituationIdOrderByIdAsc(String situationId);

    /** Log rows after a cursor that announced an SSE event (the {@code situations} topic replay). */
    List<SituationLogEntity> findByIdGreaterThanAndEventIsNotNullOrderByIdAsc(long after, Limit limit);

    /** The newest log row (the topic head). */
    Optional<SituationLogEntity> findTop1ByOrderByIdDesc();

    /** How many rows of {@code action} were written since {@code since} (the brief's daily cap). */
    long countByActionAndAtGreaterThanEqual(String action, Instant since);
}
