package ai.foresignal.intel;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** {@code intelligence_event} access (prompt 101). */
public interface IntelligenceEventRepository extends JpaRepository<IntelligenceEventEntity, String> {

    /** The newest events, updated-time descending. */
    List<IntelligenceEventEntity> findAllByOrderByUpdatedAtDescIdDesc(Limit limit);

    /** Keyset page: events updated strictly before {@code before}. */
    List<IntelligenceEventEntity> findByUpdatedAtBeforeOrderByUpdatedAtDescIdDesc(Instant before, Limit limit);

    /** Open events of a type, newest first (dedup scans these). */
    List<IntelligenceEventEntity> findByTypeAndStatusInOrderByUpdatedAtDesc(String type, List<String> statuses);

    /** The events a merged one points at. */
    List<IntelligenceEventEntity> findByMergedInto(String mergedInto);
}
