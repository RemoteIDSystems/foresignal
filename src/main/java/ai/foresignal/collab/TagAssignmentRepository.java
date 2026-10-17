package ai.foresignal.collab;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code tag_assignment} table. */
public interface TagAssignmentRepository extends JpaRepository<TagAssignmentEntity, String> {

    /** The assignments of several objects. */
    List<TagAssignmentEntity> findByObjectRefIn(Collection<String> refs);

    /** The assignments of one object. */
    List<TagAssignmentEntity> findByObjectRefOrderByAssignedAtAsc(String ref);

    /** One assignment. */
    Optional<TagAssignmentEntity> findByTagIdAndObjectRef(String tagId, String ref);

    /** Assignments of a tag after a cursor id, by id. */
    List<TagAssignmentEntity> findByTagIdAndIdGreaterThanOrderByIdAsc(String tagId, String after, Limit limit);

    /** Every assignment of a tag. */
    List<TagAssignmentEntity> findByTagId(String tagId);

    /** How many objects carry a tag. */
    long countByTagId(String tagId);

    /** Deletes a tag's assignments. */
    void deleteByTagId(String tagId);

    /** Deletes the assignments of an object (a deleted work product). */
    void deleteByObjectRef(String ref);
}
