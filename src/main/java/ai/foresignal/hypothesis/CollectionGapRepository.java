package ai.foresignal.hypothesis;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code collection_gap} rows (prompt 125). */
public interface CollectionGapRepository extends JpaRepository<CollectionGapEntity, String> {

    /** The gaps of a set. */
    List<CollectionGapEntity> findBySetIdOrderByIdAsc(String setId);

    /** The gap of an indicator, if any. */
    Optional<CollectionGapEntity> findByIndicatorId(String indicatorId);

    /** Every gap, oldest first. */
    List<CollectionGapEntity> findAllByOrderByIdAsc();
}
