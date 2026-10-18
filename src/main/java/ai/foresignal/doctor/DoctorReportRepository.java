package ai.foresignal.doctor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/** {@code doctor_report} rows (prompt 140). Ids are ULIDs, so id order is creation order. */
public interface DoctorReportRepository extends JpaRepository<DoctorReportEntity, String> {

    /** A source's reports, newest first. */
    List<DoctorReportEntity> findBySourceIdOrderByIdDesc(String sourceId, Limit limit);

    /** A source's reports older than {@code id}, newest first (keyset paging). */
    List<DoctorReportEntity> findBySourceIdAndIdLessThanOrderByIdDesc(String sourceId, String id, Limit limit);

    /** A source's newest report. */
    Optional<DoctorReportEntity> findFirstBySourceIdOrderByIdDesc(String sourceId);

    /** A source's newest report of one trigger. */
    Optional<DoctorReportEntity> findFirstBySourceIdAndTriggerOrderByIdDesc(String sourceId, String trigger);

    /** Every report created at or after {@code since}, newest first. */
    List<DoctorReportEntity> findByCreatedAtGreaterThanEqualOrderByIdDesc(Instant since);

    /** The newest report of every source that has one. */
    @Query(value = "select distinct on (source_id) * from doctor_report order by source_id, id desc",
            nativeQuery = true)
    List<DoctorReportEntity> findLatestPerSource();

    /** Deletes the reports created before {@code cutoff}; the count deleted. */
    @Modifying
    @Transactional
    @Query("delete from DoctorReportEntity r where r.createdAt < :cutoff")
    int deleteCreatedBefore(@Param("cutoff") Instant cutoff);
}
