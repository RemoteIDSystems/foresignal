package ai.foresignal.export;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The bulk export jobs (prompt 111). */
public interface ExportJobRepository extends JpaRepository<ExportJobEntity, String> {

    /** Jobs by principal, newest first. */
    List<ExportJobEntity> findByPrincipalOrderByCreatedAtDesc(String principal);

    /** Every job, newest first. */
    List<ExportJobEntity> findAllByOrderByCreatedAtDesc();

    /** Jobs whose expiry has passed. */
    List<ExportJobEntity> findByExpiresAtBefore(Instant cutoff);

    /** Jobs a source's archive or a sweep may delete. */
    List<ExportJobEntity> findByKindAndStatus(String kind, String status);
}
