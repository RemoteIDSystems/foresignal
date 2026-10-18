package ai.foresignal.report;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Published report revisions (prompt 132). */
public interface ReportRevisionRepository extends JpaRepository<ReportRevisionEntity, String> {

    /** The revisions of a report, newest first. */
    List<ReportRevisionEntity> findByReportIdOrderByRevisionDesc(String reportId);

    /** One revision of a report. */
    Optional<ReportRevisionEntity> findByReportIdAndRevision(String reportId, int revision);

    /** The newest revision of a report. */
    Optional<ReportRevisionEntity> findFirstByReportIdOrderByRevisionDesc(String reportId);

    /** Deletes the revisions of a deleted report. */
    void deleteByReportId(String reportId);
}
