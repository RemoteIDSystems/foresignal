package ai.foresignal.cycle;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code assessment} rows (prompt 128). */
public interface AssessmentRepository extends JpaRepository<AssessmentEntity, String> {

    /** The newest assessment of a cycle. */
    Optional<AssessmentEntity> findTop1ByCycleIdOrderByIdDesc(String cycleId);

    /** The newest assessment of a cadence. */
    Optional<AssessmentEntity> findTop1ByCadenceOrderByIdDesc(String cadence);

    /** The newest public assessment of a cadence. */
    Optional<AssessmentEntity> findTop1ByCadenceAndPrivateAssessmentFalseOrderByIdDesc(String cadence);

    /** The newest assessment of a situation. */
    Optional<AssessmentEntity> findTop1BySituationIdOrderByIdDesc(String situationId);
}
