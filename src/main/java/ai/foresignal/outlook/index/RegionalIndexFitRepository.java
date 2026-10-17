package ai.foresignal.outlook.index;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** The {@code regional_index_fit} rows (prompt 126). */
public interface RegionalIndexFitRepository extends JpaRepository<RegionalIndexFitEntity, String> {

    /** The newest fit of an index. */
    Optional<RegionalIndexFitEntity> findTop1ByIndexIdOrderByCreatedAtDescIdDesc(String indexId);

    /** The newest finished fit of an index with proposed weights. */
    Optional<RegionalIndexFitEntity> findTop1ByIndexIdAndStatusOrderByCreatedAtDescIdDesc(String indexId,
            String status);
}
