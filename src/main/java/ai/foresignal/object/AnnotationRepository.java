package ai.foresignal.object;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code annotation} table. */
public interface AnnotationRepository extends JpaRepository<AnnotationEntity, String> {

    /** Live annotations of a target, newest first. */
    List<AnnotationEntity> findByTargetRefAndDeletedAtIsNullOrderByIdDesc(String targetRef, Limit limit);

    /** Live annotations of a target older than a cursor id, newest first. */
    List<AnnotationEntity> findByTargetRefAndDeletedAtIsNullAndIdLessThanOrderByIdDesc(String targetRef, String id,
            Limit limit);

    /** Live verdicts created at or after a time, oldest first (precision, cycle packets). */
    List<AnnotationEntity> findByKindAndDeletedAtIsNullAndCreatedAtGreaterThanEqualOrderByCreatedAtAscIdAsc(String kind,
            Instant since);

    /** Live verdicts on a target, newest first. */
    List<AnnotationEntity> findByTargetRefAndKindAndDeletedAtIsNullOrderByIdDesc(String targetRef, String kind);

    /** Live replies under a root, oldest first (prompt 130 threads). */
    List<AnnotationEntity> findByParentIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(String parentId);

    /** Live root annotations of a target, newest first (prompt 130 threads). */
    List<AnnotationEntity> findByTargetRefAndParentIdIsNullAndDeletedAtIsNullOrderByIdDesc(String targetRef,
            Limit limit);

    /** Live root annotations of a target older than a cursor id, newest first (prompt 130 threads). */
    List<AnnotationEntity> findByTargetRefAndParentIdIsNullAndDeletedAtIsNullAndIdLessThanOrderByIdDesc(
            String targetRef, String id, Limit limit);

    /** Live annotations with {@code kind} (the prompt 130 migration of {@code tag} annotations). */
    List<AnnotationEntity> findByKindAndDeletedAtIsNull(String kind);
}
