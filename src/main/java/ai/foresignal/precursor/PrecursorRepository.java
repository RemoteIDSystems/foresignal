package ai.foresignal.precursor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code precursor} rows (prompt 120). */
public interface PrecursorRepository extends JpaRepository<PrecursorEntity, String> {

    /** The precursor of a definition key. */
    Optional<PrecursorEntity> findByCandidateKey(String candidateKey);

    /** The precursors in any of {@code statuses}, oldest first. */
    List<PrecursorEntity> findByStatusInOrderByIdAsc(Collection<String> statuses);

    /** The precursors of one kind in one status, oldest first. */
    List<PrecursorEntity> findByKindAndStatusOrderByIdAsc(String kind, String status);
}
