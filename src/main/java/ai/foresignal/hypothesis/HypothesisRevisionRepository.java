package ai.foresignal.hypothesis;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code hypothesis_revision} rows (prompt 125). */
public interface HypothesisRevisionRepository extends JpaRepository<HypothesisRevisionEntity, String> {

    /** The revisions of a set, newest first. */
    List<HypothesisRevisionEntity> findBySetIdOrderByIdDesc(String setId, Limit limit);

    /** The revisions of a set older than {@code before}, newest first (paging). */
    List<HypothesisRevisionEntity> findBySetIdAndIdLessThanOrderByIdDesc(String setId, String before, Limit limit);

    /** The revisions of a set written since {@code since}. */
    List<HypothesisRevisionEntity> findBySetIdAndAtGreaterThanEqualOrderByIdAsc(String setId, Instant since);

    /** The newest revision of a set. */
    Optional<HypothesisRevisionEntity> findTop1BySetIdOrderByIdDesc(String setId);

    /** How many revisions a set has. */
    long countBySetId(String setId);
}
