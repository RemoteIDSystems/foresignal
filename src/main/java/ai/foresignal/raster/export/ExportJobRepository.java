package ai.foresignal.raster.export;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The raster export jobs (prompt 108). */
public interface ExportJobRepository extends JpaRepository<ExportJobEntity, String> {

    /** Jobs that finished before {@code cutoff} (retention). */
    List<ExportJobEntity> findByFinishedAtBefore(Instant cutoff);

    /** Jobs whose expiry has passed, finished or not (retention). */
    List<ExportJobEntity> findByExpiresAtBefore(Instant cutoff);

    /** Jobs still in one of {@code statuses} that were created before {@code cutoff} (stuck jobs). */
    List<ExportJobEntity> findByStatusInAndCreatedAtBefore(Collection<String> statuses, Instant cutoff);

    /** The newest job of {@code kind} with {@code inputHash}, created after {@code after}, in one of {@code statuses}. */
    Optional<ExportJobEntity> findFirstByKindAndInputHashAndCreatedAtAfterAndStatusInOrderByCreatedAtDesc(String kind,
            String inputHash, Instant after, Collection<String> statuses);
}
