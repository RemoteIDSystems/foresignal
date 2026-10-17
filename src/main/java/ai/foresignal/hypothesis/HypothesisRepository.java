package ai.foresignal.hypothesis;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code hypothesis} rows (prompt 125). */
public interface HypothesisRepository extends JpaRepository<HypothesisEntity, String> {

    /** The hypotheses of a set in position order. */
    List<HypothesisEntity> findBySetIdOrderByPositionAscIdAsc(String setId);

    /** The hypotheses of several sets. */
    List<HypothesisEntity> findBySetIdInOrderByPositionAscIdAsc(Collection<String> setIds);
}
