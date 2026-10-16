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

    /** Events of a type and status that ended at or after {@code endAfter}, newest first (reopen scans these). */
    List<IntelligenceEventEntity> findByTypeAndStatusAndEndTimeGreaterThanEqualOrderByUpdatedAtDesc(String type,
            String status, Instant endAfter);

    /** One page of every event in id order (the {@code foresignal-intel} generator's full pass). */
    org.springframework.data.domain.Slice<IntelligenceEventEntity> findAllByOrderByIdAsc(
            org.springframework.data.domain.Pageable page);

    /** Events updated at or after {@code since}, in id order, one page at a time (incremental passes). */
    org.springframework.data.domain.Slice<IntelligenceEventEntity> findByUpdatedAtGreaterThanEqualOrderByIdAsc(
            Instant since, org.springframework.data.domain.Pageable page);

    /** The events a merged one points at. */
    List<IntelligenceEventEntity> findByMergedInto(String mergedInto);
}
