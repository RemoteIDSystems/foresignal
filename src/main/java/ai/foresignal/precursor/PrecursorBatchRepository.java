package ai.foresignal.precursor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code precursor_batch} rows (prompt 120). */
public interface PrecursorBatchRepository extends JpaRepository<PrecursorBatchEntity, String> {

    /** The batch of a 103 job. */
    Optional<PrecursorBatchEntity> findByJobId(String jobId);
}
