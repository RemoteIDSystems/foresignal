package ai.foresignal.pol;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The stays (prompt 114). */
public interface PolStayRepository extends JpaRepository<PolStayEntity, String> {

    /** A subject's stays arriving at or after {@code from}, in arrive order. */
    List<PolStayEntity> findBySubjectIdAndArriveGreaterThanEqualOrderByArriveAscIdAsc(String subjectId, Instant from);

    /** A subject's stays in arrive order. */
    List<PolStayEntity> findBySubjectIdOrderByArriveAscIdAsc(String subjectId);

    /** A subject's earliest stay. */
    java.util.Optional<PolStayEntity> findFirstBySubjectIdOrderByArriveAsc(String subjectId);

    /** A subject's number of stays. */
    long countBySubjectId(String subjectId);

    /** Deletes a subject's stays. */
    long deleteBySubjectId(String subjectId);

    /** Deletes a subject's stays formed from one record's fixes (the 039 purge). */
    long deleteBySubjectIdAndRecordRef(String subjectId, String recordRef);
}
