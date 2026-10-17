package ai.foresignal.pol;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The pattern-of-life subjects (prompt 114). */
public interface PolSubjectRepository extends JpaRepository<PolSubjectEntity, String> {

    /** Every subject in id (creation) order. */
    List<PolSubjectEntity> findAllByOrderByIdAsc();

    /** The enabled subjects of one privacy class. */
    List<PolSubjectEntity> findByEnabledTrueAndPrivateSubject(boolean privateSubject);

    /** The subjects of a type and reference. */
    List<PolSubjectEntity> findBySubjectTypeAndRef(String subjectType, String ref);
}
