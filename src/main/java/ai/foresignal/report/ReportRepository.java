package ai.foresignal.report;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Reports (prompt 132). */
public interface ReportRepository extends JpaRepository<ReportEntity, String> {

    /** A report by display id. */
    Optional<ReportEntity> findByDisplayId(String displayId);

    /** Every report, newest change first. */
    List<ReportEntity> findAllByOrderByUpdatedAtDescIdAsc();
}
