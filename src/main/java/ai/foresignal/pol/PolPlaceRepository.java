package ai.foresignal.pol;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The learned places (prompt 114). */
public interface PolPlaceRepository extends JpaRepository<PolPlaceEntity, String> {

    /** A subject's places in id order. */
    List<PolPlaceEntity> findBySubjectIdOrderByIdAsc(String subjectId);

    /** A subject's number of places. */
    long countBySubjectId(String subjectId);

    /** Deletes a subject's places. */
    long deleteBySubjectId(String subjectId);
}
