package ai.foresignal.export;

import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Export, snapshot and pack jobs (prompt 111). */
public interface BulkExportJobRepository extends JpaRepository<ExportJobEntity, String> {

    /**
     * A keyset page of jobs, newest first (ULIDs sort by creation time).
     *
     * @param principal only this token's jobs, or null for every job (admin)
     * @param kind only this kind, or null
     * @param status only this status, or null
     * @param cursor only jobs older than this id, or null for the first page
     */
    @Query("SELECT j FROM BulkExportJob j WHERE (:principal IS NULL OR j.principal = :principal)"
            + " AND (:kind IS NULL OR j.kind = :kind) AND (:status IS NULL OR j.status = :status)"
            + " AND (:cursor IS NULL OR j.id < :cursor) ORDER BY j.id DESC")
    List<ExportJobEntity> page(@Param("principal") String principal, @Param("kind") String kind,
            @Param("status") String status, @Param("cursor") String cursor, Limit limit);

    /** Jobs in any of {@code statuses}. */
    List<ExportJobEntity> findByStatusIn(List<String> statuses);

    /** Number of jobs in {@code status}. */
    long countByStatus(String status);
}
