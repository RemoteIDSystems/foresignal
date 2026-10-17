package ai.foresignal.precursor;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code precursor_candidate} rows (prompt 120). */
public interface PrecursorCandidateRepository extends JpaRepository<PrecursorCandidateEntity, String> {

    /** Every candidate, oldest first. */
    List<PrecursorCandidateEntity> findAllByOrderByIdAsc();
}
