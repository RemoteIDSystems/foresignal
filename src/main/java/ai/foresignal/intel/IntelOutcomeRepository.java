package ai.foresignal.intel;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The resolved outcomes behind intelligence calibration (prompt 151, {@code intel_outcome}). */
public interface IntelOutcomeRepository extends JpaRepository<IntelOutcomeEntity, String> {

    /** The outcomes of one scope, oldest first. */
    List<IntelOutcomeEntity> findByScopeOrderByResolvedAtAsc(String scope);

    /** The outcome of one subject, or empty (the pair is unique). */
    Optional<IntelOutcomeEntity> findBySubjectKindAndSubjectId(String subjectKind, String subjectId);
}
