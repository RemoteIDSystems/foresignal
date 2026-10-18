package ai.foresignal.report;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Recorded report sends (prompt 132). */
public interface ReportSendRepository extends JpaRepository<ReportSendEntity, String> {

    /** The sends of a report, oldest first. */
    List<ReportSendEntity> findByReportIdOrderBySentAtAscIdAsc(String reportId);

    /** Deletes the sends of a deleted report. */
    void deleteByReportId(String reportId);
}
